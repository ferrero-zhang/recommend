package com.ifeng.iRecommend.likun.userCenter.tnappuc.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.ClusterPipeline;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

public class UserRedisClusterUtil {
	private static final Log log = LogFactory.getLog(UserRedisClusterUtil.class);
	private static PropertiesConfiguration REDIS_CONFIG;
	private static Set<HostAndPort> hps = new HashSet<HostAndPort>();

	/*private static String redisConfigFilePath = "./conf/redis.properties";
	
    static {
		REDIS_CONFIG = null;
		try {
			REDIS_CONFIG = new PropertiesConfiguration(redisConfigFilePath);
		} catch (ConfigurationException ex) {
			log.info("can't create Redis_Config", ex);
		}
    }*/
    
    private static void redisClusterInit(){
    	try{
        	HostAndPort hp0 = new HostAndPort("10.50.8.73",6379);
        	HostAndPort hp1 = new HostAndPort("10.50.8.74",6379);
        	HostAndPort hp2 = new HostAndPort("10.50.8.75",6379);
        	HostAndPort hp3 = new HostAndPort("10.50.8.76",6379);
        	HostAndPort hp4 = new HostAndPort("10.50.8.77",6379);
        	hps.add(hp0);
        	hps.add(hp1);
        	hps.add(hp2);
        	hps.add(hp3);
        	hps.add(hp4);
        	
    	}catch(Exception e){
    		log.error("inti redis cluster error",e);
    	}
    	
    }
    public synchronized static JedisCluster getJedisCluster(){
    	if(hps == null || hps.isEmpty()){
    		redisClusterInit();
    	}
    	JedisCluster jedisCluster = null;
    	try{
    		jedisCluster = new JedisCluster(hps);
    	}catch(Exception e){
    		log.error("get jedisCluster error ",e);
    		e.printStackTrace();
    	}		
    	return jedisCluster;
    }

}
