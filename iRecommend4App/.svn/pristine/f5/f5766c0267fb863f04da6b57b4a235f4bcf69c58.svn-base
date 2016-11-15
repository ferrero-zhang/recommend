package com.ifeng.iRecommend.featureEngineering.DetectExtrmSim;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ifeng.iRecommend.featureEngineering.dataStructure.itemf;
import com.ifeng.iRecommend.featureEngineering.databaseOperation.IKVOperationv2;
import com.ifeng.ikvlite.IkvLiteClient;


public class DetectExtrmSimDocTest {
//	static IndexOperation ob = null;
	static String tablename = "appitemdb";
	static IKVOperationv2 ikvop = null;
	@BeforeClass
	public static void setUp() throws Exception {
//		ob = IndexOperation.getInstance();
		ikvop = new IKVOperationv2(tablename);
	}

	@Test
	public void testGetSimForTarget(){
//		ArrayList<itemf> collection = null;
		itemf item = new itemf();
		item.setID("test");
		Map<itemf, Double> scores = DetectExtrmSimDoc.getSimForTarget(item, false);
		assertNull(scores);
		
		itemf item1  = null;
//		ArrayList<itemf> collection1 = ob.queryFromSolr(item1);
		scores = DetectExtrmSimDoc.getSimForTarget(item1, false);
		assertNull(scores);
		
		item.setSplitContent("\n");
		scores = DetectExtrmSimDoc.getSimForTarget(item, false);
		assertNull(scores);
		
		
		itemf item2 = new itemf();
		item2.setID("TEST");
		item2.setTitle("外籍黑人在广西被拘 每天全身涂抹润身油避暑");
		item2.setSplitTitle("外籍_n 黑人_x 在_p 广西_x 被拘_x  _w 每天_r 全身_n 涂抹_v 润_k 身_k 油_k 避暑_v");
		String feature[] = {"柳州公安","s","1","广西","et","1.0","柳州市","et","0.1","柳州","et","0.1","公安局","et","0.1","伊斯兰教","et","0.1","南宁","et","0.1","宗教","et","0.1","公安","et","0.1","教育","et","0.1","黑人","x","-0.5","被拘","x","-0.5","拘留所","x","-0.1","民警","x","-0.1","权利义务","x","-0.1","行政拘留","x","-0.1","人性化","x","-0.1","中国警察","x","-0.1","猪肉","x","-0.1","健身","x","-0.1","恐惧","x","-0.1","领事馆","x","-0.1","种族歧视","x","-0.1","食品","x","-0.1","穆斯林","x","-0.1","饮食习惯","x","-0.1","出入境管理","nz","-0.1","外籍","n","-0.1","全身","n","-0.1","普通话","n","-0.1","种族","n","-0.1","心理","n","-0.1","支队","n","-0.1","歧视性","n","-0.1","文明","n","-0.1","英文版","n","-0.1"};
		item2.setFeatures(new ArrayList<String>(Arrays.asList(feature)));
		item2.setSplitContent("此次_mq 降低_v 12项_mq 收费_k 标准_n ，_w 可_k 减轻_v 企业_x 和_c 居民_n 负担_k 约_k 40亿元_mq 。_w \n_w 昨天_t ，_w 国家发改委_x 发布_v 消息_n 称_k ，_w 自_k 今年_t 10月_t 15日_t 起_k ，_w 决定_k 降低_v 住房_n 转让_v 手续费_x 等_u 12项_mq 行政_h 事业性_b 收费_v 标准_n ，_w 并_h 延长_x 专利_x 年费_x 减缴_v 时限_n 。_w 据_h 介绍_v ，_w 此次_mq 降低_v 12项_mq 收费_k 标准_n ，_w 可_k 减轻_v 企业_x 和_c 居民_n 负担_k 约_k 40亿元_mq 。_w \n_w 据_h 了解_v ，_w 国家_n 发展_v 改革_x 委_k 、_w 财政部_x 已_h 于_h 日前_t 印发_v 了_u 《_w 关于_p 降低_v 房屋_n 转让_v 手续费_x 受理_v 商标_x 注册费_x 等_u 部分_n 行政_v 事业性_b 收费_v 标准_n 的_u 通知_k 》_w 。_w 《_w 通知_k 》_w 规定_k ，_w 自_k 10月_t 15日_t 起_k ，_w 降低_v 涉及_v 住房_n 和_c 城乡建设_nz 、_w 工商_n 、_w 农业_x 、_w 民航_n 、_w 新闻出版_nz 广电_k 、_w 林业_n 等_u 6个_mq 部门_n 12项_mq 行政_h 事业性_b 收费_v 标准_n 。_w \n_w 这_r 12项_mq 行政_h 事业性_b 收费_v 标准_n 包括_v 住房_n 和_c 城乡建设_nz 部门_n 的_u 房屋_n 转让_v 手续费_x ;_w 工商_n 部门_n 的_u 受理_v 商标_x 注册费_x ;_w 农业部门_nz 的_u 植物_x 新品种_n 保护_v 权_n 费_k 等_u ;_w 民航_n 部门_n 的_u 民用_b 航空器_e 国籍_n 登记费_n 、_w 民用_b 航空器_e 权利_n 登记费_n ;_w 新闻出版_nz 广电_k 部门_n 的_u 软件_x 著作权_x 登记_v 申请_v 费_n 和_c 软件_x 著作权_x 登记_v 证书_n 工本费_n ;_w 林业_n 部门_n 的_u 植物_x 新品种_n 保护_v 权_n 收费_v 等_u 。_w \n_w 《_w 通知_k 》_w 明确_h ，_w 新建_x 商品住房_x 转让_v 手续费_x ，_w 由_p 现行_b 每_r 平方米_q 3元_mq 降_k 为_v 每_r 平方米_q 2元_mq ，_w 存量_n 住房_n 由_p 现行_b 每_r 平方米_q 6元_mq 降_k 为_v 每_r 平方米_q 4元_mq 。_w 各_h 省级_n 价格_n 、_w 财政_x 部门_n 可_v 根据_v 当地_s 住房_n 转让_v 服务_v 成本_x 、_w 房地产市场_x 供求_n 状况_n 、_w 房价_x 水平_n 、_w 居民_n 承受_v 能力_n 等_u 因素_n ，_w 进一步_d 适当_a 降低_v 中小城市_x 住房_n 转让_v 手续费_x 标准_n ，_w 减轻_v 居民_n 购房_x 费用_n 负担_k 。_w \n_w 国家_n 发展_v 改革_x 委_k 同时_k 要求_k ，_w 各_h 地区_n 和_c 有关_v 部门_n 对_p 公布_v 降低_v 的_u 行政_v 事业性_b 收费_v 标准_n ，_w 不得_u 以_p 任何_r 理由_n 拖延_v 或者_d 拒绝_v 执行_x 。_w 各级_r 价格_n 、_w 财政_x 部门_n 要_v 加强_v 对_p 《_w 通知_k 》_w 落实_v 情况_n 的_u 监督_k 检查_h ，_w 对_p 不_h 按_v 规定_v 降低_v 收费_v 标准_n 的_u ，_w 将_d 按_v 有关_v 规定_v 给予_v 处罚_v 。_w \n");
		long start = System.currentTimeMillis();
//		ArrayList<itemf> collection2 = ob.queryFromSolr(item2);
		scores = DetectExtrmSimDoc.getSimForTarget(item2, false);
		
		long end = System.currentTimeMillis();

//		assertEquals(collection2.size(), scores.size());
		System.out.println(end - start);
		
		
//		itemf item3 = ikvop.queryItemF("2169650", "c");
//		scores = DetectExtrmSimDoc.getSimForTarget(item3);
//		
//		for(Entry<itemf, Double> entry:scores.entrySet()){
//			System.out.println(entry.getKey().getID() + "xxx" + entry.getValue()); 
//		}
		
		itemf item4 = ikvop.queryItemF("4516042", "c");
		scores = DetectExtrmSimDoc.getSimForTarget(item4, false);
		
		for(Entry<itemf, Double> entry:scores.entrySet()){
			System.out.println(entry.getKey().getID() + "xxx" + entry.getValue()); 
		}
	}
	
