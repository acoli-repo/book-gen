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

import de.acoli.informatik.uni.frankfurt.de.aplusplus.Publication;
import static de.acoli.informatik.uni.frankfurt.de.util.SentenceSplitter.AFTER_TOK_WO_BRACKET_IDX;
import static de.acoli.informatik.uni.frankfurt.de.util.SentenceSplitter.BRACKET_INFO_IDX;
import static de.acoli.informatik.uni.frankfurt.de.util.SentenceSplitter.COREFERENCE_REPLACEMENT_IDX;
import static de.acoli.informatik.uni.frankfurt.de.util.SentenceSplitter.SENT_NUM_IDX;
import static de.acoli.informatik.uni.frankfurt.de.util.SentenceSplitter.WORD_IDX;
import static de.acoli.informatik.uni.frankfurt.de.demos.AllSectionSentencesAggregator.sectionTypesToProduce;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codehaus.jackson.map.ObjectMapper;
import de.acoli.informatik.uni.frankfurt.de.reader.ChapterStructureReader;
import de.acoli.informatik.uni.frankfurt.de.util.Utility;

/**
 *
 * Reintroduces the content of parentheses into syntactically resturctured
 * sentences.
 *
 * Reads in a restructured token sequence, plus original token indices. Checks
 * the original sentence for bracket content and inserts it at the right places.
 * Whenever a citation (CRX) is encountered a normalized chapter-specific
 * NORM-CR is produced and a chapter-specific bibliography with normalized and
 * original CRs (possibly DOIs) and BibUnstructureds is produced.
 *
 * @author niko
 */
public class BracketContentReintroducerLocalBibGenerator {

    static boolean verbose = false;

    public static String DIR = "gen/";
    public static String SHARED_DIR ="gen/";

    public static String CORPUS_JSON = "corpus.json";
    public static String CHAPTER_STRUCTURE = "chap-struc.html";

    public static String IDS = "section-sentences-ids.txt";
    public static String RESTRUCTURED = "section-sentences-restructured-tok.txt";
    public static String META = "section-sentences-restructured-meta.tsv";

    public static String CITING_SENTENCES = "citing-sentences.txt";
    public static String RESTRUCTURED_WITH_BRACKETS = "section-sentences-restructured-and-bracket-content-tok.txt";
    public static String RESTRUCTURED_WITH_BRACKETS_HTML = "sentences-restructured-and-bracket-introduced.html";
    public static String RESTRUCTURED_WITH_BRACKETS_CITNORM_HTML = "sentences-restructured-and-bracket-introduced-citnorm.html";
    public static String LOCAL_BIBLIOGRAPHY = "section-sentences-restructured-and-bracket-content-tok.bib";

    public static String HTML_HEADER = "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<style>\n"
            + ".tooltip {\n"
            + "    position: relative;\n"
            + "    display: inline-block;\n"
            + "}\n"
            + "\n"
            + ".tooltip .tooltiptext {\n"
            + "    visibility: hidden;\n"
            + "    width: 1020px;\n"
            + "    background-color: #555;\n"
            + "    color: #fff;\n"
            + "    text-align: center;\n"
            + "    border-radius: 6px;\n"
            + "    padding: 5px 0;\n"
            + "    position: absolute;\n"
            + "    z-index: 1;\n"
            + "    bottom: 125%;\n"
            + "    left: 50%;\n"
            + "    margin-left: -360px;\n"
            + "    opacity: 0;\n"
            + "    transition: opacity 0.3s;\n"
            + "}\n"
            + "\n"
            + ".tooltip .tooltiptext::after {\n"
            + "    content: \"\";\n"
            + "    position: absolute;\n"
            + "    top: 100%;\n"
            + "    left: 50%;\n"
            + "    margin-left: -5px;\n"
            + "    border-width: 5px;\n"
            + "    border-style: solid;\n"
            + "    border-color: #555 transparent transparent transparent;\n"
            + "}\n"
            + "\n"
            + ".tooltip:hover .tooltiptext {\n"
            + "    visibility: visible;\n"
            + "    opacity: 1;\n"
            + "}\n"
            + "</style>\n";

