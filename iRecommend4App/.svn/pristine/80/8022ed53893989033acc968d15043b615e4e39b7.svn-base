package com.ifeng.commen.redis;

import java.lang.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import redis.clients.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author likun
 * @version 2012-3-7
 */

public class JedisTest {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		templatesLib tl = new templatesLib(null);
//		tl.toLocalFile();
//		
//		
//		
//		Jedis jedis = JedisInterfaceWithCache.getJedisInstance();
		Jedis jedis = new Jedis("10.32.21.61"/*"119.254.74.24"*/,2012,10000);
		//jedis.bgsave();
		
		jedis.select(3);
		jedis.get("*");
		System.out.println(jedis.get("hello"));
		
		
		System.out.println(jedis.dbSize());
//		jedis.select(6);
//		System.out.println(jedis.dbSize());
//		jedis.keys("*");
//		jedis.select(7);
//		System.out.println(jedis.dbSize());
		
		//jedis.flushDB();
		
	
//		//System.out.println(jedis.dbSize());
//		long b = System.currentTimeMillis();
//		String tsirootkey = "tsi_root_-1";
//		//jedis.del(tsirootkey);
//		//jedis.hset(tsirootkey, "tsi_div class=\"showbiz\"_1", "what");
//		treeStyleItem tmp = JsonUtils.fromJson(jedis.hget(tsirootkey, "tsi_div id=www.yule.huaxia.com_0"),treeStyleItem.class);
//		System.out.println(tmp.toString());
//		long e = System.currentTimeMillis();
//		System.out.println((e-b));
		
//		jedis.select(1);
//		String allJobsKey = "alljobs";
//		System.out.println(jedis.hlen(allJobsKey));
//
//		JedisQueue<Job> waitingJobsQueue = new JedisQueue<Job>(JedisInterfaceWithCache.getJedisPool(), Job.class, "waitingcrawler1",1);  		
//		//simple,取到的默认就是可以抓取的；
//		Job jb = waitingJobsQueue.take();
//		while(jb != null)
//		{
//			jb =  waitingJobsQueue.take();
//			System.out.println(jb.listIndex);
//		}
////		
		
		
		
		
		
		
		
//		System.out.println(JedisTest.class);
		
		//Map<String, String> mp = JedisInterfaceWithCache.hgetAll("allJobs", 1);
		//System.out.println(mp.size());
		
//		Jedis jedis= JedisInterfaceWithCache.getJedisInstance();
//		jedis.select(2);
//		System.out.println(jedis.exists("a4ecf881255d64aea8b59e2716ac3ba0"));
//		System.out.println(jedis.exists("allJobs".getBytes()));
//		System.out.println(jedis.hgetAll("alljobs").size());
//		String mp = jedis.hget("alljobs","people_news_a");
//		System.out.println(JsonUtils.fromJson(mp, Job.class));
//		mp = jedis.hget("alljobs","chinadaily_hqgj_a");
//		System.out.println(JsonUtils.fromJson(mp, Job.class));
	}

}
