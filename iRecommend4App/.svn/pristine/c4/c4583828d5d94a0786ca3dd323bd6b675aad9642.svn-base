package com.ifeng.iRecommend.lidm.userLog;

/**
 * <PRE>
 * 作用 : 
 *   对本地用户日志文件中的数据进行解析后，更新到Redis中;提供初始化redis，设置过期时间，插入、删除数据等操作。
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
 *          1.0          2012-12-26        lidm          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;



import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.commen.redis.JedisInterface;

public class LogToRedis {
	private static Jedis JEDIS;
	private static int expireDay = 90;// 过期时间

	// private static final Log LOG = LogFactory.getLog("userLog");

	/**
	 * Get all userID in redis and save them in UserID.index
	 */
	public static void InitUserIDIndex(int parts) {
		System.out.println("Get All UserID");
		// LOG.info("Get All UserID");

		long start = System.currentTimeMillis();
		if (JEDIS == null) {
			System.out.println("Jedis Not Inited");
			// LOG.error("Jedis Not Inited");
			return;
		}
		HashMap<String, String> userIDHashMap = new HashMap<String, String>();

		String fileName = "UserID.index";
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(new File(fileName + ".0"));
			Set<String> keysSet = JEDIS.keys("UL_*");

			System.out.println("Key Size:" + keysSet.size());
			// LOG.info("Key Size:" + keysSet.size());

			for (Object key : keysSet) {
				String userID = (String) key;

				if (userID.substring(3).length() > 5 && userID.contains("@")) {
					String idNum = userID.substring(0, userID.indexOf("@"));
					String platfomWithVersion = userID.substring(
							userID.indexOf("@") + 1).trim();
					if (userIDHashMap.containsKey(idNum)) {
						// System.out.println(idNum+"-"+userIDHashMap.get(idNum));
						// System.out.println(userID);
						String oldValue = (String) userIDHashMap.get(idNum)
								.trim();
						String[] pvs = oldValue.split("@");

						boolean v_existed = false;
						for (String pv : pvs) {
							if (pv.equals(platfomWithVersion)) {
								v_existed = true;
								break;
							}
						}
						if (!v_existed) {
							userIDHashMap.put(idNum, oldValue + "@"
									+ platfomWithVersion);
							System.out.println(idNum + "-" + oldValue + "@"
									+ platfomWithVersion);
						}

					} else {
						userIDHashMap.put(idNum, platfomWithVersion);
					}
				}
			}

			System.out.println("HashMap Size:" + userIDHashMap.size());
			// LOG.info("HashMap Size:" + userIDHashMap.size());

			int db_size = userIDHashMap.size();
			int sub_size = db_size / parts;

			int seq = 0;
			int filePart = 1;

			Iterator<Entry<String, String>> iter = userIDHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
				String userID = (String) entry.getKey();
				String value = (String) entry.getValue();
				// for (Object key : keysSet) {
				if (seq >= sub_size) {
					fileWriter.flush();
					fileWriter.close();
					System.out.println("Create File:" + fileName + "."
							+ (filePart - 1));
					fileWriter = new FileWriter(new File(fileName + "."
							+ filePart));
					filePart++;
					seq = 0;
				}
				fileWriter.append("[" + seq + "]	" + userID + "@" + value
						+ "\r\n");
				seq++;
			}

			fileWriter.flush();
			long end = System.currentTimeMillis();
			System.out.println("Init UserID Index Duration:" + (end - start));
		} catch (Exception e) {
			// LOG.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Get All UserID Finished!");
			// LOG.info("Get All UserID Finished!");
		}
	}

	public static void InitUserIDIndex_forTest() {
		System.out.println("Get All UserID");
		// LOG.info("Get All UserID");

		long start = System.currentTimeMillis();
		if (JEDIS == null) {
			System.out.println("Jedis Not Inited");
			// LOG.error("Jedis Not Inited");
			return;
		}
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(new File("UserID.index"));
			ArrayList<String> userIDList = new ArrayList<String>();
			for (int i = 0; i < 100; i++) {
				String userID = LogToRedis.getJedis().randomKey();
				userIDList.add(userID);
			}

			System.out.println("Key Size:" + userIDList.size());
			// LOG.info("Key Size:" + userIDList.size());

			int seq = 0;
			for (String key : userIDList) {
				String userID = (String) key;

				fileWriter.append("[" + seq + "]	" + userID + "\r\n");
				seq++;
			}
			fileWriter.flush();
			long end = System.currentTimeMillis();
			System.out.println("Init UserID Index Duration:" + (end - start));
		} catch (Exception e) {
			// LOG.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Get All UserID Finished!");
			// LOG.info("Get All UserID Finished!");
		}
	}

	/**
	 * Init properties
	 */
	public static void InitProperties() {
		expireDay = Integer.parseInt(LoadConfig.lookUpValueByKey("ExpireDay"));
	}

	/**
	 * Init Redis
	 * 
	 * @param dbID
	 */
	public static void InitRedis(int dbID) {
		JEDIS = JedisInterface.getJedisInstance(dbID);
		InitProperties();
		System.out.println("[DBSize]	" + JEDIS.dbSize());
	}

	public static Jedis getJedis() {
		return JEDIS;
	}

	/**
	 * Write user info to redis
	 * 
	 * @param userHashMap
	 */
	public static void WriteUserItem(HashMap<String, MobileLogItem> userHashMap) {
		if (JEDIS == null) {
			System.out.println("[ERROR]	Null Jedis!");
			return;
		}
		// Pipeline pipeline = JEDIS.pipelined();
		long start = System.currentTimeMillis();
		try {
			Iterator<Entry<String, MobileLogItem>> iterator = userHashMap.entrySet().iterator();
			while (iterator.hasNext()) {
				String userID = null;
				String logDate = null;
				String userItemValue = null;

				try {
					Map.Entry<String, MobileLogItem> entry = (Map.Entry<String, MobileLogItem>) iterator.next();
					String userIDHashKey = (String) entry.getKey();
					userID = userIDHashKey.substring(0,
							userIDHashKey.indexOf("-"));
					logDate = userIDHashKey.substring(userIDHashKey
							.indexOf("-") + 1);
					//MobileLogItem userItem = (MobileLogItem) entry.getValue();

					String userInfoValue = "";//userItem.getMobileLogItemInfoValue();
					String userActionValue = "";//userItem.getUserActionValue();
					
					userItemValue = userInfoValue + "!" + userActionValue;
					boolean isLogDateExisted = false;

					// isLogDateExisted = subJedis.hexists(userID, logDate);
					isLogDateExisted = JEDIS.hexists(userID, logDate);
					if (isLogDateExisted) {
						// System.out.println("Log Date Existed!");
						// String oldUserItemValue = subJedis.hget(userID,
						// logDate);
						String oldUserItemValue = JEDIS.hget(userID, logDate);
						// uncompress string
						oldUserItemValue = commenFuncs
								.UnCompressStr(oldUserItemValue);
						if (oldUserItemValue == null) {
							System.out.println("UnCompress Error!");
							continue;
						}

						userItemValue = oldUserItemValue + "!"
								+ userActionValue;

						// System.out.println("New Value:" + userItemValue);
						// System.out.println("Old Value:" + oldUserItemValue);
					}

					// compress string
					userItemValue = commenFuncs.CompressStr(userItemValue);
					if (userItemValue == null) {
						System.out.println("Compress Error!");
						continue;
					}
					JEDIS.hset(userID, logDate, userItemValue);
					// System.out.println("UserID:"+userID+" LogDate:"+logDate);
					// JEDIS.expire(userID, expireSeconds);
				} catch (Exception e) {
					System.out.println("[ERROR]	Can Not Check UserID Existed!");
					System.out.println("[UserID]" + userID + "-" + logDate);
					e.printStackTrace();
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("Duration:" + (end - start));
	}

	/**
	 * Delete the field of userID beyond expireTime
	 */
	public static void CleanOldUserItem() {
		// InitUserIDIndex();
		long start = System.currentTimeMillis();
		System.out.println("[Clean Old UserItem]");
		// LOG.info("Clean old userItem");
		try {
			File userIDIndexFile = new File("UserID.index");
			if (!userIDIndexFile.exists()) {
				System.out.println("UserID index file not exists!");
				// LOG.error("UserID index file not exists!");
				return;
			}

			Calendar c = Calendar.getInstance();

			int nowDate = c.get(Calendar.DATE);
			c.set(Calendar.DATE, nowDate - expireDay);

			String invalidDay = new SimpleDateFormat("yyyyMMdd").format(c
					.getTime());

			System.out.println(invalidDay);
			// LOG.info("Invalid day:" + invalidDay);

			FileUtil userIDIndex = new FileUtil();
			userIDIndex.Initialize("UserID.index", "UTF-8");
			String userID = null;
			while ((userID = userIDIndex.ReadLine()) != null) {
				userID = userID.substring(userID.indexOf("]") + 1).trim();
				// if (JEDIS.hexists(userID, invalidDay))
				// {
				// delete invalidDay of userID in redis
				JEDIS.hdel(userID, invalidDay);
				// LOG.info("Delete UserItem:" + userID + " at " + invalidDay);
				// System.out.println("Delete UserItem:" + userID + " at " +
				// invalidDay);
				// }
			}
			// remove user id index file
			userIDIndexFile.delete();
			long end = System.currentTimeMillis();
			System.out.println("Delete Duration:" + (end - start));

		} catch (Exception e) {
			e.printStackTrace();
			// LOG.error(e.getMessage());
		}
		System.out.println("[Finished Clean Old UserItem]");
		// LOG.info("Finished Clean Old UserItem");
	}

	class MobileLogItem{
		//
	}
	public static void main(String[] args) {
		// int dbID = 1;
		// UserLogToRedis.InitRedis(dbID);
		// UserLogToRedis.InitUserIDIndex(Integer.parseInt(args[0]));

		// InitProperties();
		// CleanOldUserItem();
		// Calendar c = Calendar.getInstance();
		// int nowDate = c.get(Calendar.DATE);
		// c.set(Calendar.DATE, nowDate - expireDay);
		//
		// String invalidDay = new SimpleDateFormat("yyyyMMdd").format(c
		// .getTime());
		// System.out.println(invalidDay);
	}
}
