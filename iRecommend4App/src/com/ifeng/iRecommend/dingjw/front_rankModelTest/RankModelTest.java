package com.ifeng.iRecommend.dingjw.front_rankModelTest;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.lidm.itemCollector.itemFront;
import com.ifeng.iRecommend.dingjw.front_rankModel.RankList;
import com.ifeng.iRecommend.dingjw.front_rankModelTest.RankModel2;
import com.ifeng.iRecommend.dingjw.front_rankModel.RankItem;
import com.ifeng.iRecommend.dingjw.itemParser.Item;

public class RankModelTest {
	
	private static ArrayList<itemFront> itemFrontList;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// 读取前端item
		// String requestUrl = fieldDicts.itemFrontUrl;
		// itemFrontList = RankModel.getInstance().readUrl(requestUrl);

		// 读取testcase
		/*itemFrontList = new ArrayList<itemFront>();

		String jsonSource = "";
		try {
			FileUtil testFile = new FileUtil();
			testFile.Initialize(
					"E:\\workspace\\iRecommend\\src\\com\\ifeng\\iRecommend\\dingjw\\front_rankModelTest\\specialWordsTestCase.txt",
					"UTF-8");

			jsonSource = testFile.ReadLine();
			if (jsonSource == null || jsonSource.isEmpty())
				return;

			JsonArray jsonArray = new JsonArray();

			try {
				jsonArray = new JsonParser().parse(jsonSource).getAsJsonArray();
			} catch (Exception e) {
				;
			}

			for (JsonElement jsonElement : jsonArray) {
				itemFront item_front = JsonUtils.fromJson(
						jsonElement.toString(), itemFront.class);

				if (item_front == null) {

					continue;
				} else {
					item_front.setTitle(item_front.getTitle().replaceAll(
							"[\r\n]", " "));
					itemFrontList.add(item_front);
					// System.out.println(item_front.getTitle());
				}
			}
		} catch (Exception e1) {
			;
		}*/
	}

	@Before
	public void setUp() throws Exception {
		fieldDicts.treeMappingFile = "D:\\workspace\\iRecommend\\src\\com\\ifeng\\iRecommend\\dingjw\\itemParser\\TreeMapping.txt";
		fieldDicts.frontTreeMappingFile = "D:\\workspace\\iRecommend\\src\\com\\ifeng\\iRecommend\\dingjw\\itemParser\\Front_TreeMapping.txt";

	}

	
	@Ignore
	public void testGetRankItem() {
		/*assertNull(RankModel.getInstance().getRankItem(null));
		for (itemFront item_front : itemFrontList)
		{
			assertNotNull(RankModel.getInstance().getRankItem(item_front));
			//System.out.println("Item Special Words: "+RankModel.getInstance().getRankItem(item_front).getSpecialWords());
			assertNotNull(RankModel.getInstance().getRankItem(item_front).getSpecialWords());
		}*/
	}
	
	@Ignore
	public void testFindSimilarItemBySpecialWords() {
		
		/*assertNull(RankModel.getInstance().findSimilarItemBySpecialWords(null, null, 0));
		HashSet<RankItem> itemSet = new HashSet<RankItem>();
		for(itemFront item_front: itemFrontList) {
			RankItem item = RankModel.getInstance().getRankItem(item_front);
			itemSet.add(item);
		}
		assertNull(RankModel.getInstance().findSimilarItemBySpecialWords(itemSet, null, 0));
		assertNull(RankModel.getInstance().findSimilarItemBySpecialWords(null, itemFrontList.get(0), 0));
		
		itemFront tarItemFront = new itemFront();
		tarItemFront.setTitle("奶奶不知孙子跌车 将其碾死");
		tarItemFront.setUrl("http://news.ifeng.com/society/1/detail_2013_09/17/29670571_0.shtml");
		tarItemFront.setCategory("society");
		tarItemFront.setImgurl("null");
		tarItemFront.setWeight("C");
		tarItemFront.setTimeStamp(Long.toString(System.currentTimeMillis()));
		
		RankItem rankItem = RankModel.getInstance().findSimilarItemBySpecialWords(itemSet, tarItemFront, 0.5f);
		assertNull(rankItem);
		System.out.println(tarItemFront.getTitle()+"||"+tarItemFront.getUrl());
*/
	}
	
	@Ignore
	public void testSpecialWords()
	{
		/*itemFront item_front = new itemFront();
		item_front.setTitle("大陆游客在马英九办公室前持菜刀自残原因披露");
		item_front.setUrl("http://news.ifeng.com/world/detail_2013_11/15/31279927_0.shtml");
		item_front.setCategory("taiwan");
		item_front.setImgurl("null");
		item_front.setWeight("B");
		item_front.setTimeStamp(Long.toString(System.currentTimeMillis()));
		
		RankItem rankItem1 = new RankItem(item_front);
		System.out.println("url: "+rankItem1.getUrl());
		System.out.println("title: "+rankItem1.getItem().getTitle());
		System.out.println("content: "+rankItem1.getItem().getContent());
		System.out.println("special words: "+rankItem1.getSpecialWords());*/
		
		itemFront tarItemFront = new itemFront();
		tarItemFront.setTitle("在台湾牺牲大陆女特工大解密：身中七枪高呼口号就义");
		tarItemFront.setUrl("http://news.ifeng.com/history/zhongguoxiandaishi/detail_2013_12/20/32298082_0.shtml");
		tarItemFront.setCategory("history");
		tarItemFront.setImgurl("null");
		tarItemFront.setWeight("C");
		tarItemFront.setTimeStamp(Long.toString(System.currentTimeMillis()));
		
		RankItem rankItem2 = new RankItem(tarItemFront);
		
		System.out.println("url: "+rankItem2.getUrl());
		System.out.println("title: "+rankItem2.getItem().getTitle());
		//System.out.println("content: "+rankItem2.getItem().getContent());
		System.out.println("special words: "+rankItem2.getSpecialWords());
		
		//System.out.println("diff: "+commenFuncs.diffRate(rankItem1.getSpecialWords(), rankItem2.getSpecialWords()));
		
	}
	
	@Ignore
	public void testDiff()
	{
		/*String str1 = "奶奶不知孙子跌车 将其碾死";
		String str2 = "奶奶不知孙子自副驾跌出车外倒车将其碾死(图)";
		System.out.println(commenFuncs.diffRate(str1, str2));
		String split1 = RankModel.SplitWord(str1).replaceAll("[(/)a-z\\pP]", "").replace("\r\n", "");
		String split2 = RankModel.SplitWord(str2).replaceAll("[(/)a-z\\pP]", "").replace("\r\n", "");
		System.out.println(split1);
		System.out.println(split2);
		System.out.println(commenFuncs.diffRate(split1, split2));
		String splits1[] = split1.split(" ");
		String splits2[] = split2.split(" ");
		
		for(String word: splits1)
			System.out.println(word);
		for(String word: splits2)
			System.out.println(word);
		System.out.println(commenFuncs.diffRate(splits1, splits2));*/
		
		/*float diff;
		
		RankItem rankItem1 = RankModel.getInstance().getRankItem(itemFrontList.get(0));
		RankItem rankItem2 = RankModel.getInstance().getRankItem(itemFrontList.get(1));
		System.out.println("url: "+rankItem1.getUrl());
		System.out.println("title: "+rankItem1.getTitle());
		System.out.println("special words: "+rankItem1.getSpecialWords());
		System.out.println("url: "+rankItem2.getUrl());
		System.out.println("title: "+rankItem2.getTitle());
		System.out.println("special words: "+rankItem2.getSpecialWords());
		diff = commenFuncs.diffRate(rankItem1.getSpecialWords(), rankItem2.getSpecialWords());
		System.out.println("diff: "+ diff);
		assertTrue( diff < 0.3f );
		System.out.println();
		
		rankItem1 = RankModel.getInstance().getRankItem(itemFrontList.get(3));
		rankItem2 = RankModel.getInstance().getRankItem(itemFrontList.get(4));
		System.out.println("url: "+rankItem1.getUrl());
		System.out.println("title: "+rankItem1.getTitle());
		System.out.println("special words: "+rankItem1.getSpecialWords());
		System.out.println("url: "+rankItem2.getUrl());
		System.out.println("title: "+rankItem2.getTitle());
		System.out.println("special words: "+rankItem2.getSpecialWords());
		diff = commenFuncs.diffRate(rankItem1.getSpecialWords(), rankItem2.getSpecialWords());
		System.out.println("diff: "+ diff);
		assertTrue( diff < 0.3f );
		System.out.println();
		
		rankItem1 = RankModel.getInstance().getRankItem(itemFrontList.get(5));
		rankItem2 = RankModel.getInstance().getRankItem(itemFrontList.get(6));
		System.out.println("url: "+rankItem1.getUrl());
		System.out.println("title: "+rankItem1.getTitle());
		System.out.println("special words: "+rankItem1.getSpecialWords());
		System.out.println("url: "+rankItem2.getUrl());
		System.out.println("title: "+rankItem2.getTitle());
		System.out.println("special words: "+rankItem2.getSpecialWords());
		diff = commenFuncs.diffRate(rankItem1.getSpecialWords(), rankItem2.getSpecialWords());
		System.out.println("diff: "+ diff);
		assertTrue( diff > 0.3f );
		System.out.println();
		
		rankItem1 = RankModel.getInstance().getRankItem(itemFrontList.get(7));
		rankItem2 = RankModel.getInstance().getRankItem(itemFrontList.get(8));
		System.out.println("url: "+rankItem1.getUrl());
		System.out.println("title: "+rankItem1.getTitle());
		System.out.println("special words: "+rankItem1.getSpecialWords());
		System.out.println("url: "+rankItem2.getUrl());
		System.out.println("title: "+rankItem2.getTitle());
		System.out.println("special words: "+rankItem2.getSpecialWords());
		diff = commenFuncs.diffRate(rankItem1.getSpecialWords(), rankItem2.getSpecialWords());
		System.out.println("diff: "+ diff);
		assertTrue( diff < 0.3f );
		System.out.println();
		
		rankItem1 = RankModel.getInstance().getRankItem(itemFrontList.get(9));
		rankItem2 = RankModel.getInstance().getRankItem(itemFrontList.get(10));
		System.out.println("url: "+rankItem1.getUrl());
		System.out.println("title: "+rankItem1.getTitle());
		System.out.println("special words: "+rankItem1.getSpecialWords());
		System.out.println("url: "+rankItem2.getUrl());
		System.out.println("title: "+rankItem2.getTitle());
		System.out.println("special words: "+rankItem2.getSpecialWords());
		diff = commenFuncs.diffRate(rankItem1.getSpecialWords(), rankItem2.getSpecialWords());
		System.out.println("diff: "+ diff);
		assertTrue( diff < 0.3f );
		System.out.println();
		
		rankItem1 = RankModel.getInstance().getRankItem(itemFrontList.get(11));
		rankItem2 = RankModel.getInstance().getRankItem(itemFrontList.get(12));
		System.out.println("url: "+rankItem1.getUrl());
		System.out.println("title: "+rankItem1.getTitle());
		System.out.println("special words: "+rankItem1.getSpecialWords());
		System.out.println("url: "+rankItem2.getUrl());
		System.out.println("title: "+rankItem2.getTitle());
		System.out.println("special words: "+rankItem2.getSpecialWords());
		diff = commenFuncs.diffRate(rankItem1.getSpecialWords(), rankItem2.getSpecialWords());
		System.out.println("diff: "+ diff);
		assertTrue( diff < 0.3f );
		System.out.println();
		*/
		
	}

	@Ignore
	public void testFindSimilarRankItemInOscache() {
		
		//assertNull( RankModel.getInstance().findSimilarRankItemInOscache(null));
		/*RankList rankList = new RankList();
		for(itemFront item_front: itemFrontList) {
			try {
				RankItem rankItem = (RankItem) RankModel.getInstance().get(item_front.getTitle());
				if(rankItem!=null) {//如果oscache中存在rankitem
					RankModel.getInstance().updateRankItem(rankList, rankItem, item_front);
				}
				else{//如果oscache中没有，
					rankItem = RankModel.getInstance().findSimilarRankItemInOscache(item_front);//查找相似的rankitem
					if(rankItem!=null){//如果有，则更新
						System.out.println("Similar Item!");
						System.out.println("Old item: "+rankItem.getTitle()+" "+rankItem.getUrl());
						System.out.println("New item: "+item_front.getTitle()+" "+item_front.getUrl());
						RankModel.getInstance().updateRankItem(rankList, rankItem, item_front);
					}
					else {//如果没有，则新建
						RankModel.getInstance().createRankItem(rankList, item_front);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				continue;
			}
			
		}*/
	}
	
	@Ignore
	public void testReadTongji() {
		/*String requestUrl = "http://tongji.ifeng.com:9090/webtop/loadNews?chnnid=http://news.ifeng.com/mil/&num=100&tmnum=0";

		ArrayList<itemFront> itemFrontList = RankModel.getInstance().readTongji(requestUrl);
		
		for( itemFront item_front: itemFrontList) {
			System.out.println(item_front.getTitle());
			System.out.println(item_front.getUrl());
			System.out.println(item_front.getWeight());
			System.out.println(item_front.getCategory());
			System.out.println(item_front.getImgurl());
			System.out.println(item_front.getTimeStamp());
		}*/
		
		/*ArrayList<String> tongjiUrls = fieldDicts.tongjiUrls;
		for(String url: tongjiUrls)
			System.out.println(url);*/
	}
	
	@Ignore
	public void testReadUrl() {
		String requestUrl = "http://223.203.209.98:8080/irecommend/irecommend_main/irecommend_main.txt";

		ArrayList<itemFront> itemFrontList = RankModel.getInstance().readUrl(requestUrl);
		
		for( itemFront item_front: itemFrontList) {
			System.out.println(item_front.getTitle());
			System.out.println(item_front.getUrl());
			System.out.println(item_front.getWeight());
			System.out.println(item_front.getCategory());
			System.out.println(item_front.getImgurl());
			System.out.println(item_front.getTimeStamp());
		}
	}
	
	@Ignore
	public void testRankItemByPv(){
		/*String requestUrl = fieldDicts.itemFrontUrl;
		ArrayList<itemFront> itemFrontList = new ArrayList<itemFront>();
		System.out.println("itemFrontList Size: "+itemFrontList.size());
		
		//从统计系统获取文章列表
		ArrayList<String> tongjiUrls = fieldDicts.tongjiUrls;
		for(String url: tongjiUrls) {
			ArrayList<itemFront> itemFrontList1 = RankModel2.getInstance().readTongji(url);
			//合并itemFront列表
			for(itemFront item_front: itemFrontList1)
				itemFrontList.add(item_front);
		}
		System.out.println("itemFrontList Size: "+itemFrontList.size());
		
		RankModel2.getInstance().rankItemByPv(itemFrontList);
		
		for(itemFront item: itemFrontList)
			System.out.println(item.getTitle()+" "+item.getUrl()+" "+item.getWeight()+" "+item.getPv());*/
		
		/*ArrayList<itemFront> itemFrontList = RankModel2.getInstance().readTongji2(fieldDicts.itemFromTongji, 20);
		//对itemFront根据频道进行分类
		HashMap<String, ArrayList<itemFront>> channelMap = new HashMap<String, ArrayList<itemFront>>();
		for(itemFront itemF : itemFrontList)
		{
			if (itemF.getPv() == null || itemF.getPv() < 0) {
				itemF.setPv(0);
			}
			String channel = itemF.getCategory();
			ArrayList<itemFront> itemList;
			if ((itemList = channelMap.get(channel)) != null) {
				itemList.add(itemF);
			} else {
				itemList = new ArrayList<itemFront>();
				itemList.add(itemF);
				channelMap.put(channel, itemList);
			}
		}*/
		
		
		
	}

	
	@Ignore
	public void testReadTongji2(){
		
		
		long start = System.currentTimeMillis();
		String requestUrl = RankModel2.getInstance().getTongjiURL(fieldDicts.itemFromTongji, 10);
		ArrayList<itemFront> itemFrontList2 = null;
		try {
			itemFrontList2 = RankModel2.getInstance().readTongji2(requestUrl, 15);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			requestUrl = RankModel2.getInstance().getTongjiURL(fieldDicts.itemFromTongji, 20);
			try {
				itemFrontList2 = RankModel2.getInstance().readTongji2(requestUrl, 10);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("time consumed: "+(end - start));
		
		for(itemFront itemF: itemFrontList2){
			System.out.println(itemF.getTitle()+" "+itemF.getUrl()+" "+itemF.getCategory());
		}
		//RankModel2.getInstance().rankItemByPv(itemFrontList);
		System.out.println(itemFrontList2.size());
		
		
		//ArrayList<itemFront> itemFrontList2 = RankModel2.getInstance().readTongji2(-1);
		//System.out.println(itemFrontList2.size());
		
		/*assertNull(RankModel2.getInstance().readTongji2(null, 20));
		assertNull(RankModel2.getInstance().readTongji2("", 20));
		
		ArrayList<itemFront> itemFrontList2 = RankModel2.getInstance().readTongji2("http://ifeng.com/", 20);
		System.out.println(itemFrontList2.size());*/
	}
	
	

	@Ignore
	public void testNormalizeUrl(){
		String url = "http://xm.ifeng.com/detail_2013_11/10/1440127_13.shtml";
		System.out.println(RankModel2.getInstance().normalizedUrl(url));
	}
	
	@Test
	public void testReadAItems() {
		ArrayList<itemFront> aItemList = RankModel2.getInstance().readAItems();
		System.out.println(aItemList.size());
		for(itemFront itemF: aItemList) {
			System.out.println(itemF.getTitle()+" "+itemF.getUrl()+" "+itemF.getWeight());
		}
	}

}
