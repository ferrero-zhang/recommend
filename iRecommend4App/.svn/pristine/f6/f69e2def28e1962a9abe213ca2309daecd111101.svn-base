package com.ifeng.iRecommend.usermodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Charsets;
import com.ifeng.commen.Utils.FileUtil;

/**
 * <PRE>
 * 作用 : 
 *   基于内部类的单例模式 Lazy 线程安全
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   此IDF是基于user df统计；
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
	private static final String TFIDF_DATA_Path = "./conf/allFieldIdf_userDF.txt";

	/**
	 * 内部类，用于实现lzay机制
	 */
	private static class SingletonHolder {
		private static idfQueryInterface instance = new idfQueryInterface();
	}

	private idfQueryInterface() {
		this.CACHE_TFIDF = new HashMap<String, Double>();
		log.info("Loading tfidf data from file...");
		List<String> tfidf_word = new ArrayList<String>();
		
		tfidf_word = readDocs(TFIDF_DATA_Path);

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
		double value = 1.0;
		
		try
		{
			value = CACHE_TFIDF.get(key);
		} catch (Exception e) {
			value = 1.0;
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
	
	
	private static List<String> readDocs(String inputFileDir){
		ArrayList<String> al_str = new ArrayList<String>();
		FileUtil fu = new FileUtil();
		fu.Initialize(inputFileDir, "utf-8");
		String line = null;
		while((line = fu.ReadLine()) != null){
			al_str.add(line);
		}
		return al_str;
	}
	
	public static void main(String[] args) {
		System.out.println(idfQueryInterface.getInstance().queryIdfValue("大陆时事"));
		System.out.println(idfQueryInterface.getInstance().queryIdfValue("国际"));
		System.out.println(idfQueryInterface.getInstance().queryIdfValue("军事"));
		System.out.println(idfQueryInterface.getInstance().queryIdfValue("明星"));
		System.out.println(idfQueryInterface.getInstance().queryIdfValue("电视娱乐"));
		System.out.println(idfQueryInterface.getInstance().queryIdfValue("社会资讯"));
	}

}
