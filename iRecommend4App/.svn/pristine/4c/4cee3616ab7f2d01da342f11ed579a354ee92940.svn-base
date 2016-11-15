package com.ifeng.iRecommend.zxc.bdhotword;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import redis.clients.jedis.Jedis;

import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.zxc.util.FileUtils;


public class SameEventCombiner {
	private static final Logger log= Logger.getLogger(SameEventCombiner.class);
	public static final String CHILD_KEY_PREFIX="SameEventCombiner_c_1_";
	public static final String PARENT_KEY_PREFIX="SameEventCombiner_p_";
	public static final String DEALED_KEY="SameEventCombiner_Dealed";
	Jedis jedis=null;
	private static class HotWordInfoTime implements Comparable<HotWordInfoTime>{
		private String key;
		private HotWordInfo info;
		public HotWordInfoTime(String key,HotWordInfo info){
			this.info=info;
			this.key=key;
		}
		@Override
		public int compareTo(HotWordInfoTime o) {
			// TODO Auto-generated method stub
			if(this.info.getStarttimestamp()>info.getStarttimestamp()){
				return -1;
			}else if(this.info.getStarttimestamp()<info.getStarttimestamp()){
				return 1;
			}
			return 0;
		}
	}
/*	public double getSimRatio(String s1,String s2){
		char[] chars1=s1.toCharArray();
		int sameCount=0;
		for(int k=0;k<chars1.length;k++){
			if(s2.contains(String.valueOf(chars1[k]))){
				sameCount++;
			}
		}
		return (double)sameCount/s2.length();
	}*/
	public double getSimRatio(String s1,String s2){

		String[] temps1=s1.split("\\s+");
		int countAll=0;
		int sameCount=0;
		for(int k=0;k<temps1.length;k++){
			
			/*if(!temps1[k].matches("[0-9A-Za-z]+")){
				char[] cs=temps1[k].toCharArray();
				for(int i=0;i<cs.length;i++){
					countAll++;
					if(s2.contains(String.valueOf(cs[i]))){
						sameCount++;
					}
				}
			}else{*/
				countAll++;
				if(s2.contains(temps1[k])){
					sameCount++;
			//	}
			}
			
			
		}
		return (double)sameCount/countAll;
	}
	private void doCombine(){
		String rt="";
		Set<String> s=new HashSet<String>();
		HotWordData hotWordData =HotWordData.getInstance();
		Map<String, HotWordInfo> map=hotWordData.getHotwordMap();
		Set<String> keys=map.keySet();
		List<HotWordInfoTime> infos=new ArrayList<HotWordInfoTime>();
		for(String key:keys){
			HotWordInfo info=map.get(key);
			String hotWordSplit=info.getSplitContent();
			String keyTemp=key.replaceAll("[a-zA-Z0-9]+", "A");
			if(hotWordSplit.split(" ").length>=3||keyTemp.length()>5){
				key=key.toLowerCase();
				if(s.contains(key)){
					continue;
				}
				s.add(key);
				HotWordInfoTime infot=new HotWordInfoTime(key,info);
				infos.add(infot);
			}
		}
		HotWordInfoTime[] arr=new HotWordInfoTime[infos.size()];
		infos.toArray(arr);
		Arrays.sort(arr);
		
		for(int i=0;i<arr.length;i++){
			HotWordInfoTime item=arr[i];
			String key1=item.key;
			String skey1=item.info.getSplitContent().replaceAll("_[a-z]{1,} {0,1}", " ").toLowerCase().trim();
			if(jedis.sadd(SameEventCombiner.DEALED_KEY, key1)<1){
				continue;
			}
			
			String keyP=null;
			List<String> simevent=new ArrayList<String>();
			
			if(!jedis.exists(item.key)){
				for(int j=i+1;j<arr.length;j++){
					HotWordInfoTime item2=arr[j];
					String key2=item2.key;
					
					String skey2=item2.info.getSplitContent().replaceAll("_[a-z]{1,} {0,1}", " ").toLowerCase().trim();
			
					double ratio1=getSimRatio(skey1,skey2);
					double ratio2=getSimRatio(skey2,skey1);
					double ratioHold=1;
					/*if(key1.replaceAll("[a-zA-Z0-9]{2,}", "").length()<key1.length()){
						ratioHold=1;
					}
					if(key2.replaceAll("[a-zA-Z0-9]{2,}", "").length()<key2.length()){
						ratioHold=1;
					}*/
					if(ratio1>=ratioHold||ratio2>=ratioHold){
						//这里 名字短的会作为主名，因为名字越短，概括性越强，tag文章看起来会更合适
						//
						//if(item.info.getStarttimestamp()-item2.info.getStarttimestamp()<1000l*60*60*24*4){
						simevent.add(key2);	
						if(key2.replaceAll("[a-zA-Z0-9]+", "").length()<=key1.replaceAll("[a-zA-Z0-9]+", "").length()){
								keyP=key2;
								
							}else{
								keyP=key1;
								
								SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");
								Date d=new Date(item.info.getStarttimestamp());
								Date d2=new Date(item2.info.getStarttimestamp());
								log.info("use new event name="+keyP+sf.format(d)+", because the new one is shorter. the old="+key2+sf.format(d2));
							}
						//}
					}

				}
			}
			if(keyP!=null&&simevent.size()>0){
				simevent.add(key1);
				for(String key:simevent){
					if(!key.equals(keyP)){
						jedis.del(SameEventCombiner.PARENT_KEY_PREFIX+key);
						jedis.set(SameEventCombiner.CHILD_KEY_PREFIX+key, keyP);
						log.info("Combined\t"+key+"--->"+keyP);
						jedis.sadd(SameEventCombiner.PARENT_KEY_PREFIX+keyP,key);
					}
				}
			}
		}

	}
	public void init(){
		jedis = new Jedis(Config.REDIS_IP, Config.REDIS_PORT);
		
		jedis.select(Config.SAME_EVENT_DB);
		Set<String> s=jedis.keys(PARENT_KEY_PREFIX+"*");
		for(String ss:s){
			System.out.println(ss);
		}
	}
	public void close(){
		jedis.close();
	}
	
	public static void addSameEventManually(String pname,String cname){
		SameEventCombiner c=new SameEventCombiner();
		c.init();
		c.jedis.sadd(SameEventCombiner.CHILD_KEY_PREFIX+cname, pname);
		c.jedis.sadd(SameEventCombiner.PARENT_KEY_PREFIX+pname,cname);
		c.close();
	}
	public static void main(String[] args){
		args=new String[]{"conf"};
		DOMConfigurator.configure(args[0]+"/log4j.xml");
		SameEventCombiner c=new SameEventCombiner();
		c.init();
		c.doCombine();
		c.close();
		//addSameEventManually("女子和颐酒店遇袭","女子和颐酒店遭陌生男劫持");
	}
}
