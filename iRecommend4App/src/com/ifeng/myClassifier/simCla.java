package com.ifeng.myClassifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.gson.reflect.TypeToken;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.featureEngineering.LayerGraph.Graph;
import com.ifeng.iRecommend.featureEngineering.LayerGraph.GraphNode;


/**
 * <PRE>
 * 作用 : 
 *   读入可信强术语表，并通过传入的item features列表和内容标题信息进行投票；
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
 *          1.0          2015年8月10日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class simCla {

	class candidate_clas {
		String cla_name;
		String cla_type;
		int total_vote_num;
		float total_vote_score;
		int title_vote_num;// title对此候选的投票数目
		float title_vote_score;// title对此后续的投票概率
		boolean isClaNameInTitle;// title中是否有此后续cla_name;
		int first_vote_num;// 各个词第一倾向的投票候选，数目
		float first_vote_score;// 各个词第一倾向的投票候选，得分

		public String toString() {
			return new StringBuffer().append(cla_name).append("\t")
					.append(cla_type).append("\t").append(total_vote_num)
					.append("\t").append(total_vote_score).append("\t")
					.append(title_vote_num).append("\t")
					.append(title_vote_score).append("\t")
					.append(isClaNameInTitle).append("\t")
					.append(first_vote_num).append("\t")
					.append(first_vote_score).toString();
		}

	}

	static Logger LOG = Logger.getLogger(simCla.class);
	// 术语等置信表
	
	private static String wordsDis = LoadConfig
			.lookUpValueByKey("wordsDis"); //simcla 词分布表
	private static String entities = LoadConfig
			.lookUpValueByKey("entities");  //simcla 术语库
		
	private HashMap<String, ArrayList<wordDis>> hm_wordCanCla;
	// 稿源置信表
	private HashMap<String, ArrayList<wordDis>> hm_sourceCanCla;
	// kb\kq等置信表
	private HashMap<String, ArrayList<wordDis>> hm_specialCanCla;
	
	//术语表
	private HashMap<String, String> hm_et2domain;
	
	
	public static simCla instance = new simCla();

	public static simCla getInstance() {
		return instance;
	}

	private simCla() {

		hm_sourceCanCla = new HashMap<String, ArrayList<wordDis>>();
		hm_wordCanCla = new HashMap<String, ArrayList<wordDis>>();
		hm_specialCanCla = new HashMap<String, ArrayList<wordDis>>();
				
		FileUtil fu = new FileUtil();
		hm_et2domain = new HashMap<String, String>();
		
		// 载入术语表
		fu.Initialize(entities, "utf-8");
		String line = null;
		while ((line = fu.ReadLine()) != null) {
			String[] secs = line.split("\\|\\|");
			String word = secs[0].trim();
			String domain = secs[1].trim();

			//处理财经
			if(domain.equals("财经"))
				domain = "财经1";
			
			String domain_all = hm_et2domain.get(word);
			if (domain_all == null)
				hm_et2domain.put(word, domain);
			else if (domain_all.indexOf(domain) >= 0) {

			} else {
				hm_et2domain.put(word, domain + "," + domain_all);
			}

			if (secs.length >= 3) {
				String[] aliasNames = secs[2].split("\t");
				for (String aliasName : aliasNames) {
					aliasName = aliasName.trim();
					domain_all = hm_et2domain.get(aliasName);
					if (domain_all == null)
						hm_et2domain.put(aliasName, domain);
					else if (domain_all.indexOf(domain) >= 0) {

					} else {
						hm_et2domain.put(aliasName, domain + "," + domain_all);
					}
				}
			}
		}

		// 读入source distribution
		fu = new FileUtil();
		// 读入原始分类关联表
		fu.Initialize(wordsDis,
				"utf-8");
		line = null;
		String word = null, word_prob = null, domain = null;
		int df = -1, gd = -1;
		ArrayList<wordDis> al_wds = new ArrayList<wordDis>();
		while ((line = fu.ReadLine()) != null) {
			if (line.startsWith("-------")) {

				if (word_prob.startsWith("s")) {
					if(df >= 8)
						hm_sourceCanCla.put(word, al_wds);
				} else if (word_prob.equals("kb") || word_prob.equals("kq")) {
					hm_specialCanCla.put(word, al_wds);
				} else {
					// 过滤掉特别短的x k n等词:年轻人 其他人 一个人 生命通道
					// 术语类：犯罪 如意 娱乐类术语和最大分布不一致时候不加入；
					boolean ignore = false;
					
					//过滤df太小的词
					if(df < 5)
						ignore = true;
					
					// if(word.length() <= 2
					// && !word_prob.startsWith("et")){
					// ignore = true;
					// }

					if (ignore == false
							&& (!hm_wordCanCla.containsKey(word) || word_prob
									.indexOf("et_et_") >= 0)) {
						hm_wordCanCla.put(word, al_wds);
					}

				}

				word = null;
				word_prob = null;
				domain = null;
				
				df = -1;
				gd = -1;
				al_wds = new ArrayList<wordDis>();
				continue;
			}

			if (line.isEmpty())
				continue;

			// 修正word_prob并加入domain
			if (word == null) {
				String[] secs = line.split("\t");
				word = secs[0].trim();
				word_prob = secs[1].trim();
				gd = Integer.valueOf(secs[2]);
				df = Integer.valueOf(secs[3]);
				domain = hm_et2domain.get(word);
				// et默认加入初始domain
				if (word_prob.indexOf("_et_") > 0 && domain != null && domain.indexOf("全部") < 0 && domain.indexOf("其他") < 0) {

					String[] domain_secs = domain.split(",");
					for (String sec1 : domain_secs) {
						wordDis wd = new wordDis();
						wd.pa_claName = "root";
						wd.classname = sec1;
						wd.cla_type = "c0";
						wd.df = 100;
						wd.p = 0.5f;
						wd.pa_p = 1.0f;
						al_wds.add(wd);
					}
				}

			} else {// 解析c0 c1 sc各级别分布
				String cla_type = "";
				if (line.indexOf(",c0,") > 0) {
					cla_type = "c0";
				}
				if (line.indexOf(",c1,") > 0) {
					cla_type = "c1";
				}
				if (line.indexOf(",sc,") > 0) {
					cla_type = "sc";
				}
				String[] secs = line.split(",");
				for (int i = 0; i < secs.length; i += 5) {
					if (word_prob.indexOf("_et_") > 0 && domain != null
							&& domain.indexOf(secs[i].trim()) >= 0)
						continue;
					
				
					
					wordDis wd = new wordDis();
					int index_flag =  secs[i].trim().indexOf("|");
					wd.pa_claName =  secs[i].trim().substring(index_flag+1);
					wd.classname = secs[i].trim().substring(0,index_flag);
					wd.cla_type = cla_type;
					wd.df = Integer.valueOf(secs[i + 4]);
					wd.p = Float.valueOf(secs[i + 2]);
					wd.pa_p = Float.valueOf(secs[i + 3]);
					
					//股市特殊处理
					if(wd.classname.equals("股市"))
						wd.p = wd.p / 2;
					
					al_wds.add(wd);

				}
			}

		}

		// 最后遍历术语库，将没有出现的术语加入，除了娱乐的3个字以下
		Iterator<Entry<String, String>> it = hm_et2domain.entrySet().iterator();
		while (it.hasNext()) {
			al_wds = new ArrayList<wordDis>();
			Entry<String, String> et = it.next();
			word = et.getKey();
			domain = et.getValue();
			if (hm_wordCanCla.containsKey(word))
				continue;
			if (domain.indexOf("全部") < 0 && domain.indexOf("其他") < 0) {
				if (domain.indexOf("娱乐") >= 0 && word.length() <= 2)
					continue;
				String[] domain_secs = domain.split(",");
				for (String sec1 : domain_secs) {
					wordDis wd = new wordDis();
					wd.pa_claName = "root";
					wd.classname = sec1;
					wd.cla_type = "c0";
					wd.df = 100;
					wd.p = 0.5f;
					wd.pa_p = 1.0f;
					al_wds.add(wd);
				}
				hm_wordCanCla.put(word, al_wds);

			}

		}
	}

	
	/**
	 * 输入文章的所有et等强术语信息，输入文章的已有c0 c1 sc cn等信息，决策出合适的c0 c1 sc等；
	 * 决策将依赖多算法boost投票；目前主要是simcla、bayes算法；以及svm支撑向量
	 * 
	 * 注意：   
	 * 	    1) 如果有sc，那么不做分类算法直接返回；理论上有sc就有c1 c0；不太需要多做了；
	 * 		2) 如果只有c0、c1，那么提取出最大的c1并决策sc；
	 * 		3） 如果只有c0，那么提取出最大的c0并决策出c1 sc；
	 * 
	 * 
	 * @param al_words
	 *            格式是string的三元组：et type weight；比如：台湾时报 s 1.0 马英九 et 1.0 台湾 0.5 习近平 x
	 *            0.5 。。。
	 * @param title
	 *            分词后的title信息
	 *            
	 * @return 分类结果
	 */
	public ArrayList<String> classify2(ArrayList<String> al_words, String title) {
		if(al_words == null || al_words.isEmpty() || al_words.size() % 3 != 0)
			return null;
		
		if(title == null)
			title = "";
		
		//判断有没有sc，并提取出最大的c0 c1；（如果可能有的话）
		//1) 如果有sc，那么不做分类算法直接返回；理论上有sc就有c1 c0；不太需要多做了；
		//2) 如果只有c0、c1，那么提取出最大的c1并决策sc；
		//3） 如果只有c0，那么提取出最大的c0并决策出c1 sc；
		
		String bestC0 = "",bestC1 = "";
		float bestC0_w = 0f;float bestC1_w = 0f;
		
		boolean isSCexist = false;//是否有sc

		int i = 0;		
		for (; i < al_words.size(); i += 3) {
			String word = al_words.get(i);
			String type = al_words.get(i + 1);
			float p = Float.valueOf(al_words.get(i + 2));

			//c0
			if(type.equals("c") && p <= -0.5)
			{
				if(p <= bestC0_w){
					bestC0_w = p;
					bestC0 = word;
				}
			}
			
			
			//c1
			if(type.equals("c") && p >= 0.5)
			{
				if(p >= bestC1_w){
					bestC1_w = p;
					bestC1 = word;
				}
			}
			//sc则直接返回
			if(type.equals("sc") && p >= 0.5)
			{
				isSCexist = true;
				return null;
			}

		}
		
		//如果有c1，则决策sc即可
		if(!bestC1.isEmpty()){
			return classifyC1orSC(bestC1,"c1",bestC1_w,al_words,title);
		}
		
		//如果有c0，则决策c1,sc即可
		if(!bestC0.isEmpty()){
			return classifyC1orSC(bestC0,"c0",bestC0_w,al_words,title);
		}		
		
		//否则，就进行完整的决策		
		HashMap<String,Float> hm_c0_candidate = new HashMap<String,Float>();
		HashMap<String,Integer> hm_c0_candidate_voteNum = new HashMap<String,Integer>();
		
		cmpC0Dis(al_words,title,hm_c0_candidate,hm_c0_candidate_voteNum);
		
		if (hm_c0_candidate.size() <= 0)
			return null;
		
		HashMap<String, candidate_clas> hm_candidates = new HashMap<String, candidate_clas>();
		// 挑选候选集
		/*
		 * 1）提取出候选集：总投票的top 2 + 总得分的 top 2； 
		 * 2）title对候选的投票概率; 
		 * 3) title是否有候选一样的分类名字； (暂时ignore)
		 */
		genC0Candidates(hm_candidates,hm_c0_candidate,hm_c0_candidate_voteNum);
		// 统计title的投票倾向
		voteFromTitle(hm_candidates, title);
		
		//得到所有的候选集后，输出
		String s_candd = hm_candidates.toString();
		// test
//		commenFuncs.writeResult(
//				"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//				"testRes", s_candd + "\n", "utf-8", true,
//				null);
		LOG.info(s_candd + "\n");
		
		//决策阶段，给出c0结果
		ArrayList<candidate_clas> c0_res = chooseSimClaBestC0(hm_candidates);

		//bayes 
		NaiveBayes nb = NaiveBayes.getInstance();
		String[] NBRes = nb.classify(al_words);
		
		//@test
//		StringBuffer test_sb = new StringBuffer();
//		if(NBRes == null){
//			test_sb.append("");
//		}else{
//			for(String sec:NBRes){
//				test_sb.append(sec).append(",");
//			}
//		}		
//		if(c0_res == null)
//			c0_res = new ArrayList<candidate_clas>();
		
//		//@test
//		commenFuncs.writeResult(
//				"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//				"testRes", c0_res.toString() + "||"+test_sb.toString()+"\n", "utf-8", true, null);
		
		c0_res = chooseBestC0(c0_res, NBRes);
		
		
		
		
		if(c0_res == null || c0_res.isEmpty())
			return null;
		
		LOG.info(c0_res.toString());
		
		
		
		
		//----------将最终结果转化成3元组-------
		ArrayList<String> c = new ArrayList<String>(c0_res.size());
		c.add(c0_res.get(0).cla_name);
		c.add("c");
		c.add("-0.7");
		for (i = 1; i < c0_res.size(); i++){
			c.add(c0_res.get(i).cla_name);
			c.add("c");
			c.add("-0.5");
		}
		
		
		//统计c0到c1条件下的各个c1的dis分布,并决策最优c1
		for(candidate_clas c0_cla:c0_res){
	
			ArrayList<String> c_res = classifyC1orSC(c0_cla.cla_name,"c0",0.5f,al_words,title);
			if(c_res == null || c_res.isEmpty())
				continue;
			c.addAll(c_res);
			LOG.info(c_res.toString());

		}
		
		return c;
	}
	
	
	/**
	 * 输入文章的所有et等强术语信息，通过投票计算出可能的c
	 * 
	 * 注意：   
	 * 	    1) 一个sc可能有两个c1，但是这种情况都是一个样本同时含有这两个c1；所以以词为单位投票时候，不影响统计
	 * 		2)目前投票时候，只取一个词的top 2 c0做投票；并且top1和2的差距很小;
	 * 
	 * 
	 * @param al_words
	 *            格式是string的三元组：et type weight；比如：台湾时报 s 1.0 马英九 et 1.0 台湾 0.5 习近平 x
	 *            0.5 。。。
	 * @param title
	 *            分词后的title信息
	 *            
	 * @return 分类结果
	 */
	public ArrayList<String> classify(ArrayList<String> al_words, String title) {
		if(al_words == null || al_words.isEmpty() || al_words.size() % 3 != 0)
			return null;
		
		if(title == null)
			title = "";
		
		HashMap<String,Float> hm_c0_candidate = new HashMap<String,Float>();
		HashMap<String,Integer> hm_c0_candidate_voteNum = new HashMap<String,Integer>();
		
		cmpC0Dis(al_words,title,hm_c0_candidate,hm_c0_candidate_voteNum);
		
		if (hm_c0_candidate.size() <= 0)
			return null;
		
		HashMap<String, candidate_clas> hm_candidates = new HashMap<String, candidate_clas>();
		// 挑选候选集
		/*
		 * 1）提取出候选集：总投票的top 2 + 总得分的 top 2； 
		 * 2）title对候选的投票概率; 
		 * 3) title是否有候选一样的分类名字； (暂时ignore)
		 */
		genC0Candidates(hm_candidates,hm_c0_candidate,hm_c0_candidate_voteNum);
		// 统计title的投票倾向
		voteFromTitle(hm_candidates, title);
		
		//得到所有的候选集后，输出
		String s_candd = hm_candidates.toString();
		// test
//		commenFuncs.writeResult(
//				"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//				"testRes", s_candd + "\n", "utf-8", true,
//				null);
		LOG.info(s_candd + "\n");
		
		//决策阶段，给出c0结果
		ArrayList<candidate_clas> c0_res = chooseSimClaBestC0(hm_candidates);

		//bayes 
		NaiveBayes nb = NaiveBayes.getInstance();
		String[] NBRes = nb.classify(al_words);
		
		//@test
//		StringBuffer test_sb = new StringBuffer();
//		if(NBRes == null){
//			test_sb.append("");
//		}else{
//			for(String sec:NBRes){
//				test_sb.append(sec).append(",");
//			}
//		}		
//		if(c0_res == null)
//			c0_res = new ArrayList<candidate_clas>();
		
//		//@test
//		commenFuncs.writeResult(
//				"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//				"testRes", c0_res.toString() + "||"+test_sb.toString()+"\n", "utf-8", true, null);
		
		c0_res = chooseBestC0(c0_res, NBRes);
		
		
		
		
		if(c0_res == null || c0_res.isEmpty())
			return null;
		
		LOG.info(c0_res.toString());
		
		//----------将最终结果转化成3元组-------
		ArrayList<String> c = new ArrayList<String>(c0_res.size());
		c.add(c0_res.get(0).cla_name);
		c.add("c");
		c.add("-0.7");
		for (int i = 1; i < c0_res.size(); i++){
			c.add(c0_res.get(i).cla_name);
			c.add("c");
			c.add("-0.5");
		}
		
		
		//统计c0到c1条件下的各个c1的dis分布,并决策最优c1
		for(candidate_clas c0_cla:c0_res){
	
			//取各个c0下最高score的c1
			ArrayList<candidate_clas> c1_res = chooseBestChildClass("c1",c0_cla.cla_name,al_words,title,0.2f);
			
			if(c1_res == null || c1_res.isEmpty()) {
				continue;
			}
			
			c.add(c1_res.get(0).cla_name);
			c.add("c");
			c.add("0.7");
			for (int i = 1; i < c1_res.size(); i++){
				c.add(c1_res.get(i).cla_name);
				c.add("c");
				c.add("0.5");
			}	
			
			LOG.info(c1_res.toString());
			
			// 统计c1到sc条件下的各个sc的dis分布,并决策最优sc
			for (candidate_clas c1_cla:c1_res){
				ArrayList<candidate_clas> sc_res = chooseBestChildClass("sc",
						c1_cla.cla_name, al_words, title, 0.2f);

				if (sc_res == null || sc_res.isEmpty()) {
					continue;
				}

				c.add(sc_res.get(0).cla_name);
				c.add("sc");
				c.add("0.7");
				for (int i = 1; i < sc_res.size(); i++) {
					c.add(sc_res.get(i).cla_name);
					c.add("sc");
					c.add("0.5");
				}

				LOG.info(sc_res.toString());
			}
			

		}
		
		
	
		

		
		// test
//		commenFuncs.writeResult(
//				"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//				"testRes", hm_c0_candidate.toString() + "\n", "utf-8", true,
//				null);

//		commenFuncs.writeResult(
//				"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//				"testRes", hm_c0_candidate_voteNum.toString() + "\n", "utf-8",
//				true, null);
		
//		commenFuncs.writeResult(
//				"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//				"testRes", hm_c1_candidate.toString() + "\n", "utf-8", true,
//				null);

//		commenFuncs.writeResult(
//				"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//				"testRes", hm_c1_candidate_voteNum.toString() + "\n", "utf-8",
//				true, null);

	
		return c;
	}
	
	
	
	/**
	 * choose Best C1 or SC;
	 * 如果已经知道c0或者c1，那么输入文章的所有et等强术语信息，通过投票计算出可能的child class;
	 * 
	 * 注意：  
	 * 	  返回的只是计算出来的分类，不会包含已知的分类；比如不会包含c0到return中；
	 * 
	 * @param pa_name
	 * 			已经知道的pa分类name，比如“财经1”
	 * @param pa_type
	 * 	      	 已经知道的pa分类type，比如“c0”、“c1”
	 * @param pa_p
	 * 	      	 已经知道的pa分类权重（由此开始的子节点不能权重比c0高）
	 * @param al_words
	 *            格式是string的三元组：et type weight；比如：台湾时报 s 1.0 马英九 et 1.0 台湾 0.5 习近平 x
	 *            0.5 。。。
	 * @param title
	 *            分词后的title信息
	 *            
	 * @return 分类结果
	 */
	public ArrayList<String> classifyC1orSC(String pa_name,String pa_type,float pa_p,ArrayList<String> al_words, String title) {
		if(pa_name == null || pa_type == null || al_words == null || al_words.isEmpty() || al_words.size() % 3 != 0)
			return null;
		
		if(pa_name.isEmpty() || pa_type.isEmpty())
			return null;
		
		if(pa_p < 0.5f && pa_p > -0.5f)
			return null;
		
		
		if(title == null)
			title = "";
		
		String cla_type = "";
		if(pa_type.equals("c0"))
		{
			cla_type = "c1";
		}

		if (pa_type.equals("c1")) {
			cla_type = "sc";
		}
		
		// 取各个c0下最高score的c1
		ArrayList<candidate_clas> c_res = chooseBestChildClass(cla_type,
				pa_name, al_words, title, 0.2f);

		if (c_res == null || c_res.isEmpty()) {
			return null;
		}
		
		LOG.info(c_res.toString());

		// ----------将最终结果转化成3元组-------
		ArrayList<String> c = new ArrayList<String>(3);
		
		
		//如果是sc，那么决策已经到顶点，可以直接返回
		if(cla_type.equals("sc")){
			c.add(c_res.get(0).cla_name);
			c.add("sc");
			c.add(String.valueOf(Math.abs(pa_p)));
			for (int i = 1; i < c_res.size(); i++) {
				c.add(c_res.get(i).cla_name);
				c.add("sc");
				c.add("0.5");
			}
			return c;
		}
		
		if(cla_type.equals("c1")){
			c.add(c_res.get(0).cla_name);
			c.add("c");
			c.add(String.valueOf(Math.abs(pa_p)));
			for (int i = 1; i < c_res.size(); i++) {
				c.add(c_res.get(i).cla_name);
				c.add("c");
				c.add("0.5");
			}
		}

		// 统计c1到sc条件下的各个sc的dis分布,并决策最优sc
		for (candidate_clas c1_cla : c_res) {
			ArrayList<candidate_clas> sc_res = chooseBestChildClass("sc",
					c1_cla.cla_name, al_words, title, 0.2f);

			if (sc_res == null || sc_res.isEmpty()) {
				continue;
			}

			c.add(sc_res.get(0).cla_name);
			c.add("sc");
			c.add(String.valueOf(Math.abs(pa_p)));
			for (int i = 1; i < sc_res.size(); i++) {
				c.add(sc_res.get(i).cla_name);
				c.add("sc");
				c.add("0.5");
			}

			LOG.info(sc_res.toString());
		}
	
		return c;
	}
	

	/**
	 * 根据simCla 和 bayes的各自结果给出最终结果
	 * @param c0_Res
	 * @param nBRes
	 * @return
	 */
	private ArrayList<candidate_clas> chooseBestC0(
			ArrayList<candidate_clas> c0_Res, String[] nBRes) {
		LOG.info("simCla_C0 ===" + c0_Res);
	
		
		if(nBRes == null || nBRes.length <= 0) //bayes 没有结果
			return c0_Res; //返回simCla
		
		if(c0_Res == null || c0_Res.isEmpty()){ //simCla没有结果 返回bayes
			c0_Res = new ArrayList<candidate_clas>(nBRes.length); //根据bayes给出的c0名称虚构candidate_clas对象
			candidate_clas temp = new candidate_clas();
			temp.cla_name = nBRes[0];
			temp.cla_type = "c0";
			c0_Res.add(temp);
			
			return c0_Res;
		}
		
		ArrayList<candidate_clas> best_c0_Res = new ArrayList<candidate_clas>(c0_Res.size());
		String simCla1st = c0_Res.get(0).cla_name; //simCla第一位
		String NB1st = nBRes[0];//bayes第一位
		
		if(simCla1st.equals(NB1st)) { //simCla第一位和bayes第一位一样
			best_c0_Res.add(c0_Res.get(0)); //将第一位分类放入结果里
		}
		if(c0_Res.size() > 1){ //simCla has 2nd result
			for (int i = 1; i < c0_Res.size(); i++) {

				String simClaXth = c0_Res.get(i).cla_name;
				if (simClaXth.equals(NB1st)) { // simCla第二位和bayes第一位一样，取bayes第一位
					best_c0_Res.add(c0_Res.get(i));
					break;
					// best_c0_Res.add(c0_Res.get(1));
				} else if (nBRes.length > 1 && simClaXth.equals(nBRes[1])) { // bayes有第二位结果并且和simcla第二位一样，放入结果
//					candidate_clas temp = new candidate_clas();
//					temp.cla_name = nBRes[0];
//					temp.cla_type = "c0";
//					best_c0_Res.add(temp);
					best_c0_Res.add(c0_Res.get(i));
					break;
				}
			}
		}
		if(nBRes.length > 1 && simCla1st.equals(nBRes[1]) && best_c0_Res.indexOf(c0_Res.get(0)) < 0){ //bayes有第二位结果并且和simcla第一位一样，将simCla第一位放入结果
//			candidate_clas temp = new candidate_clas();
//			temp.cla_name = nBRes[0];
//			temp.cla_type = "c0";
//			best_c0_Res.add(temp);
			best_c0_Res.add(c0_Res.get(0));
		}
		return best_c0_Res;
	}

	// 挑选c0的候选集
	private void genC0Candidates(HashMap<String, candidate_clas> hm_candidates,
			HashMap<String, Float> hm_c0_candidate,
			HashMap<String, Integer> hm_c0_candidate_voteNum) {
		// TODO Auto-generated method stub

		// sort
		// sort hm_c0_candidate
		ArrayList<Entry<String, Float>> maplist_vote_score = new ArrayList(
				hm_c0_candidate.entrySet());
		if (maplist_vote_score.size() > 1)
			Collections.sort(maplist_vote_score,
					new Comparator<Entry<String, Float>>() {
						@Override
						public int compare(Entry<String, Float> arg0,
								Entry<String, Float> arg1) {
							// TODO Auto-generated method stub
							Float result = arg0.getValue() - arg1.getValue();
							if (result > 0)
								return -1;
							else if (result == 0)
								return 0;
							else
								return 1;
						}
					});
		// sort hm_c0_candidate
		ArrayList<Entry<String, Integer>> maplist_vote_num = new ArrayList(
				hm_c0_candidate_voteNum.entrySet());
		if (hm_c0_candidate_voteNum.size() > 1)
			Collections.sort(maplist_vote_num,
					new Comparator<Entry<String, Integer>>() {
						@Override
						public int compare(Entry<String, Integer> arg0,
								Entry<String, Integer> arg1) {
							// TODO Auto-generated method stub
							Integer result = arg0.getValue() - arg1.getValue();
							if (result > 0)
								return -1;
							else if (result == 0)
								return 0;
							else
								return 1;
						}
					});

		// 取score最好的top 2
		if (maplist_vote_score != null && maplist_vote_score.size() >= 1) {
			// 加入候选集
			float top1_score = -1.0f, top2_score = -1.0f;
			for (int i1 = 0; i1 < maplist_vote_score.size(); i1++) {
				Entry<String, Float> top1Entry = maplist_vote_score.get(i1);
				if (top1Entry == null || top1Entry.getValue() < 0.1f) {
					break;
				}

				if (top1_score <= 0) {
					candidate_clas cc = new candidate_clas();
					cc.cla_name = top1Entry.getKey();
					cc.cla_type = "c0";
					cc.total_vote_score = top1Entry.getValue();
					cc.total_vote_num = hm_c0_candidate_voteNum.get(top1Entry
							.getKey()) == null ? 0 : hm_c0_candidate_voteNum
							.get(top1Entry.getKey());
					hm_candidates.put(top1Entry.getKey(), cc);

					top1_score = top1Entry.getValue();
				} else {
					float score = top1Entry.getValue();
					if (score < (top1_score * 0.5f))
						break;

					// 否则只取top 2
					if (top2_score <= 0) {
						candidate_clas cc = new candidate_clas();
						cc.cla_name = top1Entry.getKey();
						cc.cla_type = "c0";
						cc.total_vote_score = top1Entry.getValue();
						cc.total_vote_num = hm_c0_candidate_voteNum
								.get(top1Entry.getKey()) == null ? 0
								: hm_c0_candidate_voteNum.get(top1Entry
										.getKey());
						hm_candidates.put(top1Entry.getKey(), cc);

						top2_score = top1Entry.getValue();
					} else {
						if (score < top2_score)
							break;

						candidate_clas cc = new candidate_clas();
						cc.cla_name = top1Entry.getKey();
						cc.cla_type = "c0";
						cc.total_vote_score = top1Entry.getValue();
						cc.total_vote_num = hm_c0_candidate_voteNum
								.get(top1Entry.getKey()) == null ? 0
								: hm_c0_candidate_voteNum.get(top1Entry
										.getKey());
						hm_candidates.put(top1Entry.getKey(), cc);

					}

				}
			}
		}

		// 取vote num最好的top 2
		if (maplist_vote_num != null && maplist_vote_num.size() >= 1) {
			// 加入候选集
			int top1_num = -1, top2_num = -1;
			for (int i1 = 0; i1 < maplist_vote_num.size(); i1++) {
				Entry<String, Integer> top1Entry = maplist_vote_num.get(i1);
				if (top1Entry == null || top1Entry.getValue() < 1) {
					break;
				}

				if (top1_num <= 0) {
					if (!hm_candidates.containsKey(top1Entry.getKey())) {
						candidate_clas cc = new candidate_clas();
						cc.cla_name = top1Entry.getKey();
						cc.cla_type = "c0";
						cc.total_vote_num = top1Entry.getValue();
						cc.total_vote_score = hm_c0_candidate.get(top1Entry
								.getKey()) == null ? 0 : hm_c0_candidate
								.get(top1Entry.getKey());
						hm_candidates.put(top1Entry.getKey(), cc);
					}

					top1_num = top1Entry.getValue();
				} else {
					if (top1Entry.getValue() < (top1_num * 0.5f))
						break;

					// 否则只取top 2
					if (top2_num <= 0) {
						if (!hm_candidates.containsKey(top1Entry.getKey())) {
							candidate_clas cc = new candidate_clas();
							cc.cla_name = top1Entry.getKey();
							cc.cla_type = "c0";
							cc.total_vote_num = top1Entry.getValue();
							cc.total_vote_score = hm_c0_candidate.get(top1Entry
									.getKey()) == null ? 0 : hm_c0_candidate
									.get(top1Entry.getKey());
							hm_candidates.put(top1Entry.getKey(), cc);
						}

						top2_num = top1Entry.getValue();
					} else {
						if (top1Entry.getValue() < top2_num)
							break;

						if (!hm_candidates.containsKey(top1Entry.getKey())) {
							candidate_clas cc = new candidate_clas();
							cc.cla_name = top1Entry.getKey();
							cc.cla_type = "c0";
							cc.total_vote_num = top1Entry.getValue();
							cc.total_vote_score = hm_c0_candidate.get(top1Entry
									.getKey()) == null ? 0 : hm_c0_candidate
									.get(top1Entry.getKey());
							hm_candidates.put(top1Entry.getKey(), cc);
						}

					}

				}
			}

		}
	}

	/**
	 * 根据输入的c0或者c1，计算最好的child分类
	 * 
	 * 注意： 1)要求c1必须得分大于1.0;
	 *      2)要求c1投票必须大于1；
	 * 
	 * 
	 * @param al_ets
	 *            格式是string的三元组：et weight；比如：台湾时报 s 1.0 马英九 et 1.0 台湾 0.5 习近平 x
	 *            0.5 。。。
	 * 
	 * @return 分类名称
	 */
	private ArrayList<candidate_clas> chooseBestChildClass(
			String cla_type,
			String pa_cla_name,
			ArrayList<String> al_words,
			String title,
			float score_threshold) {
		// TODO Auto-generated method stub
		if(pa_cla_name == null || pa_cla_name.isEmpty())
			return null;
		
		if(cla_type == null || cla_type.isEmpty())
			return null;
		
		if(title == null || title.isEmpty())
			title = "";
		

		HashMap<String,Float> hm_cla_candidate = new HashMap<String,Float>();
		HashMap<String,Integer> hm_cla_candidate_voteNum = new HashMap<String,Integer>();
		
		//统计各个pa_cla的child分布，取最大的child做置信,加入返回结果
		cmpClaDis(pa_cla_name,al_words,title,cla_type,hm_cla_candidate,hm_cla_candidate_voteNum);
		
		if(hm_cla_candidate == null || hm_cla_candidate.isEmpty())
			return null;
		if(hm_cla_candidate_voteNum == null || hm_cla_candidate_voteNum.isEmpty())
			return null;
		
		//取hm_cla_candidate最大的那个,如果置信，返回
		candidate_clas top_cddc_c1 = new candidate_clas();
		float top2_score = 0f;
		Iterator<Entry<String, Float>> it = hm_cla_candidate.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, Float> et = it.next();
			String child_cla_name = et.getKey();
			Float score = et.getValue();
			if(child_cla_name == null || score == null)
				continue;
			
			if(top_cddc_c1.total_vote_score <= 0)
			{
				top_cddc_c1.total_vote_score = score;
				top_cddc_c1.cla_name = child_cla_name;
				top_cddc_c1.cla_type = cla_type;
				top_cddc_c1.total_vote_num = hm_cla_candidate_voteNum.get(child_cla_name);
			}else if(score > top_cddc_c1.total_vote_score){
				//top2
				top2_score = top_cddc_c1.total_vote_score;
				
				top_cddc_c1.total_vote_score = score;
				top_cddc_c1.cla_name = child_cla_name;
				top_cddc_c1.cla_type = cla_type;
				top_cddc_c1.total_vote_num = hm_cla_candidate_voteNum.get(child_cla_name);
			}
			
			
		}
		
		
		ArrayList<candidate_clas> al_res = new ArrayList<candidate_clas>();
		if(top_cddc_c1.cla_type.equals("c1") && top_cddc_c1.cla_name.equals(pa_cla_name))
			al_res.add(top_cddc_c1);
		//如果置信
		else if(top_cddc_c1.total_vote_score >= score_threshold && top_cddc_c1.total_vote_num >= 2){
			//临时设置，要求top1比top2有足够的优势，降低召回提高准确，避免模糊歧义
			if((top2_score*1.5) <= top_cddc_c1.total_vote_score)
				al_res.add(top_cddc_c1);
		}
		
		return al_res;
	}

