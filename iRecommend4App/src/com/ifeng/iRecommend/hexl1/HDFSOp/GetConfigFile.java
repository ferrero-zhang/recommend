package com.ifeng.iRecommend.hexl1.HDFSOp;

import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.junit.Before;

public class GetConfigFile {
	HDFSOperation hdfs;
	GetConfigFile() throws Exception
	{
		hdfs = new HDFSOperation(); 
	}
	public void updateLocalFile(String listPath, String localPath){
		try {
			hdfs.DownHDFSFileList(listPath, localPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		GetConfigFile getFile = null;
		try {
			 getFile = new GetConfigFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getFile.updateLocalFile(args[0], args[1]);
	}
}
