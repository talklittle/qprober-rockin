package coms6111.proj2;

import java.io.*;
import java.net.*;


import javax.xml.parsers.*;

import org.w3c.dom.*;

import java.util.Arrays;
import java.util.HashMap;

public class Classify {
	
	public static HashMap<String, ClassificationNode> cTable;
	
	public static String ClassifyDatabase(String database)throws Exception{
		String website=database;
		int tec=100;
		double tes=0.6;
		String result="";
		if ((GetECoverage(website)[0][0]>=tec)&&(GetESpecificity(website)[0][0]>=tes)){
			if((GetECoverage(website)[1][0]>=tec)&&(GetESpecificity(website)[1][0]>=tes)){
				result="Root/Computers/Hardware";
			}
			else if((GetECoverage(website)[1][1]>=tec)&&(GetESpecificity(website)[1][1]>=tes)){
				result="Root/Computers/Programming";
			}
			else
				result="Root/Computers";
				
		}
		else if ((GetECoverage(website)[0][1]>=tec)&&(GetESpecificity(website)[0][1]>=tes)){
			if((GetECoverage(website)[2][0]>=tec)&&(GetESpecificity(website)[2][0]>=tes)){
				result="Root/Health/Fitness";
			}
			else if((GetECoverage(website)[2][1]>=tec)&&(GetESpecificity(website)[2][1]>=tes)){
				result="Root/Health/Diseases";
			}
			else
				result="Root/Health";
		}
		else if ((GetECoverage(website)[0][2]>=tec)&&(GetESpecificity(website)[0][2]>=tes)){
			if((GetECoverage(website)[3][0]>=tec)&&(GetESpecificity(website)[3][0]>=tes)){
				result="Root/Sports/Basketball";
			}
			else if((GetECoverage(website)[3][1]>=tec)&&(GetESpecificity(website)[3][1]>=tes)){
				result="Root/Sports/Soccer";
			}
			else
				result="Root/Sports";
		}
		else
			result="Root";
		return result;
		
		
		
		
		
		
	}
	public static int[][] GetECoverage(String database){
		String website=database;
		int FirstMatch[]= new int [3];
		int SecondMatchCom[]= new int [2];
		int SecondMatchHeal[]= new int [2];
		int SecondMatchSport[] = new int [2];
		Arrays.fill(FirstMatch,0);
		Arrays.fill(SecondMatchCom,0);
		Arrays.fill(SecondMatchHeal, 0);
		Arrays.fill(SecondMatchSport,0);
		int TotalMatchNum=0;
		int TotalList=0;
		ClassificationNode rootNode = cTable.get("Root");
		ClassificationNode computerNode = cTable.get("Computers");
		ClassificationNode healthNode = cTable.get("Health");
		ClassificationNode sportsNode = cTable.get("Sports");
	for (String queryStr : rootNode.getQueries()) {
		if(rootNode.getChildByQuery(queryStr).getName().equals("Computers")){
			TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
			FirstMatch[0]=TotalMatchNum;
			for (String queryString : computerNode.getQueries()){
				if (computerNode.getChildByQuery(queryString).getName().equals("Hardware")){
					TotalList=TotalList+NumberMatch(queryString,website);
					SecondMatchCom[0]= TotalList;
				}
				else if (computerNode.getChildByQuery(queryString).getName().equals("Programming")){
					TotalList=TotalList+NumberMatch(queryString,website);
					SecondMatchCom[1]= TotalList;
				}
			}
			}
		else if (rootNode.getChildByQuery(queryStr).getName().equals("Health")){
			TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
			FirstMatch[1]=TotalMatchNum;
			for (String queryString : healthNode.getQueries()){
				if (healthNode.getChildByQuery(queryString).getName().equals("Fitness")){
					TotalList=TotalList+NumberMatch(queryString,website);
					SecondMatchHeal[0]= TotalList;
					
				}
				else if (healthNode.getChildByQuery(queryString).getName().equals("Diseases")){
					TotalList=TotalList+NumberMatch(queryString,website);
					SecondMatchHeal[1]= TotalList;
					
				}
			}
		}
		else if (rootNode.getChildByQuery(queryStr).getName().equals("Sports")){
			TotalMatchNum=TotalMatchNum+NumberMatch(queryStr,website);
			FirstMatch[2]=TotalMatchNum;
			for (String queryString : sportsNode.getQueries()){
				if (sportsNode.getChildByQuery(queryString).getName().equals("Hardware")){
					TotalList=TotalList+NumberMatch(queryString,website);
					SecondMatchSport[0]= TotalList;
				}
				else if (healthNode.getChildByQuery(queryString).getName().equals("Programming")){
					TotalList=TotalList+NumberMatch(queryString,website);
					SecondMatchSport[1]= TotalList;
				
				}
			}
	
		}
		}
	return new int[][]{FirstMatch,SecondMatchCom,SecondMatchHeal,SecondMatchSport};
	
	
}
public static double[][] GetESpecificity(String database){
	String website=database;
	double Specificity []= new double [3];
	double SpecificityCom []= new double [2];
	double SpecificityHeal []= new double [2];
	double SpecificitySport []= new double [2];
	Specificity [0]= GetECoverage(website)[0][0]/GetECoverage(website)[0][0]+GetECoverage(website)[0][1]+GetECoverage(website)[0][2];
	Specificity [1]= GetECoverage(website)[0][1]/GetECoverage(website)[0][0]+GetECoverage(website)[0][1]+GetECoverage(website)[0][2];
	Specificity [2]= GetECoverage(website)[0][2]/GetECoverage(website)[0][0]+GetECoverage(website)[0][1]+GetECoverage(website)[0][2];
	SpecificityCom [0]=GetECoverage(website)[1][0]/GetECoverage(website)[1][0]+GetECoverage(website)[1][1];
	SpecificityCom [1]=GetECoverage(website)[1][1]/GetECoverage(website)[1][0]+GetECoverage(website)[1][1];
	SpecificityHeal [0]=GetECoverage(website)[2][0]/GetECoverage(website)[2][0]+GetECoverage(website)[2][1];
	SpecificityHeal [1]= GetECoverage(website)[2][1]/GetECoverage(website)[2][0]+GetECoverage(website)[2][1];
	SpecificitySport [0]=GetECoverage(website)[3][0]/GetECoverage(website)[3][0]+GetECoverage(website)[3][1];
	SpecificitySport [0]=GetECoverage(website)[3][1]/GetECoverage(website)[3][0]+GetECoverage(website)[3][1];
	return new double [][]{Specificity,SpecificityCom,SpecificityHeal,SpecificitySport};
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
		System.out.println(EachMatchNum);
		return EachMatchNum;
	}
	catch(Exception e) {
		System.err.println("There is a error!");
		e.printStackTrace();
		return 0;
	} 
	
	
	
}

}
