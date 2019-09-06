/**
 * *****************************************************************************
 * Copyright (c) 2019 Christian Chiarcos, Niko Schenk
 * Applied Computational Linguistics Lab (ACoLi)
 * Goethe-Universität Frankfurt am Main
 * http://acoli.cs.uni-frankfurt.de
 * Robert-Mayer-Straße 10
 * 60325 Frankfurt am Main
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * *****************************************************************************
 */
package de.acoli.informatik.uni.frankfurt.de.reader;

import de.acoli.informatik.uni.frankfurt.de.aplusplus.Publication;
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.output.Bibliography;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.codehaus.jackson.map.ObjectMapper;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;
import static de.acoli.informatik.uni.frankfurt.de.reader.BibTextForDOIsExtractor.getBibTexForDoi;
import de.acoli.informatik.uni.frankfurt.de.util.MapUtil;
import de.acoli.informatik.uni.frankfurt.de.util.Utility;

/**
 *
 * Export a global chapter bibliography based on the content of the chapter
 * sections and subsections.
 *
 * @author niko
 */
public class ChapterBibliographyMaker {

    public static String DIR = "gen/";
    public static String SHARED_DIR = "gen/";

    public static String CHAPTER_STRUCTURE = "chap-struc.html";
    public static String CORPUS_JSON = "corpus.json";

    // Paper at least cited by MINIMUM_CITATION_FREQUENCY distinct papers.
    public static int MINIMUM_CITATION_FREQUENCY = 2;

