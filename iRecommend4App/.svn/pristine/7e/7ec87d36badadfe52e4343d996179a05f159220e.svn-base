package com.ifeng.iRecommend.featureEngineering;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.hexl.redis.GetUsefulKeyFromRedis;
import com.ifeng.iRecommend.featureEngineering.DetectExtrmSim.DetectExtrmSimDoc;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.*;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperation;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.ArticleSourceData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.WordReadData;
import com.ifeng.myClassifier.GetSimcFeature;
import com.ifeng.myClassifier.simCla;

/**
 * 
 * <PRE>
 * 作用 : 根据输入的标题，url进行查询，如果不能查到结果 则对标题和内容进行解析，寻找相似的内容并把其feature赋给当前查询
 *   
 *   
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
 *          1.0          2015-8-8         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class QuerySimilar {
	static Logger LOG = Logger.getLogger(QuerySimilar.class);
	// 在lucene库中进行查询，获取itemlist
	static IndexOperation indexOp = IndexOperation.getInstance();
	simCla simc = simCla.getInstance();
	// 特征的类型
	private static String originalPath = LoadConfig.lookUpValueByKey("originalPath");
	private static HashSet<String> blackList = new HashSet<String>();
	//手凤端相关阅读
	private static IKVOperation wapRelatedIDsInterface = new IKVOperation("cmppDyn");
	//feature修正规则引擎
	RuleModify ruleModify = RuleModify.getInstance();
	KeywordValueJudge kvJduge = KeywordValueJudge.getKVJudgeInstance();
	WordReadData wordReadData;
	ArticleSourceData sourceData = ArticleSourceData.getInstance();
	ConcurrentHashMap<String, String> articleSourceMap = sourceData.getArticleSourceMap();
	QuerySimilar() {
		try {
			wordReadData = WordReadData.getInstance();
			LOG.info("word readdata map size is :"+wordReadData.getWordReadMap().size());
			readBlackList();
		} catch (Exception e) {
			LOG.error("[ERROR]Load blackList failed.", e);
		}
	}

	private static QuerySimilar instance = new QuerySimilar();

	public static QuerySimilar getInstance() {
		return instance;
	}

	public void test(){
		for(Entry<String,String> e : articleSourceMap.entrySet())
		{
			System.out.println(e.getKey()+"-----------------"+e.getValue());
		}
	}
	/**
	 * 加载黑名单
	 * 
	 * @throws IOException
	 */
	private void readBlackList() throws IOException {
		FileReader fr = new FileReader(originalPath);
		BufferedReader br = new BufferedReader(fr);
		String s = null;
		while ((s = br.readLine()) != null) {
			blackList.add(s.trim());
		}
		br.close();
		fr.close();
		if (blackList != null)
			LOG.info("BlackList size is " + blackList.size());
		else
			LOG.info("BlackList is null.");
	}

	/**
	 * 
	 */
	public ArrayList<String> GetRelatedItemId(ArrayList<String> featureList, String id, String title, String url, String s_title, String source)
	{
		ArrayList<String> appRelatedIDs = new ArrayList<String>();
		List<String> wapRelatedIDs = new ArrayList<String>();
		try {
			RelatedItem obj = new RelatedItem();
			Map<String, List<String>> idList = obj.getRelatedItemId(featureList, s_title, source);
			if (idList != null) {
				if(idList.get("客户端")!=null)
				appRelatedIDs.addAll(idList.get("客户端"));
				if(idList.get("手凤")!=null)
				wapRelatedIDs = idList.get("手凤");
			}
		} catch (Exception e) {
			LOG.error("[ERROR]Some error in getRelatedItemId.", e);
		}
		LOG.info("[INFO] app relatedIDs for id" + id + " title " + title + "is" + appRelatedIDs);
		LOG.info("[INFO] wap relatedIDs for id" + id + " title " + title + "is" + wapRelatedIDs);
		if (appRelatedIDs == null || appRelatedIDs.size() < 3)
			appRelatedIDs = null;
		else {
		}
		if (wapRelatedIDs == null || wapRelatedIDs.size() < 3)
			wapRelatedIDs = null;
		else {
			wapRelatedIDs.remove(0);
		}
		writeWapRelatedIds(id, url, title, wapRelatedIDs);
		return appRelatedIDs;
	}
	/**
	 * 
	 * @param title
	 *            文章标题
	 * @param url
	 *            文章url http://news.ifeng.com/xxxxxx.html
	 * @param content
	 *            文章内容
	 * @param docType
	 *            文章类型 doc普通文章 slide幻灯图集 self自媒体
	 * @return List<String> featureList [word,type,weight,word,type,weight...]
	 *         or null
	 * @throws IOException
	 */
	public HashMap<String,ArrayList<String>> querySimilar(String id, String title, String s_title, String url, String s_content, String docType, String stage, String other, String source) throws IOException {
		if (title == null) {
			LOG.info("Title is null.Return.");
			return null;
		}
		if(other == null)
			other = "";
		if(source == null)
			source = "";
		ArrayList<String> resultList = new ArrayList<String>();
		boolean blackFlag = false;
		String black = null;
		for (String s : blackList) {
			if (title.contains(s)) {
				blackFlag = true;
				black = s;
				break;
			}
		}
		if (blackFlag) {
			resultList.add(black);
			resultList.add("s1");
			resultList.add("1");
		}
		itemf item = null;
		item = new itemf();
		item.setTitle(title);
		item.setUrl(url);
		item.setDocType(docType);
		item.setSplitTitle(s_title);
		item.setSplitContent(s_content);
		ArrayList<String> newFeatureList = new ArrayList<String>();
		ArrayList<String> relatedIds = new ArrayList<String>();
		if (resultList == null || resultList.size() <= 1) {
			KeywordExtract ke = kvJduge.getKeywordExtract(s_title, s_content);
			Map<String, Set<String>> etMap = kvJduge.getEtMap(ke);
			Map<String, String> levelMap = kvJduge.getLevelMap(ke);
			List<String> keywordList = kvJduge.getPreKeyword(ke);
			LOG.info("pre keyword list is " + keywordList);
			for (String f : keywordList) {
				item.addFeatures(f);
			}
			if(articleSourceMap == null || articleSourceMap.get(source) == null)
				LOG.info("[INFO] Its newsource "+source);
			else if (articleSourceMap.get(source).equals("soft") && GetUsefulKeyFromRedis.GetUsefulFlag(source) == 1)
			{
				item.addFeatures(source);
				item.addFeatures("s1");
				item.addFeatures("1.0");
			}
			else if (!articleSourceMap.get(source).equals("serious") && other.contains("wemedia") && GetUsefulKeyFromRedis.GetUsefulFlag(source) == 1)
			{
				item.addFeatures(source);
				item.addFeatures("s1");
				item.addFeatures("1.0");
			}
			
			long solrCheckTime = System.currentTimeMillis();
			// 从lucene中查询相似
			itemf newItem = solrCheck(item);
			solrCheckTime = System.currentTimeMillis() - solrCheckTime;
			LOG.info("[TIME]solrCheckTime time is " + solrCheckTime);
			newFeatureList = newItem.getFeatures();
			boolean needSimc = false;
			if(!docType.equals("slide")&&(newFeatureList == null || newFeatureList.size() < 3))
				needSimc = true;
			else
				needSimc = !FeatureExTools.ifContainC1(newFeatureList);
			if (needSimc) {
				ArrayList<String> cList = GetSimcFeature.ClassifyByWord(s_title, source, newFeatureList);
//				cList = FeatureExTools.simclaResultMap(cList);
				if(cList == null)
					cList = new ArrayList<String>();
				cList.add("simc=true");
				cList.add("k");
				cList.add("-0.01");
				LOG.info("cList is " + cList);
				if (cList != null && cList.size() % 3 == 0)
					newFeatureList.addAll(cList);
			}
			newFeatureList = ruleModify.modifyResult(s_title, newFeatureList); 
			LOG.info("[INFO] "+ id + " feature after rule modify is "+newFeatureList);
			newFeatureList = FeatureExTools.delSpareFea(newFeatureList);
			relatedIds = GetRelatedItemId(newFeatureList,id,title,url,s_title,source);
			LOG.info("List before filter is " + newFeatureList);
			// 提取文章的类别信息 用于实体词提取的判断
			String category = FeatureExTools.whatCategory(newFeatureList);
			ArrayList<String> featureList = (ArrayList<String>) kvJduge.getFilterFeature(etMap, levelMap, newFeatureList, s_title, category);
			 LOG.info("[INFO] feature after filter is " + featureList);
			resultList = featureList;
		} else {
			newFeatureList = resultList;
			relatedIds = GetRelatedItemId(newFeatureList,id,title,url,s_title,source);
		}
		HashMap<String,ArrayList<String>> retmap = new HashMap<String,ArrayList<String>>();
		retmap.put("feature", resultList);
		retmap.put("relatedIds", relatedIds);
		return retmap;
	}

	/**
	 * 存储相关推荐的id列表(手凤需求)
	 * 
	 * @param id
	 * @param url
	 * @param title
	 * @param list
	 */
	public void writeWapRelatedIds(String id, String url, String title, List<String> list) {
		if (list == null || list.isEmpty())
			return;
		if (id == null && url == null && title == null)
			return;
		String value = JsonUtils.toJson(list, ArrayList.class);
		LOG.info("[INFO] Write related ids to IKV. " + id + "\t" + title + "\t" + value);
		if (id != null)
			wapRelatedIDsInterface.set("common_" + id, value);
		if (url != null)
			wapRelatedIDsInterface.set("common_" + url, value);
		if (title != null)
			wapRelatedIDsInterface.set("common_" + title, value);
	}

	/**
	 * 
	 * @param featureList
	 * @param source
	 * @param s_title
	 * @param s_content
	 * @return
	 */
	public ArrayList<String> featureClear(ArrayList<String> featureList, String source, String other, String relatedTag) {
		LOG.info("[INFO] Feature for clear is " + featureList);
		LOG.info("[INFO] Related tag for related id get is " + relatedTag);
		if (featureList == null)
			return null;

		ArrayList<String> originalList = new ArrayList<String>(featureList);
		ArrayList<String> backList = new ArrayList<String>();
		if (originalList != null && originalList.size() > 0 && blackList.contains(originalList.get(0))) {
			backList.add(originalList.get(0));
			if (relatedTag != null && !relatedTag.isEmpty() && !relatedTag.equals(""))
				backList.add(relatedTag);
			else
				backList.add("");
			if (backList != null && backList.size() >= 1)
				return backList;
			else
				return null;
		}
		String c0 = null;
		for (int i = 0; i < originalList.size(); i += 3) {
			if (originalList.get(i + 1).equals("c") && Double.valueOf(originalList.get(i + 2)) < 0) {
				c0 = originalList.get(i);
				break;
			}
		}
		String c1 = null;
		for (int i = 0; i < originalList.size(); i += 3) {
			if (originalList.get(i + 1).equals("c") && Double.valueOf(originalList.get(i + 2)) > 0) {
				c1 = originalList.get(i);
				break;
			}
		}
		String sc = null;
		for (int i = 0; i < originalList.size(); i += 3) {
			if (originalList.get(i + 1).equals("sc")) {
				sc = originalList.get(i);
				break;
			}
		}
		LOG.info("featureList before useful check is " + originalList);
		long usefulCheckTime = System.currentTimeMillis();
		originalList = GetUsefulKeyFromRedis.featureUsefulCheck(originalList);
		LOG.info("[TIME] usefulCheck time is " + (System.currentTimeMillis() - usefulCheckTime));
		HashSet<String> tempC0Set = new HashSet<String>();
		HashSet<String> tempC1Set = new HashSet<String>();
		if (originalList == null)
			return null;
		ArrayList<String> tfeaList = new ArrayList<String>();
		for (int i = 0; i < originalList.size() - 2; i += 3) {
			if (originalList.get(i + 1).equals("c") && Double.valueOf(originalList.get(i + 2)) < -0.5) {
				if (tempC0Set == null || tempC0Set.size() < 1)
					tempC0Set.add(originalList.get(i));
			} else if (originalList.get(i + 1).equals("c") && Double.valueOf(originalList.get(i + 2)) > 0.5) {
				if (tempC1Set == null || tempC1Set.size() < 2)
					tempC1Set.add(originalList.get(i));
			} else {
				tfeaList.add(originalList.get(i));
				tfeaList.add(originalList.get(i + 1));
				tfeaList.add(originalList.get(i + 2));
			}
		}
		LOG.info("tfeaList aftrer remove check is " + tfeaList);
		originalList = FeatureExTools.delSpareFea(tfeaList);
		ArrayList<String> resultList = new ArrayList<String>();
		
		int longCount = 0;
		if (originalList != null)
			for (int i = 0; i < originalList.size() - 2; i += 3) {
				String word = originalList.get(i);
				String type = originalList.get(i + 1);
				Double value = Double.valueOf(originalList.get(i + 2));
				Double absvalue = Math.abs(Double.valueOf(originalList.get(i + 2)));
				boolean ifread = true;
				boolean ifcontain = false;
				if(wordReadData.getWordReadMap().containsKey(word))
				{
					ifcontain = true;
					ifread = wordReadData.getWordReadMap().get(word).isRead();
				}
				if ((type.equals("c") && absvalue > 0.4)
						|| ((type.equals("sc") 
						|| (type.equals("cn") && value >= 0.4) 
						|| type.equals("e") 
						|| type.equals("t") 
						|| (type.equals("et") && ifread) 
						|| type.equals("s")
						|| type.equals("s1") 
						|| type.equals("loc"))&& absvalue >= 0.5 && absvalue < 10) 
						|| (type.equals("kq") && absvalue >= 0.5 && absvalue < 10 && ifread) 
						|| (type.equals("ks") && absvalue >= 0.5 && absvalue < 10 && ifread)
						|| (type.equals("kb") && absvalue > 0.4 && absvalue < 10 && ifread)
						|| (type.startsWith("n") && absvalue >= 0.5 && absvalue < 10 && ifcontain && ifread)
						|| (type.equals("k") && absvalue >= 0.5 && absvalue < 10 && ifcontain && ifread)
						|| (type.equals("x") && absvalue >= 0.9 && absvalue < 10 && ifread && (commenFuncs.computeWordsLen(word) >= 3 || (ifcontain && ifread)))) {// || title.contains(word)
					if (commenFuncs.computeWordsLen(word) <= 7 && !resultList.contains(word) && word.length() >= 2) {
						if (commenFuncs.computeWordsLen(word) >= 5 && longCount >= 2) {
						} else {
							resultList.add(word);
						}
					}
					if (resultList.size() >= 4)
						break;
				}
			}
		if (tempC1Set != null)
			resultList.addAll(tempC1Set);
		if (tempC0Set != null)
			resultList.addAll(tempC0Set);
		if (resultList.size() >= 4) {
			List<String> tempList = resultList.subList(0, 4);
			LOG.info("tempList is " + tempList);
			if (tempList != null)
				backList.addAll(tempList);
		} else if (resultList != null)
			backList.addAll(resultList);
		if (relatedTag != null && !relatedTag.isEmpty() && !relatedTag.equals(""))
			backList.add(relatedTag);
		else if (sc != null)
			backList.add(sc);
		else if (c1 != null)
			backList.add(c1);
		else if (c0 != null)
			backList.add(c0);
		else
			backList.add("");
		if (backList != null && backList.size() >= 1)
			return backList;
		else
			return null;
	}

	/**
	 * 传入当前的item，命名为itemNow。 使用分词后的title，从solr中进行查询，找到相似度最大的top100（最大值），倒排item
	 * list 使用当前itemnow与itemlist中的item进行对比
	 * 
	 * @param s_title
	 *            已经分词的title
	 * @throws IOException
	 */
	private static itemf solrCheck(itemf itemNow) throws IOException// synchronized
	{
		if (itemNow == null)
			return itemNow;
		String solr_title = itemNow.getSplitTitle();
		if (solr_title == null) {
			LOG.info("solr_title is null.");
		} else
			LOG.info("solr_title is " + solr_title.trim() + "\n");
		ArrayList<itemf> itemList = null;
		try {
			itemList = indexOp.queryFromSolr(itemNow);
		} catch (Exception e) {
			LOG.error("[ERROR] Get itemlist from Solr. Title is " + solr_title, e);
			return itemNow;
		}
		if (itemList == null)
			LOG.info("itemList is null.");
		else
			LOG.info("itemList size is " + itemList.size());
		// 存储内容抄袭检测的结果
		List<Double> docSimList; // = new ArrayList<Double>();
		docSimList = DetectExtrmSimDoc.getSimForTarget(itemList, itemNow);
		if (docSimList != null)
			LOG.info("docSimList size is " + docSimList.size());
		else {
			LOG.info("docSimList is null.");
		}
		ArrayList<String> featureNow = itemNow.getFeatures();
		ArrayList<String> featureEnd = itemNow.getFeatures();
		LOG.info("The original feature is " + featureNow);
		// 对相似度超过一定阈值的item进行处理
		if(docSimList!=null)
		for (int k = 0; k < docSimList.size(); k++) {
			if (Math.abs(docSimList.get(k)) > 0.4) {
				// 取出要处理的item
				itemf itemSaved = itemList.get(k);
				LOG.info("similar item is " + itemSaved.getID() + "\t" + itemSaved.getTitle() + "\tdocSim=" + docSimList.get(k) + "\n");
				// 需要合并的item的featureList
				ArrayList<String> featureList = itemSaved.getFeatures();
				// 把旧的item feature合并到新item中
				if (docSimList.get(k) != 0.5)
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
		return itemNow;
	}

	public static void main(String[] args) throws IOException {
		QuerySimilar query = QuerySimilar.getInstance();
		query.test();
//		List<String> featureList = query.querySimilar(null, "外交部：坚决反对蔡英文赴日活动",//
//				"http://i.ifeng.com/news?aid=101633925",//
//				"新华社9月25日消息据报道，蔡英文将于10月上旬访问日本。外交部发言人洪磊25日表示，我们对蔡英文将赴日本活动表示严重关切和坚决反对，希望日方坚持一个中国原则，恪守在台湾问题上对中方所做承诺，不给任何人以任何名义或借口散布“台独”言论提供空间。(记者靳若成)", "", "", "新华社");
//		System.out.println(featureList);
	}
}
