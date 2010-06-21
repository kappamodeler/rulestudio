package com.plectix.rulestudio.editors.view.model;

import org.eclipse.swt.graphics.Image;

public enum OutlineType {
	
	MODEL(OutlineObject.MODEL_IMAGE),
	RULE(OutlineObject.RULE_IMAGE),
	ARROW(OutlineObject.LEAF_IMAGE),
	AGENT(OutlineObject.AGENT_IMAGE),
	SITE(OutlineObject.LEAF_IMAGE),
	INIT(OutlineObject.AGENT_IMAGE),
	OBS(OutlineObject.AGENT_IMAGE),
	STORY(OutlineObject.SIM_IMAGE),
	MOD(OutlineObject.SIM_IMAGE),
	LABEL(OutlineObject.LEAF_IMAGE),
	EXP(OutlineObject.MODEL_IMAGE),
	;
	
	private Image image;
	
	private OutlineType(Image image) {
		this.image = image;
	}
	
	public Image getImage() {
		return image;
	}

}
