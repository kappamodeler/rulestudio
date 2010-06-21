package com.plectix.rulestudio.views.storyrenderer;

import java.util.Comparator;

public class StoryNode {
	public static final StoryNodeComparator STORY_NODE_COMPARATOR = new StoryNodeComparator();

	public static enum Type {
		INTRO,
		OBSERVABLE,
		RULE
	}

	private int nodeID = -1;
	private Type type = null;
	private String text = null;
	private String data = null;
	private int depth = -1;
	
	public StoryNode() {
		super();	
	}
	
	public String toString() {
		return text; // nodeID + " " + text;
	}
	
	public final boolean isValid() {
		if (nodeID == -1) return false;
		if (type == null) return false;
		if (text == null) return false;
		if (data == null) return false;
		if (depth == -1) return false;
		return true;
	}
	
	private static final Type getTypeByName(String name) {
		for (Type type : Type.values()) {
			if (type.toString().equalsIgnoreCase(name)) {
				return type;
			}
		}
		throw new RuntimeException("Unknown Node Type " + name);
	}

	public final void setNodeID(String nodeID) {
		this.nodeID = Integer.parseInt(nodeID);
	}

	public final void setType(String type) {
		this.type = getTypeByName(type);
	}
	
	public final void setText(String text) {
		this.text = text;
	}

	public final void setData(String data) {
		this.data = data;
	}

	public final void setDepth(String depth) {
		this.depth = Integer.parseInt(depth);
	}
	
	
	public final int getNodeID() {
		return nodeID;
	}

	public final Type getType() {
		return type;
	}

	public final String getText() {
		return text;
	}

	public final String getData() {
		return data;
	}

	public final int getDepth() {
		return depth;
	}

	private static final class StoryNodeComparator implements Comparator<StoryNode> {
		public int compare(StoryNode o1, StoryNode o2) {
			return Double.compare(o1.nodeID, o2.nodeID);
		}
	}

}
