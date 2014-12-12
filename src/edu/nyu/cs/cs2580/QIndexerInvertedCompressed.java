package edu.nyu.cs.cs2580;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;

import edu.nyu.cs.cs2580.Compress.Compression;
import edu.nyu.cs.cs2580.Compress.GammaCompression;
import edu.nyu.cs.cs2580.SkipPointer.SkipPointer;
import edu.nyu.cs.cs2580.SearchEngine.Options;


/**
 * @CS2580: Implement this class for HW2.
 */
public class QIndexerInvertedCompressed implements Serializable{

  /**
   * 
   */
  private static final long serialVersionUID = -3454178520283822976L;

  protected Options _options = null;
  protected int _numDocs = 0;
  protected long _totalTermFrequency = 0;
  protected long totalwordsincorpus = 0;
  protected long totalnumviews = 0;
  
  protected PatriciaTrie<String, Double> trie = new PatriciaTrie<String, Double>(StringKeyAnalyzer.INSTANCE);
  private HashMap<String,HashMap<String,Double>> coOccurrenceMap = new HashMap<String,HashMap<String,Double>>();
  
  public class PostingList implements Serializable
  {

    /**
     * 
     */
    private static final long serialVersionUID = 4943742163285282047L;
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

  private Vector<QDocument> _documents = new Vector<QDocument>();
  protected HashMap<String,PostingList> index = new HashMap<String,PostingList>();
  private HashMap<String,SkipPointer> skippointermap = new HashMap<String, SkipPointer>();

  private HashMap<Integer,String> stringIdToWordMap = new HashMap<Integer,String>();

  
  private int skipSteps;

  public QIndexerInvertedCompressed(Options options) {
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
    this._options = options;
  }

  @SuppressWarnings("unchecked")
  public void constructIndex() throws IOException 
  {
      long x = (System.currentTimeMillis());
      
      CorpusAnalyzer analyzer = CorpusAnalyzer.Factory.getCorpusAnalyzerByOption(SearchEngine.OPTIONS);
      LogMiner miner = LogMiner.Factory.getLogMinerByOption(SearchEngine.OPTIONS);
      
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
  
  
      
      File f = new File(_options._indexPrefix + "qtotalwordsincorpus.long");
      FileWriter fw = new FileWriter(f.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(Long.toString(totalwordsincorpus));
      bw.newLine();
      bw.write(Integer.toString(_numDocs));
      bw.newLine();
      bw.write(Long.toString(totalnumviews));
      bw.newLine();
      bw.write(Long.toString(_totalTermFrequency));
      bw.newLine();
      bw.close();
      
      String indexFile = null;
      
      ObjectOutputStream writer = null;
  
      indexFile = _options._indexPrefix + "qskippointer.map";
      writer = new ObjectOutputStream(new FileOutputStream(indexFile));
      writer.writeObject(this.skippointermap);
      writer.close();
  
      indexFile = _options._indexPrefix + "qdoc.list";
      writer = new ObjectOutputStream(new FileOutputStream(indexFile));
      writer.writeObject(this._documents);
      writer.close();
  
      indexFile = _options._indexPrefix + "qwordids.map";
      writer = new ObjectOutputStream(new FileOutputStream(indexFile));
      writer.writeObject(this.stringIdToWordMap);
      writer.close();
      
      indexFile = _options._indexPrefix + "qtrie.map";
      writer = new ObjectOutputStream(new FileOutputStream(indexFile));
      writer.writeObject(this.trie);
      writer.close();
      
      loadCoOccurrence();
      
      indexFile = _options._indexPrefix + "cooccurrences.map";
      writer = new ObjectOutputStream(new FileOutputStream(indexFile));
      writer.writeObject(this.coOccurrenceMap);
      writer.close();
  
      System.out.println("Index File Created!");
      x = (System.currentTimeMillis() - x)/1000/60;
      System.out.println("Time to Construct:" + x + " mins.");
  }
  
  
  private void loadCoOccurrence() throws IOException {
    String coOccFileName = "data/cooccurrences.txt";
    BufferedReader br = new BufferedReader(new FileReader(coOccFileName));
    try {
      String line = null;
      do {
        line = br.readLine();
        if(line == null) {
          continue;
        }
        Scanner sc = new Scanner(line);
        sc.useDelimiter("\t");
        String word1 = sc.next();
        String word2 = sc.next();
        double coOccVal = Double.parseDouble(sc.next());
        if(!coOccurrenceMap.containsKey(word1)) {
          HashMap<String,Double> wordCoOccMap = new HashMap<String,Double>();
          coOccurrenceMap.put(word1, wordCoOccMap);
        }
        coOccurrenceMap.get(word1).put(word2,coOccVal);
      } while(line != null);
    }
    finally {
      br.close();
    }
  }


  @SuppressWarnings({ "unchecked"})
  public void loadIndex() throws IOException, ClassNotFoundException 
  {
      long x = (System.currentTimeMillis());
      String indexFile = null;
      this.skipSteps = _options.skips;
  
      Scanner sc = new Scanner(new File(_options._indexPrefix + "qtotalwordsincorpus.long"));
      this.totalwordsincorpus = sc.nextLong();
      this._numDocs = sc.nextInt();
      this.totalnumviews = sc.nextLong();
      this._totalTermFrequency = sc.nextLong();
      sc.close();
  
      merge();
  
      indexFile = _options._indexPrefix + "qdoc.list";
      ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));
      this._documents = (Vector<QDocument>) reader.readObject();
      reader.close();
  
