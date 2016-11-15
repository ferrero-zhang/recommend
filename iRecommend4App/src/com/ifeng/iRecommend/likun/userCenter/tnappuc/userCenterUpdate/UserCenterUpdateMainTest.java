package com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.kedm.util.UserlogIKVOperation;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userinfomodel.UserCenterModel;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.GlobalParams;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.userShardedRedisUtil;

public class UserCenterUpdateMainTest {
	private static Set<String> days_keysSet = new HashSet<String>();//用户的日期段keys
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//准备好90 day key，每个用户需要取90天的用户记录
		long end = System.currentTimeMillis();
		long start = end - 90*24*3600*1000L;
		
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		Calendar calS = Calendar.getInstance();
		Calendar calE = Calendar.getInstance();

		try{
			//遍历组合一个用户的所有日期段的keys，批量查询ikv
			calS.setTimeInMillis(start);
			calE.setTimeInMillis(end);
			while(calS.compareTo(calE) <= 0){
				String date = df.format(calS.getTime());
				if(date != null){
					days_keysSet.add(date);
				}
				calS.set(Calendar.DATE, calS.get(Calendar.DATE)+1);
			}
		}catch(Exception e){
			System.out.println("init 90 days key error:");
			e.printStackTrace();
		}
	}

	@Ignore
	public void testUpdateUserCenterInfo() {

		Set<String> allUpdatedUserSet = new HashSet<String>();
		allUpdatedUserSet.add("");
		allUpdatedUserSet.add("nobody");
		allUpdatedUserSet.add("A000004206BD05");
		allUpdatedUserSet.add("865863026995352");
		//用户中心集群shardedJedis
		ShardedJedisPool sjp = userShardedRedisUtil.getJedisPoolMaster();
		ShardedJedis sj = sjp.getResource();
		
		Collection<Jedis> js = sj.getAllShards();  
    	Iterator<Jedis> it = js.iterator(); 
    	while(it.hasNext()){  
    		Jedis j=it.next();
    		j.select(GlobalParams.userCenterDBnum);  
    	}
    	    	
		ShardedJedisPipeline pipeline = sj.pipelined();
		
		queryItemFeatures qif = new queryItemFeatures();
		
		// debug信息，看下单个特征词在计算中的各种值
		String debugFeature = "";
		// 更新所有有新的访问行为的用户中心模型
		int tmp_num = 1;//计数器，控制pipeline的长短
		for (String tempUid : allUpdatedUserSet) {
			UserCenterUpdateMain.updateUserCenterInfo(qif,tempUid, debugFeature,pipeline,7,UserlogIKVOperation.getIKVClient(),days_keysSet);
			tmp_num++;
			if(tmp_num >= 3000){
				pipeline.sync();
				tmp_num = 1;
			}
		}
		
		pipeline.sync();
		
		//get	
		for (String tempUid : allUpdatedUserSet) {
			Map<String, String> userModel = sj.hgetAll(tempUid);
			System.out.println(tempUid+"\r\n");
			System.out.println(JsonUtils.toJson(userModel));
		}
		
		
		
		
		sjp.returnResourceObject(sj);
		sj.disconnect();
		
		
		
		
	}

	@Test
	public void testGetUserTopicInfo() {
		
		System.out.println("8d27bdb0da886aadd059f39157ed2d3250d07179");
		Map<String, String> mss = UserCenterUpdateMain
				.getUserLogInfo("8d27bdb0da886aadd059f39157ed2d3250d07179",60,UserlogIKVOperation.getIKVClient(),days_keysSet);
		System.out.println(mss.toString());
		
		queryItemFeatures qif = new queryItemFeatures();
		
		Map<String, String> m_topicinfo = UserCenterUpdateMain
				.getUserTopicInfo(qif,mss, "");
		// 新建用户中心中的用户模型
		String topic1_top_value = m_topicinfo.get("topic1_top_value");
		String topic2_top_value = m_topicinfo.get("topic2_top_value");
		String topic3_top_value = m_topicinfo.get("topic3_top_value");
		String topic1_lastview = m_topicinfo.get("topic1_lastview");
		String topic2_lastview = m_topicinfo.get("topic2_lastview");
		String topic3_lastview = m_topicinfo.get("topic3_lastview");
		
		System.out.println("topic1_top_value:"+topic1_top_value);
		System.out.println("topic2_top_value:"+topic2_top_value);
		System.out.println("topic3_top_value:"+topic3_top_value);
		System.out.println("topic1_lastview:"+topic1_lastview);
		System.out.println("topic2_lastview:"+topic2_lastview);
		System.out.println("topic3_lastview:"+topic3_lastview);
		System.out.println("////////////////////////////////");
		
		//commenFuncs.writeResult("", "test_shuz", topic1_top_value+"\n"+topic3_top_value+"\n"+topic2_top_value, "utf-8", true, null);
		System.out.println(topic1_top_value+"\n"+topic3_top_value+"\n"+topic2_top_value);
		
//	 	//////////////////////////////////////////////////////
////		System.out.println("nobody:");
////		mss = UserCenterUpdateMain
////				.getUserLogInfo("nobody",30);
////		System.out.println(mss.toString());
////		
////		m_topicinfo = UserCenterUpdateMain
////				.getUserTopicInfo(qif,mss, "");
////		if(m_topicinfo != null)
////		{
////			// 新建用户中心中的用户模型
////			topic1_top_value = m_topicinfo.get("topic1_top_value");
////			topic2_top_value = m_topicinfo.get("topic2_top_value");
////			topic3_top_value = m_topicinfo.get("topic3_top_value");
////			topic1_lastview = m_topicinfo.get("topic1_lastview");
////			topic2_lastview = m_topicinfo.get("topic2_lastview");
////			topic3_lastview = m_topicinfo.get("topic3_lastview");
////			
////			System.out.println("topic1_top_value:"+topic1_top_value);
////			System.out.println("topic2_top_value:"+topic2_top_value);
////			System.out.println("topic3_top_value:"+topic3_top_value);
////			System.out.println("topic1_lastview:"+topic1_lastview);
////			System.out.println("topic2_lastview:"+topic2_lastview);
////			System.out.println("topic3_lastview:"+topic3_lastview);
////		}
////		System.out.println("////////////////////////////////");	
//		//////////////////////////////////////////////////////
//		System.out.println("A000004206BD05:");
//		mss = UserCenterUpdateMain
//		.getUserLogInfo("A000004206BD05",200);
//		System.out.println(mss.toString());
//		
//		m_topicinfo = UserCenterUpdateMain
//		.getUserTopicInfo(qif,mss, "");
//		if(m_topicinfo != null)
//		{
//		// 新建用户中心中的用户模型
//		topic1_top_value = m_topicinfo.get("topic1_top_value");
//		topic2_top_value = m_topicinfo.get("topic2_top_value");
//		topic3_top_value = m_topicinfo.get("topic3_top_value");
//		topic1_lastview = m_topicinfo.get("topic1_lastview");
//		topic2_lastview = m_topicinfo.get("topic2_lastview");
//		topic3_lastview = m_topicinfo.get("topic3_lastview");
//		
//		System.out.println("topic1_top_value:"+topic1_top_value);
//		System.out.println("topic2_top_value:"+topic2_top_value);
//		System.out.println("topic3_top_value:"+topic3_top_value);
//		System.out.println("topic1_lastview:"+topic1_lastview);
//		System.out.println("topic2_lastview:"+topic2_lastview);
//		System.out.println("topic3_lastview:"+topic3_lastview);
//		}
//		System.out.println("////////////////////////////////");
//		//////////////////////////////////////////////////////
//		System.out.println(":");
//		mss = UserCenterUpdateMain
//				.getUserLogInfo("",200);
//		if(mss != null)
//			System.out.println(mss.toString());
//		else
//			System.out.println("mss = null");
//		
//		m_topicinfo = UserCenterUpdateMain
//				.getUserTopicInfo(qif,mss, "");
//		if(m_topicinfo != null)
//		{
//			// 新建用户中心中的用户模型
//			topic1_top_value = m_topicinfo.get("topic1_top_value");
//			topic2_top_value = m_topicinfo.get("topic2_top_value");
//			topic3_top_value = m_topicinfo.get("topic3_top_value");
//			topic1_lastview = m_topicinfo.get("topic1_lastview");
//			topic2_lastview = m_topicinfo.get("topic2_lastview");
//			topic3_lastview = m_topicinfo.get("topic3_lastview");
//			
//			System.out.println("topic1_top_value:"+topic1_top_value);
//			System.out.println("topic2_top_value:"+topic2_top_value);
//			System.out.println("topic3_top_value:"+topic3_top_value);
//			System.out.println("topic1_lastview:"+topic1_lastview);
//			System.out.println("topic2_lastview:"+topic2_lastview);
//			System.out.println("topic3_lastview:"+topic3_lastview);
//		}
//		System.out.println("////////////////////////////////");
//		//////////////////////////////////////////////////////
//		System.out.println("865586021345899:");
//		mss = UserCenterUpdateMain
//		.getUserLogInfo("865586021345899",200);
//		System.out.println(mss.toString());
//		
//		m_topicinfo = UserCenterUpdateMain
//				
//		.getUserTopicInfo(qif,mss, "");
//		if(m_topicinfo != null)
//		{
//			// 新建用户中心中的用户模型
//			topic1_top_value = m_topicinfo.get("topic1_top_value");
//			topic2_top_value = m_topicinfo.get("topic2_top_value");
//			topic3_top_value = m_topicinfo.get("topic3_top_value");
//			topic1_lastview = m_topicinfo.get("topic1_lastview");
//			topic2_lastview = m_topicinfo.get("topic2_lastview");
//			topic3_lastview = m_topicinfo.get("topic3_lastview");
//			
//			System.out.println("topic1_top_value:"+topic1_top_value);
//			System.out.println("topic2_top_value:"+topic2_top_value);
//			System.out.println("topic3_top_value:"+topic3_top_value);
//			System.out.println("topic1_lastview:"+topic1_lastview);
//			System.out.println("topic2_lastview:"+topic2_lastview);
//			System.out.println("topic3_lastview:"+topic3_lastview);
//		}
//		System.out.println("////////////////////////////////");
//		
//	
//		//////////////////////////////////////////////////////
//		System.out.println("865863026995352:");
//		mss = UserCenterUpdateMain
//		.getUserLogInfo("865863026995352",200);
//		System.out.println(mss.toString());
//		
//		m_topicinfo = UserCenterUpdateMain
//		.getUserTopicInfo(qif,mss, "");
//		if(m_topicinfo != null)
//		{
//			// 新建用户中心中的用户模型
//			topic1_top_value = m_topicinfo.get("topic1_top_value");
//			topic2_top_value = m_topicinfo.get("topic2_top_value");
//			topic3_top_value = m_topicinfo.get("topic3_top_value");
//			topic1_lastview = m_topicinfo.get("topic1_lastview");
//			topic2_lastview = m_topicinfo.get("topic2_lastview");
//			topic3_lastview = m_topicinfo.get("topic3_lastview");
//			
//			System.out.println("topic1_top_value:"+topic1_top_value);
//			System.out.println("topic2_top_value:"+topic2_top_value);
//			System.out.println("topic3_top_value:"+topic3_top_value);
//			System.out.println("topic1_lastview:"+topic1_lastview);
//			System.out.println("topic2_lastview:"+topic2_lastview);
//			System.out.println("topic3_lastview:"+topic3_lastview);
//		}
//		System.out.println("////////////////////////////////");
//		//////////////////////////////////////////////////////
//		System.out.println("867101020344568:");
//		mss = UserCenterUpdateMain
//		.getUserLogInfo("867101020344568",200);
//		System.out.println(mss.toString());
//		
//		m_topicinfo = UserCenterUpdateMain.getUserTopicInfo(qif,mss, "");
//		if(m_topicinfo != null)
//		{
//			// 新建用户中心中的用户模型
//			topic1_top_value = m_topicinfo.get("topic1_top_value");
//			topic2_top_value = m_topicinfo.get("topic2_top_value");
//			topic3_top_value = m_topicinfo.get("topic3_top_value");
//			topic1_lastview = m_topicinfo.get("topic1_lastview");
//			topic2_lastview = m_topicinfo.get("topic2_lastview");
//			topic3_lastview = m_topicinfo.get("topic3_lastview");
//			
//			System.out.println("topic1_top_value:"+topic1_top_value);
//			System.out.println("topic2_top_value:"+topic2_top_value);
//			System.out.println("topic3_top_value:"+topic3_top_value);
//			System.out.println("topic1_lastview:"+topic1_lastview);
//			System.out.println("topic2_lastview:"+topic2_lastview);
//			System.out.println("topic3_lastview:"+topic3_lastview);
//		}
//		System.out.println("////////////////////////////////");
	
	}

	@Test
	public void testGetSortBookInfo() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetLatestInfo() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetTopWord() {
		//fail("Not yet implemented");
	}

	@Ignore
	public void testGetUpdateUserID() {
		ArrayList<String> set_users = UserCenterUpdateMain.getUpdateUserID();
		System.out.println("users in redis:"+set_users.size());
	}

	@Ignore
	public void testGetUserLogInfo() {
		Map<String, String> mss = UserCenterUpdateMain.getUserLogInfo("A000004206BD05",7,UserlogIKVOperation.getIKVClient(),days_keysSet);
		System.out.println(mss.toString());
		
		ArrayList<String> set_users = UserCenterUpdateMain.getUpdateUserID();
		System.out.println("users in redis:"+set_users.size());
		int num = 100;
		for(String userid:set_users){
			mss = UserCenterUpdateMain.getUserLogInfo(userid,7,UserlogIKVOperation.getIKVClient(),days_keysSet);
			System.out.println(userid);
			try{
				System.out.println(mss.toString());
			}catch(Exception e){
				
			}
			num--;
			if(num <= 0)
				break;
		}
		
	}

}
