/**
 * 
 */
package com.ifeng.iRecommend.xuzc.userCenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

import com.google.gson.Gson;
import com.ifeng.iRecommend.kedm.userCenterTag.DoTagMain;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.userShardedRedisUtil;

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
 *          1.0          2016-3-14        xuzc          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class GetHotTag {
	private static final Log log = LogFactory.getLog(DoTagMain.class);
	private static Map<String, Long> tag_vc = new HashMap<String, Long>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Map<String, String> userinfos = new HashMap<String, String>();
		StringBuilder userinfos = new StringBuilder();
		
		List<String> uids = null;
		try {
			uids = getUserIds("e:/uids.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("user size "+uids.size());
		int rows = uids.size()/10000;
		try{
			for(int i=0 ; i <= rows ; i++){
				int len = 10000*(i+1);
				if(len > (uids.size()-1)){
					len = uids.size()-1;
				}
				if(len < i*10000)
					break;
				List<String> once_uids = uids.subList(i*10000, len);
				getTagsFromPipelineT2andT3ToString(once_uids);
				//userinfos.putAll(userinfo);
				log.info("fihish get tags from pipeline sublist "+ len);
			}
			log.info("finish write to flie");
		}catch(Exception e){
			log.error("do user tag error",e);
			e.printStackTrace();
		}
//		for(Entry<String, String> en : userinfos.entrySet()){
//			System.out.println(en.getKey()+":"+en.getValue());
//		}
		/*for(Entry<String, String> en : userinfos.entrySet()){
			String[] tags = en.getValue().split("#");
			if(tags.length>0){
				for(String tag : tags){
					if(tag_vc.keySet().contains(tag)){
						Long count = tag_vc.get(tag);
						count++;
						tag_vc.put(tag, count);
					}else{
						tag_vc.put(tag, (long)1);
					}
				}
			}			
		}*/
		FileUtil fu = new FileUtil();
		fu.writeMapToFile(SortUtil.sortMap(tag_vc), "e:/tagstest1.txt");
		System.out.println("game over !");
	}
	/**
	 * @param once_uids
	 * @return
	 */
	private static void getTagsFromPipelineT2andT3ToString(List<String> uids) {
		ShardedJedisPool jedispool = userShardedRedisUtil.getJedisPoolSlave();
		ShardedJedis sjedis = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		//Map<String, String> user_tag = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder();
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
	    	for(String uid : uids){
	    		//pipeline.hgetAll(uid);
	    		pipeline.hget(uid, "t2");
	    		pipeline.hget(uid, "t3");
	    	}
	    	List<Object> result = pipeline.syncAndReturnAll();
	    	for (int i = 0; i < result.size(); i=i+2) {
	    		String tags = null;
	    		if(result.get(i)!=null&&result.get(i+1)!=null){
	    			tags = result.get(i).toString()+"#"+result.get(i+1).toString();
	    		}else if(result.get(i)!=null&&result.get(i+1)==null){
	    			tags = result.get(i).toString();
	    		}else if(result.get(i)==null&&result.get(i+1)!=null){
	    			tags = result.get(i+1).toString();
	    		}
				if(tags!=null){
					tags = filter(tags);
					if(!"#".equals(tags)){
						String[] topics = tags.split("#");
						if(topics.length>0){
							for(String tag : topics){
								if(tag_vc.keySet().contains(tag)){
									Long count = tag_vc.get(tag);
									count++;
									tag_vc.put(tag, count);
								}else{
									tag_vc.put(tag, (long)1);
								}
							}
						}
					}
				}					
			}
		}catch(Exception e){
			borrowOrOprSuccess = false;
			log.error("get tags from redis error for uid ",e);
			userShardedRedisUtil.returnResource(sjedis, jedispool);
		}finally{
			sjedis.disconnect();
			if(borrowOrOprSuccess == true)
				userShardedRedisUtil.returnResource(sjedis, jedispool);
		}
	}
	/**
	 * @param string
	 * @return
	 * @throws IOException 
	 */
	private static List<String> getUserIds(String filepath) throws IOException {
		List<String> strSet = new ArrayList<String>();
		File file = new File(filepath);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line;
		while((line = br.readLine())!=null){
			strSet.add(line);
		}
		br.close();
		return strSet;
	}
	@Test
	public void test(){
		String s = getTagsFromRedis("ada7ae9c2b86b18d40500f3da25cf1957f4a011f");
		System.out.println(s);
	}
	public static String getTagsFromRedis(String uid){
		ShardedJedisPool jedispool = userShardedRedisUtil.getJedisPoolSlave();
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
	    		jedis.select(1);
	    		if(jedis.hexists(uid, "t1"))
	    			break;
	    	}
	    	String t2 = jedis.hget(uid, "t2");
	    	String t3 = jedis.hget(uid, "t3");
	    	res = t2+"#"+t3;
		}catch(Exception e){
			borrowOrOprSuccess = false;
			log.error("get tags from redis error for uid ",e);
			userShardedRedisUtil.returnResource(sjedis, jedispool);
		}finally{
			if(borrowOrOprSuccess == true)
				userShardedRedisUtil.returnResource(sjedis, jedispool);
		}
		return res;
	}
	/**
	 * 批量获取用户画像
	 * @param uids 用户id的集合
	 * @return 返回用户画像
	 */
	public static String getTagsFromPipeline(List<String> uids){
		ShardedJedisPool jedispool = userShardedRedisUtil.getJedisPoolSlave();
		String res = null;
		ShardedJedis sjedis = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		StringBuffer sb = new StringBuffer();
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
	    	for(String uid : uids){
	    		pipeline.hgetAll(uid);
	    	}
	    	List<Object> result = pipeline.syncAndReturnAll();
	    	int count = 0;
	    	
	    	for(Object o : result){
	    		Map<String,String> info = (Map<String,String>)o;
	    		if(info != null){
	    			sb.append(uids.get(count)).append("=");
	    			String jsonstr = gson.toJson(info);
	    			sb.append(jsonstr).append("\n");
		    		
	    		}
	    		//dotagToMap(uids.get(count),info);
	    		//System.out.println(uids.get(count)+info);
	    		count++;
	    	}
		}catch(Exception e){
			borrowOrOprSuccess = false;
			log.error("get tags from redis error for uid ",e);
			userShardedRedisUtil.returnResource(sjedis, jedispool);
		}finally{
			sjedis.disconnect();
			if(borrowOrOprSuccess == true)
				userShardedRedisUtil.returnResource(sjedis, jedispool);
		}
		return sb.toString();
	}
	/**
	 * 批量获取用户画像t2和t3
	 * @param uids 用户id的集合
	 * @return 返回用户画像
	 */
	public static Map<String, String> getTagsFromPipelineT2andT3(List<String> uids){
		ShardedJedisPool jedispool = userShardedRedisUtil.getJedisPoolSlave();
		String res = null;
		ShardedJedis sjedis = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		Map<String, String> user_tag = new HashMap<String, String>();
		//StringBuilder sb = new StringBuilder();
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
	    	for(String uid : uids){
	    		//pipeline.hgetAll(uid);
	    		pipeline.hget(uid, "t2");
	    		pipeline.hget(uid, "t3");
	    	}
	    	List<Object> result = pipeline.syncAndReturnAll();
	    	int count = 0;
	    	for (int i = 0; i < result.size(); i=i+2) {
	    		String tags = null;
	    		if(result.get(i)!=null&&result.get(i+1)!=null){
	    			tags = result.get(i).toString()+"#"+result.get(i+1).toString();
	    		}else if(result.get(i)!=null&&result.get(i+1)==null){
	    			tags = result.get(i).toString();
	    		}else if(result.get(i)==null&&result.get(i+1)!=null){
	    			tags = result.get(i+1).toString();
	    		}
				if(tags!=null){
					tags = filter(tags);
					if(!"#".equals(tags)){
						user_tag.put(uids.get(count), tags);
					}
				}				
				count++;		
			}	    	
		}catch(Exception e){
			borrowOrOprSuccess = false;
			log.error("get tags from redis error for uid ",e);
			userShardedRedisUtil.returnResource(sjedis, jedispool);
		}finally{
			sjedis.disconnect();
			if(borrowOrOprSuccess == true)
				userShardedRedisUtil.returnResource(sjedis, jedispool);
		}
		return user_tag;
	}
	/**
	 * @param tags
	 * @return
	 */
	private static String filter(String tags) {
		if(tags.contains("$")){
			tags = tags.replace("$", "#");
		}
		//将tags分解为单个tag的数组
		String[] tts = tags.split("#");
		StringBuilder sb = new StringBuilder();
		for(String tag : tts){
			if(tag.contains("_")){
				/*String[] ts = tag.split("_");
				if(ts.length==3){
					if(Integer.parseInt(ts[1])>10){
						sb.append(ts[0]).append("#");
					}
				}*/
			}else{
				sb.append(tag).append("#");
			}
		}
		tags = sb.toString();
		if(tags.contains("##")){
			tags = tags.replace("##", "#");
		}
		//System.out.println(tags);
		return tags;
	}
}
