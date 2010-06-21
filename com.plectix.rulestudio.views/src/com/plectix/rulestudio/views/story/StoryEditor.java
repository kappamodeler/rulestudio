package com.plectix.rulestudio.views.story;

import javax.swing.JPanel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.plectix.rulestudio.views.storyrenderer.GraphSettings;
import com.plectix.rulestudio.views.storyrenderer.Story;
import com.plectix.rulestudio.views.storyrenderer.StoryVisualizer;

public class StoryEditor extends MultiPageEditorPart {
	
	private StyledText log = null;
	private StyledText console = null;
	protected Button pick;
	protected Button tran;
	protected StoryVisualizer storyVisualizer;

	public StoryEditor() {
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
				storyVisualizer = new StoryVisualizer(fileName,
						new GraphSettings());
				if (storyVisualizer.getNumberOfStories() > 0) {
					buildList(storyVisualizer);
				}
				String log = storyVisualizer.getLog();
				createLog(log, "Log");
				String model = storyVisualizer.getModel();
				if (model != null && model.length() > 0) {
					createLog(model, "Model");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		} else if (input instanceof StoryData) {
			setPartName("Stories");
			createConsole();
			StoryData sd = (StoryData)input;
			String text = sd.getConsole();
			if (text != null) {
				console.append(text);
			}
			sd.setEditor(this);
				
		}

	}

	private void buildList(final StoryVisualizer storyVisualizer) {
		Composite comp = new Composite(getContainer(), SWT.NONE);
		
		Display display = Display.getCurrent();
		Color white = display.getSystemColor(SWT.COLOR_WHITE);
		comp.setBackground(white);
		
		Table table = new Table(comp, SWT.CHECK | SWT.SINGLE);
		table.setHeaderVisible(true);
		
		TableColumn count = new TableColumn(table, SWT.NONE);
		
		TableColumn obs = new TableColumn(table, SWT.NONE);
		obs.setText("Observable");
		
		TableColumn percent = new TableColumn(table, SWT.NONE);
		percent.setText("Percentage");
		
		TableColumn avg = new TableColumn(table, SWT.NONE);
		avg.setText("Average");
		
		table.setData(storyVisualizer);
		
		int index = 0;
		Listener listen = new Listener() {

			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK && event.item instanceof TableItem) {
					TableItem item = (TableItem)(event.item);
					if (item.getData() instanceof Integer) {
						int index = ((Integer) item.getData()).intValue();
						if (item.getChecked()) {
							addStory(index, storyVisualizer);
						} else {
							Story story = storyVisualizer.getStoryList().get(index);
							for (int i = 0; i < getPageCount(); ++i) {
								Control con = getControl(i);
								if (con.getData() == story) {
									removePage(i);
									break;
								}
							}
						}
					}
				}
				
			}

		};
		
		table.addListener(SWT.Selection, listen);
		
		for (Story story: storyVisualizer.getStoryList()) {
			TableItem row = new TableItem(table, SWT.NONE);
			row.setData(index);
			++index;
			row.setText(0, Integer.toString(index));
			row.setText(1, story.getObservable());
			row.setText(2, Double.toString(story.getPercentage()));
			row.setText(3, Double.toString(story.getAverage()));
		}
		
		count.pack();
		obs.pack();
		percent.pack();
		avg.pack();
		
		table.pack();
		comp.layout();
		
		int in = addPage(comp);
		setPageText(in, "Control");
	}
	
	private void addStory(int index, StoryVisualizer sv) {
		try {
			Composite comp = new Composite(StoryEditor.this.getContainer(),
					SWT.EMBEDDED);
			
			Story story = sv.getStoryList().get(index);
			comp.setData(story);
			
			java.awt.Frame frame = SWT_AWT.new_Frame(comp);

			JPanel jPanel = sv.displayStory(index);
			sv.setCustomMouse();

			frame.pack();
			frame.add(jPanel);
			frame.pack();
			sv.autoResize();

			++index;
			String name = "Story " + index;
			int addIndex = findAdd(index, name);
			if (addIndex >= 0) {
				StoryEditor.this.addPage(addIndex, comp);
			} else {
				addIndex = StoryEditor.this.addPage(comp);
			}
			StoryEditor.this.setPageText(addIndex, name);
			StoryEditor.this.setActivePage(addIndex);
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

	private int findAdd(int index, String name) {
		for (int page = 0; page < StoryEditor.this.getPageCount(); ++page) {
			String pName = StoryEditor.this.getPageText(page);
			if (pName.startsWith("Story ")) {
				if (pName.length() > name.length() || pName.compareTo(name) > 0)
					return page;					// Story 10 > Story 9 and Story 9 > Story 10
			}
		}
		return -1;
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
	
	public void writeConsole(String line) {
		console.append(line);
		console.append("\n");
	}

}
