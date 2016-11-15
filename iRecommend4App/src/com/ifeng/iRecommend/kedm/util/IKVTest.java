package com.ifeng.iRecommend.kedm.util;

import ikvdb.client.IkvdbClient;
import ikvdb.client.IkvdbClientConfig;
import ikvdb.client.IkvdbClientFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.kedm.userlog.UserInfoFromLog;
import com.ifeng.iRecommend.kedm.userlog.LogToIKV.putData;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate.FeatureWordStatistics;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate.queryItemFeatures;
import com.ifeng.ikvlite.IkvLiteClient;


public class IKVTest {
	private static final Log LOG = LogFactory.getLog("ItemIKVOperation");
	private static Set<String> keysSet = new HashSet<String>();

	private static IkvdbClient<String, String> client;//客户端item内容的ikvdb操作对象
	private static IkvdbClient<String, String> client_userlog;//用户中心模型的userlog内容的ikvdb操作对象
	private static IkvdbClient<String, String> client_day_userlog;//用户中心模型的userlog内容的ikvdb操作对象
	

	private static final String IKV_TABLE_NAME = "ir_items";//客户端item内容表
	private static final String IKV_TABLE_NAME_USERLOG = "ir_user_log";//用于用户模型的userlog表
	private static final String IKV_TABLE_NAME_DAYUSERLOG = "ir_user_daylog";//用于用户模型的userlog表

	public static void ItemIKVInit(){
		// 初始ikv;
		try{
			IkvdbClientConfig config = new IkvdbClientConfig();
			

			// 设置服务器地址（启动路径），可以设置多个，保证至少有一个能连接上
			String[] urls = new String[] { "tcp://10.32.25.30:6666",
					"tcp://10.32.25.36:6666", "tcp://10.32.25.40:6666",
					"tcp://10.32.25.50:6666", };
			config.setBootstrapUrls(Arrays.asList(urls));
			config.setConnectionTimeout(2000, TimeUnit.MILLISECONDS);
			config.setSocketTimeout(5000, TimeUnit.MILLISECONDS);

			IkvdbClientFactory factory = new IkvdbClientFactory(config);

			client = factory.getClient(IKV_TABLE_NAME);
			client_userlog = factory.getClient(IKV_TABLE_NAME_USERLOG);
			client_day_userlog = factory.getClient(IKV_TABLE_NAME_DAYUSERLOG);

			LOG.info(IKV_TABLE_NAME + " connect...");
		}catch(Exception e){
			LOG.error("init ikv error..."+e.getMessage());
		}
		
	}
	/*// item类型
	public enum ItemType {
		PCITEM, APPITEM, UNDEFINED
	};
	// Item的默认类型为UNDEFINED
	private ItemType itemType = ItemType.UNDEFINED;

	// 设置item的类型，并根据item类型自动设置合适的数据表，以利用接口读取数据表中的数据。
	// 在调用接口前设置，只设置一次即可
	public void setItemType(ItemType _ItemType) {
		itemType = _ItemType;
		setTableName(_ItemType);
	}

	// 手动设置数据表的名称，会自动更新item类型
	private void setTableName(ItemType itemType) {
		switch (itemType) {
		case PCITEM:
			tableName = fieldDicts.pcItemTableNameInHbase;
			break;
		case APPITEM:
			tableName = fieldDicts.appItemTableNameInHbase;
			break;
		default:
			break;
		}
		;
	}*/
	
	/**
	 * @description 获取ikv表中客户端item串，反序列化为Item对象
	 * @param @param key 内容的docid
	 * @param @return
	 * @return Item
	 */
	public static Item get(String key) {
		if (key == null)
			return null;
		Item value = null;
		String jsonSource = "";
		try {
			jsonSource = client.getValue(key);
			value = JsonUtils.fromJson(jsonSource, Item.class);
		} catch (Exception e) {
			LOG.error("ERROR get: " + key);
			LOG.error("ERROR get", e);
			return null;
		}
		return value;
	}
	
