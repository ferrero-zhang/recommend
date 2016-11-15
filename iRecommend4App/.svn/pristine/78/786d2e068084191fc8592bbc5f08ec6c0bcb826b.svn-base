/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;


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
 *          1.0          2016年3月24日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class IfengHotPicturePreload implements Runnable {
	private static Log LOG = LogFactory.getLog("IfengHotPicturePreload");
	class RedismodelItem{
		List<FrontNewsItem> al_special_items;
	}
	private List<FrontNewsItem> readItemFromRedis(){
		List<FrontNewsItem> tempList = new ArrayList<FrontNewsItem>();
		try{
			Jedis jedis = new Jedis("10.32.21.62", 6379, 2000);
			jedis.select(11);
			String content = jedis.get("backup_items");
			Gson gson = new Gson();
			tempList = gson.fromJson(content, RedismodelItem.class).al_special_items;
		} catch (Exception e){
			LOG.error(" ", e);
		}
		return tempList;
	}
	
	private void preload(){
		String key = "凤凰热图榜";
		List<FrontNewsItem> tempList = readItemFromRedis();
		List<FrontNewsItem> disList = new ArrayList<FrontNewsItem>();
		for(FrontNewsItem fItem : tempList){
			if(fItem.getDocType().equals("slide")){
				disList.add(fItem);
			}
		}
		Gson gson = new Gson();
		String disStr = gson.toJson(disList);
		LOG.info("凤凰热图榜 size ："+disList.size());
		disToRedis(key, 8*60*60*1000, disStr);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		preload();
		
	}
	
	public static void main(String[] args){
		IfengHotPicturePreload p = new IfengHotPicturePreload();
		p.run();
	}
	/**
	 * 投放至redis函数
	 * 
	 * 
	 * 
	 * @param String tableName ： table名称
	 * 		  long validTime : 存活时间
	 * 		  String disStr ： 需要投放的字符串
	 * 
	 * @return 
	 */
	private void disToRedis(String tableName,long validTime,String disStr){
		if(disStr == null){
			return ;
		}
//		try{
//
//			Jedis jedis = new Jedis("10.90.1.57", 6379, 10000);
//			jedis.select(2);
//			String status = jedis.setex(tableName, (int) validTime, disStr);
//			if(!status.equals("OK")){
//				LOG.error("set status code:"+status);
//			}else{
//				LOG.info("Dis "+tableName+" to redis");
//			}
//		}catch(Exception e){
//			LOG.error("ERROR"+e);
//		}
		
		try{

			Jedis jedis = new Jedis("10.90.7.60", 6379, 10000);
			jedis.select(2);
			String status = jedis.setex(tableName, (int) validTime, disStr);
			if(!status.equals("OK")){
				LOG.error("set status code:"+status);
			}else{
				LOG.info("Dis "+tableName+" to redis");
			}
		}catch(Exception e){
			LOG.error("ERROR"+e);
		}
	}
	
	
	
}
