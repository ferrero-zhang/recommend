package com.ifeng.iRecommend.lidm.userLog;

/**
 * <PRE>
 * 作用 : 
 *   LRU列表。
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 * 	 
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2014-01-21       lidm          change
 * -----------------------------------------------------------------------------
 * </PRE>
 */

import java.util.LinkedHashMap;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;

    private int maxCapacity;

    public LRUCache(int maxCapacity) {
	super(16, 0.75f, true);
	this.maxCapacity = maxCapacity;
    }

    public int getMaxCapacity() {
	return this.maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
	this.maxCapacity = maxCapacity;
    }

    /**
     * 当列表中的元素个数大于指定的最大容量时，返回true,并将最老的元素删除。
     */
    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
	if (super.size() > maxCapacity) {
	    return true;
	}
	return false;
    }
}