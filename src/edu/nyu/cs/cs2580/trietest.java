package edu.nyu.cs.cs2580;

import java.util.Map;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;

public class trietest {
	public static void main(String[] args) {
		@SuppressWarnings("deprecation")
		PatriciaTrie<String, Double> t = new PatriciaTrie<String, Double>(StringKeyAnalyzer.INSTANCE);
		
		t.put("ronak", 100.0);
		t.put("ronald", 90.0);
		t.put("rat", 50.0);
		t.put("robert", 200.0);
		t.put("bat", 44.0);
		t.put("batman", 440.0);
	
		System.out.println(t.prefixMap("r"));
		System.out.println(t.containsKey("ronak"));
		//System.out.println(t.select("ron"));
	}
}
