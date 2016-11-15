package com.ifeng.iRecommend.lidm.userLog;

public class UserSlide {
	private String id;
	
	private String uid;
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	private String url;
	
	private String title;
	
	private String date;
	
	private String channel;
	
	public UserSlide(String id,String uid,String url,String title,String date,String channel){
		this.id = id;
		this.uid = uid;
		this.url = url;
		this.title = title;
		this.date = date;
		this.channel = channel;
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

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

}
