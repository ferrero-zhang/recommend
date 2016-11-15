package com.ifeng.commen.blackList.publisher;

import redis.clients.jedis.Jedis;

public class RedisUpdateMethod {
	protected Jedis jedis;

	public RedisUpdateMethod(Jedis jedis) {
		this.setJedis(jedis);
	}

	public boolean updateInfo_Add(String data) {
		return true;
	}

	public boolean updateInfo_Alter(String data) {
		return true;
	}

	public boolean updateInfo_Del(String data) {
		return true;
	}

	public Jedis getJedis() {
		return jedis;
	}

	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}
}
