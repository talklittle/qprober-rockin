package coms6111.proj2;

import java.io.*;
import java.net.*;


import javax.xml.parsers.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;

import java.util.Arrays;
import java.util.HashMap;

public class Classify {
	
	protected static final Log log = LogFactory.getLog(Classify.class);
	public static HashMap<String, ClassificationNode> cTable;
	public static String ClassifyDatabase(String database,String root)throws Exception{
		HashMap<String, String[]> hierarchy = new HashMap<String, String[]>();
		hierarchy.put("Root",new String[]{"Computers","Health","Sports"});
		hierarchy.put("Computers", new String[]{"Hardware","Programming"});
		hierarchy.put("Health",new String[]{"Fitness","Diseases"});
		hierarchy.put("Sports", new String[]{"Basketball","Soccer"});
		String website=database;
		String allCategory=root;
		int tec=100;
		double tes=0.6;
		String result="";
		for(String category:hierarchy.get(allCategory)){
			int coverage=GetECoverage(website,category);
			double specificity=GetESpecificity(website,category);
			if ((coverage>=tec)&&(specificity>=tes)){
				result="Root"+category+""+ClassifyDatabase(website,category);
				//for(String subCategory : hierarchy.get(category)){
					//int coverage1=GetECoverage(website,subCategory);
					//double specificity1=GetESpecificity(website,subCategory);
					//if((coverage1>=tec)&&(specificity1>=tes)){
						//result=category+""+subCategory;
					//}
					//else{
						//result=category;
					//}
					
					
				//}
				//result=result+" "+ClassifyDatabase(website,hierarchy.get(category));
			}
			}
		System.out.println(result);
		return result;
		}
	public static int GetECoverage(String database,String Category){
		String website=database;
		String categ=Category;
		int TotalMatchNum=0;
		ClassificationNode rootNode = cTable.get("Root");
		ClassificationNode computerNode = cTable.get("Computers");
		ClassificationNode healthNode = cTable.get("Health");
		ClassificationNode sportsNode = cTable.get("Sports");
		if(categ.equals("Computers")){
			for(String queryStr : rootNode.getQueries()){
				if(rootNode.getChildByQuery(queryStr).getName().equals("Computers")){
					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);         
					}
			}
		}
		else if (categ.equals("Health")){
			for(String queryStr : rootNode.getQueries()){
				if(rootNode.getChildByQuery(queryStr).getName().equals("Health")){
					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
				}
			}
		}
		else if (categ.equals("Sports")){
			for(String queryStr : rootNode.getQueries()){
				if(rootNode.getChildByQuery(queryStr).getName().equals("Sports")){
					TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
				}
			}
		}
		else if (categ.equals("Hardware")){
			for (String queryString : computerNode.getQueries()){
				if (computerNode.getChildByQuery(queryString).getName().equals("Hardware")){
					TotalMatchNum=TotalMatchNum+NumberMatch(queryString,website);
				}
			}
		}
		else if (categ.equals("Programming")){
			for (String queryString : computerNode.getQueries()){
				if (computerNode.getChildByQuery(queryString).getName().equals("Programming")){
					TotalMatchNum=TotalMatchNum+NumberMatch(queryString,website);
				}
			}
			
		}
		else if (categ.equals("Fitness")){
			for (String queryString : healthNode.getQueries()){
				if (healthNode.getChildByQuery(queryString).getName().equals("Fitness")){
					TotalMatchNum=TotalMatchNum+NumberMatch(queryString,website);
				}
			}
		}
		else if (categ.equals("Diseases")){
			for (String queryString : healthNode.getQueries()){
				if (healthNode.getChildByQuery(queryString).getName().equals("Diseases")){
					TotalMatchNum=TotalMatchNum+NumberMatch(queryString,website);
				}
			}
		}
		else if (categ.equals("Basketball")){
			for (String queryString : sportsNode.getQueries()){
				if (sportsNode.getChildByQuery(queryString).getName().equals("Basketball")){
					TotalMatchNum=TotalMatchNum+NumberMatch(queryString,website);
				}
			}
		}
		else if (categ.equals("Soccer")){
			for (String queryString : sportsNode.getQueries()){
				if (sportsNode.getChildByQuery(queryString).getName().equals("Soccer")){
					TotalMatchNum=TotalMatchNum+NumberMatch(queryString,website);
				}
			}
		}
		return TotalMatchNum;
		//return new int[][]{FirstMatch,SecondMatchCom,SecondMatchHeal,SecondMatchSport};
	
	
}
public static double GetESpecificity(String database,String Category){
	String website=database;
	String categ=Category;
	double specificity=0;
	int computerCoverage=GetECoverage(website,categ);
	int healthCoverage=GetECoverage(website,categ);
	int sportsCoverage=GetECoverage(website,categ);
	int hardwareCoverage=GetECoverage(website,categ);
	int programmingCoverage=GetECoverage(website,categ);
	int fitnessCoverage=GetECoverage(website,categ);
	int diseaseCoverage=GetECoverage(website,categ);
	int basketballCoverage=GetECoverage(website,categ);
	int soccerCoverage=GetECoverage(website,categ);
	if (categ.equals("Computers")){
		specificity=computerCoverage/(computerCoverage+healthCoverage+sportsCoverage);
		}
	else if (categ.equals("Health")){
		specificity=healthCoverage/(computerCoverage+healthCoverage+sportsCoverage);
	}
	else if (categ.equals("Sports")){
		specificity=sportsCoverage/(computerCoverage+healthCoverage+sportsCoverage);
	}
	else if (categ.equals("Hardware")){
		specificity=hardwareCoverage/(hardwareCoverage+programmingCoverage);
	}
	else if (categ.equals("Programming")){
		specificity=programmingCoverage/(hardwareCoverage+programmingCoverage);
	}
	else if (categ.equals("Basketball")){
		specificity=basketballCoverage/(basketballCoverage+soccerCoverage);
	}
	else if (categ.equals("Soccer")){
		specificity=soccerCoverage/(basketballCoverage+soccerCoverage);
	}
	else if (categ.equals("Fitness")){
		specificity=fitnessCoverage/(fitnessCoverage+diseaseCoverage);
	}
	else if (categ.equals("Diseases")){
		specificity=diseaseCoverage/(fitnessCoverage+diseaseCoverage);
	}
	return specificity;
}



public static int NumberMatch(String query, String database){
	try{
		int EachMatchNum;
		query = URLEncoder.encode(query, "UTF-8");
		URL url = new URL("http://boss.yahooapis.com/ysearch/web/v1/"+query+"?appid=SeJQZ5fV34F7ohb4ONiSH9bbdWH9RtbodjvH_cN_BRj9QWEgfSFLW1h.Jkj0i52LT6I-&result=10&format=xml&sites="+database);
		URLConnection connection = url.openConnection();
		InputStream in=new DataInputStream(connection.getInputStream());
		Document response = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        NodeList list=response.getElementsByTagName("resultset_web");
		String out=((Element)list.item(0)).getAttribute("totalhits");
		EachMatchNum=Integer.parseInt(out);
		//System.out.println(EachMatchNum);
		return EachMatchNum;
	}
	catch(Exception e) {
		System.err.println("There is a error!");
		e.printStackTrace();
		return 0;
	} 
	
	
	
}

}
