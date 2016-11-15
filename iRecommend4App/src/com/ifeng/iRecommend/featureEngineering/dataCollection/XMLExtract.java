package com.ifeng.iRecommend.featureEngineering.dataCollection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3c.dom.*;

import javax.xml.parsers.*;

import com.ifeng.commen.reidx.SplitWordClient;
import com.ifeng.iRecommend.featureEngineering.FeatureExTools;
import com.ifeng.iRecommend.featureEngineering.TextProTools;

/**
 * 
 * <PRE>
 * 作用 : Read xml file , extract content , split words, analyse and save into map.
 *   
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :possible key of map: 
 * 		flag / cat / id / title / keywords / wwwurl / classid /
 * 		updatetime / source / message / createtime / other / 
 * 		documenturl / videotime / imgnum / 
 * 		imgcount / s_title / s_content / doctype
 * 
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015-12-26         hexl          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class XMLExtract {
	static Logger LOG = Logger.getLogger(XMLExtract.class);
	/**
	 * Read xml file, transformed it into hashmap with dom.
	 * 
	 * @param filename
	 * @return HashMap<String,String> xmlMap
	 */
	public static HashMap<String, String> xmlProcess(String filename) {
		if(filename == null || filename.isEmpty())
			return null;
		HashMap<String, String> xmlMap = new HashMap<String, String>();
		String logstr = null;
		try {
			File f = new File(filename);
			 BufferedReader reader = null;
		        try {
		            reader = new BufferedReader(new FileReader(f));
		            String tempString = null;
		            String str = "";
		            // 一次读入一行，直到读入null为文件结束
		            while ((tempString = reader.readLine()) != null) {
		                // 显示行号
		            	str = str + tempString;
		            }
		            reader.close();
		            //清洗掉xml文件中的特殊字符，否则会影响解析
		            FileWriter fw = null;
		            fw = new FileWriter(filename, false);
		            str = str.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");
		            fw.write(str);
		            fw.flush();
		            fw.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        } finally {
		            if (reader != null) {
		                try {
		                    reader.close();
		                } catch (IOException e1) {
		                }
		            }
		        }
			logstr = "#id_"+f.getName();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//			System.out.println(f.toString());
			DocumentBuilder builder = factory.newDocumentBuilder();
			File file = new File(filename);
			Document doc = builder.parse(file);
			Node root = doc.getFirstChild();
			NodeList nl = root.getChildNodes();
//			if(file.exists())
//				file.delete();
			// Traverse all node of root, put into map
			for (int i = 0; i < nl.getLength(); i++) {
				xmlMap.put(nl.item(i).getNodeName(), nl.item(i).getTextContent());
			}
		} catch (Exception e) {
			LOG.error(logstr+"[ERROR] Read xml " + filename + " file failed.", e);
			xmlMap = null;
			e.printStackTrace();
			return xmlMap;
		}
		
		 logstr = logstr+ "#title_"+xmlMap.get("title");
		LOG.info(logstr+ "[INFO] read xml "+ filename);
		try {
			xmlMap = splitTitleAndContent(xmlMap);
		} catch (Exception e) {
			LOG.error(logstr+"[ERROR] In split title and content.", e);
			e.printStackTrace();
		}
		// process url, fix some url lack of /
		try {
			String wwwurl = xmlMap.get("wwwurl");
			if (wwwurl == null || wwwurl.contains("http://")) {
			} else {
				wwwurl = wwwurl.replaceAll("http:/", "http://");
			}
			xmlMap.put("wwwurl", wwwurl);
		} catch (Exception e) {
			LOG.error(logstr+"[ERROR] In fix url.", e);
		}
		// process imgnum field, move its value to imgcount
		try {
			String imgnum = xmlMap.get("imgnum");
			if (imgnum == null) {
			} else {
				xmlMap.put("imgcount", imgnum);
			}
		} catch (Exception e) {
			LOG.error(logstr+"[ERROR] In fix imgcount.", e);
		}
		// process other field, add imgNum=
		try {
			String other = xmlMap.get("other");
			if (other == null) {
			} else {
				String imgcount = xmlMap.get("imgcount");
				if (imgcount == null)
					other = other + "|!|imgNum=0";
				else
					other = other + "|!|imgNum=" + imgcount;
				xmlMap.put("other", other);
			}
			String doctype = GetDocType(other);
			xmlMap.put("doctype", doctype);
		} catch (Exception e) {
			LOG.error(logstr+"[ERROR] In fix other.", e);
		}
		return xmlMap;
	}
	/**
	 * split Title And Content
	 * @param xmlMap
	 * @return
	 */
	private static HashMap<String, String> splitTitleAndContent(HashMap<String, String> xmlMap) {
		if (xmlMap == null)
			return null;
		String title = xmlMap.get("title");
		String content = xmlMap.get("message");
		if(title == null)
			title = "";
		if(content == null)
			content = "";
		int imgcount = 0;
		StringBuffer splitBuffer = new StringBuffer();
		title = TextProTools.filterString(title);
		splitBuffer.append(title);
		String separater = FeatureExTools.splitTag;
		List<String> picurlList = null;
		try {
			// 判断正文中是否有图，并记录图片数量（仅适用于非高清图内容）
			if (content.contains("<img") || content.contains("<IMG") || content.contains("<Img")) {
				String[] imgSplit = content.split("<img|<IMG|<Img");
				imgcount = imgSplit.length - 1;
				xmlMap.put("imgcount", String.valueOf(imgcount));
				picurlList = TextProTools.findPicUrl(content);
			} else {
				// There is no picture in content.
			}
			content = TextProTools.filterHtml(content);
			content = TextProTools.filterString(content);
			if (content == null || content.isEmpty())
				content = "";
			splitBuffer.append(separater).append(content);
		} catch (Exception e) {
			LOG.error("[ERROR] InsplitTitleAndContent", e);
		}
		// 对标题、内容进行合并和统一分词
		// 用SplidWordClient进行分词
		// 分词
				String textSplited = "";
				String preSplit = TextProTools.filterString(splitBuffer.toString());
				for(int i = 0; i < 3; i++){
					try {
						textSplited = new String(SplitWordClient.split(preSplit, null).replace("(/", "_").replace(") ", " "));
						break;
					} catch (Exception e) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						LOG.error("[ERROR] Split error. Wait 5 seconds. "+i+" times.");
					}
				}
			if(textSplited == null || textSplited.length() < splitBuffer.toString().length())
				LOG.info(xmlMap.get("id")+" [SPLITWORD]splited length less than before or null. "+xmlMap.get("wwwurl"));
		String textSplits[] = textSplited.split(FeatureExTools.splitedTag);

		if (textSplits.length > 0)
			xmlMap.put("s_title", textSplits[0]);
		if (textSplits.length > 1) {
			String splitcontent = textSplits[1];
			
			if (picurlList != null && picurlList.size() > 0 && splitcontent != null && splitcontent.contains(FeatureExTools.picTag)) {
				for (int i = 0; i < picurlList.size(); i++) {
					try{
					splitcontent = splitcontent.replaceFirst(FeatureExTools.picTag, picurlList.get(i));
					}catch(Exception e){
						LOG.error("[ERROR] replace pic tag."+xmlMap.get("id"));
					}
				}
			}
			xmlMap.put("s_content", splitcontent);
		}
		return xmlMap;
	}
	
	/**
	 * analyse other to get doc type 
	 * @param other
	 * @return
	 */
	private static String GetDocType(String other) {
		if (other == null)
			return "doc";
		String[] others = other.split("\\|!\\|");

		for (String s : others) {
			if (s.contains("pic=")) {
				String[] tags = s.split("\\|");
				if (tags.length != 3)
					return "doc";
				if (tags[2].equals("1"))
					return "slide";
				else if (tags[1].equals("1"))
					return "docpic";
				else
					return "doc";
			} else
				return "doc";
		}
		return "doc";
	}
	
	public static void main(String args[]) {
		HashMap<String,String> map = XMLExtract.xmlProcess("C:\\Users\\hexl1\\Downloads\\111728758.xml");
		for(Entry<String,String> entry : map.entrySet())
		{
			System.out.println(entry.getKey()+" "+entry.getValue());
		}
	}
}