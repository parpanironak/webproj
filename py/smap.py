#!/usr/bin/env python

import sys


for line in sys.stdin:

    	
    tup = line.strip().split('\t')
    if len(tup) != 3:
	continue;
	
    words2 = tup[0].strip().split()
    words = [];
    for word in words2:
        word = word.strip();
        if len(word) > 0:
                words.append(word.lower());

    words = list(set(words));
    for word in words:
                a = word;
                for cword in words:
                        if(word != cword):
                                a = a +"\t"+ cword +"\t"+ str(1);
                print a;






