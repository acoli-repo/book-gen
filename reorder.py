import sys
import os
import gensim
import re
import random
import csv
import logging
import numpy as np
# import theanets
import json
# from sklearn.metrics import classification_report, confusion_matrix

import operator

from random import random
from gensim.models import KeyedVectors
### Main program
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)


path = ''

# d2vFile = "300dimStreamedChemPhrases/model_streamed_topic.w2v"
d2vFile = "doc2vec/samuel_ngrams_model/model_chemdump_ngrams.w2v"


threshold = 6 #7
sim_threshold = 0.5 #0.4
importance = 0 #20

if "-t" in sys.argv:
	for i in range(len(sys.argv)):
		if sys.argv[i] == "-t":
			if (sys.argv[i+1][:sys.argv[i+1].find(".")] + sys.argv[i+1][sys.argv[i+1].find(".")+1:]).isdigit():
				threshold = float(sys.argv[i+1])
			else:
				input(sys.argv[i+1] + " is not a valid number. " + str(threshold) + " used as default. \nPress Enter to confirm. ")
			sys.argv = sys.argv[:i] + sys.argv[i+2:]
			break

if "-m" in sys.argv:
	for i in range(len(sys.argv)):
		if sys.argv[i] == "-m":
			if (sys.argv[i+1][:sys.argv[i+1].find(".")] + sys.argv[i+1][sys.argv[i+1].find(".")+1:]).isdigit():
				sim_threshold = float(sys.argv[i+1])
			else:
				input(sys.argv[i+1] + " is not a valid number. " + str(sim_threshold) + " used as default. \nPress Enter to confirm. ")
			sys.argv = sys.argv[:i] + sys.argv[i+2:]
			break

if "-i" in sys.argv:
	for i in range(len(sys.argv)):
		if sys.argv[i] == "-i":
			if (sys.argv[i+1][:sys.argv[i+1].find(".")] + sys.argv[i+1][sys.argv[i+1].find(".")+1:]).isdigit():
				importance = float(sys.argv[i+1])
			else:
				input(sys.argv[i+1] + " is not a valid number. " + str(importance) + " used as default. \nPress Enter to confirm. ")
			sys.argv = sys.argv[:i] + sys.argv[i+2:]
			break

black = False
if "-black" in sys.argv:
	black = True
	for i in range(len(sys.argv)):
		if sys.argv[i] == "-black":
			sys.argv = sys.argv[:i] + sys.argv[i+1:]
			break

plain = False
if "-plain" in sys.argv:
	plain = True
	for i in range(len(sys.argv)):
		if sys.argv[i] == "-plain":
			sys.argv = sys.argv[:i] + sys.argv[i+1:]
			break


if len(sys.argv) == 1:
	input("No folder given. Current folder used. \nPress Enter to confirm. ")


elif sys.argv[1] == "-h":
	print("folder [doc2vecmodel] [-t 7] [-m 0.4]")

else: 
	path = sys.argv[1]
	if path[-1] != "/":
		path += "/"

	if len(sys.argv) > 2:
		d2vFile = sys.argv[2]

### Main program
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)


# Load word vectors
vectors = KeyedVectors.load(d2vFile, mmap='r')

# print(vectors.most_similar("crops"))
# input("->")

# opening files

thisFileName = path + "section-sentences-restructured-syn-pos.txt"
if not os.path.exists(thisFileName):
	print("no synonym pos file found")
	thisFileName = path + "section-sentences-restructured-pos.txt"
	if not os.path.exists(thisFileName):
		print("no restructured pos file found")
		thisFileName = path + "section-sentences-pos.txt"
f1 = open(thisFileName)
pos = f1.read()
f1.close()
pos = pos.split("\n")[:-1]
for i in range(len(pos)):
	pos[i] = pos[i].split(" ")[:-1]



thisFileName = path + "section-sentences-restructured-syn-tok.txt"
if not os.path.exists(thisFileName):
	print("no synonym tok file found")
	thisFileName = path + "section-sentences-restructured-tok.txt"
	if not os.path.exists(thisFileName):
		print("no restructured tok file found")
		thisFileName = path + "section-sentences-tok.txt"
