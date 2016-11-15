/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.preloadModel;

import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import redis.clients.jedis.Jedis;

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
 *          1.0          2016年1月6日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class PVItem {
	private String id;
	private Map<String, Integer> id_count;
	private String tag;
	private String time;
	private Map<String,String> cmpp_imcp;
	public Map<String, String> getCmpp_imcp() {
		return cmpp_imcp;
	}
	public void setCmpp_imcp(Map<String, String> cmpp_imcp) {
		this.cmpp_imcp = cmpp_imcp;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Map<String, Integer> getId_count() {
		return id_count;
	}
	public void setId_count(Map<String, Integer> id_count) {
		this.id_count = id_count;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	public static void main(String[] args){
		Jedis jedis = new Jedis("10.32.21.62", 6379, 10000);
		jedis.select(12);
		Set<String> keySet = jedis.keys("*");
		Map<String, String> tempMap = jedis.hgetAll("clientLogs_srhkey_byhour");
		String temp = tempMap.get("数码");
		Gson gson = new Gson();
		PVItem item = gson.fromJson(temp, PVItem.class);
		String str = gson.toJson(tempMap);
		System.out.println(keySet);
		System.out.println(str);
		System.out.println(item.getId());
		System.out.println(item.getId_count());
		System.out.println(item.getCmpp_imcp());
	}
}
