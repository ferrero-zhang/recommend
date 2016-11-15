package com.ifeng.iRecommend.featureEngineering.RuleFunction;

import java.util.ArrayList;
import java.util.Set;

import com.ql.util.express.Operator;

public class featureListContain extends Operator {
	public featureListContain(String name){
		this.name=name;
	}
	public Object executeInner(Object[] list) throws Exception{
		if (list[0]==null || list[0].toString().isEmpty()) {
			return false;
		}
		ArrayList<String> featureList = (ArrayList<String>)list[0];
		String words=list[1].toString();
		double weight=Double.parseDouble(list[2].toString());
		String[] word=words.split("\\|");
		for (int i = 0; i < featureList.size(); i+=3) {
			for (int j = 0; j < word.length; j++) {
				if (featureList.get(i).toLowerCase().contains("fun来了") && 
						featureList.get(i).equalsIgnoreCase(word[j]) && Math.abs(Double.parseDouble(featureList.get(i+2)))>=weight) {
					return true;
				}
				if (featureList.get(i).equals(word[j]) && featureList.get(i+1).equals("c") 
						&& Math.abs(Double.parseDouble(featureList.get(i+2)))>=weight) {
					return true;
				}
			}
		}
		return false;
		
	}
}
