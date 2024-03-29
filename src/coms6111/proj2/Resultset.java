package coms6111.proj2;

import java.io.*;
import java.util.*;

import javax.xml.xpath.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;

/**
 * A Resultset either from search engine or manually created.
 */
public class Resultset {
	private static Log log = LogFactory.getLog(Resultset.class);
	
	private List<Result> myResults;
	private int resultSize;
	private double totalHits = -1;
	
	public Resultset(Collection<Result> results) {
		myResults = new ArrayList<Result>(results);
		if (results != null)
			resultSize = results.size();
		else
			resultSize = 0;
	}
	
	public Resultset(Collection<Result> results, double totalHitsInDb) {
		myResults = new ArrayList<Result>(results);
		if (results != null)
			resultSize = results.size();
		else
			resultSize = 0;
		totalHits = totalHitsInDb;
	}
	
	/**
	 * Constructor using input stream returned from HttpClient
	 */
	public Resultset(InputStream rstream) throws Exception {
		Document response = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(rstream);
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath=factory.newXPath();

		totalHits = (Double)xPath.evaluate("/ysearchresponse/resultset_web/@totalhits", response, XPathConstants.NUMBER);
		
		//Get all search Result nodes
		NodeList nodes = (NodeList)xPath.evaluate("/ysearchresponse/resultset_web/result", response, XPathConstants.NODESET);
		int nodeCount = nodes.getLength();
        
		myResults = new ArrayList<Result>();
		resultSize = 0;
		//iterate over search Result nodes
		for (int i = 0; i < nodeCount; i++) {
			//Get each xpath expression as a string
			String title = (String)xPath.evaluate("title", nodes.item(i), XPathConstants.STRING);
			String summary = (String)xPath.evaluate("abstract", nodes.item(i), XPathConstants.STRING);
			String url = (String)xPath.evaluate("url", nodes.item(i), XPathConstants.STRING);
			//print out the Title, Summary, and URL for each search result
//			log.info("-- Result " + (i+1));
//			log.info("Title: " + title);
//			log.info("Summary: " + summary);
			log.info("URL: " + url);
//			log.info("--");
			
			myResults.add(new Result(title, summary, url));
			resultSize++;
		}
	}
	
	/**
	 * Get an in-order Iterator for the Resultset.
	 * @return An in-order Iterator for the Resultset.
	 */
	public Iterator<Result> iterator() {
		return myResults.iterator();
	}
	/**
	 * Get an in-order ListIterator for the Resultset.
	 * @return An in-order ListIterator for the Resultset.
	 */
	public ListIterator<Result> listIterator() {
		return myResults.listIterator();
	}
	
	/**
	 * Return the number of results.
	 * @return The number of results contained in this Resultset.
	 */
	public int getSize() {
		return resultSize;
	}
	
	/**
	 * Return the number of total hits from the query.
	 * This is greater than or equal to resultSize from getSize()
	 * @return number of total hits from the query
	 */
	public double getTotalHits() {
		return totalHits;
	}
}
