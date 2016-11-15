/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.LifeTimePreidct;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

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
 *          1.0          2015年7月23日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class LifeTimePredict {
	private static Log LOG = LogFactory.getLog("LifeTimePredic");
	public static HashMap<String, ArrayList<Integer>> getNewsLifeTimeList(){
		HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
		File file = new File(NewsLifeTimeDataCollector.localPath);
		File[] filelist = file.listFiles();
		for(File tempfile : filelist){
			String key = tempfile.getName();
			HashMap<String, Long> tempMap = NewsLifeTimeDataCollector.loadingLocalLifeTimeMap(key);
			if(tempMap == null || tempMap.isEmpty()){
				LOG.warn(key+" is null ");
				continue;
			}
			LOG.info(key+" size : "+tempMap.size());
			if(tempMap.size()<10){
				continue;
			}
			Set<String> keySet = tempMap.keySet();
			ArrayList<Integer> lifetimelist = new ArrayList<Integer>();
			for(String tempkey : keySet){
				Long time = tempMap.get(tempkey)/(60*60*1000);//转换为小时
				lifetimelist.add(time.intValue());
			}
			Collections.sort(lifetimelist, new Comparator<Integer>() {

				@Override
				public int compare(Integer o1, Integer o2) {
					// TODO Auto-generated method stub
					if(o1 < o2){
						return -1;
					}else{
						return 1;
					}
				}
			});
			map.put(key, lifetimelist);
		}
		return map;
	}
	
	public static HashMap<String, List<List<Integer>>> getNewsLifeTimeSplitList(){
		HashMap<String, List<List<Integer>>> map = new HashMap<String,List<List<Integer>>>();
		File file = new File(NewsLifeTimeDataCollector.localPath);
		File[] filelist = file.listFiles();
		for(File tempfile : filelist){
			String key = tempfile.getName();
			HashMap<String, Long> tempMap = NewsLifeTimeDataCollector.loadingLocalLifeTimeMap(key);
			if(tempMap == null || tempMap.isEmpty()){
				LOG.warn(key+" is null ");
				continue;
			}
			LOG.info(key+" size : "+tempMap.size());
			if(tempMap.size()<50){
				continue;
			}
			Set<String> keySet = tempMap.keySet();
			ArrayList<Integer> lifetimelist = new ArrayList<Integer>();
			for(String tempkey : keySet){
				Long time = tempMap.get(tempkey)/(60*60*1000);//转换为小时
				if(time.intValue()<8){
					continue;
				}
				lifetimelist.add(time.intValue());
			}
			Collections.sort(lifetimelist, new Comparator<Integer>() {

				@Override
				public int compare(Integer o1, Integer o2) {
					// TODO Auto-generated method stub
					if(o1 < o2){
						return -1;
					}else{
						return 1;
					}
				}
			});
			List<List<Integer>> templist = splitByStep(lifetimelist, 0.5f);
			map.put(key,templist);
		}
		return map;
	}
	
	
	private static List<List<Integer>> splitByStep(ArrayList<Integer> sortlist,float rate){
		List<List<Integer>> splitList = new ArrayList<List<Integer>>();
		if(sortlist == null || sortlist.isEmpty()){
			return null;
		}
		int index = 0;
		while(index < sortlist.size()){
			int nextindex = findNextStep(index, sortlist, rate);
			List<Integer> templist = sortlist.subList(index, nextindex+1);
			splitList.add(templist);
			index = nextindex+1;
		}
		return splitList;
	}
	
	private static int findNextStep(int begin,ArrayList<Integer> sortlist,float rate){
		int index = begin+1;
		if(index >= sortlist.size()){
			return sortlist.size()-1;
		}
		for(;index<sortlist.size();index++){
			int i = sortlist.get(index-1);
			int j = sortlist.get(index);
			
			int gap = j-i;
			if(i==0){
				i = 1;
			}
			//test 选取sortlist中位数作为除数
			int midIndex = (0+sortlist.size())/2;
			int midNum = sortlist.get(midIndex);
			
			float gaprate = (float)gap/midNum;
//			System.out.println("before : "+i+" next : "+j+" gaprate : "+gaprate);
			if(gaprate >= rate ){
				return index-1;
			}
		}
		return index-1;
	}
	
	
	
	public static HashMap<String, Integer> countNewsLifeTime(){
		HashMap<String, Integer> newsLifeTime = new HashMap<String, Integer>();
		File file = new File(NewsLifeTimeDataCollector.localPath);
		File[] filelist = file.listFiles();
		for(File tempfile : filelist){
			String key = tempfile.getName();
			HashMap<String, Long> tempMap = NewsLifeTimeDataCollector.loadingLocalLifeTimeMap(key);
			if(tempMap == null || tempMap.isEmpty()){
				LOG.warn(key+" is null ");
				continue;
			}
			LOG.info(key+" size : "+tempMap.size());
			if(tempMap.size()<30){
				continue;
			}
			int hourtime = 0;
			Set<String> keySet = tempMap.keySet();
			for(String tempkey : keySet){
				Long time = tempMap.get(tempkey)/(60*60*1000);//转换为小时
				hourtime += time.intValue();
			}
			int lifetime = hourtime/tempMap.size();
			newsLifeTime.put(key, lifetime);
		}
		return newsLifeTime;
	}
	
	public static HashMap<String, Integer> countNewsLifeTimeNew(){
		HashMap<String, Integer> newsLifeTime = new HashMap<String, Integer>();
		HashMap<String, List<List<Integer>>> lifetimesplitlist = getNewsLifeTimeSplitList();
		Set<String> keySet = lifetimesplitlist.keySet();
		for(String key : keySet){
			List<List<Integer>> lifetimelist = lifetimesplitlist.get(key);
			List<Integer> sizeMaxList = lifetimelist.get(0);
			//选取size最大的
			for(int i=0;i<lifetimelist.size();i++){
				List<Integer> tempList = lifetimelist.get(i);
				if(tempList.size()>sizeMaxList.size()){
					sizeMaxList = tempList;
				}
			}
			//计算生命周期  算法：取平均值 
			int lifeTime = 0;
			for(Integer time : sizeMaxList){
				lifeTime += time;
			}
			lifeTime = lifeTime/sizeMaxList.size();
			newsLifeTime.put(key, lifeTime);
		}
		return newsLifeTime;
	}
	
	
	public static void main(String[] args){
//		HashMap<String, Integer> lifetime = countNewsLifeTime();
//		HashMap<String, ArrayList<Integer>> lifetime = getNewsLifeTimeList();
//		HashMap<String, List<List<Integer>>> lifetime = getNewsLifeTimeSplitList();
		HashMap<String, Integer> lifetime = countNewsLifeTimeNew();
		Gson gson = new Gson();
		
		String json = gson.toJson(lifetime);
		System.out.println(json);
		
//		ArrayList<Integer> test = new ArrayList<Integer>();
//		test.add(0);
//		test.add(1);
//		test.add(2);
//		test.add(3);
//		
//		System.out.println(test.size());
//		List<Integer> temp = test.subList(0, test.size());
//		int midle = (0+test.size())/2;
//		System.out.println(midle);
//		System.out.println(test.get(midle));
//		System.out.println(temp);
		
//		String localPath = "D:\\test\\lifeTimeFile\\";
//		File file = new File(localPath);
//		File[] fillist = file.listFiles();
//		System.out.println(fillist.length);
//		for(File f : fillist){
//			System.out.println(f.getName());
//		}
	}
}
