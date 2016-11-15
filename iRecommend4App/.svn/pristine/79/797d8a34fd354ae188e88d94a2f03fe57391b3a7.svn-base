package com.ifeng.iRecommend.wuyg.commonData.Update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import redis.clients.jedis.Pipeline;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.EntityLibQuery;
import com.ifeng.iRecommend.wuyg.commonData.Update.CommonSubDataWord.WordState;
import com.ifeng.iRecommend.wuyg.commonData.Update.HotWordSubData.EventState;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;

public class PublisherOperate extends Publisher {

	protected String key;
	private String blacklistPattern = LoadConfig
			.lookUpValueByKey("blacklistPattern");
	private String aliasDataPattern = LoadConfig
			.lookUpValueByKey("aliasDataPattern");
	private String dataExpLibPattern = LoadConfig
			.lookUpValueByKey("dataExpLibPattern");
	private String keySegment = LoadConfig.lookUpValueByKey("keySegment");
	private String cateSegment = LoadConfig.lookUpValueByKey("cateSegment");
	private String aliasSegment = LoadConfig.lookUpValueByKey("aliasSegment");
	private String ExpSegment = LoadConfig.lookUpValueByKey("ExpSegment");

	public PublisherOperate(String channel) {
		super(channel);
	}

	public PublisherOperate(String channel, String key) {
		super(channel);
		this.key = key;
	}

	public PublisherOperate(String host, String port, String channel, String key) {
		super(host, port, channel);
		this.key = key;
	}

	/**
	 * 
	 * @Title:getDataFromRedis
	 * @Description:得到指定key中的所有数据
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月23日
	 */
	public <T> T getDataFromRedis() {
		return getDataFromRedis(new HashMap<String, String>());
	}

	/**
	 * 
	 * @Title:getDataFromRedis
	 * @Description:
	 * @param dataRedisFormatMap
	 *            保存记录在redis中的原始数据格式，以便于删除操作
	 * @return
	 * @author:wuyg1
	 * @date:2016年2月23日
	 */
	public <T> T getDataFromRedis(HashMap<String, String> dataRedisFormatMap) {
		HashMap<String, String> dataMap = new HashMap<String, String>();

		HashMap<String, HashMap<String, HashSet<String>>> aliasMaps = new HashMap<String, HashMap<String, HashSet<String>>>();

		HashMap<String, ArrayList<String>> dataExpLibMaps = new HashMap<String, ArrayList<String>>();

		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));
		List<String> dataList = publisherJedis.lrange(key, 0, -1);
		if (this.key.equals(blacklistPattern)) {
			for (String elem : dataList) {
				String word = elem.substring(0, elem.lastIndexOf(LoadConfig
						.lookUpValueByKey("fieldDelimiter")));
				dataMap.put(word, elem);
			}
			return (T) dataMap;
		} else if (this.key.equals(aliasDataPattern)) {

			for (String elem : dataList) {
				String keySegment = LoadConfig.lookUpValueByKey("keySegment");

				String[] elems = elem.split(keySegment);

				aliasMaps.put(elems[0], createCateAliasMap(elem));
				dataRedisFormatMap.put(elems[0], elem);
			}
			return (T) aliasMaps;

		} else if (this.key.equals(dataExpLibPattern)) {

			for (String elem : dataList) {
				String keySegment = LoadConfig.lookUpValueByKey("keySegment");

				String[] elems = elem.split(keySegment);

				ArrayList<String> ExpLibList = new ArrayList<String>();
				ExpLibList.addAll(Arrays.asList(elems[1].split(LoadConfig
						.lookUpValueByKey("ExpSegment"))));

				dataExpLibMaps.put(elems[0], ExpLibList);
				dataRedisFormatMap.put(elems[0], elem);
			}
			return (T) dataExpLibMaps;

		} else {
			for (String elem : dataList) {
				String word = elem.split(LoadConfig
						.lookUpValueByKey("fieldDelimiter"))[2];
				dataMap.put(word, elem);
			}
			return (T) dataMap;
		}
	}

	/**
	 * 
	 * @Title:checkWordFromRedis
	 * @Description:检查redis是否存在相应的word
	 * @param words
	 * @author:wuyg1
	 * @date:2015年12月23日
	 */
	public void checkWordFromRedis(ArrayList<String> words) {
		HashMap<String, String> dataMap = new HashMap<String, String>();
		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));
		dataMap = this.getDataFromRedis();
		for (String word : words) {
			logger.info("search word :" + word);
			if (dataMap.containsKey(word)) {
				logger.info("the redis containce:" + word);
			} else {
				logger.info("the redis not containce:" + word);
			}
		}
	}

	@Override
	public String updateWord_Add(List<String> wordInfos) {
		// TODO Auto-generated method stub
		return updateWord_Add(wordInfos, null, new ArrayList<String>());
	}

	@Override
	public String updateWord_Add(List<String> data, String state) {
		// TODO Auto-generated method stub
		return updateWord_Add(data, state, new ArrayList<String>());
	}

	@Override
	public String updateWord_Add(List<String> data, String state, String time) {
		// TODO Auto-generated method stub
		return updateWord_Add(data, state, new ArrayList<String>(), time);
	}

	@Override
	public String updateWord_Add(List<String> wordInfos, String state,
			ArrayList<String> userDicList) {

		return updateWord_Add(wordInfos, state, userDicList, null);

	}

	public String updateWord_Add(List<String> data, String state, String time,
			HashMap<String, WordReadableSubData> wordReadMaps) {
		// TODO Auto-generated method stub
		return updateWord_Add(data, state, new ArrayList<String>(), time,
				wordReadMaps);
	}
