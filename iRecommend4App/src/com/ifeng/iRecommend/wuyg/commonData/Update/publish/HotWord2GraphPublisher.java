package com.ifeng.iRecommend.wuyg.commonData.Update.publish;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.PublisherOperate;
import com.ifeng.iRecommend.wuyg.commonData.Update.UpdateActionType;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.CommonSubDataWord.WordState;
/**
 * 
 * <PRE>
 * 作用 : 
 *    热点事件发布到图数据库中  
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
 *          1.0          2016年5月9日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class HotWord2GraphPublisher extends PublisherOperate {
	
	private static Log LOG = LogFactory.getLog(HotWord2GraphPublisher.class);
	
	public HotWord2GraphPublisher(String channel) {
		super(channel);
		// TODO Auto-generated constructor stub
	}

	public HotWord2GraphPublisher(String key, String channel) {
		super(key, channel);
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
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        HotWord2GraphPublisher hotWord2GraphPublisher = new HotWord2GraphPublisher(LoadConfig.lookUpValueByKey("hotWordGraphMessageChannel"));
        
		HotWordPublisher hotWordPublisher = new HotWordPublisher(
				commonDataUpdateConfig.hotWordPattern,
				commonDataUpdateConfig.hotWordMessageChannel);
        
        List<String> dataList = new ArrayList<String>();
        
        dataList.add("TESTTESTTEST");
        dataList.add("HALO");

        String message = hotWordPublisher.updateWord_Add(dataList, WordState.read.name());

        hotWord2GraphPublisher.publish(message, UpdateActionType.ADD_WORD.name(), null, null);
        
        message = hotWordPublisher.updateWord_Del(dataList, WordState.read.name());
        
        hotWord2GraphPublisher.publish(message, UpdateActionType.DEL_WORD.name(), null, null);
        
       
		
	}

}
