package com.ifeng.iRecommend.wuyg.commonData.Update.publish;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.PropertyConfigurator;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.WordReadData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.WordReadData.WordInfo;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;
import com.ifeng.iRecommend.wuyg.commonData.entity.entityBase.KnowledgeBaseBuild;
/**
 * 
 * <PRE>
 * 作用 : 
 *    全量词合并  
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
 *          1.0          2016年4月26日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class AllWordMerge {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PropertyConfigurator.configure("./conf/log4j.properties");

		AllWordPublisher allWordPublisher = new AllWordPublisher(
				LoadConfig.lookUpValueByKey("allWordMessageChannel"), null);
		
		Iterator<String> iterator = null;
		ArrayList<String> userDicList = new ArrayList<String>();

//		// 从可读表中载入全量词
//		WordReadData wordReadData = WordReadData.getInstance();
//
//		ConcurrentHashMap<String, WordInfo> maps = wordReadData
//				.getWordReadMap();
//		
//
//		iterator = maps.keySet().iterator();
//
//		while (iterator.hasNext()) {
//			String key = iterator.next();
//			userDicList.add(key);
//		}
//
//		allWordPublisher.addWord2AllWordLib(userDicList);
//
//		userDicList.clear();
		
		// 从热词中载入全量词
		
//		 HotWordData hotWordData = HotWordData.getInstance();
//		 ConcurrentHashMap<String, HotWordInfo> HotwordMaps = hotWordData.getHotwordMap();
//		  
//		 iterator = HotwordMaps.keySet().iterator();
//
//			while (iterator.hasNext()) {
//				String key = iterator.next();
//				userDicList.add(key);
//			}
//
//			allWordPublisher.addWord2AllWordLib(userDicList);
//			userDicList.clear();
//			//从术语库中载入全量词
//			KnowledgeBaseBuild.initEntityTree();
//			for(EntityInfo entityInfo : KnowledgeBaseBuild.wordInfos){
//				userDicList.add(entityInfo.getWord());
//				for(String word : entityInfo.getNicknameList()){
//					if(null == word || word.isEmpty()){
//						continue;
//					}
//					userDicList.add(word);
//				}
//			}
//			allWordPublisher.addWord2AllWordLib(userDicList);
//			userDicList.clear();
		
	}

}
