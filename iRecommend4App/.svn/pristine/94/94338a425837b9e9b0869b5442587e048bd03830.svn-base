/**
 * 
 */
package com.ifeng.iRecommend.front.recommend2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.gson.annotations.Expose;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.dingjw.front_rankModel.RankItem;
import com.ifeng.iRecommend.dingjw.itemParser.Item;

/**
 * <PRE>
 * 作用 : 
 *   前端，投放到user待推荐队列中的item信息；
 *   版本2；
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
 *          1.0          2013-8-14        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class item2distribute2 {
	@Expose
	public String id;//唯一ID编码
	@Expose
	public String title;//标题
	@Expose
	public String url;//url
	@Expose
	public String imageUrl;//图片地址
	@Expose
	public String category;//内容类型,高清图、新闻、视频等
	@Expose
	public String hotLevel;//热度：A-D
	@Expose
	public float  score;//对user的匹配分数；
	
	public RankItem rankItem;
	
	public item2distribute2() {
		id = title = url = imageUrl = category = hotLevel = null;
	}
	
	public item2distribute2(RankItem r_item) {
		// TODO Auto-generated constructor stub
		if(r_item == null)
			return;
		
		rankItem = r_item;
		
		id = rankItem.getOscacheID();
		
		url = r_item.getUrl();
		title = r_item.getTitle();
		
		rankItem = r_item;
		

		hotLevel = r_item.getHotLevel();
		category = r_item.getCategory();
		imageUrl = r_item.getImgurl();
		
		/*
		 * 以下是来计算对外显示的title：
		 * 1)应该优先选择两个标题中编辑给的标题； 
		 * 2)应该优先选择两个标题中有效长度13-23之间的标题；
		 * 3)优先考虑都长标题进行截取；
		 */
		title = new String(r_item.getTitle());
		float f_r_len = commenFuncs.stringLength(title);
		int r_len = Math.round(f_r_len);
		if (r_len >= 13 && r_len <= 23)
			return;
		
//示例
//		舒淇_nr 出席_v 盛典_n 惊艳_nz 全场_n (_w 图_k _w 
//		舒淇_nr 出席_v 盛典_n 惊艳_nz 全场_n [_w 高清_nz 大_k 图_k ]_w  _w 
//		民进党_nz 今_k 讨论_v 对_p 陆_n 政策_n 初稿_n  _w 或_k 将_d 放弃_v “_w 法理_n 台独_n ”_w 
//		民进党_nz 今_k 讨论_v 对_p 陆_n 政策_n 初稿_n  _w 或_k 将_d 放弃_v “_w 法理_n 台独_n			
		// 计算item的title
		String item_title = "";
		Item tmp_item = r_item.getItem();
		if (tmp_item != null && tmp_item.getTitle() != null) {
			item_title = tmp_item.getTitle().replaceAll("_[a-z]{1,2}\\s?", "");
			// (图
			if (item_title.endsWith("(图")) {
				item_title = item_title + ")";
			}
		}
		int i_len = 0;
		if (item_title != null) {
			float f_i_len = commenFuncs.stringLength(item_title);
			i_len = Math.round(f_i_len);
			if (i_len >= 13 && i_len <= 23) {
				title = new String(item_title);
				return;
			}
		}
		//先截取编辑给的title
		if(r_len > 23){
			//（组图） (图)
			title = title.replace("\\(组?图\\)", "");
			title = title.replace("（组?图）", "");
			f_r_len = commenFuncs.stringLength(title);
			r_len = Math.round(f_r_len);
			//先截取下
			if(r_len > 23){
				title = commenFuncs.subString(title, 23);
			}
			return;
		}
		//再截取hbase中title
		if(i_len > 23){
			title = new String(item_title);
			//（组图） (图)
			title = title.replace("\\(组?图\\)", "");
			title = title.replace("（组?图）", "");
			float f_i_len = commenFuncs.stringLength(title);
			i_len = Math.round(f_i_len);
			//先截取下
			if(i_len > 23){
				title = commenFuncs.subString(title, 23);
			}
			return;
		}

		//否则取两个title中最长的
		if(r_len > i_len){
			return;
		}else{
			title = new String(item_title);
			return;
		}
	}
	
	public String toString(){
		StringBuffer sbTmp = new StringBuffer();
		sbTmp.append(id).append("\r\n");
		sbTmp.append(url).append("\r\n");
		sbTmp.append(title).append("\r\n");
		sbTmp.append(this.hotLevel).append("\r\n");
		sbTmp.append(this.score).append("\r\n");
		return sbTmp.toString();
	}
}
