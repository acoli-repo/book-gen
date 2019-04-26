import sys

asterisk = ""

def father(w, sent):
	for s in sent:
		
		if w[8] == s[0]:
			return s

	# input(w)
	# return ([""]*20)
	return []

def sons(w, sent):
	res = []
	if not w:
		return []
	
	# print(sent)

	for s in sent:
		# print("")
		# for x in s:
			# print(x, end=", ")
		if s and (s[8] == w[0]):
			res += [s]

	return res

def leftSons(w, sent):
	res = []
	ss = sons(w, sent)
	for s in sent:
		if s == w:
			return res
		if s in ss:
			res += [s]

	return res

def rightSons(w, sent):
	res = []
	ss = sons(w, sent)
	for s in sent[::-1]:
		if s == w:
			return res
		if s in ss:
			res += [s]

	return res

def offspring(w, sent):
	res = []
	# print(w)
	if w[4] == "``":
		inside = False
		res = []
		for s in sent:
			if inside:
				res += [s]

			if s == w:
				inside = True
				# print("+")

			if s[4] == "''":
				break
		
		return res



	for s in sons(w, sent):
		if s:
			res += [s] + offspring(s, sent)


	return res



def leftOffspring(w, sent):
	res = []
	ss = offspring(w, sent)
	for s in sent:
		if s == w:
			return res
		if s in ss:
			res += [s]

	return res

def leftOffspring(w, sent):
	res = []
	ss = offspring(w, sent)
	for s in sent[::-1]:
		if s == w:
			return res
		if s in ss:
			res += [s]

	return res


def root(sent):
	for s in sent:
		if s[10] == "ROOT":
			return s

	# return ([""]*20)
	return []


def purebredVerb(w, sent):
	# print(w)
	if not w:
		return False

	if w[10] == "ROOT":
		return True

	if not w[4].startswith("V"):
		return False

	return purebredVerb(father(w, sent), sent)


def refNoun(w, sent):
	for i in range(len(w)):
		if w[i].startswith("R-A"):
			# print(i)
			for s in sent:
				# print(s)
				if (s != w) and (len(s) > i) and (w[i].endswith(s[i])):
					return s
			# break

	# return ([""]*20)
	return []

def checkQuote(t, s = []):
	hx = [u[4] for u in t]
	return hx.count("''") == hx.count("``")

def addToNeworder(neworder, word):
	# return neworder + word[1] + " "
	return neworder + [word]


def neworderInit():
	# return ""
	return []


present =	["beat",	"become",	"begin",	"bend",	"bet",	"bid",	"bite",		"blow",		"break",	"bring",	"build",	"buy",		"catch",	"choose",	"come",	"cost",	"cut",	"dig",	"dive",		"do",	"draw",		"drive",	"drink",	"eat",		"fall",		"feel",	"fight",	"find",		"fly",		"forget",		"forgive",	"freeze",	"get",		"give",		"go",	"grow",		"hang",	"have",	"hear",		"hide",		"hit",	"hold",	"hurt",	"keep",	"know",		"lay",	"lead",	"leave",	"lend",	"let",	"lie",	"lose",	"make",	"mean",		"meet",	"pay",	"put",	"read",	"ride",		"ring",	"rise",		"run",	"say",	"see",	"sell",	"send",	"show",		"shut",		"sing",		"sit",		"sleep",	"speak",	"spend",	"stand",	"swim",		"take",		"teach",		"tear",		"tell",		"think",	"throw",	"understand",	"wake",		"wear",	"win",	"write"		]
past =		["beat",	"became",	"began",	"bent",	"bet",	"bid",	"bit",		"blew",		"broke",	"brought",	"built",	"bought",	"caught",	"chose",	"came",	"cost",	"cut",	"dug",	"dove",		"did",	"drew",		"drove",	"drank",	"ate",		"fell",		"felt",	"fought",	"found",	"flew",		"forgot",		"forgave",	"froze",	"got",		"gave",		"went",	"grew",		"hung",	"had",	"heard",	"hid",		"hit",	"held",	"hurt",	"kept",	"knew",		"laid",	"led",	"left",		"lent",	"let",	"lay",	"lost",	"made",	"meant",	"met",	"paid",	"put",	"read",	"rode",		"rang",	"rose",		"ran",	"said",	"saw",	"sold",	"sent",	"showed",	"shut",		"sang",		"sat",		"slept",	"spoke",	"spent",	"stood",	"swam",		"took",		"taught",		"tore",		"told",		"thought",	"threw",	"understood",	"woke",		"wore",	"won",	"wrote"		]
particip =	["beaten",	"become",	"begun",	"bent",	"bet",	"bid",	"bitten",	"blown",	"broken",	"brought",	"built",	"bought",	"caught",	"chosen",	"come",	"cost",	"cut",	"dug",	"dived",	"done",	"drawn",	"driven",	"drunk",	"eaten",	"fallen",	"felt",	"fought",	"found",	"flown",	"forgotten",	"forgiven",	"frozen",	"gotten",	"given",	"gone",	"grown",	"hung",	"had",	"heard",	"hidden",	"hit",	"held",	"hurt",	"kept",	"known",	"laid",	"led",	"left",		"lent",	"let",	"lain",	"lost",	"made",	"meant",	"met",	"paid",	"put",	"read",	"ridden",	"rung",	"risen",	"run",	"said",	"seen",	"sold",	"sent",	"shown",	"shut",		"sung",		"sat",		"slept",	"spoken",	"spent",	"stood",	"swum",		"taken",	"taught",		"torn",		"told",		"thought",	"thrown",	"understood",	"woken",	"worn",	"won",	"written"	]

# https://languageonschools.com/english-irregular-verbs-list/

def isPast(word):
	# if not word[4].endswith("D") == (word[1].endswith("ed") or word in past):
	# 	input(word)

	return word[4].endswith("D")

def getParticip(word):
	# if word.endswith("s"):
	# 	word = word[:-1]

	for i in range(len(past)):
		if word[2] == present[i]:
			return particip[i]

	word = word[2]

	if word.endswith("e"):
		return word + "d"

	return word + "ed"


def upperCase(t, s):
	for w in s:
		if w in t:
			w[1] = w[1][0].upper() + w[1][1:]
			return t


	# t[0][1] = t[0][1][0].upper() + t[0][1][1:]
	return t

def lowerCase(t, s):
	for w in s:
		if w in t:
			if (not w[4].startswith("NNP")) and (w[1][1:].islower() or not (w[1][1:])):
				w[1] = w[1][0].lower() + w[1][1:]
			# else:
			# 	print(w)
			return t