    public static void main(String[] args) throws IOException, ParseException {

        if (args.length == 2) {
            DIR = args[0];
            SHARED_DIR = args[1];
        }

        // Read in corpus.json.
        byte[] jsonData = Files.readAllBytes(Paths.get(SHARED_DIR + CORPUS_JSON));
        ObjectMapper mapper = new ObjectMapper();
        List<Publication> publications = Arrays.asList(mapper.readValue(jsonData, Publication[].class));

        LinkedHashMap<String, String> sectionToDocids = ChapterStructureReader.getSectionToDocAssignments(DIR + CHAPTER_STRUCTURE);
        int numChapters = ChapterStructureReader.getNumChapters();

        // Make "bibliography" folders.
        for (int i = 1; i <= numChapters; i++) {
            String chaptersDir = DIR + "chapters/" + i + "/bibliography";
            File directory = new File(chaptersDir);
            if (directory.exists()) {
                Utility.deleteDirectory(directory);
            }
            directory.mkdir();
        }

        // In each chapter, collect all local biblographies.
        for (int i = 1; i <= numChapters; i++) {

            HashMap<String, Integer> normCRToFreqMap = new HashMap<>();

            String bibFolder = DIR + "/chapters/" + i + "/bibliography/";
            PrintWriter w = new PrintWriter(new File(bibFolder + "bib_chap_" + i + ".bib"));
            w.write("NORM-CR\tdoc-CR\tdoc-DOI\tCR-DOI\tBibUnstructured\n");
            PrintWriter bibHtml = new PrintWriter(new File(DIR + "/chapters/" + i + "/bibliography/" + "bib_chap_" + i + ".html"));
            bibHtml.write("<h3>Main document references:  </h3>");
            // Export all references to documents from this chapter.
            ArrayList<Integer> documentIdsInThisChapter = new ArrayList<>();
            for (String aSectId : sectionToDocids.keySet()) {
                if (aSectId.startsWith(i + ".")) {
                    documentIdsInThisChapter.add(Integer.parseInt(sectionToDocids.get(aSectId)));
                }
            }

            ArrayList<String> doisForThisChapter = new ArrayList<>();
            for (int aDocumentIdOfThisChapter : documentIdsInThisChapter) {
                for (Publication p : publications) {
                    if (p.getDocId() == aDocumentIdOfThisChapter) {
                        doisForThisChapter.add(p.getDoi());
                    }
                }
            }

            String chapterMainDocsBib = bibFolder + "main_docs.bib";
            PrintWriter wr = new PrintWriter(new File(chapterMainDocsBib));
            int pubNum = 1;
            for (String aDocDoiForThisChapter : doisForThisChapter) {
                String bibtex = getBibTexForDoi(aDocDoiForThisChapter);
                wr.write("%" + pubNum + ": " + "\n");
                wr.write(bibtex + "\n");
                pubNum++;
            }
            wr.flush();
            wr.close();

            BibTeXDatabase db = new BibTeXConverter().loadDatabase(
                    new FileInputStream(chapterMainDocsBib));
            BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
            provider.addDatabase(db);
            CSL citeproc = new CSL(provider, "ieee");
            citeproc.setOutputFormat("html");
            provider.registerCitationItems(citeproc);
            Bibliography bibl = citeproc.makeBibliography();
            int numWrittenToHtml = 0;
            for (String entry : bibl.getEntries()) {
                numWrittenToHtml++;
            }
            bibHtml.write("<a href=\"" + "chapters/" + i + "/bibliography/" + "main_docs.bib" + "\" target=\"_blank\">"
                    + "Chapter Bibliography (BibTeX)" + "</a>\n");

            bibHtml.write("<h3>Other bibliographic references:</h3>");
            bibHtml.write("<table border=\"0\" align=\"left\" >\n");

            ArrayList<File> localBibFiles = new ArrayList<>();
            getLocalBibliographies(new File(DIR + "/chapters/" + i), localBibFiles);
            Collections.sort(localBibFiles);

            ArrayList<String> entries = new ArrayList<String>();
            ArrayList<String> alreadyExportedNormCRs = new ArrayList<String>();

            ArrayList<String> distinctDocDois = new ArrayList<String>();
            for (File aLocalBibFile : localBibFiles) {
                Scanner s = new Scanner(aLocalBibFile);
                // skip first line.
                s.nextLine();
                while (s.hasNextLine()) {
                    String aLine = s.nextLine().trim();
                    String normCR = aLine.split("\\t")[0];
                    String docDOI = aLine.split("\\t")[2];
                    String bibunstr = aLine.split("\\t")[4];
                    entries.add(normCR + "\t" + bibunstr);
                    addToMap(normCR, normCRToFreqMap, distinctDocDois, docDOI);
                    if (aLine.length() > 0) {
                        w.write(aLine + "\n");
                    }
                }
                s.close();
            }

            // A chapter collection of NORM-CRs can be as follows:
            // Different mentions, e.g.,
            //NORM-CR-30	CR2	10.1007/s11047-011-9285-6	10.1109/34.598228 // unique doc, single mention.
            //NORM-CR-30	CR6	10.1007/s00521-012-0962-x	10.1109/34.598228 // same doc, multiple mention.
            //NORM-CR-30	CR1	10.1007/s11042-013-1572-z	10.1109/34.598228 // another unique doc, another single mention.
            //NORM-CR-30	CR6	10.1007/s00521-012-0962-x	10.1109/34.598228 // same doc, multiple mention.
            Collections.sort(entries, new Comparator<String>() {
                public int compare(String s1, String s2) {
                    s1 = s1.substring(0, s1.indexOf("\t")).replace("NORM-CR-", "").trim();
                    s2 = s2.substring(0, s2.indexOf("\t")).replace("NORM-CR-", "").trim();

                    if (Integer.parseInt(s1) < Integer.parseInt(s2)) {
                        return -1;
                    }
                    if (Integer.parseInt(s2) < Integer.parseInt(s1)) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            for (String entry : entries) {
                String normCR = entry.substring(0, entry.indexOf("\t"));
                // Avoid duplicate norm CRs in html.
                if (!alreadyExportedNormCRs.contains(normCR)) {
                    bibHtml.write("<tr><th>" + normCR.replace("NORM-CR-", "") + ".</th><td>" + entry.substring(entry.indexOf("\t")) + "</td></tr>\n");
                    alreadyExportedNormCRs.add(normCR);
                }
            }

            bibHtml.write("</table>");
            bibHtml.flush();
            bibHtml.close();
            w.flush();
            w.close();

            // Sort CR to freq map.
            Map<String, Integer> sortedMap = MapUtil.sortByValue2(normCRToFreqMap);

            // Export most frequent citations.
            PrintWriter mostFrequentlyCitedPapersExportW = new PrintWriter(new File(bibFolder + "most_freq_cit_papers.txt"));
            // Save to external file.
            for (String aNormCR : sortedMap.keySet()) {
                int freq = sortedMap.get(aNormCR);
                if (freq >= MINIMUM_CITATION_FREQUENCY) {
                    mostFrequentlyCitedPapersExportW.write(aNormCR + ":\t" + freq + "\n");
                }
            }
            mostFrequentlyCitedPapersExportW.flush();
            mostFrequentlyCitedPapersExportW.close();
            //System.out.println();
        }
    }

    public static void getLocalBibliographies(final File folder, ArrayList<File> files) {
        if (folder.listFiles() == null) {
            System.err.println("Sorry, please verify that this folder exists: " + folder);
            System.exit(0);
        }
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getLocalBibliographies(fileEntry, files);
            } else {
                if (fileEntry.getName().equals("section-sentences-restructured-and-bracket-content-tok.bib")) {
                    files.add(fileEntry);
                }
            }
        }
    }

    private static void addToMap(String normCR, HashMap<String, Integer> normCRToFreqMap,
            ArrayList<String> docDois, String aDocDoi) {

        if (docDois.contains(aDocDoi)) {
            // do nothing.
        } else {
            if (normCRToFreqMap.containsKey(normCR)) {
                // Get old freq.
                int oldfreq = normCRToFreqMap.get(normCR);
                oldfreq++;
                normCRToFreqMap.put(normCR, oldfreq);
            } else {
                normCRToFreqMap.put(normCR, 1);
            }
            // now, we have seen this docDoi, don't increment any other
            // occurrence of a normCR when we see the same document again,
            // in either conclusion, paper, etc.
            docDois.add(aDocDoi);
        }
    }

}
