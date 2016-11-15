package com.ifeng.iRecommend.featureEngineering;

import static org.junit.Assert.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.reidx.SplitWordClient;

public class CMPPItemToIKVAndRedisTest
{
	CMPPItemToIKVAndRedis ob = new CMPPItemToIKVAndRedis(0,0);
	@Before
	public void setUp() throws Exception
	{
	}
	@Ignore
	public void testFilterHtml() throws IOException
	{
//		String url = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_187.jhtml?startTime=2015-04-27T20:27:00Z&endTime=2015-04-27T20:39:00Z";
//		List<JsonFromCMPP> list = CMPPDataCollect.readJson(url);
////		String str = CMPPDataCollect.downloadPage(url);
////		System.out.println(str);
////		FileWriter fw = null;
////		fw = new FileWriter("D:\\data\\testcounttt",true);
//		for(JsonFromCMPP e : list)
//		{
////			fw.write(e.getContent());
//			
//			System.out.println(e.getContent());
//			String str2 = ob.filterHtml(e.getContent());
////			fw.write(str2);
//			System.out.println(str2);
//			String textSplited = new String(SplitWordClient.split(str2, null).replace("(/", "_").replace(") ", " "));
////			fw.write(textSplited);
//			itemf item = new itemf();
//			item.setID("test");
//			item.setSplitContent(textSplited);
//			String json = "";
//			try
//			{
//				json = JsonUtils.toJson(item, itemf.class);
//			}
//			catch (Exception x)
//			{
////				LOG.error("[ERROR] Error occurred when change item to Json. Id is " + item.getID(), x);
//				return;
//			}
//			System.out.println(json);
//		}
//		fw.close();
//		String s = "<p>凤凰汽车讯 在去年北京车展期间，力帆汽车发布了旗下全新旗舰车款力帆820。日前，我们从官方了解到，力帆820定于6月8日正式上市(之前报道该车有望6月10日上市)，新车预计售价9-12万元。</p><p align=\"center\" class=\"img-c\"><a href=\"#1\"><img src=\"http://a0.ifengimg.com/autoimg/74/68/2006874_8.jpeg\" style=\"border-top: #2f2f2f 1px solid; border-right: #2f2f2f 1px solid; border-bottom: #2f2f2f 1px solid; border-left: #2f2f2f 1px solid\" onerror=\"javascript:defaultimage(this)\" alt=\"\" /></a></p><p align=\"center\" class=\"pictext\">力帆820</p><p align=\"center\" class=\"img-c\"><a href=\"#2\"><img src=\"http://a0.ifengimg.com/autoimg/71/68/2006871_8.gif\" style=\"border-top: #2f2f2f 1px solid; border-right: #2f2f2f 1px solid; border-bottom: #2f2f2f 1px solid; border-left: #2f2f2f 1px solid\" onerror=\"javascript:defaultimage(this)\" alt=\"\" /></a></p><p>作为力帆旗舰车型，力帆820相比之前力帆车型在造型设计上确实达到了新的高度，车身比例也更加协调舒展。同时在细节方面，新车融入了更多流行元素，大灯的设计也很显档次。车身尺寸方面，力帆820的长宽高分别为4865/1835/1480mm，轴距为2775mm。配置方面，新车配备了定速续航、自动恒温空调、一键启动、倒车影像、GPS导航、前排座椅加热等。</p><p align=\"center\" class=\"img-c\"><a href=\"#3\"><img src=\"http://a0.ifengimg.com/autoimg/94/68/2006894_8.jpg\" style=\"border-top: #2f2f2f 1px solid; border-right: #2f2f2f 1px solid; border-bottom: #2f2f2f 1px solid; border-left: #2f2f2f 1px solid\" onerror=\"javascript:defaultimage(this)\" alt=\"\" /></a></p><p class=\"jj_60\">动力方面，力帆820将提供1.8L、2.4L和2.0T三种发动机可选，其中搭载1.8L发动机的车型将配备手动变速箱，搭载2.4L和2.0T发动机的车型则将匹配6速自动变速器。</p>\"";
		String s = "";
		List<String> picUrl = ob.findPicUrl(s);
		String str2 = ob.filterHtml(s);
		System.out.println(str2);
		String textSplited = new String(SplitWordClient.split(str2, null).replace("(/", "_").replace(") ", " "));
		System.out.println(textSplited);
		//#_w p_nx #_w
		for(int i = 0; i < picUrl.size(); i++)
		{
			textSplited = textSplited.replaceFirst("#_w p_nx #_w", picUrl.get(i));
		}
		System.out.println(textSplited);
		
	}
	
	@Ignore
	public void testFilterString()
	{
		String s = "\\\\' asd";
		String result = ob.filterString(s);
		System.out.println(result);
	}
	
