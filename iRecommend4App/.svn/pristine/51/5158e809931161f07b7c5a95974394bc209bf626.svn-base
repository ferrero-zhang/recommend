/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.editorChannelPreload;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import redis.clients.jedis.Jedis;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.commen.blackList.BlackListData;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.zhanzh.Utils.AdjStringsIsSim;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.NewsSortUtil;
import com.ifeng.webapp.simArticle.client.SimDocClient;

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
 *          1.0          2015年12月14日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class EditorChannelPreload implements Runnable {
	private static Log LOG = LogFactory.getLog("EditorChannelPreload");
	private OSCache osCache;
	private PropertiesConfiguration configs;
	private static final String editorChannelPrefix = "EditorChannel_";
	
	public EditorChannelPreload(OSCache osCache){
		//配置文件初始化 编辑频道配置文件单独loading
		try {
			configs = new PropertiesConfiguration();
			configs.setEncoding("UTF-8");
			configs.load("conf/editorpreloadlist.properties");
			configs.setReloadingStrategy(new FileChangedReloadingStrategy());
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.error("Load properties errror " + e);
		}
		//公用统一个oscache
		this.osCache = osCache;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		LOG.info("EditorChannelPreload start ~");
		long time = System.currentTimeMillis();
		try {
			preload();
		} catch (Exception e) {
			LOG.info("EditorChannelPreload thread error ~"+e.getMessage());
		}
		time = (System.currentTimeMillis() - time)/1000;
		LOG.info("EditorChannelPreload finished ~ time-consuming : "+time+"s");
	}
	
	private void preload(){
		HashSet<String> channelSet = new HashSet<String>();
		Gson gson = new Gson();
		// 加载白名单channel
		List<String> list = configs.getList("editor");
		channelSet.addAll(list);
		for(String channel : channelSet){
			//存储在osCache中的key，与算法频道做区分需要加入编辑频道前缀
			String osCacheKey = EditorChannelPreload.editorChannelPrefix+channel;
			String redisKey = channel+"0";
			//最终投放数据
			List<PreloadItem> disItemList = new ArrayList<PreloadItem>();
			
			//获取osCache缓存中的数据
			List<PreloadItem> cacheNewsItemList = loadingNewsItemFromOSCache(osCacheKey);
			LOG.info("LoadingNewsItemFromOsCache success ~"+osCacheKey);
			//获取本轮待更新的数据
			List<PreloadItem> updateNewsItemList = loadingUpdateNews(channel);
			LOG.info("LoadingUpdateNewsSuccess ~"+channel);
			//建立索引，便于cache数据的检索
			Map<String, PreloadItem> indexMap = new HashMap<String, PreloadItem>();
			
			//先将cache中的数据加入dislist,并建立索引
			for(PreloadItem item : cacheNewsItemList){
				//确保缓存中不会有重复文章出现
				if(!indexMap.containsKey(item.getFitem().getDocId())){
					disItemList.add(item);
					indexMap.put(item.getFitem().getDocId(), item);
				}
			}
			//再将本轮需要更新的数据更新到cache中
			for(PreloadItem item : updateNewsItemList){
				//若id命中更新排序得分
				if(indexMap.containsKey(item.getFitem().getDocId())){
					PreloadItem tempItem = indexMap.get(item.getFitem().getDocId());
					tempItem.setRankscore(item.getRankscore());
					tempItem.getFitem().setScore(item.getFitem().getScore());
					//temp 运行一段时间后可以去除，只是在第一次启动时更新旧数据
					tempItem.getFitem().setSimId(item.getFitem().getSimId());
					tempItem.getFitem().setReadableFeatures(item.getFitem().getReadableFeatures());
					tempItem.getFitem().setHotBoost(item.getFitem().getHotBoost());
					continue;
				}
				//id没有命中的话再进行标题近似匹配
				String simDocID = null; //此simID非 集中排重的 simId
				for(PreloadItem disitem : disItemList){
					//编辑推荐位中有特殊类型数据（直播、专题）不走相似排重逻辑
					if(item.getFitem().getDocType() != null && disitem.getFitem().getDocType() != null){
						if (item.getFitem().getDocType().indexOf("doc") < 0
								|| item.getFitem().getDocType().indexOf("slide") < 0) {
							LOG.info("Find editor special item beforeDis : updateItem id "+item.getFitem().getDocId()+" title "+item.getFitem().getTitle()+
									" CacheItem id "+disitem.getFitem().getDocId()+" title "+disitem.getFitem().getTitle());
							break;
						}	
						if ( disitem.getFitem().getDocType().indexOf("doc") < 0
							   || disitem.getFitem().getDocType().indexOf("slide") < 0) {
							LOG.info("Find editor oscatch special item beforeDis : updateItem id "+item.getFitem().getDocId()+" title "+item.getFitem().getTitle()+
									" CacheItem id "+disitem.getFitem().getDocId()+" title "+disitem.getFitem().getTitle());
							continue;
						}
					}
					
					//优先走simId近似排重
					if(item.getFitem().getSimId() != null && disitem.getFitem().getSimId() != null){
						if(item.getFitem().getSimId().equals(disitem.getFitem().getSimId())){
							simDocID = disitem.getFitem().getDocId();
							LOG.info("Find sim item beforeDis by simId : updateItem id "+item.getFitem().getDocId()+" title "+item.getFitem().getTitle()+
									" CacheItem id "+disitem.getFitem().getDocId()+" title "+disitem.getFitem().getTitle());
							break;
						}
					}
					
					//集中排重simId无法排重时用标题近似排重
					if(AdjStringsIsSim.isSimStr(item.getFitem().getTitle(), disitem.getFitem().getTitle())){
						simDocID = disitem.getFitem().getDocId();
						LOG.info("Find sim item beforeDis by title : updateItem id "+item.getFitem().getDocId()+" title "+item.getFitem().getTitle()+
								" CacheItem id "+disitem.getFitem().getDocId()+" title "+disitem.getFitem().getTitle());
						break;
					}
				}
				
				
				if(simDocID != null){
					PreloadItem disitem = indexMap.get(simDocID);
					
					if(disitem.getFitem().getWhy() != null && disitem.getFitem().getWhy().contains("imcp") && item.getFitem().getWhy() != null && !item.getFitem().getWhy().contains("imcp")){ //投放的数据为编辑推荐位且待更新数据 不是 编辑推荐位时不做替换，隐藏bug
						
					} else if(item.getFitem().getWhy() != null && item.getFitem().getWhy().contains("imcp")){//如果需要更新的数据是imcp的数据，则删除缓存中旧的新闻数据
						LOG.info("found sim item , updateItem is imcp "+item.getFitem().getDocId()+" "+item.getFitem().getTitle()+
								" ,remove cache old item "+disitem.getFitem().getDocId()+" "+disitem.getFitem().getTitle());
						disItemList.remove(disitem);		
						disItemList.add(item);
						indexMap.put(item.getFitem().getDocId(), item);
					} else {
						disitem.setRankscore(item.getRankscore());
						disitem.getFitem().setScore(item.getFitem().getScore());
					}
					continue;
				}

				disItemList.add(item);
				indexMap.put(item.getFitem().getDocId(), item);
			}
			//排序
			
			disItemList = NewsSortUtil.sortEditorChannelNewsList(disItemList);

			//黑名单逻辑
			//加载黑名单
			Map<String, Set<String>> blacklistMap = getBlacklistMap();
			
			//获取频道黑名单Set
			Set<String> channelBlackSet = blacklistMap.get(channel);
			if(channelBlackSet == null){
				channelBlackSet = new HashSet<String>();
			}
			//获取全站下线黑名单
			Set<String> allBlackSet = blacklistMap.get("all");
			if(allBlackSet == null){
				allBlackSet = new HashSet<String>();
			}
			
			List<FrontNewsItem> disFrontNewsList = new ArrayList<FrontNewsItem>();
			
			Iterator<PreloadItem> it = disItemList.iterator();
			Set<String> idSet=new HashSet<String>();
			while(it.hasNext()){
				PreloadItem pitem = it.next();
				FrontNewsItem fitem = pitem.getFitem();
				if(fitem != null){
					//过滤频道黑名单
					if(channelBlackSet.contains(fitem.getDocId()) || channelBlackSet.contains(fitem.getTitle())){
						LOG.info("hit in " + channel + " black List "+fitem.getDocId()+" title : "+fitem.getTitle());
						it.remove();
						continue;
					}
					//过滤全站下线黑名单
					if(allBlackSet.contains(fitem.getDocId()) || allBlackSet.contains(fitem.getTitle())){
						LOG.info("hit in allChannel blacklist "+fitem.getDocId()+" title : "+fitem.getTitle());
						it.remove();
						continue;
					}
					
					//过滤simScore<7的数据，临时逻辑 temp临时逻辑
//					if(pitem.getSimScore() < 7){
//						it.remove();
//						LOG.info("Simscore less than 7 "+fitem.getDocId()+" title : "+fitem.getTitle());
//						continue;
//					}
					//进行一次id排重确保无id和simid重复数据
					if(!idSet.contains(fitem.getDocId())){
						if(fitem.getSimId()!=null){
							if(!idSet.contains(fitem.getSimId())){
								idSet.add(fitem.getSimId());
								disFrontNewsList.add(fitem);
							}else{
								LOG.info("Simid repet from Disdata :id :"+fitem.getDocId()+" title : "+fitem.getTitle()+" simid : "+fitem.getSimId());
								continue;
							}
						}else{
							idSet.add(fitem.getDocId());
							disFrontNewsList.add(fitem);
						}
					}
					
				}
			}
			
			//存入oscache
			if(disItemList.size() > 1000){
				disItemList = disItemList.subList(0, 1000);
			}
			
			disItemList = cacheFilter(disItemList);
			String disOSCacheStr = gson.toJson(disItemList);
			osCache.put(osCacheKey, disOSCacheStr);
			
			LOG.info("dis "+redisKey+" size : "+disFrontNewsList.size());
			
			//存入redis
			String disRedisStr = gson.toJson(disFrontNewsList);
			disToRedis(redisKey, 8 * 24 * 60 * 60 , disRedisStr);

			
		}
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
	private void disToRedis(String tableName,long validTime,String disStr){
		if(disStr == null){
			return ;
		}
		//操作57redis 后续将被60redis替代
//		try{
//
//			Jedis jedis = new Jedis("10.90.1.57", 6379, 10000);
//			jedis.select(1);
//			String status = jedis.setex(tableName, (int) validTime, disStr);
//			if(!status.equals("OK")){
//				LOG.error("set status code:"+status);
//			}else{
//				LOG.info("Dis "+tableName+" to redis");
//			}
//		}catch(Exception e){
//			LOG.error("ERROR"+e);
//		}
		//操作60redis
		try{

			Jedis jedis = new Jedis("10.90.7.60", 6379, 10000);
			jedis.select(1);
			String status = jedis.setex(tableName, (int) validTime, disStr);
			if(!status.equals("OK")){
				LOG.error("set status code:"+status);
			}else{
				LOG.info("Dis "+tableName+" to redis");
			}
		}catch(Exception e){
			LOG.error("ERROR"+e);
		}
	}
	
	/**
	 * 对加入缓存中的数据进行过滤
	 * 
	 * 专题直播类内容不加入缓存
	 * 
	 * 注：
	 * 
	 * @param 
	 * 
	 * @return void
	 */
	private List<PreloadItem> cacheFilter(List<PreloadItem> tempCache){
		if(tempCache == null || tempCache.isEmpty()){
			return tempCache;
		}
		List<PreloadItem> tempList = new ArrayList<PreloadItem>();
		for(PreloadItem tempItem : tempCache){
			
			if(tempItem.getFitem().getDocType() != null){
				if (tempItem.getFitem().getDocType().indexOf("doc") >= 0
						|| tempItem.getFitem().getDocType().indexOf("slide") >= 0
						|| tempItem.getFitem().getDocType().indexOf("video") >= 0){
					tempList.add(tempItem);
				}
			} else {
				LOG.info("Live or Special do not cache ~ "+tempItem.getFitem().getTitle()+" "+tempItem.getFitem().getDocId());
			}
		}
		return tempList;
	}
	
	/**
	 * 加载本轮需要更新的数据（编辑推荐位，以及本轮更新的数据)
	 * 
	 * 注意：通过channel字段访问配置文件获取对应的数据接口地址
	 * @param String channel 
	 * 
	 * @return String
	 * 
	 */
	private List<PreloadItem> loadingUpdateNews(String channel){
		List<PreloadItem> mergeList = new ArrayList<PreloadItem>();
		

		//获取当前编辑推荐位数据
		List<PreloadItem> editorNewsItemList = loadingNewsItemFromEditor(channel);
		LOG.info("LoadingNewsItemFromEditor ~");
		//获取算法数据
		List<PreloadItem> algNewsItemList = loadingNewsItemFromOSCache(channel);
		LOG.info("loadingNewsItemFromOSCache ~");
		//将三个list合并 用新数据取代旧数据
		//先将编辑数据加入mergeList
		for(PreloadItem item : editorNewsItemList){
			mergeList.add(item);
			
		}
		//加入算法数据
		for(PreloadItem item : algNewsItemList){
			//标题近似排重
			boolean isSim = false;
			for(PreloadItem disitem : mergeList){
				
				if(item.getFitem().getSimId() != null && disitem.getFitem().getSimId() != null){
					if(item.getFitem().getSimId().equals(disitem.getFitem().getSimId())){
						LOG.info("Find sim item beforeDis : Editoritem id "+item.getFitem().getDocId()+" title "+item.getFitem().getTitle()+
								" algItem id "+disitem.getFitem().getDocId()+" title "+disitem.getFitem().getTitle());
						//将算法数据的readableFeature加入编辑数据中
						disitem.getFitem().setReadableFeatures(item.getFitem().getReadableFeatures());
						isSim = true;
						break;
					}
				}
				
				if(AdjStringsIsSim.isSimStr(item.getFitem().getTitle(), disitem.getFitem().getTitle())){
					LOG.info("Find sim item beforeDis : Editoritem id "+item.getFitem().getDocId()+" title "+item.getFitem().getTitle()+
							" algItem id "+disitem.getFitem().getDocId()+" title "+disitem.getFitem().getTitle());
					//将算法数据的readableFeature加入编辑数据中
					disitem.getFitem().setReadableFeatures(item.getFitem().getReadableFeatures());
					disitem.getFitem().setSimId(item.getFitem().getSimId());
					isSim = true;
					break;
				}
			}
			//如果为相似，加入相似id，并过滤该条新闻 
			if(isSim){
				continue;
			} else{
				mergeList.add(item);
			}
		}
		
		//设置排序权重
		int index = 0;
		for(PreloadItem item : mergeList){
			if(item.getFitem().getWhy() != null && item.getFitem().getWhy().contains("imcp")){
				item.setRankscore(50000 - index);
			} else {
				item.setRankscore(5000 - index);
			}
			index ++;
		}
		return mergeList;
	}
	
	/**
	 * 调用小草接口查询simId
	 * 
	 * 
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	class simid{
		String clusterId;
		List<String> simIds;
	}
	private String getItemSimId(String title,String id,String content){
		try {
			String test = SimDocClient.doSearch(id, title, null, null);
			//需要加入非空检验
			if(test == null){
				return null;
			}
			simid simItem = null;
			Gson gson = new Gson();
			simItem = gson.fromJson(test, simid.class);
			if(simItem.clusterId != null){
				return simItem.clusterId;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error(" ", e);
		}
		return null;
	}
	
	
	/**
	 * 加载编辑推荐为数据
	 * 
	 * 注意：通过channel字段访问配置文件获取对应的数据接口地址
	 * @param String channel 
	 * 
	 * @return String
	 * 
	 */
	private List<PreloadItem> loadingNewsItemFromOSCache(String channel){
		List<PreloadItem> algItemList = new ArrayList<PreloadItem>();
		try {
			String cacheItem = (String) osCache.get(channel);
			if(cacheItem != null){
				Gson gson = new Gson();
				algItemList = gson.fromJson(cacheItem, new TypeToken<List<PreloadItem>>() {
				}.getType());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.warn("Loading "+channel+" from oscache error ");
		}
		return algItemList;
	}
	
	/**
	 * 加载编辑推荐为数据
	 * 
	 * 注意：通过channel字段访问配置文件获取对应的数据接口地址
	 * @param String channel 
	 * 
	 * @return String
	 * 
	 */
	private List<PreloadItem> loadingNewsItemFromEditor(String channel){
		List<PreloadItem> editorNewsList = new ArrayList<PreloadItem>();
		String entryUrl = configs.getString("EditorChannel"+channel);
		
		if(entryUrl == null){
			LOG.warn("Get Editore Visited EntryURL error "+channel);
			return editorNewsList;
		}
		String content = DownloadPageUtil.downloadPage(entryUrl, "UTF-8");
		if(content == null){
			LOG.warn("Download EditorNewsPage error "+channel+" "+entryUrl);
			return editorNewsList;
		}
		List<EditorNewsStructure> editorItems = new ArrayList<EditorNewsStructure>();
		Gson gson = new Gson();
		try{
			editorItems = gson.fromJson(content, new TypeToken<List<EditorNewsStructure>>() {
			}.getType());
		}catch (Exception e){
			LOG.warn("Turn to EditorNewsStructure error "+channel+" "+entryUrl);
			return editorNewsList;
		}
		//解析成前端需要的字符串(保留推荐位原有字符串格式）
		List<String> editorItemStr = new ArrayList<String>();
		try{
			editorItemStr = parserEditorContentStr(content);
		} catch (Exception e){
			LOG.warn("Parser EidtorJson error "+channel, e);
		}
		
		SimpleDateFormat formate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat formate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		for(EditorNewsStructure ed : editorItems){
			List<EditorNewsItem> items = ed.item;
			
			if(ed.type.equals("list")){
				//记录编辑推荐位位置信息
				for(EditorNewsItem item :items){
					FrontNewsItem fitem = new FrontNewsItem();
					//直播数据
					
//					if(item.type.indexOf("live")>=0){
////						fitem.setTitle(item.title);
////						fitem.setDocId(item.documentId);
////						fitem.setDocType(item.type);
////						fitem.setOthers("imcp");
//						//暂不做处理
//						continue;
//					}
					//转换时间
					String time = null;
					try {
						time = formate1.format(formate.parse(item.updateTime));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						LOG.error(item.updateTime+" "+item.documentId);
						LOG.error("paser editor time error and then deal new date for editorchannel");
						time = formate1.format(new Date());
					}
					
					
					String others =  "";
					for(String str : editorItemStr){
						if(str.indexOf(item.documentId) >= 0){
							others = str;
						}
					}
					//普通文章
					fitem.setTitle(item.title);
					fitem.setDocId(item.documentId);
					
					if(item.type.equals("doc")){
						fitem.setDocType("docpic");
					}else{
						fitem.setDocType(item.type);
					}
					
					fitem.setDate(time);
					fitem.setOthers(others);
					fitem.setWhy("imcp");
					
					//编辑数据相似度score 得分为10分
					fitem.setScore(10);
					
					//编辑数据获取simId
					String simId = null;
					try {
						simId = getItemSimId(fitem.getTitle(), fitem.getDocId(), null);
					} catch (Exception e) {
						LOG.info("EditorgetItemSimId error"+e.getMessage());
						continue;
					}
					if(simId != null){
						if(simId.indexOf("clusterId_")>-1){
							fitem.setSimId(simId);
						}else {
							fitem.setSimId("clusterId_"+simId);
						}
					}

					
					PreloadItem pItem = new PreloadItem();
					pItem.setFitem(fitem);
					pItem.setSimScore(10);
					editorNewsList.add(pItem);
				}
			}
		}
		
		return editorNewsList;
	}
	
	/**
	 * 将编辑推荐为的抓取数据进行字符串解析，用于提供给前端杨凯做特殊展示
	 * 
	 * 该字段对应到相应的item的other字段中同步给杨凯
	 * 
	 * 注意：
	 * @param String jsonStr 编辑推荐位的json字符串
	 * 
	 * @return List<String> 返回相应的字符串list
	 * 
	 */
	private List<String> parserEditorContentStr(String jsonStr) {
		List<String> editorItemList = new ArrayList<String>();
		if(jsonStr == null){
			return editorItemList;
		}
		if(jsonStr.indexOf("thumbnail")<0 || jsonStr.indexOf("},")<0){
			return editorItemList;
		}
		String[] strarray = jsonStr.split("},");
		StringBuffer tempSave = null;
		for(String str : strarray){
			if(tempSave != null && str.indexOf("thumbnail")>=0){
				editorItemList.add(tempSave.subSequence(0, tempSave.length()-1).toString());
				tempSave = null;
			}
			
			if(tempSave == null && str.indexOf("{\"thumbnail\"")>=0){
				tempSave = new StringBuffer();
				tempSave.append(str.substring(str.indexOf("{\"thumbnail\""))).append("},");
			}
			
			if(tempSave != null && str.indexOf("thumbnail")<0){
				tempSave.append(str).append("},");
			}
			
		}
		return editorItemList;
	}
	/**
	 * 用于处理实时获取黑名单数据
	 * 
	 * key=作用域即channel字段 all代表全部下线
	 * value Set 包含title和id两个下线字段
	 * 注意：
	 * 
	 * 返回的map中get("all")或者get(channel)可能为空 注意做处理
	 * 
	 * @param 
	 * 
	 * @return Map<String, Set<String>>
	 * 
	 */
	private static Map<String, Set<String>> getBlacklistMap(){
		HashMap<String, Set<String>> blacklistmap = new HashMap<String, Set<String>>();
		
		List<String> blacklist = BlackListData.getInstance().get_blacklist();
		  
		if(blacklist != null && !blacklist.isEmpty()){
			for(String str : blacklist){
				//兼容旧系统，actionField:all或者不带该字段的均为全站下线
				if(str.contains("actionField")){
					String[] strSplit = str.split("\\|");
					String channel = strSplit[1].replaceAll("actionField:", "");
					Set<String> tempSet = blacklistmap.get(channel);
					if(tempSet == null){
						tempSet = new HashSet<String>();
						tempSet.add(strSplit[0]);
						blacklistmap.put(channel, tempSet);
					} else {
						tempSet.add(strSplit[0]);
					}
				} else {
					Set<String> tempSet = blacklistmap.get("all");
					if(tempSet == null){
						tempSet = new HashSet<String>();
						tempSet.add(str);
						blacklistmap.put("all", tempSet);
					} else {
						tempSet.add(str);
					}
				}
			}
		}
		return blacklistmap;
	}

	public static void main(String[] args){

		List<PreloadItem> editorNewsList = new ArrayList<PreloadItem>();
		
		String content = DownloadPageUtil.downloadPage("http://api.iclient.ifeng.com/list_test?id=SH133,FOCUSSH133&gv=4.5.5", "UTF-8");
		List<EditorNewsStructure> editorItems = new ArrayList<EditorNewsStructure>();
		Gson gson = new Gson();
		try{
			editorItems = gson.fromJson(content, new TypeToken<List<EditorNewsStructure>>() {
			}.getType());
		}catch (Exception e){
			LOG.warn("Turn to EditorNewsStructure error ");
		}
		//解析成前端需要的字符串(保留推荐位原有字符串格式）
		List<String> editorItemStr = new ArrayList<String>();
		try{
//			editorItemStr = parserEditorContentStr(content);
		} catch (Exception e){
//			LOG.warn("Parser EidtorJson error "+channel, e);
		}
		for(EditorNewsStructure ed : editorItems){
			List<EditorNewsItem> items = ed.item;
			
			if(ed.type.equals("list")){
				//记录编辑推荐位位置信息
				for(EditorNewsItem item :items){
					FrontNewsItem fitem = new FrontNewsItem();
					//直播数据
					
//					if(item.type.indexOf("live")>=0){
////						fitem.setTitle(item.title);
////						fitem.setDocId(item.documentId);
////						fitem.setDocType(item.type);
////						fitem.setOthers("imcp");
//						//暂不做处理
//						continue;
//					}
					//转换时间
//					String time = null;
//					try {
//						time = formate1.format(formate.parse(item.updateTime));
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						LOG.error(item.updateTime+" "+item.documentId);
//						LOG.error("paser editor time error ", e);
//						time = formate1.format(new Date());
//					}
//					
//					
//					String others =  "";
//					for(String str : editorItemStr){
//						if(str.indexOf(item.documentId) >= 0){
//							others = str;
//						}
//					}
//					//普通文章
//					fitem.setTitle(item.title);
//					fitem.setDocId(item.documentId);
//					
//					if(item.type.equals("doc")){
//						fitem.setDocType("docpic");
//					}else{
//						fitem.setDocType(item.type);
//					}
//					
//					fitem.setDate(time);
//					fitem.setOthers(others);
//					fitem.setWhy("imcp");
//					
//					//编辑数据相似度score 得分为10分
//					fitem.setScore(10);
//					
//					//编辑数据获取simId
//					String simId = getItemSimId(fitem.getTitle(), fitem.getDocId(), null);
//					if(simId != null){
//						fitem.setSimId("clusterId_"+simId);
//					}

					
					PreloadItem pItem = new PreloadItem();
					pItem.setFitem(fitem);
					pItem.setSimScore(10);
					editorNewsList.add(pItem);
				}
			}
		}
	
		System.out.print(editorItems);
		
	}
	
	

}
