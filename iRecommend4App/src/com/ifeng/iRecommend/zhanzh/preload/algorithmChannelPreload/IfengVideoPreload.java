/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.PreloadItemFromSolrUtil;

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
 *          1.0          2016年7月20日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class IfengVideoPreload implements Runnable{
	private Log LOG = LogFactory.getLog("IfengVideoPreload");
	
	private PropertiesConfiguration configs;
	public IfengVideoPreload(PropertiesConfiguration configs) {
		// TODO Auto-generated constructor stub
		this.configs = configs;
	}
	
	public IfengVideoPreload() {
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		preload();
	}
	
	private void preload(){
		List<PreloadItem> itemList = getVideoItemFromSolr();
		if(itemList == null || itemList.isEmpty()){
			LOG.warn("Loading item from solr error ~ itemlist is null ");
			return;
		}
//		itemList = setVideoHotScore(itemList);
		itemList = setVideoHotScore_new(itemList);
		sortVideoItem(itemList);
		
		//将搞笑视频排在靠前位置
		List<PreloadItem> videoList= sortVideoNew(itemList);
//		System.out.println(new Gson().toJson(itemList));
		
		List<FrontNewsItem> list = new ArrayList<FrontNewsItem>();
		for(PreloadItem item : videoList){
			list.add(item.getFitem());
		}
		String disStr = new Gson().toJson(list);
		String key = "凤凰视频热榜";
		disToRedis(key, 8*60*60*1000, disStr);
		LOG.info(new Gson().toJson(videoList));
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
	
	private void sortVideoItem(List<PreloadItem> itemlist){
		Collections.sort(itemlist, new Comparator<PreloadItem>() {

			@Override
			public int compare(PreloadItem o1, PreloadItem o2) {
				// TODO Auto-generated method stub
				if(o1.getRankscore()>o2.getRankscore()){
					return -1;
				}else if(o1.getRankscore()<o2.getRankscore()){
					return 1;
				}else{
					return 0;
				}
			}
		});
	}
	
	private List<PreloadItem> sortVideoNew(List<PreloadItem> itemList) {
		List<PreloadItem> videoHappyList=new ArrayList<PreloadItem>();
		for(PreloadItem item:itemList){
			//获取可读特征
			String readableFeatures=item.getFitem().getReadableFeatures();
			if (null == readableFeatures || 0 == readableFeatures.length()) {
				videoHappyList.add(item);
				continue;
			}
			//将特征根据|！|切分成单个
			String[] featureArray=readableFeatures.split("\\|!\\|");
			//对每个特征进行遍历
			for(int i=0;i<featureArray.length;i++){
				String feature=featureArray[i];
				
				String[] featureItem=feature.split("=");
				if(featureItem.length!=2){
					continue;
				}
//				String featureType=featureItem[0];
				String featureValue=featureItem[1];
				//将cn=搞笑视频之类用户爱看的的选出      "cn".equals(featureType)
				if("搞笑视频".equals(featureValue)||"强奸".equals(featureValue)||"美女".equals(featureValue)||"科学探索".equals(featureValue)
						||"美女视频".equals(featureValue)||"搞笑".equals(featureValue)||"萌宠".equals(featureValue)){
					videoHappyList.add(item);
					break;
				}
			}
		}
		itemList.removeAll(videoHappyList);
		videoHappyList=ListUtils.union(videoHappyList, itemList);
		return videoHappyList;
	}
	
	private List<PreloadItem> setVideoHotScore(List<PreloadItem> itemList){
		HashMap<String, Integer> pvMap = getVideoPvMapFromRedis();
		if(pvMap == null || pvMap.isEmpty()){
			LOG.warn("Loading videoMap Error ~");
		}
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
		
		
		for(PreloadItem item : itemList){
			String other = item.getFitem().getOthers();
			if(other.indexOf("ifengvideo")<0 || other.indexOf("url=")<0){
				LOG.info("this item is not video "+item.getFitem().getDocId());
				continue;
			}
			
			try {
				int fromIndex = other.indexOf("url=") + 4;
				
				//需要做id切割，id存在other字段url字段
				String videoid = other.substring(fromIndex, other.indexOf("|!|", fromIndex));
				
				Integer pv = pvMap.get(videoid);
				if(pv == null){
					pv = 0;
				}
				
				item.setPv(pv);
				long lifeTime = dateFormate.parse(item.getFitem().getDate()).getTime();
				double hackernewsScore = hackerNews(pv, lifeTime);
				
				item.setRankscore(hackernewsScore);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error(" ", e);
				continue;
			} catch (Exception e) {
				// TODO: handle exception
				LOG.error(" ", e);
				continue;
			}
			
			
		}
		
		return itemList;
	}
	
	class TempVideoItem{
		String guid;
		String name;
		String createDate;
		String cpName;
		int duration;
		String playTime;
	}
	
	private TempVideoItem getVideoItemFromInterface(String guid){
		String url = "http://newsvcsp.ifeng.com/vcsp/appData/videoGuid.do?guid="+guid;
		String content = DownloadPageUtil.downloadPageByRetry(url, "UTF-8", 1);
		TempVideoItem vItem = new Gson().fromJson(content, TempVideoItem.class);
		return vItem;
	}
	
	private List<PreloadItem> setVideoHotScore_new(List<PreloadItem> itemList){
		HashMap<String, Integer> pvMap = getVideoPvMapFromRedis();
		if(pvMap == null || pvMap.isEmpty()){
			LOG.warn("Loading videoMap Error ~");
		}
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
		
		for(PreloadItem item : itemList){
			String other = item.getFitem().getOthers();
			if(other.indexOf("ifengvideo")<0 || other.indexOf("url=")<0){
				LOG.info("this item is not video "+item.getFitem().getDocId());
				continue;
			}
			
			try {
				int fromIndex = other.indexOf("url=") + 4;
				
				//需要做id切割，id存在other字段url字段
				String videoid = other.substring(fromIndex, other.indexOf("|!|", fromIndex));
				TempVideoItem vItem = getVideoItemFromInterface(videoid);
				//解决bug 直接为0
				if(vItem==null) {
					item.setRankscore(0);
					continue;
				}
				
				Integer playTime = new Integer(vItem.playTime);
				
				Integer pv = pvMap.get(videoid);
				if(pv == null){
					pv = 0;
				}
				
				item.setPv(playTime);
				long lifeTime = dateFormate.parse(vItem.createDate).getTime();
				lifeTime = System.currentTimeMillis() - lifeTime;
				double hackernewsScore = hackerNews(playTime, lifeTime);
				
				item.setRankscore(hackernewsScore);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error(" ", e);
				continue;
			} catch (Exception e) {
				// TODO: handle exception
				LOG.error(" ", e);
				continue;
			}
			
			
		}
		
		return itemList;
	}
	
	private static double hackerNews(int pv, double lifeTime) {
		double score = 0.0;
		if (pv == 0) {
			return score;
		}
		lifeTime = lifeTime / (1000 * 60 * 60);
		double G = 1.4;     //1.8;将时间比重下调
		double under = Math.pow((lifeTime + 2), G);
		score = ((pv ) / under ) * 10000;
		return score;
	}
	
	private HashMap<String, Integer> getVideoPvMapFromRedis(){
		HashMap<String, Integer> pvMap = new HashMap<String, Integer>();
		
		try{
			Jedis jedis = new Jedis("10.32.21.62", 6379, 1000);
			jedis.select(12);
			Map<String, String> map = jedis.hgetAll("clientLogs_videopv");
			Set<String> keySet = map.keySet();
			for(String key : keySet){
				String value = map.get(key);
				pvMap.put(key, new Integer(value));
			}
		} catch (Exception e){
			LOG.error("Redis error  ", e);
			
		}
		return pvMap;
	}
	
	private List<PreloadItem> getVideoItemFromSolr(){
		List<PreloadItem> videoItemList = new ArrayList<PreloadItem>();
		
//		String solrUrl = configs.getString("solrUrl");
		String solrUrl = "http://10.32.28.119:8082/solr46/item/";
		
		String queryStr = "other:ifengvideo AND available:true";
		SolrQuery query = generaterSolrQuery(queryStr, 500);
		videoItemList = PreloadItemFromSolrUtil.preloadItemFromSolr(query, solrUrl, true);

		return videoItemList;
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
		
		return query;
	}
	
	public static void main(String[] args){
		new IfengVideoPreload().run();
	}

}
