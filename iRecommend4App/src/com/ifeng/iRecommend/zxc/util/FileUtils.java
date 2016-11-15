package com.ifeng.iRecommend.zxc.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;


public class FileUtils {
	private static final Logger log= Logger.getLogger(FileUtils.class);
    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     * @param fileName 读取文件的路径
     * @param withComment 是否为具有comment格式的文件。如果为true则以#开头的行内容将不会被读取
     * @return 
     * @throws IOException 
     */
    public static List<String> readFileByLines(String fileName,boolean withComment) throws IOException {
    	List<String> lines=new ArrayList<String>();
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                if(withComment&&tempString.startsWith("#")){
                	continue;
                }
            	lines.add(tempString.trim());
            }
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                	 log.error(e1.getMessage(), e1);
                }
            }
        }
    }
    public static void save(Iterator<String> it,String path,boolean append) throws IOException{
    	FileWriter fw = new FileWriter(path,append);
    	try{
	    	while(it.hasNext()){
	    		String s=it.next();
	    		fw.write(s+"\n");
	    	}
    	}finally{
    		fw.close();
    	}
    }

    public static byte[] readBytes(String fileName) throws IOException {
    	BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileName));        
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);        
        byte[] temp = new byte[1024];        
        int size = 0;        
        while ((size = in.read(temp)) != -1) {        
            out.write(temp, 0, size);        
        }        
        in.close();        
        byte[] content = out.toByteArray();      
        return content;
    }
    /**
     * 以行为单位读取文件，返回一个String
     * @param fileName 读取文件的路径
     * @param withComment 是否为具有comment格式的文件。如果为true则以#开头的行内容将不会被读取
     * @return 
     * @throws IOException 
     */
    public static String readFileAsStr(String fileName,boolean withComment, boolean withEmpty) throws IOException {
    	StringBuffer sb=new StringBuffer();
    	sb.append("");
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                if(withComment&&tempString.startsWith("#")){
                	continue;
                }
                if(!withEmpty&&tempString.trim().length()==0){
                	continue;
                }
                sb.append(tempString).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                	 log.error(e1.getMessage(), e1);
                }
            }
        }

    }
    public static void save(String str,String path,boolean append) throws IOException{
    	FileWriter fw = new FileWriter(path,append);
    	try{
	    	
    		fw.write(str);

    	}finally{
    		fw.close();
    	}
    }
    public static void writeBytes(byte[] data,String path,boolean append) throws IOException {
    	OutputStream out = new FileOutputStream(path,append);      
       try{
	    	out.write(data);  
	        out.flush();  
       }finally{
    	   out.close();
       }
    }
}
