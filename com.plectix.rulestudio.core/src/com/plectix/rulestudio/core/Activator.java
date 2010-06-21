package com.plectix.rulestudio.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import com.plectix.rulestudio.core.usagedata.UsageDataCollector;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.plectix.rulestudio.core";
	private static final String DEFAULT_CELLUCIDATE_URL = "http://api.cellucidate.com";
	private static String url = null;

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		verifyPlatform();
		UsageDataCollector.getInstance().start();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		UsageDataCollector.getInstance().stop();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public Version getVersion() {
		return getBundle().getVersion();
	}
	
	public String getUrl() {
		if (url == null) {
			String setUrl = System.getProperty("cellucidate");
			if (setUrl instanceof String && setUrl.length() > 0)
				url = setUrl;
			else
				url = DEFAULT_CELLUCIDATE_URL;
		}
		return url;
	}
	
	public void verifyPlatform() {
		final String version = System.getProperty("java.version");
		final String vendor = System.getProperty("java.vendor");
		final String osVersion = System.getProperty("os.version");
		Version os = new Version(osVersion);
		boolean reportError = false;
		if (vendor.startsWith("Apple") && os.getMajor() <= 10 && os.getMinor() <= 5) {
			if (!version.startsWith("1.5")) {
				reportError = true;
			}
		} else if (!version.startsWith("1.6")) {
			reportError = true;
		}
		
		if (reportError) {
			UIJob fixup = new UIJob("Update client JRE.") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					String message;
					if (vendor.startsWith("Apple")) {
						message = "MAC clients before 10.6 require Java 1.5.";
					} else {
						message = "This client requires Java 1.6 for Windows and Linux.";
					}
					Display disp = Display.getCurrent();
					if (disp == null) 
						disp = Display.getDefault();
					Shell parent = disp.getActiveShell();
					MessageDialog.openError(parent, "Java Version Error", message);
					
					return Status.OK_STATUS;
				}
				
			};
			fixup.setSystem(true);
			fixup.setUser(false);
			fixup.schedule();
			
		}

	}

}
