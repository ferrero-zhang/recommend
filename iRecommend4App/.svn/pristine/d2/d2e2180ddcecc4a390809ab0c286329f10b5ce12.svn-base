package com.ifeng.iRecommend.lidm.userLog;

/**
 * <PRE>
 * 作用 : 
 *   工厂方法。
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
 *          1.0          2014-01-21       lidm          change
 * -----------------------------------------------------------------------------
 * </PRE>
 */

import java.util.HashMap;

public class LogToDBFactory {
	public static LogToDB createLogToHbase(String tableName,HashMap<String, String> dataHashMap) {
		return new LogToHbase(tableName,dataHashMap);
	}

	public static LogToDB createLogToCF(HashMap<String, String> dataHashMap) {
		return new LogToCF(dataHashMap);
	}
}
