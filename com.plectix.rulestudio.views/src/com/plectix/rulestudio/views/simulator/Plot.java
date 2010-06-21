package com.plectix.rulestudio.views.simulator;

import java.io.Serializable;

public class Plot implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String type;
	private String name;
	private boolean display = true;
	

	public Plot(String t, String n) {
		type = t;
		name = n;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	
	public void setDisplay(boolean value) {
		display = value;
	}
	
	public boolean getDisplay() {
		return display;
	}

}
