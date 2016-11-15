package com.ifeng.iRecommend.zhangxc.userlog;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.ifeng.commen.Utils.HttpRequest;
import com.ifeng.iRecommend.kedm.ipPredict.Ip2locIndex;
import com.ifeng.iRecommend.kedm.util.ItemIKVOperation;
import com.ifeng.iRecommend.kedm.util.LocalInfo;
import com.ifeng.iRecommend.kedm.util.RedisPoolForUserlog;
import com.ifeng.iRecommend.kedm.util.UserCenterIKVOperation;
import com.ifeng.iRecommend.kedm.util.UseridCache;
import com.ifeng.iRecommend.kedm.util.UserlogIKVOperation;
import com.ifeng.ikvlite.IkvLiteClient;
import com.ifeng.ikvlite.LiteClientStore;
import com.ifeng.webapp.simArticle.client.SimDocClient;

public class LogToURL {
	private static final Log LOG = LogFactory.getLog("log_to_IKV");
	private static String parser_time = "";
	
	public void pushToIKV(HashMap<String, String> dataHashMap){
		long start_push = System.currentTimeMillis();

		LinkedList<putData> put_list = new LinkedList<putData>();
		String time = dataHashMap.get("log_date")+dataHashMap.get("log_time");//用户操作时间
		parser_time = dataHashMap.get("log_date");
		String userid;
		String userid_value;
		
		Iterator<Entry<String, String>> iterator_rawdata = dataHashMap
				.entrySet().iterator();
        //构建解析后的数据对象
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
		//创建多线程执行操作
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
	
	/**
	 * <PRE>
	 * 作用 : 用户操作记录数据结构
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
	 *          1.0          2015-7-23        kedm          update
	 * -----------------------------------------------------------------------------
	 * </PRE>
	 */
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
		private int put_threads_count = 10;
		private CountDownLatch countDownLatch;

		public PutThreadManager(String thread_name,
				LinkedList<putData> put_list, CountDownLatch countDownLatch) {
			super(thread_name);
			this.put_list = put_list;
			this.countDownLatch = countDownLatch;
		}

		public void run() {
			//long start_put = System.currentTimeMillis();
			try{
				doWork();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				countDownLatch.countDown();
				LOG.info("put thread manager count down");
			}
			
			
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
			try{
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
			}catch(Exception e){
				LOG.error("put thread manage dowork error",e);
				e.printStackTrace();
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
		String start = "";
		int b = value.indexOf("id=");
		if (b >=0 ) {
			int e = value.indexOf("$", b);
			if(e > b){
				imcp_id = value.substring(b+3,e);
			}else{
				imcp_id = value.substring(b+3);
			}
		}
		
		if(imcp_id.startsWith("imcp_") || imcp_id.startsWith("cmpp_")){
			start =  imcp_id.substring(0,5);
			imcp_id = imcp_id.substring(5);
		}
		//处理分页
		if((b=imcp_id.indexOf("_")) > 0){
			imcp_id = imcp_id.substring(0, b);
		}
		
		imcp_id = start + imcp_id;
		return imcp_id;
	}
	

	String getType(String value){
		String type = "";
		int b = value.indexOf("type=");
		if (b >= 0 ) {
			int e = value.indexOf("$", b);
			if(e > b){
				type = value.substring(b+5,e);
			}else{
				type = value.substring(b+5);
			}
		}
		return type;
	}
	
	String getRef(String value){
		String ref = "";
		int b = value.indexOf("ref=");
		if(b >= 0){
			int e = value.indexOf("$", b);
			if(e > b){
				ref = value.substring(b+4,e);
			}else{
				ref = value.substring(b+4);
			}
		}
		return ref;
	}


	/**
	 * @Title: longstrToDate
	 * @Description: 将长字符串格式"yyyy-MM-dd HH:mm:ss"转为时间格式
	 * @author liu_yi
	 * @param strDate
	 * @return
	 * @throws
	 */
	public Date longstrToDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd+HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);

		return strtodate;
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
			try{
				doWork();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				countDownLatch.countDown();
				LOG.info("put thread count down");
			}
		}

