package com.ifeng.iRecommend.featureEngineering.OrientDB;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class KnowledgeGraphTest {

	@Test
	public void test() {		
		KnowledgeGraph kgraph=new KnowledgeGraph();
		ArrayList<Vertex> vertexList= new ArrayList<Vertex>();
		//查询当前词 
		vertexList=kgraph.queryWord("你好","t");
		vertexList=kgraph.queryType("c1");
		for (Vertex v:vertexList) {
			System.out.println(v.getProperty("typelabel").toString() + "=" + v.getProperty("word").toString());
		}
		vertexList=kgraph.queryWord("音乐","c1");
		//vertexList=kgraph.queryWord("音乐","c1","c");
		
		//查询父节点
		vertexList=kgraph.queryParent(null);
		vertexList=kgraph.queryParent("音乐","c1");
		//vertexList=kgraph.queryParent("音乐","c1","c"); 
		
		//查询子节点
		vertexList=kgraph.queryChild("音乐");
		vertexList=kgraph.queryChild("音乐","c1");
		//vertexList=kgraph.queryChild("音乐","c1","c");
		
		//上溯查询
		vertexList=kgraph.queryUpTraverse("段子");
		//vertexList=kgraph.queryUpTraverse("音乐","c1");
		//vertexList=kgraph.queryUpTraverse("音乐","c1","c");
		//vertexList=kgraph.queryType("c");
		for (Vertex v:vertexList) {
			System.out.println(v.getProperty("word").toString()+"	"+v.getProperty("typelabel").toString());
		}
		
		//下溯查询
		vertexList=kgraph.queryDownTraverse("音乐");
		vertexList=kgraph.queryDownTraverse("音乐","c1");
		//vertexList=kgraph.queryDownTraverse("音乐","c1","c");
		
		//上溯传递边权重，返回特征三元组
		ArrayList<String> featureList=new ArrayList<String>();
		//featureList=kgraph.queryUpTraverseWeight("互联网金融");
		featureList=kgraph.queryUpTraverseWeight("互联网金融", "sc");
		//featureList=kgraph.queryUpTraverseWeight("互联网金融", "sc","sc"); 
		System.out.println(featureList);
		
		//获取两个顶点之间边的权重
		ArrayList<Vertex> v1List = kgraph.queryWord("足球","c1");//当前节点
		ArrayList<Vertex> v2List = kgraph.queryWord("体育","c0");//父亲节点
		String weight=kgraph.getEdgeWeight(v1List.get(0),v2List.get(0));//两个节点之间
		System.out.println(weight);
		
		
		kgraph.shutdown();//操作完成记得关闭
		
		
			
	}

}
