package com.ifeng.iRecommend.lidm.userLog;

/**
 * <PRE>
 * 作用 : 
 *   进行解析log并将解析后的数据写入数据库。
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
 *          1.0          2014-01-21       lidm          change
 * -----------------------------------------------------------------------------
 * </PRE>
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.lidm.userLog.LogProcessor.LogType;

public class StartProcessLog {
	private static final Log LOG = LogFactory.getLog("log_to_hbase");

	public static void main(String[] args) {
		LOG.info("LogProcessor Started");
		//args = new String[] { "2014-09-28","1323"};
		LogType logType;
		/*String logTypesStr = LoadConfig.lookUpValueByKey("log_type").toLowerCase();
		
		if(logTypesStr.equals("pclog"))
			logType = LogType.PCLOG;
		else if(logTypesStr.equals("applog"))
			logType = LogType.APPLOG;
		else
			logType = LogType.UNDEFINED;*/
		logType = LogType.APPLOG;
		LogProcessor logProcessor = new LogProcessor(logType);
		logProcessor.initLogDate(args);
		ItemIKVOperation.ItemIKVInit();
		AppChannelsParser.initChannelTree();
		logProcessor.StartProcessLog();
	}
}
