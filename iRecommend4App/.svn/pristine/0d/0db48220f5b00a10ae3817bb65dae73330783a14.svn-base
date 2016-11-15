package com.ifeng.iRecommend.zhanzh.LifeTimePreidct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.ifeng.iRecommend.featureEngineering.itemf;
import com.ifeng.iRecommend.zhanzh.newHotPredict.HeatPredictUtils;
import com.ifeng.iRecommend.zhanzh.newHotPredict.HotRankItem;

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
public class NewsLifeTimeDataCollector {
	private static Log LOG = LogFactory.getLog("NewsLifeTimeCollector");
	public static final String localPath = "D:\\test\\lifeTimeFile\\";
//	public static final String localPath = "/data/zhanzh/LifeTimePredict/lifeTimeFile/";
	public static void updateNewsLifeTime(){
		HashMap<String, HashMap<String, Long>> lifeTimeMap = getNewsLifeTimeRT();
		if(lifeTimeMap == null || lifeTimeMap.isEmpty()){
			LOG.error("get lifeTimeRT error ~");
			return;
		}
		Set<String> keySet = lifeTimeMap.keySet();
		for(String key : keySet){
			HashMap<String, Long> tempmap = loadingLocalLifeTimeMap(key);
			HashMap<String, Long> RTmap = lifeTimeMap.get(key);
			if(tempmap != null){
				LOG.info("Load "+key+" map size "+tempmap.size());
				tempmap.putAll(RTmap);
			}else{
				LOG.info(" This file not exist creat "+key);
				tempmap = RTmap;
			}
			LOG.info("Save "+key+" map size "+tempmap.size());
			saveToLocalFile(key, tempmap);
		}
	}
	
	public static void saveToLocalFile(String key,HashMap<String, Long> savemap){
		if(key==null||savemap==null||savemap.isEmpty()){
			LOG.error(key+" map is null ~");
			return;
		}
		String filepath= localPath+key;
		File file = new File(filepath);
		int i=0;
		while(file.exists()&&!file.canWrite()){
			try {
				Thread.sleep(1*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(i>=3){
				LOG.error("File cannot write "+filepath);
				return;
			}
			i++;
		}
		file.setReadable(false);
		try {
			PrintWriter pw = new PrintWriter(file);
			Set<String> keySet = savemap.keySet();
			for(String tempkey : keySet){
				Long value = savemap.get(tempkey);
				pw.print(tempkey);
				pw.print(":");
				pw.print(value);
				pw.println();
			}
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			file.setReadable(true);
		}
		
	}
	
	public static HashMap<String, Long> loadingLocalLifeTimeMap(String key){
		HashMap<String, Long> lifetimemap = new HashMap<String, Long>();
		if(key == null){
			return null;
		}
		String filepath = localPath+key;
		File file = new File(filepath);
		if(!file.exists()){
			return null;
		}
		int i=0;
		while(!file.canRead()){
			try {
				Thread.sleep(1*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(i>=3){
				LOG.error("file cannot read "+filepath);
				return null;
			}	
			i++;
		}
		file.setWritable(false);
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			while((line= br.readLine())!=null){
				String[] templist = line.split(":");
				if(templist.length==2){
					String id = templist[0];
					String time = templist[1];
					Long longtime = new Long(time);
					lifetimemap.put(id, longtime);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			file.setWritable(true);
		}
		
		return lifetimemap;
	}
	/**
	 * 获取当前实时的生命周期信息
	 * 
	 * @param 
	 * 
	 * @return key=c/sc/cn value=(key=docid,value=lifeTime)
	 * 
	 */
	public static HashMap<String, HashMap<String, Long>> getNewsLifeTimeRT(){
		//key=c/sc/cn value=(key=docid,value=lifeTime)
		HashMap<String, HashMap<String, Long>> newsLifeTimeRT = new HashMap<String, HashMap<String,Long>>();
		HeatPredictUtils util = HeatPredictUtils.getInstance();
		List<HotRankItem> nowPvList = util.parserPVLog();
		if(nowPvList == null){
			return newsLifeTimeRT;
		}
		for(HotRankItem hot : nowPvList){
			itemf item = hot.getItem();
			if(item == null){
				continue;
			}
			HashMap<String, String> featureMap = parserItemfFeature(item);
			String[] channelList = {"c","sc","cn"};
			for(String channel : channelList){
				String value = featureMap.get(channel);
				if(value != null){
					String[] list = value.split("\\|");
					for(String temp : list){
						Long time = hot.getLifeTime();
						String key = channel+"="+temp;
						HashMap<String, Long> tempMap = newsLifeTimeRT.get(key);
						if(tempMap == null){
							tempMap = new HashMap<String, Long>();
							tempMap.put(hot.getDocId(), time);
							newsLifeTimeRT.put(key, tempMap);
						}else{
							tempMap.put(hot.getDocId(), time);
						}
					}
				}
			}
		}
		return newsLifeTimeRT;
	}
	
	/**
	 * 解析itemF的feature字段，并返回itemF的特征map
	 * 
	 * @param itemF 
	 * 
	 * @return HashMap<String,String> key=c\sc\cn\et\...  value=自媒体|网球|...
	 * 
	 */
	private static HashMap<String, String> parserItemfFeature(itemf item){
		HashMap<String, String> FeatureMap = new HashMap<String, String>();
		if(item == null){
			return FeatureMap;
		}
		ArrayList<String> featurelist = item.getFeatures();
		if(featurelist == null||featurelist.isEmpty()){
			return FeatureMap;
		}
		for(int i=1;i<=featurelist.size()-2;i+=3){
			String temp = featurelist.get(i);
			String feature = FeatureMap.get(temp);
			if(feature != null){
				feature = feature+"|"+featurelist.get(i-1);
				FeatureMap.put(temp, feature);
			}else{
				FeatureMap.put(temp, featurelist.get(i-1));
			}
			
		}
		return FeatureMap;
	}
	
	public static void main(String[] args){
//		Gson gson = new Gson();
//		HashMap<String, HashMap<String, Long>> nomap = getNewsLifeTimeRT();
//		String str = gson.toJson(nomap);
//		System.out.println(str);
//		Set<String> keyset = nomap.keySet();
//		for(String key : keyset){
//			saveToLocalFile(key, nomap.get(key));
//		}
//		for(String key : keyset){
//			HashMap<String, Long> tempmap = loadingLocalLifeTimeMap(key);
//			str = gson.toJson(tempmap);
//			System.out.println(key+" : "+str);
//		}
		while(true){
			updateNewsLifeTime();
			try {
				LOG.info("sleep 3 min");
				Thread.sleep(3*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
