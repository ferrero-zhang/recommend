package com.ifeng.iRecommend.wuyg.commonData.Update;

import com.ifeng.commen.Utils.LoadConfig;

public class commonDataUpdateConfig {
//	public static String alterDelimiter = LoadConfig
//			.lookUpValueByKey("alterDelimiter");
	public static String recordDelimiter = LoadConfig
			.lookUpValueByKey("recordDelimiter");
	public static String wordUpdateFiledir = LoadConfig
			.lookUpValueByKey("wordUpdateFiledir");
	public static String blacklistPattern = LoadConfig
			.lookUpValueByKey("blacklistPattern");
	public static String blackListMessageChannel = LoadConfig
			.lookUpValueByKey("blackListMessageChannel");
	public static String hotWordPattern = LoadConfig
			.lookUpValueByKey("hotWordPattern");
	public static String hotWordMessageChannel = LoadConfig
			.lookUpValueByKey("hotWordMessageChannel");
	public static String wordPattern = LoadConfig
			.lookUpValueByKey("wordPattern");
	public static String wordMessageChannel = LoadConfig
			.lookUpValueByKey("wordMessageChannel");
	public static String entLibKeyPattern = LoadConfig
			.lookUpValueByKey("entLibKeyPattern");
	public static String entLibMessageChannel = LoadConfig
			.lookUpValueByKey("entLibMessageChannel");
	public static String userDicPatternChannel = LoadConfig.lookUpValueByKey("userDicPatternChannel");
}
