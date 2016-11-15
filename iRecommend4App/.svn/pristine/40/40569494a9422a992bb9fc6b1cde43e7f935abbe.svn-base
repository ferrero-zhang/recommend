package com.ifeng.iRecommend.kedm.userlog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <PRE>
 * 作用 : 
 *   对日志文件解析得到的结构化信息，处理成uid-->userinfo的结构
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
 *          1.0          2015年6月16日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class LogParserResultProcess {
	private static final Log log = LogFactory.getLog(LogParserResultProcess.class);
	
	/**
	 * @Title: getUserInfoMap
	 * @Description: 根据单个文件解析得到全部用户操作信息，得到整理后的结构化用户信息Map
	 * @author liu_yi
	 * @param userIdSet 
	 *  用户id集合
	 * @param userPageLog
	 *  用户看过文章集合
	 * @param userAddInterestLog
	 *  订阅增
	 * @param userDelInterestLog
	 *  订阅删
	 * @param userAddBookLog
	 *  订阅增
	 * @param userDelBookLog
	 *  订阅删
	 * @param userSetLocLog
	 *  地理位置
	 * @return
	 * @throws
	 */
	public static Map<String, UserInfoFromLog> getUserInfoMap(Set<String> userIdSet, 
			Map<String, Map<String, String>> userPageLog,
			Map<String, Map<String, String>> userReadSWLog,
			Map<String, Map<String, String>> userStoreLog,
			Map<String, Map<String, String>> userTsLog,
			Map<String, Map<String, String>> userKeywordListLog,
			Map<String, Map<String, String>> userSearchListLog,
			Map<String, Map<String, String>> userAddInterestLog,
			Map<String, Map<String, String>> userDelInterestLog,
			Map<String, Map<String, String>> userAddBookLog,
			Map<String, Map<String, String>> userDelBookLog,
			Map<String, Map<String, String>> userSetLocLog,
			Map<String, Map<String, String>> userPageDuaLog,
			Map<String, Map<String, String>> userDislikeLog,
			Map<String, Map<String, String>> userVidLog,
			Map<String, Map<String, String>> userPicLog,
			Map<String, String> userPlat) {
		if (null == userIdSet || userIdSet.isEmpty()) {
			log.info("Empty userIdSet");
			return null;
		}
		
		Map<String, UserInfoFromLog> result = new HashMap<String, UserInfoFromLog>();
		
		for (String tempUID : userIdSet) {
			Map<String, String> tempUserPage = userPageLog.get(tempUID);
			Map<String, String> tempUserReadSW = userReadSWLog.get(tempUID);
			Map<String, String> tempUserStore = userStoreLog.get(tempUID);
			Map<String, String> tempUserTs = userTsLog.get(tempUID);
			Map<String, String> tempUserKeyword = userKeywordListLog.get(tempUID);
			Map<String, String> tempUserSearch = userSearchListLog.get(tempUID);
			Map<String, String> tempAddInterest = userAddInterestLog.get(tempUID);
			Map<String, String> tempDelInterest = userDelInterestLog.get(tempUID);
			Map<String, String> tempAddBook = userAddBookLog.get(tempUID);
			Map<String, String> tempDelBook = userDelBookLog.get(tempUID);
			Map<String, String> tempUserSetLoc = userSetLocLog.get(tempUID);
			Map<String, String> tempUserPageDura = userPageDuaLog.get(tempUID);
			Map<String, String> tempUserDislike = userDislikeLog.get(tempUID);
			Map<String, String> tempUserVid = userVidLog.get(tempUID);
			Map<String, String> tempUserPic = userPicLog.get(tempUID);
			String tempUserPlat = userPlat.get(tempUID);
			if (null == tempUserPage && null == tempUserReadSW && null == tempUserStore && null == tempUserTs &&
					null == tempUserKeyword && null == tempUserSearch && null == tempAddInterest && 
					null == tempDelInterest && null == tempAddBook && null == tempDelBook && null == tempUserSetLoc && 
					null == tempUserPlat && null == tempUserPageDura && null == tempUserDislike && null == tempUserVid &&
					null == tempUserPic) {
				continue;
			}
			
			UserInfoFromLog tempUserInfo = new UserInfoFromLog(tempUID, tempUserSetLoc, tempUserPage, tempUserKeyword,
					tempAddInterest, tempDelInterest, tempAddBook, tempDelBook);
			tempUserInfo.setUser_search_time_map(tempUserSearch);
			tempUserInfo.setRead_sw_time_map(tempUserReadSW);
			tempUserInfo.setStore_docid_time_map(tempUserStore);
			tempUserInfo.setTs_docid_time_map(tempUserTs);
			tempUserInfo.setPage_duration(tempUserPageDura);
			tempUserInfo.setDislike_doc(tempUserDislike);
			tempUserInfo.setVid_duration(tempUserVid);
			tempUserInfo.setOpen_pic_id_time_map(tempUserPic);
			String[] palts = tempUserPlat.split("#");
			if(palts.length == 3){
				tempUserInfo.setUser_os(palts[0]);
				tempUserInfo.setUser_ver(palts[1]);
				tempUserInfo.setUser_mtype(palts[2]);
			}
			result.put(tempUID, tempUserInfo);
		}
				
		return result;
	}
}
