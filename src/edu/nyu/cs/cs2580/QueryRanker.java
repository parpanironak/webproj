package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import edu.nyu.cs.cs2580.InstantQueryHandler.CgiArguments;

public class QueryRanker {
  
  private QIndexerInvertedCompressed qindexer;
  private int numSuggestionsFromTrie = 10;
  private double lambda1 = 0.3;
  private double lambda2 = 8993.8;
  private double lambda3 = 997.0999;
  private double lambda4 = 8.0;
  private double lambda5 = 0.8;
  private double lambda6 = 0.0001;
  
  protected QueryRanker(QIndexerInvertedCompressed qindexer) {
    this.qindexer = qindexer;
  }
  
  public Vector<ScoredQueryDocument> runQuery(final Query query, CgiArguments cgiArgs) {
    //We use the trie to try to complete the last word being typed
    Vector<ScoredQueryDocument> results = new Vector<ScoredQueryDocument>();
    String lastWord = query._tokens.get(query._tokens.size()-1);
    Map<String,Double> possibleCompletions = qindexer.trie.prefixMap(lastWord);
    
    final Map<String,Double> coOccurrenceVals = new HashMap<String,Double>();
    for(String possibleCompletion:possibleCompletions.keySet()) {
      double coOcc = 1.0;
      for(int k=0;k<query._tokens.size()-1;k++) {
        String tok = query._tokens.get(k);
        if(!tok.equals(possibleCompletion)) {
          coOcc *= qindexer.getCoOccurrence(tok,possibleCompletion);
        }
      }
      coOccurrenceVals.put(possibleCompletion, coOcc);
    }
    
    Comparator<Map.Entry<String,Double>> cmp = new Comparator<Map.Entry<String,Double>>() {
      @Override
      public int compare(Entry<String, Double> entry1,
          Entry<String, Double> entry2) {
        double coOcc1 = coOccurrenceVals.get(entry1.getKey());
        double coOcc2 = coOccurrenceVals.get(entry2.getKey());
        if(coOcc1 > coOcc2) {
          return -1;
        }
        else if (coOcc1 < coOcc2) {
          return 1;
        }
        if(entry1.getValue() > entry2.getValue()) {
          return -1;
        }
        else if (entry1.getValue() < entry2.getValue()) {
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
            scoredDoc = new ScoredQueryDocument(doc, getScore(doc,true,true,cgiArgs));
          }
          else {
            scoredDoc = new ScoredQueryDocument(doc, getScore(doc,true,false,cgiArgs));
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
          ScoredQueryDocument scoredDoc = new ScoredQueryDocument(doc, getScore(doc,false,false,cgiArgs));
          results.add(scoredDoc);
          allDocIds.add(docid);
        }
      }
      if(i == numSugg) {
        break;
      }
    }
    
    int numResults = cgiArgs.numQueryResults;
    
    Collections.sort(results, Collections.reverseOrder());
    for(int j=results.size()-1;j>=numResults;j--) {
      results.remove(j);
    }
    return results;
  }
  
  private double getScore(QDocument doc,boolean isPhrase,boolean startsWithPhrase,CgiArguments cgiArgs) {
    double score = lambda2 * doc.getFrequency() / qindexer._totalTermFrequency + lambda3 * doc.getNumViews() / qindexer.totalnumviews;// + lambda6 * doc.getIdf();
    if(isPhrase) {
      score += lambda1;
    }
    if(startsWithPhrase) {
      score += lambda4;
    }
    if(qindexer.userData.get(cgiArgs.uname) != null) {
      if(qindexer.userData.get(cgiArgs.uname).containsKey(doc.getContent()))
        score += lambda5 * (0.5 + Math.log(qindexer.userData.get(cgiArgs.uname).get(doc.getContent())));
    }
    return score;
  }
}
