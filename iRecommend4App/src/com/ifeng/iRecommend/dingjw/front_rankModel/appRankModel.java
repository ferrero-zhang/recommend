package com.ifeng.iRecommend.dingjw.front_rankModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.google.gson.JsonSyntaxException;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.commen.sms.SmsSend;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.channelsParser;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.likun.hotpredict.CommentsHackerNews;
import com.ifeng.iRecommend.likun.hotpredict.CommentsItem;
import com.ifeng.iRecommend.likun.hotpredict.heatPredict;
import com.ifeng.iRecommend.likun.locationNews.locationExtraction;
import com.opensymphony.oscache.base.NeedsRefreshException;

/**
 * <PRE>
 * 作用 : 
 *   专用于凤凰新闻客户端的文章权重模型，获取编辑挑选的新闻标题信息，缓存item，根据请求调整权重并返回列表
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
 *          1.0          2014-05-13        dingjw          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class appRankModel {
	
	private static final Log LOG = LogFactory.getLog("appRankModel");
	
	private OSCache osCache;
	
	private static final String CACHE_NAME = "OSCACHE_appRankModel";
	
	private static appRankModel instance = new appRankModel();

	public ItemOperation itemOP;
	
	public ItemCache itemcache; // ！！！！新增！！！！id缓存变量
	
	public static appRankModel getInstance(){
		return instance;
	}
	
	private appRankModel() {
		itemOP =  ItemOperation.getInstance();
		
		//oscache键值的存活周期
		int refreshInterval = 365*24*60*1000;
		
		LOG.info("refreshInterval = "+ refreshInterval);
		
		
		// 初始OSCache_MOOD;
		osCache = new OSCache("conf/oscache_appRankModel.properties",CACHE_NAME, refreshInterval);
		//osCache = new OSCache("oscache.properties",CACHE_NAME, refreshInterval);
		
		itemcache = new ItemCache(); // ！！！！新增！！！！ 缓存初始化
		
		LOG.info(CACHE_NAME + " cache creating...");
	}
	
	/**
	 * 比较两个TimeStamp，判断beforeTime是否在afterTime的前n分钟内
	 * 注意：
	 * 
	 * @param beforeTime， afterTime， minutes
	 * @return true or false
	 */
	private boolean withinMinutes(Timestamp  beforeTime, Timestamp  afterTime,
			int minutes) {
		if(beforeTime==null||afterTime==null||minutes<0)
			return false;
		try {
			Calendar before = Calendar.getInstance();
			Calendar after = Calendar.getInstance();
			Date date1 = beforeTime;
			Date date2 = afterTime;
			before.setTime(date1);
			after.setTime(date2);
			after.add(Calendar.MINUTE, -minutes);
			return after.before(before);
		} catch (Exception e) {
			LOG.error("ERROR withinMinutes", e);
			return false;
		}
	}
	
	/**
	 * 规范化url
	 * 注意：
	 * 
	 * @param srcUrl
	 * @return result
	 */
	public String normalizedUrl(String srcUrl)
	{
		String result = srcUrl;
		if(srcUrl.matches(".*html.*"))
		{
			Pattern pattern = Pattern.compile("http.*(s?)html");
			Matcher matcher = pattern.matcher(srcUrl);
			if (matcher.find()) {
				result = matcher.group(0);
			}
		}else {
			result = srcUrl.substring(srcUrl.lastIndexOf("http"));
		}
		result = new String(result.replaceAll("_\\d{1,3}\\.", "_0\\.").trim());
		//result.replaceAll("", "_0.html");
		return result.trim();
	}
	
	/**
	 * 获取统计系统的访问URL，如http://10.32.21.21/zhineng/doc/2013-11-25/1005.log
	 * 注意：因统计系统每十分钟更新一次，所以当前能访问到的是10分钟前的日志，注意preMinutes应设为如10,20,30.。。
	 * 
	 * @param requestUrl 统计系统访问路径
	 * @param preMinutes 提前时间，以分钟为单位
	 * @return srcUrl
	 */
	public String getTongjiURL(String requestUrl, int preMinutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE, -preMinutes);
		String day = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		String timeStr = new SimpleDateFormat("HHmm").format(cal.getTime());
		String readTime = timeStr.substring(0, 3) + "5";
		String srcUrl = requestUrl+day+"/"+readTime+".log";//例如：requestUrl = "http://10.32.21.21/zhineng/doc/2013-11-25/1005.log";
		return srcUrl;
	}
	
	
	
	
	/**
	 * 遍历item集合，查找与目标item的标题或特征词差异度在diff内的item,若存在近似，返回近似的item，否则返回null
	 * 注意：差异度diff在[0,1]间，0是相同，1是不同
	 * 
	 * @param itemSet， targetItemFront， diff
	 * @return targetItemFront
	 */
	public RankItem findSimilarItemBySpecialWords(HashSet<RankItem> itemSet, RankItem targetRankItem, float diff)
	{
		if( itemSet == null || itemSet.isEmpty())
			return null;

		//逻辑上可能只是比较物理地址?后续俊伟再看看
		if( itemSet.contains(targetRankItem))
			return targetRankItem;
		
		for ( RankItem rankitem : itemSet){
			
			//先判定标题是否相似，如果相似，则返回相似
			float rate = commenFuncs.simRate(rankitem.getTitle().trim(), targetRankItem.getTitle().trim());
			if(rate >= (1-diff)){
				LOG.info("found same RankItems by title.diff: "+rate);
				LOG.info("Old item: "+rankitem.getTitle()+" "+rankitem.getUrl()+" "+rankitem.getSpecialWords());
				LOG.info("New item: "+targetRankItem.getTitle()+" "+targetRankItem.getUrl()+" "+targetRankItem.getSpecialWords());
				return rankitem;
			}else{//如果title互相包含，那么也应该认定为相似
				String title1 = rankitem.getTitle().trim().toLowerCase();
				String title2 = targetRankItem.getTitle().trim().toLowerCase();
				if(title1.indexOf(title2)>=0 ||title2.indexOf(title1)>=0){
					LOG.info("found same RankItems by title.indexof: "+rate);
					LOG.info("Old item: "+rankitem.getTitle()+" "+rankitem.getUrl()+" "+rankitem.getSpecialWords());
					LOG.info("New item: "+targetRankItem.getTitle()+" "+targetRankItem.getUrl()+" "+targetRankItem.getSpecialWords());
					return rankitem;
				}
			}
			
//			//有一种情况，标题被编辑修改了，所以我们得查下每一个rankitem的目前正式title，然后再比较下。。。
//			rankitem
			
			
			//再判定特征词相似与否，相似则返回item
			rate = commenFuncs.simRate(rankitem.getSpecialWords().trim().split("\\s"), targetRankItem.getSpecialWords().trim().split("\\s"));
			//System.out.println("diff rate: "+rate);
			if(rate >= (1-diff)){
				LOG.info("found same RankItems by specialwords.diff: "+rate);
				LOG.info("Old item: "+rankitem.getTitle()+" "+rankitem.getUrl()+" "+rankitem.getSpecialWords());
				LOG.info("New item: "+targetRankItem.getTitle()+" "+targetRankItem.getUrl()+" "+targetRankItem.getSpecialWords());
				return rankitem;
			}
			
//			//若其中一个rankitem的内容为空，或两者类型不一，则只判断标题是否相似; 否则判断特征码
//			if( targetRankItem.getItem() == null || targetRankItem.getItem().getContent() == null 
//				|| rankitem.getItem() == null || rankitem.getItem().getContent() == null 
//				|| !rankitem.getDocType().equals("doc")
//				|| !targetRankItem.getDocType().equals("doc")) {
//				//如果标题相似，则返回item
//				rate = commenFuncs.simRate(rankitem.getTitle().trim(), targetRankItem.getTitle().trim());
//				if(rate >= (1-diff)){
//					LOG.info("found same RankItems by title.diff: "+rate);
//					LOG.info("Old item: "+rankitem.getTitle()+" "+rankitem.getUrl()+" "+rankitem.getSpecialWords());
//					LOG.info("New item: "+targetRankItem.getTitle()+" "+targetRankItem.getUrl()+" "+targetRankItem.getSpecialWords());
//					return rankitem;
//				}
//			}else {
//				//如果特征词相似，则返回item
//				float rate = commenFuncs.simRate(rankitem.getSpecialWords().trim().split("\\s"), targetRankItem.getSpecialWords().trim().split("\\s"));
//				//System.out.println("diff rate: "+rate);
//	
//				if(rate >= (1-diff)){
//					LOG.info("found same RankItems by specialwords.diff: "+rate);
//					LOG.info("Old item: "+rankitem.getTitle()+" "+rankitem.getUrl()+" "+rankitem.getSpecialWords());
//					LOG.info("New item: "+targetRankItem.getTitle()+" "+targetRankItem.getUrl()+" "+targetRankItem.getSpecialWords());
//					return rankitem;
//				}
//			}
			
		}
		return null;
	}
	
	/**
	 * 在oscache中，遍历，若存在与rankItem_front近似，返回近似的resultItem，否则返回null
	 * 注意
	 * 
	 * @param rankItem_front
	 * @return resultItem
	 */
	public RankItem findSimilarRankItemInOscache(RankItem rankItem_front)
	{
		if(rankItem_front==null)
			return null;
		
		//遍历oscache中所有item的id和url，看看与新加入的item的id或url是否相同,返回有相同的rankitem集合
		ArrayList<RankItem> sameRankItems = instance.findSameIDorUrl(rankItem_front);
		
		if(!sameRankItems.isEmpty()){//如果有相同ID或url的item
			for(RankItem sameItemR: sameRankItems)
			{
				if(sameItemR!=null) {
					//如果id相同，则返回该item
					if(rankItem_front.getID().equals(sameItemR.getID()))
						return sameItemR;
					else{//若没有相同id，则有相同的url
						//如果是文章页或者专题页，则返回该item
						if((rankItem_front.getUrl().matches(".*html$")
							&&!rankItem_front.getUrl().matches(".*index\\.(s?)html$"))
							||channelsParser.isZhuanti(rankItem_front.getUrl())){
							return sameItemR;
						} else{
							float rate = commenFuncs.simRate(sameItemR.getSpecialWords().trim(), rankItem_front.getSpecialWords().trim());
							if(rate >= 0.4f)//如果相似度于0.4则认为相似
								return sameItemR;
						} 
					}
					
				}
			}
		}
		else {//如果没有相同ID或url
			HashSet<RankItem> itemSet = new HashSet<RankItem>();
			for(String key: instance.getKeys()) {
				RankItem item = instance.get(key);
				itemSet.add(item);
			}
			//查找相似特征词,差异度小于0.3则认为相似
			RankItem similarItem = instance.findSimilarItemBySpecialWords(itemSet, rankItem_front, 0.4f);
			if( similarItem!= null) {
				return similarItem;
			}
		}
		return null;
	}
	
	/**
	 * 以新的itemFront的信息更新oscache中存的rankItem的信息
	 * 注意：
	 * 
	 * @param rankList;
	 * @param  rankItem
	 *  	oscache中的相似rankitem
	 * @param  item_front
	 *   	从前端（统计或者抓取得到）来的相似rankitem
	 * @return
	 */
	public void updateRankItem(RankList rankList, RankItem rankItem,  RankItem rankItem_front)
	{
		//test log
		LOG.info("updateRankItem in oscache,the info is:");
		LOG.info(rankItem.getUrl());
		LOG.info(rankItem.getOthers());
		LOG.info(rankItem.getID());
		LOG.info("--------");
		LOG.info(rankItem_front.getUrl());
		LOG.info(rankItem_front.getOthers());
		LOG.info(rankItem_front.getID());
		LOG.info("--------^");
		//记录信息：先修改实时权重及时间这两个字段
		long currentTime = System.currentTimeMillis();
		

		//如果新增文章是AA级
		if(rankItem_front.getWeight().equals("AA")){
			//如果缓存中的文章不是AA的
			if(!rankItem.getWeight().equals("AA")){
				//删除旧的文章
				rankList.getDelList().add(rankItem);
				instance.del(rankItem.getSpecialWords());
				StringBuffer sbTmp = new StringBuffer();
				sbTmp.append("Update Delete: ").append(rankItem.getTitle())
				.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID());
				LOG.info(sbTmp.toString());
				
			} 
			
			// 再放入新的进oscache，但不放入新增队列newlist
			StringBuffer sbTmp = new StringBuffer();
			instance.add(rankItem_front.getSpecialWords(), rankItem_front);
			sbTmp.append("Update Add: ").append(rankItem_front.getTitle())
					.append(" ").append(rankItem_front.getWeight()).append(" ")
					.append(rankItem_front.getID());
			LOG.info(sbTmp.toString());
			return;
		}
		
		//如果新增的文章不是AA级，而缓存的文章是AA级的，则不作处理
		if(rankItem.getWeight().equals("AA")){
			rankItem.setTimeFromOutSide(currentTime);
			
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("Not Qualified to Update Old: ").append(rankItem.getTitle())
			.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID());
			LOG.info(sbTmp.toString());
			
			sbTmp = new StringBuffer();
			sbTmp.append("Not Qualified to Update New: ").append(rankItem_front.getTitle())
			.append(" ").append(rankItem_front.getWeight()).append(" ").append(rankItem_front.getID());
			LOG.info(sbTmp.toString());
			
			return;
		}

		// 霸道设置：如果是来自编辑的投放行为，那么一定重新投放
		if (rankItem_front.getOthers().indexOf("fromEditor") >= 0) {
			rankList.getDelList().add(rankItem);
			instance.del(rankItem.getSpecialWords());
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("editor change ");
			sbTmp.append("Update Delete: ").append(rankItem.getTitle())
					.append(" ").append(rankItem.getWeight()).append(" ")
					.append(rankItem.getID());
			LOG.info(sbTmp.toString());

			//特殊设置，如果编辑给的item找不到url，那么用rankItem的补充
			if(rankItem_front.getUrl() == null
					|| rankItem_front.getUrl().isEmpty()
					|| rankItem_front.getUrl().equals(" "))
				rankItem_front.setUrl(rankItem.getUrl());
			
			// 再放入新的
			sbTmp = new StringBuffer();
			//如果rankItem_front和rankItem的ID不同，那么需要记录并传递给业务逻辑层，以做历史排重；
			if(!(rankItem_front.getID().equals(rankItem.getID())))
			{
				String others = rankItem_front.getOthers();
				rankItem_front.setOthers(others+"|!|simID="+rankItem.getID());
				LOG.info("add simID="+rankItem.getID());
			}
				
			rankList.getNewList().add(rankItem_front);
			instance.add(rankItem_front.getSpecialWords(), rankItem_front);
			sbTmp.append("Update Add: ").append(rankItem_front.getTitle())
					.append(" ").append(rankItem_front.getWeight()).append(" ")
					.append(rankItem_front.getID());
			LOG.info(sbTmp.toString());
			return;
		}

		//如果新来的有缩略图而旧的没有缩略图，重新投放
		if(rankItem_front.getDocType().equals("docpic")
				&& rankItem.getDocType().equals("doc")){
			rankList.getDelList().add(rankItem);
			instance.del(rankItem.getSpecialWords());
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("Update Delete because pic: ").append(rankItem.getTitle())
			.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID());
			LOG.info(sbTmp.toString());
			
			//如果ID不同，那么加入simIDlist,并且将替换ID传递给业务逻辑层，以做历史排重；
			if(!(rankItem.getID().equals(rankItem_front.getID())))
			{
				rankItem_front.getSimIDList().addAll(rankItem.getSimIDList());
				String others = rankItem_front.getOthers();
				rankItem_front.setOthers(others+"|!|simID="+rankItem.getID());
				LOG.info("add simID="+rankItem.getID());
			}
			
			//再放入新的
			sbTmp = new StringBuffer();
			rankList.getNewList().add(rankItem_front);
			instance.add(rankItem_front.getSpecialWords(), rankItem_front);
			sbTmp.append("Update Add because pic: ").append(rankItem_front.getTitle())
			.append(" ").append(rankItem_front.getWeight()).append(" ").append(rankItem_front.getID());
			LOG.info(sbTmp.toString());
			
			
			
			return;
		}
		
		// 如果outside的weight更大，重新投放；
		if (rankItem.getWeightFromOutside().compareTo(
				rankItem_front.getWeight()) > 0) {
			rankList.getDelList().add(rankItem);
			instance.del(rankItem.getSpecialWords());
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("Update Delete because weight: ").append(rankItem.getTitle())
			.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID());
			LOG.info(sbTmp.toString());
			
			//如果ID不同，那么加入simIDlist,并且将替换ID传递给业务逻辑层，以做历史排重；
			if(!(rankItem.getID().equals(rankItem_front.getID())))
			{
				rankItem_front.getSimIDList().addAll(rankItem.getSimIDList());
				String others = rankItem_front.getOthers();
				rankItem_front.setOthers(others+"|!|simID="+rankItem.getID());
				LOG.info("add simID="+rankItem.getID());
			}
			
			//再放入新的
			sbTmp = new StringBuffer();
			rankList.getNewList().add(rankItem_front);
			instance.add(rankItem_front.getSpecialWords(), rankItem_front);
			sbTmp.append("Update Add because weight: ").append(rankItem_front.getTitle())
			.append(" ").append(rankItem_front.getWeight()).append(" ").append(rankItem_front.getID());
			LOG.info(sbTmp.toString());
			
			
			return;
		}
		
		//如果新增文章有url,而旧文章没有url，则重新投放
		if(rankItem_front.getUrl()!=null 
			&& !rankItem_front.getUrl().isEmpty()
			&& !rankItem_front.getUrl().equals(" ")
			&& (rankItem.getUrl()==null||rankItem.getUrl().isEmpty()|| rankItem.getUrl().equals(" "))){
			
			rankList.getDelList().add(rankItem);
			instance.del(rankItem.getSpecialWords());
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("Update Delete: ").append(rankItem.getTitle())
			.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID());
			LOG.info(sbTmp.toString());
			
			//如果ID不同，那么加入simIDlist,并且将替换ID传递给业务逻辑层，以做历史排重；
			if(!(rankItem.getID().equals(rankItem_front.getID())))
			{
				rankItem_front.getSimIDList().addAll(rankItem.getSimIDList());
				String others = rankItem_front.getOthers();
				rankItem_front.setOthers(others+"|!|simID="+rankItem.getID());
				LOG.info("add simID="+rankItem.getID());
			}
			
			//再放入新的
			sbTmp = new StringBuffer();
			rankList.getNewList().add(rankItem_front);
			instance.add(rankItem_front.getSpecialWords(), rankItem_front);
			sbTmp.append("Update Add: ").append(rankItem_front.getTitle())
			.append(" ").append(rankItem_front.getWeight()).append(" ").append(rankItem_front.getID());
			LOG.info(sbTmp.toString());

			return;
		} 
		
		//如果新加item是专题，且新旧标题的差异度大于0.5，则进行重新投放
		if(rankItem_front.getUrl()!=null 
			&& !rankItem_front.getUrl().equals(" ")
			&& channelsParser.isZhuanti(rankItem_front.getUrl())
			&& commenFuncs.simRate(rankItem.getTitle(), rankItem_front.getTitle()) <= 0.5f)
		{
				rankList.getDelList().add(rankItem);
				instance.del(rankItem.getSpecialWords());
				StringBuffer sbTmp = new StringBuffer();
				sbTmp.append("Update Delete: ").append(rankItem.getTitle())
				.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID());
				LOG.info(sbTmp.toString());
				
				//再放入新的
				sbTmp = new StringBuffer();
				rankList.getNewList().add(rankItem_front);
				instance.add(rankItem_front.getSpecialWords(), rankItem_front);
				sbTmp.append("Update Add: ").append(rankItem_front.getTitle())
				.append(" ").append(rankItem_front.getWeight()).append(" ").append(rankItem_front.getID());
				LOG.info(sbTmp.toString());

				return;
		}
		
		/*
		 * 如果新的和旧的相似，同时新的能找到item，同时新的权重更高，那么执行加权投放
		 * 具体细节是：
		 * 1）旧的执行del投放；
		 * 2）新的执行add投放；
		 */
		Item item = rankItem_front.getItem();
		if(item!=null && item.getID()!=null) {
			//新的权重更高
			if(rankItem.getWeight().compareTo(rankItem_front.getWeight()) > 0)
			{
				//先删除旧的
				rankList.getDelList().add(rankItem);
				instance.del(rankItem.getSpecialWords());
				StringBuffer sbTmp = new StringBuffer();
				sbTmp.append("Weighting Delete: ").append(rankItem.getTitle())
				.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID());
				LOG.info(sbTmp.toString());
				
				//如果ID不同，那么加入simIDlist,并且将替换ID传递给业务逻辑层，以做历史排重；
				if(!(rankItem.getID().equals(rankItem_front.getID())))
				{
					rankItem_front.getSimIDList().addAll(rankItem.getSimIDList());
					String others = rankItem_front.getOthers();
					rankItem_front.setOthers(others+"|!|simID="+rankItem.getID());
					LOG.info("add simID="+rankItem.getID());
				}
				
				//再放入新的
				sbTmp = new StringBuffer();
				rankList.getNewList().add(rankItem_front);
				instance.add(rankItem_front.getSpecialWords(), rankItem_front);
				sbTmp.append("Weighting Add: ").append(rankItem_front.getTitle())
				.append(" ").append(rankItem_front.getWeight()).append(" ").append(rankItem_front.getID());
				LOG.info(sbTmp.toString());
				
			
				
				return;
			}
		}
		
		//如果item的标题或者权重发生变化，我们并不进行重新投放，输出提示
		if ( !rankItem.getWeight().equals(rankItem_front.getWeight())
				|| !rankItem.getTitle().equals(rankItem_front.getTitle())) {
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("Not Qualified to Update Old: ").append(rankItem.getTitle())
			.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID());
			LOG.info(sbTmp.toString());
			
			sbTmp = new StringBuffer();
			sbTmp.append("Not Qualified to Update New: ").append(rankItem_front.getTitle())
			.append(" ").append(rankItem_front.getWeight()).append(" ").append(rankItem_front.getID());
			LOG.info(sbTmp.toString());
		}
		
		//如果ID不同，那么加入simIDlist
		if(!(rankItem.getID().equals(rankItem_front.getID())))
			rankItem.getSimIDList().addAll(rankItem_front.getSimIDList());
		
		rankItem.setTimeStamp(Long.toString(System.currentTimeMillis()/1000));
		instance.add(rankItem.getSpecialWords(), rankItem);
	}
	
	/**
	 * 新的rankitem，插入oscache和newlist
	 * 注意：
	 * 
	 * @param rankList， item_front
	 * @return
	 */
	public void addNewRankItemInRankList(RankList rankList, RankItem rankItem_front)
	{
		//如果newlist超过200个，退出
		if(rankList.getNewList().size() > 2000
				|| rankItem_front == null)	
			return;
		
		Item item = rankItem_front.getItem();
		
		//如果在hbase中，item的title和content有一个不为空，就算有效item；
		if (item != null && (item.getTitle() != null || item.getContent() != null))// 如果在hbase中能找到
		{
			//若文章是AA级的，则不加入新增队列，直接加入缓存，然后再加入候补队列
			if(!rankItem_front.getWeight().equals("AA"))
				rankList.getNewList().add(rankItem_front);
			instance.add(rankItem_front.getSpecialWords(), rankItem_front);
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("Add New Item: ").append(rankItem_front.getTitle())
			.append(" ").append(rankItem_front.getWeight()).append(" ").append(rankItem_front.getID());
			LOG.info(sbTmp.toString());
			
			
		}else{
			String frontUrl = rankItem_front.getUrl();
			//判断不是单页，是多级频道页或者专题页
			if(!frontUrl.matches(".*html$")||frontUrl.matches(".*index\\.(s?)html$"))
			{
				if(!rankItem_front.getWeight().equals("AA"))
					rankList.getNewList().add(rankItem_front);
				instance.add(rankItem_front.getSpecialWords(), rankItem_front);
				StringBuffer sbTmp = new StringBuffer();
				sbTmp.append("Add New Item: ").append(rankItem_front.getTitle())
				.append(" ").append(rankItem_front.getWeight()).append(" ").append(rankItem_front.getID());
				LOG.info(sbTmp.toString());
			}
		}
	
	}
	
	/**
	 * 遍历缓存中所有item的id和url，看看与新加入的item的id和url是否相同,返回有相同的rankItem集合
	 * 注意：
	 * 
	 * @param rankitem_front
	 * @return al_rankItems
	 */
	public ArrayList<RankItem> findSameIDorUrl(RankItem rankitem_front)
	{
		HashSet<String> keySet = instance.getKeys();
		ArrayList<RankItem> al_rankItems = new ArrayList<RankItem>();
		//先判断id是否相同
		String targetItemID = rankitem_front.getID();
		if(keySet.contains(targetItemID))
			al_rankItems.add(instance.get(targetItemID));
		//再判断url是否相同
		for(String key:keySet)
		{
			RankItem rankItem = (RankItem) instance.get(key);				
//			//@Test
//			if(rankItem == null){
//				LOG.error("ERROR:rankItem == null,key = "+key);
//			}
			
			if(rankItem.getUrl()!=null && rankitem_front.getUrl()!=null 
					&& rankItem.getUrl().equals(rankitem_front.getUrl()))
				al_rankItems.add(rankItem);
		}
		return al_rankItems;
	}
	
	/**
	 * 获取存放在redis中的编辑筛选的推送和推薦item
	 * 编辑内容：fromEditor
	   cmpp同步数据:fromCmpp
	        手凤数据:fromIfeng
	        两万条imcp数据:fromImcp
	        客户端数据:fromApp
	 * 注意：
	 * @param appid, 如newspush, recommend
	 * 
	 * @return appitemfront_list
	 */
	public ArrayList<appItemFront> getAppItemsFromPool(String appid, LinkedHashMap<String, Integer> idPvMap){
		
		Jedis jedis = null;
		Set<String> keyset = null;
		try {
			jedis = new Jedis("10.32.21.62",6379,10000);
			jedis.select(2);
			keyset = jedis.keys("*");
			LOG.info("redis size: "+keyset.size());
		}catch (Exception e) {
			LOG.error("ERROR", e);
			return null;
		}
	
		HashSet<appItemFront> appitemfrontCache = itemcache.getItemSet(); // ！！！！新增！！！！从缓存中获取待再次查询的集合
		if(appitemfrontCache == null){
			appitemfrontCache = new HashSet<appItemFront>();
		}
		
		LOG.info("appitemfrontCache size:"+appitemfrontCache.size());
		/*
		 * 取出redis中的数据并与缓存数据合并，统一存入appitemfrontCache
		*/
		for (String key : keyset) {
			String value = null;
			try {
				value = jedis.get(key);
			} catch (Exception e) {
				LOG.error("[ERROR] ", e);
				continue;
			}

			if (value == null || value.isEmpty())
				continue;

			// 删除掉key
			try {
				jedis.del(key);
			} catch (Exception e) {
				LOG.error("[ERROR] ", e);
				continue;
			}

			appItemFront appitemfront = null;
			try {
				appitemfront = JsonUtils.fromJson(value, appItemFront.class);
			} catch (JsonSyntaxException je) {
				LOG.error("[ERROR] ", je);
				continue;
			} catch (IllegalStateException ie) {
				LOG.error("[ERROR] ", ie);
				continue;
			} catch (Exception e) {
				LOG.error("[ERROR] ", e);
				continue;
			}

			if (appitemfront == null) {
				LOG.error("appitemfront == null");
				LOG.error(value);
				continue;
			}

			if (appid != null && !appitemfront.getAppID().equals(appid))
				continue;
			appitemfrontCache.add(appitemfront);
		}
		
		ArrayList<appItemFront> appitemfront_list = new ArrayList<appItemFront>();
		/*
		 * 逻辑处理模块，遍历appitemfrontCahce集合，将未查询到item的项加入缓存，将查询到的item从缓存中删除
		*/
		for(appItemFront appitemfront : appitemfrontCache){
			
			String key = appitemfront.getImcpID();
			itemOP.setItemType(ItemType.APPITEM);
			Item item = itemOP.getItem(appitemfront.getImcpID());
			if (item == null || item.getTitle() == null) {
				LOG.info("Cannot find this item in hbase: " + key);
				LOG.info("Save to cache..");
				itemcache.addToCache(appitemfront); // ！！！！新增！！！！查询不到的加入缓存list
				continue;
			}else{
				itemcache.delete(key);            //！！！！新增！！！！查询到的更新缓存数据，将其从缓存中删除
			}

			// 过滤掉url或者channel中含有“finance*roll”、"news*gundong"的item
			if (item.getUrl() != null&& item.getUrl().matches("^.*(http://?news.*gundong.*|http://?finance.*roll.*)$"))
				continue;
			if (item.getChannel() != null&& item.getChannel().matches("^.*(http://?news.*gundong.*|http://?finance.*roll.*)$"))
				continue;

			// 尽量去修补url
			if (item.getUrl() == null) {
				LOG.info("Cannot find url in hbase: " + key);
				LOG.warn("need do some new work!");
			}

			// @test
			LOG.info("test: others field OK:" + appitemfront.getImcpID() + "|"
					+ appitemfront.getOthers());

			// 新建生成appItemFront
			appitemfront.setUrl(item.getUrl());
			appitemfront.setImgurl("null");
			appitemfront.setTimeStamp(Long.toString(System.currentTimeMillis() / 1000));
			if (idPvMap != null && idPvMap.containsKey(key)) {
				appitemfront.setPv(idPvMap.get(key));
			}

			// addlikun；因为url有时候缺“/”，所以给予补充；
			if (appitemfront.getUrl() != null&& appitemfront.getUrl().startsWith("http:/")&& !(appitemfront.getUrl().startsWith("http://")))
				appitemfront.setUrl(appitemfront.getUrl().replaceFirst("http:","http:/"));

			appitemfront_list.add(appitemfront);
		}
//old code;not cache version change likun,2014/12/12		
//		for(String key: keyset)
//		{
//			String value = null;
//			try {
//				value = jedis.get(key);
//			}catch (Exception e) {
//				LOG.error("[ERROR] ", e);
//				continue;
//			}
//			
//			if(value == null || value.isEmpty())
//				continue;
//	
//			////@test 测试环境部署用；			
//			//删除掉key
//			try {
//				jedis.del(key);
//			}catch (Exception e) {
//				LOG.error("[ERROR] ", e);
//				continue;
//			}
//			
//			appItemFront appitemfront = null;
//			try {
//				appitemfront = JsonUtils.fromJson(value,appItemFront.class);
//			}catch (JsonSyntaxException je){
//				LOG.error("[ERROR] ", je);
//				continue;
//			}catch (IllegalStateException ie){
//				LOG.error("[ERROR] ", ie);
//				continue;
//			}catch (Exception e) {
//				LOG.error("[ERROR] ", e);
//				continue;
//			}
//			
//			if(appitemfront == null){
//				LOG.error("appitemfront == null");
//				LOG.error(value);
//				continue;
//			}
//			
//			if(appid!=null && !appitemfront.getAppID().equals(appid))
//				continue;
//
//			itemOP.setItemType(ItemType.APPITEM);
//			
//			Item item = itemOP.getItem(appitemfront.getImcpID());
//			if (item == null || item.getTitle() == null){
//				LOG.info("Cannot find this item in hbase: "+key);
//				continue;
//			}
//			
//			
//			
//			//过滤掉url或者channel中含有“finance*roll”、"news*gundong"的item
//			if(item.getUrl()!=null && item.getUrl().matches("^.*(http://?news.*gundong.*|http://?finance.*roll.*)$"))
//				continue;
//			if(item.getChannel()!=null && item.getChannel().matches("^.*(http://?news.*gundong.*|http://?finance.*roll.*)$"))
//				continue;
//			
//			//尽量去修补url
//			if(item.getUrl() == null){
//				LOG.info("Cannot find url in hbase: "+key);
//				LOG.warn("need do some new work!");
//			}
//
//			//@test
//			LOG.info("test: others field OK:"+appitemfront.getImcpID()+"|"+appitemfront.getOthers());
//			
//			// 新建生成appItemFront
//			appitemfront.setUrl(item.getUrl());
//			appitemfront.setImgurl("null");
//			appitemfront.setTimeStamp(Long.toString(System.currentTimeMillis() / 1000));
//			if(idPvMap!=null && idPvMap.containsKey(key)){
//				appitemfront.setPv(idPvMap.get(key));
//			}
//
////change likun;2014/12/04;category放此处无用，统一到rankItem层次计算
////			//根据前端映射树，计算category
////			String category = null;
////			if(item.getUrl()!=null && !item.getUrl().isEmpty() && !item.getUrl().equals(" ")){
////				try{
////					category =  channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannelByItem(item, 0).split("-")[0];
////				}catch (Exception e) {
////					LOG.info("Cannot find channel: "+key);
////					LOG.error("[ERROR]", e);
////					category = "notfound";
////				}
////			}	
////			appitemfront.setCategory(category);
//
//			
//			//addlikun；因为url有时候缺“/”，所以给予补充；
//			if(appitemfront.getUrl() != null 
//					&& appitemfront.getUrl().startsWith("http:/")
//					&& !(appitemfront.getUrl().startsWith("http://")))
//				appitemfront.setUrl(appitemfront.getUrl().replaceFirst("http:", "http:/"));
//			
//			appitemfront_list.add(appitemfront);
//		
//		}
		
		jedis = null;
		
		return appitemfront_list;
	}
	
	/**
	 * 获取存放在redis中的编辑筛选的推送和推薦item
	 * 注意：
	 * @param appid, 如newspush, recommend
	 * 
	 * @return appitemfront_list
	 */
	private Set<String> getItemIDFromPool() {
		Set<String> keyset = new HashSet<String>();
		try{
			Jedis jedis = new Jedis("10.32.21.62",6379,10000);
			jedis.select(3);
			keyset = jedis.keys("*");
		}catch (Exception e) {
			LOG.error("[ERROR]", e);
		}
		return keyset;
	}
	
	/**
	 * 对从前端抓下来的一轮item进行排重过滤等处理；
	 * 流程和功能如下：
	 * 1）过滤在首页存在的item，转化成RankItem, 并初步过滤掉不合适的url
	 * 2）对id进行排重；保留权重更高或者标题更长；
	 * 3）根据时间进行进一步过滤，且只保留最近的文章；
	 * 4）进行相似识别；相似item修正排重，使权重更高或者标题更长；
	 * @param rankList 投放数据集
	 * @param appItemFrontList
	 * @param syItems，首页存在的文章
	 * @return filterList
	 */
	public ArrayList<RankItem> filterItems(RankList rankList,ArrayList<appItemFront> appItemFrontList, HashSet<String> syItems)
	{
		//1. 过滤在首页存在的item，转化成RankItem, 并初步过滤掉不合适的url
		ArrayList<RankItem> raw_rankItems = new ArrayList<RankItem>();
		for(appItemFront appItemF: appItemFrontList) {
			//过滤掉首页存在的items
			if(syItems.contains(appItemF.getImcpID()) && !appItemF.getWeight().equals("AA"))
				continue;
			//查询hbase，生成对应的rankitem
			RankItem itemR = new RankItem(appItemF,itemOP);
			Item item = itemR.getItem();	
			//如果在hbase中，item的title和content有一个不为空，就算有效item；否则因为找不到具体item内容，所以丢弃，不再走后续投放流程；
			if (item != null && (item.getTitle() != null || item.getContent() != null))
			{
//				//过滤站外url
//				if(!itemR.getUrl().contains("ifeng.com") || itemR.getUrl().contains("bbs.")){
//					continue;
//				}
				raw_rankItems.add(itemR);
			}else{
				LOG.warn("remove item == null:"+appItemF.getImcpID());
				continue;
			}
		}
			
		//2.对id排重：相同id进行相应处理
		HashMap<String, RankItem> idFilterMap = new HashMap<String, RankItem>();
		for(RankItem itemR : raw_rankItems) {
			RankItem oldItemR = null;
			if ((oldItemR = idFilterMap.get(itemR.getID()))!=null) {
				//设置权重更高
				if (oldItemR.getWeight().compareTo(itemR.getWeight()) > 0) {
					oldItemR.setWeight(itemR.getWeight());
					LOG.info("find same id item in idFilterMap,change weight:");
					LOG.info(oldItemR.getID());
					LOG.info(itemR.getID());
					
				}
				
				//设置title长度更长
				if (itemR.getTitle().length()>oldItemR.getTitle().length()) {
					oldItemR.setTitle(itemR.getTitle());
					LOG.info("find same id item in idFilterMap,change title:");
					LOG.info(oldItemR.getID());
					LOG.info(itemR.getID());
				}
			}else//id不重复
				idFilterMap.put(itemR.getID(), itemR);
		}
		
		//3.根据时间进行进一步过滤
		ArrayList<RankItem> al_rankItems = new ArrayList<RankItem>();
		for(RankItem itemR : idFilterMap.values()) {
			Item item = itemR.getItem();	
			//如果在hbase中，item的title和content有一个不为空，就算有效item；否则因为找不到具体item内容，所以丢弃，不再走后续投放流程；
			if (item != null && (item.getTitle() != null || item.getContent() != null) && item.getDate() != null && !item.getDate().isEmpty())
			{
				//获取创建时间
				Timestamp createTS = null;
				try{
					createTS = Timestamp.valueOf(item.getDate());
				}catch (Exception e){
					LOG.warn("date format: "+item.getDate());
					LOG.error("[ERROR] ", e);
					continue;
				}
				
				 
				Timestamp nowTS = new Timestamp(System.currentTimeMillis());
				//统一过滤掉3天前的
				if(instance.withinMinutes(createTS, nowTS, 3*24*60) == false)
				{
					LOG.warn("remove items 7 days ago:"+itemR.getUrl());
					continue;
				}
				//D level只保留1天内的
				if (itemR.getWeight().equals("D") && (false == instance.withinMinutes(createTS, nowTS, 1*24*60))) {
					LOG.warn("remove D_level items 2 days ago:"+itemR.getUrl());
					continue;
				}
				//过滤抓取错误
				if(item.getTitle() != null
						&& item.getTitle().startsWith("failed"))
				{
					LOG.warn("remove item not right:"+itemR.getUrl());
					continue;
				}
				
				//解析other字段，过滤掉内容特别短的自媒体，防止分类出错::1
				if(itemR.getOthers()!=null && itemR.getOthers().indexOf("wemedialevel")>=0){
					String content = itemR.getItem().getContent();
					if (content != null && content.length() < 192) {
						//@test 输出具体日志，方便做debug
						LOG.info("ignore this item,fromWemedia,content<192:"+itemR.getID());
						continue;
					}
				}
				//解析other字段，过滤掉内容特别短的自媒体，防止分类出错::2
				HashMap<String,String> hm_others = parseItemOthersField(itemR.getOthers());
				String source = hm_others.get("source");
				if (source != null && source.equals("spider")) {
					String wemedialevel =  hm_others.get("wemedialevel");
					if(wemedialevel != null){
						String content = itemR.getItem().getContent();
						if (content != null && content.length() < 192) {
							//@test 输出具体日志，方便做debug
							LOG.info("ignore this item,from wemedia,content<192:"+itemR.getID());
							continue;
						}
					}
					
				}
				
				
				//查找Other字段中的wemedialevel字段，获取自媒体编辑评级
				String wemedialevel = hm_others.get("wemedialevel");
				if(wemedialevel != null){
					//将评级为4的自媒体过滤掉
					if(wemedialevel.equals("4")){
						LOG.info("ignore this item , wemedialevel = 4"+itemR.getID()+" || "+itemR.getOthers());
						continue;
					}
					//过滤自媒体标题
					String title = itemR.getTitle();
					if(title != null){
						if(title.indexOf("微信号")>0||title.indexOf("订阅号")>0||title.indexOf("公众号")>0){
							LOG.info("ignore this item , title include illegal string "+itemR.getID()+"||"+title);
							continue;
						}
					}
				}
				
				// 解析other字段;先分发数据到天气，因为天气不用走投放模型包括近似排重；
				if (source != null && source.equals("spider")) {
					String channel = hm_others.get("channel");
					if (channel != null && channel.equals("weather")) {
						rankList.getWeatherList().add(itemR);
						
						//@test 输出具体日志，方便做debug
						LOG.info("find weather item:"+itemR.getID());	
						continue;
					}
				}
				
				
				//本地其它业务的数据分流
				if (hm_others.containsKey("loc")){// 做地域映射发现
					rankList.getLocList().add(itemR);
					continue;
				}

				al_rankItems.add(itemR);
			}else{
				LOG.warn("remove item == null:"+itemR.getUrl());
				continue;
			}
		}
		
		
		//3.相似item识别和处理；
		HashSet<RankItem> rankItemSet = new HashSet<RankItem>();
		for(RankItem itemR : al_rankItems)
		{
			// 查找有无近似
			RankItem similarItem = instance.findSimilarItemBySpecialWords(
					rankItemSet, itemR, 0.4f);
		
			if (similarItem != null) {//找到近似标题，进行比较和排重
				//如果ID不同，那么判断有图无图，同时加入simIDlist
				if(!(itemR.getID().equals(similarItem.getID())))
				{
					//如果similarItem没有缩略图而itemR有，那么删除similarItem,并且把similar加入SimIDList
					if(itemR.getDocType().equals("docpic")
							&& similarItem.getDocType().equals("doc")){
						rankItemSet.remove(similarItem);
						rankItemSet.add(itemR);
						itemR.getSimIDList().addAll(similarItem.getSimIDList());
						//@Test
						LOG.warn( "rankItem simIDList 0:"+itemR.getSimIDList().toString() );
						
						continue;
					}
					
					similarItem.getSimIDList().addAll(itemR.getSimIDList());
					//@Test
					LOG.warn( "rankItem simIDList 1:"+similarItem.getSimIDList().toString() );
					
				}
				
				
				// 如果权重更高更新权重
				if (similarItem.getWeight().compareTo(itemR.getWeight()) > 0) {
					similarItem.setWeight(itemR.getWeight());
					LOG.info("find similar item in filterMap,change weight:");
					LOG.info(similarItem.getID());
					LOG.info(itemR.getID());

				}
				//如果title长度更长的
				if (itemR.getTitle().length() > similarItem.getTitle().length()) {
					similarItem.setTitle(itemR.getTitle());
					LOG.info("find similar item in filterMap,change title:");
					LOG.info(similarItem.getID());
					LOG.info(itemR.getID());
				}
				
			}else//没有找到相同和相似的item，放入filterMap
				rankItemSet.add(itemR);	
		}
		
		
		//把哈希集合转换成ArrayList，
		ArrayList<RankItem> filterList = new ArrayList<RankItem>();
		Iterator<RankItem> it = rankItemSet.iterator();
		while(it.hasNext()) {
			filterList.add(it.next());
		}
	
		return filterList;
	}
	
	/**
	 * 遍历进行过排重过滤后的rankItem列表，把新的item和权重变化的item加入oscache；
	 * 取每个频道pv最高的item加入backupList；
	 * 注意：
	 * 
	 * @param rankList， cacheSet， itemFrontList
	 * @return rankList
	 */
	public int traverFront(RankList rankList, ArrayList<RankItem> rankItemFrontList) {

		if(rankList == null)
			return -1;
		
		if(rankItemFrontList == null||rankItemFrontList.size()==0)
			return -1;
			
		for(RankItem itemR: rankItemFrontList) {
			try {
				RankItem rankItem = (RankItem) instance.get(itemR
						.getSpecialWords());

				if (rankItem != null) {// 如果oscache中存在rankitem
					instance.updateRankItem(rankList, rankItem, itemR);

					// @Test
					LOG.warn("rankItem simIDList 2:" + rankItem.getID() + " "
							+ rankItem.getSimIDList().toString());

				} else {// 如果oscache中没有
					rankItem = instance.findSimilarRankItemInOscache(itemR);// 查找相似的rankitem
					if (rankItem != null) {// 如果有，则更新
						instance.updateRankItem(rankList, rankItem, itemR);
						// @Test
						LOG.warn("rankItem simIDList 3:" + rankItem.getID()
								+ " " + rankItem.getSimIDList().toString());

					} else {// 如果没有，则新建
						instance.addNewRankItemInRankList(rankList, itemR);
					}
				}

			} catch (Exception e) {
				// TODO: handle exception
				LOG.error("ERROR traverFront", e);
				continue;
			}

		}
			
		return 1;
	}
	
	
	/**
	 * 遍历所有oscache中的item，调整item的权重和吧过期的item以及和首页重复的items删除，并加入到ranklist中
	 * 同时将权重大于等于B且没被降权过的文章，以及图集则加入候补队列
	 * 注意：
	 * 
	 * @param rankList, cacheSet
	 * @return RankList
	 */
	public int traverBack(RankList rankList, HashSet<String> syItems, LinkedHashMap<String, Integer> idPvMap)
	{
		if(rankList == null)
			return -1;
		
		HashSet<String> cacheSet = instance.getKeys();
		
		ArrayList<RankItem> tempbackList_ABC = new ArrayList<RankItem>();
		ArrayList<RankItem> tempbackList_slide = new ArrayList<RankItem>();

		//地域识别器
		locationExtraction locEx = new locationExtraction(); 
		
		//获取hot map
		HashMap<String,Double> hm_hotItemIDs = new HashMap<String,Double>();
		try{
			ArrayList<CommentsItem> hotList = CommentsHackerNews.getHotNewsList();
			if(hotList != null){
				for(CommentsItem citem : hotList){
					hm_hotItemIDs.put(citem.getId(), citem.getScore());
				}
			}
			LOG.info("hotList size="+hotList.size());
		}catch(Exception e){
			LOG.error("not got hotList",e);
		}
		
		//人工删除接口：从本地文件中读出blacklist，投放将执行删除指令；
		HashSet<String> hs_blackList = new HashSet<String>();
		FileUtil blacklistFile = new FileUtil();													//xml文件读取器
		blacklistFile.Initialize(fieldDicts.blackListFileForRankModel, "UTF-8");
		String line = null;						
		while ((line = blacklistFile.ReadLine()) != null) {
			hs_blackList.add(line.trim());
		}
		
		
		for(String key:cacheSet)
		{
			try {
				RankItem rankItem = (RankItem) instance.get(key);
				if(rankItem!=null){

//					//@test,输出85380571\85383239
//					if(rankItem.getID().equals("85539502"))
//							//|| rankItem.getID().equals("85380571"))
//					{
//						LOG.info("t4d:"+rankItem.getID() + rankItem.getTitle()+" "+rankItem.getUrl()+" "+rankItem.getWeight()+" "+rankItem.getWeightFromOutside());		
//					}
//					
//					//@test，输出others字段瞧一瞧
//					LOG.warn(rankItem.getID() + " others = "+rankItem.getOthers());
					
					//人工删除接口：从本地文件中读出需要马上删除的ID并执行删除指令；
					if(hs_blackList.contains(rankItem.getID())){
						rankList.getDelList().add(rankItem);
						instance.del(key);
						StringBuffer sbTmp = new StringBuffer();
						sbTmp.append("blacklist del: ")
								.append(rankItem.getTitle()).append(" ")
								.append(rankItem.getWeight()).append(" ")
								.append(rankItem.getID());
						LOG.info(sbTmp.toString());
						continue;
					}
					

					//更新rankitem的pv
					if(idPvMap!=null && idPvMap.containsKey(rankItem.getID())){
						int pv = idPvMap.get(rankItem.getID());
						rankItem.setPv(pv);
					}
					
					Timestamp createTS = new Timestamp(Long.valueOf(rankItem.getCreateTimeStamp())*1000); 
					Timestamp nowTS = new Timestamp(System.currentTimeMillis());
					int periodUnit = fieldDicts.periodUnit;
					
					
					distributeData(rankList,rankItem,hm_hotItemIDs,locEx);
					
//					//@test
//					if(rankItem.getID().equals("85169522"))
//					{
//						System.out.println("test:"+rankItem.getID());
//						System.out.println("test:"+rankItem.lifetime);
//						System.out.println("test:"+periodUnit);
//						System.out.println("test:"+createTS.toGMTString());
//						System.out.println("test:"+nowTS.toGMTString());
//						
//					}
					
					//对和首页40条重复的item进行删除
					if(syItems.contains(rankItem.getID())){
						rankList.getDelList().add(rankItem);
						instance.del(key);
						StringBuffer sbTmp = new StringBuffer();
						sbTmp.append("Duplicate Delete: ").append(rankItem.getTitle())
						.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID()).append(" ").append(createTS.toString());
						LOG.info(sbTmp.toString());
						continue;
					}
					

					//如果是AA等级，则只判断存活时间，不作降权,若存活则加入backuplist
					if(rankItem.getWeight().equals("AA")){
						
						if(!instance.withinMinutes(createTS, nowTS, rankItem.lifetime*periodUnit)){//如果rankitem超过生命周期，则删除
							rankList.getDelList().add(rankItem);
							instance.del(key);
							StringBuffer sbTmp = new StringBuffer();
							sbTmp.append("Delete AA: ").append(rankItem.getTitle())
							.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID()).append(" ").append(createTS.toString());
							LOG.info(sbTmp.toString());
						}else
							rankList.getBackupList().add(rankItem);
						continue;
					}
					
					/*
					 * 默认删除的都可能经历过降权，如果降过那么必须算出其降到多少档并对应修改权重
					 * 降权目前逻辑是：
					 * B档最高，8个小时的存活期；过4个小时降到C；再过4个小时不降了，直接删除；
					 * C档存活4个小时，不降权，可以直接删除；
					 * D档存活4个小时，不降权，可以直接删除；
					 */
					if(!instance.withinMinutes(createTS, nowTS, rankItem.lifetime*periodUnit)){//如果rankitem超过生命周期，则删除
						rankList.getDelList().add(rankItem);
						instance.del(key);
						StringBuffer sbTmp = new StringBuffer();
						sbTmp.append("Delete: ").append(rankItem.getTitle())
						.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID()).append(" ").append(createTS.toString());
						LOG.info(sbTmp.toString());
						continue;
					}
					
					/*
					 * 查询外部权重，判断是否需要提升hot level；如果需要，那么删除，并重新投放，重新投放的权重是新的，生成时间？
					 * 暂时处理：如果有加权的现象，那么先简单重新投放之，再观察；
					 */
					// 编辑给的文章不作处理
					
					if (rankItem.getOthers().indexOf("fromEditor") < 0)
					{
						heatPredict hp = heatPredict.getInstance();
						String outsideWeightAndBestID = hp.rankOneItemHotLevel(rankItem);
						if(outsideWeightAndBestID!=null && !outsideWeightAndBestID.isEmpty()){
							String[] secs = outsideWeightAndBestID.split("\\s");
							if(secs.length == 2){
								String outsideWeight = secs[0];
								String bestID = secs[1];
								
								if(outsideWeight.compareTo(rankItem.getWeight()) < 0){
									//指令进行删除，只是通知存储计算层；
									rankList.getDelList().add(rankItem);
									//再根据业务逻辑，修正出新的rankItem
									rankItem.weightingTo(outsideWeight);
									
									StringBuffer sbTmp = new StringBuffer();
									sbTmp.append("Weighting Add (traverback): ").append(rankItem.getTitle())
									.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID());
									
//									//20140827 add likun：此处应该有新逻辑,如果命中的是simIDList中其它ID，那么应该重新投放并且换ID
//									if(!bestID.isEmpty() && !bestID.equals(rankItem.getID())){
//										rankItem.setID(bestID);
//										sbTmp.append(".change to best ID: ").append(bestID);
//									}
									
									//指令进行新增
									rankList.getNewList().add(rankItem);
									instance.add(key, rankItem);
									
									LOG.info(sbTmp.toString());
									
									//加入候补；
									if(rankItem.getWeight().compareTo("B") <= 0
											&& !(rankItem.getCategory().equals("auto"))) 
										tempbackList_ABC.add(rankItem);
			
									continue;
								}
							}
						}
					}
					
				
					/*
					 * （默认：hot level == 权重）
					 * 降权，应该考虑item从外部得到的最近的权重(来自抓取或者boss系统统计接口)
					 * 根据item的权重和degraded分量来判断，当前时间应该距离createTime于区间：
					 * [(grade-degraded)*2h, (grade-degraded+1)*2h];
					 */
					//item的总降权长度
					int grade = 'E' - rankItem.getWeight().charAt(0);
			
					if (rankItem.degraded > 0) {
						
						if (!instance.withinMinutes(createTS, nowTS, (grade-rankItem.degraded+1)*fieldDicts.degradePeriod*periodUnit))
						{
							/*
							 * 此处加上一个判断，就是判断20分钟内 outside
							 * weight是否大于当前要降到的逻辑weight，如果大于，那么就不要执行降权；
							 */
							boolean canDegr = true;
							long currentTime = System.currentTimeMillis();
							long lastOutSideTime = rankItem.getTimeFromOutSide();
							
//							//test log
//							LOG.info("test:!");
//							LOG.info(rankItem.getWeightFromOutside());
//							LOG.info("outside time ="+(System.currentTimeMillis()-rankItem.getTimeFromOutSide())/60000);
//							LOG.info(rankItem.getWeight());
//							LOG.info(rankItem.getUrl());
							
							
							if(currentTime < (lastOutSideTime+1000*20*60))
							{
								String lastOutSideWeight = rankItem.getWeightFromOutside();
								//degrToWeight是逻辑上要降权到的weight
								String degrToWeight = String.valueOf((char)(rankItem.getWeight().charAt(0)+(grade-rankItem.degraded+1)));
								LOG.info(rankItem.getID());
								LOG.info(rankItem.getTitle());
								LOG.info("lastOutSideWeight="+lastOutSideWeight+" degrToWeight="+degrToWeight);
								
								if(lastOutSideWeight.compareTo(degrToWeight) < 0)
								{
									canDegr = false;
									LOG.info("not degr because outside weight = "+lastOutSideWeight);
								}
							}
							
							//可以降权
							if(canDegr == true)
							{
								rankList.getDegrList().add(rankItem);
								rankItem.degraded--;
								instance.add(key, rankItem);
								StringBuffer sbTmp = new StringBuffer();
								sbTmp.append("Degrade: ").append(rankItem.getTitle())
								.append(" ").append(rankItem.getWeight()).append(" ").append(rankItem.getID()).append(" others=").append(rankItem.getOthers());
								LOG.info(sbTmp.toString());
								
								
							}
							
							LOG.info("--------");
						}
					}
					
					//筛选backup
					if(rankItem.getDocType().equals("slide")){//如果是图集，也加入候补队列
						tempbackList_slide.add(rankItem);
						LOG.info("Put slide into backlist: "+rankItem.getID()+" "+rankItem.getWeight());
						LOG.info(rankItem.getUrl()+" "+rankItem.getTitle());
						
					} else {

						//B以上，或者fromEditor,或者fromApp，加入候补
						if(!(rankItem.getCategory().equals("auto"))&&((rankItem.getWeight().compareTo("B") == 0 && rankItem.degraded >= grade)
								|| (rankItem.getWeight().compareTo("A") == 0 && rankItem.degraded >= (grade-1))))
							tempbackList_ABC.add(rankItem);
						else if(rankItem.getOthers().indexOf("fromEditor") >= 0){
							tempbackList_ABC.add(rankItem);
						}
					}
					
				}
				else {
					instance.del(key);
				}
			} catch (Exception e) {
				LOG.error("ERROR traverBack:"+key, e);
				continue;
			}	
		}
		//把tempbackList中的item加入backupList, 现取A级、B级、C级的文章，按pv排序
		Collections.sort(tempbackList_slide, new Comparator<RankItem>() {
			public int compare(RankItem item1, RankItem item2) {
				//比较实际的weight
				int grade = 'E' - item1.getWeight().charAt(0);
				int real_W_1 = (item1.getWeight().charAt(0)+(grade-item1.degraded));
				grade = 'E' - item2.getWeight().charAt(0);
				int real_W_2 = (item2.getWeight().charAt(0)+(grade-item2.degraded));
				
				int rt = real_W_2 - real_W_1;
				if(rt < 0)
					return 1;
				if(rt > 0)
					return -1;
				if(rt == 0){
					rt = item2.getPv() - item1.getPv();
					if(rt > 0)
						return 1;
					else if(rt == 0)
						return 0;
					else
						return -1;
				}
				return 0;
			}
		});
		//加入备选list，注意进行同ID排重
		String oldID = "";
		for(RankItem itemR : tempbackList_slide)
		{
			if(itemR.getID().equals(oldID))
				continue;

			rankList.getBackupList().add(itemR);
			oldID = itemR.getID();
		}
		
		Collections.sort(tempbackList_ABC, new Comparator<RankItem>() {
			public int compare(RankItem item1, RankItem item2) {
				//比较实际的weight
				int grade = 'E' - item1.getWeight().charAt(0);
				int real_W_1 = (item1.getWeight().charAt(0)+(grade-item1.degraded));
				grade = 'E' - item2.getWeight().charAt(0);
				int real_W_2 = (item2.getWeight().charAt(0)+(grade-item2.degraded));
				
				int rt = real_W_2 - real_W_1;
				if(rt < 0)
					return 1;
				if(rt > 0)
					return -1;
				if(rt == 0){
					rt = item2.getPv() - item1.getPv();
					if(rt > 0)
						return 1;
					else if(rt == 0)
						return 0;
					else
						return -1;
				}
				return 0;
			}
		});
		
		//加入备选list，注意进行同ID排重
		oldID = "";
		for(RankItem itemR : tempbackList_ABC)
		{
			if(itemR.getID().equals(oldID))
				continue;

			rankList.getBackupList().add(itemR);
			oldID = itemR.getID();
		}
				
		return 1;
	}
	
	/**
	 * 判断rankItem的other字段属性，并分发到天气、地域、季节等相应数据中去；
	 * 注意：
	 * 
	 * @param rankList 数据分发流
	 * @param rankItem
	 * @param hm_hotItemIDs 热点新闻map，做热点新闻识别
	 * @param locationExtraction 地域识别器
	 * @param locEx 
	 * 暂时否决：@return 内容是否可以参与投放模型计算，投放到内容匹配、协同、新闻追踪等算法去；
	 * 天气新闻样例：other : fromImcp|!|loc=北京|!|channel=weather|!|pic=1|1|1
	        地域汽车新闻样例： other : fromImcp|!|loc=北京|!|channel=auto|!|pic=1|1|1
                   游戏新闻样例 ： other : fromImcp|!|channel=games|!|pic=1|1|1
       
	 */
	private void distributeData(RankList rankList, RankItem rankItem, HashMap<String, Double> hm_hotItemIDs, locationExtraction locEx) {
		// TODO Auto-generated method stub
		//如果内容命中了热度，那么加入hotList，并记录hot score；这样dis程序可以根据这个socre重新排序
		ArrayList<String> simList = rankItem.getSimIDList();
		double maxScore = 0;
		Double score = hm_hotItemIDs.get(rankItem.getID());
		if(score != null){
			maxScore = score;
		}
		if(simList != null){
			for(String id:simList){
				score = hm_hotItemIDs.get(id);
				if(score == null
						|| score <= 0)
					continue;
					
				if(score > maxScore)
					maxScore = score;
			}
			
		}
		if(maxScore > 0){
			String others = rankItem.getOthers();
			if(others.indexOf("commentScore")>=0)
			{
				others = others.replaceAll("commentScore=[\\d\\.]{0,32}","commentScore="+maxScore);
				rankItem.setOthers(others);
			}else
				rankItem.setOthers(rankItem.getOthers()+"|!|commentScore="+maxScore);
			rankList.getHotList().add(rankItem);
		}
		
		
		//其它业务的数据分流
		HashMap<String,String> hm_others = parseItemOthersField(rankItem.getOthers());
		String source = hm_others.get("source");
		if(source != null
				&& source.equals("spider"))
		{
			String channel = hm_others.get("channel");
			if(channel == null)
				return;
			//做业务数据分割
			if(channel.equals("auto")){
				rankList.getAutoList().add(rankItem);
			}else if(channel.equals("games")){
				rankList.getGamesList().add(rankItem);
			}
			
//likun del 20151102，将本地数据分割，不投放入rankmodel，所以流程前移；		
//			else if (hm_others.containsKey("loc")){// 做地域映射发现
//				rankList.getLocList().add(rankItem);
//
//			}
		}else {
			if (!(hm_others.containsKey("loc")))// 做地域映射发现
			{
				// 必须是society、mainland、house、finance等四个category的内容，才是地域信息
				// pv得大于10才能做地域新闻
				if (rankItem.getCategory() != null
						&& !(rankItem.getCategory().matches(
								"^.*?(world|finance|mil|hongkong).*?$"))) {
					String loc = locEx.extractLocation(rankItem.getItem());
					if (loc != null && !loc.isEmpty()) {
						// add likun
						// 20150401,特殊处理：一些标题含有“北京”、“中国”、“大陆”的自媒体内容分类不准，使world分成mainland，导致本地数据出错，特殊处理之；
						if (!(loc.equals("北京市")
								&&rankItem.getCategory().equals("mainland")
								&&rankItem.getUrl()!=null
								&&rankItem.getUrl().indexOf("cdn.iclient")>0))
						{
							rankItem.setOthers(new StringBuffer(rankItem
									.getOthers()).append("|!|loc=").append(loc)
									.toString());
							rankList.getLocList().add(rankItem);
						}
					}
				}
				
				//auto\games等频道数据补充
				if (rankItem.getCategory() != null
						&& rankItem.getCategory().equals("games")){
					rankList.getGamesList().add(rankItem);
				}
				if (rankItem.getCategory() != null
						&& rankItem.getCategory().equals("auto")){
					rankList.getAutoList().add(rankItem);
				}
				
				//season\festival等数据补充
				if (rankItem.getTitle().indexOf("秋天")>=0
						||rankItem.getTitle().indexOf("霜降")>=0
						||rankItem.getTitle().indexOf("冬天")>=0
						||rankItem.getTitle().indexOf("立冬")>=0){
					if(rankItem.getCategory() != null
							&& (rankItem.getCategory().indexOf("travel")>=0
                                || rankItem.getCategory().indexOf("history")>=0
                                || rankItem.getCategory().indexOf("health")>=0
                                || rankItem.getCategory().indexOf("fashion")>=0))
						rankList.getSeasonList().add(rankItem);
				}
				
//				if (rankItem.getTitle().indexOf("高考志愿")>=0
//						||(rankItem.getTitle().indexOf("高校")>=0 && rankItem.getTitle().indexOf("排名")>=0)
//						||(rankItem.getTitle().indexOf("毕业")>=0 && rankItem.getTitle().indexOf("旅行")>=0)){
//					
//						rankList.getFestivalList().add(rankItem);
//				}
				
				
			} 
//likun del 20151102，将本地数据分割，不投放入rankmodel，所以流程前移；			
//			else
//				rankList.getLocList().add(rankItem);
		}
	}

	/**
	 * 主要暴露接口，获取权重模型并更新oscache
	 * 注意：
	 * 
	 * @param source 原始文档
	 * @return RankList
	 */
	public RankList getUpdates()
	{
		
		//获取客户端文章的实时pv
		LinkedHashMap<String, Integer> idPvMap = heatPredict.readTongjiApp();
		if(idPvMap == null)
			idPvMap = new LinkedHashMap<String, Integer>();
		
		//获取存放在redis的编辑推荐的items
		ArrayList<appItemFront> appitemList = getAppItemsFromPool("recommend", idPvMap);
		if(appitemList == null)
		{
			LOG.info("appitemList from pool is null");
			return null;
		}
		LOG.info("appitemList from pool size: "+appitemList.size());
		
		//对新的item，通过hot predict模块，生成热度预测
		{	
			heatPredict hp = heatPredict.getInstance();
			hp.updateAPPRankLib();
			hp.updateSFRankLib();
			
			//临时否决，hdfs出错；changelikun 20141223
			hp.updatePCVisualRankLib();
			
			
			//@test
			LOG.info("test:");
			for(appItemFront item: appitemList){
				LOG.info("appitemList before rank:"+item.getImcpID()+" "+item.getTitle()+" "+item.getWeight());
			}
			LOG.info("---------------------");
			
			hp.rankItemsHotLevel(appitemList);
			
			//@test
			LOG.info("test:");
			for(appItemFront item: appitemList){
				LOG.info("appitemList after rank:"+item.getImcpID()+" "+item.getTitle()+" "+item.getWeight());
			}
			LOG.info("---------------------");
		
		}
		
		//获取存放在redis的首页固定出现的items
		HashSet<String> syItems = new HashSet<String>();//getItemIDFromPool());
		LOG.info("Items from sy Size: "+syItems.size());
		
		
		RankList rankList = new RankList();
		//过滤和规范外部来的itemFront列表；同时每个item已经查找到具体hbase中item数据；
		ArrayList<RankItem> rankItemListFromFront = instance.filterItems(rankList,appitemList, syItems);
		LOG.info("rankItemListFromFront Size: "+rankItemListFromFront.size());
		
		//@test
		LOG.info("rankList weather filter:"+rankList.getWeatherList().size());
		
		//按照权重、时间等重新排序；尽量把最好的、最新的内容投放,因为投放内容数量有限制
		Collections.sort(rankItemListFromFront, new Comparator<RankItem>() {
			public int compare(RankItem item1, RankItem item2) {
				int rt = item2.getWeight().compareTo(item1.getWeight());
				if(rt > 0)
					return -1;
				if(rt < 0)
					return 1;
				return 0;
			}
		});
		
		//遍历进行过排重过滤后的rankitemFromFront列表，把新的item和权重变化的item加入oscache
		int check = traverFront(rankList, rankItemListFromFront);
		if(check==1)
			LOG.info("traverFront succeed.");
		else
			LOG.info("traverFront failed.");
		
		//@test
		LOG.info("rankList weather front:"+rankList.getWeatherList().size());
		
		//遍历后台oscache存储的rankitem，加入权重调整的item和过期删除的item,以及删除和首页重复的item
		check = traverBack(rankList, syItems, idPvMap);
		if(check==1)
			LOG.info("traverBack succeed.");
		else
			LOG.info("traverBack failed.");
		
		//@test
		LOG.info("rankList weather back:"+rankList.getWeatherList().size());
		
		HashSet<String> keySet = instance.getKeys();
		LOG.info("KeySet Size: "+keySet.size());
		
		return rankList;
	}
	
	/**
	 * 用于解析appitemfront的other字段
	 * 
	 * 
	 * @param String fromSub|!|weather=北京|!|pic=0|0|0 
	 * @return HasmMap: <weather,北京> <pic,0|0|0>
	 */
	private HashMap<String, String> parseItemOthersField(String itemotherfield){
		HashMap<String, String> itemOtherfieldMap = new HashMap<String, String>();
		if(itemotherfield==null||itemotherfield.length()==0){
			return itemOtherfieldMap;			
		}else{
			if(itemotherfield.indexOf("|!|")>0){
				String[] temps = itemotherfield.split("\\|!\\|");
				for(String str : temps){
					if(str.length()>0&&str.indexOf("=")>0){
						String[] temp2 = str.split("=");
						if(temp2.length==2){
							itemOtherfieldMap.put(temp2[0], temp2[1]);
						}
					}
				}
			}
		}
		return itemOtherfieldMap;
	}
	
	
	/**
	 * 在oscache中添加rankitem
	 * 注意：
	 * 
	 * @param key， rankItem
	 * @return 
	 */
	public void add(String key, RankItem value)
	{
		if(key==null)
			return;
		if(value==null)
			return;
		HashSet<String> keySet = getKeys();
		keySet.add(key);
		String json = "";
		try {
			json = JsonUtils.toJson(value, RankItem.class);
			instance.osCache.put(key, json);
			json = JsonUtils.toJson(keySet, HashSet.class);
			instance.osCache.put("keys", json);
		} catch (Exception e) {
			LOG.error("ERROR add key: "+key);
			LOG.error("ERROR add", e);
			return;
		}
	}
	
	/**
	 * 在oscache中删除item
	 * 注意：
	 * 
	 * @param key，
	 * @return 
	 */
	public void del(String key)
	{
		if(key==null)
			return;
		instance.osCache.remove(key);
		
		HashSet<String> keySet = getKeys();
		keySet.remove(key);
		String json = "";
		try {
			json = JsonUtils.toJson(keySet, HashSet.class);
			instance.osCache.put("keys", json);
		} catch (Exception e) {
			LOG.error("ERROR del key: "+key);
			LOG.error("ERROR del", e);
			return;
		}
	}
	
	/**
	 * 在oscache中获取所有key
	 * 注意：
	 * 
	 * @param 
	 * @return keySet
	 */
	public HashSet<String> getKeys()
	{
		HashSet<String> keySet = new HashSet<String>();
		String jsonSource = "";
		try{
			jsonSource = (String) instance.osCache.get("keys");
			HashSet<String> fromJson = (HashSet<String>) JsonUtils.fromJson(jsonSource, HashSet.class);
			if(fromJson==null)
				return keySet;
			keySet = fromJson;
		}catch(NeedsRefreshException e1)
		{
			return keySet;
		}catch(Exception e)
		{
			LOG.error("ERROR getKeys", e);
			return keySet;
		}
		
		return keySet;
	}
	
	/**
	 * 在oscache中获取value
	 * 注意：
	 * 
	 * @param key，
	 * @return value
	 */
	public RankItem get(String key)
	{
		if(key==null)
			return null;
		RankItem value = null;
		String jsonSource = "";
		try{
			jsonSource = (String) instance.osCache.get(key);
			value = JsonUtils.fromJson(jsonSource, RankItem.class);
		}catch(NeedsRefreshException e1)
		{
			return value;
		}catch(Exception e)
		{
			LOG.error("ERROR get: "+key);
			LOG.error("ERROR get", e);
			return null;
		}
		return value;
	}
	
	
	/**
	 * 清空oscache
	 * 注意：
	 * 
	 * @param 
	 * @return 
	 */
	public void removeAll()
	{
		HashSet<String> cacheSet = instance.getKeys();
		for(String key: cacheSet)
		{
			instance.osCache.remove(key);
		}
		instance.osCache.remove("keys");
		instance.osCache.removeAll();
	}

	/*public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		LinkedHashMap<String, Integer> map = readTongjiApp();
		
		
		
		HashMap<Item, Double> scoreMap = new HashMap<Item, Double>();
		
		for(Entry<String, Integer> entry: map.entrySet()){
			System.out.print(entry.getKey()+" "+entry.getValue()+" ");
			ItemOperation itemOp = appRankModel.instance.itemOP;
			itemOp.setItemType(ItemOperation.ItemType.APPITEM);
			Item item = itemOp.getItem(entry.getKey());
			if(item !=null){
				String date = item.getDate();
				Timestamp creatTS = Timestamp.valueOf(date);
				Timestamp nowTS = new Timestamp(System.currentTimeMillis());
				float hours = (float) (nowTS.getTime() - creatTS.getTime())/3600000;
				int pv = entry.getValue();
				double score = (double) (pv - 1)/Math.pow(hours+2, 1.5);
				scoreMap.put(item, score);
				System.out.println(item.getTitle().replaceAll("_[a-z]+ ", ""));
				//System.out.println(creatTS+" "+hours+" "+score);
			}
			
		}
		ArrayList<Entry<Item, Double>> maplist = new ArrayList(scoreMap.entrySet());
		Collections.sort(maplist, new Comparator<Entry<Item, Double>>() {
			public int compare(Entry<Item, Double> entry1, Entry<Item, Double> entry2) {
				double result = entry2.getValue() - entry1.getValue();
				if(result > 0)
					return 1;
				else if(result == 0)
					return 0;
				else
					return -1;
			}
		});
		
		for(Entry<Item, Double> entry: maplist)
			System.out.println(entry.getKey().getID()+" "+entry.getKey().getTitle().replaceAll("_[a-z]+ ", "")+" "+entry.getValue());
	}*/

}
