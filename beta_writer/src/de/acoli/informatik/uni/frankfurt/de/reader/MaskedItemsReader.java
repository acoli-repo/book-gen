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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 *
 * @author niko
 */
public class MaskedItemsReader {

    public static String ENTITY_MAP_NAME = "entity_map.tsv";

    static LinkedHashMap<String, String> entityMap;

    public static LinkedHashMap<String, String> getEntities(String fileName) throws FileNotFoundException {
        loadThem(fileName);
        return entityMap;
    }

    public static void loadThem(String fileName) throws FileNotFoundException {
        entityMap = new LinkedHashMap<String, String>();
        Scanner s = new Scanner(new File(fileName));
        while (s.hasNextLine()) {
            String aLine = s.nextLine().trim();
            String[] items = aLine.split("\\t");
            if (entityMap.containsKey(items[0])) {
                System.err.println("Key error.");
                System.err.println(items[0] + " contained already!");
                //System.exit(0);
            }
            entityMap.put(items[0], items[1]);
        }
        System.err.println("Loaded " + entityMap.size() + " masked entities.");
        s.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        LinkedHashMap<String, String> bla = getEntities("gen/data/" + ENTITY_MAP_NAME);
        System.out.println(bla.get("ENTITY_1043"));
    }

}
