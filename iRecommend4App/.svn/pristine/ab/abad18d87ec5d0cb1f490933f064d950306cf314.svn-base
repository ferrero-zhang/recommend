package com.ifeng.iRecommend.dingjw.front_rankModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Category;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.commen.classifyClient.ClassifierClient;
import com.ifeng.commen.lidm.hbase.HbaseInterface;
import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.channelsParser;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
/**
 * <PRE>
 * 作用 : 
 *   权重模型文章item类。
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
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class RankItem{

	@Expose
	@SerializedName("item")
	private Item item;
	
	@Expose
	@SerializedName("frontTitleSplited")
	private String frontTitleSplited;//分好词的标题
	
	@Expose
	@SerializedName("timeStamp")
	private String timeStamp;//最近一次从内容池中获取的时间
	
	@Expose
	@SerializedName("createTimeStamp")
	private String createTimeStamp;//创建rankitem的时间,以秒为单位
	
	@Expose
	@SerializedName("degraded")
	public int degraded;//权重的信号值A为2，B为1，C，D为0
	
	@Expose
	@SerializedName("lifetime")
	public int lifetime;//存活周期
	
	@Expose
	@SerializedName("weightFromOutside")
	private String weightFromOutside;//从内容池、统计或者抓取得到的实时weight
	
	@Expose
	@SerializedName("timeFromOutSide")
	private Long timeFromOutSide;//从内容池、统计或者抓取得到的实时weight的时间,注意，此处这个时间用的是系统当前时间，更简单一些，有误差；

	@Expose
	@SerializedName("id")
	private String id;//在oscache中的编号
	
	@Expose
	@SerializedName("specialWords")
	private String specialWords;//从标题和内容中随机抽取的特征码，用于相似性比较
	
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
	private String weight;//初始的hot level，不变
	
	@Expose
	@SerializedName("imgUrl")
	private String imgUrl;
	
	@Expose
	@SerializedName("pv")
	private Integer pv;//文章的每分钟实时浏览量
	
	@Expose
	@SerializedName("doc_type ")
	private String doc_type ;//item类型，如slide,doc,video
	
	@Expose
	@SerializedName("more_tags")
	private String more_tags;//编辑打的标签
	
	@Expose
	@SerializedName("others")
	private String others;//备注字段
	
	@Expose
	@SerializedName("publisdate")
	private String publishDate;//item的发布时间
	
	@Expose
	@SerializedName("simIDList")
	private ArrayList<String> simIDList;//相似的item的ID集合，用于hot predict的命中

	
	public RankItem() {}
	
	/* @test用
	 * 构造函数；根据输入的item；
	 * 注意，测试用：
	 * item限定为imcp来源
	 */
	public RankItem(Item item){
		if(item == null)
			return;
		this.setItem(item);
		this.id = item.getID();
		this.setTitle(item.getTitle());
		this.setImgurl("");
		this.setUrl(item.getUrl());
		this.setWeight("B");
		this.setTimeStamp(item.getDate());
		this.setCreateTimeStamp(item.getDate());
		this.setPv(100);//若没有pv，则pv初始化为0；
		//设置outside weight及outside time
		this.setWeightFromOutside("B");
		this.setTimeFromOutSide(System.currentTimeMillis());
		
		//根据前端映射树，计算category
		String catgory = null;
		try{
			catgory =  channelsParser.getInstance(ItemOperation.ItemType.APPITEM).getTransChannelByItem(item, 1).split("-")[0];
		}catch (Exception e) {
			e.printStackTrace();
			catgory = "notfound";
		}
		this.setCategory(catgory);	
		
		//对前端标题分词
		splitFrontTitle();
		
		//生成对应特征码
		this.extractSpecialWords();
	}
	
	/*
	 * 构造函数；根据输入的item_front，查询hbase，生成item；
	 */
	public RankItem(itemFront item_front,ItemOperation itemOP)
	{
		
		if(item_front!=null)
		{
			Item item = null;
			ItemOperation.ItemType itemtype = null;
			//判断item_front是什么类型
			if(item_front instanceof appItemFront){
				id = ((appItemFront)item_front).getImcpID();
				itemtype = ItemOperation.ItemType.APPITEM;
				itemOP.setItemType(itemtype);
				item = itemOP.getItem(((appItemFront)item_front).getImcpID());
				setDocType(((appItemFront)item_front).getDocType());
				setMoreTags(((appItemFront)item_front).getMoreTags());
				others = ((appItemFront)item_front).getOthers();
				
				//特殊处理，如果docType是doc并且有缩略图，那么doctype字段加上“pic”后缀
				if(this.getDocType().equals("doc")
						&& (others.indexOf("|!|pic=1")>0 || others.indexOf("|!|pic=0|1")>0)){
					this.setDocType("docpic");
				}
				
			}else if(item_front instanceof pcItemFront){
				//oscacheID = new SimpleDateFormat("yyMMddHHmmssSSS").format(new java.util.Date());
				id = String.valueOf(System.currentTimeMillis());
				id = id.substring(1, id.length());//适当减少1个字节
				itemtype = ItemOperation.ItemType.PCITEM;
				itemOP.setItemType(itemtype);
				item = itemOP.getItem(item_front.getUrl());
			}
			
			if(item == null)
			{
				System.out.println("not found:"+item_front.getUrl());
				return;
			}
			
			this.setItem(item);
			
			
			
			
			//做一些特殊处理，比如去除"高清大图"等字段
			String title1 = item_front.getTitle();
			if(title1 != null)
				title1 = title1.replace("[高清大图]", "(图)");
			
			this.setTitle(title1);
			this.setImgurl(item_front.getImgurl());
			this.setUrl(item_front.getUrl());
			this.setWeight(item_front.getWeight());
			this.setTimeStamp(item_front.getTimeStamp());
			this.setCreateTimeStamp(item_front.getTimeStamp());
			this.setPv(item_front.getPv() == null? 0 : item_front.getPv());//若没有pv，则pv初始化为0；
			this.setPublishDate(item.getDate());
			
		
			
			//设置outside weight及outside time
			this.setWeightFromOutside(this.getWeight());
			this.setTimeFromOutSide(System.currentTimeMillis());
			
			//对前端标题分词
			splitFrontTitle();

			
			//计算category
			/*//根据前端映射树，计算category
			String category = null;
			try{
				category =  channelsParser.getInstance(itemtype).getTransChannelByItem(item, 1).split("-")[0];
			}catch (Exception e) {
				e.printStackTrace();
				category = "other";
			}
			if(category == null || category.equals("notopic"))
				category = "other";
			
			this.setCategory(category);
			*/
			
			//add likun;2014-12-04;new method;采用自动分类算法寻找前端映射category
			//调用分类算法,如果content内容很短，不太靠谱，那么直接设置content为空，分类结果也为error
			//@问题：如果是来自于抓取数据，那么本身就有channel字段信息，这个时候，是不是不用调用分类算法，只用做下分类映射就可以？
			//用url来提取category
			String catFromUrl = "";
			try{
				catFromUrl =  channelsParser.getInstance(itemtype).getTransChannelByItem(item, 1).split("-")[0];
			}catch (Exception e) {
				e.printStackTrace();
				catFromUrl = "other";
			}
			if(catFromUrl == null || catFromUrl.equals("notopic") || catFromUrl.contains("null"))
				catFromUrl = "other";
			if(catFromUrl.equals("other")
					||catFromUrl.equals("blog")
					||catFromUrl.equals("v")
					||catFromUrl.equals("t")
					||catFromUrl.equals("news")){
				String content = this.getItem().getContent();
				if(content == null || content.trim().length() <= 192)
					content = "";
				String categorys = ClassifierClient.predict(
						item.getTitle(), content,
						"",
						"com.ifeng.secondLevelMapping.secondMappingForDiversity",
						null);

				if(categorys.startsWith("error")
						|| categorys.startsWith("client.error")){
					
				}else{
					String[] secs = categorys.split("\\s");
					if(secs.length == 2)
						this.setCategory(secs[0]);
					else
						this.setCategory(categorys);
				}

			}else
				this.setCategory(catFromUrl);
			
			//@Test
			if(this.getCategory() == null)
				this.setCategory("other");
		
			
			
//			//@test
//			System.out.println("url:"+item.getUrl());
//			System.out.println("title:"+this.getFrontTitleSplited());
//			System.out.println("content:"+this.getItem().getContent());
//			System.out.println("categorys:"+categorys+"|"+this.getCategory());
//			//...
			
			//AA级的item不走正常的降权删除逻辑，降权次数暂设为0
			if(item_front.getWeight().equals("AA")){
				this.degraded=0;
				this.lifetime=fieldDicts.hm_itemLifeTimes.get("AA");
			}else{
//				char weight = item_front.getWeight().charAt(0);
//				setLifeTime(weight);
				if(fieldDicts.hm_itemLifeTimes.containsKey(category))
					this.lifetime = fieldDicts.hm_itemLifeTimes.get(category);
				else
					this.lifetime = fieldDicts.hm_itemLifeTimes.get("other");
				
				//高清图加大生命周期
				if(this.getDocType().equals("slide"))
					this.lifetime = this.lifetime + 8;
				
				//具有时效性的内容，控制生命周期
				{
					String title = item_front.getTitle();
					if(title != null){
						if(title.indexOf("今日")>=0||title.indexOf("每日")>=0
								||title.indexOf("今晚")>=0
								||title.indexOf("今天")>=0)
						{
							//计算到24点的时间差，lifetime将截止到24点
							long time = System.currentTimeMillis();
							Date dt = new Date(time);
							int hour = dt.getHours();
							this.lifetime = 24 - hour -1;
							if(this.lifetime <= 0)
								this.lifetime = 1;
						}
						
						if(title.startsWith("预告")
								|| title.indexOf("预告：")>=0
								|| title.indexOf("预告-")>=0
								|| title.indexOf("发布会")>=0
								|| title.indexOf("直播")>=0
								||((this.category.equals("sports")||this.category.equals("zq")||this.category.equals("lanqiu"))&&title.indexOf("半场")>=0))
						{
							//计算到24点的时间差，lifetime将截止到24点
							long time = System.currentTimeMillis();
							Date dt = new Date(time);
							int hour = dt.getHours();
							this.lifetime = 24 - hour -1;
							if(this.lifetime <= 0)
								this.lifetime = 1;
						}
						
						if((this.category.equals("sports")||this.category.equals("zq")||this.category.equals("lanqiu"))&&title.indexOf("半场")>=0)
						{
							//计算到24点的时间差，lifetime将截止到24点
							long time = System.currentTimeMillis();
							Date dt = new Date(time);
							int hour = dt.getHours();
							this.lifetime = 24 - hour -1;
							if(this.lifetime <= 0)
								this.lifetime = 1;
						}
						
					}
					
					
					
					
				}
				
				//set degraded
				this.setDegraded(item_front.getWeight().charAt(0));
				
			}
			//如果编辑设置了存活时间，则采用编辑的
			if(item_front.getExpire() != null && !item_front.getExpire().isEmpty()){
				int editor_lifetime = Integer.parseInt(item_front.getExpire()) / 3600;
				if(editor_lifetime > this.lifetime)
					this.lifetime = editor_lifetime;
			}
			
			
			//生成对应特征码
			this.extractSpecialWords();
			
			simIDList = new ArrayList<String>();
			simIDList.add(id);
		}
	}
	


