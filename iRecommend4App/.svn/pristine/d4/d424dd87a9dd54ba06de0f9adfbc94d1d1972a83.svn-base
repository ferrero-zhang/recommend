package com.ifeng.iRecommend.likun.hotpredict;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.iRecommend.dingjw.front_rankModel.appItemFront;
import com.ifeng.iRecommend.dingjw.front_rankModel.appRankModel;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

public class heatPredictTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		fieldDicts.appTreeMappingFile = "D:/workspace/iRecommend4App/testenv/AppTreeMapping.txt";
		fieldDicts.frontAppTreeMappingFile = "D:/workspace/iRecommend4App/testenv/APPFront_TreeMapping.txt";
		fieldDicts.pcVisualHotFile = "/projects/zhineng/pcHotPredict/pcHotLevel";
		
	}

	@Ignore
	public void testReadTongjiSF() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testupdateSFRankLib() {
//		// 获取客户端文章的实时pv
//		LinkedHashMap<String, Integer> idPvMap = heatPredict.readTongjiApp();
//
//		// 获取存放在redis的编辑推荐的items
//		ArrayList<appItemFront> appitemList = appRankModel.getInstance()
//				.getAppItemsFromPool("recommend", idPvMap);
//		System.out.println("appitemList from pool size: " + appitemList.size());
//
//		for (appItemFront item : appitemList) {
//			System.out.println("appitemList before rank.");
//			System.out.println(item.getTitle() + ": " + item.getWeight());
//		}
//		System.out.println("---------------------");

		heatPredict hp = heatPredict.getInstance();
		hp.updateSFRankLib();

//		for (appItemFront item : appitemList) {
//			System.out.println("appitemList after rank.");
//			System.out.println(item.getTitle() + ": " + item.getWeight());
//		}
	}
	
	@Ignore
	public void testupdateAPPRankLib() {
//		// 获取客户端文章的实时pv
//		LinkedHashMap<String, Integer> idPvMap = heatPredict.readTongjiApp();
//
//		// 获取存放在redis的编辑推荐的items
//		ArrayList<appItemFront> appitemList = appRankModel.getInstance()
//				.getAppItemsFromPool("recommend", idPvMap);
//		System.out.println("appitemList from pool size: " + appitemList.size());
//
//		for (appItemFront item : appitemList) {
//			System.out.println("appitemList before rank.");
//			System.out.println(item.getTitle() + ": " + item.getWeight());
//		}
//		System.out.println("---------------------");

		heatPredict hp = heatPredict.getInstance();
		hp.updateAPPRankLib();

//		for (appItemFront item : appitemList) {
//			System.out.println("appitemList after rank.");
//			System.out.println(item.getTitle() + ": " + item.getWeight());
//		}
	}
	
	@Ignore
	public void testrankItemBySFLib() {
//		heatPredict hp = heatPredict.getInstance();
//		hp.updateSFRankLib();
//		String imcp_id = "85790606";
//		System.out.println(hp.rankOneItemHotLevel(imcp_id));
//		imcp_id = "85767476";
//		System.out.println(heatPredict.getInstance().rankOneItemHotLevel(imcp_id));
//		imcp_id = "85823150";
//		System.out.println(heatPredict.getInstance().rankOneItemHotLevel(imcp_id));
//		imcp_id = "85818134";
//		System.out.println(hp.rankOneItemHotLevel(imcp_id));
//		imcp_id = "85778926";
//		System.out.println(heatPredict.getInstance().rankOneItemHotLevel(imcp_id));
//		imcp_id = "77852528";
//		System.out.println(heatPredict.getInstance().rankOneItemHotLevel(imcp_id));
	}

	@Test
	public void testupdatePCVisualRankLib() {
		heatPredict hp = heatPredict.getInstance();
		hp.updatePCVisualRankLib();
	}
	
}
