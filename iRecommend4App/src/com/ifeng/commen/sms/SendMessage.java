package com.ifeng.commen.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * 
 * <PRE>
 * 作用 : 
 *   
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :首先需要绑定IP，才能成功调用网关的实时接口(可以先测试一下，如果成功发送可以忽略)
 * 	 
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015-8-8         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class SendMessage {
	static Logger LOG = Logger.getLogger(SendMessage.class);
	private static String url = "http://211.151.175.61:8080/QXTGateway/send.do";
	/**
	 * 
	 * @param mobile  必须   手机号码 11位数字
	 * @param content 必须   短信内容
	 * @param gatewayid  指定网关
	 * @param longcode   指定业务长号码
	 * @param senderid   指定发送者id
	 * @return
	 * 返回内容说明：
	 * IP is not legal!  //IP地址未绑定
	 * Mobile is not legal!  //黑名单或者号码格式错误
	 * Content is empty!  //内容为空
	 * Content can not be Chinese！ //文字转换错误 
	 * Contain discordant keyword：XXX  //含有敏感词
	 * Send success!  //发送成功
	 * Send error!  //发送失败
	 */
	public static String send(String mobile, String content, String gatewayid, String longcode, String senderid)
	{
		if(!isPhoneNum(mobile))
			return "Mobile is not legal!";
		if(content == null || content.isEmpty())
			return "Content is empty!";
		String param = "mobile="+mobile+"&content="+content;
		if(gatewayid != null && !gatewayid.isEmpty())
			param += "&gatewayid="+gatewayid;
		if(longcode != null && !longcode.isEmpty())
			param += "&longcode="+longcode;
		if(senderid != null && !senderid.isEmpty())
			param += "&longcode="+senderid;
		
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
            out.print(param);
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
	/**
	 * 粗略判断是否为手机号码
	 * @param str
	 * @return
	 */
	private static boolean isPhoneNum(String str) {
		if (str == null) {
			return false;
		}
		int sz = str.length();
		if(sz!=11)
			return false;
		for (int i = 0; i < sz; i++) {
			if (Character.isDigit(str.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}                         
	
	public static void main(String[] args){
		String res = SendMessage.send("17865153777", "Test of send message.", null, null, null);
	}
}
