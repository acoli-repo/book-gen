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
import static de.acoli.informatik.uni.frankfurt.de.util.SentenceSplitter.BRACKET_INFO_IDX;
import static de.acoli.informatik.uni.frankfurt.de.util.SentenceSplitter.SENT_NUM_IDX;
import static de.acoli.informatik.uni.frankfurt.de.util.SentenceSplitter.WORD_IDX;
import de.acoli.informatik.uni.frankfurt.de.extraction.TextRankMod;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;
import de.acoli.informatik.uni.frankfurt.de.reader.ChapterStructureReader;
import de.acoli.informatik.uni.frankfurt.de.reader.SentIDCounter;
import de.acoli.informatik.uni.frankfurt.de.util.SimilarSentence;
import de.acoli.informatik.uni.frankfurt.de.util.Utility;
import static de.acoli.informatik.uni.frankfurt.de.util.Utility.expressBrackets;

/**
 *
 *
 * @author niko
 */
public class AllSectionSentencesAggregator {

    public static String DIR = "gen/";
    public static String SHARED_DIR = "gen/";

    // Reads chapter structure as produced by mkstructure.py
    public static String CHAPTER_STRUCTURE = DIR + "chap-struc.html";
    // Reference corpus (input to mkstructure.py).
    public static String CORPUS_JSON = SHARED_DIR + "corpus.json";

    public static boolean ALSO_ADD_OTHER_BODY_SECTIONS_APART_FROM_EXTENDED_ABSTRACT = false;

    public static String PATH_TO_TEXTRANK = null;
    public static String PATH_TO_PYTHON = "/usr/local/bin/python3";
    public static boolean EXTENDED_ABSTRACT;

    public static boolean TEXT_RANK = true;
    public static String TEXT_RANK_WORD_LIMIT = "270";
    public static String TEXT_RANK_SUMMARY_RATIO = "0.6";

    public static String SENTENCES_NOT_RESTRUCTURED = "sentences-not-restructured.html";

    public static final String HEADER = "sendId,docId,a++,origSentIdx,globalSentIdx,section,heading\n";

    public static ArrayList<String> sectionTypesToProduce;
    public static ArrayList<Integer> addedSentencesForExtendedAbstract;
    public static ArrayList<String> extendedAbstractPhrases;

    static {
        sectionTypesToProduce = new ArrayList<String>();
        //sectionTypesToProduce.add("Abstract");
        sectionTypesToProduce.add("Introduction");
        sectionTypesToProduce.add("Conclusion");
        sectionTypesToProduce.add("Discussion");
        addedSentencesForExtendedAbstract = new ArrayList<>();
        
        // Read in 
        extendedAbstractPhrases = Utility.getExtendedAbstractPhrases();
    }

