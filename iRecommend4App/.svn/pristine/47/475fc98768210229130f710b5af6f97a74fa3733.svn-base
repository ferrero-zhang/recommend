/**
 * 
 */
package com.ifeng.iRecommend.dingjw.dataCollection;

import ikvdb.client.IkvdbClient;
import ikvdb.client.IkvdbClientConfig;
import ikvdb.client.IkvdbClientFactory;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 *          1.0          2014年8月5日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class GetLurlFromIKV {
	private static final Log LOG = LogFactory.getLog("VisitIkv");
	private IkvdbClient<String, String> client;
	private static final String IKV_TABLE_NAME = "ir_slurls";

	public GetLurlFromIKV() {
		

		// 初始IKV
		IkvdbClientConfig config = new IkvdbClientConfig();
		// 设置服务器地址（启动路径），可以设置多个，保证至少有一个能连接上 
		String[] urls = new String[] { "tcp://10.32.25.30:6666",
				"tcp://10.32.25.36:6666","tcp://10.32.25.40:6666",
				"tcp://10.32.25.50:6666", };
		config.setBootstrapUrls(Arrays.asList(urls));
		IkvdbClientFactory factory = new IkvdbClientFactory(config);
		client = factory.getClient(IKV_TABLE_NAME);
		LOG.info(IKV_TABLE_NAME + " connected ....");
	}

/*	public void add(String key, String value) {
		if (key == null) {
			return;
		}
		if (value == null) {
			return;
		}
		try {
			client.add(key, value);
			//LOG.info("save key : " + key + " value : " + value + " success ");
		} catch (Exception e) {
			LOG.error("IKV add ERROR get : " + key + " value : " + value);
			LOG.error("ERROR get ", e);
			return;
		}

	}*/

	public String get(String key) {

		if (key == null)
			return null;
		if (key.contains("shtml")) {
			key = key.split("shtml")[0] + "shtml";
		}
		String value = null;
		try{
			value = client.getValue(key);
		}catch(Exception e){
			LOG.warn("Connect IKV ERROR !!!!", e);
		}
		return value;
	}
}
