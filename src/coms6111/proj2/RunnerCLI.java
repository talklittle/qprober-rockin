package coms6111.proj2;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RunnerCLI {
	
	protected static final Log log = LogFactory.getLog(RunnerCLI.class);
	
	private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private static HashMap<String, ClassificationNode> classificationNodes = new HashMap<String, ClassificationNode>();
	
	private static void constructClassificationTables(Node categories) {
		NodeList eachCategory, children, queriesNodeList;
		Node cat, child, queryNode;
		String categoryName;
		
		if (categories == null)
			return;
		eachCategory = categories.getChildNodes();
		// For each category, save its queries and subcategories
		// And then recurse on the subcategories
//		log.debug(eachCategory.getLength());
		for (int i = 0; i < eachCategory.getLength(); i++) {
			ClassificationNode cnParent;
			
			cat = eachCategory.item(i);
			if (!cat.getNodeName().equals("category"))
				continue;
			categoryName = cat.getAttributes().getNamedItem("name").getNodeValue();
			log.debug("categoryName " + categoryName);
			
			if (classificationNodes.containsKey(cat))
				cnParent = classificationNodes.get(categoryName);
			else {
				// Add category to table of all category nodes
				cnParent = new ClassificationNode(categoryName);
				classificationNodes.put(categoryName, cnParent);
			}
			
			// Children are "queries" and "categories"
			children = cat.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				child = children.item(j);
				if (child.getNodeName().equals("queries")) {
					queriesNodeList = child.getChildNodes();
					for (int k = 0; k < queriesNodeList.getLength(); k++) {
						queryNode = queriesNodeList.item(k);
						// Ignore whitespace and other nodes that shouldn't be there
						if (!queryNode.getNodeName().equals("query"))
							continue;
						String querySubcat = queryNode.getAttributes().getNamedItem("subcategory").getNodeValue();
						// Add the query to associated queries for parent, pointing to child
						ClassificationNode cnChild;
						if (classificationNodes.containsKey(querySubcat)) {
							cnChild = classificationNodes.get(querySubcat);
						} else {
							// Add subcategory to table of all category nodes
							cnChild = new ClassificationNode(querySubcat);
							classificationNodes.put(querySubcat, cnChild);
						}
						// Add a mapping from parent to this child based on query term
						cnParent.addQueryMapping(queryNode.getTextContent(), cnChild);
					}
				}
				else if (child.getNodeName().equals("categories")) {
					constructClassificationTables(child);
				}
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DocumentBuilder xmlReader;
		Document xmlDoc;
		
		if (args.length < 1) {
			log.error("Usage:");
			log.error("java RunnerCLI <defaultClassifications.xml>");
			System.exit(1);
		}
		File defaultClassifications = new File(args[0]);
		if (defaultClassifications.exists()) {
			log.debug(defaultClassifications.getAbsolutePath() + " exists");
			try {
				xmlReader = dbf.newDocumentBuilder();
				xmlDoc = xmlReader.parse(defaultClassifications);
				constructClassificationTables(xmlDoc.getFirstChild());
				
			} catch (Exception e) {
				log.error(null, e);
				System.exit(1);
			}
		} else {
			log.debug("Couldn't find file " + defaultClassifications.getAbsolutePath() + " exists");
		}

//		for (String category : classificationNodes.keySet()) {
//			log.debug("CATEGORY: " + category);
//			ClassificationNode cn = classificationNodes.get(category);
//			
//			for (String query : cn.getQueries()) {
//				log.debug("[" + query + "] -- " + cn.getChildByQuery(query).getName());
//			}
//			log.debug("--");
//		}
		
		Classify.cTable = classificationNodes;
		try {
			HashMap<String, String[]> hierarchy = new HashMap<String, String[]>();
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
			
			String category = Classify.ClassifyDatabase("java.sun.com","Root");
			log.info("Category:Root " + category);
		} catch (Exception e) {
			log.error("Error classifying database", e);
		}
		// FIXME
		System.exit(0);
		
		// XXX DEBUG
		TreeSet<String> categories = new TreeSet<String>();
		categories.add("Root");
		categories.add("Health");
		Set<String> queries = DocumentSampler.getQueriesToSample(categories, classificationNodes);
		URL site = null;
		try {
			site = new URL("http://diabetes.org");
		} catch (MalformedURLException e) {
			log.error("Bad url", e);
			System.exit(0);
		}
		Resultset rs = DocumentSampler.sample(site, queries);
		ContentSummary cs = ContentSummaryConstructor.construct("http://diabetes.org", rs);
		cs.printAlphabeticalOrder(System.out);
	}
}
