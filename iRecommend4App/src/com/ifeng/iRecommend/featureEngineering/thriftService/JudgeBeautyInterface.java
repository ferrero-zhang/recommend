package com.ifeng.iRecommend.featureEngineering.thriftService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import test_beauty_package.JudgeBeautyService;

public class JudgeBeautyInterface {
	private static final Log LOG = LogFactory.getLog("JudgeBeautyThrift");
	private TTransport transport;
	private TProtocol protocol;
	JudgeBeautyService.Client client;
    public JudgeBeautyInterface(){
    	transport = new TSocket("10.32.23.98", 9090);
		protocol = new TBinaryProtocol(transport);
		client = new JudgeBeautyService.Client(protocol);
		try {
			transport.open();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 建立连接
    }
    
	public void closeTransport() {
		transport.close();
	}
    
   public boolean ifBeauty(String id, String title, String docType, String requestMan){
	   String requestResult = null;
	   try {
		   requestResult = client.beautyPredict(id, title, docType, requestMan);
	} catch (TTransportException e) {
		e.printStackTrace();
		try {
			transport.open();
		} catch (TTransportException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		LOG.error("[ERROR] In get c list result. Connect reset.");
	}catch (TException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
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
   
}
