package com.ifeng.iRecommend.zxc.bdhotword;


public class Config {
	public static final int DB_EVENT_COLLECT=4;
	//2 in use
	public static final int EVENT_DB=2;
	public static final int EVENT_TEST_DB=3;
	public static final int EVENT_NAME_SPLIT_RECTIFY_DB=8;
	public static final int EVENT_DOMAIN_STATIC_DB=8;
	public static final int SAME_EVENT_DB=5;
	public static final String REDIS_IP="10.32.24.194";
	public static final int REDIS_PORT=6379;
	public static final String EVENT_REC_KEY_PREFIX="eventname_rec_";
	public static final String EVENT_DOMAIN_STATIC_PREFIX="event_domain_static_";
}
