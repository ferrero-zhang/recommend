package com.ifeng.iRecommend.xuzc.userCenter.locAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.iRecommend.xuzc.userCenter.util.UserModelUtil;

public class BayesPredictLocByIpMain {
	public static Map<String,String> user_loc = new HashMap<String,String>();//已有GPS信息的用户——地域
	public static List<String> locs = new ArrayList<String>();//去掉一些人数较少的地域，可用的地域表
	public static List<String> ig_ips = new ArrayList<String>();//去掉一些对应地域过多的ip，非法ip列表
	public static Map<String,List<String>> user_ips = new HashMap<String,List<String>>();//样本用户——ips列表
	public static Map<String,Map<String,Integer>> ip_locc = new HashMap<String,Map<String,Integer>>();//ip对应的多个地域已经在这个地域出现的用户次数
	public static Map<String,String> ip_locs = new HashMap<String,String>();//ip对应的地域列表
	public static Map<String,List<String>> loc_users = new HashMap<String,List<String>>();//地域对应的样本用户列表
	public static Map<String,Map<String,Integer>> loc_ip_num = new HashMap<String,Map<String,Integer>>();
	public static int preNum = 100;
	public static int same = 0;
	public static int same2 = 0;
	public static int same3 = 0;
	public static int num = 0;
	public static int model1 = 0;
	public static int model2 = 0;
	
