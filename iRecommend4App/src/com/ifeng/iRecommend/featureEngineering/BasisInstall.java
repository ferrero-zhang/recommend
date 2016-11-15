package com.ifeng.iRecommend.featureEngineering;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement.TimeSensitiveJudgement;
import com.ifeng.iRecommend.featureEngineering.dataStructure.JsonFromCMPP;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.ArticleSourceData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.CommonDataSub;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.CustomWordUpdate;
import com.ifeng.iRecommend.zxc.bdhotword.HotWordDetector;

public class BasisInstall {
	static Logger LOG = Logger.getLogger(BasisInstall.class);
	// 阻塞队列的最大长度，防止内存溢出。
	protected static final int MAX_QUEUE_SIZE = 300;
	// 致命毒药，优雅关闭后续的工作线程
	protected static final JsonFromCMPP DEADLY_POISON = new JsonFromCMPP();
	// 数据库操作代码初始化
	protected static IKVOperationv2 itemop = new IKVOperationv2("appitemdb");
	protected static IKVOperationv2 appbillop = new IKVOperationv2("appbilldb");
	// 获取关键词
	protected static KeywordValueJudge kvJudge = KeywordValueJudge.getKVJudgeInstance();
	// feature修正规则引擎
	protected static RuleModify ruleModify = RuleModify.getInstance();
	protected static HashSet<String> stopword = InitialLoadFile.readStopword(LoadConfig.lookUpValueByKey("StopWordPath"));
	// 一点资讯白名单
	protected static HashSet<String> yidianBlackSet = InitialLoadFile.yidianBlackReader(LoadConfig.lookUpValueByKey("yidianBlackPath"));
	// 客户端原创内容集
	protected static HashSet<String> originalSet = InitialLoadFile.readOriginalList(LoadConfig.lookUpValueByKey("originalPath"));
	protected static ExtractTags extractTags = ExtractTags.getInstance();
	protected static TimeSensitiveJudgement timeSensitiveJudgement = TimeSensitiveJudgement.getInstance();
	// 通用数据层服务
	private static CommonDataSub cds;
	protected static CustomWordUpdate cwu = CustomWordUpdate.getInstance();
	// 热词订阅服务
	protected static HotWordDetector hotwordOb = new HotWordDetector();
	protected static ArticleSourceData sourceData = ArticleSourceData.getInstance();
	protected static ConcurrentHashMap<String, String> articleSourceMap = sourceData.getArticleSourceMap();

	/**
	 * 启动通用数据层线程 加载配置文件等
	 * 
	 * @param startTime
	 * @param reverseFlag
	 */
	static {
		DEADLY_POISON.setId("DEADLY_POISON");
		BasicConfigurator.configure();
		PropertyConfigurator.configure("conf/log4j.properties");
		// 通用数据层线程启动
		cds = new CommonDataSub();
		Thread t = new Thread(cds);
		t.start();
	}
}
