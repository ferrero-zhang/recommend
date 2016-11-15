/**
 * 
 */
package com.ifeng.iRecommend.usermodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.ifeng.commen.Utils.stopwordsFilter;
import com.ifeng.commen.classifyClient.ClassifierClient;
import com.ifeng.iRecommend.dingjw.front_rankModel.RankItem;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation;
import com.ifeng.iRecommend.dingjw.itemParser.channelsParser;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;
import com.ifeng.iRecommend.topicmodel.topicCmpInterface;

/**
 * <PRE>
 * 作用 : 
 *   控制item的抽象特征表达；提供多个具体表达方法给其它模块用；
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
 *          1.0          2013-7-25        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class itemAbstraction {
	
	class int2double{
		int key;
		double value;
	}
	
	/**
	 * 对tag的value进行统一约束，以满足某些场景应用；
	 * 
	 * @param item
	 *        (已经分词)
	 * @return word-value对；
	 * @throws
	 */
	public static HashMap<String, Float> normalization(HashMap<String, Float> hm_in){
		if(hm_in != null){
			Iterator<Entry<String, Float>> it = hm_in.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, Float> et = it.next();
				if(et.getValue() >= fieldDicts.hm_tagLevels.get("B"))
					et.setValue(2.0f);
				else
					et.setValue(1.0f);
			}
		}
		
		return hm_in;
	}
	
	
	/**
	 * 根据具体item的url、ID信息，计算其频道信息并确定权重；如果计算失败，那么调用分类算法补充；如果other字段有分类信息，那么采用这个
	 * 信息来修正或者补充；
	 * 兼容了不同生产系统带来的长url、短url问题；如果只有imcp_id或者url，也会尽量算一个channel出来用；
	 * 
	 * @注意：如果是来自于抓取数据，那么本身就有channel字段信息，这个时候，是不是不用调用分类算法，只用做下分类映射就可以？
	 * 
	 * @param item
	 *        (已经分词)
	 *        idOrUrl
	 *        如果item为空，那么imcp_id或者url也是可以算出一个channel来的
	 * @return word-value对；
	 * @throws
	 */
	public static HashMap<String, Float> cmpChannels(RankItem r_item,String idOrUrl) {
		if(r_item == null || r_item.getItem() == null)
			return null;
		
		HashMap<String, Float> hm_res = cmpChannelsByUrlOrID(r_item.getItem(),idOrUrl);
		
	
		

		if(hm_res == null || hm_res.isEmpty() || hm_res.containsKey("blog")
				|| hm_res.containsKey("iclient")
				|| hm_res.containsKey("rcmd")
				|| hm_res.containsKey("t")){
			hm_res = new HashMap<String, Float>();
			//调用分类算法,如果content内容很短，不太靠谱，那么直接设置content为空，分类结果也为error
			String content = r_item.getItem().getContent();
			if(content == null || content.trim().length() <= 192)
				content = "";
			String categorys = ClassifierClient.predict(
					r_item.getItem().getTitle(), content,
					"",
					"com.ifeng.secondLevelMapping.none",
					null);
	
			if(categorys.startsWith("error")
					||categorys.startsWith("client.error")){
				//...
			}else{
				String[] secs = categorys.split("\\s");
				if(secs.length == 2){
					hm_res.put(secs[0], (float)fieldDicts.hm_tagLevels.get("D"));
					hm_res.put(secs[1],(float)fieldDicts.hm_tagLevels.get("D"));
				}else
					hm_res.put(categorys, (float)fieldDicts.hm_tagLevels.get("D"));
			}
		}
		
		//add likun,解析other字段，加入channel，补充入hm_res
		String others = r_item.getOthers();
		if (others.contains("source=spider")) {
			int b = others.indexOf("channel=");
			if(b > 0)
			{
				int e = others.indexOf("|!|",b);
				if(e > b)
				{
					String channel = others.substring(b+8, e);
					hm_res.put(channel, (float)fieldDicts.hm_tagLevels.get("D"));
				}	
			}
		}
		
		
		return hm_res;
	}
	
	/**
	 * 根据具体item的url、ID信息，计算其分类频道信息并确定权重；
	 * 兼容了不同生产系统带来的长url、短url问题；如果只有imcp_id或者url，也会尽量算一个channel出来用；
	 * @param item
	 *        (已经分词)
	 *        idOrUrl
	 *        如果item为空，那么imcp_id或者url也是可以算出一个channel来的
	 * @return word-value对；
	 * @throws
	 */
	private static HashMap<String, Float> cmpChannelsByUrlOrID(Item item,String idOrUrl) {
		HashMap<String, Float> hm_res = new HashMap<String, Float>();
		//找到可以寻找channel的真正url
		String url = "";
		if(item != null){
			url = item.getUrl();
			//对于cmpp的文章，由于都是短url，得找到背后真正的长url
			if (item.getChannel()!=null && !item.getChannel().isEmpty()) {
				url = item.getChannel();
			}
		}else{
			url = idOrUrl;
		}
			
		if(url == null||url.trim().isEmpty())
			return hm_res;
		
//		//@test，临时处理，将i.ifeng.com等url先丢弃，不做映射，因为脏数据比较多；
//		if(url.indexOf("i.ifeng.com/news") > 0
//				||url.indexOf("3g.ifeng.com") > 0)
//			return hm_res;
		
		float w = fieldDicts.hm_tagLevels.get("D");

		String channels = channelsParser.getInstance(ItemOperation.ItemType.APPITEM)
				.getTransChannel(url, 0);
		if(channels != null && !channels.isEmpty())
		{
			//需要丢弃notopic
			if(channels.startsWith("notopic")
					|| channels.matches("\\d{4,15}"))
				return hm_res;
			
			//@test;丢弃gundong和roll、video
			if(channels.indexOf("roll")>=0
					|| channels.indexOf("gundong")>0
					|| channels.indexOf("video")>=0)
				return hm_res;
			
			String[] channelSecs = channels.split("-");
			for (int i = 0; i < channelSecs.length; i++) {
				String tag = "";
				
				//放过special
				if(channelSecs[i].equals("special")
						||channelSecs[i].equals("a")
						||channelSecs[i].equals("b")
						||channelSecs[i].equals("c"))
					continue;
				
				for (int j = 0; j <= i; j++)
					tag = tag + channelSecs[j];
				//给权重
				if(i < 2)
					w = fieldDicts.hm_tagLevels.get("D");
				else
					w = fieldDicts.hm_tagLevels.get("C");
				hm_res.put(tag, w);	
			}
		}
		
		return hm_res;
	}
	
	/**
	 * 根据item具体信息，计算latent topic
	 * <p>计算topic向量，并取top 2</p>
	 * @param title，分完词的标题
	 * @param content，分完词的内容
	 * @return topic的字符串表达；比如"topic1 topic2" 
	 * @throws
	 */
	public static HashMap<String, Float> cmpLatentTopics(HashMap<String, Float> hm_words){
		HashMap<String, Float> hm_res = new HashMap<String, Float>();
		if(hm_words == null || hm_words.isEmpty())
			return hm_res;

		double[] lTopics = topicCmpInterface.getInstance().cmpItemTopicVector(hm_words);
		if (lTopics != null) {
			HashMap<Integer, Double> hm_topics = new HashMap<Integer, Double>();
			for (int i1 = 0; i1 < lTopics.length; i1++) {
				if (lTopics[i1] <= 0)
					continue;
				hm_topics.put(i1, lTopics[i1]);
			}
			// 取top 2 topics
			ArrayList<Entry<Integer, Double>> topicLists = new ArrayList<Entry<Integer, Double>>(
					hm_topics.entrySet());

			Collections.sort(topicLists,
					new Comparator<Entry<Integer, Double>>() {
						@Override
						public int compare(Entry<Integer, Double> arg0,
								Entry<Integer, Double> arg1) {
							// TODO
							// Auto-generated
							// method
							// stub
							if (arg0.getValue() > arg1.getValue())
								return -1;
							else if (arg0.getValue() < arg1.getValue())
								return 1;
							else
								return 0;
						}

					});

			Iterator<Entry<Integer, Double>> it_topics = topicLists.iterator();
			int topK = 2;
			while (it_topics.hasNext() && topK > 0) {
				Entry<Integer, Double> et_topic = it_topics
						.next();
				int topicid = et_topic.getKey();
				topK--;
				//过滤一些意义有问题的topic
				if(topicid == 12 || topicid == 38 || topicid == 96 || topicid == 63)
					continue;
				float v = et_topic.getValue().floatValue();
				//hm_res.put("topic"+topicid,v*10);//概率小于0.5居多，在userdoc描述时候会丢失，所以临时加权一下；
				if(v < 0.3)
				{
					v = fieldDicts.hm_tagLevels.get("D");
				}else
					v = fieldDicts.hm_tagLevels.get("C");
				//拼装一个value，方便记录原始概率
				hm_res.put("topic"+topicid,v+0.01f*et_topic.getValue().floatValue());
			}
		}
		
		return hm_res;
	}
	
	/**
	 * 根据输入item的title、content、keywords等分词信息，并根据tfidf、动名词过滤得到最好的若干词来表达item；
	 * 
	 * @param item
	 *        (已经分词)
	 * @param useWordsInContent
	 *        (已经分词)
	 * @return word-value对；
	 * @throws
	 */
	public static HashMap<String, Float> getItemTopWords(Item item,boolean useWordsInContent) {
		HashMap<String, Float> hm_res = new HashMap<String, Float>();
		if(item == null ||item.getTitle() == null||item.getTitle().isEmpty())
			return hm_res;
		
		HashMap<String, Float> hm_tmp = new HashMap<String, Float>();
		// title
		String title = item.getTitle();// 新能源_nz 汽车产业_nz 路线图_n 敲定_v _w 十年_mq 将_d 投_v
		String[] wordSecs = title.split("\\s");
		for (int j = 0; j < wordSecs.length; j++) {
			String[] secs = wordSecs[j].split("_");
			if (secs.length != 2)
				continue;
			String word = secs[0];
			String prob = secs[1];
			if (word.length() < 1)
				continue;
			// 过滤停用词
			if (stopwordsFilter.isStopWords(word))
				continue;
			//过滤标点符号
			if(word.matches("[\\pP+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]"))
				continue;
			
			float w = 1.0f;
			// 词性加权？？
			if (prob.indexOf("nr") >= 0 || prob.indexOf("nz") >= 0
					|| prob.indexOf("ns") >= 0) {
				w++;
			}

			hm_tmp.put(word, w);
		}
	
		//keywords(要分词)
		String keywords = item.getKeywords();
		if(keywords != null)
		{
			wordSecs = keywords.trim().split("[,，\\s]");
			for (int j = 0; j < wordSecs.length; j++) {
				String word = wordSecs[j];
				String prob = "";
				int b = -1;
				if((b=wordSecs[j].indexOf("_"))>0){
					word = wordSecs[j].substring(0,b);
					prob = wordSecs[j].substring(b+1);
				}
				if (word.length() <= 1)
					continue;
				// 过滤停用词
				if (stopwordsFilter.isStopWords(word))
					continue;
				float w = 1.0f;
				// 词性加权？？
				if (prob.indexOf("nr") >= 0 || prob.indexOf("nz") >= 0
						|| prob.indexOf("ns") >= 0) {
					w++;
				}
				
				Float real_w = hm_tmp.get(word);
				if (real_w != null)
				{
					hm_tmp.put(word, real_w+w);
				}else{
					hm_tmp.put(word, w);
				}
			}
		}
		// 接下来根据内容进行权重（tf）调整
		String sDetail = item.getContent();
		if (sDetail != null && !sDetail.isEmpty()) {
			String[] secs = sDetail.split("\\s");
			for (int i = 0; i < secs.length; i++) {
				if (!secs[i].isEmpty()) {
					String[] wordsecs = secs[i].split("_");
					if (wordsecs.length != 2)
						continue;
					String word = wordsecs[0];
					String prob = wordsecs[1];

					if (word.length() < 1)
						continue;

					// stopwords
					if (stopwordsFilter.isStopWords(word))
						continue;

					float w = 1.0f;

					if (title.contains(word + "_")
							|| title.contains(" " + word)) {
						w += 2;
					}

					if (keywords != null && keywords.contains(word)) {
						w += 1;
					}

					// 词性加权？？
					if (prob.indexOf("nr") >= 0 || prob.indexOf("nz") >= 0
							|| prob.indexOf("ns") >= 0) {
						w++;
					}
					
					Float real_w = hm_tmp.get(word);
					if (real_w != null)
					{
						hm_tmp.put(word, real_w+w);
					}else if(useWordsInContent){
						hm_tmp.put(word, w);
					}
					
					
				}
			}
		}
		
		// 筛选topK，并调整权重
		Iterator<Entry<String, Float>> it = hm_tmp.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Float> es = it.next();
			float value = (float) Math.sqrt(es.getValue());
			es.setValue(value);
		}

		// 取top K topics
		ArrayList<Entry<String, Float>> wordLists = new ArrayList<Entry<String, Float>>(
				hm_tmp.entrySet());
		Collections.sort(wordLists, new Comparator<Entry<String, Float>>() {
			@Override
			public int compare(Entry<String, Float> arg0,
					Entry<String, Float> arg1) {
				// TODO
				// Auto-generated
				// method
				// stub
				if (arg0.getValue() > arg1.getValue())
					return -1;
				else if (arg0.getValue() < arg1.getValue())
					return 1;
				else
					return 0;
			}

		});

		
		
