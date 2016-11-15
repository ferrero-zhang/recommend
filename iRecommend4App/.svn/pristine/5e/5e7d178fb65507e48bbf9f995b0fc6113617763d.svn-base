package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <PRE>
 * 作用 : 
 *   句子类，句子组成段落
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
public class Sentence {
	private List<String> wordList;
	private int sentenceIndex;
	private String splitSentence;
	
    public Sentence(List<String> wordList, int sentenceIndex)
    {
        this.setWordList(wordList);
        this.setSentenceIndex(sentenceIndex);
        this.setSplitSentenceStr("");
    }
    
    public Sentence(String splitSentenceStr, int sentenceIndex) {
    	this.setSplitSentenceStr(splitSentenceStr);
    	this.setSentenceIndex(sentenceIndex);
    	if (null == splitSentenceStr || splitSentenceStr.length() <= 3) {
    		this.wordList = null;
    	} else {
    		this.wordList = new ArrayList<String>();
        	
        	// Pattern pattern = Pattern.compile("([^\\s]+_[a-z]+)|([^\\s_w]+\\s+[^\\s]+_[a-z]+)");
    		Pattern pattern = Pattern.compile("([^\\s_]+\\s*\\S+_[a-z]+)|(\\s*_[a-z])|(\\S+_[a-z]+)");
            Matcher matcher = pattern.matcher(splitSentenceStr);
            
            // 匹配不上
            if (!matcher.find()) {
            	this.wordList.add(splitSentenceStr);  
            }
            
            // 回到起点
            matcher.reset();
            while (matcher.find()) {
               String param = matcher.group();
               if (param.startsWith("http") || param.length() >= 20) {
            	   continue;
               }
               
               this.wordList.add(param);        
            }
    	}
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	if (null == this.wordList || this.wordList.isEmpty()) {
    		return null;
    	}
    	
    	for (String tempWord : this.wordList) {
    		sb.append(tempWord + " ");
    	}
    	
    	return sb.toString();
    }

	public List<String> getWordList() {
		return wordList;
	}

	public void setWordList(List<String> wordList) {
		this.wordList = wordList;
	}

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	public void setSentenceIndex(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}
	
	public String getSplitSentenceStr() {
		return splitSentence;
	}

	public void setSplitSentenceStr(String splitSentence) {
		this.splitSentence = splitSentence;
	}
	
	public static void main(String[] args) {
		String test = "_w 习近平_x 会见_v 昂山素季_x 。_w Windows 10_x 开始_k 菜单_n 回归_v  _w 拿下_v 设计界_n 和_c 奥斯卡_x iPad mini4_x";
		String test2 = "华为_x 将_d 推_v 新_d 荣耀_a 手机_x ：_w 5_mq 吋_x 屏_k 八_m 核_k  _w 1396元_mq 起_k";
		String test3 = "_k 习近平_x 同志_n 指出_v";
		
		Pattern pattern = Pattern.compile("([^\\s_]+\\s*\\S+_[a-z]+)|(\\s*_[a-z])|(\\S+_[a-z]+)");
        Matcher matcher = pattern.matcher(test);
        
        while (matcher.find()) {
            String param = matcher.group();
            System.out.println(param);
         }
	}
}
