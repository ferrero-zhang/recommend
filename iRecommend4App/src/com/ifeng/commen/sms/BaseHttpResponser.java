package com.ifeng.commen.sms;

public class BaseHttpResponser {
	private String _url = null;
	private String _encoding = null;
	private String _content = null;

	public String getContent() {
		return _content;
	}

	public void setContent(String content) {
		this._content = content;
	}

	public String getEncoding() {
		return _encoding;
	}

	public void setEncoding(String encoding) {
		this._encoding = encoding;
	}

	public String getUrl() {
		return _url;
	}

	public void setUrl(String url) {
		this._url = url;
	}
}
