/**
 * 
 */
package com.ifeng.iRecommend.front.recommend2;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

import com.google.gson.annotations.Expose;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.k2v;
import com.ifeng.iRecommend.dingjw.front_rankModel.RankItem;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.front.recommend2.recommend4HeadLine.item2app;
import com.ifeng.iRecommend.usermodel.itemAbstraction;

/**
 * <PRE>
 * 作用 : 
 *   json item document类；方便放入solr的接口中
 *   内容匹配算法的item库用；
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
 *          1.0          2014年3月10日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

class itemSolrDoc{
	@Expose
	String itemid;//
	@Expose
	String topic1;
	@Expose
	String topic2;
	@Expose
	String topic3;
	@Expose
	String item2app;
}

public class addItem {
	@Expose
	float boost;
	@Expose
	itemSolrDoc doc;
	
	/*
	 * 很重的构造函数，执行逻辑包括了：生成topic三层向量、生成item的当前weight
	 */
	public addItem(RankItem r_item){
		if(r_item == null)
			return;
		doc = new itemSolrDoc();
		//用oscacheID做后台存储中的关键key,可能是imcp_id，或者url
		doc.itemid = r_item.getID();
		cmpItemVector(r_item);
		boost = 1.0f;

	}
	
	/**
	 * 计算item的向量表达
	 * <p>
	 * 对item，计算其抽象表达；按一定规则，合并所有item的抽象表达，形成一个统一抽象；并存入topic1、topic2、topic3
	 * </p>
	 * 
	 * @param r_item
	 *            item的r_item表达
	 */
	public void cmpItemVector(RankItem r_item) {
		if(r_item == null)
			return;
		Item item = r_item.getItem();
		if(item == null)
		{
			item = new Item();
		}

		//url和title补充修正
		if(item.getTitle() == null
				||item.getTitle().isEmpty())
		{
			item.setTitle("");
			item.setUrl(r_item.getUrl());
		}	
		
		
		//A理论上要投放给所有用户
		if(r_item.getWeight().equals("A"))
		{
			
		}else{
			//补充成一个综合的title进行匹配计算
			if(r_item.getFrontTitleSplited() != null
					&& (false == r_item.getFrontTitleSplited().isEmpty())){
				String title = item.getTitle()+" "+r_item.getFrontTitleSplited();	
				item.setTitle(title);
			}
		}


		// 得到channels信息
		HashMap<String, Float> hm_tagValues = itemAbstraction
				.cmpChannels(r_item,item.getUrl());
		if (hm_tagValues != null) {
			this.doc.topic1 = turnVectorToDoc(hm_tagValues);
		}else{
			this.doc.topic1 = "";
		}

		// 得到top tags
		hm_tagValues = itemAbstraction.getItemTopWords(item, false);
		if (hm_tagValues != null) {
			this.doc.topic3 = turnVectorToDoc(hm_tagValues);
		}else{
			this.doc.topic3 = "";
		}

		// 得到隐含主题
		hm_tagValues = itemAbstraction.cmpLatentTopics(hm_tagValues);
		if (hm_tagValues != null) {
			this.doc.topic2 = turnVectorToDoc(hm_tagValues);
		}else{
			this.doc.topic2 = "";
		}
	}
	
	/*
	 * vector的document化描述
	 * tag按照w来描述其个数；tag之间以空格分开；
	 * (non-Javadoc)
	 */
	private static String turnVectorToDoc(HashMap<String, Float> hm_tags){
		if(hm_tags == null)
			return "";
		StringBuffer sbRes = new StringBuffer();
		Iterator<Entry<String, Float>> it = hm_tags.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, Float> et = it.next();
			int tagTF = (int) (et.getValue()+0.5f);
			for(int i=0;i<tagTF;i++){
				sbRes.append(et.getKey()).append(" ");
			}
			
		}	
		return sbRes.toString().trim();	
	}
}
