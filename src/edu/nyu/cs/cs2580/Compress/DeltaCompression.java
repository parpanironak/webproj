package edu.nyu.cs.cs2580.Compress;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class DeltaCompression extends Compression implements Serializable{

	/**
   * 
   */
  private static final long serialVersionUID = 7057030826431982033L;

  @Override
	public int compressBatch(int[] arg, BitSet b) {
		// TODO Auto-generated method stub
		int bitpos = 0;
		for(int i = 0; i < arg.length; i++)
		{
			int k = arg[i];
			int kd = (int)Math.floor((Math.log(k)/log2));
			int kr = k - (1<<kd);
			int kdd = (int)Math.floor((Math.log(kd + 1)/log2));
			int kdr = kd - (1<<(kdd)) + 1;
			b.set(bitpos, bitpos + kdd, true);
			bitpos += kdd;
			b.set(bitpos, false);
			bitpos++;
			BitSet b2 = convert(kdr, kdd);
			set(b, b2, bitpos, bitpos + kdd);
			bitpos += kdd;
			b2 = convert(kr, kd);
			set(b, b2, bitpos, bitpos + kd);
			bitpos += kd;
//			System.out.println(kd);
//			System.out.println(kr);
//			System.out.println(kdd);
//			System.out.println(kdr);
		}
		return bitpos;
	}

	@Override
	public int compress(int arg, BitSet b, int pos) {
		// TODO Auto-generated method stub
		int bitpos = pos;
		int k = arg+1;
		int kd = (int)Math.floor((Math.log(k)/log2));
		int kr = k - (1<<kd);
		int kdd = (int)Math.floor((Math.log(kd + 1)/log2));
		int kdr = kd - (1<<(kdd)) + 1;
		b.set(bitpos, bitpos + kdd, true);
		bitpos += kdd;
		b.set(bitpos, false);
		bitpos++;
		BitSet b2 = convert(kdr, kdd);
		set(b, b2, bitpos, bitpos + kdd);
		bitpos += kdd;
		b2 = convert(kr, kd);
		set(b, b2, bitpos, bitpos + kd);
		bitpos += kd;
		return bitpos;
	}

	@Override
	public ArrayList<Integer> deCompressBatch(BitSet b, int count) {
		// TODO Auto-generated method stub
		ArrayList<Integer> postinglist = new ArrayList<Integer>();
		for(int i = 0; i < count;)
		{
			int unary = 0;
			while(b.get(i))
			{
				unary++;
				i++;
			}
			
			i++;
			if(unary == 0)
			{
				postinglist.add(1);
			}
			
			else
			{
				int val = 0;
				for(int k = 0; k < unary; k++)
				{
					val = val << 1;
					val = val | (b.get(i + k)?1:0);
					
				}
				
				i = i + unary;
				int kd = (1 << unary) + val - 1;
				
				val = 0;
				for(int k = 0; k < kd; k++)
				{
					val = val << 1;
					val = val | (b.get(i + k)?1:0);
				}
				postinglist.add((1 << kd) + val);
				i = i + kd ;
			}			
		}
		return postinglist;
	}

	@Override
	public int[] deCompress(BitSet b, int count, int pos) {
		// TODO Auto-generated method stub
		int unary = 0;
		int i = pos;
		if(i < 0)
			System.out.println("culprit");
		while(b.get(i))
		{
			unary++;
			i++;
		}
		
		i++;
		if(unary == 0)
		{
			if(i < count)
				return new int[]{1-1, i};
			else
				return new int[]{1-1, -1};
		}
		
		else
		{
			int val = 0;
			for(int k = 0; k < unary; k++)
			{
				val = val << 1;
				val = val | (b.get(i + k)?1:0);
				
			}
			
			i = i + unary;
			int kd = (1 << unary) + val - 1;
			
			val = 0;
			for(int k = 0; k < kd; k++)
			{
				val = val << 1;
				val = val | (b.get(i + k)?1:0);
			}
			int ans = ((1 << kd) + val);
			i = i + kd ;
			if(i < count)
				return new int[]{ans-1, i};
			else
				return new int[]{ans-1, -1};
		}
	}
	
  @Override
  public int compressBatch(List<Integer> arg, BitSet b,int pos) {
 // TODO Auto-generated method stub
    int bitpos = pos;
    for(int i = 0; i < arg.size(); i++)
    {
      int k = arg.get(i)+1;
      int kd = (int)Math.floor((Math.log(k)/log2));
      int kr = k - (1<<kd);
      int kdd = (int)Math.floor((Math.log(kd + 1)/log2));
      int kdr = kd - (1<<(kdd)) + 1;
      b.set(bitpos, bitpos + kdd, true);
      bitpos += kdd;
      b.set(bitpos, false);
      bitpos++;
      BitSet b2 = convert(kdr, kdd);
      set(b, b2, bitpos, bitpos + kdd);
      bitpos += kdd;
      b2 = convert(kr, kd);
      set(b, b2, bitpos, bitpos + kd);
      bitpos += kd;
//      System.out.println(kd);
//      System.out.println(kr);
//      System.out.println(kdd);
//      System.out.println(kdr);
    }
    return bitpos;
  }

}
