package com.ifeng.iRecommend.featureEngineering;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import org.apache.log4j.Logger;

public class InitialLoadFile {
	static Logger LOG = Logger.getLogger(InitialLoadFile.class);
	/**
	 * 加载原创集列表
	 * 
	 * @throws IOException
	 */
	public static HashSet<String> readOriginalList(String originalPath) {
		HashSet<String> originalSet = new HashSet<String>();
		if(originalPath == null || originalPath.equals(""))
			return originalSet;
		FileReader fr = null;
		try {
			fr = new FileReader(originalPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String s = null;
		try {
			while ((s = br.readLine()) != null) {
				originalSet.add(s.trim());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (originalSet != null)
			LOG.info("BlackList size is " + originalSet.size());
		else
			LOG.info("BlackList is null.");
		return originalSet;
	}
	/**
	 * 加载停用词表
	 * 用于过滤feature中的无效词
	 * @param stopwordPath
	 * @return
	 */
	public static HashSet<String> readStopword(String stopwordPath) {
		HashSet<String> stopwordSet = new HashSet<String>();
		if(stopwordPath == null || stopwordPath.equals(""))
			return stopwordSet;
		FileReader fr = null;
		try {
			fr = new FileReader(stopwordPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String s = null;
		try {
			while ((s = br.readLine()) != null) {
				stopwordSet.add(s);
			}
			LOG.info("[STOPWORD]Read stopword " + stopwordSet.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stopwordSet;
	}
	/**
	 * 载入一点资讯白名单
	 */
	public static HashSet<String> yidianBlackReader(String yidianBlackPath) {
		HashSet<String> yidianBlackSet = new HashSet<String>();
		FileReader fr = null;
		try {
			fr = new FileReader(yidianBlackPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String s = null;
		try {
			while ((s = br.readLine()) != null) {
				yidianBlackSet.add(s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.info("[INFO]WhiteList load finished. The size of yidian black is " + yidianBlackSet.size());
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return yidianBlackSet;
	}
}
