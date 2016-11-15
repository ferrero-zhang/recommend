package com.ifeng.hexl.redis;

import java.util.ArrayList;

import redis.clients.jedis.Jedis;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
/**
 * 
 * <PRE>
 * 作用 : 从redis库中获取词的可用评价
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
 *          1.0          2015-8-8         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class GetUsefulKeyFromRedis
{
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
	 * @param name
	 * @return
	 */
	public static int GetUsefulFlag(String name)
	{
		Jedis usefulJedis = new Jedis("10.32.24.194", 6379);
		try {
			usefulJedis.select(1);
		} catch (Exception e) {
			usefulJedis.close();
		}
		String value = usefulJedis.get(name);
		usefulJedis.close();
		KeyWord keyword = JsonUtils.fromJson(value, KeyWord.class);
		if(keyword == null)
		{
			return 99;
//			LOG.info("There is no this key "+ name);
		}
		if(keyword.isAvailable)
			return 1;
		else
			return 0;
	}
	/**
	 * 
	 * @param featureList
	 * @return
	 */
	public static ArrayList<String> featureUsefulCheck(ArrayList<String> featureList) {
		if(featureList == null || featureList.isEmpty())
			return null;
		for(int i = 0; i < featureList.size() - 2; i = i + 3)
		{
			String name = featureList.get(i);
			String type = featureList.get(i + 1);
			double weight = Double.valueOf(featureList.get(i + 2));
			if(type.equals("c")||type.equals("sc")||type.equals("cn")||type.equals("s")||
			   type.equals("s1")||type.equals("e")||type.equals("t")||type.equals("et")||type.equals("lc")||
			   type.equals("kb")||type.equals("kq")||type.equals("ks")||type.equals("x")||type.equals("nr")||
			   type.equals("nt")||type.equals("nz")||type.equals("n")||type.equals("ns")||type.equals("ne"))
			{
				int flag = GetUsefulKeyFromRedis.GetUsefulFlag(name);
				if(flag != 1)
					featureList.set(i + 2, String.valueOf(weight + 100));
			}
		}
		return featureList;
	}
	/**
	 * 检查这个词是不是自媒体稿源
	 * @param source
	 * @return
	 */
	public static boolean wemediaCheck(String source){
		if(null == source || source.isEmpty())
			return false;
		Jedis usefulJedis = new Jedis("10.32.24.194", 6379);
		try {
			usefulJedis.select(1);
		} catch (Exception e) {
			usefulJedis.close();
		}
		String value = usefulJedis.get(source);
		usefulJedis.close();
		KeyWord keyword = JsonUtils.fromJson(value, KeyWord.class);
		if(keyword!=null && keyword.type.equals("swm"))
			return true;
		return false;
	}
}
