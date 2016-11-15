package com.ifeng.iRecommend.zhangxc.userlog.phonePrice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * <PRE>
 * 作用 : 
 *   post文档到solr中
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
 *          1.0          2014-6-4        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class DocumentPost {
	private static Logger logger = Logger.getLogger(DocumentPost.class);
		
	
	public static void postXml(String xmlFile, URL url,String type) {
		HttpURLConnection urlc = null;
		try {
			urlc = (HttpURLConnection) prepareURLCon(url,type);

			urlc.setChunkedStreamingMode(1024000);
			OutputStream out = urlc.getOutputStream();
			OutputStreamWriter outputStrm = new OutputStreamWriter(out, "UTF-8");
			outputStrm.write(xmlFile);
			outputStrm.flush();
			outputStrm.close();

			InputStream in = null;
			try {
				if (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
					if (urlc.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
							|| urlc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
						System.out.println("post failed:");
					}
					logger.error("error json "+xmlFile);
					logger.error("post returned an error :"
							+ " ErrorCode=" + urlc.getResponseCode());
				} 

				in = urlc.getInputStream();
				//pipe(in, System.out);
			} catch (IOException e) {
				logger.error("IOException while reading response:"
						+ " Exception=" + e);
				e.printStackTrace();
			} catch (Exception e) {
				logger.error("Exception while reading response: word="
						+ " Exception=" + e);
				e.printStackTrace();
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException x) {
				}
			}
		} catch (Exception e) {
			logger.error("Post Data unknown Exception:"+ e);
		} finally {
			if (urlc != null) {
				urlc.disconnect();
			}
		}
	} 
	
	public static HttpURLConnection prepareURLCon(URL url,String type) throws IOException {
		HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
		try {
			urlc.setRequestMethod("POST");
		} catch (ProtocolException e) {
			logger.error(e.getMessage());
		}
		urlc.setDoOutput(true);
		urlc.setDoInput(true);
		urlc.setUseCaches(false);
		urlc.setAllowUserInteraction(false);
		if(type.equals("json")){
			urlc.setRequestProperty("Content-type", "text/json");
		}else if(type.equals("app")){
			urlc.setRequestProperty("Proxy-Connection", "Keep-Alive");
		}else{
			urlc.setRequestProperty("Content-type", "text/xml");
		}
		
		return urlc;
	}
	
	public static String sendPostMsg(String url, String postData, String type) {
		String data = "";

		try {
			// URL
			URL dataUrl = new URL(url);

			// 建立连接
			HttpURLConnection con = (HttpURLConnection) dataUrl.openConnection();

			// 超时处理
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);

			// 发送消息的类型
			con.setRequestMethod("POST");
			if (type.equalsIgnoreCase("jsp")) {
				con.setRequestProperty("Proxy-Connection", "Keep-Alive");
			} else if (type.equalsIgnoreCase("struts")) {
				con.setRequestProperty("content-type", "text/xml");
			}
			// 连接可以收发信息
			con.setDoOutput(true);
			con.setDoInput(true);

			// 开启输出流
			OutputStream os = con.getOutputStream();

			if (os != null) {
				DataOutputStream dos = new DataOutputStream(os);
				dos.write(postData.getBytes("utf-8"));
				dos.flush();
				// 关闭输出流
				dos.close();
				os.close();
			}

			// 开启输入流
			InputStream is = con.getInputStream();
			DataInputStream dis = new DataInputStream(is);
			byte d[] = new byte[dis.available()];
			dis.read(d);
			// 获取输出的内容
			data = new String(d, "utf-8");
			con.disconnect();
			// 关闭输入流
			is.close();
			dis.close();
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		} catch (MalformedURLException e) {
			return e.getMessage();
		} catch (Exception e) {
			logger.error("send error!"+postData);
			return e.getMessage();
		}
		return data;
	}
	
}
