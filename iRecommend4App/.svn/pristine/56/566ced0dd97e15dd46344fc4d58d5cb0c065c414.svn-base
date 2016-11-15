package com.ifeng.iRecommend.zxc.bdhotword.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zxc.bdhotword.Config;
import com.ifeng.iRecommend.zxc.util.FileUtils;




public class EventBean{
	String name;
	List<EventBean> sameEventName=new ArrayList<EventBean>();
	List<Set<String>> existInTitleDetermined;
	//主题词汇集
	Set<String> mayExist=new HashSet<String>();
	Set<String> mustNotExist=new HashSet<String>();
	//标题中必须出现的词
	Set<String> mustExistInTitle=new HashSet<String>();
	//内容中必须出现的词及其出现次数
	Map<String,Integer> words2CountThreshold=new HashMap<String,Integer>();
	Map<String,Double> words2DensityThreshold=new HashMap<String,Double>();
	//主词 to 其同义词
	Map<String,List<String>> keyWord2SimWord=new HashMap<String,List<String>>();
	//同义词 to 词
	Map<String,String> simWord2KeyWord=new HashMap<String,String>();
	String splitName;
	String deadLine=null;
	double rangeRatioThreshold=0.6;
	double densityThreshold=(double)1/250;
	double mayWordsRatioThreshold=0.3;
	int matchWordCountThreshold =3;
	public EventBean(){}
	private Set<String> toSet(String str){
		Set<String> l=new HashSet<String>();
		if(str!=null){
			
		
		for(String s:str.split(",|，")){
			if(s.trim().length()>0){
				l.add(s);
			}
			
		}
		}
		return l;
	}
	
	public String getSplitName() {
		return splitName;
	}
	public void setSplitName(String splitName) {
		this.splitName = splitName;
	}
	public EventBean(String name, String deadLine,String mayExist,
			String mustNotExist,String mustExistInTitle,String existInTitleDetermined,
			Map<String,List<String>> keyWord2SimWord,
			Map<String,Integer> words2CountThreshold,Map<String,Double> words2DensityThreshold,double rangeRatioThreshold,
			double densityThreshold,double mayWordsRatioThreshold,
			int matchWordCountThreshold) {
		super();
		if(deadLine!=null&&deadLine.length()>0){
			this.deadLine=deadLine;
		}
		this.words2DensityThreshold=words2DensityThreshold;
		this.existInTitleDetermined=new ArrayList<Set<String>>();
		String[] arr=existInTitleDetermined.split("\\|\\|\\|");
		for(int i=0;i<arr.length;i++){
			Set<String> s=toSet(arr[i]);
			this.existInTitleDetermined.add(s);
		}
		this.words2CountThreshold=words2CountThreshold;
		this.name = name;
		this.mayExist =toSet(mayExist);
		this.mustNotExist =toSet( mustNotExist);
		this.mustExistInTitle =toSet(mustExistInTitle);
		this.keyWord2SimWord=keyWord2SimWord;
		if(rangeRatioThreshold>=0){
			this.rangeRatioThreshold=rangeRatioThreshold;
		}
		
		if(densityThreshold>=0){
			this.densityThreshold=densityThreshold;
		}
		if(mayWordsRatioThreshold>=0){
			this.mayWordsRatioThreshold=mayWordsRatioThreshold;
		}
		if(matchWordCountThreshold>=0){
			this.matchWordCountThreshold=matchWordCountThreshold;
		}
		
		simWord2KeyWord=new HashMap<String,String>();
		for(String key:keyWord2SimWord.keySet()){
			List<String> words=keyWord2SimWord.get(key);
			for(String word:words){
				simWord2KeyWord.put(word, key);
			}
		}
	}
	
	public String toJsonStr(){
		Gson gson = new Gson();  
        String result = gson.toJson(this);
        return result;
	}
	public static EventBean fromJsonStr(String json){
		Gson gson = new Gson(); 
		EventBean bean=gson.fromJson(json, EventBean.class);
		return bean;
	}

