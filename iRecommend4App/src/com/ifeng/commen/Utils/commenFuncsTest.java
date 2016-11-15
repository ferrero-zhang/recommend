package com.ifeng.commen.Utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class commenFuncsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Ignore
	public void test() {
		//fail("Not yet implemented");
	}
	
	@Ignore
	public void testStringLength()
	{
		assertNotNull(commenFuncs.stringLength(null));
		assertNotNull(commenFuncs.stringLength(""));
		assertNotNull(commenFuncs.stringLength("传王健林曾向梦工厂创始人炫耀私人飞机遭对方反击"));
		assertTrue(commenFuncs.stringLength("") == 0.0);
		assertTrue(commenFuncs.stringLength(" ") == 0.5);
		assertTrue(commenFuncs.stringLength("传王健林曾向梦工厂创始人炫耀私人飞机遭对方反击") == 23.0);
		assertTrue(commenFuncs.stringLength("传王健林曾向梦工厂创始人炫耀私人飞机 遭对方反击") == 23.5);
		assertTrue(commenFuncs.stringLength("传王健林曾向梦工厂创始人炫耀私人飞机：遭对方反击！") == 25.0);
		assertTrue(commenFuncs.stringLength("传王健林曾向梦工厂创始人炫耀私人1飞机：遭对方反击！a") == 26.0);
		assertTrue(commenFuncs.stringLength("元配骂小三“贱人”被起诉 法官认为是“实话”判无罪") == 24.5);
		assertTrue(commenFuncs.stringLength("123456789") == 4.5);
		assertTrue(commenFuncs.stringLength("abcdefghi") == 4.5);
		assertTrue(commenFuncs.stringLength(",.:{}[]!%") == 4.5);
		assertTrue(commenFuncs.stringLength("《》，。、！：；【】（）￥？") == 14.0);
	}
	
	@Ignore
	public void testSubTitle()
	{
		assertNull(commenFuncs.subString(null, 1f));
		assertNull(commenFuncs.subString("", -1f));
		assertNotNull(commenFuncs.subString("", 1f));
		assertNotNull(commenFuncs.subString("abc", 0f));
		String title = "2013-11-28凤凰大视野 黑雪1932——台湾人的中国情（四）";
		assertTrue(commenFuncs.stringLength(title) == 25.5);
		String subTitle = commenFuncs.subString(title, 23f);
		System.out.println(subTitle);
		System.out.println(commenFuncs.stringLength(subTitle));
		assertTrue(commenFuncs.stringLength(subTitle) <= 23f);
		subTitle = commenFuncs.subString(title, 26f);
		System.out.println(subTitle);
		System.out.println(commenFuncs.stringLength(subTitle));
		assertTrue(commenFuncs.stringLength(subTitle) == 25.5f);
	}
	
	@Test
	public void testlongToString()
	{
		long num = 0L;
		for(num =0L;num < 100;num++)
			System.out.println(num+":"+commenFuncs.longToString(num));
		
		num = 0L;
		assertTrue(0 == commenFuncs.longToString(num).compareTo("0"));
		num = -1L;
		assertTrue(0 == commenFuncs.longToString(num).compareTo("-1"));
		num = 31L;
		assertTrue(0 == commenFuncs.longToString(num).compareTo("53"));
		num = 35L;
		assertTrue(0 == commenFuncs.longToString(num).compareTo("57"));
		num = 9L;
		assertTrue(0 == commenFuncs.longToString(num).compareTo("9"));
		num = 10L;
		assertTrue(0 == commenFuncs.longToString(num).compareTo(" "));
		num = 26L;
		assertTrue(0 == commenFuncs.longToString(num).compareTo("z"));
		num = 99L;
		assertTrue(0 == commenFuncs.longToString(num).compareTo("y"));
		num = 26L;
		assertTrue(0 == commenFuncs.longToString(num).compareTo("z"));
		num = 131216090935527L;
		assertTrue(0 == commenFuncs.longToString(num).compareTo("{MspR+531"));
		num = 131216090935528L;
		assertFalse(0 == commenFuncs.longToString(num).compareTo("{MspR+531"));
		num = 131206090935527L;
		assertFalse(0 == commenFuncs.longToString(num).compareTo("{MspR+531"));
		num = 131216090835527L;
		assertFalse(0 == commenFuncs.longToString(num).compareTo("{MspR+531"));
		num = 5527L;
		assertFalse(0 == commenFuncs.longToString(num).compareTo("{MspR+531"));
		num = 131216085140541L;
		assertFalse(0 == commenFuncs.longToString(num).compareTo("{MspR+531"));
		System.out.println(num+":"+commenFuncs.longToString(num));
	
		num = System.currentTimeMillis();
		System.out.println(num+":"+commenFuncs.longToString(num));
		num = System.currentTimeMillis();
		System.out.println(num+":"+commenFuncs.longToString(num));
		num = System.currentTimeMillis();
		System.out.println(num+":"+commenFuncs.longToString(num));
		num = System.currentTimeMillis();
		System.out.println(num+":"+commenFuncs.longToString(num));
	
	}
	
}
