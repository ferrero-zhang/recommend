package com.ifeng.commen.blackList.publisher;

import redis.clients.jedis.Jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifeng.commen.blackList.util.CommonPSParams;


/**
 * <PRE>
 * 作用 : 
 *   通用数据发布订阅消息
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *  1. 在发布数据的时候，应该保证redis中数据更新与发布消息是原子操作。该类仅用于消息发布，数据原子性操作应该在具体的数据更新类中保证。
 *     更新包括两部分：更新数据到redis，发布消息到相应订阅频道
 *     example1: 数据2redis fail, 此时不应该再发消息通知
 *     example2: 数据2redis suc, 发布消息fail，此时应该回滚redis改动
 *     example3: 数据2redis suc, 发布消息suc，此时完整更新有效
 *  2. 各模块数据发布消息时继承该类，在必要的情况下可以重写发布消息相关操作
 *  
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年11月25日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class Publisher {
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);
    
    /**
     * @Fields channel : 发布消息的频道name
     */
    protected String channelName;
    
	// 全局使用的Jedis实例
	protected static Jedis publisherJedis;

    /**
     * @Title: init
     * @Description: 部分类常量初始化
     * @author liu_yi
     * @throws
     */
    public void init() {
		try {
			publisherJedis = new Jedis(CommonPSParams.commonDataRedisHost, CommonPSParams.commonDataRedisPort);
			publisherJedis.select(CommonPSParams.commonDataDbNum);
		} catch (Exception e) {
			logger.error("Redis Init Error:", e);
			publisherJedis = null;
		}
	}

    /**
     * <p>Title: Publisher</p>
     * <p>Description: 构造方法</p>
     * @author liu_yi
     * @param channel
     */
    public Publisher(String channel) {
    	this.init();
        this.channelName = channel;
    }
    
    /**
     * @Title: pubMessage
     * @Description: 发布消息时调用的方法
     * @author liu_yi
     * @return 返回发布状态码。success返回true, fail返回false
     * @throws
     */
    public boolean pubMessage(String message) {
    	boolean operateState = false;
   
    	if (null != publisherJedis) {
    		logger.info("Start Pub Message...");
    		publisherJedis.publish(channelName, message);
    		operateState=true;
    		logger.info("Has Published Message to Channel: {}, Msg: {}", channelName, message);
    	} else {
    		logger.error("publisherJedis Error: Null Jedis Instance");
    	}
    	
    	return operateState;
    }
      
    // test main
    public static void main(String[] args) {
    	/*String pubChannelName = LoadConfig.lookUpValueByKey("entLibMessageChannel");
    	String pubMessage = "add#word#value";
    	Publisher publisherInstance = new Publisher(pubChannelName);
    	if (publisherInstance.pubMessage(pubMessage)) {
    		System.out.println("Publish Success.");
    	} else {
    		System.out.println("Publish Failed.");
    	}*/
    }
}
