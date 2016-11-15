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
 *   基于内部类的单例模式 Lazy 线程安全
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
public class idfQueryInterface {
	private static final Log log = LogFactory.getLog(idfQueryInterface.class);

	private Map<String, Double> CACHE_TFIDF;
	private static final String TFIDF_DATA_Path = LoadConfig.lookUpValueByKey("WordIDFPath");

	/**
	 * 内部类，用于实现lazy机制
	 */
	private static class SingletonHolder {
		private static idfQueryInterface instance = new idfQueryInterface();
	}

	private idfQueryInterface() {
		this.CACHE_TFIDF = new HashMap<String, Double>();
		log.info("Loading tfidf data from file...");
		List<String> tfidf_word = new ArrayList<String>();
		try {
			tfidf_word = readDocs(TFIDF_DATA_Path);
		} catch (IOException ex) {
			log.error("tfidf data file read error", ex);
		}

		for (String tempWord : tfidf_word) {
			if (2 != tempWord.split("#").length) {
				continue;
			}
			String key = tempWord.split("#")[0];
			double value = Double.valueOf(tempWord.split("#")[1]);
			this.CACHE_TFIDF.put(key, value);
		}

		log.info("Loading tfidf data from file end");
	}
	
	public double queryIdfValue(String key)
	{
		if (key == null || key.isEmpty())
			return 0.0;

		// 默认值
		double value = 4.5;
		
		try
		{
			value = SingletonHolder.instance.CACHE_TFIDF.get(key);
			// 如果长度为2，且idf值很低，应该让其更低
			if (key.length() <= 2 && value < 4.0) {
				value = 1.5;
			}
		} catch (Exception e) {
			//log.error("There is no cache for key:" + key);
			value = 6.0;
		}
		
		return value;
	}

	/**
	 * 获取单例对象实例
	 * 
	 * @return 单例对象
	 */
	public static idfQueryInterface getInstance() {
		return SingletonHolder.instance;
	}
	
	
	private static List<String> readDocs(String inputFileDir) throws IOException {
		return Files.readLines(new File(inputFileDir), Charsets.UTF_8);
	}
	
	public static void main(String[] args) {
		System.out.println(idfQueryInterface.getInstance().queryIdfValue("散户"));
	}

}
