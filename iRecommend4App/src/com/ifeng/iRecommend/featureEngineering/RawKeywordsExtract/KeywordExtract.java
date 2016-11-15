package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.AllWordLibData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.DataExpLib;
import com.ifeng.iRecommend.wuyg.commonData.entity.entityBase.KnowledgeBaseBuild;

/**
 * <PRE>
 * 作用 : 
 *   关键词抽取类，根据相关特征及权重，得到特征词集合，并将其得分统一映射到1.0 0.5 0.1 离散值区间 
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
public class KeywordExtract {
	protected String title;
	protected String split_title;
	
	protected String content;
	protected String split_content;
	
	protected Document document;
	protected DocWordFilter filtered_doc;
	
	// 是否加入用户自定义词典
	protected boolean isUseCustomDic;
	
	protected ArrayList<String> textClass;
		
	/**
	 * <p>Title: </p>
	 * <p>Description: </p>
	 * @author liu_yi
	 * @param isUseCustomDic 是否采用用户自定义词典
	 * @param split_title 分词标题
	 * @param split_content 分词内容
	 * @param textClass 文本所属类别
	 */
	public KeywordExtract(boolean isUseCustomDic, String split_title, String split_content, ArrayList<String> textClass) {
		if (null != split_title) {
			this.set_split_title(split_title);
		}
		
		if (null != split_content) {
			this.set_split_content(split_content);
		}
		
		this.isUseCustomDic = isUseCustomDic;
		this.title = SpecialWordExtract.scnt2cnt(split_title);
		this.content = SpecialWordExtract.scnt2cnt(split_content);
		this.textClass = textClass;
		
		// 初始化文档对象
		this.document = new Document(this.split_title, this.split_content);
				
		// 词过滤与词频统计
		this.filtered_doc = new DocWordFilter(this.document, isUseCustomDic, this.title, this.content, textClass);
		this.filtered_doc.getFilteredDoc();
	}
	
	public List<String> get_weighted_keywords() {
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
		
		Map<String, Double> content_weighted_wordFre = this.filtered_doc.get_word_weighted_frequency();

		System.out.println("content_weighted_wordFre:" + content_weighted_wordFre);

		List<Double> content_weighted_value = new ArrayList<Double>();
		if (null != content_weighted_wordFre) {
			for (String word : content_weighted_wordFre.keySet()) {
				double word_weight = content_weighted_wordFre.get(word);
				String word_label = Word.create(word).getLabel();
				String word_value = Word.create(word).getValue();
				
				double word_idf = wordWeightQueryInterface.getInstance().queryWordWeightValue(word_value);
				
				// 对长度为2的词进一步降权
				if (word_value.length() <= 2) {
					word_idf = word_idf * 0.8;
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
		
		// 1.0结果
		List<String> result_1 = new ArrayList<String>();
		// 0.5结果
		List<String> result_2 = new ArrayList<String>();
		// 0.1结果
		List<String> result_3 = new ArrayList<String>();
		
		List<String> title_filtered_words = this.filtered_doc.getFilteredDoc().getTitle_wordList();
		Set<String> title_highW_words = this.filtered_doc.getTitleHighWords();
		
		// 用于存储已经出现过的词，只存value
		Set<String> appearedWords = new HashSet<String>();
		
		// 排序后做下值映射				
		for (String word : sort_counter.keySet()) {
			double word_weight = sort_counter.get(word);
			if (word_weight <= KeywordGlobalParams.epsilon) {
				continue;
			}		
			
			// 映射到1.0 0.5 0.1 离散值区间
			double standard_weight = this.weightValueMaping(word_weight, Word.create(word).getLabel());
			String word_label = Word.create(word).getLabel();
			String word_value = Word.create(word).getValue();
		
			if (word_value.equals("成都地铁")) {
				int i = 0;
			}
			
			// 将k h 词性的词做限定，最终结果限定在当前已知的所有词的值空间中
			if (!AllWordLibData.getInstance().containsKey(word_value) && 
				(word_label.equals("h") || word_label.equals("k"))) {
				continue;
			}

			if (null == word_label || null == word_value) {
				continue;
			}

			// 处理全表达信息 北京楼市--> 应该额外加入北京
			List<String> word_expands = null;
			// 扩展词的词性，如果是et就用et，否则就沿用母词的词性标签
			boolean isEtWord = false;
			if (null != KnowledgeBaseBuild.getObjectList(word_value)) {
				isEtWord = true;
			}
			
			if (appearedWords.contains(word_value)) {
				continue;
			} else {
				word_expands = DataExpLib.getInstance().getDataExpMaps().get(word_value);
			}
						
			// 先把标题中的高权重词加入1.0中
			if (title_highW_words.contains(word)) {
				result_1.add(word_value);
				result_1.add(word_label);
				result_1.add("1.0");
				
				if (null != word_expands) {
					for (String tempWord : word_expands) {
						// 没出现过，才加入全表达
						if (!appearedWords.contains(tempWord)) {
							result_1.add(tempWord);
							if (isEtWord) {
								result_1.add("et");
							} else {
								result_1.add(word_label);	
							}
							
							result_1.add("1.0");
							
							appearedWords.add(tempWord);
						}
					}
				}
								
				appearedWords.add(word_value);
				continue;
			}
			
			if (String.valueOf(standard_weight).equals("1.0")) {
				// 如果标题中未出现的et成为1.0，应该将其强制变为0.5
				if (null != title_filtered_words && !title_filtered_words.contains(word.toLowerCase())) {
					result_2.add(word_value);
					result_2.add(word_label);
					result_2.add("0.5");
					
					if (null != word_expands) {
						for (String tempWord : word_expands) {
							// 没出现过，才加入全表达
							if (!appearedWords.contains(tempWord)) {
								result_2.add(tempWord);
								if (isEtWord) {
									result_2.add("et");
								} else {
									result_2.add(word_label);	
								}
								
								result_2.add("0.5");
								
								appearedWords.add(tempWord);
							}
						}
					}
					
					appearedWords.add(word_value);
				} else {
					if (KeywordGlobalParams.Word_Tag_Weight.containsKey(word_label)) {
						result_1.add(word_value);
						result_1.add(word_label);
						result_1.add(String.valueOf(standard_weight));
						
						if (null != word_expands) {
							for (String tempWord : word_expands) {
								// 没出现过，才加入全表达
								if (!appearedWords.contains(tempWord)) {
									result_1.add(tempWord);
									
									if (isEtWord) {
										result_1.add("et");
									} else {
										result_1.add(word_label);	
									}
									
									result_1.add(String.valueOf(standard_weight));
									
									appearedWords.add(tempWord);
								}
							}
						}
						
						appearedWords.add(word_value);
					}
				}
			} else if (String.valueOf(standard_weight).equals("0.5")) {
				result_2.add(word_value);
				result_2.add(word_label);
				result_2.add(String.valueOf(standard_weight));
				
				if (null != word_expands) {
					for (String tempWord : word_expands) {
						// 没出现过，才加入全表达
						if (!appearedWords.contains(tempWord)) {
							result_2.add(tempWord);

							if (isEtWord) {
								result_2.add("et");
							} else {
								result_2.add(word_label);	
							}
							
							result_2.add(String.valueOf(standard_weight));
							
							appearedWords.add(tempWord);
						}
					}
				}
				
				appearedWords.add(word_value);
			} else if (String.valueOf(standard_weight).equals("0.1") && (word_label.equals("et")|| 
					word_label.startsWith("n")) && 
					null != title_filtered_words && title_filtered_words.contains(word)) {
				// 标题中的et，保证至少给0.5
				result_2.add(word_value);
				result_2.add(word_label);
				result_2.add("0.5");
				
				if (null != word_expands) {
					for (String tempWord : word_expands) {
						// 没出现过，才加入全表达
						if (!appearedWords.contains(tempWord)) {
							result_2.add(tempWord);

							if (isEtWord) {
								result_2.add("et");
							} else {
								result_2.add(word_label);	
							}
							
							result_2.add("0.5");
							
							appearedWords.add(tempWord);
						}
					}
				}
				
				appearedWords.add(word_value);
			} else if (null != title_filtered_words && title_filtered_words.contains(word) && word_value.length() >= 2 
					&& (word_label.equals("x") || word_label.equals("et")||word_label.equals("k") || word_label.equals("n"))) {
				// 标题中出现的词，长度大于等于3的外挂词，强制给0.5
				result_2.add(word_value);
				result_2.add(word_label);
				result_2.add("0.5");
				
				if (null != word_expands) {
					for (String tempWord : word_expands) {
						// 没出现过，才加入全表达
						if (!appearedWords.contains(tempWord)) {
							result_2.add(tempWord);

							if (isEtWord) {
								result_2.add("et");
							} else {
								result_2.add(word_label);	
							}
							
							result_2.add("0.5");
							
							appearedWords.add(tempWord);
						}
					}
				}
				
				appearedWords.add(word_value);
			} else {
				result_3.add(word_value);
				result_3.add(word_label);
				result_3.add(String.valueOf(standard_weight));
				
				if (null != word_expands) {
					for (String tempWord : word_expands) {
						// 没出现过，才加入全表达
						if (!appearedWords.contains(tempWord)) {
							result_3.add(tempWord);

							if (isEtWord) {
								result_3.add("et");
							} else {
								result_3.add(word_label);	
							}
							
							result_3.add(String.valueOf(standard_weight));
							
							appearedWords.add(tempWord);
						}
					}
				}
				
				appearedWords.add(word_value);
			}
		}
				
		// 按顺序诸神归位
		result.addAll(result_1);
		result.addAll(result_2);
		result.addAll(result_3);
		
		List<String> filteredResult = keywordExtResultFilter(result);
		
		// 保障措施，如果result全部数量过少，且内容词较少，信息不足，应该补充标题中的词
		if (filteredResult.size() <= 2 && null != content_weighted_wordFre && content_weighted_wordFre.size() <= 20) {
			if (null != title_filtered_words) {
				for (String tempTitleWord : title_filtered_words) {
					String word_label = Word.create(tempTitleWord).getLabel();
					String word_value = Word.create(tempTitleWord).getValue();
					if (!filteredResult.contains(word_value)) {
						filteredResult.add(word_value);
						filteredResult.add(word_label);
						filteredResult.add("0.1");
					}
				}
			}
		}
		
		return filteredResult;
	}
	
	/**
	 * @Title: keywordExtResultFilter
	 * @Description: 对特征抽取的最终结果做过滤
	 * @author liu_yi
	 * @return
	 * @throws
	 */
	public List<String> keywordExtResultFilter(List<String> wordList) {
		if (null == wordList) {
			return null;
		}
		
		List<String> result = new ArrayList<String>();
		
		// 从地域别名-->主名映射表中获取所有地域主名，用于出多个地域名的数量控制
		Set<String> all_place_name = this.filtered_doc.getAll_place_mainName();
		Set<String> title_place_name = this.filtered_doc.getTitle_place_name();
		
		System.out.println("all_place_name" + all_place_name);
		
		// 地名计数器，如果地名数量 > 2，只保留2个, 标题中的地名不参与计数
		int place_name_count = 0;
		int featuresNum = wordList.size()/3;
		for (int index = 0; index != featuresNum; index++) {
			int featureValueIndex = index * 3;
			String tempWord = wordList.get(featureValueIndex) + "_et";
			
			if (all_place_name.contains(tempWord) && 
					!title_place_name.contains(tempWord)) {
				if (place_name_count <= 1) {
					result.add(wordList.get(featureValueIndex));
					result.add(wordList.get(featureValueIndex + 1));
					result.add(wordList.get(featureValueIndex + 2));
				}
				
				place_name_count += 1;
			} else {
				result.add(wordList.get(featureValueIndex));
				result.add(wordList.get(featureValueIndex + 1));
				result.add(wordList.get(featureValueIndex + 2));
			}
		}
		
		return result;
	}
	
	/**
	 * @Title: getWordDisWeight
	 * @Description: 对词在文章中的分布做加权
	 * @author liu_yi
	 * @param word
	 * @return
	 * @throws
	 */
	public double getWordDisWeight(String word) {
		double defaultWeight = 1.0;
		Map<String, List<Integer>> wordDis = this.filtered_doc.getWordDistribute();
		
		Map<String, Double> wordFreq = this.filtered_doc.get_word_weighted_frequency();
		double word_freq = wordFreq.get(word);
		
		IWord iword = WordFactory.create(word);
		boolean isInTitle = false;
		
		if (wordDis.containsKey(word)) {
			List<Integer> wordDisList = wordDis.get(word);
			
			int wordDisSecCount = 0;
			if (wordDisList.contains(1024)) {
				isInTitle = true;
				wordDisSecCount = wordDisList.size() - 1;
			} else {
				wordDisSecCount = wordDisList.size();
			}
			
			// 词在文章中出现的段落比例
			double wordDisSecProportion = 0.0; 
			int docSecSize = this.filtered_doc.getDoc().getDocSecSize();
			if (0 != docSecSize) {
				wordDisSecProportion = wordDisSecCount/docSecSize;
			}
			
			double threshold_1 = 0.25; 
			double threshold_2 = 0.5;
			double threshold_3 = 0.75;			
			
			int retval_1 = Double.compare(wordDisSecProportion, threshold_1); 
			int retval_2 = Double.compare(wordDisSecProportion, threshold_2);
			int retval_3 = Double.compare(wordDisSecProportion, threshold_3);
			
			// 0-0.25:retval_1 < 0
			// 0.25-0.5:retval_1 >= 0 && retval_2 < 0
			// 0.5-0.75:retval_2 >= 0 && retval_3 < 0
			// >= 0.5:retval_3 >= 0
			
			if (!isInTitle) {
				if (retval_1 >= 0 && retval_2 < 0 && iword.getValue().length() > 2 && iword.getValue().length() < 4) {
					// 标题中未出现，内容中出现了多次，且词长不够
					return 1.5;
				} else if (retval_2 >= 0 && retval_3 < 0 && iword.getValue().length() >= 4) {
					// 标题中未出现，内容中出现了多次，且词长大于4
					return 2.5;
				} else if (docSecSize >= 10 && retval_3 >= 0 && iword.getValue().length() <= 2 && word_freq >= 12.0) {
					// 标题中未出现，内容中出现了多次，且词长小于等于2，说明该词很普遍，可以降权
					return 0.01;
				} else if (retval_3 >= 0 && iword.getValue().length() >= 4) {
					return 3.0;
				} else if (wordDisSecCount == 1 && word_freq > 5.0 && !iword.getLabel().equals("et")) {
					// 标题中未出现，只在一段中经常出现
					return 0.01;
				} else if (wordDisSecCount <= 2 && this.filtered_doc.getDoc().getDocSecSize() >= 12) {
					// 标题中未出现，分布比例又比较低的
					return defaultWeight;
				}
			} else {
				// 标题和内容中都有的情况
				if (wordDisSecCount <= 0) {
					return defaultWeight;
				} else if (wordDisSecCount > 1 && wordDisSecCount < 4 && retval_1 >= 0) {
					return 2.0;
				} else if (wordDisSecCount >= 4 && wordDisSecCount <= 8 && retval_2 >= 0) {
					return 3.0;
				} else if (wordDisSecCount >= 8 && retval_2 >= 0 && iword.getValue().length() > 2){
					return 4.0;
				} else {
					return defaultWeight;
				}
			}
			
			return defaultWeight;
			
		} else {
			return defaultWeight;
		}
	}
	
		
	/**
	 * @Title: weightValueMaping
	 * @Description: 权值映射到1.0 0.5 0.1 离散值区间 
	 * @author liu_yi
	 * @param weight
	 * @param wordLable
	 * @return
	 * @throws
	 */
	public double weightValueMaping(double weight, String wordLable) {
		double data1 = 0.2;  
	    double data2 = 0.35;  
	    double data3 = 0.55;
	    BigDecimal threshold_1 = new BigDecimal(data1);  
	    BigDecimal threshold_2 = new BigDecimal(data2);
	    BigDecimal threshold_3 = new BigDecimal(data3);
	    
	    BigDecimal inputWeight = new BigDecimal(weight);
	    
	    if (inputWeight.compareTo(threshold_3) >= 0) {
	    	return 1.0;
	    }
	    
	    if (inputWeight.compareTo(threshold_2) >= 0 &&
	    	(wordLable.equals("et"))) {
	    	return 1.0;
	    }
	    
	    if ((inputWeight.compareTo(threshold_2) < 0 && inputWeight.compareTo(threshold_1) >= 0) &&
		    	(wordLable.equals("et") || wordLable.equals("x"))) {
	    	return 0.5;
	    }
	    
	    return 0.1;
	 }
	

	/**
	 * @Title: sortMapByValue
	 * @Description: 
	 * @author liu_yi
	 * @param asc_desc desc时降序, asc升序;默认升序
	 * @param map 待排序map
	 * @return
	 * @throws
	 */
	public <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(final String asc_desc, Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				if (asc_desc.equals("desc")) {
					return -(o1.getValue()).compareTo(o2.getValue());
				} else if (asc_desc.equals("asc")){
					return (o1.getValue()).compareTo(o2.getValue());
				} else {
					return (o1.getValue()).compareTo(o2.getValue());
				}
				
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public String get_split_title() {
		return split_title;
	}

	public void set_split_title(String split_title) {
		this.split_title = split_title;
	}

	public String get_split_content() {
		return split_content;
	}

	public void set_split_content(String split_content) {
		this.split_content = split_content;
	}


	public String get_content() {
		return content;
	}

	public void set_content(String content) {
		this.content = content;
	}

	public Document get_document() {
		return document;
	}

	public void set_document(Document document) {
		this.document = document;
	}

	public DocWordFilter get_filtered_doc() {
		return filtered_doc;
	}

	public void set_filtered_doc(DocWordFilter filtered_doc) {
		this.filtered_doc = filtered_doc;
	}
}
