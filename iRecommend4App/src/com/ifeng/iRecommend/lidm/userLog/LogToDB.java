package com.ifeng.iRecommend.lidm.userLog;

/**
 * <PRE>
 * 作用 : 
 *   将log解析后的数据写入数据库。
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

public interface LogToDB {
    public void InitDB();
    public void PushLogToDB();
}
