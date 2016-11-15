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
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;

import redis.clients.jedis.Jedis;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.commen.blackList.BlackListData;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.AliasLibData;
import com.ifeng.iRecommend.zhanzh.Utils.AdjStringsIsSim;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.EditorInstructItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.KeyWord;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.AlgorithmPreloadKeyUtil;
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
 *          1.0          2016年1月28日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class AlgPreloadProcess implements Runnable {
	private static Log LOG = LogFactory.getLog("AlgorithmChannelPreload");
	private OSCache osCache ;
	private List<KeyWord> keyWordList;
	private PropertiesConfiguration configs;
	//tags映射对于名称错乱的tags建立映射 以及 较粗分类建立映射关系
	private HashMap<String, List<String>> tagsMap ;
	
	public AlgPreloadProcess(OSCache osCache , List<KeyWord> keyWordList,PropertiesConfiguration configs){
		this.osCache = osCache;
		this.keyWordList = keyWordList;
		this.configs = configs;
		//加载配置文件数据
		this.tagsMap = BasicDataUpdateJob.getInstance().getConfigTagsMap();
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			preload();
		} catch (Exception e) {
			LOG.info("thread error ~"+e.getMessage());
		}
	}
	
	private void preload(){
		List<KeyWord> keyWordList = this.keyWordList;
		if(keyWordList == null || keyWordList.isEmpty()){
			LOG.info(Thread.currentThread().getName()+" finished ~");
			return;
		}
		Gson gson = new Gson();
		//加载黑名单数据
		Map<String, Set<String>> blacklistMap = getBlacklistMap();
		
		//加载泛编下线文章数据（后续取代黑名单数据)
		Set<String> offlineSet = NeedEditorCheckChannelPreload.getOfflineNewsIdSet();
		
		//test
		String testKey = configs.getString("testKey");
		
		//频道黑名单
		List<String> channelBlackList = configs.getList("forbiddenWord");
		
		
		
		for(KeyWord keyword : keyWordList){
			String channel = keyword.getName();
			//频道黑名单，命中的话直接将其从redis中删除
			if(channelBlackList.contains(channel)){
				LOG.info(channel+"hit in channelBlackList ~");
				delFromRedis(channel);
				continue;
			}
			
			//主别名映射
			//获取tag主名 有问题
			String mainChannelName = getTagMainName(channel);
			//若主名不为null 则将主名加入tagMap 将主名数据映射的该频道下
			if(mainChannelName != null && !channel.equals(mainChannelName)){
				List<String> tagList = tagsMap.get(channel);
				if(tagList != null){
					tagList.add(mainChannelName);
				} else {
					tagList = new ArrayList<String>();
					tagList.add(mainChannelName);
					tagsMap.put(channel, tagList);
				}
			}
			
			//用于最终投放
			List<FrontNewsItem> newslist = new ArrayList<FrontNewsItem>();
			//用于后台缓存
			List<PreloadItem> cacheRankList = new ArrayList<PreloadItem>();
			//建立索引
			HashMap<String, PreloadItem> indexMap = new HashMap<String, PreloadItem>();
			//用于id排重
			Set<String> idSet = new HashSet<String>();
			
			//记录每次在solr中查询数据中最旧的一条时间
			long endTime = System.currentTimeMillis();
			
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
					} else {
						LOG.info(keyword.getName()+" mapTag is null "+tag);
					}
				}
			}else{
				solrLoadNewsItem = getPreloadNewsItem(keyword);
			}
			

			
			//设置从solr中检索出的最后一条的时间
			endTime = getLastItemTime(solrLoadNewsItem);
			
			//设置热度
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
//					LOG.info("Title length longer than 26 : "+pItem.getFitem().getTitle()+" "+pItem.getFitem().getDocId());
					//标题长的新闻相似度降权使其排序靠后
