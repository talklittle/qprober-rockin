package coms6111.proj2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class Classify {
	
	protected static final Log log = LogFactory.getLog(Classify.class);
	public static final int topN = 4; // used for ContentSummary
	
	public static HashMap<String, ClassificationNode> cTable;

	public static List<Resultset> docSamples = null;

	/**
	 * Call this function before ClassifyDatabase
	 */
	public static void init() {
		// initialize the document sampler results
		docSamples = new ArrayList<Resultset>();
	}
	
	public static String ClassifyDatabase(String database,String root, double tec, double tes) throws Exception{
		HashMap<String, String[]> hierarchy = new HashMap<String, String[]>();
		HashMap<String, String> reverseHierarchy = new HashMap<String, String>(); // parent list
		HashMap<String, Double> eCoverageTable = new HashMap<String, Double>();
		HashMap<String, Double> eSpecificityTable = new HashMap<String, Double>();
		HashMap<String, Resultset> resultsByCategory = new HashMap<String, Resultset>();
		
		hierarchy.put("Root",new String[]{"Computers","Health","Sports"});
		hierarchy.put("Computers", new String[]{"Hardware","Programming"});
		hierarchy.put("Health",new String[]{"Fitness","Diseases"});
		hierarchy.put("Sports", new String[]{"Basketball","Soccer"});
		hierarchy.put("Hardware",new String[0]);
		hierarchy.put("Programming",new String[0]);
		hierarchy.put("Fitness",new String[0]);
		hierarchy.put("Diseases",new String[0]);
		hierarchy.put("Basketball",new String[0]);
		hierarchy.put("Soccer",new String[0]);
		
		// Reverse hierarchy (parent list) is used in specificity calculation
		reverseHierarchy.put("Root", null);
		reverseHierarchy.put("Computers", "Root");
		reverseHierarchy.put("Health", "Root");
		reverseHierarchy.put("Sports", "Root");
		reverseHierarchy.put("Hardware", "Computers");
		reverseHierarchy.put("Programming", "Computers");
		reverseHierarchy.put("Fitness", "Health");
		reverseHierarchy.put("Diseases", "Health");
		reverseHierarchy.put("Basketball", "Sports");
		reverseHierarchy.put("Soccer", "Sports");
		
//		String allCategory;
		// Categories are stored in hierarchy table as name of final path component
		// e.g., "Basketball" instead of "Root/Sports/Basketball"
//		if (root.indexOf('/') != -1) {
//			allCategory = root.substring(root.lastIndexOf('/')+1);
//		} else {
//			allCategory = root;
//		}
		
		// Create a results and ECoverage table
		for (String category : hierarchy.keySet()) {
			Resultset rs = createResultsetForCategory(category, database);
			resultsByCategory.put(category, rs);
			eCoverageTable.put(category, rs.getTotalHits());
		}
		// Create ESpecificity table using coverage values
		for (String category : hierarchy.keySet()) {
			eSpecificityTable.put(category, GetESpecificity(database, category, hierarchy, reverseHierarchy, eCoverageTable));
		}
		
		String classifications = classifyDatabaseHelper(database, "Root", hierarchy, tec, tes, eCoverageTable, eSpecificityTable);
		
		// Create ContentSummary
		TreeSet<String> allMatchingNodes = new TreeSet<String>();
		StringTokenizer st_space = new StringTokenizer(classifications);
		// First use the results of classifyDatabaseHelper to figure out which nodes we want
		while (st_space.hasMoreTokens()) {
			String s1 = st_space.nextToken();
			StringTokenizer st_slash = new StringTokenizer(s1, "/");
			while (st_slash.hasMoreTokens()) {
				String s = st_slash.nextToken();
				allMatchingNodes.add(s);
			}
		}
		for (String nodeToSummarize : allMatchingNodes) {
			// Skip leaf nodes, since no additional queries were performed for them
			if (hierarchy.get(nodeToSummarize).length == 0)
				continue;
			Set<String> branch = getBranchToSummarize(nodeToSummarize, allMatchingNodes, hierarchy);
			HashSet<Resultset> rsSet = new HashSet<Resultset>();
			for (String s : branch) {
				rsSet.add(resultsByCategory.get(s));
			}
			Resultset rsToSummarize = DocumentSampler.combineResultsets(rsSet);
			File outFile = new File(nodeToSummarize+"-"+database+".txt");
			FileOutputStream fos = new FileOutputStream(outFile);
			ContentSummary summary = ContentSummaryConstructor.construct(database, rsToSummarize);
			summary.printAlphabeticalOrder(fos);
		}
	
		return classifications;
	}
	
	private static String classifyDatabaseHelper(String website, String root, HashMap<String, String[]> hierarchy,
			double tec, double tes, HashMap<String, Double> eCoverageTable, HashMap<String, Double> eSpecificityTable) {
		String result="";
		
		// Check each child's coverage and specificity
		for(String category : hierarchy.get(root)){
//			double coverage=GetECoverage(website,category, rs);
			double coverage = eCoverageTable.get(category);
			double specificity = eSpecificityTable.get(category);

			log.debug("category " + category + " coverage " + coverage + " specificity " + specificity);
			if ((coverage>=tec)&&(specificity>=tes)){
				log.debug("coverage and specificity above threshold for " + category);
//				String subclassify = ClassifyDatabase(website, category);
				String subMatches = classifyDatabaseHelper(website, category,
						hierarchy, tec, tes, eCoverageTable, eSpecificityTable);
				if (subMatches.length() > 0) {
					// At least one of category's children matched, so they override this category in result list
					StringTokenizer st = new StringTokenizer(subMatches);
					while (st.hasMoreTokens()) {
						String subMatch = st.nextToken();
						result += root+"/"+subMatch + " ";
					}
				} else {
					// None of this category's children matched, so category is the lowest level
					result += root+"/"+category + " ";
				}
			} else {
//				log.debug("coverage and specificity below threshold for " + root+"/"+category);
			}
		}
		if (result.length() > 0)
			log.debug("ClassifyDatabase(" + website + ", " + root + ") = " + result);
		else
			log.debug("ClassifyDatabase(" + website + ", " + root + ") = NIL");
		return result.trim();
	}
	
	/**
	 * Combine a top node with all its descendants who matched in QProber. Return this new Resultset.
	 * @param topNode The node to start with. Doesn't have to be "Root"
	 * @param allMatchingNodes Set of all nodes that matched in QProber
	 * @param hierarchy
	 * @return
	 */
	private static Set<String> getBranchToSummarize(String topNode, Set<String> allMatchingNodes, HashMap<String, String[]> hierarchy) {
		HashSet<String> branchNodes = new HashSet<String>();
		for (String child : hierarchy.get(topNode)) {
			if (allMatchingNodes.contains(child)) {
				branchNodes.add(child);
				branchNodes.addAll(getBranchToSummarize(child, allMatchingNodes, hierarchy));
			}
		}
		return branchNodes;
	}

	public static Resultset createResultsetForCategory(String Category, String database){
		String categ=Category;
//		double TotalMatchNum=0;
		ClassificationNode rootNode = cTable.get("Root");
		ClassificationNode computerNode = cTable.get("Computers");
		ClassificationNode healthNode = cTable.get("Health");
		ClassificationNode sportsNode = cTable.get("Sports");
		HashSet<Resultset> rsSet = new HashSet<Resultset>();
		
		if (categ.equals("Computers")){
			for(String queryStr : rootNode.getQueries()){
				if(rootNode.getChildByQuery(queryStr).getName().equals("Computers")){
					Query q = new Query(queryStr);
					Resultset rs = q.execute(topN, database);
					if (rs == null) {
						log.warn("createResultsetsForCategory(): Resultset was null for query ["+queryStr+"]");
						continue;
					}
					rsSet.add(rs);
//					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);         
				}
			}
		}
		else if (categ.equals("Health")){
			for(String queryStr : rootNode.getQueries()){
				if(rootNode.getChildByQuery(queryStr).getName().equals("Health")){
					Query q = new Query(queryStr);
					Resultset rs = q.execute(topN, database);
					if (rs == null) {
						log.warn("createResultsetsForCategory(): Resultset was null for query ["+queryStr+"]");
						continue;
					}
					rsSet.add(rs);
//					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
				}
			}
		}
		else if (categ.equals("Sports")){
			for(String queryStr : rootNode.getQueries()){
				if(rootNode.getChildByQuery(queryStr).getName().equals("Sports")){
					Query q = new Query(queryStr);
					Resultset rs = q.execute(topN, database);
					if (rs == null) {
						log.warn("createResultsetsForCategory(): Resultset was null for query ["+queryStr+"]");
						continue;
					}
					rsSet.add(rs);
//					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
				}
			}
		}
		else if (categ.equals("Hardware")){
			for (String queryString : computerNode.getQueries()){
				if (computerNode.getChildByQuery(queryString).getName().equals("Hardware")){
					Query q = new Query(queryString);
					Resultset rs = q.execute(topN, database);
					if (rs == null) {
						log.warn("createResultsetsForCategory(): Resultset was null for query ["+queryString+"]");
						continue;
					}
					rsSet.add(rs);
//					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
				}
			}
		}
		else if (categ.equals("Programming")){
			for (String queryString : computerNode.getQueries()){
				if (computerNode.getChildByQuery(queryString).getName().equals("Programming")){
					Query q = new Query(queryString);
					Resultset rs = q.execute(topN, database);
					if (rs == null) {
						log.warn("createResultsetsForCategory(): Resultset was null for query ["+queryString+"]");
						continue;
					}
					rsSet.add(rs);
//					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
				}
			}
			
		}
		else if (categ.equals("Fitness")){
			for (String queryString : healthNode.getQueries()){
				if (healthNode.getChildByQuery(queryString).getName().equals("Fitness")){
					Query q = new Query(queryString);
					Resultset rs = q.execute(topN, database);
					if (rs == null) {
						log.warn("createResultsetsForCategory(): Resultset was null for query ["+queryString+"]");
						continue;
					}
					rsSet.add(rs);
//					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
				}
			}
		}
		else if (categ.equals("Diseases")){
			for (String queryString : healthNode.getQueries()){
				if (healthNode.getChildByQuery(queryString).getName().equals("Diseases")){
					Query q = new Query(queryString);
					Resultset rs = q.execute(topN, database);
					if (rs == null) {
						log.warn("createResultsetsForCategory(): Resultset was null for query ["+queryString+"]");
						continue;
					}
					rsSet.add(rs);
//					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
				}
			}
		}
		else if (categ.equals("Basketball")){
			for (String queryString : sportsNode.getQueries()){
				if (sportsNode.getChildByQuery(queryString).getName().equals("Basketball")){
					Query q = new Query(queryString);
					Resultset rs = q.execute(topN, database);
					if (rs == null) {
						log.warn("createResultsetsForCategory(): Resultset was null for query ["+queryString+"]");
						continue;
					}
					rsSet.add(rs);
//					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
				}
			}
		}
		else if (categ.equals("Soccer")){
			for (String queryString : sportsNode.getQueries()){
				if (sportsNode.getChildByQuery(queryString).getName().equals("Soccer")){
					Query q = new Query(queryString);
					Resultset rs = q.execute(topN, database);
					if (rs == null) {
						log.warn("createResultsetsForCategory(): Resultset was null for query ["+queryString+"]");
						continue;
					}
					rsSet.add(rs);
//					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
				}
			}
		}
		
//		return TotalMatchNum;
		return DocumentSampler.combineResultsets(rsSet);
}

public static double GetESpecificity(String database,String categ,
		HashMap<String, String[]> hierarchy, HashMap<String, String> reverseHierarchy,
		HashMap<String, Double> eCoverageTable){
//	double specificity=0.0;
//	double computerCoverage=GetECoverage(website,"Computers");
//	double healthCoverage=GetECoverage(website,"Health");
//	double sportsCoverage=GetECoverage(website,"Sports");
//	double hardwareCoverage=GetECoverage(website,"Hardware");
//	double programmingCoverage=GetECoverage(website,"Programming");
//	double fitnessCoverage=GetECoverage(website,"Fitness");
//	double diseaseCoverage=GetECoverage(website,"Diseases");
//	double basketballCoverage=GetECoverage(website,"Basketball");
//	double soccerCoverage=GetECoverage(website,"Soccer");
	
//	if (categ.equals("Computers")){
//		specificity=computerCoverage/(computerCoverage+healthCoverage+sportsCoverage);
//		}
//	else if (categ.equals("Health")){
//		specificity=healthCoverage/(computerCoverage+healthCoverage+sportsCoverage);
//	}
//	else if (categ.equals("Sports")){
//		specificity=sportsCoverage/(computerCoverage+healthCoverage+sportsCoverage);
//	}
//	else if (categ.equals("Hardware")){
//		specificity=hardwareCoverage/(hardwareCoverage+programmingCoverage);
//	}
//	else if (categ.equals("Programming")){
//		specificity=programmingCoverage/(hardwareCoverage+programmingCoverage);
//	}
//	else if (categ.equals("Basketball")){
//		specificity=basketballCoverage/(basketballCoverage+soccerCoverage);
//	}
//	else if (categ.equals("Soccer")){
//		specificity=soccerCoverage/(basketballCoverage+soccerCoverage);
//	}
//	else if (categ.equals("Fitness")){
//		specificity=fitnessCoverage/(fitnessCoverage+diseaseCoverage);
//	}
//	else if (categ.equals("Diseases")){
//		specificity=diseaseCoverage/(fitnessCoverage+diseaseCoverage);
//	}
	
	if (categ.equals("Root"))
		return 1.0;
	
	String parent = reverseHierarchy.get(categ);
	double denominator = 0.0;
	for (String sibling : hierarchy.get(parent)) {
		denominator += eCoverageTable.get(sibling);
	}
	
	double numerator = GetESpecificity(database, parent, hierarchy, reverseHierarchy, eCoverageTable) * eCoverageTable.get(categ);
	
	return numerator / denominator;
}



	// TODO get rid of this function??? Instead, pass the rs around outside, so we can reuse for ContentSummary
	public static double NumberMatch(String query, String database){
		try{
			Query q = new Query(query);
			
	//		query = URLEncoder.encode(query, "UTF-8");
	//		URL url = new URL("http://boss.yahooapis.com/ysearch/web/v1/"+query+"?appid=SeJQZ5fV34F7ohb4ONiSH9bbdWH9RtbodjvH_cN_BRj9QWEgfSFLW1h.Jkj0i52LT6I-&result=10&format=xml&sites="+database);
	//		URLConnection connection = url.openConnection();
	//		InputStream in=new DataInputStream(connection.getInputStream());
	//		Document response = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
	//        NodeList list=response.getElementsByTagName("resultset_web");
	//		String out=((Element)list.item(0)).getAttribute("totalhits");
	//		EachMatchNum=Integer.parseInt(out);
			//System.out.println(EachMatchNum);
			
			// Takes the place of DocumentSampler...
			Resultset rs = q.execute(4, database);
			if (rs == null) {
				log.warn("NumberMatch(): Resultset was null for query ["+query+"]");
				return 0;
			}
	
			docSamples.add(rs);
			
			return rs.getTotalHits();
		}
		catch(Exception e) {
			System.err.println("There is a error!");
			e.printStackTrace();
			return 0;
		} 
	}
}
