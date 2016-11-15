/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.selectForPush;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.zhanzh.SolrUtil.ItemSorlServerClient;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload.AlgPreloadManager;
import com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload.BasicDataUpdateJob;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.KeyWord;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.TimeUtil;

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
 *          1.0          2016年3月22日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

class SelectItem{
	PreloadItem pItem;
	String source;
	Map<String, String> topicMap = new HashMap<String, String>();
}

public class NewsSelectForPush implements Runnable {
	private static Log LOG = LogFactory.getLog("NewsSelectForPush");
	//从osCache中获取基础数据
	private OSCache osCache ;
	//从配置文件中获取分类筛选的分类词
	private PropertiesConfiguration configs = null;
	public NewsSelectForPush(OSCache osCache){
		this.osCache = osCache;
		try {
			configs = new PropertiesConfiguration();
			configs.setEncoding("UTF-8");
			configs.load("conf/push_topics_recommend.properties");
			configs.setReloadingStrategy(new FileChangedReloadingStrategy());
			LOG.info("Loading configs success ~");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.error("Loading configs error ~", e);
		}
	}
	
	public void selectInterestNews(){
		int selectNewsNum = 3 ; //标明每轮最多推送数目
		List<String> channelList = configs.getList("t");
		if(channelList == null || channelList.isEmpty()){
			LOG.error("Loading push channel error ~");
			return;
		}
		
		//用于保存每轮推送结果
		Map<String, List<SelectItem>> tempMap = new HashMap<String, List<SelectItem>>();
		
		for(String channel : channelList){
			List<PreloadItem> pItemList = loadingDataFromOscache(channel);
			//根据时效性筛选（选取当天的）
			List<SelectItem> todayList = selectNewsByCreatTime(pItemList);
			LOG.info(channel+" today news num : "+todayList.size());
			if(todayList.size() == 0){
				continue;
			}
			//热度筛选
			List<SelectItem> hotList = selectNewsByHotScore(todayList, selectNewsNum, 0.5);
			LOG.info(channel+" hot news num : "+hotList.size());
			
			if(hotList.size() != 0){
				tempMap.put(channel, hotList);
				continue;
			}
		}
		
		//推送新闻
		Set<String> channelSet = tempMap.keySet();
		for(String channel : channelSet){
			List<SelectItem> tempList = tempMap.get(channel);
			for(SelectItem sItem : tempList){
				sendPushNews_new(sItem, channel, "channel");
			}
			LOG.info(channel+" post news num : "+tempList.size());
		}
		LOG.info("Cover channel num : "+channelSet.size());
		Gson gson = new Gson();
		String out = gson.toJson(tempMap);
		LOG.info(out);
	}
	
