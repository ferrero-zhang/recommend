package com.ifeng.iRecommend.kedm.usercenter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import com.google.gson.Gson;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.userShardedRedisUtil;

public class SetAndGetUsercenterFromRedis {
	
	public static void setUsercenter(Map<String,String> keyvalue){
		
	}
	
	public static Map<String,Map<String,String>> getUsercenterPipeline(List<String> users,String...keys){
		ShardedJedisPool jedispool = userShardedRedisUtil.getJedisPoolSlave();
		Map<String,Map<String,String>> res = new HashMap<String,Map<String,String>>();
		ShardedJedis sjedis = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		Gson gson = new Gson();
		try{
			sjedis = jedispool.getResource();
			Collection<Jedis> js = sjedis.getAllShards();
			Iterator<Jedis> it = js.iterator(); 
	    	while(it.hasNext()){  
	    		jedis = it.next();
	    		jedis.select(1);
	    		/*if(jedis.hexists(uid, "t1"))
	    			break;*/
	    	}
	    	
	    	ShardedJedisPipeline pipeline = sjedis.pipelined();
	    	for(String uid : users){
	    		pipeline.hmget(uid, keys);
	    	}
	    	List<Object> result = pipeline.syncAndReturnAll();
	    	int count = 0;
	    	
	    	for(Object o : result){
	    		List<String> info = (List<String>)o;
	    		Map<String,String> temp = new HashMap<String,String>();
	    		temp.put(keys[0], info.get(0));
	    		temp.put(keys[1], info.get(1));
	    		if(info != null){
	    			res.put(users.get(count), temp);
	    		}
	    		//dotagToMap(uids.get(count),info);
	    		//System.out.println(uids.get(count)+info);
	    		count++;
	    	}
		}catch(Exception e){
			borrowOrOprSuccess = false;
			userShardedRedisUtil.returnResource(sjedis, jedispool);
		}finally{
			sjedis.disconnect();
			if(borrowOrOprSuccess == true)
				userShardedRedisUtil.returnResource(sjedis, jedispool);
		}
		return res;
	}
	
	public static void main(String[] args){
		
	}

}
