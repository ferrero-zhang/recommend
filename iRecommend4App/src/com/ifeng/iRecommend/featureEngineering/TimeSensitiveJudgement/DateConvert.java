package com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateConvert {

	public static String convertCnAccurateDate(String cprq, String pub_time,
			SimpleDateFormat ymd_sdf, SimpleDateFormat ym_sdf,
			SimpleDateFormat md_sdf) {

		String year = null;
		String month = null;
		String day = null;

		if (cprq.contains("年")
				&& !(cprq.contains("月") || cprq.contains("日") || cprq
						.contains("号"))) {
			return cprq;
		}

		Calendar calendar = Calendar.getInstance();

		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		Date date = null;

		try {
			date = ymd_sdf.parse(pub_time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		calendar.setTime(date);

		if (cprq.contains("年")) {
			int yearPos = cprq.indexOf("年");

			if (yearPos == 0) {
				return cprq;
			}
			year = cprq.substring(0, yearPos);

			year = DateConvert.ConvertCnNumberChar(year);

			if (!CalendarParser.isNumber(year)) {
				return cprq;
			}
		}

		if (cprq.contains("月")) {
			int monthPos = cprq.indexOf("月");
			if (cprq.contains("年")) {
				int yearPos = cprq.indexOf("年");
				if (yearPos + 1 == monthPos) {
					return cprq;
				}
				month = cprq.substring(yearPos + 1, monthPos);
			} else {
				if (monthPos == 0) {
					return cprq;
				}
				month = cprq.substring(0, monthPos);
			}

			month = DateConvert.ConvertCnNumberChar(month);

			if (!CalendarParser.isNumber(month)) {
				return cprq;
			}

		}

		String dayStr = cprq;

		if (dayStr.contains("月")) {
			dayStr = dayStr.substring(dayStr.indexOf("月") + 1);
			dayStr = dayStr.trim();
		}
		if (null != dayStr && !dayStr.isEmpty()) {

			if (cprq.contains("日") || cprq.contains("号")) {
				int end = dayStr.indexOf("日") > dayStr.indexOf("号") ? dayStr
						.indexOf("日") : dayStr.indexOf("号");
				day = dayStr.substring(0, end);
			} else {
				if (dayStr.length() > 3 || dayStr.contains("年")
						|| dayStr.contains("-")) {
					day = null;
				} else {
					day = dayStr;
				}
			}

			day = DateConvert.ConvertCnNumberChar(day);

			if (!CalendarParser.isNumber(day)) {
				return cprq;
			}
		}

		Calendar c = Calendar.getInstance();

		if (null != year && !year.isEmpty()) {
			year = ConvertCnYear(year);
			c.set(Calendar.YEAR, Integer.parseInt(year));
		}
		if (null != month && !month.isEmpty() && !month.equals("0")) {
			month = ConvertCnDateNumber(month);
			c.set(Calendar.MONTH, Integer.parseInt(month) - 1);
		}
		if (null != day && !day.isEmpty()) {
			day = ConvertCnDateNumber(day);
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
		}

		if (cprq.contains("年") && cprq.contains("月")
				&& (cprq.contains("日") || cprq.contains("号"))) {
			return CalendarParser.getYearFromDate(c.getTime()) + "年"
					+ CalendarParser.getMonthFromDate(c.getTime()) + "月"
					+ CalendarParser.getDayFromDate(c.getTime()) + "日";
		} else if (cprq.contains("月")
				&& (cprq.contains("日") || cprq.contains("号") || null != day)) {
			return CalendarParser.getYearFromDate(calendar.getTime()) + "年"
					+ CalendarParser.getMonthFromDate(c.getTime()) + "月"
					+ CalendarParser.getDayFromDate(c.getTime()) + "日";
		} else if (cprq.contains("年") && cprq.contains("月")) {
			return CalendarParser.getYearFromDate(c.getTime()) + "年"
					+ CalendarParser.getMonthFromDate(c.getTime()) + "月";
		} else {
			String string = new String();
			if (null != day && !day.isEmpty()) {
				string = CalendarParser.getYearFromDate(calendar.getTime())+"年"+CalendarParser.getMonthFromDate(calendar.getTime())+"月"+day + "日";
			}else if (null != month && !month.isEmpty()) {
				string = CalendarParser.getYearFromDate(calendar.getTime())+"年"+month +"月";
			}else if(null != year && !year.isEmpty()){
				string = year + "年";
			}
			
			return string;
		}
	}
	
	
	public static String convertCnDate(String cprq, String pub_time,
			SimpleDateFormat ymd_sdf, SimpleDateFormat ym_sdf,
			SimpleDateFormat md_sdf) {

		String year = null;
		String month = null;
		String day = null;

		if (cprq.contains("年")
				&& !(cprq.contains("月") || cprq.contains("日") || cprq
						.contains("号"))) {
			return cprq;
		}

		Calendar calendar = Calendar.getInstance();

		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		Date date = null;

		try {
			date = ymd_sdf.parse(pub_time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		calendar.setTime(date);

		if (cprq.contains("年")) {
			int yearPos = cprq.indexOf("年");

			if (yearPos == 0) {
				return cprq;
			}
			year = cprq.substring(0, yearPos);

			year = DateConvert.ConvertCnNumberChar(year);

			if (!CalendarParser.isNumber(year)) {
				return cprq;
			}
		}

		if (cprq.contains("月")) {
			int monthPos = cprq.indexOf("月");
			if (cprq.contains("年")) {
				int yearPos = cprq.indexOf("年");
				if (yearPos + 1 == monthPos) {
					return cprq;
				}
				month = cprq.substring(yearPos + 1, monthPos);
			} else {
				if (monthPos == 0) {
					return cprq;
				}
				month = cprq.substring(0, monthPos);
			}

			month = DateConvert.ConvertCnNumberChar(month);

			if (!CalendarParser.isNumber(month)) {
				return cprq;
			}

		}

		String dayStr = cprq;

		if (dayStr.contains("月")) {
			dayStr = dayStr.substring(dayStr.indexOf("月") + 1);
			dayStr = dayStr.trim();
		}
		if (null != dayStr && !dayStr.isEmpty()) {

			if (cprq.contains("日") || cprq.contains("号")) {
				int end = dayStr.indexOf("日") > dayStr.indexOf("号") ? dayStr
						.indexOf("日") : dayStr.indexOf("号");
				day = dayStr.substring(0, end);
			} else {
				if (dayStr.length() > 3 || dayStr.contains("年")
						|| dayStr.contains("-")) {
					day = null;
				} else {
					day = dayStr;
				}
			}

			day = DateConvert.ConvertCnNumberChar(day);

			if (!CalendarParser.isNumber(day)) {
				return cprq;
			}
		}

		Calendar c = Calendar.getInstance();

		if (null != year && !year.isEmpty()) {
			year = ConvertCnYear(year);
			c.set(Calendar.YEAR, Integer.parseInt(year));
		}
		if (null != month && !month.isEmpty() && !month.equals("0")) {
			month = ConvertCnDateNumber(month);
			c.set(Calendar.MONTH, Integer.parseInt(month) - 1);
		}
		if (null != day && !day.isEmpty()) {
			day = ConvertCnDateNumber(day);
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
		}

		if (cprq.contains("年") && cprq.contains("月")
				&& (cprq.contains("日") || cprq.contains("号"))) {
			return CalendarParser.getYearFromDate(c.getTime()) + "年"
					+ CalendarParser.getMonthFromDate(c.getTime()) + "月"
					+ CalendarParser.getDayFromDate(c.getTime()) + "日";
		} else if (cprq.contains("月")
				&& (cprq.contains("日") || cprq.contains("号") || null != day)) {
			return CalendarParser.getYearFromDate(calendar.getTime()) + "年"
					+ CalendarParser.getMonthFromDate(c.getTime()) + "月"
					+ CalendarParser.getDayFromDate(c.getTime()) + "日";
		} else if (cprq.contains("年") && cprq.contains("月")) {
			return CalendarParser.getYearFromDate(c.getTime()) + "年"
					+ CalendarParser.getMonthFromDate(c.getTime()) + "月";
		} else {
			StringBuffer sbBuffer = new StringBuffer();
			if (null != year && !year.isEmpty()) {
				sbBuffer = sbBuffer.append(year + "年");
			}
			if (null != month && !month.isEmpty()) {
				sbBuffer = sbBuffer.append(month + "月");
			}
			if (null != day && !day.isEmpty()) {
				sbBuffer = sbBuffer.append(day + "日");
			}
			return sbBuffer.toString();
		}
	}

	private static String ConvertCnYear(String cnYear) {
		if (cnYear.length() == 2)
			return "20" + ConvertCnNumberChar(cnYear);
		else
			return ConvertCnNumberChar(cnYear);
	}

	private static String ConvertCnDateNumber(String cnNumber) {
		if (cnNumber.length() == 1) {
			if (cnNumber.equals("十"))
				return "10";
			else
				return ConvertCnNumberChar(cnNumber);
		} else if (cnNumber.length() == 2) {
			if (cnNumber.startsWith("十")) {
				return "1" + ConvertCnNumberChar(cnNumber.substring(1, 2));
			} else if (cnNumber.endsWith("十")) {
				return ConvertCnNumberChar(cnNumber.substring(0, 1)) + "0";
			} else {
				return ConvertCnNumberChar(cnNumber);
			}
		} else if (cnNumber.length() == 3) {
			return ConvertCnNumberChar(cnNumber.substring(0, 1)
					+ cnNumber.substring(2, 3));
		}
		return null;
	}

	public static String ConvertCnNumberChar(String cnNumberStr) {

		if (null == cnNumberStr || cnNumberStr.isEmpty()) {
			return cnNumberStr;
		}

		String ALL_CN_NUMBER = "○０零１一２二３三４四５五６六７七８八９九";
		String ALL_NUMBER = "000112233445566778899";
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < cnNumberStr.length(); i++) {
			char c = cnNumberStr.charAt(i);
			int index = ALL_CN_NUMBER.indexOf(c);
			if (index != -1) {
				buf.append(ALL_NUMBER.charAt(index));
			} else {
				buf.append(cnNumberStr.charAt(i));
			}
		}

		String result = buf.toString();

		if (result.contains("十")) {

			int end = buf.length() - 1;
			int index = result.indexOf("十");
			if (index == end) {
				result = result.replace("十", "0");
			} else if (index == 0) {
				result = result.replace("十", "1");
			} else {
				result = result.replace("十", "");
			}
		}

		return result;
	}

	/**
	 * 
	 * @Title:monthTag2DateStr
	 * @Description: 对月份的时间戳进行规范化
	 * @param timeTag
	 * @param dateStr
	 * @param sdf
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月28日
	 */
	public static String monthTag2DateStr(String timeTag, String dateStr,
			SimpleDateFormat sdf) {

		if (null == timeTag || timeTag.isEmpty()) {
			return timeTag;
		}

		String result = null;

		if (timeTag.equals("本月")) {
			result = lastDayOfthisMonth(0, dateStr, sdf);
		} else if (timeTag.equals("下月")) {
			result = firstDayOfthisMonth(1, dateStr, sdf);
		} else if (timeTag.equals("上月")) {

			result = lastDayOfthisMonth(-1, dateStr, sdf);
		}
		return result;
	}

	/**
	 * 
	 * @Title:getWeekOfDate
	 * @Description: 根据日期获得对应的周几
	 * @param dateStr
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月31日
	 */
	public static String getWeekOfDate(String dateStr) {

		if (null == dateStr || dateStr.isEmpty()) {
			return dateStr;
		}

		String[] weekDaysName = { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };

		Calendar calendar = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");

		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}

		calendar.setTime(date);

		int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

		return weekDaysName[intWeek];
	}

	/**
	 * 
	 * @Title:firstDayOfthisMonth
	 * @Description: 获取dateStr所在月份的第一天
	 * @param month_dis
	 * @param dateStr
	 * @param sdf
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月28日
	 */
	private static String firstDayOfthisMonth(int month_dis, String dateStr,
			SimpleDateFormat sdf) {
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Calendar calendar = Calendar.getInstance();

		calendar.setTime(date);

		calendar.add(Calendar.MONTH, month_dis);

		calendar.set(Calendar.DAY_OF_MONTH,
				calendar.getActualMinimum(Calendar.DAY_OF_MONTH));

		return CalendarParser.getYearFromDate(calendar.getTime()) + "-"
				+ CalendarParser.getMonthFromDate(calendar.getTime()) + "-"
				+ CalendarParser.getDayFromDate(calendar.getTime());
	}

	/**
	 * 
	 * @Title:lastDayOfthisMonth
	 * @Description: 获取dateStr所在月份的最后一天
	 * @param month_dis
	 * @param dateStr
	 * @param sdf
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月28日
	 */
	private static String lastDayOfthisMonth(int month_dis, String dateStr,
			SimpleDateFormat sdf) {

		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Calendar calendar = Calendar.getInstance();

		calendar.setTime(date);

		calendar.add(Calendar.MONTH, month_dis);

		calendar.set(Calendar.DAY_OF_MONTH,
				calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

		return CalendarParser.getYearFromDate(calendar.getTime()) + "-"
				+ CalendarParser.getMonthFromDate(calendar.getTime()) + "-"
				+ CalendarParser.getDayFromDate(calendar.getTime());
	}

	
	/**
	 * 
	* @Title:getDvalue
	* @Description: 获取两个时间对象的小时差值
	* @param calendar 
	* 				文章发布的时间
	* @param calendar2
	* 				段落内的时间
	* @return
	* @author:wuyg1
	* @date:2016年11月1日
	 */
	public static int getDvalue(Calendar calendar, Calendar calendar2){
		int dvalue = calendar2.get(Calendar.HOUR_OF_DAY) - calendar.get(Calendar.HOUR_OF_DAY)-1;

		return dvalue;
	}
	
	/**
	 * 
	 * @Title:isSameDay
	 * @Description: 判定指定段落的时间戳是否有与发布时间是同一天的，如果有，则返回true，否则返回false；
	 * @param pubDateCalendar
	 * @param sdf
	 * @param dateSet
	 * @return
	 * @author:wuyg1
	 * @date:2016年11月1日
	 */
	public static boolean isSameDay(Calendar pubDateCalendar,
			SimpleDateFormat sdf, HashSet<String> dateSet) {

		if (null == dateSet || dateSet.isEmpty()) {
			return true;
		}

		for (String dateStr : dateSet) {
			Date date = null;
			try {
				dateStr = dateStr.replaceAll("日|号", "");
				dateStr = dateStr.replaceAll("年|月", "-");
				date = sdf.parse(dateStr);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				continue;
			}
			Calendar calendar = Calendar.getInstance();

			calendar.setTime(date);

			boolean isSameYear = pubDateCalendar.get(Calendar.YEAR) == calendar
					.get(Calendar.YEAR);

			if (isSameYear) {
				boolean isSameMonth = pubDateCalendar.get(Calendar.MONTH) == calendar
						.get(Calendar.MONTH);

				if (isSameMonth) {

					boolean isSameDay = pubDateCalendar
							.get(Calendar.DAY_OF_MONTH) == calendar
							.get(Calendar.DAY_OF_MONTH);

					if (isSameDay) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @Title:getClockOftimeTag
	 * @Description: 返回时分之类的时间标签对应的Calendar对象
	 * @param dateStr
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月31日
	 */
	public static Calendar getClockOftimeTag(String dateStr) {
		Pattern p = Pattern
				.compile(
						"(今天下午|今日下午|今天晚上|今晚|)?((\\d{1,2}|[一二三四五六七八九十]{1,3})([点|时|:|：])((\\d{1,2}|[一二三四五六七八九十]{1,3})(分)?)?)?",
						Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

		Matcher matcher = p.matcher(dateStr);

		Calendar calendar = Calendar.getInstance();

		while (matcher.find()) {
			String time = matcher.group();
			if (null == time || time.isEmpty()) {
				continue;
			}

			//(今天下午|今日下午|今天晚上|今晚|)
		    String tag = null;
		    boolean reviseFlag = false;
			if(time.contains("今天下午")){
				tag = "今天下午";
				reviseFlag = true;
			}else if(time.contains("今日下午")){
				tag = "今日下午";
				reviseFlag = true;
			}else if(time.contains("今天晚上")){
				tag = "今天晚上";
				reviseFlag = true;
			}else if(time.contains("今晚")){
                tag = "今晚";			
                reviseFlag = true;
			}
			if(reviseFlag){
				time = time.substring(time.indexOf(tag)+tag.length());
			}
			
			
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

			if(time.endsWith("时") || time.endsWith("点")){
				time = time + "00";
			}
			
			time = time.replaceAll("点|时|分|：", ":");

			if (time.endsWith(":")) {
				time = time.substring(0, time.lastIndexOf(":"));
			}

			Date date = null;
			try {
				date = sdf.parse(time);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				return null;
			}

			calendar.setTime(date);
			
			if(reviseFlag){
				calendar.add(Calendar.HOUR_OF_DAY, 12);
			}
		}
		return calendar;
	}

}
