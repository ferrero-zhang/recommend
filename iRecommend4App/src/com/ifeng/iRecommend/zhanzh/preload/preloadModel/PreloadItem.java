/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.preloadModel;


/**
 * <PRE>
 * 作用 : 
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
 *          1.0          2015年11月25日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class PreloadItem {
	private FrontNewsItem fitem ;
	private boolean isRelatedSearchItem;//现在：是否为本次搜索的solr数据     弃用：是否为通过relatedfeatures搜索到的item 
	private float simScore;  //根据solr中查询该tag的相似得分
	private double hotscore;//根据计算得到的热度得分
	private double rankscore;//根据计算得到的排序得分
	private int pv;        //记录对应频道下pv信息
	private int editorScore; //目前是手凤编辑给新闻打的编辑得分
	private String source; //用于存储文章稿源

	
	public PreloadItem(){
		
	}
	public PreloadItem(FrontNewsItem fitem, boolean isRelatedSearchItem,
			float simScore) {
		super();
		this.fitem = fitem;
		this.isRelatedSearchItem = isRelatedSearchItem;
		this.simScore = simScore;
		
	}
	//for wemedia
	public PreloadItem(FrontNewsItem fitem, boolean isRelatedSearchItem,
			float simScore,String source) {
		super();
		this.fitem = fitem;
		this.isRelatedSearchItem = isRelatedSearchItem;
		this.simScore = simScore;
		this.source=source;
		
	}
	public FrontNewsItem getFitem() {
		return fitem;
	}
	public void setFitem(FrontNewsItem fitem) {
		this.fitem = fitem;
	}
	public boolean isRelatedSearchItem() {
		return isRelatedSearchItem;
	}
	public void setRelatedSearchItem(boolean isRelatedSearchItem) {
		this.isRelatedSearchItem = isRelatedSearchItem;
	}
	public float getSimScore() {
		return simScore;
	}
	public void setSimScore(float simScore) {
		this.simScore = simScore;
	}
	public double getHotscore() {
		return hotscore;
	}
	public void setHotscore(double hotscore) {
		this.hotscore = hotscore;
	}
	public double getRankscore() {
		return rankscore;
	}
	public void setRankscore(double rankscore) {
		this.rankscore = rankscore;
	}
	public int getPv() {
		return pv;
	}
	public void setPv(int pv) {
		this.pv = pv;
	}
	public int getEditorScore() {
		return editorScore;
	}
	public void setEditorScore(int editorScore) {
		this.editorScore = editorScore;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
}
