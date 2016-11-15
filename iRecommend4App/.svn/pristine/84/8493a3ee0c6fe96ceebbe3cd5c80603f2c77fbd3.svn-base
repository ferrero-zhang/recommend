package com.ifeng.iRecommend.front.recommend2;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.dingjw.front_rankModel.RankItem;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.front.recommend2.recommend4HeadLine.item2app;
import com.ifeng.iRecommend.front.recommend2.recommend4HeadLine.responseData;

public class recommend4HeadLineTest {
	public recommend4HeadLine r4h = null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		fieldDicts.itemModelPath = "D:/workspace/iRecommend4App/testenv/itemmodel/";
		fieldDicts.modelPath = "D:/workspace/iRecommend4App/testenv/usermodel/";
		fieldDicts.appTreeMappingFile = "D:/workspace/iRecommend4App/testenv/AppTreeMapping.txt";
		fieldDicts.frontAppTreeMappingFile = "D:/workspace/iRecommend4App/testenv/Front_TreeMapping.txt";
		fieldDicts.stopwordsFile = "D:/workspace/iRecommend4App/testenv/stopwords.txt";
		fieldDicts.tm_doc_dir = "D:\\workspace\\iRecommend4App\\testenv\\tm\\doc\\";
		fieldDicts.tm_word_dir = "D:\\workspace\\iRecommend4App\\testenv\\tm\\word\\";
		fieldDicts.tm_words_file = "D:\\workspace\\iRecommend4App\\testenv\\tm\\dict_topicmodel";
		
		
	}

	@Ignore
	public void testSave2Index() {
		r4h = new recommend4HeadLine();
		String imcp_id = "82987603";
		//query检验
		item2app i2a = r4h.testQueryItemID(imcp_id);
		assertNotNull(null,i2a);
		
		ItemOperation itemOP = new ItemOperation();
		itemOP.setItemType(ItemType.APPITEM);
		
		Item item = itemOP.getItem(imcp_id);
		if (item == null || item.getTitle() == null){
			return;
		}
		RankItem r_item = new RankItem(item);
		r_item.setDocType("slide");
		r4h.save2Index(r_item, "add");
		
		
		imcp_id = "82139526";
		item = itemOP.getItem(imcp_id);
		if (item == null || item.getTitle() == null){
			return;
		}
		r_item = new RankItem(item);
		r4h.save2Index(r_item, "add");
		
		imcp_id = "82105418";
		item = itemOP.getItem(imcp_id);
		if (item == null || item.getTitle() == null){
			return;
		}
		r_item = new RankItem(item);
		r4h.save2Index(r_item, "add");
		
		imcp_id = "82266045";
		item = itemOP.getItem(imcp_id);
		if (item == null || item.getTitle() == null){
			return;
		}
		r_item = new RankItem(item);
		r4h.save2Index(r_item, "add");
		
		
//		//query检验
//		item2app i2a = r4h.testQueryItemID(imcp_id);
//		assertEquals(imcp_id,i2a.docId);
//		
//		//query检验
//		r4h.reload();
//		i2a = r4h.testQueryItemID(imcp_id);
//		assertNull(null,i2a);
	}

	@Ignore
	public void testSave2IndexSolr() {
		r4h = new recommend4HeadLine();
		String imcp_id = "86520245";
		
		ItemOperation itemOP = new ItemOperation();
		itemOP.setItemType(ItemType.APPITEM);
		
		Item item = itemOP.getItem(imcp_id);
		if (item == null || item.getTitle() == null){
			return;
		}
		RankItem r_item = new RankItem(item);
		r_item.setDocType("slide");
		r4h.save2IndexSolr(r_item, "add");
		r4h.commit2Solr();
		System.out.println(r4h.testQueryItemIDSolr(imcp_id));
		
		imcp_id = "82139526";
		item = itemOP.getItem(imcp_id);
		if (item == null || item.getTitle() == null){
			return;
		}
		r_item = new RankItem(item);
		r_item.setDocType("doc");
		r4h.save2IndexSolr(r_item, "add");
		r4h.commit2Solr();
		System.out.println(r4h.testQueryItemIDSolr(imcp_id));
	
		
		imcp_id = "82105418";
		item = itemOP.getItem(imcp_id);
		if (item == null || item.getTitle() == null){
			return;
		}
		r_item = new RankItem(item);
		r_item.setDocType("doc");
		r4h.save2IndexSolr(r_item, "add");
		r4h.commit2Solr();
		System.out.println(r4h.testQueryItemIDSolr(imcp_id));

		imcp_id = "82266045";
		item = itemOP.getItem(imcp_id);
		if (item == null || item.getTitle() == null){
			return;
		}
		r_item = new RankItem(item);
		r_item.setDocType("doc");
		r4h.save2IndexSolr(r_item, "add");
		r4h.commit2Solr();
		System.out.println(r4h.testQueryItemIDSolr(imcp_id));
////		//query检验
////		item2app i2a = r4h.testQueryItemID(imcp_id);
////		assertEquals(imcp_id,i2a.docId);
////		
////		//query检验
////		r4h.reload();
////		i2a = r4h.testQueryItemID(imcp_id);
////		assertNull(null,i2a);
	}
	
	@Ignore
	public void testcmpTop100DocsInLucene() {
		fail("Not yet implemented");
	}

	@Test
	public void testGiveMeNews() {
		r4h = new recommend4HeadLine();
		String userid = "A000004206BD05";//A000004206BD05";//"coldstartusers";
//		responseData res = r4h.giveMeNews(userid);
//		System.out.println(JsonUtils.toJson(res));
		Jedis jedis = new Jedis("10.32.21.62",6379,10000);
		jedis.select(6);
		System.out.println(jedis.get(userid));
	}

}
