/**
 * 
 */
package com.ifeng.iRecommend.train;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.UrlPretreatment;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation.LogType;
import com.ifeng.iRecommend.usermodel.usermodelInterfaceSolr;

/**
 * <PRE>
 * 作用 :
 *   solr版本
 * 	   兼容PC端和APP端两种情况； 
 *   负责定时训练，催动用户模型更新；定时执行（一周一次？）
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   用户模型更新，涉及到硬盘上位置的切换以及对模型层的通知；
 *   【非必须前提】训练任务的前提是最好能重新构建topicmodel;
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2013-7-17        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class usermodelTrainingSolr {
	private static final Log LOG = LogFactory.getLog("train");
	

	
	/*
	 * 读入所有user,来自hdfs
	 */
	public static ArrayList<String> readAllTestUsers(int beginPoint,int endPoint) throws IOException{
		//HDFS文件读取
		Configuration conf = null;
		FileSystem hdfs = null;
		conf = new Configuration();
		conf.set("fs.defaultFS", "hdfs://10.32.21.111:8020/");//指定hdfs
		conf.set("hadoop.job.user", "hdfs");
		hdfs = FileSystem.get(conf);
		Path path = new Path(fieldDicts.testUsersFile);
		FSDataInputStream fsr = hdfs.open(path);
		BufferedReader bis = new BufferedReader(new InputStreamReader(fsr,
				"utf-8"));
		String line = null;
		ArrayList<String> al_users = new ArrayList<String>(6000);
		int num = 0;
		while ((line=bis.readLine())!= null) {
			String[] secs = line.split("\t");
			if(secs.length != 2)
				continue;
			num++;
			try{
				String userID = secs[0].trim();
				int live_days = Integer.valueOf(secs[1].trim());
//				//小于minDays的不算
//				if(live_days < minDays)
//					continue;
//				//小于minDays的不算
//				if(live_days > maxDays)
//					continue;
				
				if(num >= beginPoint && num <= endPoint)
					al_users.add(new String(userID));
				
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
		}
		return al_users;
// old		
//		//读入test users
//		HashSet s_testusers = new HashSet<String>();
//		FileUtil wordidFile = new FileUtil();
//		wordidFile.Initialize(fieldDicts.testUsersFile, "UTF-8");
//		String line = "";
//		while ((line = wordidFile.ReadLine()) != null) {
//			s_testusers.add(line.trim());
//		}
//		String[] ss_users = (String[])s_testusers.toArray(new String[0]);
//		return ss_users;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub	
		////////////////////////////////////////////////////////////////////////////
//		//test
//		fieldDicts.modelPath = "D:/workspace/iRecommend4App/testenv/usermodel/";
//		fieldDicts.appTreeMappingFile = "D:/workspace/iRecommend4App/testenv/AppTreeMapping.txt";
//		fieldDicts.frontAppTreeMappingFile = "D:/workspace/iRecommend4App/testenv/Front_TreeMapping.txt";
//		fieldDicts.stopwordsFile = "D:/workspace/iRecommend4App/testenv/stopwords.txt";
//		fieldDicts.tm_doc_dir = "D:\\workspace\\iRecommend4App\\testenv\\tm\\doc\\";
//		fieldDicts.tm_word_dir = "D:\\workspace\\iRecommend4App\\testenv\\tm\\word\\";
//		fieldDicts.tm_words_file = "D:\\workspace\\iRecommend4App\\testenv\\tm\\dict_topicmodel";
//		fieldDicts.testUsersFile = "/projects/zhineng/publishId/5281id";

		fieldDicts.itemModelPath = "/data/irecommend4app/itemmodel/";
		fieldDicts.modelPath = "/data/irecommend4app/usermodel/";
		fieldDicts.appTreeMappingFile = "/data/irecommend4app/AppTreeMapping.txt";
		fieldDicts.frontAppTreeMappingFile = "/data/irecommend4app/APPFront_TreeMapping.txt";
		fieldDicts.stopwordsFile = "/data/irecommend4app/stopwords.txt";
		fieldDicts.tm_doc_dir = "/data/irecommend4app/tm/doc/";
		fieldDicts.tm_word_dir = "/data/irecommend4app/tm/word/";
		fieldDicts.tm_words_file = "/data/irecommend4app/tm/dict_topicmodel";
		fieldDicts.testUsersFile = "/projects/zhineng/publishId/5281id";//"/data/irecommend4app/appActiveUsers_5w.txt";
		
		String TRAINMODE = "all";
		
		//限定在hbase中是newsapp的log和item table
		if(args[4].equals("new"))
		{
			logDBOperation.setLogType(logDBOperation.LogType.APPLOGREALTIME);
			TRAINMODE = "new";
		}else if(args[4].equals("more"))
		{
			logDBOperation.setLogType(logDBOperation.LogType.APPLOGREALTIME);
			TRAINMODE = "more";
		}else
			logDBOperation.setLogType(logDBOperation.LogType.APPLOGREALTIME);
		
		//用户取全量的最大天数，用户如果实际访问天数小于minDays,那么取更大天数maxDays2的记录；
		int maxDays = 7,minDays = 3,maxDays2 = 14;
		maxDays = Integer.valueOf(args[5]);
		minDays = Integer.valueOf(args[6]);
		maxDays2 = Integer.valueOf(args[7]);
		
		String deviceType = args[8];
		
		//1.读入测试user list；
		//2.遍历测试user list，逐个user查询其3个月内的用户行为，调用模型层的建模接口建模；
		//如果发现training状态是“停止后续建模”，则退出建模步骤；
		//3.建模完毕，得到新模型；旧模型被替换，并硬盘标记，通知模型层载入新模型；
		LOG.info("read all test users");
		//读入user list
		ArrayList<String> al_testusers = null;
		try {
			fieldDicts.testUsersFile = String.valueOf(args[1]);
			int beginPoint = Integer.valueOf(args[2]);
			int endPoint = Integer.valueOf(args[3]);
			al_testusers = readAllTestUsers(beginPoint,endPoint);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			LOG.error("al_testusers error:",e1);
			return;
		}
		
		LOG.info("all test users:"+al_testusers.size());
		
	
		
		// 分组，多线程进行建模
		int allUsersNum = al_testusers.size();
		int threadsNum = Integer.valueOf(args[0]);
		final CountDownLatch cdl = new CountDownLatch(threadsNum);
		for (int i = 0; i < threadsNum; ++i) {
			// 计算最恰当的间隔长度
			int interNum = allUsersNum / threadsNum;
			if (allUsersNum % threadsNum != 0)
				interNum++;

			final int begin = (interNum) * i;
			final int end = allUsersNum < (begin + interNum) ? allUsersNum
					: (begin + interNum);
			final String[] someUsers = new String[end - begin];
			for (int k = begin; k < end; k++)
				someUsers[k - begin] = al_testusers.get(k);
			//训练模式
			final String TRAINMODE_banch = TRAINMODE;
			
			final int maxDays_tmp = maxDays;
			final int minDays_tmp = minDays;
			final int maxDays2_tmp = maxDays2;
			final String deviceType_tmp = deviceType;
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						
						ItemOperation itemOP = ItemOperation.getInstance();
						itemOP.setItemType(ItemOperation.ItemType.APPITEM);
						int num = 0;
						usermodelInterfaceSolr uifs = new usermodelInterfaceSolr();
						// user缓存池，放入一批user，后续集中入库
						HashMap<String, HashMap<String, String>> hm_user2items = new HashMap<String, HashMap<String, String>>(
								96);
						for (int j = 0; j < someUsers.length; j++) {
							String userID = someUsers[j];
							// 集中一批，一起入库，速度比较快
							if (96 == hm_user2items.size()) {
								num += hm_user2items.size();
								String rt = uifs.modelSomeUsers(hm_user2items, itemOP,TRAINMODE_banch,deviceType_tmp);
								if (rt == null) {
									LOG.warn("modelSomeUsers,rt == null");
								}
								LOG.info("finish:" + num);
								//@test;写出具体的ID
								//...
								
								hm_user2items.clear();

								if (num % 96 == 0)
									LOG.info("finish:" + num);
							}

							// 查询得到此user最近7天的历史行为
							HashMap<String, String> hm_log = null;
							if(TRAINMODE_banch.equals("all")){
//								hm_log = logDBOperation.queryUserIDInDateRange(userID,String.valueOf(System
//												.currentTimeMillis()), maxDays_tmp);
//								// 如果记录太少,查询更多天的item
//								if (hm_log == null || hm_log.size() < minDays_tmp)
//									hm_log = logDBOperation.queryUserIDInDateRange(
//											userID, String.valueOf(System
//													.currentTimeMillis()), maxDays2_tmp);
								//此处特别指明tablename，避免并发互斥之错之慢;但是代码显得可读性降低；
								hm_log = logDBOperation.queryUserIDInDateRange(
												fieldDicts.appUserLogTableNameInHbase,
												userID,
												String.valueOf(System
														.currentTimeMillis()),
														maxDays_tmp);
								// 如果记录太少,查询更多天的item
								if (hm_log == null || hm_log.size() < minDays_tmp)
									hm_log = logDBOperation
											.queryUserIDInDateRange(
													fieldDicts.appUserLogTableNameInHbase,
													userID,
													String.valueOf(System
															.currentTimeMillis()),
															maxDays2_tmp);
								
								//特殊处理，如果是测试人员的账号，比如：863151026712833（战志恒）,只取最近1天的记录
								if(userID.equals("863151026712833")){
									hm_log = logDBOperation.queryUserIDInDateRange(
											fieldDicts.appUserLogTableNameInHbase,
											userID,
											String.valueOf(System
													.currentTimeMillis()),
													7);
								}
							}
						
							if(TRAINMODE_banch.equals("more")){
								hm_log = logDBOperation
											.queryUserIDInDateRange(userID,
													String.valueOf(System
															.currentTimeMillis()), maxDays_tmp);		
								//查询历史user model，如果不存在那么就是新用户
								String userString = usermodelInterfaceSolr.queryUserStringFromSolr(userID);
								//历史上为空，是不是考虑进行全量训练？还是直接写入最近？
								if (userString != null
										&& userString.equals("cold"))// 完全是一个新用户
								{
									LOG.warn("user is new:" + userID);
									//此处特别指明tablename，避免并发互斥之错之慢;但是代码显得可读性降低；
									hm_log = logDBOperation.queryUserIDInDateRange(
													fieldDicts.appUserLogTableNameInHbase,
													userID,
													String.valueOf(System
															.currentTimeMillis()),
															maxDays_tmp);
									// 如果记录太少,查询更多天的item
									if (hm_log == null || hm_log.size() < minDays_tmp)
										hm_log = logDBOperation
												.queryUserIDInDateRange(
														fieldDicts.appUserLogTableNameInHbase,
														userID,
														String.valueOf(System
																.currentTimeMillis()),
																maxDays2_tmp);

								}
								
								//历史不为空，但是模型太大,怀疑user too old；取最近14天或者更少天数数据进行全量训练，重构user model
								if (userString != null)
								{
									String topic1 = usermodelInterfaceSolr.getTopicVector(userString, "topic1");
									if(topic1 != null && !(topic1.equals("error"))
											&& topic1.length() >= 10000)
									{
										
										LOG.warn("user model is big,maybe too old,so rebuild:" + userID);
										//此处特别指明tablename，避免并发互斥之错之慢;但是代码显得可读性降低；
										hm_log = logDBOperation.queryUserIDInDateRange(
														fieldDicts.appUserLogTableNameInHbase,
														userID,
														String.valueOf(System
																.currentTimeMillis()),
																maxDays_tmp);
										
										// 如果记录太少,查询更多天的item
										if (hm_log == null || hm_log.size() < minDays_tmp)
											hm_log = logDBOperation
													.queryUserIDInDateRange(
															fieldDicts.appUserLogTableNameInHbase,
															userID,
															String.valueOf(System
																	.currentTimeMillis()),
																	maxDays2_tmp);
									
									}

								}
							}

							if(TRAINMODE_banch.equals("new")){
								//查询历史user model，如果不存在那么就是新用户
								String userString = usermodelInterfaceSolr.queryUserStringFromSolr(userID);
								//历史上为空，是不是考虑进行全量训练？还是直接写入最近？
								if (userString != null
										&& userString.equals("cold"))// 完全是一个新用户
								{
									LOG.warn("user is new:" + userID);
									//此处特别指明tablename，避免并发互斥之错之慢;但是代码显得可读性降低；
									hm_log = logDBOperation.queryUserIDInDateRange(
													fieldDicts.appUserLogTableNameInHbase,
													userID,
													String.valueOf(System
															.currentTimeMillis()),
															maxDays_tmp);
									// 如果记录太少,查询更多天的item
									if (hm_log == null || hm_log.size() < minDays_tmp)
										hm_log = logDBOperation
												.queryUserIDInDateRange(
														fieldDicts.appUserLogTableNameInHbase,
														userID,
														String.valueOf(System
																.currentTimeMillis()),
																maxDays2_tmp);
									

								}
								
								
							}
							
							// 清理取出的行为列表，10分钟内的分页合并并去除其它额外信息；
							if(hm_log != null && hm_log.size() > 0)
							{
								HashMap<String, String> hm_dayitems = usermodelTraining
										.parseNewsAppUserDayLog(hm_log);
								hm_user2items.put(userID, hm_dayitems);
							}else{
								LOG.error("hbase is empty:"+userID);
							}
						}

						String rt = uifs.modelSomeUsers(hm_user2items, itemOP,TRAINMODE_banch,deviceType_tmp);
						if (rt == null) {
							LOG.warn("modelSomeUsers,rt == null");
						}
						LOG.info("finish:" + num);
						hm_user2items.clear();

						LOG.info("thread " + begin + " has finished...");

						cdl.countDown();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						LOG.error("", e);
					}

				}
			}).start();

		}

		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		LOG.info("now all threads have finished");
		LOG.info("train over");

	}
}
