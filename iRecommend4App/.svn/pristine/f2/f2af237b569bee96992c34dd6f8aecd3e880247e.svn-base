package com.ifeng.iRecommend.featureEngineering;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ifeng.commen.Utils.AutoLoadingConfiguration;
import com.ifeng.hexl.redis.GetUsefulKeyFromRedis;
import com.ifeng.iRecommend.featureEngineering.OrientDB.KnowledgeGraph;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.tinkerpop.blueprints.Vertex;

/**
 * 
 * <PRE>
 * 作用 : 根据输入features 返回与其相关的item id list
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
 *          1.0          2015年9月11日        lixiao          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class RelatedItem {
	private final static Logger log = Logger.getLogger(RelatedItem.class);

	private static AutoLoadingConfiguration config = new AutoLoadingConfiguration(
			"conf/sys.properties", log);
	private static String pathStopword = config.getValueBykey("StopWordPath");
	private static Set<String> swSet = new HashSet<String>();

	private static int APP_DOC_NUM = 6;
	private static int WAP_DOC_NUM = 3;
	static {
		// 加载停用词
		FileReader reader = null;
		try {
			reader = new FileReader(pathStopword);// 读取stopword文件
			BufferedReader alines = new BufferedReader(reader);
			String s1 = null;
			while ((s1 = alines.readLine()) != null)
				swSet.add(s1.trim());
			reader.close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	private String topic1 = "";
	private String topic1_candidate = "";
	private String sc = "";
	private String e = "";
	private String loc = "";
	private StringBuffer topic2 = new StringBuffer();
	private StringBuffer topic3 = new StringBuffer();

	private int n = 0; // 权重大于0.5的topic3个数
	private int m = 0; // 其他权重的topic3个数

	private String channelWord = ""; // 最终给出的频道名称

	private boolean isRandom = true; // 是否随机选取结果
	private int mm = 2;
	private boolean containWap = true;

	// private int rows = 60; //查询返回数目

	RelatedItem(boolean containWap) {
		this.containWap = containWap;
	}

	public RelatedItem() {
	}

	/**
	 * 根据不同的应用场景返回对应的数据Map，当前有客户端和手凤
	 * 
	 * 返回Map中key为应用场景名，value为对应list，例如：客户端:[巴黎恐怖袭击, 1054233, 1053926, 1054614]
	 * 
	 * @param featureList
	 *            传入的待匹配item 的feature list
	 * @param splitedTitle
	 *            待匹配item分词后的标题
	 * @param source
	 *            稿源
	 * @return 根据feature list返回的相关item id list 以及对应频道名称。
	 *         若有c而查询结果不足docNum时，去除topic2和topic3做二次查询，直接返回结果list；
	 *         若对应c缺失，则在返回list首位放入空串""，并不做二次查询。
	 * 
	 *         注意：当返回的id list只含有空串""时，list.isEmpty() is false
	 */
	public Map<String, List<String>> getRelatedItemId(
			ArrayList<String> featureList, String splitedTitle, String source) {
		if (featureList == null || featureList.isEmpty()
				|| featureList.size() < 3) {
			log.warn("[PREPROC] FEATURES ARE NULL!");

			String query = makeQueryForTitle(splitedTitle);
			if (query == null || query.isEmpty())
				return null;

			NodeList results = getHttpRequestResult(query.toString());
			Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
			// 客户端
			List<String> list = getUltimateList(splitedTitle, results,
					APP_DOC_NUM, "app");
			resultMap.put("客户端", list);
			return resultMap;
		}
		if (splitedTitle == null || splitedTitle.isEmpty()) {
			log.warn("[PREPROC] SPLITED TITLE IS NULL!");
			return null;
		}
		log.info("[Related] Features input is " + featureList);

		// 处理featureList 填充topic1，2，3等fields 并确定channelWord
		handleFeatures(featureList);

		// 根据填充的topic1，2，3 等 生成query，生成不够时使用source字段随机出
		String query = makeQuery(source);
		// query = new
		// StringBuffer("http://10.32.28.117:8081/solr46/item/select?q=+topic1:(%E6%B3%95%E5%88%B6)&sort=date+desc&rows=60");
		if (query == null || query.isEmpty())
			return null;

		// http直连查询solr库
		NodeList results = getHttpRequestResult(query.toString());

		Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
		// 客户端
		List<String> list = getUltimateList(splitedTitle, results, APP_DOC_NUM,
				"app");
		resultMap.put("客户端", list);

		if (containWap) {
			// 手凤
			list = getUltimateList(splitedTitle, results, WAP_DOC_NUM, "wap");
			resultMap.put("手凤", list);
		}

		return resultMap;
	}

	private void handleFeatures(ArrayList<String> featureList) {
		double maxWeightC1 = 0; // c1 对应最大权重
		double maxWeightC0 = 0; // c0 对应最大权重
		double maxWeightSc = 0.4; // sc 对应最大权重
		double maxWeightE = 0.4; // e 对应最大权重
		double maxWeightLoc = 0.4; // loc 对应最大权重

		/*
		 * 处理features， 将对应类型映射的topic1，2，3
		 */

		int firstTopic3 = -1;
		for (int k = 0; k < featureList.size(); k += 3) {
			String label = featureList.get(k);
			String labelType = featureList.get(k + 1);
			String weight = featureList.get(k + 2);

			double w = Double.valueOf(weight);

			if ("c".equals(labelType) && w > 0) {
				if (w > maxWeightC1) {
					if (label.equals("大陆时事") || label.equals("社会八卦")
							|| label.equals("国际"))
						maxWeightC1 = Math.min(0.5, w);
					else
						maxWeightC1 = w;
					topic1 = label;
				}
				continue;
			} else if ("c".equals(labelType) && w < 0) {
				w = Math.abs(w);
				if (w > maxWeightC0
						|| (w == maxWeightC0 && !label.equals("时政"))) {
					maxWeightC0 = w;
					topic1_candidate = label;
				}
				continue;
			} else if ("sc".equals(labelType)) {
				w = Math.abs(w);

				if (w > maxWeightSc) {
					topic2.append(label).append(" OR ");
					maxWeightSc = w;
					sc = label;
				}
				continue;
			} else if ((w = Math.abs(w)) >= 0.5 && "e".equals(labelType)) {
				// topic2.append(label).append(" OR ");
				if (w > maxWeightE && label.length() < 11 && label.length() > 3) {
					maxWeightE = w;
					e = label;
				}
				continue;
			} else if ((w = Math.abs(w)) >= 0.5
					&& ("cn".equals(labelType) || "t".equals(labelType))) {
				topic2.append(label).append(" OR ");
				continue;
			} else if ("loc".equals(labelType)) {
				w = Math.abs(w);
				if (w > maxWeightLoc) {
					maxWeightLoc = w;
					loc = label;
				}
				continue;
			} else if (FeatureExTools.isUsableLabel(labelType)
					|| "s1".equals(labelType)) {
				if (firstTopic3 == -1)
					firstTopic3 = k;

				if ((w = Math.abs(w)) >= 0.5) {
					// w = w * 10;
					n++;
					if (labelType.equals("et")) {
						w += 0.5;
						mm = 1;
					}
					topic3.append(label).append("^" + w).append(" OR ");
				}

				if (n >= 10)
					break;
			}
		}
		if (firstTopic3 == -1)
			firstTopic3 = 0;
		if (n < 6) {
			for (int i = firstTopic3; i < featureList.size(); i += 3) {
				String label = featureList.get(i);
				String labelType = featureList.get(i + 1);
				String weight = featureList.get(i + 2);
				double w = Double.valueOf(weight);
				w = Math.abs(w);
				if (FeatureExTools.isUsableLabel(labelType) && w < 0.5
						&& w >= 0.1) {
					// w = w * 10;
					m++;
					topic3.append(label).append("^" + w).append(" OR ");

				}
				if (m >= 10)
					break;// 特征本身按层次排序，topic3词够多后跳出避免query过长
			}
		}

		// //2016.07.22 temporary
		// if(topic1.equals("奥运")){
		// channelWord = topic1;
		// topic2.setLength(0);
		// return;
		// }

		if (!e.isEmpty()) {
			int flag = GetUsefulKeyFromRedis.GetUsefulFlag(e);
			if (flag == 1) {
				channelWord = e;
				topic2.setLength(0);
				topic2.append(e).append(" OR ");
				topic3.setLength(0);
				log.info("[Feature handling] E IS AVALIABLE!");
				return;
			} else {
				e = "";
				log.info("[Feature handling] E IS NOT USABLE!");
			}
			
		}
		if (!sc.isEmpty()) {
			KnowledgeGraph kgraph = new KnowledgeGraph();
			// kgraph.queryWord(sc, "sc");
			ArrayList<Vertex> VertexList = kgraph.queryWord(sc, "sc");
			if (VertexList == null) { // 没有查到sc node，视为sc不可用
				sc = "";
				log.error("[Feature handling] ERROR FOR CHECK SC NODE!");
				return;
			}

			ArrayList<Vertex> parentsList = kgraph.queryParent(sc, "sc");
			Set<String> c_set = new HashSet<String>();

			// 仅往上溯一层
			for (int i = 0; i < parentsList.size(); i++) {
				Vertex node = parentsList.get(i);
				if (node.getProperty("type").equals("c"))
					c_set.add((String) node.getProperty("word"));
			}

			maxWeightC1 = 0;
			maxWeightC0 = 0;
			topic1 = "";
			for (String c : c_set) {
				int p = featureList.indexOf(c);
				if (p <= -1) // 底层数据出错
					continue;
				String labelType = featureList.get(p + 1);
				String weight = featureList.get(p + 2);

				double w = Double.valueOf(weight);
				if ("c".equals(labelType) && w >= 0) {
					if (w > maxWeightC1) {
						maxWeightC1 = w;
						topic1 = c;
					}
					continue;
				} else if ("c".equals(labelType) && w < 0) {
					if (Math.abs(w) > maxWeightC0) {
						maxWeightC0 = Math.abs(w);
						topic1_candidate = c;
					}
					continue;
				}
			}
			log.info("[Feature handling] SC IS AVALIABLE!");
			channelWord = sc;
			return;
		}
		if (e.isEmpty() && sc.isEmpty()) { // 未取到可用sc e，取可用c
			if(!loc.isEmpty() && (topic1.equals("天气") || topic1.isEmpty())){
				channelWord = loc;
				topic2.setLength(0);
				topic3.setLength(0);
				log.info("[Feature handling] WEATHER CHANGE TO LOCATE!");
				return;
			}
			if (topic1.length() > 0 && maxWeightC1 >= 0.4) {
				channelWord = topic1;
			}
			else if (topic1_candidate.length() > 0 && maxWeightC0 >= 0.4)
				channelWord = topic1_candidate;
			return;
		}
	}

	private String makeQuery(String source) {
		String encodedTopic;
		// 生成访问item 投放 solr query
		StringBuffer query = new StringBuffer("select?q=");
		boolean canQuery = false;

		String regex = "[+\\-&|!(){}\\[\\]\"~*?:(\\)]";
		Pattern pattern = Pattern.compile(regex);

		try {
			// topic1 与 topic1_candidate 互斥使用
			if (topic1.length() > 0 && !topic1.equals("天气")&& e.isEmpty()) {
				canQuery = true;
				encodedTopic = URLEncoder.encode(topic1.toString().trim(),
						"UTF-8");
				query.append("+topic1:(");
				query.append(encodedTopic);
				query.append(")%20");
			} else if (e.isEmpty() && !loc.isEmpty()) {
				canQuery = true;
//				isRandom = false;
//				encodedTopic = URLEncoder
//						.encode(topic1.toString().trim(), "UTF-8");
//				 query.append("+topic1:(NOT%20");
//				  query.append(encodedTopic);
//				 query.append(")%20");
				 encodedTopic = URLEncoder
							.encode(loc.toString().trim(), "UTF-8");
				query.append("+relatedfeatures:(loc=");
				query.append(encodedTopic);
				query.append(")%20other:(ifeng%20OR%20ifengpc%20OR%20ifengtoutiao%20OR%20editor%20OR%20yidian%20OR%20dftoutiao)%20");
				topic1 = "";
			} else if (topic1_candidate.length() > 0 && e.isEmpty()) {
				canQuery = true;
				encodedTopic = URLEncoder.encode(topic1_candidate.toString()
						.trim(), "UTF-8");
				// query.append("+topic1:(");
				// query.append(encodedTopic);
				// query.append(")%20");
				query.append("relatedfeatures:(");
				query.append(encodedTopic);
				query.append(")%20");
			}
			if (topic2.length() > 0) {
				canQuery = true;
				isRandom = false;
				topic2.delete(topic2.length() - 4, topic2.length()); // trim the	encoded space in the end
				Matcher matcher = pattern.matcher(topic2);
				StringBuffer sb = new StringBuffer();
				while (matcher.find()) {
					matcher.appendReplacement(sb, "\\\\" + matcher.group());
				}
				matcher.appendTail(sb);
				topic2 = new StringBuffer(sb);
				query.append("topic2:(");
				encodedTopic = URLEncoder.encode(topic2.toString().trim(),
						"UTF-8");
				query.append(encodedTopic);
				query.append(")%20");
			}
			if (topic3.length() > 0 && (n > 0 || m > 2)) {
				canQuery = true;
				isRandom = false;
				topic3.delete(topic3.length() - 4, topic3.length());
				Matcher matcher = pattern.matcher(topic3);
				StringBuffer sb = new StringBuffer();
				while (matcher.find()) {
					matcher.appendReplacement(sb, "\\\\" + matcher.group());
				}
				matcher.appendTail(sb);
				topic3 = new StringBuffer(sb);
				query.append("topic3:(");
				encodedTopic = URLEncoder.encode(topic3.toString().trim(),
						"UTF-8");
				query.append(encodedTopic);
				query.append(")");
			}
			if (canQuery)
				query.append("&qf=topic1+topic2^2+topic3^3&defType=payload&rows=60&sort=score+desc,date+desc&mm="
						+ mm + "&fl=*,score");
			else if (source != null && !source.isEmpty()) {
				query.append("source:");
				encodedTopic = URLEncoder.encode(source, "UTF-8");
				query.append(encodedTopic).append(
						"&rows=60&sort=score+desc,date+desc");
				isRandom = true;
			} else
				return null;

		} catch (UnsupportedEncodingException e1) {
			log.error("[PREPROC] TOPICS CANNOT ENCODE!");
			return null;
		} finally {
			log.info("[PREPROC] TOPICS HANDLED");
		}

		String item_url = getRandomURL();

		query.insert(0, item_url);

		return query.toString();
	}

	private String makeQueryForTitle(String splitedTitle) {
		if (splitedTitle == null || splitedTitle.isEmpty()) {
			log.warn("[PREPROC] SPLITED TITLE IS NULL!");
			return null;
		}
		log.info("[RelatedOnline] Splited title input is " + splitedTitle);

		String str_query = makeIndexFeature(splitedTitle);
		if (str_query == null || str_query.isEmpty()) {
			log.info("[RelatedOnline] NO SOLID CONTENT TO GENERATE QUERY!");
			return null;
		}
		// System.out.println(str_query);
		// str_query = "nba";

		String encodedTopic;
		// 生成访问item 投放 solr query
		StringBuffer query = new StringBuffer("select?q=title:(");
		try {
			encodedTopic = URLEncoder.encode(str_query, "UTF-8");
			query.append(encodedTopic);
			query.append(")");
		} catch (UnsupportedEncodingException e1) {
			log.error("[PREPROC] TITLE CANNOT ENCODE!");
			return null;
		} finally {
			log.info("[PREPROC] TITLE HANDLED");
		}

		String item_url = getRandomURL();

		query.insert(0, item_url);
		query.append("&rows=15&q.op=OR&sort=score+desc,date+desc");
		return query.toString();
	}

	private List<String> getUltimateList(String splitedTitle, NodeList results,
			int docNum, String type) {
		ArrayList<String> resultList = handleHttpRequestResult(results,
				channelWord, splitedTitle, isRandom, docNum, type);
		if (resultList == null)
			return null;
		else if (resultList.size() < docNum + 1 && !resultList.get(0).isEmpty()
				&& !isRandom) {
			// System.out.println(resultList);
			StringBuffer secondQuery = new StringBuffer("select?q=");
			String encodedTopic;
			try {
				if (topic1.length() > 0) {
					channelWord = topic1;
					encodedTopic = URLEncoder.encode(topic1.toString().trim(),
							"UTF-8");
					secondQuery.append("+topic1:(");
					secondQuery.append(encodedTopic);
					secondQuery.append(")%20");
				} else if (topic1_candidate.length() > 0) {
					channelWord = topic1_candidate;
					encodedTopic = URLEncoder.encode(topic1_candidate
							.toString().trim(), "UTF-8");
					// secondQuery.append("+topic1:(");
					// secondQuery.append(encodedTopic);
					// secondQuery.append(")%20OR%20");
					secondQuery.append("relatedfeatures:(");
					secondQuery.append(encodedTopic);
					secondQuery.append(")%20");
				}
			} catch (UnsupportedEncodingException e) {
				log.error("[PREPROC] TOPICS CANNOT ENCODE!");
				return null;
			}
			secondQuery.insert(0, getRandomURL());
			secondQuery
					.append("&qf=topic1+topic2^2+topic3^3&defType=payload&rows=100&sort=score+desc,date+desc&fl=*,score");
			NodeList results2 = getHttpRequestResult(secondQuery.toString());
			resultList = handleHttpRequestResult(results2, channelWord,
					splitedTitle, true, docNum, type);
		}
		return resultList;
	}

	private static String getRandomURL() {
		Random random = new Random();
		String queryPort = config.getValueBykey("RelatedQueryPort");
		String itemSolrUrl1 = "http://10.32.28.116:" + queryPort
				+ "/solr46/item/";
		String itemSolrUrl2 = "http://10.32.28.117:" + queryPort
				+ "/solr46/item/";
		String itemSolrUrl3 = "http://10.32.28.120:" + queryPort
				+ "/solr46/item/";
		double d_value = random.nextDouble();
		BigDecimal d_value_bd = new BigDecimal(d_value);
		BigDecimal range0 = new BigDecimal(0.0);
		BigDecimal range1 = new BigDecimal(0.333333);
		BigDecimal range2 = new BigDecimal(0.666667);

		if (d_value_bd.compareTo(range0) >= 0
				&& d_value_bd.compareTo(range1) < 0) {
			return itemSolrUrl1;
		} else if (d_value_bd.compareTo(range1) >= 0
				&& d_value_bd.compareTo(range2) < 0) {
			return itemSolrUrl2;
		} else
			return itemSolrUrl3;
	}

	/**
	 * 
	 * @param results
	 *            http直连返回的response中xml文件对应的results NodeList
	 * @param docNum
	 *            相关ids的数目
	 * @param channelWord
	 *            给出的频道词
	 * @param splitedTitle
	 *            目标文章的分词后标题
	 * @param isRandom
	 *            是否需要随机选取返回结果
	 * @param type
	 *            选取结果的应用场景，如app, wap等
	 * @return 选取的c0或c1 + 相关ids
	 */

	private static ArrayList<String> handleHttpRequestResult(NodeList results,
			String channelWord, String splitedTitle, boolean isRandom,
			int docNum, String type) {
		// handle returned results
		if (results == null) {
			log.warn("[FILTER RESULT] Response results is NULL!!");
			return null;
		}
		if (type == null || type.isEmpty()) {
			log.error("[FILTER RESULT] Filter type is NULL!!");
			return null;
		}
		if (docNum <= 0) {
			log.error("[FILTER RESULT] Query doc num is ZERO or negative!!");
			return null;
		}
		if (isRandom)
			log.info("[FILTER RESULT] Will select ids randomly");

		String condition = null;
		if (type.equals("app"))
			condition = "yidian ifeng dftoutiao"; //20160816 added 东方头条
		else if (type.equals("wap"))
			condition = "ifeng";

		if (condition == null) {
			log.error("[FILTER RESULT] Cannot recognise type");
			return null;
		}
		String[] conditions = condition.split(" ");

		int size = Math.min(docNum, results.getLength());
//		size = Math.min(4, size);
		int j = 0;
		Set<String> clusterIDSet = new HashSet<String>(docNum);
		Set<String> titleSet = new HashSet<String>(docNum + 1);
		ArrayList<String> resultList = new ArrayList<String>();
		ArrayList<String> candidatesIds = new ArrayList<String>(docNum);

		if (channelWord.equals("财经1"))
			channelWord = "";
		resultList.add(channelWord);

		splitedTitle = splitedTitle.replaceAll("_[a-z]+", "");
		titleSet.add(splitedTitle.trim());

		log.info("[Related] Begin to select ids");
		ArrayList<Long> arrayTimeCandi = new ArrayList<Long>(docNum);
		ArrayList<Long> arrayTimeResult = new ArrayList<Long>(docNum);
		Random r1 = new Random();
		int k = (isRandom ? r1.nextInt(10) : 0);
		for (; k < results.getLength() && j < size; k++) {
			Node doc = results.item(k);
			NodeList docContent = doc.getChildNodes();

			String id = doc.getFirstChild().getTextContent();
			if (id == null || id.isEmpty())
				continue;

			Node otherNode = docContent.item(11);
			String other = otherNode.getTextContent();
			if (other == null)
				continue;
			other = other.trim();
			// if (!other.contains("yidian") && !other.contains("ifeng") ||
			// other.contains("illegal"))
			// continue;

			if (other.contains("illegal"))
				continue;
			boolean jump = true;
			for (String c : conditions) {
				if (other.contains(c)) {
					jump = false; // 不跳过
					break;
				}
			}
			if (jump)
				continue;
			
			String currentTopic1 = docContent.item(1).getTextContent();
			if(currentTopic1 == null || currentTopic1.contains("天气"))
				continue;		
			
			long gaptime = -1;
			String str_date = docContent.item(5).getTextContent();
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd\'T\'HH:mm:ss\'Z\'");
			Date date = null;
			try {
				date = format.parse(str_date);
				gaptime = System.currentTimeMillis() - date.getTime();

			} catch (ParseException e) {
				log.error("[FILTER RESULT] DATE STRING CANNOT BE PARSED!!");
				continue;
			}
			if (channelWord.equals("社会八卦") || channelWord.equals("国际")
					|| channelWord.equals("港澳") || channelWord.equals("台湾")
					|| channelWord.equals("大陆时事") || channelWord.equals("军事")
					|| channelWord.equals("时政")) {
				if (gaptime > (long) 30 * 24 * 3600 * 1000 || gaptime <= 0)
					continue;
				// System.out.println(topic1);
			}

			String clusterID = docContent.item(10).getTextContent();
			if (clusterID != null && !clusterID.isEmpty()) {
				if (clusterIDSet.contains(clusterID))
					continue;
				else
					clusterIDSet.add(clusterID);
			}

			String str_title = docContent.item(4).getTextContent();
			if (str_title == null)
				continue;
			// String str_title = "天津新闻 （2015 11  29）";
			int count = 0;
			String regEx = "[\\u4e00-\\u9fa5]";
			Pattern pa = Pattern.compile(regEx);
			Matcher m = pa.matcher(str_title);
			while (m.find())
				count++;
			String shrinkedTitle = str_title.replaceAll("\\s+", "");
			// System.out.println(count);
			if (count < 8 || shrinkedTitle.contains("正播") 
					|| shrinkedTitle.contains("直播") || shrinkedTitle.contains("今日")
					|| shrinkedTitle.contains("今天") || shrinkedTitle.contains("明日")
					|| shrinkedTitle.contains("明天") || shrinkedTitle.contains("昨天")
					|| shrinkedTitle.contains("昨日") || shrinkedTitle.contains("一周")
					|| shrinkedTitle.contains("本周") || shrinkedTitle.contains("今明")
					|| shrinkedTitle.matches(".*第.{1,2}日.*"))
				continue;
			boolean flag = false;
			for (String s : titleSet) {
				if (isSimilarity(s, str_title)) {
					flag = true;
					break;
				}
			}

			if (flag)
				continue;

			double score = 0.0;
			String temp = doc.getLastChild().getTextContent();
			if (temp == null)
				continue;
			score = Double.valueOf(temp);
			// System.out.println(score);
			String item2app = docContent.item(7).getTextContent();
			int selectResult = selectPriority(score, item2app);
			if (selectResult == 0 || selectResult == 3)
				continue;
			else if (selectResult == 2) {
//				if (candidatesIds.size() < docNum)
					flag = true; // 待定
					// candidatesIds.add(id);
					// continue;
			}

			//candidates 和 results 分别排序
			long currentTime = date.getTime();
			if (flag) {
				if (arrayTimeCandi.size() == 0
						|| currentTime > arrayTimeCandi.get(0)) {
					candidatesIds.add(0, id);
					arrayTimeCandi.add(0, currentTime);
				} else {
					boolean f = false;
					for (int p = 1; p < arrayTimeCandi.size(); p++) {
						if (currentTime >= arrayTimeCandi.get(p)) {
							candidatesIds.add(p, id);
							f = true;
							// resultList.add(p*2 + 1 , id); //测试时使用
							// resultList.add(p*2 + 2 ,str_title); //测试时使用
							arrayTimeCandi.add(p, currentTime);
							break;
						}
					}
					if (!f) {
						candidatesIds.add(id);
						// resultList.add(str_title); //测试时使用
						arrayTimeCandi.add(currentTime);
					}
				}
			} else {
				if (arrayTimeResult.size() == 0
						|| currentTime > arrayTimeResult.get(0)) {// 当前时间新

					resultList.add(1, id);
					j++;
					// resultList.add(2,str_title); //测试时使用
					arrayTimeResult.add(0, currentTime);
				} else {
					boolean f = false;
					for (int p = 1; p < arrayTimeResult.size(); p++) {
						if (currentTime >= arrayTimeResult.get(p)) {
							resultList.add(p + 1, id);
							j++;
							f = true;
							// resultList.add(p*2 + 1 , id); //测试时使用
							// resultList.add(p*2 + 2 ,str_title); //测试时使用
							arrayTimeResult.add(p, currentTime);
							break;
						}
					}
					if (!f) {
						resultList.add(id);
						j++;
						// resultList.add(str_title); //测试时使用
						arrayTimeResult.add(currentTime);
					}
				}
			}

//			// 旧手动排序
//			
//			if (arrayTimeResult.size() == 0 || currentTime > arrayTimeResult.get(0)) {// 当前时间新
//				if(flag)
//					candidatesIds.add(0,id);
//				else {
//					resultList.add(1, id);
//					j++;
//				}
////				 resultList.add(2,str_title); //测试时使用
//				arrayTimeResult.add(0, currentTime);
//			} else {
//				boolean f = false;
//				for (int p = 1; p < arrayTimeResult.size(); p++) {
//					if (currentTime >= arrayTimeResult.get(p)) {
//						if(flag)
//							candidatesIds.add(p, id);
//						else {
//							resultList.add(p + 1, id);
//							j++;
//						}
//						f = true;
////						 resultList.add(p*2 + 1 , id); //测试时使用
////						 resultList.add(p*2 + 2 ,str_title); //测试时使用
//						arrayTimeResult.add(p, currentTime);
//						break;
//					}
//				}
//				if (!f) {
//					if(flag)
//						candidatesIds.add(id);
//					else {
//						resultList.add(id);
//						j++;
//					}
////					 resultList.add(str_title); //测试时使用
//					arrayTimeResult.add(currentTime);
//				}
//			}
//			// System.out.println(arrayTime);

			titleSet.add(str_title);
		}

		if (candidatesIds.size() > 0 && resultList.size() < (size + 1) ){
			int difference = size + 1 - resultList.size();
			int toIndex = Math.min(difference, candidatesIds.size());
			resultList.addAll(candidatesIds.subList(0, toIndex));
		}
		// System.out.println(candidatesIds);
		return resultList;
	}

	private static int selectPriority(double score, String item2app) {

		if (item2app == null)
			return 0;
		if ((item2app.contains("\"docType\":\"docpic\"") || item2app
				.contains("\"docType\":\"slide\"")) && score >= 1.1)
			return 1;
		else if ((item2app.contains("\"docType\":\"doc\"") && score >= 1.1)
				|| ((item2app.contains("\"docType\":\"docpic\"") || item2app
						.contains("\"docType\":\"slide\"")) && score < 1.1))
			return 2;
		else
			return 3;
	}

	private static NodeList getHttpRequestResult(String query) {
		// 建立http 直连
		HttpURLConnection urlc = null;
		try {
			URL url = new URL(query);
			urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestMethod("GET");
		} catch (MalformedURLException e) {
			log.warn("[SOLR SERVER CONNECTION] MalformedURLException!");
		} catch (IOException e) {
			log.warn("[SOLR SERVER CONNECTION] IOException!");
		}
		if (urlc == null)
			return null;
		urlc.setDoOutput(true);
		urlc.setDoInput(true);
		urlc.setUseCaches(false);
		urlc.setAllowUserInteraction(false);
		urlc.setRequestProperty("accept", "*/*");
		urlc.setRequestProperty("connection", "Keep-Alive");
		urlc.setConnectTimeout(10000);
		urlc.setReadTimeout(10000);

		Element response = null;
		InputStream in = null;

		log.info("[Related item query] " + query);
		try {
			if (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
				if (urlc.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
						|| urlc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					log.error("Get failed:");
				}
				log.error("Solr returned an error :" + " ErrorCode="
						+ urlc.getResponseCode());
			}
			in = urlc.getInputStream();
			response = parseXML(in);
		} catch (IOException e) {
			log.error("[Related QUERY] IOException while reading response:"
					+ " Exception=" + e);
		} catch (Exception e) {
			log.error("[Related QUERY] Exception while reading response: word="
					+ " Exception=" + e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch (IOException x) {
				log.error("[Related QUERY] IOException while reading response: word="
						+ " Exception=" + x);
			}
		}

		if (response == null) {
			log.error("[QUERY] CANNOT GET QUERY RESPONSE!");
			return null;
		}

		NodeList results = response.getElementsByTagName("result").item(0)
				.getChildNodes();
		if (results == null || results.getLength() <= 0) {
			log.warn("[QUERY] QUERY RESULT IS INVALID!!");
			return null;
		}
		return results;
	}

	/**
	 * 将http链接读入的返回xml流解析成为xml document
	 * 
	 * @param is
	 *            http链接的输入流
	 * @return xml document
	 */
	private static Element parseXML(InputStream is) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			Element root = document.getDocumentElement();
			return root;
		} catch (ParserConfigurationException e) {
			log.error("Exception while parsing response messages"
					+ " Exception=" + e);
		} catch (SAXException e) {
			log.error("Exception while parsing response messages"
					+ " Exception=" + e);
		} catch (IOException e) {
			log.error("Exception while parsing response messages"
					+ " Exception=" + e);
		}
		return null;
	}

	/**
	 * 
	 * @param str1
	 *            已有标题集合中的标题
	 * @param str2
	 *            为待匹配标题
	 * @return
	 */

	public static boolean isSimilarity(String str1, String str2) {
		if (str1 == null || str2 == null) {
			return true;
		}
		// str1 = str1.replaceAll("._w", "");
		// str2 = str2.replaceAll("._w", "");

		String[] str = str1.split("\\s+");
		String[] targetStr = str2.split("\\s+");
		int[] common = new int[targetStr.length];

		for (int i = 0; i < targetStr.length; i++) {
			// if(str2[i].length() > 1 )
			// usableWordCount++;
			common[i] = 0; // 初始化
			for (int j = 0; j < str.length; j++) {
				if (str[j].equals(targetStr[i])) {
					common[i] = 1;
					if (i == j)
						common[i] += 1;
					break;
				}
			}
		}

		int commonWord = common[0];// 共词数目
		int maxSccssStart = 0; // 最大连续起始
		int maxSccssEnd = -1; // 最大连续末尾
		int maxSccss = -100;
		boolean ifSccss = false;
		for (int i = 1; i < common.length; i++) {
			commonWord += common[i];
			if (common[i] > 0 && common[i - 1] > 0)
				ifSccss = true;
			else
				ifSccss = false;

			if (ifSccss)
				maxSccssEnd = i;
			else {
				if (maxSccss < (maxSccssEnd - maxSccssStart + 1))
					maxSccss = maxSccssEnd - maxSccssStart + 1;
				maxSccssStart = i;
			}
		}

		maxSccss = Math.max(maxSccssEnd - maxSccssStart + 1, maxSccss);

		if ((double) commonWord / targetStr.length > 0.6)
			return true;
		else if (maxSccss >= 4)
			return true;
		return false;
	}

	private static String makeIndexFeature(String s_title) {

		Map<String, Integer> titleMap = new HashMap<String, Integer>();
		// String s_title = item.getSplitTitle();
		if (s_title == null) {
			log.info("Item's splited is null!!");
		} else {
			// System.out.println("s_title is "+s_title);
			// 处理title，转换成solr可以接受的形式
			String[] titleArray = s_title.split("\\s+");
			// 将title中的词先放入titleMap中
			for (int i = 0; i < titleArray.length; i++) {
				int q = titleArray[i].indexOf("_");
				int weight = 1;
				if (q < 0)
					continue;
				if (titleArray[i].substring(q + 1).equals("w"))// 去除标点符号
					continue;
				else if (titleArray[i].substring(q + 1).equals("x"))// 去除标点符号
					weight = 3;
				else if (titleArray[i].substring(q + 1).equals("et"))// 去除标点符号
					weight = 5;
				String unit = "";
				if (swSet.contains(unit = titleArray[i].substring(0, q))) // 去除停用词
					continue;
				Integer Int = titleMap.get(unit);
				if (Int == null)
					Int = weight;
				else
					Int += weight;
				titleMap.put(unit, Int);
			}
		}

		StringBuffer sb = new StringBuffer(); // 盛放结果的stringbuffer

		String regex = "[+\\-&|!(){}\\[\\]^\"~*?:(\\)]";
		Pattern pattern = Pattern.compile(regex);

		for (Map.Entry<String, Integer> entry : titleMap.entrySet()) {
			String keyWord = entry.getKey();
			StringBuffer sb1 = new StringBuffer();
			Matcher matcher = pattern.matcher(keyWord);
			while (matcher.find()) {
				matcher.appendReplacement(sb1, "\\\\" + matcher.group());
			}
			matcher.appendTail(sb1);
			if (keyWord.equals("OR"))
				keyWord = "or";
			keyWord = sb1.toString();
			sb.append(keyWord).append("^").append(entry.getValue()).append(" ");
		}

		String text = sb.toString().trim();
		// System.out.println(text);
		return text;
	}

	public static void main(String[] args) {
		IKVOperationv2 ikvop = new IKVOperationv2("appitemdb");
		itemf item = ikvop.queryItemF("113721187", "x");
		//
		RelatedItem obj = new RelatedItem();
		Map<String, List<String>> idList = obj.getRelatedItemId(
				item.getFeatures(), item.getSplitTitle(), item.getSource());
		System.out.println(idList);

		// String[] feature =
		// {"财经1","c","-1.0","财经","c","0.5","奥运","c","0.1","黄金","sc","0.5","体育高清图","cn","1.0"};
		// // String[] feature = {};
		// String title =
		// "太阳_h 最_k 毒_k 时候_n 做_v 的_u 姜_h 茶_e ，_w 最_k 驱寒_v";
		// RelatedItem obj = new RelatedItem();
		// Map<String, List<String>> idList = obj.getRelatedItemId(new
		// ArrayList<String>(Arrays.asList(feature)), title, "凤凰财经");
		// //
		// System.out.println(idList);
		ikvop.close();
		System.exit(0);
	}
}
