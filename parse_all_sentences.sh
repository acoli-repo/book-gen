#!/bin/bash
echo "Parsing all sentences..."

for f in $(find $1 -name 'all-section-sentences-tok.txt'); 
do 
  DIR=$(dirname "${f}")
  UNPARSED=$f
  echo 'parsing: '$UNPARSED
  TMP=${f%-tok.*}
  PARSED=$TMP".parsed.conll"
  echo 'parsed: '$PARSED
  # take action (parse) this file. 
  #echo $fname
  java -cp $2srl.jar:$2lib/anna-3.3.jar:$2lib/liblinear-1.51-with-deps.jar:$2lib/opennlp-tools-1.4.3.jar:$2lib/maxent-2.5.2.jar:$2lib/trove.jar:$2lib/seg.jar -Xmx3g se.lth.cs.srl.CompletePipeline eng -tagger $2models/CoNLL2009-ST-English-ALL.anna-3.3.postagger.model -parser $2models/CoNLL2009-ST-English-ALL.anna-3.3.parser.model -srl $2models/CoNLL2009-ST-English-ALL.anna-3.3.srl-4.1.srl.model -lemma $2models/CoNLL2009-ST-English-ALL.anna-3.3.lemmatizer.model -tokenize -test $UNPARSED -out $PARSED
done