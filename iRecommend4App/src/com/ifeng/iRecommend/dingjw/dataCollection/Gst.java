package com.ifeng.iRecommend.dingjw.dataCollection;

import java.util.ArrayList;
import java.util.List;

public class Gst
{
	private final int MIN_LENGTH = 2;

	List<Stats> titles = new ArrayList<Stats>();
	List<Stats> matchs = new ArrayList<Stats>();

	// GST算法实现
	public void calculateGST(String pwd1, String pwd2)
	{
		int maxlength, count;
		int len1 = pwd1.length();
		int len2 = pwd2.length();

		// 辅助标记变量
		int[] mark1 = new int[len1];
		int[] mark2 = new int[len2];
		for (int i = 0; i < len1; i++)
			mark1[i] = 0;
		for (int j = 0; j < len2; j++)
			mark2[j] = 0;

		// algorithm
		do
		{
			matchs.clear();
			maxlength = MIN_LENGTH;

			for (int i = 0; i < len1-1; i++)
			{
				if (mark1[i] == 1)
					continue;
				for (int j = 0; j < len2-1; j++)
				{
					if (mark2[j] == 1)
						continue;

					count = 0;
					while ((i + count)<len1 && (j + count)<len2 && pwd1.charAt(i + count) == pwd2.charAt(j + count) && mark1[i + count] != 1 && mark2[j + count] != 1)
					{
						count++;
					}
					if (count == maxlength)
						this.matchs.add(new Stats(i, j, count));
					else if (count > maxlength)
					{
						this.matchs.clear();
						this.matchs.add(new Stats(i, j, count));
						maxlength = count;
					}
				}
			}

			// 标记阶段,标记前面找到的最大匹配，防止重复使用
			for (Stats stats : this.matchs)
			{
				int pos1 = stats.getPosFirst();
				int pos2 = stats.getPosSecond();
				int length = stats.getLength();

				for (int i = 0; i < length; i++)
				{
					mark1[pos1 + i] = 1;
					mark2[pos2 + i] = 1;
				}
				this.titles.add(stats);
			}

		} while (maxlength != MIN_LENGTH);
		/*for(Stats stat : titles)
		{
			System.out.println("first : "+stat.getPosFirst()+" second : "+stat.getPosSecond()+" third : "+stat.getLength()+"");
		}*/
	}

	class Stats
	{
		private int posFirst;
		private int posSecond;
		private int length;

		public Stats(int posM, int posN, int length)
		{
			this.posFirst = posM;
			this.posSecond = posN;
			this.length = length;
		}

		public int getPosFirst()
		{
			return posFirst;
		}

		public int getPosSecond()
		{
			return posSecond;
		}

		public int getLength()
		{
			return length;
		}
	}

	// 输出找到的匹配串供测试
	public String getTitles(String pwd1)
	{
		StringBuffer buf = new StringBuffer();
		for (Stats stats : this.titles)
		{
			int pos1 = stats.getPosFirst();
			int length = stats.getLength();
			for (int i = 0; i < length; i++)
			{
				buf.append(pwd1.charAt(pos1 + i));
			}
			buf.append("\t");
		}
		return buf.toString();
	}

	// 计算相似度
	public double calSimilarity(String pwd1, String pwd2)
	{
		int length = 0;
		String[] vals = getTitles(pwd1).split("\t");
		for (String val : vals)
		{
			length += val.length();
		}

		return (2.0 * length) / (pwd1.length() + pwd2.length());
	}

}
