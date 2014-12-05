webproj
=======

QDocument.java - numviews, content, id, weight(may-be), freqincorpus, 

QIndexerCompressed.java - store trie, List of QDocuments, Index of word to postinglist, (have a word specific class to stor in trie)

QRanker.java

Stop word removal is similar in java and in python. In Java, numbers are not removed though. It will not affect it as we can only do stopword removal and stemming on queries (and not on the corpus at all).
