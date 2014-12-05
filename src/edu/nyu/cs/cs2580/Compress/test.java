package edu.nyu.cs.cs2580.Compress;

import java.util.BitSet;

public class test {
	public static void main(String[] args) {
		
		BitSet b = new BitSet();
		GammaCompression g = new GammaCompression();
		int c = g.compress(4, b, 0);
		
		System.out.println(c);
		Compression.printBits(b, c);
		
		BitSet b1 = new BitSet();
		int a = g.compress(10, b1, 0);
		System.out.println(a);
		Compression.printBits(b1, a);
		g.set(b, b1, c, c + a);
		
		System.out.println(c+a);
		Compression.printBits(b, c+a);
		c = c + a;
		
		g.set(b, b1, c, c + a);
		
		System.out.println(c+a);
		Compression.printBits(b, c+a);
	}
}
