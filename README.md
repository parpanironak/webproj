webproj
=======

QDocument.java - numviews, content, id, weight(may-be), freqincorpus, 

QIndexerCompressed.java - store trie, List of QDocuments, Index of word to postinglist, (have a word specific class to stor in trie)

QRanker.java

Stop word removal is similar in java and in python. In Java, numbers are not removed though. It will not affect it as we can only do stopword removal and stemming on queries (and not on the corpus at all).


-----SAMPLING-----------
1. Select top x phrases from every document.
2. Distribute average numviews (~90) among only these phrases.
3. Do not sample the top x phrases. These will be part of the final set of phrases.
4. If the probability of a phrase lesser than some value (need to fix the value), we ignore the phrase. We sample y% of the remaining phrases after this.
5. If document has numviews > 0, we distribute them among all phrases (including the top ones) that we sample.
