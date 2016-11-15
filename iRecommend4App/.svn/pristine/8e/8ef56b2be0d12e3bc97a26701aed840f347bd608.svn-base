package com.ifeng.iRecommend.front.recommend2;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation;

public class test {
	/*
	 * 临时函数：得到redis before端链接;
	 * (不严格)
	 */
	protected static Jedis getRedisBefore() {
		String ip = "",port = "";
		String timeOut = "";
		int iport = 0;
		try{
			ip = LoadConfig.lookUpValueByKey("redisserverip_before");
			port = LoadConfig.lookUpValueByKey("redisserverport_before");
			timeOut = LoadConfig.lookUpValueByKey("redistimeout_before");
			iport = Integer.valueOf(port);
		}catch(Exception e){
			e.printStackTrace();
			if(timeOut == null || timeOut.isEmpty())
				timeOut = "5000";
			//退出
			return null;
		}
		
		Jedis redis_before = null;
		// TODO Auto-generated method stub
		try {
			redis_before  = new Jedis(ip,iport);
			redis_before.select(fieldDicts.userList_dbid_in_redis);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		return redis_before;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		FileUtil usersFile = new FileUtil();
		usersFile.Initialize("D:\\sz_rz\\1350289381468_ktoo4j2491", "UTF-8");
		String line = null;
		System.out.println("start:");
		HashMap<String,Integer> hm_tmp = new HashMap<String,Integer>();
		while ((line = usersFile.ReadLine()) != null) {
			String termline = line.trim();
			String[] terms = termline.split("\\s");
			for(String term:terms){
				Integer num = hm_tmp.get(term);
				if(num == null)
					num = 1;
				else
					num++;
				hm_tmp.put(term, num);
			}
		}
		
		//sort
		ArrayList<Entry<String, Integer>> infoIds =
			    new ArrayList<Entry<String, Integer>>(hm_tmp.entrySet());
		Collections.sort(infoIds, new Comparator<Entry<String, Integer>>() {   
		    public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {      
		        //return (o2.getValue() - o1.getValue()); 
		        return (o2.getValue() - o1.getValue());
		    }
		}); 
		
		
		String txt = "";
		for(Entry<String, Integer> et:infoIds){
			txt = txt+et.getKey()+"|"+et.getValue()+" ";
			if(txt.length()>1024)
			{
				commenFuncs.writeResult("D:\\sz_rz\\", "1350289381468_ktoo4j2491.vector.txt", txt+"\r\n", "utf-8", true, null);
				txt = "";
			}
		}
		commenFuncs.writeResult("D:\\sz_rz\\", "1350289381468_ktoo4j2491.vector.txt", txt, "utf-8", true, null);
		txt = "";//		FileUtil usersFile = new FileUtil();
//		usersFile.Initialize("D:\\sz_rz\\clean_cookies", "UTF-8");
//		String line = null;
//		int num = 0;
//		System.out.println("start:");
//		HashSet<String> hs_cleanCookies = new HashSet<String>();
//		while ((line = usersFile.ReadLine()) != null) {
//			String userid = line.trim();
//			num++;
//			hs_cleanCookies.add(userid);
//		}
//	
//		//从前端redis推荐库中去除这些user的推荐内容
//		Jedis redis_before = getRedisBefore();
//		for(String userid:hs_cleanCookies){
//			try {
//				redis_before.del(userid);
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}
	
		
//		usersFile = new FileUtil();
//		usersFile.Initialize("D:\\sz_rz\\testusers", "UTF-8");
//		System.out.println("start:");
//		HashSet<String> hs_allusers = new HashSet<String>();
//		while ((line = usersFile.ReadLine()) != null) {
//			String userid = line.trim();
//			if(hs_cleanCookies.contains(userid)){
//				System.out.println("clean:"+userid);
//			}else{
//				hs_allusers.add(userid);
//				
//			}
//		}
//		System.out.println("new size:"+hs_allusers.size());
//		StringBuffer sbTmp = new StringBuffer();
//		for(String userid1:hs_allusers){
//			sbTmp.append(userid1).append("\r\n");
//			if(sbTmp.length() > 8196){
//				commenFuncs.writeResult("D:\\sz_rz\\", "new_testusers", sbTmp.toString(), "utf-8", true, null);
//				sbTmp.delete(0, sbTmp.length());
//			}
//		}
//		commenFuncs.writeResult("D:\\sz_rz\\", "new_testusers", sbTmp.toString(), "utf-8", true, null);
//		// 读入test users
//		FileUtil usersFile = new FileUtil();
//		usersFile.Initialize("D:\\sz_rz\\testusers_aaa", "UTF-8");
//		String line = usersFile.ReadLine();
//		String[] secs = line.split("\\s");
//		if (secs.length != 2 || secs[0].length() > 1)
//			return;
//
//		ArrayList<String> al_users = new ArrayList<String>();
//		while ((line = usersFile.ReadLine()) != null) {
//			String userid = line.trim();
//			al_users.add(userid);
//		}
//		
//		int allUsersNum = al_users.size();
//		System.out.println(allUsersNum);
//		int threadsNum = 10;
//		final CountDownLatch cdl = new CountDownLatch(threadsNum);
//		for (int i = 0; i < threadsNum; ++i) {
//			//计算最恰当的间隔长度
//			int interNum = allUsersNum / threadsNum;
//			if(allUsersNum%threadsNum != 0)
//				interNum++;
//			
//			final int begin = (interNum) * i;
//			final int end = allUsersNum < (begin + interNum) ? allUsersNum
//					: (begin + interNum);
//			//LOG.info("interNum:"+interNum+" begin:"+begin+" end:"+end);
//			
//			final ArrayList<String> al_someUsers = new ArrayList<String>();
//			for (int k = begin; k < end; k++)
//				al_someUsers.add(al_users.get(k));
//			
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					int num=0,finishedNum = 0;
//					for(String userid:al_someUsers)
//					{
//						//test
//						if(finishedNum%1000 == 0)
//							System.out.println(Thread.currentThread().getId()+" finish:"+finishedNum+" userid="+userid);
//						
//						HashMap<String, String> logs = logDBOperation.queryByUsrID(userid);
//						String info = logs.entrySet().toString();
//						//System.out.println(info);
//						if(info.indexOf("210.51.19.2")>=0
//								||info.indexOf("223.203.209.7")>=0
//								||info.indexOf("220.181.67.203")>=0)
//						{
//							commenFuncs.writeResult("D:\\sz_rz\\", "static_coworker_cookie", userid+"\r\n"+info+"\r\n", "utf-8", true, null);
//							num++;
//						}
//						finishedNum++;
//					}
//					System.out.println(num);
//					cdl.countDown();
//				}
//
//
//			}).start();
//
//		}
//		
//		try {
//			cdl.await();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println("all done");
	}

}
