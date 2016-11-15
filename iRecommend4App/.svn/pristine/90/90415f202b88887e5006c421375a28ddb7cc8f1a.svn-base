package com.ifeng.iRecommend.lidm.userLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

public class PostSlideToSolr {
	private static final Log LOG = LogFactory.getLog(PostSlideToSolr.class);
	static String slideurl = "http://10.32.23.188:8081/solr46reco/slide/update";
	
	public static void postToSolr(List<UserSlide> uslides){
		if(uslides == null || uslides.isEmpty())
			return;
		try{
			Gson gson = new Gson();
			String data = gson.toJson(uslides);
			post(data);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void post(String xmlFile){
		xmlFile = Util.replaceInvaldateCharacter(xmlFile);
		
		HttpURLConnection urlc = null;
		try {
			URL url = new URL(slideurl);
			urlc = (HttpURLConnection) url.openConnection();
			try {
				urlc.setRequestMethod("POST");
			} catch (ProtocolException e) {
				System.out.println("Shouldn't happen: HttpURLConnection doesn't support POST??" + e);
			}
			urlc.setDoOutput(true);
			urlc.setDoInput(true);
			urlc.setUseCaches(false);
			urlc.setAllowUserInteraction(false);
			urlc.setRequestProperty("Content-type", "text/json");

			urlc.setChunkedStreamingMode(1024000);
			OutputStream out = urlc.getOutputStream();
			OutputStreamWriter outputStrm = new OutputStreamWriter(out, "UTF-8");
			outputStrm.write(xmlFile);
			outputStrm.flush();
			outputStrm.close();

			InputStream in = null;
			try {
				if (HttpURLConnection.HTTP_OK != urlc.getResponseCode()) {
					if (urlc.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST
							|| urlc.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
						System.out.println("post failed:");
					}
					LOG.error("Solr returned an error :"
							+ " ErrorCode=" + urlc.getResponseCode());
				} 

				in = urlc.getInputStream();
				//pipe(in, System.out);
			} catch (IOException e) {
				LOG.error("IOException while reading response:"
						+ " Exception=" + e);
				e.printStackTrace();
			} catch (Exception e) {
				LOG.error("Exception while reading response: word="
						+ " Exception=" + e);
				e.printStackTrace();
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException x) {
				}
			}
		} catch (Exception e) {
			LOG.error("Post Data unknown Exception:"+ e);
		} finally {
			if (urlc != null) {
				urlc.disconnect();
			}
		}
	}

}
