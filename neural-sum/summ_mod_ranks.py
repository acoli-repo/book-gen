#run e.g.
"""
python summ_mod_ranks.py -le=10 \
-s=scorer_models/results_500epochs/sent_embs_normalized_scores_minmaxscaler.csv-activation_\<function\ tanh\ at\ 0x7fba2f489c80\>_None_fnih-1.0_fnih_2-1.0.model \
-e=../word_embedding_models/entity_model/model_chemdump_entities.w2v -p=. --summa_parent_directory_path=../textrank/
"""

import argparse
import sys
from pprint import pprint

def get_rank(summary_with_score):
  summary_with_ranks = enumerate([(pos, sent, score) for (pos, (sent, score)) in sorted(enumerate(summary_with_score), key=lambda x: x[1][1])], 1)
  sent_dicts = dict()
  for rank, (pos, sent, score) in summary_with_ranks:
    sent_dicts[pos]={}
    sent_dicts[pos]["rank"]=rank
    sent_dicts[pos]["score"]=score
    sent_dicts[pos]["sentence"]=sent
  #pprint(sent_dicts)
  return sent_dicts


parser = argparse.ArgumentParser()
parser.add_argument('--input_text', '-i', type=str, default='''Automatic summarization is the process of reducing a text document with a computer program in order to create a summary that retains the most important points of the original document .
\nAs the problem of information overload has grown , and as the quantity of data has increased , so has interest in automatic summarization .
\nTechnologies that can make a coherent summary take into account variables such as length , writing style and syntax .
\nAn example of the use of summarization technology is search engines such as Google .
\nDocument summarization is another .''') #, required=True, help='text - one sentence per line, tokenized', )
parser.add_argument('--thresh', '-t', type=float, default=0.4)
parser.add_argument('--which', '-w', type=str, help="ratio or words - choose one of [ratio|words] for the length calculattion", default="ratio")
parser.add_argument('--length', '-le', type=float, help="ratio or number of words", required=True)
parser.add_argument('--scorer_model_path', '-s', type=str, default="/home/kdonandt/score-neural/results_200epochs/sent_embs_normalized_scores_limit_minmaxscaler.csv-activation_<function tanh at 0x7f01cb2c6268>_<tensorflow.python.keras.optimizers.Adam object at 0x7f025a6107b8>_fnih-0.5_fnih_2-1.0.model", required=True, help='path to the neural scoring model file')
parser.add_argument('--wordemb_model_path', '-e', type=str, default="/home/kdonandt/score-neural/model_chemdump_entities.w2v", required=True, help='path to the word embedding model file')
parser.add_argument('--score_neural_directory_path', '-p', type=str, default="/home/kdonandt/score-neural", required=True, help='path to the directory containing the score_neural.py script')
parser.add_argument('--summa_parent_directory_path', type=str, default="/home/kdonandt/bookgen/textrank", required=True)
parser.add_argument('--weight_textrank', '-wt', default=0.9, type=float)
parser.add_argument('--weight_neural', '-wn', default=0.1, type=float)
args = parser.parse_args()

sys.path.insert(0, args.score_neural_directory_path)
from score_neural import predict_score
sys.path.insert(0, args.summa_parent_directory_path)
from summa import summarizer
from summa import keywords

from gensim.models import Word2Vec
from keras.models import load_model


orig_text = args.input_text # sys.argv[1]
text_newline_splitted = orig_text.split('\n')
text = [sent.split() for sent in text_newline_splitted]

thresh = args.thresh # sys.argv[2]
which = args.which # sys.argv[3] # ratio or words

# Summary Textrank:
summary_textrank = summarizer.summarize(text, ratio=1, scores=True) 
#ratio = 1 as we want to generate summary afterwards,considering the neural rank

summary_textrank_ranks = enumerate([(pos, sent, score) for (pos, (sent, score)) in sorted(enumerate(summary_textrank), key=lambda x: x[1][1])], 1)


# Summary Neural:
text_scorer = [s for s in text_newline_splitted if len(s)>0]
#print("Loading scorer model")
nn = load_model(args.scorer_model_path)
#print("Scorer model loaded successfully!")
#print("Loading word embedding model")
word_emb_model = Word2Vec.load(args.wordemb_model_path)
#print("Loading done!")
predicted = [p[0] for p in predict_score(nn, text_scorer, word_emb_model=word_emb_model)]

summary_neural = list(zip(text_scorer, predicted))


# Combine scores:
# 1. ranking
summ_textrank_ranked_dict=get_rank(summary_textrank)
summ_neural_ranked_dict=get_rank(summary_neural)
#2. combine
final=dict()
for sent in summ_textrank_ranked_dict:
  #print(summ_textrank_ranked_dict)
  rank_textrank = args.weight_textrank*summ_textrank_ranked_dict[sent]["rank"]
  rank_neural = args.weight_neural*summ_neural_ranked_dict[sent]["rank"]
  final_rank = rank_textrank+rank_neural
  final[sent]={"sentence":summ_textrank_ranked_dict[sent]["sentence"], "rank":final_rank}
#pprint(final)


len_text = len(text)
if args.which == "ratio":
  limit= args.length * len_text
else:
  limit=args.length



rank_sorted = list(sorted([(k, final[k]["rank"], final[k]["sentence"]) for k in final.keys()], key=lambda x: x[1], reverse=True))


len_summary=0
if len_summary>=len_text:
  summary_final= summary_textrank #with ratio=1!
else:
  summary_final=[]

for k,r,s in rank_sorted:
  if len_summary<= limit:
    len_summary+=len(s.split())
    summary_final.append((k, s, r))
  else:
    break

id_sorted_final_summary = [(s,r) for k,s,r in list(sorted(summary_final, key=lambda x: x[0]))]
#pprint(id_sorted_final_summary)


for s,r in id_sorted_final_summary:
  print(s+"\t"+str(r))


# Keywords:
#print(keywords.keywords(orig_text))


#example for input:

