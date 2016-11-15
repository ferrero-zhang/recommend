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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.SolrUtil.ItemSorlServerClient;
import com.ifeng.iRecommend.zhanzh.Utils.AdjStringsIsSim;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.KeyWord;




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
//class KeyWord{
//	String name;
//	String type;
//	int hitDocNum;
//	String date;
//	boolean isAvailable;
//	int state;//0为可用状态，1为分类内容少于8篇，2为长时间没有更新内容，3为用户订阅确更新不足（但默认可用）,4为用户订阅但文章数为0（默认不可用）
//}
public class KeyWordAssessmentMT implements Runnable {
	private static Log LOG = LogFactory.getLog("KeyWordAssessmentMT");
	private static List<KeyWord> keyWordsList = new ArrayList<KeyWord>();
	private static Set<String> userSubWords = new HashSet<String>();
	private static PropertiesConfiguration configs;
	static 
	{
		configs = new PropertiesConfiguration();
		configs.setEncoding("UTF-8");
		try {
			configs.load("conf/KeyWordAssessment.properties");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			LOG.error("Load config error  ",e);
		}
		configs.setReloadingStrategy(new FileChangedReloadingStrategy());
	}
	
	/**
	 * 加载用户订阅词表
	 * 
	 * 
	 * @param String path
	 * 
	 * @return List<KeyWord>
	 * 
	 */
	private static void loadingUserSubWords(){
		String url = "http://10.50.4.68:7000/Apiuserprofile";
		String jsonStr = DownloadPageUtil.downloadPageByRetry(url, "UTF-8", 3);
		if(jsonStr != null){
			try{
				Gson gson = new Gson();
				UserSubTagsItem usb = gson.fromJson(jsonStr, UserSubTagsItem.class);
				Map<String, Integer> tempMap = usb.getMsg().getData();
				Set<String> keySet = tempMap.keySet();
				if(keySet != null){
					userSubWords.clear();
					userSubWords.addAll(keySet);
				}
			} catch (Exception e){
				LOG.error(" ",e);
				return;
			}
			LOG.info("Loading UserSubWords success , size : "+userSubWords.size());
		}
		
	}
	
	private synchronized static List<KeyWord> getKeywordsList(){
		int Length = 1000;
		if(keyWordsList == null || keyWordsList.isEmpty()){
			LOG.warn(Thread.currentThread().getName()+" keyWordList is Empty ~");
			return null;
		}
		List<KeyWord> returnlist = new ArrayList<KeyWord>();
		if(keyWordsList.size() < Length){
			returnlist.addAll(keyWordsList.subList(0, keyWordsList.size()));
		} else {
			returnlist.addAll(keyWordsList.subList(0, Length));
		}
		//将返回的keyWord从keywordlist中删除
		keyWordsList.removeAll(returnlist);
		LOG.info(Thread.currentThread().getName()+" get keywordlist success ~ remain : "+keyWordsList.size());
		return returnlist;
	}
	
