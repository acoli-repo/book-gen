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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

/**
 *
 * @author niko
 */
public class NgramsExtractor {

    public static String DIR = "gen/";

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 2) {
            DIR = args[1];
        }
        collectAllNgramsFromChapters(DIR, DIR + "/ngrams.txt");
    }

    public static void collectAllNgramsFromChapters(String inputDir, String ngramsOutputFile) throws FileNotFoundException {
        // Get all restructured files.
        ArrayList<String> tokFiles = new ArrayList<String>();
        getRestructuredFiles(new File(DIR + "/chapters/"), tokFiles);

        TreeSet<String> ngrams = new TreeSet<String>();
        for (String f : tokFiles) {
            File file = new File(f);
            Scanner s = new Scanner(file);
            while (s.hasNextLine()) {
                String aLine = s.nextLine();
                // Split into tokens.
                String[] tokens = aLine.split("\\s");
                // Add all unigrams.
                for (String t : tokens) {
                    if (t.length() > 1) {
                        ngrams.add(t);
                    }
                }
                if (tokens.length > 1) {
                    // Extract all bigrams.
                    for (int tIdx = 1; tIdx < tokens.length; tIdx++) {
                        String first = tokens[tIdx - 1];
                        String second = tokens[tIdx];
                        String bigram = first + "_" + second;
                        ngrams.add(bigram);
                    }
                }
                if (tokens.length > 2) {
                    // Extract all trigrams.
                    for (int tIdx = 2; tIdx < tokens.length; tIdx++) {
                        String first = tokens[tIdx - 2];
                        String second = tokens[tIdx - 1];
                        String third = tokens[tIdx];
                        String trigram = first + "_" + second + "_" + third;
                        ngrams.add(trigram);
                    }
                }
            }
            s.close();
        }
        PrintWriter w = new PrintWriter(new File(ngramsOutputFile));
        for (String ngram : ngrams) {
            if (isOkayNgram(ngram)) {
                w.write(ngram + "\n");
            }
        }
        w.flush();
        w.close();

    }

    public static void getRestructuredFiles(final File folder, ArrayList<String> files) {
        if (folder.listFiles() == null) {
            System.err.println("Sorry, please verify that this folder exists: " + folder);
            System.exit(0);
        }
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getRestructuredFiles(fileEntry, files);
            } else {
                if (fileEntry.getAbsolutePath().endsWith("section-sentences-restructured-tok.txt")) {
                    if (files.contains(fileEntry.getAbsolutePath())) {
                        System.err.println("file: " + fileEntry + " already present.");
                    }
                    files.add(fileEntry.getAbsolutePath());
                }
            }
        }
    }

    private static boolean isOkayNgram(String ngram) {
        boolean rval = true;
        if (ngram.contains(",")
                || ngram.contains("!")
                || ngram.contains("?")
                || ngram.contains("'")
                || ngram.contains("$")
                || ngram.contains("%")
                || ngram.startsWith("-")
                || ngram.contains(":")
                || ngram.contains(";")
                || ngram.contains(".")
                || ngram.contains("`")
                || ngram.contains("\"")
                || ngram.contains("&")
                || ngram.contains("/")) {
            return false;
        }
        // contains digit.
        if (ngram.matches(".*\\d+.*")) {
            return false;
        }
        // Remove all upper case ngrams.
        if (ngram.toUpperCase().equals(ngram)) {
            //return false;
        }
        if (ngram.contains("_")) {
            String[] components = ngram.split("\\_");
            for (String comp : components) {
                if (comp.toUpperCase().equals(comp)) {
                    return false;
                }
            }
        }
        if (Character.isUpperCase(ngram.charAt(0))) {
            //return false;   
        }
        return rval;
    }
}
