package com.ifeng.iRecommend.featureEngineering;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CMPPDataOtherField {
	private String tags = null;
	private String bn = null;// 区块名称
	private String lt = null; // 列表页名称
	private String ref = null; // 跳转到该页的锚文字
	private boolean isSpecial = false; // 是否是专题
	private int upperClass = -1;
	private boolean isWeMedia = false; //是否是微信自媒体公众号文章
	private String source = null;
	private String loc=null;

	/**
	 * CMPP数据other字段解析
	 *
	 * @param other
	 *            例：source=spider|!|channel=finance|!|tags=凤凰网财经-财经资讯-行业|!|bn=头条
	 *            |!|ref=药监局多官员遭实名举报|!|lt=食药监总局多官员遭实名举报_财经频道_凤凰网
	 * 
	 *
	 */
	public CMPPDataOtherField(String other) {
		if (other == null || other.isEmpty())
			return;
		String[] fields = other.split("\\|!\\|");
		if (fields == null || fields.length <= 0)
			return;
		for (int i = 0; i < fields.length; i++) {
			String[] field = fields[i].split("=");
			if (field == null || field.length <= 0)
				continue;
			if (field[0].equals("tags")) {
				setTags(field[1]);
				String[] tags = field[1].split("-");
				String flag = null;
				if (tags.length > 1 && tags[0].contains("资讯"))
					flag = tags[1];
				else
					flag = tags[0];
				//这段代码，没有处理教育，但是我们的一级分类C，并没有教育
				
				if (flag.contains("大陆")
						|| flag.contains("国际") || flag.contains("台湾")
						|| flag.contains("港澳") || flag.contains("军事"))
					this.upperClass = 0;
				else if (flag.contains("娱乐"))
					this.upperClass = 1; // 娱乐
				else if (flag.contains("财经"))
					this.upperClass = 2; // 财经
				else if (flag.contains("时尚")){
					try{
						if(tags[1].contains("健康") || tags[1].contains("亲子"))
							this.upperClass = 11; //健康
					}
					catch (ArrayIndexOutOfBoundsException e){
						this.upperClass = 3;// 时尚
					}
				}
				else if (flag.contains("体育"))
					this.upperClass = 4;// 体育
				else if (flag.contains("数码"))
					this.upperClass = 5;
				else if (flag.contains("房产"))
					this.upperClass = 6;
				else if (flag.contains("汽车"))
					this.upperClass = 7;
				else if (flag.contains("科技"))
					this.upperClass = 8;
				else if (flag.contains("家居"))
					this.upperClass = 9;
				else if (flag.contains("教育"))
					this.upperClass = 10;
				else if (flag.contains("读书"))
					this.upperClass = 12;
				
			} else if (field[0].equals("bn")) {
				setBn(field[1]);
			} else if (field[0].equals("ref"))
				setRef(field[1]);
			else if (field[0].equals("lt"))
				setLt(field[1]);
			else if ((field[0].equals("referurl") || field[0].equals("ru"))
					&& field[1].contains("/special/"))
				setSpecial(true);
			else if(field[0].equals("source")){
				 if(field[1].toLowerCase().startsWith("wemedia"))
					setWeMedia(true);
				 setSource(field[1]);
			}
			else if(field[0].equals("classify")){
				setTags(field[1]);
			}
			else if (field[0].equals("loc")) {
				setLoc(field[1]);
			}
		}
		modifyFields();
		
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	private void cleanLt(){
		// System.out.print(getTags() + "\t" + field[1] + "\t");
		String temp = getLt();
		if(temp == null)
			return;
		String lt_head = null;
		if (temp != null && !temp.isEmpty()) {
			if (temp.equals("404-页面不存在") || temp.equals("404 Not Found") || temp.contains("平米") || temp.contains("㎡")
					|| temp.equals("兰台说史汇总页:_凤凰历史")) {
				setLt(null);
				return;
			}
//			temp = temp.replaceAll("(娱乐)?\\s*[_\\-—]+凤凰网?.*$", "").replaceAll(
//					"频道|新闻|抓取|专题|首页|资讯|·+", "");
			if(temp.matches(".*娱乐\\s*[_\\-—]+凤凰网?.*$")){
				temp = temp.replaceAll("娱乐\\s*[_\\-—]+凤凰网?.*$", "");
				if(!temp.startsWith("娱乐"))
					temp = "娱乐" + temp;
			}
			else{
				temp = temp.replaceAll("[_\\-—]+凤凰网?.*$", "");
			}
			if (temp.contains("_"))
				lt_head = temp.split("\\s*_\\s*")[0];
			else if (temp.contains("-"))
				lt_head = temp.split("\\s*-\\s*")[0];// 用-分隔的lt与other字段中tags构成
			else if (temp.contains("–"))
				lt_head = temp.split("\\s*–\\s*")[0];
			else
				lt_head = temp;
		}
		if(lt_head == null || lt_head.length() >= 20){
			setLt(null);
			return;
		}

		if (this.tags != null && !this.tags.contains(lt_head) || this.tags == null) {
			lt_head = lt_head.replaceAll("^凤凰(网)?(国际)?", "").replaceAll(
					"频道|新闻|抓取|专题|首页|资讯|官方网站|·+", "");
			// System.out.print("2 ===" + getLt() + "\t");
		}

		if (lt_head.isEmpty() || lt_head.contains("资讯")
				|| lt_head.contains("图片") || lt_head.equals("体育")
				|| lt_head.equals("娱乐") || lt_head.equals("时尚")
				|| lt_head.equals("教育") || lt_head.equals("财经")
				|| lt_head.equals("科技") || lt_head.equals("公益")
				|| lt_head.equals("最新报道") || lt_head.equals("新闻")
				|| lt_head.equals("综合体育") || lt_head.equals("凤凰汽车")
				|| lt_head.equals("原创") || lt_head.equals("凤凰")
				|| lt_head.contains("诸强") || lt_head.contains("诸强")
				|| lt_head.contains("列表") || lt_head.equals("数据") || lt_head.equals("传媒")  || lt_head.contains("面积") || lt_head.equals("评论")) {
			setLt(null);
			// System.out.print("3 ===" + getLt() + "\t");
		} else {
			setLt(lt_head);
			// System.out.println("4 ===" + getLt());
		}
	}
	
	private void cleanRef(){
		String ref = getRef();
		if(ref == null || ref.isEmpty()){
			setRef(null);
			return;
		}
		ref = ref.replaceAll("[【】\\[\\]\"·•\\|\u00A0]+", "").trim();
		ref = ref.replaceAll("第?\\s*[0-9一二三四五六七八九〇]+\\s*期[：:]", "").trim();
		ref = ref.replaceAll("独家策划\\s*\\|","").trim();
		if (isInvalidString(ref)) {
			setRef(null);
			return;
		}
		else if(ref.matches(".*[:：\\-].*")){
			setRef(null);
			return;
		}

		String[] refSlides = ref.split("\\s+");
		
		if(refSlides.length <= 1){
			setRef(ref);
			return;
		}
		StringBuffer sb = new StringBuffer();
		int i = 0;
		if(refSlides[i].matches(".*[a-zA-Z0-9/\\\\]+$") || refSlides[i + 1].matches("^[a-zA-Z0-9/\\\\]+.*")){
			sb.append(refSlides[i]);
			sb.append(refSlides[i + 1]);
			i++;
		}
		while(i < refSlides.length -1){
			if(refSlides[i].matches(".*[a-zA-Z0-9/\\\\]+$") || refSlides[i + 1].matches("^[a-zA-Z0-9/\\\\]+.*")){
				sb.append(refSlides[i + 1]);
//				sb.append(refSlides[i + 1]);
				i++;
			}
			else 
				break;
		}
		
		String str = sb.toString();
		if(str == null || str.isEmpty()){
			setRef(null);
			return;
		}
		setRef(str);
	}
	
	private void cleanBn(){
		if(isSpecial){
			setBn(null); //若为专题，不使用bn 2015.06.17 暂定
		}
		else if(getTags() != null && (getTags().contains("羽球") || getTags().contains("乒乓")))
			setBn(null);
		else{
			String temp_bn = getBn();
			//清洗bn
			if (isInvalidString(temp_bn)) {
				setBn(null);
				return;
			}
			if(temp_bn.equals("赛事报道") && getUpperClass() == 2){
				setBn(null);
				setRef(null);
				setLt(null);
				return;
			}
			if(getTags() != null && getTags().toLowerCase().contains(temp_bn.toLowerCase())){
				setBn(null);
				return;
			}
			temp_bn = temp_bn.replaceAll("[·•\\|\u00A0\\s:：\\[\\]]+", "");
			if(temp_bn.matches("[0-9\\-:/]+") || temp_bn.matches("[0-9]+月[0-9]+日")){
				setRef(null);
				setLt(null); //bn 为时间表示时ref与lt都不可用
				setBn(null);
				return;
			}
			
			if((lt = getLt()) != null && lt.toLowerCase().contains(temp_bn.toLowerCase())){
				setBn(null);
				return;
			}
			//特殊繁体处理
			int[] tranditionalHash = {"防務觀察".hashCode(), "環球軍情".hashCode(),"鄰邦掃描".hashCode(),"臺海風雲".hashCode(),"中國軍情".hashCode()};
			String [] simplified = {"防务观察","环球军情","邻邦扫描","台海风云","中国军情"};
			int code = temp_bn.hashCode();
			for(int i = 0; i < tranditionalHash.length; i++){
				if(code == tranditionalHash[i])
					temp_bn = simplified[i];
			}
//			if(temp_bn.length() > 5 || temp_bn.matches("[a-zA-Z0-9]+")){
//				setBn(null);
//				return;
//			}
			setBn(temp_bn);
		}
	}
	/**
	 * 对已经从other字段获取到的bn ref等用规则进行修正
	 */
	private void modifyFields(){
		if(getSource() !=null && getSource().equals("yidianzixun")){
			return;
		}
		cleanBn();
		String ref = getRef();
		String lt = getLt();
		if (ref != null && (ref.equals("root")
				|| (ref.contains("更多") && getUpperClass() == 7))) {// ref为以上时，lt不可用
																	// （观察数据得出）
			setLt(null);
			setRef(null);
		}
		else {// 获取lt头部
			cleanLt();
			cleanRef();
			ref = getRef();
			lt = getLt();
			if (ref != null && lt != null
					&& (ref.toLowerCase().contains(lt.toLowerCase()) || lt.toLowerCase().contains(ref.toLowerCase()))) // ref与lt互相包含以lt为准
				setRef(null);
		}
		if (getUpperClass() == 6)
			setLt(null);
		

	}
	
	private boolean isInvalidString(String str) {
		if (str == null || str.isEmpty())
			return true;
//		char[] a ;
//		a = str.toCharArray();
//		for(int i = 0; i < a.length; i++){
//			System.out.print(Integer.valueOf(Integer.valueOf(a[i])) + " ");
//		}
//		
//		System.out.println(str);
		if (str.contains("专题") || str.contains("更多") || str.contains("详细")
				|| str.contains("最新") || str.contains("下一页")
				|| str.contains("末页") || str.contains("首页")
				|| str.contains("首頁") || str.contains("全部")
				|| str.matches("^\\s+$") || str.contains("导读")
				|| str.contains("新闻") || str.contains("要闻")
				|| str.contains("凤凰网") || str.equals("资讯")
				|| str.contains("NEWS") || str.equals("原创")
				|| str.contains("头条") || str.contains("综合")
				|| str.contains("热点") || str.equals("精品栏目")
				|| str.contains("今日") || str.contains("聚焦")
				|| str.contains("图片") || str.contains("大图")
				|| str.contains("图集") || str.contains("相关")
				|| str.contains("诸强") || str.contains("列表") || str.equals("数据")
				|| str.equals("动态") || str.equals("首发") || str.equals("前言")
				|| str.equals("换一换") || str.equals("时间") || str.contains("推荐")
				|| str.contains("评论") || str.equals("特写") || str.equals("深度")
				|| str.contains("本地") || str.equals("凤凰观察") || str.equals("传媒")
				|| str.equals("公司") || str.contains("平米") || str.contains("㎡") 
				|| str.equalsIgnoreCase("more") || str.contains("滚动") || str.contains("作者"))
			return true;
		return false;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getBn() {
		return bn;
	}

	public void setBn(String bn) {
		this.bn = bn;
	}

	public String getLt() {
		return lt;
	}

	public void setLt(String lt) {
		this.lt = lt;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public boolean isSpecial() {
		return isSpecial;
	}

	public void setSpecial(boolean isSpecial) {
		this.isSpecial = isSpecial;
	}

	public int getUpperClass() {
		return upperClass;
	}

	public void setUpperClass(int upperClass) {
		this.upperClass = upperClass;
	}
	public boolean isWeMedia() {
		return isWeMedia;
	}
	public void setWeMedia(boolean isWeMedia) {
		this.isWeMedia = isWeMedia;
	}
	public String getLoc() {
		return loc;
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}
}
