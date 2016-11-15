package com.ifeng.iRecommend.featureEngineering.KnowledgeGraphSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.collections.IteratorUtils;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * 
 * <PRE>
 * 作用 : 
 *     知识图谱检索  
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016年9月23日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class KnowledgeGraphSearch {

	IndexKnowledgeGraph knowledgeGraph = null;

	static final String relationShip = "relationShip";

	private static KnowledgeGraphSearch INSTANCE = null;

	private static HashMap<String, Integer> peopleRelRankMap = null;

	private static HashMap<String, ArrayList<String>> PrimaryCateMap = null;

	private KnowledgeGraphSearch() {
		initRelRankMap();
		initPrimaryCateMap();
		knowledgeGraph = new IndexKnowledgeGraph();
	}

	/**
	 * 
	 * @Title:initRelRankMap
	 * @Description: 人物关系重要度排序初始化
	 * @author:wuyg1
	 * @date:2016年9月26日
	 */
	private void initRelRankMap() {

		peopleRelRankMap = new HashMap<String, Integer>();

		FileUtil fileUtil = new FileUtil();

		String content = fileUtil.Read(
				LoadConfig.lookUpValueByKey("peopleRelRankFile"), "utf-8");
		for (String row : content.split("\n")) {

			if (null == row || row.isEmpty()) {
				continue;
			}

			String word = row.substring(0, row.indexOf("\t"));

			String counts = row.substring(row.indexOf("\t") + "\t".length());

			counts = counts.replaceAll(" ", "");

			int count = Integer.valueOf(counts);

			peopleRelRankMap.put(word, count);
		}

		System.err.println();
	}

	/**
	 * 
	 * @Title:initPrimaryCateMap
	 * @Description: 术语实体TOP2主领域初始化
	 * @author:wuyg1
	 * @date:2016年9月27日
	 */
	private void initPrimaryCateMap() {
		PrimaryCateMap = new HashMap<String, ArrayList<String>>();
		FileUtil fileUtil = new FileUtil();

		String content = fileUtil.Read(
				LoadConfig.lookUpValueByKey("primaryCatFile"), "utf-8");

		for (String row : content.split("\n")) {

			if (null == row || row.isEmpty()) {
				continue;
			}

			String word = row.substring(0, row.indexOf("||["));

			ArrayList<String> categoryList = new ArrayList<String>();
			categoryList.addAll(Arrays.asList(row.substring(
					row.indexOf("[") + "[".length(), row.indexOf("]")).split(
					", ")));
			PrimaryCateMap.put(word, categoryList);
		}
		System.err.println();
	}

	/**
	 * 
	 * @Title:getInstance
	 * @Description:实例化
	 * @return
	 * @author:wuyg1
	 * @date:2016年9月23日
	 */
	public synchronized static KnowledgeGraphSearch getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new KnowledgeGraphSearch();
		}
		return INSTANCE;
	}

	/**
	 * 
	 * @Title:searchNode
	 * @Description: 查询指定query对应的节点集合
	 * @param query
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	private List<Vertex> searchNode(String query) {
		return knowledgeGraph.searchNode(query);
		// return knowledgeGraph.searchNoIndex(query);
	}

	/**
	 * 
	 * @Title:searchNode2CurNode
	 * @Description: 获取以vertex为终点，且与vertex是contatince关系的节点集合（默认是containce关系）
	 * @param vertex
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	private List<Vertex> searchNode2CurNode(Vertex vertex) {
		return knowledgeGraph.getNodes2CurNode(vertex);
	}

	/**
	 * 
	 * @Title:searchNode2CurNode
	 * @Description: 获取以vertex为终点，且与vertex是relationType关系的节点集合
	 * @param vertex
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	private List<Vertex> searchNode2CurNode(Vertex vertex, String relationType) {
		return knowledgeGraph.getNodes2CurNode(vertex, relationType);
	}

	/**
	 * 
	 * @Title:searchPeerNodesAboutCurNode
	 * @Description: 获取到vertex节点的同行的兄弟姐妹节点
	 * @param vertex
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年9月12日
	 */
	private List<Vertex> searchPeerNodesAboutCurNode(Vertex vertex,
			String relationType) {

		List<Vertex> peerNodeList = new ArrayList<Vertex>();

		peerNodeList = knowledgeGraph.getPeerNodes(vertex, relationType);

		peerNodeList = knowledgeGraph.getVertexList(peerNodeList);
		return peerNodeList;
	}

	/**
	 * 
	 * @Title:searchChildrenNodesAboutCurNode
	 * @Description: 获取到vertex节点的孩子节点，与vertex的关系是relationType
	 * @param vertex
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年9月12日
	 */
	private List<Vertex> searchChildrenNodesAboutCurNode(Vertex vertex,
			String relationType) {

		List<Vertex> childrenNodesList = new ArrayList<Vertex>();

		childrenNodesList = knowledgeGraph.getChildrenNodes(vertex,
				relationType);

		childrenNodesList = knowledgeGraph.getVertexList(childrenNodesList);
		return childrenNodesList;
	}

	/**
	 * 
	 * @Title:getChildrenNodeSet
	 * @Description: 获取指定vertex节点的孩子节点
	 * @param vertex
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年9月29日
	 */
	public HashSet<String> getChildrenNodeSet(Vertex vertex, String relationType) {
		HashSet<String> childrenNodeSet = new HashSet<String>();

		List<Vertex> childrenNodesList = new ArrayList<Vertex>();

		childrenNodesList = knowledgeGraph.getChildrenNodes(vertex,
				relationType);

		for (Vertex v : childrenNodesList) {
			String name = v.getProperty("word");
			childrenNodeSet.add(name);
		}
		return childrenNodeSet;
	}

	/**
	 * 
	 * @Title:searchNodeFromCurNode
	 * @Description:获取以vertex为起点，与vertex是containce关系的节点结合（默认是containce关系）
	 * @param vertex
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	private List<Vertex> searchNodeFromCurNode(Vertex vertex) {
		return knowledgeGraph.getNodesFromCurNode(vertex);
	}

	/**
	 * 
	 * @Title:searchNodeFromCurNode
	 * @Description:获取以vertex为起点，与vertex是relationType关系的节点集合
	 * @param vertex
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	private List<Vertex> searchNodeFromCurNode(Vertex vertex,
			String relationType) {
		return knowledgeGraph.getNodesFromCurNode(vertex, relationType);
	}

	private List<Vertex> searchNodeRelatedCurNode(Vertex vertex,
			String relationType) {
		return knowledgeGraph.getRelatedNodesWithCurNode(vertex, relationType);
	}

	public List<Edge> searchEdge2CurNode(Vertex vertex, String relationType) {
		return knowledgeGraph.getEdge2CurNode(vertex, relationType);
	}

	public List<Edge> searchEdgeFromCurNode(Vertex vertex, String relationType) {
		return knowledgeGraph.getEdgeFromCurNode(vertex, relationType);
	}

	private List<Vertex> findNodeClassify(Vertex vertex, String relationType) {
		String Sql = "select expand(in()) from (select from v where @rid='"
				+ vertex.getId() + "')";
		Iterable<Vertex> vs1 = knowledgeGraph.getGraph()
				.command(new OCommandSQL(Sql)).execute();

		return IteratorUtils.toList(vs1.iterator());
	}

	private List<Vertex> findAllParentList(Vertex vertex, String relationType) {
		// traverse in("EntityContaince") from (select * from V where word =
		// "刘国梁")
		String Sql = "traverse in('" + relationType
				+ "') from (select * from V where @rid = '" + vertex.getId()
				+ "')";
		Iterable<Vertex> vs1 = knowledgeGraph.getGraph()
				.command(new OCommandSQL(Sql)).execute();

		return IteratorUtils.toList(vs1.iterator());
	}

	/**
	 * 
	 * @Title:getTwoNodeShortestPath
	 * @Description: 获取两节点之间的最短距离
	 * @param query
	 * @param query2
	 * @author:wuyg1
	 * @date:2016年7月4日
	 */
	private void getTwoNodeShortestPath(String query, String query2) {
		List<Vertex> vertex = searchNode(query);

		List<Vertex> vertex2 = searchNode(query2);

		for (Vertex v1 : vertex) {
			for (Vertex v2 : vertex2) {
				getShortestPath(v1, v2);
			}
		}
	}

	private void getShortestPath(Vertex v1, Vertex v2) {
		knowledgeGraph.shortestPath(v1, v2);
	}

	public void shutdown() {
		knowledgeGraph.shutdown();
	}

	/**
	 * 
	 * @Title:getRelatedNodes
	 * @Description:获取到与该节点存在相关关系的节点
	 * @param vertex
	 * @return
	 * @author:wuyg1
	 * @date:2016年9月23日
	 */
	public HashMap<String, RelationEntity> getRelatedNodes(Vertex vertex) {

		HashMap<String, RelationEntity> relatedNodeMap = new HashMap<String, RelationEntity>();

		List<Edge> edgechilden = searchEdgeFromCurNode(vertex, "relationShip");

		for (Edge edge : edgechilden) {

			String edgeName = edge.getProperty("relation");

			if (null == edgeName) {
				continue;
			}

			Vertex inV = edge.getVertex(Direction.IN);

			String word = inV.getProperty("word");

			if (!relatedNodeMap.containsKey(edgeName)) {
				HashSet<String> set = new HashSet<String>();
				set.add(word);

				RelationEntity relationEntity = new RelationEntity();

				relationEntity.setCount(peopleRelRankMap.get(edgeName));
				relationEntity.setPersonSet(set);

				relatedNodeMap.put(edgeName, relationEntity);
			} else if (relatedNodeMap.containsKey(edgeName)) {
				relatedNodeMap.get(edgeName).getPersonSet().add(word);
			}
		}

		edgechilden = searchEdge2CurNode(vertex, "relationShip");

		for (Edge edge : edgechilden) {

			Vertex outV = edge.getVertex(Direction.OUT);

			String edgeName = edge.getProperty("relation");

			if (null == edgeName) {
				continue;
			}

			String word = outV.getProperty("word");

			if (!relatedNodeMap.containsKey(edgeName)) {
				HashSet<String> set = new HashSet<String>();
				set.add(word);

				RelationEntity relationEntity = new RelationEntity();

				relationEntity.setCount(peopleRelRankMap.get(edgeName));
				relationEntity.setPersonSet(set);

				relatedNodeMap.put(edgeName, relationEntity);
			} else if (relatedNodeMap.containsKey(edgeName)) {
				relatedNodeMap.get(edgeName).getPersonSet().add(word);
			}
		}

		if (relatedNodeMap.isEmpty() || relatedNodeMap.size() == 0) {
			relatedNodeMap = null;
		}

		return relatedNodeMap;
	}

	/**
	 * 
	 * @Title:getBrotherNodes
	 * @Description:获取到与该节点处于同层次的所有节点
	 * @param vertex
	 * @return
	 * @author:wuyg1
	 * @date:2016年9月23日
	 */
	public HashSet<String> getPeerNodes(Vertex vertex) {

		HashSet<String> brotherNodeSet = new HashSet<String>();
		// 同层次兄弟节点
		List<Vertex> peerNodeList = searchPeerNodesAboutCurNode(vertex,
				"EntityContaince");

		for (Vertex v : peerNodeList) {
			if (v.getPropertyKeys().contains("middleNode")) {
				boolean middleNode = v.getProperty("middleNode");
				if (middleNode) {
					continue;
				}
			}
			String name = v.getProperty("word");
			brotherNodeSet.add(name);
		}

		if (brotherNodeSet.isEmpty() || brotherNodeSet.size() == 0) {
			brotherNodeSet = null;
		}

		return brotherNodeSet;
	}

	/**
	 * 
	 * @Title:getClassifyBrotherNodes
	 * @Description:获取到与该节点处于同层次的所有节点
	 * @param vertex
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年9月23日
	 */
	public HashSet<String> getClassifyBrotherNodes(Vertex vertex,
			String relationType) {

		HashSet<String> brotherNodeSet = new HashSet<String>();
		List<Vertex> peerNodeList = new ArrayList<Vertex>();

		// 同层次兄弟节点
		peerNodeList = searchPeerNodesAboutCurNode(vertex, relationType);

		for (Vertex v : peerNodeList) {
			String name = v.getProperty("word");
			brotherNodeSet.add(name);
		}
		if (brotherNodeSet.isEmpty() || brotherNodeSet.size() == 0) {
			brotherNodeSet = null;
		}

		return brotherNodeSet;
	}

	/**
	 * 
	 * @Title:getMiddleBrotherNodes
	 * @Description:获取到与该中间节点处于同层次的所有节点
	 * @param vertex
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年9月23日
	 */
	public HashSet<String> getMiddleBrotherNodes(Vertex vertex,
			String relationType) {

		HashSet<String> brotherNodeSet = new HashSet<String>();
		// 同层次兄弟节点
		List<Vertex> peerNodeList = searchPeerNodesAboutCurNode(vertex,
				relationType);

		for (Vertex v : peerNodeList) {

			String name = v.getProperty("word");

			brotherNodeSet.add(name);
		}

		if (brotherNodeSet.isEmpty() || brotherNodeSet.size() == 0) {
			brotherNodeSet = null;
		}

		return brotherNodeSet;
	}

	/**
	 * 
	 * @Title:getBrotherNodes
	 * @Description:获取到与该节点处于同层次的所有节点
	 * @param vertex
	 * @param levelSet
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年9月23日
	 */
	public HashMap<String, HashSet<String>> getBrotherNodes(Vertex vertex,
			Set<String> levelSet, String relationType) {

		HashMap<String, HashSet<String>> brotherNodeMap = new HashMap<String, HashSet<String>>();
		// 同层次兄弟节点
		List<Vertex> peerNodeList = searchPeerNodesAboutCurNode(vertex,
				relationType);

		for (Vertex v : peerNodeList) {
			if (v.getPropertyKeys().contains("middleNode")) {
				boolean middleNode = v.getProperty("middleNode");
				if (middleNode) {
					continue;
				}
			}
			String name = v.getProperty("word");

			Set<String> classifySet = v.getProperty("corCategorySet");

			Set<String> peerNodeLevelSet = v.getProperty("levelTags");

			if (!peerNodeLevelSet.equals(levelSet) && !(levelSet.containsAll(peerNodeLevelSet) || peerNodeLevelSet.containsAll(levelSet))) {
				continue;
			}

			for (String classify : classifySet) {
				if (brotherNodeMap.containsKey(classify)) {
					brotherNodeMap.get(classify).add(name);
				} else {
					HashSet<String> set = new HashSet<String>();
					set.add(name);
					brotherNodeMap.put(classify, set);
				}
			}
		}

		if (brotherNodeMap.isEmpty() || brotherNodeMap.size() == 0) {
			brotherNodeMap = null;
		}

		return brotherNodeMap;
	}

	public ArrayList<PersonInfo> getRelationNodes(String query) {
		List<Vertex> vertexs = searchNode(query);

		ArrayList<PersonInfo> queryPersonList = new ArrayList<PersonInfo>();

		if (null == vertexs) {
			return null;
		}
		for (Vertex vertex : vertexs) {

			if (vertex.getPropertyKeys().contains("isolate")) {
				boolean isolate = vertex.getProperty("isolate");
				if (isolate) {
					continue;
				}
			}

			boolean classifyFlag = vertex.getProperty("classifyTree");

			// if (classifyFlag == false &&
			// vertex.getPropertyKeys().contains("middleNode")) {
			// boolean middleNode = vertex.getProperty("middleNode");
			// if (middleNode) {
			// continue;
			// }
			// }

			System.err.println(vertex.getId() + "\t"
					+ vertex.getProperty("word"));

			PersonInfo personInfo = new PersonInfo();

			String word = vertex.getProperty("word");

			Set<String> levelTagSet = vertex.getProperty("levelTags");
			
			Set<String> CorCategorySet = vertex.getProperty("corCategorySet");

			personInfo.setWord(word);

			personInfo.setClassifyNodeFlag(classifyFlag);

			HashSet<String> wordSet = new HashSet<String>();

			wordSet.add(word);

			boolean middleFlag = false;

			if (vertex.getPropertyKeys().contains("middleNode")) {
				boolean middleNode = vertex.getProperty("middleNode");
				if (middleNode == true) {
					middleFlag = true;
				}
			}

			if (middleFlag == true) {

				if (classifyFlag == true) {
					// ==========处理类似 中国足球这样的例子；主要获取孩子节点和同层次节点

					// System.err.println("------------分类体系同层次节点获取-------------------");

					String type = vertex.getProperty("typelabel");
					if (!type.equals("c0")) {
						HashSet<String> peerNodeSet = getClassifyBrotherNodes(
								vertex, "ClassifyContain");
						if (null != peerNodeSet) {
							personInfo.getBrotherNodeList().addAll(peerNodeSet);
						}
						personInfo.getBrotherNodeList().remove(word);
					}

					// ----------------------------------分类体系孩子节点---------------------
					// 孩子节点是分类体系的
					Set<String> childrenNodeSet = getChildrenNodeSet(vertex,
							"ClassifyContain");

					if (null != childrenNodeSet && !childrenNodeSet.isEmpty()) {
						personInfo.getChildrenNodeClassifyList().addAll(
								childrenNodeSet);
					}

					childrenNodeSet.clear();
					// 孩子节点不是分类体系的
					childrenNodeSet = getChildrenNodeSet(vertex,
							"EntityContaince");

					if (null != childrenNodeSet || !childrenNodeSet.isEmpty()) {
						personInfo.getChildrenNodeNoclassifyList().addAll(
								childrenNodeSet);
					}

				} else if (classifyFlag == false) {
					// ==========处理类似 广州恒大这样的例子；主要获取孩子节点和同层次节点

					// --------------非分类体系的同层次节点获取------------------------
					HashSet<String> brotherNodeSet = getMiddleBrotherNodes(
							vertex, "EntityContaince");

					if (null != brotherNodeSet) {
						personInfo.getBrotherNodeList().addAll(brotherNodeSet);
					}
					personInfo.getBrotherNodeList().remove(word);
					// -------------非分类体系的孩子节点----------------------------
					HashSet<String> childrenNodeSet = getChildrenNodeSet(
							vertex, "EntityContaince");

					if (null != childrenNodeSet) {
						personInfo.getChildrenNodeNoclassifyList().addAll(
								childrenNodeSet);
					}
				}

			} else if (middleFlag == false) {
				// System.err.println("-----------相关节点获取--------------------");
				HashMap<String, RelationEntity> relatedNodeMap = getRelatedNodes(vertex);

				if (null != relatedNodeMap) {
					ArrayList<String> keyList = new ArrayList<String>(
							relatedNodeMap.keySet());
					Collections
							.sort(keyList, new MapCompare<String, RelationEntity>(
									relatedNodeMap));
					for (String key : keyList) {
						for (String name : relatedNodeMap.get(key)
								.getPersonSet()) {
							if (wordSet.contains(name)) {
								continue;
							} else {
								wordSet.add(name);
								personInfo.getRelatedNodeList().add(name);
							}
						}
					}
				}

				// System.err.println("------------同层次节点获取-------------------");

				HashMap<String, HashSet<String>> peerNodeMap = getBrotherNodes(
						vertex, levelTagSet, "EntityContaince");

				if (null != peerNodeMap) {
					personInfo.setBrotherNodeMap(peerNodeMap);
					if (PrimaryCateMap.containsKey(word)) {
						ArrayList<String> categoryList = PrimaryCateMap
								.get(word);
						for (String category : categoryList) {
							if (peerNodeMap.containsKey(category)) {
								personInfo.getBrotherNodeList().addAll(
										peerNodeMap.get(category));
							}
						}
						if (null == personInfo.getBrotherNodeList()
								|| personInfo.getBrotherNodeList().isEmpty()) {
							Set<String> categorySet = vertex
									.getProperty("corCategorySet");
							for (String category : categorySet) {
								personInfo.getBrotherNodeList().addAll(
										peerNodeMap.get(category));

							}
						}
					}else{
						if(CorCategorySet.size() == 1){
							String category = null;
							for(String cat : CorCategorySet){
								if(null == cat || cat.isEmpty()){
									continue;
								}
								category = cat;
							}
							if(null != category && !category.isEmpty()){
								personInfo.getBrotherNodeList().addAll(personInfo.getBrotherNodeMap().get(category));
							}
							
						}
					}
					personInfo.getBrotherNodeList().remove(word);
				}
			}

			if (null != personInfo.getRelatedNodeList()
					|| !personInfo.getRelatedNodeList().isEmpty()) {
				personInfo.getRelationNodeList().addAll(
						personInfo.getRelatedNodeList());
			}

			if (null != personInfo.getBrotherNodeList()
					|| !personInfo.getBrotherNodeList().isEmpty()) {
				for (String name : personInfo.getBrotherNodeList()) {
					if (wordSet.contains(name)) {
						continue;
					} else {
						wordSet.add(name);
						personInfo.getRelationNodeList().add(name);
					}
				}

			}

			queryPersonList.add(personInfo);
		}
		return queryPersonList;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		KnowledgeGraphSearch knowledgeGraphSearch = KnowledgeGraphSearch
				.getInstance();

		while (true) {

			System.err.println("请输入查询的query:");

			Scanner scanner = new Scanner(System.in);
			String query = scanner.nextLine();

			ArrayList<PersonInfo> quePersonInfos = knowledgeGraphSearch
					.getRelationNodes(query);
			if (null == quePersonInfos) {
				continue;
			}

			for (PersonInfo personInfo : quePersonInfos) {
				System.err.println(personInfo.getWord() + "===");
				System.err.println("是否为分类体系:" + personInfo.isClassifyNodeFlag);
				System.err.println("相关关系:" + personInfo.getRelatedNodeList());
				System.err.println("同层次同领域节点:"
						+ personInfo.getBrotherNodeList());
				System.err.println("孩子节点是分类体系的："
						+ personInfo.getChildrenNodeClassifyList());
				System.err.println("孩子节点不是分类体系的："
						+ personInfo.getChildrenNodeNoclassifyList());
				System.err.println("集合:" + personInfo.getRelationNodeList());
			}

		}

	}

}
