package com.ifeng.iRecommend.lidm.userLog;

/**
 * <PRE>
 * 作用 : 
 *   提供在hbase中更新数据、批量查询数据、删除数据等相关的操作。
 *   
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
 *          1.0          2013-07-16        lidm          change
 * -----------------------------------------------------------------------------
 * </PRE>
 */

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.FilterList.Operator;

import com.ifeng.commen.lidm.hbase.HbaseInterface;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

public class logDBOperation
{
	protected static final Log LOG = LogFactory.getLog("manage_log_hbase");
	private static String tableName = fieldDicts.pcUserLogTableNameInHbase;

	/*
	 * WARN:需要首先初始化log数据的类型 PCLOG:pc端日志类型，读取数据时会选取hbase数据中的pc端日志数据表
	 * APPLOG:客户端日志类型，读取数据时会选取hbase数据中的客户端日志数据表
	 * UNDEFINED:未定义类型，读取数据时会默认选择pc端日志数据表 默认类型为UNDEFINED 调用setLogType(LogType
	 * logType) 或setTableName(fieldDicts.pclogname...)进行初始化，否则将使用默认数据表
	 */
	// log文件的类型
	public enum LogType {
		PCLOG, APPLOG, APPLOGREALTIME, UNDEFINED
	};

	// log文件的默认类型为UNDEFINED
	private static LogType logType = LogType.UNDEFINED;

	// 设置log文件的类型，并根据log文件类型自动设置合适的数据表，以利用接口读取数据表中的数据。
	// 在调用接口前设置，只设置一次即可
	public static void setLogType(LogType _logType)
	{
		logType = _logType;
		setTableName(_logType);
	}
	//返回当前日志类型
	public static LogType getLogType()
	{
		return logType;
	}
	// 手动设置数据表的名称，会自动更新log文件类型
	public static void setTableName(LogType logType)
	{
		switch (logType)
		{
		case PCLOG:
			setTableName(fieldDicts.pcUserLogTableNameInHbase);
			break;
		case APPLOG:
			setTableName(fieldDicts.appUserLogTableNameInHbase);
			break;
		case APPLOGREALTIME:
			setTableName(fieldDicts.appRealtimeUserLogTableNameInHbase);
			break;
		default:
			break;
		}
		;
	}
	// 返回当前tableName名称
	public static String getTableName()
	{
		return tableName;
	}
	// 手动设置数据表的名称，会自动更新log文件类型
	public static void setTableName(String _tableName)
	{
		tableName = _tableName;
		if (tableName.equals(fieldDicts.pcUserLogTableNameInHbase))
			logType = LogType.PCLOG;
		else if (tableName.equals(fieldDicts.appUserLogTableNameInHbase))
			logType = LogType.APPLOG;
		else if (tableName.equals(fieldDicts.appRealtimeUserLogTableNameInHbase))
			logType = LogType.APPLOGREALTIME;
		else
			logType = LogType.UNDEFINED;
	}

	/**
	 * 检测log文件类型是否已设置，用于提示。
	 */
	public static void checkLogTypeInit()
	{
		if (logType == LogType.UNDEFINED)
		{
			System.out.println("WARN:需要首先初始化log数据的类型:");
			System.out.println("	PCLOG:pc端日志类型，读取数据时会选取hbase数据中的pc端日志数据表");
			System.out.println("	APPLOG:客户端日志类型，读取数据时会选取hbase数据中的客户端日志数据表");
			System.out.println("	UNDEFINED:未定义类型，读取数据时会默认选择pc端日志数据表");
			System.out.println("	默认类型为UNDEFINED");
			System.out.println("	调用setLogType(LogType logType) 或setTableName(fieldDicts.pclogname...)进行初始化，否则将使用默认数据表");

			LOG.warn("WARN:需要首先初始化log数据的类型:");
			LOG.warn("	PCLOG:pc端日志类型，读取数据时会选取hbase数据中的pc端日志数据表");
			LOG.warn("	APPLOG:客户端日志类型，读取数据时会选取hbase数据中的客户端日志数据表");
			LOG.warn("	UNDEFINED:未定义类型，读取数据时会默认选择pc端日志数据表");
			LOG.warn("	默认类型为UNDEFINED");
			LOG.warn("	调用setLogType(LogType logType) 或setTableName(fieldDicts.pclogname...)进行初始化，否则将使用默认数据表");

			LOG.warn("WARNNING:LogType is null,using default tableName: fieldDicts.pcLogTableNameInHbase,should call setLogType(LogType) or setTableName(tableName) first to init logType and tableName!");
			System.out
					.println("WARNNING:LogType is null,using default tableName: fieldDicts.pcLogTableNameInHbase,should call setLogType(LogType) or setTableName(tableName) first to init logType and tableName!");
		}
	}