	/**
	 * @description 在ikv中查看单个uid的Userinfo信息
	 * @param @param key——》userid
	 * @param @return
	 * @return UserInfoFromLog
	 */
	public static UserInfoFromLog getUserInfo(String key){
		if(key == null)
			return null;
		Gson gson = new Gson();
		UserInfoFromLog value = null;
		String jsonSource = "";
		try{
			jsonSource = client_userlog.getValue(key);
			long s = System.currentTimeMillis();
			value = gson.fromJson(jsonSource, UserInfoFromLog.class);
			//System.out.println("json to object spend "+(System.currentTimeMillis()-s));
		}catch(Exception e){
			//LOG.error("IKV ERROR get "+key,e);
			return null;
		}
		return value;
	}
	public static Map<String,String> getUserInfoDAY(String key){
		if(key == null)
			return null;
		long end = System.currentTimeMillis();
		long start = end - 90*24*3600*1000L;
		Set<String> keysSet = new HashSet<String>();//用户的日期段keys
		Map<String,String> result = new HashMap<String,String>();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		Calendar calS = Calendar.getInstance();
		Calendar calE = Calendar.getInstance();
		
		Gson gson = new Gson();
		UserInfoFromLog value = null;
		String jsonSource = "";
		try{
			//遍历组合一个用户的所有日期段的keys，批量查询ikv
			calS.setTimeInMillis(start);
			calE.setTimeInMillis(end);
			while(calS.compareTo(calE) <= 0){
				String date = df.format(calS.getTime());
				if(date != null){
					keysSet.add(key + "_" + date);
				}
				calS.set(Calendar.DATE, calS.get(Calendar.DATE)+1);
			}
			if(keysSet == null || keysSet.isEmpty())
				return null;
			String[] keys = keysSet.toArray(new String[keysSet.size()]);
			Map<String,String> opendocs = client_day_userlog.getAllValue(keys);
			if(opendocs == null || opendocs.isEmpty())
				return null;
			for(String uid_day : opendocs.keySet()){
				try{
					jsonSource = opendocs.get(uid_day);
					value = gson.fromJson(jsonSource, UserInfoFromLog.class);
					if(value == null)
						continue;
					Map<String, String> userOpenDoc = value.getOpen_doc_id_time_map();
					if (null == userOpenDoc) {
						continue;
					}
					
					String userOpenDocListStr = "";
					for (String tempKey : userOpenDoc.keySet()) {
						String openTimeAndRef = userOpenDoc.get(tempKey);
						if(null == openTimeAndRef)
							continue;
						userOpenDocListStr += tempKey + "#" + openTimeAndRef + "$";
						
					}
							
					// 去掉最后一个"$"分割符号
					if (userOpenDocListStr.endsWith("$")) {
						String userOpenDocListStrTemp = userOpenDocListStr.substring(0, userOpenDocListStr.length() - 1);
						userOpenDocListStr = userOpenDocListStrTemp;
					}
					
					result.put("userReadDocList", userOpenDocListStr);
				}catch(Exception e){
					LOG.error("ikv get uid_day error "+uid_day,e);
					e.printStackTrace();
				}
				
			}
		}catch(Exception e){
			LOG.error("IKV ERROR get "+key,e);
			return null;
		}
		return result;
	}
	/**
	 * @description 将新用户模型post到ikv中，增量添加
	 * @param @param uid
	 * @param @param userInfo
	 * @return void
	 */
	public static void addUserInfo(String uid,UserInfoFromLog userInfo){
		if(userInfo == null)
			return;
		try{
			Gson gson = new Gson();
			UserInfoFromLog old = getUserInfo(uid);
			if(old != null && old.getOpen_doc_id_time_map() != null){
				Map<String,String> old_readMap = old.getOpen_doc_id_time_map();
				if(userInfo.getOpen_doc_id_time_map() != null){
					for(String docid : userInfo.getOpen_doc_id_time_map().keySet()){
						String value = userInfo.getOpen_doc_id_time_map().get(docid);
						old_readMap.put(docid, value);
					}
				}
				userInfo.setOpen_doc_id_time_map(old_readMap);
			}
			String value = gson.toJson(userInfo);
			client_userlog.set(uid, value);
		}catch(Exception e){
			LOG.error("IKV ERROR add userInfo "+uid,e);
			return;
		}
	}
	/**
	 * @description 兼容的从ikv获取用户模型信息，并组装成原redis中的存储结构
	 * @param @param uid
	 * @param @param limitTime取用户模型数据的时间下线，为时间戳（如：Date.getTime()/1000,1444705501）
	 * @param @return
	 * @return Map<String,String>
	 */
	public static Map<String, String> getUserLogInfo(String uid,long limitTime) {
		if (null == uid || uid.isEmpty()) {
			return null;
		}
		
		if(limitTime <= 0)
			limitTime = (System.currentTimeMillis() - 7*24*3600*1000L)/1000;
			
		Map<String, String> result = new HashMap<String, String>();
		UserInfoFromLog item = getUserInfo(uid);
		if (null == item) {
			return result;
		}
		Map<String, String> userOpenDoc = item.getOpen_doc_id_time_map();
		if (null == userOpenDoc) {
			return result;
		}
		
		String userOpenDocListStr = "";
		for (String tempKey : userOpenDoc.keySet()) {
			String openTimeAndRef = userOpenDoc.get(tempKey);
			if(null == openTimeAndRef)
				continue;
			//不带来源的数据是否保留
			/*if(openTimeAndRef.split("#").length != 2)
				continue;*/
			if(Long.parseLong(openTimeAndRef.split("#")[0]) > limitTime){
				userOpenDocListStr += tempKey + "#" + openTimeAndRef + "$";
			}
		}
				
		// 去掉最后一个"$"分割符号
		if (userOpenDocListStr.endsWith("$")) {
			String userOpenDocListStrTemp = userOpenDocListStr.substring(0, userOpenDocListStr.length() - 1);
			userOpenDocListStr = userOpenDocListStrTemp;
		}
		
		result.put(FeatureWordStatistics.readDoc_type_name, userOpenDocListStr);
		
		return result;
	}
	public static void delete(String key){
		if(key == null)
			return;
		try{
			client_userlog.delete(key);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static LinkedList<String> getUserids(String path){
		LinkedList<String> uids = new LinkedList<String>();
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				uids.add(line);
			}
		}catch(Exception e){
			LOG.error("read uids from file error");
			e.printStackTrace();
		}
		fileutil.CloseRead();
		return uids;
	}
	static class ikvThread implements Runnable{
		public void run() {
			List<String> uids = getUserids("E:/dayliyUser");
			for(String uid : uids){
				long s = System.currentTimeMillis();
				Map<String,String> temp = getUserInfoDAY(uid);
				//System.out.println(temp);
				if(temp == null)
					continue;
				System.out.println("temp size "+temp.size());
				System.out.println(Thread.currentThread().getName()+" get user info spend"+(System.currentTimeMillis()-s));
			}
		}
		
	}
	public static void updateIKV(String uid){
		Gson gson = new Gson();
		UserInfoFromLog item = getUserInfo(uid);
		if (null == item) {
			return ;
		}
		Map<String, String> userOpenDoc = item.getOpen_doc_id_time_map();
		if (null == userOpenDoc) {
			return ;
		}
		Map<String, Map<String, String>> temp = new HashMap<String,Map<String, String>>();
		try{
			for (String tempKey : userOpenDoc.keySet()) {
				String openTimeAndRef = userOpenDoc.get(tempKey);
				if(null == openTimeAndRef)
					continue;
				//不带来源的数据是否保留
				/*if(openTimeAndRef.split("#").length != 2)
					continue;*/
				long time = Long.parseLong(openTimeAndRef.split("#")[0])*1000;
				DateFormat df = new SimpleDateFormat("yyyyMMdd");
				Date date = new Date(time);
				String t = df.format(date);
				if(temp.containsKey(t)){
					temp.get(t).put(tempKey, openTimeAndRef);
				}else{
					Map<String,String> openM = new HashMap<String,String>();
					openM.put(tempKey, openTimeAndRef);
					temp.put(t, openM);
				}
				
			}
			for(String day : temp.keySet()){
				UserInfoFromLog uif = new UserInfoFromLog();
				uif.setOpen_doc_id_time_map(temp.get(day));
				String value = gson.toJson(uif);
				client_day_userlog.add(uid+"_"+day, value);
			}
		}catch(Exception e){
			LOG.error("update ikv error for uid "+uid,e);
			e.printStackTrace();
		}
		
	}
	
