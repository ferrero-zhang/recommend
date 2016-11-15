package com.ifeng.iRecommend.wuyg.commonData.Update.publish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.CommonSubDataWord.WordState;
import com.ifeng.iRecommend.wuyg.commonData.Update.PublisherOperate;
import com.ifeng.iRecommend.wuyg.commonData.Update.UpdateActionType;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;

/**
 * 
 * <PRE>
 * 作用 : 
 *   热词每次更新注意不要与用户词典同步，此块代码有可能是注释掉的。
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
 *          1.0          2015年12月16日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class HotWordPublisher extends PublisherOperate {
	private static Log LOG = LogFactory.getLog(HotWordPublisher.class);
    static String userDicPattern = LoadConfig.lookUpValueByKey("userDicPattern");
    static String UserDicDataDbNum = LoadConfig.lookUpValueByKey("UserDicDataDbNum");
	public HotWordPublisher(String channel) {
		super(channel);
		// TODO Auto-generated constructor stub
	}

	public HotWordPublisher(String key, String channel) {
		super(key, channel);
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
		PropertyConfigurator.configure("./conf/log4j.properties");
		HotWordPublisher hotWordPublisher = new HotWordPublisher(
				commonDataUpdateConfig.hotWordPattern,
				commonDataUpdateConfig.hotWordMessageChannel);
		WordReadPublisher wordReadPublisher = new WordReadPublisher(
				commonDataUpdateConfig.wordPattern,
				commonDataUpdateConfig.wordMessageChannel);
		
		ArrayList<String> userDicList = new ArrayList<String>();
		
//		ArrayList<String> wordsList = new ArrayList<String>();
//		wordsList.add("百度VIP送健康");
//		hotWordPublisher.checkWordFromRedis(wordsList);
//		
//		HashMap<String, String> map = hotWordPublisher.getDataFromRedis();
//		
//		Iterator<String> iter = map.keySet().iterator();
//		OutputStreamWriter osw = new OutputStreamWriter(
//				new FileOutputStream("e:/data/hotword.txt", false), "utf-8");
//		while(iter.hasNext()){
//			String key = iter.next();
//			osw.append(key+"\t"+map.get(key)+"\n");
//		}
//		osw.flush();
//		osw.close();
		//EntityLibQuery.init();
		
		List<String> wordInfos = new ArrayList<String>();
		
		String path = new String();
		
		String message = new String();
		
//		wordInfos = hotWordPublisher.getSourceData(LoadConfig
//				.lookUpValueByKey("Filedir") + "hotwords");
		wordInfos.clear();
//		wordInfos.add("奇迹！国足晋级12强赛#DIV#IMCP2018wcq");
//		wordInfos.add("NBA专题#DIV#IMCPnbazhuanti");
//		wordInfos.add("山东问题疫苗引恐慌#DIV#IMCPymwti");
//		wordInfos.add("习近平首次出访捷克#DIV#IMCPxjpfjki");
//		wordInfos.add("国足征战世预赛#DIV#IMCP2018wcq");
//		wordInfos.add("中国队征战世预赛#DIV#IMCP2018wcq");
//		wordInfos.add("北大学生弑母潜逃#DIV#IMCPbdxssmi");
//		wordInfos.add("凤凰科技直接2016年IT领袖峰会#DIV#IMCP2016hrsdi");
//		wordInfos.add("多地出台楼市新政#DIV#IMCPqfci");
//		wordInfos.add("国际足球新版#DIV#IMCPgj");
//		wordInfos.add("暖新闻#DIV#IMCPnxwi");
//		wordInfos.add("2016年博鳌论坛#DIV#IMCP2016boaolt");
		//wordInfos.add("这三年#DIV#IMCPzsni");
//		wordInfos.add("Delisting#DIV#IMCPDelisting");
//		wordInfos.add("StockHot20160328#DIV#IMCPStockHot20160328");
		//wordInfos.add("TESTTESTTESTTEST");
//		wordInfos.clear();
//		wordInfos.add("杀岳父母携妻埋尸");
//		
//		message = hotWordPublisher.updateWord_Add(wordInfos, WordState.read.name());
//		hotWordPublisher.publish(message, UpdateActionType.ADD_WORD.name(),
//		wordInfos,WordState.read.name());
		
//
		//path = LoadConfig.lookUpValueByKey("Filedir") + "addword.txt";
		//wordInfos = hotWordPublisher.getSourceData(path);		
		
//		wordInfos.clear();
//		wordInfos.add("嫦娥三号月面照片");
//		//wordInfos.add("70年房屋产权");
//		//wordInfos.add("成精何首乌");
		wordInfos.add("中国男篮出征里约#DIV#fashion_special_31");
		
		message = hotWordPublisher.updateWord_Add(wordInfos, WordState.read.name(),new ArrayList<String>(),null,false);

		hotWordPublisher.publish(message, UpdateActionType.ADD_WORD.name(),
				wordInfos,WordState.read.name());
		
//		message = wordReadPublisher.updateWord_Add(wordInfos,WordState.unread.name(),userDicList,null,null);
//		wordReadPublisher.publish(message, UpdateActionType.ADD_WORD.name(),
//				wordInfos,WordState.read.name());
//		
//		
//		path = LoadConfig.lookUpValueByKey("Filedir") + "alterword.txt";
//	//	wordInfos = hotWordPublisher.getSourceData(path);
//		wordInfos.clear();
//		
		
//	    wordInfos.add("TESTTESTTEST");
//	    wordInfos.add("南海仲裁案");
//		message = hotWordPublisher.updateWord_Alter(wordInfos, WordState.read.name());
//		hotWordPublisher.publish(message,
//				UpdateActionType.ALTER_WORD.name(), wordInfos,WordState.read.name());

		//
		
//		path = LoadConfig.lookUpValueByKey("Filedir") + "addword.txt";
//		//wordInfos = hotWordPublisher.getSourceData(path);		
//		wordInfos.clear();
//		wordInfos.add("奥运冠军");
//		message = hotWordPublisher.updateWord_Add(wordInfos, WordState.read.name());
//		hotWordPublisher.publish(message, UpdateActionType.ADD_WORD.name(),
//				wordInfos,WordState.read.name());
//		//
//		
//		path = LoadConfig.lookUpValueByKey("Filedir") + "delword.txt";
//		//wordInfos = hotWordPublisher.getSourceData(path);
//		wordInfos.clear();
//		wordInfos.add("内地反复是否");
//		message = hotWordPublisher.updateWord_Del(wordInfos, WordState.read.name());
//		hotWordPublisher.publish(message, UpdateActionType.DEL_WORD.name(),
//				wordInfos,WordState.read.name());
	}

}
