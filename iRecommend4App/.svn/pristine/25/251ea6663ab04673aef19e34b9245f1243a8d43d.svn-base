package com.ifeng.iRecommend.zxc.bdhotword.manual;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ifeng.iRecommend.zxc.util.HttpUtils;

public class CmppArticleSelector {
	public static String getEventTotalCount(String event){
		List<String> ids=new ArrayList<String>();
		try {
				String eventName=event;
				String url="http://fashion.cmpp.ifeng.com/Cmpp/runtime/xlist!data.jhtml";
				String cookie="cmpp_user=zhouxc3; cmpp_token=439d306ad71f773b4183892f21850dfc; cmpp_cn=%E5%91%A8%E5%B0%8F%E8%8D%89; vjuids=-573be78d3.151af1fdd38.0.6929a064; userid=1450342080366_n9rkxs1436; BDTUJIAID=a304e27d56626a216f506e3f30abef93; __gads=ID=c22cfb2ee6e2e3f2:T=1454317935:S=ALNI_MYJ4AtBU7ncSbknN5FYS_WE0xV2BQ; vjlast=1450342080.1455672578.11";
				Map<String,String> postParam=new HashMap<String,String>();
				postParam.put("Cookie", cookie);
				postParam.put("Referer", "http://fashion.cmpp.ifeng.com/Cmpp/runtime/xlist!render.jhtml?nodeId=16001&formId=600&listId=471");
				postParam.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");
				String post="start=0&limit=40&from=service&fd=id&q="+URLEncoder.encode(eventName,"utf-8")+":title&sorts=[{\"field\":\"id\",\"order\":\"desc\"}]&formId=600&listId=471";
				try {
					String str=HttpUtils.doPostDefault(url, post, 10000, 10000, postParam);
					return str;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return "";
	}
	public static void main(String[] args){
		 getEventTotalCount("首套房首付25%");
	}
}
