package com.ifeng.commen.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseHttpRequester {
	private String _encoding = Charset.defaultCharset().name();

	/**
	 * 发送Get请求
	 * 
	 * @param urlString
	 * @return
	 */
	public BaseHttpResponser sendGet(String urlString) throws IOException {
		return send(urlString, "GET", null);
	}

	/**
	 * 发送Post请求
	 * 
	 * @param urlString
	 * @param data
	 * @return
	 */
	public synchronized BaseHttpResponser sendPost(String urlString, String data)
			throws IOException {
		return send(urlString, "POST", data);
	}

	/**
	 * 发送请求
	 * 
	 * @param urlString
	 * @param method
	 * @param data
	 * @return
	 */
	public BaseHttpResponser send(String urlString, String method, String data)
			throws IOException {
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
				urlConnection.getOutputStream().write(
						data.getBytes(this._encoding));
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
				ecod = this._encoding;
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

	/**
	 * 获取请求编码
	 * 
	 * @return
	 */
	public String GetEncoding() {
		return this._encoding;
	}

	/**
	 * 设置请求编码
	 * 
	 * @param encoding
	 */
	public void setEncoding(String encoding) {
		this._encoding = encoding;
	}
}
