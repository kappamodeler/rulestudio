package com.plectix.rulestudio.views.simulator;

import java.io.Serializable;

public class DataPoint implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double time;
	private double[] values;

	public DataPoint(double t, double[] v) {
		this.time = t;
		this.values = v;
	}

	public double getTime() {
		return time;
	}

	public double[] getValues() {
		return values;
	}

}
