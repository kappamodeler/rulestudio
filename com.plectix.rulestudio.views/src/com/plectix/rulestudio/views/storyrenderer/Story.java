package com.plectix.rulestudio.views.storyrenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Story {

	private String observable = null;
	private double percentage = Double.NaN;
	private double average = Double.NaN;
	
	private List<StoryNode> storyNodes = new ArrayList<StoryNode>();
	private List<StoryConnection> storyConnections = new ArrayList<StoryConnection>();

	public Story() {
		super();
	}
	
	public final boolean isValid() {
		if (observable == null) return false;
		if (Double.isNaN(percentage)) return false;
		if (Double.isNaN(average)) return false;
		return true;
	}

	public final void sortNodes() {
		Collections.sort(storyNodes, StoryNode.STORY_NODE_COMPARATOR);
	}
	
	public final boolean addNode(StoryNode storyNode) {
		return storyNodes.add(storyNode);
	}

	public final boolean addConnection(StoryConnection storyConnection) {
		return storyConnections.add(storyConnection);
	}

	public final void setObservable(String observable) {
		this.observable = observable;
	}

	public final void setPercentage(String percentage) {
		this.percentage = Double.parseDouble(percentage);
	}

	public final void setAverage(String average) {
		this.average = Double.parseDouble(average);
	}

	public String toString() {
		return observable + "\t" + percentage + "\t" + average + "\t" + storyNodes.size() + " nodes \t" + storyConnections.size() + " connections"; 
	}
	
	public final String getObservable() {
		return observable;
	}

	public final double getPercentage() {
		return percentage;
	}

	public final double getAverage() {
		return average;
	}

	public final List<StoryNode> getStoryNodes() {
		return storyNodes;
	}

	public final List<StoryConnection> getStoryConnections() {
		return storyConnections;
	}

}
