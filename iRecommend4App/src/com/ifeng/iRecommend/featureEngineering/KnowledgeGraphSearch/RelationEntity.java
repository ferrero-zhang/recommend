package com.ifeng.iRecommend.featureEngineering.KnowledgeGraphSearch;

import java.util.HashSet;

public class RelationEntity{
	int count;
	HashSet<String> personSet = new HashSet<String>();
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public HashSet<String> getPersonSet() {
		return personSet;
	}
	public void setPersonSet(HashSet<String> personSet) {
		this.personSet = personSet;
	}
}
