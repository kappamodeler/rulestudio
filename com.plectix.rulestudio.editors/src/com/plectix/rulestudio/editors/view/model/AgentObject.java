/**
 * 
 */
package com.plectix.rulestudio.editors.view.model;

import java.util.Map;

/**
 * @author bill
 *
 */
public class AgentObject extends OutlineObject {
	
	private AgentObject outline;				// The outline one
	private int start;							// start of this agent in the line
	private int end;							// end of this agent in the line
	private Map<String, SiteObject> sites;
	private boolean any = false;
	private boolean wild = false;

	/**
	 * @param parent
	 * @param label
	 * @param searchString
	 * @param type
	 */
	public AgentObject(OutlineObject parent, String label, String searchString,
			OutlineType type, AgentObject outline) {
		super(parent, label, searchString, type);
		this.outline = outline;
	}
	
	public void setStart(int start) {
		this.start = start;
	}
	
	public int getStart() {
		return start;
	}
	
	public void setEnd(int end) {
		this.end = end+1;
	}
	
	public int getEnd() {
		return end;
	}
	
	public void setSiteMap(Map<String, SiteObject> map) {
		this.sites = map;
	}
	
	public Map<String, SiteObject> getSiteMap() {
		return sites;
	}
	
	public void updateOutlineAgent() {
		if (outline == null)
			throw new UnsupportedOperationException("Cannot verify outline agent.");
		OutlineObject[] children = getChildren();
		for (OutlineObject site: children) {
			OutlineObject oSite = outline.findChildByLabel(site._label);
			if (oSite == null) {
				OutlineObject newSite = outline.addChild(new SiteObject(outline, site._label));
				for (OutlineObject state: site.getChildren()) {
					newSite.addChild(new OutlineObject(newSite, state._label, "", OutlineType.SITE));
				}
			} else {
				for (OutlineObject state: site.getChildren()) {
					OutlineObject marker = oSite.findChildByLabel(state.getLabel());
					if (marker == null) {
						oSite.addChild(new OutlineObject(oSite, state.getLabel(), "", OutlineType.SITE));
					}
				}
			}
			
		}
	}

	public void setAny() {
		any = true;	
	}
	
	public boolean hasAny() {
		return any;
	}

	public void setWild() {
		wild = true;
	}
	
	public boolean hasWild() {
		return wild;
	}
	
	public AgentObject getMaster() {
		return outline;
	}
	
}
