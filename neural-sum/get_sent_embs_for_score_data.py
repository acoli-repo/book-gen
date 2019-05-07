from get_sent_embedding import sent_vec_by_harmonic_mean, sent_vec_by_addition
import csv, sys, os
import numpy as np
from gensim.models import Word2Vec


#run e.g.
"""
python get_sent_embs_for_score_data.py normalized_scores/normalized_scores_limit_minmaxscaler.csv ../word_embedding_models/entity_model/model_chemdump_entities.w2v addition
"""

def get_embs(score_sent_pairs_norm, word_emb_model=None, sent_emb_model=False, sent_emb_by="addition"):
  if sent_emb_model:
    print("not implemented yet")
    return None
  sentences = [sent for (score_norm, sent) in score_sent_pairs_norm]
  print("len sentences: ", len(sentences))
  scores = [score_norm for (score_norm, sent) in score_sent_pairs_norm]
  final_sentences, sent_embs, final_scores = [],[],[]
  for sent,score in zip(sentences, scores):
    if sent_emb_by=="addition":
      emb = sent_vec_by_addition(sent.split(), word_emb_model)
    elif sent_emb_by=="harmonic_mean":
      emb = sent_vec_by_harmonic_mean(sent.split(), word_emb_model)
    else:
      print("Choose either 'addition' or 'harmonic_mean'")
      return None
    try:
      le = len(emb)
    except TypeError:
      print("no embedding possible for ", sent)
      continue
    final_sentences.append(sent)
    final_scores.append(score)
    sent_embs.append(emb)
  sent_embs = np.array(sent_embs)
  print("len final_sentences: ", len(final_sentences), " versus: ", len(sentences))
  sent_emb_dim = len(sent_embs[0])
  print("len sent emb: ", sent_emb_dim)
  return [(s,sent,emb) for ([s,sent],emb) in zip(score_sent_pairs_norm,sent_embs)]


encoding="ISO-8859-1"
with open(sys.argv[1],  encoding=encoding) as csvfile:
  reader = csv.reader(csvfile, delimiter=',')
  ssp = []
  for row in reader:
    ssp.append((float(row[0]), row[1]))

score_sent_pairs_norm = ssp
model = Word2Vec.load(sys.argv[2]) #"model_chemdump_entities.w2v")
ssp_embs = get_embs(score_sent_pairs_norm, word_emb_model=model, sent_emb_model=False, sent_emb_by=sys.argv[3])

import pickle
resultdir="sent_embs_normalized_scores"
if os.path.exists(resultdir):
  print("directory %s already exists" %resultdir)
else:
  os.mkdir(resultdir)
pickle.dump(ssp_embs, open(os.path.join("sent_embs_normalized_scores", "sent_embs_"+os.path.basename(sys.argv[1])+".pickle"), "wb"))
