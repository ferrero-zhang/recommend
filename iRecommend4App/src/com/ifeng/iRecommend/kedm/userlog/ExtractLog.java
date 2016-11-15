package com.ifeng.iRecommend.kedm.userlog;

/**
 * <PRE>
 * 作用 : 
 *   解析pc端日志、客户端日志；抽取日期、抽取时间、抽取用户浏览行为、合并用户信息等
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
 *          1.0          2013-10-25        lidm          change
 * -----------------------------------------------------------------------------
 * </PRE>
 */

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.kedm.util.LocalInfo;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation;
import com.ifeng.iRecommend.zhangxc.userlog.phonePrice.JSoupBaiduSearcher;

public class ExtractLog {
	private static final Log LOG = LogFactory.getLog("log_to_hbase");

	// 访问此url的log数据可能为爬虫，将不被保存在hbase中
	private static HashMap<String, String> channel_url_hashmap;
	static {
		channel_url_hashmap = new HashMap<String, String>();
		FileUtil fileUtil = new FileUtil();
		fileUtil.Initialize(
				LoadConfig.lookUpValueByKey("channel_url_file_path"), "utf-8");
		String line = null;
		while ((line = fileUtil.ReadLine()) != null) {
			channel_url_hashmap.put(line.trim(), "");
			LOG.info("channel url:" + line);
		}
		if (fileUtil != null)
			fileUtil.CloseRead();
	}

	/**
	 * 解析抽取pc端日志并存入哈希表中
	 * 
	 * @param fileName
	 *            日志文件
	 * @return
	 */
	public static HashMap<String, String> ExtractPCLog(String fileName) {
		long start = System.currentTimeMillis();
		// 保存所有解析后的数据
		HashMap<String, String> dataHashMap = new HashMap<String, String>();

		FileUtil logFile = new FileUtil();
		logFile.Initialize(fileName, "UTF-8");

		String rawUserData = null;// log文件中待解析的原始的一行数据
		String userID = "";// user id 字段
		String ip = "";// ip 字段
		String timeStamp = "";// 时间戳字段
		String url = "";// 访问的url字段

		String value = "";// 组合上面各字段的值

		int invalidUrlCount = 0;// 只访问channel url的行数
		int invalidUserID = 0;// user id不合法的行数
		int invalidRawDataCount = 0;// 原始数据不合法的行数

		int line_count = 0;
		while ((rawUserData = logFile.ReadLine()) != null) {
			line_count++;
			try {
				String[] rawUserDataItems = rawUserData.split("\t");

				// 获取各字段的值
				url = rawUserDataItems[0].trim();

				if (url.endsWith("/"))
					url = url.substring(0, url.length() - 1);
				if(url.contains("#p=")){
					url = url.substring(0,url.lastIndexOf("#p="));
				}

				// 访问的url非法，访问的是频道的url，作为爬虫信息排除
				if (channel_url_hashmap.containsKey(url)) {
					invalidUrlCount++;
					continue;
				}

				// 原始数据非法，字段数!=10
				if (rawUserDataItems.length < 10) {
					invalidRawDataCount++;
					continue;
				}

				ip = rawUserDataItems[2].trim();
				userID = rawUserDataItems[3].trim();
				timeStamp = rawUserDataItems[7].trim();

				// 组合各字段值
				value = url + "\t" + ip + "\t" + timeStamp;

				// 组合同一个log文件内同一个user id的所有信息
				if (dataHashMap.containsKey(userID)) {
					String value_in = dataHashMap.get(userID);
					if (!value_in.equals(value)) {
						value = value_in + "!" + value;
					}
					dataHashMap.put(userID, value);
				} else {
					if (logDBOperation.checkPcLogUserID(userID)) {
						dataHashMap.put(userID, value);
					} else {
						// user id非法
						invalidUserID++;
					}
				}
			} catch (Exception e) {
				LOG.error("check user_data_valid error:" + rawUserData, e);
				continue;
			}
		}
		LOG.info("line count:" + line_count);
		//LOG.info("invalid url:" + invalidUrlCount);
		//LOG.info("invalid userid:" + invalidUserID);
		//LOG.info("invalid rawdata:" + invalidRawDataCount);
		LOG.info("invalid line:"
				+ (invalidUrlCount + invalidRawDataCount + invalidUserID));
		LOG.info("valid data:" + dataHashMap.size());
		long end = System.currentTimeMillis();
		LOG.info("duration of extract log file:" + (end - start));
		return dataHashMap;
	}

