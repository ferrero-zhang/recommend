/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.SolrUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.SortOrder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.newHotPredict.Preload.PreloadItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;

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
 *          1.0          2015年7月9日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class SearchItemsFromSolr {
	private static Log LOG = LogFactory.getLog(SearchItemsFromSolr.class);
	
	
	
	/**
	 * 根据channel关键词搜索other字段，主要用于获取地域数据
	 * 
	 * 注意：只获取最近的100条
	 * @param String channel 
	 * 
	 * @return List<String> item2app (FrontItem) Json
	 * 
	 */
	public static List<String> searchLocNewsFromSolr(String channel){
		List<String> itemList = new ArrayList<String>();
		if(channel == null){
			LOG.error("channel is null");
			return itemList;
		}
		channel = escapeQueryChars(channel);
		try {
			ItemSorlServerClient client = ItemSorlServerClient.getInstance();
			HttpSolrServer server = client.getServer();
			String queryStr = "other:("+channel+")";
			SolrQuery query = new SolrQuery();
			query.setQuery(queryStr);
			query.setRows(500);
			query.addSort("date",SolrQuery.ORDER.desc);
			
			QueryResponse qr = server.query(query);
			SolrDocumentList doclist = qr.getResults();
			Iterator<SolrDocument> it = doclist.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				String str = (String) doc.get("item2app");
				itemList.add(str);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			LOG.error("Search Error ", e);
			return itemList;
		} catch (Exception e1) {
			// TODO: handle exception
			LOG.error("Search Error ", e1);
			return itemList;
		}
		return itemList;
	}
	/**
	 * 根据doc id查询solr,返回item2app字段，若查询失败返回空
	 * 
	 * 
	 * @param String id
	 * 
	 * @return String
	 * 
	 */
	public static String searchItem2appJsonById(String id){
		String item2appjson = null;
		if(id == null){
			LOG.error("Search item2app error : id is null");
			return item2appjson;
		}
		try {
			ItemSorlServerClient client = ItemSorlServerClient.getInstance();
			HttpSolrServer server = client.getServer();
			String queryStr = "itemid:"+id;
			SolrQuery query = new SolrQuery();
			query.setQuery(queryStr);
			//加入数据源限制
			query.addFilterQuery("other:ifeng");
			QueryResponse qr = server.query(query);
			SolrDocumentList doclist = qr.getResults();
			Iterator<SolrDocument> it = doclist.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				item2appjson = (String) doc.get("item2app");
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			LOG.error("Search item2app error  ", e);
			return item2appjson;
		} catch (Exception e1) {
			// TODO: handle exception
			LOG.error("Search item2app error  ", e1);
			return item2appjson;
		}
		return item2appjson;
	}
	/**
	 * 根据item id查询solr判断此item是否是存活状态
	 * 
	 * 
	 * @param String id
	 * 
	 * @return boolean
	 * 
	 */
	public static boolean searchItemIsAvailable(String id){
		boolean isAvialable = false;
		if(id == null){
			LOG.error("id is null");
			return isAvialable;
		}

		try {
			ItemSorlServerClient client = ItemSorlServerClient.getInstance();
			HttpSolrServer server = client.getServer();
			String queryStr = "itemid:"+id;
			SolrQuery query = new SolrQuery();
			query.setQuery(queryStr);
			
			QueryResponse qr = server.query(query);
			SolrDocumentList doclist = qr.getResults();
			Iterator<SolrDocument> it = doclist.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				isAvialable = (Boolean) doc.get("available");
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			LOG.error("Search item available error  ", e);
			return isAvialable;
		} catch (Exception e1) {
			// TODO: handle exception
			LOG.error("Search item available error  ", e1);
			return isAvialable;
		}
		return isAvialable;
	}
	
	/**
	 * 根据channel关键词搜索topic1和topic2，返回所查到的item中item2app字段
	 * 
	 * 注意：只获取最近的100条
	 * @param String channel 
	 * 
	 * @return List<String> item2app (FrontItem) Json
	 * 
	 */
	public static List<String> searchFrontItemFromSolr(String channel){
		List<String> itemList = new ArrayList<String>();
		if(channel == null){
			LOG.error("channel is null");
			return itemList;
		}
		channel = escapeQueryChars(channel);
		try {
			ItemSorlServerClient client = ItemSorlServerClient.getInstance();
			HttpSolrServer server = client.getServer();
			String queryStr = "topic1:("+channel+") OR topic2:("+channel+") OR topic3:("+channel+") OR relatedfeatures:("+channel+")";
			SolrQuery query = new SolrQuery();
			query.setQuery(queryStr);
			query.setRows(500);
			query.addSort("date",SolrQuery.ORDER.desc);
			
			QueryResponse qr = server.query(query);
			SolrDocumentList doclist = qr.getResults();
			Iterator<SolrDocument> it = doclist.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				String str = (String) doc.get("item2app");
				itemList.add(str);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			LOG.error("Search Error ", e);
			return itemList;
		} catch (Exception e1) {
			// TODO: handle exception
			LOG.error("Search Error ", e1);
			return itemList;
		}
		return itemList;
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
}
