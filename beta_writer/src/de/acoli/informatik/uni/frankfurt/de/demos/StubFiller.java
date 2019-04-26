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
package de.acoli.informatik.uni.frankfurt.de.demos;

import de.acoli.informatik.uni.frankfurt.de.aplusplus.Publication;
import static de.acoli.informatik.uni.frankfurt.de.demos.BySynonymReplacer.produceSynonymHtmlFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codehaus.jackson.map.ObjectMapper;
import org.jbibtex.ParseException;
import static de.acoli.informatik.uni.frankfurt.de.reader.BibTextForDOIsExtractor.getBibTexForDoi;
import static de.acoli.informatik.uni.frankfurt.de.reader.BibTextForDOIsExtractor.getBibliographyHTMLforBibtexfile;
import static de.acoli.informatik.uni.frankfurt.de.reader.BibTextForDOIsExtractor.writeBibtexEntriesToFile;
import de.acoli.informatik.uni.frankfurt.de.reader.MaskedItemsReader;
import de.acoli.informatik.uni.frankfurt.de.util.Utility;

/**
 *
 *
 * @author Niko
 */
public class StubFiller {

    public static String DIR = "gen/";
    public static String CHAP_STRUC = "chap-struc.html";
    public static String CORPUS_JSON = "corpus.json";

    public static String RESTRUCTURED_SENTENCES_SYN = "sentences-restructured-and-bracket-introduced-citnorm-syn.html";
    public static String RESTRUCTURED_SENTENCES_SYN_RELWORK = "sentences-restructured-and-bracket-introduced-citnorm-syn-relwork.html";

    public static String PATH_TO_TEXTRANK = "textrank/summ_and_keywords.py";
    public static String PATH_TO_PYTHON = "/usr/local/bin/python3";

    public static String DMYINTRO = "DUMMY_INTRODUCTION";
    public static String DMYCONCL = "DUMMY_CONCLUSION";
    public static String DMYRLTWOK = "DUMMY_RELATEDWORK";
    public static String DMYRFRCS = "DUMMY_REFERENCES";

    public static boolean USE_PRECOMPUTED_SYNONYMS;

