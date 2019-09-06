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

import de.acoli.informatik.uni.frankfurt.de.aplusplus.Publication;
import de.acoli.informatik.uni.frankfurt.de.util.SentenceSplitter;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.IntTuple;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;
import static de.acoli.informatik.uni.frankfurt.de.util.Utility.listAPlusPlusFilesForFolder;
import static de.acoli.informatik.uni.frankfurt.de.util.Utility.removeSelectedTags;
import static de.acoli.informatik.uni.frankfurt.de.util.Utility.getSentencesTokensAndAnnotations;

/**
 *
 * Reads in text content from A++ documents and associates it to section types,
 * e.g., abstract, intro, related work, conclusion, bibliography.
 *
 * All other remaining sections are part of "body sections".
 *
 * @author niko
 */
public class APlusPlusCollectionsReader {

    // The input directory containing your A++ files.
    public static String APLUSPLUS_FILES_INPUT_DIR = "aplusplusdocs/";

    // Set to true if you want to detect coreferential expressions.
    public static final boolean RESOLVE_COREFERENTIAL_EXPRESSIONS = false;

    public static SentIDCounter sentIdCounter = null;
    public static int MAX_DOCUMENT_COLLECTION_SIZE = 8000;
    public static ArrayList<Publication> pubs;
    public static String NO_DOI_MARKER = "null";

    public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, ScriptException {
        if (args.length != 0) {
            APLUSPLUS_FILES_INPUT_DIR = args[0];
        }
        ArrayList<Publication> publications = getPublications(APLUSPLUS_FILES_INPUT_DIR, MAX_DOCUMENT_COLLECTION_SIZE);
        System.out.println(publications.get(0));
    }

