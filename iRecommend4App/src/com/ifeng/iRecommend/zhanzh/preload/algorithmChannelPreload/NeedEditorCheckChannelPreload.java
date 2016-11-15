/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;





import org.apache.solr.client.solrj.SolrQuery;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.iRecommend.featureEngineering.dataStructure.JsonFromCMPP;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.KeyWord;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.PreloadItemFromSolrUtil;

import redis.clients.jedis.Jedis;

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
 *          1.0          2016年3月14日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class NeedEditorCheckChannelPreload implements Runnable {
	private static Log LOG = LogFactory.getLog("NeedEditorCheckChannelPreload");
	PropertiesConfiguration configs;
	//用于记录泛编平台下线指令的文章id和simid 每隔5分钟获取近24个小时的下线指令
	private static Set<String> offlineNewsSet = new HashSet<String>();
	
	public NeedEditorCheckChannelPreload(PropertiesConfiguration configs) {
		// TODO Auto-generated constructor stub
		this.configs = configs;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
//		preload();
		updateOfflineNewsSet();
	}
	
	//实时获取泛编更新的下线接口
	class offlineIdItem{
		boolean success;
		List<offlineData> data;
	}
	class offlineData{
		int id;
		int state;
		String sameId;
//		 //channel字段暂时没用
	}
	private void updateOfflineNewsSet(){
		Set<String> offlineNewsSet = new HashSet<String>();
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Gson gson = new Gson();
		
		String endDate = formate.format(System.currentTimeMillis());
		String startDate = null;
		//每次请求12个小时的下线id
		for(int i=1;i<=12;i++){
			startDate = formate.format(System.currentTimeMillis() - i*0.5*60*60*1000);
			String queryUrl = "http://nyx.staff.ifeng.com/project/api/recommendMgr/getOperationStatus?startDate="+startDate+"&endDate="+endDate;
			String content = DownloadPageUtil.downloadPageByRetry(queryUrl, "UTF-8", 1);
			if(content != null){
				offlineIdItem item = gson.fromJson(content, offlineIdItem.class);
				//筛选 state=0 的item将sameId 和 id 加入offlineNewsSet
				for(offlineData data : item.data){
					if(data.state == 0){
						offlineNewsSet.add(String.valueOf(data.id));
						offlineNewsSet.add(data.sameId);
					}
				}
			}
			endDate = startDate;
		}
		this.offlineNewsSet = offlineNewsSet;
		LOG.info("Update offline newsid set success , set size : "+offlineNewsSet.size());
	}
	
	public static Set<String> getOfflineNewsIdSet(){
		Set<String> tempSet = new HashSet<String>();
		tempSet.addAll(offlineNewsSet);
		return tempSet;
	}
	
	public void preload(){
		//获取配置文件手动添加的频道
		List<String> channelList = getEditorCheckChannelList();
		//获取本地频道数据
		List<String> loc = getLocChannelList();
		channelList.addAll(loc);
		
		Gson gson = new Gson();
		String disStr = gson.toJson(channelList);
//System.out.println(disStr);
		disToRedis("EditorCheckChannel", 8*60*60*1000, disStr,2);
		
//		preloadAllTodayNewsId(); //旧的筛选方案放弃
	}
	
	private List<String> getEditorCheckChannelList(){
		List<String> list = new ArrayList<String>();
		String configStr = configs.getString("EditorCheckChannel");
		if(configStr != null){
			String[] valueList = configStr.split("\\|");
			for(String str : valueList){
				list.add(str);
			}
		}
		return list;
	}
	
	private List<String> getLocChannelList(){
		List<String> list = new ArrayList<String>();
		List<KeyWord> keyList = BasicDataUpdateJob.getInstance().getKeyWordsList(BasicDataUpdateJob.local);
		for(KeyWord word : keyList){
			if(word.getName().endsWith("省") || word.getName().endsWith("市") || word.getName().endsWith("自治州") || word.getName().endsWith("自治区") || word.getName().equals("延边州") || word.getName().equals("哈克苏地区") ){
				list.add(word.getName());
			}
			
		}
		return list;
	}
	
	/**
	 * 投放至redis函数
	 * 
	 * 
	 * 
	 * @param String tableName ： table名称
	 * 		  long validTime : 存活时间
	 * 		  String disStr ： 需要投放的字符串
	 * 
	 * @return 
	 */
	private void disToRedis(String tableName,long validTime,String disStr,int dbNum){
		if(disStr == null){
			return ;
		}
		try{

			Jedis jedis = new Jedis("10.90.1.57", 6379, 10000);
			jedis.select(dbNum);
			String status = jedis.setex(tableName, (int) validTime, disStr);
			if(!status.equals("OK")){
				LOG.error("set status code:"+status);
			}else{
				LOG.info("Dis loc "+tableName+" to redis");
			}
		}catch(Exception e){
			LOG.error("ERROR"+e);
		}
	}
	
	class editorCheckItem{
		String id;
		List<String> tags;
	}
	private static Date lastExDate = null;
	private static HashMap<String, List<PreloadItem>> cacheListMap = new HashMap<String, List<PreloadItem>>();
	/**
	 * 新的编辑筛选数据查询模块
	 * 
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 */
	private void preloadAllTodayNewsId(){

		Gson gson = new Gson();
		//更新cache
		updatePreloadNewsItem();
		Set<String> keySet = cacheListMap.keySet();
		for(String key : keySet){
			List<PreloadItem> pItemList = cacheListMap.get(key);
			List<editorCheckItem> checkList = processPreloadItemList(pItemList);
			
			String disStr = gson.toJson(checkList);
			disToRedis(key, 8*60*60*1000, disStr, 3);
			LOG.info("dis "+key+" idList size : "+checkList.size());
		}
		
//		List<editorCheckItem> checkList = processPreloadItemList(todayPreloadList);
//		LOG.info("today id size : "+todayPreloadList.size());
//		String disStr = gson.toJson(checkList);
//System.out.println(disStr);
//		disToRedis(today, 8*60*60*1000, disStr, 3);
		
		
//		List<PreloadItem> yesterdayPreloadList = getPreloadNewsItem(yesterday);
//		checkList = processPreloadItemList(yesterdayPreloadList);
//		disStr = gson.toJson(checkList);
//		disToRedis(yesterday, 8*60*60*1000, disStr, 3);
//		LOG.info("yesterday id size : "+yesterdayPreloadList.size());
		
	}
	

	
	
	private List<editorCheckItem> processPreloadItemList(List<PreloadItem> checkList){
		List<editorCheckItem> editorList = new ArrayList<editorCheckItem>();
		for(PreloadItem item : checkList){
			String features = item.getFitem().getReadableFeatures();
			List<String> tagsList = paserReadableFeature(features);
			editorCheckItem cItem = new editorCheckItem();
			cItem.id = item.getFitem().getDocId();
			cItem.tags = tagsList;
			editorList.add(cItem);
		}
		return editorList;
	}
	
	private List<String> paserReadableFeature(String features){
		List<String> featuresList = new ArrayList<String>();
		Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
		if(features.contains("|!|")){
			String[] tags = features.split("\\|!\\|");
			for(String tag : tags){
				String[] typeTag = tag.split("=");
				List<String> tempList = tagsMap.get(typeTag[0]);
				if(tempList != null){
					tempList.add(typeTag[1]);
				} else {
					tempList = new ArrayList<String>();
					tempList.add(typeTag[1]);
					tagsMap.put(typeTag[0], tempList);
				}
			}
		}
		if(tagsMap.get("c") != null){
			featuresList.addAll(tagsMap.get("c"));
		} else {
			featuresList.add("其他");
		}
		return featuresList;
	}
	
	/**
	 * 
	 * 从solr中查询预加载新闻
	 * 
	 * 
	 * 
	 * 注意：
	 * @param KeyWord keyword
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	private void updatePreloadNewsItem(){
		List<PreloadItem> preloadItemList = new ArrayList<PreloadItem>();
		Date nowDate = new Date();//当前的时间
		Date exDate = null;//程序当前执行的时间
		
		//时间解析
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd'T'");
		SimpleDateFormat formate1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Calendar cal = Calendar.getInstance();
		String begin = "00:00:00Z";
		String end = "23:59:59Z";

		//若上次执行时间与当前时间相比跨天，则进行特殊处理
		if(lastExDate != null){
			String nowString = formate.format(nowDate);
			String lastExString = formate.format(exDate);
			String lastExStringDetail = formate1.format(lastExDate);
			String specailTime = lastExString+end;
			if(!nowString.equals(lastExString) && !lastExStringDetail.equals(specailTime)){ //如果跨天
				try {
					LOG.info("Just span a day , lastExString "+lastExString+" nowDay "+nowString);
					nowDate = formate1.parse(specailTime);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					LOG.error("Process Spical day error ~ "+nowString+" "+lastExString);
				}
			} else if(lastExStringDetail.equals(specailTime)){ // 如果上次已经执行到当天的最后一秒，则将上次执行时间设置为null
				LOG.info("Yesterday has finished ~ just reset");
				lastExDate = null;
			}
		}
		
		cal.setTime(nowDate);
		String today = formate.format(cal.getTime());
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)-1);
		String yesterday = formate.format(cal.getTime());
		
		//若第一次启动则从当天00:00:00点开始加载

		if(lastExDate == null){
			String todayBegin = today+begin;
			try {
				exDate = formate1.parse(todayBegin);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error("ParserTime Error ~ "+todayBegin,e);
				exDate = new Date();
			}
		} else {
			exDate = lastExDate; //取上一次读取的时间
		}
		
		LOG.info("now begin at : "+formate.format(exDate));
		
		//一下为从cmpp中获取数据
		Gson gson = new Gson();
		List<JsonFromCMPP> cmppItemList = new ArrayList<JsonFromCMPP>();
		
		
		//创建时间获取方式通过cmpp表接口来获取

		//按秒从cmpp接口获取数据
		while(exDate.before(nowDate)){
			cal.setTime(exDate);
			String startTime = formate1.format(cal.getTime());
			//时间间隔为30秒
			cal.set(Calendar.SECOND, cal.get(Calendar.SECOND)+30);
			String endTime = formate1.format(cal.getTime());
			//当前执行date设置为15秒以后
			exDate = cal.getTime();
			List<JsonFromCMPP> tempList = loadingItemFromCMPPByTime(startTime, endTime);
//LOG.info(startTime+ " "+ endTime +" get cmppItemList size : "+tempList.size());
			cmppItemList.addAll(tempList);
		}
		
		LOG.info("cmppItemList Size : "+cmppItemList.size());
		preloadItemList = turnCmppItemToPreloadItem(cmppItemList, today);
		
		//更新cache并返回合并完的数据(删除昨天的缓存数据)
		List<PreloadItem> cacheList = cacheListMap.get(today);
		if(cacheList == null){
			cacheList = preloadItemList;
		} else {
			cacheList.addAll(preloadItemList);
		}
		
		LOG.info("Befor del repeat item list size : "+cacheList.size());
		//排重
		List<PreloadItem> resultItemList = new ArrayList<PreloadItem>();
		Set<String> idSet = new HashSet<String>();
		Iterator<PreloadItem> it = cacheList.iterator();
		while(it.hasNext()){
			PreloadItem item = it.next();
			if(idSet.contains(item.getFitem().getDocId())){
				continue;
			}
			resultItemList.add(item);
			idSet.add(item.getFitem().getDocId());
		}
		
		LOG.info("After del repeat item list size : "+resultItemList.size());
		LOG.info("Save to cache ~");
		cacheListMap.put(today, resultItemList);
		
		//删除昨天的list
		List<PreloadItem> yesterdayList = cacheListMap.get(yesterday);
		if(yesterdayList != null){
			LOG.info("Del yesterday list ~ listSize : "+yesterdayList.size());
			cacheListMap.remove(yesterday);
		}
		
	}
	
	private List<PreloadItem> turnCmppItemToPreloadItem(List<JsonFromCMPP> cmppItemList,String dayTime){
		List<PreloadItem> preloadItemList = new ArrayList<PreloadItem>();
		
		//先从solr中查找作为基础索引数据(减少查solr)
		Map<String, PreloadItem> indexMap = new HashMap<String, PreloadItem>();
		String timeBegin = dayTime+"00:00:00Z";
		String timeEnd = dayTime+"23:59:59Z";
		int rows = 20000;//预加载数据量
		
		String queryStr = null;
		queryStr = "date:["+timeBegin+" "+timeEnd+"]";
		//直接搜索t1 t2 t3 获得的数据
		SolrQuery query = generaterSolrQuery(queryStr, rows);
		String solrUrl = configs.getString("solrUrl");
		List<PreloadItem> itemList = PreloadItemFromSolrUtil.preloadItemFromSolr(query,solrUrl,false);
		LOG.info("Loading index from Solr size : "+itemList.size());
		for(PreloadItem item : itemList){
			indexMap.put(item.getFitem().getDocId(), item);
		}
		
		//将cmpp接口数据转换成实际需要审核的id
		for(JsonFromCMPP cmppItem : cmppItemList){
			PreloadItem item = indexMap.get(cmppItem.getId());
			if(item != null){
//				LOG.info(cmppItem.getId()+" hit it");
				preloadItemList.add(item);
			} else {
				rows = 2;//预加载数据量
				queryStr = null;
				queryStr = "itemid:"+cmppItem.getId();
				//直接搜索t1 t2 t3 获得的数据
				query = generaterSolrQuery(queryStr, rows);
				solrUrl = configs.getString("solrUrl");
				List<PreloadItem> tempitemList = PreloadItemFromSolrUtil.preloadItemFromSolr(query,solrUrl,false);
				preloadItemList.addAll(tempitemList);
			}
		}
		
		return preloadItemList;
	}
	
	/**
	 * 
	 * 根据起止时间从cmpp接口获取数据
	 * 
	 * 
	 * 
	 * 注意：建议不超过30s
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	private List<JsonFromCMPP> loadingItemFromCMPPByTime(String startTime ,String endTime){
		List<JsonFromCMPP> cmppItemList = new ArrayList<JsonFromCMPP>();
		Gson gson = new Gson();
		String url = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_187.jhtml?startTime="+startTime+"&endTime="+endTime;
		String content = DownloadPageUtil.downloadPageByRetry(url, "UTF-8", 2);
		try{
			List<JsonFromCMPP> tempList = gson.fromJson(content, new TypeToken<List<JsonFromCMPP>>() {
			}.getType());
			cmppItemList.addAll(tempList);
		} catch(Exception e){
			LOG.error("Parser cmppItemList error "+url, e);
		}
		if(cmppItemList.size() >=300){
			LOG.warn(startTime+ " "+ endTime +" get cmppItemList size large than 300");
		}
		return cmppItemList;
	}
	
	
	/**
	 * 
	 * 获取solr查询参数
	 * 
	 * 
	 * 
	 * 注意：
	 * @param SolrQuery query
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	private SolrQuery generaterSolrQuery(String queryStr,int rows){
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		query.setRows(rows);
		query.addSort("date",SolrQuery.ORDER.desc);
		query.set("fl", "*,score");
		
		//test 8081全新公式
		query.set("simi", "tfonly");
		query.set("defType", "payload");
		
		//过滤条件	(本地数据不加该过滤)
		if(queryStr.indexOf("loc=") < 0){
			query.addFilterQuery("other:(ifeng OR yidian -illegal )");
		}
		return query;
	}
	
	
	public static void main(String[] args){
		PropertiesConfiguration configs = new PropertiesConfiguration();
		configs.setEncoding("UTF-8");
		try {
			configs.load("conf/AlgorithmChannelPreload.properties");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		configs.setReloadingStrategy(new FileChangedReloadingStrategy());
		LOG.info("Loading config success ~");
//		BasicDataUpdateJob.getInstance().preload();
		
		NeedEditorCheckChannelPreload n = new NeedEditorCheckChannelPreload(configs);
//		n.preload();
//		n.preloadAllTodayNewsId();
		n.updateOfflineNewsSet();
	}
	
}
