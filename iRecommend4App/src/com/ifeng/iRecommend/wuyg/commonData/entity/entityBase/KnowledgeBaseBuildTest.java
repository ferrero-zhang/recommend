package com.ifeng.iRecommend.wuyg.commonData.entity.entityBase;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.publish.UserDicPublisher;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;

public class KnowledgeBaseBuildTest {
	
	static Log LOG = LogFactory.getLog(KnowledgeBaseBuildTest.class);
	
	private static final String FilePath = "D:/data/wuyonggang/baseDatabase/topbaidu/fromredisUpdate/word/";
	
	static FileUtil fileUtil = new FileUtil();

	private Scanner input;
	
	static String userDicPattern = LoadConfig
			.lookUpValueByKey("userDicPattern");
	static String UserDicDataDbNum = LoadConfig
			.lookUpValueByKey("UserDicDataDbNum");
	
	@Test
	public void testInitEntityTree() {
		//KnowledgeBaseBuild.initEntityTree(entityPath);
		//KnowledgeBaseBuild.initpartEntityTree("entLib_军事武器&entLib_公司");
		KnowledgeBaseBuild.initEntityTree();
	}

	@Test
	public void testUpdateFileArray() {
		File [] files = (new File(FilePath)).listFiles();
		KnowledgeBaseBuild.update(files);
	}

	public void testUpdateWordsAlter(){
		String content = fileUtil.Read(FilePath+"alterword.txt");
		KnowledgeBaseBuild.updateWords_Alter(content);
//        ArrayList<String> records = new ArrayList<String>(Arrays.asList(content.split("\n")));
//        KnowledgeBaseBuild.updateWords_Alter(records);
	}
	
	public void testUpdateWordsDel(){
		String content = fileUtil.Read(FilePath+"delword.txt");
		KnowledgeBaseBuild.updateWords_Del(content);
//        ArrayList<String> records = new ArrayList<String>(Arrays.asList(content.split("\n")));
//        KnowledgeBaseBuild.updateWords_Del(records);
	}
	
	@Test
	public void testUpdateArrayListOfEntityInfo() {
		String content = fileUtil.Read(FilePath+"addword.txt");
		content = "entLib_IT术语@w:李三#num:0#c:科技#ls:[科技, 人物术语]#ns:[]";
		KnowledgeBaseBuild.updateWords_Add(content);
       // ArrayList<String> records = new ArrayList<String>(Arrays.asList(content.split("\n")));
		//KnowledgeBaseBuild.updateWords_Add(records);
	}

	@Test
	public void testGetEntityList() {
		while(true){
			LOG.info("请输入:输入-1即可退出");
			input = new Scanner(System.in);
			String query = input.nextLine();
			if("-1".equals(query)){
				break;
			}
			String json = KnowledgeBaseBuild.getEntityList(query);
			ArrayList<EntityInfo> entityInfos = KnowledgeBaseBuild.getObjectList(query);
            LOG.info(entityInfos);
			LOG.info(json);
		}
	}
	
	public void testUpdateFileAlter(){
		String content = fileUtil.Read(FilePath+"alterfile.txt", "utf-8");
		content = "entLib_战争大咖$entLib_军事武器";
		KnowledgeBaseBuild.updateFile_Alter(content);
	}
	
	public void testUpdateFileAdd(){
		String content = fileUtil.Read(FilePath+"addfile.txt", "utf-8");
		KnowledgeBaseBuild.updateFile_Add(content);
	}
	
	public void testUpdateFileDel(){
		String content = fileUtil.Read(FilePath+"delfile.txt", "utf-8");
		content = "del_entLib_军事武器&del_entLib_公司";
		KnowledgeBaseBuild.updateFile_Del(content);
	}

