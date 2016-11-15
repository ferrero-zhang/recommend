package com.ifeng.iRecommend.zxc.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 可以支持匹配简单伪正则的Trie树。简单伪正则模式：韩国{10}宣布{5}疫情{5}结束=韩国{0,10}宣布{0,5}疫情{0,5}结束
 * 也支持普通字符串匹配。示例见main函数
 * @author zhouxiaocao
 * @create 2015-12-23
 * **/
public class TrieTree implements Serializable{
	public static class MaxMatchInfo{
		/**
		 * @field str 匹配到的字符串
		 * **/
		List<String> str;
		/**
		 * @field startIndex str在被匹配字符串中的起始位置
		 * **/
		int startIndex;
		/**
		 * @field 匹配到的正则
		 * **/
		String rex;
		int endIndex;
		int matchCount;
		public MaxMatchInfo(List<String> str, int startIndex,int endIndex,String rex) {
			super();
			this.str = str;
			this.startIndex = startIndex;
			this.rex=rex;
		}
		public void incCount(){
			
		}
		public int getStartIndex() {
			return startIndex;
		}
		public String getRex() {
			return rex;
		}
		public String toString(){
			return str+"-->"+startIndex;
		}
	}
	
	//距离子节点
	protected Set<TrieTree> rexDistanceNode=new HashSet<TrieTree>();
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -7054366549011455074L;
	private boolean isEnd; // 是否到叶子节点
	private Map<String, TrieTree> children; // 子节点
	private String tag;

	//节点类型，0为普通文字节点，1为正则距离节点。
	protected int type=0;
	//如果其父节点是距离节点，该字段是其父节点tag中定义的距离数值
	protected int rexDistance=0;
	//当前节点所有距离节点的最大值。
	protected int maxRexDistance=0;
	public String toString() {
		String s = this.tag;
		if(!isEnd)
			for(String ttag : children.keySet()) {
				s += "[" + children.get(ttag) + "]";
			}
		return s;
	}

	TrieTree(boolean isEnd, HashMap<String, TrieTree> children) {
		this.isEnd = isEnd;
		this.children = children;
	}

	private void init(Collection<String> nodes){
		this.isEnd = false;
		this.children = new HashMap<String, TrieTree>();
		TrieTree root = this;
		for(String node : nodes) {
			if(node.trim().length()>0){
				addWord(root, node, node);
			}
			
		}
	}


	/**
	 * @Title: TrieTree
	 * @Description: TrieTree构造
	 * @param @param nodes
	 * @return 返回类型
	 * @throws Exception
	 */
	public TrieTree(Collection<String> nodes) {
		init(nodes);
	}

	private void addWord(TrieTree t, String word, String origin) {
		if(word.length() == 0) {
			TrieTree leaf = new TrieTree(true, null);
			leaf.tag = origin;
			t.children.put("$end", leaf);
		} else{
			int index=1;
			int rexDis=0;
			int type=0;
			if(word.startsWith("{")){
				type=1;
				while(word.charAt(index)!='}'){
					index++;
				}
				rexDis=Integer.valueOf(word.substring(1,index));
				index+=1;
			}
			String addWord=word.substring(0, index);
			if(t.children.containsKey(addWord)) {
				addWord(t.children.get(addWord), word.substring(index), origin);
			} else {
				TrieTree subt = new TrieTree(false, new HashMap<String, TrieTree>());
				subt.tag = addWord;
				subt.type=type;
				t.children.put(subt.tag, subt);
				addWord(subt, word.substring(index), origin);
				if(type==1){
					subt.rexDistance=rexDis;
					t.rexDistanceNode.add(subt);
					if(rexDis>t.maxRexDistance){
						t.maxRexDistance=rexDis;
					}
				}
			}
		}
	}

