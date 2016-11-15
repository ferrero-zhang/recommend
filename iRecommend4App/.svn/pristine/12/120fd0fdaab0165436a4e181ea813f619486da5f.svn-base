package com.ifeng.iRecommend.dingjw.front_rankModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ifeng.commen.Utils.JsonUtils;

/**
 * <PRE>
 * 作用 : 
 *   前端文章类型，保存文章类型、url、标题等信息
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
 *          1.0          2013-08-06        lidm          change
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class itemFront {
	@Expose
	@SerializedName("category")
	private String category;
	
	@Expose
	@SerializedName("url")
	private String url;
	
	@Expose
	@SerializedName("title")
	private String title;
	
	@Expose
	@SerializedName("weight")
	private String weight;//来自外部的hot level
	
	@Expose
	@SerializedName("imgUrl")
	private String imgUrl;
	
	@Expose
	@SerializedName("timeStamp")
	private String timeStamp;//来自外部的时间；
	
	@Expose
	@SerializedName("pv")
	private Integer pv;//文章的每分钟实时浏览量
	
	@Expose
	@SerializedName("expiry_date")
	private String expiry_date;//保存时间,以秒为单位

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getImgurl() {
		return imgUrl;
	}

	public void setImgurl(String imgurl) {
		this.imgUrl = imgurl;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public Integer getPv() {
		return pv;
	}

	public void setPv(int pv) {
		this.pv = pv;
	}
	
	public String getExpire() {
		return expiry_date;
	}

	public void setExpire(String expire) {
		this.expiry_date = expire;
	}
	
	public static void unserializeItemFront(){
		String requestUrl = "http://223.203.209.98:8080/irecommend/irecommend_main/irecommend_main.txt";

		URL url = null;
		try {
			url = new URL(requestUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		BufferedReader r = null;
		try {
			URLConnection con = url.openConnection();
			r = new BufferedReader(new InputStreamReader(con.getInputStream(),
					"UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String jsonSource = "";
		try {
			jsonSource = r.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		

		System.out.println("JsonSource:"+jsonSource);
		
		JsonArray jsonArray=new JsonParser().parse(jsonSource).getAsJsonArray();
		for (JsonElement jsonElement : jsonArray) {
			itemFront item_front=JsonUtils.fromJson(jsonElement.toString(),itemFront.class);
			if(item_front==null){
				System.out.println("error while unserialize json:"+jsonElement.toString());
			}
			System.out.println(item_front.getTimeStamp()+"_"+item_front.getCategory()+"-"+item_front.getWeight()+"-"+item_front.getTitle()+"-"+item_front.getUrl());
		}
	}

	
}