def isConected(t, s):
	state = 0
	for x in s:
		# if x in t:
		# 	count += 1
		# 	if count == l:
		# 		return True
		if (state == 0) and (x in t):
			state = 1
		elif (state == 1) and (not x in t):
			state = 2
		elif (state == 2) and (x in t):
			return False

	return True



# # konkinationen (X and Y -> Y and Y)

def concatenationIntern(s, beginning):
	i = beginning-1
	inQuote = False
	for w in s[beginning:]:
		if w[4] == "``":
			inQuote = True
		if inQuote:
			if w[4] == "''":
				inQuote == False
			continue

		i += 1

		if (w[4] == "CC")   or   (w[1] == "as") and (([u[1] for u in (rightSons(w, s)[-2:])] == ["as", "well"])):

			# if not (w[4] == "CC"):
			# 	# print ([u[1] for u in (rightSons(w, s)[-3:])])
			# 	print(w)
			# 	print(" ".join([u[1] for u in s]))
			# 	# continue

			cc = [w]
			if w[1] == "as":
				cc += (rightSons(w, s)[-2:])

			snd = [u for u in offspring(w, s) if (not u in cc)]

			fst = []

			if not snd:
				# print("snd empty")
				# print(w)
				# return (threeItemConcat(s, i))
				continue

			s0 = []
			for x in sons(w, s):
				if not x[1] in [",", "well", "as"]:
					# if s0:
					# 	print(s0[1], x[1])
					s0 = x
					break

			f = father(w, s)
			f0 = f
			# print(f0)
			if not f:
				# print("whos your dady")
				continue

			fst = [f] + [o for o in offspring(f, s) if not o in (snd + cc)]
			# print(fst)

			for u in fst:
				if u[1].isdigit():
					for u in snd:
						if u[1].isdigit():
							return []
							break
					break


			if (not isConected(fst, s)) or (not isConected(snd, s)):
				continue

			# contin = False
			for u in snd: # aufweichen? 
				if u[2] in ["both", "other", "others", "same", "neither", "even"]:
					# print("both", len(fst), len(snd))
					return (threeItemConcat(s, i))
					# continue

			for u in s:
				if u in fst:
					if (u[2] in ["both"]):
						return []

					break


			for x in sons(w, s):
				if (x[4] in ["CD", "RB"])   or   (x[4][0] in ["V"] and (not leftSons(sons(w, s)[0], s)) and (leftSons(f, s))):
					# fst = [y for y in fst if not y in leftOffspring(f, s)]

					if w[1] == "as":
						continue

					# print(x)
					# print("!!!!!!!!!!!")
					# print("sons", (fst), (snd))

					return (threeItemConcat(s, i))
				# elif not x[4][0] in ["N", "J", "V"]:
				# 	print(x)



			if len(snd) > 3 + 2*len(fst):
				return (threeItemConcat(s, i))

			if len(fst) > 3 + 2*len(snd):
				# return (threeItemConcat(s, i))
				return []

			neworder = neworderInit()

			j = 0
			while not s[j] in (fst + snd):
				neworder = addToNeworder(neworder, s[j])
				j += 1
				if j >= len(s)-1:
					break

			if not neworder:
				snd = upperCase(snd, s)
				fst = lowerCase(fst, s)

			for u in s:
				if u in (snd):
					if (u[1] == ","):
						s = [x for x in s if x != u] + [u]
					break


			fstOne = True
			for v in s:
				if v in snd:
					# if False: # v == s0:
					# 	print("s0")
					# 	neworder = addToNeworder(neworder, s0[:8] + f0[8:10] + s0[10:])
					# else:
					if (fstOne) and (neworder) and (neworder[-1][1].lower() == "a") and (v[1]) and (v[1][0] in ["a", "e", "i", "o", "u"]):
						neworder[-1][1] += "n"
					elif (fstOne) and (neworder) and (neworder[-1][1].lower() == "an") and (v[1]) and not (v[1][0] in ["a", "e", "i", "o", "u"]):
						neworder[-1][1] = "a"

					neworder = addToNeworder(neworder, v)
					fstOne = False

			# if fst ends with an komma. "A, and B" -> "B, and A"
			lastOne = []
			for u in s[::-1]:
				if u in (fst):
					if (u[1] == "."):
						continue
					if (u[1] == ","):
						neworder = addToNeworder(neworder, u[:8] + [f0[0]]*2 + u[10:])

						fst = [x for x in fst if x != u]


						# neworder += [u]
						# if lastOne[1].lower() == "and":
						# 	print (lastOne in neworder)
						# 	fst += [lastOne]
						# 	u, lastOne = lastOne, u
						continue
					break
				# lastOne = u

			for u in s:
				if u in cc:
					neworder = addToNeworder(neworder, u)

			# for x in fst:
			# 	if x in snd:
			# 		input(x)

			# neworder = addToNeworder(neworder, ["|"]*10)

			# print(fst)

			for v in s:
				if v in fst:
					if False: # v == f0:
						print("f0")
						neworder = addToNeworder(neworder, f0[:8] + s0[8:10] + f0[10:])
					else:
						neworder = addToNeworder(neworder, v)
			

			while j < len(s):
				if not s[j] in (fst + cc + snd):
					neworder = addToNeworder(neworder, s[j])
				j += 1

			return (neworder, i+len(fst+snd)+1)
	return []


def concatenation(s):
	res = concatenationIntern(s, 0)
	# print (res)
	if res:
		return res[0]
	return []


def concatenationMultiple(s):
	i = 0
	res = concatenationIntern(s, i) # (s, 0) # (s, len(s))
	if not res:
		return []
	i = res[1]
	resAlt = res
	while mum([u[1] for u in res[0]], [u[1] for u in s])[0] >= 5:
		res = concatenationIntern(res[0], i)
		if not res:
			return resAlt[0]
		resAlt = res
		i = res[1]

	return resAlt[0]


