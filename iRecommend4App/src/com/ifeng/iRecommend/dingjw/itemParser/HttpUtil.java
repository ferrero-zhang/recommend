/**
 * 
 */
package com.ifeng.iRecommend.dingjw.itemParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;





/**
 * @author zhouxiaocao
 *
 * 2013-2-4
 */
public class HttpUtil {
	/**
	 * 目前仅适合发送给存储搜索服务使用
	 * **/
	public static String doPostWithTextHtmlType(String url,String postData,int connectionTimeout,int readTimeOut) throws IOException
	{
		DataInputStream inputStream=null;
		HttpURLConnection con =null;
		DataOutputStream outputStream=null;
		try 
		{	
			URL dataUrl = new URL(url);
			con = (HttpURLConnection) dataUrl.openConnection();
			con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeOut);
			con.setRequestMethod("POST");
			if(url.contains("do.action")){
				con.setRequestProperty("content-type", "text/html"); 
			}else{
				con.setRequestProperty("content-type", "text/xml");
			}
			con.setDoOutput(true);
			con.setDoInput(true);
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
				//throw new UgcAuditorException(e.getMessage(),e);
				e.printStackTrace();
			}
		}
		try{
			inputStream=new DataInputStream(con.getInputStream());
			byte[] byteBuffer=new byte[256];
			int b=inputStream.read();
			int index=0;
			int round=2;
			byteBuffer[index]=(byte)b;
			index++;
			while(b!=-1){
				if(index%256==0){
					byte[] newb=new byte[round*256];
					System.arraycopy(byteBuffer, 0, newb, 0, byteBuffer.length);
					byteBuffer=newb;
					round++;
				}
				byteBuffer[index]=(byte)b;
				index++;
				b=inputStream.read();
			}
			byte[] result=new byte[index];
			System.arraycopy(byteBuffer, 0, result, 0, index);
			String s= new String(result,"UTF-8");
			return s;
		}catch (IOException e){
			throw e;
		}finally{
			try{
				if(inputStream!=null)
					inputStream.close();
			}catch (Exception e){
				//throw new UgcAuditorException(e.getMessage(),e);
				e.printStackTrace();
			}
		}
	}
	public static String doPostDefault(String url,String postData,int connectionTimeout,int readTimeOut) throws IOException
	{
		DataInputStream inputStream=null;
		HttpURLConnection con =null;
		DataOutputStream outputStream=null;
		try 
		{	
			URL dataUrl = new URL(url);
			con = (HttpURLConnection) dataUrl.openConnection();
			con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeOut);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
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
				//throw new UgcAuditorException(e.getMessage(),e);
				e.printStackTrace();
			}
		}
		try{
			inputStream=new DataInputStream(con.getInputStream());
			byte[] byteBuffer=new byte[256];
			int b=inputStream.read();
			int index=0;
			int round=2;
			byteBuffer[index]=(byte)b;
			index++;
			while(b!=-1){
				if(index%256==0){
					byte[] newb=new byte[round*256];
					System.arraycopy(byteBuffer, 0, newb, 0, byteBuffer.length);
					byteBuffer=newb;
					round++;
				}
				byteBuffer[index]=(byte)b;
				index++;
				b=inputStream.read();
			}
			byte[] result=new byte[index];
			System.arraycopy(byteBuffer, 0, result, 0, index);
			String s= new String(result,"UTF-8");
			return s;
		}catch (IOException e){
			throw e;
		}finally{
			try{
				if(inputStream!=null)
					inputStream.close();
/*				if(con!=null){
					con.disconnect();
				}*/
			}catch (Exception e){
				//throw new UgcAuditorException(e.getMessage(),e);
				e.printStackTrace();
			}
		}
	}
	public static String doGet(String url,int connectionTimeout,int readTimeOut) throws IOException
	{
		DataInputStream inputStream=null;
		HttpURLConnection con =null;
		try 
		{	
			URL dataUrl = new URL(url);
			con = (HttpURLConnection) dataUrl.openConnection();
			con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeOut);
			con.setDoOutput(true);
			con.setDoInput(true);
			//long len=Long.valueOf(con.getHeaderField("Content-length"));
			inputStream=new DataInputStream(con.getInputStream());
			byte[] byteBuffer=new byte[256];
			int b=inputStream.read();
			int index=0;
			int round=2;
			byteBuffer[index]=(byte)b;
			index++;
			while(b!=-1){
				if(index%256==0){
					byte[] newb=new byte[round*256];
					System.arraycopy(byteBuffer, 0, newb, 0, byteBuffer.length);
					byteBuffer=newb;
					round++;
				}
				byteBuffer[index]=(byte)b;
				index++;
				b=inputStream.read();
			}
			byte[] result=new byte[index];
			System.arraycopy(byteBuffer, 0, result, 0, index);
			String s= new String(result,"UTF-8");
			return s;
		}catch (IOException e){
			throw e;
		}finally{
			try{
			if(inputStream!=null)
				inputStream.close();
			}catch (Exception e){
				//throw new UgcAuditorException(e.getMessage(),e);
				e.printStackTrace();
			}
		}
	}
}
