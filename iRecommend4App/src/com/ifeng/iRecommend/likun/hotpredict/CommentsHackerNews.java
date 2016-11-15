/**
 * 
 */
package com.ifeng.iRecommend.likun.hotpredict;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.classifyClient.ClassifierClient;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.dingjw.itemParser.channelsParser;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;


/**
 * <PRE>
 * 作用 : 根据评论参与数的HackerNews得分筛选出实时的热点新闻列表
 *   
 * 使用 : 通过调用getHotNewsList()函数获得热点新闻列表
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年1月8日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */


public class CommentsHackerNews {
	private static final Log LOG = LogFactory.getLog("HakerNews");
	private static HashMap<String, CommentsItem> hackerNewsMap = new HashMap<String, CommentsItem>();
	private static ItemOperation itemOP = ItemOperation.getInstance();

	/**
	 * 功能函数  用于解析统计日志中的ID和PV数
	 * 注意：
	 * 
	 * @param 输入对应日志行String
	 * @return CommentsItem
	 */
	private static CommentsItem parsePVLog(String line) {
		try {
			String type = "", imcp_id = "", sPV = "", ch = "";
			int b = 0, e = 0;
			b = line.indexOf("\"");
			e = line.indexOf("\"", b + 1);
			if (e <= b)
				return null;
			type = line.substring(b + 1, e);
			b = line.indexOf("\"", e + 1);
			e = line.indexOf("\"", b + 1);
			if (e <= b)
				return null;
			imcp_id = line.substring(b + 1, e);

			// 过滤掉无用的ID
			if (!imcp_id.startsWith("imcp_"))
				return null;
			imcp_id = imcp_id.substring(5);
			b = line.indexOf("\"", e + 1);
			e = line.indexOf("\"", b + 1);
			if (e <= b)
				return null;
			sPV = line.substring(b + 1, e);
			int pv = Integer.valueOf(sPV);// 文章pv
			CommentsItem comItem = new CommentsItem();
			comItem.setType(new String(type));
			comItem.setPv(pv);
			comItem.setId(new String(imcp_id));
			if (!comItem.getId().matches("\\d{5,12}")) {
				return null;
			}
			return comItem;
		} catch (Exception e) {
			LOG.info("paser PV log error ", e);
		}
		return null;
	}

