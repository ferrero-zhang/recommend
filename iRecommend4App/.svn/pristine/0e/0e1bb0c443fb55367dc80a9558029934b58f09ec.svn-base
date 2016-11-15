/**
 * 
 */
package com.ifeng.iRecommend.dingjw.itemParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.oscache.OSCache;
import com.ifeng.iRecommend.dingjw.itemParser.Tree.TreeNode;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

/**
 * <PRE>
 * 作用 : 
 *   对输入的item，计算其逻辑频道
 * 使用 : 
 *   
 * 示例 :
 *   只处理长短url或者item，不能处理item id
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2013-8-20        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class channelsParser {
	private static final Log LOG = LogFactory.getLog("channelsParser");
	private static channelsParser instance = null;
	private Tree<String> tree;//频道映射树
	private Tree<String> front_tree;//前端映射树
	private Tree<String> slide_tree;
	private ItemOperation itemOP;
	
	private channelsParser(ItemOperation.ItemType itemType)
	{
		LOG.info("BuildMappingTree...");
		if(itemType == ItemOperation.ItemType.PCITEM)
		{
			tree = BuildMappingTree(fieldDicts.pcTreeMappingFile);
			front_tree = BuildMappingTree(fieldDicts.pcFrontTreeMappingFile);
			slide_tree = BuildMappingTree(fieldDicts.pcSlideTreeMappingFile);
		}
		if(itemType == ItemOperation.ItemType.APPITEM)
		{
			tree = BuildMappingTree(fieldDicts.appTreeMappingFile);
			front_tree = BuildMappingTree(fieldDicts.frontAppTreeMappingFile);
			slide_tree = BuildMappingTree(fieldDicts.appSlideTreeMappingFile);
		}
		itemOP = ItemOperation.getInstance();
		itemOP.setItemType(itemType);
	}
	
	public synchronized static channelsParser getInstance(ItemOperation.ItemType itemType){
		if(instance == null)
			instance = new channelsParser(itemType);
		return instance;
	}
	
	/**
	 * 根据item获取一篇文章映射后的频道路径
	 * <p>
	 * 根据item的url获取长url，解析出原始频道路径，再根据本地存储的频道映射表获取进行映射后的频道路径
	 * </p>
	 * 
	 * @param item 文章类, 
	 * @param treeNum： 0是频道映射树，1是前端映射树,2是高清图映射树
	 * @return 频道路径，如ent-live;或taiwan-special,taiwan-fanhe
	 * @throws
	 */
	public String getTransChannelByItem(Item item, int treeNum){
		if(item==null||item.getUrl() == null || item.getUrl().isEmpty())
			return null;
		try{
			String url = item.getUrl();
			//如果是长url
			if(!UrlPretreatment.isShortUrl(url))
				return UrlMapping(url, treeNum);
			String longUrls = null;
			
			if(item.getChannel() != null && !(item.getChannel().trim().isEmpty()))
				longUrls = item.getChannel();
			else
				;//@test longUrls = UrlPretreatment.urlProcessing(url);
			
			if(longUrls==null)
				return UrlMapping(url, treeNum);
			//一个短url有可能对应两个长url
			return getChannelByLongUrls(longUrls, treeNum);
			
		}catch(Exception e){
			LOG.error("[ERROR] ", e);
			return null;
		}
	}
	
	/**
	 * 根据url或者原始頻道路徑获取一篇文章映射后的频道路径
	 * <p>
	 * 根据url获取长url，解析出原始频道路径，再根据本地存储的频道映射表获取进行映射后的频道路径
	 * </p>
	 * 
	 * @param url，文章的网址，如http://news.ifeng.com/mil/, 或者頻道路徑，如news-mil
	 * @param treeNum： 0是频道映射树，1是前端映射树,2是高清图映射树
	 * @return 频道路径，如ent-live;或taiwan-special,taiwan-fanhe
	 * @throws
	 */
	public String getTransChannel(String url, int treeNum) {
		if(url==null||url.isEmpty())
			return null;
		try{
			//如果url不规范，补充上
			if(url.startsWith("/"))
				url = "http://i.ifeng.com"+url;
			
			//如果是长url
			if(!UrlPretreatment.isShortUrl(url)) 
			{
				//System.out.println("This url is long url");
				return UrlMapping(url, treeNum);
			}
			
			//如果不是长url，先从存放pc端item的hbase中获取长url
			String longUrls = null;
			//change likun 2014/12/08 bug!!!:itemOP.setItemType(ItemOperation.ItemType.PCITEM);
			Item item = itemOP.getItem(url);
			if(item != null && item.getChannel() != null)
				longUrls = item.getChannel();
			else if(url.indexOf("i.ifeng.com")<0&&url.indexOf("3g.ifeng.com")<0)
				;//@test longUrls = UrlPretreatment.urlProcessing(url);
			
			
			if(longUrls==null)
			{
				
				return UrlMapping(url, treeNum);
			}
			//一个短url有可能对应两个长url
			return getChannelByLongUrls(longUrls, treeNum);
			
		}catch(Exception e){
			LOG.error("[ERROR] ", e);
			return null;
		}
	}
	
	/**
	 * 输入一个到两个长url，获取频道路径
	 * <p>
	 * 
	 * @param longUrls，一个到两个长url, 
	 * @param treeNum： 0是频道映射树，1是前端映射树,2是高清图映射树
	 * @return 频道路径，如ent-live
	 * @throws
	 */
	private String getChannelByLongUrls(String longUrls, int treeNum){
		
		if(longUrls==null)
			return null;
		//一个短url有可能对应两个长url
		String[] longUrlList = longUrls.split(",");
		if(longUrlList.length==1){
			return UrlMapping(longUrlList[0], treeNum);
			
		}else{
			//若一个url包含另一个url，则保留较长的url
			if(longUrlList[0].contains(longUrlList[1]) ||
					longUrlList[1].contains(longUrlList[0])){
				String longerUrl = longUrlList[0].length()>=longUrlList[1].length()?longUrlList[0]:longUrlList[0];
				return UrlMapping(longerUrl, treeNum);
			}else{//若一个url不包含另一个url，则返回两个频道路径，以逗号分隔
				StringBuffer channelSB = new StringBuffer();
				channelSB.append(UrlMapping(longUrlList[0], treeNum)).append(",").
				append(UrlMapping(longUrlList[1], treeNum));
				return channelSB.toString();
			}	
		}
		
	}
	
	/**
	 * 根据url获取一篇文章映射后的频道路径
	 * <p>
	 * 
	 * @param url，文章的网址, 
	 * @param treeNum： 0是频道映射树，1是前端映射树,2是高清图映射树
	 * @return 频道路径，如ent-live
	 * @throws
	 */
	private String UrlMapping(String url, int treeNum){
		
		if(url==null||url.isEmpty())
			return null;
		//若treeNum为0，则以频道映射树获取频道，否则取前端映射树
		Tree<String> currentTree;
		switch (treeNum) {
		case 0:
			currentTree = tree;
			break;
		case 1:
			currentTree = front_tree;
			break;
		case 2:
			currentTree = slide_tree;
			break;
		default:
			currentTree = front_tree;
			break;
		}
		String oldPath = null;
		
		//@test，临时处理，将数据临时处理下；
		url = url.replaceAll("(3g|wap|iwap).ifeng.com", "i.ifeng.com");
		
		
		//判斷是否url
		if(url.contains("http:/"))
			oldPath = urlParse(url);//如果是url，則返回原始频道路径
		else
			oldPath = url;//如果不是url，則直接處理
		try {
			List<String> path  = new ArrayList<String>();
			for(String str:oldPath.split("-"))
				path.add(str);	
			String old="";		
			TreeNode<String> node =currentTree.SearchNode(path);//获取在映射树中的节点
			if (node != currentTree.getRoot())//获取在映射树中的路径
				for (int i = 0; i < node.getDepth() + 1; ++i) {
					if (i != 0)
						old += "-";
					old += path.get(i);
				}
			
			String content = node.getContent();//获取映射频道
			String tempString = "";
			if(content==null)//如果映射为空，则不处理
				tempString = oldPath;
			else
				if(content.split("-").length>=oldPath.split("-").length)//若映射频道长度不小于原有频道，则替换原有频道
					tempString = content;
				else{
					if(old.contains("-"))//若映射频道不是第一层，在替换需要映射的频道
						tempString = oldPath.replace(old,content );
					else {//只替换第一层频道
						String topics[] = oldPath.split("-");
						tempString+=content;
						for(int i=1; i<topics.length;i++)
							tempString+="-"+topics[i];
					}
				}
			//过滤日期
			StringBuffer sBuffer = new StringBuffer();
			for(String str:tempString.split("-"))
			{
				 Pattern pattern = Pattern.compile("[12]\\d{3,7}"); 
				 if(str.length()>=4 && pattern.matcher(str).matches() && sBuffer.length() > 0)
					 continue;
				 else{
					sBuffer.append(str).append("-");
				 }
					
			}
			String result = sBuffer.toString();
			return result.substring(0, result.length()-1);
		}catch (Exception e) {
			LOG.error("",e);
			return null;
		}
		
		
	}
	

	
	/**从本地读取映射表 初始化树
	 * 
	 * @return
	 */
	private static Tree<String> BuildMappingTree(String path){
		Tree<String> tree = new Tree<String>(String.class);
		BufferedReader rd =null;
		//Properties props=System.getProperties(); 
		//String path = fieldDicts.treeMappingFile;
	    
		try {
			rd = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF8"));
			String line =null;
			while((line=rd.readLine())!=null)
			{
				String[] splited =line.split("->");
				List<String> oldPath = new ArrayList<String>();
				for(String str:splited[0].split("-"))
						oldPath.add(str);
				String newPath = splited[1];
				tree.insertNode(oldPath, newPath);
				
			}
		} catch (UnsupportedEncodingException e) {
			LOG.error("[ERROR] ", e);
		} catch (FileNotFoundException e) {
			LOG.error("[ERROR] ", e);
		} catch (IOException e) {
			LOG.error("[ERROR] ", e);
		}
		finally{
			if(rd!=null)
				try {
					rd.close();
				} catch (IOException e) {
					LOG.error("[ERROR] ", e);
				}

		}
		return tree;
	}
	
	/**
	 * 解析url，获取url中的频道信息
	 * 
	 * @param url 如 http://news.ifeng.com/mainland/detail_2011_10/19/9955077_0.shtml
	 * @return channel 如news-mainland
	 */
	public static String urlParse(String url){
		if(url==null||url.isEmpty())
			return null;
		Pattern pattern = Pattern.compile("http:/+([\\w+\\.]+?)\\.\\w+\\.com.*?/(.*)$");
		Pattern isWords = Pattern.compile("^[a-zA-Z0-9]+$");
		StringBuilder sb = new StringBuilder();
		Matcher matcher = pattern.matcher(url);
		try{
			if(matcher.matches())
			{
				String[] splited = matcher.group(2).split("/");
				String head = matcher.group(1);
				String headSplited[] = head.split("\\.");
				//若url如http://hj.house.ifeng.com/a/20140612/
				if(headSplited.length > 1){
					for(int i = headSplited.length-1; i>=0; i--)
						sb.append(headSplited[i]).append("-");
				}else
					sb.append(head).append("-");

				for(int i =0;i<splited.length;++i){
					Matcher isWordsMatcher = isWords.matcher(splited[i]);
					if(isWordsMatcher.matches()){
						sb.append(splited[i]);
						sb.append("-");
					}
					else
						break;
				}
					if(sb.length()!=0)
						sb.deleteCharAt(sb.length()-1);
			}
		}catch (Exception e) {
			LOG.error("",e);
		}
		return sb.toString();
	}
	
	/**
	 * 根据url判断是否专题页
	 * 注意
	 * 
	 * @param url
	 * @return true or false
	 */
	public static boolean isZhuanti(String url)
	{
		if(url == null)
			return false;
		
		if(!url.endsWith("html")
				&&(url.contains("/special/")||url.contains("/zhuanti/")))
			return true;
		return false;
	}
	
	/**
	 * 根据url判断是否一级频道目录页
	 * 注意
	 * 
	 * @param url，文章的网址, treeNum： 0是频道映射树，1是前端映射树,2是高清图映射树
	 * @return true or false
	 */
	public boolean isFirstLevelMenu(String url, int treeNum)
	{
		if(url == null)
			return false;
		
		if((url.endsWith("/") || url.endsWith(".com"))
			&& getTransChannel(url, treeNum).split("-").length <= 1)
			return true;			
		else
			return false;
	}
	
	/**
	 * 根据url获取一级频道
	 * 注意
	 * 
	 * @param url，文章的网址, treeNum： 0是频道映射树，1是前端映射树,2是高清图映射树
	 * @return true or false
	 */
	public String getFirstLevel(String url, int treeNum)
	{
		if(url == null)
			return "";
		
		String transChannel = getTransChannel(url, treeNum);
		if(transChannel == null)
			return null;
		return transChannel.split("-")[0];
	}
}
