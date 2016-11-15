package com.ifeng.iRecommend.lidm.userLog;

/**
 * <PRE>
 * 作用 : 
 *   数据写入线程。将log解析后的数据写入hbase或其它数据库。
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

import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 用于数据写入的内部线程类
 * 
 * @author lidm
 * 
 */
public class LogPushThread extends Thread {
	private static final Log LOG = LogFactory.getLog("log_to_hbase");
	private LogToDB logToDB;
	private CountDownLatch countDownLatch;

	public LogPushThread(String threadName, CountDownLatch countDownLatch,
			LogToDB logToDB) {
		super(threadName);
		this.countDownLatch = countDownLatch;
		this.logToDB = logToDB;
	}

	public void run() {
		try {
			logToDB.PushLogToDB();
			countDownLatch.countDown();
		} catch (Exception e) {
			LOG.error("failed push data:"+getName(), e);
		}
	}
}
