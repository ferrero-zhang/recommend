/**
 * 
 */
package com.ifeng.iRecommend.usermodel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * <PRE>
 * 作用 : 
 *   进行单个user的抽象描述；
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
 *          1.0          2013-7-22        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class userDoc {
	public String userID;
	
	//记录tag--value对（tag及其tf信息对）
	private HashMap<String,Float> hm_tags;
	
	public userDoc(){
		hm_tags = new HashMap<String,Float>();
		userID = "";
	}
	
	/*
	 * 添加一个tag；
	 */
	public void addOneTag(String tag,Float w){
		if(tag == null || tag.isEmpty())
			return;
		Float w1 = hm_tags.get(tag);
		if(w1 == null)
			w1 = w;
		else
			w1 = w1 + w;
		hm_tags.put(tag, w1);
	}
	
	
	/*
	 * user的document化描述
	 * tag按照w来描述其个数；tag之间以空格分开；
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuffer sbRes = new StringBuffer();
		Iterator<Entry<String, Float>> it = hm_tags.entrySet().iterator();
		int num = 0;
		while(it.hasNext()){
			Entry<String, Float> et = it.next();
			int tagTF = (int) (et.getValue()+0.5f);
			for(int i=0;i<tagTF;i++){
				sbRes.append(et.getKey()).append(" ");
				num++;
				if(num%32 == 0){
					sbRes.append("\n");
				}
			}
			
		}	
		return sbRes.toString().trim();	
	}

	/*
	 * 添加多个tag--value入user doc
	 * @input hm_tagValues：tag--value键值对
	 */
	public void add(HashMap<String, Float> hm_tagValues) {
		// TODO Auto-generated method stub
		if(hm_tagValues == null)
			return;
		Iterator<Entry<String, Float>> it = hm_tagValues.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, Float> et = it.next();
			this.addOneTag(et.getKey(), et.getValue());
		}
	}

}