	/**
	 * 解析抽取客户端日志并存入哈希表中
	 * 
	 * @param fileName
	 *            日志文件
	 * @return
	 */
	public static HashMap<String, String> ExtractAppLog(String fileName) {
		if (null == fileName || 0 == fileName.length()) {
			LOG.error("invalid log file dir");
			return null;
		}
		long start = System.currentTimeMillis();
		// 保存所有解析后的数据
		HashMap<String, String> dataHashMap = new HashMap<String, String>();
		Set<String> phonebrand = new HashSet<String>();
		
		FileUtil logFile = new FileUtil();
		logFile.Initialize(fileName, "UTF-8");

		String rawUserData = "";// log文件中待解析的原始的一行数据

		String userID = "";// user id 字段
		String action = "";
		String platform = "";
		String ip = "";
		String net = "";
		String time = "";
		
		String value = "";// 组合上面各字段的值

		int invalidUserID = 0;// user id不合法的行数
		int invalidAction = 0;// 操作行为不合法的行数
		int invalidRawDataCount = 0;// 原始数据不合法的行数

		int line_count = 0;
		while (rawUserData != null) {
			try{
				rawUserData = logFile.ReadLine();
				if(rawUserData==null)
					break;
			}catch(Exception e){
				LOG.error("read log file error,wait 5 seconds...",e);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				continue;
			}
			
			line_count++;
			
			if(rawUserData.trim().equals("")||rawUserData.length()<10){
				invalidRawDataCount++;
				continue;
			}
			
			try {
				String[] rawUserDataItems = rawUserData.split("\t");

				// 原始数据非法，字段数==13
				if (rawUserDataItems.length != 13) {
					invalidRawDataCount++;
					continue;
				}

				// 获取各字段的值
				// userid
				userID = rawUserDataItems[5].trim();
				//临时测试。。。
				/*Set<String> test = new HashSet<String>();
				test.add("A000004206BD05");
				test.add("865586021345899");
				test.add("865863026995352");
				test.add("9d62db556b71181a48dfd310185fe6935cf47a90");
				test.add("359938050752490");
				if(!test.contains(userID)){
					continue;
				}*/
				if (userID.length() < 2) {
					invalidUserID++;
					continue;
				}

				action = rawUserDataItems[11].trim() + "#"
						+ rawUserDataItems[12].trim();
				if (action.length() < 2) {
					invalidAction++;
					continue;
				}

				platform = GetPlatform(rawUserDataItems[2].trim(),rawUserDataItems[3].trim(),rawUserDataItems[6].trim());
				ip=rawUserDataItems[1].trim();
				net=GetNet(rawUserDataItems[7].trim());
				time = rawUserDataItems[10].trim();
				phonebrand.add(rawUserDataItems[6].trim());
				
				
				if(time.trim().equals(""))
					time = "iphone";
				// 组合各字段值
				value = time+"\t"+ip+"\t"+platform + "\t" + net+"\t"+action;

				// 组合同一个log文件内同一个user id的所有信息
				if (dataHashMap.containsKey(userID)) {
					String value_in = dataHashMap.get(userID);
					if (!value_in.equals(value)) {
						value = value_in + "!" + value;
					}
					dataHashMap.put(userID, value);
				} else {
					dataHashMap.put(userID, value);
				}
				
				/*
				if(rawUserDataItems[11].trim().equals("in")){
					Jedis jedis = null;
					try{
						jedis = new Jedis("10.90.1.58",6379,5000);
						jedis.select(6);
						if(rawUserDataItems[12].trim().contains("loc=")){
							String loc = getLoc(rawUserDataItems[12].trim());
							if(loc != null && !loc.contains("null"))
								jedis.sadd(ip, loc);
						} else {
							  //加查询似乎太慢了
							if(!jedis.exists(ip)){
								jedis.select(7);
								try{
									String num = jedis.get(ip);
									int num_int = Integer.valueOf(num);
									num_int++;
									jedis.set(ip, String.valueOf(num_int));
								} catch (Exception e){
									jedis.set(ip, "0");
								}
							}
						}
					}catch(Exception e){
						LOG.error("Redis connect error: " + e.getMessage());
						jedis.disconnect();
					}
					jedis.disconnect();
				}*/
			} catch (Exception e) {
				LOG.error("check user_data_valid error:" + rawUserData, e);
				continue;
			}
		}
		
		
		/*
		
		Jedis jedis = null;
		try{
			jedis = new Jedis("10.90.1.58",6379,5000);
			jedis.select(4);
			Set<String> brands = new HashSet<String>();
			for(String brand : phonebrand){
				if(jedis.get(brand) == null) //不存在
					brands.add(brand);				
			}
		
			jedis.select(5);
			for(String brand : brands){
				try{
					String num = jedis.get(brand);
					int num_int = Integer.valueOf(num);
					num_int++;
					jedis.set(brand, String.valueOf(num_int));
				} catch (Exception e){
					jedis.set(brand, "0");
				}
			}	
		}catch(Exception e){
			LOG.error("Connect error: " + e.getMessage());
			jedis.disconnect();
		}
		jedis.disconnect();
	*/
		
		LOG.info("line count:" + line_count);
		//LOG.info("invalid rawdata:" + invalidRawDataCount);
		//LOG.info("invalid action:" + invalidAction);
		//LOG.info("invalid userid:" + invalidUserID);
		LOG.info("invalid line:"
				+ (invalidRawDataCount + invalidAction + invalidUserID));
		long end = System.currentTimeMillis();
		LOG.info("duration of extract log file:" + (end - start));
		return dataHashMap;
	}
	

