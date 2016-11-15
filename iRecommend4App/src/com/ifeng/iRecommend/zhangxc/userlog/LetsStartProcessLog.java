package com.ifeng.iRecommend.zhangxc.userlog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.iRecommend.zhangxc.userlog.LogProcessor.LogType;

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
 *          1.0          2015-10-10       kedm          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class LetsStartProcessLog {
	private static final Log LOG = LogFactory.getLog("log_to_hbase");

	public static void main(String[] args) {
		LOG.info("LogProcessor Started");
		//args = new String[] { "2016-08-08","0900"};
		LogType logType = null;
		
		logType = logType.EXLOG;
		LogProcessor logProcessor = new LogProcessor(logType);
		logProcessor.initLogDate(args);
		logProcessor.StartProcessLog();
	}
}
