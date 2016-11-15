package com.ifeng.iRecommend.zhangxc.userlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import org.apache.log4j.Logger;

import com.ifeng.commen.Utils.HttpRequest;


public class HttpRequestHostUtil {
    
    private static final Logger logger = Logger.getLogger(HttpRequestHostUtil.class);
    
    
    private static final int DEFAULT_TIMEOUT = 4000;  //默认httpclient请求的超时时间

	/**
	 * 根据绑定去查询绑定地址的url html,HttpUrlConnection调用方式
	 * 
	 * @param url
	 *            查询的url
	 * @throws IOException
	 */
	public static String getResponseText(String queryUrl) {
		return getResponseText(queryUrl, null, null);
	}
	/**
	 * 根据绑定去查询绑定地址的url html,HttpUrlConnection调用方式
	 * 
	 * @param url
	 *            查询的url
	 * @param host
	 *            需要绑定的host
	 * @param ip
	 *            对应host绑定的ip
	 * @throws IOException
	 */
	public static String getResponseText(String queryUrl,String host,String ip) {
		InputStream is = null;
		BufferedReader br = null;
		StringBuffer res = new StringBuffer();
		try {
			HttpURLConnection httpUrlConn = null;
			URL url = new URL(queryUrl);
			if(ip!=null){
			    String str[] = ip.split("\\.");
			    byte[] b =new byte[str.length];
			    for(int i=0,len=str.length;i<len;i++){
			        b[i] = (byte)(Integer.parseInt(str[i],10));
			    }
	            Proxy proxy = new Proxy(Proxy.Type.HTTP,
	            new InetSocketAddress(InetAddress.getByAddress(b), 80));
	            httpUrlConn = (HttpURLConnection) url
                .openConnection(proxy);
			}else{
	            httpUrlConn = (HttpURLConnection) url
	                    .openConnection();
			}
			httpUrlConn.setRequestMethod("GET");
			httpUrlConn.setDoOutput(true);
			httpUrlConn.setConnectTimeout(DEFAULT_TIMEOUT);
			httpUrlConn.setReadTimeout(DEFAULT_TIMEOUT);
			httpUrlConn.setDefaultUseCaches(false);
			httpUrlConn.setUseCaches(false);

			is = httpUrlConn.getInputStream();

			int responseCode = httpUrlConn.getResponseCode();
			// 如果返回的结果是400以上，那么就说明出问题了
			if (responseCode > 400) {
				logger.error("getResponseText for queryurl:" + queryUrl + " got responseCode :" + responseCode);
				return "getResponseText for queryurl:" + queryUrl + " got responseCode :" + responseCode;
			}
			// 需要自动识别页面的编码，通过从context-type中解析得到，默认为UTF-8
			String encoding = "UTF-8";
			String contextType = httpUrlConn.getContentType();
			if (!contextType.trim().equals("")) {
				int pos = contextType.lastIndexOf("=");
				if (pos > -1) {
					encoding = contextType.substring(pos + 1);
				}
			}
			// System.out.println(encoding);

			br = new BufferedReader(new InputStreamReader(is, encoding));

			String data = null;
			while ((data = br.readLine()) != null) {
				res.append(data + "\n");
			}
			return res.toString();

		} catch (IOException e) {
            logger.error(e.getMessage(), e);
            return "failed: " + e.getMessage();
		} catch (Exception e) {
		    logger.error(e.getMessage(), e);
		    return "failed: " + e.getMessage();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
		            logger.error(e.getMessage(), e);
		            res.append(e.getMessage());
				}
			}
		}
	}


	public static void main(String[] args){
	//   String ct = HttpRequestHostUtil.getResponseText("http://10.90.7.57:8080/ExploredRec/AddBfData?user_id=123&docIds=123,234,345",
	   // 		"local.toutiao-irecommend.ifeng.com", "10.32.25.115");
		String ct = HttpRequest.sendGet("http://10.90.7.57:8080/ExploredRec/AddBfData", "user_id=123&docIds=123,234,345");
	    System.out.println(ct);
	}
}
