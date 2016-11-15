package com.ifeng.iRecommend.hexl1.HDFSOp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class UploadFileDir {
	HDFSOperation hdfs;
	ArrayList<String> filelist = new ArrayList<String>();
	public UploadFileDir() throws Exception
	{
		hdfs=new HDFSOperation(); 
	}
	
	/**
	 * 遍历文件夹 获取全部本地文件名
	 * @param localPath
	 */
	public void refreshFileList(String localPath)
	{
		File dir = new File(localPath);
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
				filelist.add(files[i].getName());
			}
		}
	}
	
	public void upload(){
		for(int i = 0; i < filelist.size(); i++)
		{
			try {
				hdfs.CopyFile("E:\\backup\\featureEngineer\\config\\indexFile\\"+filelist.get(i),"/dataTeam/featureEngineering/configFile/"+filelist.get(i));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			hdfs.listHDFS("/dataTeam/featureEngineering/configFile");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception{
		UploadFileDir ob = new UploadFileDir();
		ob.refreshFileList("E:\\backup\\featureEngineer\\config\\indexFile");
		ob.upload();
	}
}
