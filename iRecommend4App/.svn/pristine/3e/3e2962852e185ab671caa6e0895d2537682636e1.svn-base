package com.ifeng.iRecommend.featureEngineering.thriftService;

import java.util.ArrayList;
import java.util.List;

import locClassify.LOC_CLASSIFY;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


public class LocIdentify {
	private static final Log LOG = LogFactory.getLog("LocIdentify");
	private TTransport transport;
	private TProtocol protocol;
	LOC_CLASSIFY.Client client;

	public LocIdentify() {
		transport = new TSocket("10.90.7.53", 9090);
		protocol = new TCompactProtocol(transport);
		client = new LOC_CLASSIFY.Client(protocol);
		try {
			transport.open();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 建立连接
	}
	public List<String> getLocList(String s_title, String s_content, String source, ArrayList<String> tag_list){
		long time = System.currentTimeMillis();
		List<String> list = new ArrayList<String>();
    	try {
    		list = client.loc_recognize(s_title,s_content,source,tag_list);
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
    	long caltime = System.currentTimeMillis() - time;
    	LOG.info("[INFO] loc recognize used time "+ caltime);
    	return list;
	}
	
	public void closeTransport() {
		transport.close();
	}
}
