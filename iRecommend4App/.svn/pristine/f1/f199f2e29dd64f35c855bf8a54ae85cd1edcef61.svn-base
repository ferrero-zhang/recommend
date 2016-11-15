package com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userinfomodel.UserCenterModel;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.GlobalParams;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.userShardedRedisUtil;

public class UserCenterModel2RedisTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testUcModelUpdate2Redis() {
		Set<String> allUpdatedUserSet = new HashSet<String>();
		allUpdatedUserSet.add("");
		allUpdatedUserSet.add("nobody");
		allUpdatedUserSet.add("860241034247360");
		allUpdatedUserSet.add("351564060599867");
		allUpdatedUserSet.add("9d62db556b71181a48dfd310185fe6935cf47a90");
		allUpdatedUserSet.add("A000005593CF2B");
		allUpdatedUserSet.add("352324079342620");
		allUpdatedUserSet.add("A000003859134F");
		allUpdatedUserSet.add("b059f8fc97b185c02a2e6ae894de1421");
		allUpdatedUserSet.add("860991024074667");
		allUpdatedUserSet.add("358858055241482");
		allUpdatedUserSet.add("0c75312f2be2b07ed503bb6b6b8bc67602fc7b9b");
		allUpdatedUserSet.add("865293021447219");
		allUpdatedUserSet.add("9aeeb890c51428382eb1252b26ec56881b7e56bb");
		allUpdatedUserSet.add("863811012923211");
		allUpdatedUserSet.add("865644020702246");
		allUpdatedUserSet.add("fbc91801fda2e78455774e177ae3726e");
		allUpdatedUserSet.add("8d27bdb0da886aadd059f39157ed2d3250d07179");
		//用户中心集群shardedJedis
		ShardedJedisPool sjp = userShardedRedisUtil.getJedisPoolMaster();
		ShardedJedis sj = sjp.getResource();

		Collection<Jedis> js = sj.getAllShards();
		Iterator<Jedis> it = js.iterator();
		while (it.hasNext()) {
			Jedis j = it.next();
			j.select(GlobalParams.userCenterDBnum);
		}

		// get
		for (String tempUid : allUpdatedUserSet) {
			Map<String, String> userModel = sj.hgetAll(tempUid);
			System.out.println(tempUid);
			System.out.println(JsonUtils.toJson(userModel));
		}
	}

}
