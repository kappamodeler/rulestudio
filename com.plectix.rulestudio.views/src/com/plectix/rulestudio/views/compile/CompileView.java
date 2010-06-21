/**
 * 
 */
package com.plectix.rulestudio.views.compile;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.plectix.rulestudio.core.usagedata.Action;
import com.plectix.rulestudio.core.usagedata.UsageDataCollector;
import com.plectix.rulestudio.editors.kappa.KappaEditor;
import com.plectix.rulestudio.views.jobs.JsimJob;
import com.plectix.rulestudio.views.jobs.JsimJobFactory;

/**
 * @author bill
 *
 */
public class CompileView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = CompileView.class.getCanonicalName();
	public static final String KAPPA_EDITOR_ID = "com.plectix.rulestudio.editors.kappa.KappaEditor";
	public static final String REACHABLE_XML = "/resources/reachable.xml";

	private Button run;
	private Label press;
	private Font labelFont = null;
	private Composite window;
	private Table table;
	private Color white;
	private KappaEditor _lastTargetEditor;
	
	/**
	 * 
	 */
	public CompileView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		window = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = layout.marginHeight = 9;
		window.setLayout(layout);
		Display display = Display.getCurrent();
		white = display.getSystemColor(SWT.COLOR_WHITE);
		window.setBackground(white);
		
		run = new Button(window, SWT.PUSH);
		run.setBackground(white);
		run.setText("Compile");
		run.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				compileModel();
				
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
	
			}
		});
		
		press = new Label(window, SWT.WRAP);
		press.setText("Press Compile to compile the rules in a model.");
		press.setBackground(white);
		Font system = display.getSystemFont();
		FontData[] fons = system.getFontData();
		labelFont = new Font (display, fons[0].getName(), 20, SWT.BOLD);
		press.setFont(labelFont);
		GridData gd = new GridData();
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

	public void showCompiled(ArrayList<ArrayList<String>> input) {
		if (window == null) 
			return;
		
		if (press != null) {
			press.dispose();
			press = null;
		}
		
		if (table == null) {
			table = new Table(window, SWT.H_SCROLL | SWT.V_SCROLL);
			table.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					if (e.item instanceof TableItem) {
						findRule(e.item);
					}
				}

				public void widgetSelected(SelectionEvent e) {
					if (e.item instanceof TableItem) {
						findRule(e.item);
					}
				}

			});
			table.setBackground(white);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			gd.horizontalAlignment = SWT.LEFT;
			gd.verticalAlignment = SWT.TOP;
			table.setLayoutData(gd);	
		} else {
			table.removeAll();
		}

		for (ArrayList<String> rule: input) {
			TableItem ruleItem = new TableItem(table, SWT.NONE);
			String label = rule.get(0);
			int lPos = label.lastIndexOf(":");
			if (lPos != -1) {
				int bPos = label.indexOf("_op");
				if (bPos > -1 && bPos < lPos) {
					label = '\'' + label.substring(0, bPos) + '\'';
				} else {
					label = '\'' + label.substring(0, lPos) + '\'';
				}
			}
			ruleItem.setText(rule.get(0));
			ruleItem.setData(label);
			for (int i = 1; i < rule.size(); ++i) {
				TableItem entryItem = new TableItem(table, SWT.NONE);
				entryItem.setText("    " + rule.get(i));
				entryItem.setData(label);
			}
		}

		window.pack();
		window.getParent().layout(true);
	}
	
	private void findRule(Widget item) {
		Object rLabel = item.getData();
		if (rLabel instanceof String && _lastTargetEditor != null) {
			String toFind = (String)rLabel;
			if (toFind.charAt(0) == '\'')
				_lastTargetEditor.highlightKeyword(toFind, toFind);
			else if (!_lastTargetEditor.highlightRuleText(toFind)) {
				int offset = toFind.indexOf("->");
				String rev = toFind.substring(offset+3, toFind.length()) + "->"
					+ toFind.substring(0, offset);
				_lastTargetEditor.highlightRuleText(rev);
			}
		}	
	}

	/**
	 * This is the method that will display the compiled file
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
			press.setText("Compiling Model");
		}
		
		run.setEnabled(false);
		IFileEditorInput input = (IFileEditorInput)_lastTargetEditor.getEditorInput();
		IFile file = input.getFile();
		final  String kappaFile = file.getRawLocation().toOSString();

		final JsimJob job = JsimJobFactory.INSTANCE.getJsimJob();
		
		Job runJob = new Job("Compile Model") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String[] args = makeCommandLineArguments(kappaFile);
				UsageDataCollector.getInstance().addTimeSpanAction(Action.SIMULATOR_JSIM_GENERATEMAP_COMPILE, true);
				IStatus ret = job.startJob(monitor, args);
				
				ArrayList<ArrayList<String>> output = null;
				if (ret.getSeverity() == IStatus.OK) {
					output = parse(job.getConsole());
				}
				final ArrayList<ArrayList<String>> out = output;
				UsageDataCollector.getInstance().addTimeSpanAction(Action.SIMULATOR_JSIM_GENERATEMAP_REACHABLE, false);
				
				UIJob done = new UIJob("Update View") {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (out != null) {
							showCompiled(out);
						} else if (job.getError() != null) {
							Throwable th = job.getError();
							String message = job.getError().getMessage();
							if (message == null) {
								message = th.getClass().getName() + " compiling model.";
							}
							showError(message);
						} else {
							showError("Unable to compile model.");
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
	public void compileModel() {
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		if (part instanceof KappaEditor) {
			_lastTargetEditor = (KappaEditor)part;
		} else {
				Shell parent = Display.getCurrent().getActiveShell();
				MessageDialog
						.openInformation(parent, "Select Model",
								"You must select the model to compile.");
			return;
		}
		changeEditor(_lastTargetEditor);
	}
	
	protected String[] makeCommandLineArguments(String kappaFile) {

		return new String[]{
				"--compile",
				kappaFile,
			};
	}


	public void showError(String message) {
		if (table != null) {
			table.dispose();
			table = null;
		}
		
		if (press == null) {
			press = new Label(window, SWT.WRAP);
			press.setBackground(white);
			press.setFont(labelFont);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			gd.verticalAlignment = SWT.TOP;
			press.setLayoutData(gd);
			
		}
		press.setText(message);
		
		window.pack();
		window.getParent().layout(true);
	}
	
	private final static String DASHES = "------";
	
	/**
	 * Parse stdout from jsim for compiled.
	 * 
	 * @param input Console output from jsim
	 * @return ArrayList of compiled entries.  Each entry is an arraylist starting with
	 * the rule and then followed by the operatons the rule is compiled into.
	 */
	private ArrayList<ArrayList<String>> parse(String input) {
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		StringTokenizer st = new StringTokenizer(input, "\r\n");
		ArrayList<String> current = null;
		try {
			while (st.hasMoreTokens()) {
				String line = st.nextToken();
				if (hasCompOp(line)) {
					if (current == null) {
						current = new ArrayList<String>();
					}
					current.add(line);
				} else if (line.startsWith(DASHES)) {
					line = st.nextToken();
					if (current == null) {
						current = new ArrayList<String>();
					}
					current.add(0, line);
					st.nextToken(); // skip end dash line
					ret.add(current);
					current = null;
				}
			}
		} catch (NoSuchElementException ne) {
			ne.printStackTrace();
		}

		return ret;
	}

	/*
	 *) BREAK Example: BRK (#0,a) (#1,x) 
	 *) DELETE Example: DEL #0 
	 *) ADD Example: ADD a#0(x) 
	 *) BIND Example: BND (#1,x) (#0,a) 
	 *) MODIFY Example: MOD (#1,x) with p 
	 */
	
	private static final String[] compOps = { "BRK", "DEL", "ADD", "BND", "MOD" };
	private boolean hasCompOp(String line) {
		for (String op: compOps) {
			if (line.startsWith(op))
				return true;
		}
		return false;
	}
	

}
