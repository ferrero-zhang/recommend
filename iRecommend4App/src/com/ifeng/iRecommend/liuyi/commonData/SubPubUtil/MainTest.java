package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.ArrayList;

import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.EntityLibQuery;

public class MainTest {
	
	public static void main(String[] args) {
//		EntityLibQuery.init();
		CommonDataSub cds = new CommonDataSub();
		Thread t = new Thread(cds); 
		t.start();
//		
		while (true) {
//			ArrayList<EntInfo> ei = EntityLibQuery.getEntityList("李三");
//			ArrayList<EntInfo> ei2 = EntityLibQuery.getEntityList("李四");
//			if (null != ei) {
//				System.out.println(ei.toString());
//			} else {
//				System.out.println("Has no this word： 李三");
//			}
//			
//			if (null != ei2) {
//				System.out.println(ei2.toString());
//			} else {
//				System.out.println("Has no this word：李四");
//			}
//			
//			try {
//				Thread.sleep(3000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			System.out.println("江泽民" + BlackListData.getInstance().isBlacklistElem("江泽民"));
			System.out.println("方法" + BlackListData.getInstance().isBlacklistElem("方法"));
			System.out.println("美国" + BlackListData.getInstance().isBlacklistElem("俄罗斯"));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("=================");
		}
		
	}
}
