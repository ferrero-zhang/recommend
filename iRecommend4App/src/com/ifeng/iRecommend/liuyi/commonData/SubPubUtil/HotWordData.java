package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.CommonSubDataWord.WordState;

import redis.clients.jedis.Jedis;

public class HotWordData extends LocalDataUpdate {

	private static Log LOG = LogFactory.getLog(HotWordData.class);

	private ConcurrentHashMap<String, HotWordInfo> hotwordMap = new ConcurrentHashMap<String, HotWordInfo>();
	private static HotWordData INSTANCE = null;
	public AtomicLong hotWordchangeTime = new AtomicLong(-1);

	public class HotWordInfo {
		boolean isRead;// 热词是否可读
		long starttimestamp;
		long latesttimestamp;
		String splitContent;// 分词后的结果
		String documentId; // 热点事件对应的ID，这个主要是用于张阳从客户端抓取的专题事件标注用的
		String eventType; // 事件类型

		public HotWordInfo(boolean isRead, long starttimestamp,
				long latesttimestamp, String splitContent, String documentId) {
			this(isRead, starttimestamp, latesttimestamp, splitContent);

			setDocumentId(documentId);
		}

		public HotWordInfo(boolean isRead, long starttimestamp,
				long latesttimestamp, String splitContent, String documentId,
				String eventType) {
			this(isRead, starttimestamp, latesttimestamp, splitContent,
					documentId);
			setEventType(eventType);
		}

		public HotWordInfo(boolean isRead, long starttimestamp,
				long latesttimestamp, String splitContent) {
			setRead(isRead);
			setStarttimestamp(starttimestamp);
			setLatesttimestamp(latesttimestamp);
			setSplitContent(splitContent);
		}

		public HotWordInfo(boolean isRead, long starttimestamp,
				long latesttimestamp, String splitContent, boolean isEvent) {
			this(isRead, starttimestamp, latesttimestamp, splitContent);
			setEventType(eventType);
		}

		public boolean isRead() {
			return isRead;
		}

		public void setRead(boolean isRead) {
			this.isRead = isRead;
		}

		public long getStarttimestamp() {
			return starttimestamp;
		}

		public void setStarttimestamp(long starttimestamp) {
			this.starttimestamp = starttimestamp;
		}

		public long getLatesttimestamp() {
			return latesttimestamp;
		}

		public void setLatesttimestamp(long latesttimestamp) {
			this.latesttimestamp = latesttimestamp;
		}

		public String getSplitContent() {
			return splitContent;
		}

		public void setSplitContent(String splitContent) {
			this.splitContent = splitContent;
		}

		public String getDocumentId() {
			return documentId;
		}

		public void setDocumentId(String documentId) {
			this.documentId = documentId;
		}

		public String getEventType() {
			return eventType;
		}

		public void setEventType(String eventType) {
			this.eventType = eventType;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "eventType:" + eventType + "\tisread:" + isRead
					+ "\tstartTime:" + starttimestamp + "\tlatestTime:"
					+ latesttimestamp + "\tsplitContent:" + splitContent
					+ "\tdocumentId:" + documentId;
		}

	}

