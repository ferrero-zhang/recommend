/**
 * 
 */
package com.ifeng.commen.Utils;

/**
 * <PRE>
 * 作用 : 
 *   string为key类型，value可以泛型的一个二元key-value数据结构
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
 *          1.0          2013-7-29        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class k2v<T> {
	private String k;
	private T t;
	
	/**
	 * 
	 */
	public k2v(String k_in,T t_in) {
		// TODO Auto-generated constructor stub
		k = k_in;
		t = t_in;
	}

	public String getK(){return k;}
	public T getV(){return t;}
	
}
