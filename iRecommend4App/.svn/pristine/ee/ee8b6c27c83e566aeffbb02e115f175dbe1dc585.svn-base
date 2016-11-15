package com.ifeng.iRecommend.featureEngineering;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;

public class CMPPDataCollectTest
{

	@Before
	public void setUp() throws Exception
	{
	}
	@Ignore
	public void testJsonVert()
	{

		Gson gson = new Gson();
//		String s = "{\"title\":\"伊能静素颜卖萌遭讽刺“老了” 回呛：比少女还少女\",\"subTitle\":\"\",\"sourceAlias\":\"凤凰娱乐\",\"sourceLink\":\"http://ent.ifeng.com/a/20150330/42355796_0.shtml\",\"keywords\":\"\",\"author\":\"\",\"other\":\"source=spider|!|channel=ent|!|tags=凤凰网娱乐-明星-港台\",\"description\":\"\",\"summary\":\"46岁的伊能静29日凌晨在微博上表示，长大是更有能力的单纯，“我们不用再为生活卑微、不用迁就他人。”疑似透露只想做自己，不要因为外界的眼光而退缩。此外，她提到年轻时若因现实生活而苍老，老了的时候就应该比少女还少女，发文的时间点刚好是被网友批评“素颜萌照很老”之后，因此被怀疑是针对恶评的回应；不过，有粉丝把焦点放在她的文字上，直呼“写得太美了！”\",\"publishedTime\":\"2015-03-30 09:08:00\",\"content\":'伊能静与秦昊恩爱度蜜月伊能静与可爱小狗合影凤凰娱乐讯 伊能静21日和小10岁秦昊在泰国开心完婚，婚后至今却招来恶评不断，高调再婚被恶毒网友痛骂不顾儿子“小哈利”的感受，是个丢脸的妈妈。而她没有因此停止发文，甚至直言“我就是喜欢写字”，睡前还分享自拍萌照，没想到遭网友狠酸“老了”，她两小时后再度发文，疑似针对网上的谩骂做出回应。46岁的伊能静29日凌晨在微博上表示，长大是更有能力的单纯，“我们不用再为生活卑微、不用迁就他人。”疑似透露只想做自己，不要因为外界的眼光而退缩。此外，她提到年轻时若因现实生活而苍老，老了的时候就应该比少女还少女，发文的时间点刚好是被网友批评“素颜萌照很老”之后，因此被怀疑是针对恶评的回应；不过，有粉丝把焦点放在她的文字上，直呼“写得太美了！”'}";
		String s = "{\"title\":\"互联网概念股走势强 顺网科技等三股涨停\",\"subTitle\":\"\",\"sourceAlias\":\"凤凰财经综合\",\"sourceLink\":\"http://finance.ifeng.com/a/20150407/13611885_0.shtml\",\"keywords\":\"\",\"author\":\"\",\"other\":\"source=spider|!|channel=finance|!|tags=凤凰网财经-证券-凤凰报盘\",\"description\":\"\",\"summary\":\"凤凰财经讯 今日，互联网概念股走势强劲，截至10点32分，顺网科技<span id=\"sz300113_hq\"></span>、游族网络、暴风科技涨停，昆仑万维涨幅逾9%。\",\"publishedTime\":\"2015-04-07 10:36:00\",\"content\":\"凤凰财经讯 今日，互联网概念股走势强劲，截至10点32分，顺网科技、游族网络、暴风科技涨停，昆仑万维涨幅逾9%。\"}";
//		System.out.println("cmpplist length is "+cmpplist.length);

			JsonFromCMPP jsonData = null;
			try
			{
//				System.out.println(s);
				jsonData = gson.fromJson(s,JsonFromCMPP.class);
			}
			catch(Exception e)
			{
				System.out.println("Some error occoured in json analyse. "+s);
//				e.printStackTrace();
			}
//			System.out.println(jsonData.getOther());
	}
	@Ignore
	public void testReadJson()
	{
		String url = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_187.jhtml?startTime=2015-03-30T15:46:41Z&endTime=2015-03-30T15:55:40Z";
		List<JsonFromCMPP> jList = CMPPDataCollect.readJson(url);
		
		//System.out.println(jList.toString());
	}
	@Ignore
	public void testReadChannel()
	{
		String url = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_187.jhtml?startTime=2015-03-30T15:46:41Z&endTime=2015-03-30T15:55:40Z";
		List<JsonFromCMPP> jList = CMPPDataCollect.readJson(url);
		for(JsonFromCMPP j : jList)
		{
			
		}
		//System.out.println(jList.toString());
	}
	@Test
	public void testDownloadPage()
	{
		String url = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_187.jhtml?startTime=2015-03-30T15:46:41Z&endTime=2015-03-30T15:55:40Z";
		
		String str = CMPPDataCollect.downloadPage(url);
		System.out.println(str);
		
	}

	@Ignore
	public void testGetHtml()
	{
		fail("Not yet implemented");
	}

}
