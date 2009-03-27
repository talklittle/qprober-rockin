package coms6111.proj2;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RunnerCLI {
	
	protected static final Log log = LogFactory.getLog(RunnerCLI.class);
	
	public static final String defaultClassificationsFile = "defaultClassifications.xml";
	
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
		String database;
		double tes, tec;
		
		if (args.length < 3) {
			log.error("Usage:");
			log.error("java RunnerCLI <database> <tes (0.6)> <tec (100)>");
			System.exit(1);
		}
		database = args[0];
		tes = Double.parseDouble(args[1]);
		tec = Double.parseDouble(args[2]);
		File defaultClassifications = new File(defaultClassificationsFile);
		if (defaultClassifications.exists()) {
			log.info("Loading default classifications from " + defaultClassifications.getAbsolutePath());
			try {
				xmlReader = dbf.newDocumentBuilder();
				xmlDoc = xmlReader.parse(defaultClassifications);
				constructClassificationTables(xmlDoc.getFirstChild());
				
			} catch (Exception e) {
				log.error(null, e);
				System.exit(1);
			}
		} else {
			log.error("Couldn't find file " + defaultClassifications.getAbsolutePath());
			System.exit(1);
		}

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
			
			String category = Classify.ClassifyDatabase(database,"Root", tec, tes);
			log.info("Database:" + database + "  Category: " + category);
		} catch (Exception e) {
			log.error("Error classifying database", e);
		}
		
	}
}
