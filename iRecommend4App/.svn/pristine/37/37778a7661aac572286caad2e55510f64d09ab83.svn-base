/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.preloadUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;

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
 *          1.0          2016年3月8日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class ToutiaoHotUtil {
	private static Log LOG = LogFactory.getLog("ToutiaoHotUtil");
	private static ToutiaoHotUtil instance = new ToutiaoHotUtil();
	private OSCache osCache ;
	private static final String CACHE_NAME = "OScache_ToutiaoCache";
	private HashMap<String, Long> newsExposureTimeMap ;
	
	private HashMap<String, Long> newsExposureTimeMap_test ;
	
	private ToutiaoHotUtil(){
		int refreshInterval = 24 * 60 * 60;
		LOG.info("refreshInterval = " + refreshInterval);
		osCache = new OSCache("conf/oscache_AlgorithmChannel.properties", CACHE_NAME,
				refreshInterval);
		LOG.info(CACHE_NAME + " has created");
		newsExposureTimeMap = new HashMap<String, Long>();
		
		newsExposureTimeMap_test = new HashMap<String, Long>();
		//加载落地缓存的新闻曝光时间cache
		try {
			String str = (String) osCache.get("ExposureTimeMapCache");
			if(str != null){
				Gson gson = new Gson();
				newsExposureTimeMap = gson.fromJson(str, new TypeToken<HashMap<String, Long>>() {
				}.getType());
				LOG.info("Loading ExposureTimeMapCache from redis ~ size : "+newsExposureTimeMap.size());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.warn("ExposureTimeMapCache is null");
		}
		
		//test_加载落地缓存的新闻曝光时间cache
		try {
			String str = (String) osCache.get("newsExposureTimeMap_test");
			if(str != null){
				Gson gson = new Gson();
				newsExposureTimeMap_test = gson.fromJson(str, new TypeToken<HashMap<String, Long>>() {
				}.getType());
				LOG.info("Loading ExposureTimeMapCache from redis ~ size : "+newsExposureTimeMap.size());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.warn("ExposureTimeMapCache is null");
		}
		
		
	}
	
	public static ToutiaoHotUtil getInstance(){
		return instance;
	}
	
	public List<PreloadItem> rankItemList(List<PreloadItem> itemList){
		LOG.info("rank start ~");
		if(itemList == null || itemList.isEmpty()){
			LOG.warn("rank item list is null ");
			return itemList;
		}
		
		LOG.info("newsExposureTimeMap size : "+newsExposureTimeMap.size());
		
		//用于存储当前在生命周期内的新闻曝光时间,后续替换更新当前的曝光时间cache
		HashMap<String, Long> tempNewsExHashMap = new HashMap<String, Long>();
		//计算曝光率hackernews，评论数hackernews，并加权计算最终rank得分
		//获取曝光数Map
		HashMap<String, Integer> exposureNumMap = getNewsExposureNum();
		//获取PVMap
		HashMap<String, Integer> PVNumMap = getNewsPVNum();
		
		//谨慎放开，ifengtoutiao按热度排在靠前的位置，放开垂直频道数据排在头条数据后面
		List<PreloadItem> toutiaoList = new ArrayList<PreloadItem>();
		List<PreloadItem> channelList = new ArrayList<PreloadItem>();
		List<PreloadItem> rankList = new ArrayList<PreloadItem>();
		
		for(PreloadItem item : itemList){
			StringBuffer logsb = new StringBuffer();
			//获取曝光数、PV数、曝光时长，计算点击率、点击率hackernews
			double exposureRate = 1; //曝光率
			String cmppId = item.getFitem().getDocId();
			String imcpId = item.getFitem().getId_value();
			
			//对娱乐的数据进行单独处理
			if(imcpId.contains("cmpp")){
				imcpId = imcpId.substring(0, 16);
			}
			Integer exposureNum = exposureNumMap.get(cmppId);
			if(exposureNum == null){
				exposureNum = 0;
			}
			
			Integer pvNum = PVNumMap.get(imcpId);
			if(pvNum == null){
				pvNum = 0;
			}
			
			//获取曝光时长
			Long exposureTime = newsExposureTimeMap.get(cmppId);
			if(exposureTime == null){
				exposureTime = System.currentTimeMillis();
				tempNewsExHashMap.put(cmppId, exposureTime);
			}else{
				tempNewsExHashMap.put(cmppId, exposureTime);
			}
			
			long lifeTime = System.currentTimeMillis() - exposureTime;
			
			//计算点击率
			if(exposureNum != 0){
				exposureRate = (double)pvNum/(double)exposureNum;
			}
			//将曝光率*100计算对应的hackernews得分
			int computNum = (int) (exposureRate*10000);
			double exposureRateHackerNewsScore = hackerNews(computNum, lifeTime);
			
			//计算评论数hackernews
			int commentNum = getNewsCommentsNum(imcpId);
			double commentHackerNewsScore = hackerNews(commentNum, lifeTime);
			
			double rankNum = 0.8*exposureRateHackerNewsScore + 0.2*commentHackerNewsScore;
			
			logsb.append("cmppid: ").append(cmppId);
			logsb.append(" imcpId: ").append(imcpId);
			logsb.append(" expourseNum: ").append(exposureNum);
			logsb.append(" pvNum: ").append(pvNum);
			logsb.append(" expourseRate: ").append(exposureRate);
			logsb.append(" lifetime: ").append(lifeTime);
			logsb.append(" exHacker: ").append(exposureRateHackerNewsScore);
			logsb.append(" comNum: ").append(commentNum);
			logsb.append(" comHacker: ").append(commentHackerNewsScore);
			logsb.append(" rankscore: ").append(rankNum);
			LOG.info(logsb.toString());
			
			item.setRankscore(rankNum);
			//test
			item.setHotscore(exposureRate);
			item.setPv(pvNum);
			
			//按ifengtoutiao以及其他垂直频道进行区分
			if(item.getFitem().getOthers().contains("ifengtoutiao")){
				toutiaoList.add(item);
			} else {
				channelList.add(item);
			}
			
		}
		
		//先排ifengtoutiao数据
		toutiaoList = sortItemListByRankScore(toutiaoList);
		//再排
		channelList = sortItemListByRankScore(channelList);
		
		rankList.addAll(toutiaoList);
		rankList.addAll(channelList);
		
		Gson gson = new Gson();
		//替换曝光时间Map 目的在于淘汰不在生命周期内的曝光时长缓存
		newsExposureTimeMap = tempNewsExHashMap;
		//本轮结束将曝光时长map保存到oscache中
		String saveStr = gson.toJson(newsExposureTimeMap);
		osCache.put("ExposureTimeMapCache", saveStr);
		
		//test
		String test = gson.toJson(rankList);
		LOG.info("toutiao : "+test);
		
		return rankList;
	}
	
	
	/*
	 * 按得分排序 
	 * 
	 */
	private List<PreloadItem> sortItemListByRankScore(List<PreloadItem> preloadList){
		if(preloadList == null || preloadList.isEmpty()){
			return preloadList;
		}
		//排序
		Collections.sort(preloadList, new Comparator<PreloadItem>() {

			@Override
			public int compare(PreloadItem o1, PreloadItem o2) {
				// TODO Auto-generated method stub
				if(o1.getRankscore() > o2.getRankscore()){
					return -1;
				}else if(o1.getRankscore() < o2.getRankscore()){
					return 1;
				} else {
					return 0;
				}
			}
		});
		return preloadList;
	}
	
	/**
	 * 获取新闻曝光数Map 并 更新新闻的第一次曝光时间
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private HashMap<String, Integer> getNewsExposureNum(){
		HashMap<String, Integer> ExposureMap = new HashMap<String, Integer>();
		try{
			Jedis jedis = new Jedis("10.32.21.62", 6379, 2000);
			jedis.select(12);
			
			Set<String> keySet = jedis.keys("recommendExposure*");
			if(keySet == null || keySet.isEmpty()){
				LOG.error("Update RecommendExposure error ~");
				return ExposureMap;
			}
			//遍历key，转换为integer格式，并更新第一次曝光时间
			for(String key : keySet){
				String value = jedis.get(key);
				Integer num = Integer.valueOf(value);
				String tempKey = key.replaceAll("recommendExposure:", "");
				ExposureMap.put(tempKey, num);
				//更新该条新闻第一次曝光时间
				Long time = newsExposureTimeMap.get(tempKey);
				if(time == null){
					newsExposureTimeMap.put(tempKey, System.currentTimeMillis());
				} 
			}
		} catch (Exception e){
			LOG.error("ExposureMap error : "+e);
		}
		
		LOG.info("ExposureMap size "+ExposureMap.size());
		return ExposureMap;
	}
	
	/**
	 * 获取新闻点击数
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private HashMap<String, Integer> getNewsPVNum(){
		HashMap<String, Integer> pvNumMap = new HashMap<String, Integer>();
		try{
			Jedis jedis = new Jedis("10.32.21.62", 6379, 2000);
			jedis.select(12);
			Set<String> keySet = jedis.keys("SYactionInfo*");
			if(keySet == null || keySet.isEmpty()){
				LOG.error("Update SYpvNumMap error ~");
				return pvNumMap;
			}

			for(String key : keySet){
				String value = jedis.get(key);
				if(value != null){
					Integer num = Integer.valueOf(value);
					String tempKey = key.replaceAll("SYactionInfo:", "");
					pvNumMap.put(tempKey, num);
				}
			}
		} catch(Exception e){
			LOG.error("pvNum error : "+e);
		}
		
		LOG.info("pvNumMap size "+pvNumMap.size());
		return pvNumMap;
	}
	
	
	/**
	 * 获取新闻评论数
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private int getNewsCommentsNum(String imcpId){
		String url = "http://comment.ifeng.com/get.php?job=14&doc_url=http://wap.ifeng.com/news.jsp?aid="+imcpId;
		String result = DownloadPageUtil.downloadPageByRetry(url, "UTF-8", 1);
		int commentNum = 0;
		if(result != null && result.indexOf("<join_count>")>0){
			String temp = result.substring(result.indexOf("<join_count>")+12, result.indexOf("</join_count>"));
			commentNum = Integer.valueOf(temp);
		}
		return commentNum;
	}
	
	
	private double hackerNews(int pv, double lifeTime) {
		double score = 0.0;
		if (pv == 0) {
			return score;
		}
		lifeTime = lifeTime / (1000 * 60 * 60);
		double G = 1.8;     //1.8;
		double under = Math.pow((lifeTime + 2), G);
		score = (pv - 1) / under;
		return score;
	}
	
	
	/*        AB_test1         */
	/*
	 * 利用明明预估的点击率进行计算 
	 * 
	 * 
	 */
	public List<PreloadItem> rankItemList_test(List<PreloadItem> itemList){
		LOG.info("rank_test start ~");
		if(itemList == null || itemList.isEmpty()){
			LOG.warn("rank item list is null ");
			return itemList;
		}
		
		LOG.info("newsExposureTimeMap size : "+newsExposureTimeMap_test.size());
		
		//用于存储当前在生命周期内的新闻曝光时间,后续替换更新当前的曝光时间cache
		HashMap<String, Long> tempNewsExHashMap = new HashMap<String, Long>();
		//计算曝光率hackernews，评论数hackernews，并加权计算最终rank得分
		//获取曝光率Map
		Map<String, Double> hitRateMap = getNewHitRate();
		//点击数曝光数Map
		Map<String, Map<String, String>> map = getMap_test();
		
		//谨慎放开，ifengtoutiao按热度排在靠前的位置，放开垂直频道数据排在头条数据后面
		List<PreloadItem> toutiaoList = new ArrayList<PreloadItem>();
		List<PreloadItem> channelList = new ArrayList<PreloadItem>();
		List<PreloadItem> rankList = new ArrayList<PreloadItem>();
		
		for(PreloadItem item : itemList){
			StringBuffer logsb = new StringBuffer();
			//获取曝光数、PV数、曝光时长，计算点击率、点击率hackernews
			double exposureRate = 1; //曝光率
			String cmppId = item.getFitem().getDocId();
			String imcpId = item.getFitem().getId_value();
			
			//对娱乐的数据进行单独处理
			if(imcpId.contains("cmpp_")){
				imcpId = imcpId.substring(0, 16);
			}
			
			Double tempRate = hitRateMap.get(imcpId);
			if(tempRate != null){
				exposureRate = tempRate;
			} else {
//				LOG.warn(imcpId+" "+cmppId+" not hit ~");
			}
			//获取曝光时长
			Long exposureTime = newsExposureTimeMap_test.get(cmppId);
			if(exposureTime == null){
				exposureTime = System.currentTimeMillis();
				tempNewsExHashMap.put(cmppId, exposureTime);
			}else{
				tempNewsExHashMap.put(cmppId, exposureTime);
			}
			
			long lifeTime = System.currentTimeMillis() - exposureTime;
			
			//将曝光率*100计算对应的hackernews得分
			int computNum = (int) (exposureRate*10000);
			double exposureRateHackerNewsScore = hackerNews(computNum, lifeTime);
			
			//计算评论数hackernews
			int commentNum = getNewsCommentsNum(imcpId);
			double commentHackerNewsScore = hackerNews(commentNum, lifeTime);
			
			double rankNum = 0.8*exposureRateHackerNewsScore + 0.2*commentHackerNewsScore;
			
			logsb.append("cmppid: ").append(cmppId);
			logsb.append(" imcpId: ").append(imcpId);
			logsb.append(" expourseRate: ").append(exposureRate);
			logsb.append(" lifetime: ").append(lifeTime);
			logsb.append(" exHacker: ").append(exposureRateHackerNewsScore);
			logsb.append(" comNum: ").append(commentNum);
			logsb.append(" comHacker: ").append(commentHackerNewsScore);
			logsb.append(" rankscore: ").append(rankNum);
			LOG.info("Test_ "+logsb.toString());
			
			item.setRankscore(rankNum);
			item.setHotscore(exposureRate);
			item.setPv(0);
			//test
			Map<String, String> tempMap = map.get(imcpId);
			if(tempMap != null){
				String hitNumStr = tempMap.get("cn");
				if(hitNumStr != null){
					item.setPv(Double.valueOf(hitNumStr).intValue()); 
				}
			}

			
			
			//按ifengtoutiao以及其他垂直频道进行区分
			if(item.getFitem().getOthers().contains("ifengtoutiao")){
				toutiaoList.add(item);
			} else {
				channelList.add(item);
			}
			
		}
		
		//先排ifengtoutiao数据
		toutiaoList = sortItemListByRankScore(toutiaoList);
		//再排
		channelList = sortItemListByRankScore(channelList);
		
		rankList.addAll(toutiaoList);
		rankList.addAll(channelList);
		
		Gson gson = new Gson();
		//替换曝光时间Map 目的在于淘汰不在生命周期内的曝光时长缓存
		newsExposureTimeMap_test = tempNewsExHashMap;
		//本轮结束将曝光时长map保存到oscache中
		String saveStr = gson.toJson(newsExposureTimeMap_test);
		osCache.put("newsExposureTimeMap_test", saveStr);
		
		//test
		String test = gson.toJson(rankList);
//		LOG.info("test_toutiao : "+test);
		
		return rankList;
	}
	/*
	 * 从明明那获取预估点击率进行计算推荐率
	 * 
	 */
	private Map<String, Double> getNewHitRate(){
		Map<String, Double> hitRateMap = new HashMap<String, Double>();
		try{
			Jedis jedis = new Jedis("10.50.8.71", 6379, 1000);
			jedis.select(1);
			Set<String> keySet = jedis.keys("*");
			if(keySet == null || keySet.isEmpty()){
				LOG.error("Update RecommendExposure error ~");
				return hitRateMap;
			}
			for(String key : keySet){
				Map<String, String> tempMap = jedis.hgetAll(key);
				String exNumStr = tempMap.get("sum");
				String hitNumStr = tempMap.get("cn");
				double rate = 1.0;
				int exNum = 0;
				double hitNum = 0;
				if(exNumStr != null){
					exNum = Integer.valueOf(exNumStr);
				}
				if(hitNumStr != null){
					hitNum = Double.valueOf(hitNumStr);
				}
				if(exNum != 0){
					rate = (double)hitNum/(double)exNum;
				}
				hitRateMap.put(key, rate);
			}
		} catch(Exception e){
			LOG.error("get hit rate error :  ", e);
		}
		
		return hitRateMap;
	}
	
	/*
	 * 从明明那获取点击数和曝光数
	 * 
	 */
	private Map<String, Map<String, String>> getMap_test(){
		Map<String, Map<String, String>> map = new HashMap<String, Map<String,String>>();
		try{
			Jedis jedis = new Jedis("10.50.8.71", 6379, 1000);
			jedis.select(1);
			Set<String> keySet = jedis.keys("*");
			if(keySet == null || keySet.isEmpty()){
				LOG.error("Update RecommendExposure error ~");
				return map;
			}
			for(String key : keySet){
				Map<String, String> tempMap = jedis.hgetAll(key);
				map.put(key, tempMap);
			}
		} catch(Exception e){
			LOG.error("get hit rate error :  ", e);
		}
		return map;
	}
	
	public static void main(String[] args){
		ToutiaoHotUtil in = ToutiaoHotUtil.getInstance();
//		long ls = System.currentTimeMillis();
//		
////		
//////		in.getNewsCommentsNum("107036206");
//		HashMap<String, Integer> temp = in.getNewsExposureNum();
//		
//		ls = System.currentTimeMillis() - ls;
//		System.out.println(ls);
		Gson gson = new Gson();
//		String out = gson.toJson(temp);
//		System.out.println(out);
//		String out1 = gson.toJson(newsExposureTimeMap);
//		System.out.println(out1);
		
		Map<String, Double> rate = in.getNewHitRate();
		String out = gson.toJson(rate);
		System.out.println(out);
		
	}
	
	
	
}
