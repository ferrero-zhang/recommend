package com.ifeng.iRecommend.zhangxc.userlog.phonePrice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class JSoupBaiduSearcher extends AbstractBaiduSearcher{
    private static final Logger LOG = LoggerFactory.getLogger(JSoupBaiduSearcher.class);

    @Override
    public SearchResult search(String keyword) {
        return search(keyword, 1);
    }
    
    public String searchPhonePriceandBrand(String keyword, int page){
    	int pageSize = 10;
        String ret = "";
        String brand = "";
        String url = "http://www.baidu.com/s?pn="+(page-1)*pageSize+"&wd="+keyword;
        
        try {
            Document document = Jsoup.connect(url).get();            
            //获取搜索结果数目
            //int total = getBaiduSearchResultCount(document);
            int len = 10;                        
            for (int i = 1; i <= len; i++) {
            	try{
            		String cssQuery = "div#" + i; 
            		Element element = document.select(cssQuery).first();
            		Elements elementTitle = element.select("h3");
            		
            		if(elementTitle.text().contains("ZOL中关村在线") || elementTitle.text().contains("参数_图片")){
            			String href = elementTitle.select("a[href]").first().attr("href");
            			Document documentZOL = Jsoup.connect(href).get();
            			ret = documentZOL.select(".product-price-info").select(".price-type").first().text();
            			if(!ret.matches("^[0-9]*$"))     //匹配数字
            				ret = "";
            			
            			try {
            				brand = documentZOL.select(".page-title").select("h1").first().text().trim();
            				if(brand.equals(""))
            					brand = documentZOL.select(".page-title").select(".product-name").first().text().trim();
            			} catch (Exception e) {
            				brand = documentZOL.select(".page-title").select(".product-name").first().text().trim();
            			}
            			
          			
            			if(!brand.equals("") && !ret.equals(""))
            				ret = ret + "#" + brand;
            			
            			break;
            		} else continue;
            		
            	} catch (Exception e) {
            		LOG.debug("Cssquery error: " + e.getMessage());
            		continue;
            	}
            }
                     
        } catch (IOException ex) {
            LOG.error("搜索出错",ex);
        }
        return ret;
    }
    
    public int searchPhonePrice(String keyword, int page){
    	int pageSize = 10;
    	int price = 0;  //手机价格
        //百度搜索结果每页大小为10，pn参数代表的不是页数，而是返回结果的开始数
        //如获取第一页则pn=0，第二页则pn=10，第三页则pn=20，以此类推，抽象出模式：(page-1)*pageSize
        String url = "http://www.baidu.com/s?pn="+(page-1)*pageSize+"&wd="+keyword;
        
        try {
            Document document = Jsoup.connect(url).get();
            
            //获取搜索结果数目
            //int total = getBaiduSearchResultCount(document);
            int len = 10;
            //if (total < 1) {
                //return 0;
            //}
            //如果搜索到的结果不足一页
           // if (total < 10) {
            //    len = total;
          // }
                        
            for (int i = 1; i <= len; i++) {
            	try{
            		String cssQuery = "div#" + i; 
            		Element element = document.select(cssQuery).first();
            		Elements elementTitle = element.select("h3");
            		
            		if(elementTitle.text().contains("ZOL中关村在线") || elementTitle.text().contains("参数_图片")){
            			Element priceEle = element.select(".c-row").select(".ecl-pc-digital-orange").first();
            			if(priceEle == null){
            				String href = elementTitle.select("a[href]").first().attr("href");
            				//LOG.info("Get ZOL Page: " + href);
            				Document documentZOL = Jsoup.connect(href).get();
            				//price = Integer.parseInt( documentZOL.select("#J_PriceTrend").select(".price-type").first().text() );
            				//if( price <= 0 )
            				price = Integer.parseInt( documentZOL.select(".product-price-info").select(".price-type").first().text() );
            			} else 
            				price = Integer.parseInt( priceEle.text().substring(1) );
            			
            			break;
            		} else continue;
            		
            	} catch (Exception e) {
            		LOG.debug("Cssquery error: " + e.getMessage());
            		continue;
            	}
            }
                     
        } catch (IOException ex) {
            LOG.error("搜索出错",ex);
        }
        return price;
    }
    
    @Override
    public SearchResult search(String keyword, int page) {
        int pageSize = 10;
        //百度搜索结果每页大小为10，pn参数代表的不是页数，而是返回结果的开始数
        //如获取第一页则pn=0，第二页则pn=10，第三页则pn=20，以此类推，抽象出模式：(page-1)*pageSize
        String url = "http://www.baidu.com/s?pn="+(page-1)*pageSize+"&wd="+keyword;
        
        SearchResult searchResult = new SearchResult();
        searchResult.setPage(page);
        List<Webpage> webpages = new ArrayList();
        try {
            Document document = Jsoup.connect(url).get();
            
            //获取搜索结果数目
            int total = getBaiduSearchResultCount(document);
            searchResult.setTotal(total);
            int len = 10;
            if (total < 1) {
                return null;
            }
            //如果搜索到的结果不足一页
            if (total < 10) {
                len = total;
            }
            
            
            for (int i = 1; i <= len; i++) {
            	try{
            		String cssQuery = "div#" + i; 
            		Element element = document.select(cssQuery).first();
            		Elements elementTitle = element.select("h3");
            		
            		if(elementTitle.text().contains("ZOL中关村在线")){
            			
            		} else continue;
            		
            		LOG.info(elementTitle.text());
            	} catch (Exception e) {
            		LOG.error("Cssquery error: " + e.getMessage());
            		continue;
            	}
            }
            
            
            
            for (int i = 0; i < len; i++) {
                String titleCssQuery = "html body div div div div#content_left div#" + (i + 1 + (page-1)*pageSize) + ".result.c-container h3.t a";
                String summaryCssQuery = "html body div div div div#content_left div#" + (i + 1 + (page-1)*pageSize) + ".result.c-container div.c-abstract";
                LOG.debug("titleCssQuery:" + titleCssQuery);
                LOG.debug("summaryCssQuery:" + summaryCssQuery);
                Element titleElement = document.select(titleCssQuery).first();
                String href = "";
                String titleText = "";
                if(titleElement != null){
                    titleText = titleElement.text();
                    href = titleElement.attr("href");
                }else{
                    //处理百度百科
                    titleCssQuery = "html body div#out div#in div#wrapper div#container div#content_left div#1.result-op h3.t a";
                    summaryCssQuery = "html body div#out div#in div#wrapper div#container div#content_left div#1.result-op div p";
                    LOG.debug("处理百度百科 titleCssQuery:" + titleCssQuery);
                    LOG.debug("处理百度百科 summaryCssQuery:" + summaryCssQuery);
                    titleElement = document.select(titleCssQuery).first();
                    if(titleElement != null){
                        titleText = titleElement.text();
                        href = titleElement.attr("href");
                    }
                }
                LOG.debug(titleText);
                Element summaryElement = document.select(summaryCssQuery).first();
                //处理百度知道
                if(summaryElement == null){
                    summaryCssQuery = summaryCssQuery.replace("div.c-abstract","font");
                    LOG.debug("处理百度知道 summaryCssQuery:" + summaryCssQuery);
                    summaryElement = document.select(summaryCssQuery).first();
                }
                String summaryText = "";
                if(summaryElement != null){
                    summaryText = summaryElement.text(); 
                }
                LOG.debug(summaryText);                
                
                if (titleText != null && !"".equals(titleText.trim()) && summaryText != null && !"".equals(summaryText.trim())) {
                    Webpage webpage = new Webpage();
                    webpage.setTitle(titleText);
                    webpage.setUrl(href);
                    webpage.setSummary(summaryText);
                    if (href != null) {
                        String content = Tools.getHTMLContent(href);
                        webpage.setContent(content);
                    } else {
                        LOG.info("页面正确提取失败");
                    }
                    webpages.add(webpage);
                } else {
                    LOG.error("获取搜索结果列表项出错:" + titleText + " - " + summaryText);
                }
            }
            
            
        } catch (IOException ex) {
            LOG.error("搜索出错",ex);
        }
        searchResult.setWebpages(webpages);;
        return searchResult;
    }
    /**
     * 获取百度搜索结果数
     * 获取如下文本并解析数字：
     * 百度为您找到相关结果约13,200个
     * @param document 文档
     * @return 结果数
     */
    private int getBaiduSearchResultCount(Document document){
        String cssQuery = "html body div div div div.nums";
        LOG.debug("total cssQuery: " + cssQuery);
        Element totalElement = document.select(cssQuery).first();
        String totalText = totalElement.text(); 
       // LOG.info("搜索结果文本：" + totalText);
        
        String regEx="[^0-9]";   
        Pattern pattern = Pattern.compile(regEx);      
        Matcher matcher = pattern.matcher(totalText);
        totalText = matcher.replaceAll("");
        int total = Integer.parseInt(totalText);
       // LOG.info("搜索结果数：" + total);
        return total;
    }

    
    public static void readPhoneBrandinLog(String file){
    	Set<String> phoneVersion = new HashSet<String>();
    	int count = 0;   //查询不到的品牌
    	//解析日志
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		    for(String data = br.readLine(); data != null; data = br.readLine()){	 
		        String[] infos = data.split("	");
		        phoneVersion.add(infos[6]);
		    }
		    br.close();
		} catch (IOException e) {
			LOG.error(e.getMessage());
		} 	
    	
				
		for(String phone : phoneVersion){			
			//存储到redis
			Jedis jedis = null; 
			
			try{
				jedis = new Jedis("10.90.1.58",6379,5000);
				jedis.select(4);
				if(jedis.get(phone) != null)  //已存在过滤
					continue;
				//搜索手机报价
				JSoupBaiduSearcher searcher = new JSoupBaiduSearcher();
				//int phonePrice = searcher.searchPhonePrice(phone, 1);
                String ret = searcher.searchPhonePriceandBrand(phone, 1);
				
				if(ret.equals("")){   //查询不到
					LOG.error("查询不到该品牌： " + phone);
					count++;
					continue;
				}
												
				jedis.set(phone, ret);
				LOG.info("查询到品牌：" + phone + "  价格： " + ret);
			}catch(Exception e){
				LOG.error("get user local error "+phoneVersion,e);
				jedis.disconnect();
				e.printStackTrace();
				continue;
			}
			jedis.disconnect();
		}
    	
		LOG.info("该文件品牌总数：" + phoneVersion.size() + "  查询出错总数： " + count);
		
	}
    
    
    public static void updateRedis(){
    	Jedis jedis = new Jedis("10.90.1.58",6379,5000);
		jedis.select(4);
    	Set<String> keys = jedis.keys("*");
    	
    	
    	for(String key: keys){
    		try{
    			if(jedis.get(key).contains("#"))
    				continue;
    			
    			JSoupBaiduSearcher searcher = new JSoupBaiduSearcher();
    			String ret = searcher.searchPhonePriceandBrand(key, 1);
    			
    			if(ret.equals("")){   //查询不到
    				LOG.error("查询不到该品牌： " + key);
    				continue;
    			}
    			
    			jedis.set(key, ret);
    			LOG.info("查询到品牌：" + key + "  价格、品牌： " + ret);	
    		} catch(Exception e){
    			LOG.error("get phone brand error " + key, e);
    			jedis.disconnect();
    			e.printStackTrace();
    			continue;
    		}
    		
    	}
    	jedis.disconnect();
		
    }
    
    public static void main(String[] args) {  
    	readPhoneBrandinLog("E:\\clientLogs\\2016-08-21\\2230.sta");
    	//updateRedis();
    	//搜索手机报价
		//JSoupBaiduSearcher searcher = new JSoupBaiduSearcher();
		//searcher.searchPhonePriceandBrand("t29", 1);
    }
}