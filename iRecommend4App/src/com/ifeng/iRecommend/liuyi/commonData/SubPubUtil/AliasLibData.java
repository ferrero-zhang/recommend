package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;

public class AliasLibData extends LocalDataUpdate {
	private ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>> AliasLibDataMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>>();
	private static AliasLibData INSTANCE = null;

	private ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>> getAliasLibDataMap() {
		return AliasLibDataMap;
	}

	@SuppressWarnings("unused")
	private void setAliasLibDataMap(
			ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>> aliasLibDataMap) {
		AliasLibDataMap = aliasLibDataMap;
	}

	public AliasLibData() {
		// TODO Auto-generated constructor stub
		init();
	}
	/**
	 * 
	* @Title:searchAlias
	* @Description: 查询指定query在各个领域的主名
	* @param query 待查询字段
	* @return
	* @author:wuyg1
	* @date:2016年6月27日
	 */
	public ConcurrentHashMap<String, HashSet<String>>  searchAlias(String query){
		
		if(null == query){
			return null;
		}
		query = query.trim();
		if(query.isEmpty()){
			return null;
		}
		
		query = query.toLowerCase();
		
	    return	this.getAliasLibDataMap().get(query);
	}
	/**
	 * 
	* @Title:isContaince
	* @Description:判断在主别名库中是否包含指定的query
	* @param query  待查询字段
	* @return
	* @author:wuyg1
	* @date:2016年6月27日
	 */
	public boolean isContaince(String query){

		if(null == query){
			return false;
		}
		query = query.trim();
		if(query.isEmpty()){
			return false;
		}
		
		query = query.toLowerCase();
		
		return this.getAliasLibDataMap().containsKey(query);
	}
	

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		try {
			setRedis_key(LoadConfig.lookUpValueByKey("aliasDataPattern"));
			commonDataRedisPort = Integer.valueOf(LoadConfig
					.lookUpValueByKey("aliasDataRedisPort"));
			commonDataRedisHost = LoadConfig
					.lookUpValueByKey("aliasDataRedisHost");
			// 默认超时时间正常情况够了，不用设置
			setJedis(new Jedis(commonDataRedisHost, commonDataRedisPort));

		} catch (Exception e) {
			logger.error("Redis Init Error:", e);
			setJedis(null);
		}

