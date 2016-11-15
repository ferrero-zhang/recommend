package com.ifeng.iRecommend.hexl1.HDFSOp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.classifyClient.ClassifierClient;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.channelsParser;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation;
import com.ifeng.iRecommend.lidm.userLog.logDBOperation.LogType;
/**
 * 
 * <PRE>
 * 作用 :分析一定的活跃用户一周内的访问倾向 ，结果分析追加到HDFS相应的文件中
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :每周要追加一次，活跃用户的id列表在低于25W时要更新
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
public class tfAnalyseikv
{
	private static final Log LOG = LogFactory.getLog("tfAnalyseikv");
	
	private HashMap<String, Integer> result_hashmap = new HashMap<String, Integer>();
	private Set<String> usrSet = new HashSet<String>();
	private HashMap<String, Integer> tf_hashmap = new HashMap<String, Integer>();
	private int count = 0;//记录总用户数目
	private int countUserLog = 0; 
	private int countUsefulLog = 0;
	private int countUselessUser = 0;
	private int countref = 0;
	private int countNoInf = 0;
	
	tfAnalyseikv()
	{
		logDBOperation.setLogType(LogType.APPLOG);
	}
	
	public void cycle (ItemOperation itemop, String s1, Pattern pattern, Pattern pattern1)
	{
		String timeStamp = String.valueOf(System.currentTimeMillis());// "1405958399000";//String.valueOf(System.currentTimeMillis());
		//System.out.println();
		// 获取一个用户id
		String id = s1;
		// 根据id获取用户行为记录
		HashMap<String, String> usrlog_hashmap = new HashMap<String, String>();
		usrlog_hashmap = logDBOperation.queryUserIDInDateRange(id, timeStamp, 7);
		int tempcount = 0;
		// 循环读取用户行为
		for (Entry<String, String> entry : usrlog_hashmap.entrySet())
		{
			//System.out.println(entry.getValue());
			String[] usrlog = entry.getValue().split("!");
			for(String tem : usrlog)
			{
				tempcount++;
				countUserLog++;
				//匹配
				Matcher matcher = pattern.matcher(tem);
				if(matcher.find())
				{
					countUsefulLog++;
					String postid = matcher.group(1);
					//System.out.println(postid);
					String leftStr = matcher.group(2);
					Item item = null;
					boolean flag = true;
					while(flag)
					{
						try
						{
							item = itemop.getItem(postid);
							flag = false;
						}
						catch(Exception e)
						{
							LOG.error("[ERROR] Get item failed.");
							continue;
						}
					}

					String channel = "";
					try
					{
						channel = channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannelByItem(item, 0);//
						//System.out.println(channel);
					}	
					catch(java.lang.NoClassDefFoundError e)
					{
						LOG.error("[ERROR] Get channel failed.");
						continue;
					}
					if(channel == null || channel == "" || channel == " ")
					{
						Matcher matcher1 = pattern1.matcher(leftStr);
						if(matcher1.find())
						{
								channel = matcher1.group(1);
								//System.out.println("channel: "+channel);	
								countref++;
						}
					}	
					if(channel!=null)
					{
						if(channel.startsWith("video")||channel.startsWith("icmstest")||channel.startsWith("notopic"))
							channel = "other";
						String[] row = channel.split("-");
						for(int i = 1; i < row.length; i++)
						{
							row[i] = row[i-1]+"-"+row[i];
						}
//						String content = item.getContent();
//						String title = item.getTitle();
//						HashSet<String> secs = classify(title,content);
						HashSet<String> secs = null;
						if(secs == null)
						{
							secs = new HashSet<String>();
						}
						secs.add(row[0]);
//						LOG.info(tem+" "+title+" "+secs.size());
						for(String e : secs)
						{
							if(e.trim() == "")
							{}
							else 
							{
								if(tf_hashmap.containsKey(e))
							{
								tf_hashmap.put(e, tf_hashmap.get(e)+1);
							}
							else
							{
								tf_hashmap.put(e,1);
							}
							usrSet.add(e);
							}
						}
						for(int j = 1; j < row.length; j++)
						{
							if(tf_hashmap.containsKey(row[j]))
							{
								tf_hashmap.put(row[j], tf_hashmap.get(row[j])+1);
							}
							else
							{
								tf_hashmap.put(row[j],1);
							}
							//System.out.println("row[j] "+ row[j]);
							usrSet.add(row[j]);
							//System.out.println("usrSet.size() "+usrSet.size());
						}
					}

				}
			}
		}
		//System.out.println("usrSet.size() "+usrSet.size());
		if(tempcount==0)
			countNoInf++;
		if(usrSet.isEmpty() || usrSet.size() == 0)
			countUselessUser++;
		for(String entry : usrSet)
		{
			//System.out.println(entry);
			if(result_hashmap.containsKey(entry))
			{
				Integer tempValue = result_hashmap.get(entry);
				tempValue++;
				result_hashmap.put(entry, tempValue);
			}
			else
			{
				result_hashmap.put(entry, 1);
			}
		}
		usrSet.clear();
	}
	private HashSet<String> classify(String title , String content)
	{
		if(content == null || content.trim().length() <= 64)
		{
			content = "";
		}
		String categorys = ClassifierClient.predict(title,content,"","com.ifeng.secondLevelMapping.none",null);
		if(categorys.startsWith("error")||categorys.startsWith("client.error"))
		{
			return null;
		}
		else
		{
			String[] secs = categorys.split("\\s");
			HashSet<String> resultSet = new HashSet<String>();
//			for(String entry : secs)
//			{
//				resultSet.add(entry);
//			}
		
			resultSet.add(secs[0]);
//			if(secs.length == 2)
//			{}
//			else
//			{}
			return resultSet;
		}
	}
	public void readFile(String file) throws IOException
	{
		// 读取文件流
		FileReader reader = null;
		try
		{
			reader = new FileReader(file);// 读取url文件
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("[ERROR] Read file failed.");
		}

		BufferedReader br = new BufferedReader(reader);
		String s1 = null;
		ItemOperation itemop = ItemOperation.getInstance();
		itemop.setItemType(ItemType.APPITEM);
		Pattern pattern = Pattern.compile(".*page#id=imcp_(\\d+)(.*)");
		Pattern pattern1 = Pattern.compile(".*$ref=(\\w+)\\$.*");
		// String loopID = null;

		while ((s1 = br.readLine()) != null)
		{

			try
			{
				count++;
				LOG.info("Now is extracting the "+count+" user ID.");
				cycle(itemop, s1, pattern, pattern1);

			}
			catch (Exception e)
			{
				e.printStackTrace();
				LOG.error("Something wrong with me.");
				continue;
			}
		}

		br.close();
		reader.close();

	}
	public void writeFile(String resultFileName) throws IOException
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
		for(Entry<String, Integer> entry : result_hashmap.entrySet())
		{
			fileWriter.write(entry.getKey()+"\n"+entry.getValue()+" "+tf_hashmap.get(entry.getKey())+"\n");
		}
		try
		{
			fileWriter.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	/** 
	 *  根据路径删除指定的目录或文件，无论存在与否 
	 *@param sPath  要删除的目录或文件 
	 *@return 删除成功返回 true，否则返回 false。 
	 */  
	public boolean DeleteFolder(String sPath) {  
	    boolean flag = false;  
	    File file = new File(sPath);  
	    // 判断目录或文件是否存在  
	    if (!file.exists()) {  // 不存在返回 false  
	        return flag;  
	    } else {  
	        // 判断是否为文件  
	        if (file.isFile()) {  // 为文件时调用删除文件方法  
	            return file.delete(); 
	        } else {  // 为目录时调用删除目录方法  
	            return DeleteFolder(sPath);  
	        }  
	    }  
	}
	
	public void weekFile(String allfile, String weekfile, String resultFile) throws IOException, ParseException
	{

		List<String> list1 = new ArrayList<String>();// 存储总文件内容的list
		List<String> list2 = new ArrayList<String>();// 存储每周新增文件内容的list
		int numFlag = 0;
		// 读取全局文件
		FileReader reader1 = null;
		try
		{
			reader1 = new FileReader(allfile);// 读取url文件
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("[ERROR] Read file failed.");
		}

		BufferedReader br1 = new BufferedReader(reader1);
		String s1 = null;
		// String loopID = null;

		while ((s1 = br1.readLine()) != null)
		{
			if (s1.equals("----------"))
			{
				numFlag++;
			}
			list1.add(s1);
		}
		br1.close();
		reader1.close();
		//读取每周的新增文件
		FileReader reader2 = null;
		try
		{
			reader2 = new FileReader(weekfile);// 读取url文件
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("[ERROR] Read file failed.");
		}

		BufferedReader br2 = new BufferedReader(reader2);
		String s2 = null;
		// String loopID = null;

		while ((s2 = br2.readLine()) != null)
		{
			if (s2.equals("------------------------TWO LEVEL-----------------------------"))
			{
				break;
			}
			else
			{
				list2.add(s2);
			}

		}
		br2.close();
		reader2.close();
		
		String usrNum = list2.get(0).split(" ")[1];
		String newtempStr = "";
		for (int i = list1.size(); i > 0; i--)
		{
			if (i == list1.size())
			{
				continue;
				// nothing
			}
			if (list1.get(i - 1).equals("----------"))
			{
				String temp[] = list1.get(i).split(" ");
				temp[2] = usrNum;
				list1.remove(i);
				String tempStr = temp[0]+" "+temp[1]+" "+temp[2];
				list1.add(tempStr);
				String newdate1 = getTimeString(temp[0]);
				String newdate2 = getTimeString(temp[1]);
				newtempStr = newdate1+" "+newdate2+" "+temp[2];
				break;
			}
			else
			{
				list1.remove(i);
			}
		}

			for(int j = 2; j < list2.size(); j++)
			{
				list1.add(list2.get(j));
			}
			list1.add("----------");
			list1.add(newtempStr);
			for(int j = 2; j < list2.size(); j++)
			{
				list1.add(list2.get(j));
			}
			list1.add("----------");
		FileWriter fileWriter = null;
		//写入文件流
		try
		{
			fileWriter = new FileWriter(resultFile, true);
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(String entry : list1)
		{
			fileWriter.write(entry+"\n");
		}
		fileWriter.flush();
		fileWriter.close();
	}
	
	public String getTimeString(String timeStr) throws ParseException
	{
		//String time = String.valueOf(System.currentTimeMillis()-1*86400000);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");  
		Date date = sdf.parse(timeStr);
		//String sourceFileName = sdf.format(new Date(Long.parseLong(time)));  
		long time = date.getTime()+7*86400000;
		String sourceTime = sdf.format(time);
		return sourceTime;
	}
	public static void main(String[] args) throws IOException, ParseException
	{
		// 读取原ID文件
		String sourceFileName = "/data/activeid";
		SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		long time = System.currentTimeMillis();
		String tempTime = format.format(time);
		String timeStr = tempTime.substring(0,10);
		
		String middleFileName = "/data/hexl/test/tfidf"+timeStr+"chaos";
		
		tfAnalyseikv tf = new tfAnalyseikv();
		tf.readFile(sourceFileName);
		tf.writeFile(middleFileName);
	
		String resultFile = "/data/hexl/test/tfidf"+timeStr;
		String lastFile = "/data/hexl/test/newtfidf"+timeStr;
		String weekFile = "/data/hexl/test/tfidf_week";
		FileWriter fileWriter = null;
		//写入文件流
		try
		{
			fileWriter = new FileWriter(resultFile, true);
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		fileWriter.write("有效用户数为： "+(500000-tf.countUselessUser)+"\n");
		try
		{
			fileWriter.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		calChannelNum calobj = new calChannelNum();
		calobj.readFile(middleFileName);
		calobj.writeFile(resultFile);
		HDFSOperation hdfs = new HDFSOperation();
		hdfs.downFile("/projects/zhineng/tfidf/tfidf_week", weekFile);
		tf.weekFile(weekFile, resultFile, lastFile);
		hdfs.DeleteFile("/projects/zhineng/tfidf/tfidf_week", true);
		hdfs.CopyFile(lastFile, "/projects/zhineng/tfidf/tfidf_week");
		boolean result = tf.DeleteFolder(middleFileName);
	//	
		
		result = tf.DeleteFolder(weekFile);
	}
}
