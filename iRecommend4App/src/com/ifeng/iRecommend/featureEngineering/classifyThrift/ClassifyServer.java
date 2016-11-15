package com.ifeng.iRecommend.featureEngineering.classifyThrift;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;

public class ClassifyServer {
	static Logger LOG = Logger.getLogger(ClassifyServer.class);
	final int port = 7983;
	TProtocolFactory tProtocolFactory = new TBinaryProtocol.Factory();
	TTransportFactory tTransportFactory = new TTransportFactory();
	TServerSocket serverTransport = null;

	ClassifyServer() {
		try {
			serverTransport = new TServerSocket(new TServerSocket.ServerSocketTransportArgs().port(port));
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ClassifyService.Processor processor = new ClassifyService.Processor(new ServerProcess());
		Args tServerArgs = new Args(serverTransport);
		tServerArgs.processor(processor);
		tServerArgs.protocolFactory(tProtocolFactory);
		tServerArgs.transportFactory(tTransportFactory);
		TServer server = new TThreadPoolServer(tServerArgs);
		LOG.info("Running server...");
		server.serve();
	}

	public static void main(String[] args) throws Exception {
		// Protocol factory
		ClassifyServer classifyServer = new ClassifyServer();
	}
}
