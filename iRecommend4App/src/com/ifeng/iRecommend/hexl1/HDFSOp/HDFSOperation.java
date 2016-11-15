package com.ifeng.iRecommend.hexl1.HDFSOp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.io.IOUtils;

import com.ifeng.commen.Utils.LoadConfig;
/**
 * 
 * <PRE>
 * 作用 : HDFS操作方法集合
 *   
 * 使用 : 创建文件，创建路径，上传文件，下载文件，删除文件，重命名文件，追加文件，按行读，获取文件在集群的位置，查看修改时间
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0        2014-04-02         Hxl          create
 * -----------------------------------------------------------------------------
 * 
 * </PRE>
 */
public class HDFSOperation
{
		private static final Log logger = LogFactory.getLog(HDFSOperation.class);
		private Configuration conf;
		private FileSystem hdfs;
		private static BufferedWriter bw = null;
		private static String hdfsip = "hdfs://10.90.4.195:8020";//LoadConfig.lookUpValueByKey("HDFSIP");
		public HDFSOperation() throws IOException {
			conf = new Configuration();
			conf.set("fs.defaultFS", hdfsip);// 指定hdfs
			//hdfs://10.32.21.131:8020/    hdfs://10.32.21.120:8020/   hdfs://10.32.21.111:8020/
			conf.set("hadoop.job.user", "hdfs");
			conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
			hdfs = FileSystem.get(conf);
			bw = new BufferedWriter(new FileWriter("d:\\data\\urlss.txt"));
		}

		/**
		 * 在hdfs上创建文件
		 */
		public void CreateFile(String path) throws IOException {
			byte[] buff = "hello".getBytes();// 内容
			Path dfs = new Path(path);
			FSDataOutputStream outputStream = hdfs.create(dfs);
			outputStream.write(buff, 0, buff.length);
			outputStream.flush(); // 刷新此输出流并强制写出所有缓冲的输出字节。
			outputStream.close(); // 关闭此输出流并释放与此流有关的所有系统资源。
		}

		/**
		 * 在hdfs上创建文件路径
		 */
		public void CreateDirectory(String dir) throws IOException {
			Path dfs = new Path(dir);
			if (hdfs.exists(dfs))
				System.out.println(dir + " has exists");
			else {
				if (hdfs.mkdirs(dfs))
					System.out.println(dir + " created.");
			}
		}

		/**
		 * 上传本地文件到HDFS 可以是单个文件，可以是个文件夹，但是得是完整路径
		 */
		public void CopyFile(String srcStr, String dstStr) throws IOException {
			Path src = new Path(srcStr);
			Path dst = new Path(dstStr);// "/"
			try {
				logger.info("start copy file:" + src);
				hdfs.copyFromLocalFile(src, dst);
				logger.info("the file:" + src + " has copy to:" + dst);
			} catch (Exception ex) {
				logger.info("copy has Exception" + ex);
			}
		}

		/**
		 * 重命名文件 其中srcStr和dstStr均为文件的完整路径
		 */
		public void RenameFile(String srcStr, String dstStr) throws IOException {
			Path src = new Path(srcStr);
			Path dst = new Path(dstStr);
			if (hdfs.rename(src, dst))
				System.out.println("rename success");
			else
				System.out.println("rename fail");
		}

		/**
		 * 删除文件
		 */
		public void DeleteFile(String file, boolean del) throws IOException {
			Path src = new Path(file);
			if (hdfs.exists(src)) {
				if (hdfs.delete(src, del))
					System.out.println(file + " delete success");// 不递归删除
			} else
				System.out.println("The file or directory " + file + " not exist.");
		}

