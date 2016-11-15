/**
 * 
 */
package com.ifeng.myClassifier;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

/**
 * <PRE>
 * 作用 : 
 *   word分布统计条件概率
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   条件概率，意味着可能多个父节点传递到一个子节点，<父节点，子节点，条件概率>这样才是一个统计分布
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016年3月8日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class wordDis {
	String pa_claName;//refer节点，或者叫父节点
	@Expose
	String classname;
	@Expose
	String cla_type;// c0/c1/sc...
	@Expose
	int df;//父节点是pa_claName情况下，class中word中的df分布
	@Expose
	float p;//父节点是pa_claName情况下，class在word中分布概率(注意一定是父子传递的条件概率)
	@Expose
	float pa_p;//父节点本身的概率
	
	public String toString(){
		return classname+"|"+pa_claName+","+cla_type+","+p+","+pa_p+","+df;
	}
}
