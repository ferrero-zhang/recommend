/**
 * 
 */
package com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.ItemIKVUtil;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.XMLQueryInterface;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.queryCmppItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redis.clients.jedis.Jedis;


/**
 * <PRE>
 * 作用 : 
 *   根据输入的user行为（imcp id），查询得到其features信息
 * 使用 : 
 *    根据输入的user行为（imcp id），查询得到其features信息
 * 示例 :
 *   
 * 注意 :
 * 	单线程版本，不支持并发；
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015-09-22        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */


public class queryItemFeatures {
	//xml query cache
	private static Map<String,ArrayList<String>> hm_item2features = new ConcurrentHashMap<String, ArrayList<String>>();
	
	/*private static int hashsize = 1<<22;

	
	public queryItemFeatures() {
		hm_item2features = new HashMap<String, ArrayList<String>>(hashsize);
	}*/
	
	/**
	 * @Title: clear cache
	 * @Description: 清空短期cache，避免xml features长时间不更新导致的不一致
	 * @author likun
	 * @param itemID
	 * @return 特征表达向量
	 * @throws
	 */
	public static void clean() {
		try{
			if(hm_item2features.size() > 100000)
				hm_item2features.clear();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @Title: getXMLItemF
	 * @Description: 根据输入的imcp ID，查询其features表达；注
	 * @author likun
	 * @param itemID
	 * @return 特征表达向量
	 * @throws
	 */
	public static ArrayList<String> getXMLFeatures(String itemID) {
		if(null == itemID || itemID.isEmpty())
			return null;

		ArrayList<String>  al_res = hm_item2features.get(itemID);
		//ArrayList<String>  al_res = getFt(itemID);
		if(al_res != null){	
			return al_res;
		}
		if(itemID.length() < 16){
			if(itemID.matches("\\d{5,16}")){
				al_res = XMLQueryInterface.queryExFeature(itemID);
			}
		}else{
			al_res = XMLQueryInterface.queryExFeature(itemID);
		}
		
		//如果是cmpp ID（目前只是ent）
		if (itemID.matches("4\\d{5,16}000")) {
			//查询得到title或者url，并在ikv中找到imcp id，然后继续查询
			al_res = queryCmppItem.getInstance().getExFeatures(itemID.substring(0,itemID.length()-3),"ent");
		}
		
//		synchronized(hm_item2features)
//		{
//			if(al_res != null)
//				hm_item2features.put(itemID, al_res);
//			else
//				hm_item2features.put(itemID, new ArrayList<String>());
//
//		}

	
		if(al_res != null)
			hm_item2features.put(new String(itemID), al_res);
		else
			hm_item2features.put(new String(itemID), new ArrayList<String>());
		/*if(al_res != null){
			putFt2Redis(itemID,al_res);
		}*/
		return al_res;
	}
	public void putFt2Redis(String docid, ArrayList<String> fts){
		Gson gson = new Gson();
		Jedis jedis = null;
		try {
			String json = gson.toJson(fts);
			// 将放入redis的db 13中，方便前端业务逻辑层接口调用
			jedis = new Jedis("10.90.1.58"/*"10.32.21.62"*/, 6379, 10000);//GlobalParams.updateUserSetRedisIP
			jedis.select(10);//GlobalParams.updateUserSetDBnum
			jedis.set(docid, json);
			jedis.expire(docid, 24*3600);
		} catch (Exception e) {
			jedis.disconnect();
		}
		jedis.disconnect();
	}
	public ArrayList<String> getFt(String docid){
		Gson gson = new Gson();
		ArrayList<String> fts = new ArrayList<String>();
		Jedis jedis = null;
		try {
			// 将放入redis的db 13中，方便前端业务逻辑层接口调用
			jedis = new Jedis("10.90.1.58", 6379, 10000);//GlobalParams.updateUserSetRedisIP
			jedis.select(10);//GlobalParams.updateUserSetDBnum
			String value = jedis.get(docid);
			if(value == null)
				return null;
			fts = gson.fromJson(value, new TypeToken<ArrayList<String>>(){}.getType());
		} catch (Exception e) {
			jedis.disconnect();
			return null;
		}
		jedis.disconnect();
		return fts;
	}
	public static void main(String[] args){
		queryItemFeatures query = new queryItemFeatures();
		String itemID = "101569553";
		ArrayList<String>  al_res = new ArrayList<String>();
		if (itemID.matches("4\\d{5,16}000")) {
			//查询得到title或者url，并在ikv中找到imcp id，然后继续查询
			al_res = queryCmppItem.getInstance().getExFeatures(itemID.substring(0,itemID.length()-3),"ent");
		}
		
		//对所有的imcp ID，XMLQueryInterface
		if (itemID.matches("\\d{5,16}")) {
			//al_res = XMLQueryInterface.getInstance().queryExFeature(itemID);
		}
		//query.putFt2Redis(itemID,al_res);
		//List<String> ss = query.getFt("101569553");
	}
}
