import sys
import re
import numpy as np
import collections
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.cluster import KMeans
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.manifold import MDS
import matplotlib.pyplot as plt
import plot
import text_processing as tp
import heapq

### Main parameters

CORPUS_FILE = sys.argv[1]
#print('corpus file: ' + CORPUS_FILE)

### Clustering (Test)
N_chapters = int(sys.argv[2]) # Chapters
N_sections = int(sys.argv[3]) # Per chapter
N_docs     = int(sys.argv[4]) # Per section

Max_df = int(sys.argv[5])
Min_df = int(sys.argv[6])
Num_feats= int(sys.argv[7])


corpus_file = CORPUS_FILE

produce_sim_plot = False
produce_mds_plot = False
produce_dendogram = False




def order_clusters(matrix):
	N_clusters = matrix.shape[0]
	simmx = cosine_similarity(matrix)
	# Order clusters
	_, first = max([(centrality, idx) for idx, centrality in enumerate(sum(simmx))]) # Most central cluster
	queue = [first]
	while len(queue) < N_clusters:
		# Pick closest cluster
		sim, next = max([(centrality, idx) for idx, centrality in enumerate(sum(simmx)) if idx not in queue])
		#print(sim, queue[-1], next)
		queue.append(next)
	return queue

### Preparing data
"""data_filtered = [re.sub("-[LR].B-", "", doc) for doc in data_filtered]
data = [x.strip() for x in open(sys.argv[1]).readlines()]
data = [re.sub("-[LR].B-", "", doc) for doc in data]
data_filtered = [' '.join([term for term in doc.split() if len(term) > 1]) for doc in data]
"""

#print("Reading file...")
#(doc_names) = read_docnames(docnames_file)

data = [x.strip() for x in open(corpus_file).readlines()]
#print("Filtering...")
data_filtered = [' '.join([term for term in tp.clean(doc.split(),stem=False)]) for doc in data]


#print("Vectorizing %d documents..." % len(data))
tfidf_vectorizer = TfidfVectorizer(max_df=Max_df, max_features=Num_feats, min_df=Min_df,
#tfidf_vectorizer = TfidfVectorizer(max_df=0.9, max_features=5000, min_df=0.01,
	   use_idf=True, sublinear_tf=True,
	   tokenizer=str.split,
	   #tokenizer=stem,
	   stop_words='english',
	   ngram_range=(1,2))

tfidf_matrix = tfidf_vectorizer.fit_transform(data_filtered)


#print("Clustering with %d features..." % tfidf_matrix.shape[1])
#print("  - Clustering into %d chapters..." % N_chapters)
km = KMeans(n_clusters=N_chapters, random_state=0)
km.fit(tfidf_matrix)


feats = tfidf_vectorizer.get_feature_names()
dfs = np.mean(tfidf_matrix.toarray(), axis=0)
feat_dfs = dict(zip(feats,dfs))
tfidf_array = tfidf_matrix.toarray()

#print("  - Clustering into %d sections each..." % N_sections)
km_centroids = []
km2_centroids = []
chapter_outline = []
keywords = collections.defaultdict(lambda: collections.defaultdict(lambda: collections.defaultdict(lambda: 0)))
for i, ch in enumerate(order_clusters(km.cluster_centers_)):
	#print("    - Clustering chapter", i+1)
	km_centroids.append(km.cluster_centers_[ch])
	ch_members = [i for i,c in enumerate(km.labels_) if c==ch]
	# Second-level clustering
	km2 = KMeans(n_clusters=N_sections, random_state=0)
	km2.fit(tfidf_matrix[ch_members,:])
	#centroids.append(km2.cluster_centers_) # For visualization
	sects2docs_simmx = cosine_similarity(km2.cluster_centers_, tfidf_matrix)
	# Structure sections in chapter
	section_outline = []
	for j, section in enumerate(order_clusters(km2.cluster_centers_)):
		km2_centroids.append(km2.cluster_centers_[section])
		docs_in_section = [doc for doc, sect in zip(ch_members, km2.labels_) if sect==section]
		top_docs = heapq.nlargest(N_docs, zip(docs_in_section, sects2docs_simmx[section][docs_in_section]), key=lambda x:x[1])
		section_outline.append(top_docs)
		# Get section keywords from its documents
		for doc, score in top_docs:
			for word, tfidf in zip(feats, list(tfidf_array[doc])):
				if tfidf > 0:
					keywords[i][j][word] += tfidf

	chapter_outline.append(section_outline)


