package com.ifeng.iRecommend.likun.rankModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import redis.clients.jedis.Jedis;

import com.google.gson.JsonSyntaxException;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.HttpRequest;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.commen.blackList.BlackListData;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.featureEngineering.freshProject.FreshItemf;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.likun.hotpredict.heatPredictNew;
import com.ifeng.iRecommend.likun.locationNews.locationExtraction;
import com.opensymphony.oscache.base.NeedsRefreshException;

/**
 * <PRE>
 * 作用 : 
 *   新的内容体系rankmodel，控制所有内容的投放模型；
 *   新的内容体系，主要区别在于特征表达的接口、热度预测的流程、生命周期控制、内容持久化、
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
 *          1.0          2015-04-13        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class rankModel {
	
	private static final Log LOG = LogFactory.getLog("rankModel");
	
	private OSCache osCache;
	
	private static final String CACHE_NAME = "OSCACHE_rankModel";
	
	private static rankModel instance = new rankModel();
	
	private static IKVOperationv2 ob = new IKVOperationv2(IKVOperationv2.defaultTables[0]);
	
	private HashMap<String,String> hm_blacklist;
	
//	public queryInterface itemfOP;
	
	//业务区分ID
	private String appid;
	
	//solr item 库设置
	public static String solrItemDBPath; 
	//redis同步内容池，来自原始投放
	public static int itemsPoolDBInRedis;
	//redis同步内容池，来自log中后验检验出来的item
	public static int itemsPoolDBInRedis_fromlog;
	
	
	//isRealRankModel
	public static String isRealRankModel;
	
	
	public static rankModel getInstance(){
		return instance;
	}
	
	private rankModel() {
//		//item及feature特征表达的query instance
//		itemfOP =  queryInterface.getInstance();
		
		//oscache键值的存活周期
		int refreshInterval = 365*24*60*1000;
		
		LOG.info("refreshInterval = "+ refreshInterval);
		
		
		// 初始OSCache_MOOD;
		osCache = new OSCache("conf/oscache_rankModel.properties",CACHE_NAME, refreshInterval);
		//osCache = new OSCache("oscache.properties",CACHE_NAME, refreshInterval);
		
		hm_blacklist = new HashMap<String,String>();
		
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
	 * 遍历item集合，查找相同的simID（specialwords），判定为近似
	 * 
	 * @param itemSet， targetItemFront
	 * @return targetItemFront
	 */
	public RankItemNew findSimilarItemBySpecialWords(HashSet<RankItemNew> itemSet, RankItemNew targetRankItemNew)
	{
		if( itemSet == null || itemSet.isEmpty())
			return null;

		//逻辑上可能只是比较物理地址?后续俊伟再看看
		if( itemSet.contains(targetRankItemNew))
			return targetRankItemNew;
		
		
		
		for ( RankItemNew RankItemNew : itemSet){
			if(RankItemNew.getSpecialWords().equals(targetRankItemNew.getSpecialWords()))
			{
				LOG.info("found same RankItemNews by specialwords.");
				LOG.info("Old item: "+RankItemNew.getItem().getTitle()+" "+RankItemNew.getItem().getUrl()+" "+RankItemNew.getSpecialWords());
				LOG.info("New item: "+targetRankItemNew.getItem().getTitle()+" "+targetRankItemNew.getItem().getUrl()+" "+targetRankItemNew.getSpecialWords());
				return RankItemNew;
			}
		
		}
		
		return null;
	}
	
