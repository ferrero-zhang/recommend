package com.ifeng.iRecommend.lidm.userLog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.iRecommend.lidm.userLog.logDBOperation.LogType;

public class LogDBOperationTest {

	@BeforeClass
	/**
	 * WARN:需要首先初始化log数据的类型 
	 * 	PCLOG:pc端日志类型，读取数据时会选取hbase数据中的pc端日志数据表
	 *	APPLOG:客户端日志类型，读取数据时会选取hbase数据中的客户端日志数据表
	 *	UNDEFINED:未定义类型，读取数据时会默认选择pc端日志数据表
	 *	默认类型为UNDEFINED
	 *	调用setLogType(LogType logType) 或setTableName(fieldDicts.pclogname...)进行初始化，否则将使用默认数据表
	 */
	public static void setUpBeforeClass() throws Exception {
		System.out.println("WARN:需要首先初始化log数据的类型:");
		System.out.println("	PCLOG:pc端日志类型，读取数据时会选取hbase数据中的pc端日志数据表");
		System.out.println("	APPLOG:客户端日志类型，读取数据时会选取hbase数据中的客户端日志数据表");
		System.out.println("	UNDEFINED:未定义类型，读取数据时会默认选择pc端日志数据表");
		System.out.println("	默认类型为UNDEFINED");
		System.out
				.println("	调用setLogType(LogType logType) 或setTableName(fieldDicts.pclogname...)进行初始化，否则将使用默认数据表");

		logDBOperation.setLogType(LogType.APPLOGREALTIME);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	/**测试:queryUserIDInDateRange(usrID,timeStamp, 20)
	 * 查询指定的用户id的指定时间间隔内的所有用户行为
	 * 
	 * WARN:需要首先初始化log数据的类型
	 * 
	 * @param usrID
	 *            用户id
	 * @param timeStamp
	 *            截止日期的时间戳信息，为13位数字
	 * @param interval
	 *            时间间隔，默认单位为一天
	 * @return hashmap对象，key为day,value为day对应的用户行为
	 * 
	 * 如queryUserIDInDateRange(usrID,timeStamp, 20)，即查询20天内此id的所有信息，截止日期的时间戳为timeStamp
	 * 
	 */
	public void testQueryUserIDInDateRange() {
		String usrID = "862901022338830";
		String timeStamp = String.valueOf(System.currentTimeMillis());//"1384312439152";
		HashMap<String, String> id_hashmap = logDBOperation
				.queryUserIDInDateRange(usrID, timeStamp, 20);

		System.out.println(id_hashmap.size());
		Iterator<Entry<String, String>> iterator_id = id_hashmap.entrySet()
				.iterator();
		while (iterator_id.hasNext()) {
			Map.Entry<String, String> entry = iterator_id.next();
			String day = entry.getKey();
			String value = entry.getValue();
			System.out.println(day);
			System.out.println(value);
			

		}
	}
	@Test
	public void testOfDeleteByUsrID()
	{
		String usrID = "862901022338830";
		logDBOperation.deleteByUsrID(usrID);
	}
	@Ignore
	/**测试 queryByUsrIDList(id_list):批量查询函数
	 * 
	 * WARN:需要首先初始化log数据的类型 
	 * 
	 * 一次性查询userid_list中的所有id，效率高
	 * 单个id查询函数queryByUsrID(id)的效率较低
	 */
	public void testQueryByUsrIDList() {
		LinkedList<String> userid_list = new LinkedList<String>();
		userid_list.add("1356484997031_e46umy6006");

		// 继续添加id
		// userid_list.add("1356484997031_e46umy6006");
		// userid_list.add("1356484997031_e46umy6006");
		// userid_list.add("1356484997031_e46umy6006");
		// userid_list.add("1356484997031_e46umy6006");

		HashMap<String, HashMap<String, String>> result_hashmap = logDBOperation
				.queryByUsrIDList(userid_list);

		Iterator<Entry<String, HashMap<String, String>>> iterator_ids = result_hashmap
				.entrySet().iterator();

		while (iterator_ids.hasNext()) {
			Map.Entry<String, HashMap<String, String>> entry_ids = iterator_ids
					.next();

			String id = entry_ids.getKey();
			HashMap<String, String> id_hashmap = entry_ids.getValue();

			System.out.println(id);
			Iterator<Entry<String, String>> iterator_id = id_hashmap.entrySet()
					.iterator();

			int count_urls = 0;
			while (iterator_id.hasNext()) {
				Map.Entry<String, String> entry = iterator_id.next();
				//String day = entry.getKey();
				String value = entry.getValue();
				String[] urls_day = value.split("!");
				int count_urls_day = urls_day.length;
				count_urls += count_urls_day;
				// System.out.println(day + "-" + count_urls_day);
				// System.out.println(value);
			}
			System.out.println("days:" + id_hashmap.size());
			System.out.println("avag urls per day:" + count_urls
					/ id_hashmap.size());
		}
	}
	@Ignore
	public void testOfqueryActiveUserID()
	{
		logDBOperation.setLogType(LogType.APPLOG);
		logDBOperation.queryActiveUserID(45);
	}
@Ignore
public void testOfqueryByUsrID()
{
	logDBOperation.setLogType(LogType.APPLOG);
	HashMap<String, String> hashmap = logDBOperation.queryByUsrID("860311022079552");
	for(Entry<String,String> entry : hashmap.entrySet())
	{
		System.out.println(entry.getKey());
		System.out.println(entry.getValue());
	}
}
}
