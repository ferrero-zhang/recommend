package com.ifeng.iRecommend.zxc.bdhotword.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 匹配到的百度热词信息
 * **/
public class BDHotWordBean {
	public static String TYPE_EVENT="e";
	public static String TYPE_ENTITY="et";
	public static String TYPE_UNKNOWN="x";
	//热词内容
	private String str;
	private String splitStr;
	//热词类型:entity 或者 event
	private String type;
	//置信度
	private double score;
	//出现的次数
	private int exsitCount;
	//是否出现在标题
	private boolean exsitInTitle;

	private double wordExsitRatio;
	private List<String> sameEventNames=new ArrayList<String>();
	public void setScore(double score) {
		this.score = score;
	}
	public void addSameEventName(String name){
		sameEventNames.add(name);
	}
	
	public String getSplitStr() {
		return splitStr;
	}
	public void setSplitStr(String splitStr) {
		this.splitStr = splitStr;
	}
	public List<String> getSameEventNames() {
		return sameEventNames;
	}

	public static BDHotWordBean clone(BDHotWordBean bean){
		BDHotWordBean rtb=new BDHotWordBean(bean.str,bean.type ,bean.score);
		return rtb;
	}
	public void setType(String type) {
		this.type = type;
	}

	public void setStr(String str) {
		this.str = str;
	}

	private int wordExsitCount;

	public double getWordExsitRatio() {
		return wordExsitRatio;
	}

	public int getWordExsitCount() {
		return wordExsitCount;
	}


	public BDHotWordBean(String str, String type, int wordExsitCount,
			double wordExsitRatio) {
		super();
		
		this.str = str;
		this.type = type;
		
		this.wordExsitCount = wordExsitCount;
		
		this.wordExsitRatio=wordExsitRatio;
	}
	
	public BDHotWordBean(String str, String type, double score) {
		super();

		this.str = str;
		this.type = type;
		this.score = score;
		
	}
	public BDHotWordBean(String str, String type, double score, int exsitCount,
			boolean exsitInTitle) {
		super();

		this.str = str;
		this.type = type;
		this.score = score;
		this.exsitCount = exsitCount;
		this.exsitInTitle = exsitInTitle;
	}
	public String getStr() {
		return str;
	}

	public String getType() {
		return type;
	}
	public double getScore() {
		return score;
	}

	public int getExsitCount() {
		return exsitCount;
	}
	public boolean isExsitInTitle() {
		return exsitInTitle;
	}
	@Override
	public boolean equals(Object obj){
		if(this==obj){
			return true;
		}
		if(obj instanceof BDHotWordBean){
			BDHotWordBean that=(BDHotWordBean) obj;
			if(that.str.equals(this.str)){
				return true;
			}
		}
		return false;
	}
	@Override
	public int hashCode(){
		return this.str.hashCode();
	}
	public void setExsitCount(int exsitCount) {
		this.exsitCount = exsitCount;
	}
	
}
