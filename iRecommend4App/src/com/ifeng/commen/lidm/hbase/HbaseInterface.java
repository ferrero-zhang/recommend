package com.ifeng.commen.lidm.hbase;

/**
 * <PRE>
 * 作用 : 
 *   提供Hbase的使用接口，包括初始化Hbase、创建表、删除表等操作;
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
 *          1.0          2013-10-25        lidm          modify
 * -----------------------------------------------------------------------------
 * </PRE>
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.hfile.Compression.Algorithm;

public class HbaseInterface {
	private static final Log LOG = LogFactory.getLog("hbase");

	private static Configuration config;
	private static final String zookeeperQuorum = "10.32.21.115,10.32.21.125,10.32.21.130";

	static {
		config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum", zookeeperQuorum);
	}

	/**
	 * 创建表，默认只有一个列族 DATE
	 * 
	 * @param tableName
	 * @param colFamily
	 *            列族名称
	 * @param timeToLive
	 *            数据有效时间，超过此时间，数据会被自动清除，如果值不大于0，则数据永久保存
	 * @return rv=true:创建表成功;rv=false:创建表失败
	 */
	public static boolean createTable(String tableName, String colFamily,
			int timeToLive) {
		boolean rv = true;
		LOG.info("start create table:" + tableName);
		try {
			HBaseAdmin admin = new HBaseAdmin(config);
			if (admin.tableExists(tableName)) {
				LOG.info(tableName + " exists");
				rv = true;
			} else {
				HTableDescriptor descriptor = new HTableDescriptor(tableName);
				HColumnDescriptor columnDescriptor = new HColumnDescriptor(
						colFamily);

				// 设置数据的过期时间
				if (timeToLive > 0)
					columnDescriptor.setTimeToLive(timeToLive);

				// 启用数据压缩
				columnDescriptor.setCompressionType(Algorithm.GZ);
				// 只保存最新版本的数据
				columnDescriptor.setMaxVersions(1);
				descriptor.addFamily(columnDescriptor);
				admin.createTable(descriptor);
			}
			admin.close();
		} catch (MasterNotRunningException e) {
			LOG.error("Master Failed:" + e);
			rv = false;
		} catch (ZooKeeperConnectionException e) {
			LOG.error("ZooKeeper Connection Failed:" + e);
			rv = false;
		} catch (IOException e) {
			LOG.error("IO Exception:" + e);
			rv = false;
		}
		if (!rv) {
			LOG.error("Create Table Failed:" + tableName);
		} else {
			LOG.info("Table " + tableName + " Created");
		}
		LOG.info("finish create table:" + tableName);
		return rv;
	}

	/**
	 * 返回固定数目的htable数组，比如用于多table批量插入数据
	 * 
	 * @param tableCount
	 * @param tableName
	 * @return
	 */
	public static HTable[] getHTables(int tableCount, String tableName) {
		HTable[] hTables = new HTable[tableCount];
		for (int i = 0; i < tableCount; i++) {
			try {
				hTables[i] = new HTable(config, tableName);
				hTables[i].setWriteBufferSize(20 * 1024 * 1024);
			} catch (IOException e) {
				LOG.error("Get Table Failed:" + e);
				hTables[i].setAutoFlush(false);
			}
		}
		return hTables;
	}

	/**
	 * 获取htable对象
	 * 
	 * @param tableName
	 * @return
	 */
	public static HTable getHTable(String tableName) {
		HTable hTable = null;
		try {
			hTable = new HTable(config, tableName);
			hTable.setAutoFlush(false);
			hTable.setWriteBufferSize(20 * 1024 * 1024);// 20m
		} catch (IOException e) {
			LOG.error("Get Table Failed:" + e);
		}
		return hTable;
	}

	public static Configuration getConfig() {
		return config;
	}

	/**
	 * 在数据表中插入一行数据，需要提供此行数据的rowKey、列族名称、列名称以及value
	 * 
	 * @param tableName
	 *            数据表的名称
	 * @param rowKey
	 *            行的名称
	 * @param colFamily
	 *            列族
	 * @param colName
	 *            列的名称
	 * @param value
	 *            值
	 * @return rv=true:插入数据成功;rv=false:插入数据失败
	 */
	public static boolean insertRow(String tableName, String rowKey,
			String colFamily, String colName, String value) {
		boolean rv = true;
		HTable table = getHTable(tableName);
		if (table == null) {
			LOG.error("Table:" + tableName + "Not Exists!");
			return false;
		}
		table.setAutoFlush(false); // 数据入库之前先设置此项为false
		try {
			table.setWriteBufferSize(1024 * 1024 * 5);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Put put = new Put(rowKey.getBytes());
		put.setWriteToWAL(false);
		put.add(colFamily.getBytes(), colName.getBytes(), value.getBytes());
		try {
			table.put(put);
			table.flushCommits();// 入库完成后，手动刷入数据
			if (table != null)
				table.close();
		} catch (IOException e) {
			LOG.error("Insert Row Failed:" + e);
		}
		return rv;
	}

	/**
	 * 批量插入数据
	 * 
	 * @param tableName
	 * @param putsList
	 *            put 链接，put对象中封装了数据及相关参数
	 * @return
	 */
	public static boolean insertRowList(String tableName,
			LinkedList<Put> putsList) {
		boolean rv = true;
		HTable hTable = getHTable(tableName);
		try {
			hTable.put(putsList);
			hTable.flushCommits();
		} catch (IOException e) {
			LOG.error("insert row_list failed", e);
			rv = false;
		}
		return rv;
	}

	/**
	 * 删除数据表，慎用
	 * 
	 * @param tableName
	 *            数据表的名称
	 * @return rv=true:删除表成功;rv=false:删除表失败
	 */
	public static boolean dropTable(String tableName) {
		LOG.info("start drop table......");
		boolean rv = true;
		try {
			HBaseAdmin admin = new HBaseAdmin(config);
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
			admin.close();
		} catch (MasterNotRunningException e) {
			LOG.error("Master Failed:" + e);
			rv = false;
		} catch (ZooKeeperConnectionException e) {
			LOG.error("ZooKeeper Connection Failed:" + e);
			rv = false;
		} catch (IOException e) {
			LOG.error("IO Exception:" + e);
			rv = false;
		}
		if (!rv) {
			LOG.error("Drop Table Failed");
		}
		LOG.info("end drop table......");
		return rv;
	}

	/**
	 * 根据rowKey删除一整行记录
	 * 
	 * @param tableName
	 * @param rowKey
	 * @return rv=true:删除成功;rv=false:删除失败
	 */
	public static boolean deleteRow(String tableName, String rowKey) {
		boolean rv = true;
		try {
			HTable table = getHTable(tableName);
			ArrayList<Delete> list = new ArrayList<Delete>();
			Delete d1 = new Delete(rowKey.getBytes());
			list.add(d1);
			table.delete(list);
		} catch (Exception ex) {
			LOG.error("delete row failed:" + ex);
			rv = false;
		}
		return rv;
	}

	/**
	 * 根据rowKey、colFamily及colName删除指定行中的指定列族的指定列
	 * 
	 * @param tableName
	 *            表
	 * @param rowKey
	 *            行
	 * @param colFamily
	 *            列族
	 * @param colName
	 *            列
	 * @return rv=true:删除成功;rv=false:删除失败
	 */
	public static boolean deleteCol(String tableName, String rowKey,
			String colFamily, String colName) {
		boolean rv = true;
		try {
			HTable table = getHTable(tableName);
			Delete delete = new Delete(rowKey.getBytes());
			delete.deleteColumn(colFamily.getBytes(), colName.getBytes());
			table.delete(delete);
		} catch (Exception ex) {
			LOG.error("Delete Col Failed:" + colName, ex);
			rv = false;
		}
		return rv;
	}

	public static HashMap<String, String> queryByRowKey(String tableName,
			String rowkey) {
		HashMap<String, String> result_hashmap = new HashMap<String, String>();
		HTable table =null;
		try {
			table = getHTable(tableName);
			if (table == null) {
				LOG.error("get table failed");
				return null;
			}
		} catch (Exception e) {
			LOG.error("get table error", e);
			return null;
		}
		Get get=new Get(rowkey.getBytes());
		Result result=null;
		try {
			result=table.get(get);
		} catch (IOException e) {
			LOG.error("get rowkey error", e);
			return null;
		}
		if(result==null){
			LOG.error("get rowkey failed");
			return null;
		}
		for (KeyValue keyValue : result.raw()) {
			String col = new String(keyValue.getQualifier());
			String value = new String(keyValue.getValue());
			result_hashmap.put(col, value);
		}
		return result_hashmap;
	}
}
