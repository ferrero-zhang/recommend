package com.ifeng.iRecommend.featureEngineering.DetectExtrmSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.sf.json.JSONArray;

import com.ifeng.ikvlite.IkvLiteClient;

public class HashStage {
	private static final String INDEX_1_LEV = "index1_";
	private static final String INDEX_2_LEV = "index2_";
	private static final long MASK16 = 0xffffL;
	private static final long MASK12 = 0xfffL;

	public static enum TASK_NAME {
		SELECT_IMG, COMBINE_IMG, COMBINE_HASH, SELECT_HASH, COMBINE_TITLE, SELECT_TITLE, COMBINE_SENTENCE, SELECT_SENTENCE
	};

	public static String[] getHashCodeSearchKey(long hashcode) {
		long[] index1 = new long[4];
		String[] index1Key = new String[4];
		// Map<String,String[]> index1ToIndex2=new HashMap<String,String[]>();
		String[] sk = new String[16];

		for (int i = 0; i < 4; i++) {
			index1[i] = hashcode & (MASK16 << (64 - 16 * (i + 1)));
			index1[i] = index1[i] >>> 64 - 16 * (i + 1);
			index1Key[i] = INDEX_1_LEV + i + "_" + index1[i];

		}
		int indexSk = 0;
		for (int i = 0; i < 4; i++) {
			int count = 0;
			long remain = 0;
			long[] index2 = new long[4];
			for (int j = 0; j < 4; j++) {
				if (j != i) {

					remain = remain + (index1[j] << 48 - 16 * (count + 1));

					count++;
				}
			}

			String[] index2Key = new String[4];

			for (int j = 0; j < 4; j++) {
				index2[j] = remain & (MASK12 << (48 - 12 * (j + 1)));
				index2[j] = index2[j] >>> 48 - 12 * (j + 1);
				index2Key[j] = INDEX_2_LEV + j + "_" + index2[j];
				sk[indexSk] = index1Key[i] + "_" + index2Key[j];
				indexSk++;
			}
		}
		return sk;
	}

