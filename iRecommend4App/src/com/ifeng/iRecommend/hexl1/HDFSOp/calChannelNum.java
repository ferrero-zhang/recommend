package com.ifeng.iRecommend.hexl1.HDFSOp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class calChannelNum
{
	private static final Log LOG = LogFactory.getLog("calChannelNum");
	private Map<String,Integer[]> channelMap = new HashMap<String,Integer[]>();
	private List<String[]> list1 = new ArrayList<String[]>();
	private List<String[]> list2 = new ArrayList<String[]>();
	private List<String[]> list3 = new ArrayList<String[]>();
	private List<String[]> list4 = new ArrayList<String[]>();
	private List<String[]> list5 = new ArrayList<String[]>();
	public void readFile(String filepath) throws IOException
	{
		// 读取文件流
		FileReader reader = null;
		try
		{
			reader = new FileReader(filepath);// 读取url文件
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("[ERROR] Read file failed.");
		}
		BufferedReader br = new BufferedReader(reader);
		String s1 = null;
		// String loopID = null;
		int i = 0;
		String channel = "";
		while ((s1 = br.readLine()) != null)
		{
			
			if(i % 2 == 0)
			{
				channel = s1.split(",")[0].trim();
				//System.out.println(s1);
			}
			else
			{
				Integer[] addData = new Integer[2];
				addData[0] = Integer.valueOf(s1.split(" ")[0].trim());
				addData[1] = Integer.valueOf(s1.split(" ")[1].trim());
				channelMap.put(channel, addData);
				//System.out.println(tempdata[0]+" "+tempdata[1]+" "+tempdata[2]);
			}
			i++;
		}
		br.close();
		reader.close();
	}
	
	Comparator<String[]> comparator = new Comparator<String[]>(){
		public int compare(String[] s1, String[] s2)
		{
			return s1[0].compareTo(s2[0]);
		}
	};
	private void order()
	{
		for(Entry<String,Integer[]> entry : channelMap.entrySet())
		{
			String[] tempdata = new String[3];
			if(entry.getKey().split("-").length == 1)
			{
				tempdata[0] = entry.getKey();
				tempdata[1] = String.valueOf(entry.getValue()[0]);
				tempdata[2] = String.valueOf(entry.getValue()[1]);
				list1.add(tempdata);
			}
			else if(entry.getKey().split("-").length == 2)
			{
				tempdata[0] = entry.getKey();
				tempdata[1] = String.valueOf(entry.getValue()[0]);
				tempdata[2] = String.valueOf(entry.getValue()[1]);
				list2.add(tempdata);
			}
			else if(entry.getKey().split("-").length == 3)
			{
				tempdata[0] = entry.getKey();
				tempdata[1] = String.valueOf(entry.getValue()[0]);
				tempdata[2] = String.valueOf(entry.getValue()[1]);
				list3.add(tempdata);
			}
			else if(entry.getKey().split("-").length == 4)
			{
				tempdata[0] = entry.getKey();
				tempdata[1] = String.valueOf(entry.getValue()[0]);
				tempdata[2] = String.valueOf(entry.getValue()[1]);
				list4.add(tempdata);
			}
			else if(entry.getKey().split("-").length == 5)
			{
				tempdata[0] = entry.getKey();
				tempdata[1] = String.valueOf(entry.getValue()[0]);
				tempdata[2] = String.valueOf(entry.getValue()[1]);
				list5.add(tempdata);
			}
		}
		Collections.sort(list1,comparator);
		Collections.sort(list2,comparator);
		Collections.sort(list3,comparator);
		Collections.sort(list4,comparator);
		Collections.sort(list5,comparator);
	}

	public void writeFile(String filepath) throws IOException
	{
		order();
		FileWriter fileWriter = null;
		//写入文件流
		try
		{
			fileWriter = new FileWriter(filepath, true);
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		fileWriter.write("------------------------ONE LEVEL-----------------------------\n");
		for(String[] entry : list1)
		{
			fileWriter.write(entry[0]+" "+entry[1]+" "+entry[2]+"\n");
		}
		fileWriter.write("------------------------TWO LEVEL-----------------------------\n");
		for(String[] entry : list2)
		{
			fileWriter.write(entry[0]+" "+entry[1]+" "+entry[2]+"\n");
		}
		fileWriter.write("------------------------THREE LEVEL-----------------------------\n");
		for(String[] entry : list3)
		{
			fileWriter.write(entry[0]+" "+entry[1]+" "+entry[2]+"\n");
		}
		fileWriter.write("------------------------FOUR LEVEL-----------------------------\n");
		for(String[] entry : list4)
		{
			fileWriter.write(entry[0]+" "+entry[1]+" "+entry[2]+"\n");
		}
		fileWriter.write("------------------------FIVE LEVEL-----------------------------\n");
		for(String[] entry : list5)
		{
			fileWriter.write(entry[0]+" "+entry[1]+" "+entry[2]+"\n");
		}
		try
		{
			fileWriter.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
//	public static void main(String args[]) throws IOException
//	{
//		calChannelNum calob = new calChannelNum();
//		calob.readFile("/data/hexl/tfidf2014-11-24chaos");
//		calob.writeFile("/data/hexl/testresult");
//	}
}
