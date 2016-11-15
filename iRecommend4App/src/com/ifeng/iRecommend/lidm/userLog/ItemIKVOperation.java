package com.ifeng.iRecommend.lidm.userLog;

import java.util.Arrays;

import ikvdb.client.IkvdbClient;
import ikvdb.client.IkvdbClientConfig;
import ikvdb.client.IkvdbClientFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.JsonUtils;
import com.ifeng.iRecommend.dingjw.itemParser.Item;
import com.ifeng.iRecommend.dingjw.itemParser.ItemOperation.ItemType;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

public class ItemIKVOperation {
	private static final Log LOG = LogFactory.getLog("ItemIKVOperation");

	private static IkvdbClient<String, String> client;

	private static final String IKV_TABLE_NAME = "ir_items";

	private String tableName = fieldDicts.pcItemTableNameInHbase;// tableName默认pc端的item数据表
	
	public static void ItemIKVInit(){
		// 初始ikv;
		try{
			IkvdbClientConfig config = new IkvdbClientConfig();

			// 设置服务器地址（启动路径），可以设置多个，保证至少有一个能连接上
			String[] urls = new String[] { "tcp://10.32.25.30:6666",
					"tcp://10.32.25.36:6666", "tcp://10.32.25.40:6666",
					"tcp://10.32.25.50:6666", };
			config.setBootstrapUrls(Arrays.asList(urls));

			IkvdbClientFactory factory = new IkvdbClientFactory(config);

			client = factory.getClient(IKV_TABLE_NAME);

			LOG.info(IKV_TABLE_NAME + " connect...");
		}catch(Exception e){
			LOG.error("init ikv error..."+e.getMessage());
		}
		
	}
	/*// item类型
	public enum ItemType {
		PCITEM, APPITEM, UNDEFINED
	};
	// Item的默认类型为UNDEFINED
	private ItemType itemType = ItemType.UNDEFINED;

	// 设置item的类型，并根据item类型自动设置合适的数据表，以利用接口读取数据表中的数据。
	// 在调用接口前设置，只设置一次即可
	public void setItemType(ItemType _ItemType) {
		itemType = _ItemType;
		setTableName(_ItemType);
	}

	// 手动设置数据表的名称，会自动更新item类型
	private void setTableName(ItemType itemType) {
		switch (itemType) {
		case PCITEM:
			tableName = fieldDicts.pcItemTableNameInHbase;
			break;
		case APPITEM:
			tableName = fieldDicts.appItemTableNameInHbase;
			break;
		default:
			break;
		}
		;
	}*/
	
	protected static Item get(String key) {
		if (key == null)
			return null;
		Item value = null;
		String jsonSource = "";
		try {
			jsonSource = client.getValue(key);
			value = JsonUtils.fromJson(jsonSource, Item.class);
		} catch (Exception e) {
			LOG.error("ERROR get: " + key);
			LOG.error("ERROR get", e);
			return null;
		}
		return value;
	}
	public static void main(String[] args){
		ItemIKVOperation itemO = new ItemIKVOperation();
		String key = "93472454";
		Item item = itemO.get(key);
		
	}

}