		if (null != jedis) {
			try {
				jedis.select(Integer.valueOf(LoadConfig
						.lookUpValueByKey("commonDataDbNum")));
				List<String> redisData = jedis.lrange(this.redis_key, 0, -1);
				logger.info("init redisData, get data size:" + redisData.size());
				for (String tempElem : redisData) {
					if (null == tempElem || tempElem.isEmpty()) {
						continue;
					}

					String[] tempElemSplit = tempElem.split(LoadConfig
							.lookUpValueByKey("keySegment"));
					
					tempElemSplit[0] = tempElemSplit[0].toLowerCase();

					ConcurrentHashMap<String, HashSet<String>> cateHashMap = recordParser(tempElem);
					
					hostNameTransformAliases(cateHashMap,AliasLibDataMap,tempElemSplit[0]);
//					

				}
			} catch (Exception ex) {
				logger.error("HotWordMap Init Error:" + ex.getMessage());
			}
		}
	}

	@Override
	protected void addElem2Local(String add_content) {
		// TODO Auto-generated method stub
		if (null == add_content || add_content.isEmpty()) {
			logger.info("addElems2AliasLibDataMap:Empty Input.");
			return;
		}

		String[] inputElemsList = add_content
				.split(commonDataUpdateConfig.recordDelimiter);

		for (String tempElem : inputElemsList) {
			if (null == tempElem || tempElem.isEmpty()) {
				continue;
			}
			System.err.println(tempElem);
			String[] tempElemSplit = tempElem.split(LoadConfig
					.lookUpValueByKey("keySegment"));
			
			tempElemSplit[0] = tempElemSplit[0].toLowerCase();

			ConcurrentHashMap<String, HashSet<String>> add_cateHashMap = recordParser(tempElem);

			hostNameTransformAliases(add_cateHashMap,AliasLibDataMap,tempElemSplit[0]);
		}
	}

	@Override
	protected void delElemFromLocal(String del_content) {

		if (null == del_content || del_content.isEmpty()) {
			logger.info("delElemsFromAliasLibData:Empty Input.");
			return;
		}

		String[] inputElemsList = del_content
				.split(commonDataUpdateConfig.recordDelimiter);
		for (String tempElem : inputElemsList) {
			if (null == tempElem || tempElem.isEmpty()) {
				continue;
			}
			
			String keySegment = LoadConfig.lookUpValueByKey("keySegment");
			String keyWord = tempElem.split(keySegment)[0];
			
			keyWord = keyWord.toLowerCase();
			
			ConcurrentHashMap<String, HashSet<String>> del_cateHashMap = recordParser(tempElem);
			
			delOsTransformAliases(del_cateHashMap, AliasLibDataMap,keyWord);
		}
	}

	@Override
	protected void alterElemInLocal(String alter_content) {
		// TODO Auto-generated method stub
		if (null == alter_content || alter_content.isEmpty()) {
			logger.info("alterElems2AliasLibData:Empty Input.");
			return;
		}

		String[] inputElemsList = alter_content
				.split(commonDataUpdateConfig.recordDelimiter);

		for (String tempElem : inputElemsList) {
			if (null == tempElem || tempElem.isEmpty()) {
				continue;
			}
			System.err.println(tempElem);
			String[] tempElemSplit = tempElem.split(LoadConfig
					.lookUpValueByKey("keySegment"));   // keySegment  #KEY#

			tempElemSplit[0] = tempElemSplit[0].toLowerCase();
			
			ConcurrentHashMap<String, HashSet<String>> add_cateHashMap = recordParser(tempElem);

			hostNameTransformAliases(add_cateHashMap,AliasLibDataMap,tempElemSplit[0]);
		}
	}

	// 西班牙人#KEY#体育#ALIAS#[皇家西班牙人足球俱乐部, 西班牙人俱乐部, 西班牙人足球俱乐部]#CAT#
	/**
	 * 
	 * @Title:recordParser
	 * @Description:对一个主别名记录进行解析
	 * @param record
	 * @return
	 * @author:wuyg1
	 * @date:2016年2月22日
	 */
	protected ConcurrentHashMap<String, HashSet<String>> recordParser(
			String record) {
		/**
		 * keySegment=#KEY# cateSegment=#CAT# aliasSegment=#ALIAS#
		 */
		ConcurrentHashMap<String, HashSet<String>> cateHashMap = new ConcurrentHashMap<String, HashSet<String>>();

		String[] tempElemSplit = record.split(LoadConfig
				.lookUpValueByKey("keySegment"));
		String[] cateElemSplit = tempElemSplit[1].split(LoadConfig
				.lookUpValueByKey("cateSegment"));
		for (String cateCon : cateElemSplit) {
			String[] alias = cateCon.split(LoadConfig
					.lookUpValueByKey("aliasSegment"));

			alias[1] = alias[1].substring(alias[1].indexOf("[") + "[".length(),
					alias[1].indexOf("]"));

			HashSet<String> set = new HashSet<String>();
			
			for(String nickname : Arrays.asList(alias[1].split(", "))){
				set.add(nickname.toLowerCase());
			}
			cateHashMap.put(alias[0], set);
		}
		return cateHashMap;

	}

	public static AliasLibData getInstance() {
		if (null == AliasLibData.INSTANCE) {
			AliasLibData.INSTANCE = new AliasLibData();
		}
		return AliasLibData.INSTANCE;
	}
	/**
	* @Title:delOsTransformAliases
	* @Description: 将需要删除的不合适别名进行删除，在删除的时候，需要删除本地缓存与该词相关的所有key-value键值对
	* @param cateHashMap
	* @param AliasLibDataMap
	* @param tempstr
	* @author:wuyg1
	* @date:2016年5月12日
	 */
	protected void delOsTransformAliases(ConcurrentHashMap<String,HashSet<String>> cateHashMap,
			ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>> AliasLibDataMap,String key){
			
			for(String category : cateHashMap.keySet()){
			
				HashSet<String> cateset = cateHashMap.get(category);
				for(String cateAlias : cateset){
					if(AliasLibDataMap.containsKey(cateAlias) && AliasLibDataMap.get(cateAlias).containsKey(category)){

					  HashSet<String> nickSet = AliasLibDataMap.get(cateAlias).get(category);
					  
					  nickSet.remove(key);
					  
					  if(null == nickSet || nickSet.isEmpty() || nickSet.size() == 0){
						  AliasLibDataMap.get(cateAlias).remove(category);
					  }
					   
					}
					
					if(null == AliasLibDataMap.get(cateAlias) || AliasLibDataMap.get(cateAlias).isEmpty() || AliasLibDataMap.get(key).size() == 0){
						AliasLibDataMap.remove(cateAlias);
					}
				}
				
			}
		}
	
	
	
	protected void hostNameTransformAliases(ConcurrentHashMap<String,HashSet<String>> cateHashMap,
		ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>> AliasLibDataMap,String tempstr){
		ConcurrentHashMap<String,HashSet<String>> cateMapNew = null;
		HashSet<String> cateSetNew = null;
		
		for(String category : cateHashMap.keySet()){
			// 1. 主名tempstr ----> 类别category -----> 主名tempstr
			if(!AliasLibDataMap.containsKey(tempstr)){
				cateMapNew = new ConcurrentHashMap<String,HashSet<String>>();
				cateSetNew = new HashSet<String>();
				cateSetNew.add(tempstr);
				cateMapNew.put(category, cateSetNew);
				AliasLibDataMap.put(tempstr, cateMapNew);
			}else{
				if(AliasLibDataMap.get(tempstr).containsKey(category)){
					AliasLibDataMap.get(tempstr).get(category).add(tempstr);
				}else{
					cateSetNew = new HashSet<String>();
					cateSetNew.add(tempstr);
					AliasLibDataMap.get(tempstr).put(category, cateSetNew);
				}
			}
			
		
			// 2.别名cateAlias ---->类别category ----> 主名tempstr
			HashSet<String> cateset = cateHashMap.get(category);
			for(String cateAlias : cateset){
				if(AliasLibDataMap.containsKey(cateAlias) && AliasLibDataMap.get(cateAlias).containsKey(category)){
					// 2.1.相同别名，相同类别，将不同主名合并在hashset中
					AliasLibDataMap.get(cateAlias).get(category).add(tempstr);
				}else if(AliasLibDataMap.containsKey(cateAlias) && !AliasLibDataMap.get(cateAlias).containsKey(category)){
					// 2.2.相同别名，不同类别
					cateSetNew = new HashSet<String>();
					cateSetNew.add(tempstr);
					AliasLibDataMap.get(cateAlias).put(category, cateSetNew);
				}else{
					cateMapNew = new ConcurrentHashMap<String,HashSet<String>>();
					cateSetNew = new HashSet<String>();
					cateSetNew.add(tempstr);
					cateMapNew.put(category, cateSetNew);
					AliasLibDataMap.put(cateAlias, cateMapNew);
				}
			}
		}
	}
	
}
