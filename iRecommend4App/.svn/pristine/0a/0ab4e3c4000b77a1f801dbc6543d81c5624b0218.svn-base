package com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ifeng.iRecommend.dingjw.dataCollection.newItemToHbase;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;

import redis.clients.jedis.Jedis;


/**
 * <PRE>
 * 作用 : 预加载层用于记录历史优质数据列表并按天存储
 *   
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
 *          1.0          2016年8月30日             kl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class RecordNewsPreload implements Runnable{
	private static Log LOG = LogFactory.getLog("RecordNewsPreload");

	public RecordNewsPreload() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		preload();
	}

	private void preload() {
		LOG.info("RecordNewsPreload Start ~");
		//获取新的待记录数据
		List<FrontNewsItem> idNew = loadingDataFromRedis();
		//将获取记录转换数据结构
		Map<String, Set<FrontNewsItem>> idMap=processData(idNew);
		//将获取记录融合已记录数据按当天日期排序并重新按天存储
		sortAndRecord(idMap);
		LOG.info("RecordNewsPreload Finashed ~");
	}

	/**
	 * 
	 * 遍历map并对每个key进行redis查询，有则混合数据，并最终按当天日期排序，按天记录到redis中
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	private void sortAndRecord(Map<String, Set<FrontNewsItem>> idMap) {
		LOG.info("sortAndRecord Start ~");
		Gson gson=new Gson();
		if(idMap==null){
			return ;
		}
		Iterator<Entry<String, Set<FrontNewsItem>>> iter=idMap.entrySet().iterator();
		String temp;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String timeString=(String) entry.getKey();
			Set<FrontNewsItem> str= (Set<FrontNewsItem>) entry.getValue();
			//先从redis获取
			String dataString=get(timeString);
			if(dataString!=null){
				Set<FrontNewsItem> redisRecord = gson.fromJson(dataString, new TypeToken<Set<FrontNewsItem>>() {}.getType());
				str.addAll(redisRecord);
			}
			LOG.info("get timeKey from redis finashed ~");
			//按当天时间排序
			List<FrontNewsItem> sortList=new ArrayList<FrontNewsItem>();
			Set<String> idsSet=new HashSet<String>();
			for(FrontNewsItem item:str){
				if(!idsSet.contains(item.getDocId())){
					sortList.add(item);
					idsSet.add(item.getDocId());
				}
			}
			Collections.sort(sortList, new  Comparator<FrontNewsItem>() {
				@Override
				public int compare(FrontNewsItem o1, FrontNewsItem o2) {
					 return o1.getDate().compareTo(o2.getDate());  
				}  
			});
			LOG.info("sort by time finashed ~");
			temp=gson.toJson(sortList);
			//将组合后的数据重新存入redis
			disToRedis(timeString, 50*24*60*60, temp);
			LOG.info("record to redis And sortAndRecord finashed ~");	
		}
	}

	/**
	 * 
	 * 将list数据按天归类并存入map中，key为当天的日期 yyyy-MM-dd
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */

	private Map<String, Set<FrontNewsItem>> processData( List<FrontNewsItem> itemsList) {
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
		if(itemsList==null){
			return null;
		}
		Map<String, Set<FrontNewsItem>> idMap=new HashMap<String, Set<FrontNewsItem>>();
		for(FrontNewsItem item:itemsList){
			try {
				Date date = dateFormate.parse(item.getDate());
				String lifeTime=dateFormate.format(date);
				if(!idMap.containsKey(lifeTime)){
					Set<FrontNewsItem> setFrontNews=new HashSet<FrontNewsItem>();
					setFrontNews.add(item);
					idMap.put(lifeTime, setFrontNews);
				}else {
					idMap.get(lifeTime).add(item);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error("processData failed ", e);
			}
		}
		return idMap;
	}

	/**
	 * 
	 * 去17的redis中获取按天对应的数据（db10）
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	
	private String get(String key) {
		String content = null;
        if(key==null||key.isEmpty()){
        	return content;
        }
		try{
			Jedis jedis = new Jedis("10.90.4.17", 6379, 2000);
			jedis.select(10);
			content = jedis.get(key);
			LOG.info("get"+key+" key from redis success");
		} catch (Exception e){
			LOG.error(" ", e);
		}
		return content;
	}
	
	/**
	 * 
	 * 取出当天的凤凰热闻榜数据
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	private List<FrontNewsItem> loadingDataFromRedis(){
		String content = null;
		Gson gson=new Gson();
		List<FrontNewsItem> itemsList=new ArrayList<FrontNewsItem>();
		try{
			Jedis jedis = new Jedis("10.90.4.17", 6379, 2000);
			jedis.select(2);
			content = jedis.get("凤凰热闻榜");
			if(content!=null){
				itemsList=gson.fromJson(content, new TypeToken<List<FrontNewsItem>>() {}.getType());
				LOG.info("get 凤凰热闻榜  success");
			}else {
				LOG.info("get 凤凰热闻榜  is null");
			}
		} catch (Exception e){
			LOG.error(" ", e);
		}
		return itemsList;
	}
	
	/**
	 * 
	 * 记录按天归类的数据，key为日期样式： yyyy-MM-dd
	 * 
	 * 注意：
	 * @param 
	 * 
	 * @return 
	 * 
	 */
	private void disToRedis(String tableName,int validTime,String disStr){
		if(disStr == null){
			return ;
		}
		//写60redis 集群Master节点，后续只写这一个节点
		try{

			Jedis jedis = new Jedis("10.90.7.60", 6379, 10000);
			jedis.select(10);
			String status = jedis.set(tableName, disStr);
			jedis.expire(tableName, validTime);
			if(!status.equals("OK")){
				LOG.error("set status code:"+status);
			}else{
				LOG.info("Dis "+tableName+" to redis");
			}
		}catch(Exception e){
			LOG.error("ERROR"+e);
		}
	}
	

	public static void main(String[] args) {
		RecordNewsPreload testNewsPreload=new RecordNewsPreload();
		testNewsPreload.preload();
	}
}
