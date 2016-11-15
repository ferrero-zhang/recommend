package com.ifeng.iRecommend.kedm.usercenter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.kedm.userlog.UserInfoFromLog;
import com.ifeng.iRecommend.kedm.util.UserCenterIKVOperation;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate.FeatureWordStatistics;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate.UserCenterUpdateMain;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate.queryItemFeatures;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.GlobalParams;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.ItemIKVUtil;
import com.ifeng.ikvlite.IkvLiteClient;

public class UsercenterForerverUpdateMain {
	private static final Log LOG = LogFactory.getLog(UsercenterForerverUpdateMain.class);
	public static LinkedList<String> getUserids(String path){
		LinkedList<String> uids = new LinkedList<String>();
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				uids.add(line);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
		return uids;
	}
	public static void main(String[] args){
		LOG.info("start update usercenter forever...");
		ItemIKVUtil.init();
		
		LinkedList<String> uids = new LinkedList<String>();
		uids = getUserids(args[0]);
		//uids.add("6fd6435ad967e00a58224bb8894f22ee6afd1e2d");
		CountDownLatch countDownLatch_put = new CountDownLatch(1);
		PutThreadManager put_tm = new PutThreadManager("put_thread_manager",
				uids, countDownLatch_put);
		put_tm.start();
		
		try {
			countDownLatch_put.await();
		} catch (InterruptedException ex) {
			LOG.error("wait for get index tm error");
		}
		ItemIKVUtil.close();
		LOG.info("finish update ...");
	}
	public static void doTopic(String uid,List<String> days,IkvLiteClient client){
		List<String> features_t1 = new ArrayList<String>();
		List<String> features_t2 = new ArrayList<String>();
		List<String> features_t3 = new ArrayList<String>();
		TopicFalloff2 topicOff1 = new TopicFalloff2();
		TopicFalloff2 topicOff2 = new TopicFalloff2();
		TopicFalloff2 topicOff3 = new TopicFalloff2();
		String res1 = null;
		String res2 = null;
		String res3 = null;
		int count = 0;
		for(String day : days){
			count++;
			if(count == 8){
				res1 = topicOff1.proccess(features_t1);
				res2 = topicOff2.proccess(features_t2);
				res3 = topicOff3.proccess(features_t3);
				features_t1.clear();
				features_t2.clear();
				features_t3.clear();
				count = 1;
			}
			String key = uid + "_" + day;
			StringBuffer userOpenDocListStr = new StringBuffer();
			UserInfoFromLog userlog = UserCenterIKVOperation.getUserInfo(key, client);
			if(userlog == null)
				continue;
			Map<String,String> userOpenDoc = userlog.getOpen_doc_id_time_map();
			if(userOpenDoc != null){
				for (String tempKey : userOpenDoc.keySet()) {
					String openTimeAndRef = userOpenDoc.get(tempKey);
					if(null == openTimeAndRef)
						continue;
					userOpenDocListStr.append(tempKey).append("#").append(openTimeAndRef).append("$");
					
				}
			}
			FeatureWordStatistics temp_fs = new FeatureWordStatistics("ur", userOpenDocListStr.toString());
			temp_fs.wordSta("");
			HashMap<String, Double> hm_word2score = new HashMap<String, Double>();
			String topic1_top_value = UserCenterUpdateMain.getTopWord("t1",temp_fs.getTopic1_word_List(), temp_fs.getTopic1_word_score(),hm_word2score, GlobalParams.topAskNum, "",null);
			hm_word2score.clear();
			String topic2_top_value = UserCenterUpdateMain.getTopWord("t2",temp_fs.getTopic2_word_List(), temp_fs.getTopic2_word_score(),hm_word2score, GlobalParams.topAskNum, "",null);
			hm_word2score.clear();
			String topic3_top_value = UserCenterUpdateMain.getTopWord("t3",temp_fs.getTopic3_word_List(), temp_fs.getTopic3_word_score(),hm_word2score, GlobalParams.topAskNum, "",null);
			
			if(topic1_top_value == null || topic2_top_value == null || topic3_top_value == null)
				continue;
			
			if(count <= 7){
				features_t1.add(topic1_top_value);
				features_t2.add(topic2_top_value);
				features_t3.add(topic3_top_value);
			}
			
		}
		
		LOG.info(Thread.currentThread().getName() + " " + uid + " t1 result is "+getSort(res1));
		LOG.info(Thread.currentThread().getName() + " " + uid + " t2 result is "+getSort(res2));
		LOG.info(Thread.currentThread().getName() + " " + uid + " t3 result is "+getSort(res3));
	}
	
