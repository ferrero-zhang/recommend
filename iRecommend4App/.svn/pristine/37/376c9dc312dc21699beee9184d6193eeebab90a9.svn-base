package com.ifeng.iRecommend.kedm.userlog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LogParserFunc {
	private static final Log log = LogFactory.getLog(LogParserFunc.class);

	/**
	 * @Title: getOpenDocID
	 * @Description: 提取打开文章的docid(正常文章，不是关键词列表，也不是特殊页)，有时候有翻页的，如123_5,同样需要处理下
	 * @author liu_yi
	 * @param fieldValue
	 * @return
	 * @throws
	 */
	public static String getOpenDocID(String detail) {
		// 不合法的情况
		if (detail.isEmpty() || !isPageTypeDetail(detail)) {
			return null;
		}

		String result = "";
		String idValue = detail.split("\\$")[0];
		// 不是翻页的阅读行为
		if (-1 == idValue.indexOf("_")) {
			result = idValue.substring("id=".length(), idValue.length());
		} else { // 翻页行为
			int end = idValue.indexOf("_");
			result = idValue.substring("id=".length(), end);
		}

		return result;
	}
	
	public static String getOpenKeyword(String detail) {
		if (detail.isEmpty() || !isKwTypeDetail(detail)) {
			return null;
		}
		
		String result = "";
		String idValue = detail.split("\\$")[0];
		// 不是翻页的阅读行为
		if (-1 == idValue.indexOf("_")) {
			result = idValue.substring("id=".length(), idValue.length());
		} else { // 翻页行为
			int end = idValue.length();
			result = idValue.substring("id=kw_".length(), end);
		}

		// 排除没有意义的头条
		/*if (CommonFuncUtils.valueInArrayJudge(GlobalParams.noSenseKeyword, result)) {
			result = "";
		}*/
		
		return result;
	}
	
	public static boolean isKwTypeDetail(String inputStr) {
		Pattern pattern = Pattern.compile("id=kw_.*\\$.*");
		Matcher isPageType = pattern.matcher(inputStr);
		
		if (!isPageType.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * @Title: getOpenpushDocID
	 * @Description: 提取推送打开的文章id
	 * @author liu_yi
	 * @param detail
	 * @return
	 * @throws
	 */
	public static String getOpenpushDocID(String detail) {
		// 不合法的情况
		if (detail.isEmpty() || !isPageTypeDetail(detail)) {
			return null;
		}

		String result = "";
		// #openpush#aid=12345
		if (2 == detail.split("=").length) {
			result = detail.split("=")[1];
		}

		return result;
	}

	/**
	 * @Title: isOpenTypeString
	 * @Description: 判断是否是打开文章的类型(page)
	 * @author liu_yi
	 * @param inputStr
	 * @return
	 * @throws
	 */
	public static boolean isPageTypeDetail(String inputStr) {
		Pattern pattern = Pattern.compile("id=[0-9_]+\\$.*");
		Matcher isPageType = pattern.matcher(inputStr);
		if (!isPageType.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * @Title: isOpenpushTypeDetail
	 * @Description: 判断是否是推送打开文章的类型(openpush)
	 * @author liu_yi
	 * @param inputStr
	 * @return
	 * @throws
	 */
	public static boolean isOpenpushTypeDetail(String inputStr) {
		Pattern pattern = Pattern.compile("aid=[0-9]+.*");
		Matcher isOpenType = pattern.matcher(inputStr);
		if (!isOpenType.matches()) {
			return false;
		}

		return true;
	}

	/**
	 * @Title: isInterestBookTypeDetail
	 * @Description: 判断是否是兴趣词/订阅操作类型(interest/book)
	 * @author liu_yi
	 * @return
	 * @throws
	 */
	public static boolean isBookTypeDetail(String inputStr) {
		Pattern pattern = Pattern.compile("add=.+");
		Matcher isBookType = pattern.matcher(inputStr);
		if (!isBookType.matches()) {
			return false;
		}

		return true;
	}
	
	public static void main(String[] args) {
		String test = "id=9118_1$type=article$ref=kw_考古";
		String test2 = "id=kw_我的头条$type=kw$ref=117372";
		System.out.println(isPageTypeDetail(test));
		System.out.println(isKwTypeDetail(test2));
		System.out.println(getOpenKeyword(test2));
		System.out.println(getOpenDocID(test));
	}
}
