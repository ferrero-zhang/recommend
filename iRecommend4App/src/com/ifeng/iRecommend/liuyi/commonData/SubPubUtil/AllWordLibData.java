package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
/**
 * 
 * <PRE>
 * 作用 : 
 *    全量词的订阅  
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
 *          1.0          2016年3月2日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class AllWordLibData extends LocalDataUpdate {
	
	private static DataCache allWordCache;
    private static AllWordLibData INSTANCE = null;
    
    public AllWordLibData() {
       init();
    }
    
	public static DataCache getAllWordCache() {
		return allWordCache;
	}

	public static void setAllWordCache(DataCache allWordCache) {
		AllWordLibData.allWordCache = allWordCache;
	}

	public boolean containsKey(String key){
		return getAllWordCache().getCache().asMap().containsKey(key);
	}


	@Override
	protected void init() {
		// TODO Auto-generated method stub
		try {
			setRedis_key(null);
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
				jedis.select(Integer.valueOf(LoadConfig.lookUpValueByKey("allWordDataDbNum")));
				List<String> redisData = new ArrayList<String>();
				redisData.addAll(jedis.keys("*"));
				logger.info("init redisData, get data size:" + redisData.size());
				for (String tempElem : redisData) {
					if(null == tempElem || tempElem.isEmpty()){
						continue;
					}
			        
					allWordCache.put(tempElem, tempElem);
				}
			} catch (Exception ex) {
				logger.error("AllWordCache Init Error:" + ex.getMessage());
			}
		}
	}

	@Override
	protected void addElem2Local(String add_content) {
		// TODO Auto-generated method stub
		if (null == add_content || add_content.isEmpty()) {
			logger.info("addElems2AllWordCache:Empty Input.");
			return;
		}

		String[] inputElemsList = add_content
				.split(commonDataUpdateConfig.recordDelimiter);

		for (String tempElem : inputElemsList) {
			if(null == tempElem || tempElem.isEmpty()){
				continue;
			}
			allWordCache.put(tempElem, tempElem);
		}
	}

	@Override
	protected void delElemFromLocal(String del_content) {
		// TODO Auto-generated method stub
		if (null == del_content || del_content.isEmpty()) {
			logger.info("delElemsFromAllWordCache:Empty Input.");
			return;
		}

		String[] inputElemsList = del_content.split(commonDataUpdateConfig.recordDelimiter);
		for (String tempElem : inputElemsList) {
			if(null == tempElem || tempElem.isEmpty()){
				continue;
			}
		   
		    allWordCache.getCache().asMap().remove(tempElem);

		   
		}
	}

	@Override
	protected void alterElemInLocal(String alter_content) {
		// TODO Auto-generated method stub

	}
	
	public static AllWordLibData getInstance(){
		if(null == INSTANCE){
			INSTANCE = new AllWordLibData();
		}
		return INSTANCE;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
         
        AllWordLibData allWordLibData =  AllWordLibData.getInstance();
        
        CommonDataSub cds = new CommonDataSub();
 		Thread t = new Thread(cds); 
 		t.start();
        while(true){
        	System.err.println("政协委员:"+allWordLibData.containsKey("政协委员"));
        	System.err.println("暴力抗法:"+allWordLibData.containsKey("暴力抗法"));
        	System.err.println("准妈妈:"+allWordLibData.containsKey("准妈妈"));
        	System.err.println(allWordLibData.getAllWordCache().getCache().asMap().size());
        }
        
//    	while(true){
//			System.err.println("请输入:输入-1即可退出");
//			Scanner input = new Scanner(System.in);
//			String query = input.nextLine();
//			if("-1".equals(query)){
//				break;
//			}
//			  System.err.println(allWordLibData.containsKey(query));
//			  System.err.println(allWordLibData.getAllWordCache().getCache().asMap().size());
//		}
      
	}

}
