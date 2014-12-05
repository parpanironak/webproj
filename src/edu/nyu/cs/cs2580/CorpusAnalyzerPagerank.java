package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class CorpusAnalyzerPagerank extends CorpusAnalyzer {
  
  private Map<Integer, Integer> outlinkcount = new HashMap<Integer, Integer>();
  private ArrayList<Double> pagerank = new ArrayList<Double>();
  private Map<String, Integer> linkdocid_map = null;
  HashMap<Integer, List<Integer>> columns = new HashMap<Integer, List<Integer>>();
  //private HashMap<Integer, BufferedWriter> file_handles = new HashMap<Integer, BufferedWriter>();
  private int _numdocs = 0;
  Pattern p;
  public CorpusAnalyzerPagerank(Options options) {
    super(options);
    p = Pattern.compile("0; url=+(.+).*");
  }

  /**
   * This function processes the corpus as specified inside {@link _options}
   * and extracts the "internal" graph structure from the pages inside the
   * corpus. Internal means we only store links between two pages that are both
   * inside the corpus.
   * 
   * Note that you will not be implementing a real crawler. Instead, the corpus
   * you are processing can be simply read from the disk. All you need to do is
   * reading the files one by one, parsing them, extracting the links for them,
   * and computing the graph composed of all and only links that connect two
   * pages that are both in the corpus.
   * 
   * Note that you will need to design the data structure for storing the
   * resulting graph, which will be used by the {@link compute} function. Since
   * the graph may be large, it may be necessary to store partial graphs to
   * disk before producing the final graph.
   *
   * @throws IOException
   */
  @Override
  public void prepare() throws IOException {
    System.out.println("Preparing " + this.getClass().getName());

    linkdocid_map = LinkDocIDMapGenerator.get();
    System.out.println("Generating redirect map....");
    Map<String, String> redirectmap = generateRedirectMap();
    System.out.println("done....");
    
    HeuristicLinkExtractor le = null;
    final File corpusDirectory = new File(_options._corpusPrefix);
    for (final File fileEntry : corpusDirectory.listFiles()) {
    	int docid = linkdocid_map.get(fileEntry.getName());
    	HashSet<Integer> outlinks = new HashSet<Integer>();
    	le = new HeuristicLinkExtractor(fileEntry);
    	String outlinktitle = null;
    	while((outlinktitle = le.getNextInCorpusLinkTarget()) != null)
    	{
    		if(linkdocid_map.containsKey(outlinktitle))
    		{
    			String redirect = null;
    			if(redirectmap.containsKey(outlinktitle))
    			{
    				redirect = redirectmap.get(outlinktitle);
    			}    			
    			else
    			{
    				redirect = outlinktitle;
    			}
    			
    			if(linkdocid_map.containsKey(redirect))
        		{
    				int linkid = linkdocid_map.get(redirect);
        			outlinks.add(linkid);
        		}
    		}
    	}
    	outlinkcount.put(docid, outlinks.size());
    	makeColumns(docid, outlinks);
    	_numdocs++;
    }
	
//	for( int i : file_handles.keySet())
//	{
//		if(file_handles.containsKey(i))
//			if(file_handles.get(i) != null)
//			{
//				BufferedWriter bw = file_handles.get(i);
//				bw.close();
//			}
//	}
    return;
  }
  /**
   * This function computes the PageRank based on the internal graph generated
   * by the {@link prepare} function, and stores the PageRank to be used for
   * ranking.
   * 
   * Note that you will have to store the computed PageRank with each document
   * the same way you do the indexing for HW2. I.e., the PageRank information
   * becomes part of the index and can be used for ranking in serve mode. Thus,
   * you should store the whatever is needed inside the same directory as
   * specified by _indexPrefix inside {@link _options}.
   *
   * @throws IOException
   */
  @Override
  public void compute() throws IOException {
    System.out.println("Computing using " + this.getClass().getName());
    for(int i = 0; i < _numdocs; i++)
    {
    	pagerank.add(1.0);
    }
    calculatePageRank(_numdocs);
    storePageRank();
    return;
  }

  /**
   * During indexing mode, this function loads the PageRank values computed
   * during mining mode to be used by the indexer.
   *
   * @throws IOException
   */
  @Override
  public Object load() throws IOException {
    System.out.println("Loading using " + this.getClass().getName());
    try {
		retrievePageRank();
		return pagerank;
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return null;
  }
  
  	public void deleteDirectory(File file)
	    	throws IOException{
  			
	    	if(file.isDirectory()){
	 
	    		//directory is empty, then delete it
	    		if(file.list().length==0){
	 
	    		   file.delete();
	    		   //System.out.println("Directory is deleted : " + file.getAbsolutePath());
	 
	    		}else{
	 
	    		   //list all the directory contents
	        	   String files[] = file.list();
	 
	        	   for (String temp : files) {
	        	      //construct the file structure
	        	      File fileDelete = new File(file, temp);
	 
	        	      //recursive delete
	        	     deleteDirectory(fileDelete);
	        	   }
	 
	        	   //check the directory again, if empty then delete it
	        	   if(file.list().length==0){
	           	     file.delete();
	        	     //System.out.println("Directory is deleted : " + file.getAbsolutePath());
	        	   }
	    		}
	 
	    	}else{
	    		//if file, then delete it
	    		file.delete();
	    		//System.out.println("File is deleted : " + file.getAbsolutePath());
	    	}
	    }

	public void makeColumns(int docid, HashSet<Integer> links) throws IOException
	{
		for(int linkedDoc : links)
		{	
			
//			StringBuffer filestring = new StringBuffer(_options._corpusAnalyzerColPrefix);
//			filestring.append("docid_");
//			filestring.append(linkedDoc);
//			
//			File file = new File(filestring.toString());
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//			BufferedWriter bw = null;
//			if(file_handles.containsKey(linkedDoc))
//			{
//				bw = file_handles.get(linkedDoc);
//			}
//			else
//			{
//				FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
//				bw = new BufferedWriter(fw);
//				file_handles.put(linkedDoc, bw);
//			}
//			bw.write(Integer.toString(docid));
//			bw.newLine();
//			bw.flush();	
			
			if(columns.containsKey(linkedDoc))
			{
				List<Integer> l = columns.get(linkedDoc);
				l.add(docid);
			}
			else
			{
				List<Integer> l = new ArrayList<Integer>();
				l.add(docid);
				columns.put(linkedDoc, l);
			}
		}
	}
	
//	public void initDirectory()
//	{
//			File directory = new File(_options._corpusAnalyzerColPrefix);			
//			if(!directory.exists()){
//				 
//		           System.out.println("Directory does not exist.");
//		           directory.mkdir();
//		    }
//			else{	 
//				try{
//					System.out.println("Deleting Directory...");
//					deleteDirectory(directory);
//					System.out.println("Deleting Directory Done");
//					directory.mkdir();
//				}
//				catch(IOException e)
//				{
//					e.printStackTrace();
//		            System.exit(0);
//		        }
//		    }
//	}
	
	@SuppressWarnings("unchecked")
	public void calculatePageRank(int noofdocs) throws FileNotFoundException
	{
		double lambda = _options._lambda;
		int iter = _options._iterations;
		
		double gfactor = (1.0-lambda)/noofdocs;
		
		for(int x = 0; x < iter ; x++)
		{	
			ArrayList<Double> newpagerank = new ArrayList<Double>();
			for(int i = 0; i < noofdocs; i++)
			{
				double rank = 0.0;
				
//				StringBuffer filestring = new StringBuffer(_options._corpusAnalyzerColPrefix);
//				filestring.append("docid_");
//				filestring.append(i);
				
//				File column = new File(filestring.toString());
				if(!columns.containsKey(i))
				{
					rank = 1.0 - lambda;
				}
				else
				{
					Iterator<Integer> sc = columns.get(i).iterator();
					Set<Integer> col = new HashSet<Integer>();
					while(sc.hasNext())
					{
						int docid = sc.next();
						col.add(docid);
					}
					for(int j = 0; j < noofdocs; j++)
					{
						if(!col.contains(j))
						{
							rank += (pagerank.get(j) * gfactor);	
						}
						else
						{
							rank += (pagerank.get(j) * ((lambda / outlinkcount.get(j) + gfactor)));					
						}
					}
				}			
				newpagerank.add(rank);
			}
			pagerank = (ArrayList<Double>) newpagerank.clone();
		}
	}
	
	public void storePageRank() throws FileNotFoundException, IOException
	{
		File f = new File(_options._indexPrefix);
		if(!f.isDirectory())
			f.mkdir();
		
		System.out.println("Store Page Rank to: " + _options._indexPrefix);
		
		String prFile = _options._indexPrefix + _options._corpusAnalyzerPrName + (_options._lambda+_options._iterations);
		
		ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(prFile));
		writer.writeObject(this.pagerank);
		writer.close();
		System.out.println("Storing Page Rank complete");
	}
	
	@SuppressWarnings("unchecked")
	public void retrievePageRank() throws FileNotFoundException, IOException, ClassNotFoundException
	{
		String prFile = _options._indexPrefix + _options._corpusAnalyzerPrName + (_options._lambda+_options._iterations);
		ObjectInputStream reader = new ObjectInputStream(new FileInputStream(prFile));
		pagerank = (ArrayList<Double>) reader.readObject();
		reader.close();
	} 

	private static final Pattern pat = Pattern.compile("<meta[ ]+http-equiv=\"refresh\"[ ]*content=\".*url=([^\"]*).*");
	private static final String headstart = "<head>";
	private static final String headend = "</head>";
			
	public String getRedirectLink(String outlinktitle) throws IOException
	{
		File f = new File(_options._corpusPrefix +"/"+ outlinktitle);		
		if(f.exists())
		{
			Scanner sc = new Scanner(f, "UTF8");
			boolean flag = false;
			StringBuilder sb = new StringBuilder();
			while(sc.hasNext())
			{
				String l = sc.nextLine();
				
				if(l.matches(headstart))
				{
					flag = true;
				}
				if(flag)
				{
					sb.append(l);
				}
				if(l.matches(headend))
				{
					break;
				}			
			}
			sc.close();
			Matcher m = pat.matcher(sb.toString());
			if(m.find())
			{
				return getRedirectLink(m.group(1));
			}			
	        else
	        {
	        	return outlinktitle;
	        }       	
		}
		else
		{
			return null;
		}
	}
	
	private Map<String, String> generateRedirectMap() throws IOException
	{
		Map<String, String> redirectmap = new HashMap<String, String>();
		final File corpusDirectory = new File(_options._corpusPrefix);
		for (final File fileEntry : corpusDirectory.listFiles())
		{
			String fname = fileEntry.getName();
			String redirect = getRedirectLink(fname);
			if(redirect == null || fname.equals(redirect))
			{
				//redirectmap.put(fname, null);
			}
			else
				redirectmap.put(fname, redirect);
		}
		return redirectmap;
	}
}
