package com.ifeng.commen.redis;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.JsonUtils;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisQueue<T> {
	private static final Log LOG = LogFactory.getLog(JedisQueue.class);
    private JedisPool JedisPool;
    private String suffix = "_";
    private String name;
    private Class<T> clazz;
    private int dbID;

    public JedisQueue(JedisPool JedisPool, Class<T> clazz,int dbID) {
        this.JedisPool = JedisPool;
        this.clazz = clazz;
        this.name = clazz.getName();
        this.dbID = dbID;
    }

    public JedisQueue(JedisPool JedisPool, Class<T> clazz, String suffix,int dbID) {
        this.JedisPool = JedisPool;
        this.name = clazz.getName();
        this.clazz = clazz;
        this.suffix += suffix;
        this.name += this.suffix;
        this.dbID = dbID;
    }

    
    public void push(T... ts) {
    	Jedis jedis = null;
    	boolean borrowOrOprSuccess = true;
        try{
        	jedis = JedisPool.getResource();
        	jedis.select(dbID);
        }catch(Exception e){
        	LOG.error("got an exception:",e);
        	return;
        }
        try {
            for (T t : ts)
                jedis.lpush(name, JsonUtils.toJson(t));
        } catch (Exception e) {
			borrowOrOprSuccess = false;
			LOG.error("got an exception:", e);
			JedisPool.returnBrokenResource(jedis);
		} finally {
			if (borrowOrOprSuccess == true)
				JedisPool.returnResource(jedis);
		}
    }

    public void push(Collection<T> collection) {
    	Jedis jedis = null;
    	boolean borrowOrOprSuccess = true;
        try{
        	jedis = JedisPool.getResource();
        	jedis.select(dbID);
        }catch(Exception e){
        	LOG.error("got an exception:",e);
        	return;
        }
        try {
            for (T t : collection)
                jedis.lpush(name, JsonUtils.toJson(t));
           
        }catch (Exception e) {
			borrowOrOprSuccess = false;
			LOG.error("got an exception:", e);
			JedisPool.returnBrokenResource(jedis);
		} finally {
			if (borrowOrOprSuccess == true)
				JedisPool.returnResource(jedis);
		}
    }

    public long getQueueSize() {
    	Jedis jedis = null;
    	boolean borrowOrOprSuccess = true;
        try{
        	jedis = JedisPool.getResource();
        	jedis.select(dbID);
        }catch(Exception e){
        	LOG.error("got an exception:",e);
        	return -1L;
        }
        Long size = 0L;
        try {
	        size = jedis.llen(name);  
        }catch (Exception e) {
			borrowOrOprSuccess = false;
			LOG.error("got an exception:", e);
			JedisPool.returnBrokenResource(jedis);
		} finally {
			if (borrowOrOprSuccess == true)
				JedisPool.returnResource(jedis);
		}
		
		return size;
    }
    
    public T take() {
        Jedis jedis = null;
        boolean borrowOrOprSuccess = true;
        try{
        	jedis = JedisPool.getResource();
        	jedis.select(dbID);
        }catch(Exception e){
        	LOG.error("got an exception:",e);
        	return null;
        }
		try {
			if (!jedis.exists(name))
				return null;
			
			String jsonT = jedis.rpop(name);
			if (jsonT != null) {
				return (T) JsonUtils.fromJson(jsonT, clazz);
			}
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			LOG.error("got an exception:", e);
			JedisPool.returnBrokenResource(jedis);
		} finally {
			if (borrowOrOprSuccess == true)
				JedisPool.returnResource(jedis);
		}
        return null;
    }
    
    public ArrayList<T> take(int num) {
        Jedis jedis = null;
        boolean borrowOrOprSuccess = true;
        try{
        	jedis = JedisPool.getResource();
        	jedis.select(dbID);
        }catch(Exception e){
        	LOG.error("got an exception:",e);
        	return null;
        }
		try {
			if (!jedis.exists(name))
				return null;
			
			ArrayList<T> alt = new ArrayList<T>();
			for(int i=0;i<num;i++)
			{
				String jsonT = jedis.rpop(name);
				if (jsonT != null) {
					alt.add((T) JsonUtils.fromJson(jsonT, clazz));
				}
			}
			return alt;
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			LOG.error("got an exception:", e);
			JedisPool.returnBrokenResource(jedis);
		} finally {
			if (borrowOrOprSuccess == true)
				JedisPool.returnResource(jedis);
		}
        return null;
    }
}
