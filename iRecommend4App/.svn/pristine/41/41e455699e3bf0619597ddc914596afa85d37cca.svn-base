package com.ifeng.iRecommend.featureEngineering.databaseOperation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;

public class IKVOperationv2Test {

	@Before
	public void setUp() throws Exception {
	}
	public static void main(String[] args) throws InterruptedException {
		String tablename = "appitemdb";
		IKVOperationv2 ob = new IKVOperationv2(tablename);
		String key = "5353561";
		Thread.sleep(3000);
		// System.out.println(ob.get("_"+key));
		// System.out.println(ob.get("x_"+key));
//		Map <String, itemf> map = ob.queryItems("c", key);
//		System.out.println("map size is " + map.size());
//		for (Entry<String, itemf> entry : map.entrySet()) {
//			System.out.println(entry.getKey() + " " + entry.getValue().getID()+" "+entry.getValue().getTitle());
//		}
		// appBill appitem = ob.queryAppBill(key, "c");
		 itemf appitem = ob.queryItemF(key, "c");
		 System.out.println(appitem.getID());
		 System.out.println(appitem.getTitle());
		 System.out.println(appitem.getUrl());
		 System.out.println(appitem.getAppId());
		 System.out.println(appitem.getDocType());
		 System.out.println(appitem.getOther());
		 System.out.println(appitem.getPublishedTime());
		 System.out.println(appitem.getShowStyle());
		 System.out.println(appitem.getSource());
		 System.out.println(appitem.getSplitContent());
		 System.out.println(appitem.getSplitTitle());
		 System.out.println(appitem.getFeatures());
		 System.out.println(appitem.getTags());
//		 System.out.println(appitem.getrelatedIds());
//		 System.out.println(appitem.getTags());
//		 System.out.println(appitem.getFeatures());
		ob.close();
	}
}