	private HotWordData() {
		init();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void init() {

		try {
			setRedis_key(commonDataUpdateConfig.hotWordPattern);
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

		if (null != jedis) {
			try {
				jedis.select(Integer.valueOf(LoadConfig
						.lookUpValueByKey("commonDataDbNum")));
				List<String> redisData = jedis.lrange(this.redis_key, 0, -1);
				logger.info("init redisData, get data size:" + redisData.size());
				for (String tempElem : redisData) {
					if (null == tempElem || tempElem.isEmpty()) {
						continue;
					}
					String[] tempElemSplit = tempElem.split(LoadConfig
							.lookUpValueByKey("fieldDelimiter"));

					if (tempElemSplit[0].equals(this.redis_key)) {
						boolean isread = true;
						if (tempElemSplit[1].equals(WordState.unread.name())) {
							isread = false;
						}
						String textSplited = tempElemSplit[2];

						if (commenFuncs.computeWordsLen(textSplited) >= 5) {
							textSplited = splitWord(textSplited);

						}

						String starttime = null;
						String latesttime = null;
						String eventType = null;
						if (tempElemSplit[3].contains("#TIME#")) {
							starttime = tempElemSplit[3]
									.substring(tempElemSplit[3]
											.indexOf("#TIME#")
											+ "#TIME#".length());
							latesttime = tempElemSplit[3].substring(0,
									tempElemSplit[3].indexOf("#TIME#"));
						} else {
							latesttime = tempElemSplit[3];
						}

						if (latesttime.contains("#EVENT#")) {
							eventType = latesttime.substring(0,
									latesttime.indexOf("#EVENT#"));

							latesttime = latesttime.substring(latesttime
									.indexOf("#EVENT#") + "#EVENT#".length());
						}

						HotWordInfo hotWordInfo;
						if (tempElemSplit.length == 5) {
							hotWordInfo = new HotWordInfo(isread,
									commenFuncs.datestr2Long(starttime),
									commenFuncs.datestr2Long(latesttime),
									textSplited, tempElemSplit[4], eventType);
						} else {
							hotWordInfo = new HotWordInfo(isread,
									commenFuncs.datestr2Long(starttime),
									commenFuncs.datestr2Long(latesttime),
									textSplited, null, eventType);
						}

						hotwordMap.put(tempElemSplit[2], hotWordInfo);
					}
				}
			} catch (Exception ex) {
				logger.error("HotWordMap Init Error:" + ex.getMessage());
			}
		}
	}

	@Override
	protected void addElem2Local(String inputElemsListStr) {
		if (null == inputElemsListStr || inputElemsListStr.isEmpty()) {
			logger.info("addElems2HotWordMap:Empty Input.");
			return;
		}

		String[] inputElemsList = inputElemsListStr
				.split(commonDataUpdateConfig.recordDelimiter);

		boolean changeflag = false;

		for (String tempElem : inputElemsList) {
			if (null == tempElem || tempElem.isEmpty()) {
				continue;
			}
			String[] tempElemSplit = tempElem.split(LoadConfig
					.lookUpValueByKey("fieldDelimiter"));
			if (tempElemSplit[0].equals(this.redis_key)) {
				boolean isread = true;
				if (tempElemSplit[1].equals(WordState.unread.name())) {
					isread = false;
				}

				String textSplited = tempElemSplit[2];
				if (commenFuncs.computeWordsLen(textSplited) >= 5) {
					textSplited = splitWord(textSplited);
				}
				HotWordInfo wordInfo = null;

				String starttime = null;
				String latesttime = null;
				String eventType = null;
				if (tempElemSplit[3].contains("#TIME#")) {
					starttime = tempElemSplit[3].substring(tempElemSplit[3]
							.indexOf("#TIME#") + "#TIME#".length());
					latesttime = tempElemSplit[3].substring(0,
							tempElemSplit[3].indexOf("#TIME#"));
				} else {
					latesttime = tempElemSplit[3];
				}

				if (latesttime.contains("#EVENT#")) {
					eventType = latesttime.substring(0,
							latesttime.indexOf("#EVENT#"));

					latesttime = latesttime.substring(latesttime
							.indexOf("#EVENT#") + "#EVENT#".length());
				}

				if (!hotwordMap.containsKey(tempElemSplit[2])) {

					changeflag = true;

					if (tempElemSplit.length == 5) {
						wordInfo = new HotWordInfo(isread,
								commenFuncs.datestr2Long(starttime),
								commenFuncs.datestr2Long(latesttime),
								textSplited, tempElemSplit[4], eventType);
					} else {
						wordInfo = new HotWordInfo(isread,
								commenFuncs.datestr2Long(starttime),
								commenFuncs.datestr2Long(latesttime),
								textSplited, null, eventType);
					}
				} else if (hotwordMap.containsKey(tempElemSplit[2])) {
					wordInfo = hotwordMap.get(tempElemSplit[2]);
					if (wordInfo.isRead() == true && isread == false) {
						wordInfo.setRead(isread);
						hotWordchangeTime.set(new Date().getTime());
					}

					wordInfo.setLatesttimestamp(commenFuncs
							.datestr2Long(latesttime));
					wordInfo.setStarttimestamp(commenFuncs
							.datestr2Long(starttime));

					if (null == wordInfo.getSplitContent()
							|| wordInfo.getSplitContent().isEmpty()
							|| wordInfo.getSplitContent().length() == 0) {
						wordInfo.setSplitContent(textSplited);
					}
				}

				hotwordMap.put(tempElemSplit[2], wordInfo);
			}
		}

		if (changeflag) {
			hotWordchangeTime.set(new Date().getTime());
		}

	}

	@Override
	protected void delElemFromLocal(String del_content) {
		if (null == del_content || del_content.isEmpty()) {
			logger.info("delElemsFromHashMap:Empty Input.");
			return;
		}

		String[] inputElemsList = del_content
				.split(commonDataUpdateConfig.recordDelimiter);

		boolean changeflag = false;

		for (String tempElem : inputElemsList) {
			if (null == tempElem || tempElem.isEmpty()) {
				continue;
			}

			String[] tempElemSplit = tempElem.split(LoadConfig
					.lookUpValueByKey("fieldDelimiter"));
			if (tempElemSplit[0].equals(this.redis_key)) {
				hotwordMap.remove(tempElemSplit[2]);
				changeflag = true;
			}
		}

		if (changeflag) {
			hotWordchangeTime.set(new Date().getTime());
		}

	}

	@Override
	protected void alterElemInLocal(String alter_content) {
		if (null == alter_content || alter_content.isEmpty()) {
			logger.info("alterElems2HotWordMap:Empty Input.");
			return;
		}

		String[] inputElemsList = alter_content
				.split(commonDataUpdateConfig.recordDelimiter);

		for (String tempElem : inputElemsList) {
			String[] tempElemSplit = tempElem.split(LoadConfig
					.lookUpValueByKey("fieldDelimiter"));
			if (tempElemSplit[0].equals(this.redis_key)) {
				boolean isread = true;
				if (tempElemSplit[1].equals(WordState.unread.name())) {
					isread = false;
				}

				String textSplited = tempElemSplit[2];
				if (commenFuncs.computeWordsLen(textSplited) >= 5) {
					textSplited = splitWord(textSplited);
				}

				String starttime = null;
				String latesttime = null;
				String eventType = null;
				if (tempElemSplit[3].contains("#TIME#")) {
					starttime = tempElemSplit[3].substring(tempElemSplit[3]
							.indexOf("#TIME#") + "#TIME#".length());
					latesttime = tempElemSplit[3].substring(0,
							tempElemSplit[3].indexOf("#TIME#"));
				} else {
					latesttime = tempElemSplit[3];
				}

				if (latesttime.contains("#EVENT#")) {
					eventType = latesttime.substring(0,
							latesttime.indexOf("#EVENT#"));

					latesttime = latesttime.substring(latesttime
							.indexOf("#EVENT#") + "#EVENT#".length());
				}

				if (hotwordMap.containsKey(tempElemSplit[2])) {
					HotWordInfo hotWordInfo = hotwordMap.get(tempElemSplit[2]);
					hotWordInfo.setRead(isread);

					hotWordInfo.setLatesttimestamp(commenFuncs
							.datestr2Long(latesttime));
					hotWordInfo.setStarttimestamp(commenFuncs
							.datestr2Long(starttime));

					if (null == hotWordInfo.getSplitContent()
							|| hotWordInfo.getSplitContent().isEmpty()
							|| hotWordInfo.getSplitContent().length() == 0) {
						hotWordInfo.setSplitContent(textSplited);
					}

				} else {

					HotWordInfo hotWordInfo;
					if (tempElemSplit.length == 5) {
						hotWordInfo = new HotWordInfo(isread,
								commenFuncs.datestr2Long(starttime),
								commenFuncs.datestr2Long(latesttime),
								textSplited, tempElemSplit[4], eventType);
					} else {
						hotWordInfo = new HotWordInfo(isread,
								commenFuncs.datestr2Long(starttime),
								commenFuncs.datestr2Long(latesttime),
								textSplited, null, eventType);
					}

					// HotWordInfo hotWordInfo = new
					// HotWordInfo(isread,commenFuncs.datestr2Long(starttime),
					// commenFuncs.datestr2Long(latesttime),textSplited);
					hotwordMap.put(tempElemSplit[2], hotWordInfo);
				}
			}
		}
		hotWordchangeTime.set(new Date().getTime());
	}

	/**
	 * 
	 * @Title:splitWord
	 * @Description:对内容进行分词,如果连续三次分词失败，捕获异常，并且返回空字符串
	 * @param textSplited
	 * @return
	 * @author:wuyg1
	 * @date:2015年12月17日
	 */
	private String splitWord(String textSplited) {
		int icount = 1;
		while (icount <= 3) {
			try {
				textSplited = new String(SplitWordClient
						.split(textSplited, null).replace("(/", "_")
						.replace(") ", " "));
				break;
			} catch (Exception e) {
				if (icount == 3) {
					LOG.error(textSplited + " segment is error!");
					textSplited = "";
					break;
				}
				icount++;
			}
		}
		return textSplited;
	}

	/**
	 * 
	 * @Title:getHotWordchangeTime
	 * @Description:该变量表示热点事件的最新改动时间
	 * @return
	 * @author:wuyg1
	 * @date:2016年6月6日
	 */
	public AtomicLong getHotWordchangeTime() {
		return hotWordchangeTime;
	}

	public void setHotWordchangeTime(AtomicLong hotWordchangeTime) {
		this.hotWordchangeTime = hotWordchangeTime;
	}

	public static HotWordData getInstance() {
		if (null == HotWordData.INSTANCE) {
			INSTANCE = new HotWordData();
		}
		return HotWordData.INSTANCE;
	}

	/**
	 * 
	 * @Title:searchHotWord
	 * @Description:查询热词
	 * @param query
	 * @return
	 * @author:wuyg1
	 * @date:2016年8月3日
	 */
	public HotWordInfo searchHotWord(String query) {
		if (null == query) {
			return null;
		}
		query = query.trim();
		if (query.isEmpty()) {
			return null;
		}

		query = query.toLowerCase();

		return this.getHotwordMap().get(query);
	}

	/**
	 * 
	 * @Title:getHotwordMap
	 * @Description: 返回热点事件集合，如果某个热点事件分词失败的话，splitContent字段则为空字符串（""）
	 * @return
	 * @author:wuyg1
	 * @date:2016年6月6日
	 */
	public ConcurrentHashMap<String, HotWordInfo> getHotwordMap() {
		return hotwordMap;
	}

	public void setHotwordMap(ConcurrentHashMap<String, HotWordInfo> hotwordMap) {
		this.hotwordMap = hotwordMap;
	}

}
