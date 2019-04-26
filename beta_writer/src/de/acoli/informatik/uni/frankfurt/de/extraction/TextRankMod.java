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
package de.acoli.informatik.uni.frankfurt.de.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import de.acoli.informatik.uni.frankfurt.de.util.StreamGobbler;
import static de.acoli.informatik.uni.frankfurt.de.util.Utility.TEXT_SEPARATOR;

/**
 *
 * TextRank, automatic text summarization and keyword extraction. Available at:
 * https://github.com/summanlp/textrank
 *
 * This class implements a Java wrapper for the python script. Text rank is
 * slightly modified to accept TOKENIZED SENTENCES as input.
 *
 * @author niko
 */
public class TextRankMod {

    public static String SUMMARIZER = "textrank/summ_and_keywords.py";
    public static String pythonPath = "/usr/local/bin/python3";

    private String toSummarize;
    private ArrayList<String> keywords;
    private String summary;

    public void setSummarizer(String path) {
        SUMMARIZER = path;
    }

    public TextRankMod(String aTextToSummarize) {
        toSummarize = aTextToSummarize;
    }

    public void setPythonPath(String aPythonPath) {
        pythonPath = aPythonPath;
    }

    public static void main(String[] args) throws IOException {

        String toSummarize = "The automatic summarization is the process of reducing "
                + "a text document with a computer program in order to create a summary "
                + "that retains the most important points of the original document .\n "
                + "As the problem of information overload has grown , and as the quantity "
                + "of data has increased , so has interest in automatic summarization .\n "
                + "Technologies that can make a coherent summary take into account variables "
                + "such as length , writing style and syntax .\n An example of the use of summarization "
                + "technology is search engines such as Google .\n Document summarization is another .\n".trim();

        TextRankMod tr = new TextRankMod(toSummarize);
        // Summarize with threshold.
        tr.textRank("0.9", "ratio");
        System.out.println("Summary: " + tr.getSummary());
        System.out.println("Keywords: ");
        for (String k : tr.getKeywords()) {
            System.out.println("\t" + k);
        }
        System.out.println();

        tr.textRank("20", "words");
        System.out.println("Summary: " + tr.getSummary());
        System.out.println("Keywords: ");
        for (String k : tr.getKeywords()) {
            System.out.println("\t" + k);
        }

    }

    public String getSummary() {
        return summary;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    /**
     *
     * @param summaryThreshold
     * @param type, one of "ratio" or "words"
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void textRank(String summaryThreshold, String type) throws FileNotFoundException, IOException {

        ProcessBuilder pb = new ProcessBuilder(
                pythonPath,
                SUMMARIZER,
                toSummarize,
                summaryThreshold,
                type // words or ratio.
        );
        pb.directory(new File("./"));
        Process p = pb.start();
        String aLine = "";
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder();
        while ((aLine = input.readLine()) != null) {
            aLine = aLine.trim();
            sb.append(aLine + "\n");
            // pw.write(aLine.trim() + "\n");
        }
        input.close();

        StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
        errorGobbler.start();

        //  pw.flush();
        //  pw.close();
        String[] keywordarr = sb.toString().split(TEXT_SEPARATOR)[0].split("\n");
        keywords = new ArrayList<>(Arrays.asList(keywordarr));
        summary = sb.toString().split(TEXT_SEPARATOR)[1];

    }
}
