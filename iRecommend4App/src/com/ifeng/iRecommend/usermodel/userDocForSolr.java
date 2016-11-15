/**
 * 
 */
package com.ifeng.iRecommend.usermodel;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.annotations.Expose;

/**
 * <PRE>
 * 作用 : 
 *   描述user的doc化数据，以存入远端solr引擎中去
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
 *          1.0          2014年3月5日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
//建模入库的doc向量
public class userDocForSolr{
	@Expose
	String userid;//user标识
	@Expose
	private String topic1;//向量1：逻辑频道层
	@Expose
	private String topic2;//向量2：隐含主题层
	@Expose
	private String topic3;//向量3：主题词层
	@Expose
	private String profile;//user的固态属性比如地域、性别等
	


	//记录tag--value对（tag及其tf信息对）
	private HashMap<String,Float> hm_tags_topic1;
	private HashMap<String,Float> hm_tags_topic2;
	private HashMap<String,Float> hm_tags_topic3;
	
	
	public userDocForSolr(){
		hm_tags_topic1 = new HashMap<String,Float>();
		hm_tags_topic2 = new HashMap<String,Float>();
		hm_tags_topic3 = new HashMap<String,Float>();
		profile = "";
	}
	/*
	 * 添加一个tag；
	 */
	public void addOneTag(String tag,Float w,String topicType){
		if(tag == null || tag.isEmpty())
			return;
		if(topicType == null || topicType.isEmpty())
			return;
		
		HashMap<String, Float> hm_tags = null;
		if(topicType.equals("topic1"))
			hm_tags = hm_tags_topic1;
		else if(topicType.equals("topic2"))
			hm_tags = hm_tags_topic2;
		else if(topicType.equals("topic3"))
			hm_tags = hm_tags_topic3;
		else
			return;
		Float w1 = hm_tags.get(tag);
		if(w1 == null)
			w1 = w;
		else
			w1 = w1 + w;
		hm_tags.put(tag, w1);
	}

	/*
	 * 添加多个tag--value入user doc
	 * @input hm_tagValues：tag--value键值对
	 */
	public void add(HashMap<String, Float> hm_tagValues,String topicType) {
		// TODO Auto-generated method stub
		if(hm_tagValues == null)
			return;
		if(topicType == null || topicType.isEmpty())
			return;
		
		Iterator<Entry<String, Float>> it = hm_tagValues.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, Float> et = it.next();
			this.addOneTag(et.getKey(), et.getValue(),topicType);
		}
	}
	
	public String cmpTopic1(){
		return turnVectorToDoc(hm_tags_topic1);
	}
	
	public String cmpTopic2(){
		return turnVectorToDoc(hm_tags_topic2);
	}
	
	public String cmpTopic3(){
		return turnVectorToDoc(hm_tags_topic3);
	}
	
	public void turnVectorToDocs(){
		topic1 = turnVectorToDoc(hm_tags_topic1);
		topic2 = turnVectorToDoc(hm_tags_topic2);
		topic3 = sortAndDFiltTopic3(hm_tags_topic3,150);
	}
	
	/*
	 * user vector的document化描述
	 * tag按照w来描述其个数；tag之间以空格分开；
	 * 对topic3，按tf排序并选取权重高的前100个写入
	 * (non-Javadoc)
	 */
	private static String sortAndDFiltTopic3(HashMap<String, Float> hm_tags,int maxNum){
		if(hm_tags == null || hm_tags.isEmpty())
			return "";
		
		LinkedList<Map.Entry<String, Float>> list = new LinkedList<Map.Entry<String, Float>>();
		list.addAll(hm_tags.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
			public int compare(Map.Entry<String, Float> obj1,
					Map.Entry<String, Float> obj2) {// 从高往低排序
				if (obj1.getValue() < obj2.getValue())
					return 1;
				
				if (obj1.getValue() > obj2.getValue())
					return -1;
				
				return 0;
			}
		});

		//遍历得到结果
		StringBuffer sbRes = new StringBuffer();
		int num = 0;
		for (Iterator<Map.Entry<String, Float>> ite = list.iterator(); ite
				.hasNext();) {
			Map.Entry<String, Float> et = ite.next();
			if (et.getKey().matches("[\\pP+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]"))
				continue;
			int tagTF = (int) (et.getValue()+0.5f);
			for(int i=0;i<tagTF;i++){
				sbRes.append(et.getKey()).append(" ");
			}
		
			maxNum--;
			if(maxNum < 0)
				break;
		}
		
		return sbRes.toString().trim();	
	}
	
	/*
	 * user vector的document化描述
	 * tag按照w来描述其个数；tag之间以空格分开；
	 * 对topic3，按tf排序并选取权重高的前maxNum个写入
	 * (non-Javadoc)
	 */
	private static String turnVectorToDoc(HashMap<String, Float> hm_tags){
		if(hm_tags == null || hm_tags.isEmpty())
			return "";
		
//		LinkedList<Map.Entry<String, Float>> list = new LinkedList<Map.Entry<String, Float>>();
//		list.addAll(hm_tags.entrySet());
//
//		Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
//			public int compare(Map.Entry<String, Float> obj1,
//					Map.Entry<String, Float> obj2) {// 从高往低排序
//				if (obj1.getValue() < obj2.getValue())
//					return 1;
//				
//				if (obj1.getValue() >= obj2.getValue())
//					return -1;
//				
//				return 0;
//			}
//		});
		
		StringBuffer sbRes = new StringBuffer();
		int num = 0;
		Iterator<Entry<String, Float>> it = hm_tags.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, Float> et = it.next();
			int tagTF = (int) (et.getValue()+0.5f);
			for(int i=0;i<=tagTF;i++){
				sbRes.append(et.getKey()).append(" ");
			}
			
//			maxNum--;
//			if(maxNum < 0)
//				break;
			
		}	
		return sbRes.toString().trim();	
	}
	
	/*
	 * 补充增量到历史记录,注意进行tag合并
	 */
	public void addMoreToAll(String topic,String topicType){
		if(topic == null || topicType == null
				|| topic.isEmpty()){
			return;
		}
		
		HashMap<String,Float> hm_tags_topic = null;
		if(topicType.equals("topic1")){
			hm_tags_topic = hm_tags_topic1;
		}
		if(topicType.equals("topic2")){
			hm_tags_topic = hm_tags_topic2;
		}
		if(topicType.equals("topic3")){
			hm_tags_topic = hm_tags_topic3;
		}
		if(hm_tags_topic == null)
			return;
		String[] secs = topic.split("\\s");
		for(String word:secs){
			Float w = hm_tags_topic.get(word);
			if(w == null)
				w = 1.0f;
			else
				w++;
			hm_tags_topic.put(word, w);
		}
		
	}
	
	public String getTopic1(){
		return topic1;
	}
	public String getTopic2(){
		return topic2;
	}
	public String getTopic3(){
		return topic3;
	}
	
	public void setTopic1(String topic1In){
		topic1 = topic1In;
	}
	
	public void setTopic2(String topic1In){
		topic2 = topic1In;
	}
	
	public String getProfile() {
		return profile;
	}
	public void setProfile(String profile) {
		this.profile = profile;
	}
}