		public void doWork() {
			int tempCount = 0;
			// uid-->page行为/openpush行为 (包括id与时间，多个id)
			// example: testuserid--->(docid-->readtime)
			Map<String, Map<String, String>> userPageLog = new LinkedHashMap<String, Map<String, String>>();
			
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
			try{
				for(putData data : put_list){
					String value = data.getUserid_value();
					String system_id = data.getUserid();
					long backup_time = df.parse(data.time).getTime()/1000;
					//只需要page操作
					if(value.contains("page#")){
						String[] datas = value.split("!");
						for(String s : datas){
							try{
								if(!s.contains("page"))
									continue;
								
								String[] temp_value = s.split("\t");
								if(temp_value.length != 5)
									continue;
								String[] action = temp_value[4].split("#");
								String actionTime = temp_value[0];
								String readtime;
								
								if(actionTime == null || actionTime.trim().equals("iphone")){
									readtime = backup_time+"";
								}else{
									readtime = String.valueOf(longstrToDate(actionTime).getTime()/1000);
								}
	
								String actionType = action[0];
								if(actionType.equals("page")){
									String openDocID_page = getRealId(action[1]);
									if(openDocID_page.equals("noid"))
										continue;
									
									String ref = "";
									// 无效的docid
									if (null == openDocID_page || openDocID_page.isEmpty() || openDocID_page.contains("http") || action[1].contains("ref=back")) 
										continue;
									
									if(action[1].contains("type=ch") || action[1].contains("type=sub"))    //首页
										continue;
									
									if(!openDocID_page.matches("^(imcp_|cmpp_)[0-9]{3,}$"))
										continue;
									
									if (userPageLog.containsKey(system_id)) {						
										Map<String, String> docidOperTime = userPageLog.get(system_id);
										docidOperTime.put(openDocID_page, readtime+"#"+ref);
										userPageLog.put(system_id, docidOperTime);
									} else {										
										Map<String, String> docidOperTime = new LinkedHashMap<String, String>();
										docidOperTime.put(openDocID_page, readtime+"#"+ref);
										userPageLog.put(system_id, docidOperTime);
									}				
								}												
							}catch(Exception e){
								LOG.error("detail get userlog error "+s,e);
								e.printStackTrace();
							}
						}
					}
				}
				
				Map<String, Set<String>> uid_simid = new LinkedHashMap<String, Set<String>>();   //uid --> Set (simid)
				
				for(String userid: userPageLog.keySet()){
					Map<String, String> user_docid_map = userPageLog.get(userid);
					int err = 0;
				
					for(String docid: user_docid_map.keySet()){
						String ret = SimDocClient.doSearch(docid, null, null);
						String clusterId;
						try{
							JSONObject jsonObject = JSONObject.fromObject(ret);
						    clusterId = jsonObject.getString("clusterId");
							if(clusterId.equals("clusterId_null"))
								continue;       //空则不上传
						} catch (Exception e){
							err++;
							continue;
						}
						
						if (uid_simid.containsKey(userid)) {						
							Set<String> simids = uid_simid.get(userid);
							simids.add(clusterId);
							uid_simid.put(userid, simids);
						} else {										
							Set<String> simids = new HashSet<String>();
							simids.add(clusterId);
							uid_simid.put(userid, simids);
						}	
					}				
				}

				//上传
				for(String userid: uid_simid.keySet()){					
					if(uid_simid.get(userid) == null)
						continue;
					
					try{
						String simidset = "";
						for(String simid: uid_simid.get(userid)){
							if(simidset.trim().equals(""))
								simidset = simid;
							else
								simidset = simidset + "," + simid;
						}
						
						String param = "user_id=" + userid + "&docIds=" + simidset;
						//String url = "http://client-irecommend.ifeng.com/Recommend4User/AddBfData?" + param;
						//String url = "http://10.90.7.57:8080/Recommend4User/AddBfData?" + param;
						String ret = HttpRequest.sendGet("http://10.90.7.57:8080/ExploredRec/AddBfData", param);
						//String ret = HttpRequestHostUtil.getResponseText(url, "local.toutiao-irecommend.ifeng.com", "10.32.25.115");
						LOG.info("Status: " + ret.trim() + " update user: " + userid + "   Simid: " + uid_simid.get(userid));			
					} catch (Exception e) {
						LOG.error("Update user " + userid + " error.");
						continue;
					}
					
				}
				
			
			}catch(Exception e){
				e.printStackTrace();
				LOG.error("dowork error", e);
			}			
		}
	}


}
