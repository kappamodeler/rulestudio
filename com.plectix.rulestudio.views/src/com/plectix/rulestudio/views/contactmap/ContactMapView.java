package com.plectix.rulestudio.views.contactmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.plectix.rulestudio.core.usagedata.Action;
import com.plectix.rulestudio.core.usagedata.UsageDataCollector;
import com.plectix.rulestudio.editors.kappa.KappaEditor;
import com.plectix.rulestudio.editors.view.AgentChangeListener;
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

public class ContactMapView extends ViewPart implements AgentChangeListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = ContactMapView.class.getCanonicalName();
	public static final String KAPPA_EDITOR_ID = "com.plectix.rulestudio.editors.kappa.KappaEditor";
	
	private static final String HTML_HEADER = "<html><header><style>h1 { font-size: 16pt; font-family: Verdana,Arial; text-align: center; } </style></header><body><h1>";
	private static final String HTML_FOOTER = "</h1></body></html>";
	protected final static String REPLACE_KEY = "{REPLACE_WITH_AGENT_NAME}";

	public static final String CONTACT_MAP_XML = "/resources/contact_map.xml";
	public static final String CONTACT_MAP_TEMP_XML = "/resources/contact_map_temp.xml";
	
	protected IEditorPart 	_targetEditor = null;
	
	protected String _agentName = "";

	private Browser 	_browser;
	
	private TemplateCreator _htmlFileTemplate = null;

	private IEditorPart		_lastTargetEditor = null;
	
	
	
	/**
	 * The constructor.
	 */
	public ContactMapView() {
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {		
		_browser = new Browser(parent, SWT.NULL);
		_browser.setText(HTML_HEADER + "To see a Contact Map, open up a model and press the " +
						"&quot;Show Contact Map&quot; button." + HTML_FOOTER);
		
		try {	
			//We need to load up the HTML template file.
			_htmlFileTemplate = new TemplateCreator();
			_htmlFileTemplate.loadTemplate(Activator.getDefault().getPluginLocation()+"/resources/ContactMapTemplate.html");
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
		UsageDataCollector.getInstance().addOneTimeAction(Action.WORKBENCH_VIEW_CONTACTMAP_FOCUS);
		_browser.setFocus();
	}
	
	/**
	 * This is the method that will display the contact map
	 * @param rule
	 */
	public void changeEditor(final IEditorPart targetEditor){
		if (targetEditor == null) return;
		
		if (targetEditor.isDirty() == true){
			boolean bResult = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
						"RuleStudio - Save Editor", 
						"The model has changed, would you like to save it?");
			if (bResult == true){
				targetEditor.doSave(new NullProgressMonitor());
			}
		}
		
		_lastTargetEditor = targetEditor;	
		_browser.setText(HTML_HEADER + "Preparing to display Contact Map..." + HTML_FOOTER);

		IFileEditorInput input = (IFileEditorInput)_lastTargetEditor.getEditorInput();
		IFile file = input.getFile();
		final  String kappaFile = file.getRawLocation().toOSString();
		final String outputLocation = Activator.getDefault().getPluginLocation() + CONTACT_MAP_XML;
		final File outputFile = new File(outputLocation);
		if (outputFile.exists() == true){
			outputFile.delete();
		}

		final String tempOutputLocation = Activator.getDefault().getPluginLocation() + CONTACT_MAP_TEMP_XML;
		final File tempOutputFile = new File(tempOutputLocation);
		if (tempOutputFile.exists() == true){
			tempOutputFile.delete();
		}

		final JsimJob job = JsimJobFactory.INSTANCE.getJsimJob();
		
		Job runJob = new Job("Build Contact Map") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String[] args = makeCommandLineArguments(kappaFile, tempOutputLocation);
				UsageDataCollector.getInstance().addTimeSpanAction(Action.SIMULATOR_JSIM_GENERATEMAP_CONTACTMAP, true);
				final IStatus ret = job.startJob(monitor, args);
				
				if (tempOutputFile.exists()) {
					try {
					ContactMapParser cmp = new ContactMapParser(outputFile);
					InputStream input = new FileInputStream(tempOutputFile);
					SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
					sp.parse(input, cmp);
					input.close();
					_agentName = cmp.getFirstAgent();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				UsageDataCollector.getInstance().addTimeSpanAction(Action.SIMULATOR_JSIM_GENERATEMAP_CONTACTMAP, false);
				
				UIJob done = new UIJob("Update View") {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (ret.getSeverity() == IStatus.OK && outputFile.exists()){
							try {
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().
																getActivePage().showView(ContactMapView.ID);
							} catch (PartInitException e) {
								e.printStackTrace();
							}
							
							try {
								_htmlFileTemplate.replaceAndWrite(REPLACE_KEY,
									_agentName, 
									Activator.getDefault().getPluginLocation()+"/resources/ContactMap.html");

								_browser.setUrl(Activator.getDefault().getUrlPrefix() + "ContactMap.html?time"+System.currentTimeMillis());
							} catch (Exception e) {
								MessageDialog.openError(Display.getCurrent().getActiveShell(), 
										"Error opening Contact Map", e.getLocalizedMessage());
							}
						}else if (job.getError() != null) {
							Throwable th = job.getError();
							_browser.setText(HTML_HEADER + "Error: " + th.getMessage() + HTML_FOOTER);
						}else if (job.getError() != null) {
							_browser.setText(HTML_HEADER + "Error creating Contact Map." + HTML_FOOTER);
						}
						UsageDataCollector.getInstance().addTimeSpanAction(Action.SIMULATOR_JSIM_GENERATEMAP_CONTACTMAP, false);
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
	 * Called when the user clicks on the build action in the contact map view.
	 */
	public void refresh() {

		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		if (part.getEditorSite().getId().equals(
				"com.plectix.rulestudio.editors.kappa.KappaEditor") == true) {
			_lastTargetEditor = part;
		}
		if (_lastTargetEditor != null)
			changeEditor(_lastTargetEditor);
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

	public void changeAgent(KappaEditor editor, String agentName) {
		_agentName =agentName;
		if (editor == _lastTargetEditor) {
			try {
			_htmlFileTemplate.replaceAndWrite(REPLACE_KEY,
					_agentName, 
					Activator.getDefault().getPluginLocation()+"/resources/ContactMap.html");

				_browser.setUrl(Activator.getDefault().getUrlPrefix() + "ContactMap.html?time"+System.currentTimeMillis());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}

	
}