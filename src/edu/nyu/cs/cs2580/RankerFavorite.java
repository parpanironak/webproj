package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

public class RankerFavorite extends Ranker
{
  private double lambda = 0.75;
  public RankerFavorite(Options options, CgiArguments arguments,
      Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }
  
  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
    Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();
    //int x = _indexer.documentTermFrequency("web", "world wide web");
    //System.out.println(x);
   
    Document doc = null;
    
    int docid = -1;
    try {
	    while ((doc = _indexer.nextDoc(query, docid)) != null) {
	    	double totalscore=0.0;
	    	DocumentIndexed temp = (DocumentIndexed)doc;
		    ArrayList<Integer> postingList= temp.getPosting();
		    Iterator<Integer> iter = postingList.iterator();
		    ArrayList<Integer> countslist = new ArrayList<Integer>();
		    while (iter.hasNext()) 
		    {
		        Integer counts = (Integer) iter.next();
		        countslist.add(counts);    
		        for(int k = 0; k < counts; k++)
		        {
		        	if(iter.hasNext())
		        		iter.next();
		        	else
		        		break;
		        }
		    }
		    for(int i=0 ; i<query._tokens.size() ; i++)
		    {
		    	int totaltokensdoc=0;
		    	long totaltokenscorpus=0;
		    	double scorecomp1 = 0.0;
		    	double scorecomp2 = 0.0;
		    	double corpustokenfrequency = _indexer.corpusTermFrequency(query._tokens.get(i));
		    	String[] tokencomponents = query._tokens.get(i).split(" ");
		    	if(tokencomponents.length > 1)
		    	{
		    		totaltokensdoc = doc.get_numwords() - (tokencomponents.length - 1);
		    		totaltokenscorpus = _indexer.getTotalPhrasesCorpus(tokencomponents.length);
		    	}
		    	else
		    	{
		    		totaltokensdoc = doc.get_numwords();
		    		totaltokenscorpus = _indexer.getTotalwordsincorpus();
		    	}
		    	if(totaltokensdoc > 0)
		    		scorecomp1 = countslist.get(i)/(totaltokensdoc*1.0);
		    	else
		    		scorecomp1 = 0.0;
		    	if(totaltokenscorpus > 0)
	    		  scorecomp2 = (corpustokenfrequency*1.0)/(totaltokenscorpus*1.0);
		    	else
	    		  scorecomp2 = 0.0;
	    	  
		    	totalscore += Math.log(1 + (1.0 - lambda) * scorecomp1 + lambda * scorecomp2);
		    }
		    rankQueue.add(new ScoredDocument(doc, totalscore));
		    if (rankQueue.size() > numResults) {
		    	rankQueue.poll();
		    }
		    docid = doc._docid;
	    }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    Vector<ScoredDocument> results = new Vector<ScoredDocument>();
    ScoredDocument scoredDoc = null;
    while ((scoredDoc = rankQueue.poll()) != null) {
      results.add(scoredDoc);
    }
    Collections.sort(results, Collections.reverseOrder());
    return results;
  }
}