	public void selectLocNews(){
		int selectNewsNum = 3 ; //标明每轮最多推送数目
		
		//用于保存每轮推送结果
		Map<String, List<SelectItem>> tempMap = new HashMap<String, List<SelectItem>>();
		//从基础数据中获取城市列表
		List<KeyWord> locList = BasicDataUpdateJob.getInstance().getKeyWordsList(BasicDataUpdateJob.local);
		for(KeyWord word : locList){
			List<PreloadItem> pItemList = loadingDataFromOscache(word.getName());
			//根据时效性筛选（选取当天的）
			List<SelectItem> todayList = selectNewsByCreatTime(pItemList);
			LOG.info(word.getName()+" today news num : "+todayList.size());
			if(todayList.size() == 0){
				continue;
			}
			//根据热度进行筛选
			List<SelectItem> hotList = selectNewsByHeat(todayList, selectNewsNum, 50);
			LOG.info(word.getName()+" hot news num : "+hotList.size());
			
			if(hotList.size() >= selectNewsNum){
				tempMap.put(word.getName(), hotList);
				continue;
			}
			//若热度符合条件的不满足推送数目，则根据tag词筛选
			List<SelectItem> tagsList = selectNewsByTags(todayList, selectNewsNum);
			LOG.info(word.getName()+" tagsFilt news num : "+tagsList.size());
			for(SelectItem sItem : tagsList){
				if(!hotList.contains(sItem) && hotList.size()<selectNewsNum){
					hotList.add(sItem);
				}
			}
			if(hotList.size() >= selectNewsNum){
				tempMap.put(word.getName(), hotList);
				continue;
			}
			//将标题中含有城市名称的新闻筛选出来
			List<SelectItem> titleList = selectNewsByTitle(todayList, selectNewsNum, word.getName());
			LOG.info(word.getName()+" titleFilt news num : "+titleList.size());
			for(SelectItem sItem : titleList){
				if(!hotList.contains(sItem) && hotList.size()<selectNewsNum){
					hotList.add(sItem);
				}
			}
			if(hotList.size() >= selectNewsNum){
				tempMap.put(word.getName(), hotList);
				continue;
			}
			//若没有符合的则默认选择当天前3条数据作为
			for(SelectItem sItem : todayList){
				if(!hotList.contains(sItem) && hotList.size()<selectNewsNum){
					hotList.add(sItem);
				}
			}
			if(hotList.size() != 0){
				tempMap.put(word.getName(), hotList);
				continue;
			}
		}
		
		//推送新闻
		Set<String> citySet = tempMap.keySet();
		for(String city : citySet){
			List<SelectItem> tempList = tempMap.get(city);
			for(SelectItem sItem : tempList){
//				sendLocNews(sItem, city); //drop
				sendPushNews_new(sItem, city, "loc");
			}
			LOG.info(city+" post news num : "+tempList.size());
		}
		LOG.info("Cover city num : "+citySet.size());
		Gson gson = new Gson();
		String out = gson.toJson(tempMap);
		LOG.info(out);
		
		
	}
	
	
	class UpdateClass{
		String docId ;
		String docTitle;
		String docTm;
		String keywords;
		String docSource;
		String loc;
	}
	
//	private void sendLocNews(SelectItem item,String city){
//		String url = "http://10.32.21.57:8080/pushInterface/addPushItem";
////		String url1 = "http://10.32.21.75:8080/pushInterface/addPushItem";//到抽样推送系统里一份
//		//转换id
//		String imcpId = getItemImcpId(item.pItem);
//		if(imcpId == null){
//			LOG.error("Get imcpId error"+item.pItem.getFitem().getDocId());
//			return;
//		}
//		String title = item.pItem.getFitem().getTitle();
//		String time = item.pItem.getFitem().getDate();
//		String source = item.source;
//		UpdateClass uItem = new UpdateClass();
//		uItem.docId = imcpId;
//		uItem.docTitle = title;
//		uItem.docTm = time;
//		uItem.keywords = city;
//		uItem.docSource = source;
//		Gson gson = new Gson();
//		
//		String param = "param="+gson.toJson(uItem);
//		String result = sendPost(url, param);
////		String result1 = sendPost(url1, param);
//		LOG.info(city+ " : "+title);
//		LOG.info(result);
////		LOG.info("result1 : "+result1);
//	}
	
	private void sendPushNews_new(SelectItem item,String key,String Type){
		String url = "http://10.32.21.57:8080/pushInterface/addPushItem";
		String url1 = "http://10.32.21.75:8080/pushInterface/addPushItem";//到抽样推送系统里一份
//		String url2 = "http://172.30.19.70:8080/pushInterface/addPushItem";//test
		//转换id
		String imcpId = getItemImcpId(item.pItem);
		if(imcpId == null){
			LOG.error("Get imcpId error"+item.pItem.getFitem().getDocId());
			return;
		}
		String title = item.pItem.getFitem().getTitle();
		String time = item.pItem.getFitem().getDate();
		String source = item.source;
		UpdateClass uItem = new UpdateClass();
		if(Type.equals("loc")){
			uItem.docId = imcpId;
			uItem.docTitle = title;
			uItem.docTm = time;
			uItem.loc = key;
			uItem.docSource = source;
		} else if(Type.equals("channel")){
			uItem.docId = imcpId;
			uItem.docTitle = title;
			uItem.docTm = time;
			uItem.keywords = key;
			uItem.docSource = source;
		} else {
			LOG.error("sent type error ~");
			return;
		}

		Gson gson = new Gson();
		
		String param = "param="+gson.toJson(uItem);
		String result = sendPost(url, param);
		String result1 = sendPost(url1, param);
//		String result2 = sendPost(url2, param);
		LOG.info(key+ " : "+title);
		LOG.info(result);
		LOG.info("result1 : "+result1);
//		LOG.info("result2 : "+result2);
	}
	
