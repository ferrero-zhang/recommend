package com.ifeng.iRecommend.zhangxc.userlog.phonePrice;

import com.ifeng.iRecommend.zhangxc.userlog.phonePrice.ConfigAutoLoader;

public class GlobalParams {
	public static String updateUserSetRedisIP = ConfigAutoLoader.getPropertyWithKey("updateUserIDredisIP");
	public static String userReadDocList = ConfigAutoLoader.getPropertyWithKey("userReadDocList");
	public static String userReadKeywordList = ConfigAutoLoader.getPropertyWithKey("userReadKeywordList");
	public static String userInterestWordList = ConfigAutoLoader.getPropertyWithKey("userInterestWordList");
	public static String userBookWordList = ConfigAutoLoader.getPropertyWithKey("userBookWordList");
	public static String userLocList = ConfigAutoLoader.getPropertyWithKey("userSetLoc");
	//无用public static final int userInfoDBnum = Integer.valueOf(ConfigAutoLoader.getPropertyWithKey("userInfoDBnum"));
	public static final int userCenterDBnum = Integer.valueOf(ConfigAutoLoader.getPropertyWithKey("userCenterDBnum"));
	public static final int updateUserSetDBnum = Integer.valueOf(ConfigAutoLoader.getPropertyWithKey("updateUserIDDBNum"));
	public static final int topAskNum = Integer.valueOf(ConfigAutoLoader.getPropertyWithKey("topNum"));
	public static final int lastViewDay = Integer.valueOf(ConfigAutoLoader.getPropertyWithKey("lastViewDay"));
	

	public static final String POST_SOLR_SERVER_URI=ConfigAutoLoader.getPropertyWithKey("post.solr.server.uri");

	public static final String SOLR_COLLECTION = ConfigAutoLoader.getPropertyWithKey("colName");
	public static final String SOLR_DOCUMENT_ID = ConfigAutoLoader.getPropertyWithKey("solr.document.id");
	public static final String ZOOKEEPER_HOST=ConfigAutoLoader.getPropertyWithKey("zkHost");
	public static final String ZOOKEEPER_CONNECT_TIMEOUT=ConfigAutoLoader.getPropertyWithKey("zkConnectTimeout");
	public static final String ZOOKEEPER_CLIENT_TIMEOUT=ConfigAutoLoader.getPropertyWithKey("zkClientTimeout");

	public static final String HTTPCLIENT_MAX_CONNECTIONS=ConfigAutoLoader.getPropertyWithKey("maxConnections");
	public static final String HTTPCLIENT_MAX_CONNECTIONS_PER_HOST=ConfigAutoLoader.getPropertyWithKey("maxConnectionsPerHost");
	public static final String HTTPCLIENT_CONN_TIMEOUT=ConfigAutoLoader.getPropertyWithKey("connTimeout");
	public static final String HTTPCLIENT_SOCKET_TIMEOUT=ConfigAutoLoader.getPropertyWithKey("socketTimeout");
	public static String[] noSenseKeyword = {
		"我的头条", 
		"凤凰头条",
		"热门",
		"体育头条",
		"娱乐头条",
	};
	public static String ebug = ConfigAutoLoader.getPropertyWithKey("bug");
}
