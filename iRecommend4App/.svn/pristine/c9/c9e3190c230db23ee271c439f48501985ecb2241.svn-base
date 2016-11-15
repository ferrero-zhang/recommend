package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.CustomWordUpdate;
import com.ifeng.iRecommend.liuyi.customDicWordSearch.CustomWordSearcher;

/**
 * <PRE>
 * 作用 : 
 *   新闻文章类
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   新闻文章由段落构成、段落由句子构成
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年10月10日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class Document
{
	private List<String> title_wordList;    
    private List<String> content_wordList;
    
    private List<Section> sectionList;
    
    public Document(List<String> title_wordList, List<Section> sectionList) {
    	this.setTitle_wordList(title_wordList);
    	this.setSectionList(sectionList);
    }

    public Document(String splitTitle, String splitContent) {
    	if (null == splitTitle || splitTitle.isEmpty()) {
    		this.setTitle_wordList(null);
    	} else {
    		Sentence st = new Sentence(splitTitle, 0);
    		this.setTitle_wordList(st.getWordList());
    	}
    	
    	if (null == splitContent || splitContent.isEmpty()) {
    		this.sectionList = null;
    	} else {
    		this.sectionList = new ArrayList<Section>();
    		
    		int sectionIndex = 0;
    		String[] section = splitContent.split("\n");
    		for (String tempSecStr : section) {
    			Section tempSec = new Section(tempSecStr, sectionIndex);
    			this.sectionList.add(tempSec);
    			sectionIndex++;
    		}
    		
    		this.content_wordList = new ArrayList<String>();    		
    		for (Section sc : this.sectionList) {
    			if(null == sc|| null == sc.getSentenceList()) {
					continue;
    			}
    			
    			for (Sentence st : sc.getSentenceList()) {
    				if (null == st || null == st.getWordList()) {
    					continue;
    				}
    				
    				this.content_wordList.addAll(st.getWordList());
    			}
    		}
    	}
    }
    
    public void setSectionList(List<Section> secList) {
    	this.sectionList = secList;
    }
    
    public List<Section> getSectionList() {
    	return this.sectionList; 
    }
    
    public Section getSectionByIndex(int sectionIndex) {
    	if (sectionIndex >= this.getDocSecSize()) {
    		return null;
    	} else {
    		return this.sectionList.get(sectionIndex);
    	}
    }
    
    public int getDocSecSize() {
    	if (null == this.sectionList) {
    		return 0;
    	} else {
    		return this.sectionList.size();
    	}
    }
    
    public List<String> getTitle_wordList() {
		return title_wordList;
	}

	public void setTitle_wordList(List<String> title_wordList) {
		this.title_wordList = title_wordList;
	}
    
	public List<String> getContentWordList() {
		return content_wordList;
	}

	public void setContentWordList(List<String> wordList) {
		this.content_wordList = wordList;
	}

	public static void main(String[] args) {
		// 测试自定义词典
		CustomWordUpdate cwu = CustomWordUpdate.getInstance();
		
		EntityLibQuery.init();
		//queryInterface query = queryInterface.getInstance();
		//XMLQueryInterface xmlQuery = XMLQueryInterface.getInstance(); 
		//itemf item = xmlQuery.queryItemF("107927064"); 
		//itemf item = query.queryItemF("107927064");
		
		String tablename = "appitemdb";
		IKVOperationv2 ob = new IKVOperationv2(tablename);
		itemf item = ob.queryItemF("148190","c");
		
		String splitContent = item.getSplitContent();
		String splitTitle = item.getSplitTitle();
//		String test = "近日，安徽芜湖一个小区的居民楼的楼道突发火灾，并且有人员被困，消防部门迅速赶赴现场处置。";
//		String splitContent = new String(SplitWordClient.split(test.replace("✿", "").replace("•", ""), null).replace("(/", "_").replace(") ", " "));;
//		String splitTitle = "";
		System.out.println(splitContent);
		Document doc = new Document(splitTitle, splitContent);
		System.out.println(item.getTitle());
		System.out.println(item.getSplitTitle());
		System.out.println(SpecialWordExtract.scnt2cnt(splitTitle));

		ArrayList<String> classList = new ArrayList<String>();
		classList.add("时政");
		KeywordExtract ke = new KeywordExtract(true, splitTitle, splitContent, null);
		
		Document filtered_doc = ke.get_filtered_doc().getFilteredDoc();
		List<Section> sc = filtered_doc.getSectionList();
		for (Section tempSc : sc) {
			List<Sentence> ls = tempSc.getSentenceList();
			System.out.println("[SectionIndex]:" + tempSc.getSectionIndex());
			for (Sentence st : ls) {
				System.out.println("\t [SentenceIndex]:"
						+ st.getSentenceIndex());
				System.out.println("\t" + st.toString());
			}
		}
		
		Map<String, Set<String>> et_class_map = ke.get_filtered_doc().getEt_class_map();
		System.out.println("getTitle_wordList:" + ke.get_filtered_doc().getDoc().getTitle_wordList());
		for (String tempWord : et_class_map.keySet()) {
			System.out.println(tempWord + " " + et_class_map.get(tempWord).toString());
		}
		
		Map<String, Double> sort_counter = ke.sortMapByValue("desc", ke.get_filtered_doc().get_word_weighted_frequency());
		for (String tempKey : sort_counter.keySet()) {
			System.out.println(tempKey + " --> " + sort_counter.get(tempKey));
		}
		
		System.out.println("word distribute...");
		Map<String, List<Integer>> word_dis = ke.get_filtered_doc().getWordDistribute();
		for (String tempWord : word_dis.keySet()) {
			System.out.print(tempWord + "--->");
			List<Integer> disList = word_dis.get(tempWord);
			for (Integer dis : disList) {
				System.out.print(dis + " ");
			}
			System.out.println();
		}

		List<String> keywords = ke.get_weighted_keywords();
		int featuresNum = keywords.size()/3;
		for (int i = 0; i != featuresNum; i++) {
			int featureValueIndex = i * 3;
			int featureTagIndex = (i * 3) + 1;
			int weightIndex = (i * 3) + 2;
			System.out.println(keywords.get(featureValueIndex) + "-->" + keywords.get(featureTagIndex) + "-->" + keywords.get(weightIndex));
		}
		
		ob.close();
	}
}
