/**
 * 
 */
package com.ifeng.iRecommend.dingjw.itemParser;

import java.util.Date;
import java.util.HashMap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * <PRE>
 * 作用 : 
 *   PC端的文章类，来自hbase查询；
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
 *          1.0          2013-7-16        Dingjw          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class Item {
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
	@Expose
	@SerializedName("url")
	private String url;//文章网址，如 http://house.ifeng.com/news/zhengce/detail_2010_12/21/3611659_0.shtml
	
	@Expose
	@SerializedName("id")
	private String id;//文章编号，如 n1_3611659
	
	@Expose
	@SerializedName("title")
	private String title;//文章标题，如 评论_k ：_w 上调_v 存款_v 准备金率_n 还_v 能_v 走_v 多_m 远_k 
	
	@Expose
	@SerializedName("rawKeywords")
	private String rawKeywords;//文章原有关键词，如 存款准备金 准备金 存款 上调存款 存款准备金率
	
	@Expose
	@SerializedName("content")
	private String content;//文章内容，如央行_n 决定_v 从_p 12月_t 20日_t 起_k ，_w 上调_v 存款_k 类_k 金融机构_nz
	
	@Expose
	@SerializedName("publishDate")
	private String publishDate;//发布时间。如2010-12-21 07:32:23
	
	@Expose
	@SerializedName("author")
	private String author;//作者
	
	@Expose
	@SerializedName("channel")
	private String channel;//来自生产系统，记录其所在频道、栏目、专题等信息，待解析;常URL形式
	
	@Expose
	@SerializedName("other")
	private String other;//记录分类，图片，地域信息
	
	public String getID() {
		return this.id;
	}

	public void setID(String id) {
		this.id = id;
	}
	
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getKeywords() {
		return this.rawKeywords;
	}

	public void setKeywords(String rawKeywords) {
		this.rawKeywords = rawKeywords;
	}
	
	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String getDate() {
		return this.publishDate;
	}

	public void setDate(String publishDate) {
		this.publishDate = publishDate;
	}
	
	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getChannel() {
		return this.channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public String getOther()
	{
		return this.other;
	}
	public void setOther(String other)
	{
		this.other = other;
	}
	
	public HashMap<String, String> toHashMap() {
		HashMap<String, String> itemMap = new HashMap<String, String>();
		itemMap.put("url", url);
		itemMap.put("author", author);
		itemMap.put("content", content);
		itemMap.put("date", publishDate);
		itemMap.put("documentid", id);
		itemMap.put("keywords", rawKeywords);
		itemMap.put("title", title);
		itemMap.put("channel", channel);
		itemMap.put("other",other);
		return itemMap;
	}
	
	/**
	 * 初始化
	 * 
	 * @param
	 */
	public Item()
	{
		this.url = null;
		this.id = null;
		this.title = null;
		this.content = null;
		this.publishDate = null;
		this.rawKeywords = null;
		this.author = null;
		this.channel = null;
		this.other = null;
	}
	
	public Item(Item item)
	{
		this.url = item.url;
		this.id = item.id;
		this.title = item.title;
		this.content = item.content;
		this.publishDate = item.publishDate;
		this.rawKeywords = item.rawKeywords;
		this.author = item.author;
		this.channel = item.channel;
		this.other = item.other;
	}
	/**
	 * 初始化
	 * 
	 * @param hm_itemInfo
	 */
	public Item(HashMap<String, String> hm_itemInfo)
	{
		if(hm_itemInfo!=null)
		{
			this.url = hm_itemInfo.get("url");
			this.id = hm_itemInfo.get("documentid");
			this.title = hm_itemInfo.get("title");
			this.content = hm_itemInfo.get("content");
			this.publishDate = hm_itemInfo.get("date");
			this.rawKeywords = hm_itemInfo.get("keywords");
			this.author = hm_itemInfo.get("author");
			this.channel = hm_itemInfo.get("channel");
			this.other = hm_itemInfo.get("other");
		}	
		else {
			this.url = null;
			this.id = null;
			this.title = null;
			this.content = null;
			this.publishDate = null;
			this.rawKeywords = null;
			this.author = null;
			this.channel = null;
			this.other = null;
		}
	}
	
	
}
