#!/bin/bash

# Prototype implementation of "Beta Writer"
# Complete pipeline script from A++ dump to structured book.


# run with:
# sh pipeline.sh DIR_TO_APLUSPLUS_DOCUMENTS WORKING_DIR
# e.g.,
# sh pipeline_parameters.sh your_corpus/ gen/ 4 1 10

# chapter shape
NUM_CHAPTERS=$3 #4
NUM_SECTIONS=$4 #1
NUM_PAPERS=$5 #10 #papers pro section
# params 
Max_df=$6 #80
Min_df=$7 #15
Num_feats=$8 #5000
# max_df=0.9, max_features=5000, min_df=0.01


# speficy here whether you want to parse and restructre.
PARSE_AND_RESTRUCTURE=true #false #true
# specify here whether you want to use precomputed synonyms (true)
# or compute ngram statistics (false)
USE_PRECOMPUTED_SYNONYMS=true
# extended abstract yes/no
EXTENDED_ABSTRACT=true
ALSO_ADD_OTHER_BODY_SECTIONS_APART_FROM_EXTENDED_ABSTRACT=false

#PYTHON="/usr/local/bin/python3"
PYTHON="python"
TEXTRANK="textrank/summ_and_keywords.py"
MATE="mate/"
JAR=$9 #"beta_writer/dist/beta_writer.jar"
XMX=${10} #-Xmx9g

echo "Running pipeline..."

# cleanup.
#rm -f -r $2/*

sharedData=$2

if [ ! -d "$sharedData" ]; then
  mkdir $sharedData
  mkdir $sharedData'/data'
  # Mask entities and export to new A++ data folder.
  $PYTHON mask_entities.py $1 $sharedData'/data' 'entity_map/entity_map.tsv'
  # A++ collection to corpus.json
  echo "Using jar file :"$JAR
  java $XMX -jar $JAR "A++2JSON" $sharedData'/data' $sharedData'/corpus.json'
  # Text extraction.
  $PYTHON json2txt.py $sharedData'/corpus.json' > $sharedData'/corpus.txt'
  # replace faulty sentence splits in named entities.
  #sed -i '' -e 's/ 0 . | ENTITY/ 0.ENTITY/g' $sharedData'/corpus.txt'
  sed -i -e 's/ 0 . | ENTITY/ 0.ENTITY/g' $sharedData'/corpus.txt'
fi

outputdir=${11}
mkdir $outputdir

# Chapter structure html
$PYTHON mkstructure_html_avoid_duplicates.py $sharedData'/corpus.txt' $NUM_CHAPTERS $NUM_SECTIONS $NUM_PAPERS $Max_df $Min_df $Num_feats $outputdir > $outputdir'/chap-struc.html'
# Aggregate sentences around "Intro" and "Conclusion".
java $XMX -jar $JAR "SECTION_AGGREGATOR" $outputdir"/" $TEXTRANK $PYTHON $EXTENDED_ABSTRACT $ALSO_ADD_OTHER_BODY_SECTIONS_APART_FROM_EXTENDED_ABSTRACT $sharedData"/"


if [ "$PARSE_AND_RESTRUCTURE" = true ]
then
    echo 'Running in restructuring mode...'
	# Parse all sentences.
	echo 'Parsing sentences...'
	sh parse_all_sentences.sh $outputdir $MATE > $outputdir'/chapters/mate.log'
	echo 'Distributing parsed chunks...'
	java $XMX -jar $JAR "DISTRIBUTE_PARSED_CHUNKS" $outputdir
	# restructure sentences.
	sh restr.sh $outputdir
	# reintroduce bracket content.
	java $XMX -jar $JAR "REINTRODUCE_BRACKET_CONTENT" $outputdir"/" $sharedData"/"
	# make global chapter bibliographies.
	java $XMX -jar $JAR "CHAPTER_BIBLIOGRAPHY_MAKER" $outputdir"/" $sharedData"/"
else
    echo 'Running without restructuring...'
fi


if [ "$USE_PRECOMPUTED_SYNONYMS" = true ]
then
	echo 'Using precomputed synonyms...'
    java $XMX -jar $JAR "REPLACE_SYNONYMS" $outputdir $USE_PRECOMPUTED_SYNONYMS > $outputdir'/ngrams_kept.txt'
else
	echo 'Computing synonym statistics...'
    # collect all ngrams from chapters.
    java $XMX -jar $JAR "COLLECT_NGRAMS" $outputdir"/"
    # compute all similarities.
    $PYTHON synonyms.py $outputdir'/ngrams.txt' > $outputdir'/ngrams_stats.txt'
    # Replace synonyms and generate new html output.
    java $XMX -jar $JAR "REPLACE_SYNONYMS" $outputdir"/" $USE_PRECOMPUTED_SYNONYMS > $outputdir'/ngrams_kept.txt'
fi


# Fill stubs and add intros, conclusions and paper summaries.
java $XMX -jar $JAR "STUB_FILLER" $outputdir $TEXTRANK $PYTHON $USE_PRECOMPUTED_SYNONYMS $sharedData"/" > $outputdir'/book.html' 
echo "... done."
