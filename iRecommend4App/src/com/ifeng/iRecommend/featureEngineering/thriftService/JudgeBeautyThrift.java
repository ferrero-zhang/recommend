package com.ifeng.iRecommend.featureEngineering.thriftService;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import test_beauty_package.JudgeBeautyService;

public class JudgeBeautyThrift extends ThriftClient{
	private static final Log LOG = LogFactory.getLog("JudgeBeautyThrift");
	private JudgeBeautyService.Client client;
	public JudgeBeautyThrift(){
		setIp("10.32.23.98");
		setPort(8080);
		initial();
		connect();
		client = new JudgeBeautyService.Client(getProtocol());
	}
	
	
	
	
	 public boolean ifBeauty(String id, String title, String docType, String requestMan){
		 LOG.info("[INFO] id is "+id +" title is "+title+" doctType is "+docType);
		   String requestResult = null;
		   try {
			   requestResult = client.beautyPredict(id, title, docType, requestMan);
			   LOG.info("[INFO] Beauty result is "+ requestResult);
		} catch (TTransportException e) {
			connect();
			e.printStackTrace();
			LOG.error("[ERROR] In get beauty judge result. Connect reset.");
		}catch(TException e){
			e.printStackTrace();
			LOG.error("[ERROR] In get beauty judge result. ");
		}
		   if(requestResult == null)
			   return false;
		   else if(requestResult.equals("beauty"))
			   return true;
		   else if(requestResult.equals("nonbeauty"))
			   return false;
		   else
			   return false;
	   }
	 public static void main(String[] args) throws IOException, TException{
		 JudgeBeautyThrift ob = new JudgeBeautyThrift();
		 System.out.println(ob.client.beautyPredict("123","123test","docpic","hexl"));
		 ob.closeTransport();
		 try{
		 System.out.println(ob.client.beautyPredict("123","123test","docpic","hexl"));
		 }catch(TTransportException e){
			 ob.connect();
		 }
		 System.out.println(ob.client.beautyPredict("123","123test","docpic","hexl"));
		 ob.closeTransport();
	 }
}
