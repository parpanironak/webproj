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
  private int numSuggestionsFromTrie = 20;
  private double lambda1 = 0.2;
  private double lambda2 = 9000;
  private double lambda3 = 999;
  private double lambda4 = 0.8;
  
  protected QueryRanker(Options options, CgiArguments arguments, QIndexerInvertedCompressed qindexer) {
    _options = options;
    _arguments = arguments;
    this.qindexer = qindexer;
  }
  
  public Vector<ScoredQueryDocument> runQuery(final Query query, int numResults) {
    //We use the trie to try to complete the last word being typed
    Vector<ScoredQueryDocument> results = new Vector<ScoredQueryDocument>();
    String lastWord = query._tokens.get(query._tokens.size()-1);
    Map<String,Double> possibleCompletions = qindexer.trie.prefixMap(lastWord);
    Comparator<Map.Entry<String,Double>> cmp = new Comparator<Map.Entry<String,Double>>() {
      @Override
      public int compare(Entry<String, Double> entry1,
          Entry<String, Double> entry2) {
        double coOcc1 = 1.0;
        double coOcc2 = 1.0;
        for(int k=0;k<query._tokens.size()-1;k++) {
          String tok = query._tokens.get(k);
          coOcc1 *= qindexer.getCoOccurrence(tok,entry1.getKey());
          coOcc2 *= qindexer.getCoOccurrence(tok,entry2.getKey());
        }
        if(coOcc1 > coOcc2) {
          return -1;
        }
        else if (coOcc1 < coOcc2) {
          return 1;
        }
        return 0;
      }

    };
    
    Comparator<Map.Entry<String,Double>> cmp1 = new Comparator<Map.Entry<String,Double>>() {
      @Override
      public int compare(Entry<String, Double> entry1,
          Entry<String, Double> entry2) {
        if(entry1.getValue() > entry2.getValue()) {
          return -1;
        }
        else if (entry1.getValue() < entry2.getValue()) {
          return 1;
        }
        return 0;
      }

    };
    
    List<Map.Entry<String,Double>> mapEntries = new ArrayList<Map.Entry<String,Double>>();
    for(Map.Entry<String, Double> entry:possibleCompletions.entrySet()) {
      mapEntries.add(entry);
    }
    
    if(query._tokens.size() == 1) {
      Collections.sort(mapEntries,cmp1);
    }
    else {
      Collections.sort(mapEntries,cmp);
    }
    
    int numSugg = mapEntries.size();
    if(numSugg > numSuggestionsFromTrie) {
      numSugg = numSuggestionsFromTrie;
    }
    int i = 0;
    Set<Integer> allDocIds = new HashSet<Integer>();
    for(Map.Entry<String, Double> entry:mapEntries) {
      i++;
      String word = entry.getKey();
      QDocument doc = null;
      int docid = -1;

      String queryString = QueryPhrase.getModifiedPhrase(query, word,true);
      Query q = new QueryPhrase(queryString,word);
      while((doc = qindexer.nextDoc(q, docid)) != null) {
        //This means this is a phrase doc
        //Add lambda1 here.
        docid = doc.getDocId();
        if(!allDocIds.contains(docid)) {
          ScoredQueryDocument scoredDoc = null;
          if(doc.getContent().startsWith(queryString.substring(1,queryString.length()-1))) {
            scoredDoc = new ScoredQueryDocument(doc, getScore(doc,true,true));
          }
          else {
            scoredDoc = new ScoredQueryDocument(doc, getScore(doc,true,false));
          }
          results.add(scoredDoc);
          allDocIds.add(docid);
        }
      }
      queryString = QueryPhrase.getModifiedPhrase(query, word,false);
      q = new QueryPhrase(queryString,word);
      while((doc = qindexer.nextDoc(q, docid)) != null) {
        //Processing the query tokens individually
        //Not as a phrase, so no lambda1
        docid = doc.getDocId();
        if(!allDocIds.contains(docid)) {
          ScoredQueryDocument scoredDoc = new ScoredQueryDocument(doc, getScore(doc,false,false));
          results.add(scoredDoc);
          allDocIds.add(docid);
        }
      }
      if(i == numSugg) {
        break;
      }
    }
    Collections.sort(results, Collections.reverseOrder());
    for(int j=results.size()-1;j>=numResults;j--) {
      results.remove(j);
    }
    return results;
  }
  
  private double getScore(QDocument doc,boolean isPhrase,boolean startsWithPhrase) {
    double score = lambda2 * doc.getFrequency() / qindexer._totalTermFrequency + lambda3 * doc.getNumViews() / qindexer.totalnumviews;
    if(isPhrase) {
      score += lambda1;
    }
    if(startsWithPhrase) {
      score += lambda4;
    }
    return score;
  }
}
