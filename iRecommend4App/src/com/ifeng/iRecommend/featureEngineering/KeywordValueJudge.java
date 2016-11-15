package com.ifeng.iRecommend.featureEngineering;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.DocWordFilter;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.EntityLibQuery;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.KeywordExtract;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.CustomWordUpdate;

/**
 * 
 * <PRE>
 * 作用 : 对关键词提取出来的词进行权重修改
 *   
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
 *          1.0          2015-9-23         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class KeywordValueJudge
{
	static Logger LOG = Logger.getLogger(KeywordValueJudge.class);
	KeywordValueJudge()
	{
		EntityLibQuery.init();
	}

	private static KeywordValueJudge instance = new KeywordValueJudge();

	public static KeywordValueJudge getKVJudgeInstance()
	{
		return instance;
	}

	public KeywordExtract getKeywordExtract(String s_title, String s_content, ArrayList<String> categoryList)
	{
		KeywordExtract ke = new KeywordExtract(true, s_title, s_content, categoryList);
		return ke;
	}
	
	public Map<String, Set<String>> getEtMap(KeywordExtract ke)
	{
		List<String> list = ke.get_weighted_keywords();
		DocWordFilter filterDoc = ke.get_filtered_doc();
		Map<String, Set<String>> etMap = filterDoc.getEt_class_map();
		return etMap;
	}
	
	public Map<String, String> getLevelMap(KeywordExtract ke)
	{
		List<String> list = ke.get_weighted_keywords();
		DocWordFilter filterDoc = ke.get_filtered_doc();
		Map<String, String> levelMap = filterDoc.getWord_level_map();
		return levelMap;
	}
	
	public List<String> getFilterFeature(String logstr, Map<String, Set<String>> etMap, Map<String, String> levelMap, List<String> featureList, String s_title, ArrayList<String> category)
	{
		if(etMap == null || featureList == null)
		{
			LOG.info("[INFO] etMap or featureList is null, return.");
			return featureList;
		}
		if(levelMap == null)
		{
			LOG.info("levelMap is null.");
		}
			
		LOG.info("[INFO] category for filter feature is "+ category);
		String category1 = null;
		String category2 = null;
		if(category == null)
		{
			category1 = "其他";
			category2 = "其他";
		}
		else if(category.size() == 1)
		{
			category1 = category.get(0);
			category2 = "其他";
		}
		else
		{
			category1 = category.get(0);
			category2 = category.get(1);
		}
			
		List<String> resultList = new ArrayList<String>();
		List<String> tempFeature = getCateWeight(featureList, levelMap, category);
//		System.out.println(tempFeature);
		for (int i = 0; i < tempFeature.size() - 2; i += 3)
		{
			String word = tempFeature.get(i);
			String type = tempFeature.get(i+1);
			String value = tempFeature.get(i+2);
			if (etMap != null && etMap.containsKey(word + "_" + type))
			{
				// 保留分类匹配的et，
				if ((category1.equals("其他")&&category2.equals("其他")) ||etMap.get(word + "_" + type).contains(category1) || etMap.get(word + "_" + type).contains(category2) || etMap.get(word + "_" + type).contains("全部"))//
				{
					resultList.add(word);
					resultList.add(type);
					resultList.add(value);
				}
				//长度3且出现在标题中
				else if(commenFuncs.stringLength(word) >= 2&&s_title.toLowerCase().contains(word))
				{
					resultList.add(word);
					resultList.add(type);
					resultList.add(value);
				}
				//大于等于4个字的et且非地点术语
				else if(commenFuncs.stringLength(word) >= 4&&levelMap.get(word)!=null&&!levelMap.get(word).contains("地点术语"))
				{
					resultList.add(word);
					resultList.add(type);
					resultList.add(value);
				}
				// 其他的et删掉
				else
				{}
			}
			else//其余的内容原样保存
			{
				resultList.add(word);
				resultList.add(type);
				resultList.add(value);
			}
		}
		if(category1.equals("其他")&&category2.equals("其他"))
				LOG.info(logstr+"[INFO] Category is null, we get filterlist is "+resultList);
		return resultList;
	}
	
	public List<String> getCateWeight(List<String> featureList,  Map<String, String> levelMap, ArrayList<String> category){
		List<String> resultList = new ArrayList<String>();
		for (int i = 0; i < featureList.size() - 2; i += 3)
		{
			String word = featureList.get(i);
			String type = featureList.get(i+1);
			double value = Double.valueOf(featureList.get(i+2));
			if(category!=null&&category.contains("社会")&&levelMap.get(word)!=null&&!levelMap.get(word).contains("地点术语"))
			{
				if(value > 0.5)
					value = 0.5;
			}
			
			resultList.add(word);
			resultList.add(type);
			resultList.add(String.valueOf(value));
		}
		return resultList;
	}
	/**
	 * 
	 * @param s_title
	 * @param s_content
	 * @param category
	 */
	public List<String> getPreKeyword(KeywordExtract ke)
	{	
		ke.get_filtered_doc().getFilteredDoc();
		List<String> list = ke.get_weighted_keywords();
//		System.out.println("list is "+list);
		Map<String, Set<String>> etMap = getEtMap(ke);
		if (list == null)
			return null;
		List<String> resultList = new ArrayList<String>();
		for (int i = 0; i < list.size() - 2; i += 3)
		{
			if (etMap != null && etMap.containsKey(list.get(i) + "_" + list.get(i + 1)))
			{
				resultList.add(list.get(i));
				resultList.add(list.get(i + 1));
				resultList.add(list.get(i + 2));
			}
			// 所有的x变成负值
			else if (list.get(i + 1).equals("x"))
			{
				resultList.add(list.get(i));
				resultList.add(list.get(i + 1));
				resultList.add("-" + list.get(i + 2));
			}
			// kb，ks，kq，s1不做处理
			else if (list.get(i + 1).equals("kb") || list.get(i + 1).equals("ks") || list.get(i + 1).equals("kq") || list.get(i + 1).equals("s1"))
			{
				resultList.add(list.get(i));
				resultList.add(list.get(i + 1));
				resultList.add(list.get(i + 2));
			}
			// 全部的nr，nt，nz，n，e，ns都成负值。
			// 注：所有的e改成ne
			else if (list.get(i + 1).equals("nx") || list.get(i + 1).equals("nr") || list.get(i + 1).equals("nt") || list.get(i + 1).equals("nz") || list.get(i + 1).equals("n") || list.get(i + 1).equals("ns")
					|| list.get(i + 1).equals("e")|| list.get(i + 1).equals("k"))
			{
				resultList.add(list.get(i));
				if (list.get(i + 1).equals("e"))
					resultList.add("ne");
				else
					resultList.add(list.get(i + 1));
				resultList.add("-" + list.get(i + 2));
			}
		}
		return resultList;
	}
}
