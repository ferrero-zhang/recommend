/**
 * 
 */
package com.ifeng.iRecommend.train;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.UrlPretreatment;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation.LogType;
import com.ifeng.iRecommend.usermodel.usermodelInterface;

/**
 * <PRE>
 * 作用 :
 * 	   兼容PC端和APP端两种情况； 
 *   负责定时训练，催动用户模型更新；定时执行（一周一次？）
 *   总体需要控制一个流程；当用户模型更新时，需要让模型层能获得通知；
 *   模型分新、旧；统一存放在/data/iRecommend/usermodel/目录下；
 *   硬盘标志位存入/data/iRecommend/usermodel/目录下，名叫status
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
public class usermodelTraining {
	private static final Log LOG = LogFactory.getLog("train");
	
	/*
	 * 读入model训练标志位
	 */
	public static String readStatus(){
		FileUtil fu = new FileUtil();
		String status = "";
		if (fu.Initialize(fieldDicts.modelPath+fieldDicts.trainStatusFile
				,"utf-8")) {
			try {
				String line = fu.ReadLine();
				if (line == null) {
					return "";
				}
				status = line;
			} catch (Exception e) {
				LOG.error("error:", e);
			} finally {
				fu.CloseRead();
			}
		}
		return status;
	}
	
	/*
	 * 读入所有测试user
	 */
	public static String[] readAllTestUsers(){
		//读入test users
		HashSet s_testusers = new HashSet<String>();
		FileUtil wordidFile = new FileUtil();
		wordidFile.Initialize(fieldDicts.testUsersFile, "UTF-8");
		String line = "";
		while ((line = wordidFile.ReadLine()) != null) {
			s_testusers.add(line.trim());
		}
		String[] ss_users = (String[])s_testusers.toArray(new String[0]);
		return ss_users;
	}
	
	
	/*
	 * 解析一个newsApp user的log行为集合；
	 * 进行行为解析、行为过滤、分页合并；
	 * 3分钟内同一个ID进行合并；识别分页，1分钟内同一个分页合并等工作；
	 * 最后返回按天的itemid-time序列。
	 * 
	 */
	public static HashMap<String, String> parseNewsAppUserDayLog(HashMap<String, String> hm_logs) {
		// TODO Auto-generated method stub
		HashMap<String, String> hm_dayitems = new HashMap<String, String>();
		if(hm_logs == null || hm_logs.isEmpty())
			return hm_dayitems;
		
		//临时缓存
		StringBuffer sbTmp = new StringBuffer(1024);
		
		//打散log，1分钟内的分页item进行合并
		Iterator<Entry<String, String>> it = hm_logs.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> et = it.next();
			
			//get one day
			HashMap<String,String> hm_tmpItems = new HashMap<String,String>();
			String str = et.getValue();
			if(str.isEmpty())
				continue;
			
			String sDay = et.getKey();
			
	//////////@test,sDay转换成毫秒级的时间，以解决夏龙那边儿的time字段丢失bug;示例：20140610
			long testTime = System.currentTimeMillis();
			{
				SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMdd");
				Date sDate = null;
				try {
					sDate = sDateFormat.parse(sDay);
					testTime = sDate.getTime(); 
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			
			String[] secs = str.split("!");
			if(secs == null)
				continue;
			/*
			 * 更准确的算法：
			 * time1\time2对item的time进行了不同评价，这样两个time只要有一个相同，就认为是相同时间内的行为，可以忽略
			 */
			long time1 = 0L,time2 = 0L;
			for(int i=0;i<secs.length;i++)
			{
				String sOneLog = secs[i];
				String[] secs1 = sOneLog.split("\t");
				if(secs1 == null)
					continue;
				
				String ip = "";
				String os = "";//操作系统或者设备
				String unknow = "";
				String specialWork = "";
				String stime = "";
				
				if(secs1.length == 5){
					ip = secs1[0].trim();
					os = secs1[1].trim();//操作系统或者设备
					unknow = secs1[2].trim();
					specialWork = secs1[3].trim();
					stime = secs1[4].trim();
		
				}
				
				//@Test，处理夏龙整出来的BUG，少写了一个time字段
				if(secs1.length == 4){
					ip = secs1[0].trim();
					os = secs1[1].trim();//操作系统或者设备
					unknow = secs1[2].trim();
					specialWork = secs1[3].trim();
					stime = String.valueOf(testTime);
					
					
				}
				
				if(secs1.length < 4){
					continue;
				}			
//				//@test
//				if(specialWork.indexOf("3A")>=0)
//					System.out.println(specialWork);
				
				
				long oldtime = time1;
				
				try{
					 long time = Long.valueOf(stime);
					 //换算下，几分钟内的可以合并；
					 time1 = time/fieldDicts.userLogIntelMinutes;
					 time2 = ((time+(long)(fieldDicts.userLogIntelMinutes*0.5f))/fieldDicts.userLogIntelMinutes);
				}catch(Exception e){
					LOG.error("error:"+stime,e);
					time1 = oldtime+1000L;
				}
				
				
				//过滤掉push行为;page#id=imcp_83168038$ref=push$type=article
				if(specialWork.indexOf("openpush")>=0
						|| specialWork.indexOf("ref=push")>0
						|| specialWork.indexOf("pushaccess")>=0)
					continue;
				
				//过滤掉无法识别行为;page#id=384$ref=sy$type=piclive
				if(specialWork.indexOf("type=piclive")>=0
						|| specialWork.indexOf("type=ad")>=0)
					continue;
				
				
				//寻找imcp_id
				//普通，比如：page#id=yz$ref=sy$type=set 
				//或者page#id=imcp_76561925$ref=sy$type=article
				//或者openpush#aid=78697596$type=doc
				//或者v#vid=imcp_78801170
				//或者page#id=imcp_77903053_8$ref=topic_IMCPchunwan$type=pic,带分页。。。
				String imcp_id = "";
				int b = specialWork.indexOf("id=");
				if (b >=0 ) {
					int e = specialWork.indexOf("$", b);
					if(e > b){
						imcp_id = specialWork.substring(b+3,e);
					}else{
						imcp_id = specialWork.substring(b+3);
					}
				}
				
				//特殊处理：如果是sportslive,变相处理下
				if(specialWork.indexOf("type=sportslive")>=0)
					imcp_id = "sports";
	
				
				//收藏分享，action#type=store/action#type=share，暂时无法处理
				//...
				
				//comment,比如：page#id=noid$ref=imcp_81501578$type=comment
				if(specialWork.indexOf("comment")>0)
				{
					b = specialWork.indexOf("ref=");
					if(b > 0)
					{
						int e = specialWork.indexOf("$",b);
						if(e > (b+4))
							imcp_id = specialWork.substring(b+4, e);
					}
				}

				
				if(imcp_id.isEmpty())
					continue;
				
				//修剪，并过滤掉分页
				if(imcp_id.startsWith("imcp_"))
					imcp_id = imcp_id.substring(5);
				//处理分页
				if((b=imcp_id.indexOf("_")) > 0){
					imcp_id = imcp_id.substring(0, b);
				}
				
				//根据ref，传递channel信息给后续处理;ch的不用
				String channel = "null";
				if(specialWork.indexOf("type=ch") <= 0
						&& specialWork.indexOf("comment") <= 0)
				{
					b = specialWork.indexOf("ref=");
					if(b > 0)
					{
						int e = specialWork.indexOf("$",b);
						if(e > (b+4))
							channel = specialWork.substring(b+4, e);
					}
				}
				
				//特殊处理：如果是video,变相处理下
				if(specialWork.indexOf("type=video")>=0)
					imcp_id = channel = "phtv";

				
//old			//url过滤掉一些特殊标记
//				if(url.indexOf("search.ifeng.com") > 0)
//					continue;
//				else if(url.indexOf("/book/ts/")>0)//小说行为先丢弃
//					continue;
//				//单页url，结尾：http://blog.ifeng.com/article/29463648.html和shtml结尾；		
//				if(url.indexOf(".shtml")>0){
//					int b = url.indexOf(".shtml");
//					url = url.substring(0,b+6);
//				}
//				if(url.indexOf(".html")>0){
//					int b = url.indexOf(".html");
//					url = url.substring(0,b+5);
//				}	
//				//url剔除分页因素
//				if(url.endsWith(".shtml")){//合并其它分页
//					url = url.replaceFirst("_\\d{1,2}.shtml","_0.shtml");
//				}
//		
//				
//				if(url.indexOf("#p=")>0){
//					url = url.replaceFirst("#p=\\d{1,2}", "");
//				}else if(url.indexOf("/book/ts/")>0){//小说行为先丢弃
//					continue;
//				}else if(url.indexOf("#pid=")>0){
//					url = url.replaceFirst("#pid=\\d{1,12}", "");
//				}else if(url.indexOf("#pic_view_top")>0){//blog分页阅读，丢弃分页点击行为
//					
//				}else if(url.indexOf("blog.ifeng.com")>0){//blog分页阅读，
//					
//				}else if(url.indexOf("auto.ifeng.com")>0){//汽车
//					
//				}else if(url.endsWith(".shtml")){//合并其它分页
//					url = url.replaceFirst("_\\d{1,2}.shtml","_0.shtml");
//				}

				sbTmp.delete(0, sbTmp.length());
				sbTmp.append(imcp_id).append("!").append(time1).append("!").append(time2).append("!").append(channel);
				String s_records = sbTmp.toString();
				if(!hm_tmpItems.containsKey(s_records)){
					hm_tmpItems.put(s_records, s_records);
				}
				
//				//@test
//				System.out.println(specialWork);
//				System.out.println(s_records);
//				System.out.println("--------");
				
			}
			
			//save in hash
			sbTmp.delete(0, sbTmp.length());
			Iterator<Entry<String, String>> it_tmp = hm_tmpItems.entrySet().iterator();
			while(it_tmp.hasNext()){
				String value = it_tmp.next().getValue();
				sbTmp.append(value).append(" ");
			}
			hm_dayitems.put(sDay, sbTmp.toString().trim());	
		}
		return hm_dayitems;
	}
	
	
	/*
	 * 解析一个user的log行为集合；
	 * 进行行为解析、行为过滤、分页合并；
	 * 3分钟内同一个ID进行合并；识别分页，1分钟内同一个分页合并等工作；
	 * 最后只留下按天的item-time序列。
	 * 	//分页样例1：http://news.ifeng.com/photo/hdsociety/detail_2013_07/18/27646976_0.shtml#p=2
	 * 	//分页样例2：http://v.book.ifeng.com/book/ts/38813/3475976.htm http://v.book.ifeng.com/book/ts/38813/3475977.htm
	 * 	//分页样例3：http://news.ifeng.com/history/zhongguogudaishi/detail_2013_07/18/27653699_0.shtml http://news.ifeng.com/history/zhongguogudaishi/detail_2013_07/18/27653699_1.shtml
	 *  http://data.auto.ifeng.com/pic/c-30995.html#pid=1783987 http://data.auto.ifeng.com/pic/c-30995.html#pid=1783988
	 */
	public static HashMap<String, String> parsePcUserDayLog(HashMap<String, String> hm_logs) {
		// TODO Auto-generated method stub
		HashMap<String, String> hm_dayitems = new HashMap<String, String>();
		if(hm_logs == null || hm_logs.isEmpty())
			return hm_dayitems;
		
		//临时缓存
		StringBuffer sbTmp = new StringBuffer(1024);
		
		//打散log，1分钟内的分页item进行合并
		Iterator<Entry<String, String>> it = hm_logs.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> et = it.next();
			
			//get one day
			HashMap<String,String> hm_tmpItems = new HashMap<String,String>();
			String str = et.getValue();
			if(str.isEmpty())
				continue;
			
			String sDay = et.getKey();
			
			String[] secs = str.split("!");
			if(secs == null)
				continue;
			long time = 0L;
			for(int i=0;i<secs.length;i++)
			{
				String sOneLog = secs[i];
				String[] secs1 = sOneLog.split("\t");
				if(secs1 == null || secs1.length != 3)
					continue;
				String url = secs1[0].trim();
				String ip = secs1[1].trim();
				String stime = secs1[2].trim();
				
				long oldtime = time;
				
				try{
					 time = Long.valueOf(stime);
					 //换算下，几分钟内的可以合并；
					 time = ((time+(long)(fieldDicts.userLogIntelMinutes*0.2f))/fieldDicts.userLogIntelMinutes);
				}catch(Exception e){
					LOG.error("error:"+stime,e);
					time = oldtime+1000L;
				}
				
				//comment,比如：http://comment.ifeng.com/view.php?docName=%E6%A2%A6%E9%B8%BD%EF%BC%9A%E6%9D%8E%E6%9F%90%E9%A5%AE%E9%85%92%E8%BF%87%E9%87%8F%20%E6%9D%A8%E6%9F%90%E4%B8%BA%E5%85%B6%E6%89%8B%E6%B7%AB%E4%B8%BB%E5%8A%A8%E4%BF%83%E6%88%90%E6%80%A7%E4%BA%A4%E6%98%93&docUrl=http%3A%2F%2Fent.ifeng.com%2Fidolnews%2Fspecial%2Flgf%2Fcontent-6%2Fdetail_2013_08%2F08%2F28407263_0.shtml&skey=ccdd09
				if(url.indexOf("comment")>0)
				{
					try {
						url = url.replaceAll("%", "%25");
						url = URLDecoder.decode(url, "utf-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						LOG.error("error:",e);
						continue;
					}catch (Exception e) {
						// TODO Auto-generated catch block
						LOG.error("error:",e);
						continue;
					}
				}
				
				//url提取
				int b_http = url.lastIndexOf("http");
				if(b_http >= 0){
					url = url.substring(b_http);
				}
				
				//url过滤掉一些特殊标记
				if(url.indexOf("search.ifeng.com") > 0)
					continue;
				else if(url.indexOf("/book/ts/")>0)//小说行为先丢弃
					continue;
				
				//单页url，结尾：http://blog.ifeng.com/article/29463648.html和shtml结尾；		
				if(url.indexOf(".shtml")>0){
					int b = url.indexOf(".shtml");
					url = url.substring(0,b+6);
				}
				if(url.indexOf(".html")>0){
					int b = url.indexOf(".html");
					url = url.substring(0,b+5);
				}
					
				//url剔除分页因素
				if(url.endsWith(".shtml")){//合并其它分页
					url = url.replaceFirst("_\\d{1,2}.shtml","_0.shtml");
				}
		
				
//				if(url.indexOf("#p=")>0){
//					url = url.replaceFirst("#p=\\d{1,2}", "");
//				}else if(url.indexOf("/book/ts/")>0){//小说行为先丢弃
//					continue;
//				}else if(url.indexOf("#pid=")>0){
//					url = url.replaceFirst("#pid=\\d{1,12}", "");
//				}else if(url.indexOf("#pic_view_top")>0){//blog分页阅读，丢弃分页点击行为
//					
//				}else if(url.indexOf("blog.ifeng.com")>0){//blog分页阅读，
//					
//				}else if(url.indexOf("auto.ifeng.com")>0){//汽车
//					
//				}else if(url.endsWith(".shtml")){//合并其它分页
//					url = url.replaceFirst("_\\d{1,2}.shtml","_0.shtml");
//				}
				sbTmp.delete(0, sbTmp.length());
				sbTmp.append(url).append("!").append(time);
				String s_records = sbTmp.toString();
				if(!hm_tmpItems.containsKey(s_records)){
					hm_tmpItems.put(s_records, s_records);
				}
				
			}
			
			//save in hash
			sbTmp.delete(0, sbTmp.length());
			Iterator<Entry<String, String>> it_tmp = hm_tmpItems.entrySet().iterator();
			while(it_tmp.hasNext()){
				String value = it_tmp.next().getValue();
				sbTmp.append(value).append(" ");
			}
			hm_dayitems.put(sDay, sbTmp.toString().trim());	
		}
		return hm_dayitems;
	}
	/**
	 * 加载新的usermodel；
	 * <p>重新加载入最新的usermodel；
	 * 如果加载有失败，那么就回滚到旧model；
	 * 同时还要加载docID到userID的map关系；
	 * </p>
	 * @return int 
	 * 		        0 加载成功；-1 没有加载成功，但是旧model仍然可用；<=-2 没有加载成功，而且旧model不可以用，系统无法提供计算能力；
	 * @throws Exception 
	 * 				如果是rollback failed\loadUserModel failed异常，表示旧model已经不可用同时新model加载出现异常，系统同样无法提供计算能力；
	 */
	public static int loadNewModel() throws Exception{
		LOG.info("try load new model");
		//判断标志位
		//删除旧model；备份旧model，替换；
		//重新载入
		//设置标志位
		String status = readStatus();
		if(status == null || status.isEmpty())
		{
			LOG.error("status not found");
			return -1;
		}

		if (!status.equals("canupdate")) {
			LOG.error("status not ok");
			return -1;
		}
		
		//判断new model存在与否
		File f = new File(fieldDicts.modelPath + fieldDicts.newModelFile);
		if(!f.exists()){
			LOG.warn("not found new model");		
			return -1;
		}
		
		//删除旧model's bak
		boolean res = commenFuncs.deleteDirectory(fieldDicts.modelPath + fieldDicts.bakModelFile);
		if (!res) {
			LOG.warn("can not delete old model's bak");
			LOG.warn("reload failed");
			return -1;
		}
		LOG.info("remove bak over");

		//备份旧model
		f = new File(fieldDicts.modelPath + fieldDicts.oldModelFile);
		if (f.exists()) {
			File dest = new File(fieldDicts.modelPath + fieldDicts.bakModelFile);
			if (f.renameTo(dest))
				LOG.info("bak over");
			else {
				LOG.warn("can not bak old model");
				return -1;
			}
		} else {
			LOG.warn("can not find old model");
			LOG.warn("reload failed");
			return -2;
		}

		//快速进行新旧库切换，可能导致相似度计算异常；
		f = new File(fieldDicts.modelPath + fieldDicts.newModelFile);
		File dest = new File(fieldDicts.modelPath + fieldDicts.oldModelFile);
		if (!f.renameTo(dest))
		{
			LOG.warn("rename new model failed");
			LOG.warn("reload failed,so rollback:");
			f = new File(fieldDicts.modelPath + fieldDicts.bakModelFile);
			dest = new File(fieldDicts.modelPath
					+ fieldDicts.oldModelFile);
			if (f.renameTo(dest)){
				res = usermodelInterface.getInstance("query").loadUserModel();
				if(!res){
					LOG.warn("rollback failed");
					throw new Exception("rollback failed");
				}else
					LOG.info("rollback success");
			}else {
				LOG.warn("rollback failed");		
			}
			return -3;
		}

		LOG.info("loadUserModel:");
		res = usermodelInterface.getInstance("query").loadUserModel();
		if(!res){
			LOG.warn("loadUserModel failed");
			throw new Exception("loadUserModel failed");
		}else
			LOG.info("loadUserModel success");
		// change status flag
		LOG.info("update status flag");
		commenFuncs.writeResult(fieldDicts.modelPath,
				fieldDicts.trainStatusFile, "updatedone", "utf-8", false, LOG);
		LOG.info("load over");
		//执行一次查询以生成user--doc的映射关系
		LOG.info("cmp user map to doc:");
		usermodelInterface.getInstance("query").cmpUser2DocMap();
		LOG.info("over");
		return 0;
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
//		fieldDicts.testUsersFile = "D:\\workspace\\iRecommend4App\\testenv\\appActiveUsers_5w_small.txt";

		fieldDicts.itemModelPath = "/data/irecommend4app/itemmodel/";
		fieldDicts.modelPath = "/data/irecommend4app/usermodel/";
		fieldDicts.appTreeMappingFile = "/data/irecommend4app/AppTreeMapping.txt";
		fieldDicts.frontAppTreeMappingFile = "/data/irecommend4app/APPFront_TreeMapping.txt";
		fieldDicts.stopwordsFile = "/data/irecommend4app/stopwords.txt";
		fieldDicts.tm_doc_dir = "/data/irecommend4app/tm/doc/";
		fieldDicts.tm_word_dir = "/data/irecommend4app/tm/word/";
		fieldDicts.tm_words_file = "/data/irecommend4app/tm/dict_topicmodel";
		fieldDicts.testUsersFile = "/data/irecommend4app/appActiveUsers_5w.txt";
		
		
		//限定在hbase中是newsapp的log和item table
		logDBOperation.setLogType(logDBOperation.LogType.APPLOG);
		
		//0.读取硬盘标志位，判断当前training状态：“建模完毕需要更新、建模正在进行、建模完毕更新完毕、停止后续建模”；并根据状态判断是否进行后续操作；
		//1.读入测试user list；
		//2.遍历测试user list，逐个user查询其3个月内的用户行为，调用模型层的建模接口建模；
		//如果发现training状态是“停止后续建模”，则退出建模步骤；
		//3.建模完毕，得到新模型；旧模型被替换，并硬盘标记，通知模型层载入新模型；
		String status = readStatus();
		if(status == null || status.isEmpty())
		{
			LOG.error("status not found");
			return;
		}
		
		if(status.equals("training")
				||status.equals("canupdate")
				||status.equals("stop")){
			LOG.info("status is "+status);
			return;
		}
		
		//change status flag
		LOG.info("update status flag");
		commenFuncs.writeResult(fieldDicts.modelPath, fieldDicts.trainStatusFile, "training", "utf-8", false, LOG);
		
		LOG.info("read all test users");
		//读入user list
		String[] ss_testusers = readAllTestUsers();
		LOG.info("all test users:"+ss_testusers.length);
		
		
		//分组，多线程进行建模
		int allUsersNum = ss_testusers.length;
		int threadsNum = Integer.valueOf(args[0]);
		final CountDownLatch cdl = new CountDownLatch(threadsNum);
		for (int i = 0; i < threadsNum; ++i) {
			//计算最恰当的间隔长度
			int interNum = allUsersNum / threadsNum;
			if(allUsersNum%threadsNum != 0)
				interNum++;
			
			final int begin = (interNum) * i;
			final int end = allUsersNum < (begin + interNum) ? allUsersNum
					: (begin + interNum);
			final String[] someUsers = new String[end - begin];
			for (int k = begin; k < end; k++)
				someUsers[k - begin] = ss_testusers[k];

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						ItemOperation itemOP = ItemOperation.getInstance();
						//指定是hbase中的appitem
						itemOP.setItemType(ItemOperation.ItemType.APPITEM);
						
						int num = 0;
						for (int j = 0; j < someUsers.length; j++) {
							String userID = someUsers[j];
							num++;
							if (num % 10 == 0) {
								String status = readStatus();
								if (status == null || status.isEmpty()) {
									LOG.error("status not found");
									return;
								}

								if (status.equals("stop")) {
									LOG.info("status is " + status);
									return;
								}

								usermodelInterface.getInstance("modeling").commitModel();

								LOG.info("finish:" + num);
							}

							/*
							 * 先查询建模数据库，得到此user的历史三维向量，这样可以只进行增量更新，大大提高速度；
							 * 如果在库中查不到此user，那么走新建流程。
							 */
							//...
							
							//查询得到此user最近90天的历史行为
							HashMap<String, String> hm_log = logDBOperation
									.queryUserIDInDateRange(userID,
											String.valueOf(System.currentTimeMillis()), 90);
							
							//如果记录太少,查询更多天的item
							if(hm_log == null || hm_log.size() < 20)
								hm_log = logDBOperation.queryUserIDInDateRange(userID,
											String.valueOf(System.currentTimeMillis()), 120);
							
//							//将行为从增量表中删除
//							logDBOperation.deleteByUsrID(userID);
							
							//清理取出的行为列表，10分钟内的分页合并并去除其它额外信息；
							HashMap<String, String> hm_dayitems = parseNewsAppUserDayLog(hm_log);

						
							//调用模型层建模函数建模
							//long b = System.currentTimeMillis();
							usermodelInterface.getInstance("modeling").modelOneUser(userID,
									hm_dayitems,itemOP);
							//long c = System.currentTimeMillis();
							
//							//@test
//							System.out.println(userID+" all time:"+(c-b));
						}
						
						usermodelInterface.getInstance("modeling").commitModel();
						LOG.info("finish:" + num);
						LOG.info("thread " + begin + " has finished...");
						
						cdl.countDown();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						LOG.error("",e);
					}
				
				}
			}).start();

		}
		
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("now all threads have finished");
		
		
//		int num = 0;
//		LOG.info("training...");
//		for (int i = 0; i < ss_testusers.length; i++) {
//			String userID = ss_testusers[i];
//			num++;
//			if(num % 1000 == 0){
//				status = readStatus();
//				if(status == null || status.isEmpty())
//				{
//					LOG.error("status not found");
//					return;
//				}
//				
//				if(status.equals("stop")){
//					LOG.info("status is "+status);
//					return;
//				}
//				
//				usermodelInterface.getInstance().commitModel();
//				
//				LOG.info("finish:"+num);
//			}
//			
//			//查询得到此user的历史行为
//			HashMap<String, String> hm_log = logDBOperation.queryUserIDInDateRange(userID,System.currentTimeMillis(), 90);
//			//清理取出的行为列表，10分钟内的分页合并并去除其它额外信息；
//			HashMap<String,String> hm_dayitems = parseUserDayLog(hm_log);
//			//调用模型层建模函数建模
//			usermodelInterface.getInstance().modelOneUser(userID, hm_dayitems);	
//		}
		
		LOG.info("train over");
		//change status flag
		LOG.info("update status flag");
		commenFuncs.writeResult(fieldDicts.modelPath, fieldDicts.trainStatusFile, "canupdate", "utf-8", false, LOG);
		
	}


}
