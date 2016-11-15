package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.MailSenderWithRotam;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.DocWordFilter;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.Document;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.EntityLibQuery;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.KeywordExtract;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.liuyi.customDicWordSearch.CustomWordSearcher;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;

/**
 * <PRE>
 * 作用 : 
 *   用户临时自定义词典的更新，目前的场景为添加，删除暂时不涉及。
 *   说明以下两点：
 *   	1. 用户手动添加的词，肯定是可读的，所以也要加入可读表。
 *   	2. 词同时要流入通用数据的userDic中
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
 *          1.0          2016年2月18日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class CustomWordUpdate extends LocalDataUpdate {
	private static CustomWordUpdate INSTANCE = null;

	// 达到此数值，就发邮件通知相关人员
	private static final int wordNumMailThreshold = 100;
	protected static int tempCustomDicWordCount = 0;
	
	// 分词服务器计数，所有的分词服务器重启完毕之后才清空相关数据
	private static final int splitMachineNum = 2;
	private static Set<String> splitMachine;
	private Set<String> userDicCache;
	
	public static CustomWordUpdate getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new CustomWordUpdate();
		}

		return INSTANCE;
	}
	
	public CustomWordUpdate() {
		init();
	}

	@Override
	protected void init() {
		splitMachine = new HashSet<String>();
		userDicCache = new HashSet<String>();
		
		try {
			setRedis_key(commonDataUpdateConfig.wordPattern);
			commonDataRedisPort = Integer.valueOf(LoadConfig
					.lookUpValueByKey("commonDataRedisPort"));
			commonDataRedisHost = LoadConfig
					.lookUpValueByKey("commonDataRedisHost");
			// 默认超时时间正常情况够了，不用设置
			setJedis(new Jedis(commonDataRedisHost, commonDataRedisPort));

		} catch (Exception e) {
			logger.error("Redis Init Error:", e);
			setJedis(null);
		}
		
		logger.info("begin to init custom_dic bin-trie...");
		CustomWordSearcher.initTrie();
		loadTempWordFromRedis();
		logger.info("custom_dic bin-trie end");
	}

	/**
	 * @Title: loadTempWordFromRedis
	 * @Description: 临时用户词典存储在redis中，每次启动程序的时候都判断下是否有数据
	 *               (当分词重启之后，清空redis中的词典和本地内存的词典，否则redis中的词典就作为备份)
	 * @author liu_yi
	 * @param map
	 * @return
	 * @throws
	 */
	public boolean loadTempWordFromRedis() {
		boolean result = true;
		
		if (null != jedis) {
			try {
				jedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("tempCustomDicDdNum")));

				Set<String> tempCustomDic = jedis.keys("*");
				if (!tempCustomDic.isEmpty()) {
					for (String tempWord : tempCustomDic) {
						// 添加进词典
						CustomWordSearcher.add(tempWord);
						
						// 加到本地缓存中
						userDicCache.add(tempWord);
						
						// 词计数器
						tempCustomDicWordCount++;
						System.out.println("loadTempWordFromRedis tempCustomDicWordCount++");
					}	
					
					System.out.println("loadTempWordFromRedis tempCustomDicWordCount:" + tempCustomDicWordCount);
				} else {
					logger.info("Redis tempCustomDicDB has no key");
				}
				
				// 达到一定数量就要发邮件提醒重启分词服务
				if (tempCustomDicWordCount >= wordNumMailThreshold) {
					System.out.println("请重启海量分词服务");
					String mail_subject = "请重启海量分词服务";
					String mail_content = "<strong>临时词典已经达到设定阈值</strong><br><br>====Detail Message====<br>当前临时词典中的词数量为:" + 
							tempCustomDicWordCount;
					String mail_receiver_config_name = "customDicExceptionReceivers";
					MailSenderWithRotam mswr = new MailSenderWithRotam(mail_subject, mail_content, mail_receiver_config_name);
					mswr.sendEmailWithRotam();
				}
				
			} catch (Exception ex) {
				logger.error("Jedis operation error:", ex);
				result = false;
			}
			
		}
		
		return result;
	}
	

	/**
	 * 
	 * 通过内部消息机制自动更新的词，就不需要再判断了，只需要看临时词典中是否有词即可
	 * 
	 * 注意：addElem2Local方法和addElem2Local_externalCall有大量共用代码,
	 *     可以抽时间提炼处理下
	 */
	@Override
	protected void addElem2Local(String add_content) {
		if (null == add_content || add_content.isEmpty()) {
			logger.info("addElems2localCustomWord:Empty Input.");
			return;
		}
		
		String[] inputElemsList = add_content.split(commonDataUpdateConfig.recordDelimiter);
		Set<String> newAddedTempWord = new HashSet<String>();
				
		try {
			for (String tempElem : inputElemsList) {
				if (null == tempElem || tempElem.isEmpty()) {
					continue;
				}
				
				// 如果本地缓存中没有这个词，就添加
				if (!userDicCache.contains(tempElem)) {
					CustomWordSearcher.add(tempElem);
					userDicCache.add(tempElem);
					
					// 词计数器
					tempCustomDicWordCount++;
				}
				
				// 如果之前临时词典已经添加了这个词，就不需要再add到redis中了
				jedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("tempCustomDicDdNum")));
				if (jedis.exists(tempElem)) {
					logger.info("has received word:" + tempElem + ", but reids tempUserDic has this word.");
					System.out.println("has received word:" + tempElem + ", but reids tempUserDic has this word.");
					continue;
				} else {
					// 同时还要放入redis中的临时词典中
					jedis.set(tempElem, tempElem);
				}

				newAddedTempWord.add(tempElem);
				
				// 计数器
				System.out.println("addElem2Local tempCustomDicWordCount++");
			}
			
			// 将此次更新的临时词加入userdic中
			addTempword2UserDic(newAddedTempWord);
			
		} catch (Exception ex) {
			logger.error("addElem2Local error:", ex);
		}
		

		// 达到一定数量就要发邮件提醒重启分词服务
		if (tempCustomDicWordCount >= wordNumMailThreshold) {
			System.out.println("请重启海量分词服务");
			String mail_subject = "请重启海量分词服务";
			String mail_content = "<strong>临时词典已经达到设定阈值</strong><br><br>====Detail Message====<br>当前临时词典中的词数量为:"
					+ tempCustomDicWordCount;
			String mail_receiver_config_name = "customDicExceptionReceivers";
			MailSenderWithRotam mswr = new MailSenderWithRotam(mail_subject,
					mail_content, mail_receiver_config_name);
			mswr.sendEmailWithRotam();
		}
	}

	
	
	/**
	 * @Title: addElem2Local_externalCall
	 * @Description: 外部调用方不知道分词中是否已经有要添加的词，所以需要多次判断，再添加(保留方法)
	 * @author liu_yi
	 * @param add_content
	 * @throws
	 */
	/*
	protected void addElem2Local_externalCall(String add_content) {
		if (null == add_content || add_content.isEmpty()) {
			logger.info("addElems2localCustomWord:Empty Input.");
			return;
		}
		
		String[] inputElemsList = add_content.split(commonDataUpdateConfig.recordDelimiter);
		HashSet<String> userDicSet = new HashSet<String>();
		String userDicKeyName = LoadConfig.lookUpValueByKey("userDicPattern");
		userDicSet.addAll(getUserDicfromredis(userDicKeyName));

		Set<String> newAddedTempWord = new HashSet<String>();
		try {
			for (String tempElem : inputElemsList) {
				if (null == tempElem || tempElem.isEmpty()) {
					continue;
				}
				
				// 先看下userDic中是否已经有这个词,如果其中已经有了，就不需要再处理了
				if (userDicSet.contains(tempElem)) {
					logger.info("has received word:" + tempElem + ", but tUserDic has this word.");
					System.out.println("has received word:" + tempElem + ", but userDic has this word.");
					continue;
				}
				
				// 再看下海量分词是否已经能直接分出这个词,如果能分出，就不需要再添加了
				if (isHailiangCanSplit(tempElem)) {
					logger.info("haiLiang can split this word already:" + tempElem);
					continue;
				}
				
				// 如果之前临时词典已经添加了这个词，就不需要再add了
				jedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("tempCustomDicDdNum")));
				if (jedis.exists(tempElem)) {
					logger.info("has received word:" + tempElem + ", but tempUserDic has this word.");
					System.out.println("has received word:" + tempElem + ", but tempUserDic has this word.");
					continue;
				}

				CustomWordSearcher.add(tempElem);

				// 同时还要放入redis中的临时词典中
				jedis.set(tempElem, tempElem);

				newAddedTempWord.add(tempElem);
				
				// 计数器
				System.out.println("addElem2Local tempCustomDicWordCount++");
				tempCustomDicWordCount++;
			}
			
			// 将此次更新的临时词加入userdic中
			addTempword2UserDic(newAddedTempWord);
		} catch (Exception ex) {
			logger.error("addElem2Local error:", ex);
		}
		

		// 达到一定数量就要发邮件提醒重启分词服务
		if (tempCustomDicWordCount >= wordNumMailThreshold) {
			System.out.println("请重启海量分词服务");
			String mail_subject = "请重启海量分词服务";
			String mail_content = "<strong>临时词典已经达到设定阈值</strong><br><br>====Detail Message====<br>当前临时词典中的词数量为:"
					+ tempCustomDicWordCount;
			String mail_receiver_config_name = "customDicExceptionReceivers";
			MailSenderWithRotam mswr = new MailSenderWithRotam(mail_subject,
					mail_content, mail_receiver_config_name);
			mswr.sendEmailWithRotam();
		}
	}
	
	public List<String> getUserDicfromredis(String key) {
		jedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("UserDicDataDbNum")));
		List<String> dataList = jedis.lrange(key, 0, -1);
	    return dataList;
	}
	
	*/
	
	protected void delAllElemFromLocal() {
		
	}
	
	@Override
	protected void delElemFromLocal(String del_content) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void alterElemInLocal(String alter_content) {
		// 此处只是借用alter这个字段来获取已经完成分词服务重启的机器ip
		if (null == alter_content || alter_content.isEmpty()) {
			logger.info("alterElemInLocal:Empty Input.");
			return;
		}
		
		logger.info("split service machine:" + alter_content + " has restarted.");
		splitMachine.add(alter_content);
		
		if (splitMachine.size() == splitMachineNum) {
			logger.info("all split service machine has been started, reset all variables...");
			
			// 清空本地的临时分词
			CustomWordSearcher.reset();
			
			// 词计数器清零
			tempCustomDicWordCount = 0;
			
			// 重启的分词服务器清空
			splitMachine.clear();
			
			// 清空本地的缓存
			userDicCache.clear();
			System.out.println("clear userDicCache, size:" + userDicCache.size());
			
			// 最后再执行redis数据清空
			jedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("tempCustomDicDdNum")));
			jedis.flushDB();
		}
	}
	
	/**
	 * @Title: addTempword2UserDic
	 * @Description: 将临时词典中的词加入正式词典中
	 * @author liu_yi
	 * @return
	 * @throws
	 */
	public boolean addTempword2UserDic(Set<String> tempWordSet) {
		boolean result = true;
		
		try {
			if (!tempWordSet.isEmpty()) {
				HashSet<String> userDicSet = new HashSet<String>();
				String userDicKeyName = LoadConfig.lookUpValueByKey("userDicPattern");
				jedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("UserDicDataDbNum")));
//				userDicSet.addAll(jedis.lrange(userDicKeyName, 0, -1));
//				
				for (String tempWord : tempWordSet) {
//					// 如果正式词典中没有该词，才加入到正式词典中
//					if (!userDicSet.contains(tempWord)) {
//						jedis.lpush(userDicKeyName, tempWord);
//					}
					
					// 将正式词典的list数据格式改为set数据格式，所以直接覆盖即可
					jedis.sadd(userDicKeyName, tempWord);
				}
			}	
		} catch (Exception ex) {
			logger.error("addTempword2UserDic error:", ex);
			result = false;
		}

		return result;
	} 
	
	
	/**
	 * @Title: isHailiangCanSplit
	 * @Description: 海量分词是否能分出某个词
	 * @author liu_yi
	 * @return
	 * @throws
	 */
	public boolean isHailiangCanSplit(String word) {
		boolean result = false;
		
		String splitword = SplitWordClient.split(word, null);
		int fisrt = splitword.indexOf("(/");
	    int last = splitword.lastIndexOf("(/");
	    if(((splitword.split(" ").length == 1 || fisrt == last))){
	    	//说明原有分词可以将该词进行正确分词，为了不影响分词的正确性，则不再将其作为外挂词进行添加
	    	result = true;
	    }

	    return result;
	}
	
	// test code
	public static void main(String[] args) {
		CommonDataSub cds = new CommonDataSub();
		Thread t = new Thread(cds); 
		t.start();
		
		CustomWordUpdate cwu = CustomWordUpdate.getInstance();
		EntityLibQuery.init();
		
		String tablename = "appitemdb";
		IKVOperationv2 ob = new IKVOperationv2(tablename);
		itemf item = ob.queryItemF("3267385","c");
		
		
		String splitContent = item.getSplitContent();
		String splitTitle = item.getSplitTitle();
		
		Document doc = new Document(splitTitle, splitContent);
		System.out.println(item.getTitle());
		ob.close();
		while (true) {
			DocWordFilter df = new DocWordFilter(doc, false, "", "");
			Document filtered_doc = df.getFilteredDoc();
			KeywordExtract ke = new KeywordExtract(true, splitTitle, splitContent);
			
			List<String> keywords = ke.get_weighted_keywords();
			int featuresNum = keywords.size()/3;
			for (int i = 0; i != featuresNum; i++) {
				int featureValueIndex = i * 3;
				int featureTagIndex = (i * 3) + 1;
				int weightIndex = (i * 3) + 2;
				System.out.println(keywords.get(featureValueIndex) + "-->" + keywords.get(featureTagIndex) + "-->" + keywords.get(weightIndex));
			}
			
			System.out.println("tempCustomDicWordCount:" + tempCustomDicWordCount);
			System.out.println("splitMachine.size:" + splitMachine.size());
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
