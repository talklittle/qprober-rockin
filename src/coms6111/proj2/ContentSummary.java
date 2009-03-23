package coms6111.proj2;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContentSummary {
	private static Log log = LogFactory.getLog(ContentSummary.class);
	

	private String databaseName;
	private HashMap<String, Double> docFreq;
	
	public ContentSummary(String newDatabaseName) {
		databaseName = newDatabaseName;
		docFreq = new HashMap<String, Double>();
	}
	
	public void setDocFreq(String word, Double freq) {
		docFreq.put(word, freq);
	}
	
	public void setDocFreqs(List<String> words, List<Double> freqs) {
		if (words.size() != freqs.size()) {
			log.warn("ContentSummary: setDocFreqs: words.size() != freqs.size() ("
					+ words.size() + ", " + freqs.size() + ")");
			return;
		}
		for (int i = 0; i < words.size(); i++) {
			docFreq.put(words.get(i), freqs.get(i));
		}
	}
	
	public Double getDocFreq(String word) {
		return docFreq.get(word);
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Double> getDocFreqs() {
		return (HashMap<String, Double>) docFreq.clone();
	}
	
	public String getDatabaseName() {
		return databaseName;
	}
}
