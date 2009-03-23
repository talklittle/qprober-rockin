package coms6111.proj2;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DocumentSampler {
	private static Log log = LogFactory.getLog(DocumentSampler.class);
	
	// Times to retry a query if it fails
	public final int maxRetries = 5;
	public final int topN = 4;
	public final int queryDelayTimeMillis = 5000;
	
	// TODO
	public DocumentSampler() {
		
	}
	
	public Set<String> getQueriesToSample(List<String> categories,
			HashMap<String, List<String>> associatedQueries) {
		HashSet<String> returnMe = new HashSet<String>();
		
		for (String category : categories) {
			returnMe.addAll(associatedQueries.get(category));
		}
		
		return returnMe; 
	}
	
	public Resultset[] sample(URL database, Set<String> queries) {
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
		
		return resultsetList.toArray(new Resultset[0]);
	}
	
	
	
}
