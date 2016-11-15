package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


/**
 * 
 * <PRE>
 * 作用 : 
 *   本地缓存
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
 *          1.0          2016年3月2日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class  DataCache{
	private static  final Log log = LogFactory.getLog(DataCache.class);
	private static volatile LoadingCache<String, String> cache=buildCache();
	
	public static LoadingCache<String, String> getCache() {
		return cache;
	}


	public static void setCache(LoadingCache<String, String> cache) {
		DataCache.cache = cache;
	}


	private static LoadingCache<String, String> buildCache() {
		LoadingCache<String, String> cache = CacheBuilder.newBuilder()
				// 缓存最大数量, 在达到最大限制前就会回收最近未被使用的条目
				.maximumSize(300000)
				.softValues()
				.concurrencyLevel(100)
				// 缓存移除的监听器
				.removalListener(getRemovalListener())
				// 设置缓存不存在时的默认值
				.build(getCacheLoader());
		return cache;
	}
	
	
	public static String get(String key) {
		//key存在返回value，否则返回null
		return  cache.getIfPresent(key);
		
	}
	
	public static void put(String key,String value){
		if(key!=null&&!key.isEmpty()&&value!=null&&!value.isEmpty()){
			cache.put(key, value);
		}		
	}

	private static RemovalListener<String, String> getRemovalListener() {
		RemovalListener<String, String> removalListener = new RemovalListener<String, String>() {
			@Override
			public void onRemoval(RemovalNotification<String, String> notification) {
				
				if(notification.getCause().toString().equals("SIZE")){
					log.info(" remove: " + notification.getKey()+ ";because the cache size is approaching the limitation");
				}				
			}
		};
		return removalListener;
	}

	private static CacheLoader<String, String> getCacheLoader() {
		CacheLoader<String, String> cacheLoader = new CacheLoader<String, String>() {
			@Override
			public ListenableFuture<String> reload(String key,
					String oldValue) throws Exception {
				// refresh调用此方法重新加载key的值：如果key存在,返回值;如果key不存在,使用load方法缓存新值
				return Futures.immediateFuture(oldValue);
			}

			@Override
			public String load(String key) throws Exception {
				// 当get方法对应的key无法获取到值时,返回默认值并缓存
				return null;
			}
		};
		return cacheLoader;
	}
	
	public static void main(String [] args) throws ExecutionException{
		cache.put("1", "wyg");
		cache.put("K", "test");
		
		System.err.println(cache.get("K"));
	 
		cache.asMap().remove("K");
		
		System.err.println(cache.size());
		
		System.err.println(cache.get("T"));
	}

}
