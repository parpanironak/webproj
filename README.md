webproj
=======

Compiling code:
To compile code, run
		javac -cp lib/patricia-trie-0.6.jar:lib/jsoup-1.8.1.jar:lib/json-simple-1.1.1.jar:src src/edu/nyu/cs/cs2580/*.java

Steps to run code:

1. Run server in mining mode to calculate numviews and pagerank.
   To run this step, run:
		java -cp src/ edu.nyu.cs.cs2580.SearchEngine --mode=mining --options=conf/engine.conf
2. Parse the wiki corpus and extracts meaningful text (removes html structures) from it.
   To run this step, run:
		python py/wikiparse.py
   Note: Data should be in data/wiki/, parsed data will be in data/wikitextlinux/
3. Stopword removal.
   To run this step, run:
		python py/stopwordremoval.py
   Note: Takes data from data/wikitextlinux/ and clean data is placed in data/wikitext/
4. Ngram generation - parses the clean data from step 2 and creates n grams of specific lengths and counts them in each file.
   To run this step, run:
		python py/ngram.py data/wikitext/ data/ngramswikitext/ 5
   Note: 5 is the maximum length of ngrams generated (and this is the value we use).
5. Log simulation - simulates query logs by distributing numviews of a doc among its ngrams.
   To run this step, run:
		java -cp src/ edu.nyu.cs.cs2580.LogSimulation
   Note: This puts simulated logs in data/simulatedlogs.txt
6. Generate Co-occurrences
   To run this step, run:
		cat data/simulatedlogs.txt | python py/smap.py | sort -t $'\t' -k1 -V | python py/sred.py | sort -t $'\t' -k3 -r -n > data/cooccurrences.txt
7. To run the server in index mode, run
		java -cp lib/patricia-trie-0.6.jar:src/ edu.nyu.cs.cs2580.SearchEngine --mode=index --options=conf/engine.conf
8. To run the server in serve mode, run
		java -cp lib/patricia-trie-0.6.jar:lib/json-simple-1.1.1.jar:src/ edu.nyu.cs.cs2580.SearchEngine --mode=serve --options=conf/engine.conf --port=25809
