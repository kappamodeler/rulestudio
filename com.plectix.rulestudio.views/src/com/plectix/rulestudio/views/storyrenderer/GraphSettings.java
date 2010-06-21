package com.plectix.rulestudio.views.storyrenderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;


public class GraphSettings {
	
	public static enum LayoutName {
		ISOMLayout,
		KKLayout,
		TreeLayout
	}
	
	private LayoutName layoutName = LayoutName.TreeLayout;
	private Dimension initialSize = new Dimension(600, 600);  // does not apply to TreeLayout
	private Dimension preferredSize = new Dimension(600, 600); // Sets the viewing area size 

	private int translatingMouseModifier = MouseEvent.BUTTON1_MASK;
	private int pickingMouseModifier1 = InputEvent.BUTTON1_MASK;
	private int pickingMouseModifier2 = InputEvent.BUTTON1_MASK | InputEvent.CTRL_DOWN_MASK;
	private int rotatingMouseModifier = MouseEvent.BUTTON1_MASK | MouseEvent.SHIFT_MASK;
	private int shearingMouseModifier = MouseEvent.BUTTON1_MASK | MouseEvent.META_MASK;
    
	private Paint introColor = Color.GREEN;
	private Paint observableColor = Color.MAGENTA;
	private Paint ruleColor = Color.LIGHT_GRAY;
	
	// private Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{10.0f}, 0.0f); 
	private Stroke edgeStroke = new BasicStroke(1.0f);
    
	public GraphSettings() {
		super();
	}

	public final Paint getPaint(final StoryTreeNode storyTreeNode) {
		final StoryNode.Type type = storyTreeNode.getStoryNode().getType();
		if (type == StoryNode.Type.INTRO) {
			return introColor;
		} else if (type == StoryNode.Type.OBSERVABLE) {
			return observableColor;
		} else if (type == StoryNode.Type.RULE) {
			return ruleColor;
		} else {
			throw new RuntimeException("Unknown Node Type: " + type);
		}
	}
	
	public final LayoutName getLayoutName() {
		return layoutName;
	}

	public final void setLayoutName(LayoutName layoutName) {
		this.layoutName = layoutName;
	}

	public final Dimension getInitialSize() {
		return initialSize;
	}

	public final void setInitialSize(Dimension initialSize) {
		this.initialSize = initialSize;
	}

	public final Dimension getPreferredSize() {
		return preferredSize;
	}

	public final void setPreferredSize(Dimension preferredSize) {
		this.preferredSize = preferredSize;
	}

	public final Stroke getEdgeStroke() {
		return edgeStroke;
	}

	public final void setEdgeStroke(Stroke edgeStroke) {
		this.edgeStroke = edgeStroke;
	}

	public final int getTranslatingMouseModifier() {
		return translatingMouseModifier;
	}

	public final void setTranslatingMouseModifier(int translatingMouseModifier) {
		this.translatingMouseModifier = translatingMouseModifier;
	}

	public final int getPickingMouseModifier1() {
		return pickingMouseModifier1;
	}

	public final void setPickingMouseModifier1(int pickingMouseModifier1) {
		this.pickingMouseModifier1 = pickingMouseModifier1;
	}

	public final int getPickingMouseModifier2() {
		return pickingMouseModifier2;
	}

	public final void setPickingMouseModifier2(int pickingMouseModifier2) {
		this.pickingMouseModifier2 = pickingMouseModifier2;
	}

	public final int getRotatingMouseModifier() {
		return rotatingMouseModifier;
	}

	public final void setRotatingMouseModifier(int rotatingMouseModifier) {
		this.rotatingMouseModifier = rotatingMouseModifier;
	}

	public final int getShearingMouseModifier() {
		return shearingMouseModifier;
	}

	public final void setShearingMouseModifier(int shearingMouseModifier) {
		this.shearingMouseModifier = shearingMouseModifier;
	}

}
