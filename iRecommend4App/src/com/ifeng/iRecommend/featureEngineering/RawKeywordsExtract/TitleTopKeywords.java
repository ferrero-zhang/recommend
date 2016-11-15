package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;

/**
 * <PRE>
 * 作用 : 
 *   传入标题或者短句，返回最多topNum个关键词描述
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   继承自KeywordExtract类，共用基础处理部分，重写规则抽取及组合方法
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年11月11日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class TitleTopKeywords extends KeywordExtract{
	private static final Log LOG = LogFactory.getLog(TitleTopKeywords.class);
	
	/**
	 * @Fields topNum : 一个标题，最多返回两个关键词
	 */
	private final int topNum = 2;
	
	public TitleTopKeywords(boolean isUseCustomDic, String split_title, String split_content) {
		super(isUseCustomDic, split_title, split_content, null);
	}

	/**
	 * @Title: getTitleTopKeywords
	 * @Description: title关键词抽取方法
	 * @author liu_yi
	 * @return
	 * @throws
	 */
	public List<String> getTitleTopKeywords() {
		if (null == this.filtered_doc) {
			return null;
		}
		
		List<String> result = new ArrayList<String>();
				
		// 括号、冒号...等词提取
		List<String> special_word = SpecialWordExtract.ExtractSpecialWord(this.title, this.content);
		if (null != special_word) {
			// 先加入结果
			result.addAll(special_word);
		}

		this.filtered_doc.getFilteredDoc();
		Map<String, Double> content_weighted_wordFre = this.filtered_doc.get_word_weighted_frequency();
		List<Double> content_weighted_value = new ArrayList<Double>();
		if (null != content_weighted_wordFre) {
			for (String word : content_weighted_wordFre.keySet()) {
				double word_weight = content_weighted_wordFre.get(word);
				String word_label = Word.create(word).getLabel();
				String word_value = Word.create(word).getValue();
				
				// idf权重(信息
				double word_idf = idfQueryInterface.getInstance().queryIdfValue(word_value);
				// 对长度为2的词进一步降权
				if (word_value.length() <= 2) {
					word_idf = word_idf * 0.8;
				}
				
				// 长度大于等于4的词，适当加权
				if (word_value.length() >= 4) {
					word_idf = word_idf * 2.0;
				}
				
				// 词性权重
				double label_weight = KeywordGlobalParams.Word_Tag_Weight.get(word_label);
				
				// 词分布权重
				double word_dis_weight = this.getWordDisWeight(word);
								
				double weighted_wordFreq = word_weight * word_idf * label_weight * word_dis_weight;
				
				// 替换下
				content_weighted_wordFre.put(word, weighted_wordFreq);
				content_weighted_value.add(weighted_wordFreq);
			}
		}

		MinMaxNorm mn = MinMaxNorm.createFor(content_weighted_value);
		
		if (null != content_weighted_wordFre) {
			for (String word : content_weighted_wordFre.keySet()) {
				double word_weight = content_weighted_wordFre.get(word);
				double norm_word_weight = mn.normalize(word_weight);
				content_weighted_wordFre.put(word, norm_word_weight);
			}
		}
		
		Map<String, Double> sort_counter = this.sortMapByValue("desc", content_weighted_wordFre);
		
		for (String word : sort_counter.keySet()) {			
			// 之前的抽取已经有了
			if (result.contains(Word.create(word).getValue())) {
				continue;
			}
			
			result.add(Word.create(word).getValue());
			result.add(Word.create(word).getLabel());
			result.add(String.valueOf(sort_counter.get(word)));
		}
		
		List<String> subResult = new ArrayList<String>();
		String result_str = "";
		int featuresNum = result.size()/3;
		int result_count = 0;
		for (int index = 0; index != featuresNum; index++) {
			if (result_count == topNum) {
				break;
			}
		
			int featureValueIndex = index * 3;
			int featureTagIndex = (index * 3) + 1;
			int weightIndex = (index * 3) + 2;
			
			result_str += result.get(featureValueIndex) + "_" + result.get(featureTagIndex) + "_" + result.get(weightIndex) + " ";
			
			subResult.add(result.get(featureValueIndex));
			subResult.add(result.get(featureTagIndex));
			subResult.add(result.get(weightIndex));
			
			result_count++;
		}
		result_str.trim();
		//System.out.println(super.split_title + "==>" + result_str);
		LOG.info(super.split_title + "==> " + result_str);
		
		return subResult;
	}
	
	/**
	 * @Title: words_filter
	 * @Description: 保留方法，可能要过掉一些特殊词
	 * @author liu_yi
	 * @param word_list
	 * @return
	 * @throws
	 */
	public List<String> words_filter(List<String> word_list) {
		return null;
	}
	
	public static void main(String[] args) {
		EntityLibQuery.init();
		
		// example
		String tablename = "appitemdb";
		IKVOperationv2 ob = new IKVOperationv2(tablename);
		Random r = new Random();
		for (int i = 0; i != 100; i++) {
			int randomDocID = r.nextInt(600000);
			itemf item = ob.queryItemF(String.valueOf(randomDocID),"c");

			if (null == item) {
				continue;
			}
			
			String splitTitle = item.getSplitTitle();
			TitleTopKeywords tt = new TitleTopKeywords(false, splitTitle, null);
			List<String> result = tt.getTitleTopKeywords();
		}
		
		ob.close();
	}
}
