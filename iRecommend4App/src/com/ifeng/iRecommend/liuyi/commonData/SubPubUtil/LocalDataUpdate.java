package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public abstract class LocalDataUpdate {
	protected static Logger logger = LoggerFactory.getLogger(LocalDataUpdate.class);

	protected String redis_key = "";

	// 全局使用的Jedis实例
	protected Jedis jedis;

	protected static String commonDataRedisHost;
	protected static int commonDataRedisPort;
	
	protected abstract void init();
	protected abstract void addElem2Local(String add_content);
	protected abstract void delElemFromLocal(String del_content);
	protected abstract void alterElemInLocal(String alter_content);
	
	public LocalDataUpdate(Jedis jedis){
		setJedis(jedis);
	}
	
	public LocalDataUpdate(){
	}
	
	public LocalDataUpdate(String key){
		setRedis_key(key);
	}
	
	public LocalDataUpdate(Jedis jedis,String key){
		setJedis(jedis);
		setRedis_key(key);
	}
	
	public Jedis getJedis() {
		return jedis;
	}
	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}
	public String getRedis_key() {
		return redis_key;
	}
	public void setRedis_key(String redis_key) {
		this.redis_key = redis_key;
	}
	
	
}