def threeItemConcat(s, beginning):
	i = beginning-1
	inQuote = False
	for w in s[beginning:]:
		if w[4] == "``":
			inQuote = True
		if inQuote:
			if w[4] == "''":
				inQuote == False
			continue

		if (w[4] == "CC"):
			i += 1
			# print(w)
			thrd = offspring(w, s)
			snd = []

			if not thrd:
				continue

			f = father(w, s)

			if (not f) or (f[1].isdigit()) or (not (f[10] == "COORD")):
				continue

			snd = [f] + [o for o in offspring(f, s) if not o in thrd and o != w]

			# print(w)

			g = father(f, s)
			if not g:
				continue

			# print(" ".join([u[1] for u in s]))
			# print(w)
			# print("Y")
			fst = [u for u in ([g] + offspring(g, s)) if (not u in (snd + thrd + [w]))]
			# print("Z")

			neworder = neworderInit()

			j = 0
			while not s[j] in fst:
				neworder = addToNeworder(neworder, s[j])
				j += 1
				if j >= len(s)-1:
					continue

			if not neworder:
				snd = upperCase(snd, s)
				fst = lowerCase(fst, s)

			komma = []

			for u in s[::-1]:
				if u in snd:
					if (u[1] == ","):
						snd = [x for x in snd if x != u]
						komma = komma + [u]
					break

			for u in s[::-1]:
				if u in fst:
					if (u[1] == ","):
						fst = [x for x in fst if x != u]
						s = [x for x in s if x != u] + [u]
						snd = snd + [u]
					break

			fst += komma

			for v in s:
				if v in snd:
					neworder = addToNeworder(neworder, v)


			# lastOne = []
			# for u in s[::-1]:
			# 	if u in fst:
			# 		if (u[1] == "."):
			# 			continue
			# 		if (u[1] == ","):
			# 			neworder = addToNeworder(neworder, u)

			# 			s = [x for x in s if x != u]


			# 			# neworder += [u]
			# 			# if lastOne[1].lower() == "and":
			# 			# 	print (lastOne in neworder)
			# 			# 	fst += [lastOne]
			# 			# 	u, lastOne = lastOne, u
			# 			continue
			# 		break
			# 	# lastOne = u

			# # neworder = addToNeworder(neworder, w)

			for v in s:
				if v in fst:
					neworder = addToNeworder(neworder, v)

			while j < len(s):
				if not s[j] in (fst  + snd):
					neworder = addToNeworder(neworder, s[j])
				j += 1

			return (neworder, i + len(fst + snd + thrd) + 2)
	return []



def concatThree(s):
	res = threeItemConcat(s, 0)
	if res:
		return res[0]
	return []




# # in argentina there are ... . -> there are ... in argentina . 
def defronting(s):
	f = father(s[0], s)

	if s[0][4] in ["IN", "TO"]:
		pp = s[0]

	elif f and f[4] in ["IN"]:
		pp = father(s[0], s)

	else:
		return []

	if (not pp) or (pp[10] == "ROOT"):
		return []

	pp = ([pp] + offspring(pp, s))

	if len(pp) == 1:
		return []

	neworder = neworderInit()
	for w in s:
		if not w in pp:
			neworder = addToNeworder(neworder, w)

	if (not neworder) or (not neworder[0][1] == ","):
		# pp = upperCase(pp, s)
		return []

	pp = lowerCase(pp, s)

	neworder = upperCase(neworder[1:-1], s)

	for w in s:
		if w in pp:
			neworder = addToNeworder(neworder, w)

	neworder = addToNeworder(neworder, s[-1])

	return neworder



# # there are ... in argentina . -> in argentina there are ... . 

def fronting(s):
	if s[0][4] in ["IN", "TO", "CC"]:
		return []

	inQuote = False
	for w in s:
		if w[4] == "``":
			inQuote = True
		if inQuote:
			if w[4] == "''":
				inQuote == False
			continue

		if (w[4] in ["IN"]) and (not w[0] in ["1","2"]):
			if w[10] in ["OBJ"]:
				continue

			if w[1].lower() == "as":
				continue

			# if (w[4] == "TO") and (w[10] in ["VC", "OPRD"]):
			# 	continue

			# if ("A1" in w) or ("A2" in w) or ("A0" in w) or ("AM-ADV" in w) :
			# 	continue
			# for x in w:
			# 	if x in ["A1", "A0", "A2"]:
			# 		continue

			f = father(w, s)
			if not f: 
				continue

			r = root(s)

			if not purebredVerb(f, s):
				continue

			# if not (w[10] == "LOC"):
				# print(w)
				# continue

			op = offspring(w, s)
			# if (not op) or (not checkQuote(op, s)):
			# 	continue



			r = root(s)
			if not r:
				continue
			r = [0]

			if f[2] == "be":
				continue

			# if not father(f, s):
			# 	print(f)

			if father(f, s) and (father(f, s)[4] == "VBN"):
				continue

			am = [x for x in w if x.startswith("AM-")]

			if am and (am[0] in ["AM-PNC", "AM-DIR"]):
				continue

			if len([w] + op) <= 2: # mit AM-TMP und AM-MNR oft ganz ok 
				continue

			if (w[10] in ["ADV"]) and (not "A3" in w):
				continue

			# gut: 
			# A3
			# Yoda:
			# A2
			# schlecht:
			# A0, A1



			if (not am) and (w[10] in ["COORD", "EXTR", "AMOD", "TMP", "LOC", "ADV", "EXT", "NMOD", "PMOD", "OPRD", "PRP", "DIR", "LOC-PRD"]):
				continue


			pp = upperCase([w] + op, s)
			s = lowerCase(s, s)



			# if not w[10] in ["DIR"]:
			# 	continue


			# gut: LGS, MNR

			# eher schlecht: TMP, LOC, EXT, OPRD, DIR, LOC-PRD

			# schlecht: COORD, EXTR, AMOD, PMOD, NMOD, PRP


			# print(w)
			# print(am)
			# print(w[10])



			# auf am bezogen: 
			# gut: 
			# AM-LOC
			# AM-CAU
			# AM-TMP
			# AM-MNR



			# schlecht: 
			# AM-DIR # parse fehler. partikel werden als präp interpretiert. 
			# AM-PNC

			# 	# return []

			# print(" # ".join([u[1] for u in pp]))


			# if w[4] == "TO":
			# print(w)
			# print(f)

			neworder = neworderInit()
			for u in s:
				if u in pp:
					# if u != w:
					neworder = addToNeworder(neworder, u)
					# else:
					# 	neworder = addToNeworder(neworder, u[:8] + [root(s)]*2 + u[10:])


			# print(neworder)

			neworder = addToNeworder(neworder, ["FrK", ",", ",", ",", ",", ",", "_", "_", r, r, "P", "P", "_", "_", "_", "_"] + ["_"]*10)

			for u in s:
				if not u in pp:
					neworder = addToNeworder(neworder, u)

			# print(neworder[-2])
			# print(len(neworder))


			# if (len(neworder) >= 2) and (neworder[-2][1] == ","):
			# 	neworder = neworder[:-2] + [neworder[-1]]

			return neworder

	return []



