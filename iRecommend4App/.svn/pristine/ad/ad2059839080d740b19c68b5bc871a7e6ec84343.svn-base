/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.newHotPredict;

import java.util.ArrayList;

import com.ifeng.iRecommend.featureEngineering.dataStructure.*;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;

/**
 * <PRE>
 * 作用 : 热点数据缓存结构
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
 *          1.0          2015年8月18日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class CacheHotRankItem{
	private String docId;
	private String title;
	private String url;
	private int pv;
	private String docChannel;
	private double hotScore;
	private String hotLevel;
	private int commentsNum;
	private String publishTime;
	private String docType;
	private String why;
	private String readableFeatures;
	private FrontNewsItem frontNewsItem;

	//存储用于热度排序的最高得分
	private double maxScore;
	public CacheHotRankItem(HotRankItem hot){
		docId = hot.getDocId();
		title = hot.getTitle();
		url = hot.getUrl();
		pv = hot.getPv();
		docChannel = hot.getDocChannel();
		hotScore = hot.getHotScore();
		hotLevel = hot.getHotLevel();
		commentsNum = hot.getCommentsNum();
		publishTime = hot.getPublishTime();
		docType = hot.getDocType();
		why = hot.getWhy();
		maxScore = hot.getHotScore();
		frontNewsItem = hot.getFrontItem();
		itemf item = hot.getItem();
		if(item == null){
			readableFeatures = "";
		}else{
			//计算可读化标签并放入others字段中；遍历其features字段，将c按权重放入topic1 sc放入topic2 cn和t放入topic3
			StringBuffer sbTmp = new StringBuffer();
			ArrayList<String> al_features = item.getFeatures();
			if(al_features.size()%3 == 0){
				for(int i=0;i<al_features.size();i+=3){
					String feature = al_features.get(i);
					String type = al_features.get(i+1);
					float weight = 0f;
					try{
						weight = Float.valueOf(al_features.get(i+2));
					}catch(Exception e){
						weight = 0f;
						e.printStackTrace();
					}
					//不可读，暂时不加入可读化表达
					if(weight <= 0)
						continue;
					if(type.equals("c")
							||type.equals("sc")
							||type.equals("cn")
							||type.equals("t")
							||type.equals("e")
							||type.equals("s1")
							||type.equals("et")){
						sbTmp.append(type).append("=").append(feature).append("|!|");
					}
					if(type.equals("kb")){
						sbTmp.append(type).append("=《").append(feature).append("》|!|");
					}
				}
			}
			readableFeatures = sbTmp.toString();
		}
	}
	//用于记录该条热点新闻历史上最高热度得分
	public void update(CacheHotRankItem hot){
		if(hot.getHotScore() > this.maxScore){
			this.maxScore = hot.getHotScore();
		}
		FrontNewsItem fitem = hot.getFrontNewsItem();
		if(fitem != null && fitem.getOthers()!=null){
			this.frontNewsItem.setOthers(fitem.getOthers());
		}
		
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
	public String getPublishTime() {
		return publishTime;
	}
	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	public String getWhy() {
		return why;
	}
	public void setWhy(String why) {
		this.why = why;
	}
	public String getReadableFeatures() {
		return readableFeatures;
	}
	public void setReadableFeatures(String readableFeatures) {
		this.readableFeatures = readableFeatures;
	}
	public double getMaxScore() {
		return maxScore;
	}
	public void setMaxScore(double maxScore) {
		this.maxScore = maxScore;
	}
	public FrontNewsItem getFrontNewsItem() {
		return frontNewsItem;
	}
	public void setFrontNewsItem(FrontNewsItem frontNewsItem) {
		this.frontNewsItem = frontNewsItem;
	}
}
