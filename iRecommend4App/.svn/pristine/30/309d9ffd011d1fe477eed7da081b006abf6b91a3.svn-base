/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.preloadUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * <PRE>
 * 作用 : 
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
 *          1.0          2015年12月8日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class HttpRequestUtil {
	private static Log LOG = LogFactory.getLog("HttpRequestUtil");
	/**
	 * 下载获得网页的html代码，超时次数最多为三次
	 * 
	 * 
	 * 
	 * 
	 * @param url：网页地址
	 *            
	 * @return String 页面html代码
	 */

	public static String downloadPageByRetry(String url,String codeType,int times){
		String content =null;
		int i = 0;
		while (i<times){
			try{
//				content = downloadPage(url,codeType);
				content = getHtmlStr(url, codeType);
				if(content != null){
					return content;
				}else{
					i++;
////temp 如果请求超时或者请求错误，这个线程就睡100ms
//Thread.sleep(100);
//LOG.info("Just sleep while ~");
					LOG.info(url+" 重试次数 : "+i);	
				}
			}catch(Exception e){				
				i++;
//temp 如果请求超时或者请求错误，这个线程就睡100ms
//try {
//	Thread.sleep(100);
//} catch (InterruptedException e1) {
//	// TODO Auto-generated catch block
//	
//}
				continue;
			}		
		}
		return content;
	}
	
	public static String downloadPage(String urlstr,String codeType) {
		URL url;
		HttpURLConnection conn = null;
		InputStream in = null;
		BufferedReader br = null;
		String content = null;
		try {
			url = new URL(urlstr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(1 * 2000);
			conn.setReadTimeout(1 * 2000);
			conn.addRequestProperty("User-Agent",
					"	Mozilla/5.0 (Windows NT 5.1; rv:7.0.1) Gecko/20100101 Firefox/7.0.1");
			conn.addRequestProperty("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.addRequestProperty("Cache-Control", "max-age=0");// Cache-Control:
			in = conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(in, codeType));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
//				line = URLDecoder.decode(line,"utf-8");
//            	line = line.replaceAll("},", "}\r\n");
				sb.append(line).append("\r\n");
			}
			content = sb.toString();
		} catch (MalformedURLException e) {
			LOG.error(" ", e);
		} catch (IOException e) {
			LOG.error(" ", e);
		} catch(Exception e){
			LOG.error(" ", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.error(" ", e);
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					LOG.error(" ", e);
				}
			}
		}
		return content;
	}
	
	private static String getHtmlStr(String url,String codeType){
		String content = null;
		
		RequestConfig defaultRecuestConfig = RequestConfig.custom().setConnectTimeout(2000).setConnectionRequestTimeout(2000).build();
		
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRecuestConfig).build();
		HttpGet httpget=null;
		try {
			URL neturl = new URL(url);
			neturl.getPort();
			URI uri = new URI(neturl.getProtocol(), null, neturl.getHost(), neturl.getPort(), neturl.getPath(), neturl.getQuery(), null);
			httpget = new HttpGet(uri);
			CloseableHttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity,"UTF-8");
			httpget.releaseConnection();
			response.close();
			httpclient.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			LOG.error(" ", e);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			LOG.error(" ", e);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			LOG.error(" ", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error(" ", e);
		}catch(Exception e){
			LOG.error(" ", e);
		}finally {
			try {
				if(httpget != null){
					httpget.releaseConnection();
				}
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error(" ", e);
			}
		}
		
		return content;
	}
	
	
}
