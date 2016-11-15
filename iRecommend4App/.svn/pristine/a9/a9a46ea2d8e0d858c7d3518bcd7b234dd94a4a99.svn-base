package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.dingjw.dataCollection.newItemToHbase;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.CommonSubDataWord.WordState;

public class WordReadData extends LocalDataUpdate implements Serializable{

	private static Log logger = LogFactory.getLog(WordReadData.class);
	private ConcurrentHashMap<String, WordInfo> wordReadMap = new ConcurrentHashMap<String, WordInfo>();
	private static WordReadData INSTANCE = null;
	
	public class WordInfo implements Serializable{
		boolean isRead;//热词是否可读
		long timestamp;
		int clientSubWordCount;
		int otherSouceCount;
		
		public WordInfo(boolean isRead,long timestamp){
			setRead(isRead);
			setTimestamp(timestamp);
		}
		
		public WordInfo(boolean isRead,long timestamp, int clientSubWordCount, int otherSouceCount){
			setRead(isRead);
			setTimestamp(timestamp);
			setClientSubWordCount(clientSubWordCount);
			setOtherSouceCount(otherSouceCount);
		}
		
		
		public int getClientSubWordCount() {
			return clientSubWordCount;
		}

		public void setClientSubWordCount(int clientSubWordCount) {
			this.clientSubWordCount = clientSubWordCount;
		}

		public int getOtherSouceCount() {
			return otherSouceCount;
		}

		public void setOtherSouceCount(int otherSouceCount) {
			this.otherSouceCount = otherSouceCount;
		}

