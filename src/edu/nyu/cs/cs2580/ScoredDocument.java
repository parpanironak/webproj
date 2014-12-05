package edu.nyu.cs.cs2580;


import org.json.simple.*;
/**
 * Document with score.
 * 
 * @author fdiaz
 * @author congyu
 */
public class ScoredDocument implements Comparable<ScoredDocument> {
  private Document _doc;
  private double _score;
  public Document get_doc() {
    return _doc;
  }

  public ScoredDocument(Document doc, double score) {
    _doc = doc;
    _score = score;
  }

  public String asTextResult() {
    StringBuffer buf = new StringBuffer();
    buf.append(_doc._docid).append("\t");
    buf.append(_doc.getTitle()).append("\t");
    buf.append(String.format("%.7f", _score)).append("\t");
    buf.append(String.format("%.4f", _doc.getPageRank())).append("\t");
    buf.append(_doc.getNumViews());
    return buf.toString();
  }

  @SuppressWarnings("unchecked")
public JSONObject asJsonResult() {
	    JSONObject obj = new JSONObject();
	    obj.put("docid", new Integer(_doc._docid));
	    obj.put("title", _doc.getTitle());
	    obj.put("score", new Double(_score));
	    obj.put("pgrank", new Double(_doc.getPageRank()));
	    obj.put("nviews", new Double(_doc.getNumViews()));
	    return obj;
	  }
  /**
   * @CS2580: Student should implement {@code asHtmlResult} for final project.
   */
  public String asHtmlResult() {
    return "";
  }

  @Override
  public int compareTo(ScoredDocument o) {
    if (this._score == o._score) {
      return 0;
    }
    return (this._score > o._score) ? 1 : -1;
  }
}