    public static void main(String[] args) throws IOException {

        if (args.length == 6) {
            DIR = args[0]; // DIR.
            SHARED_DIR = args[5];
            CHAPTER_STRUCTURE = DIR + "chap-struc.html";
            CORPUS_JSON = SHARED_DIR + "corpus.json";
            PATH_TO_TEXTRANK = args[1];
            PATH_TO_PYTHON = args[2];
            EXTENDED_ABSTRACT = Boolean.parseBoolean(args[3]);
            ALSO_ADD_OTHER_BODY_SECTIONS_APART_FROM_EXTENDED_ABSTRACT = Boolean.parseBoolean(args[4]);
        } else {

            System.err.println("Running without arguments...");
            
        }

        // Read in corpus.json.
        byte[] jsonData = Files.readAllBytes(Paths.get(CORPUS_JSON));
        ObjectMapper mapper = new ObjectMapper();
        List<Publication> publications = Arrays.asList(mapper.readValue(jsonData, Publication[].class));

        LinkedHashMap<String, String> sectionToDocids = ChapterStructureReader.getSectionToDocAssignments(CHAPTER_STRUCTURE);
        int numChapters = ChapterStructureReader.getNumChapters();

        // Make a directory for all the generated files
        // in the following format:
        //
        // Chapters/
        //      Chapter 1
        //          Intro
        //          Conclusion
        //          (Related Work)
        //      Chapter 2
        //          Intro
        //          ...
        //      Chapter 3
        //          ...
        String chaptersDir = DIR + "/chapters";
        File directory = new File(chaptersDir);
        if (directory.exists()) {
            System.err.println("Directory non-empty. Deleting it's content.");
            // Delete it's contents.
            Utility.deleteDirectory(directory);
        }
        directory.mkdir();

        
        PrintWriter allSentsTokWriter = new PrintWriter(new File(DIR + "/chapters/all-section-sentences-tok.txt"));
        PrintWriter allSentsIdsWriter = new PrintWriter(new File(DIR + "/chapters/all-section-sentences-ids.txt"));
        allSentsIdsWriter.write(HEADER);

        for (int chapNum = 0; chapNum < numChapters; chapNum++) {
            File chapDir = new File(chaptersDir + "/" + (chapNum + 1));
            chapDir.mkdir();

            // Reset already added sentences for extended abstract.
            addedSentencesForExtendedAbstract.clear();
            addedSentencesForExtendedAbstract = new ArrayList<Integer>();

            for (String aSectionTypeToProduce : sectionTypesToProduce) {

                String chaptersSectiontypeDir = chaptersDir + "/" + (chapNum + 1) + "/" + aSectionTypeToProduce;
                File chapSectionTypeDir = new File(chaptersSectiontypeDir);
                chapSectionTypeDir.mkdir();

                PrintWriter sentencesNOTRestructuredWriter = new PrintWriter(new File(chaptersSectiontypeDir + "/" + SENTENCES_NOT_RESTRUCTURED));
                sentencesNOTRestructuredWriter.write("<p>\n");

                // Sentences to be reordered later. 
                PrintWriter sectionSentsTokWriter = new PrintWriter(new File(chaptersSectiontypeDir + "/" + "section-sentences-tok.txt"));
                PrintWriter sectionSentsIdsWriter = new PrintWriter(new File(chaptersSectiontypeDir + "/" + "section-sentences-ids.txt"));
                sectionSentsIdsWriter.write(HEADER);

                SentIDCounter sic = new SentIDCounter(1);
                for (String section : sectionToDocids.keySet()) {
                    int docId = Integer.parseInt(sectionToDocids.get(section));

                    // For a specific chapter
                    if (section.startsWith((chapNum + 1) + ".")) {
                        for (Publication p : publications) {
                            int corpusDocId = p.getDocId();
                            if (docId == corpusDocId) {

                                ArrayList<ArrayList<String>> sectionSents = null;
                                switch (aSectionTypeToProduce) {
                                    case "Abstract":
                                        sectionSents = p.abstrTokens;
                                        break;
                                    case "Discussion":
                                        sectionSents = p.discussionTokens;
                                        break;
                                    case "Introduction":
                                        sectionSents = p.introductionTokens;
                                        break;
                                    case "Conclusion":
                                        sectionSents = p.conclusionTokens;
                                        break;
                                    case "RelatedWork":
                                        sectionSents = new ArrayList<ArrayList<String>>();
                                        sectionSents.addAll(p.relatedWorkTokens);
                                        break;
                                }

                                ArrayList<Integer> exportSentIndices = computeExportSummarySentences(aSectionTypeToProduce, sectionSents);
                                exportSummarySentences(exportSentIndices, sectionSents, section, p,
                                        sic, sentencesNOTRestructuredWriter,
                                        sectionSentsTokWriter, sectionSentsIdsWriter,
                                        allSentsTokWriter, allSentsIdsWriter, aSectionTypeToProduce
                                );
                            }
                        }
                    }
                }

                sentencesNOTRestructuredWriter.write("</p>\n");
                sentencesNOTRestructuredWriter.flush();
                sentencesNOTRestructuredWriter.close();

                sectionSentsTokWriter.flush();
                sectionSentsTokWriter.close();

                sectionSentsIdsWriter.flush();
                sectionSentsIdsWriter.close();
            }
        }

        // Generate paper summaries, i.e. summaries of sections other than
        // intro, conclusion.
        for (int chapNum = 0; chapNum < numChapters; chapNum++) {
            String papersDir = chaptersDir + "/" + (chapNum + 1) + "/" + "Papers";
            File papersTypeDir = new File(papersDir);
            papersTypeDir.mkdir();
        }

        // Every 3-level "section", e.g. "1.2.1" corresponds to a document.
        for (String section : sectionToDocids.keySet()) {
            int docId = Integer.parseInt(sectionToDocids.get(section));
            int chapNum = Integer.parseInt(section.substring(0, section.indexOf(".")));
            // Make new folder with section name.
            File sectionType = new File(chaptersDir + "/" + chapNum + "/Papers/" + section);
            sectionType.mkdir();
            Publication aPub = null;
            for (Publication p : publications) {
                if (docId == p.getDocId()) {
                    aPub = p;
                }
            }

            System.err.println("Summarizing section " + section + " / body text of file: " + aPub.getAPlusPlusID() + " (" + aPub.getDocId() + "): " + aPub.getDoi());

            SentIDCounter sic = new SentIDCounter(1);
            PrintWriter sentencesNOTReorderedWriter = new PrintWriter(new File(sectionType.getPath() + "/" + SENTENCES_NOT_RESTRUCTURED));
            sentencesNOTReorderedWriter.write("<p>\n");
            PrintWriter sectionSentencesTokWriter = new PrintWriter(new File(sectionType.getPath() + "/" + "section-sentences-tok.txt"));
            PrintWriter sectionSentsIdsWriter = new PrintWriter(new File(sectionType.getPath() + "/" + "section-sentences-ids.txt"));
            sectionSentsIdsWriter.write(HEADER);

            ArrayList<ArrayList<ArrayList<String>>> sectionsToProcess = new ArrayList<>();
            ArrayList<String> headingsToProcess = new ArrayList<>();

            // Add abstract 
            ArrayList<ArrayList<String>> theAbstract = aPub.abstrTokens;
            if (theAbstract.size() > 0) {
                sectionsToProcess.add(theAbstract);
                headingsToProcess.add("Abstract");
            }

            if (ALSO_ADD_OTHER_BODY_SECTIONS_APART_FROM_EXTENDED_ABSTRACT) {
                // Add normal body sections.
                sectionsToProcess.addAll(aPub.bodySectionsTokens);
                headingsToProcess.addAll(aPub.getBodySectionHeadings());

                // Add materials 
                ArrayList<ArrayList<String>> matAndMethods = aPub.materialsAndMethodsTokens;
                if (matAndMethods.size() > 0) {
                    sectionsToProcess.add(matAndMethods);
                    headingsToProcess.add("Materials and Methods");
                }

                // Add results
                ArrayList<ArrayList<String>> results = aPub.resultsTokens;
                if (results.size() > 0) {
                    sectionsToProcess.add(results);
                    headingsToProcess.add("Results");
                }
            }

            for (int b = 0; b < sectionsToProcess.size(); b++) {
                ArrayList<ArrayList<String>> sectionSents = sectionsToProcess.get(b);
                String aBodySectionHeading = headingsToProcess.get(b);

                ArrayList<Integer> exportSentIndices = computeExportSummarySentences(aBodySectionHeading, sectionSents);
                exportSummarySentences(exportSentIndices, sectionSents, section, aPub,
                        sic, sentencesNOTReorderedWriter, sectionSentencesTokWriter, sectionSentsIdsWriter,
                        allSentsTokWriter, allSentsIdsWriter,
                        aBodySectionHeading);

            }

            sentencesNOTReorderedWriter.flush();
            sentencesNOTReorderedWriter.close();
            sectionSentencesTokWriter.flush();
            sectionSentencesTokWriter.close();
            sectionSentsIdsWriter.flush();
            sectionSentsIdsWriter.close();
        }

        allSentsTokWriter.flush();
        allSentsTokWriter.close();

        allSentsIdsWriter.flush();
        allSentsIdsWriter.close();
        System.out.println("... produced aggregated files.");

    }

