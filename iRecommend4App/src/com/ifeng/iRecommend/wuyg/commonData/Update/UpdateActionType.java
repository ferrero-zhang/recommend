package com.ifeng.iRecommend.wuyg.commonData.Update;
/**
 * 
 * <PRE>
 * 作用 : 
 *    redis操作的类型  
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
 *          1.0          2015年12月9日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public enum UpdateActionType {
	//增加词
	ADD_WORD,
	//增加文件
	ADD_FILE, 
	//修改词
	ALTER_WORD,
	//修改文件
	ALTER_FILE,
	//删除词
	DEL_WORD,
	//删除文件
	DEL_FILE;

	public static UpdateActionType getActionType(String action_type) {
		return valueOf(action_type);
	}
}
