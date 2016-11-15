package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifeng.commen.Utils.LoadConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * <PRE>
 * 作用 : 
 *   通用数据层数据统一订阅工具类。可以订阅多个频道。
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
public class CommonDataSub implements Runnable{
	private static Logger logger = LoggerFactory.getLogger(CommonDataSub.class);
	
	// 应用中用到的所有数据频道名称(名称在配置文件中)，没用到的通用数据可以在自己的应用中删除
	private static String[] ALL_SUB_CHANNELS = {};
//	private static String[] ALL_SUB_CHANNELS = { "entLibTEST", "blackListTestTEST","hotWordTEST","wordReadableTEST","articleSourceTEST","aliasDataTEST","dataExpLibTEST","allWordLibTEST","tempCustomDic"};
	private  String commonDataRedisHost;
	private  int commonDataRedisPort;

	// 全局使用的Jedis实例
	private  Jedis subscriberJedis;
	
	// 订阅
	private JedisPubSub Subscriber;

	public void init() {
		try {
			commonDataRedisPort = Integer.valueOf(LoadConfig.lookUpValueByKey("commonDataRedisPort"));
			commonDataRedisHost = LoadConfig.lookUpValueByKey("commonDataRedisHost");
			// 默认超时时间正常情况够了，不用设置
			subscriberJedis = new Jedis(commonDataRedisHost, commonDataRedisPort);
			
			String allchannels = LoadConfig.lookUpValueByKey("ALL_SUB_CHANNELS");
			ALL_SUB_CHANNELS = allchannels.split(LoadConfig.lookUpValueByKey("SPLITFLAG"));
			
		} catch (Exception e) {
			logger.error("Redis Init Error.", e);
			subscriberJedis = null;
		}
		
		Subscriber = new Subscriber();
	}

	public CommonDataSub() {
		this.init();
	}
	
	@Override
	public void run() {
		if (null != this.subscriberJedis) {
			this.subscriberJedis.subscribe(this.Subscriber, ALL_SUB_CHANNELS);
		} else {
			logger.error("subscriberJedis Error:Empty");
		}
		
	}
}