	private String getItemImcpId(PreloadItem item){
		String url = "http://i.ifeng.com/getIdByUrl?url=http://t.ifeng.com/appshare/"+item.getFitem().getDocId()+".shtml";
		String content = DownloadPageUtil.downloadPageByRetry(url, "UTF-8", 3);
		if(content == null || content.indexOf(":")<0){
			return null;
		}
		String imcpId = content.substring(content.indexOf("\":\"")+3, content.indexOf("\"}"));
		return imcpId;
	}
	
	
	/**
	 * 
	 * 根据标题是否包含城市名称
	 * 
	 * 
	 * 
	 * 注意：
	 * @param String key
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	private List<SelectItem> selectNewsByTitle(List<SelectItem> sItemList,int selectNum , String channel){
		List<SelectItem> tempList = new ArrayList<SelectItem>();
		if(sItemList.isEmpty()){
			return tempList;
		}
		int i = 0;
		String cityName = channel.replaceAll("市", "").replaceAll("省", "").replaceAll("自治区", "");
		for(SelectItem sItem : sItemList){
			if(sItem.pItem.getFitem().getTitle().indexOf(cityName) >= 0){
				tempList.add(sItem);
				i++;
			}
			if(i >= selectNum){
				break;
			}
		}
		
		return tempList;
	}
	
	
	/**
	 * 
	 * 根据tags进行筛选
	 * 
	 * 
	 * 
	 * 注意：
	 * @param String key
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	private List<SelectItem> selectNewsByTags(List<SelectItem> sItemList,int selectNum){
		List<SelectItem> tempList = new ArrayList<SelectItem>();
		if(sItemList.isEmpty()){
			return tempList;
		}
		int i = 0;
		for(SelectItem sItem : sItemList){
			Map<String,String> tagsMap = sItem.topicMap;
			Set<String> keySet = tagsMap.keySet();
			for(String key : keySet){
				String tags = tagsMap.get(key);
				if(tags.indexOf("社会")>=0 || 
				   tags.indexOf("反腐")>=0 || 
				   tags.indexOf("大陆")>=0 ||
				   tags.indexOf("人事")>=0 ||
				   tags.indexOf("旅游")>=0 ||
				   tags.indexOf("生活")>=0 ){
					tempList.add(sItem);
					i++;
				}
				if(i >= selectNum){
					break;
				}
			}
		}
		return tempList;
	}
	
	/**
	 * 
	 * 筛选当天的新闻
	 * 
	 * 并且查询solr，查询稿源、和对于tag标签
	 * 
	 * 注意：
	 * @param String key
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	private List<SelectItem> selectNewsByCreatTime(List<PreloadItem> pItemList){
		List<SelectItem> tempList = new ArrayList<SelectItem>();
		if(pItemList == null || pItemList.isEmpty()){
			return tempList;
		}
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		for(PreloadItem pItem : pItemList){
			try {
				Date date = dateFormate.parse(pItem.getFitem().getDate());
				if(TimeUtil.isSameDayOfMillis(System.currentTimeMillis(), date.getTime())){
					//查询solr，查询稿源、和tag标签
					SelectItem sItem = setDocSorceAndTagsFromSolr(pItem);
					tempList.add(sItem);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error("ParserTimeError : "+pItem.getFitem().getDocId(), e);
				continue;
			}
		}
		return tempList;
	}
	
	/**
	 * 
	 * 查询solr，查询稿源、和对于tag标签
	 * 
	 * 注意：
	 * @param PreloadItem
	 * 
	 * @return SelectItem
	 * 
	 */
	public static SelectItem setDocSorceAndTagsFromSolr(PreloadItem item){
		String docId = item.getFitem().getDocId();
		String source = "";
		String queryStr = "itemid:("+docId+")";
		String solrUrl = "http://10.32.28.119:8081/solr46/item/";
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		SelectItem sItem = new SelectItem();
		sItem.pItem = item;
		try {
			ItemSorlServerClient client = ItemSorlServerClient.getInstance();
			HttpSolrServer server = client.getServer(solrUrl);
			QueryResponse qr = server.query(query);
			SolrDocumentList doclist = qr.getResults();
			Iterator<SolrDocument> it = doclist.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				source  = (String) doc.get("source");
				String topic1 = (String) doc.get("topic1");
				String topic2 = (String) doc.get("topic2");
				String topic3 = (String) doc.get("topic3");
				sItem.source = source;
				sItem.topicMap.put("topic1", topic1);
				sItem.topicMap.put("topic2", topic2);
				sItem.topicMap.put("topic3", topic3);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("",e);
		}
		
		return sItem;
	}

	
	/**
	 * 
	 * 根据热度筛选符合阈值的新闻
	 * 
	 * 
	 * 
	 * 注意：
	 * @param String key
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	private List<SelectItem> selectNewsByHeat(List<SelectItem> sItemList,int selectNum, double score){
		List<SelectItem> tempList = new ArrayList<SelectItem>();
		if(sItemList == null || sItemList.isEmpty()){
			return tempList;
		}
		
		int i = 0;
		for(SelectItem sItem : sItemList){
			if(sItem.pItem.getRankscore() >= score && i<selectNum){
				tempList.add(sItem);
				i++;
			}
			if(i >=selectNum){
				break;
			}
		}
		
		return tempList;
	}
	
	/**
	 * 
	 * 根据热度筛选符合阈值的新闻
	 * 
	 * 
	 * 
	 * 注意：
	 * @param String key
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	private List<SelectItem> selectNewsByHotScore(List<SelectItem> sItemList,int selectNum, double score){
		List<SelectItem> tempList = new ArrayList<SelectItem>();
		if(sItemList == null || sItemList.isEmpty()){
			return tempList;
		}
		
		int i = 0;
		for(SelectItem sItem : sItemList){
			if(sItem.pItem.getHotscore() >= score && i<selectNum){
				tempList.add(sItem);
				i++;
			}
			if(i >=selectNum){
				break;
			}
		}
		
		return tempList;
	}
	
	/**
	 * 
	 * 在预加载层的oscache缓存中获取数据
	 * 
	 * 
	 * 
	 * 注意：
	 * @param String key
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	private List<PreloadItem> loadingDataFromOscache(String key){
		List<PreloadItem> datalist = null;
		String content = null;
		try {
			content = (String) osCache.get(key);
			if(content != null){
				Gson gson = new Gson();
				datalist = gson.fromJson(content,  new TypeToken<List<PreloadItem>>() {
				}.getType());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.warn("Loading "+key+" from oscache error ~",e);
			return datalist;
		}
		return datalist;
	}
	
	private String sendPost(String url,String param){
		PrintWriter out = null;
		BufferedReader in = null;
		StringBuffer result = new StringBuffer();
		
		try {
			URL realUrl = new URL(url);
			//建立连接
			URLConnection conn = realUrl.openConnection();
			//设置通用属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			//设置连接超时
			conn.setConnectTimeout(1000*1);
			conn.setReadTimeout(1*1000);
			//发送POST请求必须设置参数
			conn.setDoOutput(true);
			conn.setDoInput(true);
			//out
			out = new PrintWriter(conn.getOutputStream());
			out.print(param);
			out.flush();
			//in
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while((line = in.readLine()) != null){
				result.append(line);
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error(" ",e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error(" ",e);
		} catch (Exception e){
			LOG.error(" ", e);
		} finally {		
			try {
				if(out != null){
					out.close();
				}
				if(in != null){
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				LOG.error("", e);
			}
		}
		return result.toString();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		selectLocNews();
		selectInterestNews();
	}
	
	
	public static void main(String[] args){
//		setDocSorceAndTagsFromSolr("2818908");
		
//		selectNews();
//		AlgPreloadManager al = AlgPreloadManager.getInstance();
//		NewsSelectForPush ns = new NewsSelectForPush(al.osCache);
//		ns.run();

	}
}