    public static void main(String[] args) throws FileNotFoundException, IOException {

        if (args.length == 2) {
            DIR = args[0];
            SHARED_DIR = args[1];
        }

        LinkedHashMap<String, String> sectionToDocids = ChapterStructureReader.getSectionToDocAssignments(DIR + CHAPTER_STRUCTURE);
        int numChapters = ChapterStructureReader.getNumChapters();

        // Read in corpus.json.
        byte[] jsonData = Files.readAllBytes(Paths.get(SHARED_DIR + CORPUS_JSON));
        ObjectMapper mapper = new ObjectMapper();
        List<Publication> publications = Arrays.asList(mapper.readValue(jsonData, Publication[].class));

        for (int chapNum = 1; chapNum <= numChapters; chapNum++) {
            // Find all restructured files.
            ArrayList<File> restructuredFiles = new ArrayList<>();
            getRestructuredFiles(new File(DIR + "/chapters/" + chapNum + "/"), restructuredFiles);
            Collections.sort(restructuredFiles);

            int normalizedCRId = 1;
            LinkedHashMap<String, String> allCollectedCitations = new LinkedHashMap<String, String>();

            for (File aRestructuredFile : restructuredFiles) {
                if (verbose) {
                    System.out.println("*** Analyzing " + aRestructuredFile.getAbsolutePath() + "");
                }
                String parent = aRestructuredFile.getParent() + "/";

                // Export every "citing" sentence for each CR-NORM.
                PrintWriter citingSentencesWriter = new PrintWriter(new File(parent + CITING_SENTENCES));

                PrintWriter wRestrAndBrack = new PrintWriter(new File(parent + RESTRUCTURED_WITH_BRACKETS_HTML));
                wRestrAndBrack.write("<p>\n");
                PrintWriter wRestrAndBrackCitNorm = new PrintWriter(new File(parent + RESTRUCTURED_WITH_BRACKETS_CITNORM_HTML));
                wRestrAndBrackCitNorm.write(HTML_HEADER);

                PrintWriter w = new PrintWriter(new File(parent + RESTRUCTURED_WITH_BRACKETS));
                PrintWriter localBibWriter = new PrintWriter(new File(parent + LOCAL_BIBLIOGRAPHY));
                // Bibunstructured.
                localBibWriter.write("NORM-CR" + "\t" + "doc-CR" + "\t" + "doc-DOI" + "\t" + "CR-DOI" + "\t" + "BibUnstructured" + "\n");
                // EXPORT bibliography, i.e. export all 
                TreeSet<String> bibToExport = new TreeSet<String>();

                Scanner metaS = new Scanner(new File(parent + META));
                Scanner idsS = new Scanner(new File(parent + IDS));
                // skip first line.
                idsS.nextLine();
                Scanner restrS = new Scanner(new File(parent + RESTRUCTURED));
                String prevHeading = "";
                int prevDocId = -1;

                while (restrS.hasNextLine()) {
                    String aLine = restrS.nextLine();
                    ArrayList<String> restructuredTokens = new ArrayList<String>(Arrays.asList(aLine.split("\\s")));
                    String metaLine = metaS.nextLine().trim();
                    String[] metaItems = metaLine.split("\\t");
                    ArrayList<String> restructuredTokensIndices = new ArrayList<String>(Arrays.asList(metaItems[metaItems.length - 1].split("\\,")));

                    if (restructuredTokens.size() != restructuredTokensIndices.size()) {
                        System.err.println("Something wrong! #restr tokens != # indices!");
                        System.exit(0);
                    }

                    String idLine = idsS.nextLine().trim();
                    String[] idItems = idLine.split("\\,");
                    String aSectionTypeToProduce = idItems[idItems.length - 1];
                    int docId = Integer.parseInt(idItems[1]);

                    String heading = aSectionTypeToProduce.replace("\"", "");
                    if (!heading.equals(prevHeading) && !sectionTypesToProduce.contains(heading)) {
                        wRestrAndBrack.write("<h3>" + heading + "</h3>\n");
                        wRestrAndBrackCitNorm.write("<h3>" + heading + "</h3>\n");
                    } else {
                        if (sectionTypesToProduce.contains(heading)) {
                            // "Introduction": New doc ID new paragraph!
                            if (prevDocId != docId) {
                                wRestrAndBrack.write("<p>" + "</p>\n");
                                wRestrAndBrackCitNorm.write("<p>" + "</p>\n");
                            }
                        }
                    }

                    prevHeading = heading;
                    prevDocId = docId;

                    Publication thisOne = null;
                    for (Publication aPub : publications) {
                        if (aPub.getDocId() == docId) {
                            thisOne = aPub;
                        }
                    }
                    // Get the sentence number (of this summary sentence).
                    int sentNum = Integer.parseInt(idItems[4]);
                    if (verbose) {
                        System.out.println("Sent num: " + sentNum);
                    }
                    // Get the sentence object form corpus.json.
                    ArrayList<String> sentence = getSentence(sentNum, thisOne);
                    //System.out.println("sentence object: " + sentence);

                    HashMap<String, ArrayList<String>> bibliography = thisOne.getBibliography();

                    // Get the original.
                    StringBuilder sbOriginalSentence = new StringBuilder();
                    if (verbose) {
                        System.out.print("Original:\t");
                    }
                    for (int tIdx = 0; tIdx < sentence.size(); tIdx++) {
                        String anOriginalToken = sentence.get(tIdx).split("\\|")[WORD_IDX] + " ";
                        sbOriginalSentence.append(anOriginalToken);
                        if (verbose) {
                            System.out.print(anOriginalToken);
                        }
                    }

                    StringBuilder sb = new StringBuilder();
                    boolean containsCitationWithoutBracket = false;
                    for (int tIdx = 0; tIdx < sentence.size(); tIdx++) {
                        String word = sentence.get(tIdx).split("\\|")[WORD_IDX];
                        // CR reference but not! part of bracket annotation.
                        if (word.matches("CR\\d+") && "n".equals(sentence.get(tIdx).split("\\|")[BRACKET_INFO_IDX])) {
                            containsCitationWithoutBracket = true;
                        }
                        sb.append(word + " ");
                    }

                    if (verbose) {
                        System.out.println();
                    }
                    if (verbose) {
                        System.out.println("Restruct:\t" + aLine);
                    }

                    // Collect bracket tokens.
                    HashMap<Integer, String> afterIdxToBracket = new HashMap<>();
                    for (int tIdx = 0; tIdx < sentence.size(); tIdx++) {
                        String token;
                        String bracketInfo = sentence.get(tIdx).split("\\|")[BRACKET_INFO_IDX];
                        StringBuilder bracketContent = new StringBuilder();
                        if ("b".equals(bracketInfo)) {
                            int afterIdx = Integer.parseInt(sentence.get(tIdx).split("\\|")[AFTER_TOK_WO_BRACKET_IDX].replace(">", ""));
                            while (!"n".equals(bracketInfo) && tIdx < sentence.size()) {
                                token = sentence.get(tIdx).split("\\|")[WORD_IDX];
                                bracketContent.append(token + " ");
                                tIdx++;
                                if (tIdx < sentence.size()) {
                                    bracketInfo = sentence.get(tIdx).split("\\|")[BRACKET_INFO_IDX];
                                }
                            }
                            afterIdxToBracket.put(afterIdx, bracketContent.toString().trim());
                        }
                    }
                    if (verbose) {
                        System.out.println("Bracket content to insert: " + afterIdxToBracket);
                    }

                    // Iterate over restructured sentence tokens and ids in parallel
                    // and insert bracket content where applicable.
                    StringBuilder restructuredWithBracketsReintroduced = new StringBuilder();
                    for (int i = 0; i < restructuredTokens.size(); i++) {
                        String aRestrTok = restructuredTokens.get(i);
                        String aRestrIdx = restructuredTokensIndices.get(i);

                        if (!aRestrIdx.equals("X") && !aRestrIdx.startsWith("R-")) {
                            int aRestrIdxInt = Integer.parseInt(aRestrIdx);
                            if (sentence.size() > aRestrIdxInt) {
                                String coreferenceReplacement = sentence.get(aRestrIdxInt).split("\\|")[COREFERENCE_REPLACEMENT_IDX];
                                if (!coreferenceReplacement.equals("-") && coreferenceReplacement.length() > 0) {
                                    // First token of sentence. Make sure first letter is upper case!
                                    if (i == 0) {
                                        char firstChar = Character.toUpperCase(coreferenceReplacement.charAt(0));
                                        coreferenceReplacement = firstChar + coreferenceReplacement.substring(1);
                                    } else {
                                        if (!coreferenceReplacement.contains("-RSB-") && !coreferenceReplacement.contains("ENTITY")) {
                                            // Lowercase first letter when it's within a sentence.
                                            char firstChar = Character.toLowerCase(coreferenceReplacement.charAt(0));
                                            coreferenceReplacement = firstChar + coreferenceReplacement.substring(1);
                                        }
                                    }
                                    coreferenceReplacement = coreferenceReplacement.replace("-RSB-", "").replace("-LSB-", "").replace(" 's", "'s");
                                    if (coreferenceReplacement.endsWith("~'s")) {
                                        coreferenceReplacement = coreferenceReplacement.replace("~'s", "~ 's");
                                    }
                                    aRestrTok = "" + coreferenceReplacement + "";
                                }
                            } else {
                            }
                        }

                        if (!aRestrIdx.startsWith("R-")) {
                            restructuredWithBracketsReintroduced.append(aRestrTok + " ");
                        }

                        if (aRestrIdx.equals("X")) {
                            continue;
                        }
                        aRestrIdx = aRestrIdx.replace("R-", "");

                        if (aRestrIdx.equals("X")) {
                            continue;
                        }
                        if (afterIdxToBracket.containsKey(Integer.parseInt(aRestrIdx))) {
                            String bracketSpan = afterIdxToBracket.get(Integer.parseInt(aRestrIdx));
                            restructuredWithBracketsReintroduced.append(bracketSpan + " ");
                        }
                    }
                    String restructuredWithBracketsReintroducedStr = restructuredWithBracketsReintroduced.toString().replaceAll("\\s+", " ");
                    if (verbose) {
                        System.out.println("Inserted:\t" + restructuredWithBracketsReintroducedStr);
                    }
                    if (verbose) {
                        System.out.println();
                    }
                    w.write(restructuredWithBracketsReintroducedStr + "\n");
                    LinkedHashMap<String, String> citationsInThatSentence = new LinkedHashMap<>();
                    String[] tokens = restructuredWithBracketsReintroducedStr.split("\\s");
                    for (int tIdx = 0; tIdx < tokens.length; tIdx++) {
                        String tok = tokens[tIdx];
                        if (tok.matches("CR\\d+")) {
                            if (tIdx > 0 && tIdx < tokens.length - 1) {
                                // check left and right.
                                if (tokens[tIdx - 1].equals(Utility.CITATION_MARKER) && tokens[tIdx + 1].equals(Utility.CITATION_MARKER)) {
                                    // Get the citation reference from the bibliography.
                                    ArrayList<String> reference = bibliography.get(tok);

                                    String bibunstr = reference.get(1);
                                    String crDoi = reference.get(0);

                                    String crNormForThatRef = "NORM-CR-" + normalizedCRId + "";
                                    // When crDOI or citation is already know. 
                                    // dont add it again. only replace by normalized CR.
                                    if (allCollectedCitations.containsKey(bibunstr)) {
                                        // Get normalized CR for that reference.
                                        crNormForThatRef = allCollectedCitations.get(bibunstr);
                                    } else if (allCollectedCitations.containsKey(crDoi)) {
                                        // Get already know normalized CR.
                                        crNormForThatRef = allCollectedCitations.get(crDoi);
                                    } // add a NEW normalized CR.
                                    else {
                                        allCollectedCitations.put(bibunstr, crNormForThatRef);
                                        if (!crDoi.equals("null")) {
                                            allCollectedCitations.put(crDoi, crNormForThatRef);
                                        }
                                        normalizedCRId++;
                                    }

                                    String aBibToExport
                                            = crNormForThatRef + "\t" + tok + "\t" + thisOne.getDoi() + "\t" + crDoi + "\t" + bibunstr;
                                    bibToExport.add(aBibToExport);

                                    // Replace in text.
                                    citationsInThatSentence.put(Utility.CITATION_MARKER + " " + tok + " " + Utility.CITATION_MARKER, crNormForThatRef);

                                } else {
                                }
                            }
                        }
                    }

                    wRestrAndBrack.write(restructuredWithBracketsReintroducedStr + " [" + "<a href=\"https://doi.org/" + thisOne.getDoi() + "\" target=\"_blank\"/>" + thisOne.getDoi() + "</a>" + "] <br>");

                    String restructuredWithBracketsReintroducedStrCitsNormalized = restructuredWithBracketsReintroducedStr;
                    for (String aCitInSent : citationsInThatSentence.keySet()) {
                        restructuredWithBracketsReintroducedStrCitsNormalized
                                = restructuredWithBracketsReintroducedStrCitsNormalized.replace(aCitInSent, citationsInThatSentence.get(aCitInSent));

                    }
                    wRestrAndBrackCitNorm.write(
                            Utility.writeTooltipLine(restructuredWithBracketsReintroducedStrCitsNormalized, thisOne.getDoi(), sbOriginalSentence.toString(), true));

                    // Detect all normalized citations in that sentence and 
                    // print a sentence instance for each citation. 
                    // Replace NORM-CR-XXX matches by just a [X].
                    Pattern normCrPattern = Pattern.compile("NORM-CR-(\\d+)");
                    // get a matcher object
                    Matcher normCrMatcher = normCrPattern.matcher(restructuredWithBracketsReintroducedStrCitsNormalized);
                    while (normCrMatcher.find()) {
                        String match = normCrMatcher.group();
                        citingSentencesWriter.write(match + "\t"
                                + restructuredWithBracketsReintroducedStrCitsNormalized.replace("\t", " ") + "\t"
                                + sbOriginalSentence.toString().replace("\t", " ") + "\t"
                                + thisOne.getDoi() + "\n");
                    }

                }
                idsS.close();
                restrS.close();
                metaS.close();

                w.flush();
                w.close();

                // Export bibliography.
                for (String aBibToExport : bibToExport) {
                    localBibWriter.write(aBibToExport + "\n");
                }

                localBibWriter.flush();
                localBibWriter.close();

                citingSentencesWriter.flush();
                citingSentencesWriter.close();

                wRestrAndBrack.write("</p>\n");
                wRestrAndBrack.flush();
                wRestrAndBrack.close();

                wRestrAndBrackCitNorm.write("\n"
                        + "</html>\n");
                wRestrAndBrackCitNorm.flush();
                wRestrAndBrackCitNorm.close();

            }
        }

    }

