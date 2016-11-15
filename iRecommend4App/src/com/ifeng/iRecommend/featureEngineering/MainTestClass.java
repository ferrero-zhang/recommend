package com.ifeng.iRecommend.featureEngineering;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.hexl.redis.GetUsefulKeyFromRedis;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.EntityLibQuery;
import com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement.TimeSensitiveInfo;
import com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement.TimeSensitiveJudgement;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.ArticleSourceData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.CommonDataSub;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.CustomWordUpdate;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.WordReadData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.WordReadData.WordInfo;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;
import com.ifeng.iRecommend.zxc.bdhotword.HotWordDetector;
import com.ifeng.iRecommend.zxc.bdhotword.bean.BDHotWordBean;
import com.ifeng.myClassifier.GetSimcFeature;

public class MainTestClass {
	private static IKVOperationv2 itemop = new IKVOperationv2("appitemdb");
	private static IKVOperationv2 appbillop = new IKVOperationv2("appbilldb");
	// 通用数据层服务
	private static CommonDataSub cds;
	private static CustomWordUpdate cwu = CustomWordUpdate.getInstance();
	// 热词订阅服务
//	private static HotWordDetector hotwordOb = new HotWordDetector();
	static ArticleSourceData sourceData = ArticleSourceData.getInstance();
	static ConcurrentHashMap<String, String> articleSourceMap = sourceData.getArticleSourceMap();
	static WordReadData wordReadData = WordReadData.getInstance();

	MainTestClass() {
		cds = new CommonDataSub();
		Thread t = new Thread(cds);
		t.start();
	}

	/**
	 * 
	 * @param word
	 */
	public void testUsefulCheck(String word) {
		int usefulFlag = GetUsefulKeyFromRedis.GetUsefulFlag(word);
		if (usefulFlag == 1)
			System.out.println("The word " + word + " is useful.");
		if (usefulFlag == 99)
			System.out.println("Could not find word " + word + " in useful redis.");
		if (usefulFlag == 0)
			System.out.println("The word " + word + " is unuseful.");
	}

	/**
	 * 
	 * @param word
	 */
	public void testReadableCheck(String word) {
		ConcurrentHashMap<String, WordInfo> wordReadMap = wordReadData.getWordReadMap();
		if (wordReadMap.containsKey(word)) {
			System.out.println(word + " has been included in readable table.");
			WordInfo info = wordReadMap.get(word);
			int clientCount = info.getClientSubWordCount();
			int otherSourceCount = info.getOtherSouceCount();
			boolean isread = info.isRead();
			if (isread)
				System.out.println(word + " is readable. client count is " + clientCount + " other source count is " + otherSourceCount);
			else
				System.out.println(word + " is unreadable. client count is " + clientCount + " other source count is " + otherSourceCount);
		} else {
			System.out.println(word + "has not been included in readable table.");
		}
	}

	/**
	 * 
	 * @param s_title
	 * @param s_content
	 */
	public void testHotEvent(String s_title, String s_content) {
		Set<BDHotWordBean> baiduHotKey = hotwordOb.detectWord(s_title, s_content);
		// 事件识别，注意返回的注释说明
		BDHotWordBean rt = hotwordOb.detectEvent(s_title, s_content);
		if (rt != null) {
			System.out.println("Event is " + rt.getStr() + " type is " + rt.getType() + " score is " + rt.getScore() + " alias has " + rt.getSameEventNames());
		} else {
			System.out.println("Event is null.");
		}
		if (baiduHotKey != null && baiduHotKey.size() >= 1)
			for (BDHotWordBean b : baiduHotKey) {
				System.out.println("Hotword is " + b.getStr() + " type is " + b.getType() + " score is " + b.getScore());

			}
		else {
			System.out.println("Hotword is null.");
		}
	}

	/**
	 * 
	 * @param word
	 */
	public void testEtWord(String word) {
		EntityLibQuery.init();
		ArrayList<EntityInfo> ei = EntityLibQuery.getEntityList(word);
		if (null != ei) {
			System.out.println("This et string is "+ei.toString());
		} else {
			System.out.println("Has no this word： " + word);
		}
	}
	
	public void testSimc(String s_title, String source, ArrayList<String> featureList){
		ArrayList<String> cList = GetSimcFeature.ClassifyByWord(s_title, source, featureList);
		if(cList == null)
			System.out.println("Could not get c for this title."+ s_title);
		else
			System.out.println("Get c list for this title is "+ cList);
	}
	
	public void testTagParser(String other, String source, String doctype){
		CMPPDataOtherField f = new CMPPDataOtherField(other);
		ArrayList<String> tagList = CMPPDataOtherParser.OtherParser(f, source, doctype);
		if(tagList == null)
			System.out.println("Could not get tag result for this other."+ other);
		else
			System.out.println("Get c list for this title is "+ tagList);
	}
	
