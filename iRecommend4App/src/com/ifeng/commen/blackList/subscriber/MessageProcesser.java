package com.ifeng.commen.blackList.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifeng.commen.blackList.BlackListData;
import com.ifeng.commen.blackList.util.ChannelName;
import com.ifeng.commen.blackList.util.MailSenderWithRotam;

/**
 * <PRE>
 * 作用 : 
 *   消息的处理类。根据消息的频道，执行本地数据更新等相关操作。
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
 *          1.0          2015年11月25日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class MessageProcesser {
	private static Logger logger = LoggerFactory.getLogger(MessageProcesser.class);
	
	/**
	 * @Title: msgProcessControl
	 * @Description: 消息处理控制。根据消息频道名称，选择相应频道消息处理方法
	 * @author liu_yi
	 * @param channel
	 * @param message
	 * @throws
	 */
	public static void msgProcessControl(String channel, String message) {
		if (null == channel || channel.isEmpty()) {
			logger.error("Channel Invalid: Null OR Empty");
			return;
		}
		
		if (null == message || message.isEmpty()) {
			logger.error("Message Invalid: Null OR Empty");
			return;
		}
		
		boolean updateState = false;
		switch (ChannelName.getMessageType(channel)) {
		case blackList:
			logger.info("Start Updated Local blackList Data...");
			updateState = updateBlackList(message);
			if (updateState) {
				logger.info("Local blackList Data Update Successed.");
			} else {
				logger.error("Local blackList Data Update Failed.");
			}
			break;
		// 其他分支处理 ...
		default:
			logger.error("Can Not Process This Channel:" + channel);
			break;
		}
	}
	
	
	/**
	 * @Title: updateBlackList
	 * @Description: 黑名单通用数据消息处理方法，更新应用内存中的实例数据
	 * @author liu_yi
	 * @param message action#DIV#type#DIV#value
	 * @return
	 * @throws
	 */
	public static boolean  updateBlackList(String message) {
		// blackList msg process code
		logger.info("Start Update BlackList...");

		boolean updateState = false;
		if (null == message || message.isEmpty()) {
			logger.error("BlackList Empty Input Message");
			return updateState;
		}
		
		String[] messageArray = message.split("#DIV#");
		if (3 != messageArray.length) {
			logger.error("BlackList Message Format Invalid.");
			return updateState;
		}
		
		String action = messageArray[0];
		String type = messageArray[1];
		String value = messageArray[2];
		
		if (action.equals("add")) {
			BlackListData.getInstance().addElems2BlackList(value);
			updateState = true;
		} else if (action.equals("del")) {
			BlackListData.getInstance().delElemsFromBlackList(value);
			updateState = true;
		} else {
			logger.error("BlackList Message action Invalid.");
		}
		
		// 更新成功的时候发邮件
//		if (updateState) {
//			String mail_subject = "Blacklist Update Action Warning!";
//			String mail_content = "<strong>Blacklist Update</strong><br><br>Detail Message:<br>" + message + "<br>";
//			String mail_receiver_config_name = "blacklistExceptionReceivers";
//			MailSenderWithRotam mswr = new MailSenderWithRotam(mail_subject, mail_content, mail_receiver_config_name);
//			mswr.sendEmailWithRotam();
//		}
		
		return updateState;
	}
	
	// other commonData msg process func 
}
