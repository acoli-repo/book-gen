from gensim.models.phrases import Phrases, Phraser
import nltk
import sys
#import collocations


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
                if not self.convert and i > 3000000:
                    break
                if i % 10000 == 0:
                    print("Counting",i)

print("Reading from:", sys.argv[1:-1])
print("Writing to:", sys.argv[-1])
phrases = Phrases(Reader(sys.argv[1:-1]), min_count=50, threshold=0.5, scoring='npmi')

out = open(sys.argv[-1], "w")
#word_freq = collocations.defaultdict(lambda: 0)
for i, sent in enumerate(Reader(sys.argv[1:-1], convert=True)):
    #for token in sent:
    #    word_freq[token] += 1
    #print(' '.join(phrases[sent]))
    out.write(' '.join(phrases[sent])+'\n')
    if i % 100000 == 0:
        print("Converting",i)
        print(' '.join(phrases[sent]))
    #if i > 100:
    #    break
