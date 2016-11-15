package com.ifeng.commen.blackList.publisher;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;





import com.ifeng.commen.blackList.util.CommonPSParams;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.commenFuncs;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;


/**
 * 
 * <PRE>
 * 作用 : 
 *    redis具体的更新操作  
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
 *          1.0          2015年12月9日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class BlackListRedisUpdate extends RedisUpdateMethod {
	public static Log LOG = LogFactory.getLog(BlackListRedisUpdate.class);

	public BlackListRedisUpdate(Jedis jedis) {
		super(jedis);
	}

	/**
	 * redis数据初始化
	 * 读取本地文件数据对redis进行初始化(可选)
	 * 
	 * @param filedir
	 */
	public boolean InitRedis(String filedir) {
		Pipeline pipeline = jedis.pipelined();
		String time = commenFuncs.date2Longstr(new Date());
		FileUtil fileUtil = new FileUtil();
		ArrayList<String> fileList = fileUtil.refreshFileList(filedir);
		long oldsize = 0;
		long newsize = 0;
		for (String f : fileList) {
			File file = new File(f);
			String content = fileUtil.Read(file.getPath(), "utf-8");
			String[] cs = content.split("\n");

			for (String info : cs) {
				String in = info + "_" + time;
				pipeline.lpush(CommonPSParams.blacklistKeyInRedis, in);
				oldsize++;
			}
		}
		pipeline.sync();

		newsize = jedis.llen(CommonPSParams.blacklistKeyInRedis);

		if ((oldsize != newsize) || (oldsize == 0 || newsize == 0)) {
			LOG.error("InitRedis exception : \toldsize:" + oldsize
					+ "\tnewsize:" + newsize);
			return false;
		}

		return true;
	}

	
	/**
	 * 向redis中blacklistKeyInRedis插入新数据data,返回插入是否成功的标记
	 * @param data 要插入的新数据,多条数据之间以recordDelimiter分隔
	 * 
	 * 若插入过程失败,返回
	 */
	@Override
	public boolean updateInfo_Add(String data) {
		String time = commenFuncs.date2Longstr(new Date());
		
		String[] infos = data.split(CommonPSParams.recordDelimiter);

		HashMap<String, String> dataMap = new HashMap<String, String>();
		dataMap = getdatafromredis(CommonPSParams.blacklistKeyInRedis);

		long oldsize = dataMap.size();

		int addcount = 0;

		for (String info : infos) {
			LOG.info("updateInfo_Add:" + info);
			if (!dataMap.containsKey(info)) {
				dataMap.put(info, info + "_" + time);
				addcount++;
			} else {
				dataMap.put(info, info + "_" + time);
			}
		}
		//取dataMap的values,存入dataArrays
		String[] dataArrays  = getdataArrays(dataMap);

		//先删除再插入
		jedis.del(CommonPSParams.blacklistKeyInRedis);
		jedis.lpush(CommonPSParams.blacklistKeyInRedis, dataArrays);

		long newsize = super.jedis.llen(CommonPSParams.blacklistKeyInRedis);

		//校验插入结果
		boolean state = true;
		if (((oldsize + addcount) != newsize) ||  newsize == 0) {
			LOG.error("updateInfo_Add exception:\tkey"
					+ CommonPSParams.blacklistKeyInRedis+ "\toldsize:" + oldsize
					+ " \taddsize:" + addcount + "\taddrecord:" + infos.length
					+ "\tnewsize:" + newsize);
			state = false;
		}
		return state;
	}

	@Override
	public boolean updateInfo_Alter(String data) {
		// TODO Auto-generated method stub
		return super.updateInfo_Alter(data);
	}

	@Override
	public boolean updateInfo_Del(String data) {
		boolean state = true;
		String[] infos = data.split(CommonPSParams.recordDelimiter);

		 HashMap<String, String> dataMap = getdatafromredis(CommonPSParams.blacklistKeyInRedis);

		long oldsize = dataMap.size();
		int delcount = 0;
		for (String info : infos) {
			LOG.info("updateInfo_Del:" + info);
			if (dataMap.containsKey(info)) {
				dataMap.remove(info);
				delcount++;
			}
		}
		
		String[] dataArrays  = getdataArrays(dataMap);

		jedis.del(CommonPSParams.blacklistKeyInRedis);
		long newsize=0;
		if(dataArrays.length!=0){
			jedis.lpush(CommonPSParams.blacklistKeyInRedis, dataArrays);
		}

		if ((oldsize - delcount) != newsize) {
			LOG.error("updateInfo_Del exception:\toldsize:" + oldsize
					+ " \tdelsize:" + delcount + "\tnewsize:" + newsize);
			state = false;
		}
		return state;
	}
	
	
	public boolean updateInfo_DelAll(){
		
		jedis.del(CommonPSParams.blacklistKeyInRedis);
		long newsize = super.jedis.llen(CommonPSParams.blacklistKeyInRedis);
		boolean state = true;
		if(newsize!=0){
			state=false;
		}
		return state;
	}

	private HashMap<String, String> getdatafromredis(String key) {
		List<String> dataList = super.jedis.lrange(key, 0, -1);
		HashMap<String, String> dataMap = new HashMap<String, String>();

		for (String elem : dataList) {
			//elem: data_time
			String word = elem.substring(0, elem.lastIndexOf("_"));
			dataMap.put(word, elem);
		}
		return dataMap;
	}

	private static String[] getdataArrays(HashMap<String, String> dataMap) {
		String[] dataArrays = new String[dataMap.size()];
		Iterator<String> iterator = dataMap.keySet().iterator();
		int index = 0;
		while (iterator.hasNext()) {
			String key = iterator.next();
			dataArrays[index++] = dataMap.get(key);
		}
		return dataArrays;
	}
	
	
	
}
