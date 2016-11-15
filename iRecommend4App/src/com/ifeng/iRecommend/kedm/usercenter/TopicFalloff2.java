package com.ifeng.iRecommend.kedm.usercenter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TopicFalloff2 {
	private double notcome = 1.2;//没来的一周衰减因子
	private double comenoteread = 1.5;//来了没看的一周衰减因子
	private String current;
	public String proccess(List<String> addedTopic){
		if(current == null && addedTopic == null)
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
				if(temp.length != 4)
					continue;
				if(currentTopic_map.containsKey(temp[0])){
					String[] tempvalue = currentTopic_map.get(temp[0]).split("_");
					String resvalue = tempvalue[0]+"_"+(Double.parseDouble(tempvalue[1])+Double.parseDouble(temp[1]))
							+ "_" +(Double.parseDouble(tempvalue[2])+Double.parseDouble(temp[2]))
							+ "_" +Double.parseDouble(tempvalue[3]);
					currentTopic_map.put(temp[0], resvalue);
				}else{
					currentTopic_map.put(temp[0], tagscore);
				}
				cur_tag.add(temp[0]);
			}
		}
		if(addedTopic == null || addedTopic.isEmpty()){
			DecimalFormat df = new DecimalFormat("#0.00");
			StringBuffer sb = new StringBuffer();
			for(String tag : currentTopic_map.keySet()){
				String[] topic = currentTopic_map.get(tag).split("_");
				double count = Double.parseDouble(topic[1]) - notcome*0.3;
				double score = Double.parseDouble(topic[2]) - notcome*0.03;
				if(count > 0){
					sb.append(tag).append("_").append(count).append("_");
					if(score <= 0){
						score = 0.1;
					}
					sb.append(df.format(score)).append("_").append(topic[3]).append("#");
				}
			}
			this.current = sb.toString();
			//System.out.println(this.current);
			notcome = notcome *1.2;
		}else{
			for(String tagscores : addedTopic){
				String[] temp_tagscores = tagscores.split("#");
				for(String tagscore : temp_tagscores){
					String[] temp = tagscore.split("_");
					if(temp.length != 3)
						continue;
					if(currentTopic_map.containsKey(temp[0])){
						String[] tempvalue = currentTopic_map.get(temp[0]).split("_");
						
						String resvalue = tempvalue[0]+"_"+(Double.parseDouble(tempvalue[1])+Double.parseDouble(temp[1]))
								+ "_" +(Double.parseDouble(tempvalue[2])+Double.parseDouble(temp[2]))
								+ "_" + (Double.parseDouble(tempvalue[3]));
						currentTopic_map.put(temp[0], resvalue);
					}else{
						currentTopic_map.put(temp[0], tagscore+"_"+comenoteread);
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
					double count = Double.parseDouble(topic[1]) - Double.parseDouble(topic[3])*0.3;
					double score = Double.parseDouble(topic[2]) - Double.parseDouble(topic[3])*0.03;
					if(count > 0 && score > 0){
						sb.append(tag).append("_").append(count).append("_");
						sb.append(df.format(score)).append("_").append(comenoteread*Double.parseDouble(topic[3])).append("#");
					}
				}else{
					String[] topic = currentTopic_map.get(tag).split("_");
					sb.append(tag).append("_").append(topic[1]).append("_").append(topic[2]).append("_").append(comenoteread).append("#");
				}
			}
			this.current = sb.toString();
			//System.out.println(this.current);
			notcome = 1.2;
		}
		return this.current;
		
	}

}
