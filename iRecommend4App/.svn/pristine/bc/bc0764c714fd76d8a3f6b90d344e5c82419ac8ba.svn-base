package com.ifeng.iRecommend.liuyi.customDicWordSearch;


import java.io.DataOutputStream;
import java.util.TreeMap;

public interface ITrie<V>
{
	int build(TreeMap<String, V> keyValueMap);

	boolean save(DataOutputStream out);

	V get(char[] key);

	V get(String key);

	V[] getValueArray(V[] a);

	boolean containsKey(String key);

	int size();
}
