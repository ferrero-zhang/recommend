package com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ifeng.commen.Utils.ConfigAutoLoader;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.featureEngineering.TagsQueryInterface;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.kedm.usercenter.TopicFalloff2;
import com.ifeng.iRecommend.kedm.util.ItemIKVOperation;
import com.ifeng.iRecommend.kedm.util.UserCenterIKVOperation;
import com.ifeng.iRecommend.kedm.util.UserlogIKVOperation;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userinfomodel.UserCenterModel;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userinfomodel.UserInfoFromLog;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.GlobalParams;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.HotWordUtil;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.ItemIKVUtil;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.UserCenterRedisUtil;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.UserRedisClusterUtil;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.userShardedRedisUtil;
import com.ifeng.iRecommend.train.usermodelTraining;
import com.ifeng.iRecommend.usermodel.idfQueryInterface;
import com.ifeng.iRecommend.usermodel.usermodelInterfaceSolr;
import com.ifeng.ikvlite.IkvLiteClient;
import com.ifeng.webapp.simArticle.client.SimDocClient;

/**
 * <PRE>
 * 作用 : 
 *   从redis中获取最近一段时间userinfo信息发生改动的userid集合，遍历集合，对其中的所有user进行模型更新
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
 *          1.0          2015年9月23日        likun         create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class UserCenterUpdateMain {
	private static final Log LOG = LogFactory.getLog(UserCenterUpdateMain.class);
//	//private static ItemIKVOperation  itemIKVOperation;
//	private static final String IKV_TABLE_NAME_USERLOG = "ir_user_log";//用于新用户模型的userlog表
	private static int userModelDBnum = GlobalParams.userCenterDBnum;//新用户中心redis集群的db
	
//	static{
//		//ItemIKVOperation.ItemIKVInit();
//		UserlogIKVOperation.userlogIKVInit();
//	}
	public static Map<String, String> phoneBrandMap = new HashMap<String, String>();
	public static Map<String, String> phonePriceMap = new HashMap<String, String>();
	
	public static Set<String> phoneVersion = new HashSet<String>();
	public static void main(String[] args) {
		//PropertyConfigurator.configure("./conf/log4j.properties");
		UserCenterModel2Solr.cloudServerInit();
		UserCenterIKVOperation.init();
		ItemIKVUtil.init();
		while (true) {
			try {
				long start = System.currentTimeMillis();
				int recordsNum_threshold = 30;
				
				LOG.info("Getting updated userid set...");
				List<String> al_allUpdatedUsers = new ArrayList<String>();
				
				if (0 == args.length) {
					LOG.warn("default update type:incr_update");
					al_allUpdatedUsers = getUpdateUserID();
				} else if (args[0].equals("incr_update")) {
					al_allUpdatedUsers = getUpdateUserID();
					recordsNum_threshold = Integer.valueOf(args[1]);
				} else if (args[0].equals("all_update")) {
					al_allUpdatedUsers = getAllUserID();
					recordsNum_threshold = Integer.valueOf(args[1]);
				} else {
					LOG.error("Please input update type:incr_update or all_update");
					break;
				}
				
				
				// 跑完了，再把UpdateUserID db 清空
				if (0 != args.length && !args[0].equals("all_update")) {
					LOG.info("Flush updated_user_id_set db...");
					flushRedisDB(GlobalParams.updateUserSetDBnum, al_allUpdatedUsers);
				}
//				al_allUpdatedUsers = new ArrayList<String>();
//				al_allUpdatedUsers.add("869161020003463");
				if (null == al_allUpdatedUsers || al_allUpdatedUsers.isEmpty()) {
					LOG.info("Has No Updated User, try to sleep 1min...");
					
					// 休息一会
					try {
						Thread.currentThread();
						Thread.sleep(1000 * 30);
					} catch (InterruptedException e) {
						LOG.error("thread sleep error:", e);
					}
					
					continue;
				}
				
				//手机品牌初始化
				phoneBrandMapInit();
				
				HotWordUtil.init();      //*********
				LOG.info("Running usercenter model update...");
				LOG.info("user set size="+al_allUpdatedUsers.size());
	
//old				
//				//用户中心集群shardedJedis
//				ShardedJedisPool sjp = userShardedRedisUtil.getJedisPoolMaster();
//				ShardedJedis sj = sjp.getResource();
//				
//				Collection<Jedis> js = sj.getAllShards();  
//		    	Iterator<Jedis> it = js.iterator(); 
//		    	while(it.hasNext()){  
//		    		Jedis j=it.next();
//		    		j.select(userModelDBnum);  
//		    	}
//		    	    	
//				ShardedJedisPipeline pipeline = sj.pipelined();
//				
//				
//				// debug信息，看下单个特征词在计算中的各种值
//				String debugFeature = "";
//				// 更新所有有新的访问行为的用户中心模型
//				int tmp_num = 1;//计数器，控制pipeline的长短
//				for (String tempUid : allUpdatedUserSet) {
//					updateUserCenterInfo(tempUid, debugFeature,pipeline,day_threshold);
//					tmp_num++;
//					if(tmp_num >= 100000){
//						try {
//							pipeline.sync();
//						} catch (Exception e) {
//							LOG.error("pipeline sync error:", e);
//						}
//						
//						tmp_num = 1;
//					}
//				}
				
//				try {
//					pipeline.sync();
//				} catch (Exception e) {
//					LOG.error("pipeline sync error:", e);
//				}
//				
//				sjp.returnResourceObject(sj);
//				sj.disconnect();
				
				
				
				//避免单例模式失效，预加载
				//UserlogIKVOperation.userlogIKVInit();
				
				// 分组，多线程进行建模
				int allUsersNum = al_allUpdatedUsers.size();
				//为安全考虑，超过100w的不处理
				allUsersNum = allUsersNum>1000000?1000000:allUsersNum;
				
				LOG.warn("allUsersNum="+allUsersNum);
				
			
				int threadsNum = Integer.valueOf(args[2]);
				//int threadsNum = Integer.valueOf("1");
				if(allUsersNum < threadsNum)
					threadsNum = 1;
				
				final CountDownLatch cdl = new CountDownLatch(threadsNum);
				for (int i = 0; i < threadsNum; ++i) {
					
					// debug信息，看下单个特征词在计算中的各种值
					final String debugFeature = "";
					final int recordsNum_threshold_td = recordsNum_threshold;
					
					
					// 计算最恰当的间隔长度
					int interNum = allUsersNum / threadsNum;
					if (allUsersNum % threadsNum != 0)
						interNum++;

					final int begin = (interNum) * i;
					final int end = allUsersNum < (begin + interNum) ? allUsersNum
							: (begin + interNum);
					final String[] someUsers = new String[end - begin];
					for (int k = begin; k < end; k++)
						someUsers[k - begin] = al_allUpdatedUsers.get(k);
				
				
					
					new Thread(new Runnable() {
						@Override
						public void run() {
							//用户中心集群shardedJedis
							

							//每一轮清空
							//queryItemFeatures query = new queryItemFeatures();
							
							//ikv lite client
							//LiteClientStore liteClient = UserlogIKVOperation.getIKVClient();
							//IkvLiteClient liteClient = null;
							
							
							//准备好90 day key，每个用户需要取90天的用户记录
							long end = System.currentTimeMillis();
							long start = end - 60*24*3600*1000L;
							List<String> days_keysSet = new ArrayList<String>();//用户的日期段keys
							DateFormat df = new SimpleDateFormat("yyyyMMdd");
							Calendar calS = Calendar.getInstance();
							Calendar calE = Calendar.getInstance();

							try{
								//遍历组合一个用户的所有日期段的keys，批量查询ikv
								calS.setTimeInMillis(start);
								calE.setTimeInMillis(end);
								while(calE.compareTo(calS) >= 0){
									String date = df.format(calE.getTime());
									if(date != null){
										days_keysSet.add(date);
									}
									calE.set(Calendar.DATE, calE.get(Calendar.DATE)-1);
								}
							}catch(Exception e){
								LOG.error("init 90 days key error:",e);
							}
							//redis cluster 方式
							//JedisCluster jc = UserRedisClusterUtil.getJedisCluster();
							//ShardedJedisPool sjp = null;
							//ShardedJedis sj = null;
							
							//用户中心新机房集群，暂时两个集群都写，完全迁移数据后，暂停旧的
							ShardedJedisPool sjp_uc = UserCenterRedisUtil.getJedisPoolMaster();
							ShardedJedis sj_uc = sjp_uc.getResource();
							// 更新所有有新的访问行为的用户中心模型
							try{
								/*sjp = userShardedRedisUtil.getJedisPoolMaster();
								sj = sjp.getResource();
								
								Collection<Jedis> js = sj.getAllShards();  
						    	Iterator<Jedis> it = js.iterator(); 
						    	while(it.hasNext()){  
						    		Jedis j=it.next();
						    		j.select(userModelDBnum);  
						    	}
						    	    	
								ShardedJedisPipeline pipeline = sj.pipelined();*/
								
								
								
								Collection<Jedis> js_uc =  sj_uc.getAllShards();
						    	Iterator<Jedis> it_uc = js_uc.iterator(); 
						    	while(it_uc.hasNext()){  
						    		Jedis j=it_uc.next();  
						    	}
						    	    	
								ShardedJedisPipeline pipeline_uc = sj_uc.pipelined();
								//liteClient = UserCenterIKVOperation.getIKVClient();
								int tmp_num = 1;// 计数器，控制pipeline的长短=
								for (String userid : someUsers) {
									try{
										deleteRedis(GlobalParams.updateUserSetDBnum,userid);
										updateUserCenterInfo(/*query,*/userid, debugFeature, pipeline_uc, sj_uc,
												recordsNum_threshold_td,days_keysSet);
										
									}catch(Exception e){
										LOG.error("updateUserCenterInfo error "+userid,e);
										continue;
									}
									tmp_num++;
									if (tmp_num >= 50000) {
										try {
											//pipeline.sync();
											long s_pp = System.currentTimeMillis();
											pipeline_uc.sync();
											LOG.info("pipeline to redis spend "+(System.currentTimeMillis()-s_pp));
										} catch (Exception e) {
											LOG.error("pipeline sync error:", e);
											e.printStackTrace();
										}

										tmp_num = 1;
									}
								}

								try {
									//pipeline.sync();
									pipeline_uc.sync();
								} catch (Exception e) {
									LOG.error("pipeline sync error:", e);
									e.printStackTrace();
								}
							}catch(Exception e){
								LOG.error("update user center error:",e);
							}finally{
								cdl.countDown();
								//query.clean();
								LOG.info(Thread.currentThread().getName() + " close ikv liteClient");

								//sjp.returnResourceObject(sj);
								//sj.disconnect();
								
								sjp_uc.returnResourceObject(sj_uc);
								sj_uc.disconnect();
								
								
							}
							
							
						}

					}).start();
					
				}

				try {
					cdl.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// single user debug
				//updateUserCenterInfo("A39C7851-579A-4CE5-9F63-7467DAE24F37", debugFeature);
				
				LOG.info("Updated " + al_allUpdatedUsers.size() + " user's model");
				
				//ItemIKVUtil.close();
				//UserCenterIKVOperation.close();
				long end = System.currentTimeMillis();
				LOG.info("duration of process:" + (end - start)/1000 + " ms");
				
				/*try {
					//保存未获取品牌的手机型号
					StringBuilder sb = new StringBuilder();
					Iterator<String> it = phoneVersion.iterator();
					while(it.hasNext()){
						sb.append(it.next()).append("\r\n");
					}
					new FileUtil().Append2File(sb.toString(), "/data/xuzc/phoneVersion.log");
				} catch (Exception e1) {
					e1.printStackTrace();
				}*/
				
				// 休息一会
				try {
					LOG.info("process end, try to sleep 1min...");
					Thread.currentThread();
					Thread.sleep(1000 * 30);
				} catch (InterruptedException e) {
					LOG.error("thread sleep error:", e);
				}
			} catch (Exception ex) {
				LOG.error("Exception during user model update:", ex);
				// 休息一会
				try {
					Thread.currentThread();
					Thread.sleep(1000 * 30);
				} catch (InterruptedException e) {
					LOG.error("thread sleep error:", e);
				}
				
				// 继续
				continue;
			}
		}
	}
	
	/**
	 * @param days_keysSet 
	 * @Title: getUserLogInfo
	 * @Description: 对单个用户的模型进行计算更新,从ikv中查询
	 * @author likun
	 * @param  userid
	 * @param  recordsNum_threshold 取最近多少条目的log行为建模；
	 * @throws
	 */
	public static Map<String, String> getUserLogInfo(String userid,int recordsNum_threshold,List<String> days_keysSet){
		if (null == userid || userid.isEmpty()) {
			return null;
		}
		
		if(recordsNum_threshold <0 || recordsNum_threshold > 90)
			recordsNum_threshold = 30;

		Map<String, String> userLogInfo = null;
		try{
			//userLogInfo = UserlogIKVOperation.getUserLogInfo(userid,recordsNum_threshold);
			//userLogInfo = UserlogIKVOperation.getUserLogInfo(userid, recordsNum_threshold, liteclient, days_keysSet);
			long s = System.currentTimeMillis();
			userLogInfo = UserCenterIKVOperation.getUserLogInfo(userid, recordsNum_threshold, days_keysSet);
			LOG.info(userid + " get from ikv spend "+(System.currentTimeMillis()-s));
			//userLogInfo = ItemIKVOperation.getUserLogInfo(userid,recordsNum_threshold);
		}catch(Exception e){
			userLogInfo = null;
			LOG.error("getUserLogInfo",e);
		}
		
		return userLogInfo;
	}
	
	/**
	 * @param days_keysSet 
	 * @Title: updateUserCenterInfo
	 * @Description: 对单个用户的模型进行计算更新,从ikv中查询
	 * @author liu_yi
	 * @param userid
	 * @param  day_threshold 取最近多少天的log行为建模；以天为单位
	 * @throws
	 */
	public static void updateUserCenterInfo(/*queryItemFeatures query,*/String userid, String debugFeature,ShardedJedisPipeline pipeline_uc,ShardedJedis sj_uc,int day_threshold,List<String> days_keysSet) {
		if (null == userid || userid.isEmpty() || pipeline_uc == null) {
			return;
		}
	
		
		// 先获取日志信息
		LOG.info("Getting userlog info...");	
		long updateS = System.currentTimeMillis();
		Map<String, String> userLogInfo = getUserLogInfo(userid,day_threshold, days_keysSet);
		if(userLogInfo == null)
		{
			LOG.warn(userid+" loginfo is null");
			return;
		}
		String login_interest = null;
		try{
			login_interest = sj_uc.hget(userid, "login_interest");
		}catch(Exception e){
			e.printStackTrace();
		}
		String userStarttime = userLogInfo.get("starttime");
		//LOG.info(userid + " userloginfo "+userLogInfo.get(FeatureWordStatistics.readDoc_type_name));
		String userInterestWordList = userLogInfo.get(FeatureWordStatistics.userInterest_type_name);
		String userBookWordList = userLogInfo.get(FeatureWordStatistics.bookWord_type_name);
		String userLoc = userLogInfo.get(ConfigAutoLoader.getPropertyWithKey("userSetLoc"));
		String userKeywordList = userLogInfo.get(FeatureWordStatistics.readKW_type_name);
		String userSearchList = userLogInfo.get(FeatureWordStatistics.search_type_name);
		String userSwListStr = "";
		String userSwList = userLogInfo.get(FeatureWordStatistics.readSW_type_name);
		String userChList = userLogInfo.get("ch_open");
		Map<String,Integer> swopen = getSwCountInfo(userSwList);
		
		// sort interest words str
		String userInterest_last = getConfirmInfo(userInterestWordList, swopen);
		// sort book words str
		String userBook_last = getConfirmInfo(userBookWordList, swopen);
		// sort keywords str
		//String userKeyword_last = getSortBookInfo(userKeywordList, GlobalParams.topAskNum);
		//String userKeyword_last = getSwCountInfo(userKeywordList);
		String userKeyword_confirm = getConfirmInfo(userKeywordList,swopen);
		// sort search str
		//String userSearch_last = getSortBookInfo(userSearchList, GlobalParams.topAskNum);
		//String userSearch_last = getSwCountInfo(userSearchList);
		String userSearch_confirm = getConfirmInfo(userSearchList,swopen);
		
		Map<String,Integer> chopen = getChCountInfo(userChList);
		if(swopen == null){
			swopen = chopen;
		}else{
			swopen.putAll(chopen);
		}
		//定性swopen
		if(swopen != null){
			Map<String,Integer> tempswopen = commenFuncs.sortMapByValues(swopen, "descend");
			if(tempswopen != null){
				int swcount = 0;
				for(String sw : tempswopen.keySet()){
					if(swcount == 2)
						break;
					if(tempswopen.get(sw) > 2){
						swcount++;
						userSwListStr += sw + "#";
					}
				}
			}
		}
		String userDuration = userLogInfo.get("duration");
		String fineDocid = getConfirmDuration(userDuration);//定性用户近三天最愿意看的三篇内容
		String userVideo = userLogInfo.get("likevideo");
		String islikevideo = getConfirmVideo(userVideo);//判断用户是否爱看视频
		//新增user_mos user_ver user_mt
		String user_mos = userLogInfo.get("u_mos");
		String user_ver = userLogInfo.get("u_ver");
		String user_mt = userLogInfo.get("u_mt");
		
//		result.put(FeatureWordStatistics.readDoc_type_name, userOpenDocListStr);
//		result.put(FeatureWordStatistics.readKW_type_name, userKeywordListStr);
//		result.put(FeatureWordStatistics.search_type_name, userSearchListStr);
		
		// topic1, topic2, topic3
		LOG.info("Computing user " + userid + "'s topic...");
		Map<String, String> topInfoFromUserReadDoc = getUserTopicInfo(/*query,*/userLogInfo, debugFeature);
		if(topInfoFromUserReadDoc == null)
			return;

		
		// 新建用户中心中的用户模型
		String topic1_top_value = topInfoFromUserReadDoc.get("topic1_top_value");
		String topic2_top_value = topInfoFromUserReadDoc.get("topic2_top_value");
		String topic3_top_value = topInfoFromUserReadDoc.get("topic3_top_value");
		String last7daysStr = topInfoFromUserReadDoc.get("last7daysStr");
		String phoneBrand = null;
		if(user_mt != null){
			phoneBrand = phoneBrandMap.get(user_mt);
			if(phoneBrand != null){
				user_mt += "$"+phoneBrand;
			}else{
				phoneVersion.add(user_mt);
			}
			
		}
		//用户订阅加入启动兴趣
		userInterest_last = joinLoginInterest(login_interest,userInterest_last,last7daysStr);
		String t1_confirm = getConfirmT1(topic1_top_value);
		if(t1_confirm != null && !t1_confirm.trim().equals("")){
			topic1_top_value = t1_confirm;
		}
		
		String t2_confirm = getConfirmT3(topic2_top_value);
		if(t2_confirm != null && !t2_confirm.trim().equals("")){
			topic2_top_value = t2_confirm;
		}
		
		String t3_confirm = getConfirmT3(topic3_top_value);
		if(t3_confirm != null && !t3_confirm.trim().equals("")){
			topic3_top_value = t3_confirm;
		}
		
		String e_top_value = topInfoFromUserReadDoc.get("e_top_value");
		String s1_top_value = topInfoFromUserReadDoc.get("s1_top_value");
		
		String last_topic1 = topInfoFromUserReadDoc.get("last_t1_value");
		String last_topic2 = topInfoFromUserReadDoc.get("last_t2_value");
		String last_topic3 = topInfoFromUserReadDoc.get("last_t3_value");
		
		String pic_topic1 = topInfoFromUserReadDoc.get("pic_topic1_value");
		String pic_topic2 = topInfoFromUserReadDoc.get("pic_topic2_value");
		String pic_topic3 = topInfoFromUserReadDoc.get("pic_topic3_value");
		
		String vid_topic1 = topInfoFromUserReadDoc.get("video_topic1_value");
		String vid_topic2 = topInfoFromUserReadDoc.get("video_topic2_value");
		String vid_topic3 = topInfoFromUserReadDoc.get("video_topic3_value");
		
		String slidet1_confirm = getConfirmSlideT1(pic_topic1,GlobalParams.SLIDE_TAGS,topic1_top_value);
		
		if(slidet1_confirm != null && !slidet1_confirm.trim().equals("")){
			pic_topic1 = slidet1_confirm;
		}
		
		String slidet2_confirm = getConfirmT3(pic_topic2);
		if(slidet2_confirm != null && !slidet2_confirm.trim().equals("")){
			pic_topic2 = slidet2_confirm;
		}
		
		String slidet3_confirm = getConfirmT3(pic_topic3);
		if(slidet3_confirm != null && !slidet3_confirm.trim().equals("")){
			pic_topic3 = slidet3_confirm;
		}
		
		String active = getUserActive(topic1_top_value,userStarttime);

		//LOG.info("Building UserCenterModel for user:" + userid);
		if(topic1_top_value == null || topic1_top_value.equals("null")){
			LOG.info(userid + " get topic null ");
		}
		UserCenterModel ucm = new UserCenterModel(userid, topic1_top_value + "$" , 
				topic2_top_value + "$" , topic3_top_value + "$" , 
				userInterest_last, userBook_last, active, userLoc);
		/*ucm.setSort_keywords(userKeyword_last);
		ucm.setSort_search_words(userSearch_last);*/
		ucm.setUser_mtype(user_mt);
		
		ucm.setE_top_words(e_top_value);
		ucm.setS1_top_words(s1_top_value);
		
		ucm.setLast_topic1(last_topic1);
		ucm.setLast_topic2(last_topic2);
		ucm.setLast_topic3(last_topic3);
		ucm.setPic_topic1(pic_topic1);
		ucm.setPic_topic2(pic_topic2);
		ucm.setPic_topic3(pic_topic3);
		ucm.setVid_topic1(vid_topic1);
		ucm.setVid_topic2(vid_topic2);
		ucm.setVid_topic3(vid_topic3);
		
		ucm.setLikevideo(islikevideo);
		ucm.setFineitem(fineDocid);
		//添加用户订阅算法频道
		try {
			ucm.setSort_interest_words(userInterest_last);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//添加用户订阅频道
		try {
			ucm.setSort_book_words(userBook_last);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//LOG.info(userid + " search and keywords is "+ userKeyword_last + userSearch_last);
		if(userKeyword_confirm != null){
			ucm.setSort_keywords(userKeyword_confirm);
		}
		if(userSearch_confirm != null){
			ucm.setSort_search_words(userSearch_confirm);
		}
		if(user_mos != null){
			ucm.setUser_os(user_mos);
		}
		if(user_ver != null){
			ucm.setUser_ver(user_ver);
			//LOG.info(userid + " client version is " + user_ver);
		}
		
		if(ucm.getUser_loc() == null || ucm.getUser_loc().trim().equals("$")){
			String loc = UserCenterModel2Redis.tempGetLocalInfo(ucm.getUser_id());
			if(loc != null){
				ucm.setUser_loc(loc);
			}
		}
		if(userSwListStr != null){
			ucm.setSw_open(userSwListStr);
		}
		LOG.info("Updating user " + userid + " center model to redis...");
		//写入solr用户中心
		try{
			long s = System.currentTimeMillis();
			
			UserCenterModel2Solr.ucmToSolr(ucm);
			LOG.info("umt to solr spend "+(System.currentTimeMillis()-s));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//写入redis用户中心
		UserCenterModel2Redis.ucModelUpdate2Redis(ucm,pipeline_uc);
		queryItemFeatures.clean();
		LOG.info(Thread.currentThread().getName() + " update usercenter spend "+(System.currentTimeMillis()-updateS));
	}
	
	/**
	 * @param userInterestWordList
	 * @param swopen 算法频道下用户点击的排行
	 * @return 将用户订阅按照点击的数量进行排序，没有点击行为的排在后面
	 */
	private static String getInfoFromT2OrT3(String wordList, Map<String,Integer> swopen) {
		String result = "";
		StringBuffer sb = new StringBuffer();
		if(wordList!=null){
			List<String> listword = new ArrayList<String>();
			List<String> listword_noT2T3 = new ArrayList<String>();
			String[] words = wordList.split("#");
			if(words.length < 1)
				return null;
			for(String word : words){
				if(swopen.containsKey(word)){
					listword.add(word+"_"+swopen.get(word));
				}else{
					listword_noT2T3.add(word);
				}
			}
			//排序
			if(listword.size()>0){
				sort(listword);
				for(String w : listword){
					sb.append(w).append("#");
				}
			}			
			if(listword_noT2T3.size()>0){
				for(String w : listword_noT2T3){
					sb.append(w).append("#");
				}
			}
		}
		result = sb.toString();
		if(result.endsWith("#")){
			result = result.substring(0, result.length()-1);
		}
		return result;
	}
	public static void sort(List<String> list) {  
        Collections.sort(list, new Comparator<String>() {  
  
			@Override
			public int compare(String o1, String o2) {
				String[] tt = o1.split("_");
				String[] yy = o2.split("_");
				return Integer.parseInt(yy[1]) - Integer.parseInt(tt[1]);
			}  
        });    
    }
	/**
	 * @Title: getDocTopicInfo
	 * @Description: 获取用户阅读文章的top信息以及lastview信息
	 * @author liu_yi
	 * @param userLogInfo 用户行为信息，按字段记录在map中
	 * @return
	 * @throws
	 */
	public static Map<String, String> getUserTopicInfo(/*queryItemFeatures query,*/Map<String, String> userLogInfo, String debugFeature) {
		if (null == userLogInfo || userLogInfo.isEmpty()) {
			return null;
		}
		Map<String, String> result = new HashMap<String, String>();
		Gson gson = new Gson();
		// 词列表
		List<String> topic1_word_list = new ArrayList<String>();
		List<String> topic2_word_list = new ArrayList<String>();
		List<String> topic3_word_list = new ArrayList<String>();

		// 词的得分
		List<Double> topic1_word_score = new ArrayList<Double>();
		List<Double> topic2_word_score = new ArrayList<Double>();
		List<Double> topic3_word_score = new ArrayList<Double>();
		
		List<String> last_topic1_word_list = new ArrayList<String>();
		List<String> last_topic2_word_list = new ArrayList<String>();
		List<String> last_topic3_word_list = new ArrayList<String>();

		// 词的得分
		List<Double> last_topic1_word_score = new ArrayList<Double>();
		List<Double> last_topic2_word_score = new ArrayList<Double>();
		List<Double> last_topic3_word_score = new ArrayList<Double>();
		
		List<String> dislike_topic1_word_list = new ArrayList<String>();
		List<String> dislike_topic2_word_list = new ArrayList<String>();
		List<String> dislike_topic3_word_list = new ArrayList<String>();
		
		//注意，由于word会覆盖写，所以取用户最近的time
		Map<String, String> topic1_wordTimeMap = new HashMap<String,String>();
		Map<String, String> topic2_wordTimeMap = new HashMap<String,String>();
		Map<String, String> topic3_wordTimeMap = new HashMap<String,String>();
		
		//热点事件、优质稿源
		List<String> e_wordTimeList = new ArrayList<String>();
		List<String> s1_wordTimeList = new ArrayList<String>();
		Map<String,String> i_tagWord = new HashMap<String,String>();
		String t1_result = null;
		String t2_result = null;
		String t3_result = null;
		//Test
		//LOG.info("userLogInfo begin:");
		long b = System.currentTimeMillis();
		String userDislike = userLogInfo.get("dislike");
		if(userDislike != null){
			FeatureWordStatistics dislike_fs = new FeatureWordStatistics("dislike", userDislike);
			dislike_fs.wordSta(debugFeature);
			dislike_topic1_word_list.addAll(dislike_fs.getDislike_topic1_word_List());
			dislike_topic2_word_list.addAll(dislike_fs.getDislike_topic2_word_List());
			dislike_topic3_word_list.addAll(dislike_fs.getDislike_topic3_word_List());
		}
		
		String userRead = userLogInfo.get("ur");
		if(userRead != null){
			List<String> dayliyUserReads = gson.fromJson(userRead, new TypeToken<List<String>>(){}.getType());
			List<String> features_t1 = new ArrayList<String>();
			List<String> features_t2 = new ArrayList<String>();
			List<String> features_t3 = new ArrayList<String>();
			TopicFalloff2 topicOff1 = new TopicFalloff2();
			TopicFalloff2 topicOff2 = new TopicFalloff2();
			TopicFalloff2 topicOff3 = new TopicFalloff2();
			
			int day_count = 0;
			int sum = 0;
			int last7days = 0;
			StringBuffer last7daysStr = new StringBuffer();
			Collections.reverse(dayliyUserReads);
			for(String ur : dayliyUserReads){
				last7days++;
				
				FeatureWordStatistics temp_fs = new FeatureWordStatistics("ur", ur);
				temp_fs.wordSta(debugFeature);
				
				e_wordTimeList.addAll(temp_fs.getE_wordTime_list());
				s1_wordTimeList.addAll(temp_fs.getS1_wordTime_list());
				
				//计算得分模型，以及最近行为得分模型
				HashMap<String, Double> hm_word2score = new HashMap<String, Double>();
				String topic1_top_value = getTopWord("t1",temp_fs.getTopic1_word_List(), temp_fs.getTopic1_word_score(),hm_word2score, GlobalParams.topAskNum, debugFeature,dislike_topic1_word_list);
				//String topic1_lastview = getLatestInfo(topic1_wordTimeMap,hm_word2score, GlobalParams.lastViewDay,GlobalParams.topAskNum/10);
				
				hm_word2score.clear();
				String topic2_top_value = getTopWord("t2",temp_fs.getTopic2_word_List(), temp_fs.getTopic2_word_score(),hm_word2score, GlobalParams.topAskNum, debugFeature,dislike_topic2_word_list);
				//String topic2_lastview = getLatestInfo(topic2_wordTimeMap,hm_word2score, GlobalParams.lastViewDay,GlobalParams.topAskNum/10);
				
				hm_word2score.clear();
				String topic3_top_value = getTopWord("t3",temp_fs.getTopic3_word_List(), temp_fs.getTopic3_word_score(), hm_word2score,GlobalParams.topAskNum, debugFeature,dislike_topic3_word_list);
				//String topic3_lastview = getLatestInfo(topic3_wordTimeMap,hm_word2score, GlobalParams.lastViewDay,GlobalParams.topAskNum/10);
				hm_word2score.clear();
				if(null == topic1_top_value || null == topic2_top_value || null == topic3_top_value){
					continue;
				}
				day_count++;
				sum++;
				//System.out.println(topic1_top_value);
				if(day_count <= 1){
					features_t1.add(topic1_top_value);
					features_t2.add(topic2_top_value);
					features_t3.add(topic3_top_value);
				}
				if(last7days > (dayliyUserReads.size() -7)){
					last7daysStr.append(topic1_top_value).append(topic2_top_value)
					.append(topic3_top_value);
				}
				if(day_count == 1 || sum == dayliyUserReads.size()){
					t1_result = topicOff1.proccess(features_t1);
					//System.out.println("current ... "+t1_result);
					t2_result = topicOff2.proccess(features_t2);
					t3_result = topicOff3.proccess(features_t3);
					//System.out.println(t3_result);
					features_t1.clear();
					features_t2.clear();
					features_t3.clear();
					day_count = 0;
				}
			}
			String e_top_value = getEInfo(e_wordTimeList);
			String s1_top_value = getSInfo(s1_wordTimeList);
			if(e_top_value != null){
				result.put("e_top_value", e_top_value);
			}
			if(s1_top_value != null){
				result.put("s1_top_value", s1_top_value);
			}
			result.put("topic1_top_value", getSortTopic(t1_result));
			result.put("topic2_top_value", getSortTopic(t2_result));
			result.put("topic3_top_value", getSortTopic(t3_result));
			result.put("last7daysStr", last7daysStr.toString());
		}
		String userReadPic = userLogInfo.get("upic");
		if(userReadPic != null){
			FeatureWordStatistics upic_fs = new FeatureWordStatistics("ur", userReadPic);
			upic_fs.wordSta(debugFeature);
			//计算得分模型，以及最近行为得分模型
			HashMap<String, Double> hm_word2score = new HashMap<String, Double>();
			String topic1_top_value = getTopWord("t1",upic_fs.getTopic1_word_List(), upic_fs.getTopic1_word_score(),hm_word2score, GlobalParams.topAskNum, debugFeature,null);
			//String topic1_lastview = getLatestInfo(topic1_wordTimeMap,hm_word2score, GlobalParams.lastViewDay,GlobalParams.topAskNum/10);
			
			hm_word2score.clear();
			String topic2_top_value = getTopWord("t2",upic_fs.getTopic2_word_List(), upic_fs.getTopic2_word_score(),hm_word2score, GlobalParams.topAskNum, debugFeature,null);
			//String topic2_lastview = getLatestInfo(topic2_wordTimeMap,hm_word2score, GlobalParams.lastViewDay,GlobalParams.topAskNum/10);
			
			hm_word2score.clear();
			String topic3_top_value = getTopWord("t3",upic_fs.getTopic3_word_List(), upic_fs.getTopic3_word_score(), hm_word2score,GlobalParams.topAskNum, debugFeature,null);
			//String topic3_lastview = getLatestInfo(topic3_wordTimeMap,hm_word2score, GlobalParams.lastViewDay,GlobalParams.topAskNum/10);
			hm_word2score.clear();
			result.put("pic_topic1_value", topic1_top_value);
			result.put("pic_topic2_value", topic2_top_value);
			result.put("pic_topic3_value", topic3_top_value);
		}
		String userReadVideo = userLogInfo.get("likevideo");
		if(userReadVideo != null){
			FeatureWordStatistics uvideo_fs = new FeatureWordStatistics("ur", userReadVideo);
			uvideo_fs.wordSta(debugFeature);
			//计算得分模型，以及最近行为得分模型
			HashMap<String, Double> hm_word2score = new HashMap<String, Double>();
			String topic1_top_value = getVidTopWord("t1", userReadVideo, uvideo_fs.getTopic1_word_List(), uvideo_fs.getTopic1_word_score(),hm_word2score, GlobalParams.topAskNum, debugFeature,null);
			//String topic1_lastview = getLatestInfo(topic1_wordTimeMap,hm_word2score, GlobalParams.lastViewDay,GlobalParams.topAskNum/10);
			
			hm_word2score.clear();
			String topic2_top_value = getVidTopWord("t2", userReadVideo, uvideo_fs.getTopic2_word_List(), uvideo_fs.getTopic2_word_score(),hm_word2score, GlobalParams.topAskNum, debugFeature,null);
			//String topic2_lastview = getLatestInfo(topic2_wordTimeMap,hm_word2score, GlobalParams.lastViewDay,GlobalParams.topAskNum/10);
			
			hm_word2score.clear();
			String topic3_top_value = getVidTopWord("t3", userReadVideo, uvideo_fs.getTopic3_word_List(), uvideo_fs.getTopic3_word_score(), hm_word2score,GlobalParams.topAskNum, debugFeature,null);
			//String topic3_lastview = getLatestInfo(topic3_wordTimeMap,hm_word2score, GlobalParams.lastViewDay,GlobalParams.topAskNum/10);
			hm_word2score.clear();
			result.put("video_topic1_value", topic1_top_value);
			result.put("video_topic2_value", topic2_top_value);
			result.put("video_topic3_value", topic3_top_value);
		}
		String userReadLast = userLogInfo.get("last2days");
		if(userReadLast != null){
			FeatureWordStatistics userReadLast_fs = new FeatureWordStatistics("last2day", userReadLast);
			userReadLast_fs.wordSta(debugFeature);
			last_topic1_word_list.addAll(userReadLast_fs.getLast_topic1_word_List());
			last_topic2_word_list.addAll(userReadLast_fs.getLast_topic2_word_List());
			last_topic3_word_list.addAll(userReadLast_fs.getLast_topic3_word_List());
			
			last_topic1_word_score.addAll(userReadLast_fs.getLast_topic1_word_score());
			last_topic2_word_score.addAll(userReadLast_fs.getLast_topic2_word_score());
			last_topic3_word_score.addAll(userReadLast_fs.getLast_topic3_word_score());
			
			HashMap<String, Double> hm_word2score = new HashMap<String, Double>();
			String last_t1_value = getTopWord("t1",last_topic1_word_list, last_topic1_word_score,hm_word2score, GlobalParams.topAskNum, "last",null);
			hm_word2score.clear();
			String last_t2_value = getTopWord("t2",last_topic2_word_list, last_topic2_word_score,hm_word2score, GlobalParams.topAskNum, "last",null);
			hm_word2score.clear();
			String last_t3_value = getTopWord("t3",last_topic3_word_list, last_topic3_word_score,hm_word2score, GlobalParams.topAskNum, "last",null);
			hm_word2score.clear();
			
			result.put("last_t1_value", last_t1_value);
			result.put("last_t2_value", last_t2_value);
			result.put("last_t3_value", last_t3_value);
		}
		return result;
	}
	public static String getUserActive(String t1,String starttime){
		String res = "B";
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		if(t1 == null || t1.isEmpty())
			return "C";
		try{
			List<String> t = Arrays.asList(t1.split("#"));
			String[] first = t.get(0).split("_");
			int top = Integer.parseInt(first[1]);
			if(top > 20){
				res = "A";
			}
			if(top < 10){
				res = "C";
			}
			Date st = df.parse(starttime.split("_")[1]);
			long start = st.getTime()/1000;
			if((System.currentTimeMillis()/1000 - start) < 7*24*3600){
				res = "C";
			}
		}catch(Exception e){
			e.printStackTrace();
		}	
		return res;
	}
	public static String getSortTopic(String info){
		if(null == info){
			return null;
		}
		StringBuffer sb = new StringBuffer();
		try{
			Map<String,Double> tempsort = new HashMap<String,Double>();
			Map<String,Double> tagscore = new HashMap<String,Double>();
			Map<String,Integer> tagcount = new HashMap<String,Integer>();
			DecimalFormat df2 = new DecimalFormat("#0.00");
			for(String s : info.split("#")){
				String[] temp = s.split("_");
				tagscore.put(temp[0], Double.parseDouble(temp[2]));
				try{
					tagcount.put(temp[0], Integer.parseInt(temp[1]));
				} catch (NumberFormatException e){
					tagcount.put(temp[0], (int) Math.round(Double.parseDouble(temp[1])));
				}
			}
			tempsort = commenFuncs.sortMapByValues(tagscore, "descend");
			
			for(String key : tempsort.keySet()){
				if(tagcount.get(key) < 1.0)
					continue;
				sb.append(key).append("_").append(tagcount.get(key)).append("_").append(df2.format(tempsort.get(key))).append("#");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	
	/**
	 * @Title: getSortBookInfo
	 * @Description: 传入用户订阅列表，返回排序后的订阅词
	 * @author liu_yi
	 * @param userBookList
	 * @return
	 * @throws
	 */
	public static String getSortBookInfo(String userBookList, int topNum) {
		if (null == userBookList || userBookList.isEmpty()) {
			return null;
		}
		
		Map<String, Long> book_time_map = new HashMap<String, Long>();
		
		String[] bookword_array = userBookList.split("\\$");
		for (String tempBook : bookword_array) {
			String[] tempBookSplit = tempBook.split("#");
			if (2 != tempBookSplit.length) {
				continue;
			}
			
			Long tempBookTime = Long.valueOf(tempBookSplit[1]);
			book_time_map.put(tempBookSplit[0], tempBookTime);
		}
		
		// 排下序		
		Map<String, Long> book_time_map_sort = new HashMap<String, Long>();
		book_time_map_sort = commenFuncs.sortMapByValues(book_time_map, "descend");
		int topSize = topNum;
		if (book_time_map_sort.size() <= topNum) {
			topSize = book_time_map_sort.size();
		}
		
		String topValue = "";
		int topCount = 0;
		for (String tempWord : book_time_map_sort.keySet()) {
			if (topCount != topSize) {
				topValue += tempWord + "#";
			} else {
				break;
			}
			
			topCount++;
		}
		
		if (topValue.endsWith("#")) {
			String newTopValue = topValue.substring(0, topValue.length() - 1);
			return newTopValue;
		}
		
		return topValue;
	}
	public static String getConfirmDuration(String infoList){
		if(null == infoList || infoList.isEmpty()){
			return null;
		}
		Map<String,Long> doc_duration = new HashMap<String,Long>();
		String[] word_array = infoList.split("\\$");
		for(String tempword : word_array){
			String[] tempwordsplit = tempword.split("#");
			if(3 != tempwordsplit.length)
				continue;
			if(!tempwordsplit[0].matches("\\d{5,16}"))
				continue;
			if(Double.parseDouble(tempwordsplit[1]) < 10)
				continue;
			doc_duration.put(tempwordsplit[0], Long.parseLong(tempwordsplit[2]));
			
		}
		Map<String,Long> sortmap = commenFuncs.sortMapByValues(doc_duration, "descend");

		int count = 0;
		StringBuffer sb = new StringBuffer();
		for(String tempword : sortmap.keySet()){			
			String clusterId;
			try{
				String ret = SimDocClient.doSearch(tempword, null, null, null);  //查simid
				JSONObject jsonObject = JSONObject.fromObject(ret);
			    clusterId = jsonObject.getString("clusterId");
				if(clusterId.contains("null")){  //查不到
					sb.append(tempword).append("#");
				} else 
					sb.append(clusterId).append("#");
				
			} catch (Exception e){   //查不到
				sb.append(tempword).append("#");
			}
			
			count++;	
			
			if(count == 3) break;
			
		}
		return sb.toString();
	}
	
	public static String getConfirmVideo(String infoList){
		String res = "false";
		if(null == infoList || infoList.isEmpty()){
			return res;
		}
		int video_count = 0;
		String[] word_array = infoList.split("\\$");
		long now = System.currentTimeMillis()/1000;
		for(String tempword : word_array){
			
			String[] tempwordsplit = tempword.split("#");
			if(3 != tempwordsplit.length)
				continue;
			if((now - Long.parseLong(tempwordsplit[2])) > 7*24*3600){
				continue;
			}
			video_count++;
		}
		if(video_count > 5){
			res = "true";
		}
		return res;
	}
	public static Map<String, Integer> getChCountInfo(String userChList){
		if (null == userChList || userChList.isEmpty()) {
			return null;
		}
		
		Map<String, Integer> sw_time_map = new HashMap<String, Integer>();
		long now = System.currentTimeMillis()/1000;
		String[] swword_array = userChList.split("\\$");
		for (String tempSw : swword_array) {
			String[] tempBookSplit = tempSw.split("#");
			if (2 != tempBookSplit.length) {
				continue;
			}
			if((now - Long.parseLong(tempBookSplit[1])) > 15*24*3600){
				continue;
			}
			String ch = HotWordUtil.ch_tag.get(tempBookSplit[0]);
			if(ch == null)
				continue;
			if(sw_time_map.containsKey(ch)){
				int c = sw_time_map.get(ch);
				c = c + 1;
				sw_time_map.put(ch, c);
			}else{
				sw_time_map.put(ch, 1);
			}
		}
		return sw_time_map;
	}
	public static Map<String, Integer> getSwCountInfo(String userSwList){
		if (null == userSwList || userSwList.isEmpty()) {
			return null;
		}
		
		Map<String, Integer> sw_time_map = new HashMap<String, Integer>();
		long now = System.currentTimeMillis()/1000;
		String[] swword_array = userSwList.split("\\$");
		for (String tempSw : swword_array) {
			String[] tempBookSplit = tempSw.split("#");
			if (2 != tempBookSplit.length) {
				continue;
			}
			if((now - Long.parseLong(tempBookSplit[1])) > 15*24*3600){
				continue;
			}
			if(sw_time_map.containsKey(tempBookSplit[0])){
				int c = sw_time_map.get(tempBookSplit[0]);
				c = c + 1;
				sw_time_map.put(tempBookSplit[0], c);
			}else{
				sw_time_map.put(tempBookSplit[0], 1);
			}
		}
		/*String topValue = "";
		for(String sw : swword_array){
			String[] tempsw = sw.split("#");
			if (2 != tempsw.length) {
				continue;
			}
			if(topValue.contains(tempsw[0]))
				continue;
			if(sw_time_map.get(tempsw[0]) == null)
				continue;
			if(HotWordUtil.ch_tag.get(tempsw[0]) == null)
				continue;
			topValue += HotWordUtil.ch_tag.get(tempsw[0]) + "_" + sw_time_map.get(tempsw[0]) + "#";
		}
		if (topValue.endsWith("#")) {
			String newTopValue = topValue.substring(0, topValue.length() - 1);
			return newTopValue;
		}*/
		
		return sw_time_map;
	}
	public static String getConfirmSlideT1(String slidet1,String slidetags,String t1){
		if(slidet1 == null && t1 == null)
			return null;
		Map<String,Integer> tag_count = new HashMap<String,Integer>();
		Map<String,Double> tag_score = new HashMap<String,Double>();
		if(slidet1 != null){
			String[] slidet1_array = slidet1.split("#");
			for(String slide : slidet1_array){
				String[] tempslide = slide.split("_");
				if(!slidetags.contains(tempslide[0]))
					continue;
				if(tag_count.containsKey(tempslide[0])){
					int tempcount = tag_count.get(tempslide[0]);
					tag_count.put(tempslide[0], tempcount+Integer.parseInt(tempslide[1]));
				}else{
					tag_count.put(tempslide[0], Integer.parseInt(tempslide[1]));
				}
				if(tag_score.containsKey(tempslide[0])){
					double tempscore = tag_score.get(tempslide[0]);
					tag_score.put(tempslide[0], tempscore+Double.parseDouble((tempslide[2])));
				}else{
					tag_score.put(tempslide[0], Double.parseDouble((tempslide[2])));
				}
			}
		}
		if(t1 != null){
			String[] t1_array = t1.split("#");
			for(String a : t1_array){
				String[] tempa = a.split("_");
				if(!slidetags.contains(tempa[0]))
					continue;
				if(tag_count.containsKey(tempa[0])){
					int tempcount = tag_count.get(tempa[0]);
					tag_count.put(tempa[0], tempcount+Integer.parseInt(tempa[1]));
				}else{
					tag_count.put(tempa[0], Integer.parseInt(tempa[1]));
				}
				if(tag_score.containsKey(tempa[0])){
					double tempscore = tag_score.get(tempa[0]);
					tag_score.put(tempa[0], tempscore+Double.parseDouble((tempa[2])));
				}else{
					tag_score.put(tempa[0], Double.parseDouble((tempa[2])));
				}
			}
		}
		Map<String, Double> sortWordWeightMap = commenFuncs.sortMapByValues(tag_score, "descend");

		
		DecimalFormat df = new DecimalFormat("#0.00");
		StringBuffer sbTmp = new StringBuffer();
		for (String tempWord : sortWordWeightMap.keySet()) {
			if(sortWordWeightMap.get(tempWord) < 0.1)
				continue;
			if(tag_count.get(tempWord) < 2)
				continue;
			sbTmp.append(tempWord).append("_")
			.append(tag_count.get(tempWord)).append("_")
			.append(df.format(sortWordWeightMap.get(tempWord))).append("#");
		}
		return sbTmp.toString();
	}
	public static String getConfirmT1(String t1){
		if(t1 == null)
			return null;
		StringBuffer res = new StringBuffer();
		int listener = 2;
		try{
			List<String> t = Arrays.asList(t1.split("#"));
			String[] first = t.get(0).split("_");
			int top = Integer.parseInt(first[1]);
			if(top > 100){
				listener = 5;
			}else if(top > 15){
				listener = 3;
			}
			for(String taginfo : t){
				if(Integer.parseInt(taginfo.split("_")[1]) < listener)
					continue;
				res.append(taginfo).append("#");
			}
		}catch(Exception e){
			return t1;
		}
		return res.toString();
	}
	public static String getConfirmT2(String t2,String es){
		if(t2 == null)
			return null;
		StringBuffer res = new StringBuffer();
		int listener = 2;
		try{
			List<String> t = Arrays.asList(t2.split("#"));
			String[] first = t.get(0).split("_");
			int top = Integer.parseInt(first[1]);
			if(top > 50){
				listener = 7;
			}else if(top > 15){
				listener = 4;
			}else if(top < 3){
				listener = 1;
			}
			for(String taginfo : t){
				if(Integer.parseInt(taginfo.split("_")[1]) < listener || Double.parseDouble(taginfo.split("_")[2]) < 1.0)
					continue;
				res.append(taginfo).append("#");
			}
			if(es != null){
				for(String e : es.split("#")){
					String[] temp = e.split("_");
					if(temp.length != 2)
						continue;
					if(Double.parseDouble(temp[1]) > 0.5){
						res.append(temp[0]).append("_5_3.0").append("#");
					}
				}
			}
		}catch(Exception e){
			return t2;
		}
		
		return res.toString();
	}
	public static String getConfirmT3(String t3){
		if(t3 == null)
			return null;
		StringBuffer res = new StringBuffer();
		int listener = 3;
		try{
			List<String> t = Arrays.asList(t3.split("#"));
			String[] first = t.get(0).split("_");
			int top = Integer.parseInt(first[1]);
			if(top < 6){
				listener = 2;
			}
			for(String taginfo : t){
				if(Integer.parseInt(taginfo.split("_")[1]) < listener)
					continue;
				if(Double.parseDouble(taginfo.split("_")[2]) < 0.6)
					continue;
				res.append(taginfo).append("#");
			}
		}catch(Exception e){
			return t3;
		}
		return res.toString();
	}
	public static String getConfirmInfo2(String infoList,String swopenStr){
		if(null == infoList || infoList.isEmpty()){
			return null;
		}
		StringBuffer sb = new StringBuffer();
		Map<String,Double> tempres = new HashMap<String,Double>();
		Set<String> tag_now = new HashSet<String>();
		
		long now = System.currentTimeMillis()/1000;
		
		Map<String, Integer> sw_time_map = new HashMap<String, Integer>();
		Map<String, Long> sw_lasttime = new HashMap<String,Long>();
		
		String[] swword_array = swopenStr.split("\\$");
		for (String tempSw : swword_array) {
			String[] tempBookSplit = tempSw.split("#");
			if (2 != tempBookSplit.length) {
				continue;
			}
			if(sw_lasttime.containsKey(tempBookSplit[0])){
				long temp =  sw_lasttime.get(tempBookSplit[0]);
				if(Long.parseLong(tempBookSplit[1]) > temp)
					sw_lasttime.put(tempBookSplit[0], Long.parseLong(tempBookSplit[1]));
			}else{
				sw_lasttime.put(tempBookSplit[0], Long.parseLong(tempBookSplit[1]));
			}
			if(sw_time_map.containsKey(tempBookSplit[0])){
				int c = sw_time_map.get(tempBookSplit[0]);
				c = c + 1;
				sw_time_map.put(tempBookSplit[0], c);
			}else{
				sw_time_map.put(tempBookSplit[0], 1);
			}
		}
		String[] word_array = infoList.split("\\$");
		for(String tempword : word_array){
			String[] tempwordsplit = tempword.split("#");
			if(2 != tempwordsplit.length)
				continue;
			if(sb.toString().contains(tempwordsplit[0]))
				continue;
			if((now - Long.parseLong(tempwordsplit[1])) < 3*24*3600){
				sb.append(tempwordsplit[0]).append("#");
			}else{
				if(sw_lasttime.get(tempwordsplit[0]) != null ){
					if((now - sw_lasttime.get(tempwordsplit[0])) < 7*24*3600){
						sb.append(tempwordsplit[0]).append("#");
					}
				}
			}
			
			
		}
		return sb.toString();
	}
	public static String joinLoginInterest(String loginInterest,String userInterest,String last7daysStr){
		if(loginInterest == null || loginInterest.trim().equals(""))
			return userInterest;
		StringBuffer sb = new StringBuffer();
		Set<String> readtags = new HashSet<String>();
		if(last7daysStr != null){
			String[] temp = last7daysStr.split("#");
			for(String info : temp){
				String[] tag = info.split("_");
				if(tag.length != 3)
					continue;
				readtags.add(tag[0]);
			}
		}
		
		String[] login_interest = loginInterest.split("#");
		if(userInterest != null){
			sb.append(userInterest);
			if(!userInterest.endsWith("#"))
				sb.append("#");
		}
		for(String li : login_interest){
			String[] templi = li.split("_");
			if(!readtags.contains(templi[0]))
				continue;
			if(userInterest != null && userInterest.contains(templi[0]))
				continue;
			sb.append(templi[0]).append("#");
		}
		return sb.toString();
	}
	public static String getConfirmInfo(String infoList,Map<String,Integer> swopen){
		if(null == infoList || infoList.isEmpty()){
			return null;
		}
		Map<String,Double> tempres = new HashMap<String,Double>();
		Map<String,Double> tempres_now = new HashMap<String,Double>();
		Set<String> tag_now = new HashSet<String>();
		double timeW = 0.1;
		
		long now = System.currentTimeMillis()/1000;
		double score = 0;
		String[] word_array = infoList.split("\\$");
		for(String tempword : word_array){
			double swopenW = 1;
			String[] tempwordsplit = tempword.split("#");
			if(2 != tempwordsplit.length)
				continue;
			if((now - Long.parseLong(tempwordsplit[1])) > 7*24*3600){
				timeW = 0.8;
			}else if((now - Long.parseLong(tempwordsplit[1])) > 3*24*3600){
				timeW = 1.0;
			}else{
				tag_now.add(tempwordsplit[0]);
				timeW = 1.5;
			}
			if(swopen != null && swopen.get(tempwordsplit[0]) != null){
				swopenW = 2.5*swopen.get(tempwordsplit[0]);
			}
			score = timeW * swopenW;
			/*if((now - Long.parseLong(tempwordsplit[1])) > 15*24*3600){
				continue;
			}*/
			if((now - Long.parseLong(tempwordsplit[1])) < 3*24*3600){
				if(tempres_now.containsKey(tempwordsplit[0])){
					double temp = tempres_now.get(tempwordsplit[0]);
					tempres_now.put(tempwordsplit[0], temp+score);
				}else{
					tempres_now.put(tempwordsplit[0], score);
				}
			}else{
				if(tempres.containsKey(tempwordsplit[0])){
					double temp = tempres.get(tempwordsplit[0]);
					tempres.put(tempwordsplit[0], temp+score);
				}else{
					tempres.put(tempwordsplit[0], score);
				}
			}
			
		}
		ArrayList<Entry<String, Double>> maplist = new ArrayList(tempres.entrySet());
		Collections.sort(maplist, new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> arg0,
					Entry<String, Double> arg1) {
				// TODO Auto-generated method stub
				double result = 0;
				try{
					result = arg0.getValue().compareTo(arg1.getValue());
				}catch(Exception e){
					//解析异常
					e.printStackTrace();
				}
				if(result > 0)
					return -1;
				else if(result == 0)
					return 0;
				else
					return 1;
			}

		});
		Map<String,Double> sortres_now = commenFuncs.sortMapByValues(tempres_now, "descend");
		
		StringBuffer sb = new StringBuffer();
		int count = 0;
		if(sortres_now != null){
			for(String tempn : sortres_now.keySet()){
				sb.append(tempn).append("#");
				count++;
				break;
			}
		}
		
		for(Entry<String, Double> tempword : maplist){
			String temp = tempword.getKey();
			if(sb.toString() != null && sb.toString().contains(temp)){
				continue;
			}
			if(swopen == null || swopen.get(temp) == null)
				continue;
			if(swopen.get(temp) < 2)
				continue;
			count++;
			if(count == maplist.size() || count == 2){
				sb.append(temp);
				break;
			}
			sb.append(temp).append("#");
		}
		return sb.toString();
	}
	public static String getEInfo(List<String> wtList){
		if (null == wtList || wtList.isEmpty()) {
			return null;
		}
		String res = "";
		
		Map<String,Double> word_count = new HashMap<String,Double>();
		for(String wt : wtList){
			double timeW = 1.0;
			double refW = 1.0;
			String[] wordTime = wt.split("#");
			if(wordTime.length != 3)
				continue;
			if(wordTime[2].equals("topic")){
				refW = 5.0;
			}else if(wordTime[2].equals("sy")){
				refW = 1.0;
			}else{
				refW = 3.0;
			}
			/*if(Long.parseLong(wordTime[1]) > 7*24*3600){
				timeW = 0.3;
			}else if(Long.parseLong(wordTime[1]) > 3*24*3600){
				timeW = 0.7;
			}else{
				timeW = 1.0;
			}*/
			if(word_count.containsKey(wordTime[0])){
				double temp = word_count.get(wordTime[0]);
				temp = temp + timeW*refW;
				word_count.put(wordTime[0], temp);
			}else{
				word_count.put(wordTime[0], timeW*refW);
			}
			
		}
		ArrayList<Entry<String, Double>> maplist = new ArrayList(word_count.entrySet());
		Collections.sort(maplist, new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> arg0,
					Entry<String, Double> arg1) {
				// TODO Auto-generated method stub
				double result = 0;
				try{
					result = arg0.getValue()- arg1.getValue();
				}catch(Exception e){
					//解析异常
					e.printStackTrace();
				}
				if(result > 0)
					return -1;
				else if(result == 0)
					return 0;
				else
					return 1;
			}

		});
		try{
			DecimalFormat df = new DecimalFormat("#0.00");
			int count = 0;
			for(Entry<String, Double> tempword : maplist){
				count++;
				if(count == maplist.size() || count == 3){
					res = res + tempword.getKey() + "#";
					break;
				}
				res = res + tempword.getKey() + "#";
			}
			if (res.endsWith("#")) {
				res = res.substring(0, res.length() - 1);
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return res;
	}
	public static String getSInfo(List<String> wtList){
		if (null == wtList || wtList.isEmpty()) {
			return null;
		}
		String res = "";
		Map<String,Integer> word_count = new HashMap<String,Integer>();
		long now = System.currentTimeMillis()/1000;
		for(String wt : wtList){
			String[] wordTime = wt.split("#");
			if(wordTime.length != 2)
				continue;
			if((now - Long.parseLong(wordTime[1])) > 10*24*3600){
				continue;
			}
			if(word_count.containsKey(wordTime[0])){
				int temp = word_count.get(wordTime[0]);
				temp = temp + 1;
				word_count.put(wordTime[0], temp);
			}else{
				word_count.put(wordTime[0], 1);
			}
			
		}
		Map<String,Integer> maplist = commenFuncs.sortMapByValues(word_count, "descend");
		for(String word : maplist.keySet()){
			if(maplist.get(word) < 5)
				continue;
			res = res + word + "#";
		}
		if (res.endsWith("#")) {
			res = res.substring(0, res.length() - 1);
			
		}
		return res;
	}
	/**
	 * @param topic1_word_score 
	 * @Title: getLatestInfo
	 * @Description: 用户最近看过的信息，为了准确度考虑，保留用户最近几天看的、得分最高的几个分类表达即可
	 * @author likun
	 * @param wordTimeMap  词-->时间(example  2015-06-24+16:10:26)
	 * @param day 最近多少天，取lastview这几天的数据
	 * 注意：
	 * topic_word_score是用户一段时间所有topic的综合得分，不是用户最近行为的综合得分；所以这个得分最高，意味着鼓励用户以往看过的同时最近看过的行为
	 * 
	 * 
	 * @return
	 * @throws
	 */
	public static String getLatestInfo(Map<String, String> wordTimeMap, HashMap<String, Double> hm_word2score, int day, int topNum) {
		if (null == wordTimeMap || wordTimeMap.isEmpty() || day <= 0) {
			return null;
		}
		if(hm_word2score == null)
			hm_word2score = new HashMap<String, Double>();
		
		
		// 先转换一下时间格式，由example:2015-06-24+16:10:26转为long型
		Map<String, Double> word_score = new HashMap<String, Double>();
		for (String tempWord : wordTimeMap.keySet()) {
			String tempTime = wordTimeMap.get(tempWord);
			Long tempTime_long = Long.valueOf(tempTime);
			
			if(tempTime_long < (System.currentTimeMillis()-day*24*3600*1000)/1000)
				continue;
			//查询得分
			Double weight = hm_word2score.get(tempWord);
			if(weight == null)
				weight = 0.1;
			
			word_score.put(tempWord, weight);
		}
		
		Map<String, Double> word_timelong_sort = new HashMap<String, Double>();
		word_timelong_sort = commenFuncs.sortMapByValues(word_score, "descend");
		
		int topSize = topNum;
		if (word_timelong_sort.size() <= topNum) {
			topSize = word_timelong_sort.size();
		}
		
		String topValue = "";
		int topCount = 0;
		for (String tempWord : word_timelong_sort.keySet()) {
			if (topCount != topSize) {
				topValue += tempWord + "#";
			} else {
				break;
			}
			
			topCount++;
		}
		
		if (topValue.endsWith("#")) {
			String newTopValue = topValue.substring(0, topValue.length() - 1);
			return newTopValue; 
		}
		
		return topValue;
	}
	
	/**
	 * @Title: getTopWord
	 * @Description: 对word list进行count并排序,并取top n
	 * 注意：
	 * 		只对c1级别的分类做idf计算；
	 * 		后续可以将c的idf也转移计算到其它sc等维度上；
	 * 		从业务理解来看，似乎c级别的idf才是有意义的？
	 * @author liu_yi
	 * @param topic_level t1/t2/t3
	 * @param wordList
	 * @param wordImp
	 * @param topNum
	 * @param hm_word2score 传出重要性得分给后续其它业务
	 * @return
	 * @throws
	 */
	public static String getTopWord(String topic_level,List<String> wordList, List<Double> wordScore, HashMap<String, Double> hm_word2score,
			int topNum, String debugFeature, List<String> dislike) {
		if (null == wordList || wordList.isEmpty()) {
			return null;
		}
		
		Map<String, Integer> counterMap = new HashMap<String, Integer>();
		// 得分(重要性)值求和
		for (int i = 0; i != wordList.size(); i++) {			
			if (counterMap.containsKey(wordList.get(i))) {
				int oldCount = counterMap.get(wordList.get(i));
				int newCount = oldCount + 1;
				counterMap.put(wordList.get(i), newCount);
			} else {
				counterMap.put(wordList.get(i), 1);
			}
			
			if (hm_word2score.containsKey(wordList.get(i))) {
				double oldImpValue = hm_word2score.get(wordList.get(i));
				double newImpValue = oldImpValue + wordScore.get(i);
				hm_word2score.put(wordList.get(i), newImpValue);
			} else {
				hm_word2score.put(wordList.get(i), wordScore.get(i));
			}
		}
		if(dislike != null){
			for(String tag : dislike){
				Double score = hm_word2score.get(tag);
				if(score == null || score <= 0){
					continue;
				}
				if(topic_level.equals("t1")){
					score = score * 0.7;
				}else if(topic_level.equals("t2")){
					score = score * 0.5;
				}else if(topic_level.equals("t3")){
					score = score * 0.3;
				}
				hm_word2score.put(tag, score);
			}
		}
		
		
////将IDF环节放入单篇文章中去算了，否则无法做c_idf_value的同文章表达传递			
//		// 西格玛(重要性值*tf-idf)
//		Map<String, Double> word_weight_map = new HashMap<String, Double>();
//		for (String tempWord : impSumMap.keySet()) {
//			double temp_imp_value_sum = impSumMap.get(tempWord);
//			double temp_idf_value = 1.0f;
////将IDF环节放入单篇文章中去算了，否则无法做c_idf_value的同文章表达传递			
////			if(topic_level != null && topic_level.equals("t1"))
////				temp_idf_value = idfQueryInterface.getInstance().queryIdfValue(tempWord);
////
////			
////			if (tempWord.equals(debugFeature)) {
////				System.out.println("temp_imp_value_sum:" + temp_imp_value_sum);
////				System.out.println("temp_idf_value:" + temp_idf_value);
////			}
//
//			double temp_weight =  temp_imp_value_sum * temp_idf_value;
//			word_weight_map.put(tempWord, temp_weight);
//		}
		
		// 词-->weight map排序
		Map<String, Double> sortWordWeightMap = commenFuncs.sortMapByValues(hm_word2score, "descend");
		int topSize = topNum;
		if (sortWordWeightMap.size() <= topNum) {
			topSize = sortWordWeightMap.size();
		}
		
		String topValue = "";
		
		
		DecimalFormat df = new DecimalFormat("#0.00");
		
		int topCount = 0;
		StringBuffer sbTmp = new StringBuffer();
		for (String tempWord : sortWordWeightMap.keySet()) {
			if(debugFeature.equals("last") && counterMap.get(tempWord) < 5)
				continue;
			if(sortWordWeightMap.get(tempWord) < 0.05)
				continue;
			if (topCount != topSize) {
				sbTmp.append(tempWord).append("_")
						.append(counterMap.get(tempWord)).append("_")
						.append(df.format(sortWordWeightMap.get(tempWord))).append("#");
			} else {
				break;
			}
			topCount++;
		}
		
		topValue = sbTmp.toString();
		
		if (topValue.endsWith("#")) {
			String newTopValue = topValue.substring(0, topValue.length() - 1);
			return newTopValue; 
		}
		
		return topValue;
	}
	
	public static String getVidTopWord(String topic_level, String userReadVideo, List<String> wordList, List<Double> wordScore, HashMap<String, Double> hm_word2score,
			int topNum, String debugFeature, List<String> dislike) {
		if (null == wordList || wordList.isEmpty()) {
			return null;
		}
		
		Map<String, Integer> counterMap = new HashMap<String, Integer>();
		// 得分(重要性)值求和
		for (int i = 0; i != wordList.size(); i++) {			
			if (counterMap.containsKey(wordList.get(i))) {
				int oldCount = counterMap.get(wordList.get(i));
				int newCount = oldCount + 1;
				counterMap.put(wordList.get(i), newCount);
			} else {
				counterMap.put(wordList.get(i), 1);
			}
			
			if (hm_word2score.containsKey(wordList.get(i))) {
				double oldImpValue = hm_word2score.get(wordList.get(i));
				double newImpValue = oldImpValue + wordScore.get(i);
				hm_word2score.put(wordList.get(i), newImpValue);
			} else {
				hm_word2score.put(wordList.get(i), wordScore.get(i));
			}
		}
		if(dislike != null){
			for(String tag : dislike){
				Double score = hm_word2score.get(tag);
				if(score == null || score <= 0){
					continue;
				}
				if(topic_level.equals("t1")){
					score = score * 0.7;
				}else if(topic_level.equals("t2")){
					score = score * 0.5;
				}else if(topic_level.equals("t3")){
					score = score * 0.3;
				}
				hm_word2score.put(tag, score);
			}
		}
		
		String[] vid_array = userReadVideo.split("\\$");
		for (String tempVid : vid_array) {
			String[] tempVidSplit = tempVid.split("#");
			if (3 != tempVidSplit.length) {
				continue;
			}
			
			FeatureWordStatistics video_fs = new FeatureWordStatistics("ur", tempVid);
			video_fs.wordSta(debugFeature);
			List<String> temp_wordlist = null;
			List<Double> temp_wordscore = null;
			if(topic_level.equals("t1")){
				temp_wordlist = video_fs.getTopic1_word_List();
				temp_wordscore = video_fs.getTopic1_word_score();
			} else if(topic_level.equals("t2")){
				temp_wordlist = video_fs.getTopic2_word_List();
				temp_wordscore = video_fs.getTopic2_word_score();
			} else if(topic_level.equals("t3")){
				temp_wordlist = video_fs.getTopic3_word_List();
				temp_wordscore = video_fs.getTopic3_word_score();
			}
				
			if(!temp_wordlist.isEmpty()){
				String dua = tempVidSplit[1];
				if(tempVidSplit[1].contains("yn"))
					dua = tempVidSplit[1].split("&")[0];
									
				Double tempVidDuration = 0.0;
				try{
					tempVidDuration = Double.valueOf(dua);
				} catch (NumberFormatException e) {
					tempVidDuration = 30.0;
				}
				
				for(String tag : temp_wordlist){
					Double score = hm_word2score.get(tag);
					if(score == null || score <= 0){
						continue;
					}
					
					if(tempVidDuration < 10) {
						score = score * 0.6;
					} else if(10 <= tempVidDuration && tempVidDuration < 30){
						score = score * 0.8;
					} else if(30 <= tempVidDuration && tempVidDuration < 60){
						score = score * 1;
					} else if(60 <= tempVidDuration)
						score = score * 1.2;
					
					hm_word2score.put(tag, score);
				}
				
			}			
		}
		
		// 词-->weight map排序
		Map<String, Double> sortWordWeightMap = commenFuncs.sortMapByValues(hm_word2score, "descend");
		int topSize = topNum;
		if (sortWordWeightMap.size() <= topNum) {
			topSize = sortWordWeightMap.size();
		}
		
		String topValue = "";
		
		
		DecimalFormat df = new DecimalFormat("#0.00");
		
		int topCount = 0;
		StringBuffer sbTmp = new StringBuffer();
		for (String tempWord : sortWordWeightMap.keySet()) {
			if(debugFeature.equals("last") && counterMap.get(tempWord) < 5)
				continue;
			if(sortWordWeightMap.get(tempWord) < 0.05)
				continue;
			if (topCount != topSize) {
				sbTmp.append(tempWord).append("_")
						.append(counterMap.get(tempWord)).append("_")
						.append(df.format(sortWordWeightMap.get(tempWord))).append("#");
			} else {
				break;
			}
			topCount++;
		}
		
		topValue = sbTmp.toString();
		
		if (topValue.endsWith("#")) {
			String newTopValue = topValue.substring(0, topValue.length() - 1);
			return newTopValue; 
		}
		
		return topValue;
	}
	
	
	/**
	 * @Title: getUpdateUserID
	 * @Description: 获取有更新的用户ID集合
	 * @author liu_yi
	 * @return
	 * @throws
	 */
	public static ArrayList<String> getUpdateUserID() {
		ArrayList<String> al_updateUserIDs = new ArrayList<String>();
		Set<String> updateUserIDSet = null;
		Jedis jedis = null;
		try {
			// 将放入redis的db 13中，方便前端业务逻辑层接口调用
			jedis = new Jedis(GlobalParams.updateUserSetRedisIP/*"10.32.21.62"*/, 6379, 10000);//GlobalParams.updateUserSetRedisIP
			jedis.select(GlobalParams.updateUserSetDBnum);//GlobalParams.updateUserSetDBnum
			updateUserIDSet  = jedis.keys("*");
			//updateUserIDSet.clear();
			//test,debug用户			
			//updateUserIDSet.add("A0000059A2230D");
			//updateUserIDSet.add("A0000059807347");
			al_updateUserIDs.addAll(updateUserIDSet);
		} catch (Exception e) {
			LOG.error("ERROR", e);
			jedis.disconnect();
			return null;
		}
		jedis.disconnect();
		return al_updateUserIDs;
	}
		
	/**
	 * @Title: getAllUserID
	 * @Description: 获取用户日志中心的所有uid;这个从hdfs中获取
	 * @author likun
	 * @return
	 * @throws
	 */
	public static ArrayList<String> getAllUserID() {
		return null;
	}
	
	public static String getTopFromMap(Map<String,String> wordtime){
		if(wordtime == null)
			return null;
		ArrayList<Entry<String, String>> maplist = new ArrayList(wordtime.entrySet());
		Collections.sort(maplist, new Comparator<Entry<String, String>>() {
			@Override
			public int compare(Entry<String, String> arg0,
					Entry<String, String> arg1) {
				// TODO Auto-generated method stub
				double result = 0;
				try{
					result = arg0.getValue().compareTo(arg1.getValue());
				}catch(Exception e){
					//解析异常
					e.printStackTrace();
				}
				if(result > 0)
					return -1;
				else if(result == 0)
					return 0;
				else
					return 1;
			}

		});
		StringBuffer sb = new StringBuffer();
		int count = 1;
		for(Entry<String, String> tempword : maplist){
			String temp = tempword.getKey();
			if(count == maplist.size() || count == 20){
				sb.append(temp);
				break;
			}
			sb.append(temp).append("#");
		}
		return sb.toString();
	}
	public static String getTopCountFromMap(Map<String,String> wordtime){
		if(wordtime == null)
			return null;
		ArrayList<Entry<String, String>> maplist = new ArrayList(wordtime.entrySet());
		Collections.sort(maplist, new Comparator<Entry<String, String>>() {
			@Override
			public int compare(Entry<String, String> arg0,
					Entry<String, String> arg1) {
				// TODO Auto-generated method stub
				double result = 0;
				try{
					result = arg0.getValue().compareTo(arg1.getValue());
				}catch(Exception e){
					//解析异常
					e.printStackTrace();
				}
				if(result > 0)
					return -1;
				else if(result == 0)
					return 0;
				else
					return 1;
			}

		});
		StringBuffer sb = new StringBuffer();
		int count = 1;
		for(Entry<String, String> tempword : maplist){
			String temp = tempword.getKey();
			if(count == maplist.size() || count == 20){
				sb.append(temp);
				break;
			}
			sb.append(temp).append("#");
		}
		return sb.toString();
	}
	public static String getTopCountFromString(String words){
		if(words == null)
			return null;
		Map<String,Integer> word_count = new HashMap<String,Integer>();
		String[] word_array = words.split("#");
		for(String word : word_array){
			if(word_count.containsKey(word)){
				int temp = word_count.get(word);
				temp = temp + 1;
				word_count.put(word, temp);
			}else{
				word_count.put(word, 1);
			}
		}
		ArrayList<Entry<String, Integer>> maplist = new ArrayList(word_count.entrySet());
		Collections.sort(maplist, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> arg0,
					Entry<String, Integer> arg1) {
				// TODO Auto-generated method stub
				double result = 0;
				try{
					result = arg0.getValue() - arg1.getValue();
				}catch(Exception e){
					//解析异常
					e.printStackTrace();
				}
				if(result > 0)
					return -1;
				else if(result == 0)
					return 0;
				else
					return 1;
			}

		});
		StringBuffer sb = new StringBuffer();
		int count = 1;
		for(Entry<String, Integer> tempword : maplist){
			String temp = tempword.getKey();
			count++;
			if(count == maplist.size() || count == 20){
				sb.append(temp).append("_").append(tempword.getValue());
				break;
			}
			sb.append(temp).append("_").append(tempword.getValue()).append("#");
		}
		return sb.toString();
	}
	/**
	 * @Title: flushRedisDB
	 * @Description: 清空db数据
	 * @author liu_yi
	 * @param dbNum
	 * @throws
	 */
	public static void flushRedisDB(int dbNum, List<String> al_allUpdatedUsers) {
		if (null == al_allUpdatedUsers || al_allUpdatedUsers.isEmpty()) {
			return;
		}
		
		try {
			// 将放入redis的db 13中，方便前端业务逻辑层接口调用
			Jedis jedis = new Jedis(GlobalParams.updateUserSetRedisIP/*"10.32.21.62"*/, 6379, 10000);//GlobalParams.updateUserSetRedisIP		
			jedis.select(dbNum);
			Pipeline pJedis = jedis.pipelined();
			try {
				int tmp_num = 0;
				for (String uid : al_allUpdatedUsers) {
					pJedis.del(uid);
					tmp_num++;
					if(tmp_num >=300000)
					{
						pJedis.syncAndReturnAll();
						tmp_num = 0;
					}
				}	
				pJedis.syncAndReturnAll();
			} catch (Exception ex) {
				LOG.error("redis del key error:", ex);
			} finally {
				
			}
		} catch (Exception e) {
			LOG.error("ERROR", e);
			return;
		}
	}
	public static void deleteRedis(int dbNum, String uid) {
		if (null == uid) {
			return;
		}
		
		try {
			// 将放入redis的db 13中，方便前端业务逻辑层接口调用
			Jedis jedis = new Jedis(GlobalParams.updateUserSetRedisIP/*"10.32.21.62"*/, 6379, 10000);//GlobalParams.updateUserSetRedisIP		
			jedis.select(dbNum);
			try {
				jedis.del(uid);
			} catch (Exception ex) {
				LOG.error("redis del key error:", ex);
			} finally {
				
			}
		} catch (Exception e) {
			LOG.error("ERROR", e);
			return;
		}
	}
	//根据手机型号获取手机品牌
	public static void phoneBrandMapInit(){
		Jedis jedis = null;
		try{
			jedis = new Jedis("10.90.1.58",6379,5000);
			jedis.select(3);
			phoneBrandMap = jedis.hgetAll("phoneBrands");
			
			jedis.select(4);
			Set<String> keys = jedis.keys("*");
	    	for(String key: keys){
	    		phonePriceMap.put(key, jedis.get(key));
	    	}
		}catch(Exception e){
			LOG.error("get user local error "+phoneVersion,e);
			jedis.disconnect();
			e.printStackTrace();
		}
		jedis.disconnect();
	}
}
