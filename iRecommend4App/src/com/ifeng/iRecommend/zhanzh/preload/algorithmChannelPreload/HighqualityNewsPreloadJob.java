/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload;



import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;

import com.google.gson.Gson;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.PreloadItemFromSolrUtil;
import com.ifeng.webapp.simArticle.client.SimDocClient;

import redis.clients.jedis.Jedis;

/**
 * <PRE>
 * 作用 : 预加载基于用户阅读偏离度计算得出的优质数据列表
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
 *          1.0          2016年6月20日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class HighqualityNewsPreloadJob implements Runnable{
	private static Log LOG = LogFactory.getLog("HighqualityNewsPreloadJob");
	private PropertiesConfiguration configs;
	private static IKVOperationv2 query = new IKVOperationv2(IKVOperationv2.defaultTables[0]);
	public HighqualityNewsPreloadJob(PropertiesConfiguration configs) {
		// TODO Auto-generated constructor stub
		this.configs = configs;
	}
	
	public HighqualityNewsPreloadJob() {
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void run() {
		try {
			preload();
			preloadForSharedAndStore();
		} catch (Exception e) {
			LOG.info("HighqualityNewsPreloadJob thread error ~"+e.getMessage());
		}
	}

	/**
	 * 主流程
	 * 
	 * 1、将id转化为cmppid，并映射到体系中的数据
	 * 2、投放至redis中
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	private void preload(){
		LOG.info("HighqualityNewsPreload Start ~");
		String idStr = loadingDataFromRedis("deviateUserPCT");
//		List<FrontNewsItem> itemList = loadingImcpFItemListFromRedis();
		List<FrontNewsItem> itemList = loadingfItemListFromSolr(idStr);
		Gson gson = new Gson();
		String disStr = gson.toJson(itemList);
		String channel = "凤凰热闻榜";
		disToRedis(channel, 8*60*60*1000, disStr,2);
		LOG.info("HighqualityNewsPreload finished ~");
	}
	/**
	 * 主要目的
	 * 
	 * 将计算好的分享和收藏数据id映射到体系中的数据
	 * 投放到redis中
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	private void preloadForSharedAndStore() {
		LOG.info("preloadForSharedAndStore Start ~");
		String idStrShare = loadingDataFromRedis("userShareGroup");
		String idStrStore = loadingDataFromRedis("userStoreGroup");
		List<FrontNewsItem> itemListShare = loadingfItemListFromSolrForShare(idStrShare);
		List<FrontNewsItem> itemListStore = loadingfItemListFromSolrForShare(idStrStore);
		System.out.print(itemListShare.size());
		Gson gson = new Gson();
		String disStrShare = gson.toJson(itemListShare);
		String disStrStore = gson.toJson(itemListStore);
		String channelShare = "凤凰分享榜";
		String channelStore = "凤凰收藏榜";
		disToRedis(channelShare, 8*60*60*1000, disStrShare,11);
		disToRedis(channelStore, 8*60*60*1000, disStrStore,11);
		LOG.info("preloadForSharedAndStore Finashed ~");		
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
	private void disToRedis(String tableName,long validTime,String disStr,int db){
		if(disStr == null){
			return ;
		}
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
		
		try{

			Jedis jedis = new Jedis("10.90.7.60", 6379, 1000);
			jedis.select(db);
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
	
	private List<FrontNewsItem> loadingImcpFItemListFromRedis(){
		String idStr = loadingDataFromRedis("deviateUserPCT");
		List<FrontNewsItem> fItemList = new ArrayList<FrontNewsItem>();
		if(idStr == null){
			LOG.warn("Loading qualityNews id is null ~");
			return fItemList;
		}
		
		
		String[] idList = idStr.split(",");
		
		for(String id : idList){
			FrontNewsItem fItem = queryItemByImcpId(id);
			if(fItem != null){
				//客户端无法展示cmpp数据，暂时过滤掉cmpp数据
				if(fItem.getDocId().contains("cmpp")){
					continue;
				}
				fItemList.add(fItem);
			} else {
				LOG.warn("Get frontItem error ~ "+id);
			}
		}
		return fItemList;
	}
	
	private List<FrontNewsItem> loadingfItemListFromSolr(String idStr){
		List<FrontNewsItem> fItemList = new ArrayList<FrontNewsItem>();
		if(idStr == null){
			LOG.warn("Loading qualityNews id is null ~");
			return fItemList;
		}
		
		Set<String> filterSet = new HashSet<String>();
		Gson gson  = new Gson();
		String[] idList = idStr.split(",");
		
		for(String id : idList){
			editordocument eitem = queryByImcpId(id);
			if(eitem == null || eitem.body.title == null){
				LOG.error(" title is null "+id);
				continue;
			}
			String title = eitem.body.title;
			//直接用id查ikv
			itemf item = query.queryItemF(title, "c");
			if(item == null){
				LOG.error(" item is null "+id +" "+title);
				continue;
			}
			String str =getItemSimId(title, item.getID(), null);
			//需要加入非空检验
			if(str == null){
				continue;
			}
			simid simItem = null;
			try{
				simItem = gson.fromJson(str, simid.class);
			} catch (Exception e){
				LOG.error(" ",e);
				continue;
			}
			
			FrontNewsItem fitem=new FrontNewsItem();
			if(simItem.clusterId != null){
					if(simItem.clusterId.indexOf("clusterId_")>-1){
						fitem = getItemFromSolr(simItem.clusterId);
					}else {
						fitem = getItemFromSolr("clusterId_"+simItem.clusterId);
					}
				if(fitem == null){
					LOG.error(" fitem is null "+id +" "+title);
					continue;
				}
				if(filterSet.contains(simItem.clusterId)){
					continue;
				}
				fItemList.add(fitem);
				filterSet.add(simItem.clusterId);
			}
		}
		return fItemList;
	}

	private List<FrontNewsItem> loadingfItemListFromSolrForShare(String idStr){
		//首先将imcp_前缀都去掉，方便查ikv
		idStr=idStr.replace("imcp_", "");
		List<FrontNewsItem> fItemList = new ArrayList<FrontNewsItem>();
		if(idStr == null){
			LOG.warn("Loading qualityNews id is null ~");
			return fItemList;
		}
		
		Set<String> filterSet = new HashSet<String>();
		Gson gson  = new Gson();
		String[] idList = idStr.split(",");
		
		for(String id : idList){
			
			//直接用id查ikv
			itemf item = query.queryItemF(id, "c");
			if(item == null){
				item = query.queryItemF(id, "x");
			}
			if(item == null){
				LOG.error(" item is null "+id );
				continue;
			}
			String str =getItemSimId(item.getTitle(), item.getID(), null);
			//需要加入非空检验
			if(str == null){
				continue;
			}
			simid simItem = null;
			try{
				simItem = gson.fromJson(str, simid.class);
			} catch (Exception e){
				LOG.error(" ",e);
				continue;
			}
			
			
			FrontNewsItem fitem = new FrontNewsItem();
			if(simItem.clusterId != null){
				if(simItem.clusterId.indexOf("clusterId_")>-1){ 
					fitem = getItemFromSolr(simItem.clusterId);
				}else {
					fitem = getItemFromSolr("clusterId_"+simItem.clusterId);
				}
			
				if(fitem == null){
					LOG.error(" fitem is null "+id );
					continue;
				}
				if(filterSet.contains(simItem.clusterId)){
					continue;
				}
				fItemList.add(fitem);
				filterSet.add(simItem.clusterId);
			}
		}
		return fItemList;
	}
	/**
	 * 根据imcpid查询接口，返回标题
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	private FrontNewsItem queryItemByImcpId(String imcpid){
		String url = "http://api.iclient.ifeng.com/ipadtestdoc?aid="+imcpid;
		String content = DownloadPageUtil.downloadPageByRetry(url, "UTF-8", 1);
		Gson gson = new Gson();
		SimpleDateFormat formate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat formate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			if(content != null){
				editordocument doc = gson.fromJson(content, editordocument.class);
				FrontNewsItem fItem = new FrontNewsItem();
				
				String time = doc.body.editTime;
				String time1 = formate1.format(formate.parse(time));
				fItem.setDate(time1);
				fItem.setDocId(doc.body.documentId);
				fItem.setTitle(doc.body.title);
				return fItem;
			}
		} catch (Exception e){
			LOG.error("", e);
		}
		return null;
	}
	
	/**
	 * 从redis中获取计算好的数据
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	private String loadingDataFromRedis( String key){
		String content = null;
		try{
			Jedis jedis = new Jedis("10.32.21.62", 6379, 2000);
			jedis.select(11);
			content = jedis.get(key);
		} catch (Exception e){
			LOG.error(" ", e);
		}
		return content;
	}

	
	private String getItemSimId(String title,String id,String content){
		try {
			String test = SimDocClient.doSearch(id, title, null, null);
			return test;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("getItemSimId ERROR ", e);
		}
		return null;
	}
	
	

	private FrontNewsItem getItemFromSolr(String cmppid){
		
//		String solrUrl = configs.getString("solrUrl");
		String solrUrl = "http://10.32.28.119:8082/solr46/item/";
		
			String queryStr = "simID:"+cmppid;
			SolrQuery query = generaterSolrQuery(queryStr, 1);
			query.setQuery(queryStr);
			List<PreloadItem> itemList = PreloadItemFromSolrUtil.preloadItemFromSolr(query,solrUrl,false);
			if(itemList == null || itemList.isEmpty()){
				LOG.warn(cmppid + " can not found from solr ~");
				return null;
				
			}
			FrontNewsItem item = itemList.get(0).getFitem();
		return item;
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
		
		//过滤条件	(本地数据不加该过滤)
		if(queryStr.indexOf("loc=") < 0){
			query.addFilterQuery("other:(ifeng OR yidian -illegal )");
		}
		return query;
	}
	
//	/**
//	 * 从redis中获取计算好的imcp数据,并转换为cmppid
//	 * 
//	 * 注意：
//	 * @param 
//	 * 
//	 * @return 
//	 * 
//	 */
//	private List<String> getCmppidDataList(){
//		String idStr = loadingDataFromRedis();
//		List<String> cmppIdList = new ArrayList<String>();
//		if(idStr == null){
//			LOG.warn("Loading qualityNews id is null ~");
//			return cmppIdList;
//		}
//		
//		Gson gson  = new Gson();
//		String[] idList = idStr.split(",");
//		
//		List<editordocument> errorlist = new ArrayList<editordocument>();
//		List<editordocument> simerrorlist = new ArrayList<editordocument>();
//		List<editordocument> solrerrorlist = new ArrayList<editordocument>();
//		for(String id : idList){
//			String title = queryTitleByImcpId(id);
//			editordocument eitem = queryByImcpId(id);
//			if(eitem == null){
//				LOG.error(" title is null "+id);
//				continue;
//			}
//			//直接用id查ikv
//			itemf item = query.queryItemF(title, "c");
//			if(item == null){
//				LOG.error(" item is null "+id +" "+title);
//				errorlist.add(eitem);
//				continue;
//			}
//			String str =getItemSimId(title, item.getID(), null);
//			simid simItem = gson.fromJson(str, simid.class);
//			if(simItem.clusterId == null){
//				eitem.body.documentId=item.getID();
//				simerrorlist.add(eitem);
//			}else{
//				FrontNewsItem fitem = getItemFromSolr("clusterId_"+simItem.clusterId);
//				if(fitem == null){
//					
//					solrerrorlist.add(eitem);
//				}
//			}
//			
//			
//			
//			cmppIdList.add(item.getID());
//			
//		}
////		System.out.println("hit num :"+cmppIdList.size());
////		System.out.println("not hit num :"+errorlist.size());
////		System.out.println("simnot hit num :"+simerrorlist.size());
////		System.out.println("solr not hit num :"+solrerrorlist.size());
////		System.out.println(new Gson().toJson(errorlist));
////		System.out.println(new Gson().toJson(simerrorlist));
////		System.out.println(new Gson().toJson(solrerrorlist));
//		return cmppIdList;
//	}
	
	class simid{
		String clusterId;
		List<String> simIds;
	}
	
	/**
	 * 根据imcpid查询接口，返回标题
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	public String queryTitleByImcpId(String imcpid){
		String url = "http://api.iclient.ifeng.com/ipadtestdoc?aid="+imcpid;
		String content = DownloadPageUtil.downloadPageByRetry(url, "UTF-8", 1);
		Gson gson = new Gson();
		try{
			if(content != null){
				editordocument doc = gson.fromJson(content, editordocument.class);
				return doc.body.title;
			}
		} catch (Exception e){
			LOG.error("", e);
		}
		return null;
	}
	
	/**
	 * 根据imcpid查询接口，返回标题
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	public editordocument queryByImcpId(String imcpid){
		String url = "http://api.iclient.ifeng.com/ipadtestdoc?aid="+imcpid;
		String content = DownloadPageUtil.downloadPageByRetry(url, "UTF-8", 1);
		Gson gson = new Gson();
		try{
			if(content != null){
				editordocument doc = gson.fromJson(content, editordocument.class);
				return doc;
			}
		} catch (Exception e){
			LOG.error("", e);
		}
		return null;
	}
	
	public static void main(String[] args){
		HighqualityNewsPreloadJob job = new HighqualityNewsPreloadJob();
//		job.getCmppidDataList();
		job.preload();
//		job.preloadForSharedAndStore();
//		String str = job.queryTitleByImcpId("030280049194469");
//		System.out.println(str);
	}
	

}

class editordocument{
	document_meta meta;
	document_body body;
}

class document_meta{
	
}

class document_body{
	String documentId;
	String title;
	String editTime;
}

