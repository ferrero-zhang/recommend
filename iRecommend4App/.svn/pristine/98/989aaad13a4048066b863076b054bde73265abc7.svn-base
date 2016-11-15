/**
 * 
 */
package com.ifeng.iRecommend.likun.rankModel;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.HttpRequest;
import com.ifeng.commen.Utils.commenFuncs;

/**
 * <PRE>
 * 作用 : 
 *   定期手动处理那些依然存活但是其实生命周期已过的item
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   直接从solr中del，不是改成available=false，为简单方便
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016年3月15日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class dealItemsOutOfLife {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//读入待处理xml列表
		//取出item id
		//在solr中将其del，available变成false
		FileUtil fu = new FileUtil();
		fu.Initialize(
				"C:\\Users\\likun\\Desktop\\debug\\itemNotInLifeTimeNeedDel.txt",
				"utf-8");
	
		String sline = "";
		while ((sline = fu.ReadLine()) != null) {
			String line = sline.trim();
			int b = line.indexOf("itemid\">");
			if(b > 0){
				b = b + 8;
				int e = line.indexOf("</str>",b);
				if(e > b){
					String itemid = line.substring(b,e);
					dealItem(itemid);
				}
			}
		}
		
		
		
		
	}

	
	
	//处理过期item
	private static void dealItem(String itemid) {
		// TODO Auto-generated method stub
		if(itemid == null || itemid.isEmpty())
			return;
		
		String sCmd = new String("{\"delete\":{\"id\":\"z0\"}}");
		sCmd = sCmd.replace("z0",itemid);
		try {
			// 建模写入后台存储和计算引擎;构建json数据
			String rt = HttpRequest.sendPost(
					"http://10.32.28.119:8081/solr46/item/update/json",
					sCmd);
			// test
			if (rt.indexOf("failed") > 0) {
				System.out.println("send post failed,del:" + sCmd + " rt=" + rt);
				return;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("send post failed"+e.toString());
			return;
		}
		// @test
		System.out.println("success,  del i2a:" + itemid);
	
	}

}
