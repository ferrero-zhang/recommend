package com.ifeng.iRecommend.wuyg.commonData.Update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Entity;

import redis.clients.jedis.Pipeline;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;
import com.ifeng.iRecommend.wuyg.commonData.entity.entityBase.KnowledgeBaseBuild;

/**
 * 
 * <PRE>
 * 作用 : 
 *   将术语导入redis中  
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
 *          1.0          2015年11月24日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class EntityPulisherOp extends Publisher {
	
	protected String key = "";

	public EntityPulisherOp(String channel) {
		super(channel);
		// TODO Auto-generated constructor stub
	}
	
	public EntityPulisherOp(String channel,String key){
		super(channel);
		this.key = key;
	}

	static Log LOG = LogFactory.getLog(EntityPulisherOp.class);
	static String commonDataDbNum = LoadConfig
			.lookUpValueByKey("commonDataDbNum");

	public static String alterDelimiter = LoadConfig
			.lookUpValueByKey("alterDelimiter"); // 用于修改术语时将新旧数据进行分割的符号
	static String alterDelimiterWord = LoadConfig
			.lookUpValueByKey("alterDelimiterWord");
	public static String filenameDelimiter = LoadConfig
			.lookUpValueByKey("filenameDelimiter");// 一条记录内部分割出文件名的分隔符

	public static String entLibKeyPattern = LoadConfig
			.lookUpValueByKey("entLibKeyPattern");

	public static String recordDelimiter = LoadConfig
			.lookUpValueByKey("recordDelimiter");

	public static String entityFiledir = LoadConfig
			.lookUpValueByKey("entityFiledir");

	static FileUtil fileUtil = new FileUtil();

	private boolean updateWords_AlterRedis(String[] oldrecords,
			String[] newrecords) {
		long oldsize = -1;
		long newsize = -1;

		List<String> dataList = publisherJedis.lrange(oldrecords[0], 0, -1);

		oldsize = dataList.size();

		if (dataList.contains(oldrecords[1])) {
			publisherJedis.lrem(oldrecords[0], 0, oldrecords[1]);

			newsize = publisherJedis.llen(oldrecords[0]);

			if ((oldsize - 1) != newsize) {
				LOG.error("updateWords_Alter exception: filekey:"
						+ oldrecords[0] + "\tdelword:" + oldrecords[1]
						+ "\toldsize:" + oldsize + "\tnewsize:" + newsize);
				return false;
			}

			oldsize = 0;
			newsize = 0;

			oldsize = publisherJedis.llen(newrecords[0]);

			publisherJedis.lpush(newrecords[0], newrecords[1]);

			newsize = publisherJedis.llen(newrecords[0]);

			if ((oldsize + 1) != newsize) {
				LOG.error("updateWords_Alter exception: filekey:"
						+ newrecords[0] + "\taddword:" + newrecords[1]
						+ "\toldsize:" + oldsize + "\tnewsize:" + newsize);
				return false;
			}

		}

		return true;
	}
	
//	// 文件的添加
//	public String updateFile_add(List<String> fileInfos,ArrayList<String> userDicList) {
//		
//		publisherJedis.select(Integer.valueOf(commonDataDbNum));
//		
//		String message = new String();
//		StringBuffer messageBuffer = new StringBuffer();
//		
//		for (String fileInfo : fileInfos) {
//			long oldsize = -1;
//			long newsize = -1;
//
//			String filename = fileInfo.substring(fileInfo.indexOf("entLib_")
//					+ "entLib_".length());
//			File file = new File(entityFiledir + filename);
//			//File file = new File(entityFiledir + filename + "实体");
//			if (!file.exists()) {
//				file = new File(entityFiledir + filename + "实体.txt");
//			}
//			String fcontent = fileUtil.Read(file.getPath(), "utf-8");
//			ArrayList<String> list = new ArrayList<String>(
//					Arrays.asList(fcontent.split("\n")));
//			ArrayList<EntityInfo> entityInfos = KnowledgeBaseBuild
//					.string2EntityInfos(list);
//			
//			//Pipeline pipeline = publisherJedis.pipelined();
//			
//			List<String> dataList = new ArrayList<String>();
//			int addCount = 0;
//
//			oldsize = dataList.size();
//
//			for (EntityInfo entityInfo : entityInfos) {
//				String wordInfo = entityInfo.toredisFormat();
//
//				if (!dataList.contains(wordInfo)) {
//					dataList.add(wordInfo);
//					userDicList.add(entityInfo.getWord());
//					userDicList.addAll(entityInfo.getNicknameList());
//					//pipeline.lpush("entLib_"+filename, wordInfo);
//					addCount++;
//				}
//			}
//			//pipeline.sync();
//
//			//newsize = publisherJedis.llen("entLib_" + filename);
//
//		
//				messageBuffer.append(fileInfo + recordDelimiter);
//		}
//		message = "add#DIV#file#DIV#" + messageBuffer.toString();
//		return message;
//
//	}
	
	// 文件的添加
	public String updateFile_add(List<String> fileInfos,ArrayList<String> userDicList) {
		
		publisherJedis.select(Integer.valueOf(commonDataDbNum));
		
		String message = new String();
		StringBuffer messageBuffer = new StringBuffer();
		
		for (String fileInfo : fileInfos) {
			long oldsize = -1;
			long newsize = -1;

			String filename = fileInfo.substring(fileInfo.indexOf("entLib_")
					+ "entLib_".length());
			File file = new File(entityFiledir + filename);
			//File file = new File(entityFiledir + filename + "实体");
			if (!file.exists()) {
				file = new File(entityFiledir + filename + "实体.txt");
			}
			String fcontent = fileUtil.Read(file.getPath(), "utf-8");
			ArrayList<String> list = new ArrayList<String>(
					Arrays.asList(fcontent.split("\n")));
			ArrayList<EntityInfo> entityInfos = KnowledgeBaseBuild
					.string2EntityInfos(list);
			
			Pipeline pipeline = publisherJedis.pipelined();
			
			List<String> dataList = publisherJedis.lrange("entLib_" + filename,
					0, -1);
			int addCount = 0;

			oldsize = dataList.size();

			for (EntityInfo entityInfo : entityInfos) {
				String wordInfo = entityInfo.toredisFormat();

				if (!dataList.contains(wordInfo)) {
					dataList.add(wordInfo);
					userDicList.add(entityInfo.getWord());
					userDicList.addAll(entityInfo.getNicknameList());
					pipeline.lpush("entLib_"+filename, wordInfo);
					addCount++;
				}
			}
			pipeline.sync();

			newsize = publisherJedis.llen("entLib_" + filename);

			if ((oldsize + addCount) != newsize) {
				LOG.error("updateFile_Add2redis exception:" + "\taddfile:"
						+ filename + "\toldsize:" + oldsize + "\tnewsize:"
						+ newsize);
			}else{
				messageBuffer.append(fileInfo + recordDelimiter);
			}
		}
		if (null == messageBuffer || messageBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "add#DIV#file#DIV#" + messageBuffer.toString();
		}
		
		return message;

	}

	// 文件的修改
	public String updateFile_alter(List<String> fileInfos) {
		String message = new String();
		StringBuffer meStringBuffer = new StringBuffer();
		for (String fileInfo : fileInfos) {
			if (null == fileInfo || fileInfo.isEmpty()) {
				continue;
			}
			String[] ss = fileInfo.split(alterDelimiter);

			long oldsize = -1;
			long newsize = -1;

			LOG.info("updateFile_Alter2redis" + fileInfo);
			String oldfilekey = ss[0];
			String newfilekey = ss[1];

			oldsize = publisherJedis.llen(oldfilekey);

			publisherJedis.rename(oldfilekey, newfilekey);

			newsize = publisherJedis.llen(newfilekey);

			boolean state = true;
			while(publisherJedis.exists(oldfilekey) && !publisherJedis.exists(newfilekey)){
				publisherJedis.rename(oldfilekey, newfilekey);
			}

			if (oldsize != newsize) {
				LOG.error("updateFile_Alter2redis exception:" + "\talterfile:"
						+ oldfilekey + "to" + "\t" + newfilekey + "\toldsize:"
						+ oldsize + "\tnewsize:" + newsize);
			}
			
			if (true == state) {
				meStringBuffer.append(ss[1] + alterDelimiterWord + ss[0]+recordDelimiter);
			}
		}
		
		if (null == meStringBuffer || meStringBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "alter#DIV#file#DIV#" + meStringBuffer.toString();
		}
		return message;
	}
	
	// 文件的修改
	public String updateFile_alter_undo(List<String> fileInfos) {
		String message = new String();
		StringBuffer meStringBuffer = new StringBuffer();
		for (String fileInfo : fileInfos) {
			if (null == fileInfo || fileInfo.isEmpty()) {
				continue;
			}

			String[] ss = fileInfo.split(alterDelimiter);
			long oldsize = -1;
			long newsize = -1;

			LOG.info("updateFile_Alter2redis_undo" + fileInfo);
			String oldfilekey = ss[0];
			String newfilekey = ss[1];

			oldsize = publisherJedis.llen(newfilekey);

			publisherJedis.rename(newfilekey, oldfilekey);

			boolean state = true;
			
			if (publisherJedis.exists(newfilekey)
					|| !publisherJedis.exists(oldfilekey)) {
				LOG.error("updateFile_Alter2redis_undo exception: del newfilekey failed:"
						+ newfilekey + "\t oldfilekey:" + oldfilekey);
				state = false;
			}

			newsize = publisherJedis.llen(oldfilekey);

			if (oldsize != newsize) {
				LOG.error("updateFile_Alter2redis_undo exception:"
						+ "\talterfile:" + oldfilekey + "to" + "\t"
						+ newfilekey + "\toldsize:" + oldsize + "\tnewsize:"
						+ newsize);
				state = false;
			}
			
			if (true == state) {
				meStringBuffer.append(ss[1] + alterDelimiterWord + ss[0]+recordDelimiter);
			}
		}
		
		if (null == meStringBuffer || meStringBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "alter#DIV#file#DIV#" + meStringBuffer.toString();
		}
		return message;
	}

	// 文件的删除
	public String updateFile_del(List<String> fileInfos) {
		String message = new String();
		publisherJedis.select(Integer.valueOf(commonDataDbNum));
		StringBuffer messageBuffer = new StringBuffer();
		for (String fileInfo : fileInfos) {

			long oldsize = -1;
			long newsize = -1;

			oldsize = publisherJedis.llen(fileInfo);;
			publisherJedis.rename(fileInfo, "del_"+fileInfo);

			newsize = publisherJedis.llen("del_" + fileInfo);
			
			boolean state = true;

			if (publisherJedis.exists(fileInfo)) {
				LOG.error("updateFile_Del2redis failed:" + fileInfo);
				state = false;
			}

			if (oldsize != newsize) {
				LOG.error("updateFile_Del2redis exception:" + "\tdelfile:"
						+ fileInfo + "to" + "\t" + "del_" + fileInfo
						+ "\toldsize:" + oldsize + "\tnewsize:" + newsize);
				state = false;
			}
			
			if (true == state) {
				messageBuffer.append("del_"+fileInfo + recordDelimiter);
			}

		}
		
		if (null == messageBuffer || messageBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "del#DIV#file#DIV#" + messageBuffer.toString();
		}
		return message;
	}

	// 文件的删除
	public boolean updateFile_del_undo(ArrayList<String> fileInfos) {
		for (String fileInfo : fileInfos) {

			publisherJedis.del(fileInfo);

			if (publisherJedis.exists(fileInfo)) {
				LOG.error("updateFile_Del2redis_undo failed:" + fileInfo);
				return false;
			}
		}
		return true;
	}
	
	// 文件的添加
	public String updateFile_add_undo(List<String> fileInfos) {
		String message = new String();
		StringBuffer messageBuffer = new StringBuffer();
		for (String fileInfo : fileInfos) {

			long oldsize = 0;
			long newsize = 0;

			oldsize = publisherJedis.llen("del_" + fileInfo);

			publisherJedis.rename("del_" + fileInfo,
					fileInfo);

			newsize = publisherJedis.llen(fileInfo);
			
			boolean state = true;

			if (publisherJedis.exists("del_" + fileInfo)
					|| !publisherJedis.exists(fileInfo)) {
				LOG.error("updateFile_Add2redis_undo exception :" + "del_"
						+ fileInfo + ":"
						+ publisherJedis.exists("del_" + fileInfo) + "\t"
						+ fileInfo + ":" + publisherJedis.exists(fileInfo));
				state = false;
			}

			if (oldsize != newsize) {
				LOG.error("updateFile_Add2redis_undo exception :" + " oldsize:"
						+ oldsize + "\tnewsize:" + newsize + "\toldfilekey:"
						+ "del_" + fileInfo + "\tnewfilekey:" + fileInfo);
				state = false;
			}
			if (true == state) {
				messageBuffer.append(fileInfo + recordDelimiter);
			}
		}
		
		if (null == messageBuffer || messageBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "add#DIV#file#DIV#" + messageBuffer.toString();
		}
		
		return message;
	}

	/**
	 * 从redis中获取所有术语
	 * 
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public HashMap<String, List<String>> readAllFromRedis(
			String keyspattern) throws IOException, ClassNotFoundException {

		Set<String> set = new HashSet<String>();

		int dbname = Integer.valueOf(commonDataDbNum);

		publisherJedis.select(dbname);

		set.addAll(publisherJedis.keys(keyspattern + "*"));

		HashMap<String, List<String>> dataMap = new HashMap<String, List<String>>();
		LOG.info("entityCount:" + set.size());
		LOG.info("entities:" + set);
		Iterator<String> iter = set.iterator();
		int size = 0;
		while (iter.hasNext()) {
			String key = iter.next();
			LOG.info("filenamekey:" + key);
			size++;
			List<String> dataList = new ArrayList<String>();
			dataList = publisherJedis.lrange(key, 0, -1);
			dataMap.put(key, dataList);
		}
		LOG.info("Load entityCount:" + size);
		return dataMap;
	}

	/**
	 * 从redis中获取指定文件的术语
	 * 
	 * @param keys
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public HashMap<String, List<String>> readSomeFileFromRedis(
			ArrayList<String> keys) throws IOException, ClassNotFoundException {
		HashMap<String, List<String>> dataMap = new HashMap<String, List<String>>();
		int dbname = Integer.valueOf(commonDataDbNum);

		publisherJedis.select(dbname);
		for (String key : keys) {
			List<String> dataList = publisherJedis.lrange(key, 0, -1);
			dataMap.put(key, dataList);
		}
		return dataMap;
	}

	public void AddFile2Redis(String filepath) throws IOException {
		ArrayList<String> fileList = fileUtil.refreshFileList(filepath);
		redisInit(fileList);
	}

	@Override
	public boolean redisInit(List<String> fileList) {
		publisherJedis.select(Integer.valueOf(commonDataDbNum));
		Pipeline pipeline = publisherJedis.pipelined();
		for (String fp : fileList) {
			if (fp.contains(".svn")) {
				continue;
			}
			File file = new File(fp);
			LOG.info(file.getPath());
			String filename = file.getName().substring(0,
					file.getName().indexOf("实体"));
			String content = fileUtil.Read(file.getPath(), "utf-8");

			ArrayList<String> list = new ArrayList<String>(
					Arrays.asList(content.split("\n")));
			ArrayList<EntityInfo> entityInfos = KnowledgeBaseBuild
					.string2EntityInfos(list);
			System.err.println(entityInfos.size());
			for (EntityInfo entityInfo : entityInfos) {
				String wordInfo = entityInfo.toredisFormat();
				// shardedJedis.lpush("entLib_" + filename, wordInfo);
				pipeline.lpush("entLib_" + filename, wordInfo);
			}

		}
		pipeline.sync();
		return true;
	}

	@Override
	public String updateWord_Alter_undo(List<String> data, String state) {
		LOG.info("updateWords_Alter_undoFilepath");
		publisherJedis.select(Integer.valueOf(commonDataDbNum));
		String message = new String();
		StringBuffer messageBuffer = new StringBuffer();
		for (String wordInfo : data) {
			String[] ws = wordInfo.split(alterDelimiter);
			String oldInfo = EntityInfo.str2redisFormat(ws[0]);
			String newInfo = EntityInfo.str2redisFormat(ws[1]);
			wordInfo = oldInfo + alterDelimiterWord + newInfo;
			
			String[] oldrecords = KnowledgeBaseBuild.recordSplit(oldInfo);

			String[] newrecords = KnowledgeBaseBuild.recordSplit(newInfo);

			boolean flag = updateWords_AlterRedis(newrecords, oldrecords);

			if (true == flag) {
				messageBuffer.append(wordInfo + recordDelimiter);
			}
			
		}
		
		if (null == messageBuffer || messageBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "alter#DIV#word#DIV#" + messageBuffer.toString();
		}

		return message;
	}

	@Override
	public String updateWord_Add(List<String> data, String state) {
		LOG.info("updateWords_AddFilepath");
		publisherJedis.select(Integer.valueOf(commonDataDbNum));
		String message = new String();
		StringBuffer messageBuffer = new StringBuffer();
		for (String wordInfo : data) {
			long oldsize = 0;
			long newsize = 0;
			wordInfo = EntityInfo.str2redisFormat(wordInfo);
			
			String[] info = KnowledgeBaseBuild.recordSplit(wordInfo);
			
			oldsize = publisherJedis.llen(info[0]);
			
			List<String> dataList = publisherJedis.lrange(info[0], 0, -1);
			
			if (!dataList.contains(info[1])) {
				publisherJedis.lpush(info[0], info[1]);
				newsize = publisherJedis.llen(info[0]);
				if ((oldsize + 1) != newsize) {
					LOG.error("updateWords_Add exception: filekey:" + info[0]
							+ "\taddword:" + wordInfo + "\toldsize:" + oldsize
							+ "\tnewsize:" + newsize);
				}else{
					messageBuffer.append(wordInfo + recordDelimiter);
				}
			}
			
		}
		
		if (null == messageBuffer || messageBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "add#DIV#word#DIV#" + messageBuffer.toString();
		}
		return message;
	}

	@Override
	public String updateWord_Add(List<String> data, String state,
			ArrayList<String> userDicList) {
		LOG.info("updateWords_AddFilepath");
		publisherJedis.select(Integer.valueOf(commonDataDbNum));
		String message = new String();
		StringBuffer messageBuffer = new StringBuffer();
		for (String wordInfo : data) {
			System.err.println(wordInfo);
			long oldsize = 0;
			long newsize = 0;
			String t_wordInfo = wordInfo;
			wordInfo = EntityInfo.str2redisFormat(wordInfo);
			
			String[] info = KnowledgeBaseBuild.recordSplit(wordInfo);
			
			oldsize = publisherJedis.llen(info[0]);
			
			List<String> dataList = publisherJedis.lrange(info[0], 0, -1);
			
			if (!dataList.contains(info[1])) {
				EntityInfo entityInfo = EntityInfo.string2Object(t_wordInfo);
				publisherJedis.lpush(info[0], info[1]);
				newsize = publisherJedis.llen(info[0]);
				userDicList.add(entityInfo.getWord());
				userDicList.addAll(entityInfo.getNicknameList());
				if ((oldsize + 1) != newsize) {
					LOG.error("updateWords_Add exception: filekey:" + info[0]
							+ "\taddword:" + wordInfo + "\toldsize:" + oldsize
							+ "\tnewsize:" + newsize);
				}else{
					messageBuffer.append(wordInfo + recordDelimiter);
				}
			}
			
		}
		
		if (null == messageBuffer || messageBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "add#DIV#word#DIV#" + messageBuffer.toString();
		}
		return message;
	}

	@Override
	public String updateWord_Alter(List<String> data, String state) {
		LOG.info("updateWords_AlterFilepath");
		publisherJedis.select(Integer.valueOf(commonDataDbNum));
		String message = new String();
		StringBuffer messageBuffer = new StringBuffer();
		for (String wordInfo : data) {
			String[] ws = wordInfo.split(alterDelimiter);
			String oldInfo = EntityInfo.str2redisFormat(ws[0]);
			String newInfo = EntityInfo.str2redisFormat(ws[1]);
			wordInfo = oldInfo + alterDelimiterWord + newInfo;
			
			String[] oldrecords = KnowledgeBaseBuild.recordSplit(oldInfo);

			String[] newrecords = KnowledgeBaseBuild.recordSplit(newInfo);
			boolean flag = updateWords_AlterRedis(oldrecords, newrecords);
			if (true == flag) {
				messageBuffer.append(wordInfo + recordDelimiter);
			}
		}
		
		if (null == messageBuffer || messageBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "alter#DIV#word#DIV#" + messageBuffer.toString();
		}

		return message;
	}
	
	@Override
	public String updateWord_Alter(List<String> data, String state,
			ArrayList<String> userDicList) {
		LOG.info("updateWords_AlterFilepath");
		publisherJedis.select(Integer.valueOf(commonDataDbNum));
		String message = new String();
		StringBuffer messageBuffer = new StringBuffer();
		for (String wordInfo : data) {
			System.err.println(wordInfo);
			String[] ws = wordInfo.split(alterDelimiter);
			String oldInfo = EntityInfo.str2redisFormat(ws[0]);
			String newInfo = EntityInfo.str2redisFormat(ws[1]);
			wordInfo = oldInfo + alterDelimiterWord + newInfo;
			
			String[] oldrecords = KnowledgeBaseBuild.recordSplit(oldInfo);

			String[] newrecords = KnowledgeBaseBuild.recordSplit(newInfo);
			boolean flag = updateWords_AlterRedis(oldrecords, newrecords);
			if (true == flag) {
				messageBuffer.append(wordInfo + recordDelimiter);
				EntityInfo entityInfo = EntityInfo.string2Object(ws[1]);
				userDicList.add(entityInfo.getWord());
				userDicList.addAll(entityInfo.getNicknameList());
			}
		}
		
		if (null == messageBuffer || messageBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "alter#DIV#word#DIV#" + messageBuffer.toString();
		}

		return message;
	}
	
	@Override
	public String updateWord_Del(List<String> data, String state,ArrayList<String> userDicList) {
		LOG.info("updateWords_DelFilepath");
		publisherJedis.select(Integer.valueOf(commonDataDbNum));
		String message = new String();
		StringBuffer messageBuffer = new StringBuffer();
		for (String wordInfo : data) {
			if(wordInfo.contains("中国青年志愿者协会") || wordInfo.contains("人口发展战略") || wordInfo.contains("奥运冠军")){
				System.err.println("");
			}
			EntityInfo entityInfo = EntityInfo.string2Object(wordInfo);
			wordInfo = EntityInfo.str2redisFormat(wordInfo);
			
			long oldsize = -1;
			long newsize = -1;

			String[] info = KnowledgeBaseBuild.recordSplit(wordInfo);

			info[0] = info[0].trim();
			
			List<String> dataList = publisherJedis.lrange(info[0], 0, -1);

			oldsize = dataList.size();
			if (dataList.contains(info[1])) {
				
				userDicList.add(entityInfo.getWord());
				for(String str : entityInfo.getNicknameList()){
					userDicList.add(str);
				}
				
				publisherJedis.lrem(info[0], 0, info[1]);

				newsize = publisherJedis.llen(info[0]);

				if ((oldsize - 1) != newsize) {
					LOG.error("updateWords_Del exception: filekey:" + info[0]
							+ "\tdelword:" + wordInfo + "\toldsize:" + oldsize
							+ "\tnewsize:" + newsize);
				}else{
					messageBuffer.append(wordInfo+recordDelimiter);
				}
			}
			
		}
		if (null == messageBuffer || messageBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "del#DIV#word#DIV#" + messageBuffer.toString();
		}
		return message;
	}
	
	@Override
	public String updateWord_Del(List<String> data, String state) {
		LOG.info("updateWords_DelFilepath");
		publisherJedis.select(Integer.valueOf(commonDataDbNum));
		String message = new String();
		StringBuffer messageBuffer = new StringBuffer();
		for (String wordInfo : data) {
			wordInfo = EntityInfo.str2redisFormat(wordInfo);
			
			long oldsize = -1;
			long newsize = -1;

			String[] info = KnowledgeBaseBuild.recordSplit(wordInfo);

			List<String> dataList = publisherJedis.lrange(info[0], 0, -1);

			oldsize = dataList.size();
			if (dataList.contains(info[1])) {
				
				publisherJedis.lrem(info[0], 0, info[1]);

				newsize = publisherJedis.llen(info[0]);

				if ((oldsize - 1) != newsize) {
					LOG.error("updateWords_Del exception: filekey:" + info[0]
							+ "\tdelword:" + wordInfo + "\toldsize:" + oldsize
							+ "\tnewsize:" + newsize);
				}else{
					messageBuffer.append(wordInfo+recordDelimiter);
				}
			}
			
		}

		if (null == messageBuffer || messageBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "del#DIV#word#DIV#" + messageBuffer.toString();
		}
		return message;
	}

	@Override
	public String updateWord_Add(List<String> data, String state, String time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String updateWord_Add(List<String> data, String state,
			ArrayList<String> userDicList, String time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String updateWord_Add(List<String> data) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}