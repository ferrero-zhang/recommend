package com.ifeng.iRecommend.featureEngineering.thriftService;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.ifeng.iRecommend.featureEngineering.FeatureExTools;
import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.myClassifier.GetSimcFeature;

import ClassifySer.ClassifyService;
import ClassifySer.RequestException;

public class SimcInterface {
	private static final Log LOG = LogFactory.getLog("SimcInterface");
	private TTransport transport;
	private TProtocol protocol;
	ClassifyService.Client client;

	public SimcInterface() {
		transport = new TSocket("10.32.23.98", 9083);
		protocol = new TCompactProtocol(transport);
		client = new ClassifyService.Client(protocol);
		try {
			transport.open();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 建立连接
	}
	
	public itemf simCal(String logstr, itemf item) {
		if(item == null)
			return item;
		long simcTime = System.currentTimeMillis();
		if (!FeatureExTools.ifContainC1(item.getFeatures()) && !item.getOther().contains("source=chaoslocal")) {
			List<String> cList = null;
			/******************* simc分类模块 ***********************/
			boolean needSimc = false;
			if (!item.getDocType().equals("slide") && (item.getTags() == null || item.getTags().size() < 3) && !item.getDocType().equals("video"))
				needSimc = true;
			else if (item.getDocType().equals("slide") || item.getDocType().equals("video") || item.getFeatures().contains("天气"))
				needSimc = false;
			else
				needSimc = !FeatureExTools.ifContainC1(item.getTags());
			if (needSimc)
			{
				LOG.info("[INFO]Begin to cal simc result.");
				try {
					cList = client.classify(item.getID(), "hexl", item.getSplitTitle(), item.getSource(), item.getFeatures());
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
					if(e instanceof TApplicationException && ((TApplicationException)e).getType() == TApplicationException.MISSING_RESULT)
						cList = null;
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (cList == null)
				cList = new ArrayList<String>();
			LOG.info("[INFO] " + logstr + " simc result is " + cList);
			
			LOG.info("[INFO] " + logstr + " feature doesn't contain c1, copy simc result to feature. " + cList);
			for (int k = 0; k < cList.size() - 2; k += 3) {
				item.addFeatures(cList.get(k));
				item.addFeatures(cList.get(k + 1));
				item.addFeatures(cList.get(k + 2));
			}
		}
		LOG.info(logstr + " [TIME] simc used time " + (System.currentTimeMillis() - simcTime));
		return item;
	}
	public void closeTransport() {
		transport.close();
	}
}
