/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.TimerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <PRE>
 * 作用 : 
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
 *          1.0          2016年1月28日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class AlgTimerManager {
	private static Log LOG = LogFactory.getLog("AlgTimerManager");
	private static List<Timer> timerList = new ArrayList<Timer>();
	/*
	 * 取消所有项目时调用
	 */
	public static synchronized void cancelAllTimers(){
		for(Timer timer : timerList){
			timer.cancel();
		}
	}
	
	/*
	 * 创建定时任务
	 * 
	 */
	public static Timer startATimer(final Runnable r,int delaySec,int updatePreiod,String timerName){
		LOG.info("Start timer : "+timerName);
		Timer timer = new Timer(timerName , true);
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(r).start();
			}
		}, delaySec*1000, updatePreiod*60*1000);
		timerList.add(timer);
		return timer;
	}
}