//					pItem.setSimScore(0.1f);
//					continue;
				}
				
				//本次来自solr的数据 也置为true
				pItem.setRelatedSearchItem(true);
				
				cacheRankList.add(pItem);
			}
			
			//temp 对"白银市"混入 白银 数据做临时处理，等重做数据后同步下线
			if(keyword.getType().equals("loc") && keyword.getName().equals("白银市")){
				LOG.info("特殊处理白银市~");
				cacheRankList = tempLocDataFilterScript(cacheRankList);
			}
			
			if(cacheRankList.isEmpty()){
				LOG.warn("Preload "+channel+" is faild , newslist is empty ~");
			}
			

			
			//时间、热度、相似度序排列
			if(keyword.getType().equals("loc")){
				cacheRankList = NewsSortUtil.sortLocChannelNewsList(cacheRankList);
				LOG.info("sort loc news : "+keyword.getName()+" size : "+cacheRankList.size());
			} else {
//				cacheRankList = NewsSortUtil.sortAlgorithmChannelNewsList(cacheRankList);
				cacheRankList = NewsSortUtil.sortAlgorithmChannelNewsList_new(cacheRankList);
			}
			
			
			//只取前500条
			if(cacheRankList.size()>500){
				cacheRankList = cacheRankList.subList(0, 500);
			}

			
			//将PreloadItem转换成FrontNewsItem
			//此处进行黑名单过滤
			//将本地数据why字段加入北京市地域信息
			//获取频道黑名单Set
			//泛编下线数据在此处过滤，同时在缓存中删除（后续将取代黑名单逻辑）
			//加入solr修改实时生效功能，仅限2天之内(如果从solr中加载的按时间倒序排序最后一条时间大于2天则时限为2天，否则为最后一条时间）
			Set<String> channelBlackSet = blacklistMap.get(channel);
			if(channelBlackSet == null){
				channelBlackSet = new HashSet<String>();
			}
			//获取全站下线黑名单
			Set<String> allBlackSet = blacklistMap.get("all");
			if(allBlackSet == null){
				allBlackSet = new HashSet<String>();
			}
			SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Iterator<PreloadItem> it = cacheRankList.iterator();
			while(it.hasNext()){
				PreloadItem item = it.next();
				//过滤频道黑名单
				if(channelBlackSet.contains(item.getFitem().getDocId()) || channelBlackSet.contains(item.getFitem().getTitle())){
					LOG.info("hit in " + channel + " black List "+item.getFitem().getDocId()+" title : "+item.getFitem().getTitle());
					it.remove();
					continue;
				}
				//过滤全站下线黑名单
				if(allBlackSet.contains(item.getFitem().getDocId()) || allBlackSet.contains(item.getFitem().getTitle())){
					LOG.info("hit in allChannel blacklist "+item.getFitem().getDocId()+" title : "+item.getFitem().getTitle());
					it.remove();
					continue;
				}
				//why字段加入地域信息
				if(keyword.getType().equals("loc")){
					item.getFitem().setWhy(keyword.getName());
				}
				//泛编下线(暂时没用simid，等稳定后加入simid)
				if(offlineSet.contains(item.getFitem().getDocId())){
					LOG.info("hit in offline news set "+item.getFitem().getDocId()+" title : "+item.getFitem().getTitle());
					it.remove();
					continue;
				}

				
				//solr中如果做了修改或者删除操作，则在瑜伽层同时生效  限时为两天以内  (即本次没有命中solr中的数据直接删除)
				if(!item.isRelatedSearchItem()){ //新闻在solr中没用命中
					try {
						Date date =dateFormate.parse(item.getFitem().getDate());
						
						long limitTime = System.currentTimeMillis() - endTime;
						if(limitTime > 2 * 24 * 60 * 60 * 1000){
							limitTime = 2 * 24 * 60 * 60 * 1000;
						}
						
						long time = System.currentTimeMillis() - date.getTime();
						long testOutTime = limitTime / (60 * 60 * 1000); //test 测试输出时间
						//新闻是两天以内的
						if(time < limitTime){
							LOG.info("This item del from solr "+testOutTime+" "+item.getFitem().getDocId()+" title : "+item.getFitem().getTitle()+" "+item.isRelatedSearchItem()+" "+channel);
							it.remove();
							continue;
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						LOG.error(" ", e);
						continue;
					}
				}
				newslist.add(item.getFitem());
			}
			
			
			//test
			if(testKey != null && testKey.equals(keyword.getName())){
				String testStr = gson.toJson(cacheRankList);
				LOG.info("test : "+keyword.getName()+" "+testStr);
			}
			
			LOG.info(channel+" dis size : "+newslist.size());
			String oscacheStr = gson.toJson(cacheRankList);
			osCache.put(channel, oscacheStr);
			
			//temp 本地数据进行编辑指令干预（焦点图+置顶）但不放入缓存及时生效
			if(keyword.getType().equals("loc")){
				List<PreloadItem> editorInstructList = getEditorInstructItems(channel);
				if(editorInstructList != null && !editorInstructList.isEmpty()){
					for(PreloadItem pItem : cacheRankList){
						if(isContainedSimItem(pItem, editorInstructList)){
							LOG.info("hit in editorList : "+pItem.getFitem().getTitle());
							continue;
						}
						editorInstructList.add(pItem);
					}
					newslist.clear();
					for(PreloadItem pItem : editorInstructList){
						newslist.add(pItem.getFitem());
					}
				}
			}
			
			
			String disStr = gson.toJson(newslist);
			disToRedis(channel, 8*60*60*1000, disStr);
//			LOG.info(channel+" "+disStr);
			
		}
		
	}
	
	/**
	 * 取主tag的主名，若有则返回主名随机返回某一领域主名，没有返回null
	 * 
	 * 没有做领域区分
	 * 
	 * @param  List<PreloadItem> tempList
	 * @return  List<PreloadItem> tempList
	 */
	private String getTagMainName(String tag){
		//主别名控制，如果tag有主名，则将主名tag添加到tagmap中，
		AliasLibData aliasLibData = AliasLibData.getInstance();
		
		//判断是否有主名，有的话加入至tagmap中
		Map<String, HashSet<String>> aliasMap = aliasLibData.searchAlias(tag);
		
		if(aliasMap != null){
			Set<String> keySet = aliasMap.keySet();
			for(String key : keySet){
				HashSet<String> valueSet = aliasMap.get(key);
				Iterator<String> it =  valueSet.iterator();
				String mainNameTag = it.next();
				return mainNameTag;
			}
		}
		
		return null;
	}
	
	/**
	 * 临时处理脚本
	 * 本地 “白银市” 新闻临时处理逻辑：
	 * 将7月19日 12:00以前的数据全都过滤掉,等重做数据以后去除该逻辑
	 * 
	 * @param  List<PreloadItem> tempList
	 * @return  List<PreloadItem> tempList
	 */
	private List<PreloadItem> tempLocDataFilterScript(List<PreloadItem> tempList){
		if(tempList == null || tempList.isEmpty()){
			return tempList;
		}
		LOG.info("白银市 size "+tempList.size());
		List<PreloadItem> cacheList = new ArrayList<PreloadItem>();
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = "2016-07-19 10:00:00";
		try {
			long time = formate.parse(date).getTime();
			for(PreloadItem item : tempList){
				String tempDate = item.getFitem().getDate();
				long tempTime = formate.parse(tempDate).getTime();
//				LOG.info("白银市 "+tempTime + " : "+time +" "+item.getFitem().getTitle()+" "+item.getFitem().getDocId());
				if(tempTime > time){
					cacheList.add(item);
					LOG.info("白银市 add"+item.getFitem().getTitle());
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			LOG.error(" ",e);
		}
		
		return cacheList;
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
		//写57redis 后续将会被替换
//		try{
//
//			Jedis jedis = new Jedis("10.90.1.57", 6379, 10000);
//			jedis.select(2);
//			String status = jedis.setex(tableName, (int) validTime, disStr);
//			if(!status.equals("OK")){
//				LOG.error("set status code:"+status);
//			}else{
//				LOG.info("Dis "+tableName+" to redis");
//			}
//		}catch(Exception e){
//			LOG.error("ERROR"+e);
//		}
		//写60redis 集群Master节点，后续只写这一个节点
		try{

			Jedis jedis = new Jedis("10.90.7.60", 6379, 10000);
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
	
	/**
	 * 选择list最后一个item的数据时间
	 * 
	 * 
	 * 
	 * @param String deltableName ： table名称
	 * 		  
	 * 
	 * @return 
	 */
	private long getLastItemTime(List<PreloadItem> preloadList){
		if(preloadList == null || preloadList.isEmpty()){
			return System.currentTimeMillis();
		}
		
//		Collections.sort(preloadList, new Comparator<PreloadItem>() {
//
//			@Override
//			public int compare(PreloadItem o1, PreloadItem o2) {
//				// TODO Auto-generated method stub
//				SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				try {
//					Date d1 = dateFormate.parse(o1.getFitem().getDate());
//					Date d2 = dateFormate.parse(o2.getFitem().getDate());
//					if(d1.before(d2)){
//						return 1;
//					}else if(d1.equals(d2)){
//						return 0;
//					}else{
//						return -1;
//					}
//				} catch (ParseException e) {
//					// TODO Auto-generated catch block
//					LOG.error(" ", e);
//					return -1;
//				}
//			}
//		});
		
		PreloadItem item = preloadList.get(preloadList.size() - 1);
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = dateFormate.parse(item.getFitem().getDate());
			return date.getTime();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error(" ",e);
		}
		return System.currentTimeMillis();
	}
	
	/**
	 * redis删除函数
	 * 
	 * 
	 * 
	 * @param String deltableName ： table名称
	 * 		  
	 * 
	 * @return 
	 */
	private void delFromRedis(String deltableName){
		if(deltableName == null){
			return ;
		}
		//操作57redis 后续将被60redis替换
//		try{
//
//			Jedis jedis = new Jedis("10.90.1.57", 6379, 10000);
//			jedis.select(2);
//			String cache = jedis.get(deltableName);
//			if(cache != null){
//				jedis.del(deltableName);
//				LOG.info("Del "+deltableName+" from redis");
//			} else {
//				LOG.info("Already del from redis : "+deltableName);
//			}
//		}catch(Exception e){
//			LOG.error("ERROR"+e);
//		}
		
		//操作60redis
		try{

			Jedis jedis = new Jedis("10.90.7.60", 6379, 10000);
			jedis.select(2);
			String cache = jedis.get(deltableName);
			if(cache != null){
				jedis.del(deltableName);
				LOG.info("Del "+deltableName+" from redis");
			} else {
				LOG.info("Already del from redis : "+deltableName);
			}
		}catch(Exception e){
			LOG.error("ERROR"+e);
		}
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
			if(pitem.getFitem().getSimId()!=null && item.getFitem().getSimId() != null){
				if(pitem.getFitem().getSimId().equals(item.getFitem().getSimId())){
					return true;
				}
			}
			
			//标题近似排重，现在统一用simid，此逻辑已过时
//			if(AdjStringsIsSim.isSimStr(item.getFitem().getTitle(), pitem.getFitem().getTitle())){
//				return true;
//			}
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
		//（temp） 更新缓存中simId 新旧切换时期使用  后续可以去掉
		oldItem.getFitem().setSimId(newItem.getFitem().getSimId());
		//更新旧版相似得分 到itemf.score字段
		oldItem.getFitem().setScore(newItem.getFitem().getScore());
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
	private List<PreloadItem> getPreloadNewsItem(KeyWord keyword){
		List<PreloadItem> preloadItemList = new ArrayList<PreloadItem>();
		if(keyword == null){
			return null;
		}
		int rows = 500;//预加载数据量
		String channel = keyword.getName();
		
		channel = escapeQueryChars(channel);
		String queryStr = null;
		LOG.info("keyword type:"+keyword.getType()+"keyword name"+keyword.getName());
		//区分是否为稿源Tag
		if(keyword.getType().equals("swm")){
			queryStr = "source:("+channel+")";
		}else if("科技".equals(channel)||"/ 科技".equals(channel)||"/科技".equals(channel)||"文化".equals(channel)){
			queryStr="relatedfeatures:("+channel+")";
		}else if("美容".equals(channel)||"美体".equals(channel)||"商业".equals(channel)||"养生".equals(channel)){
			queryStr = "topic1:("+channel+") OR topic2:("+channel+") OR topic3:("+channel+")";
		}else if(keyword.getType().equals("c")){
			queryStr = "topic1:("+channel+") OR relatedfeatures:("+channel+")";
			LOG.info("c type solr query:"+channel);
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
	private SolrQuery generaterSolrQuery(String queryStr,int rows){
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		query.setRows(rows);
		query.addSort("date",SolrQuery.ORDER.desc);
		query.set("fl", "*,score");
		
		//test 8081全新公式
		query.set("simi", "tfonly");
		query.set("defType", "payload");
		
		//过滤条件	(本地数据不加该过滤) drop 
		if(queryStr.indexOf("loc=") < 0){
			query.addFilterQuery("other:(ifeng OR yidian OR wemedia -illegal )");
		} else {
			query.addFilterQuery("other:(-ifengvideo)");
		}
		
		//数据全部要加入过滤条件
//		
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
//	private static Map<String, Set<String>> getBlacklistMap(){
//		HashMap<String, Set<String>> blacklistmap = new HashMap<String, Set<String>>();
//		blacklistmap.put("title", new HashSet<String>());
//		blacklistmap.put("id", new HashSet<String>());
//		List<String> blacklist = BlackListData.getInstance().get_blacklist();
//		Pattern pattern = Pattern.compile("[0-9]*"); 
//		  
//		if(blacklist != null && !blacklist.isEmpty()){
//			for(String str : blacklist){
//				Matcher isNum = pattern.matcher(str);
//				//纯数字组合识别成id
//				if(isNum.matches()){
//					blacklistmap.get("id").add(str);
//				}else {
//					blacklistmap.get("title").add(str);
//				}
//			}
//		}
//		return blacklistmap;
//	}
	
	
	/**
	 * 用于处理实时获取黑名单数据
	 * 
	 * key=作用域即channel字段 all代表全部下线
	 * value Set 包含title和id两个下线字段
	 * 注意：
	 * 
	 * 返回的map中get("all")或者get(channel)可能为空 注意做处理
	 * 
	 * @param 
	 * 
	 * @return Map<String, Set<String>>
	 * 
	 */
	private static Map<String, Set<String>> getBlacklistMap(){
		HashMap<String, Set<String>> blacklistmap = new HashMap<String, Set<String>>();
		
		List<String> blacklist = BlackListData.getInstance().get_blacklist();
		  
		if(blacklist != null && !blacklist.isEmpty()){
			for(String str : blacklist){
				//兼容旧系统，actionField:all或者不带该字段的均为全站下线
				if(str.contains("actionField")){
					String[] strSplit = str.split("\\|");
					String channel = strSplit[1].replaceAll("actionField:", "");
					Set<String> tempSet = blacklistmap.get(channel);
					if(tempSet == null){
						tempSet = new HashSet<String>();
						tempSet.add(strSplit[0]);
						blacklistmap.put(channel, tempSet);
					} else {
						tempSet.add(strSplit[0]);
					}
				} else {
					Set<String> tempSet = blacklistmap.get("all");
					if(tempSet == null){
						tempSet = new HashSet<String>();
						tempSet.add(str);
						blacklistmap.put("all", tempSet);
					} else {
						tempSet.add(str);
					}
				}
			}
		}
		return blacklistmap;
	}
	
	
	
	/**
	 * 获取编辑焦点图和置顶指令函数
	 * 
	 * 
	 * 
	 * 注意： 先仅用于本地频道北京市
	 * 
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	private List<PreloadItem> getEditorInstructItems(String channel){
		List<PreloadItem> pItemList = new ArrayList<PreloadItem>();
		String reqUrl = "http://api.iclient.ifeng.com/getLocalRecomandNews?region="+channel;
		String content = DownloadPageUtil.downloadPageByRetry(reqUrl, "UTF-8", 3);
		Gson gson = new Gson();
		try{
			if(content != null && content.indexOf("title")>0){
				SimpleDateFormat formate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				SimpleDateFormat formate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				List<EditorInstructItem> itemList = gson.fromJson(content, new TypeToken<List<EditorInstructItem>>() {
				}.getType());
				LOG.info("Loading "+channel+" EditorInstruct success ");
				for(EditorInstructItem eitem : itemList){
					FrontNewsItem fitem = new FrontNewsItem();
					fitem.setDocId(eitem.getDocumentId());
					String time = null;
					try{
						time = formate1.format(formate.parse(eitem.getUpdateTime()));

					} catch (Exception e){
						LOG.error(" Parser editor LocalNews time error ~"+eitem.getDocumentId());
					}
					fitem.setDate(time);
					fitem.setTitle(eitem.getTitle());
					fitem.setDocType(eitem.getType());
					fitem.setWhy(channel);
					fitem.setOthers("instruct="+eitem.getInstruct());
					PreloadItem pItem = new PreloadItem();
					pItem.setFitem(fitem);
					pItemList.add(pItem);
				}
			}
		} catch (Exception e){
			LOG.error("Loading EditorInstruct error ~ "+channel, e);
		}
		return pItemList;
	}
//	public  List<PreloadItem> testPvSort() {
//		KeyWord llKeyWord=new KeyWord();
//		llKeyWord.setName("科技");
//		llKeyWord.setState(0);
//		llKeyWord.setType("c");
//		List<PreloadItem> solrLoadNewsItem = new ArrayList<PreloadItem>();
//		solrLoadNewsItem = getPreloadNewsItem(llKeyWord);
//		RankScoreUtil tpRankScoreUtil=new RankScoreUtil();
//		tpRankScoreUtil.updatePVMap();
//		RankScoreUtil.getInstance().setRankscore(solrLoadNewsItem,"科技");
//		return solrLoadNewsItem = NewsSortUtil.sortAlgorithmChannelNewsList_new(solrLoadNewsItem);
//	}
//	public static void main(String[] args) {
//		AlgPreloadProcess testAlgPreloadProcess=new AlgPreloadProcess(null, null, null);
//		List<PreloadItem> solrLoadNewsItem=testAlgPreloadProcess.testPvSort();
//		List<FrontNewsItem> newslist = new ArrayList<FrontNewsItem>();
//		for(PreloadItem item:solrLoadNewsItem){
//			newslist.add(item.getFitem());
//		}
////		try {
////			System.setOut(new PrintStream(new FileOutputStream("D:\\output.txt")));
////		} catch (FileNotFoundException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		String disStr =new Gson().toJson(newslist);
//		System.out.print(disStr);
//	}

	
}
