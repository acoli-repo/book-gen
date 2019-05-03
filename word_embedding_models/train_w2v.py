
import gensim
import io
import gzip
import os
import sys
import random
import nltk
import logging

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)


class FileReader(object):
    def __init__(self, filenames, convert=False):
        self.filenames = filenames
        self.convert = convert
        self.j = 0
    def __iter__(self):
        for file in self.filenames:
            print("Reading", file)
            for i, line in enumerate(open(file)):
                line = line.strip()
                yield nltk.tokenize.word_tokenize(line)
                if i % 100000 == 0:
                    print("Counting",i)


class DirReader(object):
    def __init__(self, paths, convert=False):
        self.paths = paths
        self.convert = convert
        self.j = 0
    def __iter__(self):
        for root in self.paths:
            for path, dirs, files in os.walk(root):
                for file in files:
                    self.j += 1
                    if not file.endswith('.txt'):
                        continue
                    file = path+'/'+file
                    if self.j % 100 == 0:
                        print(self.j, "Reading", file)
                    for i, line in enumerate(open(file)):
                        line = line.strip()
                        yield nltk.tokenize.word_tokenize(line)



READ_DIRS = True                        

if READ_DIRS:
    reader = DirReader # Scan directories for .txt files to read
else:
    reader = FileReader # Read files


model = gensim.models.Word2Vec(reader(sys.argv[1:]), size=300, min_count=20, iter=20, workers=14)
model.save("model_ngrams.w2v")
