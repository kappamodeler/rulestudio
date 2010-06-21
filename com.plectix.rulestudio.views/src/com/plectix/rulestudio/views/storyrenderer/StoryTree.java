/**
 * 
 */
package com.plectix.rulestudio.views.storyrenderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class StoryTree {
	private int maximumDepth = -1;
		
	private boolean isCyclic = false;
	private StoryTreeNode rootNode = null;
	private List<StoryTreeNode> treeNodes = new ArrayList<StoryTreeNode>();
	
	private DirectedGraph<StoryTreeNode, StoryConnection> graph = null;
	
	public StoryTree(Story story) {
		graph = new DirectedSparseMultigraph<StoryTreeNode, StoryConnection>();

		List<StoryNode> storyNodes = story.getStoryNodes();
		for (StoryNode storyNode : storyNodes) {
			StoryTreeNode treeNode = new StoryTreeNode(storyNode);
			// graph.addVertex(storyNode);
			graph.addVertex(treeNode);
			treeNodes.add(treeNode);
		}
		
		for (StoryConnection storyConnection : story.getStoryConnections()) {
			// graph.addEdge(storyConnection, storyNodes.get(storyConnection.getFromNode()), storyNodes.get(storyConnection.getToNode()), EdgeType.DIRECTED);
			// graph.addEdge(storyConnection, storyNodes.get(storyConnection.getToNode()), storyNodes.get(storyConnection.getFromNode()));
			graph.addEdge(storyConnection, treeNodes.get(storyConnection.getFromNode()), treeNodes.get(storyConnection.getToNode()));
			treeNodes.get(storyConnection.getToNode()).addChild(treeNodes.get(storyConnection.getFromNode()), storyConnection);
		}
		
		for (StoryTreeNode treeNode : treeNodes) {
			int numberOfParents = treeNode.getParents().size();
			if (numberOfParents == 0) {
				rootNode = treeNode;
				break;
			} else if (numberOfParents > 1) {
				isCyclic = true;
			}
		}
		
		if (isCyclic) {
			rootNode.breakCycles(true);
			for (StoryTreeNode treeNode : treeNodes) {
				treeNode.cleanChildren();
			}
		}
		
		rootNode.setBranches();
		
		maximumDepth = rootNode.setDepth(0);
		
		rootNode.setNumberOfDescendants();
		
		// System.err.println("storyNodes.size(): " + storyNodes.size());
		// System.err.println("numberOfDescendants: " + rootNode.getNumberOfDescendants());
	
		
		ArrayList<ArrayList<StoryTreeNode>> depthInfo = new ArrayList<ArrayList<StoryTreeNode>>();
		for (int i = 0; i< maximumDepth + 2; i++) {
			depthInfo.add(new ArrayList<StoryTreeNode>());
		}
		
		rootNode.setDepthInfo(depthInfo);
		
		// Here is some heuristical code to set the node locations:
		for (ArrayList<StoryTreeNode> arrayList : depthInfo) {
			if (arrayList.size() == 1) {
				StoryTreeNode storyTreeNode = arrayList.get(0);
				if (storyTreeNode.getParents().size() == 0) {
					// let's put the root node in the middle...
					storyTreeNode.setX(0.5);
				} else {
					storyTreeNode.setX(storyTreeNode.getParents().get(0).getX());
				}
			} else {
				HashSet<Integer> anchorNodes = new HashSet<Integer>();
				for (int i = 0; i < arrayList.size(); i++) {
					StoryTreeNode storyTreeNode = arrayList.get(i);
					if (i != 0) {
						storyTreeNode.setLeftNode(arrayList.get(i-1));
					}
					if (i != arrayList.size()-1) {
						storyTreeNode.setRightNode(arrayList.get(i+1));
					}
					
					if (arrayList.get(i).getNumberOfDescendants() > 0.60 * arrayList.get(i).getParents().get(0).getNumberOfDescendants()) {
						storyTreeNode.setX(storyTreeNode.getParents().get(0).getX());
						anchorNodes.add(i);
					} 
				}
				for (int i = 0; i < arrayList.size(); i++) {
					if (anchorNodes.contains(i) == false) {
						int toIndex = i+1;
						while (toIndex < arrayList.size() && anchorNodes.contains(toIndex) == false) {
							toIndex++;
						}
						double leftBound = 0;
						double rightBound = 1.0;
						if (i != 0) {
							leftBound = arrayList.get(i-1).getX();
						}
						if (toIndex != arrayList.size()) {
							rightBound = arrayList.get(toIndex).getX();
						}
						for (int index = 0; index < toIndex-i; index++) {
							StoryTreeNode storyTreeNode = arrayList.get(i+index);
							storyTreeNode.setX(leftBound + (index+1)*(rightBound-leftBound)/(toIndex-i+1));
						}
						i = toIndex;
					}
				}
			}
		}
		
		rootNode.setY(maximumDepth);
	}

	public final DirectedGraph<StoryTreeNode, StoryConnection> getGraph() {
		return graph;
	}
	
	public final boolean isCyclic() {
		return isCyclic;
	}
	
	public final StoryTreeNode getRootNode() {
		return rootNode;
	}

	public final int getMaximumDepth() {
		return maximumDepth;
	}

	public final List<StoryTreeNode> getTreeNodes() {
		return treeNodes;
	}



}