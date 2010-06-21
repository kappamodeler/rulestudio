/**
 * 
 */
package com.plectix.rulestudio.views.jobs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

/**
 * @author bill
 *
 */
public class JsimJobFactory {
	public static JsimJobFactory INSTANCE = new JsimJobFactory();

	public JsimJob getJsimJob() {
		String version = System.getProperty("java.version");
		String vendor = System.getProperty("java.vendor");
		String forceExternal = System.getProperty("force.external");
		if (version.startsWith("1.6") == true && forceExternal == null){
			return new JsimEmbeddedJob();
		}else if (vendor.startsWith("Apple") == true || forceExternal != null){
			return new JsimExternalJob();
		}else{
			MessageDialog.openError(Display.getCurrent().getActiveShell(), 
					"RuleStudio Job Error",
					"We have detected that we are not running inside of the 1.6 JVM. " +
					"RuleStudio Jobs must run with a Java 1.6 JVM.");
			return null;
		}
	}
	
	public Controller getController() {
		String version = System.getProperty("java.version");
		String vendor = System.getProperty("java.vendor");
		String forceExternal = System.getProperty("force.external");
		IPreferenceStore store = com.plectix.rulestudio.core.Activator.getDefault().getPreferenceStore();
		store.setDefault("cellucidate_use_external", false);
		boolean useExt = store.getBoolean("cellucidate_use_external");

		if (version.startsWith("1.6") == true && forceExternal == null && !useExt){
			return new ControlEmbedded();
		}else if (vendor.startsWith("Apple") == true || forceExternal != null || useExt){
			return new ControlExternal();
		}else{
			MessageDialog.openError(Display.getCurrent().getActiveShell(), 
					"RuleStudio Job Error",
					"We have detected that we are not running inside of the 1.6 JVM. " +
					"RuleStudio Jobs must run with a Java 1.6 JVM.");
			return null;
		}
		
	}
}
