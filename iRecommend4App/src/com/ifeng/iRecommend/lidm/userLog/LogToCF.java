package com.ifeng.iRecommend.lidm.userLog;

/**
 * <PRE>
 * 作用 : 
 *   将log解析后的数据写入数据库cf。
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.LoadConfig;

public class LogToCF implements LogToDB {
	private HashMap<String, String> dataHashMap;
	private String logDate;
	private static boolean dbInited = false;
	private static final Log LOG = LogFactory.getLog("log_to_hbase");
	public static DatabaseType dbType = DatabaseType.CF;

	private static int batch = 1000;
	public static String simplecfUrl = null;

	public LogToCF(HashMap<String, String> dataHashMap) {
		this.dataHashMap = dataHashMap;
	}

	@Override
	public void InitDB() {
		if (dbInited)
			return;
		dbInited = true;
		simplecfUrl = LoadConfig.lookUpValueByKey("simplecfUrl");
	}

	@Override
	public void PushLogToDB() {
		long start_push = System.currentTimeMillis();
		InitDB();
		String log_date = dataHashMap.get("log_date");
		String log_time = dataHashMap.get("log_time");
		String time = log_date + "-" + log_time;// 20131106-0910
		LOG.info("time:" + log_date + "-" + log_time);
		//dataHashMap.remove("log_date");
		//dataHashMap.remove("log_time");

		//LOG.info("count_data_hashmap:" + dataHashMap.size());
		sendUserIdAndTimeToSimpleCF(dataHashMap, time);
		long end_push = System.currentTimeMillis();
		LOG.info("duration of push data to cf:" + (end_push - start_push));
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

	public void sendUserIdAndTimeToSimpleCF(Map<String, String> map, String time) {
		int count = 0;
		StringBuffer sb = new StringBuffer();
		Set<Entry<String, String>> enset = map.entrySet();
		for (Entry<String, String> en : enset) {
			String uid = en.getKey();
			String v = en.getValue();
			count++;
			sb.append(uid).append("|").append(v).append("\n");
			if (count % batch == 0) {
				String data = sb.toString();
				try {
					data = URLEncoder.encode(data, "utf-8");
					StringBuffer sbt = new StringBuffer();
					sbt.append("data=").append(data).append("&day=" + time);
					doPostDefault(simplecfUrl, sbt.toString(), 3000, 10000);
					sb.setLength(0);
				} catch (UnsupportedEncodingException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		//LOG.info("count_send_to_cf:" + count);
		String data = sb.toString();
		if (data.length() > 0) {
			try {
				data = URLEncoder.encode(data, "utf-8");
				StringBuffer sbt = new StringBuffer();
				sbt.append("data=").append(data).append("&day=" + time);
				doPostDefault(simplecfUrl, sbt.toString(), 3000, 10000);
				sb.setLength(0);
			} catch (UnsupportedEncodingException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	public static String doPostDefault(String url, String postData,
			int connectionTimeout, int readTimeOut) {
		DataInputStream inputStream = null;
		HttpURLConnection con = null;
		DataOutputStream outputStream = null;
		try {
			URL dataUrl = new URL(url);
			con = (HttpURLConnection) dataUrl.openConnection();
			con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeOut);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			outputStream = new DataOutputStream(con.getOutputStream());
			outputStream.write(postData.getBytes("UTF-8"));
			outputStream.flush();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (outputStream != null)
					outputStream.close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		try {
			inputStream = new DataInputStream(con.getInputStream());
			byte byteArray[] = new byte[inputStream.available()];
			inputStream.read(byteArray);
			String data = new String(byteArray, "UTF-8");
			return data;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return null;
	}
}
