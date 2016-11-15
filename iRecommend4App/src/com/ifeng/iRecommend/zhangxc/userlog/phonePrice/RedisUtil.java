package com.ifeng.iRecommend.zhangxc.userlog.phonePrice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

public class RedisUtil {
	private static final Log LOG = LogFactory.getLog(RedisUtil.class);
	
	public static String getTagsFromRedis(String uid){
		ShardedJedisPool jedispool = UserCenterRedisUtil.getJedisPoolSlave();
		String res = null;
		ShardedJedis sjedis = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try{
			sjedis = jedispool.getResource();
			Collection<Jedis> js = sjedis.getAllShards();
			Iterator<Jedis> it = js.iterator(); 
	    	while(it.hasNext()){  
	    		jedis = it.next();
	    	}
	    	Set<String> set = jedis.hkeys(uid);
	    	
	    	for(String key: set){
	    		if(res==null)
	    			res =  key + ": " + jedis.hget(uid, key);
	    		else
	    		    res =  res + "\n" + key + ": " + jedis.hget(uid, key);
	    	}
	    	
		}catch(Exception e){
			borrowOrOprSuccess = false;
			LOG.error("get tags from redis error for uid ",e);
			UserCenterRedisUtil.returnResource(sjedis, jedispool);
		}finally{
			if(borrowOrOprSuccess == true)
				UserCenterRedisUtil.returnResource(sjedis, jedispool);
		}
		return res;
	}
	
	
	
	public static String getTagsFromPipeline(List<String> uids){
		ShardedJedisPool jedispool = UserCenterRedisUtil.getJedisPoolSlave();
		String res = null;
		ShardedJedis sjedis = null;
		Jedis jedis = null;
		int test = 0;
		boolean borrowOrOprSuccess = true;
		try{
			sjedis = jedispool.getResource();
			Collection<Jedis> js = sjedis.getAllShards();
			Iterator<Jedis> it = js.iterator(); 
	    	while(it.hasNext()){  
	    		jedis = it.next();
	    	}
	    	ShardedJedisPipeline pipeline = sjedis.pipelined();
	    	for(String uid : uids){
	    		pipeline.hgetAll(uid);
	    	}
	    	List<Object> result = pipeline.syncAndReturnAll();
	    	int count = 0;
	    	for(Object o : result){
	    		Map<String,String> info = (Map<String,String>) o;
	    		if(info != null && !info.isEmpty()){
	    			if(info.get("t1") == null || info.get("t1").equals("null$null")){
	    				test++;
	    			}
		    		System.out.println(info.toString());
	    		}    		
	    		count++;
	    	}
		}catch(Exception e){
			borrowOrOprSuccess = false;
			LOG.error("get tags from redis error for uid ",e);
			UserCenterRedisUtil.returnResource(sjedis, jedispool);
		}finally{
			try{				
				if(borrowOrOprSuccess == true)
					UserCenterRedisUtil.returnResource(sjedis, jedispool);
				sjedis.disconnect();
			}catch(Exception e){
				if(borrowOrOprSuccess == true)
					UserCenterRedisUtil.returnResource(sjedis, jedispool);
				e.printStackTrace();
			}
			LOG.info("There are " + test + "users don't have data");
		}
		return res;
	}
	
	
	public static void setUserTag(String uid, Map<String,String> map){
		ShardedJedisPool jedispool = UserCenterRedisUtil.getJedisPoolMaster();
		ShardedJedis sjedis = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		
		try{
			sjedis = jedispool.getResource();
			Collection<Jedis> js = sjedis.getAllShards();
			Iterator<Jedis> it = js.iterator(); 
	    	while(it.hasNext()){  
	    		jedis = it.next();
	    	}
	    	jedis.hmset(uid, map);
		}catch(Exception e){
			borrowOrOprSuccess = false;
			System.out.println(e.getMessage());
			LOG.error("set tags from redis error for uid " + e.getMessage());
			UserCenterRedisUtil.returnResource(sjedis, jedispool);
		}finally{
			if(borrowOrOprSuccess == true)
				UserCenterRedisUtil.returnResource(sjedis, jedispool);
		}
		return;
	}
	
	public static void main(String[] args) {
		//System.out.println( RedisUtil.getTagsFromRedis("9253d4db628b13098c923b9b1f4dc751f91a216d") );
		//RedisUtil.setUserTag("9253d4db628b13098c923b9b1f4dc751f91a216d");
		System.out.println("\n" + RedisUtil.getTagsFromRedis("868256022839509") ); //qq yes
		System.out.println("\n" + RedisUtil.getTagsFromRedis("A0000055900F10") );   //sina  yes
		System.out.println("\n" + RedisUtil.getTagsFromRedis("355066064604228") );  //ifengso  no
		System.out.println("\n" + RedisUtil.getTagsFromRedis("356751060847298") );  //sina  no
		System.out.println("\n" + RedisUtil.getTagsFromRedis("9253d4db628b13098c923b9b1f4dc751f91a216d") );   //weixin yes
		
	/*	List<String> uids = new ArrayList<String>();
		uids.add("9253d4db628b13098c923b9b1f4dc751f91a216d");
		uids.add("99000520137538");
		uids.add("869841026586070");
	    RedisUtil.getTagsFromPipeline(uids);*/
	}
}
