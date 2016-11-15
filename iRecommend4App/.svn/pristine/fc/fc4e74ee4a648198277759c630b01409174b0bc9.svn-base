package com.ifeng.iRecommend.featureEngineering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.featureEngineering.OrientDB.KnowledgeGraph;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;
import com.tinkerpop.blueprints.Vertex;

/**
 * 
 * <PRE>
 * 作用 : 将item others字段中的tags标签映射分析
 *   
 * 使用 : 输入tags，返回具体频道属性
 *   
 * 示例 :输入 “凤凰汽车-试驾-抢先试驾”；返回
 * 一级分类 c: 汽车
 * 二级分类 sc: 试驾
 * 栏目或者专题 cn: 抢先试驾
 *   
 * 注意 : tags输入及返回均为中文
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          4.0          2015年4月8日        lixiao          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class TagsParser {
	private static final Log LOG = LogFactory.getLog("tagsParser");
	private static String functionfile=LoadConfig.lookUpValueByKey("functionPath");
	private static String Rulefile = LoadConfig.lookUpValueByKey("RulePath");
	private static String tagsMappingFileName = LoadConfig
			.lookUpValueByKey("tagsMappingPath");
	private static String locmapfile=LoadConfig.lookUpValueByKey("locmapPath");
	private Map<String, String> locMap= new HashMap<String, String>();
	private Map<String, String> mapTable = null; // tags映射表	
	private Set<String> star_set=null;
	private ArrayList<String> TagRuleList=new ArrayList<String>();
	private ArrayList<String> TagsRuleList=new ArrayList<String>();
	private ArrayList<String> TagTailRuleList=new ArrayList<String>();
	private ExpressRunner runner=new ExpressRunner();
	private static TagsParser instance = new TagsParser();
	//private static Graph graph=Graph.getInstance();
	private TagsParser() {
		//LOG.info("BuildingMappingTree...");
		buildMappingTable(tagsMappingFileName);
		//LOG.info("BuildingRule");
		buildingRuleMap();
		createlocationMap();
	}

	public static TagsParser getInstance() {
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
		} catch (Exception e) {
			// TODO: handle exception
			LOG.error(e);
		}
		
	}
	/**
	 * 解析规则文件，得到若干规则列表
	 */
	private void buildingRuleMap() {
		ArrayList<String> result=new ArrayList<String>();
		try {
			runner.loadExpress(functionfile);	
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(Rulefile),"UTF-8"));
			String s=null;
			star_set = new HashSet<String>();
			Map<String, String> condition=null;
			condition=new HashMap<String, String>();
			String rule=null;
			while((s=reader.readLine())!=null && !s.isEmpty() ){
				if (s.startsWith("#start")) {
					rule=s.split("_")[1];
				}
				else if (s.startsWith("condition")) {
					String[] ss=s.split("::");
					if (ss[0].contains("Star")) {
						String[] starname=ss[1].split("\\|");
						for (int i = 0; i < starname.length; i++) {
							star_set.add(starname[i]);
						}
					}
					else {
						condition.put(ss[0], ss[1]);					
					}
				}
				else if (s.startsWith("#end")) {
					if (rule.equals("TagsRuleList")) {
						TagsRuleList=result;
					}
					else if (rule.equals("TagRuleList")) {
						TagRuleList=result;
					}
					else if (rule.equals("TagTailRuleList")) {
						TagTailRuleList=result;
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
		} catch (Exception e) {
			// TODO: handle exception
			LOG.error("[ERROR] ", e);
		}
	}

	/**
	 * 从本地读取映射表文件，初始化tags映射表
	 * 
	 * @param mappingFileName
	 *            本地映射表文件路径
	 *
	 */
	private void buildMappingTable(String mappingFileName) {
		BufferedReader rd = null;
		try {
			rd = new BufferedReader(new InputStreamReader(new FileInputStream(
					mappingFileName), "UTF-8"));
			String line = null;
			mapTable = new HashMap<String, String>(500);
			int l = 0;
			while ((line = rd.readLine()) != null) {
				line = line.trim();
				if(line.isEmpty())
					continue;
				l++;
				String[] splited = line.split("->");
				try {
					mapTable.put(splited[0], splited[1]);
				} catch (ArrayIndexOutOfBoundsException e) {
					LOG.warn("[WARNING] Line " + l + " is empty!");
				}
			}
			
			//@test add likun
			LOG.debug("mapTable.size = "+mapTable.size());
		} catch (UnsupportedEncodingException e) {
			LOG.error("[ERROR] ", e);
		} catch (FileNotFoundException e) {
			LOG.error("[ERROR] ", e);
		} catch (IOException e) {
			LOG.error("[ERROR] ", e);
		} finally {
			if (rd != null) {
				try {
					rd.close();
				} catch (IOException e) {
					LOG.error("[ERROR] ", e);
				}
			}
		}
		
	}

	/**
	 * 根据输入的tag获取一篇文章映射后的分类信息 1. 尝试全匹配 2.
	 * 失败后截去最后一级再匹配，截去的部分长度大于4，直接给cn属性并置权重为-1，表明只可用作用户表达不可用作可读化表达 3.
	 *若截去部分长度小于四则放入待给前缀的pendingList中 若此时sc与c同时缺失，返回已有的映射结果  
	 * 改为：失败后截去最后一级再匹配，截去的部分先查c和sc，没有结果则按原来的规则，如果不满足规则，则给x
	 * 
	 * @param inputTag
	 * @param f 当前item对应的CMPPDataOtherField 对象 若为null，则此时为获取c0的调用
	 * @return "null" 丢弃 "notopic" 走分类算法 null 标记为新数据
	 */
	public ArrayList<String> parseTags(String inputTag, CMPPDataOtherField f,IExpressContext<String, Object> expressContext) {
		if (inputTag == null || inputTag.isEmpty())
			return null;
		String str_temp = new String(inputTag);
		ArrayList<String> classList = new ArrayList<String>();
		String result = mapTable.get(str_temp);
		// result 为null 表明全匹配失败
		if (f == null && result == null)
			return classList; // 当前c没有c0
		int lastSplitedPosition = str_temp.lastIndexOf("-");
		String str_tail;
		boolean isSame = false;
		//LOG.info("starting tagtail");
		while (result == null && lastSplitedPosition>-1 && f != null) {// 上一个sub没查到并且还可以截断
			if (f.getSource()!=null && (f.getSource().toLowerCase().contains("yidian"))) {
				break;
			}
			str_tail = str_temp.substring(lastSplitedPosition + 1);// 当前尾部
			str_temp = str_temp.substring(0, lastSplitedPosition);// 截断至最后一个-
			lastSplitedPosition = str_temp.lastIndexOf("-");// 更新最后一个-位置
//			Pattern pattern=Pattern.compile("[0-9]*");
//			if (pattern.matcher(str_tail).matches()) {
//				continue;
//			}
			result = mapTable.get(str_temp);// 继续查	
			ArrayList<String> candidateList = null;
			candidateList=isOtherLabel(str_tail,"1");
			if (candidateList==null || candidateList.isEmpty()) {
				expressContext.put("source",f.getSource());
				expressContext.put("UpperClass", f.getUpperClass());
				expressContext.put("尾部",str_tail );
				if (str_tail.equals(f.getLt())) {
					isSame = true;
					f.setLt(null);
				}
				String result1=null;
				expressContext.put("SameLt",isSame);
				try {
					for(String temp:TagTailRuleList){				
						result1=(String)runner.execute(temp, expressContext, null, false, false);
						String is=expressContext.get("SameLt").toString();
						Boolean Bl=new Boolean(is);
						isSame=Bl.booleanValue();
						if (result1!=null) {
							break;
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					LOG.error(e);
				}
				if (result1!=null) {
					if (result1.equals("continue")) {
						continue;
					}
					else {
						String[] r=result1.split(",");
						for (int i = 0; i < r.length; i++) {
							classList.add(r[i]);
						}
					}
				}
				if (result1==null) {
					classList.add(str_tail);
					classList.add("x");
					classList.add("-1.0");
					//invPendingTagTails.add(str_tail);
				}
			}
			else {
				classList.addAll(candidateList);
			}			
	}
		if (result == null){
			return classList;// 没有可以补充的结果，因此待定list也不考虑
		}
		if (f != null && f.getLt() != null && inputTag.contains(f.getLt())) //
			f.setLt(null);
		if (!result.equals("notopic")) {
			String[] classify = result.split(",");
			if (classify == null || classify.length <= 0) {
				LOG.error("[ERROR] Empty mapping pair!!");
				return classList;
			}
			for (int i = 0; i < classify.length; i++) {
				String[] valuePair = classify[i].split("=");
				if (valuePair.length > 1) {
					String[] temp = valuePair[1].split("\\*");
					String weight="1";
					if (temp.length > 1) {
						valuePair[1]=temp[0];
						weight=temp[1];
					}
					classList.add(i * 3, valuePair[1]);
					classList.add(i * 3 + 1, valuePair[0]);
					String type=getType(valuePair[1]);
					if (type!=null && type.equals("c0")) {
						classList.add(i * 3 + 2, "-"+weight);
					}
					else if (valuePair[0].equals("x")) {
						classList.add(i * 3 + 2, "-1.0");
					}
					else {
						classList.add(i * 3 + 2, weight);
					}
				}
			}
//			if (!invPendingTagTails.isEmpty()) {
//				String prefix = null; // 待定cn前缀
//				int index = classList.indexOf("sc");
//				if (index > 0 || (index = classList.indexOf("c")) > 0)
//					prefix = classList.get(index - 1);
//				if (prefix == null)// 没有可以为待定cn给出的前缀，不考虑待定cn？？
//					return classList;
//				for (int i = invPendingTagTails.size() - 1; i >= 0; i--) {
//					classList.add(prefix + invPendingTagTails.get(i));
//					classList.add("cn");
//					classList.add("-1");
//				}
//				
			}
		return classList;
	}

	/**
	 *
	 * @param inputTags
	 *            从cmpp获取的tags, 可能含有多个
	 * @return
	 */
	ArrayList<String> mapTags(CMPPDataOtherField otherField,String sourceAlias, String docType) {
		long timegraph=0;
		IExpressContext<String, Object> expressContext=new DefaultContext<String, Object>();
		ArrayList<String> semiResult = new ArrayList<String>();
		String tags=otherField.getTags();
		String source = otherField.getSource();
		if ("spider2wap".equals(source)) {
			if (tags!=null && !tags.isEmpty()) {
				semiResult.add(tags);
				semiResult.add("s1");
				semiResult.add("1");
			}
			return semiResult;
		}
		if (("spider".equals(source) || "appeditor".equals(source)) && "时尚".equals(tags)) {
			return semiResult;
		}
		if (source!=null && !source.isEmpty()) {
			source=source.replace("|", "");
		}
//		if ("phvideo".equals(source)) {
//			semiResult=isOtherLabel(tags, "1");
//			if (semiResult!=null && !semiResult.isEmpty()) {
//				return semiResult;
//			}
//		}
		if ("wemedia".equals(source)) {//对自媒体的数据单独处理
			if (tags==null) {
				if (sourceAlias==null || sourceAlias.isEmpty()) {
					return semiResult;
				}
				semiResult.add(sourceAlias);
				semiResult.add("s1");
				semiResult.add("1");
				return semiResult;
			}
			long start=System.currentTimeMillis();
			semiResult=isOtherLabel(tags,"0.5");
			if(semiResult == null || semiResult.isEmpty()){//无对应c、sc
				ArrayList<String> tempResult =new ArrayList<String>();
				tempResult=parseTags(tags,otherField,expressContext);
				if (tempResult==null || tempResult.isEmpty()) {
					tempResult.add(tags);
					tempResult.add("cn");
					tempResult.add("0.5");
				}
				else {
					KnowledgeGraph kGraph=new KnowledgeGraph();
					try{
						for (int i = 0; i < tempResult.size(); i+=3) {
							if (kGraph.queryWord(tempResult.get(i)).get(0).getProperty("typelabel").equals("c0")){
								tempResult.set(i+2, "-0.5");
							}
							else {
								tempResult.set(i+2, "0.5");
							}
						}
					}
					finally{
						kGraph.shutdown();
					}
				}
				semiResult.addAll(tempResult);
			}
			long end =System.currentTimeMillis();
			timegraph += (end-start);
			if (sourceAlias!=null && !sourceAlias.isEmpty()) {
				semiResult.add(sourceAlias);
				semiResult.add("s1");
				semiResult.add("1");
			}
			return semiResult;
		}
		
		if (tags==null || tags.isEmpty()) {
			return semiResult;
		}
		try {
			//整个tags检验规则
			
			if (tags!=null && !tags.isEmpty()) {
				tags=tags.trim();
				//LOG.info("Starting tagsrule");
				//expressContext=null;
				expressContext.put("source", source);
				expressContext.put("tags", tags);
				expressContext.put("sourceAlias", sourceAlias);
				expressContext.put("docType", docType);
				String result=null;
				//System.out.println(TagsParser.TagsRuleList.toString());
				for(String temp:TagsRuleList){
					if (tags==null || tags.isEmpty()) {
						break;
					}
					if (temp!=null && !temp.isEmpty()) {
						result=(String)runner.execute(temp, expressContext, null, false, false);
						if (result!=null) {
							result=result.replace("||||", "||");
							tags=result;
							expressContext.put("tags", tags);
						}
					}
				}
				if (tags==null || tags.isEmpty()) {
					return semiResult;
				}
				//LOG.info("After tagsrule: tags="+ tags);
				String[] tagbeforeRule = tags.split("\\|\\|");
				String tagFirst="";
				for (int i = 0; i < tagbeforeRule.length; i++) {
					if (!tagbeforeRule[i].isEmpty()) {
						tagFirst=tagbeforeRule[i];
						break;
					}
				}
				//单个tag规则检验
				ArrayList<String> tagsFirstResult=new ArrayList<String>();
				//LOG.info("Starting tagrule");
				String[] tag = tags.split("\\|\\|");
				String weight="1";
				
				for (int i = 0; i < tag.length; i++) {
					expressContext.put("weight", weight);
					if (tag[i]==null || tag[i].isEmpty()) {
						i++;
					}
					tag[i]=tag[i].trim();
					expressContext.put("tag",tag[i]);
					result=null;
					for(String temp:TagRuleList){
						if (temp!=null && !temp.isEmpty()) {
							result=(String)runner.execute(temp, expressContext, null, false, false);
							if(result!=null){
								break;
							}
						}
					}
					if (result!=null) {
						if (result.equals("continue")) {
							weight="0.5";
							continue;
						}
						else{
							String[] re=result.split(",");
							for (int j = 0; j < re.length; j++) {
								semiResult.add(re[j]);
							}
						}
					}
					if (result == null) {//无规则
						long start=System.currentTimeMillis();
						ArrayList<String> mapResult = null;
						mapResult=parseTags(tag[i], otherField, expressContext);
						if (mapResult==null || mapResult.isEmpty()) {
							if (source!=null && "phvideo".equals(source.toLowerCase()) && i==0) {
								continue;
							}
							if (source!=null && (source.toLowerCase().contains("yidian") || source.toLowerCase().equals("todaytoutiao") || source.toLowerCase().equals("phvideo") 
									||source.toLowerCase().equals("ifengpush")|| !tag[i].contains("-"))){
								mapResult=isOtherLabel(tag[i], weight);
							}
						}
						if (mapResult!=null && !mapResult.isEmpty()) {
							if(i!=0){
								mapResult=NonFirstWeight(mapResult,tagsFirstResult,weight);//tags不是第一位的，进行加权处理
							}
							semiResult.addAll(mapResult);
						}
						else {
							if (source!=null && (source.toLowerCase().contains("yidian") || source.toLowerCase().equals("ifengpush") || source.toLowerCase().equals("todaytoutiao") 
									|| source.toLowerCase().equals("phvideo"))) {//今日头条和一点资讯处理方式相同
								semiResult.add(tag[i]);
								semiResult.add("x");
								semiResult.add("-1.0");
							}
						}
						long end=System.currentTimeMillis();
						timegraph+=(end-start);
					}
					
					expressContext.put("semiResult", semiResult);
					if (weight.equals("1")) {
						tagsFirstResult.addAll(semiResult);
					}
					if (tag[i].equals(tagFirst)) {
						weight="0.5";
					}
				}
				
				LOG.info("mapping result:"+semiResult);
			}	
		} catch (Exception e) {
			LOG.error("[ERROR] ", e);
			// TODO: handle exception
		}
		LOG.info("graph query used time:"+(timegraph)+"ms");
		return semiResult;
	}
	/**
	 *  对非第一位的tag进行加权处理
	 * @param mapResult 当前tag对应的映射或graph上溯结果
	 * @param tagsFirstResult 第一位tag对应的结果
	 * @return  处理之后的结果
	 */
	private ArrayList<String> NonFirstWeight(ArrayList<String> mapResult,ArrayList<String> tagsFirstResult,String weight){
		boolean flag=false;
		KnowledgeGraph kGraph=new KnowledgeGraph();
		try{
			for (int j = 0; j < mapResult.size(); j+=3) {//判断是否和tags第一个词映射的c和sc相同
				for (int j2 = 0; j2 < tagsFirstResult.size(); j2+=3) {
					if (mapResult.get(j).equals(tagsFirstResult.get(j2))) {
						flag=true;
						mapResult.remove(j+2);
						mapResult.remove(j+1);
						mapResult.remove(j);
						for (int k = 0; k < mapResult.size(); k+=3) {//有相同的 则将权重改为1
							ArrayList<Vertex> vertexList=kGraph.queryWord(mapResult.get(k));
							if (vertexList==null || vertexList.isEmpty()) {
								continue;
							}
							for (Vertex v:vertexList) {
								if (v.getProperty("typelabel").equals("c0")) {
									mapResult.set(k+2, "-1.0");
								}
								else{
									mapResult.set(k+2, "1");
								}
							}
						}
						break;
					}
				}
			}
		}
		finally{
			kGraph.shutdown();
		}
		if (!flag && (mapResult.get(2).equals("1") || 
				mapResult.get(2).equals("-1.0") || mapResult.get(2).equals("-1"))) {//第二个tag进行降权至0.5处理；映射表中权重如果不是"1",则进行过降权处理，就不需要再对其进行权重处理了
			for (int j = 0; j < mapResult.size(); j+=3) {
				String type=getType(mapResult.get(j));
				if ((type!=null && type.equals("c0")) || ("-1.0").equals(mapResult.get(j+2)) || ("-1").equals(mapResult.get(j+2))){
					mapResult.set(j+2, "-"+weight);
				}
				else{
					mapResult.set(j+2, weight);
				}
			}
		}
		return mapResult;
	}	
	/**
	 *  str是否是已有的c类，sc类或cn类
	 * @param str 待判定的字符串
	 * @return  判定后需补充的映射结果
	 */
	protected ArrayList<String> isOtherLabel(String str,String weight){
		if(str == null || str.isEmpty())
			return null;
		KnowledgeGraph kGraph=new KnowledgeGraph();
		ArrayList<String> semiResult = new ArrayList<String>();
		ArrayList<Vertex> vertexList=new ArrayList<Vertex>();
		try{
			vertexList=kGraph.queryUpTraverse(str);
		}
		finally{
			kGraph.shutdown();
		}
		if (vertexList==null || vertexList.isEmpty()) {
			return semiResult;
		}
		for (Vertex v:vertexList) {
			semiResult.add(v.getProperty("displayword").toString());
			semiResult.add(v.getProperty("type").toString());
			if ("c0".equals(v.getProperty("typelabel"))) {
				semiResult.add("-"+weight);
			}
			else {
				semiResult.add(weight);
			}
		}
		return semiResult;

	}
	/**
	 *  获取待查字符串的类型：c0、c1、sc等
	 */
	protected String getType(String word){
		if(word == null || word.isEmpty())
			return null;
		KnowledgeGraph kGraph=new KnowledgeGraph();
		ArrayList<Vertex> vertexList=new ArrayList<Vertex>();
		try{
			vertexList=kGraph.queryWord(word);
		}
		finally{
			kGraph.shutdown();
		}
		if (vertexList.isEmpty()|| vertexList==null) {
			return null;
		}
		return vertexList.get(0).getProperty("typelabel");

	}
	
	public String getlocMap(String word){
		if(locMap.get(word)==null || locMap.get(word).isEmpty()){
			return null;
		}
		else {
			return locMap.get(word);
		}
	}
	public void addTagsmapping(){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream("E:\\FeatureProject\\newchanneltags(2).txt"),"UTF-8"));
			BufferedWriter bw=new BufferedWriter(new FileWriter("E:\\FeatureProject\\addtagsmapping.txt"));
			String str;
			while((str=reader.readLine())!=null){
				String[] words=str.split("\\|!\\|");
				for (int i = 0; i < words.length; i++) {
					if(words[i].contains("tags")){
						String word=words[i].split("=")[1];
						if (!mapTable.containsKey(word)) {
							bw.write(str);
							bw.newLine();
						}
					}
				}
			}
			reader.close();
			bw.close();
		}catch (Exception e) {
			// TODO: handle exception
			
		}
	}
	
}
