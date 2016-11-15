package com.ifeng.iRecommend.featureEngineering.LayerGraph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.LoadConfig;
/**
 * 
 * <PRE>
 * 作用 : 通过图结构实现分类体系的上下溯查询
 *   
 * 使用 : 输入需要查询的word或者需要查询的NodeData（word和type），给出其上下溯的查询结果
 *   
 * 示例 :以"银行"为例
 * 输入查询 NodeData("银行","sc"),节点唯一
 * 返回"银行sc"在图中节点位置，遍历可得到
 * 上溯结果：
 * 		c=财经,
 * 下溯结果：
 * 		cn=外资银行
 * 		cn=央行与银监会....
 * 但是银行有sc和cn两种类型，所以提供对word的查询，输入查询word:"银行",节点不唯一,会同时返回两种结果
 * sc=银行
 * 上溯结果：
 * 		c=财经,
 * 下溯结果：
 * 		cn=外资银行
 * 		cn=央行与银监会....
 * 
 * cn=银行
 * 上溯结果:
 * 		sc=理财,c=财经,
 * 下溯结果:
 * 
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016年1月14日        hyx          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class Graph {
	private static final Log LOG = LogFactory.getLog("Graph");
	private GraphNode root=new GraphNode(new NodeData("root","root"));
	private Map<NodeData, NodeData> nodeMap= new HashMap<NodeData, NodeData>(); //每个节点作为key，节点的某个父节点作为value，作为索引来查找节点位置
	private Map<String, ArrayList<String>> wordMap=new HashMap<String,ArrayList<String>>();//节点为key，value为对应类型列表，如：key=台湾，value=[c,x]
	
	private Map<String,Integer> type_value=new HashMap<String,Integer>();
	
	private static Graph instance = new Graph();
	
	private Graph(){
		createGraph();
	}
	
	public static Graph getInstance(){
		return instance;
	}
	/**
	 * 创建图，将文件中的图节点依次加入图中，每行的形式为：c1=财经,sc=宏观经济,cn=宏观政策
	 */
	private void createGraph(){	
		type_value.put("c0", 1);
		type_value.put("c1", 2);
		type_value.put("sc", 3);
		type_value.put("cn", 4);
		type_value.put("e", 4);		
		try {
			String Filename = LoadConfig.lookUpValueByKey("graphNodeFile");//图中节点数据从文件中读取
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(Filename),"UTF-8"));
			String str;
			while((str=reader.readLine())!=null){
				List<GraphNode> currentGraphNodeList=new ArrayList<GraphNode>();
				currentGraphNodeList.add(root);
				List<NodeData> nextNodeList=new ArrayList<NodeData>();
				String[] feature=str.split(",");
				List<String> featureList=new ArrayList<String>();
				for (int i = 0; i < feature.length; i++) {
					String[] group=feature[i].split("=");
					if (type_value.containsKey(group[0])) {
						featureList.add(group[0]);
						featureList.add(group[1]);
//						if (group[0].equals("c0") || group[0].equals("c1")) {
//							AddWordMap(group[1],"c");
//						}
						//else {
							AddWordMap(group[1],group[0]);
						//}
					}
				}
				if (featureList==null || featureList.isEmpty()) {
					continue;
				}
				getNextNodeList(nextNodeList,featureList);
				AddNode(currentGraphNodeList,nextNodeList,featureList);				
			}
			reader.close();
		} catch (Exception e) {
			// TODO: handle exception
			LOG.error(e);
		}			
	}
	/**
	 * 向图中增加节点
	 * @param currentGraphNodeList 当前节点列表
	 * @param nextNodeList 当前节点的下一个要插入节点列表
	 */
	private void AddNode(List<GraphNode> currentGraphNodeList,List<NodeData> nextNodeList,List<String> featureList){
		if(nextNodeList==null || nextNodeList.isEmpty()){
			return;
		}
		List<GraphNode> tempGraphNodeList=new ArrayList<GraphNode>();
		GraphNode tempGraphNode=null;
		for (NodeData nextnd : nextNodeList) {
			tempGraphNode=new GraphNode(nextnd);
			for (GraphNode currentGraphNode:currentGraphNodeList) {			
				if (nodeMap.containsKey(nextnd)) {//判断之前是否已经加过该节点
					GraphNode presentNode=FindNode(nextnd);
					if (ParentListContain(presentNode, currentGraphNode.getData().getWord()) || 
							(presentNode!=null && currentGraphNode.getData().getWord().equals("root"))) {
								//当前节点已存在，判断这个节点父节点列表中是否有当前的这个父节点
						if(!containGraphNode(tempGraphNodeList, presentNode)){
							tempGraphNodeList.add(presentNode);
						}
						continue;
					}
					else {
						currentGraphNode.getChildList().add(presentNode);
						presentNode.getParentList().add(currentGraphNode);
						if (containGraphNode(tempGraphNodeList, presentNode)) {
							tempGraphNodeList=DeleteGraphNode(tempGraphNodeList, presentNode);
						}
						tempGraphNodeList.add(presentNode);
					}
				}
				else {
					currentGraphNode.getChildList().add(tempGraphNode);
					tempGraphNode.getParentList().add(currentGraphNode);
					nodeMap.put(nextnd, currentGraphNode.getData());
					tempGraphNodeList.add(tempGraphNode);
				}
			}
		}
		currentGraphNodeList=tempGraphNodeList;
		if (featureList!=null && !featureList.isEmpty()) {
			nextNodeList.clear();
			getNextNodeList(nextNodeList,featureList);
			AddNode(currentGraphNodeList, nextNodeList, featureList);
		}
	}
	/**
	 * 获得下一个要插入的节点列表
	 * @param nextNodeList 存储下一个要插入的节点列表
	 * @param featureList 取出要插入的节点之后，将其删除
	 */
	private void getNextNodeList(List<NodeData> nextNodeList,List<String> featureList){
		String featureTypeValue=featureList.get(0);
		if (featureList.get(0).equals("c1") || featureList.get(0).equals("c0")) {
			nextNodeList.add(new NodeData(featureList.get(1),"c",featureList.get(0)));	
		}
		else {
			nextNodeList.add(new NodeData(featureList.get(1),featureList.get(0),featureList.get(0)));	
		}
		featureList.remove(1);
		featureList.remove(0);
		while (featureList!=null && !featureList.isEmpty() && type_value.get(featureList.get(0)).equals(type_value.get(featureTypeValue))) {
			if (featureList.get(0).equals("c1") || featureList.get(0).equals("c0")) {
				nextNodeList.add(new NodeData(featureList.get(1),"c",featureList.get(0)));	
			}
			else {
				nextNodeList.add(new NodeData(featureList.get(1),featureList.get(0),featureList.get(0)));	
			}
			featureList.remove(1);
			featureList.remove(0);
		}
	}
	/**
	 * 找到nodeData在图中的节点所在
	 * @return nodeData在图中的对应节点
	 */
	private GraphNode FindNode(NodeData nodeData){
		GraphNode presentNode=root;
		if (!nodeMap.containsKey(nodeData)) {
			return null;
		}
		List<NodeData> pathList=new ArrayList<NodeData>();
		pathList.add(nodeData);
		NodeData parent=nodeMap.get(nodeData);
		pathList.add(0,parent);
		while(!parent.equals(root.getData())){
			parent=nodeMap.get(parent);
			pathList.add(0,parent);
		}
		for (NodeData nd:pathList) {
			for (GraphNode gn : presentNode.getChildList()) {
				if (gn.getData().equals(nd)) {
					presentNode=gn;
					break;
				}
			}
		}
		return presentNode;
	}

	/**
	 * 下溯查找图节点 
	 * @param GraphNode 要查找的节点
	 * @return 查找结果
	 */
	public String iteratorGraphDown(GraphNode GraphNode)
	{
		NodeData nodeData=new NodeData();
		nodeData=GraphNode.getData();
		String result=iteratorDown(GraphNode, nodeData);
		return result;	
	}
	private String iteratorDown(GraphNode GraphNode,NodeData nodeData){
		StringBuffer buffer = new StringBuffer();
		//buffer.append("\n");
		
		if(GraphNode != null) 
		{	
			for (GraphNode index : GraphNode.getChildList()) 
			{
				boolean flag=true;
				for (GraphNode gn : index.getParentList()) {
					if (gn.getData().equals(nodeData)) {
						buffer.append(index.getData().getType()+"=" +index.getData().getWord());
						flag=false;
						break;
					}
				}
				if (flag) {
					buffer.append("	"+index.getData().getType()+"=" +index.getData().getWord());
				} 
				//buffer.append(GraphNode.getData().getWord());
				//buffer.append(index.getData().getLayer()+"=" +index.getData().getWord()+ "	");
				
				if (index.getChildList() != null && index.getChildList().size() > 0 ) 
				{	
					buffer.append(iteratorDown(index,nodeData));
					//buffer.append("\n");
				}
				else {
					buffer.append("\n");
				}	
			}
		}		
		buffer.append("\n");
		
		return buffer.toString();
	}
	/**
	 * 上溯查找图节点 
	 * @param GraphNode 要查找的节点
	 * @return 查找结果
	 */
	public String iteratorGraphUp(GraphNode GraphNode)
	{
		StringBuffer buffer = new StringBuffer();		
		if(GraphNode != null) 
		{	
			for (GraphNode index : GraphNode.getParentList()) 
			{
				if (!index.getData().getWord().equals("root")) {
//					boolean flag=true;
//					for (GraphNode gn:index.getChildList()) {
//						if (gn.getData().getWord().equals(queryWord)) {
// 							flag=false;
//							break;
//						}
//					}
				//	if (flag) {
						buffer.append(index.getData().getType() + "=" +index.getData().getWord() + ",");
				//	}
				//	else {
				//		buffer.append(index.getData().getType()+"=" +index.getData().getWord()+",");						
				//	}

					//upList.add(index.getData().getLayer()+"=" +index.getData().getWord()+ ",");
				}
				if (index.getParentList() != null && index.getParentList().size() > 0 ) 
				{
					buffer.append(iteratorGraphUp(index));
				}
				else {
					buffer.append("\n");
				}
			}
		}
		//buffer.append("\n");

		return buffer.toString();
	}
	/**
	 * 上溯查找图节点 
	 * @param GraphNode 要查找的节点
	 * @param UpResult 通过不断迭代，将结果存到UpResult中
	 * @param weight 权重，上溯传递权重
	 */
	public void iteratorGraphUp(GraphNode GraphNode,ArrayList<String> UpResult,String weight)
	{	
		if (weight==null || weight.isEmpty()) {
			weight="1.0";
		}
		if(GraphNode != null) 
		{	
			for (GraphNode index : GraphNode.getParentList()) 
			{
				if (!index.getData().getWord().equals("root")) {
					UpResult.add(index.getData().getWord());
					UpResult.add(index.getData().getType());
					if (index.getData().getTypeLabel().equals("c0")) {
						UpResult.add("-"+weight);
					}
					else {
						UpResult.add(weight);
					}
				}
				if (index.getParentList() != null && index.getParentList().size() > 0 ) 
				{
					iteratorGraphUp(index, UpResult,weight);
				}
			}
		}
	}
	
	/**
	 * 按NodeData查询
	 * @param querynodeData 当前要查询的NodeData
	 * @return NodeData在图中的节点
	 */
	public GraphNode queryNodeData(NodeData querynodeData){
		if (querynodeData.getType()==null || querynodeData.getWord()== null) {
			return null;
		}
		GraphNode presentNode=root;
		if (querynodeData.getTypeLabel()==null){
			if(querynodeData.getType().equals("c")) {
				ArrayList<String> types=wordMap.get(querynodeData.getWord());
				if (types==null) {
					return null;
				}
				querynodeData.setTypeLable(types.get(0));
			}
			else {
				querynodeData.setTypeLable(querynodeData.getType());
			}
		}
		if (!nodeMap.containsKey(querynodeData)) {
			return null;
		}
		List<NodeData> pathList=new ArrayList<NodeData>();
		pathList.add(querynodeData);
		NodeData parent=nodeMap.get(querynodeData);
		while(!parent.equals(root.getData())){
			pathList.add(0,parent);
			parent=nodeMap.get(parent);
		}
		for (NodeData nd:pathList) {
			for (GraphNode gn : presentNode.getChildList()) {
				if (gn.getData().equals(nd)) {
					presentNode=gn;
					break;
				}
			}
		}
		//System.out.println(presentNode.getChildList());
		//System.out.println(presentNode.getParentList());
		return presentNode;
	}
	/**
	 * 按word查询
	 * @param queryword 当前要查询的word（可能对应多个nodeData）
	 * @return word 对于多个nodeData,返回图中节点列表
	 */
	public ArrayList<GraphNode> queryWord(String queryword){
		if (queryword == null || queryword.isEmpty()) {
			return null;
		}
		ArrayList<String> typeList=new ArrayList<String>();
		String modifyqueryword=isBuildIn(wordMap.keySet(), queryword);
		if(modifyqueryword == null){
			return null;
		}
		typeList=wordMap.get(modifyqueryword);
		if (typeList==null || typeList.isEmpty()) {
			return null;
		}
		ArrayList<GraphNode> graphNodeList=new ArrayList<GraphNode>();
		for (String typelabel:typeList) {
			String type;
			if (typelabel.equals("c0") || typelabel.equals("c1")) {
				type="c";
			}
			else {
				type=typelabel;
			}
			NodeData nd=new NodeData(modifyqueryword,type,typelabel);
			graphNodeList.add(queryNodeData(nd));
		}
		return graphNodeList;
	}
	/**
	 * 增加word到type的映射 
	**/
	private void AddWordMap(String word,String type){
		ArrayList<String> typeList=new ArrayList<String>();
		if (wordMap.containsKey(word)) {
			if (wordMap.get(word).contains(type)) {
				return;
			}
			else {
				typeList=wordMap.get(word);
				typeList.add(type);
				wordMap.put(word, typeList);
			}
		}
		else {
			typeList.add(type);
			wordMap.put(word, typeList);
		}
	}
	/**
	 * 判断某节点的父节点是否包含某word 
	**/
	private boolean ParentListContain(GraphNode graphnode,String s){
		for (GraphNode gn: graphnode.getParentList()) {
			if (gn.getData().getWord().equals(s)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 删除图节点列表中的某节点 
	**/
	private List<GraphNode> DeleteGraphNode(List<GraphNode> GraphNodeList,GraphNode graphNode) {
		for (int i = 0; i < GraphNodeList.size(); i++) {
			if (GraphNodeList.get(i).getData().getWord().equals(graphNode.getData().getWord())) {
				GraphNodeList.remove(i);
			}
		}
		return GraphNodeList;
	}
	/**
	 * 判断图节点列表中是否包含某图节点 
	**/
	private boolean containGraphNode(List<GraphNode> GraphNodeList,GraphNode graphNode){
		for (GraphNode gn:GraphNodeList) {
			if (gn.getData().getWord().equals(graphNode.getData().getWord())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 规范str的大小写，返回set中跟str对应的字符串
	 */
	private String isBuildIn(Set<String> set, String str){
		if(set == null || set.isEmpty() || str == null || str.isEmpty())
			return null;
		for(String s : set){
			if(str.equalsIgnoreCase(s))
				return s;
		}
		return null;
	}
}
