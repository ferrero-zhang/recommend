/**
 * 
 */
package com.ifeng.iRecommend.likun.locationNews;

import java.util.ArrayList;
import java.util.HashMap;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

/**
 * <PRE>
 * 作用 : 
 *   根据输入内容，识别出其地域特征，并验证其能否成为地域新闻；
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
 *          1.0          2014年9月23日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class locationExtraction {
	private HashMap<String,String> hm_loc_map;
	
	public locationExtraction(){
		hm_loc_map = new HashMap<String,String>();
		//读入映射表
		FileUtil fu = new FileUtil();
		fu.Initialize(fieldDicts.locationMapFilePath, "utf-8");
		String line = null;
		while((line=fu.ReadLine())!=null){
			String[] secs = line.split("->");
			if(secs.length != 2)
				continue;
			String loc = secs[1];
			String[] secs1 = secs[0].split("\\s");
			for(String sec:secs1){
				hm_loc_map.put(sec+"_ns", loc);
				hm_loc_map.put(sec+"_h", loc);
				hm_loc_map.put(sec, loc);
			}
		}
	}
	
	/**
	 * 根据输入的item内容比如title，已经分词，抽取其地域特征
	 * @param item
	 * 
	 */
	public String extractLocation(Item item){
		if(item == null)
			return "";
		String title = item.getTitle();
		//根据地域映射表来做地域映射
		if(title == null)
			return "";
		String[] secs = title.split("\\s");
		if(secs.length > 0){
			for(String sec:secs){
				if(hm_loc_map.containsKey(sec))
					return hm_loc_map.get(sec);
			}
		}
		return "";
	}
	
	
	/**
	 * 根据输入的item特征，抽取其地域特征
	 * @param item
	 * 
	 */
	public String extractLocation(itemf item){
		if(item == null)
			return "";
		
		ArrayList<String> al_features = item.getFeatures();
		if(al_features == null || al_features.size()%3 != 0){
			System.out.println("ERROR:features num % 3 != 0");
			return null;
		}
		
		for(int i=0;i<al_features.size();i+=3){
			String feature = al_features.get(i);
			String type = al_features.get(i+1);
			float weight = 0f;
			try{
				weight = Float.valueOf(al_features.get(i+2));
			}catch(Exception e){
				weight = 0f;
				e.printStackTrace();
			}
			
			//weight太小，不能置信
			if(weight < 0.5 && weight > -0.5f)
				continue;
			
		
			if(hm_loc_map.containsKey(feature))
				return hm_loc_map.get(feature);
			
		}
		return "";
	}

}
