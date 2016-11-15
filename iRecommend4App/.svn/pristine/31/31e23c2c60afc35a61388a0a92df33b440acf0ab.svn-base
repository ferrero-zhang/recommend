package com.ifeng.iRecommend.wuyg.commonData.Update.publish;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.TaskLog.LogName;

import redis.clients.jedis.Pipeline;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.EntityLibQuery;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.WordReadData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.WordReadData.WordInfo;
import com.ifeng.iRecommend.wuyg.commonData.Update.PublisherOperate;
import com.ifeng.iRecommend.wuyg.commonData.Update.UpdateActionType;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;
import com.ifeng.iRecommend.wuyg.commonData.entity.entityBase.KnowledgeBaseBuild;

public class AllWordPublisher extends PublisherOperate {
	
	static Log LOG = LogFactory.getLog(AllWordPublisher.class);
	
	public AllWordPublisher(String channel, String key){
		super(channel, key);
	}
	
	/**
	 * 
	 * @Title:addWord2UserDic
	 * @Description:添加词到外挂词典中
	 * @param words
	 * @author:wuyg1
	 * @date:2015年12月15日
	 */
	public String addWord2AllWordLib(List<String> wordList) {
        HashSet<String> allWordSet = new HashSet<String>();
        
        publisherJedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("allWordDataDbNum")));
        
		allWordSet.addAll(getDataFromAllWordLib());
		
		long oldsize = allWordSet.size();
		
		Pipeline pipeline = publisherJedis.pipelined();
		
		StringBuffer sBuffer = new StringBuffer();
		String message = new String();
		long addcount = 0;
		for (String word : wordList) {
			if(null == word || word.isEmpty()){
				continue;
			}
			if (!allWordSet.contains(word)) {
				allWordSet.add(word);
				pipeline.append(word, "");
				sBuffer.append(word+commonDataUpdateConfig.recordDelimiter);
				addcount ++ ;
			}
		}	
		pipeline.sync();
		
		
		if (null == sBuffer || sBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "add#DIV#elem#DIV#" + sBuffer.toString();
		}

		long newsize = getDataFromAllWordLib().size();

		if (((oldsize + addcount) != newsize)) {
			logger.error("updateInfo_Add exception:\tAllWordLib" + "\toldsize:"
					+ oldsize + " \taddsize:" + addcount + "\tnewsize:" + newsize);
			message = null;
		}

		return message;

	}
	/**
	 * 
	* @Title:delWordFromAllWordLib
	* @Description: 删除全部词汇中的某些不合要求的词汇
	* @param wordList
	* @author:wuyg1
	* @date:2016年3月1日
	 */
	public String delWordFromAllWordLib(List<String> wordList){
        
	    HashSet<String> allWordSet = new HashSet<String>();
		
        publisherJedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("allWordDataDbNum")));
        
        Pipeline pipeline = publisherJedis.pipelined();
        
        
        allWordSet.addAll(getDataFromAllWordLib());
		
		long oldsize = allWordSet.size();
        
        
		StringBuffer sBuffer = new StringBuffer();
		String message = new String();
		long delcount = 0;
		
        for(String key : wordList){
        	
        	if(allWordSet.contains(key)){
        		allWordSet.remove(key);
        		pipeline.del(key);
        		sBuffer.append(key+commonDataUpdateConfig.recordDelimiter);
        		delcount ++;
        	}
        	
        }
        
        pipeline.sync();
        
        if (null == sBuffer || sBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "del#DIV#elem#DIV#" + sBuffer.toString();
		}

		long newsize = getDataFromAllWordLib().size();

		if (((oldsize - delcount) != newsize)) {
			logger.error("updateInfo_del exception:\tAllWordLib" + "\toldsize:"
					+ oldsize + " \tdelsize:" + delcount + "\tnewsize:" + newsize);
			message = null;
		}

		return message;
        
	}
	
	public HashSet<String> getDataFromAllWordLib(){
		 publisherJedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("allWordDataDbNum")));
		HashSet<String> keySet = new HashSet<String>();
		keySet.addAll(publisherJedis.keys("*"));
		
		return keySet;
	}
	
	
	public static HashSet<String> getEntity() throws IOException{
		
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File("e:/明星(带别名).txt"), false), "utf-8");
		
		HashSet<String> set = new HashSet<String>();
		
		KnowledgeBaseBuild.initEntityTree();
		
		for(EntityInfo entityInfo : KnowledgeBaseBuild.wordInfos){
			
			if(entityInfo.getCategory().equals("娱乐") && entityInfo.getLevels().get(1).equals("明星")){
				set.add(entityInfo.getWord());
				
				set.addAll(entityInfo.getNicknameList());
				osw.append(entityInfo.getWord()+"\n");
				
				for(String str : entityInfo.getNicknameList()){
					if(null == str || str.equals("") || str.length() == 0){
						continue;
					}
					osw.append(str+"\n");
				}
			}
		
		}
		
		osw.flush();
		osw.close();
		return set;
		
	}
	
	public static HashSet<String> getUserdic(){
		String userDicPattern = LoadConfig.lookUpValueByKey("userDicPattern");
		UserDicPublisher userDicPublisher = new UserDicPublisher(userDicPattern, userDicPattern);
        HashMap<String, String> map = userDicPublisher.getDataFromRedis();
		HashSet<String> set = new HashSet<String>();
		Iterator<String> iterator = map.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();
			
			if(key.contains(",") || key.contains("|!|") || key.equals("注意事项")){
				continue;
			}
          set.add(key);
		}
		return set;
	}

	public static HashSet<String> getWordReadable(){
		WordReadData wordReadData = WordReadData.getInstance();
		ConcurrentHashMap<String, WordInfo> woHashMap = wordReadData.getWordReadMap();
		HashSet<String> set = new HashSet<String>();
		set.addAll(woHashMap.keySet());
		return set;
	}
	
	public static HashSet<String> getHotWord(){
		HotWordData hotWordData = HotWordData.getInstance();
		ConcurrentHashMap<String, HotWordInfo> hoHashMap = hotWordData.getHotwordMap();
		HashSet<String> set = new HashSet<String>();
		set.addAll(hoHashMap.keySet());
		return set;
	}
	
	
	public void publish(String message, String updatetype,
			List<String> dataList, String state) {
		if (null == message || message.isEmpty()) {
			return;
		}

			LOG.info("publish_message:" + message);
			if (super.pubMessage(message)) {
				LOG.info("Publish Success.");
			} else {
				LOG.error("Publish Failed.");
				switch (UpdateActionType.getActionType(updatetype)) {
				case ADD_WORD:
					delWordFromAllWordLib(dataList);
					break;
				case DEL_WORD:
					addWord2AllWordLib(dataList);
					break;
				case ALTER_WORD:
					LOG.warn("this function is not existed!!");
					break;
				default:
					break;
				}
		}
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
	getEntity();
	   
		
		
		
//		AllWordPublisher allWordPublisher = new AllWordPublisher(LoadConfig.lookUpValueByKey("allWordMessageChannel"), null);
//		
//		String message = null;
//		
//		List<String> userDicList = new ArrayList<String>();
//		
////		HashSet<String> set = new HashSet<String>();
////		set.addAll(getEntity());
////		set.addAll(getUserdic());
////		set.addAll(getWordReadable());
////		set.addAll(getHotWord());
//		
//	  //  userDicList.addAll(set);
//		
//		userDicList.add("TESTTESTTEST");
////		
//		message = allWordPublisher.addWord2AllWordLib(userDicList);
////		
//		allWordPublisher.publish(message, UpdateActionType.ADD_WORD.name(), userDicList, null);
//		
//		userDicList.add("007");
//		userDicList.add("0731团购网");
//		message = allWordPublisher.delWordFromAllWordLib(userDicList);
//		
//		allWordPublisher.publish(message, UpdateActionType.DEL_WORD.name(), userDicList, null);
		
	}

}
