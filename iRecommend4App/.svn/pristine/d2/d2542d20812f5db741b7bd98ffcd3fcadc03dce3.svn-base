/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.keyWordsAssessment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.SolrUtil.ItemSorlServerClient;
import com.ifeng.iRecommend.zhanzh.Utils.AdjStringsIsSim;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;


/**
 * <PRE>
 * 作用 : 
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 : 10-15 加入标题排重逻辑（仅限于solr中新闻数在8-20之间的）
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年8月4日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
class KeyWord{
	String name;
	String type;
	int hitDocNum;
	String date;
	boolean isAvailable;
	int state;//0为可用状态，1为分类内容少于8篇，2为长时间没有更新内容
}
public class KeyWordsAssessment {
	private static Log LOG = LogFactory.getLog("KeyWordsAssessment");
	
	public static List<KeyWord> getAllKeyWordFromRedis(){
		List<KeyWord> keywordlist = new ArrayList<KeyWord>();
		Gson gson = new Gson();
		Jedis jedis = new Jedis("10.32.24.194", 6379, 10000);
		jedis.select(0);
		Set<String> keyset = jedis.keys("*");
		LOG.info("Read keySet size : "+keyset.size());
		Iterator<String> it = keyset.iterator();
		while(it.hasNext()){
			String key = it.next();
			String json = jedis.get(key);
			if(json == null){
				LOG.error("this "+key+" value is null ~");
				continue;
			}
			try{
				KeyWord keyword = gson.fromJson(json, KeyWord.class);
				keywordlist.add(keyword);
			}catch(Exception e){
				LOG.error(key+" "+e);
				continue;
			}
		}
		return keywordlist;
	}
	
	public static void saveToRedis(List<KeyWord> keywordlist){
		if(keywordlist == null || keywordlist.isEmpty()){
			LOG.error("save redis error : keywordlist is empty");
			return;
		}
		Jedis jedis = new Jedis("10.32.24.194", 6379, 10000);
		jedis.select(1);
		Gson gson = new Gson();
		Iterator<KeyWord> it = keywordlist.iterator();
		while(it.hasNext()){
			KeyWord word = it.next();
			String savestr = gson.toJson(word);
			jedis.set(word.name, savestr);
		}
		LOG.info("save key size : "+keywordlist.size());
	}
	
	/**
	 * 根据channel关键词搜索topic1和topic2，返回所查到的item中item2app字段
	 * 
	 * 注意：取历史上近3000记录
	 * @param String channel 
	 * 
	 * @return List<String> item2app (FrontItem) Json
	 * 
	 */
	public static List<String> searchFrontItemFromSolrByKeyWord(String channel){
		List<String> itemList = new ArrayList<String>();
		if(channel == null){
			LOG.error("channel is null");
			return itemList;
		}
		channel = escapeQueryChars(channel);
		String queryStr = null;
		try {
			ItemSorlServerClient client = ItemSorlServerClient.getInstance();
			HttpSolrServer server = client.getServer();
			queryStr = "topic1:("+channel+") OR topic2:("+channel+") OR topic3:("+channel+") OR relatedfeatures:("+channel+")";
			SolrQuery query = new SolrQuery();
			query.setQuery(queryStr);
			query.setRows(1000);
			query.addSort("date",SolrQuery.ORDER.desc);
			//过滤条件	
			query.addFilterQuery("other:(ifeng OR yidian -illegal )");
			
			QueryResponse qr = server.query(query);
			SolrDocumentList doclist = qr.getResults();
			Iterator<SolrDocument> it = doclist.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				String str = (String) doc.get("item2app");
				itemList.add(str);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			LOG.error("Search Error ", e);
			System.out.println(queryStr);
			return itemList;
		} catch (Exception e1) {
			// TODO: handle exception
			LOG.error("Search Error ", e1);
			System.out.println(queryStr);
			return itemList;
		}
		return itemList;
	}
	
	/**
	 * 根据channel关键词搜索source，返回所查到的item中item2app字段
	 * 
	 * 稿源类tags单独查找该稿源分类下内容做关键词评价
	 * 
	 * 注意：取历史上近3000记录
	 * @param String channel 
	 * 
	 * @return List<String> item2app (FrontItem) Json
	 * 
	 */
	public static List<String> searchFrontItemFromSolrBySource(String channel){
		List<String> itemList = new ArrayList<String>();
		if(channel == null){
			LOG.error("channel is null");
			return itemList;
		}
		channel = escapeQueryChars(channel);
		String queryStr = null;
		try {
			ItemSorlServerClient client = ItemSorlServerClient.getInstance();
			HttpSolrServer server = client.getServer();
			queryStr = "source:("+channel+")";
			SolrQuery query = new SolrQuery();
			query.setQuery(queryStr);
			query.setRows(1000);
			query.addSort("date",SolrQuery.ORDER.desc);
			//过滤条件	
			query.addFilterQuery("other:(ifeng OR yidian -illegal )");
			
			QueryResponse qr = server.query(query);
			SolrDocumentList doclist = qr.getResults();
			Iterator<SolrDocument> it = doclist.iterator();
			while(it.hasNext()){
				SolrDocument doc = it.next();
				String str = (String) doc.get("item2app");
				itemList.add(str);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			LOG.error("Search Error ", e);
			System.out.println(queryStr);
			return itemList;
		} catch (Exception e1) {
			// TODO: handle exception
			LOG.error("Search Error ", e1);
			System.out.println(queryStr);
			return itemList;
		}
		return itemList;
	}
	
	
	/**
	 * 用于处理在solr查询中的特殊字符
	 * 
	 * 注意：
	 * @param String channel 
	 * 
	 * @return String
	 * 
	 */
	private static String escapeQueryChars(String s) {  
	    StringBuilder sb = new StringBuilder();  
	    for (int i = 0; i < s.length(); i++) {  
	      char c = s.charAt(i);  
	      // These characters are part of the query syntax and must be escaped  
	      if (c == '\\' || c == '+' || c == '-' || c == '!'  || c == '(' || c == ')' || c == ':'  
	        || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'  
	        || c == '*' || c == '?' || c == '|' || c == '&'  || c == ';' || c == '/'  
	        || Character.isWhitespace(c)) {  
	        sb.append('\\');  
	      }  
	      sb.append(c);  
	    }  
	    return sb.toString();  
	  } 
	/**
	 * 根据标题在list中查找相似item的函数
	 * 
	 * 注意：
	 * @param  FrontNewsItem item ,List<FrontNewsItem> itemlist
	 * 
	 * @return boolean
	 * 
	 */
	private static boolean isContainedSimItem(FrontNewsItem item ,List<FrontNewsItem> itemlist){
		if(item == null || itemlist == null || itemlist.isEmpty()){
			return false;
		}
		for(FrontNewsItem fitem : itemlist){
			if(AdjStringsIsSim.isSimStr(item.getTitle(), fitem.getTitle())){
//				LOG.info("Find sim item : Item 1 id "+item.getDocId()+" title "+item.getTitle()+
//						" Item 2 id "+fitem.getDocId()+" title "+fitem.getTitle());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 将在solr中查询到的文章抽象成更新时间向量
	 * 
	 * 注意：只选取近一年的数据作为统计，list最长为365，0代表今天，i代表今天第前i天
	 * @param List<String> docList 
	 * 
	 * @return List<Integer> 
	 * 
	 */
	public static List<Integer> countUpdateNewsNumByDay(List<String> docList){
		
		if(docList == null || docList.isEmpty() ){
			return null;
		}
		HashMap<Integer, Integer> dayUpdateNumMap = new HashMap<Integer, Integer>();
		int MaxDay = 0;
		Gson gson = new Gson();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<FrontNewsItem> tempList = new ArrayList<FrontNewsItem>();//用于标题排重
		for(String doc : docList){
			try {
				FrontNewsItem item = gson.fromJson(doc, FrontNewsItem.class);
				
				
				//标题排重
				//test为了效率考虑，loading出来的size大于20就不做标题排重了 临时做法
				if(docList.size()<20 && isContainedSimItem(item, tempList)){
					continue;
				}
				tempList.add(item);
				
				
				
				Date date = format.parse(item.getDate());
				Long time = (System.currentTimeMillis()-date.getTime())/(24*60*60*1000);
				int day = time.intValue();
				//只取近一年的数据做统计
				if(day>365){
					break;
				}
				if(day>MaxDay){
					MaxDay = day;
				}
				Integer tempday = dayUpdateNumMap.get(day);
				if(tempday == null){
					dayUpdateNumMap.put(day, 1);
				}else{
					tempday++;
					dayUpdateNumMap.put(day, tempday);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error("parser date error : ",e);
				continue;
			} catch (Exception e){
				LOG.error("ERROR at setting UpdateNumList : ",e);
				continue;
			}
		}
		
		//标题排重后数量不足8的差评
		if(tempList.size()<8){
			return null;
		}
		
		if(dayUpdateNumMap.isEmpty()){
			return null;
		}
		//组装成一维向量
		ArrayList<Integer> daysUpdateNumlist = new ArrayList<Integer>();
		for(int i=0;i<=MaxDay;i++){
			Integer temp = dayUpdateNumMap.get(i);
			if(temp != null){
				daysUpdateNumlist.add(temp);
			}else{
				daysUpdateNumlist.add(0);
			}
		}
		return daysUpdateNumlist;
	}
	
	/**
	 * tag评价的入口函数
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	public static void asssessKeyWord(){
//		List<KeyWord> keyWordsList = KeyWordsAssessment.loadingKeyWords("D://keywordset.txt");
		List<KeyWord> keyWordsList = null;
		try{
			keyWordsList = KeyWordsAssessment.getAllKeyWordFromRedis();
		} catch (Exception e){
			LOG.error(" ", e);
		}
		
//		List<KeyWord> badlist = new ArrayList<KeyWord>();
//		List<KeyWord> availablelist = new ArrayList<KeyWord>();
//		List<KeyWord> tempList = new ArrayList<KeyWord>();
//		List<KeyWord> noupdatelist = new ArrayList<KeyWord>();
		if(keyWordsList == null || keyWordsList.isEmpty()){
			LOG.error("Loading keywords file error ~");
			return;
		}
		for(KeyWord word : keyWordsList){
			String name = word.name;
			List<String> doclist = null;
			if(word.type.equals("swm")){
				doclist = KeyWordsAssessment.searchFrontItemFromSolrBySource(name);
			}else{
				doclist = KeyWordsAssessment.searchFrontItemFromSolrByKeyWord(name);
			}

			word.hitDocNum = doclist.size();
			//文章数小于8直接差评
			if(doclist.size()<8){
				word.state = 1;
//				badlist.add(word);
				LOG.info(word.name+" docNum less than 8 ,add to badlist");
				continue;
			}
			List<Integer> updateNumList = KeyWordsAssessment.countUpdateNewsNumByDay(doclist);
			if(updateNumList == null || updateNumList.isEmpty()){
//				badlist.add(word);
				word.state = 1;
				LOG.info(word.name+" docNum less than 8 ,add to badlist");
				continue;
			}
			//近2天有更新的直接好评
			int d=0;
			while(d<2&&d<updateNumList.size()){
				if(updateNumList.get(d)>0){
					word.isAvailable = true;
					LOG.info(word.name+" doc update today ,add to availablelist");
//					availablelist.add(word);
					word.state = 0;
					break;
				}
				d++;
			}
			if(word.isAvailable){
				continue;
			}
			
			//更新频率均值判断,如果最近更新周期超过平均更新时长则判断为抓取出问题，暂时差评
			
			double result = KeyWordsAssessment.countAverageUpdateTime(updateNumList);
			int daycount = 0;
			while(daycount<result*5){
				if(updateNumList.get(daycount)>0){
					word.isAvailable = true;
					LOG.info(word.name+" less than average updatetime ,add to availablelist");
//					availablelist.add(word);
//					tempList.add(word);
					word.state = 0;
					break;
				}
				daycount++;
			}
			if(word.isAvailable){
				continue;
			}
//			noupdatelist.add(word);
			word.state = 2;
			//若非以上两种情况进入模型判断
		}
		saveToRedis(keyWordsList);
		
//		Gson gson = new Gson();
//		System.out.println("available list size : "+availablelist.size());
//		System.out.println(gson.toJson(keyWordsList));
//		System.out.println("bad list size : "+badlist.size());
//		System.out.println(gson.toJson(badlist));
//		System.out.println("temp list size : "+tempList.size());
//		System.out.println(gson.toJson(tempList));
//		System.out.println("temp list size : "+noupdatelist.size());
//		System.out.println(gson.toJson(noupdatelist));
	}
	
	/**
	 * 根据步长计算向量
	 * 
	 * 例如：计算7天更新数，设定step为7，
	 * @param List<Integer> updateDocNumList,int step
	 * 
	 * @return List<Integer>
	 * 
	 */
	public static List<Integer> countUpdateDocNumByStep(List<Integer> updateDocNumList,int step){
		List<Integer> countlist = new ArrayList<Integer>();
		if(updateDocNumList == null || updateDocNumList.isEmpty()){
			return countlist;
		}
		int j=0;
		while(j<updateDocNumList.size()){
			int sum = 0;
			for(int i=0;j<updateDocNumList.size()&&i<step;i++,j++){
				 sum += updateDocNumList.get(j);
			}
			countlist.add(sum);
		}
		return countlist;
	}
	
	/**
	 * 计算tag平均更新周期
	 * 
	 * 
	 * @param List<Integer> updateDocNumList
	 * 
	 * @return double
	 * 
	 */
	public static double countAverageUpdateTime(List<Integer> updateDocNumList){
		if(updateDocNumList == null || updateDocNumList.isEmpty()){
			return 0;
		}
		int index = 0;
		int count = 1;
		int sum = 0;
		boolean isFirst = true;
		for(Integer i : updateDocNumList){
			
			if(i!=0&&!isFirst){
				count++;
				sum+=index;
				index=0;
			}
			if(i!=0&&isFirst){
				index=0;
				isFirst = false;
			}
			index++;
		}
		if(count == 0){
			return 0;
		}
//		System.out.println(sum);
//		System.out.println(count);
		double  result = (double) sum/count;
		return result;
	}
	
	/**
	 * 从本地文件中loading所有关键词
	 * 
	 * 
	 * @param String path
	 * 
	 * @return List<KeyWord>
	 * 
	 */
	public static List<KeyWord> loadingKeyWords(String path){
		if(path == null){
			return null;
		}
		ArrayList<KeyWord> keywordsList = new ArrayList<KeyWord>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = null;
			while((line = br.readLine())!=null){
				String[] templist = line.split("\\t");
				if(templist.length != 4){
					continue;
				}
				KeyWord word = new KeyWord();
				word.name = templist[0];
				word.type = templist[1];
//				word.hitDocNum = Integer.valueOf(templist[2]);
//				word.date = templist[3];
				keywordsList.add(word);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return keywordsList;
	}
	
	public static void test(){
		Jedis jedis = new Jedis("10.32.24.194", 6379, 10000);
		jedis.select(1);
		String url = "http://10.50.4.68:7000/Apiuserprofile";
		String str = DownloadPageUtil.downloadPage(url, "UTF-8");
		Gson gson = new Gson();
		UserSubTagsItem usb = gson.fromJson(str, UserSubTagsItem.class);
		Map<String, Integer> data = usb.getMsg().getData();
		Set<String> keySet = data.keySet();
//		Set<String> keySet = jedis.keys("*");
		for(String key : keySet){
			String test = jedis.get(key);
			if(test != null && test.indexOf("hitDocNum\":0")>0){
				System.out.println(key);
				System.out.println(test);
			}
			
		}

		
	}
	
	public static void main(String[] args){
//		test();
		Jedis jedis = new Jedis("10.32.24.194", 6379, 10000);
		jedis.select(1);
//		String[] testlist = {"|宁夏回族自治区"
//
//
//			    };
//		for(String str : testlist){
//			String test = jedis.get(str);
//			if(test != null){
//				jedis.del(str);
//			}
//			System.out.println(test);
//		}
		Set<String> testSet1 = jedis.keys("莆田*");	
		System.out.println(testSet1);
		

		
		
		
		
//		Set<String> testSet = jedis.keys("*");
//		jedis.setex("test", 30, "test");
//		Set<String> testSet0 = jedis.keys("*");
//		
//System.out.println("total : "+testSet0.size());
//jedis.select(1);
//Set<String> testSet1 = jedis.keys("*");
//		System.out.println("total : "+testSet0.size());
//		
//		for(String key : testSet0){
//			if(!testSet1.contains(key)){
//				System.out.println(key);
////				jedis.del(key);
//			}
//		}
		
//		HashMap<String, List<String>> tagMap = new HashMap<String, List<String>>();
////		
//		Gson gson = new Gson();
//		int count = 0;
////		int count1 = 0;
//		for (String key : testSet) {
//			String value = jedis.get(key);
//			KeyWord word = gson.fromJson(value, KeyWord.class);
//			if (word.isAvailable) {
//				count++;
//				String name = word.name;
//				if(name.matches(".*?[a-zA-Z]+.*?")){
//					String upperName = name.toUpperCase();
//					List<String> tempList = tagMap.get(upperName);
//					if(tempList != null){
//						tempList.add(name);
//					}else{
//						tempList = new ArrayList<String>();
//						tempList.add(name);
//						tagMap.put(upperName, tempList);
//					}
//				}
////				System.out.println(key);
//			}
//			if (word.state == 2) {
//				count++;
//				System.out.println(key);
//			}
//			if(word.type.equals("et")||word.type.equals("kb")||word.type.equals("ks")||word.type.equals("kq")||word.type.equals("kr")){
//				count++;
//				
//			}
//			if(word.type.equals("swm")&&word.isAvailable){
//				count1++;
//				System.out.println(key);
//			}
//			
//			if(word.type.equals("loc")){
//			count++;
//			System.out.println(key+" "+word.isAvailable);
//			}
//		}
//		String out = gson.toJson(tagMap);
//		System.out.println(out);
//			
//			
//		}
//		System.out.println(count);
//		System.out.println(count1);
//		
//		asssessKeyWord();
		
//		while(true){
//			asssessKeyWord();
//			LOG.info("sleep 10 min");
//			try {
//				Thread.sleep(10*60*1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

	}

}
