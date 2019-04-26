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

import static de.acoli.informatik.uni.frankfurt.de.demos.AllSectionSentencesAggregator.sectionTypesToProduce;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * Reads in dependency parsed collection of sentences and copies splits into
 * respective section folders.
 *
 *
 * @author niko
 */
public class ParsedSentencesToFoldersCopier {

    // 
    public static String DIR = "gen/";

    public static String PARSED_SENTENCES_FILE = "chapters/all-section-sentences.parsed.conll";
    public static String IDS_FILE = "chapters/all-section-sentences-ids.txt";

    public static void main(String[] args) throws FileNotFoundException {

        if (args.length == 1) {
            DIR = args[0];
        }

        // Read in .conll parsed sentences.
        ArrayList<ArrayList<String>> parsedSentences = new ArrayList<>();
        ArrayList<String> aSent = new ArrayList<>();
        Scanner parsedSentsScanner = new Scanner(new File(DIR + PARSED_SENTENCES_FILE));
        while (parsedSentsScanner.hasNextLine()) {
            String l = parsedSentsScanner.nextLine().trim();
            if (l.length() == 0) {
                parsedSentences.add(aSent);
                aSent = new ArrayList<>();
            } else {
                aSent.add(l);
            }
        }
        parsedSentsScanner.close();
        System.err.println("Collected " + parsedSentences.size() + " parsed conll sentences.");

        // Cleanup / Delete parsed conll files and ids files first.
        deleteParsedFiles(new File(DIR + "chapters/"));

        Scanner idsS = new Scanner(new File(DIR + IDS_FILE));
        // skip first.
        idsS.nextLine();
        int lineIdx = 0;
        while (idsS.hasNextLine()) {
            String idLine = idsS.nextLine().trim();
            String[] items = idLine.split("\\,");
            int len = items.length;
            String section = items[5];
            String heading = items[len - 1].replace("\"", "");
            int chapNum = Integer.parseInt(section.substring(0, section.indexOf(".")));
            String folder = "-";
            if (sectionTypesToProduce.contains(heading)) {
                folder = heading;
            } else {
                folder = "Papers/" + section;
            }

            String parsedFileName = DIR + "chapters/" + chapNum + "/" + folder + "/" + "section-sentences.parsed.conll";

            // Append parsed sentences.
            PrintWriter parsedWriter = new PrintWriter(new FileOutputStream(
                    new File(parsedFileName),
                    true));
            // Get parsed sentence.
            ArrayList<String> sent = parsedSentences.get(lineIdx);
            for (String l : sent) {
                parsedWriter.write(l + "\n");
            }
            parsedWriter.write("\n"); // separate sentences by newline.

            parsedWriter.flush();
            parsedWriter.close();

            lineIdx++;
        }
        idsS.close();
        System.err.println("Copying parsed .conll slices into folders.");
    }

    public static void deleteParsedFiles(final File folder) {
        if (folder.listFiles() == null) {
            System.err.println("Sorry, please verify that this folder exists: " + folder);
            System.exit(0);
        }
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                deleteParsedFiles(fileEntry);
            } else {
                if (fileEntry.getName().equals("section-sentences.parsed.conll")) {
                    fileEntry.delete();
                }
            }
        }
    }

}
