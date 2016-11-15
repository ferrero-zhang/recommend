package com.ifeng.iRecommend.likun.userCenter.tnappuc.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.core.RBatch;
import org.redisson.core.RMapAsync;

public class RedissionUtil {
	private static final Log log = LogFactory.getLog(RedissionUtil.class);
	
	private static RedissonClient get(){
		Config config = new Config();
		config.useClusterServers().addNodeAddress("","","","","");
		RedissonClient redisson = Redisson.create(config);
		return redisson;
	}

}
