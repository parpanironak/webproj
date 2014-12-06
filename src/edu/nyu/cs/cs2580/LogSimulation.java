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
				logs.get(phrase).add(view, count);
			}
			else
			{
				Pair p = new Pair(view,count);
				logs.put(phrase, p);
			}
		}
		
	}
	
	public void startsimulation(String srcdir, String destfile) throws IOException
	{
		File dir = new File(srcdir);
		
		
		HashMap<String, Pair> logs = new HashMap<String, LogSimulation.Pair>();
		
		if(dir.isDirectory())
		{
			for(final File fileentry : dir.listFiles())
			{
				ArrayList<String> phrases  = new ArrayList<String>();
				ArrayList<Integer> counts = new ArrayList<Integer>();
				ArrayList<String> selectedphrases  = new ArrayList<String>();
				ArrayList<Integer> selectedcounts = new ArrayList<Integer>();
				
				loadFile(fileentry, phrases, counts);
				///
				int n = (int)(phrases.size() * 0.5);
				sample(counts, phrases, n, selectedcounts, selectedphrases);
				ArrayList<Integer> viewdistribution = getEmptyList(selectedcounts.size());
				
				String filename = fileentry.getName();
				int fileid = linkdocid_map.get(filename);
				int views = numviews.get(fileid);
				
				simulate(views, viewdistribution, selectedcounts);
				
				updateLogs(selectedphrases, selectedcounts, viewdistribution, logs);
			}
		}
		else
		{
			System.out.println("not a directory");
		}
		
		File dest = new File(destfile);
		FileWriter fw = new FileWriter(dest.getAbsoluteFile(),true);
		BufferedWriter bw = new BufferedWriter(fw);
		Set<String> keys = logs.keySet();
		for(String key: keys)
		{
			Pair p = logs.get(key);
			bw.write(key);
			bw.write('\t');
			bw.write(p.getFrequency());
			bw.write('\t');
			bw.write(p.getNumviews());
			bw.newLine();		
		}
		bw.close();
		
		
	}
	public static void main(String[] args) throws FileNotFoundException {
		ArrayList<String> phrases  = new ArrayList<String>();
		ArrayList<Integer> counts = new ArrayList<Integer>();
		ArrayList<String> selectedphrases  = new ArrayList<String>();
		ArrayList<Integer> selectedcounts = new ArrayList<Integer>();
		
		loadFile(new File("D:/abc.txt"), phrases, counts);
		
		sample(counts, phrases, 5, selectedcounts, selectedphrases);
	}

}