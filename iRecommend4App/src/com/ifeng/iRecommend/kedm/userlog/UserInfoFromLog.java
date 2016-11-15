package com.ifeng.iRecommend.kedm.userlog;

import java.util.Map;
import java.util.Set;

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
	private Map<String, String> user_loc;
	
	/**
	 * @Fields in_time :
	 * 用户打开时间
	 */
	private Set<String> in_time;
	public Set<String> getIn_time() {
		return in_time;
	}

	public void setIn_time(Set<String> in_time) {
		this.in_time = in_time;
	}

	/**
	 * @Fields user_os :
	 * 用户系统号
	 */
	private String user_os;
	
	/**
	 * @Fields user_ver :
	 * 用户客户端版本
	 */
	private String user_ver;
	private String user_mtype;

	public String getUser_mtype() {
		return user_mtype;
	}

	public void setUser_mtype(String user_mtype) {
		this.user_mtype = user_mtype;
	}

	/**
	 * @Fields open_doc_id_time_map :
	 * 用户打开过的docid-->操作时间 map (同id以最后一次操作为准)
	 * "打开"动作默认为阅读，包括page/openpush等操作
	 */
	private Map<String, String> open_doc_id_time_map;
	
	/**
	 * @Fields read_sw_time_map :
	 * 用户打开过的订阅搜索等内容的sw词-->操作时间 map (同id以最后一次操作为准)
	 * "打开"动作默认为阅读，包括page/openpush等操作
	 */
	private Map<String, String> read_sw_time_map;
	
	/**
	 * @Fields store_docid_time_map :
	 * 用户搜藏过的docid-->操作时间 map (同id以最后一次操作为准)
	 * store操作
	 */
	private Map<String, String> store_docid_time_map;
	
	/**
	 * @Fields store_docid_time_map :
	 * 用户转发过的docid-->操作时间 map (同id以最后一次操作为准)
	 * store操作
	 */
	private Map<String, String> ts_docid_time_map;
	
	public Map<String, String> getTs_docid_time_map() {
		return ts_docid_time_map;
	}

	public void setTs_docid_time_map(Map<String, String> ts_docid_time_map) {
		this.ts_docid_time_map = ts_docid_time_map;
	}

	/**
	 * @Fields user_search_time_map :
	 * 用户搜索的关键词search-->操作时间 map (同id以最后一次操作为准)
	 * example：type=search$yn=yes$sw=薄谷开来
	 * 
	 */
	private Map<String, String> user_search_time_map;

	/**
	 * @Fields userKeywordListLog : 
	 * 用户打开过的keyword-->操作时间 map (同id以最后一次操作为准)
	 * action type=keywd$sw=凤凰新闻
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
	 * @Fields book_words_search :
	 * 用户的订阅号搜索行为--->操作时间
	 */
	private Map<String, String> book_words_search;

	/**
	 * @Fields book_words_del : 
	 * 用户新删除的订阅关键词--->操作时间
	 */
	private Map<String, String> book_words_del;

	/**
	 * @Fields page_duration : 
	 * 用户阅读页面id--->阅读时长#操作时间
	 */
	private Map<String, String> page_duration;
	/**
	 * @Fields dislike_doc : 
	 * 用户不感兴趣的id--->操作时间
	 */
	private Map<String, String> dislike_doc;
	
	/**
	 * @Fields dislike_doc : 
	 * 用户观看视频id--->观看时长#操作时间
	 */
	private Map<String, String> vid_duration;
	
	/**
	 * @Fields open_pic_id_time_map : 
	 * 用户查看图片id--->操作时间
	 */
	private Map<String, String> open_pic_id_time_map;
	
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
					Map<String, String> userloc,
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
	
	public UserInfoFromLog(){
		
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
	
	public Map<String, String> getUser_loc() {
		return user_loc;
	}

	public void setUser_loc(Map<String, String> user_loc) {
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
	
	public Map<String, String> getUser_search_time_map() {
		return user_search_time_map;
	}

	public void setUser_search_time_map(Map<String, String> user_search_time_map) {
		this.user_search_time_map = user_search_time_map;
	}
	
	public String getUser_os() {
		return user_os;
	}

	public void setUser_os(String user_os) {
		this.user_os = user_os;
	}

	public String getUser_ver() {
		return user_ver;
	}

	public void setUser_ver(String user_ver) {
		this.user_ver = user_ver;
	}
	
	public Map<String, String> getRead_sw_time_map() {
		return read_sw_time_map;
	}

	public void setRead_sw_time_map(Map<String, String> read_sw_time_map) {
		this.read_sw_time_map = read_sw_time_map;
	}

	public Map<String, String> getStore_docid_time_map() {
		return store_docid_time_map;
	}

	public void setStore_docid_time_map(Map<String, String> store_docid_time_map) {
		this.store_docid_time_map = store_docid_time_map;
	}

	public Map<String, String> getBook_words_search() {
		return book_words_search;
	}

	public void setBook_words_search(Map<String, String> book_words_search) {
		this.book_words_search = book_words_search;
	}
	
	public Map<String, String> getPage_duration() {
		return page_duration;
	}

	public void setPage_duration(Map<String, String> page_duration) {
		this.page_duration = page_duration;
	}
	
	public Map<String, String> getDislike_doc() {
		return dislike_doc;
	}

	public void setDislike_doc(Map<String, String> dislike_doc) {
		this.dislike_doc = dislike_doc;
	}
	
	public Map<String, String> getVid_duration() {
		return vid_duration;
	}

	public void setVid_duration(Map<String, String> vid_duration) {
		this.vid_duration = vid_duration;
	}
	
	public Map<String, String> getOpen_pic_id_time_map() {
		return open_pic_id_time_map;
	}

	public void setOpen_pic_id_time_map(Map<String, String> open_pic_id_time_map) {
		this.open_pic_id_time_map = open_pic_id_time_map;
	}
}
