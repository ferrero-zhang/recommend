package com.ifeng.iRecommend.zxc.bdhotword;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.iRecommend.featureEngineering.itemf;
import com.ifeng.iRecommend.featureEngineering.queryInterface;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.zxc.bdhotword.bean.BDHotWordBean;
import com.ifeng.iRecommend.zxc.bdhotword.manual.CmppArticleSelector;
import com.ifeng.iRecommend.zxc.util.TrieTree;
/**
 * 对文章，使用百度热词，检测出 事件。注意 detect和refresh函数 是非线程安全的。
 * **/
public class BDHotWordDetector {
	static Logger LOG = Logger.getLogger(BDHotWordDetector.class);
	private TrieTree eventTree;
	private long day2=1000*60*60*24*2;
	/**
	 * @field wordDistance 所有的事件会被转化为这种正则形式：韩国{0,n}宣布{0,n}疫情{0,n}结束.
	 * 这里的n就是wordDistance
	 * **/
	private int wordDistance=8;

	/**
	 * @field eventStrLenThreshold 被认为是事件的热词长度必须大于等于eventStrLenThreshold
	 * **/
	private int eventStrLenThreshold=5;
	/**
	 * @field eventWordLenThreshold 被认为是事件的热词分词后的词长度必须大于等于eventWordLenThreshold
	 * **/
	private int eventWordLenThreshold=3;
	/**
	 * @field maxMatched 使用trie树近似匹配时，必须匹配到开头的maxMatched个char
	 * **/
	private int maxMatched=2;
	/**
	 * @field wordExistRatio 由trie树近似匹配获得的热词，令 x=(结果热词字串 交 被匹配字串).len/结果热词字串长度
	 * ,只有x>wordExistRatio，才能认为匹配到该热词
	 * **/
	private double wordExistRatio=0.7d;
	/**
	 * @field eventExistThreshold 匹配到的实体类型热词，在content中出现了eventExistThreshold次以上。
	 * 才会被加入到结果集
	 * **/
	private int eventExistThreshold=2;
	private Set<String> xWords=new HashSet<String>();
	private static Set<String> mayDel=new HashSet<String>();
	private static Set<String> matchPerfect=new HashSet<String>();
	private static Set<String> filter=new HashSet<String>();
	static {
		mayDel.add("曝");
		matchPerfect.add("老人");
		matchPerfect.add("女子");
		matchPerfect.add("健身");
		filter.add("会");
		filter.add("会议");
	}
	private Map<String, String> str2SplitSimpleStr;
	public BDHotWordDetector(){
		
	}

	public BDHotWordDetector(int wordDistance){
		
		this.wordDistance=wordDistance;

	}
	public static String formatDate(Date d,String template){
		//	DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
			DateFormat format1 = new SimpleDateFormat(template);
			String time=format1.format(d);
			return time;
	}
	/**
	 * 
	 * 更新百度热词
	 * 注意它与detect函数是非线程安全的。
	 * @param map 泳钢提供的百度热词map
	 * **/
	public void refresh(Map<String, HotWordInfo> map){
		long nowTime=System.currentTimeMillis();
		Map<String, String> str2SplitSimpleStrTmp=new HashMap<String,String>();
		Set<String> rexSet=new HashSet<String>();
		Set<Entry<String, HotWordInfo>> set=map.entrySet();
		int count=0;
		for(Entry<String, HotWordInfo> en:set){
			HotWordInfo info=en.getValue();
			if(info.isRead()){
				String hotWordSplit=info.getSplitContent();
				String keyTemp=en.getKey().replaceAll("[a-zA-Z0-9]+", "A");
				if(en.getKey().toLowerCase().equals("mh370")){
					System.out.println("zxc");
				}
				if(hotWordSplit.split(" ").length>=eventWordLenThreshold||keyTemp.length()>=eventStrLenThreshold){
					
					boolean ok=true;
					for(String fil:filter){
						if(en.getKey().endsWith(fil)){
							
							ok=false;
						}
					}
					if(!ok){
						continue;
					}
					String rex=hotWordSplit.replaceAll("_[a-z]{1,} {1}", "{"+wordDistance+"}").replaceAll("_[a-z]{1,}", "");
					
					String[] words=hotWordSplit.trim().split(" ");

					if(mayDel.contains(words[0].replaceAll("_[a-z]{1,}", ""))){
						String rex1="";
						for(int i=1;i<words.length;i++){
							rex1+=words[i]+" ";
						}
						rex1=rex1.trim();
						String s=rex1.replaceAll("_[a-z]{1,} {1}", "{"+wordDistance+"}").replaceAll("_[a-z]{1,}", "");
						rexSet.add(s);
						str2SplitSimpleStrTmp.put(s.replaceAll("\\{.*?\\}", ""), rex1.replaceAll("_[a-z]{1,}", "").trim());
					}else{
						rexSet.add(rex);
						str2SplitSimpleStrTmp.put(en.getKey(), hotWordSplit.replaceAll("_[a-z]{1,}", "").replaceAll("\\s+", " "));
					}
				}else{
					long delta=nowTime-info.getLatesttimestamp();
					if(delta<day2){
						String words=hotWordSplit.replaceAll("_[a-z]{1,} {0,1}", "").trim();
						rexSet.add(words);
						xWords.add(words);
						str2SplitSimpleStrTmp.put(words, words);
					}
				}
			}
		}
		//System.out.println("ok count:"+count);
		TrieTree tempEvent=new TrieTree(rexSet);
		eventTree=tempEvent;
		str2SplitSimpleStr=str2SplitSimpleStrTmp;
	}

	
	private List<BDHotWordBean> match(String str,boolean isTitle,String splitStr){
		StringBuffer sb=new StringBuffer();
		sb.append(" ").append(splitStr).append(" ");
		List<BDHotWordBean> rt=new ArrayList<BDHotWordBean>();
		Map<String, List<Integer>> tMatch=eventTree.detect(str);
		
		if(tMatch.size()>0){
			for(String key:tMatch.keySet()){
				List<Integer> pos=tMatch.get(key);
				int existCount=pos.size();
				String key2=key.replaceAll("\\{.*?\\}", "").replaceAll("\\pP", "");
				BDHotWordBean bean=null;
				String type=BDHotWordBean.TYPE_EVENT;
				if(xWords.contains(key2)){
					type=BDHotWordBean.TYPE_UNKNOWN;
				}else{
					continue;
				}
				double score=1;
				if(isTitle==false){
					//0.0025表示每400个字应该出现一次关键词~ 作为一个标准
					score=0.5*(existCount/(str.length()*0.0025));
				}
				if(score>1d){
					score=1d;
				}
				bean=new BDHotWordBean(key2,type , score,existCount,isTitle);
				
				rt.add(bean);
				

			}
		}
		
		return rt;
	}

