package com.ifeng.iRecommend.topicmodel;

import java.util.HashMap;
import java.util.Map;
/**
 * <PRE>
 * 作用 : 
 * 		段落词频统计表达；  
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
 *          1.0          2012-10-24        mayk          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class Paragraph {
	private Map<Integer,Integer> wordCount;
	public Paragraph(){
		this.wordCount = new HashMap<Integer,Integer>();
	}
	
	/*
	 * 添加一个词id；
	 */
	public void addByOne(Integer id){
		Integer count = wordCount.get(id);
		if(count ==null)
			count =0;
		wordCount.put(id, ++count);
	}
	
	public Map<Integer,Integer> getWordCount(){
		return this.wordCount;
	}
}
