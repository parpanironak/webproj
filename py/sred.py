#!/usr/bin/env python

from operator import itemgetter
import sys

current_word = None
current_count = 0
word = None
H = {};

for line in sys.stdin:
    
    line = line.strip()


    data = line.split('\t')
    word = data[0];

    n = len(data);
    if current_word == word:
        if n <= 1:
                continue
        for i in range(1,n,2):
                cword = data[i];
                count = None;
                try:
                        count = float(data[i+1]);
                except ValueError:
                        continue
                except IndexError:
                        continue

                if H.has_key(cword):
                    H[cword] = H[cword] + count;
                else:
                    H[cword] = count;
    else:
        if current_word:
             for i in H:
                H[i] = H[i]/current_count;
                print current_word + "\t" + i + "\t" + format(H[i],'.4f') 
        H = {};
        current_count = 0.0;
        current_word = word


        if n <= 1:
                continue
        for i in range(1,n,2):
                cword = data[i];
                count = None;
                try:
                        count = float(data[i+1]);
                except ValueError:
                        continue
                except IndexError:
                        continue

                if H.has_key(cword):
                    H[cword] = H[cword] + count;
                else:
                    H[cword] = count;

    current_count = current_count + (n-1)/2.0;

if current_word == word:
    for i in H:
                H[i] = H[i]/current_count;
                print current_word + "\t" + i + "\t" + format(H[i],'.4f')
