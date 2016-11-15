package com.ifeng.iRecommend.featureEngineering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.iRecommend.featureEngineering.OrientDB.KnowledgeGraph;
import com.ifeng.iRecommend.wuyg.commonData.Update.UpdateActionType;
import com.ifeng.iRecommend.wuyg.commonData.Update.commonDataUpdateConfig;
import com.ifeng.iRecommend.wuyg.commonData.Update.CommonSubDataWord.WordState;
import com.ifeng.iRecommend.wuyg.commonData.Update.publish.HotWordPublisher;
import com.tinkerpop.blueprints.Vertex;
/**
 * 
 * <PRE>
 * 作用 : 根据other字段分析得到文章特征list
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
 *          1.0          2015年7月15日        lixiao          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class CMPPDataOtherParser {
	private static final Log LOG = LogFactory.getLog("CMPPDataOtherParser");
	/**
	 * 根据other字段和外部source字段得到初步features
	 * @param otherField
	 * @param sourceAlias
	 * @return
	 */
	protected static ArrayList<String> OtherParser(CMPPDataOtherField otherField, String sourceAlias, String docType){
		if(otherField == null)
			return null;
//		ArrayList<String> UpperClassResult = new ArrayList<String>();
//		
//		if(otherField.getUpperClass() == 2 || otherField.getUpperClass() == 6){
//			UpperClassResult.add("财经");
//			UpperClassResult.add("c");
//			UpperClassResult.add("-1.0");
//		}
//		else if(otherField.getUpperClass() == 1){
//			UpperClassResult.add("娱乐");
//			UpperClassResult.add("c");
//			UpperClassResult.add("-1.0");
//		}
//		else if(otherField.getUpperClass() == 4){
//			UpperClassResult.add("体育");
//			UpperClassResult.add("c");
//			UpperClassResult.add("-1.0");
//		}
//		else if(otherField.getUpperClass() == 8){
//			UpperClassResult.add("科技");
//			UpperClassResult.add("c");
//			UpperClassResult.add("-1.0");
//		}
//		else if(otherField.getUpperClass() == 0){
//			UpperClassResult.add("时政");
//			UpperClassResult.add("c");
//			UpperClassResult.add("-1.0");
//		}
//		else if(otherField.getUpperClass() == 3){
//			UpperClassResult.add("时尚");
//			UpperClassResult.add("c");
//			UpperClassResult.add("-1.0");
//		}
//		else if(otherField.getUpperClass() == 10){
//			UpperClassResult.add("教育");
//			UpperClassResult.add("c");
//			UpperClassResult.add("-1.0");
//		}
//		else if(otherField.getUpperClass() == 11){
//			UpperClassResult.add("健康");
//			UpperClassResult.add("c");
//			UpperClassResult.add("-1.0");
//		}
//		if(source != null && source.equals("ignore"))
//			return TagsParser.getInstance().parseTags(wechatSource, null);
		ArrayList<String> semiResult = TagsParser.getInstance().mapTags(otherField,sourceAlias,docType);
		String source=otherField.getSource();
		if (source!=null && (source.equals("original") || source.equals("ignore"))) {
			return semiResult;
		}
		semiResult = modifyResult(semiResult,docType);
		
		// 利用bn等其他内容补充tagsParser的结果
		semiResult = supplyTagsResult(semiResult, otherField);
		ArrayList<String> finalResult = modifyResult(semiResult,docType);
		ArrayList<String> hotWord=new ArrayList<String>();
		for (int i = 0; i < finalResult.size(); i+=3) {
			if (finalResult.get(i+1).equals("e") && finalResult.get(i).length() > 1 && !hotWord.contains("_")) {
				hotWord.add(finalResult.get(i).toLowerCase());
			}
			if (finalResult.get(i+1).equals("cn")) {
				//如果cn和地名相同，则不加入数据库，并且把cn改为loc 
				String loc=TagsParser.getInstance().getlocMap(finalResult.get(i));
				if (loc==null) {
					AddcnTodb(finalResult.get(i));
				}
				else {
					finalResult.set(i, loc);
					finalResult.set(i+1, "loc");
				}
			}
		}
		if (hotWord!=null && !hotWord.isEmpty()) {//将专题事件加入热词库中
			HotWordPublisher hotWordPublisher=new HotWordPublisher(
					commonDataUpdateConfig.hotWordPattern,
					commonDataUpdateConfig.hotWordMessageChannel);
//			String message = hotWordPublisher.updateWord_Del(hotWord, WordState.read.name());
//			hotWordPublisher.publish(message, UpdateActionType.DEL_WORD.name(),
//					hotWord,WordState.read.name());
			
			String message = hotWordPublisher.updateWord_Add(hotWord,WordState.read.name(), new ArrayList<String>(),null,false);
					//updateWord_Add(hotWord, null,new ArrayList<String>(),false);
			
			hotWordPublisher.publish(message, UpdateActionType.ADD_WORD.name(),
					hotWord,WordState.read.name());
			LOG.info("add HotEvent:"+hotWord);
		}
		//北京晚报和房产的本地数据 特征进行降权到0.5
		if (("chaoslocal".equalsIgnoreCase(source) && sourceAlias!=null && sourceAlias.equals("北京晚报")) ||
				("ifengpc_error".equalsIgnoreCase(source) && otherField.getLoc()!=null)) {
			for(int i = 0;i < finalResult.size(); i+=3){
				if (finalResult.get(i+2).contains("-")) {
					finalResult.set(i+2, "-0.5");
				}
				else {
					finalResult.set(i+2, "0.5");
				}
			}
		}
//		if(finalResult == null)
//			return null;
//		finalResult.addAll(0,UpperClassResult);

		return finalResult;
	}
	/**
	 * 根据Other字段中bn，ref，lt，ru或referurl，loc对已有的映射结果做补充
	 * 
	 * @param semiResult
	 *            从tags得到的文章特征list
	 */
	private static ArrayList<String> supplyTagsResult(ArrayList<String> semiResult,
			CMPPDataOtherField otherField) {
		if (semiResult == null)
			semiResult = new ArrayList<String>();
		String ref = otherField.getRef();
		String lt_head = otherField.getLt();
		String loc=otherField.getLoc();
		String source=otherField.getSource();
		if (loc!=null && !loc.isEmpty()) {//本地数据补充loc
			if (loc.contains("[") || loc.contains("{")) {
				loc=loc.replaceAll("[\\[\\]\\{\\}\"]", "");
				loc=loc.trim();
				String[] locarr=loc.split(",");
				for (int i = 0; i < locarr.length; i++) {
					if (locarr[i].split(":").length==2) {
						String locname=locarr[i].split(":")[1];
						if (locname!=null && !locname.isEmpty()) {
							semiResult.add(locname);
							semiResult.add("loc");
							if("chaoslocal".equals(source)){
								semiResult.add("0.5");
							}
							else {
								semiResult.add("1");
							}
						}
					}
						
					}
			}
			else {
				semiResult.add(loc);
				semiResult.add("loc");
				if("chaoslocal".equals(source)){
					semiResult.add("0.5");
				}
				else {
					semiResult.add("1");
				}
			}
			}
			
		if (parseSpecialPattern(semiResult, lt_head)) {// 如果lt被使用过
			lt_head = null;
		}
		if (otherField.isSpecial()) {
			handleRefOrLtSpecial(semiResult, lt_head,
					otherField.getUpperClass());
			handleRefOrLtSpecial(semiResult, ref, otherField.getUpperClass());
			// if (lt_head != null && !lt_head.equals(otherField.getRef()))
			// {
			// handleRefOrLtSpecial(semiResult, otherField.getRef(),
			// otherField.getUpperClass());
			// // 写事件别名文件
			// }
		} else {
			handleRefOrLtNoSpecial(semiResult, lt_head,
					otherField.getUpperClass(), "0.5");
			handleRefOrLtNoSpecial(semiResult, otherField.getRef(),
					otherField.getUpperClass(), "0.5");
			handleRefOrLtNoSpecial(semiResult, otherField.getBn(),
					otherField.getUpperClass(), "-0.5");
		}

		// String bn = otherField.getBn();
		// if (bn!= null && semiResult.indexOf(bn) < 0 &&
		// listSubLabel(semiResult, bn) < 0) {
		// semiResult.add(bn);
		// semiResult.add("cn");
		// semiResult.add("-1");
		// }
		return semiResult;
	}

	private static boolean parseSpecialPattern(ArrayList<String> semiResult,
			String lt_head) {
		Pattern p = Pattern.compile("第?([0-9]+|[一二三四五六七八九十]+)期");
		if (lt_head != null) {
			Matcher m = p.matcher(lt_head);
			StringBuffer sb1 = new StringBuffer();
			StringBuffer sb2 = new StringBuffer();
			if (m.find()) {
				m.appendReplacement(sb1, "");
				m.appendTail(sb2);
				String str = sb1.toString();
				int i;
				if ((i = semiResult.indexOf(str)) >= 0
						||(i = findSubLabelPostion(semiResult, str)) >=0) {
					semiResult.set(i + 1, "s1");
					semiResult.set(i + 2, "1");
				} else {
					semiResult.add(str);
					semiResult.add("s1");
					semiResult.add("1");
				}
				str = sb2.toString().replaceAll("[：:\\-]+", "");
				if (str == null || str.isEmpty())
					return true;
				if ((i = semiResult.indexOf(str)) >= 0
						|| (i = findSubLabelPostion(semiResult, str)) >= 0) {
					semiResult.set(i + 1, "e");
					semiResult.set(i + 2, "1");
				} else {
					semiResult.add(sb2.toString().replaceAll("[：:\\-]+", ""));
					semiResult.add("e");
					semiResult.add("1");
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 文章是专题时
	 * 
	 * @param semiResult
	 *            tags映射的初步结果
	 * @param str
	 *            ref或者lt头部
	 * @param c
	 * 				根据tags得出的类别信息 具体见 CMPPDataOtherField.java
	 */

	private static void handleRefOrLtSpecial(ArrayList<String> semiResult, String str,
			int c) {
		if (str == null || str.isEmpty())
			return;

		ArrayList<String> candidateList = TagsParser.getInstance().isOtherLabel(str,"0.5");
		int p;
		if ((p = semiResult.indexOf(str)) >= 0
				&& !semiResult.get(p + 1).equals("c")) { // 如果认定为专题事件
			semiResult.set(p + 2, "1");
		} else if (findSubLabelPostion(semiResult, str) < 0) {
			if (c == 0) {
				semiResult.add(str);
				semiResult.add("e");
				semiResult.add("0.5");
			} else if (candidateList != null && !candidateList.isEmpty()) {
				semiResult.addAll(candidateList);
			}
			else {
				semiResult.add(str);
				semiResult.add("cn");
				semiResult.add("0.5");
			}
		}
		 
		Pattern p1 = Pattern.compile("《.{1,12}》");
		Matcher m = p1.matcher(str);

		if (m.find()) {
			semiResult.add(m.group().replaceAll("《|》", ""));
			semiResult.add("kb");
			semiResult.add("0.5");
		}

	}

	/**
	 * 文章不是专题时
	 * 
	 * @param semiResult
	 *            tags映射的初步结果
	 * @param str
	 *            ref或者lt头部
	 * @param c
	 *            新闻类别 -1为default值
	 * @param str_weight
	 *            新增补充信息的可读权重，一般bn给不可读-1
	 * @param type
	 * 				新增补充信息的类别标志
	 */
	private static void handleRefOrLtNoSpecial(ArrayList<String> semiResult,
			String str, int c, String str_weight) {
		if (str == null || str.isEmpty())
			return;
		 String type = "cn";
		if(c == 12) //读书
			type = "s1";
		ArrayList<String> candidateList = TagsParser.getInstance().isOtherLabel(str,"0.5");
		int p = semiResult.indexOf(str);
		if(candidateList != null && !candidateList.isEmpty())
			semiResult.addAll(candidateList);
		
		else if (p >= 0 || findSubLabelPostion(semiResult, str) >= 0) {
			String classLabel = semiResult.get(p + 1);
			if (classLabel.equals("cn"))
				semiResult.set(p + 2, "1");
		} else if (str.length() > 6 && (c == 0)) {
			semiResult.add(str);
			semiResult.add("e");
			semiResult.add("0.5");
		} else if (str.length() >= 2) {
			if ((str.equals("内地") || str.equals("欧美") || str.equals("日韩") || str
					.equals("内地")) && c == 1 && semiResult.contains("电影"))
				semiResult.add(str + "电影");
			else
				semiResult.add(str);
			semiResult.add(type);
			semiResult.add(str_weight);
		}
	}


	/**
	 * 
	 * @param semiResult
	 *            待修改的映射结果
	 * @param str
	 *            作为补充的字符串，ref或lt头部
	 * @return 在待修改的结果list中找到与str互相包含的情形时的位置
	 */
	protected static int findSubLabelPostion(ArrayList<String> semiResult, String str) {
		for (int i = 0; i < semiResult.size() - 2; i += 3) {
			String temp = semiResult.get(i).toLowerCase();
			if (((temp.contains(str.toLowerCase()) || str.toLowerCase().contains(temp)) && (semiResult.get(i + 1).equals("c")
					|| semiResult.get(i + 1).equals("sc") || semiResult.get(
					i + 1).equals("s1") || semiResult.get(i + 1).equals("e") ))
					|| (str.toLowerCase().contains(temp)
					&& semiResult.get(i + 1).equals("cn")
					&& semiResult.get(i + 2).equals("1"))) {// list元素包含str
				// semiResult.set(i, str);
				return i;
			} else if (temp.contains(str.toLowerCase())) {
				semiResult.set(i, str);
				semiResult.set(i + 2, "1");
				return i;
			} else if (str.toLowerCase().contains(temp)
					&& !semiResult.get(i + 1).equals("c")
					&& !semiResult.get(i + 1).equals("sc")) {// str元素包含list
				semiResult.set(i, str);
				semiResult.set(i + 2, "1");
				return i;
			}
		}
		return -1;
	}

	private static ArrayList<String> modifyResult(ArrayList<String> semiResult,String docType) {
		if (semiResult == null || semiResult.isEmpty())
			return semiResult;
		Map<String, Double> combineMap =  new HashMap<String, Double>();// 利用map去除不同映射结果的重复内容
		ArrayList<String> upperList  = new ArrayList<String>(); //保存更上层c
		int j = 0;
		int needRemove = -1;
		boolean flag=false;
//		if (semiResult.contains("军事") || semiResult.contains("科学探索")||semiResult.contains("家居")
//				||semiResult.contains("手机") || semiResult.contains("平板电脑") || semiResult.contains("笔记本")
//				|| semiResult.contains("美女")) {
//			flag=true;
//		}
		while (j < semiResult.size() - 2) {
			StringBuffer mappingTuple = new StringBuffer("");
//			if(docType!=null && !docType.isEmpty() && docType.equals("slide") && !flag){
//				if((semiResult.get(j+1).equals("c") && Double.valueOf(semiResult.get(j + 2)) >= 0) 
//						|| semiResult.get(j+1).equals("sc") ){
//					j += 3;
//					continue;
//			}		
//			}
			if(semiResult.get(j).contains("?") || semiResult.get(j).contains("!") || semiResult.get(j).contains("？") || semiResult.get(j).contains("！")){
				j+=3;
				continue;
			}
			double weight = 0.0;
			Pattern p1 = Pattern.compile("《.{1,12}》?");
			Matcher m1 = p1.matcher(semiResult.get(j));

			Pattern p2 = Pattern
					.compile("^((20)?[0-9]{2}(年)?)?第?([0-9]+|[一二三四五六七八九十]+)届|^((20)?[0-9]{2}(年)?[-\\/])?(20)?[0-9]{2}(年)?(赛季)?");
			Matcher m2 = p2.matcher(semiResult.get(j));

			Pattern p3 = Pattern.compile("【.{1,10}】?");
			Matcher m3 = p3.matcher(semiResult.get(j));
			
			Pattern p4=Pattern.compile("^[0-9]+.[0-9]+");
			Matcher m4=p4.matcher(semiResult.get(j));
			if (m1.find()) {
				semiResult.set(j, m1.group().replaceAll("《|》", ""));
				semiResult.set(j + 1, "kb");
				semiResult.set(j + 2, "1");
			}else if (m4.find() && !m4.group().isEmpty() && semiResult.contains("体育")) {
				StringBuffer sb=new StringBuffer();
				m4.appendReplacement(sb, "");
				m4.appendTail(sb);
				if (sb.length()>1) {
					semiResult.set(j, sb.toString());
				}
				else {
					j+=3;
					continue;
				}
			} 
			else if (m2.find() && !m2.group().isEmpty() && m2.group().length() > 1) {
				StringBuffer sb = new StringBuffer();
				m2.appendReplacement(sb, "");
				while (m2.find() && !m2.group().isEmpty()) {
					m2.appendReplacement(sb, "");
				}
				m2.appendTail(sb);
				if(sb.length() > 1){
					semiResult.set(j, sb.toString());
					// semiResult.set(j + 1, "cn");
					semiResult.set(j + 2, "1");
				}
				if (m2.matches()) {
					j+=3;
					continue;
				}
			} else if (m3.find()) {
				String str=m3.group().replaceAll("【|】", "");
				semiResult.set(j, str);
				semiResult.set(j + 1, "cn");
				semiResult.set(j + 2, "1");
			} else {
				String str = semiResult.get(j);
				if(parseSpecialPattern(semiResult, str)){
					needRemove = j;
				}
			}
			if (semiResult.get(j).isEmpty()) {
				j += 3;
				continue;
			}
			if (semiResult.get(j).contains("合作") || semiResult.get(j).equals("高清图")||semiResult.get(j).equals("高清")
					|| semiResult.get(j).contains("新闻客户端") || semiResult.get(j).contains("手机凤凰网")||semiResult.get(j).contains("手凤")
					|| semiResult.get(j).equals("幻灯图")) {
				j += 3;
				continue;
			}
			if(semiResult.get(j).equals("图库")){
				semiResult.set(j, "高清图库");
			}
			if(semiResult.get(j + 1).equals("c") && Double.valueOf(semiResult.get(j + 2)) >= 0){
				try{
					ArrayList<String> tempList=new ArrayList<String>();
					tempList=TagsParser.getInstance().isOtherLabel(semiResult.get(j),semiResult.get(j+2));
					upperList.addAll(tempList);
				} catch (NullPointerException e){
					LOG.error(e);
				}
			}
			mappingTuple.append(semiResult.get(j)).append("*");
			mappingTuple.append(semiResult.get(j + 1));
			try {
				weight = Double.valueOf(semiResult.get(j + 2));
			} catch (NumberFormatException e){
				LOG.error(e);
			}
			String key = mappingTuple.toString();
			Double currWeight = combineMap.get(key);
			if(currWeight == null)
				combineMap.put(key, weight);
			else if(Math.abs(currWeight) < Math.abs(weight)){
				combineMap.put(key, weight);
			}
			j += 3;
		}
		semiResult.addAll(upperList);
		if(needRemove > 0){
			semiResult.remove(needRemove + 2);
			semiResult.remove(needRemove + 1);
			semiResult.remove(needRemove);
		}
		
		for(int i = 0; i < upperList.size() - 2; i+= 3){
			StringBuffer mappingTuple = new StringBuffer();
			double weight = 0.0;
			mappingTuple.append(upperList.get(i)).append("*");
			mappingTuple.append(upperList.get(i + 1));
			try {
				weight = Double.valueOf(upperList.get(i + 2));
			} catch (NumberFormatException e){
				LOG.error(e);
			}
			String key = mappingTuple.toString();
			Double currWeight = combineMap.get(key);
			if(currWeight == null)
				combineMap.put(key, weight);
			else if(Math.abs(currWeight) < Math.abs(weight)){
				combineMap.put(key, weight);
			}
		}

		ArrayList<String> resultList = new ArrayList<String>();
		if (combineMap == null || combineMap.isEmpty())
			return resultList;

		int c_count = 0;
		int sc_count = 0;
		int cn_count = 0;
		int e_count = 0;
		int t_count = 0;
		int s_count = 0;
		int s1_count = 0;

		// String flag = ""; // 是否含有notopic或者null的标志位
		Iterator<Map.Entry<String, Double>> it = combineMap.entrySet().iterator();
		int p;
		while (it.hasNext()) {
			Entry<String, Double> temp = it.next();
			String key = temp.getKey();
			if (temp != null) {
				if (key.contains("*c")) {
					addListItemInPosition(key, resultList, 0, temp.getValue());
					c_count++;
				} else if (key.contains("*sc")) {
					p = c_count * 3;
					addListItemInPosition(key, resultList, p, temp.getValue());
					sc_count++;
				} else if (key.contains("*cn")) {
					p = c_count * 3 + sc_count * 3;
					addListItemInPosition(key, resultList, p, temp.getValue());
					cn_count++;
				} else if (key.contains("*e")) {
					p = c_count * 3 + sc_count * 3 + cn_count * 3;
					addListItemInPosition(key, resultList, p, temp.getValue());
					e_count++;
				} else if (key.contains("*t")) {
					p = c_count * 3 + sc_count * 3 + cn_count * 3 + e_count * 3;
					addListItemInPosition(key, resultList, p, temp.getValue());
					t_count++;
				} else if (key.contains("*s")) {
					p = c_count * 3 + sc_count * 3 + cn_count * 3 + e_count * 3
							+ t_count * 3;
					addListItemInPosition(key, resultList, p, temp.getValue());
					s_count++;
				} else if (key.contains("*s1")) {
					p = c_count * 3 + sc_count * 3 + cn_count * 3 + e_count * 3
							+ t_count * 3 + s_count * 3;
					addListItemInPosition(key, resultList, p, temp.getValue());
					s1_count++;
				} else {
					p = c_count * 3 + sc_count * 3 + cn_count * 3 + e_count * 3
							+ t_count * 3 + s_count * 3 + s1_count * 3;
					addListItemInPosition(key, resultList, p, temp.getValue());
				}
			}
		}
		return resultList;
	}

	private static void addListItemInPosition(String origin,
			ArrayList<String> resultList, int p, double weight) {
		String[] tuples = origin.split("\\*", 2);
		for (int i = 0; i < tuples.length; i++) {
			resultList.add(p + i, tuples[i]);
		}
		resultList.add(p + tuples.length, String.valueOf(weight));
	}
	private static void AddcnTodb(String str){
		KnowledgeGraph kgraph=new KnowledgeGraph();
		ArrayList<Vertex> vertexlist=new ArrayList<Vertex>();
		vertexlist=kgraph.queryWord(str);
		if (vertexlist==null || vertexlist.isEmpty()) {
			LOG.info("add cn="+str+"to knowledgeGraph");
			kgraph.addVertex(str, "cn");
		}
		kgraph.shutdown();
	}
	public static void main(String[] args) {
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					new FileInputStream("E:\\FeatureProject\\test.txt"),"UTF-8"));
//			BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("E:\\FeatureProject\\mapresult.txt"),"UTF-8"));
//			
////			CMPPDataOtherField f = new CMPPDataOtherField(
////					"source=ignoreYidian|!|channel=news|!|hotlevel=B|!|tags=O2O||BAT||电商新知||互联网|!|qualitylevel=D ");
//			String s=null;
//			while((s=reader.readLine())!=null && !s.isEmpty() ){
//				CMPPDataOtherField f = new CMPPDataOtherField(s);
//				ArrayList<String> result = OtherParser(f, "手机腾讯网");
//				bw.write(f.getTags()+"		"+result);
//				System.out.println(result);
//			}
//			reader.close();
//			bw.close();
//
//		} catch (Exception e) {
//			// TODO: handle exception
//			LOG.error("[ERROR] ", e);
//		}
		
		CMPPDataOtherField f = new CMPPDataOtherField(
				"source=ifengpc|!|channel=sports|!|tags=体育-篮球风云-NBA|!|ref=6 5.5 威斯布鲁克|!|qualitylevel=B|!|reviewStatus=auto|!|referurl=http://sports.ifeng.com/nba/team/player/3016|!|lt=俄克拉荷马城雷霆-拉塞尔-威斯布鲁克信息");		
		System.out.println(f.getBn());
		System.out.println(f.getRef());
		System.out.println(f.getLt());
		System.out.println(f.isSpecial());
		long time1=System.currentTimeMillis();
		ArrayList<String> result = OtherParser(f, "男人装","doc");
		long time2=System.currentTimeMillis();
		System.out.println(time2-time1);
		System.out.println(result);

//		experiment();
	}

	public static void experiment() {
		BufferedReader rd = null;
		FileWriter resultWriter = null;
		try {
			rd = new BufferedReader(new InputStreamReader(new FileInputStream(
					"D:\\classify\\new\\v17_others_test.txt"), "UTF8"));
			resultWriter = new FileWriter(
					"D:\\classify\\new\\v17_others_result_1204_new", false);
			String line = null;
			int l = 0;
			while ((line = rd.readLine()) != null) {
				l++;
				CMPPDataOtherField f = new CMPPDataOtherField(line);
				ArrayList<String> result = OtherParser(f, null,"slide");
				StringBuffer sb = new StringBuffer();
				sb.append(l);
				sb.append("\t");
				sb.append(line);
				sb.append("\t");
				sb.append(result);
				sb.append("\n");
				resultWriter.write(sb.toString());
				resultWriter.flush();
			}
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
					resultWriter.flush();
					resultWriter.close();
				} catch (IOException e) {
					LOG.error("[ERROR] ", e);
				}
			}
		}
	}

}
