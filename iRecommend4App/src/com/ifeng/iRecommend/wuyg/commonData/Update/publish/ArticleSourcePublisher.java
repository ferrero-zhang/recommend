package com.ifeng.iRecommend.wuyg.commonData.Update.publish;

import java.util.ArrayList;
import java.util.List;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.PublisherOperate;
import com.ifeng.iRecommend.wuyg.commonData.Update.UpdateActionType;
import com.ifeng.iRecommend.wuyg.commonData.Update.CommonSubDataWord.ArticleSouceState;

public class ArticleSourcePublisher extends PublisherOperate {

	public ArticleSourcePublisher(String channel, String key) {
		super(channel, key);
	}

	public ArticleSourcePublisher(String channel) {
		super(channel);
	}

	public static void main(String[] args) {

		FileUtil fileUtil = new FileUtil();
		
		ArticleSourcePublisher articleSourcePublisher = new ArticleSourcePublisher(
				LoadConfig.lookUpValueByKey("articleSourceMessageChannel"),
				LoadConfig.lookUpValueByKey("articleSourcePattern"));
		
		List<String> wordInfos = new ArrayList<String>();
		
		String message = new String();
		ArrayList<String> files = fileUtil.refreshFileList(LoadConfig.lookUpValueByKey("Filedir"));
		for(String filepath : files){
			if(filepath.contains("seriouSource")){
				wordInfos = articleSourcePublisher.getSourceData(filepath);
				message = articleSourcePublisher.updateWord_Add(wordInfos, ArticleSouceState.serious.name());
				articleSourcePublisher.publish(message, UpdateActionType.ADD_WORD.name(), wordInfos, ArticleSouceState.serious.name());
			}else if(filepath.contains("softSource")){
				wordInfos = articleSourcePublisher.getSourceData(filepath);
				message = articleSourcePublisher.updateWord_Add(wordInfos, ArticleSouceState.soft.name());
				articleSourcePublisher.publish(message, UpdateActionType.ADD_WORD.name(), wordInfos, ArticleSouceState.soft.name());
			}else 
				if(filepath.contains("rubbishSource")){
				wordInfos = articleSourcePublisher.getSourceData(filepath);
				message = articleSourcePublisher.updateWord_Add(wordInfos, ArticleSouceState.rubbish.name());
				articleSourcePublisher.publish(message, UpdateActionType.ADD_WORD.name(), wordInfos, ArticleSouceState.rubbish.name());
			}
		}
		
		
	
	}
	
	public void publish(String message, String updatetype,
			List<String> dataList,String state) {
		if (null == message || message.isEmpty()) {
			return;
		}

			logger.info("publish_message:" + message);
			if (super.pubMessage(message)) {
				logger.info("Publish Success.");
			} else {
				logger.error("Publish Failed.");
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

}