	public static void main(String [] args) throws IOException{
		
		PropertyConfigurator.configure("./conf/log4j.properties");
		
//		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File("e:/allwords.txt"), false), "utf-8");
//		
//		HashMap<String, List<String>> dataMap = KnowledgeBaseBuild.getAllEntityMap();
//		
//		Iterator<String> iterator = dataMap.keySet().iterator();
//		
//		KnowledgeBaseBuild.initEntityTree();
//		
//		while(iterator.hasNext()){
//			String key = iterator.next();
//			
//			if(!key.equals("entLib_地区")){
//				continue;
//			}
//
//			List<String> list = dataMap.get(key);
//			for(String str : list){
//			
//				EntityInfo entityInfo = EntityInfo.redis2Object(str, key);
//				
//				boolean flag = false;
//				
//				for(String nick : entityInfo.getNicknameList()){
//					if(nick.endsWith("省")){
//						flag = true;
//						break;
//					}
//				}
//				
//				if(!(entityInfo.getWord().endsWith("市") || flag)){
//					continue;
//				}
//				
//				if(entityInfo.toString().contains("category:全部")){
//					//System.err.println(entityInfo.toString());
//					continue;
//				}
//				
//				if(!entityInfo.getCategory().equals("社会")){
//					System.err.println(entityInfo);
//				}
//				
//				
//				if(KnowledgeBaseBuild.getEntityList(entityInfo.getWord()).contains("全领域")){
//					//System.err.println(entityInfo.toString());
//					continue;
//				}
//				
//			   osw.append(entityInfo.toString()+"\n");
//			}
//		}
//		
//		osw.flush();
//		osw.close();
		
//		UserDicPublisher userDicPublisher = new UserDicPublisher(userDicPattern, userDicPattern);
//		
//		ArrayList<String> userDicList = new ArrayList<String>();
//		
//		KnowledgeBaseBuild.initEntityTree();
//		
//		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File("e:/et.txt"), false), "utf-8");
//		
//		for(EntityInfo entityInfo : KnowledgeBaseBuild.wordInfos){
//		    userDicList.add(entityInfo.getWord());
//		    userDicList.addAll(entityInfo.getNicknameList());
//		}
//		
//		for(String userWord : userDicList){
//			osw.append(userWord);
//			osw.append("\n");
//		}
//		
//		userDicPublisher.delWordsFromUserDic(userDicList);
//		osw.flush();
//		osw.close();
		
//		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File("e:/艺术人物.txt"), false), "utf-8");
//		KnowledgeBaseBuild knowledgeBaseBuild = new KnowledgeBaseBuild();
//		knowledgeBaseBuild.readEntityFromRedis();
//		for(EntityInfo entityInfo : knowledgeBaseBuild.wordInfos){
//			if(entityInfo.getFilename().endsWith("艺术人物")){
//				osw.append(entityInfo.toString()+"\n");
//			}
//			
//		}
//		osw.flush();
//		osw.close();
		
		//统计术语的类别与别名
		
//		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File("e:/allET.txt"), false), "utf-8");
//		KnowledgeBaseBuild knowledgeBaseBuild = new KnowledgeBaseBuild();
//		knowledgeBaseBuild.readEntityFromRedis();
//		HashMap<String, HashMap<String, ArrayList<String>>> Maps = new HashMap<String, HashMap<String,ArrayList<String>>>();
//		for(EntityInfo entityInfo : knowledgeBaseBuild.wordInfos){
//				
//				if(entityInfo.getWord().equals("人工智能")){
//					System.err.println();
//				}
//				ArrayList<String> nickList = new ArrayList<String>();
//				for(String nick : entityInfo.getNicknameList()){
//					if(null == nick || nick.trim().isEmpty()){
//						continue;
//					}
//					nickList.add(nick);
//				}
//				HashMap<String, ArrayList<String>> cateMaps = new HashMap<String, ArrayList<String>>();
//				if(Maps.containsKey(entityInfo.getWord())){
//					if(Maps.get(entityInfo.getWord()).containsKey(entityInfo.getCategory())){
//						Maps.get(entityInfo.getWord()).get(entityInfo.getCategory()).addAll(nickList);
//					}else{
//						Maps.get(entityInfo.getWord()).put(entityInfo.getCategory(), nickList);
//					}
//				}else{
//					cateMaps.put(entityInfo.getCategory(), nickList);
//					Maps.put(entityInfo.getWord(), cateMaps);
//				}
//		}
//		Iterator<String> iterator = Maps.keySet().iterator();
//		while(iterator.hasNext()){
//			String key = iterator.next();
//			
//			HashMap<String, ArrayList<String>> cateNickMaps = Maps.get(key);
//			
//			Iterator<String> cateIterator = cateNickMaps.keySet().iterator();
//			while(cateIterator.hasNext()){
//				String category = cateIterator.next();
//				osw.append(key+"||"+category);
//				if(cateNickMaps.get(category).size() >0 && null != cateNickMaps.get(category)){
//					osw.append("||");
//					for(String nick : cateNickMaps.get(category)){
//						osw.append(nick+"\t");
//					}
//				}
//				
//				osw.append("\n");
//			}
//			
//		}
//		
//		osw.flush();
//		osw.close();
//		
		
		KnowledgeBaseBuildTest knowledgeBaseBuildTest = new KnowledgeBaseBuildTest();
		
		//初始化
		knowledgeBaseBuildTest.testInitEntityTree();
		knowledgeBaseBuildTest.testGetEntityList();
		
		
		//文件添加
//        knowledgeBaseBuildTest.testUpdateFileAdd();
//		knowledgeBaseBuildTest.testGetEntityList();
//
////		//文件删除
////		knowledgeBaseBuildTest.testUpdateFileDel();
////		knowledgeBaseBuildTest.testGetEntityList();
//		
////		//文件修改
////		knowledgeBaseBuildTest.testUpdateFileAlter();
////		knowledgeBaseBuildTest.testGetEntityList();
//		
//		//增加新节点
//		knowledgeBaseBuildTest.testUpdateArrayListOfEntityInfo();
//		knowledgeBaseBuildTest.testGetEntityList();
//		//对原有节点修改
//		knowledgeBaseBuildTest.testUpdateWordsAlter();
//		knowledgeBaseBuildTest.testGetEntityList();		
//		//对原有节点删除
//		knowledgeBaseBuildTest.testUpdateWordsDel();
//		knowledgeBaseBuildTest.testGetEntityList();
		
	}
	
}

class MyThread extends Thread{
	KnowledgeBaseBuildTest knowledgeBaseBuildTest;
	
	public MyThread(KnowledgeBaseBuildTest knowledgeBaseBuildTest){
		this.knowledgeBaseBuildTest = knowledgeBaseBuildTest;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}

class MyThread1 extends MyThread{

	public MyThread1(KnowledgeBaseBuildTest knowledgeBaseBuildTest) {
		super(knowledgeBaseBuildTest);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		knowledgeBaseBuildTest.testInitEntityTree();
	}
	
}

class MyThread2 extends MyThread{

	public MyThread2(KnowledgeBaseBuildTest knowledgeBaseBuildTest) {
		super(knowledgeBaseBuildTest);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		//knowledgeBaseBuildTest.testUpdateArrayListOfEntityInfo();
		knowledgeBaseBuildTest.testGetEntityList();
	}
	
}

class Entity{
	String category;
	ArrayList<String> nickname = new ArrayList<String>();
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public ArrayList<String> getNickname() {
		return nickname;
	}
	public void setNickname(ArrayList<String> nickname) {
		this.nickname = nickname;
	}
}