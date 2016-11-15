/**
 * 
 */
package com.ifeng.iRecommend.front.recommend2;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.time.DateUtils;
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
import com.ifeng.iRecommend.dingjw.front_rankModel.appRankModel;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation;
import com.ifeng.iRecommend.likun.hotpredict.CommentsItem;
import com.ifeng.iRecommend.likun.hotpredict.heatPredict;
import com.ifeng.iRecommend.train.usermodelTraining;
import com.ifeng.iRecommend.usermodel.HitCollector;
import com.ifeng.iRecommend.usermodel.MySimilarity2;
import com.ifeng.iRecommend.usermodel.VecTextField;
import com.ifeng.iRecommend.usermodel.usermodelInterface;

/**
 * <PRE>
 * 作用 : 
 *   1）执行item的建模和投放（rankmodel处理后存入lucene中）；
 *   *item的建模程序；特点是实时动态，需要频繁修改item在oscache、lucene中存储库；
 *   执行item的lucene存储修改逻辑；操作有add、degr、del；
 *   步骤上是item先经过rankmodel筛选归并处理并放入oscache，然后传入本模块，执行item的solr存储修改逻辑；
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
public class itemDis {
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
		@Expose
		public String others;//备注字段，放入其它信息，比如替换掉的ID等；
		
		public item2app(RankItem rankItem) {
			docId = rankItem.getID();
			title = rankItem.getTitle();
			docChannel = rankItem.getCategory();
			why = "";
			others = "";
			score = 0;
			hotBoost = 1;
			docType = rankItem.getDocType();
			Item item = rankItem.getItem();
			if(item!=null)
				date = item.getDate();
//			if(date == null || date.isEmpty())
//				date = rankItem.getCreateTimeStamp();
			
			//重新计算hotlevel，比如A降权后应该是B，这样可以用于推荐数据debug，以及相似度得分的加权计算
			/*
			 * 计算item的doc boost
			 * weight的公式是：sqrt(目前hotlevel对应weight) / sqrt(初始hotlevel对应weight)
			 */
			//计算降权分数
			String first_hotLevel = rankItem.getWeight();//初始hotlevel值
			//如果是AA，则不变化
			if(first_hotLevel.equals("AA"))
			{
				hotLevel = "AA";
				hotBoost = 1;
			}else{
				int grade = 'E' - first_hotLevel.charAt(0);
				hotLevel = String.valueOf((char) (first_hotLevel
						.charAt(0) + (grade - rankItem.degraded)));
//old				if (fieldDicts.hm_itemHotLevels.get(first_hotLevel) > 0
//						&& fieldDicts.hm_itemHotLevels.get(hotLevel) > 0) {
//					hotBoost = (float) ( Math.sqrt(fieldDicts.hm_itemHotLevels
//									.get(hotLevel)) / Math
//							.sqrt(fieldDicts.hm_itemHotLevels.get(first_hotLevel)));
//				}
				
				//new logic;sigmod函数
				hotBoost = (float)(commenFuncs.sigmoid(fieldDicts.hm_itemHotLevels.get(hotLevel)));
				
			}
			
			//提取rankitem中的ID替换等信息，并放入others字段中
			{
				String others = rankItem.getOthers();
				int b = others.indexOf("simID=");
				if (b > 0) {
					int e = others.indexOf("|!|", b + 6);
					if (e > b)
						this.others = others.substring(b + 6, e);
					else
						this.others = others.substring(b + 6);
				}
				
				//临时处理，将自媒体标记放入other字段中去，以供后续能出逻辑
				if(others.indexOf("wemedialevel")>=0){
					this.others = this.others + "|!|wemedia";
				}
				
				//放入imgNum
				others = rankItem.getItem().getOther();
				b = others.indexOf("imgNum=");
				if (b > 0) {
					int e = others.indexOf("|!|", b + 7);
					if (e > b)
						this.others = this.others + "|!|" + others.substring(b,e);
					else if((b+8) <= others.length())
						this.others = this.others + "|!|" + others.substring(b,b+8);
				}
				
				//放入抓取得到的quality
				b = others.indexOf("qualitylevel=");
				if (b > 0) {
					int e = others.indexOf("|!|", b + 13);
					if (e > b)
						this.others = this.others + "|!|" + others.substring(b,e);
					else if((b+14) <= others.length())
						this.others = this.others + "|!|" + others.substring(b,b+14);
				}
		
				//放入source=yidianzixun
				b = others.indexOf("source=");
				if (b > 0) {
					int e = others.indexOf("|!|", b + 7);
					if (e > b)
						this.others = this.others + "|!|" + others.substring(b,e);
					else if((b+8) <= others.length())
						this.others = this.others + "|!|" + others.substring(b,b+8);
				}
				
			
			}
