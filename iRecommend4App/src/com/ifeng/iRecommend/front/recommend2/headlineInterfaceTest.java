package com.ifeng.iRecommend.front.recommend2;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.front.recommend2.headlineInterface.responseData;

public class headlineInterfaceTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testGuessYouLike() {
		String userid = "A000004206BD05";
		int askNum = 1000;
		headlineInterface qi4f = new headlineInterface();
		responseData rt = null;
//		rt = qi4f.guessYouLike(userid, askNum, 0.5f);
//		System.out.println(JsonUtils.toJson(rt));
		
		userid = "";
		askNum = 1;
		rt = qi4f.guessYouLike(userid, askNum,0f);
		assertEquals(null,rt);
		
		userid = " ";
		askNum = 1;
		rt = qi4f.guessYouLike(userid, askNum,0f);
		assertEquals(userid,rt.userID);
		
		userid = null;
		askNum = 1;
		rt = qi4f.guessYouLike(userid, askNum,0f);
		assertEquals(null,rt);
		
	}

}
