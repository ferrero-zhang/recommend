package com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
/**
 * 
 * <PRE>
 * 作用 : 
 *    文章抽取得到的时间戳信息集合  
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
 *          1.0          2016年8月18日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class ArticleTimeInfo {
	/**
	 * 文章ID
	 */
	String id = new String();
	/**
	 * 文章标题
	 */
	String title = new String();
	// Key为段落，HashSet为该段落中的时间戳词
	/**
	 * 精确时间戳
	 */
	HashMap<Integer, HashSet<String>> dateStampMap = new HashMap<Integer, HashSet<String>>();
	/**
	 * 节日
	 */
	HashMap<Integer, HashSet<String>> festivalStampMap = new HashMap<Integer, HashSet<String>>();
	/**
	 * _t时间戳
	 */
	HashMap<Integer, HashSet<String>> timestampMap = new HashMap<Integer, HashSet<String>>();
	/**
	 * 文章电讯的时间
	 */
	HashSet<String> reportTimeSet = new HashSet<String>();
    /**
     * 文章发布时间
     */
	String publishedTime = new String();
	/**
	 * 文章所属领域
	 */
	HashSet<String> categorySet = new HashSet<String>();
	
	/**
	 * 常见时间指示性词语
	 */
    HashSet<String> timeIndicateWordSet = new HashSet<String>();
    /**
     * 以该集合的时间词作为段落开始的
     */
    HashMap<Integer,HashSet<String>> timeStartWordMap = new HashMap<Integer,HashSet<String>>();
	/**
	 * 该字段专门用于存储几点几分这样的具体时间，用于几个小时的推算
	 */
    HashMap<Integer, HashSet<String>> clockMap = new HashMap<Integer,HashSet<String>>();
    
    String firstSeg = new String();
    /**
     * 时效性的Level
     */
    TimeSensitiveLevel timeSensitiveLevel = TimeSensitiveLevel.UNVALID;
    /**
     * 文章有效时间长度，此处主要记载的是天数的间隔，时分的间隔不记载
     */
    int timeValidDate = -1;
    /**
     * 文章的小时有效时间
     */
    int timeValidHour = -1;
    
	
	public String getFirstSeg() {
		return firstSeg;
	}

	public void setFirstSeg(String firstSeg) {
		this.firstSeg = firstSeg;
	}

	public HashMap<Integer, HashSet<String>> getDateStampMap() {
		return dateStampMap;
	}

	public void setDateStampMap(HashMap<Integer, HashSet<String>> dateStampMap) {
		this.dateStampMap = dateStampMap;
	}

	public HashMap<Integer, HashSet<String>> getFestivalStampMap() {
		return festivalStampMap;
	}

	public void setFestivalStampMap(
			HashMap<Integer, HashSet<String>> festivalStampMap) {
		this.festivalStampMap = festivalStampMap;
	}

	public HashMap<Integer, HashSet<String>> getTimestampMap() {
		return timestampMap;
	}

	public void setTimestampMap(HashMap<Integer, HashSet<String>> timestampMap) {
		this.timestampMap = timestampMap;
	}

	public HashSet<String> getReportTimeSet() {
		return reportTimeSet;
	}

	public void setReportTimeSet(HashSet<String> reportTimeSet) {
		this.reportTimeSet = reportTimeSet;
	}

	public String getPublishedTime() {
		return publishedTime;
	}

	public void setPublishedTime(String publishedTime) {
		this.publishedTime = publishedTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public HashSet<String> getCategorySet() {
		return categorySet;
	}

	public void setCategorySet(HashSet<String> categorySet) {
		this.categorySet = categorySet;
	}

	public HashSet<String> getTimeIndicateWordSet() {
		return timeIndicateWordSet;
	}

	public void setTimeIndicateWordSet(HashSet<String> timeIndicateWordSet) {
		this.timeIndicateWordSet = timeIndicateWordSet;
	}

	public HashMap<Integer,HashSet<String>> getTimeStartWordMap() {
		return timeStartWordMap;
	}

	public void setTimeStartWordMap(HashMap<Integer,HashSet<String>> timeStartWordMap) {
		this.timeStartWordMap = timeStartWordMap;
	}

	public HashMap<Integer, HashSet<String>> getClockMap() {
		return clockMap;
	}

	public void setClockMap(HashMap<Integer, HashSet<String>> clockMap) {
		this.clockMap = clockMap;
	}

	public TimeSensitiveLevel getTimeSensitiveLevel() {
		return timeSensitiveLevel;
	}

	public void setTimeSensitiveLevel(TimeSensitiveLevel timeSensitiveLevel) {
		this.timeSensitiveLevel = timeSensitiveLevel;
	}

	public int getTimeValidDate() {
		return timeValidDate;
	}

	public void setTimeValidDate(int timeValidDate) {
		this.timeValidDate = timeValidDate;
	}

	public int getTimeValidHour() {
		return timeValidHour;
	}

	public void setTimeValidHour(int timeValidHour) {
		this.timeValidHour = timeValidHour;
	}
}