f1 = open(thisFileName)
raw = f1.read()
f1.close()
raw = raw.split("\n")[:-1]
for i in range(len(raw)):
	raw[i] = raw[i].split(" ")[:-1]


thisFileName = path + "section-sentences-ids.txt"
if not os.path.exists(thisFileName):
	thisFileName += ".txt"
f1 = open(thisFileName)
ids = f1.read()
f1.close()
ids = ids.split("\n")[1:-1]
for i in range(len(ids)):
	ids[i] = ids[i].split(",")




f1 = open(path + "section-sentences-restructured-meta.tsv")
meta = f1.read()
f1.close()
meta = meta.split("\n")[:-1]
for i in range(len(meta)):
	meta[i] = meta[i].split("\t")


# # opening the files with the frist sentences

thisFileName = path + "abstract-sentences-restructured-syn-pos.txt"
if not os.path.exists(thisFileName):
	print("no synonym abstract pos file found")
	thisFileName = path + "abstract-sentences-restructured-pos.txt"
	if not os.path.exists(thisFileName):
		print("no abstract restructured pos file found")
		thisFileName = path + "abstract-sentences-pos.txt"
if os.path.exists(thisFileName):

	f1 = open(thisFileName)
	fst_pos = f1.read()
	f1.close()
	fst_pos = fst_pos.split("\n")[:-1]
	for i in range(len(fst_pos)):
		fst_pos[i] = fst_pos[i].split(" ")[:-1]



	thisFileName = path + "abstract-sentences-restructured-syn-tok.txt"
	if not os.path.exists(thisFileName):
		print("no synonym abstract tok file found")
		thisFileName = path + "abstract-sentences-restructured-tok.txt"
		if not os.path.exists(thisFileName):
			print("no abstract restructured tok file found")
			thisFileName = path + "abstract-sentences-tok.txt"
	f1 = open(thisFileName)
	fst_raw = f1.read()
	f1.close()
	fst_raw = fst_raw.split("\n")[:-1]
	for i in range(len(fst_raw)):
		fst_raw[i] = fst_raw[i].split(" ") #[:-1]



	f1 = open(path + "abstract-sentences-ids.txt")
	fst_ids = f1.read()
	f1.close()
	fst_ids = fst_ids.split("\n")[1:-1]
	for i in range(len(fst_ids)):
		fst_ids[i] = fst_ids[i].split(",")


	f1 = open(path + "abstract-sentences-restructured-meta.tsv")
	fst_meta = f1.read()
	f1.close()
	fst_meta = fst_meta.split("\n")[:-1]
	for i in range(len(fst_meta)):
		fst_meta[i] = fst_meta[i].split("\t")

else:
	print("no abstract found at all")
	fst_pos  = []
	fst_raw  = []
	fst_ids  = []
	fst_meta = []

# # adding the first sentences to the list of all sentences
# to seperate the first sentences later on 
sent_count = len(raw)

raw  += fst_raw
pos  += fst_pos
ids  += fst_ids
meta += fst_meta

# raw = [["lithium"]]

allWords = {}

wholeSize = 0
for s in raw:
	for w in s:
		wholeSize += 1
		if w in allWords:
			allWords[w] += 1
		else:
			allWords[w] = 1

corpusSize = 0
for w in vectors.wv.vocab:
	corpusSize += vectors.wv.vocab[w].count
			
for w in allWords:
	if w in vectors.wv.vocab:
		# print (w, allWords[w], (allWords[w]/wholeSize)/(vectors.wv.vocab[w].count/corpusSize), sep = "\t")
		allWords[w] = (allWords[w]/wholeSize)/(vectors.wv.vocab[w].count/corpusSize)


# sortedWords = sorted(allWords.items(), key=operator.itemgetter(1))
# for w in sortedWords:
# 	if type(w[1]) == int:
# 		print(w[0], "\tnot in corpus")
# 	else:
# 		print(w[0], "\t", w[1])

tags = {}

sent_tags = {}

for i in range(len(raw)):
	sent_tags[i] = {}

imp_sent = range(len(raw))