	/**
	 * 根据 usrID删除一条记录
	 * 
	 * @param usrID  用户ID
	 * 
	 * @return true 删除成功  false 删除失败
	 */
	public static boolean deleteByUsrID(String usrID)
	{
		boolean deleteFlag = false;
		try
		{
			HTable table = HbaseInterface.getHTable(tableName);
			List list = new ArrayList();
			Delete d1 = new Delete(usrID.getBytes());
			list.add(d1);
			table.delete(list);
			deleteFlag = true;
			LOG.info("Delete data of id " + usrID + " succeed.");
		}
		catch (IOException e)
		{
			deleteFlag = false;
			LOG.error("Delete data of id " + usrID + " failed. \n",e);
		}
		return deleteFlag;
	}

	/**
	 * WARN:需要初始化log数据的类型，否则将查询默认的pc端日志数据表
	 * 
	 * 根据usrID(rowKey)查找整行数据，返回结果保存在hashmap中 其中key为日期，value为此日期下的用户行为。
	 * 效率不如批量查询函数queryByUsrIDList(id_list)
	 * 
	 * @param usrID		用户ID
	 *
	 * @return 	HashMap
	 */
	public static HashMap<String, String> queryByUsrID(String usrID)
	{
		LOG.info("start query user id:" + usrID);

		checkLogTypeInit();

		// String tableName = fieldDicts.pcLogTableNameInHbase;
		HTable table = null;
		HashMap<String, String> date_value_HashMap = new HashMap<String, String>();
		String date_time = null;
		String date = null;
		StringBuffer value_buf = new StringBuffer();
		try
		{
			table = HbaseInterface.getHTable(tableName);
			if (table == null)
			{
				LOG.error("can not get table:" + tableName);
				return null;
			}
			Get get = new Get(usrID.getBytes());
			Result r = table.get(get);
			if (r.isEmpty())
			{
				LOG.error("cannot find usr id:" + usrID);
			}
			else
			{
				for (KeyValue keyValue : r.raw())
				{
					date_time = new String(keyValue.getQualifier());
					date = date_time.substring(0, 8);
					value_buf.append(new String(keyValue.getValue()));
					if (date_value_HashMap.containsKey(date))
						value_buf.append("!" + date_value_HashMap.get(date));
					date_value_HashMap.put(date, value_buf.toString());

					value_buf.delete(0, value_buf.length());
				}
			}
			table.close();
		}
		catch (IOException ex)
		{
			LOG.error("error", ex);
		}
		LOG.info("end query user id:" + usrID);
		return date_value_HashMap;
	}

