/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.newHotPredict;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.iRecommend.featureEngineering.dataStructure.*;
import com.ifeng.iRecommend.zhanzh.SolrUtil.SearchItemsFromSolr;


/**
 * <PRE>
 * 作用 : 热点新闻预测程序
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
 *          1.0          2015年7月14日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class HotNewsPredict {
	private static Log LOG = LogFactory.getLog("HotNewsPredict");
	/**
	 * 头条热点新闻排序函数
	 * 
	 * 
	 * 在使用该函数前请主动更新HotItemLoadingUtil里的基础热点数据
	 * 
	 * 
	 * @param 
	 * 
	 * @return List<HotRankItem>
	 */
	public static List<HotRankItem> toutiaoHotNewsPredict(){
		//热点数据加载模块，加载所需的热度数据信息
		HotItemLoadingUtil util = HotItemLoadingUtil.getInstance();
		
		//取出首页要闻区新闻列表，并过滤掉未在内容池命中的Item
		List<HotRankItem> LoadIfengYaowenList = util.getHotRankList(HotItemLoadingUtil.IfengMainpageYaowen);
		List<HotRankItem> ifengMainpageYaowenList = new ArrayList<HotRankItem>();
		if(LoadIfengYaowenList != null){
			for(HotRankItem hot : LoadIfengYaowenList){
				itemf item = hot.getItem();
				if(item != null){
					hot.setWhy("MainpageYaowen");
					ifengMainpageYaowenList.add(hot);
				}
			}
		}else{
			LOG.error("Loading MainpageYaowenArea error ~");
		}

		//取出hackernews热点新闻
		List<HotRankItem> LoadingHackerNewsHotlist = util.getHotRankList(HotItemLoadingUtil.IfengHackerHotList);
		List<HotRankItem> hackernewsHotList = new ArrayList<HotRankItem>();
		if(LoadingHackerNewsHotlist != null){
			for(HotRankItem hot : LoadingHackerNewsHotlist){
				hot.setWhy("hackerHot");
				hackernewsHotList.add(hot);
			}
		}
		
		List<HotRankItem> rankList = new ArrayList<HotRankItem>();
		
		HashMap<String, HotRankItem> IdItemMap = new HashMap<String, HotRankItem>();
		
		//用于计数统计Map
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		
		//统计一条新闻在2个list中命中次数
		for(HotRankItem hot : ifengMainpageYaowenList){
			Integer count = countMap.get(hot.getDocId());
			if(count == null){
				count = new Integer(1);
				countMap.put(hot.getDocId(), count);
			}else{
				count++;
				countMap.put(hot.getDocId(), count);
			}
			IdItemMap.put(hot.getDocId(), hot);
		}
		
		for(HotRankItem hot : hackernewsHotList){
			Integer count = countMap.get(hot.getDocId());
			if(count == null){
				count = new Integer(1);
				countMap.put(hot.getDocId(), count);
			}else{
				count++;
				countMap.put(hot.getDocId(), count);
			}
			IdItemMap.put(hot.getDocId(), hot);
		}
		
		HashMap<Integer, List<HotRankItem>> timesMap = new HashMap<Integer, List<HotRankItem>>();
		//分别将hotRankItem按照命中次数存入timesMap中
		Set<String> keySet = countMap.keySet();
		int i = 2;
		while(i >=1){
			for(String key : keySet){
				int c = countMap.get(key);
				if(c == i ){
					HotRankItem hot = IdItemMap.get(key);
					
					//查询solr将不在生命周期内的内容过滤掉
					if(hot.getDocId() == null||!SearchItemsFromSolr.searchItemIsAvailable(hot.getDocId())){
						LOG.info("out of time "+hot.getTitle()+" "+hot.getDocId());
						continue;
					}
					
					//test 若Hacker得分大于阈值100 则向上提一层，若hacker阈值小于30则向下降一层
					if(c<2 && hot.getHotScore()>100){
						c++;
					}
					if(c>1 && hot.getHotScore()<30 ){
						c--;
					}
					
					List<HotRankItem> hotlist = timesMap.get(c);
					if(hotlist == null){
						hotlist = new ArrayList<HotRankItem>();
						hotlist.add(hot);
						timesMap.put(c, hotlist);
					}else{
						hotlist.add(hot);
					}
				}
			}
			i--;
		}
		//将timesmap数据插入最终排序列表
		i=2;
		while(i >= 1){
			List<HotRankItem> hotlist = timesMap.get(i);
			if(hotlist == null || hotlist.isEmpty()){
				i--;
				continue;
			}
			try{
				Collections.sort(hotlist, new Comparator<HotRankItem>() {

					@Override
					public int compare(HotRankItem o1, HotRankItem o2) {
						// TODO Auto-generated method stub
						if(o1.getHotScore() == o2.getHotScore()){
							return 0;
							
						}else if(o1.getHotScore()<o2.getHotScore()){
							return 1;
						}else{
							return -1;
						}
					}
				});
			} catch (Exception e){
				LOG.warn(" ",e);
				System.out.println(e);
			}
			
			for(HotRankItem hot : hotlist){
				hot.setHotLevel(String.valueOf(i));
				rankList.add(hot);
			}
			i--;
		}
		return rankList;
	}
	
	/**
	 * 体育热点新闻排序函数
	 * 
	 * 
	 * 在使用该函数前请主动更新HotItemLoadingUtil里的基础热点数据
	 * 
	 * 
	 * @param 
	 * 
	 * @return List<HotRankItem>
	 */
	public static List<HotRankItem> SportsHotNewsPredict(){
		//热点数据加载模块，加载所需的热度数据信息
		HotItemLoadingUtil util = HotItemLoadingUtil.getInstance();

		//取出体育主页热度列表，并过滤掉未在内容池命中的Item
		List<HotRankItem> LoadIfengSportpageList= util.getHotRankList(HotItemLoadingUtil.IfengSportsPageHotList);
		List<HotRankItem> ifengSportPageList = new ArrayList<HotRankItem>();
		if(LoadIfengSportpageList != null){
			
			for(HotRankItem hot : LoadIfengSportpageList){
				itemf item = hot.getItem();
				if(item != null){
					ifengSportPageList.add(hot);
					hot.setWhy("IfengSportsPagehot");
				}
			}
		}else{
			LOG.warn("Load IfengSportpageList error , is null");
		}

		//取出凤凰首页中体育区块新闻
		List<HotRankItem> LoadIfengMainpageSports = util.getHotRankList(HotItemLoadingUtil.IfengMainpageSports);
		List<HotRankItem> ifengMainpageSportsList = new ArrayList<HotRankItem>();
		if(LoadIfengMainpageSports!=null){
		
			for(HotRankItem hot : LoadIfengMainpageSports){
				itemf item = hot.getItem();
				if(item != null){
					ifengMainpageSportsList.add(hot);
					hot.setWhy("IfengMainpageSportsHot");
				}
			}
		}else{
			LOG.warn("Load IfemgMainpageSportsList error , is null");
		}
		
		//取出评论pv热点list数据，并筛选体育类别数据
		List<HotRankItem> LoadHackerHotList = util.getHotRankList(HotItemLoadingUtil.IfengHackerHotList);
		List<HotRankItem> SportsHackerHotList = new ArrayList<HotRankItem>();
		if(LoadHackerHotList != null){
			for(HotRankItem hot : LoadHackerHotList){
				if(hot.getDocChannel().indexOf("体育")>=0
						||hot.getDocChannel().indexOf("足球")>=0
						||hot.getDocChannel().indexOf("篮球")>=0
						||hot.getDocChannel().indexOf("亚运")>=0
						||hot.getDocChannel().indexOf("冰雪")>=0
						||hot.getDocChannel().indexOf("游泳")>=0
						||hot.getDocChannel().indexOf("棒球")>=0
						||hot.getDocChannel().indexOf("奥运")>=0
						||hot.getDocChannel().indexOf("体操")>=0
						||hot.getDocChannel().indexOf("排球")>=0
						||hot.getDocChannel().indexOf("台球")>=0
						||hot.getDocChannel().indexOf("赛车")>=0
						||hot.getDocChannel().indexOf("网球")>=0
						||hot.getDocChannel().indexOf("乒乓球")>=0){
					
					hot.setWhy("HackerSports hot");
					SportsHackerHotList.add(hot);
				}
			}
		}
		
		//以下部分对体育新闻做热点预测排序 
		List<HotRankItem> rankList = new ArrayList<HotRankItem>();
		
//		HashMap<String, HotRankItem> ifengSportPageMap = new HashMap<String, HotRankItem>();
//		HashMap<String, HotRankItem> ifengMainpageSportsMap = new HashMap<String, HotRankItem>();
//		HashMap<String, HotRankItem> SportsHackerHotMap = new HashMap<String, HotRankItem>();
		
		HashMap<String, HotRankItem> IdItemMap = new HashMap<String, HotRankItem>();
		
		//用于计数统计Map
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		//统计一条新闻在3个list中命中次数
		for(HotRankItem hot : ifengSportPageList){
			Integer count = countMap.get(hot.getDocId());
			if(count == null){
				count = new Integer(1);
				countMap.put(hot.getDocId(), count);
			}else{
				count++;
				countMap.put(hot.getDocId(), count);
			}
			IdItemMap.put(hot.getDocId(), hot);
		}
		
		for(HotRankItem hot : ifengMainpageSportsList){
			Integer count = countMap.get(hot.getDocId());
			if(count == null){
				count = new Integer(1);
				countMap.put(hot.getDocId(), count);
			}else{
				count++;
				countMap.put(hot.getDocId(), count);
			}
			IdItemMap.put(hot.getDocId(), hot);
		}
		
		for(HotRankItem hot : SportsHackerHotList){
			Integer count = countMap.get(hot.getDocId());
			if(count == null){
				count = new Integer(1);
				countMap.put(hot.getDocId(), count);
			}else{
				count++;
				countMap.put(hot.getDocId(), count);
			}
			IdItemMap.put(hot.getDocId(), hot);
		}
		
		
		HashMap<Integer, List<HotRankItem>> timesMap = new HashMap<Integer, List<HotRankItem>>();
		//分别将hotRankItem按照命中次数存入timesMap中
		Set<String> keySet = countMap.keySet();
		int i = 3;
		while(i >=1){
			for(String key : keySet){
				int c = countMap.get(key);
				if(c == i ){
					HotRankItem hot = IdItemMap.get(key);
					
					//test：临时处理办法，过滤掉一天以前的旧新闻
//					if(hot.getLifeTime()>24*60*60*1000){
//						LOG.info("out of time "+hot.getTitle()+" "+hot.getDocId());
//						continue;
//					}
					//查询solr将不在生命周期内的内容过滤掉
					if(hot.getDocId() == null||!SearchItemsFromSolr.searchItemIsAvailable(hot.getDocId())){
						LOG.info("out of time "+hot.getTitle()+" "+hot.getDocId());
						continue;
					}
					
					
					List<HotRankItem> hotlist = timesMap.get(i);
					if(hotlist == null){
						hotlist = new ArrayList<HotRankItem>();
						hotlist.add(hot);
						timesMap.put(i, hotlist);
					}else{
						hotlist.add(hot);
					}
				}
			}
			i--;
		}
		//将timesmap数据插入最终排序列表
		i=3;
		while(i >=1){
			List<HotRankItem> hotlist = timesMap.get(i);
			if(hotlist == null || hotlist.isEmpty()){
				i--;
				continue;
			}
			Collections.sort(hotlist, new Comparator<HotRankItem>() {

				@Override
				public int compare(HotRankItem o1, HotRankItem o2) {
					// TODO Auto-generated method stub
					if(o1.getHotScore()>o2.getHotScore()){
						return -1;
					}else if(o1.getHotScore()<o2.getHotScore()){
						return 1;
					}else{
						return 0;
					}
				}
			});
			for(HotRankItem hot : hotlist){
				hot.setHotLevel(String.valueOf(i));
				rankList.add(hot);
			}
			i--;
		}
		return rankList;
	}
	
	/**
	 * 预加载本地新闻排序函数
	 * 
	 * 排序策略暂时按照PV时间序
	 * 
	 * 在使用该函数前请主动更新HotItemLoadingUtil里的基础热点数据
	 * 
	 * 
	 * @param 
	 * 
	 * @return List<HotRankItem> 
	 */
	public static HashMap<String, List<HotRankItem>> localNewsHeatPredict(){
		HashMap<String, List<HotRankItem>> localMap = new HashMap<String, List<HotRankItem>>();
		HotItemLoadingUtil util = HotItemLoadingUtil.getInstance();
		List<String> keyList = util.getConfigKeyList("loc");
		for(String key : keyList){
			List<HotRankItem> tempLocalList = util.getHotRankList(key);
			if(tempLocalList == null || tempLocalList.isEmpty()){
				LOG.warn("This "+tempLocalList+" has no local news ");
				continue;
			}
			//先按时间排序
			Collections.sort(tempLocalList, new Comparator<HotRankItem>() {

				@Override
				public int compare(HotRankItem o1, HotRankItem o2) {
					// TODO Auto-generated method stub
					SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try{
						Date d1 = formate.parse(o1.getPublishTime());
						Date d2 = formate.parse(o2.getPublishTime());
						if(d1.before(d2)){
							return 1;
						}else if(d1.after(d2)){
							return -1;
						}else{
							return 0;
						}
					}catch(Exception e){
						LOG.error("sort error : ",e);
						return 0;
					}
				}
			});
			//再按PV排序
			Collections.sort(tempLocalList, new Comparator<HotRankItem>() {

				@Override
				public int compare(HotRankItem o1, HotRankItem o2) {
					// TODO Auto-generated method stub
					if(o1.getPv()>o2.getPv()){
						return -1;
					}else if(o1.getPv()<o2.getPv()){
						return 1;
					}else{
						return 0;
					}
				}
			});
			//再根据日期精确到天排序
			Collections.sort(tempLocalList, new Comparator<HotRankItem>() {

				@Override
				public int compare(HotRankItem o1, HotRankItem o2) {
					// TODO Auto-generated method stub
					SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd");
					try{
						Date d1 = formate.parse(o1.getPublishTime());
						Date d2 = formate.parse(o2.getPublishTime());
						if(d1.before(d2)){
							return 1;
						}else if(d1.after(d2)){
							return -1;
						}else{
							return 0;
						}
					}catch(Exception e){
						LOG.error("sort error : ",e);
						return 0;
					}
				}
			});
			
			localMap.put(key, tempLocalList);
		}
		return localMap;
	}
	/**
	 * 预加载优质新闻排序函数
	 * 
	 * 
	 * 在使用该函数前请主动更新HotItemLoadingUtil里的基础热点数据
	 * 
	 * 
	 * @param 
	 * 
	 * @return List<HotRankItem> 
	 */
	public static List<HotRankItem> highQualityNewsPreloaded(){
		ArrayList<HotRankItem> preloadList = new ArrayList<HotRankItem>();
		HashMap<String, List<HotRankItem>> preloadMap = new HashMap<String, List<HotRankItem>>();
		int count = 50;//用于限定每个优质分类下新闻数量
		//热点数据加载模块，加载所需的热度数据信息
		HotItemLoadingUtil util = HotItemLoadingUtil.getInstance();
		List<String> keylist = util.getConfigKeyList("good");
		for(String key : keylist){
			List<HotRankItem> hotlist = util.getHotRankList(key);
			if(hotlist == null||hotlist.isEmpty()){
				LOG.warn(key+" hotRankList is null ");
				continue;
			}
			if(hotlist.size()>count){
				hotlist = hotlist.subList(0, count);
			}
			preloadMap.put(key, hotlist);
		}
		for(int i=0;i<count;i++){
			Set<String> keySet = preloadMap.keySet();
			for(String key : keySet){
				List<HotRankItem> rankList = preloadMap.get(key);
				if(i<rankList.size()){
					preloadList.add(rankList.get(i));
				}
			}
		}
		return preloadList;		
	}
	
	
	/**
	 * 预加载白名单热点新闻排序函数
	 * 
	 * 
	 * 在使用该函数前请主动更新HotItemLoadingUtil里的基础热点数据
	 * 
	 * 
	 * @param 
	 * 
	 * @return HashMap<String,List<HotRankItem>> key为对应类别名
	 */
	public static HashMap<String, List<HotRankItem>> whiteListPreloadHotpredict(){
		HashMap<String, List<HotRankItem>> preloadMap = new HashMap<String, List<HotRankItem>>();
		//热点数据加载模块，加载所需的热度数据信息
		HotItemLoadingUtil util = HotItemLoadingUtil.getInstance();
		List<String> keylist = util.getWhiteListKey();
		for(String key : keylist){
			List<HotRankItem> hotlist = util.getHotRankList(key);
			if(hotlist == null||hotlist.isEmpty()){
				LOG.warn(key+" hotRankList is null ");
				continue;
			}
			for(HotRankItem hot : hotlist){
				hot.setHotScore(hot.getPv());
			}
			preloadMap.put(key, hotlist);
		}
		return preloadMap;		
	}
	
	/**
	 * slide doc 分离函数
	 * 
	 * 
	 * 
	 * 
	 * @param List<HotRankItem>
	 * 
	 * @return HashMap<String, List<HotRankItem>>
	 */
	public static HashMap<String, List<HotRankItem>> SplitSportsHotListBySlide(List<HotRankItem> hotList){
		if(hotList == null || hotList.size() == 0){
			return null;
		}
		HashMap<String, List<HotRankItem>> hotMap = new HashMap<String, List<HotRankItem>>();
		for(HotRankItem hot: hotList){
			if(hot.getDocType()!=null&&hot.getDocType().equals("slide")){
				List<HotRankItem> slideList = hotMap.get("slide");
				if(slideList == null){
					slideList = new ArrayList<HotRankItem>();
					slideList.add(hot);
					hotMap.put("slide", slideList);
				}else{
					slideList.add(hot);
				}
			}else{
				List<HotRankItem> docList = hotMap.get("doc");
				if(docList == null){
					docList = new ArrayList<HotRankItem>();
					docList.add(hot);
					hotMap.put("doc", docList);
				}else{
					docList.add(hot);
				}
			}
		}
		return hotMap;
	}
}
