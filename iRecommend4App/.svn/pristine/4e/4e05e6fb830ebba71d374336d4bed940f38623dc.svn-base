package com.ifeng.commen.blackList.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * 通用订阅发布参数
 * @author jiangmm
 *
 */
public class CommonPSParams {
	private static Log LOG = LogFactory.getLog(CommonPSParams.class);
	private static PropertiesConfiguration configLoader;
	static {
		//白名单配置文件初始化
		try {
			configLoader = new PropertiesConfiguration();
			configLoader.setEncoding("UTF-8");
			configLoader.load("conf/commonPSparams.properties");
			configLoader.setReloadingStrategy(new FileChangedReloadingStrategy());
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.error("Load properties errror " + e);
		}
	}
	
	
	
	public static String blacklistKeyInRedis = configLoader.getString("blacklistPrefix");
	public static String blackListMessageChannel=configLoader.getString("blackListMessageChannel");
	
	public static int commonDataRedisPort =configLoader.getInt("commonDataRedisPort");
	public static String commonDataRedisHost = configLoader.getString("commonDataRedisHost");
	public static int commonDataDbNum=configLoader.getInt("commonDataDbNum");
	
	public static String mail_receiver_config_name=configLoader.getString("mail_receiver_config_name");
	
	
	public static String alterDelimiter = configLoader.getString("alterDelimiter");
	public static String recordDelimiter = configLoader.getString("recordDelimiter");
	
	public static String addFilePath= configLoader.getString("addFilePath");
	public static String delFilePath=  configLoader.getString("delFilePath");
}





