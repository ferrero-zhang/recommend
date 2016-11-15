/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.preloadUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;

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
 *          1.0          2015年12月11日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class NewsSortUtil {
	private static Log LOG = LogFactory.getLog("NewsSortUtil");
	public static List<PreloadItem> sortAlgorithmChannelNewsList(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return itemlist;
		}
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<PreloadItem> todayList = new ArrayList<PreloadItem>();
		List<PreloadItem> otherList = new ArrayList<PreloadItem>();
		for(PreloadItem item : itemlist){
			try {
				Date date = dateFormate.parse(item.getFitem().getDate());
				long lifeTime = System.currentTimeMillis() - date.getTime();
				if(lifeTime < 18*60*60*1000 && item.isRelatedSearchItem()){ //isRelatedSearchItem指是本次solr搜索中命中的数据
					todayList.add(item);
				}else if(lifeTime < 24 * 60 * 60 * 1000 && lifeTime>0 && item.getRankscore() > 0.25 && item.isRelatedSearchItem()){
					todayList.add(item);
				}else{
					otherList.add(item);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error(" ", e);
				otherList.add(item);
				continue;
			}
		}
		//将数据按相似度排序，相似度>0.5
		List<PreloadItem> todayMoreSimList = new ArrayList<PreloadItem>();
		List<PreloadItem> todayLessSimList = new ArrayList<PreloadItem>();
		for(PreloadItem item : todayList){
			if(item.getSimScore() > 0.5){
				todayMoreSimList.add(item);
			}else{
				todayLessSimList.add(item);
			}
		}
		todayList = new ArrayList<PreloadItem>();
		
		//相似得分高的排在前面
		if(!todayMoreSimList.isEmpty()){
			sortListByMinutes(todayMoreSimList);
			sortListByHotScore(todayMoreSimList);
			sortListByPVNum(todayMoreSimList);
			sortListBySlide(todayMoreSimList);
			
			todayList.addAll(todayMoreSimList);
		}
		
		//相似得分低的排在相对后面
		if(!todayLessSimList.isEmpty()){
			sortListByMinutes(todayLessSimList);
			sortListByHotScore(todayLessSimList);
			sortListByPVNum(todayLessSimList);
			sortListBySlide(todayLessSimList);
			
			todayList.addAll(todayLessSimList);
		}

		//排序今日的新闻
//		sortListByMinutes(todayList);
//		sortListByRankScore(todayList);
//		//保证今日新闻前20条均有图
//		sortListBySlide(todayList);

		//排序历史的新闻
		sortListByMinutes(otherList);
		sortListByRankScore(otherList);
		sortListByPVNum(otherList);
		sortListByDay(otherList);
		todayList.addAll(otherList);

		return todayList;
	}
	
	public static List<PreloadItem> sortSFChannelNewsList(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return itemlist;
		}
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<PreloadItem> todayList = new ArrayList<PreloadItem>();
		List<PreloadItem> otherList = new ArrayList<PreloadItem>();
		for(PreloadItem item : itemlist){
			try {
				Date date = dateFormate.parse(item.getFitem().getDate());
				long lifeTime = System.currentTimeMillis() - date.getTime();
				if(lifeTime < 18*60*60*1000){ 
					todayList.add(item);
				}else{
					otherList.add(item);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error(" ", e);
				otherList.add(item);
				continue;
			}
		}
		//将数据按相似度排序，相似度10/7/5/<
		List<List<PreloadItem>> tempranklist = new ArrayList<List<PreloadItem>>();//按照相似度分块的List
		for(int i=0;i<3;i++){
			List<PreloadItem> templist = new ArrayList<PreloadItem>();
			tempranklist.add(templist);
		}
		
		for(PreloadItem item : todayList){
			if(item.getSimScore()>= 7){
				List<PreloadItem> templist = tempranklist.get(0);
				templist.add(item);
			}else if(item.getSimScore()>= 5){
				List<PreloadItem> templist = tempranklist.get(1);
				templist.add(item);
			} else {
				List<PreloadItem> templist = tempranklist.get(2);
				templist.add(item);
			}
		}
		
		todayList = new ArrayList<PreloadItem>();
		
		for(List<PreloadItem> tempList : tempranklist){
			if(!tempList.isEmpty()){
				
				
				sortListByMinutes(tempList);
				sortListByHotScore(tempList);
				sortListByPVNum(tempList);
				sortListBySlide(tempList);
				todayList.addAll(tempList);
			}
		}
		
		//test 对slide与doc分割拼接
//		LOG.info("Test befor insertSlide size : "+todayList.size());
		//优先满足编辑打分排序
		sortListByEditorScore(todayList);
		todayList = insertSlideToDoc(todayList);
//		LOG.info("Test after insertSlide size : "+todayList.size());
		

		//排序历史的新闻
		
		sortListByMinutes(otherList);
		sortListByRankScore(otherList);
		sortListByPVNum(otherList);
		sortListByEditorScore(otherList);
		sortListByDay(otherList);
		todayList.addAll(otherList);

		return todayList;
	}
	
	
	public static List<PreloadItem> sortAlgorithmChannelNewsList_new(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return itemlist;
		}
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<PreloadItem> todayList = new ArrayList<PreloadItem>();
		List<PreloadItem> otherList = new ArrayList<PreloadItem>();
		for(PreloadItem item : itemlist){
			try {
				Date date = dateFormate.parse(item.getFitem().getDate());
				long lifeTime = System.currentTimeMillis() - date.getTime();
				if(lifeTime < 18*60*60*1000 && item.isRelatedSearchItem()){ //isRelatedSearchItem指是本次solr搜索中命中的数据
					todayList.add(item);
				}else if(lifeTime < 24 * 60 * 60 * 1000 && lifeTime>0 && item.getRankscore() > 0.5 && item.isRelatedSearchItem()){
					todayList.add(item);
				}else{
					otherList.add(item);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error(" ", e);
				otherList.add(item);
				continue;
			}
		}
		//将数据按相似度排序，相似度10/7/5/<
		List<List<PreloadItem>> tempranklist = new ArrayList<List<PreloadItem>>();//按照相似度分块的List
		for(int i=0;i<3;i++){
			List<PreloadItem> templist = new ArrayList<PreloadItem>();
			tempranklist.add(templist);
		}
		
		for(PreloadItem item : todayList){
			if(item.getSimScore()>= 7){
				List<PreloadItem> templist = tempranklist.get(0);
				templist.add(item);
			}else if(item.getSimScore()>= 5){
				List<PreloadItem> templist = tempranklist.get(1);
				templist.add(item);
			} else {
				List<PreloadItem> templist = tempranklist.get(2);
				templist.add(item);
			}
		}
		
		todayList = new ArrayList<PreloadItem>();
		
		for(List<PreloadItem> tempList : tempranklist){
			if(!tempList.isEmpty()){
				sortListByMinutes(tempList);
//				sortListByHotScore(tempList);
//				sortListByPVNum(tempList);
				sortListByhotBoost(tempList);
				sortListBySlide(tempList);
				
				todayList.addAll(tempList);
			}
		}
		
		//test 对slide与doc分割拼接
//		LOG.info("Test befor insertSlide size : "+todayList.size());
		todayList = insertSlideToDoc(todayList);
//		LOG.info("Test after insertSlide size : "+todayList.size());
		

		//排序今日的新闻
//		sortListByMinutes(todayList);
//		sortListByRankScore(todayList);
//		//保证今日新闻前20条均有图
//		sortListBySlide(todayList);

		//排序历史的新闻
		sortListByMinutes(otherList);
//		sortListByRankScore(otherList);
//		sortListByPVNum(otherList);
		sortListByhotBoost(otherList);
		sortListByDay(otherList);
		todayList.addAll(otherList);

		return todayList;
	}
	
	
	public static List<PreloadItem> sortLocChannelNewsList(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return itemlist;
		}
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<PreloadItem> todayList = new ArrayList<PreloadItem>();
		List<PreloadItem> otherList = new ArrayList<PreloadItem>();
		for(PreloadItem item : itemlist){
			try {
				//强制限制，对得分小于1.0的直接过滤掉
				if(item.getSimScore() < 10){
					LOG.warn(item.getFitem().getDocId()+" "+item.getFitem().getTitle()+""+item.getSimScore()+" simscore<10 removed ");
					continue;
				}
				
				Date date = dateFormate.parse(item.getFitem().getDate());
				long lifeTime = System.currentTimeMillis() - date.getTime();
				if(lifeTime < 12*60*60*1000 && item.isRelatedSearchItem()){ //isRelatedSearchItem指是本次solr搜索中命中的数据
					//只对"今天"的新闻的PV进行HackerNews降权
//					int hackerPv = (int) (hackerNews(item.getPv(), lifeTime)*100);
//					item.setPv(hackerPv);
					double hackerPv = (hackerNews(item.getPv(), lifeTime)*100);
					item.setRankscore(hackerPv);
					todayList.add(item);
				}else if(lifeTime < 18 * 60 * 60 * 1000 && lifeTime>0 && item.getPv() > 100 && item.isRelatedSearchItem()){
					//只对"今天"的新闻的PV进行HackerNews降权
//					int hackerPv = (int) (hackerNews(item.getPv(), lifeTime)*100);
//					item.setPv(hackerPv);
					
					double hackerPv = (hackerNews(item.getPv(), lifeTime)*100);
					item.setRankscore(hackerPv);
					
					todayList.add(item);
				}else{
					otherList.add(item);
				}
				
//				int hackerPv = (int) (hackerNews(item.getPv(), lifeTime)*100);
				
				//test
//				if(item.getPv() != 0){
//					System.out.println("Id : "+item.getFitem().getDocId()+" PV : "+item.getPv()+" hackerPV : "+hackerPv);
//				}
				

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error(" ", e);
				otherList.add(item);
				continue;
			}
		}

		//排序今日的新闻
		sortListByMinutes(todayList);
		
		
		//保证今日新闻前20条均有图
		sortListBySlide(todayList);
		sortListByPVNum(todayList);
		sortListByRankScore(todayList);
		
		//排序历史的新闻
		sortListByMinutes(otherList);
		sortListByRankScore(otherList);
		sortListByPVNum(otherList);
		sortListByDay(otherList);
		todayList.addAll(otherList);

		return todayList;
	}
	
	public static List<PreloadItem> sortEditorChannelNewsList(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return itemlist;
		}
		SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<PreloadItem> todayList = new ArrayList<PreloadItem>();
		List<PreloadItem> otherList = new ArrayList<PreloadItem>();
		for(PreloadItem item : itemlist){
			try {
				Date date = dateFormate.parse(item.getFitem().getDate());
				long lifeTime = System.currentTimeMillis() - date.getTime();
				//把12小时内的新闻列为今天的新闻
				if(lifeTime < 18*60*60*1000 ){ 
					todayList.add(item);
				} else {
					otherList.add(item);
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				LOG.error(" ", e);
				otherList.add(item);
				continue;
			}
		}

		//排序今日的新闻
		sortListByRankScore(todayList);
		
		//排序历史的新闻

		sortListByRankScore(otherList);
		sortListByDay(otherList);
		todayList.addAll(otherList);

		return todayList;
	}
	
	private static double hackerNews(int pv, double lifeTime) {
		double score = 0.0;
		if (pv == 0) {
			return score;
		}
		lifeTime = lifeTime / (1000 * 60 * 60);
		double G = 1.8;     //1.8;
		double under = Math.pow((lifeTime + 2), G);
		score = (pv - 1) / under;
		return score;
	}
	
	private static void sortListByDay(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return ;
		}
		Collections.sort(itemlist, new Comparator<PreloadItem>() {

			@Override
			public int compare(PreloadItem o1, PreloadItem o2) {
				// TODO Auto-generated method stub
				SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
				try {
					Date d1 = dateFormate.parse(o1.getFitem().getDate());
					Date d2 = dateFormate.parse(o2.getFitem().getDate());
					if(d1.before(d2)){
						return 1;
					}else if(d1.equals(d2)){
						return 0;
					}else{
						return -1;
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					LOG.error(" ", e);
					return -1;
				}
			}
		});
	}
	
	private static void sortListByMinutes(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return ;
		}
		Collections.sort(itemlist, new Comparator<PreloadItem>() {

			@Override
			public int compare(PreloadItem o1, PreloadItem o2) {
				// TODO Auto-generated method stub
				SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					Date d1 = dateFormate.parse(o1.getFitem().getDate());
					Date d2 = dateFormate.parse(o2.getFitem().getDate());
					if(d1.before(d2)){
						return 1;
					}else if(d1.equals(d2)){
						return 0;
					}else{
						return -1;
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					LOG.error(" ", e);
					return -1;
				}
			}
		});
	}
	
	private static void sortListByRankScore(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return ;
		}
		Collections.sort(itemlist, new Comparator<PreloadItem>() {

			@Override
			public int compare(PreloadItem o1, PreloadItem o2) {
				// TODO Auto-generated method stub
				if(o1.getRankscore()>o2.getRankscore()){
					return -1;
				}else if(o1.getRankscore()<o2.getRankscore()){
					return 1;
				}else{
					return 0;
				}
			}
		});
	}
	
	private static void sortListByEditorScore(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return ;
		}
		Collections.sort(itemlist, new Comparator<PreloadItem>() {

			@Override
			public int compare(PreloadItem o1, PreloadItem o2) {
				// TODO Auto-generated method stub
				if(o1.getEditorScore()>o2.getEditorScore()){
					return -1;
				}else if(o1.getEditorScore()<o2.getEditorScore()){
					return 1;
				}else{
					return 0;
				}
			}
		});
	}
	/*
	 * 保证排序文章前20条均有图
	 * 
	 */
	private static void sortListBySlide(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return ;
		}
		Collections.sort(itemlist, new Comparator<PreloadItem>() {

			@Override
			public int compare(PreloadItem o1, PreloadItem o2) {
				// TODO Auto-generated method stub
				
				if((o1.getFitem().getDocType().equals("docpic")|| o1.getFitem().getDocType().equals("slide"))
						&& o2.getFitem().getDocType().equals("doc")){
					return -1;
				}else if(o1.getFitem().getDocType().equals(o2.getFitem().getDocType())){
					return 0;
				}else{
					return 1;
				}
			}
		});
	}
	
	private static void sortListByHotScore(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return ;
		}
		Collections.sort(itemlist, new Comparator<PreloadItem>() {

			@Override
			public int compare(PreloadItem o1, PreloadItem o2) {
				// TODO Auto-generated method stub
				if(o1.getHotscore()>o2.getHotscore()){
					return -1;
				}else if(o1.getHotscore()<o2.getHotscore()){
					return 1;
				}else{
					return 0;
				}
			}
		});
	}
	
	private static void sortListByPVNum(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return ;
		}
		Collections.sort(itemlist, new Comparator<PreloadItem>() {

			@Override
			public int compare(PreloadItem o1, PreloadItem o2) {
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
	}
	
	private static void sortListByhotBoost(List<PreloadItem> itemlist){
		if(itemlist == null || itemlist.isEmpty()){
			return ;
		}
		Collections.sort(itemlist, new Comparator<PreloadItem>() {

			@Override
			public int compare(PreloadItem o1, PreloadItem o2) {
				// TODO Auto-generated method stub
				try {
					if(o1.getFitem().getHotBoost()>o2.getFitem().getHotBoost()){
						return -1;
					}else if(o1.getFitem().getHotBoost()<o2.getFitem().getHotBoost()){
						return 1;
					}else{
						//相同的根据pv排序
//						if(o1.getPv()>o2.getPv()){
//							return -1;
//						}else if(o1.getPv()<o2.getPv()){
//							return 1;
//						}else{
//							return 0;
//						}
						return 0;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOG.error("sortListByhotBoost",e);
					return 0;
				}
			}
		});
	}
	/*
	 * 
	 * 将预加载List分割为幻灯和文章并进行有效排版
	 * 3篇文章插一篇slide
	 * 
	 */
	private static List<PreloadItem> insertSlideToDoc(List<PreloadItem> itemlist){
		
		List<PreloadItem> returnlist = new ArrayList<PreloadItem>();
		if(itemlist == null || itemlist.isEmpty()){
			return returnlist;
		}
		List<PreloadItem> tempSlideList = new ArrayList<PreloadItem>();
		List<PreloadItem> tempDocList = new ArrayList<PreloadItem>();
		//现将幻灯分割
		for(PreloadItem item : itemlist){
			if(item.getFitem().getDocType().equals("slide")){
				tempSlideList.add(item);
			}else{
				tempDocList.add(item);
			}
		}
		
		
		//再做插入排序
		int slideIndex = 0;
		int docIndex = 0;
		int length = tempSlideList.size()+tempDocList.size();
		for(int i=1;i<=length;i++){
			if(i%4 == 0 && slideIndex<tempSlideList.size()){
				returnlist.add(tempSlideList.get(slideIndex));
				slideIndex++;
			} 
			if(i%4 != 0 && docIndex<tempDocList.size()){
				returnlist.add(tempDocList.get(docIndex));
				docIndex++;
			}
			if(slideIndex >= tempSlideList.size()){
				returnlist.addAll(tempDocList.subList(docIndex, tempDocList.size()));
				break;
			}
			if(docIndex >= tempDocList.size()){
				returnlist.addAll(tempSlideList.subList(slideIndex, tempSlideList.size()));
				break;
			}
		}
		return returnlist;
	}

}
