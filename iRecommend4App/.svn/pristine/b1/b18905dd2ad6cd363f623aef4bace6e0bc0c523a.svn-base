package com.ifeng.iRecommend.dingjw.itemParser;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <PRE>
 * 作用 : 
 *   url预处理类
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
 *          1.0          2014-04-15      liu_yi        create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class UrlPretreatment {
	//private static final Log logger = LogFactory.getLog(UrlPretreatment.class.getName());
	private static Logger logger = Logger.getLogger(UrlPretreatment.class.getName());
	
	/**
	 * 
	 * @Title isShortUrl
	 * @Description 判断一个url是否是cmpp短url(http://news.ifeng.com/a/20140414/35742621_0.shtml)
	 * @param url
	 * @author LiuYi
	 * @return
	 */
	public static boolean isShortUrl(String url) {
		String shortUrl = "^http://.+/[a-zA-z]/[0-9]+/.*html$";
		Pattern parttenShortUrl = Pattern.compile(shortUrl, Pattern.CASE_INSENSITIVE);
		Matcher mat = parttenShortUrl.matcher(url);  
		if (url == null || url.equals("")) {
			return false;
		} else {
			if (mat.find()) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	private static String getTxtFromPage(String s,String rex){
		int i=0;
		while(i<3){
				//String s=HttpUtil.doGet(url, 5000, 10000);
				Pattern pat=Pattern.compile(rex,Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
				Matcher mat=pat.matcher(s);
				String res=null;
				while(mat.find()){
					res=mat.group();
					break;
				}
				
				i++;
				return res;
		}
		return null;
	}
	
	private static String getHtml(String url){
		int i=0;
		String s=null;
		while(i<3){
			try {
				s=HttpUtil.doGet(url, 5000, 5000);
				return s;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage(), e);
			}
			i++;
		}
		return s;
	}
	
	public static String getLongUrl(String url) {
		String result = null;
		try {
			String s = getHtml(url);
			String srcResult = getTxtFromPage(s,"<script>\\s+function\\s+getChannelInfo().*?}\\s+</script>");
			if(null != srcResult) {
				String subString = srcResult.substring(srcResult.indexOf("return") + "return".length(), srcResult.indexOf("}") - 1).replaceAll("\"|;", "").trim();
				result = subString;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
	
	/**
	 * 
	 * @Title urlProcessing
	 * @Description urlProcessing
	 *              url处理：传入一个url，如果是长url，原值返回；否则，抓取短url页面，得到对应的url层级；同时，
	 *              写入一份到HBbase中
	 *              注意：对应的url层级可能有多个，也可能有一个，用逗号分割;当层级数目为一个的时候，不能用于建special（不准确）
	 *              
	 * @param url
	 * @author LiuYi
	 * @return  输入短url时，返回值是一个拼接的假url，带special层次关系；输入长URL时，原值返回
	 */
	public static String urlProcessing(String url) {
		if(url == null || url.length() < 9)
			return null;
		String result = null;
		Pattern pattern = Pattern.compile("http:/+");
		Matcher matcher = pattern.matcher(url);
		url = matcher.replaceFirst("http://");
		String longUrl = "";
		if (!isShortUrl(url)) {
			result = url.substring(0, url.lastIndexOf("/")+1);
			return result;
		} else {
			longUrl = getLongUrl(url);
			result = longUrl;
			
		}
		return result;
	}
	
	public static void main(String[] args) {
		String longUrls = urlProcessing("http://news.ifeng.com/a/20140714/41143386_0.shtml");
		//String res = urlProcessing("http://ent.ifeng.com/a/20140424/40029531_0.shtml");
		//String longUrls = urlProcessing("http://news.ifeng.com/world/special/obamayatai/content-3/detail_2014_04/21/35921140_0.shtml");
		System.out.println(longUrls);
	}
	
}
