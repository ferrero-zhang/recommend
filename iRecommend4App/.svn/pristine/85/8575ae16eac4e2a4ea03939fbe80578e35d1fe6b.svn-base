package com.ifeng.iRecommend.kedm.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.ifeng.iRecommend.kedm.userlog.UserInfoFromLog;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate.FeatureWordStatistics;
import com.ifeng.ikvlite.LiteClientFactory;
import com.ifeng.ikvlite.LiteClientStore;

/**
 * <PRE>
 * 作用 : 新版IKV工具类，主要针对客户端用户日志解析后的结构化用户模型的存储，key为uid_yymmdd,value为用户模型序列化的json字符串
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
 *          1.0          2016-1-8        kedm          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class UserlogIKVOperation {
	private static final Log LOG = LogFactory.getLog("UserlogIKVOperation");
	private static LiteClientFactory factory = null;
	private static final String IKV_TABLE_NAME_DAYUSERLOG = "ir_user_daylog";//用于用户模型的userlog表
	
	public static void userlogIKVInit(){
		try{
			
			factory = new LiteClientFactory("10.32.25.50", 6886 , IKV_TABLE_NAME_DAYUSERLOG);

		}catch(Exception e){
			LOG.error("init ikv error",e);
			e.printStackTrace();
		}
	}
	
	public static LiteClientStore getIKVClient(){
		if(factory == null){
			userlogIKVInit();
		}
		try{
			return factory.getStore();
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * @description 在ikv中查看单个uid的Userinfo信息
	 * @param @param key——》userid
	 * @param @return
	 * @return UserInfoFromLog
	 */
	public static UserInfoFromLog getUserInfo(String key,LiteClientStore liteclient){
		if(key == null)
			return null;
		Gson gson = new Gson();
		UserInfoFromLog value = null;
		String jsonSource = "";
		try{
			jsonSource = liteclient.get(key);
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
	public static void addUserInfo(String uid,UserInfoFromLog userInfo,LiteClientStore liteclient){
		if(userInfo == null)
			return;
		Gson gson = new Gson();
		try{
			UserInfoFromLog old = getUserInfo(uid,liteclient);
			if(old != null){
				Map<String,String> old_readMap = old.getOpen_doc_id_time_map();
				if(old_readMap == null){
					old_readMap = new HashMap<String,String>();
				}
				if(userInfo.getOpen_doc_id_time_map() != null){
					for(String docid : userInfo.getOpen_doc_id_time_map().keySet()){
						String value = userInfo.getOpen_doc_id_time_map().get(docid);
						old_readMap.put(docid, value);
					}
				}
				userInfo.setOpen_doc_id_time_map(old_readMap);
				//更新keyword和search
				Map<String,String> old_keywordMap = old.getUser_keyword_time_map();
				if(old_keywordMap == null){
					old_keywordMap = new HashMap<String,String>();
				}
				if(userInfo.getUser_keyword_time_map() != null){
					for(String sw : userInfo.getUser_keyword_time_map().keySet()){
						String value = userInfo.getUser_keyword_time_map().get(sw);
						old_keywordMap.put(sw, value);
					}
				}
				userInfo.setUser_keyword_time_map(old_keywordMap);
				
				Map<String,String> old_searchMap = old.getUser_search_time_map();
				if(old_searchMap == null){
					old_searchMap = new HashMap<String,String>();
				}
				if(userInfo.getUser_search_time_map() != null){
					for(String sw : userInfo.getUser_search_time_map().keySet()){
						String value = userInfo.getUser_search_time_map().get(sw);
						old_searchMap.put(sw, value);
					}
				}
				userInfo.setUser_search_time_map(old_searchMap);
				
			}
			String value = gson.toJson(userInfo);
			liteclient.put(uid, value);
		}catch(Exception e){
			LOG.error("IKV ERROR add userInfo "+uid,e);
			e.printStackTrace();
		}
	}
	
	/**
	 * @param days_keysSet 
	 * @description 根据用户id组合三个月的用户_时间id（如：12345_20150901）,根据生成的id list批量查询ikv获取用户模型信息
	 * @param @param key 用户id
	 * @param @param limit_day 取用户模型数据的有效天数
	 * @param @return
	 * @return Map<String,String>
	 */
	public static Map<String,String> getUserLogInfo(String key,int limit_day,LiteClientStore liteclient, Set<String> days_keysSet){
		if(key == null || limit_day <= 0)
			return null;
		
		if(limit_day >= 90)
			limit_day = 90;
		Map<String, String> result = new HashMap<String, String>();
		Gson gson = new Gson();
		UserInfoFromLog value = null;
		String jsonSource = "";
		try{
			//遍历组合一个用户的所有日期段的keys，批量查询ikv
			Set<String> keysSet = new HashSet<String>();
			for(String day:days_keysSet){
				keysSet.add(key + "_" + day);
			}
			
			if(keysSet == null || keysSet.isEmpty())
				return null;
			
			String[] keys = keysSet.toArray(new String[keysSet.size()]);
			//批量从ikv获取userloginfo
			Map<String,String> opendocs = liteclient.gets(keys);
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
			
			StringBuffer userOpenDocListStr = new StringBuffer();
			StringBuffer userKeywordListStr = new StringBuffer();
			StringBuffer userSearchListStr = new StringBuffer();
			String user_mos = "";
			String user_ver = "";
			String user_mtype = "";
			int count = 0;
			for(Entry<String, String> dayItems : maplist){
				try{
					jsonSource = dayItems.getValue();
					if(jsonSource == null || jsonSource.equals("null"))
						continue;
					value = gson.fromJson(jsonSource, UserInfoFromLog.class);
					if(value == null)
						continue;
					
					Map<String, String> userOpenDoc = value.getOpen_doc_id_time_map();
					Map<String, String> userKeywords = value.getUser_keyword_time_map();
					Map<String, String> userSearchs = value.getUser_search_time_map();
					if (null == userOpenDoc && null == userKeywords && null == userSearchs) {
						continue;
					}
					if(userOpenDoc != null){
						for (String tempKey : userOpenDoc.keySet()) {
							String openTimeAndRef = userOpenDoc.get(tempKey);
							if(null == openTimeAndRef)
								continue;
							userOpenDocListStr.append(tempKey).append("#").append(openTimeAndRef).append("$");
							
						}
					}
					if(userKeywords != null){
						for (String tempKey : userKeywords.keySet()) {
							String opentime = userKeywords.get(tempKey);
							if(null == opentime)
								continue;
							userKeywordListStr.append(tempKey).append("#").append(opentime).append("$");
							
						}
					}
					if(userSearchs != null){
						for (String tempKey : userSearchs.keySet()) {
							String opentime = userSearchs.get(tempKey);
							if(null == opentime)
								continue;
							userSearchListStr.append(tempKey).append("#").append(opentime).append("$");
							
						}
					}
					if(count == 0){
						user_mos = value.getUser_os();
						user_ver = value.getUser_ver();
						user_mtype = value.getUser_mtype();
					}
					count++;
					limit_day--;
					if(limit_day <= 0)
						break;
					
				}catch(Exception e){
					LOG.error("ikv get uid_day error "+dayItems.getKey(),e);
					e.printStackTrace();
				}
			}
			
			String userOpenDocListStr_result = "";
			String userKeywordListStr_result = "";
			String userSearchListStr_result = "";
			if( userOpenDocListStr != null){
				userOpenDocListStr_result = userOpenDocListStr.toString();
			}
			if( userKeywordListStr != null){
				userKeywordListStr_result = userKeywordListStr.toString();
			}
			if( userSearchListStr != null){
				userSearchListStr_result = userSearchListStr.toString();
			}
			// 去掉最后一个"$"分割符号
			if (userOpenDocListStr_result.endsWith("$")) {
				userOpenDocListStr_result = userOpenDocListStr_result.substring(0, userOpenDocListStr_result.length() - 1);
			}
			if (userKeywordListStr_result.endsWith("$")) {
				userKeywordListStr_result = userKeywordListStr_result.substring(0, userKeywordListStr_result.length() - 1);
			}
			if (userSearchListStr_result.endsWith("$")) {
				userSearchListStr_result = userSearchListStr_result.substring(0, userSearchListStr_result.length() - 1);
			}
			
			result.put(FeatureWordStatistics.readDoc_type_name, userOpenDocListStr_result);
			result.put(FeatureWordStatistics.readKW_type_name, userKeywordListStr_result);
			result.put(FeatureWordStatistics.search_type_name, userSearchListStr_result);
			result.put("u_mos", user_mos);
			result.put("u_ver", user_ver);
			result.put("u_mt", user_mtype);
		}catch(Exception e){
			LOG.error("IKV ERROR get "+key,e);
			return null;
		}
		return result;
	}
	
	public static void delete(String key,LiteClientStore liteclient){
		if(key == null)
			return;
		try{
			liteclient.delete(key);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void deleteUserByDay(String uid,LiteClientStore liteclient){
		if(uid == null)
			return;
		try{
			liteclient.delete(uid);
			
		}catch(Exception e){
			LOG.error("delete outtime for uid "+uid,e);
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		userlogIKVInit();
		String uid = "A00000494A800E";
		LiteClientStore liteclient = getIKVClient();
		Set<String> ttt = new HashSet<String>();
		ttt.add("20160220");
		ttt.add("20160222");
		ttt.add("20160223");
		ttt.add("20160224");
		ttt.add("20160225");
		Map<String,String> temp = getUserLogInfo("1ed6477d109d3789abb09045ece9e3ebf2a9cd8a",30,liteclient,ttt);
		System.out.println(liteclient.get("1ed6477d109d3789abb09045ece9e3ebf2a9cd8a_20160225"));
	}

}
