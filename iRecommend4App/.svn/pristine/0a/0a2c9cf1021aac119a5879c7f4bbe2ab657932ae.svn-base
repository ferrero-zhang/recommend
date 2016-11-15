package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.CommonSubDataWord.WordState;
/**
 * 
 * <PRE>
 * 作用 : 
 *   信息全表达的订阅
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
 *          1.0          2016年8月25日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class DataExpLib extends LocalDataUpdate {
	public static ConcurrentHashMap<String, ArrayList<String>> DataExpMaps = new ConcurrentHashMap<String, ArrayList<String>>();
	private static DataExpLib INSTANCE = null;

	public static ConcurrentHashMap<String, ArrayList<String>> getDataExpMaps() {
		return DataExpMaps;
	}

	public static void setDataExpMaps(
			ConcurrentHashMap<String, ArrayList<String>> dataExpMaps) {
		DataExpMaps = dataExpMaps;
	}

	public DataExpLib() {
		// TODO Auto-generated constructor stub
		init();
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		try {
			setRedis_key(LoadConfig.lookUpValueByKey("dataExpLibPattern"));
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

					String[] values = tempElem.split(LoadConfig
							.lookUpValueByKey("keySegment"));

					if (DataExpMaps.containsKey(values[0])) {
						DataExpMaps.get(values[0]).addAll(
								Arrays.asList(values[1].split(LoadConfig
										.lookUpValueByKey("ExpSegment"))));
					} else {
						ArrayList<String> ExpLibList = new ArrayList<String>();

						ExpLibList.addAll(Arrays.asList(values[1]
								.split(LoadConfig
										.lookUpValueByKey("ExpSegment"))));

						DataExpMaps.put(values[0], ExpLibList);
					}

				}
			} catch (Exception ex) {
				logger.error("HotWordMap Init Error:" + ex.getMessage());
			}
		}
	}

	/**
	 * dataExpLib_互联网金融#KEY#金融#EXP#_2016-02-23 09:46:12
	 */
	@Override
	protected void addElem2Local(String add_content) {
		// TODO Auto-generated method stub
		if (null == add_content || add_content.isEmpty()) {
			logger.info("addElems2DataExpLib:Empty Input.");
			return;
		}

		String[] inputElemsList = add_content
				.split(commonDataUpdateConfig.recordDelimiter);

		for (String tempElem : inputElemsList) {
			if (null == tempElem || tempElem.isEmpty()) {
				continue;
			}
			System.err.println(tempElem);
			String[] tempElemSplit = tempElem.split(LoadConfig
					.lookUpValueByKey("keySegment"));

			ArrayList<String> add_ExpLibList = recordParser(tempElem);

			if (DataExpMaps.containsKey(tempElemSplit[0])) {
				ArrayList<String> ExpLibList = DataExpMaps
						.get(tempElemSplit[0]);

				for (String ExpWord : add_ExpLibList) {
					if (!DataExpMaps.get(tempElemSplit[0]).contains(ExpWord)) {
						DataExpMaps.get(tempElemSplit[0]).add(ExpWord);
					}
				}

			} else {
				DataExpMaps.put(tempElemSplit[0], add_ExpLibList);
			}

		}

	}

	@Override
	protected void delElemFromLocal(String del_content) {
		// TODO Auto-generated method stub
		if (null == del_content || del_content.isEmpty()) {
			logger.info("delElemsFromDataExpLib:Empty Input.");
			return;
		}

		String[] inputElemsList = del_content
				.split(commonDataUpdateConfig.recordDelimiter);
		for (String tempElem : inputElemsList) {
			if (null == tempElem || tempElem.isEmpty()) {
				continue;
			}
			String keySegment = LoadConfig.lookUpValueByKey("keySegment");
			String keyWord = tempElem.split(keySegment)[0];
			
			DataExpMaps.remove(keyWord);
		}
	}

	@Override
	protected void alterElemInLocal(String alter_content) {
		
		if (null == alter_content || alter_content.isEmpty()) {
			logger.info("alterElems2DataExpLib:Empty Input.");
			return;
		}

		String[] inputElemsList = alter_content
				.split(commonDataUpdateConfig.recordDelimiter);

		for (String tempElem : inputElemsList) {
			if (null == tempElem || tempElem.isEmpty()) {
				continue;
			}
			String[] tempElemSplit = tempElem.split(LoadConfig
					.lookUpValueByKey("keySegment"));

			ArrayList<String> newExpLibList = recordParser(tempElem);

			DataExpMaps.put(tempElemSplit[0], newExpLibList);
		}
	}

	/**
	 * 
	 * @Title:recordParser
	 * @Description:
	 * @param elem
	 * @return
	 * @author:wuyg1
	 * @date:2016年2月23日
	 */
	protected ArrayList<String> recordParser(String elem) {
		String keySegment = LoadConfig.lookUpValueByKey("keySegment");

		String[] elems = elem.split(keySegment);

		ArrayList<String> ExpLibList = new ArrayList<String>();
		ExpLibList.addAll(Arrays.asList(elems[1].split(LoadConfig
				.lookUpValueByKey("ExpSegment"))));

		return ExpLibList;
	}

	public static DataExpLib getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new DataExpLib();
		}
		return INSTANCE;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataExpLib dataExpLib = DataExpLib.getInstance();

		CommonDataSub cds = new CommonDataSub();
		Thread t = new Thread(cds);
		t.start();
        while (true) {
        	System.err.println("国家公务员考试:"
    				+ dataExpLib.getDataExpMaps().get("国家公务员考试"));
    		System.err.println("互联网金融:" + dataExpLib.getDataExpMaps().get("互联网金融"));
    		System.err.println("湖南卫视:" + dataExpLib.getDataExpMaps().get("湖南卫视"));
    		
    		Iterator<String> iterator = dataExpLib.getDataExpMaps().keySet().iterator();
    		while(iterator.hasNext()){
    			String key = iterator.next();
    			
    			if("互联网金融".equals(key)){
    				System.err.println();
    			}
    			
    			System.err.println(key+"\t"+dataExpLib.getDataExpMaps().get(key));
    			
    			if(key.equals("国家公务员考试") || key.equals("互联网金融") || key.equals("湖南卫视")){
    				System.out.println(key+"\t"+dataExpLib.getDataExpMaps().get(key));
    			}
    		}
			
		}
	
	}

}
