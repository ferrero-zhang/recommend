package com.ifeng.iRecommend.featureEngineering;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class KeywordValueJudgeTest
{
	KeywordValueJudge kvJudge = KeywordValueJudge.getKVJudgeInstance();
	@Before
	public void setUp() throws Exception
	{
	}

	@Test
	public void test()
	{
		String s_title = "外交部_x ：_w 坚决_a 反对_v 蔡英文_x 赴_k 日_n 活动_v";
		String s_content = " 新华社_x 9月_t 25日_t 消息_n 据_h 报道_k ，_w 蔡英文_x 将_d 于_h 10月_t 上旬_t 访问_v 日本_x 。_w 外交部发言人_x 洪磊_x 25日_mq 表示_k ，_w 我们_r 对_p 蔡英文_x 将_d 赴_k 日本_x 活动_v 表示_k 严重_a 关切_v 和_c 坚决_a 反对_v ，_w 希望_k 日方_n 坚持_v 一个_mq 中国_x 原则_h ，_w 恪守_v 在_p 台湾问题_x 上_f 对_p 中方_x 所_u 做_v 承诺_v ，_w 不_h 给_p 任何人_r 以_p 任何_r 名义_n 或_d 借口_v 散布_v “_w 台独_x ”_w 言论_n 提供_v 空间_n 。_w (_w 记者_x 靳若成_nr )_w ";
		String category = "时政";
		List<String> list = kvJudge.getKeyword(s_title, s_content, category);
		System.out.println(list);
	}

}
