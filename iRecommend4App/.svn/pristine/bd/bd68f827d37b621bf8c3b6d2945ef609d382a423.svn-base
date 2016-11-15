package com.ifeng.iRecommend.wuyg.commonData.Update;

import com.ifeng.commen.Utils.LoadConfig;

public class HotWordSubData extends CommonSubDataWord {

	private String documentId;

	/**
	 * 该词录入的最开始时间
	 */
	private String starttime;
	/**
	 * value的时间，为该词以后是否删除做判断
	 */
	private String endtime;
	/**
	 * 事件类型
	 */
	private String event;

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public HotWordSubData(String wordType, String wordState, String value,
			String documentId, String starttime, String endtime) {
		super(wordType, wordState, value, endtime);
		setStarttime(starttime);
		setDocumentId(documentId);
	}

	public HotWordSubData(String wordType, String wordState, String value,
			String documentId, String starttime, String endtime,
			boolean isEvent) {
		this(wordType, wordState, value, documentId, starttime, null);
		setEndtime(endtime);
		
		  if(isEvent){
			  setEvent(EventState.AbstractEvent.name());
		  }else if(!isEvent){
			  setEvent(EventState.NormalEvent.name());
		  }
		
		
	}

	@Override
	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(super.toString());

		sBuffer.append(LoadConfig.lookUpValueByKey("fieldDelimiter")
				+ getEvent() + "#EVENT#");

		if (null != endtime) {
			sBuffer.append(getEndtime());
		}

		if (null != starttime) {
			sBuffer.append("#TIME#" + getStarttime());
		}

		if (null != documentId) {
			sBuffer.append(LoadConfig.lookUpValueByKey("fieldDelimiter")
					+ getDocumentId());
		}
		return sBuffer.toString();
	}

	public enum EventState {
		/**
		 * 抽象事件，目前指的是手动补充的一些具有强概括能力的事件表达
		 */
		AbstractEvent,
		/**
		 * 由百度热点事件，客户端编辑等流过来的一般事件
		 */
		NormalEvent;
	}

}
