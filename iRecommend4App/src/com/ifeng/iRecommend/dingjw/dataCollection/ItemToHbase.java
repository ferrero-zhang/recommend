package com.ifeng.iRecommend.dingjw.dataCollection;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.io.File;
import java.util.Map.Entry;
import java.util.Calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Put;

	import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.reidx.SplitWordClient;
	//import com.ifeng.commen.reidx.SplidWordStub;
	//import com.ifeng.commen.reidx.SplidWordStub.SplitResponse;
	import com.ifeng.commen.lidm.hbase.HbaseInterface;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.UrlPretreatment;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.featureEngineering.FeatureExTools;
import com.ifeng.iRecommend.fieldDicts.*;

	/**
	 * <PRE>
	 * 作用 : 
	 *   采集并解析icms的xml文件，写入hbase
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
	 *          1.0          2013-7-23        dingjw          create
	 * -----------------------------------------------------------------------------
	 * </PRE>
	 */
public class ItemToHbase {

	private static ItemToHbase instance = new ItemToHbase();
		
		private static final Log LOG = LogFactory.getLog("clientItemToHbase");
		
		private HashSet<String> xmlSet = new HashSet<String>();//待处理文件路径集合
		private static ItemOperation itemOP = ItemOperation.getInstance();
		private static int imgCount = 0;
		private static HashMap<String,String> otherChannelMap = new readOtherChannelMapping().getOtherChannelMap();
		
		
		public static ItemToHbase getInstance(){
			return instance;
		}
		
		private static class dirTraver extends Thread{
			private CountDownLatch latch;
			private String threadName;
			private List<String> dirpaths;
			private boolean delete;
			
			public dirTraver(String threadName,List<String> dirpaths, CountDownLatch l, boolean delete){
				this.threadName=threadName;
				this.dirpaths=dirpaths;
				this.latch=l;
				this.delete = delete;
			}
			
