/**
 * 
 */
package com.ifeng.commen.Utils;

import java.util.HashMap;
import java.util.HashSet;

import com.ifeng.iRecommend.fieldDicts.fieldDicts;

/**
 * <PRE>
 * 作用 : 
 *   过滤停用词；
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
 *          1.0          2013-7-22        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class stopwordsFilter {
	private static HashSet<String> s_stopwords = null;
	
	static{
		//加载词典
		//载入测试集合，构建虚拟hashmap接口
		s_stopwords =new HashSet<String>();
		FileUtil fu = new FileUtil();
		if(fu.Initialize(fieldDicts.stopwordsFile, "UTF-8")){
			try{
			String rawUserData = null;
			while ((rawUserData = fu.ReadLine()) != null) {
				if (!rawUserData.isEmpty()) {
					s_stopwords.add(rawUserData.trim());
				}
			}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				fu.CloseRead();
			}
		}else{
			System.out.println("stop words file not found!");
		}
		
	}
	
	/*
	 * 判断输入word是否是停用词；
	 */
	public static boolean isStopWords(String word){
		if(word == null || word.isEmpty())
			return true;
		if(s_stopwords.contains(word))
			return true;
		return false;
	}
}
