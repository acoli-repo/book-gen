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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.xml.sax.SAXException;
import static de.acoli.informatik.uni.frankfurt.de.reader.APlusPlusCollectionsReader.getPublications;

/**
 *
 * Reads in a list of A++ files and exports selected content into JSON format.
 *
 * @author niko
 */
public class APlusPlusToJSONConverter {

    public static String APLUSPLUS_FILES_INPUT_DIR = "aplusplusdocs/";
    public static String EXPORT_JSON = "gen/corpus.json";

    public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, ScriptException {

        if (args.length == 2) {
            APLUSPLUS_FILES_INPUT_DIR = args[0];
            EXPORT_JSON = args[1];
        }
        ArrayList<Publication> publications = getPublications(APLUSPLUS_FILES_INPUT_DIR, APlusPlusCollectionsReader.MAX_DOCUMENT_COLLECTION_SIZE);
        aPlusPlusToJSON(publications);

    }

    public static void aPlusPlusToJSON(ArrayList<Publication> pubs) throws SAXException, IOException, ParserConfigurationException, ScriptException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(pubs);
        PrintWriter w = new PrintWriter(new File(EXPORT_JSON));
        w.write(json);
        w.flush();
        w.close();
    }
}
