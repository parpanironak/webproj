# -*- coding: utf-8 -*-
"""
Created on Sun Dec 14 16:45:42 2014

@author: kumar
"""

import sys

filename = sys.argv[1]
relsFileName = sys.argv[2]
K = 10

if len(sys.argv) == 3:
    K = int(sys.argv[3])
    
f = open(filename, 'r')

d={}
numRel = 0
for line in f:
    rels = line.split('\t')
    rel = int(rels[1])
    numRel = numRel + rel
    d[rels[0]] = rel
    
rr = 0
f2 = open(relsFileName, 'r')
k = 0
for line in f2:
    rr = rr + d[line]
    k = k + 1
    if k == K:
        break

print("Precision at "+str(K) + ": " + (rr/K))
print("Recall at "+str(K) + ": "+(rr/numRel))