    public static LinkedHashMap<String, String> entitiesMap;

    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {

        if (args.length == 4) {
            DIR = args[0];
            PATH_TO_TEXTRANK = args[1];
            PATH_TO_PYTHON = args[2];
            USE_PRECOMPUTED_SYNONYMS = Boolean.parseBoolean(args[3]); // Only for related work.
        }

        // Entities to be replaced in the HTML, e.g., ENTITY_15 => Fe<sup>3+</sup>
        entitiesMap = MaskedItemsReader.getEntities(DIR + "/data/" + MaskedItemsReader.ENTITY_MAP_NAME);

        // Read in corpus.json.
        byte[] jsonData = Files.readAllBytes(Paths.get(DIR + CORPUS_JSON));
        ObjectMapper mapper = new ObjectMapper();
        List<Publication> publications = Arrays.asList(mapper.readValue(jsonData, Publication[].class));

        ArrayList<String> doisOfChap = new ArrayList<String>();

        Scanner s = new Scanner(new File(DIR + CHAP_STRUC));
        while (s.hasNextLine()) {
            String aLine = s.nextLine();
            if (aLine.contains("(doc:") && aLine.contains("/rel:")) {
                String sectionNumber = "";
                if (aLine.contains("<h3>")) {
                    String startHeading = aLine.substring(aLine.indexOf("<h3>")).trim();
                    sectionNumber = startHeading.substring(4, startHeading.indexOf(" ")).trim();
                }

                String left = aLine.substring(0, aLine.indexOf("(doc:") + 1);
                String doc = aLine.substring(aLine.indexOf("(doc:") + 1, aLine.indexOf("/rel:"));
                String docNum = doc.replace("doc:", ""); // e.g., 23
                String right = aLine.substring(aLine.indexOf("/rel:"));
                // Get corresponding publication.
                Publication aPub = getCorrespondingPublication(publications, docNum);

                // Unmask entities in the document title
                System.out.print(unmaskAndClean(left));
                String doi = getDoiLink(aPub, docNum);
                doisOfChap.add(getDoi(aPub));
                //System.out.print(doc + "/");
                System.out.print(doi + ", " + aPub.domain);
                System.out.println(fillPaperStub(sectionNumber, right));
            } // Chapter introduction.
            else if (aLine.contains(DMYINTRO)) {
                fillSectionStub(aLine, "Introduction", DMYINTRO);
            } // Chapter conclusion.
            else if (aLine.contains(DMYCONCL)) {
                fillSectionStub(aLine, "Conclusion", DMYCONCL);
            } // Chapter related work with sentences containing most cited works.
            else if (aLine.contains(DMYRLTWOK)) {
                fillRelatedWorkSection(aLine, DMYRLTWOK);
            } // Chapter bibliography
            else if (aLine.contains(DMYRFRCS)) {
                fillReferenceSectionStub(aLine);
            } // clean keywords.
            else if ((aLine.contains("<h2>") || aLine.contains("<h1>")) && !aLine.contains("DUMMY_")) {
                String headingStart = "<h1>";
                String headingEnd = "</h1>";
                if (aLine.contains("<h2>")) {
                    headingStart = "<h2>";
                    headingEnd = "</h2>";
                }
                String left = aLine.substring(0, aLine.indexOf(headingStart) + 4);
                String keywordsWithChapNum = aLine.substring(aLine.indexOf(headingStart) + 4, aLine.indexOf("</button>") - 5);
                String chapNum = keywordsWithChapNum.substring(0, keywordsWithChapNum.indexOf(" "));
                String keywords = keywordsWithChapNum.substring(keywordsWithChapNum.indexOf(" ")).trim();
                String right = aLine.substring(aLine.indexOf(headingEnd));
                String cleanedKeywords = getCleanedKeywords(keywords);
                System.out.println(left + chapNum + " " + cleanedKeywords + right);
            } // TODO: Maybe add a discussion section?
            else {
                System.out.println(aLine);
            }

        }
        s.close();

    }

    private static void fillSectionStub(String aLine, String sectionType, String stub) throws FileNotFoundException {
        // Get chapter number.
        String chapNum = aLine.substring(aLine.indexOf(stub) + stub.length() + 1, aLine.indexOf(stub) + stub.length() + 2);
        File f = new File(DIR + "chapters/" + chapNum + "/" + sectionType + "/" + RESTRUCTURED_SENTENCES_SYN);
        // Make the restructured html file if it does not exist.
        if (!f.exists()) {
            System.err.println("Making file " + f.getAbsolutePath() + " because it was not generated (most likely empty restructuring content).");
            PrintWriter w = new PrintWriter(f);
            w.write("<html></html>");
            w.flush();
            w.close();
        }
        Scanner iS = new Scanner(f);
        StringBuilder iSBuilder = new StringBuilder();
        while (iS.hasNextLine()) {
            String introLine = iS.nextLine();
            //System.out.println(introLine);
            iSBuilder.append(unmaskAndClean(introLine) + "\n");
        }
        iSBuilder.append("<br>");
        iS.close();
        // Replace dummy stub by reordered intro.
        aLine = aLine.replace("<p>" + stub + "_" + chapNum + "_CONTENT</p>", iSBuilder.toString());
        // Some spacing at the end.
        System.out.println(aLine);

    }

    
    private static String fillPaperStub(String sectionNumber, String aLine) throws FileNotFoundException {

        StringBuilder sb = new StringBuilder();
        File f = new File(DIR + "chapters/" + sectionNumber.substring(0, sectionNumber.indexOf(".")) + "/Papers/" + sectionNumber + "/"
                + RESTRUCTURED_SENTENCES_SYN);
        if (!f.exists()) {
            System.err.println("Making file " + f.getAbsolutePath() + " because it was not generated (most likely empty restructuring content).");
            PrintWriter w = new PrintWriter(f);
            w.write("<html></html>");
            w.flush();
            w.close();
        }
        Scanner s = new Scanner(f);
        while (s.hasNextLine()) {
            String l = s.nextLine();
            if (l.contains("<h3>Abstract</h3>")) {
                //l = l.replace("<h3>Abstract</h3>", "<h3><i>Extended</i> Abstract</h3>");
                l = l.replace("<h3>Abstract</h3>", "<h3></h3>");
            }
            sb.append(unmaskAndClean(l) + "\n");
        }
        sb.append("<br>");
        s.close();
        String paperSectionSummaries = sb.toString();

        String patternStr = "<p>DUMMY_PAPER_\\d+\\.\\d+\\.\\d+\\._CONTENT</p>";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(aLine);
        while (matcher.find()) {
            String match = matcher.group(0);
            //System.out.println("match: "+ match);
            aLine = aLine.replace(match, paperSectionSummaries);
        }
        // Some spacing at the end.
        aLine = aLine.concat("<br>");
        // Remove the "rel" info.
        aLine = aLine.replaceAll("/rel:\\d+.\\d%", "");

        return aLine;

    }

