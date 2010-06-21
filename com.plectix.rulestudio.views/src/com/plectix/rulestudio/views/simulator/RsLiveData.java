package com.plectix.rulestudio.views.simulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.plectix.simulator.streaming.LiveData;
import com.plectix.simulator.streaming.LiveDataPoint;

public class RsLiveData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Plot[] plots;
	private ArrayList<DataPoint> data;
	private String log = "";
	private int progress;
	private String model = null;
	private String csv = null;

	public RsLiveData(LiveData liveData) throws Exception {
		int pc = liveData.getNumberOfPlots();
		plots = new Plot[pc];
		for (int i = 0; i < pc; ++i) {
			plots[i] = new Plot(liveData.getPlotTypes()[i].getName(), liveData.getPlotNames()[i]);
		}
		if (liveData != null) {
			Collection<LiveDataPoint> liveDataPoints = liveData.getData();
			if (liveDataPoints != null) {
				data = new ArrayList<DataPoint>(liveDataPoints.size());
				for (LiveDataPoint ldp : liveDataPoints) {
					data.add(new DataPoint(ldp.getEventTime(), ldp.getPlotValues()));

				}
			}
		}
	}
	
	public RsLiveData() {
		// default constructor for file
	}
	
	public void addPlots(Plot[] plots, ArrayList<DataPoint> data) {
		this.plots = plots;
		this.data = data;
	}

	public int count() {
		return plots.length;
	}

	public Plot getPlot(int i) {
		return plots[i];
	}

	public Collection<DataPoint> getPoints() {
		return data;
	}
	
	public void setLog(String log) {
		this.log = log;
	}

	public String getLog() {
		return log;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}
	
	public int getProgress() {
		return progress;
	}
	
	public void setModel(String model) {
		this.model = model;
	}
	
	public String getModel() {
		return model;
	}
	
	public void setCSV(String csv) {
		this.csv = csv;
	}
	
	public String getCSV() {
		StringBuffer buf = new StringBuffer();
		for (Plot plot: plots) {
			buf.append(plot.getType());
			buf.append(',');
			buf.append(plot.getName());
			buf.append(",\n");
		}
		
		return buf.toString() + csv;
	}

}
