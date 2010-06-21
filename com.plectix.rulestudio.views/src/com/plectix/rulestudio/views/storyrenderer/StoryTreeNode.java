/**
 * 
 */
package com.plectix.rulestudio.views.storyrenderer;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.uci.ics.jung.graph.DirectedGraph;

public final class StoryTreeNode {
	private enum Branch {
		LEFT,
		CENTER,
		RIGHT
	};
	
	
	private List<StoryTreeNode> parents = new ArrayList<StoryTreeNode>();
	private List<StoryTreeNode> pseudoParents = new ArrayList<StoryTreeNode>();
	
	private List<StoryTreeNode> children = new ArrayList<StoryTreeNode>();
	private List<StoryTreeNode> pseudoChildren = new ArrayList<StoryTreeNode>();
	
	private List<StoryConnection> storyConnections = new ArrayList<StoryConnection>();
	
	private Rectangle2D shape = null;
	
	private int depth = Integer.MAX_VALUE;
	private int numberOfDescendants = 1;
		
	private double x = Double.NaN;
	private double y = Double.NaN;
	
	private boolean infoAlreadyAdded = false;
	
	private Branch branch = Branch.CENTER;

	private StoryTreeNode rightNode = null;

	private StoryTreeNode leftNode = null;

	private StoryNode storyNode = null;
	
	
	public StoryTreeNode(StoryNode storyNode) {
		this.storyNode = storyNode;
	}


	public void setDepthInfo(ArrayList<ArrayList<StoryTreeNode>> depthInfo) {
		if (infoAlreadyAdded) {
			return;
		}
		depthInfo.get(depth).add(this);
		infoAlreadyAdded = true;
		for (int i= 0; i < children.size(); i++) {
			children.get(i).setDepthInfo(depthInfo);
		}
	}

	public void breakCycles(boolean recursive) {
		if (parents.size() > 1) {
			// this node has 2 or more parents...
			List<List<StoryTreeNode>> ancestorsList = new ArrayList<List<StoryTreeNode>>(parents.size());
			for (StoryTreeNode parent : parents) {
				List<StoryTreeNode> parentAncestors = new ArrayList<StoryTreeNode>();
				parent.getAncestors(parentAncestors);
				ancestorsList.add(parentAncestors);
			}
			int parentToKeepIndex = 0;
			for (int i= 0; i < ancestorsList.size(); i++) {
				if (ancestorsList.get(i).size() > ancestorsList.get(parentToKeepIndex).size()) {
					parentToKeepIndex = i;
				}
			}
			StoryTreeNode parentToKeep = parents.get(parentToKeepIndex);
			for (int i= 0; i < parents.size(); i++) {
				if (i != parentToKeepIndex) {
					pseudoParents.add(parents.get(i));
				}
			}
			parents.clear();
			parents.add(parentToKeep);
		}
		if (recursive) {
			for (int i= 0; i < children.size(); i++) {
				children.get(i).breakCycles(true);
			}
		}
	}
	
	private void getAncestors(List<StoryTreeNode> parentAncestors) {
		parentAncestors.add(this);
		if (parents.size() == 0) {
			return;
		}
		if (parents.size() > 1) {
			breakCycles(false);
		}
		if (parents.size() > 1) {
			throw new RuntimeException("Still more than one parent!");
		}
		parents.get(0).getAncestors(parentAncestors);
	}

	public void cleanChildren() {
		for (StoryTreeNode pseudoParent: pseudoParents) {
			pseudoParent.children.remove(this);
			pseudoParent.pseudoChildren.add(this);
		}
	}

