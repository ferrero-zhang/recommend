package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.wuyg.commonData.entity.entityBase.KnowledgeBaseBuild;

public class AliasLibDataTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
           
		AliasLibData aliasLibData = AliasLibData.getInstance();
		
//		ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>> AliasLibDataMap = aliasLibData.getAliasLibDataMap();
//
//		Iterator<String> iterator = AliasLibDataMap.keySet().iterator();
//		
//		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File("e:data/aliasEntity.txt"), false), "utf-8");
//		
//		while(iterator.hasNext()){
//			String key = iterator.next();
//			osw.append(key+"\t"+AliasLibDataMap.get(key)+"\n");
//		}
//		
//		osw.flush();
//		osw.close();
		
		CommonDataSub cds = new CommonDataSub();
		Thread t = new Thread(cds);
		t.start();
		
		while(true){
			System.err.println("请输入:输入-1即可退出");
			Scanner input = new Scanner(System.in);
			String query = input.nextLine();
			if("-1".equals(query)){
				break;
			}
			System.err.println(query+":"+aliasLibData.searchAlias(query));
		}
	}

}
