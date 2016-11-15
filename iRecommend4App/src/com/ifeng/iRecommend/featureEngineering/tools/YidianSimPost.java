package com.ifeng.iRecommend.featureEngineering.tools;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ifeng.iRecommend.featureEngineering.CMPPDataCollect;
import com.ifeng.iRecommend.featureEngineering.TextProTools;
import com.ifeng.iRecommend.featureEngineering.dataStructure.JsonFromCMPP;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;

public class YidianSimPost {
	private static final Log LOG = LogFactory.getLog("YidianSimPost");
	final private static String url = "http://ups.yidianzixun.com/featurestore/ifengtags";
	HashSet<String> originalId = new HashSet<String>();
//	HashSet<String> algorithmId = new HashSet<String>();
	IKVOperationv2 ikvop = new IKVOperationv2("appitemdb");
	static class data{
		@Expose
		@SerializedName("url")
		String url;
		@Expose
		@SerializedName("title")
		String title;
		@Expose
		@SerializedName("content")
		String content;
	}
	public static String scnt2cnt(String s_content)
	{
		if(s_content == null || s_content.isEmpty())
			return null;
		String content = s_content.replaceAll(" *_[a-zA-Z]+ *", "");
		content = content.replaceAll("http://.*?.(jpg|jpeg|gif|bmp|png|undefined)", "");
		return content;
	}
	public String getJson(String id){
		itemf item = ikvop.queryItemF(id, "c");
		if(item == null)
			return null;
		data d = new data();
		d.url = "";
		d.title = item.getTitle();
		d.content = scnt2cnt(item.getSplitContent());
		d.content = TextProTools.filterHtmlPure(d.content);
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String jResult = gson.toJson(d,data.class);
		LOG.info("[INFO] request json content is "+jResult);
		return jResult;
	}
	/**
	 * 获取最初的抓取数据
	 * 
	 * @param sTime
	 * @param terminalTime
	 * @return
	 */
	public void readOriginalData(long sTime, long terminalTime) {
		long eTime = sTime + CMPPDataCollect.INTERVAL2;
		// int jsonCount = 0;
		LOG.info("[INFO]Begin to read original data.");
		while (sTime != terminalTime) {
//			System.out.println(sTime);
			long cTime = System.currentTimeMillis() / 1000;
			String startDate = CMPPDataCollect.dateReverse(sTime);
			String endDate = CMPPDataCollect.dateReverse(sTime + CMPPDataCollect.INTERVAL2);
			if (cTime > (eTime + CMPPDataCollect.INTERVAL2)) {
				String url1 = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_187.jhtml?startTime=" + startDate + "&endTime=" + endDate;
				String url2 = "http://fashion.cmpp.ifeng.com/Cmpp/runtime/interface_346.jhtml?startTime=" + startDate + "&endTime=" + endDate;
				List<JsonFromCMPP> jList1 = null;
				List<JsonFromCMPP> jList2 = null;
				try {
					jList1 = CMPPDataCollect.readJsonList(url1);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("[ERROR]GetJsonList error.", e);
					
				}
				try {
					jList2 = CMPPDataCollect.readJsonList(url2);
				} catch (Exception e) {
					LOG.error("[ERROR]GetJsonList error.", e);
				}
				for (JsonFromCMPP j : jList1) {
					originalId.add(j.getId());
				}
				for (JsonFromCMPP j : jList2) {
					originalId.add(j.getId());
				}
				sTime = eTime;
				eTime = sTime + CMPPDataCollect.INTERVAL2;
			} 
			else{
				break;
			}
		}
		LOG.info("originalId size is " + originalId.size());
	}
	
	public String send(String json)
	{
		if(json == null)
			return null;
		PrintWriter out = null;
        BufferedReader in = null;
        StringBuffer sbRes = new StringBuffer();
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            // 发送请求参数
            out.print(json);
            // flush输出流的缓冲
            out.flush();
            if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
				if (conn.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
						|| conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					throw new Exception("post failed ErrorCode="+conn.getResponseCode());
				}
				LOG.error("ErrorCode="+conn.getResponseCode());
			} 		
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
            	sbRes.append(line).append("\r\n");
            }
        } catch (Exception e) {
            LOG.error("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
            sbRes.append("Send error!");
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return sbRes.toString();
	}
	public void process(String startDate, String endDate)
	{
		long sTime = 0;
		try {
			sTime = CalTools.getTimeStamp(startDate+" 23:00:00");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		long terminalTime = 0;
		try {
			terminalTime = CalTools.getTimeStamp(endDate+" 23:00:00");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		readOriginalData(sTime, terminalTime);
		FileWriter fwRight = null;
		try {
			fwRight = new FileWriter("data/"+endDate+"_right", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int nullCount = 0;
		int successCount = 0;
		int failedCount = 0;
		int errorCount = 0;
		for(String id : originalId)
		{
			LOG.info("[INFO] The "+id +" begin processing.");
			String result  = send(getJson(id));
			LOG.info("[INFO] Id "+id +" result is "+ result);
			if(result == null)
			{
				nullCount++;
			}
			else if(result.contains("tags"))
			{
				successCount++;
				try {
					fwRight.write(result+"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (result.contains("reason"))
			{
				failedCount++;
			}
			else errorCount++;
		}
		try {
			fwRight.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fwRight.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.info("[INFO] Today check id num is "+originalId.size());
		LOG.info("[INFO] Null item count is "+ nullCount);
		LOG.info("[INFO] Success get result count is "+ successCount);
		LOG.info("[INFO] Could not find result count is "+ failedCount);
		LOG.info("[INFO] Error count is "+ errorCount);
		ikvop.close();
	}
	public static void main(String[] args){
		YidianSimPost o = new YidianSimPost();
		String startDate = CalTools.getDate(System.currentTimeMillis() - 48 * 60 * 60 * 1000);
		String endDate = CalTools.getDate(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
		o.process(startDate, endDate);
		try {
			Thread.sleep(60 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
}