	@Ignore
	public void testMakeChuckForDoc(){
		
		itemf item =  ikvop.queryItemF("3529095", "c");//目标itemf
		Set<Long> chunk = DetectExtrmSimDoc.makeHashCodeForDoc(item);
		System.out.println(chunk);
		
		itemf item1 =  ikvop.queryItemF("106278229", "x");//目标itemf
		chunk = DetectExtrmSimDoc.makeHashCodeForDoc(item1);
		System.out.println(chunk);
		
		/*BufferedReader rd = null;
		FileWriter resultWriter = null;
		try {
			rd = new BufferedReader(new InputStreamReader(new FileInputStream(
					"D:\\novaildid"), "UTF8"));
			resultWriter = new FileWriter("D:\\result1",
					false);
			String line = null;
			while ((line = rd.readLine()) != null && !line.isEmpty()) {
				item =  query.queryItemF(line);//目标itemf
				chunk = DetectExtrmSimDoc.makeChuckForDoc(item);
				
				if(chunk == null){
					resultWriter.write(line + "\n");
					resultWriter.write(item.getSplitContent() + "\n");
					resultWriter.flush();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (rd != null) {
				try {
					rd.close();
					resultWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}*/
//		itemf item2 = new itemf();
//		item2.setID("TEST");
//		item2.setTitle("原标题：住房转让手续费10月中旬下调");
//		item2.setSplitTitle("原_k 标题_n ：_w 住房_n 转让_v 手续费_x 10月_t 中旬_t 下调_v");
//		String feature[] = {"手续费", "x", "-1.0", "住房", "n", "-0.1", "著作权", "x", "-0.1", "航空器", "ne", "-0.1", "新品种", "n", "-0.1", "城乡建设", "nz", "-0.1", "财政部", "et", "0.1", "新闻出版", "nz", "-0.1", "居民", "n", "-0.1", "延长", "et", "0.1", "新建", "et", "0.1", "注册费", "x", "-0.1", "商标", "x", "-0.1", "标题", "n", "-0.1", "登记费", "n", "-0.1", "林业", "n", "-0.1", "植物", "x", "-0.1", "民航", "n", "-0.1", "部门", "n", "-0.1", "标准", "n", "-0.1", "工商", "n", "-0.1", "中小城市", "x", "-0.1", "财政", "x", "-0.1", "商品住房", "x", "-0.1", "软件", "x", "-0.1", "房屋", "n", "-0.1", "年费", "x", "-0.1", "国家发改委", "x", "-0.1", "房地产市场", "x", "-0.1", "农业部门", "nz", "-0.1", "工本费", "n", "-0.1", "时限", "n", "-0.1", "国籍", "n", "-0.1", "专利", "x", "-0.1", "供求", "n", "-0.1", "证书", "n", "-0.1", "购房", "x", "-0.1"};
//		item2.setFeatures(new ArrayList<String>(Arrays.asList(feature)));
//		item2.setSplitContent("此次_mq 降低_v 12项_mq 收费_k 标准_n ，_w 可_k 减轻_v 企业_x 和_c 居民_n 负担_k 约_k 40亿元_mq 。_w \n_w 昨天_t ，_w 国家发改委_x 发布_v 消息_n 称_k ，_w 自_k 今年_t 10月_t 15日_t 起_k ，_w 决定_k 降低_v 住房_n 转让_v 手续费_x 等_u 12项_mq 行政_h 事业性_b 收费_v 标准_n ，_w 并_h 延长_x 专利_x 年费_x 减缴_v 时限_n 。_w 据_h 介绍_v ，_w 此次_mq 降低_v 12项_mq 收费_k 标准_n ，_w 可_k 减轻_v 企业_x 和_c 居民_n 负担_k 约_k 40亿元_mq 。_w \n_w 据_h 了解_v ，_w 国家_n 发展_v 改革_x 委_k 、_w 财政部_x 已_h 于_h 日前_t 印发_v 了_u 《_w 关于_p 降低_v 房屋_n 转让_v 手续费_x 受理_v 商标_x 注册费_x 等_u 部分_n 行政_v 事业性_b 收费_v 标准_n 的_u 通知_k 》_w 。_w 《_w 通知_k 》_w 规定_k ，_w 自_k 10月_t 15日_t 起_k ，_w 降低_v 涉及_v 住房_n 和_c 城乡建设_nz 、_w 工商_n 、_w 农业_x 、_w 民航_n 、_w 新闻出版_nz 广电_k 、_w 林业_n 等_u 6个_mq 部门_n 12项_mq 行政_h 事业性_b 收费_v 标准_n 。_w \n_w 这_r 12项_mq 行政_h 事业性_b 收费_v 标准_n 包括_v 住房_n 和_c 城乡建设_nz 部门_n 的_u 房屋_n 转让_v 手续费_x ;_w 工商_n 部门_n 的_u 受理_v 商标_x 注册费_x ;_w 农业部门_nz 的_u 植物_x 新品种_n 保护_v 权_n 费_k 等_u ;_w 民航_n 部门_n 的_u 民用_b 航空器_e 国籍_n 登记费_n 、_w 民用_b 航空器_e 权利_n 登记费_n ;_w 新闻出版_nz 广电_k 部门_n 的_u 软件_x 著作权_x 登记_v 申请_v 费_n 和_c 软件_x 著作权_x 登记_v 证书_n 工本费_n ;_w 林业_n 部门_n 的_u 植物_x 新品种_n 保护_v 权_n 收费_v 等_u 。_w \n_w 《_w 通知_k 》_w 明确_h ，_w 新建_x 商品住房_x 转让_v 手续费_x ，_w 由_p 现行_b 每_r 平方米_q 3元_mq 降_k 为_v 每_r 平方米_q 2元_mq ，_w 存量_n 住房_n 由_p 现行_b 每_r 平方米_q 6元_mq 降_k 为_v 每_r 平方米_q 4元_mq 。_w 各_h 省级_n 价格_n 、_w 财政_x 部门_n 可_v 根据_v 当地_s 住房_n 转让_v 服务_v 成本_x 、_w 房地产市场_x 供求_n 状况_n 、_w 房价_x 水平_n 、_w 居民_n 承受_v 能力_n 等_u 因素_n ，_w 进一步_d 适当_a 降低_v 中小城市_x 住房_n 转让_v 手续费_x 标准_n ，_w 减轻_v 居民_n 购房_x 费用_n 负担_k 。_w \n_w 国家_n 发展_v 改革_x 委_k 同时_k 要求_k ，_w 各_h 地区_n 和_c 有关_v 部门_n 对_p 公布_v 降低_v 的_u 行政_v 事业性_b 收费_v 标准_n ，_w 不得_u 以_p 任何_r 理由_n 拖延_v 或者_d 拒绝_v 执行_x 。_w 各级_r 价格_n 、_w 财政_x 部门_n 要_v 加强_v 对_p 《_w 通知_k 》_w 落实_v 情况_n 的_u 监督_k 检查_h ，_w 对_p 不_h 按_v 规定_v 降低_v 收费_v 标准_n 的_u ，_w 将_d 按_v 有关_v 规定_v 给予_v 处罚_v 。_w \n");
		
//		ArrayList<String> chunk = DetectExtrmSimDoc.makeChuckForDoc(item2);
//		System.out.println(chunk);
//		itemf testItem = new itemf();
//		testItem.setID("test");
//		testItem.setDocType("self");
//		testItem.setTitle("《夜行书生》今天的新闻图");
//		testItem.setSplitTitle("《_w 夜行_v 书生_k 》_w 今天_t 的_u 新闻_n 图_k");
//		testItem.setSplitContent("\n_w 广州日报_x 讯_k  _w （_w 记者_n  _w 李斌_nr ）_w  _w 广州恒大淘宝_x 在_p 夏季_t 转会_k 窗_n 关闭_v 前_f 签_v 下_f 罗比尼奥_x ，_w 但_k 这个_h 消息_n 对_p 目前_t 的_u 球队_x 没有_v 任何_r 帮助_v ，_w 斯科拉里_x 依然_v 只能_d 带着_v 残缺_v 阵容_n 远_k 赴_k 辽宁_x 。_w 今晚_t 7时_t 35分_t ，_w 广州恒大淘宝_x 将_d 在_p 辽宁_x 盘锦_x 奥体_nz 中心_k 对阵_v 辽宁宏运_x 。_w \n\n_w 　_w 　_w 辽宁宏运_x 目前_t 在_p 积分榜_n 的_u 排名_x 不_h 佳_a ，_w 身处_v 降级_v 区_k 边缘_s 。_w 然而_c 这_r 并_d 不是_n 一支_mq 被_p 忽视_v 的_u 对手_n ，_w 辽宁宏运_x 在_p 二次_mq 转会_k 市场_x 引进_v 了_u 罗马尼亚_x 射手_n 埃里克_ns ，_w 同时_k 引进_v 的_u 还有_v 此前_t 效力_h 上海申花_x 的_u 保罗·恩里克_x ，_w 后者_n 在_p 中超联赛_nz 已_h 证明_k 了_u 自己_h 的_u 能力_n 。_w 辽宁宏运_x 在_p 本_r 赛季_n 首_k 次_q 遭遇_h 广州恒大淘宝_x 时_n ，_w 在_p 天河_n 体育场_n 被_p 卡纳瓦罗_x 的_u 球队_x 打出_v 6_mq 比_k 1_mq 的_u 高分_n ，_w 但_k 时过境迁_l ，_w 如今_t 辽宁宏运_x 有着_v 完成_v 保级_v 任务_n 的_u 决心_h ，_w 也_d 能_v 反省_v 此前_t 的_u 战术_n 失误_v ，_w 他们_r 在_p 主场_n 必然_n 将_d 防守_x 反击_x 打_k 得_u 更加_d 坚决_a 。_w \n\n_w 　_w 　_w 斯科拉里_x 面临_v 的_u 依然_v 是_v 无_v 人_r 可用_v 的_u 局面_n ，_w 高拉特_x 在_p 下周_t 才_n 能_v 复出_v ，_w 埃尔克森_x 的_u 伤情_n 可能_v 延续_v 到_v 月底_t 。_w 唯一_b 值得_h 庆幸_a 的_u 是_v ，_w 在_p 经过_v 上_v 轮_v 的_u 临时_d 登场_v 后_f ，_w 保利尼奥_x 的_u 状态_n 看起来_d 可以_v 完成_v 90分钟_mq 的_u 比赛_k ，_w 如今_t 保利尼奥_x 和_c 金英权_x 是_v 斯科拉里_x 在_p 阵容_n 中_n 能_v 使用_v 的_u 外援_n 。_w 而_h 国内_s 球员_n 方面_n ，_w 郑智_x 上_f 轮_v 的_u 红牌_x 使_v 其_u 缺席_v ，_w 郜林_x 从_p 伤病_n 中_n 走出_v 可以_v 一战_x 。_w 广州恒大淘宝_x 在_p 本场_r 比赛_v 的_u 进攻_v 线_k ，_w 依然_v 仰仗_v 于汉超_x 、_w 郑龙_x 与_p 郜林_x 三_m 人_h 。_w “_w 我_r 还_h 没_h 看到_v 罗比尼奥_x ，_w 现在_t 的_u 球员_n 是_v 郜林_x 、_w 黄博文_x 、_w 于汉超_x 等_u 。_w 埃尔克森_x 、_w 张琳芃_x 、_w 廖力生_x 这些_r 球员_n 赶快_d 复出_v ，_w 这_r 是_v 我_r 现在_t 要_v 考虑_v 的_u 。_w ”_w 斯科拉里_x 说_v 。_w 去_h 东北_k 比赛_v 对于_p 斯科拉里_x 而言_u 并_d 没有_v 太_d 大_k 问题_n ，_w 尽管_h 他_r 和_c 球队_x 都_h 乘坐_v 大巴_e 从_p 沈阳_x 前往_v 盘锦_x 。_w 面对_v 保级_v 动力_n 十足_h 的_u 对手_n ，_w 斯科拉里_x 说_v ：_w “_w 我们_r 打_v 任何_r 对手_n 都_n 没有_v 压力_n ，_w 我们_r 需要_v 做_v 的_u 是_v 拿下_v 明天_t 的_u 比赛_k 。_w ”_w \n\n_w 　_w 　_w 罗比_nr 踢_v 亚冠_x  _w 并非_v 没_v 机会_n \n\n_w 　_w 　_w 值得一提的是_nz 罗比尼奥_x ，_w 巴西人_n 目前_t 还_h 未_k 抵达_v 广州_x ，_w 但_k 他_r 的_u 签约_v 有着_v 新_n 的_u 说法_k 。_w 有_v 消息_n 称_k 罗比尼奥_x 的_u 合同_n 中_n 有_v 条款_n 说明_h ，_w 这位_mq 巴西_x 球员_n 只能_d 征战_v 中超_x 而_h 无法_d 登陆_v 亚冠_x 。_w 但_k 记者_n 从_p 相关_v 渠道_n 了解_v 到_v ，_w 这个_h 合同_n 条款_n 的_u 存在_k 仅_k 是_v 规避_v 一些_n 风险_n 。_w “_w 在_p 谈判_v 的_u 时候_n 提出_v 过_u 这个_h 想法_n ，_w 就是_k ‘_w 可能_h 让_v 罗比尼奥_x 只_k 踢_v 中超_x ’_w ，_w 希望_k 罗比尼奥_x 能_v 接受_v 这个_h 条件_n 。_w 罗比尼奥_x 最终_b 答应_v 了_u 。_w 这个_h 条件_n 虽然_c 看起来_d 有些_h 苛刻_k ，_w 但_k 却_k 并_h 不是_n 说_v 他_r 只能_d 踢_v 中超_x 。_w ”_w \n\n_w 　_w 　_w 知情人士_nz 表示_k 说_v ，_w “_w 条款_n 的_u 存在_k 是_v 一种_mq 安抚_v ，_w 因为_c 球队_x 肯定_k 只有_c 三名_mq 非_h 亚_h 外援_n 报名_v 亚冠_x 。_w 按照_v 罗比尼奥_x 的_u 身价_n 和_c 名气_n ，_w 如果_c 他_r 没_v 法_v 报名_v 亚冠_x ，_w 可能_h 会_v 引发_v 一些_h 麻烦_k 。_w 所以_c 就是_k 希望_v 他_r 能够_v 理解_v ‘_w 可能_h ’_w 会_v 出现_v 的_u 情况_n ，_w 而_h 不_h 会_v 因此_c 产生_v 不满_v 。_w ”_w \n\n_w 　_w 　_w 记者_n 了解_v 到_v ，_w 对_p 罗比尼奥_x “_w 可能_h 不_h 打_k 亚冠_x ”_w 的_u 说明_v 只是_k 个_q “_w 提醒_v ”_w ，_w 以_p 防止_v 诸如_v 外援_n 对_p 落选_v 失望_a 并且_c 心_k 生_v 不满_v 的_u 情况_n 出现_v ，_w 这_r 在_p 广州恒大淘宝_x 此前_t 的_u 亚冠_x 经历_k 中_n 并_d 不是_n 没有_v 发生_v 过_v 的_u 事情_n ，_w 所以_c 对_p 罗比尼奥_x 的_u 提早_h 说明_h ，_w 就是_k 为了_p 规避_v 恶劣_a 后果_n 的_u 出现_v 。_w 实际上_d 广州恒大淘宝_x 亚冠_x 名单_n 选择_v 中_n 有着_v 高拉特_x 、_w 保利尼奥_x 、_w 埃尔克森_x 、_w 罗比尼奥_x 四_k 人_h ，_w 势必_d 有_v 一_m 人_h 会_v 落选_v 名单_n 。_w 俱乐部_x 也_d 不_h 可能_h 在_p 关键_n 的_u 时候_n 限制_v 罗比尼奥_x 参加_v 亚冠_x ，_w 而_h 给_k 其他_r 三名_mq 外援_n 吃_k 定心丸_n 。_w 如果_c 真_d 按照_v 传言_k 中_f 那样_mq 让_v 罗比尼奥_x 远离_v 亚冠_x ，_w 那么_k 一旦_k 在_p 8月份_t 有_v 一名_mq 外援_n 重伤_k 或者_h 状态_n 不_h 佳_a ，_w 那么_k 对_p 罗比尼奥_x 的_u 限制_v 岂不_d 是_v 搬_v 起_v 石头_x 砸_v 自己_r 的_u 脚_n 。_w 所以_c 四名_mq 外援_n 之间_f 的_u 名额_n 竞争_v 依然_v 存在_k ，_w 只是_k 他们_r 都_n 需要_nz 在_p 一个多_n 月_n 时间_n 里_q 用_v 自己_r 的_u 表现_k 去_h 说服_v 斯科拉里_x 。_w \n");
//		chunk = DetectExtrmSimDoc.makeChuckForDoc(testItem);
//		System.out.println(chunk);
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		StageExcutor.close();
		DetectExtrmSimDoc.close();
		ikvop.close();
	}
	
}
