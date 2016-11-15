package com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.awt.SunHints.Value;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;

/**
 * 
 * <PRE>
 * 作用 : 
 *     文章时效性判定  
 * 使用 : 
 *     输入：
 *     <p> string split_title;  切词后的tile
 *    		<p>string split_content; 切词后的content
 *   		<p>List text_class_list; 文章分类
 *   		<p>
 *     输出：   private boolean isTimeSensitive;  // 是否具备时效性
 *     		<p>private string reasonDescribe; // 原因描述(时间要素、节日识别、事件识别等类型，用于快速debug追踪)
 *          <p>private string others; // 备用字段，后续可能加入事件时间
 * 
 * 示例 :
 *     输入： split_title: 看到_v 这_r 3种_mq 迹象_n ，_w 预示_v 你_r 要_v 转_v 大运_n ，_w 发大财_v 	
 *         split_content: 分词后的文章
 *         text_class_list:[星座]
 *     输出： isTimeSensitive: false
 *          resonDescribe:  文章里面没有任何时间词，而且没有星座领域的时效性描述词汇（如：本周运势等）
 *          others:
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
public class TimeSensitiveJudgement {

	
	private static Log LOG = LogFactory.getLog(TimeSensitiveJudgement.class);

	static Pattern dateStampPattern = Pattern
			.compile(
					"(前([\\d]|[\uFF10-\uFF19]|[两一二三四五六七八九十])+[轮])|(第([\\d]|[\uFF10-\uFF19]|[一二三四五六七八九十])+届)|((\\d{1,4}|[\uFF10-\uFF19]{1,4})[-|\\/|年|\\.](\\d{1,2}|[\uFF10-\uFF19]{1,2})[-|\\/|月|\\.](\\d{1,2}|[\uFF10-\uFF19]{1,2})(日|号)?(\\s)*((\\d{1,2}|[\uFF10-\uFF19]{1,2})(点|时)?((:)?(\\d{1,2}|[\uFF10-\uFF19]{1,2})(分)?((:)?(\\d{1,2}|[\uFF10-\uFF19]{1,2})(秒)?)?)?)?(\\s)*(PM|AM)?)|(\\d{1,4}|[\uFF10-\uFF19]{1,4})[年](\\d{1,2}|[\uFF10-\uFF19]{1,2})[月]|((\\d{1,2}|[一二三四五六七八九十]{1,3})(点|时)(\\d{1,2}|[一二三四五六七八九十]{1,3})[分]((\\d{1,2}|[一二三四五六七八九十]{1,3})([秒])?)?)(\\s)*(PM|AM)?|\\d{4}[年]|(\\d{1,2}|一|二|三|四|五|六|七|八|九|十|十一|十二|腊|正|下|上|本)[月](初|份|末|底)*((\\d{1,2}|([一二三四五六七八九十]{1,3}))(日|号)?)?|(\\d{1,2}|[\uFF10-\uFF19]{1,2})[月]((\\d{1,2}|[\uFF10-\uFF19]{1,2})[日])?|[\uFF10-\uFF19]{1,2}[日]|(\\d{4}|[\uFF10-\uFF19]{4})[年](\\d{1,2}|[\uFF10-\uFF19]{1,2})[月]((\\d{1,2}|[\uFF10-\uFF19]{1,2})[日])?|((\\d{1,2}|([一二三四五六七八九十]{1,3}))([日]))|月[初|末]|(周|星期|礼拜)[一二三四五六七日末天]|[本|上|下]周[一|二|三|四|五|六|日|天]?|初[一|二|三|四|五|六|七|八|九|十]",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	static Pattern ExactDateStampPattern = Pattern
			.compile(
					"(第([\\d]|[\uFF10-\uFF19]|[一二三四五六七八九十])+届)|((\\d{1,4}|[\uFF10-\uFF19]{1,4})[-|\\/|年|\\.](\\d{1,2}|[\uFF10-\uFF19]{1,2})[-|\\/|月|\\.](\\d{1,2}|[\uFF10-\uFF19]{1,2})(日|号)?(\\s)*((\\d{1,2}|[\uFF10-\uFF19]{1,2})([点|时])?((:)?(\\d{1,2}|[\uFF10-\uFF19]{1,2})(分)?((:)?(\\d{1,2}|[\uFF10-\uFF19]{1,2})(秒)?)?)?)?(\\s)*(PM|AM)?)|\\d{1,4}[年]\\d{1,2}[月]|(\\d{1,2}|一|二|三|四|五|六|七|八|九|十|十一|十二|腊|正|下|上|本)[月](初|份|末|底)*((\\d{1,2}|([一二三四五六七八九十]{1,3}))(日|号)?)?|(\\d{1,2}|[\uFF10-\uFF19]{1,2})[月]((\\d{1,2}|[\uFF10-\uFF19]{1,2})[日])?|[\uFF10-\uFF19]{4}[年][\uFF10-\uFF19]{1,2}[月]([\uFF10-\uFF19]{1,2}[日])?|[本|上|下]周[一|二|三|四|五|六|日|天]?",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	static Pattern reportTimePattern = Pattern
			.compile("(\\d{1,2}|[\uFF10-\uFF19]{1,2})[-|\\/|月](\\d{1,2}|[\uFF10-\uFF19]{1,2})(日|号)(讯|电|报道)");

	static Pattern timeIndicatePattern = Pattern
				.compile("(前([\\d]|[\uFF10-\uFF19]|[两一二三四五六七八九十])+[轮])|(第([\\d]|[\uFF10-\uFF19]|[一二三四五六七八九十])+[期|批])|[\u4e00-\u9fa5]+节|(\\d{1,4}[-|.]\\d{1,2}[-|.]\\d{1,2})|(\\d{1,2}[-|.]\\d{1,2})|(第([\\d]|[\uFF10-\uFF19]|[一二三四五六七八九十])+届)|((\\d{1,4}|[\uFF10-\uFF19]{1,4})[-|\\/|年|\\.](\\d{1,2}|[\uFF10-\uFF19]{1,2})[-|\\/|月|\\.](\\d{1,2}|[\uFF10-\uFF19]{1,2})(日|号)?(\\s)*((\\d{1,2}|[\uFF10-\uFF19]{1,2})(点|时)?((:)?(\\d{1,2}|[\uFF10-\uFF19]{1,2})(分)?((:)?(\\d{1,2}|[\uFF10-\uFF19]{1,2})(秒)?)?)?)?(\\s)*(PM|AM)?)|(\\d{1,4}|[\uFF10-\uFF19]{1,4})[年](\\d{1,2}|[\uFF10-\uFF19]{1,2})[月]|(\\d{1,2}|一|二|三|四|五|六|七|八|九|十|十一|十二|腊|正|下|上|本)[月](初|份|末|底)*((\\d{1,2}|([一二三四五六七八九十]{1,3}))(日|号)?)?|(\\d{1,2}|[\uFF10-\uFF19]{1,2})[月]((\\d{1,2}|[\uFF10-\uFF19]{1,2})[日])?|[\uFF10-\uFF19]{4}[年][\uFF10-\uFF19]{1,2}[月]([\uFF10-\uFF19]{1,2}[日])?|[本|上|下]周[一|二|三|四|五|六|日|天]?|月[初|末]|(周|星期|礼拜)[一二三四五六七日末天]|((\\d|([一二三四五六七八九十]))(周年|季度))");

	static Pattern timeIndicateYearPattern = Pattern
			.compile("(\\d{4}|[\uFF10-\uFF19]{4})");
	
	static 	Pattern timeStartStampPattern = Pattern
			.compile(
					"(周|星期|礼拜)[一二三四五六七日末天]|[本|上|下]周[一|二|三|四|五|六|日|天]?",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	/**
	 * 时分的正则表达式
	 */
	static Pattern clockPattern = Pattern.compile("(今天下午|今日下午|今天晚上|今晚|)?(\\d{1,2}|[一二三四五六七八九十]{1,3})([点|时|:|：])((\\d{1,2}|[一二三四五六七八九十]{1,3})(分)?)?", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);

	static Pattern festivalStampPattern = null;

	static Pattern tPattern = Pattern.compile("[^\\s]+?_t ");
	// 时间戳词存储
	private HashSet<String> timeWordSet = new HashSet<String>();
	// 常见的时间指示性词语，如： 发布 出台
	private HashSet<String> timeIndicateWordSet = new HashSet<String>();
	// 中性词（即如果只有这些词，而没有其他的精确时间做支撑，则认为是没有时效性的）
	private HashSet<String> neutralWordSet = new HashSet<String>();
	// 无效词的删除
	private HashSet<String> invalidWordSet = new HashSet<String>();

	// 领域敏感性级别存储
	private HashMap<String, Integer> categorySensitiveMap = new HashMap<String, Integer>();

	private String TimeSensitiveConfigFile = LoadConfig
			.lookUpValueByKey("TimeSensitiveConfigFile");
	// 该集合中的时间戳对于时效性LEVEL设置为当天具有重要作用
	private HashSet<String> dateOfTodaySet = new HashSet<String>();
	// 该集合中的时间戳对于时效性LEVEL设置为本周具有重要作用
	private HashSet<String> thisWeekTagSet = new HashSet<String>();
	//季节对应的时间区间 
	private HashMap<String, SeasonDate> seasonDateMap = new HashMap<String, SeasonDate>();
	
	private FileUtil fileUtil = new FileUtil();

	private CalendarParser calendarParser;

	private static TimeSensitiveJudgement INSTANCE = null;
	
	
	// 节日处理的相关参数

	private TraditionalFestivalDayStandard traditionalFestivalDayStandard = null;

	/** standardDayMap 存放的是阳历格式的时间 <节日,时间> */
	private HashMap<String, String> standardDayMap = new HashMap<String, String>();
	/** rangeDayMap 存放的是区间段的时间 <节日,时间区间> */
	private HashMap<String, String> rangeDayMap = new HashMap<String, String>();
	/** inferDayMap 存放的是推理式的时间 <节日,推理式时间> */
	private HashMap<String, String> inferDayMap = new HashMap<String, String>();

	/**
	 * 
	 * @Title:getInstance
	 * @Description:实例获取
	 * @return
	 * @author:wuyg1
	 * @date:2016年8月31日
	 */
	public synchronized static TimeSensitiveJudgement getInstance() {

		if (null == TimeSensitiveJudgement.INSTANCE) {
			TimeSensitiveJudgement.INSTANCE = new TimeSensitiveJudgement();
		}
		return TimeSensitiveJudgement.INSTANCE;
	}

	/**
	 * 
	 * @Title: TimeSensitiveJudgement
	 * @Description:构造方法
	 * @author:wuyg1
	 * @date:2016年8月31日
	 */
	private TimeSensitiveJudgement() {
		calendarParser = new CalendarParser();
		loadTimeSensitiveConfigFile();
		traditionalFestivalDayStandard = new TraditionalFestivalDayStandard(
				standardDayMap, rangeDayMap, inferDayMap);
	}

	/**
	 * 
	 * @Title:loadTimeSensitiveConfigFile
	 * @Description:加载时效性配置文件
	 * @author:wuyg1
	 * @date:2016年8月29日
	 */
	private void loadTimeSensitiveConfigFile() {
		String configCon = fileUtil.Read(TimeSensitiveConfigFile, "utf-8");

		HashMap<String, HashSet<String>> timeSensitiveConfigMap = new HashMap<String, HashSet<String>>();

		for (String str : configCon.split("\n")) {

			if (null == str || str.isEmpty()
			// || !str.contains("#DIV#")
			) {
				continue;
			}

			if (!str.contains("#DIV#")) {
				
				if(str.contains("seasonTime")){
					String word = str.substring(str.indexOf("#WORD#")+"#WORD#".length(), str.indexOf("#FROM#"));
					String fromDateStr = str.substring(str.indexOf("#FROM#")+"#FROM#".length(), str.indexOf("#TO#"));
					String toDateStr = str.substring(str.indexOf("#TO#")+"#TO#".length());
					int beginDate = Integer.valueOf(fromDateStr);
					int endDate = Integer.valueOf(toDateStr);
					
					SeasonDate seasonDate = new SeasonDate();
					
					seasonDate.setBeginDate(beginDate);
					seasonDate.setEndDate(endDate);
					
					seasonDateMap.put(word, seasonDate);
					
				}
				
				if (str.contains("inferDay:")) {
					String[] inferDays = str.replace("inferDay:", "").split(
							"\t");
					inferDayMap.put(inferDays[0], inferDays[1]);

				} else if (str.contains("rangeDay:")) {
					String[] rangeDays = str.replace("rangeDay:", "").split(
							"\t");
					rangeDayMap.put(rangeDays[0], rangeDays[1]);

				} else if (str.contains("standardDay:")) {
					String[] standardDays = str.replace("standardDay:", "")
							.split("\t");
					standardDayMap.put(standardDays[0], standardDays[1]);

				}
				continue;
			}

			String key = str.substring(0, str.indexOf("#DIV#"));
			String value = str.substring(str.indexOf("#DIV#")
					+ "#DIV#".length());
			if (timeSensitiveConfigMap.containsKey(key)) {
				timeSensitiveConfigMap.get(key).add(value);
			} else {
				HashSet<String> set = new HashSet<String>();
				set.add(value);
				timeSensitiveConfigMap.put(key, set);
			}
		}

		if (timeSensitiveConfigMap.containsKey("timeWord")) {
			timeWordSet.addAll(timeSensitiveConfigMap.get("timeWord"));
		}

		if (timeSensitiveConfigMap.containsKey("todayWord")) {
			dateOfTodaySet.addAll(timeSensitiveConfigMap.get("todayWord"));
		}

		if (timeSensitiveConfigMap.containsKey("thisWeek")) {
			thisWeekTagSet.addAll(timeSensitiveConfigMap.get("thisWeek"));
		}
		
		if (timeSensitiveConfigMap.containsKey("InvalidWord")) {
			invalidWordSet.addAll(timeSensitiveConfigMap.get("InvalidWord"));
		}

		if (timeSensitiveConfigMap.containsKey("festivalWord")) {
			StringBuffer sBuffer = new StringBuffer();

			for (String festivalWord : timeSensitiveConfigMap
					.get("festivalWord")) {
				sBuffer.append("|" + festivalWord);
			}

			festivalStampPattern = Pattern.compile("(" + sBuffer.substring(1)
					+ ")");
		}
		if (timeSensitiveConfigMap.containsKey("timeIndicateWord")) {
			timeIndicateWordSet.addAll(timeSensitiveConfigMap
					.get("timeIndicateWord"));
		}
		if (timeSensitiveConfigMap.containsKey("categorySensitive")) {
			for (String str : timeSensitiveConfigMap.get("categorySensitive")) {
				if (null == str || str.isEmpty() || str.length() == 0) {
					continue;
				}
				String word = str.split("#DIV#")[0];
				Integer level = Integer.valueOf(str.split("#DIV#")[1]);
				categorySensitiveMap.put(word, level);
			}
		}
		if (timeSensitiveConfigMap.containsKey("neutralWord")) {
			neutralWordSet.addAll(timeSensitiveConfigMap.get("neutralWord"));
		}
	}

	/**
	 * 
	 * @Title:EstimateTimeSensitiveOfArticle
	 * @Description: 文章时效性判定，第二版 增加了文章的预设有效时间
	 * @param pub_time
	 *            文章发布时间
	 * @param split_title
	 *            分词的文章标题
	 * @param split_content
	 *            分词的文章内容
	 * @param text_class_list
	 *            文章所属的类别列表
	 * @return  如果是非时效性的，isTimeSensitive字段为false;在other字段中设置为sensitiveLevel=NT；
	 *          <br>如果是时效性的，isTimeSensitive字段为true;在other字段中设置sensitiveLevel=yyyy-MM-dd HH:mm:ss
	 * @author:wuyg1
	 * @date:2016年10月17日
	 */
	public TimeSensitiveInfo EstimateTimeSensitiveOfArticle(String pub_time,
			String split_title, String split_content,
			List<String> text_class_list) {

		if (null == split_title && null == split_content) {
			TimeSensitiveInfo timeSensitiveInfo = new TimeSensitiveInfo();
			timeSensitiveInfo.setTimeSensitive(false);
			timeSensitiveInfo.setOthers("sensitiveLevel="+TimeSensitiveLevel.nt+"|!|");
			timeSensitiveInfo
					.setReasonDescribe("split_title and split_content is null!");
			return timeSensitiveInfo;
		}

		if (null == split_title) {
			split_content = "";
		}

		if (null == split_content) {
			split_content = "";
		}
		
		if(null == pub_time || pub_time.isEmpty()){
			//如果没有发布时间，则设置为当前时间
			pub_time = calendarParser.getAccurateDateStr();
		}

		ArticleTimeInfo articleTimeInfo = new ArticleTimeInfo();

		HashSet<String> categorySet = new HashSet<String>();
		if (null != text_class_list) {
			categorySet.addAll(text_class_list);
		}

		String regex = "_[A-Za-z]+ | ";

		int index = 0;

		articleTimeInfo.setPublishedTime(pub_time);

		articleTimeInfo.setCategorySet(categorySet);

		ArrayList<String> segmentList = new ArrayList<String>();

		segmentList.add(split_title);

		segmentList.addAll(Arrays.asList(split_content.split("\n")));

		StringBuffer articleBuffer = new StringBuffer();

		articleBuffer.append(split_title.replaceAll(regex, "") + "\t");

		articleBuffer.append(split_content.replaceAll(regex, ""));

		Matcher reportMatcher = reportTimePattern.matcher(articleBuffer
				.toString());

		while (reportMatcher.find()) {
			String reportTime = reportMatcher.group();
			if(reportTime.contains("|")){
				continue;
			}
			// LOG.info("电讯时间：" + reportTime);
			articleTimeInfo.getReportTimeSet().add(reportTime);
		}

		for (String segmentCon : segmentList) {

			String originalStr = segmentCon.replaceAll(regex, "");

			if (null == originalStr || originalStr.isEmpty()
					|| originalStr.equals("")) {
				continue;
			}

			originalStr = originalStr.replaceAll(" ", "");

			if (originalStr.isEmpty() || originalStr.equals("")
					|| originalStr.equals("_w_w")) {
				continue;
			}

			if (originalStr.startsWith("http://")
					|| originalStr.startsWith("https://")
					|| originalStr.startsWith("ftp://")) {
				continue;
			}

			for (String invalidWord : invalidWordSet) {
				originalStr = originalStr.replaceAll(invalidWord, "");
			}

			if (originalStr.length() < 30 && index >= 1) {
				index = index - 1;
			}

			Matcher dateStampMatcher = dateStampPattern.matcher(originalStr);

			while (dateStampMatcher.find()) {
				String stamp = dateStampMatcher.group();
				
				if(stamp.contains("|")){
					continue;
				}

				if (articleTimeInfo.getDateStampMap().containsKey(index)) {
					articleTimeInfo.getDateStampMap().get(index).add(stamp);
				} else {
					HashSet<String> stampSet = new HashSet<String>();
					stampSet.add(stamp);
					articleTimeInfo.getDateStampMap().put(index, stampSet);
				}
			}
			
			
			//抽取到时分的时间戳
			
			Matcher clockMatcher = clockPattern.matcher(originalStr);
			
			while(clockMatcher.find()){
				String stamp = clockMatcher.group();
				if(stamp.contains("|")){
					continue;
				}
				if(articleTimeInfo.getClockMap().containsKey(index)){
					articleTimeInfo.getClockMap().get(index).add(stamp);
				}else{
					HashSet<String> stampSet = new HashSet<String>();
					stampSet.add(stamp);
					articleTimeInfo.getClockMap().put(index, stampSet);
				}
			}
			
			

			Matcher festivalMatcher = festivalStampPattern.matcher(originalStr);

			while (festivalMatcher.find()) {
				String stamp = festivalMatcher.group();
                if(stamp.contains("|")){
                	continue;
                }
				if (articleTimeInfo.getFestivalStampMap().containsKey(index)) {
					articleTimeInfo.getFestivalStampMap().get(index).add(stamp);
				} else {
					HashSet<String> stampSet = new HashSet<String>();
					stampSet.add(stamp);
					articleTimeInfo.getFestivalStampMap().put(index, stampSet);
				}
			}

			if (index == 0) {
				for (String word : timeIndicateWordSet) {
					if (originalStr.contains(word)) {
						articleTimeInfo.getTimeIndicateWordSet().add(word);
					}
				}
				Matcher timeIndicateMatcher = timeIndicatePattern
						.matcher(originalStr);
				while (timeIndicateMatcher.find()) {

					String stamp = timeIndicateMatcher.group();
					if(stamp.contains("|")){
						continue;
					}
					articleTimeInfo.getTimeIndicateWordSet().add(stamp);
				}

				Matcher timeIndicateYearMatcher = timeIndicateYearPattern
						.matcher(originalStr);
				while (timeIndicateYearMatcher.find()) {
					int year = Integer.valueOf(timeIndicateYearMatcher.group());
					if (year >= getCalendarParser().getYear()
							&& year <= (getCalendarParser().getYear() + 5)) {
						articleTimeInfo.getTimeIndicateWordSet().add(
								Integer.toString(year));
					}
				}
			}

			for (String word : timeWordSet) {
				if (originalStr.contains(word)) {
					if (articleTimeInfo.getTimeStartWordMap()
							.containsKey(index)) {
						articleTimeInfo.getTimeStartWordMap().get(index)
								.add(word);
					} else if (!articleTimeInfo.getTimeStartWordMap()
							.containsKey(index)) {
						HashSet<String> set = new HashSet<String>();
						set.add(word);
						articleTimeInfo.getTimeStartWordMap().put(index, set);
					}
				}
			}
			
			Matcher timeWordMatcher = timeStartStampPattern.matcher(originalStr);
			while(timeWordMatcher.find()){
				String word = timeWordMatcher.group();
				if(word.contains("|")){
					continue;
				}
				if(articleTimeInfo.getTimeStartWordMap().containsKey(index)){
					articleTimeInfo.getTimeStartWordMap().get(index).add(word);
				}else if(!articleTimeInfo.getTimeStartWordMap().containsKey(index)){
					HashSet<String> set = new HashSet<String>();
					set.add(word);
					articleTimeInfo.getTimeStartWordMap().put(index, set);
				}
			}
			

			Matcher tMatcher = tPattern.matcher(segmentCon);
			while (tMatcher.find()) {
				String tempWord = tMatcher.group();
				if(tempWord.contains("|")){
					continue;
				}
				tempWord = tempWord.substring(0, tempWord.lastIndexOf("_t "));
				if (timeWordSet.contains(tempWord)) {
					if (articleTimeInfo.getTimestampMap().containsKey(index)) {
						articleTimeInfo.getTimestampMap().get(index)
								.add(tempWord);
					} else {
						HashSet<String> stampSet = new HashSet<String>();
						stampSet.add(tempWord);
						articleTimeInfo.getTimestampMap().put(index, stampSet);
					}
				}
			}
			index++;
		}
		
		TimeTagConvert(articleTimeInfo);
		
		TimeSensitiveInfo timeSensitiveInfo = articleTimeInfoParser(articleTimeInfo);
		
		getTimesensitiveLevel(timeSensitiveInfo, articleTimeInfo);
		
		getTimeSensitiveInOneDay(timeSensitiveInfo, articleTimeInfo);
		
		return getDeadLineOfTimeSensitive(timeSensitiveInfo, articleTimeInfo);
	}
	/**
	 * 
	* @Title:getDeadLineOfTimeSensitive
	* @Description: 获取到最终的文章有效截止时间
	* @param timeSensitiveInfo
	* @param articleTimeInfo
	* @return  如果是非时效性的，则在other字段中设置为sensitiveLevel=NT；如果是时效性的，则在other字段中设置sensitiveLevel=yyyy-MM-dd HH:mm:ss
	* @author:wuyg1
	* @date:2016年11月1日
	 */
	private TimeSensitiveInfo getDeadLineOfTimeSensitive(TimeSensitiveInfo timeSensitiveInfo, 
			ArticleTimeInfo articleTimeInfo){
	
		if(!timeSensitiveInfo.isTimeSensitive()){
			timeSensitiveInfo.setOthers("sensitiveLevel="+TimeSensitiveLevel.nt+"|!|");
			return timeSensitiveInfo;
		}
		
		TimeSensitiveLevel timeSensitiveLevel = articleTimeInfo.getTimeSensitiveLevel();
		int validDate = articleTimeInfo.getTimeValidDate();
		int validHour = articleTimeInfo.getTimeValidHour();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		String article_deadLine = null;
		
		if(null != timeSensitiveLevel && (TimeSensitiveLevel.UNVALID != timeSensitiveLevel)){
			if(timeSensitiveLevel == TimeSensitiveLevel.td){
				timeSensitiveInfo.setTimeTag("当天");
				article_deadLine = CalendarParser.getCertainDate(articleTimeInfo.getPublishedTime(), 0, sdf);
			}else if(timeSensitiveLevel == TimeSensitiveLevel.tw){
				timeSensitiveInfo.setTimeTag("本周");
				article_deadLine = CalendarParser.getCertainWeek(articleTimeInfo.getPublishedTime(), 0, sdf);
			}else if(timeSensitiveLevel == TimeSensitiveLevel.tq){
				timeSensitiveInfo.setTimeTag("本季度");
				article_deadLine = CalendarParser.getCertainQuarter(articleTimeInfo.getPublishedTime(), sdf);
			}
		}
		if(-1 != validDate){
			timeSensitiveInfo.setTimeTag(validDate+"天");
			article_deadLine = CalendarParser.getCertainDate(articleTimeInfo.getPublishedTime(), validDate, sdf);
		}
		if(-1 != validHour){
			timeSensitiveInfo.setTimeTag(validHour+"小时");
			article_deadLine = CalendarParser.getCertainClock(articleTimeInfo.getPublishedTime(), validHour, sdf);
		}
		
		timeSensitiveInfo.setOthers("sensitiveLevel="+article_deadLine+"|!|");
		
		return timeSensitiveInfo;
	}
	
	
	/**
	 * 
	* @Title:getTimeSensitiveInOneDay
	* @Description: 针对时效性为当天的文章，进一步的挖掘其在一天内的小时时效性，做到更加精确的时效性区间把控
	* @param timeSensitiveInfo
	* @param articleTimeInfo
	* @return
	* @author:wuyg1
	* @date:2016年10月31日
	 */
	private void getTimeSensitiveInOneDay(TimeSensitiveInfo timeSensitiveInfo, ArticleTimeInfo articleTimeInfo){
		
		if (!timeSensitiveInfo.isTimeSensitive() ) {
			// 如果是非时效性的文章直接返回;
			timeSensitiveInfo.setOthers("sensitiveLevel="+TimeSensitiveLevel.nt+"|!|");
			return;
		}
		
		//获取到文章的时效性级别
		int timeValidDate = articleTimeInfo.getTimeValidDate();
		TimeSensitiveLevel timeSensitiveLevel = articleTimeInfo.getTimeSensitiveLevel();
        if(timeValidDate == 0 || timeSensitiveLevel == TimeSensitiveLevel.td){
			
			//处理时效性为当天的文章，时效性为当天的文章，需要判定时效性是否可能缩短至几个小时，如果没有可能，则仍旧设置为当天24点失效，否则设置为合理的小时有效区间
			
			HashMap<Integer, HashSet<String>> clockMap = articleTimeInfo.getClockMap();
			
			Iterator<Integer> iterator = clockMap.keySet().iterator();
			
			String pub_time = articleTimeInfo.getPublishedTime();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			Date pub_datetime = null;
			
			Calendar calendar_pubtime = Calendar.getInstance();
			try {
				pub_datetime = sdf.parse(pub_time);
				if(null == pub_datetime){
					//如果文章没有发布时间，则返回，不再进行处理
					return;
				}
				calendar_pubtime.setTime(pub_datetime);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			HashSet<Integer> dvalueSet = new HashSet<Integer>();
			
			while(iterator.hasNext()){
				int segmentKey = iterator.next();
				
				if(segmentKey > 4){
					continue;
				}
				
				SimpleDateFormat ymd_format = new SimpleDateFormat("yyyy-MM-dd");
				
				boolean isSameDayFlag = DateConvert.isSameDay(calendar_pubtime, ymd_format, articleTimeInfo.getDateStampMap().get(segmentKey));
				
				if(isSameDayFlag){
					//如果本段中存在和发布时间是同一天的时间戳，则计算时分
					
					HashSet<String> clockSet = clockMap.get(segmentKey);
					
					for(String timeTag : clockSet){
						Calendar calendar = DateConvert.getClockOftimeTag(timeTag);
						
						if(null == calendar){
							continue;
						}
						
						int dvalue = DateConvert.getDvalue(calendar_pubtime, calendar);
						
						dvalueSet.add(dvalue);
					}	
				}
			}
			
			//对获取到的时间差进行排序
			if(null != dvalueSet && dvalueSet.size() > 0){
				ArrayList<Integer> arrayList = new ArrayList<Integer>(dvalueSet);
				
				Collections.sort(arrayList);
				//差值是否为负值，默认是全部为负值
				boolean isNegativeDvalue = true;
				
				int value = Integer.MIN_VALUE;
				
				for(int index = 0; index < arrayList.size(); index++){
					int dvalue = arrayList.get(index);
					
					if(dvalue >= 0){
						//获取到差值大于0的第一个差值
						value = dvalue;
						isNegativeDvalue = false;
						break;
					}
				}
				
				if(isNegativeDvalue){
					if(articleTimeInfo.getCategorySet().contains("体育")){
						value = 1;
					}else if(articleTimeInfo.getCategorySet().contains("天气")){
						value = 1;
					}
				}
				
				if(value != Integer.MIN_VALUE){
					articleTimeInfo.setTimeValidHour(value);
				}
			}
		}
		
		return ;
	}

	/**
	 * 
	 * @Title:EstimateTimeSensitiveOfArticle
	 * @Description:时效性判定
	 * @param split_title
	 *            分词的文章标题
	 * @param split_content
	 *            分词的文章内容
	 * @param text_class_list
	 *            文章所属的类别列表
	 * @return {@TimeSensitiveInfo}
	 * @author:wuyg1
	 * @date:2016年8月26日
	 */
	public TimeSensitiveInfo EstimateTimeSensitiveOfArticle(String split_title,
			String split_content, List<String> text_class_list) {

		if (null == split_title && null == split_content) {
			TimeSensitiveInfo timeSensitiveInfo = new TimeSensitiveInfo();
			timeSensitiveInfo.setTimeSensitive(false);
			timeSensitiveInfo
					.setReasonDescribe("split_title and split_content is null!");
			return timeSensitiveInfo;
		}

		if (null == split_title) {
			split_content = "";
		}

		if (null == split_content) {
			split_content = "";
		}

		ArticleTimeInfo articleTimeInfo = new ArticleTimeInfo();

		HashSet<String> categorySet = new HashSet<String>();
		if (null != text_class_list) {
			categorySet.addAll(text_class_list);
		}

		String regex = "_[A-Za-z]+ | ";

		int index = 0;

		articleTimeInfo.setCategorySet(categorySet);

		ArrayList<String> segmentList = new ArrayList<String>();

		segmentList.add(split_title);

		segmentList.addAll(Arrays.asList(split_content.split("\n")));

		StringBuffer articleBuffer = new StringBuffer();

		articleBuffer.append(split_title.replaceAll(regex, "") + "\t");

		articleBuffer.append(split_content.replaceAll(regex, ""));

		Matcher reportMatcher = reportTimePattern.matcher(articleBuffer
				.toString());

		while (reportMatcher.find()) {
			String reportTime = reportMatcher.group();
			// LOG.info("电讯时间：" + reportTime);
			articleTimeInfo.getReportTimeSet().add(reportTime);
		}

		for (String segmentCon : segmentList) {

			String originalStr = segmentCon.replaceAll(regex, "");

			if (null == originalStr || originalStr.isEmpty()
					|| originalStr.equals("")) {
				continue;
			}

			originalStr = originalStr.replaceAll(" ", "");

			if (originalStr.isEmpty() || originalStr.equals("")
					|| originalStr.equals("_w_w")) {
				continue;
			}

			if (originalStr.startsWith("http://")
					|| originalStr.startsWith("https://")
					|| originalStr.startsWith("ftp://")) {
				continue;
			}

			for (String invalidWord : invalidWordSet) {
				originalStr = originalStr.replaceAll(invalidWord, "");
			}

			if (originalStr.length() < 30 && index >= 1) {
				index = index - 1;
			}

			// LOG.info("第" + index + "段：");

			Matcher dateStampMatcher = dateStampPattern.matcher(originalStr);

			while (dateStampMatcher.find()) {
				String stamp = dateStampMatcher.group();
				// LOG.info("时间戳的精确匹配：" + stamp);
				// dateStampSet.add(stamp);

				if (articleTimeInfo.getDateStampMap().containsKey(index)) {
					articleTimeInfo.getDateStampMap().get(index).add(stamp);
				} else {
					HashSet<String> stampSet = new HashSet<String>();
					stampSet.add(stamp);
					articleTimeInfo.getDateStampMap().put(index, stampSet);
				}
			}

			Matcher festivalMatcher = festivalStampPattern.matcher(originalStr);

			while (festivalMatcher.find()) {
				String stamp = festivalMatcher.group();
				// LOG.info("节气(节日)：" + stamp);
				// festivalStampSet.add(stamp);

				if (articleTimeInfo.getFestivalStampMap().containsKey(index)) {
					articleTimeInfo.getFestivalStampMap().get(index).add(stamp);
				} else {
					HashSet<String> stampSet = new HashSet<String>();
					stampSet.add(stamp);
					articleTimeInfo.getFestivalStampMap().put(index, stampSet);
				}
			}

			if (index == 0) {
				for (String word : timeIndicateWordSet) {
					if (originalStr.contains(word)) {
						articleTimeInfo.getTimeIndicateWordSet().add(word);
					}
				}
				Matcher timeIndicateMatcher = timeIndicatePattern
						.matcher(originalStr);
				while (timeIndicateMatcher.find()) {
					articleTimeInfo.getTimeIndicateWordSet().add(
							timeIndicateMatcher.group());
				}

				Matcher timeIndicateYearMatcher = timeIndicateYearPattern
						.matcher(originalStr);
				while (timeIndicateYearMatcher.find()) {
					int year = Integer.valueOf(timeIndicateYearMatcher.group());
					if (year >= getCalendarParser().getYear()
							&& year <= (getCalendarParser().getYear() + 5)) {
						articleTimeInfo.getTimeIndicateWordSet().add(
								Integer.toString(year));
					}
				}
			}

			for (String word : timeWordSet) {
				if (originalStr.contains(word)) {
					if (articleTimeInfo.getTimeStartWordMap()
							.containsKey(index)) {
						articleTimeInfo.getTimeStartWordMap().get(index)
								.add(word);
					} else if (!articleTimeInfo.getTimeStartWordMap()
							.containsKey(index)) {
						HashSet<String> set = new HashSet<String>();
						set.add(word);
						articleTimeInfo.getTimeStartWordMap().put(index, set);
					}
				}
			}

			Matcher tMatcher = tPattern.matcher(segmentCon);
			while (tMatcher.find()) {
				String tempWord = tMatcher.group();
				tempWord = tempWord.substring(0, tempWord.lastIndexOf("_t "));
				if (timeWordSet.contains(tempWord)) {
					// LOG.info("t_的时间匹配：" + tempWord);
					if (articleTimeInfo.getTimestampMap().containsKey(index)) {
						articleTimeInfo.getTimestampMap().get(index)
								.add(tempWord);
					} else {
						HashSet<String> stampSet = new HashSet<String>();
						stampSet.add(tempWord);
						articleTimeInfo.getTimestampMap().put(index, stampSet);
					}
				}

			}
			index++;
		}
		TimeSensitiveInfo timeSensitiveInfo = articleTimeInfoParser(articleTimeInfo);
		return getTimesensitiveLevel(timeSensitiveInfo, articleTimeInfo);

	}

	/**
	 * 
	 * @Title:getTimesensitiveLevel
	 * @Description:对时效性的文章进行时效级别的分析，如果是非时效性文章则返回；不予处理
	 * @param timeSensitiveInfo
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月13日
	 */
	private TimeSensitiveInfo getTimesensitiveLevel(
			TimeSensitiveInfo timeSensitiveInfo, ArticleTimeInfo articleTimeInfo) {

		if (!timeSensitiveInfo.isTimeSensitive()) {
			// 如果是非时效性的文章直接返回;
			return timeSensitiveInfo;
		}
		
		//发布时间：2016-10-17 10:54:00
		SimpleDateFormat sdf_pub = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date_pub = null;
		try {
			date_pub = sdf_pub.parse(articleTimeInfo.getPublishedTime());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		// 处理每一篇文章之前，把文章各个段落的时刻表清空
		HashMap<Integer, HashSet<String>> timeClockMap = new HashMap<Integer, HashSet<String>>();

		
		
		// 如果是时效性的文章，则需要对文章的时效进行分档
		ArrayList<Date> dateList_sorted = new ArrayList<Date>();
		ArrayList<Date> dateList_temp_sorted = new ArrayList<Date>();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
				Locale.CHINESE);

		HashMap<Integer, HashSet<String>> dateStampMap = articleTimeInfo
				.getDateStampMap();
		HashMap<Integer, HashSet<Date>> formatDateMap = dateStamp2RegularDateFormat(
				sdf, dateStampMap, timeClockMap, date_pub);
		HashMap<Date, HashSet<Integer>> dateMap_inverted = invertedDateIndex(
				formatDateMap, dateList_temp_sorted);
		dateList_sorted.addAll(dateList_temp_sorted);

		dateList_temp_sorted.clear();
		HashMap<Integer, HashSet<String>> festivalStampMap = articleTimeInfo
				.getFestivalStampMap();
		formatDateMap = festivalStamp2RegularDateFormat(sdf, festivalStampMap);
		dateMap_inverted = invertedDateIndex(formatDateMap,
				dateList_temp_sorted);
		if(null != dateMap_inverted){
			dateList_sorted.addAll(dateList_temp_sorted);
		}

		dateList_temp_sorted.clear();
		HashSet<String> reportTimeSet = articleTimeInfo.getReportTimeSet();
		dateList_temp_sorted = reportStamp2RegularDateFormat(sdf, reportTimeSet, date_pub);
		if(null != dateList_temp_sorted){
			dateList_sorted.addAll(dateList_temp_sorted);
		}
	
		dateList_temp_sorted.clear();
		HashSet<String> indicateWordSet = articleTimeInfo
				.getTimeIndicateWordSet();
		HashSet<Date> indicatedDateSet = IndicatedTime2FormatDate(sdf,
				indicateWordSet);
		if(null != indicatedDateSet){
			dateList_temp_sorted.addAll(indicatedDateSet);
		}

		dateList_temp_sorted.clear();
		HashMap<Integer, HashSet<String>> timeStartWordMap = articleTimeInfo
				.getTimeStartWordMap();

		formatDateMap = timeStamp2FormatDate(sdf, timeStartWordMap);
		dateMap_inverted = invertedDateIndex(formatDateMap,
				dateList_temp_sorted);
		if(null != dateMap_inverted){
			dateList_sorted.addAll(dateList_temp_sorted);
		}
		

		/********************** 对所有的时间进行统一处理 *****************************/
		
		HashSet<Date> dateSet = new HashSet<Date>();
		
		dateSet.addAll(dateList_sorted);
		
		dateList_sorted = new ArrayList<Date>(dateSet);
		
		Collections.sort(dateList_sorted);
		
		
		boolean todayTag_indicated = false;
		
		boolean thisSeasonTag_indicated = false;
		
		boolean todayTag_timeStamp = false;
		
		boolean thisSeasonTag_timeStamp = false;
		
		boolean thisWeekTag_indicated = false;
		
		boolean thisWeekTag_timeStamp = false;
		
		if((null != dateStampMap || !dateStampMap.isEmpty())
				||(null != reportTimeSet || !reportTimeSet.isEmpty())
				||(null != festivalStampMap || !festivalStampMap.isEmpty())){
			
			todayTag_indicated = isContainceTodayTag(dateOfTodaySet, indicateWordSet);
			
			thisSeasonTag_indicated = isCurSeasonTag(seasonDateMap, indicateWordSet);
			
			HashSet<String> timeStampSet = getTimeStampOfTopNseg(timeStartWordMap, Integer.MAX_VALUE);
			
		    todayTag_timeStamp = isContainceTodayTag(dateOfTodaySet, timeStampSet);
			
			thisSeasonTag_timeStamp = isCurSeasonTag(seasonDateMap, timeStampSet);
			
			thisWeekTag_indicated = isContainceThisWeekTag(thisWeekTagSet, indicateWordSet);
			
			thisWeekTag_timeStamp = isContainceThisWeekTag(thisWeekTagSet, indicateWordSet);
		}
		

		if(todayTag_indicated || todayTag_timeStamp){
			//时效性为当天
			//timeSensitiveInfo.setOthers("sensitiveLevel="+TimeSensitiveLevel.td+"|!|");
			articleTimeInfo.setTimeSensitiveLevel(TimeSensitiveLevel.td);
		}else{
			
			dateList_sorted.add(date_pub);
			
			Collections.sort(dateList_sorted);
			
			int index = dateList_sorted.indexOf(date_pub);
			
			int before_today_Dvalue = 0;
			int today_after_Dvalue = 0;
			if(index > 0){
				Date date_before = dateList_sorted.get(index - 1);

				before_today_Dvalue = CalendarParser.daysBetween(date_pub, date_before);
			}
			if(index < dateList_sorted.size() - 1){
				Date date_after = dateList_sorted.get(index + 1);

				today_after_Dvalue = CalendarParser.daysBetween(date_after, date_pub);
			}
			
			
			if((before_today_Dvalue == today_after_Dvalue && today_after_Dvalue == 0)//文章只讲述今天的事情
					|| (today_after_Dvalue == 0)//文章没有涉及到未来的时间点
					){
				//timeSensitiveInfo.setOthers("sensitiveLevel="+TimeSensitiveLevel.td+"|!|");
				articleTimeInfo.setTimeSensitiveLevel(TimeSensitiveLevel.td);
			}else if(today_after_Dvalue > 7){
				//timeSensitiveInfo.setOthers("sensitiveLevel="+7+"|!|");
				articleTimeInfo.setTimeValidDate(7);
			}else{
				if(thisSeasonTag_indicated || thisSeasonTag_timeStamp){
					//timeSensitiveInfo.setOthers("sensitiveLevel="+TimeSensitiveLevel.tq+"|!|");
					articleTimeInfo.setTimeSensitiveLevel(TimeSensitiveLevel.tq);
				}else{
					//timeSensitiveInfo.setOthers("sensitiveLevel="+(today_after_Dvalue-1)+"|!|");
					articleTimeInfo.setTimeValidDate((today_after_Dvalue-1));
				}
			}
		}
		return timeSensitiveInfo;
	}

	/**
	 * 
	 * @Title:getTimeStampOfTopNseg
	 * @Description: 提取指定的topN段落的时间戳tag
	 * @param timeStampMap
	 * @param topN
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月18日
	 */
	private HashSet<String> getTimeStampOfTopNseg(
			HashMap<Integer, HashSet<String>> timeStampMap, int topN) {

		HashSet<String> timeSet = new HashSet<String>();

		Iterator<Integer> iterator = timeStampMap.keySet().iterator();

		while (iterator.hasNext()) {
			int key = iterator.next();
			if (key <= topN) {
				timeSet.addAll(timeStampMap.get(key));
			}
		}
		if (timeSet.isEmpty()) {
			timeSet = null;
		}
		return timeSet;
	}

	/**
	 * 
	 * @Title:isCurWeekTag
	 * @Description: 判定文章时效性是否为当前周
	 * @param thisWeekTagSet
	 * @param tagSet
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月18日
	 */
	private boolean isCurWeekTag(HashSet<String> thisWeekTagSet,
			HashSet<String> tagSet) {
		return isContainceTodayTag(thisWeekTagSet, tagSet);
	}
	/**
	 * 
	* @Title:isCurSeasonTag
	* @Description: 判定文章时效性是否为当季度
	* @param thisSeasonTagSet
	* @param tagSet
	* @return
	* @author:wuyg1
	* @date:2016年10月19日
	 */
	private boolean isCurSeasonTag(HashMap<String, SeasonDate> seasonDateMap,
			HashSet<String> tagSet){
		return isContainceThisSeasonTag(seasonDateMap, tagSet);
	}

	/**
	 * 
	 * @Title:isContainceTodayTag
	 * @Description: 是否包含时效性必须为今天的显著性时间tag
	 * @param todayTagSet
	 * @param tagSet
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月18日
	 */
	private boolean isContainceThisSeasonTag(HashMap<String, SeasonDate> seasonDateMap,
			HashSet<String> tagSet) {
		boolean containceFlag = false;
		if (null != tagSet && !tagSet.isEmpty()) {
			Iterator<String> iterator = seasonDateMap.keySet().iterator();
			while(iterator.hasNext()) {
				String tag = iterator.next();
				if (tagSet.contains(tag)) {
					SeasonDate seasonDate = seasonDateMap.get(tag);
					if(calendarParser.getMonth() >= seasonDate.getBeginDate()
							&& calendarParser.getMonth() <= seasonDate.getEndDate()){
						containceFlag = true;
						break;
					}
				}
			}
		}
		return containceFlag;
	}
	/**
	 * 
	* @Title:isContainceThisWeekTag
	* @Description: 是否包含时效性为本周的显著性tag
	* @param weekTagSet
	* @param tagSet
	* @return
	* @author:wuyg1
	* @date:2016年11月1日
	 */
	private boolean isContainceThisWeekTag(HashSet<String> weekTagSet, HashSet<String> tagSet){
		return isContainceTodayTag(weekTagSet, tagSet);
	}
	
	/**
	 * 
	 * @Title:isContainceTodayTag
	 * @Description: 是否包含时效性必须为今天的显著性时间tag
	 * @param todayTagSet
	 * @param tagSet
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月18日
	 */
	private boolean isContainceTodayTag(HashSet<String> todayTagSet,
			HashSet<String> tagSet) {
		boolean containceFlag = false;
		if (null != tagSet && !tagSet.isEmpty()) {
			for (String tag : todayTagSet) {
				if (tagSet.contains(tag)) {
					containceFlag = true;
					break;
				}
			}
		}
		return containceFlag;
	}

	/**
	 * 
	 * @Title:timeStamp2FormatDate
	 * @Description: 将timestamp格式的时间戳转化为标准化的时间
	 * @param sdf
	 * @param timeStampMap
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月17日
	 */
	private HashMap<Integer, HashSet<Date>> timeStamp2FormatDate(
			SimpleDateFormat sdf, HashMap<Integer, HashSet<String>> timeStampMap) {

		if (null == timeStampMap) {
			return null;
		}

		HashMap<Integer, HashSet<Date>> formatDateMap = new HashMap<Integer, HashSet<Date>>();

		if (timeStampMap.containsKey(0)) {
			formatDateMap.put(0,
					IndicatedTime2FormatDate(sdf, timeStampMap.get(0)));
		}
		if (timeStampMap.containsKey(1)) {
			formatDateMap.put(1,
					IndicatedTime2FormatDate(sdf, timeStampMap.get(1)));
		}
		if (timeStampMap.containsKey(2)) {
			formatDateMap.put(2,
					IndicatedTime2FormatDate(sdf, timeStampMap.get(2)));
		}

		if (null == formatDateMap || formatDateMap.size() == 0
				|| formatDateMap.isEmpty()) {
			formatDateMap = null;
		}
		return formatDateMap;
	}

	/**
	 * 
	 * @Title:IndicatedTime2FormatDate
	 * @Description: 将指示性时间词转变为规范化的时间格式
	 * @param sdf
	 * @param dateSet_indicated
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月17日
	 */
	private HashSet<Date> IndicatedTime2FormatDate(SimpleDateFormat sdf,
			HashSet<String> dateSet_indicated) {

		if (null == dateSet_indicated || dateSet_indicated.isEmpty()) {
			return null;
		}

		HashSet<Date> dateSet = new HashSet<Date>();

		for (String dateStr : dateSet_indicated) {

			Date date = getFormatDateFromIndicatedTime(sdf, dateStr);

			if (null == date) {
				continue;
			} else {
				dateSet.add(date);
			}
		}
		return dateSet;
	}

	private Date getFormatDateFromIndicatedTime(SimpleDateFormat sdf,
			String dateStr) {

		Date date = null;
		/** 该标记用于标记是否为符合以下要求的任何一种情况，如果任何情况都不符合，则返回NULL */
		boolean processedFlag = true;

		if (dateStr.equals("今天")) {
			dateStr = calendarParser.getDateStr();
		} else if (dateStr.equals("昨天")) {
			dateStr = calendarParser.getLastDay();
		} else if (dateStr.equals("明天")) {
			dateStr = calendarParser.getNextDay();
		} else {
			Date dateFormat = getFormatDate(sdf, dateStr);
			if (null == dateFormat) {
				processedFlag = false;
			} else {
				return dateFormat;
			}
		}

		if (processedFlag) {
			try {
				date = sdf.parse(dateStr);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				return null;
			}
		}
		return date;
	}

	/**
	 * 
	 * @Title:getFormatDate
	 * @Description: 将不规则的日期转为规范化的时间 <br>
	 *               该方法处理的对象包括 <br>
	 *               (\\d{1,4}[-|.]\\d{1,2}[-|.]\\d{1,2}) <br>
	 *               (\\d{1,2}[-|.]\\d{1,2}) <br>
	 *               [本|上|下]周
	 * @param sdf
	 * @param dateStr
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月17日
	 */
	private Date getFormatDate(SimpleDateFormat sdf, String dateStr) {

		String regex_YMD = "(\\d{1,4}[-|.]\\d{1,2}[-|.]\\d{1,2})";
		// String regex_MD = "(\\d{1,2}[-|.]\\d{1,2})";
		String regex_WEEK = "[本|上|下]周";

		boolean ymdFlag = dateStr.matches(regex_YMD);
		// boolean mdFag = dateStr.matches(regex_MD);
		boolean weekFlag = dateStr.matches(regex_WEEK);

		boolean isFormatFlag = false;

		//System.err.println("ymdFlag:"+dateStr);
		
		if (ymdFlag) {
			dateStr = dateStr.replaceAll("\\.", "-");

			String[] dateArray = dateStr.split("-");

			if (dateArray[0].length() != 4) {
				isFormatFlag = false;
			}else{
				isFormatFlag = true;
			}
		}
		// else if (mdFag) {
		// dateStr = dateStr.replaceAll("\\.", "-");
		//
		// String[] dateArray = dateStr.split("-");
		//
		// if (dateArray[0].length() == 4) {
		// dateStr = dateStr + "01";
		// } else {
		// try {
		// int month = Integer.valueOf(dateArray[0]);
		// if (month >= 1 && month <= 12) {
		// dateStr = calendarParser.getYear() + "-" + dateStr;
		// }
		// } catch (NumberFormatException e) {
		// isFormatFlag = false;
		// }
		//
		// }
		// }
		else if (weekFlag) {
			if (dateStr.equals("本周")) {
				dateStr = calendarParser.getThisWeekend();
			} else if (dateStr.equals("上周")) {
				dateStr = calendarParser.getLastWeekend();
			} else if (dateStr.equals("下周")) {
				dateStr = calendarParser.getNextWeekend();
			}
			isFormatFlag = true;
		}

		Date date = null;

		if (isFormatFlag) {
			try {
				date = sdf.parse(dateStr);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				return null;
			}
		}

		return date;

	}

	/**
	 * 
	 * @Title:InvertedDateIndex
	 * @Description: 将得到的精准的格式化时间进行倒排索引，同时对所有的时间进行排序
	 * @param dateMap
	 *            精准的格式化时间
	 * @param dateList_sorted
	 *            对所有时间进行排序
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月17日
	 */
	private HashMap<Date, HashSet<Integer>> invertedDateIndex(
			HashMap<Integer, HashSet<Date>> dateMap,
			ArrayList<Date> dateList_sorted) {

		if (null == dateMap) {
			dateList_sorted = new ArrayList<Date>();
			return null;
		}

		HashMap<Date, HashSet<Integer>> dateMap_inverted = new HashMap<Date, HashSet<Integer>>();

		Iterator<Integer> iterator = dateMap.keySet().iterator();
		while (iterator.hasNext()) {
			int key = iterator.next();
			if(null == dateMap.get(key) || dateMap.get(key).isEmpty()){
				continue;
			}
			dateList_sorted.addAll(dateMap.get(key));
			for (Date date : dateMap.get(key)) {
				if (dateMap_inverted.containsKey(date)) {
					dateMap_inverted.get(date).add(key);
				} else if (!dateMap_inverted.containsKey(date)) {
					HashSet<Integer> segmentIndexSet = new HashSet<Integer>();
					segmentIndexSet.add(key);
					dateMap_inverted.put(date, segmentIndexSet);
				}
			}
		}
		Collections.sort(dateList_sorted);
		return dateMap_inverted;
	}

//	private void print(ArrayList<Date> dateList) {
//		for (Date date : dateList) {
//			System.err.println((date.getYear() + 1900) + ":"
//					+ (date.getMonth() + 1) + ":" + date.getDate());
//		}
//	}
	
	/**
	 * 
	* @Title:weekTag2DateStr
	* @Description: 将上周，本周，下周这样的时间词转为标准的时间
	* @param timeTag
	* @param dateStr
	* @param sdf
	* @return
	* @author:wuyg1
	* @date:2016年10月20日
	 */
	private String weekTag2DateStr(String timeTag, HashSet<String> dateSet, String dateStr, SimpleDateFormat ymd_sdf, 
    		SimpleDateFormat ym_sdf, SimpleDateFormat md_sdf){
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		
		Date date = null;
		
		try {
			date = ymd_sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		calendar.setTime(date);
		
		String dayRegex = "(\\d{1,2}|[\uFF10-\uFF19]{1,2})(日|号)";
		
		if(timeTag.matches(dayRegex)){
			String dateStr_format = DateConvert.convertCnAccurateDate(timeTag,dateStr,ymd_sdf,ym_sdf,md_sdf);
		    return dateStr_format;
		}
		
		
		String regex = "[本|上|下]周[一|二|三|四|五|六|日|天|末]?";
		
		String temp_regex = "周[一|二|三|四|五|六|日|天|末]";
		
		if(!timeTag.matches(regex) && timeTag.matches(temp_regex)){
			
			if(null == dateSet || dateSet.isEmpty()){
				timeTag = "本"+timeTag;
			}else{
				boolean accurateDateFlag = false;
				for(String sdate : dateSet){
					String dateStr_format = DateConvert.convertCnAccurateDate(sdate,dateStr,ymd_sdf,ym_sdf,md_sdf);
					
					String weekDay = DateConvert.getWeekOfDate(dateStr_format);
					
					if(null != weekDay && timeTag.equals(weekDay)){
						accurateDateFlag = true;
						return dateStr_format;
					}
				}
				if(!accurateDateFlag){
						timeTag = "本"+timeTag;
				}
			}
		}
		
		String ALL_CN_NUMBER = "0一二三四五六七日天末";
		String ALL_NUMBER = "01234567777";
		
		boolean flag = timeTag.matches(regex);
		
		int week_dis = 0;
		int day_dis = 0;
		
		if(flag){
			if(timeTag.startsWith("本周")){
				
				String subStr = timeTag.substring(timeTag.indexOf("本周")+"本周".length());
				int index = ALL_CN_NUMBER.indexOf(subStr);
				char dateIndex = ALL_NUMBER.charAt(index);
				
				week_dis = 0;
				
				if(dateIndex == '0'){
					week_dis = +1;
					dateIndex = '1';
				}
				
				day_dis = Integer.valueOf(String.valueOf(dateIndex));
				
			}else if(timeTag.startsWith("上周")){
				
				String subStr = timeTag.substring(timeTag.indexOf("上周")+"上周".length());
				int index = ALL_CN_NUMBER.indexOf(subStr);
				char dateIndex = ALL_NUMBER.charAt(index);
				
				week_dis = -1;
				day_dis = Integer.valueOf(String.valueOf(dateIndex));
				
			}else if(timeTag.startsWith("下周")){
				int index = 1;
				char dateIndex = ALL_NUMBER.charAt(index);
				
				week_dis = +1;
				day_dis = Integer.valueOf(String.valueOf(dateIndex));
			}
			
			day_dis = (day_dis+1)%7;

			calendar.add(Calendar.WEEK_OF_YEAR, week_dis);
			calendar.set(Calendar.DAY_OF_WEEK, day_dis);
			
			return CalendarParser.getYearFromDate(calendar.getTime())
					+"年"+CalendarParser.getMonthFromDate(calendar.getTime())
					+"月"+CalendarParser.getDayFromDate(calendar.getTime())+"日";
			
		}else {
			String regex_hour = "(\\d{1,2}|[\uFF10-\uFF19]{1,2})([点])?";
			if(timeTag.matches(regex_hour)){
				timeTag = null;
			}
			return timeTag;
		}
		
	}

	/**
	 * 
	 * @Title:dateStamp2RegularDateFormat
	 * @Description: 将精准时间映射到规范化的时间格式
	 * @param sdf
	 * @param dateStampMap
	 * @param timeClockMap
	 *            存储时刻表
	 * @param pub_date
	 * 			  文章发布时间
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月14日
	 */
	private HashMap<Integer, HashSet<Date>> dateStamp2RegularDateFormat(
			SimpleDateFormat sdf,
			HashMap<Integer, HashSet<String>> dateStampMap,
			HashMap<Integer, HashSet<String>> timeClockMap,
			Date pub_date) {

		if (null == dateStampMap) {
			timeClockMap = null;
			return null;
		}

		HashMap<Integer, HashSet<Date>> formatDateMap = new HashMap<Integer, HashSet<Date>>();

		Iterator<Integer> iterator = dateStampMap.keySet().iterator();

		String regex_YMD = "(\\d{1,4}|[\uFF10-\uFF19]{1,4})[-|\\/|年](\\d{1,2}|[\uFF10-\uFF19]{1,2})[-|\\/|月](\\d{1,2}|[\uFF10-\uFF19]{1,2})([日|号])?";
		String regex_YM = "(\\d{1,4}|[\uFF10-\uFF19]{1,4})[-|\\/|年](\\d{1,2}|[\uFF10-\uFF19]{1,2})[-|\\/|月]";
		String regex_MD = "(\\d{1,2}|[\uFF10-\uFF19]{1,2})[月]((\\d{1,2}|[\uFF10-\uFF19]{1,2})[日]?)?";

		String regularDatePattern = "年|月|/";
		while (iterator.hasNext()) {
			int key = iterator.next();
			for (String timeStr : dateStampMap.get(key)) {
				
				//timeStr = timeStr.replaceAll("本月", calendarParser.getMonth()+"月");
				
				timeStr = timeStr.replaceAll("日|号", "");
				/**
				 * 判定是否该时间标签仅仅为几号，如果是，则不再处理
				 */
				boolean OnlyNumFlag = timeStr.matches("[0-9]+");
				if (OnlyNumFlag) {
					// 当只存在单独的14日，15日信息除了在标题和首段的，其他段落都不在统计，因为存在错误的时候过多；
					if (key >= 2) {
						continue;
					} else if (key < 2) {
						timeStr = CalendarParser.getYearFromDate(pub_date)+ "年"
								+ CalendarParser.getMonthFromDate(pub_date) + "月" + timeStr;
					}
				}

				String formatTimeClock = null;

				formatTimeClock = TimeClock2FormatTime(timeStr);

				if (null != formatTimeClock) {
					if (timeClockMap.containsKey(key)) {
						timeClockMap.get(key).add(formatTimeClock);
					} else {
						HashSet<String> clockSet = new HashSet<String>();
						clockSet.add(formatTimeClock);
						timeClockMap.put(key, clockSet);
					}
				}

				boolean ymdFlag = timeStr.matches(regex_YMD);
				boolean mdFlag = timeStr.matches(regex_MD);
				boolean ymFlag = timeStr.matches(regex_YM);

				if (ymdFlag || mdFlag || ymFlag) {
					timeStr = timeStr.replaceAll(regularDatePattern, "-");
					String[] timeArray = timeStr.split("-");

					if (timeArray.length < 3 && timeArray.length > 1) {
						if (timeArray[0].trim().length() == 4) {
							// 此处处理的是几年几月的情况，在后边直接补充上具体日期就可以了
							timeStr = timeStr + "01";
						} else if (timeArray[0].trim().length() <= 2) {
							// 此处处理的是几月几日的形式
							timeStr = CalendarParser.getYearFromDate(pub_date) + "-" + timeStr;
						}
					} else if (timeArray.length == 1) {
						// 此处应该处理的是单独的年份，月份，暂且不处理
						continue;
					}

					try {
						Date date = sdf.parse(timeStr);
						if (formatDateMap.containsKey(key)) {
							formatDateMap.get(key).add(date);
						} else {
							HashSet<Date> dateSet = new HashSet<Date>();
							dateSet.add(date);
							formatDateMap.put(key, dateSet);
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						continue;
					}
				}
			}
		}
		return formatDateMap;
	}
/**
 * 
* @Title:reviseMonthTag
* @Description: 月份映射
* @param timeTag
* @return
* @author:wuyg1
* @date:2016年10月19日
 */
	private String reviseMonthTag(String timeTag){
		//(\\d{1,2}|一|二|三|四|五|六|七|八|九|十|十一|十二|腊|正|下|上|本)[月](初|份|末|底)*((\\d{1,2}|([一二三四五六七八九十]{1,3}))([日|号])?)?
		HashMap<String, String> hashMap = new HashMap<String, String>();

		hashMap.put("本月", calendarParser.getMonth()+"月");
		hashMap.put("上月", calendarParser.getLastMonth()+"月");
		hashMap.put("下月", calendarParser.getNextMonth()+"月");
		hashMap.put("腊月", "12月");
		
		String monthStr = timeTag.substring(0, timeTag.indexOf("月")+1);
		
		if(hashMap.containsKey(monthStr)){
			timeTag = hashMap.get(monthStr)+timeTag.substring(timeTag.indexOf("月")+1);
		}
		
		return timeTag;
		
	}
	
	/**
	 * 
	 * @Title:festivalStamp2RegularDateFormat
	 * @Description:对获得到的节日进行精准时间获取，进而转成规范化时间格式
	 * @param sdf
	 * @param festivalStampMap
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月14日
	 */
	private HashMap<Integer, HashSet<Date>> festivalStamp2RegularDateFormat(
			SimpleDateFormat sdf,
			HashMap<Integer, HashSet<String>> festivalStampMap) {

		if (null == festivalStampMap) {
			return null;
		}

		HashMap<Integer, HashSet<Date>> formatDateMap = new HashMap<Integer, HashSet<Date>>();

		Iterator<Integer> iterator = festivalStampMap.keySet().iterator();

		while (iterator.hasNext()) {
			int key = iterator.next();
			for (String timeStr : festivalStampMap.get(key)) {
				timeStr = traditionalFestivalDayStandard.judge(
						calendarParser.getYear(), timeStr);

				if(null == timeStr){
					continue;
				}
				
				try {
					Date date = sdf.parse(timeStr);
					if (formatDateMap.containsKey(key)) {
						formatDateMap.get(key).add(date);
					} else {
						HashSet<Date> dateSet = new HashSet<Date>();
						dateSet.add(date);
						formatDateMap.put(key, dateSet);
					}

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					continue;
				}
			}
		}
		return formatDateMap;
	}

	/**
	 * 
	 * @Title:reportStamp2RegularDateFormat
	 * @Description: 报道时间转成规范化时间格式
	 * @param sdf
	 * @param reportStampSet
	 * @param pub_date
	 * 		   文章发布时间
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月14日
	 */
	private ArrayList<Date> reportStamp2RegularDateFormat(SimpleDateFormat sdf,
			HashSet<String> reportStampSet,
			Date pub_date) {

		ArrayList<Date> dateList = new ArrayList<Date>();

		if (null == reportStampSet || reportStampSet.isEmpty()) {
			return dateList;
		}

		for (String timeStr : reportStampSet) {
			try {
				timeStr = timeStr.replaceAll("日|号|讯|电|报道", "");

				String new_timeStr = Pattern.compile("年|月|/").matcher(timeStr)
						.replaceAll("-");
				
				if(timeStr.contains("年")){
					timeStr = new_timeStr;
				}else{
					timeStr = CalendarParser.getYearFromDate(pub_date) + "-" + timeStr;
				}
				
				

				Date date = sdf.parse(timeStr);
				dateList.add(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
		}
		return dateList;
	}

	/**
	 * 
	 * @Title:TimeClock2FormatTime
	 * @Description: 将是时钟的时间转为规范化的时间格式
	 * @param dateStr
	 * @return
	 * @author:wuyg1
	 * @date:2016年10月17日
	 */
	private String TimeClock2FormatTime(String dateStr) {
		if(dateStr.contains("|")){
			return null;
		}
		String regex = "(\\d{1,2}|二|三|四|五|六|七|八|九|十|十一|十二)[点|时]";
		HashMap<String, Integer> timeClockFormatMap = new HashMap<String, Integer>();
		timeClockFormatMap.put("二", 2);
		timeClockFormatMap.put("三", 3);
		timeClockFormatMap.put("四", 4);
		timeClockFormatMap.put("五", 5);
		timeClockFormatMap.put("六", 6);
		timeClockFormatMap.put("七", 7);
		timeClockFormatMap.put("八", 8);
		timeClockFormatMap.put("九", 9);
		timeClockFormatMap.put("十", 10);
		timeClockFormatMap.put("十一", 11);
		timeClockFormatMap.put("十二", 12);
		boolean flag = Pattern.compile(regex).matcher(dateStr).find();
		if (!flag) {
			dateStr = null;
		} else if (flag) {
			dateStr = dateStr.replaceAll("点", "时");
			Integer time_int = timeClockFormatMap.get(dateStr.substring(0,
					dateStr.indexOf("时")));
			if (null != time_int) {
				dateStr = time_int + "时";
			}

		}
		return dateStr;
	}

	/**
	 * 
	 * @Title:EstimateTimeSensitiveOfArticle
	 * @Description:时效性判定
	 * @param split_title
	 *            分词的文章标题
	 * @param split_content
	 *            分词的文章内容
	 * @param text_class_list
	 *            文章所属的类别列表
	 * @param docType
	 *            标记传递的文章是文字型还是图片型
	 * @return {@TimeSensitiveInfo}
	 * @author:wuyg1
	 * @date:2016年8月26日
	 */
	public TimeSensitiveInfo EstimateTimeSensitiveOfArticle(String split_title,
			String split_content, List<String> text_class_list, String docType) {

		if (null == split_title && null == split_content) {
			TimeSensitiveInfo timeSensitiveInfo = new TimeSensitiveInfo();
			timeSensitiveInfo.setTimeSensitive(false);
			timeSensitiveInfo
					.setReasonDescribe("split_title and split_content is null!");
			return timeSensitiveInfo;
		}

		if (null == split_title) {
			split_content = "";
		}

		if (null == split_content) {
			split_content = "";
		}

		ArticleTimeInfo articleTimeInfo = new ArticleTimeInfo();

		HashSet<String> categorySet = new HashSet<String>();
		if (null != text_class_list) {
			categorySet.addAll(text_class_list);
		}

		String regex = "_[A-Za-z]+ | ";

		int index = 0;

		articleTimeInfo.setCategorySet(categorySet);

		ArrayList<String> segmentList = new ArrayList<String>();

		segmentList.add(split_title);

		segmentList.addAll(Arrays.asList(split_content.split("\n")));

		StringBuffer articleBuffer = new StringBuffer();

		articleBuffer.append(split_title.replaceAll(regex, "") + "\t");

		articleBuffer.append(split_content.replaceAll(regex, ""));

		Matcher reportMatcher = reportTimePattern.matcher(articleBuffer
				.toString());

		while (reportMatcher.find()) {
			String reportTime = reportMatcher.group();
			// LOG.info("电讯时间：" + reportTime);
			articleTimeInfo.getReportTimeSet().add(reportTime);
		}

		for (String segmentCon : segmentList) {

			String originalStr = segmentCon.replaceAll(regex, "");

			if (null == originalStr || originalStr.isEmpty()
					|| originalStr.equals("")) {
				continue;
			}

			originalStr = originalStr.replaceAll(" ", "");

			if (originalStr.isEmpty() || originalStr.equals("")
					|| originalStr.equals("_w_w")) {
				continue;
			}

			if (originalStr.startsWith("http://")
					|| originalStr.startsWith("https://")
					|| originalStr.startsWith("ftp://")) {
				continue;
			}

			if (originalStr.length() < 30 && index >= 1) {
				index = index - 1;
			}

			// LOG.info("第" + index + "段：");

			Matcher dateStampMatcher = dateStampPattern.matcher(originalStr);

			while (dateStampMatcher.find()) {
				String stamp = dateStampMatcher.group();
				// LOG.info("时间戳的精确匹配：" + stamp);
				// dateStampSet.add(stamp);

				if (articleTimeInfo.getDateStampMap().containsKey(index)) {
					articleTimeInfo.getDateStampMap().get(index).add(stamp);
				} else {
					HashSet<String> stampSet = new HashSet<String>();
					stampSet.add(stamp);
					articleTimeInfo.getDateStampMap().put(index, stampSet);
				}
			}

			Matcher festivalMatcher = festivalStampPattern.matcher(originalStr);

			while (festivalMatcher.find()) {
				String stamp = festivalMatcher.group();
				// LOG.info("节气(节日)：" + stamp);
				// festivalStampSet.add(stamp);

				if (articleTimeInfo.getFestivalStampMap().containsKey(index)) {
					articleTimeInfo.getFestivalStampMap().get(index).add(stamp);
				} else {
					HashSet<String> stampSet = new HashSet<String>();
					stampSet.add(stamp);
					articleTimeInfo.getFestivalStampMap().put(index, stampSet);
				}
			}

			if (index == 0) {
				for (String word : timeIndicateWordSet) {
					if (originalStr.contains(word)) {
						articleTimeInfo.getTimeIndicateWordSet().add(word);
					}
				}
				Matcher timeIndicateMatcher = timeIndicatePattern
						.matcher(originalStr);
				while (timeIndicateMatcher.find()) {
					articleTimeInfo.getTimeIndicateWordSet().add(
							timeIndicateMatcher.group());
				}

				Matcher timeIndicateYearMatcher = timeIndicateYearPattern
						.matcher(originalStr);
				while (timeIndicateYearMatcher.find()) {
					int year = Integer.valueOf(timeIndicateYearMatcher.group());
					if (year >= getCalendarParser().getYear()
							&& year <= (getCalendarParser().getYear() + 5)) {
						articleTimeInfo.getTimeIndicateWordSet().add(
								Integer.toString(year));
					}
				}
			}
			for (String word : timeWordSet) {
				if (originalStr.contains(word)) {
					if (articleTimeInfo.getTimeStartWordMap()
							.containsKey(index)) {
						articleTimeInfo.getTimeStartWordMap().get(index)
								.add(word);
					} else if (!articleTimeInfo.getTimeStartWordMap()
							.containsKey(index)) {
						HashSet<String> set = new HashSet<String>();
						set.add(word);
						articleTimeInfo.getTimeStartWordMap().put(index, set);
					}
				}
			}

			Matcher tMatcher = tPattern.matcher(segmentCon);
			while (tMatcher.find()) {
				String tempWord = tMatcher.group();
				tempWord = tempWord.substring(0, tempWord.lastIndexOf("_t "));
				if (timeWordSet.contains(tempWord)) {
					// LOG.info("t_的时间匹配：" + tempWord);
					if (articleTimeInfo.getTimestampMap().containsKey(index)) {
						articleTimeInfo.getTimestampMap().get(index)
								.add(tempWord);
					} else {
						HashSet<String> stampSet = new HashSet<String>();
						stampSet.add(tempWord);
						articleTimeInfo.getTimestampMap().put(index, stampSet);
					}
				}

			}

			index++;
		}

		TimeSensitiveInfo timeSensitiveInfo = null;

		if (null == docType || docType.isEmpty() || docType.equals("doc")) {
			timeSensitiveInfo = articleTimeInfoParser(articleTimeInfo);
		} else if (docType.equals("slide")) {
			timeSensitiveInfo = slideTimeInfoParser(articleTimeInfo);
		}

		return timeSensitiveInfo;
	}

	/**
	 * 
	 * @Title:slideTimeInfoParser
	 * @Description:slide类型内容的时效性判定，第一版主要是根据精准时间戳，文章内容的电讯，以及节日来进行处理；第二版会加入与当前时间的对比，
	 * @param articleTimeInfo
	 * @return 返回数据结构如@TimeSensitiveInfo
	 * @author:wuyg1
	 * @date:2016年8月26日
	 */
	private TimeSensitiveInfo slideTimeInfoParser(
			ArticleTimeInfo articleTimeInfo) {

		// 文章时效性判定结果
		TimeSensitiveInfo timeSensitiveInfo = new TimeSensitiveInfo();

		// 文章类别所处的类别敏感度级别
		int timeEffectiveFlag = getCategorySensitiveLevel(articleTimeInfo
				.getCategorySet());

		// 对 0,1,2级别的敏感度类别分别按照各自的逻辑进行处理
		if (timeEffectiveFlag == 0) {
			if (articleTimeInfo.getCategorySet().contains("体育")) {
				if ((null == articleTimeInfo.getReportTimeSet() || articleTimeInfo
						.getReportTimeSet().size() == 0)
						&& (null == articleTimeInfo.getFestivalStampMap() || articleTimeInfo
								.getFestivalStampMap().size() == 0)
						&& (null == articleTimeInfo.getDateStampMap() || articleTimeInfo
								.getDateStampMap().size() == 0)
						&& (null == articleTimeInfo.getTimeIndicateWordSet() || articleTimeInfo
								.getTimeIndicateWordSet().size() == 0)
						&& (null == articleTimeInfo.getTimeStartWordMap() || articleTimeInfo
								.getTimeStartWordMap().size() == 0)) {
					timeSensitiveInfo.setTimeSensitive(false);
					timeSensitiveInfo
							.setReasonDescribe("article categoryList containce sports,"
									+ " and the reportTimeSet,FestivalStampMap,DateStampMap,TimeIndicateWordMap,TimeStartWordMap is null!");
					return timeSensitiveInfo;
				} else {
					timeSensitiveInfo.setTimeSensitive(true);
					timeSensitiveInfo
							.setReasonDescribe("the article categoryList containce sports,"
									+ " and the reportTimeSet,FestivalStampMap,DateStampMap,TimeIndicateWordMap,TimeStartWordMap is not all null!");
					return timeSensitiveInfo;
				}
			}
			timeSensitiveInfo.setTimeSensitive(true);
			timeSensitiveInfo
					.setReasonDescribe("the article categoryLevel is zero, and it's not containce sports!");
			return timeSensitiveInfo;
		}
		// 处理不是强时效类别的文章
		if (timeEffectiveFlag != 0) {
			if ((null == articleTimeInfo.getDateStampMap() || articleTimeInfo
					.getDateStampMap().size() == 0)
					&& (null == articleTimeInfo.getFestivalStampMap() || articleTimeInfo
							.getFestivalStampMap().size() == 0)
					&& (null == articleTimeInfo.getReportTimeSet() || articleTimeInfo
							.getReportTimeSet().size() == 0)
					&& (null == articleTimeInfo.getTimeIndicateWordSet() || articleTimeInfo
							.getTimeIndicateWordSet().size() == 0)
			// && (!articleTimeInfo.getCategorySet().contains("时政")
			// && !articleTimeInfo.getCategorySet().contains("教育")
			// && !articleTimeInfo.getCategorySet().contains("娱乐") &&
			// !articleTimeInfo
			// .getCategorySet().contains("科技"))
			) {
				if (null != articleTimeInfo.getTimeStartWordMap()) {

					boolean onlycontainceNeutral = onlycontainceNeutralStamp(
							articleTimeInfo.getTimeStartWordMap(), 3);

					boolean timeStartWordFlag = false;

					if (articleTimeInfo.getTimeStartWordMap().containsKey(0)
							|| articleTimeInfo.getTimeStartWordMap()
									.containsKey(1)) {
						timeStartWordFlag = true;
					}

					timeSensitiveInfo.setTimeSensitive(!onlycontainceNeutral
							|| timeStartWordFlag);
					timeSensitiveInfo
							.setReasonDescribe("the article categoryLevel is not zero,"
									+ " and the DateStampMap,FestivalStampMap,ReportTimeSet,TimeIndicateWordSet is null,"
									+ "and the category doesn't containce politics,edu,ent and tech!");
					return timeSensitiveInfo;

				} else if (null == articleTimeInfo.getTimeStartWordMap()
						|| articleTimeInfo.getTimeStartWordMap().isEmpty()) {
					timeSensitiveInfo.setTimeSensitive(false);
					timeSensitiveInfo
							.setReasonDescribe("the article categoryLevel is not zero,"
									+ "and all feature is null!");
					return timeSensitiveInfo;
				}
			} else {
				// 处理类别时效性为1的类别
				if (timeEffectiveFlag == 1) {
					if (articleTimeInfo.getCategorySet().contains("星座")) {
						if ((null != articleTimeInfo.getDateStampMap() && articleTimeInfo
								.getDateStampMap().size() > 0)
								|| (null != articleTimeInfo
										.getFestivalStampMap() && articleTimeInfo
										.getFestivalStampMap().size() > 0)
								|| (null != articleTimeInfo.getTimestampMap() && articleTimeInfo
										.getTimestampMap().size() > 0)
								|| (null != articleTimeInfo
										.getTimeIndicateWordSet() && articleTimeInfo
										.getTimeIndicateWordSet().size() > 0)
								|| (null != articleTimeInfo.getReportTimeSet() && articleTimeInfo
										.getReportTimeSet().size() > 0)) {

							timeSensitiveInfo.setTimeSensitive(true);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category is constellation,"
											+ "and the DateStampMap,FestivalStampMap,TimeIndicateWordSet,"
											+ "ReportTimeSet is not all null!");
							return timeSensitiveInfo;
						} else {
							timeSensitiveInfo.setTimeSensitive(false);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category is constellation,"
											+ "and the DateStampMap,FestivalStampMap,TimeIndicateWordSet,"
											+ "ReportTimeSet is all null!");
							return timeSensitiveInfo;
						}
					} else if (articleTimeInfo.getCategorySet().contains("历史")
							&& !articleTimeInfo.getCategorySet().contains("时政")) {

						boolean flag = false;

						flag = checkyear(articleTimeInfo, 3,
								getCalendarParser().getYear());

						if (!flag) {

							if (articleTimeInfo.getTimeStartWordMap()
									.containsKey(0)
									|| articleTimeInfo.getTimeStartWordMap()
											.containsKey(1)) {
								flag = true;
							}
						}

						if (!flag) {
							flag = checkmonth(articleTimeInfo, 2,
									getCalendarParser().getMonth());
						}

						if (flag
								|| (null != articleTimeInfo.getReportTimeSet() && articleTimeInfo
										.getReportTimeSet().size() > 0)
								|| (null != articleTimeInfo
										.getTimeIndicateWordSet() && articleTimeInfo
										.getTimeIndicateWordSet().size() > 0)) {
							timeSensitiveInfo.setTimeSensitive(true);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category containce history and doesn't containce politics,"
											+ "and the article containce this year or the ReportTimeSet length or TimeIndicateWordSet length is greater than zero!");
							return timeSensitiveInfo;
						} else {
							timeSensitiveInfo.setTimeSensitive(false);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category containce history and doesn't containce politics,"
											+ "and the article doesn't containce this year and the ReportTimeSet and TimeIndicateWordSet length is zero!");
							return timeSensitiveInfo;
						}

					} else {
						if (articleTimeInfo.getCategorySet().contains("考古")) {

							boolean flag1 = false;

							flag1 = checkyear(articleTimeInfo,
									Integer.MAX_VALUE, getCalendarParser()
											.getYear());

							boolean flag2 = exactDateStampExtr(articleTimeInfo,
									0, Integer.MAX_VALUE);

							timeSensitiveInfo.setTimeSensitive(flag1 && flag2);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category containce archaeology!");

							return timeSensitiveInfo;
						} else if (articleTimeInfo.getCategorySet().contains(
								"摄影")
								|| articleTimeInfo.getCategorySet().contains(
										"生活")
								|| articleTimeInfo.getCategorySet().contains(
										"职场")) {

							boolean flag1 = false;

							flag1 = checkyear(articleTimeInfo,
									Integer.MAX_VALUE, getCalendarParser()
											.getYear());

							if (!flag1) {
								flag1 = checkmonth(articleTimeInfo, 2,
										getCalendarParser().getMonth());
							}

							boolean flag2 = exactDateStampExtr(articleTimeInfo,
									0, Integer.MAX_VALUE);

							boolean flag3 = false;

							if (null != articleTimeInfo
									.getTimeIndicateWordSet()
									&& articleTimeInfo.getTimeIndicateWordSet()
											.size() > 0) {
								flag3 = true;
							}

							timeSensitiveInfo.setTimeSensitive(flag1 || flag2
									|| flag3);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category containce Photography or life or career!");

							return timeSensitiveInfo;
						} else {

							boolean flag = false;

							if (isContaince(articleTimeInfo.getDateStampMap(),
									2)) {
								timeSensitiveInfo.setTimeSensitive(true);
								timeSensitiveInfo
										.setReasonDescribe("the  article categoryLevel is one,"
												+ "and the top2 segment containce DateStampMap!");
								return timeSensitiveInfo;
							}

							flag = exactDateStampExtr(articleTimeInfo, 0,
									Integer.MAX_VALUE);

							if (flag
									|| (null != articleTimeInfo
											.getReportTimeSet() && articleTimeInfo
											.getReportTimeSet().size() > 0)
									|| (null != articleTimeInfo
											.getFestivalStampMap() && articleTimeInfo
											.getFestivalStampMap().size() > 0)
									|| (null != articleTimeInfo
											.getTimeIndicateWordSet() && articleTimeInfo
											.getTimeIndicateWordSet().size() > 0)
									|| (null != articleTimeInfo
											.getTimestampMap() && articleTimeInfo
											.getTimestampMap().size() > 0)
									|| (null != articleTimeInfo
											.getTimeStartWordMap() && articleTimeInfo
											.getTimeStartWordMap().size() > 0)) {
								flag = true;
								timeSensitiveInfo.setTimeSensitive(flag);
								timeSensitiveInfo
										.setReasonDescribe("the article categoryLevel is one,"
												+ "and the ReportTimeSet,FestivalStampMap,TimeIndicateWordSet,TimestampMap"
												+ "and TimeStartWordMap is not all null !");
							}

							if (!flag) {
								if ((isContaince(
										articleTimeInfo.getFestivalStampMap(),
										2))
										|| (null != articleTimeInfo
												.getReportTimeSet() && articleTimeInfo
												.getReportTimeSet().size() > 0)
										|| (null != articleTimeInfo
												.getTimeIndicateWordSet() && articleTimeInfo
												.getTimeIndicateWordSet()
												.size() > 0)
										|| (!onlycontainceNeutralStamp(
												articleTimeInfo
														.getTimestampMap(),
												2))
										|| !onlycontainceNeutralStamp(
												articleTimeInfo
														.getTimeStartWordMap(),
												2)) {
									flag = true;
									timeSensitiveInfo.setTimeSensitive(flag);
									timeSensitiveInfo
											.setReasonDescribe("the artical categoryLevel is one,"
													+ "and the Top2 segment containce FestivalWord or "
													+ "ReportTimeSet,TimeIndicateWordSet is not all zeor!");
								} else {
									flag = false;
									timeSensitiveInfo.setTimeSensitive(flag);
									timeSensitiveInfo
											.setReasonDescribe("the artical categoryLevel is one,"
													+ "and the Top2 segment containce FestivalWord or "
													+ "ReportTimeSet,TimeIndicateWordSet is all zeor!");
								}
							}
							if (!flag) {
								if (articleTimeInfo.getCategorySet().contains(
										"时政")) {
									if ((null != articleTimeInfo
											.getTimestampMap() && articleTimeInfo
											.getTimestampMap().size() > 0)
											|| (null != articleTimeInfo
													.getTimeStartWordMap() && articleTimeInfo
													.getTimeStartWordMap()
													.size() > 0)) {
										flag = true;
										timeSensitiveInfo
												.setTimeSensitive(flag);
										timeSensitiveInfo
												.setReasonDescribe("the article categoryLevel is one,"
														+ "and the category is politics,"
														+ "and the TimeStampMap or TimeStartWordMap length is greater than zero!");
									}
								}
							}
							return timeSensitiveInfo;
						}
					}
				}
				// 处理类别时效性为2的文章
				if (timeEffectiveFlag == 2) {

					boolean flag = false;

					if (isContaince(articleTimeInfo.getDateStampMap(), 2)) {
						flag = true;
					}

					if (!flag) {
						flag = checkyear(articleTimeInfo, 5,
								getCalendarParser().getYear());
					}

					if (!flag) {
						flag = checkmonth(articleTimeInfo, 2,
								getCalendarParser().getMonth());
					}

					if (!flag) {

						if (articleTimeInfo.getTimeStartWordMap()
								.containsKey(0)
								|| articleTimeInfo.getTimeStartWordMap()
										.containsKey(1)) {
							flag = true;
						}
					}

					if (flag
							|| (null != articleTimeInfo.getReportTimeSet() && articleTimeInfo
									.getReportTimeSet().size() > 0)
							|| (isContaince(
									articleTimeInfo.getFestivalStampMap(), 2))
							|| (null != articleTimeInfo
									.getTimeIndicateWordSet() && articleTimeInfo
									.getTimeIndicateWordSet().size() > 0)) {
						timeSensitiveInfo.setTimeSensitive(true);
						timeSensitiveInfo
								.setReasonDescribe("the article categoryLevel is two,"
										+ "'and the Top5 segment containce this year or the ReportTimeSet,FestivalStampMap,TimeIndicateWordSet is not all null!");
						return timeSensitiveInfo;
					} else {
						timeSensitiveInfo.setTimeSensitive(false);
						timeSensitiveInfo
								.setReasonDescribe("the article categoryLevel is two,"
										+ "'and the Top5 segment containce this year or the ReportTimeSet,FestivalStampMap,TimeIndicateWordSet is all null!");
						return timeSensitiveInfo;
					}
				}
			}
		}

		if (null != articleTimeInfo.getReportTimeSet()
				&& articleTimeInfo.getReportTimeSet().size() > 0) {
			timeSensitiveInfo.setTimeSensitive(true);
			timeSensitiveInfo
					.setReasonDescribe("the ReportTimeSet length is greater than zero!");
			return timeSensitiveInfo;
		}

		if (articleTimeInfo.getFestivalStampMap().size() > 0) {

			if (isContaince(articleTimeInfo.getFestivalStampMap(), 2)) {
				timeSensitiveInfo.setTimeSensitive(true);
				timeSensitiveInfo
						.setReasonDescribe("the FestivalStampMap length is greater than zero,"
								+ "and the Top2 segment containce FestivalStampMap!");
				return timeSensitiveInfo;
			}
		}

		if (isContaince(articleTimeInfo.getTimestampMap(), 2)) {
			timeSensitiveInfo.setTimeSensitive(true);
			timeSensitiveInfo
					.setReasonDescribe("the Top2 segment containce TimeStampMap!");
			return timeSensitiveInfo;
		}

		if ((null != articleTimeInfo.getTimeIndicateWordSet() && articleTimeInfo
				.getTimeIndicateWordSet().size() > 0)
				|| (isContaince(articleTimeInfo.getTimeStartWordMap(), 2))) {
			timeSensitiveInfo.setTimeSensitive(true);
			timeSensitiveInfo
					.setReasonDescribe("the TimeIndicateWordSet length is greater than zero,"
							+ "or Top2 segment containce TimeStartWordMap!");
			return timeSensitiveInfo;
		}
		timeSensitiveInfo.setTimeSensitive(false);
		timeSensitiveInfo.setReasonDescribe("last select!");
		return timeSensitiveInfo;
	}
	
	
	private void TimeTagConvert(ArticleTimeInfo articleTimeInfo){

	    HashMap<Integer, HashSet<String>> dateStampMap = articleTimeInfo.getDateStampMap();
	    
	    dateFormatConvert(dateStampMap, articleTimeInfo.getPublishedTime());

	    articleTimeInfo.setReportTimeSet(dateFormatConvert(null, articleTimeInfo.getReportTimeSet(), articleTimeInfo.getPublishedTime()));
	    
	    HashMap<Integer, HashSet<String>> timeStampMap = articleTimeInfo.getTimestampMap();
	    
	    articleTimeInfo.setTimeIndicateWordSet(dateFormatConvert(null, articleTimeInfo.getTimeIndicateWordSet(), articleTimeInfo.getPublishedTime()));
	    
	    dateFormatConvert(timeStampMap, articleTimeInfo.getPublishedTime());
	    
	    dateFormatConvert(articleTimeInfo.getTimeStartWordMap(), articleTimeInfo.getPublishedTime());
	}
	/**
	 * 
	* @Title:dateFormatConvert
	* @Description: 时间格式转换为规范格式，转换包括全角，汉字等
	* @param hashMap
	* @param sdf
	* @author:wuyg1
	* @date:2016年10月19日
	 */
	private void dateFormatConvert(HashMap<Integer, HashSet<String>> hashMap, String pub_time){
		    
		   if(null == hashMap || hashMap.isEmpty()){
			   return;
		   }
		   
		   Iterator<Integer> iterator = hashMap.keySet().iterator();
		   HashSet<String> weekTagSet = new HashSet<String>();
		    while(iterator.hasNext()){
		    	Integer segKey = iterator.next();
		    	HashSet<String> dateSet = hashMap.get(segKey);
		    	HashSet<String> set = dateFormatConvert(weekTagSet,dateSet, pub_time);
		    	if(null == set){
		    		continue;
		    	}
		    	hashMap.put(segKey, set);
		    }
	}
	
	private HashSet<String> dateFormatConvert(HashSet<String> weekTagSet, HashSet<String> dateSet, String pub_time){
    	HashSet<String> dateSet_Format = new HashSet<String>();
    	
		 SimpleDateFormat ymd_sdf = new SimpleDateFormat("yyyy-MM-dd");
		 
		 SimpleDateFormat ym_sdf = new SimpleDateFormat("yyyy-MM");
		 
		 SimpleDateFormat md_sdf = new SimpleDateFormat("MM-dd");
    	
		 if(null == dateSet || dateSet.isEmpty()){
			 return null;
		 }
		 
    	for(String dateStr : dateSet){
    		if(!dateStr.contains("年")
    				&& !dateStr.contains("月")){
    			if(null != weekTagSet && weekTagSet.contains(dateStr)){
    				continue;
    			}
    			String timeTag = weekTag2DateStr(dateStr, dateSet,pub_time, ymd_sdf, ym_sdf, md_sdf);
    			if(null != weekTagSet && !dateStr.equals(timeTag)){
    				weekTagSet.add(dateStr);
    			}
    			if(null != timeTag && !timeTag.isEmpty()){
    				dateSet_Format.add(timeTag);
    			}
    			
    		}else if(dateStr.equals("上月") || dateStr.equals("本月") || dateStr.equals("下月")){
    			String timeTag =  DateConvert.monthTag2DateStr(dateStr, pub_time, ymd_sdf);
    			if(null != timeTag && !timeTag.isEmpty()){
    				dateSet_Format.add(timeTag);
    			}
    		}else{	
    			String dateStr_format = DateConvert.convertCnDate(dateStr,pub_time,ymd_sdf,ym_sdf,md_sdf);
    			dateSet_Format.add(dateStr_format);
    		}
    	}
    	return dateSet_Format;
	}
	/**
	 * 
	 * @Title:articleTimeInfoParser
	 * @Description:文章时效性判定，第一版主要是根据精准时间戳，文章内容的电讯，以及节日来进行处理；第二版会加入与当前时间的对比，
	 * @param articleTimeInfo
	 * @return 返回数据结构如@TimeSensitiveInfo
	 * @author:wuyg1
	 * @date:2016年8月26日
	 */
	private TimeSensitiveInfo articleTimeInfoParser(
			ArticleTimeInfo articleTimeInfo) {

		// 文章时效性判定结果
		TimeSensitiveInfo timeSensitiveInfo = new TimeSensitiveInfo();

		// 文章类别所处的类别敏感度级别
		int timeEffectiveFlag = getCategorySensitiveLevel(articleTimeInfo
				.getCategorySet());

		// 对 0,1,2级别的敏感度类别分别按照各自的逻辑进行处理
		if (timeEffectiveFlag == 0) {
			if (articleTimeInfo.getCategorySet().contains("体育")) {
				if ((null == articleTimeInfo.getReportTimeSet() || articleTimeInfo
						.getReportTimeSet().size() == 0)
						&& (null == articleTimeInfo.getFestivalStampMap() || articleTimeInfo
								.getFestivalStampMap().size() == 0)
						&& (null == articleTimeInfo.getDateStampMap() || articleTimeInfo
								.getDateStampMap().size() == 0)
						&& (null == articleTimeInfo.getTimeIndicateWordSet() || articleTimeInfo
								.getTimeIndicateWordSet().size() == 0)
						&& (null == articleTimeInfo.getTimeStartWordMap() || articleTimeInfo
								.getTimeStartWordMap().size() == 0)) {
					timeSensitiveInfo.setTimeSensitive(false);
					timeSensitiveInfo
							.setReasonDescribe("article categoryList containce sports,"
									+ " and the reportTimeSet,FestivalStampMap,DateStampMap,TimeIndicateWordMap,TimeStartWordMap is null!");
					return timeSensitiveInfo;
				} else {
					timeSensitiveInfo.setTimeSensitive(true);
					timeSensitiveInfo
							.setReasonDescribe("the article categoryList containce sports,"
									+ " and the reportTimeSet,FestivalStampMap,DateStampMap,TimeIndicateWordMap,TimeStartWordMap is not all null!");
					return timeSensitiveInfo;
				}
			}
			timeSensitiveInfo.setTimeSensitive(true);
			timeSensitiveInfo
					.setReasonDescribe("the article categoryLevel is zero, and it's not containce sports!");
			return timeSensitiveInfo;
		}
		// 处理不是强时效类别的文章
		if (timeEffectiveFlag != 0) {
			if ((null == articleTimeInfo.getDateStampMap() || articleTimeInfo
					.getDateStampMap().size() == 0)
					&& (null == articleTimeInfo.getFestivalStampMap() || articleTimeInfo
							.getFestivalStampMap().size() == 0)
					&& (null == articleTimeInfo.getReportTimeSet() || articleTimeInfo
							.getReportTimeSet().size() == 0)
					&& (null == articleTimeInfo.getTimeIndicateWordSet() || articleTimeInfo
							.getTimeIndicateWordSet().size() == 0)
			// && (!articleTimeInfo.getCategorySet().contains("时政")
			// && !articleTimeInfo.getCategorySet().contains("教育")
			// && !articleTimeInfo.getCategorySet().contains("娱乐") &&
			// !articleTimeInfo
			// .getCategorySet().contains("科技"))
			) {
				if (null != articleTimeInfo.getTimeStartWordMap()) {

					boolean onlycontainceNeutral = onlycontainceNeutralStamp(
							articleTimeInfo.getTimeStartWordMap(), 3);

					boolean timeStartWordFlag = false;

					if (articleTimeInfo.getTimeStartWordMap().containsKey(0)
							|| articleTimeInfo.getTimeStartWordMap()
									.containsKey(1)) {
						timeStartWordFlag = true;
					}

					timeSensitiveInfo.setTimeSensitive(!onlycontainceNeutral
							|| timeStartWordFlag);
					timeSensitiveInfo
							.setReasonDescribe("the article categoryLevel is not zero,"
									+ " and the DateStampMap,FestivalStampMap,ReportTimeSet,TimeIndicateWordSet is null,"
									+ "and the category doesn't containce politics,edu,ent and tech!");
					return timeSensitiveInfo;

				} else if (null == articleTimeInfo.getTimeStartWordMap()
						|| articleTimeInfo.getTimeStartWordMap().isEmpty()) {
					timeSensitiveInfo.setTimeSensitive(false);
					timeSensitiveInfo
							.setReasonDescribe("the article categoryLevel is not zero,"
									+ "and all feature is null!");
					return timeSensitiveInfo;
				}
			} else {
				// 处理类别时效性为1的类别
				if (timeEffectiveFlag == 1) {
					if (articleTimeInfo.getCategorySet().contains("星座")) {
						if ((null != articleTimeInfo.getDateStampMap() && articleTimeInfo
								.getDateStampMap().size() > 0)
								|| (null != articleTimeInfo
										.getFestivalStampMap() && articleTimeInfo
										.getFestivalStampMap().size() > 0)
								|| (null != articleTimeInfo.getTimestampMap() && articleTimeInfo
										.getTimestampMap().size() > 0)
								|| (null != articleTimeInfo
										.getTimeIndicateWordSet() && articleTimeInfo
										.getTimeIndicateWordSet().size() > 0)
								|| (null != articleTimeInfo.getReportTimeSet() && articleTimeInfo
										.getReportTimeSet().size() > 0)) {

							timeSensitiveInfo.setTimeSensitive(true);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category is constellation,"
											+ "and the DateStampMap,FestivalStampMap,TimeIndicateWordSet,"
											+ "ReportTimeSet is not all null!");
							return timeSensitiveInfo;
						} else {
							timeSensitiveInfo.setTimeSensitive(false);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category is constellation,"
											+ "and the DateStampMap,FestivalStampMap,TimeIndicateWordSet,"
											+ "ReportTimeSet is all null!");
							return timeSensitiveInfo;
						}
					} else if (articleTimeInfo.getCategorySet().contains("历史")
							&& !articleTimeInfo.getCategorySet().contains("时政")) {

						boolean flag = false;

						flag = checkyear(articleTimeInfo, 3,
								getCalendarParser().getYear());

						if (!flag) {

							if (articleTimeInfo.getTimeStartWordMap()
									.containsKey(0)
									|| articleTimeInfo.getTimeStartWordMap()
											.containsKey(1)) {
								flag = true;
							}
						}

						if (!flag) {
							flag = checkmonth(articleTimeInfo, 2,
									getCalendarParser().getMonth());
						}

						if (flag
								|| (null != articleTimeInfo.getReportTimeSet() && articleTimeInfo
										.getReportTimeSet().size() > 0)
								|| (null != articleTimeInfo
										.getTimeIndicateWordSet() && articleTimeInfo
										.getTimeIndicateWordSet().size() > 0)) {
							timeSensitiveInfo.setTimeSensitive(true);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category containce history and doesn't containce politics,"
											+ "and the article containce this year or the ReportTimeSet length or TimeIndicateWordSet length is greater than zero!");
							return timeSensitiveInfo;
						} else {
							timeSensitiveInfo.setTimeSensitive(false);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category containce history and doesn't containce politics,"
											+ "and the article doesn't containce this year and the ReportTimeSet and TimeIndicateWordSet length is zero!");
							return timeSensitiveInfo;
						}

					} else {
						if (articleTimeInfo.getCategorySet().contains("考古")) {

							boolean flag1 = false;

							flag1 = checkyear(articleTimeInfo,
									Integer.MAX_VALUE, getCalendarParser()
											.getYear());

							boolean flag2 = exactDateStampExtr(articleTimeInfo,
									0, Integer.MAX_VALUE);

							timeSensitiveInfo.setTimeSensitive(flag1 && flag2);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category containce archaeology!");

							return timeSensitiveInfo;
						} else if (articleTimeInfo.getCategorySet().contains(
								"摄影")
								|| articleTimeInfo.getCategorySet().contains(
										"生活")
								|| articleTimeInfo.getCategorySet().contains(
										"职场")) {

							boolean flag1 = false;

							flag1 = checkyear(articleTimeInfo,
									Integer.MAX_VALUE, getCalendarParser()
											.getYear());

							if (!flag1) {
								flag1 = checkmonth(articleTimeInfo, 2,
										getCalendarParser().getMonth());
							}

							boolean flag2 = exactDateStampExtr(articleTimeInfo,
									0, Integer.MAX_VALUE);

							boolean flag3 = false;

							if (null != articleTimeInfo
									.getTimeIndicateWordSet()
									&& articleTimeInfo.getTimeIndicateWordSet()
											.size() > 0) {
								flag3 = true;
							}

							timeSensitiveInfo.setTimeSensitive(flag1 || flag2
									|| flag3);
							timeSensitiveInfo
									.setReasonDescribe("the article categoryLevel is one,"
											+ "and the category containce Photography or life or career!");

							return timeSensitiveInfo;
						} else {

							boolean flag = false;

							if((null == articleTimeInfo.getDateStampMap() || articleTimeInfo.getDateStampMap().isEmpty())
									&& (null == articleTimeInfo.getFestivalStampMap() || articleTimeInfo.getFestivalStampMap().isEmpty())
									&& (null == articleTimeInfo.getReportTimeSet() || articleTimeInfo.getReportTimeSet().isEmpty())
									&& (null == articleTimeInfo.getTimeIndicateWordSet() || articleTimeInfo.getTimeIndicateWordSet().isEmpty())){
								
								boolean timestampContainceFlag = isContaince(articleTimeInfo.getTimeStartWordMap(), 4);
								
								if(timestampContainceFlag){
									//如果文章前四段包含了指定的时间戳，则文章定义为时效性
									
									timeSensitiveInfo.setTimeSensitive(true);
									timeSensitiveInfo
											.setReasonDescribe("the  article categoryLevel is one,"
													+ "and the top4 segment containce TimeStartWord!");
									return timeSensitiveInfo;
									
								}else if(!timestampContainceFlag){
									//如果文章中其他的时间特征都没有，而且前四段也没有相应的时间戳标记，那么定性为非时效性的
									flag = false;
									
									timeSensitiveInfo.setTimeSensitive(false);
									timeSensitiveInfo
											.setReasonDescribe("the  article categoryLevel is one,"
													+ "and the top4 segment doesn't containce TimeStartWord!");
									return timeSensitiveInfo;
									
								}
							}
							
							
							
							if (isContaince(articleTimeInfo.getDateStampMap(),
									2)) {
								timeSensitiveInfo.setTimeSensitive(true);
								timeSensitiveInfo
										.setReasonDescribe("the  article categoryLevel is one,"
												+ "and the top2 segment containce DateStampMap!");
								return timeSensitiveInfo;
							}
							

							flag = exactDateStampExtr(articleTimeInfo, 0,
									Integer.MAX_VALUE);

							if (flag
									|| (null != articleTimeInfo
											.getReportTimeSet() && articleTimeInfo
											.getReportTimeSet().size() > 0)
									|| (null != articleTimeInfo
											.getFestivalStampMap() && articleTimeInfo
											.getFestivalStampMap().size() > 0)
									|| (null != articleTimeInfo
											.getTimeIndicateWordSet() && articleTimeInfo
											.getTimeIndicateWordSet().size() > 0)
									|| (null != articleTimeInfo
											.getTimestampMap() && articleTimeInfo
											.getTimestampMap().size() > 0)
									|| (null != articleTimeInfo
											.getTimeStartWordMap() && articleTimeInfo
											.getTimeStartWordMap().size() > 0)) {
								flag = true;
								timeSensitiveInfo.setTimeSensitive(flag);
								timeSensitiveInfo
										.setReasonDescribe("the article categoryLevel is one,"
												+ "and the ReportTimeSet,FestivalStampMap,TimeIndicateWordSet,TimestampMap"
												+ "and TimeStartWordMap is not all null !");
							}

							if (!flag) {
								if ((isContaince(
										articleTimeInfo.getFestivalStampMap(),
										2))
										|| (null != articleTimeInfo
												.getReportTimeSet() && articleTimeInfo
												.getReportTimeSet().size() > 0)
										|| (null != articleTimeInfo
												.getTimeIndicateWordSet() && articleTimeInfo
												.getTimeIndicateWordSet()
												.size() > 0)
										|| (!onlycontainceNeutralStamp(
												articleTimeInfo
														.getTimestampMap(),
												2))
										|| !onlycontainceNeutralStamp(
												articleTimeInfo
														.getTimeStartWordMap(),
												2)) {
									flag = true;
									timeSensitiveInfo.setTimeSensitive(flag);
									timeSensitiveInfo
											.setReasonDescribe("the artical categoryLevel is one,"
													+ "and the Top2 segment containce FestivalWord or "
													+ "ReportTimeSet,TimeIndicateWordSet is not all zeor!");
								} else {
									flag = false;
									timeSensitiveInfo.setTimeSensitive(flag);
									timeSensitiveInfo
											.setReasonDescribe("the artical categoryLevel is one,"
													+ "and the Top2 segment containce FestivalWord or "
													+ "ReportTimeSet,TimeIndicateWordSet is all zeor!");
								}
							}
							if (!flag) {
								if (articleTimeInfo.getCategorySet().contains(
										"时政")) {
									if ((null != articleTimeInfo
											.getTimestampMap() && articleTimeInfo
											.getTimestampMap().size() > 0)
											|| (null != articleTimeInfo
													.getTimeStartWordMap() && articleTimeInfo
													.getTimeStartWordMap()
													.size() > 0)) {
										flag = true;
										timeSensitiveInfo
												.setTimeSensitive(flag);
										timeSensitiveInfo
												.setReasonDescribe("the article categoryLevel is one,"
														+ "and the category is politics,"
														+ "and the TimeStampMap or TimeStartWordMap length is greater than zero!");
									}
								}
							}
							return timeSensitiveInfo;
						}
					}
				}
				// 处理类别时效性为2的文章
				if (timeEffectiveFlag == 2) {

					boolean flag = false;

					if (isContaince(articleTimeInfo.getDateStampMap(), 2)) {
						flag = true;
					}

					if (!flag) {
						flag = checkyear(articleTimeInfo, 5,
								getCalendarParser().getYear());
					}

					if (!flag) {
						flag = checkmonth(articleTimeInfo, 2,
								getCalendarParser().getMonth());
					}

					if (!flag) {

						if (articleTimeInfo.getTimeStartWordMap()
								.containsKey(0)
								|| articleTimeInfo.getTimeStartWordMap()
										.containsKey(1)) {
							flag = true;
						}
					}

					if (flag
							|| (null != articleTimeInfo.getReportTimeSet() && articleTimeInfo
									.getReportTimeSet().size() > 0)
							|| (isContaince(
									articleTimeInfo.getFestivalStampMap(), 2))
							|| (null != articleTimeInfo
									.getTimeIndicateWordSet() && articleTimeInfo
									.getTimeIndicateWordSet().size() > 0)) {
						timeSensitiveInfo.setTimeSensitive(true);
						timeSensitiveInfo
								.setReasonDescribe("the article categoryLevel is two,"
										+ "'and the Top5 segment containce this year or the ReportTimeSet,FestivalStampMap,TimeIndicateWordSet is not all null!");
						return timeSensitiveInfo;
					} else {
						timeSensitiveInfo.setTimeSensitive(false);
						timeSensitiveInfo
								.setReasonDescribe("the article categoryLevel is two,"
										+ "'and the Top5 segment containce this year or the ReportTimeSet,FestivalStampMap,TimeIndicateWordSet is all null!");
						return timeSensitiveInfo;
					}
				}
			}
		}

		if (null != articleTimeInfo.getReportTimeSet()
				&& articleTimeInfo.getReportTimeSet().size() > 0) {
			timeSensitiveInfo.setTimeSensitive(true);
			timeSensitiveInfo
					.setReasonDescribe("the ReportTimeSet length is greater than zero!");
			return timeSensitiveInfo;
		}

		if (articleTimeInfo.getFestivalStampMap().size() > 0) {

			if (isContaince(articleTimeInfo.getFestivalStampMap(), 2)) {
				timeSensitiveInfo.setTimeSensitive(true);
				timeSensitiveInfo
						.setReasonDescribe("the FestivalStampMap length is greater than zero,"
								+ "and the Top2 segment containce FestivalStampMap!");
				return timeSensitiveInfo;
			}
		}

		if (isContaince(articleTimeInfo.getTimestampMap(), 2)) {
			timeSensitiveInfo.setTimeSensitive(true);
			timeSensitiveInfo
					.setReasonDescribe("the Top2 segment containce TimeStampMap!");
			return timeSensitiveInfo;
		}

		if ((null != articleTimeInfo.getTimeIndicateWordSet() && articleTimeInfo
				.getTimeIndicateWordSet().size() > 0)
				|| (isContaince(articleTimeInfo.getTimeStartWordMap(), 2))) {
			timeSensitiveInfo.setTimeSensitive(true);
			timeSensitiveInfo
					.setReasonDescribe("the TimeIndicateWordSet length is greater than zero,"
							+ "or Top2 segment containce TimeStartWordMap!");
			return timeSensitiveInfo;
		}
		timeSensitiveInfo.setTimeSensitive(false);
		timeSensitiveInfo.setReasonDescribe("last select!");
		return timeSensitiveInfo;
	}

	/**
	 * 
	 * @Title:onlycontainceNeutralStamp
	 * @Description:判定是否仅包含中性时间词
	 * @param hashMap
	 * @return
	 * @author:wuyg1
	 * @date:2016年8月24日
	 */
	private boolean onlycontainceNeutralStamp(
			HashMap<Integer, HashSet<String>> hashMap, int maxsize) {
		Iterator<Integer> iterator = hashMap.keySet().iterator();

		boolean flag = true;

		while (iterator.hasNext()) {
			int key = iterator.next();
			if (key <= maxsize) {
				HashSet<String> wordSet = hashMap.get(key);
				if (!neutralWordSet.containsAll(wordSet)) {
					flag = false;
				}
			}
		}
		return flag;
	}

	/**
	 * 
	 * @Title:exactDateStampExtr
	 * @Description:能否抽取到精确的时间，从文章的beginSegment段落开始,到maxsizeSegment段落截止
	 * @param articleTimeInfo
	 * @param beginSegment
	 * @param maxsizeSegment
	 * @return
	 * @author:wuyg1
	 * @date:2016年8月24日
	 */
	private boolean exactDateStampExtr(ArticleTimeInfo articleTimeInfo,
			int beginSegment, int maxsizeSegment) {
		Iterator<Integer> iterator = articleTimeInfo.getDateStampMap().keySet()
				.iterator();
		while (iterator.hasNext()) {
			Integer key = iterator.next();
			if (key > beginSegment && key < maxsizeSegment) {
				HashSet<String> dateStampSet = articleTimeInfo
						.getDateStampMap().get(key);
				for (String date : dateStampSet) {
					Matcher matcher = ExactDateStampPattern.matcher(date);
					if (matcher.find()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @Title:checkyear
	 * @Description:检查文章规定的前几段是否有包含的年份
	 * @param articleTimeInfo
	 * @param maxsize
	 * @param yearCal
	 * @return
	 * @author:wuyg1
	 * @date:2016年8月24日
	 */
	private boolean checkyear(ArticleTimeInfo articleTimeInfo, int maxsize,
			int yearCal) {
		boolean flag = false;
		Iterator<Integer> iterator = articleTimeInfo.getDateStampMap().keySet()
				.iterator();
		while (iterator.hasNext()) {
			int key = iterator.next();
			if (key <= maxsize) {
				for (String time : articleTimeInfo.getDateStampMap().get(key)) {
					if (time.contains(yearCal + "年")) {
						flag = true;
					}
				}
			}
		}
		return flag;
	}

	/**
	 * 
	 * @Title:checkmonth
	 * @Description:检查文章指定的段落是否包含本月份
	 * @param articleTimeInfo
	 * @param maxsize
	 * @param monthCal
	 * @return
	 * @author:wuyg1
	 * @date:2016年9月19日
	 */
	private boolean checkmonth(ArticleTimeInfo articleTimeInfo, int maxsize,
			int monthCal) {
		boolean flag = false;
		Iterator<Integer> iterator = articleTimeInfo.getDateStampMap().keySet()
				.iterator();
		while (iterator.hasNext()) {
			int key = iterator.next();
			if (key <= maxsize) {
				for (String time : articleTimeInfo.getDateStampMap().get(key)) {
					if (time.contains(monthCal + "月")) {
						flag = true;
					}
				}
			}
		}
		return flag;
	}

	/**
	 * 
	 * @Title:isContaince
	 * @Description:判定hashMap中key小于index的value是否存在，如果存在返回true;
	 * @param hashMap
	 * @param index
	 * @return
	 * @author:wuyg1
	 * @date:2016年8月23日
	 */
	private boolean isContaince(HashMap<Integer, HashSet<String>> hashMap,
			int index) {
		if (null == hashMap) {
			return false;
		}
		boolean flag = false;
		Iterator<Integer> iterator = hashMap.keySet().iterator();
		while (iterator.hasNext()) {
			Integer key = iterator.next();
			if (key <= index) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	/**
	 * 
	 * @Title:getCategorySensitiveLevel
	 * @Description:如果文章所属领域为时效敏感性的，则直接返回0;<p>如果是不具有时效性类型的，则返回2；<p>如果有可能具有时效性，也有可能不具有时效性，则返回1；
	 * @param categorySet
	 * @return
	 * @author:wuyg1
	 * @date:2016年8月16日
	 */
	private int getCategorySensitiveLevel(HashSet<String> categorySet) {

		int timeEffectiveFlag = -1;
		// 对于没有类别的文章，有可能有时效性，也有可能没有看，因为无法从类别的时效性上对其进行初步判定
		if (null == categorySet || categorySet.isEmpty()) {
			return 1;
		}

		for (String category : categorySet) {
			if (categorySensitiveMap.containsKey(category)) {
				switch (categorySensitiveMap.get(category)) {
				case 0:
					timeEffectiveFlag = 0;
					break;
				case 1:
					if (timeEffectiveFlag != 0) {
						timeEffectiveFlag = 1;
					}
					break;
				case 2:
					if (timeEffectiveFlag == -1) {
						timeEffectiveFlag = 2;
					}
					break;
				}
			}
		}
		return timeEffectiveFlag;
	}
	
	

	private CalendarParser getCalendarParser() {
		return calendarParser;
	}

	private void setCalendarParser(CalendarParser calendarParser) {
		this.calendarParser = calendarParser;
	}
}
