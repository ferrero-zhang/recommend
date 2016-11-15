package com.ifeng.iRecommend.kedm.util;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;


public class SolrOperateUtil {
	
	public static int getCount(String baseUrl){
		int count=-1;
		try{
			URL url=new URL(baseUrl + "&rows=0");
			HttpURLConnection connection = (HttpURLConnection)url.openConnection(); 
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8")); 	
			String line=in.readLine();
			while(line!=null && !line.contains("result")){
				line=in.readLine();
			}
			in.close();		
			Pattern pat=Pattern.compile(".*?numFound=\"(\\d+)\".*?");
			Matcher m=pat.matcher(line);
			if(m.find()){
				count=Integer.valueOf(m.group(1));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}	
		return count;
	}
	
	public static List<String> GetDates(String time1, String time2) {
		List<String> dates = new ArrayList();
	
		try
		{
			DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
			Date date1 = f.parse(time1);
			Date date2 = f.parse(time2);
			
			Calendar start= Calendar.getInstance();
			start.set(date1.getYear()+1900, date1.getMonth(), date1.getDate());
		
			Calendar end = Calendar.getInstance();
			end.set(date2.getYear()+1900, date2.getMonth(), date2.getDate());
			
			while (start.compareTo(end) <= 0) {
				dates.add(f.format(start.getTime()));
				start.set(Calendar.DATE, start.get(Calendar.DATE) + 1);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return dates;
	}
	
	public static boolean isNullOrEmpty(String input) {
		return input == null || input.length() == 0;
	}
	
	public static String docToString(Document doc) {
		// XML转字符串
		String xmlStr = "";
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty("encoding", "UTF-8");// 解决中文问题，试过用GBK不行
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(doc), new StreamResult(bos));
			xmlStr = bos.toString();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return xmlStr;
	}
	
	public static String replaceInvaldateCharacter(String text) {
		char d = (char) 0x20;

		if (text != null) {
			char[] data = text.toCharArray();
			for (int i = 0; i < data.length; i++) {
				if (!isXMLCharacter(data[i])) {
					data[i] = d;
				}
			}
			return new String(data).replaceAll("&\\w{1,6};", " ").replaceAll(
					"&", "&amp;");
		}
		return "";
	}
	static boolean isXMLCharacter(int c) {
		if (c <= 0xD7FF) {
			if (c >= 0x20)
				return true;
			else
				return c == '\n' || c == '\r' || c == '\t';
		}
		return (c >= 0xE000 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0x10FFFF);
	}
	
}

class Word
{
	public Word(String name,Integer count)
	{
		this.Name=name;
		this.Count=count;
	}
	String Name;
	Integer Count;
	
	public String toString()
	{
		return Name + "," + Count;
	}
}

class ComparatorWord implements Comparator {

	public int compare(Object arg0, Object arg1) {
		Word word0 = (Word) arg0;
		Word word1 = (Word) arg1;

		int flag =0; //升序排序，符合时flag=1
		if(word0.Count<word1.Count)
			flag=1;
		return flag;
	}
}
