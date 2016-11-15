package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.WordReadData.WordInfo;
import com.ifeng.iRecommend.wuyg.commonData.Update.DateFormatUtil;
import com.ifeng.iRecommend.wuyg.commonData.Update.publish.AllWordPublisher;
/**
 * 
 * <PRE>
 * 作用 : 
 *    可读表测试  
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
 *          1.0          2015年12月29日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class WordReadTest {
	 
    //static	Regex reg=new  Regex("^[a-zA-Z]+$");   
    
	public static void main(String[] args) throws ParseException, IOException {
		// TODO Auto-generated method stub
	     PropertyConfigurator.configure("./conf/log4j.properties");
         
	     WordReadData wordReadData = WordReadData.getInstance();
	    
	     ConcurrentHashMap<String, WordInfo> maps = wordReadData.getWordReadMap();
	     
	     OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File("e:/data/wordReadALLINFO_不可读.txt"), false), "utf-8");
	     Iterator<String> iterator = maps.keySet().iterator();
	     
	     System.err.println(maps.size());
	     
	     HashMap<String, Integer> tempMaps = new HashMap<String, Integer>();
	     
	    // HotWordData hotWordData = HotWordData.getInstance();
	     
	     Pattern pattern = Pattern.compile("^[a-zA-Z]+$");

	     ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("e:/data/wordReadObject"), false));
	     
	     oos.writeObject(maps);
	     
	     oos.flush();
	     oos.close();
	     
	     while(iterator.hasNext()){
	    	 String key = iterator.next();
	    	 
//	    	 if(key.length() >2){
//	    		 continue;
//	    	 }
	    	 
//	    	 if(!maps.get(key).isRead){
//	    		 if(key.contains("|!|")){
//	    			 continue;
//	    		 }
//	    		 osw.append(key+maps.get(key)+"\t"+DateFormatUtil.longToString(maps.get(key).getTimestamp(),"yyyy-MM-dd HH:mm:ss")+"\n");
	    	     osw.append(key+maps.get(key)+"\t"+DateFormatUtil.longToString(maps.get(key).getTimestamp(),"yyyy-MM-dd HH:mm:ss")+"\n");
	    //	 }
//				+ DateFormatUtil.longToString(
//				hotWordInfo.getLatesttimestamp(),
//				"yyyy-MM-dd HH:mm:ss"));
	    	 
//	    	 Matcher matcher = pattern.matcher(key);
//	    	 
//	    	 osw.append(maps.get(key)+"\n");
	    	 
//	    	 if(matcher.find() && maps.get(key).isRead){
//	    		 //osw.append(key+"\t"+maps.get(key).toString()+"\t"+DateFormatUtil.longToString(maps.get(key).getTimestamp(),"yyyy-MM-dd HH:mm:ss")+"\n");
//                     osw.append(key+"\n");
//	    	 }
	    	 
	    	 //	    		int length = commenFuncs.computeWordsLen(key);
//				String tword = splitWord(key);
				
//				if(key.equals("女儿") || key.equals("工作")){
//					System.err.println("  ");
//				}
//				
//				if(!maps.get(key).isRead){
//					continue;
//				}
				
			//	String [] ws = tword.split(" ");
				
				//tempMaps.put(key, maps.get(key).getClientSubWordCount());
	    	 
	     }
	     
	     
//	     List<String> keysList = new ArrayList<String>(tempMaps.keySet());
//	     
//	     Collections.sort(keysList, new Compare<String, Integer>(tempMaps));
//	     
//	     for(String word : keysList){
//	    	 osw.append(word+"\t"+tempMaps.get(word)+"\n");
//	     }
	     
	     osw.flush();
	     osw.close();
	     
	 	while(true){
			System.err.println("请输入:输入-1即可退出");
			Scanner input = new Scanner(System.in);
			String query = input.nextLine();
//			query = "等等";
			if("-1".equals(query)){
				break;
			}
			FileUtil fileUtil = new FileUtil();
			ArrayList<String> dataList = new ArrayList<String>();
			String filepath = new String();
			filepath = LoadConfig.lookUpValueByKey("Filedir") + "word.txt";
			//ArrayList<String> fileList = fileUtil.refreshFileList(filepath);
			//for (String filename : fileList) {
			String content = fileUtil.Read(filepath, "utf-8");
			dataList.addAll(Arrays.asList(content.split("\n")));
			//}
			//System.err.println(dataList);
			for(String qword : dataList){
				qword = qword.toLowerCase();
				//System.err.println(qword);
				WordInfo wordInfo = wordReadData.searchWord(qword);
				System.err.println(qword+":"+wordInfo+"\t");
				if(null != wordInfo){
					System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),"yyyy-MM-dd HH:mm:ss"));
				}
			}
		}
	}

	/**
	 * 
	* @Title:splitWord
	* @Description:对内容进行分词
	* @param textSplited
	* @return
	* @author:wuyg1
	* @date:2015年12月17日
	 */
	private static String splitWord(String textSplited){
		int icount = 1;
		while(icount <=3){
			try {
				textSplited = new String(SplitWordClient.split(textSplited, null).replace("(/", "_").replace(") ", " "));
			    break;
			} catch (Exception e) {
				if(icount == 3){
					break;
				}
				icount ++;
			}
		}
		return textSplited;
	}
	
}
