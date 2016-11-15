package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifeng.commen.Utils.MailSenderWithRotam;
import com.ifeng.iRecommend.wuyg.commonData.entity.entityBase.KnowledgeBaseBuild;

/**
 * <PRE>
 * 作用 : 
 *   消息的处理类。根据消息的频道，选择相应的更新处理方法。
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
	private static Logger logger = LoggerFactory
			.getLogger(MessageProcesser.class);

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
		case entLib:
			logger.info("Start Updating Local entLib Data...");
			updateState = updateEntlib(message);
			if (updateState) {
				logger.info("Local entLib Data Update Successed.");
			} else {
				logger.error("Local entLib Data Update Failed.");
			}

			break;
		case blackList:
			logger.info("Start Updating Local blackList Data...");
			updateState = updateBlackList(message);
			if (updateState) {
				logger.info("Local blackList Data Update Successed.");
			} else {
				logger.error("Local blackList Data Update Failed.");
			}
			break;
		case hotWord:
			logger.info("Start Updating Local hotWord Data...");
			updateState = updateHotWord(message);
			if (updateState) {
				logger.info("Local HotWordMap Data Update Successed.");
			} else {
				logger.error("Local HotWordMap Data Update Failed.");
			}
			break;
		case wordReadable:
			logger.info("Start Updating Local hotWord Data...");
			updateState = updateWord(message);
			if (updateState) {
				logger.info("Local HotWordMap Data Update Successed.");
			} else {
				logger.error("Local HotWordMap Data Update Failed.");
			}
			break;
		case articleSource:
			logger.info("Start Updating Local articleSource Data ...");
			updateState = updateArticleSource(message);
			if (updateState) {
				logger.info("Local articleSource Data Update Successed.");
			} else {
				logger.error("Local articleSource Data Update Failed.");
			}
			break;
		case aliasData:
			logger.info("Satart Updating Local aliasLibData Data ...");
            updateState = updateAliasLibData(message);
            if(updateState){
            	logger.info("Local aliasData Update Successed.");
            }else{
            	logger.error("Local aliasData Update Failed.");
            }
            break;
		case dataExpLib:
			logger.info("Start Updating Local dataExpLib Data ...");
			updateState = updateDataExpLibData(message);
			if(updateState){
				logger.info("Local dataExpLibData Update Successed.");
			}else{
				logger.error("Local dataExpLibData Update Failed.");
			}
			break;
		case allWordLib:
			logger.info("Start Updating Local AllWordLib Data ...");
			updateState = updateAllWordLibData(message);
			if(updateState){
				logger.info("Local AllWordLib Update Successed.");
			}else{
				logger.error("Local AllWordLib Update Failed.");
			}
			break;
		case tempCustomDic:
			logger.info("Start Updating Local tempCustomDic Data...");
			updateState = updateTempCustomDic(message);
			if(updateState){
				logger.info("Local tempCustomDic Update Successed.");
			}else{
				logger.error("Local tempCustomDic Update Failed.");
			}
			break;
		case hotWordGraph:
			logger.info("Start Updating HotWordGraph Data...");
			updateState = updatehotWordGraphMessageChannel(message);
			if(updateState){
				logger.info("HotWordGraph Update Successed.");
			}else{
				logger.error("HotWordGraph Update Failed.");
			}
			
			// 其他分支处理 ...
		default:
			logger.error("Can Not Process This Channel:" + channel);
			break;
		}
	}
	
	
	public static boolean  updatehotWordGraphMessageChannel(String message){
		logger.info("Start Updating HotWordGraph...");

		boolean updateState = false;
		if (null == message || message.isEmpty()) {
			logger.error("HotWordGraph Empty Input Message");
			return updateState;
		}

		String[] messageArray = message.split("#DIV#");
		if (3 != messageArray.length) {
			logger.error("HotWordGraph Message Format Invalid.");
			return updateState;
		}

		String action = messageArray[0];
		String type = messageArray[1];
		String value = messageArray[2];

		if (action.equals("add")) {
			HotWordGraphData.getInstance().addElem2Local(value);
			updateState = true;
		} else if (action.equals("del")) {
			HotWordGraphData.getInstance().delElemFromLocal(value);
			updateState = true;
		} else if (action.equals("alter")) {
			HotWordGraphData.getInstance().alterElemInLocal(value);
			updateState = true;
		} else {
			logger.error("HotWordGraph Message Format Invalid.");
		}

		return updateState;
		
	}

	/**
	 * @Title: updateEntlib
	 * @Description: 实体库消息处理方法,更新应用内存中的实例数据
	 * @author liu_yi
	 * @param message
	 * @return 方法执行状态码。success返回true, fail返回false
	 * @throws
	 */
	public static boolean updateEntlib(String message) {
		boolean updateState = false;
		if (null == message || message.isEmpty()) {
			logger.error("Entlib Empty Input Message");
			return updateState;
		}

		String[] messageArray = message.split("#DIV#");
		if (3 != messageArray.length) {
			logger.error("Entlib Message Format Invalid.");
			return updateState;
		}

		logger.info("Start Updating Entlib...");

		String action = messageArray[0];
		String type = messageArray[1];
		String value = messageArray[2];

		if (action.equals("add") && type.equals("word")) {
			KnowledgeBaseBuild.updateWords_Add(value);
			updateState = true;
		} else if (action.equals("alter") && type.equals("word")) {
			KnowledgeBaseBuild.updateWords_Alter(value);
			updateState = true;
		} else if (action.equals("del") && type.equals("word")) {
			KnowledgeBaseBuild.updateWords_Del(value);
			updateState = true;
		} else if (action.equals("add") && type.equals("file")) {
			KnowledgeBaseBuild.updateFile_Add(value);
			updateState = true;
		} else if (action.equals("alter") && type.equals("file")) {
			KnowledgeBaseBuild.updateFile_Alter(value);
			updateState = true;
		} else if (action.equals("del") && type.equals("file")) {
			KnowledgeBaseBuild.updateFile_Del(value);
			updateState = true;
		} else {
			logger.error("Entlib Message Format Invalid.");
		}

		logger.info("Entlib Update End.");

		// 如果是文件更新，即领域更新，可能改动比较大，发告警, 发邮件通知相关人员
		if (type.equals("file")) {
			String mail_subject = "EntLib File Update Action Warning!";
			message = message.substring(0, message.lastIndexOf("&"));
			message = message.replaceAll("&", "\tand\t");
			String mail_content = "<strong>EntLib File Update</strong><br><br>Detail Message:<br>"
					+ message + "<br>";
			String mail_receiver_config_name = "entLibExceptionReceivers";
			MailSenderWithRotam mswr = new MailSenderWithRotam(mail_subject,
					mail_content, mail_receiver_config_name);
			mswr.sendEmailWithRotam();
		}

		return updateState;
	}

	/**
	 * @Title: updateBlackList
	 * @Description: 黑名单通用数据消息处理方法，更新应用内存中的实例数据
	 * @author liu_yi
	 * @param message
	 * @return
	 * @throws
	 */
	public static boolean updateBlackList(String message) {
		// blackList msg process code
		logger.info("Start Updating BlackList...");

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
			logger.error("BlackList Message Format Invalid.");
		}

		// 关键词比较重要，更新成功的时候发邮件
		if (updateState) {
			String mail_subject = "Blacklist Update Action Warning!";
			String mail_content = "<strong>Blacklist Update</strong><br><br>Detail Message:<br>"
					+ message + "<br>";
			String mail_receiver_config_name = "blacklistExceptionReceivers";
			MailSenderWithRotam mswr = new MailSenderWithRotam(mail_subject,
					mail_content, mail_receiver_config_name);
			mswr.sendEmailWithRotam();
		}

		return updateState;
	}

	/**
	 * 热词通用数据消息处理方法，更新应用内存中的实例数据
	 * 
	 * @param message
	 * @return
	 */
	public static boolean updateHotWord(String message) {
		// blackList msg process code
		logger.info("Start Updating HotWord...");

		boolean updateState = false;
		if (null == message || message.isEmpty()) {
			logger.error("HotWord Empty Input Message");
			return updateState;
		}

		String[] messageArray = message.split("#DIV#");
		if (3 != messageArray.length) {
			logger.error("HotWord Message Format Invalid.");
			return updateState;
		}

		String action = messageArray[0];
		String type = messageArray[1];
		String value = messageArray[2];

		if (action.equals("add")) {
			HotWordData.getInstance().addElem2Local(value);
			updateState = true;
		} else if (action.equals("del")) {
			HotWordData.getInstance().delElemFromLocal(value);
			updateState = true;
		} else if (action.equals("alter")) {
			HotWordData.getInstance().alterElemInLocal(value);
			updateState = true;
		} else {
			logger.error("HotWord Message Format Invalid.");
		}
		// 由于热词更新频繁，定时的任务，因此此处不予每次都邮件通知

		return updateState;
	}

	/**
	 * 可读表通用数据消息处理方法，更新应用内存中的实例数据
	 * 
	 * @param message
	 * @return
	 */
	public static boolean updateWord(String message) {
		// blackList msg process code
		logger.info("Start Updating WordRead...");

		boolean updateState = false;
		if (null == message || message.isEmpty()) {
			logger.error("WordRead Empty Input Message");
			return updateState;
		}

		String[] messageArray = message.split("#DIV#");
		if (3 != messageArray.length) {
			logger.error("WordRead Message Format Invalid.");
			return updateState;
		}

		String action = messageArray[0];
		String type = messageArray[1];
		String value = messageArray[2];

		if (action.equals("add")) {
			WordReadData.getInstance().addElem2Local(value);
			updateState = true;
		} else if (action.equals("del")) {
			WordReadData.getInstance().delElemFromLocal(value);
			updateState = true;
		} else if (action.equals("alter")) {
			WordReadData.getInstance().alterElemInLocal(value);
			updateState = true;
		} else {
			logger.error("WordRead Message Format Invalid.");
		}

		return updateState;
	}

	/**
	 * 
	 * @Title:updateArticleSource
	 * @Description: 稿源信息通用数据消息处理方法，更新应用内存中的实例数据
	 * @param message
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月30日
	 */
	public static boolean updateArticleSource(String message) {
		// blackList msg process code
		logger.info("Start Updating ArticleSource...");

		boolean updateState = false;
		if (null == message || message.isEmpty()) {
			logger.error("ArticleSource Empty Input Message");
			return updateState;
		}

		String[] messageArray = message.split("#DIV#");
		if (3 != messageArray.length) {
			logger.error("ArticleSource Message Format Invalid.");
			return updateState;
		}

		String action = messageArray[0];
		String type = messageArray[1];
		String value = messageArray[2];

		if (action.equals("add")) {
			ArticleSourceData.getInstance().addElem2Local(value);
			updateState = true;
		} else if (action.equals("del")) {
			ArticleSourceData.getInstance().delElemFromLocal(value);
			updateState = true;
		} else if (action.equals("alter")) {
			logger.info("this function is not exist!");
			updateState = false;
		} else {
			logger.error("ArticleSource Message Format Invalid.");
		}

		return updateState;
	}

	/**
	 * 
	 * @Title:updateAliasLibData
	 * @Description: 主别名库通用数据消息处理方法，更新应用内存中的实例数据
	 * @param message  add#DIV#elem#DIV#山东鲁能#KEY#体育#ALIAS#
	 * @return
	 * @author:wuyg1
	 * @date:2016年2月22日
	 */
	public static boolean updateAliasLibData(String message) {
		// blackList msg process code
		logger.info("Start Updating AliasLibData...");

		boolean updateState = false;
		if (null == message || message.isEmpty()) {
			logger.error("AliasLibData Empty Input Message");
			return updateState;
		}

		String[] messageArray = message.split("#DIV#");
		if (3 != messageArray.length) {
			logger.error("AliasLibData Message Format Invalid.");
			return updateState;
		}

		String action = messageArray[0];
		String type = messageArray[1];
		String value = messageArray[2];

		if (action.equals("add")) {
			AliasLibData.getInstance().addElem2Local(value);
			updateState = true;
		} else if (action.equals("del")) {
			AliasLibData.getInstance().delElemFromLocal(value);
			updateState = true;
		} else if (action.equals("alter")) {
			AliasLibData.getInstance().alterElemInLocal(value);
			updateState = true;
		} else {
			logger.error("AliasLibData Message Format Invalid.");
		}

		return updateState;
	}


	public static boolean updateDataExpLibData(String message) {
		// blackList msg process code
		logger.info("Start Updating DataExpLib...");

		boolean updateState = false;
		if (null == message || message.isEmpty()) {
			logger.error("DataExpLib Empty Input Message");
			return updateState;
		}

		String[] messageArray = message.split("#DIV#");
		if (3 != messageArray.length) {
			logger.error("DataExpLib Message Format Invalid.");
			return updateState;
		}

		String action = messageArray[0];
		String type = messageArray[1];
		String value = messageArray[2];

		if (action.equals("add")) {
			DataExpLib.getInstance().addElem2Local(value);
			updateState = true;
		} else if (action.equals("del")) {
			DataExpLib.getInstance().delElemFromLocal(value);
			updateState = true;
		} else if (action.equals("alter")) {
			DataExpLib.getInstance().alterElemInLocal(value);
			updateState = true;
		} else {
			logger.error("DataExpLib Message Format Invalid.");
		}

		return updateState;
	}
	
	
	public static boolean updateAllWordLibData(String message) {
		// blackList msg process code
		logger.info("Start Updating AllWordLib...");

		boolean updateState = false;
		if (null == message || message.isEmpty()) {
			logger.error("AllWordLib Empty Input Message");
			return updateState;
		}

		String[] messageArray = message.split("#DIV#");
		if (3 != messageArray.length) {
			logger.error("AllWordLib Message Format Invalid.");
			return updateState;
		}

		String action = messageArray[0];
		String type = messageArray[1];
		String value = messageArray[2];

		if (action.equals("add")) {
			AllWordLibData.getInstance().addElem2Local(value);
			updateState = true;
		} else if (action.equals("del")) {
			AllWordLibData.getInstance().delElemFromLocal(value);
			updateState = true;
		} else if (action.equals("alter")) {
			logger.info("this function is not exist!");
			updateState = false;
		} else {
			logger.error("DataExpLib Message Format Invalid.");
		}

		return updateState;
	}
	
	public static boolean updateTempCustomDic(String message) {
		logger.info("Start Updating tempCustomDic...");
		
		boolean updateState = false;
		if (null == message || message.isEmpty()) {
			logger.error("TempCustomDic Empty Input Message");
			return updateState;
		}

		String[] messageArray = message.split("#DIV#");
		if (3 != messageArray.length) {
			logger.error("TempCustomDic Message Format Invalid.");
			return updateState;
		}

		String action = messageArray[0];
		String type = messageArray[1];
		String value = messageArray[2];
		
		if (action.equals("add")) {
			CustomWordUpdate.getInstance().addElem2Local(value);
			updateState = true;
		} else if (action.equals("del")) {
			// 暂时空着，有需要再实现
		} else if (action.equals("alter")) {
			// 暂借alter字段用来传输已经重启过的分词机器
			CustomWordUpdate.getInstance().alterElemInLocal(value);
			updateState = true;
		}
		
		return updateState;
	}
	
	// other commonData msg process func
}
