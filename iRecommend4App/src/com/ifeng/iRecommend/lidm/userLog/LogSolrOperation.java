package com.ifeng.iRecommend.lidm.userLog;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Result;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.ifeng.iRecommend.dingjw.itemParser.Item;

public class LogSolrOperation {
	private static final Log LOG = LogFactory.getLog("log_to_solr");
	private static Configuration config;
	static HTablePool hTablePool = null;
	protected static URL solrUrl;
	protected static URL solrUrlEx;
	protected static URL userdocUrl;
	private static final String zookeeperQuorum = "10.32.21.115,10.32.21.125,10.32.21.130";
    static {
		config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum", zookeeperQuorum);
		config.setLong(HConstants.HBASE_REGIONSERVER_LEASE_PERIOD_KEY, 300000);
	}
	public static List<SolrPojo> getPojo(String key,HashMap<String,String> cloum){
    	DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    	DateFormat f2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	DateFormat f3 = new SimpleDateFormat("yyyyMMddHHmm");
    	List<SolrPojo> pojos = new ArrayList<SolrPojo>();
    	String category = "";
    	try{
    		HTableInterface table = getTable("zhineng_irecommend_wap_itemlist");
    		Iterator iter = cloum.entrySet().iterator();
        	while(iter.hasNext()){
        		Map.Entry entry = (Map.Entry)iter.next();
        		String id = (String)entry.getKey();
        		String s = (String)entry.getValue();
        		String[] realid = id.split("###");
        		String[] record = s.split("\t");
        		SolrPojo pojo = new SolrPojo();
				/*HashMap<String, String> temp = new HashMap<String, String>();
        		temp = queryByRowKey(table,realid[0]);*/
        		Item temp = ItemIKVOperation.get(realid[0]);
        		if(temp == null){
        			continue;
        		}
        		if(s.contains("type=pic")){
        			pojo.setType("pic");
        		}else{
        			pojo.setType("other");
        		}
        		category = AppChannelsParser.getCategory(temp, 1);
        		String content = (temp.getContent()==null)?"":temp.getContent();
        		pojo.setFcwordset(ExtractContent.extract(content));
        		Date cdate = f3.parse(realid[1]);
        		Date date = new Date();
        		if(temp.getDate()!=null && !temp.getDate().trim().equals("")){
        			cdate = f2.parse(temp.getDate());
        		}else{
        			cdate = date;
        		}
				String ctm = f.format(cdate);
				String tm = f.format(date);
				pojo.setPdate(tm);
				pojo.setCdate(ctm);
				pojo.setAid(realid[0]);
				pojo.setTitle((temp.getTitle()==null)?"":temp.getTitle());
				pojo.setOrgtitle(pojo.getTitle().replaceAll("_\\w+\\s", ""));
				pojo.setUrl((temp.getUrl()==null)?"":temp.getUrl());
				pojo.setOrgtitle(EscapeUtil.replaceInvaldateCharacter(pojo.getOrgtitle()));
				pojo.setUrl(EscapeUtil.replaceInvaldateCharacter(pojo.getUrl()));
				pojo.setTm(tm.substring(0,10 ));
				//pojo.setChannel((temp.getChannel()==null)?"":temp.getChannel());
				pojo.setChannel(category);
				//pojo.setSource((temp.get("source")==null)?"":temp.get("source"));
				pojo.setUid(key);
				pojo.setId(key+"#"+realid[0]);
				pojos.add(pojo);
        	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return pojos;
		
    }
	
	static void commit() {
		doGet(appendParam(solrUrl.toString(), "softCommit=true"));
	}
	static String appendParam(String url, String param) {
		return url + (url.indexOf('?') > 0 ? "&" : "?") + param;
	}
	static void doGet(String sUrl) {
		try {
			URL url=new URL(sUrl);
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			int iTryNO = 0;
			while (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
				try {
					if (iTryNO > 40) {
						LOG.error("Solr returned an error #ErrorCode=" + urlc.getResponseCode() + " ErrorMessage=" + urlc.getResponseMessage());
						break;
					}
					iTryNO++;
					LOG.info("try posting data to " + sUrl
							+ ":" + iTryNO );
					Thread.sleep(2000);// 歇会儿
					urlc = (HttpURLConnection) url.openConnection();
				} catch (Exception ex) {
					LOG.error("An error occured posting data to " + sUrl,ex);
				}
			}
		} catch (IOException e) {
			LOG.error("An error occured posting data to " + sUrl + ". Please check that Solr is running.",e);
		}
	}
	static void post(org.w3c.dom.Document xmlDoc,Element root)
	{
		xmlDoc.appendChild(root);
		String xml = Util.docToString(xmlDoc);
        postXml(xml,solrUrlEx);
	}
	static void postMp(org.w3c.dom.Document xmlDoc,Element root)
	{
		xmlDoc.appendChild(root);
		String xml = Util.docToString(xmlDoc);
		postXml(xml, solrUrl);
	}
	static void postEx(org.w3c.dom.Document xmlDoc,Element root)
	{
		xmlDoc.appendChild(root);
		String xml = Util.docToString(xmlDoc);
        postXml(xml,userdocUrl);
	}
	static boolean hasDoc(UserDoc udoc){
		boolean res = false;
		String tempurl = "http://10.32.24.195:8081/solr/userdoc/select?q=id:"+udoc.getId()+"&wt=xml&rows=0";
		String result = getIn(tempurl);
		return res;
		
	}
	public static String getIn(String url){
		String data="";
		InputStream in = null;
		URL dataUrl = null;
		try {
			dataUrl = new URL(url);
			// 建立连接
			HttpURLConnection con = (HttpURLConnection) dataUrl
					.openConnection();

			// 超时处理
			con.setConnectTimeout(1500000);
			con.setReadTimeout(1500000);

			con.setDoOutput(true);
			con.setDoInput(true);
			if(con.getInputStream()==null){
				System.out.println("con  null");
			}
			in = con.getInputStream();
			DataInputStream dis = new DataInputStream(in);
			byte d[] = new byte[dis.available()];
			dis.read(d);
			// 获取输出的内容
			data = new String(d, "utf-8");
			
			
			
			con.disconnect();
			// 关闭输入流
			in.close();
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	static void postXml(String xmlFile, URL url)
	{
		xmlFile = Util.replaceInvaldateCharacter(xmlFile);
		
		HttpURLConnection urlc = null;
		try {
			urlc = (HttpURLConnection) prepareURLCon(url);

			urlc.setChunkedStreamingMode(1024000);
			OutputStream out = urlc.getOutputStream();
			OutputStreamWriter outputStrm = new OutputStreamWriter(out, "UTF-8");
			outputStrm.write(xmlFile);
			outputStrm.flush();
			outputStrm.close();

			InputStream in = null;
			try {
				if (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
					if (urlc.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
							|| urlc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
						System.out.println("post failed:");
					}
					LOG.error("Solr returned an error :"
							+ " ErrorCode=" + urlc.getResponseCode());
				} 

				in = urlc.getInputStream();
				//pipe(in, System.out);
			} catch (IOException e) {
				LOG.error("IOException while reading response:"
						+ " Exception=" + e);
				e.printStackTrace();
			} catch (Exception e) {
				LOG.error("Exception while reading response: word="
						+ " Exception=" + e);
				e.printStackTrace();
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException x) {
				}
			}
		} catch (Exception e) {
			LOG.error("Post Data unknown Exception:"+ e);
		} finally {
			if (urlc != null) {
				urlc.disconnect();
			}
		}
	}
	static HttpURLConnection prepareURLCon(URL url) throws IOException {
		HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
		try {
			urlc.setRequestMethod("POST");
		} catch (ProtocolException e) {
			System.out.println("Shouldn't happen: HttpURLConnection doesn't support POST??" + e);
		}
		urlc.setDoOutput(true);
		urlc.setDoInput(true);
		urlc.setUseCaches(false);
		urlc.setAllowUserInteraction(false);
		urlc.setRequestProperty("Content-type", "text/xml");
		return urlc;
	}
	
	static HashMap<String, String> queryByRowKey(HTableInterface table,String rowKey){
    	HashMap<String, String> date_value_HashMap = new HashMap<String, String>();
    	if(table == null){
           return null;
    	}
    	try{
    		Get get = new Get(rowKey.getBytes());
    		Result r = table.get(get);
    		if (r.isEmpty()) {
    			//LOG.error("Can not Find Row:" + rowKey);
    		} else {
    			for (KeyValue keyValue : r.raw()) {
    				String col = new String(keyValue.getQualifier());
    				String value = new String(keyValue.getValue());
    				date_value_HashMap.put(col, value);
    			}
    		}
    		table.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return date_value_HashMap;
    	
    }
	static List<String> queryByUserid(HTableInterface table,
			String rowKey) {
		List<String> date_value_list = new ArrayList<String>();
		try {
			
			if (table == null) {
				LOG.error("Can not get table");
				return null;
			}
			Get get = new Get(rowKey.getBytes());
			Result r = table.get(get);
			if (r.isEmpty()) {
				LOG.error("Can not Find Row:" + rowKey);
			} else {
				for (KeyValue keyValue : r.raw()) {
					
					date_value_list.add(new String(keyValue.getValue()));
				}
			}
			//table.close();
		} catch (IOException ex) {
			LOG.error("IO Exception:" + ex);
		}
		return date_value_list;
	}
    static HTableInterface getTable(String tableName) throws Exception
	{
		if (hTablePool == null) {
			hTablePool = new HTablePool(config, 10);
		}
		//String tableName = "zhineng_irecommend_applog_table_time";
		HTableInterface table =  hTablePool.getTable(tableName.getBytes());
		if (table == null) {
			LOG.error("Can not get table:" + tableName);
			throw new Exception("Can not get table:" + tableName);
		}
		return table;
	}
    
    public static void main(String[] args){
    	try{
    		int ss = 15*24*3600;
    		System.out.println(ss);
    		ItemIKVOperation.ItemIKVInit();
    		Item temp = ItemIKVOperation.get("98587705");
    		String sss = EscapeUtil.replaceInvaldateCharacter(temp.getUrl());
    		HTableInterface table = getTable("zhineng_irecommend_wap_itemlist");
    		//HashMap<String, String> temp = new HashMap<String, String>();
    		//temp = queryByRowKey(table,"89976549");
    		String url = "http://10.32.24.195:8081/solr/userlog/select?q=uid:865524010376534";
    		String res = getIn(url);
    		System.out.println(res);
    		/*HTableInterface table = getTable("zhineng_irecommend_wap_itemlist");
        	String id = "89408194";
        	HashMap<String, String> temp = new HashMap<String, String>();
    		temp = queryByRowKey(table,id);
    		System.out.println(temp);*/
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	
    }
    
    

}
