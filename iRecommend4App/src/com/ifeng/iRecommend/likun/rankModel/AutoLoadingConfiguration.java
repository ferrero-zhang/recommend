/**
 * 
 */
package com.ifeng.iRecommend.likun.rankModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;

/**
 * <PRE>
 * 作用 : 动态加载配置文件的工具类
 *   
 * 使用 : 通过构造函数传入配置文件路径，和日志对象
 *      提供三个函数来获取配置文件内容
 *   
 * 示例 :
 *   
 * 注意 :动态加载配置文件，直接修改配置文件即可无需重启程序
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年4月16日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class AutoLoadingConfiguration {
	private PropertiesConfiguration config;
	private Log LOG;
	private String configFilePath;
	
	public AutoLoadingConfiguration(String configFilePath,Log LOG) {
		// TODO Auto-generated constructor stub
		try {
			this.configFilePath = configFilePath;
			this.LOG = LOG;
			config = new PropertiesConfiguration();
			config.setEncoding("UTF-8");
			config.load(configFilePath);
			//自动加载修改后的config
			config.setReloadingStrategy(new FileChangedReloadingStrategy());
			
		}  catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.error("Load"+configFilePath+" Config error", e);
		}
	}
	/**
	 * 通过配置文件中的key值来查询对应的value值
	 * 
	 * @param String key
	 * 
	 * @return String value
	 */
	public String getValueBykey(String key){
		if(config == null){
			LOG.error("Load"+configFilePath+" Config error config is null");
			return null;
		}
		String value = config.getString(key);
		return value;
	}
	
	/**
	 * 直接返回配置文件的Map
	 * 
	 * @param null
	 * 
	 * @return Map<String，String>
	 */
	public Map<String, String> getPropertiesMap(){
		HashMap<String, String> propertiesMap = new HashMap<String, String>();
		if(config == null){
			LOG.error("Load"+configFilePath+" Config error config is null");
		}
		Iterator<?> it =config.getKeys();
		while(it.hasNext()){
			String key = (String) it.next();
			String value = config.getString(key);
			propertiesMap.put(key, value);
		}
		return propertiesMap;
	}
	
	/**
	 * 获取配置文件的key集合
	 * 
	 * @param null
	 * 
	 * @return Set<String>
	 */
	public Set<String> getKeySet(){
		HashSet<String> keySet = new HashSet<String>();
		if(config == null){
			LOG.error("Load"+configFilePath+" Config error config is null");
		}
		Iterator<?> it =config.getKeys();
		while(it.hasNext()){
			String key = (String) it.next();
			keySet.add(key);
		}
		return keySet;
	}

}
