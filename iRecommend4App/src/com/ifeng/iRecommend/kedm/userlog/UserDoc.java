package com.ifeng.iRecommend.kedm.userlog;

public class UserDoc {
	private String id;//内容docid
	
	private String url;//内容url
	
	private String orgtitle;//未分词的标题
	
	private String segtitle;//分词后的标题
	
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

	public String getOrgtitle() {
		return orgtitle;
	}

	public void setOrgtitle(String orgtitle) {
		this.orgtitle = orgtitle;
	}

	public String getSegtitle() {
		return segtitle;
	}

	public void setSegtitle(String segtitle) {
		this.segtitle = segtitle;
	}

	public String getPdate() {
		return pdate;
	}

	public void setPdate(String pdate) {
		this.pdate = pdate;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	private String pdate;//内容发布时间
	
	private String channel;//内容所属频道
	private String fcwordset;
	
	public String getFcwordset() {
		return fcwordset;
	}

	public void setFcwordset(String fcwordset) {
		this.fcwordset = fcwordset;
	}

	public UserDoc(String id,String url,String orgtitle,String segtitle,String pdate,String channel,String fcwordset){
		this.id = id;
		this.url = url;
		this.orgtitle = orgtitle;
		this.segtitle = segtitle;
		this.pdate = pdate;
		this.channel = channel;
		this.fcwordset = fcwordset;
	}

}
