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

/**
 *
 * @author niko
 */
import de.acoli.informatik.uni.frankfurt.de.aplusplus.Publication;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Mention;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.acoli.informatik.uni.frankfurt.de.reader.APlusPlusCollectionsReader;
import de.acoli.informatik.uni.frankfurt.de.reader.SentIDCounter;
import static de.acoli.informatik.uni.frankfurt.de.util.SentenceStartersCollector.getSentenceStarters;
import de.acoli.informatik.uni.frankfurt.de.util.Utility;

public class SentenceSplitter {

    private static StanfordCoreNLP sentenceSplitterTokenizerPipeline = null;
    public static StanfordCoreNLP coreferencePipeline = null;

    private static Map<String, Integer> sentenceStarters = null;

    public static Integer WORD_IDX = 0;
    public static Integer POS_IDX = 1;
    public static Integer LEMMA_IDX = 2;
    public static Integer NER_IDX = 3;
    public static Integer BEG_OFFSET_IDX = 4;
    public static Integer END_OFFSET_IDX = 5;
    public static Integer TOK_NUM_IDX = 6;
    public static Integer SENT_NUM_IDX = 7;
    public static Integer BRACKET_INFO_IDX = 8;
    public static Integer TOK_IDX_WO_BRACKET = 9;
    public static Integer AFTER_TOK_WO_BRACKET_IDX = 10;
    public static Integer COREFERENCE_REPLACEMENT_IDX = 11;

    private static final int NUM_TOKEN_ANNOTATION_ELEMENTS = 12;

