package com.ifeng.iRecommend.lidm.userLog;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.google.gson.Gson;

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
	public static void postJedis(List<UserDoc> udocs){
		Jedis jedis = RedisPoolUtil.getJedisClient();
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
			RedisPoolUtil.returnResource(jedis);
		}
		LOG.info("post to jedis done.....");
	}
	public static void simpostJedis(List<UserDoc> udocs){
		Jedis jedis = RedisPoolUtil.getJedisClient();
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
			RedisPoolUtil.returnResource(jedis);
		}
		LOG.info("post to simple jedis done.....");
	}
	public static void main(String[] args){
		
	}

}
