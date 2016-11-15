package com.ifeng.iRecommend.featureEngineering.LayerGraph;
public class NodeData {
	private String word=null;
	private String type=null;	//词的类型c/sc/cn等
	private String typeLabel=null; // 用来区分c0,c1
	
	public NodeData(){
		
	}
	public NodeData(String word)
	{
		this.word = word;
	}
	public NodeData(String word,String type)
	{
		this.word = word;
		this.type=type;
	}
	public NodeData(String word,String type,String typeLabel){
		this.word = word;
		this.type = type;
		this.typeLabel=typeLabel;
	}
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	public String getTypeLabel() {
		return typeLabel;
	}

	public void setTypeLable(String typeLabel) {
		this.typeLabel = typeLabel;
	}
	// map中用自定义的NodeData作为key，需要重载equals和hashCode函数
	@Override
	public boolean equals(Object o){
		return (o instanceof NodeData) && 
			(word==null && ((NodeData)o).word==null||
			word.equals(((NodeData)o).word)) &&
			(type==null && ((NodeData)o).type==null||
			type.equals(((NodeData)o).type)) &&
			(typeLabel==null && ((NodeData)o).typeLabel==null||
			typeLabel.equals(((NodeData)o).typeLabel));
	}
	@Override
	public int hashCode(){
		return (word+type+typeLabel).hashCode();
	}
}
