package com.ifeng.iRecommend.zxc.bdhotword.manual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ifeng.iRecommend.featureEngineering.itemf;
import com.ifeng.iRecommend.featureEngineering.queryInterface;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.zxc.bdhotword.HotWordDetector;
import com.ifeng.iRecommend.zxc.bdhotword.bean.BDHotWordBean;
import com.ifeng.iRecommend.zxc.bdhotword.event.EventBean;
import com.ifeng.iRecommend.zxc.util.FileUtils;

public class ManualFixEvent {
	public static class EventStrBean {
		String name;
		int cmppCount;

		public EventStrBean(String name, int cmppCount) {
			super();
			this.name = name;
			this.cmppCount = cmppCount;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getCmppCount() {
			return cmppCount;
		}
		public void setCmppCount(int cmppCount) {
			this.cmppCount = cmppCount;
		}
		
	}
	public Set<String> getCrawledEvent(){
		HotWordData hotWordData =HotWordData.getInstance();
		Map<String, HotWordInfo> map=hotWordData.getHotwordMap();
		HotWordDetector d=new HotWordDetector();
		return d.getEvent(map,false);
	}
	public void test(int from,int to,String savePath){
		HotWordData hotWordData =HotWordData.getInstance();
		Map<String, HotWordInfo> map=hotWordData.getHotwordMap();
		//识别热词和事件的类
		HotWordDetector d=new HotWordDetector();
		//传入初始化数据
		d.refresh(map);
		StringBuffer sb=new StringBuffer();
		long s=0l;
		long maxs=0l;
		StringBuffer ab=new StringBuffer();
		long all=0;
		Set<String> title=new HashSet<String>();
		//int[] ids=new int[]{2564489,2567364};
		//for(int i=2320173;i<2346305;i++){
		for(int i=from;i<to;i++){
		//for(int i:ids){
			
			queryInterface query = queryInterface.getInstance();
			itemf item = query.queryItemF(String.valueOf(i));
			if(item==null){
				continue;
			}
			if(title.contains(item.getTitle())){
				continue;
			}
			title.add(item.getTitle());
			long now=System.currentTimeMillis();
			//热词识别，注意返回的注释说明
			Set<BDHotWordBean> words=d.detectWord(item.getSplitTitle(), item.getSplitContent());
			//事件识别，注意返回的注释说明
			BDHotWordBean rt=d.detectEvent(item.getSplitTitle(), item.getSplitContent());
			String ss="";
			ss=item.getID()+"\t"+item.getTitle();
			if(rt!=null){
				ss+="\t"+rt.getStr()+"\t"+rt.getScore()+"\t";
			}else{
				ss+="\t \t";
			}
			for(BDHotWordBean w:words){
				ss+=w.getStr()+"("+w.getScore()+")"+",";
			}
			if(words.size()>0||rt!=null){
				ab.append(ss+"\n");
			}
			long n=System.currentTimeMillis()-now;
			all+=n;
			if(maxs<n){
				maxs=n;
			}
			
		}
		try {
			FileUtils.save(ab.toString(), savePath, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(maxs+","+all);
	}
	public Set<String> getEventNameManual(){
		Set<EventBean> beans=EventBean.getBeanFromDb();
		Set<String> eNames=new HashSet<String>();
		for(EventBean bean:beans){
			eNames.add(bean.getName());
			System.out.println(bean.getName());
		}
		
		return eNames;
	}
	public void getEventNameNeedFix(String eventName){
		HotWordData hotWordData =HotWordData.getInstance();
		Map<String, HotWordInfo> map=hotWordData.getHotwordMap();
		HotWordDetector d=new HotWordDetector();
		d.refresh(map);
		String rt="";
			
				try{
				int ok=0;
				int selectCount=0;
				String str=CmppArticleSelector.getEventTotalCount(eventName);
				List<String> ids=new ArrayList<String>();
				JSONObject j=JSONObject.fromObject(str);
				int totalCount=j.getInt("totalCount");
				JSONArray idsArr=j.getJSONArray("data");
				for(int i=0;i<idsArr.size();i++){
					JSONObject o=(JSONObject)idsArr.get(i);
					ids.add(o.getString("id"));
				}
				
				selectCount=ids.size();
				String rts="";
				for(String id:ids){
					
					queryInterface query = queryInterface.getInstance();
					itemf item = query.queryItemF(id);
					if(item==null){
						continue;
					}
					rt+=id+"\t"+item.getTitle()+"\t"+item.getSplitTitle()+"\t";
					Set<BDHotWordBean> words=d.detectWord(item.getSplitTitle(), item.getSplitContent());
					BDHotWordBean bean=d.detectEvent(item.getSplitTitle(), item.getSplitContent());
					for(BDHotWordBean word:words){
						rt+= word.getStr()+",";
					}
					rt+="\t";
					if(bean!=null){
						
							
						rt+=bean.getStr()+"\n";
						
						
					
					}
					if(bean!=null || words.size()>0){
						rts+=rt;
					}
				}
				
				System.out.println(rts);
			}
			catch(Exception em){
				em.printStackTrace();
			}
	}
	public void getRecallInfo(String path1,String path2){
		HotWordData hotWordData =HotWordData.getInstance();
		Map<String, HotWordInfo> map=hotWordData.getHotwordMap();
		HotWordDetector d=new HotWordDetector();
		d.refresh(map);
		Set<String> crawled=getCrawledEvent();
		Set<String> manual=getEventNameManual();
		String rt="事件名称\t创建时间\t最后更新时间\t分词\t搜索总数\t采样数\t匹配数\t未获得数\t匹配比率\n";
		Set<String> title=new HashSet<String>();
		StringBuffer sb=new StringBuffer();
		for(String e:crawled){
			
			String[] earr=e.split("\t");
			String eName=earr[0];
			//if(!manual.contains(eName)){
				try{
				int ok=0;
				int selectCount=0;
				String str=CmppArticleSelector.getEventTotalCount(eName);
				List<String> ids=new ArrayList<String>();
				JSONObject j=JSONObject.fromObject(str);
				int totalCount=j.getInt("totalCount");
				JSONArray idsArr=j.getJSONArray("data");
				for(int i=0;i<idsArr.size();i++){
					JSONObject o=(JSONObject)idsArr.get(i);
					ids.add(o.getString("id"));
				}
				
				
				if(totalCount==0){
					continue;
				}
				selectCount=ids.size();
				int countNotSelect=0;
				for(String id:ids){
					
					queryInterface query = queryInterface.getInstance();
					itemf item = query.queryItemF(id);
					if(item==null){
						countNotSelect++;
						continue;
					}
					BDHotWordBean bean=d.detectEvent(item.getSplitTitle(), item.getSplitContent());
					if(bean!=null){
						if(!title.contains(item.getTitle())){
							title.add(item.getTitle());
							sb.append(id+"\t"+item.getTitle()+"\t"+bean.getStr()+"\n");
						}
						
						ok++;
					}
				}
				
				//if(totalCount<=5&&totalCount>3){
				if(selectCount-countNotSelect>0){
					rt=rt+e+"\t"+totalCount+"\t"+selectCount+"\t"+ok+"\t"+countNotSelect+"\t"+(ok/(double)(selectCount-countNotSelect))+"\n";
				}else{
					rt=rt+e+"\t"+totalCount+"\t"+selectCount+"\t"+ok+"\t"+countNotSelect+"\tNA"+"\n";
				}
					
					System.out.println(e+"\t"+totalCount);
				//}
			}
			catch(Exception em){
				em.printStackTrace();
			}
		}
		//}
		try {
			FileUtils.save(rt, path1, false);
			FileUtils.save(sb.toString(), path2, false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	/*public static void addManualEventBean(String path){
		List<String> lines;
		try {
			lines = FileUtils.readFileByLines(path, false);
		
		for(String line:lines){
			String[] tmp=line.split("\t");
			String name=tmp[0];
			String mayExist=tmp[1];
			String mustExist=tmp[2];
			String mustNotExist=tmp[3];
			int matchWordCountThreshold=Integer.valueOf(tmp[4]);
			EventBean bean=new EventBean(name, mayExist,
					 mustNotExist,  mustExist,
					0,  matchWordCountThreshold);
			bean.save();
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	public static void main(String[] args){
		ManualFixEvent e=new ManualFixEvent();
		e.test(2637445,2654245,"e:/test.txt");
		//e.getRecallInfo("e:/needFix.txt","e:/needFix2.txt");
		
		//e.getEventNameNeedFix("北大学生弑母");
		//Set<String> s=e.getEventNameManual();
		//e.getEventNameNeedFix("");
		
		//e.addManualEventBean("e:/addBean.txt");
	}
}
