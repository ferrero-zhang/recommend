/**
 * 
 */
package com.ifeng.iRecommend.zhanzh.preload.editorChannelPreload;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.iRecommend.zhanzh.Utils.DownloadPageUtil;
import com.ifeng.iRecommend.zhanzh.preload.algorithmChannelPreload.BasicDataUpdateJob;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.FrontNewsItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadModel.PreloadItem;
import com.ifeng.iRecommend.zhanzh.preload.preloadUtil.PreloadItemFromSolrUtil;
import com.ifeng.webapp.simArticle.client.SimDocClient;

import redis.clients.jedis.Jedis;

/**
 * <PRE>
 * 作用 : 
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
 *          1.0          2016年8月16日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class IfengEditorToutiaoPreload implements Runnable {
	private static Log LOG = LogFactory.getLog("IfengEditorToutiaoPreload");
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			new IfengEditorToutiaoPreload().preload();
		} catch (Exception e) {
			LOG.info("IfengEditorToutiaoPreload thread error ~"+e.getMessage());
		}
	}
	
	public void preload(){
		LOG.info("IfengEditorToutiaoPreload start");
		List<PreloadItem> focusItemList = new ArrayList<PreloadItem>();
		List<PreloadItem> docItemList = new ArrayList<PreloadItem>();
		
		//获取头条前20条内容，外加3条缩略图
		String page1url = "http://api.iclient.ifeng.com/ClientNews?id=SYLB10,SYDT10&gv=4.6.5&av=0&os=ios_9.0.2&vt=5&screen=750x1334&publishid=4002&pagesize=40";
		Map<String, List<PreloadItem>> page1itemMap = loadingNewsItemFromEditor(page1url);
		if(page1itemMap.get("focus") != null){
			focusItemList.addAll(page1itemMap.get("focus"));
		}
		if(page1itemMap.get("list") != null){
			docItemList.addAll(page1itemMap.get("list"));
		}
		
		//获取头条后20条内容
		String page2url = "http://api.iclient.ifeng.com/ClientNews?id=SYLB10,SYDT10&gv=4.6.5&av=0&os=ios_9.0.2&vt=5&screen=750x1334&publishid=4002&pagesize=40&page=2";
		Map<String, List<PreloadItem>> page2itemMap = loadingNewsItemFromEditor(page2url);
		if(page2itemMap.get("focus") != null){
			focusItemList.addAll(page2itemMap.get("focus"));
		}
		if(page2itemMap.get("list") != null){
			docItemList.addAll(page2itemMap.get("list"));
		}
		
//		//将头条数据跟内容池抓取数据匹配，获取simid
//		List<PreloadItem> tempList = getPreloadNewsItem("ifengtoutiao");
//		setSimId(tempList, docItemList);
//		setSimId(tempList, focusItemList);
		
		//通过将id和title传入小草的接口获取simid
		setSimIdByIdAndTitle(docItemList,false);
		setSimIdByIdAndTitle(focusItemList,true);
		
		List<FrontNewsItem> focusDisList = new ArrayList<FrontNewsItem>();
		List<FrontNewsItem> docDisList = new ArrayList<FrontNewsItem>();
		
		for(PreloadItem tempItem : focusItemList){
			focusDisList.add(tempItem.getFitem());
		}
		
		for(PreloadItem tempItem : docItemList){
			docDisList.add(tempItem.getFitem());
		}
		
		Gson gson = new Gson();
		String focusdis = gson.toJson(focusDisList);
		String docdis = gson.toJson(docDisList);
		
		String focusKey = "discoverToutiaoFocus";
		String docKey = "discoverToutiaoDoc";
		
		disToRedis(docKey, 8*60*60*1000, docdis);
		disToRedis(focusKey, 8*60*60*1000, focusdis);
		
		LOG.info("IfengEditorToutiaoPreload finashed");
//		System.out.println(new Gson().toJson(focusItemList));
//		System.out.println(new Gson().toJson(docItemList));
	}
	
	public static void main(String[] args){
//		new IfengEditorToutiaoPreload().preload();
		getItemSimId("七旬老太在斑马线上被大车碾压身亡", "cmpp_030000050136607", null);
	}

	
	/**
	 * 投放至redis函数
	 * 
	 * 
	 * 
	 * @param String tableName ： table名称
	 * 		  long validTime : 存活时间
	 * 		  String disStr ： 需要投放的字符串
	 * 
	 * @return 
	 */
	private void disToRedis(String tableName,long validTime,String disStr){
		if(disStr == null){
			return ;
		}
		try{

			Jedis jedis = new Jedis("10.90.7.60", 6379, 10000);
			jedis.select(1);
			String status = jedis.setex(tableName, (int) validTime, disStr);
			if(!status.equals("OK")){
				LOG.error("set status code:"+status);
			}else{
				LOG.info("Dis "+tableName+" to redis");
			}
		}catch(Exception e){
			LOG.error("ERROR"+e);
		}
	}
	
	private void setSimIdByIdAndTitle(List<PreloadItem> needSetDocList,boolean forFocus){
		LOG.info("setSimIdByIdAndTitle start");
		Gson gson  = new Gson();
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for(PreloadItem item : needSetDocList){
				String docId = item.getFitem().getDocId();
				String docTitle=item.getFitem().getTitle();
				String docType=item.getFitem().getDocType();
				String time=item.getFitem().getDate();
				//直播、专题、民调类不用查询simid
				if("text_live".equals(docType)||"topic2".equals(docType)||"survey".equals(docType)){
					continue;
				}
				String str=getItemSimId(docTitle, docId, null);
				if(str == null){
					LOG.warn("docid :"+docId+",title"+docTitle+"getItemSimId is null");
					continue;
				}
				simid simItem = null;
				try{
					simItem = gson.fromJson(str.trim(), simid.class);   //可能有空格之类导致解析异常
				} catch (Exception e){
					LOG.error("simid to gson failed :"+str,e);
					continue;
				}
				if(forFocus){
					//针对焦点图通过小草接口生成simID
					if("clusterId_null".equals(simItem.clusterId) &&"slide".equals(item.getFitem().getDocType())){
						try {
							Date date=formate.parse(time);
							long longtime=date.getTime();
							str=SimDocClient.doClusterNotCmppStyle(docId, docTitle, item.getFitem().getDocContent(), item.getFitem().getImages(), docType, longtime);
//						str=SimDocClient.doClusterNotCmppStyle(item.getFitem().getDocId(), item.getFitem().getTitle(), "", item.getFitem().getImages(), "slide", item.getFitem().getDate());
						    if(str!=null){
						    	simItem = gson.fromJson(str, simid.class);
						    }
						} catch (Exception e) {
							LOG.warn("SimDocClient.doClusterNotCmppStyle false :"+docId+",title"+docTitle+"getItemSimId is null");
							continue;
						}
					}
				}
				
				if(simItem.clusterId != null){
						if(simItem.clusterId.indexOf("clusterId_")>-1){
							item.getFitem().setSimId(simItem.clusterId);
						}else {
							item.getFitem().setSimId("clusterId_"+simItem.clusterId);
						}
				}
		}
		LOG.info("setSimIdByIdAndTitle finished");
	}
	
	private static  String getItemSimId(String title,String id,String content){
		try {
			String test = SimDocClient.doSearch(id, title, null, null);
			return test;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.error("getItemSimId failed ", e);
		}
		return null;
	}
	
