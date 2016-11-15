package com.ifeng.iRecommend.kedm.ipPredict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.ifeng.commen.Utils.FileUtil;

public class IpIndexTest {
	public static Map<String,Set<String>> user_locs = new HashMap<String,Set<String>>();
	public static Map<String,Set<String>> user_ips = new HashMap<String,Set<String>>();
	public static Map<String,List<String>> ip_locs = new ConcurrentHashMap<String,List<String>>();
	public static void main(String[] args){
		getUserid("E:/requestLog.log.2016-03-21");
		getUserid("E:/requestLog.log.2016-03-20");
		getUserid("E:/requestLog.log.2016-03-19");
		getUserid("E:/requestLog.log.2016-03-22");
		getUserid("E:/requestLog.log.2016-03-23");
		getUserid("E:/requestLog.log.2016-03-24");
		getUserid("E:/requestLog.log.2016-03-25");
		FileUtil fileutil2 = new FileUtil();
		fileutil2.Initialize("E:/userloc2", "utf-8");
		for(String uid : user_locs.keySet()){
			//doprocess(loc,locips.get(loc),path);
			Set<String> locs = user_locs.get(uid);
			if(locs.size() !=1)
				continue;
			String r = "";
			for(String loc : locs){
				r = loc;
			}
			fileutil2.Append2File("E:/userloc2",uid+"\t"+r+"\n");
			
		}
		fileutil2.CloseRead();
		/*getUserid("E:/2016-03-21");
		getUserid("E:/2016-03-20");
		getUserid("E:/2016-03-19");
		getUserid("E:/2016-03-18");
		getUserid("E:/2016-03-17");
		String path = "E:/fulluserloc";
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		for(String user : user_locs.keySet()){
			Set<String> temp = user_locs.get(user);
			if(temp.size() == 1){
				String l = "";
				for(String s : temp){
					l = s;
				}
				fileutil.Append2File(path,user + "\t"+l +"\n");
			}
		}
		fileutil.CloseRead();*/
		//args = new String[]{"E:/2016-03-21.txt","",""};
		/*doprocess();
		getUserloc(args[0]);
		getLocips(args[1]);
		writeUserdoc2file(args[2],args[3]);*/
		/*CountDownLatch threadSignal = new CountDownLatch(5);
		try{
			
			dayThread st1 = new dayThread("2016-03-17",threadSignal);
			new Thread(st1).start();
			dayThread st2 = new dayThread("2016-03-18",threadSignal);
			new Thread(st2).start();
			dayThread st3 = new dayThread("2016-03-19",threadSignal);
			new Thread(st3).start();
			dayThread st4 = new dayThread("2016-03-20",threadSignal);
			new Thread(st4).start();
			dayThread st5 = new dayThread("2016-03-21",threadSignal);
			new Thread(st5).start();
		}catch(Exception e){
			System.out.println("thread start error");
			e.printStackTrace();
		}
		
		try {
			threadSignal.await();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ip locs size is "+ip_locs.size());
		writeUserdoc2file(args[0],args[1]);*/
		Map<String,String> user_loc = getUserloc(args[0]);
		getLocips("/data/clientLogs/2016-03-21",user_loc);
		getLocips("/data/clientLogs/2016-03-20",user_loc);
		getLocips("/data/clientLogs/2016-03-19",user_loc);
		getLocips("/data/clientLogs/2016-03-18",user_loc);
		getLocips("/data/clientLogs/2016-03-17",user_loc);
		writeUserdoc2file(args[1],args[2],args[3]);
		getuseriplocs(args[4]);
	}
	static class dayThread implements Runnable{
		private String day;
		private CountDownLatch threadSignal;
		public dayThread(String day,CountDownLatch threadSignal){
			this.day = day;
			this.threadSignal = threadSignal;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String userloc_path = "/data/kedm/"+day;
			String userip_path = "/data/clientLogs/"+day;
			Map<String,String> user_loc = getUserloc(userloc_path);
			getLocips(userip_path,user_loc);
			threadSignal.countDown();
			System.out.println(Thread.currentThread().getName() + " finish run...");
		}
		
	}
	public static Map<String,List<String>> getRes(String path){
		Map<String,List<String>> res = new HashMap<String,List<String>>();
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				String[] userloc = line.split("\t");
				if(userloc.length != 2)
					continue;
				String[] locs = userloc[1].split("\\|");
				res.put(userloc[0], Arrays.asList(locs));
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
		return res;
	}
	public static Map<String,String> getUserloc(String path){
		Map<String,String> user_loc = new HashMap<String,String>();
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				String[] userloc = line.split("\t");
				if(userloc.length != 2)
					continue;
				/*String[] locs = userloc[1].split("#");
				if(locs.length > 1)
					continue;*/
				user_loc.put(userloc[0], userloc[1]);
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
		return user_loc;
	}
	public static void getLocips(String path,Map<String,String> user_loc){
		try{
			File dir = new File(path);
			if(dir.isDirectory()){
				File[] files = dir.listFiles();
				for(File file : files){
					String line;
					FileInputStream fis = new FileInputStream(file);
					InputStreamReader read = new InputStreamReader(fis, "UTF-8");
					BufferedReader br = new BufferedReader(read);
					while ((line = br.readLine()) != null) {
						if(line.trim().equals("")||line.length()<10){
							continue;
						}
						String[] rawUserDataItems = line.split("\t");

						// 原始数据非法，字段数==13
						if (rawUserDataItems.length != 13) {
							continue;
						}

						// 获取各字段的值
						// userid
						String userID = rawUserDataItems[5].trim();
						if (userID.length() < 2) {
							continue;
						}
						/*if(rawUserDataItems[7].toLowerCase().contains("wifi"))
							continue;*/
						if(!user_loc.containsKey(userID)){
							continue;
						}
						String ip=rawUserDataItems[1].trim();
						if(ip == null)
							continue;
						
						String loc = user_loc.get(userID);
						/*if(!loc.replace("_", "").matches("[\u4e00-\u9fa5]{1,30}"))
							continue;*/
						
						if(ip_locs.containsKey(ip)){
							List<String> locs = ip_locs.get(ip);
							locs.add(loc);
							ip_locs.put(ip, locs);
						}else{
							List<String> locs = new ArrayList<String>();
							locs.add(loc);
							ip_locs.put(ip, locs);
						}
						if(user_ips.containsKey(userID)){
							Set<String> ips = user_ips.get(userID);
							ips.add(ip);
							user_ips.put(userID, ips);
						}else{
							Set<String> ips = new HashSet<String>();
							ips.add(ip);
							user_ips.put(userID, ips);
						}
					}
					br.close();
					read.close();
					fis.close();
					System.out.println(path + " finally  ips  size is "+file.getName());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public static void writeUserdoc2file(String path,String noip,String iplocsPath){
		Map<String,List<String>> locips = new HashMap<String,List<String>>();
		File f = new File(noip);
		if(f.exists()){
			f.delete();
		}
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(noip, "utf-8");
		
		FileUtil fileutilEx = new FileUtil();
		fileutilEx.Initialize(iplocsPath, "utf-8");
		try{
			System.out.println("ip_locs size is "+ip_locs.size());
			for(String key : ip_locs.keySet()){
				List<String> temp = ip_locs.get(key);
				Set<String> templocs = new HashSet<String>();
				for(String l : temp){
					templocs.add(l);
				}
				if(templocs.size() > 1){
					StringBuffer sb = new StringBuffer();
					for(String s : templocs){
						sb.append(s).append("\t");
					}
					fileutil.Append2File(noip, key+ "\t" + sb.toString() + "\n");
				}else if(temp.size() < 2){
					StringBuffer sb = new StringBuffer();
					for(String s : templocs){
						sb.append(s).append("\t");
					}
					fileutil.Append2File(noip, key+ "\t" + sb.toString() + "\n");
				}else{
					StringBuffer sb = new StringBuffer();
					for(String s : templocs){
						sb.append(s).append("\t");
					}
					fileutilEx.Append2File(iplocsPath, key+ "\t" + sb.toString() + "\n");
					String loc = null;
					for(String s : templocs){
						loc = s;
					}
					if(locips.containsKey(loc)){
						List<String> ips = locips.get(loc);
						if(!ips.contains(key)){
							ips.add(key);
						}
						locips.put(loc, ips);
					}else{
						List<String> ips = new ArrayList<String>();
						ips.add(key);
						locips.put(loc, ips);
					}
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
		fileutilEx.CloseRead();
		FileUtil fileutil2 = new FileUtil();
		fileutil2.Initialize(path, "utf-8");
		for(String loc : locips.keySet()){
			//doprocess(loc,locips.get(loc),path);
			List<String> ips = locips.get(loc);
			Collections.sort(ips);
			//String path = "E:/resip";
			StringBuffer sb = new StringBuffer();
			
			for(int i =0;i< ips.size();i++){
				if( i != ips.size()-1){
					if(longMatch(ips.get(i)) + 1 == longMatch(ips.get(i+1))){
						if(sb == null || sb.length() == 0){
							sb.append(loc).append(" ").append(ips.get(i)).append(" ").append(ips.get(i+1));
						}else{
							sb.append(" ").append(ips.get(i+1));
						}
						
					}else{
						if(sb != null && sb.length() > 0){
							//System.out.println(sb.toString());
							String[] temp = sb.toString().split(" ");
							fileutil2.Append2File(path,temp[0]+"\t"+temp[temp.length-1]+"\t"+loc+"\n");
							sb = new StringBuffer();
						}
					}
				}
				
			}
			if(sb != null && sb.length() > 0){
				String[] temp = sb.toString().split(" ");
				fileutil2.Append2File(path,temp[0]+"\t"+temp[temp.length-1]+"\t"+loc+"\n");
				sb = new StringBuffer();
			}
			
			System.out.println("has do work "+loc);
		}
		fileutil2.CloseRead();
		/*File fo = new File(path);
		if(fo.exists()){
			fo.delete();
		}
		FileUtil fileutil2 = new FileUtil();
		fileutil2.Initialize(path, "utf-8");
		System.out.println("finally  ips  size is "+locips.size());
		for(String loc : locips.keySet()){
			Set<String> temp = locips.get(loc);
			StringBuffer sb = new StringBuffer();
			int count = 0;
			for(String ip : temp){
				sb.append(ip);
				count++;
				if(count != temp.size()){
					sb.append("|");
				}
			}
			fileutil2.Append2File(path,loc+"\t"+sb.toString()+"\n");
			
		}
		fileutil2.CloseRead();*/
	}
	public static long longMatch(String ip){
		String[] ips = ip.split("\\.");
		long ip1 = Integer.parseInt(ips[0]);
		long ip2 = Integer.parseInt(ips[1]);
		long ip3 = Integer.parseInt(ips[2]);
		long ip4 = Integer.parseInt(ips[3]);
		long ip2long = 1L* ip1 * 256 * 256 * 256 + ip2 * 256 * 256 + ip3 * 256 + ip4;
		return ip2long;
	}
	//确定ip段
	public static void doprocess(String loc,List<String> ips,String path){
		Collections.sort(ips);
		//String path = "E:/resip";
		StringBuffer sb = new StringBuffer();
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		for(int i =0;i< ips.size();i++){
			if( i != ips.size()-1){
				if(longMatch(ips.get(i)) + 1 == longMatch(ips.get(i+1))){
					if(sb == null || sb.length() == 0){
						sb.append(loc).append(" ").append(ips.get(i)).append(" ").append(ips.get(i+1));
					}else{
						sb.append(" ").append(ips.get(i+1));
					}
					
				}else{
					if(sb != null && sb.length() > 0){
						//System.out.println(sb.toString());
						fileutil.Append2File(path,sb.toString()+"\n");
						sb = new StringBuffer();
					}
				}
			}
			
		}
		if(sb != null && sb.length() > 0){
			fileutil.Append2File(path,sb.toString()+"\n");
			sb = new StringBuffer();
		}
		fileutil.CloseRead();
		System.out.println("has do work "+loc);
	}
	
	public static void getUserid(String path){
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				/*String[] userloc = line.split("\t");
				if(userloc.length != 2)
					continue;
				String[] locs = userloc[1].split("#");
				if(locs.length > 1)
					continue;*/
				String uid = line.substring(line.indexOf("userid=")+7, line.indexOf(",ask_channel="));
				String city = line.substring(line.indexOf("city=")+5, line.indexOf(",province="));
				String provice = line.substring(line.indexOf("province=")+9);
				String loc = city + "_" + provice;
				if(city.trim().equals("") || provice.trim().equals(""))
					continue;
				if(user_locs.containsKey(uid)){
					user_locs.get(uid).add(loc);
				}else{
					Set<String> templocs = new HashSet<String>();
					templocs.add(loc);
					user_locs.put(uid, templocs);
				}	
				
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
		System.out.println("finsish file  sss");
	}
	
	public static void getuseriplocs(String path){
		File fo = new File(path);
		if(fo.exists()){
			fo.delete();
		}
		FileUtil fileutil2 = new FileUtil();
		fileutil2.Initialize(path, "utf-8");
		System.out.println("start write user ips..");
		for(String user : user_ips.keySet()){
			Set<String> temp = user_ips.get(user);
			StringBuffer sb = new StringBuffer();
			int count = 0;
			for(String ip : temp){
				sb.append(ip);
				count++;
				if(count != temp.size()){
					sb.append("|");
				}
			}
			fileutil2.Append2File(path,user+"\t"+sb.toString()+"\n");
			
		}
		fileutil2.CloseRead();
	}

}
