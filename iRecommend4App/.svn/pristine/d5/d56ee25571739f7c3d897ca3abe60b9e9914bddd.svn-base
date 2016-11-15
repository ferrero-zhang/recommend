/**
 * 
 */
package com.ifeng.iRecommend.dingjw.itemParser;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.lidm.hbase.*;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

import ikvdb.client.IkvdbClient;
import ikvdb.client.IkvdbClientConfig;
import ikvdb.client.IkvdbClientFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

/**
 * <PRE>
 * 作用 : 
 *   PC端的文章的工具类，提供文章的操作接口
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
 *          1.0          2013-7-16        Dingjw          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class ItemOperation {

	private static final Log LOG = LogFactory.getLog("ItemOperation");

	private IkvdbClient<String, String> client;

	private static final String IKV_TABLE_NAME = "ir_items";

	private String tableName = fieldDicts.pcItemTableNameInHbase;// tableName默认pc端的item数据表
	

	private OSCache osCache;
	
	private static final String CACHE_NAME = "OSCACHE_ItemOperation";
	
	private static ItemOperation instance = new ItemOperation();
	
	private ItemOperation() {
		// 初始ikv;
		IkvdbClientConfig config = new IkvdbClientConfig();

		// 设置服务器地址（启动路径），可以设置多个，保证至少有一个能连接上
		String[] urls = new String[] { "tcp://10.32.25.30:6666",
				"tcp://10.32.25.36:6666", "tcp://10.32.25.40:6666",
				"tcp://10.32.25.50:6666", };
		config.setBootstrapUrls(Arrays.asList(urls));

		IkvdbClientFactory factory = new IkvdbClientFactory(config);

		client = factory.getClient(IKV_TABLE_NAME);

		LOG.info(IKV_TABLE_NAME + " connect...");

		// oscache键值的存活周期
		int refreshInterval = 90 * 24 * 60 * 1000;

		LOG.info("refreshInterval = " + refreshInterval);
		// 初始OSCache_MOOD;
		osCache = new OSCache("conf/oscache_itemOP.properties", CACHE_NAME,
				refreshInterval);
		// osCache = new OSCache("oscache_itemOP.properties",CACHE_NAME,
		// 1*1000);

		LOG.info(CACHE_NAME + " cache creating...");
	}

	public static ItemOperation getInstance(){
		return instance;
	}
	
	// item类型
	public enum ItemType {
		PCITEM, APPITEM, UNDEFINED
	};

	// Item的默认类型为UNDEFINED
	private ItemType itemType = ItemType.UNDEFINED;

	// 设置item的类型，并根据item类型自动设置合适的数据表，以利用接口读取数据表中的数据。
	// 在调用接口前设置，只设置一次即可
	public void setItemType(ItemType _ItemType) {
		itemType = _ItemType;
		setTableName(_ItemType);
	}

	// 手动设置数据表的名称，会自动更新item类型
	private void setTableName(ItemType itemType) {
		switch (itemType) {
		case PCITEM:
			tableName = fieldDicts.pcItemTableNameInHbase;
			break;
		case APPITEM:
			tableName = fieldDicts.appItemTableNameInHbase;
			break;
		default:
			break;
		}
		;
	}

	/**
	 * 检测log文件类型是否已设置，用于提示。
	 */
	public void checkItemTypeInit() {
		if (itemType == ItemType.UNDEFINED) {
			System.out.println("WARN:需要首先初始化Item数据的类型:");
			System.out.println("	PCITEM:pc端item类型，读取数据时会选取hbase数据中的pc端item数据表");
			System.out
					.println("	APPITEM:客户端item类型，读取数据时会选取hbase数据中的客户端item数据表");
			System.out.println("	UNDEFINED:未定义类型，读取数据时会默认选择pc端item数据表");
			System.out.println("	默认类型为UNDEFINED");
			System.out
					.println("	调用setItemType(ItemType itemType) 或setTableName(fieldDicts.pcItem...)进行初始化，否则将使用默认数据表");

			LOG.warn("WARN:需要首先初始化Item数据的类型:");
			LOG.warn("	PCITEM:pc端item类型，读取数据时会选取hbase数据中的pc端item数据表");
			LOG.warn("	APPITEM:客户端item类型，读取数据时会选取hbase数据中的客户端item数据表");
			LOG.warn("	UNDEFINED:未定义类型，读取数据时会默认选择pc端item数据表");
			LOG.warn("	默认类型为UNDEFINED");
			LOG.warn("	调用setItemType(ItemType itemType) 或setTableName(fieldDicts.pcItem...)进行初始化，否则将使用默认数据表");

			LOG.warn("WARNNING:ItemType is null,using default tableName: fieldDicts.pcItemTableNameInHbase,should call setItemType(ItemType) or setTableName(tableName) first to init itemType and tableName!");
			System.out
					.println("WARNNING:ItemType is null,using default tableName: fieldDicts.pcItemTableNameInHbase,should call setItemType(ItemType) or setTableName(tableName) first to init itemType and tableName!");
		}
	}

	/**
	 * 根据key从缓存(redis)中获取item，若缓存中没有，则从ikv中获取
	 * 
	 * 注意：目前如果ikv中没有，则还进入hbase中查询，历史问题；
	 * @param key
	 *            ，item的rowkey:是url，也有可能是ID
	 *          
	 * @return Item，文章类
	 * 
	 */
	public Item getItem(String key) {
		checkItemTypeInit();
		Item item = null;
		if (key == null || key.isEmpty())
			return null;

		if (key.startsWith("http://bbs.ifeng")
				|| key.startsWith("http://survey.ifeng"))
			return null;
		
		if((item = instance.get(key)) != null)
			return item;
		
		
		//@test
		//System.out.println("out redis:"+key);
		
		//@test,考虑到有些item的url后续才给，所以ikv的cache需要更新下
		if ((item = this.get(key)) != null
				&& item.getUrl() != null )
				//&& !item.getUrl().isEmpty())
		{
			
			instance.add(key, item);
			//cache
			osCache.put(key, item);
		}else {
			/*change likun 20141223,hbase出错，临时否决
			item = getItemFromHbase(key);
			if (item != null && item.getID() != null) {
				instance.add(key, item);
				//cache
				osCache.put(key, item);
				
			}
			*/
		}
		return item;
	}

	/**
	 * 从Hbase中查询Item，若hbase中没有，则从搜索获取 注意：
	 * 如果是无线客户端的文章，tableName不一样，同时url实际是imcp_id
	 * 
	 * @param key
	 * @return Item
	 */
	public Item getItemFromHbase(String key) {
		checkItemTypeInit();
		Item item = null;
		if (key == null || key.isEmpty())
			return null;
		HashMap<String, String> hm_itemInfo = null;
		try {
			hm_itemInfo = HbaseInterface.queryByRowKey(tableName, key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (hm_itemInfo != null && hm_itemInfo.size() != 0) {
			item = new Item(hm_itemInfo);
		} else {
			item = null;
		}
		return item;
	}

	/**
	 * 在oscache中添加item 注意：
	 * 
	 * @param key
	 *            ， value
	 * @return
	 */
	public void add(String key, Item value) {
		if (key == null)
			return;
		if (value == null)
			return;

		String json = "";
		try {
			json = JsonUtils.toJson(value, Item.class);
			client.add(key, json);
		} catch (Exception e) {
			LOG.error("ERROR add key: " + key);
			LOG.error("ERROR add toJson", e);
			return;
		}

	}
	/**
	 * 在oscache中重置item 注意：
	 * 
	 * @param key
	 *            ， value
	 * @return
	 */
	public void set(String key, Item value) {
		if (key == null)
			return;
		if (value == null)
			return;

		String json = "";
		try {
			json = JsonUtils.toJson(value, Item.class);
			client.set(key, json);
		} catch (Exception e) {
			LOG.error("ERROR set key: " + key);
			LOG.error("ERROR set toJson", e);
			return;
		}

	}
	/**
	 * 在oscache中删除item 注意：
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
	protected Item get(String key) {
		if (key == null)
			return null;
		Item value = null;
		String jsonSource = "";
		try {
			jsonSource = client.getValue(key);
			value = JsonUtils.fromJson(jsonSource, Item.class);
		} catch (Exception e) {
			LOG.error("ERROR get: " + key);
			LOG.error("ERROR get", e);
			return null;
		}
		return value;
	}

	/**
	 * 根据url从搜索中获取统计获取频道
	 * 
	 * @param url
	 *            ，文章的网址
	 * @return Item，文章类
	 * 
	 */
	public static String getChannelFromSearch(String requestUrl) {
		if (requestUrl == null) {
			return null;
		}
		URL url = null;
		String instruction = "http://search.ifeng.com/so/rawsearch?q=url:"
				+ requestUrl;
		try {
			url = new URL(instruction);
		} catch (MalformedURLException e) {
			LOG.error("ERROR getFronSearch", e);
		}
		BufferedReader r = null;
		String channel = "";
		try {
			// 读取url
			URLConnection con = url.openConnection();
			r = new BufferedReader(new InputStreamReader(con.getInputStream(),
					"UTF-8"));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = r.readLine()) != null) {
				sb.append(line).append("\n");
			}
			if (sb.toString() == null) {
				return null;
			}

			Pattern pattern = null;
			Matcher matcher = null;
			try {
				// 从搜索获取频道信息
				String textSrc = "";
				pattern = Pattern
						.compile(".*<Doc_Channel><\\!\\[CDATA\\[(.+)\\]\\]></Doc_Channel>.*");
				matcher = pattern.matcher(sb.toString());
				if (matcher.find()) {
					textSrc = matcher.group(1);
					if (textSrc != null) {
						channel = textSrc;
					}
				}
			} catch (Exception e1) {
				LOG.error("ERROR getChannelFromSearch", e1);
			}
			r.close();
		} catch (Exception e1) {
			LOG.error("ERROR getChannelFromSearch", e1);
		}
		return channel;
	}

	/**
	 * 根据url从搜索端口获取一个item
	 * <p>
	 * 从搜索端口获取信息，再封装成Item返回
	 * </p>
	 * 
	 * @param url
	 *            ，文章的网址
	 * @return Item，文章类
	 * 
	 */
	public static Item getItemFromSearch(String requestUrl) {
		if (requestUrl == null || requestUrl.isEmpty())
			return null;
		Item item = null;
		URL url = null;
		String instruction = fieldDicts.itemQueryFromSearch + requestUrl;
		try {
			url = new URL(instruction);
		} catch (MalformedURLException e) {
			LOG.error("ERROR getFronSearch", e);
		}
		BufferedReader r = null;
		item = new Item();

		try {
			// 读取url
			URLConnection con = url.openConnection();
			r = new BufferedReader(new InputStreamReader(con.getInputStream(),
					"UTF-8"));
			String line = null;
			StringBuffer sb = new StringBuffer();

			while ((line = r.readLine()) != null) {

				sb.append(line).append("\n");
			}
			String xmlStr = sb.toString();

			if (sb.toString() == null) {
				return null;
			}

			Pattern pattern = null;
			Matcher matcher = null;
			try {
				String textSrc = "";

				item.setUrl(requestUrl);

				// 获取作者
				pattern = Pattern
						.compile("<Doc_Author><\\!\\[CDATA\\[(.+)\\]\\]></Doc_Author>");
				matcher = pattern.matcher(xmlStr);
				if (matcher.find()) {
					textSrc = matcher.group(1);
					if (textSrc == null)
						textSrc = "";
					item.setAuthor(textSrc);
				}

				// 获取标题
				pattern = Pattern
						.compile("<Doc_Title><\\!\\[CDATA\\[(.+?)\\]\\]></Doc_Title>");
				matcher = pattern.matcher(xmlStr);
				if (matcher.find()) {
					textSrc = matcher.group(1);
					if (textSrc == null)
						textSrc = "";
					String titleSplited = new String(SplitWordClient
							.split(textSrc.replace("•", ""), LOG)
							.replace("(/", "_").replace(") ", " "));
					item.setTitle(titleSplited);
				}
				// 获取内容
				pattern = Pattern
						.compile("<Doc_Content><\\!\\[CDATA\\[([\\s\\S]+?)\\]\\]></Doc_Content>");
				matcher = pattern.matcher(xmlStr);
				if (matcher.find()) {
					textSrc = matcher.group(1);
					if (textSrc == null || textSrc.isEmpty())
						textSrc = "";
					StringBuffer contentSB = new StringBuffer();
					String subContent = null;
					// 获取分页的文字内容
					if (textSrc.startsWith("<div>")) {
						int end = 0;
						int start = 0;
						while ((start = textSrc.indexOf("alt=\"", end)) > 0) {
							if ((end = textSrc.indexOf("\"", start + 5)) > 0) {
								String tempContent = textSrc.substring(
										start + 5, end);
								// 如果当前分页内容与上一页重复，则跳过
								if (tempContent.equals(subContent)) {
									continue;
								}
								subContent = tempContent;
								contentSB.append(subContent).append("\n");
							}
						}
					} else {
						String textTrans = textSrc.replaceAll("<.*?>", "");
						contentSB.append(textTrans);
					}

					String contentSplited = new String(SplitWordClient
							.split(contentSB.toString().replace("•", ""), LOG)
							.replace("(/", "_").replace(") ", " "));
					item.setContent(contentSplited);
				}

				// 获取日期
				pattern = Pattern
						.compile("<Doc_Date><\\!\\[CDATA\\[(.+)\\]\\]></Doc_Date>");
				matcher = pattern.matcher(xmlStr);
				if (matcher.find()) {
					textSrc = matcher.group(1);
					if (textSrc == null)
						textSrc = " ";
					item.setDate(textSrc);
				}

				// 获取关键词
				HashMap<String, String> keywordMap = new HashMap<String, String>();
				pattern = Pattern
						.compile("<Doc_Keywords><\\!\\[CDATA\\[(.+)\\]\\]></Doc_Keywords>");
				matcher = pattern.matcher(xmlStr);
				if (matcher.find()) {
					textSrc = matcher.group(1);
					if (textSrc != null) {
						String keywordsSplited[] = textSrc.split("[,\\s，]");
						StringBuffer tmp_sb = new StringBuffer();
						for (String word : keywordsSplited) {
							if (word.equals("凤凰") || word.equals("凤凰新媒体"))
								continue;
							else {
								tmp_sb.append(word).append(" ");
								keywordMap.put(word, " ");
							}
						}
						// 对关键词进行分词
						String keywordSplited = new String(SplitWordClient
								.split(tmp_sb.toString(), LOG)
								.replace("(/", "_").replace(") ", " "));
						for (String str : keywordSplited.split(" ")) {
							if (!str.isEmpty()) {
								String strSplits[] = str.split("_");
								if (!strSplits[0].isEmpty()
										&& strSplits.length > 1)
									keywordMap.put(strSplits[0], strSplits[1]);
							}
						}
						// 对分完词的关键词进行排重处理
						StringBuffer keywordBuffer = new StringBuffer();
						for (Entry<String, String> entry : keywordMap
								.entrySet())
							if (entry.getValue().equals(" "))
								keywordBuffer.append(entry.getKey())
										.append(" ");
							else
								keywordBuffer.append(entry.getKey())
										.append("_").append(entry.getValue())
										.append(" ");
						item.setKeywords(keywordBuffer.toString());

					}
				}

				// 获取id
				pattern = Pattern
						.compile("<Doc_Documentid><\\!\\[CDATA\\[(.+)\\]\\]></Doc_Documentid>");
				matcher = pattern.matcher(xmlStr);
				if (matcher.find()) {
					textSrc = matcher.group(1);
					if (textSrc == null)
						textSrc = "";
					item.setID(textSrc);
				}

				// 获取频道
				pattern = Pattern
						.compile("<Doc_Channel><\\!\\[CDATA\\[(.+)\\]\\]></Doc_Channel>");
				matcher = pattern.matcher(xmlStr);
				if (matcher.find()) {
					textSrc = matcher.group(1);
					if (textSrc == null)
						textSrc = "";
					item.setChannel(textSrc);
				}

			} catch (Exception e1) {
				LOG.error("ERROR url: " + requestUrl);
				LOG.error("ERROR getFronSearch", e1);
			}

			r.close();
		} catch (Exception e1) {
			LOG.error("ERROR getFronSearch", e1);
		}
		return item;
	}

	/**
	 * 清空oscache 注意：
	 * 
	 * @param
	 * @return
	 */
	public void removeAll() {
		// instance.client.
	}

}
