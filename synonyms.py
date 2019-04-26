import sys
import gensim
import re
import random
import csv
import logging
import numpy as np
import theanets
import json
from sklearn.metrics import classification_report, confusion_matrix

from gensim.models import KeyedVectors


def hasNoNumbers(inputString):
    return not any(char.isdigit() for char in inputString)



fname = sys.argv[1]
# read in ngrams file.
with open(fname) as f:
    content = f.readlines()
# you may also want to remove whitespace characters like `\n` at the end of each line
ngrams = [x.strip() for x in content] 
print("read in " + str(len(ngrams)) + " ngrams.")



### Main program
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

# Load word vectors
vectors = KeyedVectors.load("models/model_chemdump_ngrams.w2v", mmap='r')
#vectors = KeyedVectors.load("models/model_soc_ngrams.w2v", mmap='r')

# iterate over each ngram and see if we have it in the word2vec model.
for ngram in ngrams:
    #print(ngram)
    if ngram in vectors and hasNoNumbers(ngram) and (len(ngram)>1):
        #print(vectors[ngram])
        print (ngram + '\t' + str(vectors.wv.vocab[ngram].count) + '\t' + str(vectors.most_similar(ngram,topn=3)))
        #print()
        #print()
    #else:
        #print (ngram + ' - ')

# Examples on how to use word vectors.
#print (vectors.most_similar('In_general')) 


