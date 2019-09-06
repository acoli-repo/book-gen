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
package de.acoli.informatik.uni.frankfurt.de.main;

import de.acoli.informatik.uni.frankfurt.de.demos.AllSectionSentencesAggregator;
import de.acoli.informatik.uni.frankfurt.de.demos.BracketContentReintroducerLocalBibGenerator;
import de.acoli.informatik.uni.frankfurt.de.demos.BySynonymReplacer;
import de.acoli.informatik.uni.frankfurt.de.demos.NgramsExtractor;
import de.acoli.informatik.uni.frankfurt.de.demos.StubFiller;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;
import org.jbibtex.ParseException;
import org.xml.sax.SAXException;
import de.acoli.informatik.uni.frankfurt.de.reader.APlusPlusToJSONConverter;
import de.acoli.informatik.uni.frankfurt.de.reader.ChapterBibliographyMaker;
import de.acoli.informatik.uni.frankfurt.de.reader.ParsedSentencesToFoldersCopier;

/**
 *
 * Prototype implementation of "Beta Writer".
 * The main interface to the program.
 * 
 * @author Niko
 */
public class Main {

    public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, ScriptException, FileNotFoundException, ParseException, URISyntaxException {
        String arg1 = args[0];
        switch (arg1) {
            case "A++2JSON":
                // Turn A++ corpus into JSON.
                APlusPlusToJSONConverter.main(new String[]{args[1], args[2]});
                break;
            case "SECTION_AGGREGATOR":
                // Aggretate introduction and conclusion sections.
                AllSectionSentencesAggregator.main(new String[]{args[1], args[2], args[3], args[4], args[5], args[6]});
                break;
            case "DISTRIBUTE_PARSED_CHUNKS":
                // Split sentences into portions.
                ParsedSentencesToFoldersCopier.main(new String[]{args[1]});
                break;
            case "REINTRODUCE_BRACKET_CONTENT":
                // Reintroduce the content of parentheses after sententes were syntactically parsed.
                BracketContentReintroducerLocalBibGenerator.main(new String[]{args[1], args[2]});
                break;
            case "CHAPTER_BIBLIOGRAPHY_MAKER":
                // Generate a bibliographies.
                ChapterBibliographyMaker.main(new String[]{args[1], args[2]});
                break;
            case "STUB_FILLER":
                StubFiller.main(new String[]{args[1], args[2], args[3], args[4], args[5]});
                break;
            case "COLLECT_NGRAMS":
                // Compute n-grams
                NgramsExtractor.main(new String[]{args[1]});
                break;
            case "REPLACE_SYNONYMS":
                // Substitute n-grams by synonymous expressions.
                BySynonymReplacer.main(new String[]{args[1], args[2]});
                break;
            default:
                System.out.println("Erroneous argument: " + arg1);
                System.exit(0);
                break;
        }
    }
}
