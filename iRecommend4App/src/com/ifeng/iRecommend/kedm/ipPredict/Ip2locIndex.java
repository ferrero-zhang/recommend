package com.ifeng.iRecommend.kedm.ipPredict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.FileUtil;

public class Ip2locIndex {
	private static final Log log = LogFactory.getLog(Ip2locIndex.class);
	private static Map<String,Map<String,List<String>>> index_ip_reg = new ConcurrentHashMap<String,Map<String,List<String>>>();
	private static List<String> index_ip_reg_other = new ArrayList<String>();
	private static Set<String> ipLocs = new HashSet<String>();//起始ip和地域信息的字符串，eg ：startip#endip#loc
	private static Map<String,String> code_loc = new HashMap<String,String>();//地域编码——》两级地域的map映射
	
	public static void init(String codeLocPath,String iplocsPath){
		try{
			//获取code——loc的映射关系表
			initCodeLoc(codeLocPath);
			//初始化ip段到loc的映射表
			getIpsLoc(iplocsPath);
			//建立ip库的预测模型的两层索引map
			createIndex();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public static void initCodeLoc(String path){
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				String[] temp = line.split("\t");
				if(temp.length != 3)
					continue;
				code_loc.put(temp[2], temp[0]+"_"+temp[1]);
			}
		}catch(Exception e){
			log.error("get code loc from file error ",e);
			e.printStackTrace();
		}
		fileutil.CloseRead();
	}
	
	public static void getIpsLoc(String path){
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				ipLocs.add(line);
				
			}
		}catch(Exception e){
			e.printStackTrace();
			log.error("init iplocs error",e);
		}
		fileutil.CloseRead();
	}
	
	public static void createIndex(){
		for(String iplocs : ipLocs){
			try{
				String[] temp = iplocs.split("\t");
				if(temp.length != 3)
					continue;
				String sip = temp[0];
				String eip = temp[1];
				String loc = code_loc.get(temp[2]);
				String[] startIp = sip.split("\\.");
				String[] endIp = eip.split("\\.");
				String reg = longMatch(startIp) + "#" + longMatch(endIp) + "#" + loc;
				if(startIp[0].equals(endIp[0])){
					if(startIp[1].equals(endIp[1])){
						if(index_ip_reg.containsKey(startIp[0])){
							Map<String,List<String>> tempsec = index_ip_reg.get(startIp[0]);
							if(tempsec.containsKey(startIp[1])){
								List<String> templist = tempsec.get(startIp[1]);
								templist.add(reg);
								tempsec.put(startIp[1], templist);
								index_ip_reg.put(startIp[0], tempsec);
							}else{
								List<String> templist = new ArrayList<String>();
								templist.add(reg);
								tempsec.put(startIp[1], templist);
								index_ip_reg.put(startIp[0], tempsec);
							}
						}else{
							Map<String,List<String>> tempsec = new HashMap<String,List<String>>();
							List<String> templist = new ArrayList<String>();
							
							templist.add(reg);
							tempsec.put(startIp[1], templist);
							index_ip_reg.put(startIp[0], tempsec);
						}
					}else{
						int s = Integer.parseInt(startIp[1]);
						int e = Integer.parseInt(endIp[1]);
						for(int i = s ; i<= e;i++){
							try{
								if(index_ip_reg.containsKey(startIp[0])){
									Map<String,List<String>> tempsec = index_ip_reg.get(startIp[0]);
									if(tempsec.containsKey(i+"")){
										List<String> templist = tempsec.get(i+"");
										templist.add(reg);
										tempsec.put(i+"", templist);
										index_ip_reg.put(startIp[0], tempsec);
									}else{
										List<String> templist = new ArrayList<String>();
										templist.add(reg);
										tempsec.put(i+"", templist);
										index_ip_reg.put(startIp[0], tempsec);
									}
								}else{
									Map<String,List<String>> tempsec = new HashMap<String,List<String>>();
									List<String> templist = new ArrayList<String>();
									
									templist.add(reg);
									tempsec.put(i+"", templist);
									index_ip_reg.put(startIp[0], tempsec);
								}
							}catch(Exception e1){
								log.error("error ",e1);
							}
							
						}
					}
				}else{
					
					if(!index_ip_reg_other.contains(reg)){
						index_ip_reg_other.add(reg);
					}
					
				}
			}catch(Exception e){
				log.error("create index error "+iplocs,e);
			}
			
		}
		log.info("finished create iplocs index ...");
		
	}
	public static long longMatch(String[] ips){
		long ip1 = Integer.parseInt(ips[0]);
		long ip2 = Integer.parseInt(ips[1]);
		long ip3 = Integer.parseInt(ips[2]);
		long ip4 = Integer.parseInt(ips[3]);
		long ip2long = 1L* ip1 * 256 * 256 * 256 + ip2 * 256 * 256 + ip3 * 256 + ip4;
		return ip2long;
	}
	
	public static String checkLoc(String ip){
		if(ip == null)
			return null;
		String res = null;
		String[] ips = ip.split("\\.");
		if(ips.length != 4){
			log.info("error ip ..."+ip);
			return null;
		}
		Map<String,List<String>> sec = index_ip_reg.get(ips[0]);
		long ip2long = longMatch(ips);
		if(sec != null){
			List<String> regs = sec.get(ips[1]);
			
			if(regs != null){
				for(String reg : regs){
					String[] temp =reg.split("#");
					if(ip2long <= Long.parseLong(temp[1]) && Long.parseLong(temp[0]) <= ip2long){
						res = temp[2];
						break;
					}
				}
			}
		}
		if(res == null){
			for(String reg : index_ip_reg_other){
				String[] temp =reg.split("#");
				if(ip2long <= Long.parseLong(temp[1]) && Long.parseLong(temp[0]) <= ip2long){
					res = temp[2];
					break;
				}
			}
		}
		return res;
		
	}
	
	public static void main(String[] args){
		initCodeLoc("E:/codeloc");
		getIpsLoc("E:/iplocs");
		long s = System.currentTimeMillis();
		createIndex();
		String[] ss = "67.231.224.0".split("\\.");
		String[] es = "68.66.47.255".split("\\.");
		System.out.println(longMatch(ss));
		System.out.println(longMatch(es));
		System.out.println(index_ip_reg.size());
		System.out.println(checkLoc("68.54.112.224"));
		System.out.println(longMatch("68.54.112.224".split("\\.")));
	}

}
