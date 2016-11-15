/**
 * item with features
 * item的特征表达类；包含item基本信息以及特征表达信息
 */
package com.ifeng.iRecommend.featureEngineering.dataStructure;

import java.text.ParseException;
import java.util.ArrayList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * <PRE>
 * 作用 : 
 *   item的表达类；
 *   1）item的基本信息：ID、title 、url、分词后title、分词后content信息
 *   2）item的特征表达信息：一级分类、二级分类、栏目或者专题事件、topic、keywords实体、稿源（足够优质的来源才会做出表达）；
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
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年4月8日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */


public class itemf{
	@Expose
	@SerializedName("ID")
	private String id;			//ID
	@Expose
	@SerializedName("newcmppid")
	private String newcmppid;			//newcmppid
	@Expose
	@SerializedName("subid")
	private String subid;			//subid
	@Expose
	@SerializedName("zmtid")
	private String zmtid;			//zmtid
	@Expose
	@SerializedName("url")
	private String url;			//url
	@Expose
	@SerializedName("title")
	private String title;			//标题
	@Expose
	@SerializedName("splitTitle")
	private String splitTitle;		//分词后的标题
	@Expose
	@SerializedName("splitContent")
	private String splitContent;			//分词后的内容
	@Expose
	@SerializedName("publishedTime")
	private String publishedTime;	//发布时间
	@Expose
	@SerializedName("source")
	private String source;	//发布来源
	@Expose
	@SerializedName("appId")
	private String appId;//这个item所对应的应用，如newspush、ifengapp等
	@Expose
	@SerializedName("docType")
	private String docType ;//item类型，如slide,doc,video
	@Expose
	@SerializedName("showStyle")
	private String showStyle ;//前端显示样式，支持从cmpp后端人工控制
	@Expose
	@SerializedName("modifyTime")
	private long modifyTime;//标识feature修改的最后时间
	@Expose
	@SerializedName("other")
	private String other;			//其他
	
	public String getID() 
	{
		return this.id;
	}
	public void setID(String id)
	{
		this.id = id;
	}
	
	public String getNewcmppid() 
	{
		return this.newcmppid;
	}
	public void setNewcmppid(String newcmppid)
	{
		this.newcmppid = newcmppid;
	}
	
	public String getSubid() 
	{
		return this.subid;
	}
	public void setSubid(String subid)
	{
		this.subid = subid;
	}
	
	public String getZmtid() 
	{
		return this.zmtid;
	}
	public void setZmtid(String zmtid)
	{
		this.zmtid = zmtid;
	}
	
	public String getUrl()
	{
		return this.url;
	}
	public void setUrl(String url)
{
		this.url = url;
	}
	
	public String getTitle() 
	{
		return this.title;
	}
	public void setTitle(String title) 
	{
		this.title = title;
	}
	
	public String getSplitTitle()
	{
		return this.splitTitle;
	}
	public void setSplitTitle(String splitTitle) 
	{
		this.splitTitle = splitTitle;
	}
	
	public String getSplitContent() 
	{
		return this.splitContent;
	}
	public void setSplitContent(String splitContent) 
	{
		this.splitContent = splitContent;
	}
	
	public String getPublishedTime()
	{
		return this.publishedTime;
	}
	public void setPublishedTime(String publishedTime) 
	{
		this.publishedTime = publishedTime;
	}
	
	public String getSource()
	{
		return this.source;
	}
	public void setSource(String source) 
	{
		this.source = source;
	}
	
	public String getAppId() 
	{
		return this.appId;
	}
	public void setAppId(String appId) 
	{
		this.appId = appId;
	}
	
	public String getDocType() 
	{
		return this.docType;
	}
	public void setDocType(String docType) 
	{
		this.docType = docType;
	}
	
	public String getShowStyle() 
	{
		return this.showStyle;
	}
	public void setShowStyle(String showStyle) 
	{
		this.showStyle = showStyle;
	}
	
	public long getModifyTime() {
		return this.modifyTime;
	}
	public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}
	public void setModifyTimeCurrent(){
		this.modifyTime = System.currentTimeMillis();
	}
	public void setModifyTimeDate(String date){//把一定格式的时间字符串转换成long并设置为修改时间
		try {
			this.modifyTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String displayModifyTimeToDate(){//返回字符串格式化的修改时间
		String ModifyDate = new java.text.SimpleDateFormat("yyyy-MM-dd#HH:mm:ss").format(new java.util.Date(this.modifyTime));
		return ModifyDate;
	}
	
	public String getOther() {
		return this.other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	
	//tags解析的结果
	@Expose
	@SerializedName("tags")
	private ArrayList<String> tags;
	public ArrayList<String> getTags()
	{
		return this.tags;
	}
	public void setTags(ArrayList<String> tagsList){
//		this.al_features.clear();
		this.tags = tagsList;
	}
	//从features中提取的分类特征
	@Expose
	@SerializedName("category")
	private ArrayList<String> category;
	public ArrayList<String> getCategory()
	{
		return this.category;
	}
	public void setCategory(ArrayList<String> categoryList){
//		this.al_features.clear();
		this.category = categoryList;
	}
	//热点事件、热点词汇
	@Expose
	@SerializedName("hotEvent")
	private ArrayList<String> hotEvent;
	public ArrayList<String> getHotEvent()
	{
		return this.hotEvent;
	}
	public void setHotEvent(ArrayList<String> hotEventList){
//		this.al_features.clear();
		this.hotEvent = hotEventList;
	}
	//特征表达：三个一组顺序构成数组，也即feature1 type weight feature2 type weight... 
	//按一级分类、二级分类、栏目专题事件、topic顺序排列
	@Expose
	@SerializedName("features")
	private ArrayList<String> al_features;		
	
	
	public itemf(){
		al_features = new ArrayList<String>();
		tags = new ArrayList<String>();
		category = new ArrayList<String>();
		hotEvent = new ArrayList<String>();
	}
	
	public ArrayList<String> getFeatures()
	{
		return this.al_features;
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
	public void addFeatures(String feature,String type,String weight){
		this.al_features.add(feature);
		this.al_features.add(type);
		this.al_features.add(weight);
	}
	
	public void addFeatures(String s){
		this.al_features.add(s);
	}
	
	public void addFeatures(ArrayList<String> tagList){
		this.al_features.clear();
		this.al_features.addAll(tagList);
	}
	
	public void setFeatures(ArrayList<String> featureList){
		this.al_features = featureList;
	}
}
