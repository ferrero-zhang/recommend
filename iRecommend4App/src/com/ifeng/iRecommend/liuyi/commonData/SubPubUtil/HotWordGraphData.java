package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.featureEngineering.OrientDB.KnowledgeGraph;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.CommonSubDataWord.WordState;

import redis.clients.jedis.Jedis;

public class HotWordGraphData extends LocalDataUpdate {

	static HotWordGraphData hotWordGraphData = null;
	private static KnowledgeGraph kGraph = new KnowledgeGraph();

	public class HotWordInfo {
		boolean isRead;// 热词是否可读
		long starttimestamp;
		long latesttimestamp;
		String splitContent;// 分词后的结果
		String documentId; // 热点事件对应的ID，这个主要是用于张阳从客户端抓取的专题事件标注用的

		public HotWordInfo(boolean isRead, long starttimestamp,
				long latesttimestamp, String splitContent, String documentId) {
			this(isRead, starttimestamp, latesttimestamp, splitContent);
			setDocumentId(documentId);
		}

		public HotWordInfo(boolean isRead, long starttimestamp,
				long latesttimestamp, String splitContent) {
			setRead(isRead);
			setStarttimestamp(starttimestamp);
			setLatesttimestamp(latesttimestamp);
			setSplitContent(splitContent);
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

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "isread:" + isRead + "\tstartTime:" + starttimestamp
					+ "\tlatestTime:" + latesttimestamp + "\tsplitContent:"
					+ splitContent + "\tdocumentId:" + documentId;
		}

	}

	private HotWordGraphData() {
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

						textSplited = splitWord(textSplited);

						int length = commenFuncs.computeWordsLen(textSplited);
						

						String [] ws = textSplited.split(" ");
						
						if(((ws.length >=3 || length >3) && length<5) ||
								(ws.length < 3 && length <=3))
						{
							continue;
						}


						String starttime = null;
						String latesttime = null;
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
		                
		                    latesttime = latesttime.substring(latesttime.indexOf("#EVENT#")+"#EVENT#".length());
						}
						
						HotWordInfo hotWordInfo;
						if (tempElemSplit.length == 5) {
							hotWordInfo = new HotWordInfo(isread,
									commenFuncs.datestr2Long(starttime),
									commenFuncs.datestr2Long(latesttime),
									textSplited, tempElemSplit[4]);
						} else {
							hotWordInfo = new HotWordInfo(isread,
									commenFuncs.datestr2Long(starttime),
									commenFuncs.datestr2Long(latesttime),
									textSplited);
						}
						if (hotWordInfo.isRead()) {
							// 只有当该热点事件是可读的时候才可以加入图谱 热点词是tempElemSplit[2]
							if (kGraph.queryWord(tempElemSplit[2]) == null
									|| kGraph.queryWord(tempElemSplit[2])
											.isEmpty()) {
								kGraph.addVertex(tempElemSplit[2], "e");
							}
						}

					}
				}
			} catch (Exception ex) {
				logger.error("HotWordGraph Init Error:" + ex.getMessage());
			}
		}
		kGraph.shutdown();
	}

	@Override
	protected void addElem2Local(String inputElemsListStr) {
		if (null == inputElemsListStr || inputElemsListStr.isEmpty()) {
			logger.info("addElems2HotWordMap:Empty Input.");
			return;
		}

		String[] inputElemsList = inputElemsListStr
				.split(commonDataUpdateConfig.recordDelimiter);

		for (String tempElem : inputElemsList) {
			if (null == tempElem || tempElem.isEmpty()) {
				continue;
			}
			String[] tempElemSplit = tempElem.split(LoadConfig
					.lookUpValueByKey("fieldDelimiter"));
			boolean isread = true;
			if (tempElemSplit[1].equals(WordState.unread.name())) {
				isread = false;
			}

			if (!isread) {
				// 如果该热点事件不可读，则不向图数据库中添加节点
				continue;
			}

			String textSplited = tempElemSplit[2];
			textSplited = splitWord(textSplited);
			
			int length = commenFuncs.computeWordsLen(textSplited);

			String [] ws = textSplited.split(" ");
			
			if(((ws.length >=3 || length >3) && length<5) ||
					(ws.length < 3 && length <=3))
			{
				continue;
			}

			// HotWordInfo wordInfo = null;
			// 将tempElemSplit[2] 加入图数据库的节点中
			if (kGraph.queryWord(tempElemSplit[2]) == null
					|| kGraph.queryWord(tempElemSplit[2]).isEmpty()) {
				kGraph.addVertex(tempElemSplit[2], "e");
			}
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
		for (String tempElem : inputElemsList) {
			if (null == tempElem || tempElem.isEmpty()) {
				continue;
			}
			String[] tempElemSplit = tempElem.split(LoadConfig
					.lookUpValueByKey("fieldDelimiter"));

			// hotwordMap.remove(tempElemSplit[2]);
			// 从图谱中删除该热点事件 tempElemSplit[2]
			kGraph.deleteVertex(tempElemSplit[2], "e");
		}

	}

	// 暂时不涉及热点事件图谱的修改操作
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

				if (!isread) {
					// 如果热点事件是不可读的，则从图谱中删除该节点
				}

			}
		}

	}

	/**
	 * 
	 * @Title:splitWord
	 * @Description:对内容进行分词
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
					break;
				}
				icount++;
			}
		}
		return textSplited;
	}

	public static HotWordGraphData getInstance() {
		if (null == hotWordGraphData) {
			hotWordGraphData = new HotWordGraphData();
		}
		return hotWordGraphData;
	}

	public static void main(String[] args) {
		HotWordGraphData hotWordGraphData = HotWordGraphData.getInstance();

	}

}