print('<!DOCTYPE html>\n'
'<html>\n'
'<head>\n'
'<meta name="viewport" content="width=device-width, initial-scale=1">\n'
'<style>\n'
'.collapsible {\n'
'    background-color: #777;\n'
'    color: white;\n'
'    cursor: pointer;\n'
'    padding: 18px;\n'
'    width: 100%;\n'
'    border: none;\n'
'    text-align: left;\n'
'    outline: none;\n'
'    font-size: 15px;\n'
'}\n'
'\n'
'.active, .collapsible:hover {\n'
'    background-color: #555;\n'
'}\n'
'\n'
'.content {\n'
'    padding: 0 18px;\n'
'    display: none;\n'
'    overflow: hidden;\n'
'    background-color: #f1f1f1;\n'
'}\n'
'.tooltip {\n'
'position: relative;\n'
'display: inline-block;\n'
'}\n'
'\n'
'.tooltip .tooltiptext {\n'
'visibility: hidden;\n'
'width: 1020px;\n'
'background-color: #555;\n'
'color: #fff;\n'
'text-align: center;\n'
'border-radius: 6px;\n'
'padding: 5px 0;\n'
'position: absolute;\n'
'z-index: 1;\n'
'bottom: 125%;\n'
'left: 50%;\n'
'margin-left: -360px;\n'
'opacity: 0;\n'
'transition: opacity 0.3s;\n'
'}\n'
'\n'
'.tooltip .tooltiptext::after {\n'
'content: "";\n'
'position: absolute;\n'
'top: 100%;\n'
'left: 50%;\n'
'margin-left: -5px;\n'
'border-width: 5px;\n'
'border-style: solid;\n'
'border-color: #555 transparent transparent transparent;\n'
'}\n'
'\n'
'.tooltip:hover .tooltiptext {\n'
'visibility: visible;\n'
'opacity: 1;\n'
'}\n'
'</style>\n'
'</head>\n'
'<body>\n')

## Print outline
#print("Outline:")
for i, chapter in enumerate(chapter_outline):
	ch_kws = collections.defaultdict(lambda: 0)
	for sect in keywords[i]:
		for word in keywords[i][sect]:
			ch_kws[word] += keywords[i][sect][word]
	top_kws = heapq.nlargest(8, ch_kws.items(), key=lambda x:x[1])
	print("  <button class=\"collapsible\"><h1>%d. %s</h1></button>" % (i+1, ', '.join([w for w,_ in top_kws])))
	print("  <div class=\"content\">  ")
	print("    <button class=\"collapsible\"><h2>" + str(i+1) + ".1. Introduction</h2></button>   <div class=\"content\"><p>DUMMY_INTRODUCTION_"+str(i+1)+"_CONTENT</p></div>")
	for j, section in enumerate(chapter):
		sect_kws = collections.defaultdict(lambda: 0)
		for word in keywords[i][j]:
			sect_kws[word] += keywords[i][j][word]
		top_kws = heapq.nlargest(8, sect_kws.items(), key=lambda x:x[1])
		print("    <button class=\"collapsible\"><h2>%d.%d. %s</h2></button>" % (i+1, j+2, ', '.join([w for w,_ in top_kws])))
		print("    <div class=\"content\">  ")
		for k, doc in enumerate(section):
			doc_idx, score = doc
			print("      <button class=\"collapsible\"><h3>%d.%d.%d. %s (doc:%d/rel:%.1f%%)</h3></button>   <div class=\"content\"><p>DUMMY_PAPER_%d.%d.%d._CONTENT</p></div>" % (i+1, j+2, k+1, data[doc_idx].split('|')[0], doc_idx, score*100, i+1, j+2, k+1))
		print("    </div>  ")
	print("    <button class=\"collapsible\"><h2>" + str(i+1) + "." + str(N_sections+2) + ". Conclusion</h2></button>   <div class=\"content\"><p>DUMMY_CONCLUSION_"+str(i+1)+"_CONTENT</p></div>")
	print("    <button class=\"collapsible\"><h2>" + str(i+1) + "." + str(N_sections+3) + ". Related Work</h2></button>   <div class=\"content\"><p>DUMMY_RELATEDWORK_"+str(i+1)+"_CONTENT</p></div>")
	print("    <button class=\"collapsible\"><h2>" + str(i+1) + "." + str(N_sections+4) + ". References</h2></button>   <div class=\"content\"><p>DUMMY_REFERENCES_"+str(i+1)+"_CONTENT</p></div>")
	print("  </div>  ")

