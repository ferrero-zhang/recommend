/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.preloadUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import voldemort.store.readonly.io.jna.errno;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.SolrUtil.ItemSorlServerClient;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
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
 *          1.0          2015年12月4日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class PreloadItemFromSolrUtil {
	private static Log LOG = LogFactory.getLog(PreloadItemFromSolrUtil.class);
	
	/**
	 * 根据query 在solr中搜索，组装成PreloadItem返回
	 * 查询过程中会记录相似度
	 * 
	 * 
	 * 
	 * 注意：
	 * @param SolrQuery query
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	public static List<PreloadItem> preloadItemFromSolr(SolrQuery query, String solrUrl , boolean isRelatedFeaturesItem){
		List<PreloadItem> itemList = new ArrayList<PreloadItem>();
		if(query == null){
			LOG.error("channel is null");
			return itemList;
		}
		String queryStr = null;
		Gson gson = new Gson();
		try {
			ItemSorlServerClient client = ItemSorlServerClient.getInstance();
//			HttpSolrServer server = client.getServer();
			HttpSolrServer server = client.getServer(solrUrl);
			QueryResponse qr = server.query(query);
			SolrDocumentList doclist = qr.getResults();
			Iterator<SolrDocument> it = doclist.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				String str = (String) doc.get("item2app");
				Float score = (Float) doc.get("score");
				//获取simid
				String simId = (String) doc.get("simID");
				//获取来源
				String source = (String) doc.get("source");
				FrontNewsItem fitem = gson.fromJson(str, FrontNewsItem.class);
				//设置simid
				fitem.setSimId(simId);
				//fitem score 暂时设置成 simscore
				fitem.setScore(score);
				PreloadItem item = new PreloadItem(fitem,isRelatedFeaturesItem,score,source);
				itemList.add(item);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			LOG.error("Search Error ", e);
			LOG.info(queryStr);
			return itemList;
		} catch (Exception e1) {
			// TODO: handle exception
			LOG.error("Search Error ", e1);
			LOG.info(queryStr);
			return itemList;
		}
		return itemList;
	}
	
	public static List<PreloadItem> preloadItemFromSolrNew(SolrQuery query, String solrUrl , boolean isRelatedFeaturesItem, String channel){
		List<PreloadItem> itemList = new ArrayList<PreloadItem>();
		if(query == null){
			LOG.error("channel is null");
			return itemList;
		}
		String queryStr = null;
		Gson gson = new Gson();
		try {
			ItemSorlServerClient client = ItemSorlServerClient.getInstance();
//			HttpSolrServer server = client.getServer();
			HttpSolrServer server = client.getServer(solrUrl);
			QueryResponse qr = server.query(query);
			SolrDocumentList doclist = qr.getResults();
			Iterator<SolrDocument> it = doclist.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				String str = (String) doc.get("item2app");
				Float score = (Float) doc.get("score");
				//获取simid
				String simId = (String) doc.get("simID");
				//获取来源
				String source = (String) doc.get("source");
				FrontNewsItem fitem = gson.fromJson(str, FrontNewsItem.class);
				//设置simid
				fitem.setSimId(simId);
				//fitem score 暂时设置成 simscore
				fitem.setScore(score);
				//relatedfeatures小于0.7位不置信，舍去
				String relatedfeatures=(String) doc.get("relatedfeatures");
				try {
					if(relatedfeatures!=null&&!relatedfeatures.isEmpty()){
							if(relatedfeatures.indexOf(channel)>-1){
								int indexNum=relatedfeatures.indexOf(channel)+channel.length();
								Double isBelieve=Double.valueOf(relatedfeatures.substring(indexNum+1,indexNum+4));
								if(isBelieve<=0.7){
									continue;
								}  						
							}
					}
				} catch (Exception e) {
					LOG.error("deal relatedfeatures error",e);
					continue;
				}
				PreloadItem item = new PreloadItem(fitem,isRelatedFeaturesItem,score,source);
				itemList.add(item);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			LOG.error("Search Error ", e);
			LOG.info(queryStr);
			return itemList;
		} catch (Exception e1) {
			// TODO: handle exception
			LOG.error("Search Error ", e1);
			LOG.info(queryStr);
			return itemList;
		}
		return itemList;
	}
	/**
	 * 根据query 在solr中搜索，组装成PreloadItem返回
	 * 查询过程中会记录相似度
	 * 
	 * 
	 * 
	 * 注意：
	 * @param SolrQuery query
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	public static List<PreloadItem> preloadItemFromSolrWithSimId_test(SolrQuery query, String solrUrl , boolean isRelatedFeaturesItem){
		List<PreloadItem> itemList = new ArrayList<PreloadItem>();
		if(query == null){
			LOG.error("channel is null");
			return itemList;
		}
		String queryStr = null;
		Gson gson = new Gson();
		try {
			ItemSorlServerClient client = ItemSorlServerClient.getInstance();
//			HttpSolrServer server = client.getServer();
			HttpSolrServer server = client.getServer(solrUrl);
			QueryResponse qr = server.query(query);
			SolrDocumentList doclist = qr.getResults();
			Iterator<SolrDocument> it = doclist.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				String str = (String) doc.get("item2app");
				Float score = (Float) doc.get("score");
				String simId = (String) doc.get("simID");
				FrontNewsItem fitem = gson.fromJson(str, FrontNewsItem.class);
				fitem.setSimId(simId);
				PreloadItem item = new PreloadItem(fitem,isRelatedFeaturesItem,score);
				itemList.add(item);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			LOG.error("Search Error ", e);
			LOG.info(queryStr);
			return itemList;
		} catch (Exception e1) {
			// TODO: handle exception
			LOG.error("Search Error ", e1);
			LOG.info(queryStr);
			return itemList;
		}
		return itemList;
	}
	
	
}
