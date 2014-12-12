package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import org.json.simple.JSONArray;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;

public class InstantQueryHandler implements HttpHandler {

  private QIndexerInvertedCompressed qindexer;
  private int numQueryResults = 10;

  public InstantQueryHandler(QIndexerInvertedCompressed qindexer) {
    this.qindexer = qindexer;
  }

  private void respondWithMsg(HttpExchange exchange, final String message)
      throws IOException {
    Headers responseHeaders = exchange.getResponseHeaders();
    responseHeaders.set("Access-Control-Allow-Origin", "*");
    responseHeaders.set("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, 0); // arbitrary number of bytes
    OutputStream responseBody = exchange.getResponseBody();
    responseBody.write(message.getBytes());
    responseBody.close();
  }

  private void constructTextOutput(final Vector<ScoredQueryDocument> docs,
      StringBuffer response) {
    for (ScoredQueryDocument doc : docs) {
      response.append(response.length() > 0 ? "\n" : "");
      response.append(doc.asTextResult());
    }
    response.append(response.length() > 0 ? "\n" : "");
  }

  private void constructJSONOutput(final Vector<ScoredQueryDocument> docs,
      StringBuffer response) {
    JSONArray arr = new JSONArray();
    for (ScoredQueryDocument doc : docs) 
    {
      arr.add(doc.asJsonResult());
    }
    response.append(arr.toString());
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    // TODO Auto-generated method stub
    String requestMethod = exchange.getRequestMethod();
    if (!requestMethod.equalsIgnoreCase("GET")) { // GET requests only.
      return;
    }

    // Print the user request header.
    Headers requestHeaders = exchange.getRequestHeaders();
    System.out.print("Incoming request: ");
    for (String key : requestHeaders.keySet()) {
      System.out.print(key + ":" + requestHeaders.get(key) + "; ");
    }
    System.out.println();

    // Validate the incoming request.
    String uriQuery = exchange.getRequestURI().getQuery();
    String uriPath = exchange.getRequestURI().getPath();
    if (uriPath == null || uriQuery == null) {
      respondWithMsg(exchange, "Something wrong with the URI!");
    }
    if (!uriPath.equals("/instant")) {
      respondWithMsg(exchange, "Only /instant is handled!");
    }
    System.out.println("Query: " + uriQuery);

    // Process the CGI arguments.
    CgiArguments cgiArgs = new CgiArguments(uriQuery);
    if (cgiArgs._query.isEmpty()) {
      respondWithMsg(exchange, "No query is given!");
    }

    // Create the ranker.
    QueryRanker ranker = new QueryRanker(SearchEngine.OPTIONS, cgiArgs, qindexer);

    // Processing the query.
    Query processedQuery = new QueryPhrase(cgiArgs._query);
    processedQuery.processQuery();

    Vector<ScoredQueryDocument> scoredQueryDocs = null;
    if(processedQuery._tokens.size() > 0)
      scoredQueryDocs = ranker.runQuery(processedQuery,
          numQueryResults);
    else
      scoredQueryDocs = new Vector<ScoredQueryDocument>();

    StringBuffer response = new StringBuffer();
    switch (cgiArgs._outputFormat) {
    case TEXT:
      constructJSONOutput(scoredQueryDocs, response);
      break;
    case HTML:
      // @CS2580: Plug in your HTML output
      break;
    default:
      // nothing
    }
    respondWithMsg(exchange, response.toString());
    System.out.println("Finished query: " + cgiArgs._query);
  }
}