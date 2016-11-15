package com.ifeng.iRecommend.dingjw.itemParser;

import static org.junit.Assert.*;
import ikvdb.client.IkvdbClient;
import ikvdb.client.IkvdbClientConfig;
import ikvdb.client.IkvdbClientFactory;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;

public class ItemOperationTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testGetItem() {
		ItemOperation itemOP = ItemOperation.getInstance();
//		itemOP.setItemType(ItemType.PCITEM);
//		String url = "http://sports.ifeng.com/gnzq/special/yayusai/guozu/detail_2014_02/26/34206625_0.shtml";
//		long start = System.currentTimeMillis();
//		Item oneItem = itemOP.getItem(url);
//		long end = System.currentTimeMillis();
//		System.out.println(end-start);
//		System.out.println(oneItem.getTitle());
//		System.out.println(oneItem.getContent());
//		System.out.println(oneItem.getKeywords());
//		System.out.println("--------");
//		String bbsurl = "http://edu.ifeng.com/news/detail_2014_03/12/34680081_0.shtml";
//		oneItem = itemOP.getItem(bbsurl);
//		assertNull(oneItem);
//		System.out.println("--------");
//		oneItem = itemOP.get("http://news.ifeng.com/mainland/special/haoren/content-3/detail_2013_11/22/31467867_0.shtml");
//		System.out.println(end-start);
//		System.out.println(oneItem.getTitle());
//		System.out.println(oneItem.getContent());
//		System.out.println(oneItem.getKeywords());
//		System.out.println("--------");
//		

		itemOP.setItemType(ItemType.APPITEM);
		long a = System.currentTimeMillis();
		Item oneItem = itemOP.get("http://fashion.ifeng.com/a/20140801/40030620_0.shtml");
		System.out.println(JsonUtils.toJson(oneItem));
		long b = System.currentTimeMillis();
		System.out.println(b-a);
		oneItem = itemOP.getItemFromHbase("http://fashion.ifeng.com/a/20140801/40030620_0.shtml");
		System.out.println(JsonUtils.toJson(oneItem));
		long c = System.currentTimeMillis();
		System.out.println(c-b);
		oneItem = itemOP.getItem("http://fashion.ifeng.com/a/20140801/40030620_0.shtml");
		System.out.println(JsonUtils.toJson(oneItem));
		long d = System.currentTimeMillis();
		System.out.println(d-c);
		oneItem = itemOP.getItem("85484371");
		System.out.println(JsonUtils.toJson(oneItem));
		long e = System.currentTimeMillis();
		System.out.println(e-d);
	}

	@Ignore
	public void testAdd() {
		ItemOperation itemOP = ItemOperation.getInstance();
		itemOP.setItemType(ItemType.PCITEM);
		Item oneItem = new Item();
		oneItem.setUrl("url1");
		oneItem.setID("id1");
		itemOP.add(oneItem.getID(), oneItem);
		
		oneItem.setUrl("url2");
		oneItem.setID("id2");
		itemOP.add(oneItem.getID(), oneItem);
		
		Item oneItem2 = itemOP.get("id1");
		assertEquals("url1",oneItem2.getUrl());
		
		oneItem2 = itemOP.get("id2");
		assertEquals("url2",oneItem2.getUrl());
	}

	@Ignore
	public void testDel() {
		ItemOperation itemOP = ItemOperation.getInstance();
		itemOP.setItemType(ItemType.PCITEM);
		
		Item oneItem = new Item();
		oneItem.setUrl("url1");
		oneItem.setID("id1");
		itemOP.add(oneItem.getID(), oneItem);
		
		itemOP.del("id1");
		
		oneItem = itemOP.get("id1");
		assertNull(oneItem);
	
	}

	@Ignore
	public void testUrlParse() {
//		assertEquals("news-mainland", ItemOperation.urlParse("http://news.ifeng.com/mainland/detail_2011_10/19/9955077_0.shtml"));
//		//fail("Not yet implemented");
	}
	
}
