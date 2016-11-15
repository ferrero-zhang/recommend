package com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.ConfigAutoLoader;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.featureEngineering.TagsQueryInterface;
import com.ifeng.iRecommend.featureEngineering.XMLitemf;
import com.ifeng.iRecommend.featureEngineering.LayerGraph.Graph;
import com.ifeng.iRecommend.featureEngineering.LayerGraph.GraphNode;
import com.ifeng.iRecommend.likun.rankModel.addItemNew;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.GlobalParams;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.HotWordUtil;
import com.ifeng.iRecommend.usermodel.idfQueryInterface;
import com.ifeng.iRecommend.usermodel.queryCmppItem;

/**
 * <PRE>
 * 作用 : 
 *   user模型的建模抽象化表达，以及item的建模抽象化表达
 * 使用 : 
 *   
 * 示例 :
 *   user log info案例：
 *   {
    "userid": "867628021329651",
    "open_doc_id_time_map：{"ent":"1452580841#ch","cmpp":"1452580848#ent","srh":"1452581791#imcp_105093285","noid":"1452580902#ent","rcmd":"1452580005#ch","104570900":"1452580027#rcmd","104761774":"1452580046#rcmd","104956779":"1452580504#rcmd","101398774":"1452580748#rcmd","sy":"1452581764#ch","105093285":"1452581779#sy"},"user_search_time_map":{"武侠":"1452579972","港台明星":"1452580921"},"user_keyword_time_map":{"港台明星":"1452579932","张学友":"1452579939","激情戏":"1452580891","马赛克":"1452581790"}}
 * 注意 :
 *   由于客户端的item ID，都是被转换后的imcp或者cmpp ID，也有一部分是我们新内容体系的ID，所以需要同时去@贺夏龙的多个接口中去查。
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年9月23日        liuyi         create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class FeatureWordStatistics {
	private static final Log LOG = LogFactory.getLog(FeatureWordStatistics.class);
	
	/**
	 * @Fields userLogAction : 日志中心中的用户行为记录
	 *         example: 186101#1438284082$194839#1438284075  (阅读过的文章ID#阅读时间$阅读过的文章ID#阅读时间)
	 */
	private String userLogAction;
	
	
	/**
	 * @Fields userLogAction_Type : 日志中心中的用户行为的类型
	 *         example: 阅读行为/订阅行为 ...
	 */
	private String userLogAction_Type;
	
	// 用户模型三个层次topic的词序列
	private List<String> topic1_word_List;
	private List<String> topic2_word_List;
	private List<String> topic3_word_List;
	
	// 用户模型三个层次topic词序列对应的词得分
	private List<Double> topic1_word_score;
	private List<Double> topic2_word_score;
	private List<Double> topic3_word_score;
	
	private List<String> last_topic1_word_List;
	private List<String> last_topic2_word_List;
	private List<String> last_topic3_word_List;
	
	private List<String> dislike_topic1_word_List;
	private List<String> dislike_topic2_word_List;
	private List<String> dislike_topic3_word_List;
	
	

	// 用户模型三个层次topic词序列对应的词得分
	private List<Double> last_topic1_word_score;
	private List<Double> last_topic2_word_score;
	private List<Double> last_topic3_word_score;
	
	// word-->time map，用于保存行为时间
	private Map<String, String> topic1_wordTimeMap;
	private Map<String, String> topic2_wordTimeMap;
	private Map<String, String> topic3_wordTimeMap;
	
	//热点事件、优质稿源
	private List<String> e_wordTime_list;
	private List<String> s1_wordTime_list;
	
	//interest 
	private Map<String,String> i_tagWord_map;
	public Map<String, String> getI_tagWord_map() {
		return i_tagWord_map;
	}

	public void setI_tagWord_map(Map<String, String> i_tagWord_map) {
		this.i_tagWord_map = i_tagWord_map;
	}

	// 不同行为的权重,推送、头条里、订阅、搜索、点击频道等等，都有不同权重
	private static double readDoc_weight = Double.valueOf(ConfigAutoLoader.getPropertyWithKey("userReadDocWeight"));
	private static double readDoc_push_weight = Double.valueOf(ConfigAutoLoader.getPropertyWithKey("userReadDocPushWeight"));
	private static double readDoc_sy_weight = Double.valueOf(ConfigAutoLoader.getPropertyWithKey("userReadDocSYWeight"));
	private static double search_weight = Double.valueOf(ConfigAutoLoader.getPropertyWithKey("userSearchWeight"));//用户主动搜索
	private static double readKW_weight = Double.valueOf(ConfigAutoLoader.getPropertyWithKey("userKeywordWeight"));//用户点击频道
	private static double bookWord_weight = Double.valueOf(ConfigAutoLoader.getPropertyWithKey("userBookWeight"));//用户订阅word（频道）
	private static double readCH_weight = Double.valueOf(ConfigAutoLoader.getPropertyWithKey("userReadCHWeight"));//用户点击频道
	//用户感兴趣或者不感兴趣频道
	private static double userInterestWeight = Double.valueOf(ConfigAutoLoader.getPropertyWithKey("userInterestWeight"));
	
	//user log中不同行为field名称
	public static String readDoc_type_name = ConfigAutoLoader.getPropertyWithKey("userReadDocList");
	public static String readSW_type_name = ConfigAutoLoader.getPropertyWithKey("userReadSWList");
	public static String readKW_type_name = ConfigAutoLoader.getPropertyWithKey("userReadKeywordList");//用户点击频道
	public static String search_type_name = ConfigAutoLoader.getPropertyWithKey("userSearchList");//用户主动搜索
	public static String userInterest_type_name = ConfigAutoLoader.getPropertyWithKey("userInterestWordList");//用户感兴趣或者不感兴趣频道
	public static String bookWord_type_name = ConfigAutoLoader.getPropertyWithKey("userBookWordList");//用户订阅word（频道）
	
	
	//test，测试用
	public long queryJedisTime;
	
	private queryItemFeatures query;
	
		
	public FeatureWordStatistics(/*queryItemFeatures _query,*/String _userLogAction_Type,
			                     String _userLogAction) {
		this.userLogAction_Type = _userLogAction_Type;
		this.userLogAction = _userLogAction;
		//this.query = _query;
	}
	
	public void wordSta(String debugFeature) {
		this.topic1_word_List = new ArrayList<String>();
		this.topic2_word_List = new ArrayList<String>();
		this.topic3_word_List = new ArrayList<String>();
		
		this.topic1_word_score = new ArrayList<Double>();
		this.topic2_word_score = new ArrayList<Double>();
		this.topic3_word_score = new ArrayList<Double>();
		
		this.topic1_wordTimeMap = new HashMap<String,String>();
		this.topic2_wordTimeMap = new HashMap<String,String>();
		this.topic3_wordTimeMap = new HashMap<String,String>();
		
		this.e_wordTime_list = new ArrayList<String>();
		this.s1_wordTime_list = new ArrayList<String>();
		
		this.last_topic1_word_List = new ArrayList<String>();
		this.last_topic2_word_List = new ArrayList<String>();
		this.last_topic3_word_List = new ArrayList<String>();
		this.last_topic1_word_score = new ArrayList<Double>();
		this.last_topic2_word_score = new ArrayList<Double>();
		this.last_topic3_word_score = new ArrayList<Double>();
		
		this.dislike_topic1_word_List = new ArrayList<String>();
		this.dislike_topic2_word_List = new ArrayList<String>();
		this.dislike_topic3_word_List = new ArrayList<String>();
		
		this.i_tagWord_map = new HashMap<String,String>();
		//test
		queryJedisTime = 0;		
		
		if (this.userLogAction_Type.equals(readDoc_type_name)) {
			// 阅读文章行为类型
			//LOG.info("Running word sta:" +  readDoc_type_name);
			this.wordSta_ReadDoc(debugFeature);
		} else if (this.userLogAction_Type.equals("last2days")){
			this.wordSta_ReadDoc_last2days(debugFeature);
		}else if (this.userLogAction_Type.equals("dislike")) {
			// 点击dilike
			this.wordSta_Dislike(debugFeature);
		} else if (this.userLogAction_Type.equals(userInterest_type_name)) {
			// 感兴趣行为
			//LOG.info("Running word sta:" +  userInterest_type_name);
			//this.wordSta_UserBook(debugFeature);还没有完成
		} else if (this.userLogAction_Type.equals(bookWord_type_name)) {
			// 订阅行为
			//LOG.info("Running word sta:" +  bookWord_type_name);
			//this.wordSta_UserBook(debugFeature);
		}else if (this.userLogAction_Type.equals(search_type_name)) {
			// 搜索关键词行为
			//LOG.info("Running word sta:" +  search_type_name);
			//this.wordSta_userSearchWord(debugFeature);
		}
		
		
	
	}
	

	/**
	 * @Title: wordSta_ReadDoc
	 * @Description: 文章阅读行为统计
	 * @author liu_yi
	 * @param debugFeature
	 * @throws
	 */
	private void wordSta_ReadDoc(String debugFeature) {
		if (null == this.userLogAction || this.userLogAction.isEmpty()) {
			return;
		}
		
		String[] doc_array = this.userLogAction.split("\\$");
		
		//为计算速度考虑，降低doc size和计算规模到300
		int doc_num = 600;
		
		for (String tempDoc : doc_array) {
			String[] tempDocSplit = tempDoc.split("#");
		
			if(2 > tempDocSplit.length){
				continue;
			}

			if(doc_num <= 0)
				break;
			
			//docid doctime openscene(旧版本还没有openscene)
			String tempDocid = tempDocSplit[0];
			String tempReadDocTime = null;
			try{
				if(Double.parseDouble(tempDocSplit[1])<1000000000)
					tempReadDocTime = tempDocSplit[2];
				else
					tempReadDocTime = tempDocSplit[1];
			} catch (Exception e){
				if(3 == tempDocSplit.length)
					tempReadDocTime = tempDocSplit[2];
				else if(2 == tempDocSplit.length)
					tempReadDocTime = tempDocSplit[1];
				else
					continue;
			}
			String openScene = "";
					
			if(3 == tempDocSplit.length)
				openScene = tempDocSplit[2];
			else if(2 == tempDocSplit.length)
				openScene = "sy";
			else
				continue;
			
			//query itemf
			long b = System.currentTimeMillis();
			//List<String> features = query.getXMLFeatures(tempDocid);
			List<String> features = queryItemFeatures.getXMLFeatures(tempDocid);
			/*if(features != null && features.contains("社会资讯")){
				StringBuffer sb = new StringBuffer();
				for(String s : features){
					sb.append(s);
				}
				System.out.println(tempDoc+" "+sb.toString());
			}*/
			long e = System.currentTimeMillis();
			queryJedisTime = queryJedisTime + (e-b);
			
			/*if(tempDocid.contains("IMCP") && HotWordUtil.imcpid_word != null){
				String word = HotWordUtil.imcpid_word.get(tempDocid);
				if(word != null){
					this.e_wordTime_map.put(word, tempReadDocTime);
				}
			}*/
			if(openScene.contains("topic_") && HotWordUtil.imcpid_word != null){
				String word = HotWordUtil.imcpid_word.get(openScene.substring(6));
				if(word != null){
					this.e_wordTime_list.add(word+"#"+tempReadDocTime+"#topic");
					
				}
			}
			
			//如果用户点击了文章，但是没有给出features，或者features中没有c，这个时候用secene
			if(tempDocid.matches("\\d{5,20}")){
				doc_num--;
				//修补features;主要是添加一个c,和features中要排重；娱乐、财经等这个时候也给出来了，不见得一定是c1
				features = get_F_byChannel(openScene,features);
			}

			if (null == features || features.isEmpty()) {
				
//				//test
//				LOG.info("features is null,itemid="+tempDocid);
				//System.out.println(tempDoc);
				continue;
			}
			
//			//test
//			LOG.info("features not null,itemid="+tempDocid+",features="+features.toString());
		
			// 先求取这篇文章的c_idf_value;计算方法是，如果一篇文章有多个c1，那么取概率最大的那个c1对应的c_idf_value；如果概率最大的c1不止1个，那么取值最大的c_idf_value
			// 如果这篇文章没有c1，只有c0，则相同逻辑取出idf
			double c_idf_value = cmpDocUserIDFValue(features);
			if(c_idf_value <= 0)
				c_idf_value = 1.0f;
			//readDoc_weight,根据scene做控制
			if(openScene.equals("sy"))
				readDoc_weight = readDoc_sy_weight;
			else if(openScene.equals("push"))
				readDoc_weight = readDoc_push_weight;
			else if(openScene.equals("ch"))
				readDoc_weight = readCH_weight;
			else
				readDoc_weight = 1.0f;
		
			// 特征是三元组
			int featuresNum = features.size()/3;
			//现在对文章做user表达，附带考虑行为权重readDoc_weight、user行为的IDF；
			for (int i = 0; i != featuresNum; i++) {
				int featureValueIndex = i * 3;
				int featureTagIndex = (i * 3) + 1;
				int featureTagImpIndex = (i * 3) + 2;
				String f_tag = features.get(featureTagIndex) + "#";
				String f_value = features.get(featureValueIndex);
				double b_score = Double.valueOf(features.get(featureTagImpIndex));
				
				if(b_score > 1.0)
					b_score = b_score - 100;
				if(f_value == null || f_value.length() < 2)
					continue;
				//排除掉c0
				if(f_tag.equals("c#") && b_score <=0)
					continue;
				// 排除掉无意义的词
				if (commenFuncs.valueInArrayJudge(GlobalParams.noSenseKeyword, f_value)) {
					continue;
				}
				//排除停用词
				if(HotWordUtil.nosense_word.contains(f_value) && !(f_tag.equals("c#")||f_tag.equals("sc#")||f_tag.equals("et#"))){
					continue;
				}
				//临时解决事件e bug问题
				/*if(GlobalParams.ebug.equals("e") && f_tag.equals("e#")){
					continue;
				}*/
				double f_imp = Math.abs(b_score);
				//对特定时间段的一些热点tag降权
				if((f_value.equals("奥运") || f_value.equals("明星")) && (Long.parseLong(tempReadDocTime) > 1470326400) && (Long.parseLong(tempReadDocTime) < 1471708800)){
					f_imp = f_imp*0.3f;
				}
				//weight太低的非c、sc，不计算
				if(f_imp < 0.5 && !(f_tag.equals("c#")||f_tag.equals("sc#")))
					continue;
			
				
				//add likun，我的临时逻辑，提高et表达价值，降低其它x n等现有weight
				if(f_tag.equals("et#"))
					f_imp = f_imp * 1.2f;
				
				if (f_value.equals(debugFeature)) {
					LOG.info("wordSta_ReadDoc f_imp*w score:" + f_imp * readDoc_weight * c_idf_value);
				}
				
				String tagJudge = addItemNew.judge_T_what(features.get(featureTagIndex));
				//添加对热点事件、优质稿源的表达by kedm
				if(features.get(featureTagIndex) != null && features.get(featureTagIndex).equals("e")){
					this.e_wordTime_list.add(f_value+"#"+tempReadDocTime+"#"+openScene);
				}
				if(features.get(featureTagIndex) != null && features.get(featureTagIndex).equals("cn")){
					if(HotWordUtil.word_read != null){
						//hotWordSplit.split(" ").length>=3||keyTemp.length()>4
						if(HotWordUtil.word_read.containsKey(f_value) && HotWordUtil.word_read.get(f_value) && f_value.length()>4){							
							this.e_wordTime_list.add(f_value+"#"+tempReadDocTime+"#"+openScene);
						}
					}
				}
				if(features.get(featureTagIndex) != null && features.get(featureTagIndex).equals("s1")){
					this.s1_wordTime_list.add(f_value+"#"+tempReadDocTime);
				}
				
				if ("t1".equals(tagJudge)) {
					this.topic1_word_List.add(f_value);
					String tempTime = this.topic1_wordTimeMap.get(f_value);
					if(tempTime == null || tempTime.compareTo(tempReadDocTime) < 0)
						this.topic1_wordTimeMap.put(f_value, tempReadDocTime);
					this.topic1_word_score.add(f_imp * readDoc_weight * c_idf_value);
				} else if ("t2".equals(tagJudge)) {
					this.topic2_word_List.add(f_value);
					String tempTime = this.topic2_wordTimeMap.get(f_value);
					if(tempTime == null || tempTime.compareTo(tempReadDocTime) < 0)
						this.topic2_wordTimeMap.put(f_value, tempReadDocTime);
					this.topic2_word_score.add(f_imp * readDoc_weight * c_idf_value);
				} else if ("t3".equals(tagJudge)) {
					this.topic3_word_List.add(f_value);
					String tempTime = this.topic3_wordTimeMap.get(f_value);
					if(tempTime == null || tempTime.compareTo(tempReadDocTime) < 0)
						this.topic3_wordTimeMap.put(f_value, tempReadDocTime);
					this.topic3_word_score.add(f_imp * readDoc_weight * c_idf_value);
					
				} else {
					continue;
				}
			}
		}
	}
	
	/**
	 * @Title: wordSta_ReadDoc
	 * @Description: 文章阅读行为统计
	 * @author liu_yi
	 * @param debugFeature
	 * @throws
	 */
	private void wordSta_ReadDoc_last2days(String debugFeature) {
		if (null == this.userLogAction || this.userLogAction.isEmpty()) {
			return;
		}
		
		String[] doc_array = this.userLogAction.split("\\$");
		
		//为计算速度考虑，降低doc size和计算规模到300
		int doc_num = 300;
		
		for (String tempDoc : doc_array) {
			String[] tempDocSplit = tempDoc.split("#");
		
			if(2 > tempDocSplit.length){
				continue;
			}

			if(doc_num-- <= 0)
				break;
			
			//docid doctime openscene(旧版本还没有openscene)
			String tempDocid = tempDocSplit[0];
			String tempReadDocTime = tempDocSplit[1];
			String openScene = "";
			
			if(3 == tempDocSplit.length)
				openScene = tempDocSplit[2];
			else if(2 == tempDocSplit.length)
				openScene = "sy";
			else
				continue;
			//query itemf
			long b = System.currentTimeMillis();
			List<String> features = queryItemFeatures.getXMLFeatures(tempDocid);
			/*if(features != null && features.contains("娱乐")){
				StringBuffer sb = new StringBuffer();
				for(String s : features){
					sb.append(s);
				}
				System.out.println(tempDocid+" "+sb.toString());
			}*/
			long e = System.currentTimeMillis();
			queryJedisTime = queryJedisTime + (e-b);
			
			
			//如果用户点击了文章，但是没有给出features，或者features中没有c，这个时候用secene
			if(tempDocid.matches("\\d{5,20}")){
				//修补features;主要是添加一个c,和features中要排重；娱乐、财经等这个时候也给出来了，不见得一定是c1
				features = get_F_byChannel(openScene,features);
			}

			if (null == features || features.isEmpty()) {
				//System.out.println(tempDoc);
				continue;
			}
			
			// 先求取这篇文章的c_idf_value;计算方法是，如果一篇文章有多个c1，那么取概率最大的那个c1对应的c_idf_value；如果概率最大的c1不止1个，那么取值最大的c_idf_value
			// 如果这篇文章没有c1，只有c0，则相同逻辑取出idf
			double c_idf_value = cmpDocUserIDFValue(features);
			if(c_idf_value <= 0)
				c_idf_value = 1.0f;
			//readDoc_weight,根据scene做控制
			if(openScene.equals("sy"))
				readDoc_weight = readDoc_sy_weight;
			else if(openScene.equals("push"))
				readDoc_weight = readDoc_push_weight;
			else if(openScene.equals("ch"))
				readDoc_weight = readCH_weight;
			else
				readDoc_weight = 1.0f;
		
			// 特征是三元组
			int featuresNum = features.size()/3;
			//现在对文章做user表达，附带考虑行为权重readDoc_weight、user行为的IDF；
			for (int i = 0; i != featuresNum; i++) {
				int featureValueIndex = i * 3;
				int featureTagIndex = (i * 3) + 1;
				int featureTagImpIndex = (i * 3) + 2;
				String f_tag = features.get(featureTagIndex) + "#";
				String f_value = features.get(featureValueIndex);
				
				double b_score = Double.valueOf(features.get(featureTagImpIndex));
				
				if(b_score > 1.0)
					b_score = b_score - 100;
				
				//排除掉c0
				if(f_tag.equals("c#") && b_score <=0)
					continue;
				// 排除掉无意义的词
				if (commenFuncs.valueInArrayJudge(GlobalParams.noSenseKeyword, f_value)) {
					continue;
				}
				
				double f_imp = Math.abs(b_score);
				
				//weight太低的非c、sc，不计算
				if(f_imp < 0.5 && !(f_tag.equals("c#")||f_tag.equals("sc#")||!f_tag.matches("k[a-z]#")))
					continue;
			
				
				//add likun，我的临时逻辑，提高et表达价值，降低其它x n等现有weight
				if(f_tag.equals("et#"))
					f_imp = f_imp * 1.2f;
				
				if (f_value.equals(debugFeature)) {
					LOG.info("wordSta_ReadDoc f_imp*w score:" + f_imp * readDoc_weight * c_idf_value);
				}
				
				String tagJudge = addItemNew.judge_T_what(features.get(featureTagIndex));
				if(f_imp * readDoc_weight * c_idf_value < 0.5)
					continue;
				if ("t1".equals(tagJudge)) {
					this.last_topic1_word_List.add(f_value);
					this.last_topic1_word_score.add(f_imp * readDoc_weight * c_idf_value);
				} else if ("t2".equals(tagJudge)) {
					this.last_topic2_word_List.add(f_value);
					this.last_topic2_word_score.add(f_imp * readDoc_weight * c_idf_value);
				} else if ("t3".equals(tagJudge)) {
					if(f_tag.startsWith("et")
							||f_tag.equals("loc")){
						this.last_topic3_word_List.add(f_value);
						this.last_topic3_word_score.add(f_imp * readDoc_weight * c_idf_value);
					}
					
				} else {
					continue;
				}
			}
		}
	}
	
	private void wordSta_Dislike(String debugFeature) {
		if (null == this.userLogAction || this.userLogAction.isEmpty()) {
			return;
		}
		
		String[] doc_array = this.userLogAction.split("\\$");
		
		//为计算速度考虑，降低doc size和计算规模到300
		int doc_num = 300;
		
		for (String tempDoc : doc_array) {
			String[] tempDocSplit = tempDoc.split("#");
		
			if(2 > tempDocSplit.length){
				continue;
			}

			if(doc_num-- <= 0)
				break;
			
			//docid doctime openscene(旧版本还没有openscene)
			String tempDocid = tempDocSplit[0];
			String tempReadDocTime = tempDocSplit[1];
			String openScene = "";
			
			if(3 == tempDocSplit.length)
				openScene = tempDocSplit[2];
			else
				continue;
			//query itemf
			long b = System.currentTimeMillis();
			List<String> features = queryItemFeatures.getXMLFeatures(tempDocid);
			long e = System.currentTimeMillis();
			queryJedisTime = queryJedisTime + (e-b);
			

			if (null == features || features.isEmpty()) {
				System.out.println(tempDoc);
				continue;
			}
			
			// 先求取这篇文章的c_idf_value;计算方法是，如果一篇文章有多个c1，那么取概率最大的那个c1对应的c_idf_value；如果概率最大的c1不止1个，那么取值最大的c_idf_value
			// 如果这篇文章没有c1，只有c0，则相同逻辑取出idf
			double c_idf_value = cmpDocUserIDFValue(features);
			if(c_idf_value <= 0)
				c_idf_value = 1.0f;
			//readDoc_weight,根据scene做控制
			if(openScene.equals("sy"))
				readDoc_weight = readDoc_sy_weight;
			else
				readDoc_weight = 1.0f;
		
			// 特征是三元组
			int featuresNum = features.size()/3;
			//现在对文章做user表达，附带考虑行为权重readDoc_weight、user行为的IDF；
			for (int i = 0; i != featuresNum; i++) {
				int featureValueIndex = i * 3;
				int featureTagIndex = (i * 3) + 1;
				int featureTagImpIndex = (i * 3) + 2;
				String f_tag = features.get(featureTagIndex) + "#";
				String f_value = features.get(featureValueIndex);
				
				double b_score = Double.valueOf(features.get(featureTagImpIndex));
				
				if(b_score > 1.0)
					b_score = b_score - 100;
				
				//排除掉c0
				if(f_tag.equals("c#") && b_score <=0)
					continue;
				// 排除掉无意义的词
				if (commenFuncs.valueInArrayJudge(GlobalParams.noSenseKeyword, f_value)) {
					continue;
				}
				
				double f_imp = Math.abs(b_score);
				
				//weight太低的非c、sc，不计算
				if(f_imp < 0.5 && !(f_tag.equals("c#")||f_tag.equals("sc#")||!f_tag.matches("k[a-z]#")))
					continue;
				
				String tagJudge = addItemNew.judge_T_what(features.get(featureTagIndex));
				
				if ("t1".equals(tagJudge)) {
					this.dislike_topic1_word_List.add(f_value);
				} else if ("t2".equals(tagJudge)) {
					this.dislike_topic2_word_List.add(f_value);
				} else if ("t3".equals(tagJudge)) {
					this.dislike_topic3_word_List.add(f_value);
				} else {
					continue;
				}
			}
		}
	}
	
	/**
	 * @Title: get_F_byChannel
	 * @Description: 用频道比如ent、finance等，补充c分类；
	 * @author likun
	 * @param channel 输入频道
	 * @param features 特征三元组
	 * @throws
	 */
	private List<String> get_F_byChannel(String channel, List<String> features) {
		// TODO Auto-generated method stub
		if(channel == null || channel.isEmpty())
			return null;
		if(features == null)
			features = new ArrayList<String>();
		
		//以下代码惨不忍睹，但是由于以后几乎不会改，从可读性和维护轻量考虑，代码中集成更合适
		String feature = null;
		String type = "";
		float weight = 0f;
		if(channel.equals("ent")){
			feature = "娱乐";
			type = "c";
			weight = -1.0f;
		}else if(channel.equals("finance")){
			feature = "财经";
			type = "c";
			weight = -1.0f;
		}else if(channel.equals("history")){
			feature = "历史";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("sports")){
			feature = "体育";
			type = "c";
			weight = -1.0f;
		}else if(channel.equals("fashion")){
			feature = "时尚";
			type = "c";
			weight = -1.0f;
		}else if(channel.equals("culture")){
			feature = "文化";
			type = "c";
			weight = -1.0f;
		}else if(channel.equals("HK")){
			feature = "港澳";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("edu")){
			feature = "教育";
			type = "c";
			weight = -1.0f;
		}else if(channel.equals("travel")){
			feature = "旅游";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("opinion")||channel.equals("commentary")){
			feature = "凤凰评论";
			type = "s1";
			weight = 1.0f;
		}else if(channel.equals("tech")){
			feature = "科技";
			type = "c";
			weight = -1.0f;
		}else if(channel.equals("society")){
			feature = "社会八卦";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("mil")){
			feature = "军事";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("tw")){
			feature = "台湾";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("auto")){
			feature = "汽车";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("house")){
			feature = "房产";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("health")){
			feature = "健康";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("book")){
			feature = "文化";
			type = "c";
			weight = -1.0f;
		}else if(channel.equals("fo")){
			feature = "佛教";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("astro")){
			feature = "星座";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("digi")){
			feature = "数码";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("game")){
			feature = "游戏";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("cmd")){
			feature = "段子";
			type = "c";
			weight = 1.0f;
		}else if(channel.equals("home")){
			feature = "家居";
			type = "c";
			weight = 1.0f;
		}
		
		//加入表达中
		if(feature == null || features.contains(feature))
			return features;
		else{
			features.add(0, String.valueOf(weight));
			features.add(0, type);
			features.add(0, feature);
		}
		
		return features;
	}


	/**
	 * @Title: cmpDocUserIDFValue
	 * @Description: 计算doc对user的具体表达能力，在user base IDF层次
	 * 先求取这篇文章的c_idf_value;计算方法是，如果一篇文章有多个c1，那么取概率最大的那个c1对应的c_idf_value；如果概率最大的c1不止1个，那么取值最大的c_idf_value
	        如果这篇文章没有c1，只有c0，则相同逻辑取出idf
	 * @author likun
	 * @param features 特征三元组
	 * @throws
	 */
	private double cmpDocUserIDFValue(List<String> features) {
		// TODO Auto-generated method stub
		if(features == null || features.isEmpty())
			return 1.0f;
		int featuresNum = features.size()/3;
		double c_idf_value = 2.0f;
		double max_c1_weight = 0f;
		for (int i = 0; i != featuresNum; i++) {
			int featureValueIndex = i * 3;
			int featureTagIndex = (i * 3) + 1;
			int featureTagImpIndex = (i * 3) + 2;
			String f_type = features.get(featureTagIndex);
			String f_value = features.get(featureValueIndex);
			
			double b_score = 0;
			
			try{
				b_score = Double.valueOf(features.get(featureTagImpIndex));
			}catch(Exception e){
				LOG.error("features num error:",e);
				LOG.error(features.toString());
			}
			
			if(b_score > 1.0)
				b_score = b_score - 100;
			
			// 排除掉无意义的词
			if (commenFuncs.valueInArrayJudge(GlobalParams.noSenseKeyword, f_value)) {
				continue;
			}
			
			//取c1
			if(f_type.equals("c") && b_score >0)
			{
				
				double temp_c_idf_value = idfQueryInterface.getInstance().queryIdfValue(f_value);
			
				if(b_score > max_c1_weight){
					c_idf_value = temp_c_idf_value;
					max_c1_weight = b_score;
				}else if(b_score == max_c1_weight){
					if(temp_c_idf_value > c_idf_value){
						c_idf_value = temp_c_idf_value;
					}
				}
			}
		
		}
		
		//c1层次没有查询到idf value，则看c0
		if(c_idf_value == 2.0f){
			double max_c0_weight = 0f;
			for (int i = 0; i != featuresNum; i++) {
				int featureValueIndex = i * 3;
				int featureTagIndex = (i * 3) + 1;
				int featureTagImpIndex = (i * 3) + 2;
				String f_type = features.get(featureTagIndex);
				String f_value = features.get(featureValueIndex);
				
				double b_score = Double.valueOf(features.get(featureTagImpIndex));
				
				if(b_score > 1.0)
					b_score = b_score - 100;
				
				// 排除掉无意义的词
				if (commenFuncs.valueInArrayJudge(GlobalParams.noSenseKeyword, f_value)) {
					continue;
				}
				
				//取c0
				if(f_type.equals("c") && b_score < 0)
				{
					b_score = Math.abs(b_score);
					
					double temp_c_idf_value = idfQueryInterface.getInstance().queryIdfValue(f_value);
				
					if(b_score > max_c0_weight){
						c_idf_value = temp_c_idf_value;
						max_c0_weight = b_score;
					}else if(b_score == max_c0_weight){
						if(temp_c_idf_value > c_idf_value){
							c_idf_value = temp_c_idf_value;
						}
					}
				}
			
			}
		}
		
		if(c_idf_value > 1.0f)
			c_idf_value = 1.0f;
		
		return c_idf_value;
	}

	/**
	 * @Title: wordSta_ReadKeyword
	 * @Description: 关键词点入行为统计
	 * @author liu_yi
	 * @param debugFeature
	 * 
	 * 日志样例：
	 * 
	 * 
	 * @throws
	 */
	private void wordSta_ReadKeyword(String debugFeature) {
		if (null == this.userLogAction || this.userLogAction.isEmpty()) {
			return;
		}
		
		String[] keyword_array = this.userLogAction.split("\\$");
		
		//为计算速度考虑，降低doc size和计算规模到500
		int keyword_num = 15;
		
		// 由于TagsQueryInterface给不出关键词列表中的关键词重要度值,自己给个默认值
		double default_uk_imp = 1.0;
		for (String keyword : keyword_array) {
			String[] tempKeywordSplit = keyword.split("#");
			if (2 != tempKeywordSplit.length) {
				continue;
			}
			
			String tempKeyword = tempKeywordSplit[0];
			if (null == tempKeyword || tempKeyword.isEmpty()) {
				continue;
			}
		
			// 排除掉无意义的词
			if (commenFuncs.valueInArrayJudge(GlobalParams.noSenseKeyword, tempKeyword)) {
				continue;
			}
			
			if(keyword_num-- <= 0)
				break;
			
			if (tempKeyword.equals(debugFeature)) {
				LOG.info("wordSta_ReadKeyword f_imp*w score:" + readKW_weight * default_uk_imp);
			}
			
			// 根据keyword遍历graph提取对应分类体系,并归入相应topic list
			String queryWord = tempKeyword;
			//以word来查询,一个word可能有多个type,节点不唯一,如"银行"既是sc,也是cn
			ArrayList<GraphNode> graphNodeList=Graph.getInstance().queryWord(queryWord);
			
			String tempKeyword_tag = "";
			String minT123_type = "";
			if(graphNodeList == null || graphNodeList.isEmpty())
				minT123_type = "t3";//默认是t3
			else{
				for (GraphNode gn:graphNodeList) {
					String c0Orc1 = gn.getData().getTypeLabel();
					if(c0Orc1 != null && c0Orc1.equals("c0"))
						continue;
						
					tempKeyword_tag = gn.getData().getType();
					String T123_type = addItemNew.judge_T_what(tempKeyword_tag);
					if(T123_type == null || T123_type.isEmpty())
						continue;
					
					if(T123_type.compareTo(minT123_type) > 0 || minT123_type.isEmpty())
						minT123_type = T123_type;
				}
			}
			
			if ("t1".equals(minT123_type)) {
				//t1获取idf信息
				ArrayList<String> al_features = new ArrayList<String>();
				al_features.add(tempKeyword);
				al_features.add("c");
				al_features.add(String.valueOf(default_uk_imp));
				double c_idf_value = cmpDocUserIDFValue(al_features);
				if(c_idf_value <= 0)
					c_idf_value = 1.0f;
							
				this.topic1_word_List.add(tempKeyword);
				this.topic1_wordTimeMap.put(tempKeyword, tempKeywordSplit[1]);
				this.topic1_word_score.add(readKW_weight * default_uk_imp * c_idf_value);
				
			} else if ("t2".equals(minT123_type)) {
				this.topic2_word_List.add(tempKeyword);
				this.topic2_wordTimeMap.put(tempKeyword, tempKeywordSplit[1]);
				this.topic2_word_score.add(readKW_weight * default_uk_imp);
			} else if ("t3".equals(minT123_type)) {
				this.topic3_word_List.add(tempKeyword);
				this.topic3_wordTimeMap.put(tempKeyword, tempKeywordSplit[1]);
				this.topic3_word_score.add(readKW_weight * default_uk_imp);
			} else {
				continue;
			}
			
		}
	}
	
	
	/**
	 * @Title: wordSta_ReadKeyword
	 * @Description: 关键词点入行为统计
	 * @author liu_yi
	 * @param debugFeature
	 * 
	 * 日志样例：
	 * 
	 * 
	 * @throws
	 */
	private void wordSta_userSearchWord(String debugFeature) {
		if (null == this.userLogAction || this.userLogAction.isEmpty()) {
			return;
		}
		
		String[] keyword_array = this.userLogAction.split("\\$");
		
		//为计算速度考虑，降低doc size和计算规模到500
		int keyword_num = 15;
				
		
		// 由于TagsQueryInterface给不出关键词列表中的关键词重要度值,自己给个默认值
		double default_uk_imp = 1.0;
		for (String keyword : keyword_array) {
			String[] tempKeywordSplit = keyword.split("#");
			if (2 != tempKeywordSplit.length) {
				continue;
			}
			
			String tempKeyword = tempKeywordSplit[0];
			if (null == tempKeyword || tempKeyword.isEmpty()) {
				continue;
			}
		
			// 排除掉无意义的词
			if (commenFuncs.valueInArrayJudge(GlobalParams.noSenseKeyword, tempKeyword)) {
				continue;
			}
			
			if(keyword_num-- <= 0)
				break;
			
			if (tempKeyword.equals(debugFeature)) {
				LOG.info("wordSta_ReadKeyword f_imp*w score:" + search_weight * default_uk_imp);
			}
			
			// 根据keyword遍历graph提取对应分类体系,并归入相应topic list
			String queryWord = tempKeyword;
			//以word来查询,一个word可能有多个type,节点不唯一,如"银行"既是sc,也是cn
			ArrayList<GraphNode> graphNodeList=Graph.getInstance().queryWord(queryWord);
			
			String tempKeyword_tag = "";
			String minT123_type = "";
			if(graphNodeList == null || graphNodeList.isEmpty())
				minT123_type = "t3";//默认是t3
			else{
				for (GraphNode gn:graphNodeList) {
					String c0Orc1 = gn.getData().getTypeLabel();
					if(c0Orc1 != null && c0Orc1.equals("c0"))
						continue;
						
					tempKeyword_tag = gn.getData().getType();
					String T123_type = addItemNew.judge_T_what(tempKeyword_tag);
					if(T123_type == null || T123_type.isEmpty())
						continue;
					
					if(T123_type.compareTo(minT123_type) > 0 || minT123_type.isEmpty())
						minT123_type = T123_type;
				}
			}
			
			if ("t1".equals(minT123_type)) {
				//t1获取idf信息
				ArrayList<String> al_features = new ArrayList<String>();
				al_features.add(tempKeyword);
				al_features.add("c");
				al_features.add(String.valueOf(default_uk_imp));
				double c_idf_value = cmpDocUserIDFValue(al_features);
				if(c_idf_value <= 0)
					c_idf_value = 1.0f;
				
				this.topic1_word_List.add(tempKeyword);
				this.topic1_wordTimeMap.put(tempKeyword, tempKeywordSplit[1]);
				this.topic1_word_score.add(readKW_weight * default_uk_imp * c_idf_value);
			} else if ("t2".equals(minT123_type)) {
				this.topic2_word_List.add(tempKeyword);
				this.topic2_wordTimeMap.put(tempKeyword, tempKeywordSplit[1]);
				this.topic2_word_score.add(readKW_weight * default_uk_imp);
			} else if ("t3".equals(minT123_type)) {
				this.topic3_word_List.add(tempKeyword);
				this.topic3_wordTimeMap.put(tempKeyword, tempKeywordSplit[1]);
				this.topic3_word_score.add(readKW_weight * default_uk_imp);
			} else {
				continue;
			}
			
		}
	}
	
	/**
	 * @Title: wordSta_UserBook
	 * @Description: 订阅行为统计
	 * @author liu_yi
	 * @param debugFeature
	 * @throws
	 */
	private void wordSta_UserBook(String debugFeature) {
		if (null == this.userLogAction || this.userLogAction.isEmpty()) {
			return;
		}
		
		if (null == this.userLogAction || this.userLogAction.isEmpty()) {
			return;
		}
		
		String[] keyword_array = this.userLogAction.split("\\$");
		// 由于TagsQueryInterface给不出关键词列表中的关键词重要度值,自己给个默认值
		double default_ub_imp = 1.0;
		for (String keyword : keyword_array) {
			String[] tempKeywordSplit = keyword.split("#");
			if (2 != tempKeywordSplit.length) {
				continue;
			}
			
			String tempKeyword = tempKeywordSplit[0];
			if (null == tempKeyword || tempKeyword.isEmpty()) {
				continue;
			}
			
			//排除掉c0?
			
			// 排除掉无意义的词
			if (commenFuncs.valueInArrayJudge(GlobalParams.noSenseKeyword, tempKeyword)) {
				continue;
			}
			
			if (tempKeyword.equals(debugFeature)) {
				LOG.info("wordSta_UserBook f_imp*w score:" + bookWord_weight * default_ub_imp);
			}
			
			// 根据tempKeyword的tag信息(c/sc),归入相应topic list
			String tempKeyword_tag = TagsQueryInterface.getInstance().queryTagType(tempKeyword);
			if (null == tempKeyword_tag || tempKeyword_tag.isEmpty()) {
				// 默认放到topic3吧
				this.topic3_word_List.add(tempKeyword);
				this.topic3_wordTimeMap.put(tempKeyword, tempKeywordSplit[1]);
				this.topic3_word_score.add(bookWord_weight * default_ub_imp);
				
				continue;
			}
			String tagJudge = "#"+tempKeyword_tag+"#";
			if ("#c#".indexOf(tagJudge) >= 0) {
				this.topic1_word_List.add(tempKeyword);
				this.topic1_wordTimeMap.put(tempKeyword, tempKeywordSplit[1]);
				this.topic1_word_score.add(bookWord_weight * default_ub_imp);
			} else if ("#sc#e#cn#t#s#s1#".indexOf(tagJudge) >= 0) {
				this.topic2_word_List.add(tempKeyword);
				this.topic2_wordTimeMap.put(tempKeyword, tempKeywordSplit[1]);
				this.topic2_word_score.add(bookWord_weight * default_ub_imp);
			} else if ("#et#kb#ks#kq#kr#nr#x#nz#ne#n#nt#ns".indexOf(tagJudge) >= 0) {
				this.topic3_word_List.add(tempKeyword);
				this.topic3_wordTimeMap.put(tempKeyword, tempKeywordSplit[1]);
				this.topic3_word_score.add(bookWord_weight * default_ub_imp);
			} else { 
				continue;
			}
			
		}
	}
	
	public String getUserLogAction() {
		return userLogAction;
	}

	public void setUserLogAction(String userLogAction) {
		this.userLogAction = userLogAction;
	}

	public List<String> getTopic1_word_List() {
		return topic1_word_List;
	}

	public void setTopic1_word_List(List<String> topic1_word_List) {
		this.topic1_word_List = topic1_word_List;
	}

	public List<String> getTopic2_word_List() {
		return topic2_word_List;
	}

	public void setTopic2_word_List(List<String> topic2_word_List) {
		this.topic2_word_List = topic2_word_List;
	}

	public List<String> getTopic3_word_List() {
		return topic3_word_List;
	}

	public void setTopic3_word_List(List<String> topic3_word_List) {
		this.topic3_word_List = topic3_word_List;
	}

	public List<Double> getTopic1_word_score() {
		return topic1_word_score;
	}

	public void setTopic1_word_score(List<Double> topic1_word_score) {
		this.topic1_word_score = topic1_word_score;
	}

	public List<Double> getTopic2_word_score() {
		return topic2_word_score;
	}

	public void setTopic2_word_score(List<Double> topic2_word_score) {
		this.topic2_word_score = topic2_word_score;
	}

	public List<Double> getTopic3_word_score() {
		return topic3_word_score;
	}

	public void setTopic3_word_score(List<Double> topic3_word_score) {
		this.topic3_word_score = topic3_word_score;
	}
	
	public Map<String, String> getTopic1_wordTimeMap() {
		return topic1_wordTimeMap;
	}

	public void setTopic1_wordTimeMap(Map<String, String> topic1_wordTimeMap) {
		this.topic1_wordTimeMap = topic1_wordTimeMap;
	}

	public Map<String, String> getTopic2_wordTimeMap() {
		return topic2_wordTimeMap;
	}

	public void setTopic2_wordTimeMap(Map<String, String> topic2_wordTimeMap) {
		this.topic2_wordTimeMap = topic2_wordTimeMap;
	}

	public Map<String, String> getTopic3_wordTimeMap() {
		return topic3_wordTimeMap;
	}

	public void setTopic3_wordTimeMap(Map<String, String> topic3_wordTimeMap) {
		this.topic3_wordTimeMap = topic3_wordTimeMap;
	}
	
	public List<String> getLast_topic1_word_List() {
		return last_topic1_word_List;
	}

	public void setLast_topic1_word_List(List<String> last_topic1_word_List) {
		this.last_topic1_word_List = last_topic1_word_List;
	}

	public List<String> getLast_topic2_word_List() {
		return last_topic2_word_List;
	}

	public void setLast_topic2_word_List(List<String> last_topic2_word_List) {
		this.last_topic2_word_List = last_topic2_word_List;
	}

	public List<String> getLast_topic3_word_List() {
		return last_topic3_word_List;
	}

	public void setLast_topic3_word_List(List<String> last_topic3_word_List) {
		this.last_topic3_word_List = last_topic3_word_List;
	}

	public List<Double> getLast_topic1_word_score() {
		return last_topic1_word_score;
	}

	public void setLast_topic1_word_score(List<Double> last_topic1_word_score) {
		this.last_topic1_word_score = last_topic1_word_score;
	}

	public List<Double> getLast_topic2_word_score() {
		return last_topic2_word_score;
	}

	public void setLast_topic2_word_score(List<Double> last_topic2_word_score) {
		this.last_topic2_word_score = last_topic2_word_score;
	}

	public List<Double> getLast_topic3_word_score() {
		return last_topic3_word_score;
	}

	public void setLast_topic3_word_score(List<Double> last_topic3_word_score) {
		this.last_topic3_word_score = last_topic3_word_score;
	}
	public List<String> getE_wordTime_list() {
		return e_wordTime_list;
	}

	public void setE_wordTime_list(List<String> e_wordTime_list) {
		this.e_wordTime_list = e_wordTime_list;
	}

	public List<String> getS1_wordTime_list() {
		return s1_wordTime_list;
	}

	public void setS1_wordTime_list(List<String> s1_wordTime_list) {
		this.s1_wordTime_list = s1_wordTime_list;
	}
	
	public List<String> getDislike_topic1_word_List() {
		return dislike_topic1_word_List;
	}

	public void setDislike_topic1_word_List(List<String> dislike_topic1_word_List) {
		this.dislike_topic1_word_List = dislike_topic1_word_List;
	}

	public List<String> getDislike_topic2_word_List() {
		return dislike_topic2_word_List;
	}

	public void setDislike_topic2_word_List(List<String> dislike_topic2_word_List) {
		this.dislike_topic2_word_List = dislike_topic2_word_List;
	}

	public List<String> getDislike_topic3_word_List() {
		return dislike_topic3_word_List;
	}

	public void setDislike_topic3_word_List(List<String> dislike_topic3_word_List) {
		this.dislike_topic3_word_List = dislike_topic3_word_List;
	}

}