	public static void updateikv2(String uid, IkvLiteClient client){
		if(null == uid)
			return;
		List<String> keys = new ArrayList<String>();
		Set<String> days = keysSet;
		for(String d : days){
			keys.add(uid+"_"+d);
		}
		try{
			long start = System.currentTimeMillis();
			Map<String,String> udocs = client_day_userlog.getAllValue(keys.toArray(new String[keys.size()]));
			long sec = System.currentTimeMillis();
			LOG.info("get from old ikv spend "+(sec-start));
			for(String key : udocs.keySet()){
				client.put(key, udocs.get(key));
			}
			long thr = System.currentTimeMillis();
			LOG.info("put to new ikv spend "+(thr-sec));
		}catch(Exception e){
			LOG.error("update ikv for uid error "+uid,e);
			e.printStackTrace();
		}
		
		
	}
	public static void initday(){
		long end = System.currentTimeMillis();
		long start = end - 90*24*3600*1000L;
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
	} 
	
	/**
	 * 数据写入线程的管理线程
	 * 创建多个写入线程，批量写入数据
	 * @author lidm
	 *
	 */
	static class PutThreadManager extends Thread {
		private LinkedList<String> put_list = new LinkedList<String>();
		private int put_threads_count = 15;
		private CountDownLatch countDownLatch;