      indexFile = _options._indexPrefix + "qskippointer.map";
      reader = new ObjectInputStream(new FileInputStream(indexFile));
      this.skippointermap = (HashMap<String, SkipPointer>) reader.readObject();
      reader.close();
  
      indexFile = _options._indexPrefix + "qwordids.map";
      reader = new ObjectInputStream(new FileInputStream(indexFile));
      this.stringIdToWordMap = (HashMap<Integer, String>) reader.readObject();
      reader.close();
      
      indexFile = _options._indexPrefix + "qtrie.map";
      reader = new ObjectInputStream(new FileInputStream(indexFile));
      this.trie = (PatriciaTrie<String, Double>) reader.readObject();
      reader.close();
      
      indexFile = _options._indexPrefix + "cooccurrences.map";
      reader = new ObjectInputStream(new FileInputStream(indexFile));
      this.coOccurrenceMap = (HashMap<String, HashMap<String,Double>>) reader.readObject();
      reader.close();
  
      x = (System.currentTimeMillis() - x)/1000/60;
      System.out.println("Time to load:" + x + " mins.");
  }
  
  @SuppressWarnings("unchecked")
  public void merge() throws IOException, ClassNotFoundException
  {
      String partialindexfile = _options._indexPrefix + _options.qindexdocsplitprefix;
      ObjectInputStream reader = new ObjectInputStream(new FileInputStream(partialindexfile + 0));
      this.index = (HashMap<String, QIndexerInvertedCompressed.PostingList>)reader.readObject();
      reader.close();
  
      int deno = _options.qindexdocsplit;
  
      /*for(int i = 1; i < _numDocs/deno; i++)
      {
          HashMap<String, PostingList> temp = null;
          reader = new ObjectInputStream(new FileInputStream(partialindexfile + i));
          temp = (HashMap<String, QIndexerInvertedCompressed.PostingList>)reader.readObject();
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
      }*/
  }

  public QDocument getDoc(int docid) {
    return _documents.get(docid);
  }

  public QDocument nextDoc(Query query, int docid) {

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

    QDocument doci = getQDocument(getDoc(docids[0]));
    doci.setPosting(returnposting);
    return doci;
  }
  
  public QDocument getQDocument(QDocument d) {
    QDocument di = new QDocument(d.getDocId());
    di.setNumViews(d.getNumViews());
    di.setContent(d.getContent());
    di.setFrequency(d.getFrequency());
    return di;
  }

  public int corpusDocFrequencyByTerm(String term) {
    if(index.containsKey(term))
      return index.get(term).getCorpusDocFrequency();
    else
      return 0;
  }

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
    String corpusDataFile = _options._query_log_file;
    System.out.println("Construct index from: " + corpusDataFile);

    HashMap<String,Integer> skipnumberlist = new HashMap<String,Integer>();
    HashMap<String,Integer> posinpostinglist = new HashMap<String,Integer>();
    HashMap<String,Integer> lastdocinserted = new HashMap<String,Integer>();
    HashMap<String,Integer> allWords = new HashMap<String,Integer>();

    int docIdIndex = 0;
    
    BufferedReader br = new BufferedReader(new FileReader(corpusDataFile));
    
