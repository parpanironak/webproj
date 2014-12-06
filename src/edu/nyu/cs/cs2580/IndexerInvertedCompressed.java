package edu.nyu.cs.cs2580;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.nyu.cs.cs2580.Compress.Compression;
import edu.nyu.cs.cs2580.Compress.GammaCompression;
import edu.nyu.cs.cs2580.SkipPointer.SkipPointer;
import edu.nyu.cs.cs2580.SearchEngine.Options;


/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedCompressed extends Indexer implements Serializable{

  private static final long serialVersionUID = -113983643337683565L;

  public class PostingList implements Serializable
  {
    private static final long serialVersionUID = -3373610604746592865L;

    private int count;
    private BitSet bits;
    private Compression compress;
    private int corpusDocFrequency;
    private int corpusTermFrequency;

    public BitSet getBits() {
      return bits;
    }
    
    public void merge(PostingList pl)
    {
      compress.set(this.bits, pl.getBits(), this.count, this.count + pl.getCount());
      this.count += pl.getCount();
      this.corpusDocFrequency += pl.getCorpusDocFrequency();
      this.corpusTermFrequency += pl.getCorpusTermFrequency();
    }

    public void setBits(BitSet bits) {
      this.bits = bits;
    }

    public PostingList()
    {
      count = 0;
      bits = new BitSet();
      compress = new GammaCompression();
    }

    public int getCount() {
      return count;
    }

    public void setCount(int count) {
      if(count < this.count)
        System.out.println(count);

      this.count = count;
    }   

    public void add(int x)
    {
      setCount(compress.compress(x, getBits(), count));
    }

    public void add(List<Integer> list)
    {
      for(Integer i: list)
        setCount(compress.compress(i, getBits(), count));
    }

    public int[] get(int pos)
    {
      try {
        return compress.deCompress(getBits(), getCount(), pos);
      } catch (Exception e) {
        // TODO: handle exception
        return new int[]{-1,-1};
      }     
    }

    public int getCorpusDocFrequency() {
      return corpusDocFrequency;
    }

    public void setCorpusDocFrequency(int corpusDocFrequency) {
      this.corpusDocFrequency = corpusDocFrequency;
    }

    public int getCorpusTermFrequency() {
      return corpusTermFrequency;
    }

    public void setCorpusTermFrequency(int corpusTermFrequency) {
      this.corpusTermFrequency = corpusTermFrequency;
    }

    public void increaseCorpusTermFreqency()
    {
      this.corpusTermFrequency++;
    }

    public void increaseCorpusDocFreqency()
    {
      this.corpusDocFrequency++;
    }

  }

  private Vector<DocumentIndexed> _documents = new Vector<DocumentIndexed>();
  private HashMap<String,PostingList> index = new HashMap<String,PostingList>();
  private HashMap<String,SkipPointer> skippointermap = new HashMap<String, SkipPointer>();

  
  private HashMap<String,Integer> skipnumberlist = new HashMap<String,Integer>();
  private HashMap<String,Integer> posinpostinglist = new HashMap<String,Integer>();
  private HashMap<String,Integer> lastdocinserted = new HashMap<String,Integer>();
  private HashMap<String,Integer> allWords = new HashMap<String,Integer>();
  
  private HashMap<Integer,String> stringIdToWordMap = new HashMap<Integer,String>();
 
  ArrayList<Double> pagerank = null;
  ArrayList<Integer> numviews = null;
  
  private int skipSteps;
  
  public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  @SuppressWarnings("unchecked")
@Override
  public void constructIndex() throws IOException 
  {
	    long x = (System.currentTimeMillis());
	    CorpusAnalyzer analyzer = CorpusAnalyzer.Factory.getCorpusAnalyzerByOption(SearchEngine.OPTIONS);
	    pagerank = (ArrayList<Double>)analyzer.load();
	    LogMiner miner = LogMiner.Factory.getLogMinerByOption(SearchEngine.OPTIONS);
	    numviews = (ArrayList<Integer>)miner.load();
	    
	    skipnumberlist = new HashMap<String,Integer>();
	    posinpostinglist = new HashMap<String,Integer>();
	    lastdocinserted = new HashMap<String,Integer>();
	    allWords = new HashMap<String,Integer>();
	    
	    skipSteps = _options.skips;
	    try {
	      parse();
	    } catch (Exception e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	
	    System.out.println(
	        "Indexed " + Integer.toString(_numDocs) + " docs with " +
	            Long.toString(_totalTermFrequency) + " terms.");
	
	
	    
	    File f = new File(_options._indexPrefix + "totalwordsincorpus.long");
	    FileWriter fw = new FileWriter(f.getAbsoluteFile());
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(Long.toString(totalwordsincorpus));
	    bw.newLine();
	    bw.write(Integer.toString(_numDocs));
	    bw.newLine();
	    bw.write(Double.toString(totalpagerank));
	    bw.newLine();
	    bw.write(Long.toString(totalnumviews));
	    bw.newLine();
	    bw.close();
	
	    
	    String indexFile = _options._indexPrefix + _options._index_file;
	    System.out.println("Store index to: " + indexFile);
	    
	    ObjectOutputStream writer = null;
	
	    indexFile = _options._indexPrefix + "skippointer.map";
	    writer = new ObjectOutputStream(new FileOutputStream(indexFile));
	    writer.writeObject(this.skippointermap);
	    writer.close();
	
	    indexFile = _options._indexPrefix + "doc.list";
	    writer = new ObjectOutputStream(new FileOutputStream(indexFile));
	    writer.writeObject(this._documents);
	    writer.close();
	
	    indexFile = _options._indexPrefix + "wordids.map";
	    writer = new ObjectOutputStream(new FileOutputStream(indexFile));
	    writer.writeObject(this.stringIdToWordMap);
	    writer.close();
	
	    System.out.println("Index File Created!");
	    x = (System.currentTimeMillis() - x)/1000/60;
	    System.out.println("Time to Construct:" + x + " mins.");
  }


  @SuppressWarnings({ "unchecked"})
@Override
  public void loadIndex() throws IOException, ClassNotFoundException 
  {
	    long x = (System.currentTimeMillis());
	    String indexFile = _options._indexPrefix + _options._index_file;    
	    System.out.println("Load index from: " + indexFile);
	    this.skipSteps = _options.skips;
	
	    Scanner sc = new Scanner(new File(_options._indexPrefix + "totalwordsincorpus.long"));
	    this.totalwordsincorpus = sc.nextLong();
	    this._numDocs = sc.nextInt();
	    this.totalpagerank = sc.nextDouble();
	    this.totalnumviews = sc.nextLong();
	    sc.close();
	
	    merge();
	
	    indexFile = _options._indexPrefix + "doc.list";
	    ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));
	    this._documents = (Vector<DocumentIndexed>) reader.readObject();
	    reader.close();
	
	    indexFile = _options._indexPrefix + "skippointer.map";
	    reader = new ObjectInputStream(new FileInputStream(indexFile));
	    this.skippointermap = (HashMap<String, SkipPointer>) reader.readObject();
	    reader.close();
	
	    indexFile = _options._indexPrefix + "wordids.map";
	    reader = new ObjectInputStream(new FileInputStream(indexFile));
	    this.stringIdToWordMap = (HashMap<Integer, String>) reader.readObject();
	    reader.close();
	
	    x = (System.currentTimeMillis() - x)/1000/60;
	    System.out.println("Time to load:" + x + " mins.");
  }
  
  @SuppressWarnings("unchecked")
  public void merge() throws IOException, ClassNotFoundException
  {
	    String partialindexfile = _options._indexPrefix + _options.indexdocsplitprefix;
	    ObjectInputStream reader = new ObjectInputStream(new FileInputStream(partialindexfile + 0));
	    this.index = (HashMap<String, IndexerInvertedCompressed.PostingList>)reader.readObject();
	    reader.close();
	
	    int deno = _options.indexdocsplit;
	
	    for(int i = 1; i < _numDocs/deno; i++)
	    {
		      HashMap<String, PostingList> temp = null;
		      reader = new ObjectInputStream(new FileInputStream(partialindexfile + i));
		      temp = (HashMap<String, IndexerInvertedCompressed.PostingList>)reader.readObject();
		      reader.close();
		
		      for(String token: temp.keySet())
		      {
			        if(this.index.containsKey(token))
			        {
			        	this.index.get(token).merge(temp.get(token));
			        }
			        else
			        {
			        	this.index.put(token, temp.get(token));
			        }
		      }
	    }
  }



  @Override
  public Document getDoc(int docid) {
    return _documents.get(docid);
  }

  /**
   * In HW2, you should be using {@link DocumentIndexed}
   */
  @Override
  public Document nextDoc(Query query, int docid) {

    int docids[] = new int[query._tokens.size()];
    ArrayList<Integer> returnposting= new ArrayList<Integer>();

    int maxdocid = -1;
    boolean flag = true;

    for(int i = 0 ; i < docids.length ; i++)
    {
      docids[i] = -1;
    }

    for(int i = 0; i < docids.length; i++)
    {
	      String token = query._tokens.get(i);
	      ArrayList<Integer> posting = next(token, (maxdocid > docid ? maxdocid - 1: docid));
	      if(posting == null)
	      {
	        return null;
	      }
	
	      docids[i] = posting.get(0);       
	      if(docids[i] < 0 || docids[i] >= _documents.size())
	      {
	    	  return null;
	      }
	
	      returnposting.addAll(posting.subList(1, posting.size()));
	
	      if(i > 0 && docids[i] != docids[i-1])
	      {
	    	  flag = false;
	      }
	      if(maxdocid < docids[i])
	      {
	    	  maxdocid = docids[i];
	      }
    }
    if(!flag)
    {
    	  return nextDoc(query, maxdocid - 1);
    }

    DocumentIndexed doci = getDocumentIndexed(getDoc(docids[0]));
    doci.setPosting(returnposting);
    return doci;
  }
  
  public DocumentIndexed getDocumentIndexed(Document d) {
    DocumentIndexed di = new DocumentIndexed(d._docid);
    di.set_numwords(d.get_numwords());
    di.setPageRank(d.getPageRank());
    di.setTitle(d.getTitle());
    di.setUrl(d.getUrl());
    di.setNumViews(d.getNumViews());
    return di;
  }

  @Override
  public int corpusDocFrequencyByTerm(String term) {
    if(index.containsKey(term))
      return index.get(term).getCorpusDocFrequency();
    else
      return 0;
  }

  @Override
  public int corpusTermFrequency(String term) {
    if(term.contains(" "))
      return (int)corpusPhraseFrequency(term);

    if(index.containsKey(term))
      return index.get(term).getCorpusTermFrequency();
    else
      return 0;
  }

  /**
   * @CS2580: Implement this to work with your RankerFavorite.
   */
  @Override
  public int documentTermFrequency(String term, int docid) {
    ArrayList<Integer> posting = new ArrayList<Integer>();
    
    PostingList pl = index.get(term);
    
    int startpos = 0;
    
    startpos = getPosting(pl, startpos, posting); 
    int offset = posting.get(0);
      if(offset > docid)
      {
        return 0;
      }     
      else if(offset == docid)
      {
        return posting.get(1);
      }
      
      SkipPointer.Pair pair = null;
      
      if(skippointermap.containsKey(term))
      {
        pair = skippointermap.get(term).search(docid);
      }     
      if(pair != null)
      {
        startpos = (int)pair.getPos();
        offset = pair.getDocid();
        if(offset == -1 || offset > docid)
        {
          return 0;
        }
        else
        {
          posting.clear();
          startpos = getPosting(pl, startpos, posting);
          offset += posting.get(0);
        }
    }
      
      while(offset < docid)
      {
        posting.clear();
      startpos = getPosting(pl, startpos, posting);
      offset += posting.get(0);
      }
      if(offset == docid)
      {
        return posting.get(1);
      }
    return 0;
  }


  public void parse() throws Exception 
  {
    String corpusDirectoryString = _options._corpusPrefix;
    System.out.println("Construct index from: " + corpusDirectoryString);

    final File corpusDirectory = new File(corpusDirectoryString);
    int i = 0;
    
    for (final File fileEntry : corpusDirectory.listFiles()) 
    {

      if(i == 100)
        break;
      
      //i++;
      
      if (!fileEntry.isDirectory()) 
      {
        String url = fileEntry.getName();
        String title = null;
        StringBuilder sb = new StringBuilder();
        Element doc = Jsoup.parse(fileEntry, "UTF-8").body();
        Element head = doc.select("h1[id=firstHeading]").first();
        if(head != null && head.text() != null) 
        {
          title = head.text().trim();
          sb.append(title.toLowerCase());
          sb.append(' ');
        }
        Elements content_text = doc.select("div[id=mw-content-text]");

        for (Element elem : content_text) 
        {
          Elements paras = elem.getElementsByTag("p");
          
          for (Element para : paras) 
          {
            if(para.text() != null) 
            {
              sb.append(para.text());
              sb.append(' ');
            }
          }
        } 
        doc = null;
        int docid = createDocument(title, url);
        processDocument(docid, sb.toString().toLowerCase().replaceAll("[^a-zA-Z0-9 ]", " "));
        
        if((docid+1)%_options.indexdocsplit == 0)
          flushIndex(docid/_options.indexdocsplit);
      }
    }
    if(index.size() != 0)
      flushIndex(_numDocs/_options.indexdocsplit);
  }
  
  
  public void simpleparse() throws IOException
  {
	  String corpusDirectoryString = _options._corpusPrefix;
	    System.out.println("Construct index from: " + corpusDirectoryString);
	    
	    final File corpusDirectory = new File(corpusDirectoryString);

	    
	    for (final File fileEntry : corpusDirectory.listFiles()) 
	    {
	    	Scanner sc = new Scanner(fileEntry);
	    	StringBuilder sb = new StringBuilder(); 
	    	if(sc.hasNext())
	    	{	
	    		String title = sc.nextLine();
	    		String url = fileEntry.getName();
	    		
	    		int docid = createDocument(title, url);
	    		while(sc.hasNext())
	    		{	
	    			sb.append(sc.nextLine());
	    			sb.append(' ');
	    		}
	    		
	    		processDocument(docid, sb.toString().trim());
	    		
	    		if((docid+1)%_options.indexdocsplit == 0)
	    		{
	    			flushIndex(docid/_options.indexdocsplit);
	    		}
	    	}
	    	sc.close();    	
	    	
	    }
	    
	    if(index.size() != 0)
	        flushIndex(_numDocs/_options.indexdocsplit);
  }
  
  private void flushIndex(int id) throws FileNotFoundException, IOException
  {
    //System.out.println("index " + id);
    String partialindexfile = _options._indexPrefix + _options.indexdocsplitprefix + id;  
    ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(partialindexfile));
    writer.writeObject(this.index);
    writer.close();
    this.index.clear();
  }

  private int createDocument(String title, String url)
  {
    int docid = _documents.size();
    DocumentIndexed doc = new DocumentIndexed(docid);
    doc.setTitle(title);
    doc.setUrl(url);      
    
    doc.setPageRank(pagerank.get(docid));
    totalpagerank += pagerank.get(docid);
    doc.setNumViews(numviews.get(docid));
    totalnumviews += numviews.get(docid);
    
    _documents.add(doc);      
    ++_numDocs;

    //doc._normfactor = Math.sqrt(normfactor);
    //doc._numwords = totalwords;
    return docid;
  }

  private void processDocument(int docid, String content) 
  {
    HashMap<String, List<Integer>> tokens = new HashMap<String, List<Integer>>();
    //double normfactor = 0; 

    int totalwords = readTermVector(docid, content);

    //updateIndex(tokens, docid);   

    DocumentIndexed d = _documents.get(docid);
    d.set_numwords(totalwords);
    for(String token: tokens.keySet()) 
    {
      index.get(token).increaseCorpusDocFreqency();
    }     
  }


  private int readTermVector(int docid, String content) 
  {
    HashMap<String, List<Integer>> tokens = new HashMap<String, List<Integer>>();

    Scanner s = new Scanner(content);  // Uses white space by default.

    int wordcount = 1;

    HashMap<String, Integer> lastwordpos = new HashMap<String, Integer>();
    PorterStemming stemmer = new PorterStemming();
    LinkedHashMap<Integer,Integer> stringToCountMap = new LinkedHashMap<Integer,Integer>();

    while (s.hasNext()) 
    {
      String word = s.next();
      
      String text = Stopwords.removeStopWords(word);
      if(text != null) {
        text = stemmer.stem(text);
        if(text == null) {
          continue;
        }
        //take care of auxilliary structure HERE
        if(!allWords.containsKey(text)) {
          int stringId = allWords.size();
          allWords.put(text, stringId);
          stringIdToWordMap.put(stringId, text);
        }
        int stringId = allWords.get(text);
        int stringCount = 0;
        if(stringToCountMap.containsKey(stringId)) {
          stringCount = stringToCountMap.get(stringId);
        }
        stringToCountMap.put(stringId, stringCount+1);
      }
      else {
        continue;
      }

      word = text;
      totalwordsincorpus++;

      if(index.containsKey(word))
        index.get(word).increaseCorpusTermFreqency();
      else
      {

        PostingList p = new PostingList();
        index.put(word, p);
        p.increaseCorpusTermFreqency();
      }

      if(tokens.containsKey(word)) 
      {
        List<Integer> positions = tokens.get(word);
        positions.add(wordcount - lastwordpos.get(word));
        lastwordpos.put(word, wordcount);
        tokens.put(word, positions);
      }
      else 
      {
        List<Integer> positions = new ArrayList<Integer>();
        positions.add(wordcount);
        lastwordpos.put(word, wordcount);
        tokens.put(word, positions);
      }
      wordcount++;
    }
    s.close();
    
    updateIndex(tokens, docid);

    return wordcount-1;
  }

  private void updateIndex( HashMap<String,List<Integer>> tokens, int docid) 
  {
    for(String word : tokens.keySet()) 
    {
      List<Integer> postions = tokens.get(word);
      PostingList pl = null;
      if(index.containsKey(word)) 
      {
        pl = index.get(word);
      }
      else
      {
        pl = new PostingList();
        index.put(word, pl);
      }

      int lastdocid = 0;
      if(lastdocinserted.containsKey(word)) 
      {
        lastdocid = lastdocinserted.get(word);
      }
      int initialpos = pl.getCount();
      pl.add(docid - lastdocid);
      pl.add(postions.size());
      pl.add(postions);

      int deltapos = pl.getCount() - initialpos;
      
      if(!posinpostinglist.containsKey(word))
      {
        posinpostinglist.put(word, deltapos);
      }
      else
        posinpostinglist.put(word, posinpostinglist.get(word) + deltapos);
      
      lastdocinserted.put(word, docid);

      int lastupdated = 0;

      if(skipnumberlist.containsKey(word)) 
      {
        lastupdated = skipnumberlist.get(word);           
      }
      
      if(lastupdated%skipSteps == 0) 
      {
        lastupdated = 0;
        SkipPointer skippointer = null;
        if(skippointermap.containsKey(word)) 
        {
          skippointer = skippointermap.get(word);
        }
        else 
        {
          skippointer = new SkipPointer();
          skippointermap.put(word, skippointer);
        }
        skippointer.addPointer(docid, pl.getCount());
      }

      skipnumberlist.put(word, lastupdated + 1);
    }
    return;
  } 

  private ArrayList<Integer> next(String token, int docid)
  {
    if(token.contains(" "))
    {
      Query query = new Query(token);
      query.processQuery();
      return nextPhraseDoc(query,docid);
    }
    PostingList pl = index.get(token);
    if(pl == null)
    {
      return null;
    }

    int startpos = 0;
    ArrayList<Integer> posting = new ArrayList<Integer>(); 
    startpos = getPosting(pl, startpos, posting);
    int offset = posting.get(0);

    if(offset > docid)
    {
      return posting;
    }

    SkipPointer.Pair pair = null;
    if(skippointermap.containsKey(token))
    {
      SkipPointer sp = skippointermap.get(token);
      pair = sp.search(docid);
    }     
    if(pair != null)
    {
      startpos = (int)pair.getPos();
      offset = pair.getDocid();
      if(offset == -1 || offset > docid)
      {
        posting.clear();
        startpos = getPosting(pl ,startpos, posting);
        offset = posting.get(0);
      }
    }

    //      
    if(offset > docid)
    {
      return posting;
    }

    while(startpos < pl.getCount() && startpos != -2) 
    {
      posting.clear();
      startpos = getPosting(pl, startpos, posting);

      if(startpos == -2)
      {
        break;
      }
      offset += posting.get(0);
      posting.set(0, offset);
      if(offset > docid)
      {
        break;
      }
    }     

    if(startpos >= pl.getCount() || startpos == -2) 
    {
      return null;
    }
    if(offset >= _documents.size())
    {
      return null;
    }
    return posting;
  }

  private int getPosting(PostingList pl, int startpos, ArrayList<Integer> posting)
  {
    if(startpos == -1)
    {
      return -2;
    }
    int pair[] = pl.get(startpos);
    int docid = pair[0];
    pair = pl.get(pair[1]);
    int count = pair[0];

    posting.add(docid);
    posting.add(count);

    int position = 0;
    for(int i=0;i<count;i++) 
    {
      pair = pl.get(pair[1]);
      position += pair[0];
      posting.add(position);
    }
    return pair[1];
  }

  private ArrayList<Integer> nextPhraseDoc(Query query, int docid)
  {
    ArrayList<ArrayList<Integer>> postinglist = new ArrayList<ArrayList<Integer>>();
    int docids[] = new int[query._tokens.size()];
    int maxdocid = -1;
    boolean flag = true;
    for(int i = 0; i < query._tokens.size(); i++)
    {
      String token = query._tokens.get(i);
      ArrayList<Integer> posting = next(token, maxdocid > docid ? maxdocid - 1 : docid);
      if(posting == null)
      {
        return null;
      }

      docids[i] = posting.get(0);

      try {
        postinglist.add( new ArrayList<Integer>(posting.subList(2, posting.size())));       
      } catch (Exception e) {
        // TODO: handle exception
        e.printStackTrace();
      } 


      if(i > 0 && docids[i] != docids[i-1])
      {
        flag = false;
      }
      if(maxdocid < docids[i])
      {
        maxdocid = docids[i];
      }
    }
    if(!flag)
    {
      return nextPhraseDoc(query, maxdocid - 1);
    }
    int size = postinglist.size(); 
    for(int i = 1; i < size; i++)
    {
      decreasePosting(postinglist.get(i), i);
    }

    ArrayList<Integer> phraseposting = postinglist.get(0);
    for(int i = 1; i < size; i++)
    {
      phraseposting.retainAll(postinglist.get(i));
    }
    if(phraseposting.isEmpty())
    {
      return nextPhraseDoc(query, maxdocid);
    }

    phraseposting.add(0, phraseposting.size());
    phraseposting.add(0, docids[0]);
    return phraseposting;
  }

  private void decreasePosting(ArrayList<Integer> list, int number)
  {
    for (int i = 0; i < list.size(); i++)
    {
      int item = list.get(i);
      list.set(i,item - number);
    }
  }

  public long getTotalPhrasesCorpus(int tokenwordcount)
  {
    long numtokenscorpus=0;
    for(int i=0 ; i<_documents.size() ; i++)
    {
      Document d = _documents.get(i);
      if(d.get_numwords() >= tokenwordcount)
        numtokenscorpus += d.get_numwords() - (tokenwordcount - 1);
    }
    return numtokenscorpus;
  }

  public int corpusPhraseFrequency(String term)
  {
    Document doc = null;
    int totalphrases=0;
    String quotedterm = "\"" + term + "\"";
    Query query = new QueryPhrase(quotedterm);
    query.processQuery();
    int docid = -1;
    while ((doc = nextDoc(query, docid)) != null) {
      DocumentIndexed temp = (DocumentIndexed)doc;
      ArrayList<Integer> postingList= temp.getPosting();
      totalphrases += postingList.get(0);
      docid = doc._docid;
    }
    return totalphrases;
  }
}