package com.ifeng.commen.oscache;

import java.util.Date;
import java.util.Properties;

import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: ifeng.com</p>
 * @author :Hu WeiQi
 * @version 1.0
 * <p>--------------------------------------------------------------------</p>
 * <p>date                   author                    reason             </p>
 * <p>2007-11-8             Hu WeiQi               create the class     </p>
 * <p>2012-09-14            Shi Yuxin               降低开放度为包     </p>
 * <p>--------------------------------------------------------------------</p>
 */
public class OSCache extends GeneralCacheAdministrator  {
	
	//过期时间(单位为秒);
	private int refreshPeriod;
	//关键字前缀字符;
	private String keyPrefix = "";
	private static final long serialVersionUID = -4397192926052141162L;
	
//	//20120926获取properties绝对路径，保存在{project}/conf/oscache.properties
//	private static String propertiesPath = "conf/oscache.properties";
	
	public OSCache(String propertiesPath,String keyPrefix,int refreshPeriod){
		//20120926读取配置。
		super(Config.loadProperties(propertiesPath, "Load oscache properties from " + propertiesPath));
		
		System.out.println("load oscache config from:"+propertiesPath);
		
		this.keyPrefix = keyPrefix;
		this.refreshPeriod = refreshPeriod;
		
		//20120926验证是否读取成功。
		System.out.println(config.get(CACHE_BLOCKING_KEY));
	}
	
	//添加被缓存的对象;
	public void put(String key,Object value){
		this.putInCache(this.keyPrefix+"_"+key,value);
	}
	
	//删除被缓存的对象;
	public void remove(String key){
		this.flushEntry(this.keyPrefix+"_"+key);
	}
	
	//删除所有被缓存的对象;
	public void removeAll(Date date){
		this.flushAll(date);
	}              
	
	public void removeAll(){
		this.flushAll();
	}
	
	//获取被缓存的对象;
	public Object get(String key) throws Exception{
		try{
			return this.getFromCache(this.keyPrefix+"_"+key,this.refreshPeriod);
		} catch (NeedsRefreshException e) {
			this.cancelUpdate(this.keyPrefix+"_"+key);
			throw e;
		}
	}   
}
