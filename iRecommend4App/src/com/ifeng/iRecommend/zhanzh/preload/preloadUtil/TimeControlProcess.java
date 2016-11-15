/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.preloadUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadJobInterface;

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
 *          1.0          2016年1月29日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class TimeControlProcess implements Runnable {
	private static Log LOG = LogFactory.getLog("TimeControlProcess");
	private int interval;//调用时间间隔单位min
	private int delyTime;//延时执行时间单位min
	private PreloadJobInterface job ;
	public TimeControlProcess(PreloadJobInterface job, int interval ,int delyTime) {
		// TODO Auto-generated constructor stub
		this.job = job;
		this.interval = interval;
		this.delyTime = delyTime;
	}

	@Override
	public void run() {
		try {
			LOG.info(job.getName()+" Dely "+delyTime+" min ~");
			Thread.sleep(delyTime * 60 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		while(true){
			
			job.preload();

				try {
					LOG.info(job.getName()+" Sleep "+interval+" min ~");
					Thread.sleep(interval * 60 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
	}

}
