/**
 * 
 */
package com.ifeng.iRecommend.usermodel;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
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

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.commen.Utils.k2v;
import com.ifeng.commen.Utils.stopwordsFilter;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.channelsParser;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.train.usermodelTraining;

/**
 * <PRE>
 * 作用 : 
 *   提供usermodel的建模、相似度计算等对外接口；
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
 *          1.0          2013-7-15        likun          修改一些细节代码，调整为客户端的log和item接口、形式
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class usermodelInterface {
	private static final Log LOG = LogFactory.getLog("usermodel");
	private static usermodelInterface instance = null;

	// index
	// searcher
	// index的新model所在
	// searcher读取的旧model所在
	// doc到user的map关系
	private IndexWriter writer = null;
	private IndexSearcher searcher = null;
	private HashMap<String, String> hm_doc2user = null;
	private HashMap<String, Integer> hm_tagLevels = null;

	private String workingType = null;
	
	private Document doc = null;
	
	
	//各个channel的IDF信息，用于用户channel行为的更精准表达;注意：key的形式是当前day + channel，比如“20130708mil”
	private HashMap<String,Float> hm_channelIDFs  = null;
 	
	
	//lucene index
	private Field idField = null;
	private Field topic1Field = null;
	private Field topic2Field = null;
	private Field topic3Field = null;
	/*
	 * 构造函数；
	 * @param 
	 * 		workingType:工作类型；"modeling":建模用；"query":模型专门用于查询；
	 */
	private usermodelInterface(String workingTypeIn) {
		if(workingTypeIn == null || workingTypeIn.isEmpty())
			return;
		workingType = workingTypeIn;
		if("modeling".equals(workingType))
		{
			// 准备建模newmodel
			Directory dir_new = null;
			try {
				dir_new = FSDirectory.open(new File(fieldDicts.modelPath
						+ fieldDicts.newModelFile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("error:", e);
			}

			try {
				Analyzer myAnalyzer = new WhitespaceAnalyzer(Version.LUCENE_42);
				IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_42,
						myAnalyzer);
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE);
				iwc.setSimilarity(new MySimilarity2()); // 设置计算得分的Similarity
				iwc.setRAMBufferSizeMB(32);
				
				writer = new IndexWriter(dir_new, iwc);
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
			
			//高度重用doument、field结构，提高速度
			doc = new Document();
			topic1Field = new VecTextField("topic1",
					"", Field.Store.YES);// ,Field.Index.ANALYZED);
			topic2Field = new VecTextField("topic2",
					"", Field.Store.YES);// ,Field.Index.ANALYZED);
			topic3Field = new VecTextField("topic3",
					"", Field.Store.YES);// ,Field.Index.ANALYZED);
			idField = new StringField("userID", "", Field.Store.YES);
			doc.add(topic1Field);
			doc.add(topic2Field);
			doc.add(topic3Field);
			doc.add(idField);
			
			try {
				hm_channelIDFs = readChannelIDFs(/*"/data/irecommend4app/tfidf");*/"D:\\workspace\\iRecommend4App\\testenv\\tfidf");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error("readChannelIDFs error",e);
			}
			if(hm_channelIDFs == null)
				hm_channelIDFs = new HashMap<String,Float>();
			
		}
		
		if("query".equals(workingType))
		{
			// 载入model
			searcher = null;
			LOG.info("load user model:");
			boolean rt = this.loadUserModel();
			if(false == rt)
			{
				LOG.error("load user model failed!");
			}
		}

	}
	
	
	
	/*
	 * 从文件中读取channel IDF信息；
	 * 注意：
	 * @param 
	 * 		idfFilePath: channel IDF 文件路径
	 */
	protected static HashMap<String, Float> readChannelIDFs(String idfFilePath) throws Exception {
		// TODO Auto-generated method stub
		if(idfFilePath == null || idfFilePath.isEmpty())
			return null;
		
		//HDFS文件读取
		Configuration conf = null;
		FileSystem hdfs = null;
		conf = new Configuration();
		conf.set("fs.defaultFS", "hdfs://10.32.21.111:8020/");//指定hdfs
		conf.set("hadoop.job.user", "hdfs");
		hdfs = FileSystem.get(conf);
		Path path = new Path(idfFilePath);
		FSDataInputStream fsr = hdfs.open(path);
		BufferedReader bis = new BufferedReader(new InputStreamReader(fsr,
				"utf-8"));
		HashMap<String, Float> res = new HashMap<String, Float>();
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMdd");
		String line = "";
		String d1 = "",d2 = "",d3 = "",d4 = "",d5 = "",d6 = "",d7 = "";
		int allUserNum = 0;
		while ((line=bis.readLine())!= null) {
			if(line.startsWith("2014")){
				
				String[] secs = line.split("\\s");
				String dayBegin = secs[0];
				String dayEnd = secs[1];
				String sAllUserNum = secs[2];
				try {
					allUserNum = Integer.valueOf(sAllUserNum);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					LOG.error("error:"+line,e1);	
					break;
				}
				
				//find seven day
				d1 = dayBegin;
				d7 = dayEnd;
				Date sDate = null;
				try {
					sDate = sDateFormat.parse(dayBegin);
					long milSecTime = sDate.getTime(); 
					Date date=new Date(milSecTime+24*3600*1000L);  
					d2 = sDateFormat.format(date);
					date=new Date(milSecTime+2*24*3600*1000L);  
					d3 = sDateFormat.format(date);
					date=new Date(milSecTime+3*24*3600*1000L);  
					d4 = sDateFormat.format(date);
					date=new Date(milSecTime+4*24*3600*1000L);  
					d5 = sDateFormat.format(date);
					date=new Date(milSecTime+5*24*3600*1000L);  
					d6 = sDateFormat.format(date);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					LOG.error("error:"+line,e1);	
					break;
				}
				continue;
			}
		
			
			if(line.startsWith("-------"))
				continue;
			
			String[] secs = line.split("\\s");
			if(secs.length != 3)
				continue;
			
			//过滤icmstest、video、notopic
			if("icmstest、video、notopic".indexOf(secs[0]) >= 0)
				continue;
			
			res.put(d1+secs[0], (float) Math.log(allUserNum/Float.valueOf(secs[1])));
			res.put(d2+secs[0], (float) Math.log(allUserNum/Float.valueOf(secs[1])));
			res.put(d3+secs[0], (float) Math.log(allUserNum/Float.valueOf(secs[1])));
			res.put(d4+secs[0], (float) Math.log(allUserNum/Float.valueOf(secs[1])));
			res.put(d5+secs[0], (float) Math.log(allUserNum/Float.valueOf(secs[1])));
			res.put(d6+secs[0], (float) Math.log(allUserNum/Float.valueOf(secs[1])));
			res.put(d7+secs[0], (float) Math.log(allUserNum/Float.valueOf(secs[1])));
			
		}
		
		//默认多写入一周
		
		
		
		return res;
	}

	/*
	 * 获取实例；
	 * 注意：一个进程中，不能出现两种workingType
	 * @param 
	 * 		workingType:工作类型；"modeling":建模用；"query":模型专门用于查询；
	 */
	public synchronized static usermodelInterface getInstance(String workingType) {
		if(instance == null)
			instance = new usermodelInterface(workingType);
		return instance;
	}

	/**
	 * 兼容PC和APP两种情况
	 * 根据输入的单个user的增量行为，对其进行增量建模，形成新的usermodel
	 * <p>
	 * 注意要先得到user的旧三维向量；
	 * 对user新阅读的每一个item，计算其抽象表达；按一定规则，合并所有item的抽象表达，形成一个统一抽象，再调用lucene的
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
	 *            http://games.ifeng.com/!7387759!ent 
	 *            http://games2.ifeng.com/!7443314!sports”
	 * @param topic*
	 * 			上次计算得到的三维向量；
	 * @param _mins
	 * 			上次计算最后的时间点；
	 * @return <0表示参数不对；==0表示完全成功；>0表示可能某一个topic有问题；
	 *            
	 */
	public int updateOneUser(String userID, HashMap<String, String> hm_dayitems,ItemOperation itemOP
			,String topic1,String topic2,String topic3
			) {
		if (userID == null || userID.isEmpty())
			return -1;
		if (hm_dayitems == null || hm_dayitems.isEmpty())
			return -2;
		
		int rt = 0;
		
		userDocForSolr ud = this.cmpOneUserDoc(userID, hm_dayitems, itemOP);

		// modeling
		String new_topic1 = ud.cmpTopic1();
		String new_topic2 = ud.cmpTopic2();
		String new_topic3 = ud.cmpTopic3();

		if(topic1 == null)
			topic1 = new_topic1;
		else
			topic1 = topic1+" "+new_topic1;
		
		if(topic2 == null)
			topic2 = new_topic2;
		else
			topic2 = topic2+" "+new_topic2;
		
		if(topic3 == null)
			topic3 = new_topic3;
		else
			topic3 = topic3+" "+new_topic3;
		
		//test,随机输出进行观察
		double rd = Math.random();
		if(rd <= 1)
			commenFuncs.writeResult(fieldDicts.modelPath+"userdocs//",
					ud.userid.replaceAll("[:\\/?*\"|]", "")+"", ud.userid+"\r\n"+topic1+"\r\n"+topic2+"\r\n"+topic3, "utf-8", false, null);

		
		// 建模加入lucene
		synchronized(writer)
		{
//			//@test
//			LOG.info("begin write.adddocument");
			if(topic1 != null)
				topic1Field.setStringValue(topic1);
			else
				rt += 2;
			if(topic2 != null)
				topic2Field.setStringValue(topic2);
			else
				rt += 3;
			if(topic3 != null)
				topic3Field.setStringValue(topic3);
			else
				rt += 4;
			idField.setStringValue(userID);
	
			try {
				writer.addDocument(doc);
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				LOG.error("error:", e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("error:", e);
			}catch (Exception e){
				LOG.error("error:", e);
			}
			
		

//			//@test
//			LOG.info("over write.adddocument");
		}

		return rt;
	}

	/**
	 * 兼容PC和APP两种情况
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
	 *            http://games.ifeng.com/!7387759!ent 
	 *            http://games2.ifeng.com/!7443314!sports”
	 * @param _mins
	 * 			用户此次的计算时间
	 * @return <0表示参数不对；==0表示完全成功；>0表示可能某一个topic有问题；
	 *            
	 */
	public int modelOneUser(String userID, HashMap<String, String> hm_dayitems,ItemOperation itemOP) {
		if (userID == null || userID.isEmpty())
			return -1;
		if (hm_dayitems == null || hm_dayitems.isEmpty())
			return -2;
		
		int rt = 0;
		
		userDocForSolr ud = this.cmpOneUserDoc(userID, hm_dayitems, itemOP);

		// modeling
		String topic1 = ud.cmpTopic1();
		String topic2 = ud.cmpTopic2();
		String topic3 = ud.cmpTopic3();

		
		//test,随机输出进行观察
		double rd = Math.random();
		if(rd <= 1)
			commenFuncs.writeResult(fieldDicts.modelPath+"userdocs//",
					ud.userid.replaceAll("[:\\/?*\"|]", "")+"", ud.userid+"\r\n"+topic1+"\r\n"+topic2+"\r\n"+topic3, "utf-8", false, null);

		
		//建模加入lucene
		synchronized(writer)
		{
//			//@test
//			LOG.info("begin write.adddocument");
			if(topic1 != null)
				topic1Field.setStringValue(topic1);
			else
				rt += 2;
			if(topic2 != null)
				topic2Field.setStringValue(topic2);
			else
				rt += 3;
			if(topic3 != null)
				topic3Field.setStringValue(topic3);
			else
				rt += 4;
			idField.setStringValue(userID);

			try {
				writer.addDocument(doc);
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				LOG.error("error:", e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("error:", e);
			}catch (Exception e){
				LOG.error("error:", e);
			}	

//			//@test
//			LOG.info("over write.adddocument");
		}

		return rt;
	}
	
	
	/*
	 * model增量落地；
	 */
	public void commitModel() {
		//@test
		LOG.info("begin commitModel");
		synchronized(writer)
		{
			try {
				writer.commit();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error("error:", e);
			}
			
			//@test
			LOG.info("over commitModel");
		}
	}
//
//	/*
//	 * 采用某个indexsearcher对某个field执行相似度计算;
//	 */
//	private static LinkedList<ScoreDoc> searchOneIndex(String whichField,
//			IndexSearcher whichsearcher, HashMap<String, Float> vecTermValue,
//			float totalValue) throws Exception {
//		// TODO Auto-generated method stub
//		StringBuffer sbLog = new StringBuffer();
//		//debug output: if totaValue <= 0
//		if(totalValue <= 0){
//			sbLog.append("searching will not be execute because totalValue = ").append(totalValue).append("\r\n");
//			Iterator<Entry<String, Float>> it = vecTermValue.entrySet().iterator();
//			while (it.hasNext()) {
//				Entry<String, Float> et = it.next();
//				String queryTerm = et.getKey();
//				float value = et.getValue();
//				sbLog.append(queryTerm).append(" ").append(value).append("\r\n");
//			}
//			LOG.info(sbLog.toString());
//			throw new Exception("NaN error found");//抛出异常
//		}
//		
//		BooleanQuery bq = new BooleanQuery();
//		// 对所有query归一化并组建booleanquery
//		Iterator<Entry<String, Float>> it = vecTermValue.entrySet().iterator();
//		while (it.hasNext()) {
//			Entry<String, Float> et = it.next();
//			String queryTerm = et.getKey();
//			float value = et.getValue();
//
//			// term
//			TermQuery tq = new TermQuery(new Term(whichField, queryTerm));
//			tq.setBoost(value / totalValue);
//			bq.add(tq, Occur.SHOULD);
//
//			//LOG.info(queryTerm + " " + value / totalValue);
//			sbLog.append(queryTerm).append(" ").append(value / totalValue).append("\r\n");
//		}
//
//		HitCollector hc = new HitCollector(whichsearcher);
//		whichsearcher.setSimilarity(new MySimilarity());
//		whichsearcher.search(bq, hc);
//		LinkedList<ScoreDoc> ll_rt = hc.getScoreDocList();
//		//LOG.info(hc.totalHits + " total matching users");
//		sbLog.append(hc.totalHits).append(" total matching users.\r\n");
//		LOG.info(sbLog.toString());
//		return ll_rt;
//	}
//
//	/**
//	 * 否决，这一系列函数有问题；luceneIndexField有变化
//	 * 计算item的最合适的投放user list，及对应score
//	 * <p>
//	 * 对item，计算其抽象表达；按一定规则，合并所有item的抽象表达，形成一个统一抽象；
//	 * 进入lucene，按照指定rank比例，计算其最匹配user rank list；
//	 * </p>
//	 * 
//	 * @param itemID
//	 *            ,item的url信息；
//	 * @param baseLine
//	 *            ，item投放的base rank比例；
//	 * @param hotLevel
//	 *            ，item的hot level；
//	 * @param category 
//	 * 			     ，item的前端归类；
//	 * @return LinkedList<Map<String,Float>>,item最合适的user list，<userid-score>；
//	 */
//	public LinkedList<k2v<Float>> cmpBestUserListForItem(Item oneItem,
//			String hotLevel, String category) {
//		LOG.info("now we cmp best rank users for:" + oneItem.getTitle());
//		if (oneItem == null || hotLevel == null || category == null)
//			return null;
//
//		StringBuffer sbLog = new StringBuffer();
//		
//		String whichField = fieldDicts.luceneIndexField;
//
//		HashMap<String, Float> hm_tagValuesAll = new HashMap<String, Float>();
//
//		float totalValue = 0f;
//
//		// 得到channels信息
//		HashMap<String, Float> hm_tagValues = itemAbstraction
//				.cmpChannels(oneItem,oneItem.getUrl());
//		if (hm_tagValues != null) {
//			hm_tagValuesAll.putAll(hm_tagValues);
//		}
//
//		// 得到top tags
//		hm_tagValues = itemAbstraction.getItemTopWords(oneItem, false);
//		if (hm_tagValues != null) {
//			hm_tagValuesAll.putAll(hm_tagValues);
//		}
//
//		// 得到隐含主题
//		hm_tagValues = itemAbstraction.cmpLatentTopics(hm_tagValues);
//		if (hm_tagValues != null) {
//			hm_tagValuesAll.putAll(hm_tagValues);
//		}
//
//		Iterator<Entry<String, Float>> it = hm_tagValuesAll.entrySet()
//				.iterator();
//		while (it.hasNext()) {
//			totalValue += it.next().getValue();
//		}
//
//		LinkedList<ScoreDoc> ll_rt = null;
//		try {
//			ll_rt = searchOneIndex(whichField, searcher, hm_tagValuesAll,
//					totalValue);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			LOG.error("error:", e);
//			return null;
//		}
//
//		//LOG.info("ll_rt size:" + ll_rt.size());
//		sbLog.append("ll_rt size:").append(ll_rt.size());
//		// sort
//		Collections.sort(ll_rt, new Comparator<ScoreDoc>() {
//			@Override
//			public int compare(ScoreDoc arg0, ScoreDoc arg1) {
//				// TODO
//				// Auto-generated
//				// method
//				// stub
//				if (arg0.score > arg1.score)
//					return -1;
//				return 1;
//			}
//
//		});
//
//		// //@test
//		// for(int i=0;i<ll_rt.size();i++){
//		// ScoreDoc sd = ll_rt.get(i);
//		//System.out.println(sd.doc+"|"+sd.score);
//		// }
//		// System.out.println("--------");
//
//		// rank比例投放并找到具体的userid
//		Float baseRankLine = fieldDicts.hm_BaseRankLines.get(category);
//		if(baseRankLine == null)
//			baseRankLine = fieldDicts.hm_BaseRankLines.get("other");
//		
//		sbLog.append(" baseRankLine:").append(baseRankLine).append("\r\n");
//		
//		LinkedList<k2v<Float>> ll_res = new LinkedList<k2v<Float>>();
//		int bestUserSize = (int) (ll_rt.size() * baseRankLine * fieldDicts.hm_itemHotLevels
//				.get(hotLevel));
//		//LOG.info("bestUserSize:" + bestUserSize);
//		sbLog.append(" bestUserSize:").append(bestUserSize).append("\r\n");
//		Iterator<ScoreDoc> it_scoreDoc = ll_rt.iterator();
//		while (it_scoreDoc.hasNext() && bestUserSize-- > 0) {
//			ScoreDoc sd = it_scoreDoc.next();
//			String sdocid = String.valueOf(sd.doc);
//			String userid = hm_doc2user.get(sdocid);
//			if (userid == null) {
//				Document doc = null;
//				try {
//					doc = searcher.doc(sd.doc);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					LOG.error("error:", e);
//					continue;
//				}
//				userid = doc.get("userID");
//				hm_doc2user.put(sdocid, userid);
//			}
//
//			// adjust score；将热度值与实际score相乘，使更热的item能得到更大的score，从而更可能出现；
//			sd.score = (float) (sd.score * Math.sqrt(fieldDicts.hm_itemHotLevels.get(hotLevel)));
//
//			//score低于阈值，过滤
//			if(sd.score < fieldDicts.minMatchScore)
//				break;
//			k2v<Float> esf = new k2v<Float>(userid, sd.score);
//			ll_res.add(esf);
//		}
//
//		//LOG.info("cmp best rank users finished.");
//		sbLog.append("cmp best rank users finished.\r\n");
//		LOG.info(sbLog.toString());
//		return ll_res;
//	}

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
	 * 注意：user的单个行为，同一时间段内的合并；这个时间段有两个评价因子：_mins1、_mins2，只要有任何一个命中，那么就认为属于同一时间段，可以合并；
	 * 
	 * @param userID
	 *            用户ID
	 * @param hm_dayitems
	 *            ，user按天为单位的item行为集合； 比如“20130709
	 *            http://games.ifeng.com/!7387759
	 *            http://games2.ifeng.com/!7443314”
 	 * @param itemOP
	 *          item的查询接口
	 */
	public userDocForSolr cmpOneUserDoc(String userID, HashMap<String, String> hm_dayitems,ItemOperation itemOP) {
		if (userID == null || userID.isEmpty())
			return null;
		if (hm_dayitems == null || hm_dayitems.isEmpty())
			return null;
		if (itemOP == null)
			return null;
		
		//后台存储计算引擎solr的数据接口
		userDocForSolr ud = new userDocForSolr();
		ud.userid = userID;
		
		//@test
		int sumQueryItemTime = 0;
		
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
				String imcp_id = secs[i];//pc应用中是url，app应用中是imcp_id
				String _mins1 = secs[i + 1];
				String _mins2 = secs[i + 2];
				String _channel = secs[i + 3];
				
				//专题的信息：
				//...
				
				// 得到item具体信息，并合并1分钟内相似标题
				Item oneItem = null;
				//@test
//				long b = System.currentTimeMillis();
				if(imcp_id.matches("\\d{5,16}"))
				{
					oneItem = itemOP.getItem(imcp_id);
				}
//				long c = System.currentTimeMillis();
//				//@test
//				System.out.println(imcp_id+"|"+(c-b));
//				sumQueryItemTime += c-b;
				
				// 进行_mins内的排重
				if (oneItem != null) {
					
//					//@test
//					System.out.println(oneItem.getID());
//					System.out.println(oneItem.getUrl());
//					System.out.println(oneItem.getChannel());
//					System.out.println(oneItem.getTitle());
					
					
					String title = oneItem.getTitle();// 新能源_nz 汽车产业_nz 路线图_n 敲定_v _w
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
						
						//得到另外一个time段
						ArrayList<String[]> al_titles2 = hm_minsTitles2
								.get(_mins2);
						if (al_titles2 == null) {
							al_titles2 = new ArrayList<String[]>();
							hm_minsTitles2.put(_mins2, al_titles2);
						}
						
						if (isSame) {
							LOG.info("title is same 1,abandon imcp_id,url:" +imcp_id+","+ oneItem.getUrl());
							continue;
						}
						
						//time2
						for (int j = 0; j < al_titles2.size(); j++) {
							if (commenFuncs.simRate(titleSecs,
									al_titles2.get(j)) >= 0.9f) {
								isSame = true;
								break;
							}
						}
						if (isSame) {
							LOG.info("title is same 2,abandon imcp_id,url:" +imcp_id+","+ oneItem.getUrl());
							continue;
						}
						
						
						// remember in cache
						al_titles1.add(titleSecs);
						al_titles2.add(titleSecs);
						
					}
				}
//				else{//debug,test
//					System.out.println("not found item:"+imcp_id);
//				}
			
				// 构建三维topic
				HashMap<String, Float> hm_tagValues = itemAbstraction
						.cmpChannelsWithIDFs(oneItem,imcp_id,day,hm_channelIDFs);
				
				if (hm_tagValues == null || hm_tagValues.isEmpty()) {
					hm_tagValues = itemAbstraction.cmpChannelsWithIDFs(oneItem, _channel,day,hm_channelIDFs);
				}
				
//				//@test
//				if(hm_tagValues.containsKey("http%3A%2F%2Fi.ifeng.com%2Fsports%2Fworldcup2014%2Fwcqiudui%3Fch%3Dclient"))
//				{
//					System.out.println("fuck wmj");
//				}
				
				// 得到channels信息
				if (hm_tagValues != null && !hm_tagValues.isEmpty()) {
					//hm_tagValues = itemAbstraction.normalization(hm_tagValues);
		
					// 合并入user抽象表达
					ud.add(hm_tagValues, "topic1");
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
						ud.add(hm_topics,"topic2");
					}

					// 将细化标题等tag放入ud,合并入user抽象表达
					hm_tagValues = itemAbstraction.normalization(hm_tagValues);
					ud.add(hm_tagValues,"topic3");
				}

			}
		}
	
		//@test
		//System.out.println("sumQueryItemTime:"+sumQueryItemTime);
		