def addToTag(sentence, word):
	if word.isdigit():
		return

	if (len(word) > 0): # and (word in vectors):
		if word in tags:
			tags[word][sentence] = True
		else:
			tags[word] = {sentence: True}

		if sentence in sent_tags:
			sent_tags[sentence][word] = True
		else:
			sent_tags[sentence] = {word: True}




for sentence in range(len(pos)):
	for word in range(len(pos[sentence])):
		if importance:
			if (allWords[raw[sentence][word]] > importance) or (type(allWords[raw[sentence][word]]) == int): # 1e-8:
				addToTag(sentence, raw[sentence][word])

		else:
			if (pos[sentence][word].startswith("NN")):
				addToTag(sentence, raw[sentence][word])
				continue

			if ("-" in raw[sentence][word]) and not(raw[sentence][word].endswith("-")):
				addToTag(sentence, raw[sentence][word])
				continue

			if not (raw[sentence][word].endswith("-")):
				for x in raw[sentence][word][1:]:
					if x.isupper() or x.isdigit():
						addToTag(sentence, raw[sentence][word])
						break

			if (len(pos[sentence])-2 >= word) and ((pos[sentence][word] == "NN") and (pos[sentence][word+1] == "NN")) or ((pos[sentence][word] == "JJ") and (pos[sentence][word+1] == "NN")):
				addToTag(sentence, raw[sentence][word] + "_" + raw[sentence][word+1])




# for sentence in range(len(pos)):
# 	for word in range(len(pos[sentence])):
# 		if raw[sentence][word] in tags:
# 			addToTag(sentence, raw[sentence][word])



ord_sent = []

for i in range(len(raw)):
	ord_sent.append(i)


def getColor(doc):
	doc = int(doc)
	r = int(((((575*doc+313)) % 907 ) % 20 + 2)*10)
	g = int(((((612*doc+741)) % 1223) % 20 + 3)*10)
	b = int(((((754*doc+329)) % 761 ) % 20 + 1)*10)

	def fillNeros(x):
		while len(x) < 6:
			x = "0" + x
			
		return x

	return '#' + fillNeros(hex( (r*256 + g)*256 + b )[2:])

neworder = [ord_sent[0]]
# ord_sent = ord_sent[1:]

html = ["<p>"] #+ " ".join(raw[neworder[-1]])

def format(outputLine, maxi):
	output = outputLine
	# while True:
	# 	# output = output.replace('` <font style="text-decoration: underline">', "`")
	# 	# output = output.replace("</font> </font>", "</font>")
	# 	# output = output.replace('<font style="text-decoration: underline"> <font style="text-decoration: underline">', '<font style="text-decoration: underline">')
	# 	# output = output.replace("'</font> '", "''")

	# 	# print(output)
	# 	if output == outputLine:
	# 		break
	# 	outputLine = output

	outputLine = output.replace("` ", "`").replace(" '", "'").replace('underline"> ', 'underline">').replace(" </font>", "</font>")

	html = ""
	if (not plain) and (maxi >= sent_count):
		html += "<b>"

	if not black:
		html += '<font color="' + getColor(ids[maxi][1]) + '">'

	html += outputLine
	# html += untermaxi(maxi) 

	if not black:
		html += '''</font>  ''' + '<font color="#000000">'

	if plain:
		html += "<!-- "

	if maxi < sent_count:
		# print(maxi, ids[maxi])
		html += "(sentID:" + ids[maxi][0] + ',doc:' + ids[maxi][1] + ',origSent:' + ids[maxi][3] + ")"
	else:
		html += '''(doc:''' + str(ids[maxi][1]) + ''',abstract Sentence) '''

	if (not plain) and (maxi >= sent_count):
		html += "</b>"

	if int(meta[maxi][1]) < 11:
		html += " -- ok"
	else:
		html += " -- <i> LCS: " + str(meta[maxi][1]) + "</i>"

	if plain:
		html += " -->"

	html += "<br/>"

	return html

outputLast = ""

