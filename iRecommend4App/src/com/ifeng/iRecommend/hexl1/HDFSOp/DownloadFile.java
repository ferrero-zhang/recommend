package com.ifeng.iRecommend.hexl1.HDFSOp;

import java.io.IOException;

public class DownloadFile {
	HDFSOperation hdfs;
	 public DownloadFile(){
		try {
			hdfs=new HDFSOperation();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	 public void download(String serverPath, String localPath){
			try {
				hdfs.downFile(serverPath, localPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	 }
public static void main(String[] args){
	DownloadFile ob = new DownloadFile();
	ob.download(args[0], args[1]);
}
}
