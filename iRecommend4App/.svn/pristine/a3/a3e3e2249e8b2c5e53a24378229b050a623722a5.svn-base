package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <PRE>
 * 作用 : 
 *   段落类，段落组成文章
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
public class Section
{
    private List<Sentence> sentenceList;
    private int sectionIndex;
    
    public Section(String sectionStr, int sectionIndex) {
    	this.sectionIndex = sectionIndex;
    	
    	if (null == sectionStr || sectionStr.length() <= 4) {
    		this.sentenceList = null;
    	} else {		
    		this.sentenceList = new ArrayList<Sentence>();
    
    		int sentenceIndex = 0;
    		
    		// 先分割成句子
    		Pattern pattern = Pattern.compile(".+?((。_w)|(！_w)|(？_w))");
			Matcher matcher = pattern.matcher(sectionStr);
			// 如果没有匹配的，也是单独一段
			if (!matcher.find()) {
				Sentence st = new Sentence(sectionStr, sentenceIndex);
				this.sentenceList.add(st);
		    }
			
			// 回到起点
			matcher.reset();
	        while (matcher.find()) {
	        	String param = matcher.group();
	        	Sentence st = new Sentence(param, sentenceIndex);
				this.sentenceList.add(st);
				
				sentenceIndex++;
	        }
    	}
    }
    
    public Section(List<Sentence> sentenceList, int sectionIndex) {
        this.setSentenceList(sentenceList);
        this.setSectionIndex(sectionIndex);
    }
    
	public List<Sentence> getSentenceList() {
		return sentenceList;
	}

	public void setSentenceList(List<Sentence> sentenceList) {
		this.sentenceList = sentenceList;
	}

	public int getSectionIndex() {
		return sectionIndex;
	}

	public void setSectionIndex(int sectionIndex) {
		this.sectionIndex = sectionIndex;
	}
}
