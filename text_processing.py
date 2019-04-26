from gensim import corpora, models
import gensim
import nltk
import re
from nltk.stem import *
from nltk.corpus import stopwords
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

# Choose a stemmer
stemmer = SnowballStemmer("english") # improvement over Porter: 
# https://stackoverflow.com/questions/10554052/what-are-the-major-differences-and-benefits-of-porter-and-lancaster-stemming-alg
#stemmer = PorterStemmer()


def read_docnames(docnames):
    with open(docnames, "r") as f:
        titles = []
        for line in f:
            line_str = line.strip()
            titles.append(line_str)
    f.close()
    return (titles)


def clean(list_to_clean,stem):
    
    
    #items_to_clean = set(list(stopwords.words('english')) + ['\n','\n\n','\n\n\n','\n\n\n\n','',' '])
    regex_non_alphanumeric = re.compile('[^0-9a-zA-Z]')  # REGEX for non alphanumeric chars
    for index,item in enumerate(list_to_clean):
        item = regex_non_alphanumeric.sub('', item)  # Filter text, remove non alphanumeric chars
        item = item.lower()  # Lowercase the text
        item = re.sub('[lrcLRC][rscRSC]b', '', item) # brackets
        item = re.sub('\d\.?\d*', '', item) # digits
        item = re.sub('~', '', item) # 
        item = re.sub(',\s*', '', item) # 
        item = re.sub('\"', '', item)
        item = re.sub('[^A-Za-z0-9]+', '', item)
        #item = re.sub('CR\d+', '', item) # citation references.
        item = re.sub('entity', '', item) # entities.
        item = re.sub('mah', '', item) # entities.
        item = re.sub('lib', '', item) # 
        item = re.sub('libs', '', item) # 
        
        if(stem):
            item = stemmer.stem(item)  # Stem the text
        if len(item) < 3:  # If the length of item is lower than 3, remove item
            item = ''
        list_to_clean[index] = item  # Put item back to the list
    cleaned_list = list_to_clean #[elem for elem in list_to_clean if elem not in items_to_clean]
    # Remove empty items from the list
    #print(cleaned_list)
    return cleaned_list

def stem(text):
    filtered_tokens = []
    for token in text.split():
        if re.search('[a-zA-Z]', token):
            filtered_tokens.append(token)
    stems = [stemmer.stem(t) for t in filtered_tokens]
    return stems


def tokenize_and_stem(text):
    # first tokenize by sentence, then by word to ensure that punctuation is caught as it's own token
    #tokens = [word for sent in nltk.sent_tokenize(text) for word in nltk.word_tokenize(sent)]
    tokens = [word for sent in nltk.sent_tokenize(text) for word in nltk.word_tokenize(sent)]
    filtered_tokens = []
    # filter out any tokens not containing letters (e.g., numeric tokens, raw punctuation)
    for token in tokens:
        if re.search('[a-zA-Z]', token):
            filtered_tokens.append(token)
    stems = [stemmer.stem(t) for t in filtered_tokens]
    return stems