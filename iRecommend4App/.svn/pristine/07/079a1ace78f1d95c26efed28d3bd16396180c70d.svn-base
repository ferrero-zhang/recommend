/**
 * 
 */
package com.ifeng.iRecommend.topicmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

/**
 * <PRE>
 * 作用 : 
 *   加载主题模型，提供主题模型计算接口
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
 *          1.0          2013-7-15        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class topicCmpInterface {
	
	// doc_id table
	public static HashMap<String, Integer> docidTable = null;
	// word_id table
	public static HashMap<String, Integer> wordidTable = null;

	// tm
	public static TopicMatrix tm_doctopic = null;
	// tm
	public static TopicMatrix tm_wordtopic = null;

	
	private static topicCmpInterface instance = new topicCmpInterface();
	
	
	private topicCmpInterface(){
//		// 读入doc topicmatrix,word topicmatrix,docid table,wordid table;
//		tm_doctopic = TopicMatrixReader.ReadInDir(
//				new File(fieldDicts.tm_doc_dir), fieldDicts.tm_matrixA_size, fieldDicts.tm_topic_num);

		tm_wordtopic = TopicMatrixReader.ReadInDir(
				new File(fieldDicts.tm_word_dir), fieldDicts.tm_matrixB_size, fieldDicts.tm_topic_num);
		// word_id table
		wordidTable = new HashMap<String, Integer>();
		FileUtil wordidFile = new FileUtil();
		wordidFile.Initialize(fieldDicts.tm_words_file, "UTF-8");
		String line = "";
		int wordid = 0;
		while ((line = wordidFile.ReadLine()) != null) {
			wordidTable.put(line.trim(), wordid++);
		}
	}
	
	public static topicCmpInterface getInstance(){
		return instance;
	}
	
	/**
	 * 计算item的topic vector
	 * 
	 * @param hm_words
	 *            item的词-权重列表；
	 * @return topic vector
	 */
	public double[] cmpItemTopicVector(HashMap<String, Float> hm_words) {
		if(hm_words == null)
			return null;
		// 计算词频信息
		Map<Integer, Float> mp_wordcount = new HashMap<Integer, Float>();
		Iterator<Entry<String, Float>> it = hm_words.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<String, Float> et = it.next();
			String word = et.getKey();
			Integer wordid = wordidTable.get(word);
			if (wordid == null)
				continue;
			mp_wordcount.put(wordid,et.getValue());
		}

		double[] lTopics = new double[tm_wordtopic.getTopicNum()];
		double tSum = 0.0;
		for (Entry<Integer, Float> entry : mp_wordcount.entrySet()) {
			for (int i = 0; i < lTopics.length; ++i) {
				double tmp = tm_wordtopic.getTopicVector(entry.getKey())[i]
						* entry.getValue();
				lTopics[i] += tmp;
				tSum += tmp;
			}
		}

		for (int i = 0; i < lTopics.length; ++i)
			lTopics[i] /= tSum;

		return lTopics;

	}

}
