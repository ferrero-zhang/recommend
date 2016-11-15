package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

/**
 * <PRE>
 * 作用 : 
 *   词的工厂类，生成带标签的词语
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
 *          1.0          2015年10月10日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class WordFactory {
	/**
	 * 根据参数字符串产生对应的词语
	 * 
	 * @param param
	 * @return
	 */
	public static IWord create(String param) {
		if (param == null)
			return null;

		else {
			return Word.create(param);
		}
	}
}
