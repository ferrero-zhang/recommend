package com.ifeng.iRecommend.featureEngineering.KnowledgeGraphSearch;

import java.util.Comparator;
import java.util.HashMap;


public class MapCompare<T,E> implements Comparator<T>{
	public HashMap<T,E> hashMap = new HashMap<T, E>();
	
	public MapCompare(HashMap<T,E> hashMap){
		this.hashMap =hashMap;
	}

	@Override
	public int compare(T o1, T o2) {
		// TODO Auto-generated method stub
		E v1 = hashMap.get(o1);
		E v2 = hashMap.get(o2);
		
		if(v1 instanceof Integer){
			Integer i_v1 = (java.lang.Integer) v1;
			Integer i_v2 = (java.lang.Integer) v2;
			return i_v2.compareTo(i_v1);
		}
		
		if(v1 instanceof Double){
			Double i_v1 = (java.lang.Double) v1;
			Double i_v2 = (java.lang.Double) v2;
		   return i_v2.compareTo(i_v1);
		}
		
		if(v1 instanceof Float){
			Float i_v1 = (java.lang.Float) v1;
			Float i_v2 = (java.lang.Float) v2;
			return i_v2.compareTo(i_v1);
		}
		
		return 0;
	}

}

