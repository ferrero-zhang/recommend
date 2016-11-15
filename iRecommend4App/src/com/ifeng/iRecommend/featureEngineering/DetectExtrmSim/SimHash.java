package com.ifeng.iRecommend.featureEngineering.DetectExtrmSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimHash {
	// private NlpirSpliter sp;
	// private SimpleSpliter sp;
	private static Map<String, Integer> POS_WEIGHT;

	public SimHash() {
		/*
		 * sp=new NlpirSpliter (); try { sp.init(); } catch (Exception e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); }
		 */
		POS_WEIGHT = new HashMap<String, Integer>();
		POS_WEIGHT.put("n", 2);
		POS_WEIGHT.put("v", 2);
		POS_WEIGHT.put("a", 2);
		// POS_WEIGHT.put("tu", 5);
	}

	// private static Set<String> FILT_POS;

	// static{
	// FILT_POS=new HashSet<String>();
	// }

	public Set<Long> generateHashCodeForStr(String[] paragraphs) {
		int maxSize = paragraphs.length;
		if(maxSize >= 40)
			maxSize = 40;
		Set<Long> hashCodeLists = new HashSet<Long>(maxSize);
		for (int j = 0; j < maxSize; j++) {
			if(paragraphs[j] == null || paragraphs[j].isEmpty())
				continue;
//			System.out.println(paragraphs[j]);
			String[] clearWords = paragraphs[j].toString().trim().split(" ");			
			int[] weights = new int[64];
			for (int i = 0; i < clearWords.length; i++) {
				FNV64 hash = new FNV64();
				byte[] wbs = clearWords[i].getBytes();
				hash.update(wbs, 0, wbs.length);
				long hashcode = hash.getValue();
				// System.out.println(word+"---"+hashcode);
				int weight = 1;
				// if(POS_WEIGHT.containsKey(pos)){
				// weight=POS_WEIGHT.get(pos);
				// }
//				if (weightedWords.containsKey(word)) {
//					// System.out.println(word);
//					weight = weightedWords.get(word);
//				}

				for (int k = 0; k < 64; k++) {

					if (hashcode % 2 == 0) {
						weights[63 - k] += 0 - weight;
					} else {
						weights[63 - k] += weight;
					}
					/*
					 * if(Long.toBinaryString(hashcode).length()==2){
					 * System.out.println("zxc"); }
					 * System.out.println(Long.toBinaryString(hashcode));
					 */
					hashcode = hashcode >>> 1;
					/*
					 * System.out.println(Long.toBinaryString(hashcode));
					 * System.out.println("---");
					 */
				}
			}
			long hashcode = 0;
			for (int i = 0; i < 64; i++) {
				if (weights[i] > 0) {
					long k = 1l << (63 - i);
					hashcode |= k;
				}
			}
			hashCodeLists.add(hashcode);
//			System.out.println(hashcode);
		}
		

		return hashCodeLists;
	}
	/*
	 * public long getHashCode(String title,String content/*,ArticleInfo ar){
	 * if(content.length()==0){ content=title; } if(content.length()==0){ throw
	 * new RuntimeException("content len=0"); } while(content.length()<1000){
	 * content=content.concat(content); } String splitTitle=sp.split(title);
	 * String splitContent=sp.split(content); long
	 * hashcode=generateHashCodeForStr(splitTitle,splitContent); //
	 * System.out.println("=============="); return hashcode; }
	 */
	/*
	 * public void close(){ sp.close(); }
	 */
}