    // TODO: Refactor code.
    private static ArrayList<String> getSentence(int sentNum, Publication aPub) {
        // Search in abstract.
        for (ArrayList<String> sentence : aPub.abstrTokens) {
            int aSentNum = Integer.parseInt(sentence.get(0).split("\\|")[SENT_NUM_IDX]);
            if (sentNum == aSentNum) {
                return sentence;
            }
        }
        for (ArrayList<String> sentence : aPub.introductionTokens) {
            int aSentNum = Integer.parseInt(sentence.get(0).split("\\|")[SENT_NUM_IDX]);
            if (sentNum == aSentNum) {
                return sentence;
            }
        }
        for (ArrayList<String> sentence : aPub.discussionTokens) {
            int aSentNum = Integer.parseInt(sentence.get(0).split("\\|")[SENT_NUM_IDX]);
            if (sentNum == aSentNum) {
                return sentence;
            }
        }
        for (ArrayList<String> sentence : aPub.relatedWorkTokens) {
            int aSentNum = Integer.parseInt(sentence.get(0).split("\\|")[SENT_NUM_IDX]);
            if (sentNum == aSentNum) {
                return sentence;
            }
        }
        for (ArrayList<String> sentence : aPub.conclusionTokens) {
            int aSentNum = Integer.parseInt(sentence.get(0).split("\\|")[SENT_NUM_IDX]);
            if (sentNum == aSentNum) {
                return sentence;
            }
        }

        for (ArrayList<String> sentence : aPub.materialsAndMethodsTokens) {
            int aSentNum = Integer.parseInt(sentence.get(0).split("\\|")[SENT_NUM_IDX]);
            if (sentNum == aSentNum) {
                return sentence;
            }
        }

        for (ArrayList<String> sentence : aPub.resultsTokens) {
            int aSentNum = Integer.parseInt(sentence.get(0).split("\\|")[SENT_NUM_IDX]);
            if (sentNum == aSentNum) {
                return sentence;
            }
        }

        ArrayList<ArrayList<ArrayList<String>>> bodySections = aPub.bodySectionsTokens;
        for (ArrayList<ArrayList<String>> aBodySection : bodySections) {
            for (ArrayList<String> sentence : aBodySection) {
                int aSentNum = Integer.parseInt(sentence.get(0).split("\\|")[SENT_NUM_IDX]);
                if (sentNum == aSentNum) {
                    return sentence;
                }
            }
        }

        System.err.println("Error: No sentence found in corpus.json for sentence " + sentNum);
        return null;
    }

    public static void getRestructuredFiles(final File folder, ArrayList<File> files) {

        if (folder.listFiles() == null) {
            System.err.println("Sorry, please verify that this folder exists: " + folder);
            System.exit(0);
        }
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                getRestructuredFiles(fileEntry, files);
            } else {
                if (fileEntry.getName().equals("section-sentences-restructured-tok.txt")) {
                    files.add(fileEntry);
                }
            }
        }
    }
}
