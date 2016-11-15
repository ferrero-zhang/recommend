package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DataFormatException;

import org.apache.log4j.PropertyConfigurator;

import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.EntityLibQuery;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.WordReadData.WordInfo;
import com.ifeng.iRecommend.wuyg.commonData.Update.DateFormatUtil;

/**
 * 
 * <PRE>
 * 作用 : 
 *    订阅端接收测试  
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
 *          1.0          2015年12月29日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class SubTest {

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		PropertyConfigurator.configure("./conf/log4j.properties");

		// BlackListData blackListData = BlackListData.getInstance();

	//	WordReadData wordReadData = WordReadData.getInstance();
//
//		HotWordData hotWordData = HotWordData.getInstance();

		// ArticleSourceData articleSourceData =
		// ArticleSourceData.getInstance();

		 AliasLibData aliasLibData = AliasLibData.getInstance();

		// DataExpLib dataExpLib = DataExpLib.getInstance();

//		 ArticleSourceData articleSourceData =
//		 ArticleSourceData.getInstance();
		
		//
	//	 EntityLibQuery.init();
		//AllWordLibData allWordLibData = AllWordLibData.getInstance();

		CommonDataSub cds = new CommonDataSub();
		Thread t = new Thread(cds);
		t.start();

		while (true) {
			
		//	System.err.println("团体赛："+EntityLibQuery.getEntityList("团体赛"));
//			
//			System.err.println("思铂睿:"+EntityLibQuery.getEntityList("思铂睿"));
//			
//////			System.err.println("TESTESTETSTESS:"+allWordLibData.containsKey("TESTESTETSTESS"));
//////			System.err.println("fdsajfdasfd:"+allWordLibData.containsKey("fdsajfdasfd"));
////			// System.err.println(blackListData.get_blacklist().contains("testtesttest"));
////			// System.err.println(blackListData.get_blacklist().contains("TSETREST"));
////
//			 WordInfo wordInfo = wordReadData.getWordReadMap().get("太行");
//			
//			 if(null != wordInfo){
//				 System.err.println("太行："+wordInfo);
//				 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
//				 "yyyy-MM-dd HH:mm:ss"));
//			 }
////			 
////			 
////			 wordInfo = wordReadData.getWordReadMap().get("专栏");
////				
////			 if(null != wordInfo){
////				 System.err.println("专栏："+wordInfo);
////				 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
////				 "yyyy-MM-dd HH:mm:ss"));
////			 }
////			
////			 
////			 wordInfo = wordReadData.getWordReadMap().get("势如破竹");
////				
////			 if(null != wordInfo){
////				 System.err.println("势如破竹："+wordInfo);
////				 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
////				 "yyyy-MM-dd HH:mm:ss"));
////			 }
////			
////			 wordInfo = wordReadData.getWordReadMap().get("路人");
////				
////			 if(null != wordInfo){
////				 System.err.println("路人："+wordInfo);
////				 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
////				 "yyyy-MM-dd HH:mm:ss"));
////			 }
////			 
////			 wordInfo = wordReadData.getWordReadMap().get("惨状");
////				
////			 if(null != wordInfo){
////				 System.err.println("惨状："+wordInfo);
////				 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
////				 "yyyy-MM-dd HH:mm:ss"));
////			 }
////			 
////			 wordInfo = wordReadData.getWordReadMap().get("职位");
////				
////			 if(null != wordInfo){
////				 System.err.println("职位："+wordInfo);
////				 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
////				 "yyyy-MM-dd HH:mm:ss"));
////			 }
//////			 
////			 
//////			 wordInfo = wordReadData.getWordReadMap().get("学生");
//////				
//////			 System.err.println("学生："+wordInfo);
//////			 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
//////					 "yyyy-MM-dd HH:mm:ss"));
////			
//////			 wordInfo = wordReadData.getWordReadMap().get("99健康网");
//////				
//////			 System.err.println("99健康网:"+wordInfo);
//////			 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
//////		 "yyyy-MM-dd HH:mm:ss"));
//////			 
//////			 wordInfo = wordReadData.getWordReadMap().get("12缸汽车网");
//////			 System.err.println("12缸汽车网:"+wordInfo);
//////			 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
//////		 "yyyy-MM-dd HH:mm:ss"));
////			
//////			 wordInfo = wordReadData.getWordReadMap().get("全员");
//////				
//////			 System.err.println(wordInfo);
//////			 
//////			 wordInfo = wordReadData.getWordReadMap().get("全民");
//////				
//////			 System.err.println(wordInfo);
//////			//
//////			//
//////			//
////			//System.err.println(wordReadData.getWordReadMap().size());
////			
//////			WordInfo wordInfo = wordReadData.getWordReadMap().get("金发女郎测防爆服");
//////			
//////			 System.err.println("金发女郎测防爆服："+wordInfo);
//////			 
//////	
//////			 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
//////			 "yyyy-MM-dd HH:mm:ss"));
//////			 
//////			 wordInfo = wordReadData.getWordReadMap().get("广告主网");
//////				
//////			 System.err.println("广告主网："+wordInfo);
//////			 
//////	
//////			 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
//////			 "yyyy-MM-dd HH:mm:ss"));
//////			 
//////			 wordInfo = wordReadData.getWordReadMap().get("李克强");
//////				
//////			 System.err.println("李克强："+wordInfo);
//////			 
//////	
//////			 System.err.println(DateFormatUtil.longToString(wordInfo.getTimestamp(),
//////			 "yyyy-MM-dd HH:mm:ss"));
////
//////			 System.err.println("励志一生:"+articleSourceData.getArticleSourceMap().get("励志一生"));
//////			 System.err.println("深圳优生活:"+articleSourceData.getArticleSourceMap().get("深圳优生活"));
			 System.err.println("达赖:"+aliasLibData.searchAlias("达赖"));
			 System.err.println("达赖喇嘛:"+aliasLibData.searchAlias("达赖喇嘛"));
			 System.err.println("优步:"+aliasLibData.searchAlias("优步"));
			 System.err.println("优步："+aliasLibData.isContaince("优步"));
			 System.err.println("Uber："+aliasLibData.isContaince("Uber"));
			 System.err.println("Test："+aliasLibData.isContaince("Test"));
//////			 System.err.println("加菲猫:"+aliasLibData.getInstance().getAliasLibDataMap().get("加菲猫"));
//////			 System.err.println("大爷:"+aliasLibData.getInstance().getAliasLibDataMap().get("大爷"));
//////			 System.err.println("欧阳震华:"+aliasLibData.getInstance().getAliasLibDataMap().get("欧阳震华"));
////			//
////			// System.err.println("adidas："+aliasLibData.getInstance().getAliasLibDataMap().get("adidas"));
////
////			// System.err.println("国家公务员考试:"+
////			// dataExpLib.getDataExpMaps().get("国家公务员考试"));
////			// System.err.println("北京楼市:"+
////			// dataExpLib.getDataExpMaps().get("北京楼市"));
////			// System.err.println("互联网金融:"+
////			// dataExpLib.getDataExpMaps().get("互联网金融"));
////			// System.err.println("湖南卫视:"+
////			// dataExpLib.getDataExpMaps().get("湖南卫视"));
////			// System.err.println("马拉多纳:"+aliasLibData.getAliasLibDataMap().get("马拉多纳"));
////			// System.err.println();
////
//			ArrayList<String> keyList = new ArrayList<String>();
//////			
//////			keyList.add("公积金缴存比");
//////			keyList.add("台湾渔船被扣");
//////			keyList.add("日本外相访华");
//////			keyList.add("实习报酬");
//////			keyList.add("银河系富二代");
//////			keyList.add("温家宝雕像");
//////			keyList.add("朝鲜七大");
//////			keyList.add("网红协警");
//////			keyList.add("恒大收购");
//			keyList.add("从开始到现在");
//			for(String word : keyList){
//				HotWordInfo hotWordInfo = hotWordData.getHotwordMap().get(word);
//
//				if (null != hotWordInfo) {
//					System.err.println(word+"\t" + hotWordInfo.toString());
//					System.err.println(word+"\t" 
//							+ DateFormatUtil.longToString(
//									hotWordInfo.getLatesttimestamp(),
//									"yyyy-MM-dd HH:mm:ss"));
//					System.err.println(word+"\t" 
//							+ DateFormatUtil.longToString(
//									hotWordInfo.getStarttimestamp(),
//									"yyyy-MM-dd HH:mm:ss"));
//
//				} else {
//					System.err.println(word+"\t"  + "还没有");
//				}
//				System.err.println("hotwordstate:"+hotWordData.getHotWordchangeTime()+
//						transferLongToDate("yyyy-MM-dd HH:mm:ss", Long.valueOf(hotWordData.getHotWordchangeTime().toString())));
//			}
////			
//////System.err.println("=====");
////			 
////			HotWordInfo hotWordInfo = hotWordData.getHotwordMap().get("东方之星客轮倾覆事故");
////			
////			 if(null != hotWordInfo){ 
////			 System.err.println("东方之星客轮倾覆事故"+hotWordInfo.toString());
////			 System.err.println("东方之星客轮倾覆事故"+DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(),
////			 "yyyy-MM-dd HH:mm:ss"));
////			 System.err.println("东方之星客轮倾覆事故"+DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(),
////					 "yyyy-MM-dd HH:mm:ss"));
////			 }else{
////			 System.err.println("东方之星客轮倾覆事故  "+"还没有");
////			 }
////			 
////			 hotWordInfo = hotWordData.getHotwordMap().get("路人");
////			
////			 System.err.println("路人："+hotWordInfo);
////			 
////			 hotWordInfo = hotWordData.getHotwordMap().get("惨状");
////			 
////			 System.err.println("惨状："+hotWordInfo);
////			 
////			 hotWordInfo = hotWordData.getHotwordMap().get("势如破竹");
////			 
////			 System.err.println("势如破竹："+hotWordInfo);
////			 
////			 
////			 hotWordInfo = hotWordData.getHotwordMap().get("职位");
////			 
////			 System.err.println("职位："+hotWordInfo);
////
////			// //System.err.println("一字马："+hotWordData.getHotwordMap().get("一字马"));
////			// // System.err.println(hotWordData.getHotwordMap().get("兄弟"));
//////			 WordInfo wordInfo = wordReadData.getWordReadMap().get("西安本地化");
//////			 System.err.println(wordInfo.toString());
//////			 System.err.println("西安本地化："+DateFormatUtil.longToString(wordInfo.getTimestamp(),
//////			 "yyyy-MM-dd HH:mm:ss"));
////			// wordInfo = wordReadData.getWordReadMap().get("女人");
////			// System.err.println("女人："+DateFormatUtil.longToString(wordInfo.getTimestamp(),
////			// "yyyy-MM-dd HH:mm:ss"));
////			// wordInfo = wordReadData.getWordReadMap().get("兄弟");
////			// System.err.println("兄弟："+DateFormatUtil.longToString(wordInfo.getTimestamp(),
////			// "yyyy-MM-dd HH:mm:ss"));
////			 System.err.println("龙猫："+EntityLibQuery.getEntityList("龙猫"));
//////			 System.err.println("伙伴："+EntityLibQuery.getEntityList("伙伴"));
//		//	System.err.println("山鸡传奇.：" + EntityLibQuery.getEntityList("山鸡传奇."));
//			// System.err.println("姜宁："+EntityLibQuery.getEntityList("姜宁"));
//			// System.err.println("南华队："+EntityLibQuery.getEntityList("南华队"));
//			// System.err.println("卡德罗夫："+EntityLibQuery.getEntityList("卡德罗夫"));
//			// System.err.println("春季时尚："+EntityLibQuery.getEntityList("春季时尚"));
//			// System.err.println("海马普力马："+EntityLibQuery.getEntityList("海马普力马"));
//			// System.err.println("羊八井镇："+EntityLibQuery.getEntityList("羊八井镇"));
//			// System.err.println("左光斗："+EntityLibQuery.getEntityList("左光斗"));
//			// System.err.println("函数式编程："+EntityLibQuery.getEntityList("函数式编程"));
//			// System.err.println("碳酸二甲酯："+EntityLibQuery.getEntityList("碳酸二甲酯"));
//
//			// ConcurrentHashMap<String, WordInfo> woConcurrentHashMap =
//			// wordReadData.getWordReadMap();
//			// // WordInfo wordInfo = woConcurrentHashMap.get("TESTTESTTEST");
//			// // System.err.println("TESTTESTTEST："+wordInfo);
//			// // wordInfo = woConcurrentHashMap.get("中国银行外汇牌价");
//			// // System.err.println("中国银行外汇牌价："+wordInfo);
//			// WordInfo wordInfo = woConcurrentHashMap.get("人机大战");
//			// System.err.println("人机大战："+wordInfo);
//			// String time =
//			// DateFormatUtil.longToString(wordInfo.getTimestamp(),
//			// "yyyy-MM-dd HH:mm:ss");
//			// System.out.println("人机大战:"+ time);
//			// ////// time =
//			// DateFormatUtil.longToString(wordReadData.getWordReadMap().get("就是").getTimestamp(),
//			// "yyyy-MM-dd HH:mm:ss");
//			// ////// System.out.println("就是:"+ time);
//			// ////
//			// ////
//			// HotWordInfo hotWordInfo =
//			// hotWordData.getHotwordMap().get("人机大战");
//			// System.err.println("人机大战："+hotWordInfo);
//			// String starttime =
//			// DateFormatUtil.longToString(hotWordInfo.getStarttimestamp(),
//			// "yyyy-MM-dd HH:mm:ss");
//			// String latesttime =
//			// DateFormatUtil.longToString(hotWordInfo.getLatesttimestamp(),
//			// "yyyy-MM-dd HH:mm:ss");
//			// System.err.println("人机大战："+starttime+"\t"+latesttime);
//			// System.err.println("女士："+hotWordData.getHotwordMap().get("女士"));
//			// System.err.println("就是："+hotWordData.getHotwordMap().get("就是"));
//			// time =
//			// DateFormatUtil.longToString(hotWordData.getHotwordMap().get("全国企业信用信息公示系统").getTimestamp(),
//			// "yyyy-MM-dd HH:mm:ss");
//			// System.out.println("全国企业信用信息公示系统:"+ time);
//			// System.err.println("丁肇中："+wordReadData.getWordReadMap().get("丁肇中"));
//			// System.err.println("灵芝糖浆："+wordReadData.getWordReadMap().get("灵芝糖浆"));
//
//			// WordInfo wordInfo = wordReadData.getWordReadMap().get("供给侧改革");
//			// String time =
//			// DateFormatUtil.longToString(wordInfo.getTimestamp(),
//			// "yyyy-MM-dd HH:mm:ss");
//			// System.out.println("供给侧改革:"+ time);
//			//
//			// WordInfo wordInfo = wordReadData.getWordReadMap().get("弑母");
//			// String time =
//			// DateFormatUtil.longToString(wordInfo.getTimestamp(),
//			// "yyyy-MM-dd HH:mm:ss");
//			// System.out.println("弑母:"+
//			// wordReadData.getWordReadMap().get("弑母"));
//
//			// System.err.println("斑马运动："+articleSourceData.getArticleSourceMap().get("斑马运动"));
//			//
//			// System.err.println("凤凰网时尚:"+articleSourceData.getArticleSourceMap().get("凤凰网时尚"));
//
//			// System.err.println("10元厕所："+wordReadData.getWordReadMap().get("10元厕所"));
//			// System.err.println("test1："+wordReadData.getWordReadMap().get("test1"));
//			// System.err.println("test2："+wordReadData.getWordReadMap().get("test2"));
//			// System.err.println("test3："+wordReadData.getWordReadMap().get("test3"));
//			// System.err.println("test4："+wordReadData.getWordReadMap().get("test4"));
//			//
////			 System.err.println("金星："+EntityLibQuery.getEntityList("金星"));
////			 System.err.println("驾考："+EntityLibQuery.getEntityList("驾考"));
////			 System.err.println("护教辞："+EntityLibQuery.getEntityList("护教辞"));
//			// System.err.println("贺家铁："+EntityLibQuery.getEntityList("贺家铁"));
//			// System.err.println("阎肃："+EntityLibQuery.getEntityList("阎肃"));
//			// System.err.println("开放小区："+hotWordData.getHotwordMap().get("开放小区"));
//			// System.err.println("注射死："+hotWordData.getHotwordMap().get("注射死"));
//			// System.err.println("去库存："+hotWordData.getHotwordMap().get("去库存"));
//			// System.err.println("地铁逃票："+hotWordData.getHotwordMap().get("地铁逃票"));
//			// System.err.println("10元厕所："+hotWordData.getHotwordMap().get("10元厕所"));
//			// System.err.println("引力波："+hotWordData.getHotwordMap().get("引力波"));
//			// System.err.println("上海女孩："+hotWordData.getHotwordMap().get("上海女孩"));
//			// //
//			// System.err.println("世界互联网大会："+hotWordData.getHotwordMap().get("世界互联网大会"));
//			// System.err.println("邢利斌："+wordReadData.getWordReadMap().get("邢利斌"));
//			// System.err.println("mh370："+wordReadData.getWordReadMap().get("mh370"));
//			// System.err.println("MH370："+wordReadData.getWordReadMap().get("MH370"));
//			// System.err.println("引力波："+wordReadData.getWordReadMap().get("引力波"));
//			// //System.err.println("慰安妇："+EntityLibQuery.getEntityList("慰安妇"));
//			//
//			// System.err.println("德甲："+hotWordData.getHotwordMap().get("德甲"));
//			// System.err.println("熔断机制："+wordReadData.getWordReadMap().get("熔断机制"));
//			// System.err.println("奥运冠军："+EntityLibQuery.getEntityList("奥运冠军"));
//
//			// System.err.println("TESTTESTETESTE："+hotWordData.getHotwordMap().get("TESTTESTETESTE"));
//			// System.err.println("hexl："+hotWordData.getHotwordMap().get("hexl"));
//			// System.err.println("柯俊雄患肺癌病危:"+hotWordData.getHotwordMap().get("柯俊雄患肺癌病危"));
//			// System.err.println("测试:"+hotWordData.getHotwordMap().get("测试"));
//			// System.err.println("中超："+hotWordData.getHotwordMap().get("中超"));
//
//			// System.err.println("周子瑜："+wordReadData.getWordReadMap().get("周子瑜"));
//			// System.err.println("维密天使："+wordReadData.getWordReadMap().get("维密天使"));
//			// System.err.println("中储粮："+wordReadData.getWordReadMap().get("中储粮"));
//
//			// System.err.println("维密天使："+blackListData.get_blacklist().contains("维密天使"));
//			// System.err.println("中国："+blackListData.get_blacklist().contains("中国"));
//			// System.err.println("印度："+blackListData.get_blacklist().contains("印度"));
//			// System.err.println("日本："+blackListData.get_blacklist().contains("日本"));
//
//			// System.err.println("留守儿童："+wordReadData.getWordReadMap().get("留守儿童"));
//			//
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	   private static String transferLongToDate(String dateFormat,Long millSec){
		     SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		     Date date= new Date(millSec);
		            return sdf.format(date);
		    }
	
}
