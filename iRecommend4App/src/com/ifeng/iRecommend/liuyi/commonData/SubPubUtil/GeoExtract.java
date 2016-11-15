package com.ifeng.iRecommend.liuyi.commonData.SubPubUtil;

import java.util.ArrayList;
import java.util.HashSet;

import com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract.EntityLibQuery;
import com.ifeng.iRecommend.wuyg.commonData.entity.EntityInfo;
import com.ifeng.iRecommend.wuyg.commonData.entity.entityBase.KnowledgeBaseBuild;

public class GeoExtract {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		   
		    HashSet<String> set = new HashSet<String>();
		
            EntityLibQuery.init();
            ArrayList<String> wordList = new ArrayList<String>();
            for(String word : wordList){
            	ArrayList<EntityInfo> entityInfos = EntityLibQuery.getEntityList(word);
            	
            	for(EntityInfo en : entityInfos){
            		if(en.getFilename().equals("地区") && en.getLevels().get(en.getLevels().size() -1).equals("地点术语")){
            			set.add(en.getWord());
            		}
            	}
            }
            
            
	}

}
