package com.ifeng.hexl.redis;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.hexl.redis.GetUsefulKeyFromRedis.KeyWord;
import com.ifeng.iRecommend.featureEngineering.CMPPItemToIKVAndRedis;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
/**
 * 
 * <PRE>
 * 作用 : 向redis库中写入特征词
 *   
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 * 	 
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015-8-12         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class UsefulKeyToRedis
{
	static Logger LOG = Logger.getLogger(UsefulKeyToRedis.class);
//	String ip = "10.32.24.194",port = "6379";
	class KeyWord{
		@Expose
		@SerializedName("name")
		String name;
		@Expose
		@SerializedName("type")
		String type;
		@Expose
		@SerializedName("hitDocNum")
		int hitDocNum;
		@Expose
		@SerializedName("date")
		String date;
		@Expose
		@SerializedName("isAvailable")
		boolean isAvailable;
		@Expose
		@SerializedName("state")
		int state;
	}
	/**
	 * 
	 * @param featureList
	 * @param publishedTime
	 */
	public void keyToRedis(ArrayList<String> featureList,String publishedTime)
	{
		if(featureList == null || featureList.size() < 3)
			return;
		String date = publishedTime;
		Jedis usefulJedis = new Jedis("10.32.24.194", 6379);
		try {
			usefulJedis.select(0);
		} catch (Exception e) {
			usefulJedis.close();
		}
		for(int j = 0; j < featureList.size() - 2; j = j + 3)
		{
			String name = featureList.get(j);
			String type = featureList.get(j + 1);
			if(type.equals("c")||type.equals("sc")||type.equals("cn")||type.equals("s")||
					   type.equals("s1")||type.equals("e")||type.equals("t")||type.equals("et")||type.equals("loc")||
					   type.equals("kb")||type.equals("kq")||type.equals("ks")||type.equals("x")||type.equals("nr")||
					   type.equals("nt")||type.equals("nz")||type.equals("n")||type.equals("ns")||type.equals("ne")||type.equals("swm"))
			{
				KeyWord keyword = new KeyWord();
				keyword.name = name;
				keyword.type = type;
				keyword.date = date;
				String value = JsonUtils.toJson(keyword, KeyWord.class);
				try
				{
					String savedvalue = usefulJedis.get(name);
					if(savedvalue != null)
					{
						KeyWord savedkeyword = JsonUtils.fromJson(savedvalue, KeyWord.class);
						if(savedkeyword != null && savedkeyword.type!=null && savedkeyword.type.equals("loc"))
							continue;
					}
					usefulJedis.set(name, value);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		usefulJedis.close();
		LOG.info("Set "+featureList.size()+" words into useful redis db 0.");
	}
		
public static void main(String[] args)
{
	UsefulKeyToRedis uk =new UsefulKeyToRedis();
//	uk.process();
	ArrayList<String> list = new ArrayList<String>();
	String publishedTime = "2016-01-19 13:52:55";
	list.add("北京市");
	list.add("et");
	list.add("1");
//	String savedvalue = UsefulKeyJedisInterface.get("北京市", redisDbNum);
//	if(savedvalue != null)
//	{
//		KeyWord savedkeyword = JsonUtils.fromJson(savedvalue, KeyWord.class);
//		if(savedkeyword.type.equals("loc"))
//			System.out.println("continue");
//	}
	uk.keyToRedis(list,publishedTime);
//	String savedvalue = UsefulKeyJedisInterface.get("北京市", redisDbNum);
//	System.out.println(savedvalue);
}
	
}
