package com.plectix.rulestudio.views.storyrenderer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class GraphVisualizer {

	public static final VisualizationViewer<StoryTreeNode, StoryConnection> makeVisualizationServer(final StoryTree storyTree, final GraphSettings settings) {
		Layout<StoryTreeNode, StoryConnection> layout = new CustomTreeLayout(storyTree); 
		/*
		if (settings.getLayoutName() == GraphSettings.LayoutName.ISOMLayout) {
			layout = new ISOMLayout<StoryTreeNode, StoryConnection>(graph); 
		} else if (settings.getLayoutName() == GraphSettings.LayoutName.KKLayout) {
			layout = new KKLayout<StoryTreeNode, StoryConnection>(graph); 
		} else if (settings.getLayoutName() == GraphSettings.LayoutName.TreeLayout) {
			if (storyTree.isCyclic()) {
				/*
				KKLayout<StoryNode, StoryConnection> kkLayout = new KKLayout<StoryNode, StoryConnection>(graph); 
				kkLayout.setLengthFactor(2);
				layout = kkLayout;
				
				CustomTreeLayout2 customTreeLayout = new CustomTreeLayout2(storyTree); 
				layout = customTreeLayout;
			} else {
				layout = new TreeLayout<StoryTreeNode, StoryConnection>(new DelegateForest<StoryTreeNode, StoryConnection>(graph), 100, 50);
				// layout = new TreeLayout<StoryNode, StoryConnection>(graph, 100, 50); 
			}
		} 

		if (settings.getLayoutName() != GraphSettings.LayoutName.TreeLayout) {
			layout.setSize(settings.getInitialSize()); // Sets the initial size of the space 
		}
		*/

		VisualizationViewer<StoryTreeNode, StoryConnection> visualizationServer = new VisualizationViewer<StoryTreeNode, StoryConnection>(layout);
		visualizationServer.setPreferredSize(settings.getPreferredSize()); // Sets the viewing area size 
		
		final RenderContext<StoryTreeNode, StoryConnection> rendererContext = visualizationServer.getRenderContext();
		
		rendererContext.setVertexFillPaintTransformer(new Transformer<StoryTreeNode, Paint>() { 
            public Paint transform(StoryTreeNode storyNode) { 
                return settings.getPaint(storyNode); 
            } 
        });
        
        // Set up a new stroke Transformer for the edges 
		rendererContext.setEdgeStrokeTransformer(new Transformer<StoryConnection, Stroke>() { 
            public Stroke transform(StoryConnection storyConnection) { 
                return settings.getEdgeStroke(); 
            } 
        });
   	
        visualizationServer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<StoryTreeNode>()); 
        visualizationServer.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<StoryConnection>()); 
        visualizationServer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR); 

		return visualizationServer;
	}
	
	public static final void autoResize(final VisualizationViewer<StoryTreeNode, StoryConnection> visualizationServer) {
		final RenderContext<StoryTreeNode, StoryConnection> rendererContext = visualizationServer.getRenderContext();
		final Graphics2D graphics2d = (Graphics2D) visualizationServer.getGraphics();

		if (graphics2d == null) {
			// System.err.println("Graphics2D is NULL");
			return;
		}
			
		rendererContext.setVertexShapeTransformer(new Transformer<StoryTreeNode, Shape>() {
			public Shape transform(StoryTreeNode storyNode) {
				if (storyNode.getShape() != null) {
					return storyNode.getShape();
				}
				Font font = rendererContext.getVertexFontTransformer().transform(storyNode);
				FontRenderContext fontRenderContext = graphics2d.getFontRenderContext();
				TextLayout textLayout = new TextLayout(storyNode.toString(), font, fontRenderContext);
				RoundRectangle2D.Double roundRectangle = new RoundRectangle2D.Double(0, 0, 0, 0, 8, 8);
				Rectangle2D rectangle = textLayout.getBounds();
				double width = rectangle.getWidth()+6;
				double height = rectangle.getHeight()+6;
				roundRectangle.setFrame(rectangle.getX()-3-width/2, rectangle.getY()-3, width, height);
				storyNode.setShape(roundRectangle.getFrame());
				visualizationServer.getGraphLayout().reset();
				return roundRectangle;
			}
        });
	}

}
