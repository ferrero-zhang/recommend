package com.ifeng.commen.classifyClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;

import com.ifeng.commen.Utils.LoadConfig;

/**
 * <PRE>
 * 作用 : 
 * 	分类请求的客户端代码
 * 使用 : 
 *   指定配置路径<code>LoadConfig.reLoadPath("conf/sys_pipeline.properties")</code> 
 *   
 * 示例 :
 *   
 * 注意 :
 * 	日志的问题，目前有两个方案，一个是通过静态引入，使用调用端的日志对象,此方法可以全局使用日志，但使用时需要修改import，用户不容易懂。
 * 	另个是通过传参数，不能全局调用，用户直接传进一个log对象既可以。
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2013-10-28        chendw          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class ClassifierClient {

//	private static Socket socket;
	
	private static String ip;				//ip地址
	
	private static int port;				//分词服务端口
	
	private static int timeOut;				//数据读取超时
	
//	private static DataInputStream dis;		//读流
//	
//	private static DataOutputStream dos;	//写流
	
	//类初始化需要加载的几个字段
	static{
		try{
			LoadConfig.reLoadPath("conf/sys_client.properties");
			String ips = LoadConfig.lookUpValueByKey("classifier_server_ip");
			int ports = Integer.valueOf(LoadConfig.lookUpValueByKey("classifier_server_port"));
			int timeouts = Integer.valueOf(LoadConfig.lookUpValueByKey("classifier_timeoutInMS"));
			ip = ips;
			port = ports;
			timeOut = timeouts;
		}catch(Exception e){
			e.printStackTrace();
			timeOut = 5000;
		}
	}
	
	/**
	 * 支持并发
	 * @param content
	 * @return
	 */
	public static String predict(String title, String content, String url,String className, Log logger){
		String response = "";
		Socket socket = null;
		DataInputStream dis = null;
		DataOutputStream dos = null;
		try {
			socket = new Socket(ip, port);
			socket.setSoTimeout(timeOut);
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			sendCmd(dos, title, content, className, url);
			response = dis.readUTF();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			if(logger!=null)
				logger.error("UnknownHostException", e);
			return "client.error1";
		}catch (UTFDataFormatException e) {
			//数据太长,写失败
			e.printStackTrace();
			if(logger!=null)
				logger.error("data too long", e);
			return "client.error2";
		}catch (IOException e) {
			e.printStackTrace();
			if(logger!=null)
				logger.error("IOException", e);
			return "client.error3";
		}catch (Exception e) {
			e.printStackTrace();
			if(logger!=null)
				logger.error("Exception", e);
			return "client.error4";
		}finally{
			try {
				if(dis!=null){
					dis.close();
				}
				if(dos!=null){
					dos.close();
				}
				if(socket!=null){
					socket.close();
				}
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
		return response;
	}
	
	/**
	 * 发送数据长度大于20000需要切割
	 * @param content
	 * @return
	 */
	private static String dataTrim(String content){
		if(content==null){
			return "";
		}
		int top = 20000;
		if(content.length()>top){
			return content.substring(0, top);
		}else{
			return content;
		}
	}
	
	
	/**
	 * 发送数据，定义了关键词服务的数据协议：false content true
	 * @param content
	 * @throws IOException 直接抛给上一级
	 */
	private static void sendCmd(DataOutputStream dos, String title, String content, String className, String url) throws IOException{
		dos.writeBoolean(false);

//		if(content.length()>40000){
//			for (int i = 1; i < content.length()/40000+2; i++) {
//				dos.writeUTF(content.substring(40000*(i-1),40000*i<content.length()?40000*i:content.length())); 
//			}
//	    }else{
//	    	dos.writeUTF(content);
//	    }
//		dos.flush();
		content = dataTrim(content);
		dos.writeUTF(title);
		dos.writeUTF(content);
		dos.writeUTF(url);
		dos.writeUTF(className);
		dos.writeBoolean(true);
		dos.flush();
	}
	
	
	
	
	public static void main(String[] args) {
		String title = "多_m 吃_k 护_v 牙_k 食品_n 保护_v 伴随_v 一生_n 的_u 牙齿_n";
		String content = "●_w 对_p 青少年_n 儿童_n ，_w 家长_n 应_h 注意_h 千万_m 不要_v 让_v 他们_r 刚_k 吃_k 了_u 热_n 烫_k 的_u 食品_n 后_f ，_w 马上_d 又_d 去_h 吃_k 冰糕_n 、_w 冰淇淋_e ^_w ^_w ●_w 出现_v 牙_k 菌斑_n 、_w 牙_k 结石_n 等_u 情况_n 后_f ，_w 应_h 及时_a 到_v 正规_a 医院_n 清洗_v 口腔_n ^_w ^_w ●_w 选择_v 刷毛_n 排列_k 合理_a 、_w 便于_v 清洁_k 牙齿_n 、_w 在_p 口腔_n 内能_n 转动_v 灵活_a 的_u 牙刷_e ^_w ^_w 炎炎_z 夏日_t ，_w 人们_r 的_u 饮食_n 结构_n 发生_v 了_u 变化_k ，_w 一些_h 人_h 在_p 纳凉_v 时_n 喜欢_v 吃_k 火锅_n 、_w 夜宵_n 烧烤_n ，_w 同时_k 又_d 吃_k 冰糕_n 、_w 冰淇淋_e 等_u 降温_v 。_w 冷热_n 不均_a 的_u 饮食_n ，_w 会_v 对_p 牙齿_n 造成_v 严重_a 损伤_k 。_w 夏季_t 饮食_n 该_v 如何_r 调理_v ，_w 如何_r 保护_v 好_v 自己_r 的_u 牙齿_n ？_w 宜宾_ns 口腔_n 医院_n 院长_n 佘晓晴_nr ，_w 在_p 此_r 支招_nz 。_w ^_w ^_w 一_m 、_w 多_m 吃_k 护_v 牙_k 食品_n ^_w ^_w 针对_v 夏季_t 食品_n 冷热_n 不_h 均等_a 情况_n ，_w 佘晓晴_nr 院长_n 认为_v ，_w 夏季_t 应_h 多_m 吃_k 高_k 纤维_n 食品_n ，_w 如_k 蔬菜_n 、_w 粗粮_n 、_w 水果_n 等_u ，_w 对_p 保护_v 牙齿_n 非常_d 有利_a ，_w 还有_v 含_v 钙_e 较_k 高_k 的_u 肉_k 、_w 蛋_n 、_w 牛奶_e 等_u ，_w 也_d 对_p 保护_v 牙齿_n 有利_a 。_w 对_p 青少年_n 儿童_n ，_w 家长_n 应_h 注意_h 千万_m 不要_v 在_p 他们_r 刚_k 吃_k 了_u 热_n 烫_k 的_u 食品_n 后_f ，_w 马上_d 又_d 去_h 吃_k 冰糕_n 、_w 冰淇淋_e 、_w 冰粉_nz 等_u ，_w 以免_c 对_p 牙齿_n 造成_v 强烈_a 的_u 冷热_n 不均_a 刺激_k 。_w 夏季_t 多_m 给_v 青少年_n 儿童_n 吃_k 芹菜_e 、_w 卷心菜_e 、_w 菠菜_e 、_w 韭菜_e 等_u ，_w 除_k 有利于_v 保护_v 牙齿_n ，_w 对_p 促进_v 青少年_n 儿童_n 的_u 下颌_n 发达_k 和_c 牙齿_n 整齐_k 等_u ，_w 也_d 很_d 有_v 效果_n 。_w ^_w ^_w 二_k 、_w 注意_h 口腔卫生_nz ^_w ^_w 夏天_t 气温_n 较_k 高_k ，_w 一些_h 人_h 在_p 食用_k 辛辣_a 食物_n 后_f ，_w 不_h 注意_h 口腔卫生_nz ，_w 易_k 导致_v 牙齿_n 表面_n 出现_v 大量_m 菌斑_n ，_w 久而久之_i 即_v 成为_v 牙_k 结石_n 。_w 如果_c 牙齿_n 表面_n 有_v 牙_k 菌斑_n 、_w 牙_k 结石_n ，_w 又_d 没_v 能_v 及时_a 消除_v ，_w 有_v 可能_v 引起_v 牙龈_n 炎_h 、_w 牙周炎_n 等_u 一系列_n 疾病_n 。_w 出现_v 牙_k 菌斑_n 、_w 牙_k 结石_n 等_u 情况_n 后_f ，_w 应_h 及时_a 到_v 正规_a 医院_n 清洗_v 口腔_n ，_w 就是_k 人们_r 所说_n 的_u 洗_k 牙_k 。_w 此_r 方法_n 对_p 保护_v 口腔卫生_nz 、_w 预防_v 牙周病_nz 等_u 非常_d 重要_a 。_w ^_w ^_w 三_m 、_w 选择_v 适合_a 自己_h 的_u 牙刷牙膏_nz ^_w ^_w 由于_c 夏季_t 吃_k 忽_k 热_k 忽_k 冷_k 的_u 东西_k ，_w 会_v 对_p 牙齿_n 造成_v 严重_a 损伤_k ，_w 选择_v 符合_v 自己_h 口腔卫生_nz 要求_k 的_u 牙刷_e 、_w 牙膏_e ，_w 才_h 能_v 起到_v 护_v 牙_k 效果_n 。_w 在_p 一般_k 情况_n 下_f ，_w 选择_v 刷毛_n 排列_k 合理_a 、_w 便于_v 清洁_k 牙齿_n 、_w 在_p 口腔_n 内能_n 转动_v 灵活_a 的_u 牙刷_e ，_w 是_v 一种_mq 较_k 好_v 的_u 选择_v 。_w 而_h 电动牙刷_nz 也_d 是_v 不错_v 的_u 选择_v ，_w 其_h 在_p 刷牙_v 时_n 的_u 轻微_a 振动_v ，_w 不仅_k 能_v 促进_v 口腔_n 的_u 血液_n 循环_v ，_w 对_p 牙龈_n 组织_v 也_d 有_v 按摩_v 效果_n 。_w ^_w ^_w 牙膏_e 最好_d 选择_v 具有_v 修复_v 功能_n 的_u 防蛀_v 牙膏_e ，_w 其_h 使用_v 中_n 牙齿_n 发生_v 裂纹_n 的_u 地方_n ，_w 也_d 容易_a 吸收_v ，_w 并_h 能_v 起到_v 定向_v 修复_v 作用_k ，_w 有效_a 修补_v 破损_v 的_u 牙釉质_n ，_w 并_h 防止_v 细菌_n 等_u 的_u 入侵_v 。_w ";
		String url = "http://tech.ifeng.com/a/20120619/15407143_0.shtml";
		String className = "com.ifeng.secondLevelMapping.secondMappingForDiversity";
		String label = predict(title, content, url,className , null);
		//assertEquals("3.0",label);
		System.out.println(label);

	}
	
}
