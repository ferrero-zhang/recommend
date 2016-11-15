package com.ifeng.iRecommend.likun.rankModel;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ifeng.iRecommend.featureEngineering.itemf;
import com.ifeng.iRecommend.featureEngineering.queryInterface;

public class addItemNewTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testCmpItemVector() {
//		itemf item = new itemf();
//		queryInterface query = queryInterface.getInstance();
//		item = query.queryItemF("29429");
//		RankItemNew  r_item = new RankItemNew(item);
//		addItemNew ain = new addItemNew(r_item);
//		System.out.println(ain.doc.topic1);
//		System.out.println(ain.doc.topic2);
//		System.out.println(ain.doc.topic3);
//		System.out.println("-------------");
		

		
		itemf item = new itemf();
		item.setAppId("abc");
		item.setID("hhh");
		item.setPublishedTime("2014-10-10T23:23:23Z");
		item.setSplitTitle("我们 大中国 就是 一个 人");
		item.setDocType("doc");
		ArrayList<String> al_features = new ArrayList<String>();
		
		//test 1
		al_features.clear();
		item.addFeatures(al_features);
		RankItemNew r_item = new RankItemNew(item);
		addItemNew ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		//test 2
		al_features.clear();
		al_features.add("社会");
		al_features.add("c");
		al_features.add("0.1");
		item.addFeatures(al_features);	
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		//test 3
		al_features.clear();
		al_features.add("社会");
		al_features.add("c");
		al_features.add("0.1");
		al_features.add("房产");
		al_features.add("c");
		al_features.add("0.2");
		item.addFeatures(al_features);	
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		
		//test 3
		al_features.clear();
		al_features.add("社会");
		al_features.add("c");
		al_features.add("0.1");
		al_features.add("房产");
		al_features.add("c");
		al_features.add("0.2");
		al_features.add("军事");
		al_features.add("c");
		al_features.add("1.0");
		item.addFeatures(al_features);	
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		//test 3
		al_features.clear();
		al_features.add("军事");
		al_features.add("c");
		al_features.add("1.0");
		item.addFeatures(al_features);	
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		//test 5
		al_features.clear();
		al_features.add("社会");
		al_features.add("c");
		al_features.add("0.1");
		al_features.add("房产");
		al_features.add("c");
		al_features.add("0.2");
		al_features.add("军事");
		al_features.add("cs");
		al_features.add("1.0");
		item.addFeatures(al_features);	
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		//test 6
		al_features.clear();
		al_features.add("社会");
		al_features.add("c");
		al_features.add("0.1");
		al_features.add("房产");
		al_features.add("c");
		al_features.add("0.2");
		al_features.add("军事");
		al_features.add("sc");
		al_features.add("1.0");
		item.addFeatures(al_features);	
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		// test 7
		al_features.clear();
		al_features.add("社会");
		al_features.add("c");
		al_features.add("0.1");
		al_features.add("房产");
		al_features.add("c");
		al_features.add("0.2");
		al_features.add("军事");
		al_features.add("sc");
		al_features.add("0.3");
		item.addFeatures(al_features);
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		// test 8
		al_features.clear();
		al_features.add("房产");
		al_features.add("c");
		al_features.add("0.2");
		al_features.add("军事");
		al_features.add("sc");
		al_features.add("0.3");
		al_features.add("军事武器");
		al_features.add("cn");
		al_features.add("0.1");
		al_features.add("飞机航空");
		al_features.add("cn");
		al_features.add("0.5");
		item.addFeatures(al_features);
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		// test 9
		al_features.clear();
		al_features.add("飞机航空");
		al_features.add("cn");
		al_features.add("0.8");
		item.addFeatures(al_features);
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		// test 10
		al_features.clear();
		al_features.add("飞机航空");
		al_features.add("t");
		al_features.add("0.8");
		item.addFeatures(al_features);
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		// test 11
		al_features.clear();
		al_features.add("飞机");
		al_features.add("sc");
		al_features.add("0.8");
		item.addFeatures(al_features);
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		
		// test 12
		al_features.clear();
		al_features.add("飞机");
		al_features.add("sc");
		al_features.add("0.76");
		al_features.add("打飞机");
		al_features.add("sc");
		al_features.add("0.2");
		item.addFeatures(al_features);
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println("-------------");
		
		
		// test 13
		al_features.clear();
		al_features.add("房产");
		al_features.add("c");
		al_features.add("-0.2");
		al_features.add("飞机");
		al_features.add("sc");
		al_features.add("0.76");
		al_features.add("打飞机");
		al_features.add("sc");
		al_features.add("0.2");
		item.addFeatures(al_features);
		r_item = new RankItemNew(item);
		ain = new addItemNew(r_item);
		System.out.println(ain.doc.topic1);
		System.out.println(ain.doc.topic2);
		System.out.println(ain.doc.topic3);
		System.out.println(ain.doc.relatedfeatures);
		System.out.println("-------------");
	}

}
