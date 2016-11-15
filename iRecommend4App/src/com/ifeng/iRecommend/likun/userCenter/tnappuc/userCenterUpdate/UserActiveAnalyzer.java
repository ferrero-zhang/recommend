package com.ifeng.iRecommend.likun.userCenter.tnappuc.userCenterUpdate;

/**
 * <PRE>
 * 作用 : 
 *  根据用户行为，分析计算用户活跃度 
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
 *          1.0          2015年7月4日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class UserActiveAnalyzer {
	/**
	 * @Fields statistics_days : 
	 *  数据统计的天数
	 */
	private int statistics_days;
		
	/**
	 * @Fields active_days : 
	 *  在数据统计的天数内，有访问行为的天数
	 */
	private int active_days;
	
	/**
	 * @Fields read_doc_num : 
	 * 在数据统计的天数内，读过的文章数量
	 */
	private int read_doc_num;
	
	/**
	 * @Fields read_keyword_num :
	 *  在数据统计的天数内，读过的关键词列表数量
	 */
	private int read_keyword_num;
	
	/**
	 * @Fields book_word_num : 
	 * 在数据统计的天数内，订阅过的关键词的数量(包括interest和book)
	 */
	private int book_word_num;
	
	
	/**
	 * @Fields howlongnotcome : 
	 * 多久没有来
	 */
	private int howlongnotcome;
	
	

	// 各计算要素权重
	private  final int read_doc_num_weight = 3;
	private  final int read_keyword_weight = 2;
	private  final int book_word_weight = 4;
	
	// 初始启动用户action num分水岭
	private static final int startUserActionNum = 10;
	
	private static final String level1User = "A";
	private static final String level2User = "B";
	private static final String level3User = "C";
	private static final String level4User = "D";//死亡状态用户，已经一个月不来了，而且历史上也没有多于startUserActionNum条的访问行为
	private static final String startUser = "S";
	

	public UserActiveAnalyzer(int statistics_days, int active_days, int howlongnotcome,
			                  int read_doc_num, int read_keyword_num, int book_word_num) {
		this.setStatistics_days(statistics_days);
		this.setActive_days(active_days);
		this.setRead_doc_num(read_doc_num);
		this.setRead_keyword_num(read_keyword_num);
		this.setBook_word_num(book_word_num);
		this.setHowlongnotcome(howlongnotcome);
	}
	
	// 临时用下
	public static String getDefalutUserActive() {
		return level1User;
	}
	
	/**
	 * @Title: getUserActive
	 * @Description: 活跃度计算，综合考虑多重影响因子，具体数字都是拍大腿定的，可继续调整
	 * @author liu_yi
	 * @return
	 * @throws
	 */
	public String getUserActive() {
		double epsilon = 1e-5;
		
		int userActionNum = this.book_word_num + this.read_doc_num + this.read_keyword_num;
		
		//如果用户，已经一个月不来了，而且历史上也没有多于startUserActionNum条的访问行为，认为是死亡用户，给D
		if (userActionNum <= startUserActionNum && howlongnotcome>=30) {
			return level4User;
		}
		
		// 如果用户没什么action行为，认为是初始启动用户;
		if (userActionNum <= startUserActionNum) {
			return startUser;
		}
		
		// 有访问行为的天数/统计窗口时间(天数) 作为权重, 显然天天访问的用户更为活跃
		// 0.001是为了防止除0错误
		double userActionWeight = this.active_days/(this.statistics_days + epsilon);
		
		int weightedSum = 0;
		if (0 != this.book_word_num) {
			weightedSum += this.book_word_weight;
		}
		
		if (0 != this.read_doc_num) {
			weightedSum += this.read_doc_num_weight;
		}
		
		if (0 != this.read_keyword_num) {
			weightedSum += this.read_keyword_weight;
		}
		
		// 权重计算/权重和
		double weightedUserAction = (this.book_word_num * this.book_word_weight + 
				                    this.read_doc_num * this.read_doc_num_weight +
				                    this.read_keyword_num * this.read_keyword_weight)/
				                    Double.valueOf(weightedSum + epsilon);
		
		// 无访问行为的用户
		if (userActionWeight < epsilon) {
			// level4User
			return level4User;
		}
		
		double avg_weightedUserAction = weightedUserAction/(active_days + 0.001);
				
		if ((userActionWeight > 0.85 && avg_weightedUserAction > 3.5) ||
		    (userActionWeight > 0.43 && avg_weightedUserAction > 7.0) ||
		    (userActionWeight > 0.28 && avg_weightedUserAction > 12.0)) {
			
			/*
			 * A级别的用户:
			 * 
			 * 1) 周活跃6天以上(6/7) && 活跃天内平均权值阅读 > 3.5
			 * 2) 周活跃4天以上(4/7) && 活跃天内平均权值阅读 > 10.0
			 * 3) 周活跃2天以上(2/7) && 活跃天内平均权值阅读 > 15.0
			 */
			
			return level1User;
		} else if ((userActionWeight > 0.29 && avg_weightedUserAction > 5.0) ||
			       (userActionWeight > 0.57 && avg_weightedUserAction > 3.0)) {
			/*
			 * B级别的用户:
			 * 
			 * 1) 周活跃2天以上(2/7) && 每天权值阅读 > 6.0
			 * 2) 周活跃4天以上(4/7) && 每天权值阅读 > 3.2
			 * 
			 */
			
			return level2User;
		} else if ((userActionWeight > 0.29 && avg_weightedUserAction > 1.5)) {
			/*
			 * C级别的用户:
			 * 
			 * 1) 周活跃2天以上(2/7) && 每天权值阅读 > 1.5
			 * 
			 */
			
			return level3User;
			
		} else {
			return level4User;
		}
	}
	
	public int getStatistics_days() {
		return statistics_days;
	}

	public void setStatistics_days(int statistics_days) {
		this.statistics_days = statistics_days;
	}

	public int getActive_days() {
		return active_days;
	}

	public void setActive_days(int active_days) {
		this.active_days = active_days;
	}

	public int getRead_doc_num() {
		return read_doc_num;
	}

	public void setRead_doc_num(int read_doc_num) {
		this.read_doc_num = read_doc_num;
	}

	public int getRead_keyword_num() {
		return read_keyword_num;
	}

	public void setRead_keyword_num(int read_keyword_num) {
		this.read_keyword_num = read_keyword_num;
	}

	public int getBook_word_num() {
		return book_word_num;
	}

	public void setBook_word_num(int book_word_num) {
		this.book_word_num = book_word_num;
	}
	
	
	public int getHowlongnotcome() {
		return howlongnotcome;
	}

	public void setHowlongnotcome(int howlongnotcome) {
		this.howlongnotcome = howlongnotcome;
	}
	
	public static void main(String[] args) {
		UserActiveAnalyzer ua = new UserActiveAnalyzer(10, 6, 3, 10, 20, 28);
		System.out.println(ua.getUserActive());
	}
}
