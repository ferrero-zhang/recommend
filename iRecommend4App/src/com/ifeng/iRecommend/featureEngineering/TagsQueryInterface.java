package com.ifeng.iRecommend.featureEngineering;

import ikvdb.client.IkvdbClient;
import ikvdb.client.IkvdbClientConfig;
import ikvdb.client.IkvdbClientFactory;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.oscache.OSCache;
/**
 * 
 * <PRE>
 * 作用 : tagType查询接口
 *   
 *   
 * 使用 : 单例使用，输入tag词 返回该词的type 如c,sc,cn,s等 如一个词有多个type只返回最高级的type
 *   	级别设定如下{ "c", "sc", "cn", "e", "t", "s", "s1", "et", "kb", "kq", "ks", "kr", "kl" }
 * 示例 :
 *   
 * 注意 :
 * 	 
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015-8-8         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class TagsQueryInterface
{
	private static final Log LOG = LogFactory.getLog("queryTagType");
	// 声明一个ikv client
	private IkvdbClient<String, String> client;
	// ikvclient所调用的IKV表名
	private static final String IKV_TABLE_NAME = "tagitems";
	// ikv?oscache?
	// 特征的类型
	private static final String[] featureType = { "c", "sc", "cn", "e", "t", "s", "s1", "et", "kb", "kq", "ks", "kr", "kl" };
	
	private OSCache osCache;
	private static final String CACHE_NAME = "queryTags";

	private static TagsQueryInterface instance = new TagsQueryInterface();

	private TagsQueryInterface()
	{
		// 初始化ikv;
		IkvdbClientConfig config = new IkvdbClientConfig();

		// 设置服务器地址（启动路径），可以设置多个，保证至少有一个能连接上
		String[] urls = new String[] { "tcp://10.32.25.30:6666", "tcp://10.32.25.36:6666", "tcp://10.32.25.40:6666", "tcp://10.32.25.50:6666", };
		config.setBootstrapUrls(Arrays.asList(urls));

		IkvdbClientFactory factory = new IkvdbClientFactory(config);

		client = factory.getClient(IKV_TABLE_NAME);

		LOG.info(IKV_TABLE_NAME + " connect...");

		// oscache键值的存活周期
		int refreshInterval = 90 * 24 * 60 * 1000;

		LOG.info("refreshInterval = " + refreshInterval);
		// 初始OSCache_MOOD;
		osCache = new OSCache("conf/oscache_queryTags.properties", CACHE_NAME, refreshInterval);

		LOG.info(CACHE_NAME + " cache creating...");
	}

	/**
	 * 单例调用
	 * 
	 * @return
	 */
	public static TagsQueryInterface getInstance()
	{
		return instance;
	}

	/**
	 * 从oscache和ikv等多重cache中查询，得到tag的type信息；
	 * 
	 * 注意： 输入的是词，例如：足球
	 * 
	 * @param key
	 * 
	 * @return type 例如：c
	 * 
	 */
	public String queryTagType(String key)
	{
		if (key == null || key.isEmpty())
			return null;

		String type = null;
		try
		{
			type = (String) instance.osCache.get(key);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			LOG.error("[ERROR] There is no cache.");
		}
		if (type == null)
		{
			type = get(key);
			if (type != null)
			{
				instance.osCache.put(key, type);
			}
		}
		return type;
	}

	/**
	 * 在IKV中添加type 注意： useless now
	 * 
	 * @param key
	 *            ， value
	 * @return
	 */
	public void add(String key, String value)
	{
		if (key == null)
			return;
		if (value == null)
			return;
		
		
		try
		{
			client.add(key, value);
		}
		catch (Exception e)
		{
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
	public void set(String key, String value)
	{
		if (key == null)
			return;
		if (value == null)
			return;
		String historyType = queryTagType(key);
		int historyLoc = 999;
		int nowLoc = 0;
		if(get(key) != null)
		for(int i = 0; i < featureType.length; i++)
		{
			if(featureType[i].equals(historyType))
				historyLoc = i;
			if(featureType[i].equals(value))
				nowLoc = i;
		}
//		System.out.println("historyLoc "+historyLoc);
//		System.out.println("nowLoc "+nowLoc);
		if(historyLoc > nowLoc)
		try
		{
			client.set(key, value);
			LOG.info("Successed set key "+key+"\t"+value);
		}
		catch (Exception e)
		{
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
	public void del(String key)
	{
		if (key != null)
			client.delete(key);
	}

	/**
	 * 在ikv中获取type 注意：
	 * 
	 * @param key
	 *            ，
	 * @return value
	 */
	protected String get(String key)
	{
		if (key == null)
			return null;
		// String value = null;
		String type = "";
		try
		{
			type = client.getValue(key);
		}
		catch (Exception e)
		{
			LOG.error("ERROR get: " + key);
			LOG.error("ERROR get", e);
			return null;
		}
		return type;
	}

}
