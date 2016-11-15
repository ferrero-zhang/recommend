/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.newHotPredict;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import redis.clients.jedis.Jedis;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.zhanzh.SolrUtil.SearchItemsFromSolr;
import com.ifeng.iRecommend.zhanzh.Utils.AdjStringsIsSim;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;





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
 *          1.0          2015年9月21日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class YidianHotPredict {
	
    private static Log LOG = LogFactory.getLog("TestCrawlerYidian");
	
	//loading channel列表
	private static PropertiesConfiguration configs;
	
	//加入OSCache作为本地缓存，测试差值增长率
	//OSCache
	private OSCache osCache ;
	private static final String CACHE_NAME = "OScache_YidianHot";
//	private static final String configPath = "D:/test/yidian_hotpredic.properties";
	private static final String configPath = "/data/zhanzh/YidianHotpredict/conf/yidian_hotpredic.properties";
	private static final String OScache_configPath = "conf/oscache_YidianHot.properties";
	
	//ikv 数据库
	private static IKVOperationv2 ikv ;
	
	public YidianHotPredict(){
		// 白名单配置文件初始化
		try {
			configs = new PropertiesConfiguration();
			configs.setEncoding("UTF-8");
			configs.load(configPath);
			configs.setReloadingStrategy(new FileChangedReloadingStrategy());
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.error("Load properties errror " + e);
		}

		// OScache初始化
		int refreshInterval = 30 * 24 * 60 * 60;
		LOG.info("refreshInterval = " + refreshInterval);
		osCache = new OSCache(OScache_configPath, CACHE_NAME,
				refreshInterval);
		LOG.info(CACHE_NAME + " has created");
		
		//ikv 初始化
		ikv = new IKVOperationv2("appitemdb");
	}
	
	/**
	 * 一点资讯数据获取函数
	 * 
	 * 注意：通过HTTP连接，绑定cookieid进行访问
	 * 
	 * @param String channel 请求频道
	 * @return String json 返回格式化json字符串 <YidianResponseClass> 
	 *         获取失败返回null
	 */
	private String getYidianNewsJsonByChannel(String channel){
		String jsonStr = null;
		String urlStr = "http://www.yidianzixun.com/api/q/?path=channel|news-list-for-keyword&display="+channel+"&word_type=token&fields=docid&fields=category&fields=date&fields=image&fields=image_urls&fields=like&fields=source&fields=title&fields=url&fields=comment_count&fields=summary&fields=up&cstart=0&cend=500&version=999999&infinite=true";
		RequestConfig defaultRecuestConfig = RequestConfig.custom().setConnectTimeout(2000).setConnectionRequestTimeout(2000).build();
		
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRecuestConfig).build();
		
		try {
			URL url = new URL(urlStr);
			URI uri = new URI(url.getProtocol(),url.getHost(),url.getPath(),url.getQuery(),null);
			
			HttpGet httpget = new HttpGet(uri);
		
			httpget.setHeader(new BasicHeader("Cookie", "JSESSIONID=constant-session-1; captcha=s%3A18V7ZK.dQcSbxrhnqMsi2HFoo0E%2BHtBEboACvsqKdyHcOPAQ%2Bc; Hm_lvt_15fafbae2b9b11d280c79eff3b840e45=1441612354; Hm_lpvt_15fafbae2b9b11d280c79eff3b840e45=1441695434; CNZZDATA1255169715=1217341293-1441611603-null%7C1441692545"));
			CloseableHttpResponse response = httpclient.execute(httpget);
			
			HttpEntity entity = response.getEntity();
			jsonStr = EntityUtils.toString(entity,"UTF-8");
			response.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			LOG.error(" ", e);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			LOG.error(" ", e);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			LOG.error(" ", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error(" ", e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error(" ", e);
			}
		}
		return jsonStr;
	}
	
	/**
	 * 一点资讯新闻列表获取函数
	 * 
	 * 注意：
	 * 
	 * @param String channel 请求频道
	 * @return List<YidianResponseResultClass>
	 */
	private List<YidianResponseResultClass> getYidianDocInfoList(String channel){
		List<YidianResponseResultClass> resultList = null;
		String json = getYidianNewsJsonByChannel(channel);
		if(json == null || json.isEmpty()|| json.indexOf("status\":\"success")<0){
			LOG.warn("get yidian json error : json is null");
			return null;
		}
		Gson gson = new Gson();
		YidianResponseClass response = gson.fromJson(json, YidianResponseClass.class);
		resultList = response.result;
		return resultList;
	}
	
	/**
	 * 查找下一个分割点；
	 * 
	 * 
	 */
	private int findNextSep(List<HotRankItem> itemList, int begin,
			int end, double PVFlagRate) {
		if(begin < 0)
			return -1;
		if(begin > end)
			return -1;
		
		// 默认第一个是
		int index_rt = -1; 
		double score = itemList.get(begin).getHotScore();
		if (end < 0)
			end = itemList.size() - 1;
		if(end >= itemList.size())
			end = itemList.size() - 1;
		// 计算[begin,end]的落差，获取第一个满足条件的落差
		int index = begin;
		for (; index < end; index++) {
			double gap = itemList.get(index).getHotScore() - itemList.get(index + 1).getHotScore();
			double gap_rate = gap / itemList.get(index).getHotScore();
//System.out.println("beforeScore : "+itemList.get(index).getHotScore()+" afterscore "+itemList.get(index + 1).getHotScore()+" gap : "+gap+" gaprate : "+gap_rate);
			// 落差太小可以忽略
			if (gap <= 10 || gap_rate < 0.5)
				continue;
			//落差满足，算出落差的点，但是和目前PV差距太大
			if(gap_rate >= 0.90)
				break;
			
			index_rt = index;
			break;

		}
		// 没有算出落差，怎么处理？？
		if (index_rt < 0) {
			for (int i = begin; i <= end; i++) {
				if (itemList.get(i).getHotScore() < score * PVFlagRate)
				{
					index_rt = i - 1;
					break;
				}
			}
		}
		
		//得分太小则不再计算落差
		if(index_rt < 0 || itemList.get(index_rt).getHotScore()<10){
			index_rt = begin;
		}
		
		return index_rt;
	}
	
	/**
	 * 以固定格式存入本地OScache中，
	 * 
	 * 注意：根据saveTimes保存制定轮数的数据，更新式保存。
	 * 
	 * @param List<YidianResponseResultClass> yidianlist,String channel,int saveTimes
	 * @return void
	 */
	private void saveToOscache(List<YidianResponseResultClass> yidianlist,String channel,int saveTimes){
		if(yidianlist==null||yidianlist.isEmpty()||channel==null){
			return ;
		}
		for(int i=saveTimes-1;i>0;i--){
			String tempchannel = channel + i;
			try {
				String json = (String) osCache.get(tempchannel);
				tempchannel = channel+(i+1);
				osCache.put(tempchannel	, json);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.info("Save cache warn : this channel cannot found in oscache "+tempchannel);
			}
		}
		Gson gson = new Gson();
		String disJson = gson.toJson(yidianlist);
		String tempChannel = channel + "1";
		osCache.put(tempChannel, disJson);
	}
	
	private FrontNewsItem searchItemFromLocalPool(YidianResponseResultClass result){
		if(result.title != null){
			//item库查不到再通过标题近似匹配
//			String title = result.title;
//			String querUrl = "http://10.32.21.62:8080/Rec4User/GetSearchResult?key="+title;
////			String content = DownloadPageUtil.downloadPageByRetry(querUrl, "UTF-8", 3);
//			String content = getPageHtml(querUrl);
//			if(content != null && content.indexOf("success\":false")<0){
//				Gson gson = new Gson();
//				List<FrontNewsItem> itemlist = gson.fromJson(content, new TypeToken<List<FrontNewsItem>>() {
//				}.getType());
//				for(FrontNewsItem newsItem : itemlist){
//					if(newsItem.getTitle() != null && AdjStringsIsSim.isSimStr(newsItem.getTitle(), title)){
//						return newsItem;
//					}
//				}
//				
//			}
			
			itemf item = ikv.queryItemF(result.title, "c");
			if(item != null && item.getID() != null){
				String front = SearchItemsFromSolr.searchItem2appJsonById(item.getID());
				if(front != null){
					Gson gson = new Gson();
					FrontNewsItem fitem = gson.fromJson(front, FrontNewsItem.class);
					return fitem;
				}else{
					LOG.error("Search id from solr error : "+item.getID());

				}
				LOG.info("Turn to frontItem ");
				FrontNewsItem fitem = new FrontNewsItem(item);
				return fitem;
			}
		}
		if(result !=null){
			//先通过拼接url差item库获取id在查询solr中的FrontNewsItem
			if(result.docid != null){
				String url = "http://a1.go2yd.com/Website/contents/content?appid\u003dyidian\u0026bottom_channels\u003dtrue\u0026cv\u003d2.6.1.3\u0026docid\u003d"+result.docid;
				
				itemf item = ikv.queryItemF(url, "c");
				if(item != null && item.getID() != null){
					String front = SearchItemsFromSolr.searchItem2appJsonById(item.getID());
					if(front != null){
						Gson gson = new Gson();
						FrontNewsItem fitem = gson.fromJson(front, FrontNewsItem.class);
						return fitem;
					}else{
						LOG.error("Search id from solr error : "+item.getID());

					}
					LOG.info("Turn to frontItem ");
					FrontNewsItem fitem = new FrontNewsItem(item);
					return fitem;
				}
			}
			


		}
		return null;
	}
	
	private String getPageHtml(String url){
		String content = null;
		
		RequestConfig defaultRecuestConfig = RequestConfig.custom().setConnectTimeout(2000).setConnectionRequestTimeout(2000).build();
		
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRecuestConfig).build();
		try {
			URL neturl = new URL(url);
			neturl.getPort();
			URI uri = new URI(neturl.getProtocol(), null, neturl.getHost(), neturl.getPort(), neturl.getPath(), neturl.getQuery(), null);
			HttpGet httpget = new HttpGet(uri);
			CloseableHttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity,"UTF-8");
			response.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return content;
	}
	

	/**
	 * hackerNews得分计算函数
	 * 注意：G是新闻生命时间权重因子，G越大新闻生命之间对得分的惩罚越高，目前默认是G=1.8
	 * 
	 * @param int 评论参与数 double 新闻生命时间
	 * @return double 返回hackerNews得分
	 */
	private double hackerNews(int pv, double lifeTime) {
		double score = 0.0;
		if (pv == 0) {
			return score;
		}
		lifeTime = lifeTime / (1000 * 60 * 60);
		double G = 1.8; // 1.8;
		double under = Math.pow((lifeTime + 2), G);
		score = (pv - 1) / under;
		return score;
	}
	
	
	private List<HotRankItem> turnToHotItem(List<YidianResponseResultClass> yidianlist){
		List<HotRankItem> rankList = new ArrayList<HotRankItem>();
		if(yidianlist == null || yidianlist.isEmpty()){
			return rankList;
		}
		for(YidianResponseResultClass result : yidianlist){
			if(result.docid == null || result.title == null){
				continue;
			}

			FrontNewsItem item = searchItemFromLocalPool(result);
			if(item == null){
				continue;
			}
			HotRankItem hot = new HotRankItem(item);
			hot.setPv(result.up);
			hot.setCommentsNum(result.comment_count+result.like);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				Date date = format.parse(hot.getPublishTime());
				long lifetime = System.currentTimeMillis()-date.getTime();
				hot.setLifeTime(lifetime);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
//			用评论数+喜欢数
			double hotscore0 = hackerNews(hot.getCommentsNum(), hot.getLifeTime());
//			test 用up数据
			double hotscore1 = hackerNews(hot.getPv(), hot.getLifeTime());
			
			double hotscore =  hotscore1;
			hot.setHotScore(hotscore);
			rankList.add(hot);
		}
		return rankList;
	}
	
	private List<HotRankItem> turnToHotItem(String channel,List<YidianResponseResultClass> yidianlist){
		List<HotRankItem> rankList = new ArrayList<HotRankItem>();
		List<YidianResponseResultClass> cacheyidian = null;
		int cachenum = 3;
		Gson gson = new Gson();
		if(yidianlist == null || yidianlist.isEmpty()){
			return rankList;
		}
		String json = null;
		for (int i = cachenum; i >= 1; i--) {
			String tempchannel = channel + i;
			try {
				json = (String) osCache.get(tempchannel);
				if(json != null){
					cacheyidian = gson.fromJson(json, new TypeToken<List<YidianResponseResultClass>>() {
					}.getType());
					LOG.info("Loading "+tempchannel+" success");
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.info("Can not found this channel : "+tempchannel);
				continue;
			}
		}
		saveToOscache(yidianlist, channel, cachenum);
		//取两次获取数据的差值
		if(cacheyidian != null){
			HashMap<String, YidianResponseResultClass> cacheyidianmap = new HashMap<String, YidianResponseResultClass>();
			for(YidianResponseResultClass yidian : cacheyidian){
				cacheyidianmap.put(yidian.docid, yidian);
			}
			int index = 1;
			for(YidianResponseResultClass result : yidianlist){
				FrontNewsItem item = searchItemFromLocalPool(result);
				if(item == null){
					continue;
				}
				int pv=0;
				int comments = 0;
				int like = 0;
				YidianResponseResultClass tempyidian = cacheyidianmap.get(result.docid);
				if(tempyidian == null){
					LOG.info("This "+result.title+" is new ~");
					pv = result.up/index;
					comments = result.comment_count/index;
					like = result.like/index;
				}else{
					pv = Math.abs(result.up - tempyidian.up+1);
					comments = Math.abs(result.comment_count - tempyidian.comment_count+1);
					like = Math.abs(result.like -tempyidian.like);
				}
				HotRankItem hot = new HotRankItem(item);
				hot.setPv(pv);
				hot.setCommentsNum(comments+like);
				hot.setNewsLocate(index++);
				double rate = Math.abs((double) (100-index)/100);
				double hotscore = (double) (pv+hot.getCommentsNum()*5)*rate;
				hot.setHotScore(hotscore);
				rankList.add(hot);
			}
		}else{
			LOG.info("cache is null return old hotlist");
			return turnToHotItem(yidianlist);
		}

		return rankList;
	}
	
	private void SortByHotscore(List<HotRankItem> ranklist){
		if(ranklist == null || ranklist.isEmpty()){
			return;
		}
		Collections.sort(ranklist, new Comparator<HotRankItem>() {

			@Override
			public int compare(HotRankItem o1, HotRankItem o2) {
				// TODO Auto-generated method stub
				if(o1.getHotScore()<o2.getHotScore()){
					return 1;
				}else{
					return -1;
				}
			}
		});
	}
	
	private void setHotLevel(List<HotRankItem> ranklist){
		if(ranklist == null || ranklist.isEmpty()){
			return;
		}
		//find A
		int indexA = findNextSep(ranklist, 0, 10 , 0.8);
//System.out.println("indexA : "+indexA);
		for(int i=0;i<=indexA;i++){
			ranklist.get(i).setHotLevel("A");
		}
		//find B
		int indexB = findNextSep(ranklist, indexA+1, ranklist.size() - 1, 0.8);
//System.out.println("indexB : "+indexB);
		if(indexB<0){
			return;
		}
		for(int i=indexA+1;i<=indexB;i++){
			ranklist.get(i).setHotLevel("B");
		}
		//find C
		int indexC = findNextSep(ranklist, indexB+1, ranklist.size() - 1, 0.8);
//System.out.println("indexC : "+indexC);
		if(indexC<0){
			return;
		}
		for(int i=indexB+1;i<=indexC;i++){
			ranklist.get(i).setHotLevel("C");
		}
		//find D
		if(indexC+1<ranklist.size()){
			for(int i=indexC+1;i<ranklist.size();i++){
				ranklist.get(i).setHotLevel("D");
			}
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
		try{
			Jedis jedis = new Jedis("10.32.21.62", 6379, 10000);
			jedis.select(12);
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
	
	public void calculateYidianHotLevel(){
		List<String> channelList = configs.getList("c");
		if(channelList == null || channelList.isEmpty()){
			LOG.warn("Loading configs channel error ~");
			return;
		}
		HashMap<String, String> disHotMap = new HashMap<String, String>();
		for(String channel : channelList){
			List<YidianResponseResultClass> resultlist = getYidianDocInfoList(channel);
			if(resultlist == null || resultlist.isEmpty()){
				LOG.error("get "+channel+" yidian list error : yidian list is null");
				continue;
			}
			
			LOG.info(channel + " YidianList_size : "+resultlist.size());
			List<HotRankItem> ranklist = turnToHotItem(channel,resultlist);
			LOG.info(channel + " pool_size : "+ranklist.size());
			if(ranklist == null||ranklist.size()<50){
				LOG.warn(channel+" size less than 50 just continue ~");
				continue;
			}
			SortByHotscore(ranklist);
			setHotLevel(ranklist);
			LOG.info("========>> "+channel+" <<=========");
			for(HotRankItem hot : ranklist){
				//热度为D的不加入计算
				if(hot.getHotLevel()!=null && hot.getHotLevel().equals("D")){
					break;
				}
				LOG.info(hot.getTitle()+" ==> "+hot.getHotLevel());
				if(hot.getDocId()!=null&&hot.getHotLevel()!=null){
					disHotMap.put(hot.getDocId(), hot.getHotLevel());
				}
			}
			

			Gson gson = new Gson();
			String json = gson.toJson(ranklist);
			LOG.info("debug : "+json);
		}
		LOG.info("DisHotMapSize : "+disHotMap.size());
		Gson gson = new Gson();
		String json = gson.toJson(disHotMap);
		disToRedis("yidian_hotmap", 1*60*60, json);
	}
	
	public static void main(String[] args){
		YidianHotPredict yd = new YidianHotPredict();
		while(true){
			yd.calculateYidianHotLevel();
			try {
				LOG.info("Sleep 5 minutes");
				Thread.sleep(5*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
class YidianResponseResultClass{
	String ctype;
	String title;
	List<String> tags;
	String meta;
	String date;
	String docid;
	int comment_count;
	String image;
	List<String> image_urls;
	int like;
	int mtype;
	String source;
	String summary;
	String url;
	String auth;
	int dtype;
	int up;
	
	//channel标签数据结构
	String description;
	boolean show_search_bar;
	String show_search_hint;
	List<Channels> channels;
}

class Channels{
	String name;
	String id ;
	String type;
	String image;
	String bookcount;
}

class YidianResponseClass{
	String status;
	int code;
	List<YidianResponseResultClass> result;
	int fresh_count;
	String channel_name;
	String channel_type;
	String channel_image;
}