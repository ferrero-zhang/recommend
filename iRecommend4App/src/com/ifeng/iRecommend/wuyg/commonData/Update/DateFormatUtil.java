package com.ifeng.iRecommend.wuyg.commonData.Update;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {
	/**
	 * 
	* @Title:longToString
	* @Description:long类型转换为String类型
	* @param currentTime 要转换的long类型的时间
	* @param formatType  要转换的string类型的时间格式
	* @return
	* @throws ParseException
	* @author:wuyg1
	* @date:2015年12月28日
	 */
	public static String longToString(long currentTime, String formatType)
			throws ParseException {
		Date date = longToDate(currentTime, formatType); // long类型转成Date类型
		String strTime = dateToString(date, formatType); // date类型转成String
		return strTime;
	}

	/**
	 * 
	* @Title:stringToDate
	* @Description:string类型转换为date类型
	* @param strTime 要转换的string类型的时间
	* @param formatType  要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒，
	* @return
	* @throws ParseException
	* @author:wuyg1
	* @date:2015年12月28日
	 */
	public static Date stringToDate(String strTime, String formatType)
			throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(formatType);
		Date date = null;
		date = formatter.parse(strTime);
		return date;
	}
	/**
	 * 
	* @Title:longToDate
	* @Description:long转换为Date类型 
	* @param currentTime  要转换的long类型的时间
	* @param formatType   要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
	* @return
	* @throws ParseException
	* @author:wuyg1
	* @date:2015年12月28日
	 */
	public static Date longToDate(long currentTime, String formatType)
			throws ParseException {
		Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
		String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
		Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
		return date;
	}
	/**
	 * 
	* @Title:dateToString
	* @Description: date类型转换为String类型
	* @param data
	* @param formatType 格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
	* @return
	* @author:wuyg1
	* @date:2015年12月28日
	 */
	public static String dateToString(Date data, String formatType) {
		return new SimpleDateFormat(formatType).format(data);
	}
}
