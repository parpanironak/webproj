package edu.nyu.cs.cs2580;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class LogSimulation {

	
	private Map<String, Integer> linkdocid_map = null; 	
	private ArrayList<Integer> numviews = null;
	
	private class Pair
	{
		private int numviews;
		private int frequency;
		
		public Pair(int numviews, int frequency) {
			this.numviews = numviews;
			this.frequency = frequency;
		}
		
		public int getNumviews() {
			return numviews;
		}
		public int getFrequency() {
			return frequency;
		}
		public void add(int view, int freq)
		{
			this.numviews += view;
			this.frequency +=freq;
		}
		
	}
	void loadLinkDocidMap(String src) throws IOException
	{
		LinkDocIDMapGenerator.generateMap(src);
		linkdocid_map = LinkDocIDMapGenerator.get();
	}
	
	@SuppressWarnings("unchecked")
	void loadNumviews(String src) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		ObjectInputStream reader = new ObjectInputStream(new FileInputStream(src));
		numviews = (ArrayList<Integer>)reader.readObject();
		reader.close();		
	}
	
	public static int binarysearch(ArrayList<Integer> prefixsum, int x)
	{
		int low = 0;
		int high = prefixsum.size() - 1;
		
		if(x < prefixsum.get(low))
			return -1;
		
		if(x > prefixsum.get(high))
			return high;
		
		while(low <= high)
		{
			int mid = (high+low)/2;
			
			if(prefixsum.get(mid) == x)
			{
				return mid;
				
			}
			else if (x < prefixsum.get(mid))
			{
				high = mid - 1;
			}
			else
			{
				if(prefixsum.get(mid + 1) > x)
					return mid;
				else
					low = mid + 1;
			}			
		}
		return -2;
	}
	
	public static void simulate(int views, ArrayList<Integer> qnumviews, ArrayList<Integer> counts)
	{
		Random r = new Random();
		ArrayList<Integer> prefixsum = generatePrefixSum(counts);
		int randmax = prefixsum.get(prefixsum.size() - 1);
		for(int i = 0 ; i < views; i++)
		{
			int index = binarysearch(prefixsum,r.nextInt(randmax)) + 1;
			qnumviews.set(index, qnumviews.get(index) + 1);
		}
	}
	
	public static ArrayList<Integer> generatePrefixSum(ArrayList<Integer> frequency)
	{
		ArrayList<Integer> prefixsum = new ArrayList<Integer>();
		
		int sum = 0;
		Iterator<Integer> i = frequency.iterator();
		
		while(i.hasNext())
		{
			sum += i.next();
			prefixsum.add(sum);
		}
				
		return prefixsum;
	}
	
	@SuppressWarnings("resource")
	public static void loadFile(File f, ArrayList<String> phraselist, ArrayList<Integer> counts) throws FileNotFoundException
	{
		Scanner sc = new Scanner(f).useDelimiter("[\t\n]");
		
		while(sc.hasNext())
		{
			String phrase = sc.next().trim();
			int count = Integer.parseInt(sc.next().trim());
			phraselist.add(phrase);
			counts.add(count);
		}
		
		sc.close();
	}
	
	public static void updateprefixsumlist(ArrayList<Integer> prefixsum, int index, int count)
	{
		prefixsum.remove(index);
		
		for(int i = index; i < prefixsum.size() ; i++)
		{
			prefixsum.set(i, prefixsum.get(i) - count);
		}
	}
	
	public static void sample(ArrayList<Integer> counts, 
			ArrayList<String> phrases, 
			int n,
			ArrayList<Integer> selectedcounts,
			ArrayList<String> selectedphrases )
	{
		ArrayList<Integer> prefixsum = null;
		Random r = new Random();
		
		prefixsum = generatePrefixSum(counts);
		for(int i = 0; i < n; i++)
		{			
			int maxrand = prefixsum.get(prefixsum.size() - 1);
			int rand = r.nextInt(maxrand);
			int index = binarysearch(prefixsum, rand) + 1;
			int count = counts.remove(index);			
			selectedcounts.add(count);
			String phrase = phrases.remove(index);
			selectedphrases.add(phrase);			
			updateprefixsumlist(prefixsum, index, count);
		}			
	}
	
	public static ArrayList<Integer> getEmptyList(int size)
	{
		ArrayList<Integer> arr = new ArrayList<Integer>();
		for(int i = 0; i < size; i++)
		{
			arr.add(0);
		}
		return arr;
	}
	
	public void updateLogs(ArrayList<String> phrases,
			ArrayList<Integer> counts,
			ArrayList<Integer> views,
			HashMap<String, Pair> logs)
	{
		int size = phrases.size();
		for(int i = 0; i < size; i++)
		{
			String phrase = phrases.get(i);
			int view = views.get(i);
			int count = counts.get(i);
			if(logs.containsKey(phrase))
			{
				Pair p = logs.get(phrase);
				p.add(view, count);
			}
			else
			{
				Pair p = new Pair(view,count);
				logs.put(phrase, p);
			}
		}
		
	}
	
	public void selectTopPhrases(ArrayList<String> phrases,
			ArrayList<Integer> counts,
			int percent,
			int maxdocs,
			ArrayList<String> selectedphrases,
			ArrayList<Integer> selectedcounts)
	{
		if(maxdocs > ((percent/100.0)*counts.size()))
		{
			maxdocs = (int) (Math.ceil((percent/100.0)*counts.size()));
		}
		
		for(int i = 0; i < maxdocs; i ++)
		{
			int count = counts.remove(0);
			String phrase = phrases.remove(0);
			
			selectedcounts.add(count);
			selectedphrases.add(phrase);
		}
	}
	
	public void removeBottomPhrases(ArrayList<String> phrases, 
			ArrayList<Integer> counts, 
			double probability)
	{
		int totalfreq = 0;
		
		for(int count : counts)
		{
			totalfreq += count;
		}
		
		int mincount = (int)(probability * totalfreq);
		
		int i = 0;
		for(; i < phrases.size(); i++)
		{
			int count = counts.get(i);
			
			if(mincount > count)
				break;
		}
		
		for(; i < phrases.size();)
		{
			counts.remove(i);
			phrases.remove(i);
		}
	}
	
	public void startsimulation(String srcdir, 
			String destfile, 
			int topdocspercent, 
			int maxtopdocs,
			int fixedviews,
			double removeprobability,
			double docsamplefactor) throws IOException
	{
		File dir = new File(srcdir);
		int mkdf = 0;
		HashMap<String, Pair> logs = new HashMap<String, LogSimulation.Pair>();
		
		long totalViews = 0;
		
		if(dir.isDirectory())
		{
			for(final File fileentry : dir.listFiles())
			{
				ArrayList<String> phrases  = new ArrayList<String>();
				ArrayList<Integer> counts = new ArrayList<Integer>();
				ArrayList<String> selectedphrases  = new ArrayList<String>();
				ArrayList<Integer> selectedcounts = new ArrayList<Integer>();
				
				ArrayList<String> selectedphrases2  = new ArrayList<String>();
				ArrayList<Integer> selectedcounts2 = new ArrayList<Integer>();
				
				
				loadFile(fileentry, phrases, counts);
				
				if(phrases.size() > 0) {
				  
				  selectTopPhrases(phrases,
						  counts, 
						  topdocspercent, 
						  maxtopdocs, 
						  selectedphrases, 
						  selectedcounts);
				  
				  ArrayList<Integer> viewdistribution = getEmptyList(selectedcounts.size());
				  
				  int fixedviews1 = (int)((fixedviews * selectedcounts.size() * 1.0)/(maxtopdocs))/2;
				  
				  if(selectedcounts.size() > 0) {
					    simulate(fixedviews1, viewdistribution, selectedcounts);
					    //updateLogs(selectedphrases, selectedcounts, viewdistribution, logs);
				  }
				  
				  removeBottomPhrases(phrases, counts, removeprobability);
				  
				  
				  int n = (int)(phrases.size() * docsamplefactor);
				  sample(counts, phrases, n, selectedcounts2, selectedphrases2);

				  ArrayList<Integer> viewdistribution2 = getEmptyList(selectedcounts2.size());
				  
				  int fixedviews2 = 2*selectedcounts2.size() < fixedviews/2 ? 2*selectedcounts2.size() : fixedviews/2;
				  
				  if(selectedcounts.size() > 0) {
					    simulate(fixedviews2, viewdistribution2, selectedcounts2);
					    //updateLogs(selectedphrases, selectedcounts, viewdistribution, logs);
				  }
				  
				  
				  String filename = fileentry.getName();
				  int fileid = linkdocid_map.get(filename);
				  int views = numviews.get(fileid);
				  
				  totalViews += views;

				  selectedphrases.addAll(selectedphrases2);
				  selectedcounts.addAll(selectedcounts2);
				  viewdistribution.addAll(viewdistribution2);
				  
				  if(selectedcounts.size() > 0) {
				    simulate(views, viewdistribution, selectedcounts);
				    updateLogs(selectedphrases, selectedcounts, viewdistribution, logs);
				  }
				}
				//mkdf++;
				if(mkdf == 100) {
				  break;
				}
			}
			
			File dest = new File(destfile);
			FileWriter fw = new FileWriter(dest.getAbsoluteFile(),false);
			BufferedWriter bw = new BufferedWriter(fw);
			Set<String> keys = logs.keySet();
			for(String key: keys)
			{
			  Pair p = logs.get(key);
			  //if(p.getNumviews() > 0) {
			    bw.write(key);
			    bw.write('\t');
			    bw.write(Integer.toString(p.getFrequency()));
			    bw.write('\t');
			    bw.write(Integer.toString(p.getNumviews()));
			    bw.newLine();
			  //}
			}
			System.out.println(totalViews);
			bw.close();
		}
		else
		{
			System.out.println("not a directory");
		}		
	}
	public static void main(String[] args) throws FileNotFoundException {
		
		LogSimulation simulation = new LogSimulation();
		try {
			simulation.loadNumviews("data/index/numviews.nv");
			simulation.loadLinkDocidMap("data/wiki/");
		} catch (ClassNotFoundException | IOException e1) {
		// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			simulation.startsimulation("D:/files/", 
					"D:/logs.txt",
					15,
					20,
					90,
					0.001,
					0.3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
