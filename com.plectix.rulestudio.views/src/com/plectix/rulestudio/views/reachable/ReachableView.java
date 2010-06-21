/**
 * 
 */
package com.plectix.rulestudio.views.reachable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
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
public class ReachableView extends ViewPart implements PropertyChangeListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = ReachableView.class.getCanonicalName();
	public static final String KAPPA_EDITOR_ID = "com.plectix.rulestudio.editors.kappa.KappaEditor";
	public static final String REACHABLE_XML = "/resources/reachable.xml";

	private Button run;
	private Button enumerate;
	private Label press;
	private Font labelFont = null;
	private Composite window;
	private Tree tree;
	private Color white;
	private KappaEditor _lastTargetEditor;
	private static boolean shownComputeError = false;
	
	/**
	 * 
	 */
	public ReachableView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		window = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = layout.marginHeight = 9;
		window.setLayout(layout);
		Display display = Display.getCurrent();
		white = display.getSystemColor(SWT.COLOR_WHITE);
		window.setBackground(white);
		
		run = new Button(window, SWT.PUSH);
		run.setBackground(white);
		run.setText("Analyze");
		run.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				calculateReachable();
				
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
	
			}
		});
		
		enumerate = new Button(window, SWT.CHECK);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.LEFT;
		enumerate.setLayoutData(gd);
		enumerate.setBackground(white);
		enumerate.setText("Enumerate Complexes");
		enumerate.setSelection(false);
		
		press = new Label(window, SWT.WRAP);
		press.setText("Press Analyze to compute the reachable rules for a model.");
		press.setBackground(white);
		Font system = display.getSystemFont();
		FontData[] fons = system.getFontData();
		labelFont = new Font (display, fons[0].getName(), 20, SWT.BOLD);
		press.setFont(labelFont);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalSpan = 2;
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

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub

	}

	public void showReachable(Reachable reach) {
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
			gd.horizontalAlignment = SWT.LEFT;
			gd.verticalAlignment = SWT.TOP;
			gd.horizontalSpan = 2;
			tree.setLayoutData(gd);	
		} else {
			tree.removeAll();
			GridData gd = (GridData)tree.getLayoutData();
			gd.exclude = false;
			tree.setLayoutData(gd);
			tree.setVisible(true);
			
		}

		buildViewTree(reach.getViews(), "Views");
		buildViewTree(reach.getSubViews(), "SubViews");
		
		ArrayList<String> speciesList = reach.getSpecies();
		if (speciesList != null) {
			TreeItem species = new TreeItem(tree, SWT.NONE);
			species.setText("Species");
			for (String entry: speciesList) {
				TreeItem ent = new TreeItem(species, SWT.NONE);
				ent.setText(entry);
			}
			species.setExpanded(true);
		}
		
		window.pack();
		window.getParent().layout(true);
	}

	private void buildViewTree(ArrayList<ArrayList<String>> viewList, String name) {
		if (viewList != null) {
			TreeItem uri = new TreeItem(tree, SWT.NONE);
			uri.setText(name);
			
			for (ArrayList<String> agents: viewList) {
				String agent = agents.get(0);
				TreeItem top = new TreeItem(uri, SWT.NONE);
				top.setText(agent);
				for (int i = 1; i < agents.size(); ++i) {
					TreeItem item = new TreeItem(top, SWT.NONE);
					item.setText(agents.get(i));
				}
				top.setExpanded(true);
			}
			uri.setExpanded(true);
		}
	}


	/**
	 * This is the method that will display the reachables
	 * @param rule
	 */
	public void changeEditor(final KappaEditor targetEditor){
		if (targetEditor == null) return;
		
		if (targetEditor.isDirty() == true){
			boolean bResult = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
						"RuleStudio - Save Changes", 
						"Contents of the Model have changed, would you like to save them?");
			if (bResult == true){
				targetEditor.doSave(new NullProgressMonitor());
			}
		}
		
		_lastTargetEditor = targetEditor;	
		if (press != null) {
			press.setText("Analyzing Model");
		}
		
		run.setEnabled(false);
		IFileEditorInput input = (IFileEditorInput)_lastTargetEditor.getEditorInput();
		IFile file = input.getFile();
		final  String kappaFile = file.getRawLocation().toOSString();
		final String outputLocation = Activator.getDefault().getPluginLocation() + REACHABLE_XML;
		final File outputFile = new File(outputLocation);
		if (outputFile.exists() == true){
			outputFile.delete();
		}

		final JsimJob job = JsimJobFactory.INSTANCE.getJsimJob();
		final boolean doEnum = enumerate.getSelection();
		
		Job runJob = new Job("Get Reachables") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Reachable reachRet = null;
				String[] args = makeCommandLineArguments(kappaFile, outputLocation, doEnum);
				UsageDataCollector.getInstance().addTimeSpanAction(Action.SIMULATOR_JSIM_GENERATEMAP_REACHABLE, true);
				IStatus ret = job.startJob(monitor, args);
				
				if (ret.getSeverity() == IStatus.OK && outputFile.exists()) {
					reachRet = parseReachable(outputFile);
				}
				final Reachable rch = reachRet;
				UsageDataCollector.getInstance().addTimeSpanAction(Action.SIMULATOR_JSIM_GENERATEMAP_REACHABLE, false);
				
				UIJob done = new UIJob("Update View") {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						String msg = job.getConsole();
						System.out.println(msg);
						if (rch != null) {
							showReachable(rch);
						} else if (job.getError() != null) {
							showError(job.getError().getMessage());
						} else {
							showError("Unable to analyze model.");
						}
						run.setEnabled(true);
						return Status.OK_STATUS;
					}
					
				};
				done.setSystem(true);
				done.setUser(false);
				done.schedule();
				return ret;
			}
			
		};
		if (job.isExternal()) {
			runJob.setUser(true);
		}
		runJob.schedule();
	}
	
	/**
	 * Called when the presses the analyze button.
	 */
	public void calculateReachable() {
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		if (part instanceof KappaEditor) {
			_lastTargetEditor = (KappaEditor)part;
		} else {
			if (!shownComputeError) {
				Shell parent = Display.getCurrent().getActiveShell();
				MessageDialog
						.openInformation(parent, "Select Model",
								"You must select model before getting reachables.");
				shownComputeError = true;
			}
			return;
		}
		changeEditor(_lastTargetEditor);
	}
	
	protected String[] makeCommandLineArguments(String kappaFile, String outputLocation, boolean enumerate) {
		String[] result = null;

		if (enumerate) {
			result = new String[]{
				"--no-dump-iteration-number", 
				"--no-dump-rule-iteration",  
				"--contact-map",
				kappaFile,
				"--xml-session-name",
				outputLocation,
				"--compute-local-views",
				"--compute-sub-views",
				"--enumerate-complexes",
			};
		} else {
			result = new String[]{
				"--no-dump-iteration-number", 
				"--no-dump-rule-iteration",  
				"--contact-map",
				kappaFile,
				"--xml-session-name",
				outputLocation,
				"--compute-local-views",
				"--compute-sub-views",
			};
		}
		return result;
	}

	public Reachable parseReachable(File file) {
		InputStream input = null;
		Reachable reach = new Reachable();
		try {
			input = new FileInputStream(file);
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			ReachableParse parse = new ReachableParse();
			sp.parse(input, parse);
			input.close();
			reach = parse.getReachable();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return reach;
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
			gd.horizontalSpan = 2;
			press.setLayoutData(gd);
			
		}
		press.setText(message);
		
		window.pack();
		window.getParent().layout(true);
	}
}