//	private void setLifeTime(char weight){
//		switch (weight) {
//		case 'A':
//			this.degraded=3;
//			this.lifetime=fieldDicts.hm_itemLifeTime.get("A");
//			break;
//		case 'B':
//			this.degraded=2;
//			this.lifetime=fieldDicts.hm_itemLifeTime.get("B");
//			break;
//		case 'C':
//			this.degraded=1;
//			this.lifetime=fieldDicts.hm_itemLifeTime.get("C");
//			break;
//		case 'D':
//			this.degraded=0;
//			this.lifetime=fieldDicts.hm_itemLifeTime.get("D");
//			break;
//		default:
//			this.degraded=0;
//			this.lifetime=fieldDicts.hm_itemLifeTime.get("D");
//			break;
//		}
//	}
	
	public String getID()
	{
		return id;
	}
	
	public void setID(String idIn) {
		// TODO Auto-generated method stub
		if(idIn != null && !idIn.isEmpty())
			id = idIn;
	}
	
	public String getFrontTitleSplited() {
		return frontTitleSplited;
	}
	
	public String getCreateTimeStamp()
	{
		return this.createTimeStamp;
	}
	
	public void setCreateTimeStamp(String timeStamp)
	{
//		if(this.createTimeStamp==null)
//			this.createTimeStamp=timeStamp;
		
		this.createTimeStamp=timeStamp;
	}
	

	public void setHotLevel(String hotlevel)
	{
		if(hotlevel!=null)
			this.setWeight(hotlevel);
	}
	
	public void setItem(Item item)
	{
		this.item = item;
	}
	
	public Item getItem() {
		return this.item;
	}
	
	
	public void setPublishDate(String date)
	{
		this.publishDate = date;
	}
	
	public String getPublishDate() {
		return publishDate;
	}
	
	public String getSpecialWords()
	{
		if(this.specialWords == null)
			this.extractSpecialWords();
		return this.specialWords;
	}
	
	
	
	/**
	 * 对标题进行分词
	 * 注意：
	 * 
	 * @param 
	 * @return
	 */
	public void splitFrontTitle()
	{
		//String split = RankModel.SplitWord(this.getTitle());
		if(this.getTitle() == null || this.getTitle().isEmpty())
			return;

		//String split = splidWordsInterface.getInstance().segment(this.getTitle().replace("[高清大图]", "").replace("(图)", ""));
		String split = SplitWordClient.split(this.getTitle(), null);
	
//		if(split == null)
//		{
//			return;
//		}
		
		//分词出错，则用hbase或者ikv中title替代
		if(split == null || split.isEmpty() || split.startsWith("error")){
			if(this.getItem() != null)
				this.frontTitleSplited  = this.getItem().getTitle();
		}else
			this.frontTitleSplited = split.replace("(/", "_").replace(")", "");
	}
	
	
	
	/**
	 * 根据标题和内容抽取特征词
	 * 注意：
	 * 
	 * @param 
	 * @return
	 */
	public void extractSpecialWords()
	{
		this.specialWords = "";
		if(this.getFrontTitleSplited() == null)
			return;
		//如果没有item、正文或内容，则将前端抓取标题作为特征词，并去除词性、换行以及标点
		if (this.item == null || this.item.getContent() == null || this.item.getContent().isEmpty() || this.getItem().getTitle() == null) {
			this.specialWords = this.getFrontTitleSplited().replaceAll("_[a-z]+", "").replace("\r\n", "").replaceAll("\\pP|\\pS", "");
			return;
		}
		
		//选取正文前20个词作为特征词
		/*StringBuffer sbTmp = new StringBuffer();
		String contentSplits[] = this.getItem().getContent().replaceAll("_[a-z]+", "").replace("\r\n", "").replaceAll("\\pP|\\pS", "").split(" ");
		int max = contentSplits.length < 20? contentSplits.length : 20;
		for (int i = 0; i < max; i++) {
			sbTmp.append(contentSplits[i]).append(" ");
		}*/
		
		//如果有正文和内容，则将真实标题，以及正文中每个句子的第一个和最后一个词拼装成特征词，例如
		StringBuffer sbTmp = new StringBuffer();
		String titleFiltered = this.getItem().getTitle().replaceAll("_[a-z]+", "").replace("\n", "");
		sbTmp.append(titleFiltered).append(" ");
		
		String contentSplits[] = this.getItem().getContent().replaceAll("_[a-z]+ ", " ").replace("\n", "").replace("\r", "").split("。");

		for(String sentence: contentSplits) {
			
			String sentenceSplited[] = sentence.replace("\n", "").replaceAll("\\pP|\\pS", "").split(" ");
			if (sentenceSplited.length > 2) {
				sbTmp.append(sentenceSplited[1]).append(" ").append(sentenceSplited[sentenceSplited.length-1]).append(" ");
			} else {
				sbTmp.append(sentence).append(" ");
			}
		}
		this.specialWords = sbTmp.toString();

	}
	
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
	
	public String getWeightFromOutside() {
		return weightFromOutside;
	}

	public void setWeightFromOutside(String weightFromOutside) {
		this.weightFromOutside = weightFromOutside;
	}

	public Long getTimeFromOutSide() {
		return timeFromOutSide;
	}

	public void setTimeFromOutSide(Long timeFromOutSide) {
		this.timeFromOutSide = timeFromOutSide;
	}
	
	public Integer getPv() {
		return pv;
	}

	public void setPv(int pv) {
		this.pv = pv;
	}
	
	public String getDocType() {
		return doc_type ;
	}

	public void setDocType(String doctype) {
		this.doc_type  = doctype;
	}
	
	public String getMoreTags() {
		return more_tags;
	}

	public void setMoreTags(String tags) {
		this.more_tags = tags;
	}
	
	
	public String getOthers() {
		return others;
	}
	
	public void setOthers(String othersIn) {
		this.others = othersIn;
	}
	
	public void setDegraded(char weight) {
		// TODO Auto-generated method stub
		switch (weight) {
		case 'A':
			this.degraded = 4;
			break;
		case 'B':
			this.degraded = 3;
			break;
		case 'C':
			this.degraded = 2;
			break;
		case 'D':
			this.degraded = 1;
			break;
		case 'E':
			this.degraded = 0;
			break;
		default:
			this.degraded = 0;
			break;
		}
	}
	
	/**
	 * 这是一个特殊的业务场景逻辑：当投放模型中的rankItem，外部的weight已经提升，那么这个时候我们可以考虑对其修正下lifetime、createtime、weight、degrede，
	 * 以方便投放模型进行重新投放；
	 * 注意：
	 * 
	 * @param outsideWeight
	 */
	public void weightingTo(String outsideWeight) {
		// TODO Auto-generated method stub
		if(outsideWeight == null || outsideWeight.isEmpty())
			return;
		this.setDegraded(outsideWeight.charAt(0));
		this.setWeight(outsideWeight);
		this.setWeightFromOutside(outsideWeight);
		this.setCreateTimeStamp(Long.toString(System.currentTimeMillis() / 1000));
		this.setTimeFromOutSide(System.currentTimeMillis());
	}


	public ArrayList<String> getSimIDList() {
		return simIDList;
	}

	
	
}
