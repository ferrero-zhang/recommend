package com.ifeng.iRecommend.zhanzh.SolrUtil;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;

public class SearchItemsFromSolrTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Ignore
	public void testSearchFrontItemFromSolr() {
		List<String> test = SearchItemsFromSolr.searchFrontItemFromSolr("篮球");
		Gson gson =new Gson();
		String str = gson.toJson(test);
		System.out.println(str);
	}
	
	@Ignore
	public void testSearchItemIsAvailable() {
		boolean test = SearchItemsFromSolr.searchItemIsAvailable("149846");

		System.out.println(test);
	}
	
	@Test
	public void testSearchItem2appJsonById() {
		String json = SearchItemsFromSolr.searchItem2appJsonById("149846");
		Gson gson = new Gson();
		FrontNewsItem fitem = gson.fromJson(json, FrontNewsItem.class);
		
		System.out.println(fitem.getReadableFeatures());
	}
}
