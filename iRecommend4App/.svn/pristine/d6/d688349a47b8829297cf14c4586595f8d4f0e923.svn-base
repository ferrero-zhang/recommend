package com.ifeng.iRecommend.featureEngineering;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TagsQueryInterfaceTest
{
	TagsQueryInterface instance = TagsQueryInterface.getInstance();
	@Before
	public void setUp() throws Exception
	{
	}

	@Ignore
	public void testGetInstance()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testQueryTagType()
	{
		String type = instance.queryTagType("中国足球");
		System.out.println(type);
	}
	
	@Ignore
	public void testAdd()
	{
		instance.add("test", "cn");
	}

	@Ignore
	public void testSet()
	{
		instance.set("test", "cn");
	}

	@Ignore
	public void testDel()
	{
		instance.del("test");
	}

	@Ignore
	public void testGet()
	{
		fail("Not yet implemented");
	}
//	@Ignore
//	public void testWrite() throws IOException
//	{
//		FileReader fr = null;
//		fr = new FileReader("D:\\data\\entytyword.txt");
//		
//		BufferedReader br = new BufferedReader(fr);
//		String s = null;
//		while((s = br.readLine()) != null)
//		{
//			if(s.contains("="))
//			{
//				String[] sarray = s.split("=");
//				instance.set(sarray[1], sarray[0]);
//			}
//			else
//				continue;
//		}
//		br.close();
//		fr.close();
//	}
}
