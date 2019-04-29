import gensim
import io
import gzip
import os
import sys
import random
import nltk
import logging

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

class GzipReader(object):
    def __init__(self, dirs):
        self.dirs = dirs
    def __iter__(self):
        for dir in self.dirs:
            print (dir)
            files = os.listdir(dir)
            for i, file in enumerate(random.sample(files, len(files))):
                if '.txt' not in file:
                    continue
                try:
                    print ("Reading %s (%d of %d)" % (file, i, len(files)))
                    #compr = StringIO.StringIO(open(file).read())
                    #gzipper = gzip.GzipFile(fileobj=open(dir+"/"+file))
                    #gzipper = gzip.open(dir+"/"+file)
                    for line in open(dir+"/"+file):
                        line = line.strip()
                        yield nltk.tokenize.word_tokenize(line)
                        #yield line.split()
                except:
                    print ("Error in", file)
                    raise


#model = gensim.models.Word2Vec(GzipReader(sys.argv[1:]), size=600, min_count=5, workers=4, iter=1, sg=0)
model = gensim.models.Word2Vec(GzipReader(sys.argv[1:]), size=300, min_count=5, iter=1, workers=16)
model.save("model_streamed_topic.w2v")