//	/**
//	 * 在oscache中，遍历，若存在与RankItemNew_front近似，返回近似的resultItem，否则返回null
//	 * 注意:
//	 * 1）这个函数上下文逻辑不好，前边的文意其实是先用specialwords从oscache中查询，查不到再调用本函数；
//	 * 	    但是更合理的上下文逻辑应该是把specialwords查找逻辑集成入本函数；
//	 * 
//	 * ！！考虑代码的更新效率，暂时先不这样做
//	 * 
//	 * 2）新加入逻辑：
//	 *    如果没有匹配出近似items，那么再去匹配solr中旧数据；如果solr中有标题高度近似甚至一致、或者ID一致、或者url一致，则删除solr中旧数据；
//	 * 
//	 * @param RankItemNew_front
//	 * @return resultItem
//	 */
//	public RankItemNew findSimilarRankItemNewInSolr(RankItemNew RankItemNew_front)
//	{
//		if(RankItemNew_front==null)
//			return null;
//		
//		//遍历oscache中所有item，看看与新加入的item的specialwords,id或url是否相同,返回有相同的RankItemNew集合
//		ArrayList<RankItemNew> sameRankItemNews = instance.findSameIDorUrlInOscache(RankItemNew_front);
//		
//		if(!sameRankItemNews.isEmpty()){//如果有相同ID或url的item
//			for(RankItemNew sameItemR: sameRankItemNews)
//			{
//				if(sameItemR!=null) {
//					//如果id相同，则返回该item
//					if(RankItemNew_front.getItem().getID().equals(sameItemR.getItem().getID()))
//						return sameItemR;
//					else{//若没有相同id，则有相同的url
//						//如果是文章页或者专题页，则返回该item
//						if((RankItemNew_front.getItem().getUrl().matches(".*html$")
//							&&!RankItemNew_front.getItem().getUrl().matches(".*index\\.(s?)html$"))
//							/*||channelsParser.isZhuanti(RankItemNew_front.getItem().getUrl())*/){
//							return sameItemR;
//						} else{
//							float rate = commenFuncs.simRate(sameItemR.getSpecialWords().trim(), RankItemNew_front.getSpecialWords().trim());
//							if(rate >= 0.4f)//如果相似度于0.4则认为相似
//								return sameItemR;
//						} 
//					}
//					
//				}
//			}
//		}else {//如果没有相同ID、url
//			HashSet<RankItemNew> itemSet = new HashSet<RankItemNew>();
//			for(String key: instance.getKeys()) {
//				RankItemNew item = instance.get(key);
//				itemSet.add(item);
//			}
//			//查找相似特征词,差异度小于0.3则认为相似
//			RankItemNew similarItem = instance.findSimilarItemBySpecialWords(itemSet, RankItemNew_front,);
//			if( similarItem!= null) {
//				return similarItem;
//			}
//		}
//	
//		return null;
//	}
	
	/**
	 * 匹配solr中旧数据；如果solr中有标题高度近似甚至一致、或者ID一致、或者url一致，则删除solr中旧数据
	 * 注意：
	 * 	  目前投放模型的流程，是只在以往的same match流程上做补丁；
	 * 	  也即，只有findSimilarRankItemNew确定oscache等rankmodel模型中满足下面两个条件：
	 * 	 1）rankmodel中没有发现相同的ID；
	 * 	 2）rankmodel中没有发现相似的item；
	 * 
	 * 	这种情况下，我们：
		//1)available == true，什么都不做，继续走后续流程;
		//2)available == false，删除solr中旧内容；
	 * @param  RankItemNew
	 *  	待匹配的item
	 * @return
	 */
	private void dealSameItemsInSolr(RankItemNew rankItemNew) {
		// TODO Auto-generated method stub
		if(rankItemNew == null || rankItemNew.getItem() == null)
			return;
		
		String sCmd = "{\"delete\":{\"id\":\"z0\"}}";
		//查询solr中的近似案例，feature只是用title,另外看下ID相同的item
		HashMap<String, Boolean> hm_items = queryFromSolr(rankItemNew.getItem());
		if(hm_items != null){
			for(Entry<String, Boolean> et:hm_items.entrySet()){
				if(et.getValue() == true)
					continue;			
				sCmd = "{\"delete\":{\"id\":\"z0\"}}";
				sCmd = sCmd.replace("z0",et.getKey());
				try {
					// 建模写入后台存储和计算引擎;构建json数据
					String rt = HttpRequest.sendPost(
							this.solrItemDBPath+"update/json"/*"http://10.32.28.119:8081/solr46/item/update/json"*/,
							sCmd);
					// test
					if (rt.indexOf("failed") > 0) {
						LOG.info("send post failed,del:" + sCmd + " rt=" + rt);
					}else{
						//记录simID，方便前端做bf排重
						
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOG.error("send post failed", e);
					continue;
				}
				LOG.info("success,  del i2a:" + et.getKey());
			}
			
		}
		
	}

	
	/**
	 * 匹配solr中黑名单数据，如果是available=false，直接删除之；
	 * @param  titleOrID
	 *  	
	 * @return
	 */
	private void dealBlackItemInSolr(String titleOrID) {
		// TODO Auto-generated method stub
		if(titleOrID == null || titleOrID.isEmpty())
			return;
		
		HashMap<String, Boolean> hm_items = querySameItemFromSolr(titleOrID);
		String sCmd = "{\"delete\":{\"id\":\"z0\"}}";

		if(hm_items != null){
			for(Entry<String, Boolean> et:hm_items.entrySet()){
				if(et.getValue() == true)
					continue;			
				sCmd = "{\"delete\":{\"id\":\"z0\"}}";
				sCmd = sCmd.replace("z0",et.getKey());
				try {
					// 建模写入后台存储和计算引擎;构建json数据
					String rt = HttpRequest.sendPost(
							this.solrItemDBPath+"update/json"/*"http://10.32.28.119:8081/solr46/item/update/json"*/,
							sCmd);
					// test
					if (rt.indexOf("failed") > 0) {
						LOG.info("send post failed,del:" + sCmd + " rt=" + rt);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOG.error("send post failed", e);
					continue;
				}
				LOG.info("success,  del black i2a:" + et.getKey());
			}
			
		}
		
	}
	
	/**
	 * 查询solr中的近似案例，feature还是用title和itemID来查询'
	 * 
	 * 注意：
	 * 	simID相同的文章，我们假定在整个solr内容体系中，只能出现一篇？这样合理的吗？从垂直频道业务角度考虑，不太合理；应该是高度近似的文章只能留一份；
	 * 	所以:
	 * 		此处逻辑不会调整，因为泛编流程会保证高度近似内容被丢弃，不会流到rankmodel；此处只是将相同ID，或者title高度近似的文章识别并处理下；
	 * 
	 * @param item
	 * @author likun   
	 * @return 返回查询到的itemf ID hash list 可能为null and empty;
	 * key是itemID；value是available
	 */
	public static HashMap<String,Boolean> queryFromSolr(itemf item) {
		if (item == null) {
			LOG.warn("queryFromSolr item == null");
			return null;
		}
		
		String s_title = item.getSplitTitle(); // 预处理item title
		if (s_title == null) {
			LOG.warn("queryFromSolr item title == null");
			return null;
		}

		// title
		Map<String, Integer> titleMap = new HashMap<String, Integer>();

		// 处理title，转换成solr可以接受的形式
		String[] titleArray = s_title.split("\\s+");
		// 将title中的词先放入titleMap中
		for (int i = 0; i < titleArray.length; i++) {
			int q = titleArray[i].indexOf("_");
			if (q < 0)
				continue;
			if (titleArray[i].substring(q + 1).equals("w"))// 去除标点符号
				continue;
			String unit = titleArray[i].substring(0, q);
			Integer Int = titleMap.get(unit);
			if (Int == null)
				titleMap.put(unit, 1);
			else
				Int += 1;

		}

		// 组装title search cmd
		StringBuffer sb = new StringBuffer();
	
		for (Map.Entry<String, Integer> entry : titleMap.entrySet()) {
			sb.append(entry.getKey()).append("^").append(entry.getValue())
					.append(" ");
		}

		String str_query = replaceInvaldateCharacter(sb.toString().trim());
		try {
			str_query = URLEncoder.encode(str_query, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			LOG.warn("encode error！");
			return null;
		}
		StringBuffer query = new StringBuffer(str_query);
		query.insert(0, "q=(itemid:"+item.getID()+"%20OR%20title:(").append("))&q.op=OR&rows=").append(100);
		//query.insert(0, "q=(").append("))&q.op=OR&rows=").append(100);

//		//test
//		LOG.error("query:"+query.toString());
		
		String xmlResponse = HttpRequest
				.sendGet(solrItemDBPath+"select"/*"http://10.32.28.119:8081/solr46/item/select"*/,query.toString());
		
		
		if (xmlResponse == null || xmlResponse.isEmpty()
				|| xmlResponse.indexOf("failed") > 0)
			return null;
		

		
		HashMap<String,Boolean> hm_res = new HashMap<String,Boolean>();
		// 解析xml逐个处理
		int b = 0;
		do{
			String itemID = "";
			b = xmlResponse.indexOf("itemid\">",b);
			if(b > 0){
				b = b + 8;	
				int e = xmlResponse.indexOf("</str>",b);
				if(e > b){
					itemID = xmlResponse.substring(b,e);
				}
			}
			
			if(itemID == null || itemID.isEmpty())
				break;
			
			b = xmlResponse.indexOf("title\">",b);
			
			//相同ID
			if(itemID.equals(item.getID())){
				boolean avaliable = false;
				int trueFlag = xmlResponse.indexOf("available\">true</bool>", b);
				int falseFlag = xmlResponse.indexOf("available\">false</bool>", b);
				if(trueFlag > 0 && (trueFlag <falseFlag || falseFlag<=0))
					avaliable = true;
							
				hm_res.put(itemID, avaliable);
				continue;
			}
			
		
			if(b > 0){
				b = b + 7;	
				int e = xmlResponse.indexOf("</str>",b);
				if(e > b){
					String title_solr = xmlResponse.substring(b, e).trim();
					float simRate = commenFuncs.simRate(s_title.replaceAll("_[a-zA-Z]{1,3}", "").split("\\s+"), title_solr.split("\\s+"));
					if(simRate >= 0.8 && titleArray.length >=5){
						boolean avaliable = false;
						int trueFlag = xmlResponse.indexOf("available\">true</bool>", e);
						int falseFlag = xmlResponse.indexOf("available\">false</bool>",e);
						if(trueFlag > 0 && (trueFlag <falseFlag || falseFlag<=0))
							avaliable = true;
									
						hm_res.put(itemID, avaliable);
					
					}
				}
				
			}
		
		}while(b > 0);

		LOG.info("sim items:"+hm_res.toString());
		
		return hm_res;
	}
	

	/**
	 * 查询solr中的相同案例，feature只是用title
	 * 
	 * (这段代码的可读性简直了。。。我都没法写下去)
	 * 
	 * @param titleOrID
	 * @author likun   
	 * @return 返回查询到的itemf ID hash list 可能为null and empty;
	 * key是itemID；value是available
	 */
	public static HashMap<String,Boolean> querySameItemFromSolr(String titleOrID) {
		if (titleOrID == null || titleOrID.isEmpty()) {
			LOG.warn("queryFromSolr titleOrID == null");
			return null;
		}
		
		StringBuffer query;
		
		String s_title = null;
		String[] titleArray = null;
		
		if(titleOrID.matches("\\d{1,20}"))
		{
			query = new StringBuffer(titleOrID);
			query.insert(0, "q=itemid:").append("&q.op=OR&rows=").append(10);
		}else{
			// title
			Map<String, Integer> titleMap = new HashMap<String, Integer>();

			try{
				s_title = SplitWordClient.split(titleOrID, null);
			}catch(Exception e){
				e.printStackTrace();
				s_title = null;
			}
			
			if(s_title == null || s_title.isEmpty()){
				LOG.warn("queryFromSolr s_title == null");
				return null;
			}
			
			// 处理title，转换成solr可以接受的形式
			s_title = s_title.replace("(/", "_").replace(")", "");
			titleArray = s_title.split("\\s+");
			// 将title中的词先放入titleMap中
			for (int i = 0; i < titleArray.length; i++) {
				int q = titleArray[i].indexOf("_");
				if (q < 0)
					continue;
				if (titleArray[i].substring(q + 1).equals("w"))// 去除标点符号
					continue;
				String unit = titleArray[i].substring(0, q);
				Integer Int = titleMap.get(unit);
				if (Int == null)
					titleMap.put(unit, 1);
				else
					Int += 1;

			}

			// 组装title search cmd
			StringBuffer sb = new StringBuffer();
		
			for (Map.Entry<String, Integer> entry : titleMap.entrySet()) {
				sb.append(entry.getKey()).append("^").append(entry.getValue())
						.append(" ");
			}

			String str_query = replaceInvaldateCharacter(sb.toString().trim());
			try {
				str_query = URLEncoder.encode(str_query, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				LOG.warn("encode error！");
				return null;
			}
			query = new StringBuffer(str_query);
			query.insert(0, "q=title:(").append(")&q.op=OR&rows=").append(100);
			
		}
		

		
		String xmlResponse = HttpRequest
				.sendGet(solrItemDBPath+"select"/*"http://10.32.28.119:8081/solr46/item/select"*/,query.toString());
		
		
		if (xmlResponse == null || xmlResponse.isEmpty()
				|| xmlResponse.indexOf("failed") > 0)
			return null;
		

		
		HashMap<String,Boolean> hm_res = new HashMap<String,Boolean>();
		// 解析xml逐个处理
		int b = 0;
		do{
			String itemID = "";
			b = xmlResponse.indexOf("itemid\">",b);
			if(b > 0){
				b = b + 8;	
				int e = xmlResponse.indexOf("</str>",b);
				if(e > b){
					itemID = xmlResponse.substring(b,e);
				}
			}
			
			if(itemID == null || itemID.isEmpty())
				break;

			b = xmlResponse.indexOf("title\">",b);
		
			//相同ID
			if(itemID.equals(titleOrID)){
				boolean avaliable = false;
				int trueFlag = xmlResponse.indexOf("available\">true</bool>", b);
				int falseFlag = xmlResponse.indexOf("available\">false</bool>", b);
				if(trueFlag > 0 && (trueFlag <falseFlag || falseFlag<=0))
					avaliable = true;
							
				hm_res.put(itemID, avaliable);
				break;
			}
			
			if(b > 0){
				b = b + 7;	
				int e = xmlResponse.indexOf("</str>",b);
				if(e > b && s_title != null && titleArray != null){
					String title_solr = xmlResponse.substring(b, e).trim();
					float simRate = commenFuncs.simRate(s_title.replaceAll("_[a-zA-Z]{1,3}", "").split("\\s+"), title_solr.split("\\s+"));
					if(simRate >= 0.9 && titleArray.length >=5){
						boolean avaliable = false;
						int trueFlag = xmlResponse.indexOf("available\">true</bool>", e);
						int falseFlag = xmlResponse.indexOf("available\">false</bool>",e);
						if(trueFlag > 0 && (trueFlag <falseFlag || falseFlag<=0))
							avaliable = true;
									
						hm_res.put(itemID, avaliable);
					
					}
				}
				
			}
		
		}while(b > 0);

		LOG.info("same items:"+hm_res.toString());
		
		return hm_res;
	}
	
	
	/**
	 * 去除无效字符
	 * 注意：
	 * 
	 * @param text;
	 * @return
	 */
	private static String replaceInvaldateCharacter(String text) {
		char d = (char) 0x20;

		if (text != null) {
			char[] data = text.toCharArray();
			for (int i = 0; i < data.length; i++) {
				if (!commenFuncs.isXMLCharacter(data[i])) {
					data[i] = d;
				}
			}
			return new String(data).replaceAll("&\\w{1,6};", " ").replaceAll(
					"&", "&amp;");
		}
		return "";
	}
	
	/**
	 * 以新的itemFront的信息更新oscache中存的RankItemNew的信息
	 * 注意：
	 * 
	 * @param rankList;
	 * @param  RankItemNew
	 *  	oscache中的相似RankItemNew
	 * @param  item_front
	 *   	从前端（统计或者抓取得到）来的相似RankItemNew
	 * @return
	 */
	public void updateRankItemNew(RankListNew rankList, RankItemNew RankItemNew_in,  RankItemNew RankItemNew_front)
	{
		//test log
		LOG.info("updateRankItemNew in oscache,the info is:");
		LOG.info(RankItemNew_in.getItem().getUrl());
		LOG.info(RankItemNew_in.getOthers());
		LOG.info(RankItemNew_in.getItem().getID());
		LOG.info("--------");
		LOG.info(RankItemNew_front.getItem().getUrl());
		LOG.info(RankItemNew_front.getOthers());
		LOG.info(RankItemNew_front.getItem().getID());
		LOG.info("--------^");
		//记录信息：先修改实时权重及时间这两个字段
		long currentTime = System.currentTimeMillis();
		

		//如果新增文章是AA级
		if(RankItemNew_front.getWeight().equals("AA")){
			//如果存活的文章不是AA的
			if(!RankItemNew_in.getWeight().equals("AA")){
				//删除旧的文章
				rankList.getDelList().add(RankItemNew_in);
				instance.del(RankItemNew_in.getSpecialWords());
				StringBuffer sbTmp = new StringBuffer();
				sbTmp.append("Update Delete: ").append(RankItemNew_in.getItem().getTitle())
				.append(" ").append(RankItemNew_in.getWeight()).append(" ").append(RankItemNew_in.getItem().getID());
				LOG.info(sbTmp.toString());
				
			} 
			
			// 再放入新的进oscache，但不放入新增队列newlist
			StringBuffer sbTmp = new StringBuffer();
			instance.add(RankItemNew_front.getSpecialWords(), RankItemNew_front);
			sbTmp.append("Update Add: ").append(RankItemNew_front.getItem().getTitle())
					.append(" ").append(RankItemNew_front.getWeight()).append(" ")
					.append(RankItemNew_front.getItem().getID());
			LOG.info(sbTmp.toString());
			return;
		}
		
		//如果新增的文章不是AA级，而存活中的文章是AA级的，则不作处理
		if(RankItemNew_in.getWeight().equals("AA")){
			RankItemNew_in.setCreateTimeStamp(currentTime/1000);
			
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("Not Qualified to Update Old: ").append(RankItemNew_in.getItem().getTitle())
			.append(" ").append(RankItemNew_in.getWeight()).append(" ").append(RankItemNew_in.getItem().getID());
			LOG.info(sbTmp.toString());
			
			sbTmp = new StringBuffer();
			sbTmp.append("Not Qualified to Update New: ").append(RankItemNew_front.getItem().getTitle())
			.append(" ").append(RankItemNew_front.getWeight()).append(" ").append(RankItemNew_front.getItem().getID());
			LOG.info(sbTmp.toString());
			
			return;
		}

		// 霸道设置：如果是来自头条内容池，则一定重新投放
		String cla_tags = RankItemNew.genDataAuthLabel(RankItemNew_front);
		if(cla_tags != null && cla_tags.indexOf("ifengtoutiao") >= 0){
			rankList.getDelList().add(RankItemNew_in);
			instance.del(RankItemNew_in.getSpecialWords());
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("toutiao change ");
			sbTmp.append("Update Delete: ")
					.append(RankItemNew_in.getItem().getTitle()).append(" ")
					.append(RankItemNew_in.getWeight()).append(" ")
					.append(RankItemNew_in.getItem().getID());
			LOG.info(sbTmp.toString());

		
			// 再放入新的
			sbTmp = new StringBuffer();

			rankList.getNewList().add(RankItemNew_front);
			instance.add(RankItemNew_front.getSpecialWords(), RankItemNew_front);
			sbTmp.append("Update Add: ")
					.append(RankItemNew_front.getItem().getTitle()).append(" ")
					.append(RankItemNew_front.getWeight()).append(" ")
					.append(RankItemNew_front.getItem().getID());
			LOG.info(sbTmp.toString());
			return;
		}
		
		// 霸道设置：如果front不是来自头条内容池，而存活数据来自头条内容池，不重新投放
		String cla_tags1 = RankItemNew.genDataAuthLabel(RankItemNew_in);
		if(cla_tags1 != null && cla_tags1.indexOf("ifengtoutiao") >= 0){
		
			LOG.info("Not Update Old because toutiao:"+RankItemNew_in.getItem().getID());
			return;
		}		
		
		//霸道设置：如果都不是来自头条，那么如果外来的是editor数据，一定重新投放
		if(cla_tags != null && cla_tags.indexOf("editor") >= 0){
			rankList.getDelList().add(RankItemNew_in);
			instance.del(RankItemNew_in.getSpecialWords());
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("editor change ");
			sbTmp.append("Update Delete: ")
					.append(RankItemNew_in.getItem().getTitle()).append(" ")
					.append(RankItemNew_in.getWeight()).append(" ")
					.append(RankItemNew_in.getItem().getID());
			LOG.info(sbTmp.toString());

		
			// 再放入新的
			sbTmp = new StringBuffer();

			rankList.getNewList().add(RankItemNew_front);
			instance.add(RankItemNew_front.getSpecialWords(), RankItemNew_front);
			sbTmp.append("Update Add: ")
					.append(RankItemNew_front.getItem().getTitle()).append(" ")
					.append(RankItemNew_front.getWeight()).append(" ")
					.append(RankItemNew_front.getItem().getID());
			LOG.info(sbTmp.toString());
			return;
		}
		
		//霸道设置：如果都不是来自头条，而且外来的不是editor数据，存活的是editor数据，不重新投放
		if(cla_tags1 != null && cla_tags1.indexOf("editor") >= 0){
			
			LOG.info("Not Update Old because editor:"+RankItemNew_in.getItem().getID());
			return;
		}		
	
		
		//如果新来的有缩略图而旧的没有缩略图，重新投放
		if(RankItemNew_front.getItem().getDocType().equals("docpic")
				&& RankItemNew_in.getItem().getDocType().equals("doc")){
			rankList.getDelList().add(RankItemNew_in);
			instance.del(RankItemNew_in.getSpecialWords());
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("Update Delete because pic: ").append(RankItemNew_in.getItem().getTitle())
			.append(" ").append(RankItemNew_in.getWeight()).append(" ").append(RankItemNew_in.getItem().getID());
			LOG.info(sbTmp.toString());
			
		
			
			//再放入新的
			sbTmp = new StringBuffer();
			rankList.getNewList().add(RankItemNew_front);
			instance.add(RankItemNew_front.getSpecialWords(), RankItemNew_front);
			sbTmp.append("Update Add because pic: ").append(RankItemNew_front.getItem().getTitle())
			.append(" ").append(RankItemNew_front.getWeight()).append(" ").append(RankItemNew_front.getItem().getID());
			LOG.info(sbTmp.toString());
	
			return;
		}
		
		// 如果outside item的weight更大，重新投放；
		if (RankItemNew_in.getWeight().compareTo(RankItemNew_front.getWeight()) > 0) {
			rankList.getDelList().add(RankItemNew_in);
			instance.del(RankItemNew_in.getSpecialWords());
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("Update Delete because weight: ").append(RankItemNew_in.getItem().getTitle())
			.append(" ").append(RankItemNew_in.getWeight()).append(" ").append(RankItemNew_in.getItem().getID());
			LOG.info(sbTmp.toString());
			
			//再放入新的
			sbTmp = new StringBuffer();
			rankList.getNewList().add(RankItemNew_front);
			instance.add(RankItemNew_front.getSpecialWords(), RankItemNew_front);
			sbTmp.append("Update Add because weight: ").append(RankItemNew_front.getItem().getTitle())
			.append(" ").append(RankItemNew_front.getWeight()).append(" ").append(RankItemNew_front.getItem().getID());
			LOG.info(sbTmp.toString());
			
			
			return;
		}
		
		
		

		//如果相似item其tags有变化，则重新投放

		String lastFeature2 = "";
		if(RankItemNew_in.getItem().getFeatures().size() > 0)
			lastFeature2 = RankItemNew_in.getItem().getFeatures().get(0);
		if (!lastFeature2.isEmpty()){
			//比较features，看哪一组更好一些
			int rt = compareFeatures(RankItemNew_front.getItem().getFeatures(),RankItemNew_in.getItem().getFeatures());
			//如果新来的feature更多更全，重新投放
			if(rt == -1){
				rankList.getDelList().add(RankItemNew_in);
				instance.del(RankItemNew_in.getSpecialWords());
				StringBuffer sbTmp = new StringBuffer();
				sbTmp.append("Update Delete because tags: ").append(RankItemNew_in.getItem().getTitle())
				.append(" ").append(RankItemNew_in.getWeight()).append(" ").append(RankItemNew_in.getItem().getID());
				LOG.info(sbTmp.toString());
				
				
				//再放入新的
				sbTmp = new StringBuffer();
				rankList.getNewList().add(RankItemNew_front);
				instance.add(RankItemNew_front.getSpecialWords(), RankItemNew_front);
				sbTmp.append("Update Add because tags: ").append(RankItemNew_front.getItem().getTitle())
				.append(" ").append(RankItemNew_front.getWeight()).append(" ").append(RankItemNew_front.getItem().getID());
				LOG.info(sbTmp.toString());

				return;
			}
		}
		
		
		//如果item的标题或者权重发生变化，我们并不进行重新投放，输出提示
		if ( !RankItemNew_in.getWeight().equals(RankItemNew_front.getWeight())
				|| !RankItemNew_in.getItem().getTitle().equals(RankItemNew_front.getItem().getTitle())) {
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("Not Qualified to Update Old: ").append(RankItemNew_in.getItem().getTitle())
			.append(" ").append(RankItemNew_in.getWeight()).append(" ").append(RankItemNew_in.getItem().getID());
			LOG.info(sbTmp.toString());
			
			sbTmp = new StringBuffer();
			sbTmp.append("Not Qualified to Update New: ").append(RankItemNew_front.getItem().getTitle())
			.append(" ").append(RankItemNew_front.getWeight()).append(" ").append(RankItemNew_front.getItem().getID());
			LOG.info(sbTmp.toString());
		}
		
		
		RankItemNew_in.setCreateTimeStamp(currentTime/1000);
		//需要覆盖，这样oscache才能更新数据
		instance.add(RankItemNew_in.getSpecialWords(), RankItemNew_in);
	}
	
	/**
	 * 新的RankItemNew，插入oscache和newlist
	 * 注意：
	 * 
	 * @param rankList， item_front
	 * @return
	 */
	public void addNewRankItemNewInRankList(RankListNew rankList, RankItemNew RankItemNew_front)
	{
		//如果newlist超过2000个，退出
		if(rankList.getNewList().size() > 2000
				|| RankItemNew_front == null)	
			return;
		
		itemf item = RankItemNew_front.getItem();
		
		//如果item的title和content有一个不为空，就算有效item；
		if (item != null && (item.getTitle() != null || item.getSplitContent() != null))// 如果在hbase中能找到
		{
			//若文章是AA级的，则不加入新增队列，直接加入缓存，然后再加入候补队列
			if(!RankItemNew_front.getWeight().equals("AA"))
				rankList.getNewList().add(RankItemNew_front);
			instance.add(RankItemNew_front.getSpecialWords(), RankItemNew_front);
			StringBuffer sbTmp = new StringBuffer();
			sbTmp.append("Add New Item: ").append(RankItemNew_front.getItem().getTitle())
			.append(" ").append(RankItemNew_front.getWeight()).append(" ").append(RankItemNew_front.getItem().getID());
			LOG.info(sbTmp.toString());
			
		}else{
			String frontUrl = RankItemNew_front.getItem().getUrl();
			//判断不是单页，是多级频道页或者专题页
			if(!frontUrl.matches(".*html$")||frontUrl.matches(".*index\\.(s?)html$"))
			{
				if(!RankItemNew_front.getWeight().equals("AA"))
					rankList.getNewList().add(RankItemNew_front);
				instance.add(RankItemNew_front.getSpecialWords(), RankItemNew_front);
				StringBuffer sbTmp = new StringBuffer();
				sbTmp.append("Add New Item: ").append(RankItemNew_front.getItem().getTitle())
				.append(" ").append(RankItemNew_front.getWeight()).append(" ").append(RankItemNew_front.getItem().getID());
				LOG.info(sbTmp.toString());
			}
		}
	
	}
	
	/**
	 * 遍历缓存中所有item的id和url，看看与新加入的item的id和url是否相同,返回有相同的RankItemNew集合
	 * 注意：
	 * 
	 * @param RankItemNew_front
	 * @return al_RankItemNews
	 */
	public ArrayList<RankItemNew> findSameIDorUrlInOscache(RankItemNew RankItemNew_front)
	{
		HashSet<String> keySet = instance.getKeys();
		ArrayList<RankItemNew> al_RankItemNews = new ArrayList<RankItemNew>();
		
		for(String key:keySet)
		{
			RankItemNew RankItemNew = (RankItemNew) instance.get(key);				

			//判断url\ID是否相同
			if(RankItemNew.getItem().getID().equals(RankItemNew_front.getItem().getID())){
				al_RankItemNews.add(RankItemNew);
				continue;
			}
			//判断url\ID是否相同
			if(RankItemNew.getItem().getUrl()!=null && RankItemNew_front.getItem().getUrl()!=null 
					&& RankItemNew.getItem().getUrl().equals(RankItemNew_front.getItem().getUrl()))
				al_RankItemNews.add(RankItemNew);
		}
		
		return al_RankItemNews;
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
	public ArrayList<itemf> getFrontItemfsFromPool(String appid){
		Jedis jedis = null;
		Set<String> keyset = null;
		try {
			jedis = new Jedis("10.32.21.62",6379,10000);
			
			if(appid.equals("recommend"))
				jedis.select(this.itemsPoolDBInRedis);
			
			keyset = jedis.keys("*");
			LOG.info("redis size: "+keyset.size());
		}catch (Exception e) {
			LOG.error("ERROR", e);
			return null;
		}
		
		
		HashSet<itemf> appitemfrontCache = new HashSet<itemf>();
		

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
			{
				
				LOG.error("value from pool is null:"+key);
				continue;
			}

//注意 8081是不用删除的，因为当前可以投放的所有内容都在里面了			
			// 删除掉key
			try {
				jedis.del(key);
			} catch (Exception e) {
				LOG.error("[ERROR] ", e);
				continue;
			}

			itemf itemfFront = null;
			try {
				if(appid.equals("fresh"))
					itemfFront = JsonUtils.fromJson(value, FreshItemf.class);
				else
					itemfFront = JsonUtils.fromJson(value, itemf.class);
				
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

			if (itemfFront == null) {
				LOG.error("appitemfront == null");
				LOG.error(value);
				continue;
			}

			if (appid != null && !itemfFront.getAppId().equals(appid))
				continue;
			appitemfrontCache.add(itemfFront);
		}
		
		ArrayList<itemf> appitemfront_list = new ArrayList<itemf>();
		/*
		 * 逻辑处理模块，遍历appitemfrontCahce集合，将未查询到item的项加入缓存，将查询到的item从缓存中删除
		*/
		for(itemf itemfFront : appitemfrontCache){

////			// 过滤掉url或者channel中含有“finance*roll”、"news*gundong"的item
////			if (itemfFront.getUrl() != null&& itemfFront.getUrl().matches("^.*(http://?news.*gundong.*|http://?finance.*roll.*)$"))
////				continue;
////			if (itemfFront.getChannel() != null&& itemfFront.getChannel().matches("^.*(http://?news.*gundong.*|http://?finance.*roll.*)$"))
////				continue;
//
//
//
//			// 新建生成appItemFront
//			appitemfront.setUrl(item.getUrl());
//			appitemfront.setImgurl("null");
//			appitemfront.setTimeStamp(Long.toString(System.currentTimeMillis() / 1000));
//			if (idPvMap != null && idPvMap.containsKey(key)) {
//				appitemfront.setPv(idPvMap.get(key));
//			}
//
//			// addlikun；因为url有时候缺“/”，所以给予补充；
//			if (appitemfront.getUrl() != null&& appitemfront.getUrl().startsWith("http:/")&& !(appitemfront.getUrl().startsWith("http://")))
//				appitemfront.setUrl(appitemfront.getUrl().replaceFirst("http:","http:/"));

			appitemfront_list.add(itemfFront);
		}

		jedis = null;
		
		return appitemfront_list;
	}
	
	
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
	 * 对从前端抓下来的一轮item进行排重过滤等处理；
	 * 流程和功能如下：
	 * 2）对id进行排重；保留权重更高或者标题更长；
	 * 3）根据时间进行进一步过滤，且只保留最近的文章；
	 * 4）进行相似识别；相似item修正排重，使权重更高或者标题更长；
	 * @param rankList 投放数据集
	 * @param appItemFrontList
	 * @param syItems，首页存在的文章
	 * @return filterList
	 */
	public ArrayList<RankItemNew> filterItems(String appid,RankListNew rankList,ArrayList<itemf> appItemFrontList)
	{
		if(appid == null)
			return null;
		
		
	
		//1. 过滤在首页存在的item，转化成RankItemNew, 并初步过滤掉不合适的url
		ArrayList<RankItemNew> raw_RankItemNews = new ArrayList<RankItemNew>();
		for(itemf appItemF: appItemFrontList) {
			
			LOG.info("item from pool is :"+appItemF.getID());
			
			//对应的RankItemNew
			RankItemNew itemR = new RankItemNew(appItemF);
			itemf item = appItemF;	
			//如果item的title和content有一个不为空，就算有效item；否则因为找不到具体item内容，所以丢弃，不再走后续投放流程；
			if (item != null && item.getTitle() != null && item.getSplitContent() != null)
			{
				//过滤黑名单
				if(hm_blacklist.containsKey(item.getTitle().trim())
						|| hm_blacklist.containsKey(item.getID().trim()))
					continue;
				
				//过滤唯一simID（sameID）为空item
				if(itemR.getSpecialWords() == null || itemR.getSpecialWords().isEmpty())
				{
					LOG.error("ignore.simID is null or empty,ID="+itemR.getItem().getID());
					continue;
				}
				
				raw_RankItemNews.add(itemR);
			}else{
				LOG.warn("remove item == null:"+appItemF.getID());
				continue;
			}
		}
			
		//2.对id排重：相同id进行相应处理
		HashMap<String, RankItemNew> idFilterMap = new HashMap<String, RankItemNew>();
		for(RankItemNew itemR : raw_RankItemNews) {
			RankItemNew oldItemR = null;
			if ((oldItemR = idFilterMap.get(itemR.getItem().getID()))!=null) {
				//选择权重更高替代
				if (oldItemR.getWeight().compareTo(itemR.getWeight()) > 0) {
					LOG.info("find same id item in idFilterMap,choose better one:");
					LOG.info(oldItemR.getItem().getID());
					LOG.info(itemR.getItem().getID());
					idFilterMap.put(itemR.getItem().getID(), itemR);
					
				}
			}else//id不重复
				idFilterMap.put(itemR.getItem().getID(), itemR);
		}
		
		//3.根据时间、标题、url等做进一步过滤
		ArrayList<RankItemNew> al_RankItemNews = new ArrayList<RankItemNew>();
		for(RankItemNew itemR : idFilterMap.values()) {
			itemf item = itemR.getItem();	
			//如果在hbase中，item的title和content有一个不为空，就算有效item；否则因为找不到具体item内容，所以丢弃，不再走后续投放流程；
			if (item != null && (item.getTitle() != null || item.getSplitContent() != null) && item.getPublishedTime() != null && !item.getPublishedTime().isEmpty())
			{
				//获取创建时间
				Timestamp createTS = null;
				try{
					createTS = Timestamp.valueOf(item.getPublishedTime());
				}catch (Exception e){
					LOG.warn("date format: "+item.getID()+"||"+item.getPublishedTime());
					LOG.error("[ERROR] ", e);
					continue;
				}
				
				
//		//!!由于读入数据规模太大，排重已经处理不了，所以只好先临时丢弃7天前的数据
//				Timestamp nowTS = new Timestamp(System.currentTimeMillis());
//				//统一过滤掉7天前的,不对fresh业务起效
//				if(!appid.equals("fresh") && instance.withinMinutes(createTS, nowTS, 7*24*60) == false)
//				{
//					LOG.warn("remove items 7 days ago:"+item.getUrl());
//					continue;
//				}
				
				//过滤抓取错误
				if(item.getTitle() != null
						&& item.getTitle().startsWith("failed"))
				{
					LOG.warn("remove item not right:"+item.getUrl());
					continue;
				}
				
				//过滤标题太短或者太长
				if(item.getTitle() != null
						&& item.getDocType() != null){
				
					if((item.getDocType().equals("doc") || item.getDocType().equals("docpic"))
							&& (commenFuncs.cmpWordLen(item.getTitle()) < 12 || commenFuncs.cmpWordLen(item.getTitle()) > 32))
					{
						LOG.warn("remove item too short/long:"+item.getID()+"||"+item.getUrl());
						continue;
					}
					
					
					if((item.getDocType().equals("slide") || item.getDocType().equals("video"))
							&& (commenFuncs.cmpWordLen(item.getTitle()) < 8 || commenFuncs.cmpWordLen(item.getTitle()) > 32))
					{
						LOG.warn("remove item too short/long:"+item.getID()+"||"+item.getUrl());
						continue;
					}
				}

				//解析other字段，根据抓取系统给出的文章评价quality信息，过滤掉不好的内容
				HashMap<String,String> hm_others = parseItemOthersField(itemR.getOthers());
				String quality = hm_others.get("qualitylevel");	
				if (quality != null && quality.equals("E")) {
					LOG.warn("qualitylevel is E,remove:"+item.getID()+"||"+item.getUrl());
					continue;
				}
				
				//丢弃illegal item
				String cla_tags = RankItemNew.genDataAuthLabel(itemR);
				if(cla_tags != null && cla_tags.indexOf("illegal")>=0){
					LOG.warn("illegal,remove:"+item.getID()+"|"+item.getUrl());
					continue;
				}
				
				
				
				
//				解析other字段，阻止分类过短的自媒体内容；
//				...
//				HashMap<String,String> hm_others = parseItemOthersField(itemR.getOthers());
//				String source = hm_others.get("source");
//				// 解析other字段;先分发数据到天气，因为天气不用走投放模型包括近似排重；
//				if (source != null && source.equals("spider")) {
//					String channel = hm_others.get("channel");
//					if (channel != null && channel.equals("weather")) {
//						rankList.getWeatherList().add(itemR);
//						//@test 输出具体日志，方便做debug
//						LOG.info("find weather item:"+itemR.getID());
//						
//						continue;
//					}
//				}

				al_RankItemNews.add(itemR);
			}else{
				LOG.warn("remove item == null:"+item.getUrl());
				continue;
			}
		}
		
	
		//3.相似item识别和处理；
		HashSet<RankItemNew> RankItemNewSet = new HashSet<RankItemNew>();
		for(RankItemNew itemR : al_RankItemNews)
		{
			// 查找有无近似
			RankItemNew similarItem = instance.findSimilarItemBySpecialWords(
					RankItemNewSet, itemR);
		
			if (similarItem != null) {

				/*
				 * similarItem是否来自头条内容池（source=ifengtoutiao）;头条内容池有绝对优先权（
				 * 当两个都来自头条内容池时候，也不做替换）
				 * 注意：此处要处理ifenttoutiao \ editor两种情况
				 */
				String cla_tags = RankItemNew.genDataAuthLabel(similarItem);
				if(cla_tags != null && cla_tags.indexOf("ifengtoutiao") >= 0){
					//@Test
					LOG.warn("RankItemNew simIDList -4:"
							+ itemR.getItem().getID()+" simID="+similarItem.getSpecialWords());
					continue;
				}
				
				//如果只有itemR是来自头条内容池，则此时取代
				String cla_tags1 = RankItemNew.genDataAuthLabel(itemR);
				if(cla_tags1 != null && cla_tags1.indexOf("ifengtoutiao") >= 0){
					RankItemNewSet.remove(similarItem);
					RankItemNewSet.add(itemR);
					// @Test
					LOG.warn("RankItemNew simIDList -3:"
							+ itemR.getItem().getID()+" simID="+similarItem.getSpecialWords());
					continue;
				}
				
				if(cla_tags != null && cla_tags.indexOf("editor") >= 0){
					
					//@Test
					LOG.warn("RankItemNew simIDList -2:"
							+ itemR.getItem().getID()+" simID="+similarItem.getSpecialWords());
					continue;
				}
				if(cla_tags1 != null && cla_tags1.indexOf("editor") >= 0){
					RankItemNewSet.remove(similarItem);
					RankItemNewSet.add(itemR);
				
					// @Test
					LOG.warn("RankItemNew simIDList -1:"
							+ itemR.getItem().getID()+" simID="+similarItem.getSpecialWords());
					
					continue;
				}
				

				// ID不同，判断有图无图，同时加入simIDlist
				// 如果similarItem没有缩略图而itemR有，那么删除similarItem,并且把similar加入SimIDList
				if (itemR.getItem().getDocType().equals("docpic")
						&& similarItem.getItem().getDocType().equals("doc")) {
					RankItemNewSet.remove(similarItem);
					RankItemNewSet.add(itemR);
					
					// @Test
					LOG.warn("RankItemNew simIDList 0:"
							+ itemR.getItem().getID()+" simID="+similarItem.getSpecialWords());
					continue;
				}

				// @Test
				LOG.warn("RankItemNew simIDList 1:"
						+ itemR.getItem().getID()+" simID="+similarItem.getSpecialWords());

				// 如果权重更高更新权重
				if (similarItem.getWeight().compareTo(itemR.getWeight()) > 0) {
					LOG.info("find similar item in filterMap,choose better weight one:");
					RankItemNewSet.remove(similarItem);
					RankItemNewSet.add(itemR);
					
					LOG.info(similarItem.getItem().getID());
					LOG.info(itemR.getItem().getID());

				}
			
			}else//没有找到相同和相似的item，放入filterMap
				RankItemNewSet.add(itemR);	
		}
		
		//把哈希集合转换成ArrayList，
		ArrayList<RankItemNew> filterList = new ArrayList<RankItemNew>();
		Iterator<RankItemNew> it = RankItemNewSet.iterator();
		while(it.hasNext()) {
			filterList.add(it.next());
		}
	
		return filterList;
	}
	

	/**
	 * 遍历进行过排重过滤后的RankItemNew列表，把新的item和权重变化的item加入oscache；
	 * 取每个频道pv最高的item加入backupList；
	 * 注意：
	 * 
	 * @param rankList， cacheSet， itemFrontList
	 * @return rankList
	 */
	public int traverFront(String appid,RankListNew rankList, ArrayList<RankItemNew> RankItemNewFrontList) {

		if(rankList == null)
			return -1;
		
		if(RankItemNewFrontList == null||RankItemNewFrontList.size()==0)
			return -1;
			
		SimpleDateFormat timeFmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		Iterator<RankItemNew> it = RankItemNewFrontList.iterator();
		while(it.hasNext()){
			try {
				
				RankItemNew itemR = it.next();
				
				//先从solr中处理掉相似或者相同item；具体逻辑是，对每一个相同或者相似item：
				//1)available == true，什么都不做，继续走后续流程;
				//2)available == false，删除solr中旧内容；
				dealSameItemsInSolr(itemR);
	
				//如果发现是本地抓取稿源数据，那么直接分发进入本地投放list，不走内容模型
				//注意，如果是正常数据，需要从表达中识别出映射关系，成为本地数据，这部分数据不加入loclist，正常映射；
				String others = itemR.getItem().getOther();
				if(others!=null && others.indexOf("loc=") >=0){	
					
					LOG.warn("loc item:"+itemR.getItem().getID());
					rankList.getLocList().add(itemR);
					continue;
				}
	

				//添加一个特殊流程，如果发现item是2天以前的，并且timeSensitive==true,那么直接放入solr库中，不经过投放，同时设置avaliable字段为false
				String publishDate = itemR.getItem().getPublishedTime().trim();
				publishDate = publishDate.replaceFirst("\\s\\s", "\\s");
			
				try {
					Date date = timeFmt.parse(publishDate);
					long daysInter = (System.currentTimeMillis() - date
							.getTime()) / (24 * 3600 * 1000);
					if (!appid.equals("fresh") && (daysInter >= 2 || daysInter <0)
							&& itemR.getOthers() != null
							&& itemR.getOthers().indexOf("timeSensitive=true") >= 0) {
						LOG.warn("too old:"+daysInter+" "+itemR.getItem().getID());
						rankList.getDelList().add(itemR);
						continue;
					}
				} catch (Exception e) {
					LOG.warn("[error] publisDate is:"+publishDate,e);
				}

				
				//@test，目前策略，将一点资讯第二批抓取的item默认为不存活，直接进入solr库
				if(others!=null && others.indexOf("source=ignoreYidian") >=0){
					LOG.warn("source=ignoreYidian:"+itemR.getItem().getID());
					rankList.getDelList().add(itemR);
					continue;
					
				}
				
				//@test,添加一个女性强偏向内容的过滤，设置成available false，避免直接推荐
				if(femaleFilter.isFemaleDoc(itemR))
				{
					LOG.warn("may be female doc:"+itemR.getItem().getID());
					rankList.getDelList().add(itemR);
					continue;
				}
				
				
				
				RankItemNew RankItemNew = (RankItemNew) instance.get(itemR
						.getSpecialWords());


				if (RankItemNew != null) {// 如果oscache中存在RankItemNew
					instance.updateRankItemNew(rankList, RankItemNew, itemR);

					// @Test
					LOG.warn("RankItemNew simIDList 2:" + RankItemNew.getItem().getID() + " simID="
							+ RankItemNew.getSpecialWords());

				} else {//如果oscache中没有,则新建
					instance.addNewRankItemNewInRankList(rankList, itemR);
					
					
//					RankItemNew = instance.findSimilarRankItemNew(itemR);// 查找相似的RankItemNew(不仅仅看投放模型，也弱一致地看后台solr旧数据)
//					if (RankItemNew != null) {// 如果有，则更新
//						instance.updateRankItemNew(rankList, RankItemNew, itemR);
//						// @Test
//						LOG.warn("RankItemNew simIDList 3:" + RankItemNew.getItem().getID()
//								+ " " + RankItemNew.getSimIDList().toString());
//
//					} else {// 如果没有，则新建
//						instance.addNewRankItemNewInRankList(rankList, itemR);
//					}
					
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
	 * 判断rankItem的other字段属性，并分发到天气、地域、季节等相应数据中去；
	 * 注意：
	 * 		在这次改造过程中，我们需要统一下认知，只有临时的或者测试性质的分类数据，才通过投放模型产生；理论上，可以确定OK的分类，都应该从@志恒的预加载机制产生。比如地域数据，只需要
	 * 在“抓取--特征抽取--特征表达”环节完善实现，那么通过solr内容库的检索就能产生粗糙的分类list，然后通过@志恒的热度排序，就能预加载出更好的分类数据了。
	 * 		也正因为这样，所以像“优质”这种标签，用于特征表达似乎不太恰当，通过投放模型产生数据就比较合理化了。
	 * 
	 * @param rankList 数据分发流
	 * @param rankItem
	 * 否决 我们已经不用投放模型来产生热点新闻了 hm_hotItemIDs 热点新闻map，做热点新闻识别
	 * @param locationExtraction 地域识别器
	 * @param locEx 8
	 * 暂时否决：@return 内容是否可以参与投放模型计算，投放到内容匹配、协同、新闻追踪等算法去；
	 * 天气新闻样例：other : fromImcp|!|loc=北京|!|channel=weather|!|pic=1|1|1
	        地域汽车新闻样例： other : fromImcp|!|loc=北京|!|channel=auto|!|pic=1|1|1
                   游戏新闻样例 ： other : fromImcp|!|channel=games|!|pic=1|1|1
       
	 */
	private void distributeData(RankListNew rankList, RankItemNew rankItem,
			locationExtraction locEx) {
		// TODO Auto-generated method stub
//		// 其它业务的数据分流
//		HashMap<String, String> hm_others = parseItemOthersField(rankItem
//				.getOthers());
//		// season\festival等数据补充
//		if (rankItem.getTitle().indexOf("夏天") >= 0
//				|| rankItem.getTitle().indexOf("酷暑") >= 0
//				|| rankItem.getTitle().indexOf("暑假") >= 0
//				|| rankItem.getTitle().indexOf("暑期") >= 0) {
//			if (rankItem.getCategory() != null
//					&& (rankItem.getCategory().indexOf("旅游") >= 0
//							|| rankItem.getCategory().indexOf("历史") >= 0
//							|| rankItem.getCategory().indexOf("健康") >= 0 || rankItem
//							.getCategory().indexOf("时尚") >= 0))
//				rankList.getSeasonList().add(rankItem);
//		}
//		if (rankItem.getTitle().indexOf("高考志愿") >= 0
//				|| (rankItem.getTitle().indexOf("高校") >= 0 && rankItem
//						.getTitle().indexOf("排名") >= 0)
//				|| (rankItem.getTitle().indexOf("毕业") >= 0 && rankItem
//						.getTitle().indexOf("旅行") >= 0)) {
//
//			rankList.getFestivalList().add(rankItem);
//		}

		
//		//优质数据的分流
//		String  s_whyQuality = "";
//		if(rankItem.getCategory().equals("体育")){
//			ArrayList<String> item_features = rankItem.getItem().getFeatures();
//			if(item_features.size()%3 == 0){
//				for(int i=0;i<item_features.size();i+=3){
//					String feature = item_features.get(i);
//					String type = item_features.get(i+1);
//					float weight = 0f;
//					try{
//						weight = Float.valueOf(item_features.get(i+2));
//					}catch(Exception e){
//						weight = 0f;
//						e.printStackTrace();
//					}
//					//不可读，暂时不加入可读化表达
//					if(weight <= 0)
//						continue;
//					
//					if(feature.equals("体育图片策划")
//							|| feature.equals("独家评论")
//							|| feature.equals("独家访谈")
//							|| feature.equals("图腾")
//							|| feature.equals("景深")
//							|| rankItem.getItem().getTitle().indexOf("一周看台")>=0
//							|| rankItem.getItem().getTitle().indexOf("【景深】")>=0
//							|| rankItem.getItem().getTitle().indexOf("【图腾】")>=0){
//						rankList.getQualityList().add(rankItem);
//						s_whyQuality = feature;
//						LOG.warn("[debug] quality itemid = "+rankItem.getItem().getID()+" feature = "+feature);
//						break;
//					}
//					
//				}
//			}
//		}
		

	}

	/**
	 * 遍历所有oscache中的item，调整item的权重和吧过期的item，并加入到ranklist中
	 * 同时将权重大于等于B且没被降权过的文章，以及图集则加入候补队列
	 * 注意：
	 * 
	 * @param rankList, cacheSet
	 * @return RankList
	 */
	public int traverBack(RankListNew rankList)
	{
		if(rankList == null)
			return -1;
		
		HashSet<String> cacheSet = instance.getKeys();
		
		ArrayList<RankItemNew> tempbackList_ABC = new ArrayList<RankItemNew>();
		ArrayList<RankItemNew> tempbackList_slide = new ArrayList<RankItemNew>();

		//地域识别器
		locationExtraction locEx = new locationExtraction(); 
		
		//hot predict interface
		heatPredictNew hp = heatPredictNew.getInstance();
		
		if(ob == null)
			ob = new IKVOperationv2(IKVOperationv2.defaultTables[0]);
		
		rankList.getQualityList().clear();
		
		for(String key:cacheSet)
		{
			try {
				RankItemNew rankItemIn = (RankItemNew) instance.get(key);
				if(rankItemIn!=null){
					
					//人工删除接口：从本地文件中读出需要马上删除的ID并执行删除指令；
					if(hm_blacklist.containsKey(rankItemIn.getItem().getID().trim())
							|| hm_blacklist.containsKey(rankItemIn.getItem().getTitle().trim())){
						//从solr中真正删除
						String sCmd = "{\"delete\":{\"id\":\"z0\"}}";
						sCmd = sCmd.replace("z0",rankItemIn.getItem().getID());
						try {
							// 建模写入后台存储和计算引擎;构建json数据
							String rt = HttpRequest.sendPost(
									solrItemDBPath+"update/json"/*"http://10.32.28.119:8081/solr46/item/update/json"*/,
									sCmd);
							// test
							if (rt.indexOf("failed") > 0) {
								LOG.info("send post failed,del:" + sCmd + " rt=" + rt);
							}else{
								LOG.info("success,  del i2a:" + rankItemIn.getItem().getID());
								instance.del(key);
							}

						} catch (Exception e) {
							// TODO Auto-generated catch block
							LOG.error("send post failed", e);
						}
						
						StringBuffer sbTmp = new StringBuffer();
						sbTmp.append("blacklist del: ")
								.append(rankItemIn.getItem().getTitle()).append(" ")
								.append(rankItemIn.getWeight()).append(" ")
								.append(rankItemIn.getItem().getID());
						LOG.info(sbTmp.toString());
						continue;
					}

					
					Timestamp createTS = new Timestamp(rankItemIn.getCreateTimeStamp()*1000); 
					Timestamp nowTS = new Timestamp(System.currentTimeMillis());
					int periodUnit = fieldDicts.periodUnit;
					
					//分发优质等特殊分类数据
					distributeData(rankList,rankItemIn,locEx);
					
					//如果是AA等级，则只判断存活时间，不作降权,若存活则加入backuplist
					if(rankItemIn.getWeight().equals("AA")){
						
						if(!instance.withinMinutes(createTS, nowTS, (int)(rankItemIn.lifetime*periodUnit))){//如果RankItemNew超过生命周期，则删除
							rankList.getDelList().add(rankItemIn);
							instance.del(key);
							StringBuffer sbTmp = new StringBuffer();
							sbTmp.append("Delete AA: ").append(rankItemIn.getItem().getTitle())
							.append(" ").append(rankItemIn.getWeight()).append(" ").append(rankItemIn.getItem().getID()).append(" ").append(createTS.toString());
							LOG.info(sbTmp.toString());
						}else
							rankList.getBackupList().add(rankItemIn);
						continue;
					}
					
					/*
					 * 默认删除的都可能经历过降权，如果降过那么必须算出其降到多少档并对应修改权重
					 * 降权目前逻辑是：
					 * B档最高，8个小时的存活期；过4个小时降到C；再过4个小时不降了，直接删除；
					 * C档存活4个小时，不降权，可以直接删除；
					 * D档存活4个小时，不降权，可以直接删除；
					 */
					if(!instance.withinMinutes(createTS, nowTS, (int)(rankItemIn.lifetime*periodUnit))){//如果RankItemNew超过生命周期，则删除
						rankList.getDelList().add(rankItemIn);
						instance.del(key);
						StringBuffer sbTmp = new StringBuffer();
						sbTmp.append("Delete: ").append(rankItemIn.getItem().getTitle())
						.append(" ").append(rankItemIn.getWeight()).append(" ").append(rankItemIn.getItem().getID()).append(" ").append(createTS.toString());
						LOG.info(sbTmp.toString());
						continue;
					}
					
					
					/*
					 * 查询外部表达，如果外部表达发生改变那么重新投放；
					 * (为了降低复杂度，可以只处理最近30分钟到1个小时内的item)
					 */
					if(instance.withinMinutes(createTS, nowTS, 60)
							&& !instance.withinMinutes(createTS, nowTS, 30)){
						//查询外部features，看是否与当前不同
//						itemf itemInfo = queryInterface.getInstance().queryItemF(rankItemIn.getItem().getID());
//						if(itemInfo == null){
//							itemInfo = queryInterface.getInstance().queryItemF(rankItemIn.getItem().getTitle());
//						}
						
						itemf itemInfo = ob.queryItemF(rankItemIn.getItem().getID(), "c");
						
						//特征是否被外界修改
						boolean isChange = false;
//						if(itemInfo != null){
//							ArrayList<String> featuresNew = itemInfo.getFeatures();
//							ArrayList<String> featuresOld = rankItemIn.getItem().getFeatures();
//							//判断c是否有变化
//							if(featuresNew.size() != featuresOld.size())
//								isChange = true;
//							else{
//								int rt = compareFeatures(featuresNew,featuresOld);
//								if(rt != 0)
//									isChange = true;
//							}
//							
//						}
						//时间有变化
						if(rankItemIn.getItem().getModifyTime() > 0 && itemInfo.getModifyTime() > 0
								&& itemInfo.getModifyTime() > rankItemIn.getItem().getModifyTime()){
							isChange = true;
						}
						
						
						if(isChange){
							rankList.getDelList().add(rankItemIn);
							instance.del(key);
							StringBuffer sbTmp = new StringBuffer();
							sbTmp.append("Delete: ")
									.append(rankItemIn.getItem().getTitle())
									.append(" ").append(rankItemIn.getWeight())
									.append(" ").append(rankItemIn.getItem().getID())
									.append(" ").append(createTS.toString())
									.append(" ").append(rankItemIn.getItem().getFeatures());
							LOG.info(sbTmp.toString());
							
							//修改特征
							rankItemIn.getItem().setFeatures(itemInfo.getFeatures());					
							
							
							//重新投放
							sbTmp = new StringBuffer();
							sbTmp.append("features Add (traverback): ")
									.append(rankItemIn.getItem().getTitle())
									.append(" ").append(rankItemIn.getWeight())
									.append(" ")
									.append(rankItemIn.getItem().getID())
									.append(" ")
									.append(rankItemIn.getItem().getFeatures());
							;
							
							//指令进行新增
							rankList.getNewList().add(rankItemIn);
							instance.add(key, rankItemIn);
							
							LOG.info(sbTmp.toString());
						}
						
					}
					
					
					
					/*
					 * 查询外部权重，判断是否需要提升hot level；如果需要，那么删除，并重新投放，重新投放的权重是新的，生成时间？
					 * 暂时处理：如果有加权的现象，那么先简单重新投放之，再观察；
					 * (编辑给的文章不作处理)
					 */
					if (rankItemIn.getOthers().indexOf("fromEditor") < 0)
					{
						
						String outsideWeightAndBestID = hp.getNewsHotLevel(rankItemIn);
						if(outsideWeightAndBestID!=null && !outsideWeightAndBestID.isEmpty()){
							String[] secs = outsideWeightAndBestID.split("\\s");
							if(secs.length == 2){
								String outsideWeight = secs[0];
								String bestID = secs[1];
								
								if(outsideWeight.compareTo(rankItemIn.getWeight()) < 0){
									//指令进行删除，只是通知存储计算层；
									rankList.getDelList().add(rankItemIn);
									//再根据业务逻辑，修正出新的RankItemNew
									rankItemIn.weightingTo(outsideWeight);
									
									StringBuffer sbTmp = new StringBuffer();
									sbTmp.append("Weighting Add (traverback): ").append(rankItemIn.getItem().getTitle())
									.append(" ").append(rankItemIn.getWeight()).append(" ").append(rankItemIn.getItem().getID());
									
//									//20140827 add likun：此处应该有新逻辑,如果命中的是simIDList中其它ID，那么应该重新投放并且换ID
//									if(!bestID.isEmpty() && !bestID.equals(RankItemNew.getID())){
//										RankItemNew.setID(bestID);
//										sbTmp.append(".change to best ID: ").append(bestID);
//									}
									
									//指令进行新增
									rankList.getNewList().add(rankItemIn);
									instance.add(key, rankItemIn);
									
									LOG.info(sbTmp.toString());
									
									//加入候补；
									if(rankItemIn.getWeight().compareTo("B") <= 0) 
										tempbackList_ABC.add(rankItemIn);
			
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
					int grade = 'E' - rankItemIn.getWeight().charAt(0);
			
					if (rankItemIn.degraded > 0) {
						
						if (!instance.withinMinutes(createTS, nowTS, (grade-rankItemIn.degraded+1)*fieldDicts.degradePeriod*periodUnit))
						{
							boolean canDegr = true;
//此段代码很不必要，本身意义是为了避免这一轮被降权，下一轮发现outsideweight比当前weight大，又要加权投放，如此就陷入了死循环；但是目前其实并不会因为outside weight更大而加权，只会因为outside
//比初始权重更大，才会加权投放（weightingTo）							
//							/*
//							 * 此处加上一个判断，就是判断20分钟内 outside
//							 * weight是否大于当前要降到的逻辑weight，如果大于，那么就不要执行降权；
//							 */
//							
//							long currentTime = System.currentTimeMillis();
//							long lastOutSideTime = RankItemNew.getTimeFromOutSide();
//							
////							//test log
////							LOG.info("test:!");
////							LOG.info(RankItemNew.getWeightFromOutside());
////							LOG.info("outside time ="+(System.currentTimeMillis()-RankItemNew.getTimeFromOutSide())/60000);
////							LOG.info(RankItemNew.getWeight());
////							LOG.info(RankItemNew.getUrl());
//							
//							
//							if(currentTime < (lastOutSideTime+1000*20*60))
//							{
//								String lastOutSideWeight = RankItemNew.getWeightFromOutside();
//								//degrToWeight是逻辑上要降权到的weight
//								String degrToWeight = String.valueOf((char)(RankItemNew.getWeight().charAt(0)+(grade-RankItemNew.degraded+1)));
//								LOG.info(RankItemNew.getID());
//								LOG.info(RankItemNew.getTitle());
//								LOG.info("lastOutSideWeight="+lastOutSideWeight+" degrToWeight="+degrToWeight);
//								
//								if(lastOutSideWeight.compareTo(degrToWeight) < 0)
//								{
//									canDegr = false;
//									LOG.info("not degr because outside weight = "+lastOutSideWeight);
//								}
//							}
							
							//可以降权
							if(canDegr == true)
							{
								rankList.getDegrList().add(rankItemIn);
								rankItemIn.degraded--;
								instance.add(key, rankItemIn);
								StringBuffer sbTmp = new StringBuffer();
								sbTmp.append("Degrade: ").append(rankItemIn.getItem().getTitle())
								.append(" ").append(rankItemIn.getWeight()).append(" ").append(rankItemIn.getItem().getID()).append(" others=").append(rankItemIn.getOthers());
								LOG.info(sbTmp.toString());

							}
							
							LOG.info("--------");
						}
					}
					
					//筛选backup
					if(rankItemIn.getItem().getDocType().equals("slide")){//如果是图集，也加入候补队列
						tempbackList_slide.add(rankItemIn);
						LOG.info("Put slide into backlist: "+rankItemIn.getItem().getID()+" "+rankItemIn.getWeight());
						LOG.info(rankItemIn.getItem().getUrl()+" "+rankItemIn.getItem().getTitle());
						
					} else {

						// B以上，或者fromEditor,或者fromApp，加入候补
						if ((rankItemIn.getWeight().compareTo("C") == 0 && rankItemIn.degraded >= (grade-1))
								|| (rankItemIn.getWeight().compareTo("B") == 0 && rankItemIn.degraded >= (grade - 2))
								|| (rankItemIn.getWeight().compareTo("A") == 0 && rankItemIn.degraded >= (grade - 3)))
							tempbackList_ABC.add(rankItemIn);
						else if (rankItemIn.getOthers().indexOf("fromEditor") >= 0) {
							tempbackList_ABC.add(rankItemIn);
						}
					}
					
				}
				else {
					instance.del(key);
				}
			} catch (Exception e) {
				LOG.error("ERROR traverBack:"+key, e);
				//@test
				e.printStackTrace();
				continue;
			}	
		}
		//把tempbackList中的item加入backupList, 现取A级、B级、C级的文章，按pv排序
		Collections.sort(tempbackList_slide, new Comparator<RankItemNew>() {
			public int compare(RankItemNew item1, RankItemNew item2) {
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
		for(RankItemNew itemR : tempbackList_slide)
		{
			if(itemR.getItem().getID().equals(oldID))
				continue;
			rankList.getBackupList().add(itemR);
			oldID = itemR.getItem().getID();
		}
		
		Collections.sort(tempbackList_ABC, new Comparator<RankItemNew>() {
			public int compare(RankItemNew item1, RankItemNew item2) {
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
		for(RankItemNew itemR : tempbackList_ABC)
		{
			if(itemR.getItem().getID().equals(oldID))
				continue;
			rankList.getBackupList().add(itemR);
			oldID = itemR.getItem().getID();
		}
				
		return 1;
	}
	
	/**
	 * 比较两个features数组是否表达一样
	 * c1\sc\s1\cn\e\et，依次看是不是更完整
	 * 
	 * 注意：
	 * 	这个算法是倾向于新features的，所以计算时候，主要还是挑旧的features缺陷，从而降低计算复杂度；
	 *  1）如果old和new都一样，那么返回0；
	 *  2）如果new有c1而old没有，那么认为new更好，返回-1；
	 *  3）如果new有sc\cn\s1\e而old没有，那么认为new更好，返回-1；
	 *  4）如果new没有c1，同时也没有sc\cn\s1\e,而old有c1，那么old更好，返回2；
	 *  5）其它情况下，可以认为特征不一样；返回1
	 * 
	 * @param featuresNew, featuresOld
	 * @return 
	 * 		-1 表示featuresNew的特征更全面完整；0是相等;2表示featuresOld的特征更全面完整；1表示特征不一样，但是没法判断谁更完整有价值
	 */
	private int compareFeatures(ArrayList<String> featuresNew,
				ArrayList<String> featuresOld) {
			// TODO Auto-generated method stub
		if(featuresNew == null || featuresOld == null)
			return 0;
		
		
		boolean isOldC1 = false, isNewCE = false;
		boolean isSame = true;
		//统计数据
		HashMap<String,Float> hm_tmp = new HashMap<String,Float>();
		for(int i=0;i<featuresOld.size()-2;i+=3){
			String feature = featuresOld.get(i);
			String type = featuresOld.get(i+1);
			float weight = 0f;
			try{
				weight = Float.valueOf(featuresOld.get(i+2));
			}catch(Exception e){
				weight = 0f;
			}
			
			if(Math.abs(weight) < 0.5f)
				continue;
			
			if(type.equals("c") && weight > 0)
				isOldC1 = true;
			
			
			hm_tmp.put(feature+type, 1.0f);
		}
		
		int n1 = hm_tmp.size();
		
		HashMap<String,Float> hm_tmp1 = new HashMap<String,Float>();
		for(int i=0;i<featuresNew.size()-2;i+=3){
			String feature = featuresNew.get(i);
			String type = featuresNew.get(i+1);
			float weight = 0f;
			try{
				weight = Float.valueOf(featuresNew.get(i+2));
			}catch(Exception e){
				weight = 0f;
			}
			
			if(Math.abs(weight) < 0.5f)
				continue;
			
			if((type.equals("c") || type.equals("sc") || type.equals("cn") || type.equals("s1") || type.equals("e") || type.equals("et")) && weight > 0)
				isNewCE = true;
			
			hm_tmp1.put(feature+type, 1.0f);
			
			if(type.equals("c") && weight > 0&& !hm_tmp.containsKey(feature+type))
				return -1;
			if((type.equals("sc") || type.equals("cn") || type.equals("s1") || type.equals("e") || type.equals("et")) && !hm_tmp.containsKey(feature+type))
				return -1;		
			
			if(!hm_tmp.containsKey(feature+type))
				isSame = false;
		}
		
		//相等
		int n2 = hm_tmp1.size();
		if(n1 == n2 && isSame == true)
			return 0;
			
		//old更全面
		if(isOldC1 == true && isNewCE == false)
			return 2;
	
		//认为特征不一样，但是没法评价谁更好
		if(n2 > 0)
			return 1;		
		
		return 2;
	}

//	/**
//	 * 判断RankItemNew的other字段属性，并分发到天气、地域等相应数据中去；
//	 * 注意：
//	 * 
//	 * @param rankList 数据分发流
//	 * @param RankItemNew
//	 * @param hm_hotItemIDs 热点新闻map，做热点新闻识别
//	 * @param locationExtraction 地域识别器
//	 * @param locEx 
//	 * 暂时否决：@return 内容是否可以参与投放模型计算，投放到内容匹配、协同、新闻追踪等算法去；
//	 * 天气新闻样例：other : fromImcp|!|loc=北京|!|channel=weather|!|pic=1|1|1
//	        地域汽车新闻样例： other : fromImcp|!|loc=北京|!|channel=auto|!|pic=1|1|1
//                   游戏新闻样例 ： other : fromImcp|!|channel=games|!|pic=1|1|1
//       
//	 */
//	private void distributeData(RankList rankList, RankItemNew RankItemNew, HashMap<String, Double> hm_hotItemIDs, locationExtraction locEx) {
//		// TODO Auto-generated method stub
//		//如果内容命中了热度，那么加入hotList，并记录hot score；这样dis程序可以根据这个socre重新排序
//		ArrayList<String> simList = RankItemNew.getSimIDList();
//		double maxScore = 0;
//		Double score = hm_hotItemIDs.get(RankItemNew.getID());
//		if(score != null){
//			maxScore = score;
//		}
//		if(simList != null){
//			for(String id:simList){
//				score = hm_hotItemIDs.get(id);
//				if(score == null
//						|| score <= 0)
//					continue;
//					
//				if(score > maxScore)
//					maxScore = score;
//			}
//			
//		}
//		if(maxScore > 0){
//			String others = RankItemNew.getOthers();
//			if(others.indexOf("commentScore")>=0)
//			{
//				others = others.replaceAll("commentScore=[\\d\\.]{0,32}","commentScore="+maxScore);
//				RankItemNew.setOthers(others);
//			}else
//				RankItemNew.setOthers(RankItemNew.getOthers()+"|!|commentScore="+maxScore);
//			rankList.getHotList().add(RankItemNew);
//		}
//		
//		
//		//其它业务的数据分流
//		HashMap<String,String> hm_others = parseItemOthersField(RankItemNew.getOthers());
//		String source = hm_others.get("source");
//		if(source != null
//				&& source.equals("spider"))
//		{
//			String channel = hm_others.get("channel");
//			if(channel == null)
//				return;
//			//做业务数据分割
//			if(channel.equals("auto")){
//				rankList.getAutoList().add(RankItemNew);
//			}else if(channel.equals("games")){
//				rankList.getGamesList().add(RankItemNew);
//			}else if (hm_others.containsKey("loc")){// 做地域映射发现
//				rankList.getLocList().add(RankItemNew);
//			}
//		}else {
//			if (!(hm_others.containsKey("loc")))// 做地域映射发现
//			{
//				// 必须是society、mainland、house、finance等四个category的内容，才是地域信息
//				// pv得大于10才能做地域新闻
//				if (RankItemNew.getCategory() != null
//						&& !(RankItemNew.getCategory().matches(
//								"^.*?(world|finance|mil|hongkong).*?$"))) {
//					String loc = locEx.extractLocation(RankItemNew.getItem());
//					if (loc != null && !loc.isEmpty()) {
//						// add likun
//						// 20150401,特殊处理：一些标题含有“北京”、“中国”、“大陆”的自媒体内容分类不准，使world分成mainland，导致本地数据出错，特殊处理之；
//						if (!(loc.equals("北京市")
//								&&RankItemNew.getCategory().equals("mainland")
//								&&RankItemNew.getUrl()!=null
//								&&RankItemNew.getUrl().indexOf("cdn.iclient")>0))
//						{
//							RankItemNew.setOthers(new StringBuffer(RankItemNew
//									.getOthers()).append("|!|loc=").append(loc)
//									.toString());
//							rankList.getLocList().add(RankItemNew);
//						}
//					}
//				}
//				
//				//auto\games等频道数据补充
//				if (RankItemNew.getCategory() != null
//						&& RankItemNew.getCategory().equals("games")){
//					rankList.getGamesList().add(RankItemNew);
//				}
//				if (RankItemNew.getCategory() != null
//						&& RankItemNew.getCategory().equals("auto")){
//					rankList.getAutoList().add(RankItemNew);
//				}
//				
//				//热点数据入备选
//				
//				
//			} else
//				rankList.getLocList().add(RankItemNew);
//		}
//	}

	/**
	 * 主要暴露接口，获取内容池数据更新
	 * 注意：
	 * 
	 * @param source 原始文档
	 * @return RankList
	 */
	public RankListNew getUpdates(String appid)
	{
		//读取各种路径配置
		try{
			AutoLoadingConfiguration config = new AutoLoadingConfiguration("conf/config.properties", LOG);
			this.solrItemDBPath = config.getValueBykey("solrItemLibPath");
			this.itemsPoolDBInRedis = Integer.valueOf(config.getValueBykey("itemPoolDB"));
			this.itemsPoolDBInRedis_fromlog = Integer.valueOf(config.getValueBykey("itemPoolDB_fromlog"));
			this.isRealRankModel = config.getValueBykey("isRealRankModel");
		}catch(Exception e){
			LOG.info("error for path config!");
			e.printStackTrace();
			return null;
		}
		
		LOG.info("solrItemDBPath="+this.solrItemDBPath);
		LOG.info("itemsPoolDBInRedis="+this.itemsPoolDBInRedis);
		LOG.info("isRealRankModel="+this.isRealRankModel);
		
		//黑名单数据获取
		//List<String> blacklist = BlackListData.getInstance().get_blacklist();
		Map<String, Set<String>> blacklist_map = getBlacklistMap();
		if(blacklist_map != null && !blacklist_map.isEmpty()){
			hm_blacklist.clear();
			Set<String> all_blacklistSet = blacklist_map.get("all");
			if(all_blacklistSet != null && !all_blacklistSet.isEmpty()){
				for(String titleOrID:all_blacklistSet){
					hm_blacklist.put(titleOrID.trim(), "");
					LOG.info("blacklist is:"+titleOrID);
					
					//从solr中查找对应item,如果状态是false，直接删除
					dealBlackItemInSolr(titleOrID);
					
				}
			}
		
		}
		
		
		
		
//		if(blacklist != null && !blacklist.isEmpty())
//		{
//			hm_blacklist.clear();
//			for(String titleOrID:blacklist){
//				hm_blacklist.put(titleOrID.trim(), "");
//				LOG.info("blacklist is:"+titleOrID);
//				
//				//从solr中查找对应item,如果状态是false，直接删除
//				dealBlackItemInSolr(titleOrID);
//				
//			}
//		}
		
		//获取存放在redis的编辑推荐的items
		ArrayList<itemf> appitemList = getFrontItemfsFromPool(appid);
		if(appitemList == null)
		{
			LOG.info("appitemList from pool is null");
			return null;
		}
		LOG.info("appitemList from pool size: "+appitemList.size());

		
		//编辑下线指令获取处理,eg source=spider|!|channel=ent|!|bn=明星秘闻|!|tags=凤凰网娱乐-明星-明星秘闻|!|state=1|!|channels={"电影":1}
		//注意：0 下线 2 上线
		appitemList = dealOfflineItems(appitemList,hm_blacklist);
		
		
	
		RankListNew rankList = new RankListNew();
		//过滤和规范外部来的itemFront列表
		ArrayList<RankItemNew> RankItemNewListFromFront = instance.filterItems(appid,rankList,appitemList);
		LOG.info("RankItemNewListFromFront Size: "+RankItemNewListFromFront.size());
	
		//新内容，统一在此处计算热度，weight，pv
		//@test
		LOG.info("cmp hot for items front,test:");
		for(RankItemNew r_item: RankItemNewListFromFront){
			LOG.info("appitemList before rank:"+r_item.getItem().getID()+" "+r_item.getItem().getTitle()+" "+r_item.getWeight());
		}
		
		heatPredictNew hp = heatPredictNew.getInstance();
		hp.updateHeatData();
		hp.setItemsHotLevel(RankItemNewListFromFront);
		
		// @test
		LOG.info("cmp hot for items front,test:");
		for (RankItemNew r_item : RankItemNewListFromFront) {
			LOG.info("appitemList after rank:" + r_item.getItem().getID() + " " + r_item.getItem().getTitle() + " " + r_item.getWeight());
		}
		LOG.info("---------------------");
		
//		/*
//		 * 此处加入一个新逻辑，add likun 2016/09/08;为了系统鲁棒性，以及内容筛选机制的查缺补漏，我们会从一个db中读入后台检验算法计算出来的itemfront
//		 * 这批itemfront在后台检验时候，已经给了具体的热度hot，所以不用走热度；
//		 */
//		//获取存放在redis的编辑推荐的items
//		ArrayList<itemf> appitemList_fromlog = getFrontItemfsFromPool(appid,this.itemsPoolDBInRedis_fromlog);
//		if(appitemList_fromlog != null)
//		{
//			LOG.info("appitemList_fromlog from pool size: "+appitemList_fromlog.size());
//			
//			//编辑下线指令获取处理,eg source=spider|!|channel=ent|!|bn=明星秘闻|!|tags=凤凰网娱乐-明星-明星秘闻|!|state=1|!|channels={"电影":1}
//			//注意：0 下线 2 上线
//			appitemList_fromlog = dealOfflineItems(appitemList_fromlog,hm_blacklist);
//			
//		
//			//过滤和规范外部来的itemFront列表
//			ArrayList<RankItemNew> RankItemNewListFromFront_fromlog = instance.filterItems(appid,rankList,appitemList_fromlog);
//			LOG.info("RankItemNewListFromFront_fromlog Size: "+RankItemNewListFromFront_fromlog.size());
//		
//			//@test
//			LOG.info("display hot for items front from log,test:");
//			for(RankItemNew r_item: RankItemNewListFromFront){
//				LOG.info("appitemList HOT:"+r_item.getItem().getID()+" "+r_item.getItem().getTitle()+" "+r_item.getWeight());
//			}
//			LOG.info("---------------------");
//	
//			//RankItemNewListFromFront_fromlog要合并进入RankItemNewListFromFront，同simID应该取
//			
//		}
		

		
	
		
		//新内容统一在此处set lifetime degr数据
		for(RankItemNew r_item: RankItemNewListFromFront){
			r_item.setLifeTimeAndDegraded();	
			
			//@test,输出life time，并debug
			LOG.debug(r_item.getItem().getID()+"'s lifeTime = "+r_item.lifetime);
			
		}
		
		
		//按照权重、时间等重新排序；尽量把最好的、最新的内容投放,因为投放内容数量有限制
		Collections.sort(RankItemNewListFromFront, new Comparator<RankItemNew>() {
			public int compare(RankItemNew item1, RankItemNew item2) {
				int rt = item2.getWeight().compareTo(item1.getWeight());
				if(rt > 0)
					return -1;
				if(rt < 0)
					return 1;
				return 0;
			}
		});
		
		//遍历进行过排重过滤后的RankItemNewFromFront列表，把新的item和权重变化的item加入oscache
		int check = traverFront(appid,rankList, RankItemNewListFromFront);
		if(check==1)
			LOG.info("traverFront succeed.");
		else
			LOG.info("traverFront failed.");
	
		//遍历后台oscache存储的RankItemNew，加入权重调整的item和过期删除的item,以及删除和首页重复的item
		check = traverBack(rankList);
		if(check==1)
			LOG.info("traverBack succeed.");
		else
			LOG.info("traverBack failed.");
		
		HashSet<String> keySet = instance.getKeys();
		LOG.info("KeySet Size: "+keySet.size());
		
		return rankList;
	}
	
	/**
	 * 编辑下线指令获取和处理
	 * eg source=spider|!|channel=ent|!|bn=明星秘闻|!|tags=凤凰网娱乐-明星-明星秘闻|!|state=1|!|channels={"电影":1}
	 * 注意：
	 * 	0 下线 2 上线
	 * @param appitemList原始上下线指令列表；hm_blacklist，下线指令列表
	 * @return 上线指令列表
	 */
	private ArrayList<itemf> dealOfflineItems(ArrayList<itemf> appitemList,
		HashMap<String, String> hm_blacklist) {
		// TODO Auto-generated method stub
		if(appitemList == null || appitemList.isEmpty())
			return appitemList;
		
		if(hm_blacklist == null){
			
			LOG.error("WRONG:hm_blacklist == null");
			return null;
		}
		
		ArrayList<itemf> al_res = new ArrayList<itemf>();
		for(itemf item:appitemList){
			String others = item.getOther();
			//下线
			if(others.indexOf("state=0")>=0){
				hm_blacklist.put(item.getID(), "");
				LOG.info("offline is:"+item.getID());
				
				//从solr中查找对应item,如果状态是false，直接删除
				dealBlackItemInSolr(item.getID());
			}
			
			//上线（兼容旧版本没有泛编流程，没有state字段）
			if(others.indexOf("state=2")>=0 || others.indexOf("state=")<0){
				al_res.add(item);
			}
		}
		
		return al_res;
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
	 * 在oscache中添加RankItemNew
	 * 注意：
	 * 
	 * @param key， RankItemNew
	 * @return 
	 */
	public void add(String key, RankItemNew value)
	{
		if(key==null)
			return;
		if(value==null)
			return;
		HashSet<String> keySet = getKeys();
		keySet.add(key);
		String json = "";
		try {
			json = JsonUtils.toJson(value, RankItemNew.class);
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
	public RankItemNew get(String key)
	{
		if(key==null)
			return null;
		RankItemNew value = null;
		String jsonSource = "";
		try{
			jsonSource = (String) instance.osCache.get(key);
			value = JsonUtils.fromJson(jsonSource, RankItemNew.class);
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
