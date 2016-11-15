package com.ifeng.iRecommend.featureEngineering.thriftService;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ThriftClient {
	private String IP;
	private int PORT;
	private TTransport transport;
	private TProtocol protocol;
	public String getIp(){
		return this.IP;
	}
	public void setIp(String ip){
		this.IP = ip;
	}
	public TProtocol getProtocol(){
		return this.protocol;
	}
	
	
	public int getPort(){
		return this.PORT;
	}
	public void setPort(int port){
		this.PORT = port;
	}
	public void initial(){
		transport = new TSocket(IP, PORT);
		protocol = new TBinaryProtocol(transport);
	}
	public void connect(){
		try {
			transport.open();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void closeTransport() {
		transport.close();
	}
}
