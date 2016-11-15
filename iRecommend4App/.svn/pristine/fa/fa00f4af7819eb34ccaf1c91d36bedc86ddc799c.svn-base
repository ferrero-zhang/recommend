package com.ifeng.iRecommend.usermodel;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation;
import com.ifeng.iRecommend.train.usermodelTraining;
import com.ifeng.iRecommend.usermodel.usermodelInterface;

public class usermodelInterfaceTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		fieldDicts.modelPath = "D:/workspace/iRecommend4App/testenv/usermodel/";
		fieldDicts.appTreeMappingFile = "D:/workspace/iRecommend4App/testenv/AppTreeMapping.txt";
		fieldDicts.frontAppTreeMappingFile = "D:/workspace/iRecommend4App/testenv/Front_TreeMapping.txt";
		fieldDicts.stopwordsFile = "D:/workspace/iRecommend4App/testenv/stopwords.txt";
		fieldDicts.tm_doc_dir = "D:\\workspace\\iRecommend4App\\testenv\\tm\\doc\\";
		fieldDicts.tm_word_dir = "D:\\workspace\\iRecommend4App\\testenv\\tm\\word\\";
		fieldDicts.tm_words_file = "D:\\workspace\\iRecommend4App\\testenv\\tm\\dict_topicmodel";

		//限定在hbase中是newsapp的log和item table
		logDBOperation.setLogType(logDBOperation.LogType.APPLOG);
	}
	
	@Test
	public void testcmpOneUserDoc() {
		long time1 = System.currentTimeMillis();
		//杨明：352621061112840
		String userID = "A000004206BD05";//"860511021479048";//"29e2077505e349d4b96a298883834d1e";//****
		// 查询得到此user的历史行为
		HashMap<String, String> hm_log = logDBOperation
				.queryUserIDInDateRange(userID,
						String.valueOf(System.currentTimeMillis()), 90);
		
		System.out.println(hm_log.entrySet().toString());
		
		// 清理取出的行为列表，10分钟内的分页合并并去除其它额外信息；
		HashMap<String, String> hm_dayitems = usermodelTraining.parseNewsAppUserDayLog(hm_log);
		System.out.println(hm_dayitems.entrySet().toString());
		System.out.println(hm_dayitems.entrySet().size());
		
		
		usermodelInterface ui = usermodelInterface.getInstance("modeling");
		ItemOperation itemOP = ItemOperation.getInstance();
		itemOP.setItemType(ItemType.APPITEM);
		
		// 调用模型层建模函数建模
		long b = System.currentTimeMillis();
		userDocForSolr ud = ui.cmpOneUserDoc(userID,hm_dayitems,itemOP);
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
	public void testqueryUserVectors() {
		String userID = "0020BB8D7C3BF3";
		ArrayList<String> vectors = null;
		try {
			vectors = usermodelInterface.getInstance("query").queryUserVectors(userID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(3,vectors.size());
		System.out.println(vectors.get(0));
		System.out.println(vectors.get(1));
		System.out.println(vectors.get(2));
		
		
		userID = "001e13c4-6d8e-4405-a78e-0f08aa79ee92";
		try {
			vectors = usermodelInterface.getInstance("query").queryUserVectors(userID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(3,vectors.size());
		System.out.println(vectors.get(0));
		System.out.println(vectors.get(1));
		System.out.println(vectors.get(2));
		
		userID = "00.0c.b4.00.24.88";
		try {
			vectors = usermodelInterface.getInstance("query").queryUserVectors(userID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(null,vectors);
	}
	
	

}
