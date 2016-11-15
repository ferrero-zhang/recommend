package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.MailSenderWithRotam;

/**
 * <PRE>
 * 作用 : 
 *   数据一致性检查器。
 *      启动线程，检查器线程每隔一定时间，检查本地数据与数据中心数据是否一致。
 *      如果检查发现数据不一致，说明一致性可能存在问题，发邮件通知相关人员。
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
 *          1.0          2015年12月5日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class DataConsistencyChecker extends TimerTask{
	private static Logger logger = LoggerFactory.getLogger(DataConsistencyChecker.class);
	
	/**
	 * @Fields appName : 应用的项目名称
	 */
	private String appName = "";

	
	// 全局使用的Jedis实例
	protected static Jedis databaseJedis;

	protected static String commonDataRedisHost;
	protected static int commonDataRedisPort;

	/**
	 * @Title: init
	 * @Description: 部分类常量初始化
	 * @author liu_yi
	 * @throws
	 */
	public void init() {
		try {
			commonDataRedisPort = Integer.valueOf(LoadConfig
					.lookUpValueByKey("commonDataRedisPort"));
			commonDataRedisHost = LoadConfig
					.lookUpValueByKey("commonDataRedisHost");
			// 默认超时时间正常情况够了，不用设置
			databaseJedis = new Jedis(commonDataRedisHost, commonDataRedisPort);
		} catch (Exception e) {
			logger.error("Redis Init Error:", e);
			databaseJedis = null;
		}
	}
	
	
	public DataConsistencyChecker(String appName) {
		super();
		this.setAppName(appName);
		this.init();
	}
	
	@Override
	public void run() {
		logger.info("execute " + DataConsistencyChecker.class);
		System.out.println("execute " + DataConsistencyChecker.class);
		
		// 在自己的应用中，如果没有订阅该数据，可以注释掉相应的检查代码
		boolean entLibCheckState = this.checkEntlibData();
		// 数据不一致
		if (!entLibCheckState) {
			// 发报警邮件
			String mail_subject = "Data Consistency Warn From APP:" + this.getAppName();
			String mail_content = "<strong>entLibData Consistency Error</strong><br>For Details, Please Check The App Error Log.<br>";
			String mail_receiver_config_name = "entLibExceptionReceivers";
			MailSenderWithRotam mswr = new MailSenderWithRotam(mail_subject, mail_content, mail_receiver_config_name);
			mswr.sendEmailWithRotam();
		}
		
		boolean blacklistCheckState = this.checkBlackListData();		
		// 数据不一致
		if (!blacklistCheckState) {
			// 发报警邮件
			String mail_subject = "Data Consistency Warn From APP:" + this.getAppName();
			String mail_content = "<strong>blacklistData Consistency Error</strong><br>For Details, Please Check The App Error Log.<br>";
			String mail_receiver_config_name = "blacklistExceptionReceivers";
			MailSenderWithRotam mswr = new MailSenderWithRotam(mail_subject, mail_content, mail_receiver_config_name);
			mswr.sendEmailWithRotam();
		}
		
		// 其他数据的检查
	}
	
	/**
	 * @Title: checkEntlibData
	 * @Description: 实体库数据一致性检查方法
	 * @author liu_yi
	 * @return 数据不一致的时候
	 * @throws
	 */
	public boolean checkEntlibData() {
		boolean result = true;
		
		// 检查一致性：
		// 遍历本地的File，检查每个File与redis中的File比较item数量，如果不一致，记录下不一致的item到log中，同时返回false
		
		// (实体库暂时不支持按File查询，本方法先空着，泳刚改了KnowledgeBaseBuild之后再实现)
		
		return result;
	}
	
	/**
	 * @Title: checkBlackListData
	 * @Description: 黑名单数据一致性检查
	 * @author liu_yi
	 * @return
	 * @throws
	 */
	public boolean checkBlackListData() {
		boolean result = true;
		databaseJedis.select(1);
		
		List<String> all_redis_data = databaseJedis.lrange("blackList", 0, -1);
		List<String> redis_black_list = new CopyOnWriteArrayList<String>();
		for (String tempElem : all_redis_data) {
			String[] tempElemSplit = tempElem.split("_");
			if (tempElemSplit[0].equals("article")) {
				// 添加id到黑名单
				redis_black_list.add(tempElemSplit[1]);
				
				// 添加title到黑名单
				redis_black_list.add(tempElemSplit[2]);
			} else if (tempElemSplit[0].equals("keyword")) {
				// 添加关键词到黑名单
				redis_black_list.add(tempElemSplit[1]);
			}
		}	
		
		System.out.println("redis_black_list size:" + redis_black_list.size());
		
		List<String> blacklist_in_app = new ArrayList<String>(BlackListData.getInstance().get_blacklist());
		
		System.out.println("blacklist_in_app size:" + blacklist_in_app.size());
		
		if (redis_black_list.size() != blacklist_in_app.size()) {
			result = false;
			logger.error("Black_list Consistency Error!");
			
			if (blacklist_in_app.size() < redis_black_list.size()) {
				redis_black_list.removeAll(blacklist_in_app);
				logger.error("Has Loss Blacklist Elems:" + redis_black_list);
				System.out.println("Has Loss Blacklist Elems:" + redis_black_list);
			} else if (blacklist_in_app.size() > redis_black_list.size()){
				blacklist_in_app.removeAll(redis_black_list);
				logger.error("App Blacklist Has More Elems:" + blacklist_in_app.toString());
				System.out.println("App Blacklist Has More Elems:" + blacklist_in_app);
			} 
		} else if (blacklist_in_app.size() == redis_black_list.size()) {
			// 如果一致，remove之后数据应该为空
			blacklist_in_app.removeAll(redis_black_list);			
			
			if (blacklist_in_app.size() != 0) {
				logger.error("Blacklist Elem Consistency Error, blacklist_in_app:" + blacklist_in_app);
				System.out.println("Blacklist Elem Consistency Error, blacklist_in_app:" + blacklist_in_app);
				result = false;
			}
		} 
		
		return result;
	}
	
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	// test
	public static void main(String[] args) {
		Timer timer = new Timer();
		int checkInterval_minutes = 30; 
		long delay1 = 5 * 1000;
		long period1 = checkInterval_minutes * 60 * 1000;
		// 从现在开始 5秒钟之后，每隔 20秒钟执行一次 job
		timer.schedule(new DataConsistencyChecker("rec4user"), delay1, 5000);
	}
}
