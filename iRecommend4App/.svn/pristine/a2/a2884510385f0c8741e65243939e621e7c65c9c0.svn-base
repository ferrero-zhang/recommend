package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.util.ArrayList;


import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;
import com.ifeng.iRecommend.wuyg.commonData.entity.entityBase.KnowledgeBaseBuild;

/**
 * <PRE>
 * 作用 : 
 *   知识库初始化
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
public class EntityLibQuery {
	// 初始化实体库
	public static void init() {
		KnowledgeBaseBuild.initEntityTree();
	}
	/**
	 * 
	* @Title:getEntityList
	* @Description: 如果需要获取到全量，则输入 * 
	* @param word
	* @return
	* @author:wuyg1
	* @date:2016年8月16日
	 */
	public static ArrayList<EntityInfo> getEntityList(String word) {
		
		ArrayList<EntityInfo> entityList = new ArrayList<EntityInfo>();
		
		if(word.equals("*")){
			entityList = KnowledgeBaseBuild.getAllEntLib();
		}else{
			entityList = KnowledgeBaseBuild.getObjectList(word);
		}
		
		return entityList;
	}
	
	public static void main(String[] agrs) {
		EntityLibQuery.init();
		
		System.out.println(KnowledgeBaseBuild.getObjectList("万科地产"));
//		ArrayList<EntInfo> ei = getEntityList("XXX");
//		if (null != ei) {
//			System.out.println(ei.toString());
//
////			for (EntInfo ef : ei) {
////				System.out.println(ef.toString());
////				//System.out.println(ef.getLevels().get(ef.getLevels().size() - 1));
////			}
//		} else {
//			System.out.println("Has no this word.");
//		}
//		
//		
//		while (true) {
//			ArrayList<EntInfo> ei = EntityLibQuery("pm2.5");
//			if (null != ei) {
//				System.out.println(ei.toString());
//			} else {
//				System.out.println("Has no this word.");
//			}
////			
////			try {
//				Thread.sleep(300);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
//	}
}
