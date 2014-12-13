from __future__ import print_function
from bs4 import BeautifulSoup as bs
from stripogram import html2text
import glob
import os
import re

dirpath = u"data/wiki/"
opdirpath = u"data/wikitextlinux/"
errorfile = u"data/errors.txt"
wiki = glob.glob(dirpath+u"*")
print(len(wiki))
error = open(errorfile, 'w+')

def deletedir(path):
    filelist = [ f for f in os.listdir(path) ]
    for f in filelist:
        os.remove(path+f)

deletedir(opdirpath);

for file in wiki:

    filepath = file;
    allNames = file.split("/")
    opfilepath = opdirpath + allNames[len(allNames)-1]
    soup = None
    try:
        soup = bs(open(filepath))
        soup = soup.body
    except Exception :
        print("cannot open:" + filepath, file=error);
        continue;

    if not soup:
        print("contains no body:" + filepath , file = error)
        continue;

    heading = soup.find('h1',class_='firstHeading')
    if heading:
        heading = html2text(str(heading)).strip()

    paragraphlist = soup.findAll('p')



    if paragraphlist or heading:
        try:
            opfile = open(opfilepath, 'w+')
        except Exception :
            print("cannot create o/p filename:"+opfilepath, file=error)
            continue;
    else:
        continue;

    if heading:
        print(heading, file=opfile);
        print(heading, file=opfile);

    if paragraphlist:
        for p in paragraphlist:
            p = html2text(str(p)).strip()
            p = re.sub(r"\[[0-9]*\]", "", p)
            p = re.sub(r"(\d+),(\d+)", r"\1\2", p)
            p = re.sub(r'\[|\]', "", p)
            p = re.sub(r"_|\-|\+|\*|\n", " ", p)
            p = re.split('[.,";:?/()!\n]+',p)
            for line in p:
                line = line.strip()
                if len(line) > 0:
                    print(line,file=opfile);

    opfile.close()

error.close()