package com.ifeng.iRecommend.zxc.bdhotword;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;







import com.ifeng.iRecommend.featureEngineering.FeatureExTools;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.CommonDataSub;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.zxc.bdhotword.bean.BDHotWordBean;
import com.ifeng.iRecommend.zxc.bdhotword.event.EventDetector;
import com.ifeng.iRecommend.zxc.bdhotword.event.EventManualDetector;
import com.ifeng.iRecommend.zxc.util.TrieTree;
/**
 * 对文章，使用百度热词，检测出 事件。注意 detect和refresh函数 是非线程安全的。
 * **/

public class HotWordDetector {
	
	static Logger LOG = Logger.getLogger(HotWordDetector.class);
	private Map<String,String> eName2ECombinedName=new HashMap<String,String>();
	private EventDetector emManual;
	private EventDetector em;
	private TrieTree hotWordTree;
	private long day2=1000*60*60*24*30l;
	private int wordDistance=2;
	private long refreshDelta=1000*60*5;
	private Set<String> pNames=new HashSet<String>();
	private Set<String> explicitEventName=new HashSet<String>();
	private long refreshTimeMap=0l;
	private long refreshTime=System.currentTimeMillis();
	/**
	 * @field eventStrLenThreshold 被认为是事件的热词长度必须大于eventStrLenThreshold
	 * **/
	private int eventStrLenThreshold=3;
	/**
	 * @field eventWordLenThreshold 被认为是事件的热词分词后的词长度必须大于等于eventWordLenThreshold
	 * **/
	private int eventWordLenThreshold=3;
		
	private static Set<String> filter=new HashSet<String>();
	static {
		filter.add("全会");
		filter.add("会议");
	}

	public HotWordDetector(){
		refreshMapIfNeed();
	}
	
	public Set<String> getEvent(Map<String, HotWordInfo> map,boolean all){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, -10);
		long oneMonthAgo=calendar.getTime().getTime();
		Set<String> events=new HashSet<String>();
		
		Set<Entry<String, HotWordInfo>> set=map.entrySet();

