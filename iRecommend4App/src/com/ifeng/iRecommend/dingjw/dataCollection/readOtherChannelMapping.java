package com.ifeng.iRecommend.dingjw.dataCollection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class readOtherChannelMapping
{
	private HashMap<String,String> otherChannelMap = new HashMap<String,String>();
	
	public readOtherChannelMapping()
	{
		read();
		
	}
	
	private void read()
	{	
		FileReader reader = null;
		try
		{
			reader = new FileReader("conf/otherChannelMapping.txt");
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(reader);
		String data = null;
		try
		{
			data = br.readLine();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(data != null)
		{
			String[] channel = data.split("\t");
			otherChannelMap.put(channel[0],channel[1]);
			try
			{
				data = br.readLine();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private boolean checkMap()
	{
		if(otherChannelMap.size() <= 1)
		{
			return false;
		}
		else
			return true;
	}
	
	public HashMap<String,String> getOtherChannelMap()
	{
		if(checkMap())
			return this.otherChannelMap;
		else
			return null;
	}
}
