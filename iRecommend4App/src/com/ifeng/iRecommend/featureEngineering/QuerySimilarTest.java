package com.ifeng.iRecommend.featureEngineering;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.commen.Utils.JsonUtils;

public class QuerySimilarTest
{
	QuerySimilar query = QuerySimilar.getInstance();
	@Before
	public void setUp() throws Exception
	{
	}
	@Ignore
	public void testListJson()
	{
		List<String> testList = new ArrayList<String>();
		testList.add("word");
		testList.add("c");
		testList.add("1.0");
		String value = JsonUtils.toJson(testList);
		System.out.println(value);
		List<String> resultList = JsonUtils.fromJson(value, ArrayList.class);
		for(String s : testList)
		{
			System.out.println(s);
		}
		System.out.println(resultList.toString());
	}
	@Ignore
	public void testQuerySimilar() throws IOException
	{
		List<String> featureList = null;
		boolean ifempty = false;
		
		featureList = query.querySimilar(null,"url","content","docType");
		assertNull(featureList);
		
		featureList = query.querySimilar("title","url","content","docType");
		ifempty = featureList.isEmpty();
		assertTrue(ifempty);
		
		featureList = query.querySimilar("title",null,null,null);
		ifempty = featureList.isEmpty();
		assertTrue(ifempty);
		
		featureList = query.querySimilar("科比会面阿德谈话曝光",null,"北京时间7月3日，在湖人与拉马库斯-阿尔德里奇的第一次会面中，科比-布莱恩特曾与他交谈过3分钟。那么科比究竟说了些什么呢？\nBleacher Report记者Kevin Ding撰写了一篇文章，披露了科比在会面中与阿德的交谈内容。\n科比告诉阿德，在2008年保罗-加索尔来到湖人之前，他一度对球队失去了信心，他认为球队再也没有夺冠的希望了，但加索尔的到来让自己惊喜万分，球队也在2009年和2010年连续两年登顶NBA。\n科比还提到了那笔被“篮球原因”否决的有关克里斯-保罗加盟湖人的交易，以此来称赞总经理米奇-库普切克在建队方面的才华。\n科比说，湖人试图交易来保罗并不仅仅是为夺冠铺平道路，球队在那笔交易中会剩下一大笔钱，他们曾想借此来进行一笔更大的运作。\n尽管最后交易流产，但科比想借此来告诉阿德，库普切克和湖人会尽可能帮助自己找到想要的帮手。\n对于科比的言论，文章作者Kevin Ding评价称，湖人的重点依然是试图以未来吸引阿德，并且肯定了自己辉煌的过去。而其实湖人明白，球队目前距离总冠军还差很远，这样的一支球队显然是阿尔德里奇不愿加盟的。",null);
		String feature = featureList.toString();
		assertEquals(feature,"[篮球, c, 1, nba, sc, 1, 湖人, cn, -1, 科比, et, 1, 湖人队, et, 0.5, 阿尔德里奇, et, 0.1, 保罗, et, 0.1, 加索尔, et, 0.1, 阿德, kr, 0.5]");
	}

}