			public void run(){
				try{
					//遍历目录，处理xml文件
					for(String dirpath: this.dirpaths){
						try{
							instance.processXML(dirpath, delete);
						}catch (Exception e) {
							LOG.error("Fail to deal with folder: "+dirpath);
						}
					}
					//输出信息
					LOG.info("Thread-"+this.threadName+": finish!");
				}catch (Exception e) {
					LOG.error("[OVERALL ERROR] ", e);
				}finally{
					this.latch.countDown();
				}
			}
		}
		/**
		 * 获取下一个小时
		 * 注意：时间为24小时制
		 * 
		 * @param currentHour 当前的时间 如2013/0723/16
		 * @return nextHour 下一个小时 如2013/0723/17
		 */
		public static String getNextHour(String currentHour) {
			Calendar c = Calendar.getInstance();
			Date date = null;
			try {
				date = new SimpleDateFormat("yyyy/MMdd/HH").parse(currentHour);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			c.setTime(date);
			int hour = c.get(Calendar.HOUR_OF_DAY);
			c.set(Calendar.HOUR_OF_DAY, hour + 1);

			String nextHour = new SimpleDateFormat("yyyy/MMdd/HH").format(c.getTime());
			return nextHour;
		}	
		/**
		 * 把一个xml文件写入ikv，-1失败，1成功
		 * @param itemInfoMap
		 * @return
		 */
		public static Integer writeItemToIKV(HashMap<String, String> itemInfoMap)
		{
			if (itemInfoMap == null || itemInfoMap.isEmpty()) {
				return -1;
			}
			String id = itemInfoMap.get("documentid");
			if(id.isEmpty()||id==null)
				return -1;
			itemOP.setItemType(ItemType.APPITEM);
			Item oneItem = new Item();
			try{
				oneItem.setUrl(itemInfoMap.get("url"));
				oneItem.setKeywords(itemInfoMap.get("keywords"));
				oneItem.setID(itemInfoMap.get("documentid"));
				oneItem.setAuthor(itemInfoMap.get("source"));
				oneItem.setContent(itemInfoMap.get("content"));
				oneItem.setDate(itemInfoMap.get("date"));
				oneItem.setTitle(itemInfoMap.get("title"));
				oneItem.setChannel(itemInfoMap.get("channel"));
				oneItem.setOther(itemInfoMap.get("other"));
			}
			catch(Exception e)
			{
				LOG.warn("[Warn writeItem] some of the "+id+" is missing.", e);
			}
			
			if(itemInfoMap.get("url") == null || itemInfoMap.get("url").trim().equals(""))
			{
				Item oldItem = itemOP.getItem(itemInfoMap.get("documentid"));
				if(oldItem == null || oldItem.getUrl().equals("http://news.ifeng.com/"+itemInfoMap.get("documentid")+".html"))
				{
					oneItem.setUrl("http://news.ifeng.com/"+itemInfoMap.get("documentid")+".html");
				}
				else
				{
					oneItem.setUrl(oldItem.getUrl());
				}
			}
			else
			{}
			Item oldItem = itemOP.getItem(itemInfoMap.get("documentid"));
			if(oldItem != null && oldItem.getOther() != null)
			{
				Pattern pattern = Pattern.compile("imgNum=(\\d+)");
				Matcher oldMatcher = pattern.matcher(oldItem.getOther());
				Matcher oneMatcher = pattern.matcher(oneItem.getOther());
				if(oldMatcher.find() && oneMatcher.find())
				{
					if(Integer.valueOf(oldMatcher.group(1)) > Integer.valueOf(oneMatcher.group(1)))
					{
						oneItem.setOther(oldItem.getOther());
					}
					else
					{}
				}
				else
				{
					
				}
			}
			
			
			try{
				itemOP.set(oneItem.getID(), oneItem);
			}
			catch(Exception e)
			{
				LOG.warn("[Warn writeItem] write the "+id+" item failed.", e);
				return -1;
			}
			return 1;
		}

		/**                          
		 * 把一个xml的信息写进hbase
		 * 注意：在hbase中rowkey是url，column family是 INFO
		 * 
		 * @param tablename
		 * @param itemInfoMap
		 * @return 1为成功，-1为失败
		 */
		public static Integer writeItem(String tablename, HashMap<String, String> itemInfoMap)
		{
			if (tablename == null || tablename.isEmpty() || itemInfoMap == null || itemInfoMap.isEmpty()) {
				return -1;
			}
			itemOP.setItemType(ItemType.APPITEM);
			//String url = itemInfoMap.get("url");
			String id = itemInfoMap.get("documentid");
			if(id.isEmpty()||id==null)
				return -1;
			LinkedList<Put> putsList=new LinkedList<Put>();
			
			if(itemInfoMap.get("url") == null || itemInfoMap.get("url").trim().equals(""))
			{
				
				Item oldItem = itemOP.getItemFromHbase(itemInfoMap.get("documentid"));
				if(oldItem == null || oldItem.getUrl().equals("http://news.ifeng.com/"+itemInfoMap.get("documentid")+".html"))
				{
					itemInfoMap.put("url","http://news.ifeng.com/"+itemInfoMap.get("documentid")+".html");
				}
				else
				{
					itemInfoMap.put("url",oldItem.getUrl());
				}
			}
			else
			{}
			Item oldItem = itemOP.getItemFromHbase(itemInfoMap.get("documentid"));
			if(oldItem != null && oldItem.getOther() != null)
			{
				Pattern pattern = Pattern.compile("imgNum=(\\d+)");
				Matcher oldMatcher = pattern.matcher(oldItem.getOther());
				Matcher oneMatcher = pattern.matcher(itemInfoMap.get("other"));
				if(oldMatcher.find() && oneMatcher.find())
				{
					if(Integer.valueOf(oldMatcher.group(1)) > Integer.valueOf(oneMatcher.group(1)))
					{
						itemInfoMap.put("other",oldItem.getOther());
					}
					else
					{}
				}
				else
				{
				}
			}
			
			
			for (Entry<String, String> entry : itemInfoMap.entrySet()) {
				if (entry.getValue() == null) 
					continue;
				try {
					Put put = new Put(id.getBytes());
					put.setWriteToWAL(false);
					put.add("INFO".getBytes(), entry.getKey().getBytes(), entry.getValue().getBytes());
					putsList.add(put);	
				} catch (Exception e) {
					LOG.warn("[Warn writeItem] ", e);
					continue;
				}		
			}
			
			
			
			try {
				HbaseInterface.insertRowList(tablename, putsList);
			}catch (Exception e) {
				LOG.error("[ERROR]	Can Not Insert This Item: " + id);
				LOG.error("[Item id]: " + id);
				LOG.error("[ERROR] ", e);
				return -1;
			}	
			return 1;
		}
		/**
		 * 读取xml文件操作
		 * @param filename
		 * @return StringBuffer sb
		 */
		public StringBuffer xmlOpen(String filename){
			File file = new File(filename);                       								//传入文件
			//文件不存在返回null
			if(filename==null||!file.exists())
				return null;
			
			FileUtil xmlFile = new FileUtil();													//xml文件读取器
			xmlFile.Initialize(filename, "UTF-8");
			String line = null;
			StringBuffer sb = new StringBuffer();												//缓存器
			while ((line = xmlFile.ReadLine()) != null) {
				sb.append(line);
			}
				return sb;
		} 
		
		/**
		 * 过滤非法字符
		 * @param str
		 * @return
		 */
		public String filterString(String str)
		{
			//String str = "aBc我!e觉得自*己#就!@#$%^&*()()[]{}是一个大傻逼";
			Pattern pattern = Pattern.compile("([^0-9a-zA-Z_!*@#$%^&(),.:?;【】/=+|\'\"\\{\\}\\[\\]，。：？；“”……！——《》\\u4e00-\\u9fa5]+)");
			Matcher matcher = pattern.matcher(str);
			try{
			if (matcher.find())
			{
				str = str.replaceAll(matcher.group(1), "");
				str = filterString(str);
			}
			else
			{
				return str;
			}
			return str;
			}
			catch(Exception e)
			{
				LOG.error("[ERROR] filterString error!");
				return null;
			}
			
		}
		/**
		 * 过滤html标签
		 * @param s
		 * @return
		 */
		public String filterHtml(String s)
		{
			String str = s.replaceAll("<[.[^<]]*>", "##");
			str = str.replaceAll("#+"," ");
			return str;	
		}
		/**
		 * 抽取标题，内容和关键词并进行分词处理
		 * @param sb
		 * @return String类型的 list  其中textList.get(0)为title  textList.get(1)为contents  textList.get(2)为keyWords 
		 */
		public List<String> pretreatmentOfXml(StringBuffer sb)
		{
			if(sb == null || sb.equals(""))
				return null;
			List<String> textList = new ArrayList<String>();//其中textList.get(0)为title  textList.get(1)为contents  textList.get(2)为keyWords 
			imgCount = 0;
			Pattern pattern = null;
			Matcher matcher = null;
			StringBuffer splitBuffer = new StringBuffer();
			String textSrc = "";
			pattern = Pattern.compile("<title>(.+)</title>");
			matcher = pattern.matcher(sb.toString());
			if (matcher.find()) {
				
				textSrc = matcher.group(1);
				textSrc = filterString(textSrc);
				if(textSrc==null)
					textSrc = "";
			}
			splitBuffer.append(textSrc);//合并
			
			String separater = FeatureExTools.splitTag;
			try{
			pattern = Pattern.compile("<message>[\n\t]*<\\!\\[CDATA\\[(.+)\\]\\]>[\n\t]*</message>");
			matcher = pattern.matcher(sb.toString());
			if (matcher.find()) {
				textSrc = matcher.group(1);
				textSrc = textSrc.toLowerCase();
				//判断正文中是否有图，并记录图片数量（仅适用于非高清图内容）
				if(textSrc.contains("<img"))
				{
					String[] imgSplit = textSrc.split("<img");
					imgCount = imgSplit.length - 1;
				}
				else
				{
					//内容中没有图
				}
				textSrc = filterHtml(textSrc);
				textSrc = filterString(textSrc);
				if(textSrc==null||textSrc.isEmpty())
					textSrc = "";
				
			}
			splitBuffer.append(separater).append(textSrc);//合并
			} catch(Exception e){LOG.error("[ERROR] ", e);}			
			
			String keywordFiltered = "";
			HashMap<String, String> keywordMap = new HashMap<String, String>();
			try{
			pattern = Pattern.compile("<keywords>(.+)</keywords>");
			matcher = pattern.matcher(sb.toString());
			if (matcher.find()) {
				textSrc = matcher.group(1);
				if(textSrc!=null)
	            {
	            	String keywordsSplited[] = textSrc.split("[,\\s，]");
	            	StringBuffer tmp_sb = new StringBuffer();
	            	for(String word:keywordsSplited)
	            	{
	            		if(word.equals("凤凰")||word.equals("凤凰新媒体"))
	            			continue;
	            		else{
	            			tmp_sb.append(word).append(" ");
	            			keywordMap.put(word, " ");
	            		}
	            	}
	            	keywordFiltered = tmp_sb.toString();
	            }
			}
			splitBuffer.append(separater).append(keywordFiltered);//合并
			} catch(Exception e){}
			
			//对标题、内容、关键词进行合并和统一分词
			//用SplidWordClient进行分词
			String textSplited = new String(SplitWordClient.split(splitBuffer.toString().replace("✿", "").replace("•", ""), null).replace("(/", "_").replace(") ", " "));
			String textSplits[] = textSplited.split(FeatureExTools.splitedTag);
			
			if(textSplits.length>0 && textSplits[0]!=null)
				textList.add(textSplits[0]);
		
			if(textSplits.length>1 && textSplits[1]!=null)
				textList.add(textSplits[1]);
			
			if(textSplits.length>2 && !textSplits[2].isEmpty())
			{
				String keywordSplited = new String(textSplits[2]);
				for(String str:keywordSplited.split(" "))
				{
					if(!str.isEmpty()){
						String strSplits[] = str.split("_");
						if(!strSplits[0].isEmpty()&&strSplits.length>1)
							keywordMap.put(strSplits[0], strSplits[1]);
					}
				}
				StringBuffer keywordBuffer = new StringBuffer();
				for(Entry<String, String> entry : keywordMap.entrySet())
					if(entry.getValue().equals(" "))
						keywordBuffer.append(entry.getKey()).append(" ");
					else
						keywordBuffer.append(entry.getKey()).append("_").append(entry.getValue()).append(" ");	
				textList.add(keywordBuffer.toString());
			}
			return textList;
		}
		
	/**
	 * 根据从xml文件获取的内容 解析资讯、博客、专题的xml文件
	 * @param textList  String类型的 list  其中textOfList.get(0)为title  textOfList.get(1)为contents  textOfList.get(2)为keyWords 
	 * @param sb  从xml文件获取的内容
	 * @return hm_ItemInfo 以哈希表型存储的信息
	 * @throws Exception
	 */
		public static HashMap<String, String> ParseXmlForInfo(List<String> textList, StringBuffer sb) throws Exception
		{
			if (textList==null)
				return null;
			HashMap<String, String> hm_ItemInfo = new HashMap<String, String>(); 				//存储输出HashMap
			String fullText = ""; //存储从输入StringBuffer sb获取的xml全部内容
			if(sb != null)
				fullText = sb.toString();
			
			Pattern pattern = null;
			Matcher matcher = null;
			String textSrc = "";//模式匹配获取的字符串
			if (textList.size() == 1)
			{
				hm_ItemInfo.put("title", textList.get(0));
			}
			if (textList.size() == 2)
			{
				hm_ItemInfo.put("title", textList.get(0));
				hm_ItemInfo.put("content", textList.get(1));
			}
			if (textList.size() == 3)
			{
				hm_ItemInfo.put("title", textList.get(0));
				hm_ItemInfo.put("content", textList.get(1));
				hm_ItemInfo.put("keywords", textList.get(2));
			}
			try{
				pattern = Pattern.compile("<wwwurl>(.+)</wwwurl>");
				matcher = pattern.matcher(fullText);
				if (matcher.find()) {
					textSrc = matcher.group(1);
					if(textSrc==null||textSrc.isEmpty())
						textSrc = "";
					hm_ItemInfo.put("url", textSrc);
				
					//根据短频道获取长频道
					String channel = UrlPretreatment.urlProcessing(textSrc);
					if(channel == null || channel.length() < 9)
						hm_ItemInfo.put("channel", "");
					else{
							hm_ItemInfo.put("channel", channel);
						}
					}
				} catch(Exception e){LOG.error("[ERROR] ", e);}
				
				try{			
					pattern = Pattern.compile("<documenturl>(.+)</documenturl>");
					matcher = pattern.matcher(fullText);
					if (matcher.find()) {
						textSrc = matcher.group(1);
						if(textSrc==null||textSrc.isEmpty())
							textSrc = "";
						hm_ItemInfo.put("documenturl", textSrc);
					}
				} catch(Exception e){LOG.error("[ERROR] ", e);}
				
				try{
					pattern = Pattern.compile("<cat>(.+)</cat>");
					matcher = pattern.matcher(fullText);
					if (matcher.find()) {
						textSrc = matcher.group(1);
						if(textSrc==null||textSrc.isEmpty())
							textSrc = "";
						hm_ItemInfo.put("cat", textSrc);
					}
				} catch(Exception e){LOG.error("[ERROR] ", e);}
				
				try{
					pattern = Pattern.compile("<id>(.+)</id>");
					matcher = pattern.matcher(fullText);
					if (matcher.find()) {
						textSrc = matcher.group(1);
						if(textSrc==null||textSrc.isEmpty())
							textSrc = "";
						hm_ItemInfo.put("documentid", textSrc);
					}
				} catch(Exception e){LOG.error("[ERROR] ", e);}
				
				try{
					pattern = Pattern.compile("<createtime>(.+)</createtime>");
					matcher = pattern.matcher(fullText);
					if (matcher.find()) {
						textSrc = matcher.group(1);
						if(textSrc==null||textSrc.isEmpty())
							textSrc = "";
						hm_ItemInfo.put("date", textSrc);
					}
				} catch(Exception e){LOG.error("[ERROR] ", e);}
				
				try{
					pattern = Pattern.compile("<videotime>(.+)</videotime>");
					matcher = pattern.matcher(fullText);
					if (matcher.find()) {
						textSrc = matcher.group(1);
						if(textSrc==null||textSrc.isEmpty())
							textSrc = "";
						hm_ItemInfo.put("videotime", textSrc);
					}
				} catch(Exception e){LOG.error("[ERROR] ", e);}
				
				try{
					pattern = Pattern.compile("<source>(.+)</source>");
					matcher = pattern.matcher(fullText);
					if (matcher.find()) {
						textSrc = matcher.group(1);
						if(textSrc==null||textSrc.isEmpty())
							textSrc = "";
						hm_ItemInfo.put("source", textSrc);
					}
				} catch(Exception e){LOG.error("[ERROR] ", e);}
				
				try{
					pattern = Pattern.compile("<updatetime>(.+)</updatetime>");
					matcher = pattern.matcher(fullText);
					if (matcher.find()) {
						textSrc = matcher.group(1);
						if(textSrc==null||textSrc.isEmpty())
							textSrc = "";
						hm_ItemInfo.put("updatetime", textSrc);
					}
				} catch(Exception e){LOG.error("[ERROR] ", e);}
				//解析高清图类型的文章，图片的数量
				try{
					pattern = Pattern.compile("<imgnum>(.+)</imgnum>");
					matcher = pattern.matcher(fullText);
					if (matcher.find()) {
						textSrc = matcher.group(1);
						imgCount = Integer.valueOf(textSrc);
						if(textSrc==null||textSrc.isEmpty())
							textSrc = "";
					}
				} catch(Exception e){LOG.error("[ERROR] ", e);}
				try{
					pattern = Pattern.compile("<other>(.+)</other>");
					matcher = pattern.matcher(fullText);
					if(textSrc==null||textSrc.isEmpty())
					{
						textSrc = "imgNum="+String.valueOf(imgCount);
						hm_ItemInfo.put("other", textSrc);
					}
					else
					{
						textSrc = textSrc+"|!|imgNum="+String.valueOf(imgCount);
						hm_ItemInfo.put("other", textSrc);
						if(textSrc.contains("channel"))
						{
							String otherChannel = textSrc.split("channel=")[1].split("\\|!\\|")[0].trim();
							String newurl = otherChannelMap.get(otherChannel)+"/"+hm_ItemInfo.get("documentid")+".html";
							hm_ItemInfo.put("url", newurl);
							hm_ItemInfo.put("channel", newurl);
						}
					}
				} catch(Exception e){LOG.error("[ERROR] ", e);}
			return hm_ItemInfo;
		}
		
		/**
		 * 处理xml文件
		 * 注意：
		 * 
		 * @param dirpath 文件路径
		 * @param delete 解析后删除为true，解析后不删除为false
		 * @return counts 解析文件数
		 */
		public int[] processXML(String dirpath, boolean delete){
			int counts[] = {0,0,0,0};
			try{
				counts[0]++;
				HashMap<String, String> hm_ItemInfo = new HashMap<String, String>();
				String tableName = fieldDicts.appItemTableNameInHbase;
				//判断是否视频xml, 视频xml格式与其他xml格式不一样
				try{
					hm_ItemInfo = ParseXmlForInfo(pretreatmentOfXml(xmlOpen(dirpath)),xmlOpen(dirpath));
					//hm_ItemInfo = ParseXmlForInfo(dirpath);							
					String url=null;
					if(hm_ItemInfo.containsKey("url"))
						url = hm_ItemInfo.get("url");
					StringBuffer sbTmp = new StringBuffer();
					sbTmp.append(dirpath).append(" ").append(url);
					LOG.info("Succeed to parse xml: "+sbTmp.toString());
				}catch (Exception e) {
					LOG.error("Fail to extract with file: "+dirpath);
					LOG.error("[ERROR] ", e);
					counts[1]++;
				}
				try{
					int check = writeItem(tableName, hm_ItemInfo);
					if(check==-1)
						counts[1]++;
				}catch (NullPointerException e) {
					LOG.error("No id in this xml: "+dirpath);
					LOG.error("[ERROR] ", e);
					counts[1]++;
				}catch (Exception e) {
					LOG.error("Fail to write with file: "+dirpath);
					LOG.error("[ERROR] ", e);
					Thread.sleep(10 * 1000);
					return counts;
				}
				if(hm_ItemInfo.containsKey("url"))
					counts[2]++;
				if(hm_ItemInfo.containsKey("channel"))
					counts[3]++;
				//从datasource备份数据到datasource_backup
				if(delete) {
					String dirpathDest = dirpath.replace("search_datasource_backupIKV", "search_datasource_backup");
					String dir_backup = dirpathDest.substring(0, dirpathDest.lastIndexOf("/"));//备份文件目录
					File fileSrc = new File(dirpath);
					File dirDest = new File(dir_backup);
					if(!dirDest.exists())
						dirDest.mkdirs();
					//备份文件
					File fileDest = new File(dirpathDest);
					fileSrc.renameTo(fileDest);
				}
			}catch (Exception e) {
				LOG.error("Fail to rename file: "+dirpath);
				LOG.error("[ERROR] ", e);
				counts[1]++;
			}
			return counts;
		}
		
		/**
		 * 迭代解析xml文件，如果当前文件是文件夹则遍历该文件夹，如果是xml文件则进行放进待处理集合
		 * 注意：
		 * 
		 * @param dirpath 文件路径，type xml类型：1资讯  3博客 4图片 5视频 6专题
		 * @param delete 解析后删除为true，解析后不删除为false
		 * @return
		 */
		public void multiExtractXML(String dirpath, String type, boolean delete){
			File dir = new File(dirpath);
			if(!dir.exists())
				return;
			else {
				if(dir.isDirectory())
				{
					//如果该路径是文件夹，则遍历文件夹
					String filenames[] = dir.list();
					Arrays.sort(filenames);
					if(filenames.length>0){
						for(String filename:filenames){
							multiExtractXML(dirpath+"/"+filename, type, delete);
						}
					}else if(delete && dirpath.split("/").length>=5){//处理完该文件夹中所有xml文件后，删除该文件夹
//						commenFuncs.deleteDirectory(dirpath);
					}
				}else {//如果该路径不是文件夹，则放进待处理集合中
					xmlSet.add(dirpath);
				}
			}
		}
		
		/**
		 * 多线程遍历/datasource/文件夹及其子文件夹，只要存在xml文件就进行解析，并把解析完的xml文件转移到备份文件夹/datasource_backup/，
		 * 注意：
		 * 
		 * @param parallel 线程数
		 * @return 
		 */
		public void multiTraverDirs(int parallel){
			System.out.println("Start!");
			String tableName = fieldDicts.appItemTableNameInHbase;
			HbaseInterface.createTable(tableName,"INFO",-1);
			
			String dir_source = "/data/search_datasource_backupIKV/";//原始文件目录
			
			String typePaths[] = {"1","2","5","10","11"}; //不同类别的xml 1资讯 2带图片的资讯 5视频 10图片 11图书
			
			
			while(true)
			{
				for(int i=0;i<typePaths.length;i++)
				{
					try{
						//遍历子目录，把文件路径放入集合xmlSet中
						String dirpath = dir_source+typePaths[i];
						multiExtractXML(dirpath, typePaths[i],true);
					}catch (Exception e) {
						LOG.error("Fail to deal with folder: "+dir_source+typePaths[i]);
						LOG.error("[OVERALL ERROR] ", e);
						continue;
					}
				}
				if(this.xmlSet.size() > 0){//如果集合内文件非空，则开始多线程处理文件
					List<List<String>> splitReadPath=new ArrayList<List<String>>();
					for(int i=0;i<parallel;i++){
						splitReadPath.add(new ArrayList<String>());
					}
					int size = xmlSet.size();
					Iterator it = this.xmlSet.iterator();
					int para = 0;
					//按线程数把文件分批
					while(it.hasNext()){
						String path = it.next().toString();
						List<String> l= splitReadPath.get(para);
						l.add(path);
						para = (para+1)%parallel;
					}
					xmlSet = new HashSet<String>();
					
					CountDownLatch cl=new CountDownLatch(parallel);
					for(int i=0;i<parallel;i++){
						Thread t=new dirTraver(String.valueOf(i),splitReadPath.get(i),cl,true);
						t.start();
					}
					try {
						cl.await();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						LOG.error("Fail to process xml files is set!");
						LOG.error(e.getMessage(), e);
					}
					LOG.info("multiTraverDirs finished process files: "+size);
				}
				try {
					Thread.sleep(10 * 1000);
				} catch (Exception e) {
					LOG.error("Thread Error",e);
					continue;
				}
			}
		}
		
		
		/**
		 * @param args
		 */
		public static void main(String[] args) {
			// TODO Auto-generated method stub
			ItemToHbase cItemToHbase = new ItemToHbase();
			//多线程遍历处理
			cItemToHbase.multiTraverDirs(Integer.valueOf(args[0]));
			
		}

	

}
