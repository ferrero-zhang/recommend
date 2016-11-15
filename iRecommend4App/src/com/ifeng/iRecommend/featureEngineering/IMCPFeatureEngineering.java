package com.ifeng.iRecommend.featureEngineering;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.hexl.redis.GetUsefulKeyFromRedis;
import com.ifeng.hexl.redis.UsefulKeyToRedis;
import com.ifeng.iRecommend.featureEngineering.DetectExtrmSim.DetectExtrmSimDoc;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.KeywordExtract;
import com.ifeng.iRecommend.featureEngineering.TimeSensitiveJudgement.TimeSensitiveInfo;
import com.ifeng.iRecommend.featureEngineering.dataCollection.XMLExtract;
import com.ifeng.iRecommend.featureEngineering.dataStructure.appBill;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVApplication;
import com.ifeng.iRecommend.zxc.bdhotword.HotWordDetector;
import com.ifeng.iRecommend.zxc.bdhotword.bean.BDHotWordBean;
import com.ifeng.myClassifier.GetSimcFeature;

public class IMCPFeatureEngineering extends BasisInstall {
		static Logger LOG = Logger.getLogger(IMCPFeatureEngineering.class);
		// 致命毒药，优雅关闭后续的工作线程
		private static final String DEADLY_POISON = "DEADLY_POISON";
		/**
		 * 
		 * @param featureList
		 * @param item
		 * @return
		 */
		public static ArrayList<String> GetRelatedItemId(ArrayList<String> featureList, itemf item) {
			String logstr = "id#" + item.getID() + " title#" + item.getTitle();
			ArrayList<String> appRelatedIDs = new ArrayList<String>();
			List<String> wapRelatedIDs = new ArrayList<String>();
			try {
				RelatedItem obj = new RelatedItem();
				Map<String, List<String>> idList = obj.getRelatedItemId(featureList, item.getSplitTitle(), item.getSource());
				if (idList != null) {
					if (idList.get("客户端") != null)
						appRelatedIDs.addAll(idList.get("客户端"));
					if (idList.get("手凤") != null)
						wapRelatedIDs = idList.get("手凤");
				}
			} catch (Exception e) {
				LOG.error(logstr + "[ERROR]Some error in getRelatedItemId.", e);
			}
			LOG.info("[INFO] app relatedIDs for " + logstr + "is" + appRelatedIDs);
			LOG.info("[INFO] wap relatedIDs for " + logstr + "is" + wapRelatedIDs);
			if (wapRelatedIDs == null || wapRelatedIDs.size() < 3)
				wapRelatedIDs = null;
			else {
				wapRelatedIDs.remove(0);
			}
			//相关数据写入手凤端
			IKVApplication.writeWapRelatedIds(item.getID(), item.getUrl(), item.getTitle(), wapRelatedIDs);
			return appRelatedIDs;
		}
		/**
		 * 
		 * @param item
		 * @return
		 */
		public static itemf originalCheck(itemf item) {
			for (String original : originalSet) {
				if (item.getTitle().contains(original)) {
					item.addFeatures(original);
					item.addFeatures("s1");
					item.addFeatures("1.0");
				}
			}
			return item;
		}
		/**
		 * 传入当前的item，命名为itemNow。 使用分词后的title，从solr中进行查询，找到相似度最大的top100（最大值），倒排item
		 * list 使用当前itemnow与itemlist中的item进行对比
		 * 
		 * @param s_title
		 *            已经分词的title
		 * @throws IOException
		 */
		private static itemf plagiarismCheck(itemf itemNow) throws IOException// synchronized
		{
			if (itemNow == null)
				return itemNow;
			String solr_title = itemNow.getSplitTitle();
			if (solr_title == null) {
				LOG.info("solr_title is null.");
			} else
				LOG.info("solr_title is " + solr_title.trim() + "\n");
			// 存储内容抄袭检测的结果
			Map<itemf, Double> docSimMap = null; // = new ArrayList<Double>();
			docSimMap = DetectExtrmSimDoc.getSimForTarget(itemNow,false);
			if (docSimMap != null)
				LOG.info("docSimMap size is " + docSimMap.size());
			else {
				LOG.info("docSimMap is null.");
			}
			ArrayList<String> featureNow = itemNow.getFeatures();
			ArrayList<String> featureEnd = itemNow.getFeatures();
			LOG.info("The original feature is " + featureNow);
			// 对相似度超过一定阈值的item进行处理
			if (docSimMap != null)
				for(Entry<itemf,Double> entry : docSimMap.entrySet())
				{
					itemf itemSaved = entry.getKey();
					if(entry.getValue() > 0.4)
					{
						LOG.info("similar item is " + itemSaved.getID() + "\t" + itemSaved.getTitle() + "\tdocSim=" + entry.getValue() + "\n");
						// 需要合并的item的featureList
						ArrayList<String> featureList = itemSaved.getFeatures();
						// 把旧的item feature合并到新item中
						if (entry.getValue() != 0.5)
							featureEnd = FeatureExTools.mergeFeature(featureEnd, itemNow.getDocType(), featureList, itemSaved.getDocType(), 1);
						featureList.clear();
					}
				}
			// 更新最终的新item features
			featureEnd = ruleModify.modifyResult(itemNow.getSplitTitle(), featureEnd, itemNow.getDocType(), false);
			featureEnd = FeatureExTools.delSpareFea(featureEnd);
			if (featureEnd != null)
				itemNow.setFeatures(featureEnd);
			else
				LOG.warn("featureEnd is null.");
			return itemNow;
		}
		/**
		 * 
		 * @param hm_ItemInfo
		 * @return
		 */
		private static itemf mapToItem(HashMap<String, String> hm_ItemInfo){
			if (hm_ItemInfo == null || hm_ItemInfo.isEmpty())
				return null;
			itemf item = new itemf();
			item.setID(hm_ItemInfo.get("id"));
			item.setTitle(hm_ItemInfo.get("title"));
			item.setSplitTitle(hm_ItemInfo.get("s_title"));
			item.setSource(hm_ItemInfo.get("source"));
			if(item.getSource() == null)
				item.setSource("");
			item.setSplitContent(hm_ItemInfo.get("s_content"));
			item.setUrl(hm_ItemInfo.get("wwwurl"));
			item.setPublishedTime(hm_ItemInfo.get("createtime"));
			item.setDocType(hm_ItemInfo.get("doctype"));
			item.setAppId("recommend");
			item.setOther(hm_ItemInfo.get("other"));
			item.setModifyTimeCurrent();
			return item;
		}
		/**
		 * 
		 * @param hm_ItemInfo
		 * @return
		 * @throws IOException
		 */
		public static itemf itemDeepExtract(itemf item, HotWordDetector d) throws IOException {
			/*****************************从map中取出******************************/
			String logstr = "id#" + item.getID() + " title#" + item.getTitle();
			String relatedTag = "";
			ArrayList<String> relatedIds = new ArrayList<String>();
			itemf cmppitem = null;
			cmppitem = itemop.queryItemF(item.getUrl(), "d");
			ArrayList<String> categoryList = null;
			if (cmppitem == null && item.getTitle()!=null && item.getTitle().length() >= 8)
				cmppitem = itemop.queryItemF(item.getTitle(), "d");
			if (cmppitem != null) {
				item.setFeatures(cmppitem.getFeatures());
				LOG.info("[INFO]" + logstr + " Get feature from cmpp " + cmppitem.getID() + " IKV is " + item.getFeatures());
				relatedIds = GetRelatedItemId(item.getFeatures(), item);
			} else {
				item = originalCheck(item);
//				ArrayList<String> newFeatureList = new ArrayList<String>();
					KeywordExtract ke = kvJudge.getKeywordExtract(item.getSplitTitle(), item.getSplitContent(), null);
					List<String> keywordList = kvJudge.getPreKeyword(ke);
					LOG.info(logstr + "pre keyword list is " + keywordList);
					for (String f : keywordList) {
						item.addFeatures(f);
					}
					if (articleSourceMap == null || articleSourceMap.get(item.getSource()) == null)
						LOG.info(logstr + "[INFO] Its newsource " + item.getSource());
					else if (articleSourceMap.get(item.getSource()).equals("soft") && GetUsefulKeyFromRedis.GetUsefulFlag(item.getSource()) == 1) {
						item.addFeatures(item.getSource());
						item.addFeatures("s1");
						item.addFeatures("1.0");
					} else if (!articleSourceMap.get(item.getSource()).equals("serious") && item.getOther().contains("wemedia") && GetUsefulKeyFromRedis.GetUsefulFlag(item.getSource()) == 1) {
						item.addFeatures(item.getSource());
						item.addFeatures("s1");
						item.addFeatures("1.0");
					}
					long solrCheckTime = System.currentTimeMillis();
					// 从lucene中查询相似
					item = plagiarismCheck(item);
					solrCheckTime = System.currentTimeMillis() - solrCheckTime;
					LOG.info("[TIME]solrCheckTime time is " + solrCheckTime);
//					newFeatureList = newItem.getFeatures();
					boolean needSimc = false;
					if (!item.getDocType().equals("slide") && (item.getFeatures() == null || item.getFeatures().size() < 3))
						needSimc = true;
					else if(item.getDocType().equals("slide"))
						needSimc = false;
					else
						needSimc = !FeatureExTools.ifContainC1(item.getFeatures());
					if (needSimc) {
						ArrayList<String> cList = GetSimcFeature.ClassifyByWord(logstr, item.getSplitTitle(), item.getSource(), item.getFeatures());
						if (cList == null)
							cList = new ArrayList<String>();
						cList.add("simc=true");
						cList.add("k");
						cList.add("-0.01");
						LOG.info(logstr + "cList is " + cList);
						if (cList != null && cList.size() % 3 == 0)
							item.getFeatures().addAll(cList);
					}
					long ruleModifyTime = System.currentTimeMillis();
					item.setFeatures(ruleModify.modifyResult(item.getSplitTitle(), item.getFeatures(), item.getDocType(), false));
					ruleModifyTime = System.currentTimeMillis() - ruleModifyTime;
					LOG.info(logstr + "[INFO] feature after rule modify is " + item.getFeatures() + ".  ModifyTime is " + ruleModifyTime);
					item.setFeatures(FeatureExTools.delSpareFea(item.getFeatures()));
					relatedIds = GetRelatedItemId(item.getFeatures(), item);
					LOG.info(logstr + "List before filter is " + item.getFeatures());
					// 提取文章的类别信息 用于实体词提取的判断
					categoryList = FeatureExTools.whatCategory(item.getFeatures());
					item.setCategory(categoryList);
					ke = kvJudge.getKeywordExtract(item.getSplitTitle(), item.getSplitContent(), categoryList);
					List<String> secondKeywordsList = kvJudge.getPreKeyword(ke);
					// 实体-分类关系
					Map<String, Set<String>> etMap = kvJudge.getEtMap(ke);
					// 实体-全部string关系
					Map<String, String> levelMap = kvJudge.getLevelMap(ke);
					LOG.info(logstr + " category for filter et is " + categoryList);
					List<String> list = kvJudge.getFilterFeature(logstr, etMap, levelMap, secondKeywordsList, item.getSplitTitle(), categoryList);
					if (list == null)
						list = new ArrayList<String>();
					LOG.info(logstr + " feature after filter is " + list);
					ArrayList<String> featureList = new ArrayList<String>();
					if (item.getFeatures() != null)
						for (int i = 0; i < item.getFeatures().size() - 2; i += 3) {
							String type = item.getFeatures().get(i + 1);
							if (type.equals("c") || type.equals("sc") || type.equals("cn") || type.equals("e") || type.equals("s") || type.equals("s1") || type.equals("loc"))
							{
								featureList.add(item.getFeatures().get(i));
								featureList.add(item.getFeatures().get(i + 1));
								featureList.add(item.getFeatures().get(i + 2));
							}
						}
					if(list!=null)
						featureList.addAll(list);
					item.setFeatures(featureList);
				LOG.info("[INFO]Get exfeatureList is " + item.getFeatures());
			}
			if (relatedIds != null && relatedIds.size() >= 1) {
				LOG.info(logstr +" related id is "+ relatedIds);
				relatedTag = relatedIds.remove(0);
			} else
				relatedTag = "";
			boolean realevent = false;
			// 提取百度热词
			Set<BDHotWordBean> HotKey = d.detectWord(item.getSplitTitle(), item.getSplitContent());
			ArrayList<String> hotwordList = new ArrayList<String>();
			// 事件识别，注意返回的注释说明
			BDHotWordBean rt = d.detectEvent(item.getID(), "x",item.getSplitTitle(), item.getSplitContent(),categoryList);
			Jedis eventjedis = new Jedis("10.32.24.194", 6379);
			try {
				eventjedis.select(4);
				String eventday  = null;
				try{
				if(item.getPublishedTime()!=null)
					eventday = item.getPublishedTime().split(" ")[0];
				}catch(Exception e){
					LOG.error(logstr+"ERROR get publish time.");
				}
				d.collectEvent(item.getID(), "x", eventday, rt, eventjedis);
				eventjedis.close();
			} catch (Exception e) {
				LOG.error(logstr + "[ERROR] In write event to Redis.", e);
				eventjedis.close();
			}
			if (rt != null) {
				LOG.info(logstr + "[INFO] Hot event is " + rt.getStr());
				hotwordList.add(rt.getStr());
				hotwordList.add(rt.getType());
				hotwordList.add(String.valueOf(rt.getScore()));
				if(rt.getScore() >= 0.5)
					realevent = true;
			} else {
				LOG.info(logstr + "[INFO] Hot event is null.");
			}
			if (HotKey != null && HotKey.size() >= 1)
				for (BDHotWordBean b : HotKey) {
					LOG.info(logstr + "[INFO] Hot keyword is " + b.getStr());
					hotwordList.add(b.getStr());
					hotwordList.add(b.getType());
					hotwordList.add(String.valueOf(b.getScore()));
				}
			else
				LOG.info(logstr + "[INFO] Hot keyword is null.");
			item.setHotEvent(hotwordList);
			for(int i = 0; i < hotwordList.size(); i++){
				item.addFeatures(hotwordList.get(i));
			}
			ArrayList<String> tempFea = new ArrayList<String>();
			for (int i = 0; i < item.getFeatures().size() - 2; i += 3) {
				if (stopword.contains(item.getFeatures().get(i))) {
				} else {
					tempFea.add(item.getFeatures().get(i));
					tempFea.add(item.getFeatures().get(i + 1));
					tempFea.add(item.getFeatures().get(i + 2));
				}
			}
			item.setFeatures(FeatureExTools.delSpareFea(tempFea));
			ArrayList<String> featureList = extractTags.featureClear(item.getFeatures(), item.getSplitTitle(), relatedTag, logstr);
			LOG.info("[INFO]Get featureList is " + featureList);
			if (featureList == null || featureList.size() == 0) {
				featureList = new ArrayList<String>();
				featureList.add("");
			}
			/***********************时效性**************************/
			if(realevent)
				item.setOther(item.getOther()+"|!|timeSensitive=true");
			else {
				 TimeSensitiveInfo timeSensitiveInfo =
						 timeSensitiveJudgement.EstimateTimeSensitiveOfArticle(item.getSplitTitle(),item.getSplitContent(),categoryList);

				 if(timeSensitiveInfo.isTimeSensitive()){
					 item.setOther(item.getOther()+"|!|timeSensitive=true");
				 }
				 else
					 item.setOther(item.getOther()+"|!|timeSensitive=false");
			}
			appBill appitem = new appBill();
			appitem.setID(item.getID());
			appitem.setTitle(item.getTitle());
			appitem.setUrl(item.getUrl());
			appitem.setRelatedIds(relatedIds);
			appitem.setTags(featureList);
			appitem.setOther("");
			String appJson = null;
			try {
				appJson = JsonUtils.toJson(appitem, appBill.class);
			} catch (Exception e) {
				LOG.info(logstr + "[ERROR] APPItem to Json error.", e);
			}
			appbillop.put("x_" + appitem.getID(), appJson);
			appbillop.put(appitem.getTitle(), appitem.getID());
			appbillop.put(appitem.getUrl(), appitem.getID());
			return item;
		}

