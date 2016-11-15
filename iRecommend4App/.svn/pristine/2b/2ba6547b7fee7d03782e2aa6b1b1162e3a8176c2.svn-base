package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

/**
 * <PRE>
 * 作用 : 
 *   订阅频道名称枚举，用于逻辑分支控制。
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
 *          1.0          2015年11月25日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public enum ChannelName {
	// 其他类型后续添加即可
    entLib,  // 实体库
	blackList, // 黑名单
	hotWord, //热词（百度热词和编辑热词）
	wordReadable,  //可读与不可读词
    articleSource,  //稿源
    aliasData,   //主别名
    dataExpLib,  //信息全表达
    allWordLib,   //全量词
    tempCustomDic, //用户临时自定义词典
    hotWordGraph;//热点事件导入图结构的
//    entLibTEST,  // 实体库
//	blackListTEST, // 黑名单
//	hotWordTEST, //热词（百度热词和编辑热词）
//	wordReadableTEST,  //可读与不可读词
//    articleSourceTEST,  //稿源
//    aliasDataTEST,   //主别名
//    dataExpLibTEST,  //信息全表达
//    allWordLibTEST,   //全量词
//    tempCustomDic; //用户临时自定义词典
	public static ChannelName getMessageType(String message_type) {
		return valueOf(message_type);
	}
}