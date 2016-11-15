/**
 * 
 */
package com.ifeng.iRecommend.usermodel;

import com.ifeng.commen.Utils.HttpRequest;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.lidm.hbase.*;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.dingjw.dataCollection.XMLQueryInterface;
import com.ifeng.iRecommend.featureEngineering.XMLitemf;
import com.ifeng.iRecommend.featureEngineering.itemf;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

import ikvdb.client.IkvdbClient;
import ikvdb.client.IkvdbClientConfig;
import ikvdb.client.IkvdbClientFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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
 *   根据输入的cmpp id，查询得到其imcp id等具体信息,当然后续可以扩展，提供title、content等
 *   
 * 使用 : 
 *    根据输入的cmpp id，查询得到其真实title或者url，然后根据url\title，再查询ikv中imcp id等更多具体信息；
 * 示例 :
 *   
 * 注意 :
 * 	 cmppid通过转换，已经可以从ikv中得到具体features表达；但是旧的内容体系，不能用这个表达还，所以需要从ikv中得到imcp id，然后再用itemoperation类，得到旧系统的表达；
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015-09-22        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class queryCmppItem {

	private static HashMap<String,String> hm_cmpp2imcp = new HashMap<String,String>();
	
	private static queryCmppItem instance = new queryCmppItem();
	
	//根据传入的channel，找到cmpp的查询接口
	private static HashMap<String,String> hm_channel2url  = new HashMap<String,String>(){
		{
			put("ent", "http://ient.ifeng.com/cmppid/data.json"); 
		}
	};
	
	
	private queryCmppItem() {
		
	}

	public static queryCmppItem getInstance(){
		return instance;
	}
	

	/**
	 * 根据输入的cmpp id，查询得到其imcp id等具体信息
	 * 
	 * @param cmppid
	 * @param channel 因为不同的channel，接口地址不一样，晕。。。
	 * @return imcpid
	 */
	public String getImcpID(String cmppid,String channel) {
		if (cmppid == null || cmppid.isEmpty() || channel == null)
			return null;
		String imcpID = hm_cmpp2imcp.get(cmppid);
		if(imcpID != null)
			return imcpID;
	
		//调用cmpp接口得到title或者url，然后调用ikv接口得到imcpid
		String cmpp_url = hm_channel2url.get(channel).replaceAll("cmppid", cmppid);
		String res = HttpRequest.sendGet(cmpp_url, "");
		//提取title、url
		String title = "",url = "";
		if(res != null && !res.isEmpty()){
			if(res.indexOf("\"type\":\"article\"")>0)
			{
				int b = res.indexOf("title\":\"") + 8;
				if(b > 8){
					int e = res.indexOf("\",",b);
					if(e > b)
						title = res.substring(b, e);
				}
				//url
				b = res.indexOf("url\":\"") + 6;
				if(b > 6){
					int e = res.indexOf("\",",b);
					if(e > b)
						url = res.substring(b, e);
				}
			}
			if(res.indexOf("\"type\":\"slide\"")>0)
			{
				int pre = res.indexOf("\"id\":"+cmppid);
				if(pre > 0)
				{
					int b = res.indexOf("title\":\"",pre) + 8;
					if(b > 8){
						int e = res.indexOf("\",",b);
						if(e > b)
							title = res.substring(b, e);
					}
					//url
					pre = res.indexOf("i_summary",pre);
					if(pre > 0){
						b = res.indexOf("url\":\"",pre) + 6;
						if(b > 6){
							int e = res.indexOf("\",",b);
							if(e > b)
								url = res.substring(b, e);
						}
					}
				}
			}
			
		}
		
		XMLitemf xif = null;
		
		if(url != null)
		{
			 xif = XMLQueryInterface.getInstance().queryItemF(url);
		}
		
		if(xif == null && title != null){
			 xif = XMLQueryInterface.getInstance().queryItemF(title);
		
		}
		
		if(xif != null){
			hm_cmpp2imcp.put(cmppid, xif.getID());
			return xif.getID();
		}
		
		return null;
	}

	
	/**
	 * 根据输入的cmpp id，查询得到其item的特征表达信息
	 * 
	 * @param cmppid
	 * @param channel 因为不同的channel，接口地址不一样，晕。。。
	 * @return imcpid
	 */
	public ArrayList<String> getExFeatures(String cmppid,String channel) {
		if (cmppid == null || cmppid.isEmpty() || channel == null)
			return null;
		String imcpid = getImcpID(cmppid,channel);

		if(imcpid != null)
		{
			  return XMLQueryInterface.getInstance().queryExFeature(imcpid);
		}

		return null;
	}
	
	/**
	 * 根据输入的cmpp id，查询得到其item等具体信息，数据形式是xmlItemF
	 * 
	 * @param cmppid
	 * @param channel 因为不同的channel，接口地址不一样，晕。。。
	 * @return imcpid
	 */
	private XMLitemf getItemF(String cmppid,String channel) {
		if (cmppid == null || cmppid.isEmpty() || channel == null)
			return null;
//		XMLitemf xif = hm_cmpp2item.get(cmppid);
//		if(xif != null)
//			return xif;
//	
//		//调用cmpp接口得到title或者url，然后调用ikv接口得到imcpid
//		String cmpp_url = hm_channel2url.get(channel).replaceAll("cmppid", cmppid);
//		String res = HttpRequest.sendGet(cmpp_url, "");
//		//提取title、url
//		String title = "",url = "";
//		if(res != null && !res.isEmpty()){
//			if(res.indexOf("\"type\":\"article\"")>0)
//			{
//				int b = res.indexOf("title\":\"") + 8;
//				if(b > 8){
//					int e = res.indexOf("\",",b);
//					if(e > b)
//						title = res.substring(b, e);
//				}
//				//url
//				b = res.indexOf("url\":\"") + 6;
//				if(b > 6){
//					int e = res.indexOf("\",",b);
//					if(e > b)
//						url = res.substring(b, e);
//				}
//			}
//			if(res.indexOf("\"type\":\"slide\"")>0)
//			{
//				int pre = res.indexOf("\"id\":"+cmppid);
//				if(pre > 0)
//				{
//					int b = res.indexOf("title\":\"",pre) + 8;
//					if(b > 8){
//						int e = res.indexOf("\",",b);
//						if(e > b)
//							title = res.substring(b, e);
//					}
//					//url
//					pre = res.indexOf("i_summary",pre);
//					if(pre > 0){
//						b = res.indexOf("url\":\"",pre) + 6;
//						if(b > 6){
//							int e = res.indexOf("\",",b);
//							if(e > b)
//								url = res.substring(b, e);
//						}
//					}
//				}
//			}
//			
//		}
//		
//		if(url != null)
//		{
//			 xif = XMLQueryInterface.getInstance().queryItemF(url);
//		}
//		
//		if(xif == null && title != null){
//			 xif = XMLQueryInterface.getInstance().queryItemF(title);
//		
//		}
//		
//		if(xif != null){
//			hm_cmpp2item.put(cmppid, xif);
//			return xif;
//		}
//		
		return null;
	
	}
	
	
}
