package com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.featureEngineering.OrientDB.KnowledgeGraph;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.tinkerpop.blueprints.Vertex;

public class CopyOfTimeSensitiveJudgementTest {
	static Log LOG = LogFactory.getLog(CopyOfTimeSensitiveJudgementTest.class);

	private static IKVOperationv2 ob = null;

	private static String tablename = LoadConfig.lookUpValueByKey("itemdb");

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		PropertyConfigurator.configure("./conf/log4j.properties");
		TimeSensitiveJudgement timeSensitiveJudgement = TimeSensitiveJudgement
				.getInstance();

		ob = new IKVOperationv2(tablename);

		Random random = new Random();

		OutputStreamWriter oswEffective = new OutputStreamWriter(
				new FileOutputStream(new File(
						"D:/data/logs/articleEffective/具有时效性.txt"), false),
				"utf-8");

		OutputStreamWriter oswUnEffective = new OutputStreamWriter(
				new FileOutputStream(new File(
						"D:/data/logs/articleEffective/不具有时效性.txt"), false),
				"utf-8");

//		 for (int id = 5745000; id < 5750000; id = id + 1) {
//				
//			 //int index = random.nextInt(100);
//			 int index = 0;
//			 System.err.println("id:"+id);
//			
//			 itemf item = ob.queryItemF(Integer.toString(id + index), "d");
//			
//			 if (item == null || item.getID() == null)
//			 continue;
//			
//			 ArticleTimeInfo articleTimeInfo = new ArticleTimeInfo();
//			
//			 HashSet<String> categorySet = categoryExtr(item);
//			
//			 List<String> categoryList = new ArrayList<String>();
//			 categoryList.addAll(categorySet);
//			
//			 TimeSensitiveInfo timeSensitiveInfo = timeSensitiveJudgement
//						.EstimateTimeSensitiveOfArticle(item.getPublishedTime(),
//								item.getSplitTitle(), item.getSplitContent(),
//								categoryList);
//
//				// 内容时效性评级
//				String timeLevel = null;
//				timeLevel = timeSensitiveInfo.getOthers();
//				if (timeLevel.contains("sensitiveLevel=")) {
//					timeLevel = timeLevel.substring(timeLevel
//							.indexOf("sensitiveLevel=")
//							+ "sensitiveLevel=".length());
//					timeLevel = timeLevel.substring(0, timeLevel.indexOf("|!|"));
//				}
//				
//				boolean onlyNumberFlag = timeLevel.matches("[0-9]+");
//				
//				String article_deadLine = null;
//				
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				
//				if(onlyNumberFlag){
//					
//					int dateCount = Integer.valueOf(timeLevel);
//					
//					article_deadLine = CalendarParser.getCertainDate(item.getPublishedTime(), dateCount, sdf);
//					
//				}else {
//					switch (TimeSensitiveLevel.getSensitiveLevelType(timeLevel)) {
//					case h1:
//						article_deadLine = "一小时";
//						break;
//					case h2:
//						article_deadLine = "两小时";
//						break;
//					case h3:
//						article_deadLine = "三小时";
//						break;
//					case h8:
//						article_deadLine = "八小时";
//						break;
//					case td:
//						article_deadLine = CalendarParser.getCertainDate(item.getPublishedTime(), 0, sdf);
//						break;
//					case d3:
//						article_deadLine = CalendarParser.getCertainDate(item.getPublishedTime(), 3, sdf);
//						break;
//					case d5:
//						article_deadLine = CalendarParser.getCertainDate(item.getPublishedTime(), 5, sdf);
//						break;
//					case d7:
//						article_deadLine = CalendarParser.getCertainDate(item.getPublishedTime(), 7, sdf);
//						break;
//					case tw:
//						article_deadLine = CalendarParser.getCertainWeek(item.getPublishedTime(), 0, sdf);
//						break;
//					case tq:
//						article_deadLine = CalendarParser.getCertainQuarter(item.getPublishedTime(), sdf);
//						break;
//					default:
//						break;
//					}
//				}
//				
//				
//				if (timeSensitiveInfo.isTimeSensitive()) {
//
//					oswEffective.write("具有时效性----------》" + item.getPublishedTime() +"\t"+item.getID() + "\t" + article_deadLine
//							+ "\t" + item.getTitle() + "\t" + categoryList);
//				} else {
//					oswUnEffective.write("不具有时效性----------》" +item.getPublishedTime() +"\t"+ item.getID() + "\t" + "NT"
//							+ "\t" + item.getTitle() + "\t" + categoryList);
//				}
//			
//			
//			 LOG.info("================================================");
//			 }
//		 oswEffective.flush();
//		 oswEffective.close();
//		
//		 oswUnEffective.flush();
//		 oswUnEffective.close();

