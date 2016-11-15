/**
 * 
 */
package com.ifeng.iRecommend.likun.userCenter.tnappuc.utils;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.likun.hotpredict.heatPredict;
import com.ifeng.ikvlite.IkvLiteClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

/**
 * <PRE>
 * 作用 : 
 *   item的特征表达接口类；输入一个ID \ url \ title \ content等信息，返回对应的特征表达数据；
 *   需要提供以下数据的返回接口，包括：
 *   1）item的基本信息：ID、title 、url、分词后title、分词后content信息
 *   2）item的特征表达信息：一级分类、二级分类、栏目或者专题事件、topic实体（目前等价于keywords实体）、稿源（足够优质的来源才会做出表达）；
 *   3）特征的表达信息（可以暂无）：特征的热度+聚合度+一级分类向量分布+协同相关特征等；如果是来源、栏目、专题等特征，应该给予优质与否的评价；
 * 使用 : 
 *   用于新闻客户端的item xml解析后表达查询
 * 示例 :
 *   
 * 注意 :
 * 	  feature的类型:"c" "sc" "cn" "t" "s",分布对应：一级分类、二级分类、栏目专题事件、topic、稿源source
 * 
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015-4-8        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class XMLQueryInterface {

	private static final Logger LOG = Logger.getLogger(XMLQueryInterface.class);
	


	/**
	 * 把全量feature表达存储在redis中"10.32.24.222", 6379
	 * 
	 * @param key
	 * @return
	 */
	public static ArrayList<String> queryExFeature(String key) {
		if(key.length() == 15){
			key = "cmpp_" + key;
		}
		Jedis jedis = new Jedis("10.32.24.222", 6378);
//		jedis.
		try {
			jedis.select(1);
		} catch (Exception e) {
			LOG.error("[ERROR]Get redis db error.", e);
			jedis.disconnect();
			return null;
		}
		String value = null;
		try {
			value = jedis.get(key);
		} catch (Exception e) {
			LOG.error("[ERROR]Get  value from redis error.", e);
			jedis.disconnect();
			return null;
		}
		
		jedis.disconnect();
		
		ArrayList<String> ex_feature = null;
		try {
			ex_feature = JsonUtils.fromJson(value, ArrayList.class);
			if (ex_feature == null || ex_feature.isEmpty()) {
				itemf item = ItemIKVUtil.queryItemF(key,"d");
				if(item != null){
					ex_feature = item.getFeatures();
					//System.out.println(item.getID()+item.getTitle());
				}
			}
		} catch (Exception e) {
			LOG.error("[ERROR]Get feature error.", e);
			jedis.disconnect();
			return null;
		}
		
		return ex_feature;
	}
}
