package com.ifeng.commen.blackList.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPubSub;

/**
 * <PRE>
 * 作用 : 
 *   订阅消息的监听类，继承自JedisPubSub
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
public class Subscriber extends JedisPubSub {
	private static Logger logger = LoggerFactory.getLogger(Subscriber.class);
  
    /* (非 Javadoc)
     * <p>Title: onMessage</p>
     * <p>Description: 接收订阅消息,并进行处理</p>
     * @param channel
     * @param message
     * @see redis.clients.jedis.JedisPubSub#onMessage(java.lang.String, java.lang.String)
     */
    @Override
    public void onMessage(String channel, String message) {
    	logger.info("Message received. Channel: {}, Msg: {}", channel, message);
//    	System.out.println("Message received. Channel: {}, Msg: {}"+ channel+message);
        
        // TODO 根据消息类型，执行本地数据更新等相关操作
    	MessageProcesser.msgProcessControl(channel, message);
    }

    /* (非 Javadoc)
     * <p>Title: onSubscribe</p>
     * <p>Description: 订阅初始化动作</p>
     * @param channel
     * @param subscribedChannels
     * @see redis.clients.jedis.JedisPubSub#onSubscribe(java.lang.String, int)
     */
    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
    	logger.info("Init Message received. Channel: {}, subscribedChannels: {}", channel, subscribedChannels);
    	
    	// do some other work
    }

    /* (非 Javadoc)
     * <p>Title: onUnsubscribe</p>
     * <p>Description: 取消订阅动作</p>
     * @param channel
     * @param subscribedChannels
     * @see redis.clients.jedis.JedisPubSub#onUnsubscribe(java.lang.String, int)
     */
    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
    	logger.info("onUnsubscribe. Channel: {}, subscribedChannels: {}", channel, subscribedChannels);
    	
    	// do some other work
    }

	@Override
	public void onPMessage(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPSubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPUnsubscribe(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
