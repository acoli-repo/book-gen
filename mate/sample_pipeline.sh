#!/bin/bash
FILES=input/*
for f in $FILES
do
  echo "Processing $f file..."
  fname=`basename $f`
  # take action (parse) this file. 
  #echo $fname
  java -cp srl.jar:lib/anna-3.3.jar:lib/liblinear-1.51-with-deps.jar:lib/opennlp-tools-1.4.3.jar:lib/maxent-2.5.2.jar:lib/trove.jar:lib/seg.jar -Xmx3g se.lth.cs.srl.CompletePipeline eng -tagger models/CoNLL2009-ST-English-ALL.anna-3.3.postagger.model -parser models/CoNLL2009-ST-English-ALL.anna-3.3.parser.model -srl models/CoNLL2009-ST-English-ALL.anna-3.3.srl-4.1.srl.model -lemma models/CoNLL2009-ST-English-ALL.anna-3.3.lemmatizer.model -tokenize -test $f -out "input/"$fname".parsed.txt"
done