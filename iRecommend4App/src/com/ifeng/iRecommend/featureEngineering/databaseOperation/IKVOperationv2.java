package com.ifeng.iRecommend.featureEngineering.databaseOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.featureEngineering.dataStructure.appBill;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.ikvlite.IkvLiteClient;

public class IKVOperationv2 {
	static Logger LOG = Logger.getLogger(IKVOperationv2.class);
	// 访问 10.32.25.21或者10.32.25.22
	private final String HOST0 = "10.32.25.21";
	private final String HOST1 = "10.32.25.22";
	private final String KEYSPACE = "ikv";
	public static String[] defaultTables = { "appitemdb", "appbilldb" };
	private String TABLE = null;
	IkvLiteClient client;

	public IKVOperationv2(String tablename) {
		this.TABLE = tablename;
		boolean tnameCorr = checkTbaleName();
		if (!tnameCorr) {
			LOG.error("Tablename initial failed. Create IKV operation interface failed, return.");
			return;
		}
		try {
			client = new IkvLiteClient(KEYSPACE, tablename, true);
			client.connect(HOST0, HOST1);
			LOG.info("IKV client create success.");
		} catch (Exception e) {
			LOG.error("IKV client create failed.", e);
			return;
		}

	}

	private String get(String key) {
		if (key == null)
			return null;
		String value = "";
		try {
			value = client.get(key);
		} catch (Exception e) {
			LOG.error("ERROR get: " + key);
			LOG.error("ERROR get", e);
			return null;
		}
		return value;
	}

	private Map<String, String> gets(String... key) {
		if (key == null)
			return null;
		Map<String, String> retmap = new HashMap<String, String>();
		try {
			retmap = client.gets(key);
		} catch (Exception e) {
			LOG.error("ERROR get: " + key);
			LOG.error("ERROR get", e);
			return null;
		}
		return retmap;
	}

	public void put(String key, String value) {
		if (key == null)
			return;
		if (value == null)
			return;
		try {
			client.put(key, value);
			// LOG.info("Successed set key " + key + "\t" + value);
		} catch (Exception e) {
			LOG.error("ERROR put key: " + key);
			LOG.error("ERROR put toJson", e);
			return;
		}
	}

	/**
	 * 在IKV中删除tag type 注意：
	 * 
	 * @param key
	 *            ，
	 * @return
	 */
	public void del(String key) {
		if (key != null) {
			try {
				client.delete(key);
			} catch (Exception e) {
				LOG.error("Delete " + key + " failed.");
			}
		}

		LOG.info("Delete key " + key + " success.");
	}

	public void close() {
		client.close();
		LOG.info("Client to " + TABLE + " has closed.");
	}

	private boolean checkTbaleName() {
		ArrayList<String> tableList = new ArrayList<String>();
		for (int i = 0; i < defaultTables.length; i++) {
			tableList.add(defaultTables[i]);
		}
		if (null == this.TABLE) {
			LOG.warn("[WARNING]Table name setting failed, tablename is null.");
			return false;
		} else if (!tableList.contains(this.TABLE)) {
			LOG.warn("[WARNING]Table name you are using is not in the default list.");
		}
		return true;
	}

	/**
	 * 查询itemf only for cmpp and xml item
	 * 查询示例：
	 * 1.自有cmpp数据：4773047,c
	 * 2.imcp数据：       112320266,x
	 * 3.新的cmpp数据：cmpp_033760049820820,c
	 * 4.subid数据：     sub_xxxxxxx,c
	 * 5.标题：               态度！体重不够120？谈什么性感！,d
	 * 6.url:         http://mini.eastday.com/a/160823095712489.html,d
	 * 
	 * @param key
	 *            key是title，url或者id。title和url是到id的索引，进行二次查询
	 * @param type
	 *            查询的类型，用来区分id的来源，x表示xml，c表示cmpp,使用subid和newcmppid查询都写c
	 *            d表示默认（用于标题或url查询或者未知类型时将随机选取结果）
	 * @return itemf，item及其特征表达类
	 */
	public itemf queryItemF(String key, String type) {

		if (key == null || key.isEmpty())
			return null;
		if (!type.equals("c") && !type.equals("x") && !type.equals("d")) {
			LOG.error("Input type is error, return. " + key);
			return null;
		}
		itemf item = null;
		String json = null;
		if (isNumeric(key) && (type.equals("c") || type.equals("x"))) {
			json = get(type + "_" + key);
			item = JsonUtils.fromJson(json, itemf.class);
		} else if (isNumeric(key) && type.equals("d")) {
			json = get("c_" + key);
			if (json == null || json.isEmpty())
				json = get("x_" + key);
			if (json == null || json.isEmpty())
				return null;
			item = JsonUtils.fromJson(json, itemf.class);
		} else {
			String tempId = null;
			tempId = get(key);
			if (null == tempId || tempId.equals(""))
				return null;
			json = get("c_" + tempId);
			if (json == null || json.isEmpty())
				json = get("x_" + tempId);
			if (json == null || json.isEmpty())
				return null;
			item = JsonUtils.fromJson(json, itemf.class);
		}
		return item;
	}

