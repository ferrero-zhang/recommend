package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <PRE>
 * 作用 : 
 *   特征抽取全局参数类
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
public class KeywordGlobalParams {
	// 段落权重
	public static final Map<String, Double> Sec_POS_Weight = new HashMap<String, Double>();
	
	// 词性权重
	public static final Map<String, Double> Word_Tag_Weight = new HashMap<String, Double>();
	
	// title权重(content权重默认为1.0)
	public static final double title_weight = 4;
	
	// 极小值阈值
	public static final double epsilon = 0.01;
	
	// 无意义词，先粗暴过掉吧,后续改为从配置文件载入
	public static List<String> noSenseKeyword =  new ArrayList<String>();
	
	static {
		// 第一段中的词权重  (先不加权)
		Sec_POS_Weight.put("FirstSection", 1.0);
				
		// 每个段落首句的词作适当加权(先不加权)
		Sec_POS_Weight.put("FirstSentence", 1.0);
			

		// 实体
		Word_Tag_Weight.put("et", 1.1);
		
		// 自定义分词
		Word_Tag_Weight.put("x", 1.1);
		
		// 人名
		Word_Tag_Weight.put("nr", 1.0);
		
		// 机构名
		Word_Tag_Weight.put("nt", 1.0);
		
		// 其他专名
		Word_Tag_Weight.put("nz", 1.0);
		
		// 名词
		Word_Tag_Weight.put("n", 1.0);
		
		// 地名
		Word_Tag_Weight.put("ns", 1.0);
		
		// 产品词
		Word_Tag_Weight.put("e", 1.0);
		
		// 外文词
		Word_Tag_Weight.put("nx", 1.0);
		
		// 自定义词与海量词冲突了，可能是k，如阿里巴巴
		Word_Tag_Weight.put("k", 1.0);
		// 自定义词与海量词冲突了，可能是h，如"万科"
		Word_Tag_Weight.put("h", 1.0);
		//Word_Tag_Weight.put("v", 1.0);
		
		noSenseKeyword.add("高清大图_et");
	}
}
