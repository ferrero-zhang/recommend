package com.ifeng.iRecommend.usermodel;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ifeng.iRecommend.usermodel.userDoc;

public class userDocTest {
	static userDoc ud;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ud = new userDoc();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testUserDoc() {
		
	}

	@Test
	public void testAddOneTag() {
		ud.addOneTag("fuck", 1.5f);
		assertEquals("fuck fuck",ud.toString());
		
		ud.addOneTag("luck", 1.8f);
		System.out.println(ud.toString());
		
	}

	@Test
	public void testToString() {
		
	}

	@Test
	public void testAdd() {
		HashMap<String,Float> hm_sf = new HashMap<String,Float>();
		ud.add(hm_sf);
		System.out.println(ud.toString());
	}

}
