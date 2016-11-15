/**
 * 
 */
package com.ifeng.iRecommend.likun.rankModel;

/**
 * <PRE>
 * 作用 : 
 *   识别女性倾向内容，（关键词模板），将其设置为available == false，不推荐，避免误差；
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
 *          1.0          2016年11月8日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class femaleFilter {
	
	/**
	 * 计算item是否强女性偏向,这个过滤器超级简单
	 * <p>
	 * 对item，只用title的简单模板匹配，分析其是否强女性偏向；
	 * 注意：
	 *  效果偏向误杀；
	 * </p>
	 * 
	 * @param title
	 */
	public static boolean isFemaleDoc(RankItemNew itemR){
		if(itemR == null || itemR.getItem() == null || itemR.getItem().getTitle() == null || itemR.getItem().getTitle().isEmpty())
			return false;
		
		String title = itemR.getItem().getTitle();
		
		if(title.indexOf("月经")>=0 || title.indexOf("阴道炎")>=0 || title.indexOf("备孕")>=0)
			return true;
		
		if(itemR.getItem().getFeatures() != null)
		{
			String feautures = itemR.getItem().getFeatures().toString();
			if(feautures != null && feautures.contains("健康") )
			{
				if(title.indexOf("经期")>=0 || title.indexOf("阴道")>=0 
						|| title.indexOf("孕期")>=0 || title.indexOf("怀孕")>=0
						|| title.indexOf("备孕")>=0
						|| title.indexOf("白带")>=0
						|| title.indexOf("私处")>=0)
					return true;
				
				if(title.indexOf("更年期")>=0 
						|| title.indexOf("内分泌")>=0)
					return true;
				
				if(title.indexOf("女性")>=0)
					return true;
			}
		
			if(feautures != null && feautures.contains("情感") )
			{
				if(title.indexOf("经期")>=0 || title.indexOf("阴道")>=0 
						|| title.indexOf("孕期")>=0 || title.indexOf("怀孕")>=0
						|| title.indexOf("备孕")>=0
						|| title.indexOf("白带")>=0
						|| title.indexOf("私处")>=0)
					return true;
				
				if(title.indexOf("更年期")>=0 
						|| title.indexOf("内分泌")>=0)
					return true;
				
			}
			
		}
		
		
		
		return false;
	}
}
