package com.ifeng.iRecommend.featureEngineering.freshProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.redis.JedisInterface;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.featureEngineering.CMPPDataCollect;
import com.ifeng.iRecommend.featureEngineering.JsonFromCMPP;
import com.ifeng.iRecommend.featureEngineering.itemf;

public class FreshItemToRedis {
	static Logger LOG = Logger.getLogger(FreshItemToRedis.class);
	// 阻塞队列的最大长度，防止内存溢出。
	public static final int MAX_QUEUE_SIZE = 200;
	// 致命毒药，优雅关闭后续的工作线程
	private static final JsonFromCMPP DEADLY_POISON = new JsonFromCMPP();
	// 抓取的起始时间,10位时间戳
	private static long sTime = 0;// 1427644800;
	// redis db 14 内容池3 为Fresh客户端服务
	private static final int redisDbNum = 14;

	// 初始化
	private static void initial(long startTime) {
		DEADLY_POISON.setId("DEADLY_POISON");
		BasicConfigurator.configure();
		PropertyConfigurator.configure("conf/log4j.properties");
		sTime = startTime;
	}

	/**
	 * 用于判断文章类型，目前只有doc和docpic两种，后续可能增加slide，video等
	 * 
	 * @param typeInfo
	 * @return
	 */
	static public String processDocType(String typeInfo) {
		String typeResult = null;
		if (typeInfo.equals("0")) {
			typeResult = "doc";
		} else {
			typeResult = "docpic";
		}
		return typeResult;
	}

	/**
	 * 过滤非法字符
	 * 
	 * @param str
	 * @return
	 */
	public static String filterString(String str) {
		int filterStringCount = 0;
		String reStr = str;
		boolean cycFlag = true;
		try {
			while (cycFlag && filterStringCount < 20) {
				filterStringCount++;
				Pattern pattern = Pattern
						.compile("([^0-9a-zA-Z_!*@# $%^&()-,.:?;\n\t【】/=+|'\"\\\\{\\}\\[\\]，。：？；“”……！——《》\\u4e00-\\u9fa5]+)");
				Matcher matcher = pattern.matcher(reStr);
				if (matcher.find()) {
					reStr = reStr.replaceAll(matcher.group(1), "");
				} else {
					cycFlag = false;
				}
			}
		} catch (Exception e) {
			LOG.error("[ERROR] Some error occured in filterString.", e);
			LOG.error("[ERROR] filterString str is " + str, e);
			return reStr;
		}
		return reStr;

	}

