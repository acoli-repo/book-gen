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
package de.acoli.informatik.uni.frankfurt.de.aplusplus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Publication object is a lightweight representation of an A++ file,
 * containing section headings, title, meta data, etc.
 * 
 * @author niko
 */
public class Publication {

    private String aPlusPlusID; // i.e. filename
    private int docID; // an index, typically a number.
    private String title;
    public String domain;
    private String doi;
    private String articleDOI;
    private String bookDOI;
    private String chapterDOI;

    // Meta data.
    private ArrayList<String> aPlusPlusKeywords;
    private ArrayList<String> associatedGlobalKeywords;
    private String associatedClusterID;
    public ArrayList<String> headlines;

    // Both headings and sections correspond to each other.
    private ArrayList<String> bodySectionHeadings;
    private ArrayList<String> bodySections;
    private ArrayList<ArrayList<String>> bodySectionKeywords;

    private HashMap<String, ArrayList<String>> citationIdToDOIandBibunstructured;

    public ArrayList<ArrayList<String>> abstrTokens;
    public ArrayList<ArrayList<String>> titleTokens;
    public ArrayList<ArrayList<ArrayList<String>>> bodySectionsHeadingsTokens;

    public ArrayList<ArrayList<String>> introductionTokens;
    public ArrayList<ArrayList<String>> relatedWorkTokens;
    public ArrayList<ArrayList<String>> materialsAndMethodsTokens;
    public ArrayList<ArrayList<String>> discussionTokens;
    public ArrayList<ArrayList<String>> resultsTokens;
    public ArrayList<ArrayList<String>> conclusionTokens;
    // multiple sections, multiple sentences, multiple tokens.
    public ArrayList<ArrayList<ArrayList<String>>> bodySectionsTokens;

    public Publication() {
        doi = "-";
        articleDOI = "-";
        bookDOI = "-";
        chapterDOI = "-";

        docID = -1;
        domain = "";
        aPlusPlusID = "";
        title = "";
        titleTokens = new ArrayList<>();
        headlines = new ArrayList<>();
        bodySectionHeadings = new ArrayList<>();
        bodySections = new ArrayList<>();
        bodySectionKeywords = new ArrayList<>();
        abstrTokens = new ArrayList<>();
        bodySectionsHeadingsTokens = new ArrayList<>();

        introductionTokens = new ArrayList<>();
        relatedWorkTokens = new ArrayList<>();
        conclusionTokens = new ArrayList<>();
        materialsAndMethodsTokens = new ArrayList<>();
        discussionTokens = new ArrayList<>();
        resultsTokens = new ArrayList<>();
        bodySectionsTokens = new ArrayList<>();

        citationIdToDOIandBibunstructured = new HashMap<String, ArrayList<String>>();

    }

    public void setArticleDoi(String aDoi) {
        this.articleDOI = aDoi;
    }

    public String getArticleDoi() {
        return this.articleDOI;
    }

    public void setBookDoi(String aDoi) {
        this.bookDOI = aDoi;
    }

    public String getBookDoi() {
        return this.bookDOI;
    }

    public void setChapterDoi(String aDoi) {
        this.chapterDOI = aDoi;
    }

    public String getChapterDoi() {
        return this.chapterDOI;
    }

    public void setDocId(int anId) {
        this.docID = anId;
    }

    public int getDocId() {
        return this.docID;
    }

    public void setAPlusPlusKeywords(ArrayList<String> keywords) {
        this.aPlusPlusKeywords = keywords;
    }

    public ArrayList<String> getAPlusPlusKeywords() {
        return this.aPlusPlusKeywords;
    }

    public void setKeywords(ArrayList<String> keywords) {
        this.associatedGlobalKeywords = keywords;
    }

    public ArrayList<String> getKeywords() {
        return this.associatedGlobalKeywords;
    }

    public void setCluster(String aCluster) {
        this.associatedClusterID = aCluster;
    }

    public String getCluster() {
        return this.associatedClusterID;
    }

    /**
     *
     * @param anAplusPlusID, typically something like "xkj7sdf.xml"
     */
    public void setAPlusPlusID(String anAplusPlusID) {
        this.aPlusPlusID = anAplusPlusID;
    }

    public String getAPlusPlusID() {
        if (aPlusPlusID.contains(File.separator)) {
            return aPlusPlusID.substring(aPlusPlusID.lastIndexOf(File.separator));
        }
        return aPlusPlusID;
    }

    public void addTitle(String aTitle) {
        this.title = aTitle;
    }

    public String getTitle() {
        return title;
    }

    public void addBodySectionKeywords(ArrayList<ArrayList<String>> someBodySectionKeywords) {
        this.bodySectionKeywords = someBodySectionKeywords;
    }

    public ArrayList<ArrayList<String>> getBodySectionKeywords() {
        return bodySectionKeywords;
    }

    public ArrayList<String> getBodySectionHeadings() {
        return bodySectionHeadings;
    }

    public ArrayList<String> getBodySections() {
        return bodySections;
    }

    public HashMap<String, ArrayList<String>> getBibliography() {
        return citationIdToDOIandBibunstructured;
    }

    public void addBibliography(HashMap<String, ArrayList<String>> aCitationIdToDOIandBibunstructured) {
        this.citationIdToDOIandBibunstructured = aCitationIdToDOIandBibunstructured;
    }

    public void addSectionHeading(String aBodySectionHeading) {
        this.bodySectionHeadings.add(aBodySectionHeading);
    }

    public void addSection(String aBodySection) {
        this.bodySections.add(aBodySection);
    }

    /**
     *
     * @return a string representation of this publication
     */
    public String toString() {
        return "A++ ID: " + aPlusPlusID + "\n"
                + "Title: " + title + "\n";

    }

    public String getDoi() {
        String artDoi = getArticleDoi();
        String bookDoi = getBookDoi();
        String chapDoi = getChapterDoi();
        if (artDoi.length() > 2) {
            doi = artDoi;
        }
        if (bookDoi.length() > 2) {
            doi = bookDoi;
        }
        if (chapDoi.length() > 2) {
            doi = chapDoi;
        }
        return doi;
    }

}
