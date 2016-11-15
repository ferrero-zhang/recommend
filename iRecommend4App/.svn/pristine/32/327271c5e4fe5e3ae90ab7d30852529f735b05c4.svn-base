package com.ifeng.iRecommend.wuyg.commonData.Update.publish;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.CommonSubDataWord.WordState;
import com.ifeng.iRecommend.wuyg.commonData.Update.PublisherOperate;
import com.ifeng.iRecommend.wuyg.commonData.Update.UpdateActionType;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;

/**
 * 
 * <PRE>
 * 作用 : 
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
public class BlacklistPublisher extends PublisherOperate {
	private static Log LOG = LogFactory.getLog(BlacklistPublisher.class);

	public BlacklistPublisher(String channel) {
		super(channel);
	}

	public BlacklistPublisher(String key, String channel) {
		super(channel,key);
	}

	/**
	 * 
	 * @param messageList
	 *            发布的消息
	 * @param updatetype
	 *            更新操作的类别 词的增删改，文件的增删改，该参数用来对发布不成功的数据进行redis回滚
	 * @param filepath
	 *            数据的来源，用于redis回滚时的数据获取
	 */
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
				updateWord_Del(dataList, state);
				break;
			case DEL_WORD:
				updateWord_Add(dataList, state);
				break;
			case ALTER_WORD:
				updateWord_Alter_undo(dataList, state);
				break;
			default:
				break;
			}
		}
	}

	// 测试
	public static void main(String[] args) {
		PropertyConfigurator.configure("./conf/log4j.properties");

		BlacklistPublisher blacklistPublisher = new BlacklistPublisher(
				commonDataUpdateConfig.blacklistPattern,
				commonDataUpdateConfig.blackListMessageChannel);

		List<String> wordInfos = new ArrayList<String>();

		String path = new String();
		String message = new String();
		
		wordInfos = blacklistPublisher.getSourceData(LoadConfig
				.lookUpValueByKey("Filedir")+"word.txt");

		message = blacklistPublisher.updateWord_Add(wordInfos, null);
		blacklistPublisher.publish(message,
				UpdateActionType.ADD_WORD.name(), wordInfos,WordState.read.name());

		path = LoadConfig
				.lookUpValueByKey("Filedir") + "addword.txt";
		wordInfos = blacklistPublisher.getSourceData(path);
		message = blacklistPublisher.updateWord_Add(wordInfos, null);
		blacklistPublisher.publish(message,
				UpdateActionType.ADD_WORD.name(), wordInfos,WordState.read.name());

		path = LoadConfig
				.lookUpValueByKey("Filedir") + "delword.txt";
		wordInfos = blacklistPublisher.getSourceData(path);
		message = blacklistPublisher.updateWord_Del(wordInfos, null);
		blacklistPublisher.publish(message,
				UpdateActionType.DEL_WORD.name(), wordInfos,WordState.read.name());
	}

}
