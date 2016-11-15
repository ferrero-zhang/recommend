package com.ifeng.iRecommend.featureEngineering.KnowledgeGraphSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PersonInfo {
	/**
	 * 当前词语
	 */
	String word;
    /**
     * 与word存在相关关系和同层次兄弟节点的所有顶点
     */
    ArrayList<String> relationNodeList = new ArrayList<String>();
	/**
	 * 与word存在相关关系的节点集合，暂且不使用
	 */
	HashMap<String,HashSet<String>> relatedNodeMap = new HashMap<String,HashSet<String>>();
	/**
	 * 按照人物关系的重要度排序后的相关节点List
	 */
	ArrayList<String> relatedNodeList = new ArrayList<String>();
	/**
	 * 与word是兄弟的同层次节点，暂且不使用
	 */
	HashMap<String,HashSet<String>> brotherNodeMap = new HashMap<String, HashSet<String>>();
	/**
	 * 与word是相同主领域的节点List，该List中的结果是从同一层次中筛选出来的，参照主分布领域进行的，以及level的相近性
	 */
	ArrayList<String> brotherNodeList = new ArrayList<String>();
	/**
	 * 与word形成父子关系的节点List,主要就是分类体系的节点和非分类体系的中间节点
	 */
	ArrayList<String> childrenNodeList = new ArrayList<String>();
	
	/**
	 * 这些孩子节点是分类体系的
	 */
	ArrayList<String> childrenNodeClassifyList = new ArrayList<String>();
	/**
	 * 这些孩子节点不是分类体系的
	 */
	ArrayList<String> childrenNodeNoclassifyList = new ArrayList<String>();
	/**
	 * 留作他用的字段
	 */
	String other;
	/**
	 * 默认不是分类体系节点
	 */
	boolean isClassifyNodeFlag = false;
	
	public PersonInfo(){
		
	}
	
	public PersonInfo(String word, ArrayList<String> relationNodeList){
		setWord(word);
		
	}
	
	public PersonInfo(String word,HashMap<String,HashSet<String>> relatedNodeMap,HashMap<String,HashSet<String>> brotherNodeMap){
		setWord(word);
		setRelatedNodeMap(relatedNodeMap);
	    setBrotherNodeMap(brotherNodeMap);
	}

	public boolean isClassifyNodeFlag() {
		return isClassifyNodeFlag;
	}

	public void setClassifyNodeFlag(boolean isClassifyNodeFlag) {
		this.isClassifyNodeFlag = isClassifyNodeFlag;
	}

	public ArrayList<String> getChildrenNodeClassifyList() {
		return childrenNodeClassifyList;
	}

	public void setChildrenNodeClassifyList(
			ArrayList<String> childrenNodeClassifyList) {
		this.childrenNodeClassifyList = childrenNodeClassifyList;
	}

	public ArrayList<String> getChildrenNodeNoclassifyList() {
		return childrenNodeNoclassifyList;
	}

	public void setChildrenNodeNoclassifyList(
			ArrayList<String> childrenNodeNoclassifyList) {
		this.childrenNodeNoclassifyList = childrenNodeNoclassifyList;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public HashMap<String, HashSet<String>> getRelatedNodeMap() {
		return relatedNodeMap;
	}

	public void setRelatedNodeMap(HashMap<String, HashSet<String>> relatedNodeMap) {
		this.relatedNodeMap = relatedNodeMap;
	}

	public HashMap<String, HashSet<String>> getBrotherNodeMap() {
		return brotherNodeMap;
	}

	public void setBrotherNodeMap(HashMap<String, HashSet<String>> brotherNodeMap) {
		this.brotherNodeMap = brotherNodeMap;
	}

	public ArrayList<String> getRelationNodeList() {
		return relationNodeList;
	}

	public void setRelationNodeList(ArrayList<String> relationNodeList) {
		this.relationNodeList = relationNodeList;
	}

	public ArrayList<String> getRelatedNodeList() {
		return relatedNodeList;
	}

	public void setRelatedNodeList(ArrayList<String> relatedNodeList) {
		this.relatedNodeList = relatedNodeList;
	}
	
	public ArrayList<String> getBrotherNodeList() {
		return brotherNodeList;
	}

	public void setBrotherNodeList(ArrayList<String> brotherNodeList) {
		this.brotherNodeList = brotherNodeList;
	}

	public ArrayList<String> getChildrenNodeList() {
		return childrenNodeList;
	}

	public void setChildrenNodeList(ArrayList<String> childrenNodeList) {
		this.childrenNodeList = childrenNodeList;
	}

	public String getOther() {
		return other;
	}

	public void setOther(String other) {
		this.other = other;
	}
}
