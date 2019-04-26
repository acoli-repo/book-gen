#!/bin/bash

# Prototype implementation of "Beta Writer"
# Complete pipeline script from A++ dump to structured book.


# run with:
# sh pipeline.sh DIR_TO_APLUSPLUS_DOCUMENTS WORKING_DIR
# e.g.,
# sh pipeline.sh your_corpus/ gen/

# chapter shape
NUM_CHAPTERS=4
NUM_SECTIONS=2
NUM_PAPERS=25
# params 
Max_df=180
Min_df=11
Num_feats=5000
# max_df=0.9, max_features=5000, min_df=0.01


# speficy here whether you want to parse and restructre.
PARSE_AND_RESTRUCTURE=true
# specify here whether you want to use precomputed synonyms (true)
# or compute ngram statistics (false)
USE_PRECOMPUTED_SYNONYMS=true
# extended abstract yes/no
EXTENDED_ABSTRACT=true
ALSO_ADD_OTHER_BODY_SECTIONS_APART_FROM_EXTENDED_ABSTRACT=false

PYTHON="/usr/local/bin/python3"
TEXTRANK="textrank/summ_and_keywords.py"
MATE="mate/"
JAR="beta_writer/dist/beta_writer.jar"
XMX=-Xmx7g

echo "Running pipeline..."

# cleanup.
rm -f -r $2/*

mkdir $2'/data'
# Mask entities and export to new A++ data folder.
$PYTHON mask_entities.py $1 $2'/data' 'entity_map/entity_map.tsv'
# A++ collection to corpus.json
java $XMX -jar $JAR "A++2JSON" $2'/data' $2'corpus.json'
# Text extraction.
$PYTHON json2txt.py $2'corpus.json' > $2'corpus.txt'
# replace faulty sentence splits in named entities.
sed -i '' -e 's/ 0 . | ENTITY/ 0.ENTITY/g' $2'corpus.txt'


# Chapter structure html
$PYTHON mkstructure_html.py $2'corpus.txt' $NUM_CHAPTERS $NUM_SECTIONS $NUM_PAPERS $Max_df $Min_df $Num_feats > $2'chap-struc.html'
# Aggregate sentences around "Intro" and "Conclusion".
java $XMX -jar $JAR "SECTION_AGGREGATOR" $2 $TEXTRANK $PYTHON $EXTENDED_ABSTRACT $ALSO_ADD_OTHER_BODY_SECTIONS_APART_FROM_EXTENDED_ABSTRACT


if [ "$PARSE_AND_RESTRUCTURE" = true ]
then
    echo 'Running in restructuring mode...'
	# Parse all sentences.
	echo 'Parsing sentences...'
	sh parse_all_sentences.sh $2 $MATE > $2'/chapters/mate.log'
	echo 'Distributing parsed chunks...'
	java $XMX -jar $JAR "DISTRIBUTE_PARSED_CHUNKS" $2
	# restructure sentences.
	sh restr.sh $2
	# reintroduce bracket content.
	java $XMX -jar $JAR "REINTRODUCE_BRACKET_CONTENT" $2
	# make global chapter bibliographies.
	java $XMX -jar $JAR "CHAPTER_BIBLIOGRAPHY_MAKER" $2
else
    echo 'Running without restructuring...'
fi


if [ "$USE_PRECOMPUTED_SYNONYMS" = true ]
then
	echo 'Using precomputed synonyms...'
    java $XMX -jar $JAR "REPLACE_SYNONYMS" $2 $USE_PRECOMPUTED_SYNONYMS > $2'ngrams_kept.txt'
else
	echo 'Computing synonym statistics...'
    # collect all ngrams from chapters.
    java $XMX -jar $JAR "COLLECT_NGRAMS" $2
    # compute all similarities.
    $PYTHON synonyms.py $2'ngrams.txt' > $2'ngrams_stats.txt'
    # Replace synonyms and generate new html output.
    java $XMX -jar $JAR "REPLACE_SYNONYMS" $2 $USE_PRECOMPUTED_SYNONYMS > $2'ngrams_kept.txt'
fi


# Fill stubs and add intros, conclusions and paper summaries.
java $XMX -jar $JAR "STUB_FILLER" $2 $TEXTRANK $PYTHON $USE_PRECOMPUTED_SYNONYMS > $2'book.html' 
echo "... done."