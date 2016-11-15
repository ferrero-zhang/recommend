package com.ifeng.iRecommend.featureEngineering.DetectExtrmSim;

import com.ifeng.ikvlite.IkvLiteClient;

public class IKVTEST {

	public static void main(String[] args) {
		String STORE_NAME = "tuiJianSimcla";
		String host = "10.32.25.21";
		String keyspace = "ikv";
		IkvLiteClient client;
		client = new IkvLiteClient(host, keyspace, STORE_NAME);
		client.connect();
		client.put("xxx", "testtest");
		String value = client.get("xxx");
		System.out.println(value);
	}

}
