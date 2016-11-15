
package com.ifeng.iRecommend.topicmodel;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

/*
 * 
 * topic matrix：<word或者doc> --> topic向量
 * 作用 : 
 *   
 * 使用 : 
 *   
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2012-10-24        mayk          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */

public class TopicMatrix {
	private double[][] matrix;//主题矩阵
	private int topicNum;//主题数
	private int size;//doc或word数目；
	
	public TopicMatrix(int size, int tnum) {
		this.topicNum = tnum;
		matrix = new double[size][topicNum];
		this.size = size;
	}

	public int getTopicNum() {
		return this.topicNum;
	}

	public void put(int row, int col, double val) {
		matrix[row][col] = val;
	}
	//归一化
	public void normalize(int row, double sum) {
		for (int i = 0; i < topicNum; ++i)
			matrix[row][i] /= sum;

	}

	//dot product
	public Double dotProducts(int left, int right) {
		double product = 0.0;
		if (left < 0 || left > topicNum - 1 || right < 0
				|| right > topicNum - 1)
			return null;
		for (int i = 0; i < topicNum; ++i) {
			product += matrix[left][i] * matrix[right][i];
		}
		return product;

	}

	public double[] getTopicVector(int id) {
		return matrix[id];
	}

	public void ReadFromFile(File file) {
		TopicMatrixReader.ReadInData(file);
	}


	
	
	/**
	 * add likun;
	 * 按权重合并两个主题向量，结果储存与ltopic
	 * 
	 * @param ltopic
	 * @param rtopic
	 * @param alpha
	 */
	public static final void mergeTopics(double[] ltopic, double[] rtopic,
			double alpha) {
		for (int i = 0; i < ltopic.length; ++i)
			ltopic[i] = (1 - alpha) * ltopic[i] + alpha * rtopic[i];

	}

}
