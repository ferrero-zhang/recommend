/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.LocNewsSelect;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import redis.clients.jedis.Jedis;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.SolrUtil.ItemSorlServerClient;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
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
 *          1.0          2016年2月29日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class LocNewsSelect implements Runnable {
	private static Log LOG = LogFactory.getLog("LocNewsSelect");
	
	public void selectLocNews(){
		Map<String, List<PreloadItem>> newsMap = getLocNewsFromRedis();
		Set<String> keySet = newsMap.keySet();
		
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		
		Map<String, List<PreloadItem>> tempMap = new HashMap<String, List<PreloadItem>>();
		for(String key : keySet){
			int indexCount = 0; //计数器暂设定选取3条新闻
			List<PreloadItem> list = newsMap.get(key);
			
			//对key进行处理 ， 若为省和自治区 则将push的key改为 **省余量地区
			String pushKey = key;
			if(key.endsWith("省") || key.endsWith("自治区")){
				pushKey = key+"余量地区";

			}
			
			//根据热度阈值进行筛选
			for(PreloadItem item : list){
				try {
					Date date = dateFormate.parse(item.getFitem().getDate());
					//数量超过计数器设置退出
					if(indexCount >=3 ){
						LOG.info(key+" news Num : 3");
						break;
					}
					//首先限定为当天新闻
					if(TimeUtil.isSameDayOfMillis(System.currentTimeMillis(), date.getTime())){
						//限定阈值大于100
						if(item.getRankscore() >= 50){
							List<PreloadItem> tempList = tempMap.get(key);
							if(tempList == null){
								tempList = new ArrayList<PreloadItem>();
								tempList.add(item);
								tempMap.put(key, tempList);
							} else {
								tempList.add(item);
							}
//							sendLocNews(item,pushKey);
							indexCount ++;
						} else {
							//标题中带有城市名的优先给出
							String cityName = key.replaceAll("市", "").replaceAll("省", "");
							if(item.getFitem().getTitle().indexOf(cityName)>=0){
								List<PreloadItem> tempList = tempMap.get(key);
								if(tempList == null){
									tempList = new ArrayList<PreloadItem>();
									tempList.add(item);
									tempMap.put(key, tempList);
								} else {
									tempList.add(item);
								}
//								sendLocNews(item,pushKey);
								indexCount ++;
								LOG.info(item.getFitem().getTitle()+" include city name "+cityName);
							}
						}
					}
					
					//时间超过48小时的默认break
					long lifeTime = System.currentTimeMillis() - date.getTime();
					if(lifeTime > 48 * 60 * 60 * 1000){
						List<PreloadItem> tempList = tempMap.get(key);
						if(tempList != null && !tempList.isEmpty()){
							LOG.info(key+" news Num : "+tempList.size());
						} else {
							LOG.info("Loading "+key+" faild ~");
						}
						
						break;
					}
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			
			
			
			//热度阈值无法筛选出则选取排在靠前的2条，以保证覆盖度
			if(indexCount < 3){
				
				
				
				
				for(PreloadItem item : list){
					try {
						Date date = dateFormate.parse(item.getFitem().getDate());
						//数量超过计数器设置退出
						if(indexCount >= 3 ){
							LOG.info(key+" news Num : 3");
							break;
						}
						//首先限定为当天新闻
						if(TimeUtil.isSameDayOfMillis(System.currentTimeMillis(), date.getTime())){
							//列表中自上至下选取第一条
							List<PreloadItem> tempList = tempMap.get(key);
							if(tempList == null){
								tempList = new ArrayList<PreloadItem>();
								tempList.add(item);
								tempMap.put(key, tempList);
							} else {
								if(tempList.contains(item)){
									continue;
								}
								tempList.add(item);
							}
//							sendLocNews(item,pushKey);
							indexCount ++;
							LOG.info(key+"Just send front news ");
						}
						
						//时间超过48小时的默认break
						long lifeTime = System.currentTimeMillis() - date.getTime();
						if(lifeTime > 48 * 60 * 60 * 1000){
							List<PreloadItem> tempList = tempMap.get(key);
							if(tempList != null && !tempList.isEmpty()){
								LOG.info(key+" news Num : "+tempList.size());
							} else {
								LOG.info(key+" did not update today ~");
							}
							break;
						}
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			List<PreloadItem> tempList = tempMap.get(key);
			if(tempList != null && !tempList.isEmpty()){
				for(PreloadItem pItem : tempList){
					sendLocNews(pItem,pushKey);
					if(!key.equals(pushKey)){
						sendLocNews(pItem, key);
					}
				}
			}
			
		}
		
		Gson gson = new Gson();
		String test = gson.toJson(tempMap);
		LOG.info("Cover City num : "+tempMap.size());
		LOG.info(test);
//		System.out.println(test);
		
	}
	
	class UpdateClass{
		String docId ;
		String docTitle;
		String docTm;
		String keywords;
		String docSource;
	}
	
	private void sendLocNews(PreloadItem item,String city){
		String url = "http://10.32.21.57:8080/pushInterface/addPushItem";
		//转换id
		String imcpId = getItemImcpId(item);
		if(imcpId == null){
			LOG.error("Get imcpId error"+item.getFitem().getDocId());
			return;
		}
		String title = item.getFitem().getTitle();
		String time = item.getFitem().getDate();
		String source = queryDocSourceFromSolr(item);
		UpdateClass uItem = new UpdateClass();
		uItem.docId = imcpId;
		uItem.docTitle = title;
		uItem.docTm = time;
		uItem.keywords = city;
		uItem.docSource = source;
		Gson gson = new Gson();
		
		String param = "param="+gson.toJson(uItem);
		String result = sendPost(url, param);
		LOG.info(city+ " : "+title);
		LOG.info(result);
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
	
	private String queryDocSourceFromSolr(PreloadItem item){
		String docId = item.getFitem().getDocId();
		String source = "";
		String queryStr = "itemid:("+docId+")";
		String solrUrl = "http://10.32.28.119:8081/solr46/item/";
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		
		try {
			ItemSorlServerClient client = ItemSorlServerClient.getInstance();
			HttpSolrServer server = client.getServer(solrUrl);
			QueryResponse qr = server.query(query);
			SolrDocumentList doclist = qr.getResults();
			Iterator<SolrDocument> it = doclist.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				source  = (String) doc.get("source");
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("",e);
		}
		
		return source;
				
	}
	
	/*
	 * 从redis中获取本地数据
	 */
	private Map<String, List<PreloadItem>> getLocNewsFromRedis(){
		Map<String, List<PreloadItem>> newsMap = new HashMap<String, List<PreloadItem>>();
		Jedis jedis = new Jedis("10.90.1.57", 6379, 10000);
		jedis.select(3);
		Set<String> citySet = jedis.keys("*");
		for(String city : citySet){
			try{
				String status = jedis.get(city);
				if(status == null){
					LOG.error("get "+city+" from redis error ~");
					continue;
				}
				Gson gson = new Gson();
				List<PreloadItem>  newslist = gson.fromJson(status, new TypeToken<List<PreloadItem>>() {
				}.getType());
				newsMap.put(city, newslist);
			} catch (Exception e){
				LOG.error("get "+city+" from redis error ~",e);
				continue;
			}
		}
		return newsMap;
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
	
	public static void main(String[] args){
		LocNewsSelect lo = new LocNewsSelect();
		ScheduledExecutorService scheduleThreadPool = Executors.newScheduledThreadPool(1);
		scheduleThreadPool.scheduleWithFixedDelay(lo, 0, 20, TimeUnit.MINUTES);
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		selectLocNews();
	}
}
