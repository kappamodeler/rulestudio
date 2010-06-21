/**
 * 
 */
package com.plectix.rulestudio.views.reachable;

import java.util.ArrayList;

/**
 * @author bill
 *
 */
public class Reachable {
	private ArrayList<ArrayList<String>> views;
	private ArrayList<ArrayList<String>> subviews;
	private ArrayList<String> species;
	private ArrayList<String> active = null;
	
	public Reachable() {
	}
	
	public void addViewAgent(String name) {
		if (views == null)
			views = new ArrayList<ArrayList<String>>();
		ArrayList<String> entry = new ArrayList<String>();
		entry.add("Agent: " + name);
		views.add(entry);
		active = entry;
	}
	
	public void addSubViewAgent(String name) {
		if (subviews == null)
			subviews = new ArrayList<ArrayList<String>>();
		ArrayList<String> entry = new ArrayList<String>();
		entry.add(name);
		subviews.add(entry);
		active = entry;		
	}
	
	public void startSpecies() {
		species = new ArrayList<String>();
		active = species;
	}
	
	public void addEntry(String name) {
		if (active == null) {
			throw new NullPointerException("Null reachable list");
		}
		active.add(name);
	}
	
	public ArrayList<ArrayList<String>> getViews() {
		return views;
	}
	
	public ArrayList<ArrayList<String>> getSubViews() {
		return subviews;
	}
	
	public ArrayList<String> getSpecies() {
		return species;
	}
	

	
}
