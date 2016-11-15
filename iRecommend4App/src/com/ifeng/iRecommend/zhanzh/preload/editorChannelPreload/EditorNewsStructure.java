/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.editorChannelPreload;

import java.util.List;

/**
 * <PRE>
 * 作用 : 编辑推荐位抓取数据结构
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
 *          1.0          2015年9月23日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class EditorNewsStructure {
	String listId;
	String type;
	int expiredTime;
	int currentPage;
	int totalPage;
	List<EditorNewsItem> item;
}
class EditorNewsItem{
	String thumbnail;
	String online;
	String title;
	String source;
	String updateTime;
	String id;
	String documentId;
	String type;
	StyleStructure style;
	boolean hasSlide;
	boolean hasVideo;
	String commentsUrl;
	String comments;
	String commentsall;
	LinkStructure link;
}
class LiveStructure{
	String startTime;
	String status;
}
class LinkStructure{
	String type;
	String url;
}
class StyleStructure{
	String type;
	List<String> images;
}