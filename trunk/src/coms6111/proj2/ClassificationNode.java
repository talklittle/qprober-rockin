package coms6111.proj2;

import java.util.HashMap;
import java.util.Set;

public class ClassificationNode {
	
	private String name;
	// Map each associated query to the subcategory that it points to
	private HashMap<String, ClassificationNode> queryMap = new HashMap<String, ClassificationNode>();
	
	
	public ClassificationNode(String newName) {
		name = newName;
	}
	
	public ClassificationNode getChildByQuery(String query) {
		return queryMap.get(query);
	}
	
	public String getName() {
		return name;
	}
	
	public Set<String> getQueries() {
		return queryMap.keySet();
	}
	
	public void addQueryMapping(String query, ClassificationNode child) {
		queryMap.put(query, child);
	}

	public void setName(String newName) {
		name = newName;
	}
	
	public void setQueryMap(HashMap<String, ClassificationNode> newQueryMap) {
		queryMap = newQueryMap;
	}
}
