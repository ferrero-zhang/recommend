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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.ifeng.commen.oscache.OSCache;
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
 *          1.0          2015年12月8日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class AlgorithmPreloadKeyUtil{
	private static Log LOG = LogFactory.getLog("AlgorithmPreloadKeyUtil");
	private static AlgorithmPreloadKeyUtil instance = new AlgorithmPreloadKeyUtil();
	private static final int Length = 100;
	private static List<KeyWord> keyWordList ;
	private static Map<String, KeyWord> keyWordMap;
	private static ExecutorService pool;

	//从可用表中筛选出地域tag
	private static List<KeyWord> locKeyWordList;
	
	private AlgorithmPreloadKeyUtil() {
		// TODO Auto-generated constructor stub
		keyWordList = new ArrayList<KeyWord>();
		keyWordMap = new HashMap<String, KeyWord>();
		locKeyWordList = new ArrayList<KeyWord>();
		pool = Executors.newFixedThreadPool(30);
	}
	
	public static AlgorithmPreloadKeyUtil getInstance(){
		return instance;
	}
	
	private void clear(){
		keyWordList.clear();
		keyWordMap.clear();
		locKeyWordList.clear();
	}
	
	/**
	 * 从redis中获取可用表中可用词
	 * 
	 * 
	 * @param String path
	 * 
	 * @return List<KeyWord>
	 * 
	 */
	public void loadingPreloadChannelKeySet(){
		
		List<KeyWord> keyWordList = new ArrayList<KeyWord>();
		Map<String,KeyWord> keyWordMap = new HashMap<String, KeyWord>();
		List<KeyWord> locKeyWordList = new ArrayList<KeyWord>();
		
		//科技->互联网|移动互联网|IT业界|通信业|数码|探索发现|互联网金融
		//test
		KeyWord word = new KeyWord();
		word.setName("军事");
		word.setType("c");
		word.setAvailable(true);
		keyWordList.add(word);
		keyWordMap.put(word.getName(), word);
//////		
		KeyWord word1 = new KeyWord();
		word1.setName("军事图赏");
		word1.setType("et");
		word1.setAvailable(true);
		keyWordList.add(word1);
		keyWordMap.put(word1.getName(), word1);
//		
//		KeyWord word2 = new KeyWord();
//		word2.setName("奇闻轶事");
//		word2.setType("sc");
//		word2.setAvailable(true);
//		keyWordList.add(word2);
//		keyWordMap.put(word2.getName(), word2);
//		
//		KeyWord word3 = new KeyWord();
//		word3.setName("移动互联网");
//		word3.setType("sc");
//		word3.setAvailable(true);
//		keyWordList.add(word3);
//		keyWordMap.put(word3.getName(), word3);
//		
//		KeyWord word4 = new KeyWord();
//		word4.setName("IT业界");
//		word4.setType("sc");
//		word4.setAvailable(true);
//		keyWordList.add(word4);
//		keyWordMap.put(word4.getName(), word4);
//		
//		KeyWord word5 = new KeyWord();
//		word5.setName("通信业");
//		word5.setType("sc");
//		word5.setAvailable(true);
//		keyWordList.add(word5);
//		keyWordMap.put(word5.getName(), word5);
//		
//		KeyWord word6 = new KeyWord();
//		word6.setName("数码");
//		word6.setType("sc");
//		word6.setAvailable(true);
//		keyWordList.add(word6);
//		keyWordMap.put(word6.getName(), word6);
//		
//		KeyWord word7 = new KeyWord();
//		word7.setName("创业");
//		word7.setType("sc");
//		word7.setAvailable(true);
//		keyWordList.add(word7);
//		keyWordMap.put(word7.getName(), word7);
//		
//		KeyWord word8 = new KeyWord();
//		word8.setName("探索发现");
//		word8.setType("sc");
//		word8.setAvailable(true);
//		keyWordList.add(word8);
//		keyWordMap.put(word8.getName(), word8);
//		
//		KeyWord word9 = new KeyWord();
//		word9.setName("互联网金融");
//		word9.setType("sc");
//		word9.setAvailable(true);
//		keyWordList.add(word9);
//		keyWordMap.put(word9.getName(), word9);
		
//		Gson gson = new Gson();
//		Jedis jedis = new Jedis("10.32.24.194", 6379, 10000);
//		jedis.select(1);
//		Set<String> keyset = jedis.keys("*");
//		LOG.info("Read keySet size : "+keyset.size());
//		Iterator<String> it = keyset.iterator();
//		while(it.hasNext()){
//			
//			String key = it.next();
//			String json = jedis.get(key);
//			if(json == null){
//				LOG.error("this "+key+" value is null ~");
//				continue;
//			}
//			try{
//				KeyWord keyword = gson.fromJson(json, KeyWord.class);
//				//城市tag直接预加载列表
//				if(keyword.getType().equals("loc")){
//					locKeyWordList.add(keyword);
//					keyWordList.add(keyword);
//					keyWordMap.put(keyword.getName(), keyword);
//					continue;
//				}
//				if(keyword.isAvailable()){
//					keyWordList.add(keyword);
//					keyWordMap.put(keyword.getName(), keyword);
//				}
//			}catch(Exception e){
//				LOG.error(key+" "+e);
//				continue;
//			}
//		}
		LOG.info("Available keySet size : "+keyWordList.size());
		LOG.info("Loc city keySet size : "+locKeyWordList.size());
		this.locKeyWordList = locKeyWordList;
		this.keyWordList = keyWordList;
		this.keyWordMap = keyWordMap;
	}
	
	public synchronized List<KeyWord> getKeyWordsList(){
		if(keyWordList == null || keyWordList.isEmpty()){
			LOG.warn(Thread.currentThread().getName()+" keyWordList is Empty ~");
			return null;
		}
		List<KeyWord> returnlist = new ArrayList<KeyWord>();
		if(keyWordList.size() < Length){
			returnlist.addAll(keyWordList.subList(0, keyWordList.size()));
		} else {
			returnlist.addAll(keyWordList.subList(0, Length));
		}
		//将返回的keyWord从keywordlist中删除
		keyWordList.removeAll(returnlist);
		LOG.info(Thread.currentThread().getName()+" get keywordlist success ~ remain : "+keyWordList.size());
		return returnlist;
	}
	
	public List<KeyWord> getAllKeyWordList(){
		return keyWordList;
	}
	
	public synchronized void addKeyWordsToList(KeyWord keyword){
		if(keyword == null || keyword.getName() == null){
			return ;
		}
		//如果添加的keyword已经在列表中则不替换,不在列表中的词才添加
		if(!keyWordMap.containsKey(keyword.getName())){
			keyWordList.add(keyword);
			keyWordMap.put(keyword.getName(), keyword);
		}
	}
	
	public KeyWord queryKeywordByName(String name){
		if(name == null){
			return null;
		}
		KeyWord keyword = keyWordMap.get(name);
		return keyword;
	}
	
	public List<KeyWord> getLocKeyWordList(){
		return locKeyWordList;
	}
	
	public static void main(String[] args){
		AlgorithmPreloadKeyUtil.getInstance().loadingPreloadChannelKeySet_MThread();
//		System.out.println(AlgorithmPreloadKeyUtil.getInstance().getLocKeyWordList());
	}

	/*
	 * 以下为多线程LoadingKeyWord代码
	 * 
	 * 
	 */
	
	public synchronized void addKeyWordsToListBylist(List<KeyWord> wordList){
		if(wordList == null || wordList.isEmpty()){
			return ;
		}
		//如果添加的keyword已经在列表中则不替换,不在列表中的词才添加
		for(KeyWord word : wordList){
			if(word.getType().equals("loc")){
				locKeyWordList.add(word);
			}
			keyWordList.add(word);
			keyWordMap.put(word.getName(), word);
		}
	}
	
	public void loadingPreloadChannelKeySet_MThread(){
		//先清除历史数据
		this.clear();
		Jedis jedis = new Jedis("10.32.24.194", 6379, 10000);
		jedis.select(1);
		Set<String> keyset = jedis.keys("*");
		LOG.info("Read keySet size : "+keyset.size());
		List<List<String>> splitkeylist = splitSet(keyset, 1000);
		LOG.info("SplitKeyList size : "+splitkeylist.size());
		List<Future> futureList = new ArrayList<Future>();
		for(List<String> keyList : splitkeylist){
			MThreadPreloadKeyUtil load = new MThreadPreloadKeyUtil(keyList);
			Future f = pool.submit(load);
			futureList.add(f);
		}
		for(Future f : futureList){
			try {
				f.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				LOG.equals(e);
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				LOG.equals(e);
			}
		}
		LOG.info("Available keySet size : "+keyWordList.size());
		LOG.info("Loc city keySet size : "+locKeyWordList.size());	
	}
	
	
	
	private List<List<String>> splitSet(Set<String> tempSet , int spliteSize){
		List<List<String>> splitList = new ArrayList<List<String>>();
		if(tempSet == null || tempSet.isEmpty()){
			return splitList;
		}
		int index = 0;
		List<String> tempList = new ArrayList<String>();
		for(String key : tempSet){
			tempList.add(key);
			index++;
			if(index >= spliteSize){
				splitList.add(tempList);
				index = 0;
				tempList = new ArrayList<String>();
			}
		}
		if(!tempList.isEmpty()){
			splitList.add(tempList);
		}
		return splitList;
	}
	
	
}
