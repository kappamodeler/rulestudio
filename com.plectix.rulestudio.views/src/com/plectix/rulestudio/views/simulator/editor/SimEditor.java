package com.plectix.rulestudio.views.simulator.editor;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.plectix.rulestudio.views.simulator.ChartContainer;
import com.plectix.rulestudio.views.simulator.RsLiveData;
import com.plectix.rulestudio.views.simulator.Plot;
import com.plectix.rulestudio.views.simulator.XML2LiveData;

public class SimEditor extends MultiPageEditorPart {
	
	private StyledText log = null;
	private StyledText console = null;
	private Composite obsPlot = null;
	private Composite rulePlot = null;
	private ChartContainer chart = null;
	private boolean pauseUpdate = false;
	private HashMap<String, Plot> liveUpdateList = null;
	private Button pause = null;
	private Table liveTable = null;
	private RsLiveData lastData = null;

	public SimEditor() {
		super();
		//ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	@Override
	protected void createPages() {
		IEditorInput input = getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput)input).getFile();
			String name = file.getName();
			setPartName(name);
			String fileName = file.getRawLocation().toOSString();
			try {
				RsLiveData data = XML2LiveData.getLiveData(fileName);
				createChart(data);
				createControl(data);
				createLog(data.getLog(), "Log");
				String model = data.getModel();
				if (model != null && model.length() > 0) {
					createLog(model, "Model");
				}
				String csv = data.getCSV();
				if (csv != null && csv.length() > 0) {
					createLog(csv, "CSV");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		} else if (input instanceof SimData) {
			setPartName("Simulation");
			createConsole();
			SimData sd = (SimData)input;
			String text = sd.getConsole();
			if (text != null) {
				console.append(text);
			}
			RsLiveData data = sd.getLiveData();
			if (data != null) {
				try {
					createChart(data);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			sd.setEditor(this);
				
		}

	}

	private void createControl(final RsLiveData data) {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		Display display = Display.getCurrent();
		Color white = display.getSystemColor(SWT.COLOR_WHITE);
		composite.setBackground(white);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		
		ArrayList<Plot> obs = new ArrayList<Plot>();
		ArrayList<Plot> rules = new ArrayList<Plot>();
		for (int i = 0; i < data.count(); ++i) {
			Plot plot = data.getPlot(i);
			if (plot.getType().equals("RULE"))
				rules.add(plot);
			else
				obs.add(plot);
		}
				
		Table table = new Table(composite, SWT.CHECK | SWT.SINGLE);
		table.setHeaderVisible(false);
		
		TableColumn name = new TableColumn(table, SWT.NONE);
		
		table.setData(data);
		
		Listener listen = new Listener() {

			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK && event.item instanceof TableItem) {
					TableItem item = (TableItem)(event.item);
					Object obj = item.getData();
					if (obj instanceof Plot) {
						((Plot)obj).setDisplay(item.getChecked());
					}
					if (chart != null) {
						try {
							chart.updateLiveDataChart(data);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
						
				}
				
			}

		};
		
		table.addListener(SWT.Selection, listen);
		
		addPlots(obs, table);
		addPlots(rules, table);
		
		name.pack();
		
		table.pack();
		composite.layout();
		
		int in = addPage(composite);
		setPageText(in, "Display");
	}

	private void addPlots(ArrayList<Plot> list, Table table) {
		for (Plot plot: list) {
			TableItem row = new TableItem(table, SWT.NONE);
			row.setData(plot);
			row.setChecked(plot.getDisplay());
			row.setText(0, plot.getName());
		}
	}

	private boolean createChart(RsLiveData data)
			throws Exception {
		boolean hasObs = false;
		boolean hasRule = false;
		for (int i = 0; i < data.count(); ++i) {
			Plot plot = data.getPlot(i);
			if (plot.getType().equals("OBSERVABLE")) {
				hasObs = true;
			} else if (plot.getType().equals("RULE"))
				hasRule = true;
		}
		if (hasObs)
			obsPlot = createGraph("Observables");
		if (hasRule)
			rulePlot = createGraph("Rules");
		if (hasObs || hasRule) {
			chart = new ChartContainer(obsPlot, rulePlot);
			chart.updateLiveDataChart(data);;
			return true;
		}
		return false;
	}
	
	private void createLiveControl(RsLiveData data) {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		Display display = Display.getCurrent();
		Color white = display.getSystemColor(SWT.COLOR_WHITE);
		composite.setBackground(white);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		
		pause = new Button(composite, SWT.PUSH);
		pause.setText("Pause");
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.LEFT;
		pause.setLayoutData(gd);
		pause.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				pauseUpdate = !pauseUpdate;
				pause.setText(pauseUpdate?"Restart":"Pause");
				
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		ArrayList<Plot> obs = new ArrayList<Plot>();
		ArrayList<Plot> rules = new ArrayList<Plot>();
		liveUpdateList = new HashMap<String, Plot>();		
		for (int i = 0; i < data.count(); ++i) {
			Plot plot = data.getPlot(i);
			liveUpdateList.put(plot.getName(), plot);
			if (plot.getType().equals("RULE"))
				rules.add(plot);
			else
				obs.add(plot);
		}
		
		liveTable = new Table(composite, SWT.CHECK | SWT.SINGLE);
		liveTable.setHeaderVisible(false);
		gd = new GridData();
		gd.horizontalAlignment = SWT.LEFT;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.TOP;
		gd.grabExcessVerticalSpace = true;
		liveTable.setLayoutData(gd);
		
		TableColumn name = new TableColumn(liveTable, SWT.NONE);
		
		liveTable.setData(data);
		
		Listener listen = new Listener() {

			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK && event.item instanceof TableItem) {
					TableItem item = (TableItem)(event.item);
					Plot plot = liveUpdateList.get(item.getText(0));
					if (plot != null) {
						plot.setDisplay(item.getChecked());
					}
					if (pauseUpdate && lastData != null && chart != null) {
						for (int i = 0; i < lastData.count(); ++i) {
							Plot lastPlot = lastData.getPlot(i);
							if (plot.getName().equals(lastPlot.getName())) {
								lastPlot.setDisplay(plot.getDisplay());
							}
						}
						try {
							chart.updateLiveDataChart(lastData);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}	
			}

		};
		
		liveTable.addListener(SWT.Selection, listen);
		
		addPlots(obs, liveTable);
		addPlots(rules, liveTable);
		
		name.pack();
		
		liveTable.pack();
		composite.layout();
		
		int in = addPage(composite);
		setPageText(in, "Control");
	}

	private Composite createGraph(String string) {
		Composite comp = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		comp.setLayout(layout);
		int i = addPage(comp);
		setPageText(i, string);
		return comp;
	}

	private void createLog(String string, String name) {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		log = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		log.setEditable(false);
		log.setText(string);

		int index = addPage(composite);
		setPageText(index, name);
	}
	
	private void createConsole() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		console = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		console.setEditable(false);

		int index = addPage(composite);
		setPageText(index, "Console");
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	/**
	 * Disable save operations on this file for now.
	 */
	@Override
	public boolean isDirty() {
		return false;
	}
	
	public void updateLiveData(RsLiveData data) {
		if (data == null)
			return;
		try {
			if (chart == null) {
				if (createChart(data)) {
					setActivePage(1);
				}
				createLiveControl(data);
			} else if (!pauseUpdate){
				lastData = data;
				for (int i = 0; i < data.count(); ++i) {
					Plot update = data.getPlot(i);
					Plot status = liveUpdateList.get(update.getName());
					update.setDisplay(status.getDisplay());
				}
				chart.updateLiveDataChart(data);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeConsole(String line) {
		console.append(line);
		console.append("\n");
	}
	
	public void stopSim() {
		if (pause != null)
			pause.setEnabled(false);
		if (liveTable != null)
			liveTable.setEnabled(false);
		lastData = null;
	}

}
