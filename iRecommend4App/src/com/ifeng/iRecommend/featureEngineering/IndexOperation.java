package com.ifeng.iRecommend.featureEngineering;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;


/**
 * 
 * <PRE>
 * 作用 : 用solr建立文章索引，并处理索引更新和查询操作
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
 *          2.0          2015年4月23日        lixiao          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class IndexOperation {
	private final static Logger log = Logger.getLogger(IndexOperation.class);

	private static String baseUrl = LoadConfig
			.lookUpValueByKey("solrServerUrl");
	private static String pathStopword = LoadConfig
			.lookUpValueByKey("StopWordPath");
	private final Set<String> swSet = new HashSet<String>();
	
//	private static String pathWordIdf = LoadConfig
//			.lookUpValueByKey("WordIdfPath");
//	private static Map<String, Double> idfMap = new HashMap<String, Double>();

	private URL solrUrl;
	private static final Integer rows = 100;

	private IndexOperation() {

//		 加载停用词
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

		if (baseUrl == null || baseUrl.isEmpty()) {
			log.error("[QUERY] SOLR'S URL WASN'T CONFIGURIED!!");
		}
	}
	
	 private static class Inner{   //私有的静态内部类    
	        static IndexOperation instance = new IndexOperation();  
	    } 

	public static IndexOperation getInstance() {
		return Inner.instance;
	}

	/**
	 * 查询函数
	 * 
	 * @param item
	 *            传入的query 为需要查询的item 返回相似top100 2015.09.23 若item
	 *            title为null，则可根据内容抽取含有实体术语的关键片段用作查询近似top100
	 *            2015.09.23
	 * @return 返回查询到的itemf list 可能为null and empty
	 */

	public ArrayList<itemf> queryFromSolr(itemf item) {
		if (item == null) {
			log.warn("[QUERY] ITEM IS NULL!!");
			return null;
		}

		String str_query = makeIndexFeature(item, true);//根据标题和features形成query
		if (str_query == null || str_query.isEmpty()){
			log.info("NO SOLID CONTENT TO GENERATE QUERY!");
			return null;
		}
		
//		System.out.println(str_query);
//		str_query = "nba";
		str_query = replaceInvaldateCharacter(str_query);
		try {
			str_query = URLEncoder.encode(str_query,"UTF-8");
//			System.out.println(str_query);
		} catch (UnsupportedEncodingException e1) {
			log.warn("[QUERY] QUERY CANNOT BE ENCODED!!");
			return null;
		}
		StringBuffer query = new StringBuffer(str_query);
		query.insert(0,"q=text:(").append(")&q.op=OR&rows=").append(rows);
		
		String postUrl = baseUrl + "select?" + query.toString();
//		postUrl = URLEncoder.encode(postUrl,"UTF-8");
//		System.out.println(postUrl);
		try {
			solrUrl = new URL(postUrl);
		} catch (MalformedURLException e1) {
			log.error("[QUERY] MalformedURLException while quering!!" + e1);
			return null;
		}
		
		Element response = null;
		HttpURLConnection urlc = null;
		InputStream in = null;
		urlc = (HttpURLConnection) prepareURLCon("GET");
		if(urlc == null){
			return null;
		}
		urlc.setRequestProperty("accept", "*/*");
		urlc.setRequestProperty("connection", "Keep-Alive");
		urlc.setConnectTimeout(10000);
		urlc.setReadTimeout(10000);
		try {
//			urlc.connect();
			if (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
				if (urlc.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
						|| urlc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					log.error("Get failed:");
				}
				log.error("Solr returned an error :" + " ErrorCode="
						+ urlc.getResponseCode());
			}
			in = urlc.getInputStream();
			response = parseXML(in); //处理http链接返回的xml流
			// pipe(in, System.out);
		} catch (IOException e) {
			log.error("[QUERY] IOException while reading response:" + " Exception=" + e);
		} catch (Exception e) {
			log.error("[QUERY] Exception while reading response: word=" + " Exception="
					+ e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch (IOException x) {
				log.error("[QUERY] IOException while shutting the http connection"
						+ x);
			}
		}
		
		if(response == null){
			log.error("[QUERY] CANNOT GET QUERY RESPONSE!");
			return null;
		}
			
		NodeList results = response.getElementsByTagName("result").item(0).getChildNodes();
		if(results == null || results.getLength() <= 0){
			log.warn("[QUERY] QUERY RESULT IS INVALID!!");
			return null;
		}
		ArrayList<itemf> resultList = new ArrayList<itemf>(rows);
		for(int i = 0; i < results.getLength(); i ++){
			Node doc = results.item(i);
			Node docOthers = doc.getChildNodes().item(2);
			if(docOthers == null)
				continue;
			String str_json = docOthers.getTextContent();
//			 System.out.println(str_json);
			itemf tempItem = JsonUtils.fromJson(str_json, itemf.class);
			if (tempItem != null)
				resultList.add(tempItem);
		}
		return resultList;
	}
	
	/**
	 * 以单个itemf更新solr index
	 * 
	 * @param item
	 */
	synchronized public void addDocToSolr(itemf item) {
		if (item == null)
			return;
		if(item.getID() == null || item.getID().isEmpty()){
			log.error("[Add to solr] ITEM ID IS MISSING!!");
			return;
		}
		String text = makeIndexFeature(item, false).toString().trim();

		if(text == null || text.isEmpty()){
			log.error("[Add to solr] CANNOT BUILD INDEX FOR ITEM!!");
			return;
		}
		String str_json = JsonUtils.toJson(item);
		if(str_json == null || str_json.isEmpty()){
			log.error("[Add to solr] CANNOT MAKE ITEM TO JSON!!");
			return;
		}
		try {
			solrUrl = new URL(baseUrl  + "update");

			//手动生成xml流
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			org.w3c.dom.Document xmlDoc = null;
			Element root = null;

			xmlDoc = builder.newDocument();
			xmlDoc.setXmlStandalone(true);
			root = xmlDoc.createElement("add");

			Element doc = generateXmlElement(xmlDoc, item.getID(), text, str_json);
			if (doc == null){
				log.error("[Add to solr] CANNOT MAKE ADD XML DOC!!");
				return;
			}
			root.appendChild(doc);
			Element commit = xmlDoc.createElement("commit");
			commit.setAttribute("softCommit", "true");
			root.appendChild(commit);
			xmlDoc.appendChild(root);

			String str_xml = docToString(xmlDoc);
//			 System.out.println(str_xml);
			str_xml = replaceInvaldateCharacter(str_xml); //去除非法字符
			
			Element response = null;
			response = postXml(str_xml, solrUrl); //写入xml流 到 http connection
			if(response == null)
				return;
			NodeList children = response.getElementsByTagName("int");
			String qtime = children.item(1).getTextContent();
			log.info("Added item " + item.getID()
					+ " to solr. Time taken = " + qtime);
		} catch (MalformedURLException e) {
			log.error("MalformedURLException while updating index [updating item list]:"
					+ " Exception=" + e, e);
		} catch (ParserConfigurationException e) {
			log.error("ParserConfigurationException while updating index [updating item list]:"
					+ " Exception=" + e, e);
		}
	}

	/**
	 * Make a xml document for solr index building
	 * @param xmlDoc xmlDoc frame
	 * @param id item id
	 * @param text the features of the item needed to index
	 * @param json the json of the item for OTHER field of solr index
	 * @return whole xml document with all corresponding arguments 
	 */
	private Element generateXmlElement(org.w3c.dom.Document xmlDoc, String id, String text, String json) {
		
		Element doc = xmlDoc.createElement("doc");
		Text textNode = xmlDoc.createTextNode("");
		doc.appendChild(textNode);

		Element e = xmlDoc.createElement("field");
		e.setAttribute("name", "id");
		textNode = xmlDoc.createTextNode(id);
		e.appendChild(textNode);
		doc.appendChild(e);

		e = xmlDoc.createElement("field");
		e.setAttribute("name", "text");

		textNode = xmlDoc.createTextNode(text);
		e.appendChild(textNode);
		doc.appendChild(e);

		e = xmlDoc.createElement("field");
		e.setAttribute("name", "other");
		textNode = xmlDoc.createTextNode(json);
		e.appendChild(textNode);
		doc.appendChild(e);
		
		return doc;
	}

	/**
	 * Post the xml document stream as a string through the http connection
	 * @param xmlFile  
	 * @param url 
	 * @return the response xml document after posting 
	 */
	Element postXml(String xmlFile, URL url) {
		
		HttpURLConnection urlc = null;
		Element response = null;
		urlc = (HttpURLConnection) prepareURLCon("POST");

		if (urlc == null)
			return null;
		urlc.setRequestProperty("Content-type", "text/xml");
		urlc.setChunkedStreamingMode(1024000);
		InputStream in = null;
		try {
			OutputStream out = urlc.getOutputStream();
			OutputStreamWriter outputStrm = new OutputStreamWriter(out, "UTF-8");
			outputStrm.write(xmlFile);
			outputStrm.flush();
			outputStrm.close();

			if (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
				if (urlc.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
						|| urlc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					log.error("post failed:");
				}
				log.error("Solr returned an error :" + " ErrorCode="
						+ urlc.getResponseCode());
			}

			in = urlc.getInputStream();
			response = parseXML(in);
			// pipe(in, System.out);
		} catch (IOException e) {
			log.error("[Add to solr] IOException while reading response:" + " Exception=" + e);
		} catch (Exception e) {
			log.error("Exception while reading response: word=" + " Exception="
					+ e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch (IOException x) {
			}
		}
		return response;
	}

	/**
	 * Initialize the http connection to solr index 
	 * @param requestMethod
	 * @return http connection
	 */
	HttpURLConnection prepareURLCon(String requestMethod) {
		HttpURLConnection urlc = null;
		try {
			urlc = (HttpURLConnection) solrUrl.openConnection();
			urlc.setRequestMethod(requestMethod);
		} catch (ProtocolException e) {
			log.error("Method cannot be reset or POST method is invaild!!"
							+ e);
		} catch (IOException e) {
			log.error("Connection cannot be estabilished!"
					+ e);
		}
		urlc.setDoOutput(true);
		urlc.setDoInput(true);
		urlc.setUseCaches(false);
		urlc.setAllowUserInteraction(false);
		return urlc;
	}
	/**
	 * 将http链接读入的返回xml流解析成为xml document
	 * @param is http链接的输入流
	 * @return xml document
	 */
	public static Element parseXML(InputStream is){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			Element root = document.getDocumentElement();
			return root;
		} catch (ParserConfigurationException e) {
			log.error("Exception while parsing response messages" + " Exception="
					+ e);
		} catch (SAXException e) {
			log.error("Exception while parsing response messages" + " Exception="
					+ e);
		} catch (IOException e) {
			log.error("Exception while parsing response messages" + " Exception="
					+ e);
		}
		return null;
	}

	String replaceInvaldateCharacter(String text) {
		char d = (char) 0x20;

		if (text != null) {
			char[] data = text.toCharArray();
			for (int i = 0; i < data.length; i++) {
				if (!isXMLCharacter(data[i])) {
					data[i] = d;
				}
			}
			return new String(data).replaceAll("&\\w{1,6};", " ").replaceAll(
					"&", "&amp;");
		}
		return "";
	}

	boolean isXMLCharacter(int c) {
		if (c <= 0xD7FF) {
			if (c >= 0x20)
				return true;
			else
				return c == '\n' || c == '\r' || c == '\t';
		}
		return (c >= 0xE000 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0x10FFFF);
	}

	/**
	 * Translate the xml document to a String
	 * @param doc
	 * @return the corresponding string
	 */
	private String docToString(Document doc) {
		// XML转字符串
		String xmlStr = "";
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty("encoding", "UTF-8");// 解决中文问题，试过用GBK不行
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(doc), new StreamResult(bos));
			xmlStr = bos.toString();
		} catch (TransformerConfigurationException e) {
			log.error("TransformerConfigurationException:" + e);
		} catch (TransformerException e) {
			log.error("TransformerException:" + e);
		} catch (Exception e) {
			log.error("docToString:" + e);
		}
		return xmlStr;
	}
	

	/**
	 * 根据item术语扩展搜索特征
	 * @param item
	 * @return
	 */
	private String makeIndexFeature(itemf item, boolean isQuery) {
		if (item == null){
			log.error("ITEM IS NULL!!");
			return null;
		}
		Map<String, Integer> titleMap = new HashMap<String, Integer>();
		String s_title = item.getSplitTitle();
		if (s_title == null || s_title.isEmpty()) {
			log.info("Item " + item.getID() + " splited is null!!");
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

		//handling the features of the item
		ArrayList<String> features = item.getFeatures();
		if (features == null || features.isEmpty())
			log.info("Item " + item.getID() + " features are null");
		else {
			int max_weight1_feature = 15; //The maximum number of one-weighted features
			int weight1_feature = 0; //The current number of one-weighted features
			for (int i = 2; i < features.size(); i += 3) {
				if (features.get(i - 1).contains("k")
						|| features.get(i - 1).equals("et")
						|| features.get(i - 1).equals("x") || features.get(i - 1).equals("e")) {
					String candidate = features.get(i - 2);
					if(candidate == null || candidate.isEmpty())
						continue;
					double weight = Double.valueOf(features.get(i));
					weight = Math.abs(weight);
					if(weight == 0.1){
						weight1_feature++;
						if(weight1_feature > max_weight1_feature)
							continue;
					}
					titleMap.put(candidate, (int) (weight * 10));
				}
			}
		}
		StringBuffer sb = new StringBuffer(); // 盛放结果的stringbuffer
		if (isQuery) {
			String regex = "[+\\-&|!(){}\\[\\]^\"~*?:(\\)]"; //Escaping characters
			Pattern pattern = Pattern.compile(regex);
			
			for (Map.Entry<String, Integer> entry : titleMap.entrySet()) {
				String keyWord = entry.getKey();
				StringBuffer sb1 = new StringBuffer();
				Matcher matcher = pattern.matcher(keyWord);
				while (matcher.find()) {
					matcher.appendReplacement(sb1, "\\\\" + matcher.group());
				}
				matcher.appendTail(sb1);
				keyWord = sb1.toString();
				
				if(keyWord.equals("OR"))
					keyWord = "or";
				sb.append(keyWord).append("^").append(entry.getValue())
						.append(" ");
			}
		} else {
			for (Map.Entry<String, Integer> entry : titleMap.entrySet()) {
				for (int i = 0; i < entry.getValue(); i++) {
					sb.append(entry.getKey()).append(" ");
				}
			}
		}
		
		String text = sb.toString().trim();
//		System.out.println(text);
		return text;
	}
	
	synchronized public void addDocToSolrByUsingClient(itemf item) {
		addDocToSolr(item);
	}
	
	/**
	 * 批量查询连续solr id
	 * 
	 * @param start 连续id起始
	 * @param end 连续id结束
	 * @return 查询返回的itemList
	 * 
	 * 查询返回包括等于起始和结束的id对应的item
	 * 不建议输入跨度过大的id序列
	 */
	public List<itemf> queryByIds(int start, int end){
		if(start < 0 || end < 0 || end - start < 0)
			return null;
		
		StringBuffer sb = new StringBuffer();
		for(int i = start; i < end; i ++){
			sb.append(i).append("%20OR%20");
		}
		sb.append(end);
		String postUrl = baseUrl + "select?q=id:(" + sb + ")&fl=other&rows=" + (end - start + 1) ;
//		System.out.println(postUrl);
		try {
			solrUrl = new URL(postUrl);
		} catch (MalformedURLException e1) {
			log.error("[QUERY] MalformedURLException while quering!!" + e1);
		}
		
		Element response = null;
		HttpURLConnection urlc = null;
		InputStream in = null;
		urlc = (HttpURLConnection) prepareURLCon("GET");
		if(urlc == null){
			return null;
		}
		urlc.setRequestProperty("accept", "*/*");
		urlc.setRequestProperty("connection", "Keep-Alive");
		urlc.setConnectTimeout(1000);
		urlc.setReadTimeout(10000);
		try {
//			urlc.connect();
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
			// pipe(in, System.out);
		} catch (IOException e) {
			log.error("[QUERY] IOException while reading response:" + " Exception=" + e);
			log.error(start + "xxx " + end);
		} catch (Exception e) {
			log.error("[QUERY] Exception while reading response: word=" + " Exception="
					+ e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch (IOException x) {
			}
		}
		
		if(response == null){
			log.error("[QUERY] CANNOT GET QUERY RESPONSE!");
			return null;
		}
			
		NodeList results = response.getElementsByTagName("result").item(0).getChildNodes();
		if(results == null || results.getLength() <= 0){
			log.warn("[QUERY] QUERY RESULT IS INVALID!!");
			return null;
		}
		ArrayList<itemf> resultList = new ArrayList<itemf>(rows);
		for(int i = 0; i < results.getLength(); i ++){
			Node doc = results.item(i);
			Node docOthers = doc.getFirstChild();
			if(docOthers == null)
				continue;
			String str_json = docOthers.getTextContent();
//			 System.out.println(str_json);
			itemf tempItem = JsonUtils.fromJson(str_json, itemf.class);
			if (tempItem != null)
				resultList.add(tempItem);
		}
		return resultList;
	}

	public List<itemf> queryByIds(String ids[]){
		if(ids == null || ids.length <= 0)
			return null;
		
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < ids.length - 1; i ++){
			if(ids[i] == null)
				continue;
			sb.append(ids[i]).append("%20OR%20");
		}
		sb.append(ids[ids.length - 1]);
		String postUrl = baseUrl + "select?q=id:(" + sb + ")&fl=other&rows=" + ids.length ;
//		System.out.println(postUrl);
		try {
			solrUrl = new URL(postUrl);
		} catch (MalformedURLException e1) {
			log.error("[QUERY] MalformedURLException while quering!!" + e1);
		}
		
		Element response = null;
		HttpURLConnection urlc = null;
		InputStream in = null;
		urlc = (HttpURLConnection) prepareURLCon("GET");
		if(urlc == null){
			return null;
		}
		urlc.setRequestProperty("accept", "*/*");
		urlc.setRequestProperty("connection", "Keep-Alive");
		urlc.setConnectTimeout(1000);
		urlc.setReadTimeout(10000);
		try {
//			urlc.connect();
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
			// pipe(in, System.out);
		} catch (IOException e) {
			log.error("[QUERY] IOException while reading response:" + " Exception=" + e);
//			log.error(start + "xxx " + end);
		} catch (Exception e) {
			log.error("[QUERY] Exception while reading response: word=" + " Exception="
					+ e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (urlc != null) {
					urlc.disconnect();
				}
			} catch (IOException x) {
			}
		}
		
		if(response == null){
			log.error("[QUERY] CANNOT GET QUERY RESPONSE!");
			return null;
		}
			
		NodeList results = response.getElementsByTagName("result").item(0).getChildNodes();
		if(results == null || results.getLength() <= 0){
			log.warn("[QUERY] QUERY RESULT IS INVALID!!");
			return null;
		}
		ArrayList<itemf> resultList = new ArrayList<itemf>(rows);
		for(int i = 0; i < results.getLength(); i ++){
			Node doc = results.item(i);
			Node docOthers = doc.getFirstChild();
			if(docOthers == null)
				continue;
			String str_json = docOthers.getTextContent();
//			 System.out.println(str_json);
			itemf tempItem = JsonUtils.fromJson(str_json, itemf.class);
			if (tempItem != null)
				resultList.add(tempItem);
		}
		return resultList;
	}


	/**
	 * 根据id单个删除solr
	 * 
	 * @param id
	 */
	public void deleteSolrById(String id) {
		if (id == null || id.isEmpty())
			return;
		try {
			solrUrl = new URL(baseUrl  + "update");
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			org.w3c.dom.Document xmlDoc = null;

			xmlDoc = builder.newDocument();
			xmlDoc.setXmlStandalone(true);
			Element root = xmlDoc.createElement("add");
			Element deleteNode = xmlDoc.createElement("delete");
			Element child = xmlDoc.createElement("id");
			Text textNode = xmlDoc.createTextNode(id);
			child.appendChild(textNode);
			deleteNode.appendChild(child);
			root.appendChild(deleteNode);
			
			
			Element commit = xmlDoc.createElement("commit");
			commit.setAttribute("softCommit", "true");
			root.appendChild(commit);
			
			xmlDoc.appendChild(root);
			
			
			String str_xml = docToString(xmlDoc);
			str_xml = replaceInvaldateCharacter(str_xml);
			Element response = postXml(str_xml, solrUrl);
			NodeList children = response.getElementsByTagName("int");
			String qtime = children.item(1).getTextContent();
			log.info("DELETE ITEM " + id + " Time taken "+ qtime);
		} catch (MalformedURLException e) {
			log.error("MalformedURLException while deleting:"
					+ " Exception=" + e, e);
		} catch (ParserConfigurationException e) {
			log.error("ParserConfigurationException while deleting:"
					+ " Exception=" + e, e);
		}
	}
	
	/**
	 * 根据id批量删除
	 * @param ids
	 */
	public void deleteSolrByIds(String[] ids) {
		if (ids == null || ids.length < 1)
			return;
		try {
			solrUrl = new URL(baseUrl  + "update");
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			org.w3c.dom.Document xmlDoc = null;

			xmlDoc = builder.newDocument();
			xmlDoc.setXmlStandalone(true);
			Element root = xmlDoc.createElement("add");
			Element deleteNode = xmlDoc.createElement("delete");
			
			for(int i = 0; i < ids.length; i++){
				String id = ids[i];
				if(id == null || id.isEmpty()){
					log.info("ID invalid!");
					continue;
				}
				Element child = xmlDoc.createElement("id");
				Text textNode = xmlDoc.createTextNode(id);
				child.appendChild(textNode);
				deleteNode.appendChild(child);
			}
			
			root.appendChild(deleteNode);
			Element commit = xmlDoc.createElement("commit");
			commit.setAttribute("softCommit", "true");
			root.appendChild(commit);
			
			xmlDoc.appendChild(root);
			
			
			String str_xml = docToString(xmlDoc);
			str_xml = replaceInvaldateCharacter(str_xml);
			Element response = postXml(str_xml, solrUrl);
			NodeList children = response.getElementsByTagName("int");
			String qtime = children.item(1).getTextContent();
			log.info("DELETE ITEMS Time taken "+ qtime);
		} catch (MalformedURLException e) {
			log.error("MalformedURLException while deleting:"
					+ " Exception=" + e, e);
		} catch (ParserConfigurationException e) {
			log.error("ParserConfigurationException while deleting:"
					+ " Exception=" + e, e);
		}
	}

	/**
	 * 删除所有文档，为安全起见，使用时再解注函数体 。
	 */
	public void deleteAll() {
		try {
			solrUrl = new URL(baseUrl  + "update");
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			org.w3c.dom.Document xmlDoc = null;

			xmlDoc = builder.newDocument();
			xmlDoc.setXmlStandalone(true);
			Element root = xmlDoc.createElement("add");
			Element deleteNode = xmlDoc.createElement("delete");
			Element child = xmlDoc.createElement("query");
			Text textNode = xmlDoc.createTextNode("*:*");
			child.appendChild(textNode);
			deleteNode.appendChild(child);
			root.appendChild(deleteNode);
			
			
			Element commit = xmlDoc.createElement("commit");
			commit.setAttribute("softCommit", "true");
			root.appendChild(commit);
			
			xmlDoc.appendChild(root);
			
			String str_xml = docToString(xmlDoc);
			System.out.println(str_xml);
			str_xml = replaceInvaldateCharacter(str_xml);
			Element response = postXml(str_xml, solrUrl);
			NodeList children = response.getElementsByTagName("int");
			String qtime = children.item(1).getTextContent();
			log.info("DELETE ALL " + qtime);
		} catch (MalformedURLException e) {
			log.error("MalformedURLException while deleting:"
					+ " Exception=" + e, e);
		} catch (ParserConfigurationException e) {
			log.error("ParserConfigurationException while deleting:"
					+ " Exception=" + e, e);
		}
	}

	public static void main(String[] args) {
//		IndexOperation ob = new IndexOperation();
//		queryInterface query = queryInterface.getInstance();
//		int start = 24007;
//		int end = 160000;
//		for (int i = start; i < end; i++) {
//			itemf item = query.queryItemF(String.valueOf(i));
//			ob.addDocToSolr(item);
////			ob.addDocToSolrByUsingClient(item);
//		}
//		 itemf item = query.queryItemF("20000");
		 //
		 // item = query.queryItemF("18652");
		 // items[1] = item;
//		 ob.addDocToSolrByUsingClient(item);
//		 System.out.println(ob.queryFromSolr("申花 高层 不屑 国安 “ 9:1 ” 海报 ： 轻视 我们 要 付出 代价").get(0).getSplitTitle());
		
//		 ob.deleteSolrById("50182");
	}

}
