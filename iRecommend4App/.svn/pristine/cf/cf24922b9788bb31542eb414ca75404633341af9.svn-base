/**
 * 
 */
package com.ifeng.iRecommend.xuzc.userCenter.locAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * <PRE>
 * 作用 : 
 *   
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
 *          1.0          2016-5-12        xuzc          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class LocOpration {

	/**
	 * @param t3 用户画像的T3
	 * @return 过滤出t3里的城市
	 */
	public static List<String> filterCityFromT3(String t3) {
		List<String> citys = new ArrayList<String>();
		if(t3!=null){
			String[] tags = t3.split("#");
			if(tags.length>0){
				for(String tag : tags){
					if(tag.matches(".*?市.*?")){
						citys.add(tag);
					}
				}
			}
		}
		return citys;
	}
	
}
