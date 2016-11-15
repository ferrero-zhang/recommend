package com.ifeng.iRecommend.featureEngineering;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;

public class RelatedItemTest {

	RelatedItem testObj = null;
	String tablename = "appitemdb";
	IKVOperationv2 ikvop = null;
	@Before
	public void setUp() throws Exception {
		testObj = new RelatedItem();
		ikvop = new IKVOperationv2(tablename);
	}

	@Ignore
	public void testSimilarity() {
		String str1 = "李小璐_x 紧_k 捂_k 胸_n 防_k 走光_nz  _w 老公_n 贾乃亮_x 全程_n 紧随_v";
		String str2 = "李小璐_x 紧_k 捂_k 胸_n 防_k 走光_nz  _w 柳岩_x 摆_k pose_nx 借位_v 亲吻_x 性感_x 抢镜_nz";
		boolean sim = RelatedItem.isSimilarity(str1, str2);
		System.out.println(sim);
	}
	
	@Test
	public void testGetRelatedItemId() {
		RelatedItem obj = new RelatedItem();
//		 queryInterface query = queryInterface.getInstance();
//		 itemf item = query.queryItemF("1047359");
		
//		 Map<String, List<String>> idList =	 obj.getRelatedItemId(item.getFeatures(),
////		 item.getSplitTitle());
		String[] feature = {"义勇军进行曲","kb","0.1"};
		String title = "国际足联_x 再_d 就_d 香港_x 球迷_x 嘘_k 国歌_x 事件_n 展开_v 调查_v";
		String source = "凤凰体育";
//		Map<String, List<String>> idList = obj.getRelatedItemId(null, title, source);
//		Map<String, List<String>> idList = obj.getRelatedItemId(new ArrayList<String>(Arrays.asList(feature)), null, source);
		Map<String, List<String>> idList = obj.getRelatedItemId(new ArrayList<String>(Arrays.asList(feature)), title, null);
//
		System.out.println(idList);
	}
	
	@Test
	public void testHandleFeatures() throws Exception {
		RelatedItem obj = new RelatedItem();
		 IKVOperationv2 ob = new IKVOperationv2(tablename);
		 itemf item = ob.queryItemF("1047359","c");
		
		Method testMethod = testObj.getClass().getDeclaredMethod("handleFeatures", ArrayList.class);
		testMethod.setAccessible(true);
		
		Object result = testMethod.invoke(obj, item.getFeatures());
		 assertNull(result);
		 ob.close();
	}
	
	@Test
	public void testMakeQuery() throws Exception{
		RelatedItem obj = new RelatedItem();
		 IKVOperationv2 ob = new IKVOperationv2(tablename);
		 itemf item = ob.queryItemF("1047359","c");
		 
		Method testMethod = testObj.getClass().getDeclaredMethod("makeQuery", String.class);
		testMethod.setAccessible(true);
		
		String[] feature = {"义勇军进行曲","kb","0.1","球迷","et","1.0","香港","et","1.0","世界杯","sc","1.0"};
		Method testMethod1 = testObj.getClass().getDeclaredMethod("handleFeatures", ArrayList.class);
		testMethod1.setAccessible(true);
		
		Object result1 = testMethod1.invoke(obj, new ArrayList<String>(Arrays.asList(feature)));
		Object result = testMethod.invoke(obj, "铜锣烧");
		assertNull(result1);
		assertNotNull(result);
		
		 ob.close();
	}
	
	/*@Ignore
	public void testFunction(){
		queryInterface query = queryInterface.getInstance();
		itemf item = null;
		Random r1 = new Random(10);
		FileWriter resultWriter = null;
		try {
			resultWriter = new FileWriter(
					"D:\\classify\\new\\相关推荐\\relatedTestResult", false);
			for (int i = 0; i < 101; i++) {
				int t = r1.nextInt(5000) + 1500000;
				item = query.queryItemF(String.valueOf(t));
				while (item == null) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					t ++;
					item = query.queryItemF(String.valueOf(t));
				}
				System.out.println("id=" + t);
				RelatedItem obj = new RelatedItem();
				ArrayList<String> idList = (ArrayList<String>) obj.getRelatedItemId(
						item.getFeatures(), item.getSplitTitle(), item.getSource()).get("客户端");
				StringBuffer tableTitle = new StringBuffer(item.getID());
				tableTitle.append("\t").append(item.getTitle()).append("\n");
				tableTitle.append("-------------------------\n");
				resultWriter.write(tableTitle.toString());
				if (idList == null || idList.isEmpty())
					resultWriter.write("NULL\n");
				else {
					resultWriter.write(idList.get(0) + "\n");
					for (int j = 1; j < idList.size(); j += 2)
						resultWriter.write(idList.get(j) + "\t"
								+ idList.get(j + 1) + "\n");
				}
				resultWriter.write("=========================\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				resultWriter.flush();
				resultWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}*/
}
