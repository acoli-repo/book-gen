# A neural sentence selection component 
In order to select the summary sentences, use a combination of textrank and a neural scorer component which was trained to score the sentences using information about their similarity with the abstract sentences.


## Pipeline description:

1) TODO: obtain tsv_with_scores files using getAbstractRankData.sh; these tsv files contain a sentence and a score indicating the sentence's similarity with the abstract

2) Normalize scores of tsv files obtained by 1): 
e.g. 
```
python normalization.py -o=normalized_scores -s=tsv_with_scores/CathodeAnodeMaterialsLithiumIon.3-gram.vs.maxAbstractSentence.tsv
```

Outputs are then written to the files normalized_scores/normalized_scores_*.csv 
( if you choose another directory name for the -o parameter above, the file names are accordingly [name_you_chose]/normalized_scores_*.csv )

3) get sentence embeddings from sentences of the files from 1) (which are also stored in the output files of 2) )
e.g.
```
python get_sent_embs_for_score_data.py normalized_scores/normalized_scores_limit_minmaxscaler.csv ../word_embedding_models/entity_model/model_chemdump_entities.w2v addition
```

(the second parameter gives the path to the word embedding model. Please us a Word2Vec embedding model that can be loaded with the Word2Vec.load function of the gensim library;
the last parameter determines the method, how the sentence embeddings are generated using the word embeddings. "addition" means that the embeddings of the words of the sentence are simply summed up. "harmonic_mean" would build the harmonic mean instead, however, this option is not tested yet.)

The output is written to pickle files containing a list of lists for each sentence in the files of 1) : each list contains 
a) the score, b) the sentence as string, c) the sentence embedding
These pickle files are stored into the directory sent_embs_normalized_scores as sent_embs_*.pickle

4) Train the neural scorer model: 
e.g. 
```
python score_neural.py --sent_emb_score_files_dir=sent_embs_normalized_scores --modelPath=../word_embedding_models/entity_model/model_chemdump_entities.w2v
```

The resulting scorer model files are stored in the directory scorer_models asscorer_models/sent_embs_*.model

4) Evaluation of trained neural scorer models: 
e.g.
```
python rank_scores.py -f results_200epochs/*.results -p=0 > ranking_order_switches_results200epochs.txt
```

(-f argument: file(s) with scores (result file of testing the model, like results_200epochs/*.results
-p argument: 1 if you want to print meantime results (verbose option), otherwise 0 )

This calculates ranking order switches (see paper Ranking-Based Evaluation of Regression Models, Rosset et al. https://link.springer.com/article/10.1007/s10115-006-0037-3)
Result is stored to the file you specify after ">".
You can analyse the result in order to choose the best scorer model.

5) Summarize using the neural scorer:
e.g.
```
python summ_mod_ranks.py -le=10 \
-s=scorer_models/results_500epochs/sent_embs_normalized_scores_minmaxscaler.csv-activation_\<function\ tanh\ at\ 0x7fba2f489c80\>_None_fnih-1.0_fnih_2-1.0.model \
-e=../word_embedding_models/entity_model/model_chemdump_entities.w2v -p=. --summa_parent_directory_path=../textrank/
```

For the meaning of the parameters, please call python summ_mod_ranks.py --help
