package com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.ifeng.commen.Utils.LoadConfig;


/**
 * 
 * <PRE>
 * 作用 : 
 * 	   将各种节日包含国际节日，纪念日，中国节日，中国传统节日等不规范，不统一的日期进行规范统一化处理，统一转换成日历中的 year-month-day的形式
 *   
 * 使用 : 
 *   节日规范：调用festivalNormalization(int year, String festivalDay)
 *          返回阳历的节日格式：year-month-day
 * 示例 :
 *   推理型节日：感恩节	每年11月份第4个星期四
 *           复活节         每年春分月圆之后第一个星期日
 *           除夕             每年农历腊月（十二月）的最后一个晚上， 有时12-29，有时12-30
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
public class TraditionalFestivalDayStandard {

	/** standardDayMap 存放的是阳历格式的时间     <节日,时间>  */
	private HashMap<String, String> standardDayMap = new HashMap<String, String>();
	/** rangeDayMap 存放的是区间段的时间      <节日,时间区间>*/
	private HashMap<String, String> rangeDayMap = new HashMap<String, String>();
	/** inferDayMap 存放的是推理式的时间    <节日,推理式时间>*/
	private HashMap<String, String> inferDayMap = new HashMap<String, String>();

//	public String festivalDayPath = "C:/Users/lixin5/Desktop/节日对应/AllFestivalDay.txt";
	private static String TimeSensitiveConfigFile = LoadConfig.lookUpValueByKey("TimeSensitiveConfigFile");
	
	public HashMap<String, String> getStandardDayMap() {
		return standardDayMap;
	}


	public void setStandardDayMap(HashMap<String, String> standardDayMap) {
		this.standardDayMap = standardDayMap;
	}


	public HashMap<String, String> getRangeDayMap() {
		return rangeDayMap;
	}


	public void setRangeDayMap(HashMap<String, String> rangeDayMap) {
		this.rangeDayMap = rangeDayMap;
	}


	public HashMap<String, String> getInferDayMap() {
		return inferDayMap;
	}


	public void setInferDayMap(HashMap<String, String> inferDayMap) {
		this.inferDayMap = inferDayMap;
	}


	public TraditionalFestivalDayStandard(){
		init();
	}
	/**
	 * 
	* @Title: TraditionalFestivalDayStandard
	* @Description: 节日准确时间获取初始化
	* @param standardDayMap
	* @param rangeDayMap
	* @param inferDayMap
	* @author:wuyg1
	* @date:2016年10月13日
	 */
	public TraditionalFestivalDayStandard(HashMap<String, String> standardDayMap, 
			HashMap<String, String> rangeDayMap, HashMap<String, String> inferDayMap){
		this.setStandardDayMap(standardDayMap);
		this.setRangeDayMap(rangeDayMap);
		this.setInferDayMap(inferDayMap);
	}
	
	/**
	 * @Description: 加载配置文件
	 * @author:lixin5
	 * @version V1.0 
	 */
	public void init(){

		File filefestivalDay = new File(TimeSensitiveConfigFile);
	     try {
			     if(!filefestivalDay.exists()||filefestivalDay.isDirectory())
						throw new FileNotFoundException();
			     BufferedReader brOne=new BufferedReader(new FileReader(filefestivalDay));
			     String temp=null;
			     temp=brOne.readLine();
			     while(temp!=null){
			    	 if(temp.contains("inferDay:")){
			    		 String[] str = temp.replace("inferDay:", "").split("\t");
				    	 inferDayMap.put(str[0], str[1]);
			    		 
			    	 }else if (temp.contains("rangeDay:")) {
			    		 String[] str = temp.replace("rangeDay:", "").split("\t");
				    	 rangeDayMap.put(str[0], str[1]);
			    		 
					}else if (temp.contains("standardDay:")) {
						String[] str = temp.replace("standardDay:", "").split("\t");
						standardDayMap.put(str[0], str[1]);
						
					}
			    	 temp=brOne.readLine();
			    	 
			     }
			     brOne.close();
			     
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	/**
	 * @Description: 对各种节日进行判断并规范化处理
	 * @author:lixin5
	 * @param year
	 * @param holiday
	 * @return 规范化后的日历   year-month-day
	 */
	public String judge(int year, String holiday){
		
		if(standardDayMap.containsKey(holiday)){
			return year +"-"+ standardDayMap.get(holiday);
			
		}else if (rangeDayMap.containsKey(holiday)) {
			String[] str = rangeDayMap.get(holiday).split("#");
			return year +"-"+ str[0] + " __ "+ year +"-"+ str[1];
			
		}else if (inferDayMap.containsKey(holiday)) {
			
			// 对区间的时间进行划分
			if(inferDayMap.get(holiday).contains("#")){
				// 区间时间中的农历时间
				if(inferDayMap.get(holiday).contains("$") && !inferDayMap.get(holiday).contains("&")){
					String[] str = inferDayMap.get(holiday).replace("$", "").split("#");
					String[] str1 = str[0].split("-");
					String[] str2 = str[1].split("-");
					
					return ChineseCalendar.sCalendarLundarToSolar(year, Integer.parseInt(str1[0]), Integer.parseInt(str1[1])) +
							" __ "+ ChineseCalendar.sCalendarLundarToSolar(year, Integer.parseInt(str2[0]), Integer.parseInt(str2[1]));
					
					
				}else if (inferDayMap.get(holiday).contains("$") && inferDayMap.get(holiday).contains("&")) {
					String[] str = inferDayMap.get(holiday).replace("$", "").replace("&", "").split("#");
					String[] str1 = str[0].split("-");
					String[] str2 = str[1].split("-");
					
					return ChineseCalendar.sCalendarLundarToSolar(year-1, Integer.parseInt(str1[0]), Integer.parseInt(str1[1])) +
							" __ "+ ChineseCalendar.sCalendarLundarToSolar(year-1, Integer.parseInt(str2[0]), Integer.parseInt(str2[1]));
					
				}
					
					
			// 对非区间中的农历时间进行处理
			}else if (inferDayMap.get(holiday).contains("$")) {
				
				if(inferDayMap.get(holiday).contains("&")){
					String[] str = inferDayMap.get(holiday).replace("$", "").replace("&", "").split("-");
					return ChineseCalendar.sCalendarLundarToSolar(year-1, Integer.parseInt(str[0]), Integer.parseInt(str[1]));
					
				}else {
					String[] str = inferDayMap.get(holiday).replace("$", "").split("-");
					return ChineseCalendar.sCalendarLundarToSolar(year, Integer.parseInt(str[0]), Integer.parseInt(str[1]));
					
				}
				
			// 对复活节时间进行推理运算
			}else if (inferDayMap.get(holiday).equals("EasterDay")) {

				return InferCalendar.sCalendarLundarToSolarFromEasterDay(year);
				
			// 对除夕时间进行推理运算
			}else if(inferDayMap.get(holiday).equals("chuxi1-1")){
				
				String[] str = inferDayMap.get(holiday).replace("chuxi", "").split("-");
				return ChineseCalendar.sCalendarLundarToSolarFromChuXi(year, Integer.parseInt(str[0]), Integer.parseInt(str[1]));
				
				
			}else {
				
				String[] str = inferDayMap.get(holiday).split("-");
				
				// 该月的最后一天
				if(str[1].equals("@") && str[2].equals("@")){
				  return InferCalendar.lastDayOfMonth(year, str[0], str[1], str[2]);
					
					
				  // 该月的最后一个星期几
				}else if (str[1].equals("@") && !str[2].equals("@")) {
			        return InferCalendar.lastDayOfWeekOfMonth(year, str[0], str[1], str[2]); 
					
				}else {
					// 该月的某个星期几
					return InferCalendar.someDayOfWeekOfMonth(year, str[0], str[1], str[2]);
					
				}
			}
			
		}else {
			
		}
		
		return null;
		
	}
	
	
 
	/**
	 * @Description: 对各种节日进行判断并规范化处理的调用接口
	 * @author:lixin5
	 * @param year
	 * @param festivalDay
	 */
	public String festivalNormalization(int year, String festivalDay){
		return judge(year,festivalDay);
	}
	
}
