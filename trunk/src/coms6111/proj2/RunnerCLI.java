package coms6111.proj2;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
		
		if (args.length >= 1) {
			File defaultClassifications = new File(args[0]);
			if (defaultClassifications.exists()) {
				log.debug(defaultClassifications.getAbsolutePath() + " exists");
				try {
					xmlReader = dbf.newDocumentBuilder();
					xmlDoc = xmlReader.parse(defaultClassifications);
					constructClassificationTables(xmlDoc.getFirstChild());
					
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				log.debug("Couldn't find file " + defaultClassifications.getAbsolutePath() + " exists");
			}
		}
		for (String category : classificationNodes.keySet()) {
			log.debug("CATEGORY: " + category);
			ClassificationNode cn = classificationNodes.get(category);
			
			for (String query : cn.getQueries()) {
				log.debug("[" + query + "] -- " + cn.getChildByQuery(query).getName());
			}
			log.debug("--");
		}
	}
}