# kopula (satz 25)
def kopula(s):
	inQuote = False
	for w in s:
		if w[4] == "``":
			inQuote = True
		if inQuote:
			if w[4] == "''":
				inQuote == False
			continue

		r = father(w, s)
		if not r:
			continue

		if (w[10] == "PRD") and (w[4].startswith("NN")) and (r[4] == "VBZ") and (r[2] == "be"):
			rootSons = sons(r, s)

			if len(rootSons) == 3:
				neworder = neworderInit()

				sbjP = [u for u in rootSons if u[10] == "SBJ"]
				if (not sbjP) or (sbjP[0][2].lower() in ["it", "there", "this", "which"]):
					return []

				sbjP += offspring(sbjP[0], s)

				prP = [u for u in rootSons if u[10] == "PRD"]
				prP += offspring(prP[0], s)


				j = 0
				while not s[j] in (sbjP + prP):
					neworder = addToNeworder(neworder, s[j])
					j += 1
					if j >= len(s)-1:
						print("Fehler bei Satz", i)
						continue

				if not neworder:
					s = lowerCase(s, s)
					prP = upperCase(prP, s)

				for v in s:
					if v in prP:
						neworder = addToNeworder(neworder, v)

				neworder = addToNeworder(neworder, r)

				for v in s:
					if v in sbjP:
						neworder = addToNeworder(neworder, v)

				while j < len(s):
					if not s[j] in (sbjP + prP + [r]):
						neworder = addToNeworder(neworder, s[j])
					j += 1

				return neworder

	return []

# Xly -> in a[n] X way

# manuelly solved problems:
# # only -> an on way
# # especially -> should be a special way
# is there more like that?

# open problems:
# # genetical or genetic? but: continual, not continu
# # mainly -> in a main way??!?
def adverb(s):
	for w in s:
		if (w[4] == "RB") and (w[1].endswith("ly")):
			if sons(w, s) != []:
				break

			f = father(w, s)
			if not f:
				continue

			if (f[4].startswith("V")) or (f[10] == "ROOT"):
				break

			# print(f) # root ist komisch, 


			brothers = [f] + offspring(f, s)

			neworder = neworderInit()

			i = 0
			while s[i] != w:
				neworder = addToNeworder(neworder, s[i])
				i += 1

			i += 1

			while (i < len(s)) and (s[i] in brothers):
				neworder = addToNeworder(neworder, s[i])
				i += 1

			if w[1][0] == "u":
				continue

			article = "a "
			if w[1][0] in "aeio":
				article = "an "

			adv = w[1][:-2]
			if adv == "on":
				return []
			if adv == "especial":
				adv = "special"
			# if adv.endswith("al"):
			# 	adv = adv[:-2]
			# 	if adv.endswith("i"):
			# 		adv = adv[:-1]


			w1 = [w[0]] + ["in "+ article + adv + " way"] + w[2:] # TODO

			neworder = addToNeworder(neworder, w1)

			while i < len(s):
				neworder = addToNeworder(neworder, s[i])
				i += 1


			return neworder

	return []

def passive(s):
	r = root(s)
	pradPos = -1
	if not r:
		# print("no root")
		return []

	if "''" in ([u[4] for u in s]):
		return []

	if (r[2] == "be") or (r[2] == "have"):
		# print("root be or have")
		return []

	if r[4] == "MD":
		for son in (sons(r, s)):
			if (son[2] == "be") or (son[2] == "have"):
				# print("MD b/h")
				return []

			if not son[12] == "_":
				pradPos = int(son[12])

	else:
		if r[12] == "_":
			return []
		pradPos = int(r[12])

	subj = []
	for w in s:
		if (w[10] == "SBJ") and (w[14+pradPos] == "A0"):
			if not (w.count("A0") == 1):
				return []
				# print("!!!!!")


			subj = w
			break

	obj = []
	for w in s:
		if (w[10] == "OBJ") and (w[14+pradPos] == "A1") :
			# if not "A1" in w:
			# 	# print("no A1")
			# 	return []
			obj = w
			break

	if (not obj) or (not subj) or (subj[1].lower() in ["it", "this"]) or (obj[1].lower() in ["it", "this"]):
		# if (not obj):
		# 	print("obj")
		# elif (not subj):
		# 	print("subj")
		# elif (subj[1].lower() in ["it", "this"]):
		# 	print("s it")
		# elif (obj[1].lower() in ["it", "this"]):
		# 	print("o it")
		return []

	plural = obj[4].endswith("S")

	# print(obj)

	subj = [subj] + offspring(subj, s)
	obj = [obj] + offspring(obj, s)

	for u in obj:
		if u[1].lower() in ["its", "their"]:
			# print("poss")
			return []

	# print(root(s))
	# print(sons(root(s), s))


	neworder = neworderInit()

	j = 0
	while not s[j] in (subj + obj):
		neworder = addToNeworder(neworder, s[j])
		j += 1


	vorfeldFrei = not neworder
		

	for v in s:
		if v in obj:
			neworder = addToNeworder(neworder, v)

	# neworder = addToNeworder(neworder, ["", "|"])

	# j += 1
	verbOrder = []
	while not s[j] in (obj):
		if not s[j] in subj:
			verbOrder += [s[j]]
		j += 1

		

	# if len(verbOrder) == 1:
	modalVerb = not r[4].startswith("V")

	if len([x for x in verbOrder if x[4].startswith("V")]) == 1:
		if vorfeldFrei:
			obj = upperCase(obj, s)
			s = lowerCase(s, s)

		puffer = neworderInit()
		# print(verbOrder)
		for v in verbOrder:
			# print(v)
			toBe = ""

			if v[4].startswith("V"):
				verb = getParticip(v)

				if not verb: # if pasts and present are equal (put, put, put)
					# print("pst eq pres")
					return []

				if isPast(v):
					if plural:
						toBe = "were"
					else:
						toBe = "was"
				else:
					if plural:
						toBe = "are"
					else:
						toBe = "is"

				# print(puffer)

				if modalVerb:
					toBe = "be"

				neworder = addToNeworder(neworder, ["PsvBe", toBe, "be", "be", v[4], v[5], "_", "_", v[8], v[9], v[10], v[11], "_", "_", "_", "_"]  + ["_"]*10)
				# print("v", v)


				neworder += puffer

				puffer = neworderInit()
					
				neworder = addToNeworder(neworder, ["PsvVN", verb, v[2], v[3], "VBN", "VBN", "_", "_", "PsvBe", "PsvBe", "_", "_", "_", "_"] + ["_"]*10)


			else:
				if v == r:
					neworder = addToNeworder(neworder, v)

				else:
					puffer = addToNeworder(puffer, v)

		neworder += puffer

	else: # zB nebensaetze
		# print("???")
		return []

		# neworder = addToNeworder(neworder, ["", "TO_BE"])
		# for v in verbOrder:
		# 	print(v)
		# 	neworder = addToNeworder(neworder, v)


	neworder = addToNeworder(neworder, ["PsvBy", "by", "by", "by", "IN", "IN", "_", "_", "PsvBe", "PsvBe", "_", "_", "_", "_"] + ["_"]*10)

	for v in s:
		if v in subj:
			sbjPro = ["we",	"i",	"he",	"she"]
			objPro = ["us",	"me",	"him",	"her"]
			done = False
			for i in range(len(sbjPro)):
				if v[1].lower() == sbjPro[i]:
					neworder = addToNeworder(neworder, [v[0], objPro[i], objPro[i]] + v[3:])

					done = True

			if not done:
				neworder = addToNeworder(neworder, v)



	for v in neworder:
		if (father(v, s)) and (not father(v, neworder)):
			v = v[:8] + ["PsvBe"]*2 + v[10:]

	# neworder = addToNeworder(neworder, ["", "|"])

	# j += 1
	while j < len(s):
		if not s[j] in (subj + obj):
			neworder = addToNeworder(neworder, s[j])
		j += 1

	# print(root(s))
	# print(root(neworder))

	# neworder = addToNeworder(neworder, s[-1])

	# j = 0
	# while j < len(s):
	# 	if not s[j] in (subj + obj):
	# 		neworder = addToNeworder(neworder, s[j])
	# 	j += 1

	return neworder