//		//test
//		System.out.println("show best 20 words:");
		for (int i = 0; i < wordLists.size() && i < 20; i++) {
			Entry<String, Float> et = wordLists.get(i);
//			System.out.println(et.getKey() + " " + et.getValue());
			float value = et.getValue();
			// 规范化value
			if (value >= fieldDicts.hm_tagLevels.get("A"))
				value = (float) fieldDicts.hm_tagLevels.get("A");
			else if (value >= fieldDicts.hm_tagLevels.get("B"))
				value =  (float) fieldDicts.hm_tagLevels.get("B");
			else if (value >= fieldDicts.hm_tagLevels.get("C"))
				value = (float) fieldDicts.hm_tagLevels.get("C");
			else
				value = (float) fieldDicts.hm_tagLevels.get("D");
			
			hm_res.put(et.getKey(), value);

		}
		
		
		
		return hm_res;
	}

	/**
	 * 根据具体item信息，计算其频道信息并确定权重；
	 * 兼容了不同生产系统带来的长url、短url问题；如果只有imcp_id或者url，也会尽量算一个channel出来用；
	 * @param item
	 *        (已经分词)
	 *        idOrUrl
	 *        如果item为空，那么imcp_id或者url也是可以算出一个channel来的
	 * @return word-value对；
	 * @throws
	 */
	public static HashMap<String, Float> cmpChannelsWithIDFs(Item item,String idOrUrl,String sDay,
			HashMap<String, Float> hm_channelIDFs) {
	
		HashMap<String, Float> hm_res = new HashMap<String, Float>();
		
		if(sDay == null || hm_channelIDFs == null)
			return hm_res;
		
		//找到可以寻找channel的真正url
		String url = "";
		if(item != null){
			url = item.getUrl();
			//对于cmpp的文章，由于都是短url，得找到背后真正的长url
			if (item.getChannel()!=null && !item.getChannel().isEmpty()) {
				url = item.getChannel();
			}
		}else{
			url = idOrUrl;
		}
			
		if(url == null||url.trim().isEmpty())
			return hm_res;
		
//		//@test，临时处理，将i.ifeng.com等url先丢弃，不做映射，因为脏数据比较多；
//		if(url.indexOf("i.ifeng.com/news") > 0
//				||url.indexOf("3g.ifeng.com") > 0)
//			return hm_res;
		
		float w = fieldDicts.hm_tagLevels.get("D");

		String channels = channelsParser.getInstance(ItemOperation.ItemType.APPITEM)
				.getTransChannel(url, 0);
		if(channels != null && !channels.isEmpty())
		{
			//需要丢弃notopic
			if(channels.startsWith("notopic")
					|| channels.matches("\\d{4,15}")
					|| channels.indexOf("null") >=0)
				return hm_res;
			
			//@test;丢弃gundong和roll、video
			if(channels.indexOf("roll")>=0
					|| channels.indexOf("gundong")>0
					|| channels.indexOf("video")>=0)
				return hm_res;
			
			
			String[] channelSecs = channels.split("-");
			
			Float idf = hm_channelIDFs.get(sDay+channelSecs[0]);
			if(idf == null){
				idf = 0.2f;
			}
			
			for (int i = 0; i < channelSecs.length; i++) {
				String tag = "";
				
				//放过special
				if(channelSecs[i].equals("special")
						||channelSecs[i].equals("a")
						||channelSecs[i].equals("b")
						||channelSecs[i].equals("c"))
					continue;
				
				for (int j = 0; j <= i; j++)
					tag = tag + channelSecs[j];
				//给权重
				if(i < 2)
					w = fieldDicts.hm_tagLevels.get("D");
				else
					w = fieldDicts.hm_tagLevels.get("C");
				
				//combine idf
				hm_res.put(tag, w*idf);	
			}
		}
		
		
		//add likun,解析other字段，加入channel，补充入hm_res
		if(item != null){
			String others = item.getOther();
			if (others != null && others.contains("source=spider")) {
				int b = others.indexOf("channel=");
				if(b > 0)
				{
					int e = others.indexOf("|!|",b);
					if(e > b)
					{
						String channel = others.substring(b+8, e);
						Float idf = hm_channelIDFs.get(sDay+channel);
						if(idf == null){
							idf = 0.2f;
						}
						hm_res.put(channel,  w*idf);
					}	
				}
			}
		}
		return hm_res;
	}
	

//	/*
//	 * 根据IDF统计信息表，对channel进行TF*IDF
//	 */
//	public static HashMap<String, Float> combineIDFs(String sDay,
//			HashMap<String, Float> hm_channelIDFs) {
//		// TODO Auto-generated method stub
//		//寻找首channel的IDF值
//		if(hm_channelIDFs != null && sDay!= null){
//			Iterator<Entry<String, Float>> it = hm_channelIDFs.entrySet().iterator();
//			while(it.hasNext()){
//				Entry<String, Float> et = it.next();
//				
//			}
//		}
//		
//		
//		return null;
//	}
	
	
}
