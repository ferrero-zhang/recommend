package com.ifeng.iRecommend.featureEngineering;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.hexl.redis.GetUsefulKeyFromRedis;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.WordReadData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.WordReadData.WordInfo;

public class ExtractTags {
	static Logger LOG = Logger.getLogger(ExtractTags.class);
	static WordReadData wordReadData = WordReadData.getInstance();
	// 特征的类型
	private static String originalPath = LoadConfig.lookUpValueByKey("originalPath");
	private static HashSet<String> blackList = new HashSet<String>();

	ExtractTags() {
		try {
			readBlackList();
		} catch (Exception e) {
			LOG.error("[ERROR]Load blackList failed.", e);
		}
	}

	private static ExtractTags instance = new ExtractTags();

	public static ExtractTags getInstance() {
		return instance;
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

	public List<String> extract(String logstr, String s_title, ArrayList<String> originalList) {
		if (originalList == null)
			return null;
		ArrayList<String> featureList = originalList;
		LOG.info(logstr + " featureList before read and useful check is " + featureList);
		// 对初始的list进行清洗,形成一个二阶map
		HashMap<String, HashMap<String, Double>> tagsMap = SelectReadAndUseful(featureList);
		LOG.info(logstr + " featureList after read and useful check is "+SystemOutMap(tagsMap));
		if (tagsMap == null || tagsMap.isEmpty())
			return null;
		// 分拆tagsMap，对于人工tag和抽取tag分开处理
		HashMap<String, HashMap<String, Double>> workedMap = new HashMap<String, HashMap<String, Double>>();
		if (tagsMap.containsKey("c"))
			workedMap.put("c", tagsMap.remove("c"));
		if (tagsMap.containsKey("sc"))
			workedMap.put("sc", tagsMap.remove("sc"));
		if(tagsMap.containsKey("cn") && tagsMap.containsKey("e"))
		{
			boolean cnAnde = false;
			for(Entry<String,Double> cnentry : tagsMap.get("cn").entrySet())
			{
				for(Entry<String,Double> eentry : tagsMap.get("e").entrySet())
				{
					if(commenFuncs.simRate(cnentry.getKey(), eentry.getKey()) < 0.7)
						cnAnde = true;
				}
			}
			if(cnAnde)
			{
				workedMap.put("cn", tagsMap.remove("cn"));
				workedMap.put("e", tagsMap.remove("e"));
			}
			else
				workedMap.put("e", tagsMap.remove("e"));
		}
		else if (tagsMap.containsKey("cn"))
			workedMap.put("cn", tagsMap.remove("cn"));
		else if (tagsMap.containsKey("e"))
			workedMap.put("e", tagsMap.remove("e"));
		if (tagsMap.containsKey("s"))
			workedMap.put("s", tagsMap.remove("s"));
		if (tagsMap.containsKey("s1"))
			workedMap.put("s1", tagsMap.remove("s1"));
		if (tagsMap.containsKey("loc"))
			workedMap.put("loc", tagsMap.remove("loc"));
		tagsMap = readableWeight(tagsMap);
//		System.out.println(SystemOutMap(tagsMap));
		tagsMap = titleWeight(tagsMap, s_title);
//		System.out.println(SystemOutMap(tagsMap));
		tagsMap = longWeight(tagsMap);
//		System.out.println(SystemOutMap(tagsMap));
		List<String> tagList = new ArrayList<String>();
		tagList = selectResult(tagsMap, workedMap);
		return tagList;
	}
	public List<String> SystemOutMap(HashMap<String, HashMap<String, Double>> tagsMap){
		if(tagsMap == null)
			return null;
		List<String> list = new ArrayList<String>();
		for(Entry<String, HashMap<String,Double>> entry : tagsMap.entrySet())
		{
			String type = entry.getKey();
			for(Entry<String,Double> en : entry.getValue().entrySet())
			{
				list.add(en.getKey());
				list.add(type);
				list.add(String.valueOf(en.getValue()));
			}
		}
		return list;
	}
	
//	@Test
	public static void main(String[] args) throws IOException {
		ExtractTags ext = ExtractTags.getInstance();
		IKVOperationv2 op = new IKVOperationv2("appitemdb");
		// FileWriter fw = new FileWriter("D:\\data\\tagsTestResult",true);
		// for(int i = 3000000; i < 3500000; i++)
		// {
		 String key = String.valueOf("4200491");
		 String type = "c";
		 itemf item = op.queryItemF(key, type);
		// if(item == null)
		// continue;
		 String title = item.getTitle();
		 String s_title = item.getSplitTitle();
		 ArrayList<String> list = item.getFeatures();
		// long time = System.currentTimeMillis();
		// fw.write(key+"##"+title+"##"+list+"##"+extract(key,s_title,list)+"##"+(System.currentTimeMillis()
		// - time)+"\n");
		System.out.println( ext.extract(key,s_title,list));
		// System.out.println(key);
		// }
		// fw.flush();
		// fw.close();
//		ArrayList<String> list = new ArrayList<String>();
//		list.add("上传");
//		list.add("x");
//		list.add("1");
//		list.add("欢乐颂");
//		list.add("x");
//		list.add("1");
//		list.add("孔明");
//		list.add("x");
//		list.add("1");
//		list.add("绝色双娇");
//		list.add("x");
//		list.add("1");
//		list.add("回应");
//		list.add("x");
//		list.add("1");
//		System.out.println(list);
//		for(int i = 0; i < list.size() - 2; i +=3)
//		{
//			System.out.println(!wordReadData.getWordReadMap().containsKey(list.get(i)));
//			System.out.println(list.get(i + 1).equals("x"));
//			System.out.println(commenFuncs.computeWordsLen(list.get(i)) <= 2);
//			System.out.println(!wordReadData.getWordReadMap().containsKey(list.get(i))&& list.get(i + 1).equals("x")&&commenFuncs.computeWordsLen(list.get(i)) <= 2);
//			System.out.println(commenFuncs.cmpWordLen(list.get(i)) <= 1
//					|| commenFuncs.cmpWordLen(list.get(i)) > 7
//					|| (list.get(i + 1).equals("c") && Double.valueOf(list.get(i + 2)) < 0)
//					|| Math.abs(Double.valueOf(list.get(i + 2))) <= 0.2
//					|| GetUsefulKeyFromRedis.GetUsefulFlag(list.get(i)) != 1
//					|| (wordReadData.getWordReadMap().containsKey(list.get(i)) && !wordReadData.getWordReadMap().get(list.get(i)).isRead())
//					|| (!wordReadData.getWordReadMap().containsKey(list.get(i)) && list.get(i + 1).equals("x") && commenFuncs.cmpWordLen(list.get(i)) <= 2)
//					|| (!wordReadData.getWordReadMap().containsKey(list.get(i)) && (list.get(i + 1).equals("nr")
//							|| list.get(i + 1).equals("ne") || list.get(i + 1).equals("ns") || list.get(i + 1).equals("nx") || list.get(i + 1).equals("nz") || list.get(i + 1).equals("k")
//							|| list.get(i + 1).equals("n") || list.get(i + 1).equals("nt")))
//							);
//			System.out.println();
//		}
		
//		HashMap<String, HashMap<String, Double>> map = ext.SelectReadAndUseful(list);
//		for (Entry<String, HashMap<String, Double>> entry : map.entrySet()) {
//			System.out.println(entry.getKey());
//			for (Entry<String, Double> en : entry.getValue().entrySet())
//				System.out.println(en.getKey());
//		}

	}

	private List<String> selectResult(HashMap<String, HashMap<String, Double>> tagsMap, HashMap<String, HashMap<String, Double>> workedMap) {
		if (tagsMap == null && workedMap == null)
			return null;
		List<String> resultList = new ArrayList<String>();
		List<String> otherWorkedList = new ArrayList<String>();
		if (workedMap != null && workedMap.containsKey("e")) {
			boolean goon = true;
			HashMap<String, Double> eMap = workedMap.get("e");
			if (eMap != null && !eMap.isEmpty())
				for (Entry<String, Double> en : eMap.entrySet())
					if (en.getValue() >= 0.5) {
						if (goon) {
							resultList.add(en.getKey());
							goon = false;
						}
						// otherWorkedList.add(en.getKey());
					}
		}
		if (workedMap != null && workedMap.containsKey("loc")) {
			boolean goon = true;
			HashMap<String, Double> locMap = workedMap.get("loc");
			if (locMap != null && !locMap.isEmpty())
				for (Entry<String, Double> en : locMap.entrySet())
					if (en.getValue() >= 0.5) {
						if (goon) {
							resultList.add(en.getKey());
							goon = false;
						} else
							otherWorkedList.add(en.getKey());
					}
		}
		if (workedMap != null && workedMap.containsKey("cn")) {
			boolean goon = true;
			HashMap<String, Double> cnMap = workedMap.get("cn");
			List<Map.Entry<String, Double>> cnList = mapSort(cnMap);
			if (cnList != null && !cnList.isEmpty())
				for (Entry<String, Double> en : cnList)
					if (en.getValue() >= 0.5) {
						if (goon) {
							resultList.add(en.getKey());
							goon = false;
						} else
							otherWorkedList.add(en.getKey());
					}
		}
		// et x kb ks kq的处理
		HashMap<String, Double> wordMap = new HashMap<String, Double>();
		if (tagsMap != null && tagsMap.containsKey("et")) {
			HashMap<String, Double> etMap = tagsMap.remove("et");
			if (etMap != null && !etMap.isEmpty())
				for (Entry<String, Double> en : etMap.entrySet())
					wordMap.put(en.getKey(), en.getValue());
		}
		if (tagsMap != null && tagsMap.containsKey("x")) {
			HashMap<String, Double> xMap = tagsMap.remove("x");
			if (xMap != null && !xMap.isEmpty())
				for (Entry<String, Double> en : xMap.entrySet())
					wordMap.put(en.getKey(), en.getValue());
		}
		if (tagsMap != null && tagsMap.containsKey("kb")) {
			HashMap<String, Double> kbMap = tagsMap.remove("kb");
			if (kbMap != null && !kbMap.isEmpty())
				for (Entry<String, Double> en : kbMap.entrySet())
					wordMap.put(en.getKey(), en.getValue());
		}
		if (tagsMap != null && tagsMap.containsKey("ks")) {
			HashMap<String, Double> ksMap = tagsMap.remove("ks");
			if (ksMap != null && !ksMap.isEmpty())
				for (Entry<String, Double> en : ksMap.entrySet())
					wordMap.put(en.getKey(), en.getValue());
		}
		if (tagsMap != null && tagsMap.containsKey("kq")) {
			HashMap<String, Double> kqMap = tagsMap.remove("kq");
			if (kqMap != null && !kqMap.isEmpty())
				for (Entry<String, Double> en : kqMap.entrySet())
					wordMap.put(en.getKey(), en.getValue());
		}
		List<String> words = new ArrayList<String>();
		List<String> otherwords = new ArrayList<String>();
		List<Map.Entry<String, Double>> wordList = mapSort(wordMap);
		if (wordList != null)
			for (int i = 0; i < wordList.size(); i++) {
				if (wordList.get(i).getValue() >= 0.5 && words.size() <= 4)
					words.add(wordList.get(i).getKey());
				else
					otherwords.add(wordList.get(i).getKey());
			}
		// System.out.println("otherwords "+otherwords);
		resultList.addAll(words);
		// nr nz nt n k 的处理
		HashMap<String, Double> otherMap = new HashMap<String, Double>();
		if (tagsMap != null)
			for (Entry<String, HashMap<String, Double>> entry : tagsMap.entrySet()) {
				for (Entry<String, Double> en : entry.getValue().entrySet()) {
					otherMap.put(en.getKey(), en.getValue());
				}
			}
		List<Map.Entry<String, Double>> otherList = mapSort(otherMap);
		List<String> othertags = new ArrayList<String>();
		List<String> uselesstags = new ArrayList<String>();
		if (otherList != null)
			for (int i = 0; i < otherList.size(); i++) {
				if (otherList.get(i).getValue() >= 0.9 && othertags.size() <= 4)
					othertags.add(otherList.get(i).getKey());
				else
					uselesstags.add(otherList.get(i).getKey());
			}
		resultList.addAll(othertags);
		if (workedMap != null && workedMap.containsKey("s")) {
			boolean goon = true;
			HashMap<String, Double> eMap = workedMap.get("s");
			for (Entry<String, Double> en : eMap.entrySet())
				if (en.getValue() >= 0.5) {
					if (goon) {
						resultList.add(en.getKey());
						goon = false;
					}
					// otherWorkedList.add(en.getKey());
				}
		}
		if (workedMap != null && workedMap.containsKey("s1")) {
			boolean goon = true;
			HashMap<String, Double> eMap = workedMap.get("s1");
			for (Entry<String, Double> en : eMap.entrySet())
				if (en.getValue() >= 0.5) {
					if (goon) {
						resultList.add(en.getKey());
						goon = false;
					}
					// otherWorkedList.add(en.getKey());
				}
		}
		if (workedMap != null && workedMap.containsKey("sc")) {
			boolean goon = true;
			HashMap<String, Double> scMap = workedMap.get("sc");
			if (scMap != null && !scMap.isEmpty())
				for (Entry<String, Double> en : scMap.entrySet())
					if (en.getValue() >= 0.5) {
						if (goon) {
							resultList.add(en.getKey());
							goon = false;
						} else
							otherWorkedList.add(en.getKey());
					}
		}
		if (workedMap != null && workedMap.containsKey("c")) {
			boolean goon = true;
			HashMap<String, Double> cMap = workedMap.get("c");
			if (cMap != null && !cMap.isEmpty())
				for (Entry<String, Double> en : cMap.entrySet())
					if (en.getValue() >= 0.5) {
						if (goon) {
							resultList.add(en.getKey());
							goon = false;
						} else
							otherWorkedList.add(en.getKey());
					}
		}
		resultList.addAll(otherWorkedList);
		if (resultList.size() > 4)
			resultList = resultList.subList(0, 4);
		return resultList;
	}

	/**
	 * 可读，订阅加权
	 * 
	 * @param tagsMap
	 * @return
	 */
	private HashMap<String, HashMap<String, Double>> readableWeight(HashMap<String, HashMap<String, Double>> tagsMap) {
		if (tagsMap == null || tagsMap.isEmpty())
			return null;
		for (Entry<String, HashMap<String, Double>> entry : tagsMap.entrySet()) {
			HashMap<String, Double> innerMap = entry.getValue();
			for (Entry<String, Double> en : innerMap.entrySet()) {
				if (wordReadData.getWordReadMap().containsKey(en.getKey())) {
					WordInfo info = wordReadData.getWordReadMap().get(en.getKey());
					double value = en.getValue();
					if (info.isRead()) {
						value += 0.3;
						int clientCount = info.getClientSubWordCount();
						int otherCount = info.getOtherSouceCount();
						if (clientCount > 10)
							value += 0.1;
						if (otherCount >= 50)
							value += 0.1;
						innerMap.put(en.getKey(), value);
					}
				}
			}
			tagsMap.put(entry.getKey(), innerMap);
		}
		return tagsMap;
	}

	/**
	 * 词长度权重
	 * 
	 * @param tagsMap
	 * @return
	 */
	private HashMap<String, HashMap<String, Double>> longWeight(HashMap<String, HashMap<String, Double>> tagsMap) {
		if (tagsMap == null || tagsMap.isEmpty())
			return null;
		for (Entry<String, HashMap<String, Double>> entry : tagsMap.entrySet()) {
			HashMap<String, Double> innerMap = entry.getValue();
			for (Entry<String, Double> en : innerMap.entrySet()) {
				if (Math.abs(commenFuncs.cmpWordLen(en.getKey()) - 4) == 1) {
					innerMap.put(en.getKey(), en.getValue() + 0.1);
				} else if (commenFuncs.cmpWordLen(en.getKey()) == 4) {
					innerMap.put(en.getKey(), en.getValue() + 0.2);
				}
			}
			tagsMap.put(entry.getKey(), innerMap);
		}
		return tagsMap;
	}

	/**
	 * 
	 * @param tagsMap
	 * @param s_title
	 * @return
	 */
	private HashMap<String, HashMap<String, Double>> titleWeight(HashMap<String, HashMap<String, Double>> tagsMap, String s_title) {
		if (tagsMap == null || tagsMap.isEmpty())
			return null;
		for (Entry<String, HashMap<String, Double>> entry : tagsMap.entrySet()) {
			HashMap<String, Double> innerMap = entry.getValue();
			for (Entry<String, Double> en : innerMap.entrySet()) {
				if (s_title.contains(en.getKey())) {
					innerMap.put(en.getKey(), en.getValue() + 0.5);
				}
			}
			tagsMap.put(entry.getKey(), innerMap);
		}
		return tagsMap;
	}

	/**
	 * 删除规则： 1.全部的c0 2.权重小于等于0.2的词 3.不可用词 4.不可读词 5.1个字和超过7个字的
	 * 
	 * @param list
	 * @return
	 */
	public HashMap<String, HashMap<String, Double>> SelectReadAndUseful(ArrayList<String> list) {
		if (list == null || list.size() < 3)
			return null;
		ArrayList<String> featureList = new ArrayList<String>();
		HashMap<String, HashMap<String, Double>> resultMap = new HashMap<String, HashMap<String, Double>>();
		for (int i = 0; i < list.size() - 2; i += 3) {
			if (commenFuncs.cmpWordLen(list.get(i)) <= 1
					|| commenFuncs.cmpWordLen(list.get(i)) > 7
					|| (list.get(i + 1).equals("c") && Double.valueOf(list.get(i + 2)) < 0)
					|| Math.abs(Double.valueOf(list.get(i + 2))) <= 0.2
					|| GetUsefulKeyFromRedis.GetUsefulFlag(list.get(i)) != 1
					|| (wordReadData.getWordReadMap().containsKey(list.get(i).toLowerCase()) && !wordReadData.getWordReadMap().get(list.get(i).toLowerCase()).isRead())
					|| (!wordReadData.getWordReadMap().containsKey(list.get(i).toLowerCase()) && list.get(i + 1).equals("x") && commenFuncs.cmpWordLen(list.get(i)) <= 2)
					|| (!wordReadData.getWordReadMap().containsKey(list.get(i).toLowerCase()) && (list.get(i + 1).equals("nr")
							|| list.get(i + 1).equals("ne") || list.get(i + 1).equals("ns") || list.get(i + 1).equals("nx") || list.get(i + 1).equals("nz") || list.get(i + 1).equals("k")
							|| list.get(i + 1).equals("n") || list.get(i + 1).equals("nt")))) {
			} else {
				featureList.add(list.get(i));
				featureList.add(list.get(i+1));
				featureList.add(list.get(i+2));
			}
		}
		ArrayList<String> tempList = new ArrayList<String>();
		for(int i = 0; i < featureList.size() - 2; i+= 3)
		{
			boolean addflag = true;
			for(int j = 0; j < tempList.size() - 2; j+=3)
			{
				if(tempList.get(j+1).equals("loc") || tempList.get(j+1).equals("e"))
				{
					if(tempList.get(j).contains(featureList.get(i))||featureList.get(i).contains(tempList.get(j)))
					{
						addflag = false;
					}
				}
				else if(tempList.get(j+1).equals("c") || tempList.get(j+1).equals("sc"))
				{
					
				}
				else
				{
					if(tempList.get(j).contains(featureList.get(i))||featureList.get(i).contains(tempList.get(j)))
					{
						if(tempList.get(j).length() > featureList.get(i).length())
						{}
						else
							tempList.set(j, featureList.get(i));
						addflag = false;
					}
				}
			}
			if(addflag)
			{
				tempList.add(featureList.get(i));
				tempList.add(featureList.get(i+1));
				tempList.add(featureList.get(i+2));
			}
		}
		tempList = FeatureExTools.delSpareFea(tempList);
		for(int i = 0; i < tempList.size() - 2; i+=3)
		{
			if (resultMap.containsKey(tempList.get(i + 1))) {
				HashMap<String, Double> tagMap = resultMap.get(tempList.get(i + 1));
				// System.out.println(list.get(i));
				tagMap.put(tempList.get(i), Math.abs(Double.valueOf(tempList.get(i + 2))));
				resultMap.put(tempList.get(i + 1), tagMap);
			} else {
				HashMap<String, Double> tagMap = new HashMap<String, Double>();
				// System.out.println(list.get(i));
				tagMap.put(tempList.get(i), Math.abs(Double.valueOf(tempList.get(i + 2))));
				resultMap.put(tempList.get(i + 1), tagMap);
			}
		}
		
		return resultMap;
	}

	/**
	 * 利用比较器给map按照value值排序
	 * 
	 * @param map
	 * @return
	 */
	private List<Map.Entry<String, Double>> mapSort(Map<String, Double> map) {
		List<Map.Entry<String, Double>> mappingList = null;
		// 通过ArrayList构造函数把map.entrySet()转换成list
		mappingList = new ArrayList<Map.Entry<String, Double>>(map.entrySet());
		// 通过比较器实现比较排序
		Collections.sort(mappingList, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> mapping1, Map.Entry<String, Double> mapping2) {
				return mapping2.getValue().compareTo(mapping1.getValue());
			}
		});
		return mappingList;
	}

	/**
	 * 
	 * @param featureList
	 * @param source
	 * @param s_title
	 * @param s_content
	 * @return
	 */
	public ArrayList<String> featureClear(ArrayList<String> featureList, String s_title, String relatedTag, String logstr) {
		LOG.info(logstr + "[INFO] Feature for clear is " + featureList);
		LOG.info(logstr + "[INFO] Related tag for related id get is " + relatedTag);
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
		List<String> tagList = extract(logstr, s_title, originalList);
		if (tagList != null)
			backList.addAll(tagList);
		if (relatedTag != null && !relatedTag.isEmpty() && !relatedTag.equals(""))
			backList.add(relatedTag);
		else
			backList.add("");
		if (backList != null && backList.size() >= 1)
			return backList;
		else
			return null;
	}
}
