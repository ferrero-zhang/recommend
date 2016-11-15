package com.ifeng.iRecommend.zhanzh.newHotPredictTest;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.newHotPredict.HeatPredictUtils;
import com.ifeng.iRecommend.zhanzh.newHotPredict.HotRankItem;

public class HeatPredictUtilsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Ignore
	public void testGetInstance() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testUpdate() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testSetCommentsNum() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testSetHackerNews() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testGetItemfChannel() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testSearchItemf() {
		fail("Not yet implemented");
	}
	@Test
	public void testGetHotRankItemList(){
		HeatPredictUtils util = HeatPredictUtils.getInstance();
//		util.updateHotList();
		List<HotRankItem> list = util.getHotRankItemList();
//		Gson gson = new Gson();
//		String json = gson.toJson(list);
//		System.out.println(json);
		for(HotRankItem hot : list){
			System.out.println(hot.getTitle());
			System.out.println(hot.getCommentsNum());
		}
	}

}
