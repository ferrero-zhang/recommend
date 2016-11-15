package com.ifeng.commen.Utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Properties;

/**
 * 
 * 文件读、写、追加方法 未加互斥，建议每个线程new一个
 * 
 * 
 * @author 刘宇
 *
 */
public class FileUtil {

	public  String OS_FileSeparator = null;

	private  void setOS_FileSeparator() {
		if (OS_FileSeparator == null) {
			Properties props = System.getProperties();
			if (props.getProperty("os.name").toLowerCase().contains("windows")) {
				OS_FileSeparator = "\\";
			} else {
				OS_FileSeparator = "/";
			}
		}
	}

	/**
	 * 
	 * 以UTF-8编码存储文件
	 * 
	 * @param Filename
	 * @param Content
	 * @throws NullPointerException
	 */
	public  void Save(String Filename, String Content)
			throws NullPointerException {
		setOS_FileSeparator();
		String add = Filename.substring(0,
				Filename.lastIndexOf(OS_FileSeparator));
		File dir = new File(add);
		if (!dir.exists())
			dir.mkdirs();
		try {
			FileOutputStream fout = new FileOutputStream(Filename);
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			DataOutputStream dout = new DataOutputStream(bout);
			dout.write(Content.getBytes("UTF-8"));
			dout.close();
			bout.close();
			fout.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * 
	 * 向文件末尾追加UTF-8编码内容
	 * 
	 * @param filename
	 * @param str
	 * @throws NullPointerException
	 */
	public  void Append2File(String filename, String str)
			throws NullPointerException {
		setOS_FileSeparator();
		try {
			File file = new File(filename);
			if (!file.exists())
				Save(filename, "");

			FileWriter writer = new FileWriter(filename, true);
			writer.write(str);
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 
	 * 以UTF-8编码读取文件
	 * 
	 * @param Filename
	 * @return
	 * @throws NullPointerException
	 * @throws IOException
	 */
	public  String Read(String Filename) throws NullPointerException {
		setOS_FileSeparator();
		try {
			StringBuffer text = new StringBuffer();
			File file = new File(Filename);
			if (file.exists()) {
				String line;
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader read = new InputStreamReader(fis, "UTF-8");
				BufferedReader br = new BufferedReader(read);
				int LineNum = 0;
				while ((line = br.readLine()) != null) {
					if (LineNum > 0) {
						text.append("\n");
					}
					text.append(line);
					LineNum++;
				}
				br.close();
				read.close();
				fis.close();
			} else {
				throw new IOException(Filename + "FileNotFound!");
			}
			return text.toString();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 以指定编码格式读取文件
	 * 
	 * @param Filename
	 * @param code
	 * @return
	 * @throws NullPointerException
	 */
	public  String Read(String Filename, String code)
			throws NullPointerException {
		setOS_FileSeparator();
		try {
			StringBuffer text = new StringBuffer();
			File file = new File(Filename);
			if (file.exists()) {
				String line = null;
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader read = new InputStreamReader(fis, code);
				BufferedReader br = new BufferedReader(read);
				int LineNum = 0;
				while ((line = br.readLine()) != null) {
					if (LineNum > 0) {
						text.append("\n");
					}
					text.append(line);
					LineNum++;
				}
				br.close();
				read.close();
				fis.close();
			}
			return text.toString();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 初始化文件载入类 注意使用完调用CloseReadLine()释放
	 * 
	 * @param Filename
	 * @throws NullPointerException
	 */
	public  boolean Initialize(String Filename, String Code) {
		setOS_FileSeparator();
		try {
			f = new File(Filename);
			if (f.exists()) {
				FileInputStream = new FileInputStream(f);
				InputStreamReader = new InputStreamReader(FileInputStream, Code);
				BufferedReader = new BufferedReader(InputStreamReader);
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 
	 * 读取一行
	 * 
	 * @return
	 */
	public  String ReadLine() {
		try {
			if (BufferedReader != null) {
				String line = BufferedReader.readLine();
				if (line == null) {
					CloseRead();
				}
				return line;
			} else {
				return null;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * 读取一个block
	 * 
	 * @return
	 */
	public  int ReadBlock(char[] cbuf) {
		int len = -1;
		try {
			if (BufferedReader != null) {
				len = BufferedReader.read(cbuf);
				if (len < 0) {
					CloseRead();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return len;
	}

	/**
	 * 
	 * 关闭输入流
	 * 
	 */
	public  void CloseRead() {
		try {
			if (BufferedReader != null)
				BufferedReader.close();
			BufferedReader = null;
			if (InputStreamReader != null)
				InputStreamReader.close();
			InputStreamReader = null;
			if (FileInputStream != null)
				FileInputStream.close();
			FileInputStream = null;
			f = null;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

/**
 * 
* @Title:refreshFileList
* @Description:将strPath路径下的所有文件路径遍历得到
* @param strPath
* @return
* @author:wuyg1
* @date:2015年12月15日
 */
	public  ArrayList<String> refreshFileList(String strPath) {
		ArrayList<String> filelList = new ArrayList<String>();
		File dir = new File(strPath);
		if (dir.isFile()) {
			filelList.add(strPath);
		}else{
			File[] files = dir.listFiles();

			if (files == null)
				return null;
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					filelList.addAll(refreshFileList(files[i].getPath()));
				} else {
					filelList.add(files[i].getPath());
				}
			}
		}
	
		return filelList;
	}
	
	/**
	 * 
	* @Title:getStreamFileWrite
	* @Description:获得文件输出流
	* @param path
	* @param charset
	* @param append
	* @return
	* @throws IOException
	* @author:wuyg1
	* @date:2015年12月15日
	 */
	public  OutputStreamWriter getStreamFileWrite(String path,String charset, boolean append) throws IOException {

			File file = new File(path);
		
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStreamWriter out = new OutputStreamWriter(
					new FileOutputStream(path, append), charset);

          return out;
	}
	

	private File f = null;
	private FileInputStream FileInputStream = null;
	private InputStreamReader InputStreamReader = null;
	private BufferedReader BufferedReader = null;
}
