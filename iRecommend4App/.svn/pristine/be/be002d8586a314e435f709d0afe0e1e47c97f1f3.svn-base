package com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * <PRE>
 * 作用 : 
 *   对需要推断的时间进行相应的推断：
 *   （1）某月最后一天的阳历转换
 *   （2）某月最后一个星期几的阳历转换
 *   （3）某月某个星期几的阳历转换
 *   （4）复活节阳历转换
 *   
 *   
 * 使用 : 工具类使用
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016年8月25日        lixin5          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class InferCalendar {

	public static final SimpleDateFormat FMT_YMD = new SimpleDateFormat("yyyy-MM-dd"); 
	
	/**
	 * @Description 某月最后一天的阳历转换的算法
	 * @author:lixin5
	 * @param year
	 * @param month
	 * @param dayOfWeekInMonth
	 * @param dayOfWeek
	 * @return
	 */
	static String lastDayOfMonth(int year, String month, String dayOfWeekInMonth, String dayOfWeek){
		
		  Calendar cal = Calendar.getInstance();
		  cal.set(Calendar.YEAR,year);
		  cal.set(Calendar.MONTH, Integer.parseInt(month));
		  cal.set(Calendar.DAY_OF_MONTH, 1);
		  cal.add(Calendar.DAY_OF_MONTH, -1);
		  Date lastDate = cal.getTime();
		  String yymmdd = FMT_YMD.format(lastDate);
		  return yymmdd;
		
	}
	
	/**
	 * @Description 某月最后一个星期几的阳历转换的算法
	 * @author:lixin5
	 * @param year
	 * @param month
	 * @param dayOfWeekInMonth
	 * @param dayOfWeek
	 * @return
	 */
	static String lastDayOfWeekOfMonth(int year, String month, String dayOfWeekInMonth, String dayOfWeek){
		
		int b = 0;//
		if(dayOfWeek.equals("星期一")){
			b = 2;
			
		}else if (dayOfWeek.equals("星期二")) {
			b = 3;
			
		}else if (dayOfWeek.equals("星期三")) {
			b = 4;
			
		}else if (dayOfWeek.equals("星期四")) {
			b = 5;
			
		}else if (dayOfWeek.equals("星期五")) {
			b = 6;
			
		}else if (dayOfWeek.equals("星期六")) {
			b = 7;
			
		}else if (dayOfWeek.equals("星期日")) {
			b = 1;
			
		}
		
		Calendar instance = Calendar.getInstance();  // 获得当前时间
		instance.clear();
		instance.set(Calendar.YEAR, year);
		instance.set(Calendar.MONTH, Integer.parseInt(month)-1);
	    instance.add(Calendar.MONTH, 1);//月份+1  
	    instance.set(Calendar.DAY_OF_MONTH, 1);//
	    instance.add(Calendar.DAY_OF_MONTH, -1);//
	    int a  = instance.get(Calendar.DAY_OF_WEEK);
        instance.add(Calendar.DAY_OF_MONTH,  
                b - a > 0?-a-(7-b):b-a);//
        
        String date_str = formatDate(instance.getTime(), FMT_YMD); 
        return date_str; 
		
	}
	
	/**
	 * @Description 某月某个星期几的阳历转换的算法
	 * @author:lixin5
	 * @param year
	 * @param month
	 * @param dayOfWeekInMonth
	 * @param dayOfWeek
	 * @return
	 */
	static String someDayOfWeekOfMonth(int year, String month, String dayOfWeekInMonth, String dayOfWeek){
		
		int temp = 0;
		if(dayOfWeek.equals("星期一")){
			temp = 2;
			
		}else if (dayOfWeek.equals("星期二")) {
			temp = 3;
			
		}else if (dayOfWeek.equals("星期三")) {
			temp = 4;
			
		}else if (dayOfWeek.equals("星期四")) {
			temp = 5;
			
		}else if (dayOfWeek.equals("星期五")) {
			temp = 6;
			
		}else if (dayOfWeek.equals("星期六")) {
			temp = 7;
			
		}else if (dayOfWeek.equals("星期日")) {
			temp = 1;
			
		}
		
		try {
			
	        Calendar date = Calendar.getInstance();
	        date.clear();
	        date.set(Calendar.YEAR, year);
	        date.set(Calendar.MONTH, Integer.parseInt(month)-1);
	        date.set(Calendar.DAY_OF_WEEK_IN_MONTH, Integer.parseInt(dayOfWeekInMonth));
	        date.set(Calendar.DAY_OF_WEEK, temp);
	        String date_str = formatDate(date.getTime(), FMT_YMD);  
	        return date_str;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	 /**
	   * @Description 复活节阳历转换的算法
	   * @author lixin5
	   * @param year
	   * @return 复活节的阳历时间
	   */
	static String sCalendarLundarToSolarFromEasterDay(int year) {
		
		int month = 0;
		int N = year - 1900;
		int A = N % 19;
		int Q = N / 4;
		int B = (7 * A + 1) / 19;
		int M = (11 * A + 4 - B) % 29;
		int W = (N + Q + 31 - M) % 7;
		int day = 25 - M - W;
		if (day > 0) {
			month = 4;

		} else if (day < 0) {

			month = 3;
			day = 31 + day;

		} else {
			month = 3;
			day = 31;
		}
		return year + "-" + month + "-" + day;

	}
	
	
	static String formatDate(Date date, SimpleDateFormat sdf)  
    {  
        return sdf.format(date);  
    }
	
	
	static Date parseDate(String strDate, String pattern)  
    {  
        SimpleDateFormat df = new SimpleDateFormat(pattern);  
        try  
        {  
            return df.parse(strDate);  
        } catch (ParseException e)  
        {  
            e.printStackTrace();  
            return null;  
        }  
    } 
	
}
