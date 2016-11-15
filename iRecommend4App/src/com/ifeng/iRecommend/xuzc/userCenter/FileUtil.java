/**
 * 
 */
package com.ifeng.iRecommend.xuzc.userCenter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * <PRE>
 * 作用 : 
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015-11-24        xuzc          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class FileUtil {
	/**
	 * 输出字符串到文件
	 * @param content 需要输出的内容
	 * @param url 输出的路径
	 * @throws IOException
	 */
	public void writeToFile(String content, String url) throws IOException{
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(url, true), "utf-8"));
		out.write(content);
		out.close();
	}
	/**
	 * 输出字符串到文件
	 * @param content 需要输出的内容
	 * @param url 输出的路径
	 * @throws IOException
	 */
	public void writeToFile1(String content, String url) throws IOException{
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(url, false), "utf-8"));
		out.write(content);
		out.close();
	}
	/**
	 * 将文件的每行读取到一个set集合中去
	 * @param filepath 需要读取文件的路径
	 * @return
	 * @throws IOException
	 */
	public Set<String> fileToStringSet(String filepath) throws IOException{
		 Set<String> strSet = new HashSet<String>();
		 File file = new File(filepath);
		 BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		 String line;
		 while((line = br.readLine())!=null){
			 strSet.add(line);
		 }
		 br.close();
		 return strSet;
	}
	/**
	 * 将文件的每行读取到一个list集合中去 
	 * @param filepath 需要读取文件的路径
	 * @return
	 * @throws IOException
	 */
	public List<String> fileToStringList(String filepath) throws IOException{
		List<String> strSet = new ArrayList<String>();
		File file = new File(filepath);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line;
		while((line = br.readLine())!=null){
			strSet.add(line);
		}
		br.close();
		return strSet;
	}
	/**
	 * @param tag_vc
	 */
	public void writeMapToFile(Map<String, Long> tag_vc, String filePath) {
		StringBuilder sb = new StringBuilder();
		for(Entry<String, Long> en : tag_vc.entrySet()){
			if(!"".equals(en.getKey())){
				sb.append(en.getKey()).append("\t").append(en.getValue()).append("\r\n");
			}
		}
		try {
			writeToFile(sb.toString(), filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
