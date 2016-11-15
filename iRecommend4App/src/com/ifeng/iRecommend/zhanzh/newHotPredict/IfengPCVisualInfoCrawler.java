/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.newHotPredict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;


/**
 * <PRE>
 * 作用 : 使用指定规则对PC页面视觉信息进行抓取
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
 *          1.0          2015年7月10日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class IfengPCVisualInfoCrawler {
	private static Log LOG = LogFactory.getLog("IfengPCVisualInfoCrawler");
	/**
	 * 获取体育主页视觉热度信息
	 * 
	 * @param 
	 * @return List : 包含视觉信息的HotRankItem
	 */
	public static List<HotRankItem> getIfengSportsPageHotList(){
		List<HotRankItem> sportsHotList = new ArrayList<HotRankItem>();
		String htmlStr = DownloadPageUtil.downloadPageByRetry("http://sports.ifeng.com/", "UTF-8", 3);
		if(htmlStr == null){
			LOG.error("Down load page error ");
			return null;
		}
		//初始化区域正则表达式
		List<String> newsAreaRegEx = new ArrayList<String>();
		newsAreaRegEx.add("<div class=\"box_hots clearfix\">[\\s\\S]*?<script src=");//焦点图区域
		newsAreaRegEx.add("<div class=\"box_txt01\">[\\s\\S]*?<var style=\"display:none"); // 头条区 
		List<String> pageAreaList = getPageAreaUtil(htmlStr, newsAreaRegEx);
		for(int i=0; i<pageAreaList.size();i++){
			List<String> newsTags = getNewsTagUtil(pageAreaList.get(i));
			List<HotRankItem> hotlist = parserNewsUtil(newsTags, i, 2);
			sportsHotList.addAll(hotlist);
		}
		return sportsHotList;
	}
	
	/**
	 * 获取主页视觉热度信息
	 * 
	 * 已经过期，建议用getIfengMainpageHotMap
	 * 
	 * @param newsArea : 首页新闻区域（0：要闻区；1：财经 2 ：汽车 3：娱乐 4：体育  5:房产家居  。。。)
	 * @return List : 包含视觉信息的HotRankItem
	 */
	public static List<HotRankItem> getIfengMainpageHotList(int newsArea){
		String htmlStr = DownloadPageUtil.downloadPageByRetry("http://www.ifeng.com/", "UTF-8", 3);
		if(htmlStr == null){
			LOG.error("Down load main page error ");
			return null;
		}
		List<String> newsAreaRegEx = new ArrayList<String>();
		newsAreaRegEx.add("<div id=\"headLineDefault\">[\\s\\S]*?<div id"); // 要闻区域正则表达式
		newsAreaRegEx.add("<div class=\"ColAM\">[\\s\\S]*?<div class=\"ColAR\">");// 各频道栏目区
		List<String> pageAreaList = getPageAreaUtil(htmlStr, newsAreaRegEx);
		if(newsArea<0||newsArea>pageAreaList.size()){
			LOG.error("newsArea out of bound ");
			return null;
		}
		String pageArea = pageAreaList.get(newsArea);
		List<String> newsTage = getNewsTagUtil(pageArea);
		List<HotRankItem> mainpageHotList = parserNewsUtil(newsTage, newsArea, 1);
		return mainpageHotList;
	}
	
	/**
	 * 获取主页视觉热度信息
	 * 
	 * @param newsArea : 首页新闻区域（0：要闻区；1：财经 2 ：汽车 3：娱乐 4：体育  5:房产家居  。。。)
	 * @return Map: 包含视觉信息的HotRankItem
	 */
	public static HashMap<String, List<HotRankItem>> getIfengMainpageHotMap(){
		String htmlStr = DownloadPageUtil.downloadPageByRetry("http://www.ifeng.com/", "UTF-8", 3);
		if(htmlStr == null){
			LOG.error("Down load main page error ");
			return null;
		}
		HashMap<String, List<HotRankItem>> hotmap = new HashMap<String, List<HotRankItem>>();
		List<String> newsAreaRegEx = new ArrayList<String>();
		newsAreaRegEx.add("<div id=\"headLineDefault\">[\\s\\S]*?<div id"); // 要闻区域正则表达式
		newsAreaRegEx.add("<div class=\"ColAM\">[\\s\\S]*?<div class=\"ColAR\">");// 各频道栏目区
		List<String> pageAreaList = getPageAreaUtil(htmlStr, newsAreaRegEx);
		for(int i=0;i<pageAreaList.size();i++){
			String pageArea = pageAreaList.get(i);
			List<String> newsTage = getNewsTagUtil(pageArea);
			List<HotRankItem> mainpageHotList = parserNewsUtil(newsTage, i, 1);
			hotmap.put(String.valueOf(i), mainpageHotList);
		}
		return hotmap;
	}
	
	public static List<HotRankItem> getIfengPCnewsList(){
		List<HotRankItem> pcnewsList = new ArrayList<HotRankItem>();
		
		HashSet<String> visitedUrl = new HashSet<String>();

		HashMap<String, List<HotRankItem>> mainpageNews = getIfengMainpageHotMap();
		if(mainpageNews == null){
			return null;
		}
		Set<String> keySet = mainpageNews.keySet();
		for(String key : keySet){
			List<HotRankItem> areaList = mainpageNews.get(key);
			for(HotRankItem hot : areaList){
				String url = hot.getUrl();
				if(url == null||visitedUrl.contains(url)){
					continue;
				}
				if(url.indexOf("shtml")>0){
					pcnewsList.add(hot);
				}else{
					String content = DownloadPageUtil.downloadPageByRetry(url, "UTF-8", 3);
					if(content == null){
						LOG.info("down "+url+" error ");
						continue;
					}
					List<String> newsTag = getNewsTagUtil(content);
					List<HotRankItem> tempList = parserNewsUtil(newsTag, new Integer(key), 2);
					for(HotRankItem temp : tempList){
						String tempUrl = temp.getUrl();
						if(tempUrl == null||visitedUrl.contains(tempUrl)){
							continue;
						}
						if(tempUrl.indexOf("shtml")>0){
							pcnewsList.add(temp);
						}
					}
				}
			}
		}
		return pcnewsList;
	}
	/**
	 * 将凤凰网页面分为几块不同的新闻区域
	 * 
	 * （0：要闻区；1：财经 2 ：汽车 3：娱乐 4：体育  5:房产家居  。。。)
	 * 
	 * @param htmlStr 页面的html字符串，newsAreaRegEx 为页面切块的正则表达式
	 * @return List : 提取出各个板块的html代码,存储版面顺序自上至下自左至右
	 */
	private static List<String> getPageAreaUtil(String htmlStr,List<String> newsAreaRegEx){
		ArrayList<String> newsArea = new ArrayList<String>();
		String regExNews = null;
		String news = null;
		Iterator<String> it = newsAreaRegEx.iterator();
		while (it.hasNext()) {
			regExNews = it.next();
			Pattern pattern = Pattern.compile(regExNews);
			Matcher matcher = pattern.matcher(htmlStr);
			while (matcher.find()) {
				news = matcher.group();
				newsArea.add(news);
			}
		}
		return newsArea;
	}
	
	/**
	 * 抽取各个区域块的新闻Tag标签并存入list中
	 * 
	 * 注意：该板块会分别取出加粗标题和普通标题
	 * 
	 * @param newsArea
	 *            ：凤首的新闻块字符串由getNewsArea函数处理得到
	 * @return list ： 保存有含有新闻信息的a Tag标签
	 */

	private static List<String> getNewsTagUtil(String newsArea) {
		if(newsArea != null){
			newsArea = newsArea.replaceAll("\\s", "");
		}
		List<String> newslist = new ArrayList<String>();
		List<String> newsRegExList = new ArrayList<String>();
		newsRegExList.add("<h\\d.*?>[\\s\\S]*?</h\\d>");
		newsRegExList.add("<a[^<|>]*?class=\"strong\"[^<|>]*?>[\\s\\S]*?</a>");
		newsRegExList.add("<a[^<|>]*?href=\"http://.{1,20}.ifeng.com/[^<|>]*?>[\\s\\S]*?</a>");
	
		Iterator<String> regExIt = newsRegExList.iterator();
		String news = null;
		while (regExIt.hasNext()) {
			Pattern pNews = Pattern.compile(regExIt.next());
			Matcher mNews = pNews.matcher(newsArea);
			while (mNews.find()) {
				news = mNews.group();
				newslist.add(news);
			}
		}
		return newslist;
	}
	
	/**
	 * 解析新闻标签，将对应的url与title以及是否为加粗特征等存入News类中
	 * 
	 * 注意：
	 * 
	 * @param newsTagList ：保存有含有新闻信息的a Tag标签,newsArea : 指明该新闻属于主页哪个区域，pageNum ： 指明是一级页面还是二级页面
	 *           
	 * @return list：返回存有提取完毕的News类列表
	 */
	private static List<HotRankItem> parserNewsUtil(List<String> newsTagList,int newsArea, int pageNum) {
		String newsTag = null;
		String title = null;
		String url = null;
		boolean isBold = false;
		List<HotRankItem> newsList = new ArrayList<HotRankItem>();
		//内部排重 
		HashSet<String> visitUrl = new HashSet<String>();
		
		String regEx_url = "http://.*?\"";
		String regEx_tag = "<a[^<|>]*?>[\\s\\S]*?</a>";
		String temp_tag = null;
		HotRankItem n = null;
		Iterator<String> it = newsTagList.iterator();
		while (it.hasNext()) {
			newsTag = it.next().replaceAll("\\s", " ");
			Pattern p_url = Pattern.compile(regEx_url);
			Matcher m_url = p_url.matcher(newsTag);
			title = newsTag.replaceAll("<[\\s\\S]*?>", "").trim();
			//去除title中包含（图）的字段
			title = title.replaceAll("\\(图\\)", "");
			if ((title.trim().length() >= 4) && m_url.find()
					&& !title.replaceAll("\\[.*?\\]", "").trim().equals("")) {// 去除连接中包含[焦点]，空标签，以及url不存在标签
				String tempUrl = m_url.group();
				url = tempUrl.substring(0, tempUrl.length() - 1);
				//去除重复的url
				if(visitUrl.contains(url)){
					continue;
				}else{
					visitUrl.add(url);
				}
				if (url.contains("list")) { // 去除url中包含list的非新闻类标签
					continue;
				} else if (newsTag.contains("</h3>")
						|| newsTag.contains("</h1>")
						|| newsTag.contains("</h4>")
						|| newsTag.contains("strong")
						|| newsTag.contains("</h2>")
						|| newsTag.contains("</b>")) { // 加粗标题
					isBold = true;
					Pattern p_tag = Pattern.compile(regEx_tag);
					Matcher m_tag = p_tag.matcher(newsTag);
					while (m_tag.find()) {
						temp_tag = m_tag.group();
						m_url = p_url.matcher(temp_tag);
						if (m_url.find()) {
							tempUrl = m_url.group();
							url = tempUrl.substring(0, tempUrl.length() - 1);
							title = temp_tag.replaceAll("<[\\s\\S]*?>", "").trim();
							n = new HotRankItem();
							n.setTitle(title);
							n.setUrl(url);
							n.setNewsArea(newsArea);
							n.setPageNum(pageNum);
							n.setBold(isBold);
					
							newsList.add(n);
						}
					}
				} else { // 普通标题
					isBold = false;
					n = new HotRankItem();
					n.setTitle(title);
					n.setUrl(url);
					n.setNewsArea(newsArea);
					n.setPageNum(pageNum);
					n.setBold(isBold);
					newsList.add(n);
				}
			}
		}
		return newsList;
	}
}
