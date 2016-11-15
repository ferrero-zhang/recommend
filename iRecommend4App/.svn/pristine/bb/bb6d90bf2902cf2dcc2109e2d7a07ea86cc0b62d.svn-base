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
 *   json item document类；方便放入track算法的接口或者内容池中
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
 *          1.0          2014年12月04日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class itemForTrack {
	//id title segtitle date channel doc_type others hotlevel
	@Expose
	String id;
	@Expose
	String title;
	@Expose
	String segTitle;
	@Expose
	public String date;// item publish date
	@Expose
	public String hotLevel;// 文章热度等级
	@Expose
	public String docChannel;// 内容分类(用于前端多样性)
	@Expose
	public String others;//备注字段
	@Expose
	public String docType;//文章类型 (slide/video/doc/hdSlide)
	
	/*
	 * 构造函数
	 */
	public itemForTrack(RankItem r_item){
		if(r_item == null)
			return;
		id = r_item.getID();
		title = r_item.getTitle();
		segTitle = r_item.getFrontTitleSplited();
		date = r_item.getTimeStamp();
		hotLevel = r_item.getWeight();
		docChannel = r_item.getCategory();
		others = r_item.getOthers();
		docType = r_item.getDocType();

	}
}
