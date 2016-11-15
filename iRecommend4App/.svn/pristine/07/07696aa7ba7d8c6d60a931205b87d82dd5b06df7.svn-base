package com.ifeng.iRecommend.featureEngineering.LayerGraph;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class GraphTest {
	
	@Before
	public void setUp() throws Exception {
		
	}
	@Test
	public void test() {
		Graph graph = Graph.getInstance();
		
		//以word来查询,一个word可能有多个type,节点不唯一,如"银行"既是sc,也是cn
		String queryWord="银行";
		ArrayList<GraphNode> graphNodeList=graph.queryWord(queryWord);
		assertNotNull(graphNodeList);
		for (GraphNode gn:graphNodeList) {
			System.out.println(gn.getData().getType()+"="+gn.getData().getWord());
			System.out.println("上溯结果：\n"+ graph.iteratorGraphUp(gn));	
			String a=graph.iteratorGraphUp(gn);
			a.replace("\n", "");
			String[] arr=a.split(",");
			System.out.println("下溯结果：\n"+ graph.iteratorGraphDown(gn));
		}
		
		System.out.println("-------------------------");
		
		//以NodeData来查询,节点唯一
		NodeData querynode=new NodeData("基金法规","sc");
		GraphNode presentNode=graph.queryNodeData(querynode);   //得到当前查询的节点
		assertNotNull(presentNode);
		System.out.println(presentNode.getData().getType()+"="+presentNode.getData().getWord());
		System.out.println("上溯结果：\n"+ graph.iteratorGraphUp(presentNode));	
		System.out.println("下溯结果：\n"+ graph.iteratorGraphDown(presentNode));
		
		System.out.println("-------------------------");				
		
		//查询不存在的节点，无结果
		querynode=new NodeData("帅哥","cn");
		presentNode=graph.queryNodeData(querynode);
		assertNull(presentNode);
		
		System.out.println("-------------------------");
		
		querynode=new NodeData();
		presentNode=graph.queryNodeData(querynode);
		assertNull(presentNode);
		
	}

}
