/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.preloadUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PVItem;
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
 *          1.0          2015年12月11日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class RankScoreUtil {
	private static Log LOG = LogFactory.getLog("RankScoreUtil");
	//用于缓存本轮文章热度
	private HashMap<String, Double> scoreCache = null;
	
	//用于获取redis中实时PV数据
	private HashMap<String,Map<String, Integer>> pvItemMap = null;
	
	//获取手凤编辑对文章打分（不区分频道，每篇文章唯一打分）
	private LinkedHashMap<String, Integer> sfEditorScoreMap = null;
	//上次查询手凤接口的时间
	private static long lastQueryTime = 0;
	
	private static RankScoreUtil instance = new RankScoreUtil();
	

	
	private RankScoreUtil(){
		scoreCache = new HashMap<String, Double>();
		pvItemMap = new HashMap<String,Map<String, Integer>>();
		sfEditorScoreMap = new LinkedHashMap<String, Integer>();
		
	}
	
	public static RankScoreUtil getInstance(){
		return instance;
	}
	
	private synchronized void putScore(String id, double score){
		scoreCache.put(id, score);
	}
	

	
	public void updatePVMap(){
		//redis 读取可能会超时报异常（test）
		try{
			Jedis jedis = new Jedis("10.32.21.62", 6379, 1000);
			jedis.select(12);
			
			HashMap<String,Map<String, Integer>> temppvItemMap = new HashMap<String, Map<String,Integer>>();
			//垂直频道PV
			Map<String, String> tempMap = jedis.hgetAll("clientLogs_srhkey_byhour");
			if(tempMap == null || tempMap.isEmpty()){
				LOG.error("Update PVMap error ~");
				return ;
			}
			//本地频道PV
			Map<String,String> locPVMap = jedis.hgetAll("clientLogs_loc");
			if(locPVMap != null){
				tempMap.putAll(locPVMap);
				LOG.info("LocPVmap Size : "+locPVMap.size());
			} 
			Gson gson = new Gson();
			Iterator<Entry<String, String>> it = tempMap.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, String> entry = it.next();
				String temp = entry.getValue();
				PVItem item = gson.fromJson(temp, PVItem.class);
				temppvItemMap.put(entry.getKey(), item.getId_count());
			}
					
			pvItemMap = temppvItemMap;
			LOG.info("Loading PVMap success ~");
			LOG.info("PVMap size : "+pvItemMap.size());
		} catch (Exception e){
			LOG.error("Error ~ ", e);
		}
	}
	
	public static void main(String[] args){
		RankScoreUtil.instance.updateSFEditorScore();
	}
	
	/**
	 * 更新手凤编辑打分
	 * 
	 * 初始化获取编辑近七天的打分数据
	 * 
	 * 以后每轮刷新至获取当前与上一次时间间隔的打分数据
	 * 
	 * 打分map默认存1w条，如果超出1w条则删除最旧的三分之一数据
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public void updateSFEditorScore(){
		int maxNum = 10000; //得分map最多保存量
		int deletPercent = 3; //删除比率，例：3 即 三分之一
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long startTime = 0 ;
		String startTimeParam = null;
		long endTime = 0;
		String endTimeParam = null;
		long now = System.currentTimeMillis();
		//初始化加载近7天的数据
		if(lastQueryTime == 0){
			lastQueryTime = System.currentTimeMillis() - 7*24*60*60*1000;
//			startTimeParam = formate.format(new Date(lastQueryTime));
		}
		startTime = lastQueryTime;
		endTime = startTime + 5*60*1000; 
		
		while(endTime < now){
			
			
			startTimeParam = formate.format(new Date(startTime));
			endTimeParam = formate.format(new Date(endTime));
			
			updateSFScoreMapByTime(startTimeParam,endTimeParam);
			
			startTime = endTime;
			endTime = startTime + 5*60*1000; //以5分钟为间隔请求接口
		}
		
		if(endTime > now){
			endTime = now;
			startTimeParam = formate.format(new Date(startTime));
			endTimeParam = formate.format(new Date(endTime));
			
			updateSFScoreMapByTime(startTimeParam,endTimeParam);
		}
		
		lastQueryTime = endTime;
		
		
		
		//删除模块
		int deletNum =  sfEditorScoreMap.size()/deletPercent; //超过最大值时删除的量
		if(sfEditorScoreMap.size() > maxNum){
			int j = 0;
			Iterator<Entry<String, Integer>> it = sfEditorScoreMap.entrySet().iterator();
			while(it.hasNext()){
				it.next();
				it.remove();
				if(j >= deletNum){
					break;
				}
				j++;
			}
			LOG.info("SFEditorScore has delet "+deletNum+" items, now size:"+sfEditorScoreMap.size());
		}
		
		LOG.info("Update SFEditorScore success ~ mapSize : "+sfEditorScoreMap.size());
	
	}
	/**
	 * 根据startTimeParam 和 endTimeParam 来更新scoreMap
	 * 
	 * 
	 * 
	 * 
	 */
	private void updateSFScoreMapByTime(String startTimeParam ,String endTimeParam){
		String url = "http://auto.cmpp.ifeng.com/Cmpp/runtime/interface_179.jhtml?startTime="+startTimeParam+"&endTime="+endTimeParam;
		String content = DownloadPageUtil.downloadPageByRetry(url, "UTF-8", 1);
		if(content == null){
			LOG.error("Update sfeditorScore faild , get score content error ~"+url);

			return;
		}
		SFEditorScoreItem sItem = null;
		try{
			sItem = new Gson().fromJson(content, SFEditorScoreItem.class);
		} catch (Exception e){
			LOG.error("Update sfeditorScore faild , toGson error ~"+url);
			return;
		}
		LOG.info("Update SFEditorScore "+url);
		
		List<SFScoreDataItem> dataList = sItem.dataList;
		if(dataList == null || dataList.isEmpty()){
//			LOG.error("Get SFEditorScore error ~ scoremap is null");
			
			return ;
		}
		for(SFScoreDataItem dataItem : dataList){
			sfEditorScoreMap.put(dataItem.id, dataItem.level);
		}
		
	}
	
	
	
	/**
	 * 手凤编辑打分同步列表数据结构
	 * level :10  非常推荐  7 一般推荐  4 推荐  1 不推荐
	 * 
	 * 
	 * 
	 */
	class SFEditorScoreItem{
		List<SFScoreDataItem> dataList;
		String timestamp;
	}
	class SFScoreDataItem{
		String id;
		Integer level;
	}
	/**
	 * 手凤编辑打分查询接口
	 * level :10  非常推荐  7 一般推荐  4 推荐  1 不推荐
	 * 
	 * 默认文章值为2
	 * 
	 */
	public void setSFEditorScore(List<PreloadItem> itemList){
		if(itemList == null || itemList.isEmpty()){
			return ;
		}
		for(PreloadItem item : itemList){
			Integer score = sfEditorScoreMap.get(item.getFitem().getDocId());
			if(score != null){
				item.setEditorScore(score);
			} else if (item.getEditorScore() == 0){
				item.setEditorScore(2);
			}
		}
	}
	
	
	
	
	
	public void setRankscore(List<PreloadItem> itemList,String channel){
		if(itemList == null || itemList.isEmpty()){
			return ;
		}
		//获取该频道下的PV数 
		Map<String, Integer> pvMap = pvItemMap.get(channel);
		if(pvMap == null){
			pvMap = new HashMap<String, Integer>();
		}
		
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		for(PreloadItem item : itemList){
			double hotscore = 0;
			
			
			//根据生命时长判断是否需要请求热度，生命时长在大于2天的默认热度为0不请求热度接口
			long lifeTime = 50*60*60*1000;
			
			try {
				Date date = dateFormate.parse(item.getFitem().getDate());
				lifeTime = System.currentTimeMillis() - date.getTime();
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				LOG.error(" ", e1);
			}
			
			if(lifeTime < 48*60*60*1000){
				Double cacheHotScore = scoreCache.get(item.getFitem().getDocId());
				if(cacheHotScore != null){
					hotscore = cacheHotScore;
				}else{
					try{
						//temp 热度后台改造
						String url = "http://10.32.21.62:8081/HotNews/GetNewsItemHotLevel?docId="+item.getFitem().getDocId()+"&title="+item.getFitem().getTitle();
						String result = HttpRequestUtil.downloadPageByRetry(url, "UTF-8", 1);
//						String result = "0.2";
						if(result != null && result.indexOf("faild")<0){
							hotscore = Double.valueOf(result);
						}
						
					} catch (Exception e){
						LOG.warn("Get "+item.getFitem().getDocId()+" hotscore error : ", e);
					}
					putScore(item.getFitem().getDocId(), hotscore);
				}
			}
			

			double score = 0;
			if(!item.isRelatedSearchItem()){
				score = (double)item.getSimScore()*0.7/10 + (double)hotscore*0.3;
			}else{
				score = (double)item.getSimScore()*0.5/10 + hotscore*0.5;
			}
			item.setHotscore(hotscore);
			item.setRankscore(score);
			
			String id = item.getFitem().getDocId();
			Integer pv = pvMap.get(id);
			if(pv != null){
				item.setPv(pv);
			} else {
				item.setPv(0);
			}
		}
		
	}
	
	public synchronized void clearCacheScoreMap(){
		LOG.info("Before clear CacheScore Map Size : "+scoreCache.size());
		scoreCache.clear();
		LOG.info("After clear CacheScore Map Size : "+scoreCache.size());
//		pvItemMap.clear();
	}
	

}
