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
vectors = KeyedVectors.load("model_streamed_topic.w2v", mmap='r')

# Examples on how to use word vectors.




print ("\nsucfficient_condition:")
print(vectors.most_similar('sufficient_condition'))
print ("\nelectric_field_distribution:")
print(vectors.most_similar('electric_field_distribution'))


ngram = 'genotoxic_effects'
print ("\n" + ngram + ":")
print(vectors.most_similar(ngram))

ngram = 'risk_assessment'
print ("\n" + ngram + ":")
print(vectors.most_similar(ngram))

#ngram = 'risk_assessment_evaluation'
print ("\n" + ngram + ":")
print(vectors.most_similar(ngram))

ngram = 'single_components'
print ("\n" + ngram + ":")
print(vectors.most_similar(ngram))

ngram = 'binary_mixtures'
print ("\n" + ngram + ":")
print(vectors.most_similar(ngram))

ngram = 'active_ingredient'
print ("\n" + ngram + ":")
print(vectors.most_similar(ngram))


print ("\nhighest_amounts:")
print(vectors.most_similar('highest_amounts'))
print ("\nfurther_analysis:")
print(vectors.most_similar('further_analysis'))

print ("\ngood:")
print (vectors.most_similar('good'))


print ("\nappropriate:")
print (vectors.most_similar('appropriate'))

print ("\nnice:")
print (vectors.most_similar('nice'))
print ("\nbad:")
print (vectors.most_similar('bad'))
print ("\ninsufficient:")
print (vectors.most_similar('insufficient'))
print ("\nworse:")
print (vectors.most_similar('worse'))

print ("\nglyphosate")
print (vectors.most_similar('glyphosate'))

print ("\nGlyphosate")
print (vectors.most_similar('Glyphosate'))


print ("\nchemistry:")
print (vectors.most_similar('chemistry'))
print ("\nherbicides:")
print (vectors.most_similar('herbicides'))
print ("\nbut:")
print (vectors.most_similar('but'))
print ("\nreport:")
print (vectors.most_similar(['report']))


print ("\nlithium-ion")
print (vectors.most_similar('lithium-ion'))
print ("\nLithium-ion")
print (vectors.most_similar('Lithium-ion'))
print ("\nLi-ion:")
print (vectors.most_similar('Li-ion'))
print ("\nreport, latest:")
print (vectors.most_similar(['report', 'latest']))
print ("\nbattery, lithium, negative: lithium-ion, Li-ion:")
print (vectors.most_similar(['battery','lithium'], negative=['lithium-ion', 'Li-ion']))








