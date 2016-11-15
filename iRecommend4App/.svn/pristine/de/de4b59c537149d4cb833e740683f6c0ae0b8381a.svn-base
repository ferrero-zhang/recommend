package com.ifeng.iRecommend.likun.userCenter.tnappuc.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.EntityLibQuery;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;

public class HotWordUtil {
	private static final Log LOG = LogFactory.getLog(HotWordUtil.class);
	public static Map<String,Boolean> word_read = new ConcurrentHashMap<String,Boolean>();
	public static Map<String,String> imcpid_word = new ConcurrentHashMap<String,String>();
	public static Map<String,String> uid_testlocal = new ConcurrentHashMap<String,String>();
	public static Set<String> nosense_word = new HashSet<String>();
	public static Map<String,String> ch_tag = new ConcurrentHashMap<String,String>();
	private static final String NOSENSE_WORD_Path = "./conf/nosensewords.txt";
	private static final String ch_tag_Path = "./conf/chTotag.txt";
	public static void init(){
		try{
			loadNosenseWord();
			loadTestlocal();
			loadChToTag();
			EntityLibQuery.init();
			HotWordData hotWordData = HotWordData.getInstance();
			Map<String,HotWordInfo> hw = hotWordData.getHotwordMap();
			if(hw == null || hw.isEmpty()){
				LOG.info("hot word is empty ");
			}
			for(String w : hw.keySet()){
				if(hw.get(w) == null)
					continue;
				boolean read = hw.get(w).isRead();
				String id = hw.get(w).getDocumentId();
				word_read.put(w, read);
				if(id != null){
					imcpid_word.put(id, w);
				}
			}
		}catch(Exception e){
			LOG.error("hot word init error ",e);
			e.printStackTrace();
		}
		LOG.info("finish inti hot word ");
	}
	public static void loadNosenseWord(){
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(NOSENSE_WORD_Path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				nosense_word.add(line);
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
		LOG.info("finish load nosenseword size "+nosense_word.size());
	}
	public static void loadChToTag(){
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(ch_tag_Path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				String[] chtag = line.split("\t");
				if(chtag.length != 2)
					continue;
				ch_tag.put(chtag[1], chtag[0]);
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
		LOG.info("finish load chtag size "+ch_tag.size());
	}
	public static void loadTestlocal(){
		uid_testlocal.put("862784021442369", "内部测试省_内部测试市");
		uid_testlocal.put("860275021036071", "内部测试省_内部测试市");
		uid_testlocal.put("be49f2a5614b4514a7fac8ab469038a5", "内部测试省_内部测试市");
		uid_testlocal.put("04f690e72f1f49d4a0904a2b89ba0130", "内部测试省_内部测试市");
		uid_testlocal.put("3314315879d7497ab19b07cb5563debf", "内部测试省_内部测试市");
	}
	public static boolean isCarlab(String tag,String lab){
		boolean res = false;
		List<EntityInfo> info = EntityLibQuery.getEntityList(lab);
		if(info == null)
			return false;
		if(tag.equals("游戏")){
			for(EntityInfo entity : info){
				if(entity.toString().contains("客户端游戏") 
						|| entity.toString().contains("手机游戏")){
					res = true;
					break;
				}
			}
		}else{
			for(EntityInfo entity : info){
				if(entity.toString().contains("品牌")){
					res = true;
					break;
				}
			}
		}
		
		return res;
	}
	public static void main(String[] args){
		init();
		isCarlab("汽车","香奈儿");
	}

}
