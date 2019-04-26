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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author niko
 */
public class NgramsSimilarityComputer {

    public static boolean verbose = false;
    public static String DIR = "gen";

    public static double SIMILARITY_THRESH = 0.8;
    private static int CORPUS_FREQUENCY = 27;
    private static double SIMILARITY = 0.983;

    public static ArrayList<String> ano;
    public static ArrayList<String> dup;

    static {

        String pth = null;
        try {
            pth = NgramsSimilarityComputer.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException ex) {
            Logger.getLogger(NgramsSimilarityComputer.class.getName()).log(Level.SEVERE, null, ex);
        }
        String rscDup = "resources/antonyms-synonyms.txt";
        String rscAno = "resources/illegal-synonyms.txt";

        String dupFile = null;
        String anoFile = null;

        // Local project.
        if (pth.contains("build")) {
            dupFile = pth + "../../" + rscDup;
            anoFile = pth + "../../" + rscAno;
        } // jar file.
        else if (pth.contains("dist") && pth.endsWith(".jar")) {
            dupFile = pth.substring(0, pth.lastIndexOf("/")) + "/../" + rscDup;
            anoFile = pth.substring(0, pth.lastIndexOf("/")) + "/../" + rscAno;
        }

        dup = new ArrayList<String>();
        try {
            Scanner dupS = new Scanner(new File(dupFile));
            while (dupS.hasNextLine()) {
                String l = dupS.nextLine().trim();
                if (!l.startsWith("#") && l.length() > 0) {
                    dup.add(l);
                }
            }
            dupS.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NgramsSimilarityComputer.class.getName()).log(Level.SEVERE, null, ex);
        }

