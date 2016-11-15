package com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement;
/**
 * 
 * <PRE>
 * 作用 : 
 *     文章时效性判定的返回结果  
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
 *          1.0          2016年8月26日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class TimeSensitiveInfo {
	/**
	 * 是否具有时效性
	 */
	private boolean isTimeSensitive;
	/**
	 * 原因描述(时间要素、节日识别、事件识别等类型，用于快速debug追踪)
	 */
	private String reasonDescribe;
	/**
	 * 备用字段，后续可能加入事件时间
	 */
	private String others;
	/**
	 * 几小时，当天，几天，本周，本季度等的详细描述
	 */
	private String timeTag;

	public boolean isTimeSensitive() {
		return isTimeSensitive;
	}

	public void setTimeSensitive(boolean isTimeSensitive) {
		this.isTimeSensitive = isTimeSensitive;
	}

	public String getReasonDescribe() {
		return reasonDescribe;
	}

	public void setReasonDescribe(String reasonDescribe) {
		this.reasonDescribe = reasonDescribe;
	}

	public String getOthers() {
		return others;
	}

	public void setOthers(String others) {
		this.others = others;
	}

	public String getTimeTag() {
		return timeTag;
	}

	public void setTimeTag(String timeTag) {
		this.timeTag = timeTag;
	}

	@Override
	public String toString() {
		return "TimeSensitiveInfo [isTimeSensitive=" + isTimeSensitive
				+ ", reasonDescribe=" + reasonDescribe + ", others=" + others
				+ ", timeTag=" + timeTag + "]";
	}
}