def genitive(s):
	inQuote = False
	for w in s:
		if w[4] == "``":
			inQuote = True
		if inQuote:
			if w[4] == "''":
				inQuote == False
			continue

		if (w[1] == "'s") and (w[10] == 'SUFFIX'): 
			f = father(w, s)
			if (not f) or (f[4].startswith("NNP")):
				continue

			genModif = [f] + offspring(f, s)


			# if f[4].startswith("NNP"):
			# 	continue

			# print(f)

			np0 = father(f, s) # findet nicht den Kopf
			if not np0:
				continue
			np = [np0]  + offspring(np0, s)

			h = father(np0, s)
			if h and (h[1] == "of"):
				continue

			neworder = neworderInit()

			j = 0
			while not s[j] in ([np0] + genModif + [w]):
				neworder = addToNeworder(neworder, s[j])
				j += 1

			if not np0[4].startswith("NNP"):
				neworder = addToNeworder(neworder, ["g1", "the", "the", "the", "DT", "DT", "_", "_", "g2", "g2", "NMOD", "NMOD"] + ["_"]*10) ##!!!!!!!!!

			if len(neworder) == 1:
				neworder = upperCase(neworder, s)
				s = lowerCase(s, s)

			while not s[j] in ([np0]):
				if not (s[j] in genModif):
					neworder = addToNeworder(neworder, s[j])
				j += 1

			neworder = addToNeworder(neworder, np0[:8] + ["g2", "g2"] + np0[10:11] )
			j += 1

			neworder = addToNeworder(neworder, ["g2", "of", "of", "of", "IN", "IN", "_", "_", np0[8], np0[8], "NMOD", "NMOD"] + np0[12:]) ##!!!!!!!!!


			for u in s:
				if (u in genModif) and (u != w):
					neworder = addToNeworder(neworder, u)

			# for u in s:
			# 	if (u in np) and not (u in genModif):
			# 		neworder = addToNeworder(neworder, u)

			while j < len(s):
				# if not s[j] in np:
				neworder = addToNeworder(neworder, s[j])
				j += 1

			return neworder

	return []

def gerund(s):
	inQuote = False
	for w in s:
		if w[4] == "``":
			inQuote = True
		if inQuote:
			if w[4] == "''":
				inQuote == False
			continue

		if (w[4] == "VBG"):
			if (w[1] == w[2]):
				continue

			f = (father(w, s))
			if not f or f[4] != "NN":
				continue
			
			np = [f] + offspring(f, s)

			gp = [w] + offspring(w, s)

			np1 = [u for u in np if not u in gp]

			# print(w)
			# print(f)
			# print(" + ".join([u[1] for u in offspring(w, s)]))
			# print(" ".join([u[1] for u in s]))
			# print("\n")

			if (len(gp) == 1): # and (len(np1) > 1):
				# print(len(np1))
				continue

			if True: # len(gp) > 1:
				neworder = neworderInit()

				j = 0
				while not s[j] in (np):
					neworder = addToNeworder(neworder, s[j])
					j += 1

				if not neworder:
					np1 = upperCase(np1, s)

				for u in s:
					if (u in np1):
						neworder = addToNeworder(neworder, u)

				if neworder[-1][1] == ".":
					neworder = neworder[:-1]

				if not neworder[-1][1] == ",":
					neworder = addToNeworder(neworder, ["ger1", ",", ",", ",", ",", ",", ",", ",", f[0], f[0]] + ["_"]*10) # !!!!!!!!!!!!!!!!!!!

				neworder = addToNeworder(neworder, ["gerT", "which", "which", "which", "WDT", "WDT", "_", "_", f[0], f[0]] + ["_"]*10) # !!!!!!!!!!!!!!!!!!!

				flection = w[2]
				if not f[4].endswith("S"):
					if w[2][-1] in ["s", "x", "z"]:
						flection += "e"
					flection += "s"

				if w[2] == "be":
					if f[4].endswith("S"):
						flection = "are"
					else:
						flection = "is"

				if w[2] == "do" and not f[4].endswith("S"):
					flection = "does"

				if w[2] == "go" and not f[4].endswith("S"):
					flection = "goes"

				neworder = addToNeworder(neworder, (["gerV", (flection)] + w[2:]))
				j += 1

				for u in s:
					if (not u == w) and (u in gp):
					# if (not s[j] == w) and ((not s[j] in np) or (s[j] in gp)):
						neworder = addToNeworder(neworder, u)

					# j += 1
				while (j < len(s)) and (s[j] in np):
					j += 1

				if (j < len(s)-1) and (s[j][1] != ","):
					neworder = addToNeworder(neworder, ["ger2", ",", ",", ",", ",", ",", ",", ",", f[0], f[0]] + ["_"]*10 ) # !!!!!!!!!!!!!!!!!!!


				while j < len(s):
					# if (not s[j] == w) and ((not s[j] in np) or (s[j] in gp)):
					# if not s[j] in np:
					neworder = addToNeworder(neworder, s[j])

					j += 1

				if not neworder[-1][1] == ".":
					neworder = addToNeworder(neworder, s[-1])

				return neworder

			# else:

	return []


