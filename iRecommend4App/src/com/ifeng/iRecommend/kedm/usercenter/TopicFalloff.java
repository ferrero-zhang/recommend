package com.ifeng.iRecommend.kedm.usercenter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TopicFalloff {
	private String current;
	public String proccess(List<String> addedTopic){
		if(null == addedTopic)
			return null;
		List<String> waitToFalloffTag = new ArrayList<String>();
		List<String> newTag = new ArrayList<String>();
		Map<String,String> currentTopic_map = new HashMap<String,String>();
		Map<String,String> addedTopic_map = new HashMap<String,String>();
		Set<String> cur_tag = new HashSet<String>();
		Set<String> add_tag = new HashSet<String>();
		
		if(current != null){
			String[] temp_currenttag = current.split("#");
			for(String tagscore : temp_currenttag){
				String[] temp = tagscore.split("_");
				if(temp.length != 3)
					continue;
				if(currentTopic_map.containsKey(temp[0])){
					String[] tempvalue = currentTopic_map.get(temp[0]).split("_");
					String resvalue = tempvalue[0]+"_"+(Integer.parseInt(tempvalue[1])+Integer.parseInt(temp[1]))
							+ "_" +(Double.parseDouble(tempvalue[2])+Double.parseDouble(temp[2]))
							+ "_0";
					currentTopic_map.put(temp[0], resvalue);
				}else{
					currentTopic_map.put(temp[0], tagscore+"_0");
				}
				cur_tag.add(temp[0]);
			}
		}
		
		
		for(String tagscores : addedTopic){
			String[] temp_tagscores = tagscores.split("#");
			for(String tagscore : temp_tagscores){
				String[] temp = tagscore.split("_");
				if(temp.length != 3)
					continue;
				if(currentTopic_map.containsKey(temp[0])){
					String[] tempvalue = currentTopic_map.get(temp[0]).split("_");
					String resvalue = tempvalue[0]+"_"+(Integer.parseInt(tempvalue[1])+Integer.parseInt(temp[1]))
							+ "_" +(Double.parseDouble(tempvalue[2])+Double.parseDouble(temp[2]))
							+ "_" + (Integer.parseInt(tempvalue[3])+1);
					currentTopic_map.put(temp[0], resvalue);
				}else{
					currentTopic_map.put(temp[0], tagscore+"_1");
				}
				add_tag.add(temp[0]);
			}
			
		}
		for(String tag : cur_tag){
			if(!add_tag.contains(tag)){
				waitToFalloffTag.add(tag);
			}
		}
		for(String tag : add_tag){
			if(cur_tag == null ||!cur_tag.contains(tag)){
				newTag.add(tag);
			}
		}
		Map<String,Double> tosort = new HashMap<String,Double>();
		DecimalFormat df = new DecimalFormat("#0.00");
		StringBuffer sb = new StringBuffer();
		for(String tag : currentTopic_map.keySet()){
			if(waitToFalloffTag.contains(tag)){
				String[] topic = currentTopic_map.get(tag).split("_");
				int count = Integer.parseInt(topic[1]);
				double score = Double.parseDouble(topic[2]);
				if(count > 10){
					sb.append(tag).append("_").append(count-10).append("_");
					if(score > 5){
						score = score -5;
					}else{
						score = 0.1;
					}
					sb.append(df.format(score)).append("#");
				}
			}else if(newTag.contains(tag)){
				String[] topic = currentTopic_map.get(tag).split("_");
				sb.append(tag).append("_").append(topic[1]).append("_").append(topic[2]).append("#");
			}else{
				String[] topic = currentTopic_map.get(tag).split("_");
				int num = 7 - Integer.parseInt(topic[3]);
				int count = Integer.parseInt(topic[1]) - 1*num;
				double score = Double.parseDouble(topic[2]) - 0.3*num;
				if(count > 0){
					if(score < 0){
						score = 0.1;
					}
					sb.append(tag).append("_").append(count).append("_").append(df.format(score)).append("#");
				}
			}
		}
		this.current = sb.toString();
		return this.current;
	}

}
