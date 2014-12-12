package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

public class QueryRanker {
  
  private QIndexerInvertedCompressed qindexer;
  private Options _options;
  private CgiArguments _arguments;
  private int numSuggestionsFromTrie = 5;
  
  protected QueryRanker(Options options, CgiArguments arguments, QIndexerInvertedCompressed qindexer) {
    _options = options;
    _arguments = arguments;
    this.qindexer = qindexer;
  }
  
  public Vector<ScoredQueryDocument> runQuery(Query query, int numResults) {
    //We use the trie to try to complete the last word being typed
    Vector<ScoredQueryDocument> results = new Vector<ScoredQueryDocument>();
    /*if(qindexer.trie.containsKey(query._tokens.get(query._tokens.size()-1))) {
      //This is a full word
      //We need not get suggestions from the trie
      QDocument doc = null;
      int docid = -1;
      while ((doc = qindexer.nextDoc(query, docid)) != null) {
        ScoredQueryDocument scoredDoc = new ScoredQueryDocument(doc, doc.getFrequency());
        results.add(scoredDoc);
        docid = doc.getDocId();
      }
    }
    else {*/
      //This is a partial word
      //We should try to complete this word and use
      //top few completions
    String lastWord = query._tokens.get(query._tokens.size()-1);
    Map<String,Double> possibleCompletions = qindexer.trie.prefixMap(lastWord);
    Comparator<Map.Entry<String,Double>> cmp = new Comparator<Map.Entry<String,Double>>() {

      @Override
      public int compare(Entry<String, Double> entry1,
          Entry<String, Double> entry2) {
        double d = entry1.getValue() - entry2.getValue();
        if(d < 0) {
          return 1;
        }
        else if (d > 0) {
          return -1;
        }
        return 0;
      }

    };
    List<Map.Entry<String,Double>> mapEntries = new ArrayList<Map.Entry<String,Double>>();
    for(Map.Entry<String, Double> entry:possibleCompletions.entrySet()) {
      mapEntries.add(entry);
    }
    Collections.sort(mapEntries,cmp);
    int numSugg = mapEntries.size();
    if(numSugg > numSuggestionsFromTrie) {
      numSugg = numSuggestionsFromTrie;
    }
    int i = 0;
    Set<Integer> allDocIds = new HashSet<Integer>();
    for(Map.Entry<String, Double> entry:mapEntries) {
      String word = entry.getKey();
      QDocument doc = null;
      int docid = -1;
      Query q = new QueryPhrase(query,word);
      while((doc = qindexer.nextDoc(q, docid)) != null) {
        ScoredQueryDocument scoredDoc = new ScoredQueryDocument(doc, doc.getFrequency());
        docid = doc.getDocId();
        if(!allDocIds.contains(docid)) {
          results.add(scoredDoc);
          allDocIds.add(docid);
        }
      }
    }
    //}
  Collections.sort(results, Collections.reverseOrder());
  for(int j=results.size()-1;j>=numResults;j--) {
    results.remove(j);
  }
  return results;
  }
}
