/**
 * 
 */
package com.ifeng.iRecommend.xuzc.userCenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
 *          1.0          2015-11-25        xuzc          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class SortUtil {
	/**
	 * 
	 * @param oldMap 需要排序的map对象
	 * @return 返回经过排序后的对象（由大到小排序） 
	 */
	public static Map sortMap(Map oldMap) {  
        ArrayList<Map.Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>(oldMap.entrySet());  
        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {  
  
            @Override  
            public int compare(Entry<java.lang.String, Long> arg0,  
                    Entry<java.lang.String, Long> arg1) {  
                return (int) (arg1.getValue() - arg0.getValue());  
            }  
        });  
        Map newMap = new LinkedHashMap();  
        for (int i = 0; i < list.size(); i++) {  
            newMap.put(list.get(i).getKey(), list.get(i).getValue());  
        }  
        return newMap;  
    }
}
