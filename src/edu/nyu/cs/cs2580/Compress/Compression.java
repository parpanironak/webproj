package edu.nyu.cs.cs2580.Compress;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

abstract public class Compression {

  protected static final double log2 = Math.log(2);

  public static BitSet convert(int value, int count) {
    BitSet bits = new BitSet(count);
    int index = count-1;
    for(int i = 0; i < count; i++){
      if ((value & 1) != 0) {
        bits.set(index);
      }
      --index;
      value = value >>> 1;
    }
    return bits;
  }

  //Set bits in BitSet b to b2 from index start to index end
  public void set(BitSet b, BitSet b2, int start, int end) {
    for(int i = 0 ; i < end - start; i++ )
    {
      if(b2.get(i))
        b.set(i+start);
    }
  }

  public static void printBits(BitSet b, int bitpos) {
    for(int i = 0; i < bitpos; i++)
    {
      int x = b.get(i)?1:0;
      System.out.print(x);
    }
    System.out.println();
  }

  abstract public int compressBatch(int arg[], BitSet b);
  abstract public int compressBatch(List<Integer> arg, BitSet b,int pos);
  abstract public int compress(int arg, BitSet b, int pos);
  abstract public ArrayList<Integer> deCompressBatch(BitSet b, int count);
  abstract public int[] deCompress(BitSet b, int count, int pos);
}