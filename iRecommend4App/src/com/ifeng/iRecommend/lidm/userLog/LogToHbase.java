package com.ifeng.iRecommend.lidm.userLog;

/**
 * <PRE>
 * 作用 : 
 *   将log解析后的数据写入数据库hbase。
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
 *          1.0          2014-01-21       lidm          change
 * -----------------------------------------------------------------------------
 * </PRE>
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ifeng.commen.lidm.hbase.HbaseInterface;

public class LogToHbase implements LogToDB {
	private static final Log LOG = LogFactory.getLog("log_to_hbase");
	
	private String logDate;// log文件的日期，如2013-10-28
	private String tableName;// hbase中的数据表名称
	
	private HashMap<String, String> dataHashMap;// 缓存log文件中的信息
	
	private static boolean dbInited = false;
	private DatabaseType dbType = DatabaseType.HBASE;
	private static final String USERID_FAMILY = "userid_family";
	private static final int timeToLive = 6 * 30 * 24 * 60 * 60;// 设置数据过期时间为半年，过期后数据会自动删除

	static {
		// 设置hbase中一些log级别,避免输出一些info信息
		Logger.getLogger("org.apache.zookeeper").setLevel(Level.ERROR);
		Logger.getLogger("org.apache.hadoop.hbase").setLevel(Level.ERROR);
	}

	public LogToHbase(HashMap<String, String> dataHashMap) {
		this.dataHashMap = dataHashMap;
		dbType = DatabaseType.HBASE;
	}

	public LogToHbase(String tableName,HashMap<String, String> dataHashMap) {
		this.dataHashMap = dataHashMap;
		dbType = DatabaseType.HBASE;
		this.tableName=tableName;
	}

	@Override
	public void InitDB() {
		if (dbInited)
			return;
		dbInited = true;

		LOG.info("init db");

		//String table_name = fieldDicts.pcLogTableNameInHbase;
		LOG.info("table_name:" + tableName);

		// 创建info表
		HbaseInterface.createTable(tableName, USERID_FAMILY, timeToLive);
	}

	@Override
	public void PushLogToDB() {
		if (!dbInited)
			InitDB();
		PushLogToHbase(tableName, dataHashMap);
	}

	public void PushLogToHbase(String tableName,
			HashMap<String, String> dataHashMap) {
		long start_push = System.currentTimeMillis();

		LinkedList<Put> put_list = new LinkedList<Put>();
		String table_name =tableName;
		String userid;
		String userid_value;
		String col_name;
		String log_date = dataHashMap.get("log_date");
		String log_time = dataHashMap.get("log_time");

		// data_hash_map中保存了log_time和log_date信息，非log数据，需要排除
		//dataHashMap.remove("log_date");
		//dataHashMap.remove("log_time");
		
		Iterator<Entry<String, String>> iterator_rawdata = dataHashMap
				.entrySet().iterator();

		while (iterator_rawdata.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator_rawdata
					.next();
			userid = (String) entry.getKey();
			userid_value = (String) entry.getValue();

			if(userid.equals("log_date")||userid.equals("log_time"))
				continue;
			
			Put put = new Put(userid.getBytes());
			put.setWriteToWAL(false);
			col_name = log_date + "_" + log_time;
			put.add(USERID_FAMILY.getBytes(), col_name.getBytes(),
					userid_value.getBytes());
			put_list.add(put);
		}
		
		CountDownLatch countDownLatch_put = new CountDownLatch(1);
		PutThreadManager put_tm = new PutThreadManager("put_thread_manager",
				table_name, put_list, countDownLatch_put);
		put_tm.start();
		
		try {
			countDownLatch_put.await();
		} catch (InterruptedException ex) {
			LOG.error("wait for get index tm error");
		}

		long end_push = System.currentTimeMillis();
		LOG.info("duration of push data to hbase:" + (end_push - start_push));
	}
	
	/**
	 * 数据写入线程的管理线程
	 * 创建多个写入线程，批量写入数据
	 * @author lidm
	 *
	 */
	class PutThreadManager extends Thread {
		private LinkedList<Put> put_list = new LinkedList<Put>();
		private String table_name;
		private int put_threads_count = 50;
		private CountDownLatch countDownLatch;

		public PutThreadManager(String thread_name, String table_name,
				LinkedList<Put> put_list, CountDownLatch countDownLatch) {
			super(thread_name);
			this.table_name = table_name;
			this.put_list = put_list;
			this.countDownLatch = countDownLatch;
		}

		public void run() {
			//long start_put = System.currentTimeMillis();
			doWork();
			countDownLatch.countDown();
			//long end_put = System.currentTimeMillis();
			//LOG.info("duration of put put_list:" + (end_put - start_put));
		}

		public void doWork() {
			// 启动10个PutThread线程put put_list
			int s_index = 0;
			int e_index = 0;
			int put_list_size = put_list.size();
			int _put_threads_count = put_threads_count;
			LOG.info("size of put_list:" + put_list_size);
			if (_put_threads_count > put_list_size)
				_put_threads_count = put_list_size;
			CountDownLatch countDownLatch_put = new CountDownLatch(
					_put_threads_count);
			int p_unit = put_list_size / _put_threads_count;

			for (int i = 0; i < _put_threads_count; i++) {
				s_index = i * p_unit;
				if (i == _put_threads_count - 1)
					e_index = put_list_size;
				else
					e_index = s_index + p_unit;
				List<Put> sub_put_list = put_list.subList(s_index, e_index);
				PutThread putThread = new PutThread("put_thread" + i,
						table_name, sub_put_list, countDownLatch_put);
				putThread.start();
			}
			try {
				countDownLatch_put.await();
			} catch (InterruptedException ex) {
				LOG.error("wait for " + table_name + " put threads error", ex);
			}
		}
	}

	/**
	 * 数据读取线程的管理线程
	 * 创建多个读取线程，批量读取数据
	 * @author lidm
	 *
	 */
	class GetThreadManager extends Thread {
		private LinkedList<Get> get_list = new LinkedList<Get>();
		private String table_name;
		private int get_threads_count = 10;
		private CountDownLatch countDownLatch;
		HashMap<String, String> userid_index_hashmap;

		public GetThreadManager(String thread_name, String table_name,
				LinkedList<Get> get_list,
				HashMap<String, String> userid_index_hashmap,
				CountDownLatch countDownLatch) {
			super(thread_name);
			this.table_name = table_name;
			this.get_list = get_list;
			this.userid_index_hashmap = userid_index_hashmap;
			this.countDownLatch = countDownLatch;
		}

		public void run() {
			//long start_get = System.currentTimeMillis();
			doWork();
			countDownLatch.countDown();
			//long end_get = System.currentTimeMillis();
			//LOG.info("duration of get get_list:" + (end_get - start_get));
		}

		public void doWork() {
			// 启动10个GetThread线程执行过滤出的各get，并更新得到uncached userid
			int s_index = 0;
			int e_index = 0;
			int get_list_size = get_list.size();
			int _get_threads_count = get_threads_count;
			// LOG.info("size of uncached userid:" + get_list_size);
			if (_get_threads_count > get_list_size)
				_get_threads_count = get_list_size;
			CountDownLatch countDownLatch_get = new CountDownLatch(
					_get_threads_count);

			int g_unit = get_list_size / _get_threads_count;

			LinkedList<HashMap<String, String>> result_hm_list = new LinkedList<HashMap<String, String>>();
			for (int i = 0; i < _get_threads_count; i++) {
				HashMap<String, String> hm = new HashMap<String, String>();
				result_hm_list.add(hm);
			}

			for (int i = 0; i < _get_threads_count; i++) {
				s_index = i * g_unit;
				if (i == _get_threads_count - 1)
					e_index = get_list_size;
				else
					e_index = s_index + g_unit;
				List<Get> sub_get_list = get_list.subList(s_index, e_index);
				GetThread getThread = new GetThread("get_thread" + i,
						table_name, sub_get_list, countDownLatch_get,
						result_hm_list.get(i));
				getThread.start();
			}
			try {
				countDownLatch_get.await();
			} catch (InterruptedException ex) {
				LOG.error("wait for get-threads error", ex);
			}

			for (int i = 0; i < _get_threads_count; i++) {
				userid_index_hashmap.putAll(result_hm_list.get(i));
			}
		}
	}

	/**
	 * 数据写入线程
	 * 将put_list中的每个put写入数据表table_name中
	 * @author lidm
	 *
	 */
	class PutThread extends Thread {
		private List<Put> put_list;
		private HTable table;
		private String table_name;
		private CountDownLatch countDownLatch;

		public PutThread(String thread_name, String table_name,
				List<Put> put_list, CountDownLatch countDownLatch) {
			super(thread_name);
			this.table_name = table_name;
			this.put_list = put_list;
			this.countDownLatch = countDownLatch;
		}

		public void run() {
			doWork();
			countDownLatch.countDown();
		}

		public void doWork() {
			table = HbaseInterface.getHTable(table_name);
			if (table == null)
				return;
			try {
				table.put(put_list);
			} catch (IOException e) {
				LOG.error(getName() + " push data error", e);
			}
			if (table != null)
				try {
					table.close();
				} catch (IOException e) {
					LOG.error(getName() + " close table error", e);
				}
		}
	}

	/**
	 * 数据查询线程
	 * 从数据表table_name中取出get_list中每个get对应的值
	 * @author lidm
	 *
	 */
	class GetThread extends Thread {
		private List<Get> get_list;
		private HTable table;
		private String table_name;
		private CountDownLatch countDownLatch;
		HashMap<String, String> result_hashmap;

		public GetThread(String thread_name, String table_name,
				List<Get> get_list, CountDownLatch countDownLatch,
				HashMap<String, String> result_hashmap) {
			super(thread_name);
			this.table_name = table_name;
			this.get_list = get_list;
			this.countDownLatch = countDownLatch;
			this.result_hashmap = result_hashmap;
		}

		public void run() {
			doWork();
			countDownLatch.countDown();
		}

		public void doWork() {
			table = HbaseInterface.getHTable(table_name);
			if (table == null)
				return;
			String rowkey = null;
			String index = null;
			try {
				Result[] results = table.get(get_list);
				for (Result result : results) {
					if (result.isEmpty())
						continue;
					rowkey = new String(result.raw()[0].getRow());
					index = new String(result.raw()[0].getValue());
					// synchronized (result_hashmap) {
					result_hashmap.put(rowkey, index);
					// }
				}
			} catch (IOException e) {
				LOG.error(getName() + " get data error", e);
			}
			if (table != null)
				try {
					table.close();
				} catch (IOException e) {
					LOG.error(getName() + " close table error", e);
				}
		}

	}
	
	public String getDatabaseType() {
		return dbType.name();
	}

	public HashMap<String, String> getDataHashMap() {
		return dataHashMap;
	}

	public String getLogDate() {
		return logDate;
	}

	//////////obsoluted code///////////
	
	/*
	public void PushLogToDB_v2() {
		if (!dbInited)
			InitDB();
		PushPCLogToHbase_v2(tableName, dataHashMap);
	}
	 */
	
	//将解析pc端日志得到的数据更新到hbase

	/*
	public void PushPCLogToHbase_v2(String tableName,
			HashMap<String, String> dataHashMap) {
		LOG.info("start put data to hbase");
		long start_push = System.currentTimeMillis();

		String userid_info_rowkey;
		String userid;
		String userid_value;
		String info_col_qualifier;
		String userid_date_col_name;

		// info table,stores all the value of every userid
		String info_table_name = pcLogTableNameInHbase;
		// userid-index table,stores all the userid and its index
		String userid_table_name = pclog_userid_hbase;
		// index-userid table
		String userid_post_table_name = pclog_userid_post_hbase;

		// puts to info table
		LinkedList<Put> info_put_list = new LinkedList<Put>();
		// puts to userid table
		LinkedList<Put> userid_put_list = new LinkedList<Put>();
		// puts to post userid table
		LinkedList<Put> userid_post_put_list = new LinkedList<Put>();
		// gets to userid table
		LinkedList<Get> userid_get_list = new LinkedList<Get>();

		HTable info_table = HbaseInterface.getHTable(info_table_name);// 获取info表
		HTable userid_table = HbaseInterface.getHTable(userid_table_name);// 获取userid表
		HTable userid_post_table = HbaseInterface
				.getHTable(userid_post_table_name);// 获取userid倒排表

		String log_time = dataHashMap.get("log_time");// log文件的时间(即log文件名称)，如1201
		String log_date = dataHashMap.get("log_date");// log文件的日期,如20131025

		// start get indexes
		// 过滤出未被缓存的userid-index，并放到get_list中
		Iterator<Entry<String, String>> iterator_rawdata = dataHashMap
				.entrySet().iterator();
		while (iterator_rawdata.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator_rawdata
					.next();
			userid = (String) entry.getKey();
			userid_value = (String) entry.getValue();

			// data_hash_map中保存了log_time和log_date信息，非log数据，需要排除
			if (userid.equals("log_time") || userid.equals("log_date")) {
				continue;
			}

			if (!userid_index_lru_cache.containsKey(userid)) {
				// 生成get list，一次性get，提高读取速度
				Get userid_get = new Get(userid.getBytes());
				userid_get.addColumn(userid_family.getBytes(),
						userid_index_qualifier.getBytes());
				userid_get_list.add(userid_get);
			}
		}// end while

		// get userid_gett_list by GetThreadManager,which will start 10
		// GetThread to finish the get process
		HashMap<String, String> userid_index_uncached = new HashMap<String, String>();
		CountDownLatch countDownLatch_userid_get = new CountDownLatch(1);
		GetThreadManager userid_get_tm = new GetThreadManager(
				"userid_get_thread", pclog_userid_hbase, userid_get_list,
				userid_index_uncached, countDownLatch_userid_get);
		userid_get_tm.start();
		try {
			countDownLatch_userid_get.await();
		} catch (InterruptedException ex) {
			LOG.error("wait for get index tm error");
		}
		LOG.info("size of uncached userid:" + userid_index_uncached.size());
		long end_get_index = System.currentTimeMillis();
		LOG.info("duration of get index:" + (end_get_index - start_push));
		// end get indexes

		// start init puts
		String u_id = null;
		String u_index = null;

		long start_init_put = System.currentTimeMillis();

		HashMap<String, String> userid_index_update = new HashMap<String, String>();
		//遍历每个userid，若此userid已被索引，则取其索引值写入hbase 否则对其索引，并更新userid表和post userid表
		 
		iterator_rawdata = dataHashMap.entrySet().iterator();
		while (iterator_rawdata.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator_rawdata
					.next();
			userid = (String) entry.getKey();
			userid_value = (String) entry.getValue();

			// data_hash_map中保存了log_time和log_date信息，非log数据，需要排除
			if (userid.equals("log_time") || userid.equals("log_date")) {
				continue;
			}

			u_id = userid;
			if (userid_index_lru_cache.containsKey(u_id))
				u_index = userid_index_lru_cache.get(u_id);
			else if (userid_index_uncached.containsKey(u_id))
				u_index = userid_index_uncached.get(u_id);
			else {
				u_index = null;
				// LOG.error("cannot find index of userid:" + u_id);
			}

			// 如果id_index_hashmap中即userid_table中不存在此userid的index值，表示其未被索引
			if (u_index == null || u_index.equals("")) {
				userid_size++;// userid_table size
				userid_post_size++;// userid_post_table size

				// index of this userid，以userid_table的size作为此userid的索引
				// current_userid_index = userid_size;
				u_index = String.valueOf(userid_size);
				// LOG.info("uncached (userid,index)=(" + u_id + "," + u_index +
				// ")");
				// 更新userid_put_list，后期提交更新到userid_table
				Put userid_put = new Put(u_id.getBytes());
				userid_put.setWriteToWAL(false);
				// 更新userid对应的index
				userid_put.add(userid_family.getBytes(),
						userid_index_qualifier.getBytes(), u_index.getBytes());
				// 更新userid对应的log_date
				userid_date_col_name = userid_date_qualifier_prefix + log_date;
				userid_put.add(userid_family.getBytes(),
						userid_date_col_name.getBytes(), log_date.getBytes());
				userid_put_list.add(userid_put);

				// 更新userid_post_put_list，后期提交更新到userid_post_table
				Put userid_post_put = new Put(u_index.getBytes());
				userid_post_put.setWriteToWAL(false);
				userid_post_put.add(userid_post_family.getBytes(),
						userid_post_userid_qualifier.getBytes(),
						u_id.getBytes());
				userid_post_put_list.add(userid_post_put);
				userid_index_update.put(u_id, u_index);
			} else {
				// userid_table中已存在此userid的索引
				// LOG.info("cached (userid,index)=(" + u_id + "," + u_index +
				// ")");

				// 更新userid对应的log_date
				Put userid_put = new Put(userid.getBytes());
				userid_put.setWriteToWAL(false);
				userid_date_col_name = userid_date_qualifier_prefix + log_date;
				userid_put.add(userid_family.getBytes(),
						userid_date_col_name.getBytes(), log_date.getBytes());
				userid_put_list.add(userid_put);
			}

			userid_info_rowkey = log_date + "#" + u_index;// info表中此userid对应的一行，行名称类似：20131028#1001
			info_col_qualifier = "info_" + log_date + "_" + log_time;// info表中userid对应的一行中其中一列，列名称类似：info_20131028_1201

			// 更新info_put_list，后期提交更新到info_table
			Put info_put = new Put(userid_info_rowkey.getBytes());
			info_put.setWriteToWAL(false);
			info_put.add(info_family.getBytes(), info_col_qualifier.getBytes(),
					userid_value.getBytes());
			info_put_list.add(info_put);
		}// end while

		LOG.info("current size:" + userid_size);

		// 更新userid_index缓存
		userid_index_lru_cache.putAll(userid_index_update);
		userid_index_lru_cache.putAll(userid_index_uncached);

		long end_init_put = System.currentTimeMillis();
		LOG.info("duration of init_put:" + (end_init_put - start_init_put));

		// 在info_table中插入以log_date为名称的起始节点，方便以后的查询操作，类似：20131028
		Put start_put = new Put((log_date).getBytes());
		start_put.setWriteToWAL(false);
		start_put.add(info_family.getBytes(), info_value_qualifier.getBytes(),
				"search point".getBytes());

		// 在info_table中插入以log_date+1为名称的结束节点，方便以后的查询操作，类似：20131029
		String log_date_end = logDBOperation.getDay(log_date, 1, true,
				"yyyyMMdd");
		Put end_put = new Put((log_date_end).getBytes());
		end_put.setWriteToWAL(false);
		end_put.add(info_family.getBytes(), info_value_qualifier.getBytes(),
				"search point".getBytes());

		info_put_list.add(start_put);
		info_put_list.add(end_put);

		// 更新userid_table的第一行，其中存储的是userid_table表的size
		Put userid_firstrow_put = new Put(userid_first_row.getBytes());
		// userid_firstrow_put.setWriteToWAL(false);
		userid_firstrow_put.add(userid_family.getBytes(),
				userid_index_qualifier.getBytes(), String.valueOf(userid_size)
						.getBytes());
		userid_put_list.add(userid_firstrow_put);

		// 更新userid_post_table的第一行，其中存储的是userid_post_table表的size
		Put userid_post_firstrow_put = new Put(userid_post_first_row.getBytes());
		// userid_firstrow_put.setWriteToWAL(false);
		userid_post_firstrow_put.add(userid_post_family.getBytes(),
				userid_post_userid_qualifier.getBytes(),
				String.valueOf(userid_post_size).getBytes());
		userid_post_put_list.add(userid_post_firstrow_put);
		// end init puts

		// start put puts
		long start_put_puts = System.currentTimeMillis();
		// put every put-list by PutThreadManager,which will start 10 PutThread
		// to finish the put process
		CountDownLatch countDownLatch_put_tm = new CountDownLatch(3);
		PutThreadManager info_put_tm = new PutThreadManager("info_put_thread",
				pcLogTableNameInHbase, info_put_list, countDownLatch_put_tm);
		PutThreadManager userid_put_tm = new PutThreadManager(
				"userid_put_thread", pclog_userid_hbase, userid_put_list,
				countDownLatch_put_tm);
		PutThreadManager userid_post_put_tm = new PutThreadManager(
				"userid_post_put_thread", pclog_userid_post_hbase,
				userid_post_put_list, countDownLatch_put_tm);

		info_put_tm.start();
		userid_put_tm.start();
		userid_post_put_tm.start();

		try {
			countDownLatch_put_tm.await();
		} catch (InterruptedException ex) {
			LOG.error("wait for put_tm_threads error", ex);
		}

		long end_put_puts = System.currentTimeMillis();
		LOG.info("duration of put put_list:" + (end_put_puts - start_put_puts));
		// end put puts

		// start test firstrow
		Get userid_get = new Get(userid_first_row.getBytes());
		Result userid_r = null;

		try {
			userid_r = userid_table.get(userid_get);
		} catch (IOException e) {
			LOG.error("get first row of userid_table error", e);
		}
		for (KeyValue keyValue : userid_r.raw()) {
			LOG.info("current size from firstrow:"
					+ new String(keyValue.getValue()));
		}

		// end test firstfow
		// close tables
		try {
			if (info_table != null)
				info_table.close();
			if (userid_table != null)
				userid_table.close();
			if (userid_post_table != null)
				userid_post_table.close();
		} catch (Exception e) {
			LOG.error("close htable error", e);
		}

		long end_push = System.currentTimeMillis();
		LOG.info("duration of put data:" + (end_push - start_push));
		LOG.info("finish put data to hbase");
	}
*/
	/*
	public void InitDB_v2() {
		if (dbInited)
			return;
		dbInited = true;

		LOG.info("init db");

		// 缓存userid_index，减少get userid的次数
		userid_index_lru_cache = new LRUCache<String, String>(max_userid_index);

		String info_table_name = pcLogTableNameInHbase;
		String userid_table_name = pclog_userid_hbase;
		String userid_post_table_name = pclog_userid_post_hbase;

		LOG.info("info_table:" + info_table_name);
		LOG.info("userid_table:" + userid_table_name);
		LOG.info("userid_post_table:" + userid_post_table_name);

		userid_size = 0;
		userid_post_size = 0;

		// 创建info表
		HbaseInterface.createTable(info_table_name, info_family, timeToLive);
		// 创建userid表
		HbaseInterface.createTable(userid_table_name, userid_family, -1);
		// 创建userid倒排表
		HbaseInterface.createTable(userid_post_table_name, userid_post_family,
				-1);

		// 获取info表
		HTable info_table = HbaseInterface.getHTable(info_table_name);
		// 获取userid表
		HTable userid_table = HbaseInterface.getHTable(userid_table_name);
		// 获取userid倒排表
		HTable userid_post_table = HbaseInterface
				.getHTable(userid_post_table_name);

		// init first row of userid_table
		if (userid_table != null) {
			Get userid_get = new Get(userid_first_row.getBytes());
			Result userid_r = null;

			try {
				userid_r = userid_table.get(userid_get);
			} catch (IOException e) {
				LOG.error("get first row of userid_table error", e);
			}
			if (!userid_r.isEmpty()) {
				userid_size = Integer.parseInt(new String(userid_r.raw()[0]
						.getValue()));
			} else {
				Put userid_firstrow_put = new Put(userid_first_row.getBytes());
				userid_firstrow_put.add(userid_family.getBytes(),
						userid_index_qualifier.getBytes(), "0".getBytes());
				try {
					userid_table.put(userid_firstrow_put);
				} catch (IOException e) {
					LOG.error("put first row to userid_table error", e);
				}
			}
			LOG.info("size of userid_table:" + userid_size);
		}

		// init first row of userid_post_table
		if (userid_post_table != null) {
			Get userid_post_get = new Get(userid_post_first_row.getBytes());
			Result userid_post_r = null;

			try {
				userid_post_r = userid_post_table.get(userid_post_get);
			} catch (IOException e) {
				LOG.error("get first row of userid_post_table error", e);
			}
			if (!userid_post_r.isEmpty()) {
				userid_post_size = Integer.parseInt(new String(userid_post_r
						.raw()[0].getValue()));
			} else {
				Put userid_post_firstrow_put = new Put(
						userid_post_first_row.getBytes());
				userid_post_firstrow_put
						.add(userid_post_family.getBytes(),
								userid_post_userid_qualifier.getBytes(),
								"0".getBytes());
				try {
					userid_post_table.put(userid_post_firstrow_put);
				} catch (IOException e) {
					LOG.error("put first row to userid_post_table error", e);
				}
			}
			LOG.info("size of userid_post_table:" + userid_post_size);
		}
		try {
			if (info_table != null)
				info_table.close();
			if (userid_table != null)
				userid_table.close();
			if (userid_post_table != null)
				userid_post_table.close();
		} catch (Exception e) {
			LOG.error("close htable error", e);
		}
	}
*/
}
