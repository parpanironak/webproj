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

n=3;
dirpath = u"D:\\wikitextonly\\"
outdirpath = u"D:\\wiki3gram\\"

deletedir(outdirpath)

wikilines = glob.glob(dirpath+u"*")
dirpath
for infile in wikilines:
    outfilepath = outdirpath + infile.split("\\")[2];
    in_file = None

    try:
        in_file = open(infile, 'r');
        for line in in_file:
            ng = getngramlist(n, line)
            writetofile(ng, outfilepath)

    except Exception:
        print("cannot open %s" % infile)

print("complete")