		public PutThreadManager(String thread_name,
				LinkedList<String> put_list, CountDownLatch countDownLatch) {
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
					List<String> sub_put_list = put_list.subList(s_index, e_index);
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
	/**
	 * 数据写入线程
	 * 将put_list中的每个put写入solr中
	 * @author lidm
	 *
	 */
	static class PutThread extends Thread {
		private List<String> put_list;
		private CountDownLatch countDownLatch;

		public PutThread(String thread_name,
				List<String> put_list, CountDownLatch countDownLatch) {
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
		public void doWork(){
			int count = 0;
			String host = "10.32.25.24";
	        String keyspace = "ikv";
	        String table  = "ir_user_daylog";

	        IkvLiteClient client =  new IkvLiteClient(host,keyspace,table);
			for(String uid : put_list){
				
				updateikv2(uid,client);
				count++;
				LOG.info(Thread.currentThread().getName()+ "finish update uid "+count);
			}
		}
	}
	public static void deleteUserByDay(String uid){
		if(uid == null)
			return;
		try{
			client_day_userlog.delete(uid);
			
		}catch(Exception e){
			LOG.error("delete outtime for uid "+uid,e);
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		/*if(args.length != 2){
			System.out.println("Usage <uidfile> <outputfile>");
			System.exit(1);
		}*/
		queryItemFeatures query = new queryItemFeatures();
		List<String> features = query.getXMLFeatures("106854168");
		ItemIKVInit();
		//UserlogIKVOperation.userlogIKVInit();
		/*String t = "A000004206BD05";
		List<String> keys = new ArrayList<String>();
		Set<String> days = keysSet;
		for(String d : days){
			keys.add(t+"_"+d);
		}
		Map<String,String> test = liteclient.gets(keys.toArray(new String[keys.size()]));
		updateikv2(t);*/
		/*String key = "9d62db556b71181a48dfd310185fe6935cf47a90";
		List<String> uids = getUserids("E:/dayliyUser");
		for(String uid : uids){
			long s = System.currentTimeMillis();
			UserInfoFromLog temp = getUserInfo(uid);
			//System.out.println(temp);
			System.out.println("get user info spend"+(System.currentTimeMillis()-s));
		}*/
		//itemO.getUserInfoDAY("710e24f7630a3f5e1d72ba4cfcb609d5a38d1fa0_20150924");
		/*List<String> uids = getUserids("/data/kedm/dayliyUser");
		int count =0;
		for(String uid : uids){
			updateIKV(uid);
			count++;
			LOG.info("finish update ikv count "+count+" uid"+uid);
		}*/
		//String test = "355210060029838";
		//Map<String,String> temp = getUserInfoDAY(test);
		//创建多线程执行操作
		LinkedList<String> put_list = getUserids("E:/dayliyUser_a");
		LinkedList<String> put_list2 = getUserids("E:/dayliyUser_ios");
		put_list.addAll(put_list2);
		CountDownLatch countDownLatch_put = new CountDownLatch(1);
		PutThreadManager put_tm = new PutThreadManager("put_thread_manager",
				put_list, countDownLatch_put);
		put_tm.start();
				
		try {
			countDownLatch_put.await();
		} catch (InterruptedException ex) {
			LOG.error("wait for get index tm error");
		}
	}


}
