package com.ifeng.iRecommend.dingjw.front_rankModel;

import java.util.ArrayList;

/**
 * <PRE>
 * 作用 : 
 *   权重模型接口类，包含3个链表
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
 *          1.0          2013-08-13        dingjw          create
 *          1.1			 2013-08-22		   dingjw		   modify
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class RankList {

	private ArrayList<RankItem> newList;//新加入的rankitem列表
	
	private ArrayList<RankItem> degrList;//发生降权的rankitem列表
	
	private ArrayList<RankItem> delList;//删除的rankitem列表
	
	private ArrayList<RankItem> backupList;//候补的rankitem列表
	
	private ArrayList<RankItem> hotList;//判断为热点的rankitem列表
	
	private ArrayList<RankItem> locList;//location news的rankitem列表
	
	private ArrayList<RankItem> weatherList;//天气资讯的rankitem列表
	
	private ArrayList<RankItem> autoList;//汽车的rankitem列表
	
	private ArrayList<RankItem> gamesList;//游戏的rankitem列表
	
	private ArrayList<RankItem> seasonList;//季节的rankitem列表
	
	public ArrayList<RankItem> getSeasonList() {
		return seasonList;
	}

	public void setSeasonList(ArrayList<RankItem> seasonList) {
		this.seasonList = seasonList;
	}

	private ArrayList<RankItem> festivalList;//节日的rankitem列表

	public ArrayList<RankItem> getFestivalList() {
		return festivalList;
	}

	public void setFestivalList(ArrayList<RankItem> festivalList) {
		this.festivalList = festivalList;
	}

	public RankList() {
		// TODO Auto-generated constructor stub
		newList = new ArrayList<RankItem>();
		degrList = new ArrayList<RankItem>();
		delList = new ArrayList<RankItem>();
		backupList = new ArrayList<RankItem>();
		locList = new ArrayList<RankItem>();
		weatherList = new ArrayList<RankItem>();
		autoList = new ArrayList<RankItem>();
		gamesList = new ArrayList<RankItem>();
		hotList = new ArrayList<RankItem>();
		seasonList = new ArrayList<RankItem>();
		festivalList = new ArrayList<RankItem>();
	}
	
	public void setHotList(ArrayList<RankItem> hotList)
	{
		this.hotList = hotList;
	}
	
	public ArrayList<RankItem> getHotList()
	{
		return this.hotList;
	}
	
	public void setNewList(ArrayList<RankItem> newList)
	{
		this.newList = newList;
	}
	
	public ArrayList<RankItem> getNewList()
	{
		return this.newList;
	}
	
	public void setDegrList(ArrayList<RankItem> degrList)
	{
		this.degrList = degrList;
	}
	
	public ArrayList<RankItem> getDegrList()
	{
		return this.degrList;
	}
	
	public void setDelList(ArrayList<RankItem> delList)
	{
		this.delList = delList;
	}
	
	public ArrayList<RankItem> getDelList()
	{
		return this.delList;
	}
	
	public void setBackupList(ArrayList<RankItem> backupList)
	{
		this.backupList = backupList;
	}
	
	public ArrayList<RankItem> getBackupList()
	{
		return this.backupList;
	}
	
	public void setLocList(ArrayList<RankItem> locList)
	{
		this.locList = locList;
	}
	
	public ArrayList<RankItem> getLocList()
	{
		return this.locList;
	}
	
	public ArrayList<RankItem> getWeatherList() {
		return this.weatherList;
	}

	public void setWeatherList(ArrayList<RankItem> weatherList) {
		this.weatherList = weatherList;
	}
	
	public void setAutoList(ArrayList<RankItem> autoList)
	{
		this.autoList = autoList;
	}
	
	public ArrayList<RankItem> getAutoList()
	{
		return this.autoList;
	}
	
	public void setGamesList(ArrayList<RankItem> gameList)
	{
		this.gamesList = gameList;
	}
	
	public ArrayList<RankItem> getGamesList()
	{
		return this.gamesList;
	}
	
	public RankList(RankList rankList){
		if (rankList != null) {
			this.setNewList(rankList.getNewList());
			this.setDegrList(rankList.getDegrList());
			this.setDelList(rankList.getDelList());
			this.setLocList(rankList.getLocList());
			this.setAutoList(rankList.getAutoList());
			this.setWeatherList(rankList.getWeatherList());
			this.setGamesList(rankList.getGamesList());
			this.setBackupList(rankList.getBackupList());
		}
		else {
			newList = new ArrayList<RankItem>();
			degrList = new ArrayList<RankItem>();
			delList = new ArrayList<RankItem>();
			locList = new ArrayList<RankItem>();
			weatherList = new ArrayList<RankItem>();
			autoList = new ArrayList<RankItem>();
			gamesList = new ArrayList<RankItem>();
			backupList = new ArrayList<RankItem>();
		}
	}

}
