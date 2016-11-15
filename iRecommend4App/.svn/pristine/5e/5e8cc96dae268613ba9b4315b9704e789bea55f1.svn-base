package com.ifeng.iRecommend.dingjw.front_rankModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ifeng.commen.Utils.JsonUtils;
/**
 * <PRE>
 * 作用 : 
 *   客户端前端文章item类。
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
 *          1.0          2014-05-15        dingjw          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class appItemFront extends itemFront{
	
	@Expose
	@SerializedName("appid")
	private String appid;//这个item所在的应用，如newspush
	
	@Expose
	@SerializedName("imcp_id")
	private String imcp_id;//在imcp中的编号
	
	@Expose
	@SerializedName("desc")
	private String desc;//摘要描述
	
	@Expose
	@SerializedName("content")
	private String content;//item的內容
	
	@Expose
	@SerializedName("type")
	private String type;//推送类型，即时推送或延时推送，如realtime
	
	@Expose
	@SerializedName("hotlevel")
	private String hotlevel;//編輯設置的新聞的等級
	
	@Expose
	@SerializedName("doc_type")
	private String doc_type;//item类型，如slide,doc,video
	
	@Expose
	@SerializedName("more_tags")
	private String more_tags;//编辑打的标签
	
	@Expose
	@SerializedName("others")
	private String others;//备注字段
	
	public String getOthers() {
		return others;
	}

	public String getImcpID() {
		return imcp_id;
	}

	public void setImcpID(String imcpid) {
		this.imcp_id = imcpid;
	}
	
	public String getAppID() {
		return appid;
	}

	public void setAppID(String appid) {
		this.appid = appid;
	}
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getDocType() {
		return doc_type ;
	}

	public void setDocType(String doctype) {
		this.doc_type  = doctype;
	}
	
	public String getMoreTags() {
		return more_tags;
	}

	public void setMoreTags(String tags) {
		this.more_tags = tags;
	}
	
	
	public String getWeight() {
		return hotlevel;
	}

	public void setWeight(String weight) {
		this.hotlevel = weight;
	}
}
