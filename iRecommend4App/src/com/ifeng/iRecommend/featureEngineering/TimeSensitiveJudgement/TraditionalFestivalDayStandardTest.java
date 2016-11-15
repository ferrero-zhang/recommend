package com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TraditionalFestivalDayStandardTest {

//	private TraditionalFestivalDayStandard traditionalFestivalDayStandard = null;
//	@Before
//	public void setUp() throws Exception {
//		System.out.println("set up");
//        // 生成成员变量的实例
//		traditionalFestivalDayStandard = new TraditionalFestivalDayStandard();
//	}
//
//	@After
//	public void tearDown() throws Exception {
//		System.out.println("tear down");
//	}

	@Test
	public void testFestivalNormalization() {
		
		TraditionalFestivalDayStandard traditionalFestivalDayStandard = new TraditionalFestivalDayStandard();
		
		Assert.assertEquals("2016-11-24", traditionalFestivalDayStandard.festivalNormalization(2016, "感恩节"));
		Assert.assertEquals("2016-12-25", traditionalFestivalDayStandard.festivalNormalization(2016, "圣诞节"));
		Assert.assertEquals("2016-02-14", traditionalFestivalDayStandard.festivalNormalization(2016, "情人节"));
		Assert.assertEquals("2015-06-21", traditionalFestivalDayStandard.festivalNormalization(2015, "父亲节"));
		Assert.assertEquals("2015-02-19", traditionalFestivalDayStandard.festivalNormalization(2015, "春节"));
		Assert.assertEquals("2015-03-20 __ 2015-03-21", traditionalFestivalDayStandard.festivalNormalization(2015, "春分"));
		Assert.assertEquals("找不到指定节日", traditionalFestivalDayStandard.festivalNormalization(2016, "xx节"));
	
	}

}
