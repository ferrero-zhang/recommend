package com.ifeng.iRecommend.featureEngineering;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Locale.Category;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.featureEngineering.OrientDB.KnowledgeGraph;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.EntityLibQuery;
import com.ifeng.iRecommend.featureEngineering.RuleFunction.StringContainsSet;
import com.ifeng.iRecommend.featureEngineering.RuleFunction.featureListContain;
import com.ifeng.iRecommend.featureEngineering.RuleFunction.featureListContainsSet;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import com.tinkerpop.blueprints.Vertex;
/**
 * 
 * <PRE>
 * 作用 : 通过规则引擎对特征列表进行校正
 *   
 * 使用 : 根据特征列表和标题，通过规则来优化
 *   
 * 示例 :
 *   
 * 注意 : 
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016年1月7日           hyx          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class RuleModify {
	private static final Log LOG = LogFactory.getLog("RuleModify");
	private static String Rulefile = LoadConfig.lookUpValueByKey("ModifyResultRulePath");
	private static String functionfile=LoadConfig.lookUpValueByKey("functionPath");
	private static String starListfile=LoadConfig.lookUpValueByKey("starListPath");
	private static String locmapfile=LoadConfig.lookUpValueByKey("locmapPath");
	private static String termmapfile=LoadConfig.lookUpValueByKey("termmapPath");

	private Set<String> Star_set=null;
	private Set<String> sport_set=null;
	private ArrayList<String> allRuleList=new ArrayList<String>();
	private ArrayList<String> sportRuleList=new ArrayList<String>();
	private Map<String, String> locMap= new HashMap<String, String>();
	private Map<String, String> termMap=new HashMap<String,String>  ();
	private ExpressRunner runner=new ExpressRunner();
	private static RuleModify instance =new RuleModify();
	
	private RuleModify() {
		buildingRuleMap();
		createlocationMap();
		EntityLibQuery.init();
	}

	public static RuleModify getInstance() {
		return instance;
	}
	
	public void createlocationMap(){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(locmapfile),"UTF-8"));
			String str;
			while((str=reader.readLine())!=null){
				String[] features=str.split("->");
				String[] locarr=features[0].split(" ");
				for(int i=0;i<locarr.length;i++){
					locMap.put(locarr[i], features[1]);
				}
			}
			reader.close();
			BufferedReader reader1 = new BufferedReader(new InputStreamReader(
					new FileInputStream(termmapfile),"UTF-8"));
			while((str=reader1.readLine())!=null){
				str=str.toLowerCase();
				String[] feature=str.split("\t");
				String word=feature[0].split(":")[1];
				String category=feature[1].split(":")[1];
				termMap.put(word, category);
			}
			reader1.close();
		} catch (Exception e) {
			// TODO: handle exception
			LOG.error(e);
		}
		
	}
	
	/**
	 * 解析规则文件，得到规则列表
	 * 
	 * @param Rulefile   规则文件路径
	 *
	 */
	private void buildingRuleMap() {
		ArrayList<String> result=new ArrayList<String>();
		try {

			runner.addFunction("StringContainsSet", new StringContainsSet("StringContainsSet"));
			runner.addFunction("featureListContainsSet", new featureListContainsSet("featureListContainsSet"));
			runner.addFunction("featureListContain", new featureListContain("featureListContain"));
			runner.loadExpress(functionfile);	
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(Rulefile),"UTF-8"));
			String s=null;
			Map<String, String> condition=null;
			condition=new HashMap<String, String>();
			String rule=null;
			while((s=reader.readLine())!=null){
				s = s.trim();
				if (s.startsWith("#start")) {
					rule=s.split("_")[1];
				}
				if(s.isEmpty() || s.startsWith("#start"))
					continue;
				else if (s.startsWith("condition")) {
					String[] ss=s.split("::");
					condition.put(ss[0], ss[1]);					
				}
				else if (s.startsWith("#end")) {
					if (rule.equals("allRuleList")) {
						allRuleList=result;
					}
					else if (rule.equals("sportRuleList")) {
						sportRuleList=result;
					}
					condition=new HashMap<String, String>();
					result=new ArrayList<String>();;
					continue;
				}
				else {
					for (Map.Entry<String, String> entry : condition.entrySet()) {
						s=s.replaceAll(entry.getKey(), entry.getValue());
					}
					result.add(s);
				}
				
			}
		reader.close();
		reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(starListfile),"UTF-8"));
		Star_set = new HashSet<String>();
		while((s=reader.readLine())!=null){
			s = s.trim();
			if(s.isEmpty())
				continue;
			Star_set.add(s);
		}
		reader.close();
		sport_set=new HashSet<String>();
		for (int i = 0; i < sportRuleList.size(); i++) {
			String[] words=sportRuleList.get(i).split("\\|");
			sport_set.add(words[0]);
		}
		sport_set.add("足球");
		sport_set.add("篮球");
		} catch (Exception e) {
			// TODO: handle exception
			LOG.error("[ERROR] ", e);
		}
	}
	
	/**
	 * 根据特征列表和标题，使用规则来修改结果
	 * 
	 * @param title   标题
	 * @param featureList   特征列表
	 * @return 修正过的特征列表
	 *
	 */
	public ArrayList<String> modifyResult(String title,ArrayList<String> featureList,String docType,boolean canbeSlide){
		if (featureList==null||featureList.isEmpty()) {
			return featureList;
		}
		String feature=null;
		if ("slide".equals(docType) || canbeSlide==true) {
			featureList=modifySlide(title, featureList,docType);
			//featureList=modifyet(featureList);
			//return featureList;
		}
		
		if (featureList!=null && !featureList.isEmpty()) {
			if (featureList.contains("股市") && featureList.get(featureList.indexOf("股市")+1).equals("sc") && Math.abs(Double.parseDouble(featureList.get(featureList.indexOf("股市")+2)))>=0.5) {
				return featureList;
			}
			for (int i = 0; i < featureList.size(); i++) {
				if (featureList.get(i).contains(",")) {
					featureList.set(i, featureList.get(i).replace(",", ""));
				}
			}
			feature=featureList.toString();
			feature=feature.replaceAll("[\\[\\]\\s]", "");
		}
		else {
			return featureList;
		}
//		ArrayList<String> titleWord=new ArrayList<String>();
//		if (title!=null && !title.isEmpty()) {
//			String [] temp = title.split("\\s+");
//			for (int i = 0; i < temp.length; i++) {
//				titleWord.add(temp[i].split("_")[0]);
//			}
//		}
//		EntityLibQuery.init();
//		for (String s:titleWord) {
//			ArrayList<EntityInfo> entityInfoList=EntityLibQuery.getEntityList(s);
//			for (EntityInfo ei :entityInfoList) {
//				System.out.println(ei);
//			}
//		}
		
		//String mtitle=title.toString();
		//mtitle=title.replaceAll("[\\[\\]\\s]", "");
		IExpressContext<String, Object> expressContext=new DefaultContext<String, Object>();
		expressContext.put("标题", title);
		//expressContext.put("标题列表", titleWord);
		expressContext.put("特征", feature);
		expressContext.put("特征列表", featureList);
		expressContext.put("明星集", Star_set);
		expressContext.put("docType", docType);
		//定向优化数据	
		String result=null;
		try {
			for (String temp:allRuleList) {
				if (temp!=null && !temp.isEmpty()) {
					result=(String)runner.execute(temp, expressContext, null, false, false);
					if (result!=null) {
						break;
					}
				}
			}
			if (result!=null) {
				featureList=new ArrayList<String>();
				String[] re=result.split(",");
				for (int j = 0; j < re.length; j++) {
					featureList.add(re[j]);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			LOG.error(e);
		}
		featureList=modifySport(title,featureList);
		featureList=modifyet(featureList); //对et进行修正
		featureList=modifyloc(title,featureList); //修正loc
		featureList=modifyTravel(title, featureList);//修正旅游类别的国家地域
		featureList=modifyFinal(title,featureList);

		LOG.info("modifyResult using rule:"+featureList);
		return featureList;
	}
//	private ArrayList<String> modifyExistCategory(ArrayList<String> featureList,Set<String> modifyCSet){
//		ArrayList<String> result=new ArrayList<String>();
//		ArrayList<String> otherList=new ArrayList<String>();
//		for(String c:modifyCSet){
//			boolean flag=false;
//			if (featurecontain(featureList, c, 0.5)) {
//				for (int i = 0; i < featureList.size(); i+=3) {
//					if (featureList.get(i+1).equals("sc") && (featureList.get(i).equals("手机")|| featureList.get(i).equals("平板电脑") || featureList.get(i).equals("笔记本")) &&
//							Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5) {
//						flag=true;
//						break;
//					}
//				}
//			}
//			else if (featurecontain(featureList, "萌宠", 0.5)) {
//				
//			}
//		}
//		
//		return featureList;
//	}
	private ArrayList<String> modifySlide(String title,ArrayList<String> featureList,String docType){
		ArrayList<String> otherList=new ArrayList<String>();
		if("slide".equals(docType)){
			if (featurecontain(featureList, "数码", 0.5)) {
				boolean flag1=false;
				for (int i = 0; i < featureList.size(); i+=3) {
					if (featureList.get(i+1).equals("sc") && (featureList.get(i).equals("手机")|| featureList.get(i).equals("平板电脑") || featureList.get(i).equals("笔记本")
							|| featureList.get(i).equals("影像"))
						 &&	Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5) {
						flag1=true;
						break;
					}
				}
				if(!flag1){//删除数码以及数码下的子分类
					featureList=DeleteDown(featureList, "数码");
				}
			}
			else if (featurecontain(featureList, "汽车|旅游", 0.5)) {
				boolean flag1=false;
				for(int i=0;i<featureList.size();i+=3){
					if (featurecontain(featureList, "社会", 0.5)) {
						flag1=true;
						break;
					}
					if (featureList.get(i).equals("社会图片") && Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5) {
						flag1=true;
						break;
					}
				}
				if (flag1) {
					featureList=DeleteDown(featureList, "汽车");
					featureList=DeleteDown(featureList, "旅游");
				}
			}
			else if (featurecontain(featureList, "科学探索", 0.5)) {
				boolean flag=false;
				for(int i=0;i<featureList.size();i+=3){
					if (featureList.get(i).equals("军事") || featureList.get(i).equals("动物图片") || featureList.get(i).equals("植物图片") && Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5) {
						flag=true;
						break;
					}
				}
				if (flag) {
					featureList=DeleteDown(featureList, "科学探索");
				}
			}
		}
			String category=modifyCategory(title, featureList);
			if ("美女".equals(category) && featurecontain(featureList, "娱乐|游戏|摄影|时尚", 0.5)) {
					
			}
			else if (featurecontain(featureList, "美女", 0.5)) {
				for(int i=0;i<featureList.size();i+=3){
					if (featureList.get(i+1).equals("c") && !featureList.get(i).equals("美女") && Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5){
						otherList.add("美女");
						category=null;
						break;
					}
				}
				
			}
			else {
				otherList.add("美女");
				category=null;
				
			}
			featureList=DeleteOther(featureList, otherList);
			if (category!=null) {
				if (!featurecontain(featureList, category, 0.5)) {
					featureList.add(category);
					featureList.add("c");
					featureList.add("0.5");
				}
				
			}
			return featureList;
	}
	private String modifyCategory(String title,ArrayList<String> featureList){
		if (title==null || title.isEmpty()) {
			return null;
		}
		title=title.toLowerCase();
		ArrayList<String> titleWord=new ArrayList<String>();
		if (title!=null && !title.isEmpty()) {
			String [] temp = title.split("\\s+");
			for (int i = 0; i < temp.length; i++) {
				titleWord.add(temp[i].split("_")[0]);
			}
		}
		boolean isStar=false;
		for(String s:titleWord){
			if (termMap.containsKey(s)) {
//				if (termMap.get(s).equals("美女") && (featurecontain(featureList, "奥运", 0.5) || featurecontain(featureList, "足球", 0.5) 
//						|| featurecontain(featureList, "篮球", 0.5) || title.contains("男子"))) {
//					return null;
//				}
				String category=termMap.get(s);
				if("美女".equals(category)){
					return category;
				}
				else if ("家居".equals(category) && featurecontain(featureList, "生活|高科技产业|房产|健康|风水", 0.5)) {
					return category;
				}
				else if ("科学探索".equals(category) && featurecontain(featureList, "科技", 0.5)) {
					return category;
				}
				else if ("历史".equals(category) && featurecontain(featureList, "时政|考古", 0.5)) {
					return category;
				}
				else if ("数码".equals(category) && featurecontain(featureList, "科技", 0.5)) {
					return category;
				}
			}
		}
		return null;
	}
	private ArrayList<String> DeleteDown(ArrayList<String> featureList,String word){
		ArrayList<String> result=new ArrayList<String>();
		KnowledgeGraph kGraph=new KnowledgeGraph();
		word=word.toLowerCase();
		ArrayList<Vertex> vList=kGraph.queryDownTraverse(word);
		for(int i=0;i<featureList.size();i+=3){
			boolean flag=false;
			for(Vertex v:vList){
				if (featureList.get(i).equalsIgnoreCase(v.getProperty("word").toString())) {
					flag=true;
					break;
				}
			}
			if (!flag) {
				result.add(featureList.get(i));
				result.add(featureList.get(i+1));
				result.add(featureList.get(i+2));
			}
		}
		kGraph.shutdown();
		return result;
	}
	private ArrayList<String> DeleteOther(ArrayList<String> featureList,ArrayList<String> otherList){
		for (int i = 0; i < otherList.size(); i++) {
			featureList=DeleteDown(featureList, otherList.get(i));
		}
		return featureList;
	}
	/**
	 * 修正体育类的结果
	 * 
	 * @param title   标题
	 * @param featureList   特征列表
	 * @param sportRuleList 各个类别的规则（关键字）
	 * @return 修正过的特征列表
	 *
	 */
	private ArrayList<String> modifySport(String title,ArrayList<String> featureList){
		if (featureList==null || featureList.isEmpty()) {
			return featureList;
		}
		if (featureList.contains("体育") && Math.abs(Double.parseDouble(featureList.get(featureList.indexOf("体育")+2))) >= 0.5) {
			String tyweight=featureList.get(featureList.indexOf("体育")+2);
			for(String s:sportRuleList){
				String[] words=s.split("\\|");
				String c=words[0];
				if (title!=null && title.contains(c)) {
					 featureList=deleteOtherSport(featureList, c);
				}
			}			
//			for (String s:sportRuleList) {
//				String[] words=s.split("\\|");
//				String c=words[0];
//				if (titleOrfeatureContain(title,featureList,words)) {
//					featureList=deleteOtherSport(featureList,c);
//				}
//			}
			if(title!=null && !title.contains("奥运") && featureList.contains("奥运") && !featureList.get(featureList.indexOf("奥运")+1).equals("c"))
			{
				int index=featureList.indexOf("奥运");
				featureList.set(index+1, "c");
				featureList.set(index+2, featureList.get(index+2).replace("-", ""));
			}
			if (title!=null && title.contains("奥运")) {
				if (featureList.contains("奥运")) {
					int index=featureList.indexOf("奥运");
					double weight=Math.abs(Double.parseDouble(featureList.get(index+2)));
					double tywei=Math.abs(Double.parseDouble(tyweight));
					if (tywei>weight) {
						weight=tywei;
					}
					featureList.set(index+1, "c");
					featureList.set(index+2, String.valueOf(weight));
					featureList.set(featureList.indexOf("体育")+2, "-"+String.valueOf(weight));
				}
				else
				{
					featureList.add("奥运");
					featureList.add("c");
					featureList.add(tyweight.replace("-", ""));
				}
			}
		}
		if (featureList.contains("奥运") && featureList.get(featureList.indexOf("奥运")+1).equals("c") && Math.abs(Double.parseDouble(featureList.get(featureList.indexOf("体育")+2)))==1) {
			
		}
		else {
			if ((featureList.contains("里约奥运") && Math.abs(Double.parseDouble(featureList.get(featureList.indexOf("里约奥运")+2)))==1)
					|| (featureList.contains("里约奥运会") && Math.abs(Double.parseDouble(featureList.get(featureList.indexOf("里约奥运会")+2)))==1)) {
				AddtoFeatureList(featureList, "奥运", "c", "1.0");
				AddtoFeatureList(featureList, "体育", "c", "-1.0");
			}
		}
		return featureList;
	}
	//向featureList中加入三元组，先判断是否已存在
	private void AddtoFeatureList(ArrayList<String> featureList,String word,String type,String weight){
		if (featureList.contains(word)) {
			int index=featureList.indexOf(word);
			featureList.set(index+1,type);
			featureList.set(index+2,weight);
		}
		else {
			featureList.add(word);
			featureList.add(type);
			featureList.add(weight);
		}
	}
	//标题和特征列表中是否包含关键字
	private boolean titleOrfeatureContain(String title,ArrayList<String> featureList,String[] words) {
		for (int i = 0; i < words.length; i++) {
			if (featureList.contains(words[i]) && Double.parseDouble(featureList.get(featureList.indexOf(words[i])+2))> 0.5) {
				return true;		
			}
		}
		if (title!=null && !title.isEmpty()) {
			for (int i = 0; i < words.length; i++) {
				if (title.contains(words[i])) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 通过规则得到领域c，删除除了c以外的其他体育类的c1（奥运，健身除外）
	 * 
	 * @param c 通过规则得到的领域c
	 * @return 修正过的特征列表
	 *
	 */
	private ArrayList<String> deleteOtherSport(ArrayList<String> featureList,String c){
		//ArrayList<String> childList=new ArrayList<String>();
		ArrayList<Vertex> vertexList=new ArrayList<Vertex>();
		ArrayList<String> semiresultList=new ArrayList<String>();
		KnowledgeGraph kGraph=new KnowledgeGraph();
		try{
			for (int i = 0; i < featureList.size(); i+=3) {
				boolean flag=false;
				for (String c1:sport_set) {			
					if (c1.equals(featureList.get(i)) && !c1.equals(c)) {
						flag=true;		
						vertexList=kGraph.queryChild(c1);
						break;
					}
				}
				if(!flag){
					semiresultList.add(featureList.get(i));
					semiresultList.add(featureList.get(i+1));
					semiresultList.add(featureList.get(i+2));
				}
			}
		}
		finally{
			kGraph.shutdown();
		}
		ArrayList<String> resultList=new ArrayList<String>();
		for (int i = 0; i < semiresultList.size(); i+=3) {
			boolean flag=false;
			for(Vertex v:vertexList){
				if (v.getProperty("word").equals(semiresultList.get(i).toLowerCase())) {
					flag=true;
					break;
				}
			}
			if (!flag) {
				resultList.add(semiresultList.get(i));
				resultList.add(semiresultList.get(i+1));
				resultList.add(semiresultList.get(i+2));
			}
		}
		String tyweight=featureList.get(featureList.indexOf("体育")+2);
		resultList.add(0,tyweight.replace("-", ""));
		resultList.add(0,"c");
		resultList.add(0,c);
		return resultList;
	}
	
	//在确保领域c正确的前提下，将跟sc和cn同名的et提上来
	private ArrayList<String> modifyet(ArrayList<String> featureList){
		if (featureList==null || featureList.isEmpty()) {
			return featureList;
		}
		ArrayList<String> supplyList=new ArrayList<String>();
		KnowledgeGraph kGraph=new KnowledgeGraph();
		try{
			for(int i=0;i<featureList.size();i+=3){
				String word=featureList.get(i);
				String type = getType(featureList.get(i));
				String weight=featureList.get(i+2);
				if (featureList.get(i+1).equals(type)) {
					continue;
				}
				if(!"cn".equals(featureList.get(i+1)) && !"et".equals(featureList.get(i+1)) && !"x".equals(featureList.get(i+1))){
					continue;
				}
				ArrayList<Vertex> upVertexList=kGraph.queryParent(word);
				boolean flagModify=false;
				if (upVertexList!=null && !upVertexList.isEmpty()) {
					for(Vertex v:upVertexList){
						int index=featureList.indexOf(v.getProperty("word"));
						if (index>=0 && v.getProperty("typelabel").equals("c0") && featureList.get(index+1).equals("c") && Math.abs(Double.parseDouble(featureList.get(index+2))) >=0.5){
							ArrayList<Vertex> vList=kGraph.queryWord(word,type);
							if (vList!=null && !vList.isEmpty() && "c".equals(type) && !featureList.get(i+1).equals(type)
									&&!word.equals("国际") && !word.equals("军事") && !word.equals("演出") 
									&& Math.abs(Double.parseDouble(weight))==1) {
								featureList.set(i, vList.get(0).getProperty("displayword").toString());
								featureList.set(i+1, "c");
								featureList.set(i+2, weight.replace("-", ""));
								flagModify=true;
							}
							break;
						}
					}
				}
				else {
					if ("c".equals(type) && !featureList.get(i+1).equals(type) && Math.abs(Double.parseDouble(weight))==1
							&& (word.equals("佛教")||word.equals("美食")||word.equals("公益")
									||word.equals("考古")||word.equals("星座")||word.equals("风水"))) {
						featureList.set(i+1, "c");
						featureList.set(i+2, weight.replace("-", ""));
						flagModify=true;
					}
				}
				if(!flagModify){
					if (featureList.get(i+1).equals(type) && !type.equals("cn")) {
						continue;
					}
					if (word.contains("华为") && Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5) {
						word="华为";
					}
					else if (word.contains("小米") && Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5) {
						word="小米";
					}
					else if ((word.contains("三星") ||word.toLowerCase().contains("samsung")) && Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5) {
						word="三星";
					}
					else if ((word.toLowerCase().contains("iphone"))&& Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5) {
						featureList.set(i+1, "et");
						word="苹果";
					}
					else if (word.contains("苹果") && Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5) {
						word="苹果";
					}
					upVertexList=kGraph.queryUpTraverse(word);
					if (upVertexList!=null && !upVertexList.isEmpty()) {
						type = getType(word);
						for(Vertex v:upVertexList){
							int index=featureList.indexOf(v.getProperty("word"));
							if (index>=0 && v.getProperty("typelabel").equals("c1") && featureList.get(index+1).equals("c") && Math.abs(Double.parseDouble(featureList.get(index+2))) >= 0.5) {
								ArrayList<Vertex> vList=kGraph.queryWord(word,type);
								if (vList!=null && !vList.isEmpty()) {
									if (!word.equals(featureList.get(i)) && (word.equals("华为")||word.equals("小米")||word.equals("三星")||word.equals("苹果")) ) {
										supplyList.add(featureList.get(i));
										if(featureList.get(i).toLowerCase().contains("iphone")){
											supplyList.add("et");
										}
										else{
											supplyList.add(featureList.get(i+1));
										}
										supplyList.add(featureList.get(i+2));
									}
									featureList.set(i, vList.get(0).getProperty("displayword").toString());
									featureList.set(i+1, type);
									featureList.set(i+2, weight.replace("-", ""));
									
								}
								break;
							}
						}
					}			
				}
			}	
			featureList.addAll(supplyList);
		}
		finally{
			kGraph.shutdown();
		}
		return featureList;
	}

	/**
	 *  获取待查字符串的类型：c0、c1、sc等
	 */
	protected String getType(String word){
		if(word == null || word.isEmpty())
			return null;
		KnowledgeGraph kGraph=new KnowledgeGraph();
		ArrayList<Vertex> vertexList=new ArrayList<Vertex>();
		try {
			vertexList=kGraph.queryWord(word);
		} 
		finally{
			kGraph.shutdown();
		}
		if (vertexList ==null || vertexList.isEmpty()) {
			return null;
		}
		return vertexList.get(0).getProperty("type");
	}
	
	//原特征列表中不包含>=0.5的loc，则将特征列表中含地域的词修正到loc，房产类的同时给出cn=loc+"房产"
	private ArrayList<String> modifyloc(String title,ArrayList<String> featureList) {
		if (featureList==null || featureList.isEmpty()) {
			return featureList;
		}
		//将loc无价值的分类 对应特征列表中的loc删除
		if (featurecontain(featureList, "段子|军事|移民|萌宠|家居|星座|国际|港澳|台湾",0.5)) {
			featureList=deleteloc(featureList);
			return featureList;
		}
		//社会、房产、天气对地域的修正可以放宽至0.5
		boolean flagsh=featurecontain(featureList, "社会",0.5);
		boolean flagfc=featurecontain(featureList, "房产", 0.5);
		boolean flagtq=featurecontain(featureList, "天气", 0.5);
		if (featurecontain(featureList, "风水|体育|美女|动漫|时尚|汽车|健康|娱乐|收藏|游戏|亲子|情感",0.5) && !flagsh) {
			featureList=deleteloc(featureList);
			return featureList;
		}
		ArrayList<String> supplyList=new ArrayList<String>();			
			for (int i = 0; i < featureList.size(); i+=3) {
				String loc="";
				if (locMap.containsKey(featureList.get(i)) && (Math.abs(Double.parseDouble(featureList.get(i+2)))==1 ||
						((flagsh || flagfc || flagtq) && Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5)) ) {
					loc=locMap.get(featureList.get(i));
				}
				if (loc.isEmpty() || loc==null) {
					continue;
				}
				if (!loc.equals(featureList.get(i))) {
					supplyList.add(loc);
					supplyList.add("loc");
					supplyList.add(featureList.get(i+2).replace("-", ""));
				}
				else {
					String weight=featureList.get(i+2);
					featureList.set(i+1, "loc");
					featureList.set(i+2, weight.replace("-", ""));
				}
			}
		//}
		featureList.addAll(supplyList);
		supplyList=new ArrayList<String>();
		for (int i = 0; i < featureList.size(); i+=3) {
			if (featureList.get(i+1).equals("loc") && Math.abs(Double.parseDouble(featureList.get(i+2)))>=0.5) {
				String loc=featureList.get(i);
				if (loc.endsWith("省") || loc.endsWith("市")) {
					loc=loc.substring(0, loc.length()-1);
				}
				else {
					ArrayList<EntityInfo> entityInfoList=EntityLibQuery.getEntityList(loc);
					if (entityInfoList!=null && !entityInfoList.isEmpty()) {
						loc=entityInfoList.get(0).getWord();
					}
				}
				if (featureList.contains("房产") && featureList.get(featureList.indexOf("房产")+1).equals("c") && Math.abs(Double.parseDouble(featureList.get(featureList.indexOf("房产")+2)))>=0.5) {
					int fcindex=featureList.indexOf("房产");
					
					if (!supplyList.contains(loc+"房产")) {
						supplyList.add(loc+"房产");
						supplyList.add("cn");
						supplyList.add(featureList.get(fcindex+2).replace("-", ""));
						AddcnTodb(loc+"房产");
					}
				}
				if (featureList.contains("天气") && featureList.get(featureList.indexOf("天气")+1).equals("c") && Math.abs(Double.parseDouble(featureList.get(featureList.indexOf("天气")+2)))>=0.5) {
					int tqindex=featureList.indexOf("天气");
					
					if (!supplyList.contains(loc+"天气")) {
						supplyList.add(loc+"天气");
						supplyList.add("cn");
						supplyList.add(featureList.get(tqindex+2).replace("-", ""));
						AddcnTodb(loc+"天气");
					}
				}else if (strcontain(title, "天气|冰雹|气温|降雪|雨夹雪|橙色预警|蓝色预警|低温|高温|冷空气|小雨|多云|暴雨|雷阵雨|雷雨|阵雨|中雨|降雨|降水")) {
					supplyList.add("天气");
					supplyList.add("c");
					supplyList.add(featureList.get(i+2).replace("-", ""));
					supplyList.add(loc+"天气");
					supplyList.add("cn");
					supplyList.add(featureList.get(i+2).replace("-", ""));
					AddcnTodb(loc+"天气");
				}
			}
		}
		
		featureList.addAll(supplyList);
		return featureList;
	}
	
	private ArrayList<String> modifyTravel(String title,ArrayList<String> featureList){
		if (featureList==null || featureList.isEmpty()) {
			return featureList;
		}
		if (featureList.contains("旅游") && Math.abs(Double.parseDouble(featureList.get(featureList.indexOf("旅游")+2))) >= 0.5) {
			
			ArrayList<String> titleWord=new ArrayList<String>();
			if (title!=null && !title.isEmpty()) {
				String [] temp = title.split("\\s+");
				for (int i = 0; i < temp.length; i++) {
					titleWord.add(temp[i].split("_")[0]);
				}
			}
			String weight=featureList.get(featureList.indexOf("旅游")+2);
			for (String s:titleWord) {
				ArrayList<EntityInfo> entityInfoList=EntityLibQuery.getEntityList(s);
				for (EntityInfo ei :entityInfoList) {
					if ((ei.getLevels().contains("国家") || ei.getLevels().contains("国家地区")) && ei.getLevels().contains("地点术语")) {
						featureList.add(ei.getWord()+"旅游");
						featureList.add("cn");
						featureList.add(weight.replace("-", ""));
						AddcnTodb(ei.getWord()+"旅游");
					}
				}
			}
//			ArrayList<Integer> flagi=new ArrayList<Integer>();
//			int count=0;
//			for (int i = 0; i < featureList.size(); i+=3) {
//				ArrayList<EntityInfo> entityInfoList=EntityLibQuery.getEntityList(featureList.get(i));
//				boolean flag=false;
//				for(EntityInfo ei:entityInfoList){
//					if (ei.getFilename().equals("entLib_各地著名地点") || ei.getLevels().contains("风景名胜") ||
//							ei.getLevels().contains("旅游景点")) {
//						flag=true;
//						break;
//					}
//				}
//				if (flag) {
//					flagi.add(i);
//					count++;
//				}
//				
//			}
//			if (count < 3) {//景点数少于3个
//				for (int i = 0; i < flagi.size(); i++) {
//					if (Math.abs(Double.parseDouble(featureList.get(flagi.get(i)+2)))>=0.5) {
//						featureList.set(flagi.get(i)+2, "1.0");
//					}
//				}
//			}
		}
		
		return featureList;
	}
	private ArrayList<String> modifyFinal (String title,ArrayList<String> featureList){
		for(int i=0;i<featureList.size();i+=3){
			if (featureList.get(i).equals("明星") && featureList.get(i+1).equals("c") && 
					Math.abs(Double.parseDouble(featureList.get(i+2)))>0.5) {
				featureList.set(i+2, "0.5");
			}
		}
		return featureList;
	}
	private boolean strcontain(String str,String words){
		boolean iscontain=false;
		if (str==null ||str.isEmpty()) {
			return iscontain;
		}
		String[] wordarr=words.split("\\|");
		for (int i = 0; i < wordarr.length; i++) {
			if (str.contains(wordarr[i])) {
				iscontain=true;
				break;
			}
		}
		return iscontain;
	}
	private boolean featurecontain(ArrayList<String> featureList,String words,double weight){
		if (featureList==null || featureList.isEmpty()) {
			return false;
		}
		for (int i = 0; i < featureList.size(); i+=3) {
			if (featureList.get(i+1).equals("c") && Math.abs(Double.parseDouble(featureList.get(i+2)))>=weight) {
				if (words.contains(featureList.get(i))) {
					return true;
				}
//				flag=i;
//				weight=Math.abs(Double.parseDouble(featureList.get(i+2)));
			}
		}
		
		return false;
	}
	private ArrayList<String> deleteloc(ArrayList<String> featureList){
		ArrayList<String> resultList=new ArrayList<String>();
		for (int i = 0; i < featureList.size(); i+=3) {
			if (featureList.get(i+1).equals("loc")) {
				continue;
			}
			resultList.add(featureList.get(i));
			resultList.add(featureList.get(i+1));
			resultList.add(featureList.get(i+2));
		}
		return resultList;
	}
	private static void AddcnTodb(String str){
		KnowledgeGraph kgraph=new KnowledgeGraph();
		ArrayList<Vertex> vertexlist=new ArrayList<Vertex>();
		vertexlist=kgraph.queryWord(str);
		if (vertexlist==null || vertexlist.isEmpty()) {
			kgraph.addVertex(str, "cn");
		}
		kgraph.shutdown();
	}
}
