import re, os, sys
import collections, json
import xml.etree.ElementTree as ET

entity_map = collections.defaultdict(lambda: 'ENTITY%d' % len(entity_map))

PATH=sys.argv[1]
if not PATH.endswith('/'):
    PATH += '/'

OUTPATH=sys.argv[2]
if not OUTPATH.endswith('/'):
    OUTPATH += '/'

if len(sys.argv) > 3:
    print("Loading entity map", sys.argv[3])
    for line in open(sys.argv[3]):
        k,v = line.strip().split('\t')
        entity_map[v] = k

search_recursively = False # Switch on for masking documents in nested directory structure and save them back inside it, otherwise uses flat directories
found_files = []
if search_recursively:
    for path, dirs, files in os.walk(PATH):
        for file in files:
            found_files.append(path+'/'+file)
else:
    for file in os.listdir(PATH):
        found_files.append(file)

print("files in dir: ", len(found_files))
file_counter=0
for file in found_files:
    if file.endswith('.xml') and not file.endswith('_masked.xml'):
        no_valid_ent_found=False
        print("Reading", file)
        out = ""
        if search_recursively:
            data_ = open(file).read()
        else:
            data_ = open(PATH+file).read()

        data_ = re.sub("</?Emphasis( \w+=\"\w+\")?>", "", data_)

        #for pat_beg, pat_end in [('(<(Article|Book|Chapter)Title)','(</(Article|Book|Chapter)Title)'),('<Abstract','</Abstract'),('<KeywordGroup','</KeywordGroup'),('<Body','</Body')]:
        data = data_
        pat_beg = '(<(Article|Book|Chapter)Title)'
        pat_end = '</Body>'
        para_beg = re.search(pat_beg, data)
        para_end = re.search(pat_end, data)
        if not para_beg or not para_end or para_beg.start() > para_end.start():
            print("Not considering file: ", file)
            continue
        out = data[:para_beg.start()]
        tail = data[para_end.end():]
        data = data[para_beg.start():para_end.end()]
        while data:
            entity_match_beg = re.search("([\w\d\(\[\)\]–\-·/,]+<Su(per|b)script>)", data)
            if not entity_match_beg:
                out += data
                data = ""
                break
            entity_beg = entity_match_beg.start(0)
            #out += data[:entity_beg]
            #entity_match_end = re.search("([\w\d\(\[\)\]–\-·/]+<Su(per|b)script>)", data[entity_beg:])
            brackets = {'[': 0, '(': 0}
            tags = {'<sup>': 0, '<sub>': 0}
            for j in range(entity_beg, entity_beg+700):
                if data[j] in '[(':
                    brackets[data[j]] += 1
                elif data[j:].startswith('<Superscript>'):
                    tags['<sup>'] += 1
                elif data[j:].startswith('<Subscript>'):
                    tags['<sub>'] += 1
                elif data[j:].startswith('</Superscript>'):
                    tags['<sup>'] -= 1
                elif data[j:].startswith('</Subscript>'):
                    tags['<sub>'] -= 1
                else:
                    if max(brackets.values()) > 0:
                        if data[j] == ']':
                            brackets['['] -= 1
                            continue
                        elif data[j] == ')':
                            brackets['('] -= 1
                            continue

                    done = False
                    if data[j].isspace() and max(tags.values()) == 0:
                        done = True
                    elif data[j] in ')]' and entity_beg < j:
                        done = True
                    elif data[j] == '<':# and not re.match("<Su(per|b)script>", data[j:])):
                        done = True

                    if max(tags.values()) > 0:
                        done = False

                    if done:
                        entity = re.sub("\s+", " ", data[entity_beg:j])
                        if '<Superscript>' not in entity and '<Subscript>' not in entity:
                            out += data[:j]
                            data = data[j:]
                            break
                        entity = entity.replace('<Superscript>', '<sup>').replace('<Subscript>', '<sub>').replace('</Superscript>', '</sup>').replace('</Subscript>', '</sub>')
                        if entity[-1] in ".,;:":
                            j -= 1
                            entity = entity[:-1]
                        if entity[0] == '(' and entity[-1] == ')':
                            entity_beg += 1
                            j -= 1
                            entity = entity[1:-1]
                        #if entity not in entity_map:
                        #    print("   ",entity_map[entity], entity)#,"\t",data[j:j+20])
                        out += data[:entity_beg]
                        out += entity_map[entity]
                        data = data[j:]
                        break
            else:
                print("No valid entity found in:",data[entity_beg:entity_beg+700])
                #raise
                no_valid_ent_found=True
                break
                #data = data[entity_beg+100:]
        if no_valid_ent_found:
            print("No valid ent found for file: ", file, " . Skipping...")
            continue

        out += tail
        #root = ET.fromstring(out) # Check XML correctness
        out = out.replace('<Superscript>', '').replace('<Subscript>', '').replace('</Superscript>', '').replace('</Subscript>', '')
        if search_recursively:
            outfile = file.replace('.xml','_masked.xml')
        else:
            outfile = OUTPATH+file.replace('.xml','_masked.xml')
        #print("-->", outfile)
        open(outfile, 'w').write(out)
        file_counter+=1

