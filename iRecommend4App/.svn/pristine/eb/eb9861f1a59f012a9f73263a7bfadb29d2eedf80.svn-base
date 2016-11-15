package com.ifeng.iRecommend.featureEngineering.thriftService;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.ifeng.iRecommend.featureEngineering.classifyThrift.ClassifyService;
import com.ifeng.iRecommend.featureEngineering.classifyThrift.Request;
import com.ifeng.iRecommend.featureEngineering.classifyThrift.RequestException;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;

import test_beauty_package.JudgeBeautyService;

public class ClassifyInterface {
	private static final Log LOG = LogFactory.getLog("ClassifyInterface");
	private TTransport transport;
	private TProtocol protocol;
	ClassifyService.Client client;

	public ClassifyInterface() {
		transport = new TSocket("10.90.7.52", 7983);
		protocol = new TBinaryProtocol(transport);
		client = new ClassifyService.Client(protocol);
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

	/**
	 * 
	 * @param name
	 *            required
	 * @param id
	 * @param s_title
	 *            required
	 * @param s_content
	 * @param other
	 * @param source
	 * @param doctype
	 * @return
	 */
	public List<String> getClassifyResult(String name, String id, String s_title, String s_content, String other, String source, String doctype) {
		if (name == null)
			return null;
		if (s_title == null)
			s_title = "";
		Request request = new Request();
		request.setName(name).setS_title(s_title);
		if (id != null)
			request.setId(id);
		if (s_content != null)
			request.setS_content(s_content);
		if (other != null)
			request.setOther(other);
		if (source != null)
			request.setSource(source);
		if (doctype != null)
			request.setDoctype(doctype);
		List<String> resultList = new ArrayList<String>();
		try {
			resultList = client.doAction(request);
		} catch (TTransportException e) {
			e.printStackTrace();
			try {
				transport.open();
			} catch (TTransportException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			LOG.error("[ERROR] In get c list result. Connect reset.");
		} catch (RequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultList;
	}

	public static void main(String[] args) {
		ClassifyInterface a = new ClassifyInterface();
		IKVOperationv2 ob = new IKVOperationv2("appitemdb");
		int count = 0;
		long counttime = 0;
		for(int i = 150000; i < 201000; i++){
			itemf item = ob.queryItemF(String.valueOf(i), "c");
			if(count == 500)
				break;
			if (item != null) {
				count++;
				long time = System.currentTimeMillis();
				System.out.println(a.getClassifyResult("hexl", item.getID(), item.getSplitTitle(), item.getSplitContent(), item.getOther(), item.getSource(), item.getDocType()));
				counttime = counttime+System.currentTimeMillis() - time;
				System.out.println("Used time is "+ (System.currentTimeMillis() - time));
			}
		}
		System.out.println("Used time is "+ counttime);
	}
}
