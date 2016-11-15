package com.ifeng.iRecommend.hexl1.HDFSOp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class analyseClientData
{
	private ArrayList<String> filelist = new ArrayList<String>();
	private Map<String,String> usridMap = new HashMap<String,String>();
	
	public String getTimeString()
	{
		String time = String.valueOf(System.currentTimeMillis()-1*86400000);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
		String sourceFileName = sdf.format(new Date(Long.parseLong(time)));  
		return sourceFileName;
	}
	public void refreshFileList(String strPath)
	{
		File dir = new File(strPath);
		File[] files = dir.listFiles();

		if (files == null)
			return;
		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isDirectory())
			{
				refreshFileList(files[i].getAbsolutePath());
			}
			else
			{
				String strFileName = files[i].getAbsolutePath().toLowerCase();
				//System.out.println("---" + strFileName);
				filelist.add(strFileName);
			}
		}
	}
	public void readFile(String fileName) throws IOException
	{
		//读取文件流
				FileReader reader = null;
				//读取原ID文件
				try {
					reader = new FileReader(fileName);//读取url文件
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				  BufferedReader br = new BufferedReader(reader);
				  String s1 = null;
					while((s1 = br.readLine()) != null) 
					{
						String[] tempdata = s1.split("\t");
						try{
							if(tempdata[5].contains("android")&&tempdata[6].contains("4.3.2")&&tempdata[11].contains("1"))//&&tempdata[6].contains("4.2.3")
							{
								if(usridMap.containsKey(tempdata[0]))
								{
									int activeCount = Integer.valueOf(usridMap.get(tempdata[0]));
									activeCount = activeCount + 1;
									usridMap.put(tempdata[0], String.valueOf(activeCount));
								}
								else
								{
									usridMap.put(tempdata[0], "1");
								}
							}
						}
						catch(java.lang.ArrayIndexOutOfBoundsException e)
						{
							for(String entry : tempdata)
							{
								System.out.print(entry+"\t");
							}
							continue;
						}
						
					}
					br.close();
					reader.close();
	}
	public List<String> readDayFile(String fileName) throws IOException
	{
		List<String> list = new ArrayList<String>();
		//读取文件流
				FileReader reader = null;
				//读取原ID文件
				try {
					reader = new FileReader(fileName);//读取url文件
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				  BufferedReader br = new BufferedReader(reader);
				  String s1 = null;
					while((s1 = br.readLine()) != null) 
					{
						String[] tempdata = s1.split("\t");
						if(tempdata[5].contains("android")&&tempdata[6].contains("4.3.2")&&tempdata[11].contains("1"))//
						{
							list.add(tempdata[0]);
						}
					}
					br.close();
					reader.close();
					return list;
	}
	public void process(ArrayList<String> filelist) throws IOException
	{
		for(String entry : filelist)
		{
			System.out.println(entry);
			readFile(entry);
		}
	}
	public void writeDayFile(List<String> list, String resultFileName) throws IOException
	{
		FileWriter fileWriter = null;
		//写入文件流
		try
		{
			fileWriter = new FileWriter(resultFileName, true);
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(String entry : list)
		{
			fileWriter.write(entry+"\t"+"1\n");
		}
		fileWriter.close();
	}
	public void writeFile(Map<String,String> usridMap, String resultFileName) throws IOException
	{
		FileWriter fileWriter = null;
		//写入文件流
		try
		{
			fileWriter = new FileWriter(resultFileName, true);
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(Entry<String,String> entry : usridMap.entrySet())
		{
			fileWriter.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		fileWriter.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		/*<------------------------------分析某版本累积用户id及统计活跃天数并存储在本地------------------------------>*/
		/*long a = System.currentTimeMillis();
		analyseClientData obj = new analyseClientData();
		String resultFileName = "/data/app424uid";
		String strPath = "/data/logs/client_uv/";
		obj.refreshFileList(strPath);
		obj.process(obj.filelist);
		obj.writeFile(obj.usridMap, resultFileName);
		System.out.println(System.currentTimeMillis() - a);*/
		/*<--------------------------------分析某版本一日用户id及计活跃天数1并存储在HDFS---------------------------->*/
		long a = System.currentTimeMillis();
		analyseClientData obj = new analyseClientData();
		String timeString = obj.getTimeString();
		String sourceFileName = "/data/logs/client_uv/client_uv_"+timeString;
		String resultFileName = "/data/logs/client_uv/tempdata/app432uid_"+timeString;///data/logs/client_uv/analuseClientData
		List<String> list = obj.readDayFile(sourceFileName);
		obj.writeDayFile(list,resultFileName);
		HDFSOperation hdfs = new HDFSOperation(); 
		hdfs.CopyFile(resultFileName, "/projects/zhineng/publishId/app432/app432uid_"+timeString);
		System.out.println(System.currentTimeMillis() - a);
		/*<----------------------------------分析某版本一日用户id及计活跃天数1并存储在本地-------------------------->*/
		/*long a = System.currentTimeMillis();
		analyseClientData obj = new analyseClientData();
		String sourceFileName = "C:\\Users\\Hexl1\\Downloads\\script\\0812";
		String resultFileName = "/data/app429uid_2014-08-12";
		List<String> list = obj.readDayFile(sourceFileName);
		obj.writeDayFile(list,resultFileName);
		System.out.println(System.currentTimeMillis() - a);*/
	}
}
