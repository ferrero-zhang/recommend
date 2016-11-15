package com.ifeng.iRecommend.wuyg.commonData.Update.publish;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Pipeline;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.wuyg.commonData.Update.PublisherOperate;
import com.ifeng.iRecommend.wuyg.commonData.Update.UpdateActionType;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
public class UserDicPublisher extends PublisherOperate{
	static Log LOG = LogFactory.getLog(UserDicPublisher.class);
    protected String key;
    static String userDicPattern = LoadConfig.lookUpValueByKey("userDicPattern");
    static String UserDicDataDbNum = LoadConfig.lookUpValueByKey("UserDicDataDbNum");
	public UserDicPublisher(String channel,String key) {
		super(channel);
		this.key = key;
	}
	
	/**
	 * 
	 * @Title:addWord2UserDic
	 * @Description:判定外挂词中是否有，如果没有就进行消息传递让缓存进行添加
	 * @param words
	 * @param isEntity 表示wordList里面的词汇是术语的，这样的话，不再对其进行分词判定，直接加入userdic
	 * @author:wuyg1
	 * @date:2015年12月15日
	 */
	public String addWord2UserDic(ArrayList<String> wordList,boolean isEntity) {
        HashSet<String> userDicSet = new HashSet<String>();
		userDicSet.addAll(getUserDicfromredis(key));
		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));

		Pipeline pipeline = publisherJedis.pipelined();
		StringBuffer sBuffer = new StringBuffer();
		for (String word : wordList) {
			if(null == word || word.isEmpty()){
				continue;
			}
			if (!userDicSet.contains(word)) {
				userDicSet.add(word);
		       // pipeline.lpush(key, word);
				sBuffer.append(word+commonDataUpdateConfig.recordDelimiter);
			}
		}	
		
		String message = null;
		if (null == sBuffer || sBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "add#DIV#elem#DIV#" + sBuffer.toString();
		}
		
       return message;
	}
	
	/**
	 * 
	 * @Title:addWord2UserDic
	 * @Description:判断分词系统是否可以分词，如果不可以，表明需要添加（只是暂时不添加），只需发送消息；由毅哥进行缓存，最终在生成分词外挂时进行添加统一合并
	 * @param words
	 * @author:wuyg1
	 * @date:2015年12月15日
	 */
	public String addWord2UserDic(ArrayList<String> wordList) {
        HashSet<String> userDicSet = new HashSet<String>();
		userDicSet.addAll(getUserDicfromredis(key));
		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));

		Pipeline pipeline = publisherJedis.pipelined();
		StringBuffer sBuffer = new StringBuffer();
		for (String word : wordList) {
			if(null == word || word.isEmpty()){
				continue;
			}
			String splitword = SplitWordClient.split(word, null);
			int fisrt = splitword.indexOf("(/");
		    int last = splitword.lastIndexOf("(/");
		    if(((splitword.split(" ").length == 1 || fisrt == last))){
		    	//说明原有分词可以将该词进行正确分词，为了不影响分词的正确性，则不再将其作为外挂词进行添加
		    	continue;
		    }
		    
			int length = commenFuncs.computeWordsLen(word);
			String tword = splitWord(word);
			
			String [] ws = tword.split(" ");
			
			if(!(((ws.length >=3 || length >3) && length<5) ||
					(ws.length < 3 && length <=3)))
			{
			     continue;
			}
		    
			if (!userDicSet.contains(word)) {
				userDicSet.add(word);
				//pipeline.lpush(key, word);
				sBuffer.append(word+commonDataUpdateConfig.recordDelimiter);
			}
		}
		String message = null;
		if (null == sBuffer || sBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "add#DIV#elem#DIV#" + sBuffer.toString();
		}

		return message;
		
	}

	/**
	 * 
	* @Title:splitWord
	* @Description:对内容进行分词
	* @param textSplited
	* @return
	* @author:wuyg1
	* @date:2015年12月17日
	 */
	private String splitWord(String textSplited){
		int icount = 1;
		while(icount <=3){
			try {
				textSplited = new String(SplitWordClient.split(textSplited, null).replace("(/", "_").replace(") ", " "));
			    break;
			} catch (Exception e) {
				if(icount == 3){
					break;
				}
				icount ++;
			}
		}
		return textSplited;
	}
	
	
	/**
	 * 
	 * @Title:addFile2UserDic
	 * @Description:添加新增的文件术语到外挂词库
	 * @param files
	 * @author:wuyg1
	 * @date:2015年12月15日
	 */
	public void addFile2UserDic(String files) {

	}
	/**
	 * 
	* @Title:delWordsFromUserDic
	* @Description:删除指定的词集合从UserDic的redis中
	* @param words
	* @author:wuyg1
	* @date:2015年12月23日
	 */
	public String delWordsFromUserDic(ArrayList<String> words){
		//HashMap<String, String> map = getDataFromRedis();
		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("UserDicDataDbNum")));
		Pipeline pipeline = publisherJedis.pipelined();
		
		StringBuffer sBuffer = new StringBuffer();
		
		for(String word : words){
			//pipeline.lrem(this.key, 0, word);
			pipeline.srem(this.key, word);
			sBuffer.append(word+commonDataUpdateConfig.recordDelimiter);
		}
		pipeline.sync();
		
		String message = null;
		if (null == sBuffer || sBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "del#DIV#elem#DIV#" + sBuffer.toString();
		}
		
		return message;
	}
	
	
	
	@Override
	public HashMap<String, String> getDataFromRedis() {
		// TODO Auto-generated method stub
		HashMap<String, String> dataMap = new HashMap<String, String>();
		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("UserDicDataDbNum")));
		//List<String> dataList = publisherJedis.lrange(key, 0, -1);
		Set<String> dataSet = publisherJedis.smembers(key);
		for (String elem : dataSet) {
			dataMap.put(elem, elem);
			
			//publisherJedis.sadd(key+"Set", elem);
			
		}
		
		publisherJedis.sync();
		
		return dataMap;
	}

	/**
	 * 
	 * @Title:alterWord2UserDic
	 * @Description:该方法暂时用于通知对方该机器已经更新完成
	 * @author:wuyg1
	 * @date:2015年12月15日
	 */
	public String  alterWord2UserDic() {
		InetAddress addr=null;  
        String ip="";  
 
        try {
			addr=InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        ip=addr.getHostAddress().toString();//获得本机IP　　  
        
        StringBuffer sBuffer = new StringBuffer();
        
        sBuffer.append("alter#DIV#elem#DIV#"+ip);
        
        return sBuffer.toString();
        
	}
	
	public Set<String> getUserDicfromredis(String key) {
		publisherJedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("UserDicDataDbNum")));
		//List<String> dataList = publisherJedis.lrange(key, 0, -1);
		Set<String> dataSet = publisherJedis.smembers(key);
	    return dataSet;
	}
	
	public void getWordFromUserDicRedis(ArrayList<String> words){
		 HashSet<String> userDicSet = new HashSet<String>();
		 userDicSet.addAll(getUserDicfromredis(key));
		 for(String word : words){
			 logger.info("search word:"+word);
			 if(userDicSet.contains(word)){
				 logger.info("the UserDic containce:"+word);
			 }else{
				 logger.info("the UserDic not containce:"+word);
			 }
		 }
	}
	
	
	public void publish(String message, String updatetype,
			List<String> dataList,String state) {
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
					updateWord_Del(dataList, state);
					break;
				case DEL_WORD:
					updateWord_Add(dataList, state);
					break;
				case ALTER_WORD:
					updateWord_Alter_undo(dataList,state);
					break;
				default:
					break;
				}

			}
	}
	
	
	public static void main(String [] args) throws IOException{
        UserDicPublisher userDicPublisher = new UserDicPublisher(LoadConfig.lookUpValueByKey("userDicPatternChannel"), 
     		   LoadConfig.lookUpValueByKey("userDicPattern"));
		
        ArrayList<String> userDicList = new ArrayList<String>();
//        
//        userDicList.add("水果浴");
//        String message = userDicPublisher.addWord2UserDic(userDicList);
//        userDicPublisher.publish(message, UpdateActionType.ADD_WORD.name(),
//				null, null);
        
//      userDicPublisher.getDataFromRedis();
      
      userDicList.add("ce=wemediaYidian");
        
      String message = userDicPublisher.delWordsFromUserDic(userDicList);
       
       userDicPublisher.publish(message, UpdateActionType.DEL_WORD.name(), null, null);
	}
	
}
