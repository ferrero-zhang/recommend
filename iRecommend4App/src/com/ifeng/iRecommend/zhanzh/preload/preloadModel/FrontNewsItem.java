/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.preloadModel;

import java.util.ArrayList;
import java.util.List;

import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.zhanzh.newHotPredict.HotRankItem;



/**
 * <PRE>
 * 作用 : 同步给前端使用的数据结构
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

public class FrontNewsItem {
	private String docId;
	private String id_value;
	private String simId; //保存相似新闻simID
	private String title;
	private String date;
	private String hotLevel;
	private String docChannel;
	private String why;
	private double score;
	private double hotBoost;
	private String docType;
	private String doc_type;
	private String readableFeatures;
	private String others;
	private String cardType;
	
	private String isAvailable; 


	private String showStyle;
	private String openway;
	private String tips;

	private String[] keywords;
	private List<String> images;
    private String docContent;
	
	public FrontNewsItem(){
		
	}
	
	public FrontNewsItem(HotRankItem hot){
		docId = hot.getDocId();
		title = hot.getTitle();
		date = hot.getPublishTime();
//		hotLevel = hot.getHotLevel();
		docType = hot.getDocType();
		docChannel = hot.getDocChannel();
		why = hot.getWhy();
		itemf item = hot.getItem();
		if(item == null){
			readableFeatures = "";
		}else{
			//test 统一用itemTitle
//			title = item.getTitle();
			
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
	
	public FrontNewsItem(itemf item){
		docId = item.getID();
		title = item.getTitle();
		date = item.getPublishedTime();
//		hotLevel = hot.getHotLevel();
		docType = item.getDocType();
		
		if(item.getFeatures() == null){
			readableFeatures = "";
		}else{
			//test 统一用itemTitle
//			title = item.getTitle();
			
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
	
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	
	public String getId_value() {
		return id_value;
	}
	public void setId_value(String id_value) {
		this.id_value = id_value;
	}
	
	public String getSimId() {
		return simId;
	}
	public void setSimId(String simId) {
		this.simId = simId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getHotLevel() {
		return hotLevel;
	}
	public void setHotLevel(String hotLevel) {
		this.hotLevel = hotLevel;
	}
	public String getDocChannel() {
		return docChannel;
	}
	public void setDocChannel(String docChannel) {
		this.docChannel = docChannel;
	}
	public String getWhy() {
		return why;
	}
	public void setWhy(String why) {
		this.why = why;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public double getHotBoost() {
		return hotBoost;
	}
	public void setHotBoost(double hotBoost) {
		this.hotBoost = hotBoost;
	}
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	public String getDoc_type() {
		return doc_type;
	}
	public void setDoc_type(String doc_type) {
		this.doc_type = doc_type;
	}
	public String getReadableFeatures() {
		return readableFeatures;
	}
	public void setReadableFeatures(String readableFeatures) {
		this.readableFeatures = readableFeatures;
	}
	public String getOthers() {
		return others;
	}
	public void setOthers(String others) {
		this.others = others;
	}
	
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	
	public String getIsAvailable() {
		return isAvailable;
	}
	public void setIsAvailable(String isAvailable) {
		this.isAvailable = isAvailable;
	}
	public String getShowStyle() {
		return showStyle;
	}
	public void setShowStyle(String showStyle) {
		this.showStyle = showStyle;
	}
	public String getOpenway() {
		return openway;
	}
	public void setOpenway(String openway) {
		this.openway = openway;
	}
	public String getTips() {
		return tips;
	}
	public void setTips(String tips) {
		this.tips = tips;
	}
	public String[] getKeywords() {
		return keywords;
	}
	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}
	public List<String> getImages() {
		return images;
	}
	public void setImages(List<String> images) {
		this.images = images;
	}

	public String getDocContent() {
		return docContent;
	}

	public void setDocContent(String docContent) {
		this.docContent = docContent;
	}
}
