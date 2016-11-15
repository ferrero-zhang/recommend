package com.ifeng.iRecommend.wuyg.commonData.Update.publish;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.PublisherOperate;
import com.ifeng.iRecommend.wuyg.commonData.Update.UpdateActionType;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;
import com.ifeng.iRecommend.wuyg.commonData.entity.entityBase.KnowledgeBaseBuild;
/**
 * 
 * <PRE>
 * 作用 : 
 *     主别名库的发布订阅流程中的发布端
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
 *          1.0          2016年2月22日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class AliasLibPublisher extends PublisherOperate {
	private static Log LOG = LogFactory.getLog(AliasLibPublisher.class);
	private static String keySegment = LoadConfig.lookUpValueByKey("keySegment");
	private static String cateSegment = LoadConfig.lookUpValueByKey("cateSegment");
	private static String aliasSegment = LoadConfig.lookUpValueByKey("aliasSegment");
	private static String aliasDataRedisHost = LoadConfig.lookUpValueByKey("aliasDataRedisHost");
	private static String aliasDataRedisPort = LoadConfig.lookUpValueByKey("aliasDataRedisPort");
	private static String aliasDataMessageChannel = LoadConfig.lookUpValueByKey("aliasDataMessageChannel");
	private static String aliasDataPattern = LoadConfig.lookUpValueByKey("aliasDataPattern");
	
	public AliasLibPublisher(String channel, String key) {
		super(channel, key);
		// TODO Auto-generated constructor stub
	}

	public AliasLibPublisher(String host, String port, String channel,
			String key) {
		super(host, port, channel, key);
	}

	public void publish(String message, String updatetype,
			List<String> dataList, String state) {
		if (null == message || message.isEmpty()) {
			return;
		}

			LOG.info("publish_message:" + message);
			if (super.pubMessage(message)) {
				LOG.info("Publish Success.");
			} else {
				LOG.error("Publish Failed.");
				switch (UpdateActionType.getActionType(updatetype)) {
				case ADD_WORD:
					updateWord_Del(dataList, state);
					break;
				case DEL_WORD:
					updateWord_Add(dataList, state);
					break;
				case ALTER_WORD:
					updateWord_Alter_undo(dataList,state);
					break;
				default:
					break;
				}
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		AliasLibPublisher aliasLibPublisher = new AliasLibPublisher(aliasDataRedisHost, aliasDataRedisPort, aliasDataMessageChannel, aliasDataPattern);
		List<String> wordInfos = new ArrayList<String>();
		
		
		HashMap<String, HashMap<String, HashSet<String>>> maps = new HashMap<String, HashMap<String, HashSet<String>>>();
       
		KnowledgeBaseBuild.initEntityTree();

		for (EntityInfo en : KnowledgeBaseBuild.wordInfos) {

			if (en.getNicknameList().isEmpty()
					|| (en.getNicknameList().size() == 1 && en
							.getNicknameList().get(0).isEmpty())) {
				continue;
			}

			if(en.toString().contains("地点术语") || en.getFilename().equals("地区")){
				continue;
			}

			
			if (maps.containsKey(en.getWord())) {
				HashMap<String, HashSet<String>> cateAlisMaps = maps.get(en
						.getWord());
				if (cateAlisMaps.containsKey(en.getCategory())) {
					cateAlisMaps.get(en.getCategory()).addAll(
							en.getNicknameList());
				} else {
					HashSet<String> aliasSet = new HashSet<String>();
					aliasSet.addAll(en.getNicknameList());
					cateAlisMaps.put(en.getCategory(), aliasSet);
				}
			} else {
				HashMap<String, HashSet<String>> cateAlisMaps = new HashMap<String, HashSet<String>>();
				HashSet<String> aliasSet = new HashSet<String>();
				aliasSet.addAll(en.getNicknameList());
				cateAlisMaps.put(en.getCategory(), aliasSet);
				maps.put(en.getWord(), cateAlisMaps);
			}
		}
		
		String filedir = "D:/data/wuyg/别名/主别名数据/";
		
	    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(filedir + "knowledge.txt"), false), "utf-8");
		
		Iterator<String> iterator = maps.keySet().iterator();
		
		while(iterator.hasNext()){
			String key = iterator.next();
			
			osw.append(key+"\t"+maps.get(key)+"\n");
			
		}
		
		osw.flush();
		osw.close();
		
		
//		Iterator<String> iterator = maps.keySet().iterator();
//		
//		while (iterator.hasNext()) {
//			StringBuffer sBuffer = new StringBuffer();
//			String key = iterator.next();
//			HashMap<String, HashSet<String>> itMaps = maps.get(key);
//			if (itMaps.size() > 0) {
//			Iterator<String> iterator2 = itMaps.keySet().iterator();
//			sBuffer.append(key + keySegment);
//			while (iterator2.hasNext()) {
//				String key2 = iterator2.next();
//				sBuffer.append(key2 + aliasSegment);
//				sBuffer.append(itMaps.get(key2) + cateSegment);
//			}
//			}
//            wordInfos.add(sBuffer.toString());
//			sBuffer = null;
//		}
//		
//		
//
//		
//		
//		
//		String message = aliasLibPublisher.updateWord_Add(wordInfos);
//		aliasLibPublisher.publish(message, UpdateActionType.ADD_WORD.name(), wordInfos, null);
//		
//		wordInfos.clear();
		
//		FileUtil fileUtil = new FileUtil();
//		
//		String content = fileUtil.Read("e:/主别名/alias_alter.txt", "utf-8");
//		
//		wordInfos.addAll(Arrays.asList(content.split("\n")));
//		
//		message = aliasLibPublisher.updateWord_Alter(wordInfos, null);
//		
//		aliasLibPublisher.publish(message, UpdateActionType.ALTER_WORD.name(), wordInfos, null);
//		
//		wordInfos.clear();
//		
//		content = fileUtil.Read("e:/主别名/alias_del.txt", "utf-8");
//		
//		wordInfos.addAll(Arrays.asList(content.split("\n")));
//		
//		message = aliasLibPublisher.updateWord_Del(wordInfos, null);
//		
//		aliasLibPublisher.publish(message, UpdateActionType.DEL_WORD.name(), wordInfos, null);
		
	}

}