	/**
	 * 批量查询 仅支持id查询
	 * 
	 * @param key
	 * @param type
	 * @return Map<String//id,itemf>
	 */
	public Map<String, itemf> queryItems(String type, String... keys) {

		if (keys == null || keys.length <= 0)
			return null;
		if (!type.equals("c") && !type.equals("x") && !type.equals("d")) {
			LOG.error("Input type is error, return. " + keys);
			return null;
		}
		String[] queryKeys = keys;
		Map<String, itemf> itemMap = new HashMap<String, itemf>();
		Map<String, String> jsonMap = new HashMap<String, String>();
		boolean pass;
		// 检查keys 数组的类型是否一致
		if (queryKeys.length > 1) {
			pass = isNumeric(queryKeys[0]);
			for (int i = 1; i < queryKeys.length; i++) {
				if (pass != isNumeric(queryKeys[i])) {
					LOG.error("Input keys in different space, return. " + queryKeys);
					return null;
				}
			}
		}
		// 获取类型信息
		pass = isNumeric(queryKeys[0]);
		if (pass && (type.equals("c") || type.equals("x"))) {
			queryKeys = addStr(queryKeys, type);
			jsonMap = gets(queryKeys);
			itemMap = getItemMap(jsonMap);
		} else if (pass && type.equals("d")) {
			queryKeys = addStr(queryKeys, "c");
			jsonMap = gets(queryKeys);
			if (jsonMap == null)
			{
				queryKeys = addStr(queryKeys, "x");
				jsonMap = gets(queryKeys);
			}
				
			if (jsonMap == null)
				return null;
			itemMap = getItemMap(jsonMap);
		}
		return itemMap;
	}

	/**
	 * 查询appitem
	 * 
	 * @param key
	 *            key是title，url或者id。title和url是到id的索引，进行二次查询
	 * @param type
	 *            查询的类型，用来区分id的来源，x表示xml，c表示cmpp
	 * @return appitem其特征表达类
	 */
	public appBill queryAppBill(String key, String type) {
		if (key == null || key.isEmpty())
			return null;
		if (!type.equals("c") && !type.equals("x") && !type.equals("d"))
			LOG.error("Input type is error, return. " + key);
		appBill appitem = null;
		String json = null;
		if (isNumeric(key) && (type.equals("c") || type.equals("x"))) {
			json = get(type + "_" + key);
			appitem = JsonUtils.fromJson(json, appBill.class);
		} else if (isNumeric(key) && type.equals("d")) {
			json = get("c_" + key);
			if (json == null || json.isEmpty())
				json = get("x_" + key);
			if (json == null || json.isEmpty())
				return null;
			appitem = JsonUtils.fromJson(json, appBill.class);
		} else {
			String tempId = null;
			tempId = get(key);
			if (null == tempId || tempId.equals(""))
				return null;
			json = get("c_" + tempId);
			if (json == null || json.isEmpty())
				json = get("x_" + tempId);
			if (json == null || json.isEmpty())
				return null;
			appitem = JsonUtils.fromJson(json, appBill.class);
		}
		return appitem;
	}

	/**
	 * 判断是否为数字字符串
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if (Character.isDigit(str.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}
	/**
	 * 为传入ids添加类型
	 * @param strs
	 * @param add
	 * @return
	 */
	private static String[] addStr(String[] strs, String add) {
		if (strs == null || add == null)
			return strs;
		String[] result = new String[strs.length];
		for (int i = 0; i < strs.length; i++) {
			result[i] = add + "_" + strs[i];
		}
		return result;
	}
	/**
	 * 循环获取itemf并导出到map中
	 * @param jsonMap
	 * @return
	 */
	private static Map<String, itemf> getItemMap(Map<String, String> jsonMap) {
		if (jsonMap == null)
			return null;
		Map<String, itemf> resultMap = new HashMap<String, itemf>();
		for (Entry<String, String> entry : jsonMap.entrySet()) {
			try {
				itemf item = JsonUtils.fromJson(entry.getValue(), itemf.class);
				resultMap.put(entry.getKey().split("_")[1], item);
			} catch (Exception e) {
				LOG.error("ERROR in change to item.");
			}
		}
		return resultMap;
	}
}
