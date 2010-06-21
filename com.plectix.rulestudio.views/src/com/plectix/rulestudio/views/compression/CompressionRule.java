/**
 * 
 */
package com.plectix.rulestudio.views.compression;

import java.util.ArrayList;

/**
 * @author bill
 *
 */
public class CompressionRule {
	private String id;
	private String name;
	private String data;
	private String rate;
	private ArrayList<String> map = null;
	private int offset = -1;
	
	public CompressionRule(String id, String name, String data, String rate) {
		this.id = id;
		this.name = name;
		this.data = data;
		this.rate = rate;
	}
	
	public void addMap(String id) {
		if (map == null) {
			map = new ArrayList<String>();
		}
		map.add(id);
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getData() {
		return data;
	}

	public String getRage() {
		return rate;
	}
	
	public ArrayList<String> getMap() {
		return map;
	}
	
	public void setOffset(int off) {
		offset = off;
	}
	
	public int getOffset() {
		return offset;
	}

}
