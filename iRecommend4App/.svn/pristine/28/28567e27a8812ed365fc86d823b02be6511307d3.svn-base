/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.KeyWord;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PVItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadJobInterface;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.AlgorithmPreloadKeyUtil;

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
 *          1.0          2016年2月1日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class BasicDataUpdateJob implements PreloadJobInterface,Runnable{
	private static Log LOG = LogFactory.getLog("BasicDataUpdateJob");
	
	private static BasicDataUpdateJob instance = new BasicDataUpdateJob();
	
	private PropertiesConfiguration configs;
	
	public static final String highPriority = "highPriority";
	public static final String lowPriority = "lowPriority";
	public static final String local = "local";
	
	//tags映射对于名称错乱的tags建立映射 以及 较粗分类建立映射关系
	private static HashMap<String, List<String>> tagsMap = new HashMap<String, List<String>>();
	
	private static ConcurrentHashMap<String, List<KeyWord>> keywordsMap ;
	//高优先级
	private static List<KeyWord> highPriorityList ;
	//低优先级
	private static List<KeyWord> lowPriorityList ;
	//地域城市列表List
	private static List<KeyWord> locList;
	
	private BasicDataUpdateJob() {
		// TODO Auto-generated constructor stub
		//配置文件初始化
		try {
			configs = new PropertiesConfiguration();
			configs.setEncoding("UTF-8");
			configs.load("conf/AlgorithmChannelPreload.properties");
			configs.setReloadingStrategy(new FileChangedReloadingStrategy());
			LOG.info("Loading config success ~");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.error("Load properties errror " + e);
		}
		
		highPriorityList = new ArrayList<KeyWord>();
		lowPriorityList = new ArrayList<KeyWord>();
		locList = new ArrayList<KeyWord>();
		keywordsMap = new ConcurrentHashMap<String, List<KeyWord>>();
		keywordsMap.put(highPriority, highPriorityList);
		keywordsMap.put(lowPriority, lowPriorityList);
		keywordsMap.put(local, locList);
	}
	
	public static BasicDataUpdateJob getInstance(){
		return instance;
	}
	
	@Override
	public void preload() {
		LOG.info("BasicDateUpdate start  ~");
		
		// TODO Auto-generated method stub
		AlgorithmPreloadKeyUtil keyUtil = AlgorithmPreloadKeyUtil.getInstance();
		//先更新数据
		keyUtil.loadingPreloadChannelKeySet_MThread();
		//test
//		keyUtil.loadingPreloadChannelKeySet();
		//加载配置日志，获取映射tag
		HashMap<String, List<String>> tempTagsMap = loadingConfigurationTagsMap();
		
		
		HashMap<String,Map<String, Integer>> pvMap = updatePVMap();
		
		List<KeyWord> high = new ArrayList<KeyWord>();
		List<KeyWord> low = new ArrayList<KeyWord>();
		List<KeyWord> loc = new ArrayList<KeyWord>();
		List<KeyWord> allKeyWordsList = keyUtil.getAllKeyWordList();
		LOG.info("Loading All avaliable key size : "+allKeyWordsList.size());

		//tagsMap获取完毕，并将tags添加至预加载队列中
		tagsMap = tempTagsMap;
		addMapTagsToKeywordList();
		
		for(KeyWord keyword : allKeyWordsList){
			
			//对包含字母的关键词进行tag映射，统一映射到全大写的tag下
//			String name = keyword.getName();
//			if(name.matches(".*?[a-zA-Z]+.*?")){
//				String upperName = name.toUpperCase();
//				List<String> tempList = tempTagsMap.get(upperName);
//				if(tempList != null){
//					tempList.add(name);
//				}else{
//					tempList = new ArrayList<String>();
//					tempList.add(name);
//					tempTagsMap.put(upperName, tempList);
//				}
//			}
			
			//先把一级分类和最近有点击的频道、本地频道加入优先队列
			if(keyword.getType().equals("c")){
				high.add(keyword);
				continue;
			}
			//本地
			if(keyword.getType().equals("loc")){
				high.add(keyword);
				loc.add(keyword);
				continue;
			}
			//有点击
			if(pvMap.containsKey(keyword.getName())){
				high.add(keyword);
				continue;
			}
			
			//test
//			if(keyword.getName().equals("白银市")){
//				high.add(keyword);
//				continue;
//			}
			
			low.add(keyword);
		}
		

		
		
		
		highPriorityList = high;
		lowPriorityList = low;
		locList = loc;

		
		
		keywordsMap.put(highPriority, highPriorityList);
		keywordsMap.put(lowPriority, lowPriorityList);
		keywordsMap.put(local, locList);
		LOG.info("BasicDateUpdate finish : highPriorityList size : "+highPriorityList.size()+" lowPriorityList size : "+lowPriorityList.size()+" tagsMap size : "+tagsMap.size());
		LOG.info("Loc list size : "+locList.size());
	}
	
	public static void main(String[] args){
		BasicDataUpdateJob.getInstance().preload();
		
		HashMap<String, List<String>> tempMap = BasicDataUpdateJob.getInstance().getConfigTagsMap();
		Gson gson = new Gson();
		String out = gson.toJson(tempMap);
		System.out.println(out);
	}
	
	public synchronized List<KeyWord> getKeyWordsList(String key){
		List<KeyWord> temp = keywordsMap.get(key);
		return temp;
	}
	
	private synchronized void setKeyWordsMap(HashMap<String, List<KeyWord>> tempMap){
		keywordsMap.clear();
		keywordsMap.putAll(tempMap);
	}
	
	public synchronized List<KeyWord> getHighPriorityList(){
		return highPriorityList;
	}
	
	public synchronized List<KeyWord> getLowPriorityList(){
		return lowPriorityList;
	}
	
	/**
	 * 获取PV实时点击数据
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private HashMap<String,Map<String, Integer>> updatePVMap(){
		HashMap<String,Map<String, Integer>> pvItemMap = new HashMap<String, Map<String,Integer>>();
		Jedis jedis = new Jedis("10.32.21.62", 6379, 2000);
		jedis.select(12);
		//垂直频道PV
		Map<String, String> tempMap = jedis.hgetAll("clientLogs_srhkey_byhour");
		if(tempMap == null || tempMap.isEmpty()){
			LOG.error("Update PVMap error ~");
			return pvItemMap;
		}
		Gson gson = new Gson();
		Iterator<Entry<String, String>> it = tempMap.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> entry = it.next();
			String temp = entry.getValue();
			PVItem item = gson.fromJson(temp, PVItem.class);
			pvItemMap.put(entry.getKey(), item.getId_count());
		}
		return pvItemMap;
	}
	
	
	public PropertiesConfiguration getConfigs(){
		return configs;
	}
	
	/**
	 * 用于执行线程访问配置文件数据
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	public synchronized HashMap<String, List<String>> getConfigTagsMap(){
		//drop每次调用都重新加载配置文件内容
		HashMap<String, List<String>> tempMap = new HashMap<String, List<String>>();
		tempMap.putAll(tagsMap);
		return tempMap;
	}
	/**
	 * 加载配置文件
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private HashMap<String, List<String>> loadingConfigurationTagsMap(){
		HashMap<String, List<String>> tagsMap = new HashMap<String, List<String>>();
		//加载Tags映射表 社会->社会资讯
		List<String> mapTagsList = configs.getList("mapTags");
		if(mapTagsList != null){
			for(String tags : mapTagsList){
				try{
					String[] temp = tags.split("->");
					String key = temp[0];
					String value = temp[1];
					List<String> mapList = new ArrayList<String>();
					String[] valueList = value.split("\\|");
					for(String tempValue : valueList){
						mapList.add(tempValue);
					}
					tagsMap.put(key, mapList);
				}catch (Exception e){
					LOG.error("Parser TagsMap error : "+tags, e);
					continue;
				}
			}
		}
		LOG.info("Loading config success ");
		return tagsMap;
		
	}

	/**
	 * 将需要映射的tags词添加至预加载list
	 * 
	 * 注：因为存在有些词不在可用表中，这部分词则一直无法预加载，对这部分词只能通过
	 * 手动添加，使其可以预加载，这部分词只需要添加到映射表配置文件即可
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private void addMapTagsToKeywordList(){
		HashMap<String, List<String>> temptagsMap = getConfigTagsMap();
		Set<String> keySet = temptagsMap.keySet();
		for(String key : keySet){
			KeyWord keyword = new KeyWord();
			keyword.setName(key);
			keyword.setAvailable(true);
			keyword.setType("temp");
			AlgorithmPreloadKeyUtil.getInstance().addKeyWordsToList(keyword);
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "BasicDataUpdateJob";
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		preload();
	}
	
}
