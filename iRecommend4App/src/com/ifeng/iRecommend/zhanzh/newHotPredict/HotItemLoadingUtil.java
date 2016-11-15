/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.newHotPredict;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.ifeng.iRecommend.featureEngineering.dataStructure.*;
import com.ifeng.iRecommend.zhanzh.SolrUtil.SearchItemsFromSolr;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;


/**
 * <PRE>
 * 作用 : 热度信息加载模块，将不同来源采集的热度信息处理成统一的格式（HotRankItem）并缓存在hotmap中
 *   
 * 使用 : 在获取对应热度list前请先调用update方法对数据进行更新
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年7月13日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class HotItemLoadingUtil {
	private static Log LOG = LogFactory.getLog("HotItemLoadingUtil");
	private static HotItemLoadingUtil instance = null;
	//预加载白名单分类配置文件
	private static PropertiesConfiguration config;
//	private static String configPath = "D:/test/testPreload/preloadwhitelist.properties";
	private static String configPath = "/data/zhanzh/HotNewsPredict/conf/preloadwhitelist.properties";
	//用于存储热点信息的hashmap
	private static HashMap<String, List<HotRankItem>> hotmap = new HashMap<String, List<HotRankItem>>();
	//Key
	public static final String IfengSportsPageHotList = "IfengSportsPageHotList";
	public static final String IfengMainpageYaowen = "IfengMainpageYaowen";
	public static final String IfengMainpageSports = "IfengMainpageSports";
	public static final String IfengHackerHotList = "IfengHackerHotList";
	public static final String IfengPCNewsList = "IfengPCNewsList";
	
	public static HotItemLoadingUtil getInstance(){
		if(instance == null){
			instance = new HotItemLoadingUtil();
		}
		return instance;
	}
	
	public HotItemLoadingUtil(){
		//初始化日志信息
		try {
			config = new PropertiesConfiguration();
			config.setEncoding("UTF-8");
			config.load(configPath);
			config.setReloadingStrategy(new FileChangedReloadingStrategy());
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.error("Load properties errror "+e);
		}
	}
	
	
	public void updateAll(){
		updateIfengMainPageHotList();
//		updateIfengSportsPageHotList();
		updateHackerHotList();
//		preloadWhiteList();
//		preloadHighQualityChannelList();
//		preloadLocalNews();
	}
	/**
	 * 返回白名单中keyList
	 * 
	 * @param 
	 * 
	 * @return List<String>
	 */
	public List<String> getWhiteListKey(){
		List<String> whitelist = new ArrayList<String>();
		List<String> list = config.getList("w");
		whitelist.addAll(list);
		return whitelist;
	}
	
	/**
	 * 返回configkeyList
	 * 
	 * @param 
	 * 
	 * @return List<String>
	 */
	public List<String> getConfigKeyList(String key){
		List<String> whitelist = new ArrayList<String>();
		List<String> list = config.getList(key);
		if(list == null||list.isEmpty()){
			return whitelist;
		}
		whitelist.addAll(list);
		return whitelist;
	}
	/**
	 * 预加载优质数据分类
	 * 
	 * @param 
	 * 
	 * @return List<String>
	 */
	public void preloadHighQualityChannelList(){
		HashSet<String> channelSet = new HashSet<String>();
		//加载白名单channel
		List<String> list = config.getList("good");
		channelSet.addAll(list);
		for(String channel : channelSet){
			List<HotRankItem> rankList = getItemListByChannel(channel);
			if(rankList == null || rankList.isEmpty()){
				LOG.warn(channel + " from solr is null ~");
				continue;
			}
			hotmap.put(channel, rankList);
		}
	}
	
	/**
	 * 预加载白名单频道热点数据放入缓存
	 * 
	 * @param 
	 * 
	 * @return void
	 */
	public void preloadWhiteList(){
		HeatPredictUtils heatUtil = HeatPredictUtils.getInstance();
		heatUtil.updatePV();
		HashSet<String> channelSet = new HashSet<String>();
		//加载白名单channel
		List<String> list = config.getList("w");
		channelSet.addAll(list);
		for(String channel : channelSet){
			List<HotRankItem> rankList = getItemListByChannel(channel);
			if(rankList == null || rankList.isEmpty()){
				continue;
			}
			heatUtil.setPVNum(rankList);
			hotmap.put(channel, rankList);
		}
	}
	/**
	 * 预加本地数据放入缓存
	 * 
	 * @param 
	 * 
	 * @return void
	 */
	public void preloadLocalNews(){
		HeatPredictUtils heatUtil = HeatPredictUtils.getInstance();
		HashSet<String> channelSet = new HashSet<String>();
		//加载白名单channel
		List<String> list = config.getList("loc");
		channelSet.addAll(list);
		for(String channel : channelSet){
			List<HotRankItem> rankList = getLocalNewsByChannel(channel);		
			if(rankList == null || rankList.isEmpty()){
				continue;
			}
			heatUtil.setPVNum(rankList);
			hotmap.put(channel, rankList);
			
			//@test临时解决北京和北京市问题，后续需要删除
			if(channel.indexOf("市")>0){
				String tempchannel = channel.substring(0, channel.indexOf("市"));
				List<HotRankItem> tempList = getLocalNewsByChannel(tempchannel);
				if(tempList == null || tempList.isEmpty()){
					continue;
				}
				heatUtil.setPVNum(tempList);
				hotmap.put(channel, tempList);
			}
		}
	}
	
	/**
	 * 在solr直接查询获取channel数据
	 * 
	 * solr中查询topic 1、2、3、relatedfeatures
	 * 
	 * @param String channel
	 * 
	 * @return List<HotRankItem>
	 */
	private List<HotRankItem> getItemListByChannel(String channel){
		List<HotRankItem> rankList = new ArrayList<HotRankItem>();
		if(channel == null){
			return null;
		}
		List<String> contentList = SearchItemsFromSolr.searchFrontItemFromSolr(channel);
		if(contentList==null||contentList.isEmpty()){
			LOG.info("This channel cannot find any Item "+channel);
			return null;
		}
		try{
			Gson gson = new Gson();
			for(String content : contentList){
				FrontNewsItem fitem = gson.fromJson(content, FrontNewsItem.class);
				HotRankItem hot = new HotRankItem(fitem);
				if(isContainSameTitleItem(rankList, hot)){
					continue;
				}
				rankList.add(hot);
			}
			//将前100条返回
//			if(rankList.size()>100){
//				rankList = rankList.subList(0, 100);
//			}
		}catch(Exception e){
			LOG.error("Parser json error ", e);
			return null;
		}
		return rankList;
	}
	
	/**
	 * 在solr直接查询获取本地数据数据
	 * 
	 * solr中查询other
	 * 
	 * @param String channel 城市名
	 * 
	 * @return List<HotRankItem>
	 */
	private List<HotRankItem> getLocalNewsByChannel(String channel){
		List<HotRankItem> rankList = new ArrayList<HotRankItem>();
		if(channel == null){
			return null;
		}
		List<String> contentList = SearchItemsFromSolr.searchLocNewsFromSolr(channel);
		if(contentList==null||contentList.isEmpty()){
			LOG.info("This loc cannot find any Item "+channel);
			return null;
		}
		try{
			Gson gson = new Gson();
			for(String content : contentList){
				FrontNewsItem fitem = gson.fromJson(content, FrontNewsItem.class);
				HotRankItem hot = new HotRankItem(fitem);
				if(isContainSameTitleItem(rankList, hot)){
					continue;
				}
				rankList.add(hot);
			}
		}catch(Exception e){
			LOG.error("Parser json error ", e);
			return null;
		}
		return rankList;
	}
	/**
	 * 标题全匹配排重逻辑
	 * 
	 * @param List<HotRankItem> , HotRankItem 
	 * 
	 * @return boolean
	 */
	private boolean isContainSameTitleItem(List<HotRankItem> itemlist , HotRankItem item){
		if(itemlist == null || itemlist.isEmpty()||item.getTitle()==null){
			return false;
		}
		String title = item.getTitle(); 
		for(HotRankItem hot : itemlist){
			if(hot.getTitle()!=null&&hot.getTitle().equals(title)){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 主动更新凤凰体育频道页面热度list
	 * 
	 * key=IfengSportsPageHotList value=体育频道热点新闻list
	 * 
	 * @param List<HotRankItem>
	 * 
	 * @return 
	 */
	public void updateIfengSportsPageHotList(){
		List<HotRankItem> sportsHotlist = IfengPCVisualInfoCrawler.getIfengSportsPageHotList();
		HeatPredictUtils heatUtils = HeatPredictUtils.getInstance();
		setHotItem(sportsHotlist);
		heatUtils.setCommentsNum(sportsHotlist);
		heatUtils.setHackerNews(sportsHotlist);
		if(sportsHotlist==null||sportsHotlist.isEmpty()){
			LOG.warn("IfengSportsPageHotList update faild ");
			return;
		}
		hotmap.put(IfengSportsPageHotList, sportsHotlist);
		
		LOG.info("update IfengSportsPageHotList success");
	}
	
	

	/**
	 * 主动更新凤凰主页热度list
	 * 
	 * key=IfengMainpageYaowen value=凤首要闻区热点新闻list
	 * key=IfengMainpageSports value=凤首体育区热点新闻list
	 * 
	 * @param List<HotRankItem>
	 * 
	 * @return 
	 */
	public void updateIfengMainPageHotList(){
		Map<String, List<HotRankItem>> mainpageMap = IfengPCVisualInfoCrawler.getIfengMainpageHotMap();
		if(mainpageMap == null || mainpageMap.isEmpty()){
			LOG.warn("IfengMainpageHotList update faild ");
			return;
		}
		HeatPredictUtils heatUtils = HeatPredictUtils.getInstance();
		//要闻区加入hotmap
		List<HotRankItem> yaowen = mainpageMap.get("0");
		setHotItem(yaowen);

		if(yaowen == null||yaowen.isEmpty()){
			LOG.warn("IfengMainpageHotList update faild ");
			return;
		}
		heatUtils.setCommentsNum(yaowen);
		heatUtils.setHackerNews(yaowen);
		hotmap.put(IfengMainpageYaowen,yaowen);
		//体育热点加入hotmap

		List<HotRankItem> sportsList = mainpageMap.get("4");
		setHotItem(sportsList);
		heatUtils.setCommentsNum(sportsList);
		heatUtils.setHackerNews(sportsList);
		hotmap.put(IfengMainpageSports, sportsList);
		LOG.info("update IfengMainpageHotList success");
	}
	
	/**
	 * 主动更新凤凰一级页面二级页面下所有新闻
	 * 
	 * 用于实现新闻唤醒
	 * 
	 * key=IfengPCNews value=凤首要闻区热点新闻list
	 * 
	 * @param List<HotRankItem>
	 * 
	 * @return 
	 */
	public void updateIfengPCnews(){
		List<HotRankItem> pcnewsList = IfengPCVisualInfoCrawler.getIfengPCnewsList();
		setHotItem(pcnewsList);
		hotmap.put(IfengPCNewsList, pcnewsList);
		LOG.info("update IfengPCNewsList success");
	}
	
	
	
	/**
	 * 主动更新根据PV，和Comments数计算的热点头条列表
	 * 
	 * key=IfengMainpageYaowen value=凤首要闻区热点新闻list
	 * 
	 * @param List<HotRankItem>
	 * 
	 * @return 
	 */
	public void updateHackerHotList(){
		HeatPredictUtils heatUtil = HeatPredictUtils.getInstance();
		heatUtil.updateHotList();
		List<HotRankItem> hotlist = heatUtil.getHotRankItemList();
		if(hotlist == null || hotlist.isEmpty()){
			LOG.warn("IfengHackerHotList update faild ");
			return;
		}
		hotmap.put(IfengHackerHotList, hotlist);
		LOG.info("update IfengHackerHotList success ");
	}
	

	
	public List<HotRankItem> getHotRankList(String key){
		if(hotmap==null||hotmap.isEmpty()){
			updateAll();
		}
		List<HotRankItem> ranklist = hotmap.get(key);
		return ranklist;
	}
	
	

	
	
	
	
	/**
	 * 对热点list设置更多信息如（item，commentNum，等）
	 * 
	 * 
	 * 
	 * @param List<HotRankItem>
	 * 
	 * @return 
	 */
	private void setHotItem(List<HotRankItem> hotlist){
		if(hotlist == null || hotlist.isEmpty()){
			return ;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		HeatPredictUtils heatUtil = HeatPredictUtils.getInstance();
		for(HotRankItem hot : hotlist){
			//规范化URL
			if(hot.getUrl() != null){
				if(hot.getUrl().indexOf("#")>=0&&hot.getUrl().indexOf("html")>0){
					hot.setUrl(hot.getUrl().substring(0, hot.getUrl().indexOf("html"))+"html");
				}	
			}
			
			//查询item，若查不到item即未在内容池中命中，暂不做处理,交给上一层使用层处理
			
			itemf item = heatUtil.searchItemf(hot);
			if(item == null){
				LOG.info(hot.getTitle()+" can not found in the pool "+hot.getUrl());
				continue;
			}
			try {
				Date date = format.parse(item.getPublishedTime());
				long lifeTime = System.currentTimeMillis()-date.getTime();
				hot.setLifeTime(lifeTime);
				
				hot.setDocChannel(heatUtil.getItemfChannel(item));
				hot.setDocId(item.getID());
				hot.setTitle(item.getTitle());
				hot.setUrl(item.getUrl());
				hot.setDocType(item.getDocType());
				hot.setPublishTime(item.getPublishedTime());
				
				hot.setItem(item);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error("Parser date error ", e);
				continue;
			}
		}
//		heatUtil.setCommentsNum(hotlist);
//		heatUtil.setHackerNews(hotlist);
	}
	
}