	/**
	 * 从redis读出所有key
	 * 
	 * 
	 * @param String path
	 * 
	 * @return List<KeyWord>
	 * 
	 */
	public static void setKeyWordsListFromRedis(){
		List<KeyWord> keywordlist = new ArrayList<KeyWord>();
		Gson gson = new Gson();
		Jedis jedis = new Jedis("10.32.24.194", 6379, 10000);
		jedis.select(0);
		Set<String> keyset = jedis.keys("*");
		LOG.info("Read keySet size : "+keyset.size());
		Iterator<String> it = keyset.iterator();
		while(it.hasNext()){
			//test
//			if(keywordlist.size() > 1000){
//				break;
//			}
			
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
		keyWordsList = keywordlist;
	}
	
	public static synchronized void saveToRedis(List<KeyWord> keywordlist){
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
			jedis.set(word.getName(), savestr);
		}
		LOG.info("save key size : "+keywordlist.size());
		jedis.close();
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
			String solrUrl = configs.getString("solrUrl");
			
			HttpSolrServer server = client.getServer(solrUrl);
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
			LOG.error("Search "+channel+" Error ", e);
//			System.out.println(queryStr);
			return itemList;
		} catch (Exception e1) {
			// TODO: handle exception
			LOG.error("Search "+channel+" Error ", e1);
//			System.out.println(queryStr);
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
			String solrUrl = configs.getString("solrUrl");
			HttpSolrServer server = client.getServer(solrUrl);
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
//			System.out.println(queryStr);
			return itemList;
		} catch (Exception e1) {
			// TODO: handle exception
			LOG.error("Search Error ", e1);
//			System.out.println(queryStr);
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
	public void asssessKeyWordMT(List<KeyWord> keyWordList){
//		List<KeyWord> keyWordsList = KeyWordsAssessment.loadingKeyWords("D://keywordset.txt");
		
//		List<KeyWord> badlist = new ArrayList<KeyWord>();
//		List<KeyWord> availablelist = new ArrayList<KeyWord>();
//		List<KeyWord> tempList = new ArrayList<KeyWord>();
//		List<KeyWord> noupdatelist = new ArrayList<KeyWord>();
		
		//禁用词默认直接变为false
		List<String> forbiddenlist = configs.getList("forbiddenWord");
		Set<String> forbiddenSet = new HashSet<String>();
		forbiddenSet.addAll(forbiddenlist);
		
		if(keyWordList == null || keyWordList.isEmpty()){
			LOG.error("keyword List is null ~");
			return;
		}
		for(KeyWord word : keyWordList){
			String name = word.getName();
			
			//禁用词默认直接变为false
			if(forbiddenSet.contains(name)){
				LOG.info(name + "hit forbiddenWord just set false ~");
				word.setAvailable(false);
				continue;
			}
//			//将e 全部置为false 临时上线
//			if(word.getType().equals("e")){
//				LOG.info(name + " "+ word.getType() + "type is e just set false ~");
//				word.setAvailable(false);
//				continue;
//			}
			
			List<String> doclist = null;
			if(word.getType().equals("swm")){
				doclist = KeyWordAssessmentMT.searchFrontItemFromSolrBySource(name);
			}else{
				doclist = KeyWordAssessmentMT.searchFrontItemFromSolrByKeyWord(name);
			}

			word.setHitDocNum(doclist.size());
			
			//对于用户订阅的词只要文章数大于0即可用state=3，若文章数等于0不可用state=4
/*********************/
			if(userSubWords.contains(word.getName())){

				if(doclist.size() == 0 ){
					word.setState(4); 
					continue;
				}
				if(doclist.size()<8 ){
					word.setState(3); 
					word.setAvailable(true);
					continue;
				}
				List<Integer> updateNumList = KeyWordAssessmentMT.countUpdateNewsNumByDay(doclist);
				if(updateNumList == null || updateNumList.isEmpty()){
					word.setState(3); 
					word.setAvailable(true);
					continue;
				}
				//近2天有更新的直接好评
				int d=0;
				while(d<2&&d<updateNumList.size()){
					if(updateNumList.get(d)>0){
						word.setAvailable(true); 
						LOG.info(word.getName()+" doc update today ,add to availablelist");
//						availablelist.add(word);
						word.setState(0);
						break;
					}
					d++;
				}
				if(word.isAvailable()){
					continue;
				}
				//更新频率均值判断,如果最近更新周期超过平均更新时长则判断为抓取出问题，暂时差评
				double result = KeyWordAssessmentMT.countAverageUpdateTime(updateNumList);
				int daycount = 0;
				while(daycount<result*5){
					if(updateNumList.get(daycount)>0){
						word.setAvailable(true);
						LOG.info(word.getName()+" less than average updatetime ,add to availablelist");
//						availablelist.add(word);
//						tempList.add(word);
						word.setState(0);
						break;
					}
					daycount++;
				}
				if(word.isAvailable()){
					continue;
				}
				word.setState(3); 
				word.setAvailable(true);
				continue;
			}
/*********************/
			
			
			//文章数小于8直接差评
			if(doclist.size()<8){
				word.setState(1); 
//				badlist.add(word);
				LOG.info(word.getName()+" docNum less than 8 ,add to badlist");
				continue;
			}
			List<Integer> updateNumList = KeyWordAssessmentMT.countUpdateNewsNumByDay(doclist);
			if(updateNumList == null || updateNumList.isEmpty()){
//				badlist.add(word);
				word.setState(1); 
				LOG.info(word.getName()+" docNum less than 8 ,add to badlist");
				continue;
			}
			//近2天有更新的直接好评
			int d=0;
			while(d<2&&d<updateNumList.size()){
				if(updateNumList.get(d)>0){
					word.setAvailable(true); 
					LOG.info(word.getName()+" doc update today ,add to availablelist");
//					availablelist.add(word);
					word.setState(0);
					break;
				}
				d++;
			}
			if(word.isAvailable()){
				continue;
			}
			
			//更新频率均值判断,如果最近更新周期超过平均更新时长则判断为抓取出问题，暂时差评
			
			double result = KeyWordAssessmentMT.countAverageUpdateTime(updateNumList);
			int daycount = 0;
			while(daycount<result*5){
				if(updateNumList.get(daycount)>0){
					word.setAvailable(true);
					LOG.info(word.getName()+" less than average updatetime ,add to availablelist");
//					availablelist.add(word);
//					tempList.add(word);
					word.setState(0);
					break;
				}
				daycount++;
			}
			if(word.isAvailable()){
				continue;
			}
//			noupdatelist.add(word);
			word.setState(2);
			//若非以上两种情况进入模型判断
		}
		saveToRedis(keyWordList);
		keywordsDelete(keyWordList);
//		Gson gson = new Gson();
//		System.out.println("available list size : "+availablelist.size());
//		System.out.println(gson.toJson(keyWordList));
//		System.out.println("bad list size : "+badlist.size());
//		System.out.println(gson.toJson(badlist));
//		System.out.println("temp list size : "+tempList.size());
//		System.out.println(gson.toJson(tempList));
//		System.out.println("temp list size : "+noupdatelist.size());
//		System.out.println(gson.toJson(noupdatelist));
	}
	
	/**
	 * keyword删除函数 遍历列表将不符合要求keyword直接从redis删除
	 * 
	 * 删除规则（同时满足）：
	 * 1、最后命中时间为2月以前
	 * 2、命中文章数少于3篇
	 * 3、不可用
	 * 
	 * 
	 * @param List<KeyWord> keywordlist
	 * 
	 * @return void
	 * 
	 */
	private void keywordsDelete(List<KeyWord> keywordlist){
		List<KeyWord> delList = new ArrayList<KeyWord>();
		if(null == keywordlist || keywordlist.isEmpty()){
			return;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long maxTime = 2*30*24*60*60*1000; //两个月时间 
		for(KeyWord keyword : keywordlist){
			String datestr = keyword.getDate();
			try {
				//不可用
				if(keyword.isAvailable()){
					continue;
				}
				//两个月以前
				long time = System.currentTimeMillis() - format.parse(datestr).getTime();
				if(time < maxTime){
					continue;
				}
				//命中文章数小于3
				if(keyword.getHitDocNum() > 3){
					continue;
				}
				delList.add(keyword);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error("parser time error ", e);
				continue;
			}
		}
		LOG.info("delList size "+delList.size());
		delFromRedis(delList);
	}
	
	private synchronized void delFromRedis(List<KeyWord> delList){
		
		if(null == delList ||delList.isEmpty()){
			return;
		}
		Jedis jedis = new Jedis("10.32.24.194", 6379, 1000);
		for(KeyWord word : delList){
			String deltableName = word.getName();
			try{
				jedis.select(0);
				String cache = jedis.get(deltableName);
				if(cache != null){
					jedis.del(deltableName);
					LOG.info("Del "+deltableName+" from redis");
				} else {
					LOG.info("Already del from redis : "+deltableName);
				}
				jedis.select(1);
				String cache1 = jedis.get(deltableName);
				if(cache1 != null){
					jedis.del(deltableName);
					LOG.info("Del "+deltableName+" from redis");
				} else {
					LOG.info("Already del from redis : "+deltableName);
				}
			}catch(Exception e){
				LOG.error("ERROR"+e);
				continue;
			}
		}
		jedis.close();
		
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
				word.setName(templist[0]);
				word.setType(templist[1]);
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
	public static void main(String[] args){

		ExecutorService pool = Executors.newFixedThreadPool(20);
		while(true){
			//加载用户订阅词表
			loadingUserSubWords();
			//加载所有待评价关键词
			setKeyWordsListFromRedis();
			KeyWordAssessmentMT mt = new KeyWordAssessmentMT();
			List<Future> futureList = new ArrayList<Future>();
			for(int i=0;i<20;i++){
				Future f =pool.submit(mt);
				futureList.add(f);
			}
			
			for(Future f : futureList){
				try {
					f.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				LOG.info("Sleep 20 min ~");
				Thread.sleep(20*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			List<KeyWord> keywordlist = getKeywordsList();
			while(true){
				if(keywordlist == null || keywordlist.isEmpty()){
					break;
				}
				asssessKeyWordMT(keywordlist);
				keywordlist = getKeywordsList();
			}
		} catch (Exception e){
			LOG.error(" ", e);
		}
		
	}

}
