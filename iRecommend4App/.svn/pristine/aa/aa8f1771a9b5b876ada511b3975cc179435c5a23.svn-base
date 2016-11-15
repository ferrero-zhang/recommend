package com.ifeng.iRecommend.zxc.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;




public class HttpUtils {
	public static int getResponseCode(String urlstr,String proxyIp,String proxyPort) throws IOException{
		if(urlstr==null||urlstr.isEmpty()){
			return 0;
		}
		Proxy proxy=null;
		if(proxyIp!=null&&proxyPort!=null){
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getByName(proxyIp), Integer.valueOf(proxyPort)));
		}
		URL url = new URL( urlstr);
		HttpURLConnection conn = null;
		if(proxy!=null){
			conn = (HttpURLConnection) url.openConnection (proxy);
		}else{
			conn = (HttpURLConnection) url.openConnection ();
		}
        conn.setUseCaches(false);
        conn.setConnectTimeout(18000);
        conn.setReadTimeout(18000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36");
    	conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");//www.people.com的cdn缓存 varnish 会直接忽略no-cache,只关心max-age
    	conn.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
//    	conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
//        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:7.0.1) Gecko/20100101 Firefox/7.0.1");
//        conn.setRequestProperty("Cache-Control", "max-age=0");//www.people.com的cdn缓存 varnish 会直接忽略no-cache,只关心max-age
//        conn.setRequestProperty("Pragma", "no-cache");
//        conn.setRequestProperty("Accept-Encoding","gzip, deflate, sdch");
       // conn.setRequestProperty("Cookie", "SINAGLOBAL=1427364123519.51.1442820161863; UOR=www.baidu.com,open.weibo.com,www.sogou.com; TC-Page-G0=07e0932d682fda4e14f38fbcb20fac81; login_sid_t=59cc5477e5558a49d2a471de7d9aba58; TC-Ugrow-G0=370f21725a3b0b57d0baaf8dd6f16a18; _s_tentry=-; Apache=6932674492709.339.1444364597288; ULV=1444364597307:3:1:1:6932674492709.339.1444364597288:1443492453558; SUS=SID-3941349956-1444364610-XD-hwflt-09bd2eb435eb9157490afaa2cc5a730f; SUE=es%3D9938cd0695b74832dfc6d7c37a970656%26ev%3Dv1%26es2%3D53535b33cf5f4ce23ae080547d19f911%26rs0%3DmOOcW4tmhrptOYBIaZKRBEaxA93w3GOp7uW95c8eOIMNATfjlzfWgQuB91u0irdFggYe%252B7mdF6l5OagNs%252B1%252FhlxQxLQLbZjw9ulpGvnbFf08%252FzIXjG7mswomo1GdzTIPQcz5AkC5DEfhKpTrmU6kgIxMtgbRwHbuD3l5e41Asqw%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1444364610%26et%3D1444451010%26d%3Dc909%26i%3D730f%26us%3D1%26vf%3D2%26vt%3D1%26ac%3D0%26st%3D0%26uid%3D3941349956%26name%3D895045340%2540qq.com%26nick%3D%25E5%25B0%258F%25E5%2585%25B5895%26fmp%3D%26lcp%3D2015-06-09%252010%253A31%253A47; SUB=_2A257EzESDeTxGeVH71MS9CfFzjqIHXVYaSXarDV8PUNbuNAPLWXFkW9_xTN8JzDUJUAPOoylFJUHDHwdiw..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WWJdM_nzCR7C9OzSEgPvoeG5JpX5K2t; SUHB=0u5RRMFjrSvCeK; ALF=1475900610; SSOLoginState=1444364610; un=895045340@qq.com; wvr=6; TC-V5-G0=666db167df2946fecd1ccee47498a93b");  
    	conn.connect();
    	int s=conn.getResponseCode();
    	
		return s;
	}
	
	public static String doPostDefault(String url,String postData,int connectionTimeout,int readTimeOut,Map<String,String> requestProperties) throws Exception
	{
		BufferedReader in = null;
		DataInputStream inputStream=null;
		HttpURLConnection con =null;
		DataOutputStream outputStream=null;
		String result = "";
		try 
		{	
			URL dataUrl = new URL(url);
			con = (HttpURLConnection) dataUrl.openConnection();
			con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeOut);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			 for(String key:requestProperties.keySet()){
		    	   con.setRequestProperty(key, requestProperties.get(key));
		       }
		    	
			outputStream=new DataOutputStream(con.getOutputStream());
			outputStream.write(postData.getBytes("UTF-8"));
			outputStream.flush();	
		}catch (IOException e){
			throw e;
		}finally{
			try{
				if(outputStream!=null)
					outputStream.close();
			}catch (Exception e){
				throw e;
			}
		}
		try{
			in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            return result;
		}catch (IOException e){
			throw e;
		}finally{
			try{
				if(inputStream!=null)
					inputStream.close();
				
				if(in!=null){
					in.close();
				}
			}catch (Exception e){
				throw e;
			}
		}
	}
	public static String doGet(String url,int connectionTimeout,int readTimeOut,String proxyIp,String proxyPort) throws Exception
	{
		InputStream inputStream=null;
		HttpURLConnection con =null;
		
		try 
		{	
			URL dataUrl = new URL(url);
			Proxy proxy=null;
			if(proxyIp!=null&&proxyPort!=null){
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getByName(proxyIp), Integer.valueOf(proxyPort)));
			}
			
			
			if(proxy!=null){
				con = (HttpURLConnection) dataUrl.openConnection (proxy);
			}else{
				con = (HttpURLConnection) dataUrl.openConnection ();
			}
			 con.setUseCaches(false);
	        con.setConnectTimeout(18000);
	        con.setReadTimeout(18000);
	        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36");
	    	con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");//www.people.com的cdn缓存 varnish 会直接忽略no-cache,只关心max-age
	    	con.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
	    	
	    	con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeOut);
			con.setDoOutput(true);
			con.setDoInput(true);
			String coding=con.getContentEncoding();
			if(coding!=null&&coding.equals("gzip")){
				 inputStream = new GZIPInputStream(con.getInputStream());
			}else{
				inputStream=new DataInputStream(con.getInputStream());
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));  
            String line = "";  
            StringBuffer sb=new StringBuffer();
            while((line = reader.readLine()) != null) {  
            	sb.append(line).append("\n"); 
            }  
			return sb.toString();
		}catch (IOException e){
			throw e;
		}finally{
			try{
			if(inputStream!=null)
				inputStream.close();
			}catch (Exception e){
				throw e;
			}
		}
	}
	public static String doGet(String url,int connectionTimeout,int readTimeOut,String proxyIp,String proxyPort,Map<String,String> requestProperties) throws Exception
	{
		InputStream inputStream=null;
		HttpURLConnection con =null;
		
		try 
		{	
			URL dataUrl = new URL(url);
			Proxy proxy=null;
			if(proxyIp!=null&&proxyPort!=null){
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getByName(proxyIp), Integer.valueOf(proxyPort)));
			}
			
			
			if(proxy!=null){
				con = (HttpURLConnection) dataUrl.openConnection (proxy);
			}else{
				con = (HttpURLConnection) dataUrl.openConnection ();
			}
			 con.setUseCaches(false);
	        con.setConnectTimeout(18000);
	        con.setReadTimeout(18000);
	       for(String key:requestProperties.keySet()){
	    	   con.setRequestProperty(key, requestProperties.get(key));
	       }
	    	
	    	con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeOut);
			con.setDoOutput(true);
			con.setDoInput(true);
			String coding=con.getContentEncoding();
			if(coding!=null&&coding.equals("gzip")){
				 inputStream = new GZIPInputStream(con.getInputStream());
			}else{
				inputStream=new DataInputStream(con.getInputStream());
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));  
            String line = "";  
            StringBuffer sb=new StringBuffer();
            while((line = reader.readLine()) != null) {  
            	sb.append(line).append("\n"); 
            }  
			return sb.toString();
		}catch (IOException e){
			throw e;
		}finally{
			try{
			if(inputStream!=null)
				inputStream.close();
			}catch (Exception e){
				throw e;
			}
		}
	}
}
