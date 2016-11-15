package com.ifeng.iRecommend.zxc.bdhotword.event;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.zxc.bdhotword.Config;
import com.ifeng.iRecommend.zxc.bdhotword.bean.BDHotWordBean;
import com.ifeng.iRecommend.zxc.util.TrieTree;




public class EventDetector {
	static Logger LOG = Logger.getLogger(EventDetector.class);
	TrieTree wordsMatchTree=null;
	TrieTree nameMathTree=null;
	Map<String,Set<EventBean>> wordToEventBeans=null;
	double matchWordRatioThreshold=0.9;
	int existCountThreshold=4;
	double disWordsThreshold=0.017;
	double disThreshold=0.04;
	private static Set<String> stopWords=new HashSet<String>();
	Map<String,String> eName2SplitInDB=new HashMap<String,String>();
	Map<String,JSONObject> eName2Domain2Dis=new HashMap<String,JSONObject>();
	static{
		stopWords.add("的");
		stopWords.add("和");
		stopWords.add("是");
		stopWords.add("曝");
		stopWords.add("在");
		//stopWords.add("中");
	}
	public boolean isManualConfig(String eventName){
		return false;
	}
	private void readENameDomainDis(){

		Jedis j=null;
		
		try{
			j=new Jedis(Config.REDIS_IP,Config.REDIS_PORT);
			Map<String,JSONObject> eName2Domain2DisTmp=new HashMap<String,JSONObject>();
			j.select(Config.EVENT_DOMAIN_STATIC_DB);
			Set<String> keys=j.keys(Config.EVENT_DOMAIN_STATIC_PREFIX+"*");
			for(String k:keys){
				
				eName2Domain2DisTmp.put(k.replace(Config.EVENT_DOMAIN_STATIC_PREFIX, ""),JSONObject.fromObject( j.get(k)));
			}
			this.eName2Domain2Dis=eName2Domain2DisTmp;
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}finally{
			if(j!=null){
				j.close();
			}
			
		}
	}
	private void readENameSplitInDB(){
		Jedis j=null;
		try{
			j=new Jedis(Config.REDIS_IP,Config.REDIS_PORT);
			Map<String,String> eName2SplitInDBTemp=new HashMap<String,String>();
			
			j.select(Config.EVENT_NAME_SPLIT_RECTIFY_DB);
			Set<String> keys=j.keys(Config.EVENT_REC_KEY_PREFIX+"*");
			for(String k:keys){
				eName2SplitInDBTemp.put(k.replace(Config.EVENT_REC_KEY_PREFIX, ""), j.get(k));
			}
			this.eName2SplitInDB=eName2SplitInDBTemp;
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}finally{
			if(j!=null){
				j.close();
			}
			
		}

	}
	public void readEvent(Map<String, HotWordInfo> map){
		readENameSplitInDB();
		readENameDomainDis();
		Set<String> keys=map.keySet();
		Map<String,Set<EventBean>> wordToEventBeansTemp=new HashMap<String,Set<EventBean>>();
		Set<String> wordsMatch=new HashSet<String>();
		for(String key:keys){
			HotWordInfo info=map.get(key);
			List<String> wordsTmp=new ArrayList<String>();
			if(this.eName2SplitInDB.containsKey(key)){
				wordsTmp.addAll(JSONArray.fromObject(this.eName2SplitInDB.get(key)));
			}else{
				String[] wordsArrTmp=info.getSplitContent().toLowerCase().replaceAll("_[a-z]{1,}", "").split("\\s+");
				for(String s:wordsArrTmp){
					
					wordsTmp.add(s);
				}
			}
			
			/*for(String s:wordsArrTmp){
				if(s.matches("[a-z]+")||s.matches(".{0,5}[0-9]+.{0,5}")){
					wordsTmp.add(s);
				}else{
					for(char c:s.toCharArray()){
						wordsTmp.add(String.valueOf(c));
					}
				}
			}*/
			//String[] wordsArr=new String[wordsTmp.size()];
			
			EventBean bean=new EventBean();
			
			bean.name=key;
			bean.splitName=info.getSplitContent();
			Set<String> removeStop=new HashSet<String>();
			
			for(String word:wordsTmp){
				
				if(stopWords.contains(word)||word.trim().length()==0){
					
					continue;
				}
				removeStop.add(word);
				wordsMatch.add(word);
				Set<EventBean> beans=wordToEventBeansTemp.get(word);
				if(beans==null){
					beans=new HashSet<EventBean>();
					wordToEventBeansTemp.put(word,beans );
				}
				beans.add(bean);
			}
			bean.mayExist=removeStop;
		}

		TrieTree tempt=new TrieTree(wordsMatch);
		this.wordsMatchTree=tempt;	
		this.wordToEventBeans=wordToEventBeansTemp;
	}
	public void readEvent(){
	}
	public boolean isValid(Map<String, List<Integer>> wordsToPositions,EventBean bean,String str,boolean isTitle,int beanWordExistCount){
		Map<String, List<Integer>> wordsToPositions3=new HashMap<String,List<Integer>>();
		List<String> numberL=new ArrayList<String>();
		
		Set<String> ks=wordsToPositions.keySet();
		for(String word:ks){
			
			if(bean.mayExist.contains(word)){
				Pattern p=Pattern.compile("[a-zA-Z0-9]+");
				Matcher m=p.matcher(word);
				while(m.find()){
					numberL.add(m.group());
				}
				wordsToPositions3.put(word, wordsToPositions.get(word));
			}
		}
		Pattern p=Pattern.compile("[a-zA-Z0-9]+");
		Matcher m=p.matcher(str);
		Set<String> numberInStr=new HashSet<String>();
		while(m.find()){
			numberInStr.add(m.group());
		}
		for(String number:numberL){
			if(!numberInStr.contains(number)){
				return false;
			}
		}
		return true;
	}
	/**/
	protected Set<BDHotWordBean> match(String str,boolean isTitle){
		Set<BDHotWordBean> rt=new HashSet<BDHotWordBean>();
		Date now=new Date();
		Map<String, List<Integer>> wordsToPositions=wordsMatchTree.detect(str);
		/*if(isTitle){
			LOG.info("title="+str+", matchWordInfo="+JSONObject.fromObject(wordsToPositions).toString());
		}*/
		//Map<String,EventBean> nameToBean=new HashMap<String,EventBean>();
		EventBean maxMatchRatioBean=null;
		EventBean maxMatchCountBean=null;
		int maxWordMatchCount=0;
		double maxWordMatchRatio=0;
		Map<EventBean,Integer> bean2WordCount=new HashMap<EventBean,Integer>();
		
		for(String word:wordsToPositions.keySet()){
			Set<EventBean> eventBeans=wordToEventBeans.get(word);

			for(EventBean bean:eventBeans){
				String deadLine=bean.deadLine;
				if(deadLine!=null&&deadLine.length()>0){
					DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
					try {
						Date d=format1.parse(deadLine);
						if(now.after(d)){
							continue;
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						LOG.error(e.getMessage(),e);
					}
				}
				if(bean2WordCount.containsKey(bean)){
					int wc=bean2WordCount.get(bean);
					wc=wc+1;
					bean2WordCount.put(bean, wc);
					
				}else{
					bean2WordCount.put(bean, 1);
					if(maxMatchRatioBean==null){
						maxMatchRatioBean=bean;
					}
					if(maxMatchCountBean==null){
						maxMatchCountBean=bean;
					}
				}
				
				double matchRatio=(double)bean2WordCount.get(bean)/bean.mayExist.size();
				if(matchRatio>1){
					matchRatio=1;
				}
				if( matchRatio>maxWordMatchRatio){
					maxWordMatchRatio=matchRatio;
					maxMatchRatioBean=bean;
				}
				if(matchRatio==maxWordMatchRatio){
					if(bean.name.length()<maxMatchRatioBean.name.length()){
						maxWordMatchRatio=matchRatio;
						maxMatchRatioBean=bean;
					}
				}
				if(bean2WordCount.get(bean)>maxWordMatchCount){
					maxWordMatchCount=bean2WordCount.get(bean);
					maxMatchCountBean=bean;
				}
				
			}
		}
		
		
		if(maxMatchRatioBean!=null){
			/*if(isTitle){
				LOG.info("maxMatchRatioBean="+maxMatchRatioBean.name);
			}*/
			
			boolean ok=isValid(wordsToPositions,maxMatchRatioBean,str,isTitle,bean2WordCount.get(maxMatchRatioBean));
			if(ok){
				double matchWordRatio=maxWordMatchRatio;
				BDHotWordBean rtb=new BDHotWordBean(maxMatchRatioBean.name,BDHotWordBean.TYPE_EVENT ,bean2WordCount.get(maxMatchRatioBean),matchWordRatio);
				rtb.setSplitStr(maxMatchRatioBean.splitName);
				rt.add(rtb);
			}
		}
		if(maxMatchCountBean!=null){
			/*if(isTitle){
				LOG.info("maxMatchCountBean="+maxMatchRatioBean.name);
			}*/
			boolean ok=isValid(wordsToPositions,maxMatchCountBean,str,isTitle,maxWordMatchCount);
			if(ok){
				double matchWordRatio=maxWordMatchCount/(double)maxMatchCountBean.mayExist.size();
				BDHotWordBean rtb=new BDHotWordBean(maxMatchCountBean.name,BDHotWordBean.TYPE_EVENT ,maxWordMatchCount,matchWordRatio);
				rtb.setSplitStr(maxMatchCountBean.splitName);
				rt.add(rtb);
			}
		}
		return rt;
	}
	/*public BDHotWordBean detectEvent(String title,String content,StringBuffer sb){
		try{
			Set<BDHotWordBean> rt=match(title,true);
			BDHotWordBean beanRt=null;
			for(BDHotWordBean b:rt){
				if(b.getWordExsitRatio()>this.matchWordRatioThreshold){
					if(beanRt==null){
						beanRt=b;
					}else{
						if(beanRt.getStr().length()>b.getStr().length()){
							beanRt=b;
						}
					}
					
				}
			}
			if(beanRt!=null){
				beanRt.setScore(1);
				return beanRt;
			}
			String[] sentances=content.split("[。！!,，;；:：\"”“]");
			
			Map<BDHotWordBean,Integer> beanToExistCount=new HashMap<BDHotWordBean,Integer>();
			for(String s:sentances){

				rt=match(s,false);
				for(BDHotWordBean b:rt){
					if(b.getWordExsitRatio()>this.matchWordRatioThreshold){
	 					Integer count=beanToExistCount.get(b);
						if(count==null){
							beanToExistCount.put(b, 1);
						}else{
							beanToExistCount.put(b, count+1);
						}
					}
				}
				
			}
			BDHotWordBean maxExistBean=null;
			int maxExistCount=0;
			for(BDHotWordBean bean:beanToExistCount.keySet()){
				if(beanToExistCount.get(bean)>maxExistCount){
					maxExistBean=bean;
					maxExistCount=beanToExistCount.get(bean);
				}
			}
			if(maxExistBean!=null){
				double distribution=maxExistCount/(double)sentances.length;
				sb.append(distribution+"\t");
				if(distribution>=disThreshold){
					maxExistBean.setScore(0.8);
				}else {
					double ratio=bothExistRatio(content,maxExistBean,sb);
					if(ratio>=0.09){
						maxExistBean.setScore(0.5);
					}else{
						maxExistBean.setScore(0.1);
					}
					
				}
				return maxExistBean;
			}
			
		}catch(Exception e){
			LOG.error(e.getMessage(),e);
		}
		return null;
	}*/
	public BDHotWordBean detectEvent(String title,String content){
		StringBuffer sb=new StringBuffer();
		return detectEvent( title,content,sb);
	}
	public boolean filteredByDomain(String docId,String eName,List<String> domains){
		if(domains==null){
			LOG.info(docId+" domains=null");
			return true;
		}
		JSONObject o=eName2Domain2Dis.get(eName);
		if(o!=null){
			LOG.info("eName domain distribution:"+o.toString());
			for(String domain:domains){
				if(o.containsKey(domain)){
					
					Double d=o.getDouble(domain);
					
					LOG.info(docId+", ename="+eName+" domain="+domain+",dis="+d);
					if(d>0.3){
						
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
	public BDHotWordBean detectEvent(String title,String content,StringBuffer sb){
		try{
			Set<BDHotWordBean> rt=match(title,true);
			BDHotWordBean beanRt=null;
			for(BDHotWordBean b:rt){
				if(b.getWordExsitRatio()>this.matchWordRatioThreshold){
					if(beanRt==null){
						beanRt=b;
					}else{
						if(beanRt.getStr().length()>b.getStr().length()){
							beanRt=b;
						}
					}
					
				}
			}
			if(beanRt!=null){
				beanRt.setScore(1);
				
				return beanRt;
			}
			String[] sentances=content.split("[。！!,，;；:：\"”“]");
			List<String> snew=new ArrayList<String>();
			int sensLenth=0;
			StringBuffer sbWords=new StringBuffer();
			Map<BDHotWordBean,Integer> beanToExistCount=new HashMap<BDHotWordBean,Integer>();
			for(String s:sentances){
				if(s.trim().replaceAll("[a-zA-Z0-9]", "").replaceAll("\\pP|\\pS", "").replaceAll("[\\s\\p{Zs}]+", "").length()==0){
					continue;
				}
				snew.add(s);
				sbWords.append(s.replaceAll("[a-zA-Z0-9]", "").replaceAll("\\pP|\\pS", "").replaceAll("[\\s\\p{Zs}]+", ""));
				sensLenth++;
				rt=match(s,false);
				for(BDHotWordBean b:rt){
					if(b.getWordExsitRatio()>this.matchWordRatioThreshold){
	 					Integer count=beanToExistCount.get(b);
						if(count==null){
							beanToExistCount.put(b, 1);
						}else{
							beanToExistCount.put(b, count+1);
						}
					}
				}
				
			}
			BDHotWordBean maxExistBean=null;
			int maxExistCount=0;
			for(BDHotWordBean bean:beanToExistCount.keySet()){
				if(beanToExistCount.get(bean)>maxExistCount){
					maxExistBean=bean;
					maxExistCount=beanToExistCount.get(bean);
				}
			}
			if(maxExistBean!=null){
				double distribution=maxExistCount/(double)sensLenth;
				double dis=(maxExistBean.getStr().replaceAll(" ", "").length()*maxExistCount)/(double)sbWords.length();
				if(dis>disWordsThreshold){
					maxExistBean.setScore(0.8);
				}else
				if(distribution>=disThreshold){
					maxExistBean.setScore(0.8);
				}else {
					String[] sarr=new String[snew.size()];
					snew.toArray(sarr);
					boolean isOk=couldFindEvent(sarr,maxExistBean,sb,sbWords.length());
					if(isOk){
						sb.append("\t"+0.5);
						maxExistBean.setScore(0.5);
					}else{
						sb.append("\t"+0.1);
						maxExistBean.setScore(0.1);
					}
					
				}
				return maxExistBean;
			}
			
		}catch(Exception e){
			LOG.error(e.getMessage(),e);
		}
		return null;
	}
	/*private double bothExistRatio(String content,BDHotWordBean bean,StringBuffer sb){
		String[] sentances=content.split("[。！!;；:：\"”“]");
		String[] eNameWords=bean.getSplitStr().replaceAll("_[a-z]{1,} {0,1}", " ").trim().toLowerCase().split("\\s+");
		List<String> selectedWords=new ArrayList<String>();
		for(String w:eNameWords){
			if(w.length()>1){
				selectedWords.add(w);
			}
		}
		int countOne=0;
		int countBoth=0;
		for(String s:sentances){
			int count=0;
			for(String w:selectedWords){
				if(s.contains(w)){
					count++;
				}
			}
			if(count>1){
				countBoth++;
			}else if(count==1){
				countOne++;
			}
		}
		double ratioOne=(double)countOne/sentances.length;
		double ratioBoth=(double)countBoth/sentances.length;
		
		double ratio=0.6*ratioBoth+0.4*ratioOne;
		sb.append(ratioOne+"\t"+ratioBoth+"\t"+ratio);
		return ratio;
	}*/
	private boolean couldFindEvent(String[] sentances,BDHotWordBean bean,StringBuffer sb,int wordsLen){
		
		String[] eNameWords=bean.getSplitStr().replaceAll("_[a-z]{1,} {0,1}", " ").trim().toLowerCase().split("\\s+");
		List<String> selectedWords=new ArrayList<String>();
		int matchWordLen=0;
		for(String w:eNameWords){
			if(w.length()>1){
				selectedWords.add(w);
			}
		}
		/*int countOne=0;
		int countBoth=0;*/
		int count=0;
		Map<String,Integer> w2c=new HashMap<String,Integer>();
		for(String s:sentances){
			//int count=0;
			for(String w:selectedWords){
				if(s.contains(w)){
					
					Integer c=w2c.get(w);
					if(c==null){
						w2c.put(w, 1);
					}else{
						w2c.put(w, c+1);
					}
					count++;
					matchWordLen+=w.length();
				}
			}
			/*if(count>1){
				countBoth++;
			}else if(count==1){
				countOne++;
			}*/
		}
		int wCount2=0;
		
		int wCount1=0;
		for(String w:w2c.keySet()){
			if(w2c.get(w)>2){
				wCount2++;
			}
			if(w2c.get(w)>1){
				wCount1++;
			}
			
		}
		sb.append("\t");
		sb.append(wCount2+","+ wCount1);
		double ratio=count/(double)sentances.length;
		sb.append("\t").append(count+"\t"+sentances.length+"\t"+ratio);
		if(ratio>0.2&&(double)matchWordLen/wordsLen>0.017&&wCount2>1){
			return true;
		}
		/*if(ratio>0.1&&wCount2>1){
			return true;
		}*/
		/*double ratioOne=(double)countOne/sentances.length;
		double ratioBoth=(double)countBoth/sentances.length;
		double ratio=0.6*ratioBoth+0.4*ratioOne;*/
		return false;
	}
/*	public static void main(String[] args){
		Map<String,List<Integer>> m=new HashMap<String,List<Integer>>();
		List<Integer> l=new ArrayList<Integer>();
		m.put("4万", l);
		m.put("周小草", l);
		EventBean bean=new EventBean();
		bean.mayExist=new HashSet<String>();
		bean.mayExist.add("4万");
		bean.mayExist.add("周小草");
		isValid(m,bean,"周小草有334万",false,2);
	}*/
}