	@Ignore
	public void testFl()
	{
		String s = "<p>凤凰汽车讯 在去年北京车展期间，力帆汽车发布了旗下全新旗舰车款力帆820。日前，我们从官方了解到，力帆820定于6月8日正式上市(之前报道该车有望6月10日上市)，新车预计售价9-12万元。</p><p align=\"center\" class=\"img-c\"><a href=\"#1\"><img src=\"http://a0.ifengimg.com/autoimg/74/68/2006874_8.jpg\" style=\"border-top: #2f2f2f 1px solid; border-right: #2f2f2f 1px solid; border-bottom: #2f2f2f 1px solid; border-left: #2f2f2f 1px solid\" onerror=\"javascript:defaultimage(this)\" alt=\"\" /></a></p><p align=\"center\" class=\"pictext\">力帆820</p><p align=\"center\" class=\"img-c\"><a href=\"#2\"><img src=\"http://a0.ifengimg.com/autoimg/71/68/2006871_8.jpg\" style=\"border-top: #2f2f2f 1px solid; border-right: #2f2f2f 1px solid; border-bottom: #2f2f2f 1px solid; border-left: #2f2f2f 1px solid\" onerror=\"javascript:defaultimage(this)\" alt=\"\" /></a></p><p>作为力帆旗舰车型，力帆820相比之前力帆车型在造型设计上确实达到了新的高度，车身比例也更加协调舒展。同时在细节方面，新车融入了更多流行元素，大灯的设计也很显档次。车身尺寸方面，力帆820的长宽高分别为4865/1835/1480mm，轴距为2775mm。配置方面，新车配备了定速续航、自动恒温空调、一键启动、倒车影像、GPS导航、前排座椅加热等。</p><p align=\"center\" class=\"img-c\"><a href=\"#3\"><img src=\"http://a0.ifengimg.com/autoimg/94/68/2006894_8.jpg\" style=\"border-top: #2f2f2f 1px solid; border-right: #2f2f2f 1px solid; border-bottom: #2f2f2f 1px solid; border-left: #2f2f2f 1px solid\" onerror=\"javascript:defaultimage(this)\" alt=\"\" /></a></p><p class=\"jj_60\">动力方面，力帆820将提供1.8L、2.4L和2.0T三种发动机可选，其中搭载1.8L发动机的车型将配备手动变速箱，搭载2.4L和2.0T发动机的车型则将匹配6速自动变速器。</p>\"";
//		String picUrl = ob.findPicUrl(s);
//		if(s == null || s.isEmpty())
//			return null;
//		String s = "defertg";
		List<String> picUrl = ob.findPicUrl(s);
		if(picUrl == null)
			System.out.println(picUrl);
//		System.out.println(picUrl.toString());
	}
	
	@Test
	public void testMergeFeature()
	{
		ArrayList<String> feature1 = new ArrayList<String>();
		ArrayList<String> feature2 = new ArrayList<String>();
////////正常测试////////////////////////////////////////////////
//		feature1.add("1abc");
//		feature1.add("c");
//		feature1.add("1");
//		feature1.add("1饮食");
//		feature1.add("sc");
//		feature1.add("0.9");
//		feature1.add("1减肥bobo站");
//		feature1.add("t");
//		feature1.add("0.9");
//		feature1.add("1bcd");
//		feature1.add("s");
//		feature1.add("0.9");
//		feature1.add("1疯狂减肥");
//		feature1.add("cn");
//		feature1.add("0.7");
//		feature1.add("1减肥");
//		feature1.add("kb");
//		feature1.add("0.7");
//		
//		feature2.add("2健康");
//		feature2.add("c");
//		feature2.add("0.8");
//		feature2.add("2健身");
//		feature2.add("sc");
//		feature2.add("0.7");
//		feature2.add("2加油");
//		feature2.add("cn");
//		feature2.add("0.8");
//		feature2.add("2励志");
//		feature2.add("t");
//		feature2.add("0.7");
//		feature2.add("2锻炼身体");
//		feature2.add("ks");
//		feature2.add("0.7");
		feature1.add("1旅游");
		feature1.add("c");
		feature1.add("1");
		feature1.add("1beijing");
		feature1.add("kr");
		feature1.add("0.5");
		feature2.add("房产");
		feature2.add("c");
		feature2.add("0.7");
		feature2.add("2锻炼身体");
		feature2.add("ks");
		feature2.add("0.7");
		List<String> feature3 = ob.mergeFeature(feature1, feature2);
		System.out.println(feature3.toString());
		
////////feature1初始值为null测试/////////////////////////////////////////
//		feature1 = null;
//		feature2.add("加油");
//		feature2.add("cn");
//		feature2.add("0.8");
//		feature2.add("励志");
//		feature2.add("t");
//		feature2.add("0.7");
//		List<String> feature3 = ob.mergeFeature(feature1, feature2);
//		System.out.println(feature3);   //null
		
////////feature2初始值为null测试/////////////////////////////////////////
//		feature1.add("abc");
//		feature1.add("c");
//		feature1.add("1");
//		feature1.add("bcd");
//		feature1.add("s");
//		feature1.add("0.9");
//		feature2 = null;
//		List<String> feature3 = ob.mergeFeature(feature1, feature2);
//		System.out.println(feature3.toString()); //[abc, c, 1, bcd, s, 0.9]
		
		
	}

	@Ignore
	public void testSolrCheck()
	{
		queryInterface instance = queryInterface.getInstance();
		itemf itemNow = instance.queryItemF("23693");
		try
		{
			itemf newItem = ob.solrCheck(itemNow);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
