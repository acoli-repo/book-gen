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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static de.acoli.informatik.uni.frankfurt.de.util.Utility.listAPlusPlusFilesForFolder;

/**
 *
 * @author niko
 */
public class APlusPlusToBibunstrCleaner {
    
    public static final String INPUT_DIR = "in/";
    public static final String OUTPUT_DIR = "out/";
    
    public static void main(String[] args) throws FileNotFoundException {

        ArrayList<String> files = new ArrayList<String>();
        final File folder = new File(INPUT_DIR);
        listAPlusPlusFilesForFolder(folder, files, 2000);

        for (String f : files) {
            System.out.println("Analyzing: " + f);
            Scanner s = new Scanner(new File(f));
            StringBuilder sb = new StringBuilder();
            while (s.hasNextLine()) {
                String aLine = s.nextLine();
                sb.append(aLine + "\n");
                if (aLine.contains("<BibUnstructured>") && !aLine.contains("</BibUnstructured>")) {      
                }

                if (aLine.contains("</BibUnstructured>") && !aLine.contains("<BibUnstructured>")) {       
                }
            }
            s.close();

            String pattern = "(?s)<BibUnstructured>.*?</BibUnstructured>";
            Pattern r = Pattern.compile(pattern);
            String documentstring = sb.toString();
            Matcher m = r.matcher(documentstring);
            while (m.find()) {
                String match = m.group();
                String norm = match.replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("\\s+", " ").replaceAll("\\t", " ");
                norm = norm.replace("<Subscript>", "<sub>");
                norm = norm.replace("</Subscript>", "</sub>");
                norm = norm.replace("<Superscript>", "<sup>");
                norm = norm.replace("</Superscript>", "</sup>");
                documentstring = documentstring.replace(match, norm);
            }
            PrintWriter w = new PrintWriter(new File(OUTPUT_DIR
                    + f.substring(f.lastIndexOf("/"))));
            w.write(documentstring);
            w.flush();
            w.close();
        }
    }

}
