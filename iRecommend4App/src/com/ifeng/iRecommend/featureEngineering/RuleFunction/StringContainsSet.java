package com.ifeng.iRecommend.featureEngineering.RuleFunction;

import java.util.Set;

import com.ql.util.express.Operator;

public class StringContainsSet extends Operator {
	public StringContainsSet(String name){
		this.name=name;
	}
	public Object executeInner(Object[] list) throws Exception{
		if (list[0]==null || list[0].toString().isEmpty()) {
			return false;
		}
		for (String s:(Set<String>)list[1]) {
			if (list[0].toString().contains(s)) {
				return true;
			}
		}
		return false;
		
	}
}