		/**
		 * 查看某个文件的HDFS集群位置 路径是文件的完整hdfs路径
		 */
		public void FileLocation(String pathStr) throws IOException {
			Path path = new Path(pathStr);
			FileStatus filestatus = hdfs.getFileStatus(path);
			System.out.println("filestatus.getBlockSize():"+filestatus.getBlockSize());
			BlockLocation[] blkLocations = hdfs.getFileBlockLocations(filestatus,
					0, filestatus.getLen());
			int blockLen = blkLocations.length;
			for (int i = 0; i < blockLen; i++) {
				String[] hosts = blkLocations[i].getHosts();
				for(int n=0 ;n<hosts.length;n++){
					System.out.print(pathStr + ":block_" + i + "_location:"+ hosts[n]+".name:"+blkLocations[i].getNames()[n]);
					System.out.println(" .  topologyPath:"+blkLocations[i].getTopologyPaths()[n]+".length:"+blkLocations[i].getLength()+", offset:"+blkLocations[i].getOffset());
				}
			}
		}

		/**
		 * 从hdfs上一行一行的读取文件
		 */
		public void ReadHdfs(String hdfsPath) throws IOException {
			Path path = new Path(hdfsPath);
			FSDataInputStream fsr = hdfs.open(path);
			BufferedReader bis = new BufferedReader(new InputStreamReader(fsr,
					"GBK"));
			String line = bis.readLine().toString();
			while (line != null) {
				System.out.println(line);
				line = bis.readLine();
			}
		}

		/**
		 * 下载文件(注意：俩参数需对应到文件名)
		 * 
		 * @param hdfsPath
		 * @param localPath
		 * @exception IOException
		 */
		public void downFile(String hdfsPath, String localPath) throws IOException {
			InputStream in = hdfs.open(new Path(hdfsPath));
			OutputStream out = new FileOutputStream(localPath);
			IOUtils.copyBytes(in, out, conf);
		}

		/**
		 * 将本地文件追加到hdfs文件末尾
		 * 
		 * @param localFile
		 * @param hdfsPath
		 * @throws IOException
		 */
		public void appendFile(String localFile, String hdfsPath)
				throws IOException {
			InputStream in = new FileInputStream(localFile);
			OutputStream out = hdfs.append(new Path(hdfsPath));
			IOUtils.copyBytes(in, out, conf);
		}

		/**
		 * 查看指定目录下文件的最后修改时间
		 * 
		 * @param path
		 * @throws Exception
		 */
		public void getModifyTime(String path) throws Exception {
			Path dst = new Path(path);
			FileStatus files[] = hdfs.listStatus(dst);
			for (FileStatus file : files) {
				System.out.println(file.getPath() + "的最后修改时间是："
						+ file.getModificationTime());
			}
		}

