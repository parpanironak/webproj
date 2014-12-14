# -*- coding: utf-8 -*-
"""
Created on Sun Dec 14 16:45:42 2014

@author: kumar
"""

import sys

filename = sys.argv[1]
relsFileName = sys.argv[2]
K = 10

if len(sys.argv) == 4:
    K = int(sys.argv[3])
    
f = open(filename, 'r')

d={}
numRel = 0
for line in f:
    rels = line.split(' ')
    word = ''
    for i in range(len(rels)):
        word = word + ' ' + rels[i]
        if i == len(rels) - 2:
            break
    rel = int(rels[len(rels) - 1])
    word = word.strip()
    #print(word + '\t' +str(rel))
    numRel = numRel + rel
    d[word] = rel
    
rr = 0
f2 = open(relsFileName, 'r')
k = 0
for line in f2:
    line = line.strip()
    rr = rr + d[line]
    k = k + 1
    if k == K:
        break

print("Precision at "+str(K) + ": " + str(rr * 1.0/K))
print("Recall at "+str(K) + ": "+str(rr * 1.0/numRel))