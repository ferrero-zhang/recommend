package com.ifeng.iRecommend.zhanzh.newHotPredictTest;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.newHotPredict.HotRankItem;
import com.ifeng.iRecommend.zhanzh.newHotPredict.IfengPCVisualInfoCrawler;

public class IfengPCVisualInfoCrawlerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Ignore
	public void testGetIfengSportsPageHotList() {
		List<HotRankItem> list = IfengPCVisualInfoCrawler.getIfengSportsPageHotList();
		Gson gson = new Gson();
		String str = gson.toJson(list);
		System.out.println(str);
	}
	
	@Ignore
	public void testGetIfengMainpageHotList() {
		List<HotRankItem> list = IfengPCVisualInfoCrawler.getIfengMainpageHotList(4);
		Gson gson = new Gson();
		String str = gson.toJson(list);
		System.out.println(str);
	}
	
	@Test
	public void testGetIfengPCnewsList() {
		List<HotRankItem> list = IfengPCVisualInfoCrawler.getIfengPCnewsList();
		Gson gson = new Gson();
		String str = gson.toJson(list);
		System.out.println(str);
	}

}
