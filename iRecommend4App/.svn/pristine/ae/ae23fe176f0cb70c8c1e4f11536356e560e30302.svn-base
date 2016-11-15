package com.ifeng.iRecommend.liuyi.customDicWordSearch;


import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.iRecommend.liuyi.customDicWordSearch.PatternHit.IHit;

/**
 * 
 * <PRE>
 * 作用 : 
 *   用户自定义词典匹配
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   根据开源项目hanlp相关实现修改，测试版
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2016年1月18日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class CustomWordSearcher
{
	
	private static final Log logger = LogFactory.getLog(CustomWordSearcher.class);
    /**
     * 用于储存用户动态插入词条的二分trie树
     */
    public static BinTrie<String> trie;
   
    /**
     * 第一个是主词典，其他是副词典
     */
    public final static String path[] = {""};

    public static void initTrie() {
    	trie = new BinTrie<String>();
    }
    
    

    /**
     * 往自定义词典中插入一个新词（非覆盖模式）
     *
     * @param word                新词 如“裸婚”
     * @return 是否插入成功（失败的原因可能是不覆盖等，可以通过调试模式了解原因）
     */
    public static synchronized boolean add(String word)
    {
		if (contains(word))
			return false;
		return insert(word);
    }

    /**
     * 以覆盖模式增加新词
     *
     * @param word
     * @return
     */
    public static boolean insert(String word)
    {
    	if (trie == null){
    		initTrie();
    	}
    	
		trie.put(word, word);
        return true;
    }

   
    /**
     * 查单词
     *
     * @param key
     * @return
     */
    public static String get(String key)
    {	
		if (trie == null) {
			return null;
		}

		return trie.get(key);
    }

    /**
     * 删除单词
     *
     * @param key
     */
    public static synchronized void remove(String key)
    {
		if (trie == null) {
			return;
		}
		
		trie.remove(key);
    }

   
    /**
     * 清空trie
     * 
     */
    public static synchronized void reset () {
    	if (trie == null) {
    		return;
    	}
    	
    	// 直接新new一个
    	trie = new BinTrie<String>();
    }
    
    @SuppressWarnings("rawtypes")
	public static BaseSearcher getSearcher(String text)
    {
        return new Searcher(text);
    }

    @Override
    public String toString()
    {
        return "CustomDictionary{" +
                "trie=" + trie +
                '}';
    }

    /**
     * 词典中是否含有词语
     * @param key 词语
     * @return 是否包含
     */
    public static boolean contains(String key)
    {
		return trie != null && trie.containsKey(key);
    }

    /**
     * 获取一个BinTrie的查询工具
     * @param charArray 文本
     * @return 查询者
     */
    @SuppressWarnings("rawtypes")
	public static BaseSearcher getSearcher(char[] charArray)
    {
        return new Searcher(charArray);
    }

    static class Searcher extends BaseSearcher<String>
    {
        /**
         * 分词从何处开始，这是一个状态
         */
        int begin;

        private LinkedList<Map.Entry<String, String>> entryList;

        protected Searcher(char[] c)
        {
            super(c);
            entryList = new LinkedList<Map.Entry<String, String>>();
        }

        protected Searcher(String text)
        {
            super(text);
            entryList = new LinkedList<Map.Entry<String, String>>();
        }

        @Override
        public Map.Entry<String, String> next()
        {
            // 保证首次调用找到一个词语
            while (entryList.size() == 0 && begin < c.length)
            {
                entryList = trie.commonPrefixSearchWithValue(c, begin);
                ++begin;
            }
            // 之后调用仅在缓存用完的时候调用一次
            if (entryList.size() == 0 && begin < c.length)
            {
                entryList = trie.commonPrefixSearchWithValue(c, begin);
                ++begin;
            }
            if (entryList.size() == 0)
            {
                return null;
            }
            Map.Entry<String, String> result = entryList.getFirst();
            entryList.removeFirst();
            offset = begin - 1;
            return result;
        }
    }

    /**
     * 获取词典对应的trie树
     *
     * @return
     */
    public static BinTrie<String> getTrie()
    {
        return trie;
    }

    /**
     * 解析一段文本
     * @param text         文本
     * @param processor    处理器
     */
    public static void parseText(char[] text, IHit<String> processor)
    {
        if (trie != null)
        {
            BaseSearcher searcher = CustomWordSearcher.getSearcher(text);
            int offset;
            Map.Entry<String, String> entry;
            while ((entry = searcher.next()) != null)
            {
                offset = searcher.getOffset();
                processor.hit(offset, offset + entry.getKey().length(), entry.getValue());
            }
        }
    }
    
	public static List<String> parseText(String text) {		
		List<String> result = new ArrayList<String>();
		
		if (null == text) {
			return result;
		}
		
		if (trie != null) {
			BaseSearcher searcher = CustomWordSearcher.getSearcher(text.toCharArray());
			Map.Entry<String, String> entry;
			while ((entry = searcher.next()) != null) {
				// "_x"是外挂词标签
				result.add(entry.getValue() + "_x");
			}
		}

		return result;
	}
	
	
	/**
	 * @Title: isContainCustomWord
	 * @Description: 判断text中是否含有用户词典中的词
	 * @author liu_yi
	 * @param text
	 * @return
	 * @throws
	 */
	public static boolean isContainCustomWord(String text) {
		boolean result = false;
		
		if (null == text) {
			return result;
		}
		
		if (trie != null) {
			BaseSearcher searcher = CustomWordSearcher.getSearcher(text.toCharArray());
			Map.Entry<String, String> entry;
			while ((entry = searcher.next()) != null) {
				result = true;
				break;
			}
		}

		return result;
	}
	
    public static void main(String[] args) {
    	//CustomDictionary cd = new CustomDictionary();
     
//    	CustomDictionary.add("周恩来");
//    	CustomDictionary.add("周恩来在");
//    	CustomDictionary.add("中南海");
//    	CustomDictionary.add("紫光阁");
    	
//    	BaseSearcher<?> searcher = CustomDictionary.getSearcher("周恩来在中南海紫光阁也接见了参加大会的著名女演员");
//        Map.Entry entry;
//        while ((entry = searcher.next()) != null)
//        {
//            System.out.println(entry.getValue());
//        }
//        
    	CustomWordSearcher.initTrie();
     	
        DicBasedLongestSegment<String> segmenter = new DicBasedLongestSegment<String>(CustomWordSearcher.trie);
        addWordThread at = new addWordThread();
        at.start();
		for (int i = 0; i != 10; i++) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
//			segmenter.reset("中华人民共和国政治协商会议");
//			System.out.println(segmenter.seg("中华人民共和国政治协商会议"));
			
//			CustomDictionary.parseText("中华人民共和国政治协商会议".toCharArray(), new PatternHit.IHit<String>() {
//				public void hit(int begin, int end, String value)
//	            {
//	                System.out.printf("[%d:%d]=%s\n", begin, end, value);
//	            }
//			});
			
			BaseSearcher<?> searcher = CustomWordSearcher.getSearcher("中华人民共和国政治协商会议");
			Map.Entry entry;
			System.out.println(CustomWordSearcher.parseText("中华人民共和国政治协商会议"));
			System.out.println("");
		}
    }
}
