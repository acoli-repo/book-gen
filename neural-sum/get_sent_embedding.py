from gensim.models.word2vec import Word2Vec
import os, argparse, codecs, math
import numpy as np
from nltk.tokenize import word_tokenize

#run e.g.
"""
python get_sent_embedding.py -m ../word_embedding_models/entity_model/model_chemdump_entities.w2v \
-f=test_sentences.txt -o=test_sentences_embeddings.txt --tokenize=1 --afterComma=50
"""


def load_w2v_model(modelpath):
  print("Loading model ", modelpath)
  return Word2Vec.load(modelpath)

def get_tokenized_sentences(sentenceFile, tokenize=0):
  with codecs.open(sentenceFile, "r", encoding="UTF-8") as of:
    lines = of.readlines()
  if tokenize:
    sentences = [word_tokenize(l[:-1]) for l in lines]
  else:
    sentences = [l[:-1].split() for l in lines]
  return sentences

def sent_vec_by_addition(words_of_sent, model):
  '''Calculates sentence embedding by adding all the word vectors '''
  word_vecs=[]
  for w in words_of_sent:
    try:
      word_vecs.append(model[w])
    except KeyError:
      #print("No word embedding for ", w)
      continue
  addition = np.sum(np.array(word_vecs), axis=0)
  return addition

def sent_vec_by_harmonic_mean(words_of_sent, model):
  '''Calculates sentence embedding by taking the harmonic mean of the word embeddings average and the word embeddings multiplication '''
  word_vecs=[]
  for w in words_of_sent:
    try:
      word_vecs.append(model[w])
    except KeyError:
      #print("No word embedding for ", w)
      continue
  addition = np.sum(np.array(word_vecs), axis=0)
  product = np.prod(np.array(word_vecs), axis=0)
  hmean = 2*(np.multiply(addition, product))/(np.add(addition, product))
  return hmean

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description = 'Get sentence embedding by taking the harmonic mean of the word embeddings')
    parser.add_argument('--modelPath', '-m', help='Path to word embedding model', type=str)
    parser.add_argument('--tokenize', '-t', help='Set to 1 if sentences in input file have to be tokenized, defaut is 0 (=just split on space)', type=int, default=0)
    parser.add_argument('--sentenceFile', '-f', help="Path to file containing one sentence per line")
    parser.add_argument('--outputFile', '-o', help="Path of file to which the sentence embeddings should be saved (one sentence embedding per line)")
    parser.add_argument('--afterComma', '-c', help="Digits after the comma of the float numbers in the embedding; recommended >30", default=50, type=int)
    args = parser.parse_args()
    model = load_w2v_model(args.modelPath)
    sentences = get_tokenized_sentences(args.sentenceFile, tokenize=args.tokenize)
#    print(sentences)
    sent_embs = [sent_vec_by_harmonic_mean(s, model) for s in sentences]
    print("Saving embeddings to ", args.outputFile)
    with open(args.outputFile, "w") as of:
      for emb in sent_embs:
        format_string = "{:0.%sf}" %args.afterComma
        emb_string = [format_string.format(elem) for elem in emb]
        of.write(" ".join(emb_string)+"\n")
