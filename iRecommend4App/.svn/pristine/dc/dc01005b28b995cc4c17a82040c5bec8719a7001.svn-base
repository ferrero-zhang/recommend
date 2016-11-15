package com.ifeng.iRecommend.dingjw.itemParser;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

public class channelsParserTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//fieldDicts.appTreeMappingFile = "D:\\分类\\高清图\\classForPic.txt";
		// fieldDicts.frontAppTreeMappingFile =
		// "D:/workspace/iRecommend4App/testenv/APPFront_TreeMapping.txt";
		fieldDicts.appSlideTreeMappingFile = "D:\\分类\\高清图\\classForPic.txt";
	}

	@Test
	public void testGetTransChannel() {
		//System.out.println(channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://3g.ifeng.com/news/taiwan/taiwanshizheng/news?aid=86071186", 1));
		System.out.println(channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://2014.ifeng.com/ch/detail_2014_05/12/",2));
//		System.out.println(channelsParser.getInstance(ItemOperation.ItemType.PCITEM).getTransChannel("http:/i.ifeng.com/ixinwen/itaiwan/news?aid=86109309",1));
//		assertNull(channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel(null, 0));
//		assertNull(channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel(null, 2));
//		assertNull(channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("", 0));
//		assertNotNull(channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel(" ", 0));
//		String channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://news.ifeng.com/mil/4/detail_2013_10/29/30751354_0.shtml", 1);
//		System.out.println(channel);
//		assertEquals(true,channel.startsWith("mil"));
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://news.ifeng.com/opinion/society/detail_2013_10/29/30751899_0.shtml", 1);
//		System.out.println(channel);
//		assertEquals(true,channel.startsWith("opinion"));
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://v.ifeng.com/ent/mingxing/201310/f9559ab7-04c2-4121-844f-89751e71c635.shtml#_v_www4", 1);
//		System.out.println(channel);
//		assertEquals(true,channel.startsWith("ent"));
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://home.ifeng.com/news/hangyedongtai/jiaju/detail_2013_10/29/1387765_0.shtml", 1);
//		System.out.println(channel);
//		assertEquals(true,channel.startsWith("home"));
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://news.ifeng.com/shendu/dycjrb/detail_2013_10/29/30736689_0.shtml", 1);
//		System.out.println(channel);
//		assertEquals(true,channel.startsWith("news"));
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://city.ifeng.com/cskx/20131028/399913.shtml", 1);
//		System.out.println(channel);
//		assertEquals(true,channel.startsWith("mainland"));
//		
//		
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://fo.ifeng.com/news/detail_2013_12/18/32225859_0.shtml", 1);
//		System.out.println(channel);
//		assertTrue(channel.startsWith("fo"));
//		assertFalse(channel.startsWith("mainland"));
//		
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://sn.ifeng.com/zixun/shehuiredian/detail_2013_12/18/1605063_0.shtml", 1);
//		System.out.println(channel);
//		assertTrue(channel.startsWith("mainland-sn"));
//		assertFalse(channel.startsWith("sn"));
//		
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://hebei.ifeng.com/news/zbc/detail_2013_12/18/1604586_0.shtml", 1);
//		System.out.println(channel);
//		assertTrue(channel.startsWith("mainland-hebei"));
//		assertFalse(channel.startsWith("hebei"));
//		
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://hb.ifeng.com/news/fygc/detail_2013_12/18/1604630_0.shtml", 1);
//		System.out.println(channel);
//		assertTrue(channel.startsWith("mainland-hb"));
//		assertFalse(channel.startsWith("hb"));
//		
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("http://hb.ifeng.com/news/fygc/detail_2013_12/18/1604630_0.shtml", 0);
//		System.out.println(channel);
//		assertTrue(channel.startsWith("mainland-hb"));
//		assertFalse(channel.startsWith("hb"));
//		
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("ent", 0);
//		System.out.println(channel);
//		assertTrue(channel.startsWith("ent"));
//		
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("15971299", 0);
//		System.out.println(channel);
//		assertTrue(channel.startsWith("15971299"));
//		
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("finance", 1);
//		System.out.println(channel);
//		assertTrue(channel.startsWith("finance"));
//		
//		channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannel("sy", 1);
//		System.out.println(channel);
//		assertTrue(channel.startsWith("notopic"));
		
	}

	@Test
	public void testGetTransChannelByItem() {
		ItemOperation itemOP = ItemOperation.getInstance();
		itemOP.setItemType(ItemType.APPITEM);
		
		String imcp_id = "82722830";
		Item oneItem = itemOP.getItem(imcp_id);
		String channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertTrue(channel.startsWith("fashion-clothes"));
		
		//System.out.println(oneItem.getTitle());
		
		imcp_id = "84194101";
		oneItem = itemOP.getItem(imcp_id);
		channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertTrue(channel.startsWith("notopic"));
				
		imcp_id = "94459029";
		oneItem = itemOP.getItem(imcp_id);
		channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertEquals(true, channel.startsWith("notopic"));
		assertEquals(false, channel.startsWith("i"));
		
		
		imcp_id = "84916657";
		oneItem = itemOP.getItem(imcp_id);
		channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertEquals(true, channel.startsWith("ladys"));
		assertEquals(false, channel.startsWith("ent"));
		
		imcp_id = "84883062";
		oneItem = itemOP.getItem(imcp_id);
		channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertEquals(true, channel.startsWith("ladys"));
		assertEquals(false, channel.startsWith("ent"));
		
		imcp_id = "88059384";
		oneItem = itemOP.getItem(imcp_id);
		channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertEquals(true, channel.startsWith("ent"));
		assertEquals(false, channel.startsWith("i-ent"));
		
		imcp_id = "93337256";
		oneItem = itemOP.getItem(imcp_id);
		channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertEquals(true, channel.startsWith("lanqiu"));
		assertEquals(false, channel.startsWith("sports"));
				
		imcp_id = "88361032";
		oneItem = itemOP.getItem(imcp_id);
		channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertEquals(true, channel.startsWith("lanqiu-nba"));
		assertEquals(false, channel.startsWith("nba"));
		
		imcp_id = "86929289";
		oneItem = itemOP.getItem(imcp_id);
		channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertEquals(true, channel.startsWith("lanqiu-nba"));
		assertEquals(false, channel.startsWith("sports"));
		
		imcp_id = "85315944";
		oneItem = itemOP.getItem(imcp_id);
		channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertEquals(true, channel.startsWith("society"));
		assertEquals(false, channel.startsWith("notopic"));
		
		imcp_id = "88259962";
		oneItem = itemOP.getItem(imcp_id);
		channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertEquals(true, channel.startsWith("taiwan"));
		assertEquals(false, channel.startsWith("notopic"));
		
		imcp_id = "85513569";
		oneItem = itemOP.getItem(imcp_id);
		channel = channelsParser.getInstance(
				ItemOperation.ItemType.APPITEM).getTransChannelByItem(oneItem,
				2);
		System.out.println(oneItem.getUrl());
		System.out.println(channel);
		System.out.println(oneItem.getChannel());
		assertEquals(true, channel.startsWith("ladys"));
		assertEquals(false, channel.startsWith("fashion"));

	}

	@Ignore
	public void testUrlParse() {
		// fail("Not yet implemented");
	}

}
