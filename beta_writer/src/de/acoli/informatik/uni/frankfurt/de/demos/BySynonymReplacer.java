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

import static de.acoli.informatik.uni.frankfurt.de.demos.NgramsSimilarityComputer.getPrecomputedSynonyms;
import static de.acoli.informatik.uni.frankfurt.de.demos.NgramsSimilarityComputer.getWord2VecSynonyms;
import static de.acoli.informatik.uni.frankfurt.de.demos.StubFiller.RESTRUCTURED_SENTENCES_SYN;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.acoli.informatik.uni.frankfurt.de.util.MapUtil;

/**
 *
 * @author niko
 */
public class BySynonymReplacer {

    public static TreeMap<String, ArrayList<String>> synonymPairs;
    public static TreeMap<String, ArrayList<String>> synonymPairsPrecomputed;
    public static TreeMap<String, ArrayList<String>> synonymPairsW2V;

    public static String DIR = "gen/";

    public static boolean USE_PRECOMPUTED_SYNONYMS;

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {

        if (args.length == 2) {
            DIR = args[0];
            USE_PRECOMPUTED_SYNONYMS = Boolean.parseBoolean(args[1]);
        }

        // Get all restructured files.
        ArrayList<String> htmlCitNormFiles = new ArrayList<String>();
        getHTMLCitNormFiles(new File(DIR + "/chapters/"), htmlCitNormFiles);

        PrintWriter wSynRepFreq = new PrintWriter(new File(DIR + "/ngram_replacements_stats.txt"));
        HashMap<String, Integer> synrepToFreqMap = new HashMap<>();

        // Replace all synyonyms in each file.
        for (String anHtmlCitNormFile : htmlCitNormFiles) {
            //System.err.println("Processing: " + anHtmlCitNormFile);
            produceSynonymHtmlFile(anHtmlCitNormFile, synrepToFreqMap, RESTRUCTURED_SENTENCES_SYN, USE_PRECOMPUTED_SYNONYMS);
        }

        Map<String, Integer> sortedMap = MapUtil.sortByValue2(synrepToFreqMap);
        for (String ngramToSynonymItem : sortedMap.keySet()) {
            wSynRepFreq.write(ngramToSynonymItem + ": " + sortedMap.get(ngramToSynonymItem) + "\n");
        }
        wSynRepFreq.flush();
        wSynRepFreq.close();

    }