	/**
	 *    读取统计接口函数
	 * 注意：会将FileNotFoundException 异常抛出，调用函数需捕获并作相应处理
	 * 
	 * @param requestUrl 请求统计接口的地址
	 * @return ArrayList<CommentsItem> 返回评论项数组，用于进一步计算
	 */
	private static ArrayList<CommentsItem> readItemPV(String requestUrl)
			throws IOException {
		URL url = null;
		try {
			url = new URL(requestUrl);
		} catch (Exception e) {
			LOG.error("ERROR getTongjiURL", e);
			return null;
		}

		ArrayList<CommentsItem> comItemList = new ArrayList<CommentsItem>();

		LOG.info("Get Items From Tongji: " + url);
		BufferedReader r = null;
		// 读取10分钟前的统计系统url
		URLConnection con = null;
		try {
			con = url.openConnection();
			con.setConnectTimeout(2*1000);
			con.setReadTimeout(2*1000);
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
		String line = null;
		while ((line = r.readLine()) != null) {
			CommentsItem comItem = parsePVLog(line);
			if (comItem != null) {
				comItemList.add(comItem);
			}
		}
		return comItemList;
	}

	/**
	 * 规范化新闻URL函数
	 * 对于无法获取item,item的URL为空的以及URL不规范的项进行修整。
	 * 注意：
	 * 
	 * @param CommentsItem 
	 * @return String 规范化后的URL
	 */
	private static String formateUrl(CommentsItem citem) {
		if (citem.getItem() == null) {
			return "http://wap.ifeng.com/news.jsp?aid=" + citem.getId();
		}
		String url = citem.getItem().getUrl();
		if (url == null || !url.startsWith("http")) {
			LOG.info("This " + citem.getId() + " url is  wrong " + url);
			return "http://wap.ifeng.com/news.jsp?aid=" + citem.getId();
		}
		if (url != null && url.startsWith("http:/")
				&& !(url.startsWith("http://")))
			url = url.replaceFirst("http:", "http:/");
		if (url.startsWith("http://i.ifeng.com/")) {
			return "http://wap.ifeng.com/news.jsp?aid=" + citem.getId();
			//查询PC端新闻的URL（覆盖度不高）
//			String reqUrl = "http://i.ifeng.com/getwwwurl?aid=" + citem.id;
//			URL u;
//			try {
//				u = new URL(reqUrl);
//				URLConnection con = u.openConnection();
//				con.setConnectTimeout(2*1000);
//				con.setReadTimeout(2*1000);
//				BufferedReader br = new BufferedReader(new InputStreamReader(
//						con.getInputStream()));
//				String read = null;
//				while ((read = br.readLine()) != null) {
//					if (read.length() > 0) {
//						return read;
//					}
//				}
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				LOG.error("get PC url error", e);
//				LOG.warn("item url is : "+ url);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				LOG.error("get PC url error", e);
//				LOG.warn("item url is : "+ url);
//			}catch (Exception e) {
//				// TODO: handle exception
//				LOG.error("get PC url error", e);
//				LOG.warn("item url is : "+ url);
//			}
		}

		return url;
	}

	/**
	 * 根据新闻URL 来获取文章评论参与数
	 * 注意：
	 * 
	 * @param ReqUrl 新闻的URL
	 * @return Int 返回对应新闻的评论参与数
	 */
	private static int getCommentsNum(String ReqUrl) {
		if (ReqUrl == null) {
			return -1;
		}
		String url = "http://comment.ifeng.com/joincount.php?doc_url=" + ReqUrl;
		try {
			URL u = new URL(url);
			URLConnection con = u.openConnection();
			con.setConnectTimeout(2*1000);
			con.setReadTimeout(2*1000);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String read = null;
			while ((read = br.readLine()) != null) {
				if (read.length() > 0) {
					String[] readList = read.split(",");
					String score = readList[1].replaceAll("\\'", "");
					int ComNum = new Integer(score);
					return ComNum;
				}
			}
			return -1;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			LOG.error("get CommentsNum error : ", e);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("get CommentsNum error : ", e);

		} catch (Exception e) {
			// TODO: handle exception
			LOG.error("get CommentsNum error : ", e);

		}
		return -1;
	}

	/**
	 * hackerNews得分计算函数
	 * 注意：G是新闻生命时间权重因子，G越大新闻生命之间对得分的惩罚越高，目前默认是G=1.8
	 * 
	 * @param int 评论参与数 double 新闻生命时间
	 * @return double 返回hackerNews得分
	 */
	private static double hackerNews(int pv, double lifeTime) {
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

	/**
	 * 获取新闻频道分类信息
	 * 注意：新闻会同时给出URL映射分类和分类算法两种分类方式的结果，用|!|分隔。若两种分类方式结果一样则只给出一个分类。
	 * 
	 * @param CommentsItem 
	 * @return String 新闻的频道分类信息
	 */
	private static String getChannel(CommentsItem citem) {
		String channel = null;
		// 根据前端映射树，获取channel
		if (channel == null) {
			channel = "";
			Item item = citem.getItem();

			String channel1 = "";
			// @test
			try {
				channel1 = channelsParser
						.getInstance(ItemOperation.ItemType.APPITEM)
						.getTransChannelByItem(item, 1).split("-")[0];
				if (channel1 == null || channel1.isEmpty()
						|| channel1.indexOf("notopic") >= 0)
					channel1 = "other";
			} catch (Exception e) {
				LOG.info("rankItemByAPPPV cannot find channel1: "
						+ citem.getId());
				channel1 = "other";
			}

			String channel2 = "";
			// 调用分类算法来修正,如果content内容很短，不太靠谱，那么直接设置content为空，分类结果也为error
			if (item != null
					&& (channel.isEmpty() || channel.equals("other")
							|| channel.equals("news") || channel.equals("blog") || channel
								.equals("iclient"))) {
				String content = item.getContent();
				//文章内容长度小于120个 字符的（包括分词字符）不进行分类算法分类
				if (content == null || content.trim().length() <= 192) {
					content = "";
					channel2 = "other";
				}

				String categorys = ClassifierClient
						.predict(item.getTitle(),content,"",
								"com.ifeng.secondLevelMapping.secondMappingForDiversity",
								null);

				if (categorys.startsWith("error")
						|| categorys.startsWith("client.error")) {
					// ...
				} else {
					String[] secs = categorys.split("\\s");
					channel2 = secs[0];
				}

			}
			
			if (channel1.isEmpty() || channel1.equals(channel2)
					|| channel1.equals("other") || channel1.equals("news")
					|| channel1.equals("blog") || channel1.equals("iclient")) {
				channel = channel2;
			} else{
				channel = channel1 + "|!|" + channel2;
			}

		}

		return channel;
	}
	
	/**
	 * 对CommentsItemList进行排序的函数
	 * 注意：根据Score由大到小排序
	 * 
	 * @param ArrayList<CommentsItem> 
	 * @return void
	 */
	private static void sortCommentsItemList(ArrayList<CommentsItem> comList){
		if(comList == null){
			return ;
		}
		Collections.sort(comList, new Comparator<CommentsItem>() {

			@Override
			public int compare(CommentsItem o1, CommentsItem o2) {
				// TODO Auto-generated method stub
				
				double sc = o1.getScore() - o2.getScore();
				if (sc >= 0) {
					return -1;
				} else {
					return 1;
				}
			}
		});
	}

	/**
	 * 获取更新新闻评论数、HackerNews得分的总流程函数
	 * 
	 * 获取实时有PV的新闻id，缓存生命周期在18个小时内的新闻，每次调用都会更新实时的评论参与数
	 * 保存在静态HashMap中HackerNewsMap
	 * 
	 * 注意：新闻生命周期暂时设定为18个小时
	 * 
	 * @param  
	 * @return void
	 */
	private static void setHackerNewsScore() {
		String requestUrl = heatPredict.getTongjiURL(
				fieldDicts.itemFromTongjiAPP, 0);
		itemOP.setItemType(ItemType.APPITEM);
		ArrayList<CommentsItem> comItemList = null;
		try {
			comItemList = readItemPV(requestUrl);
		} catch (FileNotFoundException e) {
			LOG.warn("", e);
			LOG.info("retry");
			// 重试20分钟前的文件
			requestUrl = heatPredict.getTongjiURL(fieldDicts.itemFromTongjiAPP,
					10);
			try {
				comItemList = readItemPV(requestUrl);
			} catch (FileNotFoundException e1) {
				LOG.warn("", e1);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				LOG.warn("", e1);
			}

		} catch (IOException e) {
			LOG.warn("", e);
		}

		if (comItemList != null && !comItemList.isEmpty()) {
			for (CommentsItem cItem : comItemList) {
				String key = cItem.getId();
				CommentsItem cacheItem = hackerNewsMap.get(key);
				if (cacheItem == null) {
					hackerNewsMap.put(key, cItem);
				}
			}
		} else {
			LOG.warn("get PVID error");
		}

		// 缓存Map中不为空则对其进行hackernews计算

		LOG.info("set Hackernews ...");
		Set<String> keySet = new HashSet<String>();
		keySet.addAll(hackerNewsMap.keySet());
		for (String key : keySet) {
			CommentsItem cItem = hackerNewsMap.get(key);

			// 在hbase查不到item的id从map中删除不参与计算
			if (cItem.getItem() == null) {
				Item item = itemOP.getItem(key);
				if (item != null) {
					cItem.setItem(item);
					try{
						Timestamp ts = Timestamp.valueOf(item.getDate());
						cItem.setCreatTime(ts.getTime());
					}catch(Exception e){
						LOG.error("get Item Date Error : ", e);
						LOG.info("Error Item ID : "+item.getID());
					}
				} else {
					hackerNewsMap.remove(key);
					continue;
				}
			}

			// 生命周期超过18个小时的新闻默认删除，不参与热度计算
			double lifeTime = System.currentTimeMillis() - cItem.getCreatTime();
			if (lifeTime >= 18 * 60 * 60 * 1000) {
				hackerNewsMap.remove(key);
				continue;
			}
	
	
			
			//设置新闻channel
			if(cItem.getChannel() == null || cItem.getChannel().isEmpty()){
				cItem.setChannel(getChannel(cItem));
			}
			
			
			//规范化新闻URL
			if(cItem.getUrl()==null||cItem.getUrl().isEmpty()||cItem.getUrl().indexOf("http://i.ifeng.com/")>0){
				cItem.setUrl(formateUrl(cItem)); 
			}
			int commentsNum = getCommentsNum(cItem.getUrl());
			cItem.setCommentsNum(commentsNum);
			
			double score = hackerNews(cItem.getCommentsNum(), lifeTime);
			cItem.setScore(score); 
			
			//test
			cItem.setLifetime(lifeTime/(60*60*1000));
		}
	}
	
	/**
	 * 对外的唯一接口函数，用于获取当前实时的热点新闻List
	 * 
	 * 1、通过调用setHackerNewsScore()函数更新hackerNewsMap评论数以及得分
	 * 2、遍历hackerNewsMap，根据规则筛选出对应的新闻list
	 * 
	 * 注意：目前的规则是1、新闻评论数大于10；2、选取每个类别中TOP10的新闻；3、整体的新闻List根据新闻的Score排序
	 * 
	 * @param 
	 * @return ArrayList<CommentsItem> 
	 */
	public static ArrayList<CommentsItem> getHotNewsList(){
		ArrayList<CommentsItem> hotNewsList = new ArrayList<CommentsItem>();
		setHackerNewsScore();
		//将数据按channel存储
		ArrayList<CommentsItem> channelList = null;
		HashMap<String, ArrayList<CommentsItem>> CommentsItemMap = new HashMap<String, ArrayList<CommentsItem>>();
		Set<String> keySet = hackerNewsMap.keySet();
		for(String key : keySet){
			CommentsItem cItem = hackerNewsMap.get(key);
			//过滤掉评论数小于10的新闻
			if(cItem.getCommentsNum() < 10){
				continue;
			}
			//新闻映射到多个分类下
			String[] channelArr = cItem.getChannel().split("\\|!\\|");
			for (String channel : channelArr) {
				channelList = CommentsItemMap.get(channel);
				if (channelList == null) {
					channelList = new ArrayList<CommentsItem>();
					channelList.add(cItem);
					CommentsItemMap.put(channel, channelList);
				} else {
					channelList.add(cItem);
				}
			}
		}
		//对每个channel下的新闻进行排序并选择前十进入hotNewsList
		HashSet<String> visitedId = new HashSet<String>();
		keySet = CommentsItemMap.keySet();
		for(String channel : keySet){
			channelList = CommentsItemMap.get(channel);
			sortCommentsItemList(channelList);
			//score得分大于0.5
//			for(int i=0;i<channelList.size();i++){
//				CommentsItem tempItem = channelList.get(i);
//				if(visitedId.contains(tempItem.id)||tempItem.score<0.5){
//					continue;
//				}else{
//					hotNewsList.add(tempItem);
//					visitedId.add(tempItem.id);
//				}
//			}
			
			
			//排名前十且评论数大于10
			if(channelList.size()<=10){
				for(int i=0;i<channelList.size();i++){
					CommentsItem tempItem = channelList.get(i);
					if(visitedId.contains(tempItem.getId())){
						continue;
					}else{
						hotNewsList.add(tempItem);
						visitedId.add(tempItem.getId());
					}
				}
			}else{
				for(int i=0;i<10;i++){
					CommentsItem tempItem = channelList.get(i);
					if(visitedId.contains(tempItem.getId())){
						continue;
					}else{
						hotNewsList.add(tempItem);
						visitedId.add(tempItem.getId());
					}
				}
			}
		}
		
		sortCommentsItemList(hotNewsList);
		
		return hotNewsList;
	}
	
	public static void main(String[] args){
		fieldDicts.appTreeMappingFile = "D:/test/AppTreeMapping.txt";
		fieldDicts.frontAppTreeMappingFile = "D:/test/APPFront_TreeMapping.txt";

		while(true){
			long preTime = System.currentTimeMillis();
			ArrayList<CommentsItem> hotNewsList = getHotNewsList();
			long time = System.currentTimeMillis()-preTime;
			int i = 1;
			for(CommentsItem citem : hotNewsList){
				System.out.println("------------------------");
				System.out.println(i);
				System.out.println("id : "+citem.getId()+" score : "+citem.getScore()+" CommentsNum : "+citem.getCommentsNum()+" Channel : "+citem.getChannel()+" lifetime : "+citem.getLifetime());
//				System.out.println("creatTime"+citem.creatTime);
//				System.out.println("nowTime : "+System.currentTimeMillis());
				System.out.println(citem.getItem().getTitle().replaceAll("_[a-z]+", ""));
				i++;
			}
			System.out.println(" total time : "+time/1000);
			LOG.info("hackernewsMap size : "+hackerNewsMap.size());
			try {
				LOG.info("begain to sleep  ....");
				Thread.sleep(5*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
