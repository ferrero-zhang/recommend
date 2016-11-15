package com.ifeng.iRecommend.kedm.util;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.ifeng.commen.Utils.FileUtil;
public class UseridCache {
	private static final Log log = LogFactory.getLog(UseridCache.class);
	private static volatile LoadingCache<String, String> cache=buildCache();
	
	public static LoadingCache<String, String> buildCache() {
		LoadingCache<String, String> cache = CacheBuilder.newBuilder()
				// 缓存最大数量(15w), 在达到最大限制前就会回收最近未被使用的条目
				.maximumSize(6000000)
				// JVM隐式管理内存
				.softValues()
				// 最大并发写线程(800)
				.concurrencyLevel(800)
				// 缓存值被写入后的过期时间(24小时)
				.expireAfterWrite(1440, TimeUnit.MINUTES)
				// 缓存移除的监听器
				.removalListener(getRemovalListener())
				// 设置缓存不存在时的默认值
				.build(getCacheLoader());
		return cache;
	}
	public static String get(String key) {
		//key存在返回value，否则返回null
		String value=cache.getIfPresent(key);
		return value;
	}
	
	public static void put(String key,String value){
		cache.put(key, value);
	}
	
	public static void writeToFile(String path){
		File f = new File(path);
		if(f.exists()){
			f.delete();
		}
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			for(String key : cache.asMap().keySet()){
				//String value = cache.get(key);
				String data = key + "\n";
				fileutil.Append2File(path, data);
			}
			log.info("finish write user cache to file...");
		}catch(Exception e){
			log.error("write user cache to file error!!!",e);
			e.printStackTrace();
		}
		fileutil.CloseRead();
	}
	
	public static void readFromFile(String path){
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line=fileutil.ReadLine()) != null){
				String[] temp = line.split("###");
				if(temp.length != 2)
					continue;
				cache.put(temp[0], temp[1]);
				System.out.println(temp[0]+temp[1]);
			}
			log.info("finish read cache from file...");
		}catch(Exception e){
			log.error("read from file to cache error!!!",e);
			e.printStackTrace();
		}
		fileutil.CloseRead();
	}
	
	private static RemovalListener<String, String> getRemovalListener() {
		RemovalListener<String, String> removalListener = new RemovalListener<String, String>() {
			@Override
			public void onRemoval(RemovalNotification<String, String> notification) {
				// 若移除原因是因为size则打印, 注意缓存超时后是不会触发onRemoval的
				if(notification.getCause().toString().equals("SIZE")){
					log.error(" remove: " + notification.getKey()+ ";because the cache size is approaching the limitation");
					//System.out.println(" remove: " + notification.getKey()+ ";because the cache size is approaching the limitation");
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

}
