package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.PropertyConfigurator;

import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.wuyg.commonData.Update.DateFormatUtil;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;
import com.ifeng.iRecommend.wuyg.commonData.entity.entityBase.KnowledgeBaseBuild;
/**
 * 
 * <PRE>
 * 作用 : 
 *    该类为热词的订阅接收示例  
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
 *          1.0          2015年12月10日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class HotWordDataTest {

	public static void main(String[] args) throws ParseException, IOException {
	        PropertyConfigurator.configure("./conf/log4j.properties");
            HotWordData hotWordData = HotWordData.getInstance();
	        
	        
            CommonDataSub cds = new CommonDataSub();
    		Thread t = new Thread(cds); 
    		t.start();
    		
//    		ConcurrentHashMap<String, HotWordInfo> maps = hotWordData.getHotwordMap();
//    		
//    		Iterator<String> iterator = maps.keySet().iterator();
//    		
//    		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File("e:/data/热点事件.txt"),false), "utf-8");
//    		
//    		while(iterator.hasNext()){
//    			String key = iterator.next();
//    			
//    			HotWordInfo hotWordInfo = maps.get(key);
//    			
//    			//if(!hotWordInfo.isRead()){
//    				osw.append(key+"\t"+hotWordInfo+"\t"+DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(), "yyyy-MM-dd HH:mm:ss")+"\t"+DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(), "yyyy-MM-dd HH:mm:ss")+"\n");
//    			//}
//    			
//    			//if(null != hotWordInfo.getDocumentId()){
//    			//	osw.append(key+"\t"+hotWordInfo+"\t"+DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(), "yyyy-MM-dd HH:mm:ss")+"\t"+DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(), "yyyy-MM-dd HH:mm:ss")+"\n");
//        			
//    			//}
//    			
//    			
//    			//osw.append(hotWordInfo+"\n");
//    			
////    			if(!(hotWordInfo.getSplitContent().contains(" ")|| hotWordInfo.getSplitContent().contains("_"))){
////    				continue;
////    			}
//    			
////    			String segmentWord = hotWordInfo.getSplitContent();
////    			
////    			StringBuffer sbBuffer = new StringBuffer();
//    			
////    			for(String word : segmentWord.split(" ")){
////    				if(null == word || word.isEmpty() || word == "" || !word.contains("_")){
////    					continue;
////    				}
////    				//word = word.substring(0, word.lastIndexOf("_"));
////    				sbBuffer.append(word+"\t");
////    			}
////    			
////    			osw.append(sbBuffer.toString()+"\n");
//    		}
//    		
//    		osw.flush();
//    		osw.close();
    		
    		while(true){
    			
    			System.err.println("请输入:输入-1即可退出");
    			Scanner input = new Scanner(System.in);
    			String query = input.nextLine();
    			if("-1".equals(query)){
    				break;
    			}
    		//	String query = "杀岳父母携妻埋尸";
    			HotWordInfo hotWordInfo  = hotWordData.searchHotWord(query);
    			System.err.println("query："+hotWordInfo);
    			if(null != hotWordInfo){
    				String starttime = DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(), "yyyy-MM-dd HH:mm:ss");
        			String latesttime = DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(), "yyyy-MM-dd HH:mm:ss");
        			System.err.println("query："+starttime+"\t"+latesttime);
    			}
    			
    			
//    			hotWordInfo  = hotWordData.getHotwordMap().get("北大学生杀母");
//    			System.err.println("北大学生杀母："+hotWordInfo);
//    			starttime = DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			latesttime = DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			System.err.println("北大学生杀母："+starttime+"\t"+latesttime);
//    			
//    			hotWordInfo  = hotWordData.getHotwordMap().get("风车动漫");
//    			System.err.println("风车动漫："+hotWordInfo);
//    			starttime = DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			latesttime = DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			System.err.println("风车动漫："+starttime+"\t"+latesttime);
//    			
//    			hotWordInfo  = hotWordData.getHotwordMap().get("葛剑雄");
//    			System.err.println("葛剑雄："+hotWordInfo);
//    			starttime = DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			latesttime = DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			System.err.println("葛剑雄："+starttime+"\t"+latesttime);
//    			
//    			hotWordInfo  = hotWordData.getHotwordMap().get("中超");
//    			System.err.println("中超："+hotWordInfo);
//    			starttime = DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			latesttime = DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			System.err.println("中超："+starttime+"\t"+latesttime);
//    			
//    			hotWordInfo  = hotWordData.getHotwordMap().get("天价鱼");
//    			System.err.println("天价鱼："+hotWordInfo);
//    			starttime = DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			latesttime = DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			System.err.println("天价鱼："+starttime+"\t"+latesttime);
//    			
//    			hotWordInfo  = hotWordData.getHotwordMap().get("平板支撑");
//    			System.err.println("平板支撑："+hotWordInfo);
//    			starttime = DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			latesttime = DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(), "yyyy-MM-dd HH:mm:ss");
//    			System.err.println("平板支撑："+starttime+"\t"+latesttime);
//    			
////    			starttime = DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(), "yyyy-MM-dd HH:mm:ss");
////    			latesttime = DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(), "yyyy-MM-dd HH:mm:ss");
////    			System.err.println("农妇被巫师治死："+starttime + "\t" + latesttime);
//    			
//    			System.err.println("MH370："+hotWordData.getHotwordMap().get("MH370"));
////    			
////    			System.err.println("全智贤获总统表彰："+hotWordData.getHotwordMap().get("全智贤获总统表彰"));
////    			
////    			System.err.println("测试--："+hotWordData.getHotwordMap().get("测试"));
//    			
//    			try {
//				//	Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
    		}
	}

}