    private static ArrayList<Integer> computeExportSummarySentences(String aSectionTypeToProduce, ArrayList<ArrayList<String>> sectionSents) throws IOException {

        ArrayList<Integer> exportSentIndices = new ArrayList<>();
        // Initially, add all sentences to the summary.
        for (int i = 0; i < sectionSents.size(); i++) {
            exportSentIndices.add(i);
        }
        
        //if (TEXT_RANK && !"Abstract".equals(aSectionTypeToProduce)) {
        if (TEXT_RANK) {
            // Reset sentences to export.
            exportSentIndices = new ArrayList<>();
            String introToSummarize = Utility.turnListOfSentencesIntoTextForSummary(sectionSents);
            TextRankMod trRatio = new TextRankMod(introToSummarize);
            trRatio.setPythonPath(PATH_TO_PYTHON);
            if (PATH_TO_TEXTRANK != null) {
                trRatio.setSummarizer(PATH_TO_TEXTRANK);
            }
            // Summarize with ratio.
            trRatio.textRank(TEXT_RANK_SUMMARY_RATIO, "ratio");

            TextRankMod trThresh = new TextRankMod(introToSummarize);
            trThresh.setPythonPath(PATH_TO_PYTHON);
            if (PATH_TO_TEXTRANK != null) {
                trThresh.setSummarizer(PATH_TO_TEXTRANK);
            }
            // Summarize with threshold.
            trThresh.textRank(TEXT_RANK_WORD_LIMIT, "words");

            String ratioSummary = trRatio.getSummary().toString();
            String thresholdSummary = trThresh.getSummary().toString();

            ArrayList<String> summarySentences = null;
            if (ratioSummary.length() < thresholdSummary.length()) {
                summarySentences = new ArrayList<>(Arrays.asList(trRatio.getSummary().toString().split("\n")));
                //System.err.println(aSectionTypeToProduce+ " / Taking smaller summary from: RATIO (proportion in %) "+ TEXT_RANK_SUMMARY_RATIO);
            } else {
                summarySentences = new ArrayList<>(Arrays.asList(trThresh.getSummary().toString().split("\n")));
                //System.err.println(aSectionTypeToProduce+ " / Taking smaller summary from: THRESHOLD limit in words "+ TEXT_RANK_WORD_LIMIT);
            }
            // Get the original sentences indices for that summary:
            for (int i = 0; i < sectionSents.size(); i++) {
                String anIntroSent = Utility.turnSingleSentenceIntoText(sectionSents.get(i)).trim();
                for (int j = 0; j < summarySentences.size(); j++) {
                    if (anIntroSent.equals(summarySentences.get(j))) {
                        // Check if three sentences in a row were added.
                        boolean allowToAdd = checkAllowToAddFourSentencesInARow(exportSentIndices, i);
                        if (allowToAdd) {
                            exportSentIndices.add(i);
                        }
                    }
                }
            }
            // Textrank keywords:
            //System.out.println("Keywords: ");
            //for (String k : tr.getKeywords()) {
            //    System.out.println("\t" + k);
            //}
            //System.out.println();
        }
        return exportSentIndices;

    }

