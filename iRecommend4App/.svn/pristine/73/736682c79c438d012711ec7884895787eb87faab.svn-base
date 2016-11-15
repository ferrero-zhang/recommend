package com.ifeng.iRecommend.usermodel;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation;
import com.ifeng.iRecommend.train.usermodelTraining;

public class usermodelInterfaceSolrTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		fieldDicts.modelPath = "D:/workspace/iRecommend4App/testenv/usermodel/";
		fieldDicts.appTreeMappingFile = "D:/workspace/iRecommend4App/testenv/AppTreeMapping.txt";
		fieldDicts.frontAppTreeMappingFile = "D:/workspace/iRecommend4App/testenv/APPFront_TreeMapping.txt";
		fieldDicts.stopwordsFile = "D:/workspace/iRecommend4App/testenv/stopwords.txt";
		fieldDicts.tm_doc_dir = "D:\\workspace\\iRecommend4App\\testenv\\tm\\doc\\";
		fieldDicts.tm_word_dir = "D:\\workspace\\iRecommend4App\\testenv\\tm\\word\\";
		fieldDicts.tm_words_file = "D:\\workspace\\iRecommend4App\\testenv\\tm\\dict_topicmodel";

		//限定在hbase中是newsapp的log和item table
		logDBOperation.setLogType(logDBOperation.LogType.APPLOG);
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");  
	}

	@Test
	public void testCmpOneUserDoc() {
		long time1 = System.currentTimeMillis();
		//杨明：352621061112840
		String userID = "865524010376534";//"860511021479048";//"29e2077505e349d4b96a298883834d1e";//****
		//限定在hbase中是newsapp的log和item table
		logDBOperation.setLogType(logDBOperation.LogType.APPLOGREALTIME);
		// 查询得到此user的历史行为
		HashMap<String, String> hm_log = logDBOperation.queryUserIDInDateRange(
				userID, String.valueOf(System.currentTimeMillis()),9);

		System.out.println(hm_log.entrySet().toString());
		System.out.println(hm_log.entrySet().size());
	
		
		// 清理取出的行为列表，10分钟内的分页合并并去除其它额外信息；
		HashMap<String, String> hm_dayitems = usermodelTraining.parseNewsAppUserDayLog(hm_log);
		System.out.println(hm_dayitems.entrySet().toString());
		System.out.println(hm_dayitems.entrySet().size());
		
		
		usermodelInterfaceSolr uiSolr = new usermodelInterfaceSolr();
		ItemOperation itemOP = ItemOperation.getInstance();
		itemOP.setItemType(ItemType.APPITEM);
		
		// 调用模型层建模函数建模
		long b = System.currentTimeMillis();
		userDocForSolr ud = uiSolr.cmpOneUserDoc(userID,hm_dayitems,itemOP);
		long c = System.currentTimeMillis();
		System.out.println("all time:"+(c-b));
		//ud.turnVectorToDocs();
		//输出，观察之
		System.out.println(ud.cmpTopic1());
		//输出，观察之
		System.out.println(ud.cmpTopic2());
		//输出，观察之
		System.out.println(ud.cmpTopic3());
		long time2 = System.currentTimeMillis();
		System.out.println(time2-time1);
//		// 载入测试集合，构建虚拟hashmap接口
//		HashMap<String, String> hm_logs = new HashMap<String, String>();
//		FileUtil fu = new FileUtil();
//		if (fu.Initialize(
//				"D:\\workspace\\iRecommend\\testenv\\test4logparse.txt",
//				"UTF-8")) {
//			try {
//				String rawUserData = null,userid = null;
//				while ((rawUserData = fu.ReadLine()) != null) {
//					if (rawUserData.isEmpty())
//						continue;
//					if(userid == null){
//						userid = rawUserData.trim();
//						System.out.println(userid);
//						continue;
//					}
//					if(rawUserData.indexOf("------") >= 0){
//						//调用parse函数
//						HashMap<String, String> hm_dayitems = usermodelTraining.parseUserDayLog(hm_logs);
////						//验证返回记录数目，验证某天的item个数，验证3分钟内合并是否OK
////						assertEquals(5,hm_dayitems.size());
//						//output
//						Iterator<Entry<String, String>> it = hm_dayitems.entrySet().iterator();
//						while(it.hasNext()){
//							Entry<String, String> et = it.next();
//							//System.out.println(et.getKey()+"\t"+et.getValue());
//						}
//						
//						usermodelInterface.getInstance().modelOneUser(userid, hm_dayitems);
//						
//						//@test
//						System.out.println("finish:"+userid);
//						
//						hm_logs.clear();
//						
//						userid = null;
//						continue;
//					}
//					int b = rawUserData.indexOf("\t");
//					String day = rawUserData.substring(0, b);
//					String logs = rawUserData.substring(b + 1);
//					hm_logs.put(day, logs);
//
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				fu.CloseRead();
//			}
//		}
//		
//		usermodelInterface.getInstance().commitModel();
	}

	@Ignore
	public void testCleanBadUsers() {
//		String userid = "862966024212929";
//		// 查询user topics
//		String userString = usermodelInterfaceSolr
//				.queryUserStringFromSolr(userid);
//		
//		//System.out.println(userString);
//		// 历史上为空，是不是考虑进行全量训练？还是直接写入最近？
//		// 其它情况下，解析历史数据，并追加入新数据，然后覆盖写即可
//		String topic1 = usermodelInterfaceSolr.getTopicVector(userString,
//				"topic1");
//		
//		String topic2 = usermodelInterfaceSolr.getTopicVector(userString,
//				"topic2");
//		String topic3 = usermodelInterfaceSolr.getTopicVector(userString,
//				"topic3");
//		
//		System.out.println(topic1);
//		String res = usermodelInterfaceSolr.cleanBadUsers(userid, topic1,
//				topic3);
//		if (res.equals("baduser")) {
//			System.out.println("baduser");
//		} else if (res.equals("good")) {
//			System.out.println("good");
//		} else {
//			// 清洗topic1
//			topic1 = res;
//			System.out.println("new topic1:");
//			System.out.println(topic1);
//		}
	}
	
	
	@Test
	public void testModelSomeUsers() {
		String userID = "A000004206BD05";//"864147021769892";// ****
		//限定在hbase中是newsapp的log和item table
		logDBOperation.setLogType(logDBOperation.LogType.APPLOG);
		// 查询得到此user的历史行为
		HashMap<String, String> hm_log = logDBOperation.queryUserIDInDateRange(
				userID, String.valueOf(System.currentTimeMillis()), 90);

		System.out.println(hm_log.entrySet().toString());
		System.out.println(hm_log.entrySet().size());
	
		// 清理取出的行为列表，10分钟内的分页合并并去除其它额外信息；
		HashMap<String, String> hm_dayitems = usermodelTraining
				.parseNewsAppUserDayLog(hm_log);
		System.out.println(hm_dayitems.entrySet().toString());

		HashMap<String, HashMap<String, String>> hm_user2items = new HashMap<String, HashMap<String, String>>();
		hm_user2items.put(userID, hm_dayitems);


		usermodelInterfaceSolr ui = new usermodelInterfaceSolr();
		ItemOperation itemOP = ItemOperation.getInstance();
		itemOP.setItemType(ItemType.APPITEM);
		// 调用模型层建模函数建模
		String s_docs = ui.modelSomeUsers(hm_user2items, itemOP,"all", "android");
		// 输出，观察之
		System.out.println(s_docs);

		// // 载入测试集合，构建虚拟hashmap接口
		// HashMap<String, String> hm_logs = new HashMap<String, String>();
		// FileUtil fu = new FileUtil();
		// if (fu.Initialize(
		// "D:\\workspace\\iRecommend\\testenv\\test4logparse.txt",
		// "UTF-8")) {
		// try {
		// String rawUserData = null,userid = null;
		// while ((rawUserData = fu.ReadLine()) != null) {
		// if (rawUserData.isEmpty())
		// continue;
		// if(userid == null){
		// userid = rawUserData.trim();
		// System.out.println(userid);
		// continue;
		// }
		// if(rawUserData.indexOf("------") >= 0){
		// //调用parse函数
		// HashMap<String, String> hm_dayitems =
		// usermodelTraining.parseUserDayLog(hm_logs);
		// // //验证返回记录数目，验证某天的item个数，验证3分钟内合并是否OK
		// // assertEquals(5,hm_dayitems.size());
		// //output
		// Iterator<Entry<String, String>> it =
		// hm_dayitems.entrySet().iterator();
		// while(it.hasNext()){
		// Entry<String, String> et = it.next();
		// //System.out.println(et.getKey()+"\t"+et.getValue());
		// }
		//
		// usermodelInterface.getInstance().modelOneUser(userid, hm_dayitems);
		//
		// //@test
		// System.out.println("finish:"+userid);
		//
		// hm_logs.clear();
		//
		// userid = null;
		// continue;
		// }
		// int b = rawUserData.indexOf("\t");
		// String day = rawUserData.substring(0, b);
		// String logs = rawUserData.substring(b + 1);
		// hm_logs.put(day, logs);
		//
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// } finally {
		// fu.CloseRead();
		// }
		// }
		//
		// usermodelInterface.getInstance().commitModel();
	}

	@Ignore
	public void testQueryUserVectors() {
//		String userID = "863151026712833";
//		usermodelInterfaceSolr ui = new usermodelInterfaceSolr();
//		String userDoc = ui.queryUserStringFromSolr(userID);
//		System.out.println(userDoc);
	}

	@Ignore
	public void testQueryProfile(){
//		String userID = "863151026712833";
//		usermodelInterfaceSolr ui = new usermodelInterfaceSolr();
//		userDocForSolr oneUserDoc = new userDocForSolr();
//		oneUserDoc.userid = userID;
//		oneUserDoc.setProfile("");
//		ui.queryProfile(oneUserDoc);
//		System.out.println(oneUserDoc.getProfile());
//		assertEquals(oneUserDoc.getProfile(),"北京市_北京市_朝阳区");
//		
//		oneUserDoc.userid = "867064015345216";
//		oneUserDoc.setProfile("");
//		ui.queryProfile(oneUserDoc);
//		System.out.println(oneUserDoc.getProfile());
//		assertEquals(oneUserDoc.getProfile(),"北京市_北京市_朝阳区");
//		
//		oneUserDoc.userid = "358239053823680";
//		oneUserDoc.setProfile("");
//		ui.queryProfile(oneUserDoc);
//		System.out.println(oneUserDoc.getProfile());
//		assertEquals(oneUserDoc.getProfile(),"北京市_北京市_朝阳区");
//		
//		
//		oneUserDoc.userid = "860219023280857";
//		oneUserDoc.setProfile("");
//		ui.queryProfile(oneUserDoc);
//		System.out.println(oneUserDoc.getProfile());
//		assertEquals(oneUserDoc.getProfile(),"河南省_驻马店市_西平县");
//		oneUserDoc.userid = "A00000384644B9";
//		oneUserDoc.setProfile("");
//		ui.queryProfile(oneUserDoc);
//		System.out.println(oneUserDoc.getProfile());
//		assertEquals(oneUserDoc.getProfile(),"");
//		oneUserDoc.userid = "A0000037E475E5";
//		oneUserDoc.setProfile("");
//		ui.queryProfile(oneUserDoc);
//		System.out.println(oneUserDoc.getProfile());
//		assertEquals(oneUserDoc.getProfile(),"");
//		oneUserDoc.userid = "864322014109170";
//		oneUserDoc.setProfile("");
//		ui.queryProfile(oneUserDoc);
//		System.out.println(oneUserDoc.getProfile());
//		assertEquals(oneUserDoc.getProfile(),"江西省_宜春市_袁州区");
//		oneUserDoc.userid = "356205058478434";
//		oneUserDoc.setProfile("");
//		ui.queryProfile(oneUserDoc);
//		System.out.println(oneUserDoc.getProfile());
//		assertEquals(oneUserDoc.getProfile(),"四川省_内江市_市中区");
	}
	
}