def participPerfect(s):
	inQuote = False
	for w in s:
		if w[4] == "``":
			inQuote = True
		if inQuote:
			if w[4] == "''":
				inQuote == False
			continue

		if (w[4] == "VBN"):
			if w[1] == w[2]:
				continue

			f = (father(w, s))
			if not f or f[4] != "NN":
				continue

			# print(f)
			
			np = [f] + offspring(f, s)

			gp = [w] + offspring(w, s)

			np1 = [u for u in np if not u in gp]

			if (len(gp) == 1) and (len(np1) > 1):
				# print(len(np1))
				continue

			if w[2] == "be":
				continue

			# print(w)
			# print(father(w, s))
			if True: # len(gp) > 1:
				neworder = neworderInit()

				j = 0
				while not s[j] in (np):
					neworder = addToNeworder(neworder, s[j])
					j += 1

				if not neworder:
					np1 = upperCase(np1, s)

				for u in s:
					if (u in np1):
						neworder = addToNeworder(neworder, u)

				if neworder[-1][1] == ".":
					neworder = neworder[:-1]

				subj = []
				by = []

				for u in sons(w, s):
					if u[1] == "by":
						by = u
						subj = offspring(u, s)
						for son in sons(by, s):
							if son[4][0] in ["V", "M"]:
								subj = []
								by = []
								break
						# print("!!!!!!!!!!")
						# print(subj)

				if not neworder[-1][1] == ",":
					neworder = addToNeworder(neworder, ["ppp1", ",", ",", ",", ",", ",", ",", ",", f[0], f[0]] + ["_"]*10) # !!!!!!!!!!!!!!!!!!!

				neworder = addToNeworder(neworder, ["pppT", "which", "which", "which", "WDT", "WDT", "_", "_", f[0], f[0]] + ["_"]*10) # !!!!!!!!!!!!!!!!!!!

				if not subj:
					plural = f[4].endswith("S")

					toBe = "is"
					if plural:
						toBe = "are"

					neworder = addToNeworder(neworder, ["pppBE", toBe, "be", "be", "V?", "V?", "_", "_", f[0], f[0]] + ["_"]*10) # !!!!!!!!!!!!!!!!!!!

				sbjPro = ["we",	"I",	"he",	"she"]
				objPro = ["us",	"me",	"him",	"her"]


				for u in s:
					if u in subj:
						done = False
						for i in range(len(sbjPro)):
							if u[1].lower() == objPro[i]:
								neworder = addToNeworder(neworder, [u[0], sbjPro[i], sbjPro[i]] + u[3:])
								done = True

						if not done:
							neworder = addToNeworder(neworder, u)

				# flextion = ""
				# if not f[1].endswith("S"):
				# 	flextion = "s"
				done = False
				if subj:
					for i in range(len(particip)):
						if w[1] == particip[i]:
							addToNeworder(neworder, [w[0], past[i]] + w[2:])

				if not done:
					neworder = addToNeworder(neworder, w)

				j += 1

				for u in s:
					if (not u in [w, by] + subj) and (u in gp):
					# if (not s[j] == w) and ((not s[j] in np) or (s[j] in gp)):
						neworder = addToNeworder(neworder, u)

					# j += 1
				while (j < len(s)) and (s[j] in np):
					j += 1

				if (j < len(s)-1) and (s[j][1] != ","): # (not subj) and 
					neworder = addToNeworder(neworder, ["ppp2", ",", ",", ",", ",", ",", ",", ",", f[0], f[0]] + ["_"]*10 ) # !!!!!!!!!!!!!!!!!!!


				while j < len(s):
					# if (not s[j] == w) and ((not s[j] in np) or (s[j] in gp)):
					# if not s[j] in np:
					neworder = addToNeworder(neworder, s[j])

					j += 1

				if not neworder[-1][1] == ".":
					neworder = addToNeworder(neworder, s[-1])

				return neworder

			# else:

	return []


def also(s):
	inQuote = False
	for w in s:
		if w[4] == "``":
			inQuote = True
		if inQuote:
			if w[4] == "''":
				inQuote == False
			continue

		if (w[1] == "also"):
			f = father(w, s)
			if (not f) or (not f[10] == "ROOT") or (f[2] == "see") :
				continue

			# print (w)


			neworder = neworderInit()

			count = 0

			for u in s[:-1]:
				count += 1
				if u != w:
					neworder = addToNeworder(neworder, u)
				else:
					count = 0

			if count > 10:
				return []

			# print("count:", count)

			r = root(s)
			if r:
				r = r[0]
			else:
				r = 0


			neworder = addToNeworder(neworder, ["a1", ",", ",", ",", ",", ",", r, r, "P", "P", "_", "_", "_", "_"] + ["_"]*10)
			neworder = addToNeworder(neworder, ["a2", "as", "as", "as", "as", "as", "a1", "a1", "NULL", "NULL"] + ["_"]*10)
			neworder = addToNeworder(neworder, ["a3", "well", "well", "well", "well", "well", "a1", "a1", "NULL", "NULL"] + ["_"]*10)

			neworder = addToNeworder(neworder, s[-1])

			return neworder

	return []



def rel2conc(s):
	inQuote = False
	for w in s:
		if w[4] == "``":
			inQuote = True
		if inQuote:
			if w[4] == "''":
				inQuote == False
			continue

		if (w[4] == "WDT"):
			n = refNoun(w, s)
			if not n:
				continue
			
			np = [n] + offspring(n, s)

			if s[0] in np:

				neworder = neworderInit()

				j = 0
				while  (s[j] != w): #(s[j] in np) and
					neworder = addToNeworder(neworder, s[j])
					j += 1

				if neworder[-1][1] == ",":
					# continue
					neworder = neworder[:-1]
				else:
					continue

				j += 1

				while (j < len(s)) and (s[j] in np):
					neworder = addToNeworder(neworder, s[j])
					j += 1
					
				if neworder[-1][1] == ",":
					# continue
					neworder = neworder[:-1]
				else:
					continue

				neworder = addToNeworder(neworder, ["R2c", "and", "and", "and", "and", "and", "and"] + [root(s)[0]]*2 + ["_"]*10) # !!!!!!!!!!!!

				while j < len(s):
					neworder = addToNeworder(neworder, s[j])
					j += 1


				return neworder

