package edu.nyu.cs.cs2580;


import org.json.simple.*;
/**
 * Document with score.
 * 
 * @author fdiaz
 * @author congyu
 */
public class ScoredQueryDocument implements Comparable<ScoredQueryDocument> {
  private QDocument _doc;
  private double _score;
  public QDocument get_doc() {
    return _doc;
  }

  public ScoredQueryDocument(QDocument doc, double score) {
    _doc = doc;
    _score = score;
  }

  public String asTextResult() {
    StringBuffer buf = new StringBuffer();
    buf.append(_doc.getDocId()).append("\t");
    buf.append(_doc.getContent()).append("\t");
    buf.append(String.format("%.7f", _score)).append("\t");
    buf.append(_doc.getNumViews());
    return buf.toString();
  }

  @SuppressWarnings("unchecked")
public JSONObject asJsonResult() {
      JSONObject obj = new JSONObject();
      obj.put("docid", new Integer(_doc.getDocId()));
      obj.put("title", _doc.getContent());
      obj.put("frequency", _doc.getFrequency());
      obj.put("score", new Double(_score));
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
  public int compareTo(ScoredQueryDocument o) {
    if (this._score == o._score) {
      return 0;
    }
    return (this._score > o._score) ? 1 : -1;
  }
}