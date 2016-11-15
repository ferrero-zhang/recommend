/**
 * 
 */
package com.ifeng.iRecommend.xuzc.userCenter.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import com.ifeng.iRecommend.kedm.userCenterTag.DoTagMain;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.userShardedRedisUtil;
import com.ifeng.iRecommend.xuzc.userCenter.locAnalysis.LocOpration;

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
 *          1.0          2016-5-11        xuzc          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class UserModelUtil {
	private static final Log log = LogFactory.getLog(UserModelUtil.class);
	private static ShardedJedisPool jedispool;
	private static ShardedJedis sjedis = null;
	static boolean borrowOrOprSuccess = true;
	public static void redisInit(){
		jedispool = userShardedRedisUtil.getJedisPoolSlave();
		Jedis jedis = null;		
		try{
			sjedis = jedispool.getResource();
			Collection<Jedis> js = sjedis.getAllShards();
			Iterator<Jedis> it = js.iterator(); 
	    	while(it.hasNext()){  
	    		jedis = it.next();
	    		jedis.select(1);
	    	}
		}catch(Exception e){
			borrowOrOprSuccess = false;
			log.error("get tags from redis error for uid ",e);
			userShardedRedisUtil.returnResource(sjedis, jedispool);
		}
	}
	public static void redisDestory(){
		sjedis.disconnect();
		if(borrowOrOprSuccess == true)
			userShardedRedisUtil.returnResource(sjedis, jedispool);
	}
	/**
	 * 从用户画像（redis集群）中获取T3
	 * @param uid 用户id
	 * @return 返回T3字符串
	 */
	public static String getT3FromRedis(String uid) {
		String t3 = null;
		//Map<String, String> user_tag = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder();
		
    	//ShardedJedisPipeline pipeline = sjedis.pipelined();	    	
		//pipeline.hgetAll(uid);
		//pipeline.hget(uid, "t2");
    	t3 = sjedis.hget(uid, "t3");
		return t3;
	}
	/**
	 * 从redis集群中获取用户画像
	 * @param uid 用户id
	 * @return 返回用户画像map信息
	 */
	private static Map<String, String> getUsermodelFromRedis(String uid) {
		Map<String, String> usermodel = new HashMap<String, String>();
		
    	//ShardedJedisPipeline pipeline = sjedis.pipelined();	    	
		//pipeline.hgetAll(uid);
		//pipeline.hget(uid, "t2");
		usermodel = sjedis.hgetAll(uid);
		return usermodel;
	}
	@Test
	public void test(){
		redisInit();
		String t3 = getT3FromRedis("60ce6546b62a62514e0694b6ccc89400d1ab5ea3");
		System.out.println(t3);
		List<String> citys = LocOpration.filterCityFromT3(t3);
		for(String city : citys){
			System.out.println(city);
		}
		System.out.println(getT3FromRedis("60ce6546b62a62514e0694b6ccc89400d1ab5ea3"));
		
		redisDestory();
	}
}