    /**
     * Read in a list of scientific papers from a directory containing A++ files.
     *
     * @param APlusPlusStore
     * @param maxNumDocs
     * @return a list of Publication objects
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws ScriptException
     */
    public static ArrayList<Publication> getPublications(String APlusPlusStore, int maxNumDocs)
            throws IOException, ParserConfigurationException, ScriptException {
        System.err.println("Reading in A++ collection. Limit set to " + maxNumDocs);

        int docsCollected = 0;

        pubs = new ArrayList<>();

        ArrayList<String> files = new ArrayList<String>();
        final File folder = new File(APlusPlusStore);
        listAPlusPlusFilesForFolder(folder, files, maxNumDocs);

        System.out.println("Text extraction...");
        for (String f : files) {
            sentIdCounter = new SentIDCounter(0);

            Publication aPub = new Publication();
            System.out.print(docsCollected + ":" + f);
            aPub.setAPlusPlusID(f);

            File fXmlFile = new File(f);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            // So that we don't download the DTD at devel.springer.de
            // Credits:
            // https://stackoverflow.com/questions/155101/make-documentbuilder-parse-ignore-dtd-references
            dbFactory.setValidating(false);
            dbFactory.setNamespaceAware(true);
            dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            dbFactory.setFeature("http://xml.org/sax/features/validation", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc;
            try {
                doc = dBuilder.parse(fXmlFile);
            } catch (SAXException ex) {
                System.err.println("Masked XML not valid! Skipping! (Potential error in maskentities.py!)");
                continue;
            }
            doc.getDocumentElement().normalize();

            Element domain = (Element) doc.getElementsByTagName("SubjectCollection").item(0);
            if (domain != null) {
                //System.out.println("domain: " + domain.getTextContent());
                
                // SubjectCollections "SC#" are aliases:
                // either
                //<SubjectCollection Code="SC11">Medicine</SubjectCollection>
                // or
                //<SubjectCollection Code="Medicine">SC11</SubjectCollection>
                String aDomain = domain.getTextContent();
                switch (aDomain) {
                    case "SC3":
                        aDomain = "Biomedical and Life Sciences";
                        break;
                    case "SC4":
                        aDomain = "Business and Economics";
                        break;
                    case "SC5":
                        aDomain = "Chemistry and Materials Science";
                        break;
                    case "SC7":
                        aDomain = "Earth and Environmental Science";
                        break;
                    case "SC8":
                        aDomain = "Engineering";
                        break;
                    case "SC9":
                        aDomain = "Humanities";
                        break;
                    case "SC10":
                        aDomain = "Mathematics and Statistics";
                        break;
                    case "SC11":
                        aDomain = "Medicine";
                        break;
                    case "SC12":
                        aDomain = "Physics and Astronomy";
                        break;
                    default:
                        break;
                }
                aPub.domain = aDomain;
            } else {
                aPub.domain = "NA";
            }

            Element copyrightyear = (Element) doc.getElementsByTagName("CopyrightYear").item(0);
            if (copyrightyear != null) {
                System.out.println(" " + copyrightyear.getTextContent());
            } else {
                System.out.println(" year NA.");
            }

            // Get the keywords.
            Element keywords = (Element) doc.getElementsByTagName("KeywordGroup").item(0);
            if (keywords != null) {
                String[] keywordsSplit = keywords.getTextContent().trim().split("\\n");
                ArrayList<String> keywordsList = new ArrayList<>(Arrays.asList(keywordsSplit));
                ArrayList<String> keywordsListCleaned = new ArrayList<>();
                for (int i = 1; i < keywordsList.size(); i++) {
                    keywordsListCleaned.add(keywordsList.get(i).replaceAll("\\s+", " ").trim());
                }
                //System.out.println("keywords from A++ doc: " + keywordsListCleaned);
                aPub.setAPlusPlusKeywords(keywordsListCleaned);
            }

            // Get the abstract.
            NodeList abstrNodeList = doc.getElementsByTagName("Abstract");
            if (abstrNodeList.getLength() > 0) {
                Node abstrNode = abstrNodeList.item(0);
                removeSelectedTags(abstrNode);
                aPub.abstrTokens = getSentencesTokensAndAnnotations(abstrNode, aPub, sentIdCounter);
                applyCoreference(aPub.abstrTokens);
            }

            String titleTagName = "UNDEFINED";
            Element article = (Element) doc.getElementsByTagName("Article").item(0);
            Element chapter = (Element) doc.getElementsByTagName("Chapter").item(0);
            Element book = (Element) doc.getElementsByTagName("Book").item(0);
            if (article != null) {
                titleTagName = "ArticleTitle";
            } else if (chapter != null) {
                titleTagName = "ChapterTitle";
            } else if (book != null) {
                System.err.println("It's a BibBook");
                titleTagName = "BookTitle";
            }

            // Handle DOIs.
            Element articleDOI = (Element) doc.getElementsByTagName("ArticleDOI").item(0);
            Element chapterDOI = (Element) doc.getElementsByTagName("ChapterDOI").item(0);
            Element bookDOI = (Element) doc.getElementsByTagName("BookDOI").item(0);
            if (articleDOI != null) {
                aPub.setArticleDoi(articleDOI.getTextContent());
            } else if (chapterDOI != null) {
                aPub.setChapterDoi(chapterDOI.getTextContent());
            } else if (bookDOI != null) {
                aPub.setBookDoi(bookDOI.getTextContent());
            }

            if (articleDOI == null && chapterDOI == null && bookDOI == null) {
                System.err.println("No DOI found.");
            }

            Element title = (Element) doc.getElementsByTagName(titleTagName).item(0);
            if (title != null) {
                String titleStr = title.getTextContent().replaceAll("\\s+", " ").trim();
                if (titleStr.equals("")) {
                    titleStr = "NA";
                }
                aPub.addTitle(titleStr);

                // "narrative" title sentences. We don't expect citations in the title.
                ArrayList<ArrayList<String>> tit = getSentencesTokensAndAnnotations(title, aPub, sentIdCounter);
                if (tit.size() == 0) {
                    System.err.println("No title found.");
                    ArrayList<String> tok = new ArrayList<>();
                    tok.add("NoTitle");
                    aPub.titleTokens.add(tok);
                } else {
                    aPub.titleTokens = tit;
                }
            } else {
                System.err.println("Sorry, no title found for: " + f);
            }

            Element bibliography = (Element) doc.getElementsByTagName("Bibliography").item(0);
            // CRX to DOI & BibUnstructured
            HashMap<String, ArrayList<String>> bibcrtodoiandunstr = new HashMap<String, ArrayList<String>>();
            if (bibliography != null) {
                // Set the citation ids, DOIs and bibunstructureds.
                // Get all Citation nodes.
                NodeList citationNodes = bibliography.getElementsByTagName("Citation");
                for (int cit = 0; cit < citationNodes.getLength(); cit++) {
                    Element aCit = (Element) citationNodes.item(cit);
                    String citationID = aCit.getAttribute("ID");
                    //System.out.println("ID: " + aCit.getAttribute("ID"));
                    ArrayList<String> doiAndBibunstr = new ArrayList<>(2);
                    // Two placeholders.
                    doiAndBibunstr.add("-");
                    doiAndBibunstr.add("-");
                    // Get DOI & Bibunstructured:
                    NodeList occurrences = aCit.getElementsByTagName("Occurrence");
                    Element bibunstructured = (Element) aCit.getElementsByTagName("BibUnstructured").item(0);

                    boolean foundDOI = false;

                    String bibXml = "";
                    if (bibunstructured != null) {
                        String innerXML1 = innerXml(aCit.getElementsByTagName("BibUnstructured").item(0));
                        //System.out.println(innerXML1);
                        bibXml = innerXML1.replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("\\s+", " ").replaceAll("\\t", " ");
                        bibXml = bibXml.replace("<Subscript>", "<sub>");
                        bibXml = bibXml.replace("</Subscript>", "</sub>");
                        bibXml = bibXml.replace("<Superscript>", "<sup>");
                        bibXml = bibXml.replace("</Superscript>", "</sup>");

                        String doiPattern = "<RefTarget Address=\"(.+?)\"\\s+TargetType=\"DOI\"";
                        Pattern r = Pattern.compile(doiPattern);
                        Matcher m = r.matcher(bibXml);
                        if (m.find()) {
                            String completematch = m.group();
                            String doiStr = m.group(1);
                            doiAndBibunstr.set(0, doiStr);
                            foundDOI = true;
                        }
                        doiAndBibunstr.set(1, bibXml);
                    }

                    // Cf. http://devel.springer.de/A++/V2.4/DTD/
                    // <!ELEMENT Occurrence (Handle | URL)>
                    // <!ATTLIST Occurrence
                    //	Type (DOI | COI | PID | ZLBID | AMSID | URL | ISIID | Bibcode | PMCID) #REQUIRED
                    // >
                    for (int occ = 0; occ < occurrences.getLength(); occ++) {
                        Element occurrence = (Element) occurrences.item(occ);
                        if (occurrence != null) {
                            if (occurrence.hasAttribute("Type")) {
                                if (occurrence.getAttribute("Type").equals("DOI")) {
                                    Element handle = (Element) occurrence.getElementsByTagName("Handle").item(0);
                                    if (handle != null) {
                                        String doiStr = handle.getTextContent().trim();
                                        doiAndBibunstr.set(0, doiStr);
                                        foundDOI = true;
                                    }
                                }
                            }
                        }
                    }

                    if (!foundDOI) {
                        //System.out.println("No doi found for prev ref.");
                        doiAndBibunstr.set(0, NO_DOI_MARKER);
                    }

                    if (!foundDOI && bibXml.contains("doi")) {
                        //System.out.println("Found \"DOI\" in bibunstructured string but obviously no markup for it: " + bibXml);
                    }
                    bibcrtodoiandunstr.put(citationID, doiAndBibunstr);
                }
            }
            if (bibcrtodoiandunstr.keySet().isEmpty()) {
                System.err.println("Warning: No citation references found for bibliography.");
            }
            aPub.addBibliography(bibcrtodoiandunstr);

            // Get the Body tag.
            Element body = (Element) doc.getElementsByTagName("Body").item(0);
            if (body != null) {
                NodeList nodeList = body.getElementsByTagName("*");
                boolean foundAtLeastOneSection = false;
                for (int i = 0; i < nodeList.getLength(); i++) {
                    boolean added = false;
                    Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        String nodeName = node.getNodeName();

                        if (nodeName.startsWith("Section1")) {
                            foundAtLeastOneSection = true;
                            Element el = (Element) node;
                            i++;
                            Node nextNode = nodeList.item(i);
                            Node headlineNode = null;
                            String headline = "NO_HEADING";
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                String nextNodeName = nextNode.getNodeName();
                                if (nextNodeName.equals("Heading")) {
                                    headlineNode = nextNode;
                                    headline = nextNode.getTextContent().replaceAll("\\s+", " ").trim();//.toLowerCase();
                                    aPub.headlines.add(headline);
                                    // Level-1 heading.
                                }
                            }
                            removeSelectedTags(node);
                            //String textContent = getTextContent(node, headline);
                            ArrayList<ArrayList<String>> sentsAndToks = getSentencesTokensAndAnnotations(node, aPub, sentIdCounter);

                            // Check markup which is already there.
                            // Currently, only Intro and Conclusion.
                            if (el.hasAttribute("Type")) {
                                String sectionType = el.getAttribute("Type");
                                if (sectionType.equals("Introduction")) {
                                    aPub.introductionTokens = sentsAndToks;
                                    applyCoreference(aPub.introductionTokens);
                                    added = true;
                                } else if (sectionType.equals("Conclusion")) {
                                    aPub.conclusionTokens = sentsAndToks;
                                    applyCoreference(aPub.conclusionTokens);
                                    added = true;
                                } else if (sectionType.equals("MaterialsAndMethods")) {
                                    aPub.materialsAndMethodsTokens = sentsAndToks;
                                    added = true;
                                } else if (sectionType.equals("Discussion")) {
                                    aPub.discussionTokens = sentsAndToks;
                                    added = true;
                                } else if (sectionType.equals("Results")) {
                                    aPub.resultsTokens = sentsAndToks;
                                    added = true;
                                }
                            }

                            // Use own heuristics to associate headings to sections.
                            if (headline.toLowerCase().contains("introduction") || headline.toLowerCase().contains("background")) {
                                aPub.introductionTokens = sentsAndToks;
                                if (!added) {
                                    applyCoreference(aPub.introductionTokens);
                                }
                                added = true;
                            } else if (headline.toLowerCase().contains("conclusion") || headline.toLowerCase().contains("summary")) {
                                aPub.conclusionTokens = sentsAndToks;
                                if (!added) {
                                    applyCoreference(aPub.conclusionTokens);
                                }
                                added = true;
                            } else if (headline.toLowerCase().contains("results")) {
                                aPub.resultsTokens = sentsAndToks;
                                added = true;
                            } else if (headline.toLowerCase().contains("discussion")) {
                                aPub.discussionTokens = sentsAndToks;
                                added = true;
                            } else if (headline.toLowerCase().contains("method") || headline.toLowerCase().contains("materials")) {
                                aPub.materialsAndMethodsTokens = sentsAndToks;

                                added = true;
                            } else if (headline.toLowerCase().contains("related ")
                                    || headline.toLowerCase().contains("background")
                                    || headline.toLowerCase().contains("literature")
                                    || headline.toLowerCase().contains("previous work")
                                    || headline.toLowerCase().contains("literature review")
                                    || headline.toLowerCase().contains("review of")
                                    || headline.toLowerCase().contains("overview")) {

                                aPub.relatedWorkTokens = sentsAndToks;
                                added = true;
                            }

                            // Add all remaining sections.
                            if (!added) {
                                aPub.addSectionHeading(headline);
                                if (!added) {
                                    applyCoreference(sentsAndToks); 
                                }
                                aPub.bodySectionsTokens.add(sentsAndToks);
                                aPub.bodySectionsHeadingsTokens.add(getSentencesTokensAndAnnotations(headlineNode, aPub, sentIdCounter));
                            }

                        }
                    }
                }
                if (!foundAtLeastOneSection) {
                    // Add at least the body content. (mostly applies to chapters).
                    aPub.addSectionHeading("BodySection");
                    System.err.println("No <Section> found. Adding only body text.");
                    removeSelectedTags(body);
                    aPub.bodySectionsTokens.add(getSentencesTokensAndAnnotations(body, aPub, sentIdCounter));
                }

            } else {
                System.err.println("No <Body> content found.");
            }

            // Do some sanity checks.
            if (aPub.bodySectionsHeadingsTokens.size() != aPub.getBodySectionHeadings().size()) {
                // Ignore this publication.
                System.err.println(aPub.bodySectionsHeadingsTokens.toString());
                System.err.println(aPub.bodySectionHeadings.toString());
                System.err.println("Something wrong in the # of body sections. Ignoring this A++ file.");
            } else {
                if (body != null) {
                    aPub.setDocId(docsCollected);
                    pubs.add(aPub);
                    docsCollected++;
                } else {
                    System.err.println("Ignoring document.");
                }
            }

        }
        //Collections.shuffle(pubs);
        System.out.println("Read in " + pubs.size() + " A++ documents.");
        return pubs;

    }

    private static void applyCoreference(ArrayList<ArrayList<String>> sentences) {
        if (RESOLVE_COREFERENTIAL_EXPRESSIONS) {
            // Specify all words which should be replaced by a coreferent element
            // in the prior context, e.g., "he" -> "Trump".
            ArrayList<String> personalPronouns = new ArrayList<String>();
            personalPronouns.add("he");
            personalPronouns.add("she");
            personalPronouns.add("it");
            personalPronouns.add("him");
            personalPronouns.add("her");
            personalPronouns.add("they");
            personalPronouns.add("them");
            ArrayList<String> possessivePronouns = new ArrayList<String>();
            possessivePronouns.add("its");
            possessivePronouns.add("their");
            possessivePronouns.add("his");
            possessivePronouns.add("her");

            // Add words which the system should NOT use as replacements.
            ArrayList<String> forbiddenReplacements = new ArrayList<String>();
            forbiddenReplacements.add("I");
            forbiddenReplacements.add("they");
            forbiddenReplacements.add("us");
            forbiddenReplacements.add("we");
            forbiddenReplacements.add("you");
            forbiddenReplacements.add("themselves");
            forbiddenReplacements.add("them");
            forbiddenReplacements.add("yourself");

            StringBuilder textBlock = new StringBuilder();
            for (ArrayList<String> aSentence : sentences) {
                for (String aToken : aSentence) {
                    textBlock.append(aToken.split("\\|")[0] + " ");
                }
                textBlock.append("\n");
            }

            // Run coref.
            Annotation corefDoc = new Annotation(textBlock.toString());
            // run all annotators on this text
            SentenceSplitter.coreferencePipeline.annotate(corefDoc);

            Collection<CorefChain> corefs = corefDoc.get(CorefCoreAnnotations.CorefChainAnnotation.class).values();
            if (corefs.size() > 0) {
                //System.out.println("Text block:");
                //System.out.println(textBlock.toString());
                for (CorefChain cc : corefs) {
                    //System.out.println("\t" + cc);
//                System.out.println(cc.getMentionMap());
//                Set<IntPair> keys = cc.getMentionMap().keySet();
//                for (IntPair k : keys) {
//                    int sentenceNumber = k.getSource();
//                    int tokenInSentence = k.getTarget(); // TODO: detect the span of this node.
//                    System.out.println(sentenceNumber);
//                    System.out.println(tokenInSentence);
//                    System.out.println(cc.getMentionMap().get(k));
//                }
                    for (CorefChain.CorefMention cm : cc.getMentionsInTextualOrder()) {
                        String textOfMention = cm.mentionSpan;
                        IntTuple positionOfMention = cm.position;
                        //System.out.print(textOfMention + " ");
                        int sentNum = positionOfMention.elems()[0];
                        int tokNum = cm.startIndex;
                        String possessiveMarker = "";
                        // We found a coreferent word, e.g. "it".
                        if (personalPronouns.contains(textOfMention.toLowerCase())
                                || possessivePronouns.contains(textOfMention.toLowerCase())) {

                            if (possessivePronouns.contains(textOfMention.toLowerCase())) {
                                possessiveMarker = "'s";
                            }
                            for (CorefChain.CorefMention cm2 : cc.getMentionsInTextualOrder()) {
                                String textOfMention2 = cm2.mentionSpan;
                                IntTuple positionOfMention2 = cm2.position;
                                int sentNum2 = positionOfMention2.elems()[0];
                                // Don't replace by the same pronoun.
                                if (!personalPronouns.contains(textOfMention2.toLowerCase())
                                        && !possessivePronouns.contains(textOfMention2.toLowerCase())
                                        && !forbiddenReplacements.contains(textOfMention2.toLowerCase())) {
                                    if (textOfMention2.length() < 90) {
                                        if (sentNum2 < sentNum) {
                                            // Add coreference item to this token.
                                            String tokMeta = sentences.get(sentNum - 1).get(tokNum - 1);
                                            // Get the last item after the last pipe of the token meta info.
                                            if (possessivePronouns.contains(textOfMention.toLowerCase()) && textOfMention2.endsWith("s")) {
                                                possessiveMarker = "'";
                                            }
                                            tokMeta = tokMeta.substring(0, tokMeta.length() - 1) + textOfMention2 + possessiveMarker;
                                            sentences.get(sentNum - 1).set(tokNum - 1, tokMeta);
                                            System.out.println("Replacing: " + textOfMention + " by " + textOfMention2 + possessiveMarker);
                                            //System.out.println("sentNum: " + (sentNum-1) + " and tokNum: " + (tokNum-1));
                                            //System.out.println(sentences.get(sentNum-1));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get Bibunstructured information with style tags, e.g., <Subscript>
     * Credits:
     * https://stackoverflow.com/questions/3300839/get-a-nodes-inner-xml-as-string-in-java-dom
     *
     * @param node
     * @return String of bibunstructured
     */
    public static String innerXml(Node node) {
        DOMImplementationLS lsImpl = (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer lsSerializer = lsImpl.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("xml-declaration", false);
        NodeList childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < childNodes.getLength(); i++) {
            sb.append(lsSerializer.writeToString(childNodes.item(i)));
        }
        return sb.toString();
    }
}