		while (true) {
			System.err.println("请输入query内容，输入-1退出：");

			Scanner scanner = new Scanner(System.in);
			String query = scanner.nextLine();

			if ("-1".equals(query)) {
				break;
			}
			

			itemf item = ob.queryItemF(query, "d");

			if (item == null || item.getID() == null)
				continue;

			HashSet<String> categorySet = categoryExtr(item);

			List<String> categoryList = new ArrayList<String>();
			categoryList.addAll(categorySet);

			// TimeSensitiveInfo timeSensitiveInfo =
			// timeSensitiveJudgement.EstimateTimeSensitiveOfArticle(item.getSplitTitle(),item.getSplitContent(),categoryList);

			String splitContent = item.getSplitContent();
			
//			splitContent = splitContent.replace("(_w 香港_x -_w 2016年_t 10月_t 8日_t )_w", "");
//			
//			splitContent = splitContent.replace("昨日", "");
			
			TimeSensitiveInfo timeSensitiveInfo = timeSensitiveJudgement
					.EstimateTimeSensitiveOfArticle(item.getPublishedTime(),
							item.getSplitTitle(), splitContent,
							categoryList);

			// 内容时效性评级
			String time_deadLine = null;
			time_deadLine = timeSensitiveInfo.getOthers();
			if (time_deadLine.contains("sensitiveLevel=")) {
				time_deadLine = time_deadLine.substring(time_deadLine
						.indexOf("sensitiveLevel=")
						+ "sensitiveLevel=".length());
				time_deadLine = time_deadLine.substring(0, time_deadLine.indexOf("|!|"));
			}
			
			
			if (timeSensitiveInfo.isTimeSensitive()) {

				LOG.info("具有时效性----------》" + item.getPublishedTime()+"\t"+timeSensitiveInfo.getTimeTag()+"\t有效期："+time_deadLine+"\t"+item.getID()
						+ "\t" + item.getTitle() + "\t" + categoryList+"\n");
			} else {
				LOG.info("不具有时效性----------》" +item.getPublishedTime() +"\t"+ item.getID() + "\t" + "NT"
						+ "\t" + item.getTitle() + "\t" + categoryList+"\n");
			}
		}
		ob.close();
	}

	/**
	 * 
	 * @Title:categoryExtr
	 * @Description: 抽取出文章所属的类别，有可能存在多个类别
	 * @param item
	 * @return
	 * @author:wuyg1
	 * @date:2016年8月16日
	 */
	public static HashSet<String> categoryExtr(itemf item) {

		HashSet<String> c0 = new HashSet<String>();
		ArrayList<String> featureList = item.getFeatures();
		for (int q = 0; q < featureList.size() - 2; q += 3) {
			String clazz = featureList.get(q);
			String label = featureList.get(q + 1);
			String weight = featureList.get(q + 2);
			double w = Double.parseDouble(weight);
			w = Math.abs(w);
			if ("c".equals(label) && weight.startsWith("-") && w >= 0.5) {
				c0.add(clazz);
				continue;
			} else if ("c".equals(label) && w >= 0.5) {

				// KnowledgeGraph kgraph = new KnowledgeGraph();
				// ArrayList<Vertex> vertexList = kgraph.queryParent(clazz,
				// "c1");
				// if (vertexList == null || vertexList.size() <= 0)
				// c0.add(clazz);
				continue;
			}
		}

		if (null == c0 || c0.isEmpty()) {
			return new HashSet<String>();
		} else {
			return c0;
		}
	}

}
