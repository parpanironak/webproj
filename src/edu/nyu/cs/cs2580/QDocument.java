package edu.nyu.cs.cs2580;

import java.io.Serializable;
import java.util.ArrayList;

public class QDocument implements Serializable {
  /**
	 * 
	 */
  private static final long serialVersionUID = 8656316343742944006L;
  private int _docid;
  private int _numviews;
  private String content;
  private int frequency;
  private ArrayList<Integer> posting = new ArrayList<Integer>();
  private double idf = 0.0;
  
  public QDocument(int docId) {
    this._docid = docId;
  }
  
  public int getDocId() {
    return _docid;
  }
  public void setDocId(int _docid) {
    this._docid = _docid;
  }
  public int getNumViews() {
    return _numviews;
  }
  public void setNumViews(int _numviews) {
    this._numviews = _numviews;
  }
  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }
  public int getFrequency() {
    return frequency;
  }
  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }
  public ArrayList<Integer> getPosting() {
    return posting;
  }
  public void setPosting(ArrayList<Integer> posting) {
    this.posting = posting;
  }

public double getIdf() {
	return idf;
}

public void setIdf(double idf) {
	this.idf = idf;
}
}
