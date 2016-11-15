package com.ifeng.iRecommend.featureEngineering.databaseOperation;

import ikvdb.client.IkvdbClient;
import ikvdb.client.IkvdbClientConfig;
import ikvdb.client.IkvdbClientFactory;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
/**
 * 
 * <PRE>
 * 作用 : IKV数据库操作统一接口
 *   
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :                   
 * 正在使用的IKV Table：  cmppitems    cmpp抓取文章item
 * 						xmlitems     xml同步文章item
 * 						relaids		 客户端相关推荐
 * 						cmppDyn      手凤相关推荐
 * 	 
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016-2-22         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class IKVOperation {
	private static final Log LOG = LogFactory.getLog("IKVOperation");
	// 声明一个ikv client
	private IkvdbClient<String, String> client;
	// ikvclient所调用的IKV表名
	private String IKV_TABLE_NAME;

	public IKVOperation(String tablename) {
		setTableName(tablename);
		// 初始化ikv;
		IkvdbClientConfig config = new IkvdbClientConfig();
		// 设置服务器地址（启动路径），可以设置多个，保证至少有一个能连接上
		String[] urls = new String[] { "tcp://10.32.25.30:6666", "tcp://10.32.25.40:6666", "tcp://10.32.25.50:6666", };
		config.setBootstrapUrls(Arrays.asList(urls));
		IkvdbClientFactory factory = new IkvdbClientFactory(config);
		client = factory.getClient(IKV_TABLE_NAME);
		LOG.info(IKV_TABLE_NAME + " connect...");
	}

	//设置表名称
	public void setTableName(String tablename) {
		this.IKV_TABLE_NAME = tablename;
	}

	/**
	 * 在IKV中添加type 注意： useless now
	 * 
	 * @param key
	 *            ， value
	 * @return
	 */
	public void add(String key, String value) {
		if (key == null)
			return;
		if (value == null)
			return;
		try {
			client.add(key, value);
		} catch (Exception e) {
			LOG.error("ERROR add key: " + key);
			LOG.error("ERROR add toJson", e);
			return;
		}
	}

	/**
	 * 在IKV中重置tag and type 注意：
	 * 
	 * @param key
	 *            ， value
	 * @return
	 */
	public void set(String key, String value) {
		if (key == null)
			return;
		if (value == null)
			return;
		try {
			client.set(key, value);
//			LOG.info("Successed set key " + key + "\t" + value);
		} catch (Exception e) {
			LOG.error("ERROR set key: " + key);
			LOG.error("ERROR set toJson", e);
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
		if (key != null)
			client.delete(key);
	}

	/**
	 * 在ikv中获取value 注意：
	 * 
	 * @param key
	 *            ，
	 * @return value
	 */
	protected String get(String key) {
		if (key == null)
			return null;
		String value = "";
		try {
			value = client.getValue(key);
		} catch (Exception e) {
			LOG.error("ERROR get: " + key);
			LOG.error("ERROR get", e);
			return null;
		}
		return value;
	}
	
	/**
	 * 查询itemf only for cmpp or xml item
	 * 
	 * @param key  key是title，url或者id。title和url是到id的索引，进行二次查询
	 * 			
	 * @return itemf，item及其特征表达类
	 * 
	 */
	public itemf queryItemF(String key) {
		itemf item = null;
		if (key == null || key.isEmpty())
			return null;
		if (isNumeric(key)) {
			String json = get(key);
			item = JsonUtils.fromJson(json, itemf.class);
		} else {
			String tempId = null;
			tempId = get(key);
			String json = get(tempId);
			item = JsonUtils.fromJson(json, itemf.class);
		}
		return item;
	}
	
	/**
	 * 判断是否为数字字符串
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
}