    private static void fillReferenceSectionStub(String aLine) throws FileNotFoundException {
        int start = aLine.indexOf("_CONTENT</p></div>") - 2; // Detects up to 99 chapters.
        int end = aLine.indexOf("_CONTENT</p></div>");

        String chapNum = aLine.substring(start, end).replace("_", "");
        StringBuilder sb = new StringBuilder();
        Scanner s = new Scanner(new File(DIR + "chapters/" + chapNum + "/bibliography/"
                + "bib_chap_" + chapNum + ".html"));
        while (s.hasNextLine()) {
            String l = s.nextLine();
            sb.append(l + "\n");
        }
        s.close();
        String chapterBibliography = sb.toString();

        String patternStr = "<p>DUMMY_REFERENCES_\\d+_CONTENT</p>";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(aLine);
        while (matcher.find()) {
            String match = matcher.group(0);
            aLine = aLine.replace(match, chapterBibliography);
        }
        System.out.println(aLine);
    }

    private static void fillRelatedWorkSection(String aLine, String stub) throws FileNotFoundException {
        // Get chapter number.
        String chapNum = aLine.substring(aLine.indexOf(stub) + stub.length() + 1, aLine.indexOf(stub) + stub.length() + 2);

        // First, read in most frequent normalized CR references.
        ArrayList<String> mostFrequentNormCrfs = new ArrayList<>();
        Scanner citSentScan = new Scanner(new File(DIR + "chapters/" + chapNum + "/bibliography/most_freq_cit_papers.txt"));
        while (citSentScan.hasNextLine()) {
            String aNormCrf = citSentScan.nextLine().trim().split("\\t")[0];
            mostFrequentNormCrfs.add(aNormCrf.replace(":", ""));
        }
        citSentScan.close();

        // Collect all citing sentences for this chapter.
        ArrayList<File> citingSentencesFiles = new ArrayList<>();
        getCitingSentences(new File(DIR + "/chapters/" + chapNum + "/"), citingSentencesFiles);

        StringBuilder citingSentencesBuilder = new StringBuilder();
        citingSentencesBuilder.append("<p><br></p>");

        for (String aMostFrequentNormCrf : mostFrequentNormCrfs) {
            String chapBib = DIR + "/chapters/" + chapNum + "/bibliography" + "/bib_chap_" + chapNum + ".bib";
            String bibunstructuredHeadingForImportantPubliationInRelWork = getBibunstrForImpPubInRelWork(chapBib, aMostFrequentNormCrf);
            citingSentencesBuilder.append("<p><b>" + bibunstructuredHeadingForImportantPubliationInRelWork + "</b></p>\n");

            for (File f : citingSentencesFiles) {
                Scanner s = new Scanner(f);
                while (s.hasNextLine()) {
                    String l = s.nextLine();
                    String[] citingSentenceItems = l.trim().split("\\t");
                    String aNormCrf = citingSentenceItems[0];
                    String restructuredSent = citingSentenceItems[1];
                    String originalSent = citingSentenceItems[2];
                    String paperDoi = citingSentenceItems[3];

                    if (aMostFrequentNormCrf.equals(aNormCrf)) {
                        citingSentencesBuilder.append(unmaskAndClean(Utility.writeTooltipLine(restructuredSent, paperDoi, originalSent, false)));
                    }
                }
                s.close();
            }
            citingSentencesBuilder.append("<p><br></p>");

        }

        // Write citingSentencesBuilder content to html file.
        File relWorkFolder = new File(DIR + "/chapters/" + chapNum + "/" + "RelatedWork");
        if (!relWorkFolder.exists()) {
            relWorkFolder.mkdir();
        }
        String restructRelWorkHtml = relWorkFolder.getAbsolutePath() + "/" + "restruct-relwork.html";
        PrintWriter restructRelWorkParagraphsForChap = new PrintWriter(new File(restructRelWorkHtml));
        restructRelWorkParagraphsForChap.write(citingSentencesBuilder.toString());
        restructRelWorkParagraphsForChap.flush();
        restructRelWorkParagraphsForChap.close();
        HashMap<String, Integer> synrepToFreqMap = new HashMap<>();
        
        produceSynonymHtmlFile(restructRelWorkHtml, synrepToFreqMap, RESTRUCTURED_SENTENCES_SYN_RELWORK, USE_PRECOMPUTED_SYNONYMS);

        StringBuilder restrRelWorkWithSynonymsBuilder = new StringBuilder();
        Scanner s = new Scanner(new File(relWorkFolder.getAbsolutePath() + "/" + RESTRUCTURED_SENTENCES_SYN_RELWORK));
        while (s.hasNextLine()) {
            String l = s.nextLine();
            restrRelWorkWithSynonymsBuilder.append(l + "\n");
        }
        s.close();
        aLine = aLine.replace("<p>" + stub + "_" + chapNum + "_CONTENT</p>", restrRelWorkWithSynonymsBuilder.toString());
        System.out.println(aLine);
    }

