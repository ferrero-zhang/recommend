/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;

import redis.clients.jedis.Jedis;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.KeyWord;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;
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
 *          1.0          2016年7月28日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class ShoufengChannelPreload implements Runnable{
	private static Log LOG = LogFactory.getLog("ShoufengChannelPreload");
	private OSCache osCache ;
	private PropertiesConfiguration configs;

	
	public ShoufengChannelPreload(OSCache osCache) {
		this.osCache = osCache;
		// 配置文件初始化
		try {
			configs = new PropertiesConfiguration();
			configs.setEncoding("UTF-8");
			configs.load("conf/ShoufengChannel.properties");
			configs.setReloadingStrategy(new FileChangedReloadingStrategy());
			LOG.info("Loading config success ~");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.error("Load properties errror " + e);
		}
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		LOG.info("ShoufengChannelPreload start ~");
		try {
			HashMap<String, List<String>> channelMap = loadingConfigurationTagsMap("channel");
			//频道映射表，根据编辑提供的稿源表（自媒体稿源表）检索至投放列表中
			HashMap<String, List<String>> sourceMap = loadingConfigurationTagsMap("source");
			//手凤原逻辑
			preload(channelMap,sourceMap);
			//加自媒体数据
			preloadForSpecialChannel(sourceMap);
		} catch (Exception e) {
			LOG.info("ShoufengChannelPreload thread error ~"+e.getMessage());
		}
		LOG.info("ShoufengChannelPreload finished ~");
	}
	
	/**
	 * 数据来源分两个渠道
	 * 1、来自tag映射，根据内容体系中的tag，映射到同一频道下，配置文件中样式为channel=军事->国内军事|军事风云,tag数据来源于缓存本地的osCache中
	 * 
	 * 2、来自于solr中source字段检索，根据编辑提供的稿源信息，通过去solr中检索并给相应的加权
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void preload(HashMap<String, List<String>> channelMap,HashMap<String, List<String>> sourceMap){
		if(channelMap.isEmpty()){
			LOG.error("Loading channelmap empty ~");
			return;
		}
		Gson gson = new Gson();
		Set<String> channelSet = channelMap.keySet();
		for(String channel : channelSet){
			LOG.info("Start loading "+channel);
			/*
			 * tag来源的数据
			 */
			List<String> tagList = channelMap.get(channel);
			//存储所有tag映射的内容list
			List<PreloadItem> allTagItemList = loadingDataFromOScacheByTag(tagList, channel); 
			/*
			 * source来源的数据
			 */
			//存储所有根据稿源映射的内容list 优先级比tag映射的高
			List<String> sourceList = sourceMap.get(channel);
			List<PreloadItem> allitemFromSource = loadingDataFromSolrBySouceTag(sourceList, channel);
			
			//来自编辑挑选稿源的数据默认给C即EditorScore设置为4
			for(PreloadItem pItem : allitemFromSource){
				pItem.setEditorScore(4);
			}
			
			//合并两个渠道数据并统一排重
			allitemFromSource.addAll(allTagItemList);
			List<PreloadItem> itemList = filterSimItemBySimId(allitemFromSource); 

			//执行排序处理等逻辑
			List<FrontNewsItem> disItemList=preloadProcess(itemList);
			
			String disStr = gson.toJson(disItemList);
			disToRedis(channel, 8*60*60*1000, disStr);
			//test
			LOG.info(new Gson().toJson(itemList));
		}
	}
	
	/**
	 * 对自媒体数据的添加和处理
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void preloadForSpecialChannel(HashMap<String, List<String>> sourceMap){
		if(sourceMap.isEmpty()){
			LOG.error("Loading sourceMap empty ~");
			return;
		}
		Gson gson = new Gson();
		//从solr中获取自媒体数据
		    List<PreloadItem> allitemFromSource = loadingDataFromSolrBySpecial();
		    
		//将sourceMap数据放入一个sourceSet里
			Set<String> sourceSet=new HashSet<String>();
		    Set<String> set = sourceMap.keySet();
		    for (Iterator<String> i = set.iterator(); i.hasNext();) {
	            List<String> tempList=sourceMap.get(i.next());
	           
	            for(String str:tempList){
	            	sourceSet.add(str);
	            }
	            
	        }
		    
			//来自编辑挑选稿源的数据默认给C即EditorScore设置为4
			for(PreloadItem pItem : allitemFromSource){
				if(sourceSet.contains(pItem.getSource())){
					pItem.setEditorScore(4);
				}
			}
			
			//对自身数据进行sim排重
			List<PreloadItem> itemList = filterSimItemBySimId(allitemFromSource); 
		    
			//执行排序处理等逻辑
			List<FrontNewsItem> disItemList=preloadProcess(itemList);
			
			String disStr = gson.toJson(disItemList);
			disToRedis("凤凰号", 8*60*60*1000, disStr);
			//test
			LOG.info(new Gson().toJson(itemList));
	}
	
	private List<PreloadItem> loadingDataFromSolrBySpecial() {
		String solrUrl = BasicDataUpdateJob.getInstance().getConfigs().getString("solrUrl");
		int rows = 500;//预加载数据量;
		String queryStr = "other:(wemedia)";
		//直接搜索t1 t2 t3 获得的数据
		SolrQuery query = generaterSolrQuery(queryStr, rows);
		List<PreloadItem> preloadItemList = PreloadItemFromSolrUtil.preloadItemFromSolr(query,solrUrl,false);
		
		return preloadItemList;
		
	}

	private  List<FrontNewsItem> preloadProcess(List<PreloadItem> itemList){
		List<FrontNewsItem> disItemList = new ArrayList<FrontNewsItem>();
		if(itemList==null){
			return disItemList;
		}
		//设置编辑打分
		RankScoreUtil.getInstance().setSFEditorScore(itemList);

		//走排序逻辑
		itemList = NewsSortUtil.sortSFChannelNewsList(itemList);
         
		//只取前500条
		if(itemList.size() > 500){
			itemList = itemList.subList(0, 500);
		}

		//将preloadItem 转换成 frontItem
		//过滤所有doc数据，只给docpic和slide数据
		//过滤所有编辑得分为1的数据（即不推荐数据）
		
		Iterator<PreloadItem> it = itemList.iterator();
		while(it.hasNext()){
			PreloadItem pItem = it.next();
			try{
				//有可能会有nullpoint
				if(pItem.getFitem().getDocType().equals("doc")){
					it.remove();
					continue;
				}
				
				if(pItem.getEditorScore() == 1){
					LOG.info(pItem.getFitem().getDocId()+"Score = 1 just remove "+pItem.getFitem().getTitle());
					it.remove();
					continue;
				}
				
			} catch (Exception e){
				LOG.error("SF process Exception", e);
				continue;
			}
			disItemList.add(pItem.getFitem());
		}
		LOG.info("SF preloadProcess finished~");
		return disItemList;
	}
	
	/**
	 * 
	 * 来自tag映射，根据内容体系中的tag，映射到同一频道下，
	 * 配置文件中样式为channel=军事->国内军事|军事风云,
	 * tag数据来源于缓存本地的osCache中
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private List<PreloadItem> loadingDataFromOScacheByTag(List<String> tagList,String channel){
		List<PreloadItem> allitemList = new ArrayList<PreloadItem>();  
		Gson gson = new Gson();
		if(tagList == null || tagList.isEmpty()){
			LOG.warn(channel+" taglist is null");
		} else {
			for(String tag : tagList){
				try {
					String str = (String) osCache.get(tag);
					if(str != null){
						List<PreloadItem> tempnewslist = gson.fromJson(str, new TypeToken<List<PreloadItem>>() {
						}.getType());
						allitemList.addAll(tempnewslist);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOG.error(tag+" cache content is null");
				}
			}
		}
		allitemList = filterSimItemBySimId(allitemList);

		return allitemList;
	}
	
	/**
	 * 
	 * 
	 * 来自于solr中source字段检索，根据编辑提供的稿源信息，通过去solr中检索并给相应的加权
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private List<PreloadItem> loadingDataFromSolrBySouceTag(List<String> sourceList,String channel){
		//solrUrl
		String solrUrl = BasicDataUpdateJob.getInstance().getConfigs().getString("solrUrl");
		List<PreloadItem> allitemFromSource = new ArrayList<PreloadItem>();
		
		if(sourceList != null && !sourceList.isEmpty()){
			for(String source : sourceList){
				List<PreloadItem> tempItemList = getPreloadNewsItem(source, solrUrl);
				if(tempItemList != null){
					allitemFromSource.addAll(tempItemList);
					LOG.info(channel+" source tag "+source+ " size : "+tempItemList.size());
				}
				
			}
		}
		
		allitemFromSource = filterSimItemBySimId(allitemFromSource);
		

		return allitemFromSource;

	}
	
	
	/**
	 * 对List进行simid排重
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private List<PreloadItem> filterSimItemBySimId(List<PreloadItem> simItemList){
		//simId过滤排重
		Set<String> simidFilterSet = new HashSet<String>();
		List<PreloadItem> itemList = new ArrayList<PreloadItem>(); 
		for(PreloadItem item : simItemList){
			if(simidFilterSet.contains(item.getFitem().getSimId())){
				continue;
			}
			itemList.add(item);
			simidFilterSet.add(item.getFitem().getSimId());
		}
		return itemList;
	}
	
	/**
	 * 加载配置文件解析tag映射
	 * 
	 * 配置文件样例 channel=社会->社会|奇闻轶事|社会八卦
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private HashMap<String, List<String>> loadingConfigurationTagsMap(String channel){
		HashMap<String, List<String>> tagsMap = new HashMap<String, List<String>>();
		//加载Tags映射表 社会->社会资讯
		List<String> mapTagsList = configs.getList(channel);
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
		return tagsMap;
		
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
	private List<PreloadItem> getPreloadNewsItem(String channel,String solrUrl){
		List<PreloadItem> preloadItemList = new ArrayList<PreloadItem>();
		if(channel == null){
			return null;
		}
		int rows = 200;//预加载数据量;
//			queryStr = "other:(wemedia)";
		String	queryStr = "source:("+channel+")";
		
		//直接搜索t1 t2 t3 获得的数据
		SolrQuery query = generaterSolrQuery(queryStr, rows);
		
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
		
		
		query.set("simi", "tfonly");
		query.set("defType", "payload");
		
		query.addFilterQuery("other:(-ifengvideo)");
		
		
		return query;
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

		//写60redis 集群Master节点，后续只写这一个节点
		try{

			Jedis jedis = new Jedis("10.90.7.60", 6379, 10000);
			jedis.select(3);
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


	
}
