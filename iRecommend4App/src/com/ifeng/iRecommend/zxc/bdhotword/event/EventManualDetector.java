package com.ifeng.iRecommend.zxc.bdhotword.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.ifeng.iRecommend.zxc.bdhotword.Config;
import com.ifeng.iRecommend.zxc.bdhotword.bean.BDHotWordBean;
import com.ifeng.iRecommend.zxc.util.TrieTree;

public class EventManualDetector extends EventDetector{
	static Logger LOG = Logger.getLogger(EventManualDetector.class);

	public Set<String> manualEventNames=null;
	public boolean isValid(Map<String, List<Integer>> wordsToPositions,EventBean bean,String str,boolean isTitle,int beanWordExistCount){

		
		for(String mustNot:bean.mustNotExist){
			if(str.contains(mustNot)){
				return false;
			}
		}
		Map<String, List<Integer>> wordsToPositions3=new HashMap<String,List<Integer>>();
		Map<String, List<Integer>> wordsToPositions4=new HashMap<String,List<Integer>>();
		//在匹配词汇中进行同义词转换
		Set<String> ks=wordsToPositions.keySet();
		for(String word:ks){
			if(bean.mayExist.contains(word)){
				wordsToPositions3.put(word, wordsToPositions.get(word));
			}
		}
		for(String word:wordsToPositions3.keySet()){
			String keyWord=bean.simWord2KeyWord.get(word);
			if(keyWord!=null){
				List<Integer> newPos=wordsToPositions4.get(keyWord);
				if(newPos==null){
					newPos=new ArrayList<Integer>();
					wordsToPositions4.put(keyWord,newPos);
				}
				List<Integer> poskey=wordsToPositions3.get(word);
				if(poskey!=null){
					newPos.addAll(poskey);
				}
				
			}else{
				List<Integer> oldPos=wordsToPositions4.get(word);
				if(oldPos==null){
					wordsToPositions4.put(word, wordsToPositions3.get(word));
				}else{
					oldPos.addAll(wordsToPositions3.get(word));
					
				}
				
			}
			
		}
		if(isTitle){
			
			for(int i=0;i<bean.existInTitleDetermined.size();i++){
				int count=0;
				for(String word:wordsToPositions4.keySet()){
					if(bean.existInTitleDetermined.get(i).contains(word)){
						count++;
					}
				}
				if(count==bean.existInTitleDetermined.get(i).size()&&count>0){
					return true;
				}
			}
			
			for(String mustExist:bean.mustExistInTitle){
				if(!str.contains(mustExist)){
					return false;
				}
			}
			return false;
		}else{
			if(bean.mayExist.size()<4){
				return false;
			}
			int minPos=Integer.MAX_VALUE;
			int maxPos=-1;
			int wExistCount=0;
			for(List<Integer> poses:wordsToPositions4.values()){
				for(Integer pos:poses){
					 wExistCount++;
					if(pos>maxPos){
						maxPos=pos;
					}
					if(pos<minPos){
						minPos=pos;
					}
				}
			}
			
			//内容中，必须出现词汇的限制检测
			for(String word: bean.words2CountThreshold.keySet()){
				if(wordsToPositions4.containsKey(word)){
					int count=wordsToPositions4.get(word).size();
					if(count<bean.words2CountThreshold.get(word)){
						return false;
					}
					double density=(double)count/(maxPos-minPos);
					if(bean.words2DensityThreshold.get(word)!=null&&density<bean.words2DensityThreshold.get(word)){
						return false;
					}
				}else{
					return false;
				}
			}
			
			
			//在内容中，主题词汇出现的范围比例
			double rangeRatio=(maxPos-minPos)/(double)str.length();
			
			//在内容中，出现主题词占总主题词的比例
			double subWordsRatio=wordsToPositions3.keySet().size()/(double)bean.mayExist.size();
			//在内容中，主题词汇出现的密度
			double density= wExistCount/(double)(maxPos-minPos);
			if(bean.rangeRatioThreshold>0&&rangeRatio<bean.rangeRatioThreshold){
				return false;
			}
			if(bean.mayWordsRatioThreshold>0&&subWordsRatio<bean.mayWordsRatioThreshold){
				return false;
			}
			if(bean.densityThreshold>0&&density<bean.densityThreshold){
				return false;
			}
			if(bean.matchWordCountThreshold>0&&wordsToPositions4.keySet().size()<bean.matchWordCountThreshold){
				return false;
			}
			return true;
		}
		
	}
	
	public void readEvent(){
		Jedis jedis = new Jedis(Config.REDIS_IP, Config.REDIS_PORT);
		try{
			jedis.select(Config.EVENT_DB);
			Set<String> keys=jedis.keys("*");
			Map<String,Set<EventBean>> wordToEventBeansTemp=new HashMap<String,Set<EventBean>>();
			Set<String> wordsMatch=new HashSet<String>();
			for(String key:keys){
				String json=jedis.get(key);
				EventBean bean=EventBean.fromJsonStr(json);
				List<String> words=new ArrayList<String>();
				words.addAll(bean.mayExist);
				for(String word:words){
					wordsMatch.add(word);
					Set<EventBean> beans=wordToEventBeansTemp.get(word);
					if(beans==null){
						beans=new HashSet<EventBean>();
						wordToEventBeansTemp.put(word,beans );
					}
					beans.add(bean);
				}
	
			}
			jedis.close();
			TrieTree tempt=new TrieTree(wordsMatch);
			manualEventNames= keys;
			this.wordsMatchTree=tempt;
	
			this.wordToEventBeans=wordToEventBeansTemp;
		}catch(Exception e){
			LOG.error(e.getMessage(),e);
		}finally{
			jedis.close();
		}
	}
	public BDHotWordBean detectEvent(String title,String content){
		try{
			Set<BDHotWordBean> rt=match(title,true);
			for(BDHotWordBean b:rt){
				b.setScore(1);
				return b;
			}
			rt=match(content,false);
			for(BDHotWordBean b:rt){
				b.setScore(0.8);
				return b;
			}
		}catch(Exception e){
			LOG.error(e.getMessage(),e);
		}
		return null;
	}
	public boolean isManualConfig(String eventName){
		return manualEventNames.contains(eventName);
	}
}
