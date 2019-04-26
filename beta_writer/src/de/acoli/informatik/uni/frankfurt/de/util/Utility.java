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

import de.acoli.informatik.uni.frankfurt.de.aplusplus.Publication;
import static de.acoli.informatik.uni.frankfurt.de.util.SentenceSplitter.BRACKET_INFO_IDX;
import static de.acoli.informatik.uni.frankfurt.de.util.SentenceSplitter.WORD_IDX;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import de.acoli.informatik.uni.frankfurt.de.reader.SentIDCounter;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Utility class for A++ files and I/O handling.
 *
 * @author niko
 */
public class Utility {

    private static SentenceSplitter spl;
    public static final String TEXT_SEPARATOR = "</separator>";
    public static final String CITATION_MARKER = "~";

    public static void listAPlusPlusFilesForFolder(final File folder, ArrayList<String> files, int maxfiles) {

        if (folder.listFiles() == null) {
            System.err.println("Sorry, please verify that this A++ folder exists: " + folder);
            System.exit(0);
        }
        for (final File fileEntry : folder.listFiles()) {

            if (files.size() >= maxfiles) {

                return;
            }

            if (fileEntry.isDirectory()) {
                listAPlusPlusFilesForFolder(fileEntry, files, maxfiles);
            } else {
                //if (fileEntry.getAbsolutePath().endsWith("_masked.xml")) {
                if (fileEntry.getAbsolutePath().endsWith(".xml")) {
                    if (files.contains(fileEntry.getAbsolutePath())) {
                        System.err.println("file: " + fileEntry + " already present.");
                    }
                    files.add(fileEntry.getAbsolutePath());
                }
            }
        }
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String turnListOfSentencesIntoTextForSummary(ArrayList<ArrayList<String>> sentences) {
        StringBuilder lines = new StringBuilder();
        for (ArrayList<String> sentence : sentences) {
            String line = turnSingleSentenceIntoText(sentence);
            lines.append(line);
        }
        String rval = lines.toString().trim();
        return rval;
    }

    public static String turnSingleSentenceIntoText(ArrayList<String> sentence) {
        StringBuilder line = new StringBuilder();
        for (String tokAnno : sentence) {
            String token = tokAnno.split("\\|")[WORD_IDX];
            token = expressBrackets(token);
            // Only add this token if it is not part of a "bracket content" which 
            // has to be removed before the summary.
            String bracket = tokAnno.split("\\|")[BRACKET_INFO_IDX];
            if (!("b".equals(bracket))) {
                line.append(token + " ");
            } else {

            }
        }
        String lineStr = line.toString().trim() + "\n";
        return lineStr;
    }

    /**
     *
     * @param l
     * @return
     */
    public static boolean isNarrativeSentence(String l) {
        boolean ok = false;
        l = l.trim().replace("\n", " ");
        // Filter single words, footnotes, urls.
        boolean isURL = Pattern.matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", l);
        int numWords = l.split("\\s+").length;
        if (l.length() > 1
                && !isNumeric(String.valueOf(l.charAt(0)))
                && !l.toLowerCase().contains("sections")
                && !l.toLowerCase().contains("figure")
                && !l.contains("Table")
                && !l.contains("Tables")
                && !l.toLowerCase().contains(" table")
                && !l.toLowerCase().contains("fig.")
                && !l.toLowerCase().contains("figs.")
                && !l.toLowerCase().contains("tabs.")
                && !l.toLowerCase().contains("tab.")
                && !l.toLowerCase().contains("sect.")
                && !l.toLowerCase().contains("sects.")
                && !isURL
                && numWords > 4
                && !l.toLowerCase().endsWith(", .")
                && !l.toLowerCase().endsWith("eqs.")
                && !l.toLowerCase().endsWith("eq.")
                && !l.toLowerCase().endsWith(" .")
                && !l.startsWith("$")
                && !l.startsWith("|")
                && !l.startsWith("+")
                && !l.startsWith("*")
                && !l.startsWith("In Fig")
                && !l.startsWith("In Tab")
                && !l.contains("\\math")
                && !l.contains("\\end")
                && !l.contains("\\begin")
                && !l.contains("\\frac")
                && !l.contains("$\\")
                && !l.startsWith("%")
                && !l.startsWith("{")
                && !l.startsWith("=")
                && !l.startsWith(";")
                && !l.startsWith(".")
                && !l.startsWith(":")
                && !l.startsWith("-")
                && !l.startsWith("\\")
                && !l.startsWith("(")
                && !l.startsWith(")")
                && !l.startsWith("[")
                && !l.startsWith("]")
                && !l.startsWith(",")
                && !Character.isLowerCase(l.charAt(0))
                && !l.contains("$$")
                && !l.contains("www.")) {
            ok = true;
        }
        return ok;
    }

    public static ArrayList<ArrayList<String>> getSentencesTokensAndAnnotations(Node node, Publication aPub, SentIDCounter sic) {
        String sectionContent = node.getTextContent().replaceAll("\\s+", " ").replaceAll("\n", " ").replace(" , ", ", ").trim();

        boolean isHeadline = false;
        boolean isAbstract = false;
        if (node.getNodeName().equals("Heading") || node.getNodeName().contains("Title")) {
            isHeadline = true;
        }

        // "para" is abstract.
        if (node.getNodeName().contains("Para")) {
            isAbstract = true;
        }

        if (!isHeadline) {

        } // Remove XML content / XML stuff from headline.
        else {
            removeRecursively(node, Node.ELEMENT_NODE, "Primary");
            sectionContent = node.getTextContent().replaceAll("<[^>]+>", "");
        }

        // Stanford sentence splitting.
        if (spl == null) {
            try {
                spl = new SentenceSplitter();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        ArrayList<ArrayList<String>> sentsAndToks = spl.getSentencesAndTokens(sectionContent, isHeadline, isAbstract, aPub, sic);
        return sentsAndToks;
    }

    public static void removeSelectedTags(Node node) {
        // and remove it from 
        removeRecursively(node, Node.ELEMENT_NODE, "Heading");
        // Remove all equations and tables.
        removeRecursively(node, Node.ELEMENT_NODE, "Equation");
        removeRecursively(node, Node.ELEMENT_NODE, "InlineEquation");
        removeRecursively(node, Node.ELEMENT_NODE, "EquationNumber");
        removeRecursively(node, Node.ELEMENT_NODE, "EquationSource");
        // remove citation references!
        //removeRecursively(node, Node.ELEMENT_NODE, "CitationRef"); //
        markCitationReferences(node, Node.ELEMENT_NODE, "CitationRef");
        removeRecursively(node, Node.ELEMENT_NODE, "Term");
        removeRecursively(node, Node.ELEMENT_NODE, "Primary");
        removeRecursively(node, Node.ELEMENT_NODE, "Secondary");
        removeRecursively(node, Node.ELEMENT_NODE, "InternalRef");

        removeRecursively(node, Node.ELEMENT_NODE, "Figure");
        removeRecursively(node, Node.ELEMENT_NODE, "Table");
        removeRecursively(node, Node.ELEMENT_NODE, "Caption");
    }

    public static void removeRecursively(Node node, short nodeType, String name) {
        if (node.getNodeType() == nodeType && (name == null || node.getNodeName().equals(name))) {
            //System.out.println("removing: " + node.getTextContent());
            Node parent = node.getParentNode();
            if (parent != null) {
                parent.removeChild(node);
            }
        } else {
            // check the children recursively
            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                removeRecursively(list.item(i), nodeType, name);
            }
        }
    }

    public static void markCitationReferences(Node node, short nodeType, String name) {
        if (node.getNodeType() == nodeType && (name == null || node.getNodeName().equals(name))) {

            Node parent = node.getParentNode();
            if (parent != null) {
                Element el = (Element) node;
                node.setTextContent(
                        //el.getTextContent() + 
                        CITATION_MARKER + el.getAttribute("CitationID") + CITATION_MARKER);
                //node.setTextContent(el.getAttribute("CitationID"));
            }
        } else {
            // check the children recursively
            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                markCitationReferences(list.item(i), nodeType, name);
            }
        }
    }

    public static void writeToDisk(String what, String where) throws FileNotFoundException {
        PrintWriter w = new PrintWriter(new File(where));
        w.write(what);
        w.flush();
        w.close();
    }

    public static void printClusterDist(ArrayList<Publication> pubs) {
        TreeMap<String, Integer> clusterDist = new TreeMap<>();
        for (Publication pub : pubs) {
            String aCluster = pub.getCluster();
            if (clusterDist.containsKey(aCluster)) {
                int oldFreq = clusterDist.get(aCluster);
                oldFreq++;
                clusterDist.put(aCluster, oldFreq);
            } else {
                clusterDist.put(aCluster, 1);
            }
        }
        System.out.println("Topic cluster distribution of A++ documents: " + clusterDist + "\n\n");
    }

    public static String expressBrackets(String token) {
        switch (token) {
            case "-LSB-":
                token = "[";
                break;
            case "-RSB-":
                token = "]";
                break;
            case "-LRB-":
                token = "(";
                break;
            case "-RRB-":
                token = ")";
                break;
            case "-LCB-":
                token = "{";
                break;
            case "-RCB-":
                token = "}";
                break;
        }
        return token;
    }

    public static String writeTooltipLine(String restructuredWithBracketsReintroducedStrCitsNormalized, String doiOfPaper, String originalSentence, boolean separateByNewline) {
        String newline = "";
        if (separateByNewline) {
            newline = "<br>";
        }
        return "<div class=\"tooltip\">" + restructuredWithBracketsReintroducedStrCitsNormalized + "\n"
                + " [" + "<a href=\"https://doi.org/" + doiOfPaper + "\" target=\"_blank\"/>" + doiOfPaper + "</a>" + "]\n"
                + "  <span class=\"tooltiptext\">" + originalSentence + "</span></div>"
                + newline
                + "\n";
    }
    
    
    public static ArrayList<String> getExtendedAbstractPhrases() {
        ArrayList<String> rval = new ArrayList<String>();
        String pth = null;
        try {
            pth = Utility.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        String rsc = "resources/extended-abstract-phrases.txt";
        String extendedAbstractPhrasesFile = null;
        // Local netbeans project.
        if (pth.contains("build")) {
            extendedAbstractPhrasesFile = pth + "../../" + rsc;
        } // jar file.
        else if (pth.contains("dist") && pth.endsWith(".jar")) {
            extendedAbstractPhrasesFile = pth.substring(0, pth.lastIndexOf("/")) + "/../" + rsc;
        }
        Scanner s = null;
        try {
            s = new Scanner(new File(extendedAbstractPhrasesFile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(s.hasNextLine()) {
            String aPhrase = s.nextLine().trim();
            rval.add(" " + aPhrase + " ");
        }
        s.close();
        return rval;
    }

    public static String strip(String input) {
        return input.replace(" °", "°")
                .replace("`` ", " “") //opening
                .replace(" '' ", "” ") //closing
                .replace(" − ", "-")
                .replace(" - ", "-")
                .replace(" -, ", "-, ")
                .replace(" ; ", "; ")
                .replace(" : ", ": ")
                .replace(" . ", ". ")
                .replace(" , ", ", ")
                .replace(" .", ". ")
                .replace(" ( ", " (")
                .replace(" ) ", ") ")
                .replace(" [ ", " [")
                .replace(" ] ", "] ")
                .replace(" 's ", "'s ")
                .replace(" ?", "?")
                .replace(" ', ", "', ")
                .replace(". '' ", ".'' ")
                .replace(" '.", "'.")
                .replace(" { ", " {")
                .replace(" } ", "} ")
                .replace(" -- ", "-")
                .replace(" % ", "% ")
                .replace(" %;", "%;")
                .replace("` ", " '")
                .replace("~ ", "~")
                .replace(" ' ", "' ")
                .replace(". .", ".")
                .replace(", .", ".")
                .replace(" ,”", ",\"")
                .replace(" ,; ", "; ")
                .replace(", , ", ", ")
                // TODO: make nicer.
                .replace(" -LRB- ", " (")
                .replace(" -RRB- ", ") ")
                .replace("--RRB-", "-) ")
                .replace(" -RRB--", ")- ")
                .replace(" -RRB-. ", "). ")
                .replace(" -RRB-., ", ")., ")
                .replace(" -RRB-: ", "): ")
                .replace(" -- ", "–")
                .replace(" -RRB-; ", "); ")
                .replace(" -RRB-, ", "), ")
                .replace(" -RRB-'", ")'")
                .replace(" -RRB-”", ")")
                .replace(" @", "@")
                .replace(" -LSB- ", " [")
                .replace("-LSB-. ", "[.")
                .replace("(-RRB-", "()")
                .replace(" -RSB- ", "] ")
                .replace(" -RSB-. ", "]. ")
                .replace(" -RSB-, ", "], ")
                .replace(" -LCB- ", " {")
                .replace(" -RCB- ", "} ")
                .replace(" ,: ", ",: ")
                .replace(",.", ".")
                .replace(" ); ", "); ")
                .replace(" ).", ").")
                .replace("[[", "[").replace("]]", "]")
                .replace(". ”", ".\"")
                // bugs.
                .replace("] -RSB-)", "])")
                .replace("] -RSB-;", "];")
                .replace(" -RRB-", ")")
                .replace("-LRB--", "(-")
                .replace(";, ", "; ")
                .replace(" -RSB-: ", ": ")
                .replace(" -RSB-:, ", ":, ")
                .replace(" -RSB-”", "\"")
                .replace("--LSB- ", "-[")
                .replace("--LRB- ", "(")
                .replace(" -RSB--", "]-")
                .replace(" . ", ". ") // sentence ending in front of DOI.

                .replace("()", "")
                .replace(",)", ")")
                .replace("..", ".")
                .replace(";.", ";")
                .replace(" ,,", " ")
                .replace("∼ ", "∼")
                .replace("- , ", "-, ")
                .replace("1 , ", "1, ")
                .replace("0 , ", "0, ")
                .replace(" ., ", "., ")
                .replace("] .", "].")
                .replace(" --,", "")
                .replace(" )", ")")
                .replace(" ]", "]")
                .replace(" ,", ",")
                .replace(". ; ", "; ")
                .replace("; ; ", "; ")
                .replace(" .", ".")
                .replace("- ", "-")
                .replace(" --", "")
                .replace(". \"[<", ".\" [<") // Remove space in literal quotes.
                // determiners with vowels!
                .replace(" a a", " an a")
                .replace(" a e", " an e")
                .replace(" a i", " an i")
                .replace(" a o", " an o");
    }
    
}
