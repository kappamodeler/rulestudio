/**
 * 
 */
package com.plectix.rulestudio.editors.view.model;

/**
 * @author bill
 *
 */
public class SiteObject extends OutlineObject {
	
	private boolean any = false;
	private boolean wild = false;
	private int bond = 0;
	/**
	 * @param parent
	 * @param label
	 * @param searchString
	 * @param type
	 */
	public SiteObject(OutlineObject parent, String label) {
		super(parent, label, null, OutlineType.SITE);
	}
	
	public void setAny(boolean any) {
		this.any = any;
	}
	
	public boolean isAny() {
		return any;
	}
	
	public void setWild(boolean wild) {
		this.wild = wild;
	}
	
	public boolean isWild() {
		return wild;
	}
	
	public void setBond(int bond) {
		this.bond = bond;
	}
	
	public int bond() {
		return bond;
	}
	
	

	
}
