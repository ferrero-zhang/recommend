package com.ifeng.iRecommend.featureEngineering;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * 
 * <PRE>
 * 作用 : 文本处理工具类
 *   包括从文章提取图片url，过滤非法字符，过滤html标签等方法
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
 *          1.0          2015-12-8         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class TextProTools {
	static Logger LOG = Logger.getLogger(TextProTools.class);
	/**
	 * 过滤非法字符
	 * 
	 * @param str
	 * @return
	 */
	public static String filterString(String str) {
		 String reStr = str;
		try {
			reStr = reStr.replaceAll("[^(\n\r\t \\!@#$%^&*()_\\-+=\\{\\}\\|\\[\\];:'\"\\<>,\\.?/`~·！@#￥%……&*（）——{}【】|、；‘’：《》，。？a-zA-Z0-9\\u4e00-\\u9fa5)]", "");
		} catch (Exception e) {
			LOG.error("[ERROR] Some error occured in filterString.", e);
			LOG.error("[ERROR] filterString str is " + str, e);
			return reStr;
		}
		return reStr;

	}
	
	/**
	 * 从正文中提取图片的url，存储下来，用于在分词完成后再拼接回原位置
	 * 
	 * @param s
	 * @return
	 */
	public static List<String> findPicUrl(String s) {
		if (s == null || s.isEmpty())
			return null;
		s = s.toLowerCase();
		List<String> picUrl = new ArrayList<String>();
		// 匹配图片的url
		Pattern pattern = Pattern.compile("http://.*?.(jpg|jpeg|gif|bmp|png|undefined)");
		Matcher matcher = pattern.matcher(s);
		int whileFlag = 0;
		while (matcher.find()) {
			whileFlag++;
			if (whileFlag > 200) {
				LOG.info("findPicUrl while cycle error.");
			}
			picUrl.add(matcher.group());
		}
		if (picUrl.size() >= 1)
			return picUrl;
		else
			return null;
	}
	/**
	 * 用于判断文章类型，用于区分doc和docpic,video三种
	 * 
	 * @param typeInfo
	 * @return
	 */
	static public String processDocType(String typeInfo, String other) {
		String typeResult = null;
		if (other.contains("source=phvideo"))
			typeResult = "video";
		else if (typeInfo.equals("0")) {
			typeResult = "doc";
		} else {
			typeResult = "docpic";
		}
		return typeResult;
	}
	/**
	 * 过滤html标签
	 * 
	 * @param s
	 * @return
	 */
	public static String filterHtml(String s) {
		if (s != null) {
			// \n type 1
			String str = s.replaceAll("</p>", "</p>\n");
			// \n type 2
			str = str.replaceAll("<br/>", "<br/>\n");
			// mark pictures with special stamp #p#
			str = str.replaceAll("<[img|IMG|Img][.[^<]]*/>", "#p#");
			// clear all html tags
			str = str.replaceAll("<[.[^<]]*>", " ");
			return str;
		} else {
			return s;
		}
	}
	/**
	 * 过滤html标签
	 * 
	 * @param s
	 * @return
	 */
	public static String filterHtmlPure(String s) {
		if (s != null) {
			// \n type 1
			String str = s.replaceAll("<[.[^<]]*>", "");
			str = str.replaceAll("\n", "");
			// \n type 2
//			str = str.replaceAll("<br/>", "<br/>\n");
			// mark pictures with special stamp #p#
//			str = str.replaceAll("<[img|IMG|Img][.[^<]]*/>", "#p#");
			// clear all html tags
//			str = str.replaceAll("<[.[^<]]*>", " ");
			return str;
		} else {
			return s;
		}
	}
}
