package com.ifeng.iRecommend.featureEngineering.freshProject;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;

/**
 * 
 * <PRE>
 * 作用 : 
 *   Fresh Item的表达类；
 *   1）item的基本信息：继承了itemf
 *   2）新增了rank（文章等级）和life（生存周期）
 * 使用 : 
 *   
 * 示例 :
 * 
 * 注意 :
 *   来源：足够优质和垂直的来源才会做出表达，一般情况下表达为空；
 *   特征的表达，需要给出在当前item中的权重[0-1]；（初始可以默认为1）
 *   
 *   feature的类型:"c"一级分类（足球，篮球）， "sc"二级分类（中国足球）， "cn"专题事件， "t"主题词， "s"稿源，  "s1" 少量优质栏目 
 *   			   "et"实体库词， "kb"书名号中的词（游戏名，书名，电影名等）， "ks"冒号前的词（发言人，地区等） "kq"引号中的词（特指，特定词），
 *   			   "kr"分词得到的人名，自定义实体词 "kl"分词得到的地名  
 *   
 *   
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
 *          1.0          2015-11-3         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class FreshItemf extends itemf{
	@Expose
	@SerializedName("rank")
	private String rank;			//文章等级
	
	@Expose
	@SerializedName("life")
	private float life;			//生存周期
	
	public String getRank() 
	{
		return this.rank;
	}
	public void setRank(String rank)
	{
		this.rank = rank;
	}

	public float getLife() 
	{
		return this.life;
	}
	public void setLife(float life)
	{
		this.life = life;
	}
}
