package com.ifeng.iRecommend.likun.userCenter.tnappuc.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;

/**
 * <PRE>
 * 作用 : 
 *   分片redis主从集群工具类
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
 *          1.0          2015年7月09日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class userShardedRedisUtil {
	private static final Log log = LogFactory.getLog(userShardedRedisUtil.class);
	
	protected static ReentrantLock lockPool = new ReentrantLock();  
    protected static ReentrantLock lockJedis = new ReentrantLock();  
    
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
    
    private static ShardedJedisPool jedisPool_master = null;
    private static ShardedJedisPool jedisPool_slave = null;
    
	/**
	 * @Title: redisPoolInit
	 * @Description: 初始化jedisPool
	 * @author liu_yi
	 * @throws
	 */
	private static void redisPoolInit() {
		// 当前锁是否已经锁住?if锁住了，do nothing; else continue  
	    assert !lockPool.isHeldByCurrentThread(); 
		  
		// 配置发生变化的时候自动载入
		REDIS_CONFIG.setReloadingStrategy(new FileChangedReloadingStrategy());
		
		try {
			String[] redis_master_hosts = REDIS_CONFIG.getString("redis_master_hosts").split("#");
			String[] redis_master_ports = REDIS_CONFIG.getString("redis_master_ports").split("#");
			String[] redis_slave_hosts = REDIS_CONFIG.getString("redis_slave_hosts").split("#");
			String[] redis_slave_ports = REDIS_CONFIG.getString("redis_slave_ports").split("#");
			
			List<JedisShardInfo> master_shards = new ArrayList<JedisShardInfo>();
			master_shards = buildJedisShard(redis_master_hosts, redis_master_ports);
			
			List<JedisShardInfo> slave_shards = new ArrayList<JedisShardInfo>();
			slave_shards = buildJedisShard(redis_slave_hosts, redis_slave_ports);		
			
			JedisPoolConfig config = new JedisPoolConfig();
			// 是否启用后进先出, 默认true
			config.setLifo(true);
			 
			// 最大空闲连接数
			config.setMaxIdle(10);
			
			// 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted), 如果超时就抛异常,小于零:阻塞不确定的时间
			config.setMaxWaitMillis(2000);
			 
			// 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
			config.setMinEvictableIdleTimeMillis(1800000);
			 
			// 最小空闲连接数, 默认0
			config.setMinIdle(0);
			 
			// 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
			config.setNumTestsPerEvictionRun(3);
			 
			// 对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)   
			config.setSoftMinEvictableIdleTimeMillis(1800000);
			 
			// 在获取连接的时候检查有效性, 默认false
			config.setTestOnBorrow(false);
			config.setMaxTotal(50000);
			
			// 在空闲时检查有效性, 默认false
			config.setTestWhileIdle(false);
			
			log.info("build jedisPool_master...");
			jedisPool_master = new ShardedJedisPool(config, master_shards, Hashing.MURMUR_HASH);
			
			log.info("build jedisPool_slave...");
			jedisPool_slave = new ShardedJedisPool(config, slave_shards, Hashing.MURMUR_HASH);
		} catch (Exception ex) {
			log.error("redisPoolInit failed#", ex);
		}
	}
	
	/**
	 * @Title: buildJedisShard
	 * @Description: 根据参数列表，构建shardInfo List
	 * @author liu_yi
	 * @param hosts hosts列表
	 * @param ports port列表
	 * @param shardName 分片名
	 * @return
	 * @throws
	 */
	public static List<JedisShardInfo> buildJedisShard(String[] hosts, String[] ports) {
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
		
		// 这是个固定名字，否则主从不一致
		String shardName = "FIXEDNAME";
		
		for (int i = 0; i != hosts.length; i++) {
			String tempHost = hosts[i];
			for (int j =  0; j != ports.length; j++) {
				String tempPort = ports[j];
				int tempPortsNum = Integer.valueOf(tempPort);
				
				String tempJedisShardNodeName = shardName + "_" + tempHost + "_addFixedPort" + j;
				JedisShardInfo tempJedisShardNode = new JedisShardInfo(tempHost, tempPortsNum, tempJedisShardNodeName);
				shards.add(tempJedisShardNode);
			}
		}
		
		return shards;
	}

	public static ShardedJedisPool getJedisPoolMaster() {
		 if (jedisPool_master == null) {
	            redisPoolInit();
	     }
		 
		 return jedisPool_master;
	}
	
	public static ShardedJedisPool getJedisPoolSlave() {
		 if (jedisPool_slave == null) {
	            redisPoolInit();
	     }
		 
		 return jedisPool_slave;
	}
	
	public static ShardedJedis getShardedJedis(String masterORslave) {
		assert ! lockJedis.isHeldByCurrentThread();  
		lockJedis.lock();
		ShardedJedisPool sjp = null;
    	if ("master".equals(masterORslave)) {
    		sjp = getJedisPoolMaster();
    	} 
    	
    	if ("slave".equals(masterORslave)) {
    		sjp = getJedisPoolSlave();
    	}
    	
        if (sjp == null) {
            redisPoolInit();
        }

        // 再检测一次看是否为空(可能初始化失败)
        if (sjp == null) {
            log.error("can not get jedis instance, init again..");
            redisPoolInit();
        }
        
        // 还是失败了
        if (sjp == null) {
        	log.error("can not get jedis instance");
        	return null;
        }
        
		ShardedJedis shardJedis = null;

		try {
			shardJedis = sjp.getResource();
		} catch (Exception e) {
			log.error("got an exception:", e);
			
		} finally{  
          returnResource(shardJedis, sjp);  
          lockJedis.unlock();  
        }  
		
		return shardJedis;
	}
	
	 /** 
     * 释放jedis资源 
     * @param jedis 
     */  
    public static void returnResource(final ShardedJedis jedis, ShardedJedisPool sjp) {  
        if (jedis != null && sjp !=null) {  
        	sjp.returnResourceObject(jedis);
        }  
    }  
	
    /**
     * @Title: getJedisClient
     * @Description: 获得jedis客户端
     * @author liu_yi
     * @return
     * @throws
     */
    public synchronized static ShardedJedis getJedisClient(String masterORslave) {
    	ShardedJedisPool sjp = null;
    	if ("master".equals(masterORslave)) {
    		sjp = getJedisPoolMaster();
    	} 
    	
    	if ("slave".equals(masterORslave)) {
    		sjp = getJedisPoolSlave();
    	}
    	
        if (sjp == null) {
            redisPoolInit();
        }

        // 再检测一次看是否为空(可能初始化失败)
        if (sjp == null) {
            log.error("can not get jedis instance, init again..");
            redisPoolInit();
        }
        
        // 还是失败了
        if (sjp == null) {
        	log.error("can not get jedis instance");
        	return null;
        }
        
		ShardedJedis shardJedis = null;

		try {
			shardJedis = sjp.getResource();
		} catch (Exception e) {
			log.error("got an exception:", e);
		}
		
		return shardJedis;
    }
    
    public static void main(String[] args){
    	String uid = "A0000059A2230D";
    	ShardedJedisPool jedispool = getJedisPoolSlave();
		Map<String,String> res = null;
		ShardedJedis sjedis = null;
		Jedis jedis = null;
		boolean borrowOrOprSuccess = true;
		try{
			sjedis = jedispool.getResource();
			Collection<Jedis> js = sjedis.getAllShards();
			Iterator<Jedis> it = js.iterator(); 
	    	while(it.hasNext()){  
	    		jedis = it.next();
	    		jedis.select(1);
	    		if(jedis.hexists(uid, "t1"))
	    			break;
	    	}
	    	res = jedis.hgetAll(uid);
		}catch(Exception e){
			borrowOrOprSuccess = false;
			log.error("get tags from redis error for uid ",e);
			userShardedRedisUtil.returnResource(sjedis, jedispool);
		}finally{
			if(borrowOrOprSuccess == true)
				userShardedRedisUtil.returnResource(sjedis, jedispool);
		}
		for(String k : res.keySet()){
			System.out.println(k + ":"+res.get(k));
		}
    }
}