print("Processed files: ", file_counter)

map_file = OUTPATH+"entity_map.tsv"
print("Saving map to", map_file)
f = open(map_file,"w")
for k,v in entity_map.items():
    f.write("%s\t%s\n" % (v,k))

f.close()


"""
        for line in open(PATH+file):
            out.append([])
            line = line.replace("<Emphasis Type=\"Italic\">","")
            line = line.replace("<Emphasis Type=\"Bold\">","")
            line = line.replace("</Emphasis>","")
            for token in line.split():
                if "Emphasis>" in token:
                    out[-1].append(token)
                elif "Subscript>" not in token and "Superscript>" not in token:
                    out[-1].append(token)
                else:
                    head = ""
                    tail = ""
                    string = token
                    try:
                        if string[-1] == '>' and not string.endswith('script>'):
                            cut = string[::-1].index('<')+1
                            tail = string[-cut:] + tail
                            string = string[:-cut]
                        if string[0] == '<' and not (string.startswith('<Superscript') or string.startswith('<Subscript')):
                            cut = string.index('>')+1
                            head += string[:cut]
                            string = string[cut:]
                        if '<' in string and string.index('>') < string.index('<'):
                            cut = string.index('>')
                            head += string[:cut+1]
                            string = string[cut+1:]
                        if '<' in string and '>' not in string[string.index('<'):]:
                            cut = string[::-1].index('<')+1
                            tail = string[-cut:] + tail
                            string = string[:-cut]
                        if string[-1] in ".,:":
                            tail = string[-1] + tail
                            string = string[:-1]
                        if string[0] in "([" and string[-1] in ")]":
                            head = string[0]
                            tail = string[-1] + tail
                            string = string[1:-1]
                    except (IndexError, ValueError):
                        out[-1].append(token)
                        #print("ERR",token)
                        continue


                    if head:
                        out[-1].append(head)
                    if ('<Superscript>' in string) + ('</Superscript>' in string) == 1 or ('<Subscript>' in string) + ('</Subscript>' in string) == 1:
                        out[-1].append(string)
                    elif len(string) <= 2:
                        out[-1].append(string)
                    else:
                        out[-1].append(entity_map[string.replace('Superscript>','sup>').replace('Subscript>','sub>')])
                    if tail:
                        out[-1].append(tail)

                    #print(head, string.replace('<Superscript>','^{').replace('</Superscript>','}').replace('<Subscript>','_{').replace('</Subscript>','}'), tail,"\t", token, entity_map[string])


"""

"""
                #for match in re.findall("((\w|\d|/|\-|–|\+|@|\(|\)|\[|\])*</?Su(b|per)script>(\w|\d|/|\-|–|\+|@|\(|\)|\[|\])*)+", token):
                for match in re.findall("([\w\W]*</?Su(b|per)script>[\w\W]*)+", token):
                    head = ""
                    tail = ""
                    string = match[0]
                    if string[-1] in ".,":
                        tail = string[-1]
                        string = string[:-1]
                    if string[0] in "([" and string[-1] in ")]":
                        head = string[0]
                        tail = string[-1] + tail
                        string = string[1:-1]
                    print(head, string, tail, match)
                    s = match.start(1)
                    e = match.end(1)
                    if token[:s] or token[e:]:
                        print(token[:s])
                        print(token[s:e])
                        print(token[e:])
                        print()"""