    private static void exportSummarySentences(ArrayList<Integer> exportSentIndices,
            ArrayList<ArrayList<String>> sectionSents, String section, Publication p,
            SentIDCounter sic, PrintWriter sentencesNOTRestructuredWriter,
            PrintWriter sectionSentsTokWriter,
            PrintWriter sectionSentsIdsWriter,
            PrintWriter allSentsTokWriter, 
            PrintWriter allSentsIdsWriter, 
            String aSectionTypeToProduce) {

        String heading = aSectionTypeToProduce.replace(",", "comma");

        int origSentIdx = 1;

        // Write a paragraph heading.
        if (!sectionTypesToProduce.contains(heading)) {
            sentencesNOTRestructuredWriter.write("<h3>" + heading + "</h3>\n");
        }

        // "i" is the sent index of each section
        // starting with 0-counting.
        for (int i = 0; i < sectionSents.size(); i++) {
            ArrayList<String> sentence = sectionSents.get(i);
            int prevDocID = p.getDocId();
            // Either, only use summary sentences, or export all sentences.
            if (exportSentIndices.contains(i)) {
                boolean isLastSentence = (i == sectionSents.size() - 1);
                StringBuilder sentenceBuilder = new StringBuilder();
                int globalSentIdx = -1;
                for (int tokIdx = 0; tokIdx < sentence.size(); tokIdx++) {
                    String tok = sentence.get(tokIdx);
                    globalSentIdx = addTokenToStringBuilder(tok, sentenceBuilder);
                }

                String sentenceStr = sentenceBuilder.toString();
                String doi = p.getDoi();
                int docId = p.getDocId();
                String aPlusPlusId = p.getAPlusPlusID();

                String export
                        = sic.getId() + "," + docId + "," + aPlusPlusId + ","
                        + origSentIdx + "," + globalSentIdx + "," + section
                        + ",\"" + heading + "\"\n";

                sentencesNOTRestructuredWriter.write(sentenceStr + " [" + "<a href=\"https://doi.org/" + doi + "\" target=\"_blank\"/>" + doi + "</a>" + "] <br>");
                sectionSentsTokWriter.write(sentenceStr + "\n");
                allSentsTokWriter.write(sentenceStr + "\n");
                sectionSentsIdsWriter.write(export);
                allSentsIdsWriter.write(export);

                origSentIdx++;
                sic.increment();

                // Add most similar sentence for current one to "extended abstract".
                if (EXTENDED_ABSTRACT) {
                    if ("Abstract".equals(aSectionTypeToProduce)) {
                        ArrayList<SimilarSentence> mostSimilarSents = findMostSimilarSentencesForCurrent(p, sentenceStr, isLastSentence);
                        if (mostSimilarSents.size() > 1) {
                        }
                        // For all similar sentences.
                        for (int aSimIdx = 0; aSimIdx < mostSimilarSents.size(); aSimIdx++) {
                            globalSentIdx = mostSimilarSents.get(aSimIdx).globalSentIdx;
                            sentenceStr = mostSimilarSents.get(aSimIdx).tokenizedSentence;
                            export
                                    = sic.getId() + "," + docId + "," + aPlusPlusId + ","
                                    + origSentIdx + "," + globalSentIdx + "," + section
                                    + ",\"" + heading + "\"\n";
                            sectionSentsIdsWriter.write(export);
                            allSentsTokWriter.write(sentenceStr + "\n");
                            allSentsIdsWriter.write(export);
                            sentencesNOTRestructuredWriter.write("<b>" + sentenceStr + "</b>"
                                    + " [" + "<a href=\"https://doi.org/" + doi + "\" target=\"_blank\"/>" + doi + "</a>" + "] <br>");
                            sectionSentsTokWriter.write(sentenceStr + "\n");
                            origSentIdx++;
                            sic.increment();
                        }
                    }
                }

            }
        }
        sentencesNOTRestructuredWriter.write("<p></p>");
    }