	public void setBranches() {
		if (children.size() == 0) {
			return;
		}
		if (children.size() == 1) {
			children.get(0).branch = Branch.CENTER;
			children.get(0).setBranches();
			return;
		}
		Collections.sort(children, CHILDREN_BRANCH_COMPARATOR);
		
		if (children.size() > 2) {
			List<StoryTreeNode> newChildren = new ArrayList<StoryTreeNode>(children);
			for (int i = 0; i < children.size()/2; i++) {
				newChildren.set(i, children.get(2*i));
				newChildren.set(children.size()-1 - i, children.get(2*i+1));
			}
			if (children.size()%2 == 1) {
				newChildren.set((children.size()-1)/2, children.get(children.size()-1));
			}
			
			children = newChildren;
		}
		
		if (branch == Branch.LEFT) {
			children.get(0).branch = Branch.LEFT;
			children.get(children.size()-1).branch = Branch.RIGHT;
		} else {
			children.get(0).branch = Branch.RIGHT;
			children.get(children.size()-1).branch = Branch.LEFT;
		}
		for (StoryTreeNode child : children) {
			child.setBranches();
		}
	}

	public int setNumberOfDescendants() {
		for (int i= 0; i < children.size(); i++) {
			numberOfDescendants = numberOfDescendants + children.get(i).setNumberOfDescendants();
		}
		return numberOfDescendants;
	}

	public void setY(int maximumDepth) {
		// System.err.println("---> " + depth + "\t" + maximumDepth);
		y = depth * 1.0 / maximumDepth;
		for (StoryTreeNode node : children) {
			node.setY(maximumDepth);
		}
	}

	public String toString() {
		return storyNode.toString();
	}
	
	public void addChildrenToGraph(DirectedGraph<StoryNode, StoryConnection> graph) {
		for (int i= 0; i < children.size(); i++) {
			graph.addEdge(storyConnections.get(i), this.storyNode, children.get(i).getStoryNode());
			children.get(i).addChildrenToGraph(graph);
		}
	}
	
	public boolean addChild(StoryTreeNode child, StoryConnection storyConnection) {
		boolean ret = true; 
		ret = ret && child.parents.add(this);
		ret = ret && storyConnections.add(storyConnection);
		ret = ret && children.add(child);
		return ret;
	}
	
	public int setDepth(int parentDepth) {
		int newDepth = parentDepth+1;
		int maximumDepth = newDepth;
		if (newDepth < depth) {
			depth = newDepth;
			for (int i= 0; i < children.size(); i++) {
				maximumDepth = Math.max(maximumDepth, children.get(i).setDepth(depth));
			}
		}
		return maximumDepth;
	}

	public void setX(double d) {
		x = d;
	}
	
	public void setX(double x, double xMin, double xMax) {
		this.x = x;
		double width = (xMax - xMin) / children.size();
		for (int i= 0; i < children.size(); i++) {
			double x1 = xMin + i*width;
			children.get(i).setX(x1 + 0.5*width, x1, x1+width);
		}
	}
	
	public final List<StoryTreeNode> getParents() {
		return parents;
	}
	
	public final StoryNode getStoryNode() {
		return storyNode;
	}

	public final int getDepth() {
		return depth;
	}

	public final double getX() {
		return x;
	}

	public final double getY() {
		return y;
	}


	public final int getNumberOfDescendants() {
		return numberOfDescendants;
	}


	public final List<StoryTreeNode> getPseudoParents() {
		return pseudoParents;
	}


	public final List<StoryTreeNode> getChildren() {
		return children;
	}

	private static final Comparator<StoryTreeNode> CHILDREN_BRANCH_COMPARATOR = new Comparator<StoryTreeNode>() {
		public int compare(StoryTreeNode o1, StoryTreeNode o2) {
			int ret = Double.compare(o1.getNumberOfDescendants(), o2.getNumberOfDescendants());
			if (ret != 0) {
				return ret;
			}
			ret = Double.compare(o1.getChildren().size(), o2.getChildren().size());
			if (ret != 0) {
				return ret;
			}
			Double.compare(o1.pseudoChildren.size(), o2.pseudoChildren.size());
			return 0;
		}
	};

	public final Rectangle2D getShape() {
		return shape;
	}


	public final void setShape(Rectangle2D shape) {
		this.shape = shape;
	}


	public void setRightNode(StoryTreeNode storyTreeNode) {
		rightNode = storyTreeNode;
	}


	public void setLeftNode(StoryTreeNode storyTreeNode) {
		leftNode = storyTreeNode;
	}


	public final StoryTreeNode getRightNode() {
		return rightNode;
	}


	public final StoryTreeNode getLeftNode() {
		return leftNode;
	}

}