/**
 * 
* @Title:updatedWord_Add
* @Description:该方法用于处理无效/有效外挂词的添加
* @param wordSet
* @author:wuyg1
* @date:2016年8月9日
 */
	public void updatedWord_Add(HashSet<String> wordSet){
		
		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));
		
		Pipeline pipeline = publisherJedis.pipelined();
		
		String [] wordStr = new String[wordSet.size()];
		
		wordSet.toArray(wordStr);
		
		pipeline.sadd(key, wordStr);
		
		pipeline.sync();
		
	}
	
	
	public String updateWord_Add(List<String> wordInfos, String state,
			ArrayList<String> userDicList, String time,
			HashMap<String, WordReadableSubData> wordReadMaps) {
		int addcount = 0;

		String message = new String();

		StringBuffer sBuffer = new StringBuffer();
		if (null == time) {
			time = commenFuncs.date2Longstr(new Date());
		}

		HashMap<String, String> dataMap = new HashMap<String, String>();

		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));

		Pipeline pipeline = publisherJedis.pipelined();
		long oldsize = -1;

		dataMap = this.getDataFromRedis();

		oldsize = publisherJedis.llen(key);

		for (String word : wordInfos) {
			WordReadableSubData wordInfo = null;

			if (null != wordReadMaps && wordReadMaps.containsKey(word)) {
				int clientSubWordCount = wordReadMaps.get(word)
						.getClientSubWordCount();
				int otherSouceCount = wordReadMaps.get(word)
						.getOtherSouceCount();
				wordInfo = new WordReadableSubData(this.key, state, word, time,
						clientSubWordCount, otherSouceCount);
			} else {
				wordInfo = new WordReadableSubData(this.key, state, word, time);
			}

			if (!dataMap.containsKey(word)) {
				dataMap.put(word, wordInfo.toString());
				addcount++;
				userDicList.add(word);

				sBuffer.append(wordInfo.toString()
						+ commonDataUpdateConfig.recordDelimiter);

				pipeline.lpush(key, wordInfo.toString());

			} else if (dataMap.containsKey(word)) {
				String info = dataMap.get(word);
				if (CommonSubDataWord.isreadCheck(info) == true) {
					publisherJedis.lrem(key, 0, dataMap.get(word));
					publisherJedis.lpush(key, wordInfo.toString());

					if (wordInfo.getWordState().equals(WordState.unread.name())) {
						sBuffer.append(wordInfo.toString()
								+ commonDataUpdateConfig.recordDelimiter);
					}

				}

			}

		}

		pipeline.sync();

		if (null == sBuffer || sBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "add#DIV#elem#DIV#" + sBuffer.toString();
		}

		long newsize = publisherJedis.llen(key);

		// if (((oldsize + addcount) != newsize)) {
		// logger.error("updateInfo_Add exception:\tkey" + key + "\toldsize:"
		// + oldsize + " \taddsize:" + addcount + "\taddrecord:"
		// + wordInfos.size() + "\tnewsize:" + newsize);
		// message = null;
		// }

		return message;
	}

	
	public String updateWord_Add(List<String> wordInfos, String state,
			ArrayList<String> userDicList, String time,boolean isEvent) {
		int addcount = 0;

		String message = new String();

		StringBuffer sBuffer = new StringBuffer();
		if (null == time) {
			time = commenFuncs.date2Longstr(new Date());
		}

		HashMap<String, String> dataMap = new HashMap<String, String>();
		HashMap<String, HashMap<String, HashSet<String>>> aliasMaps = new HashMap<String, HashMap<String, HashSet<String>>>();
		HashMap<String, ArrayList<String>> dataExpLibMaps = new HashMap<String, ArrayList<String>>();
		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));

		Pipeline pipeline = publisherJedis.pipelined();
		long oldsize = -1;

		if (this.key.equals(aliasDataPattern)) {
			aliasMaps = this.getDataFromRedis(dataMap);
		} else if (this.key.equals(dataExpLibPattern)) {
			dataExpLibMaps = this.getDataFromRedis();
		} else {
			dataMap = this.getDataFromRedis();
		}
		oldsize = publisherJedis.llen(key);

		for (String word : wordInfos) {
			CommonSubDataWord wordInfo = null;
			HotWordSubData hotWordSubData = null;
			if (this.key.equals(blacklistPattern)) {
				wordInfo = new CommonSubDataWord(null, state, word, time);
			} else if (this.key.equals(commonDataUpdateConfig.hotWordPattern)) {

				String documentId = null;

				if (word.contains("#DIV#")) {
					documentId = word.substring(word.indexOf("#DIV#")
							+ "#DIV#".length());
					word = word.substring(0, word.indexOf("#DIV#"));

				}

				ArrayList<EntityInfo> entityInfos = EntityLibQuery
						.getEntityList(word);
				if (entityInfos == null || entityInfos.isEmpty()) {

					hotWordSubData = new HotWordSubData(this.key, state, word,
							documentId, null, time,isEvent);

				} else {

					hotWordSubData = new HotWordSubData(this.key,
							WordState.unread.name(), word, documentId, null,
							time,isEvent);
				}
			} else if (this.key.equals(aliasDataPattern)
					|| this.key.equals(dataExpLibPattern)) {

				wordInfo = new CommonSubDataWord(null, null, word, null);
			} else {
				wordInfo = new CommonSubDataWord(this.key, state, word, time);
			}

			if (this.key.equals(aliasDataPattern)) {

				String keySegment = LoadConfig.lookUpValueByKey("keySegment");

				String keyWord = word.split(keySegment)[0];


				HashMap<String, HashSet<String>> add_cateHashMap = createCateAliasMap(word);
				
				//keyWord = keyWord.toLowerCase();
				
				if (!aliasMaps.containsKey(keyWord)) {
					aliasMaps.put(keyWord, add_cateHashMap);
					addcount++;
					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);

					pipeline.lpush(key, wordInfo.toString());

				} else if (aliasMaps.containsKey(keyWord)) {

					String removeStr = dataMap.get(keyWord);
					publisherJedis.lrem(key, 0, removeStr);
					
//					pipeline.lrem(
//							key,
//							0,
//							aliasDataObject2String(keyWord,
//									aliasMaps.get(keyWord)));

					HashMap<String, HashSet<String>> cateHashMap = aliasMaps
							.get(keyWord);

					Iterator<String> iterator = add_cateHashMap.keySet()
							.iterator();
					while (iterator.hasNext()) {
						String category = iterator.next();
						if (cateHashMap.containsKey(category)) {
							cateHashMap.get(category).addAll(
									add_cateHashMap.get(category));
						} else {
							cateHashMap.put(category,
									add_cateHashMap.get(category));
						}
					}

					publisherJedis.lpush(key,
							aliasDataObject2String(keyWord, cateHashMap));

					sBuffer.append(word
							+ commonDataUpdateConfig.recordDelimiter);

				}
			} else if (this.key.equals(dataExpLibPattern)) {

				String keySegment = LoadConfig.lookUpValueByKey("keySegment");

				String[] elemSplit = word.split(keySegment);

				String keyWord = elemSplit[0];

				ArrayList<String> ExpLibList = new ArrayList<String>();
				ExpLibList.addAll(Arrays.asList(elemSplit[1].split(LoadConfig
						.lookUpValueByKey("ExpSegment"))));

				if (dataExpLibMaps.containsKey(keyWord)) {
					publisherJedis.lrem(
							key,
							0,
							ExpData2String(keyWord, dataExpLibMaps.get(keyWord)));

					publisherJedis.lpush(key, ExpData2String(keyWord, ExpLibList));

				} else if (!dataExpLibMaps.containsKey(keyWord)) {
					dataExpLibMaps.put(keyWord, ExpLibList);
					addcount++;
					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);
					pipeline.lpush(key, ExpData2String(keyWord, ExpLibList));
				}

			} else if (this.key.equals(LoadConfig
					.lookUpValueByKey("hotWordPattern"))) {

				if (!dataMap.containsKey(word)) {
					if (this.key.equals(commonDataUpdateConfig.hotWordPattern)) {
						hotWordSubData.setStarttime(time);
					}

					dataMap.put(word, hotWordSubData.toString());
					addcount++;

					sBuffer.append(hotWordSubData.toString()
							+ commonDataUpdateConfig.recordDelimiter);

					pipeline.lpush(key, hotWordSubData.toString());

				} else if (dataMap.containsKey(word)) {
					String info = dataMap.get(word);
					if (this.key.equals(commonDataUpdateConfig.hotWordPattern)) {

						String eventType = info.split(LoadConfig.lookUpValueByKey("fieldDelimiter"))[3];
						
						if(eventType.contains("#EVENT#")){
							
							//如果相同记录的事件为AbstractEvent，则不进行替换
							
							eventType = eventType.substring(0, eventType.indexOf("#EVENT#"));
							
							if(eventType.equals(EventState.AbstractEvent.name())){
								continue;
							}
							
						}
						
						
						String starttime = info.split(LoadConfig
								.lookUpValueByKey("fieldDelimiter"))[3];
						if (starttime.contains("#TIME#")) {
							starttime = starttime.substring(starttime
									.indexOf("#TIME#") + "#TIME#".length());
						} else {
							starttime = null;
						}
						hotWordSubData.setStarttime(starttime);
					}

					if (CommonSubDataWord.isreadCheck(info) == true) {
						publisherJedis.lrem(key, 0, dataMap.get(word));
						publisherJedis.lpush(key, hotWordSubData.toString());

						sBuffer.append(hotWordSubData.toString()
								+ commonDataUpdateConfig.recordDelimiter);

					}

				}

			} else {
				if (!dataMap.containsKey(word)) {
					dataMap.put(word, wordInfo.toString());
					addcount++;
					userDicList.add(word);

					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);

					pipeline.lpush(key, wordInfo.toString());

				} else if (dataMap.containsKey(word)) {
					String info = dataMap.get(word);
					if (CommonSubDataWord.isreadCheck(info) == true) {
						publisherJedis.lrem(key, 0, dataMap.get(word));
						publisherJedis.lpush(key, wordInfo.toString());

					}

				}
			}

		}

		pipeline.sync();

		if (null == sBuffer || sBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "add#DIV#elem#DIV#" + sBuffer.toString();
		}

		long newsize = publisherJedis.llen(key);

		// if (((oldsize + addcount) != newsize)) {
		// logger.error("updateInfo_Add exception:\tkey" + key + "\toldsize:"
		// + oldsize + " \taddsize:" + addcount + "\taddrecord:"
		// + wordInfos.size() + "\tnewsize:" + newsize);
		// message = null;
		// }

		return message;
	}
	
	
	@Override
	public String updateWord_Add(List<String> wordInfos, String state,
			ArrayList<String> userDicList, String time) {
		int addcount = 0;

		String message = new String();

		StringBuffer sBuffer = new StringBuffer();
		if (null == time) {
			time = commenFuncs.date2Longstr(new Date());
		}

		HashMap<String, String> dataMap = new HashMap<String, String>();
		HashMap<String, HashMap<String, HashSet<String>>> aliasMaps = new HashMap<String, HashMap<String, HashSet<String>>>();
		HashMap<String, ArrayList<String>> dataExpLibMaps = new HashMap<String, ArrayList<String>>();
		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));

		Pipeline pipeline = publisherJedis.pipelined();
		long oldsize = -1;

		if (this.key.equals(aliasDataPattern)) {
			aliasMaps = this.getDataFromRedis(dataMap);
		} else if (this.key.equals(dataExpLibPattern)) {
			dataExpLibMaps = this.getDataFromRedis();
		} else {
			dataMap = this.getDataFromRedis();
		}
		oldsize = publisherJedis.llen(key);

		for (String word : wordInfos) {
			CommonSubDataWord wordInfo = null;
			HotWordSubData hotWordSubData = null;
			if (this.key.equals(blacklistPattern)) {
				wordInfo = new CommonSubDataWord(null, state, word, time);
			} else if (this.key.equals(commonDataUpdateConfig.hotWordPattern)) {

				String documentId = null;

				if (word.contains("#DIV#")) {
					documentId = word.substring(word.indexOf("#DIV#")
							+ "#DIV#".length());
					word = word.substring(0, word.indexOf("#DIV#"));

				}

				ArrayList<EntityInfo> entityInfos = EntityLibQuery
						.getEntityList(word);
				if (entityInfos == null || entityInfos.isEmpty()) {

					hotWordSubData = new HotWordSubData(this.key, state, word,
							documentId, null, time);

				} else {

					hotWordSubData = new HotWordSubData(this.key,
							WordState.unread.name(), word, documentId, null,
							time);
				}
			} else if (this.key.equals(aliasDataPattern)
					|| this.key.equals(dataExpLibPattern)) {

				wordInfo = new CommonSubDataWord(null, null, word, null);
			} else {
				wordInfo = new CommonSubDataWord(this.key, state, word, time);
			}

			if (this.key.equals(aliasDataPattern)) {

				String keySegment = LoadConfig.lookUpValueByKey("keySegment");

				String keyWord = word.split(keySegment)[0];


				HashMap<String, HashSet<String>> add_cateHashMap = createCateAliasMap(word);
				
				//keyWord = keyWord.toLowerCase();
				
				if (!aliasMaps.containsKey(keyWord)) {
					aliasMaps.put(keyWord, add_cateHashMap);
					addcount++;
					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);

					pipeline.lpush(key, wordInfo.toString());

				} else if (aliasMaps.containsKey(keyWord)) {

					String removeStr = dataMap.get(keyWord);
					publisherJedis.lrem(key, 0, removeStr);
					
//					pipeline.lrem(
//							key,
//							0,
//							aliasDataObject2String(keyWord,
//									aliasMaps.get(keyWord)));

					HashMap<String, HashSet<String>> cateHashMap = aliasMaps
							.get(keyWord);

					Iterator<String> iterator = add_cateHashMap.keySet()
							.iterator();
					while (iterator.hasNext()) {
						String category = iterator.next();
						if (cateHashMap.containsKey(category)) {
							cateHashMap.get(category).addAll(
									add_cateHashMap.get(category));
						} else {
							cateHashMap.put(category,
									add_cateHashMap.get(category));
						}
					}

					publisherJedis.lpush(key,
							aliasDataObject2String(keyWord, cateHashMap));

					sBuffer.append(word
							+ commonDataUpdateConfig.recordDelimiter);

				}
			} else if (this.key.equals(dataExpLibPattern)) {

				String keySegment = LoadConfig.lookUpValueByKey("keySegment");

				String[] elemSplit = word.split(keySegment);

				String keyWord = elemSplit[0];

				ArrayList<String> ExpLibList = new ArrayList<String>();
				ExpLibList.addAll(Arrays.asList(elemSplit[1].split(LoadConfig
						.lookUpValueByKey("ExpSegment"))));

				if (dataExpLibMaps.containsKey(keyWord)) {
					publisherJedis.lrem(
							key,
							0,
							ExpData2String(keyWord, dataExpLibMaps.get(keyWord)));

					publisherJedis.lpush(key, ExpData2String(keyWord, ExpLibList));

				} else if (!dataExpLibMaps.containsKey(keyWord)) {
					dataExpLibMaps.put(keyWord, ExpLibList);
					addcount++;
					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);
					pipeline.lpush(key, ExpData2String(keyWord, ExpLibList));
				}

			} else if (this.key.equals(LoadConfig
					.lookUpValueByKey("hotWordPattern"))) {

				if (!dataMap.containsKey(word)) {
					if (this.key.equals(commonDataUpdateConfig.hotWordPattern)) {
						hotWordSubData.setStarttime(time);
					}

					dataMap.put(word, hotWordSubData.toString());
					addcount++;

					sBuffer.append(hotWordSubData.toString()
							+ commonDataUpdateConfig.recordDelimiter);

					pipeline.lpush(key, hotWordSubData.toString());

				} else if (dataMap.containsKey(word)) {
					String info = dataMap.get(word);
					if (this.key.equals(commonDataUpdateConfig.hotWordPattern)) {

						String starttime = info.split(LoadConfig
								.lookUpValueByKey("fieldDelimiter"))[3];
						if (starttime.contains("#TIME#")) {
							starttime = starttime.substring(starttime
									.indexOf("#TIME#") + "#TIME#".length());
						} else {
							starttime = null;
						}
						hotWordSubData.setStarttime(starttime);
					}

					if (CommonSubDataWord.isreadCheck(info) == true) {
						publisherJedis.lrem(key, 0, dataMap.get(word));
						publisherJedis.lpush(key, hotWordSubData.toString());

						sBuffer.append(hotWordSubData.toString()
								+ commonDataUpdateConfig.recordDelimiter);

					}

				}

			} else {
				if (!dataMap.containsKey(word)) {
					dataMap.put(word, wordInfo.toString());
					addcount++;
					userDicList.add(word);

					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);

					pipeline.lpush(key, wordInfo.toString());

				} else if (dataMap.containsKey(word)) {
					String info = dataMap.get(word);
					if (CommonSubDataWord.isreadCheck(info) == true) {
						publisherJedis.lrem(key, 0, dataMap.get(word));
						publisherJedis.lpush(key, wordInfo.toString());

					}

				}
			}

		}

		pipeline.sync();

		if (null == sBuffer || sBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "add#DIV#elem#DIV#" + sBuffer.toString();
		}

		long newsize = publisherJedis.llen(key);

		// if (((oldsize + addcount) != newsize)) {
		// logger.error("updateInfo_Add exception:\tkey" + key + "\toldsize:"
		// + oldsize + " \taddsize:" + addcount + "\taddrecord:"
		// + wordInfos.size() + "\tnewsize:" + newsize);
		// message = null;
		// }

		return message;
	}

	@Override
	public String updateWord_Alter_undo(List<String> data, String state) {

		if (state.equals(WordState.read.name())) {
			state = WordState.unread.name();
		} else if (state.equals(WordState.unread)) {
			state = WordState.read.name();
		}
		return updateWord_Alter(data, state);
	}

	@Override
	public String updateWord_Alter(List<String> wordInfos, String state) {

		return updateWord_Alter(wordInfos, state, new ArrayList<String>());
	}

	@Override
	public String updateWord_Alter(List<String> wordInfos, String state,
			ArrayList<String> userDicList) {
		String message = new String();

		StringBuffer sBuffer = new StringBuffer();
		String time = commenFuncs.date2Longstr(new Date());

		HashMap<String, String> dataMap = new HashMap<String, String>();
		HashMap<String, HashMap<String, HashSet<String>>> aliasMaps = new HashMap<String, HashMap<String, HashSet<String>>>();
		HashMap<String, ArrayList<String>> dataExpLibMaps = new HashMap<String, ArrayList<String>>();

		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));

		Pipeline pipeline = publisherJedis.pipelined();

		long oldsize = -1;

		if (this.key.equals(aliasDataPattern)) {
			aliasMaps = this.getDataFromRedis(dataMap);

		} else if (this.key.equals(dataExpLibPattern)) {
			dataExpLibMaps = this.getDataFromRedis(dataMap);

		} else {
			dataMap = this.getDataFromRedis();

		}
		oldsize = publisherJedis.llen(key);
		for (String word : wordInfos) {

			if (this.key.equals(aliasDataPattern)) {

				CommonSubDataWord wordInfo = new CommonSubDataWord(null, null,
						word, null);

				String keySegment = LoadConfig.lookUpValueByKey("keySegment");

				String keyWord = word.split(keySegment)[0];

				HashMap<String, HashSet<String>> altercateHashMap = createCateAliasMap(word);

				if (aliasMaps.containsKey(keyWord)) {

					String removeStr = dataMap.get(keyWord);
					
					publisherJedis.lrem(key, 0, removeStr);

					publisherJedis.lpush(key, wordInfo.toString());

					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);

				}

			} else if (this.key.equals(dataExpLibPattern)) {

				CommonSubDataWord wordInfo = new CommonSubDataWord(null, null,
						word, null);

				String[] datas = word.split(LoadConfig
						.lookUpValueByKey("keySegment"));
				ArrayList<String> newExpLibList = new ArrayList<String>();
				newExpLibList.addAll(Arrays.asList(datas[1].split(LoadConfig
						.lookUpValueByKey("ExpSegment"))));
				if (dataExpLibMaps.containsKey(datas[0])) {

					publisherJedis.lrem(key, 0, dataMap.get(datas[0]));

					String record = ExpData2String(datas[0], newExpLibList);
					publisherJedis.lpush(key, record);

					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);
				}

			} else {
				CommonSubDataWord wordInfo = new CommonSubDataWord(this.key,
						state, word, time);
				if (dataMap.containsKey(word)) {
					System.err.println(dataMap.get(word));
					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);
					publisherJedis.lrem(key, 0, dataMap.get(word));
					publisherJedis.lpush(key, wordInfo.toString());
				}
			}

		}

		pipeline.sync();
		if (null == sBuffer || sBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "alter#DIV#elem#DIV#" + sBuffer.toString();
		}
		long newsize = publisherJedis.llen(key);

		// if (oldsize != newsize) {
		// logger.error("updateInfo_Add exception:\tkey" + key + "\toldsize:"
		// + oldsize + "\taddrecord:" + wordInfos.size()
		// + "\tnewsize:" + newsize);
		// message = null;
		// }

		return message;
	}

	public void updateWord_Del(String regex) {

		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));

		List<String> dataList = publisherJedis.lrange(key, 0, -1);

		for (String data : dataList) {
			if (data.contains(regex)) {
				publisherJedis.lrem(key, 0, data);
			}
		}
		long size = publisherJedis.llen(key);
		System.err.println(size);
	}

	@Override
	public String updateWord_Del(List<String> dataList, String state) {

		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));

		Pipeline pipeline = publisherJedis.pipelined();
		String message = new String();
		int delcount = 0;
		StringBuffer sBuffer = new StringBuffer();
		HashMap<String, String> dataMap = new HashMap<String, String>();
		long oldsize = -1;
		if (this.key.equals(aliasDataPattern)
				|| this.key.equals(dataExpLibPattern)) {
			// 如果是主别名库或者信息全表达库，那么只需存储其在redis中的存放内容，以便于删除即可
			this.getDataFromRedis(dataMap);
		} else {
			dataMap = getDataFromRedis();
		}

		oldsize = publisherJedis.llen(key);
		String time = commenFuncs.date2Longstr(new Date());
		for (String word : dataList) {
			CommonSubDataWord wordInfo = null;
			if (this.key.equals(blacklistPattern)) {
				wordInfo = new CommonSubDataWord(null, null, word, time);
			} else if (this.key.equals(aliasDataPattern)
					|| this.key.equals(dataExpLibPattern)) {
				wordInfo = new CommonSubDataWord(null, null, word, null);
			} else if (this.key.equals(commonDataUpdateConfig.hotWordPattern)) {
				String documentId = null;

				if (word.contains("#DIV#")) {
					documentId = word.substring(word.indexOf("#DIV#")
							+ "#DIV#".length());
					word = word.substring(0, word.indexOf("#DIV#"));

				}
				wordInfo = new HotWordSubData(this.key, WordState.read.name(),
						word, documentId, null, time);

			} else {
				wordInfo = new CommonSubDataWord(this.key,
						WordState.read.name(), word, time);
			}

			if (this.key.equals(aliasDataPattern)) {

				String keyWord = word.split(keySegment)[0];
				if (dataMap.containsKey(keyWord)) {
					delcount++;
					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);

					String aliasCon = dataMap.get(keyWord);

					String newCon = aliasRecordUpdate(word, aliasCon);

					publisherJedis.lrem(key, 0, aliasCon);

					if (null == newCon || newCon.isEmpty()
							|| newCon.length() == 0) {
						continue;
					} else {
						newCon = keyWord + keySegment + newCon;
						publisherJedis.lpush(key, newCon);
					}

				}

			}else if(this.key.equals(dataExpLibPattern)){
				String keyWord = word.split(keySegment)[0];
				if (dataMap.containsKey(keyWord)) {
					delcount++;
					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);

					String expOldCon = dataMap.get(keyWord);

					String newCon = dataExpRecordUpdate(word, expOldCon);

					publisherJedis.lrem(key, 0, expOldCon);

					if (null == newCon || newCon.isEmpty()
							|| newCon.length() == 0) {
						continue;
					} else {
						newCon = keyWord + newCon;
						publisherJedis.lpush(key, newCon);
					}

				}
			}else {
				if (dataMap.containsKey(wordInfo.getValue())) {
					delcount++;
					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);
					pipeline.lrem(key, 0, dataMap.get(wordInfo.getValue()));
				} else {
					sBuffer.append(wordInfo.toString()
							+ commonDataUpdateConfig.recordDelimiter);
				}
			}

		}

		pipeline.sync();
		if (null == sBuffer || sBuffer.toString().isEmpty()) {
			message = null;
		} else {
			message = "del#DIV#elem#DIV#" + sBuffer.toString();
		}
		long newsize = publisherJedis.llen(key);

		// if ((oldsize - delcount) != newsize) {
		// logger.error("updateInfo_Del exception:\toldsize:" + oldsize
		// + " \tdelsize:" + delcount + "\tnewsize:" + newsize);
		// message = null;
		// }

		return message;
	}

	public String aliasRecordUpdate(String newAliasCon, String oldAliasCon) {
		HashMap<String, HashSet<String>> newcategoryMap = aliasFormatChange(newAliasCon);
		HashMap<String, HashSet<String>> oldcategoryMap = aliasFormatChange(oldAliasCon);

		Iterator<String> newIterator = newcategoryMap.keySet().iterator();
		while (newIterator.hasNext()) {
			String cateKey = newIterator.next();
			HashSet<String> nickSet = newcategoryMap.get(cateKey);
			if (oldcategoryMap.containsKey(cateKey)) {
				HashSet<String> oldnickSet = oldcategoryMap.get(cateKey);

				Iterator<String> iterator = nickSet.iterator();

				while (iterator.hasNext()) {
					String nick = iterator.next();
					if (oldnickSet.contains(nick)) {
						oldnickSet.remove(nick);
					}
				}
			}
		}
		return aliasMapFormat2Str(oldcategoryMap);
	}
	
	public String dataExpRecordUpdate(String newExpCon, String oldExpCon) {
		HashSet<String> newExpSet = dataExpFormatChange(newExpCon);
		HashSet<String> oldcategorySet = dataExpFormatChange(oldExpCon);

		StringBuffer stringBuffer = new StringBuffer();
		
		if(null == newExpSet || newExpSet.isEmpty()){
		
			return null;
		}
		
        for(String newWord : newExpSet){
        	if(oldcategorySet.contains(newWord)){
        		continue;
        	}else{
        		if(null == newWord || newWord.isEmpty()){
        			continue;
        		}
        		stringBuffer.append(newWord+ExpSegment);
        	}
        }
        return stringBuffer.toString();
	}
	

	public HashSet<String> dataExpFormatChange(String dataExp) {

		dataExp = dataExp.substring(dataExp.indexOf(keySegment)
				+ keySegment.length());

		dataExp = dataExp.trim();
		
		if(null == dataExp || dataExp.isEmpty()){
			return null;
		}
		
		String[] expCons = dataExp.split(ExpSegment);
		HashSet<String> expSet = new HashSet<String>();
		expSet.addAll(Arrays.asList(expCons));
		return expSet;
	}
	
	public HashMap<String, HashSet<String>> aliasFormatChange(String aliasCon) {
		HashMap<String, HashSet<String>> categoryMaps = new HashMap<String, HashSet<String>>();

		aliasCon = aliasCon.substring(aliasCon.indexOf(keySegment)
				+ keySegment.length());

		aliasCon = aliasCon.trim();
		
		if(null == aliasCon || aliasCon.isEmpty()){
			return null;
		}
		
		String[] cateCons = aliasCon.split(cateSegment);
		for (String categoryCon : cateCons) {
			String category = categoryCon.substring(0,
					categoryCon.indexOf(aliasSegment));

			String nickCon = categoryCon.substring(categoryCon.indexOf("[")
					+ "[".length(), categoryCon.indexOf("]"));

			HashSet<String> nickSet = new HashSet<String>();
			nickSet.addAll(Arrays.asList(nickCon.split(", ")));

			categoryMaps.put(category, nickSet);

		}
		return categoryMaps;
	}

	/**
	 * 
	 * @Title:aliasMapFormat2Str
	 * @Description: 主别名的Map格式转为字符串拼接格式
	 * @param categoryMap
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月12日
	 */
	public String aliasMapFormat2Str(
			HashMap<String, HashSet<String>> categoryMap) {
		StringBuffer sBuffer = new StringBuffer();
		Iterator<String> iterator = categoryMap.keySet().iterator();
		while (iterator.hasNext()) {
			String category = iterator.next();

			if (null == categoryMap.get(category)
					|| 0 == categoryMap.get(category).size()
					|| categoryMap.get(category).isEmpty()) {
				continue;
			}
			sBuffer.append(category + aliasSegment);
			sBuffer.append(categoryMap.get(category) + cateSegment);
		}
		return sBuffer.toString();
	}

	@Override
	public boolean redisInit(List<String> dataList) {
		publisherJedis.select(Integer.valueOf(LoadConfig
				.lookUpValueByKey("commonDataDbNum")));

		Pipeline pipeline = publisherJedis.pipelined();

		long oldsize = publisherJedis.llen(key);

		String time = commenFuncs.date2Longstr(new Date());
		for (String wInfo : dataList) {

			CommonSubDataWord word = new CommonSubDataWord(this.key,
					WordState.read.name(), wInfo, time);
			pipeline.lpush(this.key, word.toString());
		}

		pipeline.sync();

		long newsize = publisherJedis.llen(this.key);

		if ((oldsize != newsize)) {
			logger.error("InitRedis exception : \toldsize:" + oldsize
					+ "\tnewsize:" + newsize);
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @Title:getdataArrays
	 * @Description:HashMap结构的value值转为String数组
	 * @param dataMap
	 * @param dataArrays
	 * @return String[]
	 * @author:wuyg1
	 * @date:2015年12月18日
	 */
	public String[] getdataArrays(HashMap<String, String> dataMap,
			String[] dataArrays) {
		Iterator<String> iterator = dataMap.keySet().iterator();
		int index = 0;
		while (iterator.hasNext()) {
			String key = iterator.next();
			dataArrays[index++] = dataMap.get(key);
		}
		return dataArrays;
	}

	/**
	 * 
	 * @Title:aliasDataObject2String
	 * @Description: 将一条主别名记录的格式转为字符串,目的是从redis中将其删除掉
	 * @param key
	 * @param cateHashMap
	 * @return
	 * @author:wuyg1
	 * @date:2016年2月22日
	 */
	public String aliasDataObject2String(String key,
			HashMap<String, HashSet<String>> cateHashMap) {
		Iterator<String> iterator = cateHashMap.keySet().iterator();

		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(key + LoadConfig.lookUpValueByKey("keySegment"));
		// keySegment=#KEY#
		// cateSegment=#CAT#
		// aliasSegment=#ALIAS#

		while (iterator.hasNext()) {
			String category = iterator.next();
			HashSet<String> aliaSet = cateHashMap.get(category);
			sBuffer.append(category
					+ LoadConfig.lookUpValueByKey("aliasSegment") + aliaSet
					+ LoadConfig.lookUpValueByKey("cateSegment"));
		}

		return sBuffer.toString();

	}

	/**
	 * 
	 * @Title:ExpData2String
	 * @Description: 将全表达数据结构转为字符串
	 * @param keyWord
	 * @param ExpLibList
	 * @return
	 * @author:wuyg1
	 * @date:2016年2月23日
	 */
	public String ExpData2String(String keyWord, ArrayList<String> ExpLibList) {

		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(keyWord + LoadConfig.lookUpValueByKey("keySegment"));
		for (String name : ExpLibList) {
			sBuffer.append(name + LoadConfig.lookUpValueByKey("ExpSegment"));
		}
		return sBuffer.toString();
	}

	/**
	 * 
	 * @Title:getSourceData
	 * @Description: 遍历filepath文件夹中的所有文件数据，将数据以List格式返回
	 * @param filepath
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月18日
	 */
	public List<String> getSourceData(String filepath) {
		FileUtil fileUtil = new FileUtil();
		ArrayList<String> dataList = new ArrayList<String>();
		ArrayList<String> fileList = fileUtil.refreshFileList(filepath);
		for (String filename : fileList) {
			String content = fileUtil.Read(filename, "utf-8");
			dataList.addAll(Arrays.asList(content.split("\n")));
		}
		return dataList;
	}

	@Override
	public String updateWord_Del(List<String> data, String state,
			ArrayList<String> userDicList) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @Title:createCateAliasMap
	 * @Description:生成一个主名对应的所有别名
	 * @param elem
	 * @return
	 * @author:wuyg1
	 * @date:2016年2月19日
	 */
	public HashMap<String, HashSet<String>> createCateAliasMap(String elem) {

		String keySegment = LoadConfig.lookUpValueByKey("keySegment");
		String cateSegment = LoadConfig.lookUpValueByKey("cateSegment");
		String aliasSegment = LoadConfig.lookUpValueByKey("aliasSegment");

		String[] elems = elem.split(keySegment);
		String key = elems[0];

		HashMap<String, HashSet<String>> cateAliasMap = new HashMap<String, HashSet<String>>();
		String[] categorys = elems[1].split(cateSegment);
		for (String categoryCon : categorys) {

			String[] catekeys = categoryCon.split(aliasSegment);
			String category = catekeys[0];
			categoryCon = categoryCon.substring(categoryCon.indexOf("[") + 1,
					categoryCon.indexOf("]"));
			HashSet<String> aliasSet = new HashSet<String>();
			aliasSet.addAll(Arrays.asList(categoryCon.split(", ")));
			
			cateAliasMap.put(category, aliasSet);
		}
		return cateAliasMap;
	}
}
