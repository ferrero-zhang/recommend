package com.ifeng.commen.blackList.publisher;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import com.ifeng.commen.blackList.util.CommonPSParams;
import com.ifeng.commen.Utils.FileUtil;



/**
 * 
 * <PRE>
 * 作用 :
 *    黑名单的管理类,更新redis数据同时发布更新内容
 *    数据发布到redis，同时进行相关的消息发布 ，通知订阅者进行操作
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年12月9日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class BlacklistUpdaterAndPublisher extends Publisher {
	private static Log LOG = LogFactory.getLog(BlacklistUpdaterAndPublisher.class);

	public BlacklistUpdaterAndPublisher(String channel) {
		super(channel);
		super.channelName = CommonPSParams.blackListMessageChannel;
	}

	

	/**
	 * 消息发布方法,发布失败时,回滚redis数据
	 * 
	 * @param messageList
	 *            发布的消息
	 * @param updatetype
	 *            更新操作的类别 词的增删改，文件的增删改，该参数用来对发布不成功的数据进行redis回滚
	 * @param filepath
	 *            数据的来源，用于redis回滚时的数据获取
	 */
	public void publish(ArrayList<String> messageList, String updatetype,
			String filepath) {
		if (null == messageList || messageList.isEmpty()
				|| messageList.size() == 0) {
			return;
		}

		for (String pubMessage : messageList) {
			LOG.info("publish_message:" + pubMessage);
			
			if (super.pubMessage(pubMessage)) {
				LOG.info("Publish Success.");
			} else {
				//发布失败,执行redis回滚(删除本轮添加的数据或重插本轮删除的数据)
				LOG.error("Publish Failed.");
				switch (UpdateActionType.valueOf(updatetype)) {
				case ADD_WORD:
					updateWord_del(filepath);
					break;
				case DEL_WORD:
					updateWord_add(filepath);
					break;
				default:
					break;
				}

			}
		}
	}

	/**
	 * redis初始化
	 * 
	 * @param filedir
	 * @return
	 */
	/*public boolean redisInit(String filedir) {

		super.publisherJedis.select(CommonPSParams.commonDataDbNum);

		BlackListRedisUpdate blackListRedisUpdate = new BlackListRedisUpdate(
				super.publisherJedis);
		boolean state = blackListRedisUpdate.InitRedis(filedir);
		return state;
	}*/

	/**
	 * 往redis中添加文件中词
	 * 
	 * 从文件中读取要添加的数据
	 * 每行代表一条数据
	 * 
	 * @param path
	 * @return
	 */
	public ArrayList<String> updateWord_add(String path) {
		FileUtil fileUtil = new FileUtil();
		String content = fileUtil.Read(path, "utf-8");
		String[] wordInfos = content.split("\n");
		StringBuffer messageBuffer = new StringBuffer();
		for (String wordInfo : wordInfos) {
			messageBuffer.append(wordInfo + CommonPSParams.recordDelimiter);
		}
		String addContent=messageBuffer.toString();
		
		//添加到redis
		BlackListRedisUpdate blackListRedisUpdate = new BlackListRedisUpdate(publisherJedis);
		boolean state = blackListRedisUpdate.updateInfo_Add(addContent);

		if (false == state) {
			return null;
		}
		
		//构造要发布的内容
		ArrayList<String> wordAddList = new ArrayList<String>();
		wordAddList.add("add#DIV#elem#DIV#" + addContent);
		return wordAddList;
	}


	/**
	 * 从redis中删除文件中包含的词
	 * 
	 * 从文件中读取要删除的数据
	 * 每行代表一条数据
	 * 
	 * @param path 
	 * @return
	 */
	public ArrayList<String> updateWord_del(String path) {
		FileUtil fileUtil = new FileUtil();
		String content = fileUtil.Read(path, "utf-8");
		String[] wordInfos = content.split("\n");
		StringBuffer messageBuffer = new StringBuffer();
		for (String wordInfo : wordInfos) {
			messageBuffer.append(wordInfo + CommonPSParams.recordDelimiter);
		}
		String delContent=messageBuffer.toString();
		
		//从redis删除
		BlackListRedisUpdate blackListRedisUpdate = new BlackListRedisUpdate(publisherJedis);		
		boolean state = blackListRedisUpdate.updateInfo_Del(delContent);
		if (false == state) {
			return null;
		}
		
		//构造要发布的内容
		ArrayList<String> wordAlterList = new ArrayList<String>();
		wordAlterList.add("del#DIV#elem#DIV#" + messageBuffer);
		return wordAlterList;
	}
	
	public void updateWord_delAll() {
		BlackListRedisUpdate blackListRedisUpdate = new BlackListRedisUpdate(publisherJedis);	
		blackListRedisUpdate.updateInfo_DelAll();
		
	}
	
	
    //测试
	public static void main(String[] args) {
		//PropertyConfigurator.configure("./conf/log4j.properties");

		BlacklistUpdaterAndPublisher blacklistPublisher = new BlacklistUpdaterAndPublisher(
				CommonPSParams.blacklistKeyInRedis);

		/*boolean state = blacklistPublisher.redisInit(LoadConfig
				.lookUpValueByKey("Filedir"));
		if (false == state) {
			LOG.error("initRedis failed!");
		}*/

		/*ArrayList<String> messageList = new ArrayList<String>();

		String path = CommonPSParams.addFilePath;
		messageList = blacklistPublisher.updateWord_add(path);
		blacklistPublisher.publish(messageList, UpdateActionType.ADD_WORD.name(),
				path);
		messageList.clear();

		path = CommonPSParams.delFilePath;
		messageList = blacklistPublisher.updateWord_del(path);
		blacklistPublisher.publish(messageList, UpdateActionType.DEL_WORD.name(),
				path);
		messageList.clear();*/
		
		blacklistPublisher.updateWord_delAll();
	}

}
