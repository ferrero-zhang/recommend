package com.ifeng.iRecommend.featureEngineering.databaseOperation;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;

public class JedisTest {
	static Logger LOG = Logger.getLogger(JedisTest.class);
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		int redisDbNum = 0;
		Jedis jedis = new Jedis("10.32.24.194", 6379);
		try {
			jedis.select(redisDbNum);
			String key = "美国";
			String value = jedis.get(key);
			System.out.println(value);
//			jedis.set(item.getID(), fjson);
//			jedis.set(item.getTitle(), fjson);
//			jedis.set(item.getUrl(), fjson);
			jedis.close();
//			LOG.info("set-string-succeed:" + strForLog + "," + fjson);
		} catch (Exception e) {
//			LOG.error("set-string-failed:" + strForLog + "," + fjson, e);
//			LOG.error("[ERROR] In write item to Redis." + strForLog, e);
			jedis.close();
		}
	}

}
