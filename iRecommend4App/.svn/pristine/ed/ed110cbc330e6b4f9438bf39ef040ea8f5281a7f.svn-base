package com.ifeng.iRecommend.kedm.Spider;

import java.util.HashSet;
import java.util.Set;

import com.ifeng.commen.Utils.FileUtil;

import us.codecraft.webmagic.Spider;

public class TestSpider {
	public static void main(String[] args){
		System.out.println("start spider...");
		Set<String> users = getSinaUser(/*"E:/wbUser.txt"*/args[0]);
		for(String uidurl : users){
			String[] uid_url = uidurl.split("#");
			if(uid_url.length != 2)
				continue;
			Spider.create(new SinaUserPageProcessor(uid_url[0]))
			.addUrl("http://weibo.cn/"+uid_url[1]).run();
			System.out.println("finish spider "+uidurl);
		}
		
	}
	public static Set<String> getSinaUser(String path){
		Set<String> uid_url = new HashSet<String>();
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				if(!line.contains("\"verified\":true"))
					continue;
				String tempuid = line.substring(line.indexOf("\"deviceid\":\"")+12);
				String uid = tempuid.substring(0,tempuid.indexOf("\",\""));
				String tempurl = line.substring(line.indexOf("\"profile_url\":\"")+15);
				String url = tempurl.substring(0,tempurl.indexOf("\",\"")).replace("\\", "");
				uid_url.add(uid+"#"+url);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
		return uid_url;
	}

}
