package com.ifeng.iRecommend.likun.userCenter.tnappuc.utils;

import java.util.ArrayList;

import com.ifeng.iRecommend.featureEngineering.OrientDB.KnowledgeGraph;
import com.tinkerpop.blueprints.Vertex;

public class OrientdbUtil {
	
	public static void main(String[] args){
		KnowledgeGraph kg = new KnowledgeGraph();
		String word= "广州恒大";
		String typelabel= "";
		String type= "";
		ArrayList<Vertex> vlists = kg.queryWord(word);
		for(Vertex v : vlists){
			System.out.println(v.getProperty("word"));
			System.out.println(v.getProperty("type"));
		}
		
	}

}