    public SentenceSplitter() throws FileNotFoundException {
        Properties sentSplitTokProps = new Properties();
        sentSplitTokProps.setProperty("annotators", "tokenize, ssplit");//, pos, lemma");
        sentenceSplitterTokenizerPipeline = new StanfordCoreNLP(sentSplitTokProps);
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties corefProps = new Properties();
        corefProps.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref");
        corefProps.setProperty("coref.algorithm", "neural");
        corefProps.setProperty("tokenize.whitespace", "true");
        corefProps.setProperty("ssplit.eolonly", "true");
        coreferencePipeline = new StanfordCoreNLP(corefProps);

        String pth = null;
        try {
            pth = SentenceSplitter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException ex) {
            Logger.getLogger(SentenceSplitter.class.getName()).log(Level.SEVERE, null, ex);
        }
        String rsc = "resources/sentence-starters.txt";
        String sentstartersfile = null;
        // Local netbeans project.
        if (pth.contains("build")) {
            sentstartersfile = pth + "../../" + rsc;
        } // jar file.
        else if (pth.contains("dist") && pth.endsWith(".jar")) {
            sentstartersfile = pth.substring(0, pth.lastIndexOf("/")) + "/../" + rsc;
        }
        System.out.println(sentstartersfile);
        sentenceStarters = getSentenceStarters(sentstartersfile);
        // sentenceStarters = getSentenceStarters("resources/sentence-starters.txt");

    }

    
    public static void annotate(String text) {
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);
        // run all Annotators on this text
        sentenceSplitterTokenizerPipeline.annotate(document);
        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            System.out.println("---");
            System.out.println("mentions");
            for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
                System.out.println("\t" + m);
                System.out.println("\t" + m.headString);
                //System.out.println("\t" + m.n);

            }
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String word = token.get(TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                int begOff = token.beginPosition();
                int endOff = token.endPosition();

            }
        }
    }

    public ArrayList<String> getSentences(String text) {
        ArrayList<String> rval = new ArrayList<>();
        Annotation document = new Annotation(text);
        sentenceSplitterTokenizerPipeline.annotate(document);
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            rval.add(sentence.toString());
        }
        return rval;
    }

    public ArrayList<ArrayList<String>> getSentencesAndTokens(String text, boolean isHeadline, boolean isAbstract, Publication aPub, SentIDCounter sic) {

        ArrayList<ArrayList<String>> allSentences = new ArrayList<ArrayList<String>>();

        // Suppress sentence splitting at specific abbreviations.
        // TODO: Experiment with domain-specific sentence boundary detectors.
        text = text.replace(" vol.", " vol ");
        text = text.replace(" (vol.", " (vol ");
        text = text.replace(" ca. ", " ca ");
        text = text.replace(" Chap.", " Chap ");
        text = text.replace(" (Chap.", " (Chap ");
        text = text.replace(" (Chaps.", " (Chaps ");
        text = text.replace(" (chap.", " (chap ");
        text = text.replace(" (chaps.", " (chaps ");
        text = text.replace(" ch. ", " ch ");
        text = text.replace(" transl. ", " transl ");
        text = text.replace(" op. ", " op ");
        text = text.replace(" cit. ", " cit ");
        text = text.replace(" Art. ", " Art ");
        text = text.replace(" (Art. ", " (Art ");
        text = text.replace(" (pers. ", " (pers ");
        text = text.replace(" wt. ", " wt ");
        text = text.replace(" (ie. ", " (ie ");
        text = text.replace(" et. ", " et ");
        text = text.replace(" et.al. ", " and others ");
        text = text.replace(" et. al. ", " and others ");
        text = text.replace(" et al. ", " and others ");
        text = text.replace(" et. al ", " and others ");
        text = text.replace(" cat.", " cat ");
        text = text.replace(" cats.", " cats ");
        text = text.replace(" pl.", " pl ");
        text = text.replace(" Par.", " Par ");
        text = text.replace(" law. ", " law ");
        text = text.replace(" Chaps.", " Chaps");
        text = text.replace(" chaps. ", " chaps ");
        text = text.replace(" re. ", " re ");
        text = text.replace(" (pg. ", " (pg ");
        text = text.replace(" pg. ", " pg ");
        text = text.replace(" (par. ", " (par ");
        text = text.replace(" par. ", " par ");
        text = text.replace(" (ref. ", " (ref ");
        text = text.replace(" ref. ", " ref ");
        
        text = text.replaceAll("\\s+", " ");

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);
        // run all Annotators on this text
        sentenceSplitterTokenizerPipeline.annotate(document);
        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            boolean citationSentence = false;

            String sentenceStr = sentence.toString();
            sentenceStr = smoothToBookSentences(sentenceStr);
            // Filter all sentences that are NOT narrative, e.g.,
            // Figure X shows.
            // Equations, URLs, etc.
            boolean includeThisSentence = true;

            if (!isHeadline && !isAbstract) {
                includeThisSentence = de.acoli.informatik.uni.frankfurt.de.util.Utility.isNarrativeSentence(sentence.toString());
            }

            if (includeThisSentence) {
                // Check if we have at least two occurrences of citation marker.
                if ((sentenceStr.length() - sentenceStr.replace(Utility.CITATION_MARKER, "").length() >= 2) || sentenceStr.contains(" et al")) {
                    // Sentence with citation reference.
                    citationSentence = true;
                    //System.out.println("cit>:  " + sentenceStr);
                } else {
                    // Normal narrative sentence.
                    //System.out.println("nocit>: " + sentenceStr);
                }
                if (!isHeadline) {
                    // 1.) Remove adverbials and/or conjunctions at the beginning of the sentence.
                    // TODO: Horribly inefficient. Replace by better search over strings.
                    for (String aStart : sentenceStarters.keySet()) {
                        aStart = aStart.trim();
                        // Skip these.
                        if (aStart.contains("year") || aStart.contains("decade")) {
                            continue;
                        }

                        if (sentenceStr.startsWith(aStart + ",")) {
                            
                            sentenceStr = sentenceStr.substring(aStart.length() + 1).trim();
                            break;
                        } 
                        else if (sentenceStr.startsWith(aStart + " ")
                                && (!(sentenceStr.startsWith(aStart + " " + "to") || (sentenceStr.startsWith(aStart + " " + "of"))))) {
                            // Remove beginning.
                            sentenceStr = sentenceStr.substring(aStart.length() + 1).trim();
                            // Capitalize first char.
                            if (sentenceStr.length() > 0) {
                                if (sentenceStr.charAt(0) == ',') {
                                    sentenceStr = sentenceStr.substring(1).trim();
                                } else {
                                    if (sentenceStr.charAt(1) != ' ') {
                                        sentenceStr = Character.toUpperCase(sentenceStr.charAt(0)) + sentenceStr.substring(1).trim();
                                    } else {
                                        sentenceStr = Character.toUpperCase(sentenceStr.charAt(0)) + " " + sentenceStr.substring(1).trim();
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if (sentenceStr.length() > 1) {
                        if (sentenceStr.charAt(1) == ' ') {
                            sentenceStr = Character.toUpperCase(sentenceStr.charAt(0)) + " " + sentenceStr.substring(1).trim();
                        } else {
                            sentenceStr = Character.toUpperCase(sentenceStr.charAt(0)) + sentenceStr.substring(1).trim();
                        }
                    }
                }

                // Split all cases where two sentences are accidentally merged.
                // e.g., "electrodes.After electrochemical", "transportation.LIBs"
                String faultySentencebreakRegex = "\\.([A-Z])";
                Pattern pattern = Pattern.compile(faultySentencebreakRegex);
                Matcher matcher = pattern.matcher(sentenceStr);
                while (matcher.find()) {
                    String boundary = matcher.group(0);
                    String firstChar = matcher.group(1);
                    //System.out.println("faulty sentence boundary match: " + boundary);
                    sentenceStr = sentenceStr.replace(boundary, ". " + firstChar);
                }

                // // Convert [~CR4~–~CR8~] into explicit [~CR4~, ~CR5~, ~CR6~, ~CR7~, ~CR8~]
                // // in order to avoid problems later with normalized CRs.
                // String crRangeRegex
                        // = Utility.CITATION_MARKER + "CR(\\d+)" + Utility.CITATION_MARKER + "–" + Utility.CITATION_MARKER + "CR(\\d+)" + Utility.CITATION_MARKER;
                // Pattern crRangePattern = Pattern.compile(crRangeRegex);
                // Matcher crRangeMatcher = crRangePattern.matcher(sentenceStr);
                // while (crRangeMatcher.find()) {
                    // //System.out.println(sentenceStr);
                    // String completeMatch = crRangeMatcher.group(0);
                    // int firstCrId = Integer.parseInt(crRangeMatcher.group(1));
                    // int lastCrId = Integer.parseInt(crRangeMatcher.group(2));
                    // //System.out.println(completeMatch + ": " + firstCrId + " " + lastCrId);
                    // if (lastCrId < firstCrId) {
                        // System.err.println("There is something wrong with this CR range id: " + completeMatch);
                    // }
                    // // Compute CRs in range.
                    // // E.g., 
                    // // ~CR12~–~CR14~: 12 14 
                    // // turned into:
                    // // ~CR12~, ~CR13~, ~CR14~
                    // StringBuilder crRangeBuilder = new StringBuilder();
                    // for (int crR = firstCrId; crR <= lastCrId; crR++) {
                        // crRangeBuilder.append(Utility.CITATION_MARKER + "CR" + crR + Utility.CITATION_MARKER);
                        // if (crR < lastCrId) {
                            // crRangeBuilder.append(",");
                        // }
                    // }
                    // // Replace the original range notation by the expanded crs.
                    // String expandedCrs = crRangeBuilder.toString();
                    // sentenceStr = sentenceStr.replace(completeMatch, expandedCrs);
                    // // the problems of solubility
                // }
				
				sentenceStr = replaceRange(sentenceStr);

                // 2.) Rerun pos tagging.
                sentenceStr = smoothToBookSentences(sentenceStr);
                
                document = new Annotation(sentenceStr);
                sentenceSplitterTokenizerPipeline.annotate(document);
                List<CoreMap> sentencesRerun = document.get(SentencesAnnotation.class);
                for (CoreMap sentenceRerun : sentencesRerun) {

                    // 1. Detect bracket structure in sentence.
                    ArrayList<String> matches = new ArrayList<String>();
                    matchBrackets(sentenceStr, matches, "\\(.+?\\)");
                    matchBrackets(sentenceStr, matches, "\\[.+?\\]");
                    //System.out.println("\t" + matches);

                    if (sentenceRerun.toString().contains("(") && !sentenceRerun.toString().contains(")")) {
                        //System.out.println(sentenceStr);
                    }
                    if (!sentenceRerun.toString().contains("(") && sentenceRerun.toString().contains(")")) {
                        //System.out.println(sentenceStr);
                    }

                    ArrayList<String> tokensInSent = new ArrayList<>();
                    int tokNum = 0;
                    int tokNumWOBracket = -1;
                    for (CoreLabel token : sentenceRerun.get(TokensAnnotation.class)) {
                        // this is the text of the token
                        String word = token.get(TextAnnotation.class);
                        if(word.contains("CR2848")){
                            System.err.println("word found!!!!####: "+word);
                        }
                        if(word.contains("CR002847")){
                            System.err.println("word found!!!!####: "+word);
                        }
                        if(word.contains("CR2847")){
                            System.err.println("word found!!!!####: "+word);
                            System.err.println("sentence: "+sentenceRerun.get(TokensAnnotation.class).toString());
                        }
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                        String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                        if (ner == null) {
                            ner = "-";
                        }

                        int beginOffset = token.beginPosition();
                        int endOffset = token.endPosition();
                        //System.out.println(beginOffset + " " + endOffset);

                        // 2. Remove the bracket content from the sentence, including citations and store it externally.
                        // Do not include part of brackets.
                        String bracket = tokenIsPartOfBracket(matches, beginOffset, endOffset); // n=normal, ok.  // b=bracket, ok. 
                        // 
                        boolean isBracketContent = "b".equals(bracket);
                        String afterTokWOBracket = "-";
                        if (isBracketContent) {
                            afterTokWOBracket = String.valueOf(">" + tokNumWOBracket);
                        } else {
                            tokNumWOBracket++;
                        }

                        String tokNumWOBracketStr = String.valueOf(tokNumWOBracket);
                        if (isBracketContent) {
                            tokNumWOBracketStr = "-";
                        }

                        // no pipes allowed. 
                        word = word.replace("|", "/");

                        //  "word pos lemma ner begOff endOff tokNum sentId isBracket tokNumWOBracket afterTokWOBracket"
                        // tokNumWOBracket -> tokens to be restructured.
                        // afterTokWOBracket -> index AFTER which content should be inserted again.
                        tokensInSent.add(word + "|" + pos + "|" + lemma + "|" + ner + "|"
                                + beginOffset + "|" + endOffset + "|" + tokNum + "|" + sic.getId() + "|" + bracket
                                + "|" + tokNumWOBracketStr + "|" + afterTokWOBracket + "|" + "-"
                        );

                        tokNum++;

                    }

                    // System.out.println();
                    if (tokensInSent.size() > 150) {
                        // Do nothing. Sentence too long.
                    } else {

                        //Disabled currently !!!
                        if (citationSentence) {
                            //System.out.println("Citation sentence: " + sentenceStr);
                            // Replace a++ specific "CR" references by their dois in the text.
                            HashMap<String, ArrayList<String>> bibcranddois = aPub.getBibliography();
                            for (int t = 0; t < tokensInSent.size(); t++) {
                                for (String aCrfref : bibcranddois.keySet()) {
                                    // Found CR in sentence.
                                    if (tokensInSent.get(t).split("\\|")[0].equals(aCrfref)) {
                                        // Check if we find a doi.
                                        String doi = bibcranddois.get(aCrfref).get(0);
                                        String begOff = tokensInSent.get(t).split("\\|")[BEG_OFFSET_IDX];
                                        String endOff = tokensInSent.get(t).split("\\|")[END_OFFSET_IDX];
                                        tokNum = Integer.parseInt(tokensInSent.get(t).split("\\|")[TOK_NUM_IDX]);
                                        String bracket = tokensInSent.get(t).split("\\|")[BRACKET_INFO_IDX];
                                        String tokNumWOBracketStr = tokensInSent.get(t).split("\\|")[TOK_IDX_WO_BRACKET];
                                        String afterTokWOBracket = tokensInSent.get(t).split("\\|")[AFTER_TOK_WO_BRACKET_IDX];

                                        if (doi.equals(APlusPlusCollectionsReader.NO_DOI_MARKER)) {
                                            //System.err.println("Sorry, found " + aCrfref + " in sentence, but no DOI in A++ bibliography.");
                                        } else {
                                            // Replace it by the doi.
                                            //    tokensInSent.set(t, doi + "|" + "DOI" + "|" + "NA" + "|" + "-" + "|" 
                                            //            + begOff + "|" + endOff + "|" + tokNum + "|" + sic.getId() + "|" + bracket
                                            //    + "|" + tokNumWOBracketStr + "|" + afterTokWOBracket 
                                            //    );
                                        }

                                    }
                                }
                            }
                            
                        } else {
                            
                        }

                        for (int t = 0; t < tokensInSent.size(); t++) {
                            int toklen = tokensInSent.get(t).split("\\|").length;
                            if (toklen != NUM_TOKEN_ANNOTATION_ELEMENTS) {
                                System.err.println("Error: tok len != " + NUM_TOKEN_ANNOTATION_ELEMENTS + " for token: " + tokensInSent.get(t));
                                System.exit(0);
                            }
                        }

                        allSentences.add(tokensInSent);
                        sic.increment(); // increment sentence id.
                    }

                    // add stems.
                    //ArrayList<String> stems = porter.completeStem(tokensInSent);
                    //rval.add(stems);
                }

            } else {
                //System.out.println("Removing: " + sentence);
            }

        }

        if (allSentences.isEmpty()) {
            System.err.println("Warning: No sentences were added for node \"" + text + "\"");
        }

        return allSentences;
    }

    public static ArrayList<String> matchBrackets(String sentence, ArrayList<String> collectedMatches, String patternStr) {
        // String patternStr = "\\(.+?\\)";
        // String patternStr = "\\[.+?\\]";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(sentence);
        while (matcher.find()) {
            String match = matcher.group(0);
            // Check if matches does not already contain a span with that range.
            int start = matcher.start();
            int end = matcher.end();
            boolean ok = true;
            for (int m = 0; m < collectedMatches.size(); m++) {
                String alreadyCollectedMatch = collectedMatches.get(m);
                int alreadyStartIdx = Integer.parseInt(alreadyCollectedMatch.split(":")[0].split("-")[0]);
                int alreadyEndIdx = Integer.parseInt(alreadyCollectedMatch.split(":")[0].split("-")[1]);
                if (alreadyStartIdx <= start && start < alreadyEndIdx) {
                    ok = false;
                } else if (alreadyStartIdx <= end && end <= alreadyEndIdx) {
                    ok = false;
                }
            }

            if (ok) {
                collectedMatches.add(start + "-" + end + ":" + match);
            } else {
                //System.err.println("Probably matching recursive bracket structure: " + match);
            }
        }
        return collectedMatches;
    }

    public static String tokenIsPartOfBracket(ArrayList<String> bracketMatches, int tokStart, int tokEnd) {
        String rval = "n";
        for (int m = 0; m < bracketMatches.size(); m++) {
            String alreadyCollectedMatch = bracketMatches.get(m);
            int alreadyStartIdx = Integer.parseInt(alreadyCollectedMatch.split(":")[0].split("-")[0]);
            int alreadyEndIdx = Integer.parseInt(alreadyCollectedMatch.split(":")[0].split("-")[1]);
            if (alreadyStartIdx <= tokStart && tokStart < alreadyEndIdx) {
                rval = "b";
            } else if (alreadyStartIdx <= tokEnd && tokEnd <= alreadyEndIdx) {
                rval = "b";
            }
        }
        return rval;
    }

    
    private String smoothToBookSentences(String sentenceStr) {
        if (sentenceStr.startsWith("Section")) {
            sentenceStr = sentenceStr.replace("Section", "This chapter");
        }  
        return sentenceStr.replace(" section", " chapter").replaceAll("\\s+", " ");
    }
	
	
	public static String replaceRange(String rangestr) {
		String sentenceStr = rangestr;
		String crRangeRegex
                        = Utility.CITATION_MARKER + "CR(\\d+)(_\\d+)*" + Utility.CITATION_MARKER + "–" + Utility.CITATION_MARKER + "CR(\\d+)(_\\d+)*" + Utility.CITATION_MARKER;
		Pattern crRangePattern = Pattern.compile(crRangeRegex);
		Matcher crRangeMatcher = crRangePattern.matcher(sentenceStr);
		while (crRangeMatcher.find()) {
			//System.out.println(sentenceStr);
			String completeMatch = crRangeMatcher.group(0);
			//System.out.println("complete match: "+completeMatch+" "+"org str: "+rangestr+" crrangeregex: "+crRangeRegex);
			//System.out.println("grou 1: "+crRangeMatcher.group(1));
			//System.out.println("grou 3: "+crRangeMatcher.group(3));
			
			String beforeInt = "";
			String afterUnderscore = "";
			String group1 = crRangeMatcher.group(1);
			String group2 = crRangeMatcher.group(2);
			String group3 = crRangeMatcher.group(3);
			String group4 = crRangeMatcher.group(4);
			
			if(group1.startsWith("0")){
				for (int i = 0; i < group1.length(); i++){
					char c = group1.charAt(i);
					if (c == '0'){
						beforeInt+="0";
					}
					else break;
				}						
			}
								//if(group2 != null){
			//StringBuilder crRangeBuilder = new StringBuilder();
								//    System.out.println("Contains _ !! "+group2);
								//    int firstCrId = Integer.parseInt(group2.split("_")[1]);
								//    //TODO: increment both before and after _ if necessary!
								//    System.out.println("integer: "+firstCrId);
								//}
			//else {

			int firstCrId = Integer.parseInt(crRangeMatcher.group(1));
			int lastCrId = Integer.parseInt(crRangeMatcher.group(3));
			//System.out.println(completeMatch + ": " + firstCrId + " " + lastCrId);
			if (lastCrId < firstCrId) {
				System.err.println("There is something wrong with this CR range id: " + completeMatch);
			}
			// Compute CRs in range.
			// E.g., 
			// ~CR12~–~CR14~: 12 14 
			// turned into:
			// ~CR12~, ~CR13~, ~CR14~
			StringBuilder crRangeBuilder = new StringBuilder();
			for (int crR = firstCrId; crR <= lastCrId; crR++) {
				crRangeBuilder.append(Utility.CITATION_MARKER + "CR" + beforeInt + crR + Utility.CITATION_MARKER);
				if (crR < lastCrId) {
					crRangeBuilder.append(",");
				}
			}

			// Replace the original range notation by the expanded crs.
			String expandedCrs = crRangeBuilder.toString();
			sentenceStr = sentenceStr.replace(completeMatch, expandedCrs);
			// the problems of solubility
		}

	return sentenceStr;
}
//for testing; _ does not work yet
/* public static void main(String[] args){
	String s = replaceRange("~CR12~–~CR14~");
System.out.println(s);	
String s1 = replaceRange("~CR0012~–~CR0014~");
System.out.println(s1);
String s2 = replaceRange("~CR0012_10~–~CR0014_5~");
System.out.println(s2);
} */
}
