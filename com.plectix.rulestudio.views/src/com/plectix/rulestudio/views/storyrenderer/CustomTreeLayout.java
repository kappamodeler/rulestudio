/**
 * 
 */
package com.plectix.rulestudio.views.storyrenderer;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;

public final class CustomTreeLayout extends AbstractLayout<StoryTreeNode, StoryConnection> {
	private StoryTree storyTree = null;
	private boolean resetDone = false;
	
	public CustomTreeLayout(StoryTree storyTree) {
		super(storyTree.getGraph());
		this.storyTree = storyTree;
	}

	public void initialize() {
		// System.err.println("initialize");
	}

	public void reset() {
		if (resetDone) {
			// System.err.println("reset already done! not doing again!");
			return;
		}
		
		boolean allSet = true;
		for (StoryTreeNode storyTreeNode : graph.getVertices()) {
			if (storyTreeNode.getShape() == null) {
				allSet = false;
				break;
			}
		}
		if (allSet) {
			// System.err.println("reset");
			Dimension size = getSize();
			double maxY = Double.NEGATIVE_INFINITY;
			for (StoryTreeNode storyTreeNode : graph.getVertices()) {
				maxY = Math.max(maxY, storyTreeNode.getShape().getHeight());
			}
			
			double maxScale = Double.NEGATIVE_INFINITY;
			for (StoryTreeNode storyTreeNode : graph.getVertices()) {
				StoryTreeNode rightNode = storyTreeNode.getRightNode();
				if (rightNode != null) {
					double covered = storyTreeNode.getShape().getMaxX() - rightNode.getShape().getMinX();
					double minSeperation = 1.1 * covered;
					double scale = minSeperation / (rightNode.getX() - storyTreeNode.getX());
					maxScale = Math.max(maxScale, scale);
				}
			}
			
			size.setSize(maxScale, 2*maxY*storyTree.getMaximumDepth());
			setSize(size);
			
			// System.err.println(size.getWidth() + "\t" + size.getHeight());
			Graph<StoryTreeNode, StoryConnection> graph = getGraph();
			for (StoryTreeNode storyTreeNode : graph.getVertices()) {
				Point2D coordinate = transform(storyTreeNode);
				coordinate.setLocation(size.getWidth() * storyTreeNode.getX(), size.getHeight() * (1.0 - storyTreeNode.getY()));
				// System.err.println(storyTreeNode.getX() + "\t" + storyTreeNode.getY() + "\t" + coordinate.getX() + "\t" + coordinate.getY());
			}
			resetDone = true;
		}
	}

}