/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.blackList.subscriber.CommonDataSub;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.zhanzh.preload.editorChannelPreload.EditorChannelPreload;
import com.ifeng.iRecommend.zhanzh.preload.editorChannelPreload.IfengEditorToutiaoPreload;
import com.ifeng.iRecommend.zhanzh.preload.editorChannelPreload.ToutiaoPreload;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.TimeUtil;
import com.ifeng.iRecommend.zhanzh.preload.selectForPush.NewsSelectForPush;

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

public class AlgPreloadManager {
	
	private static Log LOG = LogFactory.getLog("AlgPreloadManager");
	public OSCache osCache ;
	private static final String CACHE_NAME = "OScache_AlgorithmChannelPreload";
	
	//线程池 执行预加载程序线程
	private static ExecutorService threadPool;
	//执行定时任务线程池
	private static ScheduledExecutorService schedulePool;
	
	private static AlgPreloadManager instance = new AlgPreloadManager();
	
	private static final String oscache_properties = "conf/oscache_AlgorithmChannel.properties";
	
	private AlgPreloadManager(){

		
		//初始化OScache 有效期一个月
		int refreshInterval = 30 * 24 * 60 * 60;
		LOG.info("refreshInterval = " + refreshInterval);
		osCache = new OSCache(oscache_properties, CACHE_NAME,
				refreshInterval);
//		osCache = new OSCache("conf/oscache_AlgorithmChannel.properties", CACHE_NAME,
//				refreshInterval);
		LOG.info(CACHE_NAME + " has created");
		
		//初始化线程池
		threadPool = Executors.newFixedThreadPool(20);
		schedulePool = Executors.newScheduledThreadPool(10);
		
		LOG.info("ThreadPool created ~");
	}
	
	public static AlgPreloadManager getInstance(){
		return instance;
	}
	
	public void preload(){
//		黑名单订阅线程
		Thread t=new Thread(new CommonDataSub());
		t.start();
		
		//主别名线程
		com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.CommonDataSub cds = new com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.CommonDataSub();
		Thread t1 = new Thread(cds);
		t1.start();
		
		//开启预加载Keyword任务
		BasicDataUpdateJob dateUpdateJob = BasicDataUpdateJob.getInstance();
		//cache定时清理和更新任务
		ScoreCacheClearJob clearJob = ScoreCacheClearJob.getInstance();
		//预加载主任务
		PreloadJob highPriorityJob = new PreloadJob(osCache, BasicDataUpdateJob.highPriority, dateUpdateJob.getConfigs(), 100);
		PreloadJob lowPriorityJob = new PreloadJob(osCache, BasicDataUpdateJob.lowPriority, dateUpdateJob.getConfigs(), 100);
		//编辑频道预加载模块
		EditorChannelPreload editorPreloadJob = new EditorChannelPreload(osCache);
		//手凤垂直频道预加载模块
		ShoufengChannelPreload sfPreload = new ShoufengChannelPreload(osCache);
		
		//头条数据预加载（旧版头条优化逻辑）
		ToutiaoPreload toutiaoPreloadJob = new ToutiaoPreload(dateUpdateJob.getConfigs());
		//头条推荐位数据预加载（探索版头条预加载）
		IfengEditorToutiaoPreload discoverToutiaoPreload = new IfengEditorToutiaoPreload();
		
		//编辑参与算法筛选数据的频道
		//泛编平台下线数据更新（后续将取代黑名单）
		NeedEditorCheckChannelPreload editorCheck = new NeedEditorCheckChannelPreload(dateUpdateJob.getConfigs());
		
		//凤凰热图榜
		IfengHotPicturePreload hotPicture = new IfengHotPicturePreload();
		//凤凰热闻榜（明明计算的热点数据）
		HighqualityNewsPreloadJob hqNews = new HighqualityNewsPreloadJob(dateUpdateJob.getConfigs());
		//凤凰视频热榜（视频热度计算排行榜）
		IfengVideoPreload videoPreload = new IfengVideoPreload(dateUpdateJob.getConfigs());
		
		//地域推送、兴趣推送筛选线程
		NewsSelectForPush newsPush = new NewsSelectForPush(osCache);
		
		//将凤凰热闻榜数据定时存储和备份
		RecordNewsPreload recordNewsJob=new RecordNewsPreload();
		
		//test评价泛编数据的测试程序
//		EvaluateDocUpdateNumByChannel evaluate = new EvaluateDocUpdateNumByChannel();
		
		//线程池定时任务
		schedulePool.scheduleWithFixedDelay(dateUpdateJob, 0, 15, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(clearJob, 0, 10, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(highPriorityJob, 2, 10, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(lowPriorityJob, 3, 120, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(editorPreloadJob, 10, 10, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(sfPreload, 1, 10, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(toutiaoPreloadJob, 0, 5, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(discoverToutiaoPreload, 0, 5, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(editorCheck, 0, 5, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(hotPicture, 0, 20, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(hqNews, 0, 30, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(videoPreload, 0, 10, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(newsPush, 6, 20, TimeUnit.MINUTES);
		schedulePool.scheduleWithFixedDelay(recordNewsJob, 5, 120, TimeUnit.MINUTES);
		
		//test
//		schedulePool.scheduleWithFixedDelay(evaluate, 6, 30, TimeUnit.MINUTES);

		
		//启动线程
//		TimeControlProcess dateUpdateProcess = new TimeControlProcess(dateUpdateJob, 10, 0);
//		Thread dateUpdateThread = new Thread(dateUpdateProcess);
//		dateUpdateThread.start();
//		
//		TimeControlProcess clearProcess = new TimeControlProcess(clearJob, 10, 0);
//		Thread clearThread = new Thread(clearProcess);
//		clearThread.start();
//		
//		TimeControlProcess highPriorityProcess = new TimeControlProcess(highPriorityJob, 10, 3);
//		Thread highPriorityThread = new Thread(highPriorityProcess);
//		highPriorityThread.start();
//		
//		TimeControlProcess lowPriorityProcess = new TimeControlProcess(lowPriorityJob, 120, 5);
//		Thread lowPriorityThread = new Thread(lowPriorityProcess);
//		lowPriorityThread.start();
		
		
		
	}
	
	/**
	 * 用于像线程池提交线程
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public synchronized Future submit(Runnable r){
		Future f = threadPool.submit(r);
		return f;
	}
	
	public static void main(String[] args){
		AlgPreloadManager.getInstance().preload();
	}
	

}