//	
//	/**
//	 * 根据输入的c0，以及c2c树关系，选取各个c0下的最高得分c1
//	 * 
//	 * 注意： 1)要求c1必须得分大于1.0;
//	 *      2)要求c1投票必须大于1；
//	 * 
//	 * 
//	 * @param al_ets
//	 *            格式是string的三元组：et weight；比如：台湾时报 s 1.0 马英九 et 1.0 台湾 0.5 习近平 x
//	 *            0.5 。。。
//	 * 
//	 * @return 分类名称
//	 */
//	private ArrayList<candidate_clas> chooseBestSC(
//			ArrayList<candidate_clas> c1_res,
//			HashMap<String, Float> hm_sc_candidate,
//			HashMap<String, Integer> hm_sc_candidate_voteNum) {
//		// TODO Auto-generated method stub
//		if(c1_res == null || c1_res.isEmpty())
//			return null;
//		if(hm_sc_candidate == null || hm_sc_candidate.isEmpty())
//			return null;
//		if(hm_sc_candidate_voteNum == null || hm_sc_candidate_voteNum.isEmpty())
//			return null;
//		
//		ArrayList<candidate_clas> al_res = new ArrayList<candidate_clas>();
//		
//		for(candidate_clas cddc:c1_res){
//			String c1_cla_name = cddc.cla_name;
//			//通过graphNode找到csc子列表,逐个sc遍历并留下score最高且合乎阈值的那个sc
//			Graph graph = Graph.getInstance();
//			ArrayList<GraphNode> graphNodeList=Graph.getInstance().queryWord(c1_cla_name);
//			if(graphNodeList == null || graphNodeList.isEmpty())
//			{
//				System.out.println("error sc:graphNodeList is null,"+c1_cla_name);
//				continue;
//			}
//			String sc_cla_name = "";
//			StringBuffer sb_sc_list = new StringBuffer();
//			for (GraphNode gn:graphNodeList) {
//				for (GraphNode gnn : gn.getChildList()) {
//					sc_cla_name = gnn.getData().getWord();
//					String cla_type = gnn.getData().getType();
//					if (sc_cla_name == null || sc_cla_name.isEmpty() || !cla_type.equals("sc")) {
//						// System.out.println("error 3:can not find pa "+cla_name);
//						continue;
//					}
//					sb_sc_list.append(sc_cla_name).append(",");
//				}
//				if(sb_sc_list.length() > 0)
//					break;
//				
//			
//			
//			}
//			
//			
//			String sc_list = sb_sc_list.toString();
//			
//			String[] secs1 = sc_list.split(",");
//
//			candidate_clas top_cddc_sc = new candidate_clas();
//			for(String sc_word:secs1){
//				Float score = hm_sc_candidate.get(sc_word);
//				if(score == null)
//					continue;
//				if(top_cddc_sc.total_vote_score <= 0)
//				{
//					top_cddc_sc.total_vote_score = score;
//					top_cddc_sc.cla_name = sc_word;
//					top_cddc_sc.cla_type = "sc";
//					top_cddc_sc.total_vote_num = hm_sc_candidate_voteNum.get(sc_word);
//				}else if(score > top_cddc_sc.total_vote_score){
//					top_cddc_sc.total_vote_score = score;
//					top_cddc_sc.cla_name = sc_word;
//					top_cddc_sc.cla_type = "sc";
//					top_cddc_sc.total_vote_num = hm_sc_candidate_voteNum.get(sc_word);
//				}
//				
//				
//			}
//			
//			//如果置信
//			if(top_cddc_sc.total_vote_score >= 0.1f && top_cddc_sc.total_vote_num >= 2){
//				al_res.add(top_cddc_sc);
//			}	
//			
//		}
//		
//		
//		return al_res;
//	}
	
	
	/**
	 * 根据输入的所有候选，取最可能的top 1，或者top 2
	 */
	private ArrayList<candidate_clas> chooseSimClaBestC0(HashMap<String, candidate_clas> hm_candidates) {
		// TODO Auto-generated method stub
		if(hm_candidates == null || hm_candidates.size() <= 0)
			return null;
		
		ArrayList<candidate_clas> al_cddcs = new ArrayList<candidate_clas>();
		
		//如果候选只有一个，同时score > 0.1f,voteNum >=2,可以置信
		if(hm_candidates.size() ==1)
		{
			candidate_clas cddc = hm_candidates.entrySet().iterator().next().getValue();
			if(cddc.total_vote_num >= 2 && cddc.total_vote_score >= 0.1f)
			{
				al_cddcs.add(cddc);
				return al_cddcs;
			}else
				return null;
			
			
		}
			

		// sort
		ArrayList<Entry<String, candidate_clas>> maplist_candidates = new ArrayList(
				hm_candidates.entrySet());
		Collections.sort(maplist_candidates,
					new Comparator<Entry<String, candidate_clas>>() {
						@Override
						public int compare(Entry<String, candidate_clas> arg0,
								Entry<String, candidate_clas> arg1) {
							// TODO Auto-generated method stub
							Float result = arg0.getValue().total_vote_score - arg1.getValue().total_vote_score;
							if (result > 0)
								return -1;
							else if (result == 0)
								return 0;
							else
								return 1;
						}
		});
		
		//取按得分排序的候选，要求voteNum>=2,score>=0.1f
		Iterator<Entry<String, candidate_clas>> it = maplist_candidates.iterator();
		while(it.hasNext()){
			candidate_clas cddc = it.next().getValue();
			if(cddc.total_vote_num >= 2 && cddc.total_vote_score >0.1f){
				al_cddcs.add(cddc);
			}
			
		}
	
		return al_cddcs;
		
		
//		// sort vote num
//		Collections.sort(maplist_candidates,
//				new Comparator<Entry<String, candidate_clas>>() {
//					@Override
//					public int compare(Entry<String, candidate_clas> arg0,
//							Entry<String, candidate_clas> arg1) {
//						// TODO Auto-generated method stub
//						Integer result = arg0.getValue().total_vote_num - arg1.getValue().total_vote_num;
//						if (result > 0)
//							return -1;
//						else if (result == 0)
//							return 0;
//						else
//							return 1;
//					}
//		});
//		
//		//如果投票最高的那个，title有给投票，并且得分比最高只低20%以内，那么取之
//		cddc= maplist_candidates.get(0).getValue();
//		if(cddc.title_vote_num >= 1 && cddc.total_vote_score >= (top1_score * 0.8f))
//		{
//			if(sbRes.indexOf(cddc.cla_name) < 0)
//				sbRes.append(cddc.toString());
//		}
//		
//		//如果什么都没有，那么最高投票能有大于1以上的得分也行
//		if(sbRes.length() == 0){
//			if(cddc.total_vote_score >= 1.0f)
//				sbRes.append(cddc.toString());
//		}
		
	}

	/**
	 * 输入文章的words信息，统计其top1到candidate的投票信息
	 */
	private void voteFromWordTop1(
			HashMap<String, candidate_clas> hm_candidates,
			ArrayList<String> al_words) {
		// TODO Auto-generated method stub
		if (al_words == null || hm_candidates == null
				|| hm_candidates.isEmpty())
			return;

		for (int i = 0; i < al_words.size(); i += 3) {
			try {
				String word = al_words.get(i);
				String type = al_words.get(i + 1);
				float p = Float.valueOf(al_words.get(i + 2));

				if (type.equals("s")) {
					// 稿源不处理

				} else if ("#et#kb#ks#kq#kr#nr#x#nz#ne#n#nt#ns#k#"
						.indexOf(type) >= 0) {
					if (type.equals("kb") || type.equals("kq")) {
						if (hm_specialCanCla.get(word) != null) {

							ArrayList<wordDis> al_wordDis = hm_specialCanCla
									.get(word);

							float top1_dis_p = -1.0f;
							for (wordDis cd : al_wordDis) {
								if (cd.p < 0.1f || !cd.cla_type.equals("c0")) {
									break;
								}

								// 术语领域
								if (cd.p == 0.5f && cd.df == 100) {
									candidate_clas cds = hm_candidates
											.get(cd.classname);
									if (cds == null)
										continue;
									cds.first_vote_score = cds.first_vote_score
											+ cd.p * p;
									cds.first_vote_num = cds.first_vote_num + 1;
								} else if (top1_dis_p <= 0) {
									candidate_clas cds = hm_candidates
											.get(cd.classname);
									if (cds == null) {
										top1_dis_p = cd.p;
										continue;
									}
									cds.first_vote_score = cds.first_vote_score
											+ cd.p * p;
									cds.first_vote_num = cds.first_vote_num + 1;

									top1_dis_p = cd.p;
								} else {
									if (cd.p >= top1_dis_p) {
										candidate_clas cds = hm_candidates
												.get(cd.classname);
										if (cds == null)
											continue;
										cds.first_vote_score = cds.first_vote_score
												+ cd.p * p;
										cds.first_vote_num = cds.first_vote_num + 1;
									} else
										break;
								}

							}

						} else if (word.length() >= 6) {// 太长的也可以走普通词
							if (hm_wordCanCla.get(word) != null) {
								ArrayList<wordDis> al_wordDis = hm_wordCanCla
										.get(word);

								float top1_dis_p = -1.0f;
								for (wordDis cd : al_wordDis) {
									if (cd.p < 0.1f
											|| !cd.cla_type.equals("c0")) {
										break;
									}

									// 术语领域
									if (cd.p == 0.5f && cd.df == 100) {
										candidate_clas cds = hm_candidates
												.get(cd.classname);
										if (cds == null)
											continue;
										cds.first_vote_score = cds.first_vote_score
												+ cd.p * p;
										cds.first_vote_num = cds.first_vote_num + 1;
									} else if (top1_dis_p <= 0) {
										candidate_clas cds = hm_candidates
												.get(cd.classname);
										if (cds == null) {
											top1_dis_p = cd.p;
											continue;
										}
										cds.first_vote_score = cds.first_vote_score
												+ cd.p * p;
										cds.first_vote_num = cds.first_vote_num + 1;

										top1_dis_p = cd.p;
									} else {
										if (cd.p >= top1_dis_p) {
											candidate_clas cds = hm_candidates
													.get(cd.classname);
											if (cds == null)
												continue;
											cds.first_vote_score = cds.first_vote_score
													+ cd.p * p;
											cds.first_vote_num = cds.first_vote_num + 1;
										} else
											break;
									}

								}
							}
						}

					} else {
						if (hm_wordCanCla.get(word) != null) {
							if (hm_wordCanCla.get(word) != null) {
								ArrayList<wordDis> al_wordDis = hm_wordCanCla
										.get(word);

								float top1_dis_p = -1.0f;
								for (wordDis cd : al_wordDis) {
									if (cd.p < 0.1f
											|| !cd.cla_type.equals("c0")) {
										break;
									}

									// 术语领域
									if (cd.p == 0.5f && cd.df == 100) {
										candidate_clas cds = hm_candidates
												.get(cd.classname);
										if (cds == null)
											continue;
										cds.first_vote_score = cds.first_vote_score
												+ cd.p * p;
										cds.first_vote_num = cds.first_vote_num + 1;
									} else if (top1_dis_p <= 0) {
										candidate_clas cds = hm_candidates
												.get(cd.classname);
										if (cds == null) {
											top1_dis_p = cd.p;
											continue;
										}
										cds.first_vote_score = cds.first_vote_score
												+ cd.p * p;
										cds.first_vote_num = cds.first_vote_num + 1;

										top1_dis_p = cd.p;
									} else {
										if (cd.p >= top1_dis_p) {
											candidate_clas cds = hm_candidates
													.get(cd.classname);
											if (cds == null)
												continue;
											cds.first_vote_score = cds.first_vote_score
													+ cd.p * p;
											cds.first_vote_num = cds.first_vote_num + 1;
										} else
											break;
									}

								}
							}
						}
					}

				}
			} catch (Exception e) {

			}
		}

	}

	/**
	 * 输入文章的title信息，统计其到candidate的投票信息
	 */
	private void voteFromTitle(HashMap<String, candidate_clas> hm_candidates,
			String title) {
		// TODO Auto-generated method stub
		if (title == null || hm_candidates == null || hm_candidates.isEmpty())
			return;

		// 单独提取title做一次看看,“_w 一_m 剧_k 两星_mq ”_w 的_u 威力_k 究竟_k 有_v 多_m 大_k ？_w
		// 热_k 播_v 剧_k 《_w 花千骨_x 》_w 也_d 赔钱_v ！_w
		String[] word_secs = title.split("[\\s]");
		if (word_secs.length > 0) {
			for (int i = 0; i < word_secs.length; i++) {
				try {
					String word = word_secs[i];
					String type = word.substring(word.indexOf("_") + 1);
					word = word.substring(0, word.indexOf("_"));
					float p = 0.5f;

					if ("#et#kb#ks#kq#kr#nr#x#nz#ne#n#nt#ns#k#".indexOf(type) >= 0) {
						if (type.equals("kb") || type.equals("kq")) {
							p = 1.0f;
							if (hm_specialCanCla.get(word) != null) {

								ArrayList<wordDis> al_wordDis = hm_specialCanCla
										.get(word);
								for (wordDis cd : al_wordDis) {

									if (cd.p > 0.1f && cd.cla_type.equals("c0")) {
										candidate_clas cds = hm_candidates
												.get(cd.classname);
										if (cds == null)
											continue;
										cds.title_vote_score = cds.title_vote_score
												+ cd.p * p;
										cds.title_vote_num = cds.title_vote_num + 1;
									}

								}

							} else if (word.length() >= 6) {// 太长的也可以走普通词
								if (hm_wordCanCla.get(word) != null) {
									ArrayList<wordDis> al_wordDis = hm_wordCanCla
											.get(word);
									for (wordDis cd : al_wordDis) {

										if (cd.p > 0.1f
												&& cd.cla_type.equals("c0")) {
											candidate_clas cds = hm_candidates
													.get(cd.classname);
											if (cds == null)
												continue;
											cds.title_vote_score = cds.title_vote_score
													+ cd.p * p;
											cds.title_vote_num = cds.title_vote_num + 1;
										}

									}
								}
							}

						} else {
							if (hm_wordCanCla.get(word) != null) {
								ArrayList<wordDis> al_wordDis = hm_wordCanCla
										.get(word);
								for (wordDis cd : al_wordDis) {

									if (cd.p > 0.1f && cd.cla_type.equals("c0")) {
										candidate_clas cds = hm_candidates
												.get(cd.classname);
										if (cds == null)
											continue;
										cds.title_vote_score = cds.title_vote_score
												+ cd.p * p;
										cds.title_vote_num = cds.title_vote_num + 1;
									}

								}
							}
						}

					}
				} catch (Exception e) {

				}
			}

		}

	}
	
	
	
	
	/**
	 * 根据输入的word list及分布信息，统计具体的c0投票和得分 hashmap列表
	 * 
	 * 注意：
	 * 
	 * @param 
	 * 
	 * @return 计算状态
	 */
	private String cmpC0Dis(ArrayList<String> al_words, String title,
			HashMap<String, Float> hm_c0_candidate,
			HashMap<String, Integer> hm_c0_candidate_voteNum) {
		
		if (al_words == null || al_words.isEmpty())
			return "";

		if (al_words.size() % 3 != 0)
			return "error1";

		// deal source
		String source = al_words.get(0);
		if (hm_sourceCanCla.get(source) != null) {
			
//			LOG.info(source + "s:"
//					+ JsonUtils.toJson(hm_sourceCanCla.get(source)));
			
//			//@test
//			commenFuncs.writeResult(
//					"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//					"testRes",
//					source + "s:"
//							+ JsonUtils.toJson(hm_sourceCanCla.get(source))
//							+ "\n", "utf-8", true, null);

			
			ArrayList<wordDis> al_wordDis = hm_sourceCanCla.get(source);
			for (wordDis cd : al_wordDis) {
				if (cd.p > 0.1f && cd.cla_type.equals("c0") && cd.df>=5) {
					hm_c0_candidate_voteNum.put(cd.classname, 1);
					hm_c0_candidate.put(cd.classname, cd.p);
				}
				
			}
		}

		// deal et and x kb kq dis,让top 2可以参与投票
		int i = 3;

		for (; i < al_words.size(); i += 3) {
			String word = al_words.get(i);

			String type = al_words.get(i + 1);

			float p = Math.abs(Float.valueOf(al_words.get(i + 2)));

			if (type.equals("kb") || type.equals("kq")) {

				if (hm_specialCanCla.get(word) != null) {
					// LOG.info(word +type+":"+
					// JsonUtils.toJson(hm_wordCanCla.get(word)));
					
//					//@test
//					commenFuncs
//							.writeResult(
//									"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//									"testRes",
//									word + type
//											+ ":"
//											+ JsonUtils.toJson(hm_specialCanCla
//													.get(word)) + "\n",
//									"utf-8", true, null);

					float top1_dis_p = -1.0f;

					ArrayList<wordDis> al_wordDis = hm_specialCanCla.get(word);
					for (wordDis cd : al_wordDis) {
						if (cd.p < 0.1f || !cd.cla_type.equals("c0")) {
							break;
						}

						// 术语领域
						if (cd.p == 0.5f && cd.df == 100) {
							Float p1 = hm_c0_candidate.get(cd.classname);
							if (p1 == null)
								hm_c0_candidate.put(cd.classname, p * cd.p);
							else
								hm_c0_candidate
										.put(cd.classname, p1 + p * cd.p);
							Integer voteNum = hm_c0_candidate_voteNum
									.get(cd.classname);
							if (voteNum == null)
								hm_c0_candidate_voteNum.put(cd.classname, 1);
							else
								hm_c0_candidate_voteNum.put(cd.classname,
										voteNum + 1);

						} else if (top1_dis_p <= 0) {
							Float p1 = hm_c0_candidate.get(cd.classname);
							if (p1 == null)
								hm_c0_candidate.put(cd.classname, p * cd.p);
							else
								hm_c0_candidate
										.put(cd.classname, p1 + p * cd.p);
							Integer voteNum = hm_c0_candidate_voteNum
									.get(cd.classname);
							if (voteNum == null)
								hm_c0_candidate_voteNum.put(cd.classname, 1);
							else
								hm_c0_candidate_voteNum.put(cd.classname,
										voteNum + 1);

							top1_dis_p = cd.p;
						} else {
							if (cd.p >= top1_dis_p) {
								Float p1 = hm_c0_candidate.get(cd.classname);
								if (p1 == null)
									hm_c0_candidate.put(cd.classname, p * cd.p);
								else
									hm_c0_candidate.put(cd.classname, p1 + p
											* cd.p);
								Integer voteNum = hm_c0_candidate_voteNum
										.get(cd.classname);
								if (voteNum == null)
									hm_c0_candidate_voteNum
											.put(cd.classname, 1);
								else
									hm_c0_candidate_voteNum.put(cd.classname,
											voteNum + 1);
								
								
								top1_dis_p = 2.0f;
							} else
								break;
						}

					}

				} else if (word.length() >= 6) {// 太长的也可以走普通词
					if (hm_wordCanCla.get(word) != null) {
						// LOG.info(word +type+"0:"+
						// JsonUtils.toJson(hm_wordCanCla.get(word)));
						
//						//@test
//						commenFuncs
//								.writeResult(
//										"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//										"testRes",
//										word
//												+ type
//												+ "0:"
//												+ JsonUtils
//														.toJson(hm_wordCanCla
//																.get(word))
//												+ "\n", "utf-8", true, null);
						

						float top1_dis_p = -1.0f;

						ArrayList<wordDis> al_wordDis = hm_wordCanCla.get(word);
						for (wordDis cd : al_wordDis) {
							if (cd.p < 0.1f || !cd.cla_type.equals("c0") || cd.df < 3)
								break;

							// 术语领域
							if (cd.p == 0.5f && cd.df == 100) {
								Float p1 = hm_c0_candidate.get(cd.classname);
								if (p1 == null)
									hm_c0_candidate.put(cd.classname, p * cd.p);
								else
									hm_c0_candidate.put(cd.classname, p1 + p
											* cd.p);
								Integer voteNum = hm_c0_candidate_voteNum
										.get(cd.classname);
								if (voteNum == null)
									hm_c0_candidate_voteNum
											.put(cd.classname, 1);
								else
									hm_c0_candidate_voteNum.put(cd.classname,
											voteNum + 1);

							} else if (top1_dis_p <= 0) {
								Float p1 = hm_c0_candidate.get(cd.classname);
								if (p1 == null)
									hm_c0_candidate.put(cd.classname, p * cd.p);
								else
									hm_c0_candidate.put(cd.classname, p1 + p
											* cd.p);
								Integer voteNum = hm_c0_candidate_voteNum
										.get(cd.classname);
								if (voteNum == null)
									hm_c0_candidate_voteNum
											.put(cd.classname, 1);
								else
									hm_c0_candidate_voteNum.put(cd.classname,
											voteNum + 1);

								top1_dis_p = cd.p;
							} else {
								if (cd.p >= top1_dis_p) {
									Float p1 = hm_c0_candidate.get(cd.classname);
									if (p1 == null)
										hm_c0_candidate.put(cd.classname, p * cd.p);
									else
										hm_c0_candidate.put(cd.classname, p1 + p
												* cd.p);
									Integer voteNum = hm_c0_candidate_voteNum
											.get(cd.classname);
									if (voteNum == null)
										hm_c0_candidate_voteNum
												.put(cd.classname, 1);
									else
										hm_c0_candidate_voteNum.put(cd.classname,
												voteNum + 1);
									
									
									top1_dis_p = 2.0f;
								} else
									break;
							}

						}
					}
				}

			} else {
				if (hm_wordCanCla.get(word) != null) {
					// LOG.info(word +type+":"+
					// JsonUtils.toJson(hm_wordCanCla.get(word)));
					
//					//@test
//					commenFuncs
//							.writeResult(
//									"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//									"testRes",
//									word
//											+ type
//											+ ":"
//											+ JsonUtils.toJson(hm_wordCanCla
//													.get(word)) + "\n",
//									"utf-8", true, null);
					

					float top1_dis_p = -1.0f;

					ArrayList<wordDis> al_wordDis = hm_wordCanCla.get(word);
					for (wordDis cd : al_wordDis) {
						if (cd.p < 0.1f || !cd.cla_type.equals("c0") || cd.df < 5) {
							break;
						}

						// 术语领域
						if (cd.p == 0.5f && cd.df == 100) {
							Float p1 = hm_c0_candidate.get(cd.classname);
							if (p1 == null)
								hm_c0_candidate.put(cd.classname, p * cd.p);
							else
								hm_c0_candidate
										.put(cd.classname, p1 + p * cd.p);
							Integer voteNum = hm_c0_candidate_voteNum
									.get(cd.classname);
							if (voteNum == null)
								hm_c0_candidate_voteNum.put(cd.classname, 1);
							else
								hm_c0_candidate_voteNum.put(cd.classname,
										voteNum + 1);

						} else if (top1_dis_p <= 0) {
							Float p1 = hm_c0_candidate.get(cd.classname);
							if (p1 == null)
								hm_c0_candidate.put(cd.classname, p * cd.p);
							else
								hm_c0_candidate
										.put(cd.classname, p1 + p * cd.p);
							Integer voteNum = hm_c0_candidate_voteNum
									.get(cd.classname);
							if (voteNum == null)
								hm_c0_candidate_voteNum.put(cd.classname, 1);
							else
								hm_c0_candidate_voteNum.put(cd.classname,
										voteNum + 1);

							top1_dis_p = cd.p;
						} else {
							if (cd.p >= top1_dis_p) {
								Float p1 = hm_c0_candidate.get(cd.classname);
								if (p1 == null)
									hm_c0_candidate.put(cd.classname, p * cd.p);
								else
									hm_c0_candidate.put(cd.classname, p1 + p
											* cd.p);
								Integer voteNum = hm_c0_candidate_voteNum
										.get(cd.classname);
								if (voteNum == null)
									hm_c0_candidate_voteNum
											.put(cd.classname, 1);
								else
									hm_c0_candidate_voteNum.put(cd.classname,
											voteNum + 1);
								
								
								top1_dis_p = 2.0f;
							} else
								break;
						}

					}

				}

			}

		}
		
		return "success";
		
		
	}
	
	

	/**
	 * 根据输入的word list及分布信息，统计具体的c1投票和得分 hashmap列表
	 * 
	 * 注意：只能用于c1和sc的条件分布统计
	 *     加入rule，如果没有>=0.5的可信kb，那么就不给 sc=读书；
	 * 
	 * @param pa_cla
	 * 		在确认是父节点pa_cla情况下，c1的条件概率分布数据
	 * 
	 * @return 计算状态
	 */
	private String cmpClaDis(String pa_cla,ArrayList<String> al_words, String title,String cla_type,
			HashMap<String, Float> hm_cla_candidate,
			HashMap<String, Integer> hm_cla_candidate_voteNum) {
		
		if (pa_cla == null || pa_cla.isEmpty() ||al_words == null || al_words.isEmpty() || cla_type == null || cla_type.isEmpty())
			return "";

		if (al_words.size() % 3 != 0)
			return "error1";

		// deal source
		//注意：如果是c1，sc等细分层次，source的作用有限，降低权重
		float source_p = 1.0f;
		if(cla_type.equals("c1"))
			source_p = 0.5f;
		if(cla_type.equals("sc"))
			source_p = 0.2f;
		
		String source = al_words.get(0);
		if (hm_sourceCanCla.get(source) != null) {

//			LOG.info(source + "s:"
//					+ JsonUtils.toJson(hm_sourceCanCla.get(source)));
//			commenFuncs.writeResult(
//					"D:\\workspace\\myClassifier\\testenv\\knn_data\\data\\",
//					"testRes",
//					source + "s:"
//							+ JsonUtils.toJson(hm_sourceCanCla.get(source))
//							+ "\n", "utf-8", true, null);

			ArrayList<wordDis> al_wordDis = hm_sourceCanCla.get(source);
			for (wordDis cd : al_wordDis) {
				if(!cd.pa_claName.equals(pa_cla))
					continue;
				
				if (cd.pa_p >= 0.1f && cd.p >= 0.1f && cd.cla_type.equals(cla_type) && cd.df >=5) {
					hm_cla_candidate_voteNum.put(cd.classname, 1);
					hm_cla_candidate.put(cd.classname, source_p*cd.p*cd.pa_p);
				}

			}
		}

		// deal et and x kb kq dis,让top 2可以参与投票
		int i = 3;
		boolean isKB = false;
		for (; i < al_words.size(); i += 3) {
			String word = al_words.get(i);

			String type = al_words.get(i + 1);

			float p = Math.abs(Float.valueOf(al_words.get(i + 2)));

			//确认有可信的kb，用于提示读书、电影等分类
			if(type.equals("kb") && p >= 0.5f)
				isKB = true;
			
			if (type.equals("kb") || type.equals("kq")) {

				if (hm_specialCanCla.get(word) != null) {

					ArrayList<wordDis> al_wordDis = hm_specialCanCla.get(word);
					for (wordDis cd : al_wordDis) {
						if(!cd.pa_claName.equals(pa_cla))
							continue;
						
						if (cd.pa_p >= 0.1f && cd.p >= 0.1f && cd.cla_type.equals(cla_type)) {
							Float p1 = hm_cla_candidate.get(cd.classname);
							if (p1 == null)
								hm_cla_candidate.put(cd.classname, p * cd.p * cd.pa_p);
							else
								hm_cla_candidate.put(cd.classname, p1 + p * cd.p * cd.pa_p);
							
							Integer voteNum = hm_cla_candidate_voteNum
									.get(cd.classname);
							if (voteNum == null)
								hm_cla_candidate_voteNum.put(cd.classname, 1);
							else
								hm_cla_candidate_voteNum.put(cd.classname,
										voteNum + 1);
						}

					}

				} else if (word.length() >= 6) {// 太长的也可以走普通词
					if (hm_wordCanCla.get(word) != null) {

						ArrayList<wordDis> al_wordDis = hm_wordCanCla.get(word);
						for (wordDis cd : al_wordDis) {
							
							if(!cd.pa_claName.equals(pa_cla))
								continue;
							
							if (cd.pa_p >= 0.1f && cd.p >= 0.1f && cd.cla_type.equals(cla_type) && cd.df >= 3) {
								Float p1 = hm_cla_candidate.get(cd.classname);
								if (p1 == null)
									hm_cla_candidate.put(cd.classname, p * cd.p * cd.pa_p);
								else
									hm_cla_candidate.put(cd.classname, p1 + p * cd.p * cd.pa_p);
								Integer voteNum = hm_cla_candidate_voteNum
										.get(cd.classname);
								if (voteNum == null)
									hm_cla_candidate_voteNum
											.put(cd.classname, 1);
								else
									hm_cla_candidate_voteNum.put(cd.classname,
											voteNum + 1);
							}

						}
					}
				}

			} else {
				if (hm_wordCanCla.get(word) != null) {
					ArrayList<wordDis> al_wordDis = hm_wordCanCla.get(word);
					for (wordDis cd : al_wordDis) {
						if (cd.pa_p >= 0.1f && cd.p >= 0.1f && cd.cla_type.equals(cla_type) && cd.df >= 5) {
							
							if(!cd.pa_claName.equals(pa_cla))
								continue;
							
							Float p1 = hm_cla_candidate.get(cd.classname);
							if (p1 == null)
								hm_cla_candidate.put(cd.classname, p * cd.p * cd.pa_p);
							else
								hm_cla_candidate.put(cd.classname, p1 + p * cd.p * cd.pa_p);
							Integer voteNum = hm_cla_candidate_voteNum
									.get(cd.classname);
							if (voteNum == null)
								hm_cla_candidate_voteNum.put(cd.classname, 1);
							else
								hm_cla_candidate_voteNum.put(cd.classname,
										voteNum + 1);
						}

					}
				}
			}
		}
		
		//如果是读书，那么应该有足够置信的KB
		if(isKB == false && hm_cla_candidate.containsKey("读书"))
		{
			hm_cla_candidate.remove("读书");
			hm_cla_candidate_voteNum.remove("读书");
			
		}
		
		return "success";
		
		
	}
	

}