	/*private static int find(String str, int index, TrieTree t,int n,List<TrieTree> matchedStr,List<Integer> maxMatchN,int atLeastN) {
		
		if(t.children.containsKey("$end")) {
			maxMatchN.clear();
			maxMatchN.add(n);
			matchedStr.clear();
			matchedStr.add(t);
			return index;
		}else{
			if(index == str.length()) { // 到了字符串末尾
				return -1;
			} else {
				String tag = str.substring(index, index + 1);
				String tagTmp="";
				Set<TrieTree> rexts=t.rexDistanceNode;
				int nOri=n;
				
				for(TrieTree rext:rexts){
					int indexTmp=index;
					n=nOri;
					for(int dis=0;dis<=t.maxRexDistance;dis++){
						if(rext.rexDistance>=dis&&indexTmp<str.length()){
							tagTmp=str.substring(indexTmp, indexTmp + 1);
							n=nOri;
							if(rext.children.containsKey(tagTmp)){
								n++;
								int indexRt=find(str, indexTmp+1, rext.children.get(tagTmp),n,matchedStr,maxMatchN,atLeastN);
								if(indexRt>-1){
									return indexRt;
								}
							}
							indexTmp++;
						}
					}
				}
				if(t.children.containsKey(tag)){
					n++;
					return find(str, index + 1, t.children.get(tag),n,matchedStr,maxMatchN,atLeastN);
				}else{
					 if(n>maxMatchN.get(0)&&n>=atLeastN){
						maxMatchN.clear();
						maxMatchN.add(n);
						matchedStr.clear();
						matchedStr.add(t);
					 }
					return -1;
				}
			//}

		
		}
	}*/
	/**
	 * @Description: 从字符串@param str中找到第一个匹配的关键词
	 * @param str 待检测字符串
	 * @param index 开始检测的偏移量
	 * @param t 用于匹配的trie树
	 * @param matchedRex 存放匹配到的正则。
	 * @return 匹配的关键词的结束位置+1
	 */
	private static int find(String str, int index, TrieTree t,List<String> matchedRex) {
		String matchNode = null;
		/*if(t.children.containsKey("$end")) {
			matchNode = t.children.get("$end").tag;
			matchedRex.add(matchNode);
			return index;
		}else{*/
			if(index == str.length()) { // 到了字符串末尾
				if(t.children.containsKey("$end")) {
					matchNode = t.children.get("$end").tag;
					matchedRex.add(matchNode);
					return index;
				}
				return -1;
			} else {
				String tag = str.substring(index, index + 1);
				String tagTmp="";
				Set<TrieTree> rexts=t.rexDistanceNode;
				for(TrieTree rext:rexts){
					int indexTmp=index;
					for(int dis=0;dis<=t.maxRexDistance;dis++){
						if(rext.rexDistance>=dis&&indexTmp<str.length()){
							tagTmp=str.substring(indexTmp, indexTmp + 1);
							if(rext.children.containsKey(tagTmp)){
								int rtIndex=find(str, indexTmp+1, rext.children.get(tagTmp),matchedRex);
								if(rtIndex>-1){
									return rtIndex;
								}
							}
							indexTmp++;
						}
					}
				}
				if(t.children.containsKey(tag)){
					if(t.children.containsKey("$end")) {
						matchNode = t.children.get("$end").tag;
						matchedRex.add(matchNode);
						
					}
					return find(str, index + 1, t.children.get(tag),matchedRex);
				}else{
					if(t.children.containsKey("$end")) {
						matchNode = t.children.get("$end").tag;
						matchedRex.add(matchNode);
						return index;
					}
					return -1;
				}
			//}

		
		}
	}

