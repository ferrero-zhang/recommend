/**
 * 
 */
package com.ifeng.iRecommend.usermodel;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import com.google.gson.annotations.Expose;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.HttpRequest;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.commen.Utils.k2v;
import com.ifeng.commen.Utils.stopwordsFilter;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.channelsParser;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation;
import com.ifeng.iRecommend.train.usermodelTraining;



/**
 * <PRE>
 * 作用 : 
 *   solr版本;
 *   提供usermodel的建模、相似度计算等对外接口；基于solr的分布式存储和计算引擎，不再用内置的lucene引擎
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
 *          1.0          2013-7-15        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
// 用户模型读取用
class string2int {
	String word;
	int tf;
}

public class usermodelInterfaceSolr {
	private static final Log LOG = LogFactory.getLog("usermodel");

	//各个channel的IDF信息，用于用户channel行为的更精准表达;注意：key的形式是当前day + channel，比如“20130708mil”
	private HashMap<String,Float> hm_channelIDFs  = null;
	 	
	
	
	
	/*
	 * 构造函数；
	 * @param 
	 * 		workingType:工作类型；"modeling":建模用；"query":模型专门用于查询；
	 */
	public usermodelInterfaceSolr() {
		try {
			hm_channelIDFs = usermodelInterface.readChannelIDFs("/projects/zhineng/tfidf/tfidf_week");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("readChannelIDFs error",e);
		}
		if(hm_channelIDFs == null)
			hm_channelIDFs = new HashMap<String,Float>();
		LOG.info("hm_channelIDFs.size = "+hm_channelIDFs.size());
	}
	
	/**
	 * 根据输入的单个user的阅读行为，对其进行建模，形成usermodel
	 * <p>
	 * 对user阅读的每一个item，计算其抽象表达；按一定规则，合并所有item的抽象表达，形成一个统一抽象，再调用lucene的
	 * index接口完成入库；
	 * 由于分页格式混乱，1分钟内同一个分页的合并可能无法完全做到；那么考虑在得到title后，对3分钟内title高度近似的item，进行合并计算；
	 * </p>
	 * 
	 * 可以多线程并发，实现了同步互斥；
	 * 
	 * @param userID
	 *            用户ID
	 * @param hm_dayitems
	 *            ，user按天为单位的item行为集合； 比如“20130709
	 *            http://games.ifeng.com/!7387759
	 *            http://games2.ifeng.com/!7443314”
	 * @param itemOP
	 *            item的查询接口
	 */
	public userDocForSolr cmpOneUserDoc(String userID,
			HashMap<String, String> hm_dayitems, ItemOperation itemOP) {
		if (userID == null || userID.isEmpty())
			return null;
		if (hm_dayitems == null || hm_dayitems.isEmpty())
			return null;
		if (itemOP == null)
			return null;

		//LOG
		LOG.info("cmp usermodel for:"+userID);
		
		// 后台存储计算引擎solr的数据接口
		userDocForSolr udfs = new userDocForSolr();
		udfs.userid = userID;

		// 遍历寻找所有items
		Iterator<Entry<String, String>> it = hm_dayitems.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> et = it.next();
			String day = et.getKey();
			String items = et.getValue();
			String[] secs = items.split("[\\s!]");
			if (secs.length % 4 != 0)
				continue;

			// 记录user每1分钟观看item的title，进行1分钟内高度近似item的过滤
			HashMap<String, ArrayList<String[]>> hm_minsTitles1 = new HashMap<String, ArrayList<String[]>>();
			HashMap<String, ArrayList<String[]>> hm_minsTitles2 = new HashMap<String, ArrayList<String[]>>();

			for (int i = 0; i < secs.length; i += 4) {
				String imcp_id = secs[i];// pc应用中是url，app应用中是imcp_id
				String _mins1 = secs[i + 1];
				String _mins2 = secs[i + 2];
				String _channel = secs[i + 3];

				//过滤掉一些无用信息
				if(imcp_id.equals("ch")
						||imcp_id.matches("\\d{1,3}"))
					continue;
				// 专题的信息：
				// ...

				// 得到item具体信息，并合并1分钟内相似标题
				Item oneItem = null;
				// @test
				// long b = System.currentTimeMillis();
				
				//注意，由于cmpp和imcp共有在log中，此处有不少特殊逻辑
				//来自cmpp ent 
				if (imcp_id.matches("4\\d{5,16}000")) {
					//查询得到title或者url，并在ikv中找到imcp id，然后继续查询
					imcp_id = queryCmppItem.getInstance().getImcpID(imcp_id.substring(0,imcp_id.length()-3),"ent");
					if(imcp_id != null && !imcp_id.isEmpty())
						oneItem = itemOP.getItem(imcp_id);
				}
				
				//来自imcp
				if (imcp_id.matches("\\d{5,16}")) {
					oneItem = itemOP.getItem(imcp_id);
				}
				
				// long c = System.currentTimeMillis();
				// //@test
				// System.out.println(imcp_id+"|"+(c-b));
				// sumQueryItemTime += c-b;

				// 进行_mins内的排重
				if (oneItem != null) {

					// //@test
					// System.out.println(oneItem.getID());
					// System.out.println(oneItem.getUrl());
					// System.out.println(oneItem.getChannel());
					// System.out.println(oneItem.getTitle());

					String title = oneItem.getTitle();// 新能源_nz 汽车产业_nz 路线图_n
														// 敲定_v _w
														// 十年_mq 将_d 投_v 千亿_m
					if (title != null) {
						String[] titleSecs = title.split("\\s");
						// title太短应该过滤
						if (titleSecs.length <= 1)
							continue;
						// 如果极度近似标题在单位时间内重复出现，判断是分页，过滤掉；
						ArrayList<String[]> al_titles1 = hm_minsTitles1
								.get(_mins1);
						if (al_titles1 == null) {
							al_titles1 = new ArrayList<String[]>();
							hm_minsTitles1.put(_mins1, al_titles1);
						}
						// 判断相似title与否
						boolean isSame = false;
						for (int j = 0; j < al_titles1.size(); j++) {
							if (commenFuncs.simRate(titleSecs,
									al_titles1.get(j)) >= 0.9f) {
								isSame = true;
								break;
							}
						}

						// 得到另外一个time段
						ArrayList<String[]> al_titles2 = hm_minsTitles2
								.get(_mins2);
						if (al_titles2 == null) {
							al_titles2 = new ArrayList<String[]>();
							hm_minsTitles2.put(_mins2, al_titles2);
						}

						if (isSame) {
//							LOG.info("title is same 1,abandon imcp_id,url:"
//									+ imcp_id + "," + oneItem.getUrl());
							continue;
						}

						// time2
						for (int j = 0; j < al_titles2.size(); j++) {
							if (commenFuncs.simRate(titleSecs,
									al_titles2.get(j)) >= 0.9f) {
								isSame = true;
								break;
							}
						}
						if (isSame) {
//							LOG.info("title is same 2,abandon imcp_id,url:"
//									+ imcp_id + "," + oneItem.getUrl());
							continue;
						}

						// remember in cache
						al_titles1.add(titleSecs);
						al_titles2.add(titleSecs);

					}
				}
				// else{//debug,test
				// System.out.println("not found item:"+imcp_id);
				// }
				
//				//@test
//				if(imcp_id.indexOf("phtv") >= 0)
//					System.out.println("fuckwm");

				// 构建三维topic
				HashMap<String, Float> hm_tagValues = itemAbstraction
						.cmpChannelsWithIDFs(oneItem,imcp_id,day,hm_channelIDFs);
				
				if (hm_tagValues == null || hm_tagValues.isEmpty()) {
					hm_tagValues = itemAbstraction.cmpChannelsWithIDFs(oneItem, _channel,day,hm_channelIDFs);
				}
				
				// //@test
				// if(hm_tagValues.containsKey("http%3A%2F%2Fi.ifeng.com%2Fsports%2Fworldcup2014%2Fwcqiudui%3Fch%3Dclient"))
				// {
				// System.out.println("fuck wmj");
				// }

				// 得到channels信息
				if (hm_tagValues != null && !hm_tagValues.isEmpty()) {
//					hm_tagValues = itemAbstraction.normalization(hm_tagValues);
				
//					//@test
//					if(hm_tagValues.containsKey("01b6f100aef0"))
//						System.out.println("fuckwm");
					// 合并入user抽象表达
					udfs.add(hm_tagValues, "topic1");
				}

				// 得到top tags
				hm_tagValues = itemAbstraction.getItemTopWords(oneItem, false);
				// 计算隐含主题
				if (hm_tagValues != null && !hm_tagValues.isEmpty()) {
					HashMap<String, Float> hm_topics = itemAbstraction
							.cmpLatentTopics(hm_tagValues);
					if (hm_topics != null && !hm_topics.isEmpty()) {
						// //@test
						// LOG.info("find url:"+url+" title:"+oneItem.title+" keywords:"+oneItem.rawKeywords
						// +" topics:"+hm_topics.entrySet().toString());

						// 合并入user抽象表达
						udfs.add(hm_topics, "topic2");
					}

					// 将细化标题等tag放入ud
					hm_tagValues = itemAbstraction.normalization(hm_tagValues);
					// 合并入user抽象表达
					udfs.add(hm_tagValues, "topic3");
				}

			}
		}

		
		udfs.turnVectorToDocs();
		// modeling
		String s_doc = JsonUtils.toJson(udfs, userDocForSolr.class);

		// test,随机输出进行观察
		double rd = Math.random();
		if (rd <= 0.001)
			commenFuncs.writeResult(fieldDicts.modelPath + "userdocs//",
					udfs.userid, s_doc, "utf-8", false, null);

		return udfs;

	}

	
	/**
	 * 根据输入的一组user的阅读行为，对其进行建模，形成usermodel
	 * <p>
	 * 对user阅读的每一个item，计算其抽象表达；按一定规则，合并所有item的抽象表达，形成一个统一抽象，再调用lucene的
	 * index接口完成入库；
	 * 由于分页格式混乱，1分钟内同一个分页的合并可能无法完全做到；那么考虑在得到title后，对3分钟内title高度近似的item，进行合并计算；
	 * </p>
	 * 
	 * 可以多线程并发，实现了同步互斥；
	 * 
	 * @param hm_user2dayitems
	 *            集合：<用户ID--user dayitems历史行为>
	 * @param itemOP
	 *          item的查询接口
	 */
	public String modelSomeUsers(
			HashMap<String, HashMap<String, String>> hm_user2dayitems,
			ItemOperation itemOP) {
		if (itemOP == null)
		{
			itemOP = ItemOperation.getInstance();
			LOG.warn("itemOP == null");
		}
		if (hm_user2dayitems == null || hm_user2dayitems.isEmpty())
			return null;

		// 后台存储计算引擎solr的数据接口
		ArrayList<userDocForSolr> udfss = new ArrayList<userDocForSolr>(64);
		// 遍历寻找所有items
		Iterator<Entry<String, HashMap<String, String>>> it = hm_user2dayitems
				.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, HashMap<String, String>> et = it.next();
			//LOG.info("cmp one user doc for userid="+et.getKey());
			userDocForSolr oneUserDoc = this.cmpOneUserDoc(et.getKey(),
					et.getValue(), itemOP);
			//为空的异常
			if(oneUserDoc == null)
			{
				LOG.error("cmp one user doc error,is null:"+et.getKey());
				continue;
			}
			
		
			udfss.add(oneUserDoc);
		}

		
		if(udfss.isEmpty())
		{
			return null;
		}
		
