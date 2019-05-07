import argparse
import time

"""how to execute:
-f argument: file(s) with scores (result file of testing the model, like results_200epochs/*.results
-p argument: 1 if you want to print meantime results, otherwise 0
e.g.  python rank_scores.py -f results_200epochs/*.results -p=0"""

def get_score_strings(path):
    """get scores from test output file, which lists per line the predicted and original scores:
    predicted: [0.3563531] - orig: 0.3676470588235294
    predicted: [0.737128] - orig: 0.7205882352941176
    """
    of = open(path)
    lines=of.readlines()
    scores=lines[:-1] #last line is the loss, mae, mse line and has to be excluded
    pred, orig = zip(*[i.split(" - ") for i in scores])
    pred = enumerate([float((string.split("[")[1]).split("]")[0]) for string in pred])
    pred_list = list(pred)
    orig = enumerate([float(i.split(": ")[1].replace("\\n", "")) for i in orig])
    orig_list = list(orig)
    return pred_list, orig_list

def get_ranking_order_switches(list):
    """list has to be list of quadruples (obs_id, sent_id, score, rank) output from get_ranks function
    as in Ranking-Based Evaluation of Regression Models, Rosset et al."""
    ranking_order_switches=0
    #itearte over list, get observation i
    n = len(list)
    for i in range(0,n):
        #get observation j and iterate over j to n
        j = i+1
        while j<n-1:
            if list[i][3]>list[j][3]:
                if enable_printing:
                    print("switch detected: ", list[i], " - ", list[j])
                ranking_order_switches+=1
            j += 1
    return ranking_order_switches

def sorting(pred, orig):
    """sorts predicted scores in descending order, and
    then sorts original scores according to the order in the sorted
    predicted scores"""
    pred_sorted = list(sorted(pred, key=lambda x: x[1], reverse=True))
    if enable_printing:
        print("pred scores sorted:",pred_sorted)
    #sort orig scores like predicted socres are sorted
    orig_dict = dict(orig)
    orig_sorted_like_pred_sorted_with_observation_id = [(obs_id, sent_id, score) for (obs_id, (sent_id, score)) in enumerate([(sent_id, orig_dict[sent_id]) for sent_id,score in pred_sorted])]
    pred_sorted_with_observation_id = [(obs_id, sent_id, score) for (obs_id, (sent_id, score)) in enumerate(pred_sorted)]
    if enable_printing:
        print("predicted scores sorted - with observation id: ", pred_sorted_with_observation_id)
        print("original scores sorted like predicted scores - with observation id: ", orig_sorted_like_pred_sorted_with_observation_id)
    return pred_sorted, orig_sorted_like_pred_sorted_with_observation_id

def get_rank_for_orig(orig_sorted_like_pred, last_score=1000000):
    """input: orig_sorted_like_pred: list of triples (obs_id, sent_id, score) , where the sent_ids are in the same order as in
    pred_sorted (see ouput of sorting function) - last score: a random number which has to be higher than the maximum score;
    sorts this list of triples (key of sorting is the score) in order to determine the rank of the scores;
    this rank cannot be simply determined by enumarating after sorting, as same scores need to have same rank!
    """
    sorted_orig = sorted(orig_sorted_like_pred, key=lambda x: x[2], reverse=True)
    rank=0
    ranked_orig = []
    for obs_id, sent_id, score in sorted_orig:
        if score<last_score:
            last_score=score
            ranked_orig += [(obs_id, sent_id, score, rank)]
            rank+=1
        else:
            ranked_orig+=[(obs_id, sent_id, score, rank)]
    ranked_orig_sorted_like_pred = sorted(ranked_orig, key=lambda x: x[0])
    if enable_printing:
        print(ranked_orig_sorted_like_pred)
    return ranked_orig_sorted_like_pred

parser = argparse.ArgumentParser()
parser.add_argument('-f','--file_pathes', nargs='+', required=True)
parser.add_argument('-p', '--enable_printing', type=int, required=True)
args = parser.parse_args()
global enable_printing
enable_printing=bool(args.enable_printing)
pathes=args.file_pathes
print("number of files: ", len(pathes))

for path in pathes:
  start = time.time()
  print(path)
  pred, orig = get_score_strings(path)
  pred_sorted, orig_sorted_like_pred_sorted_with_observation_id = sorting(pred, orig)
  pred_sorted_1 = list(enumerate(pred_sorted))
  ranked_orig = get_rank_for_orig(orig_sorted_like_pred_sorted_with_observation_id)
  rank_ord_sw = get_ranking_order_switches(ranked_orig)
  print("ranking order switches: %s" %rank_ord_sw)
  end = time.time()
  print("took %s seconds.\n\n" %(end - start))
