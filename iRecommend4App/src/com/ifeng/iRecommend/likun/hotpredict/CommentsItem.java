/**
 * 
 */
package com.ifeng.iRecommend.likun.hotpredict;

import com.ifeng.iRecommend.dingjw.itemParser.Item;

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
 *          1.0          2015年1月12日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class CommentsItem {
	private String id;
	private int commentsNum;
	private String channel;
	private int pv;
	private long creatTime;
	private Item item;
	private double score;
	private String type;
	private String url;
	//test
	private double lifetime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getCommentsNum() {
		return commentsNum;
	}
	public void setCommentsNum(int commentsNum) {
		this.commentsNum = commentsNum;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public int getPv() {
		return pv;
	}
	public void setPv(int pv) {
		this.pv = pv;
	}
	public long getCreatTime() {
		return creatTime;
	}
	public void setCreatTime(long creatTime) {
		this.creatTime = creatTime;
	}
	public Item getItem() {
		return item;
	}
	public void setItem(Item item) {
		this.item = item;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public double getLifetime() {
		return lifetime;
	}
	public void setLifetime(double lifetime) {
		this.lifetime = lifetime;
	}
}
