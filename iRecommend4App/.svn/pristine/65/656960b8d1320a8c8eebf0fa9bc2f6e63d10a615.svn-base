package com.ifeng.iRecommend.wuyg.commonData.Update.publish;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.PublisherOperate;
import com.ifeng.iRecommend.wuyg.commonData.Update.UpdateActionType;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
/**
 * 
 * <PRE>
 * 作用 : 
 *     信息全表达数据的发布订阅系统，发布端  
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
 *          1.0          2016年2月22日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class DataExpLibPublisher extends PublisherOperate {
	private static Log LOG = LogFactory.getLog(DataExpLibPublisher.class);
	public DataExpLibPublisher(String host, String port, String channel,
			String key) {
		super(host, port, channel, key);
	}
	
	public DataExpLibPublisher(String channel, String key){
		super(channel, key);
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

	public static void main(String[] args) {

           FileUtil fileUtil = new FileUtil();
           String content = fileUtil.Read("E:/信息全表达/全表达样本.txt", "utf-8");
          
           List<String> wordInfos = new ArrayList<String>();
           DataExpLibPublisher dataExpLibPublisher = new DataExpLibPublisher(LoadConfig.lookUpValueByKey("dataExpLibMessageChannel"), LoadConfig.lookUpValueByKey("dataExpLibPattern"));
          
           List<String> dataList = Arrays.asList(content.split("\n"));
           StringBuffer sBuffer = new StringBuffer();
           for(String dataExp : dataList){
        	   String [] names = dataExp.split("\t");
        	   //System.err.println(dataExp);

        	   sBuffer.append(names[0]+LoadConfig.lookUpValueByKey("keySegment"));
        	   for(int index=1; index<names.length; index++){
        		   sBuffer.append(names[index]+LoadConfig.lookUpValueByKey("ExpSegment"));
        	   }
        	   wordInfos.add(sBuffer.toString());
        	   sBuffer = new StringBuffer();
           }

           String message = dataExpLibPublisher.updateWord_Add(wordInfos);
           
           dataExpLibPublisher.publish(message, UpdateActionType.ADD_WORD.name(), wordInfos, null);

           wordInfos.clear();
           sBuffer = new StringBuffer();
   		   content = fileUtil.Read("e:/信息全表达/全表达样本_alter.txt", "utf-8");
   		   dataList = Arrays.asList(content.split("\n"));
           
           for(String dataExp : dataList){
        	   String [] names = dataExp.split("\t");
        	   //System.err.println(dataExp);

        	   sBuffer.append(names[0]+LoadConfig.lookUpValueByKey("keySegment"));
        	   for(int index=1; index<names.length; index++){
        		   sBuffer.append(names[index]+LoadConfig.lookUpValueByKey("ExpSegment"));
        	   }
        	   wordInfos.add(sBuffer.toString());
        	   sBuffer = new StringBuffer();
           }
           
           message = dataExpLibPublisher.updateWord_Alter(wordInfos, null);
           
           dataExpLibPublisher.publish(message, UpdateActionType.ALTER_WORD.name(), wordInfos, null);
           
           wordInfos.clear();
           sBuffer = new StringBuffer();
           content = fileUtil.Read("e:/信息全表达/全表达样本_del.txt", "utf-8");
           dataList = Arrays.asList(content.split("\n"));
           
           for(String dataExp : dataList){
        	   String [] names = dataExp.split("\t");
        	   //System.err.println(dataExp);

        	   sBuffer.append(names[0]+LoadConfig.lookUpValueByKey("keySegment"));
        	   for(int index=1; index<names.length; index++){
        		   sBuffer.append(names[index]+LoadConfig.lookUpValueByKey("ExpSegment"));
        	   }
        	   wordInfos.add(sBuffer.toString());
        	   sBuffer = new StringBuffer();
           }
   		   
           message = dataExpLibPublisher.updateWord_Del(wordInfos, null);
           
           dataExpLibPublisher.publish(message, UpdateActionType.DEL_WORD.name(), wordInfos, null);
	}

}
