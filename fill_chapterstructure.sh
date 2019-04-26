#!/bin/bash

# run with: sh fill_chapterstructure.sh gen/

# Fills already generated chapter structure in gen/chap-struc.html
# with data from get/chapters/*/*

TEXTRANK="textrank/summ_and_keywords.py"
JAR="beta_writer/dist/beta_writer.jar"
PYTHON="/usr/local/bin/python3"

echo "Filling chapter structure..."

# Fill stubs and add intros, conclusions and method and result summaries.
# true: fill with reordered sentences.
# false: fill with NON-reordered sentences.
java -Xmx3g -jar $JAR "STUB_FILLER" $1 $TEXTRANK $PYTHON > $1'book.html' 


echo "... done."