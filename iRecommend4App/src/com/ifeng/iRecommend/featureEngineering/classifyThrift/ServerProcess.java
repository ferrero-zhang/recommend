package com.ifeng.iRecommend.featureEngineering.classifyThrift;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import com.ifeng.iRecommend.featureEngineering.CMPPDataOtherField;
import com.ifeng.iRecommend.featureEngineering.CMPPDataOtherParser;
import com.ifeng.iRecommend.featureEngineering.KeywordValueJudge;
import com.ifeng.iRecommend.featureEngineering.RuleModify;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.KeywordExtract;
import com.ifeng.myClassifier.GetSimcFeature;

public class ServerProcess implements ClassifyService.Iface {
	static Logger LOG = Logger.getLogger(ServerProcess.class);
	// 获取关键词
	private static KeywordValueJudge kvJudge = KeywordValueJudge.getKVJudgeInstance();
	private static RuleModify ruleModify = RuleModify.getInstance();

	// 实现这个方法完成具体的逻辑。
	public List<String> doAction(Request request) throws RequestException, TException {
		String strForLog = "#id_" + request.getId() + "#title_" + request.getS_title()+"#requestMan_"+request.getName();
		LOG.info(" Begin process " + strForLog);
		long allTime = System.currentTimeMillis();
		/******************* 提取关键词模块 ***********************/
		KeywordExtract ke = kvJudge.getKeywordExtract(request.getS_title(), request.getS_content(), null);
		ArrayList<String> keywordList = new ArrayList<String>();
		keywordList.addAll(kvJudge.getPreKeyword(ke));
		LOG.info("[INFO] " + strForLog + " Get keyword list is " + keywordList);
		// 获取tags对应的c，sc等信息
		/******************* other字段解析模块 ***********************/
		CMPPDataOtherField f = new CMPPDataOtherField(request.getOther());
		ArrayList<String> tagList = CMPPDataOtherParser.OtherParser(f, request.getSource(), request.getDoctype());
		if (tagList != null)
			keywordList.addAll(tagList);
		/******************* feature修正模块 ***********************/
		// 抄袭检测没有结果则把simc结果放入feature
		LOG.info("[INFO]Begin to cal simc result.");
		ArrayList<String> cList = new ArrayList<String>();
		cList = GetSimcFeature.ClassifyByWord(strForLog, request.getS_title(), request.getSource(), keywordList);
		if (cList == null)
			cList = new ArrayList<String>();
		keywordList.addAll(cList);
		LOG.info("[INFO] " + strForLog + " simc result is " + cList);
		// 规则引擎修正feature
		keywordList = ruleModify.modifyResult(request.getS_title(), keywordList, request.getDoctype(), request.getOther().contains("canbeSlide=true"));
		LOG.info("[INFO] " + strForLog + "Feature after rulemodify is " + keywordList);
		List<String> result = new ArrayList<String>();
		if(keywordList != null)
		{
			for(int i = 0; i < keywordList.size() - 2; i+=3)
			{
				if(keywordList.get(i+1).equals("c")||keywordList.get(i+1).equals("sc")||keywordList.get(i+1).equals("cn"))
				{
					result.add(keywordList.get(i));
					result.add(keywordList.get(i+1));
					result.add(keywordList.get(i+2));
				}
			}
		}
		LOG.info(strForLog+"[INFO] Cal this article used time "+ (System.currentTimeMillis() - allTime));
		LOG.info(strForLog+"[INFO] Return result is "+ result);
		return result;
	}
}