    // Appends token if not part of bracket and 
    // returns new global sent index.
    private static int addTokenToStringBuilder(String tok, StringBuilder sentenceBuilder) {
        String wordStr = tok.split("\\|")[WORD_IDX];
        int globalSentIdx = Integer.parseInt(tok.split("\\|")[SENT_NUM_IDX]);
        wordStr = expressBrackets(wordStr);
        String bracket = tok.split("\\|")[BRACKET_INFO_IDX];
        if (!"b".equals(bracket)) {
            sentenceBuilder.append(wordStr + " ");
        }
        return globalSentIdx;
    }

    /**
     * Returns a list of most similar sentences given a seed sentence (which
     * stems typically from an abstract) using a weighted bag of words similarity
     * metric
     *
     * @param p
     * @param sentenceStr
     * @return
     */
    private static ArrayList<SimilarSentence> findMostSimilarSentencesForCurrent(Publication p, String sentenceStr, boolean isLastSentence) {

        ArrayList<ArrayList<String>> allSentences = new ArrayList<>();

        ArrayList<ArrayList<String>> introSentences = p.introductionTokens;
        ArrayList<ArrayList<String>> matAndMeth = p.materialsAndMethodsTokens;
        ArrayList<ArrayList<String>> results = p.resultsTokens;
        ArrayList<ArrayList<String>> disc = p.discussionTokens;
        ArrayList<ArrayList<String>> conclusionSentences = p.conclusionTokens;

        allSentences.addAll(introSentences);
        allSentences.addAll(matAndMeth);
        allSentences.addAll(results);
        allSentences.addAll(disc);
        allSentences.addAll(conclusionSentences);

        ArrayList<SimilarSentence> rval = new ArrayList<SimilarSentence>();
        // TODO: Use alternative similarity metric.
        for (int sentIdx = 0; sentIdx < allSentences.size(); sentIdx++) {
            ArrayList<String> aSent = allSentences.get(sentIdx);
            boolean addedSentence = false;
            //double ratioOfWordOverlap = computeBOWunigramOverlap(sentenceStr, aSent);
            //if(wordOverlap > 0.8) {

            //}
            // Check beginning of sentence and exclude it if it's just the same.
            if (aSent.size() > 3 && !addedSentence) {
                String firstWord = aSent.get(0).split("\\|")[WORD_IDX];
                String secondWord = aSent.get(1).split("\\|")[WORD_IDX];
                String thirdWord = aSent.get(2).split("\\|")[WORD_IDX];

                if (// Starts with first three words.
                        (firstWord.equals(sentenceStr.split("\\s")[0])
                        && secondWord.equals(sentenceStr.split("\\s")[1])
                        && thirdWord.equals(sentenceStr.split("\\s")[2]))) {

                    StringBuilder sb = new StringBuilder();
                    int glSIdx = -1;
                    for (String t : aSent) {
                        String wordStr = t.split("\\|")[WORD_IDX];
                        glSIdx = Integer.parseInt(t.split("\\|")[SENT_NUM_IDX]);
                        wordStr = expressBrackets(wordStr);
                        String bracket = t.split("\\|")[BRACKET_INFO_IDX];
                        if (!"b".equals(bracket)) {
                            sb.append(wordStr + " ");
                        }
                    }

                    // Check how many words they have in common.
                    boolean differEnough = differEnough(sentenceStr, sb.toString().trim());
                    if (differEnough) {
                        if (!addedSentencesForExtendedAbstract.contains(glSIdx)) {
                            SimilarSentence simSent = new SimilarSentence(glSIdx, sb.toString().trim());
                            rval.add(simSent);
                            addedSentencesForExtendedAbstract.add(glSIdx);
                        }
                    }
                }
            }

            
            if (isLastSentence) {
                String sentMade = makeSentence(aSent);
                boolean foundExtAbstPhr = false;
                for (String phr : extendedAbstractPhrases) {
                    if (sentMade.contains(phr)) {
                        foundExtAbstPhr = true;
                        break;
                    }
                }
                if (foundExtAbstPhr) {
                    StringBuilder sb = new StringBuilder();
                    int glSIdx = -1;
                    for (String t : aSent) {
                        String wordStr = t.split("\\|")[WORD_IDX];
                        glSIdx = Integer.parseInt(t.split("\\|")[SENT_NUM_IDX]);
                        wordStr = expressBrackets(wordStr);
                        String bracket = t.split("\\|")[BRACKET_INFO_IDX];
                        if (!"b".equals(bracket)) {
                            sb.append(wordStr + " ");
                        }
                    }
                    if (!addedSentencesForExtendedAbstract.contains(glSIdx)) {
                        SimilarSentence simSent = new SimilarSentence(glSIdx, sb.toString().trim());
                        rval.add(simSent);
                        addedSentencesForExtendedAbstract.add(glSIdx);
                    }
                }
            }

        }
        return rval;
    }

