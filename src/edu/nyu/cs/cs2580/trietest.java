package edu.nyu.cs.cs2580;

import java.util.Map;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;

public class trietest {
	public static void main(String[] args) {
		@SuppressWarnings("deprecation")
		PatriciaTrie<String, String> t = new PatriciaTrie<String, String>(StringKeyAnalyzer.INSTANCE);
		
		t.put("ronak", "ronak");
		t.put("ronald", "ronald");
		t.put("rat", "rat");
		t.put("robert", "robert");
		t.put("bat", "bat");
		t.put("batman", "batman");
		t.put("RC244", "RC244");
		t.put("RC2345", "RC2345");
	
		System.out.println(t.prefixMap("ro"));
		
	}
}