	/**
	 * 手机系统操作系统名称
	 * 
	 * w:windowsphone iph:iphone ipa:ipad a:android o:other
	 * 
	 * @param mos
	 * @return
	 */
	public static String GetPlatform(String mos,String cli_ver,String mtype) {
		
		return mos + "#" + cli_ver + "#" + mtype;
	}

	/**
	 * 获取app log中的简化网络类型值
	 * @param netStr
	 * @return
	 */
	public static String GetNet(String netStr){
		String net = netStr.toLowerCase();
		if (net.contains("wifi"))
			net = "w";
		else if (net.contains("2g"))
			net = "2";
		else if (net.contains("3g"))
			net = "3";
		else if (net.contains("4g"))
			net = "4";
		else if (net.contains("offline"))
			net = "of";
		else if (net.contains("unknown"))
			net = "u";
		else
			net = "o";
		return net;
	}
	
	public static HashMap<String, String> ExtractTest(String fileName) {
		if (null == fileName || 0 == fileName.length()) {
			LOG.error("invalid log file dir");
			return null;
		}
		long start = System.currentTimeMillis();
		// 保存所有解析后的数据
		HashMap<String, String> dataHashMap = new HashMap<String, String>();

		FileUtil logFile = new FileUtil();
		logFile.Initialize(fileName, "UTF-8");

		String rawUserData = "";// log文件中待解析的原始的一行数据

		String userID = "";// user id 字段
		String action = "";
		String platform = "";
		String ip = "";
		String net = "";
		String time = "";
		
		String value = "";// 组合上面各字段的值

		int invalidUserID = 0;// user id不合法的行数
		int invalidAction = 0;// 操作行为不合法的行数
		int invalidRawDataCount = 0;// 原始数据不合法的行数

		int line_count = 0;
		while (rawUserData != null) {
			try{
				rawUserData = logFile.ReadLine();
				if(rawUserData==null)
					break;
			}catch(Exception e){
				LOG.error("read log file error,wait 5 seconds...",e);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				continue;
			}
			
			line_count++;
			
			if(rawUserData.trim().equals("")||rawUserData.length()<10){
				invalidRawDataCount++;
				continue;
			}
			
			try {
				String[] rawUserDataItems = rawUserData.split("\t");
				
				// 原始数据非法，字段数==13
				if (rawUserDataItems.length != 13) {
					invalidRawDataCount++;
					continue;
				}

				// 获取各字段的值
				// userid
				userID = rawUserDataItems[5].trim();
				
				if (userID.length() < 2) {
					invalidUserID++;
					continue;
				}

				action = rawUserDataItems[11].trim() + "#"
						+ rawUserDataItems[12].trim();
				if (action.length() < 2) {
					invalidAction++;
					continue;
				}

				ip=rawUserDataItems[1].trim();

				// 组合同一个log文件内同一个user id的所有信息
				if (dataHashMap.containsKey(userID)) {
					String value_in = dataHashMap.get(userID);
					if (!value_in.equals(value)) {
						value = value_in + "!" + value;
					}
					dataHashMap.put(userID, value);
				} else {
					dataHashMap.put(userID, value);
				}
			} catch (Exception e) {
				LOG.error("check user_data_valid error:" + rawUserData, e);
				continue;
			}
		}
		LOG.info("line count:" + line_count);
		//LOG.info("invalid rawdata:" + invalidRawDataCount);
		//LOG.info("invalid action:" + invalidAction);
		//LOG.info("invalid userid:" + invalidUserID);
		LOG.info("invalid line:"
				+ (invalidRawDataCount + invalidAction + invalidUserID));
		long end = System.currentTimeMillis();
		LOG.info("duration of extract log file:" + (end - start));
		return dataHashMap;
	}
	
	
	public static String getLoc(String value){
		String loc = "";
		LocalInfo locinfo = null;
		int b = value.indexOf("loc=");
		if(b >= 0){
			loc = value.substring(b+4);
		}
		Gson gson = new Gson();
		if(loc.contains("%")){
			try {
				loc = URLDecoder.decode(loc, "utf-8");
				locinfo = gson.fromJson(loc, LocalInfo.class);
			} catch (Exception e) {
				return null;
			}
			
			if(locinfo != null)
				return locinfo.getState() + "_" + locinfo.getCity();
		}else if(loc.contains("{")){
			try{
				locinfo = gson.fromJson(loc, LocalInfo.class);
			} catch(Exception e){
				return null;
			}
			if(locinfo != null)
				return locinfo.getState() + "_" + locinfo.getCity();
		}		
		return null;
	}
	
	
	
	public static void main(String[] args){
		ExtractAppLog("E:\\clientLogs\\2016-10-24\\1419.sta");
		
		
			
	}
}
