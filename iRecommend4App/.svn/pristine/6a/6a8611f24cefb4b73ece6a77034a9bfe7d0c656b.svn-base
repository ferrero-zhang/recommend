package com.ifeng.iRecommend.featureEngineering.DetectExtrmSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;

public class SentencesMatching {
	private static final double senThreshold = 0.2;// Threshold of sentences'
													// similarity
	private static final Log LOG = LogFactory.getLog("SentencesMatching");

	/**
	 * 
	 * 为候选集collection中的item给出对应的与target item相似度的分数
	 * 
	 * @param collection
	 *            候选collection
	 * @param targetItem
	 *            待放入的item
	 */
	public static Map<Integer, Double> getScore(
			ArrayList<itemf> collection, itemf targetItem) {
		if (collection == null || collection.isEmpty()) {
			LOG.warn("Similar list from simHash is NULL/EMPTY!");
			return null;
		}
		if (targetItem == null) {
			LOG.warn("Target item is NULL!");
			return null;
		}
		Map<Integer, Double> resultMap = new HashMap<Integer, Double>(collection.size());
		ArrayList<String> chunks = makeChunksForDoc(targetItem); 
		// Split article by sentences and paragraphs
		// System.out.println(chunks);
		if (chunks == null || chunks.isEmpty()) {
			return null;
		}

		Map<String, Set<Integer>> chunkAsSenTable = new HashMap<String, Set<Integer>>();
		// 按句chunking的hash table

		Map<Integer, Integer> matchSenTable = new HashMap<Integer, Integer>(); 
		// corresponding id to similar factor

		int[] collectionStat = new int[collection.size()]; 
		// sentences and paragraphs statistics of all candidates separately
		for (int i = 0; i < collection.size(); i++) {
			itemf item = collection.get(i);

//			 System.out.print(i + "-" + item.getID() + " ");
			// long start = System.currentTimeMillis();
			ArrayList<String> candi_chunks = makeChunksForDoc(item);
			if (candi_chunks == null)
				continue;
			// LOG.info("单篇切片" + (System.currentTimeMillis() - start));

			// start = System.currentTimeMillis();
			// 建立候选集句子倒排表 建立候选集段落倒排表
			int temp = initChunkTable(candi_chunks, chunkAsSenTable, i);
			collectionStat[i] = temp; // the number of sentences of i-th
											// candidate item

			// LOG.info("单篇存入map" + (System.currentTimeMillis() - start));
		}

		// System.out.println();

		if (chunkAsSenTable.isEmpty()) {
			LOG.warn("NO SOLID CONTENT of candidate articles!");
			return null;
		}
		// LOG.info("Finished to initializing candidate inverse indices!");
		// long start = System.currentTimeMillis();
		int interTemp = handleTargetChunk(chunks, chunkAsSenTable, matchSenTable); 
		// Match sentences and paragraphs
		// LOG.info("匹配句子对应ids" + (System.currentTimeMillis() - start));

		// Calculate the corresponding factor of matching sentences and
		// paragraphs
		ArrayList<Double> senFeature = getInterScore(collectionStat,
				matchSenTable, interTemp);
		// LOG.info("Finished to calculating sentences' scores!");

		// LOG.info("Finished to calculating paragraphs' scores!");

		// System.out.println(senFeature);
		// System.out.println(paraFeature);

		// Handle the split title of target item by removing word labels and
		// punctuation
//		String targetTitle = targetItem.getSplitTitle();
//		if (targetTitle != null && !targetTitle.isEmpty()) {
//			targetTitle = targetTitle.replaceAll("_[a-vx-z]+|._w", "");
//		}
		// Calculate ultimate score by using threshold pair discretely and
		// directed
		for (int i = 0; i < collection.size(); i++) {
			int k = i * 2;
			itemf item = collection.get(i);
			if (item == null || item.getFeatures() == null
					|| item.getFeatures().isEmpty()) {
				resultMap.put(i, 0.0);
				continue;
			} else if (item.getID().equals(targetItem.getID())) {
				resultMap.put(i, 1.0);
				continue;
			}
			if (senFeature.get(k) >= 0.8 && senFeature.get(k + 1) >= 0.8) { // 两篇几乎一样
				resultMap.put(i, 1.0);
				continue;
			} else if (senFeature.get(k) > senThreshold
					&& senFeature.get(k + 1) > senThreshold) { // 互相抄袭
				resultMap.put(i, 0.8);
				continue;
			} else if (senFeature.get(k) > senThreshold) { // 待匹配到候选的抄袭得分
				resultMap.put(i, 0.5);
				continue;
			} else if (senFeature.get(k + 1) > senThreshold) { // 候选到待匹配的抄袭得分
				resultMap.put(i, -0.5);
				continue;
			}
			resultMap.put(i, 0.0);
		}
		// LOG.info(ultimateFeature);
		// LOG.info("Finished to calculating ultimate scores");
		return resultMap;
	}

