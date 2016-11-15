package com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedisPipeline;

import com.ifeng.iRecommend.likun.userCenter.tnappuc.userinfomodel.UserCenterModel;

/**
 * <PRE>
 * 作用 : 
 *  用户中心模型数据写入redis 
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
 *          1.0          2015年7月6日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class UserCenterModel2Redis {
	private static final Log log = LogFactory.getLog(UserCenterModel2Redis.class);
		
	public static void ucModelUpdate2Redis(UserCenterModel userModel,ShardedJedisPipeline pipeline_uc) {
		if(userModel == null || pipeline_uc == null)
			return;
		
		String key = userModel.getUser_id();
		if (null == key || key.isEmpty()) {
			log.info("UserID Empty");
			return;
		}
		
		Map<String, String> allHashFieldValue = new HashMap<String, String>();
		if (null == userModel.getTop_topic1() || userModel.getTop_topic1().length() <= 2 || userModel.getTop_topic1().trim().equals("null$null")) {
			;
		} else {
			allHashFieldValue.put("t1", userModel.getTop_topic1());
		}
		
		if (null == userModel.getTop_topic2() || userModel.getTop_topic2().length() <= 2 || userModel.getTop_topic2().trim().equals("null$null")) {
			;
		} else {
			allHashFieldValue.put("t2", userModel.getTop_topic2());
		}
		
		if (null == userModel.getTop_topic3() || userModel.getTop_topic3().length() <= 2 || userModel.getTop_topic3().trim().equals("null$null")) {
			;
		} else {
			allHashFieldValue.put("t3", userModel.getTop_topic3());
		}
		
		if (null == userModel.getVid_topic1() || userModel.getVid_topic1().length() <= 2 || userModel.getTop_topic1().trim().equals("null$null")) {
			;
		} else {
			allHashFieldValue.put("vid_t1", userModel.getVid_topic1());
		}
		
		if (null == userModel.getVid_topic2() || userModel.getVid_topic2().length() <= 2 || userModel.getTop_topic2().trim().equals("null$null")) {
			;
		} else {
			allHashFieldValue.put("vid_t2", userModel.getVid_topic2());
		}
		
		if (null == userModel.getVid_topic3() || userModel.getVid_topic3().length() <= 2 || userModel.getTop_topic3().trim().equals("null$null")) {
			;
		} else {
			allHashFieldValue.put("vid_t3", userModel.getVid_topic3());
		}
		
		if (null == userModel.getPic_topic1() || userModel.getPic_topic1().length() <= 2 || userModel.getTop_topic1().trim().equals("null$null")) {
			;
		} else {
			allHashFieldValue.put("slide_t1", userModel.getPic_topic1());
		}
		
		if (null == userModel.getPic_topic2() || userModel.getPic_topic2().length() <= 2 || userModel.getTop_topic2().trim().equals("null$null")) {
			;
		} else {
			allHashFieldValue.put("slide_t2", userModel.getPic_topic2());
		}
		
		if (null == userModel.getPic_topic3() || userModel.getPic_topic3().length() <= 2 || userModel.getTop_topic3().trim().equals("null$null")) {
			;
		} else {
			allHashFieldValue.put("slide_t3", userModel.getPic_topic3());
		}
				
		if (null == userModel.getSort_book_words() || userModel.getSort_book_words().length() <= 2) {
			allHashFieldValue.put("ub", "");
		} else {
			allHashFieldValue.put("ub", userModel.getSort_book_words());
		}
		
		if (null == userModel.getSort_interest_words() || userModel.getSort_interest_words().length() <= 2) {
			allHashFieldValue.put("ui", "");
		} else {
			allHashFieldValue.put("ui", userModel.getSort_interest_words());
		}
		
		if (null == userModel.getSort_keywords() || userModel.getSort_keywords().length() <= 2) {
			allHashFieldValue.put("uk", "");
		} else {
			allHashFieldValue.put("uk", userModel.getSort_keywords());
		}
		
		if (null == userModel.getSort_search_words() || userModel.getSort_search_words().length() <= 2) {
			allHashFieldValue.put("search", "");
		} else {
			allHashFieldValue.put("search", userModel.getSort_search_words());
		}
        //用户活跃度单独计算
		if (null == userModel.getUser_active() || userModel.getUser_active().length() < 1) {
			;
		} else {
			allHashFieldValue.put("ua", userModel.getUser_active());
		}
		
		if (null == userModel.getUser_loc() || userModel.getUser_loc().length() <= 2 ) {
			/*String loc = tempGetLocalInfo(key);
			if(loc != null){
				allHashFieldValue.put("loc", loc);
				log.info(key + " local info is "+loc);
			}*/
		} else {
			allHashFieldValue.put("loc", userModel.getUser_loc());
				
		}
		
		//用户系统版本号等信息
		if(null == userModel.getUser_os() || userModel.getUser_os().length() < 1){
			;
		}else{
			allHashFieldValue.put("umos", userModel.getUser_os());
		}
		if(null == userModel.getUser_ver() || userModel.getUser_ver().length() < 1){
			;
		}else{
			allHashFieldValue.put("uver", userModel.getUser_ver());
		}
		if(null == userModel.getUser_mtype() || userModel.getUser_mtype().length() < 1){
			;
		}else{
			allHashFieldValue.put("umt", userModel.getUser_mtype());
		}
		
		//热点事件、优质稿源
		if(null == userModel.getE_top_words() || userModel.getE_top_words().length() < 1){
			;
		}else{
			allHashFieldValue.put("e", userModel.getE_top_words());
		}
		if(null == userModel.getS1_top_words() || userModel.getS1_top_words().length() < 1){
			;
		}else{
			allHashFieldValue.put("s", userModel.getS1_top_words());
		}
		if(null == userModel.getSw_open() || userModel.getSw_open().length() < 1){
			allHashFieldValue.put("sw_open", "");
		}else{
			allHashFieldValue.put("sw_open", userModel.getSw_open());
		}
		if(null == userModel.getLast_topic1() || userModel.getLast_topic1().length() < 1){
			allHashFieldValue.put("last_t1", "");
		}else{
			allHashFieldValue.put("last_t1", userModel.getLast_topic1());
		}
		if(null == userModel.getLast_topic2() || userModel.getLast_topic2().length() < 1){
			allHashFieldValue.put("last_t2", "");
		}else{
			allHashFieldValue.put("last_t2", userModel.getLast_topic2());
		}
		if(null == userModel.getLast_topic3() || userModel.getLast_topic3().length() < 1){
			allHashFieldValue.put("last_t3", "");
		}else{
			allHashFieldValue.put("last_t3", userModel.getLast_topic3());
		}
		if(null == userModel.getFineitem() || userModel.getFineitem().length() < 1){
			allHashFieldValue.put("lastitems", "");
		}else{
			allHashFieldValue.put("lastitems", userModel.getFineitem());
		}
		if(null == userModel.getIcar() || userModel.getIcar().length() < 1){
			;
		}else{
			allHashFieldValue.put("icar", userModel.getIcar());
		}
		if(null == userModel.getIdigi() || userModel.getIdigi().length() < 1){
			;
		}else{
			allHashFieldValue.put("idigi", userModel.getIdigi());
		}
		if(null == userModel.getIgame() || userModel.getIgame().length() < 1){
			;
		}else{
			allHashFieldValue.put("igame", userModel.getIgame());
		}
		if(null == userModel.getLikevideo() || userModel.getLikevideo().length() < 1){
			;
		}else{
			allHashFieldValue.put("likevideo", userModel.getLikevideo());
		}
		//加入最近一次更新时间
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long t = System.currentTimeMillis();
		String time = df.format(new Date(t));
		allHashFieldValue.put("utime", time);
		/*try{
			long s = System.currentTimeMillis();
			jc.hmset(key, allHashFieldValue);
			log.info(key + " add to redis cluster spend "+(System.currentTimeMillis()-s));
		}catch(Exception e){
			log.error("add to redis cluster error "+key,e);
			e.printStackTrace();
		}*/
		//pipeline.hmset(key, allHashFieldValue);
		pipeline_uc.hmset(key, allHashFieldValue);
		log.info("update userCenterModel:" + key);
	}
	//临时解决用户地理位置的问题
	public static String tempGetLocalInfo(String uid){
		String res = null;
		Jedis jedis = null;
		try{
			jedis = new Jedis("10.90.1.58",6379,5000);
			jedis.select(1);
			res = jedis.get(uid);
		}catch(Exception e){
			log.error("get user local error "+uid,e);
			jedis.disconnect();
			e.printStackTrace();
		}
		jedis.disconnect();
		return res;
	}
	//临时解决用户地理位置的问题
	public static String tempGetLocalInfo_byPredict(String uid){
		String res = null;
		Jedis jedis = null;
		try{
			jedis = new Jedis("10.90.1.58",6379,5000);
			jedis.select(2);
			res = jedis.get(uid);
		}catch(Exception e){
			log.error("get user local error "+uid,e);
			jedis.disconnect();
			e.printStackTrace();
		}
		jedis.disconnect();
		return res;
	}
	
}
