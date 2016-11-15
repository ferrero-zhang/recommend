package com.ifeng.iRecommend.featureEngineering.DetectExtrmSim;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ifeng.commen.Utils.JsonUtils;

import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

/**
 * Created by zhangmeng1 on 2015/7/1.
 */
public class HBaseRest {
	private static String baseUrl = "http://10.90.4.196:20550";
	private static final Logger logger = Logger.getLogger("HBaseRest");
	private static final String tableName = "article";
	private static final String prefix = "cf:item";

	/*
	 * public static List getTables(){ Map<String, String> header = new
	 * HashMap<String, String>(); header.put("Content-Type",
	 * "application/json"); header.put("Accept", "application/json"); List
	 * tableList = new ArrayList<String>(); try { String result =
	 * HttpURL.get(baseUrl, header); Map map = (Map) JSON.parse(result);
	 * List<Map> list = ( List<Map>) map.get("table"); for(Map m:list){ String
	 * tableName = (String) m.get("name");
	 * if(!tableName.equals(MisRecoverDataService.tableName)){
	 * tableList.add(tableName); } }
	 * 
	 * } catch (IOException e) { e.printStackTrace(); } return tableList; }
	 */
	/**
	 * 从hbase读取数据
	 * 
	 * @param tableName
	 * @param rowKey
	 * @param prefix
	 * @return
	 */
	public static itemf getOne(String rowKey, String timestamp) {
		String path = baseUrl + "/" + tableName + "/" + rowKey + "/" + prefix;
		BASE64Decoder base64Decoder = new BASE64Decoder();

		Map response = null;
		HttpURLConnection urlc = null;
		InputStream in = null;
		urlc = (HttpURLConnection) prepareURLCon("GET", path);
		if (urlc == null) {
			return null;
		}
		urlc.setRequestProperty("accept", "application/json");
		urlc.setRequestProperty("connection", "Keep-Alive");
		urlc.setConnectTimeout(10000);
		urlc.setReadTimeout(10000);
		try {
			// urlc.connect();
			if (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
				if (urlc.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
						|| urlc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					logger.error("Get failed:");
				}
				else if(urlc.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND){
					logger.info("[HBASE] ID " + rowKey + " No items in hbase");
					return null;
				}
				else
					logger.error("hbase returned an error :" + " ErrorCode="
						+ urlc.getResponseCode());
			}
			in = urlc.getInputStream();
			Gson gson = new Gson();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
//			String re = reader.readLine();
//			System.out.println(re);
//			response = gson.fromJson(re, Map.class);
			response = gson.fromJson(reader, Map.class);
			// pipe(in, System.out);
		} catch (IOException e) {
			
			logger.error("[QUERY] IOException while reading response:"
					+ " Exception=" + e);
		} catch (Exception e) {
			logger.error("[QUERY] Exception while reading response: word="
					+ " Exception=" + e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch (IOException x) {
				logger.error("[QUERY] IOException while shutting the http connection"
						+ x);
			}
		}

		if (response == null || response.isEmpty())
			return null;

		try {
			
			List<Map> row = (List<Map>) response.get("Row");
			ArrayList<itemf> itemList = new ArrayList<itemf>(row.size());
			for (Map cells : row) {
				String key = (String) cells.get("key");
				List<Map> columns = (List<Map>) cells.get("Cell");
				for (Map column : columns) {
//					String name = (String) column.get("column");
					if(timestamp != null){
						double newestTimestamp = (double) column.get("timestamp");
						double para_timestamp = Double.valueOf(timestamp);
						para_timestamp /= 1000;
						if(para_timestamp > newestTimestamp)
							return null;
					}
					String value = (String) column.get("$");
					byte[] b = base64Decoder.decodeBuffer(value);
					// System.out.println(value);
					String json = new String(b, "utf-8");
					itemf item = JsonUtils.fromJson(json, itemf.class);
					if(item != null)
						itemList.add(item);
					return item;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * added itemf data to hbase
	 * 
	 * @param tableName
	 * @param rowKey
	 * @param columnName
	 * @param string
	 */
	public static void put(itemf item) {
		if(item == null){
			logger.info("[HBase] ITEM IS NULL!");
			return;
		}
		if(item.getID() == null){
			logger.info("[HBase] ITEM ID IS NULL!");
			return;
		}
		String rowKey = item.getID();
		String itemJson = JsonUtils.toJson(item);
		BASE64Encoder base64Encoder = new BASE64Encoder();
		String path = baseUrl + "/" + tableName + "/" + rowKey;
		Map<String, String> cells = new LinkedHashMap<String, String>();
		// System.out.println(base64Encoder.encode(columnName.getBytes()));
		cells.put("column", base64Encoder.encode(prefix.getBytes()));
		cells.put("$", base64Encoder.encode(org.apache.hadoop.hbase.util.Bytes
				.toBytes(itemJson)));
		List<Map> cellList = new LinkedList<Map>();
		cellList.add(cells);
		Map<String, Object> row = new LinkedHashMap<String, Object>();
		row.put("key", base64Encoder.encode(rowKey.getBytes()));
		row.put("Cell", cellList);
		List<Map> rowList = new LinkedList<Map>();
		rowList.add(row);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Row", rowList);
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String body = gson.toJson(map);

		// System.out.println("body:" + body);
		postXml(body, path, "PUT");
		// gson.fromJson(result, Map.class);
		// System.out.println(result);
	}

	/**
	 * Post the xml document stream as a string through the http connection
	 * 
	 * @param xmlFile
	 * @param url
	 * @return the response xml document after posting
	 */
	private static void postXml(String xmlFile, String path, String method) {

		HttpURLConnection urlc = null;
		Map response = null;
		urlc = (HttpURLConnection) prepareURLCon(method, path);

		if (urlc == null){
			logger.error("[HBase] Http connection fails to establish!");
			return;
		}
		urlc.setRequestProperty("Content-type", "application/json");
		urlc.setRequestProperty("Accept", "application/json");
		urlc.setChunkedStreamingMode(1024000);
		InputStream in = null;
		try {
			OutputStream out = urlc.getOutputStream();
			OutputStreamWriter outputStrm = new OutputStreamWriter(out, "UTF-8");
			outputStrm.write(xmlFile);
			outputStrm.flush();
			outputStrm.close();

			if (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
				if (urlc.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
						|| urlc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					logger.error("post failed:");
				}
				logger.error("hbase returned an error :" + " ErrorCode="
						+ urlc.getResponseCode());
			}

			in = urlc.getInputStream();
			Gson gson = new Gson();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			// System.out.println(reader.readLine());
			response = gson.fromJson(reader, Map.class);
			// pipe(in, System.out);
		} catch (IOException e) {
			logger.error("[Add to hbase] IOException while reading response:"
					+ " Exception=" + e);
		} catch (Exception e) {
			logger.error("Exception while reading response: word="
					+ " Exception=" + e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch (IOException x) {
			}
		}
	}

	/**
	 * Initialize the http connection to hbase index
	 * 
	 * @param requestMethod
	 * @return http connection
	 */
	private static HttpURLConnection prepareURLCon(String requestMethod, String path) {
		HttpURLConnection urlc = null;
		try {
			URL url = new URL(path);
			urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestMethod(requestMethod);
		} catch (ProtocolException e) {
			logger.error("Method cannot be reset or POST method is invaild!!"
					+ e);
		} catch (IOException e) {
			logger.error("Connection cannot be estabilished!" + e);
		}
		urlc.setDoOutput(true);
		urlc.setDoInput(true);
		urlc.setUseCaches(false);
		urlc.setAllowUserInteraction(false);
		return urlc;
	}
	
	public static void deleteOne(String rowKey) {
		String path = baseUrl + "/" + tableName + "/" + rowKey + "/" + prefix;

//		Map response = null;
		HttpURLConnection urlc = null;
		InputStream in = null;
		urlc = (HttpURLConnection) prepareURLCon("DELETE", path);
		if (urlc == null) {
			return;
		}
		urlc.setRequestProperty("accept", "application/json");
		urlc.setRequestProperty("connection", "Keep-Alive");
		urlc.setConnectTimeout(10000);
		urlc.setReadTimeout(10000);
		try {
			// urlc.connect();
			if (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
				if (urlc.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
						|| urlc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					logger.error("Get failed:");
				}
				logger.error("hbase returned an error :" + " ErrorCode="
						+ urlc.getResponseCode());
			}
//			in = urlc.getInputStream();
//			Gson gson = new Gson();
//			BufferedReader reader = new BufferedReader(
//					new InputStreamReader(in));
			// System.out.println(reader.readLine());
//			gson.fromJson(reader, Map.class);
			// pipe(in, System.out);
		} catch (IOException e) {
			logger.error("[DELETE] IOException while reading response:"
					+ " Exception=" + e);
		} catch (Exception e) {
			logger.error("[DELETE] Exception while reading response: word="
					+ " Exception=" + e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch (IOException x) {
				logger.error("[DELETE] IOException while shutting the http connection"
						+ x);
			}
		}
	}
	
	/**
	 * 传入系列key，返回所有匹配的结果
	 * @param rowKey
	 * @return
	 */
	public static List<itemf> getPatch(String rowKey) {
		String path = baseUrl + "/" + tableName + "/" + rowKey + "/" + prefix;
		BASE64Decoder base64Decoder = new BASE64Decoder();

		Map response = null;
		HttpURLConnection urlc = null;
		InputStream in = null;
		urlc = (HttpURLConnection) prepareURLCon("GET", path);
		if (urlc == null) {
			return null;
		}
		urlc.setRequestProperty("accept", "application/json");
		urlc.setRequestProperty("connection", "Keep-Alive");
		urlc.setConnectTimeout(10000);
		urlc.setReadTimeout(10000);
		try {
			// urlc.connect();
			if (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
				if (urlc.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
						|| urlc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					logger.error("Get failed:");
				}
				else if(urlc.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND){
					logger.info("[HBASE] ID " + rowKey + " No items in hbase");
					return null;
				}
				else
					logger.error("hbase returned an error :" + " ErrorCode="
						+ urlc.getResponseCode());
			}
			in = urlc.getInputStream();
			Gson gson = new Gson();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
//			String re = reader.readLine();
//			System.out.println(re);
//			response = gson.fromJson(re, Map.class);
			response = gson.fromJson(reader, Map.class);
			// pipe(in, System.out);
		} catch (IOException e) {
			logger.error("[QUERY] IOException while reading response:"
					+ " Exception=" + e);
		} catch (Exception e) {
			logger.error("[QUERY] Exception while reading response: word="
					+ " Exception=" + e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch (IOException x) {
				logger.error("[QUERY] IOException while shutting the http connection"
						+ x);
			}
		}

		if (response == null || response.isEmpty())
			return null;

		try {
			List<Map> row = (List<Map>) response.get("Row");
			List<itemf> itemList = new ArrayList<itemf>(row.size());
			for (Map cells : row) {
				String key = (String) cells.get("key");
				List<Map> columns = (List<Map>) cells.get("Cell");
				for (Map column : columns) {
					String name = (String) column.get("column");
					String value = (String) column.get("$");
					byte[] b = base64Decoder.decodeBuffer(value);
					// System.out.println(value);
					String json = new String(b, "utf-8");
					itemf item = JsonUtils.fromJson(json, itemf.class);
					if(item != null)
						itemList.add(item);
				}
			}
			return itemList;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		// put("article", "rowKey", "cf:cname", 12); //cf 是列名前缀，所有列名都要带
		// int i = getOne("article", "rowKey", "cf:cname");
		// System.out.println(i);
//		IndexOperation SolrOb = IndexOperation.getInstance();
//		int range = 100;
//		for (int j = 2500185; j <= 3000000; j += range) {
//			// String[] ids = {"474162", "634766"/*,"996430","1027395","1315516","358187","303817","309746","359212","306581", "356189", "359399","356820"*/};
//			List<itemf> itemList = SolrOb.queryByIds(j,
//					Math.min(j + range - 1, 3000000));
//			int retry = 3;// 超时重试
//			while (itemList == null && retry-- > 0) {
//				// log.info("RETRY!! " + retry);
//				itemList = SolrOb.queryByIds(j,
//						Math.min(j + range - 1, 3000000));
//			}
//			if (itemList == null)
//				continue;
//			// List<itemf> items = hbaseOb.queryByIds(ids);
//			for (int i = 0; i < itemList.size(); i++) {
//				HBaseRest.put(itemList.get(i));
//			}
//			if(j % 5000 == 0)
//				System.out.println(j);
//		}
//		IKVOperationv2 ikvop = new IKVOperationv2("appitemdb"); 
//		String key = "4426115";
//		itemf item = ikvop.queryItemF(key, "c");
//		HBaseRest.put(item);
//		 List<itemf> items = HBaseRest.getPatch("44900?");
//		 System.out.println(items.size());
//		 = items.get(0);
//		 HBaseRest.deleteOne("60060999");
		itemf item = HBaseRest.getOne("83671", "1471508335");
		System.out.println(item.getSplitContent());
		System.out.println(item.getSplitTitle());
		System.out.println(item.getTitle());
		System.out.println(item.getFeatures());
		System.out.println(item.getSource());
		System.out.println(item.getOther());
		System.out.println(item.getID());
	}
}
