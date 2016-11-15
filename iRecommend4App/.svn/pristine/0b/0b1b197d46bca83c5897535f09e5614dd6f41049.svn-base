package com.ifeng.iRecommend.lidm.userLog;


import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class LogToSolr {
	private static final Log LOG = LogFactory.getLog("log_to_solr");
	DocumentBuilder builder;
	int batchSize = 1000;
	
	public void pushToSolr(HashMap<String, String> dataHashMap){
		long start_push = System.currentTimeMillis();

		LinkedList<putData> put_list = new LinkedList<putData>();
		String time = dataHashMap.get("log_date")+dataHashMap.get("log_time");
		
		String userid;
		String userid_value;
		
		Iterator<Entry<String, String>> iterator_rawdata = dataHashMap
				.entrySet().iterator();

		while (iterator_rawdata.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator_rawdata
					.next();
			
			userid = (String) entry.getKey();
			userid_value = (String) entry.getValue();
			if(userid.equals("log_date") || userid.equals("log_time")){
				continue;
			}
			putData data = new putData(userid,userid_value,time);

			put_list.add(data);
		}
		try{
			LogSolrOperation.solrUrl = new URL("http://10.90.1.72:8080/solr46/usermodel/update");
			LogSolrOperation.solrUrlEx = new URL("http://10.32.24.195:8081/solr/userlog/update");
			LogSolrOperation.userdocUrl = new URL("http://10.32.24.195:8081/solr/userdoc/update");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		CountDownLatch countDownLatch_put = new CountDownLatch(1);
		PutThreadManager put_tm = new PutThreadManager("put_thread_manager",
				put_list, countDownLatch_put);
		put_tm.start();
		
		try {
			countDownLatch_put.await();
		} catch (InterruptedException ex) {
			LOG.error("wait for get index tm error");
		}
		//LogSolrOperation.commit();

		long end_push = System.currentTimeMillis();
		LOG.info("duration of push data to solr:" + (end_push - start_push));
	}
	
	public class putData{
		private String userid;
		public String getUserid() {
			return userid;
		}
		public void setUserid(String userid) {
			this.userid = userid;
		}
		public String getUserid_value() {
			return userid_value;
		}
		public void setUserid_value(String userid_value) {
			this.userid_value = userid_value;
		}
		private String userid_value;
		private String time;
		public putData(String userid,String userid_value,String time){
			this.userid = userid;
			this.userid_value = userid_value;
			this.time = time;
		}
		public String getTime() {
			return time;
		}
		public void setTime(String time) {
			this.time = time;
		}
	}
	
	/**
	 * 数据写入线程的管理线程
	 * 创建多个写入线程，批量写入数据
	 * @author lidm
	 *
	 */
	class PutThreadManager extends Thread {
		private LinkedList<putData> put_list = new LinkedList<putData>();
		private int put_threads_count = 15;
		private CountDownLatch countDownLatch;

		public PutThreadManager(String thread_name,
				LinkedList<putData> put_list, CountDownLatch countDownLatch) {
			super(thread_name);
			this.put_list = put_list;
			this.countDownLatch = countDownLatch;
		}

		public void run() {
			//long start_put = System.currentTimeMillis();
			doWork();
			countDownLatch.countDown();
			//long end_put = System.currentTimeMillis();
			//LOG.info("duration of put put_list:" + (end_put - start_put));
		}

		public void doWork() {
			// 启动10个PutThread线程put put_list
			int s_index = 0;
			int e_index = 0;
			int put_list_size = put_list.size();
			int _put_threads_count = put_threads_count;
			LOG.info("size of put_list:" + put_list_size);
			if (_put_threads_count > put_list_size)
				_put_threads_count = put_list_size;
			CountDownLatch countDownLatch_put = new CountDownLatch(
					_put_threads_count);
			int p_unit = put_list_size / _put_threads_count;

			for (int i = 0; i < _put_threads_count; i++) {
				s_index = i * p_unit;
				if (i == _put_threads_count - 1)
					e_index = put_list_size;
				else
					e_index = s_index + p_unit;
				List<putData> sub_put_list = put_list.subList(s_index, e_index);
				PutThread putThread = new PutThread("put_thread" + i,
						 sub_put_list, countDownLatch_put);
				putThread.start();
			}
			try {
				countDownLatch_put.await();
			} catch (InterruptedException ex) {
				LOG.error(" put threads error", ex);
			}
		}
	}
	String getRealId(String value){
		String imcp_id = "";
		int b = value.indexOf("id=");
		if (b >=0 ) {
			int e = value.indexOf("$", b);
			if(e > b){
				imcp_id = value.substring(b+3,e);
			}else{
				imcp_id = value.substring(b+3);
			}
		}
		if(imcp_id.startsWith("imcp_"))
			imcp_id = imcp_id.substring(5);
		//处理分页
		if((b=imcp_id.indexOf("_")) > 0){
			imcp_id = imcp_id.substring(0, b);
		}
		return imcp_id;
	}
	String getSolrDate(String time){
		DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    	DateFormat f2 = new SimpleDateFormat("yyyyMMddHHmm");
    	Date date;
    	String tm = null;
		try {
			date = f2.parse(time);
			tm = f.format(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tm;
		
	}
	
	/**
	 * 数据写入线程
	 * 将put_list中的每个put写入solr中
	 * @author lidm
	 *
	 */
	class PutThread extends Thread {
		private List<putData> put_list;
		private CountDownLatch countDownLatch;

		public PutThread(String thread_name,
				List<putData> put_list, CountDownLatch countDownLatch) {
			super(thread_name);
			this.put_list = put_list;
			this.countDownLatch = countDownLatch;
		}

		public void run() {
			doWork();
			countDownLatch.countDown();
		}

		public void doWork() {
			int tempCount = 0;
			try{
				
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				builder = factory.newDocumentBuilder();
				org.w3c.dom.Document xmlDoc = null;
				Element root = null;
				//发送一份到userdoc
				org.w3c.dom.Document userDocXmlDoc = null;
				Element userDocRoot = null;
				//发送一份到mapreduce
				org.w3c.dom.Document mpXmlDoc = null;
				Element mpRoot = null;

				int iNO = 0;
				List<SolrPojo> pojos = new ArrayList<SolrPojo>();
				List<UserDoc> udocs = new ArrayList<UserDoc>();
				List<UserSlide> uslides = new ArrayList<UserSlide>();
				Set<String> udocids = new HashSet<String>();
				HashMap<String,String> date_value_map = new HashMap<String,String>();
				for(putData data : put_list){
					String value = data.getUserid_value();
					if(value.contains("openpush#") || value.contains("ts#") || value.contains("page#")){
						
						String[] datas = value.split("!");
						for(String s : datas){
							if(s.contains("openpush#") || s.contains("ts#") || s.contains("page#")){
								tempCount++;
								//System.out.println(s);
								String aid = getRealId(s);
								if(aid.length()!=8){
									continue;
								}
								if(!"".equals(aid) && !date_value_map.containsKey(aid+"###"+data.getTime())){
									date_value_map.put(aid+"###"+data.getTime(), s);
								}
							}
						}
					}
					if(tempCount>0){
						pojos = LogSolrOperation.getPojo(data.getUserid(), date_value_map);
						date_value_map.clear();
						//date_value_list = null;
						tempCount = 0;
					}
					
					try {
						
						if (iNO == 0) {
							xmlDoc = builder.newDocument();
							xmlDoc.setXmlStandalone(true);
							root = xmlDoc.createElement("add");
							
							mpXmlDoc = builder.newDocument();
							mpXmlDoc.setXmlStandalone(true);
							mpRoot = mpXmlDoc.createElement("add");
							
							userDocXmlDoc = builder.newDocument();
							userDocXmlDoc.setXmlStandalone(true);
							userDocRoot = userDocXmlDoc.createElement("add");
						}
						if(pojos!=null&&pojos.size()!=0){
							for(SolrPojo sp : pojos){
								
								UserDoc udoc = new UserDoc(sp.getAid(),sp.getUrl(),sp.getOrgtitle(),sp.getTitle(),sp.getCdate(),sp.getChannel(),sp.getFcwordset());
								if(sp.getType().equals("pic")){
									UserSlide uslide = new UserSlide(sp.getId(),sp.getUid(),sp.getUrl(),sp.getOrgtitle(),sp.getPdate(),sp.getChannel());
									uslides.add(uslide);
								}
								
								Element doc = generateXmlElement(xmlDoc,sp.getId(), sp.getUid(),
										 sp.getUrl(), sp.getPdate(),sp.getChannel(),sp.getOrgtitle(),sp.getTm());
								root.appendChild(doc);
								
								Element mpDoc = generateXmlElement3(mpXmlDoc,sp.getUid(),sp.getAid());
								mpRoot.appendChild(mpDoc);
								
								if(!udocids.contains(sp.getAid())){
									udocs.add(udoc);
									udocids.add(sp.getAid());
									Element docEx = generateXmlElement2(userDocXmlDoc,udoc);
									userDocRoot.appendChild(docEx);
								}
								iNO++;
								if (iNO == batchSize) // 每10000个才发送
								{
									LogSolrOperation.post(xmlDoc, root);
									LogSolrOperation.postMp(mpXmlDoc, mpRoot);
									LogRedisOperation.postJedis(udocs);
									LogRedisOperation.simpostJedis(udocs);
									LogSolrOperation.postEx(userDocXmlDoc, userDocRoot);
									System.out.println("batch num:"+iNO);
									iNO = 0;
									udocs.clear();
									uslides.clear();
									xmlDoc = builder.newDocument();
									xmlDoc.setXmlStandalone(true);
									root = xmlDoc.createElement("add");
									
									mpXmlDoc = builder.newDocument();
									mpXmlDoc.setXmlStandalone(true);
									mpRoot = mpXmlDoc.createElement("add");
									
									userDocXmlDoc = builder.newDocument();
									userDocXmlDoc.setXmlStandalone(true);
									userDocRoot = userDocXmlDoc.createElement("add");
								}
								
							}
							pojos.clear();
							//pojos = null;
						}
						
						
					} catch (Exception e2) {
						e2.printStackTrace();
						LOG.error("post xml error", e2);
					}
				}
				
				if (iNO > 0) {
					LogSolrOperation.post(xmlDoc, root);
					LogSolrOperation.postMp(mpXmlDoc, mpRoot);
					LogRedisOperation.postJedis(udocs);
					LogRedisOperation.simpostJedis(udocs);
					LogSolrOperation.postEx(userDocXmlDoc, userDocRoot);
					System.out.println("batch num:"+iNO);
				}
				//LogSolrOperation.commit();
				
			}catch(Exception e){
				e.printStackTrace();
				LOG.error("dowork error", e);
			}
			
			
		}
	}
	
	
	// search
			final String F_Id = "id";
			final String F_Uid = "uid";
			final String F_Title = "segtitle";
			final String F_Url = "url";
			final String F_Pdate = "date";
			final String F_Cdate = "pdate";
			final String F_Tm = "shortDate";
			final String F_Channel = "channel";
			final String F_Source = "source";
			final String F_Orgtitle = "orgtitle";
			final String F_Fcwordset = "fcwordset";
		Element generateXmlElement(org.w3c.dom.Document xmlDoc, String id,
				String uid, String url,String date,String channel,String orgtitle,String tm)
				throws Exception {
			Element doc = xmlDoc.createElement("doc");
			Text text = xmlDoc.createTextNode("");
			doc.appendChild(text);

			Element e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Id);
			text = xmlDoc.createTextNode(id);
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name",F_Uid);
			text = xmlDoc.createTextNode(uid);
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Orgtitle);
			text = xmlDoc.createTextNode(orgtitle);
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Url);
			text = xmlDoc.createTextNode(url);
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Pdate);
			text = xmlDoc.createTextNode(date);
			e.appendChild(text);
			doc.appendChild(e);
			
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Channel);
			text = xmlDoc.createTextNode(channel);
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Tm);
			text = xmlDoc.createTextNode(tm);
			e.appendChild(text);
			doc.appendChild(e);
			/*e = xmlDoc.createElement("field");
			e.setAttribute("name", F_PlayedTime);
			CDATASection sec =  xmlDoc.createCDATASection(played_time);
			e.appendChild(sec);
			doc.appendChild(e);*/
			return doc;
		}
		
		Element generateXmlElement2(org.w3c.dom.Document xmlDoc, UserDoc udoc)
				throws Exception {
			Element doc = xmlDoc.createElement("doc");
			Text text = xmlDoc.createTextNode("");
			doc.appendChild(text);

			Element e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Id);
			text = xmlDoc.createTextNode(udoc.getId());
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name",F_Title);
			text = xmlDoc.createTextNode(udoc.getSegtitle());
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Orgtitle);
			text = xmlDoc.createTextNode(udoc.getOrgtitle());
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Url);
			text = xmlDoc.createTextNode(udoc.getUrl());
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Cdate);
			text = xmlDoc.createTextNode(udoc.getPdate());
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Fcwordset);
			text = xmlDoc.createTextNode(udoc.getFcwordset());
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name", F_Channel);
			text = xmlDoc.createTextNode(udoc.getChannel());
			e.appendChild(text);
			doc.appendChild(e);
			
			/*e = xmlDoc.createElement("field");
			e.setAttribute("name", F_PlayedTime);
			CDATASection sec =  xmlDoc.createCDATASection(played_time);
			e.appendChild(sec);
			doc.appendChild(e);*/
			return doc;
		}
		
		Element generateXmlElement3(org.w3c.dom.Document xmlDoc,String uid,String docid)
				throws Exception {
			Element doc = xmlDoc.createElement("doc");
			Text text = xmlDoc.createTextNode("");
			doc.appendChild(text);
			
			Element e = xmlDoc.createElement("field");
			e.setAttribute("name",F_Uid);
			text = xmlDoc.createTextNode(uid);
			e.appendChild(text);
			doc.appendChild(e);
			
			e = xmlDoc.createElement("field");
			e.setAttribute("name","docids");
			e.setAttribute("update", "add");
			text = xmlDoc.createTextNode(docid);
			e.appendChild(text);
			doc.appendChild(e);
			return doc;
		}

}