	/**
	 * 获取所有输入的hashcode的相似hashcode(同步)，并且为这些hashcode建立索引（异步，结果在List<Future<
	 * StageRt>>中， 在合适的时间去检验建立索引的结果）。
	 * 
	 * @param hashcodes
	 *            待查询的hashcode
	 * @param IkvClient
	 * @param simHashcodesRt
	 *            查询到的所有相似的hashcode
	 * @return List<Future<StageRt>> 建立索引的结果通知。
	 * **/
	public static List<Future<StageRt>> getHashcodes(Set<Long> hashcodes,
			IkvLiteClient ikvClient, Set<Long> simHashcodesRt, boolean isWritable) {
		List<String> allHashcodesIndex = new ArrayList<String>();
		Map<String, List<Long>> index2Hashcodes = new HashMap<String, List<Long>>();
		for (long hashcode : hashcodes) {
			String[] hashcodeIndexes = getHashCodeSearchKey(hashcode);
			for (String index : hashcodeIndexes) {
				List<Long> codes = index2Hashcodes.get(index);
				if (codes == null) {
					codes = new ArrayList<Long>();
					index2Hashcodes.put(index, codes);
				}
				codes.add(hashcode);
				allHashcodesIndex.add(index);
			}
		}
 		String[] indexes = new String[allHashcodesIndex.size()];
		allHashcodesIndex.toArray(indexes);
		Map<String, String> index2HascodeArr = new HashMap<String, String>();
		List<Future<StageRt>> searchRt = new ArrayList<Future<StageRt>>();
		for (String index : indexes) {
			searchRt.add(StageExcutor.sumbmitTask(new SelectTask(ikvClient,
					index)));
		}
		for (Future<StageRt> srt : searchRt) {
			StageRt rt;
			try {
				rt = srt.get();
				if (rt.getRt() != null) {
					index2HascodeArr.put(rt.getParam(), rt.getRt());
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (String k : index2HascodeArr.keySet()) {
			JSONArray t = JSONArray.fromObject(index2HascodeArr.get(k));
			List<Long> targetCodes = index2Hashcodes.get(k);
			for (int h = 0; h < t.size(); h++) {
				boolean flag = false;
				for (long targetCode : targetCodes) {
					long dis = targetCode ^ t.getLong(h);
					if (Long.bitCount(dis) <= 3)
						flag = true;
				}
				if(flag)
					simHashcodesRt.add(t.getLong(h));
			}
		}
		// simHashcodesRt.addAll(index2HascodeArr.values());
		if (isWritable) {
			List<Future<StageRt>> fts = new ArrayList<Future<StageRt>>();
			for (String hashIndex : index2Hashcodes.keySet()) {
				List<Long> hcs = index2Hashcodes.get(hashIndex);
				String js = index2HascodeArr.get(hashIndex);
				JSONArray harr = null;

				if (js == null) {
					harr = new JSONArray();
					harr.addAll(hcs);
					fts.add(StageExcutor.sumbmitTask(new SetTask(hashIndex,
							harr.toString(), ikvClient)));
				} else {
					JSONArray harrTemp = JSONArray.fromObject(js);
					Set<Long> s = new HashSet<Long>();
					for (int k = 0; k < harrTemp.size(); k++) {
						s.add(harrTemp.getLong(k));
					}
					s.addAll(hcs);
					harr = JSONArray.fromObject(s);
					if (s.size() > harrTemp.size()) {
						fts.add(StageExcutor.sumbmitTask(new SetTask(hashIndex,
								harr.toString(), ikvClient)));
					}

				}

			}
			return fts;
		}
		else
			return null;

	}

	public static List<Future<StageRt>> getIds(Set<Long> hashcodes,
			IkvLiteClient ikvClient, Set<String> ids, String id, boolean isWritable) {
		String[] hashcodes_str = new String[hashcodes.size()];
		int i = 0;
		for (Long hashcode : hashcodes) {
			hashcodes_str[i] = hashcode.toString();
			i++;
		}

		Map<String, String> rt = ikvClient.gets(hashcodes_str);
		for(String jsonArray: rt.values()){
			JSONArray idArray = JSONArray.fromObject(jsonArray);
			for(int k=0; k < idArray.size();k++){
				ids.add(idArray.getString(k));
			}
		}
//		ids.addAll(rt.values());
		if (isWritable) {
			List<Future<StageRt>> fts = new ArrayList<Future<StageRt>>();
			for (Long hashcode : hashcodes) {
				// List<Long> hcs=index2Hashcodes.get(hashIndex);
				String js = rt.get(hashcode.toString());
				JSONArray harr = null;

				if (js == null) {
					harr = new JSONArray();
					harr.add(id);
					fts.add(StageExcutor.sumbmitTask(new SetTask(hashcode
							.toString(), harr.toString(), ikvClient)));
				} else {
					JSONArray harrTemp = JSONArray.fromObject(js);
					Set<String> s = new HashSet<String>();
					for (int k = 0; k < harrTemp.size(); k++) {
						s.add(harrTemp.getString(k));
					}
					s.add(id);
					harr = JSONArray.fromObject(s);
					if (s.size() > harrTemp.size()) {
						fts.add(StageExcutor.sumbmitTask(new SetTask(hashcode
								.toString(), harr.toString(), ikvClient)));
					}

				}

			}
			return fts;
		}
		else 
			return null;
	}

	private static class SelectTask implements Callable<StageRt> {
		private IkvLiteClient ikvClient;

		private String hashIndex;

		public SelectTask(IkvLiteClient ikvClient, String index) {

			this.hashIndex = index;
			this.ikvClient = ikvClient;

		}

		public StageRt call() throws Exception {

			// TODO Auto-generated method stub
			String hArrStr = ikvClient.get(hashIndex);
			StageRt rt = new StageRt(TASK_NAME.SELECT_HASH, hArrStr);
			rt.setParam(hashIndex);
			return rt;

		}

	}
	private static class SetTask implements Callable<StageRt> {
		private IkvLiteClient ikvClient;

		private String hashIndex;
		private String hashcodeArr;

		public SetTask(String hashIndex, String hashcodeArr,
				IkvLiteClient ikvClient) {
			this.ikvClient = ikvClient;
			this.hashIndex = hashIndex;
			this.hashcodeArr = hashcodeArr;
		}

		public StageRt call() throws Exception {

			// TODO Auto-generated method stub

			ikvClient.put(hashIndex, hashcodeArr);

			return new StageRt(TASK_NAME.SELECT_HASH, "ok");

		}

	}
}
