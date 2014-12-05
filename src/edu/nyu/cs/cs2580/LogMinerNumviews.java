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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class LogMinerNumviews extends LogMiner {

  private Map<String, Integer> linkdocid_map = null; 	
  //ArrayList<Double> numviews = null;
  ArrayList<Integer> numviews = null;
  public LogMinerNumviews(Options options) {
    super(options);
  }

  /**
   * This function processes the logs within the log directory as specified by
   * the {@link _options}. The logs are obtained from Wikipedia dumps and have
   * the following format per line: [language]<space>[article]<space>[#views].
   * Those view information are to be extracted for documents in our corpus and
   * stored somewhere to be used during indexing.
   *
   * Note that the log contains view information for all articles in Wikipedia
   * and it is necessary to locate the information about articles within our
   * corpus.
   *
   * @throws IOException
   */
  @Override
  public void compute() throws IOException {
    System.out.println("Computing using " + this.getClass().getName());
    
    linkdocid_map = LinkDocIDMapGenerator.get();
	File file = new File("data/index/errorlogs.txt");
	FileWriter fw = new FileWriter(file.getAbsoluteFile());
	
	BufferedWriter bw = new BufferedWriter(fw);

	//Map<String, Integer> map = LinkDocIDMapGenerator.get();
	//numviews = new ArrayList<Double>(linkdocid_map.size());
	numviews = new ArrayList<Integer>(linkdocid_map.size());
	for (int i = 0; i < linkdocid_map.size(); i++) {
		//numviews.add(0.0); 
		numviews.add(0);
	}
	
	File f = new File("data/log/20140601-160000.log");
	Scanner sc = new Scanner(f);
	
	while(sc.hasNext())
	{
		String line[] = sc.nextLine().split(" ");
		String link  = null;
		try{
				link = decode(line[1]).trim();			
		}
		catch(Exception e)
		{
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			
			bw.write(Arrays.toString(line));
			bw.newLine();
			bw.write("==========Decode error trace===========");
			bw.newLine();
			bw.write(errors.toString());
			bw.newLine();
			bw.write("=====================");
			bw.newLine();
			bw.newLine();
			
		}
		if(link == null)
			continue;
		
		int numview = 0;
		try
		{
			numview = Integer.parseInt(line[2]);
			
			if(linkdocid_map.containsKey(link))
			{
				int docid = linkdocid_map.get(link);
				numviews.set(docid, numviews.get(docid) + numview);
				//totalnumviews += numview;
			}
		}
		catch(Exception e)
		{
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			bw.write(Arrays.toString(line));
			bw.newLine();
			bw.write("==========parse error trace===========");
			bw.newLine();
			bw.write(errors.toString());
			bw.newLine();
			bw.write("=====================");
			bw.newLine();
			bw.newLine();
		}
		
	}
	
//	for(int i=0 ; i < numviews.size() ; i++) 
//	{
//		  double numview = numviews.get(i) * totalnumviews;
//		  numviews.set(i, numview);
//	}
	
	bw.close();
	sc.close();
	storeNumView();	
    return;
  }

  /**
   * During indexing mode, this function loads the NumViews values computed
   * during mining mode to be used by the indexer.
   * 
   * @throws IOException
   */
  	@Override
  	public Object load() throws IOException {
  		System.out.println("Loading using " + this.getClass().getName());
  		try {
			retrieveNumView();
			return numviews;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		return null;
  	}
  
  
  	public static String decode(String s) throws UnsupportedEncodingException
  	{
  		return URLDecoder.decode(s, "UTF-8");		
  	}
  
  	public void storeNumView() throws FileNotFoundException, IOException
  	{
  		File f = new File(_options._indexPrefix);
		if(!f.isDirectory())
			f.mkdir();
		
		System.out.println("Store Num Views to: " + _options._indexPrefix);
		String nvFile = _options._indexPrefix + _options._logMinerNvName;
		
		ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(nvFile));
		writer.writeObject(this.numviews);
		writer.close();
		System.out.println("Storing Num Views complete");
  	}

	@SuppressWarnings("unchecked")
	public void retrieveNumView() throws FileNotFoundException, IOException, ClassNotFoundException
	{
		String nvFile = _options._indexPrefix + _options._logMinerNvName;
		ObjectInputStream reader = new ObjectInputStream(new FileInputStream(nvFile));
		//numviews = (ArrayList<Double>) reader.readObject();
		numviews = (ArrayList<Integer>) reader.readObject();
		reader.close();
	} 
	
//	public static ArrayList<Double> LoadNumView(String nvFile) throws FileNotFoundException, IOException, ClassNotFoundException
//	{
//		ObjectInputStream reader = new ObjectInputStream(new FileInputStream(nvFile));
//		ArrayList<Double> numviews = (ArrayList<Double>) reader.readObject();
//		reader.close();
//		return numviews;
//	}

}