		for(Entry<String, HotWordInfo> en:set){
			HotWordInfo info=en.getValue();
			
			if(info.isRead()){
				String hotWordSplit=info.getSplitContent();
				String keyTemp=en.getKey().replaceAll("[a-zA-Z0-9]+", "A");
				if(hotWordSplit.split(" ").length>=eventWordLenThreshold||keyTemp.length()>eventStrLenThreshold){
					boolean ok=true;
					for(String fil:filter){
						if(en.getKey().contains(fil)){
							
							ok=false;
						}
					}
					if(!ok){
						continue;
					}
					if(!all&&info.getStarttimestamp()<oneMonthAgo){
						continue;
					}
					
					//if(keyTemp.length()<8){
						DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String latest=format1.format(new Date(info.getLatesttimestamp()));
						String create=format1.format(new Date(info.getStarttimestamp()));
						events.add(en.getKey()+"\t"+create+"\t"+latest+"\t"+hotWordSplit);
					//}
					
				}
			}
		}
		return events;
	}

	public synchronized void  refreshCombinedEName(Jedis jedis){
		long now=System.currentTimeMillis();
		
		if(now-this.refreshTime>this.refreshDelta){
			jedis.select(Config.SAME_EVENT_DB);
			Map<String,String> eName2ECombinedNameTemp=new HashMap<String,String>();
			Set<String> pNamesTemp=new HashSet<String>();
			Set<String> keys=jedis.keys(SameEventCombiner.CHILD_KEY_PREFIX+"*");
			for(String key:keys){
				String pname=jedis.get(key);
				eName2ECombinedNameTemp.put(key.replace(SameEventCombiner.CHILD_KEY_PREFIX, ""), pname);
				pNamesTemp.add(pname);
			}
			this.eName2ECombinedName=eName2ECombinedNameTemp;
			this.refreshTime=now;
			this.pNames=pNamesTemp;
			LOG.info("refresh Combined Event Name~");
		}
	}
	private synchronized void  refreshMapIfNeed(){
		AtomicLong changeTime=HotWordData.getInstance().getHotWordchangeTime();
		if(refreshTimeMap<changeTime.get()||refreshTimeMap==0){
			
			refreshTimeMap=System.currentTimeMillis();
			LOG.info("refresh HotwordMap~changeTime="+changeTime.get()+","+"refreshTimeMap="+refreshTimeMap);
			//System.out.println(HotWordData.getInstance().getHotwordMap().size());
			refresh(HotWordData.getInstance().getHotwordMap());
			
		}
		
	}
	
	private void refresh(Map<String, HotWordInfo> map){
		
		EventDetector emTemp=new EventDetector();
		long nowTime=System.currentTimeMillis();
		
		Set<String> hotWordSet=new HashSet<String>();
		Set<Entry<String, HotWordInfo>> set=map.entrySet();
		Map<String, HotWordInfo> eventMap=new HashMap<String, HotWordInfo>();
		int countAll=0;
		int count8=0;
		for(Entry<String, HotWordInfo> en:set){
			HotWordInfo info=en.getValue();
			
			if(info.isRead()){
				String hotWordSplit=info.getSplitContent();
				if(hotWordSplit.trim().length()==0){
					LOG.info( en.getKey()+" event split is \"\", ignore");
					continue;
				}
				String keyTemp=en.getKey().replaceAll("[a-zA-Z0-9]+", "A");
				boolean isExplicitEvent=false;
				if(info.getEventType()!=null&&info.getEventType().equals("AbstractEvent")){
					isExplicitEvent=true;
					explicitEventName.add(en.getKey());
				}
				if(isExplicitEvent||hotWordSplit.split(" ").length>=eventWordLenThreshold||keyTemp.length()>eventStrLenThreshold){
					countAll++;
					if(keyTemp.length()>=8){
						count8++;
					}
					
					boolean ok=true;
					for(String fil:filter){
						if(en.getKey().contains(fil)){
							
							ok=false;
						}
					}
					if(!ok){
						continue;
					}
					
					DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					format1.format(new Date(info.getLatesttimestamp()));
					eventMap.put(en.getKey().toLowerCase(), en.getValue());
				}else{
					long delta=nowTime-info.getLatesttimestamp();
					if(delta<day2){
						String  wordRex=hotWordSplit.replaceAll("_[a-z]{1,} {1}", "{"+wordDistance+"}").replaceAll("_[a-z]{1,}", "");
						hotWordSet.add(wordRex.toLowerCase());
					}
				}
			}
		}
		LOG.info("all event count is "+countAll+", event's length<8 count is "+count8);
		EventManualDetector emManualTemp=new EventManualDetector();
		emManualTemp.readEvent();
		emTemp.readEvent(eventMap);
		this.em=emTemp;
		this.emManual=emManualTemp;
		TrieTree hotWordTreeTmp=new TrieTree(hotWordSet);
		hotWordTree=hotWordTreeTmp;
	}
	/**
	 * 检测文章中的热词。线程安全
	 * @param titleSplit 标题分词
	 * @param contentSplit 文章内容分词
	 * @return 检测到的热词，若没有检测到结果，返回size=0的Set
	 * **/
	public Set<BDHotWordBean> detectWord(String titleSplit,String contentSplit){
		refreshMapIfNeed();
		Set<BDHotWordBean> rt=new HashSet<BDHotWordBean>();
		try{
			String title=titleSplit.replaceAll("_[a-z]{1,} {0,1}", "").toLowerCase().trim();
			String content=contentSplit.replaceAll("_[a-z]{1,} {0,1}", "").toLowerCase().trim();
			Map<String, List<Integer>> word2Pos= hotWordTree.detect(title);
			for(String word:word2Pos.keySet()){
				BDHotWordBean bean=new BDHotWordBean(word.replaceAll("\\{\\d\\}", ""), BDHotWordBean.TYPE_UNKNOWN, 1);
				rt.add(bean);
			}
			word2Pos= hotWordTree.detect(content);
			for(String word:word2Pos.keySet()){
				//0.0025表示每400个字应该出现一次关键词~ 作为一个标准
				double score=0.5*(word2Pos.get(word).size()/(content.length()*0.0025));
				
				if(score>0.5){
					score=1d;
				}else if(score<0.5){
					score=0.1;
				}else{
					score=0.5;
				}
				
				BDHotWordBean bean=new BDHotWordBean(word, BDHotWordBean.TYPE_UNKNOWN, score);
				rt.add(bean);
				
			}
		
		}catch(Exception e){
			LOG.error(e.getMessage(),e);
		}
		return rt;
	}
	/**
	 * 检测文章中的事件。线程安全
	 * @param docId文章id.
	 *  @param type 取值x(imcp数据)或者c(cmpp数据)
	 * @param titleSplit 标题分词
	 * @param contentSplit 文章内容分词
	 * @param domains 文章所属领域
	 * @return 检测到的事件，若没有检测到结果，返回null。BDHotWordBean.getSameEventNames()可以获得
	 * 与该事件同义的名字。
	 * **/
	public BDHotWordBean detectEvent(String docId,String type,String titleSplit,String contentSplit,List<String> domains){
		refreshMapIfNeed();
		BDHotWordBean bean=null;
		try{
			String title=titleSplit.replaceAll("_[a-z]{1,} {0,1}", "").trim().toLowerCase();
			String content=contentSplit.replaceAll("\n|\r\n", "").replaceAll("_[a-z]{1,} {0,1}", "").trim().toLowerCase();
			
			bean=emManual.detectEvent(title, content);
			if(bean!=null){
				
				String pName=this.eName2ECombinedName.get(bean.getStr());
				if(pName!=null){
					bean.addSameEventName(bean.getStr());
					bean.setStr(pName);
				}

				return bean;
			}
			
			bean=em.detectEvent(title, content);
			if(bean!=null){
				if(emManual.isManualConfig(bean.getStr())){
					bean.setScore(0.1);
				}
				String pName=this.eName2ECombinedName.get(bean.getStr());
				if(pName!=null){
					LOG.info("change eventname="+bean.getStr() +" to pname="+pName);
					bean.addSameEventName(bean.getStr());
					bean.setStr(pName);
					
				}
				if(!explicitEventName.contains(bean.getStr())&&!pNames.contains(bean.getStr())&&bean.getStr().length()<5&&!emManual.isManualConfig(bean.getStr())){
					bean.setType(BDHotWordBean.TYPE_UNKNOWN);
				}
				
			}
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		if(bean!=null&&bean.getScore()>=0){
			
			if(em.filteredByDomain(type+"_"+docId,bean.getStr(),domains )){
				return null;
			}
		}
		return bean;
	}
/*	public BDHotWordBean detectEvent(String titleSplit,String contentSplit,StringBuffer sb){
		refreshMapIfNeed();
		BDHotWordBean bean=null;
		try{
			String title=titleSplit.replaceAll("_[a-z]{1,} {0,1}", "").trim().toLowerCase();
			String content=contentSplit.replaceAll("\n|\r\n", "").replaceAll("_[a-z]{1,} {0,1}", "").trim().toLowerCase();
			bean=emManual.detectEvent(title, content);
			if(bean!=null){
				if(bean.getStr().length()<5&&!emManual.isManualConfig(bean.getStr())){
					bean.setType(BDHotWordBean.TYPE_UNKNOWN);
				}
				
				String pName=this.eName2ECombinedName.get(bean.getStr());
				if(pName!=null){
					bean.addSameEventName(bean.getStr());
					bean.setStr(pName);
				}
				return bean;
			}
			
			bean=em.detectEvent(title, content,sb);
			if(bean!=null){
				//if(emManual.isManualConfig(bean.getStr())){
					//bean.setScore(0.1);
				//}
				if(bean.getStr().length()<5&&!emManual.isManualConfig(bean.getStr())){
					bean.setType(BDHotWordBean.TYPE_UNKNOWN);
				}
				String pName=this.eName2ECombinedName.get(bean.getStr());
				if(pName!=null){
					LOG.info("change eventname="+bean.getStr() +" to pname="+pName);
					bean.addSameEventName(bean.getStr());
					bean.setStr(pName);
					
				}
			}
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
		return bean;
	}*/
	/**
	 * 存储事件文章。
	 * @param id 文章id
	 * @param type 取值x(imcp数据)或者c(cmpp数据)
	 * @param bean 识别到的BDHotWordBean
	 * @param j Jedis的实例，注意调用者需要负责维护Jedis,因为是要频繁调用的
	 * @param articlePublishDay 文章的发布时间，格式为yyyy-MM-dd
	 * @return 
	 * **/
	public void collectEvent(String id,String type,String articlePublishDay,BDHotWordBean bean, Jedis j){
		
		refreshCombinedEName(j);
		
		if(bean!=null&&bean.getType().equals(BDHotWordBean.TYPE_EVENT)&&bean.getScore()>=0.5){
			LOG.info("id="+id+",type="+type+",bean="+bean.getStr()+"_"+bean.getScore());
			j.select(Config.DB_EVENT_COLLECT);
			j.sadd(bean.getStr()+"_"+articlePublishDay, type+"_"+id+"_"+bean.getScore());
		}
	}
	public static void main(String[] args){
		CommonDataSub cds = new CommonDataSub();
		Thread t = new Thread(cds); 
		t.start();

		String tablename = "appitemdb";
		IKVOperationv2 ob = new IKVOperationv2(tablename);
		
		
		HotWordDetector d=new HotWordDetector();
		//http://fashion.cmpp.ifeng.com/Cmpp/runtime/xform!render.jhtml?nodeId=16001&formId=600&viewId=712&id=3739204
		//if(item!=null){
			//while(true){
			for(int i=4699176;i>4689176;i--){	
				
				String id=String.valueOf(i);
				//传入初始化数据
				itemf item = ob.queryItemF(id,"c");
				if(item==null){
					continue;
				}
				Set<BDHotWordBean> words=d.detectWord(item.getSplitTitle(), item.getSplitContent());
				List<String> domains=FeatureExTools.whatCategory(item.getFeatures());
				BDHotWordBean b=d.detectEvent(item.getID(),"c",item.getSplitTitle(), item.getSplitContent(),domains);
				if(b!=null){
					System.out.println(b.getStr()+"---"+b.getScore());
				}else{
					System.out.println("null");
				}
				/*try {
					Thread.sleep(1000*60);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
			
		//}
	//	ob.close();
	}
}
