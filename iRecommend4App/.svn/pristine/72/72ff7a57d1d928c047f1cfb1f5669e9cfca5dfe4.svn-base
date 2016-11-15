package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;

/**
 * <PRE>
 * 作用 : 
 *   词类，词组成句子，词包括词性等属性
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
 *          1.0          2015年10月10日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class Word implements IWord {
	/**
	 * 单词值
	 */
	public String value;
	/**
	 * 单词的标签，比如“n”
	 */
	public String label;

	public String toString() {
		return value + '_' + label;
	}

	public Word(String value, String label) {
		this.value = value;
		this.label = label;
	}

	/**
	 * @Title: create
	 * @Description: 通过参数构造一个单词
	 * @author liu_yi
	 * @param wordLabelStr
	 *        example: 澳大利亚_x
	 * @return 一个单词
	 * @throws
	 */
	public static Word create(String wordStr) {
		if (wordStr == null)
			return null;
		int cutIndex = wordStr.lastIndexOf('_');
		if (cutIndex <= 0 || cutIndex == wordStr.length() - 1) {
			return null;
		}

		return new Word(wordStr.substring(0, cutIndex),
				wordStr.substring(cutIndex + 1));
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}
		
	public static void main(String[] agrs) {
		//itemf item = query.queryItemF("15678");
		//System.out.println(item.getSplitContent());
		// 词语正则
		Pattern pattern = Pattern.compile("([^\\s]+_[a-z]+)|([^\\s_w]+\\s+[^\\s]+_[a-z]+)");
		String test = "  _w 布里斯班   狮吼_x ，_w 在_p 没有_v 锁定_v 提前_h 出线_v 名额_n 的_u 情况_n 下_f";
		String test2 = "    _w http://y0.ifengimg.com/a/2015_15/348005a875a7c4c.jpg   _w ";
        Matcher matcher = pattern.matcher(test);
        Set<String> filterLable = new HashSet<String>();
        filterLable.add("n");
        filterLable.add("x");
        
        while (matcher.find())
        {
           String param = matcher.group();
           IWord temp = WordFactory.create(param);
           if (null != temp) {
        	   System.out.println(temp.toString());
           }
        }
        
//		String json = KnowledgeBaseBuild.getEntityList("詹姆斯");
//		System.out.println(json);
		List<EntityInfo> result = EntityLibQuery.getEntityList("工体");
		for (EntityInfo et : result) {
			System.out.println(et.getCategory());
		}
		
	}
}