	public void save(Jedis jedis){
		boolean nullJedis=false;
		if(jedis==null){
			 jedis = new Jedis(Config.REDIS_IP, Config.REDIS_PORT);
			 nullJedis=true;
		}
		
		try{
		jedis.select(Config.EVENT_DB);
		//this.mustWordsTree=null;
		String jos=this.toJsonStr();
		jedis.set(this.name, jos);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(nullJedis){
				jedis.close();
			}
		}
	}
	public static void del(List<String> names){
		Jedis jedis = new Jedis(Config.REDIS_IP, Config.REDIS_PORT);
		try{
		jedis.select(Config.EVENT_DB);
		for(String name:names){
			jedis.del(name);
		}
		
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			jedis.close();
		}
	}
	public static void del(String name){
		Jedis jedis = new Jedis(Config.REDIS_IP, Config.REDIS_PORT);
		try{
		jedis.select(Config.EVENT_DB);
		jedis.del(name);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			jedis.close();
		}
	}
	public EventBean(String name,Set<String> mayExist){
		this.name = name;
		this.mayExist =mayExist;
	}
	@Override
	public boolean equals(Object o){
		if(this==o){
			return true;
		}
		if(o instanceof EventBean){
			EventBean that=(EventBean)o;
			if(this.name.equals(that.name)){
				return true;
			}
		}
		return false;
	}
	@Override
	public int hashCode(){
		return name.hashCode();
	}

	public static EventBean getBeanFromDb(String name){
		Jedis jedis = new Jedis(Config.REDIS_IP, Config.REDIS_PORT);
		try{
			jedis.select(Config.EVENT_DB);
			String data=jedis.get(name);
			EventBean bean=EventBean.fromJsonStr(data);
			System.out.println(bean.toJsonStr().replace("\"", "\\\""));
			return bean;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			jedis.close();
		}
		return null;
	}
	
