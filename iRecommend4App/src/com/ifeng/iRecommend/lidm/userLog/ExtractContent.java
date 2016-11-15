package com.ifeng.iRecommend.lidm.userLog;

import java.util.ArrayList;
import java.util.List;

public class ExtractContent {
	private static String paragraphMark = "<br/>";
	private static String point = "。";

	public static void main(String[] args) {
		/*String content = "<small>戴德馨，中国路桥工程有限责任公司工作人员。</small>"
				+ "中非友谊日渐深厚，许多中国年轻人都活跃在当今的非洲大陆上。学习土木工程专业的戴德馨一年前大学毕业，进入中国路桥工程有限责任公司。去年8月31日，他到了肯尼亚。"
				+ "日前，在非洲工作刚满一年的他，向记者讲述了他的经历和体会。<br/> <strong>中国青年报</strong>：你平时的工作是怎样的呢？<br/> "
				+ "<strong>戴德馨</strong>：我参与的是我们公司在肯尼亚首都内罗毕的环城公路建设项目。我是现场工程师，职责主要是监督、指导当地工人。"
				+ "中国公司的工作人员在当地很少直接参与具体施工，只负责对项目和工人的管理。<br/> "
				+ "比如在工程设计方面，国内跟肯尼亚的标准肯定存在差异。遇到这种情况，我一般会先看施工图纸，然后再告诉当地工人应该怎样做，比如钢筋捆绑和模板的安置顺序、方法等，这样一两次之后他们自己可以操作了。<br/> "
				+ "肯尼亚以前是英国的殖民地，官方语言是英语。我们在指导当地工人工作时，直接用英语就能很方便地交流。<br/> <strong>中国青年报</strong>：当地工人的总体情况如何呢？<br/>"
				+ " <strong>戴德馨</strong>：当地工人主要分为两种，一种是有一定技能的技术工人，一些测量都可以交给这些当地的技术人员完成，他们的水平还是可以的；另外一种是只能做力气活儿的非技术工人，大部分当地工人干的活儿还是体力活。"
				+ "技术工人每个月能拿到的工资折合人民币是1600元~1700元人民币，体力工人的工资折合成人民币在1300元人民币左右。<br/> 当地种族较多，只有身在外国时，他们才会介绍自己为“肯尼亚人”。在肯尼亚国内，他们会首先认为自己是基库尤人、"
				+ "罗族人、库巴人等。部族内部之间的人往往在一起工作，部族之间的人在一起工作的情况也不少，虽然有一些芥蒂，但是并不会因此而耽误工程。<br/> "
				+ "肯尼亚人的生活节奏相当慢，他们平常在街上走路步伐都很慢。我们同事之间经常开玩笑说，在非洲做工程，“保持耐心”称不上美德，而是一种适应工作的基本法则和条件。我们非常理解他们这种文化氛围所决定的节奏。<br/> "
				+ "<strong>中国青年报</strong>：肯尼亚的贫富差距严重吗？<br/> <strong>戴德馨：</strong>肯尼亚这边的贫富差距是比较明显的。富人有的能开豪车、住豪宅，甚至有很多套房子。穷人的话，底层的农民、工人"
				+ "、贫民窟里的人较为卑微，看到我们经常会有目光上的躲闪。<br/> "
				+ "肯尼亚的就业率比较低，只有50%多一点。肯尼亚有东非最大的贫民窟，但也是东非最发达的国家，政府也在加大力气去解决贫富差距问题。比如这边的一个十万人以上贫民窟，里面所有的人都是用电免费，用水免费。政府还会修一些类似于中国国内公租房一样的住宅，"
				+ "用很低的价格租给穷人。肯尼亚的前景还是不错的。<br/> "
				+ "另外，政府也会引入像中国路桥这样的公司和相关项目，扩大就业。我们帮他们修路，不仅给他们带去了更多工作机会，同时也帮助他们进行了城市建设，把相对先进的技术以及管理方式带去了。<br/> "
				+ "<strong>中国青年报</strong>：在建设过程中，是否会遇到一些麻烦？<br/> <strong>戴德馨</strong>：我们公司的合作对象是肯尼亚政府，肯尼亚政府需要协助我们完成改造任务。比如，我们现在在修一条环城公路，"
				+ "这条路会经过一个居民区。虽然在选线的时候我会尽量避开这些居民区。但如果涉及拆迁，也会请肯尼亚政府帮我们和当地居民协调好以后，我们才会进场进行施工。<br/> 肯尼亚的土地并不是很贵，所以对政府给出的价格，土地所有者一般都能接受。<br/>";*/
		String content = "原_k 标题_n ：_w 纽约_ns 华裔_n 被_p 抢_v 逾_k 10部_mq iPhone6_nx  _w 手臂_n 中_f 两枪_mq 性命_n 无碍_v  _w 中_f 新_k 网_k 9月_t 28日_t 电_k  _w 据_h 美国_ns 《_w 侨报_n 》_w 报道_k ，_w 纽约_ns 法拉_nz 盛_k 109_mq 分局_n 辖区_n 内_k 25日_mq 晚间_t 发生_v 一起_k 枪击_v 案_k ，_w 伤者_n 24岁_mq 的_u 华裔_n 男子_n 杨_h 史_h 班_k 瑟_n (_w Spencer_nx  _w Yang_nx ，_w 音译_v )_w 因为_c 与_p 买_k 家_n 当面_d 交易_v 大批_b iPhone_nx  _w 6_mq 手机_e ，_w 却_k 不测_n 遭到_v 买家_n 开枪_v 抢劫_v ，_w 嫌犯_n 在_p 连_h 开_k 两枪_mq 后_f 夺走_v 至少_d 10部_mq 手机_e ，_w 得逞_v 后_f 跳_v 上_v 车辆_n ，_w 随即_d 逃逸_v 现场_n ，_w 警方_n 目前_t 循_v 线_q 追踪_v ，_w 呼吁_v 知情者_n 举报_v 。_w 华裔_n 手臂_n 中_f 两枪_mq ，_w 所幸_c 在_p 及时_a 逃跑_v 后_f 无_v 生命_n 危险_a 。_w  _w 案件_n 发生_v 在_p 当天_t 晚间_t 11时左右_mq 。_w 据_h 了解_v ，_w 杨男_nr 在_p 网站_n 上_v 与_p 买家_n 交涉_v ，_w 经过_k 交谈_v 后_f 约_d 在_p 当晚_t 交易_h ，_w 就_d 在_p 两_q 男_n 抵达_v 后_f ，_w 杨男_nr 让_v 两_h 人_h 进入_v 位_q 在_p 白石镇_ns 的_u 家中_s ，_w 不料_d 两_h 人_h 进入_v 后_f ，_w 却_k 突然_a 掏出_v 枪_k ，_w 一把_mq 夺走_v 杨男_nr 的_u 手机_e ，_w 并且_c 将_p 客厅_n 桌上_s 的_u 10多部_mq iPhone_nx  _w 6_mq 搜刮_v 一_m 空_k ，_w 在_p 得逞_v 后_f 往_v 14_mq 大道_n 近_k 159_mq 街_n 处_v 逃逸_v 。_w  _w 杨男_nr 眼见_d 手机_e 被_p 抢_v ，_w 随即_d 追_v 跑_v 出去_v ，_w 不料_d 在_p 159_mq 街_n 14-21_mq 号_k 前_f 时_n ，_w 对方_n 掏出_v 枪_k 向_p 杨男_nr 射击_v ，_w 杨男_nr 手臂_n 因此_c 中枪_nz 。_w 根据_k 一名_mq 邻居_n 表示_k ，_w 当时_t 她_r 在_p 屋内_s 听到_v 一_m 响声_n ，_w 在_p 20秒_mq 后_f 又_d 听到_v 第二声_mq ，_w 但_k 一_m 开始_v 并未_d 察觉_v 是_v 枪响_n ，_w 就_d 在_p 与_p 家人_n 踏_v 出_v 门外_s 后_f ，_w 才_h 发现_k 大批_b 警方_n 在_p 现场_n ，_w 上方有_nr 搜索_e 直升机_e 盘旋_v ，_w 搜查_v 嫌犯_n 行踪_n 。_w 该_h 邻居_n 亦_h 指出_v ，_w 杨男_nr 就_d 居住_v 在_p 事发_v 地_u 一旁_s 的_u 转角_n 宅院_n 里_h ，_w 和_c 父母_n 以及_c 疑似_v 还有_v 另_c 一个_mq 姐妹_n 居住_v 在内_v ，_w 杨男_nr 一家_mq 约_k 在_p 3年_mq 多_m 前_f 搬进_v 该_v 屋_n 。_w  _w 记者_n 走访_v 枪击_v 案_k 发生_v 地点_n ，_w 附近_k 邻居_n 表示_k ，_w 事发_v 后_f ，_w 警方_n 在_p 不到_v 5分钟_mq 的_u 时间_n 便_d 赶到_v 现场_n ，_w 当时_t 有_v 大批_b 警车_e 及_h 警员_n 于_u 现场_n 勘查_v 侦办_v ，_w 救护_v 人员_n 也_d 将_d 杨男_nr 送往_v 就近_d 医院_n 救治_v 。_w  _w 枪案_n 发生_v 附近_n 一带_n 的_u 住宅_n 皆_d 是_v 独_k 栋_k 独院_n ，_w 每户_r 住宅_n 的_u 前院_s 均_k 铺_k 植_k 花草_n 。_w 根据_k 现场_n 一名_mq 男子_n 表示_k ，_w 这_r 附近_k 的_u 治安_n 一向_k 很_d 好_k ，_w 对于_p 发生_v 这样_mq 的_u 案件_n 让_v 他_r 感到_v 相当_d 吃惊_a 。_w 针对_v 该案_r ，_w 他_r 表示_k 自家_r 住宅_n 外_f 的_u 监视器_e 也_d 已经_d 提供_v 警方_n 办案_v 搜_v 证_k 。_w  _w 据悉_v ，_w 当时_t 作案_v 的_u 两_q 嫌疑_n 为_v 西_n 裔_n 男子_n ，_w 年纪_n 约_k 20_mq 出头_v ，_w 其中_r 一_m 嫌_k 身穿_v 黑色_n 上衣_e ，_w 头_h 戴_h 棒球帽_nz ，_w 另_c 一_m 嫌_k 身穿_v 深色_n T恤_e 。_w 警方_n 呼吁_v 知情者_n 提供_v 线索_n ，_w 协助_v 警方_n 办案_v 。_w ";
		String s=extract(content);
		System.out.println(s);
		
	}

