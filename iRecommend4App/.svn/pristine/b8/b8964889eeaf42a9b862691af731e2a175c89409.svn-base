package com.ifeng.iRecommend.likun.userCenter.tnappuc.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisException;


/**
 * <PRE>
 * 作用 : 
 *   Jedispool封装，采用连接池管理方式
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
 *          1.0          2014-8-30        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class userRedisPoolUtil {
	private static final Log log = LogFactory.getLog(userRedisPoolUtil.class);
	private static PropertiesConfiguration REDIS_CONFIG;

	private static String redisConfigFilePath = "./conf/redis.properties";
	
    static {
		REDIS_CONFIG = null;
		try {
			REDIS_CONFIG = new PropertiesConfiguration(redisConfigFilePath);
		} catch (ConfigurationException ex) {
			log.info("can't create Redis_Config", ex);
		}
    }
    
    private static JedisPool jedisPool = null;
    
	/**
	 * @Title: redisPoolInit
	 * @Description: 初始化jedisPool
	 * @author liu_yi
	 * @throws
	 */
	private static void redisPoolInit() {
		// 配置发生变化的时候自动载入
		REDIS_CONFIG.setReloadingStrategy(new FileChangedReloadingStrategy());
		String host = REDIS_CONFIG.getString("redis_host", "localhost");
		int port = REDIS_CONFIG.getInt("redis_port", 6379);

		try {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxTotal(5000);
			config.setMaxIdle(REDIS_CONFIG.getInt("redis_pool_max_idle",
					config.getMaxIdle()));
			config.setMaxWaitMillis(REDIS_CONFIG.getLong("redis_pool_max_wait",
					config.getMaxWaitMillis()));
			config.setTestOnBorrow(true);
			config.setTestOnReturn(true);
			int timeout = REDIS_CONFIG.getInt("redis_pool_connection_timeout",
					20000);
			jedisPool = new JedisPool(config, host, port, timeout);
		} catch (Exception ex) {
			log.error("redisPoolInit failed#" + host + ":" + port, ex);
		}
	}

	public static JedisPool getJedisPool() {
		 if (jedisPool == null) {
	            redisPoolInit();
	     }
		 
		 return jedisPool;
	}
	
    /**
     * @Title: getJedisClient
     * @Description: 获得jedis客户端
     * @author liu_yi
     * @return
     * @throws
     */
    public synchronized static Jedis getJedisClient() {
        if (jedisPool == null) {
            redisPoolInit();
        }

        // 再检测一次看是否为空(可能初始化失败)
        if (jedisPool == null) {
            log.error("can not get jedis instance, init again..");
            redisPoolInit();
        }
        
        if (jedisPool == null) {
        	log.error("can not get jedis instance");
        	return null;
        }
        
		Jedis jedis = null;

		try {
			jedis = jedisPool.getResource();
		} catch (Exception e) {
			log.error("got an exception:", e);
		}
		
		return jedis;
    }

    /**
     * @Title: returnResource
     * @Description: 释放jedis连接资源
     * @author liu_yi
     * @param jedis
     * @throws
     */
    public static void returnResource(final Jedis jedis) {
        if (jedis != null && jedisPool != null) {
            jedisPool.returnResourceObject(jedis);
        }
    }

    /**
     * @Title: set
     * @Description:  将字符串值 value,关联到 key
     *                如果 key 已经持有其他值， SET 就覆写旧值，无视类型
     * @author liu_yi
     * @param key
     * @param value
     * @throws
     */
    public static void set(String key, String value, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {
            jedis.set(key, value);
        } catch (Exception e) {
            log.error("set-string-failed:" + key + "," + value, e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * @Title: setnx
     * @Description: 将 key 的值设为 value ，当且仅当 key 不存在
     *               若给定的 key 已经存在，则 SETNX 不做任何动作
     *               不存在则保存成功返回1，存在或者异常则失败返回0
     * @author liu_yi
     * @param key
     * @param value
     * @return
     * @throws
     */
    public static long setnx(String key, String value, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {
            return jedis.setnx(key, value);
        } catch (Exception e) {
            log.error("setnx-string-failed:" + key + "," + value, e);
            return 0;
        } finally {
            returnResource(jedis);
        }
    }
    
    /**
     * @Title: setex
     * @Description: 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位)
     *               如果 key 已经存在， SETEX 命令将覆写旧值
     * @author liu_yi
     * @param key
     * @param value
     * @param seconds
     * @throws
     */
    public static void setex(String key, String value, int seconds, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {
        	jedis.setex(key, seconds, value);
        } catch (Exception e) {
            log.error("setex-string-failed:" + key + "," + value + "," + seconds, e);
        } finally {
            returnResource(jedis);
        }
    }
    
    /**
     * setnx + setex key seconds value
     *
     * <pre>
     * 如果不存在key就添加并设置生存时间，单位秒
     * 如果存在key，就不做任何操作
     * 这两个动作是原子操作
     * </pre>
     *
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public static long setnxex(String key, String value, int seconds, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {

            return 0;
        } catch (Exception e) {
            log.error("setnxex-string-failed:" + key + "," + value, e);
            return 0;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * get key
     *
     * <pre>
     * 返回 key 所关联的字符串值
     * 如果 key 不存在那么返回 null
     * 假如 key 储存的值不是字符串类型返回 null
     * </pre>
     *
     * @param key
     * @return
     */
    public static String get(String key, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {
            return jedis.get(key);
        } catch (Exception e) {
            log.error("get-string-failed:" + key, e);
            return null;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * DEL key
     *
     * <pre>
     * 删除给定的一个key，返回删除成功的个数， 也就是说，如果返回0表示删除失败，1表示删除成功
     * </pre>
     *
     * @param key
     * @return
     */
    public static long del(String key, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {
            return jedis.del(key);
        } catch (Exception e) {
            log.error("delete-failed:" + key, e);
            return 0;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * EXISTS key
     *
     * <pre>
     * 检查给定 key 是否存在
     * </pre>
     *
     * @param key
     * @return
     */
    public static boolean exists(String key, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {
            return jedis.exists(key);
        } catch (Exception e) {
            log.error("exists-failed:" + key, e);
            return false;
        } finally {
            returnResource(jedis);
        }
    }
    
    public static void rename(String oldKeyName, String newKeyName, int dbNum) {
    	 Jedis jedis = getJedisClient();
         jedis.select(dbNum);
         try {
             jedis.rename(oldKeyName, newKeyName);
         } catch (Exception e) {
             log.error("keyRename-failed:" + newKeyName, e);
         } finally {
             returnResource(jedis);
         }
    }

    /**
     * ttl key
     *
     * <pre>
     * 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)
     * </pre>
     *
     * @param key
     * @return
     */
    public static long ttl(String key, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {
            return jedis.ttl(key);
        } catch (Exception e) {
            log.error("ttl-failed:" + key, e);
            return -1;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * INCR key
     *
     * <pre>
     * 将 key 中储存的数字值增一
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作（返回1）
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么就抛出异常
     * </pre>
     *
     * @param key
     * @return
     */
    public static long incr(String key, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {
            return jedis.incr(key);
        } catch (JedisException e) {
            log.error("incr-failed:" + key, e);
            throw e;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * INCRBY key increment
     *
     * <pre>
     * 将 key 所储存的值加上增量 increment 。
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCRBY 命令。
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * </pre>
     *
     * @param key
     * @param increment
     * @return
     */
    public static long incrBy(String key, long increment, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {
            return jedis.incrBy(key, increment);
        } catch (JedisException e) {
            log.error("incrBy-failed:" + key, e);
            throw e;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * DECR key
     *
     * <pre>
     * 将 key 中储存的数字值减一
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么就抛出异常
     * </pre>
     *
     * @param key
     * @return
     */
    public static long decr(String key, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {
            return jedis.decr(key);
        } catch (JedisException e) {
            log.error("decr-failed:" + key, e);
            throw e;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     *
     * DECRBY key decrement
     *
     * <pre>
     * 将 key 所储存的值减去减量 decrement 。
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECRBY 操作。
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     * </pre>
     *
     * @param key
     * @param decrement
     * @return
     */
    public static long decrBy(String key, long decrement, int dbNum) {
        Jedis jedis = getJedisClient();
        jedis.select(dbNum);
        try {
            return jedis.decrBy(key, decrement);
        } catch (JedisException e) {
            log.error("decrBy-failed:" + key, e);
            throw e;
        } finally {
            returnResource(jedis);
        }
    }
    
    public static Set<String> getKeys(String keyPattern, int dbNum) {
    	Jedis jedis = null;
        Set<String> result = null;
        boolean borrowOrOprSuccess = true;
        
		try {
			jedis = getJedisClient();
			jedis.select(dbNum);
			result = jedis.keys(keyPattern);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			log.error("got an exception:",e);
			returnResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				returnResource(jedis);
		}
		
		return result;
    }
    
    public static long listPush(String listName, String listItemValue, int dbNum) {
    	 Jedis jedis = getJedisClient();
    	 jedis.select(dbNum);
    	 try {
    		 return jedis.lpush(listName, listItemValue); 
         } catch (JedisException e) {
             log.error("jedis-lpush-failed:" + listItemValue, e);
             throw e;
         } finally {
             returnResource(jedis);
         }
    }
    
    
    /*
	 * hash set;sucess:>=0 
	 */
	public static Long hset(String key,String hKey,String value,int dbID) {
		Long rt = -1L;
		
		if(dbID < 0)
			return rt;
		
		rt = 1L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisClient();
			jedis.select(dbID);
			//key从空创建
			if (!jedis.exists(key))
				rt = 1L;
			rt = jedis.hset(key,hKey,value);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			log.error("got an exception:",e);
			returnResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				returnResource(jedis);
		}
        return rt;
	
	}
	
	//hash get;if exception found,return "error";
	public static String hget(String key,String hKey,int dbID) {
		if(dbID < 0)
			return null;
		String rt = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisClient();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return null;
			rt = jedis.hget(key,hKey);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = "error";
			log.error("got an exception:",e);
			returnResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				returnResource(jedis);
		}
        return rt;
	
	}
	
	//hash get len;success:>=0
	public static Long hlen(String key,int dbID){
		Long rt = -1L;
		if(dbID < 0)
			return rt;		
		rt = 0L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisClient();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return 0L;
			rt = jedis.hlen(key);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			log.error("got an exception:",e);
			returnResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				returnResource(jedis);
		}
        return rt;
	}
	
	/*
	 * hash get all;exception,then return error;
	 */
	public static Map<String, String> hgetAll(String key,int dbID) {
		if(dbID < 0)
			return null;
		Map<String, String> rt = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisClient();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return null;
			rt = jedis.hgetAll(key);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = new HashMap<String,String>();
			rt.put("error", "1");
			log.error("got an exception:",e);
			returnResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				returnResource(jedis);
		}
        return rt;
	
	}
	
	/**
	 * @Title: hmset
	 * @Description: hash set all
	 * @author liu_yi
	 * @param key
	 * @param value
	 * @param dbID
	 * @throws
	 */
	public static void hmset(String key, Map<String, String> value, int dbID) {
		if(dbID < 0)
			return;
		
		if (null == value || value.isEmpty()) {
			return;
		}
		
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisClient();
			jedis.select(dbID);
			
			jedis.hmset(key, value);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			log.error("got an exception:",e);
			returnResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				returnResource(jedis);
		}
		
        return;
	}

	/*
	 * hash del;request sucess:>=0; 
	 */
	public static Long hdel(String key,String hKey,int dbID) {
		Long rt = -1L;
		if(dbID < 0)
			return rt;		
		rt = 0L;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try {
			jedis = getJedisClient();
			jedis.select(dbID);
			if (!jedis.exists(key))
				return -1L;
			rt = jedis.hdel(key, hKey);
		} catch (Exception e) {
			borrowOrOprSuccess = false;
			rt = -1L;
			log.error("got an exception:",e);
			returnResource(jedis);
		} finally {
			if(borrowOrOprSuccess == true)
				returnResource(jedis);
		}
        return rt;	
	}
    
    /*    
    public static void main(String[] args) {
    	int dbNum = 8;
    	Gson gson = new Gson();
    	
    	UserDoc test = new UserDoc("123456", "http://test.com", "美国自由女神", "asdasd", "asdqw", "qweqw");
		String testStr = gson.toJson(test);
		 
		Jedis jedis = getJedisClient();
        jedis.select(dbNum); 
        Pipeline pipeline = jedis.pipelined();
        List<Object> results = null;
    	long startMili = System.currentTimeMillis();   
    	try {
    		for (int i = 0 ; i != 5000; i++) {
        		pipeline.setex("id"+i, 10*60, testStr);
        		//pipeline.get("id"+i);
        	}
        	results = pipeline.syncAndReturnAll();
		} catch (Exception e) {
			// error log output
		} finally {
			returnResource(jedis);
		}
    	 
    	long endMili = System.currentTimeMillis();
		System.out.println("总耗时为："+(endMili-startMili)+"毫秒");
		
    }
    */
}