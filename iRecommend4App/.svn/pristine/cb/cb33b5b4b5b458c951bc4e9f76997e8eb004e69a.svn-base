/**
 * 
 */
package com.ifeng.iRecommend.fieldDicts;

import java.util.ArrayList;
import java.util.HashMap;

import com.ifeng.commen.Utils.LoadConfig;

/**
 * <PRE>
 * 作用 : 
 *   从配置文件中读取全局字典；包括hbase和redis中各个功能数据接口的设置；
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
 *          1.0          2013-7-17        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class fieldDicts {
	// user log table in hbase
	public static String pcUserLogTableNameInHbase = "zhineng_irecommend_pclog_table";
	public static String appUserLogTableNameInHbase = "zhineng_irecommend_applog_table_time";
	public static String appRealtimeUserLogTableNameInHbase = "zhineng_irecommend_applog_table_time_more";
	// item table in hbase
	public static String pcItemTableNameInHbase = "zhineng_irecommend_pc_itemlist";
	public static String appItemTableNameInHbase = "zhineng_irecommend_wap_itemlist";

	// user model
	public static String itemFrontUrl = "http://223.203.209.98:8080/irend/irecommend_main/irecommend_main.txt";
	public static String modelPath = "D:\\workspace\\iRecommend\\testenv\\uecommsermodel\\";
	public static String oldModelFile = "old/";
	public static String newModelFile = "new/";
	public static String bakModelFile = "bak/";
	// item model
	public static String itemModelPath = "D:\\workspace\\iRecommend\\testenv\\itemmodel\\";

	public static String trainStatusFile = "status";
	public static String docUserMapFile = "doc2user";
	public static String luceneIndexField = "usermodel";
	public static String itemQueryFromSearch = "http://search.ifeng.com/so/rawsearch?q=url:";
	public static String latestItemFromSearch = "http://10.32.21.103:8080/solr/core1/select?";
	public static String itemFromTongjiSF = "http://10.32.21.72/wap_doc/";
	public static String itemFromTongjiAPP = "http://10.32.21.72/app_doc/";
	public static String pcTreeMappingFile = "conf/AppTreeMapping.txt";
	public static String pcFrontTreeMappingFile = "conf/Front_TreeMapping.txt";// "D:\\workspace\\iRecommend\\src\\com\\ifeng\\iRecommend\\dingjw\\itemParser\\front_AppTreeMapping.txt";//"conf/Front_TreeMapping.txt";
	public static String pcSlideTreeMappingFile = "conf/SlideTreeMapping.txt";
	public static String appTreeMappingFile = "conf/AppTreeMapping.txt";// "D:\\workspace\\iRecommend\\src\\com\\ifeng\\iRecommend\\dingjw\\itemParser\\AppTreeMapping.txt";
	public static String frontAppTreeMappingFile = "conf/front_AppTreeMapping.txt";
	public static String appSlideTreeMappingFile = "conf/SlideTreeMapping.txt";
	public static String itemsOfLvAPath = "D:\\RankModel\\AItems\\";
	public static String itemsOfLvAFile = "items_A.txt";

	public static long userLogIntelMinutes = 1000 * 180L;// 2分钟内相同记录合并

	// stopwordsfile
	public static String stopwordsFile = "stopwords.txt";

	// test users
	public static String testUsersFile = "D:\\workspace\\iRecommend\\testenv\\testusers.txt";

	// redis中存放user待推荐list的db ID，对外
	public static int userList_dbid_in_redis = 9;
	// redis中存放user待推荐完整数据的db ID，不对外，只定时导向userList_dbid_in_redis；
	public static int userRecommendedItems_dbid_in_redis = 7;
	// 存放A级别投放item，默认出现在所有user的反馈列表上,db = userRecommendedItems_dbid_in_redis
	public static String A_level_itemsInRedis = "ALevel";
	// 存放backup投放item，将对那些资源位有空余的user进行补充,db =
	// userRecommendedItems_dbid_in_redis
	public static String backupItemsInRedis = "backupItems";

	// redis中存放user history behaviours的db ID,缓存队列用
	public static int userHistoryBehaviours_dbid_in_redis = 8;

	// topic model
	public static String tm_doc_dir = "./tm/doc/";//"D:\\workspace\\iRecommend\\testenv\\tm\\doc\\";// topic
																						// model
	public static String tm_word_dir = "./tm/word/";//"D:\\workspace\\iRecommend\\testenv\\tm\\word\\";// topic
																						// model
	public static String tm_words_file = "./tm/dict_topicmodel/";//"D:\\workspace\\iRecommend\\testenv\\tm\\dict_topicmodel";// dict_topicmodel
	public static int tm_topic_num = 108;
	public static int tm_matrixA_size = 500000;// doc size
	public static int tm_matrixB_size = 50000;// word size

	// 确定item每个主题规范化后的value；item tag's Level(A\B\C\D)
	public static HashMap<String, Integer> hm_tagLevels = new HashMap<String, Integer>();

	// item hot weight's Level(A\B\C\D)
	public static HashMap<String, Integer> hm_itemHotLevels = new HashMap<String, Integer>();


	// 降权周期
	public static int degradePeriod = 4;
	// 一周期多少分钟
	public static int periodUnit = 60;

	// 投放基数，频道有异
	public static HashMap<String, Float> hm_BaseRankLines = new HashMap<String, Float>() {
		{
			// 特别小众的频道
			put("home", 0.01f);
			put("fo", 0.005f);
			put("stock", 0.01f);
			put("air", 0.01f);
			put("money", 0.01f);
			put("astro", 0.005f);
			put("digi", 0.01f);
			put("baby", 0.01f);
			put("health", 0.01f);

			// 一般频道
			put("mil", 0.03f);
			put("zq", 0.03f);
			put("sports", 0.02f);
			put("taiwan", 0.03f);
			put("ent", 0.02f);
			put("finance", 0.02f);
			put("tech", 0.02f);
			put("house", 0.02f);
			put("edu", 0.01f);
			put("fashion", 0.02f);

			// 大众内容或者特色内容
			put("history", 0.05f);
			put("world", 0.04f);
			put("mainland", 0.05f);
			put("discovery", 0.05f);

			// 其它
			put("other", 0.01f);
		}
	};

	//生命周期，频道有异
	public static HashMap<String, Integer> hm_itemLifeTimes = new HashMap<String, Integer>() {
		{
			//0.特别
			put("AA", 12);
			
			//1
			put("sports", 18);
			put("zq", 18);
			put("lanqiu", 18);
			//2
			put("news", 12);
			put("mainland", 12);
			put("world", 12);
			put("hongkong",24);
			put("taiwan", 24);
			put("house", 18);
			put("finance", 18);
			//3
			put("society", 12);
			put("opinion", 18);
			put("video", 18);
			put("game", 24);
			put("mil", 18);
			put("auto", 24);
			put("culture", 24);
			put("digi", 18);
			put("tech", 18);
			put("ent", 18);
			put("fashion", 18);
			put("edu", 24);
			//4
			put("blog", 24);
			put("history", 36);			
			put("book", 24);
			put("fo", 24);
			put("baby", 36);
			put("discovery", 36);
			//5
			put("astro", 36);
			put("health",36);
			put("travel",36);
			// 其它
			put("other", 18);
		}
	};

//	// 各个hotlevel对应的生命周期
//	// item's lifetime(A\B\C\D)
//	public static HashMap<String, Integer> hm_itemLifeTime = new HashMap<String, Integer>();
//	hm_itemLifeTime.put("AA", 12);
//	hm_itemLifeTime.put("A", 24);
//	hm_itemLifeTime.put("B", 16);
//	hm_itemLifeTime.put("C", 8);
//	hm_itemLifeTime.put("D", 8);
	
	
	// 统计系统实时流量统计接口地址列表，用于进行热度指定和文章筛选
	public static ArrayList<String> tongjiUrls = new ArrayList<String>();

	static {
		String tongjiUrlSplits[] = LoadConfig.lookUpValueByKey("tongjiUrls")
				.split(";");
		for (String url : tongjiUrlSplits)
			tongjiUrls.add(url);

		hm_tagLevels.put("A", 8);
		hm_tagLevels.put("B", 5);
		hm_tagLevels.put("C", 3);
		hm_tagLevels.put("D", 1);

		// A level,用于内容热度score计算；
		hm_itemHotLevels.put("A", 16);
		// B C D level
		hm_itemHotLevels.put("B", 2);
		hm_itemHotLevels.put("C", 0);
		hm_itemHotLevels.put("D", -1);
		hm_itemHotLevels.put("E", -2);

	}

	// 投放的最小score阈值，过滤掉太不靠谱的匹配，虽然有rank比例保证
	public static float minMatchScore = 0.05f;

	//评论接口
	public static String commentQueryUrl = "http://sisp.ifeng.com/sisp/CountComment.jsp";
	
	//PC端视觉采集信息形成的hotlevel文件
	public static String pcVisualHotFile = "/projects/zhineng/pcHotPredict/pcHotLevel";
	//地域news映射表
	public static String locationMapFilePath = "./locationMap.txt";
	
	
// 分类算法系统配置字段
	//分类算法用停用词表
	public static String stopwordsFileForCL = "D:\\workspace\\iRecommend\\testenv\\stopwords.txt";
	//分类算法用特征词表
	public static String featuresFileForCL = "D:\\workspace\\iRecommend\\testenv\\stopwords.txt";
	//分类算法用svm算法model file
	public static String svmModelFileForCL = "D:\\workspace\\iRecommend\\testenv\\stopwords.txt";
	//分类算法用svm算法scale file
	public static String svmScaleFileForCL = "D:\\workspace\\iRecommend\\testenv\\stopwords.txt";
		

	
	//人工干预投放模型的接口文件，目前是黑名单的功能
	public static String blackListFileForRankModel = "blacklist";
	
	//新内容池生命周期配置文件路径
	public static String newsLifeTimePropertiesFilePath = "conf/newsLifeTime.properties";
}
