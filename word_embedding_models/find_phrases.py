from gensim.models.phrases import Phrases, Phraser
import nltk
import sys


class Reader(object):
    def __init__(self, filename, convert=False):
        self.filename = filename
        self.convert = convert
    def __iter__(self):
        for file in self.filename:
            print("Reading", file)
            for i, line in enumerate(open(file)):
                line = line.strip()
                yield nltk.tokenize.word_tokenize(line)
                if not self.convert and i > 3000000: # Limit number of lines at initialization
                    break
                if i % 10000 == 0:
                    print("Counting",i)

print("Reading from:", sys.argv[1:-1])
print("Writing to:", sys.argv[-1])
phrases = Phrases(Reader(sys.argv[1:-1]), min_count=50, threshold=0.5, scoring='npmi') # Initialize bigram detector

out = open(sys.argv[-1], "w")
# Scan files
for i, sent in enumerate(Reader(sys.argv[1:-1], convert=True)): 
    out.write(' '.join(phrases[sent])+'\n')
    if i % 100000 == 0:
        print("Converting",i)
        print(' '.join(phrases[sent]))