	/**
	 * 批量查询函数
	 * 
	 * WARN:需要初始化log数据的类型，否则将查询默认的pc端日志数据表
	 * 
	 * 一次性查询userid_list中的所有id，效率高 单个id查询函数queryByUsrID(id)的效率较低
	 */
	public static HashMap<String, HashMap<String, String>> queryByUsrIDList(List<String> usrID_list)
	{
		LOG.info("start query user id list");
		checkLogTypeInit();
		long start = System.currentTimeMillis();
		HTable table = null;
		HashMap<String, HashMap<String, String>> result_hashmap = new HashMap<String, HashMap<String, String>>();

		String date_time = null;
		String date = null;
		StringBuffer value_buf = new StringBuffer();
		List<Get> get_list = new LinkedList<Get>();
		for (String userid : usrID_list)
		{
			Get get = new Get(userid.getBytes());
			get_list.add(get);
		}

		try
		{
			table = HbaseInterface.getHTable(tableName);
			if (table == null)
			{
				LOG.error("can not get table:" + tableName);
				return null;
			}
			Result[] results = table.get(get_list);
			for (Result result : results)
			{
				HashMap<String, String> date_value_HashMap = new HashMap<String, String>();
				String key = new String(result.getRow());
				if (result.isEmpty())
				{
					continue;
				}
				else
				{
					for (KeyValue keyValue : result.raw())
					{
						date_time = new String(keyValue.getQualifier());
						date = date_time.substring(0, 8);
						value_buf.append(new String(keyValue.getValue()));
						if (date_value_HashMap.containsKey(date))
							value_buf.append("!" + date_value_HashMap.get(date));
						date_value_HashMap.put(date, value_buf.toString());
						value_buf.delete(0, value_buf.length());
					}
				}
				if (date_value_HashMap != null && date_value_HashMap.size() > 0)
				{
					result_hashmap.put(key, date_value_HashMap);
				}
			}
			table.close();
		}
		catch (IOException ex)
		{
			LOG.error("error", ex);
		}
		LOG.info("end query user id,duration:" + (System.currentTimeMillis() - start));
		return result_hashmap;
	}