		public boolean isRead() {
			return isRead;
		}
		public void setRead(boolean isRead) {
			this.isRead = isRead;
		}
		public long getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}//热词的最近刷新时间

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "isread:"+isRead+"\ttimestamp:"+timestamp+"\tclientSubWord:"+clientSubWordCount+"\totherSouceCount"+otherSouceCount;
		}
		
		
	}
	
	public boolean getWordInfoState(String word){
		if(!this.wordReadMap.contains(word)){
			return false;
		}
		return this.wordReadMap.get(word).isRead();
	}
	
	
	@Override
	protected void init() {

		try {
			setRedis_key(commonDataUpdateConfig.wordPattern);
			commonDataRedisPort = Integer.valueOf(LoadConfig
					.lookUpValueByKey("commonDataRedisPort"));
			commonDataRedisHost = LoadConfig
					.lookUpValueByKey("commonDataRedisHost");
			// 默认超时时间正常情况够了，不用设置
			setJedis(new Jedis(commonDataRedisHost, commonDataRedisPort));

		} catch (Exception e) {
			logger.error("Redis Init Error:", e);
			setJedis(null);
		}
		OutputStreamWriter osw = null;
		try {
			osw = new OutputStreamWriter(new FileOutputStream(new File("e:/data/wordReadClean.txt"), false), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (null != jedis) {
			try {
				jedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("commonDataDbNum")));
				List<String> redisData = jedis.lrange(this.redis_key,
						0, -1);
				logger.info("init redisData, get data size:"
						+ redisData.size());
				
				for (String tempElem : redisData) {
					if(null == tempElem || tempElem.isEmpty()){
						continue;
					}
					
					String[] tempElemSplit = tempElem.split(LoadConfig.lookUpValueByKey("fieldDelimiter"));
					
//					if(tempElemSplit.length >4){
//						//jedis.lrem(this.redis_key, 0, tempElem);
//						continue;
//					}
					
					if (tempElemSplit[0].equals(this.redis_key)) {
						boolean isread = true;
						if(tempElemSplit[1].equals(WordState.unread.name())){
							isread = false;
						}
						
						WordInfo wordInfo = null;
						if(tempElemSplit.length == 4){
							wordInfo =	new WordInfo(isread, commenFuncs.datestr2Long(tempElemSplit[3]));
						}else if(tempElemSplit.length == 6){
							wordInfo =	new WordInfo(isread, commenFuncs.datestr2Long(tempElemSplit[3]),Integer.valueOf(tempElemSplit[4]),Integer.valueOf(tempElemSplit[5]));
						}

						if(null == wordInfo){
							continue;
						}
						if(wordReadMap.containsKey(tempElemSplit[2])){
							osw.append(tempElemSplit[2]+"\t"+wordReadMap.get(tempElemSplit[2])+"\t"+tempElem+"\n");
						}else{
							wordReadMap.put(tempElemSplit[2],wordInfo);
						}
					}
				}
			} catch (Exception ex) {
				logger.error("WordReadable Init Error:" + ex.getMessage());
			}
		}
	}

	@Override
	protected void addElem2Local(String add_content) {
		if (null == add_content || add_content.isEmpty()) {
			logger.info("addElems2WordReadMap:Empty Input.");
			return;
		}

		String[] inputElemsList = add_content.split(commonDataUpdateConfig.recordDelimiter);

		for (String tempElem : inputElemsList) {
			if(null == tempElem || tempElem.isEmpty()){
				continue;
			}
			String[] tempElemSplit = tempElem.split(LoadConfig.lookUpValueByKey("fieldDelimiter"));
			if (tempElemSplit[0].equals(this.redis_key)) {
				boolean isread = true;
				if(tempElemSplit[1].equals(WordState.unread.name())){
					isread = false;
				}
				WordInfo wordInfo = null;
				if(!wordReadMap.containsKey(tempElemSplit[2])){
					
					if(tempElemSplit.length == 4){
						wordInfo =	new WordInfo(isread, commenFuncs.datestr2Long(tempElemSplit[3]));
					}else if(tempElemSplit.length == 6){
						wordInfo =	new WordInfo(isread, commenFuncs.datestr2Long(tempElemSplit[3]),Integer.valueOf(tempElemSplit[4]),Integer.valueOf(tempElemSplit[5]));
					}
				}else if(wordReadMap.containsKey(tempElemSplit[2])){
					wordInfo = wordReadMap.get(tempElemSplit[2]);
					
					if(wordInfo.isRead() == true && isread == false){
						wordInfo.setRead(isread);
					}
					
					if(tempElemSplit.length == 6){
						wordInfo = new WordInfo(isread, commenFuncs.datestr2Long(tempElemSplit[3]),Integer.valueOf(tempElemSplit[4]),Integer.valueOf(tempElemSplit[5]));
					}
				
				}
				
				

				if(null == wordInfo){
					continue;
				}
				
				
				wordReadMap.put(tempElemSplit[2],wordInfo);
			}
		}
		
	}

	@Override
	protected void delElemFromLocal(String del_content) {
		if (null == del_content || del_content.isEmpty()) {
			logger.info("delElemsFromHashMap:Empty Input.");
			return;
		}

		String[] inputElemsList = del_content.split(LoadConfig.lookUpValueByKey("recordDelimiter"));
		for (String tempElem : inputElemsList) {
			String[] tempElemSplit = tempElem.split(LoadConfig.lookUpValueByKey("fieldDelimiter"));
			if (tempElemSplit[0].equals(this.redis_key)) {
				wordReadMap.remove(tempElemSplit[2]);
			}
		}
		
	}

	@Override
	protected void alterElemInLocal(String alter_content) {
		if (null == alter_content || alter_content.isEmpty()) {
			logger.info("alterElems2WordReadMap:Empty Input.");
			return;
		}

		String[] inputElemsList = alter_content.split(commonDataUpdateConfig.recordDelimiter);

		for (String tempElem : inputElemsList) {
			if(null == tempElem || tempElem.isEmpty()){
				continue;
			}
			String[] tempElemSplit = tempElem.split(LoadConfig.lookUpValueByKey("fieldDelimiter"));
			if (tempElemSplit[0].equals(this.redis_key)) {
				boolean isread = true;
				if(tempElemSplit[1].equals(WordState.unread.name())){
					isread = false;
				}
				if(wordReadMap.containsKey(tempElemSplit[2])){
					WordInfo hotWordInfo = wordReadMap.get(tempElemSplit[2]);
					hotWordInfo.setRead(isread);
				}else{
					WordInfo hotWordInfo = new WordInfo(isread, commenFuncs.datestr2Long(tempElemSplit[3]));
					wordReadMap.put(tempElemSplit[2], hotWordInfo);
				}
			}
		}
		
	}
	
	private WordReadData(){
		init();
	}
	
	public static WordReadData getInstance(){
		if(null == WordReadData.INSTANCE){
			INSTANCE = new WordReadData();
		}
		return WordReadData.INSTANCE;
	}
/**
 * 
* @Title:searchWord
* @Description: 查询query是否可读，建议使用该接口，方法内已经对query的大小写进行了同一处理
* @param query
* @return
* @author:wuyg1
* @date:2016年10月8日
 */
	public WordInfo searchWord(String query){
		if (null == query) {
			return null;
		}
		query = query.trim();
		if (query.isEmpty()) {
			return null;
		}

		query = query.toLowerCase();
		return this.getWordReadMap().get(query);
	}
	
	protected ConcurrentHashMap<String, WordInfo> getWordReadMap() {
		return wordReadMap;
	}

	protected void setWordReadMap(ConcurrentHashMap<String, WordInfo> wordReadMap) {
		this.wordReadMap = wordReadMap;
	}

	
	
}
