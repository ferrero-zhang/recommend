package com.ifeng.commen.reidx;

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
 * 	分词请求的客户端代码
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
public class SplitWordClient {

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
			String ips = LoadConfig.lookUpValueByKey("segment_server_ip");
			int ports = Integer.valueOf(LoadConfig.lookUpValueByKey("segment_server_port"));
			int timeouts = Integer.valueOf(LoadConfig.lookUpValueByKey("segment_timeoutInMS"));
			ip = ips;
			port = ports;
			timeOut = timeouts;
		}catch(Exception e){
			e.printStackTrace();
			timeOut = 5000;
		}
	}
	
	/**
	 * 考虑到关键词客户端的使用并不是很频繁，没有并发请求，所以采用比较好容易实现的短连接方式
	 * @param content
	 * @return
	 */
	public static String split(String content,Log logger){
		String response = "";
		Socket socket = null;
		DataInputStream dis = null;
		DataOutputStream dos = null;
		try {
			socket = new Socket(ip, port);
			socket.setSoTimeout(timeOut);
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			sendCmd(dos, content);
			response = dis.readUTF();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			if(logger!=null)
				logger.error("UnknownHostException", e);
			return "error1";
		}catch (UTFDataFormatException e) {
			//数据太长,写失败
			e.printStackTrace();
			if(logger!=null)
				logger.error("data too long", e);
			return "error2";
		}catch (IOException e) {
			e.printStackTrace();
			if(logger!=null)
				logger.error("IOException", e);
			return "error3";
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
				e.printStackTrace();
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
	private static void sendCmd(DataOutputStream dos, String content) throws IOException{
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
		dos.writeUTF(content);
		dos.writeBoolean(true);
	}
	
	
	
	
	public static void main(String[] args) {
		String content = "<p class=\"picIntro\"><span>　　张清芳(资料图)</span></p><p>据台湾媒体报道，昨中秋节，婚后淡出歌坛8年的“东方不败”张清芳与有“投资教父”之称、现身家超过3亿元台币的高盛证券亚洲区前副董宋学仁，位于香港浅水湾的豪宅发生失窃案，她昨早发现1只价值约100万元港币的名贵钻戒不翼而飞，报警求助，警方调查后发现无偷窃痕迹，正追查是否为熟人为所，她昨惊讶说：“谁跟你讲的，我正在处理中。”</p><p>张清芳婚前唱红《激情过后》、《出嫁》、《无人熟识》等歌，曾获2届金曲歌后肯定。</p><p>2004年1月她和宋学仁订婚后，手上戴著超过4克拉的订婚钻戒质地纯净、闪亮无比，当时该钻戒价格约为340万元台币，但失窃的钻戒是否就是这只则不得而之。</p><p>2亿3层城堡 保全驻守</p><p>张清芳2005年底嫁给宋学仁后生了2个儿子，定居香港浅水湾半山丽景花园豪宅，此豪宅是宋学仁在2002年以2600万元港币购买，楼高共3层，现值近2亿元港币，有监视器和保全人员驻守。她的邻居有港星郑中基(微博)、叶童、张敏以及英皇集团主席杨受成等，屋龄虽老，但该区是香港超级豪宅区。</p><p>昨早8点多，张清芳起床后发现1只价值约380万元台币的名贵钻戒不见，立刻报警。</p><p>警方搜证 并无被搜括痕迹</p><p>警察发现该宅内并无被搜括痕迹，在现场拍照搜证，列窃案处理。记者昨致电张清芳，问她是否为内贼偷窃？她说：“我现在不知道，正在处理中。”还有其他财物失窃吗？她肯定说：“没有。”立刻讨饶说：“不要写啦，再讲我就打你屁股，中秋节快乐。”随即挂断电话。</p><p>此外，张清芳夫妇为了小孩的中文教育，曾考虑回台定居，2008年买下隔著自强隧道、离市区不会太远、环境清幽的外双溪别墅梅林社区400坪多坪土地，近期打算自建别墅。</p><p>歌坛东方不败 老公身家3亿</p><p>姓名：张清芳</p><p>年龄：47岁(1966╱08╱31)</p><p>作品：29张专辑，代表作有《激情过后》、《加州阳光》、《出嫁》、《燃烧一瞬间》等，总销售量超过1300万张。首张专辑《激情过后》创30万张佳绩，《光芒》专辑大卖100万张，有歌坛“东方不败”称号</p><p>得奖：1995年、1997年获金曲奖最佳女演唱人奖</p><p>婚姻：2005年与时任高盛证券亚洲区副董宋学仁结婚后淡出演艺圈，宋学仁现已退休，育有2子，一家4口现定居香港。宋学仁有“投资教父”之称，身家超过15亿元台币；2006年9月他送老婆4000万台币的游艇作为生第1胎的礼物，2008年夫妇俩买下台湾外双溪梅林社区内400多坪土地，最近打算自建别墅</p><p>玩票：2010年12月在台北小巨蛋连办2场演唱会，宋学仁在台下聆听。</p>";
		String response = split(content, null);
		System.out.println("result:"+response);

	}
	
}