	/**
	 * @Description:完全匹配输入的字符串
	 * @param str 输入字符串
	 * @return Map<匹配到的正则,List<匹配到的字符串的起始位置>>
	 */
	public Map<String,List<Integer>> detect(String str) {
		int i = 0;
		 Map<String,List<Integer>> matchMap = new HashMap<String,List<Integer>>();
		
		while(i < str.length()) {
			List<String> matchedRex=new ArrayList<String>();
			int index=find(str, i, this,matchedRex);
			if(matchedRex.size()>0){
				//String matchedStr=str.substring(i,index);
				for(int j=0;j<matchedRex.size();j++){
					List<Integer> pos=matchMap.get(matchedRex.get(j));
					if(pos==null){
						pos=new ArrayList<Integer>();
						
						matchMap.put(matchedRex.get(j), pos);
					}
					pos.add(i);
				}
				
				i++;
			}else{
				i++;
			}
		}
		return matchMap;
	}
	/*private void getAllStr(List<String> allStr){
		if(!this.children.containsKey("$end")){
			for(String ttag : children.keySet()) {
				 children.get(ttag).getAllStr(allStr);
			}
		}else{
			String rt=children.get("$end").tag;
			allStr.add(rt.replaceAll("\\{.*?\\}", ""));
		}
	}*/
/*	public Map<String,List<Integer>> detectMaxMatched(String str,int atLeastN) {
		int i = 0;
		Map<String,List<Integer>> rt=new HashMap<String,List<Integer>>();
		while(i < str.length()) {
			List<TrieTree> matchedTrie=new ArrayList<TrieTree>();
			List<Integer> maxMatchN=new ArrayList<Integer>();
			maxMatchN.add(0);
			int index=find(str, i, this,0,matchedTrie, maxMatchN,atLeastN);
			if(matchedTrie.size()>0){
				TrieTree tr=matchedTrie.get(0);
				List<String> allMatchedStr=new ArrayList<String>();
				tr.getAllStr(allMatchedStr);
				for(String s:allMatchedStr){
					List<Integer> pos=rt.get(s);
					if(pos==null){
						pos=new ArrayList<Integer>();
						rt.put(s, pos);
					}
					pos.add(i);
				}
			}
			if(index>-1){
				i=index+1;
			}else{
				i++;
			}
		}
		return rt;
	}*/
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + (isEnd ? 1231 : 1237);
		result = prime * result + 1237;
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof TrieTree)) {
			return false;
		}
		TrieTree other = (TrieTree)obj;
		if(children == null) {
			if(other.children != null) {
				return false;
			}
		} else if(!children.equals(other.children)) {
			return false;
		}
		if(isEnd != other.isEnd) {
			return false;
		}
		if(tag == null) {
			if(other.tag != null) {
				return false;
			}
		} else if(!tag.equals(other.tag)) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) throws Exception {
		Set<String> starNodes = new HashSet<String>();
		starNodes.add("小");
		starNodes.add("小王子");
		/*starNodes.add("首");
		starNodes.add("秀");
		starNodes.add("王思聪");
		starNodes.add("荧幕");*/
		TrieTree starTree = new TrieTree(starNodes);
		Map<String, List<Integer>> map=starTree.detect("小王");
	//	Map<String, List<Integer>> map=starTree.detect("王思聪献出荧幕首秀 弄好发型的老公帅气呀10元厕所");
		System.out.println("zxc");
		/*Set<String> starNodes = new HashSet<String>();
		starNodes.add("范冰冰");
		starNodes.add("李冰冰");
		starNodes.add("韩国{1}宣布");
		starNodes.add("李开{1}复");
		starNodes.add("韩国{10}宣布{5}疫情{5}结束");
		starNodes.add("韩国{3}宣布{5}疫情终止");
		starNodes.add("全面{8}两{8}孩{8}元旦{8}施行");
		starNodes.add("全面{8}两{8}孩{8}实施");
		

		String str = "韩国宣布疫情终止商学院 李开复 李开李冰冰,快讯:韩国卫生防疫部门宣布MERS 疫情结束,韩国宣布MERS疫情结束 提醒防范新型病毒\"入侵\" 范冰冰";
		Map<String,List<Integer>> matchMap = starTree.detect(str);
		
		
		if(matchMap != null && matchMap.size() > 0) {
				for(Entry<String,List<Integer>> entry2 :matchMap.entrySet()){
					System.out.print(entry2.getKey());
					System.out.print("--->");
					for(Integer s:entry2.getValue()){
						System.out.print(s+",");
					}
				
				System.out.print("\n");
			}
		} else {
			System.out.println("No result");
		}
		System.out.println("===============");
		String str2="“全面两孩”元旦开始施行，全面两孩将马上开始";
		matchMap=starTree.detectMaxMatched(str2,3);
		if(matchMap != null && matchMap.size() > 0) {
			for(Entry<String,List<Integer>> entry2 :matchMap.entrySet()){
				System.out.print(entry2.getKey());
				System.out.print("--->");
				for(Integer s:entry2.getValue()){
					System.out.print(s+",");
				}
			
			System.out.print("\n");
		}
	} else {
		System.out.println("No result");
	}
		System.out.println("zxc");
*/
	}
}
