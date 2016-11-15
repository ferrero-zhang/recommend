package com.ifeng.iRecommend.dingjw.dataCollection;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

public class ItemToHbaseTest0 extends TestCase{
	private static ItemToHbase it;
	@Before
	public void setUp() throws Exception {
		 it = new ItemToHbase();
	}
//	@Ignore
//	public void testadd()
//	{
//		HashMap<String, String> itemInfoMap = new HashMap<String, String>();
//		itemInfoMap.put("title", "testwritetoikv");
//		itemInfoMap.put("documentid","testwritetoikv");
//		itemInfoMap.put("url","testwritetoikv");
//		itemInfoMap.put("keywords","testwritetoikv");
//		itemInfoMap.put("source","testwritetoikv");
//		itemInfoMap.put("content","testwritetoikv");
//		itemInfoMap.put("date","testwritetoikv");
//		itemInfoMap.put("channel","testwritetoikv");
//		itemInfoMap.put("other","testothertojson");
//		Item oneItem = new Item(itemInfoMap);
//		System.out.println(oneItem.getContent());
//		try{
//			oneItem.setUrl(itemInfoMap.get("url"));
//			oneItem.setKeywords(itemInfoMap.get("keywords"));
//			oneItem.setID(itemInfoMap.get("documentid"));
//			oneItem.setAuthor(itemInfoMap.get("source"));
//			oneItem.setContent(itemInfoMap.get("content"));
//			oneItem.setDate(itemInfoMap.get("date"));
//			oneItem.setTitle(itemInfoMap.get("title"));
//			oneItem.setChannel(itemInfoMap.get("channel"));
//			oneItem.setOther(itemInfoMap.get("other"));
//		}
//		catch(Exception e)
//		{
//			System.out.println("[Warn writeItem] some of the  is missing.");
//		}
//		String json = "";
//		try {
//			json = JsonUtils.toJson(oneItem, Item.class);
//			System.out.println(json);
//		} catch (Exception e) {
//			return;
//		}
//	}
//	@Ignore
//	public void testXmlOpen() {
//		//不存在的路径测试
//		String filename1 = "/0509/00/82686977.xml";
//		StringBuffer sb = it.xmlOpen(filename1);
//		assertEquals("Result of HashMap is wrong!",null,sb);
//		
//		//正确的路径测试
//		String filename2 = "/0508/00/82686978.xml";
//		String fileContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><root><flag>1</flag><cat>1</cat><id>82686978</id><title>国开行助力青海棚户区改造</title><keywords>棚户区改造 国开行 助力</keywords><wwwurl>http://finance.ifeng.com/a/20140508/12284036_0.shtml</wwwurl><classid>10000031</classid><updatetime></updatetime><source>中国证券报</source><message><![CDATA[　　青海省人民政府与国家开发银行7日在京举行高层会谈并签署《全面推进棚户区改造等重点领域合作备忘录》。国家开发银行行长郑之杰指出，备忘录明确双方将在棚户区改造、战略性新兴产业、产业转型升级、战略通道建设等5个重点方面开展合作，并以省级平台统贷模式推动棚户区改造建设。(陈莹莹)<br/>]]></message><createtime>2014-05-08 00:24:53</createtime></root>";
//		assertNotNull("Input is null!", filename2);		
//		sb = it.xmlOpen(filename2);
//		assertEquals("Result of sb is wrong!",fileContent,sb.toString());		
//	}
//
//	@Ignore
//	public void testPretreatmentOfXml() {
//		StringBuffer sb = null;
//		
//		//错误的输入null
//		List<String> list = it.pretreatmentOfXml(sb);
//		assertNull("Wrong output is not null!", list);	
//		
//		//错误的输入""
//		sb = new StringBuffer();
//		sb.append("");
//		assertNull("Wrong output is not null!", list);
//		//正确的输入
//		String fileContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><root><flag>1</flag><cat>1</cat><id>82686978</id><title>国开行助力青海棚户区改造</title><keywords>棚户区改造 国开行 助力</keywords><wwwurl>http://finance.ifeng.com/a/20140508/12284036_0.shtml</wwwurl><classid>10000031</classid><updatetime></updatetime><source>中国证券报</source><message><![CDATA[　　青海省人民政府与国家开发银行7日在京举行高层会谈并签署《全面推进棚户区改造等重点领域合作备忘录》。国家开发银行行长郑之杰指出，备忘录明确双方将在棚户区改造、战略性新兴产业、产业转型升级、战略通道建设等5个重点方面开展合作，并以省级平台统贷模式推动棚户区改造建设。(陈莹莹)<br/>]]></message><createtime>2014-05-08 00:24:53</createtime></root>";
//		sb.append(fileContent);
//		list = it.pretreatmentOfXml(sb);
//		
//		//返回list的大小测试
//		assertEquals("Size of list is wrong!",3,list.size());
//		String res0 = "国开行_nz 助力_k 青海_ns 棚户区_n 改造_v ";
//		String res1 = " 　_w 　_w 青海省_ns 人民政府_l 与_p 国家_n 开发银行_nz 7日_mq 在_p 京_n 举行_v 高层_h 会谈_v 并_d 签署_v 《_w 全面_n 推进_v 棚户区_n 改造_v 等_u 重点_n 领域_n 合作_v 备忘录_e 》_w 。_w 国家_n 开发银行_nz 行长_n 郑之杰_nr 指出_v ，_w 备忘录_e 明确_h 双方_n 将_d 在_p 棚户区_n 改造_v 、_w 战略性_n 新兴产业_nz 、_w 产业_n 转型_v 升级_v 、_w 战略_k 通道_n 建设_v 等_u 5个_mq 重点_h 方面_n 开展_v 合作_v ，_w 并_h 以_p 省级_n 平台_n 统_k 贷_k 模式_n 推动_v 棚户区_n 改造_v 建设_v 。_w (_w 陈莹莹_nr )_w  _w ";
//		String res2 = "改造_v 棚户区改造 国开行_nz 助力_k 棚户区_n ";
//		
//		//返回list内容测试
//		assertEquals("Result of list.get(0) is wrong!",res0,list.get(0));
//		assertEquals("Result of list.get(1) is wrong!",res1,list.get(1));
//		assertEquals("Result of list.get(2) is wrong!",res2,list.get(2));
//	}

//	@Ignore
//	public void testParseXmlForInfo() {
//		List<String> list = null;
//		String fileContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><root><flag>1</flag><cat>1</cat><id>82686978</id><title>国开行助力青海棚户区改造</title><keywords>棚户区改造 国开行 助力</keywords><wwwurl>http://finance.ifeng.com/a/20140508/12284036_0.shtml</wwwurl><classid>10000031</classid><updatetime></updatetime><source>中国证券报</source><message><![CDATA[　　青海省人民政府与国家开发银行7日在京举行高层会谈并签署《全面推进棚户区改造等重点领域合作备忘录》。国家开发银行行长郑之杰指出，备忘录明确双方将在棚户区改造、战略性新兴产业、产业转型升级、战略通道建设等5个重点方面开展合作，并以省级平台统贷模式推动棚户区改造建设。(陈莹莹)<br/>]]></message><createtime>2014-05-08 00:24:53</createtime></root>";
//		StringBuffer sb = new StringBuffer();
//		sb.append(fileContent);
//		HashMap<String, String> hm = null;
//		hm = new HashMap<String, String>();
//		
//		//错误的输入  list == null
//		try {
//			hm = ItemToHbase.ParseXmlForInfo(list, sb);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		assertNull("Wrong output is not null!", hm);
//		
//		//错误的输入  list.size() != 3
//		list = new ArrayList<String>();
//		try {
//			hm = ItemToHbase.ParseXmlForInfo(list, sb);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		assertNull("Wrong output is not null!", hm);
//		
//		//正确的输入
//		String res0 = "国开行_nz 助力_k 青海_ns 棚户区_n 改造_v ";
//		String res1 = " 　_w 　_w 青海省_ns 人民政府_l 与_p 国家_n 开发银行_nz 7日_mq 在_p 京_n 举行_v 高层_h 会谈_v 并_d 签署_v 《_w 全面_n 推进_v 棚户区_n 改造_v 等_u 重点_n 领域_n 合作_v 备忘录_e 》_w 。_w 国家_n 开发银行_nz 行长_n 郑之杰_nr 指出_v ，_w 备忘录_e 明确_h 双方_n 将_d 在_p 棚户区_n 改造_v 、_w 战略性_n 新兴产业_nz 、_w 产业_n 转型_v 升级_v 、_w 战略_k 通道_n 建设_v 等_u 5个_mq 重点_h 方面_n 开展_v 合作_v ，_w 并_h 以_p 省级_n 平台_n 统_k 贷_k 模式_n 推动_v 棚户区_n 改造_v 建设_v 。_w (_w 陈莹莹_nr )_w  _w ";
//		String res2 = "改造_v 棚户区改造 国开行_nz 助力_k 棚户区_n ";
//		list.add(res0);
//		list.add(res1);
//		list.add(res2);
//		//HashMap<String, String> hm = null;
//		//hm = new HashMap<String, String>();
//		try {
//			hm = ItemToHbase.ParseXmlForInfo(list, sb);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		//HashMap 的大小测试
//		assertEquals("Number of HashMap is wrong!",9,hm.size());
//		
//		//HashMap 的内容测试
//		assertEquals("Result of parse URL is wrong!","http://finance.ifeng.com/stock/",hm.get("channel"));
//		
//	}
	@Test
	public void testWriteItem()
	{
		String tableName = fieldDicts.appItemTableNameInHbase;
		HashMap<String,String> hm = new HashMap<String,String>();
		hm.put("url", null);
		hm.put("keyword", "这是 测试 的 id");
		hm.put("documentid", "00000000");
		hm.put("content", "这个是用来测试的id");
		hm.put("source", "程序猿");
		hm.put("title", "测试的id");
		hm.put("date", "");
		hm.put("channel", null);
		hm.put("other", "pic=0|0|1|!|imgNum=5");
		
		int result = ItemToHbase.writeItem(tableName,hm);
		
		System.out.println(result);
		
		ItemOperation itemOP = ItemOperation.getInstance();
		itemOP.setItemType(ItemType.APPITEM);
		String id = "00000000";
		
		Item oneItem = itemOP.getItemFromHbase(id);
		//Item oneItem = itemOP.get(url);
		System.out.println("title "+oneItem.getTitle());
		System.out.println("ID "+oneItem.getID());
		System.out.println("content "+oneItem.getContent());
		System.out.println("keyword "+oneItem.getKeywords());
		System.out.println("url "+oneItem.getUrl());
		System.out.println("date "+oneItem.getDate());
		System.out.println("channel "+oneItem.getChannel());
		System.out.println("author "+oneItem.getAuthor());
		System.out.println("other "+oneItem.getOther());
		System.out.println("--------");
	}
//	@Ignore
//	public void test2ParseXmlForInfo()
//	{
//		ItemToHbase cItemToHbase = new ItemToHbase();
//		List<String> list = new ArrayList<String>();
//		String fileContent = "<?xml version='1.0' encoding='utf-8'?><root><flag>1</flag><cat>2</cat><id>93707868</id><title>✿开车不摸奶,摸奶不开车!不要脸,要命么?</title><keywords></keywords><wwwurl>http:/cdn.iclient.ifeng.com/res/article/32b/578/8d8/236505.html</wwwurl><classid>20068964</classid><updatetime></updatetime><source>财富人生</source><message><![CDATA[<img src='http://d.ifengimg.com/mw480/y0.ifengimg.com/ifengiclient/ipic/20141222/weixin_467_XL4T9iaPIKLVNj3jMzRHibfv9RxexI4DpdF6xiaEVYCmHJWaUzciayibFIfbpBn6RhcP5nvwXgVJu9ph2ICVUSIwkbg_w640_h360.jpg' whalt='480-270'/><br/><img src='http://d.ifengimg.com/mw480/y0.ifengimg.com/ifengiclient/ipic/20141222/weixin_467_XL4T9iaPIKLWB0zV6ibvu9EZYestdJUdW9tG68Fv1twLWngRSsXvhys8DabKnYCID1WhAcqsKves9KvvCe2bmUicg_w855_h122.jpg' whalt='480-68' onerror='this.parentNode.removeChild(this)'/><br/><strong>财富人生</strong><strong>ID</strong><strong>：</strong><strong><strong>hvovo_com</strong></strong>关注“财富人生”，助你创造人生财富。创富很简单，只是你暂时还不知道方法。关注我们将免费赠送《价值39万的一堂课》！<br/>商务合作：QQ907089900<br/><p style=''><strong>绳命诚可贵，淫欲要不得！</strong><br/>【找准平台,财富自来,关注财富商机微信号：CF1797】<br/><strong>点击阅读原文，你会爽翻的</strong><img src='http://d.ifengimg.com/mw480/y0.ifengimg.com/ifengiclient/ipic/20141222/weixin_467_qoxI9NQQfn6QK1iacKicYDPJuTs66wQMUQLIMzPm3YxTda06yibsbkq0SiafQvMwrKhISzuTN5wfjBSNgyBMq3cgTQ_w48_h38.gif' whalt='48-38' onerror='this.parentNode.removeChild(this)'/><br/>]]></message><createtime>2014-12-22 08:09:09</createtime><channelurl></channelurl><other>pic=1|1|0</other></root>";
//		
//		StringBuffer sb = new StringBuffer();
//		sb.append(fileContent);
//		HashMap<String, String> hm = null;
//		hm = new HashMap<String, String>();
////		System.out.println(list.get(0));
//		list = cItemToHbase.pretreatmentOfXml(sb);
//		System.out.println("list.size is "+list.size());
//		for(String e : list)
//		{
//			System.out.println("list is "+e);
//		}
//		//HashMap<String, String> hm = null;
//		//hm = new HashMap<String, String>();
//		try {
//			hm = ItemToHbase.ParseXmlForInfo(cItemToHbase.pretreatmentOfXml(sb), sb);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
////		System.out.println(hm.get("other").split("channel=")[1]);
////		String a = hm.get("other").split("channel=")[1];
////		System.out.println(a.split("\\|!\\|")[0]);
//		for(Entry<String,String> entry : hm.entrySet())
//		{
//			System.out.println(entry.getKey()+" : "+entry.getValue());
//		}
//	}
//	@Ignore
//	public	void testwirteItemToIKV() {
//		String fileContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><root><flag>1</flag><cat>1</cat><id>82686978</id><title>testwritetoikv</title><keywords>testwritetoikv</keywords><wwwurl>http://finance.ifeng.com/a/20140508/12284036_0.shtml</wwwurl><classid>10000031</classid><updatetime></updatetime><source>中国证券报</source><message><![CDATA[　　青海省人民政府与国家开发银行7日在京举行高层会谈并签署《全面推进棚户区改造等重点领域合作备忘录》。国家开发银行行长郑之杰指出，备忘录明确双方将在棚户区改造、战略性新兴产业、产业转型升级、战略通道建设等5个重点方面开展合作，并以省级平台统贷模式推动棚户区改造建设。(陈莹莹)<br/>]]></message><createtime>2014-05-08 00:24:53</createtime></root>";
//		StringBuffer sb = new StringBuffer();
//		sb.append(fileContent);
//		//正确的输入
//		List<String> list = new ArrayList<String>();	
//		HashMap<String, String> itemInfoMap = new HashMap<String, String>();
//		itemInfoMap.put("title", "testwritetoikv");
//		itemInfoMap.put("documentid","testwritetoikv");
//		itemInfoMap.put("url","testwritetoikv");
//		itemInfoMap.put("keywords","testwritetoikv");
//		itemInfoMap.put("source","testwritetoikv");
//		itemInfoMap.put("content","testwritetoikv");
//		itemInfoMap.put("date","testwritetoikv");
//		itemInfoMap.put("channel","testwritetoikv");
//		itemInfoMap.put("other","testwritetoikv");
//	
//		int flag = ItemToHbase.writeItemToIKV(itemInfoMap);
//		System.out.println(flag);
//		
//	}
}
