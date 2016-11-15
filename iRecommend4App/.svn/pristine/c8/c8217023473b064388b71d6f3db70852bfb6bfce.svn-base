package com.ifeng.commen.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ifeng.commen.Utils.commenFuncs;

/**
 * <PRE>
 * 作用 : 
 * 	通过手机发报警短信
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
 *          1.0          2013-10-28        chendw          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class SmsSend {

	public static void sendMsc(String mobile, String msc) {
		try {
			String smsContent = commenFuncs.convertToHexString(msc);
			String user = commenFuncs.convertToHexString("ifengZJYF");
			String password = commenFuncs.getMD5Code("Ifengzjyf").toUpperCase();
			String url = "http://210.51.19.70/interface/service.asmx/fxSubmitSms";
			String prdId = "999";
			String submitMode = "0";
			String BgnDatetime = "";
			String parameters = "User=" + user + "&Password=" + password
					+ "&PrdID=" + prdId + "&SubmitMode=" + submitMode
					+ "&BgnDatetime=" + BgnDatetime + "&SmsContent="
					+ smsContent + "&Mobiles=" + mobile;
			BaseHttpResponser bhr = send(url, "POST", parameters);
			System.out.println(bhr.getContent());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static BaseHttpResponser send(String urlString, String method,
			String data) throws IOException {
		HttpURLConnection urlConnection = null;
		InputStream input = null;
		BaseHttpResponser httpResponser = new BaseHttpResponser();
		try {

			URL url = new URL(urlString);

			// init connection
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod(method);
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setDefaultUseCaches(false);
			urlConnection.setRequestProperty("Charset", "UTF-8");

			urlConnection.setRequestProperty("Connection", "Keep-Alive");

			// writing data into outputstream
			if ("POST".equals(method)) {
				urlConnection.getOutputStream().write(data.getBytes("utf-8"));
				urlConnection.getOutputStream().flush();
				urlConnection.getOutputStream().close();
			}

			// getting response info
			input = urlConnection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(input));
			StringBuffer temp = new StringBuffer();
			String line = bufferedReader.readLine();
			while (line != null) {
				temp.append(line).append("\n");
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			String ecod = urlConnection.getContentEncoding();
			if (ecod == null) {
				ecod = "utf-8";
			}
			httpResponser.setContent(new String(temp.toString().getBytes(),
					"UTF-8"));
		} catch (IOException ex) {
			Logger.getLogger(BaseHttpRequester.class.getName()).log(
					Level.SEVERE, null, ex);
		} finally {
			if (input != null) {
				input.close();
			}
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return httpResponser;
	}
	
	public static void main(String[] args){
		sendMsc("18810828709", "test message");
	}
}
