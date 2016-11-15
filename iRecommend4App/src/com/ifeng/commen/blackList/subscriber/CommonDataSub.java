package com.ifeng.commen.blackList.subscriber;

import java.util.HashSet;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import com.ifeng.commen.blackList.BlackListData;
import com.ifeng.commen.blackList.util.CommonPSParams;


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
	private static String ALL_SUB_CHANNELS = CommonPSParams.blacklistKeyInRedis;//Test

	// 全局使用的Jedis实例
	private  Jedis subscriberJedis;
	
	// 订阅者(监听器),监听订阅频道的数据变化并进行处理
	private JedisPubSub subscriber;

	
	/**
	 * 初始化并订阅
	 */
	public void init() {
		try {
			logger.info("CommonDataSub init");
			subscriber = new Subscriber();
			subscriberJedis = new Jedis(CommonPSParams.commonDataRedisHost, CommonPSParams.commonDataRedisPort);	
			
		} catch (Exception e) {
			logger.error("Redis Init Error.", e);
			subscriberJedis = null;
			e.printStackTrace();
		}		
		
	}

	public CommonDataSub() {
		this.init();
	}
	
	@Override
	public void run() {
		subscriberJedis.subscribe(subscriber, ALL_SUB_CHANNELS);
	}
	
	
	public static void main(String[] args) {
		//加载日志文件
//    	String log4jPath = ContextListener.getWebProjectPath()+"/config/log4j.properties";
//		String log4jPath = "/config/log4j.properties";
//    	PropertyConfigurator.configure(log4jPath);
		
		Thread t=new Thread(new CommonDataSub());
		t.start();
		
		int i=0;
		while(true){
			System.out.println(BlackListData.getInstance().get_blacklist());
			try {
				Thread.sleep(5*1000l);
				i++;
				System.out.println(i);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*if(i==2){
				BlacklistPublisherTest blacklistPublisherTest = new BlacklistPublisherTest();
				blacklistPublisherTest.testUpdateWord_del();
			}*/
		}
		
//		HashSet<String> tempSet = new HashSet<String>();
//		tempSet.add("大象误踏地雷失足 专家做了只“鞋”");
//		
//		if(tempSet.contains("大象误踏地雷失足 专家做了只“鞋”")){
//			System.out.println(true);
//		}
		
		
	}
}
