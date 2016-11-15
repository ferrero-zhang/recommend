package com.ifeng.commen.blackList.publisher;

import java.util.ArrayList;

import org.junit.Test;

import com.ifeng.commen.blackList.BlackListData;
import com.ifeng.commen.blackList.util.CommonPSParams;


/**
 * 
 * <PRE>
 * 作用 : 
 *   发布代码的单测
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
public class BlacklistPublisherTest {
	static BlacklistUpdaterAndPublisher blacklistPublisher = new BlacklistUpdaterAndPublisher(
			CommonPSParams.blacklistKeyInRedis);

	 
	/**
	 * Filedir为录入redis数据文件所在的文件夹，文件数据格式说明如下：
	      keyword_word
	      article_id_title
	 具体事例：
	 * keyword_中国 
	 * keyword_美国 
	 * article_1_方法
	 * article_2_实时报道
	 */

	@Test
	public void testRedisInit() {
		//boolean state = blacklistPublisher.redisInit(CommonPSParams.Filedir);
	}

	/**
	 * addword.txt为将要添加的词文件 示例: keyword_澳大利亚 keyword_印度 article_5_俄罗斯时事新闻
	 */
	@Test
	public void testUpdateWord_add() {
		ArrayList<String> messageList = new ArrayList<String>();
		String path = CommonPSParams.addFilePath;
		
		messageList = blacklistPublisher.updateWord_add(path);
		
		blacklistPublisher.publish(messageList,
				UpdateActionType.ADD_WORD.name(), path);
		messageList.clear();
	}

	/**
	 * delword.txt为需要删除的词所在文件 示例： keyword_美国 keyword_日本
	 */
	@Test
	public void testUpdateWord_del() {
		ArrayList<String> messageList = new ArrayList<String>();
		String path =  CommonPSParams.delFilePath;
		
		messageList = blacklistPublisher.updateWord_del(path);
		
		blacklistPublisher.publish(messageList,
				UpdateActionType.DEL_WORD.name(), path);
		messageList.clear();
	}

	public static void main(String[] args) {
		BlacklistPublisherTest blacklistPublisherTest = new BlacklistPublisherTest();
		//blacklistPublisherTest.testRedisInit();
		//blacklistPublisherTest.testUpdateWord_add();
//		blacklistPublisherTest.testUpdateWord_del();
		
		
		System.out.println(BlackListData.getInstance().get_blacklist());
	
	}

}