		/**
		 * 处理xml文件 注意：
		 * 
		 * @param dirpath
		 *            文件路径
		 * @return counts 解析文件数
		 */
		public static int process(String dirpath) {
			int count = 0;
			String logstr = "";
			LOG.info("[INFO]Process xml file " + dirpath);
			try {
				HashMap<String, String> hm_ItemInfo = new HashMap<String, String>();
				// 判断是否视频xml, 视频xml格式与其他xml格式不一样
				try {
					hm_ItemInfo = XMLExtract.xmlProcess(dirpath);
					if (hm_ItemInfo != null)
						logstr = "id#" + hm_ItemInfo.get("id") + " title#" + hm_ItemInfo.get("title");
					else
						return 0;
				} catch (Exception e) {
					LOG.error(logstr + "Fail to extract with file: " + dirpath);
				}
				itemf item = mapToItem(hm_ItemInfo);
				item = itemDeepExtract(item, hotwordOb);
				if (item != null && item.getFeatures() != null && item.getFeatures().size() >= 1 && !item.getFeatures().get(0).equals("")) {
					count = 1;
					String fjson = "";
					fjson = JsonUtils.toJson(item.getFeatures(), ArrayList.class);
					int redisDbNum = 1;
					Jedis jedis = new Jedis("10.32.24.222", 6379);
					try {
						jedis.select(redisDbNum);
						jedis.set(item.getID(), fjson);
						jedis.set(item.getTitle(), fjson);
						jedis.set(item.getUrl(), fjson);
						jedis.close();
						LOG.info("set-string-succeed:" + item.getID() + "," + fjson);
					} catch (Exception e) {
						LOG.error("set-string-failed:" + item.getID() + "," + fjson, e);
						LOG.error("[ERROR] In write item to Redis.", e);
						jedis.close();
						return -1;
					}
				}
				String itemJson = null;
				try {
					itemJson = JsonUtils.toJson(item, itemf.class);
				} catch (Exception e) {
					LOG.info("[ERROR] Item to Json error.", e);
				}
				ArrayList<String> wordList = new ArrayList<String>();
				if (FeatureExTools.isWeMediaForXML(item.getSource(), item.getOther())) {
					wordList.add(item.getSource());
					wordList.add("swm");
					wordList.add("1");
				}
				if (item.getFeatures() != null)
					wordList.addAll(item.getFeatures());
				UsefulKeyToRedis uktr = new UsefulKeyToRedis();
				uktr.keyToRedis(wordList, item.getPublishedTime());
				/*--------------------item写入ikv-----------------------------*/
				try {
					String dbid = "x_" + item.getID();
					// 数据写入数据库的部分
					itemop.put("x_" + item.getID(), itemJson);
					LOG.info("[INFO] Write " + "x_" + item.getID() + " successful.");
					if (itemop.queryItemF(item.getTitle(), "d") == null) {
						itemop.put(item.getTitle(), item.getID());
						LOG.info("[INFO] Write title " + item.getTitle() + " index " + dbid + " successful.");
					}
					if (itemop.queryItemF(item.getUrl(), "d") == null) {
						itemop.put(item.getUrl(), item.getID());
						LOG.info("[INFO] Write url " + item.getUrl() + " index " + dbid + " successful.");
					}
					LOG.info("[write to ikv]Succeed to write the xml file: " + dirpath);
				} catch (NullPointerException e) {
					LOG.error("[ERROR] [write to ikv]No id in this xml: " + dirpath, e);
				} catch (Exception e) {
					LOG.error("[ERROR]Fail to write ikv with file: " + dirpath, e);
					Thread.sleep(10 * 1000);
				}
			} catch (Exception e) {
				LOG.error("[ERROR] Fail to rename file: " + dirpath, e);
				e.printStackTrace();
			}
			return count;
		}
		/**
		 * 判断一个id是否需要重新解析
		 * 
		 * @param dirpath
		 * @return true表示需要 false表示不需要
		 */
		public static boolean ifIdNeedExtrat(String dirpath, HashSet<String> pidSet) {
			if (dirpath == null || dirpath.isEmpty())
				return false;
			if (dirpath.contains(".xml")) {
				String id = dirpath.split("/")[dirpath.split("/").length - 1].split(".xml")[0];
				LOG.info("Check id " + id + " in IKV.");
				itemf item = itemop.queryItemF(id, "x");
				appBill appitem = appbillop.queryAppBill(id, "x");
				if (item == null || appitem == null)
					return true;

				if (null != pidSet && pidSet.size() > 200 && !pidSet.contains(id)) {
					return false;
				}
				ArrayList<String> tagList = appitem.getTags();
				ArrayList<String> exfeatureList = item.getFeatures();
				if (tagList == null || tagList.isEmpty())
					return true;
				if (!exfeatureList.contains("c") && !exfeatureList.contains("sc") && !exfeatureList.contains("cn"))
					return true;
				if (exfeatureList.contains("simc=true"))
					return true;
				if (tagList.size() <= 4)
					return true;
				if (!tagList.get(tagList.size() - 1).equals(""))
					return true;
				return false;
			}
			return false;
		}
		
