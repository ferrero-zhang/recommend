/**
 * 
 */
package com.ifeng.iRecommend.front.recommend2;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import redis.clients.jedis.Jedis;

import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.HttpRequest;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.front.recommend2.recommend4HeadLine.item2app;

/**
 * <PRE>
 * 作用 : 
 *   solr版本的综合计算代码：查询user+计算item+解析xml+阈值过滤+backup补充
 *   将嵌入前端业务层，为APP user的请求做响应
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
 *          1.0          2014年3月31日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class headlineInterface {
	/*
	 * 为业务逻辑层响应的数据结构；
	 */
	class responseData {
		@Expose
		String userID;
		@Expose
		ArrayList<item2app> recommendData;
		responseData(){
			userID = "";
			recommendData = new ArrayList<item2app>(16);
		}
	}

	/*
	 * 为业务逻辑层响应的数据结构；
	 */
	class item2app {
		@Expose
		public String docId;// 文章id(imcp_id或url)
		@Expose
		public String title;// 标题
		@Expose
		public String date;// item publish date
		@Expose
		public String hotLevel;// 文章热度等级
		@Expose
		public String docChannel;// 内容分类
		@Expose
		public String why;// 解释描述
		@Expose
		public float score;// 对user的匹配分数；
		@Expose
		public float hotBoost;// hotlevel对应的加降权分数；
		@Expose
		public String docType;//文章类型 (slide/video/doc/hdSlide)
		
		
		public item2app() {
		}
	}

	/*
	 * 候选集，冷启动用户，以及补充数据用；
	 */
	class backupItems {
		@Expose
		ArrayList<item2app> al_backup_items;
		
		backupItems(){
			al_backup_items = new ArrayList<item2app>(200);
		}
	}
	
	/**
	 * 为“猜你喜欢”类型应用，提供推荐数据接口；
	 * 
	 * @param userid
	 *            用户标识；
	 * @param topN
	 *            现在最大数目；
	 * @param threshold
	 *            threshold,相似度阈值；           
	 * 注意：
	 * 	
	 * 
	 *  return json数组字符串；示例：
	 *  {
		    "userID": "UUIDF28126DB65934D8FAE7241176E6669E5",
		    "recommendData": [
		        {
		            "docId": "86852671",
		            "title": "首例P2P自融判非法吸存 曾4个月吸收公众存款1.3亿",
		            "date": "2014-07-29 06:15:39",
		            "hotLevel": "AA",
		            "docChannel": "tech",
		            "why": "hot",
		            "score": 0,
		            "hotBoost": 1,
		            "docType": "doc"
		        },
		         {
		            "docId": "86839160",
		            "title": "安倍携夫人访问特多 争取对方协助“入常”",
		            "date": "2014-07-28 23:17:33",
		            "hotLevel": "E",
		            "docChannel": "video",
		            "why": "recommend",
		            "score": 0.14751403,
		            "hotBoost": 0.26894143,
		            "docType": "video"
		        }
		    ]
		}
	 */
	public responseData guessYouLike(String userid, int topN,float threshold) {
		if (userid == null || userid.isEmpty())
			return null;
		if (topN <= 0)
			topN = 500;
		if(threshold <= 0)
			threshold = 0.5f;

		responseData res = new responseData();
		res.userID = userid;

		// 请求backup items
		Jedis jedis = new Jedis("10.32.21.62", 6379, 10000);
		jedis.select(7);
		String s_backup_items = jedis.get("backup_items");
		backupItems backup_items = JsonUtils.fromJson(s_backup_items,
				backupItems.class);
		
		//@test
		System.out.println("backup_items:"+backup_items.al_backup_items.size());
		
		// 1.查询user向量；
		// 2.组合查询item库，进行向量的余弦夹角计算，并提取相似度最高的前若干条；
		// 3.阈值过滤
		// 4.加入hot boost计算，
		// 5.backup补充

//		// 1.查询user向量
		
		String user3vectors = get3Vectors(userid,jedis);
		if(user3vectors == null){
			System.out.println("ERROR,not found 3 vectors:"+userid);
			res.recommendData.addAll(backup_items.al_backup_items);
			return res;
		}
		String[] vecs = user3vectors.split("\\|!\\|");
		if(vecs.length != 3){
			System.out.println("ERROR,wrong 3 vectors:"+userid);
			res.recommendData.addAll(backup_items.al_backup_items);
			return res;
		}
		
		String queryStr = new StringBuffer("q=(topic1:(").append(vecs[0])
				.append("))^1 OR (topic2:(").append(vecs[1])
				.append("))^1 OR (topic3:(").append(vecs[2])
				.append("))^1&q.op=OR&rows=").append(topN)
				.append("&fl=itemid,item2app,score").toString();
		queryStr = queryStr.replaceAll("\\s", "%20");
		
//		//@test
//		System.out.println("http://10.32.28.116:8080/solr46/item/select?"+queryStr);

		// 2.组合查询item库
		String items = "";
		try {
			items = HttpRequest.sendGet(
					"http://10.32.28.116:8080/solr46/item/select", queryStr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROR : items is null:"+userid);
			
			return res;
		}
		if (items == null || items.isEmpty()) {
			System.out.println("ERROR : items is null");
			return res;
		}

		// 3.解析xml，过滤阈值，计算hot*boost得分，生成一个item2app数组
		ArrayList<item2app> al_items = extractFromSolrRes(items,threshold);
		
		//@test
		System.out.println("extractFromSolrRes size:" + al_items.size());
		
		// 再对al_item2apps排序
		Collections.sort(al_items, new Comparator<item2app>() {
			@Override
			public int compare(item2app arg0, item2app arg1) {
				// TODO
				// Auto-generated
				// method
				// stub
				if (arg0.score > arg1.score)
					return -1;
				else if (arg0.score < arg1.score)
					return 1;
				else
					return 0;
			}

		});
		
		res.recommendData.addAll(al_items);
		
		// 如果数目不够，那么采用候选集合补充
		// 为避免业务逻辑层排重后数据不足，另外补充一部分backup list，也即候选数据；
		if (backup_items != null) {
			if(res.recommendData == null)
				res.recommendData = new ArrayList<item2app>(topN);
			int AA_num = 0;
			for (item2app i2a : backup_items.al_backup_items) {
				//AA强行加入第一个位置(最多3个)
				if(AA_num < 3 && i2a.hotLevel.equals("AA"))
				{
					i2a.why = "hot";
					res.recommendData.add(0, i2a);
					AA_num++;
				}else
					res.recommendData.add(i2a);
				
			}
		}
		
		
		// 7.返回
		return res;
	}
	
	/*
	 * 从solr中查询user 3 vectors,先去redis的cache中查，如果没有再去solr中
	 */
	private String get3Vectors(String userid, Jedis jedis) {
		// TODO Auto-generated method stub
		if(userid == null || userid.isEmpty()||jedis == null)
			return null;
		
		jedis.select(8);
		String user3vecs = jedis.get(userid);
		if(user3vecs != null)
		{
			return user3vecs;
		}
		// 1.查询user向量
		String userDoc = "";
		try {
			userDoc = HttpRequest.sendGet(
					"http://10.32.28.116:8080/solr46/user/select", "q=userid:"
							+ commenFuncs.escapeQueryChars(userid));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROR : userDoc == null");
			return null;
		}
		if (userDoc == null || userDoc.isEmpty()) {
			System.out.println("ERROR : userDoc == null");
			return null;
		}
		
		//查询不到user，冷启动用户
		if(userDoc.indexOf("numFound=\"0\"") > 0){
			System.out.println("ERROR : user is cold");
			return null;
		}

		// 抽取合并，得到三个topic向量
		String topic1_vec = null,topic2_vec = null,topic3_vec = null;
		try {
			topic1_vec = getTopicVector(userDoc, "topic1");
			topic2_vec = getTopicVector(userDoc, "topic2");
			topic3_vec = getTopicVector(userDoc, "topic3");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (topic1_vec == null && topic2_vec == null
				&& topic3_vec == null) {
			System.out.println("ERROR : all topic is null");
			return null;
		}
		if (topic1_vec.isEmpty() && topic2_vec.isEmpty()
				&& topic3_vec.isEmpty()) {
			System.out.println("ERROR : all topic is empty");
			return null;
		}
		
		StringBuffer sbRes = new StringBuffer();
		if(topic1_vec == null || topic1_vec.isEmpty())
			sbRes.append("none|!|");
		else
			sbRes.append(topic1_vec).append("|!|");
		if(topic2_vec == null || topic2_vec.isEmpty())
			sbRes.append("none|!|");
		else
			sbRes.append(topic2_vec).append("|!|");
		if(topic3_vec == null || topic3_vec.isEmpty())
			sbRes.append("none");
		else
			sbRes.append(topic3_vec);
	
		//放入cache
		jedis.setex(userid, 1800, sbRes.toString());

		return sbRes.toString();
	}

	/**
	 * 内部接口；主要把solr返回结果解析成item2app数组；
	 * 
	 *  @param items
	 *            solr返回结果格式；
	 *  @param threshold
	 *            threshold,相似度阈值；
	 * 
	 * solr结果格式：
	 * <doc>
		<str name="itemid">86944259</str>
		<str name="item2app">
		{"docId":"86944259","title":"八一建军节前忆当年：第一枪背后的信仰选择","date":"2014-07-30 11:12:32","hotLevel":"D","docChannel":"history","why":"","score":0.0,"hotBoost":0.5,"docType":"doc"}
		</str>
		<float name="score">0.8944272</float>
	   </doc>
	 */
	private ArrayList<item2app> extractFromSolrRes(String items,float threshold) {
		// TODO Auto-generated method stub
		if (items == null || items.isEmpty())
			return null;
		int b = items.indexOf("<doc>");
		ArrayList<item2app> al_i2a = new ArrayList<item2app>();
		while (b > 0) {
			int e = items.indexOf("</doc>", b + 5);
			if (e <= b)
				return null;
			String s_doc = items.substring(b, e);
			item2app i2a = null;
			// 抽取出itemid、item2app、score标签
			try{
				float score = 0f;
				int b1 = s_doc.indexOf("score\">");
				if (b1 >= 0) {
					int e1 = s_doc.indexOf("</float>", b1);
					if (e1 > b1) {
						score = Float.valueOf(s_doc.substring(b1 + 7, e1));
					}
				} 
						
				if(score < threshold)
				{
					System.out.println("warn,score < threshold:"+score);
					// next one
					b = items.indexOf("<doc>", e);
					break;
				}
				b1 = s_doc.indexOf("item2app\">");
				if (b1 >= 0) {
					int e1 = s_doc.indexOf("</str>", b1);
					if (e1 > b1) {
						i2a = JsonUtils.fromJson(s_doc.substring(b1 + 10, e1),item2app.class);
					}
				}
				
				if(i2a == null){
					System.out.println("ERROR,parse i2a failed:"+items);
					// next one
					b = items.indexOf("<doc>", e);
					continue;
				}
				//计算出加权得分
				i2a.score = score * i2a.hotBoost;
				i2a.why = "recommend";
				al_i2a.add(i2a);
			}catch(Exception e1){
				e1.printStackTrace();
			}finally{
				// next one
				b = items.indexOf("<doc>", e);
			}
		
		}

		return al_i2a;
	}

	/**
	 * 内部接口；主要把user向量分解为topic1、topic2、topic3；并根据业务场景进行向量约减；
	 */
	private static String getTopicVector(String rt, String topicType) throws Exception {
		// TODO Auto-generated method stub
		StringBuffer sb_topic2vec = new StringBuffer();
		HashMap<String, Integer> hm_topicTerms = new HashMap<String, Integer>();
		int b = rt.indexOf(topicType + "\">");
		if (b < 0)
			return "";
		int e = rt.indexOf("</str>", b + 8);
		if (e <= b)
			return "";

		String topic1 = "";
		topic1 = rt.substring(b + 8, e).trim();
		String[] secs = topic1.split("\\s");
		for (String topicTerm : secs) {
			if(topicTerm.isEmpty())
				continue;
			if (hm_topicTerms.containsKey(topicTerm))
				hm_topicTerms.put(topicTerm, hm_topicTerms.get(topicTerm) + 1);
			else
				hm_topicTerms.put(topicTerm, 1);
		}

		LinkedList<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>();
		list.addAll(hm_topicTerms.entrySet());

		//对topic3进行sort
		if(topicType.equals("topic3"))
		{
			Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
				public int compare(Map.Entry obj1, Map.Entry obj2) {// 从高往低排序
	
					if (Integer.parseInt(obj1.getValue().toString()) < Integer
							.parseInt(obj2.getValue().toString()))
						return 1;
					if (Integer.parseInt(obj1.getValue().toString()) == Integer
							.parseInt(obj2.getValue().toString()))
						return 0;
					else
						return -1;
				}
			});
		}
		
		//遍历得到结果
		for (Iterator<Map.Entry<String, Integer>> ite = list.iterator(); ite
				.hasNext();) {
			Map.Entry<String, Integer> et = ite.next();
			if (et.getKey().matches("[\\pP+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]"))
				continue;

			//临时约束
			if (topicType.equals("topic3") && et.getValue() <= 5
					&& sb_topic2vec.length() > 1024 * 2)
				break;

			// System.out.println("key-value: " + et.getKey() + ","
			// + et.getValue());
			String topic_term = et.getKey();
			//Add for special term
			topic_term = commenFuncs.escapeQueryChars(topic_term);
			topic_term = URLEncoder.encode(topic_term);
			sb_topic2vec.append(topic_term).append("^").append(et.getValue())
					.append(" ");
		}

		// System.out.println(sb_topic2vec.toString());
		String topic_vec = sb_topic2vec.toString().trim();
		topic_vec = topic_vec.replaceAll("\\s", "%20");

		return topic_vec;

	}
	
	
	//@test
	public static void main(String[] args) {
		// 读入test users
		HashSet s_testusers = new HashSet<String>();
		FileUtil wordidFile = new FileUtil();
		wordidFile.Initialize(args[0], "UTF-8");
		String line = "";
		while ((line = wordidFile.ReadLine()) != null) {
			s_testusers.add(line.trim());
		}
		String[] ss_users = (String[]) s_testusers.toArray(new String[0]);
		int askNum = 1000;
		headlineInterface qi4f = new headlineInterface();
		for(String userid:ss_users)
		{
			long b = System.currentTimeMillis();
			try{
				responseData rt = qi4f.guessYouLike(userid, askNum, 0.5f);
				System.out.println(JsonUtils.toJson(rt));
			}catch(Exception e){
				e.printStackTrace();
			}
			long e = System.currentTimeMillis();
			System.out.println("guessYouLike for:"+userid+" "+(b-e));
			
		}
	}
}
