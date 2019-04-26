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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import de.acoli.informatik.uni.frankfurt.de.util.MapUtil;
import de.acoli.informatik.uni.frankfurt.de.util.Utility;

/**
 *
 * @author niko
 */
public class MostCitedPapersComputer {

    public static String DIR = "gen/";

    // Chapter structure as produced by mkstructure.py
    public static String CHAPTER_STRUCTURE = "chap-struc.html";
    public static String CORPUS_JSON = "corpus.json";

    public static void main(String[] args) throws IOException {

        if (args.length == 1) {
            DIR = args[0];
        }

        LinkedHashMap<Integer, ArrayList<String>> chapToSentences = getChapterToCitationSentences(DIR + CORPUS_JSON, DIR + CHAPTER_STRUCTURE);

        for (int chap : chapToSentences.keySet()) {
            // System.out.println(chap + ": " + chapToSentences.get(chap));
        }

    }

    public static LinkedHashMap<Integer, ArrayList<String>> getChapterToCitationSentences(String pathToCorpusJSON, String pathToChapStruc) throws IOException {

        LinkedHashMap<Integer, ArrayList<String>> rval = new LinkedHashMap<Integer, ArrayList<String>>();
        // Read in corpus.json.
        byte[] jsonData = Files.readAllBytes(Paths.get(pathToCorpusJSON));
        ObjectMapper mapper = new ObjectMapper();
        List<Publication> publications = Arrays.asList(mapper.readValue(jsonData, Publication[].class));
        LinkedHashMap<String, String> sectionToDocids = ChapterStructureReader.getSectionToDocAssignments(pathToChapStruc);
        int numChapters = ChapterStructureReader.getNumChapters();

        System.err.println("Related work, chapter citation sentences:");
        // For every chapter.
        for (int chapNum = 1; chapNum <= numChapters; chapNum++) {
            // X most frequent dois for chapter.
            ArrayList<String> doisForChapter = getMostFrequentDoireferencesForChapter(chapNum, 4, publications, sectionToDocids);
            System.err.println("*******");
            System.err.println("Chapter " + chapNum + ": " + doisForChapter + "\n\n");

            ArrayList<String> citationSentencesDedup = new ArrayList<>();
            // For all X DOIs
            for (String aDoi : doisForChapter) {
                //System.out.println("\tLooking references to DOI: " + aDoi);
                ArrayList<String> citationSentences = getCitationSentences(aDoi, chapNum, publications, sectionToDocids);

                for (String citationSent : citationSentences) {
                    if (!citationSentencesDedup.contains(citationSent)) {
                        citationSentencesDedup.add(citationSent);
                    }
                }
            }

            for (String s : citationSentencesDedup) {
                //System.out.println(s);
            }
            rval.put(chapNum, citationSentencesDedup);

        }
        return rval;

    }

    // List of citation sentences for DOI
    public static ArrayList<String> getCitationSentences(String aDoi, int chapNum, List<Publication> publications, LinkedHashMap<String, String> sectionToDocids) {
        ArrayList<String> citationSentences = new ArrayList<>();
        // For every publication in that chapter
        // extract all sentences containing the doi reference.
        for (String aSectiontodocid : sectionToDocids.keySet()) {
            if (aSectiontodocid.startsWith(String.valueOf(chapNum) + ".")) {
                String docID = sectionToDocids.get(aSectiontodocid);
                // Get corresponding publication object.
                for (Publication p : publications) {
                    if (p.getDocId() == Integer.parseInt(docID)) {
                        int maxIntroSentencesPerDoi = 7;
                        // Collect all sentences from that publication with that citation ref.
                        ArrayList<ArrayList<String>> intro = p.introductionTokens;
                        collectCitationSentences(intro, citationSentences, aDoi, maxIntroSentencesPerDoi);

                        int maxRelatedworkSentencesPerDoi = 7;
                        ArrayList<ArrayList<String>> related = p.relatedWorkTokens;
                        collectCitationSentences(related, citationSentences, aDoi, maxRelatedworkSentencesPerDoi);
                    }
                }
            }
        }
        return citationSentences;
    }

    public static ArrayList<String> getMostFrequentDoireferencesForChapter(int chapNum, int howmany, List<Publication> publications, LinkedHashMap<String, String> sectionToDocids) throws IOException {

        // Compute most frequently cited "DOI" for this chapter.
        ArrayList<String> doisforthischapter = new ArrayList<>();
        HashMap<String, Integer> doiToFreqMap = new HashMap<String, Integer>();
        for (String aSectiontodocid : sectionToDocids.keySet()) {
            if (aSectiontodocid.startsWith(String.valueOf(chapNum) + ".")) {
                // Get doc ID.
                String docID = sectionToDocids.get(aSectiontodocid);
                // Get corresponding publication object.
                for (Publication p : publications) {
                    if (p.getDocId() == Integer.parseInt(docID)) {
                        HashMap<String, ArrayList<String>> bibliography = p.getBibliography();
                        for (String citationRef : bibliography.keySet()) {
                            ArrayList<String> doiAndBibunstructured = bibliography.get(citationRef);
                            String doi = doiAndBibunstructured.get(0);
                            if (!doi.equals(APlusPlusCollectionsReader.NO_DOI_MARKER)) {
                                addToMap(doiToFreqMap, doi);
                            }
                        }
                        break;
                    }
                }
            }
        }

        Map<String, Integer> sorted = MapUtil.sortByValue(doiToFreqMap);
        
        ArrayList<String> dois = new ArrayList<>();
        for (String doi : sorted.keySet()) {
            dois.add(doi);
        }

        int howMany = howmany;
        for (int aDoiIdx = dois.size() - 1; aDoiIdx >= 0; aDoiIdx--) {
            if (howMany > 0) {
                String aDoi = dois.get(aDoiIdx);
                int freq = sorted.get(dois.get(aDoiIdx));
                //System.out.println(aDoi + ": " + freq);
                doisforthischapter.add(aDoi);
            }
            howMany--;
        }
        return (doisforthischapter);

    }

    private static void addToMap(HashMap<String, Integer> doiToFreqMap, String doi) {
        if (doiToFreqMap.containsKey(doi)) {
            // Get old freq.
            int oldFreq = doiToFreqMap.get(doi);
            oldFreq++;
            doiToFreqMap.put(doi, oldFreq);
        } else {
            doiToFreqMap.put(doi, 1);
        }
    }

    // Check if a token in the sentence contains the DOI, then collect that sentence.
    private static void collectCitationSentences(ArrayList<ArrayList<String>> intro, ArrayList<String> citationSentences, String aDoi, int numSentencesToAdd) {
        for (ArrayList<String> anIntroSent : intro) {
            for (int t = 0; t < anIntroSent.size(); t++) {
                String tok = anIntroSent.get(t);
                if (tok.split("\\|")[0].equals(aDoi)) {
                    String citationSentence = Utility.turnSingleSentenceIntoText(anIntroSent).trim();
                    if (!citationSentences.contains(citationSentence) && numSentencesToAdd > 0) {
                        citationSentences.add(citationSentence);
                        numSentencesToAdd--;
                    } else {
                    }
                    break;
                }
            }
        }
    }

}