//		//对字符做特殊处理
//		for(userDocForSolr udfs:udfss){
//			udfs.userid = commenFuncs.escapeQueryChars(udfs.userid);
//		}
		
		String s_docs = JsonUtils.toJson(udfss);
		
		// 建模写入后台存储和计算引擎;构建json数据
		String rt = HttpRequest.sendPost(
				//"http://10.32.21.63:8080/solr45/user/update/json", s_docs);
				"http://10.32.28.119:8080/solr46/user/update/json",s_docs);//+"&commit=true");
		//test
		if(rt.indexOf("failed") > 0)
		{
			LOG.error("send post failed,rt="+rt);
			
			Iterator<Entry<String, HashMap<String, String>>> it_tmp = hm_user2dayitems
					.entrySet().iterator();
			StringBuffer sbFailed = new StringBuffer();
			while (it_tmp.hasNext()) {
				Entry<String, HashMap<String, String>> et = it_tmp.next();
				sbFailed.append(et.getKey()).append("\r\n");
			}
			commenFuncs.writeResult("failed_docs/",""+System.currentTimeMillis(), sbFailed.toString(), "utf-8", false, null);
		}
		
		
		return s_docs;

	}

	/**
	 * 根据输入的oneUserDoc，查询得到其对应profile信息并填入profile字段
	 * 
	 * @param userID
	 * @param deviceType 不同设备类型，采用不同接口
	 */
	public void queryProfile(userDocForSolr oneUserDoc,String deviceType) {
		// TODO Auto-generated method stub
		if(oneUserDoc == null || deviceType == null)
			return;
		String queryUrl = "";
		if(deviceType.equals("ios"))
			queryUrl = "http://10.90.1.108/iosquery";
		else
			queryUrl = "http://10.90.1.108/androidquery";
		String jsonRes = "";
		try {
			jsonRes = HttpRequest.sendGet(
					queryUrl, "sn="
							+ commenFuncs.escapeQueryChars(oneUserDoc.userid) + "&appId=com.ifeng.news2");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("query user profile failed:"+oneUserDoc.userid,e);
			return;
		}
		
		if(jsonRes != null){
			//提取所有的 L:到, ，以及L:到]，取最长
			int b = jsonRes.indexOf("L:");
			String loc = "",loc_tmp = "";
			while(b > 0){
				b = b + 2;
				int e1 = jsonRes.indexOf(",",b);
				int e2 = jsonRes.indexOf("]",b);
				int e = 0;
				if(e1 > b && e1 < e2)
					e = e1;
				else if(e2 > b && e2 < e1)
					e= e2;
				if(e > b){
					loc_tmp = jsonRes.substring(b, e);
					if(loc_tmp.length() > loc.length())
						loc = loc_tmp;
				}
				b = jsonRes.indexOf("L:",b+2);
			}
			if(!(loc.isEmpty()))
				oneUserDoc.setProfile(new String(loc));
			
		}
		
	}

	/**
	 * 根据输入的userID，查询得到其对应兴趣向量
	 * 注意：已经对向量进行了排序、转码、过滤非法等操作；所以是一个可以直接用于网络传输和查询的字符串；
	 * <p>
	 * solr返回的标准格式很乱，需要简化
	 * </p>
	 * 
	 * 可以多线程并发；
	 * 
	 * @param userID
	 */
	public static String queryUserStringFromSolr(String userID){
		if (userID == null || userID.isEmpty())
			return null;
		// 1.查询user向量
		String userDoc = "";
		try {
			userDoc = HttpRequest.sendGet(
					"http://10.32.28.119:8080/solr46/user/select", "q=userid:("
							+ commenFuncs.escapeQueryChars(userID) + ")");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("query user failed:"+userID,e);
			e.printStackTrace();
			return "error";
		}
		
	
		if (userDoc == null || userDoc.isEmpty()
				|| userDoc.indexOf("get failed,param") >= 0){
			LOG.error("query user error:"+userID+" "+userDoc);
			return "error";
		}
		
		//查询不到user，新用户
		if(userDoc.indexOf("numFound=\"0\"") > 0){
			LOG.warn("Warn,user is cold:"+userID);
			return "cold";
		}
		
		return userDoc;
	}
	
	/**
	 * 内部接口；主要把user向量分解为topic1、topic2、topic3；并根据业务场景进行向量约减；
	 */
	public static String getTopicVector(String rt, String topicType) {
		// TODO Auto-generated method stub
		if(rt == null || rt.isEmpty())
			return "";
		
		//如果为空，则返回空;eg:topic1"/>
		if(rt.indexOf(topicType +"\"/>") > 0)
			return "";
		
		
		ArrayList<string2int> al_s2i = new ArrayList<string2int>();
		int b = rt.indexOf(topicType + "\">");
		if (b < 0)
			return "error";
		int e = rt.indexOf("</str>", b + 8);
		if (e <= b)
			return "error";

		String topic1 = "";
		topic1 = rt.substring(b + 8, e).trim();
		String[] secs = topic1.split("\\s");
		int tf = 0;String oldWord = "";
		for (String topicTerm : secs) {
			if(topicTerm.equals(oldWord))
				tf++;
			else{
				if(tf > 0
					&& !oldWord.matches("[\\pP+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]"))
				{
					string2int s2i = new string2int();
					s2i.word = oldWord;
					s2i.tf = tf;
					al_s2i.add(s2i);
				}
				oldWord = topicTerm;
				tf = 1;
			}
		}
		if(!(oldWord.isEmpty())){
			string2int s2i = new string2int();
			s2i.word = oldWord;
			s2i.tf = tf;
			al_s2i.add(s2i);
		}

		// sort
		Collections.sort(al_s2i, new Comparator<string2int>() {
			public int compare(string2int obj1, string2int obj2) {// 从高往低排序
				if (obj1.tf < obj2.tf)
					return 1;
				if (obj1.tf > obj2.tf)
					return -1;
				return 0;
			}
		});

		StringBuffer sbRes = new StringBuffer();
		for(string2int s2i:al_s2i){
			tf = s2i.tf;
			while(tf-->0)
				sbRes.append(s2i.word).append(" ");
		}
		
		return sbRes.toString().trim();

	}

	
	/**
	 * 根据输入的一组user的阅读行为，对其进行增量建模，形成usermodel
	 * <p>
	 * 对user阅读的每一个item，计算其抽象表达；按一定规则，合并所有item的抽象表达，形成一个统一抽象，再调用lucene的
	 * index接口完成入库；
	 * 由于分页格式混乱，1分钟内同一个分页的合并可能无法完全做到；那么考虑在得到title后，对3分钟内title高度近似的item，进行合并计算；
	 * </p>
	 * 
	 * 可以多线程并发，实现了同步互斥；
	 * 
	 * @param hm_user2dayitems
	 *            集合：<用户ID--user dayitems历史行为>
	 * @param itemOP
	 *          item的查询接口
	 * @param userDeviceType
	 * 			ios/android
	 */
	public String modelSomeUsers(
			HashMap<String, HashMap<String, String>> hm_user2dayitems,
			ItemOperation itemOP, String RAINMODE_banch,String userDeviceType) {
		// TODO Auto-generated method stub
		if (itemOP == null)
		{
			itemOP = ItemOperation.getInstance();
			LOG.warn("itemOP == null");
		}
		if (hm_user2dayitems == null || hm_user2dayitems.isEmpty())
			return null;

		// 后台存储计算引擎solr的数据接口
		ArrayList<userDocForSolr> udfss = new ArrayList<userDocForSolr>(64);
		// 遍历寻找所有items
		Iterator<Entry<String, HashMap<String, String>>> it = hm_user2dayitems
				.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, HashMap<String, String>> et = it.next();
			//LOG.info("cmp one user doc for userid="+et.getKey());
			userDocForSolr oneUserDoc = this.cmpOneUserDoc(et.getKey(),
					et.getValue(), itemOP);
			//为空的异常
			if(oneUserDoc == null)
			{
				LOG.error("cmp one user doc error,is null:"+et.getKey());
				continue;
			}
			
			//如果太大，那么这个用户是有问题的用户，不建模并从solr中删除
			String res = this.cleanBadUsers(et.getKey(), oneUserDoc.getTopic1(), oneUserDoc.getTopic2(),oneUserDoc.getTopic3());
			if(res.equals("baduser")){
				continue;
			}else if(res.equals("good")){
				
			}else{
				//清洗topic1\topic2
				if(res.startsWith("t1")){
					oneUserDoc.setTopic1(res.substring(2));
					LOG.warn("clean topic1:"+oneUserDoc.userid);
				}
				if(res.startsWith("t2")){
					oneUserDoc.setTopic2(res.substring(2));
					LOG.warn("clean topic2:"+oneUserDoc.userid);
				}
				
			}
			
			//查询得到地域等固态属性信息
			queryProfile(oneUserDoc,userDeviceType);
			if(oneUserDoc.getProfile().isEmpty())
			{
				LOG.warn("not got profile:"+oneUserDoc.userid);
			}
			
			udfss.add(oneUserDoc);
		}

		
		if(udfss.isEmpty())
		{
			return null;
		}
		
