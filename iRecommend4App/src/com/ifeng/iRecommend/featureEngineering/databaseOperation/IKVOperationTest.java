package com.ifeng.iRecommend.featureEngineering.databaseOperation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ifeng.iRecommend.featureEngineering.XMLitemf;
//* 正在使用的IKV Table：  cmppitems    cmpp抓取文章item
//* 						xmlitems     xml同步文章item
//* 						relaids		 客户端相关推荐
//* 						cmppDyn      手凤相关推荐
public class IKVOperationTest {
	IKVOperation ikvop = new IKVOperation("xmlitems"); 
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		String key = "106525920";
//		XMLitemf item = ikvop.queryItemF(key);
	}

}
