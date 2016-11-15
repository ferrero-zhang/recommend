package com.ifeng.iRecommend.kedm.userCenterTag;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import com.google.gson.Gson;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.HttpRequest;
import com.ifeng.iRecommend.kedm.userCenterTag.DoTagMain.Output;
import com.ifeng.iRecommend.kedm.userCenterTag.DoTagMain.Usertag;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.userShardedRedisUtil;

public class DoTagTest {
	private static final Log log = LogFactory.getLog(DoTagMain.class);
	private static Map<String,List<Usertag>> classify_usertag = new HashMap<String,List<Usertag>>();
	private static Map<String,List<Output>> user_tag = new HashMap<String,List<Output>>();
	private static String[] classify_tags = {"美食","旅游","教育资讯","历史","时尚","房产","家居",
		"大陆时事","创业","文化","星座","台湾","汽车","动漫","数码","足球","互联网",
		"社会资讯","篮球","国际","健康","财经","娱乐","军事"};//给定推送标签
	private static String[] test_tags = {"FUN来了"};
	private static String[] finace_tags = {"股市","基金","理财","银行","商业"};//财经小标签
	private static String[] ent_tags = {"音乐","电影","电视娱乐","明星"};//娱乐小标签
	private static String[] edu_tags = {"中小学教育","教育资讯","职业培训","公务员","高考","早教","考研","商学院教育","留学","在线教育"};
	private static List<String> usertags = new ArrayList<String>();
	
