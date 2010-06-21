package com.plectix.rulestudio.views.influencemap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.plectix.rulestudio.core.usagedata.Action;
import com.plectix.rulestudio.core.usagedata.UsageDataCollector;
import com.plectix.rulestudio.editors.kappa.KappaEditor;
import com.plectix.rulestudio.editors.view.RuleChangeListener;
import com.plectix.rulestudio.views.Activator;
import com.plectix.rulestudio.views.jobs.JsimJob;
import com.plectix.rulestudio.views.jobs.JsimJobFactory;
import com.plectix.rulestudio.views.utils.TemplateCreator;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class InfluenceMapView extends ViewPart implements RuleChangeListener {

	public static final String INFLUENCE_MAP_XML = "/resources/influence_map.xml";

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = InfluenceMapView.class.getCanonicalName();
	public static final String KAPPA_EDITOR_ID = "com.plectix.rulestudio.editors.kappa.KappaEditor";
	
	private static final String HTML_HEADER = "<html><header><style>h1 { font-size: 16pt; font-family: Verdana,Arial; text-align: center; } </style></header><body><h1>";
	private static final String HTML_FOOTER = "</h1></body></html>";
	protected final static String REPLACE_RULEID = "{REPLACE_WITH_RULE_ID_PAIR}";
	protected HashMap<String, String> ruleMap = null;
	protected String defaultRule = null;
	protected String lastRule = null;
	protected InfluenceMapView _myView = null;

	private Browser 	_browser;
	
	private TemplateCreator _htmlFileTemplate = null;

	private IEditorPart		_lastTargetEditor = null;
	
	/**
	 * The constructor.
	 */
	public InfluenceMapView() {
		_myView = this;
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {		
		_browser = new Browser(parent, SWT.NULL);
		_browser.setText(HTML_HEADER + "To see an Influence Map, open a model and press the " +
						"&quot;Show Influence Map&quot; button." + HTML_FOOTER);
		
		try {	
			//We need to load up the HTML template file.
			_htmlFileTemplate = new TemplateCreator();
			_htmlFileTemplate.loadTemplate(Activator.getDefault().getPluginLocation()+"/resources/InfluenceMapTemplate.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Need to remove the part listener
	 */
	public void dispose() {		
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		UsageDataCollector.getInstance().addOneTimeAction(Action.WORKBENCH_VIEW_INFLUENCEMAP_FOCUS);

		_browser.setFocus();
	}
	
	/**
	 * This is the method that will display the influence map
	 * @param rule
	 */
	public void changeEditor(final IEditorPart targetEditor, final String rule, final boolean reportError){
		if (targetEditor == null) return;
		
		if (targetEditor.isDirty() == true){
			boolean bResult = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
						"RuleStudio - Save Editor", 
						"The model has changed, would you like to save the changes?");
			if (bResult == true){
				targetEditor.doSave(new NullProgressMonitor());
			}
		}
		
		IFileEditorInput input = (IFileEditorInput)targetEditor.getEditorInput();
		IFile file = input.getFile();
		final String kappaFile = file.getRawLocation().toOSString();
		final String outputLocation = Activator.getDefault().getPluginLocation() + INFLUENCE_MAP_XML;
		final File outputFile = new File(outputLocation);
		if (outputFile.exists() == true){
			outputFile.delete();
		}

		_lastTargetEditor = targetEditor;	
		defaultRule = rule;
		_browser.setText(HTML_HEADER + "Preparing to display Influence Map..." + HTML_FOOTER);
		final JsimJob job = JsimJobFactory.INSTANCE.getJsimJob();
		
		final Job runJob = new Job("Build Influence Map") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String[] args = makeCommandLineArguments(kappaFile, outputLocation);
				UsageDataCollector.getInstance().addTimeSpanAction(Action.SIMULATOR_JSIM_GENERATEMAP_REACHABLE, true);
				final IStatus ret = job.startJob(monitor, args);
	
				final HashMap<String, String> list = new HashMap<String, String>();
				final MapParse dh = new MapParse(list);

				try {
				if (ret.getSeverity() == IStatus.OK && outputFile.exists()) {
					InputStream input = new FileInputStream(outputFile);
					SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
					sp.parse(input, dh);
					input.close();
					ruleMap = list;
				}

				UsageDataCollector.getInstance().addTimeSpanAction(Action.SIMULATOR_JSIM_GENERATEMAP_INFLUENCEMAP, false);
				
				UIJob done = new UIJob("Update View") {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (list.size() > 0) {
							try {
							InfluenceMapView.this.addMap(list);
							InfluenceMapView.this.setDefault(defaultRule, dh.getFirst());
							InfluenceMapView.this.updateMapUrl(defaultRule);
							} catch (IOException io) {
								io.printStackTrace();
							}
						} else if (job.getError() != null) {
							Throwable ex = job.getError();
							if (ex != null) {
								_browser.setText(HTML_HEADER + "Error: " + ex.getMessage() + " processing model." + HTML_FOOTER);
							} else {
								_browser.setText(HTML_HEADER + "Error processing model" + HTML_FOOTER);

							}	
						} else {
							_browser.setText(HTML_HEADER + "Unable to show influence map." + HTML_FOOTER);
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
		if (reportError && job.isExternal()) {
			runJob.setUser(true);
		}
		runJob.schedule();
	}
	
	protected void updateMapUrl(String rule) throws IOException {
		if (rule == null) 
			throw new IOException("No rule passed to display.");
		String rId = ruleMap.get(rule);
		if (rId == null) {
			rule = defaultRule;
			rId = ruleMap.get(rule);
			if (rId == null)
				throw new IOException("No ID for rule " + rule);
		}
		lastRule = rule;
		_htmlFileTemplate.replaceAndWrite(REPLACE_RULEID, '\'' + rule + '\'', 
				Activator.getDefault().getPluginLocation()
					+ "/resources/InfluenceMap.html");

		_browser.setUrl(Activator.getDefault().getUrlPrefix()
				+ "InfluenceMap.html?time" + System.currentTimeMillis());
	}

	
	/**
	 */
	public void refresh() {

		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		if (part != null && part.getEditorSite().getId().equals(
				"com.plectix.rulestudio.editors.kappa.KappaEditor") == true) {
			_lastTargetEditor = part;
		}
		if (_lastTargetEditor != null)
			changeEditor(_lastTargetEditor, lastRule, true);
	}
	
	protected void addMap(HashMap<String, String> map) {
		ruleMap = map;
	}
	
	protected void setDefault(String name, String backup) {
		if (ruleMap != null) {
			if (ruleMap.containsKey(name))
				defaultRule = name;
			else
				defaultRule = backup;
		} else if (name != null) {
			defaultRule = name;
		} else {
			defaultRule = backup;
		}
	}

	public void changeRule(KappaEditor edit, String ruleName, String ruleData) {
		if (!edit.equals(_lastTargetEditor))
			changeEditor(edit, ruleName, false);
		else if (ruleMap != null) {
			try {
				updateMapUrl(ruleName);
				defaultRule = ruleName;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	protected String[] makeCommandLineArguments(String kappaFilename, String outputFilename) {
		
		String[] result = new String[] {
				"--input",
				kappaFilename,
				"--contact-map-job",
				"--generate-map-job",
				"--xml-session-name",
				outputFilename,
		};
		return result;
	}
}