def rel2sent(s):
	inQuote = False
	for w in s:
		if w[4] == "``":
			inQuote = True
		if inQuote:
			if w[4] == "''":
				inQuote == False
			continue

		# if w[1] == "they":
		# 	print (w)
		# continue

		if (w[4] == "WDT"):
			if not w[1] in ["that", "which"]:
				continue

			n = refNoun(w, s)
			if (not n) or (not n[4].startswith("N")) :
				continue

			# print(n)
			
			np = [n] + offspring(n, s)

			rc = []
			for u in rightSons(n, s):
				if u[4].startswith("V") or u[4].startswith("M"):
					rc = [u] + offspring(u, s)
					break


			if s[-2] in np:
				# print(w)
				# print(n)
				# print(" ".join([u[1] for u in s]))
				# print(" | ".join([u[1] for u in rightSons(n, s)]))
				# print()
				# print(offspring(w, s))

				neworder = neworderInit()

				for u in s:
					if not u in (rc):
						neworder = addToNeworder(neworder, u)
					else:
						break

				if not neworder:
					continue

				if neworder[-1][1] == ",":
					neworder = neworder[:-1]

				neworder = addToNeworder(neworder, ["r2sSem", ";", ";", ";", ";"] + s[-1][5:])

				pro = "it"
				dt = "this"
				if n[4].endswith("S"):
					pro = "they"
					dt = "these"
				elif n[4].startswith("V"):
					pro = "that"
					dt = "that"
					# print("!!!!!!!!!!")



				if not dt == "that":
					neworder = addToNeworder(neworder, ["r2sPro", dt, dt, dt, "DT", "DT", "_", "_", "r2sClone", "r2sClone"] + ["_"]*10)
					neworder = addToNeworder(neworder, ["r2sClone"] + n[1:])
				else:
					neworder = addToNeworder(neworder, ["r2sPro", pro, pro, pro, "PRP", "PRP", "_", "_", "r2sSem", "r2sSem"] + ["_"]*10)


				for u in s:
					if u in (rc) and (u != w):
						neworder = addToNeworder(neworder, u)

				# if neworder[-1][1] == ";":
				# 	neworder = neworder[:-1]

				neworder = addToNeworder(neworder, s[-1])

				return neworder



			continue

			if s[0] in np:

				neworder = neworderInit()

				j = 0
				while  (s[j] != w): #(s[j] in np) and
					neworder = addToNeworder(neworder, s[j])
					j += 1

				if neworder[-1][1] == ",":
					# continue
					neworder = neworder[:-1]
				else:
					continue

				j += 1

				while (j < len(s)) and (s[j] in np):
					neworder = addToNeworder(neworder, s[j])
					j += 1
					
				if neworder[-1][1] == ",":
					# continue
					neworder = neworder[:-1]
				else:
					continue

				neworder = addToNeworder(neworder, ["R2c", "and", "and", "and", "and", "and", "and"] + [root(s)[0]]*2 + ["_"]*10) # !!!!!!!!!!!!

				while j < len(s):
					neworder = addToNeworder(neworder, s[j])
					j += 1


				return neworder


def delete(s):
	inQuote = False
	for w in s:
		if w[4] == "``":
			inQuote = True
		if inQuote:
			if w[4] == "''":
				inQuote == False
			continue


		# am = [x for x in w if (x != "AM-MOD") and (x != "AM-LOC") and (x != "AM-DIR") and (x != "AM-ADV") and (x != "AM-TMP") and (x != "AM-NEG") and (x != "AM-MNR") and (x != "AM-PNC") and (x != "AM-DIS") and (x != "AM-CAU")]
		am = [x for x in w if x in ["AM-DIS"]] #, "AM-ADV", "AM-CAU"]]
		# am = [x for x in w if x.startswith("AM-CAU")]
		# gut: AM-ADV, DIS
		# eher gut: TMP CAU (nebensatz)
		# eher schlecht: MNR
		# schlecht: AM-LOC, MOD, DIR, NEG, PNC, EXT
		if len(am) > 0:
			# if w[4] in ["JJ", "TO", "IN", "VBG", "RB", "VBN", "RBR", "VBD", "CC", "VBZ", "DT", "RBS", "NNP", "WDT", "UH"]:
			# 	continue

			if not (w[4] in ["RB"]):
				continue

			# if not w[1] != "further":
			# 	continue

			# print(w)
			# print(am)

			# print(w[4])
			# gut: RB?, 
			# CC
			# UH
			# DT
			# NNP
			# WDT
			# schlecht: TO, IN, VBG, JJ, VBN, VBD, VBZ
			# RBR, RBS


			# if offspring(w, s):
			# 	continue
			am = [w] + offspring(w, s)

			neworder = neworderInit()

			for u in s:
				if not u in am:
					neworder = addToNeworder(neworder, u)
				else:
					neworder = addToNeworder(neworder, ["R-" + u[0], asterisk + u[1]] + u[2:])

			for i in range(len(neworder)):
				if neworder[i][0].startswith("R-"):
					continue
				elif neworder[i][1] == ",":
					neworder[i] = ["R-" + neworder[i][0], asterisk + neworder[i][1]] + neworder[i][2:]
				else:
					neworder[i][1] = neworder[i][1][0].upper() + neworder[i][1][1:]
					break

			return neworder


	return []



# for inputs which are not recognized
def dummy(s):
	return []

def nameToFunc(name):
	name = name.lower()
	if (name in ["kp", "kop", "kopu", kopula]):
		return kopula
	elif (name in ["conc", "cc", "c"]):
		return concatenation
	elif (name in ["conc+", "cc+", "c+"]):
		return concatenationMultiple
	elif (name in ["conc3", "cc3", "c3"]):
		return concatThree
	elif (name in ["df", "defr"]):
		return defronting
	elif (name in ["fr", "frnt"]):
		return fronting
	elif (name in ["adv", "av"]):
		return adverb
	elif (name in ["ps", "psv"]):
		return passive
	elif (name in ["ger", "gerund"]):
		return gerund
	elif (name in ["pp", "ppp", "participperfect"]):
		return participPerfect
	elif (name in ["gen", "genitive"]):
		return genitive
	elif (name in ["also", "a"]):
		return also
	elif (name in ["r2c", "r"]):
		return rel2conc
	elif (name in ["r2s", "s"]):
		return rel2sent
	elif (name in ["del", "d"]):
		return delete

	print("Warning:", name, "not recognized. It is ignored. ")
	helpRules()
	return dummy


def helpRules():
	print('''The following key-words can be used:
\tKP\tkopula\t\tX is Y -> Y is X
\tCONC\tconcatenation\tX and Y -> Y and X
\tCONC3\tconcatenation\tX , Y and Z -> Y , X and Z
\tCONC+\tconcatenation\tX more than 1 conc if possible
\tDF\tdefronting\tin X […] -> […] in X
\tFR\tfronting\t[…] in X -> in X […]
\tADV\tadverbs\t\tXly -> in a X way
\tPSV\tpassive\t\tX does Y -> Y is done by X
\tGEN\tgentitive\tX 's Y -> Y of X
\tALSO\talso\t\t[…] also […] -> […] as well
\tR2C\trel2conc\tX , which […] , […] -> X […] and […]
\tR2S\trel2sent\t […] X , which […] -> […] X ; this X […]
\tGER\tgerund\t\tYing X -> Y which Xs | X Ying Z -> X which Ys Z
\tPP\tparticip perfect\t\tYed X -> X which is Yed | X Yed by Z -> X which Z Yed 
\tDEL\tdelete\t\tvery big -> big
''')