	public static Jedis jedis;
	public static Pipeline redispip;
	static{
		jedis=new Jedis("10.90.1.58", 6379, 10000);
		jedis.select(2);
		redispip = jedis.pipelined();
	}
	/**
	 * @description 根据user——loc列表，反向索引得出loc——users的列表
	 * @param 
	 * @return void
	 */
	public static void calClassifyP(){
		for(String u : user_loc.keySet()){
			String f_s = user_loc.get(u);
			/*String f_s = user_loc.get(u);
			String f = f_s.split("_")[0];
			if(loc_usernum.containsKey(f_s)){
				int tempN = loc_usernum.get(f_s);
				tempN++;
				loc_usernum.put(f_s, tempN);
			}else{
				loc_usernum.put(f_s, 1);
			}
			if(locF_usernum.containsKey(f)){
				int tempN = locF_usernum.get(f);
				tempN++;
				locF_usernum.put(f, tempN);
			}else{
				locF_usernum.put(f, 1);
			}
			count++;
			System.out.println("has init one user "+count);*/
			if(loc_users.containsKey(f_s)){
				List<String> users = loc_users.get(f_s);
				users.add(u);
				loc_users.put(f_s, users);
			}else{
				List<String> users = new ArrayList<String>();
				users.add(u);
				loc_users.put(f_s, users);
			}
		}
		/*Map<String,Double> p_loc = new HashMap<String,Double>();
		Map<String,Double> p_locF = new HashMap<String,Double>();
		for(String locf : locF_usernum.keySet()){
			double p = locF_usernum.get(locf)/(double)user_loc.size();
			p_locF.put(locf, p);
			System.out.println(locf + " "+ locF_usernum.get(locf) +" "+ user_loc.size()+""+ p);
		}
		for(String loc : loc_usernum.keySet()){
			double p = loc_usernum.get(loc)/(double)user_loc.size();
			p_loc.put(loc, p);
			//System.out.println(loc + "用户属于该城市的概率" + p);
		}*/
	}
	public static void calIPClass(String path){
		
		/*FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");*/
		for(String c : loc_users.keySet()){
			
			List<String> users = loc_users.get(c);
			if(users.size() < 100){
				continue;
			}
			System.out.println(c+ " loc class user size is "+users.size());
			locs.add(c);
			Map<String,Integer> ip_unum_c = new HashMap<String,Integer>();
			for(String u : users){
				List<String> ips = user_ips.get(u);
				if(ips == null || ips.isEmpty()){
					System.out.println(u+" ips is null ");
					continue;
				}
				for(String ip : ips){
					if(ip_unum_c.containsKey(ip)){
						int num = ip_unum_c.get(ip);
						num++;
						ip_unum_c.put(ip, num);
					}else{
						ip_unum_c.put(ip, 1);
					}
				}
			}
			users = null;
			loc_ip_num.put(c, ip_unum_c);
			/*StringBuffer sb = new StringBuffer();
			for(String ip : ip_unum_c.keySet()){
				sb.append(ip).append("\t").append(c).append("t").append(ip_unum_c.get(ip)).append("\n");
				
			}
			fileutil.Append2File(path, sb.toString());*/
		}
	}
	public static void predicet(String user,List<String> ips){
		String res = "";
		double max = 0;
		int c_usernum = 0;//类别中的训练用户数量
		int ip_cnum = 0;//类别中包含IP的训练用户数
		int sum =0;//训练集合中总ip数量
		double c_p = 0;//类别的先验概率
		try{
			for(String c : locs){
				double p = 1;
				c_usernum = loc_users.get(c).size();
				c_p = c_usernum/(double)user_ips.size();
				Map<String,Integer> ip_num = loc_ip_num.get(c);
				for(String ip:ips){
					if(!ip_locs.containsKey(ip)){
						continue;
					}
					String locs = ip_locs.get(ip);
					if(locs.contains(c)){
						
					}
				}
				for(String ip : ips){
					if(ip_num != null && ip_num.containsKey(ip)){
						ip_cnum = ip_num.get(ip);
					}
					double tempP = (ip_cnum+1)/(double)(c_usernum+sum);
					p = p*tempP;
				}
				double mp = p*c_p;
				if(mp > max){
					max = mp;
					res = c;
				}
			}
			System.out.println(user +" "+res+" "+user_loc.get(user));
			if(res.equals(user_loc.get(user))){
				same++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	/**
	 * @param list
	 * @return 返回预测出的地域信息
	 */
	private static String predict(String u, List<String> ips) {
		Map<String,Integer> loc_usernum = new HashMap<String,Integer>();
		Map<String,Integer> loc_ipnum = new HashMap<String,Integer>();
		System.out.println(u+"ip size "+ips.size());
		String max = "";
		
		for(String ip : ips){
			StringBuffer sb = new StringBuffer();
			Map<String,Integer> locc = ip_locc.get(ip);
			if(locc == null || locc.isEmpty())
				continue;
			sb.append(ip).append(" ");
			for(String loc : locc.keySet()){
				if(loc_ipnum.containsKey(loc)){
					int t = loc_ipnum.get(loc);
					t = t+1;
					loc_ipnum.put(loc, t);
				}else{
					loc_ipnum.put(loc, 1);
				}
				int c = locc.get(loc);
				if(loc_usernum.containsKey(loc)){
					int temp = loc_usernum.get(loc);
					temp =temp + c;
					loc_usernum.put(loc, temp);
				}else{
					loc_usernum.put(loc, c);
				}
				sb.append(loc).append("_").append(c).append(" ");
			}
			System.out.println(sb.toString());
		}
		ArrayList<Entry<String, Integer>> maplist = new ArrayList<Entry<String, Integer>>(loc_usernum.entrySet());
		Collections.sort(maplist, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> arg0,
					Entry<String, Integer> arg1) {
				return (int) (arg1.getValue() - arg0.getValue());
			}

		});
		loc_usernum = new LinkedHashMap<String, Integer>();  
        for (int i = 0; i < maplist.size(); i++) {  
        	loc_usernum.put(maplist.get(i).getKey(), maplist.get(i).getValue());  
        }
		int p = 0;
		for(String l : loc_usernum.keySet()){
			if(loc_usernum.get(l) > p){
				p = loc_usernum.get(l);
				max = l;
			}
			System.out.println(u + " "+l+"_"+loc_usernum.get(l));
		}
		for(String l : loc_ipnum.keySet()){
			System.out.println(u + " "+l+"_"+loc_ipnum.get(l));
		}

		String userModel = UserModelUtil.getT3FromRedis(u);
		if(userModel!=null){
			System.out.println(userModel);
			if(max.contains("_")){
				String city = max.substring(max.indexOf("_")+1);
				if(userModel.contains(city)){
					return max;
				}
			}
		}
		return null;
	}
	public static void predict2(String u,List<String> ips){
		num++;
		Map<String,Integer> loc_usernum = new HashMap<String,Integer>();
		Map<String,Integer> loc_ipnum = new HashMap<String,Integer>();
		System.out.println(u+"ip size "+ips.size());
		String max = "";
		
		for(String ip : ips){
			StringBuffer sb = new StringBuffer();
			Map<String,Integer> locc = ip_locc.get(ip);
			if(locc == null || locc.isEmpty())
				continue;
			sb.append(ip).append(" ");
			for(String loc : locc.keySet()){
				/*if(locc.size() == 1 && locc.get(loc) > 2){
					max = loc;
				}*/
				if(loc_ipnum.containsKey(loc)){
					int t = loc_ipnum.get(loc);
					t = t+1;
					loc_ipnum.put(loc, t);
				}else{
					loc_ipnum.put(loc, 1);
				}
				int c = locc.get(loc);
				if(loc_usernum.containsKey(loc)){
					int temp = loc_usernum.get(loc);
					temp =temp + c;
					loc_usernum.put(loc, temp);
				}else{
					loc_usernum.put(loc, c);
				}
				sb.append(loc).append("_").append(c).append(" ");
			}
			System.out.println(sb.toString());
		}
		ArrayList<Entry<String, Integer>> maplist = new ArrayList<Entry<String, Integer>>(loc_usernum.entrySet());
		Collections.sort(maplist, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> arg0,
					Entry<String, Integer> arg1) {
				return (int) (arg1.getValue() - arg0.getValue());
			}

		});
		loc_usernum = new LinkedHashMap<String, Integer>();  
        for (int i = 0; i < maplist.size(); i++) {  
        	loc_usernum.put(maplist.get(i).getKey(), maplist.get(i).getValue());  
        }
		int p = 0;
		for(String l : loc_usernum.keySet()){
			if(loc_usernum.get(l) > p){
				p = loc_usernum.get(l);
				max = l;
			}
			System.out.println(u + " "+l+"_"+loc_usernum.get(l)+ " "+user_loc.get(u));
		}
		for(String l : loc_ipnum.keySet()){
			System.out.println(u + " "+l+"_"+loc_ipnum.get(l)+ " "+user_loc.get(u));
		}
		if(max.equals(user_loc.get(u))){
			same++;
		}
		
		
		int cc = 0;
		for(String k : loc_usernum.keySet()){
			if(cc < 2){
				if(k.equals(user_loc.get(u))){
					same2++;
					break;
				}
			}
			cc++;
		}
		cc = 0;
		for(String k : loc_usernum.keySet()){
			if(cc < 3){
				if(k.equals(user_loc.get(u))){
					same3++;
					break;
				}
			}
			cc++;
		}
		String userModel = UserModelUtil.getT3FromRedis(u);
		if(userModel!=null){
			System.out.println(userModel);
			if(max.contains("_")){
				String city = max.substring(max.indexOf("_")+1);
				if(userModel.contains(city)){
					model1++;
					if(max.equals(user_loc.get(u))){
						model2++;
					}
				}
			}
		}
	}
	/**
	 * @description 读取用户ips列表
	 * @param @param path
	 * @return void
	 */
	public static void getUserips(String path){
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				String[] userips = line.split("\t");
				if(userips.length != 2)
					continue;
				String[] tempips = userips[1].split(" ");
				List<String> ips = Arrays.asList(tempips);
				/*String[] locs = userloc[1].split("#");
				if(locs.length > 1)
					continue;*/
				user_ips.put(userips[0], ips);
				//System.out.println(line);
			}
			System.out.println("finish load user ips ..."+user_ips.size());
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
	}
	/**
	 * @description TODO读取用户地域列表
	 * @param @param path
	 * @return void
	 */
	public static void getUserloc(String path){
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
			System.out.println("finish load user loc");
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
	}
	/**
	 * @description TODO计算 ip_loc_usersize的关系，同时记录非法ip，写入文件
	 * @param @param path
	 * @return void
	 */
	public static void getIpF(String path){
		for(String loc : loc_users.keySet()){
			List<String> users = loc_users.get(loc);
			if(users.size() < 100)
				continue;
			for(String u : users){
				List<String> ips = user_ips.get(u);
				if(ips == null || ips.isEmpty())
					continue;
				for(String ip : ips){
					if(ip_locc.containsKey(ip)){
						Map<String,Integer> temp = ip_locc.get(ip);
						if(temp.containsKey(loc)){
							int c = temp.get(loc);
							c = c+1;
							temp.put(loc, c);
						}else{
							temp.put(loc, 1);
						}
						ip_locc.put(ip, temp);
					}else{
						Map<String,Integer> temp = new HashMap<String,Integer>();
						temp.put(loc, 1);
						ip_locc.put(ip, temp);
					}
				}
			}
		}
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		for(String ip : ip_locc.keySet()){
			StringBuffer sb = new StringBuffer();
			sb.append(ip);
			Map<String,Integer> temp = ip_locc.get(ip);
			if(temp.size() > 5){
				ig_ips.add(ip);
				continue;
			}
			for(String l : temp.keySet()){
				sb.append("\t").append(l).append("_").append(temp.get(l));
			}
			sb.append("\n");
			fileutil.Append2File(path, sb.toString());
		}
		fileutil.CloseRead();
	}
	public static void readLocc(String path){
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				String[] locc = line.split("\t");
				String ip = locc[0];
				for(String s : locc){
					String[] loc_c = s.split("_");
					if(loc_c.length !=3){
						continue;
					}
					if(ip_locc.containsKey(ip)){
						Map<String,Integer> temp = ip_locc.get(ip);
						temp.put(loc_c[0]+"_"+loc_c[1], Integer.parseInt(loc_c[2]));
						ip_locc.put(ip, temp);
					}else{
						Map<String,Integer> temp = new HashMap<String,Integer>();
						temp.put(loc_c[0]+"_"+loc_c[1], Integer.parseInt(loc_c[2]));
						ip_locc.put(ip, temp);
					}					
				}
				
			}
			System.out.println("finish load locc");
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
	}
	public static void main(String[] args){
		UserModelUtil.redisInit();
		//加载贝叶斯训练样本后的概率文件  格式：  ip_loc_num
		readLocc(args[0]);
		//加载需要预测的用户ip集合   格式：uid ip【···】
		//getUserips(args[1]);
		predictByUserips(args[1]);
		/*int count =0;		
		for(String uid : user_ips.keySet()){
			count++;
			String loc = predict(uid, user_ips.get(uid));
			if(loc != null){
				num++;				
				redispip.set(uid, loc);
				if(num % 10000 == 0){
					redispip.sync();
				}
			}
			redispip.sync();
		}	
		System.out.println("总数："+ count);
		System.out.println("召回数："+ num);	*/
		UserModelUtil.redisDestory();
	}
	/**
	 * @param string
	 */
	private static void predictByUserips(String path) {
		int count =0;
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				count++;
				if(line.trim().equals(""))
					continue;
				String[] userips = line.split("\t");
				if(userips.length != 2)
					continue;
				String[] tempips = userips[1].split(" ");
				List<String> ips = Arrays.asList(tempips);
				String loc = predict(userips[0], ips);
				if(loc != null){
					num++;				
					redispip.set(userips[0], loc);
					System.out.println(userips[0]+":"+loc);
					if(num % 10000 == 0){
						redispip.sync();
					}
				}
			}
			redispip.sync();
			System.out.println("总数："+ count);
			System.out.println("召回数："+ num);
			System.out.println("finish predict By Userips ..."+user_ips.size());
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
		
	}
	

}
