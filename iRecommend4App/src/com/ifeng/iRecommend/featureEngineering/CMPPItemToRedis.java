package com.ifeng.iRecommend.featureEngineering;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.redis.JedisInterface;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.hexl.redis.GetUsefulKeyFromRedis;
import com.ifeng.iRecommend.featureEngineering.DetectExtrmSim.DetectExtrmSimDoc;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.KeywordExtract;
import com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement.TimeSensitiveInfo;
import com.ifeng.iRecommend.featureEngineering.dataStructure.JsonFromCMPP;
import com.ifeng.iRecommend.featureEngineering.dataStructure.SlideJson;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.thriftService.DocToSlideInterface;
import com.ifeng.iRecommend.featureEngineering.thriftService.JudgeBeautyInterface;
import com.ifeng.iRecommend.featureEngineering.thriftService.JudgeBeautyThrift;
import com.ifeng.iRecommend.zxc.bdhotword.bean.BDHotWordBean;
import com.ifeng.myClassifier.GetSimcFeature;

/**
 * 
 * <PRE>
 * 作用 : 解析来自cmpp的数据，并将其存入IKV中
 *   
 *   
 * 使用 : 需要输入两个参数，第一个是开启的写处理线程数量，第二个是抓取的起始时间戳（10位）
 * 								例如： 4							例如：1427644800
 *   
 * 示例 :
 *   
 * 注意 :停用词表的存储位置要及时修改
 * 	 
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015-8-8         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class CMPPItemToRedis extends BasisInstall {
	static Logger LOG = Logger.getLogger(CMPPItemToRedis.class);
	// 抓取的起始时间,10位时间戳
	private static long sTime = 0;// 1427644800;
	// 实体词提取判断标准
	// private static final int redisDbNum1 = 10; // 8081
	private static final int redisDbNum2 = 14; // 8082

	// 初始化
	private static void initial(long startTime) {
		sTime = startTime;
	}

	/**
	 * 把Item存入Redis
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Integer writeItemToRedis(String key, String value) {
		try {
			// 新方案的新数据需要同时写入两个reids DB
			// JedisInterface.set(key, value, redisDbNum1);
			JedisInterface.set(key, value, redisDbNum2);
		} catch (Exception e) {
			LOG.error("set-string-failed:" + key + "," + value, e);
			LOG.error("[ERROR] In write item to Redis.", e);
			return -1;
		}
		return 1;
	}
	public static String urlIdExtract(String url) {
		if (url == null)
			return "";
		String otherid = "";
		try{
		if (url.contains("cdn.iclient.ifeng.com"))
			otherid = "sub_" + url.split("/")[url.split("/").length - 1].split(".html")[0];
		else if (url.contains("api.iclient.ifeng.com") && url.contains("api_vampire_article_detail?fuid="))
		{
			otherid = url.split("api_vampire_article_detail\\?fuid=")[1].startsWith("zmt")?url.split("api_vampire_article_detail\\?fuid=")[1]:"zmt_"+url.split("api_vampire_article_detail\\?fuid=")[1];
		}
		else if (url.contains("api.iclient.ifeng.com") && url.contains("ipadtestdoc?aid="))
			otherid = url.split("ipadtestdoc\\?aid=")[1];
		else if (url.contains("share.iclient.ifeng.com") && url.contains("sharenews.f?aid="))
			otherid = url.split("ipadtestdoc\\?aid=")[1];
		else if (url.contains("ipadtestdoc"))
			otherid = "imcp_" + url.split("ipadtestdoc\\?aid=")[1];
		else if (!url.contains("http"))
			otherid = url;
		if(otherid == null)
			otherid = "";
		LOG.info("[INFO] url id Extract."+otherid);
		}catch(Exception e){
			e.printStackTrace();
			LOG.error("[ERROR] url id Extract.");
		}
		return otherid;
	}
	/**
	 * 从url中提取cmppid和subid
	 */
	public static itemf urlIdExtract(itemf item) {
		itemf tempitem = item;
		if (tempitem == null || tempitem.getUrl() == null)
			return null;
		String url = tempitem.getUrl();
		try{
		if (url.contains("cdn.iclient.ifeng.com"))
			tempitem.setSubid("sub_" + url.split("/")[url.split("/").length - 1].split(".html")[0]);
		else if (url.contains("api.iclient.ifeng.com") && url.contains("api_vampire_article_detail?fuid="))
		{
			tempitem.setZmtid(url.split("api_vampire_article_detail\\?fuid=")[1].startsWith("zmt")?url.split("api_vampire_article_detail\\?fuid=")[1]:"zmt_"+url.split("api_vampire_article_detail\\?fuid=")[1]);
		}
		else if (url.contains("api.iclient.ifeng.com") && url.contains("ipadtestdoc?aid="))
			tempitem.setNewcmppid(url.split("ipadtestdoc\\?aid=")[1]);
		else if (url.contains("share.iclient.ifeng.com") && url.contains("sharenews.f?aid="))
			tempitem.setNewcmppid(url.split("ipadtestdoc\\?aid=")[1]);
		}catch(Exception e){
			e.printStackTrace();
			LOG.error("[ERROR] url id Extract.");
		}
		return tempitem;
	}

	public static void witerToutiaoRelated(itemf item) {
		try {
			HashMap<String, String> resultMap = new HashMap<String, String>();
			String simid = item.getOther().split("sameId=")[1].split("\\|!\\|")[0];
			resultMap.put("title", item.getTitle());
			resultMap.put("simid", simid);
			resultMap.put("doctype", item.getDocType());
			resultMap.put("otherid", urlIdExtract(item.getUrl()));
			if (simid != null && simid.contains("clusterId")) {
				String fjson = JsonUtils.toJson(resultMap, HashMap.class);
				int redisDbNum = 5;
				Jedis jedis = new Jedis("10.90.7.60", 6379);
				try {
					jedis.select(redisDbNum);
					jedis.hmset(item.getID(), resultMap);
					jedis.close();
					LOG.info("set-string-succeed: " + fjson);
				} catch (Exception e) {
					LOG.error("set-string-failed: " + fjson, e);
					LOG.error("[ERROR] In write item to Redis.", e);
					jedis.close();
				}
			}
		} catch (Exception e) {
			LOG.error("[ERROR]Write toutiao related item.", e);
			e.printStackTrace();
		}
	}

	/**
	 * 传入当前的item，命名为itemNow。 使用分词后的title，从solr中进行查询，找到相似度最大的top100（最大值），倒排item
	 * list 使用当前itemnow与itemlist中的item进行对比
	 * 
	 * @param s_title
	 *            已经分词的title
	 * @throws IOException
	 */
	public static itemf solrCheck(itemf itemNow) throws IOException// synchronized
	{
		if (itemNow == null)
			return itemNow;
		String solr_title = "";
		solr_title = itemNow.getSplitTitle();
		// 在lucene库中进行查询，获取itemlist
		LOG.info("solr_title is " + solr_title.trim() + "\t" + itemNow.getID() + "\n");
		// 存储内容抄袭检测的结果
		Map<itemf, Double> docSimMap; // = new ArrayList<Double>();
		docSimMap = DetectExtrmSimDoc.getSimForTarget(itemNow, false);
		if (docSimMap != null)
			LOG.info("docSimMap size is " + docSimMap.size());
		else {
			LOG.info("docSimMap is null. ");
		}
		ArrayList<String> featureNow = itemNow.getFeatures();
		ArrayList<String> featureEnd = itemNow.getFeatures();
		LOG.info("The original feature is " + featureNow);
		// 对相似度超过一定阈值的item进行处理
		if (docSimMap != null)
			for (Entry<itemf, Double> entry : docSimMap.entrySet()) {
				itemf itemSaved = entry.getKey();
				Double value = entry.getValue();
				if (Math.abs(value) > 0.4) {
					LOG.info("similar item is " + itemSaved.getID() + "\t" + itemSaved.getTitle() + "\tdocSim=" + value + "\n");
					// 需要合并的item的featureList
					ArrayList<String> featureList = itemSaved.getFeatures();
					if (value > 0.9)
						featureEnd = FeatureExTools.mergeFeature(featureEnd, itemNow.getDocType(), featureList, itemSaved.getDocType(), 1);
					else if (value > 0.7 || value < 0)
						featureEnd = FeatureExTools.mergeFeature(featureEnd, itemNow.getDocType(), featureList, itemSaved.getDocType(), 1);
					featureList.clear();
				}
			}
		// 更新最终的新item features
		featureEnd = FeatureExTools.delSpareFea(featureEnd);
		if (featureEnd != null)
			itemNow.setFeatures(featureEnd);
		else
			LOG.warn("featureEnd is null.");
		// 把新的item存入solr
		LOG.info("add to lucene feature is " + itemNow.getFeatures());
		return itemNow;
	}

	public static void checkRedisFull() {
		Set<String> keyset = new HashSet<String>();
		Jedis jedis = null;
		try {
			jedis = JedisInterface.getJedisInstance();
			jedis.select(redisDbNum2);
			keyset = jedis.keys("*");
		} catch (Exception e) {
			LOG.error("[ERROR] Read item from Redis.", e);
		}
		while (keyset.size() >= 3000) {
			keyset = jedis.keys("*");
			try {
				Thread.sleep(30 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOG.info("[INFO]Jedis pool size greater than 3000, sleep 30 seconds.");
		}
		JedisInterface.releaseJedisInstance(jedis);
	}

	public static boolean checkItemFromIKV(String id, String other) {
		itemf item = null;
		try {
			item = itemop.queryItemF(id, "c");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (item != null
				&& (item.getFeatures().contains("c") || item.getFeatures().contains("sc") || item.getFeatures().contains("cn") || item.getFeatures().contains("kb") || item.getFeatures().contains("e")
						|| item.getFeatures().contains("s") || item.getFeatures().contains("s1") || item.getFeatures().contains("loc"))) {
			// 用新的other字段代替原有的other字段
			item.setOther(other);
			// 把item转换成json串
			String json = "";
			try {
				json = JsonUtils.toJson(item, itemf.class);
			} catch (Exception e) {
				LOG.error("[ERROR] Error occurred when change item to Json. Id is " + item.getID(), e);
				return false;
			}
			LOG.info("item from IKV id is " + item.getID());
			if (item.getFeatures() != null)
				LOG.info("item from IKV feature is " + item.getFeatures().toString());
			else
				LOG.info("item from IKV feature is null.");
			LOG.info("Other for redis is " + item.getOther());
			int writeRedis = writeItemToRedis(item.getID(), json);
			if (writeRedis == 1) {
				LOG.info("[INFO] Get item from IKV Write to redis " + item.getID() + " successful.");
			}
			witerToutiaoRelated(item);
			return true;
		} else {
			LOG.info("Item " + id + " in IKV is null.");
			return false;
		}

	}

	/**
	 * 稿源匹配 根据稿源库对稿源进行审查，针对符合条件的稿源，当做特征写入feature中
	 * 
	 * @param logstr
	 * @param item
	 * @return
	 */
	public static itemf sourceMatch(String logstr, itemf item) {
		if (articleSourceMap == null || articleSourceMap.get(item.getSource()) == null)
			LOG.info("[INFO] Its newsource " + item.getSource());
		else if (articleSourceMap.get(item.getSource()).equals("soft") && GetUsefulKeyFromRedis.GetUsefulFlag(item.getSource()) == 1) {
			item.addFeatures(item.getSource());
			item.addFeatures("s1");
			item.addFeatures("1.0");
		} else if (!articleSourceMap.get(item.getSource()).equals("serious") && item.getOther().contains("wemedia") && GetUsefulKeyFromRedis.GetUsefulFlag(item.getSource()) == 1) {
			item.addFeatures(item.getSource());
			item.addFeatures("s1");
			item.addFeatures("1.0");
		}
		return item;
	}

	public static void writeResult(itemf item) {
		// 把item转换成json串
		String json = "";
		try {
			json = JsonUtils.toJson(item, itemf.class);
		} catch (Exception e) {
			LOG.error("[ERROR] Error occurred when change item to Json. Id is " + item.getID(), e);
			return;
		}
		LOG.info("Other for redis is " + item.getOther());
		int writeRedis = writeItemToRedis(item.getID(), json);
		if (writeRedis == 1) {
			LOG.info("[INFO] Write redis " + item.getID() + " successful.");
			witerToutiaoRelated(item);
		}
	}
	/**
	 * 判断docpic类型的文章是否可以转为slide
	 * 
	 * @param content
	 * @return
	 */
	public static boolean docpicToSlide(int imgCount, String content) {
		String[] splitContent = content.split("#p#");
		boolean ifPicAndText = true;
		for (int i = 0; i < splitContent.length; i++) {
			if (splitContent[i].length() > 500 || splitContent[i].split("\n").length >= 3) {
				ifPicAndText = false;
			}
		}
		if (imgCount >= 5 && (content.length() <= 1000 || ifPicAndText)) {
			return true;
		}
		return false;
	}

	/**
	 * 为标题和内容分词
	 * 
	 * @param logstr
	 * @param item
	 * @param title
	 * @param content
	 * @return
	 */
	public static itemf splitWord(String logstr, itemf item, String content) {
		// 分词前的准备，合并title和content统一分词，因为分词系统需要网络访问。
		StringBuffer splitBuffer = new StringBuffer();
		splitBuffer.append(item.getTitle());
		splitBuffer.append(FeatureExTools.splitTag).append(content);
		String preSplit = TextProTools.filterString(splitBuffer.toString());
		// 分词
		String textSplited = "";
		for (int i = 0; i < 3; i++) {
			try {
				textSplited = new String(SplitWordClient.split(preSplit, null).replace("(/", "_").replace(") ", " "));
				break;
			} catch (Exception e) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				LOG.error("[ERROR] Split error. Wait 5 seconds. " + i + " times." + logstr);
			}
		}
		// 分词后对title和content进行分割和识别
		String textSplits[] = textSplited.split(FeatureExTools.splitedTag);
		String s_title = null;
		String s_content = null;
		if (textSplits.length == 2) {
			s_title = textSplits[0];
			s_content = textSplits[1];
			item.setSplitTitle(s_title);
			item.setSplitContent(s_content);
			return item;
		} else {
			LOG.error("[ERROR] Splited words result is wrong." + logstr);
			return item;
		}
	}

	/**
	 * 数据预处理 把基础内容存入item中 包括特殊字符过滤，图片地址提取，html标签过滤，分词等
	 * 
	 * @param logstr
	 * @param jsonItem
	 * @return
	 */
	public static itemf dataPreParse(String logstr, JsonFromCMPP jsonItem) {
		itemf item = new itemf();
		item.setID(jsonItem.getId());
		item.setTitle(jsonItem.getTitle());
		item.setUrl(jsonItem.getSourceLink());
		if (jsonItem.getSourceAlias() != null)
			item.setSource(jsonItem.getSourceAlias());
		else
			item.setSource("");
		if (jsonItem.getOther() != null)
			item.setOther(jsonItem.getOther());
		else
			item.setOther("");
		item.setAppId("recommend");
		item.setPublishedTime(jsonItem.getPublishedTime());
		item.setShowStyle(jsonItem.getShowStyle());
		item.setModifyTimeCurrent();
		long contentExtractTime = System.currentTimeMillis();
		/******************* 标题和内容提取过滤模块 ***********************/
		int imgCount = 0;// 记录文章中的图片数目
		// 获取文章中的图片数量
		if (jsonItem.getContent().contains("<img")) {
			String[] imgSplit = jsonItem.getContent().split("<img");
			imgCount = imgSplit.length - 1;
		}
		String content = jsonItem.getContent();
		// 存储图片的url地址
		List<String> picUrlList = new ArrayList<String>();
		// 幻灯片有特殊的格式 需要二次解析
		if (jsonItem.getType().equals("slide") && content != null) {
			Gson gson = new Gson();
			ArrayList<SlideJson> slideJsonList = null;
			try {
				slideJsonList = gson.fromJson(content, new TypeToken<List<SlideJson>>() {
				}.getType());
				content = "";
				// 获取图片url并在文本内容中插入图片位置标记
				for (SlideJson sj : slideJsonList) {
					content = content + " #p# " + "\n" + sj.getTitle() + "\n";
					picUrlList.add(sj.getTimg());
				}
			} catch (Exception e) {
				LOG.error("[ERROR]Get " + logstr + " slide json error.", e);
			}
		} else {// 非幻灯片直接提取图片url，并对内容进行过滤
			picUrlList = TextProTools.findPicUrl(content);
			content = TextProTools.filterHtml(content);
		}
		// 合并图片数目到other字段中
		item.setOther(item.getOther() + "|!|imgNum=" + String.valueOf(imgCount));
		// 获取文章类型字段
		String docType = null;
		if (jsonItem.getType().equals("article")) {
			docType = TextProTools.processDocType(String.valueOf(imgCount), jsonItem.getOther());
		} else {
			docType = "slide";
		}
		item.setDocType(docType);
		content = TextProTools.filterString(content);
		item = splitWord(logstr, item, content);
		if (docType.equals("docpic") && docpicToSlide(imgCount, content))
			item.setOther(item.getOther() + "|!|canbeSlide=true");
		// else
		// item.setOther(item.getOther() + "|!|canbeSlide=false");
		// 把图片地址恢复到正文中
		if (picUrlList != null) {
			for (int i = 0; i < picUrlList.size(); i++) {
				try {
					item.setSplitContent(item.getSplitContent().replaceFirst("#_w p_nx #_w", picUrlList.get(i)));
				} catch (Exception e) {
					LOG.error("[ERROR] picUrlList size is differ from content's pic flags size." + logstr, e);
				}
			}
		}
		LOG.info(logstr + " [TIME] Extract content and split words used time " + (System.currentTimeMillis() - contentExtractTime));
		return item;
	}

	/**
	 * 热点事件提取和时效性判断
	 * 
	 * @param strlog
	 * @param item
	 * @return
	 */
	public static itemf hotwordAndTimeSensitive(String strlog, itemf item) {
		boolean realevent = false;
		/******************* 热词匹配模块 ***********************/
		long hotwordExtractTime = System.currentTimeMillis();
		Set<BDHotWordBean> baiduHotKey = hotwordOb.detectWord(item.getSplitTitle(), item.getSplitContent());
		// 事件识别，注意返回的注释说明
		BDHotWordBean rt = hotwordOb.detectEvent(item.getID(), "c", item.getSplitTitle(), item.getSplitContent(), item.getCategory());
		// ArrayList<String> hotList = new ArrayList<String>();
		Jedis eventjedis = new Jedis("10.32.24.194", 6379);
		try {
			eventjedis.select(4);
			String publishday = null;
			if (item != null && item.getPublishedTime() != null)
				publishday = item.getPublishedTime().split(" ")[0];
			hotwordOb.collectEvent(item.getID(), "c", publishday, rt, eventjedis);
			eventjedis.close();
		} catch (Exception e) {
			LOG.error("[ERROR] In write event to Redis." + strlog, e);
			eventjedis.close();
		}
		ArrayList<String> hotList = new ArrayList<String>();
		if (rt != null) {
			hotList.add(rt.getStr());
			LOG.info(strlog + " [INFO] Hot event is " + rt.getStr());
			hotList.add(rt.getType());
			hotList.add(String.valueOf(rt.getScore()));
			if (rt.getScore() >= 0.5)
				realevent = true;
		} else {
			LOG.info(strlog + "[INFO] hot event is null.");
		}
		if (baiduHotKey != null && baiduHotKey.size() >= 1)
			for (BDHotWordBean b : baiduHotKey) {
				hotList.add(b.getStr());
				LOG.info(strlog + " [INFO]  hot keyword is " + b.getStr());
				hotList.add(b.getType());
				hotList.add(String.valueOf(b.getScore()));
			}
		else
			LOG.info(strlog + " [INFO] hot keyword is null.");
		if (hotList != null) {
			item.setHotEvent(hotList);
			for (String hotkey : hotList)
				item.addFeatures(hotkey);
		}
		LOG.info(strlog + " [TIME] hotword used time " + (System.currentTimeMillis() - hotwordExtractTime));
		/*********************** 时效性 **************************/
			TimeSensitiveInfo timeSensitiveInfo = timeSensitiveJudgement
					.EstimateTimeSensitiveOfArticle(item.getPublishedTime(),
							item.getSplitTitle(), item.getSplitContent(),
							item.getCategory());
			String timeSensitive = "timeSensitive=";
			if(timeSensitiveInfo.getOthers()!=null&&timeSensitiveInfo.getOthers().contains("="))
			{
				timeSensitive = timeSensitive + timeSensitiveInfo.getOthers().split("=")[1].split("\\|!\\|")[0];
			}
			if(timeSensitive.contains("=nt")&&realevent)
			{
				try {
					timeSensitive = "timeSensitive="+DateConvertTool.getDate(DateConvertTool.getTimeStamp(item.getPublishedTime()+24*60*60)*1000);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			item.setOther(item.getOther() + "|!|"+timeSensitive);
		return item;
	}

	/**
	 * 第二次关键词提取
	 * 
	 * @param logstr
	 * @param item
	 * @return
	 */
	public static itemf secondKeywordEx(String logstr, itemf item) {
		long categoryFilterTime = System.currentTimeMillis();
		ArrayList<String> newFeatures = new ArrayList<String>();
		KeywordExtract ke = kvJudge.getKeywordExtract(item.getSplitTitle(), item.getSplitContent(), item.getCategory());
		List<String> secondKeywordsList = kvJudge.getPreKeyword(ke);
		// 实体-分类关系
		Map<String, Set<String>> etMap = kvJudge.getEtMap(ke);
		// 实体-全部string关系
		Map<String, String> levelMap = kvJudge.getLevelMap(ke);
		LOG.info(logstr + " category for filter et is " + item.getCategory());
		List<String> list = kvJudge.getFilterFeature(logstr, etMap, levelMap, secondKeywordsList, item.getSplitTitle(), item.getCategory());
		if (list == null)
			list = new ArrayList<String>();
		LOG.info(logstr + " feature after filter is " + list);
		if (item.getFeatures() != null)
			for (int i = 0; i < item.getFeatures().size() - 2; i += 3) {
				String type = item.getFeatures().get(i + 1);
				if (type.equals("c") || type.equals("sc") || type.equals("cn") || type.equals("e") || type.equals("s") || type.equals("s1") || type.equals("loc")) {
					newFeatures.add(item.getFeatures().get(i));
					newFeatures.add(item.getFeatures().get(i + 1));
					newFeatures.add(item.getFeatures().get(i + 2));
				}
			}
		newFeatures.addAll(list);
		if (item.getTags() != null && item.getTags().size() >= 3) {
			for (String s : item.getTags())
				newFeatures.add(s);
		}
		item.setFeatures(newFeatures);
		LOG.info(logstr + " newItem feature is " + item.getFeatures());
		LOG.info(logstr + " [TIME] category filter used time " + (System.currentTimeMillis() - categoryFilterTime));
		return item;
	}

	/**
	 * 分类特征计算，调用分类器，并根据逻辑判断是否使用分类结果
	 * 
	 * @param logstr
	 * @param item
	 * @return
	 */
	public static itemf simCal(String logstr, itemf item) {
		if ((item == null || !FeatureExTools.ifContainC1(item.getFeatures())) && !item.getOther().contains("source=chaoslocal")) {
			ArrayList<String> cList = null;
			/******************* simc分类模块 ***********************/
			long simcTime = System.currentTimeMillis();
			boolean needSimc = false;
			if (!item.getDocType().equals("slide") && (item.getTags() == null || item.getTags().size() < 3) && !item.getDocType().equals("video"))
				needSimc = true;
			else if (item.getDocType().equals("slide") || item.getDocType().equals("video") || item.getFeatures().contains("天气"))
				needSimc = false;
			else
				needSimc = !FeatureExTools.ifContainC1(item.getTags());
			if (needSimc)
				cList = GetSimcFeature.ClassifyByWord(logstr, item.getSplitTitle(), item.getSource(), item.getFeatures());
			if (cList == null)
				cList = new ArrayList<String>();
			LOG.info("[INFO] " + logstr + " simc result is " + cList);
			LOG.info(logstr + " [TIME] simc used time " + (System.currentTimeMillis() - simcTime));
			LOG.info("[INFO] " + logstr + " feature doesn't contain c1, copy simc result to feature. " + cList);
			for (int k = 0; k < cList.size() - 2; k += 3) {
				item.addFeatures(cList.get(k));
				item.addFeatures(cList.get(k + 1));
				item.addFeatures(cList.get(k + 2));
			}
		}
		return item;
	}

	/**
	 * other字段解析
	 * 
	 * @param strlog
	 * @param item
	 * @return
	 */
	public static itemf otherParser(String strlog, itemf item) {
		long tagsParserTime = System.currentTimeMillis();
		CMPPDataOtherField f = new CMPPDataOtherField(item.getOther());
		ArrayList<String> tagList = CMPPDataOtherParser.OtherParser(f, item.getSource(), item.getDocType());
		LOG.info("[INFO] " + strlog + " tags parser is " + tagList);
		LOG.info(strlog + " [TIME] tags parser used time " + (System.currentTimeMillis() - tagsParserTime));
		if (tagList != null && tagList.size() >= 3) {
			item.setTags(tagList);
			for (String s : tagList)
				item.addFeatures(s);
		}
		return item;
	}

	/**
	 * 停用词过滤
	 * 
	 * @param logstr
	 * @param item
	 * @return
	 */
	public static itemf stopwordFilter(String logstr, itemf item) {
		ArrayList<String> tempFea = new ArrayList<String>();
		for (int i = 0; i < item.getFeatures().size() - 2; i += 3) {
			if (stopword.contains(item.getFeatures().get(i))) {
			} else {
				tempFea.add(item.getFeatures().get(i));
				tempFea.add(item.getFeatures().get(i + 1));
				tempFea.add(item.getFeatures().get(i + 2));
			}
		}
		LOG.info(logstr + "feature after stopword " + tempFea);
		tempFea = FeatureExTools.delSpareFea(tempFea);
		item.setFeatures(tempFea);
		return item;
	}

	/**
	 * 处理jsonItem的操作。
	 * 
	 * @param jsonItem带解析的原始item
	 * @param
	 */
	public static void processItem(JsonFromCMPP jsonItem,DocToSlideInterface docToSlide, JudgeBeautyThrift judgeBeauty) throws Throwable {
		// checkRedisFull();
		if (jsonItem == null) {
			LOG.info("jsonItem is null.");
			return;
		}
		if (checkItemFromIKV(jsonItem.getId(), jsonItem.getOther()))
			return;
		String strForLog = "#id_" + jsonItem.getId() + "#title_" + jsonItem.getTitle();
		itemf item = dataPreParse(strForLog, jsonItem);
		// 提取关键词代码初始化
		KeywordExtract ke = kvJudge.getKeywordExtract(item.getSplitTitle(), item.getSplitContent(), null);
		ArrayList<String> keywordList = new ArrayList<String>();
		keywordList.addAll(kvJudge.getPreKeyword(ke));
		for (String s : keywordList) {
			item.addFeatures(s);
		}
		LOG.info("[INFO] " + jsonItem.getId() + " Get keyword list is " + keywordList);
		// 获取tags对应的c，sc等信息
		item = otherParser(strForLog, item);
		for (String original : originalSet) {
			if (item.getTitle().contains(original)) {
				item.addFeatures(original);
				item.addFeatures("s1");
				item.addFeatures("1.0");
			}
		}
		// 从lucene中查询相似
		if (!item.getOther().contains("source=phvideo"))
			item = solrCheck(item);
		// 抄袭检测没有结果则把simc结果放入feature
		item = simCal(strForLog, item);
		long beautyTime = System.currentTimeMillis();
		if(judgeBeauty.ifBeauty(item.getID(), item.getTitle(), item.getDocType(), "hexl"))
		{
			item.setBeauty(true);
		}
		else
			item.setBeauty(false);
		LOG.info(strForLog + " [TIME] beauty judge used time " + (System.currentTimeMillis() - beautyTime));
		// 规则引擎修正feature
		long ruleModifyTime = System.currentTimeMillis();
		item.setFeatures(ruleModify.modifyResult(item.getSplitTitle(), item.getFeatures(), item.getDocType(),item.getOther().contains("canbeSlide=true")));
		LOG.info("[INFO] " + strForLog + "Feature after rulemodify is " + item.getFeatures());
		LOG.info(strForLog + " [TIME] ruleModify used time " + (System.currentTimeMillis() - ruleModifyTime));
		item.setCategory(FeatureExTools.whatCategory(item.getFeatures()));
		item = secondKeywordEx(strForLog, item);
		/******************* 稿源匹配模块 ***********************/
		item = sourceMatch(strForLog, item);
		item = urlIdExtract(item);
		item = hotwordAndTimeSensitive(strForLog, item);
		item.setFeatures(ruleModify.modifyResult(item.getSplitTitle(), item.getFeatures(), item.getDocType(),item.getOther().contains("canbeSlide=true")));
		/****************** 停用词过滤 ***********************/
		item = stopwordFilter(strForLog, item);
		writeResult(item);
	}
	/**
	 * 数据输入处理类
	 */
	static class dataInputThread extends Thread {
		private BlockingQueue<JsonFromCMPP> fileQueue;

		public dataInputThread(BlockingQueue<JsonFromCMPP> fileQueue) {
			super("data-input-thread");
			this.fileQueue = fileQueue;
		}

		public void run() {
			try {
				CMPPDataCollect.getDataForRedis(sTime, fileQueue);
				// CMPPDataCollect.getDataFromCMPP187(sTime, fileQueue);
				fileQueue.put(DEADLY_POISON);// 放置毒药，优雅关闭
			} catch (InterruptedException e) {
				// 在这里可以做一些异常处理
				e.printStackTrace();
				LOG.error("Thread ERROR, the thread has stopped.");
			}
		}
	}

	/** 处理输入jsonItem的线程，可以多线程并发处理，每个线程处理一个item */
	static class TravelQueueThread extends Thread {
		private final static AtomicInteger ThreadCount = new AtomicInteger();
		private BlockingQueue<JsonFromCMPP> fileQueue;

		public TravelQueueThread(BlockingQueue<JsonFromCMPP> fileQueue) {
			super("travel-queue-thread-" + ThreadCount.incrementAndGet());
			this.fileQueue = fileQueue;
		}

		public void run() {
			JsonFromCMPP jsonItem = null;
			DocToSlideInterface docToSlide = new DocToSlideInterface();
			JudgeBeautyThrift judgeBeauty = new JudgeBeautyThrift();
			try {
				while ((jsonItem = fileQueue.take()) != DEADLY_POISON) {
					try {
						processItem(jsonItem,docToSlide, judgeBeauty);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
				fileQueue.put(DEADLY_POISON);// 放置毒药，优雅关闭
			} catch (InterruptedException e) {
				// 在这里可以做一些异常处理
				e.printStackTrace();
			}
		}
	}

	private BlockingQueue<JsonFromCMPP> fileQueue = new LinkedBlockingQueue<JsonFromCMPP>(MAX_QUEUE_SIZE);
	private Thread visitFolderThread;
	private Thread[] travelFileThreads;

	/**
	 * 构造函数
	 * 
	 * @param sourceFile
	 * @param travelThreads
	 */
	public CMPPItemToRedis(int travelThreads, long startTime) {
		super();
		// 初始化DEADLY_POISON,抓取cmpp的起始时间（long型 10位的时间戳）
		initial(startTime);
		visitFolderThread = new dataInputThread(fileQueue);
		travelFileThreads = new TravelQueueThread[travelThreads];
		for (int i = 0; i < travelFileThreads.length; i++) {
			travelFileThreads[i] = new TravelQueueThread(fileQueue);
		}
	}

	/**
	 * 开始执行
	 */
	public void start() {

		visitFolderThread.start();
		for (int i = 0; i < travelFileThreads.length; i++) {
			travelFileThreads[i].start();
		}
	}

	/**
	 * 强行终止。请慎用。程序会自动关闭
	 */
	public void terminate() {
		visitFolderThread.interrupt();
		for (int i = 0; i < travelFileThreads.length; i++) {
			travelFileThreads[i].interrupt();
		}
	}

	/**
	 * 测试用例
	 */
	public static void main(String[] args) {
		final int travelThreads = Integer.valueOf(args[0]);
		long startTime = Long.valueOf(args[1]);
		CMPPItemToRedis fetcher = new CMPPItemToRedis(travelThreads, startTime);
		fetcher.start();
	}
}
