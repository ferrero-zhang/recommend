/**
 * 
 */
package com.ifeng.iRecommend.likun.rankModel;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

/**
 * <PRE>
 * 作用 : 判断文章稿源有效性；非法稿源返回illegal
 *   
 * 使用 : 提供judge接口，static调用
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年4月17日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class illegalSoureJudge {
	private static String illegalRulesFile = "./conf/illegal_sources.txt";
	private static String illegalSoureRules;
	
	private illegalSoureJudge(){
		//读入非法稿源表
		FileUtil wordidFile = new FileUtil();
		wordidFile.Initialize(illegalRulesFile, "UTF-8");
		String line = "";
		StringBuffer sbTmp = new StringBuffer();
		while ((line = wordidFile.ReadLine()) != null) {
			String source = line.trim();
			sbTmp.append(source).append("、");
		}
		illegalSoureRules = sbTmp.toString();
	}
	
	public static illegalSoureJudge getInstance(){
		return new illegalSoureJudge();//有多线程安全问题
	}
	
	/**
	 * judge函数
	 * 
	 * @param source字段 
	 * 
	 * @return true:illegal;false:not_illegal
	 * 
	 */
	public static boolean judge(String source,String url){
		if(source == null || source.isEmpty()){
			if(url != null && url.indexOf("ifeng.com/") > 0){
				return false;
			}else
				return true;
		}
		
		if (illegalSoureRules == null || illegalSoureRules.isEmpty()) {
			if (url != null && url.indexOf("ifeng.com/") > 0) {
				return false;
			} else
				return true;
		}

		if (illegalSoureRules.indexOf(source+"、") >= 0)
			return true;
		else {
			String[] secs = illegalSoureRules.split("、");
			for (String sec : secs) {
				if (!sec.isEmpty() && source.indexOf(sec) >= 0) {
					return true;
				}
			}

		}
		
		return false;
	}
	

	public static void main(String[] args){
		System.out.println(illegalSoureJudge.getInstance().judge("", "1.ifeng.com/do"));
		System.out.println(illegalSoureJudge.getInstance().judge("中国鞋网1", "1.ifeng.com/do"));
		System.out.println(illegalSoureJudge.getInstance().judge("中国鞋", "1.ifeng.com/do"));
		System.out.println(illegalSoureJudge.getInstance().judge("北京广播电视报-人物周刊", "1.ifeng.com/do"));
		System.out.println(illegalSoureJudge.getInstance().judge("人物周刊", "1.ifeng.com/do"));
		System.out.println(illegalSoureJudge.getInstance().judge("北京广播电视报", "1.ifeng.com/do"));
		
	}

}
