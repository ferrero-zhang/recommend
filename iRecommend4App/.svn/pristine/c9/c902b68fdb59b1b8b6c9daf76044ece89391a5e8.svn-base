package com.ifeng.iRecommend.wuyg.commonData.Update;

import com.ifeng.commen.Utils.LoadConfig;

/**
 * 
 * <PRE>
 * 作用 : 
 *    订阅通用数据详细字段信息。每个词语的基本信息存储  
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
 *          1.0          2015年12月18日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class CommonSubDataWord {
	/**
	 * value的类型：
	 *     <br>hotWord
	 *     <br>wordReadable
	 *     <br>keyWord
	 *     <br>...
	 */
	protected String wordType;
	/**
	 * value的状态(目前只有两种状态)：
	 *     <br>read
	 *     <br>unread
	 *     ...
	 */
	protected String wordState;
	/**
	 * 词语本身
	 */
	protected String value;
	/**
	 * value的时间，为该词以后是否删除做判断
	 */
	protected String time;
	
	
	public CommonSubDataWord(String wordType, String wordState, String value, String time){
		setWordType(wordType);
		setWordState(wordState);
		setValue(value);
		setTime(time);
	}

	public String getWordType() {
		return wordType;
	}

	public void setWordType(String wordType) {
		this.wordType = wordType;
	}

	public String getWordState() {
		return wordState;
	}

	public void setWordState(String wordState) {
		this.wordState = wordState;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	

	@Override
	public String toString() {
        StringBuffer sBuffer = new StringBuffer();
        if(null !=wordType){
        	sBuffer.append(getWordType()+LoadConfig.lookUpValueByKey("fieldDelimiter"));
        }
        if(null != wordState){
        	sBuffer.append(getWordState()+LoadConfig.lookUpValueByKey("fieldDelimiter"));
        }
        if(null != value){
        	sBuffer.append(getValue());
        }
        if(null != time){
        	sBuffer.append(LoadConfig.lookUpValueByKey("fieldDelimiter")+getTime());
        }
		return sBuffer.toString();
	}
	/**
	 * 
	* @Title:isreadCheck
	* @Description:检查该词条是否为可读
	* @param info
	* @return
	* @author:wuyg1
	* @date:2015年12月31日
	 */
	public static boolean isreadCheck(String info){
		String [] strs = info.split(LoadConfig.lookUpValueByKey("fieldDelimiter"));
		boolean tag = true;
		if(strs[1].equals(WordState.unread.name())){
			tag = false;
		}
		return tag;
	}



	public enum WordState {
		read, // 可读状态(默认状态)
		unread;// 不可读状态
	}
	public enum ArticleSouceState{
		soft,//软性稿源
		serious,//严肃稿源
		rubbish;//垃圾稿源
	}
}