	public void testRelatedItem(ArrayList<String> featureList,String s_title, String source){
		RelatedItem obj = new RelatedItem();
		Map<String, List<String>> idMap = obj.getRelatedItemId(featureList, s_title, source);
		if(idMap == null || idMap.size() == 0)
		{
			System.out.println("Could not get related item result for this feature.");
		}
		else
		{
			for(Entry<String, List<String>> entry : idMap.entrySet())
			{
				System.out.println(entry.getKey()+" "+entry.getValue());
			}
		}
		}
	public void testRuleModify(){
		RuleModify ruleModify = RuleModify.getInstance();
		String s_title = "这个_h 美国_x 妹_n 纸_k 的_u 极品_n 身材_n 美_n 炸_v 了_u ！_w 舔屏_nr ing_nx";
		String f = 
				"美国, et, 1.0, 好身材, x, -0.5, Julia, nx, -0.5, 身材, n, -0.5, ing, nx, -0.5, 极品, n, -0.5, 舔屏, nr, -0.5, 翘臀, x, -0.1, 深蹲, x, -0.1, 箭步, n, -0.1, 紧身裤, x, -0.1, 妹子, x, -0.1, 女神, et, 0.1, 性感, x, -0.1, INS, nx, -0.1, kidding, nx, -0.1, Are, nx, -0.1, you, nx, -0.1, 汗水, n, -0.1, 水平, n, -0.1, 粉丝, ne, -0.1, 直线, n, -0.1, 腹沟, n, -0.1, 大长腿, x, -0.1, 豪乳, x, -0.1, 有氧运动, et, 0.1, 全套动作, nz, -0.1, me, nx, -0.1, 结果, k, -0.1, 决定, k, -0.1, 牛仔, x, -0.1, 伊丽莎白, et, 0.1, 长裙, x, -0.1, 美腿, x, -0.1, 加利福尼亚, et, 0.1, 长腿, x, -0.1, 肌肤, n, -0.1, 家常便饭, et, 0.1, 短裤, x, -0.1, 美女, et, 0.1, 宅男, x, -0.1, 还原, x, -0.1, 脂肪, x, -0.1, 慢跑, et, 0.1, 马甲, x, -0.1, 裤子, x, -0.1, 时尚, c, 1.0, 塑型, cn, 1.0, 美体, sc, 1.0, 美臀, x, -1.0, 身材, x, -1.0";
				ArrayList<String> feature = new ArrayList<String>();
		String[] fs = f.split(",");
		for(int i = 0; i < fs.length; i++)
		{
			feature.add(fs[i].trim());
		}
		String docType = "docpic";
		System.out.println(ruleModify.modifyResult(s_title, feature, docType, false));
	}
	public void testTimeSensitive(){
		 TimeSensitiveJudgement timeSensitiveJudgement = TimeSensitiveJudgement.getInstance();
		 itemf item = itemop.queryItemF("4930915", "c");
		 ArrayList<String>cateList = FeatureExTools.whatCategory(item.getFeatures());
		 TimeSensitiveInfo timeSensitiveInfo =
				 timeSensitiveJudgement.EstimateTimeSensitiveOfArticle(item.getSplitTitle(),item.getSplitContent(),cateList);

		 if(timeSensitiveInfo.isTimeSensitive()){
			 System.out.println("timeSensitive=true");
		 }
		 else
			 System.out.println("timeSensitive=false");
	}
	public void testStopwordFilter(itemf item){
		String stopwordPath = LoadConfig.lookUpValueByKey("StopWordPath");
		HashSet<String> stopword = new HashSet<String>();
		FileReader fr = null;
		try {
			fr = new FileReader(stopwordPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String s = null;
		try {
			while ((s = br.readLine()) != null) {
				stopword.add(s);
			}
			System.out.println("[STOPWORD]Read stopword " + stopword.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/****************** 停用词过滤 ***********************/
		ArrayList<String> tempFea = new ArrayList<String>();
		for (int i = 0; i < item.getFeatures().size() - 2; i += 3) {
			if (stopword.contains(item.getFeatures().get(i))) {
			} else {
				tempFea.add(item.getFeatures().get(i));
				tempFea.add(item.getFeatures().get(i + 1));
				tempFea.add(item.getFeatures().get(i + 2));
			}

		}
		item.setFeatures(tempFea);
		System.out.println("feature after stopword "+item.getFeatures());
	}
	
	public static void EventRedisTest(){
		IKVOperationv2 ikvop = new IKVOperationv2("appitemdb");
		Jedis eventJedis = new Jedis("10.32.24.194", 6379);
		eventJedis.select(4);
		String key = "来宾卡车侧翻_2016-05-25";
			System.out.println("[statistic] "+key);
			Set<String> idSet = null;
			idSet = eventJedis.smembers(key);
			System.out.println(idSet.toString());
	}
	public static void simRateTest(){
		String str1 = "王宝强离婚";
		String str2 = "王宝强宣布离婚";
		System.out.println(commenFuncs.simRate(str1,str2));
	}
	public static void main(String[] args){
		MainTestClass test = new MainTestClass();
		test.testReadableCheck("新闻客户端");
//		test.testEtWord("这般");
//		test.testRuleModify();
//		test.testTimeSensitive();
		test.testUsefulCheck("新闻客户端");
//		test.EventRedisTest();
//		String key = "3959755";
//		String type = "c";
//		itemf item = test.itemop.queryItemF(key, type);
//		System.out.println(item.getFeatures());
//		test.testStopwordFilter(item);
//		test.simRateTest();
	}
}
