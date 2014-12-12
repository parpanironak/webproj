package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

public class QueryRanker {
  
  private QIndexerInvertedCompressed qindexer;
  private Options _options;
  private CgiArguments _arguments;
  
  protected QueryRanker(Options options, CgiArguments arguments, QIndexerInvertedCompressed qindexer) {
    _options = options;
    _arguments = arguments;
    this.qindexer = qindexer;
  }
  
  public Vector<ScoredQueryDocument> runQuery(Query query, int numResults) {
    Vector<ScoredQueryDocument> results = new Vector<ScoredQueryDocument>();
    Queue<ScoredQueryDocument> rankQueue = new PriorityQueue<ScoredQueryDocument>();
    QDocument doc = null;
    int docid = -1;
    while ((doc = qindexer.nextDoc(query, docid)) != null) {
      rankQueue.add(new ScoredQueryDocument(doc, doc.getFrequency()));
      if (rankQueue.size() > numResults) {
        rankQueue.poll();
      }
      docid = doc.getDocId();
    }

    ScoredQueryDocument scoredDoc = null;
    while ((scoredDoc = rankQueue.poll()) != null) {
      results.add(scoredDoc);
    }
    Collections.sort(results, Collections.reverseOrder());
    return results;
  }
}
