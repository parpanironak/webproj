package edu.nyu.cs.cs2580;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class LogSimulation {

	
	private Map<String, Integer> linkdocid_map = null; 	
	private ArrayList<Integer> numviews = null;
	
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
	
	public static void simulate(int views, ArrayList<Integer> qnumviews, int randmax)
	{
		Random r = new Random();
		for(int i = 0 ; i < views; i++)
		{
			int index = r.nextInt(randmax) + 1;
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
	
	public static void loadFile(String src)
	{
		
	}
	
	public static void main(String[] args) {
		
		ArrayList<Integer> a = new ArrayList<Integer>();
		a.add(10);
		a.add(10);
		a.add(10);
		a.add(10);
		a.add(10);
		a.add(10);
		a.add(10);
		
		System.out.println(generatePrefixSum(a));
	}

}
