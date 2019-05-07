#inspired by https://www.tensorflow.org/tutorials/keras/basic_regression

import csv, sys, pickle, os, argparse
import numpy as np
from get_sent_embedding import sent_vec_by_harmonic_mean, sent_vec_by_addition
from pprint import pprint
from sklearn.preprocessing import *
from sklearn.model_selection import train_test_split
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from sklearn.preprocessing import quantile_transform
from random import shuffle

#run e.g.
'''
python score_neural.py --sent_emb_score_files_dir=sent_embs_normalized_scores --modelPath=../word_embedding_models/entity_model/model_chemdump_entities.w2v
'''

def build_model(input_shape=300, activation=tf.nn.relu, output_activation=tf.nn.softmax, loss="mse", optimizer=None, factor_neurons_in_hidden=2.0,\
  factor_neurons_in_hidden_2=2.0):
  if output_activation:
    print("Using output activation function: ", output_activation)
    model = keras.Sequential([
      layers.Dense(int(input_shape/factor_neurons_in_hidden), activation=activation, input_shape=[input_shape]),
      layers.Dense(int(input_shape/(factor_neurons_in_hidden*factor_neurons_in_hidden_2)), activation=activation),
      layers.Dense(1, activation=output_activation)
    ])
  else:
    print("Using linear activation in output layer")
    model = keras.Sequential([
      layers.Dense(int(input_shape/factor_neurons_in_hidden), activation=activation, input_shape=[input_shape]),
      layers.Dense(int(input_shape/(factor_neurons_in_hidden*factor_neurons_in_hidden_2)), activation=activation),
      layers.Dense(1)
    ])
  if not optimizer:
    print("Using RMSProp with lr 0.00001 s defult optimizer, s no optimizer ws given")
    optimizer = tf.train.RMSPropOptimizer(0.00001)
  else:
    optimizer=optimizer
  model.compile(loss='mse', #cross entropy loss
                optimizer=optimizer,
                metrics=['mae', 'mse'])
  return model


def train_nn(ssp_embs, numb_epochs=100, optimizer=None, activation=tf.nn.relu, output_activation=None, loss=None, factor_neurons_in_hidden=2.0, \
  factor_neurons_in_hidden_2=2.0):
  final_scores = [i for i,j,k in ssp_embs]
  sent_embs = np.array([k for i,j,k in ssp_embs])
  sent_emb_dim = len(sent_embs[0])
  nn = build_model(input_shape=sent_emb_dim, activation=activation, output_activation=output_activation, loss=loss, optimizer=optimizer, \
        factor_neurons_in_hidden=factor_neurons_in_hidden, factor_neurons_in_hidden_2=factor_neurons_in_hidden_2)
  print(nn.summary())
  X_train, X_test, y_train, y_test = train_test_split(sent_embs, np.array(final_scores))
  print("training set length: ", len(X_train))
  print("test set length: ", len(X_test))
  print("training set input shape: ", X_train.shape)
  print("training set output shape: ", y_train.shape)
  history = nn.fit(X_train, y_train,
    epochs=numb_epochs, validation_split = 0.2)
  print(history)
  return nn

def predict_score(nn, sentences, sentences_embs=None, word_emb_model=None, sent_emb_model=None):
  """sentences: list of sentences (strings), with tokens separated by whitespace """
  if sent_emb_model:
    print("not implemented yet")
    return None
  try:
    fstemb=sentences_embs[0]
    print("embeddings given")
    sent_embs = np.array(sentences_embs)
  except (ValueError, TypeError) as e:
    sent_embs = np.array([sent_vec_by_addition(s.split(), word_emb_model) for s in sentences])
  scores = nn.predict(sent_embs)
  return scores

def evaluate(nn, test_input,  test_output,  test_input_embs=None):
  try:
    fstemb=test_input_embs[0]
    print("embeddings given")
    sent_embs = np.array(test_input_embs)
  except ValueError:
    sent_embs = np.array([sent_vec_by_addition(s.split(), word_emb_model) for s in sentences])
  loss, mae, mse = nn.evaluate(sent_embs, test_output, verbose=0)
  return loss, mae, mse


