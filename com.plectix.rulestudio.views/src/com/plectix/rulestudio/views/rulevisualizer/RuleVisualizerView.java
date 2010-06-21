package com.plectix.rulestudio.views.rulevisualizer;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.plectix.rulestudio.editors.kappa.KappaEditor;
import com.plectix.rulestudio.editors.view.RuleChangeListener;
import com.plectix.rulestudio.views.Activator;
import com.plectix.rulestudio.views.utils.TemplateCreator;


/**
 * This class provides the functionality to display the rule visualizer.  The 
 * called to change the rule being visualized happen outside of the view. 
 * The outline view will tell the view what rule should be visualized.
 * 
 */

public class RuleVisualizerView extends ViewPart implements RuleChangeListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = RuleVisualizerView.class.getCanonicalName();
	protected final static String REPLACE_KEY = "{REPLACE_WITH_KAPPA_RULE}";

	private Browser _browser;
	private TemplateCreator _htmlFileTemplate = null;

	/**
	 * The constructor.
	 */
	public RuleVisualizerView() {
	}
	
	/**
	 * This can be called by anyone who wants to change the rule
	 * displayed in the rule editor.  If the content supplied doesn't
	 * start with a "'" it is ignored.
	 * @param rule
	 */
	public void changeRule(KappaEditor editor, String name, String rule){
		try {
			//We only display rules in the rule editor.		
			rule = rule.trim();
			
			//When the user has a \ in the rule there will
			//also be end-of-line characters that need to be removed.
			//If they aren't the rule visualizer won't display them.
			rule = rule.replace('\r', ' ').replace('\n', ' ');
			
			//we need to remove the rule name and the 
			//rule's rate.
			int intPos = rule.indexOf('\'', 1);
			if (intPos != -1){
				rule = rule.substring(intPos + 1);
			}
			
			intPos = rule.lastIndexOf('@');
			String rateString = "";
			if (intPos != -1){
				if (intPos < rule.length()+1) {
					int ratePos = intPos+1;
					if (ratePos != -1) {
						int secondPos = rule.indexOf(',', ratePos);
						if (secondPos != -1) {
							String rate = rule.substring(intPos+1, secondPos);
							rateString = "\", \"forwardRate\":	\"" + rate.trim() + "\", ";
							rate = rule.substring(secondPos + 1);
							rateString += "\"backwardRate\":	\"" + rate.trim();
						} else {
							String rate = rule.substring(intPos+1);
							rateString = "\", \"forwardRate\":	\"" + rate.trim() + "\", "
								+ "\"backwardRate\":	\"";
						}
					} else {
						rateString = "\", \"forwardRate\":	\"\", "
							+ "\"backwardRate\":	\"";
					}
				}
				rule = rule.substring(0, intPos);
			}
			rule = rule.trim();
			rule = rule + rateString;
			_htmlFileTemplate.replaceAndWrite(REPLACE_KEY, rule, 
				Activator.getDefault().getPluginLocation()+"/resources/RuleEditor.html");

			_browser.setUrl(Activator.getDefault().getUrlPrefix() + "RuleEditor.html?time="+System.currentTimeMillis());
			
			//Make sure we can see the rule editor when we display it.
			try {
				PlatformUI.getWorkbench().
						getActiveWorkbenchWindow().getActivePage().showView(ID);
			} catch (PartInitException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), 
					"Error opening Rule Visualizer", e.getLocalizedMessage());
		}
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		_browser = new Browser(parent, SWT.NULL);

		try {	
			//We need to load up the HTML template file.
			_htmlFileTemplate = new TemplateCreator();
			_htmlFileTemplate.loadTemplate(Activator.getDefault().getPluginLocation()+"/resources/RuleEditorTemplate.html");

			_htmlFileTemplate.replaceAndWrite(REPLACE_KEY, "", 
					Activator.getDefault().getPluginLocation()+"/resources/RuleEditor.html");

			//_browser.setText("<html><h1>"+Activator.getDefault().getUrlPrefix() + "</h1></html>");
			_browser.setUrl(Activator.getDefault().getUrlPrefix() + "RuleEditor.html?time="+System.currentTimeMillis());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		_browser.setFocus();
	}
}