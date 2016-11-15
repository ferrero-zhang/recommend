package com.ifeng.iRecommend.featureEngineering.dataStructure;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class appBill {
	@Expose
	@SerializedName("id")
	private String id;			//id
	
	@Expose
	@SerializedName("url")
	private String url;			//url
	
	@Expose
	@SerializedName("title")
	private String title;			//标题
	
	@Expose
	@SerializedName("other")
	private String other;			//标题
	
	public String getID() 
	{
		return this.id;
	}
	public void setID(String id)
	{
		this.id = id;
	}
	
	public String getUrl()
	{
		return this.url;
	}
	public void setUrl(String url)
{
		this.url = url;
	}
	
	public String getTitle() 
	{
		return this.title;
	}
	public void setTitle(String title) 
	{
		this.title = title;
	}
	
	public String getOther() 
	{
		return this.other;
	}
	public void setOther(String other) 
	{
		this.other = other;
	}
	
	@Expose
	@SerializedName("tags")
	private ArrayList<String> tags;		
	
	@Expose
	@SerializedName("relatedIds")
	private ArrayList<String> relatedIds;	
	
	
	
	public ArrayList<String> getTags()
	{
		return this.tags;
	}
	
	public ArrayList<String> getrelatedIds()
	{
		return this.relatedIds;
	}

	public void setTags(ArrayList<String> tagList){
		this.tags = tagList;
	}
	
	public void setRelatedIds(ArrayList<String> idList){
		this.relatedIds = idList;
	}
	
	public appBill(){
		tags = new ArrayList<String>();
		relatedIds = new ArrayList<String>();
	}
}
