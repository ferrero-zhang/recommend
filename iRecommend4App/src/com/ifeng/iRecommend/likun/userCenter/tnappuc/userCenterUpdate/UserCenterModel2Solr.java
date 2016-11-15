package com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

import com.google.gson.Gson;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userinfomodel.UserCenterModel;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.DocumentPost;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.GlobalParams;

public class UserCenterModel2Solr {
	private static final Log log = LogFactory.getLog(UserCenterModel2Solr.class);
	private static CloudSolrClient cloudClient = null;
	private static HttpSolrServer httpSolrServer = null;
	private static HttpClient httpClient = null;
	
	public static void cloudServerInit(){
		try{
			httpClient = HttpClientUtil.createClient(null);
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
	public static void close(){
		try{
			if(cloudClient != null)
				cloudClient.close();
			if(httpSolrServer != null)
				httpSolrServer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public static void ucmToSolr2(UserCenterModel ucm){
		String[] temp_t1 = ucm.getTop_topic1().split("\\$");
		String[] temp_t2 = ucm.getTop_topic2().split("\\$");
		String[] temp_t3 = ucm.getTop_topic3().split("\\$");
		solrdata doc = new solrdata(ucm.getUser_id(), temp_t1[0], temp_t2[0], temp_t3[0], 
				ucm.getUser_active(), ucm.getUser_loc(), ucm.getUser_mtype(), 
				ucm.getUser_os(), ucm.getUser_ver(), ucm.getSort_keywords(),ucm.getSort_search_words());
		try{
			Gson gson = new Gson();
			String data = gson.toJson(doc);
			data = data.replace("\"", "\\\"");
			URL url = new URL("http://10.90.4.11:8080/solr/userprofile/update/json");
			DocumentPost.postXml(data, url, "json");
		}catch(Exception e){
			log.error("post to solr error "+ucm.getUser_id(),e);
			e.printStackTrace();
		}
	}
	public static void ucmToSolr(UserCenterModel ucm){
		if(ucm == null)
			return;
		//log.info(Thread.currentThread().getName() + " UserCenterModel2Solr test test solr");
		UpdateRequest updateRequest = new UpdateRequest();
		SolrInputDocument sdoc = new SolrInputDocument();
		String[] temp_t1 = ucm.getTop_topic1().split("\\$");
		String[] temp_t2 = ucm.getTop_topic2().split("\\$");
		String[] temp_t3 = ucm.getTop_topic3().split("\\$");
		sdoc.setField("uid", ucm.getUser_id());
		sdoc.setField("t1", temp_t1[0].replace("^", ""));
		sdoc.setField("t2", temp_t2[0].replace("^", ""));
		sdoc.setField("t3", temp_t3[0].replace("^", ""));
		sdoc.setField("ua", ucm.getUser_active());
		sdoc.setField("loc", ucm.getUser_loc());
		sdoc.setField("umt", ucm.getUser_mtype());
		sdoc.setField("umos", ucm.getUser_os());
		sdoc.setField("uver", ucm.getUser_ver());
		sdoc.setField("uk", ucm.getSort_keywords());
		sdoc.setField("ui", ucm.getSort_interest_words());
		sdoc.setField("search", ucm.getSort_search_words());
		sdoc.setField("e", ucm.getE_top_words());
		sdoc.setField("s", ucm.getS1_top_words());
		//加入最近一次更新时间
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		long t = System.currentTimeMillis();
		String time = df.format(new Date(t));
		sdoc.setField("updatetime", time);	
		updateRequest.add(sdoc);
		try{
			String res = doDataPost(updateRequest);
			log.info(ucm.getUser_id() + " model to solr res " + res);
		}catch(Exception e){
			log.error("user model to solr error",e);
			e.printStackTrace();
		}
				
		/*try{
			HttpSolrServer server = new HttpSolrServer("http://10.90.4.11:8080/solr/userprofile");
			UpdateResponse response = server.add(sdoc);
		}catch(Exception e){
			log.error("post to solr error "+ucm.getUser_id(),e);
			e.printStackTrace();
		}*/
		
	}
	static class solrdata{
		private String uid;
		private String t1;
		private String t2;
		private String t3;
		private String ua;
		private String loc;
		private String umt;
		private String umos;
		private String uver;
		private String uk;
		private String search;
		public solrdata(String uid,String t1,String t2,String t3,String ua,String loc,
				String umt,String umos,String uver,String uk,String search){
			this.uid = uid;
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
			this.ua = ua;
			this.loc = loc;
			this.umt = umt;
			this.umos = umos;
			this.uver = uver;
			this.uk = uk;
			this.search = search;
			
		}
	}
	private static String replaceWord(String str){
		if(str == null)
			return null;
		String[] words = str.split("#");
		String res = "";
		if(str.split("\\$").length == 2){
			words = str.split("\\$")[1].split("#");
		}
		
		for(String word : words){
			word = word.substring(0,word.indexOf("_"));
			res = res + word + "#";
		}
		if (res.endsWith("#")) {
			String newTopValue = res.substring(0, res.length() - 1);
			return newTopValue;
		}
		return res;
	}
	private static String doDataPost(UpdateRequest updateRequest) throws IOException, SolrServerException {
		NamedList<Object> rsp;
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
	public static void main(String[] args){
		String test = "孩子_25_2.57#学生_19_2.28#习近平_17_1.93#情况_70_1.90#俄罗斯_10_1.73#papi酱_5_1.60#媒体_47_1.51#";
		System.out.println(replaceWord(test));
	}

}