	public static String getSort(String info){
		StringBuffer sb = new StringBuffer();
		try{
			Map<String,Double> tempsort = new HashMap<String,Double>();
			Map<String,Double> tagscore = new HashMap<String,Double>();
			Map<String,Double> tagcount = new HashMap<String,Double>();
			DecimalFormat df = new DecimalFormat("#0");
			DecimalFormat df2 = new DecimalFormat("#0.00");
			for(String s : info.split("#")){
				String[] temp = s.split("_");
				tagscore.put(temp[0], Double.parseDouble(temp[2]));
				tagcount.put(temp[0], Double.parseDouble(temp[1]));
			}
			tempsort = commenFuncs.sortMapByValues(tagscore, "descend");
			
			for(String key : tempsort.keySet()){
				if(tagcount.get(key) < 3.0)
					continue;
				sb.append(key).append("_").append(df.format(tagcount.get(key))).append("_").append(df2.format(tempsort.get(key))).append("#");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	
	static class PutThreadManager extends Thread {
		private LinkedList<String> put_list = new LinkedList<String>();
		private int put_threads_count = 15;
		private CountDownLatch countDownLatch;

		public PutThreadManager(String thread_name,
				LinkedList<String> put_list, CountDownLatch countDownLatch) {
			super(thread_name);
			this.put_list = put_list;
			this.countDownLatch = countDownLatch;
		}

		public void run() {
			//long start_put = System.currentTimeMillis();
			try{
				doWork();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				countDownLatch.countDown();
				LOG.info("put thread manager count down");
			}
			
			
			//long end_put = System.currentTimeMillis();
			//LOG.info("duration of put put_list:" + (end_put - start_put));
		}

		public void doWork() {
			// 启动10个PutThread线程put put_list
			
				int s_index = 0;
				int e_index = 0;
				int put_list_size = put_list.size();
				int _put_threads_count = put_threads_count;
				LOG.info("size of put_list:" + put_list_size);
				if (_put_threads_count > put_list_size)
					_put_threads_count = put_list_size;
				CountDownLatch countDownLatch_put = new CountDownLatch(
						_put_threads_count);
				int p_unit = put_list_size / _put_threads_count;
			try{
				for (int i = 0; i < _put_threads_count; i++) {
					s_index = i * p_unit;
					if (i == _put_threads_count - 1)
						e_index = put_list_size;
					else
						e_index = s_index + p_unit;
					List<String> sub_put_list = put_list.subList(s_index, e_index);
					PutThread putThread = new PutThread("put_thread" + i,
							 sub_put_list, countDownLatch_put);
					putThread.start();
				}
			}catch(Exception e){
				LOG.error("put thread manage dowork error",e);
				e.printStackTrace();
			}
			
			try {
				countDownLatch_put.await();
			} catch (InterruptedException ex) {
				LOG.error(" put threads error", ex);
			}
		}
	}
	
	static class PutThread extends Thread {
		private List<String> put_list;
		private CountDownLatch countDownLatch;

		public PutThread(String thread_name,
				List<String> put_list, CountDownLatch countDownLatch) {
			super(thread_name);
			this.put_list = put_list;
			this.countDownLatch = countDownLatch;
		}

		public void run() {
			try{
				doWork();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				countDownLatch.countDown();
				LOG.info("put thread count down");
			}
		}
		public void doWork(){
			long end = System.currentTimeMillis();
			long start = end - 90*24*3600*1000L;
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
				e.printStackTrace();
			}
			Collections.reverse(days_keysSet);
			IkvLiteClient client = UserCenterIKVOperation.getIKVClient();
			for(String uid : put_list){
				long s =  System.currentTimeMillis();
				doTopic(uid,days_keysSet,client);
				LOG.info(uid + " finish update spend "+(System.currentTimeMillis()-s));
			}
			client.close();
			queryItemFeatures.clean();
		}
	}

}
