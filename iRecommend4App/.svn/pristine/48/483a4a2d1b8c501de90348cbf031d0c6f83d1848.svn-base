/**
 * 
 */
package com.ifeng.iRecommend.likun.rankModel;

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
import com.ifeng.commen.blackList.subscriber.CommonDataSub;
import com.ifeng.commen.redis.JedisInterface;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
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
public class itemDisNew {
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
		public String readableFeatures;//可读化特征集；分类分级表达（结合特征分类分级表）;以"|!|"分开
		
		@Expose
		public String others;//备注字段，放入其它信息，比如替换掉的ID、showStyle等；
		
		
		
		public item2app(RankItemNew rankItem) {
			docId = rankItem.getItem().getID();
			title = rankItem.getItem().getTitle();
			docChannel = rankItem.getCategory();
			why = "";
			others = "";
			score = 0;
			hotBoost = 1;
			docType = rankItem.getItem().getDocType();
			itemf item = rankItem.getItem();
			if(item!=null)
				date = item.getPublishedTime();
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
			
			//提取rankitem中的ID替换信息，并放入others字段中
			{
//				String others = rankItem.getOthers();
//				int b = others.indexOf("simID=");
//				if (b > 0) {
//					int e = others.indexOf("|!|", b + 6);
//					if (e > b)
//						this.others = others.substring(b + 6, e);
//					else
//						this.others = others.substring(b + 6);
//				}
				
				this.others = "simID="+rankItem.getSpecialWords();
				
//				//稿源合法不合法
//				b = others.indexOf("|!|illegal=true");
//				if (b >= 0) {
//					this.others = this.others + "|!|illegal";
//				}
				
				//加入url信息
				if(rankItem.getItem().getUrl() != null)
					this.others = this.others + "|!|url=" + rankItem.getItem().getUrl();
			}
//			//new， 会放入所有others字段信息
//			this.others = rankItem.getOthers();
			
			/*
			 * 放入数据授权信息，用于前端不同应用中数据可用控制；
			 * add likun,20150914
			 */
			String cla_tags = RankItemNew.genDataAuthLabel(rankItem);
			this.others = this.others + "|!|" + cla_tags;
			if(rankItem.getItem().getShowStyle() != null)
				this.others = this.others + "|!|showStyle=" + rankItem.getItem().getShowStyle();

			
			
			//计算可读化标签并放入others字段中；遍历其features字段，将c按权重放入topic1 sc放入topic2 cn和t放入topic3
			StringBuffer sbTmp = new StringBuffer();
			ArrayList<String> al_features = rankItem.getItem().getFeatures();
			
			if(al_features.size()%3 == 0){
				for(int i=0;i<al_features.size();i+=3){
					String feature = al_features.get(i);
					String type = al_features.get(i+1);
					float weight = 0f;
					try{
						weight = Float.valueOf(al_features.get(i+2));
					}catch(Exception e){
						weight = 0f;
						e.printStackTrace();
					}
					//不可读，暂时不加入可读化表达
					if(Math.abs(weight) < 0.5f)
						continue;
					
					if(type.equals("c")
							||type.equals("sc")
							||type.equals("cn")
							||type.equals("t")
							||type.equals("e")
							||type.equals("s1")){
						sbTmp.append(type).append("=").append(feature).append("|!|");
					}
					if(type.equals("kb")){
						sbTmp.append(type).append("=《").append(feature).append("》|!|");
					}
					if((type.equals("loc") || type.equals("et") || type.equals("k") || type.equals("x"))
							&& weight >=0.5){
						sbTmp.append(type).append("=").append(feature).append("|!|");
					}
				}
			}
			readableFeatures = sbTmp.toString();
			
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
	}
	
	private static final Log LOG = LogFactory.getLog("itemDis");
	
	//debug模式
	public boolean DEBUG;
	
	//业务类型区分
	private String appid;
	
