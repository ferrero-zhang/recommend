package com.ifeng.iRecommend.featureEngineering.RuleFunction;

import java.util.ArrayList;
import java.util.Set;

import com.ql.util.express.Operator;

public class featureListContainsSet extends Operator{
	public featureListContainsSet(String name){
		this.name=name;
	}
	public Object executeInner(Object[] list) throws Exception{
		if (list[0]==null || list[0].toString().isEmpty()) {
			return false;
		}
		ArrayList<String> featureList = (ArrayList<String>)list[0];
		for (int i = 0; i < featureList.size(); i+=3) {
			for (String s:(Set<String>)list[1]) {
				if (featureList.get(i).equals(s) && 
						Math.abs(Double.parseDouble(featureList.get(i+2)))>= 0.5) {
					return true;
				}
			}
		}
		
		return false;
		
	}
}
