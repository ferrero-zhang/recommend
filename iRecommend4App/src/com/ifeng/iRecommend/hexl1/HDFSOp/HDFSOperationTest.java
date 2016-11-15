package com.ifeng.iRecommend.hexl1.HDFSOp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class HDFSOperationTest
{
	HDFSOperation hdfs;
	@Before
	public void setUp() throws Exception
	{
		hdfs=new HDFSOperation(); 
	}

	@Ignore
	public void testHDFSOperation()
	{
//		fail("Not yet implemented");
	}

	@Ignore
	public void testCreateFile() throws IOException
	{
//		hdfs.CreateFile("/projects/zhineng/test.txt");
	}

	@Ignore
	public void testCreateDirectory() throws IOException
	{
		hdfs.CreateDirectory("/dataTeam/featureEngineering/configFile/");
		//hdfs.listHDFS("/projects/zhineng/");
	}

	@Test
	public void testCopyFile() throws IOException
	{
		//hdfs.CopyFile("C:\\Users\\Hexl1\\Downloads\\script\\android_2014-08-14", "/projects/zhineng/publishId/android/android_2014-08-14");
		//hdfs.CopyFile("C:\\Users\\Hexl1\\Downloads\\script\\app422uid_2014-08-14", "/projects/zhineng/publishId/app422/app422uid_2014-08-14");
		//hdfs.CopyFile("C:\\Users\\Hexl1\\Downloads\\script\\app423uid_2014-08-14", "/projects/zhineng/publishId/app423/app423uid_2014-08-14");
	//	hdfs.CopyFile("C:\\Users\\Hexl1\\Downloads\\script\\iosuid_2014-12-22", "/projects/zhineng/publishId/ios/iosuid_2014-12-22");
		hdfs.CopyFile("C:\\Users\\hexl1\\Desktop\\cmpp数据采集程序\\ikv版\\CMPPItemToIKVAndRedis.jar", "/dataTeam/featureEngineering/CMPPItemToIKVAndRedis.jar");
//		hdfs.CopyFile("E:\\backup\\featureEngineer\\config\\indexFile\\termMap.txt","/dataTeam/featureEngineering/configFile/termMap.txt");
//		hdfs.listHDFS("/dataTeam/featureEngineering/configFile");
	}

	@Ignore
	public void testRenameFile() throws IOException
	{
//		hdfs.RenameFile("/flume/english.txt","/flume/temp.bat");
	}

	@Ignore
	public void testDeleteFile() throws IOException
	{
		hdfs.DeleteFile("/dataTeam/featureEngineering/configFile/test2.txt",true);
//		hdfs.DeleteFile("/projects/zhineng/tfidf/tfidf_week",false);
//		for(int i = 0; i < 10; i++)
//		{
//			String path = "/projects/zhineng/publishId/app424/app424_2014-09-0"+String.valueOf(i);
//			System.out.println(path);
//			hdfs.DeleteFile(path,true);
//		}
		
		hdfs.listHDFS("/dataTeam/featureEngineering/configFile/");
	}

	@Ignore
	public void testFileLocation() throws IOException
	{
//		hdfs.FileLocation("/avi_video/hadoop2.avi");
	}

	@Ignore
	public void testReadHdfs() throws IOException
	{
		hdfs.ReadHdfs("/english.txt");
	}
	
	@Ignore
	public void testDownFile() throws IOException
	{
//		hdfs.downFile("/projects/zhineng/tfidf/tfidf_week", "C:\\data\\tfidf_week");
//		hdfs.downFile("/projects/zhineng/publishId/android/android_2015-08-16", "C:\\data\\android_2015-08-16");
//		hdfs.downFile("/lines", "D:\\all_urls_xml\\ss");
		hdfs.downFile("/dataTeam/featureEngineering/configFile/test","D:\\data\\test.txt");
	}
	@Ignore
	public void testDownHDFSFileList(){
		try {
			hdfs.DownHDFSFileList("/dataTeam/featureEngineering/configFile", "D:\\");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Ignore
	public void testAppendFile() throws IOException
	{
//		hdfs.appendFile("D://lines", "hdfs://tongjihadoop165:8020/test.txt");
	}

	@Ignore
	public void testGetModifyTime()
	{
//		fail("Not yet implemented");
	}

	@Ignore
	public void testMergeFile()
	{
//		fail("Not yet implemented");
	}

	@Ignore
//	@Ignore
	public void testListHDFS() throws IOException
	{
		hdfs.listHDFS("/dataTeam/");//显示hdfs根目录(指定目录)
	}

	@Ignore
	public void testTestFileBlockLocation() throws Exception
	{
//		System.out.println(hdfs.testFileBlockLocation("/urls.xml"));
	}

	@Ignore
	public void testTestGetHostName()
	{
		fail("Not yet implemented");
	}

	@Ignore
	public void testreadTfidfName() throws IOException, ParseException
	{
		String sourceFileName = "E:/backup/个性化推荐系统/tfidf/tfidf2014-08-11";
		hdfs.readTfidfName(sourceFileName);
		hdfs.appendFile("E:\\backup\\个性化推荐系统\\tfidf\\tempfile", "hdfs://tongjihadoop120:8020/projects/zhineng/tfidf/tfidf_week");
		File file = new File("E:\\backup\\个性化推荐系统\\tfidf\\tempfile");  
	    // 路径为文件且不为空则进行删除  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	    }  
	    hdfs.listHDFS("/projects/zhineng/");
	    
	}
}
//private static List<String> urls=new  ArrayList<String>();

//public static void main(String[] args) throws Exception { 
	//
//	File dir = new File(args[0]);//localfile
//	File file[] = dir.listFiles();
//	
//	
//	
	//
	//
	//
	//
	//
	//
//	
	//hdfs.testGetHostName();
	//有可能会碰到权限问题，需要将HDFS上的文件的权限改成可读写的

//}