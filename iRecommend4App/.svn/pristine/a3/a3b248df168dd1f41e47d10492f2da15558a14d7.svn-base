package com.ifeng.commen.Utils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * <PRE>
 * 作用 : 
 *   载入常规配置文件，策略是配置文件有变化的时候，自动重新加载 
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
 *          1.0          2015年6月9日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class ConfigAutoLoader {
	private static final Log log = LogFactory.getLog(ConfigAutoLoader.class);
	
	// 同步锁
	private static final Object obj = new Object();

	// 配置文件
	private static PropertiesConfiguration prop = null;

	// 配置对象singleton
	private static ConfigAutoLoader config = null;

	// config文件名
	private final static String config_file_dir = "./conf/config.properties";

	static {
		prop = new PropertiesConfiguration();
			try {
				prop = new PropertiesConfiguration(config_file_dir);
			} catch (ConfigurationException ex) {
				log.error("config init error.", ex);
			}
			// 设置reload策略，当文件被修改之后reload（默认5s中检测一次）
			prop.setReloadingStrategy(new FileChangedReloadingStrategy());
	
	}
	
	/**
	 * @Title: getInstance
	 * @Description: 获取单例模式对象实例
	 * @author liu_yi
	 * @return 唯一对象实例
	 * @throws
	 */
	public static ConfigAutoLoader getInstance() {
		if (null == config) {
			synchronized (obj) {
				config = new ConfigAutoLoader();
			}
		}
		
		return config;
	}

	/**
	 * @Title: getProperty
	 * @Description: 获取配置文件中的相关配置值,返回String值
	 * @author liu_yi
	 * @param key
	 * @return 没有找到返回null
	 * @throws
	 */
	public String getProperty(String key) {
		return (String) prop.getProperty(key);
	}

	
	/**
	 * @Title: getPropertyWithKey
	 * @Description: 供外部调用的方法，获取相应的配置值
	 * @author liu_yi
	 * @param key
	 * @return
	 * @throws
	 */
	public static String getPropertyWithKey(String key) {
		return getInstance().getProperty(key);
	}

	// test
	public static void main(String... args) {
		System.out.println(getPropertyWithKey("operatetime_pos"));
		System.out.println(getPropertyWithKey("hardwareid_pos"));
	}
}
