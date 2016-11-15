/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.newHotPredict;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;








import redis.clients.jedis.Jedis;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.zhanzh.SolrUtil.SearchItemsFromSolr;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;

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
 *          1.0          2015年8月13日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class HotItemDisSplitByDay {
	private static Log LOG = LogFactory.getLog("HotItemDisSplitByDay");
//	public static HashMap<String, String> redisTest = new HashMap<String, String>();
	/**
	 * LocCacheHotItemMap用于缓存当前各个类别的实时热点新闻
	 * key：keyname（类别名称） value : HashMap<docID,CacheHotRankItem> 
	 * channel代表当前刚刚加载临时新闻数据（待处理）
	 * channel+0今天的新闻数据。。。
	 */
	public static HashMap<String, HashMap<String,CacheHotRankItem>> LocCacheHotItemMap = new HashMap<String, HashMap<String,CacheHotRankItem>>();
	
//	public static HashMap<String, String> locOsCache = new HashMap<String, String>();
	
	private static HotItemDisSplitByDay instance = new HotItemDisSplitByDay();
	//OSCache
	private OSCache osCache ;
	private static final String CACHE_NAME = "OScache_Preload";
	//预加载白名单分类配置文件
	private static PropertiesConfiguration configs;
//	private static String configPathstr = "D:/test/testPreload/preloadwhitelist.properties";
	private static String configPath = "/data/zhanzh/WhiteListPreload/conf/preloadwhitelist.properties";
	
	
	private HotItemDisSplitByDay(){
		//白名单配置文件初始化
		try {
			configs = new PropertiesConfiguration();
			configs.setEncoding("UTF-8");
			configs.load("/data/zhanzh/WhiteListPreload/conf/preloadwhitelist.properties");
			configs.setReloadingStrategy(new FileChangedReloadingStrategy());
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.error("Load properties errror "+e);
		}
		
		//OScache初始化
		int refreshInterval = 30*24*60*60;
		LOG.info("refreshInterval = "+refreshInterval);
		osCache = new OSCache("conf/oscache_preload.properties", CACHE_NAME, refreshInterval);
//		osCache = new OSCache("D:/test/testPreload/oscache_preload.properties", CACHE_NAME, refreshInterval);
		
		LOG.info(CACHE_NAME+" has created");
		
		//从OSCache中初始化LocCacheHotItemMap
		try {
			String json = (String) osCache.get("LocCacheHotItemMap");
			if(json != null || !json.isEmpty()){
				Gson gson = new Gson();
				LocCacheHotItemMap = gson.fromJson(json, new TypeToken<HashMap<String, HashMap<String,CacheHotRankItem>>>() {
				}.getType());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.warn("LocCacheHotItemMap has not found in osCache");
		}
	}

	public static HotItemDisSplitByDay getInstance(){
		return instance;
	}
	
	/**
	 * 更新本地缓存的当前热点新闻LocCacheHotItemMap
	 * 
	 * 
	 * 注：
	 * 
	 * @param keyname 分类名  List<CacheHotRankItem>
	 * 
	 * @return void
	 */
	public void updateLocCacheHotRankItem(String keyname,List<CacheHotRankItem> hotlist){
		if(hotlist == null){
			LOG.error("hotlist is null ~");
			return;
		}
		HashMap<String, CacheHotRankItem> cachemap = LocCacheHotItemMap.get(keyname);
		if(cachemap == null){
			cachemap = new HashMap<String, CacheHotRankItem>();
		}
		for(CacheHotRankItem hot : hotlist){
			String id = hot.getDocId();	
			CacheHotRankItem cacheitem = cachemap.get(id);
			if(cacheitem != null){
				cacheitem.update(hot);
			}else{
				cachemap.put(id, hot);
			}
		}
		LocCacheHotItemMap.put(keyname, cachemap);
	}
	
	/**
	 * 将历史热点新闻按时间分别存入map中
	 * 将当前实时热点map中不在生命周期的新闻按时间分别存入map中，并从实时热点map中将该条新闻删除
	 * 
	 * 
	 * 注：返回map中Key是keyName+0/1/... 
	 * 
	 * @param keyname 分类名  int cacheDay 最长缓存天数
	 * 
	 * @return void
	 */
	public  HashMap<String, List<CacheHotRankItem>> spliteCacheItemListByDay(String keyName,int cacheDay){
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Gson gson = new Gson();
		HashMap<String, List<CacheHotRankItem>> tempcache = new HashMap<String, List<CacheHotRankItem>>();
		Set<String> addedId = new HashSet<String>();//用于id排重
		
		//将现在实时缓存的数据里不在存活的数据按日期存入缓存中
		HashMap<String, CacheHotRankItem> tempMap = LocCacheHotItemMap.get(keyName);
		if(tempMap != null && !tempMap.isEmpty()){
			Set<String> keySet = new HashSet<String>();
			keySet.addAll(tempMap.keySet());
			for(String key : keySet){
				if(!SearchItemsFromSolr.searchItemIsAvailable(key)){
					CacheHotRankItem citem = tempMap.get(key);
					try {
						Date date = formate.parse(citem.getPublishTime());
						Long time = (System.currentTimeMillis()-date.getTime())/(24*60*60*1000);
						int dayTime = time.intValue();
						//默认把今天不在生命周期的新闻也加入第一次上拉数据中
						if(dayTime == 0){
							dayTime = 1;
						}
						if(dayTime>cacheDay||dayTime<0){
							LOG.info(citem.getDocId()+" "+ citem.getTitle() +" " +"Out of cache time ");
							tempMap.remove(key);
							continue;
						}
						if (!addedId.contains(citem.getDocId())) {
							String tempKey = keyName + dayTime;
							List<CacheHotRankItem> templist = tempcache
									.get(tempKey);
							if (templist == null) {
								templist = new ArrayList<CacheHotRankItem>();
								templist.add(citem);
								tempcache.put(tempKey, templist);
							} else {
								templist.add(citem);
							}
							addedId.add(citem.getDocId());
						}
						tempMap.remove(key);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						LOG.error("Paser time error : ",e);
						tempMap.remove(key);
						continue;
					}
				}else{
					//在生命周期的存入day0中
					CacheHotRankItem citem = tempMap.get(key);
					int dayTime = 0;
					if (!addedId.contains(citem.getDocId())) {
						String tempKey = keyName + dayTime;
						List<CacheHotRankItem> templist = tempcache
								.get(tempKey);
						if (templist == null) {
							templist = new ArrayList<CacheHotRankItem>();
							templist.add(citem);
							tempcache.put(tempKey, templist);
						} else {
							templist.add(citem);
						}
						addedId.add(citem.getDocId());
					}
				}
			}
		}
		//Test
		LOG.info(keyName+" LocalCacheMap size : "+tempMap.size());
		
		// 将OSCache中的数据loading到内存，并过滤掉超出缓存日期的数据
		for (int day = cacheDay; day >= 1; day--) {
			String key = keyName + day;
//			String json = locOsCache.get(key);
			String json = null;
			try {
				json = (String) osCache.get(key);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				LOG.error("Get item from OSCache error : "+key);
				continue;
			}
			if (json == null || json.isEmpty()) {
				LOG.warn(key + " is no cache ");
				continue;
			}
			@SuppressWarnings("serial")
			List<CacheHotRankItem> itemlist = gson.fromJson(json,
					new TypeToken<List<CacheHotRankItem>>() {
					}.getType());
			for (CacheHotRankItem citem : itemlist) {

				try {
					Date date = formate.parse(citem.getPublishTime());
					Long time = (System.currentTimeMillis() - date.getTime())
							/ (24 * 60 * 60 * 1000);
					int dayTime = time.intValue();
					if (dayTime > cacheDay) {
						LOG.info(citem.getDocId() + " " + citem.getTitle()
								+ " " + "Out of cache time ");
						continue;
					}
					if (!addedId.contains(citem.getDocId())) {
						String tempKey = keyName + dayTime;
						List<CacheHotRankItem> templist = tempcache
								.get(tempKey);
						if (templist == null) {
							templist = new ArrayList<CacheHotRankItem>();
							templist.add(citem);
							tempcache.put(tempKey, templist);
						} else {
							templist.add(citem);
						}
						addedId.add(citem.getDocId());
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					LOG.error("Paser time error : ", e);
					continue;
				}
			}
		}
		return tempcache;
	}
	/**
	 * 对当前热点数据排序函数 即key0进行热点排序
	 * 
	 * 
	 * 注：
	 * 
	 * @param 
	 * 
	 * @return void
	 */
	public void sortNowCacheHotRankItemList(List<CacheHotRankItem> hotlist){
		if(hotlist == null||hotlist.isEmpty()){
			LOG.error("sort list error ~ hotlist is null");
			return;
		}
		//先按时间排序
		Collections.sort(hotlist, new Comparator<CacheHotRankItem>() {

			@Override
			public int compare(CacheHotRankItem o1, CacheHotRankItem o2) {
				// TODO Auto-generated method stub
				SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try{
					Date d1 = formate.parse(o1.getPublishTime());
					Date d2 = formate.parse(o2.getPublishTime());
					if(d1.before(d2)){
						return 1;
					}else if(d1.after(d2)){
						return -1;
					}else{
						return 0;
					}
				}catch(Exception e){
					LOG.error("sort error : ",e);
					return 0;
				}
			}
		});
		//再按PV排序
		Collections.sort(hotlist, new Comparator<CacheHotRankItem>() {

			@Override
			public int compare(CacheHotRankItem o1, CacheHotRankItem o2) {
				// TODO Auto-generated method stub
				if(o1.getPv()>o2.getPv()){
					return -1;
				}else if(o1.getPv()<o2.getPv()){
					return 1;
				}else{
					return 0;
				}
			}
		});
	}
	
	
	/**
	 * 历史热点数据排序函数
	 * 
	 * 
	 * 注：
	 * 
	 * @param 
	 * 
	 * @return void
	 */
	public void sortCacheHotRankItemList(List<CacheHotRankItem> hotlist){
		if(hotlist == null||hotlist.isEmpty()){
			LOG.error("sort list error ~ hotlist is null");
			return;
		}
		//先按时间排序
		Collections.sort(hotlist, new Comparator<CacheHotRankItem>() {

			@Override
			public int compare(CacheHotRankItem o1, CacheHotRankItem o2) {
				// TODO Auto-generated method stub
				SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try{
					Date d1 = formate.parse(o1.getPublishTime());
					Date d2 = formate.parse(o2.getPublishTime());
					if(d1.before(d2)){
						return 1;
					}else if(d1.after(d2)){
						return -1;
					}else{
						return 0;
					}
				}catch(Exception e){
					LOG.error("sort error : ",e);
					return 0;
				}
			}
		});
		//再按PV排序
		Collections.sort(hotlist, new Comparator<CacheHotRankItem>() {

			@Override
			public int compare(CacheHotRankItem o1, CacheHotRankItem o2) {
				// TODO Auto-generated method stub
				if(o1.getMaxScore()>o2.getMaxScore()){
					return -1;
				}else if(o1.getMaxScore()<o2.getMaxScore()){
					return 1;
				}else{
					return 0;
				}
			}
		});
		//再根据日期精确到天排序
		Collections.sort(hotlist, new Comparator<CacheHotRankItem>() {

			@Override
			public int compare(CacheHotRankItem o1, CacheHotRankItem o2) {
				// TODO Auto-generated method stub
				SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd");
				try{
					Date d1 = formate.parse(o1.getPublishTime());
					Date d2 = formate.parse(o2.getPublishTime());
					if(d1.before(d2)){
						return 1;
					}else if(d1.after(d2)){
						return -1;
					}else{
						return 0;
					}
				}catch(Exception e){
					LOG.error("sort error : ",e);
					return 0;
				}
			}
		});
	}
	
	/*
	 * old deprecated
	 */
//	public void DisItemSplitByDay(){
//		
//		HashSet<String> channelSet = new HashSet<String>();
//		Gson gson = new Gson();
//		//加载白名单channel
//		List<String> list = configs.getList("w");
//		channelSet.addAll(list);
//		
//		for(String channel : channelSet){
//			//从redis中取当前实时热点
//			String rediskeyname = channel+"-1";
//			List<CacheHotRankItem> fromRedisCache = loadingCacheItemFromRedis(rediskeyname);
//			//将当前实时热点与本地缓存数据更新合并
//			updateLocCacheHotRankItem(channel, fromRedisCache);
//			//将本地缓存的数据做更新，并按日期进行分割
//			HashMap<String, List<CacheHotRankItem>> spliteHotMap = spliteCacheItemListByDay(channel, 30);
//			//对更新分割的数据进行排序投放
//			Set<String> keySet = spliteHotMap.keySet();
//			for(String key : keySet){
//				String todayKeyName = channel+"0";
//				List<CacheHotRankItem> tempList = spliteHotMap.get(key);
//				if(key.equals(todayKeyName)){
////					LOG.info(todayKeyName+" use today sort method ");
//					sortNowCacheHotRankItemList(tempList);
//				}else{
//					sortCacheHotRankItemList(tempList);
//				}
//				
//				if(tempList == null || tempList.isEmpty()){
//					LOG.warn(key+" is null before disToRedis ");
//					continue;
//				}
//				//转换为前端投放数据结构
//				List<FrontNewsItem> frontList = new ArrayList<FrontNewsItem>();
//				for(CacheHotRankItem citem : tempList){
//					String json = SearchItemsFromSolr.searchItem2appJsonById(citem.getDocId());
//					try{
//						if(json != null){
//							FrontNewsItem fitem = gson.fromJson(json, FrontNewsItem.class);
//							frontList.add(fitem);
//						}else{
//							LOG.warn("turn to FrontNewsItem error "+citem.getDocId());
//						}
//					} catch (Exception e){
//						LOG.error("turn to FrontNewsItem error ",e);
//						continue;
//					}
//				}
//				String disjson = gson.toJson(frontList);
//				disToRedis(key, 8*60*60, disjson);
//				//更新OSCache内容
//				String disOSCacheString = gson.toJson(tempList); 
//				osCache.put(key, disOSCacheString);
//			}
//		}
//		//每轮更新完会将内存中实时的热点数据存入OSCache中
//		String cacheLocCacheHotItemMap = gson.toJson(LocCacheHotItemMap);
//		osCache.put("LocCacheHotItemMap", cacheLocCacheHotItemMap);
//		LOG.info("Save LocCacheHotItemMap to OSCache ~");
//	}
	
	public void DisItemSplitByDay() {

		HashSet<String> channelSet = new HashSet<String>();
		Gson gson = new Gson();
		// 加载白名单channel
		List<String> list = configs.getList("w");
		channelSet.addAll(list);

		for (String channel : channelSet) {
			// 从redis中取当前实时热点
			String rediskeyname = channel + "-1";
			List<CacheHotRankItem> fromRedisCache = loadingCacheItemFromRedis(rediskeyname);
			// 将当前实时热点与本地缓存数据更新合并
			updateLocCacheHotRankItem(channel, fromRedisCache);
			// 将本地缓存的数据做更新，并按日期进行分割
			HashMap<String, List<CacheHotRankItem>> spliteHotMap = spliteCacheItemListByDay(
					channel, 30);
			// 对更新分割的数据进行排序投放
			
			// 处理今天的数据
			ArrayList<FrontNewsItem> todayNewsList = new ArrayList<FrontNewsItem>();
			String tkey = channel + "0";
			List<CacheHotRankItem> todayList = spliteHotMap.get(tkey);
			if (todayList != null && !todayList.isEmpty()) {
				sortNowCacheHotRankItemList(todayList);
				for (CacheHotRankItem citem : todayList) {
					String json = SearchItemsFromSolr
							.searchItem2appJsonById(citem.getDocId());
					try {
						if (json != null) {
							FrontNewsItem fitem = gson.fromJson(json,
									FrontNewsItem.class);
							todayNewsList.add(fitem);
						} else {
							LOG.warn("turn to FrontNewsItem error "
									+ citem.getDocId());
						}
					} catch (Exception e) {
						LOG.error("turn to FrontNewsItem error ", e);
						continue;
					}
				}
				String disOSCacheString = gson.toJson(todayList);
				osCache.put(tkey, disOSCacheString);
			}
			String todaydisjson = gson.toJson(todayNewsList);
			disToRedis(tkey, 8 * 60 * 60, todaydisjson);

			// 处理1天前的数据
			List<FrontNewsItem> disList = new ArrayList<FrontNewsItem>();
			for (int i = 1; i <= 30; i++) {
				String key = channel + i;
				List<CacheHotRankItem> tempList = spliteHotMap.get(key);
				if (tempList == null || tempList.isEmpty()) {
					continue;
				}

				sortCacheHotRankItemList(tempList);

				for (CacheHotRankItem citem : tempList) {
					String json = SearchItemsFromSolr
							.searchItem2appJsonById(citem.getDocId());
					try {
						if (json != null) {
							FrontNewsItem fitem = gson.fromJson(json,
									FrontNewsItem.class);
							disList.add(fitem);
						} else {
							LOG.warn("turn to FrontNewsItem error "
									+ citem.getDocId());
						}
					} catch (Exception e) {
						LOG.error("turn to FrontNewsItem error ", e);
						continue;
					}
				}
				// 更新OSCache内容
				String disOSCacheString = gson.toJson(tempList);
				osCache.put(key, disOSCacheString);
			}
			//暂时只去300条
			if(disList.size()>300){
				disList = disList.subList(0, 300);
			}
			String disjson = gson.toJson(disList);
			disToRedis(channel, 8 * 60 * 60, disjson);
		}
		// 每轮更新完会将内存中实时的热点数据存入OSCache中
		String cacheLocCacheHotItemMap = gson.toJson(LocCacheHotItemMap);
		osCache.put("LocCacheHotItemMap", cacheLocCacheHotItemMap);
		LOG.info("Save LocCacheHotItemMap to OSCache ~");
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
	 * 在redis中取出对应类别的热点数据
	 * 
	 * 注：
	 * 
	 * @param redis中的keyname
	 * 
	 * @return List<CacheHotRankItem>
	 */
	public List<CacheHotRankItem> loadingCacheItemFromRedis(String keyname){
		List<CacheHotRankItem> itemlist = null;
		try{
			Jedis jedis = new Jedis("10.32.21.62", 6379, 10000);
			jedis.select(11);
			String value = jedis.get(keyname);
			Gson gson = new Gson();
			itemlist = gson.fromJson(value, new TypeToken<List<CacheHotRankItem>>() {
			}.getType());
		} catch (Exception e){
			LOG.error("Loading cache item error ", e);
			return itemlist;
		}
		return itemlist;
	}
	
	public static void main(String[] args){
		HotItemDisSplitByDay hd = HotItemDisSplitByDay.getInstance();
		while(true){
			hd.DisItemSplitByDay();
			LOG.info("sleep 5 min");
			try {
				Thread.sleep(5*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

//		try {
//			hd.osCache.put("社会5", "12345");
//			String test = (String) hd.osCache.get("社会5");
//			System.out.println(test);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
