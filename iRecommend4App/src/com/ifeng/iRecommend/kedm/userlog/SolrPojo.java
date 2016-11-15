package com.ifeng.iRecommend.kedm.userlog;

/**
 * <PRE>
 * 作用 : userlog解析过程post到外部solr的中间对象类
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
 *          1.0          2015-9-23        kedm          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class SolrPojo {

	private String id;//userlog对应solr的id，由userid#docid组合
	private String url;
	private String fcwordset;
	private String type;//浏览内容type，0、1分别为doc、slide
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFcwordset() {
		return fcwordset;
	}
	public void setFcwordset(String fcwordset) {
		this.fcwordset = fcwordset;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPdate() {
		return pdate;
	}
	public void setPdate(String pdate) {
		this.pdate = pdate;
	}
	public String getTm() {
		return tm;
	}
	public void setTm(String tm) {
		this.tm = tm;
	}
	private String aid;
	public String getAid() {
		return aid;
	}
	public void setAid(String aid) {
		this.aid = aid;
	}
	private String title;//内容标题
	private String pdate;//内容发布时间
	private String tm;//short时间格式
	private String uid;//user id
	private String source;
	private String orgtitle;//分词前标题
	private String cdate;//内容被浏览的时间
	public String getCdate() {
		return cdate;
	}
	public void setCdate(String cdate) {
		this.cdate = cdate;
	}
	public String getOrgtitle() {
		return orgtitle;
	}
	public void setOrgtitle(String orgtitle) {
		this.orgtitle = orgtitle;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	private String channel;
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}



}
