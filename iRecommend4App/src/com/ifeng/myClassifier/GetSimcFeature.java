package com.ifeng.myClassifier;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class GetSimcFeature {
	static Logger LOG = Logger.getLogger(GetSimcFeature.class);
	// 分类算法引擎
	static simCla simc = simCla.getInstance();
	/**
	 * 根据文章内容进行分类计算并返回分类结果
	 * @param sc
	 * @param s_title
	 * @param source
	 * @param featureList
	 * @return
	 */
	public static ArrayList<String> ClassifyByWord(String s_title, String source, ArrayList<String> featureList) {
		ArrayList<String> al_words = new ArrayList<String>();
		if (source != null) {
			al_words.add(source);
			al_words.add("s");
			al_words.add("1");
		} else {
			al_words.add("unknown");
			al_words.add("s");
			al_words.add("1");
		}
		if (featureList != null)
			al_words.addAll(featureList);
		LOG.info("feature for simc is " + al_words);
		long simcTime = System.currentTimeMillis();
		ArrayList<String> reList = simc.classify(al_words, s_title);
		LOG.info("[TIME] simcTime is " + (System.currentTimeMillis() - simcTime));
		if (reList == null || reList.isEmpty())
			return null;
		ArrayList<String> resultList = new ArrayList<String>();
		for(int i = 0; i < reList.size() - 2; i+=3)
		{
			if(resultList.contains(reList.get(i)))
			{
				int  wordIndex = resultList.indexOf(reList.get(i));
				resultList.set(wordIndex+2, reList.get(i+2));
			}
			else
			{
				resultList.add(reList.get(i));
				resultList.add(reList.get(i+1));
				resultList.add(reList.get(i+2));
			}
		}
		resultList = simclaResultMap(resultList);
		return resultList;
	}
	
	/**
	 * 用于修正simc的结果以符合新的分类体系
	 * @param featureList
	 * @return
	 */
	 private static ArrayList<String> simclaResultMap(ArrayList<String> featureList){
		if (featureList==null || featureList.isEmpty()) {
			return featureList;
		}
		for (int i = 0; i < featureList.size(); i+=3) {
			String s=featureList.get(i);
			String weight=featureList.get(i+2);
			if (s.equals("财经")||s.equals("时尚")) {
				weight=String.valueOf(Math.abs(Double.parseDouble(weight)));
				featureList.set(i+2,weight);
			}
			else if(s.equals("保险")||s.equals("股市")||s.equals("宏观经济")||s.equals("黄金")||s.equals("基金")
					||s.equals("理财")||s.equals("期货")||s.equals("商业")||s.equals("银行")||s.equals("美体")
					||s.equals("时装")||s.equals("美容")||s.equals("奢侈品")||s.equals("街拍")||s.equals("战争历史")
					||s.equals("互联网金融")||s.equals("移动互联网")){
				featureList.set(i+1,"sc");
			}
			else if (s.equals("财经评论")) {
				featureList.set(i+1, "cn");
			}
			else if (s.equals("教育资讯")) {
				featureList.set(i, "教育");
				featureList.set(i+2,"-"+weight);
			}
			else if (s.equals("社会资讯")) {
				featureList.set(i, "社会八卦");
			}
			else if (s.equals("公益活动")) {
				featureList.set(i, "公益");
			}
			else if (s.equals("探索发现")) {
				featureList.set(i, "科学探索");
			}
		}
		return featureList;
	}
}
