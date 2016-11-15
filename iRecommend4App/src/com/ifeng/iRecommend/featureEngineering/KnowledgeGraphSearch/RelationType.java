package com.ifeng.iRecommend.featureEngineering.KnowledgeGraphSearch;
/**
 * 
 * <PRE>
 * 作用 : 
 *    知识图谱节点的关系类型  
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
 *          1.0          2016年5月13日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public enum RelationType {
   CONTAINCE,//包含关系
   FRIEND;//朋友关系
   //....待加入的新的节点关系
   
   public static RelationType getRelationType(String type){
	   return valueOf(type);
   }
}
