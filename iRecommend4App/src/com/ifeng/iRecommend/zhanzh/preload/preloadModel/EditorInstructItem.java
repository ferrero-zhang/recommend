/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.preloadModel;

/**
 * <PRE>
 * 作用 : 编辑指令模板类
 *   
 * 使用 : 目前应用于本地频道（北京市）同步编辑指令
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016年3月17日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class EditorInstructItem {
	private String title;
	private String source;
	private String updateTime;
	private String id;
	private String documentId;
	private String type;      //slide/doc
	private String instruct; //top\focus
	private String other;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String docmentId) {
		this.documentId = docmentId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getInstruct() {
		return instruct;
	}
	public void setInstruct(String instruct) {
		this.instruct = instruct;
	}
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	
}
