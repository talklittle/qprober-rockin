package coms6111.proj2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	private static HashMap<String, List<String>> hierarchy = new HashMap<String, List<String>>();
	private static HashMap<String, List<String>> associatedQueries = new HashMap<String, List<String>>();
	
	private static void constructClassificationTables(Node parent, Node categories) {
		NodeList eachCategory, children, queriesNodeList;
		Node cat, child, queryNode;
		ArrayList<String> subcategoriesList, queriesList;
		String categoryName;
		
		if (categories == null)
			return;
		eachCategory = categories.getChildNodes();
		// For each category, save its queries and subcategories
		// And then recurse on the subcategories
		subcategoriesList = new ArrayList<String>();
//		log.debug(eachCategory.getLength());
		for (int i = 0; i < eachCategory.getLength(); i++) {
			cat = eachCategory.item(i);
			if (!cat.getNodeName().equals("category"))
				continue;
//			log.debug("cat" + cat);
//			log.debug("getatt" + cat.getAttributes());
//			log.debug(cat.getAttributes().getNamedItem("name"));
			categoryName = cat.getAttributes().getNamedItem("name").getNodeValue();
			subcategoriesList.add(categoryName);
			children = cat.getChildNodes();
			// Children are "queries" and "categories"
			for (int j = 0; j < children.getLength(); j++) {
				child = children.item(j);
				if (child.getNodeName().equals("queries")) {
					queriesNodeList = child.getChildNodes();
					queriesList = new ArrayList<String>();
					for (int k = 0; k < queriesNodeList.getLength(); k++) {
						queryNode = queriesNodeList.item(k);
						if (!queryNode.getNodeName().equals("query"))
							continue;
						queriesList.add(queryNode.getTextContent());
					}
					associatedQueries.put(categoryName, queriesList);
				}
				else if (child.getNodeName().equals("categories")) {
					constructClassificationTables(cat, child);
				}
			}
		}
		if (parent != null)
			hierarchy.put(parent.getAttributes().getNamedItem("name").getNodeValue(), subcategoriesList);
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
					constructClassificationTables(null, xmlDoc.getFirstChild());
					
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
		for (String category : hierarchy.keySet()) {
			log.debug("CATEGORY: " + category);
			for (String query : associatedQueries.get(category)) {
				log.debug("[" + query + "]");
			}
			log.debug("--");
		}
	}
}
