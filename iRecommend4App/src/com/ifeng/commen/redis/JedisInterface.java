/**
 * 
 */
package com.ifeng.commen.redis;

/**
 * <PRE>
 * 作用 : 
 *   jedis链接维护；异常统一处理；提供接口给其它；
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 * 	  重试机制：
 *   exists判断：后续需要做一个返回值的特别处理，要特别注意异常导致的查询失败；如果是其它异常原因导致查询不到某个
 *   key，那么应该等待下继续查询，而不是当做不存在处理；
 *   而且这个问题，又导致了新问题，就是需要做本地包装器缓存，防止内容查询不到。。。
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2012-6-11        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.LoadConfig;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Transaction;

public class JedisInterface {
	protected static final Log LOG = LogFactory.getLog("jedis");
	private static JedisPoolConfig jedisPoolConfig;

	private static JedisPool jedisPool;
	
	static {
		
		String ip = "",port = "";
		String maxActive = "",maxIdel = "",maxWait = "",timeOut = "";
		try{
			ip = LoadConfig.lookUpValueByKey("redisserverip");
			port = LoadConfig.lookUpValueByKey("redisserverport");
			maxActive = LoadConfig.lookUpValueByKey("maxactive");
			maxIdel = LoadConfig.lookUpValueByKey("maxidel");
			maxWait = LoadConfig.lookUpValueByKey("maxwait");
			timeOut = LoadConfig.lookUpValueByKey("redistimeout");
		}catch(Exception e){
			LOG.error("read config:",e);
			if(timeOut == null || timeOut.isEmpty())
				timeOut = "5000";
		}
		
		LOG.info(ip+" "+port);
		LOG.info("maxActive:"+maxActive);
		LOG.info("maxIdel:"+maxIdel);
		LOG.info("maxWait:"+maxWait);
		LOG.info("timeOut:"+timeOut);
		LOG.info("------");

		jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxActive(Integer.valueOf(maxActive));
		jedisPoolConfig.setMaxIdle(Integer.valueOf(maxIdel));
		jedisPoolConfig.setMaxWait(Integer.valueOf(maxWait));
		jedisPoolConfig.setTestOnBorrow(true);
		jedisPool = new JedisPool(jedisPoolConfig, ip, Integer.valueOf(port),Integer.valueOf(timeOut));
	}

	/*
	 * redis server changed;
	 */
	public static void changeServer(String s_ip,int port)
	{
		LOG.info("destroy jedispool...");
		jedisPool.destroy();
		LOG.info("change to server="+s_ip+" port="+port);
		
		String maxActive = "",maxIdel = "",maxWait = "",timeOut = "";
		try{
			maxActive = LoadConfig.lookUpValueByKey("maxactive");
			maxIdel = LoadConfig.lookUpValueByKey("maxidel");
			maxWait = LoadConfig.lookUpValueByKey("maxwait");
			timeOut = LoadConfig.lookUpValueByKey("timeout");
		}catch(Exception e){
			LOG.error("read config:",e);
			if(timeOut == null || timeOut.isEmpty())
				timeOut = "5000";
		}
		
		LOG.info(s_ip+" "+port);
		LOG.info("maxActive:"+maxActive);
		LOG.info("maxIdel:"+maxIdel);
		LOG.info("maxWait:"+maxWait);
		LOG.info("timeOut:"+timeOut);
		LOG.info("------");

		jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxActive(Integer.valueOf(maxActive));
		jedisPoolConfig.setMaxIdle(Integer.valueOf(maxIdel));
		jedisPoolConfig.setMaxWait(Integer.valueOf(maxWait));
		jedisPoolConfig.setTestOnBorrow(true);
		jedisPool = new JedisPool(jedisPoolConfig, s_ip,port,Integer.valueOf(timeOut));
	
		LOG.info("finished.");
		LOG.info("jobs num is:"+JedisInterface.hlen("alljobs", 1));

	}
	
	
	public static JedisPool getJedisPool(){
		return jedisPool;
	}

	public static Jedis getJedisInstance(){
		Jedis jedis = null;
		//失败重试次数
		int frequency = 3;
		while(frequency-- > 0)
		{
	        try{
	        	jedis = jedisPool.getResource();	
	        }catch(Exception e){
	        	LOG.error("got an exception:",e);
	        	//test
	        	//e.printStackTrace();
	        	LOG.error("retry times "+(3-frequency)+":");
	        	continue;
	        }
	        return jedis;
		}

		return jedis;
	}

	/*
	 * select error:socket timeout??,how to deal this problem?
	 */
	public static Jedis getJedisInstance(int dbID){
		Jedis jedis = null;
		//失败重试次数
		int frequency = 3;
		while(frequency-- > 0)
		{
	        try{
	        	jedis = jedisPool.getResource();
	        	jedis.select(dbID);
	        }catch(Exception e){
	        	LOG.error("got an exception:",e);
	        	//test
	        	//e.printStackTrace();
	        	LOG.error("retry times "+(3-frequency)+":");
	        	continue;
	        }
	        return jedis;
		}

		return jedis;
	}
	
	//return connection
	public static void releaseJedisInstance(Jedis jedis) {
		// TODO Auto-generated method stub
		jedisPool.returnResource(jedis);
	}

	//broke this connection
	public static void returnBrokenResource(Jedis jedis) {
		// TODO Auto-generated method stub
		jedisPool.returnBrokenResource(jedis);
	}

	//del
	public static long del(String key,int dbID) {
		if(dbID < 0)
			return 0L;
		long rt = 0L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			rt = jedis.del(key);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
		return rt;
	}
	
	/*
	 * get;if exception found,return "error";
	 */
	public static String get(String key,int dbID) {
		if(dbID < 0)
			return null;
		String rt = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return null;
			rt = jedis.get(key);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			LOG.error("got an exception:",e);
			rt = "error";
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}
	
	//set;sucess:>=0 
	public static long set(String key,String value,int dbID) {
		Long rt = -1L;
		if(dbID < 0)
			return rt;
		
		rt = 1L;
		boolean borrowOrOprSuccess = true;
		Jedis jedis = null;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			jedis.set(key,value);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
		return rt;
	}
	
	//set;sucess:>=0 
	public static long set2(String key,String value,int dbID,int expireSeconds) {
		Long rt = -1L;
		if(dbID < 0 || expireSeconds < 0)
			return rt;
		rt = 1L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			jedis.set(key,value);
			rt = jedis.expire(key,expireSeconds);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
		
		return rt;
	}
	
	/*
	 * hash set;sucess:>=0 
	 */
	public static Long hset(String key,String hKey,String value,int dbID) {
		Long rt = -1L;
		
		if(dbID < 0)
			return rt;
		
		rt = 1L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			//key从空创建
			if (!jedis.exists(key))
				rt = 1L;
			rt = jedis.hset(key,hKey,value);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}

	/*
	 * set_with_expire;success:>=0
	 */
	public static Long set_with_expire(String key,String value, int dbID,int expireSeconds) {
		Long rt = -1L;
		if (dbID < 0)
			return rt;
		rt = 1L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			String srt = jedis.set(key,value);
			rt = jedis.expire(key,expireSeconds);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("set_with_expire:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}
	
	
	//hash get;if exception found,return "error";
	public static String hget(String key,String hKey,int dbID) {
		if(dbID < 0)
			return null;
		String rt = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return null;
			rt = jedis.hget(key,hKey);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = "error";
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}
	
	//hash get len;success:>=0
	public static Long hlen(String key,int dbID){
		Long rt = -1L;
		if(dbID < 0)
			return rt;		
		rt = 0L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return 0L;
			rt = jedis.hlen(key);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	}
	
	/*
	 * hash get all;exception,then return error;
	 */
	public static Map<String, String> hgetAll(String key,int dbID) {
		if(dbID < 0)
			return null;
		Map<String, String> rt = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return null;
			rt = jedis.hgetAll(key);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = new HashMap<String,String>();
			rt.put("error", "1");
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}

	/*
	 * hash del;request sucess:>=0; 
	 */
	public static Long hdel(String key,String hKey,int dbID) {
		Long rt = -1L;
		if(dbID < 0)
			return rt;		
		rt = 0L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return -1L;
			rt = jedis.hdel(key, hKey);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;	
	}

	/*
	 * success:>=0;true:1,false:0;
	 */
	public static long hexists(String key, int dbID) {
		// TODO Auto-generated method stub
		long rt = -1L;
		if(dbID < 0)
			return rt;
		rt = 0L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return rt;
			rt = 1L;
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}

		return rt;
	}
	
	/*
	 * 针对key的限速器，保证minutes分钟内只能限定limitedTimes的请求或者调度；
	 * 仅仅是一个限定窗口；
	 * output:
	 * success >= 0;
	 * 1 可以分配资源；
	 * 0 已满，不可以分配；
	 */
	public static long limitRateInMinutes(String key, float limitedMinutes,long limitedTimesPerMinutes,int dbID) {
		// TODO Auto-generated method stub
		long rt = -1L;
		if(dbID < 0)
			return rt;
		long time = System.currentTimeMillis();
		int timeInMinute = (int) (time/(limitedMinutes*60000));
		rt = 0L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			String keyMinute = key + timeInMinute;
			String sCurrentTimes = jedis.get(keyMinute);
			if(sCurrentTimes != null)
			{
				int currentTimes = Integer.valueOf(sCurrentTimes);
				if(currentTimes > limitedTimesPerMinutes)
				{
					LOG.info(key+" too many times per minute");
					return 0L;
				}else{
					jedis.incr(keyMinute);
				}
			}else{
				Transaction t=jedis.multi();
				t.incr(keyMinute);
				t.expire(keyMinute, (int) (60*limitedMinutes));
				t.exec();
			}
			rt = 1L;
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}

		return rt;
	}
	
	/*
	 * set get all;exception,then return error;
	 */
	public static Set<String> sMembers(String key,int dbID) {
		if(dbID < 0)
			return null;
		Set<String> rt = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return null;
			rt = jedis.smembers(key);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = new HashSet<String>();
			rt.add("error");
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}
	
	/*
	 * set add;sucess:>=0 ,0代表重复；
	 */
	public static Long sadd(String key,String value,int dbID) {
		Long rt = -1L;
	
		if(dbID < 0)
			return rt;
		
		rt = 1L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			//key从空创建
			if (!jedis.exists(key))
				rt = 1L;
			rt = jedis.sadd(key,value);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("sadd:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}
	
	/*
	 * set remove;sucess:>=0 ,1表示成功；
	 */
	public static Long srem(String key,String value,int dbID) {
		Long rt = -1L;
	
		if(dbID < 0)
			return rt;
		
		rt = 1L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			//key从空创建
			if (!jedis.exists(key))
				rt = 1L;
			rt = jedis.srem(key,value);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("srem:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}
	
	
	
	/*
	 * sorted set add;sucess:>=0 ,0代表重复；
	 */
	public static Long zadd(String key,String value,double score,int dbID) {
		Long rt = -1L;
	
		if(dbID < 0)
			return rt;
		
		rt = 1L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			//key从空创建
			if (!jedis.exists(key))
				rt = 1L;
			rt = jedis.zadd(key, score, value);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("zadd:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}
	
	/*
	 * sorted set remove;sucess:>=0 ,1表示成功；
	 */
	public static Long zrem(String key,String value,int dbID) {
		Long rt = -1L;
	
		if(dbID < 0)
			return rt;
		
		rt = 1L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			//key从空创建
			if (!jedis.exists(key))
				rt = 1L;
			rt = jedis.zrem(key,value);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			LOG.error("srem:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}
	
	
	/*
	 * sorted set; get items in [min,max] by desc;exception,then return error;
	 */
	public static Set<String> zMembers(String key,double max,double min,int dbID) {
		if(dbID < 0)
			return null;
		Set<String> rt = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return null;
			rt = jedis.zrevrangeByScore(key, max, min);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = new HashSet<String>();
			rt.add("error");
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}
	
	/*
	 * sorted set; get items in [min,max] by desc;exception,then return error;
	 */
	public static Set<String> zMembersByIndex(String key,int num,int dbID) {
		if(dbID < 0)
			return null;
		Set<String> rt = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisInstance();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return null;
			rt = jedis.zrevrange(key, 0, num-1);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = new HashSet<String>();
			rt.add("error");
			LOG.error("got an exception:",e);
			returnBrokenResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				releaseJedisInstance(jedis);
		}
        return rt;
	
	}
}
