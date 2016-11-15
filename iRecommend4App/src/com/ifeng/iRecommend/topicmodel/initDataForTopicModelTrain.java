/**
 * 
 */
package com.ifeng.iRecommend.topicmodel;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.axis2.AxisFault;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.commenFuncs;

/**
 * <PRE>
 * 作用 : 
 *   读取训练文档并生成对应corpus：doc_id、dict、doc_word_cnt矩阵文件;
 *   注意item自身的id也要做一个新的id编号，从0开始，放入doc_id文档中；
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
 *          1.0          2013-4-16        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

class int2double implements Comparable<int2double> {
	int tf;
	int docfreq;
	double value;

	@Override
	public int compareTo(int2double arg0) {
		// TODO Auto-generated method stub
		if (value > arg0.value)
			return 1;
		return -1;
	}
}

public class initDataForTopicModelTrain {
	private static String date_regex = "([0-9]{4}-)?[0-9]{2}-[0-9]{2}"; //2010-01-01 format
	private static String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
	
	/*
	 *  临时工作
	 */
	public static void job1(String path){
		//1.遍历每一个分词缓存文件，记录每个word的tf及docfreq信息		
		File f = new File(path);
		final File[] files = f.listFiles();
		// 统计出现词的tf、idf信息
		final int allDocsNum = files.length;
		int docid = 0;
		//改造，分成多线程版本来做；统计每个词的tf、idf信息
		final HashMap<String, int2double> hm_allword_tfidf = new HashMap<String, int2double>();
		final CountDownLatch cdl = new CountDownLatch(8);
		for(int i = 0; i < 8; ++i)			
		{
			final int begin = (allDocsNum/8)*i;
			final int end = allDocsNum < (begin+allDocsNum/8)?allDocsNum:(begin+allDocsNum/8);
			final File[] somefiles = new File[end-begin];
			for(int k=begin;k<end;k++)
				somefiles[k-begin] = files[k];	
			final HashMap<String, int2double> hm_word_tfidf = new HashMap<String, int2double>();
			new Thread(new Runnable() {
				@Override
				public void run() {
					StringBuffer sbTmp = new StringBuffer();
					for (int j = 0; j < (end-begin); j++) {
						if(!somefiles[j].getName().endsWith("txt"))
							continue;
						if(j%10000 == 0)
							System.out.println(j);

						if (j % 10000 == 0 && j > 0) {
							commenFuncs.writeResult("allitems/",
									System.currentTimeMillis() + ".items",
									sbTmp.toString(), "utf-8", false, null);
							sbTmp.delete(0, sbTmp.length());
						}
						
						FileUtil docFile = new FileUtil();
						docFile.Initialize(somefiles[j].getAbsolutePath(), "UTF-8");
						String line = "";
						while((line=docFile.ReadLine())!=null){
							sbTmp.append(line).append("\n");
						}
						sbTmp.append("--------\n");
						docFile.CloseRead();
						//删除文件
						if(!somefiles[j].delete())
							System.out.println("delete error");
						
					}	
					commenFuncs.writeResult("allitems/",
							System.currentTimeMillis() + ".items",
							sbTmp.toString(), "utf-8", false, null);
					System.out.println("thread " + begin + " has finished...");
					cdl.countDown();
				}

			}).start();

		
		}
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("now all threads have finished");
	}
	
	/*
	 * 1.遍历保存下的每个文档doc，统计tfidf，生成dict排序表
	 */
	public static void job2(String path,int wordNumAssign){
		
		
		//1.遍历每一个分词缓存文件，记录每个word的tf及docfreq信息		
		File f = new File(path);
		final File[] files = f.listFiles();
		// 统计出现词的tf、idf信息
		final int allDocsNum = files.length;
		final int2double realAllDocsNum = new int2double();
		//改造，分成多线程版本来做；统计每个词的tf、idf信息
		final HashMap<String, int2double> hm_allword_tfidf = new HashMap<String, int2double>();
		final CountDownLatch cdl = new CountDownLatch(8);
		for(int i = 0; i < 12; ++i)			
		{
			final int begin = (allDocsNum/12)*i;
			final int end = allDocsNum < (begin+allDocsNum/12)?allDocsNum:(begin+allDocsNum/12);
			final File[] somefiles = new File[end-begin];
			for(int k=begin;k<end;k++)
				somefiles[k-begin] = files[k];	
			final HashMap<String, int2double> hm_word_tfidf = new HashMap<String, int2double>();
			new Thread(new Runnable() {
				@Override
				public void run() {
					//0.读取停用词表
					FileUtil stopwordsFile = new FileUtil();
					stopwordsFile.Initialize("stopwords.txt", "UTF-8");
					String line = "";
					final HashSet<String> hs_stop = new HashSet<String>();
					while((line=stopwordsFile.ReadLine())!=null){
						hs_stop.add(line.trim());
					}
					
					int2double realDocsNum = new int2double();
					for (int j = 0; j < (end-begin); j++) {
						if(j%1000 == 0)
							System.out.println(somefiles[j].getAbsolutePath());
						FileUtil docFile = new FileUtil();
						docFile.Initialize(somefiles[j].getAbsolutePath(), "UTF-8");
						line = "";
						HashSet<String> hss = new HashSet<String>();
						boolean foundTitle = false;
						while((line=docFile.ReadLine())!=null){
							line = line.trim();
							if(line.isEmpty())
								continue;
						
							//分割文件
							if(line.indexOf("--------")>=0){
								realDocsNum.tf = realDocsNum.tf + 1;
								hss.clear();
								foundTitle = false;
								continue;
							}
							
							int w = 1;
							//title
							if(!foundTitle){
								foundTitle = true;
								w = 2;
							}
							
							String[] secs = line.split("\\s");
							for(int j1=0;j1<secs.length;j1++){
								String word = secs[j1].toLowerCase();	
								if(hs_stop.contains(word))
									continue;
								//过滤“的(/u)”这种词
								String[] words = word.split("[\\s\\(\\)]");
								if(words.length > 1)
									word = words[0];
								//去除单个字
								if(word.length()<=1)
									continue;
								//过滤"12日\3月\2013年"
								if(word.matches("\\d{1,4}[万亿多余]?[倍米尺号日月年元个套户台亩度名分吨辆][左]?[右]?"))
									continue;
								//过滤"3.2亿英镑 490.35亿元 1.5万套"
								if(word.matches("^.*?\\d{1,10}.\\d{1,10}.*?$"))
									continue;
								//过滤"48小时"
								if(word.matches("\\d{1,2}小时"))
									continue;
								//过滤"1.4 1-4"
								if(word.matches("\\d{1,2}[._-]\\d{1,2}"))
									continue;
								//过滤html tag
								if(word.matches("^.*?(width|tr|td|height|span|center).*?$"))
									continue;
								
								// 统计每个词的tf和idf信息
								int2double itmp = hm_word_tfidf.get(word);
								if (itmp == null) {
									int2double i2 = new int2double();
									i2.tf = w;
									i2.docfreq = 1;
									hm_word_tfidf.put(word, i2);
									hss.add(word);
								} else {
									itmp.tf = itmp.tf + w;
									if (!hss.contains(word)) {
										hss.add(word);
										itmp.docfreq = itmp.docfreq + 1;
									}
								}
							
							}
						}
					}	
					
					//combine
					System.out.println("combine");
					synchronized(hm_allword_tfidf){
						realAllDocsNum.tf += realDocsNum.tf;
						Set<Entry<String, int2double>> es = hm_word_tfidf.entrySet();
						Iterator<Entry<String, int2double>> it = es.iterator();
						while (it.hasNext()) {
							Entry<String, int2double> et = it.next();
							int2double i2 = hm_allword_tfidf.get(et.getKey());
							if(i2 == null)
								i2 = et.getValue();
							else{
								i2.docfreq += et.getValue().docfreq;
								i2.tf += et.getValue().tf;
							}
							hm_allword_tfidf.put(et.getKey(), i2);	
						}
					}
					
					System.out.println("thread " + begin + " has finished...");
					cdl.countDown();
				}

			}).start();

		
		}
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("now all threads have finished");

		System.out.println(hm_allword_tfidf.size());
		
		System.out.println("realAllDocsNum="+realAllDocsNum.tf);
		
		
		// 2.计算tf、idf，筛选最高的30000个词
		Set<Entry<String, int2double>> es = hm_allword_tfidf.entrySet();
		Iterator<Entry<String, int2double>> it = es.iterator();
		while (it.hasNext()) {
			Entry<String, int2double> et = it.next();
			int2double i2d = et.getValue();
	
//			i2d.value = Math.pow(i2d.tf,0.33)
//					* (Math.log(realAllDocsNum.tf / (double) (i2d.docfreq + 1)))
//					* (i2d.docfreq/(double)i2d.tf);
			
			i2d.value = Math.pow(i2d.tf,0.25) * (Math.log(realAllDocsNum.tf / (double) (i2d.docfreq + 1)));
	
		}
		// 2.1 排序并筛选最大的若干词, 形成dict文档
		ArrayList<Entry<String, int2double>> dictLists = new ArrayList<Entry<String, int2double>>(
				es);
		System.out.println(dictLists.size());
		
		Collections.sort(dictLists,
				new Comparator<Entry<String, int2double>>() {

					@Override
					public int compare(Entry<String, int2double> arg0,
							Entry<String, int2double> arg1) {
						// TODO Auto-generated method stub
						if (arg0.getValue().compareTo(arg1.getValue()) > 0)
							return -1;
						return 1;
					}

				});
		
		System.out.println(dictLists.size());
		
		
		StringBuffer sbTmp = new StringBuffer();
		StringBuffer sbTmp1 = new StringBuffer();
		int num = 0;
		commenFuncs.writeResult("corpus/", "dict_topicmodel.all",
				sbTmp.toString(), "utf-8", false, null);
		commenFuncs.writeResult("corpus/", "dict_topicmodel",
				sbTmp1.toString(), "utf-8", false, null);
		for(int m=0;m<dictLists.size();m++)
		{
			num++;
			Entry<String, int2double> esi = dictLists.get(m);

			//过滤tf/idf > 10的word
			if(esi.getValue().tf > (esi.getValue().docfreq*10))
				continue;
			
			sbTmp1.append(esi.getKey()).append("\n");
			sbTmp.append(esi.getKey()).append("\t")
					.append(esi.getValue().value).append("\t")
					.append(esi.getValue().tf).append("\t")
					.append(esi.getValue().docfreq).append("\n");
			if (sbTmp.length() > 8096) {
				commenFuncs.writeResult("corpus/", "dict_topicmodel.all",
						sbTmp.toString(), "utf-8", true, null);
				
				if(num < wordNumAssign)
					commenFuncs.writeResult("corpus/", "dict_topicmodel",
							sbTmp1.toString(), "utf-8", true, null);
				
				sbTmp.delete(0, sbTmp.length());
				sbTmp1.delete(0, sbTmp1.length());
			}
		}
		
		commenFuncs.writeResult("corpus/", "dict_topicmodel.all",
				sbTmp.toString(), "utf-8", true, null);
		if(num < wordNumAssign)
			commenFuncs.writeResult("corpus/", "dict_topicmodel",
					sbTmp1.toString(), "utf-8", true, null);
	}
	
	
	/*
	 * 1.遍历保存下的每个文档doc，根据dict，统计形成doc_word_cnt文件；
	 */
	public static void job3(String path, int wordNumAssignIn){
		//1.读入dict
		//2.遍历每一个分词缓存文件，记录文件id入doc_id文件,同时统计词的tf信息，最后写入doc_word_cnt文件
		File f = new File(path);
		File[] files = f.listFiles();

		commenFuncs.writeResult("corpus/", "doc_word_cnt","", "utf-8", false, null);
		commenFuncs.writeResult("corpus/", "doc_id", "", "utf-8", false, null);
			
		// 统计出现词的tf、idf信息
		int allDocsNum = files.length;
		int threadsNum = 1;
		final CountDownLatch cdl = new CountDownLatch(8);
		for(int i = 0; i < threadsNum; ++i)			
		{
			final int begin = (allDocsNum/threadsNum)*i;
			final int end = allDocsNum < (begin+allDocsNum/threadsNum)?allDocsNum:(begin+allDocsNum/threadsNum);
			final File[] somefiles = new File[end-begin];
			for(int k=begin;k<end;k++)
				somefiles[k-begin] = files[k];	
			
			final int wordNumAssign = wordNumAssignIn;
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					HashMap<String,Integer> hm_dict = new HashMap<String,Integer>();
					FileUtil dictFile = new FileUtil();
					dictFile.Initialize("corpus/dict_topicmodel", "UTF-8");
					String line = "";
					int word_id = 0;
					while((line=dictFile.ReadLine())!=null){
						hm_dict.put(line.trim(), word_id);
						word_id++;
						if(word_id >= wordNumAssign)
							break;
					}
					if(word_id<=0)
						return;
					int docid = 0;
					for (int j = 0; j < (end-begin); j++) {
						if(j%100 == 0)
							System.out.println("thread " + begin + " has finished " + j);
					
						HashMap<Integer, Integer> hm_wordid_tf = new HashMap<Integer, Integer>();
						FileUtil docFile = new FileUtil();
						docFile.Initialize(somefiles[j].getAbsolutePath(), "UTF-8");
						int fileNum = 0;
						while((line=docFile.ReadLine())!=null){
							line = line.trim();
							if(line.isEmpty())
								continue;
							//此处需要分割成单个小文件，items是以"--------"隔开；
							if(line.indexOf("--------")>=0){
								fileNum++;
								//写入doc_id及doc_word_cnt文件
								docid++;
								if(hm_wordid_tf.size() > 0){
									Iterator<Entry<Integer, Integer>> it = hm_wordid_tf.entrySet().iterator();
									StringBuffer sbTmp = new StringBuffer();
									while(it.hasNext()){
										Entry<Integer, Integer> et = it.next();
										int wordid = et.getKey();
										int tf = et.getValue();
										sbTmp.append(docid).append("\t").append(wordid).append("\t").append(tf).append("\r\n");
									}
									
									commenFuncs.writeResult("corpus/", "doc_word_cnt",sbTmp.toString(), "utf-8", true, null);
									//doc_id这个文件已经没有什么用了
									commenFuncs.writeResult("corpus/", "doc_id", somefiles[j].getName()+"_"+fileNum+"\r\n", "utf-8", true, null);
								}
								hm_wordid_tf.clear();
								continue;
							}
							
							String[] secs = line.split("\\s");
							for(int j1=0;j1<secs.length;j1++){
								String word = secs[j1];
								if(!hm_dict.containsKey(word))
									continue;
								int wordid = hm_dict.get(word);
								
								Integer tf = hm_wordid_tf.get(wordid);
								if(tf == null)
									hm_wordid_tf.put(wordid, 1);
								else
									hm_wordid_tf.put(wordid, tf+1);
							
							}
						}
		
					}
				}
			}).start();

			System.out.println("thread " + begin + " has finished...");
		}
		
		try {
			cdl.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("now all threads have finished");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		double x1 = Math.pow(100000,0.25);
//		double x2 = Math.pow(8000,0.25);
//		System.out.println(x1/x2);
		
//		String word = "span";
//		//过滤html tag
//		if(word.matches("^.*?(width|tr|td|height|span).*?$"))
//			System.out.println(1);
//		//过滤"12日\3月\2013年"
//		if(word.matches("\\d{1,4}[万亿多]?[日月年元个套户台亩度名][左]?[右]?"))
//			System.out.println(1);
//		//过滤"3.2亿英镑 490.35亿元 1.5万套"
//		if(word.matches("^.*?\\d{1,10}.\\d{1,10}.*?$"))
//			System.out.println(2);
//		//过滤"48小时"
//		if(word.matches("\\d{1,2}小时"))
//			System.out.println(3);
//		//过滤"1.4 1-4"
//		if(word.matches("\\d{1,2}[._-]\\d{1,2}"))
//			System.out.println(4);
		
		
		
		// 1.遍历每个文档doc，分词并存储分词结果入硬盘缓存，统计生成每个词的tf、idf信息；同时生成doc_id；
		// 2.特征选择，挑选最好的60000个词，生成dict文件；
		// 3.遍历每个文档doc，统计dict中出现词的cnt，形成doc_word_cnt文件；
		String path = args[0];//"items_cache/";
		//job1(Integer.valueOf(args[0]));
		System.out.println(args[0]+" "+args[1]);
		int wordNumAssign = Integer.valueOf(args[1]);
		//job1(path);
		//job2(path,wordNumAssign);
		job3(path,wordNumAssign);

	}

}
