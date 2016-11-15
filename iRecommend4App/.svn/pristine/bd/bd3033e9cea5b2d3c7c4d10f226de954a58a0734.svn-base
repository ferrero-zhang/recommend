/**
 * 
 */
package com.ifeng.iRecommend.dingjw.front_rankModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.iRecommend.dingjw.front_rankModel.appItemFront;

/**
 * <PRE>
 * 作用 : 用于投放过程中无法在Hbase中查到对应Item的IMCP_ID的缓存，并对每个缓存IMCP_ID设置相应的生命周期（除缓存ID外，同时需要缓存AppItemFront对象）
 *   
 * 使用 : 对外提供三个接口
 * 1、void addToCache(appItemFront appitem)像缓存添加APPitemfront对象
 * 2、HashSet<appItemFront> getItemSet() 从缓存中取出appitemfront集合
 * 3、void update(String id) 用于将在hbase命中item在缓存中更新删除  
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2014年12月10日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

/*
*缓存对象数据结构
*/
class CacheItem {
	private final int LIFETIMES = 8;      //最多查询次数常量
	private int lifeTime ;
	private String id;
	private appItemFront item;               //缓存对象
	
	public CacheItem(String id,appItemFront item){
		this.lifeTime = LIFETIMES;
		this.item = item;
		this.id = id;
	}
	public void degree(){
		this.lifeTime--;
	}
	public boolean isLive(){
		if(this.lifeTime>0){
			return true;
		}else{
			return false;
		}
	}
	public int getLifeTime(){
		return this.lifeTime;
	}
	public String getId(){
		return this.id;
	}
	public appItemFront getItem(){
		return this.item;
	}
}

public class ItemCache{ 
	private static final Log LOG = LogFactory.getLog("ItemCache");
	private HashMap<String,CacheItem> cacheItemMap;   //缓存MAP
	public ItemCache(){
		cacheItemMap = new HashMap<String, CacheItem>();
	}
	//用于投放模型取缓存数据，每调用一次ID缓存的生命周期减1并将超过生命周期的item删除
	public HashSet<appItemFront> getItemSet(){	
		HashSet<appItemFront> itemSet = new HashSet<appItemFront>();
		Set<String> keySet = new HashSet<String>();
		keySet.addAll(cacheItemMap.keySet());
		for(String key : keySet){
			CacheItem item = cacheItemMap.get(key);
			if(!item.isLive()){
				LOG.info("Remove "+key+" Life time left : "+item.getLifeTime());
				cacheItemMap.remove(key);
			}else{
				item.degree();
				itemSet.add(item.getItem());
			}
		}
		LOG.info("Size of idCache "+cacheItemMap.size());
		return itemSet;
	}
	//用于投放模型添加缓存项
	public void addToCache(appItemFront appitem){
		if(appitem == null){
			return;
		}else{
			String id = appitem.getImcpID();
			CacheItem item = cacheItemMap.get(id);
			if(item == null){
				item = new CacheItem(id, appitem);
				cacheItemMap.put(id, item);
				LOG.info("new add to cache : "+id);
			}else{
				LOG.info("this id already in the cache : "+id+" left life time : "+item.getLifeTime());
			}
		}
	}
	//用于投放模型将在hbase命中的项进行删除
	public void delete(String id){
		if(cacheItemMap.get(id)!=null){
			LOG.info(id+" has found in hbase just removed");
			cacheItemMap.remove(id);
		}
	}
}
