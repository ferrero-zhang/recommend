package com.ifeng.iRecommend.zhanzh.Utils;

import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <PRE>
 * 作用 : 
 *   自创的快速近似相似度算法,思路是取相邻词,算交集，求相似度
 *   example: 哈尔滨红肠--> [哈尔，尔滨，滨红，红肠]
 *            哈滨红肠-->[哈滨，滨红，红肠]
 *   适用场景：标题近似排重，标题间的微小差异，比如主体结构相同，但相差几个字
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
 *          1.0          2014-7-30        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class AdjStringsIsSim {
	private static final Log log = LogFactory.getLog(AdjStringsIsSim.class);
			
	/**
	 * @Fields simThreshold : 相似度阈值
	 */
	private static double simThreshold = 0.5; // 0.35;
	
	/**
	 * @Title: isSimStr
	 * @Description: 是否是相似tilte
	 * @author liu_yi
	 * @param strLeft
	 * @param strRight
	 * @return
	 * @throws
	 */
	public static boolean isSimStr(String strLeft, String strRight) {
		if (null == strLeft || strLeft.isEmpty() ||
			null == strRight || strRight.isEmpty()) {
			return false;
		}
		
		if (compareStrings(strLeft, strRight) >= simThreshold) {
			return true;
		} else {
			return false;
		}
	}
		
	private static String[] getAdjWordPair(String str) {
		int numPairs = str.length() - 1;
		String[] adjWords = new String[numPairs];
		for (int i = 0; i < numPairs; i++) {
			adjWords[i] = str.substring(i, i + 2);
		}
		return adjWords;
	}
	
	public static double compareStrings(String strLeft, String strRight) {
		String[] adjWordsLeft = getAdjWordPair(strLeft);
		String[] adjWordsRight = getAdjWordPair(strRight);
		
		int intersection = 0;
		int union = adjWordsLeft.length + adjWordsRight.length;
			
		for (int i = 0; i < adjWordsLeft.length; i++) {
			String pair1 = adjWordsLeft[i];

			for (int j = 0; j < adjWordsRight.length; j++) {
				String pair2 = adjWordsRight[j];
				if (pair1.equals(pair2)) {

					intersection++;
					break;
				}
			}
		}

		return (2.0 * intersection) / union;
	}
	
	public static double newCompareStrings(String strLeft,String strRight){
		HashSet<String> tempStr = new HashSet<String>();
		int leftLength =0,rightLength = 0,intersection=0;
		int numPairs = strLeft.length() - 1;
		for (int i = 0; i < numPairs; i++) {
			tempStr.add(strLeft.substring(i, i + 2));
			leftLength++;
		}
		
		numPairs = strRight.length() - 1;
		for (int i = 0; i < numPairs; i++) {

			if(tempStr.contains(strRight.substring(i, i + 2))){
				intersection++;

			}
			rightLength++;
		}
		int union = leftLength+rightLength;

		return (2.000 * intersection) / union;
	}
	
	public static float simRate(String str1, String str2) {
		// TODO Auto-generated method stub
		if (str1 == null || str2 == null)
			return -1;

		int len1 = str1.length();
		int len2 = str2.length();
		HashSet<String> hm_tmp = new HashSet<String>();
		int combineNum = 0;
		for (char s : str1.toCharArray()) {
			hm_tmp.add(s+"");
		}
		for (char s : str2.toCharArray()) {
			if (hm_tmp.contains(s+""))
				combineNum++;
		}

		int maxLen = (len1 >= len2) ? len1 : len2;
		if (maxLen == 0)
			return 1;
		return (combineNum / (float) maxLen);
	}
	
	//计算编辑距离
	public static float levenshteinDistance(String left,String right){
		int leftLen = left.length();
		int rightLen = right.length();
		int matrix[][] = new int[rightLen+1][leftLen+1];
		//初始化矩阵
		for(int i = 0;i<=leftLen;i++){
			matrix[0][i] = i;
		}
		for(int j = 0;j<=rightLen;j++){
			matrix[j][0] = j;
		}
		//计算
		int tempResult = 0;
		for(int i =1;i<=leftLen;i++){
			for(int j=1;j<=rightLen;j++){
				int upnum = matrix[j-1][i]+1;
				int leftnum = matrix[j][i-1]+1;
				int leftupnum = matrix[j-1][i-1];
				if(left.charAt(i-1)!=right.charAt(j-1)){
					leftupnum++;
				}
				tempResult = findMinNum(upnum,leftnum,leftupnum);
				matrix[j][i] = tempResult;
			}
		}
//		for(int j=0;j<=rightLen;j++){
//			for(int i=0 ; i<=leftLen ; i++){
//				System.out.print(matrix[j][i]+"	");
//			}
//			System.out.println();
//		}
//		System.out.println(Math.max(leftLen, rightLen));

		float similarity = 1-(float)matrix[rightLen][leftLen]/Math.max(leftLen, rightLen);
		return similarity;
	}
	private static int findMinNum(int ...input){
		int min = Integer.MAX_VALUE;
		for(int i : input){
			if(min>i){
				min = i;
			}
		}
		return min;
	}
	
	
	public static void main(String[] args) {
//		float a = levenshteinDistance("你听说要放假了", "听说马上就要放假了");
//		System.out.println(a);
		
		
		String test1 = "2016国家公务员考试行测答题技巧：资料分析考查趋势分析";
		String test2 = "2016年国家公务员考试行测部分：数量关系与资料分析考查题型变化";
		double result = 0.0;
		long startMili = System.currentTimeMillis();
		for (int i = 0; i != 50000; i++) {
			result = compareStrings(test1, test2);
		}
//		String sub = test1.substring(0,2);
//		System.out.println(sub);
		long endMili = System.currentTimeMillis();
		System.out.println("总耗时为："+(endMili-startMili)+"毫秒");
		System.out.println(compareStrings(test1, test2));
		
		//test newCompareStrings
		startMili = System.currentTimeMillis();
		for (int i = 0; i != 50000; i++) {
			result = newCompareStrings(test1, test2);
		}
		endMili = System.currentTimeMillis();
		System.out.println("new method 总耗时： "+(endMili-startMili)+"毫秒");
		System.out.println(newCompareStrings(test1, test2));
		
		//test simRate
		startMili = System.currentTimeMillis();
		for (int i = 0; i != 50000; i++) {
			result = simRate(test1, test2);
		}
		endMili = System.currentTimeMillis();
		System.out.println("simRate 总耗时： "+(endMili-startMili)+"毫秒");
		System.out.println(simRate(test1, test2));
		
		//test levenshteinDistance
		startMili = System.currentTimeMillis();
		for (int i = 0; i != 50000; i++) {
			result = levenshteinDistance(test1, test2);
		}
		endMili = System.currentTimeMillis();
		System.out.println("new method 总耗时： "+(endMili-startMili)+"毫秒");
		System.out.println(levenshteinDistance(test1, test2));
	}
}