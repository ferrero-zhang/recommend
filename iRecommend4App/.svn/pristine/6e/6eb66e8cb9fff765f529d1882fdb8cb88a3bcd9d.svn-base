package com.ifeng.iRecommend.wuyg.commonData.Update.publish;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Pipeline;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.EntityPulisherOp;
import com.ifeng.iRecommend.wuyg.commonData.Update.UpdateActionType;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;
import com.ifeng.iRecommend.wuyg.commonData.entity.entityBase.KnowledgeBaseBuild;

/**
 * <PRE>
 * 作用 : 
 *   通用数据发布订阅消息
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
 *          1.0          2015年11月25日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class KnowledgeDBPublisher extends EntityPulisherOp {
	public KnowledgeDBPublisher(String channel, String key) {
		super(channel, key);
	}

	private static final Logger logger = LoggerFactory
			.getLogger(KnowledgeDBPublisher.class);
	private static String wordUpdateFiledir = LoadConfig
			.lookUpValueByKey("wordUpdateFiledir");

	static String entityFiledir = LoadConfig.lookUpValueByKey("entityFiledir");

	static String userDicPattern = LoadConfig
			.lookUpValueByKey("userDicPattern");
	static String UserDicDataDbNum = LoadConfig
			.lookUpValueByKey("UserDicDataDbNum");

	static final String UPDATEWORD_ADD = "updateWord_add";
	static final String UPDATEWORD_DEL = "updateWord_del";
	static final String UPDATEWORD_ALTER = "updateWord_alter";
	static final String UPDATEFILE_ADD = "updateFile_add";
	static final String UPDATEFILE_DEL = "updateFile_del";
	static final String UPDATEFILE_ALTER = "updateFile_alter";

	static FileUtil fileUtil = new FileUtil();

	/**
	 * 
	 * @param messageList
	 *            发布的消息
	 * @param updatetype
	 *            更新操作的类别 词的增删改，文件的增删改，该参数用来对发布不成功的数据进行redis回滚
	 * @param filepath
	 *            数据的来源，用于redis回滚时的数据获取
	 */
	public void publish(String message, String updatetype, List<String> data) {

		if (null == message || message.isEmpty()) {
			return;
		}

		logger.info("publish_message:" + message);
		if (super.pubMessage(message)) {
			logger.info("Publish Success.");
		} else {
			logger.error("Publish Failed.");

			switch (UpdateActionType.getActionType(updatetype)) {
			case ADD_WORD:
				updateWord_Del(data, null);
				break;
			case DEL_WORD:
				updateWord_Add(data, null);
				break;
			case ALTER_WORD:
				updateWord_Alter_undo(data, null);
				break;
			case ADD_FILE:
				updateFile_del(data);
				break;
			case DEL_FILE:
				updateFile_add_undo(data);
				break;
			case ALTER_FILE:
				updateFile_alter_undo(data);
				break;
			default:
				break;
			}

		}
	}

	// test main
	public static void main(String[] args) throws IOException {

		KnowledgeDBPublisher knowledgeDBPublisher = new KnowledgeDBPublisher(
				commonDataUpdateConfig.entLibMessageChannel,
				commonDataUpdateConfig.entLibKeyPattern);
		ArrayList<String> userDicList = new ArrayList<String>();
		UserDicPublisher userDicPublisher = new UserDicPublisher(
				LoadConfig.lookUpValueByKey("userDicPatternChannel"), userDicPattern);

		AllWordPublisher allWordPublisher = new AllWordPublisher(
				LoadConfig.lookUpValueByKey("allWordMessageChannel"), null);

		String content = new String();
		List<String> data = new ArrayList<String>();
		String message = new String();
		// 术语库初始化到redis
		// ArrayList<String> fileList = fileUtil.refreshFileList(entityFiledir);
		//
		// knowledgeDBPublisher.redisInit(fileList);

		String wordaddfile = wordUpdateFiledir + "addword.txt";

		content = fileUtil.Read(wordaddfile, "utf-8");
		data = new ArrayList<String>(Arrays.asList(content.split("\n")));
		message = knowledgeDBPublisher.updateWord_Add(data, null, userDicList);
		// updateWord_del(wordaddfile);//回滚操作
		// messageList = updateWord_add(wordaddfile);
		knowledgeDBPublisher.publish(message, UpdateActionType.ADD_WORD.name(),
				data);

		allWordPublisher.addWord2AllWordLib(userDicList);

		// 判定外挂词中是否包含本次新增的词汇，如果没有则发消息给接收方,没有测试》》》》》》》》》》
	//	userDicPublisher.addWord2UserDic(userDicList);
		message = userDicPublisher.addWord2UserDic(userDicList, true);

		userDicPublisher.publish(message, UpdateActionType.ADD_WORD.name(),
				null, null);

		userDicList.clear();

//		String wordalterfile = wordUpdateFiledir + "alterword.txt";
//		content = fileUtil.Read(wordalterfile, "utf-8");
//		data = new ArrayList<String>(Arrays.asList(content.split("\n")));
//		message = knowledgeDBPublisher
//				.updateWord_Alter(data, null, userDicList);
//		// updateWord_alter_undo(wordalterfile);// 回滚操作
//		knowledgeDBPublisher.publish(message,
//				UpdateActionType.ALTER_WORD.name(), data);
//
//		// 载入外挂词典
//		allWordPublisher.addWord2AllWordLib(userDicList);
//		userDicPublisher.addWord2UserDic(userDicList);
//		
//		
//		 String worddelfile = wordUpdateFiledir + "delword.txt";
//		 content = fileUtil.Read(worddelfile, "utf-8");
//		 data = new ArrayList<String>(Arrays.asList(content.split("\n")));
//		
////		String fileaddpath = wordUpdateFiledir + "addfile.txt";
////		content = fileUtil.Read(fileaddpath, "utf-8");
////		ArrayList<String> files = new ArrayList<String>(Arrays.asList(content.split("\n")));
////		
////		for (String fileInfo : files) {
////			long oldsize = -1;
////			long newsize = -1;
////
////			String filename = fileInfo.substring(fileInfo.indexOf("entLib_")
////					+ "entLib_".length());
////			File file = new File(entityFiledir + filename);
////			//File file = new File(entityFiledir + filename + "实体");
////			if (!file.exists()) {
////				file = new File(entityFiledir + filename + "实体.txt");
////			}
////			String fcontent = fileUtil.Read(file.getPath(), "utf-8");
////			ArrayList<String> list = new ArrayList<String>(
////					Arrays.asList(fcontent.split("\n")));
////            
////			data.addAll(list);
////		}
////		
////		
//		 message = knowledgeDBPublisher.updateWord_Del(data, null,userDicList);
//
//		 knowledgeDBPublisher.publish(message, UPDATEWORD_DEL, data);
//       userDicPublisher.delWordsFromUserDic(userDicList);
//		
//
//		// String filedelpath = wordUpdateFiledir + "delfile.txt";
//		// content = fileUtil.Read(filedelpath, "utf-8");
//		// data = new ArrayList<String>(Arrays.asList(content.split("\n")));
//		// message = knowledgeDBPublisher.updateFile_del(data);
//		// // updateFile_add_undo(filedelpath);//回滚操作
//		// // updateFile_del(filedelpath);
//		// knowledgeDBPublisher.publish(message,
//		// UpdateActionType.DEL_FILE.name(), data);
//		//
//
//		// String filealterpath = wordUpdateFiledir + "alterfile.txt";
//		// content = fileUtil.Read(filealterpath, "utf-8");
//		// data = new ArrayList<String>(Arrays.asList(content.split("\n")));
//		// message = knowledgeDBPublisher.updateFile_alter(data);
//		// // updateFile_alter_undo(filealterpath);
//		// // updateFile_alter(filealterpath);
//		// knowledgeDBPublisher.publish(message,
//		// UpdateActionType.ALTER_FILE.name(), data);
//
//		// fileaddpath = wordUpdateFiledir + "addfile.txt";
//		// content = fileUtil.Read(fileaddpath, "utf-8");
//		// data = new ArrayList<String>(Arrays.asList(content.split("\n")));
//		// message = knowledgeDBPublisher.updateFile_add(data,userDicList);
//		// //knowledgeDBPublisher.updateFile_del(data);
//		// // updateFile_del_undo(fileaddpath);//回滚操作
//		// // updateFile_add(fileaddpath);
//		// knowledgeDBPublisher.publish(message,
//		// UpdateActionType.ADD_FILE.name(), data);
//		//
//		// //载入外挂词典
//		// userDicPublisher = new UserDicPublisher(userDicPattern,
//		// userDicPattern);
//		// userDicPublisher.addWord2UserDic(userDicList);

	}
}
