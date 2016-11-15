/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.newHotPredict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.SolrUtil.SearchItemsFromSolr;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;

import redis.clients.jedis.Jedis;

/**
 * <PRE>
 * 作用 : 热点数据，以及要闻区排重区域，投放入口程序
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
 *          1.0          2015年7月15日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */


public class HotItemDis {
	private static Log LOG = LogFactory.getLog("HotItemDis");
	/**
	 * 将头条热点新闻写入redis，
	 * 
	 * 注：使用该函数前请主动更新HotItemLoadingUtil
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void disToutiaoHotNews(){
		List<HotRankItem> toutiaoHotList = HotNewsPredict.toutiaoHotNewsPredict();
//		HashMap<String, List<HotRankItem>> toutiaoHotMap = HotNewsPredict.SplitSportsHotListBySlide(toutiaoHotList);
//		HashMap<String, List<FrontNewsItem>> disMap = new HashMap<String, List<FrontNewsItem>>();
		List<FrontNewsItem> disList = new ArrayList<FrontNewsItem>();
		Gson gson = new Gson();
		//debug
		List<debugclass> debuglist = new ArrayList<debugclass>();
		//放入缓存中为上拉热度提供数据
		List<CacheHotRankItem> cachelist = new ArrayList<CacheHotRankItem>();
		
		if(toutiaoHotList == null || toutiaoHotList.isEmpty()){
			LOG.error("get toutiaoHotList error ~");
			LOG.warn("dis toutiaoHotNews faild");
			return;
		}
		//将热点新闻转换成前端数据结构


		
		for(HotRankItem hot : toutiaoHotList){
//			FrontNewsItem fitem = new FrontNewsItem(hot);
//			frontList.add(fitem);
			//改为将solr中查询的item2app传给前端
			String json = SearchItemsFromSolr.searchItem2appJsonById(hot.getDocId());
			try{
				if(json != null){
					FrontNewsItem fitem = gson.fromJson(json, FrontNewsItem.class);
					disList.add(fitem);
					//debug
					debugclass ditem = new debugclass(hot);
					debuglist.add(ditem);
					
					//cacheItem
					CacheHotRankItem cacheItem = new CacheHotRankItem(hot);
					cachelist.add(cacheItem);
				}else{
					LOG.warn("turn to FrontNewsItem error "+hot.getDocId());
				}
			} catch (Exception e){
				LOG.error("turn to FrontNewsItem error ",e);
				continue;
			}
		
	
		}

		String disStr = gson.toJson(disList);
		disToRedis("hot_items_toutiao", 3600 * 8, disStr);
		
		//test
		LOG.info("toutiao test result : "+disStr);
		//debug
		String debugstr = gson.toJson(debuglist);
		LOG.info("toutiao debug test : "+debugstr);
		
		LOG.info("hotlist size : "+disList.size());
		
		//CacheItem投放至Redis中缓存，为上拉热度缓存提供数据
//		String cacheStr = gson.toJson(cachelist);
//		disToRedis("toutiao-1", 3600 * 8, cacheStr);
	}
	
	
	
	/**
	 * 将体育热点新闻写入redis，
	 * 
	 * 注：使用该函数前请主动更新HotItemLoadingUtil
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void disSportsHotNews(){
		List<HotRankItem> sportsHotList = HotNewsPredict.SportsHotNewsPredict();
		HashMap<String, List<HotRankItem>> sportshotMap = HotNewsPredict.SplitSportsHotListBySlide(sportsHotList);
		HashMap<String, List<FrontNewsItem>> disMap = new HashMap<String, List<FrontNewsItem>>();
		Gson gson = new Gson();
		//debug
		HashMap<String, List<debugclass>> debugMap = new HashMap<String, List<debugclass>>();
		//cacheHotItem
		HashMap<String, List<CacheHotRankItem>> cacheMap = new HashMap<String, List<CacheHotRankItem>>();
		
		if(sportshotMap == null || sportshotMap.isEmpty()){
			LOG.error("get sportsHotList error ~");
			LOG.warn("dis sportsHotNews faild");
			return;
		}
		//将热点新闻转换成前端数据结构
		Set<Entry<String, List<HotRankItem>>> entrySet = sportshotMap.entrySet();
		for(Entry<String, List<HotRankItem>> entry : entrySet){
			List<HotRankItem> ranklist = entry.getValue();
			List<FrontNewsItem> frontList = new ArrayList<FrontNewsItem>();
			
			//debug
			List<debugclass> debuglist = new ArrayList<debugclass>();
			//放入缓存中为上拉热度提供数据
			List<CacheHotRankItem> cachelist = new ArrayList<CacheHotRankItem>();
			
			for(HotRankItem hot : ranklist){
//				FrontNewsItem fitem = new FrontNewsItem(hot);
//				frontList.add(fitem);
//				
//				//debug
//				debugclass ditem = new debugclass(hot);
//				debuglist.add(ditem);
				
				String json = SearchItemsFromSolr.searchItem2appJsonById(hot.getDocId());
				try{
					if(json != null){
						FrontNewsItem fitem = gson.fromJson(json, FrontNewsItem.class);
						frontList.add(fitem);
						//debug
						debugclass ditem = new debugclass(hot);
						debuglist.add(ditem);
						//cacheItem
						CacheHotRankItem cacheItem = new CacheHotRankItem(hot);
						cachelist.add(cacheItem);
					}else{
						LOG.warn("turn to FrontNewsItem error "+hot.getDocId());
					}
				} catch (Exception e){
					LOG.error("turn to FrontNewsItem error ",e);
					continue;
				}
				
				
			}
			disMap.put(entry.getKey(), frontList);
			//debug
			debugMap.put(entry.getKey(), debuglist);
			//cacheItem
			cacheMap.put(entry.getKey(), cachelist);
		}
		//投放到Redis中为前端下拉逻辑提供数据
		String disStr = gson.toJson(disMap);
		disToRedis("hot_items_sports", 3600 * 8, disStr);
		
		//CacheItem投放至Redis中缓存，为上拉热度缓存提供数据
		String cacheStr = gson.toJson(cacheMap);
		disToRedis("hot_cacheItems_sports", 3600 * 8, cacheStr);
		
		//test
		LOG.info("sportshot test result : "+disStr);
		//debug
		String debugstr = gson.toJson(debugMap);
		LOG.info(" debug test : "+debugstr);
	}
	
	
	/**
	 * 将首页要闻区写入redis，供前端pc推荐与要闻区排重使用
	 * 
	 * 注：使用该函数前请主动更新HotItemLoadingUtil
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void disIfengPCYaowenArea(){
		HotItemLoadingUtil util = HotItemLoadingUtil.getInstance();
//		util.updateIfengMainPageHotList();
		List<HotRankItem> list = util.getHotRankList(HotItemLoadingUtil.IfengMainpageYaowen);
		if(list == null || list.isEmpty()){
			LOG.error("get Yaowen Area error ~");
			LOG.warn("dis yaowenlist faild ~");
			return;
		}
		List<String> yaowentitle = new ArrayList<String>();
		for(HotRankItem hot : list){
			yaowentitle.add(hot.getTitle());
		}
		Gson gson = new Gson();
		String yaowen = gson.toJson(yaowentitle);
		disToRedis("yaowen_title", 60*60, yaowen);
		
		//test
		LOG.info("yaowen test result : "+yaowen);
	}
	/**
	 * 将白名单中类别预加载热度信息存入redis
	 * 
	 * 注：使用该函数前请主动更新HotItemLoadingUtil
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void disWhitelistHotNews(){
		HashMap<String, List<HotRankItem>> tempHotMap = HotNewsPredict.whiteListPreloadHotpredict();
		if(tempHotMap == null || tempHotMap.isEmpty()){
			LOG.warn("get preload map is null ");
			return;
		}
		Gson gson = new Gson();
		Set<String> keySet = tempHotMap.keySet();
		for(String key : keySet){
			List<HotRankItem> templist = tempHotMap.get(key);
			List<CacheHotRankItem> cachelist = new ArrayList<CacheHotRankItem>();
			for(HotRankItem hot : templist){
				CacheHotRankItem cacheItem = new CacheHotRankItem(hot);
				cachelist.add(cacheItem);
			}
			String json = gson.toJson(cachelist);
			String keyname = key+"-1";
			disToRedis(keyname, 60*60, json);
			LOG.info("dis "+keyname+" success ");
		}
	}
	
	/**
	 * 优质数据内容存入redis
	 * 
	 * 注：使用该函数前请主动更新HotItemLoadingUtil
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void disHighQualityItems(){
		List<HotRankItem> tempList = HotNewsPredict.highQualityNewsPreloaded();
		if(tempList == null || tempList.isEmpty()){
			LOG.warn("get HighQualityItemList is null ~");
			return ;
		}
		List<FrontNewsItem> disList = new ArrayList<FrontNewsItem>();
		Gson gson = new Gson();
		for(HotRankItem hot : tempList){
			FrontNewsItem fitem = hot.getFrontItem();
			if(fitem != null){
				disList.add(fitem);
			}else{
				String json = SearchItemsFromSolr.searchItem2appJsonById(hot.getDocId());
				try{
					if(json != null){
						fitem = gson.fromJson(json, FrontNewsItem.class);
						disList.add(fitem);
					}else{
						LOG.warn("turn to FrontNewsItem error "+hot.getDocId());
					}
				} catch (Exception e){
					LOG.error("turn to FrontNewsItem error ",e);
					continue;
				}
			}
		}
		String disStr = gson.toJson(disList);
		disToRedis("highquality_items", 3600 * 8, disStr);
	}
	
	/**
	 * 本地数据内容存入redis
	 * 
	 * 注：使用该函数前请主动更新HotItemLoadingUtil
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void disLocalNews(){
		HashMap<String, List<HotRankItem>> localMap = HotNewsPredict.localNewsHeatPredict();
		if(localMap == null || localMap.isEmpty()){
			LOG.warn("Get LocalNewsMap from hotnewspredict is null ~");
			return;
		}
		Gson gson = new Gson();
		Set<String> keySet = localMap.keySet();
		for(String key : keySet){
			
			List<HotRankItem> tempList = localMap.get(key);
			if(tempList == null || tempList.isEmpty()){
				LOG.warn("This "+key+" local news is null ~");
				continue;
			}
			List<FrontNewsItem> disList = new ArrayList<FrontNewsItem>();
			for(HotRankItem hot : tempList){
				FrontNewsItem fitem = hot.getFrontItem();
				if(fitem != null){
					disList.add(fitem);
				}else{
					String json = SearchItemsFromSolr.searchItem2appJsonById(hot.getDocId());
					try{
						if(json != null){
							fitem = gson.fromJson(json, FrontNewsItem.class);
							disList.add(fitem);
						}else{
							LOG.warn("turn to FrontNewsItem error "+hot.getDocId());
						}
					} catch (Exception e){
						LOG.error("turn to FrontNewsItem error ",e);
						continue;
					}
				}
			}
			String disStr = gson.toJson(disList);
			disToRedis(key, 3600 * 24, disStr);
		}
	}
	
	
	
	/**
	 * 投放至redis函数
	 * 
	 * 
	 * 
	 * @param String tableName ： table名称
	 * 		  long validTime : 存活时间
	 * 		  String disStr ： 需要投放的字符串
	 * 
	 * @return 
	 */
	private void disToRedis(String tableName,long validTime,String disStr){
		if(disStr == null){
			return ;
		}
		try{
			Jedis jedis = new Jedis("10.32.21.62", 6379, 10000);
			jedis.select(11);
			String status = jedis.setex(tableName, (int) validTime, disStr);
			if(!status.equals("OK")){
				LOG.error("set status code:"+status);
			}else{
				LOG.info("Dis "+tableName+" to redis");
			}
		}catch(Exception e){
			LOG.error("ERROR"+e);
		}
	}
	
