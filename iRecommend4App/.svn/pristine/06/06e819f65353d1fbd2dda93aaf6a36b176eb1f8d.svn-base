package com.ifeng.iRecommend.dingjw.itemParser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 一棵泛型树
 * <PRE>
 * 作用 : 
 *   
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
 *          1.0          2012-8-23        mayk          create
 * -----------------------------------------------------------------------------
 * @param <T>
 * </PRE>
 */
public class Tree<T>{
	private TreeNode<T> root;
	public static class TreeNode<T>{
		/**
		 * 树的节点类
		 */
		private int depth;
		private List<TreeNode<T>> children;
		private String nodeName;//结点的名字
		private Class<T> clazz;
		private T content;
		private TreeNode<T> parent;
		private String fullpath;
		public void setParent(TreeNode<T> parent){
			this.parent=parent;
			if(!parent.getName().equals("root"))
			fullpath= parent.getFull() +"-"+nodeName;
		}
		public TreeNode<T> getParent(){
			return this.parent;
		}
		
		public String getFull(){
			return fullpath==null?this.nodeName:this.fullpath;
		}
		
		public void setContent(T content){
			this.content = content;
		}
		
		public void setDepth(int dep){
			depth =dep;
		}
		
		public TreeNode(String nName,Class<T> clazz){
			
			this.clazz = clazz;
			children = new ArrayList<TreeNode<T>>();
			this.nodeName = nName;
			
		}
		public String ClassName(){
			return this.clazz.getName();
		}
		
		public void insertChildren(TreeNode<T> node){
			if(this.ClassName().equals(node.ClassName()))
				children.add(node);
		}
		
		public String getName(){
			return nodeName;
		}
		
		public List<TreeNode<T>> getChildren(){
			return children.size()==0?null:children;
		}
		
		public boolean isLeafNode(){
			return children.size()==0;
		}
		
		public TreeNode<T> RetrieveChild(String nName){
				for(TreeNode<T> child:children)
					if(child.getName().equals(nName))
						return child;
				return null;
		}
		public T getContent(){
			return this.content;
		}
		public int getDepth(){
			return this.depth;
		}
		
	
	}
	
	private Class<T> clazz;
	
	public Tree(Class<T> clazz){
		this.clazz = clazz;	
		this.root = new TreeNode<T>("root",clazz);
		
	}
	public Set<TreeNode<T>> allLeafs(TreeNode<T> node){
		Set<TreeNode<T>> leafs = new HashSet<TreeNode<T>>();
		List<TreeNode<T>> children = node.getChildren();
		if(children==null)
			leafs.add(node);
		else
			for(TreeNode<T> child:children)
					leafs.addAll(allLeafs(child));
		return leafs;

	}
	public TreeNode<T> getRoot(){
		return this.root;
	}
	/**
	 * 
	 * @param nNames 查找的节点路径 ，以列表形式给出。
	 * @return 节点 不存在返回null
	 */
	public TreeNode<T> SearchNode(List<String> nNames){
		
		TreeNode<T> startNode = root;
		Iterator<String> it = nNames.iterator();
		while(it.hasNext()){
			String name = it.next();
			if(name.equals("root"))
				continue;
			TreeNode<T> node  = startNode.RetrieveChild(name);
			if(node==null)
				return startNode;
			else
				startNode = node;
			}
		if(startNode!=root)
		return startNode;
		else
		return null;
	}
	/**
	 * 插入节点
	 * @param nNames 路径
	 * @param content 内容
	 */
	public void insertNode(List<String> nNames,T content){
		TreeNode<T> startNode = root;
		int depth = 0;
		for(String name:nNames){
			
			TreeNode<T> node  = startNode.RetrieveChild(name);
			if(node==null){
				node = new TreeNode<T>(name,this.clazz);
				node.setDepth(depth);
				node.setParent(startNode);
				startNode.insertChildren(node);
				
			}
			startNode=node;
			++depth;
		}
		startNode.setContent(content);
	}
	/**
	 * 打印树
	 */
	public void printTree(){
		print("", false, this.getRoot());
	}
	public void print(String prefix, boolean isTail,TreeNode<T> node) {
		
		
        System.out.println(prefix + (isTail ? "└── " : "├── ") +node.getName().substring(node.getName().lastIndexOf("-")+1,node.getName().length()));
        List<TreeNode<T>> children = node.getChildren();
        if (children != null) {
            for (int i = 0; i < children.size() - 1; i++) {
            	
                print(prefix + (isTail ? "    " : "│   "), false,children.get(i));
            }
            if (children.size() >= 1) {
            	print(prefix + (isTail ?"    " : "│   "), true,children.get(children.size()-1));
            }
        }
    }
	/**
	 * 
	 * @return 所有叶子节点
	 */
	public List<TreeNode<T>> LeafNodes(){
		List<TreeNode<T>> list = new ArrayList<TreeNode<T>>();
		return list;

	}

	public static void main(String ...args)
	{
		
		
		
		
	}	
	
}