def run(modelfile="model_chemdump_entities.w2v", score_sent_emb_files=[]):
  from gensim.models import Word2Vec
  model = Word2Vec.load(modelfile)
  #definie optimizers here and put in the list below

  adam=keras.optimizers.Adam(lr=0.00001, beta_1=0.9, beta_2=0.999, epsilon=1e-08, decay=0.0)

  numb_epochs=500
  resultdir=os.path.join("scorer_models", "results_%sepochs" %numb_epochs)
  if not os.path.exists("scorer_models"):
    os.mkdir("scorer_models")
  if os.path.exists(resultdir):
    print("directory %s already exists" %resultdir)
  else:
    os.mkdir(resultdir)
  for fnih in [0.5, 2.0, 1.0]:
    for fnih_2 in [2.0, 1.0]:
      for o in [adam, None]:
        for act_func in [tf.nn.tanh]: #, tf.nn.relu]
          for score_sent_emb_file in score_sent_emb_files:
            ssp_embs = pickle.load(open(score_sent_emb_file, "rb"))
            maximum = max(ssp_embs, key=lambda x: x[0])[0]
            print("maximum: ", maximum)
            nn = train_nn(ssp_embs, numb_epochs=200, optimizer=o, activation=act_func, output_activation=None, loss=None, factor_neurons_in_hidden=fnih, \
             factor_neurons_in_hidden_2=fnih_2)
            testsents = [sent for (sn,sent,emb) in ssp_embs]
            testsents_embs = [emb for (sn,sent,emb) in ssp_embs]
            testscores = [sn for (sn,sent,emb) in ssp_embs]
            maxscore = list(filter(lambda x: x[0] == maximum, ssp_embs))[:2]
            print(maxscore)
            testsents+=[sent for (sn,sent,emb) in maxscore]
            testsents_embs+=[emb for (sn,sent,emb) in maxscore]
            testscores+=[sn for (sn, sent,emb) in maxscore]
            predicted = predict_score(nn, testsents, sentences_embs=testsents_embs, word_emb_model=model)
            loss, mae, mse = evaluate(nn, testsents, testscores,  test_input_embs=testsents_embs)
            resultfile=os.path.join(resultdir, os.path.basename(score_sent_emb_file).replace(".pickle", "-activation_%s_%s_fnih-%s_fnih_2-%s.results" %(act_func, o, fnih, fnih_2)))
            with open(resultfile, "w") as of:
              for p,t in zip(predicted, testscores):
                of.write("prediced: %s - orig: %s\n" %(p, t))
              of.write("loss: %s, mae: %s, mse: %s" %(loss, mae, mse))
            save_to=os.path.join(resultdir, os.path.basename(score_sent_emb_file).replace(".pickle", "-activation_%s_%s_fnih-%s_fnih_2-%s.model" %(act_func, o, fnih, fnih_2)))
            nn.save(save_to)

if __name__ == "__main__":
  parser = argparse.ArgumentParser(description = '')
  parser.add_argument('--modelPath', '-m', help='Path to word embedding model', type=str, default="../word_embedding_models/entity_model/model_chemdump_entities.w2v")
  parser.add_argument('--sent_emb_score_files_dir', '-s', help="Path to the output files of get_sent_embs_for_score_data.py", type=str, default="sent_embs_normalized_scores")
  args = parser.parse_args()
  score_files = [os.path.join(args.sent_emb_score_files_dir, i) for i in os.listdir(args.sent_emb_score_files_dir)]
  run(args.modelPath, score_files)

"""
#change the following parameters:
#-output_activation, loss
#-quantile_transformation, output_distribution
#-multply score with
#-normalizer

for multi in [1]: #[1000,10000,100000]:
#  sspn,minimum, maximum = normalize_scores(ssp, normalizer=normalizer,multiply_score_with=multi, output_distribution="normal", score_range=score_range, quantile_transformation=False, outputfile="normalized_scores_normalizer_without_quantile_tranform.csv")
#  sspn,minimum, maximum = normalize_scores(ssp, normalizer=normalizer,multiply_score_with=multi, output_distribution="uniform", score_range=score_range, outputfile="normalized_scores_quantile_transform_uniform_distr.csv")
#  sspn,minimum, maximum = normalize_scores(ssp, normalizer=normalizer,multiply_score_with=multi, output_distribution="normal", score_range=score_range, outputfile="normalized_scores_quantile_tranform_normal_distr.csv")

  sspn,minimum, maximum = normalize_scores(ssp, normalizer=normalizer,multiply_score_with=multi, output_distribution="normal", score_range=score_range, outputfile="normalized_scores_normalizer_and_quantile_transform_normal_distr.csv")
  nn = train_nn(sspn, word_emb_model=model, numb_epochs=10, activation=tf.nn.relu, output_activation=None, loss=None) #tf.nn.softmax, loss=None)
  testsents = [sent for (sn,s,sent) in sspn][:10]
  testscores = [sn for (sn,s,sent) in sspn][:10]
  maxscore = list(filter(lambda x: x[0] == maximum, sspn))
  testsents+=[sent for (sn, s,sent) in maxscore]
  testscores+=[sn for (sn, s, sent) in maxscore]
  predicted = predict_score(nn, testsents,word_emb_model=model)
  pprint(list(zip(predicted, testscores)))
"""
