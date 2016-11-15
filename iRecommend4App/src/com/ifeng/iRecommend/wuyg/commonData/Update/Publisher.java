package com.ifeng.iRecommend.wuyg.commonData.Update;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifeng.commen.Utils.LoadConfig;

/**
 * <PRE>
 * 作用 : 
 *   通用数据发布订阅消息
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *  1. 在发布数据的时候，应该保证redis中数据与发布消息是原子操作。该类仅用于消息发布，数据原子性操作应该在具体的数据更新类中保证。
 *     更新包括两部分：更新数据到redis，发布消息到相应订阅频道
 *     example1: 数据2redis fail, 此时不应该再发消息通知
 *     example2: 数据2redis suc, 发布消息fail，此时应该回滚redis改动
 *     example3: 数据2redis suc, 发布消息suc，此时完整更新有效
 *  2. 各模块数据发布消息时继承该类，在必要的情况下可以重写发布消息相关操作
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年11月25日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public abstract class Publisher {
	protected static final Logger logger = LoggerFactory
			.getLogger(Publisher.class);

	/**
	 * @Fields channel : 发布消息的频道name
	 */
	protected String channelName;

	// 发布消息时使用的redis host&port
	protected String commonDataRedisHost;
	protected int commonDataRedisPort;

	// 全局使用的Jedis实例
	protected Jedis publisherJedis;

	/**
	 * @Title: init
	 * @Description: 部分类常量初始化
	 * @author liu_yi
	 * @throws
	 */
	public void init() {
		try {
			commonDataRedisPort = Integer.valueOf(LoadConfig
					.lookUpValueByKey("commonDataRedisPort"));
			commonDataRedisHost = LoadConfig
					.lookUpValueByKey("commonDataRedisHost");
			// 默认超时时间正常情况够了，不用设置
			publisherJedis = new Jedis(commonDataRedisHost, commonDataRedisPort);
		} catch (Exception e) {
			logger.error("Redis Init Error:", e);
			publisherJedis = null;
		}
	}
    /**
     * 
    * @Title:init
    * @Description:
    * @param host  redis的ip
    * @param port  redis的端口号
    * @author:wuyg1
    * @date:2016年2月19日
     */
	public void init(String host,String port) {
		try {
			commonDataRedisPort = Integer.valueOf(port);
			commonDataRedisHost = host;
			// 默认超时时间正常情况够了，不用设置
			publisherJedis = new Jedis(commonDataRedisHost, commonDataRedisPort);
		} catch (Exception e) {
			logger.error("Redis Init Error:", e);
			publisherJedis = null;
		}
	}
	
	/**
	 * <p>
	 * Title: Publisher
	 * </p>
	 * <p>
	 * Description: 构造方法
	 * </p>
	 * 
	 * @author liu_yi
	 * @param channel
	 * @param publishMessage
	 */
	public Publisher(String channel) {
		this.init();
		this.channelName = channel;
	}
	
	public Publisher(String host,String port,String channel){
		this.init(host, port);
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
			operateState = true;
			logger.info("Has Published Message to Channel: {}, Msg: {}",
					channelName, message);
		} else {
			logger.error("publisherJedis Error: Null Jedis Instance");
		}

		return operateState;
	}

	/**
	 * 
	 * @Title:redisInit
	 * @Description:redis初始化操作
	 * @param data
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月18日
	 */
	public abstract boolean redisInit(List<String> data);

	/**
	* @Title:updateWord_Add
	* @Description:
	* @param data
	* @return
	* @author:wuyg1
	* @date:2016年2月19日
	 */
	public abstract String updateWord_Add(List<String> data);
	
	/**
	 * 
	 * @Title:updateInfo_Add
	 * @Description:redis更新操作
	 * @param data
	 * @param state
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月18日
	 */
	public abstract String updateWord_Add(List<String> data, String state);

	/**
	 * 
	 * @Title:updateWord_Add
	 * @Description:redis更新操作
	 * @param data
	 * @param state
	 * @param time
	 *            更新的时间，调用者可以使用自己设置的时间
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月29日
	 */
	public abstract String updateWord_Add(List<String> data, String state,
			String time);

	/**
	 * 
	 * @Title:updateInfo_Add
	 * @Description:redis更新操作
	 * @param data
	 * @param state
	 * @param userDicList
	 *            ：需要添加的到userDic中的词语
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月18日
	 */
	public abstract String updateWord_Add(List<String> data, String state,
			ArrayList<String> userDicList);

	/**
	 * 
	 * @Title:updateWord_Add
	 * @Description:redis更新操作
	 * @param data
	 * @param state
	 * @param userDicList
	 * @param time
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月29日
	 */
	public abstract String updateWord_Add(List<String> data, String state,
			ArrayList<String> userDicList, String time);

	/**
	 * 
	 * @Title:updateInfo_Alter
	 * @Description: redis修改操作
	 * @param data
	 * @param state
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月18日
	 */
	public abstract String updateWord_Alter(List<String> data, String state);

	/**
	 * 
	 * @Title:updateWord_Alter
	 * @Description:redis修改操作
	 * @param data
	 * @param state
	 * @param userDicList
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月25日
	 */
	public abstract String updateWord_Alter(List<String> data, String state,
			ArrayList<String> userDicList);

	/**
	 * 
	 * @Title:updateInfo_Del
	 * @Description:redis删除操作
	 * @param data
	 * @param state
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月18日
	 */
	public abstract String updateWord_Del(List<String> data, String state);

	/**
	 * 
	 * @Title:updateWord_Del
	 * @Description: redis删除操作
	 * @param data
	 * @param state
	 * @param userDicList
	 *            :需要删除的外挂词
	 * @return
	 * @author:wuyg1
	 * @date:2016年1月5日
	 */
	public abstract String updateWord_Del(List<String> data, String state,
			ArrayList<String> userDicList);

	/**
	 * 
	 * @Title:updateWord_Alter_undo
	 * @Description:redis修改操作数据回滚
	 * @param data
	 * @param state
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月18日
	 */
	public abstract String updateWord_Alter_undo(List<String> data, String state);
}
