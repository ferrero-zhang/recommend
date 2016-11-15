package com.ifeng.iRecommend.featureEngineering.RawKeywordsExtract;

import java.util.Iterator;

/**
 * <PRE>
 * 作用 : 
 *   min-max归一化方法
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2015年10月10日        liu_yi          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
public class MinMaxNorm {
	private final double min;
	private final double max;
	private final double avg;
	
	private final int count;

	private MinMaxNorm(double minValue, double avg, double maxValue, int count) {
		if (minValue > maxValue) {
			throw new IllegalArgumentException("minValue > maxValue");
		}
		if (avg > maxValue) {
			throw new IllegalArgumentException("avg > maxValue");
		}
		if (avg < minValue) {
			throw new IllegalArgumentException("avg < minValue");
		}
		this.avg = avg;
		this.min = minValue;
		this.max = maxValue;
	
		this.count = count;
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public double getAvg() {
		return avg;
	}
	
	public int getCount() {
		return count;
	}
	
	public static MinMaxNorm createFor(Iterable<Double> values) {
		return createFor(values.iterator());
	}

	public static MinMaxNorm createFor(Iterator<Double> it) {
		if (!it.hasNext()) {
			return new MinMaxNorm(Double.NaN, Double.NaN, Double.NaN, 0);
		}
		double max = Double.NaN;
		double min = Double.NaN;
		double sum = 0;
		int count = 0;

		while (it.hasNext()) {
			double v = it.next();
			if (Double.isNaN(max) || v > max) {
				max = v;
			}
			if (Double.isNaN(min) || v < min) {
				min = v;
			}
			sum += v;
			count++;
		}
		
		return new MinMaxNorm(min, sum / count, max, count);
	}

	public double normalize(double value) {
		checkInterval(value, min, max);
		if (Double.isNaN(value)) {
			return Double.NaN;
		}
		if (getMax() == getMin())
			return 1.0;
		double rv = (value - getMin()) / (getMax() - getMin());
		checkNormalized(value, rv);
		return rv;
	}

	private void checkNormalized(double input, double normalized)
			throws AssertionError {
		checkNormalized(input, normalized, 0.0, 1.0);
	}

	private void checkNormalized(double input, double normalized, double min,
			double max) throws AssertionError {
		if (!(normalized >= min && normalized <= max)) {
			throw new AssertionError("Normalized value must be between " + min
					+ " and " + max + ". " + "Input value: " + input + ", "
					+ "Normalized value: " + normalized + ". " + this);
		}
	}

	private void checkInterval(double value, double min, double max) {
		if (value < min || value > max) {
			throw new IllegalArgumentException("Value must be between " + min
					+ " and " + max + ". Actual value = " + value);
		}
	}

	public double denormalize(double normalized) {
		if (Double.isNaN(normalized)) {
			return Double.NaN;
		}
		if (getMax() == getMin())
			return getMin();

		double rv = getMin() + (normalized * (getMax() - getMin()));
		return rv;
	}
}
