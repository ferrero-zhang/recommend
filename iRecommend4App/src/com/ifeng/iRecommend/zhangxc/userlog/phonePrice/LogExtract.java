package com.ifeng.iRecommend.zhangxc.userlog.phonePrice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.iRecommend.zhangxc.userlog.phonePrice.RedisUtil;


import net.sf.json.JSONObject;

/**
 * <PRE>
 * 作用 : 
 *   解析第三方日志类
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
 *          1.0          2016年8月19日        张雪纯                  create
 * -----------------------------------------------------------------------------
 * </PRE>
 */


public class LogExtract {
	private static final Log LOG = LogFactory.getLog(LogExtract.class);
	
	
	public static void  convertAndReplacer(String inputfile, String outputfile){
		String readfilename = inputfile;
		String writefilename = outputfile;
		BufferedReader br;
		BufferedWriter bw;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(readfilename)));
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writefilename, true)));
			int count = 0;
			int cont = 0;
		    for(String data = br.readLine(); data != null; data = br.readLine()){	 
		        try{	
		            String line = replacer(new StringBuffer(convert(data)));  
		            if(cont!=0)  bw.newLine();
		        	bw.append(line);		        	
		        	cont++;
		        } catch (Exception ex){
		        	count++;
		        	continue;
		        }	 
		    }
		    bw.flush();
		    LOG.info("There are "+ count + " damaged messages");	 
		    bw.close();
		    br.close();
		} catch (IOException e) {
			LOG.error(e);
		} 	
	}
	
	public static void filtDiffUser(String inputfile, String outputfile, String channel){
		
		String readfilename = inputfile;
		String writefilename = outputfile;
		BufferedReader br;
		BufferedWriter bw;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(readfilename)));
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writefilename, true)));
			int count = 0;
			int cont = 0;
		    for(String data = br.readLine(); data != null; data = br.readLine()){	        
		        JSONObject jsonObject = JSONObject.fromObject(data);
		        //JSONObject jsonOpenObj = jsonObject.getJSONObject("open");
		        try{	
		            if(jsonObject.getString("channel").equals(channel)){
		        	    String line = replacer(new StringBuffer(convert(data)));  
		        	    if(cont!=0)  bw.newLine();
		        	    bw.append(line);
		        	    cont++;
		            }
		        } catch (Exception ex){
		        	count++;
		        	//LOG.error(ex);
		        	continue;
		        }	 
		    }
		    bw.flush();
		    LOG.info("There are "+ count + " damaged messages");	 
		    bw.close();
		    br.close();
		} catch (IOException e) {
			LOG.error(e);
		} 	
	}
	
	
	public static String replacer(StringBuffer outBuffer) {
	      String data = outBuffer.toString();
	      try {
	         data = data.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
	         data = data.replaceAll("\\+", "%2B");
	         data = URLDecoder.decode(data, "utf-8");
	      } catch (Exception e) {
	    	  LOG.error("Replace error: ",e);
	      }
	      return data;
	}
	
	public static String convert(String utfString){  
	    StringBuilder sb = new StringBuilder();  
	    int i = -1;  
	    int pos = 0;  
	      
	    while((i=utfString.indexOf("\\u", pos)) != -1){  
	        sb.append(utfString.substring(pos, i));  
	        if(i+5 < utfString.length()){  
	            pos = i+6;  
	            sb.append((char)Integer.parseInt(utfString.substring(i+2, i+6), 16));  
	        }  
	    }  
	    sb.append(utfString.substring(pos));  
	    return sb.toString();  
	}  

	
	
	public static void main(String[] args) {		
		String readfilename = "E:\\log\\data.txt";
		String writefilename = "E:\\log\\qq_Converted.txt";
		
		//convertAndReplacer(readfilename, writefilename);
		filtDiffUser(readfilename, writefilename, "qq");		
	}

}
