package com.ifeng.iRecommend.likun.locationNews;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

public class locationExtractionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		fieldDicts.locationMapFilePath = "D:\\workspace\\iRecommend4App\\testenv\\locationMap.txt";
	}

	@Test
	public void testExtractLocation() {
		ItemOperation itemOP = ItemOperation.getInstance();
		itemOP.setItemType(ItemType.APPITEM);
		Item oneItem = itemOP.getItem("89734623");
		locationExtraction locE = new locationExtraction();
		String loc = locE.extractLocation(oneItem);
		System.out.println(oneItem.getTitle()+" "+loc);
		//assertEquals(loc,"邯郸市");
		// ////////////
		oneItem = itemOP.getItem("89742289");
		loc = locE.extractLocation(oneItem);
		System.out.println(oneItem.getTitle() + " " + loc);
		//assertEquals(loc, "北京市");
		// ////////////
		oneItem = itemOP.getItem("89765592");
		loc = locE.extractLocation(oneItem);
		System.out.println(oneItem.getTitle() + " " + loc);
		//assertEquals(loc, "");
		/////////
		oneItem = itemOP.getItem("89765590");
		loc = locE.extractLocation(oneItem);
		System.out.println(oneItem.getTitle() + " " + loc);
		//assertEquals(loc, "西安市");
		/////////
		oneItem = itemOP.getItem("89765425");
		loc = locE.extractLocation(oneItem);
		System.out.println(oneItem.getTitle() + " " + loc);
		//assertEquals(loc, "北京市");
		
		/////////
		oneItem = itemOP.getItem("92452205");
		loc = locE.extractLocation(oneItem);
		System.out.println(oneItem.getTitle() + " " + loc);
		//assertEquals(loc, "北京市");
		
		/////////
		oneItem = itemOP.getItem("92449481");
		loc = locE.extractLocation(oneItem);
		System.out.println(oneItem.getTitle() + " " + loc);
		assertEquals(loc, "北京市");
	}

}