        ano = new ArrayList<>();
        try {
            Scanner anoS = new Scanner(new File(anoFile));
            while (anoS.hasNextLine()) {
                String l = anoS.nextLine().trim();
                if (!l.startsWith("#") && l.length() > 0) {
                    ano.add(l);
                }
            }
            anoS.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NgramsSimilarityComputer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static TreeMap<String, ArrayList<String>> getPrecomputedSynonyms(String file) throws FileNotFoundException {
        TreeMap<String, ArrayList<String>> rval = new TreeMap<String, ArrayList<String>>();
        Scanner s = new Scanner(new File(file));
        while (s.hasNextLine()) {
            String aLine = s.nextLine().trim();
            if (aLine.contains("#")) {
                continue;
            }
            if (aLine.contains("\t")) {
                String[] wordPlusSyn = aLine.split("\\t");
                String word = wordPlusSyn[0];
                String syn = wordPlusSyn[1];
                if (rval.containsKey(word)) {
                    ArrayList<String> synonymsList = rval.get(word);
                    synonymsList.add(syn);
                    rval.put(word, synonymsList);
                } else {
                    // get old contents.
                    ArrayList<String> synonymsList = new ArrayList<String>();
                    synonymsList.add(syn);
                    rval.put(word, synonymsList);
                }
            }
        }
        s.close();
        return rval;
    }

    public static TreeMap<String, ArrayList<String>> getWord2VecSynonyms(String ngramStatsFile, TreeMap<String, ArrayList<String>> handcollected) throws FileNotFoundException {

        TreeMap<String, ArrayList<String>> rval = new TreeMap<>();
        TreeMap<String, Ngram> ngrams = readWord2VecNgrams(ngramStatsFile);

        int numwordsforwhichwehavesynonyms = 0;

        for (String ngram : ngrams.keySet()) {
            Ngram obj = ngrams.get(ngram);
            int corpusFreq = obj.corpusFrequency;
            ArrayList<HashMap<String, Double>> mostSimilarwords = obj.mostSimilarWords;
            int similarfreq = -1;
            HashMap<String, Double> mostsimilarone = mostSimilarwords.get(0);
            HashMap<String, Double> secondmostsimilarone = mostSimilarwords.get(1);
            HashMap<String, Double> thirdmostsimilarone = mostSimilarwords.get(2);

            String key = "";
            double similarity = 0.0;

            for (String k : mostsimilarone.keySet()) {
                key = k;
                similarity = mostsimilarone.get(key);
                if (ngrams.containsKey(key)) {
                    similarfreq = ngrams.get(key).corpusFrequency;
                }
                if (key.startsWith(ngram) || ngram.startsWith(key) || differInOneCharacterAtSamePos(ngram, key)
                        || !use(ngram, key)) {
                    for (String k2 : secondmostsimilarone.keySet()) {
                        key = k2;
                        similarity = secondmostsimilarone.get(key);
                        if (ngrams.containsKey(key)) {
                            similarfreq = ngrams.get(key).corpusFrequency;
                        } else {
                            similarfreq = -1;
                        }
                        if (key.startsWith(ngram) || ngram.startsWith(key) || differInOneCharacterAtSamePos(ngram, key)
                                || !use(ngram, key)) {
                            for (String k3 : thirdmostsimilarone.keySet()) {
                                key = k3;
                                similarity = thirdmostsimilarone.get(key);
                                if (ngrams.containsKey(key)) {
                                    similarfreq = ngrams.get(key).corpusFrequency;
                                } else {
                                    similarfreq = -1;
                                }
                            }
                        }
                    }
                }
            }

            if (!use(ngram, key)) {

            } else {
                if (similarity > SIMILARITY_THRESH && corpusFreq > CORPUS_FREQUENCY && !(similarity > SIMILARITY)) {
                    if (!ngram.endsWith(key) && !ngram.contains(".") && !key.contains(".") && ngram.length() > 2 && !isallupper(ngram)) {
                        double ratiooffreqs = (double) similarfreq / (double) corpusFreq;
                        if (similarfreq == -1) {
                            ratiooffreqs = 0.0;
                        }

                        if (verbose) {
                            System.out.println(ngram + ": " + key + " (" + similarity + ")   #" + corpusFreq + "/#" + similarfreq + " (" + ratiooffreqs + "%).");
                        }
                        if (!handcollected.containsKey(ngram)) {
                            ArrayList<String> synonyms = new ArrayList<String>();
                            synonyms.add(key);
                            rval.put(ngram, synonyms);
                            numwordsforwhichwehavesynonyms++;
                        }
                    }
                }
            }
        }
        if (verbose) {
            System.out.println("\n# N-grams for which we have (new) synonyms: " + numwordsforwhichwehavesynonyms + "/" + ngrams.size() + " (" + (double) numwordsforwhichwehavesynonyms / (double) ngrams.size() + "%)");
        }
        return rval;
    }

    public static TreeMap<String, Ngram> readWord2VecNgrams(String word2VecNgramfile) throws FileNotFoundException {
        TreeMap<String, Ngram> rval = new TreeMap<>();
        Scanner s = new Scanner(new File(word2VecNgramfile));
        // Skip first.
        s.nextLine();
        while (s.hasNextLine()) {
            String aLine = s.nextLine().trim().replace("\"", "'");

            String[] split = aLine.split("\t");
            String ngram = split[0];
            String corpusFreq = split[1];

            Ngram nGram = new Ngram();
            nGram.ngram = ngram;
            nGram.corpusFrequency = Integer.parseInt(corpusFreq);

            ArrayList<HashMap<String, Double>> mostsimilarwords = new ArrayList<HashMap<String, Double>>();

            String mostSimilarStr = split[2].replace("[(", "").replace("]", "");
            String[] mostSim = mostSimilarStr.split(", \\(");
            for (String mS : mostSim) {
                String tuple = mS.substring(1, mS.length() - 1);
                String[] tupleIts = tuple.split("', ");
                String aSimilarWord = tupleIts[0];
                double itsSimilarity = Double.parseDouble(tupleIts[1]);
                HashMap<String, Double> anIt = new HashMap<String, Double>();
                anIt.put(aSimilarWord, itsSimilarity);
                mostsimilarwords.add(anIt);
            }
            nGram.mostSimilarWords = mostsimilarwords;
            rval.put(ngram, nGram);

        }
        s.close();
        return rval;
    }

    private static boolean isallupper(String ngram) {
        boolean rval = true;
        for (int i = 0; i < ngram.length(); i++) {
            if (!Character.isUpperCase(ngram.charAt(i))) {
                rval = false;
                break;
            }
        }
        return rval;
    }

    private static boolean differInOneCharacterAtSamePos(String word1, String word2) {
        boolean differInOneChar = false;
        int howmanydiffchars = 0;
        if (word1.length() == word2.length()) {
            for (int cIdx = 0; cIdx < word1.length(); cIdx++) {
                if (word1.charAt(cIdx) != word2.charAt(cIdx)) {
                    howmanydiffchars++;
                }
            }
        }
        if (howmanydiffchars == 1) {
            differInOneChar = true;
        }
        return differInOneChar;
    }

    public static boolean use(String word, String synonym) {
        String SEP = ": ";
        boolean use = true;
        if (dup.contains(word + SEP + synonym)
                || dup.contains(synonym + SEP + word)
                || dup.contains(word.toLowerCase() + SEP + synonym.toLowerCase())
                || dup.contains(synonym.toLowerCase() + SEP + word.toLowerCase())
                || dup.contains(word.toLowerCase() + SEP + synonym)
                || dup.contains(word + SEP + synonym.toLowerCase())
                || dup.contains(synonym.toLowerCase() + SEP + word)
                || dup.contains(synonym + SEP + word.toLowerCase())) {
            use = false;
        }
        if (ano.contains(synonym) || ano.contains(word)
                || ano.contains(synonym.toLowerCase()) || ano.contains(word.toLowerCase())) {
            use = false;
        }
        return use;
    }
}
