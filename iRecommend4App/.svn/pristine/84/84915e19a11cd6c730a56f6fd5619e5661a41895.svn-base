/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.newHotPredict;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.gson.Gson;
import com.ifeng.iRecommend.featureEngineering.itemf;
import com.ifeng.iRecommend.zhanzh.SolrUtil.ItemSorlServerClient;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;

/**
 * <PRE>
 * 作用 : 
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
 *          1.0          2015年7月21日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class WakeUpListPredict {
	private static Log LOG = LogFactory.getLog("WakeUpListPredict");

	public static List<HotRankItem> getWakeUpList() {
		HotItemLoadingUtil util = HotItemLoadingUtil.getInstance();
		util.updateIfengPCnews();
		List<HotRankItem> pcnewsList = util
				.getHotRankList(HotItemLoadingUtil.IfengPCNewsList);
		if (pcnewsList == null) {
			return null;
		}
		HashSet<String> repetitedId = new HashSet<String>();
		ArrayList<HotRankItem> wakeUpList = new ArrayList<HotRankItem>();
		// 初始化solr查询
		ItemSorlServerClient client = ItemSorlServerClient.getInstance();
		HttpSolrServer server = client.getServer();

		for (HotRankItem hot : pcnewsList) {
			String docId = hot.getDocId();
			if (docId == null||repetitedId.contains(docId)) {
				continue;
			}
			String qureStr = "itemid:" + docId;
			SolrQuery query = new SolrQuery();
			query.setQuery(qureStr);
			try {
				QueryResponse qr = server.query(query);
				SolrDocumentList docList = qr.getResults();
				Iterator<SolrDocument> it = docList.iterator();
				while (it.hasNext()) {
					SolrDocument doc = it.next();
					boolean isAvalable = (Boolean) doc.get("available");
					if(!isAvalable){
						wakeUpList.add(hot);
					}
				}
				repetitedId.add(docId);
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				LOG.error("Search item available error  ", e);
				continue;
			}catch (Exception e1) {
				// TODO: handle exception
				LOG.error("Search item available error  ", e1);
				continue;
			}


		}
		return wakeUpList;
	}
	
	public static HashMap<String, Integer> predictLifeTime(){
		HashMap<String, ArrayList<Integer>> lifeTimeMap = new HashMap<String, ArrayList<Integer>>();
		HeatPredictUtils utils = HeatPredictUtils.getInstance();
		utils.updateHotList();
		List<HotRankItem> pvlist = utils.getHotRankItemList();
		List<HotRankItem> wakeuplist = getWakeUpList();
		pvlist.addAll(wakeuplist);
		for(HotRankItem hot : pvlist){
			itemf item = hot.getItem();
			if(item == null){
				continue;
			}
			HashMap<String, String> featureMap = parserItemfFeature(item);
			String[] channelList = {"c","sc","cn"};
			for(String channel : channelList){
				String value = featureMap.get(channel);
				if(value != null){
					String[] list = value.split("\\|");
					for(String temp : list){
						Long time = hot.getLifeTime()/(60*60*1000);
						int lifetime = time.intValue();

						String key = channel+"="+temp;
						ArrayList<Integer> templist = lifeTimeMap.get(key);
						if(templist == null){
							templist = new ArrayList<Integer>();
							templist.add(lifetime);
							lifeTimeMap.put(key, templist);
						}else{
							templist.add(lifetime);
						}
					}
				}
			}
		}
		HashMap<String, Integer> resultmap = new HashMap<String, Integer>();
		Set<Entry<String, ArrayList<Integer>>> entrySet = lifeTimeMap.entrySet();
		for(Entry<String, ArrayList<Integer>> entry : entrySet){
			int sum = 0;
			for(Integer i : entry.getValue()){
				sum += i;
			}
			sum = sum/entry.getValue().size();
			resultmap.put(entry.getKey(), sum);
		}
		return resultmap;
	}
	
	/**
	 * 解析itemF的feature字段，并返回itemF的特征map
	 * 
	 * @param itemF 
	 * 
	 * @return HashMap<String,String> key=c\sc\cn\et\...  value=自媒体|网球|...
	 * 
	 */
	private static HashMap<String, String> parserItemfFeature(itemf item){
		HashMap<String, String> FeatureMap = new HashMap<String, String>();
		if(item == null){
			return FeatureMap;
		}
		ArrayList<String> featurelist = item.getFeatures();
		if(featurelist == null||featurelist.isEmpty()){
			return FeatureMap;
		}
		for(int i=1;i<=featurelist.size()-2;i+=3){
			String temp = featurelist.get(i);
			String feature = FeatureMap.get(temp);
			if(feature != null){
				feature = feature+"|"+featurelist.get(i-1);
				FeatureMap.put(temp, feature);
			}else{
				FeatureMap.put(temp, featurelist.get(i-1));
			}
			
		}
		return FeatureMap;
	}
	
	public static void main(String[] args){
//		HashMap<String, Integer> map = predictLifeTime();
//		Gson gson = new Gson();
//		String result = gson.toJson(map);
//		System.out.println(result);
		
		
		
		List<HotRankItem> hotlist = getWakeUpList();
		List<FrontNewsItem> debuglist = new ArrayList<FrontNewsItem>();
		for(HotRankItem hot : hotlist){
			FrontNewsItem temp = new FrontNewsItem(hot);
			String feature = temp.getReadableFeatures();
			if(feature!=null){
//				if(feature.indexOf("cn")>=0||feature.indexOf("s1")>=0){
//					temp.setWhy("s1 or cn");
//					debuglist.add(temp);
//					continue;
//				}
				//过滤掉3天以前的
				if (hot.getLifeTime() > 3 * 24 * 60 * 60 * 1000) {
					LOG.info("out of time " + hot.getTitle() + " "+ hot.getDocId());
					continue;
				}
				debuglist.add(temp);
			}
		}
		Gson gson = new Gson();
		String result = gson.toJson(debuglist);
		System.out.println(result);
	}
}