while ord_sent:
	sims = []
	tag_counts = []
			
	max_sim = 0
	maxi = ord_sent[0]

	outputLine = " ".join(raw[ord_sent[0]])
	# outputLast = html[-1]

	# looking for the most similar sentence to come next 
	for i in ord_sent:
		lastLine = outputLast
		thisLine = " ".join(raw[i])
		sim = 0
		
		for y in sent_tags[i]: # raw[i]:
			for x in sent_tags[neworder[-1]]: # raw[neworder[-1]]: 
				if (x in vectors) and (y in vectors):
					this_sim = vectors.similarity(x, y)
					if this_sim >= sim_threshold:
						sim += 1 # this_sim #1
						if (not plain):
							if not x in '<font style="text-decoration: underline">':
								if (x.replace("_", " ") + " '") in lastLine:
									lastLine = (" " + lastLine).replace(x.replace("_", " ") + " '", x.replace("_", " ") + " ''")
								else:
									lastLine = (" " + lastLine).replace(" " + x.replace("_", " ") + " ", ' <font style="text-decoration: underline"> ' + x.replace("_", " ") + " '</font> ")

							if outputLast and (not y in '<font style="text-decoration: underline">'):
								if ("` " + y.replace("_", " ")) in thisLine:
									thisLine = (" " + thisLine).replace("` " + y.replace("_", " "), '`` ' + y.replace("_", " "))
								else:
									thisLine = (" " + thisLine).replace(" " + y.replace("_", " ") + " ", ' <font style="text-decoration: underline">` ' + y.replace("_", " ") + ' </font> ')

							# else:
								# print(y)


				elif x == y:
					sim += 1

		# bias on fist sentences
		if (i > sent_count) and (neworder[-1] <= sent_count):
			sim *= 1.2
			# and especially when its from the same document
			if ids[i][1] == ids[neworder[-1]][1]:
				sim *= 1.7
				# and especially when its the last sentence from the document
				if len([x for x in ord_sent if ids[x][1] == ids[i][1]]) == 1:
					sim *= 5

		if (sim > max_sim) and (sim > threshold):
			max_sim = sim
			maxi = i
			outputLast = lastLine
			outputLine = thisLine
	
	while outputLast.startswith(" "):
		outputLast = outputLast[1:]
	
	if (outputLast):

		# output += 

		html += [format(outputLast, neworder[-1])]

	outputLast = outputLine

	neworder += [maxi]
	ord_sent = [s for s in ord_sent if s != maxi] #ord_sent[:maxi] + ord_sent[maxi+1:]


html += [format(outputLast, neworder[-1])]

# 	if last_doc != ids[maxi][1]:
# 		html += "</p><p>\n"




	# print (maxi, ord_sent)
	
	# print(max_sim)
	# print(" ".join(raw[neworder[-1]]))

html = "\n".join(html)

ord_sent = neworder


def unterline(line):
	res = ""
	skip = False
	for i in range(len(raw[line])):
		if skip:
			skip = False
			continue

		if (not plain) and (raw[line][i] in tags):
			res += '<font style="text-decoration: underline">' + raw[line][i] + '</font> '

		elif (not plain) and (i < len(raw[line])-2) and ((raw[line][i] + "_" + raw[line][i+1]) in tags):
			res += '<font style="text-decoration: underline">' + raw[line][i] + " " + raw[line][i+1] + '</font> '
			skip = True

		else:
			res += raw[line][i] + ' '

	return res

# html = "<p>\n"

# last_doc = 0

# for line in ord_sent:
# 	if last_doc != ids[line][1]:
# 		html += "</p><p>\n"

# 	if (not plain) and (line > sent_count):
# 		html += "<b>"


# 	if not black:
# 		html += '<font color="' + getColor(ids[line][1]) + '">'

# 	html += unterline(line) 

# 	if not black:
# 		html += '''</font> '''

# 	if plain:
# 		html += "<!-- "

# 	if line < sent_count:
# 		# print(line, ids[line])
# 		html += "(sentID:" + ids[line][0] + ',doc:' + ids[line][1] + ',origSent:' + ids[line][3] + ")"
# 	else:
# 		html += '''(doc:'''+str(ids[line][1])+''',abstract Sentence) '''

# 	if plain:
# 		html += " -->"

# 	if (not plain) and (line > sent_count):
# 		html += "</b>"

# 	html += "<br/>\n"

# 	last_doc = ids[line][1]


html += "</p>"

f1=open(path + "reordered.html", "w")
f1.write(html)
f1.close()