    private static Publication getCorrespondingPublication(List<Publication> publications, String docNum) {
        for (Publication p : publications) {
            if (p.getDocId() == (Integer.parseInt(docNum))) {
                return p;
            }
        }
        System.err.println("Looks like no publication object could be found for doc num: " + docNum);
        return null;
    }

    private static String getDoiLink(Publication aPub, String docNum) {
        String doi = aPub.getDoi();
        if (doi.length() > 2) {
            return "<a href=\"https://doi.org/" + doi + "\" target=\"_blank\"/>" + doi + "</a>";
        } else {

            return "<a href=\"\"/>NO_DOI_FOUND</a>";
        }
    }

    private static String unmaskAndClean(String input) {
        // unmask every token.
        Pattern entityPattern = Pattern.compile("ENTITY_\\d+");
        Matcher entityMatcher = entityPattern.matcher(input);
        while (entityMatcher.find()) {
            String match = entityMatcher.group();
            // Replace it.
            input = input.replace(match, entitiesMap.get(match));
        }

        // Replace NORM-CR-XXX matches by just a [X].
        Pattern normCrPattern = Pattern.compile("NORM-CR-(\\d+)");
        // get a matcher object
        Matcher normCrMatcher = normCrPattern.matcher(input);
        while (normCrMatcher.find()) {
            String match = normCrMatcher.group();
            String crNumber = normCrMatcher.group(1);
            input = input.replace(match, "[" + crNumber + "]");
        }

        // Make the first character of a sentence upper case!
        if (input.startsWith("<div class=\"tooltip\">")) {
            if (Character.isLowerCase(input.charAt(21))) {
                input = input.substring(0, 21) + Character.toUpperCase(input.charAt(21)) + input.substring(22);
            }
        }
        // clean brackets.
        return Utility.strip(input);
    }

    private static String getDoi(Publication aPub) {
        if (!aPub.getArticleDoi().equals("-")) {
            return aPub.getArticleDoi();
        } else if (!aPub.getBookDoi().equals("-")) {
            return aPub.getBookDoi();
        } else if (!aPub.getChapterDoi().equals("-")) {
            return aPub.getChapterDoi();
        } else {
            return "DOI_UNDEFINED";
        }
    }

