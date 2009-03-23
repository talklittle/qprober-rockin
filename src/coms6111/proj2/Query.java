package coms6111.proj2;

import java.io.*;
import java.util.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Query {
	private static Log log = LogFactory.getLog(Query.class);
	
	public final String appid = "SeJQZ5fV34F7ohb4ONiSH9bbdWH9RtbodjvH_cN_BRj9QWEgfSFLW1h.Jkj0i52LT6I-";
	
	private String myQueryString;
	private List<String> myQueryStringList; // query string broken into individual terms
	
	public Query(String queryString) {
		this.setString(queryString);
	}
	
	public Resultset execute(int numResults, String sites) {
//		String request = "http://api.search.yahoo.com/WebSearchService/V1/webSearch";
		String request = "http://boss.yahooapis.com/ysearch/web/v1/";
	    HttpClient client = new HttpClient();

//	    PostMethod method = new PostMethod(request);
	    
	    // Construct query URL
	    request += myQueryString + "?";
	    request += "appid=" + appid + "&";
	    request += "count=" + numResults + "&";
	    request += "sites=" + sites + "&";
	    request += "format=xml";
	    
	    GetMethod method = new GetMethod(request);

//	    // Add POST parameters
//	    method.addParameter("appid", appid);
//	    method.addParameter("query", myQueryString);
//	    method.addParameter("results", ""+numResults);
//	    if (sites != null && !sites.isEmpty()) {
//	    	log.info("sites=" + sites);
//	    	method.addParameter("sites", sites);
//	    } else {
//	    	log.warn("No sites provided to Yahoo! query");
//	    }
//
	    log.debug(request);
	    // Send HTTP request
	    try {
	    	int statusCode = client.executeMethod(method);
	        if (statusCode != HttpStatus.SC_OK) {
	        	log.error("Method failed: " + method.getStatusLine());
	        	return null;
	        }
	    } catch (IOException e) {
	    	log.error(e.getLocalizedMessage());
	    	return null;
	    }

	    InputStream rstream = null;
	    
	    // Get the response body
	    try {
	    	rstream = method.getResponseBodyAsStream();
	    } catch (IOException e) {
	    	log.error(e.getLocalizedMessage());
	    	return null;
	    }

	    try {
	    	return new Resultset(rstream);
	    } catch (Exception e) {
	    	log.warn("Error creating Resultset from result stream");
	    	return null;
	    }
	}

	/**
	 * Return the queryString enclosed by square brackets.
	 */
	public String toString() {
		return "[" + myQueryString + "]";
	}
	
	/**
	 * Set the string of the query.
	 * @param queryString The String of the Query
	 */
	public void setString(String queryString) {
		StringTokenizer st = new StringTokenizer(queryString);
		myQueryStringList = new ArrayList<String>();
		while (st.hasMoreTokens())
			myQueryStringList.add(st.nextToken());
		myQueryString = queryString;
	}
	
	/**
	 * Return an Iterator<String> to inspect each query term separately.
	 * @return Iterator<String>
	 */
	public Iterator<String> iterator() {
		return myQueryStringList.iterator();
	}
	
	/**
	 * Return the number of terms in the query
	 * @return int of number of terms in the query
	 */
	public int length() {
		return myQueryStringList.size();
	}
}
