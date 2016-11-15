/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.SolrUtil;


import java.net.MalformedURLException;










import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.print.attribute.standard.Severity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.gson.Gson;



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

public class ItemSorlServerClient {
	private static final Log LOG = LogFactory.getLog(ItemSorlServerClient.class);
	private static ItemSorlServerClient solrSever = null;
	
	private static HttpSolrServer server = null;
	public static String url = "http://10.32.28.119:8082/solr46/item/";
	
	public static String url8081 = "http://10.32.28.119:8081/solr46/item/";
	public static String url8082 = "http://10.32.28.119:8082/solr46/item/";
	private static HttpSolrServer server8081 = null;
	private static HttpSolrServer server8082 = null;
	
	public static ItemSorlServerClient getInstance(){
		if(solrSever == null){
			solrSever = new ItemSorlServerClient();
		}
		return solrSever;
	}
	
	public HttpSolrServer getServer(){
		if(server == null){
			try{
				server = new HttpSolrServer(url);
				server.setConnectionTimeout(1000);
				server.setSoTimeout(1000);
			}catch(Exception e){
				LOG.error("Creat HttpSolrServer error "+e);
			}
		}
		return server;
	}
	
//	public HttpSolrServer getServer(String url){
//		if(server == null){
//			try{
//				server = new HttpSolrServer(url);
//				server.setConnectionTimeout(1000);
//				server.setSoTimeout(1000);
//			}catch(Exception e){
//				LOG.error("Creat HttpSolrServer error "+e);
//			}
//		}
//		return server;
//	}
	
	public HttpSolrServer getServer(String url){
		HttpSolrServer tmpserver = null;
		if(url.equals(this.url8081)){
			if(server8081 == null){
				try{
					server8081 = new HttpSolrServer(url);
					server8081.setConnectionTimeout(1000);
					server8081.setSoTimeout(1000);
				}catch(Exception e){
					LOG.error("Creat HttpSolrServer error "+e);
				}
			}
			tmpserver = server8081;
		} else if(url.equals(this.url8082)){
			if(server8082 == null){
				try{
					server8082 = new HttpSolrServer(url);
					server8082.setConnectionTimeout(1000);
					server8082.setSoTimeout(1000);
				}catch(Exception e){
					LOG.error("Creat HttpSolrServer error "+e);
				}
			}
			tmpserver = server8082;
		} else {
			if(server == null){
				try{
					server = new HttpSolrServer(url);
					server.setConnectionTimeout(1000);
					server.setSoTimeout(1000);
				}catch(Exception e){
					LOG.error("Creat HttpSolrServer error "+e);
				}
			}
			tmpserver = server;
		}
		
		return tmpserver;
	}
	
	public static void main(String args[]){
		ItemSorlServerClient solrs = ItemSorlServerClient.getInstance();
		HttpSolrServer solrserver = solrs.getServer();
		String channel = "篮球";
		String queryStr = "topic1:"+channel+" OR topic2:"+channel;
		System.out.println(queryStr);
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		query.setQuery(queryStr);
		query.setRows(20);
		query.addSort("date",SolrQuery.ORDER.desc);
		List<String> appitem = new ArrayList<String>();
		try {
			QueryResponse qres = solrserver.query(query);
			SolrDocumentList docs = qres.getResults();
			System.out.println(docs.size());
			Iterator<SolrDocument> it = docs.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				String str = (String) doc.get("item2app");
//				appitem.add(str);
				System.out.println(doc.get("item2app"));
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
