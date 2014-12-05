package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LinkDocIDMapGenerator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	private static Map<String,Integer> map = new HashMap<String, Integer>();
	
	public static void  generateMap(String src_dir_path) throws IOException
	{
		String corpusDirectoryString = src_dir_path;
	    System.out.println("Construct link_to_docid_map: " + corpusDirectoryString);
	    final File corpusDirectory = new File(corpusDirectoryString);
	    int x = 0;
	    for (final File fileEntry : corpusDirectory.listFiles()) {
	    	map.put(fileEntry.getName(),x++);	
	    }
	}
	
	public static Map<String, Integer> get()
	{
		return map;
	}
}