	public String getName() {
		return name;
	}
	public static Set<EventBean> getBeansFromDb(){
		return getBeansFromDb("*");
	}
	public static Set<EventBean> getBeansFromDb(String prefix){
		Set<EventBean> event=new HashSet<EventBean>();
		Jedis jedis = new Jedis(Config.REDIS_IP, Config.REDIS_PORT);
		try{
			jedis.select(Config.EVENT_DB);
			Set<String> keys=jedis.keys(prefix);
			for(String key:keys){
				String json=jedis.get(key);
				EventBean bean=EventBean.fromJsonStr(json);
				event.add(bean);
				//bean.mustWordsTree=null;
				System.out.println(bean.toJsonStr().replace("\"", "\\\""));
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			jedis.close();
		}
		return event;
	}
	public static void addBean2db(String json){
		EventBean bean=EventBean.fromJsonStr(json);
		bean.save(null);
	}
	private String setToString(Set<String> set){
		String s="";
		for(String w:set){
			s=w+",";
		}
		if(s.length()>0){
			return s.substring(0,s.length()-1);
		}
		return "";
	}

/*	public String toString(){
		String s=this.name+"\t"+this.deadLine+"\t";
		String mayExistS=setToString(this.mayExist);
		String mustNotS=setToString(this.mustNotExist);
		String determinTitle=setToString(this.existInTitleDetermined);
		String mustInTitle=setToString(this.mustExistInTitle);
		String simWords="";
		for(String key:this.keyWord2SimWord.keySet()){
			simWords=simWords+"|||"+key+"#";
			List<String> ws=keyWord2SimWord.get(key);
			for(String w:ws){
				simWords=simWords+w+",";
			}
			simWords=simWords.substring(0,simWords.length()-1);
		}
		String word2Count="";
		for(String key:this.words2CountThreshold.keySet()){
			word2Count=word2Count+"|||"+key+"#";
			Integer ws=words2CountThreshold.get(key);
			word2Count=word2Count+ws;
		}
		String rt=s+mayExistS+"\t"+mustNotS+"\t"+determinTitle+"\t"+mustInTitle+"\t"
				+simWords+"\t"+word2Count+"\t"+this.rangeRatioThreshold+"\t"+this.densityThreshold+
				this.mayWordsRatioThreshold+"\t"+this.matchWordCountThreshold;
		return rt;
	}*/
	public static EventBean fromStr(String s){
		s=s.toLowerCase().replace("，", ",");
		String[] temp=s.split("\t");
		Map<String,List<String>> key2SimWord=new HashMap<String,List<String>>();
		if(temp[6].trim().length()>0){
			String[] simArray=temp[6].split("\\|\\|\\|");
			for(int i=0;i<simArray.length;i++){
				String key=simArray[i].substring(0,simArray[i].indexOf("#"));
				String[] words=simArray[i].substring(simArray[i].indexOf("#")+1,simArray[i].length()).split(",");
				List<String> wl=new ArrayList<String>();
				for(int j=0;j<words.length;j++){
					wl.add(words[j]);
				}
				key2SimWord.put(key, wl);
			}
		}
		Map<String,Integer> w2c=new HashMap<String,Integer>();
		Map<String,Double> w2d=new HashMap<String,Double>();
		if(temp[7].trim().length()>0){
			String[] w2C=temp[7].split("\\|\\|\\|");
			
			for(int i=0;i<w2C.length;i++){
				String[] data=w2C[i].split("#");
				
				String key=data[0];
				Integer count=Integer.valueOf(data[1]);
				if(data.length==3){
					Double d=Double.valueOf(data[2]);
					 w2d.put(key, d);
				}
				w2c.put(key, count);
			}
		}
		
		EventBean bean=new EventBean(temp[0], temp[1],temp[2],
				temp[3],temp[5],temp[4],
				key2SimWord,
				w2c,w2d,Double.valueOf(temp[8]),
				Double.valueOf(temp[9]),Double.valueOf(temp[10]),
				Integer.valueOf(temp[11])); 
		return bean;
	}
	public static void editEvent(String name){
		//String name="香港暴乱";
		EventBean bean=getBeanFromDb(name);
		
		/*bean.words2CountThreshold.put("土耳其", 2);
		//bean.words2CountThreshold.put("爆炸", 2);*/
	//	bean.mayExist.clear();
		bean.mayExist.remove(3);
		//bean.mayExist.add("12");
		//bean.mayExist.add("连");
		//bean.mayExist.add("香港");
		//bean.mayExist.add("包围");
		//bean.mustExist.add("暴乱");
		//bean.matchWordCountThreshold=2;
		bean.matchWordCountThreshold=2;
		//bean.words2CountThreshold.put("赵丽颖", 3);
	//	bean.words2CountThreshold.put("AlphaGo", 2);
		bean.save(null);
	}
	public static String getStrTitle(){
		return "事件名称\tdeadline\t主题词集\t不可出现词集\t标题定义词集\t标题必须出现词集\t同义词配置|||key#word\t"
				+ "内容必须出现词(word2count)\t主题词出现范围比例(默认0.6)\t主题词出现密度(默认1/250)\t主题词相似度（默认0.3）\t主题词出现次数限制(默认3)";
	}
	public static void saveEventBeanFromTxt(String path,Jedis jedis){
		try {
			List<String> ls=FileUtils.readFileByLines(path, true);
			int i=0;
			for(String s:ls){
				i++;
				if(i>1){
					EventBean bean=EventBean.fromStr(s);
					System.out.println(bean.toJsonStr());
					bean.save(jedis);
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args){
		//System.out.println(getStrTitle());
		/*List<String> a=new ArrayList<String>();
		a.add("养老金12连涨");
		a.add("美航母被包围");
		a.add("高三女生被下药");
		
		del(a);*/
		Jedis jedis = new Jedis(Config.REDIS_IP, Config.REDIS_PORT);
		try{
			jedis.select(Config.EVENT_DB);
		saveEventBeanFromTxt("E:\\ifeng\\workspace\\iRecommend4App\\事件配置.txt",jedis);
		}catch(Exception e){
			e.printStackTrace();
		}
		jedis.close();
		//editEvent();
		//addEvent();
	
		//addBean2db();
		//editEvent();
		getBeansFromDb();
		/*Map<String,Integer> must2Count=new HashMap<String,Integer>();
		must2Count.put("天津", 2);
		must2Count.put("爆炸", 2);
		EventBean b=new EventBean("天津港爆炸", "天津,瑞海公司,氰化钠,8·12,滨海新区,损坏车辆,受损车辆",
				null, null,null,
				0,1,must2Count);
		b.save();*/
		//addBean2db("{\"name\":\"天津港爆炸\",\"mayExist\":[\"天津港\",\"天津\",\"瑞海公司\",\"爆炸\",\"危险品\",\"氰化钠\",\"8·12\",\"滨海新区\",\"损坏车辆\",\"受损车辆\"],\"mustNotExist\":[],\"mustExist\":[\"天津\",\"爆炸\"],\"matchWordCount\":0,\"matchWordCountThreshold\":2,\"deadLine\":\"2020-01-01\"}");
	}
}
