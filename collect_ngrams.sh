#!/bin/bash

JAR="java/springer_bookgen/dist/springer_bookgen.jar"

echo "Generating synonym information..."

# collect all ngrams from chapters.
java -Xmx3g -jar $JAR "COLLECT_NGRAMS" $1
# compute all similarities.
python3 synonyms.py $1'ngrams.txt' > $1'ngrams_stats.txt'
# Replace synonyms in each chapter and generate syn-tok.txt files
java -Xmx3g -jar $JAR "REPLACE_SYNONYMS" $1 > $1'ngrams_kept.txt'

echo "... done."