    private static boolean differEnough(String string1, String string2) {
        boolean differEnough = true;
        ArrayList<String> current = new ArrayList<String>(Arrays.asList(string1.split("\\s")));
        ArrayList<String> toCompare = new ArrayList<String>(Arrays.asList(string2.split("\\s")));
        // Check how many tokens are the same.
        int numSameTokens = 0;
        for (String currentTok : current) {
            if (toCompare.contains(currentTok)) {
                numSameTokens++;
            }
        }
        // They differ only in a length difference of less than 3.
        if (Math.abs(toCompare.size() - current.size()) <= 4) {
            // Less than only four words differ.
            if (current.size() - numSameTokens <= 4) {
                differEnough = false;
                // Reject it.
                //System.err.println("Rejecting\n" + toCompare + "\n" + "Original:\n" + current);
            }
        }
        return differEnough;
    }

    private static String makeSentence(ArrayList<String> aSent) {
        StringBuilder sb = new StringBuilder();
        for (String t : aSent) {
            String wordStr = t.split("\\|")[WORD_IDX];
            wordStr = expressBrackets(wordStr);
            String bracket = t.split("\\|")[BRACKET_INFO_IDX];
            if (!"b".equals(bracket)) {
                sb.append(wordStr + " ");
            }
        }
        return sb.toString().trim();
    }

    private static boolean checkAllowToAddFourSentencesInARow(ArrayList<Integer> exportSentIndices, int toAdd) {
        boolean allowToAdd = true;
        // Check if index would be the fourth sentence in a row.
        if (exportSentIndices.size() > 3) {
            int next = toAdd;
            int curr = exportSentIndices.get(exportSentIndices.size() - 1);
            int prev = exportSentIndices.get(exportSentIndices.size() - 2);
            int prpr = exportSentIndices.get(exportSentIndices.size() - 3);
            if (prpr + 1 == prev && prev + 1 == curr && curr + 1 == next) {
                //System.err.println("Not allowing to add 4 sents in a row.");
                allowToAdd = false;
            }
        }
        return allowToAdd;
    }
}
