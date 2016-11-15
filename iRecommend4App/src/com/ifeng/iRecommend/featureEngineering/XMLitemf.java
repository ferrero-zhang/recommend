package com.ifeng.iRecommend.featureEngineering;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class XMLitemf extends itemf
{
	@Expose
	@SerializedName("exFeatures")
	private ArrayList<String> ex_features;	
	public XMLitemf(){
		ex_features = new ArrayList<String>();
	}
	
	public ArrayList<String> getExFeatures()
	{
		return this.ex_features;
	}
	
	/**
	 * 加入一个feature表达到itemf；
	 * 
	 * 注意：
	 * 	 
	 * @param feature 
	 * @param type feature的类型:"c" "sc" "cn" "t" "s",分布对应：一级分类、二级分类、栏目专题事件、topic、稿源source
	 * @param weight feature在item中的权重
	 * 
	 */
	public void addExFeatures(String exfeature,String type,String weight){
		this.ex_features.add(exfeature);
		this.ex_features.add(type);
		this.ex_features.add(weight);
	}
	
	public void addExFeatures(String s){
		this.ex_features.add(s);
	}
	
	public void addExFeatures(ArrayList<String> tagList){
		this.ex_features.clear();
		this.ex_features.addAll(tagList);
	}
	
	public void setExFeatures(ArrayList<String> exfeatureList){
		if(exfeatureList != null)
		this.ex_features = exfeatureList;
	}
}
