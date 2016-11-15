package com.ifeng.iRecommend.topicmodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
/**
 * 多线程读取topic矩阵
 * <PRE>
 * 作用 : 
 *   
 * 使用 : 
 *   
 * 示例 :每个主题文件第一行是： 文件数   主题数  
 *   
 * 注意 :
 *   一个主题文件一个thread读取；
 *   多thread写入同一个topic matrix，以line第一个单元做key，写入matrix[key][*]；
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2012-10-24        mayk          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public final class TopicMatrixReader extends Thread {

	private static TopicMatrix matrix = null;
	private BufferedReader reader;

	private TopicMatrixReader(BufferedReader rd) {
		this.reader = rd;
	}

	public void run() {
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {

				synchronized (matrix) {
					String[] splited = line.split("\t");
					int row = Integer.parseInt(splited[0]);
					double sum = 0.0;
					for (int i = 1; i < splited.length; ++i) {
						double count = Double.parseDouble(splited[i]);
						sum += count;
						matrix.put(row, i - 1, count);
					}
					matrix.normalize(row, sum);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * 
	 */
	public static TopicMatrix ReadInDir(File dir,int sizeIn,int ntopicsIn) {
		if (!dir.isDirectory())
			return ReadInData(dir);
		int size = sizeIn;
		int ntopics = ntopicsIn;
		
		File[] files = dir.listFiles();
		List<BufferedReader> rds = new ArrayList<BufferedReader>();

		for (int i = 0; i < files.length; ++i) {
			BufferedReader rd = null;
			try {
				rd = new BufferedReader(new InputStreamReader(
						new FileInputStream(files[i]), "utf8"));
				String line = rd.readLine();
				if (line == null)
					return null;
				
				rds.add(rd);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		matrix = new TopicMatrix(size, ntopics);
		List<Thread> runningThread = new ArrayList<Thread>();

		for (int i = 0; i < files.length; ++i) {
			Thread thread = new TopicMatrixReader(rds.get(i));
			runningThread.add(thread);
			thread.start();
		}
		for (Thread thread : runningThread) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return matrix;
	}

	public static TopicMatrix ReadInData(File file) {
		// TODO Auto-generated method stub
		return null;
	}

}