	/**
	 * 将传入的chunk list 存入对应的倒排表中
	 * 
	 * @param chunks
	 *            已被chunk的文本list
	 * @param senTable
	 *            句子倒排表
	 * @param paraTable
	 *            段落倒排表
	 * @param id
	 *            对应的文章id
	 * @param c1Set
	 *            对应的c1 分布
	 */
	public static int initChunkTable(ArrayList<String> chunks,
			Map<String, Set<Integer>> senTable, int id) {
		int totalSen = 0;
		StringBuffer paraBuffer = new StringBuffer();
		for (int j = 0; j < chunks.size(); j++) {
			String sen = chunks.get(j);

			Set<Integer> ids = senTable.get(sen);
			if (ids == null)
				ids = new HashSet<Integer>();
			ids.add(id);
			senTable.put(sen, ids);
			paraBuffer.append(sen);
			// String[] words = sen.split("\\s+");
			// if (words == null || words.length <= 0)
			// continue;
			totalSen++;
		}

		// System.out.println(senTable);
		// System.out.println(result[0]);
		// System.out.println(result[1]);
		return totalSen;
	}

	/**
	 * 根据targetChunk填充备选id与重合句子/段落数目对应表，同时返回target item 全部的句子数和词总数
	 * 
	 * @param targetChunk
	 *            待计算item对应的句子list
	 * @param senTable
	 *            备选句子倒排表
	 * @param paraTable备选段落倒排表
	 * @param matchSenTable
	 *            id与重合句子的数目对应表
	 * @param matchParaTable
	 *            id与重合段落的数目对应表
	 * @return 总次数和句子总数 （用于计算对应的重合权重）
	 */

	public static int handleTargetChunk(ArrayList<String> targetChunk,
			Map<String, Set<Integer>> senTable,
			Map<Integer, Integer> matchSenTable) {
		if (targetChunk == null || targetChunk.isEmpty())
			LOG.warn("NO SOLID SENTENCES of targetItem!");

		// System.out.println(targetChunk);

		int senCount = 0;

		for (int i = 0; i < targetChunk.size(); i++) {
			String sen = targetChunk.get(i);
			int weight = 1;
			if(sen.toLowerCase().matches(
						"^http:\\/\\/.+\\.(jpg|gif|bmp|jpeg|png|undefined)$"))
				weight = 5;
			if(i <  targetChunk.size() / 5)
				weight = 2;
			if (fillMatchesTable(sen, senTable, matchSenTable, 1))
				senCount+= weight;

		}

		return senCount;

	}

	/**
	 * 计算对应id的对应抄袭度分数
	 * 
	 * @param collection
	 * @param matchesTable
	 * @param factor
	 * @return
	 */

	private static ArrayList<Double> getInterScore(int[] collection,
			Map<Integer, Integer> matchesTable, int factor) {
		ArrayList<Double> scores = new ArrayList<Double>();
		for (int i = 0; i < collection.length; i++) {
			int factor_candi = collection[i];
			if (factor_candi == 0 || matchesTable.get(i) == null) {
				scores.add(0.0);
				scores.add(0.0);
				continue;
			}
			double score1 = (double) matchesTable.get(i) / factor; // 待匹配文章到候选文章抄袭得分
			double score2 = (double) matchesTable.get(i) / factor_candi; // 候选文章到待匹配文章抄袭得分
			// System.out.println(matchesTable);
			scores.add(score1);
			scores.add(score2);
		}

		// System.out.println(matchesTable);
		// System.out.println(scores);

		return scores;
	}

	private static boolean fillMatchesTable(String chunk,
			Map<String, Set<Integer>> chunkTable,
			Map<Integer, Integer> matchesTable, int wordCount) {
		if (chunkTable.containsKey(chunk)) {
			Set<Integer> ids = chunkTable.get(chunk);
			// if (ids.size() > 5 && chunk.length() < 80)
			// return false;
			for (Integer id : ids) {
//				 if(id == 22){
//				 System.out.println(chunk + "===");
//				 System.out.println(wordCount);
//				 }
				Integer temp = matchesTable.get(id);
				if (temp != null)
					matchesTable.put(id, temp + wordCount);
				else
					matchesTable.put(id, wordCount);
			}
		}
		return true;
	}

	/**
	 * 将输入的一篇文章分成chunk
	 * 
	 * @param collection
	 * @return Sting list 每个单元为一句，段落用"|"标记
	 */
	public static ArrayList<String> makeChunksForDoc(itemf item) {
		if (item == null) {
			LOG.warn("ITEM is NULL!!");
			return null;
		}
		if (item.getTitle() != null && item.getTitle().contains("招聘")
				&& item.getTitle().contains("编辑"))
			return null;
		String doc = item.getSplitContent();
		// System.out.println(doc);
		if (doc == null || doc.isEmpty()) {
			LOG.warn("NO CONTENT of item " + item.getID());
			return null;
		}
		// 将多个标点符号合一
		Pattern p = Pattern.compile("([。！？；：…”—\\-=]_w\u0020){2,}");
		Matcher m = p.matcher(doc);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String sub = m.group().replaceAll("\\$", "\\\\\\$");
			m.appendReplacement(sb, sub.replaceAll("_w\u0020", ""));
			sb.append("_w ");
		}
		m.appendTail(sb);
		// System.out.println(sb);
		String[] all_units = sb.toString().split("[\u0020\u3000]+");

