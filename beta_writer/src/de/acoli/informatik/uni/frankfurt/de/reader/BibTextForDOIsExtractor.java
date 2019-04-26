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

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.output.Bibliography;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;

/**
 *
 * @author niko
 */
public class BibTextForDOIsExtractor {

    public static final String BIBTEX_TO_DOI_SCRIPT = "doi_to_bibtex.sh";

    public static void main(String[] args) throws IOException, ParseException {

        // 1. get all dois.
        // 2. generate bibtex for each doi.
        // 3. save them in list and write them to a bibliography .bib file.
        // 4. convert whole .bib file to html and export.
        String doi = "10.1021/cm901452z";
        String bibtex = getBibTexForDoi(doi);
        System.out.println(bibtex);
        String doiNA = "10.1021/NA";
        String bibtexNA = getBibTexForDoi(doiNA);
        System.out.println(bibtexNA);

        PrintWriter w = new PrintWriter(new File("mybib.bib"));
        w.write(bibtex + "\n");
        w.write(bibtexNA);
        w.flush();
        w.close();

        BibTeXDatabase db = new BibTeXConverter().loadDatabase(
                new FileInputStream("mybib.bib"));
        BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
        provider.addDatabase(db);
        CSL citeproc = new CSL(provider, "ieee");
        citeproc.setOutputFormat("html");
        provider.registerCitationItems(citeproc);
        Bibliography bibl = citeproc.makeBibliography();
        for (String entry : bibl.getEntries()) {
            System.out.println(entry);
        }
    }

    public static String getBibliographyHTMLforBibtexfile(String pathToBibtexfile) throws IOException, ParseException {
        BibTeXDatabase db = new BibTeXConverter().loadDatabase(
                new FileInputStream(pathToBibtexfile));
        BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
        provider.addDatabase(db);
        CSL citeproc = new CSL(provider, "ieee");
        citeproc.setOutputFormat("html");
        provider.registerCitationItems(citeproc);
        Bibliography bibl = citeproc.makeBibliography();
        StringBuilder sb = new StringBuilder();
        for (String entry : bibl.getEntries()) {
            //System.err.println(entry);
            sb.append(entry + "<br>");
        }
        return sb.toString();
    }

    public static void writeBibtexEntriesToFile(ArrayList<String> bibtexs, String bibliographyfileName) throws FileNotFoundException {
        PrintWriter w = new PrintWriter(new File(bibliographyfileName));
        int i = 1;
        for (String bibtex : bibtexs) {
            w.write("% bibtex entry " + i + "\n" + bibtex + "\n\n");
            i++;
        }
        w.flush();
        w.close();
    }

    /**
     * Get a bibtex entry string for a DOI.
     *
     * @param aDoi
     * @return the bibtex entry in string format
     * @throws IOException
     */
    public static String getBibTexForDoi(String aDoi) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "sh",
                BIBTEX_TO_DOI_SCRIPT,
                aDoi
        );
        pb.directory(new File("./"));
        Process p = pb.start();
        String aLine = "";
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder();
        while ((aLine = input.readLine()) != null) {
            aLine = aLine.trim();
            //System.out.println(aLine);
            sb.append(aLine + "\n");
            // pw.write(aLine.trim() + "\n");
        }
        input.close();
        //System.out.println(sb.toString());
        String rval = sb.toString().trim();
        rval = rval.replace("$\\", "");
        rval = rval.replace("$", "");

        if (rval.length() == 0 || rval.contains("DOI Not Found")) {
            int randomNumber = getRandomNumberInRange(1, Integer.MAX_VALUE);

            rval = "@InProceedings{DOI_NA_" + randomNumber + ",\n"
                    + "  title = 	{{No bibtex available for DOI: " + aDoi + "}},\n"
                    + "  doi = 	\"" + aDoi + "\"\n"
                    + "}";
        }
        return rval;
        // Get the errors, if any.
        //StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
        //errorGobbler.start();
    }

    private static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

}
