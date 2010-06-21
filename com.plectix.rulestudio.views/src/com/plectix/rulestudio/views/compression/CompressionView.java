/**
 * 
 */
package com.plectix.rulestudio.views.compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.plectix.rulestudio.core.usagedata.Action;
import com.plectix.rulestudio.core.usagedata.UsageDataCollector;
import com.plectix.rulestudio.editors.kappa.KappaEditor;
import com.plectix.rulestudio.views.Activator;
import com.plectix.rulestudio.views.jobs.JsimJob;
import com.plectix.rulestudio.views.jobs.JsimJobFactory;

/**
 * @author bill
 *
 */
public class CompressionView extends ViewPart implements Listener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = CompressionView.class.getCanonicalName();
	public static final String KAPPA_EDITOR_ID = "com.plectix.rulestudio.editors.kappa.KappaEditor";
	private static final String COMP_MARKER = "com.plectix.rulestudio.views.compression.markers";
	public static final String COMPRESSION_XML = "/resources/compression.xml";
	
	private Button run;
	private Button quan;
	private Button qual;
	private Label press;
	private Font labelFont = null;
	private Composite window;
	private Tree tree;
	private Color white;
	private KappaEditor _lastTargetEditor;
	private static boolean shownComputeError = false;
	private CompressionParse parse = null;
	
	/**
	 * 
	 */
	public CompressionView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		window = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = layout.marginHeight = 9;
		window.setLayout(layout);
		Display display = Display.getCurrent();
		white = display.getSystemColor(SWT.COLOR_WHITE);
		window.setBackground(white);
		
		run = new Button(window, SWT.PUSH);
		run.setBackground(white);
		run.setText("Validate");
		run.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				calculateCompression();
				
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
	
			}
		});
		
		qual = new Button(window, SWT.RADIO);
		qual.setText("Qualitative");
		qual.setSelection(true);
		qual.setBackground(white);
		qual.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				if (parse != null && qual.getSelection()) {
					showCompression(parse.getList(), parse.getQualitative());
				}				
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				if (parse != null && qual.getSelection()) {
					showCompression(parse.getList(), parse.getQualitative());
				}
			}
		});
		
		quan = new Button(window, SWT.RADIO);
		quan.setText("Quantitative");
		quan.setBackground(white);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		quan.setLayoutData(gd);
		quan.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				if (parse != null && quan.getSelection()) {
					showCompression(parse.getList(), parse.getQuantitative());
				}				
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				if (parse != null && quan.getSelection()) {
					showCompression(parse.getList(), parse.getQuantitative());
				}
			}
		});

		press = new Label(window, SWT.WRAP);
		press.setText("Press Validate to validate rules for a model.");
		press.setBackground(white);
		Font system = display.getSystemFont();
		FontData[] fons = system.getFontData();
		labelFont = new Font (display, fons[0].getName(), 20, SWT.BOLD);
		press.setFont(labelFont);
		gd = new GridData();
		gd.horizontalSpan = 3;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.TOP;
		press.setLayoutData(gd);
		
		window.pack();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		window.setFocus();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (labelFont != null) {
			labelFont.dispose();
			labelFont = null;
		}
	}

	public void showCompression(ArrayList<CompressionRule> orig, CompressionData data) {
			//HashMap<String, CompressionRule> compressed, HashMap<String, String> map,
			//HashMap<String,ArrayList<String>> merged) {
		if (window == null) 
			return;
		
		if (press != null) {
			press.dispose();
			press = null;
		}
		
		if (tree == null) {
			tree = new Tree(window, SWT.SINGLE);
			tree.setBackground(white);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			gd.horizontalSpan = 3;
			gd.horizontalAlignment = SWT.LEFT;
			gd.verticalAlignment = SWT.TOP;
			tree.setLayoutData(gd);
			tree.addListener(SWT.Selection, this);
			tree.addListener(SWT.DefaultSelection, this);
			
		} else {
			tree.removeAll();
			
		}

		ArrayList<CompressionRule> urList = data.getUnreachable(orig);
		if (urList.size() > 0) {
			TreeItem uri = new TreeItem(tree, SWT.NONE);
			uri.setText("Unreachable Rules");
			
			for (CompressionRule rule: urList) {
				TreeItem item = new TreeItem(uri, SWT.NONE);
				item.setText(rule.getName());
				item.setData(rule);
			}
			uri.setExpanded(true);
		}
		
		ArrayList<ArrayList<CompressionRule>> improvable = data.getImprovable(orig);
		ArrayList<CompressionRule> impList = new ArrayList<CompressionRule>();
		if (improvable.size() > 0) {
			TreeItem iri = new TreeItem(tree, SWT.NONE);
			iri.setText("Improvable Rules");
			for (ArrayList<CompressionRule> sets: improvable) {
				TreeItem imti = new TreeItem(iri, SWT.NONE);
				CompressionRule title = sets.get(1);
				imti.setText(title.getName());
				TreeItem ritm = new TreeItem(imti, SWT.NONE);
				ritm.setText((sets.size() == 2)?"Original Rule":"Original Rules");
				for (int i = 1; i < sets.size(); ++i) {
					CompressionRule rule = sets.get(i);
					TreeItem ori = new TreeItem(ritm, SWT.NONE);
					ori.setText(rule.getName());
					ori.setData(rule);
					impList.add(rule);
				}
				ritm.setExpanded(true);
				TreeItem brti = new TreeItem(imti, SWT.NONE);
				brti.setText("Optimized Rule");
				TreeItem better = new TreeItem(brti, SWT.NONE);
				CompressionRule bRule = sets.get(0);
				better.setText(bRule.getData());
				brti.setExpanded(true);
				imti.setExpanded(true);
			}
			iri.setExpanded(true);
		}

		updateEditor(urList, impList);
		window.layout();

	}

	private void updateEditor(ArrayList<CompressionRule> urList, ArrayList<CompressionRule> impList) {
		if (!(_lastTargetEditor instanceof KappaEditor)) {
			return;
		}
		
		KappaEditor ke = (KappaEditor)_lastTargetEditor;
		IFileEditorInput input = (IFileEditorInput)ke.getEditorInput();
		IFile file = input.getFile();
		
		try {
			file.deleteMarkers(COMP_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		String 	content = ke.getContent();	
		if (content == null)
			return;
		
		for (CompressionRule rule:urList) {
			int index = content.indexOf(rule.getName());
			if (index == -1)
				continue;
			rule.setOffset(index);
			try {
				int lineNumber = 1;
				int in = 0;
				for (int off = 0; off < index; in = off) {
					off = content.indexOf('\n', in + 1);
					if (off < 0 || off > index) {
						break;			// eof
					}
					++lineNumber;
				}
				IMarker marker = file.createMarker(COMP_MARKER);
				String loc = "Line " + lineNumber + ", Char " + (index - in);
				HashMap<String, Object> attributes= new HashMap<String, Object>();
				attributes.put(IMarker.MESSAGE, "Unreachable rule.");
				attributes.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				attributes.put(IMarker.TEXT, "Unreachable rule.");		
				attributes.put(IMarker.LOCATION, loc);
				attributes.put(IMarker.CHAR_START, new Integer(index));
				attributes.put(IMarker.CHAR_END, new Integer(index + rule.getName().length()));
				attributes.put(IMarker.LINE_NUMBER, lineNumber);
				
				marker.setAttributes(attributes);		

			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
		}
		for (CompressionRule rule: impList) {
			int index = content.indexOf(rule.getName());
			if (index > 0)
				rule.setOffset(index);
		}
	}

	public boolean getType() {
		return quan.getSelection();
	}

	/**
	 * This is the method that will display the compression
	 * @param rule
	 */
	public void changeEditor(final KappaEditor targetEditor){
		if (targetEditor == null) return;
		
		if (targetEditor.isDirty() == true){
			boolean bResult = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
						"RuleStudio - Save Editor", 
						"Contents of the Model Editor have changed, would you like to save?");
			if (bResult == true){
				targetEditor.doSave(new NullProgressMonitor());
			}
		}
		
		_lastTargetEditor = targetEditor;	
		if (press != null && press.isVisible()) {
			press.setText("Loading Validation Map");
		}
		
		IFileEditorInput input = (IFileEditorInput)targetEditor.getEditorInput();
		IFile file = input.getFile();
		final String kappaFile = file.getRawLocation().toOSString();
		final String outputLocation = Activator.getDefault().getPluginLocation() + COMPRESSION_XML;
		final File outputFile = new File(outputLocation);
		if (outputFile.exists() == true){
			outputFile.delete();
		}

		_lastTargetEditor = targetEditor;	
		final JsimJob job = JsimJobFactory.INSTANCE.getJsimJob();
		
		final Job runJob = new Job("Verify Model") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String[] args = makeCommandLineArguments(kappaFile, outputLocation);
				UsageDataCollector.getInstance().addTimeSpanAction(Action.SIMULATOR_JSIM_RUN_COMPRESSION, true);
				final IStatus ret = job.startJob(monitor, args);
	
				parse = null;
				try {
				if (ret.getSeverity() == IStatus.OK && outputFile.exists()) {
					parse = parseCompression(outputFile);
				}

				UsageDataCollector.getInstance().addTimeSpanAction(Action.SIMULATOR_JSIM_RUN_COMPRESSION, false);
				
				UIJob done = new UIJob("Update View") {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (parse != null) {
							CompressionData data = quan.getSelection()?parse.getQuantitative(): parse.getQualitative();
							showCompression(parse.getList(), data);
						} else if (job.getError() != null) {
							Throwable ex = job.getError();
							if (ex != null) {
								showError("Error: " + ex.getMessage());
							} else {
								showError("Error validating model");

							}	
						} else {
							showError ("Unable to show influence map.");
						}
						return Status.OK_STATUS;
					}
					
				};
				done.setSystem(true);
				done.setUser(false);
				done.schedule();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return ret;
			}
			
		};
		if (job.isExternal()) {
			runJob.setUser(true);
		}
		runJob.schedule();

	}
	
	/**
	 * Called when the user click the refresh action in the compression view.
	 */
	public void calculateCompression() {
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		if (part instanceof KappaEditor) {
			_lastTargetEditor = (KappaEditor)part;
		} else {
			if (!shownComputeError) {
				Shell parent = Display.getCurrent().getActiveShell();
				MessageDialog
						.openInformation(parent, "Select Model",
								"You must select model before validating rules.");
				shownComputeError = true;
			}
			return;
		}
		changeEditor(_lastTargetEditor);
	}

	public void handleEvent(Event event) {
		Widget target = event.item;
		if (target instanceof TreeItem) {
			Object data = target.getData();
			if (data instanceof CompressionRule) {
				CompressionRule rule = (CompressionRule)data;
				int offset = rule.getOffset();
				if (offset != -1)
					_lastTargetEditor.highlightAndMoveTo(offset, rule.getName().length());
			}
		}
		
	}
	
	protected String[] makeCommandLineArguments(String kappaFilename, String outputFilename) {
		String[] result = new String[] {
				"--input",
				kappaFilename,
				"--qualitative-compression-job", 
				"--quantitative-compression-job",
				"--xml-session-name",
				outputFilename,
		};
		return result;
	}
	
	public CompressionParse parseCompression(File file) {
		InputStream input = null;
		CompressionParse parse = null;
		try {
			input = new FileInputStream(file);
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			parse = new CompressionParse();
			sp.parse(input, parse);
			input.close();
			return parse;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public void showError(String message) {
		if (tree != null) {
			tree.dispose();
			tree = null;
		}
		
		if (press == null) {
			press = new Label(window, SWT.WRAP);
			press.setBackground(white);
			press.setFont(labelFont);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			gd.verticalAlignment = SWT.TOP;
			gd.horizontalSpan = 3;
			press.setLayoutData(gd);
			
		}
		press.setText(message);
		
		window.pack();
		window.getParent().layout(true);
	}


}
