package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.AliasLibData;
import com.ifeng.iRecommend.liuyi.customDicWordSearch.CustomWordSearcher;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;




/**
 * <PRE>
 * 作用 : 
 *   对新闻文章类中的组成段落/句子进行遍历，过滤词并保留待用信息
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
public class DocWordFilter {
	private Document doc;
	
	// 时政--->梁振英、李克强，主要是为了统计et的主要分布类，把主流et加权
	private Map<String, Set<String>> etClass_etWordSet;
	
	// 文章正文词频加权统计
	private Map<String, Double> word_weighted_frequency;
	
	// et--> 类别 (用于逻辑控制)
	private Map<String, Set<String>> et_class_map;
	
	// 词---> level string
	// example: 北京-->北京  count:0  category:社会 filename:地区  levels:[社会, 中国, 地点术语]  nicks:[]  host:[]  type:[]  guest:[]
	private Map<String, String> word_level_map;
	
	// 词分布的情况 (词--->List<Integer>： 标题,出现的段落索引)
	// example 中国---> (1024, 0, 1, 2)：在标题中出现(特殊计数1024表示)，出现在索引为0, 1, 2
	//         中国---> (0, 0, 1, 2)：在标题中未出现，出现在索引为0, 1, 2
	private Map<String, List<Integer>> word_distribute;
	
	// 地域名词   别名-->主名的映射,如:北京-->北京市
	private Map<String, String> place_nick_name_map;
	// 所有地域主名
	private Set<String> all_place_name;
	
	// 标题中的地域主名
	private Set<String> title_place_name;
	
	// 按规则定义的标题中的高表达力词，后续统一给1.0的权重
	private Set<String> title_highW_words;
	
	// 特征抽取的时候，是否需要采用自定义词典	 
	private boolean isUseCustomDic;
	
	// 文章标题中是否包含自定义词典(如果不包含，就可以直接不处理了)
	private boolean isTitleContainCustomWord;
	private boolean isContentContainCustomWord;
	
	private String title;
	private String content;
	
	// 分类可能有多个类别标签
	private ArrayList<String> textClass;
	
	/**
	 * <p>Title: </p>
	 * <p>Description: </p>
	 * @author liu_yi
	 * @param doc  Document对象
	 * @param isUseCustomDic 是否加入自定义词典
	 * @param title 文章标题
	 * @param content 文章内容
	 */
	public DocWordFilter(Document doc, boolean isUseCustomDic, String title, String content, ArrayList<String> textClass) {
		this.setDoc(doc);
		this.etClass_etWordSet = new HashMap<String, Set<String>>();
		this.word_weighted_frequency = new HashMap<String, Double>();
		this.et_class_map = new HashMap<String, Set<String>>();
		this.word_level_map = new HashMap<String, String>();
		this.word_distribute = new HashMap<String, List<Integer>>();
		this.place_nick_name_map = new HashMap<String, String>();
		this.all_place_name = new HashSet<String>();
		this.title_highW_words = new HashSet<String>();
		this.setTitle_place_name(new HashSet<String>());
		this.setUseCustomDic(isUseCustomDic);
		this.setTitle(title);
		this.setContent(content);
		this.setTextClass(textClass);
	}
	
	/**
	 * @Title: FilterWord
	 * @Description: 过一遍文档词：
	 *               1) 查实体库，词性替换 
	 *               2) 保留区分度高的词性的词  
	 * @author liu_yi
	 * @throws
	 */
	public Document getFilteredDoc() {
		if (null == this.doc) {
			return null;
		}
		
		// 如果需要采用自定义词典，先快速看下文章中是否含有自定义中词典的词，如果不含有，后续就可以直接不处理
		if (isUseCustomDic) {
			if (null != this.title) {
				this.setIitleContainCustomWord(CustomWordSearcher.isContainCustomWord(title));
			} else {
				this.setIitleContainCustomWord(false);
			}
			
			if (null != this.content) {
				this.setContentContainCustomWord(CustomWordSearcher.isContainCustomWord(content));
			} else {
				this.setContentContainCustomWord(false);
			}
		}
		
		// 保留标题中词性重要的词
		List<String> imp_title_words = extractValuableWords(this.doc.getTitle_wordList(), true);
		// 实体库过滤
		List<String> filtered_imp_title_words = filterEntityWord(imp_title_words, "title");
		// 合并主别名
		List<String> combined_title_words = wordNicknameCombine(filtered_imp_title_words);
		// 如果需要自定义词典，则处理一下
		if (this.isUseCustomDic && this.isTitleContainCustomWord) {
			List<String> customWords = CustomWordSearcher.parseText(this.title);
			if (null != customWords && null != combined_title_words) {
				combined_title_words.addAll(customWords);
				// 也加入到标题词统计中
				this.doc.getTitle_wordList().addAll(customWords);
			}
		}
		
		// 做下title加权词频统计
		this.weightedWordCount(combined_title_words, 0, 0, true);
		
		// 同样方式处理下content
		List<Section> doc_sec_list = this.doc.getSectionList();
		List<Section> filtered_doc_sec_list = filterDocSection(doc_sec_list);
		
		Document filtered_doc = new Document(combined_title_words, filtered_doc_sec_list);

		return filtered_doc;
	}

	/**
	 * @Title: filterDocSection
	 * @Description: 文章按段落处理，逐句过滤
	 * @author liu_yi
	 * @param doc_sec_list
	 * @return
	 * @throws
	 */
	public List<Section> filterDocSection(List<Section> doc_sec_list) {
		if (null == doc_sec_list || doc_sec_list.isEmpty()) {
			return null;
		}
		
		List<Section> filtered_doc_sec_list = new ArrayList<Section>();
		
		for (Section sec : doc_sec_list) {
			List<Sentence> stList = sec.getSentenceList();
			int secIndex = sec.getSectionIndex();	
			
			List<Sentence> filtered_stList = new ArrayList<Sentence>();

			if (null == stList || stList.isEmpty()) {
				continue;
			}
			
			// 如果最后一段词太短，不处理(一般是类似 "摄影：王大锤"之类的)
			if ((secIndex == doc_sec_list.size() - 1) &&
				(stList.size() == 1)) {
				if (stList.get(0).getWordList().size() <= 2) {
					continue;
				}
			}
			
			for (Sentence sentence : stList) {
				if (null == sentence) {
					continue;
				}
								
				int stIndex = sentence.getSentenceIndex();
				
				List<String> sentence_words = sentence.getWordList();
				List<String> imp_sentence_words = extractValuableWords(sentence_words, false);
				if (null == imp_sentence_words || imp_sentence_words.isEmpty()) {
					continue;
				}
				
				List<String> filtered_st_words = filterEntityWord(imp_sentence_words, "content");
				List<String> combined_words = wordNicknameCombine(filtered_st_words);
				if (null == combined_words || combined_words.isEmpty()) {
					continue;
				}
				
				// 如果需要启用自定义词典且内容中含有自定义词典中的词，就合并计算
				// 2016.05.05修改：不再考虑内容中是否有数据
				if (this.isUseCustomDic && this.isContentContainCustomWord) {
					String splitSentenceStr = sentence.getSplitSentenceStr();
					String sentenceStr = splitSentenceStr.replaceAll(" *_[a-zA-Z]+ *", "");
					List<String> sentenceCustomWords = CustomWordSearcher.parseText(sentenceStr);
					if (null != sentenceCustomWords) {
						combined_words.addAll(sentenceCustomWords);
					}
				}
				
				// 做下词统计
				this.weightedWordCount(combined_words, secIndex, stIndex, false);
				
				Sentence filtered_st = new Sentence(combined_words, stIndex);
				filtered_stList.add(filtered_st);
			}
		
			if (!filtered_stList.isEmpty()) {
				Section st = new Section(filtered_stList, secIndex);
				filtered_doc_sec_list.add(st);
			}
		}
		
		return filtered_doc_sec_list;
	}
		
	/**
	 * @Title: filterEntityWord
	 * @Description: 逐词过滤查询实体库，如果是实体词，改变label并记录相关全局信息
	 * @author liu_yi
	 * @param wordsList
	 * @param textTag 文本标记，title or content类型
	 * @return
	 * @throws
	 */
	public List<String> filterEntityWord(List<String> wordsList, String textTag) {
		if (null == wordsList || wordsList.isEmpty()) {
			return null;
		}

		// 复制一下做替换
		List<String> result = new ArrayList<String>();
		result.addAll(wordsList);
		
		// 过一遍过滤后的词库，查实体库
		if (null != wordsList) {
			int wordIndex = 0;
			for (String tempWord : wordsList) {
				IWord temp_iword = WordFactory.create(tempWord);
				String temp_word_label = temp_iword.getLabel();
				
				if (!(KeywordGlobalParams.Word_Tag_Weight.containsKey(temp_word_label))) {
					wordIndex += 1;
					continue;
				}
				
				List<EntityInfo> entResultArray = EntityLibQuery.getEntityList(temp_iword.getValue());
				
				// 改一下tempWord的label为et
				String newTempWord = temp_iword.getValue().toLowerCase() + "_et";
				
				if (null == entResultArray || entResultArray.isEmpty()) {
					wordIndex += 1;
					continue;
				}
	
				this.word_level_map.put(temp_iword.getValue(), entResultArray.toString());
				result.set(wordIndex, newTempWord);
				
				wordIndex += 1;
				
				for(EntityInfo temp : entResultArray) {
					if (temp.getFilename().equals("entLib_地区")) {
						String main_name = temp.getWord() + "_et";
						
						// 如果是标题中的地理位置，记一下
						if (textTag.equals("title")) {
							this.title_place_name.add(main_name);
						}
												
						this.all_place_name.add(main_name);
						if (!main_name.equals(newTempWord)) {
							if (!this.place_nick_name_map.containsKey(newTempWord)) {
								this.place_nick_name_map.put(newTempWord, main_name);
							}
						}
					}
				}

				for (EntityInfo ent : entResultArray) {
					String entCat = ent.getCategory();
					if (this.et_class_map.containsKey(newTempWord)) {
						Set<String> class_set = this.et_class_map.get(newTempWord);
						class_set.add(entCat);
						this.et_class_map.put(newTempWord, class_set);
					} else {
						Set<String> class_set = new HashSet<String>();
						class_set.add(entCat);
						this.et_class_map.put(newTempWord, class_set);
					}
					
					if (this.etClass_etWordSet.containsKey(entCat)) {
						Set<String> etWordSet = this.etClass_etWordSet.get(entCat);
						etWordSet.add(temp_iword.getValue());
						this.etClass_etWordSet.put(entCat, etWordSet);
					} else {
						Set<String> etWordSet = new HashSet<String>();
						etWordSet.add(temp_iword.getValue());
						this.etClass_etWordSet.put(entCat, etWordSet);
					}
					
				}
			}
		}
		
		return result;
	}

	/**
	 * @Title: removeLessImpWord
	 * @Description: 保留有区分度词性的词，移除重要性低的词; 同时，如果需要使用自定义词典，则进行词典查询
	 * @author liu_yi
	 * @param wordList 原始文本数据分词后的word列表
	 * @param rawTextType 原始文本数据的类型：title/content
	 * @return
	 * @throws
	 */
	public List<String> extractValuableWords(List<String> wordList, boolean isTitle) {
		if (null == wordList) {
			return null;
		}
		
		List<String> result = new ArrayList<String>();
		
		for (String temp_word : wordList) {		
			IWord temp_iword = WordFactory.create(temp_word);
			if (null == temp_iword) {
				continue;
			}
			
			String entLibValue = null;
			if (isTitle) {
				List<EntityInfo> entResultArray = EntityLibQuery.getEntityList(temp_iword.getValue());
				if (null != entResultArray) {
					entLibValue = entResultArray.toString();
				}
			}
			
			String temp_word_value = temp_iword.getValue();
			String temp_word_label = temp_iword.getLabel();
			// 长度为1的直接过掉
			if (temp_word_value.length() <= 1) {
				continue;
			}
						
			if (null == this.textClass) {
				// 属于保留label的保留下来
				if (KeywordGlobalParams.Word_Tag_Weight.containsKey(temp_word_label)) {
					result.add(temp_word);
				}
			} else if (!this.textClass.isEmpty()) {
				
				if (!KeywordGlobalParams.Word_Tag_Weight.containsKey(temp_word_label)) {
					continue;
				}
				
				String temp_wordValue = temp_iword.getValue();

				// 主别名表中没有
				ConcurrentHashMap<String, HashSet<String>> alias_value = AliasLibData.getInstance().searchAlias(temp_wordValue);
				if (null == alias_value || alias_value.isEmpty()) {
					result.add(temp_word);
				} else {
					// 主别名表中如果有该数据,就要做下替换，统一替换成主名
		
					// 当前最多传入两个类别信息
					String[] textClassArray = new String[]{"fc", "sc"};
					for (int i = 0; i != this.textClass.size(); i++) {
						textClassArray[i] = this.textClass.get(i);
					}
					
					if (!alias_value.containsKey(textClassArray[0]) &&
						!alias_value.contains(textClassArray[1])) {
						// 如果不包含，直接加入即可
						result.add(temp_word);
						
						// 如果词是标题中的人名、组织机构名等et，直接加入title_highW_words
						if (isTitle && null != entLibValue) {
							if (entLibValue.indexOf("人物术语")  >= 0 ||
								entLibValue.indexOf("组织机构术语") >= 0) {
								this.title_highW_words.add(temp_word);
							}
						}
						
					} else {
						/*
						 * 1. 如果包含，做一下数据替换：将别名替换为主名
						 * 2. 可能存在同一领域的同一别名对应不同的主名的情况，例如：足球詹姆斯和篮球詹姆斯,所以value是set
						 * 3. 对上述第二种情况，暂时没法解决，由于其可能性极低，暂不考虑
						 * 4. 同时还要将替换后的主名的领域信息加入et_class_map,用于逻辑控制
						 */
						
						Set<String> main_nameSet1 = alias_value.get(textClassArray[0]);
						Set<String> main_nameSet2 = alias_value.get(textClassArray[1]);
						
						boolean hasReplaced = false;
						if (null != main_nameSet1) {
							if (1 == main_nameSet1.size()) {
								String main_name = main_nameSet1.iterator().next();
								String new_wordStr = main_name + "_" + temp_word_label;
								result.add(new_wordStr);
								hasReplaced = true;
								
								// 如果词是标题中的人名、组织机构名等et，直接加入title_highW_words
								if (isTitle && null != entLibValue) {
									if (entLibValue.indexOf("人物术语")  >= 0 ||
										entLibValue.indexOf("组织机构术语") >= 0) {
										this.title_highW_words.add(new_wordStr);
									}
								}
								
								if (!main_name.equals(temp_wordValue)) {
									System.out.println("extractValuableWords [word replace]:" + temp_word + "=>" + new_wordStr);
								}
								
							} else {
								result.add(temp_word);
								// 如果词是标题中的人名、组织机构名等et，直接加入title_highW_words
								if (isTitle && null != entLibValue) {
									if (entLibValue.indexOf("人物术语")  >= 0 ||
										entLibValue.indexOf("组织机构术语") >= 0) {
										this.title_highW_words.add(temp_word);
									}
								}
							}
						}
						
						
						if (!hasReplaced && null != main_nameSet2) {
							if (1 == main_nameSet2.size()) {
								String main_name = main_nameSet2.iterator().next();
								String new_wordStr = main_name + "_" + temp_word_label;
								result.add(new_wordStr);
								
								// 如果词是标题中的人名、组织机构名等et，直接加入title_highW_words
								if (isTitle && null != entLibValue) {
									if (entLibValue.indexOf("人物术语")  >= 0 ||
										entLibValue.indexOf("组织机构术语") >= 0) {
										this.title_highW_words.add(new_wordStr);
									}
								}
								
								if (!main_name.equals(temp_wordValue)) {
									System.out.println("extractValuableWords [word replace]:" + temp_word + "=>" + new_wordStr);
								}
								
							} else {
								result.add(temp_word);
								
								// 如果词是标题中的人名、组织机构名等et，直接加入title_highW_words
								if (isTitle && null != entLibValue) {
									if (entLibValue.indexOf("人物术语")  >= 0 ||
										entLibValue.indexOf("组织机构术语") >= 0) {
										this.title_highW_words.add(temp_word);
									}
								}
							}
						}
					}
				}
			}
			
		}
		
		return result;
	}
	
	/**
	 * @Title: wordNicknameCombine
	 * @Description: 词的主别名合并
	 * @author liu_yi
	 * @param inputWordList
	 * @return
	 * @throws
	 */
	public List<String> wordNicknameCombine(List<String> inputWordList) {	
		if (null == inputWordList) {
			return null;
		}
		
		List<String> result = new ArrayList<String>();
		for (String temp_word : inputWordList) {
			IWord temp_iword = WordFactory.create(temp_word);
			if (null == temp_iword) {
				continue;
			}
			
			String temp_word_label = temp_iword.getLabel();
			if (!temp_word_label.equals("et")) {
				result.add(temp_word);
				continue;
			}
			
			String temp_main_name = this.place_nick_name_map.get(temp_word);
			// 对地理位置术语进行合并
			if (this.place_nick_name_map.containsKey(temp_word)) {
				result.add(temp_main_name);

				// 同时加入et_class_map
				List<EntityInfo> entResultArray = EntityLibQuery
						.getEntityList(WordFactory.create(temp_main_name)
								.getValue());
				this.word_level_map.put(WordFactory.create(temp_main_name)
						.getValue(), entResultArray.toString());

				for (EntityInfo ent : entResultArray) {
					String entCat = ent.getCategory();

					if (this.et_class_map.containsKey(temp_main_name)) {
						Set<String> class_set = this.et_class_map
								.get(temp_main_name);
						class_set.add(entCat);
						this.et_class_map.put(temp_main_name, class_set);
					} else {
						Set<String> class_set = new HashSet<String>();
						class_set.add(entCat);
						this.et_class_map.put(temp_main_name, class_set);
					}
				}
			} else {
				result.add(temp_word);
			}

		}
		
		return result;
	}
	
	/**
	 * @Title: weightedWordCount
	 * @Description: 带权词统计
	 * @author liu_yi
	 * @param wordList
	 * @param sectionIndex
	 * @param sentenceIndex
	 * @param isTitle
	 * @throws
	 */
	public void weightedWordCount(List<String> wordList, int sectionIndex, int sentenceIndex, boolean isTitle) {
		// 处理标题
		if (isTitle && null != wordList) {
			for (String tempWord : wordList) {
				IWord temp_iword = WordFactory.create(tempWord);
				
				// 过掉“http:”之类无意义的词
				if (KeywordGlobalParams.noSenseKeyword.contains(tempWord) || 
						temp_iword.getValue().startsWith("http:")) {
					continue;
				}
				
				if (!this.word_distribute.containsKey(tempWord)) {
					List<Integer> wordDis = new ArrayList<Integer>();
					wordDis.add(1024);
					// 标题中可能有多次出现的词？无影响
					this.word_distribute.put(tempWord, wordDis);
				}
				
				if (this.word_weighted_frequency.containsKey(tempWord)) {
					double old_weighted_num = this.word_weighted_frequency.get(tempWord);
					// 对于x, et的词，才对标题加权
					if (temp_iword.getLabel().equals("x") ||
						temp_iword.getLabel().equals("et")) {
						this.word_weighted_frequency.put(tempWord, old_weighted_num + KeywordGlobalParams.title_weight);
					} else {
						this.word_weighted_frequency.put(tempWord, old_weighted_num + 1.0);
					}
					
				} else {
					if (temp_iword.getLabel().equals("x") ||
							temp_iword.getLabel().equals("et")) {
						this.word_weighted_frequency.put(tempWord, KeywordGlobalParams.title_weight);	
					} else {
						this.word_weighted_frequency.put(tempWord, 1.0);
					}
				}
			}
			
			return;
		}
		
		if (null == wordList || wordList.isEmpty()) {
			return;
		}
		
		double weight = 1.0;
		
		// 首段中的词加权计数 (改为默认值)
		if (0 == sectionIndex) {
			weight = KeywordGlobalParams.Sec_POS_Weight.get("FirstSection");
		}
		
		// 段首句中的词加权计数 (改为默认值)
		if (0 != sectionIndex && 0 == sentenceIndex) {
			weight = KeywordGlobalParams.Sec_POS_Weight.get("FirstSentence");
		}
		
		for (String tempWord : wordList) {
			IWord temp_iword = WordFactory.create(tempWord);
			// 过掉“微信公众号”之类无意义的词
			if (KeywordGlobalParams.noSenseKeyword.contains(tempWord) ||
					temp_iword.getValue().startsWith("http:")) {
				continue;
			}
			
			if (this.word_weighted_frequency.containsKey(tempWord)) {
				double old_weighted_num = this.word_weighted_frequency.get(tempWord);
				this.word_weighted_frequency.put(tempWord, old_weighted_num + weight);
			} else {
				this.word_weighted_frequency.put(tempWord, weight);
			}
			
			// 标题中已经出现了该词
			if (this.word_distribute.containsKey(tempWord)) {
				List<Integer> wordDis = this.word_distribute.get(tempWord);
				// 同段落的出现，只计数一次
				if (!wordDis.contains(sectionIndex)) {
					wordDis.add(sectionIndex);
				}
				
				this.word_distribute.put(tempWord, wordDis);
			} else {
				// 标题中未出现该词
				List<Integer> wordDis = new ArrayList<Integer>();
				// 同段落的出现，只计数一次
				if (!wordDis.contains(sectionIndex)) {
					wordDis.add(sectionIndex);
				}
				
				this.word_distribute.put(tempWord, wordDis);
			}
		}
	}
	
	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public Map<String, Set<String>> getEtClass_etWordSet() {
		return etClass_etWordSet;
	}

	public void setEtClass_etWordSet(Map<String, Set<String>> etClass_etWordSet) {
		this.etClass_etWordSet = etClass_etWordSet;
	}

	public Map<String, Double> get_word_weighted_frequency() {
		return word_weighted_frequency;
	}

	public void set_word_weighted_frequency(Map<String, Double> contentWordCounter) {
		this.word_weighted_frequency = contentWordCounter;
	}
	
	public Map<String, Set<String>> getEt_class_map() {
		return this.et_class_map;
	}

	public void setEt_class_map(Map<String, Set<String>> et_class_map) {
		this.et_class_map = et_class_map;
	}
	
	public Map<String, List<Integer>> getWordDistribute() {
		return this.word_distribute;
	}

	public Map<String, String> getWord_level_map() {
		return word_level_map;
	}

	public void setWord_level_map(Map<String, String> word_level_map) {
		this.word_level_map = word_level_map;
	}

	public Map<String, String> getPlace_nick_name_map() {
		return place_nick_name_map;
	}

	public void setPlace_nick_name_map(Map<String, String> place_nick_name_map) {
		this.place_nick_name_map = place_nick_name_map;
	}
	
	public Set<String> getAll_place_mainName() {
		return all_place_name;
	}
	
	public void setAll_place_mainName(Set<String> all_place_mainName) {
		this.all_place_name = all_place_mainName;
	}

	public boolean isUseCustomDic() {
		return isUseCustomDic;
	}

	public void setUseCustomDic(boolean isUseCustomDic) {
		this.isUseCustomDic = isUseCustomDic;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean istIitleContainCustomWord() {
		return isTitleContainCustomWord;
	}

	public void setIitleContainCustomWord(boolean isTitleContainCustomWord) {
		this.isTitleContainCustomWord = isTitleContainCustomWord;
	}

	public boolean isContentContainCustomWord() {
		return isContentContainCustomWord;
	}

	public void setContentContainCustomWord(boolean isContentContainCustomWord) {
		this.isContentContainCustomWord = isContentContainCustomWord;
	}

	public Set<String> getTitle_place_name() {
		return title_place_name;
	}

	public void setTitle_place_name(Set<String> title_place_name) {
		this.title_place_name = title_place_name;
	}

	public ArrayList<String> getTextClass() {
		return textClass;
	}

	public void setTextClass(ArrayList<String> textClass) {
		this.textClass = textClass;
	}
	
	public Set<String> getTitleHighWords() {
		return title_highW_words;
	}
}
