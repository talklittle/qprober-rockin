package coms6111.proj2;

public class ClassificationNode {
	
	private String name;
	private String[] childrenTypes, queries;
	
	
	public ClassificationNode(String newName) {
		name = newName;
	}
	
	public void setName(String newName) {
		name = newName;
	}
	
	public void setChildrenTypes(String[] newTypes) {
		childrenTypes = newTypes;
	}
	
	public void setQueries(String[] newQueries) {
		queries = newQueries;
	}
	
	public String getName() {
		return name;
	}
	
	public String[] getChildrenTypes() {
		return childrenTypes;
	}
	
	public String[] getQueries() {
		return queries;
	}
	
}