	/**
	 * 从正文中提取图片的url，存储下来，用于在分词完成后再拼接回原位置
	 * 
	 * @param s
	 * @return
	 */
	public static List<String> findPicUrl(String s) {
		if (s == null || s.isEmpty())
			return null;
		s = s.toLowerCase();
		List<String> picUrl = new ArrayList<String>();
		Pattern pattern = Pattern
				.compile("http://.*?.(jpg|jpeg|gif|png|undefined)");
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			picUrl.add(matcher.group());
		}
		if (picUrl.size() >= 1)
			return picUrl;
		else
			return null;
	}

	/**
	 * 过滤html标签
	 * 
	 * @param s
	 * @return
	 */
	public static String filterHtml(String s) {
		if (s != null) {
			String str = s.replaceAll("</p>", "\n");
			str = str.replaceAll("<img[.[^<]]*/>", "#p#");
			str = str.replaceAll("<[.[^<]]*>", " ");
			return str;
		} else {
			return s;
		}
	}

	/**
	 * 把Item存入Redis
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Integer writeItemToRedis(String key, String value) {

		try {
			JedisInterface.set(key, value, redisDbNum);
		} catch (Exception e) {
			LOG.error("set-string-failed:" + key + "," + value, e);
			LOG.error("[ERROR] In write item to Redis.", e);

			return -1;
		}

		return 1;
	}

	public static String getClassify(String other) {
		String result = null;
		if (other == null || other.isEmpty())
			return null;
		String[] others = other.split("\\|!\\|");
		for (String s : others) {
			if (s.contains("classify="))
				result = s.split("classify=")[1].trim();
		}
		return result;
	}

	/**
	 * 处理jsonItem的操作。
	 * 
	 * @param jsonItem带解析的原始item
	 * @param
	 */
	public static void processItem(JsonFromCMPP jsonItem) throws Throwable {
		if (jsonItem == null) {
			LOG.info("jsonItem is null.");
			return;
		}
		FreshItemf item = null;
		item = new FreshItemf();
		int imgCount = 0;
		String separater = "||";
		if (jsonItem.getContent().contains("<img")) {
			String[] imgSplit = jsonItem.getContent().split("<img");
			imgCount = imgSplit.length - 1;
		}
		String title = jsonItem.getTitle();
		String content = jsonItem.getContent().replaceAll("&quot;", "\"");
		// 存储图片的url地址
		List<String> picUrlList = new ArrayList<String>();
		picUrlList = findPicUrl(content);
		content = filterHtml(content);
		StringBuffer splitBuffer = new StringBuffer();
		splitBuffer.append(title);

		splitBuffer.append(separater).append(content);

		String preSplit = filterString(splitBuffer.toString());
		// 对标题、内容、摘要进行合并和统一分词
		// 用SplidWordClient进行分词
		String textSplited = new String(SplitWordClient.split(preSplit, null)
				.replace("(/", "_").replace(") ", " "));
		String textSplits[] = textSplited.split("\\|_w \\|_w");

		String s_title = null;
		String s_content = null;
		if (textSplits.length == 2) {
			s_title = textSplits[0];
			s_content = textSplits[1];
		} else {
			LOG.error("[ERROR] Splited words result is wrong."
					+ jsonItem.getId());

			return;
		}
		// 获取tags对应的c，sc等信息
		String other = jsonItem.getOther();
		// 附带了clear方法，只能使用一次
		other = other + "|!|imgNum=" + String.valueOf(imgCount);
		String docType = null;
		if (jsonItem.getType().equals("article")) {
			docType = processDocType(String.valueOf(imgCount));
		} else {
			docType = "slide";
		}
		item.setID(jsonItem.getId());
		item.setTitle(title);
		item.setSplitTitle(s_title);
		item.setUrl(jsonItem.getSourceLink());
		item.setRank(jsonItem.getRank());
		item.setLife(jsonItem.getLife());

		if (picUrlList != null) {
			for (int i = 0; i < picUrlList.size(); i++) {
				s_content = s_content.replaceFirst("#_w p_nx #_w",
						picUrlList.get(i));
			}
		}
		item.setSplitContent(s_content);
		item.setPublishedTime(jsonItem.getPublishedTime());
		item.setOther(other);
		String c = getClassify(other);
		item.setDocType(docType);
		item.setAppId("fresh");
		item.setShowStyle(jsonItem.getShowStyle());
		item.setSource(jsonItem.getSourceAlias());
		ArrayList<String> feature = new ArrayList<String>();
		if (c != null) {
			feature.add(c);
			feature.add("c");
			feature.add("1");
		}
		if (jsonItem.getSourceAlias() != null
				|| !jsonItem.getSourceAlias().isEmpty()) {
			feature.add(jsonItem.getSourceAlias());
			feature.add("s");
			feature.add("1");
		}
		item.setFeatures(feature);
		// 把item转换成json串
		String json = "";
		try {
			json = JsonUtils.toJson(item, itemf.class);
		} catch (Exception e) {
			LOG.error("[ERROR] Error occurred when change item to Json. Id is "
					+ item.getID(), e);
			return;
		}
		// System.out.println(item.getID());
		// System.out.println(item.getAppId());
		// System.out.println(item.getDocType());
		// System.out.println(item.getLife());
		// System.out.println(item.getOther());
		// System.out.println(item.getPublishedTime());
		// System.out.println(item.getRank());
		// System.out.println(item.getShowStyle());
		// System.out.println(item.getSource());
		// System.out.println(item.getSplitTitle());
		// System.out.println(item.getTitle());
		// System.out.println(item.getUrl());
		// System.out.println(item.getTitle());
		// System.out.println(item.getFeatures());
		// FileWriter fw = new FileWriter("D:\\data\\newitemTest",true);
		// fw.write();
		// fw.write("id is "+item.getID()+"\n");
		// fw.write("appid is  " + item.getAppId()+"\n");
		// fw.write("doctype is  " + item.getDocType()+"\n");
		// fw.write("life is " + item.getLife()+"\n");
		// fw.write("other is  " + item.getOther()+"\n");
		// fw.write("publishTime is  " + item.getPublishedTime()+"\n");
		// fw.write("rank is " + item.getRank()+"\n");
		// fw.write("showstyle is " + item.getShowStyle()+"\n");
		// fw.write("source is " + item.getSource()+"\n");
		// fw.write("split title is  " + item.getSplitTitle()+"\n");
		// fw.write("title is  " + item.getTitle()+"\n");
		// fw.write("url is  " + item.getUrl()+"\n");
		// fw.write("title is  " + item.getTitle()+"\n");
		// fw.write("content is  " + item.getSplitContent()+"\n");
		// fw.write("feature is  " + item.getFeatures().toString()+"\n");
		// fw.flush();
		// fw.close();
		int writeRedis = writeItemToRedis(item.getID(), json);
		if (writeRedis == 1) {
			LOG.info("[INFO] Write redis " + item.getID() + " successful.");
		}
		Thread.sleep(200);
	}

	/**
	 * 数据输入处理类
	 */
	static class dataInputThread extends Thread {
		private BlockingQueue<JsonFromCMPP> fileQueue;

		public dataInputThread(BlockingQueue<JsonFromCMPP> fileQueue) {
			super("data-input-thread");
			this.fileQueue = fileQueue;
		}

		public void run() {
			try {
				CMPPDataCollect.getDataFromCMPP327(sTime, fileQueue);
				fileQueue.put(DEADLY_POISON);// 放置毒药，优雅关闭
			} catch (InterruptedException e) {
				// 在这里可以做一些异常处理
				e.printStackTrace();
				LOG.error("Thread ERROR, the thread has stopped.");
			}
		}
	}

	/** 处理输入jsonItem的线程，可以多线程并发处理，每个线程处理一个item */
	static class TravelQueueThread extends Thread {
		private final static AtomicInteger ThreadCount = new AtomicInteger();
		private BlockingQueue<JsonFromCMPP> fileQueue;

		public TravelQueueThread(BlockingQueue<JsonFromCMPP> fileQueue,
				HashMap<String, Long> keyLiveMap) {
			super("travel-queue-thread-" + ThreadCount.incrementAndGet());
			this.fileQueue = fileQueue;
		}

		public void run() {
			JsonFromCMPP jsonItem = null;

			try {
				while ((jsonItem = fileQueue.take()) != DEADLY_POISON) {
					try {
						processItem(jsonItem);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
				fileQueue.put(DEADLY_POISON);// 放置毒药，优雅关闭
			} catch (InterruptedException e) {
				// 在这里可以做一些异常处理
				e.printStackTrace();
			}
		}
	}

	private BlockingQueue<JsonFromCMPP> fileQueue = new LinkedBlockingQueue<JsonFromCMPP>(
			MAX_QUEUE_SIZE);
	private HashMap<String, Long> keyLiveMap = new HashMap<String, Long>();
	private Thread visitFolderThread;
	private Thread[] travelFileThreads;

	/**
	 * 构造函数
	 * 
	 * @param sourceFile
	 * @param travelThreads
	 */
	public FreshItemToRedis(int travelThreads, long startTime) {
		super();
		// 初始化DEADLY_POISON,抓取cmpp的起始时间（long型 10位的时间戳）
		initial(startTime);
		visitFolderThread = new dataInputThread(fileQueue);
		travelFileThreads = new TravelQueueThread[travelThreads];
		for (int i = 0; i < travelFileThreads.length; i++) {
			travelFileThreads[i] = new TravelQueueThread(fileQueue, keyLiveMap);
		}
	}

	/**
	 * 开始执行
	 */
	public void start() {

		visitFolderThread.start();
		for (int i = 0; i < travelFileThreads.length; i++) {
			travelFileThreads[i].start();
		}
	}

	/**
	 * 强行终止。请慎用。程序会自动关闭
	 */
	public void terminate() {
		visitFolderThread.interrupt();
		for (int i = 0; i < travelFileThreads.length; i++) {
			travelFileThreads[i].interrupt();
		}
	}

	/**
	 * 测试用例
	 */
	public static void main(String[] args) {
		final int travelThreads = Integer.valueOf(args[0]);
		long startTime = Long.valueOf(args[1]);
		FreshItemToRedis fetcher = new FreshItemToRedis(travelThreads,
				startTime);
		fetcher.start();
	}
}
