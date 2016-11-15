package com.ifeng.iRecommend.kedm.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.ifeng.iRecommend.kedm.userlog.UserInfoFromLog;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate.FeatureWordStatistics;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.HotWordUtil;
import com.ifeng.ikvlite.IkvLiteClient;

public class UserCenterIKVOperation {
	private static final Log LOG = LogFactory.getLog("UserCenterIKVOperation");
	public static final String IKV_TABLE_NAME_DAYUSERLOG = "ir_user_daylog";//用于用户模型的userlog表
	public static String host = "10.32.25.22";
    public static String keyspace = "ikv";
	public static IkvLiteClient client = null;
	/*public static IkvLiteClient getIKVClient(){
		IkvLiteClient client =  new IkvLiteClient(keyspace,IKV_TABLE_NAME_DAYUSERLOG,true);
		client.connect("10.32.25.21","10.32.25.22");
		return client;
	}*/
	public static void init(){
		try{
			client =  new IkvLiteClient(keyspace,IKV_TABLE_NAME_DAYUSERLOG,true);
			client.connect("10.32.25.21","10.32.25.22");
		}catch(Exception e){
			LOG.error("inti usercenter ikv error");
			e.printStackTrace();
		}
		
	}
	public static void close(){
		if(client != null)
			client.close();
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
			jsonSource = client.get(key);
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
				//更新搜藏store
				Map<String,String> old_storeMap = old.getStore_docid_time_map();
				if(old_storeMap == null){
					old_storeMap = new HashMap<String,String>();
				}
				if(userInfo.getStore_docid_time_map() != null){
					for(String docid : userInfo.getStore_docid_time_map().keySet()){
						String value = userInfo.getStore_docid_time_map().get(docid);
						old_storeMap.put(docid, value);
					}
				}
				userInfo.setStore_docid_time_map(old_storeMap);
				//更新转发ts
				Map<String,String> old_tsMap = old.getTs_docid_time_map();
				if(old_tsMap == null){
					old_tsMap = new HashMap<String,String>();
				}
				if(userInfo.getTs_docid_time_map() != null){
					for(String docid : userInfo.getTs_docid_time_map().keySet()){
						String value = userInfo.getTs_docid_time_map().get(docid);
						old_tsMap.put(docid, value);
					}
				}
				userInfo.setTs_docid_time_map(old_tsMap);
				//更新用户打开过的sw关键词
				Map<String,String> old_swMap = old.getRead_sw_time_map();
				if(old_swMap == null){
					old_swMap = new HashMap<String,String>();
				}
				if(userInfo.getRead_sw_time_map() != null){
					for(String sw : userInfo.getRead_sw_time_map().keySet()){
						String value = userInfo.getRead_sw_time_map().get(sw);
						old_swMap.put(sw, value);
					}
				}
				userInfo.setRead_sw_time_map(old_swMap);
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
				//更新user loc
				Map<String,String> old_locMap = old.getUser_loc();
				if(old_locMap == null){
					old_locMap = new HashMap<String,String>();
				}
				if(userInfo.getUser_loc() != null){
					for(String loc : userInfo.getUser_loc().keySet()){
						String value = userInfo.getUser_loc().get(loc);
						old_locMap.put(loc, value);
					}
				}
				userInfo.setUser_loc(old_locMap);
				//更新add book
				Map<String,String> old_addbook = old.getBook_words_add();
				if(old_addbook == null){
					old_addbook = new HashMap<String,String>();
				}
				if(userInfo.getBook_words_add() != null){
					for(String ub : userInfo.getBook_words_add().keySet()){
						String value = userInfo.getBook_words_add().get(ub);
						old_addbook.put(ub, value);
					}
				}
				userInfo.setBook_words_add(old_addbook);
				
				Map<String,String> old_addInterest = old.getInterest_words_add();
				if(old_addInterest == null){
					old_addInterest = new HashMap<String,String>();
				}
				if(userInfo.getInterest_words_add() != null){
					for(String ub : userInfo.getInterest_words_add().keySet()){
						String value = userInfo.getInterest_words_add().get(ub);
						old_addInterest.put(ub, value);
					}
				}
				userInfo.setInterest_words_add(old_addInterest);
				
				//更新删除订阅编辑频道
				Map<String,String> old_delbook = old.getBook_words_del();
				if(old_delbook == null){
					old_delbook = new HashMap<String,String>();
				}
				if(userInfo.getBook_words_del() != null){
					for(String ub : userInfo.getBook_words_del().keySet()){
						String value = userInfo.getBook_words_del().get(ub);
						old_delbook.put(ub, value);
					}
				}
				userInfo.setBook_words_del(old_delbook);
				
				//更新删除订阅算法频道
				Map<String,String> old_delInterest = old.getInterest_words_del();
				if(old_delInterest == null){
					old_delInterest = new HashMap<String,String>();
				}
				if(userInfo.getInterest_words_del() != null){
					for(String ub : userInfo.getInterest_words_del().keySet()){
						String value = userInfo.getInterest_words_del().get(ub);
						old_delInterest.put(ub, value);
					}
				}
				userInfo.setInterest_words_del(old_delInterest);
				
				//更新用户浏览页面时间
				Map<String,String> old_pageDuration = old.getPage_duration();
				if(old_pageDuration == null){
					old_pageDuration = new HashMap<String,String>();
				}
				if(userInfo.getPage_duration() != null){
					for(String ub : userInfo.getPage_duration().keySet()){
						String value = userInfo.getPage_duration().get(ub);
						old_pageDuration.put(ub, value);
					}
				}
				userInfo.setPage_duration(old_pageDuration);
				
				//更新用户dislike页面
				Map<String,String> old_dislikeDoc = old.getDislike_doc();
				if(old_dislikeDoc == null){
					old_dislikeDoc = new HashMap<String,String>();
				}
				if(userInfo.getDislike_doc() != null){
					for(String ub : userInfo.getDislike_doc().keySet()){
						String value = userInfo.getDislike_doc().get(ub);
						old_dislikeDoc.put(ub, value);
					}
				}
				userInfo.setDislike_doc(old_dislikeDoc);
				
				//更新用户点击视频页面
				Map<String,String> old_vidDuration = old.getVid_duration();
				if(old_vidDuration == null){
					old_vidDuration = new HashMap<String,String>();
				}
				if(userInfo.getVid_duration() != null){
					for(String ub : userInfo.getVid_duration().keySet()){
						String value = userInfo.getVid_duration().get(ub);
						old_vidDuration.put(ub, value);
					}
				}
				userInfo.setVid_duration(old_vidDuration);
				
				//更新用户点击图片时间
				Map<String,String> old_picID_map = old.getOpen_pic_id_time_map();
				if(old_picID_map == null){
					old_picID_map = new HashMap<String,String>();
				}
				if(userInfo.getOpen_pic_id_time_map() != null){
					for(String ub : userInfo.getOpen_pic_id_time_map().keySet()){
						String value = userInfo.getOpen_pic_id_time_map().get(ub);
						old_picID_map.put(ub, value);
					}
				}
				userInfo.setOpen_pic_id_time_map(old_picID_map);
			}
			String value = gson.toJson(userInfo);
			client.put(uid, value);
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
	public static Map<String,String> getUserLogInfo(String key,int limit_day, List<String> days_keysSet){
		if(key == null || limit_day <= 0)
			return null;
		
		if(limit_day >= 90)
			limit_day = 90;
		Map<String, String> result = new HashMap<String, String>();
		List<String> dayliyOpenstrs = new ArrayList<String>();
		List<String> dayliyOpenPicstrs = new ArrayList<String>();
		Gson gson = new Gson();
		UserInfoFromLog value = null;
		String jsonSource = "";
		try{
			//遍历组合一个用户的所有日期段的keys，批量查询ikv
			List<String> keysSet = new ArrayList<String>();
			for(String day:days_keysSet){
				keysSet.add(key + "_" + day);
			}
			
			if(keysSet == null || keysSet.isEmpty())
				return null;
			
			//String[] keys = keysSet.toArray(new String[keysSet.size()]);
			//批量从ikv获取userloginfo
			//Map<String,String> opendocs = client.gets(keys);
			Map<String,String> opendocs = new HashMap<String,String>();
			//尝试修改ikv取多个key的方案，gets方法太慢，遍历get单个key试试
			int countD = 0;
			for(String k : keysSet){
				if(countD == 30)
					break;
				String tempvalue = client.get(k);
				if(tempvalue != null && !tempvalue.equals("null") && !tempvalue.equals("")){
					opendocs.put(k, tempvalue);
					countD++;
				}
			}
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
			StringBuffer userOpenPicListStr = new StringBuffer();
			StringBuffer userKeywordListStr = new StringBuffer();
			StringBuffer userSearchListStr = new StringBuffer();
			StringBuffer userReadSWListStr = new StringBuffer();
			StringBuffer userReadCHListStr = new StringBuffer();
			StringBuffer userInterestListStr = new StringBuffer();
			StringBuffer userBookListStr = new StringBuffer();
			StringBuffer userDurationStr = new StringBuffer();
			StringBuffer userVideoStr = new StringBuffer();
			StringBuffer userDislikeStr = new StringBuffer();
			String user_mos = "";
			String user_ver = "";
			String user_mtype = "";
			String user_loc = "";
			String loc_now = "";
			boolean isnow = true;
			Map<String,Integer> user_loc_temp = new HashMap<String,Integer>();
			int count = 0;
			String last2day_userRead = "";
			for(Entry<String, String> dayItems : maplist){
				userOpenDocListStr = new StringBuffer();
				try{
					jsonSource = dayItems.getValue();
					if(jsonSource == null || jsonSource.equals("null"))
						continue;
					value = gson.fromJson(jsonSource, UserInfoFromLog.class);
					if(value == null)
						continue;
					
					Map<String, String> userOpenDoc = value.getOpen_doc_id_time_map();
					Map<String, String> userOpenPic = value.getOpen_pic_id_time_map();
					Map<String, String> userKeywords = value.getUser_keyword_time_map();
					Map<String, String> userSearchs = value.getUser_search_time_map();
					Map<String, String> userReadSW = value.getRead_sw_time_map();
					Map<String, String> userLocs = value.getUser_loc();
					Map<String, String> interest_words_add = value.getInterest_words_add();
					Map<String, String> book_words_add = value.getBook_words_add();
					Map<String, String> userDuration = value.getPage_duration();
					Map<String, String> userVideo = value.getVid_duration();
					Map<String, String> userDislike = value.getDislike_doc();
					if (null == userOpenDoc && null == userKeywords && null == userSearchs && null == userReadSW&& null == interest_words_add&& null == book_words_add) {
						continue;
					}
					if(userOpenDoc != null){
						for (String tempKey : userOpenDoc.keySet()) {
							String openTimeAndRef = userOpenDoc.get(tempKey);
							if(null == openTimeAndRef)
								continue;
							userOpenDocListStr.append(tempKey).append("#").append(openTimeAndRef).append("$");
							if(tempKey.equals("sy") || tempKey.equals("rcmd") || tempKey.equals("noid")
									||openTimeAndRef.contains("#sy") || openTimeAndRef.contains("#rcmd") 
									|| openTimeAndRef.contains("#imcp") || openTimeAndRef.contains("noid")
									|| openTimeAndRef.contains("srhkey")|| openTimeAndRef.contains("push")){
								continue;
							}
							String[] time_ref = openTimeAndRef.split("#");
							if(time_ref.length != 2){
								continue;
							}
							if(openTimeAndRef.contains("sw=")){
								//userReadSWListStr.append(time_ref[1].replace("sw=", "")).append("#").append(time_ref[0]).append("$");
							}else if(time_ref[1].equals("ch")){
								userReadCHListStr.append(tempKey).append("#").append(time_ref[0]).append("$");
							}else{
								userReadCHListStr.append(time_ref[1]).append("#").append(time_ref[0]).append("$");
							}
						}
						dayliyOpenstrs.add(userOpenDocListStr.toString());
					}
					if(userOpenPic != null){
						for (String tempKey : userOpenPic.keySet()) {
							String openTimeAndRef = userOpenPic.get(tempKey);
							if(null == openTimeAndRef)
								continue;
							userOpenPicListStr.append(tempKey).append("#").append(openTimeAndRef).append("$");
							
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
					if(userReadSW != null){
						for (String tempKey : userReadSW.keySet()){
							String opentime = userReadSW.get(tempKey);
							if(null == opentime)
								continue;
							if(tempKey.contains("$")){
								tempKey = tempKey.substring(0,tempKey.indexOf("$"));
							}
							userReadSWListStr.append(tempKey).append("#").append(opentime).append("$");
						}
					}
					if(userLocs != null && userLocs.size() <= 2 && count < 2){
						for(String tempKey : userLocs.keySet()){
							if(tempKey.contains("%"))
								continue;
							if(user_loc_temp.containsKey(tempKey)){
								int tempC = user_loc_temp.get(tempKey);
								tempC = tempC + 1;
								user_loc_temp.put(tempKey, tempC);
							}else{
								user_loc_temp.put(tempKey, 1);
							}
							if(isnow){
								loc_now = tempKey;
								isnow = false;
							}
						}
					}
					if(interest_words_add != null){
						for(String tempKey: interest_words_add.keySet()){
							String oprationtime = interest_words_add.get(tempKey);
							if(null == oprationtime)
								continue;
							userInterestListStr.append(tempKey).append("#").append(oprationtime).append("$");
						}
					}
					if(book_words_add != null){
						for(String tempKey: book_words_add.keySet()){
							String oprationtime = book_words_add.get(tempKey);
							if(null == oprationtime)
								continue;
							userBookListStr.append(tempKey).append("#").append(oprationtime).append("$");
						}
					}
					if(userDuration != null){
						for(String tempKey: userDuration.keySet()){
							String oprationtimeref = userDuration.get(tempKey);
							if(null == oprationtimeref)
								continue;
							String[] time_ref = oprationtimeref.split("#");
							if(time_ref.length != 2)
								continue;
							userDurationStr.append(tempKey).append("#").append(oprationtimeref).append("$");
						}
					}
					if(userVideo != null){
						for(String tempKey: userVideo.keySet()){
							String oprationtimeref = userVideo.get(tempKey);
							if(null == oprationtimeref)
								continue;
							String[] time_ref = oprationtimeref.split("#");
							if(time_ref.length != 2)
								continue;
							userVideoStr.append(tempKey).append("#").append(oprationtimeref).append("$");
						}
					}
					if(userDislike != null){
						for(String tempKey: userDislike.keySet()){
							String oprationtimeref = userDislike.get(tempKey);
							if(null == oprationtimeref)
								continue;
							String[] time_ref = oprationtimeref.split("#");
							if(time_ref.length != 2)
								continue;
							userDislikeStr.append(tempKey).append("#").append(oprationtimeref).append("$");
						}
					}
					if(count == 0){
						user_mos = value.getUser_os();
						user_ver = value.getUser_ver();
						user_mtype = value.getUser_mtype();
					}
					if(count == 2 && userOpenDocListStr != null){
						last2day_userRead += userOpenDocListStr.toString();
					}
					if(count == (maplist.size()-1)){
						result.put("starttime", dayItems.getKey());
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
			String userOpenPicListStr_result = "";
			String userKeywordListStr_result = "";
			String userSearchListStr_result = "";
			String userReadSWListStr_result = "";
			String userReadCHListStr_result = "";
			String userInterestListStr_result = "";
			String userBookListStr_result = "";
			String userDurationStr_result = "";
			String userVideoStr_result = "";
			String userDislikeStr_result = "";
			if( userOpenDocListStr != null){
				userOpenDocListStr_result = userOpenDocListStr.toString();
			}
			if( userOpenPicListStr != null){
				userOpenPicListStr_result = userOpenPicListStr.toString();
			}
			if( userKeywordListStr != null){
				userKeywordListStr_result = userKeywordListStr.toString();
			}
			if( userSearchListStr != null){
				userSearchListStr_result = userSearchListStr.toString();
			}
			if(userReadSWListStr != null){
				userReadSWListStr_result = userReadSWListStr.toString();
			}
			if(userReadCHListStr != null){
				userReadCHListStr_result = userReadCHListStr.toString();
			}
			if( userInterestListStr != null){
				userInterestListStr_result = userInterestListStr.toString();
			}
			if(userBookListStr != null){
				userBookListStr_result = userBookListStr.toString();
			}
			if(userDurationStr != null){
				userDurationStr_result = userDurationStr.toString();
			}
			if(userVideoStr != null){
				userVideoStr_result = userVideoStr.toString();
			}
			if(userDislikeStr != null){
				userDislikeStr_result = userDislikeStr.toString();
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
			if (userReadSWListStr_result.endsWith("$")) {
				userReadSWListStr_result = userReadSWListStr_result.substring(0, userReadSWListStr_result.length() - 1);
			}
			if (userInterestListStr_result.endsWith("$")) {
				userInterestListStr_result = userInterestListStr_result.substring(0, userInterestListStr_result.length() - 1);
			}
			if (userBookListStr_result.endsWith("$")) {
				userBookListStr_result = userBookListStr_result.substring(0, userBookListStr_result.length() - 1);
			}
			for(String loc : user_loc_temp.keySet()){
				int c = user_loc_temp.get(loc);
				if(c > 1){
					user_loc = user_loc + loc + "#";
				}
			}
			if(user_loc.endsWith("#")){
				user_loc = user_loc.substring(0, user_loc.length()-1);
			}
			//test
			if(HotWordUtil.uid_testlocal.containsKey(key)){
				loc_now = HotWordUtil.uid_testlocal.get(key);
			}
			//result.put(FeatureWordStatistics.readDoc_type_name, userOpenDocListStr_result);
			result.put(FeatureWordStatistics.readDoc_type_name, gson.toJson(dayliyOpenstrs));
			result.put(FeatureWordStatistics.readKW_type_name, userKeywordListStr_result);
			result.put(FeatureWordStatistics.search_type_name, userSearchListStr_result);
			result.put(FeatureWordStatistics.readSW_type_name, userReadSWListStr_result);
			result.put(FeatureWordStatistics.userInterest_type_name, userInterestListStr_result);
			result.put(FeatureWordStatistics.bookWord_type_name, userBookListStr_result);
			result.put("last2days", last2day_userRead);
			result.put("loc", loc_now + "$" + user_loc);
			result.put("u_mos", user_mos);
			result.put("u_ver", user_ver);
			result.put("u_mt", user_mtype);
			result.put("ch_open", userReadCHListStr_result);
			result.put("duration", userDurationStr_result);
			result.put("likevideo", userVideoStr_result);
			result.put("dislike", userDislikeStr_result);
			result.put("upic", userOpenPicListStr_result);
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
			client.delete(key);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		
	}

}
