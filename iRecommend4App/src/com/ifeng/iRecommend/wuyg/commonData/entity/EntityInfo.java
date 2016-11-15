package com.ifeng.iRecommend.wuyg.commonData.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;

public class EntityInfo {

	private String word = null; // 术语

	private int count = 0; // 热度

	private ArrayList<String> nicknameList = new ArrayList<String>(); // 别名

	private ArrayList<String> levels = new ArrayList<String>(); // 实体的级别信息

	private String category = null; // 所属类别

	private String filename = null; // 该实体所属的文件名

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public ArrayList<String> getNicknameList() {
		return nicknameList;
	}

	public void setNicknameList(ArrayList<String> nicknameList) {
		this.nicknameList = nicknameList;
	}

	public ArrayList<String> getLevels() {
		return levels;
	}

	public void setLevels(ArrayList<String> levels) {
		this.levels = levels;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public boolean contains(ArrayList<String> list, String key) {
		for (String str : list) {
			if (str.equalsIgnoreCase(key)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object object) {
		// TODO Auto-generated method stub
		boolean isEquals = false;
		EntityInfo obj = null;
		if (object instanceof EntityInfo) {
			obj = (EntityInfo) object;
		} else {
			return isEquals;
		}
		if (this.getWord().equals(obj.getWord())
				&& this.getCategory().equals(obj.getCategory())
				&& this.getCount() == obj.getCount()
				&& this.getFilename().equals(obj.getFilename())
				&& this.getLevels().equals(obj.getLevels())
				&& this.getNicknameList().equals(obj.getNicknameList())) {
			isEquals = true;
		}
		return isEquals;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "word:" + word + "  count:" + count + "  category:" + category
				+ " filename:" + filename.substring(filename.indexOf("entLib_")+"entLib_".length()) + "  levels:" + levels + "  nicks:"
				+ nicknameList;
	}

	public String toredisFormat() {
		// TODO Auto-generated method stub
		return "w:" + word + "#num:" + count + "#c:" + category + "#ls:"
				+ levels + "#ns:" + nicknameList;
	}

	/**
	 * 将普通的record格式转为redis数据更新格式
	 * 
	 * @param entityStr
	 * @return
	 */
	public static String str2redisFormat(String entityStr) {
		String word = entityStr.substring(
				entityStr.indexOf("word:") + "word:".length(),
				entityStr.indexOf("  count:"));

		String count = entityStr.substring(entityStr.indexOf("count:")
				+ "count:".length(), entityStr.indexOf("  category:"));
		int icount = Integer.valueOf(count);

		String category = entityStr.substring(entityStr.indexOf("category:")
				+ "category:".length(), entityStr.indexOf(" filename:"));

		String filenames = entityStr.substring(entityStr.indexOf("filename:")
				+ "filename:".length(), entityStr.indexOf("  levels:"));

		String levels = entityStr.substring(entityStr.indexOf("levels:[")
				+ "levels:[".length(), entityStr.indexOf("]  nicks:"));

		List<String> levelList = commenFuncs.getList(levels);

		if (!levelList.get(0).equalsIgnoreCase(category)) {
			levelList.add(0, category);
		}

		if (!levelList.get(levelList.size() - 1).contains("术语")) {
			levelList.add("术语");
		}

		String nicks = entityStr.substring(entityStr.indexOf("nicks:[")
				+ "nicks:[".length());

		nicks = nicks.substring(0, nicks.indexOf("]"));

		List<String> nickList = commenFuncs.getList(nicks);

		nickList.remove(word);

		return toUpdateRedisFormat(word, icount, category, levelList,
				filenames, nickList);
	}

	/**
	 * 将需要提交更新的record格式切换成redis的标准数据更新格式
	 * 
	 * @param word
	 * @param count
	 * @param category
	 * @param levels
	 * @param filename
	 * @param nicknameList
	 * @return
	 */
	private static String toUpdateRedisFormat(String word, int count,
			String category, List<String> levels, String filename,
			List<String> nicknameList) {
		/**
		 * entLib_地产人物@w:张欣#num:0#c:财经#ls:[财经, 地产人物, 人物术语]#ns:[]
		 */

		return "entLib_" + filename + LoadConfig.lookUpValueByKey("filenameDelimiter") + "w:" + word + "#num:" + count
				+ "#c:" + category + "#ls:" + levels + "#ns:" + nicknameList;
	}

	/**
	 * 将术语的redis存储格式转为正常的record格式
	* Title:
	* Description:
	* @param entityInfo
	* @return
	* author:wuyg1
	  date:2015年12月15日
	 */
	public static String toEntityStr(EntityInfo entityInfo) {
		// TODO Auto-generated method stub
		return "word:" + entityInfo.word + "  count:" + entityInfo.count
				+ "  category:" + entityInfo.category + " filename:"
				+ entityInfo.filename + "  levels:" + entityInfo.levels
				+ "  nicks:" + entityInfo.nicknameList;
	}

	public static String redisFormat2Str(String entityStr,String filename) {
		String[] words = entityStr.split("#");
		EntityInfo entityInfo = new EntityInfo();
		// System.err.println(record);
		for (String word : words) {
			if (word.startsWith("w:")) {
				String value = word.substring(word.indexOf("w:")
						+ "w:".length());
				entityInfo.setWord(value);
				continue;
			}
			if (word.startsWith("num")) {
				String value = word.substring(word.indexOf("num:")
						+ "num:".length());
				entityInfo.setCount(Integer.valueOf(value));
				continue;
			}
			if (word.startsWith("c")) {
				String value = word.substring(word.indexOf("c:")
						+ "c:".length());
				entityInfo.setCategory(value);
				continue;
			}
			if (word.startsWith("ls")) {
				String value = word.substring(word.indexOf("ls:")
						+ "ls:".length());
				value = value.substring(value.indexOf("[") + 1,
						value.indexOf("]"));
				ArrayList<String> levelList = new ArrayList<String>(
						Arrays.asList(value.split(", ")));
				entityInfo.setLevels(levelList);
				continue;
			}
			if (word.startsWith("ns")) {
				String value = word.substring(word.indexOf("ns:")
						+ "ns:".length());
				value = value.substring(value.indexOf("[") + 1,
						value.indexOf("]"));
				ArrayList<String> nickList = new ArrayList<String>(
						Arrays.asList(value.split(", ")));
				entityInfo.setNicknameList(nickList);
				continue;
			}
		}
		filename = filename.substring(filename.indexOf("entLib_")+"entLib_".length());
		entityInfo.setFilename(filename);
		return toEntityStr(entityInfo);
	}

	public static EntityInfo string2Object(String es) {
		EntityInfo otherInfo = new EntityInfo();
		String word = es.substring(es.indexOf("word:") + "word:".length(),
				es.indexOf("  count:"));

		otherInfo.setWord(word);

		String count = es.substring(es.indexOf("count:") + "count:".length(),
				es.indexOf("  category:"));
		int icount = Integer.valueOf(count);

		otherInfo.setCount(icount);

		String category = es.substring(
				es.indexOf("category:") + "category:".length(),
				es.indexOf(" filename:"));

		otherInfo.setCategory(category);

		String filenames = es.substring(
				es.indexOf("filename:") + "filename:".length(),
				es.indexOf("  levels:"));

		otherInfo.setFilename(filenames);

		String levels = es.substring(
				es.indexOf("levels:[") + "levels:[".length(),
				es.indexOf("]  nicks:"));

		List<String> levelList = commenFuncs.getList(levels);

		if (!levelList.get(0).equalsIgnoreCase(category)) {
			levelList.add(0, category);
		}

		if (!levelList.get(levelList.size() - 1).contains("术语")) {
			levelList.add("术语");
		}

		String nicks = es.substring(es.indexOf("nicks:[") + "nicks:[".length());

		nicks = nicks.substring(0, nicks.indexOf("]"));

		List<String> nickList = commenFuncs.getList(nicks);

		nickList.remove(word);

		otherInfo.getNicknameList().addAll(nickList);

		otherInfo.getLevels().addAll(levelList);

		return otherInfo;
	}

	public static EntityInfo redis2Object(String record, String filename) {
		String[] words = record.split("#");
		EntityInfo entityInfo = new EntityInfo();
		// System.err.println(record);
		for (String word : words) {
			if (word.startsWith("w:")) {
				String value = word.substring(word.indexOf("w:")
						+ "w:".length());
				entityInfo.setWord(value);
				continue;
			}
			if (word.startsWith("num")) {
				String value = word.substring(word.indexOf("num:")
						+ "num:".length());
				entityInfo.setCount(Integer.valueOf(value));
				continue;
			}
			if (word.startsWith("c")) {
				String value = word.substring(word.indexOf("c:")
						+ "c:".length());
				entityInfo.setCategory(value);
				continue;
			}
			if (word.startsWith("ls")) {
				String value = word.substring(word.indexOf("ls:")
						+ "ls:".length());
				value = value.substring(value.indexOf("[") + 1,
						value.indexOf("]"));
				ArrayList<String> levelList = new ArrayList<String>(
						Arrays.asList(value.split(", ")));
				entityInfo.setLevels(levelList);
				continue;
			}
			if (word.startsWith("ns")) {
				String value = word.substring(word.indexOf("ns:")
						+ "ns:".length());
				value = value.substring(value.indexOf("[") + 1,
						value.indexOf("]"));
				ArrayList<String> nickList = new ArrayList<String>(
						Arrays.asList(value.split(", ")));
				entityInfo.setNicknameList(nickList);
				continue;
			}
		}
		//filename = filename.substring(filename.indexOf("entLib_")+"entLib_".length());
		entityInfo.setFilename(filename);
		return entityInfo;
	}
}
