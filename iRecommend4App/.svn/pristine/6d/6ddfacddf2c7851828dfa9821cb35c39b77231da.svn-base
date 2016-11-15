package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.ifeng.commen.Utils.LoadConfig;

/**
 * <PRE>
 * 作用 : 
 *   词的权重查询接口
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
 *          1.0          2015年7月8日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class wordWeightQueryInterface {
	private static final Log log = LogFactory.getLog(wordWeightQueryInterface.class);

	private Map<String, Double> CACHE_WORDWEIGHT;
	private static final String WORDWEIGHT_DATA_Path = LoadConfig.lookUpValueByKey("WordWeightPath");

	/**
	 * 内部类，用于实现lazy机制
	 */
	private static class SingletonHolder {
		private static wordWeightQueryInterface instance = new wordWeightQueryInterface();
	}

	private wordWeightQueryInterface() {
		this.CACHE_WORDWEIGHT = new HashMap<String, Double>();
		log.info("Loading word weight data from file...");
		List<String> tfidf_word = new ArrayList<String>();
		try {
			tfidf_word = readDocs(WORDWEIGHT_DATA_Path);
		} catch (IOException ex) {
			log.error("word weight file read error", ex);
		}

		for (String tempWord : tfidf_word) {
			if (2 != tempWord.split("#").length) {
				continue;
			}
			String key = tempWord.split("#")[0];
			double value = Double.valueOf(tempWord.split("#")[1]);
			this.CACHE_WORDWEIGHT.put(key, value);
		}

		log.info("Loading word weight data from file end");
	}
	
	public double queryWordWeightValue(String key)
	{
		if (key == null || key.isEmpty())
			return 0.0;

		// 默认值(中位数)
		double value = 0.5937;
		
		try
		{
			value = SingletonHolder.instance.CACHE_WORDWEIGHT.get(key);
			// 如果长度为2，且weight值很低，应该让其更低
			if (key.length() <= 2 && value < 0.45) {
				value = 0.35;
			}
		} catch (Exception e) {
			//log.error("There is no cache for key:" + key);
			//value = 6.0;
		}
		
		return value;
	}

	/**
	 * 获取单例对象实例
	 * 
	 * @return 单例对象
	 */
	public static wordWeightQueryInterface getInstance() {
		return SingletonHolder.instance;
	}
	
	
	private static List<String> readDocs(String inputFileDir) throws IOException {
		return Files.readLines(new File(inputFileDir), Charsets.UTF_8);
	}
	
	public static void main(String[] args) {
		System.out.println(wordWeightQueryInterface.getInstance().queryWordWeightValue("散户"));
	}

}
