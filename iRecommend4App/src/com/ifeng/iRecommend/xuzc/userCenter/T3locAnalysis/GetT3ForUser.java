/**
 * 
 */
package com.ifeng.iRecommend.xuzc.userCenter.T3locAnalysis;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.ifeng.iRecommend.xuzc.userCenter.FileUtil;
import com.ifeng.iRecommend.xuzc.userCenter.util.UserModelUtil;


/**
 * <PRE>
 * 作用 : 
 *   
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
 *          1.0          2016-7-11        xuzc          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class GetT3ForUser {	
	static{
		UserModelUtil.redisInit();
	}
	public static void main(String[] args) {
		File file = new File(args[0]);
		LineIterator it = null;
		StringBuilder sb = new StringBuilder();
		FileUtil fu = new FileUtil();
		try {
			it = FileUtils.lineIterator(file);
			int count = 0;
			while(it.hasNext()){
				String readline = it.next();
				String[] line = readline.split("\t");
				String T3 = null;
				try {
					T3 = UserModelUtil.getT3FromRedis(line[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(!"null".equals(T3)&&null != T3){
					count++;
					sb.append(line[0]).append("\t").append(T3).append("\r\n");							
					if(count%10000==0){
						System.out.println(T3);
						fu.writeToFile(sb.toString(), args[1]);
						sb = new StringBuilder();
					}
				}
			}
			fu.writeToFile(sb.toString(), args[1]);
		}catch (Exception e) {
			e.printStackTrace();
		}

	}

}
