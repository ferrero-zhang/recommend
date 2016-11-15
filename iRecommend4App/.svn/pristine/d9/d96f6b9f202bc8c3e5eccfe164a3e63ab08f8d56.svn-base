/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;

import redis.clients.jedis.Jedis;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.commen.blackList.BlackListData;
import com.ifeng.commen.blackList.subscriber.CommonDataSub;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.zhanzh.Utils.AdjStringsIsSim;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.KeyWord;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.AlgorithmPreloadKeyUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.HttpRequestUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.NewsSortUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.PreloadItemFromSolrUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.RankScoreUtil;

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
 *          1.0          2015年12月8日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class AlgorithmChannelPreload implements Runnable {
	
	private static Log LOG = LogFactory.getLog("AlgorithmChannelPreload");
	private OSCache osCache ;
	private static final String CACHE_NAME = "OScache_AlgorithmChannelPreload";
	private static AlgorithmChannelPreload instance = new AlgorithmChannelPreload();
	private static PropertiesConfiguration configs;
	
	//本地频道城市列表集合
	private static Set<String> citySet = new HashSet<String>();
	//RelatedFeature 该set中数据才可以加载relatedfeature
	private static Set<String> relatedTagsSet = new HashSet<String>();
	//tags映射对于名称错乱的tags建立映射 以及 较粗分类建立映射关系
	private static HashMap<String, List<String>> tagsMap = new HashMap<String, List<String>>();
	
	private AlgorithmChannelPreload(){
		//白名单配置文件初始化
		try {
			configs = new PropertiesConfiguration();
			configs.setEncoding("UTF-8");
			configs.load("conf/AlgorithmChannelPreload.properties");
			configs.setReloadingStrategy(new FileChangedReloadingStrategy());
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.error("Load properties errror " + e);
		}
		
		//初始化OScache 有效期一个月
		int refreshInterval = 30 * 24 * 60 * 60;
		LOG.info("refreshInterval = " + refreshInterval);
		osCache = new OSCache("conf/oscache_AlgorithmChannel.properties", CACHE_NAME,
				refreshInterval);
		LOG.info(CACHE_NAME + " has created");
	}
	
	public static AlgorithmChannelPreload getInstance(){
		return instance;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		preload();
	}
	
	public void preload(){
		
		List<KeyWord> keyWordList = AlgorithmPreloadKeyUtil.getInstance().getKeyWordsList();
		while(true){
			if(keyWordList == null || keyWordList.isEmpty()){
				LOG.info(Thread.currentThread().getName()+" finished ~");
				return;
			}
			Gson gson = new Gson();
			
			//加载黑名单数据
			Map<String, Set<String>> blacklistMap = getBlacklistMap();
			
			for(KeyWord keyword : keyWordList){
				String channel = keyword.getName();
				//temp 判断词如果为本地词表暂时过掉
//				if(citySet.contains(channel)){
//					continue;
//				}
				//用于最终投放
				List<FrontNewsItem> newslist = new ArrayList<FrontNewsItem>();
				//用于后台缓存
				List<PreloadItem> cacheRankList = new ArrayList<PreloadItem>();
				//建立索引
				HashMap<String, PreloadItem> indexMap = new HashMap<String, PreloadItem>();
				//用于id排重
				Set<String> idSet = new HashSet<String>();
				//loadingOscache中的数据
				String oscacheNewsJson= null;
				try {
					oscacheNewsJson = (String) osCache.get(channel);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOG.warn("Loading "+channel+" faild , oscache newslist is null ~");
				}
				if(oscacheNewsJson != null){
					List<PreloadItem> tempnewslist = gson.fromJson(oscacheNewsJson, new TypeToken<List<PreloadItem>>() {
					}.getType());
					for(PreloadItem item : tempnewslist){
						idSet.add(item.getFitem().getDocId());
						//在oscache中选取的数据均置为false（均非本次从solr中选去的数据）
						item.setRelatedSearchItem(false);
	
						cacheRankList.add(item);
						indexMap.put(item.getFitem().getDocId(), item);
					}
				}
				
				//change 做映射，对于较粗的分类做映射处理 （只对映射表中的相关分类做映射）
				//加载solr中新闻数据
				List<PreloadItem> solrLoadNewsItem = new ArrayList<PreloadItem>();
				if(tagsMap.containsKey(keyword.getName())){
					List<String> mapList = tagsMap.get(keyword.getName());
					for(String tag : mapList){
						KeyWord word = AlgorithmPreloadKeyUtil.getInstance().queryKeywordByName(tag);
						if(word != null){
							List<PreloadItem> tempNewsItemList = getPreloadNewsItem(word);
							solrLoadNewsItem.addAll(tempNewsItemList);
							LOG.info(keyword.getName()+" has map to : "+word.getName());
						}
					}
				}else{
					solrLoadNewsItem = getPreloadNewsItem(keyword);
				}
				
				
				//test 切换新的热度排序
				RankScoreUtil.getInstance().setRankscore(solrLoadNewsItem,channel);
				

				
				//更新oscache（加入排重）
				for(PreloadItem pItem : solrLoadNewsItem){
					if(idSet.contains(pItem.getFitem().getDocId())){
						updateOldItem(indexMap.get(pItem.getFitem().getDocId()), pItem);
						continue;
					}
					
					idSet.add(pItem.getFitem().getDocId());
					indexMap.put(pItem.getFitem().getDocId(), pItem);
					
					if(isContainedSimItem(pItem, cacheRankList)){
						updateOldItem(indexMap.get(pItem.getFitem().getDocId()), pItem);
						continue;
					}
					//标题长度大于32的过滤掉
					if(getLength(pItem.getFitem().getTitle())>26){
//						LOG.info("Title length longer than 26 : "+pItem.getFitem().getTitle()+" "+pItem.getFitem().getDocId());
						//标题长的新闻相似度降权使其排序靠后
						pItem.setSimScore(0.1f);
//						continue;
					}
					
					//本次来自solr的数据 也置为true
					pItem.setRelatedSearchItem(true);
					
					cacheRankList.add(pItem);
				}
				if(cacheRankList.isEmpty()){
					LOG.warn("Preload "+channel+" is faild , newslist is empty ~");
				}
				

				
				//时间、热度、相似度序排列
				if(keyword.getType().equals("loc")){
					cacheRankList = NewsSortUtil.sortLocChannelNewsList(cacheRankList);
					LOG.info("sort loc news : "+keyword.getName()+" size : "+cacheRankList.size());
				} else {
//					cacheRankList = NewsSortUtil.sortAlgorithmChannelNewsList(cacheRankList);
					cacheRankList = NewsSortUtil.sortAlgorithmChannelNewsList_new(cacheRankList);
				}
				
				
				//只取前500条
				if(cacheRankList.size()>500){
					cacheRankList = cacheRankList.subList(0, 500);
				}
				

	
				//将PreloadItem转换成FrontNewsItem
				//此处进行黑名单过滤
				//将本地数据why字段加入北京市地域信息
				Iterator<PreloadItem> it = cacheRankList.iterator();
				while(it.hasNext()){
					PreloadItem item = it.next();
					//过滤黑名单
					if(blacklistMap.get("id").contains(item.getFitem().getDocId())){
						LOG.info("hit in black List by docId "+item.getFitem().getDocId()+" title : "+item.getFitem().getTitle());
						it.remove();
						continue;
					}
					if(blacklistMap.get("title").contains(item.getFitem().getTitle())){
						LOG.info("hit in black List by title "+item.getFitem().getDocId()+" title : "+item.getFitem().getTitle());
						it.remove();
						continue;
					}
					//why字段加入地域信息
					if(keyword.getType().equals("loc")){
						item.getFitem().setWhy(keyword.getName());
					}
					newslist.add(item.getFitem());
				}
				
//String testStr = gson.toJson(cacheRankList);
//LOG.info("test : "+keyword.getName()+" "+testStr);
//	System.out.println(testStr);
				//test
//				if(keyword.getType().equals("loc")){
//					String testStr = gson.toJson(cacheRankList);
//					LOG.info("test : "+keyword.getName()+" "+testStr);
//				}
				
				LOG.info(channel+" cache size : "+newslist.size());
				//将更新完的数据序列化后同步写入57redis，和本地osCache
				String oscacheStr = gson.toJson(cacheRankList);
//				osCache.put(channel, oscacheStr);
				saveToOscache(channel, oscacheStr);

				String disStr = gson.toJson(newslist);
//	System.out.println(disStr);
				disToRedis(channel, 8*60*60*1000, disStr);
			}
			
			keyWordList = AlgorithmPreloadKeyUtil.getInstance().getKeyWordsList();
		}
	}
	
	public static void main(String[] args){
		
//		黑名单订阅线程
		Thread t=new Thread(new CommonDataSub());
		t.start();
		ExecutorService pool = Executors.newFixedThreadPool(20);
		while(true){			
			//更新需要预加载的KEY
//			AlgorithmPreloadKeyUtil.getInstance().loadingPreloadChannelKeySet();
			AlgorithmPreloadKeyUtil.getInstance().loadingPreloadChannelKeySet_MThread();
			AlgorithmChannelPreload preloadinstance = AlgorithmChannelPreload.getInstance();
			//更新城市列表
//			AlgorithmChannelPreload.updateLocCityList();
			//更新配置文件信息
			preloadinstance.loadingConfiguration();
			//将配置文件需要映射的词加入keywordlist
			preloadinstance.addMapTagsToKeywordList();
			//清除得分缓存
			RankScoreUtil.getInstance().clearCacheScoreMap();
			RankScoreUtil.getInstance().updatePVMap();
			//建立线程池
			List<Future> flist = new ArrayList<Future>();
			for(int i=0; i<20;i++){
				Future f = pool.submit(preloadinstance);
				flist.add(f);
			}
			for(Future f : flist){
				try {
					f.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//投放本地数据
//			preloadinstance.preloadLocData();
			

			
			try {
				LOG.info("Sleep 10 min ~");
				Thread.sleep(10*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
//		
//		Thread t=new Thread(new CommonDataSub());
//		t.start();
//		
//		Map<String, Set<String>> temp = getBlacklistMap();
//		Gson gson = new Gson();
//		String json = gson.toJson(temp);
//		System.out.println(json);
		
//		updateLocCityList();
	}
	
	/**temp
	 * 获取实时本地频道城市列表,更新citySet
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return void
	 */
	//temp 本地临时数据结构
	class cityListStruct{
		List<FrontNewsItem> slideList;
		List<FrontNewsItem> docList;
	}
	class citystructure{
		List<String> province;
		List<String> city;
	}
	public static void updateLocCityList(){
		//old 
		String cityStr = HttpRequestUtil.downloadPageByRetry("http://api.irecommend.ifeng.com/citylistkeys.php", "UTF-8", 3);
		if(cityStr == null){
			LOG.error("Download city info error ~");
			return;
		}
		Gson gson = new Gson();
		try{
			
			citystructure citys = gson.fromJson(cityStr, citystructure.class);
			if(citys != null && citys.province != null && !citys.province.isEmpty()){
				for(String province : citys.province){
					citySet.add(province);
				}
			}
			if(citys != null && citys.city != null && !citys.city.isEmpty()){
				for(String city : citys.city){
					citySet.add(city);
				}
			}
		} catch (Exception e){
			LOG.error("Parser cityInfo error ~", e);
		}
		List<KeyWord> locKeyWordList = AlgorithmPreloadKeyUtil.getInstance().getLocKeyWordList();
		for(KeyWord loc : locKeyWordList){
			citySet.add(loc.getName());
		}
		
		Jedis jedis = new Jedis("10.32.24.194", 6379, 10000);
		jedis.select(0);
		
		for(String city : citySet){
			String json = jedis.get(city);
			if(json != null){
				KeyWord keyword = gson.fromJson(json, KeyWord.class);
				if(!keyword.getType().equals("loc")){
					keyword.setType("loc");
					String saveStr = gson.toJson(keyword);
					System.out.println(saveStr);
					jedis.set(keyword.getName(), saveStr);
				}
			}else{
				KeyWord keyword = new KeyWord();
				keyword.setName(city);
				keyword.setDate("2016-01-19 10:24:59");
				keyword.setType("loc");
				String saveStr = gson.toJson(keyword);
				System.out.println(saveStr);
			}
		}
		
	}
	
	public void preloadLocData(){
		//temp 对本地频道数据进行单独投放
		LOG.info("Dis Loc City Data ~");
//		Gson gson = new Gson();
//		SimpleDateFormat formate = new SimpleDateFormat("MMM d, yyyy hh:mm:ss aaa",Locale.ENGLISH);
//		SimpleDateFormat formate2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINESE);
//		for(String city : citySet){
//			String url = "http://10.32.24.202:8080/RegionalRecommend/GetDetailLocalNews?city="+city;
//			String datalist = HttpRequestUtil.downloadPageByRetry(url, "UTF-8", 2);
//			try{
//				cityListStruct cityList = gson.fromJson(datalist, cityListStruct.class);
//				List<FrontNewsItem> tempList = cityList.docList;
//				if(tempList != null && !tempList.isEmpty() ){
//					//旧本地数据与新数据格式不同需要对id_value,doc_type，date做对应转换
//					for(FrontNewsItem item : tempList){
//						item.setDocId("imcp_"+item.getId_value());
//						item.setId_value(null);
//						
//						item.setDocType(item.getDoc_type());
//						item.setDoc_type(null);
//
//						
//						Date date = formate.parse(item.getDate());
//						item.setDate(formate2.format(date));
//					}
//					
//					String disStr = gson.toJson(tempList);
//					disToRedis(city, 8*60*60*1000, disStr);
//				}
//			} catch (Exception e){
//				LOG.error("Dis loc error : "+city, e);
//				continue;
//			}
//		}
//		List<KeyWord> locKeyWordList = AlgorithmPreloadKeyUtil.getInstance().getLocKeyWordList();
//		for(KeyWord word : locKeyWordList){
//			List<PreloadItem> itemlist = getPreloadNewsItem(word);
//			
//		}
		
		
	}

	/**
	 * 加载配置文件
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void loadingConfiguration(){
		//读取可以加载relatedFeature的tags
		String[] relatedTags = configs.getStringArray("relatedTags");
		if(relatedTags != null){
			for(String tags : relatedTags){
				relatedTagsSet.add(tags);
			}
		}
		//加载Tags映射表 社会->社会资讯
		List<String> mapTagsList = configs.getList("mapTags");
		if(mapTagsList != null){
			for(String tags : mapTagsList){
				try{
					String[] temp = tags.split("->");
					String key = temp[0];
					String value = temp[1];
					List<String> mapList = new ArrayList<String>();
					String[] valueList = value.split("\\|");
					for(String tempValue : valueList){
						mapList.add(tempValue);
					}
					tagsMap.put(key, mapList);
				}catch (Exception e){
					LOG.error("Parser TagsMap error : "+tags, e);
					continue;
				}
			}
		}
		LOG.info("Loading config success ");
	}
	
	/**
	 * 将需要映射的tags词添加至预加载list
	 * 
	 * 注：因为存在有些词不在可用表中，这部分词则一直无法预加载，对这部分词只能通过
	 * 手动添加，使其可以预加载，这部分词只需要添加到映射表配置文件即可
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void addMapTagsToKeywordList(){
		Set<String> keySet = tagsMap.keySet();
		for(String key : keySet){
			KeyWord keyword = new KeyWord();
			keyword.setName(key);
			keyword.setAvailable(true);
			keyword.setType("temp");
			AlgorithmPreloadKeyUtil.getInstance().addKeyWordsToList(keyword);
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
	private synchronized void disToRedis(String tableName,long validTime,String disStr){
		if(disStr == null){
			return ;
		}
		try{

			Jedis jedis = new Jedis("10.90.1.57", 6379, 10000);
			jedis.select(2);
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
	
	private synchronized void saveToOscache(String channel,String saveStr){
		osCache.put(channel, saveStr);
	}
	
	/**
	 * 用于计算字符串长度（中文占一个字符，英文占半个字符）
	 * 
	 * 注意：
	 * @param String s 
	 * 
	 * @return double
	 * 
	 */
	private double getLength(String s) {
		if(s==null){
			return 0;
		}		
		double valueLength = 0;
		String chinese = "[\u0391-\uFFE5]";			
		for (int i = 0; i < s.length(); i++) {
			// 获取一个字符
			String temp = s.substring(i, i + 1);
			// 判断是否为中文字符
			if (temp.matches(chinese)) {
				valueLength += 1;
			} else {
				valueLength += 0.5;
			}
		}		
		return Math.ceil(valueLength);
	}
	
	/**
	 * 根据标题在list中查找相似item的函数
	 * 
	 * 注意：
	 * @param  FrontNewsItem item ,List<FrontNewsItem> itemlist
	 * 
	 * @return boolean
	 * 
	 */
	private boolean isContainedSimItem(PreloadItem item ,List<PreloadItem> itemlist){
		if(item == null || itemlist == null || itemlist.isEmpty()){
			return false;
		}
		for(PreloadItem pitem : itemlist){
			if(AdjStringsIsSim.isSimStr(item.getFitem().getTitle(), pitem.getFitem().getTitle())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 更新旧的文章项，更新热度得分等等
	 * 
	 * 
	 * 
	 * @param PreloadItem oldItem 已经加入需要更新的Item
	 * 		  PreloadItem newItem 新加入Item
	 * 		  
	 * 
	 * @return 
	 */
	private void updateOldItem(PreloadItem oldItem ,PreloadItem newItem){
		oldItem.setRankscore(newItem.getRankscore());
		oldItem.getFitem().setHotLevel(newItem.getFitem().getHotLevel());
		oldItem.getFitem().setHotBoost(newItem.getFitem().getHotBoost());
		oldItem.setHotscore(newItem.getHotscore());
		oldItem.setSimScore(newItem.getSimScore());
		oldItem.setPv(newItem.getPv());
		//若本次solr中数据与oscache数据命中，则置为true
		oldItem.setRelatedSearchItem(true);
	}
	

	/**
	 * 
	 * 从solr中查询预加载新闻
	 * 
	 * 
	 * 
	 * 注意：
	 * @param KeyWord keyword
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	public List<PreloadItem> getPreloadNewsItem(KeyWord keyword){
		List<PreloadItem> preloadItemList = new ArrayList<PreloadItem>();
		if(keyword == null){
			return null;
		}
		int rows = 200;//预加载数据量
		String channel = keyword.getName();
		
		channel = escapeQueryChars(channel);
		String queryStr = null;
		//区分是否为稿源Tag
		if(keyword.getType().equals("swm")){
			queryStr = "source:("+channel+")";
		}else if(keyword.getType().equals("c")){
			queryStr = "topic1:("+channel+")";
		}else if(keyword.getType().equals("loc")){
			queryStr = "relatedfeatures:(loc="+channel+")";
		}else{
			queryStr = "topic1:("+channel+") OR topic2:("+channel+") OR topic3:("+channel+")";
		}
		//直接搜索t1 t2 t3 获得的数据
		SolrQuery query = generaterSolrQuery(queryStr, rows);
		String solrUrl = configs.getString("solrUrl");
		List<PreloadItem> itemList = PreloadItemFromSolrUtil.preloadItemFromSolr(query,solrUrl,false);
		preloadItemList.addAll(itemList);

		
		return preloadItemList;
	}
	
	/**
	 * 
	 * 获取solr查询参数
	 * 
	 * 
	 * 
	 * 注意：
	 * @param SolrQuery query
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	public SolrQuery generaterSolrQuery(String queryStr,int rows){
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		query.setRows(rows);
		query.addSort("date",SolrQuery.ORDER.desc);
		query.set("fl", "*,score");
		
		//test 8081全新公式
		query.set("simi", "tfonly");
		query.set("defType", "payload");
		
		//过滤条件	(本地数据不加该过滤)
		if(queryStr.indexOf("loc=") < 0){
			query.addFilterQuery("other:(ifeng OR yidian -illegal )");
		}
		return query;
	}
	/**
	 * 用于处理在solr查询中的特殊字符
	 * 
	 * 注意：
	 * @param String channel 
	 * 
	 * @return String
	 * 
	 */
	private static String escapeQueryChars(String s) {  
	    StringBuilder sb = new StringBuilder();  
	    for (int i = 0; i < s.length(); i++) {  
	      char c = s.charAt(i);  
	      // These characters are part of the query syntax and must be escaped  
	      if (c == '\\' || c == '+' || c == '-' || c == '!'  || c == '(' || c == ')' || c == ':'  
	        || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'  
	        || c == '*' || c == '?' || c == '|' || c == '&'  || c == ';' || c == '/'  
	        || Character.isWhitespace(c)) {  
	        sb.append('\\');  
	      }  
	      sb.append(c);  
	    }  
	    return sb.toString();  
	  }
	
	/**
	 * 用于处理实时获取黑名单数据
	 * 
	 * 并将黑名单数据解析到id和title两个set中
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return Map<String, Set<String>>
	 * 
	 */
	private static Map<String, Set<String>> getBlacklistMap(){
		HashMap<String, Set<String>> blacklistmap = new HashMap<String, Set<String>>();
		blacklistmap.put("title", new HashSet<String>());
		blacklistmap.put("id", new HashSet<String>());
		List<String> blacklist = BlackListData.getInstance().get_blacklist();
		Pattern pattern = Pattern.compile("[0-9]*"); 
		  
		if(blacklist != null && !blacklist.isEmpty()){
			for(String str : blacklist){
				Matcher isNum = pattern.matcher(str);
				//纯数字组合识别成id
				if(isNum.matches()){
					blacklistmap.get("id").add(str);
				}else {
					blacklistmap.get("title").add(str);
				}
			}
		}
		return blacklistmap;
	}
	
}
