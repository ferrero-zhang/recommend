package com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.org.apache.bcel.internal.generic.StackInstruction;
/**
 * 
 * <PRE>
 * 作用 : 
 *      负责时间处理，可以获取到年，月，日，本周的第几天，本月的第几周  
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
 *          1.0          2016年8月26日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class CalendarParser {
	
	private Calendar nowTime;
	
	/**
	 * 
	 * @Title:initDateTime
	 * @Description:系统时间初始化
	 * @author:wuyg1
	 * @date:2016年8月25日
	 */
	private void initDateTime() {
		nowTime = Calendar.getInstance();
	}
	/**
	 * 
	* @Title:getYear
	* @Description:获取当前是哪年
	* @return
	* @author:wuyg1
	* @date:2016年8月26日
	 */
	public int getYear(){
		initDateTime();
		return nowTime.get(Calendar.YEAR);
	}
	/**
	 * 
	* @Title:getMonth
	* @Description:获取当前是哪月
	* @return
	* @author:wuyg1
	* @date:2016年8月26日
	 */
	public int getMonth(){
		initDateTime();
		int monthCal = nowTime.get(Calendar.MONTH);
		monthCal = monthCal + 1;
		return monthCal;
	}
	/**
	 * 
	* @Title:getLastMonth
	* @Description: 获取上个月的月份
	* @return
	* @author:wuyg1
	* @date:2016年10月19日
	 */
	public int getLastMonth(){
		initDateTime();
		nowTime.setTime(getDate());
		nowTime.add(Calendar.MONTH, -1);
		return (nowTime.get(Calendar.MONTH) + 1);
	}
	/**
	 * 
	* @Title:getNextMonth
	* @Description: 获取下个月的月份
	* @return
	* @author:wuyg1
	* @date:2016年10月19日
	 */
	public int getNextMonth(){
		initDateTime();
		nowTime.setTime(getDate());
		nowTime.add(Calendar.MONTH, 1);
		return (nowTime.get(Calendar.MONTH)+1);
	}	
	
	/**
	 * 
	* @Title:getDay
	* @Description: 获取当天是哪天
	* @return
	* @author:wuyg1
	* @date:2016年8月26日
	 */
	public int getDay(){
		initDateTime();
		int dayCal = nowTime.get(Calendar.DAY_OF_MONTH);
		return dayCal;
	}
	/**
	 * 
	* @Title:getDateOfWeekCal
	* @Description:本周的第几天
	* @return
	* @author:wuyg1
	* @date:2016年8月26日
	 */
	public int getDateOfWeekCal(){
		initDateTime();
		int dateOfWeekCal = nowTime.get(Calendar.DAY_OF_WEEK);
		if (dateOfWeekCal == 0) {
			dateOfWeekCal = 7;
		} else {
			dateOfWeekCal = dateOfWeekCal - 1;
		}
		return dateOfWeekCal;
	}
	/**
	 * 
	* @Title:getDateOfWeekOfMonthCal
	* @Description:本月的第几周
	* @return
	* @author:wuyg1
	* @date:2016年8月26日
	 */
	public int getDateOfWeekOfMonthCal(){
		initDateTime();
		return nowTime.get(Calendar.DAY_OF_WEEK_IN_MONTH);
	}
	/**
	 * 
	* @Title:getThisWeekend
	* @Description:获取当前周的周末
	* @return
	* @author:wuyg1
	* @date:2016年10月17日
	 */
	public String getThisWeekend(){
	   return getSomeDay(0);  
	}
	/**
	 * 
	* @Title:getNextWeekend
	* @Description:获取下一周的周末
	* @return
	* @author:wuyg1
	* @date:2016年10月17日
	 */
	public String getNextWeekend(){
		return getSomeDay(1);
	}
	
	/**
	 * 
	* @Title:getLastWeekend
	* @Description:获取上周的周末
	* @return
	* @author:wuyg1
	* @date:2016年10月17日
	 */
	public String getLastWeekend(){
		return getSomeDay(-1);
	}
	/**
	 * 
	* @Title:getLastDay
	* @Description: 获取昨天的时间
	* @return
	* @author:wuyg1
	* @date:2016年10月17日
	 */
	public String getLastDay(){
		  initDateTime();
		  nowTime.add(Calendar.DATE,-1);
		  String yesterday = new SimpleDateFormat( "yyyy-MM-dd ").format(nowTime.getTime());
		  return yesterday;
	}
	
	public static String getCertainDate(String dateStr, int distanceDay, SimpleDateFormat sdf){

		Date date = null;
		
		 try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 Calendar calendar = Calendar.getInstance();
		 
		 calendar.setTime(date);
		 
		 calendar.add(Calendar.DATE, distanceDay);
		 
		 String date_certain = CalendarParser.getYearFromDate(calendar.getTime())
		 +"-"+CalendarParser.getMonthFromDate(calendar.getTime())
		 +"-"+CalendarParser.getDayFromDate(calendar.getTime())
		 +" 23:59:00";

		 return date_certain;
	}
	/**
	 * 
	* @Title:getCertainClock
	* @Description: 得到有效的时分区间
	* @param dateStr
	* @param distanceHour
	* @param sdf
	* @return
	* @author:wuyg1
	* @date:2016年11月1日
	 */
	public static String getCertainClock(String dateStr, int distanceHour, SimpleDateFormat sdf){
		Date date = null;
		
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTime(date);
		
		if(((calendar.get(Calendar.HOUR_OF_DAY)+distanceHour) >= 24) || ((calendar.get(Calendar.HOUR_OF_DAY)+distanceHour) < 0) ){
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 0);
		}else{
			
			if(distanceHour == 0){
				//如果和发布时间相差一个小时，那么有效时间就设置为发布时间+1个小时，然后分钟设置为0
				calendar.add(Calendar.HOUR_OF_DAY, 1);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
			}else{
				calendar.add(Calendar.HOUR_OF_DAY, distanceHour);
			}
			
			
		}
		
		int minute = calendar.get(Calendar.MINUTE);
		
		String minuteStr = null;
		
		if(minute < 10){
			minuteStr = "0"+minute;
		}else{
			minuteStr = ""+minute;
		}
		
		return CalendarParser.getYearFromDate(calendar.getTime())
				 +"-"+CalendarParser.getMonthFromDate(calendar.getTime())
				 +"-"+CalendarParser.getDayFromDate(calendar.getTime())
				 +" "+calendar.get(Calendar.HOUR_OF_DAY)
				 +":"+minuteStr
		         +":"+"00";
	}
	
	/**
	 * 
	* @Title:getNextDay
	* @Description: 获取明天的时间
	* @return
	* @author:wuyg1
	* @date:2016年10月17日
	 */
	public String getNextDay(){
		  initDateTime();
		  nowTime.add(Calendar.DATE,1);
		  String tomorrow = new SimpleDateFormat( "yyyy-MM-dd ").format(nowTime.getTime());
		  return tomorrow;
	}
	
	private String getSomeDay(int point){
		   initDateTime();
		   nowTime.setFirstDayOfWeek(Calendar.MONDAY);
		   nowTime.add(Calendar.WEEK_OF_YEAR, point);
		   nowTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		   return nowTime.get(Calendar.YEAR)+"-"+(nowTime.get(Calendar.MONTH)+1)+"-"+nowTime.get(Calendar.DAY_OF_MONTH);  
		
	}
	/**
	 * 
	* @Title:getCertainQuarter
	* @Description: 获取当前季节的截止日
	* @param dateStr
	* @param sdf
	* @return
	* @author:wuyg1
	* @date:2016年10月20日
	 */
	public static String getCertainQuarter(String dateStr, SimpleDateFormat sdf){
		
		Date date = null;
		
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int curMonth = CalendarParser.getMonthFromDate(date);
		
		int month_quater = 0;
		
		String date_Quarter = null;
		
		if(curMonth >=3 && curMonth <=5){
			month_quater = 5;
			
			 date_Quarter = CalendarParser.getYearFromDate(date)
					+"-"+month_quater
					+"-"+"31"+ " 00:00:00";
			
		}else if(curMonth >=6 && curMonth <= 8){
			month_quater = 8;
			
			 date_Quarter = CalendarParser.getYearFromDate(date)
					+"-"+month_quater
					+"-"+"31"+ " 00:00:00";
			
		}else if(curMonth >=9 && curMonth <= 11){
			month_quater = 11;
			
			 date_Quarter = CalendarParser.getYearFromDate(date)
					+"-"+month_quater
					+"-"+"30"+ " 00:00:00";
			
		}else {
			if(curMonth ==12){
				month_quater = 2;
			    date_Quarter = (CalendarParser.getYearFromDate(date)+1)
						+"-"+month_quater
						+"-"+"28"+ " 00:00:00";
			}else if(curMonth == 1 || curMonth == 2){
				month_quater = 2;
			    date_Quarter = CalendarParser.getYearFromDate(date)
						+"-"+month_quater
						+"-"+"28"+ " 00:00:00";
			}
		}
		return date_Quarter;	
	}
	
	/**
	 * 
	* @Title:getCertainWeek
	* @Description: 获取到指定的周末
	* @param dateStr
	* @param distanceWeek
	* @param sdf
	* @return
	* @author:wuyg1
	* @date:2016年10月19日
	 */
	public static String getCertainWeek(String dateStr, int distanceWeek, SimpleDateFormat sdf){
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.add(Calendar.WEEK_OF_YEAR, distanceWeek);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		String date_certain = CalendarParser.getYearFromDate(calendar.getTime())
				+"-"+CalendarParser.getMonthFromDate(calendar.getTime())
				+"-"+CalendarParser.getDayFromDate(calendar.getTime())
				+" "+"23:59:00";
		return date_certain;
	}
	
	/**
	 * 
	* @Title:getDate
	* @Description: 获取Fri Aug 26 13:02:46 CST 2016 格式
	* @return
	* @author:wuyg1
	* @date:2016年8月26日
	 */
	public Date getDate(){
		initDateTime();
		return nowTime.getTime();
	}
	/**
	 * 
	* @Title:getDateStr
	* @Description: 获取时间的字符串格式 yyyy-MM-dd
	* @return
	* @author:wuyg1
	* @date:2016年10月17日
	 */
	public String getDateStr(){
		initDateTime();
		return new SimpleDateFormat("yyyy-MM-dd").format(nowTime.getTime());
	}
	/**
	 * 
	* @Title:getAccurateDateStr
	* @Description: 获取到精准时间戳 yyyy-MM-dd HH:mm:ss
	* @return
	* @author:wuyg1
	* @date:2016年11月2日
	 */
	public String getAccurateDateStr(){
		initDateTime();
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowTime.getTime());
	}
	
	/**
	 * 
	* @Title:getYearFromDate
	* @Description: 从 Date中获取到相应的年份
	* @param date
	* @return
	* @author:wuyg1
	* @date:2016年10月18日
	 */
	public static int getYearFromDate(Date date){
		return (date.getYear()+1900);
	}
	/**
	 * 
	* @Title:getMonthFromDate
	* @Description:从Date中获取相应的月份
	* @param date
	* @return
	* @author:wuyg1
	* @date:2016年10月18日
	 */
	public static int getMonthFromDate(Date date){
		return (date.getMonth()+1);
	}
	/**
	 * 
	* @Title:getDayFromDate
	* @Description: 从Date中获取相应的天数
	* @param date
	* @return
	* @author:wuyg1
	* @date:2016年10月18日
	 */
	public static int getDayFromDate(Date date){
		return (date.getDate());
	}
	/**
	 * 
	* @Title:getHourFromDate
	* @Description: 从Date中获取相应的小时
	* @param date
	* @return
	* @author:wuyg1
	* @date:2016年10月18日
	 */
	public static int getHourFromDate(Date date){
		return date.getHours();
	}
	/**
	 * 
	* @Title:getMinuteFromDate
	* @Description: 从Date中获取相应的分钟
	* @param date
	* @return
	* @author:wuyg1
	* @date:2016年10月18日
	 */
	public static int getMinuteFromDate(Date date){
		return date.getMinutes();
	}
	/**
	 * 
	* @Title:getSecondFromDate
	* @Description: 从Date中获取相应的秒数
	* @param date
	* @return
	* @author:wuyg1
	* @date:2016年10月18日
	 */
	public static int getSecondFromDate(Date date){
		return date.getSeconds();
	}
	/**
	 * 
	* @Title:daysBetween
	* @Description: 计算 (date-date2)的时间差，返回的天数
	* @param date
	* @param date2
	* @return
	* @author:wuyg1
	* @date:2016年10月18日
	 */
	public static int daysBetween(Date date, Date date2){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			date = sdf.parse(sdf.format(date));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			date2 = sdf.parse(sdf.format(date2));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		long time = calendar.getTimeInMillis();
		
		calendar.setTime(date2);
		
		long time2 = calendar.getTimeInMillis();
		
		long Dvalue = (time - time2)/(1000 * 60 * 60 * 24);
		return Integer.parseInt(String.valueOf(Dvalue));
	}
	
	/**
	 * 
	* @Title:isTheSameDay
	* @Description: 对比两个日期是否为同一天
	* @param date
	* @param date2
	* @return
	* @author:wuyg1
	* @date:2016年10月18日
	 */
	public static boolean isTheSameDay(Date date, Date date2){
		boolean isSameDayFlag = false;
		if((getYearFromDate(date) == getYearFromDate(date2)) 
				&& (getMonthFromDate(date) == getMonthFromDate(date2))
				&& (getDayFromDate(date) == getDayFromDate(date2))){
			isSameDayFlag = true;
		}
		return isSameDayFlag;
	}
	/**
	 * 
	* @Title:isNumber
	* @Description: 判定字符串是否为数字
	* @param str
	* @return
	* @author:wuyg1
	* @date:2016年10月20日
	 */
	public static boolean isNumber(String str){
		//return str.matches("[\\d]+[.]?[\\d]+");
		
		if(null == str || str.isEmpty()){
			return false;
		}
		
		Pattern pattern = Pattern.compile("[0-9]*"); 
		   Matcher isNum = pattern.matcher(str);
		   if( !isNum.matches() ){
		       return false; 
		   } 
		   return true; 
		
	}
	
	public static void main(String [] args){
           CalendarParser calendarParser = new CalendarParser();
           
           System.err.println(calendarParser.getSomeDay(0));
	}
	
}