	public itemDisNew() {
		
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
	private void cmpAndDistributeSomeItems(ArrayList<RankItemNew> itemList,
			final String opTypeIn) {
		// TODO Auto-generated method stub
		if (itemList.size() <= 0)
			return;

		LOG.info("contentMatch:"+opTypeIn + " item list size:" + itemList.size());
		Iterator<RankItemNew> it = itemList.iterator();
		StringBuffer sbTmp = new StringBuffer();
		while (it.hasNext()) {
			RankItemNew r_item = it.next();
			itemf item = r_item.getItem();
			if (item == null) {
				LOG.error("itemf == null,specialWords="+r_item.getSpecialWords());
				continue;
			}

			sbTmp.delete(0, sbTmp.length());
			sbTmp.append("distribute one item:").append(opTypeIn)
					.append("\r\n");
			sbTmp.append(r_item.getItem().getID()).append("\r\n");
			sbTmp.append(r_item.getItem().getUrl()).append("\r\n");
			if (item.getSplitTitle() != null)
				sbTmp.append(
						item.getSplitTitle().replaceAll("[\r\n]", ""))
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
			}

//			// 存储并入索引中
//			save2Index(r_item, opTypeIn);
//
			//同步写入solr库中
			save2IndexSolr(r_item, opTypeIn);

//			//转发指令给track算法
//			distribute2Track(r_item,opTypeIn);
			
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

	
	
	
	

//	/**
//	 * 把内容commit入index；solr版本
//	 */
//	public int commit2Solr() {
//		//commit
//		String rt = HttpRequest.sendGet("http://10.32.28.119:8080/solr46/item/update", "commit=true");
//		//test
//		if(rt.indexOf("failed") > 0)
//		{
//			LOG.info("commit failed");
//			return -1;
//		}
//		// @test
//		LOG.info("success, commit");
//		
//		
////		String sUrl = appendParam(solrUrl.toString(), "commit=true");
////
////		try {
////			URL url = new URL(sUrl);
////
////			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
////			int iTryNO = 0;
////			while (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
////				try {
////					if (iTryNO > 20) {
////						MessageUtil.fatal("Solr returned an error #ErrorCode="
////								+ urlc.getResponseCode() + " ErrorMessage="
////								+ urlc.getResponseMessage());
////						MessageUtil.fatal("posting data to " + sUrl
////								+ " failed");
////						break;
////					}
////					iTryNO++;
////					MessageUtil.info("try posting data to " + sUrl
////							+ ":" + iTryNO );
////					Thread.sleep(15000);// 歇会儿
////					urlc = (HttpURLConnection) url.openConnection();
////				} catch (Exception ex) {
////					log.error("An error occured posting data to " + sUrl,ex);
////				}
////			}
////		} catch (MalformedURLException e) {
////			MessageUtil.fatal("The specified URL " + sUrl
////					+ " is not a valid URL. Please check");
////		} catch (IOException e2) {
////			MessageUtil.fatal("An error occured posting data to " + sUrl
////					+ ". Please check that Solr is running.");
////		}
//		
//		return 1;
//	}
	
//	/**
//	 * 把内容指令转发给track算法，走统一内容控制；
//	 * 
//	 * @param i2d
//	 *            具体内容
//	 * @param opTypeIn
//	 *            投放类型：add\del
//	 * */
//	public int distribute2Track(RankItem r_item, String opTypeIn) {
//		// TODO Auto-generated method stub
//		if (r_item == null || opTypeIn == null)
//			return -1;
//
//		if (opTypeIn.equals("add")) {
//			//投放数据,addItemForTrack,新闻追踪用
//			try {
//				Jedis jedis = new Jedis("10.32.24.222",6379,10000);
//				jedis.select(7);
//				LOG.info("distribute2Track,add:" + r_item.getID());
//				itemForTrack dItem = new itemForTrack(r_item);
//				jedis.rpush("items_from_rankmodel", JsonUtils.toJson(dItem));
//				// @test
//				LOG.info("success,add:" + r_item.getID());
//			}catch (Exception e) {
//				LOG.error("ERROR", e);
//			}
//		
//			
//		}
//
//		if (opTypeIn.equals("del")) {
//			//投放数据,addItemForTrack,新闻追踪用
//			try {
//				Jedis jedis = new Jedis("10.32.24.222",6379,10000);
//				jedis.select(7);
//				LOG.info("distribute2Track,del:" + r_item.getID());
//				jedis.rpush("items_from_rankmodel",r_item.getID());
//				// @test
//				LOG.info("success,del:" + r_item.getID());
//			}catch (Exception e) {
//				LOG.error("ERROR", e);
//			}
//		}
//
//		return 1;
//
//	}
//	
	
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
	public int save2IndexSolr(RankItemNew r_item, String opTypeIn) {
		// TODO Auto-generated method stub
		if (r_item == null || opTypeIn == null)
			return -1;

		//change:del操作其实只是修改available字段为false，所有也是add操作
		if (opTypeIn.equals("add") || opTypeIn.equals("degr")
				|| opTypeIn.equals("del")) {
			// @test
			LOG.info("begin write i2a:" + r_item.getItem().getID());
			try {
				// 组装additem
				addItemNew aitem = new addItemNew(r_item);
				//生成item2app
				item2app i2a = new item2app(r_item);
				aitem.doc.item2app = JsonUtils.toJson(i2a);
				aitem.doc.hotboost = String.valueOf(i2a.hotBoost);
				
				//del操作其实只是修改available字段为false，所有也是add操作
				if(opTypeIn.equals("del")){
					aitem.doc.available = "F";
				}
	
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
				
				String rt = HttpRequest.sendPost(
						rankModel.solrItemDBPath+"update/json"/*"http://10.32.28.119:8081/solr46/item/update/json"*/, sb_cmd.toString());
				
//				//@test
//				commenFuncs.writeResult("failed_docs/",
//						aitem.doc.itemid,  sb_cmd.toString(), "utf-8", false, null);
	
				
				if (rt.indexOf("failed") > 0) {
					LOG.info("send post failed,rt=" + rt);
					commenFuncs.writeResult("failed_docs/",
							aitem.doc.itemid,  sb_cmd.toString(), "utf-8", false, null);
					return -1;
				} 
				
				// @test
				LOG.info("success, write i2a:" + aitem.doc.itemid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error("send post failed",e);
				return -1;
			}
	
			
			
		}

		
//		if (opTypeIn.equals("del")) {	
//			
//			String sCmd = new String("{\"delete\":{\"id\":\"z0\"}}");
//			sCmd = sCmd.replace("z0",r_item.getID());
//			try {
//				// 建模写入后台存储和计算引擎;构建json数据
//				String rt = HttpRequest.sendPost(
//						"http://10.32.28.119:8080/solr46/item/update/json",
//						sCmd);
//				// test
//				if (rt.indexOf("failed") > 0) {
//					LOG.info("send post failed,del:" + sCmd + " rt=" + rt);
//					return -1;
//				}
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				LOG.error("send post failed", e);
//				return -1;
//			}
//			// @test
//			LOG.info("success,  del i2a:" + r_item.getID());
//		}

		return 1;

	}
	
	

//	// @test用
//	public String testQueryItemIDSolr(String itemID) {
//		if (itemID == null)
//			return null;
//		// 2.组合查询item库
//		String s_items = "";
//		try {
//			s_items = HttpRequest.sendGet(
//					"http://10.32.28.119:8080/solr46/item/select", "q=itemid:"+itemID+"&fl=itemid,score,item2app");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return "items == null";
//		}
//		if (s_items == null || s_items.isEmpty())
//			return "items == null";
//		
//		return s_items;
//	}

	/*
	 * 执行内容的投放，并存入lucene;
	 */
	public void dis(String _appid) {
			if(_appid == null)
				return;
			
		    appid = _appid;
		    
		//while (true) {
			//从前端权重模型库得到所有待推荐item及其权重
			RankListNew ranklist = rankModel.getInstance().getUpdates(appid);
			if(ranklist == null)
			{
				LOG.info("ranklist == null");
				return;
			}
			
			LOG.info("ranklist's backup items size:"
					+ ranklist.getBackupList().size());
//			LOG.info("ranklist's auto items size:"
//					+ ranklist.getAutoList().size());
//			LOG.info("ranklist's games items size:"
//					+ ranklist.getGamesList().size());
			LOG.info("ranklist's loc items size:"
					+ ranklist.getLocList().size());
//			LOG.info("ranklist's weather items size:"
//					+ ranklist.getWeatherList().size());
			LOG.info("ranklist's deleted items size:"
					+ ranklist.getDelList().size());
			LOG.info("ranklist's degr items size:"
					+ ranklist.getDegrList().size());
			LOG.info("ranklist's new items size:"
					+ ranklist.getNewList().size());
			LOG.info("ranklist's quality items size:"
					+ ranklist.getQualityList().size());


			//投放时候backup仍然拼接并且沿用旧的json格式
			LOG.info("isRealRankModel = " + rankModel.isRealRankModel);
			if("true".equals(rankModel.isRealRankModel))
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
			
			
			//对最新item逐条进行计算和投放(如果条目太多，那么分批次投放)
			cmpAndDistributeSomeItems(ranklist.getLocList(), "del");
			
			
			

//			//solr commit too
//			this.commit2Solr();
			
	}
	
	/*
	 * 
	 * 分流backup、地域、天气等数据到redis库中;
	 * 注意：
	 * 	  每次都需要更新维护下各个业务的具体库，去除其中比较陈旧的数据；
	 */
	private void disSpecialItems(RankListNew ranklist) {
		// TODO Auto-generated method stub
		// backup
		ArrayList<RankItemNew> backup_rankItems = ranklist.getBackupList();
		if (backup_rankItems == null) {
			LOG.error("backup_rankItems == null");
		} else {
			specialItems backup_items = new specialItems();
			//打底数据
			for (RankItemNew r_item : backup_rankItems) {
				item2app i2a = new item2app(r_item);
				i2a.why = "additional";
				backup_items.add(i2a);
			}
	
			disToRedis("backup_items", 3600 * 8, backup_items, false);
		}

		
//		//优质数据
//		ArrayList<RankItemNew> quality_rankItems = ranklist.getQualityList();
//		if (quality_rankItems == null) {
//			LOG.error("quality_rankItems == null");
//		} else {
//			//sort
//			sortListByHot(quality_rankItems);
//			
//			specialItems quality_items = new specialItems();
//			//打底数据
//			for (RankItemNew r_item : quality_rankItems) {
//				item2app i2a = new item2app(r_item);
//				i2a.why = "优质";
//				quality_items.add(i2a);
//			}
//	
//			disToRedis("quality_items", 3600 * 48, quality_items, false);
//		}
		


	}
	
//	/**
//	 * 将LOC具体数据分发到多个redis中； 注意：LOC数据和其它业务不走相同的逻辑；LOC在redis的模型是一个hash结构
//	 * 
//	 * @param loc_hkey
//	 *            redis中的loc的hash key名称
//	 * @param loc
//	 *            redis中的loc的hash中key名称
//	 * @param dis_items
//	 *            待分发的内容
//	 *  注意：
//	 * 	  每次都需要查询得到所有的数据，然后更新维护下，去除其中比较陈旧的数据；
//	 */
//	private void disLocNewsToRedis(String loc_hkey, String loc, 
//			specialItems loc_items) {
//		// TODO Auto-generated method stub
//		if(loc_items == null||loc_items.al_special_items.isEmpty())
//			return;
//		
//		try {
//			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
//			Jedis jedis = new Jedis("10.32.21.62", 6379, 10000);
//			jedis.select(7);
//			String s_dis_items = JsonUtils.toJson(loc_items);
//			jedis.hset(loc_hkey, loc, s_dis_items);
//		} catch (Exception e) {
//			LOG.error("ERROR", e);
//		}
//
//		try {
//			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
//			Jedis jedis = new Jedis("10.32.24.202", 6379, 10000);
//			jedis.select(7);
//			String s_dis_items = JsonUtils.toJson(loc_items);
//			jedis.hset(loc_hkey, loc, s_dis_items);
//		} catch (Exception e) {
//			LOG.error("ERROR", e);
//		}
//		
//		try {
//			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
//			Jedis jedis = new Jedis("10.32.24.203", 6379, 10000);
//			jedis.select(7);
//			String s_dis_items = JsonUtils.toJson(loc_items);
//			jedis.hset(loc_hkey, loc, s_dis_items);
//		} catch (Exception e) {
//			LOG.error("ERROR", e);
//		}
//	
//		try {
//			// 将放入redis的db 7中，方便前端业务逻辑层接口调用
//			Jedis jedis = new Jedis("10.32.24.215", 6379, 10000);
//			jedis.select(7);
//			String s_dis_items = JsonUtils.toJson(loc_items);
//			jedis.hset(loc_hkey, loc, s_dis_items);
//		} catch (Exception e) {
//			LOG.error("ERROR", e);
//		}
//	}

	/*
	 * 对各种数据按hot ABCD进行排序
	 */
	private void sortListByHot(ArrayList<RankItemNew> itemList) {
		// TODO Auto-generated method stub
		if(itemList == null
				|| itemList.isEmpty())
			return;
		Collections.sort(itemList, new Comparator<RankItemNew>() {
			@Override
			public int compare(RankItemNew o1, RankItemNew o2) {
				// TODO Auto-generated method stub
				double score1,score2;
				score1 = score2 = 0;
				score1 = (double)((int)o1.getWeight().charAt(0));
				score2 = (double)((int)o2.getWeight().charAt(0));
				
				double sc = score2 - score1;
				if (sc >= 0) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		
	}

	
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
		
		//62 202 203 215
		try {
			// 将放入redis的db中，方便前端业务逻辑层接口调用
			Jedis jedis62 = new Jedis("10.32.21.62", 6379, 10000);
			if(appid.equals("fresh"))
				jedis62.select(15);
			else if(appid.equals("recommend"))
				jedis62.select(11);
			else
				return;
			
			if(isAppend){
				String s_allItems = jedis62.get(s_tableName);
				if(s_allItems == null){
					LOG.error("s_allItems == null");
				}else if(s_allItems.equals("null")||s_allItems.isEmpty()){
					String s_dis_items = JsonUtils.toJson(dis_items);
					jedis62.setex(s_tableName, (int) validTime, s_dis_items);
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
						
						String status = jedis62.setex(s_tableName, (int) validTime, s_dis_items);
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
				jedis62.setex(s_tableName, (int) validTime, s_dis_items);
				
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
		fieldDicts.itemModelPath = "/data/irecommend4app_new/itemmodel/";
		fieldDicts.modelPath = "/data/irecommend4app_new/usermodel/";
		fieldDicts.appTreeMappingFile = "/data/irecommend4app_new/AppTreeMapping.txt";
		fieldDicts.frontAppTreeMappingFile = "/data/irecommend4app_new/APPFront_TreeMapping.txt";
		fieldDicts.stopwordsFile = "/data/irecommend4app_new/stopwords.txt";
		fieldDicts.tm_doc_dir = "/data/irecommend4app_new/tm/doc/";
		fieldDicts.tm_word_dir = "/data/irecommend4app_new/tm/word/";
		fieldDicts.tm_words_file = "/data/irecommend4app_new/tm/dict_topicmodel";
		fieldDicts.testUsersFile = "/data/irecommend4app_new/appActiveUsers_5w.txt";
		fieldDicts.pcVisualHotFile = "/projects/zhineng/pcHotPredict/pcHotLevel";
		fieldDicts.locationMapFilePath = "/data/irecommend4app_new/locationMap.txt";
		fieldDicts.newsLifeTimePropertiesFilePath = "/data/irecommend4app_new/newsLifeTime.properties";
		
		
		
		
		itemDisNew id = new itemDisNew();
		
		if(args[0].equals("debug"))
			id.DEBUG = true;
		
		String appid = "recommend";
		if(args[1].equals("fresh"))
			appid = "fresh";
		
		Thread t=new Thread(new CommonDataSub());
		t.start();
		
		
		while(true)
		{
			LOG.info("start dis:");
			id.dis(appid);
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
