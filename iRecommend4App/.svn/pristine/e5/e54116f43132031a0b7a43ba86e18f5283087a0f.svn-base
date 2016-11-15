package com.ifeng.iRecommend.featureEngineering.thriftService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.ifeng.hexl.thrift.RequestException;

public class DocToSlideInterface {
	private static final Log LOG = LogFactory.getLog("DocToSlideInterface");
	private TTransport transport;
	private TProtocol protocol;
	com.ifeng.hexl.thrift.HelloWordService.Client client;

	public DocToSlideInterface() {
		transport = new TSocket("10.32.23.98", 7983);
		protocol = new TBinaryProtocol(transport);
		client = new com.ifeng.hexl.thrift.HelloWordService.Client(protocol);
		try {
			transport.open();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 建立连接
	}

	public boolean ifDocToSlde(String requestMan, String content) {
		LOG.info("[INFO] Begin quest doc to slide interface.");
		// 第二种请求类型
		com.ifeng.hexl.thrift.Request request = new com.ifeng.hexl.thrift.Request().setType(com.ifeng.hexl.thrift.RequestType.QUERY_SLIDE).setName(requestMan).setContent(content);
		boolean result = false;
		try {
			result = Boolean.valueOf(client.doAction(request));
		} catch (TTransportException e) {
			e.printStackTrace();
			try {
				transport.open();
			} catch (TTransportException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			LOG.error("[ERROR] In get c list result. Connect reset.");
		}catch (RequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.info("[INFO] Doc to slide result is "+ result);
		return result;
	}

	public void closeTransport() {
		transport.close();
	}

	public static String readToString(String fileName) {
		String encoding = "gbk";
		File file = new File(fileName);
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return new String(filecontent, encoding);
		} catch (UnsupportedEncodingException e) {
			System.err.println("The OS does not support " + encoding);
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		String content = readToString("test/false.txt");
		// System.out.println(content);
		TTransport transport = new TSocket("10.32.23.98", 7983);
		TProtocol protocol = new TBinaryProtocol(transport);

		// 创建client
		com.ifeng.hexl.thrift.HelloWordService.Client client = new com.ifeng.hexl.thrift.HelloWordService.Client(protocol);

		transport.open(); // 建立连接

		// 第一种请求类型
		com.ifeng.hexl.thrift.Request request = new com.ifeng.hexl.thrift.Request().setType(com.ifeng.hexl.thrift.RequestType.HELLO_TEST).setName("hexl").setContent(content);
		System.out.println(client.doAction(request));

		// 第二种请求类型
		request.setType(com.ifeng.hexl.thrift.RequestType.QUERY_SLIDE).setName("hexl").setContent(content);
		System.out.println(client.doAction(request));

		transport.close(); // 请求结束，断开连接
	}
}
