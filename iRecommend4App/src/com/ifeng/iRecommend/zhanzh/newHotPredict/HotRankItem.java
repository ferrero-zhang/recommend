/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.newHotPredict;

import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;




/**
 * <PRE>
 * 作用 : 用来保存热度信息和进行热度排序的数据结构
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
 *          1.0          2015年7月8日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class HotRankItem {
	private String docId;
	private String title;
	private String url;
	private int pv;
	private String docChannel;
	private itemf item;
	private double hotScore;
	private String hotLevel;
	private int commentsNum;
	private long creatTime;
	private long lifeTime;
	private String publishTime;
	private String docType;
	private String why;
	private String readableFeatures;
	

	//只有视觉抓取的新闻才有以下字段
	private boolean isBold;   //是否为粗标题
	private int pageNum;      //新闻在第几级页面中
	private int newsArea;     //新闻在页面区域
	private int newsLocate;   //新闻在区域中的位置
	
	
	//用于保存前端数据结构的变量
	private FrontNewsItem frontItem; 
	
	
	//构造函数
	public HotRankItem(){
		
	}
	
	//
	public HotRankItem(FrontNewsItem frontItem){
		this.docId = frontItem.getDocId();
		this.title = frontItem.getTitle();
		this.frontItem = frontItem;
		this.docChannel = frontItem.getDocChannel();
		this.publishTime = frontItem.getDate();
	}
	
	public FrontNewsItem getFrontItem() {
		return frontItem;
	}
	public void setFrontItem(FrontNewsItem frontItem) {
		this.frontItem = frontItem;
	}
	public String getWhy() {
		return why;
	}
	public void setWhy(String why) {
		this.why = why;
	}
	public boolean isBold() {
		return isBold;
	}
	public void setBold(boolean isBold) {
		this.isBold = isBold;
	}
	public int getPageNum() {
		return pageNum;
	}
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	public int getNewsArea() {
		return newsArea;
	}
	public void setNewsArea(int newsArea) {
		this.newsArea = newsArea;
	}
	public int getNewsLocate() {
		return newsLocate;
	}
	public void setNewsLocate(int newsLocate) {
		this.newsLocate = newsLocate;
	}
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	public String getPublishTime() {
		return publishTime;
	}
	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}
	public long getLifeTime() {
		return lifeTime;
	}
	public void setLifeTime(long lifeTime) {
		this.lifeTime = lifeTime;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getPv() {
		return pv;
	}
	public void setPv(int pv) {
		this.pv = pv;
	}
	public String getDocChannel() {
		return docChannel;
	}
	public void setDocChannel(String docChannel) {
		this.docChannel = docChannel;
	}
	public itemf getItem() {
		return item;
	}
	public void setItem(itemf item) {
		this.item = item;
	}
	public double getHotScore() {
		return hotScore;
	}
	public void setHotScore(double hotScore) {
		this.hotScore = hotScore;
	}
	public String getHotLevel() {
		return hotLevel;
	}
	public void setHotLevel(String hotLevel) {
		this.hotLevel = hotLevel;
	}
	public int getCommentsNum() {
		return commentsNum;
	}
	public void setCommentsNum(int commentsNum) {
		this.commentsNum = commentsNum;
	}
	public long getCreatTime() {
		return creatTime;
	}
	public void setCreatTime(long creatTime) {
		this.creatTime = creatTime;
	}
	public String getReadableFeatures() {
		return readableFeatures;
	}

	public void setReadableFeatures(String readableFeatures) {
		this.readableFeatures = readableFeatures;
	}
}
