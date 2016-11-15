/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadJobInterface;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.RankScoreUtil;

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
 *          1.0          2016年2月2日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class ScoreCacheClearJob implements PreloadJobInterface,Runnable {
	private static Log LOG = LogFactory.getLog("ScoreCacheClearJob");
	
	private long pvMapUpdateTimeStamp ;
	private long scoreCacheClearTimeStamp ;
	
	
	private static ScoreCacheClearJob instance = new ScoreCacheClearJob();
	
	private ScoreCacheClearJob(){
		pvMapUpdateTimeStamp = 0;
		scoreCacheClearTimeStamp = 0;
	}
	
	public static ScoreCacheClearJob getInstance(){
		return instance;
	}
	
	@Override
	public void preload() {
		LOG.info("ScoreCacheClearJob start  ~ ");
		// TODO Auto-generated method stub
		//每10分钟更新PVMap
		long time = System.currentTimeMillis() - pvMapUpdateTimeStamp;
		if(time > 10*60*1000){
			RankScoreUtil.getInstance().updatePVMap();
			pvMapUpdateTimeStamp = System.currentTimeMillis();
			LOG.info("Update PV ~");
		}
		
		//每隔1个小时清除scoreCache
		time =System.currentTimeMillis() - scoreCacheClearTimeStamp;
		if(time > 60*60*1000){
			RankScoreUtil.getInstance().clearCacheScoreMap();
			scoreCacheClearTimeStamp = System.currentTimeMillis();
			LOG.info("Clear cache");
		}
		
		//每次调度都执行一次编辑得分获取
		RankScoreUtil.getInstance().updateSFEditorScore();
		
		LOG.info("ScoreCacheClearJob finished ~");
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ScoreCacheClearJob";
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		preload();
	}

}
