package com.ifeng.iRecommend.kedm.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ikvdb.client.IkvdbClient;
import ikvdb.client.IkvdbClientConfig;
import ikvdb.client.IkvdbClientFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.kedm.userlog.UserInfoFromLog;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate.FeatureWordStatistics;

public class ItemIKVOperation {
	private static final Log LOG = LogFactory.getLog("ItemIKVOperation");

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
			jsonSource = client_day_userlog.getValue(key);
			value = gson.fromJson(jsonSource, UserInfoFromLog.class);
		}catch(Exception e){
			LOG.error("IKV ERROR get "+key,e);
			return null;
		}
		return value;
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
		Gson gson = new Gson();
		try{
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
			client_day_userlog.set(uid, value);
		}catch(Exception e){
			LOG.error("IKV ERROR add userInfo "+uid,e);
			e.printStackTrace();
		}
	}
//	
//	/**
//	 * @description 兼容的从ikv获取用户模型信息，并组装成原redis中的存储结构
//	 * @param @param uid
//	 * @param @param limit_userBehaviourNum 取用户模型数据的行为数目下限
//	 * @param @return
//	 * @return Map<String,String>
//	 */
//	public static Map<String, String> getUserLogInfo(String uid,int limit_userBehaviourNum/*long limitTime*/) {
//		if (null == uid || uid.isEmpty() || limit_userBehaviourNum <= 0) {
//			return null;
//		}
//		
////		if(limitTime <= 0)
////			limitTime = (System.currentTimeMillis() - 7*24*3600*1000L)/1000;
//		int limitTime = (int) ((System.currentTimeMillis() - 180*24*3600*1000L)/1000);
//		
//			
//		Map<String, String> result = new HashMap<String, String>();
//		UserInfoFromLog item = getUserInfo(uid);
//		if (null == item) {
//			return result;
//		}
//		Map<String, String> userOpenDoc = item.getOpen_doc_id_time_map();
//		if (null == userOpenDoc) {
//			return result;
//		}
//		
//		//按时间排序，并取有效行为条数
//		ArrayList<Entry<String, String>> maplist = new ArrayList(userOpenDoc.entrySet());
//		Collections.sort(maplist, new Comparator<Entry<String, String>>() {
//			@Override
//			public int compare(Entry<String, String> arg0,
//					Entry<String, String> arg1) {
//				// TODO Auto-generated method stub
//				double result = 0;
//				try{
//					result = Long.parseLong(arg0.getValue().split("#")[0]) - Long.parseLong(arg1.getValue().split("#")[0]);
//				}catch(Exception e){
//					//解析异常
//					e.printStackTrace();
//				}
//				if(result > 0)
//					return -1;
//				else if(result == 0)
//					return 0;
//				else
//					return 1;
//			}
//
//		});
//		
//		
//		String userOpenDocListStr = "";
//		for (Entry<String, String> tempKV : maplist) {
//			String openTimeAndRef = tempKV.getValue();
//			if(null == openTimeAndRef)
//				continue;
//			//不带来源的数据是否保留
//			/*if(openTimeAndRef.split("#").length != 2)
//				continue;*/
//			if(Long.parseLong(openTimeAndRef.split("#")[0]) > limitTime){
//				userOpenDocListStr += tempKV.getKey() + "#" + openTimeAndRef + "$";
//				limit_userBehaviourNum--;
//			}
//			
//			if(limit_userBehaviourNum <= 0)
//				break;
//		}
//		
//		
////		//大明 old change likun 2015 11 19
////		for (String tempKey : userOpenDoc.keySet()) {
////			String openTimeAndRef = userOpenDoc.get(tempKey);
////			if(null == openTimeAndRef)
////				continue;
////			//不带来源的数据是否保留
////			/*if(openTimeAndRef.split("#").length != 2)
////				continue;*/
////			if(Long.parseLong(openTimeAndRef.split("#")[0]) > limitTime){
////				userOpenDocListStr += tempKey + "#" + openTimeAndRef + "$";
////			}
////		}
//				
//		// 去掉最后一个"$"分割符号
//		if (userOpenDocListStr.endsWith("$")) {
//			String userOpenDocListStrTemp = userOpenDocListStr.substring(0, userOpenDocListStr.length() - 1);
//			userOpenDocListStr = userOpenDocListStrTemp;
//		}
//		
//		result.put(FeatureWordStatistics.readDoc_type_name, userOpenDocListStr);
//		
//		return result;
//	}
	
	/**
	 * @description 根据用户id组合三个月的用户_时间id（如：12345_20150901）,根据生成的id list批量查询ikv获取用户模型信息
	 * @param @param key 用户id
	 * @param @param limit_day 取用户模型数据的有效天数
	 * @param @return
	 * @return Map<String,String>
	 */
	public static Map<String,String> getUserLogInfo(String key,int limit_day){
		if(key == null || limit_day <= 0)
			return null;
		
		if(limit_day >= 90)
			limit_day = 90;
			
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
			//批量从ikv获取userloginfo
			Map<String,String> opendocs = client_day_userlog.getAllValue(keys);
			if(opendocs == null || opendocs.isEmpty())
				return null;
			
			//排序，筛选出最近有效的30天数据；（临时处理）
			ArrayList<Entry<String, String>> maplist = new ArrayList(opendocs.entrySet());
			Collections.sort(maplist, new Comparator<Entry<String, String>>() {
				@Override
				public int compare(Entry<String, String> arg0,
						Entry<String, String> arg1) {
					// TODO Auto-generated method stub
					double result = 0;
					try{
						result = arg0.getKey().compareTo(arg1.getKey());
					}catch(Exception e){
						//解析异常
						e.printStackTrace();
					}
					if(result > 0)
						return -1;
					else if(result == 0)
						return 0;
					else
						return 1;
				}

			});
			
			
			String userOpenDocListStr = "";
			for(Entry<String, String> dayItems : maplist){
				try{
					jsonSource = dayItems.getValue();
					value = gson.fromJson(jsonSource, UserInfoFromLog.class);
					if(value == null)
						continue;
					
					Map<String, String> userOpenDoc = value.getOpen_doc_id_time_map();
					if (null == userOpenDoc) {
						continue;
					}
					for (String tempKey : userOpenDoc.keySet()) {
						String openTimeAndRef = userOpenDoc.get(tempKey);
						if(null == openTimeAndRef)
							continue;
						userOpenDocListStr += tempKey + "#" + openTimeAndRef + "$";
						
					}	
					
					limit_day--;
					if(limit_day <= 0)
						break;
					
				}catch(Exception e){
					LOG.error("ikv get uid_day error "+dayItems.getKey(),e);
					e.printStackTrace();
				}
			}
			// 去掉最后一个"$"分割符号
			if (userOpenDocListStr.endsWith("$")) {
				String userOpenDocListStrTemp = userOpenDocListStr.substring(0, userOpenDocListStr.length() - 1);
				userOpenDocListStr = userOpenDocListStrTemp;
			}
			
			result.put(FeatureWordStatistics.readDoc_type_name, userOpenDocListStr);
		}catch(Exception e){
			LOG.error("IKV ERROR get "+key,e);
			return null;
		}
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
	
//	public static void main(String[] args){
//		ItemIKVOperation itemO = new ItemIKVOperation();
//		String key = "A000005551F6A8";
//		long s = System.currentTimeMillis()/1000;
//		//s = s/1000;
//		System.out.println((s+"").compareTo("1444705501"));
//		itemO.ItemIKVInit();
//		//itemO.client_userlog.delete(key);
//		getUserLogInfo(key,s);
//		UserInfoFromLog item = itemO.getUserInfo(key);
//		System.out.println(item.getOpen_doc_id_time_map().size());
//		
//	}

}