//			//new， 会放入所有others字段信息
//			this.others = rankItem.getOthers();
			
			
			
		}
		
		/**
		 * 转换成新的rankitem；
		 * 
		 * @return
		 * 		返回新rankitem
		 */
		public RankItem toRankItem() {
			RankItem r_item = new RankItem();
			r_item.setID(this.docId);
			r_item.setWeight(this.hotLevel);
			r_item.setOthers(this.others);
			r_item.setDocType(this.docType);
			r_item.setTitle(this.title);
			r_item.setPublishDate(date);
			return r_item;	
		}
		
	}

	/*
	 * 候选集，冷启动用户，以及补充数据用；
	 */
	class specialItems {
		@Expose
		ArrayList<item2app> al_special_items;
		
		specialItems(){
			al_special_items = new ArrayList<item2app>(200);
		}
		
		public void clear(){
			al_special_items.clear();
		}
		
		public void add(item2app i2a){
			if(i2a != null)
				al_special_items.add(i2a);
		}
		
		/**
		 * 合并新数据入目前数据池中
		 * 
		 * @param al_i2a
		 *           item list new
		 * 注意：
		 * 		需要做排重处理
		 */
		public void combine(ArrayList<item2app> al_i2a){
			if(al_i2a == null || al_i2a.isEmpty())
				return;
			HashSet<String> hs_tmp = new HashSet<String>();	
			for(item2app i2a:al_special_items){
				if(i2a.title == null)
					continue;
				hs_tmp.add(i2a.docId);
				hs_tmp.add(i2a.title.trim());
			}
			
			for(item2app i2a:al_i2a){
				if(i2a.title == null)
					continue;
				if(hs_tmp.contains(i2a.docId) || hs_tmp.contains(i2a.title.trim()))
					continue;
				al_special_items.add(i2a);
			}
			
		}
	}
	
	private static final Log LOG = LogFactory.getLog("itemDis");

	// lucene index
	private IndexWriter writer = null;
	private Field idField = null;
	private Field topic1Field = null;
	private Field topic2Field = null;
	private Field topic3Field = null;
	private Field item2appField = null;
	private Document doc = null;
	
	//debug模式
	public boolean DEBUG;
	
	public itemDis() {
		// 1.item lucene 库的writer和searcher
		Directory dir_item = null;
		try {
			dir_item = FSDirectory.open(new File(fieldDicts.itemModelPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("error:", e);
		}

		try {
			Analyzer myAnalyzer = new WhitespaceAnalyzer(Version.LUCENE_42);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_42,
					myAnalyzer);
			// Add new documents to an existing index:
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			iwc.setSimilarity(new MySimilarity2()); // 设置计算得分的Similarity
			iwc.setRAMBufferSizeMB(32);

			writer = new IndexWriter(dir_item, iwc);
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

		// 2.高度重用doument、field结构，提高速度
		doc = new Document();
		topic1Field = new VecTextField("topic1", "", Field.Store.YES);// ,Field.Index.ANALYZED);
		topic2Field = new VecTextField("topic2", "", Field.Store.YES);// ,Field.Index.ANALYZED);
		topic3Field = new VecTextField("topic3", "", Field.Store.YES);// ,Field.Index.ANALYZED);
		idField = new StringField("itemID", "", Field.Store.YES);
		item2appField = new StringField("item2app","", Field.Store.YES);
		doc.add(topic1Field);
		doc.add(topic2Field);
		doc.add(topic3Field);
		doc.add(idField);
		doc.add(item2appField);
	}

	/**
	 * (单线程版本) 对list中的item进行集中计算和投放，内容匹配算法和话题追踪算法用 
	 * 
	 * @param itemList
	 *            待投放的item list
	 * @param opType
	 *            投放类型：add\degr\del
	 * 
	 *            注意：目前的所有操作类型，前提是保证了一个item从投放开始，就不会
	 *            发生item内容改变，也不会发生hotlevel改变，也就不会出现投放人群的改变；
	 *            具体降权逻辑，将通过修改zset的score域来实现；
	 */
	private void cmpAndDistributeSomeItems(ArrayList<RankItem> itemList,
			final String opTypeIn) {
		// TODO Auto-generated method stub
		if (itemList.size() <= 0)
			return;

		LOG.info("contentMatch:"+opTypeIn + " item list size:" + itemList.size());
		Iterator<RankItem> it = itemList.iterator();
		StringBuffer sbTmp = new StringBuffer();
		while (it.hasNext()) {
			RankItem r_item = it.next();
			Item item = r_item.getItem();
			if (item == null) {
				item = new Item();
			}

			sbTmp.delete(0, sbTmp.length());
			sbTmp.append("distribute one item:").append(opTypeIn)
					.append("\r\n");
			sbTmp.append(r_item.getID()).append("\r\n");
			sbTmp.append(r_item.getUrl()).append("\r\n");
			if (r_item.getFrontTitleSplited() != null)
				sbTmp.append(
						r_item.getFrontTitleSplited().replaceAll("[\r\n]", ""))
						.append("\r\n");
			else
				sbTmp.append("\r\n");

			if (item != null && item.getTitle() != null)
				sbTmp.append(item.getTitle().replaceAll("[\r\n]", ""));
			else
				sbTmp.append("null");
			sbTmp.append("\r\n");
			sbTmp.append(r_item.getWeight()).append("\r\n");

			// url和title补充修正
			if (item.getTitle() == null || item.getTitle().isEmpty()) {
				item.setTitle("");
				item.setUrl(r_item.getUrl());
			}

//			// 存储并入索引中
//			save2Index(r_item, opTypeIn);
//
			//同步写入solr库中
			save2IndexSolr(r_item, opTypeIn);

			//转发指令给track算法
			distribute2Track(r_item,opTypeIn);
			
			sbTmp.append("--------");
			LOG.info(sbTmp.toString());
//			// 休息1-2s
//			try {
//				Thread.sleep(1000);
//				sbTmp.append("sleep 1s...");
//				LOG.info(sbTmp.toString());
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				LOG.info(sbTmp.toString());
//			}

		}

		LOG.info("finish some item's cmp and distributation:" + opTypeIn);

	}

	
	
	
	
	/**
	 * 把内容并入index；lucene版本
	 * 
	 * @param i2d
	 *            具体内容
	 * @param opTypeIn
	 *            投放类型：add\degr\del
	 * 
	 *            add操作逻辑：并入索引中； del操作逻辑：从索引中del； degr操作逻辑：修改索引中的doc.boost
	 */
	public int save2Index(RankItem r_item, String opTypeIn) {
		// TODO Auto-generated method stub
		if (r_item == null || opTypeIn == null)
			return -1;

		int rt = 0;
		if (opTypeIn.equals("add") || opTypeIn.equals("degr")) {
			// 组装additem
			addItem aitem = new addItem(r_item);
			// 建模加入lucene
			synchronized (writer) {
				
				//@test
				LOG.info("begin write.adddocument");
				
				if (aitem.doc.topic1 != null)
					topic1Field.setStringValue(aitem.doc.topic1);
				else{
					topic1Field.setStringValue("");
					rt += 2;		
				}
				if (aitem.doc.topic2 != null)
					topic2Field.setStringValue(aitem.doc.topic2);
				else{
					topic2Field.setStringValue("");
					rt += 3;
				}
				if (aitem.doc.topic3 != null)
					topic3Field.setStringValue(aitem.doc.topic3);
				else{
					topic2Field.setStringValue("");
					rt += 4;
				}
				idField.setStringValue(aitem.doc.itemid);

				//生成item2app
				item2app i2a = new item2app(r_item);
				
//				//@test
//				if(!r_item.getWeight().equals("D")){
//					LOG.info(r_item.getID()+" "+r_item.getWeight()+" "+r_item.getWeightFromOutside());
//					LOG.info(i2a.docId+" "+i2a.hotLevel+" "+i2a.hotBoost);
//					LOG.info("--------");
//				}
				
				
				item2appField.setStringValue(JsonUtils.toJson(i2a));
				//@test
				LOG.info("create i2a:"+i2a.docId);
				try {
					//先删除
					Term term = new Term("itemID", r_item.getID());
					writer.deleteDocuments(term);
					//再添加
					writer.addDocument(doc);
				} catch (CorruptIndexException e) {
					// TODO Auto-generated catch block
					LOG.error("error:", e);
					rt--;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOG.error("error:", e);
					rt--;
				} catch (Exception e) {
					LOG.error("error:", e);
					rt--;
				}
				//@test
				LOG.info("finish write i2a:"+i2a.docId);
				// //@test
				// LOG.info("over write.adddocument");
			}

		}

		if (opTypeIn.equals("del")) {

			// 建模加入lucene
			synchronized (writer) {

				try {
					Term term = new Term("itemID", r_item.getID());
					writer.deleteDocuments(term);
				} catch (CorruptIndexException e) {
					// TODO Auto-generated catch block
					LOG.error("error:", e);
					rt--;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOG.error("error:", e);
					rt--;
				} catch (Exception e) {
					LOG.error("error:", e);
					rt--;
				}

				// //@test
				// LOG.info("over write.adddocument");
			}

		}

		return rt;

	}


	/**
	 * 把内容commit入index；solr版本
	 */
	public int commit2Solr() {
		//commit
		String rt = HttpRequest.sendGet("http://10.32.28.119:8080/solr46/item/update", "commit=true");
		//test
		if(rt.indexOf("failed") > 0)
		{
			LOG.info("commit failed");
			return -1;
		}
		// @test
		LOG.info("success, commit");
		
		
//		String sUrl = appendParam(solrUrl.toString(), "commit=true");
//
//		try {
//			URL url = new URL(sUrl);
//
//			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
//			int iTryNO = 0;
//			while (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
//				try {
//					if (iTryNO > 20) {
//						MessageUtil.fatal("Solr returned an error #ErrorCode="
//								+ urlc.getResponseCode() + " ErrorMessage="
//								+ urlc.getResponseMessage());
//						MessageUtil.fatal("posting data to " + sUrl
//								+ " failed");
//						break;
//					}
//					iTryNO++;
//					MessageUtil.info("try posting data to " + sUrl
//							+ ":" + iTryNO );
//					Thread.sleep(15000);// 歇会儿
//					urlc = (HttpURLConnection) url.openConnection();
//				} catch (Exception ex) {
//					log.error("An error occured posting data to " + sUrl,ex);
//				}
//			}
//		} catch (MalformedURLException e) {
//			MessageUtil.fatal("The specified URL " + sUrl
//					+ " is not a valid URL. Please check");
//		} catch (IOException e2) {
//			MessageUtil.fatal("An error occured posting data to " + sUrl
//					+ ". Please check that Solr is running.");
//		}
		
		return 1;
	}
	
	/**
	 * 把内容指令转发给track算法，走统一内容控制；
	 * 
	 * @param i2d
	 *            具体内容
	 * @param opTypeIn
	 *            投放类型：add\del
	 * */
	public int distribute2Track(RankItem r_item, String opTypeIn) {
		// TODO Auto-generated method stub
		if (r_item == null || opTypeIn == null)
			return -1;

		if (opTypeIn.equals("add")) {
			//投放数据,addItemForTrack,新闻追踪用
			try {
				Jedis jedis = new Jedis("10.50.6.126",6379,10000);
				jedis.select(7);
				LOG.info("distribute2Track,add:" + r_item.getID());
				itemForTrack dItem = new itemForTrack(r_item);
				jedis.rpush("items_from_rankmodel", JsonUtils.toJson(dItem));
				// @test
				LOG.info("success,add:" + r_item.getID());
			}catch (Exception e) {
				LOG.error("ERROR", e);
			}
		
			
		}

		if (opTypeIn.equals("del")) {
			//投放数据,addItemForTrack,新闻追踪用
			try {
				Jedis jedis = new Jedis("10.50.6.126",6379,10000);
				jedis.select(7);
				LOG.info("distribute2Track,del:" + r_item.getID());
				jedis.rpush("items_from_rankmodel",r_item.getID());
				//@test
				LOG.info("success,del:" + r_item.getID());
			}catch (Exception e) {
				LOG.error("ERROR", e);
			}
		}

		return 1;

	}
	
	
	/**
	 * 把内容并入index；solr版本
	 * 
	 * @param i2d
	 *            具体内容
	 * @param opTypeIn
	 *            投放类型：add\degr\del
	 * 
	 *            add操作逻辑：并入索引中； del操作逻辑：从索引中del； degr操作逻辑：修改索引中的doc.boost
	 */
	public int save2IndexSolr(RankItem r_item, String opTypeIn) {
		// TODO Auto-generated method stub
		if (r_item == null || opTypeIn == null)
			return -1;

		if (opTypeIn.equals("add") || opTypeIn.equals("degr")) {
			// @test
			LOG.info("begin write i2a:" + r_item.getID());
			
			// 组装additem
			addItem aitem = new addItem(r_item);
			//生成item2app
			item2app i2a = new item2app(r_item);
			aitem.doc.item2app = JsonUtils.toJson(i2a);
			
			StringBuffer sb_cmd = new StringBuffer();
			// add":{"boost":2.5,"doc":{"itemid":"zengnjin0","topic1":"出土 大使 大使","item2app":"dfasf dsaf"}}
			sb_cmd.append("{\"add\":");
			String s_docs = JsonUtils.toJson(aitem);
			//verify
			if (s_docs == null || s_docs.isEmpty()) {
				LOG.error("s_docs == null");
				return -1;
			}
			sb_cmd.append(s_docs).append("}");

			// 建模写入后台存储和计算引擎;构建json数据
			try {
				String rt = HttpRequest.sendPost(
						"http://10.32.28.119:8080/solr46/item/update/json", sb_cmd.toString());
				
//				//@test
//				System.out.println(sb_cmd);
				
				// test
				if (rt.indexOf("failed") > 0) {
					LOG.info("send post failed,rt=" + rt);
					commenFuncs.writeResult("failed_docs/",
							aitem.doc.itemid,  sb_cmd.toString(), "utf-8", false, null);
					return -1;
				} 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error("send post failed",e);
				return -1;
			}
		
			
			// @test
			LOG.info("success, write i2a:" + aitem.doc.itemid);
		}

		if (opTypeIn.equals("del")) {

			String sCmd = new String("{\"delete\":{\"id\":\"z0\"}}");
			sCmd = sCmd.replace("z0",r_item.getID());
			try {
				// 建模写入后台存储和计算引擎;构建json数据
				String rt = HttpRequest.sendPost(
						"http://10.32.28.119:8080/solr46/item/update/json",
						sCmd);
				// test
				if (rt.indexOf("failed") > 0) {
					LOG.info("send post failed,del:" + sCmd + " rt=" + rt);
					return -1;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error("send post failed", e);
				return -1;
			}
			// @test
			LOG.info("success,  del i2a:" + r_item.getID());
		}

		return 1;

	}
	
	
	public void closeIndexWriter() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("error:", e);
		}
	}

	// @test用
	public String testQueryItemIDSolr(String itemID) {
		if (itemID == null)
			return null;
		// 2.组合查询item库
		String s_items = "";
		try {
			s_items = HttpRequest.sendGet(
					"http://10.32.28.119:8080/solr46/item/select", "q=itemid:"+itemID+"&fl=itemid,score,item2app");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "items == null";
		}
		if (s_items == null || s_items.isEmpty())
			return "items == null";
		
		return s_items;
	}

	/*
	 * 执行内容的投放，并存入lucene;
	 */
	public void dis() {
		//while (true) {
			// 从前端权重模型库得到所有待推荐item及其权重
			RankList ranklist = appRankModel.getInstance().getUpdates();
			if(ranklist == null)
			{
				LOG.info("ranklist == null");
				return;
			}
			
			LOG.info("ranklist's backup items size:"
					+ ranklist.getBackupList().size());
			LOG.info("ranklist's auto items size:"
					+ ranklist.getAutoList().size());
			LOG.info("ranklist's games items size:"
					+ ranklist.getGamesList().size());
			LOG.info("ranklist's loc items size:"
					+ ranklist.getLocList().size());
			LOG.info("ranklist's weather items size:"
					+ ranklist.getWeatherList().size());
			LOG.info("ranklist's deleted items size:"
					+ ranklist.getDelList().size());
			LOG.info("ranklist's degr items size:"
					+ ranklist.getDegrList().size());
			LOG.info("ranklist's new items size:"
					+ ranklist.getNewList().size());
			LOG.info("ranklist's hot items size:"
					+ ranklist.getHotList().size());
			LOG.info("ranklist's season items size:"
					+ ranklist.getSeasonList().size());
			LOG.info("ranklist's featival items size:"
					+ ranklist.getFestivalList().size());

			//投放地域、天气、backup等数据到redis中
			//disSpecialItems(ranklist);
			//临时代码，投放时候backup和loc仍然拼接并且沿用旧的json格式，天气、游戏等其它数据先不投放
			disSpecialItems(ranklist);
			
			// 对已过期item进行计算和投放（目前是对所有匹配的进行delete，计算量太大；需要重构数据结构，实现对投放的delete）
			cmpAndDistributeSomeItems(ranklist.getDelList(), "del");

			/*
			 * 对有更新的item逐条进行重新计算和投放（目前数据结构比较麻烦，重新设计；由于delete操作比较麻烦，所以重新投放操作失效，需要重新投放
			 * ）
			 */
			cmpAndDistributeSomeItems(ranklist.getDegrList(), "degr");

			// 对最新item逐条进行计算和投放(如果条目太多，那么分批次投放)
			cmpAndDistributeSomeItems(ranklist.getNewList(), "add");

			try {
				writer.commit();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("",e);
			}
			
//			//solr commit too
//			this.commit2Solr();
			
	}
	
	/*
	 * @test
	 * 临时代码
	 * 分流backup、地域、天气等数据到redis库中;
	 * 注意：
	 * 	  每次都需要更新维护下各个业务的具体库，去除其中比较陈旧的数据；
	 */
	private void disSpecialItems(RankList ranklist) {
		// TODO Auto-generated method stub
		// backup
		ArrayList<RankItem> backup_rankItems = ranklist.getBackupList();
		if (backup_rankItems == null) {
			LOG.error("backup_rankItems == null");
		} else {
			specialItems backup_items = new specialItems();
			
			//实时热点数据
			ArrayList<RankItem> hotList = ranklist.getHotList();
			//sort
			sortHotList(hotList);
			
			HashSet<String> hs_IDs = new HashSet<String>();
			for (RankItem r_item : hotList) {
				item2app i2a = new item2app(r_item);
				i2a.why = "hot";
				backup_items.add(i2a);
				hs_IDs.add(i2a.docId);
			}
			
			//打底数据
			for (RankItem r_item : backup_rankItems) {
				if(hs_IDs.contains(r_item.getID()))
					continue;
				item2app i2a = new item2app(r_item);
				i2a.why = "additional";
				backup_items.add(i2a);
			}
	
			disToRedis("backup_items", 3600 * 8, backup_items, false);
		}

		// loc news
		ArrayList<RankItem> loc_rankItems = ranklist.getLocList();
		if (loc_rankItems == null) {
			LOG.error("loc_rankItems == null");
		} else {
			//走地域的筛选排序算法
			HashMap<String, ArrayList<RankItem>> hm_locItems = heatPredict.getRankLocNewsMap(loc_rankItems);
			
			if(!hm_locItems.isEmpty()){
				Iterator<Entry<String, ArrayList<RankItem>>> it = hm_locItems.entrySet().iterator();
				while(it.hasNext()){
					Entry<String, ArrayList<RankItem>> et = it.next();
					String loc = et.getKey();
					ArrayList<RankItem> al_items = et.getValue();
					specialItems loc_items = new specialItems();
					for (RankItem r_item : al_items) {
						item2app i2a = new item2app(r_item);
						i2a.why = loc;
						loc_items.add(i2a);
					}
					
					disLocNewsToRedis("h_loc_items",loc, loc_items);
				}
			}
	
		}

		// auto
		ArrayList<RankItem> auto_rankItems = ranklist.getAutoList();
		if (auto_rankItems == null) {
			LOG.error("auto_rankItems == null");
		} else {
			specialItems auto_items = new specialItems();
			for (RankItem r_item : auto_rankItems) {
				item2app i2a = new item2app(r_item);
				String others = r_item.getOthers();
				int b = others.indexOf("loc=");
				if (b > 0) {
					int e = others.indexOf("|!|", b + 4);
					if (e > b)
						i2a.why = others.substring(b + 4, e);
					else
						i2a.why = others.substring(b + 4);
					
				}
				auto_items.add(i2a);
			}
			disToRedis("auto_items", 3600 * 12, auto_items, false);
		}

		// games
		ArrayList<RankItem> games_rankItems = ranklist.getGamesList();
		if (auto_rankItems == null) {
			LOG.error("games_rankItems == null");
		} else {
			specialItems games_items = new specialItems();
			for (RankItem r_item : games_rankItems) {
				item2app i2a = new item2app(r_item);
				i2a.why = "games";
				games_items.add(i2a);
			}
			disToRedis("games_items", 3600 * 12, games_items, false);
		}

		// weather
		ArrayList<RankItem> weather_rankItems = ranklist.getWeatherList();
		if (weather_rankItems == null) {
			LOG.error("weather_rankItems == null");
		} else {
			specialItems weather_items = new specialItems();
			for (RankItem r_item : weather_rankItems) {
				item2app i2a = new item2app(r_item);
				String others = r_item.getOthers();
				int b = others.indexOf("loc=");
				if (b > 0) {
					int e = others.indexOf("|!|", b + 4);
					if (e > b)
						i2a.why = others.substring(b + 4, e);
					else
						i2a.why = others.substring(b + 4);
					weather_items.add(i2a);
				}
			}
			disToRedis("weather_items", 3600 * 8, weather_items, true);
		}
		
		
		// festival
		ArrayList<RankItem> festival_rankItems = ranklist.getFestivalList();
		if (festival_rankItems == null) {
			LOG.error("festival_rankItems == null");
		} else {
			
			sortListByPV(festival_rankItems);
			
			specialItems festival_Items = new specialItems();
			for (RankItem r_item : festival_rankItems) {
				item2app i2a = new item2app(r_item);
				
				i2a.why = "高考";

				festival_Items.add(i2a);
			}
			
			
			
			disToRedis("featival_items", 3600 * 12, festival_Items, false);
		}
		
		// senson
		ArrayList<RankItem> season_rankItems = ranklist.getSeasonList();
		if (season_rankItems == null) {
			LOG.error("season_rankItems == null");
		} else {
			
			sortListByPV(season_rankItems);
			
			specialItems season_Items = new specialItems();
			for (RankItem r_item : season_rankItems) {
				item2app i2a = new item2app(r_item);
				
				i2a.why = "夏天";

				season_Items.add(i2a);
			}
			
			
			
			disToRedis("season_items", 3600 * 12, season_Items, false);
		}

		

	}
	
	/**
	 * 将LOC具体数据分发到多个redis中； 注意：LOC数据和其它业务不走相同的逻辑；LOC在redis的模型是一个hash结构
	 * 
	 * @param loc_hkey
	 *            redis中的loc的hash key名称
	 * @param loc
	 *            redis中的loc的hash中key名称
	 * @param dis_items
	 *            待分发的内容
	 *  注意：
	 * 	  每次都需要查询得到所有的数据，然后更新维护下，去除其中比较陈旧的数据；
	 */
	private void disLocNewsToRedis(String loc_hkey, String loc, 
			specialItems loc_items) {
		// TODO Auto-generated method stub
		if(loc_items == null||loc_items.al_special_items.isEmpty())
			return;
		
//		try {
//			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
//			Jedis jedis = new Jedis("10.32.21.62", 6379, 10000);
//			jedis.select(7);
//			String s_dis_items = JsonUtils.toJson(loc_items);
//			jedis.hset(loc_hkey, loc, s_dis_items);
//		} catch (Exception e) {
//			LOG.error("ERROR", e);
//		}
		
		specialItems loc_items_all = null;
		try {
			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
			Jedis jedis = new Jedis("10.32.24.202", 6379, 10000);
			jedis.select(7);
			String res = jedis.hget(loc_hkey, loc);
			if(res != null && !res.isEmpty())
			{
				loc_items_all = JsonUtils.fromJson(res, specialItems.class);
				if(loc_items_all == null)
					return;
				//合并
				loc_items_all.combine(loc_items.al_special_items);
				
			}else
				loc_items_all = loc_items;
			
			//排序
			rankItem2appList(loc_items_all);
			
		} catch (Exception e) {
			LOG.error("ERROR", e);
			return;
		}
		
		try {
			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
			Jedis jedis = new Jedis("10.32.24.202", 6379, 10000);
			jedis.select(7);
			String s_dis_items = JsonUtils.toJson(loc_items_all);
			jedis.hset(loc_hkey, loc, s_dis_items);
		} catch (Exception e) {
			LOG.error("ERROR", e);
		}
		
		try {
			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
			Jedis jedis = new Jedis("10.32.24.203", 6379, 10000);
			jedis.select(7);
			String s_dis_items = JsonUtils.toJson(loc_items_all);
			jedis.hset(loc_hkey, loc, s_dis_items);
		} catch (Exception e) {
			LOG.error("ERROR", e);
		}
	
		try {
			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
			Jedis jedis = new Jedis("10.32.24.215", 6379, 10000);
			jedis.select(7);
			String s_dis_items = JsonUtils.toJson(loc_items_all);
			jedis.hset(loc_hkey, loc, s_dis_items);
		} catch (Exception e) {
			LOG.error("ERROR", e);
		}
	}

	
	
	/**
	 * 对list做排序；参考weight、imgnum等多个参数
	 * 
	 * 注意：
	 * 		此函数本来不需要，只是因为heatpredict的接口只能处理rankItem，没法处理item2app；而item2app需要做内部类封装；
	 * 
	 * @param si_items
	 *            
	 */
	private void rankItem2appList(specialItems si_items) {
		// TODO Auto-generated method stub
		if(si_items == null || si_items.al_special_items == null)
			return;
		
		//将al_special_items转成简易的rankitem
		//排序
		//参考排序结果还原新的al_special_items列表,并做总体个数限制
		//1)
		ArrayList<RankItem> al_r_items = new ArrayList<RankItem>();
		HashMap<String,item2app> hm_tmp = new HashMap<String,item2app>();
		for(item2app i2a:si_items.al_special_items){
			al_r_items.add(i2a.toRankItem());
			hm_tmp.put(i2a.docId, i2a);
		}
//		//test
//		System.out.println("rankItem2appList before:");
//		for(RankItem r_item:al_r_items){
//			System.out.println(JsonUtils.toJson(r_item));
//		}
		
		//2)
		try{
			heatPredict.rankLocNewsList(al_r_items);
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		
//		//test
//		System.out.println("rankItem2appList rank:");
//		for(RankItem r_item:al_r_items){
//			System.out.println(JsonUtils.toJson(r_item));
//		}
		
		//3)
		si_items.al_special_items.clear();
		int num = 0;
		for(RankItem r_item:al_r_items){
			num++;
			si_items.al_special_items.add(hm_tmp.get(r_item.getID()));
			if(num > 300)
				break;
		}
	}

	/*
	 * 对热点数据按评论的hackernews score进行排序
	 */
	private void sortHotList(ArrayList<RankItem> hotList) {
		// TODO Auto-generated method stub
		if(hotList == null
				|| hotList.isEmpty())
			return;
		Collections.sort(hotList, new Comparator<RankItem>() {
			@Override
			public int compare(RankItem o1, RankItem o2) {
				// TODO Auto-generated method stub
				double score1,score2;
				score1 = score2 = 0;
				String others = o1.getOthers();
				int b = others.indexOf("commentScore=");
				if (b > 0) {
					String sScore = "";
					int e = others.indexOf("|!|", b + 13);
					if (e > b)
						sScore = others.substring(b + 13, e);
					else
						sScore = others.substring(b + 13);
					try{
						score1 = Double.valueOf(sScore);
					} catch (Exception e1) {
						score1 = 0;
					}
				}
				//score2
				others = o2.getOthers();
				b = others.indexOf("commentScore=");
				if (b > 0) {
					String sScore = "";
					int e = others.indexOf("|!|", b + 13);
					if (e > b)
						sScore = others.substring(b + 13, e);
					else
						sScore = others.substring(b + 13);
					try {
						score2 = Double.valueOf(sScore);
					} catch (Exception e1) {
						score2 = 0;
					}
				}
				
				double sc = score1 - score2;
				if (sc >= 0) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		
	}

	
	/*
	 * 对list中数据按pv排序
	 */
	private void sortListByPV(ArrayList<RankItem> al_dataList) {
		// TODO Auto-generated method stub
		if(al_dataList == null
				|| al_dataList.isEmpty())
			return;
		Collections.sort(al_dataList, new Comparator<RankItem>() {
			@Override
			public int compare(RankItem o1, RankItem o2) {
				// TODO Auto-generated method stub		
				double sc = o1.getPv() - o2.getPv();
				if (sc >= 0) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		
	}
	
//	/*
//	 * 分流backup、地域、天气等数据到redis库中;
//	 * 注意：
//	 * 	  每次都需要更新维护下各个业务的具体库，去除其中比较陈旧的数据；
//	 */
//	private void disSpecialItems(RankList ranklist) {
//		// TODO Auto-generated method stub
//		//backup
//		ArrayList<RankItem> backup_rankItems = ranklist.getBackupList();
//		if (backup_rankItems == null) {
//			LOG.error("backup_rankItems == null");
//		} else {
//			specialItems backup_items = new specialItems();
//			for (RankItem r_item : backup_rankItems) {
//				item2app i2a = new item2app(r_item);
//				i2a.why = "additional";
//				backup_items.add(i2a);
//			}
//			disToRedis("backup_items1",3600,backup_items,false);
//		}
//		
//		//loc
//		ArrayList<RankItem> loc_rankItems = ranklist.getLocList();
//		if (loc_rankItems == null) {
//			LOG.error("loc_rankItems == null");
//		} else {
//			specialItems loc_items = new specialItems();
//			for (RankItem r_item : loc_rankItems) {
//				item2app i2a = new item2app(r_item);
//				String others = r_item.getOthers();
//				int b = others.indexOf("loc=");
//				if (b > 0) {
//					int e = others.indexOf("|!|", b + 4);
//					if (e > b)
//						i2a.why = others.substring(b + 4, e);
//					else
//						i2a.why = others.substring(b + 4);
//					loc_items.add(i2a);
//				}
//			}
//			disToRedis("loc_items1",3600*12,loc_items,false);
//		}
//		
//		//auto
//		ArrayList<RankItem> auto_rankItems = ranklist.getAutoList();
//		if (auto_rankItems == null) {
//			LOG.error("auto_rankItems == null");
//		} else {
//			specialItems auto_items = new specialItems();
//			for (RankItem r_item : auto_rankItems) {
//				item2app i2a = new item2app(r_item);
//				String others = r_item.getOthers();
//				int b = others.indexOf("loc=");
//				if (b > 0) {
//					int e = others.indexOf("|!|", b + 4);
//					if (e > b)
//						i2a.why = others.substring(b + 4, e);
//					else
//						i2a.why = others.substring(b + 4);
//					auto_items.add(i2a);
//				}
//			}
//			disToRedis("auto_items1",3600*12,auto_items,false);
//		}
//		
//		//games
//		ArrayList<RankItem> games_rankItems = ranklist.getGamesList();
//		if (auto_rankItems == null) {
//			LOG.error("games_rankItems == null");
//		} else {
//			specialItems games_items = new specialItems();
//			for (RankItem r_item : games_rankItems) {
//				item2app i2a = new item2app(r_item);
//				i2a.why = "games";
//				games_items.add(i2a);
//			}
//			disToRedis("games_items1",3600*12,games_items,false);
//		}
//
//		//weather
//		ArrayList<RankItem> weather_rankItems = ranklist.getWeatherList();
//		if (weather_rankItems == null) {
//			LOG.error("weather_rankItems == null");
//		} else {
//			specialItems weather_items = new specialItems();
//			for (RankItem r_item : weather_rankItems) {
//				item2app i2a = new item2app(r_item);
//				String others = r_item.getOthers();
//				int b = others.indexOf("loc=");
//				if (b > 0) {
//					int e = others.indexOf("|!|", b + 4);
//					if (e > b)
//						i2a.why = others.substring(b + 4, e);
//					else
//						i2a.why = others.substring(b + 4);
//					weather_items.add(i2a);
//				}
//			}
//			disToRedis("weather_rankItems1",3600*8,weather_items,true);
//		}
//	}
	
	/**
	 * 将具体数据分发到多个redis中； 注意：
	 * 
	 * @param s_tableName
	 *            redis中的具体业务key
	 * @param validTime
	 *            value的有效期
	 * @param dis_items
	 *            待分发的内容
	 * @param isAppend
	 *            是否追加模式；如果是追加模式，那么需要读取旧内容，并删除过期数据，然后合并新数据写入；
	 *  注意：
	 * 	  每次都需要查询得到所有的数据，然后更新维护下，去除其中比较陈旧的数据；
	 */
	private void disToRedis(String s_tableName, long validTime,
			specialItems dis_items,boolean isAppend) {
		// TODO Auto-generated method stub
		if(dis_items == null)
			return;
		try {
			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
			Jedis jedis = new Jedis("10.32.21.62", 6379, 10000);
			jedis.select(7);
			if(isAppend){
				String s_allItems = jedis.get(s_tableName);
				if(s_allItems == null){
					LOG.error("s_allItems == null,s_tableName = " + s_tableName);
				}else if(s_allItems.equals("null")||s_allItems.isEmpty()){
					String s_dis_items = JsonUtils.toJson(dis_items);
					jedis.setex(s_tableName, (int) validTime, s_dis_items);
				}else{
					specialItems si = JsonUtils.fromJson(s_allItems, specialItems.class);
					//遍历删除非当天数据
					if(si == null)
					{
						LOG.error("query failed:"+s_tableName);
					}else{
						specialItems si_new = new specialItems();
						//遍历item，并删除非当天item
						if(si.al_special_items != null)
						{
							for(item2app i2a:si.al_special_items){
								String s_date = i2a.date;//format:2014-12-19 09:41:41
								try{
									SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
									Date dt_now = new Date(System.currentTimeMillis());
									String s_dtn = sdf.format(dt_now);
									if(!(s_date.startsWith(s_dtn)))
									{
										continue;
									}
									si_new.al_special_items.add(i2a);
								}catch(Exception e){
									LOG.error("time parse error:"+s_date);
								}
								
							}
						}
						
						for(item2app i2a:dis_items.al_special_items)
							si_new.al_special_items.add(i2a);
						
						String s_dis_items = JsonUtils.toJson(si_new);
						
//						//临时变换处理下，替换成al_backup_items
//						if(s_tableName.equals("backup_items"))
//							s_dis_items = s_dis_items.replaceAll("al_special_items", "al_backup_items");
						
						String status = jedis.setex(s_tableName, (int) validTime, s_dis_items);
						if(!status.equals("OK")){
							LOG.error("set status code:"+status);
						}
					}
				}
			}else{
				String s_dis_items = JsonUtils.toJson(dis_items);
//				//临时变换处理下，替换成al_backup_items
//				if(s_tableName.equals("backup_items"))
//					s_dis_items = s_dis_items.replaceAll("al_special_items", "al_backup_items");
				jedis.setex(s_tableName, (int) validTime, s_dis_items);
			}
			
			
		} catch (Exception e) {
			LOG.error("ERROR", e);
		}

		try {
			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
			Jedis jedis = new Jedis("10.32.24.202", 6379, 10000);
			jedis.select(7);
			if(isAppend){
				String s_allItems = jedis.get(s_tableName);
				if(s_allItems == null){
					LOG.error("s_allItems == null,s_tableName = " + s_tableName);
				}else if(s_allItems.equals("null")||s_allItems.isEmpty()){
					String s_dis_items = JsonUtils.toJson(dis_items);
					jedis.setex(s_tableName, (int) validTime, s_dis_items);
				}else{
					specialItems si = JsonUtils.fromJson(s_allItems, specialItems.class);
					//遍历删除非当天数据
					if(si == null)
					{
						LOG.error("query failed:"+s_tableName);
					}else{
						specialItems si_new = new specialItems();
						//遍历item，并删除非当天item
						if(si.al_special_items != null)
						{
							for(item2app i2a:si.al_special_items){
								String s_date = i2a.date;//format:2014-12-19 09:41:41
								try{
									SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
									Date dt_now = new Date(System.currentTimeMillis());
									String s_dtn = sdf.format(dt_now);
									if(!(s_date.startsWith(s_dtn)))
									{
										continue;
									}
									si_new.al_special_items.add(i2a);
								}catch(Exception e){
									LOG.error("time parse error:"+s_date);
								}
								
							}
						}
						
						for(item2app i2a:dis_items.al_special_items)
							si_new.al_special_items.add(i2a);
						
						String s_dis_items = JsonUtils.toJson(si_new);
//						//临时变换处理下，替换成al_backup_items
//						if(s_tableName.equals("backup_items"))
//							s_dis_items = s_dis_items.replaceAll("al_special_items", "al_backup_items");
						String status = jedis.setex(s_tableName, (int) validTime, s_dis_items);
						if(!status.equals("OK")){
							LOG.error("set status code:"+status);
						}
					}
				}
			}else{
				String s_dis_items = JsonUtils.toJson(dis_items);
//				//临时变换处理下，替换成al_backup_items
//				if(s_tableName.equals("backup_items"))
//					s_dis_items = s_dis_items.replaceAll("al_special_items", "al_backup_items");
				jedis.setex(s_tableName, (int) validTime, s_dis_items);
			}
			
			
		} catch (Exception e) {
			LOG.error("ERROR", e);
		}
		
		try {
			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
			Jedis jedis = new Jedis("10.32.24.203", 6379, 10000);
			jedis.select(7);
			if(isAppend){
				String s_allItems = jedis.get(s_tableName);
				if(s_allItems == null){
					LOG.error("s_allItems == null,s_tableName = " + s_tableName);
				}else if(s_allItems.equals("null")||s_allItems.isEmpty()){
					String s_dis_items = JsonUtils.toJson(dis_items);
					jedis.setex(s_tableName, (int) validTime, s_dis_items);
				}else{
					specialItems si = JsonUtils.fromJson(s_allItems, specialItems.class);
					//遍历删除非当天数据
					if(si == null)
					{
						LOG.error("query failed:"+s_tableName);
					}else{
						specialItems si_new = new specialItems();
						//遍历item，并删除非当天item
						if(si.al_special_items != null)
						{
							for(item2app i2a:si.al_special_items){
								String s_date = i2a.date;//format:2014-12-19 09:41:41
								try{
									SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
									Date dt_now = new Date(System.currentTimeMillis());
									String s_dtn = sdf.format(dt_now);
									if(!(s_date.startsWith(s_dtn)))
									{
										continue;
									}
									si_new.al_special_items.add(i2a);
								}catch(Exception e){
									LOG.error("time parse error:"+s_date);
								}
								
							}
						}
						
						for(item2app i2a:dis_items.al_special_items)
							si_new.al_special_items.add(i2a);
						
						String s_dis_items = JsonUtils.toJson(si_new);
//						//临时变换处理下，替换成al_backup_items
//						if(s_tableName.equals("backup_items"))
//							s_dis_items = s_dis_items.replaceAll("al_special_items", "al_backup_items");
						String status = jedis.setex(s_tableName, (int) validTime, s_dis_items);
						if(!status.equals("OK")){
							LOG.error("set status code:"+status);
						}
					}
				}
			}else{
				String s_dis_items = JsonUtils.toJson(dis_items);
//				//临时变换处理下，替换成al_backup_items
//				if(s_tableName.equals("backup_items"))
//					s_dis_items = s_dis_items.replaceAll("al_special_items", "al_backup_items");
				jedis.setex(s_tableName, (int) validTime, s_dis_items);
			}
			
			
		} catch (Exception e) {
			LOG.error("ERROR", e);
		}
		
		try {
			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
			Jedis jedis = new Jedis("10.32.24.215", 6379, 10000);
			jedis.select(7);
			if(isAppend){
				String s_allItems = jedis.get(s_tableName);
				if(s_allItems == null){
					LOG.error("s_allItems == null,s_tableName = " + s_tableName);
				}else if(s_allItems.equals("null")||s_allItems.isEmpty()){
					String s_dis_items = JsonUtils.toJson(dis_items);
					jedis.setex(s_tableName, (int) validTime, s_dis_items);
				}else{
					specialItems si = JsonUtils.fromJson(s_allItems, specialItems.class);
					//遍历删除非当天数据
					if(si == null)
					{
						LOG.error("query failed:"+s_tableName);
					}else{
						specialItems si_new = new specialItems();
						//遍历item，并删除非当天item
						if(si.al_special_items != null)
						{
							for(item2app i2a:si.al_special_items){
								String s_date = i2a.date;//format:2014-12-19 09:41:41
								try{
									SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
									Date dt_now = new Date(System.currentTimeMillis());
									String s_dtn = sdf.format(dt_now);
									if(!(s_date.startsWith(s_dtn)))
									{
										continue;
									}
									si_new.al_special_items.add(i2a);
								}catch(Exception e){
									LOG.error("time parse error:"+s_date);
								}
								
							}
						}
						
						for(item2app i2a:dis_items.al_special_items)
							si_new.al_special_items.add(i2a);
						
						String s_dis_items = JsonUtils.toJson(si_new);
//						//临时变换处理下，替换成al_backup_items
//						if(s_tableName.equals("backup_items"))
//							s_dis_items = s_dis_items.replaceAll("al_special_items", "al_backup_items");
						String status = jedis.setex(s_tableName, (int) validTime, s_dis_items);
						if(!status.equals("OK")){
							LOG.error("set status code:"+status);
						}
					}
				}
			}else{
				String s_dis_items = JsonUtils.toJson(dis_items);
//				//临时变换处理下，替换成al_backup_items
//				if(s_tableName.equals("backup_items"))
//					s_dis_items = s_dis_items.replaceAll("al_special_items", "al_backup_items");
				jedis.setex(s_tableName, (int) validTime, s_dis_items);
			}
			
			
		} catch (Exception e) {
			LOG.error("ERROR", e);
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//	// test pc:先集中修改下一些字段，以方便临时搭建环境
//		fieldDicts.itemModelPath = "D:/workspace/iRecommend4App/testenv/itemmodel/";
//		fieldDicts.modelPath = "D:/workspace/iRecommend4App/testenv/usermodel/";
//		fieldDicts.appTreeMappingFile = "D:/workspace/iRecommend4App/testenv/AppTreeMapping.txt";
//		fieldDicts.frontAppTreeMappingFile = "D:/workspace/iRecommend4App/testenv/APPFront_TreeMapping.txt";
//		fieldDicts.stopwordsFile = "D:/workspace/iRecommend4App/testenv/stopwords.txt";
//		fieldDicts.tm_doc_dir = "D:\\workspace\\iRecommend4App\\testenv\\tm\\doc\\";
//		fieldDicts.tm_word_dir = "D:\\workspace\\iRecommend4App\\testenv\\tm\\word\\";
//		fieldDicts.tm_words_file = "D:\\workspace\\iRecommend4App\\testenv\\tm\\dict_topicmodel";
//		fieldDicts.testUsersFile = "D:\\workspace\\iRecommend4App\\testenv\\appActiveUsers_5w.txt";
//		fieldDicts.pcVisualHotFile = "/projects/zhineng/pcHotPredict/pcHotLevel";
//		fieldDicts.locationMapFilePath = "/data/irecommend4app/locationMap.txt";
	
////test server:
//		fieldDicts.itemModelPath = "/data/irecommend4app_test/itemmodel/";
//		fieldDicts.modelPath = "/data/irecommend4app_test/usermodel/";
//		fieldDicts.appTreeMappingFile = "/data/irecommend4app_test/AppTreeMapping.txt";
//		fieldDicts.frontAppTreeMappingFile = "/data/irecommend4app_test/APPFront_TreeMapping.txt";
//		fieldDicts.stopwordsFile = "/data/irecommend4app_test/stopwords.txt";
//		fieldDicts.tm_doc_dir = "/data/irecommend4app_test/tm/doc/";
//		fieldDicts.tm_word_dir = "/data/irecommend4app_test/tm/word/";
//		fieldDicts.tm_words_file = "/data/irecommend4app_test/tm/dict_topicmodel";
//		fieldDicts.testUsersFile = "/data/irecommend4app_test/appActiveUsers_5w.txt";
//		fieldDicts.pcVisualHotFile = "/projects/zhineng/pcHotPredict/pcHotLevel";
//		fieldDicts.locationMapFilePath = "/data/irecommend4app_test/locationMap.txt";	
		
		
//server	
		fieldDicts.itemModelPath = "/data/irecommend4app/itemmodel/";
		fieldDicts.modelPath = "/data/irecommend4app/usermodel/";
		fieldDicts.appTreeMappingFile = "/data/irecommend4app/AppTreeMapping.txt";
		fieldDicts.frontAppTreeMappingFile = "/data/irecommend4app/APPFront_TreeMapping.txt";
		fieldDicts.stopwordsFile = "/data/irecommend4app/stopwords.txt";
		fieldDicts.tm_doc_dir = "/data/irecommend4app/tm/doc/";
		fieldDicts.tm_word_dir = "/data/irecommend4app/tm/word/";
		fieldDicts.tm_words_file = "/data/irecommend4app/tm/dict_topicmodel";
		fieldDicts.testUsersFile = "/data/irecommend4app/appActiveUsers_5w.txt";
		fieldDicts.pcVisualHotFile = "/projects/zhineng/pcHotPredict/pcHotLevel";
		fieldDicts.locationMapFilePath = "/data/irecommend4app/locationMap.txt";

		itemDis id = new itemDis();
		
		if(args[0].equals("debug"))
			id.DEBUG = true;
			
		
		while(true)
		{
			LOG.info("start dis:");
			id.dis();
			LOG.info("sleep...");
			try {
				Thread.sleep(2*60*1000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
