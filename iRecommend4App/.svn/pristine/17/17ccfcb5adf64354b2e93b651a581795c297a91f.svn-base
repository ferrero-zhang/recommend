package com.ifeng.iRecommend.usermodel;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.iRecommend.dingjw.front_rankModel.RankItem;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.usermodel.itemAbstraction;

public class itemAbstractionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		fieldDicts.appTreeMappingFile = "D:/workspace/iRecommend4App/testenv/AppTreeMapping.txt";
		fieldDicts.frontAppTreeMappingFile = "D:/workspace/iRecommend4App/testenv/APPFront_TreeMapping.txt";
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Ignore
	public void testNormalization() {
		HashMap<String, Float> hm_test = new HashMap<String, Float>();
		hm_test.put("beau", 1.0f);
		hm_test.put("ty", 2.0f);
		hm_test = itemAbstraction.normalization(hm_test);
		assertEquals(hm_test.size(),2);
		assertEquals(0,hm_test.get("beau")-1.0f,0f);
		assertEquals(0,hm_test.get("beau")-1.0f,0f);
		
	}

	@Test
	public void testCmpChannels() {
		String imcp_id = "93688564";
		ItemOperation itemOP = ItemOperation.getInstance();
		itemOP.setItemType(ItemType.APPITEM);
		Item oneItem = itemOP.getItem(imcp_id);
		RankItem rItem = new RankItem(oneItem);
		HashMap<String, Float> hm_tagValues = itemAbstraction
				.cmpChannels(rItem,imcp_id);
		Iterator<Entry<String, Float>> it = hm_tagValues.entrySet().iterator();
		System.out.println(rItem.getTitle());
		System.out.println(rItem.getItem().getContent());
		while(it.hasNext()){
			Entry<String, Float> et = it.next();
			System.out.println(et.getKey()+"|"+et.getValue());
		}
		
		
		imcp_id = "93709504";
		oneItem = itemOP.getItem(imcp_id);
		rItem = new RankItem(oneItem);
		hm_tagValues = itemAbstraction
				.cmpChannels(rItem,imcp_id);
		it = hm_tagValues.entrySet().iterator();
		System.out.println(rItem.getTitle());
		System.out.println(rItem.getItem().getContent());
		while(it.hasNext()){
			Entry<String, Float> et = it.next();
			System.out.println(et.getKey()+"|"+et.getValue());
		}
		
		imcp_id = "93705977";
		oneItem = itemOP.getItem(imcp_id);
		rItem = new RankItem(oneItem);
		hm_tagValues = itemAbstraction
				.cmpChannels(rItem,imcp_id);
		it = hm_tagValues.entrySet().iterator();
		System.out.println(rItem.getTitle());
		System.out.println(rItem.getItem().getContent());
		while(it.hasNext()){
			Entry<String, Float> et = it.next();
			System.out.println(et.getKey()+"|"+et.getValue());
		}
		
		
		imcp_id = "93705588";
		oneItem = itemOP.getItem(imcp_id);
		rItem = new RankItem(oneItem);
		hm_tagValues = itemAbstraction
				.cmpChannels(rItem,imcp_id);
		it = hm_tagValues.entrySet().iterator();
		System.out.println(rItem.getTitle());
		System.out.println(rItem.getItem().getContent());
		while(it.hasNext()){
			Entry<String, Float> et = it.next();
			System.out.println(et.getKey()+"|"+et.getValue());
		}
		
//		Item item = new Item();
//		item.setID("a");
//		item.setUrl("http://house.ifeng.com/loushi/guangzhou/detail_2013_07/29/28015837_0.shtml");
//		HashMap<String, Float> hm_res = itemAbstraction.cmpChannels(item,item.getUrl());
//		Iterator<Entry<String, Float>> it = hm_res.entrySet().iterator();
//		while(it.hasNext()){
//			Entry<String, Float> et = it.next();
//			System.out.println(et.getKey()+"|"+et.getValue());
//		}
//		
//		item.setUrl("http://fashion.ifeng.com/news/detail_2013_07/29/28031574_0.shtml");
//		hm_res = itemAbstraction.cmpChannels(item,item.getUrl());
//		it = hm_res.entrySet().iterator();
//		while(it.hasNext()){
//			Entry<String, Float> et = it.next();
//			System.out.println(et.getKey()+"|"+et.getValue());
//		}
//		
//		item.setUrl("http://v.ifeng.com/news/society/201307/31149206-6b14-495b-a0bd-5ba0447d80b7.shtml");
//		hm_res = itemAbstraction.cmpChannels(item,item.getUrl());
//		it = hm_res.entrySet().iterator();
//		while(it.hasNext()){
//			Entry<String, Float> et = it.next();
//			System.out.println(et.getKey()+"|"+et.getValue());
//		}
//		
//		
//		item.setUrl("http://v.ifeng.com/vblog/dv/201307/19f5a8b3-849c-4ca7-94d7-426eb11662c9.shtml");
//		hm_res = itemAbstraction.cmpChannels(item,item.getUrl());
//		it = hm_res.entrySet().iterator();
//		while(it.hasNext()){
//			Entry<String, Float> et = it.next();
//			System.out.println(et.getKey()+"|"+et.getValue());
//		}
//		
//		item.setUrl("http://sports.ifeng.com/gnzq/zc/hengda/detail_2013_07/29/28033235_0.shtml");
//		hm_res = itemAbstraction.cmpChannels(item,item.getUrl());
//		it = hm_res.entrySet().iterator();
//		while(it.hasNext()){
//			Entry<String, Float> et = it.next();
//			System.out.println(et.getKey()+"|"+et.getValue());
//		}
	}

	@Ignore
	public void testCmpLatentTopics() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testGetItemTopWords() {
		
		Item item = new Item();
		item.setID("a");
		item.setUrl("http://house.ifeng.com/loushi/guangzhou/detail_2013_07/29/28015837_0.shtml");
		item.setTitle("陈一冰_nr 出席_v 活动_v 巧遇_v 闫凤娇_nr  _w 隔_v 座_q 交流_v 避_v 绯闻_n 我_n");
		item.setKeywords("陈玉冰巧遇闫凤娇 体操陈一冰 陈玉冰_nr 巧遇_v 闫凤娇_nr  _w 体操_n 陈一冰_nr");
		item.setContent("2013年_t 7月_t 30日_t ，_w 上海_ns ，_w 陈一冰_nr 出席_v 某_r 游戏_v 活动_v 在_p 台下_s 就坐_v 与_p 众_h 嘉宾_k 亲切_a 合影_k ，_w 现场_n 还_v 出现_v 了_u 3年_mq 前_f “_w 厕所门_ns ”_w 不_h 雅_k 照_v 女主角_n 闫凤娇_nr 。_w 只见_v ，_w 陈一冰_nr 与_p 闫凤娇_nr 为了_p 避嫌_v 隔_v 了_u 一个_mq 座位_n 交流_v ，_w 当_k 看到_v 有_v 记者_n 拍照_v 时_n ，_w 闫凤娇_nr 马上_d 起身_v 离开_v ，_w 现场_n 也_d 有人_v 认出_v 了_u 闫凤娇_nr ，_w 并_h 与_p 她_r 合照_k 。_w");
		//item.publishDate = "";
		HashMap<String, Float> hm_res = itemAbstraction.getItemTopWords(item,true);
		Iterator<Entry<String, Float>> it = hm_res.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, Float> et = it.next();
			System.out.println(et.getKey()+"|"+et.getValue());
		}
		
		
	}

}
