/**
 * 
 */
package com.ifeng.iRecommend.likun.hotpredict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ifeng.iRecommend.featureEngineering.dataStructure.*;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.likun.rankModel.RankItemNew;


/**
 * <PRE>
 * 作用 : 新的热度预测模块
 *   
 * 使用 : 通过getInstance（）获取该类对象
 *      1、首先需要更新热度数据信息，调用updateHeatData();
 *      2、通过getNewsHotLevel函数，传入（itemf）返回新闻对应的HotLevel
 *   
 * 示例 :
 *   
 * 注意 : 在使用之前务必调用updateHeatData（）函数
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年4月21日        zhanzh          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
class HotItem{
	protected String id;
	protected String url;
	protected String title;
	protected int pv;
	protected String docType;
	protected String channel;
	protected String weight;
	protected String other;
}
public class heatPredictNew {
	private static final Log LOG = LogFactory.getLog("heatPredict");
	private static heatPredictNew instance = new heatPredictNew();
	private static IKVOperationv2 query = new IKVOperationv2(IKVOperationv2.defaultTables[0]);;
	//从PC端获取的PV进行计算后的热度MAP，key：新闻ID，value：热度项
	private HashMap<String,HotItem> pcHeatMap;
	private HashMap<String,String> yidianHeatMap;
	private heatPredictNew(){
//		String tablename = "appitemdb";
//		query = new IKVOperationv2(tablename);
		pcHeatMap = new HashMap<String, HotItem>();
		yidianHeatMap = new HashMap<String, String>();

	}
	public static heatPredictNew getInstance(){
		return instance;
	}
	
	/**
	 * 用于更新热度数据的统一接口
	 * 
	 * @param null
	 *            
	 * @return null
	 * 
	 */
	public void updateHeatData(){
		//更新PC端热度信息
		updatePCHeatMap();
		updateYidianHeatMap();
	}
	
	/**
	 * 查找rankItem中最高权重和最优ID
	 * 
	 * @param RankItemNew
	 *            
	 * @return String 数据格式 hotlevel+" "+bestID 实例： D 12321
	 * 
	 */
	public String getNewsHotLevel(RankItemNew rankItem){
//		if(rankItem == null || rankItem.getSimIDList() == null || rankItem.getSimIDList().isEmpty()||rankItem.getItem()==null){
//			return "";
//		}
		if(rankItem == null ||rankItem.getItem()==null){
			return "";
		}
		String hotLevel = "D";
		String bestID = rankItem.getItem().getID();
		//请求热度接口中的热度
		
		hotLevel = queryNewsHotLevel(rankItem.getItem().getID(), rankItem.getItem().getTitle(),rankItem.getSpecialWords());
		LOG.info(bestID + "HotLevel from HotModel : "+hotLevel);
		
		//查询PCpvhotmap 获取热度
//		if (pcHeatMap != null || !pcHeatMap.isEmpty()) {
//			if(pcHeatMap.containsKey(bestID)){
//				String temphotLevel = pcHeatMap.get(bestID).weight;
//				if(temphotLevel.compareTo(hotLevel)<0){
//					hotLevel = temphotLevel;
//					LOG.info(rankItem.getItem().getID() +"hotLevel from PCPV better : "+temphotLevel);
//				}
//			}
//		}
		
		
		//查询yidianHotmap 获取热度
//		if (yidianHeatMap != null || !yidianHeatMap.isEmpty()) {
//			String temphotLevel = yidianHeatMap.get(bestID);
//			if(temphotLevel != null){
//				if(temphotLevel.compareTo(hotLevel)<0){
//					hotLevel = temphotLevel;
//					LOG.info(rankItem.getItem().getID() +"hotLevel from yidian better : "+temphotLevel);
//				}
//			}
//		}
		//test 将自媒体类别的热度默认设为B
//		if(!hotLevel.equals("A")&&!hotLevel.equals("B")){
//			itemf item = rankItem.getItem();
//			HashMap<String, String> channelMap = parserItemfFeature(item);
//			String et = channelMap.get("et");
//			if(et != null&&et.indexOf("自媒体")>=0){
//				hotLevel = "B";
//				LOG.info("Wemedia item set hotleve B "+rankItem.getItem().getID());
//			}
//		}
		
		//test 将内容中包含GIF动画的新闻热度默认设置为B
		if(!hotLevel.equals("A")&&!hotLevel.equals("B")){
			itemf item = rankItem.getItem();
			if(item != null&&item.getSplitContent()!=null){
				String content = item.getSplitContent();
				String regEx = "http://[\\S]*?ifengimg.com[\\S]*?gif";
				Pattern pa = Pattern.compile(regEx);
				Matcher ma = pa.matcher(content);
				if(ma.find()){
					hotLevel = "B";
					LOG.info("GIF item set hotleve B "+rankItem.getItem().getID());
				}
			}
		}
		
		//test 将ifeng头条数据设置在B以上
		if(!hotLevel.equals("A")&&!hotLevel.equals("B")){
			itemf item = rankItem.getItem();
			if(item != null && item.getOther() != null){
				if(item.getOther().contains("ifengtoutiao")){
					hotLevel = "B";
					LOG.info("Toutiao item set hotlevel B "+rankItem.getItem().getID());
				}
			}
		}
		
		return hotLevel.trim()+" "+bestID;
	}
	
	/**
	 * 获取新闻PV接口，返回的是PV数
	 * 
	 * @param String id
	 *            
	 * @return int
	 * 
	 */
	public int getNewsPV(RankItemNew rankItem){
		int pv = 0;
		if(rankItem == null){
			return pv;
		}
		if(pcHeatMap == null || pcHeatMap.isEmpty()){
			LOG.warn("Please updata HeatData first ");
			return pv;
		}
		itemf item = rankItem.getItem();
		HotItem hitem = pcHeatMap.get(item.getID());
		if(hitem != null){
			pv = hitem.pv;
		}
		return pv;
	}
	
	
	/**
	 * 批量设置新闻热度和新闻PV
	 * 
	 * @param ArrayList<RankItemNew>
	 *            
	 * @return void
	 * 
	 */
	public void setItemsHotLevel(ArrayList<RankItemNew> rankItemList){
		if(rankItemList == null||rankItemList.isEmpty()){
			return;
		}
		for(RankItemNew rankItem : rankItemList){
			String weight = "D";
			int pv = getNewsPV(rankItem);
			String[] weightID = getNewsHotLevel(rankItem).split("\\s");
			if(weightID.length >= 2){
				weight = weightID[0];
			}
			rankItem.setWeight(weight);
			rankItem.setPv(pv);
		}
	}
	
	/**
	 * 查找后台热度模型获取热度信息
	 * 
	 * @param ArrayList<RankItemNew>
	 *            
	 * @return void
	 * 
	 */
	private String queryNewsHotLevel(String id,String title,String simId){
		String hotLevel = "D";
		String codeTitle = title;
		try {
			codeTitle = URLEncoder.encode(title, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			LOG.error("Title decode error : ",e1);
		}
		
		String reqUrl = "http://10.32.21.62:8081/HotNews/GetNewsItemHotLevelForDisModel?docId="+id+"&title="+codeTitle+"&simId="+simId;

		URL url;
		HttpURLConnection conn = null;
		InputStream in = null;
		BufferedReader br = null;
		try {
			url = new URL(reqUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(1 * 1000);
			conn.setReadTimeout(1 * 1000);
			in = conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\r\n");
			}
			hotLevel = sb.toString();
		} catch (MalformedURLException e) {
			LOG.error(id, e);
		} catch (IOException e) {
			LOG.error(id, e);
		} catch (Exception e) {
			LOG.error(id, e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.error(" ", e);
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					LOG.error(" ", e);
				}
			}
		}
		
		return hotLevel;
	}
	
	/**
	 * 读取统计信息并将其存入HotItem类型中
	 * 
	 * @param null
	 *            
	 * @return ArrayList<HotItem>
	 * 
	 */
	private ArrayList<HotItem> readPCnewsPv(){
		String reqUrl = "http://tongji.ifeng.com:9090/webtop/loadNews?chnnid=http://&num=10000&tmnum=0";//每分钟内PC端新闻PV统计
		HashMap<String,HotItem> pvNewsPvMap = new HashMap<String, HotItem>();
		ArrayList<HotItem> NewsPvList = new ArrayList<HotItem>();
		BufferedReader br = null;
		int count = 0;//三次重试计数器
		boolean isSuccess = false;
		while(count++  < 3 && !isSuccess){
			try {
				URL url =new URL(reqUrl);
				URLConnection con = url.openConnection();
				 br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				 String line = null;
				 while((line = br.readLine())!=null){
					 String title = null;
					 String newsurl = null;
					 int pv = 0;
					 try{
						 if(line.indexOf("title")>0&&line.indexOf("url")>0&&line.indexOf("num")>0){
							 
							 title = line.substring(line.indexOf("title=")+7, line.indexOf("url=")-2);
							 newsurl = line.substring(line.indexOf("url=")+5, line.indexOf("num=")-2);
							 String temppv = line.substring(line.indexOf("num=")+5, line.length()-8);
							 pv = Integer.valueOf(temppv);
						 }
					 } catch (Exception e){
						 LOG.warn("Parser PCPV error :", e);
						 continue;
					 }
	
					 if(title != null&&newsurl!=null){
						 HotItem hitem = new HotItem();
						 hitem.title=title;
						 hitem.url=newsurl;
						 hitem.pv=pv;
						 NewsPvList.add(hitem);
						 pvNewsPvMap.put(newsurl, hitem);
					 }
				 }
				 isSuccess = true;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				LOG.error("get PCNews pv error 1 : ", e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("get PCNews pv error 2 : ", e);
			} catch (Exception e){
				LOG.error("get PCNews pv error 3 : ", e);
			}
		}
		
		
		return NewsPvList;
	}
	
	/**
	 * 解析itemF的feature字段，并返回itemF的一级分类C的特征值，若解析失败返回null
	 * 
	 * @param itemF 
	 * 
	 * @return String 一级分类
	 * 
	 */
	private String getItemfFirstChannel(itemf item){
		ArrayList<String> featurelist = item.getFeatures();
		if(featurelist == null||featurelist.isEmpty()){
			return null;
		}
		for(int i=1;i<featurelist.size()-2;i+=2){
			String temp = featurelist.get(i);
			if(temp.equals("c")){
				String channel = featurelist.get(i-1);
				return channel;
			}
		}
		return null;
	}
	
	/**
	 * 解析itemF的feature字段，并返回itemF的特征map
	 * 
	 * @param itemF 
	 * 
	 * @return HashMap<String,String> key=c\sc\cn\et  value=自媒体|网球
	 * 
	 */
	private HashMap<String, String> parserItemfFeature(itemf item){
		HashMap<String, String> FeatureMap = new HashMap<String, String>();
		if(item == null){
			return FeatureMap;
		}
		ArrayList<String> featurelist = item.getFeatures();
		if(featurelist == null||featurelist.isEmpty()){
			return FeatureMap;
		}
		for(int i=1;i<=featurelist.size()-2;i+=3){
			String temp = featurelist.get(i);
			String feature = FeatureMap.get(temp);
			if(feature != null){
				feature = feature+"|"+featurelist.get(i-1);
				FeatureMap.put(temp, feature);
			}else{
				FeatureMap.put(temp, featurelist.get(i-1));
			}
			
		}
		return FeatureMap;
	}
	
	/**
	 * 从redis中获取已经算好的一点资讯热度map
	 * 
	 * @param null 
	 * 
	 * @return null
	 * 
	 */
	private void updateYidianHeatMap(){
		try{
			Jedis jedis = new Jedis("10.32.21.62", 6379, 10000);
			jedis.select(12);
			String temp = jedis.get("yidian_hotmap");
			if(temp == null || temp.isEmpty()){
				LOG.error("update yidianHeatMap error : yidianHotmap from redis is null ~ ");
				return;
			}
			Gson gson = new Gson();
			HashMap<String, String> hotmap = gson.fromJson(temp, new TypeToken<HashMap<String,String>>() {
			}.getType());
			if(hotmap != null){
				yidianHeatMap = hotmap;
			}
		}catch (Exception e){
			LOG.error("update yidianHeatMap error : ", e);
		}

		
	}
	
	/**
	 * 根据已有的PV信息通过阶差热度预测算法更新pcHeatMap中的热度信息
	 * 
	 * @param null 
	 * 
	 * @return null
	 * 
	 */
	private void updatePCHeatMap(){
		HashMap<String, ArrayList<HotItem>> classifyHotmap = new HashMap<String, ArrayList<HotItem>>();
		ArrayList<HotItem> newsPvList = readPCnewsPv();
		if(newsPvList == null||newsPvList.isEmpty()){
			LOG.warn("read PC news PV error");
			return;
		}
		LOG.info("newsPvList size : "+newsPvList.size());
		//清除map
		pcHeatMap.clear();
		for(HotItem hot : newsPvList){
			itemf item = query.queryItemF(hot.url,"c");
			if(item == null){
				LOG.info("Can not found this item : "+hot.url);
				continue;
			}
			
			
			hot.id = item.getID();
			hot.docType = item.getDocType();
			//按照新闻一级分类进行热度排序，若一级分类为空暂设为notopic
			String channel = getItemfFirstChannel(item);
			if(channel == null){
				LOG.info("Can not found item channel :"+item.getID());
				LOG.info("Set this item channel is notopic");
				channel = "notopic";
				//test
//				System.out.println(item.getFeatures());
			}
			hot.channel = channel;
			//将slide分离计算热度
			if(hot.docType != null&&hot.docType.equals("slide")){
				hot.channel = "slide"+channel;
			}
			//默认热度均为D
			hot.weight = "D";
			ArrayList<HotItem> tempList = classifyHotmap.get(hot.channel);
			if(tempList == null){
				tempList = new ArrayList<HotItem>();
				tempList.add(hot);
				classifyHotmap.put(hot.channel, tempList);
			}else{
				tempList.add(hot);
			}
		}
		
		for(Entry<String, ArrayList<HotItem>> entry : classifyHotmap.entrySet()){
			ArrayList<HotItem> hotList = entry.getValue();
			if(hotList == null||hotList.isEmpty()){
				continue;
			}
			Collections.sort(hotList, new Comparator<HotItem>() {

				@Override
				public int compare(HotItem o1, HotItem o2) {
					// TODO Auto-generated method stub
					int result = o2.pv-o1.pv;
					return result;
				}
			});
			//findA
			int indexA = findNextSep(hotList, 0, 9, 0.8);
			// 落差最大的位置及之前的item的权重设为B
			for (int i = 0; i <= indexA; i++)
				hotList.get(i).weight = "A";

			// 找B：
			int indexB = findNextSep(hotList, indexA + 1, hotList.size() - 1,
					0.8);
			if (indexB <= indexA)
				continue;
			// 落差最大的位置及之前的item的权重设为B
			for (int i = indexA + 1; i <= indexB; i++)
				hotList.get(i).weight = "B";

			// 找C：
			int indexC = findNextSep(hotList, indexB + 1, hotList.size() - 1,
					0.8);
			if (indexC <= indexB)
				continue;
			// 落差最大的位置及之前的item的权重设为B
			for (int i = indexB + 1; i <= indexC; i++)
				hotList.get(i).weight = "C";

			// 找D
			for (int i = indexC + 1; i < hotList.size(); i++)
				hotList.get(i).weight = "D";
		}
		if(pcHeatMap == null){
			pcHeatMap = new HashMap<String, HotItem>();
		}
		for(Entry<String, ArrayList<HotItem>> entry : classifyHotmap.entrySet()){
//			System.out.println("++++++++++++++++++++++++++");
//			System.out.println("Channel : "+entry.getKey());
			
			//test
			LOG.info("++++++++++++++++++++++++++");
			LOG.info("Channel : "+entry.getKey());
			
			for(HotItem hitme : entry.getValue()){
				pcHeatMap.put(hitme.id, hitme);
				
				//test
				LOG.info("ID : "+hitme.id+" Title : "+hitme.title+" url : "+hitme.url);
				LOG.info("weight : "+hitme.weight+" PV : "+hitme.pv);
				LOG.info("======================");
				
//				System.out.println("Title : "+hitme.title);
//				System.out.println("url : "+hitme.url);
//				System.out.println("weight : "+hitme.weight);
//				System.out.println("PV : "+hitme.pv);
//				System.out.println("======================");
			}
		}
		
		//test
		LOG.info("All hotItem num : "+newsPvList.size());
		LOG.info("hit the pool news num : "+pcHeatMap.size());
//		query.close();
//		System.out.println("All hotItem num : "+newsPvList.size());
//		System.out.println("hit the pool news num : "+pcHeatMap.size());
		
	}

	
	
	/**
	 * 查找下一个分割点；
	 * 
	 * 
	 */
	private int findNextSep(ArrayList<HotItem> itemList, int begin,
			int end, double PVFlagRate) {
		if(begin < 0)
			return -1;
		if(begin > end)
			return -1;
		
		// 默认第一个是
		int index_rt = -1, PV = itemList.get(begin).pv;
		if (end < 0)
			end = itemList.size() - 1;
		if(end >= itemList.size())
			end = itemList.size() - 1;
		// 计算[begin,end]的落差，获取第一个满足条件的落差
		int index = begin;
		for (; index < end; index++) {
			int gap = itemList.get(index).pv - itemList.get(index + 1).pv;
			float gap_rate = gap / (float) (itemList.get(index).pv);

			// 落差太小可以忽略
			if (gap <= 10 || gap_rate < 0.5)
				continue;
			//落差满足，算出落差的点，但是和目前PV差距太大
			if(gap_rate >= 0.90)
				break;
			
			index_rt = index;
			break;

		}
		// 没有算出落差，怎么处理？？
		if (index_rt < 0) {
			for (int i = begin; i <= end; i++) {
				if (itemList.get(i).pv < PV * PVFlagRate)
				{
					index_rt = i - 1;
					break;
				}
			}
		}

		if (index_rt < 0)
			index_rt = itemList.size() - 1;

		return index_rt;
	}
	
	public static void main(String[] args){
		
		heatPredictNew hn = heatPredictNew.getInstance();
		hn.updatePCHeatMap();
//		ArrayList<HotItem> hlist = hn.readPCnewsPv();
//		System.out.println("haha"+hlist);
		
//		queryInterface qi = queryInterface.getInstance();
//		itemf item = qi.queryItemF("http://ent.ifeng.com/a/20150427/42386587_0.shtml");
//		String doctype = item.getDocType();
//		System.out.println(doctype);
//		while(true){
//			heatPredictNew hn = heatPredictNew.getInstance();
//			hn.updatePCHeatMap();
//			
//			try {
//				Thread.sleep(5*1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}

		
		
		
		itemf item = query.queryItemF("3206952", "c");
		itemf item1 = query.queryItemF("3194019", "c");
		itemf item2 = query.queryItemF("3194015", "c");
////		System.out.println(item2.getFeatures());
		RankItemNew rankItem0 = new RankItemNew(item);
		RankItemNew rankItem1 = new RankItemNew(item1);
		RankItemNew rankitem2 = new RankItemNew(item2);
////		rankItem0.getSimIDList().add(item.getID());
////		rankItem0.getSimIDList().add(item1.getID());
//
////		String result = hn.getNewsHotLevel(rankItem0);
////		System.out.println("rankItem0 ID : "+rankItem0.getItem().getID());
////		System.out.println(rankItem0.getSimIDList());
////		System.out.println("result : "+result);
//		
////		RankItemNew rankItem1 = new RankItemNew(item2);
//		
		ArrayList<RankItemNew> rankItemList = new ArrayList<RankItemNew>();
//		for(int i=0;i<1000;i++){
//
//		}
		rankItemList.add(rankItem0);
		rankItemList.add(rankItem1);
		rankItemList.add(rankitem2);
//		long time = System.currentTimeMillis();
		System.out.println(rankItemList.size());
			hn.setItemsHotLevel(rankItemList);
			for(RankItemNew rankItem : rankItemList){
				String hotlevel = rankItem.getWeight();
				int pv = rankItem.getPv();
				System.out.println(rankItem.getItem().getID());
				System.out.println(hotlevel);
				System.out.println(pv);
			}
//		
//		time = System.currentTimeMillis() - time;
//		System.out.println("time : "+time);
//
	}

}
