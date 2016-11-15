/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.preloadUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.KeyWord;

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
 *          1.0          2016年1月27日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class MThreadPreloadKeyUtil implements Runnable {
	private static Log LOG = LogFactory.getLog("MThreadPreloadKeyUtil");
	private List<String> preloadKeyList ;
	public MThreadPreloadKeyUtil(List<String> preloadKeyList) {
		// TODO Auto-generated constructor stub
		if(preloadKeyList != null){
			this.preloadKeyList = preloadKeyList;
		}else{
			this.preloadKeyList = new ArrayList<String>();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		preload();
	}
	
	public void preload(){
		List<KeyWord> keyWordList = new ArrayList<KeyWord>();
		Gson gson = new Gson();
		Jedis jedis = new Jedis("10.32.24.194", 6379, 10000);
		jedis.select(1);
		Iterator<String> it = preloadKeyList.iterator();
		while(it.hasNext()){
			
			String key = it.next();
			String json = jedis.get(key);
			if(json == null){
				LOG.error("this "+key+" value is null ~");
				continue;
			}
			try{
				KeyWord keyword = gson.fromJson(json, KeyWord.class);
				//城市tag直接预加载列表
				if(keyword.getType().equals("loc")){
					keyWordList.add(keyword);
					continue;
				}
				if(keyword.isAvailable()){
					keyWordList.add(keyword);
				}
			}catch(Exception e){
				LOG.error(key+" "+e);
				continue;
			}
		}
		AlgorithmPreloadKeyUtil.getInstance().addKeyWordsToListBylist(keyWordList);
		LOG.info("Loading Available keySet size : "+keyWordList.size());
	}

}