//	private void setSimId(List<PreloadItem> solrItemList , List<PreloadItem> needSetDocList){
//		
//		
//		for(PreloadItem item : needSetDocList){
//			for(PreloadItem tempitem : solrItemList){
//				String docId = item.getFitem().getDocId();
//				if(docId.indexOf("_")>0){
//					String matchid = docId.substring(docId.lastIndexOf("_")+1, docId.length());
//					String other = tempitem.getFitem().getOthers();
//					if(other.indexOf(matchid)>0){
//						item.getFitem().setSimId(tempitem.getFitem().getSimId());
//					}
//				}
//			}
//		}
//	}
	
	/**
	 * 
	 * 从solr中查询预加载新闻
	 * 
	 * 
	 * 
	 * 注意：
	 * @param KeyWord keyword
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	private List<PreloadItem> getPreloadNewsItem(String channel){
		List<PreloadItem> preloadItemList = new ArrayList<PreloadItem>();

		int rows = 1000;//预加载数据量

//		String channel = "ifengtoutiao";
//channel = "editor";
		String queryStr = "other:("+channel+") AND available:(true)";

		SolrQuery query = generaterSolrQuery(queryStr, rows);
		//
		String solrUrl = BasicDataUpdateJob.getInstance().getConfigs().getString("solrUrl");
		List<PreloadItem> itemList = PreloadItemFromSolrUtil.preloadItemFromSolrWithSimId_test(query,solrUrl,false);
		preloadItemList.addAll(itemList);

		
		return preloadItemList;
	}
	
	/**
	 * 
	 * 获取solr查询参数
	 * 
	 * 
	 * 
	 * 注意：
	 * @param SolrQuery query
	 * 
	 * @return List<PreloadItem> 
	 * 
	 */
	private SolrQuery generaterSolrQuery(String queryStr,int rows){
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		query.setRows(rows);
		query.addSort("date",SolrQuery.ORDER.desc);
		query.set("fl", "*,score");
		
		//全新公式
		query.set("simi", "tfonly");
		query.set("defType", "payload");
		
		return query;
	}
	
	/**
	 * 加载编辑推荐位数据
	 * 
	 * 注意：通过channel字段访问配置文件获取对应的数据接口地址
	 * @param String channel 
	 * 
	 * @return String
	 * 
	 */
	private Map<String, List<PreloadItem>> loadingNewsItemFromEditor(String entryUrl){
		Map<String, List<PreloadItem>> editorNewsMap = new HashMap<String, List<PreloadItem>>();
		
		if(entryUrl == null){
			LOG.warn("Get Editore Visited EntryURL error ");
			return editorNewsMap;
		}
		String content = DownloadPageUtil.downloadPage(entryUrl, "UTF-8");
		if(content == null){
			LOG.warn("Download EditorNewsPage error "+entryUrl);
			return editorNewsMap;
		}
		List<EditorNewsStructure> editorItems = new ArrayList<EditorNewsStructure>();
		Gson gson = new Gson();
		try{
			editorItems = gson.fromJson(content, new TypeToken<List<EditorNewsStructure>>() {
			}.getType());
		}catch (Exception e){
			LOG.warn("Turn to EditorNewsStructure error "+entryUrl);
			return editorNewsMap;
		}
		//解析成前端需要的字符串(保留推荐位原有字符串格式）
		List<String> editorItemStr = new ArrayList<String>();
		try{
			editorItemStr = parserEditorContentStr(content);
		} catch (Exception e){
			LOG.warn("Parser EidtorJson error ", e);
		}
		
		
		
		for(EditorNewsStructure ed : editorItems){
			List<EditorNewsItem> items = ed.item;
			
			if(ed.type.equals("list")){
				List<PreloadItem> docList = turnEditorNewsItemToPreloadItem(items,editorItemStr);
				editorNewsMap.put("list", docList);
			}
			if(ed.type.equals("focus")){
				List<PreloadItem> focusList = turnEditorNewsItemToPreloadItemFocus(items,editorItemStr);
				editorNewsMap.put("focus", focusList);
			}
		}
		
		return editorNewsMap;
	}
	
	private List<PreloadItem> turnEditorNewsItemToPreloadItem(List<EditorNewsItem> editorItemList,List<String> editorItemStr){
		List<PreloadItem> preloadItemList = new ArrayList<PreloadItem>();
		if(editorItemList == null || editorItemStr == null){
			LOG.warn("Param is null ~");
			return preloadItemList;
		}
		
		SimpleDateFormat formate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat formate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//记录编辑推荐位位置信息
		for(EditorNewsItem item :editorItemList){
			FrontNewsItem fitem = new FrontNewsItem();

			//转换时间
			String time = null;
			try {
				if(item.updateTime == null){
					time = formate1.format(new Date());
				} else {
					time = formate1.format(formate.parse(item.updateTime));
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error(item.updateTime+" "+item.documentId);
				LOG.error("paser editor time error ", e);
				time = formate1.format(new Date());
			}
			String others =  "";
			for(String str : editorItemStr){
				if(str.indexOf(item.documentId) >= 0){
					others = str;
				}
			}
			//普通文章
			fitem.setTitle(item.title);
			fitem.setDocId(item.documentId);
			
			if(item.type.equals("doc")){
				fitem.setDocType("docpic");
			}else{
				fitem.setDocType(item.type);
			}
			
			fitem.setDate(time);
			fitem.setOthers(others);
			fitem.setWhy("imcp");
			
			//编辑数据相似度score 得分为10分
			fitem.setScore(10);
			
			PreloadItem pItem = new PreloadItem();
			pItem.setFitem(fitem);
			pItem.setSimScore(10);
			preloadItemList.add(pItem);
		}
		return preloadItemList;
	}
	
	private List<PreloadItem> turnEditorNewsItemToPreloadItemFocus(List<EditorNewsItem> editorItemList,List<String> editorItemStr){
		List<PreloadItem> preloadItemList = new ArrayList<PreloadItem>();
		if(editorItemList == null || editorItemStr == null){
			LOG.warn("Param is null ~");
			return preloadItemList;
		}
		
		SimpleDateFormat formate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		SimpleDateFormat formate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//记录编辑推荐位位置信息
		for(EditorNewsItem item :editorItemList){
			FrontNewsItem fitem = new FrontNewsItem();

			//转换时间
			String time = null;
			try {
				if(item.updateTime == null){
					time = formate1.format(new Date());
				} else {
					time = formate1.format(formate.parse(item.updateTime));
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error(item.updateTime+" "+item.documentId);
				LOG.error("paser editor time error ", e);
				time = formate1.format(new Date());
			}
			String others =  "";
			for(String str : editorItemStr){
				if(str.indexOf(item.documentId) >= 0){
					others = str;
				}
			}
			//普通文章
			fitem.setTitle(item.title);
			fitem.setDocId(item.documentId);
			
			//设置为体系内slide数据方便排重,并将图片和描述信息赋值方便请求小草接口生成simid
			fitem.setDocType("slide");
			StringBuffer sb=new StringBuffer();
			String entryUrl=item.link.url;
			String content=null;
			if(entryUrl!=null){
				content = DownloadPageUtil.downloadPage(entryUrl, "UTF-8");
			}
			if(content == null){
				LOG.warn("get focus page faild: "+entryUrl);
			}else {
				EditorFocusStructure  editorItems=new EditorFocusStructure();
				Gson gson = new Gson();
				try{
					editorItems = gson.fromJson(content, new TypeToken<EditorFocusStructure>() {
					}.getType());
				}catch (Exception e){
					LOG.warn("Turn to EditorFocusStructure error "+entryUrl);
				}
				List<slidesInner> slidesInners=editorItems.body.slides;
				if(slidesInners!=null){
					List<String> imgString=new ArrayList<String>();
					StringBuffer sbBuffer=new StringBuffer();
					for(slidesInner slide:slidesInners){
						if(slide.image!=null&&!slide.image.isEmpty()){
							imgString.add(slide.image);
						}
						if(slide.description!=null&&!slide.description.isEmpty()){
//						contentString.add(slide.description);
							sbBuffer.append(slide.description);
						}
					}
					fitem.setDocContent(sbBuffer.toString());
					fitem.setImages(imgString);
				}
			}
//			if(item.type.equals("doc")){
//				fitem.setDocType("docpic");
//			}else{
//				fitem.setDocType(item.type);
//			}
//			
			fitem.setDate(time);
			fitem.setOthers(others);
			fitem.setWhy("imcp");
			
			//编辑数据相似度score 得分为10分
			fitem.setScore(10);
			
			PreloadItem pItem = new PreloadItem();
			pItem.setFitem(fitem);
			pItem.setSimScore(10);
			preloadItemList.add(pItem);
		}
		return preloadItemList;
	}
	
	
	/**
	 * 将编辑推荐为的抓取数据进行字符串解析，用于提供给前端做特殊展示
	 * 
	 * 该字段对应到相应的item的other字段中同步给杨凯
	 * 
	 * 注意：
	 * @param String jsonStr 编辑推荐位的json字符串
	 * 
	 * @return List<String> 返回相应的字符串list
	 * 
	 */
	private List<String> parserEditorContentStr(String jsonStr) {
		List<String> editorItemList = new ArrayList<String>();
		if(jsonStr == null){
			return editorItemList;
		}
		if(jsonStr.indexOf("thumbnail")<0 || jsonStr.indexOf("},")<0){
			return editorItemList;
		}
		String[] strarray = jsonStr.split("},");
		StringBuffer tempSave = null;
		for(String str : strarray){
			if(tempSave != null && str.indexOf("thumbnail")>=0){
				editorItemList.add(tempSave.subSequence(0, tempSave.length()-1).toString());
				tempSave = null;
			}
			
			if(tempSave == null && str.indexOf("{\"thumbnail\"")>=0){
				tempSave = new StringBuffer();
				tempSave.append(str.substring(str.indexOf("{\"thumbnail\""))).append("},");
			}
			
			if(tempSave != null && str.indexOf("thumbnail")<0){
				tempSave.append(str).append("},");
			}
			
		}
		if(tempSave != null ){
			editorItemList.add(tempSave.subSequence(0, tempSave.length()-1).toString());
			tempSave = null;
		}
		return editorItemList;
	}
	
	class simid{
		String clusterId;
		List<String> simIds;
	}
	
	
}
