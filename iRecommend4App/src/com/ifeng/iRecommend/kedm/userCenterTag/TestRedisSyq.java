package com.ifeng.iRecommend.kedm.userCenterTag;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import com.google.gson.Gson;
import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.iRecommend.kedm.util.LocalInfo;
import com.ifeng.iRecommend.likun.userCenter.tnappuc.utils.UserCenterRedisUtil;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData;
import com.ifeng.iRecommend.liuyi.commonData.SubPubUtil.HotWordData.HotWordInfo;

public class TestRedisSyq {
	private static final Log LOG = LogFactory.getLog("TestRedisSyq");
	public static List<String> uids = new ArrayList<String>();
	static class doGet implements Runnable{
		private List<String> us ;
		public doGet(List<String> us){
			this.us = us;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Random random = new Random();
			IcebergClient client = IcebergClient.getInstance();
			String res = "";
			//HttpAsyncClient httpAsyncClient=new DefaultHttpAsyncClient(); 
			//httpclient.start();
			//HttpPost request = null;
			//用户中心新机房集群，暂时两个集群都写，完全迁移数据后，暂停旧的
			ShardedJedisPool sjp_uc = UserCenterRedisUtil.getJedisPoolMaster();
			ShardedJedis sj_uc = sjp_uc.getResource();
			
			Collection<Jedis> js_uc =  sj_uc.getAllShards();
	    	Iterator<Jedis> it_uc = js_uc.iterator(); 
	    	while(it_uc.hasNext()){  
	    		Jedis j=it_uc.next();  
	    	}
	    	int rnum = 0;
	    	int fnum = 0;
	    	int nonum = 0;
	    	LOG.info(Thread.currentThread().getName()+" user size is "+us.size());
	    	for(String u : us){
	    		
				try{
					Map<String,String> umt = sj_uc.hgetAll(u);
					if(umt != null && !umt.isEmpty()){
						rnum++;
						System.out.println(u + "has umt from redis");
						continue;
					}
					long s = System.currentTimeMillis();
					String po = client.getPosition(u);
					//HttpGet request = new HttpGet(po);
					//httpclient.execute(request, null);
					//Future<HttpResponse> future = httpclient.execute(request, null);
					//res = doGet(po,3000,5000);
					res = doGetHttpClient(po);
					if(res.equals("404")){
						nonum++;
					}else if(res.equals("200")){
						fnum++;
					}
					LOG.info(Thread.currentThread().getName() + " do http spend "+(System.currentTimeMillis()-s));
					LOG.info(Thread.currentThread().getName()+ " "+ po + " res "+res);
				}catch(Exception e){
					LOG.error("do http error "+res,e);
				}finally{
					
				}
	    	}
	    	LOG.info(Thread.currentThread().getName()+" num "+rnum + " "+ fnum + " " + nonum);
		}
		
	}
	public static void getUserids(String path){
		FileUtil fileutil = new FileUtil();
		fileutil.Initialize(path, "utf-8");
		try{
			String line = null;
			while((line = fileutil.ReadLine()) != null){
				if(line.trim().equals(""))
					continue;
				if(line.trim().contains("/"))
					continue;
				uids.add(line);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		fileutil.CloseRead();
	}
	public static String doSimpleHttpGet(String urlStr) throws Exception {
		//HttpAsyncClient httpclient = HttpAsyncClients.createDefault();
		URL url=new URL(urlStr);
    	HttpURLConnection connection = (HttpURLConnection)url.openConnection(); 
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8")); 	
		String line=in.readLine();
		while(line!=null && !line.contains("result")){
			line=in.readLine();
		}
		in.close();	
		connection.disconnect();
		return line;
    }
	public static String doGet(String url,int connectionTimeout,int readTimeOut) throws Exception
	{
		DataInputStream inputStream=null;
		HttpURLConnection con =null;
		try 
		{	
			URL dataUrl = new URL(url);
			con = (HttpURLConnection) dataUrl.openConnection();
			con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeOut);
			con.setDoOutput(true);
			con.setDoInput(true);
			//long len=Long.valueOf(con.getHeaderField("Content-length"));
			inputStream=new DataInputStream(con.getInputStream());
			byte[] byteBuffer=new byte[256];
			int b=inputStream.read();
			int index=0;
			int round=2;
			byteBuffer[index]=(byte)b;
			index++;
			while(b!=-1){
				if(index%256==0){
					byte[] newb=new byte[round*256];
					System.arraycopy(byteBuffer, 0, newb, 0, byteBuffer.length);
					byteBuffer=newb;
					round++;
				}
				byteBuffer[index]=(byte)b;
				index++;
				b=inputStream.read();
			}
			byte[] result=new byte[index];
			System.arraycopy(byteBuffer, 0, result, 0, index);
			String s= new String(result,"UTF-8");
			return s;
		}catch (Exception e){
			LOG.error("get error"+con.getResponseCode(),e);
			throw e;
		}finally{
			try{
			if(inputStream!=null)
				inputStream.close();
			}catch (Exception e){
				//throw new UgcAuditorException(e.getMessage(),e);
				e.printStackTrace();
			}
			con.disconnect();
		}
	}
	public static String doGetHttpClient(String url) throws IOException{
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);
		client.executeMethod(method);
		System.out.println(method.getResponseBodyAsString());
		System.out.println(URLDecoder.decode(method.getResponseBodyAsString(),"utf-8"));
		method.releaseConnection();
		StringBuffer buffer = new StringBuffer();
//      results=get.getResponseBodyAsString();
		BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(),"UTF-8"));
		String line;
		while((line=in.readLine())!=null){
			buffer.append(line);
		}
		return buffer.toString();
	}
	public static void main(String[] args){
		/*HotWordData hotWordData = HotWordData.getInstance();
		Map<String,HotWordInfo> test = hotWordData.getHotwordMap();
		for(String key : test.keySet()){
			if(test.get(key).getDocumentId() != null){
				System.out.println(key + " " + test.get(key).getDocumentId()+test.get(key).isRead());
			}
			
		}*/
		//args = new String[]{"E:/dayliyUser_a","1"};
		/*String test = "%7B%22State%22%3A%22%E5%8C%97%E4%BA%AC%E5%B8%82%22%2C%22City%22%3A%22%E5%8C%97%E4%BA%AC%E5%B8%82%22%7D";
		try {
			Gson gson = new Gson();
			System.out.println(test.contains("%"));
			test = URLDecoder.decode(test, "utf-8");
			LocalInfo locinfo = gson.fromJson(test, LocalInfo.class);
			System.out.println(locinfo);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(test);*/
		ShardedJedisPool sjp_uc = UserCenterRedisUtil.getJedisPoolMaster();
		ShardedJedis sj_uc = sjp_uc.getResource();
		
		Collection<Jedis> js_uc =  sj_uc.getAllShards();
    	Iterator<Jedis> it_uc = js_uc.iterator(); 
    	while(it_uc.hasNext()){  
    		Jedis j=it_uc.next();  
    	}
    	Map<String,String> umt = sj_uc.hgetAll("bcf75ac1e77aa5940f10b93df0427655a904f615");
		IcebergClient client = IcebergClient.getInstance();
		String po;
		try {
			po = client.getPosition("bcf75ac1e77aa5940f10b93df0427655a904f615");
			String res = doGetHttpClient(po);
			System.out.println(res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		getUserids(args[0]);
		for(int i =0;i< Integer.parseInt(args[1]);i++){
			List<String> us = uids.subList(100000*i, 100000*(i+1));
			new Thread(new doGet(us)).start();
		}
	}

}
