package com.ifeng.iRecommend.likun.userCenter.tnappuc.userinfomodel;

import java.util.Map;

/**
 * <PRE>
 * 作用 : 
 *    系统日志解析后，得到所有用户的临时概括信息，即UserInfo。
 *    得到该信息之后，可以将其更新到redis库中。 
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
 *          1.0          2015年6月10日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class UserInfoFromLog {
	
	/**
	 * @Fields userid_hardware : 
	 * 用户系统分配的id
	 */
	private String userid;
	
	/**
	 * @Fields user_loc :
	 * 用户地理位置(不确定是否有多个，在对库执行更新的时候，检查即可)
	 */
	private String user_loc;

	/**
	 * @Fields open_doc_id_time_map :
	 * 用户打开过的docid-->操作时间 map (同id以最后一次操作为准)
	 * "打开"动作默认为阅读，包括page/openpush等操作
	 */
	private Map<String, String> open_doc_id_time_map;
	
	
	/**
	 * @Fields userKeywordListLog : 
	 * 用户打开过的keyword-->操作时间 map (同id以最后一次操作为准)
	 * example:id=kw_大陆$type=kw$ref=117372
	 */
	private Map<String, String> user_keyword_time_map;
	
	/**
	 * @Fields interest_words_action :
	 * 用户新添加的兴趣关键词---> 操作时间
	 * 
	 */
	private Map<String, String> interest_words_add;
	
	/**
	 * @Fields interest_words_del :
	 * 用户新删除的兴趣关键词---> 操作时间
	 */
	private Map<String, String> interest_words_del;
	
	/**
	 * @Fields book_words_add :
	 * 用户新添加的订阅关键词--->操作时间
	 */
	private Map<String, String> book_words_add;
	
	/**
	 * @Fields book_words_del : 
	 * 用户新删除的订阅关键词--->操作时间
	 */
	private Map<String, String> book_words_del;

	
	/**
	 * <p>Title: UserInfo constructor</p>
	 * <p>Description: </p>
	 * @author liu_yi
	 * @param userid  用户id
	 *        说明：系统分配id-->硬件id的对应关系应该另外维护
	 * @param userloc  地理位置
	 * @param open_doc_id_time_map  打开过的docid -->操作时间
	 * @param interest_words_add 新添加的兴趣关键词 ---> 操作时间
	 * @param interest_words_del  新删除的兴趣关键词 ---> 操作时间
	 * @param book_words_add  新添加的订阅关键词 ---> 操作时间
	 * @param book_words_del  新删除的订阅关键词 ---> 操作时间
	 */
	public UserInfoFromLog(String userid, 
			        String userloc,
			        Map<String, String> open_doc_id_time_map, 
			        Map<String, String> user_keyword_time_map,
			        Map<String, String> interest_words_add, 
			        Map<String, String> interest_words_del,
			        Map<String, String> book_words_add, 
			        Map<String, String> book_words_del) {
		this.userid = userid;
		this.user_loc = userloc;
		this.open_doc_id_time_map = open_doc_id_time_map;
		this.setUser_keyword_time_map(user_keyword_time_map);
		this.interest_words_add = interest_words_add;
		this.interest_words_del = interest_words_del;
		this.book_words_add = book_words_add;
		this.book_words_del = book_words_del;
	}
	
	public String toString_readDocs() {
		StringBuffer bf = new StringBuffer();
		if (null == this.open_doc_id_time_map) {
			bf.append("No Read Docs");
			return bf.toString();
		}
		
		if (!this.open_doc_id_time_map.isEmpty()) {
			for (String tempDocID : open_doc_id_time_map.keySet()) {
				bf.append("[" + tempDocID + "-->" + open_doc_id_time_map.get(tempDocID) + "] ");
			}
		} else {
			bf.append("No Read Docs");
		}
		
		return bf.toString();
	}
	
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	public String getUser_loc() {
		return user_loc;
	}

	public void setUser_loc(String user_loc) {
		this.user_loc = user_loc;
	}
	
	public Map<String, String> getOpen_doc_id_time_map() {
		return open_doc_id_time_map;
	}

	public void setOpen_doc_id_time_map(Map<String, String> open_doc_id_time_map) {
		this.open_doc_id_time_map = open_doc_id_time_map;
	}

	public Map<String, String> getInterest_words_add() {
		return interest_words_add;
	}

	public void setInterest_words_add(Map<String, String> interest_words_add) {
		this.interest_words_add = interest_words_add;
	}

	public Map<String, String> getInterest_words_del() {
		return interest_words_del;
	}

	public void setInterest_words_del(Map<String, String> interest_words_del) {
		this.interest_words_del = interest_words_del;
	}

	public Map<String, String> getBook_words_add() {
		return book_words_add;
	}

	public void setBook_words_add(Map<String, String> book_words_add) {
		this.book_words_add = book_words_add;
	}

	public Map<String, String> getBook_words_del() {
		return book_words_del;
	}

	public void setBook_words_del(Map<String, String> book_words_del) {
		this.book_words_del = book_words_del;
	}

	public Map<String, String> getUser_keyword_time_map() {
		return user_keyword_time_map;
	}

	public void setUser_keyword_time_map(Map<String, String> user_keyword_time_map) {
		this.user_keyword_time_map = user_keyword_time_map;
	}
	
}
