package com.ifeng.iRecommend.usermodel;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ifeng.commen.Utils.JsonUtils;

public class queryCmppItemTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testGetImcpID() {
		queryCmppItem instance = queryCmppItem.getInstance();
		assertEquals(null,instance.getImcpID(null,null));
		assertEquals(null,instance.getImcpID("",null));
		assertEquals(null,instance.getImcpID(null,""));
		assertEquals(null,instance.getImcpID("",""));
		assertEquals(null,instance.getImcpID("afaf","ent"));
		System.out.println(instance.getImcpID("42495038","ent"));
		System.out.println(instance.getImcpID("42494589","ent"));
		System.out.println(JsonUtils.toJson(instance.getItemF("42494589","ent")));
		System.out.println(JsonUtils.toJson(instance.getItemF("42495038","ent")));
	}

}
