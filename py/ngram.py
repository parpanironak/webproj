from __future__ import print_function
from nltk import ngrams
import glob
import os

def deletedir(path):
    filelist = [ f for f in os.listdir(path) ]
    for f in filelist:
        os.remove(path+f)

def getngramlist(n, line):
    line = line.strip()
    ngramlist = ngrams(line.split(), n)
    resultlist = [];
    for ngram in ngramlist:
        phrase = " "
        resultlist.append(phrase.join(ngram))

    return resultlist

def writetofile(ngrams, opfile):
    try:
        with open(opfile, 'a+') as the_file:
            for ng in ngrams:
                print(ng, file=the_file)
    except Exception:
        print("cannot write %s" % opfile)
        
def writeToFile(sorted_words, opfile):
    try:
        the_file = open(opfile, 'a+')
        for t in sorted_words:
            print(t[0] + '\t' + str(t[1]), file=the_file)
        the_file.close()
    except Exception:
        print("cannot write %s" % opfile)
        print(traceback.format_exc())

import sys
import collections
import operator
import traceback

inf = sys.argv[1]
outf = sys.argv[2]
maxN = int(sys.argv[3])

dirpath = inf
outdirpath = outf

if not os.path.exists(outdirpath):
    os.makedirs(outdirpath)
deletedir(outdirpath)

wikilines = glob.glob(dirpath+u"*")
print(len(wikilines))

for infile in wikilines:
    allNames = infile.split("/")
    outfilepath = outdirpath + allNames[len(allNames)-1]
    #print(outfilepath)
    in_file = None
    try:
        allWords = collections.defaultdict(int)
        in_file = open(infile, 'r')
        flag = True
        for line in in_file:
            if flag:
                flag = False
                continue
            for n in range(1,maxN+1):
                ng = getngramlist(n, line)
                for phrase in ng:
                    allWords[phrase] = allWords[phrase] + 1
                #writetofile(ng, outfilepath)
        in_file.close();
        sorted_words = sorted(allWords.items(), key=operator.itemgetter(1), reverse=True)
        writeToFile(sorted_words, outfilepath)

    except Exception:
        print("cannot open %s" % infile)

print("complete")