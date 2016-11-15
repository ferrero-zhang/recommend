package com.ifeng.iRecommend.likun.hotpredict;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.ifeng.commen.classifyClient.ClassifierClient;
import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.dingjw.front_rankModel.RankItem;
import com.ifeng.iRecommend.dingjw.front_rankModel.appItemFront;
import com.ifeng.iRecommend.dingjw.front_rankModel.appRankModel;
import com.ifeng.iRecommend.dingjw.front_rankModel.itemFront;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.channelsParser;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

/**
 * <PRE>
 * 作用 : 
 *   专用于凤凰新闻客户端的文章权重预测，根据PC端的实时PV，预测客户端文章的权重
 *   问题：如果item库的历史文章的权重发生了上升（根据PV来算），那么得考虑在travelback中体现出来；
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
 *          1.0          2014-06-26        dingjw          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

class pcItem {
	protected int pv;
	protected String weight;
	protected String channel;
}

class tongjiField {
	protected int pv;
	protected String url;
	protected String id;
	protected String channel;
	protected String type;
	protected String weight;

}

public class heatPredict {

	private static final Log LOG = LogFactory.getLog("heatPredict");

	private OSCache osCache;

	private static heatPredict instance = new heatPredict();

	private static final String CACHE_NAME = "OSCACHE_heatPredict";

	// 从手凤得到的等级库\从PC得到的等级库\从客户端得到的等级库
	private HashMap<String, tongjiField> sfitemMap,pcitemMap,appitemMap;
	// 从PC网站视觉抓取得到的热度评价库，来自hdfs,路径是：hdfs://10.32.21.111:8020/projects/zhineng/pcHotPredict/pcHotLevel
	private HashMap<String, tongjiField> pcVisualItemMap;
	
	
	private heatPredict() {
		// oscache键值的存活周期
		int refreshInterval = 90 * 24 * 60 * 1000;

		LOG.info("refreshInterval = " + refreshInterval);
		// 初始OSCache_MOOD;
		osCache = new OSCache("conf/oscache_heatPredict.properties",
				CACHE_NAME, refreshInterval);
		// osCache = new OSCache("oscache_itemOP.properties",CACHE_NAME,
		// 1*1000);

		LOG.info(CACHE_NAME + " cache creating...");
		
		// 从手凤得到的等级库
		sfitemMap = new HashMap<String, tongjiField>();
		// 从PC得到的等级库
		pcitemMap = new HashMap<String, tongjiField>();
		// 从客户端得到的等级库
		appitemMap = new HashMap<String, tongjiField>();
		// 从PC页面采集得到的视觉等级库
		pcVisualItemMap = new HashMap<String, tongjiField>();

	}

	public static heatPredict getInstance() {
//		if(instance == null)
//			instance = new heatPredict();
		return instance;
	}

	/**
	 * 规范化url 注意：
	 * 
	 * @param srcUrl
	 * @return result
	 */
	private static String normalizedUrl(String srcUrl) {
		String result = srcUrl;
		try {
			if (srcUrl.matches(".*html.*")) {
				Pattern pattern = Pattern.compile("http.*(s?)html");
				Matcher matcher = pattern.matcher(srcUrl);
				if (matcher.find()) {
					result = matcher.group(0);
				}
			} else {
				if (srcUrl.contains("http://"))
					result = srcUrl.substring(srcUrl.lastIndexOf("http://"));
				else
					result = "http://" + srcUrl;
			}
		} catch (Exception e) {
			LOG.error("ERROR normalizedUrl ", e);
		}

		result = new String(result.replaceAll("_\\d{1,3}\\.", "_0\\.").trim());
		// result.replaceAll("", "_0.html");
		return result.trim();
	}

	/**
	 * 获取统计系统的访问URL，如http://10.32.21.55/zhineng/doc/2014-06-25/1005.log
	 * 注意：因统计系统每十分钟更新一次，所以当前能访问到的是10分钟前的日志，注意preMinutes应设为如10,20,30.。。
	 * 
	 * @param requestUrl
	 *            统计系统访问路径
	 * @param preMinutes
	 *            提前时间，以分钟为单位
	 * @return srcUrl
	 */
	public static String getTongjiURL(String requestUrl, int preMinutes) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE, -preMinutes);
		String day = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
		String timeStr = new SimpleDateFormat("HHmm").format(cal.getTime());
		String readTime = timeStr.substring(0, 3) + "0";
		String srcUrl = requestUrl + day + readTime + ".xml";// 例如：requestUrl
																// =
																// "http://10.32.21.21/zhineng/doc/2013-11-25/1005.log";
		return srcUrl;
	}

	/**
	 * 从统计系统获取PC文章实时pv, 并解析 注意：
	 * 
	 * @param pvLevel
	 *            浏览量阈值
	 * @return itemFrontList
	 * @throws FileNotFoundException
	 */

	public static HashMap<String, Integer> readTongjiPC(String requestUrl)
			throws FileNotFoundException {

		URL url = null;
		try {
			url = new URL(requestUrl);
		} catch (Exception e) {
			LOG.error("ERROR getTongjiURL", e);
			return null;
		}

		HashMap<String, Integer> urlPvMap = new HashMap<String, Integer>();
		HashMap<String, String> curlMap = new HashMap<String, String>();

		LOG.info("Get Items From Tongji: " + url);
		BufferedReader r = null;

		// 读取10分钟前的统计系统url
		URLConnection con = null;
		try {
			con = url.openConnection();
			r = new BufferedReader(new InputStreamReader(con.getInputStream(),
					"UTF-8"));
		} catch (FileNotFoundException e) {
			// 若10分钟前没有日志，则访问20分钟前的
			LOG.warn("WARN", e);
			throw e;
		} catch (Exception e) {
			LOG.warn("WARN", e);
			return null;
		}

		// 读取解析统计pv文件
		String line = "";
		try {
			line = r.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.warn("WARN", e);
			return null;
		}

		while (line != null) {
			// System.out.println(line);
			try {
				String splits[] = line.split("\t");
				if (splits.length == 4) {

					String urlStr = splits[1];// 文章url
					String cUrlStr = splits[2];// 文章所在目录url
					int pv = Integer.valueOf(splits[3]);// 文章pv

					String normUrl = normalizedUrl(urlStr);
					if (urlPvMap.containsKey(normUrl)) {
						if (pv > urlPvMap.get(normUrl)) {
							urlPvMap.put(normUrl, pv);
						}
					} else {
						urlPvMap.put(normUrl, pv);
						curlMap.put(normUrl, cUrlStr);
					}
				}
			} catch (Exception e) {
				LOG.warn("WARN", e);
			} finally {
				try {
					line = r.readLine();
				} catch (IOException e) {
					LOG.warn("WARN", e);
					break;
				}
			}
		}

		try {
			r.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.warn("WARN", e);
		}

		return urlPvMap;

	}

	/**
	 * 从统计系统获取客户端文章的实时pv 注意：
	 * 
	 * @return idPvMap
	 */

	public static LinkedHashMap<String, Integer> readTongjiApp() {
		String requestUrl = "http://tongji.ifeng.com:9090/appnewstop/loadNews?ch=all";

		URL url = null;
		try {
			url = new URL(requestUrl);
		} catch (MalformedURLException e) {
			LOG.error("ERROR getTongjiURL", e);
			return null;
		} catch (Exception e) {
			LOG.error("ERROR getTongjiURL", e);
			return null;
		}
		//@test
		LOG.info("test for readTJurl 0");
		
		LinkedHashMap<String, Integer> idPvMap = new LinkedHashMap<String, Integer>();

		LOG.info("Get realtime pv From Tongji: " + url);
		BufferedReader r = null;

		// 读取10分钟前的统计系统url
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			 // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive"); 
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.connect();
            if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
				LOG.error("get failed ErrorCode="+conn.getResponseCode());
				return null;
			} 	
			r = new BufferedReader(new InputStreamReader(conn.getInputStream(),
					"UTF-8"));
		} catch (Exception e) {
			LOG.warn("WARN", e);
			return null;
		}

		//@test
		LOG.info("test for readTJurl 1");
		
		// 读取解析统计pv文件
		String line = "";
		try {
			line = r.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.warn("WARN", e);
			return null;
		}

		//@test
		LOG.info("test for readTJurl 2");
		
		while (line != null) {
			try {
				if (line.startsWith("<rec ")) {
					line = line.substring(line.indexOf("<rec ") + 5,
							line.length() - 2);

					
					String splits[] = line.split(" ");
					if (line.startsWith("pid") && splits.length == 5) {
						String rawid = splits[0].substring(5,
								splits[0].length() - 1);
						String id;
						if (rawid.contains("_"))
							id = rawid.split("_")[1];
						else
							id = rawid;
						
						int pv = 0;
						if(splits[2].startsWith("pv="))
							pv = Integer.valueOf(splits[2].substring(4,
								splits[2].length() - 1));

						if (idPvMap.containsKey(id)) {
							int maxpv = pv > idPvMap.get(id) ? pv : idPvMap
									.get(id);
							idPvMap.put(id, maxpv);
						} else
							idPvMap.put(id, pv);

					}
				}

			} catch (Exception e) {
				LOG.warn("WARN", e);
			} finally {
				try {
					line = r.readLine();
				} catch (IOException e) {
					LOG.warn("WARN", e);
					break;
				}
			}
		}

		try {
			r.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.warn("WARN", e);
		}

		return idPvMap;

	}

	/**
	 * 从统计系统获取PC文章实时pv, 并解析 注意：
	 * 
	 * @param pvLevel
	 *            浏览量阈值
	 * @return itemFrontList
	 * @throws IOException
	 */

	private static ArrayList<tongjiField> readTongjiSF(String requestUrl)
			throws IOException {

		URL url = null;
		try {
			url = new URL(requestUrl);
		} catch (Exception e) {
			LOG.error("ERROR getTongjiURL", e);
			return null;
		}

		ArrayList<tongjiField> al_tj = new ArrayList<tongjiField>();

		LOG.info("Get Items From Tongji: " + url);
		BufferedReader r = null;

		// 读取10分钟前的统计系统url
		URLConnection con = null;
		try {
			con = url.openConnection();
			r = new BufferedReader(new InputStreamReader(con.getInputStream(),
					"UTF-8"));
		} catch (FileNotFoundException e) {
			// 若10分钟前没有日志，则访问20分钟前的
			LOG.warn("WARN", e);
			throw e;
		} catch (Exception e) {
			LOG.warn("WARN", e);
			return null;
		}

		// 读取解析统计pv文件
		String line = "";

		while ((line = r.readLine()) != null) {
			// <rec type="img"
			// url="http://i.ifeng.com/news/pic/jingxuan1/news?aid=85671661"
			// num="75456" />
			try {
				String type = "", item_url = "", sPV = "";
				int b = 0, e = 0;
				b = line.indexOf("\"");
				e = line.indexOf("\"", b + 1);
				if (e <= b)
					continue;
				type = line.substring(b + 1, e);
				b = line.indexOf("\"", e + 1);
				e = line.indexOf("\"", b + 1);
				if (e <= b)
					continue;
				item_url = line.substring(b + 1, e);

				// 过滤掉无用的url
				if (item_url.indexOf("i.ifeng.com") < 0
						&& item_url.indexOf("wap.ifeng.com") < 0
						&& item_url.indexOf("3g.ifeng.com") < 0)
					continue;

				b = line.indexOf("\"", e + 1);
				e = line.indexOf("\"", b + 1);
				if (e <= b)
					continue;
				sPV = line.substring(b + 1, e);
				int pv = Integer.valueOf(sPV);// 文章pv
				String normUrl = normalizedUrl(item_url);
				tongjiField tfd = new tongjiField();
				tfd.type = new String(type);
				tfd.url = normUrl;
				tfd.pv = pv;
				tfd.id = normUrl.substring(normUrl.lastIndexOf("=") + 1);
				if (!tfd.id.matches("\\d{5,12}"))
					continue;
				al_tj.add(tfd);
			} catch (Exception e) {
				LOG.warn("WARN", e);
			} finally {

			}
		}

		try {
			r.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.warn("WARN", e);
		}

		return al_tj;

	}

	/**
	 * 对item根据PC端的实时pv设定权重； 流程和功能如下： 1）对PC端的url根据频道进行分类； 2）分频道根据pv对item由大到小进行排序；
	 * 3）基于频道热度，根据pv落差和百分比来取权重：
	 * 在前10个item中取pv落差最大的item及之前的item，权重设为B；最大落差下一个item，
	 * 以及pv大于该item的pv的20%的item，权重设为C；其余设为D； 4）通过客户端文章对应的url，更新相应权重
	 * 
	 * @param appItemFrontList
	 *            来自客户端的item列表
	 * @param urlPVMap_tongjiPC
	 *            来自PC的文章pv哈希表
	 * 
	 */
	public void rankItemByPCPv(ArrayList<appItemFront> appItemFrontList,
			HashMap<String, Integer> urlPVMap_tongjiPC) {
		if (appItemFrontList == null || appItemFrontList.isEmpty()
				|| appItemFrontList.size() == 0) {
			return;
		}

		HashMap<String, pcItem> pcitemMap = new HashMap<String, pcItem>();

		HashMap<String, ArrayList<pcItem>> pcitem_channelMap = new HashMap<String, ArrayList<pcItem>>();

		for (Entry<String, Integer> entry : urlPVMap_tongjiPC.entrySet()) {
			if (entry.getValue() < 10)
				continue;
			pcItem item = new pcItem();
			item.pv = entry.getValue();
			String channel = null;
			// 先在缓存中查询频道
			try {
				channel = (String) instance.osCache.get(entry.getKey());
			} catch (Exception e) {
				LOG.info("ERROR oscache can't get channel: " + entry.getKey());
				channel = null;
			}
			// 如果channel为null，根据前端映射树，获取channel
			if (channel == null) {
				try {
					channel = channelsParser
							.getInstance(ItemOperation.ItemType.APPITEM)
							.getTransChannel(entry.getKey(), 0).split("-")[0];
					if (channel == null || channel.isEmpty())
						channel = "other";
					// System.out.println(entry.getKey()+": "+channel);
					instance.osCache.put(entry.getKey(), channel);
				} catch (Exception e) {
					LOG.info("rankItemByPCPv cannot find channel: "
							+ entry.getKey());
					LOG.error("[ERROR]", e);
					channel = "other";
				}
			}

			item.channel = channel;
			item.weight = "D";
			pcitemMap.put(entry.getKey(), item);

			// 对appItemFront根据频道进行分类
			ArrayList<pcItem> itemList;
			if ((itemList = pcitem_channelMap.get(channel)) != null) {
				itemList.add(item);
			} else {
				itemList = new ArrayList<pcItem>();
				itemList.add(item);
				pcitem_channelMap.put(channel, itemList);
			}
		}

		// 分频道设定权重
		for (Entry<String, ArrayList<pcItem>> entry : pcitem_channelMap
				.entrySet()) {
			ArrayList<pcItem> itemList = entry.getValue();
			if (itemList.isEmpty() || itemList.size() == 0) {
				continue;
			}

			// 根据落差和百分比来取权重
			if (itemList.size() > 1) {
				// 分频道根据pv对item进行排序
				Collections.sort(itemList, new Comparator<pcItem>() {
					public int compare(pcItem item1, pcItem item2) {
						int result = item2.pv - item1.pv;
						return result;
					}
				});
				// 计算前10个item的落差，获取最大落差和最大落差的位置
				float max_gap_rate = 0;
				int gapLocation = -1;
				int index = 0;
				for (; index < itemList.size() - 1 && index < 10; index++) {
					int gap = itemList.get(index).pv
							- itemList.get(index + 1).pv;
					float gap_rate = gap / (float) (itemList.get(index).pv);

					// 落差太小可以忽略
					if (gap <= 10 && gap_rate < 0.5)
						continue;

					if (gap_rate >= max_gap_rate) {
						gapLocation = index;
						max_gap_rate = gap_rate;
					}

				}

				// 没有算出落差，怎么处理？？，后续
				if (gapLocation < 0) {
					// ...
					continue;
				}

				// 落差最大的位置及之前的item的权重设为B
				for (int i = 0; i <= gapLocation; i++)
					itemList.get(i).weight = "B";

				int pv_C = itemList.get(gapLocation + 1).pv;
				for (int i = gapLocation + 1; i < itemList.size(); i++) {
					if (itemList.get(i).pv > pv_C * 0.1)
						itemList.get(i).weight = "C";
					else
						break;
				}
				/*
				 * System.out.println(entry.getKey()); for(itemFront itemTest:
				 * itemList){
				 * System.out.println(itemTest.getTitle()+" "+itemTest
				 * .getUrl()+" "+itemTest.getPv()+" "+itemTest.getWeight()); }
				 */
			} else
				itemList.get(0).weight = "B";
		}
		// 根据PC端的权重，更新客户端item的权重
		for (appItemFront appItemF : appItemFrontList) {
			// 编辑给的文章不作处理
			if (appItemF.getOthers().indexOf("fromEditor") >= 0)
				continue;
			if (pcitemMap.containsKey(appItemF.getUrl())) {
				appItemF.setWeight(pcitemMap.get(appItemF.getUrl()).weight);
				LOG.info("got one:" + appItemF.getUrl());
			}
		}

	}


	/**
	 *
	 * 根据hdfs上的视觉采集信息，生成新的权重分级lib； 
	 * ex path：hdfs://10.32.21.111:8020/projects/zhineng/pcHotPredict/pcHotLevel
	 */
	public void updatePCVisualRankLib() {
		// HDFS文件读取
		Configuration conf = null;
		FileSystem hdfs = null;
		conf = new Configuration();
		conf.set("fs.defaultFS", "hdfs://10.32.21.111:8020/");// 指定hdfs
		conf.set("hadoop.job.user", "hdfs");
		try {
			hdfs = FileSystem.get(conf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("",e);
			return;
		}
		Path path = new Path(fieldDicts.pcVisualHotFile);
		FSDataInputStream fsr = null;
		try {
			fsr = hdfs.open(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("",e);
			return;
		}
		BufferedReader bis = null;
		try {
			bis = new BufferedReader(new InputStreamReader(fsr,
					"utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			LOG.error("",e);
			return;
		}
		
		pcVisualItemMap.clear();
		
		String line = null;
		try {
			while ((line = bis.readLine()) != null) {
				String[] secs = line.split("\t");
				if (secs.length < 2)
					continue;
				
				try {
					String itemID = secs[0].trim();
					String hotLevel = secs[1].trim();
					if(!(itemID.matches("\\d{6,20}")))
						continue;
					tongjiField tfd = new tongjiField();
					tfd.type = "";
					tfd.url = "";
					tfd.pv = 1000;
					tfd.id = new String(itemID);
					tfd.weight = new String(hotLevel);
					pcVisualItemMap.put(tfd.id, tfd);
				} catch (Exception e) {
					LOG.error("",e);
					continue;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("",e);
		}
	}
	
	/**
	 * 根据手凤的实时pv，生成新的权重分级lib； 流程和功能如下： 
	 * 1）对手凤的url根据频道进行分类；
	 * 2）分频道根据pv对item由大到小进行排序； 
	 * 3）基于频道热度，根据pv落差和百分比来取权重：
	 * 3.1)默认PV最高的那个是A；
	 * 3.2)后续9个内落差满足50%的index，之前的都是A；否则就取最高PV的80%之前的为A；
	 * 3.3)A之后的是B，之后找落差满足50%的index，之前的都是B；否则取第一个B的PV的50%之前的为B；
	 * 3.3）B之后的为C，之后落差满足50%的index，之前的都是C；否则取第一个C的PV的30%之前的为C;
	 * 3.4）其它全是D；
	 * 
	 * 
	 * 在前10个item中取pv落差最大的item及之前的item，权重设为B；最大落差下一个item，
	 * 以及pv大于该item的pv的20%的item，权重设为C；其余设为D； 4）通过客户端文章对应的url，更新相应权重
	 * 
	 * @param appItemFrontList
	 *            来自客户端的item列表
	 * 
	 */
	public void updateSFRankLib() {
		String requestUrl = heatPredict.getTongjiURL(
				fieldDicts.itemFromTongjiSF, 10);
		ArrayList<tongjiField> al_tj = null;
		try {
			al_tj = heatPredict.readTongjiSF(requestUrl);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOG.warn("", e);
			LOG.info("retry:");
			// 重试20分钟前的文件
			requestUrl = getTongjiURL(fieldDicts.itemFromTongjiSF, 20);
			try {
				al_tj = heatPredict.readTongjiSF(requestUrl);
				LOG.info("tongjiSF size: " + al_tj.size());
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				LOG.warn("", e);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				LOG.warn("", e);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("", e);
		}

		if(al_tj == null || al_tj.isEmpty())
		{
			LOG.error("al_tj == null");
			return;
		}
		
		LOG.info("tongjiSF size: " + al_tj.size());
		
		sfitemMap.clear();
		
		HashMap<String, ArrayList<tongjiField>> sfitem_channelMap = new HashMap<String, ArrayList<tongjiField>>();
		
		ItemOperation itemOP = ItemOperation.getInstance();
		itemOP.setItemType(ItemType.APPITEM);
		
		for (tongjiField tfd : al_tj) {
			if (tfd.pv < 10)
				continue;

			if (tfd.type.equals("pic"))
				tfd.pv = tfd.pv / 10;

			String channel = null;
			// 根据前端映射树，获取channel
			if (channel == null) {
				try {
					channel = channelsParser
							.getInstance(ItemOperation.ItemType.APPITEM)
							.getTransChannel(tfd.url, 1).split("-")[0];
					if (channel == null || channel.isEmpty()
							|| channel.indexOf("notopic") >= 0)
						channel = "other";
				} catch (Exception e) {
					LOG.info("rankItemBySFPv cannot find channel: " + tfd.url);
					LOG.error("[ERROR]", e);
					channel = "other";
				}
				
				String channel2 = "";
				Item item = itemOP.getItem(tfd.id);
				try {
					channel2 = channelsParser
							.getInstance(ItemOperation.ItemType.APPITEM)
							.getTransChannelByItem(item, 1).split("-")[0];
					if (channel2 == null || channel2.isEmpty()
							|| channel2.indexOf("notopic") >= 0)
						channel2 = "other";
				} catch (Exception e) {
					LOG.info("rankItemBySFPv cannot find channel: " + tfd.url);
					//LOG.error("[ERROR]", e);
					channel2 = "other";
				}
				
				if(channel.isEmpty() || channel.equals("other")
						|| channel.equals("news"))
					channel = channel2;
				
				//调用分类算法来修正,如果content内容很短，不太靠谱，那么直接设置content为空，分类结果也为error
				if(item!=null &&(channel.isEmpty()
						||channel.equals("other")
						||channel.equals("news")
						||channel.equals("blog")
						||channel.equals("iclient")))
				{
					String content = item.getContent();
					if(content == null || content.trim().length() <= 192)
						content = "";
					String categorys = ClassifierClient.predict(
							item.getTitle(), content,
							"",
							"com.ifeng.secondLevelMapping.secondMappingForDiversity",
							null);
			
					if(categorys.startsWith("error")
							||categorys.startsWith("client.error")){
						//...
					}else{
						String[] secs = categorys.split("\\s");
						channel = secs[0];
					}
				
				}
			}

			tfd.channel = channel;
			tfd.weight = "D";

			// 优先取PV排名靠前的,ID会有重复
			if (sfitemMap.containsKey(tfd.id))
				continue;

			sfitemMap.put(tfd.id, tfd);

			// 对appItemFront根据频道进行分类
			ArrayList<tongjiField> itemList = null;
			if ((itemList = sfitem_channelMap.get(channel)) != null) {
				itemList.add(tfd);
			} else {
				itemList = new ArrayList<tongjiField>();
				itemList.add(tfd);
				sfitem_channelMap.put(channel, itemList);
			}
		}

		// 分频道设定权重
		for (Entry<String, ArrayList<tongjiField>> entry : sfitem_channelMap
				.entrySet()) {
			ArrayList<tongjiField> itemList = entry.getValue();
			if (itemList.isEmpty() || itemList.size() == 0) {
				continue;
			}

			// 根据落差和百分比来取权重
			// 分频道根据pv对item进行排序
			Collections.sort(itemList, new Comparator<tongjiField>() {
				public int compare(tongjiField item1, tongjiField item2) {
					int result = item2.pv - item1.pv;
					return result;
				}
			});

			// 找A：
			int indexA = findNextSep(itemList, 0, 9, 0.8);
			// 落差最大的位置及之前的item的权重设为B
			for (int i = 0; i <= indexA; i++)
				itemList.get(i).weight = "A";

			// 找B：
			int indexB = findNextSep(itemList, indexA + 1, itemList.size() - 1,
					0.8);
			if(indexB <= indexA)
				continue;
			// 落差最大的位置及之前的item的权重设为B
			for (int i = indexA + 1; i <= indexB; i++)
				itemList.get(i).weight = "B";

			// 找C：
			int indexC = findNextSep(itemList, indexB + 1, itemList.size() - 1,
					0.8);
			if(indexC <= indexB)
				continue;
			// 落差最大的位置及之前的item的权重设为B
			for (int i = indexB + 1; i <= indexC; i++)
				itemList.get(i).weight = "C";

			// 找D
			for (int i = indexC + 1; i < itemList.size(); i++)
				itemList.get(i).weight = "D";

		}

		// @test，输出各个分类的ABCD排列
		for (Entry<String, ArrayList<tongjiField>> entry : sfitem_channelMap
				.entrySet()) {
			ArrayList<tongjiField> itemList = entry.getValue();
			if (itemList.isEmpty() || itemList.size() == 0) {
				continue;
			}
			
//			if(entry.getKey().equals("taiwan"))
//			{
				LOG.info(entry.getKey());
				for (tongjiField pi : itemList)
					LOG.info(pi.id + "==>" + pi.channel + ":" + pi.pv + ":"
							+ pi.weight + "   " + pi.url);
				LOG.info("--------");
//			}

		}
		// @^test

	}

	/**
	 * 对item根据热度table设定权重；
	 * 
	 * @param appItemFrontList
	 *            来自客户端的item列表
	 * 
	 */
	public void rankItemsHotLevel(ArrayList<appItemFront> appItemFrontList) {
		if (appItemFrontList == null || appItemFrontList.isEmpty()
				|| appItemFrontList.size() == 0) {
			return;
		}

		if (appitemMap == null || appitemMap.isEmpty())
			return;

		// 根据SF端的权重，更新客户端item的权重
		for (appItemFront appItemF : appItemFrontList) {
			// 编辑给的文章不作处理
			if (appItemF.getOthers().indexOf("fromEditor") >= 0)
				continue;
			if (sfitemMap.containsKey(appItemF.getImcpID())) {
				appItemF.setWeight(sfitemMap.get(appItemF.getImcpID()).weight);
				LOG.info("got one from sf:" + appItemF.getImcpID() + " "
						+ appItemF.getWeight());
			}
			if (appitemMap.containsKey(appItemF.getImcpID())) {
				LOG.info("find one from app:" + appItemF.getImcpID() + " "
						+ appItemF.getWeight());
				if(appitemMap.get(appItemF.getImcpID()).weight.compareTo(appItemF.getWeight()) < 0)
				{	
					appItemF.setWeight(appitemMap.get(appItemF.getImcpID()).weight);
					LOG.info("app is more good,got one from app:" + appItemF.getImcpID() + " "
							+ appItemF.getWeight());
				}
			}
			if (pcVisualItemMap.containsKey(appItemF.getImcpID())) {
				LOG.info("find one from pcVisualItemMap:" + appItemF.getImcpID() + " "
						+ appItemF.getWeight());
				if(pcVisualItemMap.get(appItemF.getImcpID()).weight.compareTo(appItemF.getWeight()) < 0)
				{	
					appItemF.setWeight(pcVisualItemMap.get(appItemF.getImcpID()).weight);
					LOG.info("pcVisualItemMap is more good,got one from app:" + appItemF.getImcpID() + " "
							+ appItemF.getWeight());
				}
			}
		}

	}

	/**
	 * 对item根据热度table设定权重；
	 * 
	 * @param RankItem
	 *            来自客户端的item
	 * 
	 */
	public String rankOneItemHotLevel(RankItem rankItem) {
		if (rankItem == null || rankItem.getSimIDList().isEmpty()) {
			return "";
		}
		String w_sf = "D", w_app = "D",w_pcv = "D",w_max = "D",id_max = rankItem.getID();
		for(String imcp_id:rankItem.getSimIDList()){
			if (appitemMap != null && !appitemMap.isEmpty()) {
				if (appitemMap.containsKey(imcp_id))
				{
					w_app = appitemMap.get(imcp_id).weight;
					if (w_app.compareTo(w_max) < 0) {
						w_max = w_app;
						id_max = imcp_id;
					}
				}
			}

			if (sfitemMap != null && !sfitemMap.isEmpty()) {
				if (sfitemMap.containsKey(imcp_id))
				{
					w_sf = sfitemMap.get(imcp_id).weight;
					if (w_sf.compareTo(w_max) < 0){
						w_max = w_sf;
						id_max = imcp_id;
					}
				}
			}
			
			if (pcVisualItemMap != null && !pcVisualItemMap.isEmpty()) {
				if (pcVisualItemMap.containsKey(imcp_id))
				{
					w_pcv = pcVisualItemMap.get(imcp_id).weight;
					if (w_pcv.compareTo(w_max) < 0){
						w_max = w_pcv;
						id_max = imcp_id;
					}
				}
			}
			
		}
		
		return w_max+" "+id_max;

	}
	
	
//	/**
//	 * 对item根据手凤的实时pv设定权重；
//	 * 
//	 * @param appItemFrontList
//	 *            来自客户端的item列表
//	 * 
//	 */
//	public void rankItemBySFPv(ArrayList<appItemFront> appItemFrontList) {
//		if (appItemFrontList == null || appItemFrontList.isEmpty()
//				|| appItemFrontList.size() == 0) {
//			return;
//		}
//
//		if (sfitemMap == null || sfitemMap.isEmpty())
//			return;
//
//		// 根据SF端的权重，更新客户端item的权重
//		for (appItemFront appItemF : appItemFrontList) {
//			// 编辑给的文章不作处理
//			if (appItemF.getOthers().indexOf("fromEditor") >= 0)
//				continue;
//			if (sfitemMap.containsKey(appItemF.getImcpID())) {
//				appItemF.setWeight(sfitemMap.get(appItemF.getImcpID()).weight);
//				LOG.info("got one:" + appItemF.getImcpID() + " "
//						+ appItemF.getWeight());
//			}
//		}
//
//	}
//
//	/**
//	 * 对item根据手凤的实时pv设定权重；
//	 * 
//	 * @param appItemFrontList
//	 *            来自客户端的item列表
//	 * 
//	 */
//	public String rankItemBySFLib(String imcp_id) {
//		if (imcp_id == null || imcp_id.isEmpty()) {
//			return "";
//		}
//
//		if (sfitemMap == null || sfitemMap.isEmpty())
//			return "";
//
//		if (sfitemMap.containsKey(imcp_id)) {
//			return sfitemMap.get(imcp_id).weight;
//		}else
//			return "";
//
//	}
	
	/**
	 * 查找下一个分割点；
	 * 
	 * @param appItemFrontList
	 *            来自客户端的item列表
	 * 
	 */
	private int findNextSep(ArrayList<tongjiField> itemList, int begin,
			int end, double PVFlagRate) {
		if(begin < 0)
			return -1;
		if(begin > end)
			return -1;
		
		// 默认第一个是
		int index_rt = -1, PV = itemList.get(begin).pv;
		if (end < 0)
			end = itemList.size() - 1;
		if(end >= itemList.size())
			end = itemList.size() - 1;
		// 计算[begin,end]的落差，获取第一个满足条件的落差
		int index = begin;
		for (; index < end; index++) {
			int gap = itemList.get(index).pv - itemList.get(index + 1).pv;
			float gap_rate = gap / (float) (itemList.get(index).pv);

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
				if (itemList.get(i).pv < PV * PVFlagRate)
				{
					index_rt = i - 1;
					break;
				}
			}
		}

		if (index_rt < 0)
			index_rt = itemList.size() - 1;

		return index_rt;
	}
	
	
	
	/**
	 * 根据APP的实时pv，生成新的权重分级lib； 流程和功能如下： 
	 * 1）对手凤的url根据频道进行分类；
	 * 2）分频道根据pv对item由大到小进行排序； 
	 * 3）基于频道热度，根据pv落差和百分比来取权重：
	 * 3.1)默认PV最高的那个是A；
	 * 3.2)后续9个内落差满足50%的index，之前的都是A；否则就取最高PV的80%之前的为A；
	 * 3.3)A之后的是B，之后找落差满足50%的index，之前的都是B；否则取第一个B的PV的80%之前的为B；
	 * 3.3）B之后的为C，之后落差满足50%的index，之前的都是C；否则取第一个C的PV的80%之前的为C;
	 * 3.4）其它全是D；
	 * 
	 * 
	 * 在前10个item中取pv落差最大的item及之前的item，权重设为B；最大落差下一个item，
	 * 以及pv大于该item的pv的20%的item，权重设为C；其余设为D； 4）通过客户端文章对应的url，更新相应权重
	 * 
	 * @param appItemFrontList
	 *            来自客户端的item列表
	 * 
	 */
	public void updateAPPRankLib() {
		String requestUrl = heatPredict.getTongjiURL(
				fieldDicts.itemFromTongjiAPP, 10);
		ArrayList<tongjiField> al_tj = null;
		try {
			al_tj = heatPredict.readTongjiAPP(requestUrl);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LOG.warn("", e);
			LOG.info("retry:");
			// 重试20分钟前的文件
			requestUrl = getTongjiURL(fieldDicts.itemFromTongjiAPP, 20);
			try {
				al_tj = heatPredict.readTongjiAPP(requestUrl);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				LOG.warn("", e);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				LOG.warn("", e);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("", e);
		} 
		
		if(al_tj == null || al_tj.isEmpty())
		{
			LOG.error("al_tj == null");
			return;
		}
		
		LOG.info("tongjiAPP size: " + al_tj.size());
		
		appitemMap.clear();
		
		HashMap<String, ArrayList<tongjiField>> sfitem_channelMap = new HashMap<String, ArrayList<tongjiField>>();
		
		ItemOperation itemOP = ItemOperation.getInstance();
		itemOP.setItemType(ItemType.APPITEM);
		
		for (tongjiField tfd : al_tj) {
			if (tfd.pv < 10)
				continue;

			if (tfd.type.equals("pic"))
				tfd.pv = tfd.pv / 10;

			String channel = null;
			// 根据前端映射树，获取channel
			if (channel == null) {
				try {
					channel = channelsParser
							.getInstance(ItemOperation.ItemType.APPITEM)
							.getTransChannel(tfd.url, 1).split("-")[0];
					if (channel == null || channel.isEmpty()
							|| channel.indexOf("notopic") >= 0)
						channel = "other";
				} catch (Exception e) {
					LOG.info("rankItemByAPPPV cannot find channel1: " + tfd.url);
					LOG.error("[ERROR]", e);
					channel = "other";
				}
				
//				//@test
//				System.out.println(tfd.url+" "+ channel);
				
				String channel2 = "";
				Item item = itemOP.getItem(tfd.id);
				
				//@test
				if(item != null && item.getUrl() != null)
					tfd.url = new String(item.getUrl());
				
				try {
					channel2 = channelsParser
							.getInstance(ItemOperation.ItemType.APPITEM)
							.getTransChannelByItem(item, 1).split("-")[0];
					if (channel2 == null || channel2.isEmpty()
							|| channel2.indexOf("notopic") >= 0)
						channel2 = "other";
				} catch (Exception e) {
					LOG.info("rankItemByAPPPV cannot find channel2: " + tfd.url);
					//LOG.error("[ERROR]", e);
					channel2 = "other";
				}
				
				if(channel.isEmpty() || channel.equals("other")
						|| channel.equals("news"))
				{
					channel = channel2;
				}
				
				//调用分类算法来修正,如果content内容很短，不太靠谱，那么直接设置content为空，分类结果也为error
				if(item!=null &&(channel.isEmpty()
						||channel.equals("other")
						||channel.equals("news")
						||channel.equals("blog")
						||channel.equals("iclient")))
				{
					String content = item.getContent();
					if(content == null || content.trim().length() <= 192)
						content = "";
					String categorys = ClassifierClient.predict(
							item.getTitle(), content,
							"",
							"com.ifeng.secondLevelMapping.secondMappingForDiversity",
							null);
			
					if(categorys.startsWith("error")
							||categorys.startsWith("client.error")){
						//...
					}else{
						String[] secs = categorys.split("\\s");
						channel = secs[0];
					}
				
				}
				
				
			}

			tfd.channel = channel;
			tfd.weight = "D";

			// 优先取PV排名靠前的,ID会有重复
			if (appitemMap.containsKey(tfd.id))
				continue;

			appitemMap.put(tfd.id, tfd);

			// 对appItemFront根据频道进行分类
			ArrayList<tongjiField> itemList = null;
			if ((itemList = sfitem_channelMap.get(channel)) != null) {
				itemList.add(tfd);
			} else {
				itemList = new ArrayList<tongjiField>();
				itemList.add(tfd);
				sfitem_channelMap.put(channel, itemList);
			}
		}

		// 分频道设定权重
		for (Entry<String, ArrayList<tongjiField>> entry : sfitem_channelMap
				.entrySet()) {
			ArrayList<tongjiField> itemList = entry.getValue();
			if (itemList.isEmpty() || itemList.size() == 0) {
				continue;
			}

			// 根据落差和百分比来取权重
			// 分频道根据pv对item进行排序
			Collections.sort(itemList, new Comparator<tongjiField>() {
				public int compare(tongjiField item1, tongjiField item2) {
					int result = item2.pv - item1.pv;
					return result;
				}
			});

			// 找A：
			int indexA = findNextSep(itemList, 0, 9, 0.8);
			// 落差最大的位置及之前的item的权重设为B
			for (int i = 0; i <= indexA; i++)
				itemList.get(i).weight = "A";

			// 找B：
			int indexB = findNextSep(itemList, indexA + 1, itemList.size() - 1,
					0.8);
			if (indexB <= indexA)
				continue;
			// 落差最大的位置及之前的item的权重设为B
			for (int i = indexA + 1; i <= indexB; i++)
				itemList.get(i).weight = "B";

			// 找C：
			int indexC = findNextSep(itemList, indexB + 1, itemList.size() - 1,
					0.8);
			if (indexC <= indexB)
				continue;
			// 落差最大的位置及之前的item的权重设为B
			for (int i = indexB + 1; i <= indexC; i++)
				itemList.get(i).weight = "C";

			// 找D
			for (int i = indexC + 1; i < itemList.size(); i++)
				itemList.get(i).weight = "D";

		}

		// @test，输出各个分类的ABCD排列
		for (Entry<String, ArrayList<tongjiField>> entry : sfitem_channelMap
				.entrySet()) {
			ArrayList<tongjiField> itemList = entry.getValue();
			if (itemList.isEmpty() || itemList.size() == 0) {
				continue;
			}
			
//			if(entry.getKey().equals("taiwan"))
//			{
				LOG.info(entry.getKey());
				for (tongjiField pi : itemList)
					LOG.info(pi.id + "==>" + pi.channel + ":" + pi.pv + ":"
							+ pi.weight + "   " + pi.url);
				LOG.info("--------");
//			}

		}
		// @^test

	}

	/**
	 * 从统计系统获取APP文章实时pv, 并解析 注意：
	 * 
	 * @param pvLevel
	 *            浏览量阈值
	 * @return itemFrontList
	 * @throws IOException
	 */

	private static ArrayList<tongjiField> readTongjiAPP(String requestUrl)
			throws IOException {

		URL url = null;
		try {
			url = new URL(requestUrl);
		} catch (Exception e) {
			LOG.error("ERROR getTongjiURL", e);
			return null;
		}

		ArrayList<tongjiField> al_tj = new ArrayList<tongjiField>();

		LOG.info("Get Items From Tongji: " + url);
		BufferedReader r = null;

		// 读取10分钟前的统计系统url
		URLConnection con = null;
		try {
			con = url.openConnection();
			r = new BufferedReader(new InputStreamReader(con.getInputStream(),
					"UTF-8"));
		} catch (FileNotFoundException e) {
			// 若10分钟前没有日志，则访问20分钟前的
			LOG.warn("WARN", e);
			throw e;
		} catch (Exception e) {
			LOG.warn("WARN", e);
			return null;
		}

		// 读取解析统计pv文件
		String line = "";

		while ((line = r.readLine()) != null) {
			//<rec type="pic" pid="imcp_86115014" num="2547" ch="/sports/worldcup2014/pic"/>
			try {
				String type = "", imcp_id = "", sPV = "",ch = "";
				int b = 0, e = 0;
				b = line.indexOf("\"");
				e = line.indexOf("\"", b + 1);
				if (e <= b)
					continue;
				type = line.substring(b + 1, e);
				b = line.indexOf("\"", e + 1);
				e = line.indexOf("\"", b + 1);
				if (e <= b)
					continue;
				imcp_id = line.substring(b + 1, e);

				// 过滤掉无用的ID
				if (!imcp_id.startsWith("imcp_"))
					continue;
				imcp_id = imcp_id.substring(5);

				
				b = line.indexOf("\"", e + 1);
				e = line.indexOf("\"", b + 1);
				if (e <= b)
					continue;
				sPV = line.substring(b + 1, e);
				int pv = Integer.valueOf(sPV);// 文章pv
				
				
				b = line.indexOf("\"", e + 1);
				e = line.indexOf("\"", b + 1);
				if (e <= b)
					continue;
				ch = line.substring(b + 1, e);
				
				String item_url = "http://i.ifeng.com"+ch;
				String normUrl = normalizedUrl(item_url);
				tongjiField tfd = new tongjiField();
				tfd.type = new String(type);
				tfd.url = normUrl;
				tfd.pv = pv;
				tfd.id = new String(imcp_id);
				if (!tfd.id.matches("\\d{5,12}"))
					continue;
				al_tj.add(tfd);
			} catch (Exception e) {
				LOG.warn("WARN", e);
			} finally {

			}
		}

		try {
			r.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.warn("WARN", e);
		}

		return al_tj;

	}
	
	/**
	 * 解析地域新闻地域信息，并对每个地域的新闻list进行排序
	 * 
	 * 调用rankLocNewsList函数为地域新闻排序函数
	 * 
	 * @param ArrayList<RankItem> locrankItem 传入全部地域新闻
	 *            
	 * @return HashMap<String, ArrayList<RankItem>> LocNewsMap key为城市，value为排好序的地域新闻list
	 * 
	 */
	public static HashMap<String, ArrayList<RankItem>> getRankLocNewsMap(ArrayList<RankItem> locrankItem){
		HashMap<String, ArrayList<RankItem>> LocNewsMap = new HashMap<String, ArrayList<RankItem>>();
		if(locrankItem == null||locrankItem.isEmpty()){
			return LocNewsMap;
		}
		LOG.info("locrankItemList size : "+locrankItem.size());
		for(RankItem rankItem : locrankItem){
			String others = rankItem.getOthers();
			String loc = null;
			int b = others.indexOf("loc=");
			if (b > 0) {
				int e = others.indexOf("|!|", b + 4);
				if (e > b)
					loc = others.substring(b + 4, e);
				else
					loc = others.substring(b + 4);
				//将地域新闻按照地域Key存入LocNewsMap中
				ArrayList<RankItem> locNewsList = LocNewsMap.get(loc);
				if(locNewsList == null){
					locNewsList = new ArrayList<RankItem>();
					locNewsList.add(rankItem);
					LocNewsMap.put(loc, locNewsList);
				}else{
					locNewsList.add(rankItem);
				}
			}
		}
		//test 测试总数
		int count = 0;
		//对不同城市的本地新闻进行排序
		Iterator<Entry<String, ArrayList<RankItem>>> it = LocNewsMap.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, ArrayList<RankItem>> entry = it.next();
			LOG.info("before rank : "+entry.getKey()+" : "+entry.getValue().size());
			rankLocNewsList(entry.getValue());
			LOG.info("after rank : "+entry.getKey()+" : "+entry.getValue().size());
			count += entry.getValue().size();
		}
//		Set<String> keySet = new HashSet<String>();
//		keySet.addAll(LocNewsMap.keySet());
//		for(String key : keySet){	
//			rankLocNewsList(LocNewsMap.get(key));	
//		}
		LOG.info("total locnews num "+count);
		return LocNewsMap;
		
	}
	/**
	 * 封装对地域新闻进行排序的函数
	 * 
	 * 
	 * @param ArrayList<RankItem> locNewsList 传入需要排序的城市新闻list
	 *            
	 * @return void
	 * 
	 */
	public static void rankLocNewsList(ArrayList<RankItem> locNewsList){
		if(locNewsList == null || locNewsList.isEmpty()){
			return;
		}
		//内容暂时存储
		HashMap<String, ArrayList<RankItem>> newsHotLevelMap = new HashMap<String, ArrayList<RankItem>>();
		ArrayList<RankItem> newsHotLevelList = new ArrayList<RankItem>();
		//差内容
		ArrayList<RankItem> badcaseNewsList = new ArrayList<RankItem>();
		for(RankItem rankItem : locNewsList){
			String others = rankItem.getOthers();
			String hotLevel = "E";
			String docType = rankItem.getDocType();
			
			//判断是否有图
			if(docType!=null&&!docType.equals("doc")){
				docType = "pic";
			}else{
				docType = "doc";
			}
			
			int b = others.indexOf("hotlevel=");
			// 优先使用新闻原有权重，若原有权重为D或者E则使用稿源权重并通过图片因子进行调整
			if(!rankItem.getWeight().equals("D")&&!rankItem.getWeight().equals("E")){
				hotLevel = rankItem.getWeight();
			}else {
				
				if (b > 0) {
					int e = others.indexOf("|!|", b + 9);
					if (e > b)
						hotLevel = others.substring(b + 9, e);
					else
						hotLevel = others.substring(b + 9);
					//将编辑给的热度进行降权
					if(hotLevel.equals("A")||hotLevel.equals("B")){
						hotLevel = "D";
					}else{
						hotLevel = "E";
					}
				}
				
				//@test
				//解析文章内图片数量并利用图片数调整热度
				others = rankItem.getOthers();
				String picNum = "0";
				b = others.indexOf("imgNum=");
				if (b > 0) {
					int e = others.indexOf("|!|", b + 7);
					if (e > b)
						picNum = others.substring(b + 7, e);
					else
						picNum = others.substring(b + 7);
				}
				int pNum = new Integer(picNum);
				if(pNum >= 3){
					hotLevel ="C";
				}else if (pNum >= 1){
					hotLevel = "D";
				}
				
			}

			//Badecase发现及降权模块（ent降权，关键字识别存入badcase并放在所有list最后）
//			if(rankItem.getCategory() != null&&rankItem.getCategory().indexOf("ent")>0){
//				hotLevel = "E";
//			}
			
			if(rankItem.getTitle() != null&&rankItem.getTitle().indexOf("中奖名单")>0){
				badcaseNewsList.add(rankItem);
				continue;
			}
			
			//根据qualitylevel过滤本地新闻
			String qualitylevel = "B";
			b = others.indexOf("qualitylevel=");
			if (b > 0) {
				int e = others.indexOf("|!|", b + 13);
				if (e > b)
					qualitylevel = others.substring(b + 13, e);
				else
					qualitylevel = others.substring(b + 13);
				//质量差的文章降权
				if(qualitylevel.equals("C")||qualitylevel.equals("D")){
					hotLevel = "E";
				}
			}
			
			//根据不同类型和不同热度等级存入Map中例如："docA"、"picA"等
			String key = docType+hotLevel;
			//一点的数据默认排在最前面
			if(others.indexOf("yidianzixun") >= 0){
				key = "yidian";
			}

			ArrayList<RankItem> hotlist = newsHotLevelMap.get(key);
			if(hotlist == null){
				hotlist = new ArrayList<RankItem>();
				hotlist.add(rankItem);
				newsHotLevelMap.put(key, hotlist);
			}else{
				hotlist.add(rankItem);
			}	
		}
		String[] abcd = {"yidian","picA","picB","picC","picD","docA","docB","docC","docD","docE","picE"};
		for(String level : abcd){
			ArrayList<RankItem> hotlist = newsHotLevelMap.get(level);
			if(hotlist != null){
				//将新闻列表按照发布日期的先后进行排序
				Collections.sort(hotlist, new Comparator<RankItem>() {

					@Override
					public int compare(RankItem o1, RankItem o2) {
						// TODO Auto-generated method stub
						SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						try {
							//代码升级节点容错处理
							if(o1.getPublishDate() == null)
								o1.setPublishDate("2015-11-03 00:16:00");
							if(o2.getPublishDate() == null)
								o2.setPublishDate("2015-11-03 00:16:00");
							
							Date d1 = dateFormate.parse(o1.getPublishDate());
							Date d2 = dateFormate.parse(o2.getPublishDate());
							if(d1.before(d2)){
								return 1;
							}else if(d1.equals(d2)){
								return 0;
							}else{
								return -1;
							}
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							LOG.error(" ", e);
							return -1;
						}
					}
				});
				newsHotLevelList.addAll(hotlist);
			}
		}
		//将新闻列表按照发布日期的先后进行排序
		Collections.sort(newsHotLevelList, new Comparator<RankItem>() {

			@Override
			public int compare(RankItem o1, RankItem o2) {
				// TODO Auto-generated method stub
				SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
				try {
					//代码升级节点容错处理
					if(o1.getPublishDate() == null)
						o1.setPublishDate("2015-11-03 00:16:00");
					if(o2.getPublishDate() == null)
						o2.setPublishDate("2015-11-03 00:16:00");
					
					Date d1 = dateFormate.parse(o1.getPublishDate());
					Date d2 = dateFormate.parse(o2.getPublishDate());
					if(d1.before(d2)){
						return 1;
					}else if(d1.equals(d2)){
						return 0;
					}else{
						return -1;
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					LOG.error(" ", e);
					return -1;
				}
			}
		});
		
		locNewsList.clear();
		locNewsList.addAll(newsHotLevelList);
		//将badcase添加在最后（待定，是否将其直接删除）
		locNewsList.addAll(badcaseNewsList);
		
	}
	

}
