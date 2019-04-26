import matplotlib.pyplot as plt
import matplotlib as mpl
import pandas as pd
from scipy.cluster.hierarchy import ward, dendrogram
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.manifold import MDS
from text_processing import *
from sklearn.cluster import KMeans
import matplotlib.pyplot as plt


def mds_plot(tfidf_matrix):
    km = KMeans(n_clusters=50)
    km.fit(tfidf_matrix)

    km2 = KMeans(n_clusters=5)
    km2.fit(km.cluster_centers_)

    km_dists = 1-cosine_similarity(km.cluster_centers_)
    mds = MDS(n_components=2, dissimilarity="precomputed", random_state=4)
    pos = mds.fit_transform(km_dists)  # shape (n_components, n_samples)
    plt.scatter(pos[:,0], pos[:,1], s=200 ,c=km2.labels_)
    plt.show()

def sim_plot(tfidf_matrix,doc_names):
    print("Producing similarity matrix plot...")
    similarity_matrix = cosine_similarity(tfidf_matrix)
    fig, ax = plt.subplots(figsize=(280,320))
    cax = ax.matshow(similarity_matrix, interpolation='nearest')
    plt.xticks(range(len(doc_names)), doc_names, rotation=90);
    plt.yticks(range(len(doc_names)), doc_names);
    plt.tick_params(axis='both', which='major', labelsize=30)
    print('Saving similarity matrix figure...')
    fig.savefig("../plots/" + 'sim.png', dpi = 30)   # save the figure to file

def dendogram(tfidf_matrix,doc_names):
    similarity_matrix = cosine_similarity(tfidf_matrix)
    linkage_matrix = ward(similarity_matrix) # Define the linkage_matrix using ward clustering pre-computed distances
    mpl.rcParams['lines.linewidth'] = 2

    fig, ax = plt.subplots(figsize=(200, 80)) # Set size
    ax = dendrogram(linkage_matrix, orientation="right", labels=doc_names);

    plt.tick_params(\
        axis= 'x',
        which='both',
        bottom='off',
        top='off',
        labelbottom='off',
        length = 25)
    plt.tick_params(\
        axis= 'y',
        which='both',
        bottom='off',
        top='off',
        labelbottom='off',
        labelsize = 20)
    plt.tick_params(width=200, length = 50)
    #plt.tight_layout() # Show plot with tight layout
    print('saving dendogram figure...')
    fig.savefig("../plots/" + 'dendogram.png', dpi = 35)#, format='svg', dpi=1200) 
    # save the figure to file
    #plt.show()