		/**
		 * 将本地文件文件夹下的文件合并为一个hdfs中的文件
		 * 
		 * @param localFile
		 * @param hdfsPath
		 * @throws IOException
		 */
		public void MergeFile(String localFile, String hdfsPath) throws IOException {
			FileSystem local = FileSystem.getLocal(conf);
			Path inputDir = new Path(localFile);
			Path hdfsFile = new Path(hdfsPath);
			try {
				FileStatus[] inputFiles = local.listStatus(inputDir);
				FSDataOutputStream out = hdfs.create(hdfsFile);
				for (int i = 0; i < inputFiles.length; i++) {
					System.out.println(inputFiles[i].getPath().getName());
					FSDataInputStream in = local.open(inputFiles[i].getPath());
					byte buffer[] = new byte[256];
					int bytesRead = 0;
					while ((bytesRead = in.read(buffer)) >= 0) {
						out.write(buffer, 0, bytesRead);
					}
					in.close();
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 列出指定目录下文件
		 */
		public void listHDFS(String listPath) throws IOException {
			Path dst = new Path(listPath);// "/"
			if (hdfs.isFile(dst)) {
				bw.write(listPath);
				bw.newLine();
				bw.flush();
			} else if(hdfs.isDirectory(dst)){
				FileStatus files[] = hdfs.listStatus(dst);
				for (FileStatus file : files) {
//					String p = file.getPath().toString().substring(file.getPath().toString().indexOf("8020")+25); 
					listHDFS(file.getPath().toString());
					System.out.println(file.getPath());
				}
			}
		}
		/**
		 * 列出指定目录下文件
		 */
		public void DownHDFSFileList(String listPath, String localPath) throws IOException {
			Path dst = new Path(listPath);// "/"
//			List<String> fileList = new ArrayList<String>();
			if (hdfs.isFile(dst)) {
				downFile(listPath, localPath+listPath.split("/")[listPath.split("/").length - 1]);
			} else if(hdfs.isDirectory(dst)){
				FileStatus files[] = hdfs.listStatus(dst);
				for (FileStatus file : files) {
//					String p = file.getPath().toString().substring(file.getPath().toString().indexOf("8020")+25); 
					listHDFS(file.getPath().toString());
//					fileList.add(file.getPath().toString());
					System.out.println(file.getPath().toString());
					System.out.println(localPath+file.getPath().toString().split("/")[file.getPath().toString().split("/").length - 1]);
					downFile(file.getPath().toString(), localPath+file.getPath().toString().split("/")[file.getPath().toString().split("/").length - 1]);
//					System.out.println(file.getPath());
				}
			}
		}
		//查看某个文件在HDFS集群的位置   
	    public String testFileBlockLocation(String listPath) throws Exception{
	    		String host = "";
	    		String IP = "";
	            Path dst = new Path(listPath);                
	            FileStatus fileStatus =  hdfs.getFileStatus(dst);  
	            BlockLocation[] blockLocations =hdfs.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());  
	            Random rand = new Random();
	            int num = Math.abs(rand.nextInt())%3;
	            for(int i=0;i<blockLocations.length;i++){            	            	
	            	System.out.println(Arrays.toString(blockLocations[i].getHosts()));
	            	System.out.println(Arrays.toString(blockLocations[i].getNames()));
	            	System.out.println(blockLocations[i].getHosts()[0]);
	            	host = blockLocations[i].getHosts()[num];
	            	IP = blockLocations[i].getNames()[num];
	            }
	            return IP.substring(0,IP.lastIndexOf(":"));
	    }  
	      
	    //获取HDFS集群上所有节点名称   (缺权限)
	    public void testGetHostName() throws Exception{
	    	  FileSystem fs = FileSystem.get(conf);
	    	  DistributedFileSystem hdfs = (DistributedFileSystem) fs;
	    	  DatanodeInfo[] dataNodeStats = hdfs.getDataNodeStats();
	    	  String[] names = new String[dataNodeStats.length];
	    	  for (int i = 0; i < dataNodeStats.length; i++) {
	    	      names[i] = dataNodeStats[i].getHostName();
	    	  } 
	    }  
	    public void readTfidfName(String sourceFileName) throws IOException, ParseException
	    {

	    	String tempFileName = "E:/backup/个性化推荐系统/tfidf/tempfile";
	    	List<String> list = new ArrayList<String>();
	    	FileWriter fileWriter = null;
			//写入文件流
			try
			{
				fileWriter = new FileWriter(tempFileName, true);
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//读取文件流
			FileReader reader = null;
			//读取原ID文件
			try {
				reader = new FileReader(sourceFileName);//读取url文件
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  BufferedReader br = new BufferedReader(reader);
			  String s1 = null;
			  s1 = br.readLine();
			  String endDate = sourceFileName.substring(29);
			  endDate = endDate.replaceAll("-", "");
			  
			  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			  Date date =sdf.parse(endDate);
			  long startTime = date.getTime();
			  startTime = startTime - 518400000;
			  
			  String startDate = sdf.format(new Date(startTime));
			  //System.out.println(startDate);
			  String usrCount = s1.split(" ")[1];
			  String firstLine = startDate+" "+endDate+" "+usrCount;
			  list.add(firstLine);
			  
			  int flag = 0;
				while((s1 = br.readLine()) != null) 
				{		
					if(s1.contains("-------"))
					{
						flag++;
						if(flag == 2)
							break;
						continue;
					}

					list.add(s1);		
				}
				list.add("----------");
				br.close();
				reader.close();
				for(int i = 0; i< list.size(); i++)
				{
//					if(i<list.size()-1)
						fileWriter.write("\n"+list.get(i));
//					else
//						fileWriter.write(list.get(i));
				}
				
				fileWriter.close();
				
	    }

}