    public static void getHTMLCitNormFiles(final File folder, ArrayList<String> files) {
        if (folder.listFiles() == null) {
            System.err.println("Sorry, please verify that this folder exists: " + folder);
            System.exit(0);
        }
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getHTMLCitNormFiles(fileEntry, files);
            } else {
                if (fileEntry.getAbsolutePath().endsWith("sentences-restructured-and-bracket-introduced-citnorm.html")) {
                    if (files.contains(fileEntry.getAbsolutePath())) {
                        System.err.println("file: " + fileEntry + " already present.");
                    }
                    files.add(fileEntry.getAbsolutePath());
                }
            }
        }
    }

    private static void addToMap(String unigram, String synonym, HashMap<String, Integer> synrepToFreqMap) {
        if (synrepToFreqMap.containsKey(unigram + "\t" + synonym)) {
            int oldFreq = synrepToFreqMap.get(unigram + "\t" + synonym);
            oldFreq++;
            synrepToFreqMap.put(unigram + "\t" + synonym, oldFreq);
        } else {
            synrepToFreqMap.put(unigram + "\t" + synonym, 1);
        }
    }

    public static void produceSynonymHtmlFile(String anHtmlCitNormFile,
            HashMap<String, Integer> synrepToFreqMap,
            String synonymOutputHTML, boolean usePrecomputedSyns) throws FileNotFoundException {

        int numModifiedSentences = 0;
        int numUnmodifiedSentences = 0;
        int numToks = 0;
        int numSentencesWOModificationsMade = 0;
        int numSentencesWithModificationMade = 0;
        int numReplacementsMade = 0;

        if (synonymPairs == null) {
            initializeSynonymPairs(usePrecomputedSyns);
        }

        StringBuilder sb = new StringBuilder();

        File htmlFile = new File(anHtmlCitNormFile);
        Scanner s = new Scanner(htmlFile);
        while (s.hasNextLine()) {
            String aLine = s.nextLine().trim();
            String toReplaceLine = "";
            String doiLine = "";
            String originalSentenceLine = "";
            boolean replaceThisLine = false;
            if (aLine.contains("<div class=\"tooltip\">")) {
                toReplaceLine = aLine.replace("<div class=\"tooltip\">", "");
                doiLine = s.nextLine().trim();
                originalSentenceLine = s.nextLine().trim();
                replaceThisLine = true;
            } else {
                // Heading or css style.
                sb.append(aLine + "\n");
            }

            if (replaceThisLine) {
                ArrayList<String> aSynonymReplacedSentence = new ArrayList<>();

                ArrayList<Integer> modifiedTokenIdxs = new ArrayList<>();
                LinkedHashMap<Integer, String> occupiedTokenIdxs = new LinkedHashMap<>();
                // Split into tokens.
                String[] tokens = toReplaceLine.split("\\s");
                numToks += tokens.length;

                // Get token indices which are forbidden to replace by synonyms
                // e.g., within quotes or citations.
                TreeSet<Integer> forbiddenIndices = getForbiddenIndicesForQuotes(tokens);

                // Replace all trigrams.
                if (tokens.length > 2) {
                    for (int tIdx = 2; tIdx < tokens.length; tIdx++) {
                        String first = tokens[tIdx - 2];
                        String second = tokens[tIdx - 1];
                        String third = tokens[tIdx];
                        String trigram = first + "_" + second + "_" + third;

                        if (synonymPairs.containsKey(trigram)
                                && !(forbiddenIndices.contains(tIdx - 2)
                                || forbiddenIndices.contains(tIdx - 1)
                                || forbiddenIndices.contains(tIdx))) {
                            // Replace by synonym and update modified tok indices.
                            ArrayList<String> synonymList = synonymPairs.get(trigram);
                            // Randomnly sample one.
                            String synonym = getRandomSynonym(synonymList);
                            synonym = synonym.replace("_", " ");
                            if (!modifiedTokenIdxs.contains(tIdx - 2) && !modifiedTokenIdxs.contains(tIdx - 1)
                                    && !modifiedTokenIdxs.contains(tIdx)) {
                                occupiedTokenIdxs.put(tIdx - 2, synonym);

                                modifiedTokenIdxs.add(tIdx - 2);
                                modifiedTokenIdxs.add(tIdx - 1);
                                modifiedTokenIdxs.add(tIdx);

                                addToMap(trigram, synonym, synrepToFreqMap);
                                numReplacementsMade++;
                            }
                        }
                    }
                }

                // Replace all bigrams.
                if (tokens.length > 1) {
                    for (int tIdx = 1; tIdx < tokens.length; tIdx++) {
                        String first = tokens[tIdx - 1];
                        String second = tokens[tIdx];
                        String bigram = first + "_" + second;

                        if (synonymPairs.containsKey(bigram)
                                && !(forbiddenIndices.contains(tIdx - 1)
                                || forbiddenIndices.contains(tIdx))) {
                            // Replace by synonym and update modified tok indices.
                            ArrayList<String> synonymList = synonymPairs.get(bigram);
                            // Randomnly sample one.
                            String synonym = getRandomSynonym(synonymList);
                            synonym = synonym.replace("_", " ");
                            if (!modifiedTokenIdxs.contains(tIdx - 1)
                                    && !modifiedTokenIdxs.contains(tIdx)) {
                                occupiedTokenIdxs.put(tIdx - 1, synonym);

                                modifiedTokenIdxs.add(tIdx - 1);
                                modifiedTokenIdxs.add(tIdx);
                                addToMap(bigram, synonym, synrepToFreqMap);
                                numReplacementsMade++;
                            }
                        }
                    }
                }

                // Replace all unigrams.
                if (tokens.length > 0) {
                    for (int tIdx = 0; tIdx < tokens.length; tIdx++) {
                        String unigram = tokens[tIdx];

                        if (synonymPairs.containsKey(unigram) && !(forbiddenIndices.contains(tIdx))) {
                            // Replace by synonym and update modified tok indices.
                            ArrayList<String> synonymList = synonymPairs.get(unigram);
                            // Randomnly sample one!
                            String synonym = getRandomSynonym(synonymList);
                            synonym = synonym.replace("_", " ");
                            if (!modifiedTokenIdxs.contains(tIdx)) {
                                occupiedTokenIdxs.put(tIdx, synonym);

                                modifiedTokenIdxs.add(tIdx);
                                addToMap(unigram, synonym, synrepToFreqMap);
                                numReplacementsMade++;
                            }
                        }
                    }
                }

                
                if (occupiedTokenIdxs.size() == 0) {
                    numSentencesWOModificationsMade++;
                } else {
                    numSentencesWithModificationMade++;
                }
                
                // Reprint the rephrased sentence.
                for (int tIdx = 0; tIdx < tokens.length; tIdx++) {
                    if (modifiedTokenIdxs.contains(tIdx)) {
                        // Get the replacement.
                        if (occupiedTokenIdxs.containsKey(tIdx)) {
                            //aSynonymReplacedSentence.add("[" + occupiedTokenIdxs.get(tIdx) + "]");
                            //aSynonymReplacedSentence.add("<b>" + occupiedTokenIdxs.get(tIdx) + "</b>");
                            aSynonymReplacedSentence.add(occupiedTokenIdxs.get(tIdx));
                        }
                    } else {
                        aSynonymReplacedSentence.add(tokens[tIdx]);
                    }
                }

                sb.append("<div class=\"tooltip\">");
                // add replaced tokens.
                
                String[] syn = aSynonymReplacedSentence.toString()
                        .replace("[", "")
                        .replace("]", "")
                        .replaceAll("NORM-CR-\\d+", "")
                        .replaceAll(",\\s", " ")
                        .replaceAll(", ", " ")
                        .replace("~CR", "")
                        .replace("~", "")
                        .replace("-RRB-", "")
                        .replace("-RSB-", "")
                        .replace("-LSB-", "")
                        .replace("-RRB-", "")
                        .replace("-LRB-", "")
                        .replace("(", "")
                        .replace(")", "")
                        .replace("[", "")
                        .replace("]", "")
                        .replaceAll("\\d+", " ")
                        .replace(".", "")
                        .replaceAll("\\s+", " ").split("\\s");

                String[] orig = originalSentenceLine.replace("<span class=\"tooltiptext\">", "")
                        .replace("</span></div><br>", "")
                        .replaceAll("~ CR\\d+ ~", "")
                        .replaceAll("~CR\\d ~", "")
                        .replaceAll("~-RSB-", "")
                        .replace("-RSB-", "")
                        .replace("-RRB-", ")")
                        .replace("[", "")
                        .replaceAll(", ", " ")
                        .replace("~CR", "")
                        .replace("~", "")
                        .replace("-RSB-", "")
                        .replace("-LSB-", "")
                        .replace("-RRB-", "")
                        .replace("-LRB-", "")
                        .replace("(", "")
                        .replace(")", "")
                        .replace("[", "")
                        .replace("]", "")
                        .replaceAll("\\d+", " ")
                        .replace(".", "")
                        .replaceAll("\\s+", " ")
                        .replace("</span></div>", "").split("\\s");

                boolean isModified = false;

                if (syn.length == orig.length) {
                    for (int i = 0; i < syn.length; i++) {
                        if (syn[i].equalsIgnoreCase(orig[i])) {

                        } else {
                            isModified = true;
                            break;
                        }
                    }
                } else {
                    isModified = true;
                }

                if (isModified) {
                    numModifiedSentences++;
                } else {
                    numUnmodifiedSentences++;
                }

                
                if (!isModified) {
                    sb.append("\"");
                }
                for (int i = 0; i < aSynonymReplacedSentence.size(); i++) {
                    sb.append(aSynonymReplacedSentence.get(i) + " ");
                }
                if (!isModified) {
                    sb.append("\"");
                }
                sb.append(doiLine + "\n");
                sb.append(originalSentenceLine + "\n");

            }
        }

        s.close();
        // Export new html with synonyms replaced.
        PrintWriter w = new PrintWriter(new File(htmlFile.getParent() + "/"
                + synonymOutputHTML));
        w.write(sb.toString().replace("<html>", "").replace("</html>", ""));

        w.flush();
        w.close();

    }

    private static void initializeSynonymPairs(boolean usePrecomputedSyns) throws FileNotFoundException {
        String pth = null;
        try {
            pth = BySynonymReplacer.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException ex) {
            Logger.getLogger(BySynonymReplacer.class.getName()).log(Level.SEVERE, null, ex);
        }
        String rsc = "resources/precomputed-synonyms.txt";
        String precomputedSynonymsFile = null;
        // Local project.
        if (pth.contains("build")) {
            precomputedSynonymsFile = pth + "../../" + rsc;
        } // jar file.
        else if (pth.contains("dist") && pth.endsWith(".jar")) {
            precomputedSynonymsFile = pth.substring(0, pth.lastIndexOf("/")) + "/../" + rsc;
        }
        
        synonymPairsPrecomputed = getPrecomputedSynonyms(precomputedSynonymsFile);

        if (usePrecomputedSyns) {
            synonymPairs = synonymPairsPrecomputed;
        } else {
            String ngramStatsFile = DIR + "/ngrams_stats.txt";
            synonymPairsW2V = getWord2VecSynonyms(ngramStatsFile, synonymPairsPrecomputed);
            synonymPairs = synonymPairsW2V;
        }

    }

    private static String getRandomSynonym(ArrayList<String> synonymList) {
        Random random = new Random();
        int randIdx = random.nextInt(synonymList.size());
        return synonymList.get(randIdx);
    }

    private static TreeSet<Integer> getForbiddenIndicesForQuotes(String[] tokens) {
        TreeSet<Integer> forbiddenIndices = new TreeSet<>();
        StringBuilder sb = new StringBuilder();
        for (int tIdx = 0; tIdx < tokens.length; tIdx++) {
            String t = tokens[tIdx];
            sb.append(t + " ");
        }
        String sentenceStr = sb.toString().trim();

        for (int tIdx = 0; tIdx < tokens.length; tIdx++) {
            String t = tokens[tIdx];
            if (t.equals("``")) {
                for (int tIdx2 = tIdx; tIdx2 < tokens.length; tIdx2++) {
                    String t2 = tokens[tIdx2];
                    if (t2.equals("''")) {
                        // Found the end of a quote.
                        break;
                    } else {
                        forbiddenIndices.add(tIdx2);
                    }
                }
            }

            if (t.equals("`")) {
                for (int tIdx2 = tIdx; tIdx2 < tokens.length; tIdx2++) {
                    String t2 = tokens[tIdx2];
                    if (t2.equals("'")) {
                        // Found the end of a quote.
                        break;
                    } else {
                        forbiddenIndices.add(tIdx2);
                    }
                }
            }
        }

        if (forbiddenIndices.size() == 0) {
            //System.out.println("Found no quotes for sentence:");
            //System.out.println(sentenceStr);
        } else {
            //System.out.println("Found a quote in sentence:");
            //System.out.println(sentenceStr);
            //System.out.println(forbiddenIndices);
        }

        //System.out.println(sb.toString());
        return forbiddenIndices;
    }
}
