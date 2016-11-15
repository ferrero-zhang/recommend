package com.ifeng.iRecommend.kedm.userlog;

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

public class EXLogToIKV {
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
		int b = value.indexOf("id=");
		if (b >=0 ) {
			int e = value.indexOf("$", b);
			if(e > b){
				imcp_id = value.substring(b+3,e);
			}else{
				imcp_id = value.substring(b+3);
			}
		}
		if(imcp_id.startsWith("imcp_") || imcp_id.startsWith("cmpp_"))
			imcp_id = imcp_id.substring(5);
		//处理分页
		if((b=imcp_id.indexOf("_")) > 0){
			imcp_id = imcp_id.substring(0, b);
		}
		return imcp_id;
	}
	
	String getVidID(String value){
		String video_id = "";
		int b = value.indexOf("vid=");
		if (b >=0 ) {
			int e = value.indexOf("$", b);
			if(e > b){
				video_id = value.substring(b+4,e);
			}else{
				video_id = value.substring(b+4);
			}
		}
		if(video_id.startsWith("video_"))
			video_id = video_id.substring(6);

		return video_id;
	}
	
	
	String getDua(String value){
		String sec = "";
		int b = 0;
		int a = 4;
		
		if(value.contains("sec="))
		    b = value.indexOf("sec=");
		else {
			b = value.indexOf("pdur=");
			a = 5;
		}
		
		if (b >= 0 ) {
			int e = value.indexOf("$", b);
			if(e > b){
				sec = value.substring(b+a,e);
			}else{
				sec = value.substring(b+a);
			}
		}
		return sec;
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
	String getLoc(String value){
		String loc = "";
		int b = value.indexOf("loc=");
		if(b >= 0){
			loc = value.substring(b+4);
		}
		Gson gson = new Gson();
		if(loc.contains("%")){
			try {
				loc = URLDecoder.decode(loc, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			LocalInfo locinfo = gson.fromJson(loc, LocalInfo.class);
			if(locinfo != null)
				return locinfo.toString();
		}else if(loc.contains("{")){
			LocalInfo locinfo = gson.fromJson(loc, LocalInfo.class);
			if(locinfo != null)
				return locinfo.toString();
		}
		return loc;
	}
	String getZMT(String id){
		if(null == id || id.trim().equals(""))
			return null;
		String res = null;
		try{
			String temp = HttpRequest.sendGet("http://api.3g.ifeng.com/api_vampire_category_detail", "cid="+id);
			res = temp.substring(temp.indexOf("name\":\"")+7,temp.indexOf("\",\"type"));
		}catch(Exception e){
			return null;
		}
		
		return res;
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
			// 单个文件解析结果
			Map<String, UserInfoFromLog> result = new LinkedHashMap<String, UserInfoFromLog>();
			
			// uid-->page行为/openpush行为 (包括id与时间，多个id)
			// example: testuserid--->(docid-->readtime)
			Map<String, Map<String, String>> userPageLog = new LinkedHashMap<String, Map<String, String>>();
			
			Map<String, Map<String, String>> userStoreLog = new LinkedHashMap<String, Map<String, String>>();
			
			Map<String, Map<String, String>> userTsLog = new LinkedHashMap<String, Map<String, String>>();
			
			Map<String, Map<String, String>> userKeywordListLog = new LinkedHashMap<String, Map<String, String>>();
			
			Map<String, Map<String, String>> userSearchListLog = new LinkedHashMap<String, Map<String, String>>();
			
			Map<String, Map<String, String>> userReadSWLog = new LinkedHashMap<String, Map<String, String>>();

			// 用户add兴趣订阅行为
			// example: testuserid--->(关键词-->订阅时间)
			Map<String, Map<String, String>> userAddInterestLog = new LinkedHashMap<String, Map<String, String>>();
			// 用户del兴趣订阅行为
			Map<String, Map<String, String>> userDelInterestLog = new LinkedHashMap<String, Map<String, String>>();

			// 用户add book行为
			Map<String, Map<String, String>> userAddBookLog = new LinkedHashMap<String, Map<String, String>>();
			// 用户del book行为
			Map<String, Map<String, String>> userDelBookLog = new LinkedHashMap<String, Map<String, String>>();

			// 用户设置的地理位置
			Map<String, Map<String, String>> userSetLocLog = new LinkedHashMap<String, Map<String, String>>();
			
			// 用户浏览页面时间
			// uid-->(pid--->duration#查看时间)
			Map<String, Map<String, String>> userPageDuaLog = new LinkedHashMap<String, Map<String, String>>();
			
			// 用户在推荐频道和猜你喜欢中点击不感兴趣的操作
			Map<String, Map<String, String>> userDislikeLog = new LinkedHashMap<String, Map<String, String>>();
			
			// 用户观看视频时长#查看时间
			Map<String, Map<String, String>> userVidLog = new LinkedHashMap<String, Map<String, String>>();
			
			// 用户查看图片的集合
			Map<String, Map<String, String>> userPicLog = new LinkedHashMap<String, Map<String, String>>();
						
			// 有过操作行为的用户id集合
			Set<String> userIdSet = new HashSet<String>();
			
			Map<String,String> zmt_id = new LinkedHashMap<String,String>();
			//用户系统版本号手机型号等信息 ：手机系统号#客户端版本号#手机型号
			Map<String,String> userPlat = new LinkedHashMap<String,String>();
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
			try{
				for(putData data : put_list){
					String value = data.getUserid_value();
					String system_id = data.getUserid();
					long backup_time = df.parse(data.time).getTime()/1000;
					//解析用户操作记录，只需要打开、转发、page操作
					if(value.contains("openpush#") || value.contains("page#") || value.contains("action#") || 
							value.contains("in#") || value.contains("ts#") || value.contains("v#")){
						String[] datas = value.split("!");
						for(String s : datas){
							try{

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
								
								if(temp_value[2] != null && !temp_value[2].trim().equals("")){
									userPlat.put(system_id, temp_value[2]);
								}
								
								//用户ip loc
								/*String loc = Ip2locIndex.checkLoc(temp_value[1]);
								if(loc != null){
									if(userSetLocLog.containsKey(system_id)){
										Map<String,String> ipOprtime = userSetLocLog.get(system_id);
										ipOprtime.put(loc, readtime);
										userSetLocLog.put(system_id, ipOprtime);
									}else{
										Map<String, String> ipOprtime = new LinkedHashMap<String, String>();
										ipOprtime.put(loc, readtime);
										userSetLocLog.put(system_id, ipOprtime);
									}
								}*/
								String actionType = action[0];
								if(actionType.equals("page")){
									String openDocID_page = getRealId(action[1]);
									String ref = "";
									boolean hasReadDocID = true;
									// 无效的docid
									if (null == openDocID_page || openDocID_page.isEmpty()) {
										hasReadDocID = false;
									}
									if(openDocID_page.contains("http")){
										hasReadDocID = false;
									}
									if(action[1].contains("ref=back") || action[1].contains("type=piclive")){
										hasReadDocID = false;
									}
									
									if (hasReadDocID) {
										String operatetime = String.valueOf(longstrToDate(actionTime).getTime()/1000);
										
										if(action[1].contains("type=ch")){
											ref = "ch";
										}else if(action[1].contains("ref=")){
											ref = getRef(action[1]);
										}
										//记录通过订阅内容列表或者搜索内容列表打开的doc，通常带有sw=
										if(action[1].contains("sw=")){
											String readSw = action[1].substring(action[1].lastIndexOf("sw=")+3);
											
											if(readSw != null && !readSw.trim().equals("")){
												if(readSw.contains("$")){
													readSw = readSw.substring(0,readSw.indexOf("$"));
												}
												ref = "sw=" + readSw;
												if(userReadSWLog.containsKey(system_id)){
													Map<String,String> swOprtime = userReadSWLog.get(system_id);
													swOprtime.put(readSw, readtime);
													userReadSWLog.put(system_id, swOprtime);
												}else{
													Map<String, String> swOprtime = new LinkedHashMap<String, String>();
													swOprtime.put(readSw, readtime);
													userReadSWLog.put(system_id, swOprtime);
												}
											}
										}
										if (userPageLog.containsKey(system_id)) {
											userIdSet.add(system_id);
											
											Map<String, String> docidOperTime = userPageLog.get(system_id);
											// 注意：时间也可能无效，为#之类的，解析的时候注意下
											//String operatetime = String.valueOf(longstrToDate(actionTime).getTime()/1000);
											docidOperTime.put(openDocID_page, operatetime +"#"+ref);
											// 可能同id,不同时间。不必判断是否已经有了，覆盖就好
											//LOG.info("put ikv data "+operatetime+"#"+ref);
											userPageLog.put(system_id, docidOperTime);
										} else {
											userIdSet.add(system_id);
											
											Map<String, String> docidOperTime = new LinkedHashMap<String, String>();
											//String operatetime = String.valueOf(longstrToDate(actionTime).getTime()/1000);
											docidOperTime.put(openDocID_page, operatetime +"#"+ref);
											userPageLog.put(system_id, docidOperTime);
											//LOG.info("put ikv data "+operatetime+"#"+ref);
										}
										//添加用户查看图片的记录
										if(action[1].contains("type=pic") && !action[1].contains("type=piclive")){
											if(userPicLog.containsKey(system_id)){
												Map<String,String> picOprtime = userPicLog.get(system_id);
												picOprtime.put(openDocID_page, operatetime +"#"+ref);
												userPicLog.put(system_id, picOprtime);
											}else{
												userIdSet.add(system_id);
												Map<String, String> picOprtime = new LinkedHashMap<String, String>();
												picOprtime.put(openDocID_page, operatetime +"#"+ref);
												userPicLog.put(system_id, picOprtime);
											}											
										}
									}
									
									/*// 抽取下用户点开的关键词
									String openKeyword = LogParserFunc.getOpenKeyword(operatedetail_page);
									// 无效的docid
									if (null == openKeyword) {
										continue;
									}
									
									if (openKeyword.isEmpty()) {
										continue;
									}

									if (userKeywordListLog.containsKey(system_id)) {
										userIdSet.add(system_id);
										
										Map<String, String> docidOperTime = userKeywordListLog.get(system_id);
										String operatetime = String.valueOf(CommonFuncUtils.longstrToDate
												(splitTempLogLine[operatetime_pos]).getTime()/1000);
										
										docidOperTime.put(openKeyword, operatetime);
										// 可能同id,不同时间。不必判断是否已经有了，覆盖就好
										userKeywordListLog.put(system_id, docidOperTime);
									} else {
										userIdSet.add(system_id);
										String operatetime = String.valueOf(CommonFuncUtils.longstrToDate
												(splitTempLogLine[operatetime_pos]).getTime()/1000);
										
										Map<String, String> docidOperTime = new LinkedHashMap<String, String>();
										docidOperTime.put(openKeyword, operatetime);
										userKeywordListLog.put(system_id, docidOperTime);
									}*/
								}else if(actionType.equals("openpush")){
									String openDocID_openpush = getRealId(action[1]);
									String ref = "push";
									if (null == openDocID_openpush) {
										continue;
									}
									if (openDocID_openpush.isEmpty()) {
										continue;
									}
									if(openDocID_openpush.contains("http")){
										continue;
									}
									if(action[1].contains("ref=back")){
										continue;
									}

									// 和page一样，都认为是用户看过的文章
									if (userPageLog.containsKey(system_id)) {
										userIdSet.add(system_id);
										
										Map<String, String> docidOperTime = userPageLog.get(system_id);
										String operatetime = String.valueOf(longstrToDate(actionTime).getTime()/1000);
										
										docidOperTime.put(openDocID_openpush, operatetime+"#"+ref);
										userPageLog.put(system_id, docidOperTime);
									} else {
										userIdSet.add(system_id);
										
										Map<String, String> docidOperTime = new LinkedHashMap<String, String>();
										String operatetime = String.valueOf(longstrToDate(actionTime).getTime()/1000);
										
										docidOperTime.put(openDocID_openpush, operatetime+"#"+ref);
										userPageLog.put(system_id, docidOperTime);
									}
								}else if(actionType.equals("action")){
									String operatetime = String.valueOf(longstrToDate(actionTime).getTime()/1000);
									//添加action为 点击tag的行为
									if(action[1].contains("type=keywd")){
										String sw = action[1].substring(action[1].indexOf("sw=")+3);
										if(sw != null && !sw.trim().equals("")){
											if(userKeywordListLog.containsKey(system_id)){
												Map<String,String> kwOprtime = userKeywordListLog.get(system_id);
												kwOprtime.put(sw, operatetime);
												userKeywordListLog.put(system_id, kwOprtime);
											}else{
												userIdSet.add(system_id);
												Map<String, String> kwOprtime = new LinkedHashMap<String, String>();
												kwOprtime.put(sw, operatetime);
												userKeywordListLog.put(system_id, kwOprtime);
											}
										}
										
									}
									//添加action为主动搜索的行为
									if(action[1].contains("type=search")){
										String sw = action[1].substring(action[1].indexOf("sw=")+3);
										if(sw != null && !sw.trim().equals("")){
											if(userSearchListLog.containsKey(system_id)){
												Map<String,String> kwOprtime = userSearchListLog.get(system_id);
												kwOprtime.put(sw, operatetime);
												userSearchListLog.put(system_id, kwOprtime);
											}else{
												userIdSet.add(system_id);
												Map<String, String> kwOprtime = new LinkedHashMap<String, String>();
												kwOprtime.put(sw, operatetime);
												userSearchListLog.put(system_id, kwOprtime);
											}
										}
									}
									//添加action为文章搜藏的行为
									if(action[1].contains("type=store")){
										String store_docid = getRealId(action[1]);
										if(store_docid != null && !store_docid.trim().equals("")){
											if(userStoreLog.containsKey(system_id)){
												Map<String,String> kwOprtime = userStoreLog.get(system_id);
												kwOprtime.put(store_docid, operatetime);
												userStoreLog.put(system_id, kwOprtime);
											}else{
												userIdSet.add(system_id);
												Map<String, String> kwOprtime = new LinkedHashMap<String, String>();
												kwOprtime.put(store_docid, operatetime);
												userStoreLog.put(system_id, kwOprtime);
											}
										}
									}
									//添加action为订阅算法频道的行为
									if(action[1].contains("type=btnrecmd")){
										String btnr_sw = getRealId(action[1]);
										if(btnr_sw != null && !btnr_sw.trim().equals("")){
											if(userAddInterestLog.containsKey(system_id)){
												Map<String,String> kwOprtime = userAddInterestLog.get(system_id);
												kwOprtime.put(btnr_sw, operatetime);
												userAddInterestLog.put(system_id, kwOprtime);
											}else{
												userIdSet.add(system_id);
												Map<String, String> kwOprtime = new LinkedHashMap<String, String>();
												kwOprtime.put(btnr_sw, operatetime);
												userAddInterestLog.put(system_id, kwOprtime);
											}
										}
									}
									if(action[1].contains("type=btnsub")){
										String btns_id = getRealId(action[1]);
										String btns_sw = zmt_id.get(btns_id);
										if(btns_sw == null || btns_sw.trim().equals(""))
											btns_sw = getZMT(btns_id);
										if(btns_sw != null && !btns_sw.trim().equals("")){
											zmt_id.put(btns_id, btns_sw);
											if(userAddBookLog.containsKey(system_id)){
												Map<String,String> kwOprtime = userAddBookLog.get(system_id);
												kwOprtime.put(btns_sw, operatetime);
												userAddBookLog.put(system_id, kwOprtime);
											}else{
												userIdSet.add(system_id);
												Map<String, String> kwOprtime = new LinkedHashMap<String, String>();
												kwOprtime.put(btns_sw, operatetime);
												userAddBookLog.put(system_id, kwOprtime);
											}
										}
									}
									
									//取消action为订阅算法频道的行为
									if(action[1].contains("type=btnunrecmd")){
										String btnr_sw = getRealId(action[1]);
										if(btnr_sw != null && !btnr_sw.trim().equals("")){
											if(userDelInterestLog.containsKey(system_id)){
												Map<String,String> kwOprtime = userDelInterestLog.get(system_id);
												kwOprtime.put(btnr_sw, operatetime);
												userDelInterestLog.put(system_id, kwOprtime);
											}else{
												userIdSet.add(system_id);
												Map<String, String> kwOprtime = new LinkedHashMap<String, String>();
												kwOprtime.put(btnr_sw, operatetime);
												userDelInterestLog.put(system_id, kwOprtime);
											}
										}
									}
									//取消编辑频道的订阅
									if(action[1].contains("type=chunsub")){
										String btns_id = getRealId(action[1]);										
										if(btns_id != null && !btns_id.trim().equals("")){
											if(userDelBookLog.containsKey(system_id)){
												Map<String,String> kwOprtime = userDelBookLog.get(system_id);
												kwOprtime.put(btns_id, operatetime);
												userDelBookLog.put(system_id, kwOprtime);
											}else{
												userIdSet.add(system_id);
												Map<String, String> kwOprtime = new LinkedHashMap<String, String>();
												kwOprtime.put(btns_id, operatetime);
												userDelBookLog.put(system_id, kwOprtime);
											}
										}
									}
									//猜你喜欢中点击不感兴趣
									if(action[1].contains("type=dislike")
											|| action[1].contains("type=negative")){
										String doc_id = getRealId(action[1]);	
										String type = getType(action[1]);
										if(doc_id != null && !doc_id.trim().equals("")){
											if(userDislikeLog.containsKey(system_id)){
												Map<String,String> kwOprtime = userDislikeLog.get(system_id);
												kwOprtime.put(doc_id, operatetime + "#" + type);
												userDislikeLog.put(system_id, kwOprtime);
											}else{
												userIdSet.add(system_id);
												Map<String, String> kwOprtime = new LinkedHashMap<String, String>();
												kwOprtime.put(doc_id, operatetime + "#" + type);
												userDislikeLog.put(system_id, kwOprtime);
											}
										}
									}
								}else if(actionType.equals("in")){
									String loc = getLoc(action[1]);
									if(loc != null && !loc.trim().equals("")){
										if(userSetLocLog.containsKey(system_id)){
											Map<String,String> intime = userSetLocLog.get(system_id);
											intime.put(loc, readtime);
											userSetLocLog.put(system_id, intime);
										}else{
											Map<String,String> intime = new LinkedHashMap<String,String>();
											intime.put(loc, readtime);
											userSetLocLog.put(system_id, intime);
										}
									}
								}else if(actionType.equals("ts")){
									String ts_docid = getRealId(action[1]);
									if(ts_docid != null && !ts_docid.trim().equals("")){
										if(userTsLog.containsKey(system_id)){
											Map<String,String> kwOprtime = userTsLog.get(system_id);
											kwOprtime.put(ts_docid, readtime);
											userTsLog.put(system_id, kwOprtime);
										}else{
											userIdSet.add(system_id);
											Map<String, String> kwOprtime = new LinkedHashMap<String, String>();
											kwOprtime.put(ts_docid, readtime);
											userTsLog.put(system_id, kwOprtime);
										}
									}
									//记录用户浏览时长
								}else if(actionType.equals("duration")){
									String page_id = getRealId(action[1]);
									String dua = getDua(action[1]);
									if(page_id != null && !page_id.trim().equals("") &&
											dua != null && !dua.trim().equals("")){
										if(userPageDuaLog.containsKey(system_id)){
											Map<String,String> pageDua = userPageDuaLog.get(system_id);
											pageDua.put(page_id, dua + "#" + readtime);
											userPageDuaLog.put(system_id, pageDua);
										}else{
											userIdSet.add(system_id);
											Map<String, String> pageDua = new LinkedHashMap<String, String>();
											pageDua.put(page_id, dua + "#" + readtime);
											userPageDuaLog.put(system_id, pageDua);
										}
									}	
									//记录用户视频
								} else if (actionType.equals("v")){
									String vid_id = getVidID(action[1]);
									String dua = getDua(action[1]);
									if(vid_id != null && !vid_id.trim().equals("") &&
											dua != null && !dua.trim().equals("")){
										if(userVidLog.containsKey(system_id)){
											Map<String,String> vidDua = userVidLog.get(system_id);
											vidDua.put(vid_id, dua + "#" + readtime);
											userVidLog.put(system_id, vidDua);
										}else{
											userIdSet.add(system_id);
											Map<String, String> vidDua = new LinkedHashMap<String, String>();
											vidDua.put(vid_id, dua + "#" + readtime);
											userVidLog.put(system_id, vidDua);
										}
									}	
								} else if(actionType.equals("book")){
									// interest和book格式很简单，直接用即可,注意区分下add与del
									/*String operatedetail_book = splitTempLogLine[operatedetail_pos];
									String bookType_book = "";
									String bookValue_book = "";
									if (2 == operatedetail_book.split("=").length) {
										bookType_book = operatedetail_book.split("=")[0]; // add or del
										bookValue_book = operatedetail_book.split("=")[1];
									}

									if (bookType_book.equals("add")) {
										if (userAddBookLog.containsKey(system_id)) {
											userIdSet.add(system_id);
											
											Map<String, String> operTime = userAddBookLog.get(system_id);
											String operatetime = String.valueOf(CommonFuncUtils.longstrToDate
													(splitTempLogLine[operatetime_pos]).getTime()/1000);
											
											operTime.put(bookValue_book, operatetime);
											userAddBookLog.put(system_id, operTime);
										} else {
											userIdSet.add(system_id);
											
											Map<String, String> operTime = new LinkedHashMap<String, String>();
											String operatetime = String.valueOf(CommonFuncUtils.longstrToDate
													(splitTempLogLine[operatetime_pos]).getTime()/1000);
											
											operTime.put(bookValue_book, operatetime);
											userAddBookLog.put(system_id, operTime);
										}
									}

									if (bookType_book.equals("del")) {
										if (userDelBookLog.containsKey(system_id)) {
											userIdSet.add(system_id);
											
											Map<String, String> operTime = userDelBookLog.get(system_id);
											String operatetime = String.valueOf(CommonFuncUtils.longstrToDate
													(splitTempLogLine[operatetime_pos]).getTime()/1000);
											
											operTime.put(bookValue_book, operatetime);
											userDelBookLog.put(system_id, operTime);
										} else {
											userIdSet.add(system_id);
											
											Map<String, String> operTime = new LinkedHashMap<String, String>();
											String operatetime = String.valueOf(CommonFuncUtils.longstrToDate
													(splitTempLogLine[operatetime_pos]).getTime()/1000);
											
											operTime.put(bookValue_book, operatetime);
											userDelBookLog.put(system_id, operTime);
										}
									}*/
								}else if(actionType.equals("setloc")){
									// 设置地理位置也挺简单的，直接处理
									/*String operatedetail_seloc = splitTempLogLine[operatedetail_pos];
									String locValue = "";
									if (2 == operatedetail_seloc.split("=").length) {
										locValue = operatedetail_seloc.split("=")[1];
									}
									
									userIdSet.add(system_id);
									
									// 如果产品允许多个loc，后续再改就OK，暂时直接覆盖
									userSetLocLog.put(system_id, locValue);*/
								}
							
							}catch(Exception e){
								LOG.error("detail get userlog error "+s,e);
								e.printStackTrace();
							}
						}
					}
				}
				// 只要有一个不为空，就处理得到结构化信息
				if (!(userPageLog.isEmpty() && userKeywordListLog.isEmpty() && 
						userAddInterestLog.isEmpty() && userDelInterestLog.isEmpty() && 
						userAddBookLog.isEmpty() && userDelBookLog.isEmpty() && userSetLocLog.isEmpty() &&
						userPageDuaLog.isEmpty() && userDislikeLog.isEmpty() && userPicLog.isEmpty())) {
					result = LogParserResultProcess.getUserInfoMap(userIdSet, userPageLog, userReadSWLog, userStoreLog,userTsLog, userKeywordListLog, userSearchListLog, userAddInterestLog,
							userDelInterestLog, userAddBookLog, userDelBookLog, userSetLocLog, userPageDuaLog, userDislikeLog, userVidLog, userPicLog, userPlat);
					LOG.info(Thread.currentThread().getName()+ ",get " + userIdSet.size() + " user's action");
				} else {
					LOG.info(Thread.currentThread().getName() + ",get non action");
				}
				
				if(!userKeywordListLog.isEmpty()){
					TempUserInfoCache.add(userKeywordListLog, "keyword");
				}
				if(!userAddInterestLog.isEmpty()){
					TempUserInfoCache.add(userAddInterestLog, "book");
				}
				//将每次增量的userid发送到redis
				for(String uid : userIdSet){
					//UseridCache.put(uid, "u");
					if(LogSolrOperation.userids.containsKey(uid))
						continue;
					//LOG.info("uid is "+uid);
					LogSolrOperation.userids.put(uid, 1);
				}
				//如果存在处理userinfo结果，发送到IKV
				if(result != null && !result.isEmpty()){
					postToIKV(result);
				}
				
				//postUseridToRedis(userIdSet);
			}catch(Exception e){
				e.printStackTrace();
				LOG.error("dowork error", e);
			}
			
			
		}
	}
	public void postToIKV(Map<String, UserInfoFromLog> userInfos){
		if(userInfos == null || userInfos.isEmpty()){
			LOG.info("post to ikv userinfo is null");
			return;
		}
		Set<String> keysSet = new HashSet<String>();
		long end = System.currentTimeMillis();
		long start = end - 60*24*3600*1000L;
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		Calendar calS = Calendar.getInstance();
		Calendar calE = Calendar.getInstance();
		try{
			//遍历组合一个用户的所有日期段的keys，批量查询ikv
			calS.setTimeInMillis(start);
			calE.setTimeInMillis(end);
			while(calS.compareTo(calE) <= 0){
				String date = df.format(calS.getTime());
				if(date != null){
					keysSet.add(date);
				}
				calS.set(Calendar.DATE, calS.get(Calendar.DATE)+1);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		for(String uid : userInfos.keySet()){
			//LOG.info("put user "+uid +" content to ikv");
			long s = System.currentTimeMillis();
			//临时处理
			if(uid.equals("02:00:00:00:00:00"))
				continue;
			if(userInfos.get(uid).getOpen_doc_id_time_map() == null)
				continue;
			if(userInfos.get(uid).getOpen_doc_id_time_map().size() > 300){
				LOG.info("this user is crawler "+ uid);
				continue;
			}
			/*for(String key : keysSet){
				//ItemIKVOperation.deleteUserByDay(uid+"_"+key);
				UserlogIKVOperation.deleteUserByDay(uid+"_"+key);
			}*/
			//checkIKV(liteclient,uid,keysSet);
			UserCenterIKVOperation.addUserInfo(uid+"_"+parser_time,userInfos.get(uid));
			//ItemIKVOperation.addUserInfo(uid+"_"+parser_time,userInfos.get(uid));
			//LOG.info("put to ikv "+uid+"_"+userInfos.get(uid).getUser_ver());
			long spend = System.currentTimeMillis()-s;
			if(spend > 2000)
				LOG.info("post to ikv long time "+spend);
		}
		
		LOG.info(Thread.currentThread().getName() + " finish add to ikv and close liteclient");
	}
	public void postUseridToRedis(Set<String> userids){
		if(userids == null || userids.isEmpty()){
			LOG.info("userids is empty..");
			return;
		}
		RedisPoolForUserlog.addBatch(userids, parser_time, 13 ,24*3600);
	}

}
