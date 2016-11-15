package com.ifeng.iRecommend.featureEngineering;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TagsParserTest {

	@Before
	public void setUp() throws Exception {
	}

//	@SuppressWarnings("deprecation")
	@Test
	public void testMapTags() {
		TagsParser tagsParserOb = TagsParser.getInstance();
		String input = "source=spider||!|channel=news|!|tags=房产|!|qualitylevel=C";
		String sourceAlias="橘子娱乐";
		String title="高校食堂又推水果神菜，草莓炒西芹是什么鬼";
		//featureList=null;
		CMPPDataOtherField f = new CMPPDataOtherField(input);
		ArrayList<String> result = tagsParserOb.mapTags(f,sourceAlias,"slide");
		//ArrayList<String> result = tagsParserOb.parseTags("凤凰网资讯-社会", f);
		assertNotNull(result);
		System.out.println(result);
		
		title=null;
		input="channel=ent|!|tags=文化";
		f = new CMPPDataOtherField(input);
		result = tagsParserOb.mapTags(f,sourceAlias,"doc");
		//ArrayList<String> result = tagsParserOb.parseTags("凤凰网资讯-社会", f);
		assertNotNull(result);
		System.out.println(result);
		
		//含两个tag，第二个tag映射结果为topic
		input="source=spider|!|channel=ent|!||!|ref=第3届乌镇戏剧节|!|referurl=http://ent.ifeng.com/activities/special/3rd_wuzhenfestival/|!|lt=第3届乌镇戏剧节_活动频道_凤凰网";
		f = new CMPPDataOtherField(input);
		result = tagsParserOb.mapTags(f,sourceAlias,"");
		assertNotNull(result);
		System.out.println(result);
		
		input="source=spider|!|channel=ent|!|tags=财经||体育-图片-全景体坛";
		f = new CMPPDataOtherField(input);
		result = tagsParserOb.mapTags(f,sourceAlias,null);
		assertNotNull(result);
		System.out.println(result);
		
		
	}
}