    try {
      String line = null;
      do {
        line = br.readLine();
        if(line == null) {
          break;
        }
        line = line.trim();
        Scanner sc = new Scanner(line);
        sc.useDelimiter("\t");
        String phrase = sc.next();
        int frequency = Integer.parseInt(sc.next());
        int views = Integer.parseInt(sc.next());
        int docid = createDocument(phrase, frequency, views);
        processDocument(docid, phrase,
            skipnumberlist, 
            posinpostinglist, 
            lastdocinserted,
            allWords,
            docIdIndex);
//        if((docid+1)%_options.qindexdocsplit == 0)
//          flushIndex(docid/_options.qindexdocsplit);
      } while(line != null);
      if(index.size() != 0)
        //flushIndex(_numDocs/_options.qindexdocsplit);
        flushIndex(0);
    } finally {
      br.close();
    }
  }
  
  private void flushIndex(int id) throws FileNotFoundException, IOException
  {
    //System.out.println("index " + id);
    String partialindexfile = _options._indexPrefix + _options.qindexdocsplitprefix + id;  
    ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(partialindexfile));
    writer.writeObject(index);
    writer.close();
    this.index.clear();
  }

  private int createDocument(String content, int frequency, int views)
  {
    int docid = _documents.size();
    QDocument doc = new QDocument(docid);
    
    doc.setContent(content);
    doc.setFrequency(frequency);
    doc.setNumViews(views);
    totalnumviews += views;
    _totalTermFrequency += frequency;
    
    _documents.add(doc);      
    ++_numDocs;

    //doc._normfactor = Math.sqrt(normfactor);
    //doc._numwords = totalwords;
    return docid;
  }

  private void processDocument(int docid, String content,
      HashMap<String,Integer> skipnumberlist, 
      HashMap<String,Integer> posinpostinglist, 
      HashMap<String,Integer> lastdocinserted,
      HashMap<String,Integer> allWords,
      int indexIndex) 
  {
    HashMap<String, List<Integer>> tokens = new HashMap<String, List<Integer>>();
    //double normfactor = 0; 

    int totalwords = readTermVector(docid, content,
        skipnumberlist, 
        posinpostinglist, 
        lastdocinserted,
        allWords,
        indexIndex);

    //updateIndex(tokens, docid);   

    QDocument d = _documents.get(docid);
    for(String token: tokens.keySet()) 
    {
      //        int x = tokens.get(token).get(0);
      //        normfactor += x * x;
      index.get(token).increaseCorpusDocFreqency();
    }     
  }


  private int readTermVector(int docid, String content,
      HashMap<String,Integer> skipnumberlist, 
      HashMap<String,Integer> posinpostinglist, 
      HashMap<String,Integer> lastdocinserted,
      HashMap<String,Integer> allWords,
      int indexIndex
      ) 
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
      
      //String text = Stopwords.removeStopWords(word);
      String text = word;
      if(text != null) {
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
      //update trie
      if(trie.get(word) != null) {
        //this word is present in the trie
        trie.put(word, trie.get(word) + 1);
      }
      else {
        trie.put(word, 1.0);
      }
      
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
    //sort the map based on value and store
    //in linked hashmap in that order
    List<Map.Entry<Integer, Integer>> entries =
        new ArrayList<Map.Entry<Integer, Integer>>(stringToCountMap.entrySet());
    Collections.sort(entries, new Comparator<Map.Entry<Integer, Integer>>() {
      public int compare(Map.Entry<Integer, Integer> a, Map.Entry<Integer, Integer> b){
        //intentionally comparing b to a to sort in decreasing order
        return b.getValue().compareTo(a.getValue());
      }
    });

    stringToCountMap.clear();
    stringToCountMap = new LinkedHashMap<Integer,Integer>();
    for(Map.Entry<Integer, Integer> entry:entries) {
      stringToCountMap.put(entry.getKey(),entry.getValue());
    }
    updateIndex(tokens, docid,
        skipnumberlist, 
        posinpostinglist, 
        lastdocinserted);

    return wordcount-1;
  }

  private void updateIndex( HashMap<String,List<Integer>> tokens, int docid,
      HashMap<String,Integer> skipnumberlist, 
      HashMap<String,Integer> posinpostinglist, 
      HashMap<String,Integer> lastdocinserted
      ) 
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
  
  public double getCoOccurrence(String word1, String word2) {
    HashMap<String,Double> wordCoOccMap = coOccurrenceMap.get(word1);
    if(wordCoOccMap == null) {
      return 0.0;
    }
    Double d = coOccurrenceMap.get(word1).get(word2);
    if(d == null) {
      d = 0.0;
    }
    return d;
  }
}