		/**
		 * 迭代解析xml文件，如果当前文件是文件夹则遍历该文件夹，如果是xml文件则进行放进待处理集合 注意：
		 * 
		 * @param dirpath
		 *            文件路径，type xml类型：1资讯 3博客 4图片 5视频 6专题
		 * @param delete
		 *            解析后删除为true，解析后不删除为false
		 * @return
		 */
		public static void multiExtractXML(String dirpath, String type, HashSet<String> pidSet,BlockingQueue<String> fileQueue) {
			File dir = new File(dirpath);
			if (!dir.exists())
				return;
			else {
				if (dir.isDirectory()) {
					// 如果该路径是文件夹，则遍历文件夹
					String filenames[] = dir.list();
					Arrays.sort(filenames);
					if (filenames.length > 0) {
						for (String filename : filenames) {
							multiExtractXML(dirpath + "/" + filename, type, pidSet, fileQueue);// ***
						}
					}
				} else {// 如果该路径不是文件夹，则放进待处理集合中
					long nowTime = System.currentTimeMillis();
					File file = new File(dirpath);
					Long fileTime = file.lastModified();
					// 文件创建时间在两小时以内则对其进行解析
					if ((nowTime - fileTime) <= 2 * 60 * 60 * 1000) {
						if (ifIdNeedExtrat(dirpath, pidSet)) {
							LOG.info(dirpath + " need be extracted.");
							LOG.info("[INFO]" + dirpath + " put in process queue.");
							try {
								fileQueue.put(dirpath);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else
							LOG.info(dirpath + " need not be extracted.");
					}
				}
			}
		}
		class PVpid {
			@Expose
			@SerializedName("pid")
			public String pid;
		}
		/**
		 * 
		 * @return
		 */
		public static HashSet<String> getUsefulPid() {
			String url = "http://tongji.ifeng.com:9090/newsapp/loadNewsAppPid?type=imcp";
			String json = CMPPDataCollect.downloadPage(url);
			// System.out.println(json);
			Gson gson = new Gson();
			ArrayList<PVpid> jsonList = gson.fromJson(json, new TypeToken<List<PVpid>>() {
			}.getType());
			HashSet<String> pidSet = new HashSet<String>();
			if (jsonList == null)
				return null;
			for (PVpid pvpid : jsonList) {
				pidSet.add(pvpid.pid);
			}
			LOG.info("Get " + pidSet.size() + " useful pids.");
			return pidSet;
		}

		public static void multiTraverDirs(BlockingQueue<String> fileQueue) {
			System.out.println("Start!");
			String dir_source = "/data/search_datasource/";// ***原始文件目录/data/search_datasource/

			String typePaths[] = { "1", "2", "5", "10", "11" }; // 不同类别的xml 1资讯
																// 2带图片的资讯 5视频 10图片
																// 11图书
			String split = "/";
			while (true) {
				long start = System.currentTimeMillis();
				long nowTime = System.currentTimeMillis();
				Date date1 = new Date(nowTime - 24 * 60 * 60 * 1000);
				SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHH");
				String str1 = sdf1.format(date1);
				String year1 = str1.substring(0, 4);
				String month1 = str1.substring(4, 6);
				String day1 = str1.substring(6, 8);
				Date date2 = new Date(nowTime);
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH");
				String str2 = sdf2.format(date2);
				String year2 = str2.substring(0, 4);
				String month2 = str2.substring(4, 6);
				String day2 = str2.substring(6, 8);
				HashSet<String> pidSet = getUsefulPid();
				for (int i = 0; i < typePaths.length; i++) {
					try {
						// 遍历前一天子目录，把文件路径放入集合xmlSet中
						String dirpath1 = dir_source + typePaths[i] + split + year1 + split + month1 + day1;
						LOG.info("[INFO]Current path is " + dirpath1);
						multiExtractXML(dirpath1, typePaths[i], pidSet, fileQueue);
						// 遍历当天子目录
						String dirpath2 = dir_source + typePaths[i] + split + year2 + split + month2 + day2;
						LOG.info("[INFO]Current path is " + dirpath2);
						multiExtractXML(dirpath2, typePaths[i], pidSet, fileQueue);
					} catch (Exception e) {
						LOG.error("Fail to deal with folder: " + dir_source + typePaths[i]);
						LOG.error("[OVERALL ERROR] ", e);
						continue;
					}
				}
				long end = System.currentTimeMillis();
				if ((end - start) >= 10 * 60 * 1000)
					LOG.info("[TIME]Process used " + (end - start) + " ms.");
				else {
					try {
						long waitTime = 10 * 60 * 1000 - (end - start);
						LOG.info("[INFO]Process waiting" + waitTime + " ms.");
						Thread.sleep(waitTime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		/**
		 * 数据输入处理类
		 */
		static class dataInputThread extends Thread {
			private BlockingQueue<String> fileQueue;

			public dataInputThread(BlockingQueue<String> fileQueue) {
				super("data-input-thread");
				this.fileQueue = fileQueue;
			}
			public void run() {
				try {
					multiTraverDirs(fileQueue);
					fileQueue.put(DEADLY_POISON);// 放置毒药，优雅关闭
				} catch (InterruptedException e) {
					// 在这里可以做一些异常处理
					e.printStackTrace();
					LOG.error("Thread ERROR, the thread has stopped.");
				}
			}
		}
	 
		/** 处理输入jsonItem的线程，可以多线程并发处理，每个线程处理一个item */
		static class TravelQueueThread extends Thread {
			private final static AtomicInteger ThreadCount = new AtomicInteger();
			private BlockingQueue<String> fileQueue;

			public TravelQueueThread(BlockingQueue<String> fileQueue) {
				super("travel-queue-thread-" + ThreadCount.incrementAndGet());
				this.fileQueue = fileQueue;
			}

			public void run() {
				String filepath = null;
				try {
					while ((filepath = fileQueue.take()) != DEADLY_POISON) {
						try {
							process(filepath);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
					fileQueue.put(DEADLY_POISON);// 放置毒药，优雅关闭
				} catch (InterruptedException e) {
					// 在这里可以做一些异常处理
					e.printStackTrace();
				}
			}
		}

		private BlockingQueue<String> fileQueue = new LinkedBlockingQueue<String>(MAX_QUEUE_SIZE);
		private Thread visitFolderThread;
		private Thread[] travelFileThreads;

		/**
		 * 构造函数
		 * 
		 * @param sourceFile
		 * @param travelThreads
		 */
		public IMCPFeatureEngineering(int travelThreads) {
			super();
			// 初始化DEADLY_POISON,抓取cmpp的起始时间（long型 10位的时间戳）
			visitFolderThread = new dataInputThread(fileQueue);
			travelFileThreads = new TravelQueueThread[travelThreads];
			for (int i = 0; i < travelFileThreads.length; i++) {
				travelFileThreads[i] = new TravelQueueThread(fileQueue);
			}
		}

		/**
		 * 开始执行
		 */
		public void start() {
			visitFolderThread.start();
			for (int i = 0; i < travelFileThreads.length; i++) {
				travelFileThreads[i].start();
			}
		}

		/**
		 * 强行终止。请慎用。程序会自动关闭
		 */
		public void terminate() {
			visitFolderThread.interrupt();
			for (int i = 0; i < travelFileThreads.length; i++) {
				travelFileThreads[i].interrupt();
			}
		}
		/**
		 * 测试用例
		 */
		public static void main(String[] args) {
			final int travelThreads = Integer.valueOf(args[0]);
			IMCPFeatureEngineering fetcher = new IMCPFeatureEngineering(travelThreads);
			fetcher.start();
		}
	
 
}
