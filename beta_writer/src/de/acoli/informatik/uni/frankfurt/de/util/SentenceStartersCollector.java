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
package de.acoli.informatik.uni.frankfurt.de.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 *
 * Collect frequency sorted list of sentence-initial phrases.
 *
 * @author niko
 */
public class SentenceStartersCollector {

    // one sentence per line.
    public static final String INPUT_FILE = "ospl.txt";

    public static void main(String[] args) throws FileNotFoundException {

        TreeMap<String, Integer> m = new TreeMap<>();
        int sentences = 0;
        Scanner s = new Scanner(new File(INPUT_FILE));
        while (s.hasNextLine()) {
            sentences++;
            String aLine = s.nextLine().trim();
            String[] split = aLine.split("\\s+");
            if (split[0].endsWith(",")) {
                String item = split[0].substring(0, split[0].length() - 1);
                addToMap(item, m);
            }

            if (split.length > 1) {
                if (split[1].endsWith(",")) {
                    String item = split[0] + " " + split[1].substring(0, split[1].length() - 1);
                    addToMap(item, m);
                }
            }

            if (split.length > 2) {
                if (split[2].endsWith(",")) {
                    String item = split[0] + " " + split[1] + " " + split[2].substring(0, split[2].length() - 1);
                    addToMap(item, m);
                }
            }

            if (split.length > 3) {
                if (split[3].endsWith(",")) {
                    String item = split[0] + " " + split[1] + " " + split[2] + " " + split[3].substring(0, split[3].length() - 1);
                    addToMap(item, m);
                }
            }

            if (split.length > 4) {
                if (split[4].endsWith(",")) {
                    String item = split[0] + " " + split[1] + " " + split[2] + " " + split[3] + " " + split[4].substring(0, split[4].length() - 1);
                    addToMap(item, m);
                }
            }

            if (split.length > 5) {
                if (split[5].endsWith(",")) {
                    String item = split[0] + " " + split[1] + " " + split[2] + " " + split[3] + " " + split[4] + " " + split[5].substring(0, split[5].length() - 1);
                    addToMap(item, m);
                }
            }
        }
        s.close();
        System.out.println(sentences + " sentences.");

        Map<String, Integer> sorted = MapUtil.sortByValue(m);

        ArrayList<String> items = new ArrayList<String>();
        for (String word : sorted.keySet()) {
            if (m.get(word) >= 5) {
                items.add(word); // + ": " + m.get(word));
            }
        }

        for (int i = items.size() - 1; i >= 0; i--) {
            System.out.println(items.get(i));
        }
    }

    public static void addToMap(String item, TreeMap<String, Integer> m) {

        if (m.containsKey(item)) {
            // Get old frequency.
            int oldFreq = m.get(item);
            // increment.
            oldFreq++;
            m.put(item, oldFreq);
        } else {
            m.put(item, 1);
        }
    }

    public static Map<String, Integer> getSentenceStarters(String filePath) throws FileNotFoundException {
        //TreeSet<String> rval = new TreeSet<>();
        Scanner s = new Scanner(new File(filePath));
        while (s.hasNextLine()) {
            String aLine = s.nextLine().trim();
            if (!aLine.startsWith("#")) {
                treeMap.put(aLine, aLine.length());
            }
        }
        s.close();
        System.err.println("Read in " + treeMap.size() + " sentence starter phrases.");
        return treeMap;
    }

    static Map<String, Integer> treeMap = new TreeMap<String, Integer>(
            new Comparator<String>() {
        @Override
        public int compare(String s1, String s2) {
            if (s1.length() > s2.length()) {
                return -1;
            } else if (s1.length() < s2.length()) {
                return 1;
            } else {
                return s1.compareTo(s2);
            }
        }
    });

}
