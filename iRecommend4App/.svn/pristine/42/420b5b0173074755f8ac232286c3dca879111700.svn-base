package com.ifeng.commen.redis;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.JsonUtils;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class JedisQueue2<T> {
	private static final Log log = LogFactory.getLog(JedisQueue2.class);
    private Jedis jedis;
	private String suffix = "_";
    private String name;
    private Class<T> clazz;

    
    public JedisQueue2(Jedis jedisIn, Class<T> clazz) {
        this.jedis = jedisIn;
        this.clazz = clazz;
        this.name = clazz.getName();
    }

    public JedisQueue2(Jedis jedisIn, Class<T> clazz, String suffix) {
    	this.jedis = jedisIn;
        this.name = clazz.getName();
        this.clazz = clazz;
        this.suffix += suffix;
        this.name += this.suffix;
    }

    
    public void push(T... ts) {
        try {
            for (T t : ts)
                jedis.lpush(name, JsonUtils.toJson(t,false));
        } finally {
            
        }
    }

    public void push(Collection<T> collection) {
       
        try {
            for (T t : collection)
                jedis.lpush(name, JsonUtils.toJson(t,false));
        } finally {
           
        }
    }

	public T take() {
		if (!jedis.exists(name))
			return null;
		String jsonT = jedis.rpop(name);
		if (jsonT != null) {
			return (T) JsonUtils.fromJson(jsonT, clazz);
		} else
			return null;

	}
}
