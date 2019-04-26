""" Convert JSON format to text 

Sample call: python3 json2txt.py data/corpus.json > data/corpus.txt

"""

import json
import sys

data = json.load(open(sys.argv[1]))

ANNO = 0 # 0=token, 1=pos, 2=lemma, 3=named entity tag

docs = []
#fields = ['titleTokens', 'abstrTokens', 'introductionTokens', 'conclusionTokens']
for datum in data:
    test = []
    sections = []
    #for field in ['titleTokens', 'abstrTokens', 'introductionTokens']:
    for field in ['titleTokens']+list(datum.keys()):#fields:
        if 'Tokens' not in field:
            continue
        if not datum[field]:
            continue
        for sent in datum[field]:
            try:
                # list of sentences.
                test.append(' '.join(sent)) # only used to evoke typeerror case.
                tokens=[]
                for i in range(len(sent)):
                    tokens.append(str(sent[i]).split("|")[ANNO]) 
                sections.append(' '.join(tokens))
            except TypeError:
                # list of sentences of sentences.
                section = sent
                sents = []
                for sent in section:
                    tokens=[]
                    for i in range(len(sent)):
                        tokens.append(str(sent[i]).split("|")[ANNO])
                    sents.append(' '.join(tokens))
                sections.append(' | '.join(sents))
    doc = ' | '.join(sections)
    docs.append(doc)
    print(doc)
    
                #tokens=[]
                #for i in range(len(sent)):
                #    tokens.append(str(sent[i]).split("|")[0])
                #sections.append(' '.join(tokens))#
