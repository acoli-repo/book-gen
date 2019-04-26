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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 *
 * Reads in chapter structure produced by mkstructure_html.py and links it to the
 * corresponding A++ files (ids).
 *
 * e.g., 1.1.1.: 1 1.1.2.: 28 1.1.3.: 37
 *
 * @author niko
 */
public class ChapterStructureReader {

    // Chapter structure as produced by mkstructure.py
    public static final String CHAPTER_STRUCTURE = "gen/chap-struc.html";

    private static LinkedHashMap<String, String> assignments;

    public static void main(String[] args) throws IOException {
        assignments = getSectionToDocAssignments(CHAPTER_STRUCTURE);
        System.out.println(assignments);
        System.out.println(getNumChapters());
    }

    public static LinkedHashMap<String, String> getSectionToDocAssignments(String chapterStructureFile) throws FileNotFoundException {

        LinkedHashMap<String, String> sectionnumToDocid = new LinkedHashMap<>();
        // Read in subsections.
        Scanner s = new Scanner(new File(chapterStructureFile));
        while (s.hasNextLine()) {
            String aLine = s.nextLine().replace("<button class=\"collapsible\">", "").trim();
            if (aLine.contains("doc:")) {
                String sectionnum = aLine.substring(aLine.indexOf(">") + 1, aLine.indexOf(" ")).trim();
                String docid = aLine.substring(aLine.indexOf("doc:") + 4, aLine.indexOf("/rel:"));
                sectionnumToDocid.put(sectionnum, docid);
            }
        }
        s.close();
        assignments = sectionnumToDocid;
        return assignments;
    }

    public static int getNumChapters() {
        int rval = -1;
        for (String section : assignments.keySet()) {
            rval = Integer.parseInt(section.substring(0, section.indexOf(".")));
        }
        return rval;
    }

}