	/**
	 * 检测文章中的事件。注意它与refresh函数是非线程安全的。
	 * @param titleSplit 标题分词
	 * @param contentSplit 文章内容分词
	 * @return 检测到的事件或实体，若没有检测到结果，返回size=0的Set
	 * **/
	public Set<BDHotWordBean> detect(String titleSplit,String contentSplit){
		Set<BDHotWordBean> rt=new HashSet<BDHotWordBean>();
		try{
			String titleSplitSimple=titleSplit.replaceAll("_[a-z]{1,}", "");
			String title=titleSplit.replaceAll("_[a-z]{1,} {0,1}", "").trim();
			List<BDHotWordBean> rtl=match(title,true,titleSplitSimple);
			String contentSplitSimple=contentSplit.replaceAll("\n", "").replaceAll("_[a-z]{1,}", "");
			String content=contentSplit.replaceAll("_[a-z]{1,} {0,1}", "").trim();
			rtl.addAll(match(content,false,contentSplitSimple));
			for(BDHotWordBean b:rtl){
				if(b.getType().equals(BDHotWordBean.TYPE_UNKNOWN)){
					rt.add(b);
				}
			}
		/*	if(ecount>0){
				rt.addAll(rtl);

				return rt;
			}
			rtl=matchSimilarEvent(title,true,titleSplitSimple);
			if(rtl.size()>0){
				rt.addAll(rtl);
				return rt;
			}
			String[] sentances=content.split("[。！!,，;；:：\"”“]");
			String[] sentancesSS=contentSplitSimple.split("[。！!,，;；:：\"”“]");
			Map<BDHotWordBean,Integer> map=new HashMap<BDHotWordBean,Integer>();
			for(int i=0;i<sentances.length;i++){
				List<BDHotWordBean> rtl2=matchSimilarEvent(sentances[i],false,sentancesSS[i]);
				for(BDHotWordBean rtb:rtl2){
				
					if(map.containsKey(rtb)){
						map.put(rtb, map.get(rtb)+1);
					}else{
						map.put(rtb, 1);
					}
					
				}
			}
			
			for(BDHotWordBean k:map.keySet()){
				int existCount=map.get(k);
				if(k.getType().equals(BDHotWordBean.TYPE_EVENT)&&existCount>=this.eventExistThreshold){
					k.setExsitCount(existCount);
					rtl.add(k);
				}
			}
			rt.addAll(rtl);*/
		}catch(Exception e){
			LOG.error(e.getMessage(),e);
		}
		return rt;
	}

	public static void main(String[] args){
		HotWordData hotWordData =HotWordData.getInstance();
		Map<String, HotWordInfo> map=hotWordData.getHotwordMap();
		BDHotWordDetector d=new BDHotWordDetector();
		d.refresh(map);
		StringBuffer sb=new StringBuffer();
		long s=0l;
		long maxs=0l;
		//int[] arr=new int[]{2438795};
		String str=CmppArticleSelector.getEventTotalCount("制裁朝鲜");
		List<String> ids=new ArrayList<String>();
		JSONObject j=JSONObject.fromObject(str);
		int totalCount=j.getInt("totalCount");
		JSONArray idsArr=j.getJSONArray("data");
		for(int i=0;i<idsArr.size();i++){
			JSONObject o=(JSONObject)idsArr.get(i);
			ids.add(o.getString("id"));
		}
		//for(int i=2320173;i<2326305;i++){
		for(String i:ids){
			i="2585421";
			queryInterface query = queryInterface.getInstance();
			itemf item = query.queryItemF(String.valueOf(i));
			
			if(item==null){
				continue;
			}
			long now=System.currentTimeMillis();
			Set<BDHotWordBean> rt=d.detect(item.getSplitTitle(), item.getSplitContent());
			long ss=System.currentTimeMillis()-now;
			if(ss>maxs){
				maxs=ss;
			}
			s+=ss;
			/*System.out.println("mil="+(System.currentTimeMillis()-now));
			System.out.println("++++++++");*/
			if(rt.size()==0){
				System.out.println(item.getTitle());
			}
			
			sb.append(i).append("\t");
			sb.append(item.getTitle()).append("\t");
			//sb.append("--------\n");
			
			//System.out.println("-------");
			for(BDHotWordBean bean:rt){
				System.out.println(item.getID()+"\t"+item.getTitle()+"\t"+bean.getStr());
				sb.append(bean.getStr()).append(" , ");;
			}
			sb.append("\n");
		//	System.out.println("======"+i+"======");
		}
		FileUtil f=new FileUtil();
		f.Save("e:\\zxc2.txt", sb.toString());
		System.out.println(s);
		System.out.println(maxs);
		
	}
}
