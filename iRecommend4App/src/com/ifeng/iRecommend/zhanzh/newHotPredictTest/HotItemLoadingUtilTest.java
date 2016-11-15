package com.ifeng.iRecommend.zhanzh.newHotPredictTest;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.newHotPredict.HotItemLoadingUtil;
import com.ifeng.iRecommend.zhanzh.newHotPredict.HotRankItem;

public class HotItemLoadingUtilTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Ignore
	public void testGetInstance() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testUpdateAll() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testUpdateIfengSportsPageHotList() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testUpdateIfengMainPageHotList() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testUpdateHackerHotList() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testGetHotRankList() {
		HotItemLoadingUtil util = HotItemLoadingUtil.getInstance();
//		util.updateIfengMainPageHotList();
//		util.updateIfengPCnews();
		util.updateIfengMainPageHotList();
		List<HotRankItem> list = util.getHotRankList(HotItemLoadingUtil.IfengMainpageYaowen);
		int i=0;
		for(HotRankItem hot : list){
			if(hot.getItem() != null){
				System.out.println(i+" : "+hot.getTitle());
				i++;
			}
			
		}
//		Gson gson = new Gson();
//		String result = gson.toJson(list);
//		System.out.println(result);
	}
	
	@Test
	public void testPreloadHighQualityChannelList() {
		HotItemLoadingUtil util = HotItemLoadingUtil.getInstance();
		util.preloadHighQualityChannelList();
	}

}
