package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.Scanner;

public class DataExpLibTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataExpLib dataExpLib = DataExpLib.getInstance();

		
		CommonDataSub cds = new CommonDataSub();
  		Thread t = new Thread(cds); 
  		t.start();
		
		while (true) {
			System.err.println("请输入:输入-1即可退出");
			Scanner input = new Scanner(System.in);
			String query = input.nextLine();
			if ("-1".equals(query)) {
				break;
			}
			System.err.println(query + ":"
					+ dataExpLib.getDataExpMaps().get(query));
		}
	}

}
