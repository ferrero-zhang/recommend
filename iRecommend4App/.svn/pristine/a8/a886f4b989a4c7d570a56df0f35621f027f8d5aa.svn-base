package com.ifeng.iRecommend.featureEngineering.LayerGraph;
import java.util.ArrayList;
import java.util.List;

public class GraphNode {
	private NodeData data; //每个节点的词和类别
	private List<GraphNode> childList; //子节点列表
	private List<GraphNode> parentList; //父节点列表
	public GraphNode(){
		this.data=null;
		this.childList=new ArrayList<GraphNode>();
		this.parentList=new ArrayList<GraphNode>();
	}
	public GraphNode(NodeData data)
	{
		this.data = data;
		this.childList = new ArrayList<GraphNode>();
		this.parentList= new ArrayList<GraphNode>();
	}
	public GraphNode(NodeData data, List<GraphNode> childList,List<GraphNode> parentList)
	{
		this.data = data;
		this.childList = childList;
		this.parentList=parentList;
	}

	public NodeData getData() {
		return data;
	}

	public void setData(NodeData data) {
		this.data = data;
	}

	public List<GraphNode> getChildList() {
		return childList;
	}

	public void setChildList(List<GraphNode> childList) {
		this.childList = childList;
	}
	public List<GraphNode> getParentList() {
		return parentList;
	}

	public void setParentList(List<GraphNode> parentList) {
		this.parentList = parentList;
	}

}