	public static List<String> getUserids(String path){
		List<String> uids = new ArrayList<String>();
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				uids.add(line);
			}
		}catch(Exception e){
			log.error("read uids from file error");
			e.printStackTrace();
		}
		fileutil.CloseRead();
		return uids;
	}
	public static Map<String,String> getTagsFromRedis(String uid){
		ShardedJedisPool jedispool = userShardedRedisUtil.getJedisPoolSlave();
		Map<String,String> res = null;
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
	    	System.out.println(jedis.exists(uid));
	    	res = jedis.hgetAll(uid);
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
	static class Usertag{
		private String name;
		private String df;
		private double score;
		private double tfscore;
		private String topt;
		public String getTopt() {
			return topt;
		}
		public void setTopt(String topt) {
			this.topt = topt;
		}
		public double getTfscore() {
			return tfscore;
		}
		public void setTfscore(double tfscore) {
			this.tfscore = tfscore;
		}
		public Usertag(String name,double score,double tfscore){
			this.name = name;
			this.score = score;
			this.tfscore = tfscore;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDf() {
			return df;
		}
		public void setDf(String df) {
			this.df = df;
		}
		public double getScore() {
			return score;
		}
		public void setScore(double score) {
			this.score = score;
		}
	}
	static class Output{
		private String tag;
		private double tfscore;
		private String top;
		public String getTop() {
			return top;
		}
		public void setTop(String top) {
			this.top = top;
		}
		public Output(String tag,double tfscore){
			this.tag = tag;
			this.tfscore = tfscore;
		}
		public String getTag() {
			return tag;
		}
		public void setTag(String tag) {
			this.tag = tag;
		}
		public double getTfscore() {
			return tfscore;
		}
		public void setTfscore(double tfscore) {
			this.tfscore = tfscore;
		}
	}
	public static void getTagStr(String uid,String info){
		int count =0;
		String[] taginfo = info.split("\\$");
		StringBuffer sb = new StringBuffer();
		Set<String> limittags = new HashSet<String>();
		for(String lt : test_tags){
			limittags.add(lt);
		}
		List<String> finace = Arrays.asList(finace_tags);
		List<String> ent = Arrays.asList(ent_tags);
		List<String> edu = Arrays.asList(edu_tags);
		sb.append(uid).append(" ");
		for(String tag : taginfo[0].split("#")){
			String[] t = tag.split("_");
			if(count > 9)
				break;
			if(t.length != 3)
				continue;
			/*if(Integer.parseInt(t[1]) < 3)
				continue;*/
			if(!limittags.contains(t[0])){
				continue;
				/*if(finace.contains(t[0])){
					if(sb.toString().contains("财经")){
						continue;
					}
					sb.append("财经").append(" ");
				}else if(ent.contains(t[0])){
					if(sb.toString().contains("娱乐")){
						continue;
					}
					sb.append("娱乐").append(" ");
				}else if(edu.contains(t[0])){
					if(sb.toString().contains("教育资讯")){
						continue;
					}
					sb.append("教育资讯").append(" ");
				}else{
					continue;
				}*/
			}else{
				sb.append(t[0]).append(" ");
			}
			count++;
		}
		if(count != 0){
			usertags.add(sb.toString().trim());
		}
	}
	public static void dotagToMap(String uid,String info){
		if(info == null || info.equals("null&null")){
			return;
		}
		String[] tagsStr = info.split("\\$");
		if(tagsStr.length != 2)
			return;
		double score = 0;
		double realscore = 0;
		double tfscore = 0;
		double realtimeW = 3;
		double tfScoreW = 7;
		StringBuffer sb = new StringBuffer();
		try{
			
			for(String tag : test_tags){
				if(!info.contains(tag))
					continue;
				if(tagsStr[1].contains(tag)){
					realscore = 0.1;
				}
				for(String taginfo : tagsStr[0].split("#")){
					if(taginfo.contains(tag)){
						tfscore = Double.parseDouble(taginfo.split("_")[2]);
						break;
					}
				}
				score = (realscore*realtimeW + tfscore*tfScoreW)/(realtimeW+tfScoreW);
				Usertag ut = new Usertag(uid,score,tfscore);
				if(sb != null && !sb.toString().trim().equals("")){
					ut.setTopt(sb.toString().trim());
				}
				//System.out.println(uid + tag + score+tfscore);
				//log.info(uid + tag + score+tfscore);
				if(classify_usertag.containsKey(tag)){
					classify_usertag.get(tag).add(ut);
				}else{
					List<Usertag> temp = new ArrayList<Usertag>();
					temp.add(ut);
					classify_usertag.put(tag, temp);
				}
			}
		}catch(Exception e){
			log.error("dotag to map error",e);
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args){
		
		Set<String> uids = new HashSet<String>();
		Set<String> uids_baidu = new HashSet<String>();
		Set<String> uids_phone = new HashSet<String>();
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize("E:/test_data.txt", "utf-8");
		FileUtil fileutil2 = new FileUtil();
		fileutil2.Initialize("E:/res_imei3.txt", "utf-8");
		try{
			String line = null;
			int no = 0;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				String[] temp = line.split(";");
				if(!temp[4].trim().equals(""))
					uids.add(temp[4]);
				/*StringBuffer sb = new StringBuffer();
				for(String s : temp){
					try{
						String msg = URLDecoder.decode(s,"UTF-8");
						Pattern p = Pattern.compile("([\u4e00-\u9fa5]+)");
						Matcher m = p.matcher(msg);
						int count = 0;
						while(m.find()){
							sb.append(m.group(count)).append(" ");
							count++;
						}
					}catch(Exception e){
						e.printStackTrace();
						no++;
					}
					
				}
				if(sb != null && sb.toString().length()>0){
					fileutil2.Append2File("E:/res_imei3.txt", temp[3]+";"+temp[4]+";"+sb.toString()+"\n");
					if(line.contains("baidu") && line.contains("title")){
						uids_baidu.add(temp[3]);
					}
					if(temp[4] != null && !temp[4].trim().equals("")){
						uids_phone.add(temp[3]);
					}
					uids.add(temp[3]);
				}
				no++;*/
			}
			System.out.println("user size "+uids.size());
			System.out.println("baidu user "+uids_baidu.size());
			System.out.println("baidu user "+uids_phone.size());
		}catch(Exception e){
			log.error("read uids from file error");
			e.printStackTrace();
		}
		fileutil.CloseRead();
		fileutil2.CloseRead();
	}

}
