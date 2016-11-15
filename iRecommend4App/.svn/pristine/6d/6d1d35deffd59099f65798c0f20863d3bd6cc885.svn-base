package com.ifeng.iRecommend.zhangxc.userlog.phonePrice;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

public class SolrUtil {
	private static final Log log = LogFactory.getLog(SolrUtil.class);
	private static CloudSolrClient cloudClient = null;
	private static HttpSolrServer httpSolrServer = null;
	private static HttpClient httpClient = HttpClientUtil.createClient(null);
	
	public static void cloudServerInit(){
		try{
			if (GlobalParams.ZOOKEEPER_HOST != null) {
			      LBHttpSolrClient lbHttpSolrServer = new LBHttpSolrClient(httpClient);
			      //cloudServer = new CloudSolrServer(GlobalParams.ZOOKEEPER_HOST.replace(" ", ","), lbHttpSolrServer);
			      cloudClient = new CloudSolrClient(GlobalParams.ZOOKEEPER_HOST.replace(" ", ","), lbHttpSolrServer);
			      cloudClient.setZkClientTimeout(Integer.parseInt(GlobalParams.ZOOKEEPER_CLIENT_TIMEOUT.trim()));
			      cloudClient.setZkConnectTimeout(Integer.parseInt(GlobalParams.ZOOKEEPER_CONNECT_TIMEOUT.trim()));

			      cloudClient.setDefaultCollection(GlobalParams.SOLR_COLLECTION);
			      cloudClient.setParallelUpdates(true);
			      String documentID = GlobalParams.SOLR_DOCUMENT_ID;
			      cloudClient.setIdField(documentID);
			    } else if (GlobalParams.POST_SOLR_SERVER_URI != null) {
			      httpSolrServer = new HttpSolrServer(GlobalParams.POST_SOLR_SERVER_URI, httpClient);
			    } else {
			    	throw new IOException("No solr server create.");
			    }
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static String updateUserInfo(String id, String key, String fieldValue) throws SolrServerException, IOException{
		HashMap<String, Object> oper = new HashMap<String, Object>();
	    oper.put("set", fieldValue);

	    SolrInputDocument doc = new SolrInputDocument();
	    doc.addField("uid", id);
	    doc.addField(key, oper);
	 
		NamedList<Object> rsp;
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.add(doc);
				
		if (cloudClient != null) {			
			rsp = cloudClient.request(updateRequest);
	    } else {
 	        rsp = httpSolrServer.request(updateRequest);
	    }
	
		NamedList err = (NamedList) rsp.get("error");
	    if (err != null) {
	      String reason = (String) err.get("msg");
	      return reason;
	    }
	    return "success";
	}
	
	
	public static SolrDocument getUserDocument(String key) throws SolrServerException, IOException{
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery("uid:"+key);
		
		SolrDocumentList qr = null;
		if (cloudClient != null) {			
			qr = cloudClient.query(solrQuery).getResults();
	    } else {
 	        qr = httpSolrServer.query(solrQuery).getResults();
	    }
		SolrDocument sd = qr.get(0);
		return sd;
	}
	
    public static void addField(SolrDocument sd, String key, String value){
    	sd.addField(key, value); 	
    }
    
    public static void setField(SolrDocument sd, String key, String value){
    	sd.setField(key, value); 	
    }
	
	
	public static String sendUserDocument(SolrDocument sd) throws SolrServerException, IOException{
		SolrInputDocument doc = new SolrInputDocument();
		Collection<String> col = sd.getFieldNames();
		NamedList<Object> rsp;
		UpdateRequest updateRequest = new UpdateRequest();
		
		
		for(String key: col){
			doc.setField(key, sd.get(key));
		}
		updateRequest.add(doc);
		
		
		if (cloudClient != null) {			
			rsp = cloudClient.request(updateRequest);
	    } else {
 	       rsp = httpSolrServer.request(updateRequest);
	    }
	
		NamedList err = (NamedList) rsp.get("error");
	    if (err != null) {
	      String reason = (String) err.get("msg");
	      return reason;
	    }
	    return "success";
	}
	
	public static void main(String[] args) {
        SolrUtil.cloudServerInit();
    	
		try {
			SolrUtil.updateUserInfo("866548020023553","ua", "A");
			
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
