package com.ifeng.iRecommend.likun.rankModel;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.featureEngineering.itemf;

public class rankModelTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Ignore
	/*
	 * 人工检验各个字段的正误，尤其注意date格式，以及有无item的docType、other字段
	 */
	public void testGetFrontItemfsFromPool() {
		rankModel rm = rankModel.getInstance();
		ArrayList<itemf> al_itemfs = rm.getFrontItemfsFromPool("recommend");
		for(itemf item:al_itemfs){
			String itemStr = JsonUtils.toJson(item);
			assertTrue(item.getOther().length() > 0);
			assertTrue(item.getDocType().length() > 0);
			System.out.println(itemStr);
		}
		
		
	}
	
	@Test
	/*
	 * 人工检验各个字段的正误，尤其注意date格式，以及有无item的docType、other字段
	 */
	public void testFilterItems() {

		rankModel rm = rankModel.getInstance();
		ArrayList<itemf> al_itemfs = rm.getFrontItemfsFromPool("recommend");
		System.out.println("al_itemfs Size: "+al_itemfs.size());
		RankListNew rankList = new RankListNew();
		ArrayList<RankItemNew> RankItemNewListFromFront = rm.filterItems(rankList , al_itemfs);
		System.out.println("RankItemNewListFromFront Size: "+RankItemNewListFromFront.size());
		// 输出所有的rankitem，检验其重点字段：date\other\lifetime\category等等
		for(RankItemNew r_item:RankItemNewListFromFront){
			String jsonStr = JsonUtils.toJson(r_item);
			System.out.println(jsonStr);
		}
	}

}