	/**
	 * WARN:需要初始化log数据的类型，否则将查询默认的pc端日志数据表
	 * 
	 * 查询所有的用户id，返回结果保存在文件中
	 * 
	 * @param resultFilePath 输出的文件路径
	 * 
	 * @param maxCount 需要查询的id的数目最多为maxCount,若maxCount小于0，表示查询所有user id
	 */
	public static void queryAllUserID(String resultFilePath, long maxCount)
	{
		LOG.info("start query all user id,max count is:" + maxCount);
		checkLogTypeInit();
		long start = System.currentTimeMillis();
		HTable table = null;
		FileWriter fileWriter = null;
		try
		{
			fileWriter = new FileWriter(resultFilePath, true);
		}
		catch (IOException e1)
		{ // TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try
		{
			table = HbaseInterface.getHTable(tableName);

			if (table == null)
			{
				LOG.error("can not get table:" + tableName);
				return;
			}
			Filter filter = new FirstKeyOnlyFilter();
			FilterList filterList = new FilterList(Operator.MUST_PASS_ONE, Arrays.asList(filter));
			Scan scan = new Scan();
			scan.setCaching(1000);
			scan.setMaxVersions();
			scan.setBatch(1000);
			scan.setFilter(filterList);
			ResultScanner rs = table.getScanner(scan);
			int count = 0;
			for (Result r : rs)
			{
				if (maxCount >= 0 && count >= maxCount)
					break;
				String key = new String(r.getRow());
				count++;
				fileWriter.write(key+"\n");
				LOG.info("[" + count + "]" + key);
			}
			LOG.info("max size:" + count);
			rs.close();
			table.close();
		}
		catch (Exception ex)
		{
			LOG.error("error", ex);
		}

		try
		{
			fileWriter.close();
		}
		catch (IOException e)
		{ // TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		long end = System.currentTimeMillis();
		LOG.info("query duration:" + (end - start));
		LOG.info("end query all user id,max count is:" + maxCount);
	}

	/**
	 * 查询活跃天数为 activeDayCount，平均每天浏览url数目大于2小于50的用户id
	 * 
	 * WARN:需要初始化log数据的类型，否则将查询默认的pc端日志数据表
	 * 
	 * @param activeDayCount
	 *            活跃天数
	 */
	public static void queryActiveUserID(int activeDayCount)
	{
		checkLogTypeInit();
		HTable table = HbaseInterface.getHTable(tableName);
		if (table == null)
			return;
		Scan scan = new Scan();
		scan.setCaching(1000);
		scan.setMaxVersions();
		scan.setBatch(1000);
		// scan.setStartRow("1370142206437_fvvplr2394".getBytes());

		String key = null;
		String last_key = null;
		String date = null;
		String date_time = null;
		String value = null;

		int count_allid = 0;
		int count_activeid = 0;
		int limitcount_activeid = 1000000;
		int avg_urls_per_id = 0;

		String resultFileName = "activeUsr20150421";
		// int resultFileIndex = 0;
		LinkedList<String> activeid_list = new LinkedList<String>();
		HashMap<String, String> date_hashmap_per_id = new HashMap<String, String>();
		HashMap<String, String> url_hashmap_per_id = new HashMap<String, String>();
		ResultScanner rs = null;

		boolean should_record = true;

		try
		{
			rs = table.getScanner(scan);
			if (rs == null)
			{
				LOG.error("get scan failed");
				return;
			}
			boolean done_get_allrow = true;
			while (true)
			{
				for (Result result : rs)
				{
					boolean update_to_currentmonth = false;
					count_allid++;
					date_hashmap_per_id.clear();
					url_hashmap_per_id.clear();
					avg_urls_per_id = 0;

					try
					{
						key = new String(result.getRow());
					}
					catch (Exception e)
					{
						LOG.error("get row error", e);
						done_get_allrow = false;
						break;
					}
					for (KeyValue keyValue : result.raw())
					{
						date_time = new String(keyValue.getQualifier());
						date = date_time.substring(0, 8);
						value = new String(keyValue.getValue());
						String[] urls_per_id = value.split("!");
						for (String url_temp : urls_per_id)
							url_hashmap_per_id.put(url_temp.split("\t")[0], "");

						// 有最近一个月（目前是1月）的访问记录。此条件以后需要删除或更改
						if (date.compareTo("20150401") >= 0)
							update_to_currentmonth = true;

						date_hashmap_per_id.put(date, "");
					}

					if (!update_to_currentmonth)
						continue;

					if (date_hashmap_per_id.size() < activeDayCount)
					{
						continue;
					}

					avg_urls_per_id = url_hashmap_per_id.size() / date_hashmap_per_id.size();
					if (avg_urls_per_id < 2 || avg_urls_per_id > 50)
						continue;

					last_key = key;
					count_activeid++;
					if (should_record)
					{
						activeid_list.add(key);
					}

					LOG.info("[" + count_activeid + "-" + count_allid + "]\tactive id:" + key);

					if (count_activeid >= limitcount_activeid)
						should_record = false;

					if (should_record)
					{
						if (count_activeid % 2000 == 0)
						{
							StringBuffer sb = new StringBuffer();
							for (String activeid : activeid_list)
								sb.append(activeid + "\r\n");
							activeid_list.clear();

							FileWriter fileWriter = new FileWriter(resultFileName, true);
							fileWriter.write(sb.toString());
							fileWriter.close();
							sb = null;

						}
					}
				}// end for

				if (activeid_list != null && activeid_list.size() > 0)
				{
					StringBuffer sb = new StringBuffer();
					for (String activeid : activeid_list)
						sb.append(activeid + "\r\n");
					activeid_list.clear();

					FileWriter fileWriter = new FileWriter(resultFileName, true);
					fileWriter.write(sb.toString());
					fileWriter.close();
					sb = null;
				}
				if (done_get_allrow)
					break;
				else
				{
					LOG.info("catch exception while getting row,re-get form id:" + last_key);
					scan.setStartRow(last_key.getBytes());
					try
					{
						if (table != null)
							table.close();
						if (rs != null)
							rs.close();
					}
					catch (Exception e)
					{
						LOG.error("close table/rs error", e);
						break;
					}
					LOG.info("sleep 10 seconds");
					try
					{
						Thread.sleep(10000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					table = HbaseInterface.getHTable(tableName);
					if (table == null)
					{
						LOG.error("get table failed");
						break;
					}
					rs = table.getScanner(scan);
					count_allid--;
					done_get_allrow = true;
				}
			}// end while

			if (rs != null)
				rs.close();

		}
		catch (IOException e)
		{
			LOG.error("scan error", e);
		}

	}

	/**
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
	 */
	public static HashMap<String, String> queryUserIDInDateRange(String usrID, String timeStamp, int interval)
	{
		LOG.info("start query user id:" + usrID + "\tinterval:" + interval + "\ttimestamp:" + timeStamp);
		checkLogTypeInit();
		String endDate = parseTimeStamp(timeStamp, "yyyyMMdd");
		List<String> aval_date_list = getDateList(endDate, interval, false);

		// for (String date : aval_date_list) {
		// System.out.println(date);
		// }
		HashMap<String, String> id_hashmap = queryByUsrID(usrID);

		HashMap<String, String> result_hashmap = new HashMap<String, String>();

		Iterator<Entry<String, String>> iterator_id = id_hashmap.entrySet().iterator();

		while (iterator_id.hasNext())
		{
			Map.Entry<String, String> entry = iterator_id.next();

			String id = entry.getKey();
			String value = entry.getValue();
			if (aval_date_list.contains(id))
				result_hashmap.put(id, value);
		}
		LOG.info("end query user id " + usrID + " int " + interval + " by " + timeStamp);
		return result_hashmap;
	}

	public static void main(String[] args)
	{
		System.out
				.println("Switch:\r\n\t[0-->QueryAllUserID]\r\n\t[1-->QueryUsrID]\r\n\t[2-->QueryUsrIDAndDate]\r\n\t[3-->QueryUserIDByPostFix]\r\n\t[4-->QueryUserIDByDateInterval]\r\n\t[5-->QueryRandomUserID]\r\n\t[6-->DeleteInvalidUserID]\r\n\t[7-->drop table(disable)]");
		// testOperation(args);
		logDBOperation.setLogType(LogType.APPLOG);
		System.out.println(tableName);
	}

	// start---------------辅助方法----------------start//
	public static HashMap<String, String> queryByUsrID_detail(String usrID)
	{
		LOG.info("start query user id:" + usrID);
		checkLogTypeInit();
		HTable table = null;
		HashMap<String, String> date_value_HashMap = new HashMap<String, String>();
		String date_time = null;
		StringBuffer value_buf = new StringBuffer();
		try
		{
			table = HbaseInterface.getHTable(tableName);
			if (table == null)
			{
				LOG.error("can not get table:" + tableName);
				return null;
			}
			Get get = new Get(usrID.getBytes());
			Result r = table.get(get);
			if (r.isEmpty())
			{
				LOG.error("cannot find usr id:" + usrID);
			}
			else
			{
				for (KeyValue keyValue : r.raw())
				{
					date_time = new String(keyValue.getQualifier());
					value_buf.append(new String(keyValue.getValue()));
					// if (date_value_HashMap.containsKey(date))
					// value_buf.append("!" + date_value_HashMap.get(date));
					date_value_HashMap.put(date_time, value_buf.toString());

					value_buf.delete(0, value_buf.length());
				}
			}
			table.close();
		}
		catch (IOException ex)
		{
			LOG.error("error", ex);
		}
		LOG.info("end query user id:" + usrID);
		return date_value_HashMap;
	}

	/**
	 * 测试程序，用于测试接口中各方法的准确性
	 * 
	 * @param args
	 */
//	public static void testOperation(String[] args)
//	{
//
//		if (args.length < 1)
//		{
//			LOG.error("invalid command");
//			return;
//		}
//		int switcher = Integer.parseInt(args[0]);
//		// String tableName = fieldDicts.pcLogTableNameInHbase;
//
//		String usrID = null;
//
//		LOG.info("TableName:" + tableName);
//
//		switch (switcher)
//		{
//		case 0:
//			int maxCount = -1;
//			logDBOperation.queryAllUserID(maxCount);
//			break;
//		case 1:
//			if (args.length < 2)
//			{
//				LOG.error("invalid command");
//				break;
//			}
//			usrID = args[1];
//			LOG.info("user id:" + usrID);
//			String day = null;
//			String value = null;
//			HashMap<String, String> userIdHashMap = logDBOperation.queryByUsrID_detail(usrID);
//			Iterator<Entry<String, String>> iterator = userIdHashMap.entrySet().iterator();
//			FileUtil fileUtil = new FileUtil();
//			fileUtil.Initialize("e:\\result.txt", "utf-8");
//			StringBuffer sb = new StringBuffer();
//
//			while (iterator.hasNext())
//			{
//				Map.Entry<String, String> entry = iterator.next();
//				day = entry.getKey();
//				value = entry.getValue();
//				System.out.println(day);
//				System.out.println(value);
//				sb.append(day + "\r\n");
//				sb.append(value + "\r\n");
//			}
//			fileUtil.Save("e:\\result_" + usrID, sb.toString());
//			break;
//		// case 2:
//		// if (args.length < 3) {
//		// LOG.error("invalid command");
//		// break;
//		// }
//		// usrID = args[1];
//		// String date = args[2];
//		// LOG.info("UserID:" + usrID);
//		// LOG.info("Date:" + date);
//		//
//		// if (usrID == null || usrID.trim().equals("")) {
//		// LOG.error("invalid user id");
//		// break;
//		// }
//		// if (date == null || date.trim().equals("")) {
//		// LOG.error("invalid date");
//		// break;
//		// }
//		// logDBOperation.queryByUsrIDAndDate(usrID, date);
//		// break;
//		// case 3:
//		// if (args.length < 2) {
//		// LOG.error("invalid command");
//		// break;
//		// }
//		// String postfix = args[1];
//		// LOG.info("PostFix:" + postfix);
//		// if (postfix == null || postfix.trim().equals("")) {
//		// LOG.error("invalid postfix");
//		// break;
//		// }
//		// // logDBOperation.queryUserIDByPostFix(postfix);
//		// break;
//		case 4:
//			int activeDayCount = 20;
//			queryActiveUserID(activeDayCount);
//			break;
//		case 5:
//			// logDBOperation.queryRandomUserID();
//			break;
//		case 6:
//			// logDBOperation.deleteInvalidUserID();
//			break;
//		default:
//			break;
//		}
//	}

	/**
	 * 获取以startDate开始，以after为方向，以count为范围之内的所有date
	 * 
	 * @param startDate
	 * @param count
	 * @param after
	 *            true表示从startDate向后取，false表示向前取
	 * @return
	 */
	public static List<String> getDateList(String startDate, int count, boolean after)
	{
		List<String> dateList = new LinkedList<String>();
		if (count < 0)
			return null;
		int dateUnit = after ? 1 : -1;
		String datePattern = "yyyyMMdd";
		Pattern date_pattern = Pattern.compile("[\\d]{8}");
		if (date_pattern.matcher(startDate).matches())
		{
			Calendar c = Calendar.getInstance();
			Date date = null;
			try
			{
				date = new SimpleDateFormat(datePattern).parse(startDate);
			}
			catch (ParseException e)
			{
				LOG.error("parse date error", e);
			}
			c.setTime(date);
			int day = c.get(Calendar.DATE);

			dateList.add(new SimpleDateFormat(datePattern).format(c.getTime()));

			for (int i = 0; i < count - 1; i++)
			{
				day = c.get(Calendar.DATE);
				c.set(Calendar.DATE, day + dateUnit);
				dateList.add(new SimpleDateFormat(datePattern).format(c.getTime()));
			}
		}
		return dateList;
	}

	/**
	 * 获取startDate和endDate之间的所有date，不包括endDate
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static List<String> getDateList(String startDate, String endDate)
	{
		List<String> dateList = new LinkedList<String>();
		String datePattern = "yyyyMMdd";
		Pattern date_pattern = Pattern.compile("[\\d]{8}");
		if (!date_pattern.matcher(startDate).matches() || !date_pattern.matcher(endDate).matches())
		{
			return null;
		}
		Calendar c = Calendar.getInstance();
		Date start_date = null;
		Date end_date = null;
		try
		{
			start_date = new SimpleDateFormat(datePattern).parse(startDate);
			end_date = new SimpleDateFormat(datePattern).parse(endDate);

			if (start_date.after(end_date))
			{
				LOG.error("start date:" + startDate + " should before " + endDate + ":" + endDate);
				return null;
			}
			else
			{
				c.setTime(start_date);
				dateList.add(startDate);
				while (start_date.before(end_date))
				{
					int day = c.get(Calendar.DATE);
					c.set(Calendar.DATE, day + 1);
					String dateTemp = new SimpleDateFormat(datePattern).format(c.getTime());
					start_date = new SimpleDateFormat(datePattern).parse(dateTemp);
					dateList.add(dateTemp);
				}
			}
		}
		catch (ParseException e)
		{
			LOG.error("parse date error", e);
		}

		return dateList;
	}

	/**
	 * 获取指定日期的指定天数之前或之后的日期，并根据datePattern格式输出
	 * 
	 * @param specifiedDay
	 *            开始日期
	 * @param dayCount
	 *            相隔天数
	 * @param after
	 *            true表示从开始日期之后，false表示从开始日期之前
	 * @param datePattern
	 *            结果日期的返回格式
	 * @return
	 */
	public static String getDay(String specifiedDay, int dayCount, boolean after, String datePattern)
	{
		if (dayCount == 0)
			return specifiedDay;
		dayCount = after ? Math.abs(dayCount) : (-1 * Math.abs(dayCount));
		Calendar c = Calendar.getInstance();
		Date date = null;
		try
		{
			date = new SimpleDateFormat(datePattern).parse(specifiedDay);
		}
		catch (ParseException e)
		{
			LOG.error("parse date error", e);
		}
		c.setTime(date);
		int current_day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, current_day + dayCount);

		String day = new SimpleDateFormat(datePattern).format(c.getTime());
		return day;
	}

	/**
	 * 获取当前日期 格式为yyyyMMdd
	 * 
	 * @return
	 */
	public static String getCurrentDate()
	{
		Calendar calendar = Calendar.getInstance();
		String datePattern = "yyyyMMdd";
		return new SimpleDateFormat(datePattern).format(calendar.getTime());
	}

	/**
	 * 时间戳转换为date date格式为yyyy/MM/dd HH:mm:ss
	 * 
	 * @param timeStamp
	 * @return
	 */
	public static String parseTimeStamp(String timeStamp)
	{
		Timestamp ts = new Timestamp(Long.parseLong(timeStamp));
		String tsStr = "";
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try
		{
			tsStr = sdf.format(ts);
		}
		catch (Exception e)
		{
			LOG.error("pare timeStamp error", e);
		}
		return tsStr;
	}

	/**
	 * 时间戳转换为date date格式为yyyy/MM/dd HH:mm:ss
	 * 
	 * @param timeStamp
	 * @return
	 */
	public static String parseTimeStamp(String timeStamp, String datePattern)
	{
		Timestamp ts = new Timestamp(Long.parseLong(timeStamp));
		String tsStr = "";
		DateFormat sdf = new SimpleDateFormat(datePattern);
		try
		{
			tsStr = sdf.format(ts);
		}
		catch (Exception e)
		{
			LOG.error("pare timeStamp error", e);
		}
		return tsStr;
	}

	/**
	 * 获得指定日期的时间戳
	 * 
	 * @param nowDate
	 * @return
	 */
	public static long getTimeStamp(String nowDate)
	{
		long l = -1;
		try
		{
			Date date1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(nowDate);
			Date date2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("1970/01/01 08:00:00");
			l = date1.getTime() - date2.getTime() > 0 ? date1.getTime() - date2.getTime() : date2.getTime() - date1.getTime();
		}
		catch (Exception e)
		{
			LOG.error("pare date error", e);
		}
		return l;
	}

	/**
	 * 判断字符串是否为数字组成
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str)
	{
		boolean isNum = false;
		try
		{
			Pattern pattern = Pattern.compile("[0-9]*");
			isNum = pattern.matcher(str).matches();
		}
		catch (Exception e)
		{
			LOG.error("parse str error", e);
		}
		return isNum;
	}

	/**
	 * pc端log的user id可能会有多种形式
	 * 下划线前为时间戳信息，但可能由于用户本机时间设置问题，导致时间戳异常，如32939097955203_2k0ryf7128
	 * 下划线后为随机字符串信息，一般要求为小于10位，但是会有个别大于10位，此处放宽处理 有效user
	 * id的规则为：10~16位的时间戳+下划线+16位以内的随机字串，其它的视为非法，直接丢弃
	 */
	public static boolean checkPcLogUserID(String userID)
	{
		boolean valid = false;
		try
		{
			Pattern pattern = Pattern.compile("[\\d]{10,16}_[\\w]{1,16}");
			valid = pattern.matcher(userID).matches();
		}
		catch (Exception e)
		{
			LOG.error("parse userID error", e);
		}
		return valid;
	}

	/**
	 * drop table,慎用
	 * 
	 * @param tableName
	 */
	public static void dropTable(String tablename)
	{
		HbaseInterface.dropTable(tablename);
	}
	// end---------------辅助方法----------------end//


	/**
	 * 函数重载，指定数据库表名进行操作，根据usrID(rowKey)查找整行数据，返回结果保存在hashmap中 其中key为日期，value为此日期下的用户行为。
	 * 
	 * @param tablename    	指定Hbase表的名字 进行操作
	 * @param usrID			用户ID
	 * 
	 * @return				hashmap对象，key为day,value为day对应的用户行为
	 */
private static HashMap<String, String> queryByUsrID(String tablename, String usrID)
{
	LOG.info("start query user id:" + usrID);
	HTable table = null;
	HashMap<String, String> date_value_HashMap = new HashMap<String, String>();
	String date_time = null;
	String date = null;
	StringBuffer value_buf = new StringBuffer();
	try
	{
		table = HbaseInterface.getHTable(tablename);
		if (table == null)
		{
			LOG.error("can not get table:" + tablename);
			return null;
		}
		Get get = new Get(usrID.getBytes());
		Result r = table.get(get);
		if (r.isEmpty())
		{
			LOG.error("cannot find usr id:" + usrID);
		}
		else
		{
			for (KeyValue keyValue : r.raw())
			{
				date_time = new String(keyValue.getQualifier());
				date = date_time.substring(0, 8);
				value_buf.append(new String(keyValue.getValue()));
				if (date_value_HashMap.containsKey(date))
					value_buf.append("!" + date_value_HashMap.get(date));
				date_value_HashMap.put(date, value_buf.toString());

				value_buf.delete(0, value_buf.length());
			}
		}
		table.close();
	}
	catch (IOException ex)
	{
		LOG.error("error", ex);
	}
	LOG.info("end query user id:" + usrID);
	return date_value_HashMap;
}



/**
 * 查询函数重载，查询指定的用户id的指定时间间隔内的所有用户行为
 * 
 * @param tablename			指定Hbase表的名称 进行操作
 * @param usrID  			用户id
 * @param timeStamp			截止日期的时间戳信息，为13位数字
 * @param interval			时间间隔，默认单位为一天
 * 
 * @return					hashmap对象，key为day,value为day对应的用户行为
 * 
 * 可供选择的tablename 	fieldDicts.pcUserLogTableNameInHbase;
						fieldDicts.appUserLogTableNameInHbase;
						ieldDicts.appRealtimeUserLogTableNameInHbase;
 */
public static HashMap<String, String> queryUserIDInDateRange(String tablename, String usrID, String timeStamp, int interval)
{
	LOG.info("start query user id:" + usrID + "\tinterval:" + interval + "\ttimestamp:" + timeStamp);
	String endDate = parseTimeStamp(timeStamp, "yyyyMMdd");
	List<String> aval_date_list = getDateList(endDate, interval, false);

	HashMap<String, String> id_hashmap = queryByUsrID(tablename, usrID);

	HashMap<String, String> result_hashmap = new HashMap<String, String>();

	Iterator<Entry<String, String>> iterator_id = id_hashmap.entrySet().iterator();

	while (iterator_id.hasNext())
	{
		Map.Entry<String, String> entry = iterator_id.next();

		String id = entry.getKey();
		String value = entry.getValue();
		if (aval_date_list.contains(id))
			result_hashmap.put(id, value);
	}
	LOG.info("end query user id " + usrID + " int " + interval + " by " + timeStamp);
	return result_hashmap;
}

}
