package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import com.ifeng.iRecommend.featureEngineering.OrientDB.KnowledgeGraph;

public class HotWordGraphDataTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		HotWordGraphData hotWordGraphData = HotWordGraphData.getInstance();
		
		CommonDataSub cds = new CommonDataSub();
 		Thread t = new Thread(cds); 
 		t.start();
 		KnowledgeGraph kGraph =new KnowledgeGraph();
 		while(true){ 
 			//查图数据库是否有该热点词
 			System.out.println(kGraph.queryWord("NBA专题"));
 			System.out.println(kGraph.queryWord("TESTTESTTEST"));
 			System.out.println(kGraph.queryWord("HALO"));

 			
 		}
 		//kGraph.shutdown();
		
	}

}
