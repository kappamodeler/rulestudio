package com.plectix.rulestudio.views.storyrenderer;

import java.util.List;

import javax.swing.JPanel;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ViewScalingControl;

public class StoryVisualizer {
	
	public enum MouseMode {
		PICKING, 
		TRANSFORMING
	}
	
	private StoryReader storyReader = null;
	private List<Story> storyList = null;
	private GraphSettings graphSettings = null;
	
	private VisualizationViewer<StoryTreeNode, StoryConnection> visualizationServer  = null;
	private DefaultModalGraphMouse<StoryNode, StoryConnection> graphMousePicking = null;
	private DefaultModalGraphMouse<StoryNode, StoryConnection> graphMouseTransforming = null;
	
	private StoryTree storyTree = null;
	
	public StoryVisualizer(final String xmlFilename, final GraphSettings graphSettings) throws Exception {
		this.graphSettings = graphSettings;	
		storyReader = new StoryReader();
		storyList = storyReader.read(xmlFilename);
	}
	
	public final void autoResize() {
		GraphVisualizer.autoResize(visualizationServer);
	}
	
	public final JPanel displayStory(int storyNo) {
		storyTree = new StoryTree(storyList.get(storyNo));
		
		this.visualizationServer = GraphVisualizer.makeVisualizationServer(storyTree, graphSettings);

        // Create a graph mouse and add it to the visualization component 
        graphMousePicking = new DefaultModalGraphMouse<StoryNode, StoryConnection>(); 
        graphMousePicking.setMode(ModalGraphMouse.Mode.PICKING); 
        
        graphMouseTransforming = new DefaultModalGraphMouse<StoryNode, StoryConnection>(); 
        graphMouseTransforming.setMode(ModalGraphMouse.Mode.TRANSFORMING); 
       
        visualizationServer.setGraphMouse(graphMouseTransforming); 		 
		
		return visualizationServer;
	}
	
	public final void setCustomMouse() {
        PluggableGraphMouse gm = new PluggableGraphMouse();
        gm.add(new TranslatingGraphMousePlugin(graphSettings.getTranslatingMouseModifier()));
        gm.add(new PickingGraphMousePlugin<StoryNode, StoryConnection>(graphSettings.getPickingMouseModifier1(), graphSettings.getPickingMouseModifier2()));
        gm.add(new RotatingGraphMousePlugin(graphSettings.getRotatingMouseModifier()));
        gm.add(new ShearingGraphMousePlugin(graphSettings.getShearingMouseModifier()));
        gm.add(new ScalingGraphMousePlugin(new ViewScalingControl(), 0, 1.1f, 0.9f));
        visualizationServer.setGraphMouse(gm); 
	}
	
	public final void setMouseMode(MouseMode mode) {
		
		if (mode == MouseMode.PICKING) {
			visualizationServer.setGraphMouse(graphMousePicking); 
		} else if (mode == MouseMode.TRANSFORMING) {
			visualizationServer.setGraphMouse(graphMouseTransforming); 
		} else {
			throw new RuntimeException("Unexpected mode " + mode);
		}

	}

	public final List<Story> getStoryList() {
		return storyList;
	}

	public final int getNumberOfStories() {
		return storyList.size();
	}
	
	public final String getLog() {
		return storyReader.getLog();
	}
	
	public final String getModel() {
		return storyReader.getModel();
	}
}
