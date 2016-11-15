package com.ifeng.iRecommend.zhangxc.userlog.phonePrice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;



import net.sf.json.JSONObject;


public class ReadLogMain {

	private static final Log LOG = LogFactory.getLog(ReadLogMain.class);
	
	
	public static void main(String[] args) {
		
		SolrUtil.cloudServerInit();    //初始化solr
		 
		//用户中心新机房集群，暂时两个集群都写，完全迁移数据后，暂停旧的
		ShardedJedisPool sjp_uc = UserCenterRedisUtil.getJedisPoolMaster();
		ShardedJedis sj_uc = sjp_uc.getResource();
		// 更新所有有新的访问行为的用户中心模型
		Collection<Jedis> js_uc =  sj_uc.getAllShards();
	    Iterator<Jedis> it_uc = js_uc.iterator(); 
	    while(it_uc.hasNext()){  
	    	Jedis j=it_uc.next();  
	    }
	    	
		ShardedJedisPipeline pipeline_uc = sj_uc.pipelined();
		int tmp_num = 1;     // 计数器，控制pipeline的长短
		
		String readfilename = "E:\\log\\data_Converted.txt";      //读取文件的地址
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(readfilename)));
			int count = 0;      //统计损坏数据的数量
			int count_null = 0;     //统计redis中没有该用户用户画像的数量
			int count_value = 0;    //统计有效数据
		    for(String data = br.readLine(); data != null; data = br.readLine()){	        		      		      
		        try{	
		        	JSONObject jsonObject = JSONObject.fromObject(data);
		        	String channel = jsonObject.getString("channel");    //渠道
		        	String uid = jsonObject.getJSONObject("base").getString("uid"); //uid
		        	String gender = null;
		        	
		        	try{
		        		JSONObject jsonOpenObj = jsonObject.getJSONObject("personal");
		        		gender = jsonOpenObj.getString("gender");
		        	} catch (Exception ex) {		        		
		        		if(channel.equals("qq")) {
		        			JSONObject jsonOpenObj = jsonObject.getJSONObject("open");
		        			gender = jsonOpenObj.getString("gender");
		        		} else if(channel.equals("sinawb")) {
		        			JSONObject jsonOpenObj = jsonObject.getJSONObject("open");
		        			gender = jsonOpenObj.getString("gender");
		        		} else if(channel.equals("weixin")) {
		        			JSONObject jsonOpenObj = jsonObject.getJSONObject("personal");
		        			gender = jsonOpenObj.getString("gender");
		        		} else
		        			continue;	        		
		        	}
		        			        		        	
		        	//gender:male  男 /  gender:female  女
		        	if(gender.equals("男") || gender.equals("m"))
		        		gender = "male";
		        	else if(gender.equals("女") || gender.equals("f"))
		        		gender = "female";
		        	else if(gender==null || uid==null || !(gender.equals("f")||gender.equals("m")) )
		        		continue;
		        
		        	//SolrUtil.updateUserInfo(uid,"gender", gender);   //solr
		        	
		        	//redis中没有该用户的用户画像
		        	if(RedisUtil.getTagsFromRedis(uid) == null){
		        		count_null++;		        		
		        		continue;
		        	}
		        			     		        	
		        	Map<String,String> map = new HashMap<String,String>();
		        	map.put("gender", gender);
		        	pipeline_uc.hmset(uid, map);  		
		        	count_value++;
		        	
		        	LOG.info("Update user: " + uid + " as " + gender);
		        } catch (Exception ex){
		        	count++;
		        	//LOG.error(ex);
		        	continue;
		        }	 
		        
		        tmp_num++;	        
		        if (tmp_num >= 50000) {
					try {
						pipeline_uc.sync();
						System.out.println("同步...");
					} catch (Exception e) {
						LOG.error("pipeline sync error:", e);
						e.printStackTrace();
					}
					tmp_num = 1;
				}		        
		    }
		    
		    System.out.println("There are "+ count + " damaged messages");
		    System.out.println("There are "+ count_null + " users don't have their Personas");	 
		    System.out.println("There are "+ count_value + " users update their Personas successfully");	
		    
		    LOG.info("There are "+ count + " damaged messages");	 
		    LOG.info("There are "+ count_null + " users don't have their Personas");	 
		    LOG.info("There are "+ count_value + " users update their Personas successfully");	 
		    br.close();	    
		    pipeline_uc.sync();         //同步
		} catch (IOException e) {
			LOG.error(e);
		} 
	}
}
