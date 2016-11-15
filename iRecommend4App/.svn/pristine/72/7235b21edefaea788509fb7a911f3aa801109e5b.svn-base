package com.ifeng.myClassifier;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.ifeng.commen.Utils.LoadConfig;


public class NaiveBayes {
	private final static Logger log = Logger.getLogger(NaiveBayes.class);
	
	private static String word_Bayes_tf = LoadConfig
			.lookUpValueByKey("word_Bayes_tf");  //Bayes 词频率分布
	private static String class_stat = LoadConfig
			.lookUpValueByKey("class_stat");  //Bayes 类统计
	
	private Map<String, Double> classPropMap = new HashMap<String, Double>(30);
	private Map<String, Integer> classCountMap = new HashMap<String, Integer>(30);
	private Map<String, Map<String, Double>> wordClassPropMap = new HashMap<String, Map<String, Double>>(300000);
//	private Map<String, Map<String, Double>> wordClassFSMap;

	public static NaiveBayes instance = new NaiveBayes(class_stat, word_Bayes_tf);

	public static NaiveBayes getInstance() {
		return instance;
	}
	
	private NaiveBayes (String classPropMapFile, String wordPropFile) {
		BufferedReader rd = null;

		//load class statistics value file into memory
		try {
			rd = new BufferedReader(new InputStreamReader(new FileInputStream(
					classPropMapFile), "UTF-8"));
			String line = null;
		
			while ((line = rd.readLine()) != null && !line.isEmpty()) {
				String[] oneClass = line.split("\t");
				String className = oneClass[0];
				double prop = Double.parseDouble(oneClass[1]);
				int count = Integer.parseInt(oneClass[2]);
				classPropMap.put(className, prop);
				classCountMap.put(className, count);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (rd != null) {
				try {
					rd.close();
					log.info("Handled " + classPropMapFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		rd = null;
		try {
			rd = new BufferedReader(new InputStreamReader(new FileInputStream(
					wordPropFile), "UTF8"));
			String line = null;
			while ((line = rd.readLine()) != null && !line.isEmpty()) {
				String[] allWords = line.split("\t");
				Map<String, Double> classMap = new HashMap<String, Double>(allWords.length/2);
				
				for(int i = 1; i < allWords.length; i+= 2){
					classMap.put(allWords[i], Double.parseDouble(allWords[i + 1]));
				}
				wordClassPropMap.put(allWords[0], classMap);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (rd != null) {
				try {
					rd.close();
					log.info("Handled " + wordPropFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * @param featureList 第一位是稿源
	 * @return
	 */
	public String[] classify(ArrayList<String> featureList) {
//		ArrayList<String> featureList = item.getFeatures();
		if(featureList == null || featureList.isEmpty() || featureList.size() %3 != 0){
			return null;
		}
		//loop every feature to add up all words' class
		Map<String, Double> resultPropMap = new HashMap<String, Double>();
//		Map<String, Double> resultVoteMap = new HashMap<String, Double>();
		for (int k = 0; k < featureList.size(); k += 3) {
			String label = featureList.get(k);
			String labelType = featureList.get(k + 1);
			String weight = featureList.get(k + 2);
			double w = Double.valueOf(weight);
			w = Math.abs(w);
			
			if(labelType.contains("c"))
				continue;
			
			//handle source or s1
			if(labelType.equals("s"))
				label = label + "_s";
			else if(labelType.equals("s1"))
				label = label + "_s1";
			
			Map<String, Double> candidatePropMap = wordClassPropMap.get(label);
//			Map<String, Double> FSPropMap = wordClassFSMap.get(label);

			if(candidatePropMap == null)
				continue;
//			System.out.println(label);
//			System.out.println(candidatePropMap);
			for(String c : classPropMap.keySet()){
				Double prop = candidatePropMap.get(c); 
				double fsProp = 0.1;
//				System.out.println(1/ (classPropMap.get(c) * total));
				if(prop == null)
					prop = 0.0;
//				if(c.equals("历史")){
//					System.out.print(label + " == ");
//					System.out.println(prop);
//				}
//				if(wordClassFSMap.get(label).get(c) != null)
//					fsProp = wordClassFSMap.get(label).get(c);
				double currentProp = (1 + prop) / (classCountMap.get(c) + wordClassPropMap.size());//贝叶斯公式
				currentProp = Math.log(currentProp);
				Double origProp = resultPropMap.get(c);
				if(origProp == null)
					resultPropMap.put(c, currentProp);
				else
					resultPropMap.put(c, currentProp +  origProp);
				
//				Double origVote = resultVoteMap.get(c);
//				if(origVote == null)
//					resultVoteMap.put(c, 1.0);
//				else
//					resultVoteMap.put(c, origVote + 1);
			}
		}
		
		//addition to every class probability
		for(Entry<String, Double> entry : resultPropMap.entrySet()){
			String clazz = entry.getKey();
			Double prop = classPropMap.get(clazz);
			if(prop == null){
				log.error("CHECK CLASS PROP FILE!!");
				continue;
			}
			prop = Math.log(prop);
			prop += entry.getValue();
			entry.setValue(prop);
		}
		
		StringBuffer sb = new StringBuffer();
		StringBuffer sbAll = new StringBuffer();	
		ArrayList<String> candidateClass = new ArrayList<String>(2);//最多2个候选类
//		for( int i = 0 ; i < candidateClass.size ; i ++) { //Initialize the candidates of classifying result
//			candidateClass[i] = "";
//		}
		
		//sort resultMap as values
		ArrayList<Entry<String, Double>> maplist = new ArrayList(resultPropMap.entrySet());
		Comparator<Entry<String, Double>> cmprt = new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> arg0,
					Entry<String, Double> arg1) {
				// TODO Auto-generated method stub
				Double result = arg0.getValue() - arg1.getValue();
				if (result > 0)
					return -1;
				else if (result == 0)
					return 0;
				else
					return 1;
			}
		};
		Collections.sort(maplist, cmprt);
		if(maplist.isEmpty())
			return null;
		
		for(int i = 0; i < maplist.size(); i++){
			Entry<String, Double> result = maplist.get(i);
			sbAll.append(result.getKey()).append("=").append(result.getValue()).append(",");
		}
		sbAll.append("\n");
	
//		ArrayList<Entry<String, Double>>maplist1 = new ArrayList(resultVoteMap.entrySet());
//		Collections.sort(maplist1, cmprt);
//	
//		for(int i = 0; i < maplist1.size(); i++){
//			Entry<String, Double> resultEntry = maplist1.get(i);
//			sbAll.append(resultEntry.getKey()).append("=").append(resultEntry.getValue()).append(",");
//		}
//		sbAll.append("\n");
		//选取候选分类的规则
//		double minVote = 5;
		Entry<String, Double> resultEntry = maplist.get(0);
		double ratio = 0.9;//线性加权概率离间程度不超过上一排位90%
		double propLowBound = -50;
		Double maxProp = resultEntry.getValue();
		if(maxProp > propLowBound)
			return null;
//		if(maxProp < minProp)
//			minVote = 10;
		for(int i = 0; i < Math.min(maplist.size(), 2); i++){ 
			resultEntry = maplist.get(i);
			Double prop = resultEntry.getValue();
			String clazz = resultEntry.getKey();
			prop = maxProp / prop;
			if(prop >= Math.pow(ratio, i)){
				candidateClass.add(resultEntry.getKey());
				sb.append(clazz).append("=").append(resultPropMap.get(clazz)).append(", ");
			}
//			resultEntry.setValue(-prop);
		}
		log.info(sb + "\n");
		//投票过滤
		/*for(int i = 0; i < candidateClass.length; i++){
			Double voteNum = resultVoteMap.get(candidateClass[i]);
			if(voteNum == null || voteNum < minVote){
				candidateClass[i] = "";
			}
		}*/
		
//		ArrayList<String> result = new ArrayList<String>(candidateClass.length);
//		for(int i = 0; i < candidateClass.length; i++){
//			if(candidateClass[i].isEmpty())
//				continue;
//			String clazz = candidateClass[i];
//			sb.append(clazz).append("=").append(resultPropMap.get(clazz)).append("\n");
//			result.add(clazz);
//			result.add("c");
//			result.add(resultPropMap.get(clazz).toString());
//		}
		
	
//		System.out.println(sbAll);
//		fw.write(sbAll.toString());
//		try {
//			fw.write(sb.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
//		Entry<String, Double> result = maplist.get(0);
//		if(result.getValue() < 0.1)
//			return null;
//		String[] classifyResult = new String[2];
//		classifyResult[0] = result.getKey();
//		classifyResult[1] = result.getValue().toString();
		
		return candidateClass.toArray(new String[candidateClass.size()]);
	}
	
	public static void main(String[] args) {
		BufferedReader rd = null;
		FileWriter resultWriter = null;
		
		NaiveBayes ob = NaiveBayes.getInstance();
		try {
			rd = new BufferedReader(new InputStreamReader(new FileInputStream(
					"D:\\classify\\new\\抄袭检测实验\\knnLabelledTest2"), "UTF8"));
			String line = null;
			resultWriter = new FileWriter("D:\\classify\\new\\抄袭检测实验\\result_0229_NB",
					false);
			while ((line = rd.readLine()) != null && !line.isEmpty()) {
				String[] test = line.split("\t");
//				experiment(resultWriter,test, ob);
//				 try {
//					Thread.sleep(20);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (rd != null) {
				try {
					rd.close();
					resultWriter.flush();
					resultWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
