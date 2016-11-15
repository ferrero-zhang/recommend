/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.KeyWord;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadJobInterface;

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
 *          1.0          2016年1月29日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class PreloadJob implements PreloadJobInterface,Runnable {
	private static Log LOG = LogFactory.getLog("PreloadJob");
	private OSCache osCache ;
	private List<KeyWord> keyWordList;//需要执行的总量
	private PropertiesConfiguration configs;
	private int spliteSize;//分发给每个线程工作的任务数
	private String priority;
	
	public PreloadJob(OSCache osCache ,String priority  ,PropertiesConfiguration configs,int spliteSize){
		this.osCache = osCache;
		this.configs = configs;
		this.spliteSize = spliteSize;
		this.priority = priority;
	}
	
	//tags映射对于名称错乱的tags建立映射 以及 较粗分类建立映射关系
	
	@Override
	public void preload() {
		LOG.info(priority + " Job start ~");
		long TimeStamp = System.currentTimeMillis();
		// TODO Auto-generated method stub
		//根据优先级字符串获取对应的加载队列
		this.keyWordList = BasicDataUpdateJob.getInstance().getKeyWordsList(priority);
		
		List<Future> fList = new ArrayList<Future>();
		List<List<KeyWord>> splitKeyWordList = splitKeyWordList(keyWordList, spliteSize);
		for(List<KeyWord> wordlist : splitKeyWordList){
			AlgPreloadProcess process = new AlgPreloadProcess(osCache, wordlist, configs);
			Future f = AlgPreloadManager.getInstance().submit(process);
			fList.add(f);
		}
		
		for(Future f : fList){
			try {
				f.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				LOG.error(" ",e);
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				LOG.error(" ",e);
			}
		}
		TimeStamp = (System.currentTimeMillis() - TimeStamp)/1000;
		LOG.info(priority + " Job finished ~"+" Total Time : "+TimeStamp+"s");
		return;
	}
	
	/**
	 * 用于对数组进行切片组装
	 * 
	 * 注意：
	 * @param String s 
	 * 
	 * @return double
	 * 
	 */
	private List<List<KeyWord>> splitKeyWordList(List<KeyWord> keywordlist , int spliteSize){
		List<List<KeyWord>> splitList = new ArrayList<List<KeyWord>>();
		if(keywordlist == null || keywordlist.isEmpty()){
			return splitList;
		}
		int index = 0;
		List<KeyWord> tempList = new ArrayList<KeyWord>();
		for(KeyWord key : keywordlist){
			tempList.add(key);
			index++;
			if(index >= spliteSize){
				splitList.add(tempList);
				index = 0;
				tempList = new ArrayList<KeyWord>();
			}
		}
		if(!tempList.isEmpty()){
			splitList.add(tempList);
		}
		return splitList;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "PreloadJob";
	}

	@Override
	public void run() {
		//防止线程异常而导致线程停止
		try {
			preload();
		} catch (Exception e) {
			LOG.info(priority + "thread error ~"+e.getMessage());
		}
	}

}