	/**
	 * 投放主函数
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void hotItemDis(){
		//统一更新加载热度信息数据
		HotItemLoadingUtil util = HotItemLoadingUtil.getInstance();
		util.updateAll();
		
		//头条热点新闻投放
		disToutiaoHotNews();
		//体育热点新闻投放
//		disSportsHotNews();
		//要闻区新闻内容投放
		disIfengPCYaowenArea();
		//其他白名单预加载热点新闻投放
//		disWhitelistHotNews();
		//优质数据投放至redis
//		disHighQualityItems();
		//本地数据投放至redis
//		disLocalNews();
	}
	
	public static void main(String[] args){
		HotItemDis dis = new HotItemDis();
		while(true){
			dis.hotItemDis();
			LOG.info("Sleep 10 minutes");
			try {
				Thread.sleep(10*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}

//debug
class debugclass{
	String docId;
	String title;
	String url;
	int pv;
	String docChannel;
	double hotScore;
	String hotLevel;
	int commentsNum;
	String publishTime;
	String docType;
	String why;
	public debugclass(HotRankItem hot){
		docId = hot.getDocId();
		title = hot.getTitle();
		url = hot.getUrl();
		pv = hot.getPv();
		docChannel = hot.getDocChannel();
		hotScore = hot.getHotScore();
		hotLevel = hot.getHotLevel();
		commentsNum = hot.getCommentsNum();
		publishTime = hot.getPublishTime();
		docType = hot.getDocType();
		why = hot.getWhy();
	}
}