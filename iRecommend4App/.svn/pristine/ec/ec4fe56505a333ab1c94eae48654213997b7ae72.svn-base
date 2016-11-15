package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;

import redis.clients.jedis.Jedis;

/**
 * <PRE>
 * 作用 : 
 *  黑名单类，包括初始化、数据更新与黑名单判断 
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *  1. 黑名单数据多线程读写安全;
 *  2. 单例
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年12月8日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class BlackListData {
	private static Logger logger = LoggerFactory.getLogger(BlackListData.class);
	private String black_list_redis_key = "";
	
	/**
	 * @Fields black_list : CopyOnWriteArrayList 保证多线程读写安全
	 * 1. CopyOnWriteArrayList对读操作不加锁，对写加锁;
	 * 2. 写的时候可以同时读，但可以保证数据的最终一致性;
	 * 3. 初始化指定大小，避免多次扩容开销;
	 */
	private List<String> black_list = new CopyOnWriteArrayList<String>(new ArrayList<String>(2000));
	
	// 全局使用的Jedis实例
	private static Jedis jedis;
	
	private static String commonDataRedisHost;
	private static int commonDataRedisPort;
	
	private static class BlackListDataHolder {
		private static final BlackListData INSTANCE = new BlackListData();
	}

	private void init() {
		this.black_list_redis_key = LoadConfig.lookUpValueByKey("blacklistPattern");
		
		try {
			commonDataRedisPort = Integer.valueOf(LoadConfig.lookUpValueByKey("commonDataRedisPort"));
			commonDataRedisHost = LoadConfig.lookUpValueByKey("commonDataRedisHost");
			// 默认超时时间正常情况够了，不用设置
			setJedis(new Jedis(commonDataRedisHost, commonDataRedisPort));
			
		} catch (Exception e) {
			logger.error("Redis Init Error:", e);
			setJedis(null);
		}
		
		if (null != jedis) {
			try {
				jedis.select(1);
				List<String> redisData = jedis.lrange(this.black_list_redis_key, 0, -1);
				logger.info("Blacklist init redisData, get data size:" + redisData.size());
				for (String tempElem : redisData) {
					String[] tempElemSplit = tempElem.split("_");
					if (tempElemSplit[0].equals("article")) {
						// 添加id到黑名单
						this.black_list.add(tempElemSplit[1]);
						
						// 添加title到黑名单
						this.black_list.add(tempElemSplit[2]);
					} else if (tempElemSplit[0].equals("keyword")) {
						// 添加关键词到黑名单
						this.black_list.add(tempElemSplit[1]);
					}
				}
			} catch (Exception ex) {
				logger.error("BlackList Init Error:" + ex.getMessage());
			}
		}
	}
	
	private BlackListData() {
		init();
	}

	/**
	 * @Title: addElems2BlackList
	 * @Description: 往本地黑名单中增加数据
	 * @author liu_yi
	 * @param inputElemsList
	 * @throws
	 */
	public void addElems2BlackList(String inputElemsListStr) {
		if (null == inputElemsListStr || inputElemsListStr.isEmpty()) {
			logger.info("addElems2BlackList:Empty Input.");
			return;
		}
		
		String[] inputElemsList = inputElemsListStr.split("&");
		
		for (String tempElem : inputElemsList) {
			String[] tempElemSplit = tempElem.split("_");
			if (tempElemSplit[0].equals("article")) {
				// 添加id到黑名单
				this.black_list.add(tempElemSplit[1]);
				
				// 添加title到黑名单
				this.black_list.add(tempElemSplit[2]);
			} else if (tempElemSplit[0].equals("keyword")) {
				// 添加关键词到黑名单
				this.black_list.add(tempElemSplit[1]);
			}
		}
		
	}
	
	/**
	 * @Title: delElemsFromBlackList
	 * @Description: 从本地黑名单中删除数据
	 * @author liu_yi
	 * @param inputElemsListStr
	 * @throws
	 */
	public void delElemsFromBlackList(String inputElemsListStr) {
		if (null == inputElemsListStr || inputElemsListStr.isEmpty()) {
			logger.info("addElems2BlackList:Empty Input.");
			return;
		}
		
		String[] inputElemsList = inputElemsListStr.split(commonDataUpdateConfig.recordDelimiter);
		for (String tempElem : inputElemsList) {
			if(null == tempElem || tempElem.isEmpty()){
				continue;
			}
			String[] tempElemSplit = tempElem.split("_");
			if (tempElemSplit[0].equals("article")) {
				// 从黑名单中删除id
				this.black_list.remove(tempElemSplit[1]);
				
				// 从黑名单中删除title
				this.black_list.remove(tempElemSplit[2]);
			} else if (tempElemSplit[0].equals("keyword")) {
				// 从黑名单中删除关键词
				this.black_list.remove(tempElemSplit[1]);
			}
		}
	}
	
	/**
	 * @Title: isBlacklistElem
	 * @Description: 判断字符是否在黑名单中，可以是文章id，文章标题或者某个关键词
	 * @author liu_yi
	 * @param inputStr
	 * @return
	 * @throws
	 */
	public boolean isBlacklistElem(String inputStr) {
		if (null == inputStr || inputStr.isEmpty()) {
			return false;
		}
		
		if (this.black_list.contains(inputStr)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static final BlackListData getInstance() {
		return BlackListDataHolder.INSTANCE;
	}

	public List<String> get_blacklist() {
		return black_list;
	}

	public void set_blacklist(List<String> black_list) {
		this.black_list = black_list;
	}

	public static Jedis getJedis() {
		return jedis;
	}

	public static void setJedis(Jedis publisherJedis) {
		BlackListData.jedis = publisherJedis;
	}
}