//		// modeling
//		String s_doc = JsonUtils.toJson(ud, userDocForSolr.class);
//
//		//test,随机输出进行观察
//		double rd = Math.random();
//		if(rd <= 0.01
//				|| userID.equals("1326527269181_8701")
//				|| userID.equals("1378692638323_han6mm7272")
//				|| userID.equals("1379493473664_dm27cb7565")
//				|| userID.equals("1383809832507_b11zpn3800"))
//			commenFuncs.writeResult(fieldDicts.modelPath+"userdocs//",
//					ud.userid, s_doc, "utf-8", false, null);

        return ud;
        
	}

	
	/**
	 * 执行一次简单查询，以得到并记录user--doc的映射关系 注意：必须在searcher有效情况下执行；
	 */
	public void cmpUser2DocMap() {
		if (searcher == null)
			return;
		LOG.info("cmp user-doc map:");
		BooleanQuery bq = new BooleanQuery();
		TermQuery tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "zc"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "lanqiu"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "society"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "ent"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "finance"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "mil"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "fashion"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "美女"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "中国"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "明星"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "world"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "finance"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "news"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "sports"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		tq = new TermQuery(new Term(fieldDicts.luceneIndexField, "history"));
		tq.setBoost(1.0f);
		bq.add(tq, Occur.SHOULD);
		
		HitCollector hc = new HitCollector(searcher);
		searcher.setSimilarity(new MySimilarity2()); // DefaultSimilarity());
		try {
			searcher.search(bq, hc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("error:", e);
			return;
		}
		LinkedList<ScoreDoc> ll_rt = hc.getScoreDocList();
		LOG.info(hc.totalHits + " total matching documents");
		Iterator<ScoreDoc> it = ll_rt.iterator();

		HashMap<String, String> hm_doc2user = new HashMap<String, String>();
		// cmp map relationship
		while (it.hasNext()) {
			ScoreDoc sd = it.next();
			String sdocid = String.valueOf(sd.doc);
			// cache中有没有
			String sID = hm_doc2user.get(sdocid);
			if (sID == null || sID.isEmpty()) {
				// long a = System.currentTimeMillis();
				Document doc = null;
				try {
					doc = searcher.doc(sd.doc);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOG.error("error:", e);
					continue;
				}
				sID = doc.get("userID");
				hm_doc2user.put(sdocid, sID);
			}
		}

		// cache入地
		// 备份
		File f = new File(fieldDicts.modelPath + fieldDicts.docUserMapFile);
		if (f.exists()) {
			File dest = new File(fieldDicts.modelPath
					+ fieldDicts.docUserMapFile + ".bak");
			if (dest.exists()) {
				dest.delete();
			}
			f.renameTo(dest);
		}

		commenFuncs.writeResult(fieldDicts.modelPath,
				fieldDicts.docUserMapFile, "", "utf-8", false, null);
		StringBuffer sbCache = new StringBuffer();
		Set<Entry<String, String>> es = hm_doc2user.entrySet();
		Iterator<Entry<String, String>> it2 = es.iterator();
		while (it2.hasNext()) {
			Entry<String, String> et = it2.next();
			sbCache.append(et.getKey()).append(" ").append(et.getValue())
					.append("\r\n");
			if (sbCache.length() > 8092) {
				commenFuncs.writeResult(fieldDicts.modelPath,
						fieldDicts.docUserMapFile, sbCache.toString(), "utf-8",
						true, null);
				sbCache.delete(0, sbCache.length());
			}
		}
		commenFuncs.writeResult(fieldDicts.modelPath,
				fieldDicts.docUserMapFile, sbCache.toString(), "utf-8", true,
				null);
	}

	/**
	 * 加载当前usermodel；加锁，避免切换新旧model出错；
	 * 如果old model不存在，返回false；加载失败；
	 * 
	 * @return boolean 是否加载成功；
	 */
	public boolean loadUserModel() {
		Directory dir_old = null;
		try {
			dir_old = FSDirectory.open(new File(fieldDicts.modelPath
					+ fieldDicts.oldModelFile));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("error:", e);
			return false;
		}

		if (searcher == null) {
			try {
				searcher = new IndexSearcher(DirectoryReader.open(dir_old));
				// searcher.setSimilarity(new MySimilarity());
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("error:", e);
				return false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error("error:", e);
				return false;
			}

			File f = new File(fieldDicts.modelPath
					+ fieldDicts.docUserMapFile);
			if(!f.exists())
				cmpUser2DocMap();
			
			// 载入doc2user的map关系
			hm_doc2user = readCacheMapFromLocal(fieldDicts.modelPath
					+ fieldDicts.docUserMapFile);

		} else {
			// 同步互斥
			synchronized (searcher) {
				try {
					searcher = new IndexSearcher(DirectoryReader.open(dir_old));
					// searcher.setSimilarity(new MySimilarity());
				} catch (CorruptIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOG.error("error:", e);
					return false;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOG.error("error:", e);
					return false;
				}

				// 载入doc2user的map关系
				hm_doc2user = readCacheMapFromLocal(fieldDicts.modelPath
						+ fieldDicts.docUserMapFile);
			}
		}
		return true;
	}

	/*
	 * 从本地读入map关系文件；文件格式是按行，每行用空格分开key和value；
	 * 
	 * @param path,文件路径；
	 * 
	 * @return key-value的hash内存结构；
	 */
	public static HashMap<String, String> readCacheMapFromLocal(String path) {
		// TODO Auto-generated method stub
		HashMap<String, String> hm_cache = new HashMap<String, String>();
		FileUtil fu = new FileUtil();
		if (fu.Initialize(path, "UTF-8")) {
			try {
				String rawUserData = null;
				while ((rawUserData = fu.ReadLine()) != null) {
					if (!rawUserData.isEmpty()) {
						String[] secs = rawUserData.split("\\s");
						if (secs.length != 2)
							continue;
						hm_cache.put(secs[0].trim(), secs[1].trim());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				fu.CloseRead();
			}
		}
		return hm_cache;
	}

	
	
	/*
	 * 根据userID查询得到user doc向量;
	 * @return
	 * 		一个string list:topic1/topic2/topic3
	 */
	public ArrayList<String> queryUserVectors(String userID) throws Exception {
		// TODO Auto-generated method stub
		if(searcher == null||userID == null ||userID.isEmpty())
			return null;
		HitCollector hc = new HitCollector(searcher);
		searcher.setSimilarity(new MySimilarity2());
		Term idTerm = new Term("userID",userID);
		searcher.search(new TermQuery(idTerm),hc);
		LinkedList<ScoreDoc> ll_rt = hc.getScoreDocList();
		Iterator<ScoreDoc> it = ll_rt.iterator();
		if(it.hasNext()){
			ArrayList<String> res = new ArrayList<String>(3);
			ScoreDoc sd = it.next();
			Document userDoc = searcher.doc(sd.doc);
			String userid = userDoc.get("userID");
			if(!userid.equals(userID))
				return null;
			res.add(userDoc.get("topic1"));
			res.add(userDoc.get("topic2"));
			res.add(userDoc.get("topic3"));
			return res;
		}
		return null;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// //test
		// int tf = 3;
		// int docfreq = 1;
		// int realAllDocsNum = 1300000;
		// double value = Math.pow(tf,0.33)
		// * (Math.log(realAllDocsNum / (double) (docfreq + 1)));
		// System.out.println(value);

	}

}
