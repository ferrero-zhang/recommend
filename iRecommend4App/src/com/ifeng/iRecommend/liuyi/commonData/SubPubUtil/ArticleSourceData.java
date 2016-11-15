package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
/**
 * 
 * <PRE>
 * 作用 : 
 *   稿源信息的订阅相关操作方法
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
 *          1.0          2015年12月30日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class ArticleSourceData extends LocalDataUpdate {
	private static Log logger = LogFactory.getLog(ArticleSourceData.class);
	private ConcurrentHashMap<String, String> articleSourceMap = new ConcurrentHashMap<String, String>();
	private static ArticleSourceData INSTANCE = null;
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		try {
			setRedis_key(LoadConfig.lookUpValueByKey("articleSourcePattern"));
			commonDataRedisPort = Integer.valueOf(LoadConfig
					.lookUpValueByKey("commonDataRedisPort"));
			commonDataRedisHost = LoadConfig
					.lookUpValueByKey("commonDataRedisHost");
			// 默认超时时间正常情况够了，不用设置
			setJedis(new Jedis(commonDataRedisHost, commonDataRedisPort));

		} catch (Exception e) {
			logger.error("Redis Init Error:", e);
			setJedis(null);
		}

		if (null != jedis) {
			try {
				jedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("commonDataDbNum")));
				List<String> redisData = jedis.lrange(this.redis_key,
						0, -1);
				logger.info("init redisData, get data size:"
						+ redisData.size());
				
				for (String tempElem : redisData) {
					if(null == tempElem || tempElem.isEmpty()){
						continue;
					}
					String[] tempElemSplit = tempElem.split(LoadConfig.lookUpValueByKey("fieldDelimiter"));
					if (tempElemSplit[0].equals(this.redis_key)) {

						articleSourceMap.put(tempElemSplit[2],tempElemSplit[1]);

					}
				}
			} catch (Exception ex) {
				logger.error("ArticleSource Init Error:" + ex.getMessage());
			}
		}
	}

	@Override
	protected void addElem2Local(String add_content) {
		// TODO Auto-generated method stub
		if (null == add_content || add_content.isEmpty()) {
			logger.info("addElems2ArticleSourceMap:Empty Input.");
			return;
		}

		String[] inputElemsList = add_content.split(commonDataUpdateConfig.recordDelimiter);

		for (String tempElem : inputElemsList) {
			if(null == tempElem || tempElem.isEmpty()){
				continue;
			}
			String[] tempElemSplit = tempElem.split(LoadConfig.lookUpValueByKey("fieldDelimiter"));
			if (tempElemSplit[0].equals(this.redis_key)) {
				articleSourceMap.put(tempElemSplit[2],tempElemSplit[1]);
			}
		}
	}

	@Override
	protected void delElemFromLocal(String del_content) {
		// TODO Auto-generated method stub
		if (null == del_content || del_content.isEmpty()) {
			logger.info("delElemsFromHashMap:Empty Input.");
			return;
		}

		String[] inputElemsList = del_content.split(LoadConfig.lookUpValueByKey("recordDelimiter"));
		for (String tempElem : inputElemsList) {
			String[] tempElemSplit = tempElem.split(LoadConfig.lookUpValueByKey("fieldDelimiter"));
			if (tempElemSplit[0].equals(this.redis_key)) {
				articleSourceMap.remove(tempElemSplit[2]);
			}
		}
	}

	private ArticleSourceData(){
		init();
	}
	
	public static ArticleSourceData getInstance(){
		if(null == ArticleSourceData.INSTANCE){
			INSTANCE = new ArticleSourceData();
		}
		return ArticleSourceData.INSTANCE;
	}
	

	public ConcurrentHashMap<String, String> getArticleSourceMap() {
		return articleSourceMap;
	}

	public void setArticleSourceMap(
			ConcurrentHashMap<String, String> articleSourceMap) {
		this.articleSourceMap = articleSourceMap;
	}

	@Override
	protected void alterElemInLocal(String alter_content) {
		// TODO Auto-generated method stub

	}
	
	public static void main(String [] args){
		String str = ArticleSourceData.getInstance().articleSourceMap.get("观察者网");
		System.err.println(str);
	
		str = ArticleSourceData.getInstance().articleSourceMap.get("乌苏零距离");
		System.err.println(str);
	}

}
