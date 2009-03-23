package coms6111.proj2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContentSummaryConstructor {
	private static Log log = LogFactory.getLog(ContentSummaryConstructor.class);
	
	public static ContentSummary construct(String databaseName, Resultset resultset) {
		ContentSummary returnMe;
		HashMap<String, Integer> docFreqs = new HashMap<String, Integer>();
		
		if (resultset == null) {
			log.warn("construct() called with null contentResultset");
			return null;
		}
		
		returnMe = new ContentSummary(databaseName);
		
		for (Iterator<Result> it = resultset.iterator(); it.hasNext(); ) {
			Result r = it.next();
			Set<String> words = LynxRunner.runLynx(r.url.toString());
			for (String word : words) {
				if (docFreqs.containsKey(word)) {
					docFreqs.put(word, docFreqs.get(word)+1);
				} else {
					docFreqs.put(word, 1);
				}
			}
		}
		
		returnMe.setDocFreqs(docFreqs);
		return returnMe;
	}

	
}
