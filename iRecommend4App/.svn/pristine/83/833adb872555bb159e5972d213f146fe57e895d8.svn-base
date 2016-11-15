package com.ifeng.iRecommend.kedm.userlog;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.google.gson.Gson;
import com.ifeng.iRecommend.kedm.util.RedisPoolForUserlog;

/**
 * <PRE>
 * 作用 : 解析的用户操作记录userlog和用户看过的文章信息userdoc存入redis
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
 *          1.0          2015-7-23        kedm          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class LogRedisOperation {
	private static final Log LOG = LogFactory.getLog("log_to_solr");
	private static int dbnum = 7;
	private static int simdbnum = 6;
	private static Gson gson = new Gson();
	private static int outTime = 15*24*3600;
	private static int simoutTime = 2*60;
	
/*	public static void init(){
		jedis = RedisPoolUtil.getJedisClient();
		jedis.select(dbnum);
	}
	
	public static Jedis reJedis(){
		return jedis;
	}*/
	/**
	 * @description 15天缓存
	 * @param @param udocs
	 * @return void
	 */
	public static void postJedis(List<UserDoc> udocs){
		Jedis jedis = RedisPoolForUserlog.getJedisClient();
		jedis.select(dbnum);
		Pipeline pipeline = jedis.pipelined();
		List<Object> results = null;
		try{
			for(UserDoc udoc : udocs){
				String data = gson.toJson(udoc);
				pipeline.setex(udoc.getId()+"udoc", outTime, data);
			}
			results = pipeline.syncAndReturnAll();
		}catch(Exception e){
			LOG.error("post to jedis error!!!");
			e.printStackTrace();
		}finally{
			RedisPoolForUserlog.returnResource(jedis);
		}
		LOG.info("post to jedis done.....");
	}
	/**
	 * @description 实时缓存2分钟
	 * @param @param udocs
	 * @return void
	 */
	public static void simpostJedis(List<UserDoc> udocs){
		Jedis jedis = RedisPoolForUserlog.getJedisClient();
		jedis.select(simdbnum);
		Pipeline pipeline = jedis.pipelined();
		List<Object> results = null;
		try{
			for(UserDoc udoc : udocs){
				String data = gson.toJson(udoc);
				pipeline.setex(udoc.getId()+"udoc", simoutTime, data);
			}
			results = pipeline.syncAndReturnAll();
		}catch(Exception e){
			LOG.error("post to simple jedis error!!!");
			e.printStackTrace();
		}finally{
			RedisPoolForUserlog.returnResource(jedis);
		}
		LOG.info("post to simple jedis done.....");
	}
	public static void main(String[] args){
		
	}

}
