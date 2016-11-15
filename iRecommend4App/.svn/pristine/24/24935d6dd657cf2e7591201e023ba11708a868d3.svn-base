package com.ifeng.iRecommend.kedm.userCenterTag;

import java.io.File;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.userShardedRedisUtil;

public class DoTagMain {
	private static final Log log = LogFactory.getLog(DoTagMain.class);
	private static Map<String,List<Usertag>> classify_usertag = new HashMap<String,List<Usertag>>();
	private static Map<String,List<Output>> user_tag = new HashMap<String,List<Output>>();
	private static String[] classify_tags = {"美食","旅游","教育资讯","历史","时尚","房产","家居",
		"大陆时事","创业","文化","星座","台湾","汽车","动漫","数码","足球","互联网",
		"社会资讯","篮球","国际","健康","财经","娱乐","军事","游戏","情感","大陆人事"};//给定推送标签
	private static String[] classify_topic2 = {"暖新闻","大陆人事"};
	private static String[] finace_tags = {"股市","基金","理财","银行","商业"};//财经小标签
	private static String[] ent_tags = {"音乐","电影","电视娱乐","明星"};//娱乐小标签
	private static String[] edu_tags = {"中小学教育","教育资讯","职业培训","公务员","高考","早教","考研","商学院教育","留学","在线教育"};
	private static List<String> usertags = new ArrayList<String>();
	private static int test = 0;
	
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
	    		if(jedis.hexists(uid, "t2"))
	    			break;
	    	}
	    	res = jedis.hget(uid, "t2");
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
	    		if(info != null && !info.isEmpty()){
	    			if(info.get("t1") == null || info.get("t1").equals("null$null")){
	    				test++;
	    			}
		    		getTagStr(uids.get(count),info.get("t1"),info.get("t2"),info.get("t3"));
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
			try{
				
				if(borrowOrOprSuccess == true)
					userShardedRedisUtil.returnResource(sjedis, jedispool);
				sjedis.disconnect();
			}catch(Exception e){
				if(borrowOrOprSuccess == true)
					userShardedRedisUtil.returnResource(sjedis, jedispool);
				e.printStackTrace();
			}
			
		}
		return res;
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
	public static void getTagStr(String uid,String info,String topic2,String topic3){
		int count =0;
		if(info == null)
			return;
		String[] taginfo = info.split("\\$");
		StringBuffer sb = new StringBuffer();
		Set<String> limittags = new HashSet<String>();
		for(String lt : classify_tags){
			limittags.add(lt);
		}
		int nuanNews_num = 0;
		int renshi_num = 0;
		double fun_score = 0;
		double xijinp_score = 0;
		int fun_num = 0;
		int xijinp_num = 0;
		
		if(topic2 != null && (topic2.contains("暖新闻") || topic2.contains("大陆人事") || topic2.contains("FUN来了"))){
			String topic2info = topic2.split("\\$")[0];
			for(String t2tag : topic2info.split("#")){
				String[] t2 = t2tag.split("_");
				if(t2tag.contains("暖新闻")){
					nuanNews_num = Integer.parseInt(t2[1]);
				}
				if(t2tag.contains("大陆人事")){
					renshi_num = Integer.parseInt(t2[1]);
				}
				if(t2tag.contains("FUN来了")){
					if(fun_num == 0){
						fun_score = Double.parseDouble(t2[2]);
					}
					fun_num += Integer.parseInt(t2[1]);
					
				}
			}
		}
		if(topic3 != null && topic3.contains("习近平")){
			String topic3info = topic3.split("\\$")[0];
			for(String t3tag : topic3info.split("#")){
				String[] t3 = t3tag.split("_");
				
				if(t3tag.contains("习近平")){
					xijinp_num = Integer.parseInt(t3[1]);
					xijinp_score = Double.parseDouble(t3[2]);
				}
			}
		}
		List<String> finace = Arrays.asList(finace_tags);
		List<String> ent = Arrays.asList(ent_tags);
		List<String> edu = Arrays.asList(edu_tags);
		sb.append(uid).append(" ");
		for(String tag : taginfo[0].split("#")){
			String[] t = tag.split("_");
			/*if(count > 9)
				break;*/
			if(t.length != 3)
				continue;
			if(Integer.parseInt(t[1]) < 3)
				continue;
			
			if(fun_score > Double.parseDouble(t[2]) && fun_num > 5){
				sb.append("FUN来了").append(" ");
				fun_score = 0;
				fun_num = 0;
			}
			if(xijinp_score > Double.parseDouble(t[2]) && xijinp_num > 5){
				sb.append("习近平").append(" ");
				xijinp_score = 0;
				xijinp_num = 0;
			}
			if(nuanNews_num > Integer.parseInt(t[1])){
				sb.append("暖新闻").append(" ");
				nuanNews_num = 0;
			}
			if(!taginfo[0].contains("大陆人事")){
				if(renshi_num > Integer.parseInt(t[1])){
					sb.append("人事").append(" ");
					renshi_num = 0;
				}
			}
			if(taginfo[0].split("#").length > 5 && (Integer.parseInt(t[1]) < 3 || Double.parseDouble(t[2]) < 0.4))
				continue;
			if(!limittags.contains(t[0])){
				if(finace.contains(t[0])){
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
				}
			}else{
				if(t[0].equals("大陆人事")){
					t[0] = "人事";
				}
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
			if(tagsStr[0].split("#").length > 3){
				for(int i = 0;i<3;i++){
					String[] temp_tag = tagsStr[0].split("#")[i].split("_");
					if(temp_tag.length != 3)
						continue;
					if(Integer.parseInt(temp_tag[1]) > 3 && Double.parseDouble(temp_tag[2]) > 0.5){
						sb.append(temp_tag[0]).append(" ");
					}
				}
			}
			for(String tag : classify_tags){
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
				log.info(uid + tag + score+tfscore);
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
		/*if(args.length != 2){
			System.out.println("Usage <uidfile> <outputfile>");
			System.exit(1);
		}*/
		args = new String[]{"E:/dayliyUser_a","E:/usertags20160328a"};
		long s = System.currentTimeMillis();
		String teest = getTagsFromRedis("27e0d74dca278a92d68aeddbdac0db158a6009c1");
		File file = new File(args[1]);
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(args[1], "utf-8");
		List<String> uids = getUserids(args[0]);
		uids.clear();
		uids.add("A000004F617DC8");
		log.info("user size "+uids.size());
		int rows = uids.size()/5000;
		try{
			for(int i=0 ; i <= rows ; i++){
				int len = 5000*(i+1);
				if(len > (uids.size()-1)){
					len = uids.size()-1;
				}
				if(len < i*5000)
					break;
				List<String> once_uids = uids.subList(i*5000, len);
				try{
					once_uids.add("A000004F617DC8");
					getTagsFromPipeline(once_uids);
					log.info("fihish get tags from pipeline sublist "+ len);
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
			for(String info : usertags){
				fileutil.Append2File(args[1],info+"\n");
			}
			fileutil.CloseRead();
			System.out.println("test size is "+test);
			log.info("finish write to flie");
			log.info(System.currentTimeMillis()-s);
		}catch(Exception e){
			log.error("do user tag error",e);
			e.printStackTrace();
		}
		
		/*try{
			for(String tag : classify_tags){
				log.info("do tag "+ tag);
				List<Usertag> uts = classify_usertag.get(tag);
				if(uts == null || uts.isEmpty())
					continue;
				System.out.println(tag + " user tag size is "+uts.size());
				long ss = System.currentTimeMillis();
				Collections.sort(uts, new Comparator<Usertag>(){

					@Override
					public int compare(Usertag o1, Usertag o2) {
						// TODO Auto-generated method stub
						double res = o2.getScore() - o1.getScore();
						if(res > 0){
							return 1;
						}else if(res < 0){
							return -1;
						}
						return 0;
					}});
				int count = 0;
				for(Usertag ut : uts){
					if((uts.size()/2) < count)
						break;
					Output op = new Output(tag,ut.getTfscore());
					op.setTop(ut.getTopt());
					if(user_tag.containsKey(ut.getName())){
						user_tag.get(ut.getName()).add(op);
					}else{
						List<Output> temp = new ArrayList<Output>();
						temp.add(op);
						user_tag.put(ut.getName(), temp);
					}
					count++;
				}
				log.info("sort spend "+(System.currentTimeMillis()-ss));
				
			}
			log.info("finish classify tag..");
			for(String u : user_tag.keySet()){
				StringBuffer sb = new StringBuffer();
				sb.append(u).append(" ");
				List<Output> utags = user_tag.get(u);
				Collections.sort(utags, new Comparator<Output>(){

					@Override
					public int compare(Output o1, Output o2) {
						// TODO Auto-generated method stub
						double res = o2.getTfscore() - o1.getTfscore();
						if(res > 0){
							return 1;
						}else if(res < 0){
							return -1;
						}
						return 0;
					}});
				int count = 0;
				for(Output t : utags){
					count++;
					if(count > 5)
						break;
					sb.append(t.getTag()).append(" ");
				}
				if(utags.size() < 3){
					String top = utags.get(0).getTop();
					if(top != null){
						String[] temp_top = top.split(" ");
						for(String tt : temp_top){
							if(sb.toString().contains(tt))
								continue;
							sb.append(top).append(" ");
						}
					}
				}
				fileutil.Append2File(args[1], sb.toString().trim()+"\n");
			}
			fileutil.CloseRead();
			log.info("finish write to flie");
			log.info(System.currentTimeMillis()-s);
		}catch(Exception e){
			log.error("do sort error",e);
			e.printStackTrace();
		}*/
	}

}
