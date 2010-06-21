package com.plectix.rulestudio.views.storyrenderer;


public class StoryConnection {

	public static enum Relation {
		STRONG,
	}
	
	private int fromNode = -1;
	private int toNode = -1;
	private Relation relation = null;
	
	public StoryConnection() {
		super();
	}

	public String toString() {
		return "";
	}

	public final boolean isValid() {
		if (fromNode == -1) return false;
		if (toNode == -1) return false;
		if (relation == null) return false;
		return true;
	}
	
	public final void setFromNode(String fromNode) {
		this.fromNode = Integer.parseInt(fromNode);
	}

	public final void setToNode(String toNode) {
		this.toNode = Integer.parseInt(toNode);
	}

	public final void setRelation(String relation) {
		this.relation = getRelationByName(relation);
	}

	private static final Relation getRelationByName(String name) {
		for (Relation relation : Relation.values()) {
			if (relation.toString().equalsIgnoreCase(name)) {
				return relation;
			}
		}
		throw new RuntimeException("Unknown Connection Relation " + name);
	}
	
	public final int getFromNode() {
		return fromNode;
	}

	public final int getToNode() {
		return toNode;
	}

	public final Relation getRelation() {
		return relation;
	}

}