//		//对字符做特殊处理
//		for(userDocForSolr udfs:udfss){
//			udfs.userid = commenFuncs.escapeQueryChars(udfs.userid);
//		}
		
		//这两种模式下，无论如何都删除增量表的记录，逻辑上完备；
		if(RAINMODE_banch.equals("all")||RAINMODE_banch.equals("new")){
			//逐个读取库中user，合并入新的udfss
			for(userDocForSolr oneUserDoc:udfss)
			{
				deleteUserFromTableMore(oneUserDoc.userid);
			}
		}
		
		if(RAINMODE_banch.equals("more")){
			ArrayList<userDocForSolr> udfss_new = new ArrayList<userDocForSolr>(64);
			//逐个读取库中user，合并入新的udfss
			for(userDocForSolr oneUserDoc:udfss)
			{
				String userString = queryUserStringFromSolr(oneUserDoc.userid);
				//历史上为空，是不是考虑进行全量训练？还是直接写入最近？
				if(userString == null){
					LOG.info("user is null:"+oneUserDoc.userid);
					continue;
				}else if(userString.equals("error"))//查询历史出错，是不是停止更新这个用户先？
				{
					LOG.error("query history from solr failed:"+oneUserDoc.userid);
					continue;
				}else if(userString.equals("cold"))//完全是一个新用户
				{
					LOG.info("user is new:"+oneUserDoc.userid);
					LOG.info("so we cmp all and delete log_more:"+oneUserDoc.userid);
					
					deleteUserFromTableMore(oneUserDoc.userid);
					
				}else{
					/*
					 * 其它情况下，判断user topic1大小，如果太大太过old则重建；否则，解析历史数据，并追加入新数据，然后覆盖写即可
					 */
					String topic1 = getTopicVector(userString, "topic1");
					String topic2 = getTopicVector(userString, "topic2");
					String topic3 = getTopicVector(userString, "topic3");
					//查询历史出错，是不是停止更新这个用户先？
					if(topic1.equals("error")
							||topic2.equals("error")
							||topic3.equals("error"))
					{
						
						LOG.error("getTopicVector parse error:"+oneUserDoc.userid);
						LOG.error("userString is:"+oneUserDoc.userid+" "+userString);
						continue;
					}
					
					//对old user进行全量更新
					if(!(topic1.equals("error"))
							&& topic1.length() >= 50000)
					{		
						LOG.warn("user model is big,maybe too old,build new :" + oneUserDoc.userid);
					}else{
						/*
						 * 特殊逻辑处理,如果user model中脏数据，那么清理修复;
						 * 特殊逻辑处理，如果user model太长，那么删除，这种用户是有问题用户
						 */
						String res = cleanBadUsers(oneUserDoc.userid,topic1,topic2,topic3);
						if(res.equals("baduser"))
						{
							LOG.error("baduser:"+oneUserDoc.userid);
							deleteUserFromTableMore(oneUserDoc.userid);
							continue;
							
						}else if(res.equals("good")){
							
						}else{
							//清洗topic1\topic2
							if(res.startsWith("t1")){
								topic1 = res.substring(2);
								LOG.warn("clean topic1:"+oneUserDoc.userid);
							}
							if(res.startsWith("t2")){
								topic2 = res.substring(2);
								LOG.warn("clean topic2:"+oneUserDoc.userid);
							}
						}
						
	//					//@test
	//					LOG.info(oneUserDoc.userid+":"+topic1);
	//					LOG.info(oneUserDoc.userid+":"+topic2);
	//					LOG.info(oneUserDoc.userid+":"+topic3);
	//					//^
						
						if(!topic1.isEmpty()){
							oneUserDoc.addMoreToAll(topic1, "topic1");
						}
						if(!topic2.isEmpty()){
							oneUserDoc.addMoreToAll(topic2, "topic2");
						}
						if(!topic3.isEmpty()){
							oneUserDoc.addMoreToAll(topic3, "topic3");
						}
						
						//重新序列化
						oneUserDoc.turnVectorToDocs();
						
		//				//@test
		//				System.out.println(JsonUtils.toJson(oneUserDoc));
		//				//^
					}
					
					deleteUserFromTableMore(oneUserDoc.userid);
				}
				
				udfss_new.add(oneUserDoc);	
			}
			
			udfss.clear();
			udfss = udfss_new;
			
		}
		
		
		String s_docs = JsonUtils.toJson(udfss);

		// 建模写入后台存储和计算引擎;构建json数据
		String rt = HttpRequest.sendPost(
				//"http://10.32.21.63:8080/solr45/user/update/json", s_docs);
				"http://10.32.28.119:8080/solr46/user/update/json",s_docs);//+"&commit=true");
		//test
		if(rt.indexOf("failed") > 0)
		{
			LOG.error("send post failed,rt="+rt);
			
			Iterator<Entry<String, HashMap<String, String>>> it_tmp = hm_user2dayitems
					.entrySet().iterator();
			StringBuffer sbFailed = new StringBuffer();
			while (it_tmp.hasNext()) {
				Entry<String, HashMap<String, String>> et = it_tmp.next();
				sbFailed.append(et.getKey()).append("\r\n");
			}
			commenFuncs.writeResult("failed_docs/",""+System.currentTimeMillis(), sbFailed.toString(), "utf-8", false, null);
		}
		
		
		return s_docs;
	}

	/**
	 * 从hbase user more表中删除一user的阅读行为
	 * @param userID
	 */
	private void deleteUserFromTableMore(String userID) {
		// TODO Auto-generated method stub
		// 只有确认OK，才能删除历史记录
		if (logDBOperation.getLogType() == logDBOperation.LogType.APPLOGREALTIME) {
			LOG.info("delete table more:" + userID);
			boolean del_rt = logDBOperation.deleteByUsrID(userID);
			if (del_rt == false) {
				LOG.error("delete more table failed:" + userID);
			}
		} else {
			LOG.error("logType is wrong,don't know why:"
					+ logDBOperation.getLogType());
		}

	}

	/*
	 * 从user库中清理掉bad数据
	 */
	public static String cleanBadUsers(String userid, String topic1 ,String topic2 ,String topic3) {
		// TODO Auto-generated method stub
		//如果太大，那么这个用户是有问题的用户，不建模并从solr中删除
		String t3 = topic3;
		if(t3.length() >= 1000000)
		{
			LOG.error("user is bad,t3 len="+t3.length());
			//从solr中直接删除
			String sCmd = new String("{\"delete\":{\"id\":\"z0\"}}");
			sCmd = sCmd.replace("z0",userid);
			try {
				// 建模写入后台存储和计算引擎;构建json数据
				String rt = HttpRequest.sendPost(
						"http://10.32.28.119:8080/solr46/user/update/json",
						sCmd);
				// test
				if (rt.indexOf("failed") > 0) {
					LOG.info("send post failed,del:" + sCmd + " rt=" + rt);
					return "baduser";
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error("send post failed", e);
				return "baduser";
			}
			// @test
			LOG.info("success,  del user:" + userid);
			return "baduser";
		}

		// 临时处理，bug修复
		String t1 = topic1;
		if (t1 != null && (t1.indexOf(" topic") >= 0)) {
			LOG.error("user has dirty topic1:topic**,userid:" + userid);
			// 清洗topic1并返回
			return "t1likunbug";
		}

		// 临时处理，特殊逻辑处理,如果user model中有脏数据，那么清理修复
		t1 = topic1;
		if(t1 != null &&(t1.indexOf(" iclient ")>=0||t1.indexOf(" rcmd ")>=0))
		{
			LOG.error("user has dirty topics:iclient|rcmd,userid:"+userid);
			//清洗topic1并返回
			t1 = t1.replaceAll("(rcmd|iclient|iclientcdn[a-z0-9]{0,32}) ", " ");
			t1 = t1.replaceAll("\\s{2,1000}", " ");
			
			return "t1"+t1;
		}
		
		
		
		//临时处理，清理掉一些有问题的topic，比如12 38 63 96等；
		String t2 = topic2;
		if(t2 != null &&(t2.indexOf(" topic12 ")>=0
				||t2.indexOf(" topic38 ")>=0
				||t2.indexOf(" topic96 ")>=0
				||t2.indexOf(" topic63 ")>=0))
		{
			LOG.error("user has dirty topics:topic12 38 63 96,userid:"+userid);
			//清洗topic1并返回
			t2 = t2.replaceAll("(topic12|topic38|topic63|topic96) ", " ");
			t2 = t2.replaceAll("\\s{2,1000}", " ");
			return "t2"+t2;
		}
		
		
		return "good";
	}

}