    public static void getCitingSentences(final File folder, ArrayList<File> files) {
        if (folder.listFiles() == null) {
            System.err.println("Sorry, please verify that this folder exists: " + folder);
            System.exit(0);
        }
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getCitingSentences(fileEntry, files);
            } else {
                if (fileEntry.getName().equals("citing-sentences.txt")) {
                    files.add(fileEntry);
                }
            }
        }
    }

    private static String getCleanedKeywords(String keywords) {
        ArrayList<String> keywordList = new ArrayList(Arrays.asList(keywords.split("\\, ")));
        // Consolidate on keywords.
        TreeSet<Integer> takeIdxs = new TreeSet<>();
        TreeSet<Integer> ignoreIdxs = new TreeSet<>();
        for (int i = 0; i < keywordList.size(); i++) {
            String candidate = keywordList.get(i);
            int takeIdx = i; // candidate;
            // Compare to every other one.
            for (int j = 0; j < keywordList.size(); j++) {
                String toCompare = keywordList.get(j);
                if (candidate.length() > 2 && toCompare.length() > 2) {
                    if (candidate.substring(0, 3).equals(toCompare.substring(0, 3))) {
                        if (candidate.length() >= toCompare.length()) {
                            // keep candidate.
                        } else if (toCompare.length() > candidate.length()) {
                            // set idx to new compare.
                            takeIdx = j;
                            // Reset.
                            candidate = keywordList.get(j);
                            // remove old keyword.
                            ignoreIdxs.add(i);
                        }
                    }
                }
            }
            takeIdxs.add(takeIdx);

        }

        StringBuilder keywordsBuilder = new StringBuilder();
        for (int i = 0; i < keywordList.size(); i++) {
            if (takeIdxs.contains(i) && !ignoreIdxs.contains(i)) {
                String key = keywordList.get(i);
                keywordsBuilder.append(key + ", ");
            }
        }

        String kwrds = keywordsBuilder.toString().trim();
        if (kwrds.length() > 0) {
            kwrds = kwrds.substring(0, keywordsBuilder.toString().length() - 2);
        }

        ArrayList<Integer> removeIndices = new ArrayList<>();
        if (kwrds.contains(",")) {
            ArrayList<String> wordsToBeCheckedAgain = new ArrayList(Arrays.asList(kwrds.split("\\, ")));
            for (int w = 0; w < wordsToBeCheckedAgain.size(); w++) {
                String word = wordsToBeCheckedAgain.get(w);
                if (wordsToBeCheckedAgain.size() > 1) {
                    for (int w2 = 1; w2 < wordsToBeCheckedAgain.size(); w2++) {
                        String word2 = wordsToBeCheckedAgain.get(w2);
                        if (word2.endsWith(word) && !word2.equals(word)) {
                            removeIndices.add(w);
                        }
                        if (word.endsWith(word2) && !word2.equals(word)) {
                            removeIndices.add(w2);
                        }
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        ArrayList<String> updatedKeywords = new ArrayList(Arrays.asList(kwrds.split("\\, ")));
        for (int u = 0; u < updatedKeywords.size(); u++) {
            if (!removeIndices.contains(u)) {
                if (u == updatedKeywords.size() - 1) {
                    sb.append(updatedKeywords.get(u));
                } else {
                    sb.append(updatedKeywords.get(u) + ", ");
                }
            }
        }
        kwrds = sb.toString().trim();
        if (kwrds.endsWith(",")) {
            kwrds = kwrds.substring(0, kwrds.length() - 1);
        }
        return kwrds;
    }

    private static String getBibunstrForImpPubInRelWork(String chapBib, String aMostFrequentNormCrf) throws FileNotFoundException {
        Scanner s = new Scanner(new File(chapBib));
        String bibunstr = "null";
        while (s.hasNextLine()) {
            String aLine = s.nextLine().trim();
            String[] items = aLine.split("\\t");
            if (items[0].equals(aMostFrequentNormCrf)) {
                bibunstr = items[4] + " (" + "<a href=\"https://doi.org/" + items[3] + "\" target=\"_blank\"/>" + items[3] + "</a>)";
            }
        }
        s.close();
        return bibunstr;
    }
}
