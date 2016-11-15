package com.ifeng.iRecommend.wuyg.commonData.Update;

import com.ifeng.commen.Utils.LoadConfig;

public class WordReadableSubData extends CommonSubDataWord {
	/**
	 * 该词来自客户端用户订阅的次数
	 */
	int clientSubWordCount;
	/**
	 * 该词在其他来源中被展示的次数
	 */
	int otherSouceCount;

	public int getClientSubWordCount() {
		return clientSubWordCount;
	}

	public void setClientSubWordCount(int clientSubWordCount) {
		this.clientSubWordCount = clientSubWordCount;
	}

	public int getOtherSouceCount() {
		return otherSouceCount;
	}

	public void setOtherSouceCount(int otherSouceCount) {
		this.otherSouceCount = otherSouceCount;
	}

	public WordReadableSubData(int clientSubWordCount, int otherSouceCount) {
		super(null, null, null, null);
		setClientSubWordCount(clientSubWordCount);
		setOtherSouceCount(otherSouceCount);
	}

	public WordReadableSubData(String wordType, String wordState, String value,
			String time) {
		super(wordType, wordState, value, time);

	}

	public WordReadableSubData(String wordType, String wordState, String value,
			String time, int clientSubWordCount, int otherSouceCount) {
		super(wordType, wordState, value, time);
		setClientSubWordCount(clientSubWordCount);
		setOtherSouceCount(otherSouceCount);

	}

	@Override
	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(super.toString());

		sBuffer.append(LoadConfig.lookUpValueByKey("fieldDelimiter")
				+ getClientSubWordCount());

		sBuffer.append(LoadConfig.lookUpValueByKey("fieldDelimiter")
				+ getOtherSouceCount());
		return sBuffer.toString();
	}

}
