package com.ifeng.iRecommend.zhanzh.keyWordsAssessment;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;

public class KeyWordsAssessmentTest {

//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {
//	}
//
//	@Ignore
//	public void testLoadingKeyWords() {
//		List<KeyWord> testList = KeyWordsAssessment.loadingKeyWords("D://keywordset.txt");
//		for(KeyWord word : testList){
//			System.out.println(word.name);
//			System.out.println(word.type);
//			System.out.println(word.hitDocNum);
//			System.out.println(word.isAvailable);
//		}
//	}
//	@Ignore
//	public void testSearchFrontItemFromSolrByKeyWord() {
//		List<String> doclist = KeyWordsAssessment.searchFrontItemFromSolrByKeyWord("娱乐");
//		Gson gson = new Gson();
//		System.out.println(gson.toJson(doclist));
//	}
//	
//	@Ignore
//	public void testCountUpdateNewsNumByDay() {
//		List<String> doclist = KeyWordsAssessment.searchFrontItemFromSolrByKeyWord("宇宙图片");
//		List<Integer> testList = KeyWordsAssessment.countUpdateNewsNumByDay(doclist);
//		Gson gson = new Gson();
//		System.out.println(gson.toJson(testList));
//	}
//	
//	@Ignore
//	public void testCountUpdateDocNumByStep() {
//		List<String> doclist = KeyWordsAssessment.searchFrontItemFromSolrByKeyWord("郎朗");
//		List<Integer> testList = KeyWordsAssessment.countUpdateNewsNumByDay(doclist);
//		List<Integer> test1list = KeyWordsAssessment.countUpdateDocNumByStep(testList, 3);
//		Gson gson = new Gson();
//		System.out.println(gson.toJson(test1list));
//	}
//	@Test
//	public void testCountAverageUpdateTime() {
//		List<String> doclist = KeyWordsAssessment.searchFrontItemFromSolrByKeyWord("娱乐");
//		List<Integer> testList = KeyWordsAssessment.countUpdateNewsNumByDay(doclist);
////		List<Integer> test1list = KeyWordsAssessment.countUpdateDocNumByStep(testList, 3);
//		Gson gson = new Gson();
//		System.out.println(gson.toJson(testList));
//		double result = KeyWordsAssessment.countAverageUpdateTime(testList);
//		System.out.println(result);
//	}
}