### Visualization: scatter plot of chapter/section centroids and selected documents, in 2D projection
import functools
project_selected_docs_only = True # ..or all docs
# Scatter plot of chapter and section clusters in 2D
selected_docs = [x for x,_ in functools.reduce(lambda a,b: a+b, functools.reduce(lambda a,b: a+b, chapter_outline))]
if project_selected_docs_only:
	km2_dists = 1-cosine_similarity(np.concatenate([np.array(km2_centroids), np.array(km_centroids), tfidf_matrix.toarray()[selected_docs]]))
else:
	km2_dists = 1-cosine_similarity(np.concatenate([np.array(km2_centroids), np.array(km_centroids), tfidf_matrix.toarray()]))

mds = MDS(n_components=2, dissimilarity="precomputed", random_state=4)
pos = mds.fit_transform(km2_dists)  # shape (n_components, n_samples)
#uncomment to show or save plot.

plt.scatter(pos[:N_chapters*N_sections,0], pos[:N_chapters*N_sections,1], s=200 ,c=functools.reduce(lambda a,b: a+b, [[i]*N_sections for i in range(N_chapters)]))
plt.scatter(pos[N_chapters*N_sections:(N_chapters*(N_sections+1)),0], pos[N_chapters*N_sections:(N_chapters*(N_sections+1)),1], s=600 ,c=range(N_chapters), alpha=0.3)
if project_selected_docs_only:
	plt.scatter(pos[(N_chapters*(N_sections+1)):,0],pos[(N_chapters*(N_sections+1)):,1], s=20, c=np.array(order_clusters(km.cluster_centers_))[km.labels_[selected_docs]])
else:
	plt.scatter(pos[(N_chapters*(N_sections+1)):,0][selected_docs],pos[(N_chapters*(N_sections+1)):,1][selected_docs], s=20, c=np.array(order_clusters(km.cluster_centers_))[km.labels_[selected_docs]])

#plt.text(x,y,text)
i=0
for ch in range(N_chapters):
	for sect in range(N_sections):
		plt.text(pos[i,0]+0.01,pos[i,1],"%d.%d"%(ch+1,sect+1))
		i+=1

plt.savefig(CORPUS_FILE + '_plt.png')

#plt.show()


print('<script>\n'
'var coll = document.getElementsByClassName("collapsible");\n'
'var i;\n'
'\n'
'for (i = 0; i < coll.length; i++) {\n'
'    coll[i].addEventListener("click", function() {\n'
'        this.classList.toggle("active");\n'
'        var content = this.nextElementSibling;\n'
'        if (content.style.display === "block") {\n'
'            content.style.display = "none";\n'
'        } else {\n'
'            content.style.display = "block";\n'
'        }\n'
'    });\n'
'}\n'
'</script>\n'
'\n'
'</body>\n'
'</html>\n')

"""
def sim_plot(tfidf_matrix,doc_names):
    print("Producing similarity matrix plot...")
    similarity_matrix = cosine_similarity(tfidf_matrix)
    fig, ax = plt.subplots(figsize=(280,320))
    cax = ax.matshow(similarity_matrix, interpolation='nearest')
    plt.xticks(range(len(doc_names)), doc_names, rotation=90);
    plt.yticks(range(len(doc_names)), doc_names);
    plt.tick_params(axis='both', which='major', labelsize=30)
    print('Saving similarity matrix figure...')
    fig.savefig("plots/" + 'sim.png', dpi = 30)   # save the figure to file

doc_names = range(1, len(data)+1)
print(doc_names)
sim_plot(tfidf_matrix,doc_names)
"""



"""

# Cluster again.
if(produce_mds_plot):
	mds_plot(tfidf_matrix)

if(produce_dendogram):
	dendogram(tfidf_matrix,doc_names)
"""
