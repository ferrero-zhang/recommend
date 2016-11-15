package com.ifeng.hexl.redis;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.ifeng.commen.redis.JedisInterface;

public class JedisTest
{
	private static final int redisDbNum = 0;
	@Before
	public void setUp() throws Exception
	{
	}

	@Test
	public void test()
	{
		String key = "test";
		String value = "value";
		try
		{
			UsefulKeyJedisInterface.set(key, value, redisDbNum);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		try
		{
			String result = UsefulKeyJedisInterface.get(key, redisDbNum);
			System.out.println("result is "+result);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			long result = UsefulKeyJedisInterface.del(key, redisDbNum);
			System.out.println("result is "+result);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
