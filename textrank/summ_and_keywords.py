from summa import summarizer
from summa import keywords

import sys

orig_text = sys.argv[1]
text = orig_text.split('\n')
text = [sent.split() for sent in text]

thresh = sys.argv[2]
which = sys.argv[3] # ratio or words

#print('Keywords:')
print(keywords.keywords(orig_text))
print('</separator>')
#print('Textrank summary')

if which == 'words':
    summary = summarizer.summarize(text, words=int(thresh))
else:
    summary = summarizer.summarize(text, ratio=float(thresh))
print(summary)