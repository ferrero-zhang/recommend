package com.ifeng.iRecommend.kedm.userlog;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.FileUtil;

public class TempUserInfoCache {
	private static final Log log = LogFactory.getLog(TempUserInfoCache.class);
	public static Map<String,String> userid_tempinfo = new ConcurrentHashMap<String,String>();
	
	public static void add(String uid,String info){
		if(uid == null || info == null)
			return;
		if(userid_tempinfo.containsKey(uid)){
			String temp = userid_tempinfo.get(uid);
			userid_tempinfo.put(uid, temp+info);
		}else{
			userid_tempinfo.put(uid, info);
		}
	}
	public static void add(Map<String,Map<String,String>> infos,String type){
		if(infos == null)
			return;
		for(String uid : infos.keySet()){
			Map<String,String> wt = infos.get(uid);
			StringBuffer addinfo = new StringBuffer();
			for(String w : wt.keySet()){
				addinfo.append(w).append("\t").append(type).append("\t").append(wt.get(w)).append("#");
			}
			if(userid_tempinfo.containsKey(uid)){
				String temp = userid_tempinfo.get(uid);
				userid_tempinfo.put(uid, temp+addinfo.toString());
			}else{
				userid_tempinfo.put(uid, addinfo.toString());
			}
		}
	}
	public static void clear(){
		userid_tempinfo.clear();
	}
	
	public static void write2file(String path){
		File f = new File(path);
		if(f.exists()){
			f.delete();
		}
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			for(String uid : userid_tempinfo.keySet()){
				String info = userid_tempinfo.get(uid);
				StringBuffer sb = new StringBuffer();
				for(String line : info.split("#")){
					sb.append(uid).append("\t").append(line).append("\n");
				}
				fileutil.Append2File(path, sb.toString());
			}
			log.info("finish write userinfo cache to file...");
		}catch(Exception e){
			log.error("write userinfo cache to file error!!!",e);
			e.printStackTrace();
		}
		fileutil.CloseRead();
	}

}
