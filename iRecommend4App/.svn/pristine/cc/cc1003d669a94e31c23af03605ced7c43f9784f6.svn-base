package com.ifeng.iRecommend.zhanzh.newHotPredictTest;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.newHotPredict.HotNewsPredict;
import com.ifeng.iRecommend.zhanzh.newHotPredict.HotRankItem;

public class HotNewsPredictTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Ignore
	public void testSportsHotNewsPredict() {
		List<HotRankItem> list = HotNewsPredict.SportsHotNewsPredict();
		for(HotRankItem hot : list){
			System.out.println(hot.getDocId());
			System.out.println(hot.getPublishTime());
			System.out.println(hot.getTitle());
			System.out.println(hot.getHotScore());
			System.out.println(hot.getHotLevel());
			System.out.println(hot.getWhy());
			System.out.println(hot.getCommentsNum());
			System.out.println(hot.getPv());
			System.out.println("======================");
		}
	}
	
	@Ignore
	public void testToutiaoHotNewsPredict() {
		List<HotRankItem> list = HotNewsPredict.toutiaoHotNewsPredict();
		for(HotRankItem hot : list){
			System.out.println(hot.getDocId());
			System.out.println(hot.getPublishTime());
			System.out.println(hot.getTitle());
			System.out.println(hot.getHotScore());
			System.out.println(hot.getHotLevel());
			System.out.println(hot.getWhy());
			System.out.println(hot.getCommentsNum());
			System.out.println(hot.getPv());
			System.out.println("======================");
		}
	}
	
	@Ignore
	public void testWhiteListPreloadHotpredict() {
		HashMap<String, List<HotRankItem>> tempmap = HotNewsPredict.whiteListPreloadHotpredict();
		Set<String> keySet = tempmap.keySet();
		for(String key : keySet){
			
			List<HotRankItem> list = tempmap.get(key);
			System.out.println("++++++++++++++"+key+"++++++++++++++++");
			
			for(HotRankItem hot : list){
				System.out.println(hot.getDocId());
				System.out.println(hot.getPublishTime());
				System.out.println(hot.getTitle());
				System.out.println(hot.getHotScore());
				System.out.println(hot.getHotLevel());
				System.out.println(hot.getWhy());
				System.out.println(hot.getCommentsNum());
				System.out.println(hot.getPv());
				System.out.println("======================");
			}
		}

	}
	
	@Test
	public void testLocalNewsHeatPredict() {
		HashMap<String, List<HotRankItem>> tempmap = HotNewsPredict.localNewsHeatPredict();
		Set<String> keySet = tempmap.keySet();
		for(String key : keySet){
			
			List<HotRankItem> list = tempmap.get(key);
			System.out.println("++++++++++++++"+key+"++++++++++++++++");
			
			for(HotRankItem hot : list){
				System.out.println(hot.getDocId());
				System.out.println(hot.getPublishTime());
				System.out.println(hot.getTitle());
				System.out.println(hot.getHotScore());
				System.out.println(hot.getHotLevel());
				System.out.println(hot.getWhy());
				System.out.println(hot.getCommentsNum());
				System.out.println(hot.getPv());
				System.out.println("======================");
			}
		}

	}

}