		if (all_units == null || all_units.length == 0) {
			LOG.warn("CANNOT TOKENISE CONTENT of item " + item.getID());
			return null;
		}

		StringBuffer sentence = new StringBuffer("");
		ArrayList<String> sen = new ArrayList<String>();

		String docType = item.getDocType();
		boolean withPic = false;

		if (docType == null || docType.equalsIgnoreCase("slide")
				|| docType.equalsIgnoreCase("docpic")) // 文件类型缺失或表示含图，置含图变量为true
			withPic = true;

		for (int i = 0; i < all_units.length; i++) {
			String unit = all_units[i];
			if (unit.equals(",_w"))
				unit = "，_w";// 将英文标点转换成中文
			if (unit.matches("”?[。！？\\?；…—]+”?_w")) { // 遇到句子结束符
				sentence.append(unit); // 将结束符放入当前句子buffer中
				String str = sentence.toString().trim();
				sentence.delete(0, sentence.length());
				str = isNormalString(str);
				if (str == null)
					continue;
				else if (str.equals("break"))
					break;
				sen.add(str); // 放入句子list
				sentence.delete(0, sentence.length()); // 句子为非规范语句时清除句子buffer
			} else if (unit.matches("[\\n\\r]+_w")) { // 遇到段落结束符
				String str = sentence.toString().trim();
				sentence.delete(0, sentence.length());
				str = isNormalString(str);
				if (str == null || str.isEmpty()) {
					continue;
				} else if (str.equals("break"))
					break;
				sen.add(str); // 放入句子list

			} else if (withPic
					&& unit.toLowerCase()
							.matches(
									"^http:\\/\\/.+\\.(jpg|gif|bmp|jpeg|png|undefined)$")) {
				sen.add(unit);
			} else if (unit.startsWith("讯_") || unit.startsWith("报道_")
					|| unit.startsWith("电_") || unit.startsWith("】_")
					|| unit.startsWith("快讯_")) {
				if (sentence.length() < 35)
					sentence.delete(0, sentence.length());
				else
					sentence.append(unit);
			} else if (!unit.isEmpty()) {
				sentence.append(unit);
				// sentence.append(" ");
			}
		}
		/*
		 * 将末尾的句子放入句子list
		 */
		String str = sentence.toString().trim();
		str = isNormalString(str);
		if (str != null && !str.equals("break")) {
			sen.add(str);
		}
		sentence.delete(0, sentence.length());
		// System.out.println(sen);
		if (sen.isEmpty()) {
			LOG.warn("NO VALID CONTENT of item " + item.getID());
			return null;
		}
		return sen;
	}

	/**
	 * 判定句子是否是可用的句子
	 * 
	 * @param str
	 * @return
	 */
	private static String isNormalString(String str) {
		if (str == null || str.isEmpty())
			return null;
		str = str.replaceAll("^[.,:\\-，。？—]+_w", "");
		str = str.replaceAll("\\(.+\\)", "");
		Pattern p = Pattern.compile("_x");
		Matcher m = p.matcher(str);
		int count = 0;
		while (m.find()) {
			count++;
		}
		if (count < 3 && !str.matches(".*[《：“]+.+[》”].*") && str.length() < 15) {
			return null;
		}
		if (str.length() < 5) {
			// System.out.println(str);
			return null;
		}
		str = str.replaceAll("_[a-z]+", "");
		return commonRules(str);
	}
	
	static String commonRules(String str){

		String temp = new String(str);
		temp = temp.replaceAll("&;", "");
		temp = temp.replaceAll("middot", "");
		
		if (temp == null || temp.isEmpty())
			return null;
		if (temp.length() > 5 && temp.matches(".*[\\-—=]{5,}.*"))
			return null;
		if (commenFuncs.stringLength(temp) < 6)
			return null;
		if (temp.contains("y2.ifengimg.com/a/2015_30/366e34663d8f711.jpg"))
			return null;
		if (temp.contains("原标题"))
			return null;
		if (temp.contains("@ifeng.com"))
			return null;
		if (temp.contains("转载") && commenFuncs.stringLength(temp) < 6)
			return null;
		if (temp.contains("点击图片进入"))
			return null;
		if (temp.contains("图片") && temp.length() <= 10)
			return null;
		if (temp.contains("本文来自互联网"))
			return null;
		if (temp.contains("微信") && temp.contains("关注"))
			return null;
		if (temp.contains("二维码")
				&& (temp.contains("扫描") || temp.contains("识别")))
			return null;
		if ((temp.contains("微信") || temp.contains("公号") || temp.contains("公众号"))
				&& temp.matches(".*[0-9a-zA-Z]{3,}.*"))
			return null;
		if (temp.contains("招聘") || temp.contains("推广") || temp.contains("版权声明")
				|| temp.contains("风险提示") || temp.contains("免责声明")
				|| temp.contains("题记")
				|| (temp.contains("笔者") && (temp.contains("寄语") || temp.contains("介绍")))) {
			if (commenFuncs.stringLength(temp) < 6)
				return "break";
				else
					return null;
		}
		return temp;
	}
}
