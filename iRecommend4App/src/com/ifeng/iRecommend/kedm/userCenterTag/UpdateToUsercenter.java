package com.ifeng.iRecommend.kedm.userCenterTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.google.gson.Gson;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.UserCenterRedisUtil;

public class UpdateToUsercenter {
	public static void updateToUsercenterRedis(String json,ShardedJedisPool jedispool){
		if(json == null)
			return;
		Gson gson = new Gson();
		LoginUser loginuser = gson.fromJson(json, LoginUser.class);
		if(loginuser == null)
			return;
		Map<String,String> updatevalue = new HashMap<String,String>();
		if(loginuser.gender != null){
			updatevalue.put("gender", loginuser.gender);
		}
		StringBuffer sb = new StringBuffer();
		if(loginuser.interests != null){
			for(String i : loginuser.interests){
				sb.append(i.trim()).append("_15_10.0").append("#");
			}
			updatevalue.put("login_interest", sb.toString());
		}
		ShardedJedis sjedis = null;
		Jedis jedis = null;
		try{
			sjedis = jedispool.getResource();
			Collection<Jedis> js = sjedis.getAllShards();
			Iterator<Jedis> it = js.iterator(); 
	    	while(it.hasNext()){  
	    		jedis = it.next();
	    	}
	    	sjedis.hmset(loginuser.userId, updatevalue);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			
		}
	}
	class LoginUser{
		private String userId;
		private String gender;
		private List<String> interests = new ArrayList<String>();
	}
	
	public static void main(String[] args){
		String json = "{  \"interests\" : [    \"八卦\",    \"互联网\",    \"电视剧\",    \"健康\"  ],  \"userId\" : \"780cea603ad5eea0c34ad7bbdd370787277db557\",  \"gender\" : \"female\"}";
		ShardedJedisPool jedispool = UserCenterRedisUtil.getJedisPoolMaster();
		updateToUsercenterRedis(json,jedispool);
	}

}
