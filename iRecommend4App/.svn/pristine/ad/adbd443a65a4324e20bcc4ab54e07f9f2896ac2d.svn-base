package com.ifeng.iRecommend.featureEngineering.DetectExtrmSim;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.ifeng.commen.Utils.LoadConfig;

import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.ikvlite.IkvLiteClient;

/**
 * 
 * <PRE>
 * 作用 : 传入一篇文章标题放入solr中查询，将返回的近似候选根据抄袭检测算法选出与目标极其相似的文章
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
 *          2.0          2015年5月7日        lixiao          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class DetectExtrmSimDoc {
	private static final Logger LOG = Logger.getLogger(DetectExtrmSimDoc.class);
	private static String pathStopword = LoadConfig
			.lookUpValueByKey("StopWordPath");
	private final static Set<String> swSet = new HashSet<String>();
	private final static String STORE_NAME = "tuiJianSimcla";
	private final static String host1 = "10.32.25.21";
	private final static String host2 = "10.32.25.22";
	private final static String keyspace = "ikv";
	private final static IkvLiteClient client;

	// private static final double paraThreshold = 0.1; //Threshold of
	// paragraphs' similarity
	// private static final double senThreshold = 0.2;//Threshold of sentences'
	// similarity
	// private static Map<String, Set<Long>> index = new HashMap<String,
	// Set<Long>>();
	// private static Map<Long, Set<String>> hscodeToId = new HashMap<Long,
	// Set<String>>();
	public static void close() {
		if (client != null)
			client.close();
	}

	static {
		// LogLog.error("xxxx");
		// LogLog.setInternalDebugging(true);
		// 要求ikv表是强一致性的
		client = new IkvLiteClient(keyspace, STORE_NAME, true);
		client.connect(host1, host2);

		// 加载停用词
		FileReader reader = null;
		try {
			reader = new FileReader(pathStopword);// 读取stopword文件
			BufferedReader alines = new BufferedReader(reader);
			String s1 = null;
			while ((s1 = alines.readLine()) != null)
				swSet.add(s1.trim());
			reader.close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	/*
	 * private static LoadingCache<String, Paragraph> paraCache =
	 * CacheBuilder.newBuilder() .maximumSize(100) // maximum 100 records can be
	 * cached .expireAfterAccess(30, TimeUnit.DAYS) // cache will expire after
	 * 30 days of access .build(new CacheLoader<String, Paragraph>(){ // build
	 * the cacheloader
	 * 
	 * @Override public Paragraph load(String para) throws Exception { //make
	 * the expensive call return loadPara(para); } });
	 * 
	 * 
	 * private static Paragraph loadPara(String para){ return new Paragraph(); }
	 */
	/**
	 * 
	 * 为item给出对应的与target item相似度的分数
	 * 
	 * @param targetItem
	 *            待匹配的item
	 * @param isWritable
	 *            是否将item放入ikv作为传递样本
	 */
	public static Map<itemf, Double> getSimForTarget(itemf targetItem,
			boolean isWritable) {
		if (targetItem == null) {
			LOG.warn("Target item is NULL!");
			return null;
		}

		long start = System.currentTimeMillis();
		Set<Long> hcTarget = makeHashCodeForDoc(targetItem); // Split article by
																// sentences and
																// paragraphs
		// long end = System.currentTimeMillis();
		// LOG.info("[DetectSim INNER]" + (end - start) +
		// " building hash code");
//		 System.out.println(hcTarget + "====" + targetItem.getID());
		if (hcTarget == null) {
			return null;
		}
		// System.out.println("hashCode's size ====" + hcTarget.size());
		/*
		 * Jedis jedisIndexMaster = new Jedis("10.50.8.70", 6379, 10000); Jedis
		 * jedisIndexSlave = new Jedis("10.50.8.70", 6380, 10000); Jedis
		 * jedisCodeMaster = new Jedis("10.50.8.70", 6379, 10000); Jedis
		 * jedisCodeSlave = new Jedis("10.50.8.70", 6380, 10000);
		 * 
		 * jedisIndexMaster.select(3); //searchkey to hashcode
		 * jedisCodeMaster.select(4); //hashcode to id
		 * 
		 * jedisIndexSlave.select(3); jedisCodeSlave.select(4);
		 */

		// start = System.currentTimeMillis();

		Set<String> matchingIds = new HashSet<String>();

		Set<Long> currentCode = new HashSet<Long>();

		int tryAgain = 0;
		while (tryAgain < 2) {
			try {
				// client.truncateTable();
				List<Future<StageRt>> hashCodefrt = HashStage.getHashcodes(
						hcTarget, client, currentCode, isWritable);
				// System.out.println(currentCode + "4444" +
				// targetItem.getID());
				if (currentCode.isEmpty()) 
					LOG.info("[Plagiarism] NO MATCHED HASHCODE FOUND!");
//					long end = System.currentTimeMillis();
//					LOG.info("[DetectSim INNER TIME]" + (end - start));
//					return null;

				List<Future<StageRt>> idsfrt = HashStage.getIds(currentCode,
						client, matchingIds, targetItem.getID(), isWritable);

				if (isWritable) {
					LOG.info("[Plagiarism] Adding ID " + targetItem.getID());
					for (Future<StageRt> rt : hashCodefrt)
						rt.get();
					for (Future<StageRt> rt : idsfrt)
						rt.get();
				}
				break;
			} catch (Exception e) {
				tryAgain++;
				LOG.error("[DetectSim] IKV VISITTING FAILED! TRIED AGAIN ONCE");
			}
		}
		// end = System.currentTimeMillis();
		// System.out.println(end - start + " get candidates articles");
		if (matchingIds.isEmpty()) {
			long end = System.currentTimeMillis();
			LOG.info("[DetectSim INNER TIME]" + (end - start));
			return null;
		}

//		 System.out.println(matchingIds + "xxxx" + targetItem.getID());
		// 改为从hbase读取
		// IndexOperation ob = IndexOperation.getInstance();
		// HBaseOperator ob = HBaseOperator.getInstance();
		ArrayList<itemf> candidates = new ArrayList<itemf>(matchingIds.size());
		for (String id : matchingIds) {
			// start = System.currentTimeMillis();
			// itemf temp = new itemf(HBaseRest.getOne(id));
			itemf temp = HBaseRest.getOne(id, null);
			// end = System.currentTimeMillis();
			// System.out.println(end - start + " get one item from hbase");
			// System.out.println(id + "xxxx");
			if (temp == null)
				continue;
			candidates.add(temp);
			// System.out.println(temp.getID() + "xxxx" + targetItem.getID());
		}

		Map<Integer, Double> resultMap = SentencesMatching.getScore(candidates,
				targetItem);
		if (resultMap == null || resultMap.isEmpty())
			return null;
		Map<itemf, Double> resultMap1 = new HashMap<itemf, Double>(
				resultMap.size());
		for (int i = 0; i < candidates.size(); i++) {
			itemf item = candidates.get(i);
			// String id = item.getID();
			if (resultMap.containsKey(i) && resultMap.get(i) != 0.0) {
				resultMap1.put(item, resultMap.get(i));
			}
		}
		long end = System.currentTimeMillis();
		LOG.info("[DetectSim INNER TIME]" + (end - start));
		return resultMap1;
	}

	/**
	 * 将输入的一篇文章生成hashcode
	 * 
	 * @param collection
	 * @return Sting list 每个单元为一句，段落用"|"标记
	 */
	public static Set<Long> makeHashCodeForDoc(itemf item) {
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

		ArrayList<String> featureList = item.getFeatures();
		Map<String, Integer> weightedWords = null;
		if (featureList != null && !featureList.isEmpty()) {
			weightedWords = new HashMap<String, Integer>(featureList.size() % 3);
			for (int i = 0; i < featureList.size() - 2; i += 3) {
				String clazz = featureList.get(i);
				String label = featureList.get(i + 1);
				String weight = featureList.get(i + 2);
				double w = Double.parseDouble(weight);
				w = Math.abs(w) * 10;
				if (label.contains("c") || label.equals("e"))
					continue;
				if(label.equals("et"))
					w += 5;
				weightedWords.put(clazz, (int) w);
			}
		}

		String[] paragraphs = doc.split("(\\n)+(\\s)*(\\n)*_w");

		if (paragraphs == null || paragraphs.length == 0) {
			LOG.warn("CANNOT TOKENISE CONTENT of item " + item.getID());
			return null;
		}

		String[] articles = new String[paragraphs.length];

		for (int i = 0; i < paragraphs.length; i++) {
			if (paragraphs[i] == null || paragraphs[i].isEmpty())
				continue;
			// System.out.println(paragraphs[i]);
			String[] all_units = paragraphs[i].split("[\u0020\u3000]+");
			StringBuffer content = new StringBuffer();
			int includeFeatureCount = 0;
			for (int j = 0; j < all_units.length; j++) {
				String word = all_units[j];
				if (word == null || word.isEmpty())
					continue;
				if (word.endsWith("_w"))
					continue;
				if (word.toLowerCase().matches(
						"^http:\\/\\/.+\\.(jpg|gif|bmp|jpeg|png|undefined)$"))
					continue;
				String[] stem = word.split("_", 2);
				if (stem.length < 1 || stem[0].isEmpty()
						|| stem[0].length() <= 1 || swSet.contains(stem[0]))
					continue;
				String stemClean = stem[0].trim();
				if (weightedWords == null || weightedWords.isEmpty()) {
					includeFeatureCount = 15;
				} else if (weightedWords.containsKey(stemClean)) {
					// System.out.println(stem[0] + "xxx" + item.getID());
					includeFeatureCount += weightedWords.get(stemClean);
				}
				content.append(stemClean).append(" ");
			}
			// System.out.println(content +"===" + includeFeatureCount);
			if (content.length() <= 0 || (includeFeatureCount < 10 /*&& (double) includeFeatureCount* 2.5 /content.length() < 0.5*/))
				continue;
			// System.out.println(content + "44444");
			String element = content.toString();
			String isNormalResult = isNormalString(element);
			// System.out.println(isNormalResult + "isN");
			if (isNormalResult == null || isNormalResult.isEmpty())
				continue;
			else if (isNormalResult.equals("break"))
				break;
			articles[i] = element;
			// System.out.println(articles[i] + "zzz");
			// String[] clearWords = content.toString().trim().split(" ");
		}

		SimHash s = new SimHash();
		Set<Long> hashcode = s.generateHashCodeForStr(articles);
		// System.out.print(hashcode + " xxx ");
		return hashcode;
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
		str = str.replaceAll("\\s", "");
		if (str.length() < 5) {
			// System.out.println(str);
			return null;
		}
		return SentencesMatching.commonRules(str);
	}

	private static long experiment(FileWriter fw, String[] test,
			IKVOperationv2 ikvop) throws FileNotFoundException {
		if (test == null || test.length <= 0)
			return -1;
		String id = test[0];
		if (id == null || id.isEmpty())
			return -1;
		// System.out.println(id);
		// IndexOperation ob = IndexOperation.getInstance();

		itemf item = null;
		item = ikvop.queryItemF(id, "c");

		if (item == null)
			return -1;

		// ArrayList<itemf> collection = ob.queryFromSolr(item);
		System.out.println("ids_#" + item.getID());
		long start = System.currentTimeMillis();
		Map<itemf, Double> scores = getSimForTarget(item, false);// 抄袭检测

		long end = System.currentTimeMillis();
		System.out.println(end - start + " total");
		System.out.println("-------------------------");
		// System.out.println(scores);
		boolean flag = true;
		if (scores == null)
			return end - start;
		for (itemf id1 : scores.keySet()) {
			// System.out.println(scores.get(i) + " ----" +
			// collection.get(i).getID());
			if (scores.get(id1) != 0) {
				if (id1.getID().equals(item.getID()))
					continue;
				try {
					/*
					 * if (item.getID().length() == id1.getID().length()) { int
					 * itemID = Integer.valueOf(item.getID()); int currentID =
					 * Integer.valueOf(id1.getID()); if (currentID > itemID)
					 * continue; } else { continue; SimpleDateFormat format =
					 * new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss"); try { Date
					 * itemDate = format.parse(item.getPublishedTime()); Date
					 * currentDate = format.parse(id1.getPublishedTime());
					 * if(currentDate.getTime() > itemDate.getTime()) continue;
					 * } catch (ParseException e) { e.printStackTrace(); } }
					 */
					if (flag) {
						fw.write(item.getID() + "\n");
						fw.write(item.getTitle() + "\n");
						flag = false;
					}
					String str = String.format("%.2f", scores.get(id1));
					fw.write(str + " ----" + id1.getID() + " --- "
							+ id1.getTitle() + "\n");
					// fw.write(collection.get(i).getTitle() + "\n");
					// fw.write(collection.get(i).getTitle()+ "\n");
					// fw.write(collection.get(i).getSplitContent() + "\n");
					fw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			fw.write("=====================" + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return end - start;
	}

	public static void main(String[] args) {
		BufferedReader rd = null;
		FileWriter resultWriter = null;

		String tablename = "appitemdb";
		IKVOperationv2 ikvop = new IKVOperationv2(tablename);

		long max = 0;
		long min = 30000;
		long total = 0;
		int handlingCount = 0;
		try {
			rd = new BufferedReader(new InputStreamReader(new FileInputStream(
					"D:\\classify\\new\\抄袭检测实验\\ExtrmSimTestId.txt"), "UTF8"));
			// rd = new BufferedReader(new InputStreamReader(new
			// FileInputStream(
			// "ids"), "UTF8"));
			String line = null;
			resultWriter = new FileWriter(
					"D:\\classify\\new\\抄袭检测实验\\result_test", true);
			// resultWriter = new FileWriter("result_test",
			// false);
			while ((line = rd.readLine()) != null && !line.isEmpty()) {
				String[] test = line.split("\t");
				long usingTime = experiment(resultWriter, test, ikvop);
				if (usingTime > -1) {
					total += usingTime;
					handlingCount++;
					if (usingTime < min)
						min = usingTime;
					if (usingTime > max)
						max = usingTime;
				}

				// try {
				// Thread.sleep(20);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (rd != null) {
				try {
					rd.close();
					resultWriter.flush();
					resultWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Average time is " + (float) total
					/ handlingCount);
			System.out.println("Max time is " + max);
			System.out.println("Min time is " + min);
			StageExcutor.close();
			close();
			ikvop.close();
			System.exit(0);
		}
	}
}
