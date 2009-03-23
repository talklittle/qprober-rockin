package coms6111.proj2;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DocumentSampler {
	private static Log log = LogFactory.getLog(DocumentSampler.class);
	
	// Times to retry a query if it fails
	public static final int maxRetries = 5;
	public static final int topN = 4;
	public static final int queryDelayTimeMillis = 5000;
	
	/**
	 * Get a list of unique queries from the specified Collection of categories
	 * @param categories The categories you want to get queries from
	 * @param classificationNodes The Map of category names to representative nodes
	 * @return A Set of queries associated with the categories
	 */
	public static Set<String> getQueriesToSample(Collection<String> categories,
			Map<String, ClassificationNode> classificationNodes) {
		TreeSet<String> returnMe = new TreeSet<String>();
		
		for (String category : categories) {
			returnMe.addAll(classificationNodes.get(category).getQueries());
		}
		
		return returnMe; 
	}
	
	/**
	 * Sample a database using the list of queries.
	 * @param database URL of database to sample
	 * @param queries Set of query Strings
	 * @return Single large Resultset with unique results
	 */
	public static Resultset sample(URL database, Set<String> queries) {
		ArrayList<Resultset> resultsetList = new ArrayList<Resultset>();
		int retries;
		
		for (Iterator<String> it = queries.iterator(); it.hasNext(); ) {
			String qStr = it.next();
			Query q = new Query(qStr + " site="+database.toString());
			Resultset rs;
			
			retries = 0;
			do {
				rs = q.execute(topN);
				
				if (rs != null)
					break;
				try {
					Thread.sleep(queryDelayTimeMillis);
				} catch (InterruptedException e) {
					log.warn(e);
				}
				retries++;
			} while (rs == null && retries <= maxRetries);
		
			// Add the results of this query to the collection
			resultsetList.add(rs);
			
			// Be nice to the database servers and sleep between queries
			if (it.hasNext()) {
				try {
					Thread.sleep(queryDelayTimeMillis);
				} catch (InterruptedException e) {
					log.warn(e);
				}
			}	
		}
		
		return combineResultsets(resultsetList);
	}
	
	/**
	 * Create a new Resultset containing unique Results from
	 * the Resultsets in the list
	 * @param rsList A Collection of Resultsets
	 * @return A new Resultset containing unique Results
	 */
	public static Resultset combineResultsets(Collection<Resultset> rsList) {
		HashSet<Result> rsSet = new HashSet<Result>();
		for (Resultset rs : rsList) {
			for (Iterator<Result> it = rs.iterator(); it.hasNext(); ) {
				Result r = it.next();
				rsSet.add(r);
			}
		}
		return new Resultset(rsSet);
	}
	
	
	
}
