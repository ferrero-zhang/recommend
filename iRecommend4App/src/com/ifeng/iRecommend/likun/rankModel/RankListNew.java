package com.ifeng.iRecommend.likun.rankModel;

import java.util.ArrayList;

/**
 * <PRE>
 * 作用 : 
 *   投放指令类；提供add、degr、del等各种指令列表，每个指令操作对象是RankItem；
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 * 	 hotlist将不由投放模型出，单独离线出数据。
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2013-08-13        dingjw          create
 *          1.1			 2013-08-22		   dingjw		   modify
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class RankListNew {

	private ArrayList<RankItemNew> newList;//新加入的rankitem列表
	
	private ArrayList<RankItemNew> degrList;//发生降权的rankitem列表
	
	private ArrayList<RankItemNew> delList;//删除的rankitem列表
	
	private ArrayList<RankItemNew> backupList;//候补的rankitem列表
	
	private ArrayList<RankItemNew> qualityList;//判断为优质的rankitem列表；(注意，目前只提供体育的优质数据)
	
	private ArrayList<RankItemNew> locList;//location news的rankitem列表
	
	public RankListNew() {
		// TODO Auto-generated constructor stub
		newList = new ArrayList<RankItemNew>();
		degrList = new ArrayList<RankItemNew>();
		delList = new ArrayList<RankItemNew>();
		backupList = new ArrayList<RankItemNew>();
		locList = new ArrayList<RankItemNew>();
		qualityList = new ArrayList<RankItemNew>();

	}
	
	public void setQualityList(ArrayList<RankItemNew> hotList)
	{
		this.qualityList = qualityList;
	}
	
	public ArrayList<RankItemNew> getQualityList()
	{
		return this.qualityList;
	}
	
	public void setNewList(ArrayList<RankItemNew> newList)
	{
		this.newList = newList;
	}
	
	public ArrayList<RankItemNew> getNewList()
	{
		return this.newList;
	}
	
	public void setDegrList(ArrayList<RankItemNew> degrList)
	{
		this.degrList = degrList;
	}
	
	public ArrayList<RankItemNew> getDegrList()
	{
		return this.degrList;
	}
	
	public void setDelList(ArrayList<RankItemNew> delList)
	{
		this.delList = delList;
	}
	
	public ArrayList<RankItemNew> getDelList()
	{
		return this.delList;
	}
	
	public void setBackupList(ArrayList<RankItemNew> backupList)
	{
		this.backupList = backupList;
	}
	
	public ArrayList<RankItemNew> getBackupList()
	{
		return this.backupList;
	}
	
	public void setLocList(ArrayList<RankItemNew> locList)
	{
		this.locList = locList;
	}
	
	public ArrayList<RankItemNew> getLocList()
	{
		return this.locList;
	}

}
