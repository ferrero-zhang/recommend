/**
 * 
 */
package com.ifeng.iRecommend.likun.ipush;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.ifeng.commen.Utils.JsonUtils;

/**
 * <PRE>
 * 作用 : 
 *   描述推送的item内容细节；
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2014年5月6日        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class pushItem {
	String imcp_id;
	String title;//对外展示的title
	String content;//对外展示的description
	
	/**对android用户进行推送
	 * 
	 * @param pushitem, 推送的文章
	 * @param userList, 接收推送的用户列表
	 * @return
	 */
	public static void pushItemToAndroid(pushItem pushitem, ArrayList<String> userList){
		if(pushitem==null ||userList == null || userList.size() == 0)
			return;
		//一次最多推送1w个用户，多于1w用户时，对用户分批进行推送
		if(userList.size()>100000){
			ArrayList<String> tempUserList = new ArrayList<String>();
			for(String user: userList){
				tempUserList.add(user);
				if(tempUserList.size()>=100000){
					pushItemToAndroid(pushitem, tempUserList);
					tempUserList = new ArrayList<String>();
				}
			}
			pushItemToAndroid(pushitem, tempUserList);
		}else{
			
			StringBuffer sb = new StringBuffer();
			for(String user: userList)
				sb.append(user).append(",");
			String tempStr = sb.toString();
			String userToPush = tempStr.substring(0, tempStr.length()-1);
			System.out.println("push to users: "+userToPush);
			
			final String appKey = "b2e21459-dd8f-4458-a11b-6c153e65f39b";
			final int type = 1;
			final String tokenKey = "ipush";
			final String masterSecret = "16f9cca7-053a-426f-abb5-1c778c40564b";
			String extra = "{\"id\":\""+pushitem.imcp_id+"\"}";//在extra中输入item id，须是json串
			long time = System.currentTimeMillis();
			HttpClient client = new HttpClient();
			PostMethod postMethod = new PostMethod(
					"http://223.203.209.236:8080/ipush/interf/send.jhtml");//线上正式android推送地址： http://10.32.25.210/ipush/interf/subscribe!android.jhtml
			NameValuePair[] params = {
					new NameValuePair("appKey", appKey),//测试app应用唯一编码
					new NameValuePair("type", String.valueOf(type)),//推送类型(1通知2消息)
	                new NameValuePair("targetOS","1"),//推送目标系统,[1]android[2]ios[3]两者都推,如果为空,则默认android应用
					new NameValuePair("content", (pushitem.content)),//推送内容
					new NameValuePair("time", time + ""),//当前时间戳，时间戳精确到毫秒
					new NameValuePair("sendTimeStr",""),//定时发送时间,如:2012-02-02 22:22:22, 立即发送为空
					new NameValuePair("deviceFilter", "{\"sn\":\""+userToPush+"\"}") ,//应用推送对象
					new NameValuePair("extra",extra),//附加字段,格式为json
					new NameValuePair("serverSideExpiredTime", "300"),//离线消息保留时长(单位：秒),0为不保留，默认为0
					new NameValuePair("styleId", "3"),//通知栏样式编号
					new NameValuePair("title",pushitem.title),//Android 通知栏标题
	                                new NameValuePair("creator","ifeng"),//创建者
					// token= appkey+time+tokenKey+masterSecret
					new NameValuePair("token", MD5.encode(appKey+ time
							+ tokenKey + masterSecret)) };
			 postMethod.setRequestBody(params);
			 postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");
			 String response = null;
			 try{
				 client.executeMethod(postMethod);
				 response = postMethod.getResponseBodyAsString();
			 } catch (IOException ie) {
				 ie.printStackTrace();
			 } catch (Exception e){
				 e.printStackTrace();
			 }

			 String result = JsonUtils.toJson(response);
			 System.out.println(response);
		}
		
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		//推送样例
		pushItem pushitem = new pushItem();
		pushitem.imcp_id = "83014018";
		pushitem.title = "女子欲跳河被救";
		pushitem.content = "暴雨中一女子欲轻生";
		ArrayList<String> userList = new ArrayList<String>();
		userList.add("356380052728876");
		userList.add("356380052728884");
		pushItemToAndroid(pushitem, userList);
		
	}
}
