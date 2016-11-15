package com.ifeng.iRecommend.featureEngineering.KnowledgeGraphSearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.ifeng.commen.Utils.FileUtil;
import com.ifeng.commen.Utils.LoadConfig;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.intent.OIntentMassiveInsert;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdgeType;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;

/**
 * 
 * <PRE>
 * 作用 : 
 *     知识图谱构建  
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
 *          1.0          2016年4月15日        wuyg1          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class IndexKnowledgeGraph {

	static Log LOG = LogFactory.getLog(IndexKnowledgeGraph.class);

	OrientGraph graph = null;

	Index<Vertex> index = null;

	static int count = 0;

	final String branchRelation = "EntityContaince";
	
	public OrientGraph getGraph() {
		return graph;
	}

	public void setGraph(OrientGraph graph) {
		this.graph = graph;
	}

	public Index<Vertex> getIndex() {
		return index;
	}

	public void setIndex(Index<Vertex> index) {
		this.index = index;
	}

	public IndexKnowledgeGraph() {

		this.graph = OrientGraphInstance.getInstance().getTx();

		// OGlobalConfiguration.dumpConfiguration(System.out);

		index = graph.getIndex("indexword", Vertex.class);

		this.graph.getRawGraph().getLocalCache().setEnable(true);

		Map<String, Object> configMap = new HashMap<String, Object>();

		configMap.put("client.channel.minPool", 50);
		configMap.put("client.channel.maxPool", 1000);
		configMap.put("profiler.enabled", false);
		configMap.put("cache.local.enabled", true);

		OGlobalConfiguration.setConfiguration(configMap);

		/**
		 * Also do not use such huge heap you only introduce big GC pauses,
		 * OrientDB does not use heap it uses "direct memory" so just set this
		 * settings to -Xmx1024M -Xms1024M and and allow disk cache to use the
		 * rest of RAM by using setting storage.diskCache.bufferSize (in
		 * megabytes) and of course do not go into swap.
		 */

		/**
		 * 创建Index
		 * 
		 * this.graph.createKeyIndex("m_id", Vertex.class, new Parameter<String,
		 * String>("class", "MyClass"), new Parameter<String, String>("type",
		 * "UNIQUE"));
		 * 
		 * UNIQUE 可以替换为 DICTIONARY_HASH_INDEX，
		 * 
		 * 插入数据 vertex = this.graph.addVertex("class:myclass");
		 * vertex.setProperty("m_id", id);
		 * 
		 * 利用Index查询 vertex = this.graph.getVertices("m_id",
		 * id).iterator().next();
		 * 
		 * 
		 * -Xmx4gb -Dstorage.diskCache.bufferSize=20000
		 */

		/**
		 * Write Ahead Log, WAL form now, is operation log which is used to
		 * store data about operations which were performed on disk cache page.
		 * WAL is enabled by default.
		 * 
		 * You could disable the journal (WAL) for some operations where
		 * reliability is not necessary:
		 * 
		 * -Dstorage.useWAL=false By default, the WAL files are written in the
		 * database folder. Since these files can growth very fast, it's a best
		 * practice to store in a dedicated partition. WAL are written in
		 * append-only mode, so there is not much difference on using a SSD or a
		 * normal HDD. If you have a SSD we suggest to use for database files
		 * only, not WAL.
		 * 
		 * To setup a different location than database folder, set the
		 * WAL_LOCATIONvariable.
		 * 
		 * OGlobalConfiguration.WAL_LOCATION.setValue("/temp/wal")
		 * 
		 * or at JVM level:
		 * 
		 * java ... -Dstorage.wal.path=/temp/wal ...
		 * 
		 * 在 进行大量插入数据操作时，可以把该项设置为false
		 * 
		 * In case of massive insertion, specially when this operation is made
		 * just once, you could disable the journal (WAL) to improve insertion
		 * speed:
		 * 
		 * -storage.useWAL=false
		 * 
		 * OGlobalConfiguration.WAL_LOCATION.setValue(false);
		 * 
		 * Disable sync on flush of pages
		 * 
		 * This setting avoids to execute a sync at OS level when a page is
		 * flushed. Disabling this setting will improve throughput on writes:
		 * 
		 * -Dstorage.wal.syncOnPageFlush=false
		 * 
		 * //OGlobalConfiguration.WAL_SYNC_ON_PAGE_FLUSH.setValue(false);
		 * 
		 * that is not heap dump, you may create heap dump automatically in case
		 * of OOM by providing option -XX:+HeapDumpOnOutOfMemoryError (this one
		 * will be very useful)
		 */

		// OGlobalConfiguration.WAL_SYNC_ON_PAGE_FLUSH.setValue(false);

		OGlobalConfiguration.USE_WAL.setValue(false);

		// OGlobalConfiguration.WAL_LOCATION.setValue("D:/orienttest/temp/wal");
	}

	public IndexKnowledgeGraph(OrientGraph graph) {
		this.graph = graph;
		index = graph.getIndex("indexword", Vertex.class);
	}

	public Iterable<Vertex> getVertex(OrientGraph graph, String query) {

		Iterable<Vertex> vertexIter = graph.query().has("word", query)
				.vertices();
		return vertexIter;
	}

	/**
	 * 
	 * @Title:getParentNodesList
	 * @Description: 获取指定节点的父节点
	 * @param vertex
	 * @author:wuyg1
	 * @date:2016年5月12日
	 */
	@SuppressWarnings("unchecked")
	public List<Vertex> getParentNodesList(Vertex vertex) {
		String Sql = "traverse in('containce') from (select from v where @rid='"
				+ vertex.getId() + "') strategy DEPTH_FIRST";

		Iterable<Vertex> parentVertex = graph.command(
				new OSQLSynchQuery<Vertex>(Sql)).execute();

		Iterator<Vertex> pIterator = parentVertex.iterator();

		while (pIterator.hasNext()) {
			Vertex child = pIterator.next();
			System.err.println("   " + child.toString());
			for (String key : child.getPropertyKeys()) {
				System.err.print(key + ":" + child.getProperty(key) + "\t");
			}
			System.err.println();
		}

		return IteratorUtils.toList(pIterator);
	}

	/**
	 * 
	 * @Title:getNodes2CurNode
	 * @Description: 获取以vertex为终点，且与vertex是contatince关系的节点集合（默认是containce关系）
	 * @param vertex
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	@SuppressWarnings("unchecked")
	public List<Vertex> getNodes2CurNode(Vertex vertex) {
		// 只输出一层关系
		String Sql = "select expand(in('" + RelationType.CONTAINCE.name()
				+ "')) from (select from v where @rid='" + vertex.getId()
				+ "')";
		Iterable<Vertex> vs1 = graph.command(new OCommandSQL(Sql)).execute();

		return IteratorUtils.toList(vs1.iterator());
	}

	/**
	 * @Title:getNodes2CurNode
	 * @Description:获取以vertex为终点，且与vertex是relationType关系的节点集合
	 * @param vertex
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	@SuppressWarnings("unchecked")
	public List<Vertex> getNodes2CurNode(Vertex vertex, String relationType) {
		// 只输出一层关系
		String Sql = "select expand(in('" + relationType
				+ "')) from (select from v where @rid='" + vertex.getId()
				+ "')";
		
		//System.err.println(Sql);
		
		Iterable<Vertex> vs1 = graph.command(new OCommandSQL(Sql)).execute();
		
		return getVertexList(vs1);
	}
	
	/**
	 * @Title:getNodes2CurNode
	 * @Description:获取以vertex为终点，且与vertex是relationType关系的节点集合
	 * @param vertex
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	@SuppressWarnings("unchecked")
	public List<Vertex> getPeerNodes(Vertex vertex, String relationType) {
		// 只输出一层关系
		String Sql = "select expand(in('" + relationType
				+ "').out('"+relationType+"')) from " + vertex.getId()
				+ "";
		
		//System.err.println(Sql);
		
		Iterable<Vertex> vs1 = graph.command(new OCommandSQL(Sql)).execute();
		
		return getVertexList(vs1);
	}
	
	/**
	 * @Title:getChildrenNodes
	 * @Description:获取以vertex为父节点的孩子节点
	 * @param vertex
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	@SuppressWarnings("unchecked")
	public List<Vertex> getChildrenNodes(Vertex vertex, String relationType) {
		// 只输出一层关系
		String Sql = "select expand(out('"+relationType+"')) from " + vertex.getId()
				+ "";
		
		Iterable<Vertex> vs1 = graph.command(new OCommandSQL(Sql)).execute();
		
		return getVertexList(vs1);
	}
	
	
	private List<Vertex> getVertexList(Iterable<Vertex> vIterable){
		return getVertexList(vIterable.iterator());
	}
	
	private List<Vertex> getVertexList(Iterator<Vertex> vIterator){
		
		HashSet<Object> set = new HashSet<Object>();
		
		List<Vertex> verList = new ArrayList<Vertex>();
		
		while(vIterator.hasNext()){
			Vertex keyVertex = vIterator.next();
			
			Object idObject = keyVertex.getId();
			
			if(set.contains(idObject)){
				continue;
			}else{
				verList.add(keyVertex);
				set.add(idObject);
			}
			
		}
		return verList;
	}
	
	public List<Vertex> getVertexList(List<Vertex> vList){
		
		HashSet<Object> set = new HashSet<Object>();
		
		List<Vertex> verList = new ArrayList<Vertex>();
		
		for(Vertex vertex : vList){

			Object idObject = vertex.getId();
			
			if(set.contains(idObject)){
				continue;
			}else{
				verList.add(vertex);
				set.add(idObject);
			}
			
		}
		return verList;
	}

	/**
	 * 
	 * @Title:getNodesFromCurNode
	 * @Description: 获取以vertex为起点，与vertex是containce关系的节点结合（默认是containce关系）
	 * @param vertex
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	@SuppressWarnings("unchecked")
	public List<Vertex> getNodesFromCurNode(Vertex vertex) {
		// 只输出一层关系
		String Sql = "select expand(out('" + RelationType.CONTAINCE.name()
				+ "')) from (select from v where @rid='" + vertex.getId()
				+ "')";
		Iterable<Vertex> vs1 = graph.command(new OCommandSQL(Sql)).execute();

		return getVertexList(vs1);
	}

	/**
	 * 
	 * @Title:getNodesFromCurNode
	 * @Description: 获取以vertex为起点，与vertex是relationType关系的节点集合
	 * @param vertex
	 * @param relationType
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	@SuppressWarnings("unchecked")
	public List<Vertex> getNodesFromCurNode(Vertex vertex, String relationType) {
		// 只输出一层关系
		String Sql = "select expand(out('" + relationType
				+ "')) from (select from v where @rid='" + vertex.getId()
				+ "')";

		//System.err.println(Sql);

		Iterable<Vertex> vs1 = graph.command(new OCommandSQL(Sql)).execute();
		
		return getVertexList(vs1);
	}
	
	public List<Vertex> getRelatedNodesWithCurNode(Vertex vertex, String relationType) {
		// 只输出一层关系
		String Sql = "select expand(both('" + relationType
				+ "')) from " + vertex.getId()
				+ "";
		
		//System.err.println(Sql);
		
//		String Sql = "select expand(both('" + relationType
//				+ "')) from (select from v where @rid='" + vertex.getId()
//				+ "')";

		Iterable<Vertex> vs1 = graph.command(new OCommandSQL(Sql)).execute();
		
		return getVertexList(vs1);
	}
	
	public List<Edge> getEdge2CurNode(Vertex vertex, String relationType){
		String Sql = "SELECT expand(inE("+relationType+")) from "+vertex.getId();
		
		//System.err.println(Sql);
		
		Iterable<Edge> edges = graph.command(new OCommandSQL(Sql)).execute();
	
		return getEdgeList(edges);	
	}
	
	public List<Edge> getEdgeFromCurNode(Vertex vertex, String relationType){
		String Sql = "SELECT expand(outE("+relationType+")) from "+vertex.getId();
		
		//System.err.println(Sql);
		
		Iterable<Edge> edges = graph.command(new OCommandSQL(Sql)).execute();
	
		return getEdgeList(edges);	
	}
	
	private List<Edge> getEdgeList(Iterable<Edge> eIterable){
		return getEdgeList(eIterable.iterator());
	}
	
	private List<Edge> getEdgeList(Iterator<Edge> eIterator){
		HashSet<Object> set = new HashSet<Object>();
		
		List<Edge> edgeList = new ArrayList<Edge>();
		
		while(eIterator.hasNext()){
			Edge keyEdge = eIterator.next();
			
			Object idObject = keyEdge.getId();
			
			if(set.contains(idObject)){
				continue;
			}else{
				edgeList.add(keyEdge);
				set.add(idObject);
			}
		}
		return edgeList;
	}

	/**
	 * 
	 * @Title:searchNode
	 * @Description: 查询指定的节点
	 * @param query
	 * @return
	 * @author:wuyg1
	 * @date:2016年5月13日
	 */
	@SuppressWarnings("unchecked")
	public List<Vertex> searchNode(String query) {
		Iterator<Vertex> iterable = index.get("indexword", query).iterator();

		if(iterable.hasNext()){
			try {
				return getVertexList(iterable);
			} catch (java.lang.IllegalArgumentException e) {
				// TODO: handle exception
				iterable = index.get("indexword", query).iterator();
				System.err.println(iterable.hasNext());
				System.err.println(query);
				System.err.println(iterable.hasNext());
				iterable = index.get("indexword", query).iterator();
				while(iterable.hasNext()){
					Vertex vertex = iterable.next();
					for (String property : vertex.getPropertyKeys()) {
						System.err.print(property + ":"
								+ vertex.getProperty(property) + "\t");
					}

				}
			}
			
		}else{
			return null;
		}
		return null;
		
	}

	public List<Vertex> searchNoIndex(String query) {
		Iterable<Vertex> iterator = graph.command(
				new OCommandSQL("SELECT FROM V WHERE word ='" + query + "'"))
				.execute();
		return getVertexList(iterator);
	}

	public Vertex searchOneNode(String query) {
		Vertex vertex = null;
		Iterator<Vertex> iterable = index.get("indexword", query).iterator();
		while (iterable.hasNext()) {
			Vertex v = iterable.next();
			synchronized (IndexKnowledgeGraph.class) {
				count++;
			}
			System.err.println(count);
			for (String key : v.getPropertyKeys()) {
				System.err.println(key + "\t" + v.getProperty(key));
			}
			vertex = v;
		}
		return vertex;
	}

	public void shortestPath(Vertex from_vertex, Vertex to_Vertex) {

		String sql = "select expand(shortestPath) from( select shortestPath("
				+ from_vertex.getId().toString() + ", "
				+ to_Vertex.getId().toString() + ",'BOTH'))";

		LOG.info(sql);

		OSQLSynchQuery<Vertex> synchQuery = new OSQLSynchQuery<Vertex>(sql);

		Iterable<Vertex> execute_v = graph.command(synchQuery).execute();
		
		List<Vertex> verList = getVertexList(execute_v);

		LOG.info("vertex list : ");

		for (Vertex od : verList) {

			LOG.info(od.getProperty("word") + "\t");

		}

	}
	/**
	 * 
	* @Title:createEdgeBetweenVertex
	* @Description:建立两个顶点之间的边
	* @param middleVertexList
	* @param vertex
	* @param relation
	* @param weight
	* @author:wuyg1
	* @date:2016年7月22日
	 */
	public void createEdgeBetweenVertex(ArrayList<Vertex> middleVertexList, Vertex vertex, 
			String relation, int weight){
		for (Vertex v : middleVertexList) {
			Edge e = graph.addEdge("class:REL", v, vertex,
					relation);
			e.setProperty("weight", weight);
		}
	}
	

	// 判断图graph 两个顶点v1和v2之间是否已经有此边
	private static boolean IsExistEdge(Vertex v1, Vertex v2) {
		Iterable<Edge> edges = v1.getEdges(Direction.OUT);
		for (Edge e : edges) {
			if (e.getVertex(Direction.IN).equals(v2)) {
				return true;
			}
		}
		return false;
	}

	public void commit() {
		graph.commit();
	}

	public void shutdown() {
		graph.shutdown();
	}

}
