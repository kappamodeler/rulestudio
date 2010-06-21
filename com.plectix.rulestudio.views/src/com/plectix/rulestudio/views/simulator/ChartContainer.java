package com.plectix.rulestudio.views.simulator;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Collection;

import javax.swing.JTextArea;
import javax.swing.text.PlainDocument;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

/**
 * 
 * @author ecemis
 */
public class ChartContainer {
	private static final boolean SHOW_LEGEND = true;
	
	private static final Shape SMALL_RECTANGLE = new Rectangle(-1, -1, 2, 2);
	
	private JTextArea textArea= new JTextArea();
	
	private XYSeriesCollection seriesCollectionObservables = null;
	private XYSeriesCollection seriesCollectionRules = null;
	
	Shell obsShell = null;
	Shell ruleShell = null;
	Composite obs = null;
	Composite rule = null;
	
	public ChartContainer(Composite comp, String name) {
		super();
		initiliaze(name);
		//obsComp = comp;
		//ruleComp = comp;
	}
	
	public ChartContainer(Composite obsPlot, Composite rulePlot) {
		obs = obsPlot;
		rule = rulePlot;
	}

	public final void initiliaze(String name) {
		// Chart Panels
		//observablesChartPanel = createNewChartPanel();
		//rulesChartPanel  = createNewChartPanel();
		
		obsShell = new Shell(Display.getCurrent());
		obsShell.setSize(800, 600);
		obsShell.setLayout(new FillLayout());
		obsShell.setText("Observables for " + name);
		obs = obsShell;
		ruleShell = new Shell(Display.getCurrent());
		ruleShell.setSize(800, 600);
		ruleShell.setLayout(new FillLayout());
		ruleShell.setText("Rules for " + name);
		rule = ruleShell;
	}
	
	public final void resetCharts() {
		seriesCollectionObservables = new XYSeriesCollection();
		seriesCollectionRules = new XYSeriesCollection();
	}
	
	public final void clearConsole() {
		textArea.setDocument(new PlainDocument());
	}
	
	public final JTextArea getTextArea() {
		return textArea;
	}
	
	public final void updateLiveDataChart(RsLiveData liveData) throws Exception {
		// LOGGER.info("Entered updateLiveDataChart with liveData having " + liveData.getNumberOfPlots() + " plots.");
		seriesCollectionObservables = new XYSeriesCollection();
		seriesCollectionRules = new XYSeriesCollection();
			
		int numberOfPlots = liveData.count();
		for (int i = 0; i < numberOfPlots; i++) {
			Plot plot = liveData.getPlot(i);
			if (plot.getDisplay()) {
				String plotType = plot.getType();
				if (plotType.equalsIgnoreCase("OBSERVABLE")) {
					seriesCollectionObservables.addSeries(new XYSeries(plot
							.getName()));
				} else if (plotType.equalsIgnoreCase("RULE")) {
					seriesCollectionRules
							.addSeries(new XYSeries(plot.getName()));
				} else {
					throw new RuntimeException("Unknown plot type: " + plotType);
				}
			}
		}
		
		Collection<DataPoint> liveDataPoints = liveData.getPoints();
		if (liveDataPoints == null) {
			return;
		}
		// LOGGER.info("Each curve has " + liveData.getData().size() + " points.");
		boolean hasObs = false;
		boolean hasRule = false;
		
		for (DataPoint liveDataPoint : liveDataPoints) {
			double time = liveDataPoint.getTime();
			double[] values = liveDataPoint.getValues();
			int observablePlotCount = 0;
			int rulePlotCount = 0;
			for (int i = 0; i < numberOfPlots; i++) {
				Plot plot = liveData.getPlot(i);
				if (plot.getDisplay()) {
					String plotType = plot.getType();
					if (plotType.equalsIgnoreCase("OBSERVABLE")) {
						hasObs = true;
						seriesCollectionObservables.getSeries(
								observablePlotCount++).add(time, values[i]);
					} else if (plotType.equalsIgnoreCase("RULE")) {
						hasRule = true;
						seriesCollectionRules.getSeries(rulePlotCount++).add(
								time, values[i]);
					} else {
						throw new RuntimeException("Unknown plot type: "
								+ plotType);
					}
				}
			}
		}
		// LOGGER.info("Drawing " + liveDataPoints.size() + " points for " + seriesCollection.getSeriesCount() + " series!");
		//updateLiveDataChartPanel(observablesChartPanel, seriesCollectionObservables);
		//updateLiveDataChartPanel(rulesChartPanel, seriesCollectionRules);
		if (hasObs) {
			updateChart(obs, seriesCollectionObservables);
		}
		if (hasRule) {
			updateChart(rule, seriesCollectionRules);
		}
	}
	

	
	private JFreeChart updateChart(final Composite comp, XYSeriesCollection seriesCollection) {
		// Create X axis:
        NumberAxis xAxis = new NumberAxis();
		xAxis.setLowerMargin(0.02);
		xAxis.setUpperMargin(0.02);
        xAxis.setAutoRangeIncludesZero(true);
        
		// Create Y axis:
        NumberAxis yAxis = new NumberAxis();
		yAxis.setLowerMargin(0.02);
		yAxis.setUpperMargin(0.02);
        yAxis.setAutoRangeIncludesZero(true);

        // Plot:
        XYPlot plot = new XYPlot();
		plot.setBackgroundPaint(new Color(255,255,240));
		plot.setDomainAxis(xAxis);
		plot.setRangeAxis(yAxis);
		
		plot.setDataset(seriesCollection);
		
		int numberOfPlots = seriesCollection.getSeriesCount();
		for (int i = 0; i < numberOfPlots; i++)  {
			plot.setRenderer(i, new ItemRenderer(ColorMap.getColor(i)));  // XYLineAndShapeRenderer(true, false);
		}

		// Combine the plots
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(xAxis);
        combinedPlot.setOrientation(PlotOrientation.VERTICAL);
        combinedPlot.setGap(8);
        
        combinedPlot.add(plot, 1);

        final JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, SHOW_LEGEND);
        
        UIJob showChart = new UIJob("Show Chart") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					if (comp.isDisposed())
						return Status.CANCEL_STATUS;
					comp.setRedraw(false);
			        Control[] children = comp.getChildren();
			        for (Control child: children) {
			        	child.dispose();
			        }
			       
					ChartComposite frame = new ChartComposite(comp, SWT.NONE,
							chart, true);
					frame.pack();
					comp.layout();
					if (comp instanceof Shell) {
						((Shell)comp).open();
					}
					comp.setRedraw(true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return Status.OK_STATUS;
			}
        	
        };
        showChart.setSystem(true);
        showChart.setUser(false);
        showChart.schedule();

        return chart;
	}
	

	//***************************************************************************************
	/**
	 * 
	 * @author ecemis
	 */
	private static final class ItemRenderer extends XYLineAndShapeRenderer implements XYItemRenderer {
		private static final long serialVersionUID = 1L;
		
		public ItemRenderer(final Color color) {
			super(true, false);

			Shape shape = SMALL_RECTANGLE;
			
			setBaseShape(shape);
			setBaseShape(shape);
			setBaseItemLabelPaint(color);
			setBasePaint(color);
		}
	}

}
