package com.ifeng.iRecommend.dingjw.dataCollection;

public class EditDistance
{
	private static int Minimum(int a, int b, int c)
	{
		int mi;
		mi = a;
		if (b < mi)
		{
			mi = b;
		}
		if (c < mi)
		{
			mi = c;
		}
		return mi;
	}

	public static int getEditDistance(String s, String t)
	{
		int d[][]; // matrix
		int n; // 原串s的长度
		int m; // 对比串t的长度
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost
		// Step 1
		//获取字符串长度，构建路径矩阵
		n = s.length();
		m = t.length();
		if (n == 0)
		{
			return m;
		}
		if (m == 0)
		{
			return n;
		}
		d = new int[n + 1][m + 1];

		// Step 2
		//初始化矩阵，第一行第一列

		for (i = 0; i <= n; i++)
		{
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++)
		{
			d[0][j] = j;
		}

		// Step 3

		for (i = 1; i <= n; i++)
		{
			s_i = s.charAt(i - 1);
			// Step 4
			for (j = 1; j <= m; j++)
			{
				t_j = t.charAt(j - 1);
				// Step 5
				if (s_i == t_j)
				{
					cost = 0;
				}
				else
				{
					cost = 1;
				}
				// Step 6
				d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);
			}
		}
		// Step 7
		return d[n][m];
	}
	public static double similarity(String str1, String str2)
	{
		int min = getEditDistance(str1, str2);
		double similarity = 1 - (double) min / ((str1.length() + str2.length()) / 2);
		return similarity;
		
	}
	public static void main(String[] args  )
	{
		String str1 = "西门子因商业贿赂遭调查";
		String str2 = "西门子工商总局调查:涉行贿千家医院";
		int x = EditDistance.getEditDistance(str1, str2);
		System.out.println(x);
	}
}
