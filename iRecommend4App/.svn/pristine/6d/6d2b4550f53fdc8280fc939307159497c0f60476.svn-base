package com.ifeng.iRecommend.kedm.userlog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.kedm.userlog.LogProcessor.LogType;
import com.ifeng.iRecommend.kedm.util.ItemIKVOperation;
import com.ifeng.iRecommend.kedm.util.UserCenterIKVOperation;
import com.ifeng.iRecommend.kedm.util.UserlogIKVOperation;

/**
 * <PRE>
 * 作用 : 解析userlog的入口类，根据业务类型实现不同的解析和存储方式
 *   
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
 *          1.0          2015-10-10        kedm          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class StartProcessLog {
	private static final Log LOG = LogFactory.getLog("log_to_hbase");

	public static void main(String[] args) {
		LOG.info("LogProcessor Started");
		//args = new String[] { "2016-08-08","0900"};
		LogType logType = null;
		/*String logTypesStr = LoadConfig.lookUpValueByKey("log_type").toLowerCase();
		
		if(logTypesStr.equals("pclog"))
			logType = LogType.PCLOG;
		else if(logTypesStr.equals("applog"))
			logType = LogType.APPLOG;
		else
			logType = LogType.UNDEFINED;*/
		//logType = LogType.APPLOG;
		
		//logType决定解析过程，目前APPLOGNEW为解析后发送ikv的用户中心体系，APPLOG为新闻客户端解析应用与cp推荐
		logType = logType.APPLOGNEW;
		
		LogProcessor logProcessor = new LogProcessor(logType);
		logProcessor.initLogDate(args);
		UserCenterIKVOperation.init();
		//ItemIKVOperation.ItemIKVInit();
		//UserlogIKVOperation.userlogIKVInit();
		//AppChannelsParser.initChannelTree();
		logProcessor.StartProcessLog();
	}
}
