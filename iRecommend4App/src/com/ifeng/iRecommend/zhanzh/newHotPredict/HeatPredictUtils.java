/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.newHotPredict;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.iRecommend.featureEngineering.dataStructure.*;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.zhanzh.Utils.AdjStringsIsSim;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;


/**
 * <PRE>
 * 作用 : 热度预测的工具包
 *      1、负责获取实时PV信息，以及对item设置评论数和hackernews得分
 *      2、对外提供根据HotRankItem查询itemf的接口
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :在使用接口前请先调用update()
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年7月8日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class HeatPredictUtils {
	private static Log LOG = LogFactory.getLog("HeatPredictUtils");
	//存储IDHotRank映射表
	private HashMap<String, HotRankItem> IdHotItemMap ;
	private IKVOperationv2 query = null;
	//根据pv以及评论数等信息进行热度排序的热点list
	private List<HotRankItem> RankItemList;
	private static HeatPredictUtils instance = new HeatPredictUtils();
	private HeatPredictUtils(){
		IdHotItemMap = new HashMap<String, HotRankItem>();
		RankItemList = new ArrayList<HotRankItem>();
		String tablename = "appitemdb";
		query = new IKVOperationv2(tablename);
	}
	public static HeatPredictUtils getInstance(){
		return instance;
	}
	/**
	 * PV数据更新接口
	 * 
	 * 更新PV信息以及RankItemList
	 * 
	 * 注意：使用setPVNum请先调用update()接口
	 * 
	 * @param null
	 *            
	 * @return null
	 * 
	 */
	public void updatePV(){
		List<HotRankItem> rankList = parserPVLog();
		if(rankList == null||rankList.isEmpty()){
			LOG.warn("Update hotlist error , rankList is null ~");
			LOG.info("now IdHotItemMap size : "+IdHotItemMap.size());
			return ;
		}
		//每次更新清除旧数据
		IdHotItemMap.clear();
		for(HotRankItem hot : rankList){
			IdHotItemMap.put(hot.getDocId(), hot);
		}
		LOG.info("Update PV success , size : "+IdHotItemMap.size());
	}
	
	/**
	 * RankItemList数据更新接口
	 * 
	 * 更新RankItemList
	 * 
	 * 
	 * 
	 * @param null
	 *            
	 * @return null
	 * 
	 */
	public void updateHotList(){
		List<HotRankItem> rankList = parserPVLog();
		if(rankList == null||rankList.isEmpty()){
			LOG.warn("Update hotlist error , rankList is null ~");
			return ;
		}
		setCommentsNum(rankList);
		setHackerNews(rankList);
		
		rankList = sortRankList(rankList);
		
//		Collections.sort(rankList, new Comparator<HotRankItem>() {
//			@Override
//			public int compare(HotRankItem o1, HotRankItem o2) {
//				// TODO Auto-generated method stub
//				if(o1.getHotScore() > o2.getHotScore()){
//					return -1;
//				}
//				if(o1.getHotScore() < o2.getHotScore()){
//					return 1;
//				}
//				return 0;
//			}
//		});
		

		//每次更新清除旧数据
		RankItemList.clear();
		RankItemList.addAll(rankList);
		LOG.info("Update HotList success , size : "+RankItemList.size());
	}
	
	/**
	 * 切换jdk1.7后排序总是报错
	 * Comparison method violates its general contract!
	 * 
	 * 临时排序算法
	 * 
	 * @param null
	 *            
	 * @return List<HotRankItem>
	 * 
	 */
	public List<HotRankItem> sortRankList(List<HotRankItem> rankList){
		List<HotRankItem> list = new ArrayList<HotRankItem>();
		while(rankList.size() > 0){
			HotRankItem Maxitem = null;
			Iterator<HotRankItem> it = rankList.iterator();
			while(it.hasNext()){
				HotRankItem item = it.next();
				if(Maxitem == null || item.getHotScore() > Maxitem.getHotScore()){
					Maxitem = item;
				}
			}
			list.add(Maxitem);
			rankList.remove(Maxitem);
		}
		return list;
	}
	
	
	/**
	 * 获取基于Hackernews计算的热点新闻list
	 * 
	 * 
	 * @param null
	 *            
	 * @return List<HotRankItem>
	 * 
	 */
	public List<HotRankItem> getHotRankItemList(){
		List<HotRankItem> rankList = new ArrayList<HotRankItem>();
		if(RankItemList == null|| RankItemList.isEmpty()){
			LOG.warn("RankItemList is empty , need to update");
			updateHotList();
		}
		rankList.addAll(RankItemList);
		return rankList;
	}
	/**
	 * 从统计接口获取实时PV信息，并且解析成HotRankItem
	 * 
	 * @param null
	 *            
	 * @return List<HotRankItem>
	 * 
	 */
	public List<HotRankItem> parserPVLog(){
		List<HotRankItem> hotList = new ArrayList<HotRankItem>();
		String pvlogurl = "http://tongji.ifeng.com:9090/webtop/loadNews?chnnid=http://&num=10000&tmnum=0";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String pvPage = DownloadPageUtil.downloadPageByRetry(pvlogurl, "UTF-8", 3);
		if(pvPage == null){
			return hotList;
		}
		String[] pvItemList = pvPage.split("\n");
		for(String line : pvItemList){
			 String title = null;
			 String newsurl = null;
			 int pv = 0; 
			 itemf item = null;
			 if(line.indexOf("title")>0&&line.indexOf("url")>0&&line.indexOf("num")>0){
				 
				 title = line.substring(line.indexOf("title=")+7, line.indexOf("url=")-2);
				 newsurl = line.substring(line.indexOf("url=")+5, line.indexOf("num=")-2);
				 String temppv = line.substring(line.indexOf("num=")+5, line.length()-9);
				 pv = Integer.valueOf(temppv);
			 }
			 if(title != null&&newsurl!=null){
				 HotRankItem hot = new HotRankItem();
				 hot.setTitle(title);
				 hot.setUrl(newsurl);
				 item = searchItemf(hot);
				 if(item == null){
					 LOG.info(title+" can not found in item pool "+newsurl);
					 continue;
			 	}
				 try{
					 Date date = format.parse(item.getPublishedTime());

					 long lifeTime = System.currentTimeMillis()-date.getTime();

					 String channel = getItemfChannel(item);
					 if(channel == null){
						 channel = "notopic";
					 }
					 
					 hot.setDocId(item.getID());
					 
					 hot.setPv(pv);
					 hot.setItem(item);
					 hot.setDocType(item.getDocType());
					 hot.setPublishTime(item.getPublishedTime());
					 hot.setCreatTime(date.getTime());
					 hot.setLifeTime(lifeTime);
					 hot.setDocChannel(channel);
					 hotList.add(hot);
				 }catch(Exception e){
					 LOG.error("Save hotRankItem error ", e);
					 continue;
				 }
			 }
		}
		return hotList;
	}
	
	/**
	 * 评论数设置接口(使用批量查询接口)
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param List<HotNewsItem> 
	 *            
	 * @return void
	 */
	public void setPVNum(List<HotRankItem> hotlist){
		if(hotlist == null||hotlist.size()==0){
			LOG.warn("set PV error ,hotlist is null");
			return ;
		}
		if(IdHotItemMap == null || IdHotItemMap.isEmpty()){
			LOG.warn("PVMap is null , need update ~");
			updatePV();
		}
		for(HotRankItem hot : hotlist){
			HotRankItem item = IdHotItemMap.get(hot.getDocId());
			if(item != null){
				hot.setPv(item.getPv());
			}
		}
	}

	/**
	 * 评论数设置接口(使用批量查询接口)
	 * 
	 * 
	 * 
	 * 注意：需要利用commentJson作为数据结构
	 * 
	 * @param List<HotNewsItem> 
	 *            
	 * @return void
	 */
	class commentJson{
		String doc_url;
		int join_count;
		int count;
		int allcount;
	}
	public void setCommentsNum(List<HotRankItem> hotlist){
		if(hotlist == null||hotlist.size()==0){
			LOG.info("set Comments error ,hotlist is null");
			return ;
		}
		int step = 5;
		int index = 0;
		if(hotlist.size()<step){
			step = hotlist.size();
		}
		Gson gson = new Gson();
		while(index < hotlist.size()){
			if((index+step) > hotlist.size()){
				step = hotlist.size() - index;
			}
			List<HotRankItem> tempList = hotlist.subList(index,index+step);
			StringBuffer tempUrl = new StringBuffer("http://comment.ifeng.com/get?job=4&docUrl=");
			for(HotRankItem hot : tempList){
				tempUrl.append(hot.getUrl());
				tempUrl.append("|");
			}
			tempUrl.deleteCharAt(tempUrl.length()-1);
			tempUrl.append("&format=json");
			String content = DownloadPageUtil.downloadPageByRetry(tempUrl.toString(), "UTF-8", 3);
			if(content != null){
				try{
					List<commentJson> comments = gson.fromJson(content, new TypeToken<List<commentJson>>() {
					}.getType());

					for(int i=0;i<comments.size();i++){

						tempList.get(i).setCommentsNum(comments.get(i).join_count);
					}
				}catch (Exception e){
					LOG.error("parser json error",e);
					LOG.info("requery url : "+tempUrl.toString());
					LOG.info("parser content "+content);
					index = index+step;
					continue;
				}
			}
			index = index+step;
		}
	}
	
	/**
	 * 设置HackerNews得分接口
	 * 
	 * 注意：在设置得分前需要为HotRankItem设置评论数
	 * 
	 * @param List<HotRankItem>
	 * @return void
	 */
	public void setHackerNews(List<HotRankItem> hotlist){
		if(hotlist == null||hotlist.size()==0){
			LOG.info("set HackerNews error ,hotlist is null");
			return ;
		}
		for(HotRankItem hot : hotlist){
			//test1:slide数据的评论数与pv数除以5
//			if(hot.getDocType().equals("slide")){
//				hot.setCommentsNum(hot.getCommentsNum()/5);
//				hot.setPv(hot.getPv()/5);
//			}
			//test2:
			double commentsScore = hackerNews(hot.getCommentsNum(), hot.getLifeTime());
			double pvScore = hackerNews(hot.getPv(), hot.getLifeTime());
			hot.setHotScore(commentsScore*0.6+pvScore*0.4);
		}
		
	}
	/**
	 * hackerNews得分计算函数
	 * 注意：G是新闻生命时间权重因子，G越大新闻生命之间对得分的惩罚越高，目前默认是G=1.8
	 * 
	 * @param int 评论参与数 double 新闻生命时间
	 * @return double 返回hackerNews得分
	 */
	private double hackerNews(int pv, double lifeTime) {
		double score = 0.0;
		if (pv == 0) {
			return score;
		}
		lifeTime = lifeTime / (1000 * 60 * 60);
		double G = 1.8; // 1.8;
		double under = Math.pow((lifeTime + 2), G);
		score = (pv - 1) / under;
		return score;
	}
	/**
	 * 解析itemF的feature字段，并返回itemF的一级分类C的特征值，若解析失败返回null
	 * 
	 * @param itemF 
	 * 
	 * @return String 分类或者专题或者优质栏目
	 * 
	 */
	public  String getItemfChannel(itemf item){
		ArrayList<String> featurelist = item.getFeatures();
		if(featurelist == null||featurelist.isEmpty()){
			return null;
		}
		HashMap<String, String> featureMap = paserItemfFeature(item);
		String channel = null;
		if((channel=featureMap.get("c"))!=null){
			
		}else if((channel=featureMap.get("cn"))!=null){
//			channel = "cn="+channel;
//			channel = channel;
			
		}else if((channel=featureMap.get("s1"))!=null){
//			channel = channel;
		}
			
		return channel;
	}
	/**
	 * 解析itemF的feature字段，并返回itemF的特征map
	 * 
	 * @param itemF 
	 * 
	 * @return HashMap<String,String>
	 * 
	 */
	private HashMap<String, String> paserItemfFeature(itemf item){
		HashMap<String, String> FeatureMap = new HashMap<String, String>();
		ArrayList<String> featurelist = item.getFeatures();
		if(featurelist == null||featurelist.isEmpty()){
			return FeatureMap;
		}
		for(int i=1;i<=featurelist.size()-2;i+=3){
			String temp = featurelist.get(i);
			FeatureMap.put(temp, featurelist.get(i-1));
		}
		return FeatureMap;
	}
	
	/**
	 * 从内容池查询itemf数据
	 * 
	 * 1、根据docID查询IKV
	 * 2、根据URL查询IKV
	 * 3、根据title查询IKV
	 * 4、根据title在搜索接口进行近似匹配
	 * 
	 * @param HotRankItem 
	 * 
	 * @return itemf
	 * 
	 */
	public itemf searchItemf(HotRankItem hotItem){
		if(hotItem == null){
			return null;
		}

		itemf item = null;
		//查id
		if(hotItem.getDocId()!=null){
			item = query.queryItemF(hotItem.getDocId(),"c");
			if(item != null){
				return item;
			}
		}
		//查url
		if(hotItem.getUrl()!=null){
			item = query.queryItemF(hotItem.getUrl(),"c");
			if(item != null){
				return item;
			}
		}
		//查title
		if(hotItem.getTitle()!=null){
			item = query.queryItemF(hotItem.getTitle(),"c");
			if(item != null){
				return item;
			}
			LOG.info("not found in the pool by title+url "+hotItem.getTitle());
		}
		//查搜索接口近似匹配
//		if(hotItem.getTitle()!=null){
//			String title = hotItem.getTitle().replaceAll("\\[.*?\\]", "").replaceAll("\\(.*?\\)", "");
//			String querUrl = "http://10.32.21.62:8080/Rec4User/GetSearchResult?key="+title;
//			String content = DownloadPageUtil.downloadPageByRetry(querUrl, "UTF-8", 1);
//			if(content != null && content.indexOf("success\":false")<0){
//				String id = content.substring(content.indexOf("docId\":\"")+8, content.indexOf("\",\"title"));
//				itemf tempItem = query.queryItemF(id);
//				if(tempItem != null){
//					if(tempItem.getTitle()!=null&& AdjStringsIsSim.isSimStr(hotItem.getTitle(), tempItem.getTitle())){
//						item = tempItem;
//						LOG.info("find this itme by search "+tempItem.getTitle());
//						return tempItem;
//					}
//				}
//			}
//		}
		return null;
	}
}
