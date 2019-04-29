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
vectors = KeyedVectors.load("model_chemdump_entities_clean.w2v", mmap='r')

# Examples on how to use word vectors.

print (vectors.most_similar('ENTITY_196')) # CaCO<sub>3</sub>
print (vectors.most_similar('ENTITY_2697')) #mol<sup>−1</sup>