filename = "raw-sentences.txt.anno.txt"

rulesString = ["df", "fr", "psv", "conc+", "KP", "also", "gen", "r2s", "r2c", "ger", "pp", "del"] # "adv"
# rulesString = ["conc+", "KP", "df", "fr", "psv", "also", "gen", "r2s", "r2c", "ger", "pp"] # "adv"


args = sys.argv[1:]

showResults = False
if "-v" in args:
	showResults = True
	for i in range(len(args)):
		if args[i] == "-v":
			args = args[:i] + args[i+1:]
			break

showNegatives = False
if "-n" in args:
	showNegatives = True
	for i in range(len(args)):
		if args[i] == "-n":
			args = args[:i] + args[i+1:]
			break

showBad = False
if "-b" in args:
	showBad = True
	showNegatives = True
	for i in range(len(args)):
		if args[i] == "-b":
			args = args[:i] + args[i+1:]
			break

showInt = False
if "-i" in args:
	showInt = True
	for i in range(len(args)):
		if args[i] == "-i":
			args = args[:i] + args[i+1:]
			break

showMin = False
if "-m" in args:
	showMin = True
	for i in range(len(args)):
		if args[i] == "-m":
			args = args[:i] + args[i+1:]
			break

onlyLine = -1
if "-o" in args:
	for i in range(len(args)):
		if args[i] == "-o":
			onlyLine = int(args[i+1])
			break

if "-r" in args:
	for i in range(len(args)):
		if args[i] == "-r":
			break
	rulesString = args[i+1:]

if "-a" in args:
	pass
	asterisk = "*"
	for i in range(len(args)):
		if args[i] == "-a":
			args = args[:i] + args[i+1:]
			break

# print(rulesString)

if "-f" in args:
	for i in range(len(args)):
		if args[i] == "-f":
			filename = args[i+1]
			args = args[:i] + args[i+2:]
			break

elif not args[0].startswith("-"):
	filename = args[0]

if not filename.endswith("parsed.conll"):
	print(filename)
	input("invalide filename.")

# if args:
# 	if args[0] == "-r":
# 		rulesString = args[1:]
# 	else:
# 		print("Couldn't interpret arguments:", " ".join(args))

if not rulesString:
	print("no rules are given. ")
	helpRules()

f1 = open(filename)
sents = f1.read()
f1.close()

if  filename.endswith("-tok.txt.parsed.conll"):
	filename = filename[:-len("-tok.txt.parsed.conll")]
else:
	filename = filename[:-len(".parsed.conll")]

sents = sents.split("\n\n")[:-1]

for s in range(len(sents)):
	sents[s] = sents[s].split("\n")
	for w in range(len(sents[s])):
		sents[s][w] = sents[s][w].split("\t")


def mum(a, b):
	# arr = [[0]*(len(b)+1)]*(len(a)+1)

	arr = [[0]*(len(b)+1)]

	maxi = 0
	offset = 0
	substr = ""

	for ix in range(len(a)):
		arr += [[0]]
		for iy in range(len(b)):
			if a[ix].lower() == b[iy].lower():
				# arr[ix+1][iy+1] = arr[ix][iy] +1
				arr[-1] += [arr[ix][iy] +1]
				if maxi < arr[ix+1][iy+1]:
					maxi = arr[ix+1][iy+1]
					offset = iy - maxi + 1
					substr = a[ix-maxi+1:ix+1]

			else:
				arr[-1] += [0]

			# print(arr)

	return (maxi, " ".join(substr), offset)

# print(mum("c a b d c".split(" "), "a b c c".split(" ")))



def digit(x):
	if x.isdigit():
		return str(int(x)-1)
	elif x.startswith("R-"):
		return "R-" + digit(x[2:])
	else:
		return "X"



def roleFixer(order):
	count = 0
	for x in order:
		if x[12] == "Y":
			x[12] = str(count)

	return order



rules = [nameToFunc(s) for s in rulesString]


output = ""
outPos = ""
outMeta = ""

thisRules = ""

i = 0
count = 0
for sent in sents:
	if showMin and (i > 200):
		continue

	original = " ".join([u[1] for u in sent])
	sent = roleFixer(sent)

	if showInt:
		print(i)

	neworder = sent
	thisRules = ""
	changeTuple = (len(sent), " ".join([x[1] for x in sent]))
	changed = False

		# if concatenation(neworder) and not (concatenationMultiple(neworder)):
		# 	print(i)


	if onlyLine in [-1, i]:
		for rule in rules:
			thisNeworder = rule(neworder)
			# if neworder:
			# 	break

			if thisNeworder:
				# print(rule)
				changed = True
				thisRules += str(rule).split(" ")[1] + " "
				# print(thisRules)
				neworder = thisNeworder


				# print(neworder)
				changeTuple = (mum(([u[1] for u in sent]), [u[1] for u in neworder]))
				
				if changeTuple[0] < 5:
					pos = " ".join([u[4] for u in neworder])
					# neworder = " ".join([u[1] for u in neworder])
					# output += neworder
					# outPos += pos
					# outMeta += thisRules + "\t" + str(changeTuple[0]) + "\t" + changeTuple[1]

					break



	output += " ".join([u[1] for u in neworder])
	outPos += " ".join([u[4] for u in neworder])

	if changed:	
		outMeta += thisRules + "\t" + str(changeTuple[0]) + "\t" + str(changeTuple[2]) + "\t" + changeTuple[1]
		count += 1

		if showResults or (showBad and changeTuple[0] >= 8):
			print(i, thisRules)
			print(original, " ".join([u[1] for u in neworder if (asterisk or not u[0].startswith("R-"))]), changeTuple, "\n", sep = "\n")
	else:
		outMeta += "-\t" + str(len(neworder)) + "\t0\t" + " ".join([u[1] for u in neworder])

		if showNegatives:
			print(i, "--")
			print(original)
			print("\n\n\n")

	outMeta += "\t" + (",".join([(digit(u[0])) for u in neworder]))

	output  += " \n"
	outPos  += " \n"
	outMeta += "\n"
	i += 1

print(count, "of", len(sents), "are changed")

while("\\/" in output):
	output = output.replace("\\/", "/")

f1=open(filename + "-restructured-tok.txt", "w")
f1.write(output)
f1.close()

f1=open(filename + "-restructured-pos.txt", "w")
f1.write(outPos)
f1.close()

f1=open(filename + "-restructured-meta.tsv", "w")
f1.write(outMeta)
f1.close()