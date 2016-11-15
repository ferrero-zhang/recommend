/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.editorChannelPreload;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.PreloadItemFromSolrUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.ToutiaoHotUtil;

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
 *          1.0          2016年3月7日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class ToutiaoPreload implements Runnable {
	private static Log LOG = LogFactory.getLog("ToutiaoPreload");
	private PropertiesConfiguration configs;
	public ToutiaoPreload(PropertiesConfiguration configs) {
		// TODO Auto-generated constructor stub
		this.configs = configs;
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		preload();
	}
	
	public void preload(){
		String key = "ifengtoutiao";
		//用于最终投放
		List<FrontNewsItem> newslist = new ArrayList<FrontNewsItem>();
	
		
		Map<String, Set<String>> blacklistMap = getBlacklistMap();
		String channel1 = "ifengtoutiao";
		List<PreloadItem> solrToutiaoItemList = getPreloadNewsItem(channel1);
		
		solrToutiaoItemList = parserImcpId(solrToutiaoItemList);
		//热度排序
		
		solrToutiaoItemList = ToutiaoHotUtil.getInstance().rankItemList(solrToutiaoItemList);
		
		//获取当前头条前二十条id
		Set<String> idSetFilter = getToutiaoFrontNewsIdSet();
		List<FrontNewsItem> toutiaoFront20 = new ArrayList<FrontNewsItem>();
		LOG.info("Toutiao editor id set size : "+idSetFilter.size());
		
		//黑名单过滤
		//头条前30条做过滤排重
		//获取全站下线黑名单
		//时间控制，时间超过三天的下线
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Set<String> allBlackSet = blacklistMap.get("all");
		if(allBlackSet == null){
			allBlackSet = new HashSet<String>();
		}
		Iterator<PreloadItem> it = solrToutiaoItemList.iterator();
		while(it.hasNext()){
			PreloadItem item = it.next();
			//过滤全站下线黑名单
			if(allBlackSet.contains(item.getFitem().getDocId()) || allBlackSet.contains(item.getFitem().getTitle())){
				LOG.info("hit in allChannel blacklist "+item.getFitem().getDocId()+" title : "+item.getFitem().getTitle());
				it.remove();
				continue;
			}
			if(idSetFilter.contains(item.getFitem().getId_value())){
				LOG.info("hit editor news cmppid : "+item.getFitem().getDocId()+" imcpId : "+item.getFitem().getId_value());
				toutiaoFront20.add(item.getFitem());
				continue;
			}
			//时间控制超过三天的下线
			try {
				Date date = formate.parse(item.getFitem().getDate());
				long time = System.currentTimeMillis() - date.getTime();
				if(time > 3*24*60*60*1000){
					LOG.warn(item.getFitem().getDocId()+" Out of time "+item.getFitem().getTitle());
					continue;
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error("Parse time error ",e);
				continue;
			}
			newslist.add(item.getFitem());
		}
		
		Gson gson = new Gson();
		LOG.info(key+" dis size : "+newslist.size());
		String disStr = gson.toJson(newslist);
//System.out.println(disStr);
		disToRedis(key, 8*60*60*1000, disStr);

		
		String disStr2 = gson.toJson(toutiaoFront20);
		disToRedis("toutiaoFront20", 8*60*60*1000, disStr2);
		
		//test 给于老师写预热的测试数据
		List<FrontNewsItem> testItem = new ArrayList<FrontNewsItem>();
		String channel2 = "editor OR ifengvideo";
		List<PreloadItem> testList = getPreloadNewsItem_test(channel2);
		testList =parserImcpId(testList);
		//热度测试
		testList = ToutiaoHotUtil.getInstance().rankItemList_test(testList);
		for(PreloadItem item : testList){
			Date date;
			try {
				date = formate.parse(item.getFitem().getDate());
				long time = System.currentTimeMillis() - date.getTime();
				if(time > 3*24*60*60*1000){
					LOG.warn(item.getFitem().getDocId()+" Out of time "+item.getFitem().getTitle());
					continue;
				}
				if(idSetFilter.contains(item.getFitem().getId_value())){
					LOG.info("ToutiaoPreload hit editor news cmppid : "+item.getFitem().getDocId()+" imcpId : "+item.getFitem().getId_value());
					toutiaoFront20.add(item.getFitem());
					continue;
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error("Parse time error ",e);
				continue;
			}

			testItem.add(item.getFitem());
		}
		LOG.info("ifengtoutiao_preload dis size : "+testItem.size());
		String testStr = gson.toJson(testItem);
//		System.out.println(testStr);
		disToRedis("ifengtoutiao_preload", 8*60*60*1000, testStr);
		
		
	}
	
	private Set<String> getToutiaoFrontNewsIdSet(){
		Set<String> toutiaoFrontNewsIdSet = new HashSet<String>();
		try{
			Jedis jedis = new Jedis("10.50.8.118", 1379, 1000);
//			jedis.auth("MROh3333w32k");
			jedis.select(2);
			String temp = jedis.get("articleIds");
			Gson gson = new Gson();
			toutiaoFrontNewsIdSet = gson.fromJson(temp, new TypeToken<Set<String>>() {
			}.getType());
		} catch (Exception e) {
			LOG.error(" ", e);
		}
		
		return toutiaoFrontNewsIdSet;
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

			Jedis jedis = new Jedis("10.90.7.60", 6379, 10000);
			jedis.select(1);
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
	 * 解析IMCPID，将imcpid暂存在id_value中
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private List<PreloadItem> parserImcpId(List<PreloadItem> tempItemList){
		List<PreloadItem> returnlist = new ArrayList<PreloadItem>();
		if(tempItemList == null || tempItemList.isEmpty()){
			return tempItemList;
		}
		for(PreloadItem pItem : tempItemList){
			FrontNewsItem fitem = pItem.getFitem();
			String other = fitem.getOthers();
			int begin = other.indexOf("?aid=");
			if(begin >= 0 ){
				//兼容娱乐cmpp_ID : url=http://api.iclient.ifeng.com/ipadtestdoc?aid=cmpp_42604551000&cmpp_channelid=ent
				int end = other.indexOf("|!|", begin);
				String idStr = other.substring(begin+5, end);
//				if(idStr.contains("cmpp")){
//					idStr = idStr.substring(0, 16);
//				}
				String imcpId = idStr;
				fitem.setId_value(imcpId);
				returnlist.add(pItem);
			} else {
//				LOG.warn("cannot parser "+fitem.getDocId()+" "+other);
				//如果是ifengpc的数据需要加前缀
				if(other.indexOf("ifengpc")>0){
					String algcmppId = "algcmpp|"+fitem.getDocId();
					fitem.setId_value(algcmppId);
					returnlist.add(pItem);
				} else if (other.indexOf("ifengvideo")>0){
					String video = "ifengvideo|"+fitem.getDocId();
					fitem.setId_value(video);
					returnlist.add(pItem);
				} else {
					LOG.warn("cannot parser "+fitem.getDocId()+" "+other);
				}
				
				
			}
			
		}
		
		return returnlist;
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
	private List<PreloadItem> getPreloadNewsItem(String channel){
		List<PreloadItem> preloadItemList = new ArrayList<PreloadItem>();

		int rows = 1000;//预加载数据量

//		String channel = "ifengtoutiao";
//channel = "editor";
		String queryStr = "other:("+channel+") AND available:(true)";

		SolrQuery query = generaterSolrQuery(queryStr, rows);
		//temp  头条优化，先不切换至8082
		String solrUrl = configs.getString("solrUrl");
//		String solrUrl = "http://10.32.28.119:8081/solr46/item/";
		List<PreloadItem> itemList = PreloadItemFromSolrUtil.preloadItemFromSolrWithSimId_test(query,solrUrl,false);
		preloadItemList.addAll(itemList);

		
		return preloadItemList;
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
	private List<PreloadItem> getPreloadNewsItem_test(String channel){
		List<PreloadItem> preloadItemList = new ArrayList<PreloadItem>();

		int rows = 5000;//预加载数据量

//		String channel = "ifengtoutiao";
//channel = "editor";
		String queryStr = "other:("+channel+") AND available:(true)";

		SolrQuery query = generaterSolrQuery(queryStr, rows);
		String solrUrl = "http://10.32.28.119:8082/solr46/item/";
		List<PreloadItem> itemList = PreloadItemFromSolrUtil.preloadItemFromSolrWithSimId_test(query,solrUrl,false);
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
	
	public static void main(String[] args){
		PropertiesConfiguration configs = new PropertiesConfiguration();
		configs.setEncoding("UTF-8");
		try {
			configs.load("conf/AlgorithmChannelPreload.properties");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		configs.setReloadingStrategy(new FileChangedReloadingStrategy());
		LOG.info("Loading config success ~");
		
		ToutiaoPreload preload = new ToutiaoPreload(configs);
		
		while(true){
			preload.preload();
			try {
				Thread.sleep(3*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
