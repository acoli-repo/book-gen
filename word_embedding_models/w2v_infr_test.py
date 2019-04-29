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

### Main program
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

# Load word vectors
vectors = KeyedVectors.load("model_soc_ngrams0.w2v", mmap='r')

# Examples on how to use word vectors.
print (vectors.most_similar('Most_importantly'))
print (vectors.most_similar('satisfy'))
print (vectors.most_similar('demand'))
print (vectors.most_similar('good'))
print (vectors.most_similar('Europe'))
print (vectors.most_similar('Approximately'))

print ('***')

vectors = KeyedVectors.load("model_chemdump_ngrams.w2v", mmap='r')
print (vectors.most_similar('Most_importantly'))
print (vectors.most_similar('satisfy'))
print (vectors.most_similar('demand'))
print (vectors.most_similar('good'))
print (vectors.most_similar('Europe'))
print (vectors.most_similar('Approximately'))





