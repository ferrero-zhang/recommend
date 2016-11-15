package com.ifeng.iRecommend.featureEngineering.KnowledgeGraphSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.stream.events.EndDocument;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.sun.org.apache.bcel.internal.generic.LoadClass;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
/**
 * 
 * <PRE>
 * 作用 : 
 *    知识图谱相关的检索操作  
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
 *          1.0          2016年5月13日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class KnowledgeGraphSearchTest {
	  
	public static Log LOG = LogFactory.getLog(KnowledgeGraphSearchTest.class);
	
	public static void main(String args[]){
		
		PropertyConfigurator.configure("/data/wuyg/GraphSearch/conf/log4j.properties");
		
		FileUtil fileUtil = new FileUtil();
		
		String path = LoadConfig.lookUpValueByKey("graphSearchFile");
		
		String content = fileUtil.Read(path, "utf-8");
		
		ExecutorService service = Executors.newCachedThreadPool();
		
		ArrayList<String> queryList = new ArrayList<String>();
		
		queryList.addAll(Arrays.asList(content.split("\n")));
		
		int threadCount = Integer.valueOf(LoadConfig.lookUpValueByKey("treadCount"));
		
		int eachCount = queryList.size() / threadCount;
		
		int start = 0;
		int end = 0;
		
		long starttime = System.currentTimeMillis();
		
		for(int index =0; index <= threadCount; index++){
			start = index * eachCount;
			end = start + eachCount;
			
			if(end > queryList.size()){
				end = queryList.size();
			}
			
			if(start == end){
				continue;
			}
			
			ArrayList<String> qList = new ArrayList<String>();
			
			for(int i = start;i< end;i++){
				qList.add(queryList.get(i));
			}
			LOG.info(start+"\t\t"+end+"\t"+qList.size());
			
			service.execute(new KnowledgeGraphSearchTest().new Task(qList));
		}
		
		service.shutdown();
		
		while(!service.isTerminated()){
			try {
				Thread.sleep(60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		long endtime = System.currentTimeMillis();
		LOG.info("总运行时间" + (endtime - starttime) + "ms");
	}
	
	public class Task implements Runnable{

		ArrayList<String> queries = new ArrayList<String>();
		
		HashMap<String, Integer> relationMap = new HashMap<String, Integer>();
		
		public Task(ArrayList<String> qList) {
			queries = qList;
		}
		
		@Override
		public void run() {
			
			LOG.info("任务长度："+queries.size()+"\t"+queries.get(0)+"\t"+queries.get(queries.size()-1));

			for(String query : queries){
				
				KnowledgeGraphSearch knowledgeGraphSearch = KnowledgeGraphSearch.getInstance();
				
				ArrayList<PersonInfo> quePersonInfos = knowledgeGraphSearch
						.getRelationNodes(query);

				for (PersonInfo personInfo : quePersonInfos) {
					LOG.info(personInfo.getWord() + "==="+personInfo.getRelatedNodeMap()+"\t"
							+ personInfo.getRelationNodeList());
				
				
					HashMap<String, HashSet<String>> maps = personInfo.getRelatedNodeMap();
					
					Iterator<String> iterator = maps.keySet().iterator();
					
					while(iterator.hasNext()){
						String key = iterator.next();
						if(relationMap.containsKey(key)){
							relationMap.put(key, relationMap.get(key)+1);
						}else{
							relationMap.put(key, 1);
						}
					}
					
				}
				LOG.info("===============================");
				knowledgeGraphSearch.shutdown();
				
				ArrayList<String> keyList = new ArrayList<String>(relationMap.keySet());
				Collections.sort(keyList, new MapCompare<String, Integer>(relationMap));
				
				for(String key : keyList){
					LOG.info(key+"\t"+relationMap.get(key));
				}
			}
			
		}
		
	}

}
