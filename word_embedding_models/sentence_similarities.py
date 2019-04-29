''' 
Takes as input one-line separated, whitespace-tokenized lemmata(!)
and uses wordvector model (trained on lemmata) 
to produce sentence vectors for each line by vector average over tokens.

Call this script with: python sentence_similarities.py aggregated-sentences.txt > sentence-similarities.txt

where senti-intro-sents.txt is the line-separated list of sentences
and sentence_similarities is the output file with pairwise cosine similarity scores between all sentences.

The output serves to reorder the sentences. (not part of this implementation).
'''

import gensim
import re
import random
import csv
import logging
import numpy
import sys
import numpy as np
import theanets
import json
from sklearn.metrics import classification_report, confusion_matrix
from sklearn.metrics.pairwise import cosine_similarity


### Main program
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

# Load word vectors
vectors = gensim.models.Word2Vec.load("vectors-chem-bio-cs.d2v")

# Read in sentence by sentence, token by token.

sentence_vectors = []

with open(sys.argv[1],'r') as f:
    for sentence in f:
        tokens = sentence.split()
        found_toks = 0
        average_vector = 0
        for token in tokens:
            token = token.lower()
            #print(token)
            # check if vector exists for that token.
            if token in vectors:
                token_vector = vectors[token]
                #print(token_vector)
                average_vector = average_vector + token_vector
                found_toks = found_toks + 1
            #else:
                #print("no vector found for " + token)
        #print("# found_toks")
        #print(found_toks)
        # no tokens found, make default vector.
        if(found_toks==0):
            #print("found no toks")
            average_vector = np.zeros(100)
        else:
            average_vector = average_vector/found_toks
        #print("sentence avg:")
        #print(average_vector)
        sentence_vectors.append(average_vector.reshape(1,-1))
    print(len(sentence_vectors))
        
#print(sentence_vectors[1])
#print(sentence_vectors[2])
#print cosine_similarity(sentence_vectors[1], sentence_vectors[2])
#print cosine_similarity(sentence_vectors[1], sentence_vectors[106])
#print cosine_similarity(sentence_vectors[105], sentence_vectors[106])

# Iterate over all sentences and produce pairwise similarities for grouping.
# produce similarity matrix:
# e.g., 1 2 0.88
#       1 3 0.78
#       . . .
for first_idx in range(len(sentence_vectors)):
    first_sent = sentence_vectors[first_idx]
    for idx2 in range(len(sentence_vectors)):
        second_idx = first_idx+idx2+1
        if(second_idx < len(sentence_vectors)):
            second_sent = sentence_vectors[second_idx]
            # line compared to line and cosine similarity.
            sim = cosine_similarity(first_sent,second_sent)[0][0]
            print (first_idx+1, second_idx+1, sim)
            #if(sim > 0.94):
                #sys.exit()



# Examples on how to use word vectors
#print vectors['opinion']
#print vectors.most_similar('chemistry')
#print vectors.most_similar(['battery', 'lithium'])
#print vectors.similarity('battery', 'lithium')
#print vectors.most_similar(['battery','lithium'], negative=['lithium-ion', 'li-ion'])





