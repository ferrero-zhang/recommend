package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.util.List;
import java.util.Random;

import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;

/**
 * <PRE>
 * 作用 : 
 *   测试类
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年10月10日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class TestClass {
	public static void main(String[] args) {
		EntityLibQuery.init();
		Random r = new Random();
		
//		queryInterface query = queryInterface.getInstance();
//		itemf item = query.queryItemF(String.valueOf(r.nextInt(440000)));
//		String splitContent = item.getSplitContent();
//		String splitTitle = item.getSplitTitle();
//		long startMili=System.currentTimeMillis();
//		for (int i = 0; i != 500; i++) {
//			System.out.println(i);
//			KeywordExtract ke = new KeywordExtract(splitTitle, splitContent);
//			ke.get_weighted_keywords();
//		}
//		long endMili=System.currentTimeMillis();
//		System.out.println(endMili - startMili);
		
		IKVOperationv2 ob = new IKVOperationv2("appitemdb");		
		for (int i = 0; i != 50; i++) {
			int randomDocID = r.nextInt(440000);
			itemf item = ob.queryItemF(String.valueOf(randomDocID),"d");
			if (null == item) {
				continue;
			}
			String splitContent = item.getSplitContent();
			String splitTitle = item.getSplitTitle();

			KeywordExtract ke = new KeywordExtract(false, splitTitle, splitContent, null);
			List<String> keywords = ke.get_weighted_keywords();
			
			System.out.println(keywords);
			int featuresNum = keywords.size()/3;
			System.out.println(item.getTitle());
			for (int index = 0; index != featuresNum; index++) {
				if (index == 0) {
					System.out.print("\t");
				}
				int featureValueIndex = index * 3;
				int featureTagIndex = (index * 3) + 1;
				int weightIndex = (index * 3) + 2;
				System.out.print("[" + keywords.get(featureValueIndex) + "_" + keywords.get(featureTagIndex) + ", " + keywords.get(weightIndex) + ("] "));
			}
			System.out.println("");
		}
		
		ob.close();
	}
}