	public static String extract(String content) {
		if(content == null || content.equals("")){
			return "";
		}
		// 获取每一句开头和结尾的字
		List<String> words = new ArrayList<String>();
		List<String> wordfromFirstSentence = getWordsFromFirstSentence(content);
		if (wordfromFirstSentence.size()>0) {
			words.addAll( wordfromFirstSentence);		
		}
				
		String str=words.toString().replace(",", "");		
		return str.substring(1,str.length()-1);
	}
	
	public static String specialCharactersFilter(String paragraph){
		//过滤特殊字符及空格
		paragraph = spaceFilter(paragraph.replaceAll("[\\pP|~|$|^|<|>|\\||\\+|=]*", ""));
		return paragraph;
	}
	
	public static String spaceFilter(String paragraph){
		//去除全角和半角空格
		paragraph=paragraph.replaceAll("[\\s\\p{Zs}]+","");
		return paragraph;
	}

	
	public static List<String> getWordsFromFirstSentence(String paragraph) {
		List<String> words = new ArrayList<String>();

		String[] sentences=paragraph.split(point);
		
		
		for(int i=0;i<sentences.length;i++){
			String sentence=sentences[i];
			//System.out.println(sentence);
			/*if((sentences.length-2)>7 && i == 7){
				i = sentences.length -2;
				continue;
			}*/
			sentence=sentence.replaceAll("_\\w+\\s", "");
			//System.out.println(sentence);
			int len=sentence.length();
			if(len>0){
				if((sentence.substring(0,1))!=null && !(sentence.substring(0,1)).equals(" ")){
					words.add(sentence.substring(0,1));
				}
				if((sentence.substring(len-1,len))!=null && !(sentence.substring(len-1,len)).equals(" ")){
					words.add(sentence.substring(len-1,len));
				}
				
			}
		}		

		return words;
	}

	public static void getWordsFromParagraph(String paragraph,List<String> words) {
		
		if(paragraph.isEmpty()){
			return;
		}
		
		paragraph=specialCharactersFilter(paragraph);
		
		String start = paragraph.substring(0, 1);

		int index = paragraph.indexOf(point);
		String end = "";
		if (index > 0) {
			end = paragraph.substring(index - 1, index);
		} else {
			end = paragraph.substring(paragraph.length() - 1);
		}

		words.add(start);
		words.add(end);
	}

}
