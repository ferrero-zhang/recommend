/**
 * 
 */
package com.ifeng.iRecommend.front.recommend2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.google.gson.annotations.Expose;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.HttpRequest;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.commen.Utils.k2v;
import com.ifeng.commen.redis.JedisInterface;
import com.ifeng.iRecommend.dingjw.front_rankModel.RankItem;
import com.ifeng.iRecommend.dingjw.front_rankModel.RankList;
import com.ifeng.iRecommend.dingjw.front_rankModel.RankModel2;
import com.ifeng.iRecommend.dingjw.front_rankModel.appRankModel;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation;
import com.ifeng.iRecommend.train.usermodelTraining;
import com.ifeng.iRecommend.usermodel.HitCollector;
import com.ifeng.iRecommend.usermodel.MySimilarity2;
import com.ifeng.iRecommend.usermodel.VecTextField;
import com.ifeng.iRecommend.usermodel.usermodelInterface;

/**
 * <PRE>
 * 作用 : 
 *   1）对测试user逐个遍历计算推荐list，并补充入additional推荐数据
 *   user的遍历查询，得到推荐list后放入了redis中；适合少量用户做demo测试；
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   保证oscache和lucene库的一致；
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2014-5-21       likun          change
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class recommend4HeadLine {
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
		
		public void clear(){
			al_backup_items.clear();
		}
		public void add(item2app i2a){
			if(i2a != null)
				al_backup_items.add(i2a);
		}
	}
	
	private static final Log LOG = LogFactory.getLog("recommend4HeadLine");

	// lucene index
	private IndexWriter writer = null;
	private IndexSearcher searcher_item = null;
	private Field idField = null;
	private Field topic1Field = null;
	private Field topic2Field = null;
	private Field topic3Field = null;
	private Field item2appField = null;
	private Document doc = null;

	// user model searcher
	private IndexSearcher searcher_user = null;

	private backupItems backup_items = null;
	
	//debug模式
	public boolean DEBUG;
	
	public recommend4HeadLine() {
		// 1.item lucene 库的writer和searcher
		Directory dir_item = null;
		try {
			dir_item = FSDirectory.open(new File(fieldDicts.itemModelPath));
			searcher_item = new IndexSearcher(DirectoryReader.open(dir_item));
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			LOG.error("error:", e);
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			LOG.error("error:", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("error:", e);
		}
	}
	
	/*
	 * 重新加载硬盘上的item数据；
	 */
	public void reload(){
		Directory dir_item = null;
		try {
			dir_item = FSDirectory.open(new File(fieldDicts.itemModelPath));
			searcher_item = new IndexSearcher(DirectoryReader.open(dir_item));
			int num_items = searcher_item.getIndexReader().numDocs();
			LOG.info("item's num in lib:"+num_items);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("error:", e);
		}
	
	}

	public void closeIndexWriter() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("error:", e);
		}
	}

	
	//@test用
	public item2app testQueryItemID(String itemID) {
		if (itemID == null)
			return null;
		BooleanQuery bq = new BooleanQuery();
		// 对所有query归一化并组建booleanquery
		// term
		TermQuery tq = new TermQuery(new Term("itemID", itemID));
		bq.add(tq, Occur.MUST);
		HitCollector hc = new HitCollector(searcher_item);
		searcher_item.setSimilarity(new MySimilarity2());
		try {
			searcher_item.search(bq, hc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("", e);
			return null;
		}
		LinkedList<ScoreDoc> ll_rt = hc.getScoreDocList();
		LOG.info(new StringBuffer().append(hc.totalHits).append(
				" total matching users.\r\n"));

		// 遍历
		Iterator<ScoreDoc> it = ll_rt.iterator();
		while (it.hasNext()) {
			ScoreDoc sd = it.next();
			try {
				Document doc = searcher_item.doc(sd.doc);
				String str = doc.get("item2app");
				item2app item2app = JsonUtils.fromJson(str, item2app.class);
				return item2app;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	// @test用
	public String testQueryItemIDSolr(String itemID) {
		if (itemID == null)
			return null;
		// 2.组合查询item库
		String s_items = "";
		try {
			s_items = HttpRequest.sendGet(
					"http://10.32.28.116:8080/solr46/item/select", "q=itemid:"+itemID+"&fl=itemid,score,item2app");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "items == null";
		}
		if (s_items == null || s_items.isEmpty())
			return "items == null";
		
		return s_items;
	}

	/**
	 * 为“客户端头条刷新”应用提供推荐数据接口；
	 * 
	 * @param userid
	 *            用户标识；
	 * @param topN threshold
	 *            推荐list的top N；threshold 相似度阈值
	 */
	protected responseData giveMeNews(String userid,int topN,float threshold) {
		if (userid == null || userid.isEmpty())
			return null;

		if(topN <= 0)
			topN = 100;

		responseData res = new responseData();
		res.userID = userid;

		// 1.查询user向量；
		// 2.组合查询item库，分三次查询，计算相似得分（进行向量的余弦夹角计算），并提取相似度最高的前若干条；
		// 3.查询候选集，并补充入数据库

		// 1.从usermodelInterface中查询user向量；
		ArrayList<String> vectors = null;
		try {
			vectors = usermodelInterface.getInstance("query").queryUserVectors(userid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("query user vector:",e);
		}

		// 1.新用户，或者没有建模的用户，返回候选集合
		if (vectors == null) {
			if (backup_items != null) {
				for (item2app i2a : backup_items.al_backup_items) {
					res.recommendData.add(i2a);
					if(res.recommendData.size() > topN)
						break;
				}
			}
			return res;
		}

//		// 2.冷启动用户，如何判定？处理的策略逻辑也会不一样
//		if (vectors.get(0) == null
//				||vectors.get(0).length() < 1024) {
//			if (backup_items != null) {
//				for (item2app i2a : backup_items) {
//					res.recommendData.add(i2a);
//					if(res.recommendData.size() > 100)
//						break;
//				}
//			}
//			return res;
//		}

		// 3.活跃用户，可以进行向量计算查询
		if (vectors.size() != 3)// 异常
		{
			LOG.error(userid + " user vectors num= " + vectors.size());
			return res;
		} 
		
		
		String topic1 = vectors.get(0);
		String topic2 = vectors.get(1);
		String topic3 = vectors.get(2);
		
//		//@test
//		LOG.info(topic1+"\r\n"+topic2+"\r\n"+topic3);
		
		
		// 进行三次向量计算，并合并三次集合，线性归并得分，排序得到结果
		res.recommendData = cmpBestItemsForUser(userid,topic1, topic2, topic3,topN,threshold);
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

		// 4.返回
		return res;
	}

	/**
	 * 根据三个topic向量，计算得到最优的推荐数据；
	 * 
	 * 
	 * @param topic1 topic2 topic3
	 * @param topN 推荐list的前多少
	 * @param threshold 相似度的阈值
	 * @return ArrayList<item2app>，结果数据集合
	 * 
	 */
	protected ArrayList<item2app> cmpBestItemsForUser(String userID,String topic1,
			String topic2, String topic3,int topN,float threshold ) {
		// TODO Auto-generated method stub
		if (searcher_item == null)
			return null;
		if(topN <= 0)
		{
			topN = 100;
		}
		
		if(threshold <= 0)
		{
			threshold = 0.5f;
		}
		
		// 1,三次向量查询,得到全部结果hm_userid2score；
		// 2,对hm_userid2score的结果进行排序，取出top100;
		// 3,在lucene中查询得到具体的item2app信息（主要是item ID、hotlevel信息）根据hotlevel和score，计算出item的新得分，并按新得分进行排序；
		//1...		
		HashMap<Integer, Float> hm_userid2score = cmpTop100DocsInLucene(userID,topic1,topic2,topic3);

		//2...
		ArrayList<Entry<Integer, Float>> docLists = new ArrayList<Entry<Integer, Float>>(
				hm_userid2score.entrySet());
		Collections.sort(docLists, new Comparator<Entry<Integer, Float>>() {
			@Override
			public int compare(Entry<Integer, Float> arg0,
					Entry<Integer, Float> arg1) {
				// TODO
				// Auto-generated
				// method
				// stub
				if (arg0.getValue() > arg1.getValue())
					return -1;
				else if (arg0.getValue() < arg1.getValue())
					return 1;
				else
					return 0;
			}

		});
		
		//3...前topN个doc查找对应的hotlevel，并根据公式计算出新的score
		ArrayList<item2app> al_item2apps = new ArrayList<item2app>(topN);
		for (int i = 0; i < topN && i < docLists.size(); i++) {
			Entry<Integer, Float> doc2Value = docLists.get(i);

			// threshold判断
			if (doc2Value.getValue() < threshold)
				break;

			Document doc = null;
			try {
				doc = searcher_item.doc(doc2Value.getKey());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("error",e);
				continue;
			}
			item2app i2a = JsonUtils.fromJson(doc.get("item2app"),item2app.class);
			if(i2a == null)
			{
				LOG.error("item2app not found:"+doc2Value.getKey());
				continue;
			}
			
	////////@test,特殊处理，对news，mainland，society，ent的hot进行降权，提高多样性；临时手段，不知道效果如何
			float simScore = doc2Value.getValue();
			{
				if ("news,mainland,society,ent".indexOf(i2a.docChannel) >= 0) {
					simScore = simScore * 0.8f;
					if(simScore < threshold)
						continue;
					
					
				}
			}
	/////////////////////////////////////////////////////////////////
			
			//计算出加权得分
			i2a.score = simScore * i2a.hotBoost;
			
			i2a.why = "recommend";
			
//			//@test,加入隐含主题debug
//			i2a.why = i2a.why+"_"+doc.get("topic2");
			
			al_item2apps.add(i2a);		
		}
		//再对al_item2apps排序
		Collections.sort(al_item2apps, new Comparator<item2app>() {
			@Override
			public int compare(item2app arg0,
					item2app arg1) {
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
		
		return al_item2apps;
	}
	
	
	/**
	 * 根据三个topic向量，三次向量查询,得到全部结果并返回
	 * 
	 * 
	 * @param topic1 topic2 topic3
	 * @return HashMap<Integer, Float>，lucene docID 及对应的score
	 * 
	 */
	private HashMap<Integer, Float> cmpTop100DocsInLucene(String userID,String topic1,
			String topic2, String topic3) {

		// 加权公式的参数
		int a1 = 1, a2 = 1, a3 = 1;
		// 平衡因子
		float b = 0.1f;

		HashMap<Integer, Float> hm_userid2score = new HashMap<Integer, Float>(
				1000);
		// TODO Auto-generated method stub
		// topic1
		if (topic1 != null && !topic1.isEmpty()) {
			
//			//@test,dubug用,看下itemmodel中“84019504”是否有topic1
//			{
//				TermQuery tq = new TermQuery(new Term("topic3", "里皮"));
//				BooleanQuery bq = new BooleanQuery();
//				bq.add(tq, Occur.MUST);
//				HitCollector hc1 = new HitCollector(searcher_item);
//				searcher_item.setSimilarity(new MySimilarity2());
//				try {
//					searcher_item.search(bq, hc1);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					LOG.error("", e);
//					return null;
//				}
//				LinkedList<ScoreDoc> ll_rt = hc1.getScoreDocList();
//				// 遍历
//				Iterator<ScoreDoc> it = ll_rt.iterator();
//				while (it.hasNext()) {
//					ScoreDoc sd = it.next();
//					try {
//						Document doc = searcher_item.doc(sd.doc);
//						String i_topic1 = doc.get("topic1");
//						LOG.info(doc.get("itemID")+" topic1:"+i_topic1);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
			
			
			
			String[] topics = topic1.split("\\s");
			BooleanQuery bq = new BooleanQuery();
			// 对所有query归一化并组建booleanquery
			int tf = 0;
			String oldTopic = null;
			for (String topic : topics) {
				if (topic.isEmpty())
					continue;
				if (oldTopic == null) {
					tf = 1;
					oldTopic = topic;
				} else if (oldTopic.equals(topic))
					tf++;
				else {
					// term
					TermQuery tq = new TermQuery(new Term("topic1", oldTopic));
					tq.setBoost(tf);
					bq.add(tq, Occur.SHOULD);

					tf = 1;
					oldTopic = topic;
				}
			}
			
			if(oldTopic != null){
				// term
				TermQuery tq = new TermQuery(new Term("topic1", oldTopic));
				tq.setBoost(tf);
				bq.add(tq, Occur.SHOULD);
			}
		

			HitCollector hc = new HitCollector(searcher_item);
			searcher_item.setSimilarity(new MySimilarity2());
			try {
				searcher_item.search(bq, hc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("", e);
				return null;
			}
			LinkedList<ScoreDoc> ll_rt = hc.getScoreDocList();
			LOG.info(new StringBuffer().append("topic1:").append(hc.totalHits)
					.append(" total matching items.\r\n"));

			
			
			// 遍历，放入hm_userid2score中
			Iterator<ScoreDoc> it = ll_rt.iterator();
			while (it.hasNext()) {
				ScoreDoc sd = it.next();
				Float score = hm_userid2score.get(sd.doc);
				

				//@test
				if(DEBUG&&userID.equals("A000004206BD05"))
					//|| userID.equals("863151026712833")
					//|| userID.equals("A000004206BD05")	
				{
					try {
						LOG.info(searcher_item.doc(sd.doc).get("topic1")+"|"+sd.score+" docid="+searcher_item.doc(sd.doc).get("itemID"));
						LOG.info(searcher_item.explain(bq, sd.doc));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				if (score == null) {
					hm_userid2score.put(sd.doc, a1 * sd.score);
				
				} else {// error?怎么会topic1时候出现不为空的情况？
					LOG.error("deal topic1 but found not null.");
					hm_userid2score.put(sd.doc, a1 * sd.score);
				}
			}
		}

		// topic2
		if (topic2 != null && !topic2.isEmpty()) {
			String[] topics = topic2.split("\\s");
			BooleanQuery bq = new BooleanQuery();
			// 对所有query归一化并组建booleanquery
			int tf = 0;
			String oldTopic = null;
			for (String topic : topics) {
				if (topic.isEmpty())
					continue;
				if (oldTopic == null) {
					tf = 1;
					oldTopic = topic;
				} else if (oldTopic.equals(topic))
					tf++;
				else {
					// term
					TermQuery tq = new TermQuery(new Term("topic2", oldTopic));
					tq.setBoost(tf);
					bq.add(tq, Occur.SHOULD);

					tf = 1;
					oldTopic = topic;
				}
			}
			
			if(oldTopic != null){
				// term
				TermQuery tq = new TermQuery(new Term("topic2", oldTopic));
				tq.setBoost(tf);
				bq.add(tq, Occur.SHOULD);
			}
			
			HitCollector hc = new HitCollector(searcher_item);
			searcher_item.setSimilarity(new MySimilarity2());
			try {
				searcher_item.search(bq, hc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("", e);
				return null;
			}
			LinkedList<ScoreDoc> ll_rt = hc.getScoreDocList();
			LOG.info(new StringBuffer().append("topic2:").append(hc.totalHits)
					.append(" total matching items.\r\n"));

			// 遍历，放入hm_userid2score中
			Iterator<ScoreDoc> it = ll_rt.iterator();
			while (it.hasNext()) {
				ScoreDoc sd = it.next();
				Float score = hm_userid2score.get(sd.doc);
				
				//@test
				if(DEBUG&&userID.equals("A000004206BD05"))
					//|| userID.equals("863151026712833")
					//|| userID.equals("A000004206BD05")	
				{
					try {
						LOG.info(searcher_item.doc(sd.doc).get("topic2")+"|"+sd.score+" docid="+searcher_item.doc(sd.doc).get("itemID"));
						LOG.info(searcher_item.explain(bq, sd.doc));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if (score == null) {
					hm_userid2score.put(sd.doc, a2 * sd.score);
				} else {
					hm_userid2score.put(sd.doc, a2 * sd.score + score);
				}
			}
		}

		// topic3
		if (topic3 != null && !topic3.isEmpty()) {
			String[] topics = topic3.split("\\s");
			BooleanQuery bq = new BooleanQuery();
			bq.setMaxClauseCount(20480); 
			
			//对所有query归一化并组建booleanquery
			int tf = 0,threshold = 0;
			if(topics.length > 100000)
				threshold = 6;
			else if(topics.length > 50000)
				threshold = 2;
			else 
				threshold = 0;
			
			String oldTopic = null;
			for (String topic : topics) {
				if (topic.isEmpty())
					continue;
				if (oldTopic == null) {
					tf = 1;
					oldTopic = topic;
				} else if (oldTopic.equals(topic))
					tf++;
				else {
					//tf太小先忽略
					if(tf >= threshold)
					{	
						// term
						TermQuery tq = new TermQuery(new Term("topic3", oldTopic));
						tq.setBoost(tf);
						bq.add(tq, Occur.SHOULD);
					}
					
					tf = 1;
					oldTopic = topic;
				}
			}
			
			if(oldTopic != null){
				// term
				TermQuery tq = new TermQuery(new Term("topic3", oldTopic));
				tq.setBoost(tf);
				bq.add(tq, Occur.SHOULD);
			}
			

			HitCollector hc = new HitCollector(searcher_item);
			searcher_item.setSimilarity(new MySimilarity2());
			try {
				searcher_item.search(bq, hc);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("", e);
				return null;
			}
			LinkedList<ScoreDoc> ll_rt = hc.getScoreDocList();
			LOG.info(new StringBuffer().append("topic3:").append(hc.totalHits)
					.append(" total matching items.\r\n"));

			// 遍历，放入hm_userid2score中
			Iterator<ScoreDoc> it = ll_rt.iterator();
			while (it.hasNext()) {
				ScoreDoc sd = it.next();
				Float score = hm_userid2score.get(sd.doc);
				
				//@test
				if(DEBUG&&userID.equals("A000004206BD05"))
					//|| userID.equals("863151026712833")
					//|| userID.equals("A000004206BD05")	867156012212595
				{
					try {
						LOG.info(searcher_item.doc(sd.doc)+".topic3|"+sd.score+" docid="+searcher_item.doc(sd.doc).get("itemID"));
						LOG.info(searcher_item.explain(bq, sd.doc));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if (score == null) {
					hm_userid2score.put(sd.doc, a3 * sd.score + b);
				} else {
					hm_userid2score.put(sd.doc, a3 * sd.score + score + b);
				}
			}
		}
		return hm_userid2score;
	}

	/**
	 * 为“客户端头条刷新”应用提供推荐数据接口；
	 * 
	 * @param userid
	 *            用户标识；
	 */
	public void query4TestUsers(int topN,float threshold) {
		// 1.读入测试用户
		// 2.逐个用户请求得到推荐结果
		// 3.放入前端redis中去
		Jedis jedis = new Jedis("10.32.21.62",6379,10000);
		jedis.select(6);
		FileUtil wordidFile = new FileUtil();
		wordidFile.Initialize(fieldDicts.testUsersFile, "UTF-8");
		String line = "";
		while ((line = wordidFile.ReadLine()) != null) {
			String userID = line.trim();
			LOG.info("cmp user:"+userID);
			responseData res = this.giveMeNews(userID,topN,threshold);
			String s_res = JsonUtils.toJson(res);
			//@test
			//jedis.set(userID, s_res);
			
//			LOG.info("recommend is:");
//			LOG.info(s_res);
			LOG.info("---------");
		}

		

	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		// test:先集中修改下一些字段，以方便临时搭建环境
//		fieldDicts.itemModelPath = "D:/workspace/iRecommend4App/testenv/itemmodel/";
//		fieldDicts.modelPath = "D:/workspace/iRecommend4App/testenv/usermodel/";
//		fieldDicts.appTreeMappingFile = "D:/workspace/iRecommend4App/testenv/AppTreeMapping.txt";
//		fieldDicts.frontAppTreeMappingFile = "D:/workspace/iRecommend4App/testenv/APPFront_TreeMapping.txt";
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
		
	

		recommend4HeadLine r4h = new recommend4HeadLine();
		
		if(args[2].equals("debug"))
			r4h.DEBUG = true;
			
		
		while (true) {
			LOG.info("start query:");
			r4h.reload();
			int topN = Integer.valueOf(args[0]);
			float threshold = Float.valueOf(args[1]);
			r4h.query4TestUsers(topN, threshold);
			LOG.info("sleep...");
			try {
				Thread.sleep(5 * 60 * 1000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
